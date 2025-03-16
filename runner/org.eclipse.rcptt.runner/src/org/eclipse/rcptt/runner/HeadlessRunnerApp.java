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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.rcptt.internal.launching.Q7LaunchManager;
import org.eclipse.rcptt.runner.util.AutLaunchFail;
import org.eclipse.rcptt.runner.util.TargetPlatformFail;

public class HeadlessRunnerApp implements IApplication {

	public static final String RETURN_TEST_FAILURE_PROPERTY = "org.eclipse.rcptt.runner.returnTestFailure";
	public static final int TEST_FAIL_EXIT_CODE = 56;
	public static final int TARGET_PLATFORM_FAIL_EXIT_CODE = 62;
	public static final int AUT_FAIL_EXIT_CODE = 63;
	public static final int ILLEGAL_ARGUMENT = 64;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		disableProxyService();
		System.out
				.println("Started at " + new Date(System.currentTimeMillis()));
		System.out.println(String.format("RCPTT Runner version: %s",
				getRunnerVersion()));

		// parse arguments
		final String[] cmdLineArgs = deQuoteArgs((String[]) context
				.getArguments().get(IApplicationContext.APPLICATION_ARGS));
		Q7LaunchManager.makeHeadless();
		RunnerConfiguration conf = new RunnerConfiguration();
		if (!conf.processArgs(cmdLineArgs)) {
			return ILLEGAL_ARGUMENT;
		}

		
		boolean returnTestFailure = Boolean.valueOf(System.getProperty(
				RETURN_TEST_FAILURE_PROPERTY, "true"));

		try {
			int failCount = new HeadlessRunner(conf).performCoolThings();
			if (failCount == 0) {
				System.out.println("Finished at "
						+ new Date(System.currentTimeMillis()));
				return IApplication.EXIT_OK;
			} else {
				return returnTestFailure ? TEST_FAIL_EXIT_CODE : IApplication.EXIT_OK;
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return ILLEGAL_ARGUMENT;
		} catch (AutLaunchFail e) {
			return AUT_FAIL_EXIT_CODE;
		} catch (TargetPlatformFail e) {
			return TARGET_PLATFORM_FAIL_EXIT_CODE;
		}
	}

	private static void disableProxyService() {
		System.setProperty("org.eclipse.net.core.enableProxyService", "false");

	}

	@Override
	public void stop() {
		// don't do anything
	}

	//

	private static String getRunnerVersion() {
		String version = Platform.getBundle(HeadlessRunnerPlugin.PLUGIN_ID).getHeaders()
				.get("Bundle-Version");
		if (version == null)
			return "[unknown]";
		int dotPos = version.lastIndexOf('.');
		if (dotPos < 0)
			return version;
		return version.substring(0, dotPos);
	}

	// args stuff

	private static String[] deQuoteArgs(String[] args) {
		List<String> result = new ArrayList<String>();
		for (String arg : args) {
			result.add(deQuote(arg));
		}
		return result.toArray(new String[result.size()]);
	}

	private static String deQuote(String line) {
		if (line != null) {
			if (line.startsWith("'") && line.endsWith("'")) {
				line = line.substring(1, line.length() - 1);
			}
			if (line.startsWith("\"") && line.endsWith("\"")) {
				line = line.substring(1, line.length() - 1);
			}
		}
		return line;
	}

}
