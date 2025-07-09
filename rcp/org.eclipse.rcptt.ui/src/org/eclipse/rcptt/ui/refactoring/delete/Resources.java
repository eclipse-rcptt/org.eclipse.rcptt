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
package org.eclipse.rcptt.ui.refactoring.delete;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

public class Resources {
	public static boolean containsOnlyProjects(IResource[] resources) {
		int types = getSelectedResourceTypes(resources);
		// note that the selection may contain multiple types of resource
		return types == IResource.PROJECT;
	}

	private static int getSelectedResourceTypes(IResource[] resources) {
		int types = 0;
		for (int i = 0; i < resources.length; i++) {
			types |= resources[i].getType();
		}
		return types;
	}
	/**
	 * Checks if the given resource is in sync with the underlying file system.
	 *
	 * @param resource the resource to be checked
	 * @return IStatus status describing the check's result. If <code>status.
	 * isOK()</code> returns <code>true</code> then the resource is in sync
	 */
	public static IStatus checkInSync(IResource resource) {
		return checkInSync(new IResource[] {resource});
	}

	/**
	 * Checks if the given resources are in sync with the underlying file
	 * system.
	 *
	 * @param resources the resources to be checked
	 * @return IStatus status describing the check's result. If <code>status.
	 *  isOK() </code> returns <code>true</code> then the resources are in sync
	 */
	public static IStatus checkInSync(IResource[] resources) {
		IStatus result= null;
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if (!resource.isSynchronized(IResource.DEPTH_INFINITE)) {
				result= addOutOfSync(result, resource);
			}
		}
		if (result != null)
			return result;
		return Status.OK_STATUS;
	}
	private static IStatus addOutOfSync(IStatus status, IResource resource) {
		IStatus entry= new Status(
			IStatus.ERROR,
			ResourcesPlugin.PI_RESOURCES,
			IResourceStatus.OUT_OF_SYNC_LOCAL,
			Messages.format(RefactoringCoreMessages.Resources_outOfSync, BasicElementLabels.getPathLabel(resource.getFullPath(), false)),
			null);
		if (status == null) {
			return entry;
		} else if (status.isMultiStatus()) {
			((MultiStatus)status).add(entry);
			return status;
		} else {
			MultiStatus result= new MultiStatus(
				ResourcesPlugin.PI_RESOURCES,
				IResourceStatus.OUT_OF_SYNC_LOCAL,
				RefactoringCoreMessages.Resources_outOfSyncResources, null);
			result.add(status);
			result.add(entry);
			return result;
		}
	}
}
