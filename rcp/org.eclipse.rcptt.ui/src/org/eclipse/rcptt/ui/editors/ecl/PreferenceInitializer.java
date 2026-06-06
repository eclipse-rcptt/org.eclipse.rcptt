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
package org.eclipse.rcptt.ui.editors.ecl;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Q7UIPlugin.getDefault()
				.getPreferenceStore();
		preferences.setDefault(EclEditorToolkit.Style.commandName,
				StringConverter.asString(new RGB(127, 0, 85)));
		preferences.setDefault(EclEditorToolkit.Style.commandName
				+ EnhancedRuleScanner.boldSuffix, true);
		preferences.setDefault(EclEditorToolkit.Style.paramName,
				StringConverter.asString(new RGB(100, 100, 100)));
		preferences.setDefault(EclEditorToolkit.Style.string,
				StringConverter.asString(new RGB(42, 0, 255)));
		preferences.setDefault(EclEditorToolkit.Style.comment,
				StringConverter.asString(new RGB(63, 127, 95)));
		
		
		var defColor = Display.isSystemDarkTheme() ? new RGB(200, 200, 200) : new RGB(0, 0, 0);
		
		preferences.setDefault(EclEditorToolkit.Style.def,
				StringConverter.asString(defColor));
	}

}
