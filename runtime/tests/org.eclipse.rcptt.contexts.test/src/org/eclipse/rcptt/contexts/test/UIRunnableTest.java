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
package org.eclipse.rcptt.contexts.test;

import static java.lang.System.currentTimeMillis;
import static org.eclipse.rcptt.contexts.test.DebugContextProcessorTest.waitFor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rcptt.tesla.core.context.ContextManagement;
import org.eclipse.rcptt.tesla.ecl.impl.UIRunnable;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Test;

public class UIRunnableTest {

	private final Job loop = new Job("loop") {
		{
			setPriority(Job.INTERACTIVE);
		}
		@Override
		protected org.eclipse.core.runtime.IStatus run(IProgressMonitor monitor) {
			while (!monitor.isCanceled()) {
				Thread.yield();
			}
			return Status.OK_STATUS;
		};
	};
	
	
	
	@After
	public void after() {
		loop.cancel();
	}
	
	@Test
	public void propagateException() throws CoreException, InterruptedException {
		UIRunnable<Void> subject = new UIRunnable<>() {
			@Override
			public Void run() throws CoreException {
				throw new IllegalStateException();
			}
		};
		CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
			assertThrows(IllegalStateException.class, () -> {
						try {
							UIRunnable.exec(subject);
						} catch (CoreException e) {
							throw new AssertionError(e);
						}
			});
		});
		waitFor(result, 10_000);
	}
	
	public interface ThrowingRunnable {
	    void run() throws Exception;
	}
	
	@Test
	public void closeNewDialogs() {
		UIRunnable<Void> subject = new UIRunnable<>() {
			@Override
			public Void run() throws CoreException {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), "Information", "Client code is blocked on this dialog. The dialog has to be closed for the task to complete.");
				return null;
			}
		};
		Thread thread = Thread.currentThread();
		CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
			try {
				UIRunnable.exec(subject, 1_000, thread::isInterrupted);
			} catch (CoreException e) {
				throw new AssertionError(e);
			}
		});
		waitFor(result, 2_000);
	}
 
	@Test
	public void noopIsQuick() {
		UIRunnable<Void> subject = new UIRunnable<>() {
			@Override
			public Void run() throws CoreException {
				return null;
			}
		};
		Thread thread = Thread.currentThread();
		CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
			try {
				UIRunnable.exec(subject, 1_000, thread::isInterrupted);
			} catch (CoreException e) {
				throw new AssertionError(e);
			}
		});
		waitFor(result, 2_000);
	}
	
	private static void assertThrows(Class<?> clazz, ThrowingRunnable runnable) {		try {
			runnable.run();
			fail("Expected to throw " + clazz.getName());
		} catch (Exception e) {
			if (!clazz.isInstance(e)) {
				throw new AssertionError(e);
			}
		}
	}

	@Test
	public void immediateCancel() throws CoreException, InterruptedException {
		AtomicBoolean cancelled = new AtomicBoolean(false);
		AtomicBoolean executed = new AtomicBoolean(false);
		UIRunnable<Void> subject = new UIRunnable<>() {
			@Override
			public Void run() throws CoreException {
				executed.set(true);
				return null;
			}
		};
		long start = currentTimeMillis();
		CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
			Display.getDefault().asyncExec(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException  e) {
					throw new IllegalStateException(e);
				}
				cancelled.set(true);
			});
			try {
				UIRunnable.exec(subject, 10000, cancelled::get);
			} catch (CoreException e) {
				assertTrue(e.getStatus().matches(IStatus.CANCEL));
			}
		});
		waitFor(result, 10_000);
		long stop = currentTimeMillis();
		assertFalse(executed.get());
		long spent = stop - start;
		assertTrue("The cancellation should be quick", spent < 1000);
	}

	/**
	 * Reproduces the "Invalid thread access" problem reported in
	 * <a href="https://github.com/eclipse-rcptt/org.eclipse.rcptt/pull/334">PR #334</a>.
	 * <p>
	 * The Tesla event listener installed by {@link UIRunnable#exec} inspects the
	 * workbench display (via {@code SWTUIPlayer.hasRunnables}). A Tesla processing
	 * pass may be fired from a thread that is <em>not</em> the workbench display's
	 * UI thread (e.g. an additional UI thread or a background thread where
	 * {@code Display.getCurrent() == null}). Accessing the workbench display from
	 * such a foreign thread calls {@code Display.checkDevice()} and raises
	 * {@code SWTException: Invalid thread access}.
	 * <p>
	 * The test drives that foreign-thread pass explicitly and fails loudly if its
	 * preconditions are not met, so it cannot silently pass without exercising the
	 * vulnerable code path.
	 */
	@Test
	public void invalidThreadAccessFromForeignThread() throws Exception {
		final Display workbench = PlatformUI.getWorkbench().getDisplay();
		assertSame("Test must run on the workbench UI thread", workbench, Display.getCurrent());

		AtomicBoolean executed = new AtomicBoolean(false);
		UIRunnable<Void> subject = new UIRunnable<>() {
			@Override
			public Void run() throws CoreException {
				executed.set(true);
				return null;
			}
		};

		AtomicBoolean stop = new AtomicBoolean(false);
		// Start exec off the UI thread; this registers the Tesla event listener.
		CompletableFuture<Void> exec = CompletableFuture.runAsync(() -> {
			try {
				UIRunnable.exec(subject, 30_000, stop::get);
			} catch (CoreException e) {
				throw new CompletionException(e);
			}
		});

		try {
			// Wait until exec has registered its listener. The main (UI) thread must NOT
			// pump the workbench here, so that the only Tesla processing pass is the
			// foreign-thread one fired below. Fail loudly if the listener never appears.
			long registerDeadline = currentTimeMillis() + 5_000;
			while (!TeslaEventManager.getManager().hasListeners()) {
				if (exec.isCompletedExceptionally()) {
					exec.join(); // rethrow the unexpected failure
				}
				if (currentTimeMillis() > registerDeadline) {
					fail("UIRunnable.exec did not register a Tesla event listener");
				}
				Thread.sleep(5);
			}

			// Fire a Tesla processing pass from a foreign (non-UI) thread, where
			// Display.getCurrent() == null. This is the situation described in PR #334:
			// the listener inspects the workbench display from a thread that does not own
			// it. Without the fix, hasRunnables(workbench) calls workbench.getSynchronizer()
			// from this thread and raises "Invalid thread access".
			AtomicReference<Throwable> probeFailure = new AtomicReference<>();
			AtomicBoolean probedOffUiThread = new AtomicBoolean(false);
			Thread probe = new Thread(() -> {
				try {
					probedOffUiThread.set(Display.getCurrent() == null);
					TeslaEventManager.getManager().doProcessing(ContextManagement.currentContext());
				} catch (Throwable e) {
					probeFailure.set(e);
				}
			}, "foreign-tesla-probe");
			probe.start();
			probe.join(5_000);

			// Fail loudly on any unexpected condition in the probe setup itself, so a
			// broken reproduction can never masquerade as a passing test.
			assertFalse("The probe thread must terminate", probe.isAlive());
			assertTrue("The probe must run off the workbench UI thread", probedOffUiThread.get());
			// If the foreign-thread pass failed (e.g. the "Invalid thread access" this
			// test reproduces), rethrow the original throwable so its stack trace is
			// preserved instead of being hidden behind a generic assertion error.
			Throwable probeError = probeFailure.get();
			if (probeError != null) {
				rethrow(probeError);
			}

			// Let the workbench process events so the subject can run on the correct
			// thread when the code is fixed.
			long execDeadline = currentTimeMillis() + 10_000;
			while (!exec.isDone()) {
				if (currentTimeMillis() > execDeadline) {
					fail("UIRunnable did not finish in time");
				}
				if (!workbench.readAndDispatch()) {
					CompletableFuture.runAsync(workbench::wake);
					workbench.sleep();
				}
			}
			// Rethrows the "Invalid thread access" failure captured by exec, if it happened.
			exec.get();
		} finally {
			stop.set(true);
		}
		assertTrue("The subject should have been executed", executed.get());
	}

	private static void rethrow(Throwable t) throws Exception {
		if (t instanceof Error) {
			throw (Error) t;
		}
		throw (Exception) t;
	}
}
