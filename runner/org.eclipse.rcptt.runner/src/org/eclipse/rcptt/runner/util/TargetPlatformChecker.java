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
package org.eclipse.rcptt.runner.util;

import static org.eclipse.rcptt.runner.HeadlessRunnerPlugin.PLUGIN_ID;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ext.PDELocationUtils;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformInitializer;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformInitializer.Q7Info;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.launching.ext.JvmTargetCompatibility;
import org.eclipse.rcptt.launching.injection.InjectionConfiguration;
import org.eclipse.rcptt.launching.injection.InjectionFactory;
import org.eclipse.rcptt.launching.injection.UpdateSite;
import org.eclipse.rcptt.launching.internal.target.PDEHelper;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;
import org.eclipse.rcptt.runner.HeadlessRunnerPlugin;
import org.eclipse.rcptt.runner.PrintStreamMonitor;
import org.eclipse.rcptt.runner.Q7PluginValidator;
import org.eclipse.rcptt.runner.RunnerConfiguration;

public class TargetPlatformChecker {

	private final RunnerConfiguration conf;

	public TargetPlatformChecker(RunnerConfiguration conf) {
		this.conf = conf;
	}


	private ITargetPlatformHelper targetPlatform;
	private JvmTargetCompatibility compatibility;

	public ITargetPlatformHelper getTargetPlatform() {
		return targetPlatform;
	}

	public void initAndCheckTargetPlatform() throws CoreException {

		if (conf.location == null) {
			throw new CoreException(createError("AUT location is not set"));
		}

		IStatus locationStatus = PDELocationUtils.validateProductLocation(conf.location);
		if (!locationStatus.isOK()) {
			MultiStatus rv = new MultiStatus(HeadlessRunnerPlugin.PLUGIN_ID, 0, "AUT location is invalid: " + conf.location, null);
			rv.add(locationStatus);
			throw new CoreException(rv);
		}

		System.out.println("Initializing target platform...");
		initializeTargetPlatform();
		System.out.println("Target platform is valid.");
	}
	
	private static IStatus createError(String message) {
		return new Status(IStatus.ERROR, HeadlessRunnerPlugin.PLUGIN_ID, message, new RuntimeException());
	}

	private void initializeTargetPlatform() throws CoreException {
		targetPlatform = null;
		compatibility  = null;
		String location = PDELocationUtils.getProductLocation(conf.location).getAbsolutePath();
		PrintStreamMonitor outMonitor = new PrintStreamMonitor(true);
		if (conf.config != null) {
			targetPlatform = TargetPlatformManager.createTargetPlatform(location, outMonitor);
			Map<String, Version> versions = targetPlatform.getVersions();
			Q7Info q7Info = Q7TargetPlatformInitializer.getInfo(targetPlatform, versions);
			if (!conf.onlySpecified) {
				IMetadataRepository repository = PDEHelper
						.safeLoadRepository(q7Info.q7,
								outMonitor);
				if (repository != null) {
					InjectionConfiguration configuration = Q7TargetPlatformInitializer
							.createInjectionConfiguration(
									outMonitor, q7Info,
									versions);
					if (configuration != null) {
						conf.config.getEntries().addAll(
								configuration.getEntries());
					}
				}
			}
			else if (q7Info.aspectj != null) {
				UpdateSite aspectsSite = InjectionFactory.eINSTANCE.createUpdateSite();
				aspectsSite.setUri(q7Info.aspectj.toString());
				conf.config.getEntries().add(aspectsSite);
			}
			IStatus rv = targetPlatform.applyInjection(conf.config, outMonitor);
			if (!rv.isOK())
				throw new CoreException(rv);
			if (targetPlatform.getWeavingHook() == null) {
				throw new CoreException(
						RcpttPlugin.createStatus("No org.eclipse.weaving hook plugin in target platform"));
			}
		} else { // Try to initialize using Q7 bundled runtime
			targetPlatform = Q7TargetPlatformManager.createTargetPlatform(
					location, outMonitor);
		}
		// Check for few core q7, tesla plugins are available.
		if (!Q7PluginValidator.validate(targetPlatform)) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Target platform doesn't contain RCPTT runtime injected..."));
		}
		compatibility = new JvmTargetCompatibility(targetPlatform);
	}

	public JvmTargetCompatibility getCompatibility() {
		return compatibility;
	}

}
