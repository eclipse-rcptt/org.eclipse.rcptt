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
package org.eclipse.rcptt.internal.launching.ecl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ExecutionStatus;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.ecl.model.GetAdvancedInfo;
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;

public class ExecAdvancedInfoUtil {
	public static IStatus askForAdvancedInfo(AutLaunch launch, String err) {
		AdvancedInformation info = null;
		try {
			// try to obtain advanced error information from AUT
			GetAdvancedInfo advInfoCmd = TeslaFactory.eINSTANCE
					.createGetAdvancedInfo();
			Object obj = launch.execute(advInfoCmd);
			if (obj instanceof AdvancedInformation)
				info = (AdvancedInformation) obj;
		} catch (Exception e) {
			return RcpttPlugin.createStatus(e);
		}
		ExecutionStatus resultStatus = new ExecutionStatus(IStatus.CANCEL,
				Q7LaunchingPlugin.PLUGIN_ID, err);
		if (info != null) {
			resultStatus.setAdvancedInfo(info);
		}
		return resultStatus;
	}
}
