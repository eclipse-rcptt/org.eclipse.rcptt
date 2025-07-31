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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7Model;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.internal.core.model.deltas.DeltaProcessor;
import org.eclipse.rcptt.internal.core.model.deltas.Q7ElementDelta;
import org.osgi.framework.FrameworkUtil;

@SuppressWarnings("rawtypes")
public abstract class Q7Operation implements IWorkspaceRunnable,
		IProgressMonitor {
	protected interface IPostAction {
		/*
		 * Returns the id of this action.
		 *
		 * @see ModelOperation#postAction
		 */
		String getID();

		/*
		 * Run this action.
		 */
		void run() throws ModelException;
	}

	/*
	 * Constants controlling the insertion mode of an action.
	 *
	 * @see ModelOperation#postAction
	 */
	protected static final int APPEND = 1; // insert at the end
	protected static final int REMOVEALL_APPEND = 2; // remove all existing
	// ones with same ID,
	// and add new one at
	// the end
	protected static final int KEEP_EXISTING = 3; // do not insert if already
	// existing with same ID
	/*
	 * Whether tracing post actions is enabled.
	 */
	protected static boolean POST_ACTION_VERBOSE;
	public static final String HAS_MODIFIED_RESOURCE_ATTR = "hasModifiedResource"; //$NON-NLS-1$
	public static final String TRUE = "true"; //$NON-NLS-1$
	/**
	 * The elements created by this operation - empty until the operation
	 * actually creates elements.
	 */
	/*
	 * A per thread stack of model operations (PerThreadObject of ArrayList).
	 */
	protected static ThreadLocal operationStacks = new ThreadLocal();
	/*
	 * A list of IPostActions.
	 */
	protected IPostAction[] actions;
	protected int actionsStart = 0;
	protected int actionsEnd = -1;
	/*
	 * A HashMap of attributes that can be used by operations
	 */
	protected HashMap attributes;
	/**
	 * The elements this operation operates on, or <code>null</code> if this
	 * operation does not operate on specific elements.
	 */
	protected IQ7Element[] elementsToProcess;

	/**
	 * The parent elements this operation operates with or <code>null</code> if
	 * this operation does not operate with specific parent elements.
	 */
	protected IQ7Element[] parentElements;
	protected IQ7Element[] resultElements = Q7Element.NO_ELEMENTS;
	/**
	 * The progress monitor passed into this operation
	 */
	protected IProgressMonitor progressMonitor = null;
	/**
	 * Conflict resolution policy - by default do not force (fail on a
	 * conflict).
	 */
	protected boolean force = false;
	/**
	 * A flag indicating whether this operation is nested.
	 */
	protected boolean isNested = false;

	protected Q7Operation() {
		// default constructor used in subclasses
	}

	protected Q7Operation(IQ7Element element) {
		this.elementsToProcess = new IQ7Element[] { element };
	}

	/**
	 * A common constructor for all Model operations.
	 */
	protected Q7Operation(IQ7Element[] elements) {
		this.elementsToProcess = elements;
	}

	/**
	 * A common constructor for all Model operations.
	 */
	protected Q7Operation(IQ7Element[] elements, boolean force) {
		this.elementsToProcess = elements;
		this.force = force;
	}

	/**
	 * A common constructor for all q7 Model operations.
	 */
	protected Q7Operation(IQ7Element[] elementsToProcess,
			IQ7Element[] parentElements, boolean force) {
		this.elementsToProcess = elementsToProcess;
		this.parentElements = parentElements;
		this.force = force;
	}

	/*
	 * Registers the given action at the end of the list of actions to run.
	 */
	protected void addAction(IPostAction action) {
		int length = this.actions.length;
		if (length == ++this.actionsEnd) {
			System.arraycopy(this.actions, 0,
					this.actions = new IPostAction[length * 2], 0, length);
		}
		this.actions[this.actionsEnd] = action;
	}

	/*
	 * Registers the given delta with the q7 Model Manager.
	 */
	protected void addDelta(IQ7ElementDelta delta) {
		ModelManager.getModelManager().getDeltaProcessor()
				.registerModelDelta(delta);
	}

	/*
	 * Registers the given reconcile delta with the q7 Model Manager.
	 */
	@SuppressWarnings("unchecked")
	protected void addReconcileDelta(IQ7NamedElement workingCopy,
			IQ7ElementDelta delta) {
		HashMap reconcileDeltas = ModelManager.getModelManager()
				.getDeltaProcessor().reconcileDeltas;
		Q7ElementDelta previousDelta = (Q7ElementDelta) reconcileDeltas
				.get(workingCopy);
		if (previousDelta != null) {
			IQ7ElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				Q7ElementDelta child = (Q7ElementDelta) children[i];
				previousDelta.insertDeltaTree(child.getElement(), child);
			}
		} else {
			reconcileDeltas.put(workingCopy, delta);
		}
	}

	public void beginTask(String name, int totalWork) {
		if (progressMonitor != null) {
			progressMonitor.beginTask(name, totalWork);
		}
	}

	protected void checkCanceled() {
		if (isCanceled()) {
			throw new OperationCanceledException("Operation canceled");
		}
	}

	/**
	 * Common code used to verify the elements this operation is processing.
	 *
	 * @see Q7Operation#verify()
	 */
	protected Q7Status commonVerify() {
		if (elementsToProcess == null || elementsToProcess.length == 0) {
			return new Q7Status(0, "No elements to proceed");
		}
		for (int i = 0; i < elementsToProcess.length; i++) {
			if (elementsToProcess[i] == null) {
				return new Q7Status(0, "No elements to proceed");
			}
		}
		return Q7Status.OK;
	}

	/**
	 * Convenience method to copy resources
	 */
	protected void copyResources(IResource[] resources, IPath destinationPath)
			throws ModelException {
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.copy(resources, destinationPath, false,
					subProgressMonitor);
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	/**
	 * Convenience method to create a file
	 */
	protected void createFile(IContainer folder, String name,
			InputStream contents, boolean forceFlag) throws ModelException {
		IFile file = folder.getFile(new Path(name));
		try {
			file.create(contents, forceFlag ? IResource.FORCE
					| IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
					getSubProgressMonitor(1));
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	/**
	 * Convenience method to create a folder
	 */
	protected void createFolder(IContainer parentFolder, String name,
			boolean forceFlag) throws ModelException {
		IFolder folder = parentFolder.getFolder(new Path(name));
		try {
			// we should use true to create the file locally. Only VCM should
			// use tru/false
			folder.create(forceFlag ? IResource.FORCE | IResource.KEEP_HISTORY
					: IResource.KEEP_HISTORY, true, // local
					getSubProgressMonitor(1));
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	protected void deleteEmptyQ7Folder(IQ7Folder fragment,
			boolean forceFlag, IResource rootResource) throws ModelException {
		IContainer resource = (IContainer) fragment.getResource();
		try {
			resource.delete(forceFlag ? IResource.FORCE
					| IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
					getSubProgressMonitor(1));
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
			while (resource instanceof IFolder) {
				// deleting a package: delete the parent if it is empty (eg.
				// deleting x.y where folder x doesn't have resources but y)
				// without deleting the package fragment root
				resource = resource.getParent();
				if (!resource.equals(rootResource)
						&& resource.members().length == 0) {
					resource.delete(forceFlag ? IResource.FORCE
							| IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
							getSubProgressMonitor(1));
					this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
				}
			}
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	protected void deleteResource(IResource resource, int flags)
			throws ModelException {
		try {
			resource.delete(flags, getSubProgressMonitor(1));
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	/**
	 * Convenience method to delete resources
	 */
	protected void deleteResources(IResource[] resources, boolean forceFlag)
			throws ModelException {
		if (resources == null || resources.length == 0)
			return;
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(resources.length);
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.delete(resources, forceFlag ? IResource.FORCE
					| IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
					subProgressMonitor);
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	public void done() {
		if (progressMonitor != null) {
			progressMonitor.done();
		}
	}

	protected boolean equalsOneOf(IPath path, IPath[] otherPaths) {
		for (int i = 0, length = otherPaths.length; i < length; i++) {
			if (path.equals(otherPaths[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Convenience method to run an operation within this operation
	 */
	public void executeNestedOperation(Q7Operation operation, int subWorkAmount)
			throws ModelException {
		Q7Status status = operation.verify();
		if (!status.isOK()) {
			throw new ModelException(status);
		}
		IProgressMonitor subProgressMonitor = getSubProgressMonitor(subWorkAmount);
		// fix for 1FW7IKC, part (1)
		try {
			operation.setNested(true);
			operation.run(subProgressMonitor);
		} catch (CoreException ce) {
			if (ce instanceof ModelException) {
				throw (ModelException) ce;
			} else {
				// translate the core exception to q7 model exception
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e = ce.getStatus().getException();
					if (e instanceof ModelException) {
						throw (ModelException) e;
					}
				}
				throw new ModelException(ce);
			}
		}
	}

	/**
	 * Performs the operation specific behavior. Subclasses must override.
	 * @throws InterruptedException 
	 */
	protected abstract void executeOperation() throws ModelException, InterruptedException;

	/*
	 * Returns the index of the first registered action with the given id,
	 * starting from a given position. Returns -1 if not found.
	 */
	protected int firstActionWithID(String id, int start) {
		for (int i = start; i <= this.actionsEnd; i++) {
			if (this.actions[i].getID().equals(id)) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * Returns the attribute registered at the given key with the top level
	 * operation. Returns null if no such attribute is found.
	 */
	protected Object getAttribute(Object key) {
		ArrayList stack = this.getCurrentOperationStack();
		if (stack.size() == 0)
			return null;
		Q7Operation topLevelOp = (Q7Operation) stack.get(0);
		if (topLevelOp.attributes == null) {
			return null;
		} else {
			return topLevelOp.attributes.get(key);
		}
	}

	/*
	 * Returns the stack of operations running in the current thread. Returns an
	 * empty stack if no operations are currently running in this thread.
	 */
	@SuppressWarnings("unchecked")
	protected ArrayList getCurrentOperationStack() {
		ArrayList stack = (ArrayList) operationStacks.get();
		if (stack == null) {
			stack = new ArrayList();
			operationStacks.set(stack);
		}
		return stack;
	}

	/**
	 * Returns the element to which this operation applies, or <code>null</code>
	 * if not applicable.
	 */
	protected IQ7Element getElementToProcess() {
		if (elementsToProcess == null || elementsToProcess.length == 0) {
			return null;
		}
		return elementsToProcess[0];
	}

	/**
	 * Returns the Model this operation is operating in.
	 */
	public IQ7Model getModel() {
		if (elementsToProcess == null || elementsToProcess.length == 0) {
			return getParentElement().getModel();
		} else {
			return elementsToProcess[0].getModel();
		}
	}

	/**
	 * Returns the parent element to which this operation applies, or
	 * <code>null</code> if not applicable.
	 */
	protected IQ7Element getParentElement() {
		if (parentElements == null || parentElements.length == 0) {
			return null;
		}
		return parentElements[0];
	}

	/**
	 * Returns the parent elements to which this operation applies, or
	 * <code>null</code> if not applicable.
	 */
	protected IQ7Element[] getParentElements() {
		return parentElements;
	}

	/**
	 * Returns the elements created by this operation.
	 */
	public IQ7Element[] getResultElements() {
		return resultElements;
	}

	/*
	 * Returns the scheduling rule for this operation (i.e. the resource that
	 * needs to be locked while this operation is running. Subclasses can
	 * override.
	 */
	protected ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Creates and returns a subprogress monitor if appropriate.
	 */
	protected IProgressMonitor getSubProgressMonitor(int workAmount) {
		IProgressMonitor sub = null;
		if (progressMonitor != null) {
			sub = new SubProgressMonitor(progressMonitor, workAmount,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		}
		return sub;
	}

	/**
	 * Returns whether this operation has performed any resource modifications.
	 * Returns false if this operation has not been executed yet.
	 */
	public boolean hasModifiedResource() {
		return !this.isReadOnly()
				&& this.getAttribute(HAS_MODIFIED_RESOURCE_ATTR) == TRUE;
	}

	public void internalWorked(double work) {
		if (progressMonitor != null) {
			progressMonitor.internalWorked(work);
		}
	}

	public boolean isCanceled() {
		if (progressMonitor != null) {
			return progressMonitor.isCanceled();
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if this operation performs no resource
	 * modifications, otherwise <code>false</code>. Subclasses must override.
	 */
	public boolean isReadOnly() {
		return false;
	}

	/*
	 * Returns whether this operation is the first operation to run in the
	 * current thread.
	 */
	protected boolean isTopLevelOperation() {
		ArrayList stack;
		return (stack = this.getCurrentOperationStack()).size() > 0
				&& stack.get(0) == this;
	}

	/**
	 * Convenience method to move resources
	 */
	protected void moveResources(IResource[] resources, IPath destinationPath)
			throws ModelException {
		IProgressMonitor subProgressMonitor = null;
		if (progressMonitor != null) {
			subProgressMonitor = new SubProgressMonitor(progressMonitor,
					resources.length,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		}
		IWorkspace workspace = resources[0].getWorkspace();
		try {
			workspace.move(resources, destinationPath, false,
					subProgressMonitor);
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	/**
	 * Creates and returns a new <code>IQ7ElementDelta</code> on the model.
	 */
	public Q7ElementDelta newModelElementDelta() {
		return new Q7ElementDelta(getModel());
	}

	/*
	 * Removes the last pushed operation from the stack of running operations.
	 * Returns the poped operation or null if the stack was empty.
	 */
	@SuppressWarnings("unchecked")
	protected Q7Operation popOperation() {
		ArrayList stack = getCurrentOperationStack();
		int size = stack.size();
		if (size > 0) {
			if (size == 1) { // top level operation
				operationStacks.set(null); // release reference (see
				// http://bugs.eclipse.org/bugs/show_bug.cgi?id=33927)
			}
			return (Q7Operation) stack.remove(size - 1);
		} else {
			return null;
		}
	}

	protected void postAction(IPostAction action, int insertionMode) {
		if (POST_ACTION_VERBOSE) {
			System.out
					.print("(" + Thread.currentThread() //$NON-NLS-1$
							+ ") [ModelOperation.postAction(IPostAction, int)] Posting action " + action.getID()); //$NON-NLS-1$
			switch (insertionMode) {
			case REMOVEALL_APPEND:
				System.out.println(" (REMOVEALL_APPEND)"); //$NON-NLS-1$
				break;
			case KEEP_EXISTING:
				System.out.println(" (KEEP_EXISTING)"); //$NON-NLS-1$
				break;
			case APPEND:
				System.out.println(" (APPEND)"); //$NON-NLS-1$
				break;
			}
		}
		Q7Operation topLevelOp = (Q7Operation) getCurrentOperationStack()
				.get(0);
		IPostAction[] postActions = topLevelOp.actions;
		if (postActions == null) {
			topLevelOp.actions = postActions = new IPostAction[1];
			postActions[0] = action;
			topLevelOp.actionsEnd = 0;
		} else {
			String id = action.getID();
			switch (insertionMode) {
			case REMOVEALL_APPEND:
				int index = this.actionsStart - 1;
				while ((index = topLevelOp.firstActionWithID(id, index + 1)) >= 0) {
					// remove action[index]
					System.arraycopy(postActions, index + 1, postActions,
							index, topLevelOp.actionsEnd - index);
					postActions[topLevelOp.actionsEnd--] = null;
				}
				topLevelOp.addAction(action);
				break;
			case KEEP_EXISTING:
				if (topLevelOp.firstActionWithID(id, 0) < 0) {
					topLevelOp.addAction(action);
				}
				break;
			case APPEND:
				topLevelOp.addAction(action);
				break;
			}
		}
	}

	protected boolean prefixesOneOf(IPath path, IPath[] otherPaths) {
		for (int i = 0, length = otherPaths.length; i < length; i++) {
			if (path.isPrefixOf(otherPaths[i])) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	protected void pushOperation(Q7Operation operation) {
		getCurrentOperationStack().add(operation);
	}

	protected void removeAllPostAction(String actionID) {
		if (POST_ACTION_VERBOSE) {
			System.out
					.println("(" + Thread.currentThread() //$NON-NLS-1$
							+ ") [ModelOperation.removeAllPostAction(String)] Removing actions " + actionID); //$NON-NLS-1$
		}
		Q7Operation topLevelOp = (Q7Operation) getCurrentOperationStack()
				.get(0);
		IPostAction[] postActions = topLevelOp.actions;
		if (postActions == null)
			return;
		int index = this.actionsStart - 1;
		while ((index = topLevelOp.firstActionWithID(actionID, index + 1)) >= 0) {
			// remove action[index]
			System.arraycopy(postActions, index + 1, postActions, index,
					topLevelOp.actionsEnd - index);
			postActions[topLevelOp.actionsEnd--] = null;
		}
	}

	/*
	 * Deregister the reconcile delta for the given working copy
	 */
	protected void removeReconcileDelta(IQ7NamedElement workingCopy) {
		ModelManager.getModelManager().getDeltaProcessor().reconcileDeltas
				.remove(workingCopy);
	}

	/**
	 * Runs this operation and registers any deltas created.
	 *
	 * @see IWorkspaceRunnable
	 * @exception CoreException
	 *                if the operation fails
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		ModelManager manager = ModelManager.getModelManager();
		DeltaProcessor deltaProcessor = manager.getDeltaProcessor();
		int previousDeltaCount = deltaProcessor.modelDeltas.size();
		try {
			progressMonitor = monitor;
			pushOperation(this);
			try {
				executeOperation();
			} catch (InterruptedException e) {
				throw new CoreException(error(e));
			} finally {
				if (this.isTopLevelOperation()) {
					this.runPostActions();
				}
			}
		} finally {
			try {
				// reacquire delta processor as it can have been reset during
				// executeOperation()
				deltaProcessor = manager.getDeltaProcessor();
				// update Model using deltas that were recorded during this
				// operation
				for (int i = previousDeltaCount, size = deltaProcessor.modelDeltas
						.size(); i < size; i++) {
					deltaProcessor.updateModel(deltaProcessor.modelDeltas
							.get(i));
				}
				// close the parents of the created elements and reset their
				// project's cache (in case we are in an
				// IWorkspaceRunnable and the clients wants to use the created
				// element's parent)
				for (int i = 0, length = this.resultElements.length; i < length; i++) {
					IQ7Element element = this.resultElements[i];
					Openable openable = (Openable) element.getOpenable();
					if (!(openable instanceof Q7NamedElement)
							|| !((Q7NamedElement) openable).isWorkingCopy()) {
						/*
						 * a working copy must remain a child of its parent even
						 * after a move
						 */
						((Q7NamedElement) openable.getParent()).close();
					}
					switch (element.getElementType()) {
					case Folder:
						((Q7Project) element.getQ7Project()).resetCaches();
						break;
					}
				}
				// fire only iff:
				// - the operation is a top level operation
				// - the operation did produce some delta(s)
				// - but the operation has not modified any resource
				if (this.isTopLevelOperation()) {
					if ((deltaProcessor.modelDeltas.size() > previousDeltaCount || !deltaProcessor.reconcileDeltas
							.isEmpty()) && !this.hasModifiedResource()) {
						deltaProcessor.fire(null,
								DeltaProcessor.DEFAULT_CHANGE_EVENT);
					} // else deltas are fired while processing the resource
						// delta
				}
			} catch (InterruptedException e) {
				throw new CoreException(error(e));
			} finally {
				popOperation();
			}
		}
	}

	private  final IStatus error(InterruptedException e) {
		return new Status(IStatus.CANCEL, FrameworkUtil.getBundle(getClass()).getSymbolicName(), 0, "Interrupted", e);
	}

	/**
	 * Main entry point for Model operations. Runs a Model Operation as an
	 * IWorkspaceRunnable if not read-only.
	 */
	public void runOperation(IProgressMonitor monitor) throws ModelException {
		Q7Status status = verify();
		if (!status.isOK()) {
			throw new ModelException(status);
		}
		try {
			if (isReadOnly()) {
				run(monitor);
			} else {
				// Use IWorkspace.run(...) to ensure that a build will be done
				// in autobuild mode.
				// Note that if the tree is locked, this will throw a
				// CoreException, but this is ok
				// as this operation is modifying the tree (not read-only) and a
				// CoreException will be thrown anyway.
				IWorkspace wc = ResourcesPlugin.getWorkspace();
				wc.run(this, getSchedulingRule(), IWorkspace.AVOID_UPDATE,
						monitor);
			}
		} catch (CoreException ce) {
			if (ce instanceof ModelException) {
				throw (ModelException) ce;
			} else {
				if (ce.getStatus().getCode() == IResourceStatus.OPERATION_FAILED) {
					Throwable e = ce.getStatus().getException();
					if (e instanceof ModelException) {
						throw (ModelException) e;
					}
				}
				throw new ModelException(ce);
			}
		}
	}

	protected void runPostActions() throws ModelException {
		while (this.actionsStart <= this.actionsEnd) {
			IPostAction postAction = this.actions[this.actionsStart++];
			if (POST_ACTION_VERBOSE) {
				System.out
						.println("(" + Thread.currentThread() + ") [ModelOperation.runPostActions()] Running action " //$NON-NLS-1$ //$NON-NLS-2$
								+ postAction.getID());
			}
			postAction.run();
		}
	}

	@SuppressWarnings("unchecked")
	protected void setAttribute(Object key, Object attribute) {
		Q7Operation topLevelOp = (Q7Operation) this.getCurrentOperationStack()
				.get(0);
		if (topLevelOp.attributes == null) {
			topLevelOp.attributes = new HashMap();
		}
		topLevelOp.attributes.put(key, attribute);
	}

	public void setCanceled(boolean value) {
		if (progressMonitor != null) {
			progressMonitor.setCanceled(value);
		}
	}

	/**
	 * Sets whether this operation is nested or not.
	 *
	 * @see CreateElementInCUOperation#checkCanceled
	 */
	protected void setNested(boolean nested) {
		isNested = nested;
	}

	public void setTaskName(String name) {
		if (progressMonitor != null) {
			progressMonitor.setTaskName(name);
		}
	}

	public void subTask(String name) {
		if (progressMonitor != null) {
			progressMonitor.subTask(name);
		}
	}

	/**
	 * Returns a status indicating if there is any known reason this operation
	 * will fail. Operations are verified before they are run.
	 *
	 * Subclasses must override if they have any conditions to verify before
	 * this operation executes.
	 *
	 * @see IModelStatus
	 */
	protected Q7Status verify() {
		return commonVerify();
	}

	public void worked(int work) {
		if (progressMonitor != null) {
			progressMonitor.worked(work);
			checkCanceled();
		}
	}
}
