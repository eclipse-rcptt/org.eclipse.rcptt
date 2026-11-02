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
package org.eclipse.rcptt.core.tests;

import java.io.IOException;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/**
 * Prints diagnostics for hung tests: a full thread dump (including
 * {@link java.util.concurrent.locks} owners), Eclipse jobs with their
 * scheduling rules, rules held by non-job threads and RCPTT indexer queue.
 * <p>
 * {@link ThreadMXBean#findDeadlockedThreads()} is not enough: RCPTT
 * {@code JobManager.performConcurrentJob()} polls with {@code Thread.sleep()}
 * and {@code ValueLock} uses {@code Object.wait()}, so the JVM does not see
 * these hangs as deadlocks.
 * <p>
 * The dump is produced when a test runs longer than the given threshold or
 * fails with {@link TestTimedOutException}. It is printed to
 * {@code System.err} and, if {@code -Drcptt.hangDumpDir} is set, written into
 * that directory.
 */
public final class HangWatchdog implements TestRule {
	public static final String DUMP_DIR_PROPERTY = "rcptt.hangDumpDir";
	private static final long DUMP_TIMEOUT_MS = 10_000;
	private final long dumpAfterMillis;

	/**
	 * @param dumpAfterMillis dump state if a test is still running after this
	 *                        time
	 */
	public HangWatchdog(long dumpAfterMillis) {
		this.dumpAfterMillis = dumpAfterMillis;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				AtomicBoolean dumped = new AtomicBoolean();
				Thread watchdog = new Thread(() -> {
					try {
						Thread.sleep(dumpAfterMillis);
					} catch (InterruptedException e) {
						return;
					}
					if (dumped.compareAndSet(false, true)) {
						dump(description, "Still running after " + dumpAfterMillis + " ms");
					}
				}, "HangWatchdog " + description.getDisplayName());
				watchdog.setDaemon(true);
				watchdog.start();
				try {
					base.evaluate();
				} catch (TestTimedOutException e) {
					if (dumped.compareAndSet(false, true)) {
						dump(description, e.getMessage());
					}
					throw e;
				} finally {
					watchdog.interrupt();
				}
			}
		};
	}

	/**
	 * Dumps the state in a separate thread, so that a lock held by a hung
	 * thread can't block the caller indefinitely.
	 */
	public static void dump(Description description, String reason) {
		Thread dumper = new Thread(() -> write(description, report(description, reason)),
				"HangWatchdog dump");
		dumper.setDaemon(true);
		dumper.start();
		try {
			dumper.join(DUMP_TIMEOUT_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static String report(Description description, String reason) {
		StringBuilder result = new StringBuilder();
		result.append("===== HangWatchdog: ").append(description.getDisplayName()).append(": ").append(reason)
				.append(" =====\n");
		section(result, "Deadlocked threads (as detected by JVM)", () -> deadlocks(result));
		section(result, "Threads", () -> threads(result));
		section(result, "Eclipse jobs", () -> jobs(result));
		section(result, "Scheduling rules held by threads (implicit jobs)", () -> implicitRules(result));
		section(result, "RCPTT indexer", () -> result.append(ModelManager.getModelManager().getIndexManager()));
		result.append("===== HangWatchdog end =====\n");
		return result.toString();
	}

	private static void write(Description description, String report) {
		System.err.println(report);
		System.err.flush();
		String directory = System.getProperty(DUMP_DIR_PROPERTY);
		if (directory == null || directory.isBlank()) {
			return;
		}
		String name = (description.getClassName() + "." + description.getMethodName() + "-"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")) + ".txt")
				.replaceAll("[^A-Za-z0-9._-]", "_");
		try {
			Path dir = Path.of(directory);
			Files.createDirectories(dir);
			Files.writeString(dir.resolve(name), report, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private interface Section {
		void run() throws Exception;
	}

	private static void section(StringBuilder output, String title, Section section) {
		output.append("--- ").append(title).append(" ---\n");
		try {
			section.run();
		} catch (Throwable e) {
			output.append("Failed to collect: ").append(e).append('\n');
		}
		output.append('\n');
	}

	private static void deadlocks(StringBuilder output) {
		long[] ids = ManagementFactory.getThreadMXBean().findDeadlockedThreads();
		if (ids == null) {
			output.append("none\n");
			return;
		}
		for (long id : ids) {
			output.append(id).append('\n');
		}
	}

	private static void threads(StringBuilder output) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		for (ThreadInfo info : bean.dumpAllThreads(bean.isObjectMonitorUsageSupported(),
				bean.isSynchronizerUsageSupported())) {
			output.append('"').append(info.getThreadName()).append("\" #").append(info.getThreadId()).append(' ')
					.append(info.getThreadState());
			if (info.getLockName() != null) {
				output.append(" on ").append(info.getLockName());
			}
			if (info.getLockOwnerName() != null) {
				output.append(" owned by \"").append(info.getLockOwnerName()).append("\" #")
						.append(info.getLockOwnerId());
			}
			output.append('\n');
			StackTraceElement[] stack = info.getStackTrace();
			MonitorInfo[] monitors = info.getLockedMonitors();
			for (int depth = 0; depth < stack.length; depth++) {
				output.append("\tat ").append(stack[depth]).append('\n');
				for (MonitorInfo monitor : monitors) {
					if (monitor.getLockedStackDepth() == depth) {
						output.append("\t- locked ").append(monitor).append('\n');
					}
				}
			}
			LockInfo[] synchronizers = info.getLockedSynchronizers();
			if (synchronizers.length > 0) {
				output.append("\tLocked synchronizers:\n");
				for (LockInfo lock : synchronizers) {
					output.append("\t- ").append(lock).append('\n');
				}
			}
			output.append('\n');
		}
	}

	private static void jobs(StringBuilder output) {
		for (Job job : Job.getJobManager().find(null)) {
			output.append(job.getClass().getName()).append(" \"").append(job.getName()).append("\" state=")
					.append(stateName(job.getState())).append(" rule=").append(job.getRule());
			Thread thread = job.getThread();
			if (thread != null) {
				output.append(" thread=\"").append(thread.getName()).append('"');
			}
			output.append('\n');
		}
	}

	/**
	 * Best effort: rules acquired with
	 * {@link IJobManager#beginRule(org.eclipse.core.runtime.jobs.ISchedulingRule, org.eclipse.core.runtime.IProgressMonitor)}
	 * (for example by {@code IWorkspace.run()}) are tracked in internal
	 * {@code ImplicitJobs} of Eclipse job manager.
	 */
	private static void implicitRules(StringBuilder output) throws ReflectiveOperationException {
		IJobManager manager = Job.getJobManager();
		Object implicitJobs = fieldOfType(manager, "ImplicitJobs");
		if (implicitJobs == null) {
			output.append("ImplicitJobs is not found in ").append(manager.getClass().getName()).append('\n');
			return;
		}
		Map<?, ?> threadJobs = (Map<?, ?>) fieldOfType(implicitJobs, Map.class.getName());
		if (threadJobs == null) {
			output.append("Thread map is not found in ").append(implicitJobs.getClass().getName()).append('\n');
			return;
		}
		Object[] entries;
		synchronized (implicitJobs) {
			entries = threadJobs.entrySet().toArray();
		}
		for (Object entry : entries) {
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) entry;
			Object threadJob = e.getValue();
			Object rule = threadJob instanceof Job ? ((Job) threadJob).getRule() : threadJob;
			output.append('"').append(((Thread) e.getKey()).getName()).append("\" holds ").append(rule).append('\n');
		}
	}

	private static Object fieldOfType(Object object, String typeNameSuffix) throws IllegalAccessException {
		for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getType().getName().endsWith(typeNameSuffix)) {
					field.setAccessible(true);
					return field.get(object);
				}
			}
		}
		return null;
	}

	private static String stateName(int state) {
		switch (state) {
		case Job.RUNNING:
			return "RUNNING";
		case Job.WAITING:
			return "WAITING";
		case Job.SLEEPING:
			return "SLEEPING";
		case Job.NONE:
			return "NONE";
		default:
			return Integer.toString(state);
		}
	}
}
