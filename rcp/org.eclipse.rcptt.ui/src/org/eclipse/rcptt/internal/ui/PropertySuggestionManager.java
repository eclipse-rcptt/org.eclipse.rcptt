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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.ui.controls.SuggestionItem;
import org.eclipse.rcptt.ui.editors.IScenarioPropertyProvider;

public class PropertySuggestionManager {
	private static PropertySuggestionManager instance;

	public static PropertySuggestionManager getInstance() {
		if (instance == null) {
			instance = new PropertySuggestionManager();
		}
		return instance;
	}

	private final static String PROPSUGGESTION_EXTPT = "org.eclipse.rcptt.ui.propertySuggestionProviders";
	private final static String PROPSUGGESTION_TESTCASE_ATTR = "scenarioProperties";

	private List<IScenarioPropertyProvider> providers;

	public PropertySuggestionManager() {
		this.providers = new ArrayList<IScenarioPropertyProvider>();
		final IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PROPSUGGESTION_EXTPT);
		for (final IConfigurationElement element : elements) {
			try {
				final IScenarioPropertyProvider provider = (IScenarioPropertyProvider) element
						.createExecutableExtension(PROPSUGGESTION_TESTCASE_ATTR);
				providers.add(provider);
			} catch (final CoreException e) {
				Q7LaunchingPlugin.log(e);
			}
		}
	}

	public List<SuggestionItem> getScenarioProperties(Scenario scenario) {
		List<SuggestionItem> testCaseProps = new ArrayList<SuggestionItem>();
		for (IScenarioPropertyProvider provider : providers) {
			List<SuggestionItem> props = provider.getProperties(scenario);
			if (props != null && !props.isEmpty()) {
				testCaseProps.addAll(props);
			}
		}
		return testCaseProps;
	}

	public List<SuggestionItem> getScenarioPropertySuggestions(String name) {
		if (name == null || name.equals("")) {
			return Collections.emptyList();
		}
		for (IScenarioPropertyProvider provider : providers) {
			List<SuggestionItem> values = provider.getPropertyValues(name);
			if (values != null) {
				return values;
			}
		}
		return Collections.emptyList();
	}
}
