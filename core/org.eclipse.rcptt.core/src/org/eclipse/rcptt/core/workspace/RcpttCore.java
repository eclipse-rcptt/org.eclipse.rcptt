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
package org.eclipse.rcptt.core.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.core.IQ7Extension;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7Model;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IQ7ProjectMetadata;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.scenario.CapabilityContext;
import org.eclipse.rcptt.core.scenario.CapabilityContextItem;
import org.eclipse.rcptt.core.scenario.GroupContext;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.scenario.SuperContext;
import org.eclipse.rcptt.core.scenario.TestSuiteItem;
import org.eclipse.rcptt.core.scenario.UnresolvedContext;
import org.eclipse.rcptt.core.scenario.UnresolvedVerification;
import org.eclipse.rcptt.core.scenario.WorkbenchContext;
import org.eclipse.rcptt.internal.core.Q7ExtensionManager;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.core.model.Q7Folder;
import org.eclipse.rcptt.internal.core.model.Q7InternalContext;
import org.eclipse.rcptt.internal.core.model.Q7InternalVerification;
import org.eclipse.rcptt.internal.core.model.Q7Model;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

public class RcpttCore {
	private static final ILog LOG = Platform.getLog(FrameworkUtil.getBundle(RcpttCore.class));

	public static final double SCENARIO_VERSION = 3.0;

	public static final String DEFAULT_WORKBENCH_CONTEXT_ID = "close.modal.dialogs";

	private static RcpttCore instance;

	public static synchronized RcpttCore getInstance() {
		if (instance == null) {
			Bundle bundle = Platform.getBundle("org.eclipse.core.resources");
			if (bundle == null) {
				RcpttPlugin.log(
						"There is no resources support. Q7Core could not be initialized...",
						null);
				return null;
			}
			instance = new RcpttCore();

			IQ7Extension[] exts = Q7ExtensionManager.getInstance().getExtensions();
			for (IQ7Extension ext : exts) {
				ext.initialize();
			}
		}
		return instance;
	}

	public static Version getPlatformVersion() {
		return TeslaCore.getPlatformVersion();
	}

	/**
	 * Returns an array of all contexts contained in the given scenario or context, or an empty array if the element is
	 * neither a scenario nor a context.
	 */
	public IContext[] getContexts(IQ7NamedElement element, IWorkspaceFinder finder, boolean ignoreErrors) {
		if (element instanceof ITestCase) {
			return getContexts((ITestCase) element, finder, ignoreErrors);
		} else if (element instanceof IContext) {
			return getContexts((IContext) element, finder, ignoreErrors);
		} else {
			return new IContext[0];
		}
	}

	/**
	 * Returns an array of all contexts contained in the given scenario or context, or an empty array if the element is
	 * neither a scenario nor a context.
	 */
	public IContext[] getContexts(IQ7NamedElement element, IWorkspaceFinder finder, boolean ignoreErrors,
			String capability) {
		if (element instanceof ITestCase) {
			return getContexts((ITestCase) element, finder, ignoreErrors);
		} else if (element instanceof IContext) {
			return getContexts((IContext) element, finder, ignoreErrors, capability);
		} else {
			return new IContext[0];
		}
	}

	/**
	 * Returns an array of all contexts contained in the given scenario, or an empty array in case of an error.
	 */
	public IContext[] getContexts(ITestCase scenario, IWorkspaceFinder finder, boolean ignoreErrors) {
		return getContexts(scenario, finder, ignoreErrors, null);
	}

	/**
	 * Returns an array of all contexts contained in the given scenario, or an empty array in case of an error.
	 */
	public IContext[] getContexts(ITestCase scenario, IWorkspaceFinder finder,
			boolean ignoreErrors, String capability) {
		try {
			String[] contexts = Q7SearchCore.findContextsByDocument(scenario);
			if (contexts == null) {
				contexts = scenario.getContexts();
			}
			return getContexts(scenario, Arrays.asList(contexts), finder, ignoreErrors, capability);
		} catch (ModelException e) {
			RcpttPlugin.log(e);
			return new IContext[0];
		}
	}

	/**
	 * Returns an array of all child contexts. If the given context is neither a super context nor a group context, then
	 * it doesn't have any children and an empty array is returned.
	 */
	public IContext[] getContexts(IContext context, IWorkspaceFinder finder, boolean ignoreErrors) {
		return getContexts(context, getContextReferences(context), finder, ignoreErrors);
	}

	/**
	 * Returns an array of all child contexts. If the given context is neither a super context nor a group context, then
	 * it doesn't have any children and an empty array is returned.
	 */
	public IContext[] getContexts(IContext context, IWorkspaceFinder finder, boolean ignoreErrors, String capability) {
		return getContexts(context, getContextReferences(context, capability), finder, ignoreErrors);
	}

	public IContext[] getContexts(IQ7NamedElement element, List<String> contextIds,
			IWorkspaceFinder finder, boolean ignoreErrors) {
		return getContexts(element, contextIds, finder, ignoreErrors, null);
	}

	public IContext[] getContexts(IQ7NamedElement element, List<String> contextIds,
			IWorkspaceFinder finder, boolean ignoreErrors, String capability) {
		if (finder == null) {
			finder = WorkspaceFinder.getInstance();
		}
		List<IContext> contexts = new ArrayList<IContext>();
		Set<String> projectContexts = new HashSet<String>();
		// Add project related contexts
		try {
			if (element instanceof ITestCase) {
				IQ7Project project = element.getQ7Project();
				IQ7ProjectMetadata metadata = project == null ? null : project.getMetadata();
				if (metadata != null && metadata.exists()) {
					String[] ctxs = metadata.getContexts();
					if (ctxs != null) {
						for (String ctx : ctxs) {
							IContext result = findContext(element, ignoreErrors, ctx,
									finder);
							if (result != null) {
								if (result.getNamedElement() instanceof CapabilityContext) {
									List<IContext> subcontexts = processedCapabilityContext(element, finder,
											ignoreErrors, capability,
											(CapabilityContext) result.getNamedElement());
									for (IContext context : subcontexts) {
										contexts.add(context);
										projectContexts.add(context.getID());
									}
								} else {
									contexts.add(result);
									projectContexts.add(result.getID());
								}
							}
						}
					}
				}
			}
		} catch (ModelException e) {
			RcpttPlugin.log(e);
		}

		for (String contextId : contextIds) {
			if (projectContexts.contains(contextId)) {
				continue;
			}
			IContext result = findContext(element, ignoreErrors, contextId, finder);
			if (result != null) {
				try {
					if (result.getNamedElement() instanceof CapabilityContext) {
						List<IContext> subcontexts = processedCapabilityContext(element, finder, ignoreErrors,
								capability, (CapabilityContext) result.getNamedElement());
						contexts.addAll(subcontexts);

					} else {
						contexts.add(result);
					}
				} catch (ModelException e) {
					RcpttPlugin.log(e);
				}
			}
		}

		// Add close modal dialogs context as first
		addDefaultContext(contexts);
		return contexts.toArray(new IContext[contexts.size()]);
	}

	private List<IContext> processedCapabilityContext(IQ7NamedElement element, IWorkspaceFinder finder,
			boolean ignoreErrors, String capability, CapabilityContext context) throws ModelException {
		for (CapabilityContextItem item : context.getItems()) {
			if (item.getCapability().contains(capability)) {
				final List<IContext> result = new ArrayList<IContext>();
				for (String id : item.getContextReferences()) {
					IContext subcontext = findContext(element, ignoreErrors, id, finder);
					if (subcontext != null) {
						if (subcontext.getNamedElement() instanceof CapabilityContext) {
							List<IContext> subcontexts = processedCapabilityContext(element, finder, ignoreErrors,
									capability, (CapabilityContext) subcontext.getNamedElement());
							result.addAll(subcontexts);
						} else {
							result.add(subcontext);
						}
					}
				}
				return result;
			}
		}
		return Collections.emptyList();

	}

	/**
	 * Returns a list of references to all child contexts. If the given context is neither a super context nor a group
	 * context, then it doesn't have any children and an empty list is returned.
	 */
	public List<String> getContextReferences(IContext context) {
		if (isNotGroupOrSuperOrCapabilityContext(context)) {
			return new ArrayList<String>();
		}
		try {
			NamedElement namedElement = context.getNamedElement();
			if (namedElement instanceof GroupContext) {
				return copy(((GroupContext) namedElement).getContextReferences());
			} else if (namedElement instanceof SuperContext) {
				return copy(((SuperContext) namedElement).getContextReferences());
			} else if (namedElement instanceof CapabilityContext) {
				final CapabilityContext ccontext = (CapabilityContext) namedElement;
				final Set<String> result = new LinkedHashSet<String>();
				for (CapabilityContextItem item : ccontext.getItems()) {
					result.addAll(item.getContextReferences());
				}
				return new ArrayList<String>(result);
			}

		} catch (ModelException e) {
			RcpttPlugin.log(e);
		}
		return new ArrayList<String>();
	}

	/**
	 * Returns a list of references to all child contexts. If the given context is neither a super context nor a group
	 * context, then it doesn't have any children and an empty list is returned.
	 */
	public List<String> getContextReferences(IContext context, String capability) {
		if (isNotGroupOrSuperOrCapabilityContext(context)) {
			return new ArrayList<String>();
		}
		try {
			NamedElement namedElement = context.getNamedElement();
			if (namedElement instanceof GroupContext) {
				return ((GroupContext) namedElement).getContextReferences();
			} else if (namedElement instanceof SuperContext) {
				return ((SuperContext) namedElement).getContextReferences();
			} else if (namedElement instanceof CapabilityContext) {
				final CapabilityContext ccontext = (CapabilityContext) namedElement;
				for (CapabilityContextItem item : ccontext.getItems()) {
					if (item.getCapability().contains(capability)) {
						return item.getContextReferences();
					}
				}
			}

		} catch (ModelException e) {
			RcpttPlugin.log(e);
		}
		return new ArrayList<String>();
	}

	/**
	 * Returns an array of all verifications in the given scenario, or an empty one in case of an error.
	 */
	public IVerification[] getVerifications(ITestCase scenario, IWorkspaceFinder finder,
			boolean ignoreErrors) {
		try {
			String[] verifications = Q7SearchCore.findVerificationsByDocument(scenario);
			if (verifications == null) {
				verifications = scenario.getVerifications();
			}
			return getVerifications(scenario, Arrays.asList(verifications), finder, ignoreErrors);
		} catch (ModelException e) {
			RcpttPlugin.log(e);
			return new IVerification[0];
		}
	}

	public Map<IQ7NamedElement, IFile> getCopyDestinationMap(
			IQ7NamedElement[] namedElements, IContainer destination) {
		Map<IQ7NamedElement, IFile> result = new TreeMap<IQ7NamedElement, IFile>(
				new Comparator<IQ7NamedElement>() {
					@Override
					public int compare(IQ7NamedElement o1, IQ7NamedElement o2) {
						if (o1.getName() == null) {
							return -1;
						}
						int eo = o1.getName().compareTo(o2.getName());
						if (eo == 0) {
							return o1.equals(o2) ? 0 : -1;
						}
						return eo;
					}
				});

		for (IQ7NamedElement element : namedElements) {
			IFile destinationFile = getDestinationFile(element, destination);
			result.put(element, destinationFile);
		}

		return result;
	}

	public void copy(final IQ7NamedElement[] elements,
			final Map<IQ7NamedElement, IFile> destinations,
			final Map<IQ7NamedElement, String> newNames) throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				Map<String, String> updatedIds = new HashMap<String, String>();
				List<ITestCase> scenarios = new ArrayList<ITestCase>();
				List<IContext> groups = new ArrayList<IContext>();
				List<ITestSuite> suites = new ArrayList<ITestSuite>();
				for (IQ7NamedElement element : elements) {
					if (element instanceof ITestCase) {
						scenarios.add((ITestCase) element);
					} else if (element instanceof IContext) {
						try {
							IContext context = (IContext) element;
							String oldID = context.getID();
							IContext contextCopy = copyContext(context,
									destinations.get(context), newNames.get(context));
							updatedIds.put(oldID, contextCopy.getID());

							if (contextCopy.getNamedElement() instanceof GroupContext) {
								groups.add(contextCopy);
							}
						} catch (IOException e) {
							RcpttPlugin.log(e);
						} catch (CoreException e) {
							RcpttPlugin.log(e);
						}
					} else if (element instanceof IVerification) {
						try {
							IVerification verification = (IVerification) element;
							String oldID = verification.getID();
							IVerification verificationCopy = copyVerification(verification,
									destinations.get(verification), newNames.get(verification));
							updatedIds.put(oldID, verificationCopy.getID());
						} catch (IOException e) {
							RcpttPlugin.log(e);
						} catch (CoreException e) {
							RcpttPlugin.log(e);
						}
					} else if (element instanceof ITestSuite) {
						try {
							ITestSuite suite = (ITestSuite) element;
							String oldID = suite.getID();
							ITestSuite suiteCopy = copySuite(suite,
									destinations.get(suite), newNames.get(suite));
							updatedIds.put(oldID, suiteCopy.getID());

							suites.add(suiteCopy);
						} catch (IOException e) {
							RcpttPlugin.log(e);
						} catch (CoreException e) {
							RcpttPlugin.log(e);
						}
					}
				}
				for (IContext g : groups) {
					// update context ids
					try {
						IContext copy = (IContext) g.getWorkingCopy(new NullProgressMonitor());
						try {
							EList<String> contextIds = ((GroupContext) copy
									.getNamedElement()).getContextReferences();
							for (int i = 0; i < contextIds.size(); i++) {
								String contextId = contextIds.get(i);
								String newId = updatedIds.get(contextId);
								if (newId != null) {
									contextIds.set(i, newId);
								}
							}
							copy.commitWorkingCopy(true, new NullProgressMonitor());
						} finally {
							copy.discardWorkingCopy();
						}
					} catch (ModelException e) {
						RcpttPlugin.log(e);
					}
				}
				for (ITestSuite s : suites) {
					// update context ids
					try {
						ITestSuite copy = (ITestSuite) s
								.getWorkingCopy(new NullProgressMonitor());
						try {
							EList<TestSuiteItem> contextIds = copy.getTestSuite()
									.getItems();
							for (int i = 0; i < contextIds.size(); i++) {
								TestSuiteItem testSuiteItem = contextIds.get(i);
								String contextId = testSuiteItem.getNamedElementId();
								String newId = updatedIds.get(contextId);
								if (newId != null) {
									testSuiteItem.setNamedElementId(newId);
								}
							}
							copy.commitWorkingCopy(true, new NullProgressMonitor());
						} finally {
							copy.discardWorkingCopy();
						}
					} catch (ModelException e) {
						RcpttPlugin.log(e);
					}
				}

				for (IQ7NamedElement scenario : scenarios) {
					copyScenario((ITestCase) scenario, updatedIds, destinations.get(scenario),
							newNames.get(scenario));
				}
			};
		}, new NullProgressMonitor());
	}

	public IVerification[] getVerifications(IQ7NamedElement element, List<String> verificationIds,
			IWorkspaceFinder finder, boolean ignoreErrors) {
		if (finder == null) {
			finder = WorkspaceFinder.getInstance();
		}
		List<IVerification> verifications = new ArrayList<IVerification>();
		Set<String> projectVerifications = new HashSet<String>();
		// Add project related verifications
		try {
			if (element != null && element instanceof ITestCase) {
				IQ7Project project = element.getQ7Project();
				IQ7ProjectMetadata metadata = project == null ? null : project.getMetadata();
				if (metadata != null && metadata.exists()) {
					String[] defaultVerifications = metadata.getVerifications();
					if (defaultVerifications != null) {
						for (String v : defaultVerifications) {
							IVerification result = findVerification(element, ignoreErrors, v,
									finder);
							if (result != null) {
								verifications.add(result);
								projectVerifications.add(result.getID());
							}
						}
					}
				}
			}
		} catch (ModelException e) {
			RcpttPlugin.log(e);
		}

		for (String vid : verificationIds) {
			if (projectVerifications.contains(vid)) {
				continue;
			}
			IVerification result = findVerification(element, ignoreErrors, vid, finder);
			if (result != null) {
				verifications.add(result);
			}
		}

		return verifications.toArray(new IVerification[verifications.size()]);
	}

	public IContext findContext(IQ7NamedElement element, boolean ignoreErrors,
			String contextId, IWorkspaceFinder finder) {
		if (finder == null) {
			finder = WorkspaceFinder.getInstance();
		}
		IContext[] context = finder.findContext(element, contextId);
		IContext result = null;
		if (context != null && context.length > 0) {
			result = context[0];
		} else {
			if (!ignoreErrors) {
				LOG.log(new Status(IStatus.INFO, LOG.getBundle().getSymbolicName(), "Failed to get context: " + contextId + " for element:" + element.getName()));
				UnresolvedContext ctx = ScenarioFactory.eINSTANCE
						.createUnresolvedContext();
				ctx.setName(contextId);
				ctx.setId(contextId); // if null, causes NPE in ContextExecutable.ContextExecutable()
				result = new Q7InternalContext((Q7Folder) element.getParent(), contextId,
						ctx);
			}
		}
		return result;
	}

	private IVerification findVerification(IQ7NamedElement element, boolean ignoreErrors,
			String verificationId, IWorkspaceFinder finder) {
		IVerification[] verification = finder.findVerification(element, verificationId);
		IVerification result = null;
		if (verification != null && verification.length > 0) {
			result = verification[0];
		} else {
			RcpttPlugin.log("Failed to get verification: " + verificationId + " for element:"
					+ element.getName(), null);
			if (!ignoreErrors) {
				UnresolvedVerification unresolved = ScenarioFactory.eINSTANCE
						.createUnresolvedVerification();
				unresolved.setName(verificationId);
				result = new Q7InternalVerification((Q7Folder) element.getParent(), verificationId,
						unresolved);
			}
		}
		return result;
	}

	private void addDefaultContext(List<IContext> contexts) {
		WorkbenchContext defaultWorkbenchContext = ScenarioFactory.eINSTANCE
				.createWorkbenchContext();
		defaultWorkbenchContext.setCloseEditors(false);
		defaultWorkbenchContext.setId(DEFAULT_WORKBENCH_CONTEXT_ID);
		defaultWorkbenchContext.setNoModalDialogs(true);
		defaultWorkbenchContext.setResetPerspective(false);
		defaultWorkbenchContext.setName("Close modal dialogs");
		defaultWorkbenchContext.setDescription("");
		defaultWorkbenchContext.setClearClipboard(false);
		contexts.add(0, new Q7InternalContext(ModelManager.getModelManager().getModel(),
				defaultWorkbenchContext.getName(), defaultWorkbenchContext));
	}

	private IContext copyContext(IContext context, IFile destinationFile, String newName)
			throws IOException, CoreException {
		IContext copy = null;
		try {
			copy = (IContext) copyNamedElement(context, destinationFile);
			if (newName != null) {
				copy.setElementName(newName);
			}

			copy.commitWorkingCopy(true, new NullProgressMonitor());
		} finally {
			if (copy != null) {
				copy.discardWorkingCopy();
			}
		}
		return copy;
	}

	private IVerification copyVerification(IVerification verification, IFile destinationFile, String newName)
			throws IOException, CoreException {
		IVerification copy = null;
		try {
			copy = (IVerification) copyNamedElement(verification, destinationFile);
			if (newName != null) {
				copy.setElementName(newName);
			}

			copy.commitWorkingCopy(true, new NullProgressMonitor());
		} finally {
			if (copy != null) {
				copy.discardWorkingCopy();
			}
		}
		return copy;
	}

	private ITestSuite copySuite(ITestSuite context, IFile destinationFile, String newName)
			throws IOException, CoreException {
		ITestSuite copy = null;
		try {
			copy = (ITestSuite) copyNamedElement(context, destinationFile);
			if (newName != null) {
				copy.setElementName(newName);
			}

			copy.commitWorkingCopy(true, new NullProgressMonitor());
		} finally {
			if (copy != null) {
				copy.discardWorkingCopy();
			}
		}
		return copy;
	}

	private void copyScenario(ITestCase scenario, Map<String, String> updatedIds,
			IFile destinationFile, String newName) throws CoreException {
		ITestCase copy = null;
		try {
			copy = (ITestCase) copyNamedElement(scenario, destinationFile);
			if (newName != null) {
				copy.setElementName(newName);
			}

			List<String> contextIds = new ArrayList<String>(Arrays.asList(copy
					.getContexts()));
			for (int i = 0; i < contextIds.size(); i++) {
				String contextId = contextIds.get(i);
				String newContextId = updatedIds.get(contextId);
				if (newContextId != null) {
					contextIds.set(i, newContextId);
				}
			}
			copy.setContexts(contextIds.toArray(new String[contextIds.size()]));

			List<String> verificationIds = new ArrayList<String>(Arrays.asList(copy
					.getVerifications()));
			for (int i = 0; i < verificationIds.size(); i++) {
				String verificationId = verificationIds.get(i);
				String newVerificationId = updatedIds.get(verificationId);
				if (newVerificationId != null) {
					verificationIds.set(i, newVerificationId);
				}
			}
			copy.setVerifications(
					verificationIds.toArray(new String[verificationIds.size()]));

			copy.commitWorkingCopy(true, new NullProgressMonitor());
		} finally {
			if (copy != null) {
				copy.discardWorkingCopy();
			}
		}
	}

	/**
	 * Return working copy of copied element
	 */
	private IQ7NamedElement copyNamedElement(IQ7NamedElement element,
			IFile destinationFile) throws CoreException, ModelException {

		if (destinationFile != null) {
			if (destinationFile.exists()) {
				destinationFile.delete(true, new NullProgressMonitor());
			}
			element.getResource().copy(destinationFile.getFullPath(), true,
					new NullProgressMonitor());
		} else {
			destinationFile = (IFile) element.getResource();
		}
		IQ7NamedElement copyTestcase = (IQ7NamedElement) RcpttCore.create(destinationFile);
		// update id
		IQ7NamedElement copy = copyTestcase.getWorkingCopy(new NullProgressMonitor());
		IQ7Folder folder = (IQ7Folder) copy.getParent();
		do {
			copy.setID(EcoreUtil.generateUUID());
		} while (folder.haveIDConflict(copy.getID()));
		return copy;
	}

	private IFile getDestinationFile(IQ7NamedElement element,
			IContainer destinationContainer) {
		IFile source = (IFile) element.getResource();
		IPath destinationContainerPath = destinationContainer.getFullPath();

		IWorkspace workspace = source.getWorkspace();
		IPath destinationPath = destinationContainerPath.append(source.getName());
		return workspace.getRoot().getFile(destinationPath);
	}

	private RcpttCore() {
	}

	public static boolean hasRcpttNature(IProject project) {
		try {
			return project.isAccessible() && project.isOpen() && RcpttNature.isRcpttProject(project);
		} catch (CoreException e) {
			RcpttPlugin.log(e);
			return false;
		}
	}

	public static IProject[] getQ7Projects() {
		List<IProject> result = new ArrayList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject iProject : projects) {
			if (hasRcpttNature(iProject)) {
				result.add(iProject);
			}
		}
		return result.toArray(new IProject[result.size()]);
	}

	public IProject[] getNonQ7Projects() {
		List<IProject> result = new ArrayList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject iProject : projects) {
			if (iProject.isAccessible() && iProject.isOpen()) {
				try {
					if (!RcpttNature.isRcpttProject(iProject)) {
						result.add(iProject);
					}
				} catch (CoreException e) {
					RcpttPlugin.log(e);
				}
			}
		}
		return result.toArray(new IProject[result.size()]);
	}

	public void rename(IFile file, IPath newPath) {
		try {
			file.move(newPath, false, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static boolean isQ7File(IPath path) {
		String ext = path.getFileExtension();
		return IQ7Folder.CONTEXT_FILE_EXTENSION.equals(ext)
				|| IQ7Folder.VERIFICATION_FILE_EXTENSION.equals(ext)
				|| IQ7Folder.SCENARIO_FILE_EXTENSION.equals(ext)
				|| IQ7Folder.TEST_FILE_EXTENSION.equals(ext)
				|| IQ7Folder.TEST_SUITE_FILE_EXTENSION.equals(ext)
				|| isQ7ProjectMetadata(path);
	}

	public static boolean isQ7Context(IPath path) {
		String ext = path.getFileExtension();
		return IQ7Folder.CONTEXT_FILE_EXTENSION.equals(ext);
	}

	public static boolean isQ7Verification(IPath path) {
		String ext = path.getFileExtension();
		return IQ7Folder.VERIFICATION_FILE_EXTENSION.equals(ext);
	}

	public static boolean isQ7TestSuite(IPath path) {
		String ext = path.getFileExtension();
		return IQ7Folder.TEST_SUITE_FILE_EXTENSION.equals(ext);
	}

	public static boolean isQ7ProjectMetadata(IPath path) {
		if (Q7Features.supportQ7OptionsFile) {
			return (path.segmentCount() == 2 && path.lastSegment().equals(
					IQ7Project.METADATA_NAME));
		}
		return false;
	}

	private static String runtimeVersion = null;

	public synchronized static String getRuntimeVersion() {
		if (runtimeVersion != null) {
			return runtimeVersion;
		}

		// runtimeVersion = "";
		String runtimeBundleId = "org.eclipse.rcptt.updates.runtime.e4x";
		Bundle runtimeBundle = Platform.getBundle(runtimeBundleId);
		if (runtimeBundle == null)
			throw new NullPointerException(runtimeBundleId
					+ " plugin is not loaded");
		Enumeration<String> paths = runtimeBundle.getEntryPaths("q7/plugins/");
		if (paths == null)
			return null;

		while (paths.hasMoreElements()) {
			String path = paths.nextElement();
			String st = "org.eclipse.rcptt.tesla.swt_";
			int ind = path.indexOf(st);
			if (ind > 0) {
				String version = path.substring(ind + st.length()).trim();
				if (version.endsWith(".jar")) {
					version = version.substring(0, version.length() - 4);
					String shot = "-SNAPSHOT";
					if (version.endsWith(shot)) {
						version = version.substring(0, version.length() - shot.length());
					}
					runtimeVersion = version;
					break;
				}
			}
		}

		return runtimeVersion;
	}

	public static IQ7Project create(IProject project) {
		if (project == null) {
			return null;
		}
		Q7Model model = ModelManager.getModelManager().getModel();
		return model.getProject(project);
	}

	public static IQ7Element create(IResource resource) {
		return ModelManager.create(resource, null);
	}

	public static IQ7Model create(IWorkspaceRoot root) {
		if (root == null) {
			return null;
		}
		return ModelManager.getModelManager().getModel();
	}

	// Listeners
	private static void addElementChangedListener(IElementChangedListener listener,
			int eventMask) {
		ModelManager.getModelManager().deltaState.addElementChangedListener(listener,
				eventMask);
	}

	public static void addElementChangedListener(IElementChangedListener listener) {
		addElementChangedListener(listener, Q7ElementChangedEvent.POST_CHANGE
				| Q7ElementChangedEvent.POST_RECONCILE);
	}

	public static void removeElementChangedListener(IElementChangedListener listener) {
		ModelManager.getModelManager().deltaState.removeElementChangedListener(listener);
	}

	public void findAllContexts(IQ7NamedElement element, Set<? super IContext> ctx) {
		for (IContext cx : getContexts(element, WorkspaceFinder.getInstance(), true)) {
			if (!(cx instanceof Q7InternalContext)) {
				if (ctx.add(cx)) {
					findAllContexts(cx, ctx);
				}
			}
		}
	}

	public void findAllVerifications(IQ7NamedElement element, Set<? super IVerification> verifications) {
		if (element instanceof ITestCase) {
			IVerification[] result = getVerifications((ITestCase) element, WorkspaceFinder.getInstance(), true);
			verifications.addAll(Arrays.asList(result));
		}
	}

	public boolean isNotGroupOrSuperOrCapabilityContext(IContext context) {
		String type = Q7SearchCore.findContextTypeByDocument(context);
		if (type == null && context != null) {
			try {
				type = context.getType().getId();
			} catch (ModelException e) {
				RcpttPlugin.log(e);
			}
		}
		if (type == null) {
			return false;
		}
		if (!"org.eclipse.rcptt.ctx.capability".equals(type) && !"org.eclipse.rcptt.ctx.group".equals(type)
				&& !"org.eclipse.rcptt.ctx.super".equals(type)) {
			return true;
		}
		return false;
	}

	public static String getResourcePath(String id) {
		if (id == null) {
			return null;
		}
		IQ7NamedElement element = Q7SearchCore.findById(id);
		if (element == null) {
			return null;
		}
		return element.getResource().getFullPath().toString();
	}
	
	private static <T> List<T> copy(List<T> input) {
		return new ArrayList<>(input);
	}
}
