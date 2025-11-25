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
import java.util.Optional;
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
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.model.Q7Status.Q7StatusCode;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.ModelManager.PerWorkingCopyInfo;

public abstract class Q7NamedElement extends Openable implements
		IQ7NamedElement {
	private static final IJobManager JOB_MANAGER = Job.getJobManager();


	protected String name;
	protected boolean workingCopyMode = false;
	protected boolean indexing = false;

	public Q7NamedElement(Q7Element parent, String name)
			throws IllegalArgumentException {
		super(parent);
		// Parent can be null on agent
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
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
		IFile resource = (IFile) getResource();
		assert underlyingResource.equals(resource);
		checkCurrentRule();
		
		if (!isInWorkingCopyMode()) {
			if (!resource.isAccessible()) {
				throw newNotPresentException();
			}
		}

		Q7ResourceInfo resourceInfo = (Q7ResourceInfo) info;
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
	public <V> V openAndAccessInfo(ThrowingFunction<OpenableElementInfo, V> infoToValue, IProgressMonitor monitor)
			throws InterruptedException, ModelException {
		checkCurrentRule();
		IFile resource = getResource();
		try {
			Optional<V> result = accessInfoIfOpened(info -> {
				try {
					return infoToValue.apply(info);
				} catch (ModelException e) {
					throw new IllegalStateException(e);
				}
			});
			if (result.isPresent()) {
				return result.get();
			}
		} catch (IllegalStateException e) {
			if (e.getCause() instanceof ModelException) {
				throw (ModelException)e.getCause();
			}
			throw e;
		}
		try {
			JOB_MANAGER.beginRule(resource, monitor);
			return super.openAndAccessInfo(infoToValue, monitor);
		} finally {
			JOB_MANAGER.endRule(resource);
		}
	}

	@Override
	public IPath getPath() {
		return getParent().getPath().append(getName());
	}

	@Override
	public String getID() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getId());
	}

	@Override
	public String getElementName() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getName());
	}

	@Override
	public String getDescription() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getDescription());
	}

	@Override
	public String getVersion() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getVersion());
	}

	@Override
	public String getTags() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement().getTags());
	}

	public NamedElement getMeta() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement());
	}

	@Override
	public boolean isWorkingCopy() {
		return getPerWorkingCopyInfo() != null && workingCopyMode;
	}

	private ModelManager.PerWorkingCopyInfo getPerWorkingCopyInfo() {
		return ModelManager.getModelManager().getPerWorkingCopyInfo(this,
				false, false);
	}

	@Override
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

	@Override
	public IQ7NamedElement getIndexingWorkingCopy(IProgressMonitor monitor)
			throws ModelException {
		return internalGetWorkingCopy(monitor, true);
	}

	@Override
	public IQ7NamedElement getWorkingCopy(IProgressMonitor monitor)
			throws ModelException {
		return internalGetWorkingCopy(monitor, false);
	}

	public IQ7NamedElement internalGetWorkingCopy(IProgressMonitor monitor,
			boolean indexing) throws ModelException {
		ISchedulingRule rule = getResource().getWorkspace().getRuleFactory().refreshRule(getResource());
		IJobManager jobManager = Job.getJobManager();
		jobManager.beginRule(rule, monitor);
		try {
		ModelManager manager = ModelManager.getModelManager();

		Q7NamedElement workingCopy = createWorkingCopy();
		workingCopy.workingCopyMode = true;
		workingCopy.setIndexing(indexing);
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
		} finally {
			jobManager.endRule(rule);
		}
	}

	protected abstract Q7NamedElement createWorkingCopy();

	public void extractAllPersistence() throws ModelException {
		accessResourceInfo(info -> {	
			info.extractAllPersistence();
			return null;
		});
	}

	@Override
	public void commitWorkingCopy(boolean force, IProgressMonitor monitor)
			throws ModelException {
		CommitWorkingCopyOperation op = new CommitWorkingCopyOperation(this,
				force);
		op.runOperation(monitor);
	}

	@Override
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

	@Override
	public NamedElement getNamedElement() throws ModelException {
		return accessResourceInfo(info -> info.getNamedElement());
	}

	@Override
	public NamedElement getModifiedNamedElement() throws ModelException {
		PerWorkingCopyInfo info = getPerWorkingCopyInfo();
		if (info != null) {
			return info.getResourceInfo().getNamedElement();
		}
		return getNamedElement();
	}

	@Override
	public IPersistenceModel getModifiedPersistenceModel()
			throws ModelException {
		PerWorkingCopyInfo info = getPerWorkingCopyInfo();
		if (info != null) {
			return info.getResourceInfo().getPersistenceModel();
		}
		return getPersistenceModel();
	}

	@Override
	public IPersistenceModel getPersistenceModel() throws ModelException {
		return accessResourceInfo( info -> info.getModel());
	}

	// modifications
	@Override
	public void setDescription(String description) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setDescription(description));
	}

	@Override
	public void setElementName(String name) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setName(name));
	}

	@Override
	public void setID(String id) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setId(id));
	}

	@Override
	public void setVersion(String version) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setVersion(version));
	}

	@Override
	public void setTags(String tags) throws ModelException {
		writeWorkingCopy(info -> info.getNamedElement().setTags(tags));
	}

	@Override
	public boolean hasUnsavedChanges() throws ModelException {
		if (isWorkingCopy()) {
			return getPerWorkingCopyInfo().hasChanges();
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
		write.accept(info.getResourceInfo());
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
				Q7Status status = new Q7Status(Q7Status.ERROR, "Resource: " + getResource()
					+ " is locked and can not be synchronized. Wait for indexing and build to complete and try a again.");
				status.setStatusCode(Q7StatusCode.NotPressent);
				throw new ModelException(status);
			}

			if (isInWorkingCopyMode()) {
				PerWorkingCopyInfo info = getPerWorkingCopyInfo();
				if (info != null) {
					return infoToValue.apply(info.getResourceInfo());
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
		if (obj instanceof Q7NamedElement) {
			Q7NamedElement that = (Q7NamedElement) obj;
			return super.equals(that) && indexing == that.indexing;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Util.combineHashCodes(super.hashCode(), indexing ? 1 : 0);
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
	
	private void checkCurrentRule() {
		IFile resource = getResource();
		ISchedulingRule currentRule = JOB_MANAGER.currentRule();
		if (currentRule != null && !currentRule.contains(resource)) {
			throw new IllegalStateException("Current rule " + currentRule + " conflicts with the requested " + resource);
		}
	}
}