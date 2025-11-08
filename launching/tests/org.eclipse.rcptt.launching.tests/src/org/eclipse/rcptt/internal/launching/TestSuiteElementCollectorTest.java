/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.internal.launching;

import static org.eclipse.core.runtime.Path.fromPortableString;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.launching.utils.TestSuiteElementCollector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSuiteElementCollectorTest {
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	

	@Test
	public void corruptedResourcesShouldProduceReadaleErrorMessage() throws CoreException, IOException, InterruptedException {
		TestSuiteElementCollector subject = new TestSuiteElementCollector(Arrays.asList("testsuite1"), false);
		IProject project = importProject(fromPortableString("/resources/testSuiteReferencingCorruptedResource"), new IPath[] {
				fromPortableString("corrupted.test"),
				fromPortableString("rcptt.properties"),
				fromPortableString("suite.suite")
		});
		
		try {
			RcpttCore.create(project).accept(subject);
			Assert.fail("Should throw on corrupted resource");
		} catch (ModelException e) {
			Assert.assertEquals("Empty resource platform:/resource/testSuiteReferencingCorruptedResource/corrupted.test. Empty metadata is allowed. File: /testSuiteReferencingCorruptedResource/corrupted.test", e.getMessage());
		}
	}
	
	@Test
	public void reportAbsentSuites() throws CoreException, IOException, InterruptedException {
		TestSuiteElementCollector subject = new TestSuiteElementCollector(Arrays.asList("suite", "absent"), false);
		IProject project = importProject(fromPortableString("/resources/org.eclipse.rcptt.test.normalsuite"), new IPath[] {
				fromPortableString("test.test"),
				fromPortableString("rcptt.properties"),
				fromPortableString("suite.suite")
		});
		IQ7Project q7Project = RcpttCore.create(project);
		q7Project.accept(subject);
		assertEquals(Set.of( "absent"), subject.getAbsentSuites());
		q7Project.getRootFolder().createTestSuite("absent", true, null);
		q7Project.accept(subject);
		assertEquals(Set.of(), subject.getAbsentSuites());
	}
	
	private IProject importProject(IPath bundleAbsoluteProjecRoot, IPath[] relativeProjectFiles) {
		Assert.assertTrue(bundleAbsoluteProjecRoot.isAbsolute());
		IProjectDescription description;
		IProject project;
		try (InputStream is = TestSuiteElementCollectorTest.class.getResourceAsStream(bundleAbsoluteProjecRoot.append(".project").toPortableString())) {
			description = WORKSPACE.loadProjectDescription(is);
			project = WORKSPACE.getRoot().getProject(description.getName());
			// Workspace operation is needed due to concurrency with org.eclipse.rcptt.internal.core.model.Q7Project.getMetadata()
			WORKSPACE.run(monitor -> {
				project.create(description, null);
				project.open(null);
				for (IPath filePath: relativeProjectFiles) {
					IFile file = project.getFile(filePath);
					try(InputStream fis = TestSuiteElementCollectorTest.class.getResourceAsStream(bundleAbsoluteProjecRoot.append(filePath).toPortableString())) {
						file.create(fis, true, null); 
					} catch (IOException e) {
						throw new AssertionError(e);
					}
				}				
			}, WORKSPACE.getRuleFactory().createRule(project), IWorkspace.AVOID_UPDATE, null);
			return project;
		} catch (Exception e) {
			RcpttPlugin.log(e);
			throw new AssertionError(e);
		}
	}

	@Before
	public void before() throws CoreException {
		IWorkspaceDescription d = WORKSPACE.getDescription();
		d.setAutoBuilding(false);
		WORKSPACE.setDescription(d);
		deleteProject("1");
		deleteProject("testSuiteReferencingCorruptedResource");
	}
	
	private void deleteProject(String name) throws CoreException {
		IProject project = WORKSPACE.getRoot().getProject(name);
		if (!project.exists()) {
			project.create(null);
		}
		project.open(null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT, null);
	}
}
