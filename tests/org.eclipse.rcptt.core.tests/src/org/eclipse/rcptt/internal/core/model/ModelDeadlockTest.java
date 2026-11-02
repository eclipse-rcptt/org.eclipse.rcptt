/*******************************************************************************
 * Copyright (c) 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.core.model;

import static java.lang.System.currentTimeMillis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.tests.HangWatchdog;
import org.eclipse.rcptt.core.tests.Stress;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.junit.runner.Description;

/**
 * Reproducer for deadlocks between workspace scheduling rules and RCPTT model
 * indexer described in
 * https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176
 * <p>
 * Concurrently runs:
 * <ul>
 * <li>a model listener querying index (like {@code TagsWatcher})</li>
 * <li>workspace modifications under the root rule (like
 * {@code WorkspaceContextProcessor.apply()})</li>
 * <li>incremental builds ({@code Q7Builder} waits for indexer)</li>
 * <li>moves, deletes, file system changes bypassing workspace, project
 * close/open</li>
 * <li>plain model reads</li>
 * </ul>
 * Fails if any activity can't stop in time, dumping threads, jobs, rule owners
 * and indexer state with {@link HangWatchdog}. Unexpected runtime exceptions in
 * activities fail the test too.
 */
public class ModelDeadlockTest {
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	private static final IProject PROJECT = WORKSPACE.getRoot().getProject("DEADLOCK");
	private static final long DURATION_MS = Stress.millis(10_000);
	private static final long STOP_TIMEOUT_MS = 60_000;
	private static final int FILES = 10;
	private static final String INDEX_JOB_MANAGER = "org.eclipse.rcptt.internal.core.jobs.JobManager";

	private final TestName testName = new TestName();
	@Rule
	public final RuleChain rules = RuleChain.outerRule(testName)
			.around(new HangWatchdog(DURATION_MS + STOP_TIMEOUT_MS + 30_000))
			.around(Timeout.millis(DURATION_MS + STOP_TIMEOUT_MS + 60_000));

	private volatile boolean stop = false;
	private boolean deadlocked = false;
	private final Map<String, AtomicLong> progress = new ConcurrentHashMap<>();
	private final List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
	private final List<Thread> threads = new ArrayList<>();
	private final IElementChangedListener tagsWatcher = event -> {
		Q7SearchCore.findAllTagReferences();
		count("tagsWatcher");
	};

	@Before
	public void before() throws CoreException {
		Q7NamedElementTest.enableSyncOnAccess(false);
		deleteProjects();
		PROJECT.create(null);
		PROJECT.open(null);
		RcpttNature.updateProjectNature(PROJECT, true);
	}

	@After
	public void after() throws CoreException, InterruptedException {
		RcpttCore.removeElementChangedListener(tagsWatcher);
		stop = true;
		if (deadlocked) {
			return; // Cleanup would hang too
		}
		for (Thread thread : threads) {
			thread.join(STOP_TIMEOUT_MS);
		}
		deleteProjects();
	}

	@Test
	public void noDeadlockBetweenWorkspaceRulesAndIndexer() throws CoreException, IOException, InterruptedException {
		Stress.assumeIssue176Enabled();
		IQ7Folder root = RcpttCore.create(PROJECT).getRootFolder();
		IFolder moved = PROJECT.getFolder("moved");
		moved.create(true, true, null);
		List<IFile> files = new ArrayList<>();
		for (int i = 0; i < FILES; i++) {
			ITestCase testcase = root.createTestCase("t" + i, true, null);
			files.add((IFile) testcase.getResource());
		}
		byte[] content = Files.readAllBytes(location(files.get(0)));
		RcpttCore.addElementChangedListener(tagsWatcher);

		start("contextApply", () -> WORKSPACE.run(monitor -> {
			IFile file = random(files);
			if (file.exists()) {
				file.setContents(new ByteArrayInputStream(content), IResource.FORCE, monitor);
			}
			IFile extra = PROJECT.getFile("extra.test");
			if (extra.exists()) {
				extra.delete(true, monitor);
			} else {
				extra.create(new ByteArrayInputStream(content), IResource.FORCE, monitor);
			}
		}, WORKSPACE.getRoot(), IWorkspace.AVOID_UPDATE, null));
		start("build", () -> PROJECT.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null));
		start("move", () -> {
			IFile file = random(files);
			IFile target = moved.getFile(file.getName());
			if (file.exists()) {
				file.move(target.getFullPath(), true, null);
			} else if (target.exists()) {
				target.move(file.getFullPath(), true, null);
			}
		});
		start("outOfSyncWrite", () -> {
			IFile file = random(files);
			Path path = location(file);
			if (path != null && Files.exists(path)) {
				Files.write(path, content);
				Files.setLastModifiedTime(path, FileTime.from(Instant.now().plusSeconds(3)));
			}
			if (ThreadLocalRandom.current().nextInt(10) == 0) {
				PROJECT.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
		});
		start("closeOpen", () -> {
			Thread.sleep(500);
			PROJECT.close(null);
			PROJECT.open(null);
		});
		start("read", () -> {
			ITestCase testcase = (ITestCase) RcpttCore.create(random(files));
			if (testcase.exists()) {
				testcase.getNamedElement();
			}
			Q7SearchCore.findAllTagReferences();
		});

		Thread.sleep(DURATION_MS);
		stop = true;
		long deadline = currentTimeMillis() + STOP_TIMEOUT_MS;
		for (Thread thread : threads) {
			thread.join(Math.max(1, deadline - currentTimeMillis()));
		}
		List<String> hung = threads.stream().filter(Thread::isAlive).map(Thread::getName).collect(Collectors.toList());
		if (!hung.isEmpty()) {
			HangWatchdog.dump(Description.createTestDescription(getClass(), testName.getMethodName()),
					"Activities did not stop in " + STOP_TIMEOUT_MS + " ms: " + hung);
			deadlocked = !recover(30_000);
			Assert.fail("Deadlock: activities " + hung + " did not stop in " + STOP_TIMEOUT_MS
					+ " ms. See HangWatchdog dump in stderr. Progress: " + progress);
		}
		synchronized (failures) {
			if (!failures.isEmpty()) {
				AssertionError error = new AssertionError(
						failures.size() + " unexpected failures. Progress: " + progress, failures.get(0));
				failures.stream().skip(1).limit(10).forEach(error::addSuppressed);
				throw error;
			}
		}
		System.out.println(getClass().getSimpleName() + " progress: " + progress);
	}

	/**
	 * Interruption cancels index waits ({@code JobManager.performConcurrentJob}),
	 * which breaks known deadlocks, so that cleanup and following tests can run.
	 * 
	 * @return true if all activities stopped and nobody waits for indexer
	 */
	private boolean recover(long timeoutMs) throws InterruptedException {
		RcpttCore.removeElementChangedListener(tagsWatcher);
		long deadline = currentTimeMillis() + timeoutMs;
		while (currentTimeMillis() < deadline) {
			threads.forEach(Thread::interrupt);
			boolean waiting = interruptIndexWaits();
			if (!waiting && threads.stream().noneMatch(Thread::isAlive)) {
				return true;
			}
			Thread.sleep(100);
		}
		return false;
	}

	private static boolean interruptIndexWaits() {
		boolean found = false;
		for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
			for (StackTraceElement element : entry.getValue()) {
				if (INDEX_JOB_MANAGER.equals(element.getClassName())
						&& "performConcurrentJob".equals(element.getMethodName())) {
					entry.getKey().interrupt();
					found = true;
					break;
				}
			}
		}
		return found;
	}

	private interface Activity {
		void run() throws Exception;
	}

	private void start(String name, Activity activity) {
		Thread thread = new Thread(() -> {
			while (!stop) {
				try {
					activity.run();
					count(name);
				} catch (CoreException | OperationCanceledException e) {
					// Expected: resources disappear and projects close concurrently
					count(name + ".expectedFailures");
				} catch (InterruptedException e) {
					return;
				} catch (Throwable e) {
					failures.add(new AssertionError(name + " failed", e));
				}
			}
		}, getClass().getSimpleName() + " " + name);
		thread.setDaemon(true);
		threads.add(thread);
		thread.start();
	}

	private void count(String name) {
		progress.computeIfAbsent(name, ignored -> new AtomicLong()).incrementAndGet();
	}

	private static <T> T random(List<T> list) {
		return list.get(ThreadLocalRandom.current().nextInt(list.size()));
	}

	private static Path location(IFile file) {
		return file.getLocation() == null ? null : Path.of(file.getLocation().toOSString());
	}

	private static void deleteProjects() throws CoreException {
		for (IProject project : WORKSPACE.getRoot().getProjects()) {
			project.delete(true, true, null);
		}
	}
}
