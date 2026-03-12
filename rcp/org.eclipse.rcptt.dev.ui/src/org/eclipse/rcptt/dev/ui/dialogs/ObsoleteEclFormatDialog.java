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

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.preferences.IPreferenceKeys;

public enum ObsoleteEclFormatDialog {
	INSTANCE;

	private static IPreferenceStore preferenceStore = Q7UIPlugin.getDefault()
			.getPreferenceStore();

	public void open(final Scenario scenario) {
		String show = preferenceStore
				.getString(IPreferenceKeys.SHOW_OBSOLETE_ECL_FORMAT_ERROR);
		if (MessageDialogWithToggle.ALWAYS.equals(show)) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					MessageDialogWithToggle dialog = MessageDialogWithToggle
							.openInformation(window.getShell(), "Error",
									"Oops! Test case \"" + scenario.getName()
											+ "\" has obsolete format and "
											+ "cannot be switched to Tesla.\n",
									"Do not show this dialog again", false,
									null, null);
					if (dialog.getToggleState()) {
						preferenceStore.setValue(
								IPreferenceKeys.SHOW_OBSOLETE_ECL_FORMAT_ERROR,
								MessageDialogWithToggle.NEVER);
					}
				}
			});
		}
	}
}