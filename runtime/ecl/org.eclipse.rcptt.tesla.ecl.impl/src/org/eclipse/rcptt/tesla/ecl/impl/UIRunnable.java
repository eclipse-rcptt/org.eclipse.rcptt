/*******************************************************************************
 * Copyright (c) 2009, 2020 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.ecl.impl;

import static org.eclipse.rcptt.tesla.ecl.internal.impl.TeslaImplPlugin.PLUGIN_ID;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.sherlock.core.reporting.ReportBuilder;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.internal.core.queue.TeslaQClient;
import org.eclipse.rcptt.tesla.internal.ui.player.ReportScreenshotProvider;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.UIJobCollector;
import org.eclipse.rcptt.tesla.swt.events.ITeslaEventListener;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager.HasEventKind;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public abstract class UIRunnable<T> {
	private static final boolean DEBUG_PROCEED = "true"
			.equals(Platform.getDebugOption("org.eclipse.rcptt.tesla.ecl.impl/debug/proceed"));
	private enum RunningState {
		Starting, Execution, Done, Finished
	}

	public static <T> T exec(final UIRunnable<T> runnable) throws CoreException {
		return exec(runnable, getTimeout(), () -> false);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T exec(final UIRunnable<T> runnable, int timeout_ms, BooleanSupplier isCancelled) throws CoreException {
		final AtomicReference<RunningState> processed = new AtomicReference<RunningState>(RunningState.Starting);
		CompletableFuture<T> result = new CompletableFuture<T>();
		final UIJobCollector collector = new UIJobCollector();
		long start = System.currentTimeMillis();
		long stop = start + timeout_ms;
		long halfWay = start + (timeout_ms / 2);
		final Display display = PlatformUI.getWorkbench().getDisplay();
		Job.getJobManager().addJobChangeListener(collector);
		collector.enable();
		final ITeslaEventListener listener = new ITeslaEventListener() {
			public synchronized boolean doProcessing(
					org.eclipse.rcptt.tesla.core.context.ContextManagement.Context currentContext) {
				boolean tick = processed.get().equals(RunningState.Starting) || processed.get().equals(RunningState.Execution);
				Q7WaitInfoRoot info = TeslaBridge.getCurrentWaitInfo(tick);
				
				boolean resultValue = true;
				
				if (!PlatformUI.getWorkbench().getDisplay()
						.equals(Display.getCurrent())) {
					Q7WaitUtils.updateInfo("display", "instance", info);
					debugProceed("Wrong display");
					resultValue = false;
				}
				// Return false if we have SWT observable in timers
				if (SWTUIPlayer.hasTimers(display, info)) {
					Q7WaitUtils.updateInfo("display", "timers", info);
					debugProceed("Has timers");
					resultValue = false;
				}
				// Check for asyncs in synchronizer
				if (SWTUIPlayer.hasRunnables(display)) {
					Q7WaitUtils.updateInfo("display", "runnables", info);
					debugProceed("Has runnables");
					resultValue = false;
				}
				if (!collector.isEmpty(currentContext, info)) {
					debugProceed("Has jobs");
					resultValue = false;
				}
				if( !resultValue ) {
					return false;
				}
				if (processed.compareAndSet(RunningState.Starting, RunningState.Execution)) {
					debugProceed("Starting");
					try {
						result.complete(runnable.run());
					} catch (Throwable e) {
						result.completeExceptionally(e);
						// Do not collect anything on error
						collector.setNeedDisable();
						// collector.clean();
						processed.set(RunningState.Finished);
						return true;
					} finally {
						debugProceed("Done");
					}
					processed.set(RunningState.Done);
					return true;
				} else {
					debugProceed("Already executing");
				}
				if (processed.get().equals(RunningState.Done)) {
					collector.setNeedDisable();
					processed.set(RunningState.Finished);
					return true;
				}
				return false;
			}

			public void hasEvent(HasEventKind kind, String run) {
			}
		};
		final IStatus[] dialogCloseStatus = new IStatus[1];  
		try {
			TeslaEventManager.getManager().addEventListener(listener);
			while (!processed.get().equals(RunningState.Finished)) {
				if (display.isDisposed()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				
				if (isCancelled.getAsBoolean()) {
					throw new CoreException(Status.CANCEL_STATUS); 
				}

				// Perform wakeup async
				SWTUIPlayer.notifyUI(display);
				try {
					result.get(100, TimeUnit.MILLISECONDS);
					break;
				} catch (TimeoutException e) {
					// Continue to check for timeouts
				}
				long time = System.currentTimeMillis();
				if (time > halfWay) {
					if (processed.get().equals(RunningState.Starting)) {
						// try to close all modal dialogs and clean job
						// processor
						display.asyncExec(new Runnable() {
							public void run() {
								dialogCloseStatus[0] = Utils.closeDialogs();
							}
						});
						collector.clean();
					}
				}
				if (time > stop) {
					// Lets also capture all thread dump.
					storeTimeoutInReport(display, collector);
					MultiStatus status = new MultiStatus(PLUGIN_ID, IProcess.TIMEOUT_CODE, "Timeout during execution of " + runnable, new RuntimeException()) {
						{
							setSeverity(ERROR);
						}
					};
					if (dialogCloseStatus[0] != null)
						status.add(dialogCloseStatus[0]);
					throw new CoreException(status);
				}
			}
			if (!result.isCompletedExceptionally()) {
				for (;;) {
					Q7WaitInfoRoot info = TeslaBridge.getCurrentWaitInfo(true);
					if (collector.isEmpty(org.eclipse.rcptt.tesla.core.context.ContextManagement.currentContext(), info)) {
						break;
					}
					if (isCancelled.getAsBoolean()) {
						throw new CoreException(Status.CANCEL_STATUS); 
					}
					
					if (System.currentTimeMillis() > stop) {
						storeTimeoutInReport(display, collector);
						throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, IProcess.TIMEOUT_CODE, "Background jobs are running for too long", new RuntimeException()));
					}
					Thread.sleep(1);
				}
			}
			return (T) result.get(1, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (ExecutionException e) {
			throw new CoreException(createError(e.getCause()));
		} catch (TimeoutException e) {
			throw new CoreException(createError(e));
		} finally {
			processed.set(RunningState.Done);
			Job.getJobManager().removeJobChangeListener(collector);
			TeslaEventManager.getManager().removeEventListener(listener);
		}
	}

	private static IStatus createError(final Throwable exception) {
		return new Status(Status.ERROR, PLUGIN_ID, exception.getMessage(), exception);
	}

	private static int getTimeout() {
		return TeslaLimits.getContextRunnableTimeout();
	}

	private static void storeTimeoutInReport(final Display display,
			UIJobCollector collector) throws InterruptedException {
		final ReportBuilder currentBuilder = ReportManager.getBuilder();
		final boolean infoCollected[] = { false };
		display.asyncExec(new Runnable() {
			public void run() {
				TeslaQClient client = TeslaBridge.getClient();
				if (client != null) {
					client.collectLastFailureInformation();
				}
				ReportScreenshotProvider.takeScreenshot(display, true,
						"timeout");
			}
		});
	}

	public abstract T run() throws CoreException;

	private static void debugProceed(String message) {
		if (DEBUG_PROCEED) {
			System.out.println("UIRunnable: " + message);
			System.out.flush();
		}
	}
}
