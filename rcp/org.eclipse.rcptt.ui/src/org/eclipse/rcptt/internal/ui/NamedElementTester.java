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
package org.eclipse.rcptt.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.ui.launching.LaunchUtils;

public class NamedElementTester extends PropertyTester {

	private static final String CONTAINS_NAMED_ELEMENTS_PROP = "containsNamedElements"; //$NON-NLS-1$

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		try {
		if (CONTAINS_NAMED_ELEMENTS_PROP.equals(property)) {
			ISelection selection = new StructuredSelection(receiver);
			IResource[] resources = LaunchUtils.getContext(selection);

			for (IResource res : resources) {
				if (!RcpttCore.hasRcpttNature(res.getProject())) {
					continue;
				}
				switch (res.getType()) {
				case IResource.PROJECT:
					IQ7Project project = RcpttCore.create((IProject) res);
					try {
						IQ7Folder[] folders = project.getFolders();
						for (IQ7Folder iq7Folder : folders) {
							if (iq7Folder.containsQ7Resources()) {
								return true;
							}
						}
					} catch (ModelException e) {
						Q7UIPlugin.log(e);
					}
					break;
				case IResource.FOLDER:
					IQ7Folder folder = (IQ7Folder) RcpttCore.create(res);
					try {
						if (folder.containsQ7Resources()) {
							return true;
						}
					} catch (ModelException e) {
						Q7UIPlugin.log(e);
					}
					// check all subfolders
					try {
						final List<IFolder> folders = new ArrayList<IFolder>();
						res.accept(new IResourceVisitor() {
							public boolean visit(IResource resource)
									throws CoreException {
								if (resource.getType() == IResource.FOLDER) {
									folders.add((IFolder) resource);
									return true;
								}
								return false;
							}
						});
						for (IFolder iFolder : folders) {
							IQ7Folder folder2 = (IQ7Folder) RcpttCore
									.create(iFolder);
							try {
								if (folder2.containsQ7Resources()) {
									return true;
								}
							} catch (ModelException e) {
								Q7UIPlugin.log(e);
							}
						}
					} catch (CoreException e) {
						Q7UIPlugin.log(e);
					}
					break;

				case IResource.FILE:
					if (RcpttCore.isQ7File(res.getFullPath())) {
						return true;
					}
				}
			}
			return false;
		}
		return false;
	} catch (InterruptedException e) {
		OperationCanceledException result = new OperationCanceledException();
		result.initCause(e);
		throw result;
	}
	}
}
