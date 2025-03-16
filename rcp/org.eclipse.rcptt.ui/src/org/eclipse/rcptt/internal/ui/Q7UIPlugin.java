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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.tags.TagsFactory;
import org.eclipse.rcptt.core.tags.TagsRegistry;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.Q7ProjectMetadata;
import org.eclipse.rcptt.ui.tags.impl.TagsWatcher;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Q7UIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.rcptt.ui"; //$NON-NLS-1$

	// The shared instance
	private static Q7UIPlugin plugin;

	private TagsWatcher tagsWatcher;
	private TagsRegistry tags;

	/**
	 * The constructor
	 */
	public Q7UIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		tags = TagsFactory.eINSTANCE.createTagsRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		if (tagsWatcher != null) {
			RcpttCore.removeElementChangedListener(tagsWatcher);
		}
		tagsWatcher = null;
		tags = null;

		plugin = null;
		super.stop(context);
	}

	public TagsRegistry getTags() {
		if (tagsWatcher == null) {
			tagsWatcher = new TagsWatcher(tags);
			RcpttCore.addElementChangedListener(tagsWatcher);
		}
		return this.tags;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Q7UIPlugin getDefault() {
		return plugin;
	}

	public static void abort(String message, Throwable t) throws CoreException {
		throw new CoreException(createStatus(message, t));
	}

	public static void abort(String message) throws CoreException {
		throw new CoreException(createStatus(message));
	}

	public static void log(Throwable t) {
		log(t.getMessage(), t);
	}

	public static void log(String message, Throwable t) {
		getDefault().getLog().log(createStatus(message, t));
	}

	public static IStatus createStatus(String message) {
		return createStatus(message, null);
	}

	public static IStatus createStatus(Throwable t) {
		return createStatus(t.getMessage(), t);
	}

	public static IStatus createStatus(String message, Throwable t) {
		return new Status(Status.ERROR, PLUGIN_ID, message, t);
	}

	public static IWorkbenchWindow getActiveWindow() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow active = workbench.getActiveWorkbenchWindow();
		if (active == null) {
			IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
			if (windows.length == 0)
				throw new RuntimeException(
						Messages.Q7UIPlugin_NoActiveWindowErrorMsg);
			return windows[0];
		}
		return active;
	}

	public static Display getDisplay() {
		Display display = null;
		if (PlatformUI.isWorkbenchRunning())
			display = PlatformUI.getWorkbench().getDisplay();
		if (display == null)
			display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	public static void asyncExec(Runnable runnable) {
		Display display = getDisplay();
		if (display != null) {
			display.asyncExec(runnable);
		}
	}

	public static void syncExec(Runnable runnable) {
		Display display = getDisplay();
		if (display != null) {
			display.syncExec(runnable);
		}
	}

	public static boolean isInternalEdition() {
		return false;
	}

	public static boolean isImageRecognitionAllowed() {
		return false;
	}

	public static void initializeProjectContextUpdateJob() {
		RcpttCore.addElementChangedListener(new IElementChangedListener() {
			public void elementChanged(Q7ElementChangedEvent event) {
				IQ7ElementDelta delta = event.getDelta();
				IQ7NamedElement[] namedElements = delta.getNamedElements();
				for (final IQ7NamedElement e : namedElements) {
					IQ7ElementDelta childDelta = delta.getChildBy(e);
					if ((childDelta.getFlags() & IQ7ElementDelta.F_MARKERS) == 0) {
						continue;
					}
					switch (childDelta.getKind()) {
					case IQ7ElementDelta.REMOVED:
					case IQ7ElementDelta.CHANGED:
						if (e instanceof Q7ProjectMetadata) {
							ProjectContextReferencesUpdateJob job = new ProjectContextReferencesUpdateJob(
									e.getQ7Project());
							Job.getJobManager().cancel(job.getFamily());
							job.setRule(e.getQ7Project().getProject());
							job.schedule();
						}
						break;
					}
				}
			}
		});
	}
}
