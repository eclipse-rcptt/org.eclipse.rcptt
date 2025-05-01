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
package org.eclipse.rcptt.runner.util;

import static org.eclipse.rcptt.util.StringUtils.globToRegex;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.rcptt.core.internal.builder.MigrateProjectsJob;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.UnresolvedContext;
import org.eclipse.rcptt.core.utils.TagsUtil;
import org.eclipse.rcptt.core.workspace.IWorkspaceFinder;
import org.eclipse.rcptt.core.workspace.ProjectUtil;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.core.workspace.WorkspaceFinder;
import org.eclipse.rcptt.internal.core.model.Q7InternalContext;
import org.eclipse.rcptt.internal.core.model.Q7TestCase;
import org.eclipse.rcptt.internal.core.model.index.NamedElementCollector;
import org.eclipse.rcptt.internal.core.model.index.TestSuiteElementCollector;
import org.eclipse.rcptt.internal.launching.TestEngineManager;
import org.eclipse.rcptt.reporting.util.Q7ReportIterator;
import org.eclipse.rcptt.runner.HeadlessRunner;
import org.eclipse.rcptt.runner.HeadlessRunnerPlugin;
import org.eclipse.rcptt.runner.RunnerConfiguration;
import org.eclipse.rcptt.runner.ScenarioRunnable;
import org.eclipse.rcptt.sherlock.core.streams.SherlockReportOutputStream;

import com.google.common.base.Joiner;


public class TestsRunner {

	private final RunnerConfiguration conf;
	private final Reporter reporter;
	private final ResultsHandler resultsHandler;
	private final AUTsManager auts;
	private int failedCount = 0;
	private AtomicReference<Exception> error = new AtomicReference<>(null);

	public int getFailedCount() {
		return failedCount;
	}

	public TestsRunner(RunnerConfiguration conf,
			HeadlessRunner caller, ResultsHandler resultsHandler) {
		this.conf = conf;
		this.reporter = caller.reporter;
		this.resultsHandler = resultsHandler;
		auts = new AUTsManager(conf, caller.tpc);
	}

	public Q7ReportIterator findAndRunTests() throws CoreException, AutLaunchFail {
		System.out.println("Looking for tests...");

		TestSuite[] tests;
		tests = findScenarios();
		if (tests.length <= 0) {
			throw new IllegalArgumentException("No tests found.");
		}
		return runTests(tests);
	}

	private static List<String> buildTestNamePatterns(List<String> globs) {
		List<String> result = new ArrayList<String>();
		for (String glob : globs) {
			result.add(globToRegex(glob));
		}
		return result;
	}

	private static boolean matches(String name, List<String> patterns) {
		if (patterns.isEmpty()) {
			return true;
		}

		for (String pattern : patterns) {
			if (name.matches(pattern)) {
				return true;
			}
		}
		return false;
	}

	private TestSuite[] findScenarios() throws CoreException {
		final List<TestSuite> tests = new ArrayList<TestSuite>();
		final List<String> testNamePatterns = buildTestNamePatterns(conf.toTest);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription descrition = workspace.getDescription();
		descrition.setAutoBuilding(false);
		workspace.setDescription(descrition);
		ProjectUtil.importProjects(conf.toImport, System.out);
		new MigrateProjectsJob(ResourcesPlugin.getWorkspace().getRoot())
				.runSync();
		IProject[] projects;
		try {
			projects = ProjectUtil.projects();
		} catch (InterruptedException e1) {
			throw new CoreException(new Status(IStatus.CANCEL,
					HeadlessRunnerPlugin.PLUGIN_ID, "Interrupted", e1));
		}

		if (projects.length == 0) {
			System.out.println("No projects ready to test");
			return new TestSuite[0];
		}

		System.out.println("Searching for tests in projects:");
		for (final IProject project : projects) {
			final TestSuite suite = new TestSuite(project);
			System.out.println(String.format("    %s... ", project.getName()));
			IQ7Project q7Projet = RcpttCore.create(project);
			NamedElementCollector collector = null;
			if (conf.suites.size() == 0) {
				collector = new NamedElementCollector(HandleType.TestCase);
			} else {
				collector = new TestSuiteElementCollector(conf.suites, true);
			}
			try {
				q7Projet.accept(collector);
			} catch (ModelException e) {
				System.out.println(String.format("ERROR: %s", e.getMessage()));
				continue;
			}
			IWorkspaceFinder finder = WorkspaceFinder.getInstance();
			for (IQ7NamedElement element : collector.getElements()) {
				if (!(element instanceof ITestCase)) {
					continue;
				}
				ITestCase tcase = (ITestCase) element;
				if (isSkipExecuton(tcase)) {
					String tags = tcase.getTags();
					String skipBy = null;
					for (String s : conf.tagsToSkip) {
						if (tags.contains(s)) {
							skipBy = s;
						}
					}
					System.out.println("-- Testcase is skipped by tag \""
							+ skipBy + "\" -- " + tcase.getName());
					continue;
				}

				if (!matches(tcase.getName(), testNamePatterns)) {
					continue;
				}

				if (ensureIntegrity(tcase, finder, new HashSet<String>()))
					suite.add(tcase);

			}
			if (conf.suites.size() == 0)
				suite.sort();

			if (suite.getScenarios().size() == 0) {
				System.out.println(String.format("No tests found"));
			} else {
				tests.add(suite);
				System.out.println("Complete OK");
			}
		}
		return tests.toArray(new TestSuite[tests.size()]);
	}

	private boolean isSkipExecuton(ITestCase testCase) {
		try {
			return TagsUtil.hasAny(testCase, conf.tagsToSkip);
		} catch (ModelException e) {
			HeadlessRunnerPlugin.getDefault().info(
					"Failed to get tags for testcase:"
							+ testCase.getPath().toString());
		}
		return false;
	}

	private boolean ensureIntegrity(IQ7NamedElement tcase,
			IWorkspaceFinder finder, Set<String> processedIds) {
		if (!processedIds.add(tcase.getPath().toString())) {
			return true;// Already processed
		}
		IContext[] contexts = RcpttCore.getInstance().getContexts(tcase, finder, false);
		List<String> unresolved = new ArrayList<String>();
		for (IContext ctx : contexts) {
			if (ctx instanceof Q7InternalContext) {
				NamedElement namedElement;
				try {
					namedElement = ctx.getNamedElement();
					if (namedElement instanceof UnresolvedContext) {
						unresolved.add(ctx.getName());
					}
				} catch (ModelException e) {
					HeadlessRunnerPlugin.logErr("Failed to check testcase: "
							+ tcase.getName() + " for dependencies. Cause: "
							+ e.getMessage(), e);
					return false;
				}
			} else {
				if (!ensureIntegrity(ctx, finder, processedIds)) {
					return false;
				}
			}
		}
		if (unresolved.size() > 0) {
			try {
				System.out.println("-- Testcase is skipped because of incorrect dependencies -- "
								+ tcase.getName()
								+ ". Requires contexts: ["
								+ Joiner.on(",").join(unresolved) + "]");
			} catch (Exception e) {
				HeadlessRunnerPlugin
						.logErr("Failed to check testcase: " + tcase.getName()
								+ " for dependencies. Cause: " + e.getMessage(),
								e);
				return false;
			}
			return false;
		}

		return true;
	}

	private Q7ReportIterator runTests(final TestSuite[] tests) throws DebugException, AutLaunchFail {
		Assert.isTrue(auts.isClean(), "AUTs manages should not have been used before");
		List<ScenarioRunnable> runnables = Collections.synchronizedList(new ArrayList<ScenarioRunnable>());
		failedCount = 0;
		File reportFile = new File(conf.getQ7ReportLocation());

		createFolderForFile(reportFile);

		Set<String> failed = new HashSet<String>();
		try (SherlockReportOutputStream reportWriter = new SherlockReportOutputStream(
					new BufferedOutputStream(new FileOutputStream(reportFile)))) {
			int count = 0;
			for (final TestSuite suite : tests) {
				suite.setLimit(conf.limit);
				count += suite.getScenarios().size();
			}
			int artifacts = 0;
			for (final TestSuite suite : tests) {
				for (final IQ7NamedElement scenario : suite.getScenarios()) {
					ScenarioRunnable runnable = new ScenarioRunnable(
							resultsHandler, scenario, suite, "("
									+ Integer.valueOf(artifacts) + " from "
									+ Integer.valueOf(count) + ")",
							reportWriter);
					runnables.add(runnable);
					artifacts++;
				}
			}
			System.out.println("Testcase Artifacts:" + artifacts);

			List<Q7TestCase> testCases = new ArrayList<Q7TestCase>();
			for (final TestSuite suite : tests) {
				for (final IQ7NamedElement scenario : suite.getScenarios()) {
					if (scenario instanceof Q7TestCase) {
						testCases.add((Q7TestCase) scenario);
					}
				}
			}
			TestEngineManager.getInstance()
					.fireTestRunStarted(conf.testEngines, testCases);

			auts.initShutdownHook();
			auts.launchAutsAndStartTheirThreads(runnables);

			long startTime = System.currentTimeMillis();
			int processed = 0;
			while (true) {
				long current = System.currentTimeMillis();
				// Checks for execution timeout
				if (startTime + conf.executionTimeout * 1000 < current) {
					for (final AutThread thread : auts.autThreads) {
						if (thread.isAlive()) {
							thread.cancel();
							try {
								thread.join();
							} catch (InterruptedException e) {
								HeadlessRunnerPlugin.getDefault().info(
										"Exception during join for AUT thread termination because of timeout", e);
							}
						}
					}

					skipRemaining(runnables, "Global execution timeout");
					reporter.displayTimeoutMessage = true;
				}

				// Prints current progress
				int executed = count - runnables.size();
				int timeLeft = (int) ((1.0 + current - startTime) / 1000);
				int est = (int) ((1.0 * timeLeft / (1 + executed)) * runnables.size());
				synchronized (resultsHandler.results) {
					for (TestResult r : resultsHandler.results) {
						processed++;
						if (r.failed) {
							failed.add(r.name);
						}
						String out = r.userFriendlyMessage(est, timeLeft, processed, artifacts, failed.size());
						HeadlessRunnerPlugin.getDefault().info(out, null);
						System.out.println(out);
					}
					resultsHandler.results.clear();
				}

				// Checks for alive threads
				boolean alive = false;
				for (final AutThread thread : auts.autThreads) {
					if (thread.isAlive()) {
						alive = true;
						break;
					}
				}
				if (!alive) {
					// No alive threads -> finish
					if (!runnables.isEmpty()) {
						error.compareAndSet(null, new AutLaunchFail("AUT is not available", null));
					}
					skipRemaining(runnables, "AUT is not available");
					break;
				}

				Thread.sleep(100);
			}

			if (failed.size() > 0) {
				System.out.println("Failed Tests:");
				for (String scenario : failed) {
					System.out.println(scenario);
				}
			}
		} catch (FileNotFoundException e1) {
			System.out.println("Failed to create report file:" + conf.getQ7ReportLocation());
			HeadlessRunnerPlugin.getDefault().info("Failed to create report file:" + conf.getQ7ReportLocation());
			throw new RuntimeException(e1);
		} catch (InterruptedException e) {
			HeadlessRunnerPlugin.getDefault().info("Execution interrupted");
		}
		finally {
			skipRemaining(runnables, "Execution finished");
			HeadlessRunnerPlugin.getDefault().info("Shut down AUTs: executed");
			try {
				auts.shutdownAUTs();
			} catch (CoreException e) {
				HeadlessRunnerPlugin.log(e.getStatus());
			}
			auts.removeShutdownHook();
			failedCount = failed.size();
			TestEngineManager.getInstance().fireTestRunCompleted();
		}

		if (reportFile.exists()) {
			return new Q7ReportIterator(reportFile);
		}
		return null;
	}

	/**
	 * Skips remaining runnables.
	 * 
	 * @param runnables
	 * @param message
	 * @throws CoreException
	 */
	private void skipRemaining(List<ScenarioRunnable> runnables, String message) {
		if (runnables.size() > 0) {
			System.out.println("Skip remaining: " + message);
			HeadlessRunnerPlugin.getDefault().info("Skip remaining: " + message);
			for (ScenarioRunnable runnable : runnables) {
				runnable.skip(message, true);
			}
			runnables.clear();
		}
	}

	private void createFolderForFile(File reportFile) {
		if (reportFile.isDirectory()) {
			throw new RuntimeException("Requested report file " + reportFile
							+ " already exists and is a directory. It should not exists or be a file. Remove it or configure a different report location.");
		}
		final File parent = reportFile.getParentFile();
		if (parent == null) {
			System.out.println("ERROR: invalid report location: " + reportFile);
		} else {
			parent.mkdirs();
			if (!reportFile.exists()) {
				try {
					reportFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (!reportFile.canWrite()) {
				System.out.println("ERROR: report file is not writable: "
						+ reportFile);
			}
		}
	}

	public void throwOnError() throws Exception {
		Exception result = error.get();
		if (result != null) {
			throw result;
		}
	}

}
