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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
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
				ServerSocket serverSocket = null;
				try {
					serverSocket = new ServerSocket(port);
					serverSocket.accept();
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

	private static final String VM_INSTALL_TYPE = "org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType";
	private String autVM = null;

	public String getAutVm() {
		if (autVM != null) {
			return autVM;
		}
		for (int i = 0; i < 5; i++) {
			try {
				autVM = "aut_vm_" + System.currentTimeMillis()
						+ new Random().nextInt();
				IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
				for (IVMInstallType ivmInstallType : types) {
					// Dispose old vm installs
					IVMInstall[] vmInstalls = ivmInstallType.getVMInstalls();
					for (IVMInstall ivmInstall : vmInstalls) {
						ivmInstallType.disposeVMInstall(ivmInstall.getId());
					}
					if (ivmInstallType.getId().equals(VM_INSTALL_TYPE)) {
						IVMInstall install = ivmInstallType.createVMInstall(autVM);
						install.setInstallLocation(new File(conf.javaVM));
						install.setName(autVM);
						JavaRuntime.saveVMConfiguration();
						break;
					}
				}
			} catch (Throwable e) {
				HeadlessRunnerPlugin.getDefault().info(e.getMessage());
				continue;
			}
			break;
		}
		return autVM;
	}

	public String addJvmFromIniFile() {
		String vmFromIni = tpc.getTargetPlatform().getVmFromIniFile();
		if (vmFromIni == null) {
			return null;
		}
		System.out.println("Trying to use VM from application's ini file: "
				+ vmFromIni);

		File file = new File(vmFromIni);
		if (!file.isAbsolute()) {
			file = new File(new File(conf.location), vmFromIni);
		}
		if (!file.exists()) {
			System.out
					.println(file + " does not exist");
			return null;
		}

		if ("bin".equals(file.getParentFile().getName()) || "lib".equals(file.getParentFile().getName()) ) {
			file = file.getParentFile().getParentFile();
		} else if ("bin".equals(file.getName())) {
			file = file.getParentFile();
		} else {
			file = null;
		}

		if (file == null) {
			System.out
					.println("Unknown file system layout of Java VM from ini file");
			return null;
		}
		
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		IVMInstallType type = JavaRuntime.getVMInstallType(VM_INSTALL_TYPE);
		if (type == null) {
			System.out
					.println("Error configuring Java VM from ini file - StandardVMType does not exist");
			System.out.println("Available types are:");
			for (IVMInstallType vmtype : JavaRuntime.getVMInstallTypes()) {
				System.out.println(vmtype.getId());
			}
		}

		String vmName = String.format("%s-jre",
				new File(conf.location).getName());
		type.disposeVMInstall(vmName);

		IVMInstall install = type.createVMInstall(vmName);
		install.setInstallLocation(file);
		install.setName(vmName);

		try {
			JavaRuntime.saveVMConfiguration();
		} catch (CoreException e) {
			System.out.println("Error configuring Java VM from ini file");
			e.printStackTrace();
			return null;
		}
		return String.format("org.eclipse.jdt.launching.JRE_CONTAINER/%s/%s",
				VM_INSTALL_TYPE, vmName);
	}
}
