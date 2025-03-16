/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching.target;

import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.PLUGIN_ID;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.P2TargetUtils;
import org.eclipse.pde.internal.core.target.TargetPlatformService;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ext.PDELocationUtils;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin;
import org.eclipse.rcptt.launching.internal.target.PDEHelper;
import org.eclipse.rcptt.launching.internal.target.TargetPlatformHelper;
import org.osgi.framework.Version;

/**
 * AUT target platform management.
 * 
 */
@SuppressWarnings("restriction")
public class TargetPlatformManager {
	private final static ILog LOG = Platform.getLog(TargetPlatformManager.class);
	
	private static void throwOnError(IStatus status) throws CoreException {
		if (status.matches(IStatus.ERROR))
			throw new CoreException(status);
		if (!status.isOK())
			Q7ExtLaunchingPlugin.log(status);
	}

	/**
	 * Creates new target platform based on specified AUT location
	 * 
	 * @throws CoreException
	 */
	public static ITargetPlatformHelper createTargetPlatform(final String location, IProgressMonitor monitor)
			throws CoreException {
		boolean isOk = false;
		final ITargetPlatformService service = PDEHelper.getTargetService();
		final ITargetDefinition target = service.newTarget();
		final TargetPlatformHelper info = new TargetPlatformHelper(target);
		try {
			final List<ITargetLocation> containers = new ArrayList<ITargetLocation>();

			final File productDir = PDELocationUtils.getProductLocation(location);
			final ITargetLocation installationContainer = service
					.newProfileLocation(productDir.getAbsolutePath(), null);
			info.getQ7Target().setInstall(installationContainer);
			containers.add(installationContainer);

			final File pluginsDir = PDELocationUtils.getPluginFolder(location);
			final ITargetLocation pluginsContainer = service
					.newDirectoryLocation(pluginsDir.getAbsolutePath());
			containers.add(pluginsContainer);

			final String localLocation = info.getUserArea();
			if (localLocation != null) {
				/*
				 * final File localProductDir = PDELocationUtils.getProductLocation(location);
				 * final ITargetLocation localInstallationContainer = service
				 * .newProfileLocation(localProductDir.getAbsolutePath(), null);
				 * containers.add(localInstallationContainer);
				 */

				try {
					final File localPluginsDir = PDELocationUtils.getPluginFolder(localLocation);
					final ITargetLocation localPluginsContainer = service
							.newDirectoryLocation(localPluginsDir.getAbsolutePath());
					containers.add(localPluginsContainer);
				} catch (CoreException e) {
					if(e.getStatus().getCode() == EFS.ERROR_NOT_EXISTS) {
						Q7ExtLaunchingPlugin.log(new Status(IStatus.INFO, PLUGIN_ID, EFS.ERROR_NOT_EXISTS, localLocation + " does not have plugins.", e));
					} else
						throw e;
				}
			}

			info.setBundleContainers(containers
					.toArray(new ITargetLocation[containers.size()]));
			throwOnError(info.resolve(monitor));
			isOk = true;
			return info;
		} catch (StackOverflowError e) {
			// StackOverflowError might happen in xerces
			// throwsProductLocation(location, e);
			throw createErrorProductLocationException(location, e);
		} catch (CoreException e) {
			throw e;
		} catch (Throwable e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
		} finally {
			if (!isOk)
				info.delete();
		}
	}

	/**
	 * Restore target platform from existing configuration
	 * 
	 * @param attribute
	 * @return null if no target platform is found. Helper object otherwise.
	 * @throws CoreException
	 */
	public static ITargetPlatformHelper findTarget(
			final String requiredName, final IProgressMonitor monitorArg,
			final boolean needResolve) throws CoreException {
		SubMonitor monitor = SubMonitor.convert(monitorArg);
		monitor.beginTask("Looking up " + requiredName, 2);

		MultiStatus result = new MultiStatus(TargetPlatformManager.class, 0, "Problems while loading target definitions") {
			@Override
			protected void setSeverity(int severity) {
				if (severity == IStatus.ERROR) {
					severity = IStatus.WARNING;
				}
				super.setSeverity(severity);
			}
		};
		try {
			ITargetHandle[] targets = PDEHelper.getTargetService().getTargets(
					monitor.newChild(1));
			for (ITargetHandle handle : targets) {
				if (monitor.isCanceled()) {
					return null;
				}
				ITargetDefinition def;
				
				try {
					def = handle.getTargetDefinition();
				} catch (CoreException e) {
					result.add(e.getStatus());
					continue;
				}
				String name = def.getName();
				if (name == null || !name.equals(requiredName))
					continue;
				final TargetPlatformHelper info = new TargetPlatformHelper(def);
				if (needResolve) {
					IStatus status = info.resolve(monitor.newChild(1, SubMonitor.SUPPRESS_NONE));
					if (!status.isOK()) {
						LaunchingPlugin.log(status);
						info.delete();
						return null;
					}
				}
				return info;
			}
			return null;
		} finally {
			monitor.done();
			if (!result.isOK()) {
				LOG.log(result);
			}
		}
	}

	/**
	 * Delete target platform with specific name.
	 * 
	 * @param tName
	 */
	public static void deleteTargetPlatform(String tName) {
		try {
			// Remove previous target platforms with
			// same name.

			ITargetPlatformService service = PDEHelper.getTargetService();
			ITargetHandle[] handles = service
					.getTargets(new NullProgressMonitor());
			List<ITargetHandle> toRemove = new ArrayList<ITargetHandle>();
			for (ITargetHandle iTargetHandle : handles) {
				if (iTargetHandle.exists()) {
					ITargetDefinition def = getTargetDefinition(iTargetHandle);
					if (def != null && def.getName() != null
							&& def.getName().equals(tName)) {
						toRemove.add(iTargetHandle);
					}
				}
			}
			for (ITargetHandle iTargetHandle : toRemove) {
				service.deleteTarget(iTargetHandle);
			}
		} catch (CoreException e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
		}
	}

	public static void clearTargets() {
		ITargetPlatformService targetService = PDEHelper.getTargetService();
		TargetPlatformService s = (TargetPlatformService) targetService;

		Version version = RcpttCore.getPlatformVersion();
		if (version.getMajor() == 3 && version.getMinor() == 6) {
			try {
				// s.cleanOrphanedTargetDefinitionProfiles();
				Method method = TargetPlatformService.class
						.getDeclaredMethod("cleanOrphanedTargetDefinitionProfiles");
				method.invoke(s);

				// s.garbageCollect();
				method = TargetPlatformService.class
						.getDeclaredMethod("garbageCollect");
				method.invoke(s);

			} catch (Throwable e) {
				RcpttPlugin.log(e);
			}
		} else if (version.getMajor() == 3 && version.getMinor() == 7) {
			try {
				P2TargetUtils.cleanOrphanedTargetDefinitionProfiles();
				P2TargetUtils.garbageCollect();
			} catch (Throwable e) {
				RcpttPlugin.log(e);
			}
		}
	}

	private static ITargetDefinition getTargetDefinition(ITargetHandle handle) {
		try {
			if (handle == null) {
				return null;
			}
			return handle.getTargetDefinition();
		} catch (CoreException e) {
			Q7ExtLaunchingPlugin.logWarn(e, "Error loading target definition of %s handle (%s)",
					handle.getClass().getName(), handle.toString());
			return null;
		}
	}

	public static ITargetPlatformHelper getCurrentTargetPlatform() {
		ITargetPlatformService targetService = PDEHelper.getTargetService();
		TargetPlatformService s = (TargetPlatformService) targetService;
		try {
			ITargetHandle handle = s.getWorkspaceTargetHandle();
			if (handle != null) {
				TargetPlatformHelper helper = new TargetPlatformHelper(getTargetDefinition(handle)) {
					@Override
					public IStatus resolve(IProgressMonitor monitor) {
						// Always resolved platform
						return Status.OK_STATUS;
					};
				};
				if (helper.getStatus().isOK() && helper.getTargetPlatformProfilePath() != null) {
					return helper;
				}
			}
			ITargetDefinition selfAUT = s.newDefaultTarget();
			selfAUT.setName("selfAUT_" + System.currentTimeMillis());
			s.saveTargetDefinition(selfAUT);
			TargetPlatformHelper helper = new TargetPlatformHelper(selfAUT) {
				@Override
				public IStatus resolve(IProgressMonitor monitor) {
					// Always resolved platform
					return Status.OK_STATUS;
				}
			};
			return helper;
		} catch (CoreException e) {
			RcpttPlugin.log(e);
		}

		return null;
	}

	public static TargetPlatformHelper getCurrentTargetPlatformCopy(
			String copyName) {
		ITargetPlatformService targetService = PDEHelper.getTargetService();
		TargetPlatformService s = (TargetPlatformService) targetService;

		try {
			ITargetHandle handle = s.getWorkspaceTargetHandle();
			if (handle != null) {
				ITargetDefinition targetCopy = s.newTarget();
				ITargetDefinition targetSource = getTargetDefinition(handle);
				if (targetSource == null) {
					return null;
				}
				s.copyTargetDefinition(targetSource, targetCopy);
				targetCopy.setName(copyName);
				TargetPlatformHelper helper = new TargetPlatformHelper(targetCopy);
				return helper;
			}
		} catch (CoreException e) {
			RcpttPlugin.log(e);
		}
		return null;
	}

	private static CoreException createErrorProductLocationException(String location, Throwable e)
			throws CoreException {
		return new CoreException(
				new Status(IStatus.ERROR, PLUGIN_ID, String.format("Invalid eclipse product location: %s",
						location), e));
	}

}
