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
package org.eclipse.rcptt.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.preferences.IPreferenceKeys;
import org.eclipse.swt.widgets.Shell;

public class RemoveAllProjectReferencesDialog {

	private static IPreferenceStore prefStore = Q7UIPlugin.getDefault()
			.getPreferenceStore();

	private static boolean isOpen = false;

	public static boolean open(Shell parentShell, IQ7Project project,
			List<IQ7NamedElement> elements) {
		if (isOpen) {
			return false;
		}
		isOpen = true;
		try {
			String value = prefStore
					.getString(IPreferenceKeys.ALL_REMOVE_PROJECT_REFERENCES);
			if (MessageDialogWithToggle.ALWAYS.equals(value)) {
				return true;
			}
			if (MessageDialogWithToggle.NEVER.equals(value)) {
				return false;
			} else {
				MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(parentShell,
						"Project Context and Verification References", generateMessage(elements),
						null, false, prefStore, IPreferenceKeys.ALL_REMOVE_PROJECT_REFERENCES);
				return dialog.getReturnCode() == IDialogConstants.YES_ID;
			}
		} finally {
			isOpen = false;
		}
	}

	private static String generateMessage(List<IQ7NamedElement> references) {
		StringBuilder label = new StringBuilder(
				"Would you like to remove default project context/verification references from "
						+ references.size() + " elements?\n\n");
		// try {
		// for (IQ7NamedElement e : references) {
		// label.append(" ").append(e.getNamedElement().getName()).append("
		// (").append(e.getPath()).append(")").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		// }
		// } catch (ModelException e) {
		// Q7UIPlugin.log(e);
		// }
		return label.toString();
	}

	public static boolean isNever() {
		String value = prefStore
				.getString(IPreferenceKeys.ALL_REMOVE_PROJECT_REFERENCES);
		return MessageDialogWithToggle.NEVER.equals(value);
	}
}