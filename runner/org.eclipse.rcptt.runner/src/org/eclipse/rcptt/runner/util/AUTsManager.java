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
package org.eclipse.rcptt.runner.util;

import static org.eclipse.rcptt.runner.HeadlessRunnerPlugin.PLUGIN_ID;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.rcptt.launching.ext.VmInstallMetaData;
import org.eclipse.rcptt.runner.HeadlessRunnerPlugin;
import org.eclipse.rcptt.runner.RunnerConfiguration;
import org.eclipse.rcptt.runner.ScenarioRunnable;

public class AUTsManager {
	final List<AutThread> autThreads = new ArrayList<AutThread>();
	final AtomicInteger autCounter = new AtomicInteger(0);

	private final RunnerConfiguration conf;
	private final TargetPlatformChecker tpc;

	public AUTsManager(RunnerConfiguration conf, TargetPlatformChecker tpc) {
		this.conf = conf;
		this.tpc = tpc;
		if (conf.shudownListenerPort > 0) {
			createShutdownListener(conf.shudownListenerPort);
		}
	}

	public boolean isClean() {
		return autThreads.isEmpty();
	}

	public void launchAutsAndStartTheirThreads(List<ScenarioRunnable> runnables) throws AutLaunchFail {
		for (int i = 0; i < conf.autCount; i++) {
			if (runnables.isEmpty()) {
				break;
			}

			AutThread t = new AutThread(runnables, this, conf, tpc);
			int a = 0;
			while (true) {
				try {
					t.launchAut();
					break;
				} catch (RuntimeException e) {
					if (a > 10)
						throw new AutLaunchFail("AUT launch failed after 10 tries.", e);
					System.out
							.println("Failed to launch AUT:" + e.getMessage());
					HeadlessRunnerPlugin.getDefault().info(
							"Failed to launch AUT:" + e.getMessage());
				}
				a++;
			}
			autThreads.add(t);
		}
		for (AutThread t : autThreads) {
			t.start();
		}
	}

	//

	public void initShutdownHook() {
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	public void removeShutdownHook() {
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
	}

	// see also: createShutdownListener
	private Thread shutdownHook = new Thread() {
		@Override
		public void run() {
			try {
				shutdownAUTs();
			} catch (CoreException e) {
				HeadlessRunnerPlugin.log(e.getStatus());
			}
		}
	};

	private void createShutdownListener(final int port) {
		new Thread() {
			@Override
			public void run() {
				try (ServerSocket serverSocket = new ServerSocket(port)) {
					try (var ignored = serverSocket.accept()) {}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					shutdownAUTs();
				} catch (CoreException e) {
					HeadlessRunnerPlugin.log(e.getStatus());
				}
				Runtime.getRuntime().exit(IStatus.OK);
			}
		}.start();
	}

	void shutdownAUTs() throws CoreException {
		System.out.println("Process terminated. Shut down AUTs");
		MultiStatus status = new MultiStatus(PLUGIN_ID, 0, "Failed to shutdown AUTs", null);
		for (final AutThread thread : autThreads) {
			try {
				thread.shutdown();
			} catch (InterruptedException e) {
				// We are only invoked at the end of the TestsRunner lifecycle. So ignore
			} catch (CoreException e) {
				status.add(new MultiStatus(PLUGIN_ID, 0, new IStatus[] { e.getStatus() }, "Aut " + thread, null));
			}
		}
		autThreads.clear();
		if (!status.isOK())
			throw new CoreException(status);
	}

	//

	private Optional<VmInstallMetaData> autVM = null;

	public Optional<VmInstallMetaData> getAutVm() throws CoreException {
		if (autVM != null) {
			return autVM;
		}
		if (conf.javaVM != null) {
			return autVM = Optional.of(VmInstallMetaData.register(Path.of(conf.javaVM)));
		}

		if (conf.executionEnvironment != null) {
			return autVM = Optional
					.ofNullable(JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(conf.executionEnvironment))
					.flatMap(e -> Optional.ofNullable(e.getDefaultVM())).flatMap(VmInstallMetaData::adapt);
		}

		return autVM = addJvmFromIniFile();
	}

	private Optional<VmInstallMetaData> addJvmFromIniFile() throws CoreException {
		Path vmFromIni = tpc.getTargetPlatform().getJavaHome().orElse(null);
		if (vmFromIni == null) {
			return Optional.empty();
		}
		System.out.println("Trying to use VM from application's ini file: "
				+ vmFromIni);
		return Optional.of(VmInstallMetaData.register(vmFromIni));
	}

}
