package org.eclipse.rcptt.internal.launching;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.rcptt.core.ecl.core.model.GetReport;
import org.eclipse.rcptt.core.ecl.core.model.ResetVerifications;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.ecl.core.Sequence;
import org.eclipse.rcptt.ecl.debug.commands.CommandsFactory;
import org.eclipse.rcptt.ecl.debug.commands.ServerInfo;
import org.eclipse.rcptt.ecl.debug.commands.StartServer;
import org.eclipse.rcptt.ecl.debug.commands.StopServer;
import org.eclipse.rcptt.ecl.debug.core.NullDebuggerTransport;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.IExecutionSession;
import org.eclipse.rcptt.parameters.ResetParams;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class Q7LaunchManagerTest {
	private final ITestCase testCase = mock(ITestCase.class);
	private final AutLaunch aut = mock(AutLaunch.class);
	{
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
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(configuration.getName()).thenReturn("configurationName");
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
		Mockito.verify(aut).debug(ArgumentMatchers.eq(testCase), ArgumentMatchers.any(),	ArgumentMatchers.any(), ArgumentMatchers.any()); 
		Mockito.verify(aut).execute(ArgumentMatchers.isA(GetReport.class));
		Mockito.verify(aut).execute(ArgumentMatchers.isA(StopServer.class));
	}
	
	@Test
	public void propagateInternalFailures() throws CoreException, InterruptedException {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(configuration.getName()).thenReturn("configurationName");
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
	
	@Before
	public void before() {
		for (IExecutionSession session: Q7LaunchManager.getInstance().getExecutionSessions()) {
			Q7LaunchManager.getInstance().removeExecutionSession(session);
		}
	}
	
	
}
