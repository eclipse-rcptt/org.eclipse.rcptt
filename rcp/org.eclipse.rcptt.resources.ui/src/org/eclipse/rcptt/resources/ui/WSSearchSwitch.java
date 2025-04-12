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
package org.eclipse.rcptt.resources.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rcptt.workspace.WSFile;
import org.eclipse.rcptt.workspace.WSFolder;
import org.eclipse.rcptt.workspace.WSProject;
import org.eclipse.rcptt.workspace.WSRoot;
import org.eclipse.rcptt.workspace.util.WorkspaceSwitch;

public class WSSearchSwitch extends WorkspaceSwitch<Boolean> {

	private final String query;
	private final IProgressMonitor monitor;

	public WSSearchSwitch(final String query, final IProgressMonitor monitor) {
		this.query = query.toLowerCase();
		this.monitor = monitor;
	}

	@Override
	public Boolean caseWSFile(WSFile object) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		return object.getName().toLowerCase().contains(query);
	}

	@Override
	public Boolean caseWSFolder(WSFolder object) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		if (object.getName().toLowerCase().contains(query)) {
			return true;
		}

		for (final WSFile file : object.getFiles()) {
			if (caseWSFile(file)) {
				return true;
			}
		}
		for (final WSFolder file : object.getFolders()) {
			if (caseWSFolder(file)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Boolean caseWSProject(WSProject object) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		if (object.getName().toLowerCase().contains(query)) {
			return true;
		}

		for (final WSFile file : object.getFiles()) {
			if (caseWSFile(file)) {
				return true;
			}
		}
		for (final WSFolder file : object.getFolders()) {
			if (caseWSFolder(file)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Boolean caseWSRoot(WSRoot object) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		for (final WSProject project : object.getProjects()) {
			if (caseWSProject(project)) {
				return true;
			}
		}
		return false;
	}

}
