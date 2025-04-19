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
package org.eclipse.rcptt.testing;

import java.io.File;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.pde.internal.launching.PreferenceInitializer;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ext.PDELocationUtils;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.ext.Q7LaunchDelegateUtils;
import org.eclipse.rcptt.launching.ext.Q7LaunchingUtil;
import org.eclipse.rcptt.launching.injection.InjectionConfiguration;
import org.eclipse.rcptt.launching.injection.InjectionFactory;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.testing.commands.InvokeAUT;

@SuppressWarnings("restriction")
public class InvokeAUTService implements ICommandService {

	private static String eclipseLocation = null;

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			new PreferenceInitializer().initializeDefaultPreferences();
		}
		InvokeAUT cmd = (InvokeAUT) command;
		String name = cmd.getName();

		Aut aut = AutManager.INSTANCE.getByName(name);
		if (aut == null) {
			// AUT is not found. Let's create self AUT
			aut = createAut(cmd);
		}

		AutLaunch launch = aut.getActiveLaunch();
		if (launch == null) {
			// AUT is not launched. Let's launch it
			launch = aut.launch(new NullProgressMonitor());
		}

		return Status.OK_STATUS;
	}

	private Aut createAut(InvokeAUT cmd) throws CoreException {
		final String location;

		if (cmd.getPath() != null) {
			location = PDELocationUtils.getProductLocation(cmd.getPath()).getAbsolutePath();
		} else {
			updateEclipseLocation();
			if (eclipseLocation == null) {
				throw new CoreException(
						RcpttPlugin
								.createStatus("Failed to launch selfAUT. ${eclipse_home} variable is not resolved..."));
			}
			location = eclipseLocation;
		}
		// ITargetPlatformHelper platform = TargetPlatformManager
		// .getCurrentTargetPlatform();
		// platform.resolve(new NullProgressMonitor());
		// Q7TargetPlatformManager.createTargetPlatform(location, monitor,
		// addErrorsToLog)
		ITargetPlatformHelper platform = Q7TargetPlatformManager
				.createTargetPlatform(location, cmd.getName(),
						new NullProgressMonitor());
		
		ILaunchConfigurationWorkingCopy launch = Q7LaunchingUtil
				.createLaunchConfiguration(platform, cmd.getName());
		
		platform.save();

		InjectionConfiguration configuration = InjectionFactory.eINSTANCE.createInjectionConfiguration();
		configuration.getEntries().addAll(cmd.getInject());
		
		IStatus rv = platform.applyInjection(configuration, new NullProgressMonitor());
		if (!rv.isOK())
			throw new CoreException(rv);
		

		launch.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				"-os ${target.os} -arch ${target.arch} -nl ${target.nl} -consoleLog "
						+ cmd.getArgs()
						+ " -data "
						+ RcpttPlugin.getDefault().getStateLocation()
								.append("self_aut_")
						+ System.currentTimeMillis());

		launch.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE,
				(String) null);
		launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, (String) null);
		launch.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				getVMArgs(cmd, platform));

		ILaunchConfiguration config = launch.doSave();
		return AutManager.INSTANCE.getByLaunch(config);
	}

	private String getVMArgs(InvokeAUT cmd, ITargetPlatformHelper platform) {
		if (cmd.getVmargs() != null) {
			return cmd.getVmargs();
		}
		return Q7LaunchDelegateUtils.getJoinedVMArgs(platform, Collections.<String> emptyList());
	}

	public static void updateEclipseLocation() {
		if (eclipseLocation != null) {
			return;
		}
		IStringVariableManager vars = VariablesPlugin.getDefault()
				.getStringVariableManager();
		IDynamicVariable var = vars.getDynamicVariable("eclipse_home");
		try {
			String value = var.getValue(null);
			if (new File(value).exists()) {
				eclipseLocation = value;
			}
		} catch (CoreException e) {
		}
	}
}
