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
package org.eclipse.rcptt.launching.configuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.launching.EclipseApplicationLaunchConfiguration;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache;
import org.eclipse.rcptt.internal.launching.ext.IBundlePoolConstansts;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchMonitor;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformInitializer;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.ext.BundleStart;
import org.eclipse.rcptt.launching.ext.OriginalOrderProperties;
import org.eclipse.rcptt.launching.ext.Q7ExternalLaunchDelegate;
import org.eclipse.rcptt.launching.ext.Q7LaunchDelegateUtils;
import org.eclipse.rcptt.launching.internal.target.TargetPlatformHelper;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.osgi.framework.BundleException;

import com.google.common.collect.Maps;

public class Q7LaunchConfigurationDelegate extends
		EclipseApplicationLaunchConfiguration {
	private static final String SECURE_STORAGE_FILE_NAME = "secure_storage";

	// private Map<String, Object> fAllBundles;
	// private Map<Object, String> fModels;
	// private TargetPlatformHelper targetPlatform;
	private ILaunch launch;

	private ILaunchConfiguration getTargetConfiguration(
			ILaunchConfiguration configuration) throws CoreException {
		return LaunchConfigurationUtils.findLaunchConfiguration(
				configuration.getAttribute(Activator.TARGET_CONFIGURATION, ""),
				Activator.PDE_LAUNCH_CONFIG_ID);
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		this.launch = launch;

		monitor.beginTask("", 2); //$NON-NLS-1$
		Q7ExtLaunchMonitor waiter = new Q7ExtLaunchMonitor(launch);
		try {
			ILaunchConfiguration targetConfig = getTargetConfiguration(configuration);
			String targetConfigName = targetConfig.getName();
			ILaunchConfigurationWorkingCopy configurationWc = configuration
					.getWorkingCopy();

			configurationWc.setAttributes(targetConfig.getAttributes());
			configurationWc.setAttribute(Activator.TARGET_CONFIGURATION,
					targetConfigName);
			configurationWc.doSave();
			super.launch(configuration, mode, launch,
					SubMonitor.convert(monitor, 1));
			waiter.wait(monitor, TeslaLimits.getAUTStartupTimeout() / 1000);
		} catch (CoreException e) {
			waiter.handle(e);
			// no need to throw exception in case of cancel
			if (e.getStatus().getSeverity() != IStatus.CANCEL) {
				throw e;
			}
		} catch (RuntimeException e) {
			waiter.handle(e);
			throw e;
		} finally {
			waiter.dispose();
		}
		monitor.done();
	}

	@Override
	protected void manageLaunch(ILaunch launch) {
		// remove base PDE launch management
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		SubMonitor sm = SubMonitor.convert(monitor, 7);
		MultiStatus warnings = new MultiStatus(getClass(), 0, "Launching " + configuration.getName());
		try {
			Job.getJobManager().join(
					IBundlePoolConstansts.CLEAN_BUNDLE_POOL_JOB,
					sm.split(1));
		} catch (Exception e1) {
			warnings.add(Status.error("Failed to wait for bundle pool clear job", e1));
		}

		LaunchInfoCache.CachedInfo info = LaunchInfoCache.getInfo(configuration);

		String targetName = configuration.getName() + " with RCPTT";
		ITargetPlatformHelper helper = Q7TargetPlatformManager.findTarget(configuration, sm.split(1));
		
		if (helper != null) {
			IStatus status = helper.resolve(sm.split(1));
			if (status.matches(IStatus.CANCEL)) {
				throw new CoreException(status);
			}
			if (status.matches(IStatus.ERROR))  {
				helper.delete();
				helper = null;
			} else if (!status.isOK()) {
				warnings.add(status);
			}
		}

		if (helper == null) {
			helper = TargetPlatformManager
					.getCurrentTargetPlatformCopy(targetName);
			IStatus status = helper.resolve(sm.split(1));
			if (status.matches(IStatus.CANCEL)) {
				throw new CoreException(status);
			}
			if (status.matches(IStatus.ERROR))  {
				helper.delete();
				helper = null;
				throw new CoreException(status);
			} else if (!status.isOK()) {
				warnings.add(status);
			}
			status = Q7TargetPlatformInitializer.initialize(helper, sm.split(1));
			if (status.matches(IStatus.CANCEL)) {
				throw new CoreException(status);
			}
			if (status.matches(IStatus.ERROR))  {
				helper.delete();
				helper = null;
				throw new CoreException(status);
			} else if (!status.isOK()) {
				warnings.add(status);
			}
			helper.save();
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			Q7TargetPlatformManager.setHelper(wc, helper);
			configuration = wc.doSave();
		}
		if (helper != null) {
			info.target = helper;
		}

		try {
			return super.preLaunchCheck(configuration, mode, sm.split(1));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	@Override
	public String[] getVMArguments(ILaunchConfiguration config)
			throws CoreException {
		LaunchInfoCache.CachedInfo info = LaunchInfoCache.getInfo(config);
		List<String> args = new ArrayList<String>(Arrays.asList(super
				.getVMArguments(config)));
		
		ITargetPlatformHelper target = (ITargetPlatformHelper) info.target;
		
		Q7ExternalLaunchDelegate.massageVmArguments(config, args, target, launch.getAttribute(IQ7Launch.ATTR_AUT_ID));

		info.vmArgs = (String[]) args.toArray(new String[args.size()]);
		return info.vmArgs;
	}

	@Override
	public String[] getProgramArguments(ILaunchConfiguration configuration)
			throws CoreException {
		LaunchInfoCache.CachedInfo info = LaunchInfoCache.getInfo(configuration);
		if (info.programArgs != null) {
			return info.programArgs;
		}

		ArrayList<String> programArgs = new ArrayList<String>();

		programArgs.addAll(Arrays.asList(super
				.getProgramArguments(configuration)));

		try {
			// Correct osgi.install.area property key
			File config = new File(getConfigDir(configuration), "config.ini");
			OriginalOrderProperties props = new OriginalOrderProperties();

			try (BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(config))) {
				props.load(in);
			}

			String targetPlatformProfilePath = ((ITargetPlatformHelper) info.target)
					.getTargetPlatformProfilePath();
			if (targetPlatformProfilePath != null) {
				props.setProperty("osgi.install.area",
						targetPlatformProfilePath);
			}
			props.setProperty("osgi.bundles", Q7LaunchDelegateUtils
					.computeOSGiBundles(Q7ExternalLaunchDelegate
							.getBundlesToLaunch(info).latestVersionsOnly));

			try (BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(config))) {
				props.store(out, "Configuration File");
			}
		} catch (IOException e) {
			throw new CoreException(Q7ExtLaunchingPlugin.status(e));
		}
		if (configuration.getAttribute(
				IQ7Launch.OVERRIDE_SECURE_STORAGE, true)) {
			// Override existing parameter
			programArgs.add("-eclipse.keyring");
			programArgs.add(getConfigDir(configuration).toString()
					+ IPath.SEPARATOR + SECURE_STORAGE_FILE_NAME);
		}
		info.programArgs = programArgs.toArray(new String[programArgs.size()]);
		return programArgs.toArray(new String[programArgs.size()]);
	}

	@Override
	protected void preLaunchCheck(final ILaunchConfiguration configuration,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		SubMonitor sm = SubMonitor.convert(monitor, 2);
		super.preLaunchCheck(configuration, launch, sm.split(1));
		if (monitor.isCanceled()) {
			return;
		}
		LaunchInfoCache.CachedInfo info = LaunchInfoCache.getInfo(configuration);

		TargetPlatformHelper target = (TargetPlatformHelper) info.target;

		Q7ExternalLaunchDelegate.BundlesToLaunchCollector collector = new Q7ExternalLaunchDelegate.BundlesToLaunchCollector();

		for (Entry<IPluginModelBase, String> entry : Q7LaunchDelegateUtils
				.getEclipseApplicationModels(this).entrySet()) {
			try {
				collector.addInstallationBundle(entry.getKey(),
						BundleStart.fromModelString(entry.getValue()));
			} catch (BundleException | IOException e) {
				throw new CoreException(Status.error("Failed to process " + entry.getKey().getInstallLocation(), e));
			}
		}
		SubMonitor locationsMonitor = SubMonitor.convert(sm.split(1), target.size());
		
		target.getModels().forEach(m -> {
			locationsMonitor.subTask(m.model().getPluginBase().getName());
			collector.addPluginBundle(m.model(), m.startLevel());
			locationsMonitor.split(1);
		});

		Q7ExternalLaunchDelegate.BundlesToLaunch bundles = collector.getResult();

		Q7ExternalLaunchDelegate.setBundlesToLaunch(info, bundles);

		Q7LaunchDelegateUtils.setDelegateFields(this, bundles.fModels, Maps.transformValues(bundles.fAllBundles.asMap(), ArrayList::new));
		monitor.done();
	}
}
