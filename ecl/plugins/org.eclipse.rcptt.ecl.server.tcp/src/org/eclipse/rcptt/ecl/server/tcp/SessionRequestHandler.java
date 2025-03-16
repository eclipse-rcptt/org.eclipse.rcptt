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
package org.eclipse.rcptt.ecl.server.tcp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.internal.core.CorePlugin;
import org.eclipse.rcptt.ecl.runtime.CoreUtils;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.osgi.framework.FrameworkUtil;

final class SessionRequestHandler implements Runnable {
	private final Socket socket;
	private final ISession session;
	private final BufferedInputStream inputStream;
	private final int defaultTimeout;
	private final static ILog LOG = Platform.getLog(FrameworkUtil.getBundle(SessionRequestHandler.class));

	SessionRequestHandler(Socket socket, boolean useJobs) throws IOException {
		// super("ECL tcp session:" + socket.getPort());
		this.socket = socket;
		try {
			this.socket.setTcpNoDelay(true);
		} catch (SocketException e) {
			CorePlugin.log(e);
		}
		this.session = EclRuntime.createSession(useJobs);
		this.inputStream = new BufferedInputStream(socket.getInputStream());
		this.defaultTimeout = socket.getSoTimeout();
	}

	public void run() {
		try {
			IPipe pipe = CoreUtils.createEMFPipe(inputStream,
					socket.getOutputStream());
			while (!Thread.currentThread().isInterrupted()
					&& !socket.isClosed()) {
				try {
					pipe.reinit();
					Object object = pipe.take(Long.MAX_VALUE);
					if (!(object instanceof Command))
						break;
					Command command = (Command) object;
					IStatus status;
					try {
						IPipe input = readInput(pipe);
						IProcess process = session.execute(command, input, null);
						status = writeOutput(pipe, process);
					} catch (CoreException e) {
						status = e.getStatus();
					}
					try {
						pipe.write(status);
					} catch (ClassCastException e) { // Serialization errors
						CorePlugin.log(e);
						status = CorePlugin.err(e);
						pipe.write(status);
					}
					pipe.close(status);
				} catch (Exception e) {
					Throwable te = e;
					if (e instanceof CoreException) {
						if (e.getCause() instanceof SocketException) {
							te = e.getCause();
						}
					}
					if (te instanceof SocketException) {
						try {
							socket.close();
						} catch (IOException e2) {
						}
						break;
					}
					CorePlugin.log(e);
				}
			}
		} catch (Exception e) {
			CorePlugin.log(e);
		} finally {
			try {
				socket.close();
			} catch (Exception e) {
				CorePlugin.log(e);
			}
			try {
				session.close();
			} catch (Exception e) {
				CorePlugin.log(e);
			}
		}
	}

	private IPipe readInput(IPipe pipe) throws CoreException {
		Object object;
		IPipe input = session.createPipe();
		while (true) {
			object = pipe.take(Long.MAX_VALUE);
			if (object instanceof IStatus) {
				input.close((IStatus) object);
				break;
			}
			input.write(object);
		}
		return input;
	}

	private IStatus writeOutput(IPipe pipe, IProcess process)
			throws CoreException, IOException {
		Object object;
		do {
			object = process.getOutput().take(100);
			if (object == null) {
				if (!isConnected()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
			} else if (object instanceof IStatus) {
				try {
					return process.waitFor();
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
			} else {
				pipe.write(object);
			}
			
		} while (true);
	}
	
	private boolean isConnected() throws IOException {
		inputStream.mark(10);
		socket.setSoTimeout(1);
		try {
			return inputStream.read() >= 0 && inputStream.read() >= 0;
		} catch (SocketTimeoutException e) {
			return true;
		} finally {
			socket.setSoTimeout(defaultTimeout);
			inputStream.reset();
		}
	}

	public void recover(Socket client) {
	}
}