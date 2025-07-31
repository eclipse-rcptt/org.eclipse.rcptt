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
package org.eclipse.rcptt.internal.launching.rap.ui;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache;
import org.eclipse.rcptt.internal.launching.ext.IBundlePoolConstansts;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.launching.Q7LaunchUtils;
import org.eclipse.rcptt.launching.ext.Q7LaunchingUtil;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;
import org.eclipse.ui.IStartup;

public class CleanBundlePoolStartup implements IStartup {

	private static final String LAST_RUNTIME_VERSION = "last.runtime.version";

	public void earlyStartup() {
		Job job = new Job("Check RCPTT Runtime") {
			@Override
			public boolean belongsTo(Object family) {
				if (IBundlePoolConstansts.CLEAN_BUNDLE_POOL_JOB.equals(family)) {
					return true;
				}
				return false;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String version = RcpttCore.getRuntimeVersion();

				IPreferenceStore store = Activator.getDefault()
						.getPreferenceStore();
				String lastRuntimeVersion = store
						.getString(LAST_RUNTIME_VERSION);
				if (version == null || !version.equals(lastRuntimeVersion)) {
					if (version != null) {
						store.putValue(LAST_RUNTIME_VERSION, version);
					}
					try {
						ResourcesPlugin.getWorkspace().run(
								new IWorkspaceRunnable() {

									public void run(IProgressMonitor monitor)
											throws CoreException {

										ILaunchConfiguration[] configurations = DebugPlugin
												.getDefault()
												.getLaunchManager()
												.getLaunchConfigurations();
										for (ILaunchConfiguration cfg : configurations) {
											Q7TargetPlatformManager.delete(cfg);
											LaunchInfoCache.remove(cfg);
											Q7LaunchUtils.deleteConfigFiles(cfg);
										}
										Q7TargetPlatformManager.clear();
										Q7LaunchingUtil.cleanBundlePool(monitor);
									}
								}, monitor);
					} catch (CoreException e) {
						Activator.getDefault().log(
								"Failed to update RCPTT runtime.", e);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
}
