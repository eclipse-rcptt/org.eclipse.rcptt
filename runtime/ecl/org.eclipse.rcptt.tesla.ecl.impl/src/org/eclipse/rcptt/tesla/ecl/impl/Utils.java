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
package org.eclipse.rcptt.tesla.ecl.impl;

import static org.eclipse.rcptt.tesla.ecl.internal.impl.TeslaImplPlugin.PLUGIN_ID;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rcptt.ecl.internal.core.CorePlugin;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.swt.dialogs.SWTDialogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Utils {

	public static IStatus closeDialogs() {
		return doCloseDialogs();
	}

	private static IStatus doCloseDialogs() {
		SWTDialogManager.setCancelMessageBoxesDisplay(true);
		try {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			final Display display = workbench.getDisplay();

			// Dummy call for E4, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=440850
			workbench.getActiveWorkbenchWindow();

			IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
			Set<Shell> windowShells = new HashSet<Shell>();
			for (IWorkbenchWindow window : windows) {
				Shell shell = window.getShell();
				if (shell != null) {
					windowShells.add(shell);
				}
			}
			// Close all shells for non workbench display
			Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
			for (Thread t : map.keySet()) {
				final Display dd = Display.findDisplay(t);
				if (dd != null && !dd.equals(display)) {
					t.interrupt();
				}
			}

			MultiStatus status = new MultiStatus(PLUGIN_ID, IProcess.INTERNAL_AUT_FAILURE,
					"Failed to close dialogs", null);
			Shell[] shells = display.getShells();
			for (int i = shells.length - 1; i >= 0; i--) {
				Shell shell = shells[i];
				if (!windowShells.contains(shell) && !shell.isDisposed() && shell.isVisible()) {
					Object data = shell.getData();
					if (data != null && data instanceof WizardDialog) {
						try {
							WizardDialog dialog = (WizardDialog) data;
							if (TeslaSWTAccess.getWizardDialogHasActiveOperations(dialog)) {
								// try to cancel active operations
								ProgressMonitorPart part = TeslaSWTAccess.getProgressMonitorPart(dialog);
								if (part != null) {
									part.setCanceled(true);
									part.clearBlocked();
								}
							}
							TeslaSWTAccess.setWizardDialogHasActiveOperations(dialog, 0);

							pressCancel(dialog);
							if (!shell.isDisposed()) {
								dialog.close();
							}
							// try to cancel monitor if there is one
						} catch (Throwable e) {
							TeslaCore.log(e);
							status.add(createStatusForInternalAutFailure(e));
						}
					} else {
						shell.close();
					}
					if (!shell.isDisposed() && shell.isVisible())
						status.add(new Status(IStatus.ERROR, PLUGIN_ID, 
								String.format("Shell with name %s and class %s is not disposed and visible" , shell.getText(), shell.getClass().getName())));
				}
			}
			if (status.isOK()) {
				return Status.OK_STATUS;
			}
			return status;
		} finally {
			SWTDialogManager.setCancelMessageBoxesDisplay(false);
		}
	}

	private static IStatus createStatusForInternalAutFailure(Throwable e) {
		return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, IProcess.INTERNAL_AUT_FAILURE,
				"Internal aut failure. Cause: " + e.getMessage(), e);
	}

	private static void pressCancel(WizardDialog dialog) {
		Button cancelButton = TeslaSWTAccess.getWizardDialogButton(
				dialog, IDialogConstants.CANCEL_ID);
		if (cancelButton == null) {
			return;
		}
		cancelButton.notifyListeners(SWT.Selection, new Event());
	}

}
