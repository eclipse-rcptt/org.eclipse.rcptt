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
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.ModelManager.PerWorkingCopyInfo;

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
	protected Q7ResourceInfo createElementInfo() {
		return new Q7ResourceInfo(IPlainConstants.PLAIN_HEADER, Q7ResourceInfo.toURI(getResource()));
	}

	protected abstract NamedElement createNamedElement();

	@Override
	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm,
			IResource underlyingResource) throws ModelException {
		// Check if not working copy
		if (!isInWorkingCopyMode()) {
			if (!underlyingResource.isAccessible()) {
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

	public IPath getPath() {
		return getParent().getPath().append(getName());
	}

	public String getID() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getId());
	}

	public String getElementName() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getName());
	}

	public String getDescription() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getDescription());
	}

	public String getVersion() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getVersion());
	}

	public String getTags() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getTags());
	}

	public NamedElement getMeta() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement());
	}

	public boolean isWorkingCopy() {
		return getPerWorkingCopyInfo() != null && workingCopyMode;
	}

	private ModelManager.PerWorkingCopyInfo getPerWorkingCopyInfo() {
		return ModelManager.getModelManager().getPerWorkingCopyInfo(this,
				false, false);
	}

	public boolean hasResourceChanged() throws ModelException {
		if (!isWorkingCopy()) {
			return false;
		}
		try {
			return accessInfoIfOpened(info -> ((Q7ResourceInfo) info).timestamp != getResource()
					.getModificationStamp()).orElse(false);
		} catch (InterruptedException e) {
			throw new ModelException(e, 0);
		}
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
		accessResourceInfo(info -> {	
			info.extractAllPersistence();
			return null;
		});
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
		try {
			accessInfoIfOpened(info -> {
				((Q7ResourceInfo)info).timestamp = timeStamp;
				return null;
			});
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ModelException(e, 0);
		}
	}

	public NamedElement getNamedElement() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement());
	}

	public NamedElement getModifiedNamedElement() throws ModelException {
		PerWorkingCopyInfo info = getPerWorkingCopyInfo();
		if (info != null) {
			return info.resourceInfo.getNamedElement();
		}
		return getNamedElement();
	}

	public IPersistenceModel getModifiedPersistenceModel()
			throws ModelException {
		PerWorkingCopyInfo info = getPerWorkingCopyInfo();
		if (info != null) {
			return info.resourceInfo.getPersistenceModel();
		}
		return getPersistenceModel();
	}

	public IPersistenceModel getPersistenceModel() throws ModelException {
		return accessResourceInfo( info -> info.getModel());
	}

	// modifications
	public void setDescription(String description) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setDescription(description));
	}

	public void setElementName(String name) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setName(name));
	}

	public void setID(String id) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setId(id));
	}

	public void setVersion(String version) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setVersion(version));
	}

	public void setTags(String tags) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setTags(tags));
	}

	public boolean hasUnsavedChanges() throws ModelException {
		Q7ResourceInfo info = null;
		if (isWorkingCopy()) {
			info = getPerWorkingCopyInfo().resourceInfo;
			if (info != null)
				return info.hasChanges();
		}
		try {
			return accessInfoIfOpened(info2 -> ((Q7ResourceInfo)info2).hasChanges()).orElse(false);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ModelException(e, 0);
		}
	}
	
	public final void writeWorkingCopy(Consumer<Q7ResourceInfo> write) {
		if (!isInWorkingCopyMode()) {
			throw new IllegalStateException("This is not a working copy");
		}
		
		PerWorkingCopyInfo info = getPerWorkingCopyInfo();
		if (info == null) {
			throw new IllegalStateException("Working copy is closed");
		}
		write.accept(info.resourceInfo);
	}
	
	public final <V> V accessResourceInfo(Function<Q7ResourceInfo, V> infoToValue) throws ModelException {
		try {
			if (!getResource().getWorkspace().isTreeLocked()) {
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
			}
			if (!getResource().isSynchronized(IResource.DEPTH_INFINITE)) {
				throw newNotPresentException();
			}

			if (isInWorkingCopyMode()) {
				PerWorkingCopyInfo info = getPerWorkingCopyInfo();
				if (info != null) {
					return infoToValue.apply(info.resourceInfo);
				}
			}
			return openAndAccessInfo(info -> {
				Q7ResourceInfo resource = (Q7ResourceInfo) info;
				return infoToValue.apply(resource);
			}, null);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ModelException(e, 0);
		}
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
		writeWorkingCopy(info ->
			info.updatePersistenceModel(newModel)
		);
	}

	public void setIndexing(boolean indexing) {
		this.indexing = indexing;
	}
}