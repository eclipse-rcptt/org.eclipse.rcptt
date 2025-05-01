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
package org.eclipse.rcptt.internal.launching.ext;

import static java.lang.System.currentTimeMillis;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.rcptt.core.launching.events.AutBundleState;
import org.eclipse.rcptt.core.launching.events.AutEventStart;
import org.eclipse.rcptt.core.launching.events.AutStartState;
import org.eclipse.rcptt.core.launching.events.EventsFactory;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.internal.launching.aut.BaseAut;
import org.eclipse.rcptt.internal.launching.aut.BaseAutLaunch;
import org.eclipse.rcptt.internal.launching.aut.BaseAutManager;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache.CachedInfo;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutLaunchListener;
import org.eclipse.rcptt.launching.AutLaunchState;
import org.eclipse.rcptt.launching.IQ7Launch;

public class Q7ExtLaunchMonitor {

	private IStatus status;
	private AutEventStart start;

	public Q7ExtLaunchMonitor(ILaunch launch) throws CoreException {
		aut = BaseAutManager.INSTANCE.getByLaunch(launch);
		if (aut == null) {
			throw new CoreException(
					Q7LaunchingPlugin
							.createStatus("Couldn't find AUT for ILaunch: "
									+ launch));
		}
	}

	public void wait(IProgressMonitor monitor, int seconds)
			throws CoreException {
		monitor.beginTask("Waiting for AUT", 2);
		aut.addListener(new AutLaunchListener() {
			public void stateChanged(AutLaunch launch, AutLaunchState state) {
				if (state.equals(AutLaunchState.TERMINATE)) {
					if (launch.equals(aut)) {
						aut.setLocationOnRestart(null);
					}
				}
			}

			@Override
			public void autStarted(AutLaunch baseAutLaunch, int eclPort, int teslaPort) {
				synchronized (Q7ExtLaunchMonitor.this) {
					AutEventStart st = EventsFactory.eINSTANCE.createAutEventStart();
					st.setEclPort(eclPort);
					st.setTeslaPort(teslaPort);
					start = st;
					Q7ExtLaunchMonitor.this.notifyAll();
				}
			}

			@Override
			public void autInit(AutLaunch baseAutLaunch, List<AutBundleState> bundleState) {
			}

			@Override
			public void autLocationChange(BaseAutLaunch baseAutLaunch, String location) {
				aut.setLocationOnRestart(location);
				// Clean out programm arguments, to update -data workspace
				// location
				CachedInfo info = LaunchInfoCache.getInfo(aut.getLaunch()
						.getLaunchConfiguration());
				if (info != null) {
					info.programArgs = null;
				}
			}
		});
		long start = currentTimeMillis();
		AutEventStart startup = doWait(SubMonitor.convert(monitor, 1), seconds);

		final String platform = startup.getPlatform().getLiteral().toLowerCase();
		final String capability = startup.getCapability().getLiteral().toLowerCase();
		seconds = ((int) (start - currentTimeMillis()) / 1000) + seconds;

		aut.activate(IQ7Launch.DEFAULT_HOST, startup.getEclPort(), startup.getTeslaPort(), platform, capability,
				seconds, SubMonitor.convert(monitor, 1));

		monitor.done();
		status = Status.OK_STATUS;
	}

	public void handle(Exception e) {
		if (e instanceof CoreException) {
			status = ((CoreException) e).getStatus();
		} else {
			status = Q7LaunchingPlugin.createStatus(e.getMessage(), e);
		}
	}

	public void dispose() {
		if (status == null) {
			status = Q7LaunchingPlugin
					.createStatus("Execution finished unexpectedly");
		}
		if (!status.isOK()) {
			aut.terminated(status);
		}
	}

	private AutEventStart doWait(IProgressMonitor monitor, int seconds)
			throws CoreException {
		monitor.beginTask("Waiting for startup status...", seconds);
		int terminateTimeout = 5; // Wait 5 second to be sure process is
									// terminated.
		while (--seconds >= 0) {
			if (monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			if (aut.getState().equals(AutLaunchState.TERMINATE)) {
				if (terminateTimeout > 0) {
					terminateTimeout--;
				}
				if (terminateTimeout <= 0) {
					throw new CoreException(
							new Status(
									IStatus.ERROR,
									getClass(),
									BaseAut.TERMINATE_CODE,
									"AUT was terminated while waiting for startup data",
									null));
				}
			}
			if (aut.getLaunch().isTerminated()) {
				throw new CoreException(new Status(
						IStatus.ERROR, getClass(),
						BaseAut.TERMINATE_CODE,
						"AUT's process was terminated while waiting for startup data",
						null));
			}
			synchronized (this) {
				if (start != null) {
					if (!start.getState().equals(AutStartState.OK)) {
						throw new CoreException(
								new Status(Status.ERROR, Q7ExtLaunchingPlugin.PLUGIN_ID, start.getMessage()));
					}
					monitor.done();
					return start;
				}
				try {
					this.wait(1000);
					if (monitor.isCanceled()) {
						throw new CoreException(Status.CANCEL_STATUS);
					}
				} catch (InterruptedException e) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
			}
			monitor.worked(1);
		}
		throw new CoreException(new Status(IStatus.ERROR,
				Q7ExtLaunchingPlugin.PLUGIN_ID,
				"Timeout error: AUT doesn't respond"));
	}

	private final BaseAutLaunch aut;

	public void handleNewLocation(String location) {
		aut.setLocationOnRestart(location);
	}

}
