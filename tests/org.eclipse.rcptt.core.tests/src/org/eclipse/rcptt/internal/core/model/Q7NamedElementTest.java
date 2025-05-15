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
package org.eclipse.rcptt.internal.core.model;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.tests.NoErrorsInLog;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Q7NamedElementTest {
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	private static final IProject PROJECT = WORKSPACE.getRoot().getProject("TEST");
	private static final IFile TESTCASE_FILE = PROJECT.getFile("testcase.test");
	
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
		PROJECT.open(null);
	}

	@Test
	public void noResourceleaksID() {
		noResourceleaks(testcase -> Assert.assertEquals("_-dqP0BOHEeOQfY3L4mNcSA", testcase.getID()));
	}
	
	@Test
	public void noResourceleaksExists() {
		noResourceleaks(testcase -> assertTrue(testcase.exists()));
	}

	
	private interface ThrowingConsumer<T> {
		void accept(T data) throws Exception;
	}
	
	public void noResourceleaks(ThrowingConsumer<ITestCase> action) {
		try {
			try (InputStream is = getClass().getResourceAsStream("testcase.test")) {
				TESTCASE_FILE.create(is, IFile.REPLACE|IFile.FORCE, null);
			}
			IFile previousFile = TESTCASE_FILE;
			for (int i = 0; i < 1000; i++) {
				IFile currentFile = PROJECT.getFile("t"+i+".test");
				previousFile.move(currentFile.getFullPath(), true, false, null);
				previousFile = currentFile;
				ITestCase testcase = (ITestCase) RcpttCore.create(currentFile);
				action.accept(testcase);
				if ( i % 2 == 0) {
					action.accept(testcase); // sometimes single access does not cause leak
				}
				System.gc(); // Detect leaks with org.eclipse.rcptt.core.persistence.LeakDetector
				NO_ERRORS.assertNoErrors();
			}
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

}
