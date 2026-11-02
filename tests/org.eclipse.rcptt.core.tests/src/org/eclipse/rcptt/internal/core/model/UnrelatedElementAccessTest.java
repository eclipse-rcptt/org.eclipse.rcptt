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

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.tests.HangWatchdog;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Access to a model element should not wait for population of unrelated
 * elements, so that UI does not freeze while background work is done on other
 * parts of the model. See
 * https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176
 * <p>
 * Requires {@code byteman/block-loading.btm} (Maven profile
 * {@code issue176-byteman}) that delays loading of elements whose file name
 * starts with {@value #BLOCKED_PREFIX}. Skipped otherwise.
 */
public class UnrelatedElementAccessTest {
	public static final String BLOCKED_PREFIX = "blocked";
	/** Maximum time to access an unrelated element. */
	private static final long THRESHOLD_MS = Long.getLong("rcptt.freeze.thresholdMs", 1000);
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	private static final IProject PROJECT = WORKSPACE.getRoot().getProject("FREEZE");

	@Rule
	public final HangWatchdog WATCHDOG = new HangWatchdog(60_000);

	@Before
	@After
	public void cleanup() throws CoreException {
		for (IProject project : WORKSPACE.getRoot().getProjects()) {
			project.delete(true, true, null);
		}
	}

	@Test(timeout = 120_000)
	public void unrelatedElementIsNotBlockedByLoading() throws Exception {
		Assume.assumeTrue("Byteman agent is not on boot class path. Run with Maven profile issue176-byteman", isBytemanActive());
		Q7NamedElementTest.enableSyncOnAccess(false);
		PROJECT.create(null);
		PROJECT.open(null);
		RcpttNature.updateProjectNature(PROJECT, true);
		IQ7Folder root = RcpttCore.create(PROJECT).getRootFolder();
		ITestCase blocked = root.createTestCase(BLOCKED_PREFIX, true, null);
		ITestCase free = root.createTestCase("free", true, null);
		close(blocked);
		close(free);

		CompletableFuture<Void> blockedLoad = CompletableFuture.runAsync(() -> {
			try {
				blocked.getNamedElement();
			} catch (ModelException e) {
				throw new RuntimeException(e);
			}
		});
		try {
			Thread.sleep(300);
			Assume.assumeFalse("Loading of " + BLOCKED_PREFIX
					+ "* was not delayed. Run with Maven profile issue176-byteman (byteman/block-loading.btm)",
					blockedLoad.isDone());
			long start = currentTimeMillis();
			Assert.assertNotNull(free.getNamedElement());
			long elapsed = currentTimeMillis() - start;
			boolean stillBlocked = !blockedLoad.isDone();
			Assert.assertTrue("Access to unrelated element took " + elapsed + " ms (threshold " + THRESHOLD_MS
					+ " ms), blocked element is " + (stillBlocked ? "still loading" : "loaded"),
					elapsed < THRESHOLD_MS && stillBlocked);
		} finally {
			blockedLoad.get();
		}
	}

	private static boolean isBytemanActive() {
		try {
			Class.forName("org.jboss.byteman.rule.Rule", false, null);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static void close(ITestCase element) throws ModelException, InterruptedException {
		((Q7Element) element).close();
	}
}
