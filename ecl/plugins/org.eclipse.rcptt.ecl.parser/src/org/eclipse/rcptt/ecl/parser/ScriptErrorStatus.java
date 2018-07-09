/*******************************************************************************
 * Copyright (c) 2009, 2015 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.parser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

public class ScriptErrorStatus extends MultiStatus {
	private final int line;
	private final int column;
	private final int length;
	private final String resource;

	public ScriptErrorStatus(String pluginId, String message,
			String resource, int line, int column, int length) {
		super(pluginId, 0, message, null);
		this.line = line;
		this.column = column;
		this.length = length;
		this.resource = resource;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public int getLength() {
		return length;
	}

	public String getResource() {
		return resource;
	}

	public IStatus getCause() {
		IStatus[] children = getChildren();
		if (children.length > 0) {
			return children[0];
		}
		return null;
	}


}
