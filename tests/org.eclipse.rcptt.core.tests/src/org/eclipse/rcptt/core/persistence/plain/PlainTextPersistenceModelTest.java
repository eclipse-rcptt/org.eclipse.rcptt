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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.Q7LazyResource;
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
		IFile file = createFile();
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

	private URI toURI(IFile file) {
		return URI.createPlatformResourceURI(file.getFullPath().toString(), true);
	}

	private IFile createFile() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("x");
		project.create(null);
		project.open(null);
		IFile file = project.getFile("test.test");
		return file;
	}

}
