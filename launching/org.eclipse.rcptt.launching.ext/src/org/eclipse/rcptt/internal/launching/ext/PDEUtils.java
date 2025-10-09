/*******************************************************************************
 * Copyright (c) 2009, 2020 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.launching.ext;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.launching.PDEMessages;
import org.eclipse.pde.internal.launching.launcher.BundleLauncherHelper;
import org.eclipse.pde.internal.launching.launcher.VMHelper;

@SuppressWarnings("restriction")
public class PDEUtils {
	
	// Eclipse 4.8 doesn't contain this item.
	public static final String BUNDLE_UPDATE_CONFIGURATOR = "org.eclipse.update.configurator"; //$NON-NLS-1$
	public static final String NO_STARTUP_JAR_MESSAGE = PDEMessages.WorkbenchLauncherConfigurationDelegate_noStartup;
	
	public static void addBundleToMap(Map<Object, String> map,
			IPluginModelBase bundle, String sl) {
		BundleDescription desc = bundle.getBundleDescription();
		boolean defaultsl = (sl == null || sl.equals("default:default")); //$NON-NLS-1$
		if (desc != null && defaultsl) {
			String modelName = desc.getSymbolicName();
			if (IPDEBuildConstants.BUNDLE_DS.equals(modelName)) {
				map.put(bundle, "2:true"); //$NON-NLS-1$ 
			} else if (IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR
					.equals(modelName)) {
				map.put(bundle, "1:true"); //$NON-NLS-1$
			} else if (IPDEBuildConstants.BUNDLE_EQUINOX_COMMON
					.equals(modelName)) {
				map.put(bundle, "2:true"); //$NON-NLS-1$
			} else if (IPDEBuildConstants.BUNDLE_OSGI.equals(modelName)) {
				map.put(bundle, "-1:true"); //$NON-NLS-1$
			} else if (BUNDLE_UPDATE_CONFIGURATOR
					.equals(modelName)) {
				map.put(bundle,
						BundleLauncherHelper.DEFAULT_UPDATE_CONFIGURATOR_START_LEVEL);
			} else if (IPDEBuildConstants.BUNDLE_CORE_RUNTIME.equals(modelName)) {
				if (TargetPlatformHelper.getTargetVersion() >= 4.3) {
					map.put(bundle, "default:true"); //$NON-NLS-1$
				} else {
					map.put(bundle, "2:true"); //$NON-NLS-1$
				}
			} else {
				map.put(bundle, sl);
			}
		} else {
			map.put(bundle, sl);
		}
	}
	
	public static IVMInstall getVMInstall(ILaunchConfiguration configuration, Set<IPluginModelBase> plugins) throws CoreException {
		return VMHelper.getVMInstall(configuration, plugins);
	}
	
	public static Stream<String> startupPackageNames() {
		return Stream.of(IPDEBuildConstants.BUNDLE_EQUINOX_LAUNCHER);
	}

}
