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
package org.eclipse.rcptt.tesla.ecl.internal.impl.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.InfoFactory;
import org.eclipse.rcptt.tesla.ecl.impl.TeslaBridge;
import org.eclipse.rcptt.tesla.internal.core.info.GeneralInformationCollector;
import org.eclipse.ui.PlatformUI;

public class AdvancedInfoService implements ICommandService {

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {

		final IProcess finalContext = context;

		if (PlatformUI.isWorkbenchRunning()) {
			final boolean complete[] = { false };
			final CoreException error[] = { null };
			Thread askForInfoThread = new Thread(new Runnable() {
				public void run() {
					PlatformUI.getWorkbench().getDisplay()
							.syncExec(new Runnable() {

								public void run() {
									boolean mustClientShutdown = false;
									if (TeslaBridge.getClient() == null) {
										mustClientShutdown = true;
										TeslaBridge.setup();
									}
									try {
										TeslaBridge.makeScreenshot(true,
												"Capturing debug information");
										final AdvancedInformation info = TeslaBridge
												.getClient()
												.getAdvancedInformation(null);
										returnGeneralInfo(info, finalContext);
										ReportHelper.addSnapshotWithData(ReportManager.getCurrentReportNode(), info);
									} catch (CoreException e) {
										error[0] = e;
									} finally {
										if (mustClientShutdown) {
											TeslaBridge.shutdown();
										}
										complete[0] = true;
									}
								}
							});

				}
			}, "get-advanced-info thread");
			askForInfoThread.start();
			askForInfoThread.join(10 * 1000); // Wait only 10 seconds.
			if (!complete[0]) {
				askForInfoThread.interrupt();
				returnGeneralInfo(null, finalContext);
			}
			if (error[0] != null)
				return error[0].getStatus();
		} else {
			returnGeneralInfo(null, finalContext);
		}

		return Status.OK_STATUS;
	}

	private void returnGeneralInfo(AdvancedInformation info,
			final IProcess finalContext) throws CoreException {
		// Return general information in any case.
		if (info == null) {
			info = InfoFactory.eINSTANCE.createAdvancedInformation();
		}
		GeneralInformationCollector.collectInformation(info);

		finalContext.getOutput().write(info);
	}
}
