/*******************************************************************************
 * Copyright (c) 2024-2024 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.swt.test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.junit.rules.ExternalResource;
import org.osgi.framework.FrameworkUtil;

public class NoErrorsInLog extends ExternalResource {
	
	private final ILog log;
	private final MultiStatus statusLog;
	private final ILogListener listener = new ILogListener() {
		
		@Override
		public void logging(IStatus status, String plugin) {
			synchronized (statusLog) {
				statusLog.add(status);
			}
		}
	};
	
	public NoErrorsInLog(Class<?> clazzToWatchLogFor) {
		this.log = Platform.getLog(FrameworkUtil.getBundle(clazzToWatchLogFor));
		this.statusLog = new MultiStatus("org.eclipse.rcptt.tesla.swt.test", 0, "Errors in the error log", null);
	}
	@Override
	protected void before() throws Throwable {
		super.before();
		log.addLogListener(listener);
	}
	
	@Override
	protected void after() {
		log.removeLogListener(listener);
		synchronized (statusLog) {
			if (statusLog.matches(IStatus.ERROR)) {
				throw new AssertionError(new CoreException(statusLog));
			}
		}
		super.after();
	}

}
