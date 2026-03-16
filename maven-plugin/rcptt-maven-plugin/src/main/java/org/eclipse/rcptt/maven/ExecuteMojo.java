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
package org.eclipse.rcptt.maven;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.eclipse.rcptt.maven.util.Injection;
import org.eclipse.rcptt.maven.util.JavaExec;
import org.eclipse.rcptt.maven.util.NetUtils;
import org.eclipse.rcptt.maven.util.Rap;
import org.eclipse.rcptt.maven.util.TestEngine;
import org.eclipse.rcptt.maven.util.TestOptions;

/**
 * Executes q7 tests
 *
 * @author ivaninozemtsev
 *
 * @goal execute
 * @phase compile
 */
public class ExecuteMojo extends AbstractRCPTTMojo {

	private static final String REUSE_EXISTING_WORKSPACE = "-reuseExistingWorkspace";
	private static final String IMPORT_SEP = ";";
	private static final String IMPORT = "-import";
	private static final String MEMORY_USAGE = "-memoryUsage";
	private static final String Q7_REPORT = "-q7report";
	private static final String HTML_REPORT = "-htmlReport";
	private static final String JUNIT_REPORT = "-junitReport";
	private static final String REPORT = "-report";
	private static final String AUT_ARGS = "-autArgs";
	private static final String AUT_CONSOLE_PREFIX = "-autConsolePrefix";
	private static final String AUT_VM = "-autVM";
	private static final String AUT_EE = "-autExecutionEnvironment";
	private static final String AUT_VM_ARGS = "-autVMArgs";
	private static final String AUT_WS_PREFIX = "-autWsPrefix";
	private static final String AUT_LOC = "-aut";
	private static final String AUT_COUNT = "-autCount";
	private static final String WORKSPACE = "-data";
	private static final String INJECTION = "-injection:site";
	private static final String INJECTION_FEATURE_SEP = ";";
	private static final String TEST_OPTIONS = "-testOptions";
	private static final String IGNORE_OTHER_INJECTIONS = "-injectSpecified";
	private static final String SHUTDOWN_LISTENER_PORT = "-shutdownListenerPort";
	private static final String SKIP_TAGS = "-skipTags";
	private static final String SUITES = "-suites";
	private static final String TESTS = "-tests";
	private static final String LIMIT = "-limit";
	private static final String SPLIT_HTML_REPORT = "-splitHtmlReport";
	private static final String NO_SECURITY_OVERRIDE = "-noSecurityOverride";
	private static final String EXECUTION_TIMEOUT = "-timeout";
	private static final String RAP_PORT = "-rapPort";
	private static final String RAP_SERVLET_PATH = "-rapPath";
	private static final String RAP_BROWSER_COMMAND = "-browserCmd";
	private static final String RUNNER_PLATFORM = "-runnerPlatform";
	private static final String TESTENGINE = "-testEngine";
	private static final String ENABLE_SOFTWARE_INSTALLATION = "-enableSoftwareInstallation";

	private static int shutdownListenerPort;
	private static final String[] DEFAULT_Q7_VM_ARGS = new String[] { "-Xms256m", "-Xmx512m",
			"-Dorg.eclipse.rcptt.runner.returnTestFailure=true" };

	// TODO: Replace this random number with carefully thought one
	private static final int TEST_FAIL_EXIT_CODE = 56;

	private class AUTCommandLine extends Commandline {
		@Override public void addEnvironment(String name, String value) {
			if( name.equals("JAVA_TOOL_OPTIONS")) {
				getLog().info(format("Environment variable %s=%s is ignored, please use AUT options", name, value));
				return;
			}
			super.addEnvironment(name, value);
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skipTests()) {
			getLog().info("Tests are skipped");
			return;
		}
		if (!projectHasTests()) {
			getLog().info("No RCPTT tests found in project, skipping execution");
			return;
		}

		validatePreparation();
		JavaExec java = JavaExec.getDefault();
		AUTCommandLine cmd = new AUTCommandLine();

		cmd.setExecutable(java.getFile().getAbsolutePath());
		cmd.setWorkingDirectory(getResolvedQ7Dir());

		// Q7 VM Args

		List<String> q7VmArgs = new ArrayList<String>();
		String[] userArgs = getQ7Coords().getVmArgs();
		if (userArgs == null || userArgs.length == 0) {
			q7VmArgs.addAll(asList(DEFAULT_Q7_VM_ARGS));
			if (java.hasPermGen()) {
				q7VmArgs.add("-XX:MaxPermSize=256m");
			}
		} else {
			q7VmArgs.addAll(asList(userArgs));
		}

		for (String arg : q7VmArgs) {
			cmd.createArg().setValue(arg);
		}

		// Equinox launcher
		cmd.createArg().setValue(JAR);
		cmd.createArg().setFile(getEquinoxJar());
		// Workspace
		cmd.createArg().setValue("-application");
		ComparableVersion version = RunnerVersionDispatcher.parseVersion(getQ7Coords().getVersion());
		String applicationId = RunnerVersionDispatcher.getApplicationId(version, getQ7Coords().getPlatform());
		cmd.createArg().setValue(applicationId);

		cmd.createArg().setValue(RUNNER_PLATFORM);
		cmd.createArg().setValue(getQ7Coords().getPlatform());

		cmd.createArg().setValue("-consoleLog");

		cmd.createArg().setValue(WORKSPACE);
		cmd.createArg().setFile(getQ7WsDir());
		// AUT location
		cmd.createArg().setValue(AUT_LOC);
		cmd.createArg().setFile(getResolvedAutDir());
		// AUT count
		if (autCount != null) {
			cmd.createArg().setValue(AUT_COUNT);
			cmd.createArg().setValue(autCount);
		}
		// AUT Workspace prefix
		cmd.createArg().setValue(AUT_WS_PREFIX);
		cmd.createArg().setFile(getAutWorkspacePrefix());

		if (aut.reuseExistingWorkspace())
			cmd.createArg().setValue(REUSE_EXISTING_WORKSPACE);

		// AUT console prefix
		cmd.createArg().setValue(AUT_CONSOLE_PREFIX);
		cmd.createArg().setFile(getAutConsolePrefix());
		// AUT VM Args
		if (aut.getVmArgs() != null && aut.getVmArgs().length > 0) {
			cmd.createArg().setValue(AUT_VM_ARGS);
			cmd.createArg().setValue(StringUtils.join(aut.getVmArgs(), ";"));
		}
		// AUT args
		if (aut.getArgs() != null && aut.getArgs().length > 0) {
			cmd.createArg().setValue(AUT_ARGS);
			cmd.createArg().setValue(StringUtils.join(aut.getArgs(), ";"));
		}
		// AUT VM
		if (aut.getVm() != null) {
			cmd.createArg().setValue(AUT_VM);
			cmd.createArg().setValue(aut.getVm());
		} else if (aut.executionEnvironment != null) {
			cmd.createArg().setValue(AUT_EE);
			cmd.createArg().setValue(aut.executionEnvironment);
		}
		// AUT enableSoftwareInstallation
		if (aut.enableSoftwareInstallation())
		{
			cmd.createArg().setValue(ENABLE_SOFTWARE_INSTALLATION);
		}

		if (getQ7Coords().getPlatform().toLowerCase().equals("rap")) {
			setRapParams(cmd, aut.getRap());
		}

		// Memory usage
		if (memoryUsage) {
			cmd.createArg().setValue(MEMORY_USAGE);
		}

		// Q7 Report
		cmd.createArg().setValue(Q7_REPORT);
		cmd.createArg().setFile(getQ7ReportFile());

		// HTML Report
		cmd.createArg().setValue(HTML_REPORT);
		cmd.createArg().setFile(getHtmlReportFile());

		// Split HTML report
		if (splitHtmlReport) {
			cmd.createArg().setValue(SPLIT_HTML_REPORT);
		}

		// JUnit Report
		cmd.createArg().setValue(JUNIT_REPORT);
		cmd.createArg().setFile(getJUnitReportFile());

		if (report != null) {
			cmd.createArg().setValue(REPORT);
			cmd.createArg().setValue(report.getId() + ";" + getResultsDir().getAbsolutePath() + "/" + report.getName());
		}
		if (limit != -1) {
			cmd.createArg().setValue(LIMIT);
			cmd.createArg().setValue(Integer.toString(limit));
		}

		// Imports list
		cmd.createArg().setValue(IMPORT);
		cmd.createArg().setValue(getImports());

		// injection
		setInjectionParams(cmd, aut.getInjections());

		// eclipse.keyring
		if (useDefaultEclipseKeyring) {
			cmd.createArg().setValue(NO_SECURITY_OVERRIDE);
		}

		// test options
		cmd.createArg().setValue(TEST_OPTIONS);
		cmd.createArg().setValue(TestOptions.toString(getTestOptions()));
		cmd.createArg().setValue(EXECUTION_TIMEOUT);
		cmd.createArg().setValue(TestOptions.get(getTestOptions(), TestOptions.EXEC_TIMEOUT));

		// test engines
		if( getTestEngines() != null ) { 
			for (TestEngine engine : getTestEngines() ) {
				cmd.createArg().setValue(TESTENGINE);
				cmd.createArg().setValue(engine.toString());
			}
		}	

		int shift = (int) (new Random().nextLong() % 1000);
		shutdownListenerPort = NetUtils.findFreePort(9000 + shift, 9999 + shift);
		if (shutdownListenerPort != -1) {
			cmd.createArg().setValue(SHUTDOWN_LISTENER_PORT);
			cmd.createArg().setValue(((Integer) shutdownListenerPort).toString());
		}

		if (skipTags != null) {
			cmd.createArg().setValue(SKIP_TAGS);
			cmd.createArg().setValue(StringUtils.join(skipTags, ";"));
		}
		if (suites != null && suites.length > 0) {
			cmd.createArg().setValue(SUITES);
			cmd.createArg().setValue(StringUtils.join(suites, ";"));
		}
		if (tests != null && tests.length > 0) {
			cmd.createArg().setValue(TESTS);
			cmd.createArg().setValue(StringUtils.join(tests, ";"));
		}
		// whoo, almost ready to launch
		getLog().info(format("Runner command line is %s", cmd.toString()));
		FileStreamConsumer outConsumer;
		FileStreamConsumer errConsumer;
		try {
			outConsumer = new FileStreamConsumer(getQ7Out(), getLog(), false);
			errConsumer = new FileStreamConsumer(getQ7Err(), getLog(), true);
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Can't write out and err files", e);
		}
		try {
			int duration = TestOptions.getInt(getTestOptions(), TestOptions.EXEC_TIMEOUT);
			getLog().info(format("The execution timeout is set to %d seconds", duration));
			Runtime.getRuntime().addShutdownHook(ShutdownHook);
			int exitCode = CommandLineUtils.executeCommandLine(cmd, outConsumer, errConsumer, duration);
			getLog().info(format("Runner exit code is: %d", exitCode));
			if (exitCode != 0) {
				if (exitCode == TEST_FAIL_EXIT_CODE) {
					if (testFailureIgnore) {
						getLog().error("There are test failures");
					} else {
						throw new MojoFailureException("There are test failures");
					}
				} else {
					throw new MojoExecutionException(
							format("Failed to launch RCPTT runner. Runner exit code is: %d", exitCode));
				}
			}
			Runtime.getRuntime().removeShutdownHook(ShutdownHook);
			outConsumer.done();
			errConsumer.done();
		} catch (MojoExecutionException e) {
			throw e;
		} catch (MojoFailureException e) {
			throw e;
		} catch (/* CommandLine */Exception e) {
			throw new MojoExecutionException("Failed to launch RCPTT runner", e);
		}
	}

	static final ComparableVersion v154 = new ComparableVersion("1.5.4");
	static final ComparableVersion v155 = new ComparableVersion("1.5.5");

	Thread ShutdownHook = new Thread() {
		@Override
		public void run() {
			try (Socket socket = new Socket("127.0.0.1", shutdownListenerPort)) {
			} catch (IOException e) {
				System.out.println(e);
			}
			getLog().info("Process terminated. Send shutdown request to RCPTT runner.");
		}
	};

	private String getImports() throws MojoFailureException {
		StringBuilder sb = new StringBuilder();
		for (File file : getProjectsDir().listFiles()) {
			sb.append(file.getAbsolutePath()).append(IMPORT_SEP);
		}

		if (projects != null)
			for (String projectPath : projects)
				sb.append(projectPath).append(IMPORT_SEP);

		sb.setLength(sb.length() - 1);

		return sb.toString();
	}

	/**
	 * Validate that all necessary information is present and no one has
	 * corrupted the results of {@link PrepareMojo} by customizing lifecycle
	 *
	 * @throws MojoFailureException
	 */
	private void validatePreparation() throws MojoFailureException {
		boolean ok = true;
		ok &= getProjectsDir().exists();
		ok &= getThisProjectDir().exists();
		ok &= getAutDir().exists();
		ok &= getQ7Dir().exists();
		if (!ok) {
			throw new MojoFailureException(
					"Cannot execute RCPTT tests. Something is corrupted during prepare goal. Is lifecycle have been modified?");
		}
	}

	private static final class FileStreamConsumer implements StreamConsumer {
		private Log fLog;
		private boolean fIsError;

		public FileStreamConsumer(File dest, Log log, boolean isError) throws FileNotFoundException {
			this.writer = new PrintWriter(new FileOutputStream(dest), true);
			fLog = log;
			fIsError = isError;
		}

		public void done() {
			writer.flush();
			writer.close();
		}

		private PrintWriter writer;

		@Override
		public void consumeLine(String line) {
			writer.println(line);
			if (fLog != null) {
				if (fIsError) {
					fLog.error(line);
				} else {
					fLog.info(line);
				}
			}
		}

	}

	private void setInjectionParams(Commandline cmd, Injection[] injections) {
		if (injections == null) {
			return;
		}
		final Log log = getLog();
		for (Injection injection : injections) {
			final String site = injection.getSite();
			if (site == null || site.length() == 0) { // <site> parameter is
														// missed or empty
				log.warn("Ingored injection with invalid site paramater.");
				continue;
			}

			final StringBuilder argValue = new StringBuilder(site);
			if (injection.getFeatures() != null) { // list of features is
													// specified
				for (String feature : injection.getFeatures()) {
					if (feature.length() == 0) {
						log.warn("Ingored empty name feature in the injection with site " + site);
					}
					argValue.append(INJECTION_FEATURE_SEP).append(feature);
				}
			}

			cmd.createArg().setValue(INJECTION);
			cmd.createArg().setValue(argValue.toString());
		}

		if (aut.isIgnoreOtherInjections()) {
			cmd.createArg().setValue(IGNORE_OTHER_INJECTIONS);
			cmd.createArg().setValue(Boolean.toString(aut.isIgnoreOtherInjections()));
		}
	}

	private void setRapParams(Commandline cmd, Rap rap) {
		if (rap == null) {
			return;
		}

		if (rap.getPort() != -1) {
			cmd.createArg().setValue(RAP_PORT);
			cmd.createArg().setValue(Integer.toString(rap.getPort()));
		}

		if (rap.getServletPath() != null && rap.getServletPath().length() != 0) {
			cmd.createArg().setValue(RAP_SERVLET_PATH);
			cmd.createArg().setValue(rap.getServletPath());
		}

		if (rap.getBrowserCmd() != null && rap.getBrowserCmd().length() != 0) {
			cmd.createArg().setValue(RAP_BROWSER_COMMAND);
			cmd.createArg().setValue(rap.getBrowserCmd());
		}
	}

}
