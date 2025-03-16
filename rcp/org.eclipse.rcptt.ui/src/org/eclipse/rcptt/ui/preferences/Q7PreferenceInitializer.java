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
package org.eclipse.rcptt.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.rcptt.internal.ui.Q7UIPlugin;

public class Q7PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Q7UIPlugin.getDefault()
				.getPreferenceStore();
		preferenceStore.setDefault(
				IPreferenceKeys.SHOW_OBSOLETE_ECL_FORMAT_ERROR,
				MessageDialogWithToggle.ALWAYS);
		preferenceStore.setDefault(IPreferenceKeys.RECOVER_ECL_PROMT,
				MessageDialogWithToggle.PROMPT);
		preferenceStore.setDefault(IPreferenceKeys.DISCARD_USER_CHANGES_PROMT,
				MessageDialogWithToggle.PROMPT);
		preferenceStore.setDefault(
				IPreferenceKeys.ADD_PROJECT_REFERENCES,
				MessageDialogWithToggle.PROMPT);
		preferenceStore.setDefault(IPreferenceKeys.ALL_REMOVE_PROJECT_REFERENCES, MessageDialogWithToggle.PROMPT);
	}

}
