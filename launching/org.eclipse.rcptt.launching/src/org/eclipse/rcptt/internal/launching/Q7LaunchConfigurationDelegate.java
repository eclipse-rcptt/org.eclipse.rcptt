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
package org.eclipse.rcptt.internal.launching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.workspace.IWorkspaceFinder;
import org.eclipse.rcptt.ecl.debug.commands.CommandsFactory;
import org.eclipse.rcptt.ecl.debug.commands.ServerInfo;
import org.eclipse.rcptt.ecl.debug.commands.StartServer;
import org.eclipse.rcptt.ecl.debug.core.DebuggerBaseTransport;
import org.eclipse.rcptt.ecl.debug.core.DebuggerTransport;
import org.eclipse.rcptt.internal.core.WorkspaceMonitor;
import org.eclipse.rcptt.internal.launching.aut.BaseAutLaunch;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.Q7Launcher;
import org.eclipse.rcptt.launching.Q7Launcher.LaunchData;
import org.eclipse.rcptt.launching.utils.TestSuiteUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class Q7LaunchConfigurationDelegate extends LaunchConfigurationDelegate
		implements IQ7Launch {

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Validating RCPTT tests and contexts...", 2);
		try {
			IQ7NamedElement[] tests = getTests(configuration,
					SubMonitor.convert(monitor, 1));

			for (IQ7NamedElement t : tests) {
				if (t instanceof ITestCase) {
					try {
						// triggers underlying deserilization from plain text
						t.getID();
					} catch (Exception e) {
						throw new CoreException(
								Q7LaunchingPlugin.createStatus(String.format(
										"The '%s' test is totally broken.",
										t.getName()), e));
					}
				} else if (t instanceof IContext) {
					try {
						// triggers underlying deserilization from plain text
						t.getID();
					} catch (Exception e) {
						throw new CoreException(
								Q7LaunchingPlugin.createStatus(String.format(
										"The '%s' context is totally broken.",
										t.getName()), e));
					}
				} else if (t instanceof IVerification) {
					try {
						// triggers underlying deserilization from plain text
						t.getID();
					} catch (Exception e) {
						throw new CoreException(
								Q7LaunchingPlugin.createStatus(String.format(
										"The '%s' verification is totally broken.",
										t.getName()), e));
					}
				}
			}
		} finally {
			monitor.done();
		}

		// -- if we still not have target AUT, ask for it

		if (getAut(configuration) == null) {
			AutLaunch aut = TestSuiteUtils.SelectAUT();
			if (aut == null)
				return false; // cancel pressed
			ILaunchConfigurationWorkingCopy copy = configuration
					.getWorkingCopy();
			copy.setAttribute(IQ7Launch.ATTR_AUT_NAME, aut.getAut().getName());
			copy.doSave();
		}

		return true;
	}

	private static Aut getAut(ILaunchConfiguration configuration) {
		try {
			String autName = configuration.getAttribute(IQ7Launch.ATTR_AUT_NAME, "");
			autName = configuration.getAttribute(IQ7Launch.ATTR_BOUND_AUT_NAME, autName);
			return AutManager.INSTANCE.getByName(autName);
		} catch (CoreException e) {
			Q7LaunchingPlugin.log(e);
			return null;
		}
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return new Q7TestLaunch(configuration, mode);
	}

	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Launching RCPTT tests...", 3);
		waitForSave(SubMonitor.convert(monitor, 1));
		AutLaunch autLaunch = getAutLaunch(config,
				SubMonitor.convert(monitor, 1));
		IQ7NamedElement[] elements = getTests(config,
				SubMonitor.convert(monitor, 1));
		LaunchData data = Q7Launcher.getInstance().getRemoveLaunchData(config);
		if (data != null && (elements == null || elements.length == 0)) {
			// check internal launch elements
			elements = data.elements;
		}
		if (elements.length == 0) {
			throw new CoreException(
					Q7LaunchingPlugin
							.createStatus("No scenarios found in the '"
									+ config.getName()
									+ "' launch configuration "));
		}

		Map<String, String> variants = config.getAttribute(
				IQ7Launch.ATTR_VARIANTS, new HashMap<String, String>());

		Map<IQ7NamedElement, List<List<String>>> namedVariants = new HashMap<IQ7NamedElement, List<List<String>>>();
		for (IQ7NamedElement namedElement : elements) {
			String var = variants.get(namedElement.getID());
			if (var != null) {
				List<String> rawValues = ImmutableList.copyOf(Splitter.on(IQ7Launch.VARIANTS_SEPARATOR).split(var));
				if (rawValues != null) {
					List<List<String>> values = new ArrayList<List<String>>();
					for (String rawValue : rawValues) {
						values.add(ImmutableList.copyOf(
								Splitter.on(IQ7Launch.VARIANT_NAME_SEPARATOR).omitEmptyStrings().split(rawValue)));
					}
					namedVariants.put(namedElement, values);
				}
			}
		}

		Q7Launcher.setMappedResources(config, elements, autLaunch.getCapability());
		IWorkspaceFinder finder = data == null ? null : data.finder;
		launch(elements, (Q7TestLaunch) launch, autLaunch, finder,
				namedVariants);
		monitor.done();
	}

	private void waitForSave(IProgressMonitor monitor) {
		try {
			Job.getJobManager().join(WorkspaceMonitor.SAVE_JOB_FAMILY, monitor);
		} catch (Exception e) {
			Q7LaunchingPlugin.log(e);
		}
	}

	private AutLaunch getAutLaunch(ILaunchConfiguration config,
			IProgressMonitor monitor) throws CoreException {
		Aut aut = getAut(config);
		if (aut == null) {
			throw new CoreException(
					Q7LaunchingPlugin
							.createStatus("AUT is not specified in the '"
									+ config.getName()
									+ "' launch configuration "));
		}
		AutLaunch launch = aut.getActiveLaunch();
		if (launch == null) {
			launch = aut.launch(monitor);
		}

		// okay, we got it, time to forget it
		ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
		copy.setAttribute(IQ7Launch.ATTR_AUT_NAME, "");
		copy.doSave();

		return launch;
	}

	private IQ7NamedElement[] getTests(ILaunchConfiguration config,
			IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Collecting tests...", 1);
		IQ7NamedElement[] elements = TestSuiteUtils.getElements(config);
		monitor.worked(1);
		monitor.done();
		return elements;
	}

	private void launch(IQ7NamedElement[] elements, Q7TestLaunch launch,
			AutLaunch aut, IWorkspaceFinder finder,
			Map<IQ7NamedElement, List<List<String>>> namedVariants)
			throws CoreException {
		Q7LaunchManager.getInstance().execute(elements, aut, launch, finder,
				namedVariants, this::createDebugTransport);
	}
	
	private DebuggerTransport createDebugTransport(String host, Integer port) {
		try {
			return DebuggerBaseTransport.create(port, host);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		return false;
	}

}
