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
package org.eclipse.rcptt.core.tests.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.tests.NoErrorsInLog;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class WorkingCopyTest {
	static String PRJ_NAME = "workingCopyTests";
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	private static final IProject PROJECT = WORKSPACE.getRoot().getProject(PRJ_NAME);
	private IQ7Project q7project;
	

	@Rule
	public final NoErrorsInLog NO_ERRORS = new NoErrorsInLog(RcpttCore.class);
	
	@Before
	@After
	public void cleanup() throws CoreException {
		for (IProject i: WORKSPACE.getRoot().getProjects()) {
			i.delete(true,  true, null);
		}
	}
	
	@Before
	public void before() throws CoreException {
		IProjectDescription deQ7ion = WORKSPACE.newProjectDescription(PROJECT.getName());
		deQ7ion.setNatureIds(new String[] { RcpttNature.NATURE_ID });
		PROJECT.create(deQ7ion, null);
		q7project =  RcpttCore.create(PROJECT);
		PROJECT.open(null);
	}
	
	@Test
	public void testNewTestcaseAppear() throws ModelException {
		IQ7Project prj = q7project;
		ITestCase testcase = prj.getRootFolder().createTestCase("mytestcase",
				true, new NullProgressMonitor());

		ITestCase copy = (ITestCase) testcase
				.getWorkingCopy(new NullProgressMonitor());

		copy.setElementName("new test case name");
		copy.setTags("tag0");

		assertEquals("mytestcase", testcase.getElementName());
		assertEquals("new test case name", copy.getElementName());

		copy.commitWorkingCopy(true, new NullProgressMonitor());
		assertEquals("new test case name", testcase.getElementName());

	}

	public void _testWorkingCopyForNewResource() throws ModelException {
		IQ7Project prj = q7project;
		ITestCase testcase = prj.getRootFolder().getTestCase("newTestCase.test");
		assertTrue(!testcase.exists());
		ITestCase copy = (ITestCase) testcase
				.getWorkingCopy(new NullProgressMonitor());

		copy.setElementName("new test case name");
		copy.setTags("tag0");

		assertEquals("new test case name", copy.getElementName());

		copy.commitWorkingCopy(true, new NullProgressMonitor());
		assertEquals("new test case name", testcase.getElementName());

	}
}
