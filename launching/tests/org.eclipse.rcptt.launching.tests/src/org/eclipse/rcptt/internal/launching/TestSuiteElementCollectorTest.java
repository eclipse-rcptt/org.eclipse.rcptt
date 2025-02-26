package org.eclipse.rcptt.internal.launching;

import static org.eclipse.core.runtime.Path.fromPortableString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.index.TestSuiteElementCollector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSuiteElementCollectorTest {
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	

	@Test
	public void corruptedResourcesShouldProduceReadaleErrorMessage() throws CoreException, IOException {
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
			Assert.assertEquals("Empty resource platform:/resource/testSuiteReferencingCorruptedResource/corrupted.test", e.getMessage());
		}
		
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
