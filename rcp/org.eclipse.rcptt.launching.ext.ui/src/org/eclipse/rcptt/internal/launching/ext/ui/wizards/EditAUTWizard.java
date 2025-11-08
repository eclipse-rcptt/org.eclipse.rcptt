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
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.internal.launching.IPDEConstants;
import org.eclipse.pde.internal.launching.launcher.LaunchArgumentsHelper;
import org.eclipse.pde.internal.launching.launcher.LaunchConfigurationHelper;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.rcptt.internal.launching.aut.BaseAutManager;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.internal.launching.ext.UpdateVMArgs;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutLaunchState;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.common.Q7LaunchingCommon;
import org.eclipse.rcptt.launching.ext.Q7LaunchingUtil;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;
import org.eclipse.rcptt.launching.utils.AUTLaunchArgumentsHelper;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.rcptt.ui.launching.aut.IAUTConfigWizard;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class EditAUTWizard extends Wizard implements IAUTConfigWizard {
	private static final String ATTR_D32 = "-d32";
	private NewAUTPage page;
	private ILaunchConfiguration configuration;
	private WizardDialog wizardDialog;

	public EditAUTWizard() {
		setWindowTitle("Edit Application Under Test");
	}

	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		this.configuration = configuration;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		page = new NewAUTPage("edit", "Edit Application Under Test", null);

		try {
			String configName = this.configuration.getName();
			String autLocation = this.configuration.getAttribute(
					IQ7Launch.AUT_LOCATION, "");
			page.initializeExisting(configName, autLocation, this.configuration);
			page.addAdvancedHandler(new Runnable() {
				public void run() {
					Shell shell = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell();
					// page.getShell().close();
					wizardDialog.close();

					DebugUITools.openLaunchConfigurationPropertiesDialog(shell,
							configuration,
							IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
				}
			});
		} catch (Throwable e) {
			Q7UIPlugin.log(e);
		}
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		ITargetPlatformHelper target = page.getTarget();
		if (target != null) {
			if (!target.getStatus().isOK()) {
				target.delete();
			}
		}
		return super.performCancel();
	}

	public boolean performFinish() {
		ITargetPlatformHelper target = page.getTarget();
		if (!target.getStatus().isOK()) {
			page.setStatus(target.getStatus());
			target.delete();
			return false;
		}
		try {

			// Delete all files in configuration area.
			File area = LaunchConfigurationHelper
					.getConfigurationArea(configuration);
			if (area != null && area.exists()) {
				FileUtil.deleteFile(area, false);
			}

			LaunchInfoCache.remove(configuration);

			ILaunchConfigurationWorkingCopy workingCopy = configuration
					.getWorkingCopy();
			Q7LaunchingUtil.updateLaunchConfiguration(target, workingCopy);
			target.save();

			workingCopy.rename(page.getTargetName());
			String vmArgs = workingCopy.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
					target.getIniVMArgs());
			if (vmArgs == null) {
				// Lets use current runner vm arguments
				vmArgs = LaunchArgumentsHelper.getInitialVMArguments()
						.trim();
			} else {
				vmArgs = vmArgs.trim();
			}
			if (vmArgs != null && vmArgs.length() > 0) {
				vmArgs = UpdateVMArgs.updateAttr(vmArgs);
				workingCopy
						.setAttribute(
								IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
								vmArgs);
			}

			IVMInstall install = page.getJVMInstall();
			if (install != null) {
				if (workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, (String)null) == null) {
					workingCopy
							.setAttribute(
									IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
									String.format(
											"org.eclipse.jdt.launching.JRE_CONTAINER/%s/%s",
											install.getVMInstallType().getId(),
											install.getName()));
				}
			}

			if (workingCopy
					.getAttribute(
							IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null) == null) {
				String programArgs = workingCopy
						.getAttribute(
								IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
								AUTLaunchArgumentsHelper
									.getInitialProgramArguments(page.getArchitecture().name()));
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
					getDefaultWorkspaceLocation(workingCopy.getName()));

			workingCopy
					.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
							page.getTargetLocation());

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
			workingCopy.setAttribute(
					IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
			workingCopy
					.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false);
			List<AutLaunch> list = AutManager.INSTANCE
					.storeAUTLaunches(configuration);
			LaunchInfoCache.remove(workingCopy);
			ILaunchConfiguration newConfig = workingCopy.doSave();
			LaunchInfoCache.remove(newConfig);
			if (list != null) {
				AutManager.INSTANCE.updateAUTLaunches(newConfig, list);
			}

			if (page.isLaunchNeeded()) {
				Aut aut = BaseAutManager.INSTANCE.getByName(workingCopy.getName());
				if (aut != null) {
					List<AutLaunch> autLaunches = aut.getLaunches();
					for (AutLaunch autLaunch : autLaunches) {
						if (autLaunch.getState() != AutLaunchState.TERMINATE) {
							autLaunch.terminate();
						}
					}
					LaunchUtils.launch(aut, getShell());
				}
			}

			return true;
		} catch (CoreException e) {
			Q7UIPlugin.log(e);
		}
		return false;
	}

	public static String getDefaultWorkspaceLocation(String uniqueName) {
		return "${workspace_loc}/../aut-" + uniqueName.replaceAll("\\s", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void setWizardDialog(WizardDialog dialog) {
		this.wizardDialog = dialog;
	}
}
