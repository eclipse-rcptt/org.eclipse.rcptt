/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.data.tests;

import static java.lang.String.format;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.ecl.parser.test.TestWithSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetFileChildrenTest extends TestWithSession {
	private static final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	private final IProject project = root.getProject("project");
	private final IFolder folder = project.getFolder("folder");
	private final IFile file = folder.getFile("data.csv");
	private static final String uri = "workspace:/project/folder/data.csv";

	@Before
	public void clearWorkspace() throws CoreException {
		ResourcesPlugin.getWorkspace().delete(root.getProjects(), true, null);
	}

	@Test
	public void testWorkspace() throws IOException, CoreException {
		runScript("get-file \"workspace:/\" | get children | assert-empty", uri);
		project.create(null);
		runScript("get-file \"workspace:/\" | get children | length | eq 1 | assert-true", uri);
	}

	@Test(expected = CoreException.class)
	public void testAbsentProject() throws IOException, CoreException {
		runScript("get-file \"workspace:/project\" | get children", uri);
	}

	@Test
	public void testProject() throws IOException, CoreException {
		createWorkspaceFile();
		int extraChildren = 0;
		if (project.getFolder(".settings").exists()) {
			extraChildren++;
		}
		String check = "get-file \"workspace:/project\" | get children | length | eq %d | assert-true";
		runScript(format(check, 2 + extraChildren), uri); // .project , folder
		file.delete(true, null);
		runScript(format(check, 2 + extraChildren), uri);
		folder.delete(true, null);
		runScript(format(check, 1 + extraChildren), uri);
	}

	@Test
	public void testAbsentFolder() throws IOException, CoreException {
		try {
			runScript("get-file \"workspace:/project/folder\" | get children");
			fail("Should throw");
		} catch (CoreException e) {
			Assert.assertEquals("Failed to read childen of /project/folder", e.getMessage());
		}
	}

	@Test
	public void testAbsentFolderInExistingProject() throws IOException, CoreException {
		project.delete(true, null);
		project.create(null);
		try {
			runScript("get-file \"workspace:/project/folder\" | get children", uri);
			fail("Should throw");
		} catch (CoreException e) {
			Assert.assertEquals("Failed to read childen of /project/folder", e.getMessage());
		}
	}


	@Test
	public void testFolder() throws IOException, CoreException {
		createWorkspaceFile();
		String check = "get-file \"workspace:/project/folder\" | get children | length | eq %d | assert-true";
		runScript(format(check, 1), uri);
		file.delete(true, null);
		runScript(format(check, 0), uri);
		folder.delete(true, null);
	}

	@Test(expected = CoreException.class)
	public void testFile() throws IOException, CoreException {
		createWorkspaceFile();
		runScript("get-file \"workspace:/project/folder/data.csv\" | get children", uri);
	}

	@Test
	public void testAbsentFile() throws IOException, CoreException {
		project.delete(true, null);
		project.create(null);
		project.open(null);
		folder.create(true, true, null);
		try {
			runScript("get-file \"workspace:/project/folder/data.csv\" | get children", uri);
			fail("Should throw");
		} catch (CoreException e) {
			Assert.assertEquals("Failed to read childen of /project/folder/data.csv", e.getMessage());
		}
	}


	private void createWorkspaceFile() throws CoreException {
		project.delete(true, null);
		project.create(null);
		project.open(null);
		folder.create(true, true, null);
		file.create(null, true, null);
		runScript("get-file \"%s\" | get exists | assert-true", uri);
	}
}
