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
package org.eclipse.rcptt.core.internal.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.rcptt.core.builder.IQ7ProblemReporter.ProblemType;
import org.eclipse.rcptt.core.builder.IQ7Validator;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.utils.ModelCycleDetector;
import org.eclipse.rcptt.core.utils.ModelCycleDetector.CycleGraph;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.WorkspaceMonitor;
import org.eclipse.rcptt.internal.core.model.BackReferencesProjectScope;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.core.model.ReferencedProjectScope;
import org.eclipse.rcptt.internal.core.model.deltas.Q7ElementDelta;
import org.eclipse.rcptt.internal.core.model.index.NamedElementCollector;
import org.osgi.framework.FrameworkUtil;

public class Q7Builder extends IncrementalProjectBuilder {

	private static final String MSG_Q7_Builder = "RCP Testing Tool: Checking model consistency";
	private static boolean isEnable = true;
	private static List<Runnable> listeners = new ArrayList<Runnable>();
	private static final ILog LOG = Platform.getLog(FrameworkUtil.getBundle(Q7Builder.class));
	
	/** Listener is called at the end of each project build */
	public static synchronized void addListener(Runnable runnable) {
		listeners.add(runnable);
	}
	
	public static synchronized void removeListener(Runnable runnable) {
		listeners.remove(runnable);
	}

	class Q7DeltaVisitor implements IResourceDeltaVisitor {
		private List<IQ7NamedElement> elements;
		private List<IPath> removed = new ArrayList<IPath>();
		private Set<IPath> changedResources = new HashSet<IPath>();

		public Q7DeltaVisitor(List<IQ7NamedElement> elements) {
			this.elements = elements;
		}

		public List<IPath> getRemoved() {
			return removed;
		}

		/**
		 * Get changed resource files from provided delta. Required to find contexts linked with resource
		 * 
		 * @return
		 */
		public Set<IPath> getChangedResources() {
			return changedResources;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				addResource(resource, elements);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				removed.add(resource.getFullPath());
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				addResource(resource, elements);
				break;
			}

			// search for direct modifications of resources
			IResourceDelta[] affectedChildren = delta.getAffectedChildren();
			if (affectedChildren.length == 0) {
				changedResources.add(resource.getFullPath());
			}

			// return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = RcpttNature.BUILDER_ID;

	public static final String MARKER_TYPE = "org.eclipse.rcptt.core.builder.q7Problem";

	@SuppressWarnings("unused")
	private static final int NTHREDS = 10;

	private Q7ValidatorManager validatorMgr;

	public static boolean debug_sleep = false;

	public static IMarker addMarker(IFile file, String message, int lineNumber,
			int severity, int offset, int length, int sourceId) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			if (sourceId > 0) {
				marker.setAttribute(IMarker.SOURCE_ID, sourceId);
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			if (offset >= 0 && length > 0) {
				marker.setAttribute(IMarker.CHAR_START, offset);
				marker.setAttribute(IMarker.CHAR_END, offset + length);
			}
			return marker;
		} catch (CoreException e) {
		}
		return null;
	}

	public static void setEnabled(boolean isEnable) {
		Q7Builder.isEnable = isEnable;
	}
	
	static void log(IStatus status) {
		//These redundant checks already were here before my refactoring. Gulevich.
		try {
			Q7BuilderActivator pl = Q7BuilderActivator.getDefault();
			if (pl != null && pl.getLog() != null) {
				pl.getLog().log(status);
			} else {
				if (status.getException() != null)
					status.getException().printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (status.getException() != null)
				status.getException().printStackTrace();
		}
	}
	
	static void log(Throwable e) {
		log (new Status(IStatus.ERROR,
		Q7BuilderActivator.PLUGIN_ID,
		IResourceStatus.BUILD_FAILED,
		"Internal error in RCP Testing Tool Builder", e));
	}

	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (!isEnable) {
			return null;
		}
		ModelManager.getModelManager().projectStartBuilding(getProject());
		try {
			while (debug_sleep) {
				Thread.sleep(50);
			}
			ModelManager.getModelManager().getIndexManager()
					.waitUntilReady(new NullProgressMonitor());// new
																// SubProgressMonitor(monitor,
																// -1));
			getQ7Project().getMetadata();
			if (kind == FULL_BUILD) {
				fullBuild(monitor);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					try {
						incrementalBuild(delta, monitor);
					} catch (CoreException e) {
						log (e);
						fullBuild(monitor);
					}
				}
			}
		} catch (InterruptedException e) {
			throw new CoreException(error(e));
		} catch (RuntimeException e) {
			throw new CoreException(error(e));
		} finally {
			monitor.done();
			ModelManager.getModelManager().projectStopBuilding(getProject());
			synchronized(this) {
				for (Runnable listener: listeners) {
					listener.run();
				}
			}
		}

		return null;
	}

	private static boolean addResource(IResource resource,
			List<IQ7NamedElement> elements) {
		if (resource instanceof IFile
				&& RcpttCore.isQ7File(resource.getFullPath())
				&& !WorkspaceMonitor.isIgnored(resource)) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			elements.add((IQ7NamedElement) RcpttCore.create(file));
		}
		return true;
	}

	private static void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException, InterruptedException {
		SubMonitor sm = SubMonitor.convert(monitor, MSG_Q7_Builder, 100);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, sm.split(10));
		NamedElementCollector collector = new NamedElementCollector() {
			@Override
			public boolean visit(IQ7Element element) {
				if (monitor.isCanceled()) {
					return false;
				}
				return super.visit(element);
			}
		};
		IQ7Project project = getQ7Project();
		project.accept(collector);
		sm.worked(10);
		List<IQ7NamedElement> elements = collector.getElements();

		// simply rebuild all contexts each time
		IContext[] contexts = Q7SearchCore.findAllContexts();
		for (IContext c : contexts)
			elements.add(c);

		// List<IQ7NamedElement> elements = calculateExtraDependencies(
		// collector.getElements(), null, new SubProgressMonitor(
		// monitor, 10));
		buildElements(elements, sm.split(80));

		IProgressMonitor.done(monitor);
	}

	private IQ7Project getQ7Project() {
		return RcpttCore.create(getProject());
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException, InterruptedException {
		monitor.beginTask(MSG_Q7_Builder, 40);
		// Check for project metadata file change
		boolean needFullBuildProjectMetadataChange = false;
		IResourceDelta[] affectedChildren = delta.getAffectedChildren();
		for (IResourceDelta res : affectedChildren) {
			if (RcpttCore.isQ7ProjectMetadata(res.getFullPath())) {
				needFullBuildProjectMetadataChange = true;
				break;
			}
		}

		if (needFullBuildProjectMetadataChange
				|| (delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
			if (delta.getResource() instanceof IProject) {
				fullBuild(monitor);
				return;
			}
		}

		// the visitor does the work.
		List<IQ7NamedElement> elements = new ArrayList<IQ7NamedElement>();
		Q7DeltaVisitor visitor = new Q7DeltaVisitor(elements);
		delta.accept(visitor);
		List<IPath> removed = visitor.getRemoved();
		Set<IPath> changedResources = visitor.getChangedResources();
		@SuppressWarnings("unused")
		long start = System.currentTimeMillis();
		elements = calculateExtraDependencies(elements, removed, changedResources,
				new SubProgressMonitor(monitor, 10));
		// System.out.println("Calculate extra deps:"
		// + Long.toString(System.currentTimeMillis() - start));
		buildElements(elements, new SubProgressMonitor(monitor, 20));
		monitor.done();
	}

	private void buildElements(List<IQ7NamedElement> elements,
			final IProgressMonitor monitor) {
		try {
			final Q7ProblemCollector collector = new Q7ProblemCollector();
			final IQ7Validator[] validators = getValidators();
			monitor.beginTask("Validate RCPTT model:", elements.size() + 20);

			ExecutorService executor = Executors.newFixedThreadPool(Runtime
					.getRuntime().availableProcessors() + 1);

			for (final IQ7NamedElement element : elements) {
				if (monitor.isCanceled()) {
					monitor.done();
					return;
				}
				executor.execute(new Runnable() {
					public void run() {
						IProgressMonitor subMonitor = new SubProgressMonitor(
								monitor, 1);

						subMonitor.beginTask(
								"Validate RCPTT element " + element.getName(), 2 + validators.length);
						subMonitor.subTask("Validate RCPTT element "
								+ element.getName());
						deleteMarkers((IFile) element.getResource());
						subMonitor.worked(1);

						try {
							final IQ7NamedElement indexingWorkingCopy = element.getIndexingWorkingCopy(null);
							try {
								for (IQ7Validator validator : validators) {
									if (monitor.isCanceled()) {
										monitor.done();
										subMonitor.done();
										return;
									}
									IProgressMonitor mon = new SubProgressMonitor(
											subMonitor, 1) {
										@Override
										public void setTaskName(String name) {
										}

									};
									validator.validate(indexingWorkingCopy, collector, mon);
									mon.done();
								}
							} finally {
								indexingWorkingCopy.discardWorkingCopy();
							}
						} catch (Exception e) {
							RcpttPlugin.log(e);
							collector.reportProblem((IFile) element.getResource(), ProblemType.Error, "Validation fail", -1, -1, -1, -1);
						} finally {
							subMonitor.done();
						}
					}
				});
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
				try {
					Thread.sleep(50);
				} catch (Throwable e) {
					// ignore
				}
			}

			CycleGraph graph = ModelCycleDetector.buildCycles(
					elements.toArray(new IQ7NamedElement[elements.size()]),
					null, new SubProgressMonitor(monitor, 10));
			if (graph.cycle) {
				Set<String> set = graph.graph.keySet();
				for (String id : set) {
					if (monitor.isCanceled()) {
						monitor.done();
						return;
					}
					IQ7NamedElement element = graph.elementsMap.get(id);
					if (element != null) {
						List<String> toElementID = graph.graph.get(id);
						StringBuilder names = new StringBuilder();
						for (String eid : toElementID) {
							if (monitor.isCanceled()) {
								return;
							}
							IQ7NamedElement toElement = graph.elementsMap
									.get(eid);
							if (toElement != null) {
								if (names.length() > 0) {
									names.append(", ");
								}
								names.append(toElement.getName());
							} else {
								names.append(eid);
							}
						}
						collector.reportProblem(
								(IFile) element.getResource(),
								ProblemType.Error,
								"Circular reference to context('s) "
										+ names.toString() + " is detected",
								-1, -1, -1, -1);
					}
				}
			}

			collector.flushProblems(new SubProgressMonitor(monitor, 10));

			ModelManager mm = ModelManager.getModelManager();
			Q7ElementDelta delta = new Q7ElementDelta(mm.getModel());
			for (IQ7NamedElement el : elements) {
				delta.changed(el, IQ7ElementDelta.F_MARKERS);
			}
			mm.getDeltaProcessor().fire(delta,
					Q7ElementChangedEvent.POST_CHANGE);
		} finally {
			monitor.done();
		}
	}

	private IQ7Validator[] getValidators() {
		if (validatorMgr == null) {
			validatorMgr = new Q7ValidatorManager();
		}
		return validatorMgr.getExtensions();
	}

	private List<IQ7NamedElement> calculateExtraDependencies(
			List<IQ7NamedElement> elements, List<IPath> removed, Set<IPath> changedResources,
			IProgressMonitor monitor) throws CoreException {
		Set<IQ7NamedElement> result = new HashSet<IQ7NamedElement>();
		result.addAll(elements);

		while (true) {
			if (monitor.isCanceled()) {
				break;
			}
			int cur = result.size();
			List<IQ7NamedElement> temp = new ArrayList<IQ7NamedElement>();
			temp.addAll(result);
			for (IQ7NamedElement iFile : temp) {
				if (monitor.isCanceled()) {
					break;
				}
				if (iFile instanceof IContext) {
					// Add all dependent elements
					try {
						String id = Q7SearchCore.findIDByDocument(iFile);
						if (id == null) {
							id = iFile.getID();
						}
						IQ7NamedElement[] usage = Q7SearchCore
								.findContextUsage(id,
										new BackReferencesProjectScope(
												getQ7Project()), monitor);
						result.addAll(Arrays.asList(usage));
					} catch (ModelException e) {
						RcpttPlugin.log(e);
					}
				}
				if (iFile instanceof IVerification) {
					// Add all dependent elements
					try {
						String id = Q7SearchCore.findIDByDocument(iFile);
						if (id == null) {
							id = iFile.getID();
						}
						IQ7NamedElement[] usage = Q7SearchCore
								.findVerificationUsage(id,
										new BackReferencesProjectScope(
												getQ7Project()), monitor);
						result.addAll(Arrays.asList(usage));
					} catch (ModelException e) {
						RcpttPlugin.log(e);
					}
				}
				// Find element with same ID
				try {
					IQ7NamedElement[] sameID = Q7SearchCore.findById(
							iFile.getID(),
							new BackReferencesProjectScope(iFile.getQ7Project()),
							monitor);
					if (sameID != null) {
						result.addAll(Arrays.asList(sameID));
					}
				} catch (ModelException e) {
					RcpttPlugin.log(e);
				}
			}
			if (removed != null && removed.size() > 0) {
				result.addAll(Arrays.asList(Q7SearchCore
						.findElementsWithUnusedReferences(new ReferencedProjectScope(getQ7Project()),
								monitor)));
				
				result.addAll(findMarkedElements());

				// Process removal of items referenced by test suits
				// TODO: check performance here
				result.addAll(Arrays.asList(Q7SearchCore.findAllTestSuites(new ReferencedProjectScope(getQ7Project()))));
			}
			if (cur == result.size()) {
				break;
			}
		}

		// search for contexts linked with changed resources
		if (changedResources != null && changedResources.size() > 0) {
			IContext[] contexts = Q7SearchCore.findContextsWithLinks(
					new BackReferencesProjectScope(getQ7Project()), changedResources, monitor);
			for (IContext c : contexts) {
				result.add(c);
			}
		}


		// TODO: Add check for contexts with unresolved dependencies.
		monitor.done();
		return new ArrayList<IQ7NamedElement>(result);
	}
	
	/// Tests and contexts with problems (same id, for example)
	Collection<IQ7NamedElement> findMarkedElements() throws CoreException {
		final List<IQ7NamedElement> rv = new ArrayList<IQ7NamedElement>();
		for (IMarker marker: getProject().findMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE)) {
			IResource resource = marker.getResource();
			if (resource instanceof IFile && RcpttCore.isQ7File(resource.getFullPath())) {
				rv.add((IQ7NamedElement)RcpttCore.create(resource));
			}
		}
		return rv;
	}
	
	private static IStatus error(InterruptedException e) {
		return new Status(IStatus.CANCEL, LOG.getBundle().getSymbolicName(), 0, "Interrupted", e);
	}
	
	private static IStatus error(Exception e) {
		return new Status(IStatus.CANCEL, LOG.getBundle().getSymbolicName(), 0, e.getLocalizedMessage(), e);
	}

}
