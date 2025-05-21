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
package org.eclipse.rcptt.internal.core.model.deltas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.ModelInfo;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.core.model.Openable;
import org.eclipse.rcptt.internal.core.model.OpenableElementInfo;
import org.eclipse.rcptt.internal.core.model.Q7FolderInfo;
import org.eclipse.rcptt.internal.core.model.Q7Model;
import org.eclipse.rcptt.internal.core.model.Q7NamedElement;
import org.eclipse.rcptt.internal.core.model.Q7Project;
import org.eclipse.rcptt.internal.core.model.Q7ProjectInfo;
import org.eclipse.rcptt.internal.core.model.index.IndexManager;
import org.eclipse.rcptt.internal.core.model.index.ProjectIndexerManager;
import org.osgi.framework.FrameworkUtil;

public class DeltaProcessor {
	public static boolean DEBUG = false;
	public static boolean VERBOSE = false;
	public static final int DEFAULT_CHANGE_EVENT = 0; // must not collide with
	private static final ILog LOG = Platform.getLog(FrameworkUtil.getBundle(DeltaProcessor.class));

	// ElementChangedEvent
	// event masks

	public static long getTimeStamp(IFile file) {
		long lmodif = 0;
		lmodif = file.getModificationStamp();
		return lmodif;
	}

	/*
	 * The global state of delta processing.
	 */
	private final DeltaProcessingState state;
	/*
	 * The q7 model manager
	 */
	ModelManager manager;
	/*
	 * The <code>ModelElementDelta</code> corresponding to the
	 * <code>IResourceDelta</code> being translated.
	 */
	private Q7ElementDelta currentDelta;
	/*
	 * The model element that was last created (see createElement(IResource)).
	 * This is used as a stack of model elements (using getParent() to pop it,
	 * and using the various get(...) to push it.
	 */
	private Openable currentElement;
	/*
	 * Queue of deltas created explicily by the q7 Model that have yet to be
	 * fired.
	 */
	public ArrayList<IQ7ElementDelta> modelDeltas = new ArrayList<IQ7ElementDelta>();
	/*
	 * Queue of reconcile deltas on working copies that have yet to be fired.
	 * This is a table form IWorkingCopy to IQ7ElementDelta
	 */
	@SuppressWarnings("rawtypes")
	public HashMap reconcileDeltas = new HashMap();
	/*
	 * Turns delta firing on/off. By default it is on.
	 */
	private boolean isFiring = true;
	/*
	 * Used to update the Model for <code>IQ7ElementDelta</code>s.
	 */
	private final ModelUpdater modelUpdater = new ModelUpdater();
	/* A set of IDLTKProject whose caches need to be reset */
	private final HashSet<IQ7Project> projectCachesToReset = new HashSet<IQ7Project>();
	/*
	 * A list of IQ7Element used as a scope for external archives refresh during
	 * POST_CHANGE. This is null if no refresh is needed.
	 */
	private HashSet<IQ7Element> refreshedElements;

	/* A set of IDylanProject whose package fragment roots need to be refreshed */
	private final HashSet<IQ7Project> rootsToRefresh = new HashSet<IQ7Project>();
	/** {@link Runnable}s that should be called after model is updated */
	private final ArrayList<Runnable> postActions = new ArrayList<Runnable>();
	/*
	 * Type of event that should be processed no matter what the real event type
	 * is.
	 */
	public int overridenEventType = -1;

	// /*
	// * Map from IProject to BuildpathChange
	// */
	// public HashMap buildpathChanges = new HashMap();

	public DeltaProcessor(DeltaProcessingState state, ModelManager manager) {
		this.state = state;
		this.manager = manager;
	}

	/*
	 * Adds the dependents of the given project to the list of the projects to
	 * update.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addDependentProjects(IQ7Project project, HashMap projectDependencies,
			HashSet result) {
		IQ7Project[] dependents = (IQ7Project[]) projectDependencies.get(project);
		if (dependents == null) {
			return;
		}
		for (int i = 0, length = dependents.length; i < length; i++) {
			IQ7Project dependent = dependents[i];
			if (result.contains(dependent)) {
				continue; // no need to go further as the project is already
			}
			// known
			result.add(dependent);
			this.addDependentProjects(dependent, projectDependencies, result);
		}
	}

	/*
	 * Adds the given element to the list of elements used as a scope for
	 * external jars refresh.
	 */
	public void addForRefresh(IQ7Element element) {
		if (this.refreshedElements == null) {
			this.refreshedElements = new HashSet<IQ7Element>();
		}
		this.refreshedElements.add(element);
	}

	/*
	 * Adds the given child handle to its parent's cache of children.
	 */
	private void addToParentInfo(Openable child) throws InterruptedException {
		Openable parent = (Openable) child.getParent();
		if (parent == null) {
			return;
		}
		parent.accessInfo(info -> {
			OpenableElementInfo openable = (OpenableElementInfo) info;
			if (openable.isStructureKnown()) {
				info.addChild(child);
			}
			return null;
		});
	}

	/*
	 * Adds the given project and its dependents to the list of the roots to
	 * refresh.
	 */
	private void addToRootsToRefreshWithDependents(IQ7Project q7Project) {
		this.rootsToRefresh.add(q7Project);
		this.addDependentProjects(q7Project, this.state.projectDependencies,
				this.rootsToRefresh);
	}

	private void checkProjectsBeingAddedOrRemoved(IResourceDelta delta) throws InterruptedException {
		IResource resource = delta.getResource();
		IResourceDelta[] children = null;

		switch (resource.getType()) {
		case IResource.ROOT:
			// workaround for bug 15168 circular errors not reported
			this.state.getOldProjectNames(); // force list to be computed
			children = delta.getAffectedChildren();
			break;
		case IResource.PROJECT:
			// NB: No need to check project's nature as if the project is not a
			// q7 project:
			// - if the project is added or changed this is a noop for
			// projectsBeingDeleted
			// - if the project is closed, it has already lost its q7 nature
			IProject project = (IProject) resource;
			Q7Project q7Project = (Q7Project) RcpttCore.create(project);
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// remember project and its dependents
				this.addToRootsToRefreshWithDependents(q7Project);

				// workaround for bug 15168 circular errors not reported
				if (RcpttCore.hasRcpttNature(project)) {
					this.addToParentInfo(q7Project);
				}
				break;

			case IResourceDelta.CHANGED:
				if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
					// project opened or closed: remember project and its
					// dependents
					this.addToRootsToRefreshWithDependents(q7Project);

					// workaround for bug 15168 circular errors not reported
					if (project.isOpen()) {
						if (RcpttCore.hasRcpttNature(project)) {
							this.addToParentInfo(q7Project);
							// readRawBuildpath(q7Project);
							// ensure project references are updated
						}
					} else {
						try {
							q7Project.close();
						} catch (ModelException e) {
							// q7 project doesn't exist: ignore
						}
						this.removeFromParentInfo(q7Project);
					}
				} else if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
					boolean isQ7Project = RcpttCore.hasRcpttNature(project);
					// q7 nature added or removed: remember project and
					// its dependents
					this.addToRootsToRefreshWithDependents(q7Project);

					// workaround for bug 15168 circular errors not reported
					if (isQ7Project) {
						this.addToParentInfo(q7Project);
					} else {
						// close project
						try {
							q7Project.close();
						} catch (ModelException e) {
							// q7 project doesn't exist: ignore
						}
						this.removeFromParentInfo(q7Project);
					}
				} else {
					if (RcpttCore.hasRcpttNature(project)) { // need
						this.addToParentInfo(q7Project);
						children = delta.getAffectedChildren();
					}
				}
				break;

			case IResourceDelta.REMOVED:
				break;
			}

			// in all cases, refresh the external jars for this project
			this.addForRefresh(q7Project);

			break;
		// case IResource.FILE:
		// IFile file = (IFile) resource;
		// break;
		}
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				this.checkProjectsBeingAddedOrRemoved(children[i]);
			}
		}
	}

	private void close(Openable element) {
		try {
			element.close();
		} catch (ModelException e) {
			LOG.log(e.getStatus());
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			OperationCanceledException result = new OperationCanceledException();
			result.initCause(e);
			throw result;
		}
	}

	private void contentChanged(Openable element) {
		boolean isPrimaryWorkingCopy = false;
		if (element.getElementType().equals(HandleType.Context)
				|| element.getElementType().equals(HandleType.Verification)
				|| element.getElementType().equals(HandleType.TestCase)
				|| element.getElementType().equals(HandleType.TestSuite)) {
			Q7NamedElement cu = (Q7NamedElement) element;
			isPrimaryWorkingCopy = cu.isWorkingCopy();
		}
		if (element.getElementType().equals(HandleType.ProjectMetadata)) {
			this.currentDelta().changed(element.getQ7Project(),
					IQ7ElementDelta.F_DESCRIPTION);
		}
		if (isPrimaryWorkingCopy) {
			this.currentDelta().changed(element, IQ7ElementDelta.F_PRIMARY_RESOURCE);
		} else {
			this.currentDelta().changed(element, IQ7ElementDelta.F_CONTENT);
		}
	}

	/*
	 * Creates the openables corresponding to this resource. Returns null if
	 * none was found.
	 */
	private Openable createElement(IResource resource, HandleType elementType,
			Q7Project rootInfo) throws InterruptedException {
		if (resource == null) {
			return null;
		}

		IPath path = resource.getFullPath();
		IQ7Element element = null;
		switch (elementType) {
		case Project:
			// note that non-q7 resources rooted at the project level will
			// also enter this code with
			// an elementType SCRIPT_PROJECT (see #elementType(...)).
			if (resource instanceof IProject) {
				this.popUntilPrefixOf(path);
				if (this.currentElement != null
						&& this.currentElement.getElementType()
								.equals(HandleType.Project)
						&& ((IQ7Project) this.currentElement).getProject().equals(
								resource)) {
					return this.currentElement;
				}
				if (rootInfo != null && rootInfo.getProject().equals(resource)) {
					element = rootInfo;
					break;
				}
				IProject proj = (IProject) resource;
				if (RcpttCore.hasRcpttNature(proj)) {
					element = RcpttCore.create(proj);
				} else {
					element = this.state.findProject(proj.getName());
				}
			}
			break;
		case Folder:
			if (rootInfo != null) {
				if (rootInfo.contains(resource)) {
					// create package handle
					IPath pkgPath = path.removeFirstSegments(rootInfo.getPath()
							.segmentCount());
					element = rootInfo.getFolder(pkgPath);
				}
			} else {
				// find the element that encloses the resource
				this.popUntilPrefixOf(path);
				if (this.currentElement == null) {
					element = RcpttCore.create(resource);
				} else {
					rootInfo = (Q7Project) currentElement.getQ7Project();
					if (((Q7Project) rootInfo.getProject()).contains(resource)) {
						// create package handle
						IPath pkgPath = path.removeFirstSegments(rootInfo.getPath()
								.segmentCount());
						element = rootInfo.getFolder(pkgPath);
					}
				}
			}
			break;
		case TestCase:
		case Context:
		case TestSuite:
		case ProjectMetadata:
		case Verification:
			// find the element that encloses the resource
			this.popUntilPrefixOf(path);
			if (this.currentElement == null) {
				element = rootInfo == null ? RcpttCore.create(resource) : ModelManager
						.create(resource, rootInfo);
			} else {
				// find the package
				IQ7Folder pkgFragment = null;
				switch (this.currentElement.getElementType()) {
				case Folder:
					Openable pkg = this.currentElement;
					if (pkg.getPath().equals(path.removeLastSegments(1))) {
						pkgFragment = (IQ7Folder) pkg;
					} // else case of package x which is a prefix of
						// x.y
					break;
				case TestCase:
				case Context:
				case TestSuite:
				case ProjectMetadata:
				case Verification:
					pkgFragment = (IQ7Folder) this.currentElement.getParent();
					break;
				}
				if (pkgFragment == null) {
					element = rootInfo == null ? RcpttCore.create(resource) : ModelManager
							.create(resource, rootInfo);
				} else {
					if (elementType.equals(HandleType.Context)
							|| elementType.equals(HandleType.Verification)
							|| elementType.equals(HandleType.TestCase)
							|| elementType.equals(HandleType.TestSuite)
							|| elementType.equals(HandleType.TestSuite)) {
						String fileName = path.lastSegment();
						element = pkgFragment.getNamedElement(fileName);
					}
				}
			}
			break;
		}
		if (element == null) {
			return null;
		}
		this.currentElement = (Openable) element;
		return this.currentElement;
	}

	private Q7ElementDelta currentDelta() {
		if (this.currentDelta == null) {
			this.currentDelta = new Q7ElementDelta(this.manager.getModel());
		}
		return this.currentDelta;
	}

	/*
	 * Note that the project is about to be deleted.
	 */
	private void deleting(IProject project) throws InterruptedException {

		try {
			// discard indexing jobs that belong to this project so that the
			// project can be
			// deleted without interferences from the index manager
			this.manager.getIndexManager().discardJobs(project.getName());

			Q7Project q7Project = (Q7Project) RcpttCore.create(project);

			q7Project.close();

			// workaround for bug 15168 circular errors not reported
			this.state.getOldProjectNames(); // foce list to be computed

			this.removeFromParentInfo(q7Project);
		} catch (ModelException e) {
			// q7 project doesn't exist: ignore
		}
	}

	private void elementAdded(Openable element, IResourceDelta delta) throws InterruptedException {
		HandleType elementType = element.getElementType();
		if (elementType.equals(HandleType.Project)) {
			// project add is handled by DylanProject.configure() because
			// when a project is created, it does not yet have a q7 nature
			if (delta != null && RcpttCore.hasRcpttNature((IProject) delta.getResource())) {
				this.addToParentInfo(element);
				if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
					Openable movedFromElement = (Openable) element.getModel().getProject(
							delta.getMovedFromPath().lastSegment());
					this.currentDelta().movedTo(element, movedFromElement);
				} else {
					this.close(element);

					this.currentDelta().added(element);
				}
				final IQ7Project project = (IQ7Project) element;
				this.rootsToRefresh.add(project);
				this.projectCachesToReset.add(project);
			}
		} else {
			if (delta == null || (delta.getFlags() & IResourceDelta.MOVED_FROM) == 0) {
				// regular element addition
				if (this.isWorkingCopy(element, elementType)) {
					this.currentDelta().changed(element,
							IQ7ElementDelta.F_PRIMARY_RESOURCE);
				} else {
					this.addToParentInfo(element);
					this.close(element);
					this.currentDelta().added(element);
				}
			} else {
				// element is moved
				this.addToParentInfo(element);
				this.close(element);
				IPath movedFromPath = delta.getMovedFromPath();
				IResource res = delta.getResource();
				IResource movedFromRes;
				if (res instanceof IFile) {
					movedFromRes = res.getWorkspace().getRoot().getFile(movedFromPath);
				} else {
					movedFromRes = res.getWorkspace().getRoot().getFolder(movedFromPath);
				}
				// find the element type of the moved from element
				Q7Project movedFromInfo = (Q7Project) RcpttCore.create(movedFromRes
						.getProject());
				HandleType movedFromType = element.getParent().getElementType();
				// reset current element as it might be inside a nested root
				// (popUntilPrefixOf() may use the outer root)
				this.currentElement = null;
				// create the moved from element
				Openable movedFromElement = this.createElement(movedFromRes,
						movedFromType, movedFromInfo);
				if (movedFromElement == null) {
					// moved from outside buildpath
					this.currentDelta().added(element);
				} else {
					this.currentDelta().movedTo(element, movedFromElement);
				}
			}
			switch (elementType) {
			case Folder:
				// reset project's package fragment cache
				Q7Project project = (Q7Project) element.getQ7Project();
				this.projectCachesToReset.add(project);
				break;
			}
		}
	}

	/*
	 * Generic processing for a removed element:<ul> <li>Close the element,
	 * removing its structure from the cache <li>Remove the element from its
	 * parent's cache of children <li>Add a REMOVED entry in the delta </ul>
	 * Delta argument could be null if processing an external ZIP change
	 */
	private void elementRemoved(Openable element, IResourceDelta delta, Q7Project rootInfo) throws InterruptedException {
		HandleType elementType = element.getElementType();
		if (delta == null || (delta.getFlags() & IResourceDelta.MOVED_TO) == 0) {
			// regular element removal
			if (this.isWorkingCopy(element, elementType)) {
				// filter out changes to primary compilation unit in working
				// copy mode
				// just report a change to the resource (see
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=59500)
				this.currentDelta().changed(element, IQ7ElementDelta.F_PRIMARY_RESOURCE);
			} else {
				this.close(element);
				this.removeFromParentInfo(element);
				this.currentDelta().removed(element);
			}
		} else {
			// element is moved
			this.close(element);
			this.removeFromParentInfo(element);
			IPath movedToPath = delta.getMovedToPath();
			IResource res = delta.getResource();
			IResource movedToRes;
			switch (res.getType()) {
			case IResource.PROJECT:
				movedToRes = res.getWorkspace().getRoot()
						.getProject(movedToPath.lastSegment());
				break;
			case IResource.FOLDER:
				movedToRes = res.getWorkspace().getRoot().getFolder(movedToPath);
				break;
			case IResource.FILE:
				movedToRes = res.getWorkspace().getRoot().getFile(movedToPath);
				break;
			default:
				return;
			}
			// find the element type of the moved from element
			Q7Project movedToInfo = (Q7Project) RcpttCore.create(movedToRes.getProject());
			HandleType movedToType = element.getElementType();
			// reset current element as it might be inside a nested root
			// (popUntilPrefixOf() may use the outer root)
			this.currentElement = null;
			// create the moved To element
			Openable movedToElement = this.createElement(movedToRes, movedToType,
					movedToInfo);
			if (movedToElement == null) {
				// moved outside buildpath
				this.currentDelta().removed(element);
			} else {
				this.currentDelta().movedFrom(element, movedToElement);
			}
		}
		switch (elementType) {
		case Model:
			this.manager.getIndexManager().reset();
			break;
		case Project: {
			final IQ7Project project = (IQ7Project) element;
			this.rootsToRefresh.add(project);
			this.projectCachesToReset.add(project);
			break;
		}
		case Folder: {
			// reset package fragment cache
			IQ7Project project = element.getQ7Project();
			this.projectCachesToReset.add(project);
			break;
		}
		}
	}

	/*
	 * Flushes all deltas without firing them.
	 */
	public void flush() {
		this.modelDeltas = new ArrayList<IQ7ElementDelta>();
	}

	/*
	 * Fire q7 Model delta, flushing them after the fact after post_change
	 * notification. If the firing mode has been turned off, this has no effect.
	 */
	public void fire(IQ7ElementDelta customDelta, int eventType) {
		if (!this.isFiring) {
			return;
		}
		if (DEBUG) {
			System.out
					.println("-----------------------------------------------------------------------------------------------------------------------");//$NON-NLS-1$
		}
		IQ7ElementDelta deltaToNotify;
		if (customDelta == null) {
			deltaToNotify = this.mergeDeltas(this.modelDeltas);
		} else {
			deltaToNotify = customDelta;
		}
		// Notification
		// Important: if any listener reacts to notification by updating the
		// listeners list or mask, these lists will
		// be duplicated, so it is necessary to remember original lists in a
		// variable (since field values may change under us)
		IElementChangedListener[] listeners;
		int[] listenerMask;
		int listenerCount;
		synchronized (this.state) {
			listeners = this.state.elementChangedListeners;
			listenerMask = this.state.elementChangedListenerMasks;
			listenerCount = this.state.elementChangedListenerCount;
		}
		switch (eventType) {
		case DEFAULT_CHANGE_EVENT:
			this.firePostChangeDelta(deltaToNotify, listeners, listenerMask,
					listenerCount);
			this.fireReconcileDelta(listeners, listenerMask, listenerCount);
			break;
		case Q7ElementChangedEvent.POST_CHANGE:
			this.firePostChangeDelta(deltaToNotify, listeners, listenerMask,
					listenerCount);
			this.fireReconcileDelta(listeners, listenerMask, listenerCount);
			break;
		}
	}

	private void firePostChangeDelta(IQ7ElementDelta deltaToNotify,
			IElementChangedListener[] listeners, int[] listenerMask, int listenerCount) {
		// post change deltas
		if (DEBUG) {
			System.out
					.println("FIRING POST_CHANGE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out
					.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			// flush now so as to keep listener reactions to post their own
			// deltas for subsequent iteration
			this.flush();
			this.notifyListeners(deltaToNotify, Q7ElementChangedEvent.POST_CHANGE,
					listeners, listenerMask, listenerCount);
		}
	}

	@SuppressWarnings("rawtypes")
	private void fireReconcileDelta(IElementChangedListener[] listeners,
			int[] listenerMask, int listenerCount) {
		IQ7ElementDelta deltaToNotify = this.mergeDeltas(this.reconcileDeltas.values());
		if (DEBUG) {
			System.out
					.println("FIRING POST_RECONCILE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out
					.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			// flush now so as to keep listener reactions to post their own
			// deltas for subsequent iteration
			this.reconcileDeltas = new HashMap();
			this.notifyListeners(deltaToNotify, Q7ElementChangedEvent.POST_RECONCILE,
					listeners, listenerMask, listenerCount);
		}
	}

	/*
	 * Returns whether a given delta contains some information relevant to the
	 * Model, in particular it will not consider SYNC or MARKER only deltas.
	 */
	private boolean isAffectedBy(IResourceDelta rootDelta) {
		// if (rootDelta == null) System.out.println("NULL DELTA");
		// long start = System.currentTimeMillis();
		if (rootDelta != null) {
			// use local exception to quickly escape from delta traversal
			class FoundRelevantDeltaException extends RuntimeException {
				private static final long serialVersionUID = 7137113252936111022L; // backward
				// compatible
				// only the class name is used (to differenciate from other
				// RuntimeExceptions)
			}
			try {
				rootDelta.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) /*
																 * throws
																 * CoreException
																 */{
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
						case IResourceDelta.REMOVED:
							throw new FoundRelevantDeltaException();
						case IResourceDelta.CHANGED:
							// if any flag is set but SYNC or MARKER, this
							// delta
							// should be considered
							if (delta.getAffectedChildren().length == 0 // only
									// check
									// leaf
									// delta
									// nodes
									&& (delta.getFlags() & ~(IResourceDelta.SYNC | IResourceDelta.MARKERS)) != 0) {
								throw new FoundRelevantDeltaException();
							}
						}
						return true;
					}
				});
			} catch (FoundRelevantDeltaException e) {
				// System.out.println("RELEVANT DELTA detected in: "+
				// (System.currentTimeMillis() - start));
				return true;
			} catch (CoreException e) { // ignore delta if not able to traverse
			}
		}
		// System.out.println("IGNORE SYNC DELTA took: "+
		// (System.currentTimeMillis() - start));
		return false;
	}

	/*
	 * Returns whether the given element is a primary compilation unit in
	 * working copy mode.
	 */
	private boolean isWorkingCopy(IQ7Element element, HandleType elementType) {
		if (elementType.equals(HandleType.Context)
				|| elementType.equals(HandleType.Verification)
				|| elementType.equals(HandleType.TestCase)
				|| elementType.equals(HandleType.TestSuite)) {
			IQ7NamedElement cu = (IQ7NamedElement) element;
			return cu.isWorkingCopy();
		}
		return false;
	}

	/*
	 * Merges all awaiting deltas.
	 */
	@SuppressWarnings("rawtypes")
	private IQ7ElementDelta mergeDeltas(Collection deltas) {
		if (deltas.size() == 0) {
			return null;
		}
		if (deltas.size() == 1) {
			return (IQ7ElementDelta) deltas.iterator().next();
		}
		if (VERBOSE) {
			System.out
					.println("MERGING " + deltas.size() + " DELTAS [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		Iterator iterator = deltas.iterator();
		Q7ElementDelta rootDelta = new Q7ElementDelta(this.manager.getModel());
		boolean insertedTree = false;
		while (iterator.hasNext()) {
			Q7ElementDelta delta = (Q7ElementDelta) iterator.next();
			if (VERBOSE) {
				System.out.println(delta.toString());
			}
			IQ7Element element = delta.getElement();
			if (this.manager.getModel().equals(element)) {
				IQ7ElementDelta[] children = delta.getAffectedChildren();
				for (int j = 0; j < children.length; j++) {
					Q7ElementDelta projectDelta = (Q7ElementDelta) children[j];
					rootDelta.insertDeltaTree(projectDelta.getElement(), projectDelta);
					insertedTree = true;
				}
				IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
				if (resourceDeltas != null) {
					for (int i = 0, length = resourceDeltas.length; i < length; i++) {
						rootDelta.addResourceDelta(resourceDeltas[i]);
						insertedTree = true;
					}
				}
			} else {
				rootDelta.insertDeltaTree(element, delta);
				insertedTree = true;
			}
		}
		if (insertedTree) {
			return rootDelta;
		}
		return null;
	}

	private void notifyListeners(IQ7ElementDelta deltaToNotify, int eventType,
			IElementChangedListener[] listeners, int[] listenerMask, int listenerCount) {
		final Q7ElementChangedEvent extraEvent = new Q7ElementChangedEvent(deltaToNotify,
				eventType);
		for (int i = 0; i < listenerCount; i++) {
			if ((listenerMask[i] & eventType) != 0) {
				final IElementChangedListener listener = listeners[i];
				long start = -1;
				if (VERBOSE) {
					System.out.print("Listener #" + (i + 1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
					start = System.currentTimeMillis();
				}
				// wrap callbacks with Safe runnable for subsequent listeners to
				// be called when some are causing grief
				SafeRunner.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
						exception.printStackTrace();
					}

					public void run() throws Exception {
						listener.elementChanged(extraEvent);
					}
				});
				if (VERBOSE) {
					System.out
							.println(" -> " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	/*
	 * Generic processing for elements with changed contents:<ul> <li>The
	 * element is closed such that any subsequent accesses will re-open the
	 * element reflecting its new structure. <li>An entry is made in the delta
	 * reporting a content change (K_CHANGE with F_CONTENT flag set). </ul>
	 */
	private void nonQ7ResourcesChanged(Openable element, IResourceDelta delta)
			throws ModelException, InterruptedException {
		// reset non-q7 resources if element was open
		boolean cont = element.accessInfoIfOpened(info -> {
			switch (element.getElementType()) {
			case Model:
				((ModelInfo) info).foreignResources = null;
				this.currentDelta().addResourceDelta(delta);
				return false;
			case Project:
				((Q7ProjectInfo) info).setForeignResources(null);
				// if a package fragment root is the project, clear it too
				break;
			case Folder:
				((Q7FolderInfo) info).setForeignResources(null);
				break;
			}
			return true;
		}).orElse(true);
		if (!cont) {
			return;
		}
		Q7ElementDelta current = this.currentDelta();
		Q7ElementDelta elementDelta = current.find(element);
		if (elementDelta == null) {
			// don't use find after creating the delta as it can be null (see
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=63434)
			elementDelta = current.changed(element, IQ7ElementDelta.F_CONTENT);
		}
		elementDelta.addResourceDelta(delta);
	}

	private void popUntilPrefixOf(IPath path) {
		while (this.currentElement != null) {
			IPath currentElementPath = null;
			IResource currentElementResource = this.currentElement.getResource();
			if (currentElementResource != null) {
				currentElementPath = currentElementResource.getFullPath();
			}
			if (currentElementPath != null) {
				if (this.currentElement instanceof IQ7Folder
						&& ((IQ7Folder) this.currentElement).isRootFolder()
						&& currentElementPath.segmentCount() != path.segmentCount() - 1) {
					// default package and path is not a direct child
					this.currentElement = (Openable) this.currentElement.getParent();
				}
				if (currentElementPath.isPrefixOf(path)) {
					return;
				}
			}
			this.currentElement = (Openable) this.currentElement.getParent();
		}
	}

	private IQ7ElementDelta processResourceDelta(IResourceDelta changes) throws InterruptedException {
		try {
			Q7Model model = this.manager.getModel();
			try {
				model.openAndAccessInfo(ignored -> null, null);
			} catch (ModelException e) {
				throw new AssertionError(e);
			}
			this.currentElement = null;
			// get the workspace delta, and start processing there.
			IResourceDelta[] deltas = changes.getAffectedChildren();
			for (int i = 0; i < deltas.length; i++) {
				IResourceDelta delta = deltas[i];
				IResource res = delta.getResource();
				// find out the element type
				Q7Project rootInfo = null;
				HandleType elementType;
				IProject proj = (IProject) res;
				boolean wasQ7Project = this.state.findProject(proj.getName()) != null;
				boolean isDylanProject = RcpttCore.hasRcpttNature(proj);
				if (!wasQ7Project && !isDylanProject) {
					elementType = HandleType.NonQ7;
				} else {
					rootInfo = (Q7Project) RcpttCore.create(res.getProject());
					elementType = HandleType.Project;
				}
				// traverse delta
				Set<IQ7NamedElement> toIndex = new HashSet<IQ7NamedElement>();
				this.traverseDelta(delta, elementType, rootInfo, toIndex);
				if (toIndex.size() > 0) {
					ProjectIndexerManager.indexNamedElements(toIndex);
				}

				if (elementType.equals(HandleType.NonQ7)
						|| (wasQ7Project != isDylanProject && (delta.getKind()) == IResourceDelta.CHANGED)) {
					/*
					 * project has changed nature (deq7ion or open/closed)
					 */
					try {
						// add child as non q7 resource
						this.nonQ7ResourcesChanged((Q7Model) model, delta);
					} catch (ModelException e) {
						// q7 model could not be opened
					}
				}
			}
			this.resetProjectCaches();
			this.executePostActions();
			return this.currentDelta;
		} finally {
			this.currentDelta = null;
			this.rootsToRefresh.clear();
			this.projectCachesToReset.clear();
			this.postActions.clear();
		}
	}

	private void executePostActions() {
		if (postActions.size() == 0) {
			return;
		}
		for (Iterator<Runnable> i = postActions.iterator(); i.hasNext();) {
			i.next().run();
		}
	}

	/*
	 * Traverse the set of projects which have changed namespace, and reset
	 * their caches and their dependents
	 */
	@SuppressWarnings("rawtypes")
	private void resetProjectCaches() {
		if (this.projectCachesToReset.size() == 0) {
			return;
		}
		Iterator iterator = this.projectCachesToReset.iterator();
		HashMap projectDepencies = this.state.projectDependencies;
		HashSet affectedDependents = new HashSet();
		while (iterator.hasNext()) {
			Q7Project project = (Q7Project) iterator.next();
			project.resetCaches();
			this.addDependentProjects(project, projectDepencies, affectedDependents);
		}
		// reset caches of dependent projects
		iterator = affectedDependents.iterator();
		while (iterator.hasNext()) {
			Q7Project project = (Q7Project) iterator.next();
			project.resetCaches();
		}
	}

	/*
	 * Registers the given delta with this delta processor.
	 */
	public void registerModelDelta(IQ7ElementDelta delta) {
		this.modelDeltas.add(delta);
	}

	/*
	 * Removes the given element from its parents cache of children. If the
	 * element does not have a parent, or the parent is not currently open, this
	 * has no effect.
	 */
	private void removeFromParentInfo(Openable child) throws InterruptedException {
		Openable parent = (Openable) child.getParent();
		if (parent == null) {
			return;
		}
		parent.accessInfo(info -> {
			OpenableElementInfo openable = (OpenableElementInfo) info;
			if (openable.isStructureKnown()) {
				info.removeChild(child);
			}
			return null;
		});
	}

	/*
	 * Notification that some resource changes have happened on the platform,
	 * and that the q7 Model should update any required internal structures such
	 * that its elements remain consistent. Translates
	 * <code>IResourceDeltas</code> into <code>IQ7ElementDeltas</code>.
	 * 
	 * @see IResourceDelta
	 * 
	 * @see IResource
	 */
	@SuppressWarnings("unused")
	public void resourceChanged(IResourceChangeEvent event) throws InterruptedException {
		int eventType = this.overridenEventType == -1 ? event.getType()
				: this.overridenEventType;
		IResource resource = event.getResource();
		IResourceDelta delta = event.getDelta();

		switch (eventType) {
		case IResourceChangeEvent.PRE_DELETE:
			if (resource.getType() == IResource.PROJECT
					&& RcpttCore.hasRcpttNature((IProject) resource)) {

				this.deleting((IProject) resource);
			}
			return;

		case IResourceChangeEvent.POST_CHANGE:
			if (this.isAffectedBy(delta)) { // avoid populating for SYNC or
				// MARKER
				// deltas
				try {
					try {
						this.stopDeltas();
						this.checkProjectsBeingAddedOrRemoved(delta);
						// generate external archive change deltas
						if (this.refreshedElements != null) {
							Set<IQ7Element> refreshedElementsCopy = null;
							if (refreshedElements != null) {
								refreshedElementsCopy = new HashSet<IQ7Element>();
								refreshedElementsCopy.addAll(refreshedElements);
								// To avoid concurrent modifications
								this.refreshedElements = null;
							}

						}
						IQ7ElementDelta translatedDelta = this
								.processResourceDelta(delta);
						if (translatedDelta != null) {
							this.registerModelDelta(translatedDelta);
						}
					} finally {
						this.startDeltas();
					}
					// Call update for model
					updateModel(null);
					IElementChangedListener[] listeners;
					// int listenerCount;
					synchronized (this.state) {
						listeners = this.state.elementChangedListeners;
						// listenerCount =
						// this.state.elementChangedListenerCount;
					}
					this.fire(null, Q7ElementChangedEvent.POST_CHANGE);

				} finally {
					// workaround for bug 15168 circular errors not reported
					this.state.resetOldProjectNames();
				}
			}
			return;

		case IResourceChangeEvent.PRE_BUILD:
			if (!this.isAffectedBy(delta)) {
				return; // avoid populating for SYNC or MARKER deltas
			}
			// does not fire any deltas
			return;

		case IResourceChangeEvent.POST_BUILD:
			return;
		}
	}

	/*
	 * Turns the firing mode to on. That is, deltas that are/have been
	 * registered will be fired.
	 */
	private void startDeltas() {
		this.isFiring = true;
	}

	/*
	 * Turns the firing mode to off. That is, deltas that are/have been
	 * registered will not be fired until deltas are started again.
	 */
	private void stopDeltas() {
		this.isFiring = false;
	}

	private void traverseDelta(IResourceDelta delta, HandleType elementType,
			Q7Project rootInfo, Set<IQ7NamedElement> toIndex) throws InterruptedException {

		IResource res = delta.getResource();
		// set stack of elements
		if (this.currentElement == null && rootInfo != null) {
			this.currentElement = rootInfo;
		}
		// process current delta
		boolean processChildren = true;
		if (res instanceof IProject) {
			processChildren = this.updateCurrentDeltaAndIndex(delta, elementType,
					rootInfo, toIndex);
		} else if (rootInfo != null) {
			processChildren = this.updateCurrentDeltaAndIndex(delta, elementType,
					rootInfo, toIndex);
		} else {
			// not yet inside a package fragment root
			processChildren = true;
		}
		// process children if needed
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			int length = children.length;
			for (int i = 0; i < length; i++) {
				IResourceDelta child = children[i];
				IResource childRes = child.getResource();
				// find out whether the child is a package fragment root of the
				// current project
				int childKind = childRes.getType();
				// compute child type
				HandleType childType = elementType(childRes, childKind, rootInfo);
				this.traverseDelta(child, childType, rootInfo, toIndex);
			}

		}
	}

	private HandleType elementType(IResource childRes, int childKind, Q7Project rootInfo) {
		switch (childKind) {
		case IResource.FOLDER:
			return HandleType.Folder;
		case IResource.PROJECT:
			return HandleType.Project;
		case IResource.ROOT:
			return HandleType.Model;
		case IResource.FILE:
			IPath childPath = childRes.getFullPath();
			if (RcpttCore.isQ7File(childPath)) {
				if (RcpttCore.isQ7Context(childPath))
					return HandleType.Context;
				else if (RcpttCore.isQ7Verification(childPath))
					return HandleType.Verification;
				else if (RcpttCore.isQ7TestSuite(childPath))
					return HandleType.TestSuite;
				else if (RcpttCore.isQ7ProjectMetadata(childPath))
					return HandleType.ProjectMetadata;
				else
					return HandleType.TestCase;
			}
		}
		return HandleType.NonQ7;
	}

	public boolean updateCurrentDeltaAndIndex(IResourceDelta delta,
			HandleType elementType, Q7Project rootInfo, Set<IQ7NamedElement> toIndex) throws InterruptedException {
		Openable element;
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			IResource deltaRes = delta.getResource();
			element = this.createElement(deltaRes, elementType, rootInfo);
			if (element == null) {
				return true;
			}
			this.updateIndex(element, delta, toIndex);
			this.elementAdded(element, delta);
			return elementType.equals(HandleType.Folder);
		case IResourceDelta.REMOVED:
			deltaRes = delta.getResource();
			element = this.createElement(deltaRes, elementType, rootInfo);
			if (element == null) {
				// resource might be containing shared roots (see bug 19058)
				return true;
			}
			this.updateIndex(element, delta, toIndex);
			this.elementRemoved(element, delta, rootInfo);

			return elementType.equals(HandleType.Folder);
		case IResourceDelta.CHANGED:
			int flags = delta.getFlags();
			if ((flags & IResourceDelta.CONTENT) != 0
					|| (flags & IResourceDelta.ENCODING) != 0) {
				// content or encoding has changed
				element = this.createElement(delta.getResource(), elementType, rootInfo);
				if (element == null) {
					return false;
				}
				this.updateIndex(element, delta, toIndex);
				this.contentChanged(element);
			} else if (elementType.equals(HandleType.Project)) {
				if ((flags & IResourceDelta.OPEN) != 0) {
					// project has been opened or closed
					final IProject res = (IProject) delta.getResource();
					element = this.createElement(res, elementType, rootInfo);
					if (element == null) {
						return false;
					}
					if (res.isOpen()) {
						if (RcpttCore.hasRcpttNature(res)) {
							this.addToParentInfo(element);
							this.currentDelta().opened(element);
							// refresh pkg fragment roots and caches of the
							// project (and its dependents)
							final IQ7Project project = (IQ7Project) element;
							this.rootsToRefresh.add(project);
							this.projectCachesToReset.add(project);
							this.postActions.add(new Runnable() {
								public void run() {
									ProjectIndexerManager.indexProject(res);
								}
							});
						}
					} else {
						if (this.state.findProject(res.getName()) != null) {
							this.close(element);
							this.removeFromParentInfo(element);
							this.currentDelta().closed(element);
							this.manager.getIndexManager().discardJobs(element.getName());
							final IPath projectPath = res.getFullPath();
							this.manager.getIndexManager().removeIndex(projectPath);
							ProjectIndexerManager.removeProject(projectPath);
						}
					}
					return false; // when a project is open/closed don't
					// process children
				}
				if ((flags & IResourceDelta.DESCRIPTION) != 0) {
					IProject res = (IProject) delta.getResource();
					boolean isQ7Project = RcpttCore.hasRcpttNature(res);
					element = this.createElement(res, elementType, rootInfo);
					if (element == null) {
						return false; // note its resources are still
					}
					// project's nature has been added or removed
					// visible as roots to other
					// projects
					if (isQ7Project) {
						this.elementAdded(element, delta);
						ProjectIndexerManager.indexProject(res);
					} else {
						this.elementRemoved(element, delta, rootInfo);
						this.manager.getIndexManager().discardJobs(element.getName());
						final IPath projectPath = res.getFullPath();
						this.manager.getIndexManager().removeIndex(projectPath);
						ProjectIndexerManager.removeProject(projectPath);
					}
					return false; // when a project's nature is
						// added/removed don't process children
				}
			}
			return true;
		}
		return true;
	}

	private void updateIndex(Openable element, IResourceDelta delta,
			Set<IQ7NamedElement> toIndex) {
		IndexManager indexManager = this.manager.getIndexManager();
		if (indexManager == null) {
			return;
		}
		switch (element.getElementType()) {
		case Project:
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				final IQ7Project q7Project = element.getQ7Project();
				this.postActions.add(new Runnable() {
					public void run() {
						ProjectIndexerManager.indexProject(q7Project);
					}
				});
				break;
			case IResourceDelta.REMOVED:
				final IPath projectPath = element.getQ7Project().getProject()
						.getFullPath();
				indexManager.removeIndex(projectPath);
				ProjectIndexerManager.removeProject(projectPath);
				// NB: Discarding index jobs belonging to this project
				// was done
				// during PRE_DELETE
				break;
			// NB: Update of index if project is opened, closed, or its
			// q7
			// nature is added or removed
			// is done in updateCurrentDeltaAndIndex
			}
			break;
		case Folder:
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
			case IResourceDelta.REMOVED:
				IQ7Folder pkg = null;
				if (element instanceof IQ7Project) {
					IQ7Project root = (IQ7Project) element;
					pkg = root.getFolder(Path.EMPTY);
				} else {
					pkg = (IQ7Folder) element;
				}
				IResourceDelta[] children = delta.getAffectedChildren();
				for (int i = 0, length = children.length; i < length; i++) {
					IResourceDelta child = children[i];
					IResource resource = child.getResource();
					// TODO (philippe) Why do this? Every child is added
					// anyway
					// as the delta is walked
					if (resource instanceof IFile) {
						String name = resource.getName();
						if (RcpttCore.isQ7File(resource.getFullPath())) {
							Openable cu = (Openable) pkg.getNamedElement(name);
							this.updateIndex(cu, child, toIndex);
						}
					}
				}
			}
			break;
		case TestCase:
		case TestSuite:
		case Context:
		case Verification:
		case ProjectMetadata:
			IFile file = (IFile) delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.CHANGED:
				// no need to index if the content has not changed
				int flags = delta.getFlags();
				if ((flags & IResourceDelta.CONTENT) == 0
						&& (flags & IResourceDelta.ENCODING) == 0) {
					break;
				}
			case IResourceDelta.ADDED:
				if (ProjectIndexerManager.isIndexerEnabled(file.getProject())) {
					// ProjectIndexerManager
					// .indexNamedElement((IQ7NamedElement) element);
					toIndex.add((IQ7NamedElement) element);
				}
				// Clean file from secondary types cache but do not
				// update
				// indexing secondary type cache as it will be updated
				// through
				// indexing itself
				// this.manager.secondaryTypesRemoving(file, false);
				break;
			case IResourceDelta.REMOVED:
				final IProject project = file.getProject();
				/* remove project segment */
				final String path = file.getFullPath().removeFirstSegments(1).toString();
				ProjectIndexerManager.removeNamedElement(RcpttCore.create(project), path);
				break;
			}
		}
	}

	/*
	 * Update Model given some delta
	 */
	public void updateModel(IQ7ElementDelta customDelta) throws InterruptedException {
		if (customDelta == null) {
			for (int i = 0, length = this.modelDeltas.size(); i < length; i++) {
				IQ7ElementDelta delta = this.modelDeltas.get(i);
				this.modelUpdater.processDelta(delta);
			}
		} else {
			this.modelUpdater.processDelta(customDelta);
		}
	}
}
