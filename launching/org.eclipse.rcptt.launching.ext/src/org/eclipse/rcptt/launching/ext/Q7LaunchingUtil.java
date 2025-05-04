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
package org.eclipse.rcptt.launching.ext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
import org.eclipse.equinox.p2.query.ExpressionMatchQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.pde.internal.core.target.P2TargetUtils;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.util.FileUtil;

@SuppressWarnings("restriction")
public class Q7LaunchingUtil {

	public static final String EXTERNAL_LAUNCH_TYPE = "org.eclipse.rcptt.launching.ext";

	public static ILaunchConfigurationWorkingCopy createQ7LaunchConfiguration(
			ITargetPlatformHelper target, String sutArgs, String name)
			throws CoreException {
		final ILaunchConfigurationWorkingCopy config = createLaunchConfiguration(
				target, name);
		config.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				sutArgs);
		return config;
	}

	/**
	 * Creates new launch configuration. It is based on
	 * EclipseApplicationLaunchConfiguration so all attributes are supported
	 * 
	 * @param target
	 * @return
	 * @throws CoreException
	 *             Use
	 *             {@link #createLaunchConfiguration(ITargetPlatformHelper,String)}
	 *             instead
	 */
	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(
			ITargetPlatformHelper target) throws CoreException {
		return createLaunchConfiguration(target, null);
	}

	/**
	 * Creates new launch configuration. It is based on
	 * EclipseApplicationLaunchConfiguration so all attributes are supported
	 * 
	 * @param target
	 * @param name
	 * @return
	 * @throws CoreException
	 */
	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(
			ITargetPlatformHelper target, String name) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault()
				.getLaunchManager();
		ILaunchConfigurationType type = launchManager
				.getLaunchConfigurationType(EXTERNAL_LAUNCH_TYPE);
		if (name == null) {
			name = launchManager.generateLaunchConfigurationName("AUT");
		}
		final ILaunchConfigurationWorkingCopy config = type.newInstance(null,
				name);
		config.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				Q7LaunchDelegateUtils.getWorkingDirectory(
						new File(target.getTargetPlatformProfilePath()))
						.getAbsolutePath());
		updateLaunchConfiguration(target, config);

		return config;
	}

	public static void updateLaunchConfiguration(ITargetPlatformHelper target,
			final ILaunchConfigurationWorkingCopy config) throws CoreException {
		if (target != null) {
			Q7TargetPlatformManager.setHelper(config, target);

			config.setAttribute(IQ7Launch.TARGET_PLATFORM, target.getName());

			String product = target.getDefaultProduct();
			if (product != null) {
				config.setAttribute("useProduct", true);
				config.setAttribute(IPDELauncherConstants.PRODUCT, product);
			}
			String application = target.getDefaultApplication();
			if (application != null) {
				config.setAttribute(IPDELauncherConstants.APPLICATION,
						application);
			}
			if (product == null && application == null) {
				throw new CoreException(
						Q7LaunchingPlugin
								.createStatus("Failed to launch AUT since no Product and Application ID are found."));
			}
			config.setAttribute(IQ7Launch.AUT_LOCATION,
					target.getTargetPlatformProfilePath());

			Map<String, String> iniEnvs = target.getIniEnvironment();
			Map<String, String> configEnvs = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES,
					(Map<String, String>) null);
			if (configEnvs != null) {
				iniEnvs.putAll(configEnvs);
			}
			config.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, iniEnvs);
		}
		config.setAttribute(IQ7Launch.DOCLEAR, false);
		config.setAttribute(IQ7Launch.ASKCLEAR, false);
	}

	public static void cleanBundlePool(IProgressMonitor monitor) {
		cleanBundlePool(monitor, false, 30);
	}

	public static List<File> cleanBundlePool(IProgressMonitor monitor,
			boolean full, int timeout) {
		try {
			IFileArtifactRepository bundlePool = P2TargetUtils.getBundlePool();
			return cleanFileRepository(monitor, full, timeout, bundlePool);
		} catch (Throwable e) {
			Q7LaunchingPlugin.log(
					"Failed to clean RCPTT runtime from bundle pool.", e);
			return null;
		}
	}

	public static List<File> cleanFileRepository(IProgressMonitor monitor,
			boolean full, int timeout, IFileArtifactRepository bundlePool) {
		IQueryable<IArtifactDescriptor> queryable = bundlePool
				.descriptorQueryable();

		// Collect all installed artifacts
		IQueryResult<IArtifactDescriptor> bundlePoolArtifactsQueryResult = queryable
				.query(new ExpressionMatchQuery<IArtifactDescriptor>(
						IArtifactDescriptor.class,
						ExpressionUtil.TRUE_EXPRESSION),
						new NullProgressMonitor());

		Set<IArtifactDescriptor> bundlePoolArtifacts = bundlePoolArtifactsQueryResult
				.toSet();

		Set<IArtifactDescriptor> artifactsForRemoval = new HashSet<IArtifactDescriptor>();

		if (full) {
			// Remove all artifacts from bundle pool
			artifactsForRemoval.addAll(bundlePoolArtifacts);
		} else {
			for (IArtifactDescriptor art : bundlePoolArtifacts) {
				String id = art.getArtifactKey().getId();
				if (isQ7RuntimeArtifact(id)) {
					artifactsForRemoval.add(art);
				}
			}
		}
		// Collect all undeleted artifacts
		Map<IArtifactDescriptor, File> files = new HashMap<IArtifactDescriptor, File>();
		for (IArtifactDescriptor descr : artifactsForRemoval) {
			files.put(descr, bundlePool.getArtifactFile(descr));
		}

		if (artifactsForRemoval.size() > 0) {
			// Remove artifacts using bundle pool methods.
			bundlePool
					.removeDescriptors(
							artifactsForRemoval
									.toArray(new IArtifactDescriptor[artifactsForRemoval
											.size()]), monitor);
		}
		// Remove deleted files
		removeDeletedFilesFromMap(files);

		if (files.size() > 0) {
			Q7LaunchingPlugin
					.log("Bundle pool is not cleared for first invocation. Try to delete files manually with timeout.");
			long start = System.currentTimeMillis();
			while (((start + (1000 * timeout)) > System.currentTimeMillis())
					&& files.size() > 0) {
				boolean deleteFiles = FileUtil.deleteFiles(files.values()
						.toArray(new File[files.size()]));
				if (deleteFiles) {
					break; // Since all files are deleted
				}
				try {
					Thread.sleep(1000);
				} catch (Throwable e) {
					// ignore
				}
				// Do a garbage collect here
				Runtime.getRuntime().gc();

				// Clean deleted files from map
				removeDeletedFilesFromMap(files);
				Q7LaunchingPlugin
						.log("Bundle pool is not cleared for invocation (files left:"
								+ files.size() + ")");
			}
		}

		return new ArrayList<File>(files.values());
	}

	private static void removeDeletedFilesFromMap(
			Map<IArtifactDescriptor, File> files) {
		Iterator<Entry<IArtifactDescriptor, File>> iterator = files.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<IArtifactDescriptor, File> entry = iterator.next();
			if (!entry.getValue().exists()) {
				iterator.remove();
			}
		}
	}

	private static boolean isQ7RuntimeArtifact(String id) {
		return id.contains("org.eclipse.rcptt")
				|| id.contains("org.aspectj")
				|| id.contains("org.eclipse.equinox.weaving");
	}

}
