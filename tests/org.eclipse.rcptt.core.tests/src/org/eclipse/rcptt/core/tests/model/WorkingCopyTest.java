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

import static org.junit.Assert.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.tests.NoErrorsInLog;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class WorkingCopyTest {
	static String PRJ_NAME = "workingCopyTests";
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	private static final IProject PROJECT = WORKSPACE.getRoot().getProject(PRJ_NAME);
	private IQ7Project q7project;
	private static final int LEAK_SIZE = 100;
	

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

	
	@Test
	public void queryDeletedResource() throws CoreException, InterruptedException {
		IQ7Project prj = q7project;
		for (int i = 0; i < LEAK_SIZE; i++) {
			ITestCase testcase = prj.getRootFolder().createTestCase("mytestcase"+i,true, new NullProgressMonitor());
			PROJECT.getFile(testcase.getName()).delete(true, null);
			try {
				testcase.getContexts(); // may cause leaks
				Assert.fail();
			} catch(ModelException e) {
				// expected, as resource is deleted
			}
			System.gc();
			NO_ERRORS.assertNoErrors();
		}
	}
	
	@Test
	public void deleteWorkingCopyResource() throws CoreException {
		for (int i = 0; i < LEAK_SIZE; i++) {
			ITestCase testcase = q7project.getRootFolder().createTestCase("mytestcase" + i, true, new NullProgressMonitor());

			ITestCase copy = (ITestCase) testcase
					.getWorkingCopy(new NullProgressMonitor());
			assertTrue(testcase.exists());
			assertFalse(copy.hasUnsavedChanges());
			PROJECT.getFile(testcase.getName()).delete(true, null);
			System.gc();
			NO_ERRORS.assertNoErrors();
		}
	}

	@Test
	public void deleteEditedResource() throws CoreException, InterruptedException {
		IQ7Project prj = q7project;
		IElementChangedListener listener = event -> {
			for (IQ7NamedElement i : event.getDelta().getNamedElements()) {
				try {
					i.getDescription(); // causes element info revival
				} catch (ModelException e) {
					// Does not exist
				}
			}
		};
		RcpttCore.addElementChangedListener(listener);
		try {
			for (int i = 0; i < LEAK_SIZE; i++) {
				ITestCase testcase = prj.getRootFolder().createTestCase("mytestcase"+i,true, new NullProgressMonitor());
		
				ITestCase copy = (ITestCase) testcase
						.getWorkingCopy(new NullProgressMonitor());
				assertTrue(testcase.exists());
				assertFalse(copy.hasUnsavedChanges());
				PROJECT.getFile(testcase.getName()).delete(true, null);
				try {
					testcase.getElementName();
				} catch (ModelException e) {
					// Does not exist
				}
				try {
					copy.getElementName();
				} catch (ModelException e) {
					// Does not exist
				}
				copy.discardWorkingCopy();
				System.gc();
				NO_ERRORS.assertNoErrors();
				assertFalse(testcase.exists());
			}
		} finally {
			RcpttCore.removeElementChangedListener(listener);
		}
	}

	
}
