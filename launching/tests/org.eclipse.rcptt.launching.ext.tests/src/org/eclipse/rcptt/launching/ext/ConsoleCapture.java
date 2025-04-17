/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching.ext;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;

import com.google.common.io.Closer;

final class ConsoleCapture implements Closeable {
	private final StringBuffer buffer = new StringBuffer();
	private final Closer closer = Closer.create();
	private final StringBuffer tail = new StringBuffer();

	private final IStreamListener streamListener = new IStreamListener() {
		@Override
		public void streamAppended(String text, IStreamMonitor monitor) {
			buffer.append(text);
			String toPrint = "";
			synchronized (tail) {
				tail.append(text);
				int pos = tail.lastIndexOf("\n");
				if (pos <= 0) {
					return;
				}
				toPrint = tail.substring(0, pos);
				tail.delete(0, pos + 1);
			}
			
			for (String line: toPrint.split("\n")) {
				System.out.print("AUT output: " + line + "\n");
			}
		}
	};

	private final ILaunchesListener2 launchesListener = new ILaunchesListener2() {
		@Override
		public void launchesRemoved(ILaunch[] launches) {
		}

		@Override
		public void launchesAdded(ILaunch[] launches) {
		}

		@Override
		public synchronized void launchesChanged(ILaunch[] launches) {
			for (ILaunch l : launches) {
				for (IProcess p : l.getProcesses()) {
					capture(p.getStreamsProxy().getOutputStreamMonitor());
					capture(p.getStreamsProxy().getErrorStreamMonitor());
				}
			}
		}

		@Override
		public void launchesTerminated(ILaunch[] launches) {
		}
	};

	@SuppressWarnings("resource")
	public ConsoleCapture() {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		manager.addLaunchListener(launchesListener);
		closer.register(() -> manager.removeLaunchListener(launchesListener));
	}

	public String getOutput() {
		return buffer.toString();
	}

	@Override
	public void close() throws IOException {
		try {
			closer.close();
		} finally {
			System.out.print("AUT output: " + tail);
		}
	}

	private final Set<Object> capturing = new HashSet<>();

	@SuppressWarnings("resource")
	private void capture(IStreamMonitor ouptutMonitor) {
		if (capturing.add(ouptutMonitor)) {
			ouptutMonitor.addListener(streamListener);
			closer.register(() -> ouptutMonitor.removeListener(streamListener));
		}
	}

}