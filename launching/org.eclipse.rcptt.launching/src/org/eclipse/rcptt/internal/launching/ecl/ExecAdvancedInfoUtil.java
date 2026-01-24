/*******************************************************************************
 * Copyright (c) 2009, 2020 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.launching.ecl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.internal.launching.ExecutionStatus;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.ecl.model.GetAdvancedInfo;
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;

public class ExecAdvancedInfoUtil {

	private static AdvancedInformation getAdvancedInfo(AutLaunch launch) throws Exception {
		AdvancedInformation info = null;
		final GetAdvancedInfo advInfoCmd = TeslaFactory.eINSTANCE.createGetAdvancedInfo();
		final Object obj = launch.execute(advInfoCmd, 11000);
		if (obj instanceof AdvancedInformation) {
			info = (AdvancedInformation) obj;
		}
		return info;
	}

	private static void setAdvancedInfo(AutLaunch launch, ExecutionStatus resultStatus) {
		AdvancedInformation info = null;
		if (resultStatus.getInfo() != null) {
			return;
		}

		try {
			// try to obtain advanced error information from AUT
			info = getAdvancedInfo(launch);
		} catch (Exception e) {
			resultStatus.add(Status.error("Failed to get snapshot", e));
		}
		if (info != null) {
			resultStatus.setAdvancedInfo(info);
		} else {
			resultStatus.add(Status.error("Snapshot is null"));
		}
	}

	public static IStatus askForAdvancedInfo(AutLaunch launch, String err) {
		final ExecutionStatus resultStatus = new ExecutionStatus(IStatus.CANCEL, Q7LaunchingPlugin.PLUGIN_ID, err);
		setAdvancedInfo(launch, resultStatus);
		return resultStatus;
	}

	public static ExecutionStatus askForAdvancedInfo(AutLaunch launch, IStatus status) {
		final ExecutionStatus resultStatus;
		if (status instanceof ExecutionStatus s) {
			resultStatus = s;
		} else {
			resultStatus = new ExecutionStatus(status);
		}
		setAdvancedInfo(launch, resultStatus);
		return resultStatus;
	}

}
