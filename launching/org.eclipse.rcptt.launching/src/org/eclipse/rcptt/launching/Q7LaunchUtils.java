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
package org.eclipse.rcptt.launching;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.rcptt.core.ecl.core.model.GetQ7Information;
import org.eclipse.rcptt.core.ecl.core.model.Q7CoreFactory;
import org.eclipse.rcptt.core.ecl.core.model.Q7Information;
import org.eclipse.rcptt.core.ecl.core.model.SetQ7Features;
import org.eclipse.rcptt.ecl.client.tcp.EclTcpClientManager;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.util.FileUtil;

public final class Q7LaunchUtils {

	private static String META_DIR = ".metadata";

	private static String CONFIG_DEFAULT_DIR = "${workspace_loc}/" + META_DIR + "/.plugins/org.eclipse.pde.core/"; //$NON-NLS-1$

	private Q7LaunchUtils() {
	}

	public static void verifyConnection(String host, int port)
			throws CoreException {
		ISession session = null;
		try {
			InetAddress byName = InetAddress.getByName(host);
			session = EclTcpClientManager.Instance.startClientSession(byName,
					port);
		} catch (IOException e) {
			throw new CoreException(Q7LaunchingPlugin.createStatus(
					"Couldn't connect to AUT", e));
		}
		try {
			GetQ7Information information = Q7CoreFactory.eINSTANCE
					.createGetQ7Information();
			IPipe out = session.createPipe();
			org.eclipse.rcptt.ecl.runtime.IProcess process = session.execute(
					information, null, out);
			try {
				IStatus status = process.waitFor();
				if (!status.isOK()) {
					throw new CoreException(status);
				}
			} catch (InterruptedException e) {
				throw new CoreException(new Status(IStatus.CANCEL,
						Q7LaunchingPlugin.PLUGIN_ID, e.getMessage(), e));
			}
			Object object = out.take(TeslaLimits.getAUTStartupTimeout());
			if (object instanceof Q7Information) {
				Q7Information info = (Q7Information) object;
				if (!info.isTeslaActive()) {
					throw new CoreException(
							Q7LaunchingPlugin
									.createStatus("Tesla is not activated"));
				}
			} else {
				throw new CoreException(
						Q7LaunchingPlugin
								.createStatus("Expect Q7Information but found: "
										+ object));
			}
		} finally {
			session.close();
		}
	}

	public static String getConfigFilesLocation(ILaunchConfiguration config) {
		String filesLocation = "";
		try {
			boolean useDefaultArea = config.getAttribute(
					IPDELauncherConstants.CONFIG_USE_DEFAULT_AREA, true);
			if (!useDefaultArea) {
				filesLocation = config.getAttribute(
						IPDELauncherConstants.CONFIG_LOCATION, "");
			} else {
				filesLocation = CONFIG_DEFAULT_DIR + config.getName()
						+ IPath.SEPARATOR;
			}
			filesLocation = VariablesPlugin.getDefault()
					.getStringVariableManager()
					.performStringSubstitution(filesLocation);
		} catch (CoreException e) {
			Q7LaunchingPlugin.log(e);
		}
		return filesLocation;
	}

	public static File getWorkspaceMeta(ILaunchConfiguration config) {
		try {
			String wsFile = config.getAttribute(IPDELauncherConstants.LOCATION, ""); //$NON-NLS-1$
			wsFile = VariablesPlugin.getDefault().getStringVariableManager()
					.performStringSubstitution(wsFile);

			File file = new File(wsFile);
			if (file.exists()) {
				File meta = new File(file, META_DIR);
				if (meta.exists()) {
					return meta;
				}
			}
		} catch (CoreException e) {
			Q7LaunchingPlugin.log(e);
		}
		return null;
	}

	public static void deleteConfigFiles(ILaunchConfiguration config) {
		String configLocation = Q7LaunchUtils.getConfigFilesLocation(config);
		final File configFolder = new File(configLocation);
		if (configFolder.exists()) {
			FileUtil.deleteFile(configFolder, true);
		}
	}

	private static final String Q7_VARIABLES_KEY = "org.eclipse.rcptt.tesla.variables.q7.";
	public static final String Q7_CURRENT_TEST_VAR = "current_test";

	public static void setQ7Variable(SetQ7Features features, String name,
			String value) {
		if (value != null)
			features.getFeatures().add(
					String.format("%s%s=%s", Q7_VARIABLES_KEY, name, value));
	}
}
