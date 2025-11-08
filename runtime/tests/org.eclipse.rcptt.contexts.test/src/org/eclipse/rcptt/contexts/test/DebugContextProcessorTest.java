/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.contexts.test;

import static java.lang.System.currentTimeMillis;
import static org.eclipse.rcptt.debug.DebugFactory.eINSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.debug.DebugContext;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DebugContextProcessorTest {

	private final ContextTypeManager contexts = ContextTypeManager.getInstance();
	private final ISession session = EclRuntime.createSession();
	private static final ILaunchManager LAUNCH_MANAGER = DebugPlugin.getDefault().getLaunchManager();
	
	@After
	public void after() throws CoreException {
		session.close();
	}
	
	@Before
	public void before() {
		ReportManager.createReport("", null);
	}
	
	@Test
	public void terminateNoLaunches() throws CoreException, InterruptedException, ExecutionException {
		DebugContext context = eINSTANCE.createDebugContext();
		context.setNoLaunches(true);
		CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
			try {
				contexts.apply(context, session);
			} catch (CoreException e) {
				throw new AssertionError(e);
			}
		});
		waitFor(result, 10_000);
	}

	
//	<?xml version="1.0" encoding="UTF-8" standalone="no"?>
//	<launchConfiguration type="org.eclipse.ui.externaltools.ProgramLaunchConfigurationType">
//	    <booleanAttribute key="org.eclipse.debug.core.ATTR_FORCE_SYSTEM_CONSOLE_ENCODING" value="false"/>
//	    <mapAttribute key="org.eclipse.debug.core.environmentVariables">
//	        <mapEntry key="JAVA_HOME" value="/Library/Java/JavaVirtualMachines/jdk-17.0.4.1.jdk/Contents/Home"/>
//	        <mapEntry key="PATH" value="/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"/>
//	    </mapAttribute>
//	    <stringAttribute key="org.eclipse.ui.externaltools.ATTR_LOCATION" value="/bin/bash"/>
//	    <stringAttribute key="org.eclipse.ui.externaltools.ATTR_TOOL_ARGUMENTS" value="build_nodeps.sh"/>
//	    <stringAttribute key="org.eclipse.ui.externaltools.ATTR_WORKING_DIRECTORY" value="/Users/vasiligulevich/git/org.eclipse.rcptt/"/>
//	</launchConfiguration>

	@Test
	public void terminateALaunch() throws CoreException, InterruptedException, ExecutionException {
		assertEquals(0, LAUNCH_MANAGER.getLaunches().length);
		ILaunch launch = createSleepLaunch(30);
		assertTrue(launch.canTerminate());
		assertFalse(launch.isTerminated());
		assertEquals(1, LAUNCH_MANAGER.getLaunches().length);
		DebugContext context = eINSTANCE.createDebugContext();
		context.setNoLaunches(true);
		CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
			try {
				contexts.apply(context, session);
			} catch (CoreException e) {
				throw new AssertionError(e);
			}
		});
		waitFor(result, 10_000);
		assertTrue(launch.isTerminated());
		assertEquals(0, LAUNCH_MANAGER.getLaunches().length);
	}

	private ILaunch createSleepLaunch(int seconds) throws CoreException {
		ILaunchConfigurationType type = LAUNCH_MANAGER.getLaunchConfigurationType("org.eclipse.ui.externaltools.ProgramLaunchConfigurationType");
		ILaunchConfigurationWorkingCopy launchConfiguration = type.newInstance(null, "Test1");
		launchConfiguration.setAttributes(Map.of("org.eclipse.ui.externaltools.ATTR_LOCATION", "/bin/sleep", "org.eclipse.ui.externaltools.ATTR_TOOL_ARGUMENTS", "" + seconds));
		ILaunch launch = launchConfiguration.launch("run", null);
		return launch;
	}


	public static void waitFor(CompletableFuture<Void> result, int timeout_ms)
			throws AssertionError{
		try {
			Display display = Display.getCurrent();
			long stop = currentTimeMillis() + timeout_ms;
			while (!result.isDone()) {
				if (currentTimeMillis() > stop) {
					Thread.getAllStackTraces().forEach((thread, elements) -> {
						PrintStream out = System.out;
						out.printf("\"%s\" #%d\n", thread.getName(), thread.getId());
						out.printf("   java.lang.Thread.State:  %s\n", thread.getState());
						for (StackTraceElement i: elements) {
							out.printf("\tat %s.%s(%s:%d)\n", i.getClassName(), i.getMethodName(), i.getFileName(), i.getLineNumber());	
						}
						out.println();
					});
					throw new AssertionError("Timeout");
				}
				if (!display.readAndDispatch()) {
					CompletableFuture.runAsync(() -> display.wake());
					display.sleep(); // Dangerous, may deadlock, but without this, UIRunnable is never executed
				}
			}
			result.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new AssertionError(e);
		} finally {
			result.cancel(true);
		}
	}

}
