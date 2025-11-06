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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.tesla.ecl.impl.UIRunnable;
import org.eclipse.swt.widgets.Display;
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
 
	private static void assertThrows(Class<?> clazz, ThrowingRunnable runnable) {
		try {
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
	
	private Closeable stopUIThread() {
		CountDownLatch release = new CountDownLatch(1);
		Display.getDefault().asyncExec(() -> {
			try {
				release.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e);
			}
		});
		return release::countDown;
	}

}
