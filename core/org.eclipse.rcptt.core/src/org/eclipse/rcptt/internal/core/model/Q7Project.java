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
package org.eclipse.rcptt.internal.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IQ7ProjectMetadata;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

public class Q7Project extends Openable implements IQ7Project {

	private IProject project;

	protected Q7Project(IProject project, Q7Element parent)
			throws IllegalArgumentException {
		super(parent);
		if (parent == null)
			throw new NullPointerException();
		this.project = project;
	}

	public HandleType getElementType() {
		return HandleType.Project;
	}

	public IResource getResource() {
		return project;
	}

	public IProject getProject() {
		return project;
	}

	public Object[] getForeignResources() throws ModelException {
		return ((Q7ProjectInfo) getElementInfo()).getForeignResources(project);
	}

	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm, Map<IQ7Element, Object> newElements,
			IResource underlyingResource) throws ModelException {
		// check whether this project fragment can be opened
		if (!this.resourceExists()) {
			throw this.newNotPresentException();
		}

		getMetadata(); // Trigger metadata creation.

		// TODO determine fragment kind
		return this.computeChildren(info, newElements);
	}

	protected boolean computeChildren(OpenableElementInfo info,
			Map<IQ7Element, Object> newElements) throws ModelException {
		try {
			IResource underlyingResource = this.getResource();
			if (underlyingResource.getType() == IResource.FOLDER
					|| underlyingResource.getType() == IResource.PROJECT) {
				List<IQ7Element> vChildren = new ArrayList<IQ7Element>(5);
				IContainer rootFolder = (IContainer) underlyingResource;
				this.computeFolderChildren(rootFolder, Path.EMPTY, vChildren);
				info.setChildren(vChildren
						.toArray(new IQ7Element[vChildren.size()]));
			}
		} catch (ModelException e) {
			// problem resolving children; structure remains unknown
			info.setChildren(new IQ7Element[] {});
			throw e;
		}
		return true;
	}

	protected void computeFolderChildren(IContainer folder, IPath path,
			List<IQ7Element> vChildren) throws ModelException {
		IQ7Folder pkg = this.getFolder(path);
		vChildren.add(pkg);
		try {
			IResource[] members = folder.members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource member = members[i];
				String memberName = member.getName();
				switch (member.getType()) {
				case IResource.FOLDER:
					IPath newPath = path.append(memberName);
					this.computeFolderChildren((IFolder) member, newPath,
							vChildren);
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			/* could be thrown by ElementTree when path is not found */
			throw new ModelException(e, "Element doesn't exist");
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	public IQ7Folder getFolder(IPath path) {
		return new Q7Folder(this, path);
	}

	public IPath getPath() {
		return project.getFullPath();
	}

	@Override
	protected Object createElementInfo() {
		return new Q7ProjectInfo();
	}

	public String getName() {
		return project.getName();
	}

	public IQ7Folder[] getFolders() throws ModelException {
		List<IQ7Element> result = getChildrenOfType(HandleType.Folder);
		return result.toArray(new IQ7Folder[result.size()]);
	}

	public IQ7Project[] getReferences() {
		try {
			List<IQ7Project> references = new ArrayList<IQ7Project>();
			IProject[] projects = project.getReferencedProjects();
			for (IProject iProject : projects) {
				if (iProject.exists() && iProject.isOpen()) {
					references.add(RcpttCore.create(iProject));
				}
			}
			return references.toArray(new IQ7Project[references.size()]);
		} catch (Exception e) {
			// ignore exceptions
		}
		return new IQ7Project[0];
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> find(Class<T> class_, String id) throws ModelException {
		IQ7NamedElement[] elements = Q7SearchCore.findById(id,
				new ReferencedProjectScope(this), new NullProgressMonitor());
		List<T> contexts = new ArrayList<T>();
		for (IQ7NamedElement e : elements) {
			if (class_.isInstance(e))
				contexts.add((T) e);
		}
		return contexts;
	}

	public IQ7NamedElement[] findNamedElement(String id) throws ModelException {
		return Q7SearchCore.findById(id, new ReferencedProjectScope(this),
				new NullProgressMonitor());
	}

	public void resetCaches() {
	}

	public boolean contains(IResource resource) {
		return project.getFullPath().isPrefixOf(resource.getFullPath());
	}

	public int hashCode() {
		return this.project.hashCode();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Q7Project))
			return false;
		Q7Project other = (Q7Project) o;
		return this.project.equals(other.getProject());
	}

	public IQ7Folder getRootFolder() {
		return getFolder(new Path(""));
	}

	public IQ7Folder createFolder(Path path) {
		try {
			project.getFolder(path).create(true, true,
					new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return getFolder(path);
	}

	private static boolean resourceExists(IResource resource) {
		return resource.exists();
	}

	public IQ7ProjectMetadata getMetadata() {
		if (!Q7Features.supportQ7OptionsFile) {
			return null;
		}
		final Q7Folder rootFolder = (Q7Folder) getRootFolder();
		IQ7ProjectMetadata metadata = rootFolder.getMetadata();

		if (!resourceExists(metadata.getResource())) {
			createMetadataJob.schedule();
		}
		return metadata;
	}
	
	private static final IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private static final Job createMetadataJob = new WorkspaceJob("Create Q7 project metadata") {
		{
			setRule(workspace.getRuleFactory().modifyRule(workspace.getRoot()));
		}
		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) {
			try {
				for (IProject p: workspace.getRoot().getProjects()) {
					if (RcpttCore.hasRcpttNature(p)) {
						ResourceAttributes attrs = p.getResourceAttributes();
						if (attrs.isReadOnly() || attrs.isHidden()) {
							continue;
						}
						IQ7Project rcpttProject = RcpttCore.create(p);
						IResource resource = rcpttProject.getMetadata().getResource();
						resource.refreshLocal(IResource.DEPTH_ZERO, monitor);
						if (!resourceExists(resource)) {
							((Q7Folder)rcpttProject.getRootFolder()).createMetadata(true, monitor);
						}
					}
				}
			} catch (CoreException  e) {
				RcpttPlugin.log(e);
			}
			return Status.OK_STATUS;
		};
	};

}
