/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching.ext.tests;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.rcptt.core.ecl.core.model.ExecutionPhase;
import org.eclipse.rcptt.core.ecl.core.model.GetReport;
import org.eclipse.rcptt.core.launching.events.AutEvent;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.ecl.internal.core.ProcessStatusConverter;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.core.model.Q7InternalTestCase;
import org.eclipse.rcptt.internal.launching.Executable;
import org.eclipse.rcptt.internal.launching.ExecutionSession;
import org.eclipse.rcptt.internal.launching.GroupExecutable;
import org.eclipse.rcptt.internal.launching.PrepareExecutionWrapper;
import org.eclipse.rcptt.internal.launching.Q7LaunchManager;
import org.eclipse.rcptt.internal.launching.Q7LaunchManager.SessionRunnable;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutLaunchListener;
import org.eclipse.rcptt.launching.AutLaunchState;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.TestCaseDebugger;
import org.eclipse.rcptt.launching.utils.TestSuiteUtils;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.ReportingFactory;
import org.eclipse.rcptt.reporting.core.IQ7ReportConstants;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.LoggingCategory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.junit.Assert;
import org.junit.Test;

public class ExecutableTest {

	private final String launchId = "Launch id";

	private static Scenario createScenario(String id) {
		Scenario rv = ScenarioFactory.eINSTANCE.createScenario();
		rv.setId(id);
		return rv;
	}

	private static Executable createSleepExecutable(final String id) {

		return new Executable(false) {
			private final IQ7NamedElement element = new Q7InternalTestCase(ModelManager.getModelManager().getModel(),
					"Test name", createScenario(id));

			@Override
			public String getName() {
				return "Unit test";
			}

			@Override
			public AutLaunch getAut() {
				return null;
			}

			@Override
			public IQ7NamedElement getActualElement() {
				return element;
			}

			@Override
			public Report getResultReport() {
				return null;
			}

			@Override
			public int getType() {
				return IExecutable.TYPE_SCENARIO;
			}

			@Override
			public ExecutionPhase getPhase() {
				return ExecutionPhase.RUN;
			}

			@Override
			public Executable[] getChildren() {
				return new Executable[0];
			}

			@Override
			protected IStatus execute() throws InterruptedException {
				Thread.sleep(1000);
				return Status.OK_STATUS;
			}
		};
	}

	AutLaunch createLaunch() {
		return new AutLaunch() {

			@Override
			public void waitForRestart(IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub

			}

			@Override
			public void terminate() {
				// TODO Auto-generated method stub

			}

			@Override
			public void shutdown() {
				// TODO Auto-generated method stub

			}

			@Override
			public void run(IQ7NamedElement test, long timeout, IProgressMonitor monitor, ExecutionPhase phase)
					throws CoreException {
				// TODO Auto-generated method stub

			}

			@Override
			public void resetState() {
				// TODO Auto-generated method stub

			}

			@Override
			public void removeListener(AutLaunchListener listener) {
				// TODO Auto-generated method stub

			}

			@Override
			public void ping() throws CoreException, InterruptedException {
				// TODO Auto-generated method stub

			}

			@Override
			public void handleAutEvent(AutEvent autEvent) {
				// TODO Auto-generated method stub

			}

			@Override
			public AutLaunchState getState() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ILaunch getLaunch() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getId() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Aut getAut() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void execute(Script script, long timeout, IProgressMonitor monitor) throws CoreException {
				// TODO Auto-generated method stub

			}

			@Override
			public Object execute(Command command, long timeout, IProgressMonitor monitor)
					throws CoreException,
					InterruptedException {
				if (command instanceof GetReport) {
					Report report = ReportFactory.eINSTANCE.createReport();
					Node root = ReportFactory.eINSTANCE.createNode();
					root.setName("Element ID");
					report.setRoot(root);
					Q7Info q7info = ReportingFactory.eINSTANCE.createQ7Info();
					q7info.setId("some_id");
					root.getProperties().put(IQ7ReportConstants.ROOT, q7info);
					root.setEndTime(root.getStartTime() + 100);
					root.setDuration(root.getEndTime() - root.getStartTime());
					return report;
				}
				return null;
			}

			@Override
			public Object execute(Command command, long timeout) throws CoreException, InterruptedException {
				return execute(command, timeout, new NullProgressMonitor());
			}

			@Override
			public Object execute(Command command) throws CoreException, InterruptedException {
				return execute(command, 10);
			}

			@Override
			public void debug(IQ7NamedElement test, IProgressMonitor monitor, TestCaseDebugger debugger,
					ExecutionPhase phase) throws CoreException {
				// TODO Auto-generated method stub

			}

			@Override
			public void cancelTestExecution() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addListener(AutLaunchListener listener) {
				// TODO Auto-generated method stub

			}

			@Override
			public String getCapability() {
				return null;
			}
		};

	}

	@Test
	public void testTime() throws InterruptedException {
		Executable secondSleepExecutable = createSleepExecutable("some_id");
		ExecutionSession session = new ExecutionSession(launchId, new Executable[] { secondSleepExecutable }, null,
				null);
		run(session);
		assertInRange("Executable time", secondSleepExecutable.getTime(), 900, 7000);
		assertInRange("Session time", session.getTotalTime(), 900, 7000);
	}

	@Test
	public void testPrepareExecution() throws InterruptedException, ModelException {
		Executable secondSleepExecutable = createSleepExecutable("some_id");
		Executable executable = new PrepareExecutionWrapper(createLaunch(), secondSleepExecutable);
		ExecutionSession session = new ExecutionSession(launchId, new Executable[] { executable }, null, null);
		run(session);
		assertInRange("Executable time", executable.getTime(), 900, 7000);
		assertInRange("Session time", session.getTotalTime(), 900, 7000);
	}

	@Test
	public void testGroupExecutable() throws InterruptedException, ModelException {
		Executable secondSleepExecutable = createSleepExecutable("some_id");
		GroupExecutable group = new GroupExecutable(secondSleepExecutable, Arrays.asList(secondSleepExecutable));
		Executable executable = new PrepareExecutionWrapper(createLaunch(), group);
		ExecutionSession session = new ExecutionSession(launchId, new Executable[] { executable }, null, null);
		run(session);
		assertInRange("Executable time", executable.getTime(), 900, 7000);
		assertInRange("Session time", session.getTotalTime(), 900, 7000);
	}

	@Test(expected = NullPointerException.class)
	public void testFailOnNpe() throws InterruptedException, ModelException {
		Executable secondSleepExecutable = createSleepExecutable(null);
		new PrepareExecutionWrapper(createLaunch(), secondSleepExecutable);
	}

	private void run(ExecutionSession session) throws InterruptedException {
		final SessionRunnable sessionRunnable = new SessionRunnable(launchId, session, null);
		Q7LaunchManager.getInstance().execute(launchId, session, sessionRunnable);
		try {
			int i = 0;
			while (session.isRunning()) {
				Thread.sleep(100);
				if (i++ > 10000) {
					throw new RuntimeException("Timed out");
				}
			}
			Q7LaunchManager.getInstance().stop(Status.OK_STATUS);
			IStatus result = session.getResultStatus();
			if (result.getException() != null) {
				throw new AssertionError(result.getException());
			}
			Assert.assertTrue(result.toString(), result.isOK());
		} catch (Throwable e) {
			Q7LaunchManager.getInstance().stop(RcpttPlugin.createStatus(e));
			throw e;
		}
	}

	private void assertInRange(String message, double value, double min, double max) {
		Assert.assertTrue(String.format("%s: %f is out of range [%f, %f)", message, value, min, max), min <= value
				&& value < max);
	}

}
