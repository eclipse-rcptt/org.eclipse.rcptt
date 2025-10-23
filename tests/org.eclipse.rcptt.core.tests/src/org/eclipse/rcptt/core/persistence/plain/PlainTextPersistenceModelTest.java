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
package org.eclipse.rcptt.core.persistence.plain;

import static java.lang.System.currentTimeMillis;
import static org.eclipse.core.runtime.Path.fromPortableString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.tests.model.AbstractModelTestbase;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.internal.core.Q7LazyResource;
import org.eclipse.rcptt.internal.core.model.Q7Folder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PlainTextPersistenceModelTest {
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	
	@Before
	public void before() throws CoreException {
		for (IProject project: WORKSPACE.getRoot().getProjects()) {
			project.delete(true, true, null);
		}
	}
	
	@Test
	public void deleteDescription() throws IOException, CoreException {
		ITestCase file = createTestCase();
		URI uri = toURI(file);
		Scenario scenario = ScenarioFactory.eINSTANCE.createScenario();
		scenario.setVersion(Double.toString(RcpttCore.SCENARIO_VERSION));
		scenario.setId(EcoreUtil.generateUUID());
		scenario.setName("test");
		scenario.setDescription("description1");
		scenario = saveLoad(scenario, uri);
		Assert.assertEquals("description1", scenario.getDescription());
		scenario.setDescription(null);
		scenario = saveLoad(scenario, uri);
		Assert.assertNull(scenario.getDescription());
	}
	
	@Test
	public void returnNullWhenResourceDoesNotExist() throws IOException, CoreException {
		ITestCase file = createTestCase();
		URI uri = toURI(file);
		Scenario scenario = ScenarioFactory.eINSTANCE.createScenario();
		scenario.setVersion(Double.toString(RcpttCore.SCENARIO_VERSION));
		scenario.setId(EcoreUtil.generateUUID());
		scenario.setName("test");
		scenario.setDescription("description1");
		saveLoad(scenario, uri);
		Q7LazyResource resource = new Q7LazyResource(uri);
		
		Job noise = Job.create("Delete/create", (ICoreRunnable) monitor -> {
			while (!monitor.isCanceled()) {
				file.getResource().delete(true, monitor);
				saveLoad(scenario, uri);
			}
		});
		noise.setPriority(Job.INTERACTIVE);
		noise.schedule(0);
		try {
			for (long stop = currentTimeMillis() + 1000; currentTimeMillis() < stop;) {
				PersistenceManager.getInstance().getModel(resource).updateMetadata(); // should not throw
			}
		} finally {
			noise.cancel();
		}
	}
	
	
	@Test
	public void createAfterDeletion() throws CoreException {
		AbstractModelTestbase.disableAutoBulid();
		ITestCase test1 = createTestCase();
		int iteration = 0;
		try {
			for (long stop = currentTimeMillis() + 2000; currentTimeMillis() < stop;) {
				String content = "script " + iteration;
				setContent(test1, content);
				assertEquals(content,  ((Script)test1.getContent()).getContent());
				test1.getResource().delete(true, null);
				iteration++;
			}
		} catch (Throwable e) {
			throw new AssertionError("Failed on iteration " + iteration, e);
		}
	}
	
	@Test
	public void ensureNoCrosstalkBetweenDifferentFiles() throws IOException, CoreException {
		IQ7Project project = getProject();
		
		Path folderPath = Path.forPosix("VeryLongDirectoryName".repeat(10)); // to defeat suffix-based id generation
		IFolder folderResource = project.getProject().getFolder(Path.forPosix("1").append(folderPath));
		create(folderResource);
		IQ7Folder folder1 = (IQ7Folder) RcpttCore.create(folderResource);
		folderResource = project.getProject().getFolder(Path.forPosix("2").append(folderPath));
		create(folderResource);
		IQ7Folder folder2 = (IQ7Folder) RcpttCore.create(folderResource);
		
		ITestCase test1 = folder1.createTestCase("test1", true, null);
		ITestCase test2 = folder2.createTestCase("test1", true, null); // Same name, different path
		
		Job noise = Job.create("Creating similar file", (ICoreRunnable) monitor -> {
			while (!monitor.isCanceled()) {
				setContent(test2, "script2");
				test2.getResource().delete(true, monitor);
			}
		});
		noise.setPriority(Job.INTERACTIVE);
		noise.schedule();
		try {
			int iteration = 0;
			try {
				for (long stop = currentTimeMillis() + 2000; currentTimeMillis() < stop;) {
					setContent(test1, "script1");
					assertTrue(test1.getResource().exists());
					assertTrue(test1.exists());
					assertEquals("script1",  ((Script)test1.getContent()).getContent());
					test1.getResource().delete(true, null);
					iteration++;
				}
			} catch (Throwable e) {
				throw new AssertionError("Failed on iteration " + iteration, e);
			}
		} finally {
			noise.cancel();
		}
		
	}

	private void setContent(ITestCase test, String content) throws CoreException {
		create(test.getResource().getParent());
		if (!test.exists()) {
			ITestCase newTest = ((IQ7Folder)test.getParent()).createTestCase(test.getPath().removeFileExtension().lastSegment(), true, null);
			assertEquals(test, newTest);
		}
		assertTrue(test.exists());
		assertTrue(test.getResource().exists());
		ITestCase wc = (ITestCase) test.getWorkingCopy(null);
		try {
			Script script = CoreFactory.eINSTANCE.createScript();
			script.setContent(content);
			wc.setContent(script);
		} finally {
			wc.commitWorkingCopy(true, null);
			wc.discardWorkingCopy();
		}
	}

	
	private void create(IContainer folder) throws CoreException {
		if (folder.exists()) {
			return;
		}
		create(folder.getParent());
		((IFolder)folder).create(true, true, null);
	}

	private Scenario saveLoad(Scenario scenario, URI uri) {
		Resource resource = new Q7LazyResource(uri);
		resource.setTrackingModification(true);
		resource.getContents().add(scenario);
		resource = saveLoad(resource);
		return (Scenario) resource.getContents().get(0);

	}

	private Resource saveLoad(Resource resource) {
		PersistenceManager.getInstance().saveResource(resource);
		resource = new Q7LazyResource(resource.getURI());
		PersistenceManager.getInstance().getModel(resource).updateMetadata();
		return resource;
	}

	private URI toURI(IQ7Element file) {
		return URI.createPlatformResourceURI(file.getPath().toString(), true);
	}

	private ITestCase createTestCase() throws CoreException { 
		return getProject().getRootFolder().createTestCase("test", true, null);
	}

	private IQ7Project getProject() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("x");
		if (!project.isAccessible()) {
			project.create(null);
			project.open(null);
			RcpttNature.updateProjectNature(project, true);
		}
		IQ7Project result = RcpttCore.create(project);
		assert result.exists();
		return result;
	}

}
