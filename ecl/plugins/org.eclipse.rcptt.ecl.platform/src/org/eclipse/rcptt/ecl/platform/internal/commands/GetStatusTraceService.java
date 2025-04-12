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
package org.eclipse.rcptt.ecl.platform.internal.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.ecl.platform.commands.GetStatusTrace;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;

public class GetStatusTraceService implements ICommandService {

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		GetStatusTrace getStatusTrace = (GetStatusTrace) command;
		ProcessStatus status = getStatusTrace.getStatus();
		String trace = getTraceFromStatus(status);
		context.getOutput().write(trace);
		return Status.OK_STATUS;
	}

	private static String getTraceFromStatus(ProcessStatus status) {
		StringBuilder sb = new StringBuilder();
		processTrace(sb, status);
		return sb.toString();
	}

	private static void processTrace(StringBuilder sb, ProcessStatus status) {
		if (status.getChildren().isEmpty()) {
			String message = GetStatusMessageService.getMessageFromStatus(status);
			sb.insert(0, String.format("%s\n", message));
		} else {
			sb.insert(0, String.format("\tat %s\n", status.getMessage()));
			for (int i = status.getChildren().size() - 1; i >= 0; i--) {
				processTrace(sb, status.getChildren().get(i));
			}
		}
	}

}
