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
package org.eclipse.rcptt.runtime.ui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.ecl.client.tcp.EclTcpSession;
import org.eclipse.rcptt.ecl.debug.runtime.SuspendListener;
import org.eclipse.rcptt.ecl.debug.runtime.SuspendManager;
import org.eclipse.rcptt.ecl.server.tcp.EclTcpServerManager;

import org.eclipse.rcptt.internal.runtime.ui.Activator;
import org.eclipse.rcptt.util.NetworkUtils;
import org.eclipse.rcptt.tesla.core.server.TeslaServerManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;

public enum Q7ServerStarter {

	INSTANCE;

	private static final int PORT_RANGE_START = 7001;

	private static final int PORT_RANGE_END = 10000;

	private static final String VAR_TESLA = "teslaPort";

	private static final String VAR_ECL = "eclPort";

	private int ecl = -1;
	private int tesla = -1;

	public boolean isActive() {
		return ecl > 0 && tesla > 0;
	}

	public int getEclPort() {
		return ecl;
	}

	public int getTeslaPort() {
		return tesla;
	}

	public void start() {
		NetworkUtils.initTimeouts();
		if (ecl < 0) {
			ecl = createEclServer(PORT_RANGE_START, PORT_RANGE_END);
			Activator.info("ECL server started on port %s", ecl);
			testLocalEclServer();
		}
		if (tesla < 0) {
			tesla = createTeslaServer(PORT_RANGE_START, PORT_RANGE_END);
			Activator.info("Tesla server started on port %s", tesla);
		}
		unfreezeOnSuspend();
	}

	private void testLocalEclServer() {
		try {
			new EclTcpSession(InetAddress.getByName("localhost"), ecl).close();
			Activator.info("Verified that local ECL server is working");
		} catch ( CoreException e) {
			Activator.err(e, "Error testing a local ECL server. Something is blocking connection");
		} catch (UnknownHostException e) {
			Activator.err(e, "Error testing a local ECL server. Something is blocking connection");
		} catch (IOException e) {
			Activator.err(e, "Error testing a local ECL server. Something is blocking connection");
		}
	}

	private static int createEclServer(int from, int to) {
		int port = getProperty(VAR_ECL);
		if (port > 0) {
			try {
				Activator.info("Starting ECL server on port %s", port);
				EclTcpServerManager.Instance.startServer(port);
				return port;
			} catch (Exception e) {
				Activator.log("Default ECL port '" + port + "' already in use",
						e);
			}
		}
		for (port = from; port < to; port++) {
			try {
				Activator.info("Trying to start ECL server on port %s", port);
				EclTcpServerManager.Instance.startServer(port);
				return port;
			} catch (Exception e) {
				// OK, let's try another port
			}
		}
		// o_O
		throw new IllegalStateException(
				"No one available port found for ECL TCP server within range ["
						+ from + ", " + to + "]");
	}

	private static int createTeslaServer(int from, int to) {
		int port = getProperty(VAR_TESLA);
		if (port > 0) {
			try {
				Activator.info("Starting Tesla server on port %s", port);
				TeslaServerManager.startServer(port);
				return port;
			} catch (Exception e) {
				Activator.log("Default Tesla port '" + port
						+ "' already in use", e);
			}
		}
		for (port = from; port < to; port++) {
			try {
				Activator.info("Trying to start Tesla server on port %s", port);
				TeslaServerManager.startServer(port);
				return port;
			} catch (Exception e) {
				// OK, let's try another port
			}
		}
		// o_O
		throw new IllegalStateException(
				"No one available port found for Tesla Network server within range ["
						+ from + ", " + to + "]");
	}

	private static void unfreezeOnSuspend() {
		SuspendManager.INSTANCE.addListener(new SuspendListener() {

			public void suspend() {
				TeslaEventManager.getManager().setUnfreeze(true);
			}

			public void resume() {
				TeslaEventManager.getManager().setUnfreeze(false);
			}
		});
	}

	private static int getProperty(String var) {
		String port = System.getProperty(var);
		if (port != null && !port.isEmpty()) {
			try {
				return Integer.valueOf(port);
			} catch (NumberFormatException e) {
			}
		}
		return -1;
	}

}
