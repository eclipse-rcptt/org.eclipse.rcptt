package org.eclipse.rcptt.internal.launching.aut;

import static org.eclipse.rcptt.ecl.core.util.Statuses.hasCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ecl.context.ContextFactory;
import org.eclipse.rcptt.core.ecl.context.EclContext;
import org.eclipse.rcptt.core.ecl.core.model.ExecutionPhase;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Global;
import org.eclipse.rcptt.ecl.core.RestoreState;
import org.eclipse.rcptt.ecl.core.SaveState;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.ecl.gen.ast.AstExec;
import org.eclipse.rcptt.ecl.internal.core.Pipe;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.internal.launching.aut.BaseAutLaunch.Context;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.tesla.ecl.model.SetupPlayer;
import org.eclipse.rcptt.tesla.ecl.model.ShoutdownPlayer;
import org.eclipse.rcptt.tesla.ecl.model.ShutdownAut;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class BaseAutLaunchTest {
	private static final Object RV = new Object(); 
	private final ILaunch launch = Mockito.mock(ILaunch.class);
	private final Context context = Mockito.mock(Context.class);
	private final ISession session = Mockito.mock(ISession.class);
	private final IProcess process = Mockito.mock(IProcess.class);
	{
		try {
			Answer returnRV = invocation -> {
				IPipe output = (IPipe) invocation.getArguments()[2];
				output.write(RV);
				return process;
			};
			when(launch.getAttribute(IQ7Launch.ATTR_HOST)).thenReturn("127.0.0.1");
			when(launch.getAttribute(IQ7Launch.ATTR_ECL_PORT)).thenReturn("9922");
			when(session.createPipe()).thenAnswer(invocation -> {
				return new Pipe();
			});
			when(context.connect("127.0.0.1", 9922)).thenReturn(session);
			when(process.waitFor(anyLong(), any())).thenReturn(Status.OK_STATUS);
			when(session.execute(isA(RestoreState.class))).thenReturn(process);
			when(session.execute(isA(ShutdownAut.class), isNull(IPipe.class), isA(IPipe.class))).thenReturn(process);
			when(session.execute(isA(ShoutdownPlayer.class), isNull(IPipe.class), isA(IPipe.class))).thenAnswer(returnRV);
			when(session.execute(isA(SaveState.class), isNull(IPipe.class), isA(IPipe.class))).thenAnswer(invocation -> {
				IPipe output = (IPipe) invocation.getArguments()[2];
				if (output == null)
					return null;
				output.write(CoreFactory.eINSTANCE.createSessionState());
				output.write(Status.OK_STATUS);
				return process;
			});
			when(session.execute(isA(AstExec.class))).thenReturn(process);
			Mockito.doAnswer(returnRV).when(session).execute(isA(Global.class), ArgumentMatchers.any(), isA(IPipe.class));
			when(session.execute(isA(SetupPlayer.class), isNull(IPipe.class), isA(IPipe.class))).thenAnswer(returnRV);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void simpleValidCommand() throws CoreException, InterruptedException {
		BaseAutLaunch subject = createSubject();
		Assert.assertEquals(RV, subject.execute(CoreFactory.eINSTANCE.createGlobal()));
	}
	
	@Test(timeout=1500)
	public void connectHangs() throws CoreException, InterruptedException, IOException {
		when(context.connect("127.0.0.1", 9922)).thenAnswer(invocation -> {
			Thread.sleep(1000);
			return process;
		});
		BaseAutLaunch subject = createSubject();
		try {
			subject.execute(CoreFactory.eINSTANCE.createGlobal(), 100);
			Assert.fail("Should fail with timeout");
		} catch (CoreException e) {
			if (!hasCode(e.getStatus(), IProcess.TIMEOUT_CODE))
				throw e;
		}
	}
	
	@Test(timeout=10000)
	public void extractScriptFromEclContext() throws CoreException {
		EclContext eclContext = ContextFactory.eINSTANCE.createEclContext();
		Script script = CoreFactory.eINSTANCE.createScript();
		script.setContent("scriptBody");
		eclContext.setScript(script);
		BaseAutLaunch subject = createSubject();
		IContext context = Mockito.mock(IContext.class);
		when(context.exists()).thenReturn(true);
		when(context.getModifiedNamedElement()).thenReturn(eclContext);
		ContextType type = Mockito.mock(ContextType.class);
		when(context.getType()).thenReturn(type);
		when(type.getId()).thenReturn("org.eclipse.rcptt.ctx.ecl");
		subject.run(context, 20000, null, ExecutionPhase.START);
		Mockito.verify(session).execute(isA(RestoreState.class));
		ArgumentCaptor<AstExec> command = ArgumentCaptor.forClass(AstExec.class); 
		//TODO: Find out why mock is called twice. It is supposed to only be called once
		Mockito.verify(session, Mockito.times(2)).execute(command.capture());
		Mockito.verify(session).execute(isA(SetupPlayer.class), ArgumentMatchers.isNull(IPipe.class), ArgumentMatchers.isA(IPipe.class));
		Mockito.verify(session).execute(isA(SaveState.class), ArgumentMatchers.isNull(IPipe.class), ArgumentMatchers.isA(IPipe.class));
		Assert.assertEquals("scriptBody", command.getValue().getName());
		Mockito.verify(session).execute(isA(ShoutdownPlayer.class), ArgumentMatchers.isNull(IPipe.class), ArgumentMatchers.isA(IPipe.class));
	}
	
	@Test(timeout=10000)
	public void setupPlayerRespectsTimeoutArgument() throws CoreException {
		Script script = CoreFactory.eINSTANCE.createScript();
		script.setContent("scriptBody");
		BaseAutLaunch subject = createSubject();
		// This implementation of SetupPlayer command should always cause timeout
		// Here timeout is achieved by NOT putting response in an output pipe
		when(session.execute(isA(SetupPlayer.class), ArgumentMatchers.any(), isA(IPipe.class)))
		.thenReturn(process);
		try {
			subject.execute(script, 500, null);
			Assert.fail("Should timeout");
		} catch (CoreException e) {
			if (!hasCode(e.getStatus(), IProcess.TIMEOUT_CODE))
				throw e;
		}
	}
	
	@Test(timeout=2000)
	public void setupPlayerRespectsCancel() throws CoreException {
		Script script = CoreFactory.eINSTANCE.createScript();
		script.setContent("scriptBody");
		BaseAutLaunch subject = createSubject();
		// This implementation of SetupPlayer command should always cause timeout
		// Here timeout is achieved by NOT putting response in an output pipe
		when(session.execute(isA(SetupPlayer.class), ArgumentMatchers.any(), isA(IPipe.class))).thenReturn(process);
		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			Job.createSystem("Cancel self", m -> {
				monitor.setCanceled(true);
			}).schedule(300);
			subject.execute(script, Integer.MAX_VALUE, monitor);
			Assert.fail("Should throw cancellation");
		} catch (CoreException e) {
			if (e.getStatus().getSeverity() != IStatus.CANCEL)
				throw e;
		}
	}
	
	@Test(timeout=2000)
	public void astExecRespectsCancel() throws CoreException {
		Script script = CoreFactory.eINSTANCE.createScript();
		script.setContent("scriptBody");
		BaseAutLaunch subject = createSubject();
		when(session.execute(isA(AstExec.class))).thenAnswer(ignored -> {
			Thread.sleep(10000);
			return process;
		});
		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			Job cancelJob = Job.createSystem("cancelJob", m -> {
				monitor.setCanceled(true);
			});
			cancelJob.setPriority(Job.INTERACTIVE);
			cancelJob.schedule(300);
			
			subject.execute(script, Integer.MAX_VALUE, monitor);
			Assert.fail("Should throw cancellation");
		} catch (CoreException e) {
			if (e.getStatus().getSeverity() != IStatus.CANCEL)
				throw e;
		}
	}
	
	@Test(timeout=200000)
	public void terminateOnShutdownTimeout() throws CoreException, InterruptedException {
		Mockito.when(launch.canTerminate()).thenReturn(true);
		BaseAutLaunch subject = createSubject();
		subject.gracefulShutdown(1);
		verify(launch).terminate();
	}
	

	private BaseAutLaunch createSubject() {
		return new BaseAutLaunch(launch,null, context);
	}
	
	@After
	public void after() {
		Mockito.validateMockitoUsage();
	}


}
