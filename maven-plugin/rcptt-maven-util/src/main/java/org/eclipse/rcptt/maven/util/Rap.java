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
package org.eclipse.rcptt.maven.util;

public class Rap {
	private int port = -1;
	private String servletPath;
	private String browserCmd;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servetPath) {
		this.servletPath = servetPath;
	}

	public String getBrowserCmd() {
		return browserCmd;
	}

	public void setBrowserCmd(String browserCmd) {
		this.browserCmd = browserCmd;
	}
}
