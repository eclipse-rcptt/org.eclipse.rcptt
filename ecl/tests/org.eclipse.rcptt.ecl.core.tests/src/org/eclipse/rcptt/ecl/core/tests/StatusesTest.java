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
package org.eclipse.rcptt.ecl.core.tests;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.util.Statuses;
import org.eclipse.rcptt.ecl.parser.EclCoreParser;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StatusesTest {

	private static final NullProgressMonitor MONITOR = new NullProgressMonitor();

	private ISession session = null;

	@Before
	public void init() {
		Assert.assertNull(session);
		session = EclRuntime.createSession();
	}

	@After
	public void close() throws CoreException {
		session.close();
		session = null;
	}

	private static IStatus status(int severity, int code) {
		return new Status(severity, "plugin", code, "", null);
	}

	private static MultiStatus multiStatus(int code) {
		return new MultiStatus("plugin", code, "OK", null);
	}

	@Test
	public void testHasCode() {
		MultiStatus status = multiStatus(0);
		status.add(status(0, 0));
		Assert.assertFalse(Statuses.hasCode(status, 1));
		status = multiStatus(0);
		status.add(status(0, 1));
		Assert.assertTrue(Statuses.hasCode(status, 1));
		status = multiStatus(1);
		Assert.assertTrue(Statuses.hasCode(status, 1));
	}
	
	@Test
	public void testStackTrace() throws CoreException, InterruptedException {
		Command failingCommand = parse("invoke-static -pluginId \"org.eclipse.rcptt.ecl.core.tests\" -className org.eclipse.rcptt.ecl.core.tests.StatusesTest -methodName throwException");
		IProcess process = session.execute(failingCommand);
		IStatus result = process.waitFor(200, MONITOR);
		Assert.assertNotNull(result.getChildren()[0].getException());
	}

	private Command parse(String input) throws CoreException {
		return EclCoreParser.newCommand(input);
	}
	
	Command sleepCommand(long milliseconds) throws CoreException {
		return parse("invoke-static -pluginId \"org.eclipse.core.runtime\" -className java.lang.Thread -methodName sleep -args "
				+ milliseconds);
	}
	@Test 
	public void testNoTimeout() throws InterruptedException, CoreException {
		IProcess process = session.execute(sleepCommand(100));
		IStatus result = process.waitFor(200, MONITOR);
		Assert.assertTrue(result.getMessage(), result.isOK());
		Assert.assertFalse(Statuses.hasCode(result, IProcess.TIMEOUT_CODE));
	}

	@Test
	public void testTimeout() throws InterruptedException, CoreException {
		IProcess process = session.execute(sleepCommand(200));
		IStatus result = process.waitFor(50, MONITOR);
		Assert.assertFalse(result.isOK());
		Assert.assertTrue(result.getMessage(), Statuses.hasCode(result, IProcess.TIMEOUT_CODE));
	}
	
	public static void throwException() {
		throw new RuntimeException("Test failure");
	}
}
