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
package org.eclipse.rcptt.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;

public final class StatusUtil {
	private StatusUtil() {}

	public static String format(IStatus status) {
		try (StringWriter string = new StringWriter()) {
			print("", status, string);
			string.flush();
			return string.toString();
		} catch (IOException e) {
			throw new AssertionError("Impossible error, no IO is done", e);
		}
	}
	
	public static void print(String indent, IStatus status, Appendable output) throws IOException {
		output.append(indent).append(severityString(status));
		String message = status.getMessage();
		String childIndent = indent + "  ";
		if (!StringUtils.isEmpty(message)) {
			output.append(": ");
			output.append(StringUtils.join("\n"+childIndent, Arrays.asList(message.split("\n"))));
			output.append("\n");
		}
		for (IStatus child: status.getChildren()) {
			print(childIndent, child, output);
		}
	}
	
	private static String severityString(IStatus status) {
		if (status.matches(IStatus.CANCEL)) {
			return "CANCEL";
		}
		if (status.matches(IStatus.ERROR)) {
			return "ERROR";
		}
		if (status.matches(IStatus.WARNING)) {
			return "WARNING";
		}
		if (status.matches(IStatus.INFO)) {
			return "INFO";
		}
		return "OK";
	}
}
