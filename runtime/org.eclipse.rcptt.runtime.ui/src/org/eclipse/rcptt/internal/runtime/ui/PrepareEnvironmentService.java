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
package org.eclipse.rcptt.internal.runtime.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;

import org.eclipse.rcptt.tesla.core.am.AspectManager;

public class PrepareEnvironmentService implements ICommandService {
	private static final Method method;
	static {
		Method temp;
		try {
			temp = org.eclipse.rcptt.util.ReflectionUtil.findMethod(PrepareEnvironmentService.class.getClassLoader().loadClass("org.eclipse.core.internal.jobs.JobListeners"), "resetJobListenerTimeout", Collections.emptyList());
		}  catch (ClassNotFoundException e) {
			// The class was package-private until Eclipse Platform 2022-12 too.
			temp = null;
		}
		method = temp;
	}

	@Override
	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		// HandleStore.getStore().printInfo();
		try {
			// This method was introduced in Eclipse Platform 2022-12.
			if (method != null) {
				method.invoke(null);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}

		// Be sure tesla is active
		return AspectManager.initialize();
	}
}
