/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.core.util;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;

public final class Statuses {
	private Statuses() {}
	public interface Visitor {
		/** @true is argument's children should be inspected too */
		boolean visit(IStatus status);
	}

	public static void visit(IStatus status, Visitor visitor) {
		if (status == null)
			return;
		if (visitor.visit(status)) {
			if (status.isMultiStatus()) {
				for (IStatus child : status.getChildren()) {
					visit(child, visitor);
				}
			}
		}
	}

	public static boolean hasCode(IStatus status, final int code) {
		final boolean[] rv = new boolean[] { false };
		visit(status, new Visitor() {

			@Override
			public boolean visit(IStatus status) {
				if (status.getCode() == code)
					rv[0] = true;
				return !rv[0];
			}
		});
		return rv[0];
	}
	
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
		if (message != null && !message.isEmpty()) {
			output.append(": ");
			output.append(stream(message.split("\n")).collect(joining("\n")));
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
