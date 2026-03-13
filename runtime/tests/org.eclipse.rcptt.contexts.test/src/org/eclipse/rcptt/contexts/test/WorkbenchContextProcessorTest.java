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
import static org.eclipse.rcptt.core.scenario.ScenarioFactory.eINSTANCE;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.scenario.WorkbenchContext;
import org.eclipse.rcptt.ecl.core.util.Statuses;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Closer;

public class WorkbenchContextProcessorTest {

	private final ContextTypeManager contexts = ContextTypeManager.getInstance();
	private ISession session = EclRuntime.createSession();
	private final Closer closer = Closer.create();
	
	@After
	public void after() throws CoreException, IOException {
		session.close();
		closer.close();
	}
	
	@Before
	public void before() {
		setRunnableTimeout(100_000);
	}

	private void setRunnableTimeout(int milliseconds) {
		TeslaFeatures.getInstance().getOption("context.runnable.timeout").setValue("" + milliseconds);
	}
	
	
	@Test
	public void doNotFailOnCancel() throws CoreException, InterruptedException, ExecutionException {
		WorkbenchContext context = eINSTANCE.createWorkbenchContext();
		context.setNoModalDialogs(true);
		for (int i = 0; i < 100; i++) {
			CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
				try {
					contexts.apply(context, session);
				} catch (CoreException e) {
					IStatus s = e.getStatus();
					if (s.matches(IStatus.ERROR)) {
						throw new AssertionError(e);
					}
				}
			});
			int copy = i;
			CompletableFuture<Void> cancelFuture = CompletableFuture.runAsync(() -> {
				try {
					Thread.sleep(copy);
					session.close();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				} catch (CoreException e) {
					throw new AssertionError(e);
				}
			});
			waitFor(CompletableFuture.allOf(result, cancelFuture), 10_000);
			session.close();
			session = EclRuntime.createSession();
		}
	}
	
	/** If a dialog can't be closed immediately do not fail until timeout 
	 * 
	 * @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/97
	 * */
	@Test
	public void giveUncloseableDialogsChance() {
		Shell parent = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
		WorkbenchContext context = eINSTANCE.createWorkbenchContext();
		context.setNoModalDialogs(true);
		UncloseableDialog dialog = new UncloseableDialog(parent, "This dialog will onlly close in a second");
		// Display did not implements java.util.concurrent.Executor in Eclipse Platform 2021-03
		CompletableFuture<Void> result =  CompletableFuture.runAsync(() -> {}, parent.getDisplay()::asyncExec).thenRunAsync(() -> {
			try {
				long start = currentTimeMillis();
				contexts.apply(context, session);
				long duration = currentTimeMillis() - start;
				String message = "Unexpected duration: " + duration;
				assertTrue(message, duration > 900);
				assertTrue(message, duration < 2000);
			} catch (CoreException e) {
				throw new AssertionError(e);
			}
		}, ForkJoinPool.commonPool());
		parent.getDisplay().timerExec(1000, dialog::forceClose);
		dialog.open();
		waitFor(result, 20000);
	}
	
	/** If a dialog can't be closed, report the fact, instead of general timeout message 
	 * 
	 * @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/97
	 * */
	@Test
	public void reportProblematiicDialogs() {
		setRunnableTimeout(1000);
		Shell parent = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
		WorkbenchContext context = eINSTANCE.createWorkbenchContext();
		context.setNoModalDialogs(true);
		UncloseableDialog dialog = new UncloseableDialog(parent, "This dialog will only close after test end");
		parent.getDisplay().timerExec(200_000, dialog::forceClose);
		CompletableFuture<Void> result =  CompletableFuture.runAsync(() -> {}, parent.getDisplay()::asyncExec).thenRunAsync(() -> {
			try {
				long start = currentTimeMillis();
				CoreException e = Assert.assertThrows(CoreException.class, () -> contexts.apply(context, session));
				IStatus s = e.getStatus();
				String messages = getAllMessages(s);
				assertTrue(messages, s.matches(IStatus.ERROR));
				assertTrue(messages, Statuses.hasCode(s, IProcess.TIMEOUT_CODE));
				assertTrue(messages, messages.contains("This dialog will only close after test end"));
				long duration = currentTimeMillis() - start;
				String message = "Unexpected duration: " + duration;
				assertTrue(message, duration > 900);
				assertTrue(message, duration < 2000);
			} finally {
				parent.getDisplay().asyncExec(() -> dialog.forceClose());
			}
		}, ForkJoinPool.commonPool());
		dialog.open();
		waitFor(result, 100);
	}
	
	private static String getAllMessages(IStatus status) {
		StringBuilder result = new StringBuilder();
		Statuses.visit(status, s -> {
			result.append(s.getMessage() + "\n");
			return true;
		});
		return result.toString();
	}
	
	private final class UncloseableDialog extends MessageDialog {

		@SuppressWarnings("resource")
		public UncloseableDialog(Shell parent, String title) {
			super(parent, title, null, "This dialog can not be closed manually", MessageDialog.INFORMATION, 0, IDialogConstants.OK_LABEL);
			closer.register(() -> forceClose());
		}
		
		@Override
		public boolean close() {
			return false;
		}
		
		public void forceClose() {
			super.close();
		}
	}
}
