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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rcptt.core.model.IOpenable;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.ModelException;

public abstract class Openable extends Q7Element implements IOpenable {

	protected Openable(Q7Element parent) throws IllegalArgumentException {
		super(parent);
	}

	protected abstract boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm,
			IResource underlyingResource) throws ModelException;

	protected void openParent(Object childInfo, IProgressMonitor pm)
			throws ModelException {

		Openable openableParent = (Openable) getOpenableParent();
		if (openableParent != null && !openableParent.isOpen()) {
			openableParent.generateInfos(openableParent.createElementInfo(), pm);
		}
	}

	protected boolean parentExists() {
		IQ7Element parentElement = getParent();
		if (parentElement == null)
			return true;
		return parentElement.exists();
	}

	protected void generateInfos(Object info, IProgressMonitor monitor)
			throws ModelException {
		// open the parent if necessary
		openParent(info, monitor);
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();


		// build the structure of the openable (this will open the buffer if
		// needed)
		OpenableElementInfo openableElementInfo = (OpenableElementInfo) info;
		boolean isStructureKnown = buildStructure(openableElementInfo,
				monitor, getResource());
		openableElementInfo.setIsStructureKnown(isStructureKnown);
	}

	protected void closing(Object info) throws ModelException {
	}

	@Override
	public OpenableElementInfo getElementInfo() {
		return (OpenableElementInfo) super.getElementInfo();
	}
	
	public boolean isOpen() {
		return getElementInfo().isStructureKnown();
	}

	protected boolean resourceExists() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null)
			return false;
		return Q7Model.getTarget(workspace.getRoot(), this.getPath()
				.makeRelative(), true) != null;
	}
	public boolean isStructureKnown() throws ModelException {
		return ((OpenableElementInfo) getElementInfo()).isStructureKnown();
	}
	@Override
	public IOpenable getOpenable() {
		return this;
	}
}
