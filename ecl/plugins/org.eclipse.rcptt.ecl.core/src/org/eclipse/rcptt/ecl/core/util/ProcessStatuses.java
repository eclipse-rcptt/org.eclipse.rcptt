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
package org.eclipse.rcptt.ecl.core.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.ecl.internal.core.ProcessStatusConverter;

public final class ProcessStatuses {
	private ProcessStatuses() {}
	
	public static ProcessStatus adapt(IStatus status) {
		return ProcessStatusConverter.toProcessStatus(status);
	}
	
	public static IStatus adapt(ProcessStatus status) {
		return ProcessStatusConverter.toIStatus(status);
	}

}
