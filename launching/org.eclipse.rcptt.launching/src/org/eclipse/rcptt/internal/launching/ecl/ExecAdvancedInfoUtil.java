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
import org.eclipse.core.runtime.MultiStatus;
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

	private static IStatus setAdvancedInfo(AutLaunch launch, ExecutionStatus resultStatus) {
		AdvancedInformation info = null;
		try {
			// try to obtain advanced error information from AUT
			info = getAdvancedInfo(launch);
		} catch (Exception e) {
			MultiStatus status = new MultiStatus(Q7LaunchingPlugin.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
			status.add(resultStatus);
			return status;
		}
		if (info != null) {
			resultStatus.setAdvancedInfo(info);
		}
		return resultStatus;
	}

	public static IStatus askForAdvancedInfo(AutLaunch launch, String err) {
		final ExecutionStatus resultStatus = new ExecutionStatus(IStatus.CANCEL, Q7LaunchingPlugin.PLUGIN_ID, err);
		return setAdvancedInfo(launch, resultStatus);
	}

	public static IStatus askForAdvancedInfo(AutLaunch launch, IStatus status) {
		final ExecutionStatus resultStatus;
		if (status instanceof ExecutionStatus) {
			resultStatus = (ExecutionStatus) status;
			if (resultStatus.getInfo() != null) {
				return status;
			}
		} else {
			resultStatus = new ExecutionStatus(status);
		}
		return setAdvancedInfo(launch, resultStatus);
	}

}
