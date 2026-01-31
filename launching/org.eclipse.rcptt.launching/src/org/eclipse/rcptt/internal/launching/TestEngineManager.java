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
package org.eclipse.rcptt.internal.launching;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.rcptt.internal.core.model.Q7TestCase;
import org.eclipse.rcptt.internal.launching.ecl.EclScenarioExecutable;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.ITestEngine;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

public class TestEngineManager {
	private static TestEngineManager instance;

	public static TestEngineManager getInstance() {
		if (instance == null) {
			instance = new TestEngineManager();
		}
		return instance;
	}

	private final static String TESTENGINE_EXTPT = "org.eclipse.rcptt.launching.testEngine";
	private final static String TESTENGINE_ID_ATTR = "id";
	private final static String TESTENGINE_NAME_ATTR = "name";
	private final static String TESTENGINE_CONFIG_ATTR = "parameters";
	private final static String TESTENGINE_CLASS_ATTR = "class";

	public class TestEngineExtension {
		private String id;
		private String name;
		private String params;
		private ITestEngine engine;
		private Map<String, String> config;

		public TestEngineExtension(String id, String name, String params, ITestEngine engine) {
			this.id = id;
			this.name = name;
			this.params = params;
			this.engine = engine;
			this.config = Collections.emptyMap();
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getConfigParams() {
			return params;
		}

		public ITestEngine getEngine() {
			return engine;
		}

		public Map<String, String> getConfig() {
			return config;
		}

		public void setConfig(Map<String, String> config) {
			this.config = config;
		}
	}

	private final List<TestEngineExtension> engines;
	private List<TestEngineExtension> enabledEngines;
	private Map<String, String> engineStatuses;
	private Map<String, Map<String, String>> config;

	public TestEngineManager() {
		final List<TestEngineExtension> extensions = new ArrayList<TestEngineExtension>();
		final IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(TESTENGINE_EXTPT);
		for (final IConfigurationElement element : elements) {
			final String id = element.getAttribute(TESTENGINE_ID_ATTR);
			final String name = element.getAttribute(TESTENGINE_NAME_ATTR);
			final String params = element.getAttribute(TESTENGINE_CONFIG_ATTR);
			try {
				final ITestEngine engine = (ITestEngine) element
						.createExecutableExtension(TESTENGINE_CLASS_ATTR);
				extensions.add(new TestEngineExtension(id, name, params, engine));
			} catch (final CoreException e) {
				Q7LaunchingPlugin.log(MessageFormat.format("Failed to add {0} engine", name), e);
			}
		}
		this.engines = extensions;
    }

	public List<TestEngineExtension> getEngines() {
		return engines;
	}

	public void fireTestRunStarted(Map<String, Map<String, String>> config, List<Q7TestCase> tests) {
		applyConfig(config);
		for (TestEngineExtension engine : this.enabledEngines) {
			engine.getEngine().testRunStarted(engine.getConfig(), tests);
		}
	}

	public void fireTestRunCompleted() {
		for (TestEngineExtension engine : this.enabledEngines) {
			engine.getEngine().testRunCompleted();
		}
		cleanConfig();
	}

	public void fireSessionStarted(ExecutionSession session) {
		if (config == null || config.isEmpty()) {
			applyDefaultConfig(session);
		}
		for (TestEngineExtension engine : this.enabledEngines) {
			engine.getEngine().sessionStarted(session);
		}
	}

	public void fireSessionCompleted(ExecutionSession session) {
		for (TestEngineExtension engine : this.enabledEngines) {
			engine.getEngine().sessionCompleted(session);
		}
	}

	public void fireExecutionStarted(EclScenarioExecutable scenario) {
		for (TestEngineExtension engine : this.enabledEngines) {
			engine.getEngine().executionStarted(scenario, engine.getEngine().getConfiguration());
		}
	}

	public void fireExecutionCompleted(EclScenarioExecutable scenario, Report report) {
		for (TestEngineExtension engine : this.enabledEngines) {
			engine.getEngine().executionCompleted(scenario, report);
		}
	}

	public String validateParameter(String engineId, String paramName, String paramValue) {
		TestEngineExtension engine = this.engines.stream()
				.filter(testEngine -> testEngine.getId().equals(engineId))
				.findFirst()
				.orElse(null);

		return engine.getEngine().validateParameter(paramName, paramValue);
	}

	private void applyDefaultConfig(ExecutionSession session) {
		try {
			final Launch launch = session.getLaunch();
			if (launch != null) {
				final ILaunchConfiguration configuration = launch.getLaunchConfiguration();
				this.engineStatuses = configuration.getAttribute(IQ7Launch.ATTR_TEST_ENGINES,
						Collections.emptyMap());
			}
		} catch (CoreException e) {
			Q7LaunchingPlugin.log("Failed to request enabled Test Engines", e);
			this.engineStatuses = Collections.emptyMap();
		}
		this.enabledEngines = getEnabledEngines();
	}

	private void applyConfig(Map<String, Map<String, String>> config) {
		Map<String, String> statuses = new HashMap<String, String>();
		for (TestEngineExtension engine : this.engines) {
			String paramsString = engine.getConfigParams();
			if (paramsString == null || paramsString.equals("")) {
				statuses.put(engine.getId(), "false");
				continue;
			}
			String id = engine.getId();
			Map<String, String> engineConfig = config.get(id);
			if (engineConfig == null) {
				statuses.put(engine.getId(), "false");
				continue;
			}
			List<String> allParams = Arrays.asList(paramsString.split("\\s*,\\s*"));
			List<String> requiredParams = new ArrayList<String>();
			for (String param : allParams) {
				if (param.endsWith("*")) {
					param = param.substring(0, param.length() - 1); // remove "*"
					requiredParams.add(param);
				}
			}
			boolean allRequiredParamsAreProvided = requiredParams.stream()
					.allMatch(param -> engineConfig.containsKey(param));
			boolean anyParamsAreProvided = allParams.stream()
					.anyMatch(param -> engineConfig.containsKey(param));
			statuses.put(engine.getId(), String.valueOf(allRequiredParamsAreProvided));
			if (!allRequiredParamsAreProvided && anyParamsAreProvided) {
				Q7LaunchingPlugin.log(MessageFormat.format(
						"{0} engine is not enabled, because not all required parameters were specified",
						engine.getName()));
			}
			engine.setConfig(engineConfig);
			engine.engine.setConfiguration(engineConfig);
		}
		this.engineStatuses = statuses;
		this.enabledEngines = getEnabledEngines();
		this.config = config;
	}

	private void cleanConfig() {
		this.engineStatuses = Collections.emptyMap();
		this.enabledEngines = Collections.emptyList();
		this.config = Collections.emptyMap();
	}

	private List<TestEngineExtension> getEnabledEngines() {
		List<TestEngineExtension> enablesEngines = engines.stream()
				.filter(extension -> testEngineIsEnabled(extension))
				.collect(Collectors.toList());
		return enablesEngines;
	}

	private boolean testEngineIsEnabled(TestEngineExtension extension) {
		String id = extension.getId();
		String enabled = engineStatuses.get(id);
		return enabled != null && enabled.equals("true");
	}
}
