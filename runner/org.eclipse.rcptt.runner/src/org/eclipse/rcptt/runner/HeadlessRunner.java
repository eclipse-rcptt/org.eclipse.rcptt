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
package org.eclipse.rcptt.runner;

import java.io.File;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.rcptt.reporting.util.Q7ReportIterator;
import org.eclipse.rcptt.runner.util.Reporter;
import org.eclipse.rcptt.runner.util.ResultsHandler;
import org.eclipse.rcptt.runner.util.TargetPlatformChecker;
import org.eclipse.rcptt.runner.util.TargetPlatformFail;
import org.eclipse.rcptt.runner.util.TestsRunner;

@SuppressWarnings("restriction")
public class HeadlessRunner {
	private final ILog LOG = Platform.getLog(HeadlessRunner.class);

	public final Reporter reporter = new Reporter();

	private final RunnerConfiguration conf;
	public final TargetPlatformChecker tpc;

	public HeadlessRunner(RunnerConfiguration conf) {
		this.conf = conf;
		tpc = new TargetPlatformChecker(conf);
	}

	public int performCoolThings() throws Exception {
		clearConfigurations();
		clearPDEFiles();

		RunnerOptionsHandler runnerOptions = new RunnerOptionsHandler();
		runnerOptions.applyOptions(conf.testOptions);


		try {
			tpc.initAndCheckTargetPlatform();
		} catch(CoreException e) {
			LOG.log(e.getStatus());
			e.printStackTrace(System.err);
			throw new TargetPlatformFail();
		}

		TestsRunner testsRunner = new TestsRunner(conf, this,
				new ResultsHandler(conf, runnerOptions.isRestartAUTOnFailures()));
		try(Q7ReportIterator reportIterator = testsRunner.findAndRunTests()) {
			reporter.report(reportIterator, this, conf);
		}
		testsRunner.throwOnError();
		
		return testsRunner.getFailedCount();
	}

	//

	private void clearPDEFiles() {
		File file = PDECore.getDefault().getStateLocation().toFile();
		File[] listFiles = file.listFiles();
		for (File f : listFiles) {
			if (!f.getName().startsWith(".")
					|| f.getName().equalsIgnoreCase(".local_targets")
					|| f.getName().equalsIgnoreCase(".install_folders")) {
				deleteDir(f);
			}
		}
	}

	private void clearConfigurations() {
		DebugPlugin debug = DebugPlugin.getDefault();
		ILaunchManager launchManager = debug.getLaunchManager();
		try {
			Method method = LaunchManager.class
					.getDeclaredMethod("clearAllLaunchConfigurations");
			method.setAccessible(true);
			method.invoke(launchManager);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		File launchesPath = debug.getStateLocation().append(".launches")
				.toFile();
		deleteDir(launchesPath);
	}

	public static boolean deleteDir(final File dir) {
		if (dir.isDirectory()) {
			final String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				final boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}// The directory is now empty so delete it
		return dir.delete();
	}

}
