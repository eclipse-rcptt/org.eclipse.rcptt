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
package org.eclipse.rcptt.internal.launching;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.internal.core.CorePlugin;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.internal.launching.ecl.ExecAdvancedInfoUtil;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.IExecutionSession;
import org.eclipse.rcptt.launching.ILaunchListener;
import org.eclipse.rcptt.launching.Q7Launcher;

public class LaunchTimer extends Thread implements ILaunchListener {

	private Timer timer;

	public void finished() {
		cancel();
	}

	public void launchStatusChanged(IExecutable... executables) {
		cancel();
		for (IExecutable executable : executables) {
			if (executable.getStatus() == IExecutable.State.RUNNING
					&& !executable.isDebug()) {
				timer = new Timer(true);
				// Some timeout handling is done in 
				int timeout = Q7Launcher.getLaunchTimeout() + 30;
				timer.schedule(new StopTask((Executable) executable, timeout),
						timeout * 1000);
			}
		}
	}

	private void cancel() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public void started(IExecutionSession session) {
	}

	private static class StopTask extends TimerTask {

		private Executable executable;
		private int timeout;

		public StopTask(Executable executable, int timeout) {
			this.executable = executable;
			this.timeout = timeout;
		}

		@Override
		public void run() {
			IStatus status = new Status(IStatus.ERROR,
				getClass(), 
				IProcess.TIMEOUT_CODE,
				executable.getName() + " has timed out after " + + timeout + " seconds",
				null);
			status = ExecAdvancedInfoUtil.askForAdvancedInfo(executable.getAut(), status);
			executable.cancel(status);
		}
	}

}
