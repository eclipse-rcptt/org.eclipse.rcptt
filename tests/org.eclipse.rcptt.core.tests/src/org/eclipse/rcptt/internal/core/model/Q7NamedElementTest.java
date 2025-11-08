/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.tests.NoErrorsInLog;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class Q7NamedElementTest {
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	private static final IProject PROJECT = WORKSPACE.getRoot().getProject("TEST");
	private static final IFile TESTCASE_FILE = PROJECT.getFile("testcase.test");
	
	@Rule
	public final NoErrorsInLog NO_ERRORS = new NoErrorsInLog(RcpttCore.class);
	
	@Before
	@After
	public void cleanup() throws CoreException {
		for (IProject i: WORKSPACE.getRoot().getProjects()) {
			i.delete(true,  true, null);
		}
	}
	
	@Before
	public void before() throws CoreException {
		IProjectDescription deQ7ion = WORKSPACE.newProjectDescription(PROJECT.getName());
		deQ7ion.setNatureIds(new String[] { RcpttNature.NATURE_ID });
		PROJECT.create(deQ7ion, null);
		PROJECT.open(null);
	}

	@Test(timeout=10_000)
	public void reopenIfClosed() throws CoreException, InterruptedException, ExecutionException, IOException, BrokenBarrierException {
		long stop = currentTimeMillis() + 9000;
		try (InputStream is = getClass().getResourceAsStream("testcase.test")) {
			TESTCASE_FILE.create(is, IFile.REPLACE|IFile.FORCE, null);
		}
		ITestCase testcase = (ITestCase) RcpttCore.create(TESTCASE_FILE);
		ITestCase testcase2 = (ITestCase) RcpttCore.create(TESTCASE_FILE);
		CyclicBarrier barrier = new CyclicBarrier(2);
		CompletableFuture<Void> closerTask = CompletableFuture.runAsync(() -> {
			try {
				barrier.await();
				while(!Thread.interrupted()) {
					Thread.yield();
					testcase2.close();
				}
			} catch (ModelException | InterruptedException | BrokenBarrierException e) {
				throw new RuntimeException(e);
			}
		});
		try {
			barrier.await();
			for (int i = 0; currentTimeMillis() < stop; i++) {
				try {
					assertFalse(closerTask.isDone());
					assertNotNull(testcase.getNamedElement());
					assertNotNull(testcase.getID());
					NO_ERRORS.assertNoErrors();
				} catch (Throwable e) {
					throw new AssertionError("Failed on iteration " + i, e);
				}
			}
		} finally {
			closerTask.cancel(true);
		}
		if (!closerTask.isCancelled()) {
			closerTask.get();
		}
	}
	
	/**
	 * @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176#issuecomment-2904265630
	 */
	@Test(timeout=100_000)
	public void doNotDeadlockIfResourceIsNotSynchronized() throws CoreException, InterruptedException, ExecutionException, IOException, BrokenBarrierException {
		try (InputStream is = getClass().getResourceAsStream("testcase.test")) {
			TESTCASE_FILE.create(is, IFile.REPLACE|IFile.FORCE, null);
		}
		ITestCase testcase = (ITestCase) RcpttCore.create(TESTCASE_FILE);
		FileTime time =FileTime.from(Instant.now().plusSeconds(3));
		Files.setLastModifiedTime(Path.of(TESTCASE_FILE.getRawLocation().toOSString()), time);
		assertNotNull(testcase.getNamedElement()); // should not deadlock or throw exceptions
	}
	
	@Test
	public void noResourceleaksID() {
		noResourceleaks(testcase -> Assert.assertEquals("_-dqP0BOHEeOQfY3L4mNcSA", testcase.getID()));
	}
	
	@Test
	public void noResourceleaksExists() {
		noResourceleaks(testcase -> assertTrue(testcase.exists()));
	}
	
	@Ignore("https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176")
	@Test
	public void existsIsNoiseResistant() throws CoreException, IOException {
		ITestCase testcase = (ITestCase) RcpttCore.create(TESTCASE_FILE);
		Job noise = Job.create("Keep refetching model", (ICoreRunnable) monitor -> {
			while (!monitor.isCanceled()) {
				Thread.yield();
				testcase.getNamedElement();
			}
		});
		long stop = currentTimeMillis() + 10000;
		try {
			int i = 0;
			while (currentTimeMillis() < stop) {
				Thread.yield();
				TESTCASE_FILE.delete(true, null);
				String message = "Iteration " + i++;
				assertFalse(message, TESTCASE_FILE.exists());
				assertFalse(message, testcase.exists());
				try (InputStream is = getClass().getResourceAsStream("testcase.test")) {
					TESTCASE_FILE.create(is, IFile.REPLACE|IFile.FORCE, null);
				}
				assertTrue(message, TESTCASE_FILE.exists());
				assertTrue(message, testcase.exists());
				IStatus result = noise.getResult();
				if (result != null) {
					throw new CoreException(result);
				}
			}
		} finally {
			noise.cancel();
		}
	}

	
	private interface ThrowingConsumer<T> {
		void accept(T data) throws Exception;
	}
	
	public void noResourceleaks(ThrowingConsumer<ITestCase> action) {
		try (InputStream is = getClass().getResourceAsStream("testcase.test")) {
			TESTCASE_FILE.create(is, IFile.REPLACE | IFile.FORCE, null);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
		IFile previousFile = TESTCASE_FILE;
		for (int i = 0; i < 3_000; i++) {
			try {
				IFile currentFile = PROJECT.getFile("t" + i + ".test");
				previousFile.move(currentFile.getFullPath(), true, false, null);
				previousFile = currentFile;
				ITestCase testcase = (ITestCase) RcpttCore.create(currentFile);
				action.accept(testcase);
				action.accept(testcase); // sometimes single access does not cause leak
			} catch (Exception e) {
				throw new AssertionError("failed on iteration " + i, e);
			}
		}
		System.gc(); // Detect leaks with org.eclipse.rcptt.core.persistence.LeakDetector
		NO_ERRORS.assertNoErrors();
	}

}
