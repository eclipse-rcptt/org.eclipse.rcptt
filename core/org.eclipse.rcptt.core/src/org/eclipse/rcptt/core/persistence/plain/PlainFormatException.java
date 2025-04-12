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
package org.eclipse.rcptt.core.persistence.plain;

public class PlainFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	private String fileName = "";

	public PlainFormatException(String msg) {
		super(msg);
	}

	public void setFileName(String fName) {
		this.fileName = fName;
	}

	@Override
	public String getMessage() {
		if (this.fileName != null) {
			return super.getMessage() + " on " + this.fileName;
		}
		return super.getMessage();
	}
}
