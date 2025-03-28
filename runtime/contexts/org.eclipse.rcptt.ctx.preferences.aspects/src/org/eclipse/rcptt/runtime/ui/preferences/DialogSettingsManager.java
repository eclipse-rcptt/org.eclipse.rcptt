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
package org.eclipse.rcptt.runtime.ui.preferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

public class DialogSettingsManager {
	private static DialogSettingsManager theInstance;
	private final Map<String, IDialogSettings> settings;
	private final Function<Bundle, IDialogSettings> GET_DIALOG_SETTINGS;

	private DialogSettingsManager() {
		settings = new HashMap<String, IDialogSettings>();
		Function<Bundle, IDialogSettings> temp = null;
		try {
			Method getDialogSettingsProvider = PlatformUI.class.getMethod("getDialogSettingsProvider", Bundle.class);
			Method getDialogSettings = Class.forName("org.eclipse.jface.dialogs.IDialogSettingsProvider").getMethod("getDialogSettings");
			temp = bundle -> {
				try {
					return (IDialogSettings) getDialogSettings.invoke(getDialogSettingsProvider.invoke(null, bundle));
				} catch (InvocationTargetException | IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			};
		} catch (NoSuchMethodException | ClassNotFoundException e) {
		}
		if (temp == null) {
			// Bug 549929: before 2020 no getDialogSettingsProvider() method was available, use AspectJ to intercept AbstractUIPlugin.getDialogSettings()
			// https://github.com/eclipse-platform/eclipse.platform.ui/commit/8266c76c0792ed71ee13bff1514e3c5cab3f4883
			temp = bundle -> settings.get(getKey(bundle));
		}
		GET_DIALOG_SETTINGS = temp;
	}

	public static DialogSettingsManager getInstance() {
		if (theInstance == null) {
			theInstance = new DialogSettingsManager();
		}
		return theInstance;
	}
	
	public IDialogSettings getSettings(Bundle bundle) {
		return GET_DIALOG_SETTINGS.apply(bundle);
	}

	public void addSettings(Bundle bundle, IDialogSettings dialogSettings) {
		settings.put(getKey(bundle), dialogSettings);
	}

	public void removeSettings(Bundle bundle) {
		settings.remove(getKey(bundle));
	}

	private String getKey(Bundle bundle) {
		// eclipse 3.4 compatibility:
		// getVersion().toString() replaced with
		// getHeaders().get("Bundle-Version")
		return bundle.getSymbolicName() + ":" + bundle.getHeaders().get("Bundle-Version");
	}
}
