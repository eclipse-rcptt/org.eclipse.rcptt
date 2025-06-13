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
package org.eclipse.rcptt.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.util.NetworkUtils;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RcpttPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.rcptt.core";

	public static final String COMMANDS_EXECUTION_DELAY = "COMMANDS_EXECUTION_DELAY";

	// The shared instance
	private static RcpttPlugin plugin;

	/**
	 * The constructor
	 */
	public RcpttPlugin() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		NetworkUtils.initTimeouts();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			WorkspaceMonitor.getInstance().stop();
			ModelManager.getModelManager().shutdown();
		} catch (Throwable e) {
			// yeah, we're going to ignore even ClassNoDefFound exceptions
			log("Couldn't shutdown model manager", e);
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RcpttPlugin getDefault() {
		return plugin;
	}

	public static IStatus createStatus(String message) {
		return createStatus(message, null);
	}

	public static IStatus createStatus(Throwable t) {
		if (t instanceof CoreException) {
			IStatus result = ((CoreException) t).getStatus();
			if (result.getException() != null || result.getChildren().length != 0)
				return result;
		}
		return new Status(Status.ERROR, PLUGIN_ID, t.getMessage(), t);
	}

	public static IStatus createStatus(String message, Throwable t) {
		if (t != null) {
			if (message == null || message.equals(t.getMessage())) {
				return createStatus(t);
			}
		}
		if (t instanceof CoreException) {
			return new MultiStatus(((CoreException) t).getStatus().getPlugin(), 0, new IStatus[] { createStatus(t) }, message, null);
		}
		return new Status(Status.ERROR, PLUGIN_ID, message, t);
	}

	public static void log(Throwable t) {
		getDefault().getLog().log(createStatus(t));
	}

	@SuppressWarnings("deprecation")
	public static IEclipsePreferences getPreferences() {
		return new InstanceScope().getNode(PLUGIN_ID);
	}

	@SuppressWarnings("deprecation")
	public static IEclipsePreferences getDefaultPreferences() {
		return new DefaultScope().getNode(PLUGIN_ID);
	}

	public static void log(String msg, Throwable e) {
		RcpttPlugin def = getDefault();
		if (def != null) {
			def.getLog().log(createStatus(msg, e));
		}
	}

	public static void infoLog(String msg) {
		RcpttPlugin def = getDefault();
		if (def != null) {
			def.getLog().log(new Status(Status.INFO, PLUGIN_ID, msg));
		}
	}

	public static ProcessStatus createProcessStatus(int severity, String message) {
		ProcessStatus status = CoreFactory.eINSTANCE.createProcessStatus();
		status.setSeverity(severity);
		status.setPluginId(PLUGIN_ID);
		status.setMessage(message);
		return status;
	}

	public static void throwOnError(IStatus status) throws CoreException {
		if (status.isOK() || status.getSeverity() == IStatus.INFO)
			return;
		throw new CoreException(status);
	}

}
