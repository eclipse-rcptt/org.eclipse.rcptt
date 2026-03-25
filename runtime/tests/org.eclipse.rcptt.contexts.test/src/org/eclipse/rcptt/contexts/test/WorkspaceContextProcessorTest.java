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
package org.eclipse.rcptt.contexts.test;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.workspace.WSRoot;
import org.eclipse.rcptt.workspace.WorkspaceContext;
import org.eclipse.rcptt.workspace.WorkspaceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Closer;

public class WorkspaceContextProcessorTest {
	private final ContextTypeManager contexts = ContextTypeManager.getInstance();
	private ISession session = EclRuntime.createSession();
	private final Closer closer = Closer.create();
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	
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
	public void deleteReadOnlyFiles() throws CoreException, IOException {
		final WorkspaceContext context = org.eclipse.rcptt.workspace.WorkspaceFactory.eINSTANCE.createWorkspaceContext();
		final WSRoot eroot = WorkspaceFactory.eINSTANCE.createWSRoot();
		context.setContent(eroot);
		Path root = Path.of(WORKSPACE.getRoot().getLocation().toOSString());
		Path readonlyFile = root.resolve("readonly.txt");
		Files.createFile(readonlyFile);
		DosFileAttributeView fileStore = Files.getFileAttributeView(readonlyFile, DosFileAttributeView.class);
		boolean found = false;
		if (fileStore != null) {
			fileStore.setReadOnly(true);
			found = true;
		}
		PosixFileAttributeView fileStore2 = Files.getFileAttributeView(readonlyFile, PosixFileAttributeView.class);
		if (fileStore2 != null) {
			var p = fileStore2.readAttributes().permissions();
			p.removeAll(EnumSet.of(PosixFilePermission.GROUP_WRITE, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OTHERS_WRITE));
			found = true;
		}
		assertTrue(found);
		CompletableFuture<Void> future = runAsync(() -> {
			try {
				contexts.apply(context, session);
			} catch (CoreException e) {
				throw new AssertionError(e);
			}
		});
		
		DebugContextProcessorTest.waitFor(future, 10000);
				
		assertFalse(Files.exists(readonlyFile));
	}

}
