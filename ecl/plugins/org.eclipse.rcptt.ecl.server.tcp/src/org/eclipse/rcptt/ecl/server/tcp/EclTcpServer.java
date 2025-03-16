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

import static org.eclipse.rcptt.ecl.internal.core.CorePlugin.log;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.rcptt.ecl.internal.core.CorePlugin;

public class EclTcpServer extends Thread {
	private final ServerSocket socket;
	private final int port;
	private volatile boolean starting = true;
	private SessionManager manager = null;

	public boolean isStarting() {
		return starting;
	}

	public EclTcpServer(int port, boolean useJobs) throws IOException {
		super("ECL TCP server: " + port + " main");
		this.socket = new ServerSocket(port);
		this.port = port;
		manager = new SessionManager(useJobs);
	}

	@Override
	public void run() {
		starting = false;
		try (Closeable closeable = socket) {
			while (!isInterrupted()) {
				try {
					Socket client = socket.accept();
					//client.setKeepAlive(true);
					client.setTcpNoDelay(true);
					manager.acceptNewConnection(client);
				} catch (Exception e) {
					if (!socket.isClosed()) {
						log(CorePlugin.err(
								"Failed to accept connection", e));
					}
				}
			}
			socket.close();
		} catch (Exception e) {
			log(CorePlugin.err("Failed to start ECL TCP server", e));
		} finally {
			log(String.format("ECL TCP server on port %d has stopped", port));
		}
	}

	public int getPort() {
		return port;
	}
	
	@Override
	public void interrupt() {
		try {
			socket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			super.interrupt();
		}
	}
}
