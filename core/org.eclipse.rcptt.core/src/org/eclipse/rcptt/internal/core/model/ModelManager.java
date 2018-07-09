/*******************************************************************************
 * Copyright (c) 2009, 2015 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IParent;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.ISearchScope;
import org.eclipse.rcptt.core.scenario.TestSuiteItem;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.cache.ModelCache;
import org.eclipse.rcptt.internal.core.model.deltas.DeltaProcessingState;
import org.eclipse.rcptt.internal.core.model.deltas.DeltaProcessor;
import org.eclipse.rcptt.internal.core.model.deltas.Q7ElementDeltaBuilder;
import org.eclipse.rcptt.internal.core.model.index.IndexManager;
import org.eclipse.rcptt.internal.core.model.index.ProjectIndexerManager;

public class ModelManager {
	private static ModelManager instance;
	private ModelCache cache;// = new ModelCache();

	public DeltaProcessingState deltaState = new DeltaProcessingState();

	private Map<Q7NamedElement, PerWorkingCopyInfo> perWorkingCopyInfos = new HashMap<Q7NamedElement, PerWorkingCopyInfo>(
			5);
	/**
	 * Unique handle onto the Model
	 */
	private final Q7Model model = new Q7Model();
	private IndexManager indexManager;
	private Set<IProject> buildingProjects = new HashSet<IProject>();

	public ModelManager() {
		if (Platform.isRunning())
			this.indexManager = new IndexManager();
	}

	public synchronized static ModelManager getModelManager() {
		if (instance == null) {
			instance = new ModelManager();
		}
		return instance;
	}

	public void startup() {
		this.cache = new ModelCache();

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(this.deltaState,
				IResourceChangeEvent.PRE_BUILD
						| IResourceChangeEvent.POST_BUILD
						| IResourceChangeEvent.POST_CHANGE
						| IResourceChangeEvent.PRE_DELETE
						| IResourceChangeEvent.PRE_CLOSE);
		RcpttCore.getInstance();
		getIndexManager().reset();
		ProjectIndexerManager.startIndexing();
	}

	public void shutdown() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this.deltaState);
	}

	public synchronized Object getInfo(IQ7Element element) {
		return this.cache.getInfo(element);
	}

	protected synchronized void putInfos(IQ7Element openedElement,
			Map<IQ7Element, Object> newElements) {
		// remove children
		Object existingInfo = this.cache.peekAtInfo(openedElement);
		if (openedElement instanceof IParent
				&& existingInfo instanceof Q7ElementInfo) {
			IQ7Element[] children = ((Q7ElementInfo) existingInfo)
					.getChildren();
			for (int i = 0, size = children.length; i < size; ++i) {
				Q7Element child = (Q7Element) children[i];
				try {
					child.close();
				} catch (ModelException e) {
					// ignore
				}
			}
		}
		Iterator<Map.Entry<IQ7Element, Object>> iterator = newElements
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<IQ7Element, Object> element = iterator.next();
			this.cache.putInfo(element.getKey(), element.getValue());
		}
	}

	synchronized Object removeInfoAndChildren(Q7Element element)
			throws ModelException {
		Object info = this.cache.peekAtInfo(element);
		if (info != null) {
			element.closing(info);
			if (element instanceof IParent && info instanceof Q7ElementInfo) {
				IQ7Element[] children = ((Q7ElementInfo) info).getChildren();
				for (int i = 0, size = children.length; i < size; ++i) {
					Q7Element child = (Q7Element) children[i];
					child.close();
				}
			}
			this.cache.removeInfo(element);
			return info;
		}
		return null;
	}

	private static IQ7Element create(IFile file, IQ7Project project) {
		if (file == null) {
			return null;
		}

		if (project == null) {
			project = RcpttCore.create(file.getProject());
		}
		IQ7Folder folder = project.getFolder(file.getParent()
				.getProjectRelativePath());
		return folder.getNamedElement(file.getName());
	}

	private static IQ7Element create(IFolder file, IQ7Project project) {
		if (file == null) {
			return null;
		}
		if (project == null) {
			project = RcpttCore.create(file.getProject());
		}
		return project.getFolder(file.getProjectRelativePath());

	}

	public static IQ7Element create(IResource resource, IQ7Project project) {
		if (resource == null) {
			return null;
		}
		int type = resource.getType();
		switch (type) {
		case IResource.PROJECT:
			return RcpttCore.create((IProject) resource);
		case IResource.FILE:
			return create((IFile) resource, project);
		case IResource.FOLDER:
			return create((IFolder) resource, project);
		case IResource.ROOT:
			return getModelManager().getModel();
		default:
			return null;
		}
	}

	public Q7Model getModel() {
		return model;
	}

	public IndexManager getIndexManager() {
		return indexManager;
	}

	Object peekAtInfo(IQ7Element element) {
		return this.cache.peekAtInfo(element);
	}

	static class PerWorkingCopyInfo {
		private int useCount = 0;
		private IQ7NamedElement workingCopy;
		Q7ResourceInfo resourceInfo;
		boolean complete = false;

		private PerWorkingCopyInfo(IQ7NamedElement workingCopy) {
			this.workingCopy = workingCopy;
		}

		public IQ7NamedElement getWorkingCopy() {
			synchronized (this) {
				while (!complete) {
					try {
						this.wait(100);
					} catch (InterruptedException e) {
						RcpttPlugin.log(e);
					}
				}
			}
			return this.workingCopy;
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Info for "); //$NON-NLS-1$
			buffer.append(((Q7Element) this.workingCopy)
					.toStringWithAncestors());
			buffer.append("\nUse count = "); //$NON-NLS-1$
			buffer.append(this.useCount);
			return buffer.toString();
		}

		private void close() {
			if (resourceInfo != null) {
				resourceInfo.unload();
			}
		}
	}

	PerWorkingCopyInfo getPerWorkingCopyInfo(Q7NamedElement workingCopy,
			boolean create, boolean recordUsage) {
		synchronized (this.perWorkingCopyInfos) { // use the
			PerWorkingCopyInfo info = perWorkingCopyInfos.get(workingCopy);
			if (info == null && create) {
				info = new PerWorkingCopyInfo(workingCopy);
				perWorkingCopyInfos.put(workingCopy, info);
			}
			if (info != null && recordUsage)
				info.useCount++;
			return info;
		}
	}

	public DeltaProcessor getDeltaProcessor() {
		return this.deltaState.getDeltaProcessor();
	}

	int discardPerWorkingCopyInfo(Q7NamedElement workingCopy)
			throws ModelException {
		// create the delta builder (this remembers the current content of the
		// working copy)
		// outside the perWorkingCopyInfos lock (see bug 50667)
		Q7ElementDeltaBuilder deltaBuilder = null;
		if (workingCopy.hasUnsavedChanges()) {
			deltaBuilder = new Q7ElementDeltaBuilder(workingCopy);
		}
		PerWorkingCopyInfo info = null;
		synchronized (this.perWorkingCopyInfos) {
			info = perWorkingCopyInfos.get(workingCopy);
			if (info == null)
				return -1;
			if (--info.useCount == 0) {
				// remove per working copy info
				perWorkingCopyInfos.remove(workingCopy);
			}
		}
		if (info.useCount == 0) { // info cannot be null here (check was done
			// above)
			// remove infos + close buffer (since no longer working copy)
			// outside the perWorkingCopyInfos lock (see bug 50667)
			removeInfoAndChildren(workingCopy);
			workingCopy.close();
			info.close();
			// compute the delta if needed and register it if there are changes
			if (deltaBuilder != null) {
				deltaBuilder.buildDeltas();
				if ((deltaBuilder.delta != null)
						&& (deltaBuilder.delta.getAffectedChildren().length > 0)) {
					getDeltaProcessor().registerModelDelta(deltaBuilder.delta);
				}
			}
		}
		return info.useCount;
	}

	public void projectStartBuilding(IProject project) {
		synchronized (buildingProjects) {
			buildingProjects.add(project);
		}
	}

	public void projectStopBuilding(IProject project) {
		synchronized (buildingProjects) {
			buildingProjects.remove(project);
		}
	}

	public boolean isProjectBuilding() {
		synchronized (buildingProjects) {
			return !buildingProjects.isEmpty();
		}
	}

	private static final IQ7NamedElement[] EMPTY_NAMED_ELEMENTS = new IQ7NamedElement[0];

	public IQ7NamedElement[] findContextUsageInWorkingCopies(String contextId,
			ISearchScope scope) throws ModelException {
		List<IQ7NamedElement> result = new ArrayList<IQ7NamedElement>();

		for (PerWorkingCopyInfo info : perWorkingCopyInfos.values())
			if (scope.contains(info.workingCopy.getResource().getFullPath())) {
				IQ7NamedElement element = info.workingCopy;
				if (element instanceof ITestCase) {
					for (String cid : ((ITestCase) element).getContexts())
						if (contextId.equals(cid))
							result.add(element);
				} else if (element instanceof IContext) {
					for (String cid : RcpttCore.getInstance().getContextReferences((IContext) element))
						if (contextId.equals(cid))
							result.add(element);
				}
			}

		return result.toArray(EMPTY_NAMED_ELEMENTS);
	}

	public IQ7NamedElement[] findVerificationUsageInWorkingCopies(
			String verificationId, ISearchScope scope) throws ModelException {
		List<IQ7NamedElement> result = new ArrayList<IQ7NamedElement>();

		for (PerWorkingCopyInfo info : perWorkingCopyInfos.values())
			if (scope.contains(info.workingCopy.getResource().getFullPath())) {
				IQ7NamedElement element = info.workingCopy;
				if (element instanceof ITestCase) {
					for (String vid : ((ITestCase) element).getVerifications())
						if (verificationId.equals(vid))
							result.add(element);
				}
			}

		return result.toArray(EMPTY_NAMED_ELEMENTS);
	}

	public IQ7NamedElement[] findTestCaseUsageInWorkingCopies(
			String testCaseId, ISearchScope scope) throws ModelException {
		List<IQ7NamedElement> result = new ArrayList<IQ7NamedElement>();

		for (PerWorkingCopyInfo info : perWorkingCopyInfos.values())
			if (scope.contains(info.workingCopy.getResource().getFullPath())) {
				IQ7NamedElement element = info.workingCopy;
				if (element instanceof ITestSuite) {
					for (TestSuiteItem i : ((ITestSuite) element).getItems())
						if (testCaseId.equals(i.getNamedElementId()))
							result.add(element);
				}
			}

		return result.toArray(EMPTY_NAMED_ELEMENTS);
	}

	public IQ7NamedElement[] findTestSuiteUsageInWorkingCopies(
			String testSuiteId, ISearchScope scope) throws ModelException {
		List<IQ7NamedElement> result = new ArrayList<IQ7NamedElement>();

		for (PerWorkingCopyInfo info : perWorkingCopyInfos.values())
			if (scope.contains(info.workingCopy.getResource().getFullPath())) {
				IQ7NamedElement element = info.workingCopy;
				if (element instanceof ITestSuite) {
					for (TestSuiteItem i : ((ITestSuite) element).getItems())
						if (testSuiteId.equals(i.getNamedElementId()))
							result.add(element);
				}
			}

		return result.toArray(EMPTY_NAMED_ELEMENTS);
	}
}
