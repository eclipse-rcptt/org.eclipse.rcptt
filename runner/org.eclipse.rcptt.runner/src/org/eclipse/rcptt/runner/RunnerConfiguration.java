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
package org.eclipse.rcptt.runner;

import static java.util.Arrays.asList;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.rcptt.internal.launching.TestEngineManager;
import org.eclipse.rcptt.launching.injection.Directory;
import org.eclipse.rcptt.launching.injection.InjectionConfiguration;
import org.eclipse.rcptt.launching.injection.InjectionFactory;
import org.eclipse.rcptt.launching.injection.UpdateSite;
import org.eclipse.rcptt.reporting.core.ReportRendererManager;
import org.eclipse.rcptt.reporting.core.ReportRendererManager.ReportRenderer;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.TeslaLimits;

public class RunnerConfiguration {
	private static final int DEFAULT_EXECUTION_TIMEOUT = 18000;
	private static final int DEFAULT_SHUTDOWN_TIMEOUT = 5 * 60;
	private static final int DEFAULT_WAIT_AUT_TIMEOUT = 5 * 60;

	enum CommandArg {
		Runner("The runner platform (SWT or RAP)", "runnerPlatform"),
		Location("AUT Product location", "aut"), //
		Count("AUT Instances count", "autCount"), //
		RAPPort("RAP AUT Server port", "rapPort"), //
		RAPServletPath("RAP AUT servlet path", "rapPath"), //
		RAPOpenBrowserCommand("Start browser command", "browserCmd"), //
		WsPrefix(
				"AUT workspaces prefix to use. Workspaces will contain numeric number.",
				"autWsPrefix", "wsPrefix"), //
		Suites("Comma separated testsuite names.", "suites"), //
		Import("Project to import to AUT workspace", "import"), //
		Q7Report("File name of q7 report", "q7report"), //
		AUTReport("File name of AUT report about initialization issues",
				"autReport"), //
		HTMLReport("File name of HTML report", "htmlReport"), //
		JUnitReport("File path of JUnit report", "junitReport"), //
		Report(
				"Comma separated pair of report id, report file name without extension",
				"report"), //
//		Logging("Enable advanced logging mode", "logging"), //
		MemoryUsage("Output memory usage information", "memoryUsage"), //
		ExecutionTimeout("Execution timeout (default 18000 seconds (5 hours))",
				"timeout"), //
		Limit("Execute only first N tests)", "limit"), //
		AutArgs("Comma separated list of arguments to pass for AUT", "autArgs"), //

		AutVMArgs("Comma separated list of arguments to pass to AUT Java VM",
				"autVMArgs"), //
		JavaVM("Java virtual machine to test on. Default: current VM", "autVM",
				"autJVM", "javaVM"), //
		UpdateSite(
				"Comma separated update site location and features required to be injected into AUT",
				"injection:site", "site"), //
		Directory("Location of plugins directory to be injected into AUT.",
				"injection:dir", "dir"), //
		ConnectTimeout("Q7 Connection timeout", "connectTimeout"), //
		ConsoleLogsPrefix("Console log prefix", "autConsolePrefix"), //
		TimeoutRestartTestCount(
				"Restart AUT then specified amount of tests are failed with timeout (default value is 2, one by one)",
				"timeoutRestartTestCount"), //
		ConnectionResetRetry("Retry tests after connection reset errors", "connectionResetRetry"), //
		OnlySpecified("Inject only specified sites", "injectSpecified"), //
		NoSecurityStorage("Do not override secure storage location",
				"noSecurityOverride"), //
		TestOptions("Execution options", "testOptions"), //
		ShutdownListenerPort("Shutdown Listener Port", "shutdownListenerPort"), //
		Help("This message", "help", "h", "?"), //
		TagsToSkip("Skip tests with listed tags during execution", "skipTags"), //
		SplitHTMLReport("Split HTML Report", "splitHtmlReport"), //
		ShutdownTimeout(
				"AUT shutdown timeout (default 300 seconds (5 minutes))",
				"shutdownTimeout"), //
		ReuseExisingWorkspace(
				"When true, treat autWsPrefix as full workspace path and do not change it",
				"reuseExistingWorkspace"), //
		Tests("Semicolon-separated list of test name glob patterns (* - any chars, ? - exactly one char)", "tests"), //
		TestEngine("Semicolon-separated list of test engines configuration parameters", "testEngine"),
		EnableSoftwareInstallation("Enable support software installation in the launched application", "enableSoftwareInstallation");

		CommandArg(String message, String... val) {
			this.val = val;
			this.message = message;
		}

		private String message;
		private String[] val;

		public String[] getCommands() {
			return val;
		}

		public String getMessage() {
			return message;
		}
	};

	private static final String SEPARATOR = ";";

	public final List<File> toImport = new ArrayList<File>();
	public String location;
	private String q7ReportLocation = null;
	private String autReportLocation = null;
	public String htmlReportLocation = null;
	public String junitReportLocation = null;
	public final List<String> toTest = new ArrayList<String>();
	public final List<String> suites = new ArrayList<String>();
	public int autCount = 1;
	public String autWorkspacePrefix;
	public String[] autArgs;
	public String[] autVMArgs = new String[0];
	public int timeoutRestart = 2;
	public final int internalAutFailure = 2;
	public int connectionResetRetry = 3;
	public boolean splitHTMLReport = false;
	public boolean reuseExistingWorkspace = false;
	public boolean outputMemoryUsage = false;
	public String path = null;
	public String browserCmd = null;
	public Integer port = null;
	public boolean rapPlatform = false;
	public boolean enableSoftwareInstallation = false;

	public static class UserReport {
		public String id;
		public String path;
		public ReportRenderer renderer;
	}

	public UserReport report = null;

	public String autConsolePrefix = null;

	public int executionTimeout = DEFAULT_EXECUTION_TIMEOUT;
	public int shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;
	public int waitAutTimeout = DEFAULT_WAIT_AUT_TIMEOUT;

	public boolean onlySpecified = false;
	public Map<String, String> testOptions = new HashMap<String, String>();
	public int shudownListenerPort = 0;

	public String javaVM;

	public InjectionConfiguration config = null;

	public int limit = -1;
	public String[] tagsToSkip = new String[] { "skipExecution" };

	public boolean overrideSecurityStorage = true;

	public Map<String, Map<String, String>> testEngines = new HashMap<String, Map<String, String>>();

	public RunnerConfiguration() {

	}

	private String getFriendlyName() {
		String result = "aut-";

		// try to gather some more friendly name than
		// just a number that will be appended somewhere later
		if (this.location != null) {
			File autDir = new File(this.location);
			String autName = autDir.getName();
			if (autName != null && autName.length() > 0)
				result += autName + "-";
		}

		return result;
	}

	private boolean fillDefaults() {
		if (autWorkspacePrefix == null) {
			boolean success = false;

			try {
				Location location = Platform.getInstanceLocation();
				if (location != null) {
					URL url = location.getURL();
					if (url != null) {
						File file = new File(url.toURI());
						File parent = file.getParentFile();
						if (parent != null) {
							String path = new File(parent.getAbsolutePath(),
									getFriendlyName()).toString();

							// and finally
							autWorkspacePrefix = path;
							success = true;
						}
					}
				}
			} catch (Exception e) {
			}

			if (!success) {
				System.out.println("ERROR: Unable to guess wokspace location.");
				return false;
			}
		}

		if (autConsolePrefix == null) {
			boolean success = false;

			try {
				Location location = Platform.getInstanceLocation();
				if (location != null) {
					URL url = location.getURL();
					if (url != null) {
						File file = new File(url.toURI());
						String path = new File(file.getAbsolutePath(),
								getFriendlyName()).toString();

						// and finally
						autConsolePrefix = path;
						success = true;
					}
				}
			} catch (Exception e) {
			}

			if (!success) {
				System.out.println("ERROR: Unable to guess logs location.");
				return false;
			}
		}

		return true;
	}

	public boolean processArgs(final String[] args) {
		Iterator<String> i = Arrays.asList(args).iterator();
		while (i.hasNext()) {
			final String arg = i.next();
			CommandArg cmdArg = findArg(arg);
			if (cmdArg == null) {
				System.out.println("Invalid argument:" + arg);
				continue;
			}
			switch (cmdArg) {
			case EnableSoftwareInstallation:
				enableSoftwareInstallation = true;
				break;
			case OnlySpecified:
				onlySpecified = true;
				break;
			case SplitHTMLReport:
				splitHTMLReport = true;
				break;
			case ReuseExisingWorkspace:
				reuseExistingWorkspace = true;
				break;
//			case Logging:
//				Q7Features.getInstance().getOption(TeslaFeatures.LOGGING)
//						.setValue("true");
//				break;
			case MemoryUsage:
				outputMemoryUsage = true;
				break;
			case Suites:
				final String[] suiteArgs = getSplitArguments(i);
				for (final String suite : suiteArgs) {
					if (!suites.contains(suite))
						suites.add(suite);
				}
				break;
			case Tests:
				toTest.addAll(new HashSet<String>(asList(getSplitArguments(i))));
				break;
			case Report:
				String[] arguments = getSplitArguments(i);
				if (arguments.length != 2) {
					HeadlessRunnerPlugin.getDefault().info(
							"ERROR: Invalid report argument format.");
					return false;
				} else {
					report = new UserReport();
					report.id = arguments[0];
					report.path = arguments[1];

					ReportRenderer[] renderers = ReportRendererManager
							.getInstance().getExtensions();
					for (ReportRenderer reportRenderer : renderers) {
						if (report.id.equals(reportRenderer.getId())) {
							report.renderer = reportRenderer;
							break;
						}
					}
					if (report.renderer == null) {
						HeadlessRunnerPlugin.getDefault().info(
								"ERROR: Failed to locate report renderer for id: "
										+ report.id);
						System.err
								.println("ERROR: Failed to locate report renderer for id: "
										+ report.id);
						report = null;
						return false;
					}
				}

				break;
			case Q7Report:
				q7ReportLocation = i.next();
				break;
			case AUTReport:
				autReportLocation = i.next();
				break;
			case HTMLReport:
				htmlReportLocation = i.next();
				break;
			case JUnitReport:
				junitReportLocation = i.next();
				break;
			case Import:
				final String[] files = getSplitArguments(i);
				for (final String filePath : files) {
					final File file = new File(filePath);
					if (!file.exists() || !file.isDirectory()) {
						System.out
								.println(String
										.format("Invalid project location in '-import' arg: %s",
												filePath));
						continue;
					}
					if (!toImport.contains(file))
						toImport.add(file);
				}
				break;
			case Location:
				if (location != null) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: AUT location was specified several times. The last location will be used");
				}
				location = i.next();
				// Backward compatibility
				File f = new File(location);
				if (f.exists() && f.isFile()) {
					location = f.getParent();
				}
				break;
			case TimeoutRestartTestCount:
				try {
					timeoutRestart = Integer.parseInt(i.next());
				} catch (final NumberFormatException e) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: AUT Timeout Restart test count must be integer value.");
				}
				break;
			case ConnectionResetRetry:
				try {
					connectionResetRetry = Integer.parseInt(i.next());
				} catch (NumberFormatException e) {
					HeadlessRunnerPlugin.getDefault()
							.info("WARNING: AUT Connection Reset retry count must be an integer value.");
				}
				break;
			case Count:
				try {
					autCount = Integer.parseInt(i.next());
				} catch (final NumberFormatException e) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: AUT count parameter must be an integer value.");
				}
				break;
			case Limit:
				try {
					limit = Integer.parseInt(i.next());
				} catch (final NumberFormatException e) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: AUT limit parameter must be an integer value.");
				}
				break;
			case WsPrefix:
				autWorkspacePrefix = i.next();
				break;
			case ExecutionTimeout:
				try {
					executionTimeout = Integer.parseInt(i.next());
				} catch (final NumberFormatException e) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: Execution timeout parameter must be an integer value.");
				}
				break;
			case AutArgs:
				autArgs = getSplitArguments(i);
				break;
			case AutVMArgs:
				autVMArgs = getSplitArguments(i);
				break;
			case JavaVM:
				javaVM = i.next();
				if (!new File(javaVM).exists()) {
					HeadlessRunnerPlugin.getDefault().info(
							"ERROR: Invalid Java VM path: " + javaVM);
				}
				break;
			case UpdateSite:
				initInjection();
				String[] split = getSplitArguments(i);

				if (split.length > 0) {
					UpdateSite site = InjectionFactory.eINSTANCE
							.createUpdateSite();
					site.setUri(split[0]);
					if (split.length > 1) {
						List<String> list = new ArrayList<String>(
								Arrays.asList(split));
						list.remove(0);
						site.getUnits().addAll(list);
					} else {
						site.setAllUnits(true);
					}
					config.getEntries().add(site);
				}
				break;
			case Directory:
				initInjection();
				String folder = i.next();
				Directory directory = InjectionFactory.eINSTANCE
						.createDirectory();
				directory.setPath(folder);
				config.getEntries().add(directory);
				break;
			case ConnectTimeout:
				try {
					String strTimeout = i.next();

					int timeout = Integer.parseInt(strTimeout);
					if (timeout < 1) {
						HeadlessRunnerPlugin
								.getDefault()
								.info("WARNING: ConnectTimeout value is invalid. Default value will be used...");
					} else {
						TeslaFeatures.getInstance()
								.getOption(TeslaLimits.AUT_STARTUP_TIMEOUT)
								.setValue(strTimeout);
					}
				} catch (final NumberFormatException e) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: ConnectTimeout value is invalid. Default value will be used...");
				}
				break;
			case TestOptions:
				String optionsStr = i.next();
				for (String optionStr : optionsStr.split(";")) {
					if (optionStr.trim().length() == 0) {
						continue;
					}
					int eqIndex = optionStr.indexOf('=');
					if (eqIndex == -1) {
						HeadlessRunnerPlugin
								.getDefault()
								.info("WARNING: Invalid value for testOptions. should be key1=value1;key2=value2");
					}
					String name = optionStr.substring(0, eqIndex).trim();
					String value = optionStr.substring(eqIndex + 1).trim();
					if ("overrideSecurityStorage".equals(name)) {
						overrideSecurityStorage = "true".equals(value);
					} else {
						testOptions.put(name, value);
					}
				}
				break;
			case ShutdownListenerPort:
				try {
					shudownListenerPort = Integer.parseInt(i.next());
				} catch (final NumberFormatException e) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: shudownListenerPort parameter must be an integer value.");
				}
				break;
			case ConsoleLogsPrefix:
				autConsolePrefix = i.next();
				break;
			case TagsToSkip:
				tagsToSkip = getSplitArguments(i);
				break;
			case NoSecurityStorage:
				overrideSecurityStorage = false;
				break;
			case Help:
				System.out
						.println("RCPTT Headless Runner command line arguments:");
				for (CommandArg v : CommandArg.values()) {
					System.out.print("\t");
					String[] commands = v.getCommands();
					for (int k = 0; k < commands.length; ++k) {
						System.out.print("-" + commands[k]);
						if (k != commands.length - 1) {
							System.out.print(", ");
						}
					}
					System.out.print(":\t" + v.getMessage());
					System.out.println();
				}
				return false;

			case ShutdownTimeout:
				try {
					shutdownTimeout = Integer.parseInt(i.next());
				} catch (final NumberFormatException e) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: Shutdown timeout parameter must be an integer value.");
				}
				break;
			case RAPPort:
				try {
					port = Integer.parseInt(i.next());
				} catch (final NumberFormatException e) {
					HeadlessRunnerPlugin.getDefault().info(
							"WARNING: RAP server port parameter must be an integer value.");
					port = null;
				}
				break;
			case RAPServletPath:
				path = i.next();
				break;
			case RAPOpenBrowserCommand:
				browserCmd = i.next();
				break;
			case Runner:
				rapPlatform = i.next().equalsIgnoreCase("rap");
				break;
			case TestEngine:
				String testEngineStr = i.next();
				int colIndex = testEngineStr.indexOf(':');
				if (colIndex == -1) {
					HeadlessRunnerPlugin
							.getDefault()
							.info("WARNING: Invalid value for testEngine. Should be 'testEngineId:key1=value1;key2=value2'");
					break;
				}
				String id = testEngineStr.substring(0, colIndex).trim();
				String paramsStr = testEngineStr.substring(colIndex + 1).trim();
				Map<String, String> params = new HashMap<String, String>();
				for (String paramStr : paramsStr.split(";")) {
					if (paramStr.trim().length() == 0) {
						continue;
					}
					int eqIndex = paramStr.indexOf('=');
					if (eqIndex == -1) {
						HeadlessRunnerPlugin
								.getDefault()
								.info("WARNING: Invalid value for testEngine. Should be 'testEngineId:key1=value1;key2=value2'");
						break;
					}
					String name = paramStr.substring(0, eqIndex).trim();
					String value = paramStr.substring(eqIndex + 1).trim();
					String message = TestEngineManager.getInstance()
							.validateParameter(id, name, value);
					if (message != null && !message.equals("")) {
						HeadlessRunnerPlugin
								.getDefault()
								.info(MessageFormat.format("WARNING: Invalid value for {0}. {1}", name, message));
						continue;
					}
					params.put(name, value);
				}
				testEngines.put(id, params);
				break;
			}
		}

		return fillDefaults();
	}

	private String[] getSplitArguments(Iterator<String> i) {
		if (!i.hasNext()) {
			return new String[0];
		}
		String val = i.next();
		if (val.startsWith("'") && val.endsWith("'")) {
			val = val.substring(1, val.length() - 1);
		}
		if (val.startsWith("\"") && val.endsWith("\"")) {
			val = val.substring(1, val.length() - 1);
		}
		List<String> splitResult = new ArrayList<String>();
		String[] split = val.split(SEPARATOR);
		splitResult.addAll(Arrays.asList(split));
		if (val.endsWith(SEPARATOR)) {
			// Eat one more argument
			splitResult.addAll(Arrays.asList(getSplitArguments(i)));
		}
		return splitResult.toArray(new String[splitResult.size()]);
	}

	private void initInjection() {
		if (config == null) {
			config = InjectionFactory.eINSTANCE.createInjectionConfiguration();
		}
	}

	private CommandArg findArg(String arg) {
		CommandArg[] values = CommandArg.values();
		for (CommandArg commandArg : values) {
			String[] val = commandArg.getCommands();
			for (String s : val) {
				if (("-" + s).equals(arg) || ("--" + s).equals(arg)) {
					return commandArg;
				}
			}
		}
		return null;
	}

	// default values

	public String getQ7ReportLocation() {
		return (q7ReportLocation != null) ? q7ReportLocation : new File(
				getStateLocationFile(), "q7.report").getAbsolutePath();
	}

	public String getAutReportLocation() {
		return (autReportLocation != null) ? autReportLocation : new File(
				getStateLocationFile(), "autReport.html").getAbsolutePath();
	}

	private static File getStateLocationFile() {
		return HeadlessRunnerPlugin.getDefault().getStateLocation().toFile();
	}

}
