/*******************************************************************************
 * Copyright (c) 2026 Luise Ravnskjaer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Eclipse contributors - initial API and implementation
 *******************************************************************************/
package org.eclipse.rcptt.verifications.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.core.ecl.core.model.ExecVerification;
import org.eclipse.rcptt.core.ecl.core.model.ExecutionPhase;
import org.eclipse.rcptt.core.ecl.core.model.Q7CoreFactory;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.verifications.runtime.VerificationStatus;
import org.eclipse.rcptt.verifications.status.VerificationStatusData;
import org.eclipse.rcptt.verifications.tree.Column;
import org.eclipse.rcptt.verifications.tree.Tree;
import org.eclipse.rcptt.verifications.tree.TreeFactory;
import org.eclipse.rcptt.verifications.tree.TreeVerification;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Closer;

/**
 * Runs tree verifications through the {@code ExecVerification} ECL command,
 * as the RCPTT launcher does to execute a verification in the application
 * under test, but in an in-process ECL session instead of a remote one.
 */
public class TreeVerificationProcessorTest {

	private static final String SHELL_TITLE = "TreeVerificationProcessorTest";
	private static final long TIMEOUT_MS = 10_000;

	private final ISession session = EclRuntime.createSession();
	private final Closer closer = Closer.create();
	private Shell shell;

	@Before
	public void before() {
		shell = new Shell(Display.getCurrent());
		closer.register(shell::dispose);
		shell.setText(SHELL_TITLE);
		shell.setLayout(new FillLayout());
	}

	@After
	public void after() throws CoreException, IOException {
		try {
			session.close();
		} finally {
			closer.close();
		}
	}

	@Test
	public void matchingColumnsPass() throws Exception {
		showTreeWithColumns(shell, "A", "B");
		TreeVerification verification = verificationExpectingColumns("A", "B");

		IStatus status = verify(verification);

		assertTrue(status.toString(), status.isOK());
	}

	/**
	 * https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/205
	 */
	@Test
	public void expectedColumnBeyondActualColumnCountIsMatchedByName() throws Exception {
		showTreeWithColumns(shell, "B");
		TreeVerification verification = verificationExpectingColumns("A", "B");

		IStatus status = verify(verification);

		List<VerificationStatusData> errors = verificationErrors(status);
		assertEquals(1, errors.size());
		assertTrue(errors.get(0).getMessage(), errors.get(0).getMessage().contains("\"A\""));
	}

	private static void showTreeWithColumns(Shell parent, String... columnNames) {
		var tree = new org.eclipse.swt.widgets.Tree(parent, SWT.NONE);
		for (String name : columnNames) {
			new TreeColumn(tree, SWT.NONE).setText(name);
		}
		parent.open();
	}

	private static TreeVerification verificationExpectingColumns(String... names) {
		Tree expected = TreeFactory.eINSTANCE.createTree();
		for (String name : names) {
			Column column = TreeFactory.eINSTANCE.createColumn();
			column.setName(name);
			expected.getColumns().add(column);
		}
		TreeVerification verification = TreeFactory.eINSTANCE.createTreeVerification();
		verification.setName(SHELL_TITLE);
		verification.setSelector("get-window \"" + SHELL_TITLE + "\" | get-tree");
		verification.setTree(expected);
		return verification;
	}

	/**
	 * Executes the verification's finish phase (the only phase of tree
	 * verifications) and spins the event loop until it is done.
	 * <p>
	 * The display is only woken with {@link Display#wake()}: Tesla counts
	 * {@code timerExec}/{@code asyncExec} runnables from non-SWT code as UI
	 * activity and postpones its commands while such activity continues, so a
	 * timer-based wake-up would block the verification.
	 */
	private IStatus verify(TreeVerification verification) throws Exception {
		ExecVerification command = Q7CoreFactory.eINSTANCE.createExecVerification();
		command.setVerification(verification);
		command.setPhase(ExecutionPhase.FINISH);

		IProcess process = session.execute(command);
		CompletableFuture<IStatus> result = CompletableFuture.supplyAsync(() -> {
			try {
				return process.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e);
			}
		});
		Display display = Display.getCurrent();
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (!result.isDone()) {
			if (System.currentTimeMillis() > deadline) {
				fail("Verification did not finish within " + TIMEOUT_MS + " ms");
			}
			if (!display.readAndDispatch()) {
				CompletableFuture.runAsync(display::wake);
				display.sleep();
			}
		}
		return result.get();
	}

	private static List<VerificationStatusData> verificationErrors(IStatus status) {
		if (!(status instanceof VerificationStatus)) {
			throw new AssertionError("Expected a failed verification, but got: " + status, status.getException());
		}
		return ((VerificationStatus) status).getData();
	}
}