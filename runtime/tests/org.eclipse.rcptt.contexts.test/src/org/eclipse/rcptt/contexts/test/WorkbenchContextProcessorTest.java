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

import static org.eclipse.rcptt.contexts.test.DebugContextProcessorTest.waitFor;
import static org.eclipse.rcptt.core.scenario.ScenarioFactory.eINSTANCE;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.scenario.WorkbenchContext;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.junit.After;
import org.junit.Test;

public class WorkbenchContextProcessorTest {

	private final ContextTypeManager contexts = ContextTypeManager.getInstance();
	private ISession session = EclRuntime.createSession();
	
	@After
	public void after() throws CoreException {
		session.close();
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
}
