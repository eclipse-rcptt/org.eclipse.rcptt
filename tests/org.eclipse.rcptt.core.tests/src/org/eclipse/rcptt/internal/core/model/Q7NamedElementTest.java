package org.eclipse.rcptt.internal.core.model;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.core.model.IQ7Project;
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
	public void noResourceleaks() throws CoreException, IOException {
		try (InputStream is = getClass().getResourceAsStream("testcase.test")) {
			TESTCASE_FILE.create(is, IFile.REPLACE|IFile.FORCE, null);
		}
		IFile previousFile = TESTCASE_FILE;
		for (int i = 0; i < 10000; i++) {
			IFile currentFile = PROJECT.getFile("t"+i+".test");
			previousFile.move(currentFile.getFullPath(), true, false, null);
			previousFile = currentFile;
			ITestCase testcase = (ITestCase) RcpttCore.create(currentFile);
			Assert.assertEquals("_-dqP0BOHEeOQfY3L4mNcSA", testcase.getID());
		}
		System.gc(); // Should not log errors. See org.eclipse.rcptt.core.persistence.LeakDetector
	}

}
