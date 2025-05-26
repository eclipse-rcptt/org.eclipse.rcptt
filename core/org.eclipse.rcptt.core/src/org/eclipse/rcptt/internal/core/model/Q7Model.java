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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Model;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

public class Q7Model extends Openable implements IQ7Model {

	protected Q7Model() throws IllegalArgumentException {
		super(null);
	}

	public HandleType getElementType() {
		return HandleType.Model;
	}

	public IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public IQ7Project getProject(String name) {
		return new Q7Project(ResourcesPlugin.getWorkspace().getRoot()
				.getProject(name), this);
	}

	public IQ7Project getProject(IResource resource) {
		switch (resource.getType()) {
		case IResource.FOLDER:
			return new Q7Project(((IFolder) resource).getProject(), this);
		case IResource.FILE:
			return new Q7Project(((IFile) resource).getProject(), this);
		case IResource.PROJECT:
			return new Q7Project((IProject) resource, this);
		default:
			throw new IllegalArgumentException("Invalid argument");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IQ7Project[] getProjects() throws ModelException, InterruptedException {
		final List list = getChildrenOfType(HandleType.Project);
		return (IQ7Project[]) list.toArray(new IQ7Project[list.size()]);
	}

	@Override
	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm,
			IResource underlyingResource) throws ModelException {
		List<IQ7Project> result = new ArrayList<IQ7Project>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (IProject iProject : projects) {
			if (iProject.isAccessible() && iProject.isOpen()) {
				try {
					if (RcpttNature.isRcpttProject(iProject)) {
						result.add(getProject(iProject.getName()));
					}
				} catch (CoreException e) {
					RcpttPlugin.log(e);
				}
			}
		}
		info.setChildren(result);

		return true;
	}

	@Override
	protected ModelInfo createElementInfo() {
		return new ModelInfo();
	}

	public static Object getTarget(IContainer container, IPath path,
			boolean checkResourceExistence) {
		if (path == null)
			return null;
		if (path.getDevice() == null) { // container relative paths should not
			IResource resource = container.findMember(path);
			if (resource != null) {
				if (!checkResourceExistence || resource.exists())
					return resource;
				return null;
			}
		}
		if (!path.isAbsolute())
			return null;
		return null;
	}

	public IPath getPath() {
		return Path.ROOT;
	}

	public String getName() {
		return "";
	}

	public Object[] getForeignResources() throws ModelException, InterruptedException {
		return openAndAccessInfo(info -> ((ModelInfo)info).getForeignResources(), null);
	}

	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public boolean equals(Object o) {
		if (!(o instanceof Q7Model))
			return false;
		return super.equals(o);
	}

	protected void toStringInfo(int tab, StringBuffer buffer, Object info,
			boolean showResolvedInfo) {
		buffer.append(this.tabString(tab));
		buffer.append("Model"); //$NON-NLS-1$
		if (info == null) {
			buffer.append(" (not open)"); //$NON-NLS-1$
		}
	}
}
