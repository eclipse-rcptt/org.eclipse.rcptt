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
package org.eclipse.rcptt.internal.launching.ext.ui.wizards;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.internal.launching.IPDEConstants;
import org.eclipse.pde.internal.launching.launcher.LaunchConfigurationHelper;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.rcptt.internal.launching.aut.BaseAutManager;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.internal.launching.ext.UpdateVMArgs;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.common.Q7LaunchingCommon;
import org.eclipse.rcptt.launching.ext.Q7LaunchDelegateUtils;
import org.eclipse.rcptt.launching.ext.Q7LaunchingUtil;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.utils.AUTLaunchArgumentsHelper;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.ui.IWorkbench;

@SuppressWarnings("restriction")
public class NewAUTWizard extends Wizard {
	private static final String ATTR_D32 = "-d32";
	private NewAUTPage page;

	public NewAUTWizard() {
		setWindowTitle("New Application Under Test");
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		page = new NewAUTPage("new", "New Application Under Test", null);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		ITargetPlatformHelper target = page.getTarget();
		if (!target.getStatus().isOK()) {
			page.setStatus(target.getStatus());
			target.delete();
			return false;
		}
		try {
			target.setTargetName(Q7TargetPlatformManager
					.getTargetPlatformName(page.getTargetName()));
			target.save();
			ILaunchConfigurationWorkingCopy workingCopy = Q7LaunchingUtil
					.createLaunchConfiguration(target, page.getTargetName());
			OSArchitecture autArch = page.getArchitecture();
			workingCopy.setAttribute(Q7LaunchingCommon.ATTR_ARCH,
					autArch.name());
			OSArchitecture jvmArch = page.getJVMArch();
			List<String> vmArgs = Q7LaunchDelegateUtils.getVMArgs(target, null);
			if (!autArch.equals(jvmArch)
					&& Platform.getOS().equals(Platform.OS_MACOSX) && autArch.equals(OSArchitecture.x86)) {
				UpdateVMArgs.addIfAbsent(vmArgs, ATTR_D32, "");
			}
			workingCopy
					.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
							Q7LaunchDelegateUtils.joinCommandArgs(vmArgs));

			IVMInstall install = page.getJVMInstall();
			if (install != null) {
				workingCopy
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
								String.format(
										"org.eclipse.jdt.launching.JRE_CONTAINER/%s/%s",
										install.getVMInstallType().getId(),
										install.getName()));
			}

			String programArgs = AUTLaunchArgumentsHelper
					.getInitialProgramArguments(autArch.name());

			if (programArgs.length() > 0) {
				workingCopy
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
								programArgs);
			}

			workingCopy.setAttribute(IPDEConstants.APPEND_ARGS_EXPLICITLY,
					true);
			String product = target.getDefaultProduct();
			if (product != null) {
				workingCopy.setAttribute(IPDELauncherConstants.USE_PRODUCT,
						true);
				workingCopy.setAttribute(IPDELauncherConstants.PRODUCT,
						product);
			}
			workingCopy.setAttribute(IPDELauncherConstants.DOCLEAR, false);
			workingCopy.setAttribute(IPDELauncherConstants.ASKCLEAR, true);
			workingCopy.setAttribute(IPDEConstants.DOCLEARLOG, false);
			workingCopy.setAttribute(IPDELauncherConstants.LOCATION,
					getUnoccupiedWorkspaceLocation(workingCopy.getName()));

			// String config = target.getTemplateConfigLocation();
			// if (config != null) {
			// workingCopy.setAttribute(
			// IPDELauncherConstants.CONFIG_GENERATE_DEFAULT,
			// false);
			// workingCopy.setAttribute(
			// IPDELauncherConstants.CONFIG_TEMPLATE_LOCATION,
			// config);
			// }
			// Disable console by default

			setDefaultsAttributes(workingCopy);
			workingCopy.doSave();

			if (page.isLaunchNeeded()) {
				LaunchUtils.launch(BaseAutManager.INSTANCE.getByName(workingCopy.getName()), getShell());
			}

			return true;
		} catch (CoreException e) {
			Q7UIPlugin.log(e);
		}
		return false;
	}

	private void setDefaultsAttributes(
			ILaunchConfigurationWorkingCopy configurationWc) throws CoreException {

		String log_directory = new Path(LaunchConfigurationHelper
				.getConfigurationArea(configurationWc).getAbsolutePath())
				.append("console.log").toOSString();

		configurationWc.removeAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT);
		configurationWc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
		configurationWc.setAttribute(IQ7Launch.ATTR_APPEND_TO_FILE, true);
		configurationWc.setAttribute(IQ7Launch.ATTR_CAPTURE_IN_FILE, log_directory);
	}

	public static String getDefaultWorkspaceLocation(String uniqueName) {
		return "${workspace_loc}/../aut-" + uniqueName.replaceAll("\\s", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private String getUnoccupiedWorkspaceLocation(String name) {
		String stringPath = getDefaultWorkspaceLocation(name);
		try {
			if (isLocationExists(stringPath)) { // a new location for a new AUT
				int i = 1;
				String newStringPath;
				do {
					i++;
					newStringPath = getDefaultWorkspaceLocation(name + "-" + i);
				} while (isLocationExists(newStringPath));
				stringPath = newStringPath;
			}
		} catch (CoreException e) {
			Q7UIPlugin.log(e);
		}
		return stringPath;
	}

	private boolean isLocationExists(String location) throws CoreException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		location = manager.performStringSubstitution(location);
		IPath autPath = new Path(location);
		File autDir = new File(autPath.toOSString());
		if (autDir.exists() && autDir.isDirectory()) {
			return true;
		}
		return false;
	}

}
