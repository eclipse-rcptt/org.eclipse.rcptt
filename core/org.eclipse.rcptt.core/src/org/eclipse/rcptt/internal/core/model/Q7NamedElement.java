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

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

public abstract class Q7NamedElement extends Openable implements
		IQ7NamedElement {

	protected String name;
	protected boolean workingCopyMode = false;
	protected boolean indexing = false;

	public Q7NamedElement(Q7Element parent, String name)
			throws IllegalArgumentException {
		super(parent);
		// Parent can be null on agent
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public IFile getResource() {
		return ((IContainer) this.getParent().getResource()).getFile(new Path(
				this.getName()));
	}

	@Override
	protected Object createElementInfo() {
		return new Q7ResourceInfo(IPlainConstants.PLAIN_HEADER, Q7ResourceInfo.toURI(getResource()));
	}

	protected abstract NamedElement createNamedElement();

	@Override
	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm, Map<IQ7Element, Object> newElements,
			IResource underlyingResource) throws ModelException {
		// Check if not working copy
		if (!isInWorkingCopyMode()) {
			if (!underlyingResource.isAccessible()) {
				throw newNotPresentException();
			}
		}
		if (!getResource().isSynchronized(IResource.DEPTH_INFINITE)) {

			// refresh, only if this project is not building right now
			if (!ModelManager.getModelManager().isProjectBuilding()
					&& !indexing) {
				try {
					getResource().refreshLocal(IResource.DEPTH_INFINITE,
							new NullProgressMonitor());
				} catch (CoreException e) {
					RcpttPlugin.log(e);
				}
			}
			if (!getResource().isSynchronized(IResource.DEPTH_INFINITE)) {
				throw newNotPresentException();
			}
		}

		Q7ResourceInfo resourceInfo = (Q7ResourceInfo) info;
		IFile resource = (IFile) getResource();
		try {
			resourceInfo.load(resource);
		} catch (Throwable e) {
			e.addSuppressed(new IOException("Reading " + resource));
			throw e;
		}
		if (isInWorkingCopyMode() && !indexing) {
			if (resourceInfo.getNamedElement() == null) {
				resourceInfo.createNamedElement(createNamedElement());
			}
		}
		return true;
	}

	@Override
	protected void closing(Object info) throws ModelException {
		if (info instanceof Q7ResourceInfo) {
			((Q7ResourceInfo) info).unload();
		}
	}

	public IPath getPath() {
		return getParent().getPath().append(getName());
	}

	protected Q7ResourceInfo getInfo() throws ModelException {
		Q7ResourceInfo info = null;
		if (isWorkingCopy()) {
			info = getPerWorkingCopyInfo().resourceInfo;
		}
		if (info == null) {
			info = (Q7ResourceInfo) getElementInfo();
		}
		if (info == null || info.getNamedElement() == null) {
			throw newNotPresentException();
		}
		return info;
	}

	public String getID() throws ModelException {
		return getInfo().getNamedElement().getId();
	}

	public String getElementName() throws ModelException {
		return getInfo().getNamedElement().getName();
	}

	public String getDescription() throws ModelException {
		return getInfo().getNamedElement().getDescription();
	}

	public String getVersion() throws ModelException {
		return getInfo().getNamedElement().getVersion();
	}

	public String getTags() throws ModelException {
		return getInfo().getNamedElement().getTags();
	}

	public NamedElement getMeta() throws ModelException {
		return getInfo().getNamedElement();
	}

	public boolean isWorkingCopy() {
		return getPerWorkingCopyInfo() != null && workingCopyMode;
	}

	public ModelManager.PerWorkingCopyInfo getPerWorkingCopyInfo() {
		return ModelManager.getModelManager().getPerWorkingCopyInfo(this,
				false, false);
	}

	public boolean hasResourceChanged() {
		if (!isWorkingCopy()) {
			return false;
		}
		Object info = ModelManager.getModelManager().getInfo(this);
		if (info == null) {
			return false;
		}

		return ((Q7ResourceInfo) info).timestamp != getResource()
				.getModificationStamp();
	}

	public IQ7NamedElement getIndexingWorkingCopy(IProgressMonitor monitor)
			throws ModelException {
		return internalGetWorkingCopy(monitor, true);
	}

	public IQ7NamedElement getWorkingCopy(IProgressMonitor monitor)
			throws ModelException {
		return internalGetWorkingCopy(monitor, false);
	}

	public IQ7NamedElement internalGetWorkingCopy(IProgressMonitor monitor,
			boolean indexing) throws ModelException {

		ModelManager manager = ModelManager.getModelManager();

		Q7NamedElement workingCopy = createWorkingCopy();
		workingCopy.workingCopyMode = true;
		ModelManager.PerWorkingCopyInfo perWorkingCopyInfo = manager
				.getPerWorkingCopyInfo(workingCopy, false /* don't create */,
						true /* record usage */);
		if (perWorkingCopyInfo != null) {
			return perWorkingCopyInfo.getWorkingCopy(); // return existing
			// handle instead of the
			// one
			// created above
		}

		BecomeWorkingCopyOperation op = new BecomeWorkingCopyOperation(
				workingCopy, indexing);
		op.runOperation(monitor);
		return workingCopy;
	}

	protected abstract Q7NamedElement createWorkingCopy();

	public void extractAllPersistence() throws ModelException {
		getInfo().extractAllPersistence();
	}

	public void commitWorkingCopy(boolean force, IProgressMonitor monitor)
			throws ModelException {
		CommitWorkingCopyOperation op = new CommitWorkingCopyOperation(this,
				force);
		op.runOperation(monitor);
	}

	public void discardWorkingCopy() throws ModelException {
		DiscardWorkingCopyOperation op = new DiscardWorkingCopyOperation(this,
				indexing);
		op.runOperation(null);
	}

	public IQ7NamedElement getPrimary() {
		return ((Q7Folder) getParent()).getNamedElement(getName());
	}

	public void updateTimeStamp(Q7NamedElement primary) throws ModelException {
		long timeStamp = ((IFile) primary.getResource()).getModificationStamp();
		if (timeStamp == IResource.NULL_STAMP) {
			throw new ModelException(new Q7Status(0, "Invalid Resource"));
		}

		getInfo().timestamp = timeStamp;
	}

	public NamedElement getNamedElement() throws ModelException {
		return getInfo().getNamedElement();
	}

	public NamedElement getModifiedNamedElement() throws ModelException {
		if (getPerWorkingCopyInfo() != null) {
			return getPerWorkingCopyInfo().resourceInfo.getNamedElement();
		}
		return getInfo().getNamedElement();
	}

	public IPersistenceModel getModifiedPersistenceModel()
			throws ModelException {
		if (getPerWorkingCopyInfo() != null) {
			return getPerWorkingCopyInfo().resourceInfo.getPersistenceModel();
		}
		return getInfo().getPersistenceModel();
	}

	public IPersistenceModel getPersistenceModel() throws ModelException {
		return getInfo().getModel();
	}

	// modifications
	public void setDescription(String description) throws ModelException {
		if (isWorkingCopy()) {
			getInfo().getNamedElement().setDescription(description);
		}
	}

	public void setElementName(String name) throws ModelException {
		if (isWorkingCopy()) {
			getInfo().getNamedElement().setName(name);
		}
	}

	public void setID(String id) throws ModelException {
		if (isWorkingCopy()) {
			getInfo().getNamedElement().setId(id);
		}
	}

	public void setVersion(String version) throws ModelException {
		if (isWorkingCopy()) {
			getInfo().getNamedElement().setVersion(version);
		}
	}

	public void setTags(String tags) throws ModelException {
		if (isWorkingCopy()) {
			getInfo().getNamedElement().setTags(tags);
		}
	}

	public boolean hasUnsavedChanges() throws ModelException {
		Q7ResourceInfo info = null;
		if (isWorkingCopy()) {
			info = getPerWorkingCopyInfo().resourceInfo;
			if (info != null)
				return info.hasChanges();
		}
		ModelManager manager = ModelManager.getModelManager();
		info = (Q7ResourceInfo) manager.getInfo(this);
		if (info == null)
			return false;
		return info.hasChanges();
	}

	@Override
	protected boolean isInWorkingCopyMode() {
		return workingCopyMode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Q7NamedElement)) {
			return false;
		}

		return super.equals(obj);
	}

	public void updatePersistenceModel(IPersistenceModel newModel)
			throws ModelException {
		getInfo().updatePersistenceModel(newModel);
	}

	public void setIndexing(boolean indexing) {
		this.indexing = indexing;
	}
}