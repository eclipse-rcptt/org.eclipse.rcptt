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
package org.eclipse.rcptt.internal.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IQ7ProjectMetadata;
import org.eclipse.rcptt.core.model.search.ISearchScope;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.scenario.GroupContext;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.OneProjectScope;
import org.eclipse.rcptt.ui.dialogs.RemoveAllProjectReferencesDialog;
import org.eclipse.rcptt.ui.utils.WriteAccessChecker;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ProjectContextReferencesUpdateJob extends Job {
	private IQ7Project project;
	private static final ISchedulingRule DO_NOT_SHOW_MULTIPLE_DIALOGS_AT_ONCE = new ISchedulingRule() {
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};

	public ProjectContextReferencesUpdateJob(IQ7Project project) {
		super("Update Project Context/Verification references");
		this.project = project;

		setRule(new MultiRule(DO_NOT_SHOW_MULTIPLE_DIALOGS_AT_ONCE, project.getProject()));
	}

	@Override
	public boolean belongsTo(Object family) {
		return super.belongsTo(family);
	}

	private static Collection<IQ7NamedElement> filterOutProjectMetadata(IQ7NamedElement[] elements) {
		return Collections2.filter(Arrays.asList(elements),
				new Predicate<IQ7NamedElement>() {
					public boolean apply(IQ7NamedElement input) {
						return input.getElementType() != HandleType.ProjectMetadata;
					}
				});
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (RemoveAllProjectReferencesDialog.isNever()) {
				return Status.OK_STATUS;
			}

			SubMonitor sm = SubMonitor.convert(monitor, 3);

			IQ7ProjectMetadata metadata = project.getMetadata();
			final Set<IQ7NamedElement> elementsToProceed = new HashSet<IQ7NamedElement>();
			ISearchScope scope = new OneProjectScope(project);

			String[] contexts = metadata.exists() ? metadata.getContexts() : new String[0];
			for (String el : contexts) {
				IQ7NamedElement[] elements = Q7SearchCore.findContextUsage(el,
						scope, sm.split(1));
				if (elements != null) {
					elementsToProceed.addAll(filterOutProjectMetadata(elements));
				}

			}

			String[] verifications = metadata.getVerifications();
			for (String el : verifications) {
				IQ7NamedElement[] elements = Q7SearchCore.findVerificationUsage(el,
						scope, sm.split(1));
				if (elements != null) {
					elementsToProceed.addAll(filterOutProjectMetadata(elements));
				}

			}

			Shell shell = null;
			ArrayList<IQ7NamedElement> copy = new ArrayList<IQ7NamedElement>(
					elementsToProceed);
			if (elementsToProceed.size() > 0) {
				shell = computeInDisplay(PlatformUI.getWorkbench().getDisplay(), () -> {
					Shell shell2 = PlatformUI.getWorkbench()
							.getWorkbenchWindows()[0]
									.getShell();
					if (RemoveAllProjectReferencesDialog.open(
							shell2, project, copy)) {
						return shell2;
					}
					return null; // User requests no action
				});
			}
			if (shell != null) {
				return removeDefaultReferences(project, copy, shell, sm.split(1));
			}
		} catch (

		Exception e) {
			RcpttPlugin.log(e);
		}
		return Status.OK_STATUS;
	}

	@SuppressWarnings("unchecked")
	private static <T> T computeInDisplay(Display display, Callable<T> supplier) throws Exception {
		Object[] result = new Object[1];
		Exception[] error = new Exception[1];
		display.syncExec(() -> {
			try {
				result[0] = supplier.call();
			} catch (Exception e) {
				error[0] = e;
			}
		});
		if (error[0] != null) {
			throw error[0];
		}
		return (T) result[0];
	}

	private static IStatus removeDefaultReferences(final IQ7Project project,
			final List<IQ7NamedElement> references, Shell shell, IProgressMonitor monitorArg) {
		final IStatus[] status = { Status.OK_STATUS };
		try {
			IQ7ProjectMetadata metadata = project.getMetadata();
			if (metadata == null || !metadata.exists()) {
				return status[0];
			}
			String[] defaultContexts = metadata.getContexts();
			String[] defaultVerifications = metadata.getVerifications();
			if (defaultContexts.length == 0 && defaultVerifications.length == 0) {
				return status[0];
			}
			final List<String> contextsToRemove = Arrays.asList(defaultContexts);
			final List<String> verificationsToRemove = Arrays.asList(defaultVerifications);

			WriteAccessChecker writeAccessChecker = new WriteAccessChecker(shell);
			ResourcesPlugin.getWorkspace().run(
					new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor)
								throws CoreException {
							SubMonitor sm = SubMonitor.convert(monitor,
									"Remove project context/verification references", references.size() * 2);
							try {
								boolean result = computeInDisplay(shell.getDisplay(), () ->  writeAccessChecker.makeResourceWritable(references
										.toArray(new IQ7NamedElement[0])));
								if (!result) {
									status[0] = Status.CANCEL_STATUS;
									return;
								}
							} catch (Exception e) {
								status[0] = Status.error("Failed to write " + references, e);
								return;
							}
							for (IQ7NamedElement e : references) {
								IQ7NamedElement copy = e.getWorkingCopy(sm.split(1));
								try {
									NamedElement ne = copy.getNamedElement();
									if (ne instanceof Scenario) {
										((Scenario) ne)
												.getContexts()
												.removeAll(
														contextsToRemove);
										((Scenario) ne)
												.getVerifications()
												.removeAll(
														verificationsToRemove);
									}
									if (ne instanceof GroupContext) {
										((GroupContext) ne)
												.getContextReferences()
												.removeAll(
														contextsToRemove);
									}
									copy.commitWorkingCopy(false,
											sm.split(1));
								} finally {
									copy.discardWorkingCopy();
								}
							}
						}
					}, project.getProject(), IWorkspace.AVOID_UPDATE, monitorArg);
		} catch (CoreException e) {
			return e.getStatus();
		}
		return status[0];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectContextReferencesUpdateJob other = (ProjectContextReferencesUpdateJob) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}

	public Object getFamily() {
		return this;
	}
}
