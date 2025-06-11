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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.tests.NoErrorsInLog;
import org.eclipse.rcptt.core.tests.Util;
import org.eclipse.rcptt.core.tests.Util.Comparer;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import junit.framework.TestCase;

public class DeltaTests {
	private static final ContextType CONTEXT_TYPE = ContextTypeManager.getInstance().getTypeById("org.eclipse.rcptt.ctx.workspace");

	private static final String PRJ_NAME = "ModelMembersq";
	
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
		org.eclipse.rcptt.core.tests.model.AbstractModelTestbase.disableAutoBulid();
	}

	@Test
	public void testNewTestcaseAppear() throws ModelException, InterruptedException {
		IQ7Project prj = q7project;
		startDeltas();
		prj.getRootFolder().createTestCase("mytestcase", true,
				new NullProgressMonitor());
		assertDeltas("Assert delta", "ModelMembersq[*]: {CHILDREN}\n"
				+ "\t<default>[*]: {CHILDREN}\n" + "\t\tmytestcase.test[+]: {}");
		ITestCase[] testCases = prj.getRootFolder().getTestCases();
		TestCase.assertEquals(1, testCases.length);
		stopDeltas();
	}

	@Test
	public void testNewFolderAppear() throws ModelException {
		IQ7Project prj = q7project;
		startDeltas();
		prj.createFolder(new Path("newfolder"));
		assertDeltas("Assert delta", "ModelMembersq[*]: {CHILDREN}\n"
				+ "\tnewfolder[+]: {}");
		stopDeltas();
	}

	@Test
	public void testDeleteTestcase() throws CoreException {
		IQ7Project prj = q7project;
		IQ7Folder folder = prj.createFolder(new Path("contexts"));
		IQ7NamedElement element = folder.createContext("group", CONTEXT_TYPE, false, null);
		startDeltas();
		element.getResource().delete(true, new NullProgressMonitor());
		TestCase.assertTrue(!element.exists());
		assertDeltas("Assert delta", "ModelMembersq[*]: {CHILDREN}\n"
				+ "\tcontexts[*]: {CHILDREN}\n" + "\t\tgroup.ctx[-]: {}");
		stopDeltas();
	}

	@Test
	public void testElementRename() throws CoreException {
		IQ7Project prj = q7project;
		IQ7Folder folder = prj.createFolder(new Path("contexts"));
		IQ7NamedElement element = folder.createContext("debug", CONTEXT_TYPE, false, null);
		startDeltas();
		IResource res = element.getResource();
		res.move(
				res.getFullPath().removeLastSegments(1)
						.append("newdebug.ctx"), true,
				new NullProgressMonitor());
		TestCase.assertTrue(!element.exists());
		assertDeltas("Assert delta", "ModelMembersq[*]: {CHILDREN}\n"
				+ "\tcontexts[*]: {CHILDREN}\n" + 
				"\t\tdebug.ctx[-]: {MOVED_TO(newdebug.ctx [in contexts [in ModelMembersq]])}\n" +
				"\t\tnewdebug.ctx[+]: {MOVED_FROM(debug.ctx [in ModelMembersq])}");
		stopDeltas();
	}
	
	private void assertDeltas(String message, String expected) {
		String actual = this.deltaListener.toString();
		if (!expected.equals(actual)) {
			System.out.println(actual);
		}
		assertEquals(message, expected, actual);
	}

	/**
	 * Starts listening to element deltas, and queues them in fgDeltas.
	 */
	public void startDeltas() {
		org.eclipse.rcptt.core.tests.model.AbstractModelTestbase.waitForAutoBuild();
		ModelManager.getModelManager().getIndexManager().waitUntilReady(new NullProgressMonitor());
		clearDeltas();
		RcpttCore.addElementChangedListener(this.deltaListener);
	}

	/**
	 * Stops listening to element deltas, and clears the current deltas.
	 */
	public void stopDeltas() {
		RcpttCore.removeElementChangedListener(this.deltaListener);
		clearDeltas();
	}

	/**
	 * Empties the current deltas.
	 */
	public void clearDeltas() {
		this.deltaListener.deltas = new IQ7ElementDelta[0];
	}
	
	class DeltaListener implements IElementChangedListener {
		IQ7ElementDelta[] deltas;

		@Override
		public void elementChanged(Q7ElementChangedEvent ev) {
			IQ7ElementDelta delta = ev.getDelta();
			if ((delta.getFlags() & IQ7ElementDelta.F_WORKING_COPY) != 0) {
				return; // Skip working copy delta here
			}
			IQ7ElementDelta[] copy = new IQ7ElementDelta[deltas.length + 1];
			System.arraycopy(deltas, 0, copy, 0, deltas.length);
			copy[deltas.length] = ev.getDelta();
			deltas = copy;
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0, length = this.deltas.length; i < length; i++) {
				IQ7ElementDelta delta = this.deltas[i];
				IQ7ElementDelta[] children = delta.getAffectedChildren();
				int childrenLength = children.length;
				IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
				int resourceDeltasLength = resourceDeltas == null ? 0
						: resourceDeltas.length;
				if (childrenLength == 0 && resourceDeltasLength == 0) {
					buffer.append(delta);
				} else {
					sortDeltas(children);
					for (int j = 0; j < childrenLength; j++) {
						buffer.append(children[j]);
						if (j != childrenLength - 1) {
							buffer.append("\n");
						}
					}
					for (int j = 0; j < resourceDeltasLength; j++) {
						if (j == 0 && buffer.length() != 0) {
							buffer.append("\n");
						}
						buffer.append(resourceDeltas[j]);
						if (j != resourceDeltasLength - 1) {
							buffer.append("\n");
						}
					}
				}
				if (i != length - 1) {
					buffer.append("\n\n");

				}
			}
			return buffer.toString();
		}

		private void sortDeltas(IQ7ElementDelta[] elementDeltas) {
			Comparer comparer = new Comparer() {
				@Override
				public int compare(Object a, Object b) {
					IQ7ElementDelta deltaA = (IQ7ElementDelta) a;
					IQ7ElementDelta deltaB = (IQ7ElementDelta) b;
					return deltaA.getElement().getName()
							.compareTo(deltaB.getElement().getName());
				}
			};
			Util.sort(elementDeltas, comparer);
		}
	}

	private DeltaListener deltaListener = new DeltaListener();
	
	/**
	 * Returns the delta for the given element from the cached delta. If the
	 * boolean is true returns the first delta found.
	 */
	private IQ7ElementDelta getDeltaFor(IQ7Element element,
			boolean returnFirst) {
		IQ7ElementDelta[] deltas = this.deltaListener.deltas;
		if (deltas == null)
			return null;
		IQ7ElementDelta result = null;
		for (int i = 0; i < deltas.length; i++) {
			IQ7ElementDelta delta = searchForDelta(element,
					this.deltaListener.deltas[i]);
			if (delta != null) {
				if (returnFirst) {
					return delta;
				}
				result = delta;
			}
		}
		return result;
	}

	/**
	 * Returns a delta for the given element in the delta tree
	 */
	private IQ7ElementDelta searchForDelta(IQ7Element element,
			IQ7ElementDelta delta) {

		if (delta == null) {
			return null;
		}
		if (delta.getElement().equals(element)) {
			return delta;
		}
		for (int i = 0; i < delta.getAffectedChildren().length; i++) {
			IQ7ElementDelta child = searchForDelta(element,
					delta.getAffectedChildren()[i]);
			if (child != null) {
				return child;
			}
		}
		return null;
	}

}
