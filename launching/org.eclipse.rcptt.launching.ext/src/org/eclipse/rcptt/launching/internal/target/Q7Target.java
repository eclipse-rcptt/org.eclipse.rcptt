/*******************************************************************************
 * Copyright (c) 2009, 2014 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching.internal.target;

import static com.google.common.collect.Iterables.filter;
import static java.lang.String.format;
import static org.eclipse.pde.internal.build.IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR;
import static org.eclipse.pde.internal.core.TargetPlatformHelper.getDefaultBundleList;
import static org.eclipse.pde.internal.core.TargetPlatformHelper.stripPathInformation;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.log;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.logWarn;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.status;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
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
	private AutInstall install;

	public AutInstall getInstall() {
		return install;
	}

	public File getInstallLocation() {
		if (install == null) {
			return null;
		}
		File result = install.getInstallLocation();
		if (result == null || !result.exists()) {
			return null;
		}
		return result;
	}

	public void setInstall(ITargetLocation installation) {
		if (!(installation instanceof ProfileBundleContainer)) {
			log(status(format(
					"%s is set as an installation container, but ProfileBundleContainer expected",
					installation)));
			install = null;
		}
		install = new AutInstall((ProfileBundleContainer) installation);
	}

	/**
	 * Plugins directory, may be <code>null</code>
	 */
	public ITargetLocation pluginsDir;
	/**
	 * Q7 Runtime, Q7 runtime dependencies, other injections
	 */
	private List<ITargetLocation> extras = new ArrayList<ITargetLocation>();

	public void addExtra(ITargetLocation container) {
		if (!contains(container)) {
			extras.add(container);
		}
	}

	public Iterable<ITargetLocation> getExtras() {
		return extras;
	}

	private boolean contains(ITargetLocation container) {
		if (extras.contains(container)) {
			return true;
		}

		if (!(container instanceof IUBundleContainer)) {
			return false;
		}

		IUBundleContainer cont = (IUBundleContainer) container;
		for (IUBundleContainer existing : filter(extras,
				IUBundleContainer.class)) {
			if (Arrays.equals(existing.getRepositories(),
					cont.getRepositories())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Describes
	 * 
	 * @author ivaninozemtsev
	 * 
	 */
	public static class AutInstall {
		private static String OSGI_BUNDLES = "osgi.bundles";
		public final ProfileBundleContainer container;

		public AutInstall(ProfileBundleContainer container) {
			this.container = container;

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

		private OriginalOrderProperties config;

		protected OriginalOrderProperties getConfig() {
			if (config == null) {
				try {
					config = OriginalOrderProperties.fromFile(new File(
							getInstallLocation(), "configuration/config.ini"));
				} catch (IOException e) {
					log(status("Error reading config.ini", e));
					config = new OriginalOrderProperties();
				}
			}
			return config;
		}
	}
}
