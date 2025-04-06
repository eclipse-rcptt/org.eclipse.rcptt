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
import org.eclipse.rcptt.ecl.platform.commands.GetStatusMessage;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;

public class GetStatusMessageService implements ICommandService {

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		GetStatusMessage getStatusMessage = (GetStatusMessage) command;
		ProcessStatus status = getStatusMessage.getStatus();
		String message = getMessageFromStatus(status);
		context.getOutput().write(message);
		return Status.OK_STATUS;
	}

	public static String getMessageFromStatus(ProcessStatus status) {
		while (!status.getChildren().isEmpty()) {
			status = status.getChildren().get(0);
		}
		String message = "Unknown reason";
		if (status.getMessage() != null
				&& !status.getMessage().trim().equals("")) {
			message = status.getMessage();
		} else if (status.getException() != null) {
			Throwable throwable = status.getException().getThrowable();
			message = throwable.toString();
		}
		return message;
	}

}
