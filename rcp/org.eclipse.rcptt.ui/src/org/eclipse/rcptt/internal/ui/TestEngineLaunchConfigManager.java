/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.ui.launching.ITestEngineLaunchConfig;

public class TestEngineLaunchConfigManager {
	private static TestEngineLaunchConfigManager instance;

	public static TestEngineLaunchConfigManager getInstance() {
		if (instance == null) {
			instance = new TestEngineLaunchConfigManager();
		}
		return instance;
	}

	private final static String TESTENGINE_UI_EXTPT = "org.eclipse.rcptt.ui.testEngineLaunchConfigs";
	private final static String TESTENGINE_UI_ENGINEID_ATTR = "testEngineId";
	private final static String TESTENGINE_UI_CLASS_ATTR = "class";

	public class TestEngineLaunchConfig {
		private String id;
		private ITestEngineLaunchConfig config;

		public TestEngineLaunchConfig(String id, ITestEngineLaunchConfig config) {
			this.id = id;
			this.config = config;
		}

		public String getId() {
			return id;
		}

		public ITestEngineLaunchConfig getConfig() {
			return config;
		}
	}

	private List<TestEngineLaunchConfig> launchConfigs;

	public TestEngineLaunchConfigManager() {
		final List<TestEngineLaunchConfig> launchConfigs = new ArrayList<TestEngineLaunchConfig>();
		final IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(TESTENGINE_UI_EXTPT);
		for (final IConfigurationElement element : elements) {
			final String id = element.getAttribute(TESTENGINE_UI_ENGINEID_ATTR);
			try {
				final ITestEngineLaunchConfig paramGroup = (ITestEngineLaunchConfig) element
						.createExecutableExtension(TESTENGINE_UI_CLASS_ATTR);
				launchConfigs.add(new TestEngineLaunchConfig(id, paramGroup));
			} catch (final CoreException e) {
				Q7LaunchingPlugin.log(e);
			}
		}
		this.launchConfigs = launchConfigs;
	}

	public List<TestEngineLaunchConfig> getLaunchConfigs() {
		return launchConfigs;
	}
}
