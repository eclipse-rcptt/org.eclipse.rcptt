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
package org.eclipse.rcptt.internal.launching;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.ecl.core.model.GetReport;
import org.eclipse.rcptt.core.ecl.core.model.ResetVerifications;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.workspace.IWorkspaceFinder;
import org.eclipse.rcptt.ecl.core.Sequence;
import org.eclipse.rcptt.ecl.debug.commands.CommandsFactory;
import org.eclipse.rcptt.ecl.debug.commands.ServerInfo;
import org.eclipse.rcptt.ecl.debug.commands.StartServer;
import org.eclipse.rcptt.ecl.debug.commands.StopServer;
import org.eclipse.rcptt.ecl.debug.core.NullDebuggerTransport;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.IExecutionSession;
import org.eclipse.rcptt.launching.ILaunchListener;
import org.eclipse.rcptt.parameters.ResetParams;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.util.RcpttReportGenerator;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.eclipse.rcptt.util.StatusUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

public class Q7LaunchManagerTest {
	
	@Rule
	public final MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);
	private final ITestCase testCase = mock(ITestCase.class);
	private final AutLaunch aut = mock(AutLaunch.class);
	private final ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);

	{
		when(configuration.getName()).thenReturn("configurationName");
		try {
			when(testCase.getID()).thenReturn("id");
			when(testCase.getName()).thenReturn("name");
			when(testCase.getContexts()).thenReturn(new String[0]);
			when(testCase.getElementType()).thenReturn(HandleType.TestCase);
			when(testCase.getVerifications()).thenReturn(new String[0]);
			when(testCase.getTags()).thenReturn("");
			Scenario scenario = ScenarioFactory.eINSTANCE.createScenario();
			scenario.setId("id");
			when(testCase.getModifiedNamedElement()).thenReturn(scenario);
			
			Report report = ReportFactory.eINSTANCE.createReport();
			Node root = ReportFactory.eINSTANCE.createNode();
			ReportHelper.getInfo(root).setId("id");
			report.setRoot(root);
			when(aut.execute(ArgumentMatchers.isA(GetReport.class))).thenReturn(report);
			ServerInfo info = CommandsFactory.eINSTANCE.createServerInfo();
			when(aut.execute(ArgumentMatchers.isA(StartServer.class))).thenReturn(info);
		} catch (CoreException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void debug() throws CoreException, InterruptedException {
		Q7TestLaunch launch = new Q7TestLaunch(configuration, ILaunchManager.DEBUG_MODE);
		Q7LaunchManager.getInstance().execute(new IQ7NamedElement[] {testCase}, aut, launch, null, Collections.emptyMap(), (ignored1, ignored2) -> new NullDebuggerTransport());
		while (!launch.isTerminated()) {
			Thread.sleep(100);
		}
		IStatus result = Q7LaunchManager.getInstance().getExecutionSessions()[0].getResultStatus();
		if (result.getException() != null) {
			throw new AssertionError(result.getException());
		}
		Assert.assertTrue(result.getMessage(), result.isOK());
		Mockito.verify(aut).execute(ArgumentMatchers.isA(StartServer.class));
		Mockito.verify(aut).getCapability();
		Mockito.verify(aut).execute(ArgumentMatchers.isA(Sequence.class));
		Mockito.verify(aut).execute(ArgumentMatchers.isA(ResetParams.class));
		Mockito.verify(aut).execute(ArgumentMatchers.isA(ResetVerifications.class));
		Mockito.verify(aut).debug(ArgumentMatchers.isA(IContext.class), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
		Mockito.verify(aut).debug(ArgumentMatchers.eq(testCase), any(),	ArgumentMatchers.any(), ArgumentMatchers.any()); 
		Mockito.verify(aut).execute(ArgumentMatchers.isA(GetReport.class));
		Mockito.verify(aut).execute(ArgumentMatchers.isA(StopServer.class));
	}
	
	@Test
	public void propagateInternalFailures() throws CoreException, InterruptedException {
		Q7TestLaunch launch = new Q7TestLaunch(configuration, ILaunchManager.RUN_MODE);
		String MESSAGE = "ERROR MESSAGE MOCK";
		when(aut.execute(ArgumentMatchers.isA(ResetParams.class))).thenThrow(new IllegalArgumentException(MESSAGE));
		Q7LaunchManager.getInstance().execute(new IQ7NamedElement[] {testCase}, aut, launch, null, Collections.emptyMap(), (ignored1, ignored2) -> new NullDebuggerTransport());
		while (!launch.isTerminated()) {
			Thread.sleep(100);
		}
		IStatus result = Q7LaunchManager.getInstance().getExecutionSessions()[0].getResultStatus();
		Assert.assertEquals(MESSAGE, result.getMessage());
	}
	
	@Test
	public void notifyContextCompletion() throws CoreException, InterruptedException {
		Q7TestLaunch launch = new Q7TestLaunch(configuration, ILaunchManager.RUN_MODE);
		IWorkspaceFinder finder = mock();
		IContext context = mock();
		when(finder.findContext(testCase, "contextId")).thenReturn(new IContext[] {context});
		when(testCase.getContexts()).thenReturn(new String[] {"contextId"});
		when(context.getID()).thenReturn("contextId");
		when(context.getType()).thenReturn(ContextTypeManager.getInstance().getTypes()[0]);
		when(context.getElementName()).thenReturn("ContextName");
		
		Q7LaunchManager subject = Q7LaunchManager.getInstance();
		List<String> completedExecutableNames = Collections.synchronizedList(new ArrayList<>());
		subject.addListener(new ILaunchListener() {
			@Override
			public void started(IExecutionSession session) {
			}
			
			@Override
			public void launchStatusChanged(IExecutable... executable) {
				for (var i: executable) {
					completedExecutableNames.add(i.getName());
				}
			}
			
			@Override
			public void finished() {
			}
		});
		subject.execute(new IQ7NamedElement[] {testCase}, aut, launch, finder, Collections.emptyMap(), (ignored1, ignored2) -> new NullDebuggerTransport());
		while (!launch.isTerminated()) {
			Thread.sleep(100);
		}
		assertThat(completedExecutableNames, hasItem("ContextName"));
	}
	
	@Test
	public void doNotPostExecuteTwice() throws CoreException, InterruptedException {
		Q7TestLaunch launch = new Q7TestLaunch(configuration, ILaunchManager.RUN_MODE);
		IContext context = mock();
		when(context.getID()).thenReturn("id");
		when(context.getType()).thenReturn(ContextTypeManager.getInstance().getTypes()[0]);
		when(context.getElementType()).thenReturn(HandleType.Context);
		when(context.getElementName()).thenReturn("ContextName");
		
		Q7LaunchManager subject = Q7LaunchManager.getInstance();
		subject.execute(new IQ7NamedElement[] {context}, aut, launch, null, Collections.emptyMap(), (ignored1, ignored2) -> new NullDebuggerTransport());
		while (!launch.isTerminated()) {
			Thread.sleep(100);
		}
		IExecutionSession session = Q7LaunchManager.getInstance().getExecutionSessions()[0];
		// Double reading of report in org.eclipse.rcptt.internal.launching.PrepareExecutionWrapper.postExecute(IStatus) would cause an assertion error
		IStatus resultStatus = session.getExecutables()[0].getResultStatus();
		assertTrue(StatusUtil.format(resultStatus), resultStatus.isOK()); 
	}
	
	@Test
	public void retryOnFail() throws CoreException, InterruptedException {
		setRetries(3);
		
		Report[] reports = new Report[3];
		for (int i = 0; i<reports.length; i++) {
			Report report = ReportFactory.eINSTANCE.createReport();
			Node root = ReportFactory.eINSTANCE.createNode();
			Q7Info info = ReportHelper.getInfo(root);
			info.setId("id");
			info.setDescription("Description " + i);
			report.setRoot(root);
			reports[i] = report;
		}
		when(aut.execute(ArgumentMatchers.isA(GetReport.class))).thenReturn(reports[0], reports[1], reports[2]);
		
		CoreException coreException0 = new CoreException(Status.error("Test mock failure 0"));
		CoreException coreException1 = new CoreException(Status.error("Test mock failure 1"));
		doThrow(coreException0).doThrow(coreException1).doNothing().when(aut).run(any(), anyLong(), any(), any());
		
		Q7TestLaunch launch = new Q7TestLaunch(configuration, ILaunchManager.RUN_MODE);
		Q7LaunchManager.getInstance().execute(new IQ7NamedElement[] {testCase}, aut, launch, null, Collections.emptyMap(), (ignored1, ignored2) -> new NullDebuggerTransport());
		while (!launch.isTerminated()) {
			Thread.sleep(100);
		}
		IExecutionSession session = Q7LaunchManager.getInstance().getExecutionSessions()[0];
		assertEquals(1, session.getFailedCount());
		IExecutable retryExecutable = session.getExecutables()[0];
		Report aggregateReport = retryExecutable.getResultReport();
		String text = toString(aggregateReport);
		assertTrue(text.contains("attempt 0"));
		assertTrue(text.contains("attempt 1"));
		assertTrue(text.contains("attempt 2"));
		assertFalse(text.contains("attempt 3"));
		assertTrue(text.contains("Test mock failure 0"));
		assertTrue(text.contains("Test mock failure 1"));
		IExecutable[] children = retryExecutable.getChildren();
		assertEquals("Description 0", getReportDescription(children[0]));
		assertEquals("Description 1", getReportDescription(children[1])); // Ensure that reports are not exact copies
	}
	
	private String getReportDescription(IExecutable executable) {
		return ReportHelper.getInfo(executable.getResultReport().getRoot()).getDescription();
	}

	private String toString(Report aggregateReport) {
		StringWriter writer = new StringWriter();

		PrintWriter printWriter = new PrintWriter(writer);
		printWriter.println("Report:");
		new RcpttReportGenerator(printWriter,  new ArrayList<>()).writeReport(aggregateReport, 1);
		return writer.toString();
	}
	
	@Before
	public void before() {
		for (IExecutionSession session: Q7LaunchManager.getInstance().getExecutionSessions()) {
			Q7LaunchManager.getInstance().removeExecutionSession(session);
		}
		setRetries(1);
	}

	private void setRetries(int count) {
		Q7Features.getInstance().intOption(Q7Features.RETRY_TEST, 1).setValue("" + count);
	}
	
	
}
