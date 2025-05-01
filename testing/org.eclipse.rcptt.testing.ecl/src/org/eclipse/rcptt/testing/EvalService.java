/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.testing;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.internal.launching.ExecutionStatus;
import org.eclipse.rcptt.internal.launching.ecl.ExecAdvancedInfoUtil;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.ecl.core.model.Q7CoreFactory;
import org.eclipse.rcptt.core.ecl.core.model.SetQ7Features;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.Q7Launcher;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.testing.commands.Eval;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.ecl.model.GetAdvancedInfo;
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;

public class EvalService implements ICommandService {

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		Eval eval = (Eval) command;
		String autId = eval.getAutId();
		AutLaunch launch = AutManager.INSTANCE.getById(autId);
		if (launch == null) {
			return TestingPlugin.createErrStatus("AUT is not launched: "
					+ autId);
		}

		try {
			SetQ7Features setQ7Features = Q7CoreFactory.eINSTANCE
					.createSetQ7Features();
			TeslaFeatures.getInstance()
					.storeValues(setQ7Features.getFeatures());
			Q7Features.getInstance().storeValues(setQ7Features.getFeatures());
			launch.execute(setQ7Features);
		} catch (CoreException e) {
			return TestingPlugin.createErrStatus("Failed to send features: "
					+ e.getMessage(), e);
		}

		Script script = CoreFactory.eINSTANCE.createScript();
		script.setContent(eval.getScript());

		try {
			launch.execute(script, Q7Launcher.getLaunchTimeout() * 1000,
				new NullProgressMonitor());
		} catch (CoreException e) {
			IStatus status = ExecAdvancedInfoUtil.askForAdvancedInfo(launch, e.getStatus());
			if (status instanceof ExecutionStatus) {
				ReportHelper.addSnapshotWithData(ReportManager.getCurrentReportNode(), ((ExecutionStatus) status).getInfo());
			}
			return status;
		}
		GetAdvancedInfo info = TeslaFactory.eINSTANCE.createGetAdvancedInfo();
		Object result = launch.execute(info);
		if (result != null) {
			context.getOutput().write(result);
		}
		return Status.OK_STATUS;
	}
}
