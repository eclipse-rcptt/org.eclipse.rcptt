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
package org.eclipse.rcptt.launching.internal.target;

import static org.eclipse.pde.internal.build.IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR;
import static org.eclipse.pde.internal.core.TargetPlatformHelper.getDefaultBundleList;
import static org.eclipse.pde.internal.core.TargetPlatformHelper.stripPathInformation;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.log;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.logWarn;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.status;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.core.target.ProfileBundleContainer;
import org.eclipse.rcptt.launching.ext.BundleStart;
import org.eclipse.rcptt.launching.ext.OriginalOrderProperties;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;

/**
 * Detailed information about bundle sources
 * 
 * @author ivaninozemtsev
 * 
 */
@SuppressWarnings("restriction")
public class Q7Target {
	private final AutInstall install;

	public AutInstall getInstall() {
		return install;
	}

	public File getInstallLocation() {
		File result = install.getInstallLocation();
		if (result == null || !result.exists()) {
			return null;
		}
		return result;
	}

	public Q7Target(ProfileBundleContainer installation) {
		install = new AutInstall(installation);
	}

	/**
	 * Describes
	 * 
	 * @author ivaninozemtsev
	 * 
	 */
	public static class AutInstall {
		private static final String OSGI_BUNDLES = "osgi.bundles";
		private final OriginalOrderProperties config;
		public final ProfileBundleContainer container;
		public String userArea;

		public AutInstall(ProfileBundleContainer container) {
			this.container = Objects.requireNonNull(container);
			this.userArea = null;
			this.config = TargetPlatformHelper.processConfiguration(this);
		}

		public TargetBundle[] getBundles() {
			return container.getBundles();
		}

		public boolean usesSimpleConfigurator() {
			return getBundlesString().contains(BUNDLE_SIMPLE_CONFIGURATOR);
		}

		public Map<String, BundleStart> configIniBundles() {
			Map<String, BundleStart> result = new LinkedHashMap<String, BundleStart>();
			for (String entry : Splitter.on(',').split(getBundlesString())) {
				int sep = entry.indexOf('@');
				String id = sep == -1 ? entry : entry.substring(0, sep);
				String startInfo = sep == -1 ? "" : entry.substring(sep + 1);
				BundleStart bundleStart = BundleStart.DEFAULT;
				try {
					bundleStart = BundleStart.fromOsgiString(startInfo);
				} catch (Exception e) {
					logWarn(e,
							"config.ini 'osgi.bundles' bad entry: cannot parse start level and auto-start for entry '%s', using defaults instead.",
							entry);
				}
				result.put(id, bundleStart);
			}
			return result;
		}

		private String getBundlesString() {
			return stripPathInformation(MoreObjects.firstNonNull(getConfig()
					.getProperty(OSGI_BUNDLES), getDefaultBundleList()));
		}

		public File getInstallLocation() {
			try {
				return new File(container.getLocation(true));
			} catch (CoreException e) {
				log(status("Can't get AUT location", e));
				return null;
			}
		}

		public URL getInstallLocationURL() {
			try {
				return TargetPlatformHelper.buildURL(container.getLocation(true), true,
						getInstallLocation().getAbsolutePath());
			} catch (CoreException e) {
				log(status("Can't get AUT location", e));
				return null;
			}
		}

		protected OriginalOrderProperties getConfig() {
			return config;
		}
	}
}
