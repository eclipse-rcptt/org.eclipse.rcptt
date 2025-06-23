/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.launching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.ecl.parser.ScriptErrorStatus;
import org.eclipse.rcptt.util.StringUtils;

public class EclStackTrace {
	private EclStackTrace(IStatus rootCause, ScriptErrorStatus[] trace) {
		this.rootCause = rootCause;
		this.frames = trace;
	}

	public final IStatus rootCause;
	public final ScriptErrorStatus[] frames;

	public static EclStackTrace fromExecStatus(ExecutionStatus status) {
		while (status.getCause() instanceof ExecutionStatus) {
			status = (ExecutionStatus) status.getCause();
		}
		IStatus cause = status.getCause();
		if (cause == null) {
			cause = status;
		}
		return fromScriptStatus(cause);
	}

	public static EclStackTrace fromScriptStatus(IStatus status) {
		List<ScriptErrorStatus> trace = new ArrayList<ScriptErrorStatus>();
		while (status instanceof ScriptErrorStatus) {
			trace.add((ScriptErrorStatus) status);
			status = ((ScriptErrorStatus) status).getCause();
		}
		Collections.reverse(trace);
		if (status == null) {
			status = trace.remove(0);
		}

		return new EclStackTrace(status, trace.toArray(new ScriptErrorStatus[trace.size()]));
	}

	public String print() {
		StringBuilder sb = new StringBuilder();
		sb.append(getDisplayMessage()).append("\n");
		for (ScriptErrorStatus frame : frames) {
			sb.append(String.format("\tat %s (%s)", frame.getMessage(), getLocation(frame))).append("\n");
		}
		return sb.toString();
	}

	public String getDisplayMessage() {
		if (rootCause == null) {
			return "Execution failed";
		}

		if (!StringUtils.isBlank(rootCause.getMessage())) {
			return rootCause.getMessage();
		}

		if (rootCause.getException() != null) {
			return rootCause.getException().getMessage();
		}

		return "Unknown reason";

	}

	public static String getLocation(ScriptErrorStatus status) {
		String id = status.getResource();
		String path = RcpttCore.getResourcePath(id);
		return path == null ? String.format("line %d", status.getLine()) : String.format("%s:%d", path,
				status.getLine());
	}
}
