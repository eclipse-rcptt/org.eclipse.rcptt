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
package org.eclipse.rcptt.core;

import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.utils.AbstractFeatureManager;

public class Q7Features extends AbstractFeatureManager {

	public static final String Q7_CONTEXTS_RESOURCES_TRANSFER_CONTENT = "q7.contexts.resources.transfer.content";
	public static final String Q7_CONTEXTS_RESOURCES_ZIPPED_TRANSFER = "q7.contexts.resources.transfer.zipped";
	// public static final String Q7_WORKSPACE_CLEAR_TIMEOUT =
	// "q7.contexts.workspace.clear.timeout";
	public static final String Q7_WORKSPACE_FORCE_GC = "q7.contexts.workspace.clear.force.gc";
	public static final String LAUNCHING_KILL_AUT_ON_CONNECT_ERROR = "launching.kill.aut.on.connect.error";
	public static final String RETRY_TEST = "q7.retry.count";
	// Code:
	private static Q7Features features;

	public static boolean supportQ7OptionsFile = true;
	public static boolean supportBundlesInApplications = true;

	public static Q7Features getInstance() {
		if (features == null) {
			features = new Q7Features();
			features.loadFromPreferences(RcpttPlugin.getPreferences());
		}
		return features;
	}

	public Q7Features() {
		option(Q7_CONTEXTS_RESOURCES_TRANSFER_CONTENT)
				.category("Resources Context")
				.name("Transfer files")
				.description(
						"Resources: Transfer content with workspace context")
				.value("true").defaultValue("true")
				.values(AbstractFeatureManager.BOOLEAN_VALUES).editable(true)
				.showIn(TeslaFeatures.NONE);
		option(Q7_CONTEXTS_RESOURCES_ZIPPED_TRANSFER)
				.category("Resources Context")
				.name("Transfer use compression(zip)")
				.description(
						"Transfer content with workspace context (the option is also used in workspace verification)")
				.value("true").defaultValue("true")
				.values(AbstractFeatureManager.BOOLEAN_VALUES).editable(true)
				.showIn(TeslaFeatures.NONE);
		// option(Q7_WORKSPACE_CLEAR_TIMEOUT)
		// .category("Resources Context")
		// .name("Clear workspace timeout")
		// .description(
		// "How long workspace context tries to remove all files from workspace, in seconds")
		// .value("31").defaultValue("31").editable(true)
		// .values(TeslaFeatures.INT_VALUES)
		// .showIn(TeslaFeatures.ADV_OPTIONS);
		option(Q7_WORKSPACE_FORCE_GC)
				.category("Resources Context")
				.name("Force GC run on workspace cleanup")
				.description(
						"If on, garbage collection will be forced on workspace cleanup")
				.value("true").defaultValue("true").editable(true)
				.values(TeslaFeatures.BOOLEAN_VALUES)
				.showIn(TeslaFeatures.ADV_OPTIONS);

		option(LAUNCHING_KILL_AUT_ON_CONNECT_ERROR)
				.name("Kill AUT on connect error")
				.description(
						"Whether to kill AUT after the startup waiting time expiration?")
				.value("false").defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES).editable(true)
				.showIn(TeslaFeatures.ADV_OPTIONS, TeslaFeatures.CP_OPTIONS);
		
		option(RETRY_TEST)
			.name("Retry test after failure (times)")
			.description("If a test fails, try executing it again and collect both failed and succesfull reports")
			.value("1")
			.defaultValue("1")
			.values(AbstractFeatureManager.INT_VALUES).editable(true)
			.showIn(TeslaFeatures.ADV_OPTIONS);
	}
}
