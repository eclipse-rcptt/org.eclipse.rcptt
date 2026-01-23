/*******************************************************************************
 * Copyright (c) 2009, 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.dev.ui.handlers;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.ui.actions.ScenariosActionHandler;

public class StatisticsHandler extends ScenariosActionHandler {

	public StatisticsHandler() {
		super("Collecting statistics...");
	}

	private int eclCount;
	private int teslaCount;
	private int failToRecognize;

	@Override
	protected void onStart() {
		eclCount = 0;
		teslaCount = 0;
		failToRecognize = 0;
	}

	@Override
	protected void onFinish() {
		final StringBuilder message = new StringBuilder();
		message.append("ECL tests: " + eclCount);
		message.append("\n");
		message.append("Tesla tests: " + teslaCount);
		if (failToRecognize > 0) {
			message.append("\n");
			message.append("Failed to recognize: " + failToRecognize);
		}
		message.append("\n");
		message.append("Total: " + (eclCount + teslaCount + failToRecognize));

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				MessageDialog.openInformation(window.getShell(), "Statistics",
						message.toString());
			}
		});
	}

	@Override
	protected boolean doExecute(ITestCase testcase) {
		try {
			Scenario scenario = (Scenario) testcase.getNamedElement();
			if (Scenarios.isEclMode(scenario)) {
				eclCount++;
			} else if (Scenarios.isTeslaMode(scenario)) {
				teslaCount++;
			} else {
				failToRecognize++;
			}
		} catch (ModelException e) {
			e.printStackTrace();
		}
		return false;
	}
}
