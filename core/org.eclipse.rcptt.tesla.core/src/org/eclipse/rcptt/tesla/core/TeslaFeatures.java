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
package org.eclipse.rcptt.tesla.core;

import org.eclipse.rcptt.tesla.core.features.IMLFeatures;
import org.eclipse.rcptt.tesla.core.protocol.diagram.DiagramFeatures;
import org.eclipse.rcptt.tesla.core.utils.AbstractFeatureManager;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.util.StringUtils;

public class TeslaFeatures extends AbstractFeatureManager {
	private static final String CAT_COMMAND_DELAYS = "Command Delays";
	private static final String TESLA_PROTOCOL_VERSION = "1.0.1";
	// Protocol options
	public final static String PROTOCOL_VERSION = "org.eclipse.rcptt.tesla.protocol.version";

	public final static String COMMAND_EXECUTION_DELAY = "org.eclipse.rcptt.tesla.ecl.execution.delay";
	public final static String INTERNAL_CLIPBOARD = "org.eclipse.rcptt.tesla.internal.clipboard";
	public final static String COMMAND_EXECUTION_DELAY_TESLA = "org.eclipse.rcptt.tesla.execution.delay";
	public final static String RECORD_ALL_SELECTIONS = "org.eclipse.rcptt.tesla.record.all.selections";
	public final static String ESCAPE_TREES_TABLES_MODE = "org.eclipse.rcptt.tesla.escape.mode";
	public final static String IDENTIFY_BY_CLASS_METHODS = "org.eclipse.rcptt.tesla.identify.by.class.methods";

	public final static String CAT_RESOURCES_VERIFICATION = "Resources verification";
	public final static String RESOURCES_VERIFICATION_HUNKS_COUNT = "org.eclipse.rcptt.resources.verification.hunks.count";

	private static final String[] ESCAPE_TREES_TABLES_VALUES = new String[] {
			EscapeMode.ExactString.toString(),
			EscapeMode.EscapedRegex.toString()
	};

	public static enum EscapeMode {
		ExactString, EscapedRegex
	}

	public final static String ACTIVITY_LOGS = "com.xored.runtime.enable.activity.logs";
	public final static String TESTENGINE_LOGS = "com.xored.testengine.logs";

	public static final String ADV_OPTIONS = "adv.options";

	public static final String NONE = "";
	public static final String CP_OPTIONS = "cp.options";

	public static final String STATUS_DIALOG_ALLOWED = "com.xored.runtime.status.dialog.allowed";

	public static final String ENABLE_PROTECTED_MEMBERS = "enable.protected.members";

	// Logging features
	public static final String LOGGING_OPTIONS = "logging.options";
	public static final String CAT_LOGGING = "Logging";
	public static String CAPTURE_EXECUTION = "q7.image.capture";

	public static String CAT_REPORTING = "Reporting";
	public final static String REPORT_PASSED_TEST_DETAILS = "com.xored.reporting.passed.test.details";
	public final static String REPORT_LOGGING_SIZE_OF_INITIAL_PART = "com.xored.reporting.logging.initial.size";
	public final static String REPORT_LOGGING_SIZE_OF_ROTATION_PART = "com.xored.reporting.logging.rotation.size";
	public final static String REPORT_INCLUDE_IGNORED_WAIT_DETAILS = "com.xored.reporting.include.ignored.wait.details";
	public final static String REPORT_PASSED_WAIT_DETAILS = "com.xored.reporting.include.passed.wait.details";
	public final static String REPORT_INCLUDE_ECLIPSE_METHODS_WAIT_DETAILS = "com.xored.reporting.include.eclipse.wait.details";

	// Code:
	private static TeslaFeatures features;

	public static TeslaFeatures getInstance() {
		if (features == null) {
			features = new TeslaFeatures();
			features.loadFromPreferences(TeslaCore.getPreferences());
		}
		return features;
	}

	public TeslaFeatures() {
		option(PROTOCOL_VERSION).name("Protocol version")
				.value(TESLA_PROTOCOL_VERSION).editable(false)
				.showIn(ADV_OPTIONS);

		option(COMMAND_EXECUTION_DELAY)
				.name("ECL command execution delay")
				.category(CAT_COMMAND_DELAYS)
				.value("0")
				.defaultValue("0")
				.values(AbstractFeatureManager.INT_VALUES)
				.description(
						"Wait for specified milliseconds before execution of next Tesla ECL command")
				.editable(true).showIn(NONE);

		option(COMMAND_EXECUTION_DELAY_TESLA)
				.name("Tesla command execution delay")
				.category(CAT_COMMAND_DELAYS)
				.value("0")
				.defaultValue("0")
				.values(AbstractFeatureManager.INT_VALUES)
				.description(
						"Wait for specified milliseconds before execution of next Tesla command")
				.editable(true).showIn(NONE);

		option(RESOURCES_VERIFICATION_HUNKS_COUNT)
				.name("Maximum count of hunks per file")
				.category(CAT_RESOURCES_VERIFICATION)
				.value("10")
				.defaultValue("10")
				.values(INT_VALUES)
				.description("Lines from remaining hunks are not displayed in results")
				.editable(true).showIn(ADV_OPTIONS);

		option(INTERNAL_CLIPBOARD)
				.name("Use internal clipboard")
				.category("Clipboard options")
				.value("false")
				.defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES)
				.description(
						"Do not use OS clipboard for copy/paste operations")
				.editable(true).showIn(ADV_OPTIONS);

		option(REPORT_PASSED_TEST_DETAILS).category(CAT_REPORTING)
				.name("Include 'trace' and 'take-screenshot' in HTML report")
				.value("false")
				.defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES)
				.description(
						"Command 'trace' and 'take-screenshot' can be used to add data into test report")
				.editable(true).showIn(ADV_OPTIONS);

		option(REPORT_LOGGING_SIZE_OF_INITIAL_PART).category(CAT_REPORTING)
				.name("The size of log initial part(in MB)")
				.value("5")
				.defaultValue("5")
				.values(AbstractFeatureManager.INT_VALUES)
				.description(
						"The initial part size of logs")
				.editable(true).showIn(ADV_OPTIONS);

		option(REPORT_LOGGING_SIZE_OF_ROTATION_PART).category(CAT_REPORTING)
				.name("The size of log rotation part(in MB)")
				.value("5")
				.defaultValue("5")
				.values(AbstractFeatureManager.INT_VALUES)
				.description(
						"The rotation size of log. At overflow works by the principle of queue.")
				.editable(true).showIn(ADV_OPTIONS);

		option(REPORT_INCLUDE_IGNORED_WAIT_DETAILS).category(CAT_REPORTING)
				.name("Include 'ignored' timers into report files")
				.value("false")
				.defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES)
				.description(
						"RCPTT widget details will contain ignored timer calls")
				.editable(true).showIn(ADV_OPTIONS);

		option(REPORT_PASSED_WAIT_DETAILS).category(CAT_REPORTING)
				.name("Include 'wait details' into passed report entries")
				.value("true")
				.defaultValue("true")
				.values(AbstractFeatureManager.BOOLEAN_VALUES)
				.description(
						"RCPTT widget wait details will be also added to passed reports as separate section")
				.editable(true).showIn(ADV_OPTIONS);

		option(REPORT_INCLUDE_ECLIPSE_METHODS_WAIT_DETAILS).category(CAT_REPORTING)
				.name("Include eclipse methods into 'wait details' info")
				.value("false")
				.defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES)
				.description(
						"Eclipse methods will be added into 'wait details' info")
				.editable(true).showIn(ADV_OPTIONS);

		option(RECORD_ALL_SELECTIONS)
				.name("Record all selections for tables and trees")
				.value("false")
				.defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES)
				.description(
						"Enables or disables collapsing of selection events")
				.editable(true).showIn(ADV_OPTIONS, CP_OPTIONS);

		option(ESCAPE_TREES_TABLES_MODE)
				.name("Table/tree item literal mode")
				.values(ESCAPE_TREES_TABLES_VALUES)
				.defaultValue(EscapeMode.ExactString.toString())
				.value(EscapeMode.ExactString.toString())
				.showIn(ADV_OPTIONS, CP_OPTIONS);

		option(STATUS_DIALOG_ALLOWED)
				.name("Allow status dialogs")
				.description(
						"Should the test pass, if appears the status dialog with an error?")
				.value("false").defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES).editable(true)
				.showIn(TeslaFeatures.ADV_OPTIONS, TeslaFeatures.CP_OPTIONS);

		option(ENABLE_PROTECTED_MEMBERS)
				.name("Use protected members in Advanced properties (deprecated)")
				.description(
						"Enable assertions for protected fields and methods")
				.value("false").defaultValue("false").values(BOOLEAN_VALUES)
				.editable(true).showIn(ADV_OPTIONS);

		option(IDENTIFY_BY_CLASS_METHODS)
				.name("Identify widgets by class methods")
				.value("")
				.defaultValue("")
				.description(
						"Comma-separated list of methods will be used to identify widgets of specified class\n"
								+ "Valid format: ClassName1:getModel().getName(),ClassName2:getIdString()")
				.editable(true)
				.showIn(TeslaFeatures.ADV_OPTIONS, TeslaFeatures.CP_OPTIONS);

		// Diagram options
		DiagramFeatures.init(this);
		// Initialize other features
		IMLFeatures.init(this);
		// Timesouts
		TeslaLimits.init(this);

		option(CAPTURE_EXECUTION).category(CAT_LOGGING).name("Enable per command image capture")
				.description("Capture screenshots on each command execution")
				.value("false").defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES).editable(true)
				.showIn(TeslaFeatures.ADV_OPTIONS, TeslaFeatures.CP_OPTIONS);
		option(ACTIVITY_LOGS)
				.name("Enable Activity logging")
				.category(CAT_LOGGING)
				.value("false")
				.defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES)
				.description(
						"Store RCPTT runtime activity events into .metadata/.plugins/org.eclipse.rcptt.logging")
				.editable(true).showIn(ADV_OPTIONS);
		option(TESTENGINE_LOGS)
				.name("Enable Test Engine logging")
				.category(CAT_LOGGING)
				.value("false")
				.defaultValue("false")
				.values(AbstractFeatureManager.BOOLEAN_VALUES)
				.description(
						"Show Test Engines info messages in Error Log")
				.editable(true).showIn(ADV_OPTIONS);
	}

	public static boolean isUseInternalClipboard() {
		return getInstance().isTrue(INTERNAL_CLIPBOARD);
	}

	public static boolean isActivityLogging() {
		return getInstance().isTrue(TeslaFeatures.ACTIVITY_LOGS);
	}

	public static boolean isIncludeIgnoredWaitDetails() {
		return getInstance().isTrue(TeslaFeatures.REPORT_INCLUDE_IGNORED_WAIT_DETAILS);
	}

	public static boolean isIncludePassedWaitDetails() {
		return getInstance().isTrue(TeslaFeatures.REPORT_PASSED_WAIT_DETAILS);
	}

	public static boolean isIncludeEclipseMethodsWaitDetails() {
		return getInstance().isTrue(TeslaFeatures.REPORT_INCLUDE_ECLIPSE_METHODS_WAIT_DETAILS);
	}

	public static boolean isProtectedEnabled() {
		return getInstance().isTrue(ENABLE_PROTECTED_MEMBERS);
	}

	public static boolean isIdentifyMethodsProvided() {
		String methods = getInstance().getValue(IDENTIFY_BY_CLASS_METHODS);
		return !StringUtils.isBlank(methods);
	}

}
