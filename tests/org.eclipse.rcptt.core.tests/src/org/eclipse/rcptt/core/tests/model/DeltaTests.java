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

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.tests.CoreTestsPlugin;

public class DeltaTests extends AbstractModelTestbase {
	private static final String PRJ_NAME = "ModelMembersq";

	public DeltaTests(String name) {
		super(CoreTestsPlugin.PLUGIN_ID, name);
	}

	public static Test suite() {
		return new Suite(DeltaTests.class);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		setUpQ7ProjectTo(PRJ_NAME, "ModelMembers");
	}

	public void tearDownSuite() throws Exception {
		deleteProject(PRJ_NAME);
		super.tearDownSuite();
	}

	public void testNewTestcaseAppear() throws ModelException, InterruptedException {
		IQ7Project prj = getQ7Project(PRJ_NAME);
		startDeltas();
		prj.getRootFolder().createTestCase("mytestcase", true,
				new NullProgressMonitor());
		assertDeltas("Assert delta", "ModelMembersq[*]: {CHILDREN}\n"
				+ "\t<default>[*]: {CHILDREN}\n" + "\t\tmytestcase.test[+]: {}");
		ITestCase[] testCases = prj.getRootFolder().getTestCases();
		TestCase.assertEquals(1, testCases.length);
		stopDeltas();
	}

	public void testNewFolderAppear() throws ModelException {
		IQ7Project prj = getQ7Project(PRJ_NAME);
		startDeltas();
		prj.createFolder(new Path("newfolder"));
		assertDeltas("Assert delta", "ModelMembersq[*]: {CHILDREN}\n"
				+ "\tnewfolder[+]: {}");
		stopDeltas();
	}

	public void testDeleteTestcase() throws ModelException {
		IQ7Project prj = getQ7Project(PRJ_NAME);
		startDeltas();
		IQ7NamedElement element = prj.getFolder(new Path("contexts"))
				.getNamedElement("group.ctx");
		try {
			element.getResource().delete(true, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		TestCase.assertTrue(!element.exists());
		assertDeltas("Assert delta", "ModelMembersq[*]: {CHILDREN}\n"
				+ "\tcontexts[*]: {CHILDREN}\n" + "\t\tgroup.ctx[-]: {}");
		stopDeltas();
	}

	public void testElementRename() throws ModelException {
		IQ7Project prj = getQ7Project(PRJ_NAME);
		startDeltas();
		IQ7NamedElement element = prj.getFolder(new Path("contexts"))
				.getNamedElement("debug.ctx");
		try {
			IResource res = element.getResource();
			res.move(
					res.getFullPath().removeLastSegments(1)
							.append("newdebug.ctx"), true,
					new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		TestCase.assertTrue(!element.exists());
		assertDeltas("Assert delta", "ModelMembersq[*]: {CHILDREN}\n"
				+ "\tcontexts[*]: {CHILDREN}\n" + 
				"\t\tdebug.ctx[-]: {MOVED_TO(newdebug.ctx [in contexts [in ModelMembersq]])}\n" +
				"\t\tnewdebug.ctx[+]: {MOVED_FROM(debug.ctx [in ModelMembersq])}");
		stopDeltas();
	}
}
