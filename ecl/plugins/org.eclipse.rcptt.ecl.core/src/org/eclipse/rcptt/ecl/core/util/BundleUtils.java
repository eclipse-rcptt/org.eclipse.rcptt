/*******************************************************************************
 * Copyright (c) 2009, 2016 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class BundleUtils {
	private final Bundle bundle;
	private final ILog log;

	public BundleUtils(Bundle bundle) {
		super();
		this.bundle = bundle;
		this.log = Platform.getLog(bundle);
	}
	
	public String getID() {
		return bundle.getSymbolicName();
	}

	public IStatus createError(Throwable t) {
		if (t instanceof CoreException)
			return ((CoreException) t).getStatus();
		return new Status(IStatus.ERROR, getID(), t.getMessage(), t);
	}
	
	public void log(String msg, Throwable t) {
		log(createError(msg, t));
	}

	public void log(Throwable t) {
		log(createError(t));
	}

	public void log(IStatus status) {
		log.log(status);
	}

	public IStatus createStatus(int severity, String message) {
		return new Status(severity, getID(), message);
	}

	public IStatus createInfo(String message) {
		return createStatus(IStatus.INFO, message);
	}

	public IStatus createError(String message, Throwable t) {
		if (t != null) {
			if (message == null || message.equals(t.getMessage())) {
				return createError(t);
			}
		}
		if (t instanceof CoreException) {
			return new MultiStatus(((CoreException) t).getStatus().getPlugin(), 0, new IStatus[] { createError(t) },
					message, null);
		}

		return new Status(IStatus.ERROR, getID(), message, t);
	}

	/**
	 * Retrieves a bundle where a given class is defined.
	 * Use this to avoid addressing a bundle by its symbolic ID.
	 * 
	 * @param clazz
	 *            - a class that is defined in a bundle to get
	 */
	public static Bundle getBundleOfAClass(Class<?> clazz) {
		return FrameworkUtil.getBundle(clazz);
	}

	/**
	 * Wraps a bundle where a given class is defined.
	 * Use this to avoid addressing a bundle by its symbolic ID.
	 * 
	 * @param clazz
	 *            - a class that is defined in a bundle to get
	 */
	public static BundleUtils create(Class<?> clazz) {
		return new BundleUtils(getBundleOfAClass(clazz));
	}
}
