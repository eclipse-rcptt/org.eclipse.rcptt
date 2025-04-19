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
package org.eclipse.rcptt.internal.launching.ext;

import static java.util.stream.Stream.of;
import static org.eclipse.core.runtime.IProgressMonitor.done;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutListener;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;

import com.google.common.base.Strings;

public class Q7TargetPlatformManager {

	private static Map<String, ITargetPlatformHelper> cachedHelpers = Collections.synchronizedMap(new HashMap<>());
	private static final ILog LOG = Platform.getLog(Q7TargetPlatformManager.class);
	
	static {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		AutManager.INSTANCE.addListener(new AutListener.AutAdapter() {
			@Override
			public void autRemoved(Aut aut) {
				delete(aut.getConfig());
			}
		});
	}

	public synchronized static ITargetPlatformHelper findTarget(
			ILaunchConfiguration config, IProgressMonitor monitor)
			throws CoreException {
		String location = config.getAttribute(IQ7Launch.AUT_LOCATION, "");

		File loc = new File(location);
		if (!loc.exists()) {
			return null;
		}

		ITargetPlatformHelper cached = familyOf(config).map(Q7TargetPlatformManager::getTargetPlatformName)
				.map(cachedHelpers::get).filter(Objects::nonNull).findFirst().orElse(null);

		if (cached != null) {
			return cached;
		}

		cached = familyOf(config).map(Q7TargetPlatformManager::getTargetPlatformName)
				.map(name ->  TargetPlatformManager.findTarget(name, monitor))
				.filter(Objects::nonNull)
				.peek(tp -> tp.getStatus().isOK())
				.findFirst().orElse(null);
		done(monitor);
		cachedHelpers.put(getTargetPlatformName(config), cached);
		return cached;
	}

	/**
	 * Return existing target or re evaluate and create one new
	 * 
	 * @param config
	 * @param shell
	 * @return
	 * @throws CoreException
	 */
	public synchronized static ITargetPlatformHelper getTarget(
			ILaunchConfiguration config, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor sm = SubMonitor.convert(monitor, "Initialize target platform...", 3);
		String location = config.getAttribute(IQ7Launch.AUT_LOCATION, "");

		if (!PDELocationUtils.validateProductLocation(location).isOK()) {
			return newTargetPlatform(config, sm.split(1), location);
		}
		
		ITargetPlatformHelper result = findTarget(config, sm.split(1));
		if (result != null) {
			return result;
		}

	    ITargetPlatformHelper info = newTargetPlatform(config, sm.split(1), location);
	    assert findTarget(config, sm.split(1)) == info;
		done(monitor);
		return info;
	}

	private synchronized static ITargetPlatformHelper newTargetPlatform(
			ILaunchConfiguration config, IProgressMonitor monitor,
			String location) throws CoreException {

		String name = getTargetPlatformName(config);
		ITargetPlatformHelper info = Q7TargetPlatformManager.createTargetPlatform(location, name, monitor);
		assert info != null;
		info.save();
		cachedHelpers.put(info.getName(), info);
		return info;
	}

	private static void throwOnError(IStatus status) throws CoreException {
		if (status.matches(IStatus.ERROR))
			throw new CoreException(status);
		if (!status.isOK())
			Q7ExtLaunchingPlugin.log(status);
	}

	public synchronized static ITargetPlatformHelper createTargetPlatform(
			String location, String name, IProgressMonitor monitor) throws CoreException {
		boolean isOk = false;
		if (monitor.isCanceled()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		ITargetPlatformHelper platform = null;
		try {
			SubMonitor subMonitor = SubMonitor.convert(monitor, "Create AUT configuration", 100);
			platform = TargetPlatformManager
					.createTargetPlatform(location, name, subMonitor.split(50));
			throwOnError(platform.getStatus());
			IStatus rv = Q7TargetPlatformInitializer.initialize(platform, subMonitor.split(50));
			throwOnError(rv);
			isOk = true;
			return platform;
		} catch (CoreException e) {
			throw e;
		} catch (Throwable e) {
			throw new CoreException(new Status(IStatus.ERROR, Q7ExtLaunchingPlugin.PLUGIN_ID,
					"Target platform initialization failed", e));
		} finally {
			if (!isOk && platform != null)
				platform.delete();
			monitor.done();
		}
	}

	/**
	 * Return target platform name. This method will by default use launch
	 * configuration name.
	 * 
	 * @param config
	 * @return
	 * @throws CoreException
	 */
	private static String getTargetPlatformName(ILaunchConfiguration config) {
		String defValue = getTargetPlatformName(config.getName());
		if (!config.exists())
			return defValue;
		try {
			return config.getAttribute(IQ7Launch.TARGET_PLATFORM, defValue);
		} catch (CoreException e) {
			RcpttPlugin.log(e);
			return defValue;
		}
	}

	private static String getTargetPlatformName(String name) {
		if (Strings.isNullOrEmpty(name)) {
			throw new IllegalArgumentException();
		}
		return "AUT " + name + " (Target Platform)";
	}

	public synchronized static void initialize() {
		DebugPlugin
				.getDefault()
				.getLaunchManager()
				.addLaunchConfigurationListener(
						new ILaunchConfigurationListener() {
							public void launchConfigurationRemoved(
									ILaunchConfiguration configuration) {
								delete(configuration);
							}

							public void launchConfigurationChanged(
									ILaunchConfiguration configuration) {
								// String target =
								// getTargetPlatformName(configuration);
								// if (target != null) {
								// cachedHelpers.remove(target);
								// }
							}

							public void launchConfigurationAdded(
									ILaunchConfiguration configuration) {
							}
						});
	}

	public synchronized static void setHelper(ILaunchConfigurationWorkingCopy config, ITargetPlatformHelper info) {
		config.setAttribute(IQ7Launch.TARGET_PLATFORM, info.getName());
		cachedHelpers.put(info.getName(), info);
	}

	public synchronized static void delete(ILaunchConfiguration configuration) {
		familyOf(configuration).map(Q7TargetPlatformManager::getTargetPlatformName)
			.forEach(Q7TargetPlatformManager::delete);

	}

	private synchronized static void delete(String name) {
		ITargetPlatformHelper helper = cachedHelpers.remove(name);
		if (helper != null) {
			helper.delete();
		}
		TargetPlatformManager.deleteTargetPlatform(name);
	}

	public synchronized static void clear() {
		cachedHelpers.clear();
	}
	
	private static Stream<ILaunchConfiguration> familyOf(ILaunchConfiguration configuration) {
		Stream<ILaunchConfigurationWorkingCopy> parents = Stream.empty();
		ILaunchConfiguration original = null;
		if (configuration instanceof ILaunchConfigurationWorkingCopy wc) {
			parents = Stream.iterate(wc, Objects::nonNull, ILaunchConfigurationWorkingCopy::getParent);
			original = wc.getOriginal();
		}
		return Stream.of(of(configuration), parents, of(original)).flatMap(Function.identity()).filter(Objects::nonNull);
	}
}
