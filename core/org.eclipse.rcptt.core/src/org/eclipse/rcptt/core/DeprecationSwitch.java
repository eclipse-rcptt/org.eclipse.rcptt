/*******************************************************************************
 * Copyright (c) 2024, 2024 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.core;

import static org.eclipse.rcptt.internal.core.RcpttPlugin.PLUGIN_ID;

import java.util.Objects;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

public final class DeprecationSwitch {
	private static final ILog LOG = Platform.getLog(FrameworkUtil.getBundle(DeprecationSwitch.class));
	private static final String PREFERENCE_QUALIFIER = PLUGIN_ID + "/deprecation_reported";
	private static final IPreferencesService PREFERENCES = Objects.requireNonNull(Platform.getPreferencesService());
	private final boolean result;
	private IStatus status;
	private final String property;

	public DeprecationSwitch(String property, String message, boolean defaultValue, boolean targetValue) {
		this.property = property;
		if (property.isEmpty()) {
			throw new IllegalArgumentException("Property can't be empty");
		}
		if (property.contains(" ")) {
			throw new IllegalArgumentException("Multi word properties are not supported");
		}
		String systemProperty = "org.eclipse.rcptt.deprecate."+property;
		String propertyValue = System.getProperty(systemProperty);
		if (propertyValue != null) {
			result = Boolean.valueOf(propertyValue); 
			this.status = Status.OK_STATUS;
		} else if (defaultValue == targetValue) {
			result = defaultValue;
			this.status = Status.OK_STATUS;
			this.status = new Status(IStatus.WARNING, PLUGIN_ID, 0, message + " Suppress this warning with a VM argument -D"+systemProperty+"="+defaultValue+", switch to the old behavior with -D"+systemProperty+"="+(!targetValue) + ". This warning is shown once per workspace." , null);
		} else {
			result = defaultValue;
			this.status = new Status(IStatus.WARNING, PLUGIN_ID, 0, message + " Switch to the new behavior with a VM argument -D"+systemProperty+"="+targetValue + " Suppress this warning with -D"+systemProperty+"="+defaultValue + " This warning is shown once per workspace.", null);
		}
	}

	public boolean getResult() {
		IStatus localStatus = status;
		if (localStatus.isOK()) {
			return result;
		}
		if (PREFERENCES.getBoolean(PREFERENCE_QUALIFIER, property, false, null)) {
			return result;
		}
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(PREFERENCE_QUALIFIER);
		node.putBoolean(property, true);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			LOG.log(new Status(IStatus.ERROR, PLUGIN_ID, 0, e.getMessage(), e));
		}
		LOG.log(localStatus);
		return result;
	}
}
