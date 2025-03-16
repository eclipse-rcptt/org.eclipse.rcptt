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

package org.eclipse.rcptt.ecl.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;

public class Process implements IProcess {

	private final IPipe in;
	private final IPipe out;
	private IStatus status;
	private final ISession session;

	public Process(ISession session, IPipe in, IPipe out) {
		this.session = session;
		this.in = in;
		this.out = out;
	}

	public IPipe getInput() {
		return in;
	}

	public IPipe getOutput() {
		return out;
	}

	public synchronized void setStatus(IStatus status) throws CoreException {
		this.status = status;
		out.close(status);
		notifyAll();
	}

	public synchronized IStatus waitFor() throws InterruptedException {
		while (status == null) {
			wait(100);
		}
		return status;
	}

	public synchronized IStatus waitFor(long timeout, IProgressMonitor monitor) throws InterruptedException {
		long start = System.currentTimeMillis();
		while (status == null) {
			long cur = System.currentTimeMillis();
			if (timeout != 0 && (cur - start) > timeout) {
				return status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, TIMEOUT_CODE,
						"Execution has timed out after " + (timeout / 1000.) + " seconds", null);
			}
			if (monitor.isCanceled()) {
				return status = new Status(IStatus.CANCEL, CorePlugin.PLUGIN_ID,
						"Execution is canceled.", null);
			}
			wait(100);
		}
		return status;
	}

	public synchronized boolean isAlive() {
		return status == null && !session.isClosed();
	}

	public ISession getSession() {
		return session;
	}

}
