/*******************************************************************************
 * Copyright (c) 2025, 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.platform.internal.ui.commands;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

final class Utils {
	private static final String PLUGIN_ID = "org.eclipse.rcptt.ecl.platform.ui";

	private Utils() {
	}

	public static IStatus error(String message) {
		return error(message, null);
	}
	
	public static IStatus error(String message, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, e);
	}

	public static IStatus error(Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, e.getLocalizedMessage(), e);
	}
}
