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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

import org.eclipse.rcptt.core.launching.events.AutBundleState;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutLaunchListener;
import org.eclipse.rcptt.launching.AutLaunchState;

/**
 * Monitors a single instance of {@link AutLaunch} and aggregates output from
 * all processes into a single file
 * 
 * @author ivaninozemtsev
 * 
 */
public class OutputCaptureLaunchListener implements AutLaunchListener {

	private PrintStreamListener listener;
	private File logFile;

	public OutputCaptureLaunchListener(File logFile) {
		this.logFile = logFile;

	}

	public void stateChanged(AutLaunch launch, AutLaunchState state) {
		startLogging(launch.getLaunch().getProcesses());
		if (state.equals(AutLaunchState.TERMINATE)) {
			stopLogging();
		}
	}

	private void startLogging(IProcess[] processes) {
		try {
			if (listener == null) {
				this.listener = new PrintStreamListener(new PrintStream(
						new FileOutputStream(logFile, true), true,
						StandardCharsets.UTF_8.name()));
			}

			for (IProcess process : processes) {
				IStreamsProxy proxy = process.getStreamsProxy();
				if (proxy == null) {
					continue;
				}

				for (IStreamMonitor sm : new IStreamMonitor[] {
						proxy.getOutputStreamMonitor(),
						proxy.getErrorStreamMonitor() }) {
					listener.streamAppended(sm.getContents(), sm);
					sm.removeListener(listener);
					sm.addListener(listener);
				}
			}
		} catch (IOException e) {
			Q7LaunchingPlugin.log(
					String.format("Error appending to log file '%s'",
							logFile.getAbsolutePath()), e);
			return;
		}
	}

	private void stopLogging() {
		if (listener != null) {
			listener.stream.flush();
			listener.stream.close();
			listener = null;
		}
	}

	private static class PrintStreamListener implements IStreamListener {
		public final PrintStream stream;

		public PrintStreamListener(PrintStream stream) {
			this.stream = stream;
		}

		public void streamAppended(String text, IStreamMonitor monitor) {
			if (stream == null) {
				return;
			}
			stream.append(text);
			if (monitor instanceof IFlushableStreamMonitor) {
				((IFlushableStreamMonitor) monitor).flushContents();
			}
		}
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
}
