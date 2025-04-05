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
package org.eclipse.rcptt.tesla.recording.aspects.swt.rap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rcptt.tesla.swt.events.RcpttMouseEvents;
import org.eclipse.rcptt.tesla.ui.RWTUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class AssertionServiceHandler implements ServiceHandler {

	private static final String WIDGET_ID = "widgetId";
	public final static String HANDLER_ID = "org.eclipse.rcptt.rap.assertion"; //$NON-NLS-1$

	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		final String id = request.getParameter(WIDGET_ID);

		RWTUtils.findDisplay().asyncExec(new Runnable() {
			public void run() {
				final Widget widget = findWidget(id);
				RcpttMouseEvents.updateWidgetUnderMouse();
			}
		});
		setResponseHeaders(response);
		isConnectionAlive(response);
	}

	@SuppressWarnings("restriction")
	private static Widget findWidget(String id) {
		Display display = RWTUtils.findDisplay();
		if (display != null) {
			for (Shell shell : display.getShells()) {
				final Widget widget = WidgetUtil.find(shell, id);
				if (widget != null) {
					return widget;
				}
			}
		}

		return null;
	}

	private static void setResponseHeaders(HttpServletResponse response) {
		// Ensures that the response is not cached
		// 410157: [ServerPush] ServerPush requests always return immediately in
		// IE
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=410157
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}

	private static boolean isConnectionAlive(HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			writer.write(" ");
			return !writer.checkError();
		} catch (@SuppressWarnings("unused") IOException ioe) {
			return false;
		}
	}
}
