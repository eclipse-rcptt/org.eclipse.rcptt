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
package org.eclipse.rcptt.internal.launching.aut;

public class FailToExecuteDefaultContext extends Exception {
	private static final long serialVersionUID = -6947749663598564602L;

	public FailToExecuteDefaultContext() {
	}

	public FailToExecuteDefaultContext(String message) {
		super(message);
	}

}
