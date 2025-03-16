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
package org.eclipse.rcptt.ecl.internal.client.tcp.tests;

import com.google.common.base.Function;
import com.google.common.io.Closer;

import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.client.tcp.EclTcpClientManager;
import org.eclipse.rcptt.ecl.client.tcp.tests.EclInjectedCommandService;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.platform.commands.CommandsFactory;
import org.eclipse.rcptt.ecl.platform.commands.ListPlugins;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.ecl.server.tcp.EclTcpServerManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class TestTcpSession {

	private static final int PORT = EclTcpClientManager.DEFAULT_PORT + 1;

	private static final String LOCALHOST = "127.0.0.1";

	private final Closer closer = Closer.create();

	private ISession session;

	@Before
	public void before() throws IOException {
		EclTcpServerManager.Instance.startServer(PORT);
		closer.register(new Closeable() {
			@Override
			public void close() throws IOException {
				EclTcpServerManager.Instance.stopServer(PORT);
			}
		});
		session = EclTcpClientManager.Instance.startClientSession(InetAddress.getByName(LOCALHOST), PORT);
		closer.register(new Closeable() {
			@Override
			public void close() throws IOException {
				try {
					session.close();
				} catch (CoreException e) {
					throw new IOException(e);
				}
			}
		});
		EclInjectedCommandService.inject(ignored -> Status.OK_STATUS);
	}

	@After
	public void closeServer() throws CoreException, IOException {
		closer.close();
	}

	@Test
	public void simpleExecution() throws CoreException, InterruptedException {
		EclInjectedCommandService.inject(ignored ->  new Status(IStatus.INFO, "id", "message"));
		IStatus status = executeCommand();
		Assert.assertFalse(status.getMessage(), status.matches(IStatus.ERROR | IStatus.WARNING | IStatus.CANCEL));
		Assert.assertTrue(status.matches(IStatus.INFO));
		Assert.assertEquals("message", status.getMessage());
	}

	private IStatus executeCommand() throws CoreException, InterruptedException {
		IProcess process = session
				.execute(org.eclipse.rcptt.ecl.client.tcp.tests.TestsFactory.eINSTANCE.createEclInjectedCommand());
		IStatus status = process.waitFor();
		return status;
	}

	@Test
	public void errorIsReported() throws CoreException, InterruptedException {
		EclInjectedCommandService.inject(ignored ->  new Status(IStatus.ERROR, "id", "message"));
		IStatus status = executeCommand();
		Assert.assertTrue(status.matches(IStatus.ERROR));
		Assert.assertEquals("message", status.getMessage());
	}

	private static void absurdFunctionThatThrows() {
		IStatus status = new Status(IStatus.ERROR, "id", "message2", new IllegalStateException("message3"));
		CoreException coreException = new CoreException(status);
		throw new RuntimeException(coreException);
	}

	@Test
	public void istatusPropagatesTraces() throws CoreException, InterruptedException {
		EclInjectedCommandService.inject( ignored -> {
				absurdFunctionThatThrows();
				return Status.OK_STATUS;
			});
		IStatus status = executeCommand();
		Throwable e = status.getException();
		while (! (e instanceof CoreException)) {
			if (e == null)
				Assert.fail("CoreException is not found");
			e = e.getCause();
		}
		CoreException coreException = (CoreException) e;
		IllegalStateException illegalStateException = (IllegalStateException) coreException.getCause();
		Assert.assertEquals("message3", illegalStateException.getMessage());
		boolean found = false;
		for (StackTraceElement element: illegalStateException.getStackTrace()) {
			if (element.getMethodName().endsWith("absurdFunctionThatThrows")) {
				found = true;
				break; 
			}
		}
		Assert.assertTrue(found);
	}

	@Test
	public void testCommandExecution001() throws Throwable {
		ListPlugins listPlugins = CommandsFactory.eINSTANCE.createListPlugins();
		IProcess process = session.execute(listPlugins);
		IPipe input = process.getOutput();
		int count1 = 0;
		int count2 = 0;
		while (true) {
			Object object = input.take(Long.MAX_VALUE);
			if (object == null || object instanceof IStatus) {
				break;
			}
			count1++;
		}
		IProcess process2 = session.execute(listPlugins);
		IPipe input2 = process2.getOutput();
		while (true) {
			Object object = input2.take(Long.MAX_VALUE);
			if (object == null || object instanceof IStatus) {
				break;
			}
			count2++;
		}
		TestCase.assertEquals(count1, count2);
	}
	
	@Test(timeout=10000)
	public void processDiesIfSessionIsClosed() throws CoreException, InterruptedException {
		AtomicBoolean isAlive = new AtomicBoolean();
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch stop = new CountDownLatch(1);
		EclInjectedCommandService.inject((ignored, process) -> {
			do {
				isAlive.set(process.isAlive());
				start.countDown();
			} while (isAlive.get());
			stop.countDown();
			return Status.CANCEL_STATUS;
		});
		
		CompletableFuture.runAsync(() -> {
			try {
				executeCommand();
			} catch (CoreException | InterruptedException e) {
				throw new AssertionError(e);
			}
		});
		assertTrue(start.await(1, TimeUnit.SECONDS));
		assertTrue(isAlive.get());
		session.close();
		assertTrue(stop.await(1, TimeUnit.SECONDS));
		Assert.assertFalse(isAlive.get());
	}
}
