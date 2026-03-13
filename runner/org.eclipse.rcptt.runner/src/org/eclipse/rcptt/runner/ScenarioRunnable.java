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

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.launching.PrepareExecutionWrapper;
import org.eclipse.rcptt.internal.launching.Q7TestLaunch;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.IExecutionSession;
import org.eclipse.rcptt.launching.Q7Launcher;
import org.eclipse.rcptt.launching.utils.TestSuiteUtils;
import org.eclipse.rcptt.runner.util.AutThread;
import org.eclipse.rcptt.runner.util.ResultsHandler;
import org.eclipse.rcptt.runner.util.TestResult;
import org.eclipse.rcptt.runner.util.TestSuite;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.streams.SherlockReportOutputStream;

import com.google.common.base.Preconditions;

public class ScenarioRunnable {
	private final IQ7NamedElement scenario;
	public final TestSuite suite;
	private final String msg;
	private final SherlockReportOutputStream reportWriter;
	private final ResultsHandler resultsHandler;

	public ScenarioRunnable(ResultsHandler resultsHandler,
			final IQ7NamedElement scenario, final TestSuite suite,
			final String msg, SherlockReportOutputStream reportWriter) {
		checkNotNull(resultsHandler);
		checkNotNull(scenario);
		checkNotNull(suite);
		checkNotNull(msg);
		checkNotNull(reportWriter);
		this.resultsHandler = resultsHandler;
		this.scenario = scenario;
		this.suite = suite;
		this.msg = msg;
		this.reportWriter = reportWriter;
	}

	public void run(AutThread worker) throws CoreException, InterruptedException {
		TestResult result = null;
		Report report = null;
		long totalTime = 0;

		try {
			HeadlessRunnerPlugin.getDefault().info(worker + " Starting: " + scenario.getName() + " " + msg);

			final IExecutionSession exec = Q7Launcher.getInstance().execute(new IQ7NamedElement[] { scenario },
					worker.launch.getAut(), "Test cases");
			Q7TestLaunch testLaunch = exec.getLaunch();
			try {
				while (exec.isRunning()) {
					Thread.sleep(500);
				}
				IExecutable[] executables = exec.getExecutables();
				for (IExecutable e: executables) {
					report = makeReport(e);
					if (!e.getResultStatus().isOK()) {
						break;
					}
				}
				if (executables == null || executables.length == 0) {
					HeadlessRunnerPlugin.getDefault()
							.info(
									worker + " Executing test case " + scenario.getName()
											+ " is failed with unknown reason...");
				}
				suite.executablesMap.put(scenario, executables);
				totalTime = exec.getTotalTime();
				result = TestResult.collectFrom(executables, scenario.getElementName(), totalTime);
			} finally {
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(testLaunch);
			}
		} catch (final Throwable e) {
			result = fail(TestSuiteUtils.toString(e), false);
			HeadlessRunnerPlugin.logErr("Failed to execute " + scenario.getName(), e);
		} finally {
			if (reportWriter != null) {
				if (report != null) {
					reportWriter.write(report);
				} else {
					addToReport(result);
				}
			}
			resultsHandler.testCompleted(result, worker);

			String finishMessage = worker + " Finished: " + scenario.getName() + " in " + totalTime;
			if (result.reason != null && !result.reason.isEmpty()) {
				finishMessage += ", " + result.reason;
			}
			HeadlessRunnerPlugin.getDefault().info(finishMessage + " " + msg);
		}
	}

	private Report makeReport(IExecutable executable) {
		Report report = executable.getResultReport();
		Preconditions.checkState(report != null, executable.getName() + " failed to emit non-null report");
		return report;
	}

	/**
	 * Creates skipped TestResult and adds it to report if needed.
	 * 
	 * @param message
	 * @param addToReport
	 * @return
	 */
	public TestResult skip(String message, boolean addToReport) {
		TestResult result = new TestResult(false, true, getScenarioName(), message, 0, false, false, false);
		if (addToReport) {
			addToReport(result);
		}
		return result;
	}

	/**
	 * Creates failed TestResult and adds it to report if needed.
	 * 
	 * @param message
	 * @param addToReport
	 * @return
	 */
	public TestResult fail(String message, boolean addToReport) {
		TestResult result = new TestResult(true, false, getScenarioName(), message, 0, false, false, false);
		if (addToReport) {
			addToReport(result);
		}
		return result;
	}

	/**
	 * Writes test result to report.
	 * 
	 * @param result
	 */
	private void addToReport(TestResult result) {
		if (reportWriter == null)
			return;

		if (result.failed) {
			reportWriter.write(TestSuiteUtils.generateFailedReport((ITestCase) scenario, result.reason));
		} else if (result.skipped) {
			reportWriter.write(TestSuiteUtils.generateSkippedReport((ITestCase) scenario, result.reason));
		}
	};

	private String getScenarioName() {
		String name = scenario.getName();
		try {
			name = scenario.getElementName();
		} catch (ModelException e) {
		}

		return name;
	}

}