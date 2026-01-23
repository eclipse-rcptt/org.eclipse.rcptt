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
package org.eclipse.rcptt.dev.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.preferences.IPreferenceKeys;

public enum RecoverEclWarningDialog {
	INSTANCE;

	private static IPreferenceStore preferenceStore = Q7UIPlugin.getDefault()
			.getPreferenceStore();

	public boolean open() {
		String show = preferenceStore
				.getString(IPreferenceKeys.RECOVER_ECL_PROMT);
		if (MessageDialogWithToggle.ALWAYS.equals(show)) {
			return true;
		}
		final boolean[] answer = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				MessageDialogWithToggle dialog = MessageDialogWithToggle
						.openOkCancelConfirm(window.getShell(), "Warning",
								"All user changes will be discarded.\n"
										+ "Are you sure you want to proceed?",
								"Do not show this dialog again", false,
								preferenceStore,
								IPreferenceKeys.RECOVER_ECL_PROMT);
				int returnCode = dialog.getReturnCode();
				answer[0] = returnCode == IDialogConstants.OK_ID;
			}
		});
		return answer[0];
	}
}