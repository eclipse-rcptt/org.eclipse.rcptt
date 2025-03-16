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
package org.eclipse.rcptt.internal.launching.aut;

import static org.eclipse.rcptt.tesla.core.TeslaFeatures.REPORT_LOGGING_SIZE_OF_INITIAL_PART;
import static org.eclipse.rcptt.tesla.core.TeslaFeatures.REPORT_LOGGING_SIZE_OF_ROTATION_PART;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.rcptt.core.launching.events.AutBundleState;
import org.eclipse.rcptt.internal.launching.utils.LogBuilder;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutLaunchListener;
import org.eclipse.rcptt.launching.AutLaunchState;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class ConsoleOutputListener implements AutLaunchListener {

	private static final int BITE_IN_MEGABITE = 1024 * 1024;

	private final LogBuilder log;
	private AutLaunch launch = null;
	private boolean logStarted = false;

	public ConsoleOutputListener()
	{
		log = new LogBuilder(getLogSizeOfInitPart(), getLogSizeOfRotationPart());
	}

	private IStreamListener listener = new IStreamListener() {
		public void streamAppended(String text, IStreamMonitor monitor) {
			assert launch != null;
			log.append(text);
		}
	};

	static Collection<IStreamMonitor> getMonitors(ILaunch launch) {
		if (launch == null)
			return Collections.emptyList();
		Builder<IStreamMonitor> builder = ImmutableList.<IStreamMonitor> builder();
		for (IProcess process : launch.getProcesses()) {
			IStreamsProxy proxy = process.getStreamsProxy();
			if (proxy == null) {
				continue;
			}
			builder.add(proxy.getOutputStreamMonitor());
			builder.add(proxy.getErrorStreamMonitor());
		}
		return builder.build();
	}

	public void startLogging(AutLaunch launch) {
		stopLogging();
		log.clear();
		this.launch = launch;
		for (IStreamMonitor sm : getMonitors(launch.getLaunch())) {
			sm.addListener(listener);
			if (sm instanceof IFlushableStreamMonitor flushable) {
				log.append(sm.getContents());
				flushable.setBuffered(false);  // Fixes OutOfMemoryError
			}
		}
		logStarted = true;
	}

	public void stopLogging() {
		logStarted = false;
		AutLaunch launch2 = launch;
		if (launch2 == null)
			return;
		for (IStreamMonitor sm : getMonitors(launch2.getLaunch())) {
			sm.removeListener(listener);
			if (sm instanceof IFlushableStreamMonitor flushable) {
				flushable.setBuffered(true);
			}
		}
		log.clear();
		launch = null;
	}

	@Override
	public void stateChanged(AutLaunch launch, AutLaunchState state) {
		if (state.equals(AutLaunchState.TERMINATE)) {
			stopLogging();
		}
	}

	public String getLog() {
		if (!logStarted) {
			throw new IllegalStateException("Log is not started");
		}
		return log.toString();
	}

	@Override
	public void autStarted(AutLaunch baseAutLaunch, int eclPort, int teslaPort) {
	}

	@Override
	public void autInit(AutLaunch baseAutLaunch, List<AutBundleState> bundleState) {
	}

	@Override
	public void autLocationChange(BaseAutLaunch baseAutLaunch, String location) {
	}

	private int getLogSizeOfInitPart() {
		return TeslaFeatures.getInstance().getIntValue(REPORT_LOGGING_SIZE_OF_INITIAL_PART) * BITE_IN_MEGABITE;
	}

	private int getLogSizeOfRotationPart() {
		return TeslaFeatures.getInstance().getIntValue(REPORT_LOGGING_SIZE_OF_ROTATION_PART) * BITE_IN_MEGABITE;
	}

}
