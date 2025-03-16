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
package org.eclipse.rcptt.launching.ext;

import static java.util.Arrays.asList;
import static org.eclipse.rcptt.internal.launching.ext.AJConstants.OSGI_FRAMEWORK_EXTENSIONS;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.log;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.status;
import static org.eclipse.rcptt.launching.ext.Q7LaunchDelegateUtils.id;
import static org.eclipse.rcptt.launching.ext.Q7LaunchDelegateUtils.setDelegateFields;
import static org.eclipse.rcptt.launching.ext.StartLevelSupport.getStartInfo;
import static org.eclipse.rcptt.util.Versions.isGreater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
import org.eclipse.pde.internal.core.target.ProfileBundleContainer;
import org.eclipse.pde.internal.launching.PDEMessages;
import org.eclipse.pde.internal.launching.launcher.LaunchArgumentsHelper;
import org.eclipse.pde.internal.launching.launcher.VMHelper;
import org.eclipse.pde.launching.EclipseApplicationLaunchConfiguration;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache.CachedInfo;
import org.eclipse.rcptt.internal.launching.ext.AJConstants;
import org.eclipse.rcptt.internal.launching.ext.IBundlePoolConstansts;
import org.eclipse.rcptt.internal.launching.ext.JDTUtils;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchMonitor;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.internal.launching.ext.UpdateVMArgs;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.common.Q7LaunchingCommon;
import org.eclipse.rcptt.launching.events.AutEventManager;
import org.eclipse.rcptt.launching.internal.target.Q7Target;
import org.eclipse.rcptt.launching.internal.target.TargetPlatformHelper;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.util.FileUtil;
import org.osgi.framework.Version;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

@SuppressWarnings("restriction")
public class Q7ExternalLaunchDelegate extends
		EclipseApplicationLaunchConfiguration {

	private static final String ATTR_D32 = "-d32";

	private static final String Q7_LAUNCHING_AUT = "RCPTT: Launching AUT: ";

	private static final String SECURE_STORAGE_FILE_NAME = "secure_storage";

	private ILaunch launch;

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		this.launch = launch;
		SubMonitor subm = SubMonitor.convert(monitor, 2000);
		Q7ExtLaunchMonitor waiter = new Q7ExtLaunchMonitor(launch);

		try {
			super.launch(configuration, mode, launch, subm.split(1000));
			waiter.wait(subm.split(1000), TeslaLimits.getAUTStartupTimeout() / 1000);
		} catch (CoreException e) {
			Q7ExtLaunchingPlugin.getDefault().log(
					"RCPTT: Failed to Launch AUT: " + configuration.getName()
							+ " cause " + e.getMessage(),
					e);
			waiter.handle(e);
			// no need to throw exception in case of cancel
			if (!e.getStatus().matches(IStatus.CANCEL)) {
				throw e;
			}
		} catch (OperationCanceledException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (RuntimeException e) {
			Q7ExtLaunchingPlugin.getDefault().log(
					"RCPTT: Failed to Launch AUT: " + configuration.getName()
							+ " cause " + e.getMessage(),
					e);
			waiter.handle(e);
			throw e;
		} finally {
			waiter.dispose();
		}
		monitor.done();
	}

	@Override
	protected void manageLaunch(ILaunch launch) {
		// remove base PDE launch management
	}

	@Override
	protected boolean saveBeforeLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		if (isHeadless(configuration)) {
			return true;
		}
		return super.saveBeforeLaunch(configuration, mode, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		if (isHeadless(configuration)) {
			return true;
		}
		return super.finalLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return false;
		}
		
		SubMonitor sm = SubMonitor.convert(monitor, 4);
		
		if (!isHeadless(configuration)
				&& !super.preLaunchCheck(configuration, mode,
						sm.split(1))) {
			monitor.done();
			return false;
		}

		waitForClearBundlePool(sm.split(1));

		final CachedInfo info = LaunchInfoCache.getInfo(configuration);

		if (info.target != null) {
			monitor.done();
			return true;
		}

		final ITargetPlatformHelper target = Q7TargetPlatformManager.getTarget(configuration,
				sm.split(2));

		if (monitor.isCanceled()) {
			removeTargetPlatform(configuration);
			return false;
		}

		info.target = target;
		final MultiStatus error = new MultiStatus(Q7ExtLaunchingPlugin.PLUGIN_ID, 0,
				"Target platform initialization failed  for "
						+ configuration.getName(),
				null);
		error.add(target.getStatus());

		if (!error.isOK()) {
			if (monitor.isCanceled()) {
				removeTargetPlatform(configuration);
				return false;
			}
			Q7ExtLaunchingPlugin.log(error);
			removeTargetPlatform(configuration);
			throw new CoreException(error);
		}

		boolean haveAUT = false;

		OSArchitecture configArch = null;
		StringBuilder detectMsg = new StringBuilder();

		OSArchitecture architecture = ((configArch == null) ? ((ITargetPlatformHelper) info.target)
				.detectArchitecture(true, detectMsg) : configArch);

		Q7ExtLaunchingPlugin.getDefault().info(
				Q7_LAUNCHING_AUT + configuration.getName()
						+ ": Detected AUT architecture is "
						+ architecture.name() + "." + detectMsg.toString());

		IVMInstall install = VMHelper.getVMInstall(configuration);

		OSArchitecture jvmArch = JDTUtils.detect(install);

		Q7ExtLaunchingPlugin.getDefault().info(
				Q7_LAUNCHING_AUT + configuration.getName()
						+ ": Selected JVM is "
						+ install.getInstallLocation().toString()
						+ " detected architecture is " + jvmArch.name());

		boolean canRun32bit = false;
		if (jvmArch.equals(architecture)
				|| (jvmArch.equals(OSArchitecture.x86_64) && (canRun32bit = JDTUtils
						.canRun32bit(install)))) {
			haveAUT = true;
		}

		if (!haveAUT
				&& architecture != OSArchitecture.Unknown
				&& target.detectArchitecture(false, new StringBuilder()) == OSArchitecture.Unknown) {
			Q7ExtLaunchingPlugin
					.getDefault()
					.info("Cannot determine AUT architecture, sticking to architecture of selected JVM, which is "
							+ jvmArch.name());
			haveAUT = true;
		}

		Q7ExtLaunchingPlugin
				.getDefault()
				.info(Q7_LAUNCHING_AUT
						+ configuration.getName()
						+ ": JVM and AUT architectures are compatible: "
						+ haveAUT
						+ "."
						+ (jvmArch.equals(OSArchitecture.x86_64) ? " JVM is 64bit and support running 32bit: "
								+ canRun32bit
								: ""));
		if (!haveAUT) {
			// Let's search for configuration and update JVM if possible.
			haveAUT = updateJVM(configuration, architecture,
					((ITargetPlatformHelper) info.target));

			if (!haveAUT) {
				// try to register current JVM, it may help
				JDTUtils.registerCurrentJVM();
				haveAUT = updateJVM(configuration, architecture,
						((ITargetPlatformHelper) info.target));
			}

			if (haveAUT) {
				Q7ExtLaunchingPlugin
						.getDefault()
						.info(Q7_LAUNCHING_AUT
								+ configuration.getName()
								+ "JVM configuration is updated to compatible one: "
								+ VMHelper.getVMInstall(configuration)
										.getInstallLocation());
			}

		}
		if (!haveAUT) {
			String errorMessage = "The "
					+ configuration.getName()
					+ " requires "
					+ ((OSArchitecture.x86.equals(architecture)) ? "32 bit"
							: "64 bit")
					+ " Java VM which cannot be found.";
			Q7ExtLaunchingPlugin.getDefault().log(errorMessage, null);
			removeTargetPlatform(configuration);
			throw new CoreException(new Status(IStatus.ERROR,
					Q7ExtLaunchingPlugin.PLUGIN_ID, errorMessage, null));
		}

		monitor.done();
		return true;
	}

	private void removeTargetPlatform(ILaunchConfiguration configuration)
			throws CoreException {
		String targetPlatformName = Q7TargetPlatformManager.getTargetPlatformName(configuration);
		Q7TargetPlatformManager.delete(targetPlatformName);
		LaunchInfoCache.remove(configuration);
		TargetPlatformManager.deleteTargetPlatform(targetPlatformName);
	}

	private static boolean isHeadless(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration
				.getAttribute(IQ7Launch.ATTR_HEADLESS_LAUNCH, false);
	}

	private void waitForClearBundlePool(IProgressMonitor monitor) {
		try {
			Job.getJobManager().join(
					IBundlePoolConstansts.CLEAN_BUNDLE_POOL_JOB,
					monitor);
		} catch (Exception e1) {
			Q7ExtLaunchingPlugin.getDefault().log(
					"Failed to wait for bundle pool clear job", e1);
		}
	}

	private static boolean updateJVM(ILaunchConfiguration configuration,
			OSArchitecture architecture, ITargetPlatformHelper target) throws CoreException {
		
		VmInstallMetaData jvm = JDTUtils.findVM(architecture);
		if (jvm == null) {
			return false;
		}
		
		OSArchitecture jvmArch = jvm.arch;
		IVMInstall jvmInstall = jvm.install;
		
		ILaunchConfigurationWorkingCopy workingCopy = configuration
				.getWorkingCopy();

		String vmArgs = workingCopy.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
				Q7LaunchDelegateUtils.getJoinedVMArgs(target, null));

		OSArchitecture configArch;
		String archAttrValue = configuration.getAttribute(
				Q7LaunchingCommon.ATTR_ARCH, "");
		if (archAttrValue.isEmpty())
			configArch = null;
		else
			configArch = OSArchitecture.valueOf(archAttrValue);

		OSArchitecture autArch = configArch == null ? target
				.detectArchitecture(true, null) : configArch;

		// there is no -d32 on Windows
		if (!autArch.equals(jvmArch)
				&& Platform.getOS().equals(Platform.OS_MACOSX)) {
			if (vmArgs != null && !vmArgs.contains(ATTR_D32)) {
				vmArgs += " " + ATTR_D32;
			} else {
				vmArgs = ATTR_D32;
			}
		}
		if (vmArgs != null && vmArgs.length() > 0) {
			vmArgs = UpdateVMArgs.updateAttr(vmArgs);
			workingCopy
					.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
							vmArgs);
		}

		workingCopy
				.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
						String.format(
								"org.eclipse.jdt.launching.JRE_CONTAINER/%s/%s",
								jvmInstall.getVMInstallType().getId(),
								jvmInstall.getName()));

		String programArgs = workingCopy
				.getAttribute(
						IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
						LaunchArgumentsHelper
								.getInitialProgramArguments().trim());
		if (programArgs.contains("${target.arch}")) {
			programArgs = programArgs.replace("${target.arch}",
					autArch.name());
		} else {
			if (programArgs.contains("-arch")) {
				int pos = programArgs.indexOf("-arch ") + 6;
				int len = 6;
				int pos2 = programArgs.indexOf("x86_64", pos);
				if (pos2 == -1) {
					len = 3;
					pos2 = programArgs.indexOf("x86", pos);
				}
				if (pos2 != -1) {
					programArgs = programArgs.substring(0, pos)
							+ autArch.name()
							+ programArgs.substring(pos2 + len,
									programArgs.length());
				}
			} else {
				programArgs = programArgs + " -arch " + autArch.name();
			}
		}
		if (programArgs.length() > 0) {
			workingCopy
					.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
							programArgs);
		}
		workingCopy.doSave();
		return true;
	}

	private static String getSubstitutedString(String text)
			throws CoreException {
		if (text == null)
			return ""; //$NON-NLS-1$
		IStringVariableManager mgr = VariablesPlugin.getDefault()
				.getStringVariableManager();
		return mgr.performStringSubstitution(text);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String[] constructClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		CachedInfo info = LaunchInfoCache.getInfo(configuration);
		ITargetPlatformHelper target = (ITargetPlatformHelper) info.target;

		String jarPath = target
				.getEquinoxStartupPath(IPDEBuildConstants.BUNDLE_EQUINOX_LAUNCHER);

		if (jarPath == null)
			return null;

		ArrayList entries = new ArrayList();
		entries.add(jarPath);

		String bootstrap = configuration.getAttribute(
				IPDELauncherConstants.BOOTSTRAP_ENTRIES, ""); //$NON-NLS-1$
		StringTokenizer tok = new StringTokenizer(
				getSubstitutedString(bootstrap), ","); //$NON-NLS-1$
		while (tok.hasMoreTokens())
			entries.add(tok.nextToken().trim());
		return (String[]) entries.toArray(new String[entries.size()]);

	}

	@Override
	public String[] getClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		String[] classpath = constructClasspath(configuration);
		if (classpath == null) {
			String message = PDEMessages.WorkbenchLauncherConfigurationDelegate_noStartup;
			throw new CoreException(Q7ExtLaunchingPlugin.status(message));
		}
		return classpath;
	}

	@Override
	public String[] getProgramArguments(ILaunchConfiguration configuration)
			throws CoreException {
		CachedInfo info = LaunchInfoCache.getInfo(configuration);
		final ITargetPlatformHelper target = (ITargetPlatformHelper) info.target;
		if (info.programArgs != null) {
			return info.programArgs;
		}

		ArrayList<String> programArgs = new ArrayList<String>();

		programArgs.addAll(asList(super.getProgramArguments(configuration)));

		try {
			// Correct osgi.install.area property key
			File config = new File(getConfigDir(configuration), "config.ini");
			Properties props = new Properties();

			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(config));
			props.load(in);
			in.close();

			File location = target.getQ7Target().getInstallLocation();
			if (location != null) {
				props.setProperty("osgi.install.area",
						location.getAbsolutePath());
			}
			props.setProperty(
					"osgi.bundles",
					Q7LaunchDelegateUtils
							.computeOSGiBundles(getBundlesToLaunch(info).latestVersionsOnly));
			props.remove("osgi.framework");

			// Append all other properties from original config file
			OriginalOrderProperties properties = target.getConfigIniProperties();

			properties.setBeginAdd(true);
			properties.putAll(props);

			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(config));
			properties.store(out, "Configuration File");
			out.close();
		} catch (IOException e) {
			throw new CoreException(Q7ExtLaunchingPlugin.status(e));
		}
		if ( configuration.getAttribute(
				IQ7Launch.OVERRIDE_SECURE_STORAGE, true)) {
			// Override existing parameter
			programArgs.add("-eclipse.keyring");
			programArgs.add(getConfigDir(configuration).toString()
					+ IPath.SEPARATOR + SECURE_STORAGE_FILE_NAME);
		}

		IVMInstall install = VMHelper.getVMInstall(configuration);
		programArgs.add("-vm");
		programArgs.add(install.getInstallLocation().toString());

		info.programArgs = programArgs.toArray(new String[programArgs.size()]);
		Q7ExtLaunchingPlugin.getDefault().info(
				Q7_LAUNCHING_AUT + configuration.getName()
						+ ": AUT command line arguments is set to: "
						+ Arrays.toString(info.programArgs));
		return info.programArgs;
	}

	@Override
	public String[] getVMArguments(ILaunchConfiguration config)
			throws CoreException {
		CachedInfo info = LaunchInfoCache.getInfo(config);
		if (info.vmArgs != null) {
			return info.vmArgs;
		}
		List<String> args = new ArrayList<String>(Arrays.asList(super.getVMArguments(config)));
		ITargetPlatformHelper target = (ITargetPlatformHelper) info.target;

		massageVmArguments(config, args, target, launch.getAttribute(IQ7Launch.ATTR_AUT_ID));
		

		info.vmArgs = args.toArray(new String[args.size()]);
		Q7ExtLaunchingPlugin.getDefault().info(
				Q7_LAUNCHING_AUT + config.getName()
						+ ": AUT JVM arguments is set to : "
						+ Arrays.toString(info.vmArgs));
		return info.vmArgs;
	}

	public static void massageVmArguments(ILaunchConfiguration config, List<String> args, ITargetPlatformHelper target, String autId)
			throws CoreException {
		// Filter some PDE parameters
		Iterables.removeIf(args, new Predicate<String>() {
			public boolean apply(String input) {
				if (input.contains("-Declipse.pde.launch=true")) {
					return true;
				}
				return false;
			}
		});

		args.add("-Dq7id=" + autId);
		args.add("-Dq7EclPort=" + AutEventManager.INSTANCE.getPort());


		IPluginModelBase hook = target.getWeavingHook();
		if (hook == null) {
			throw new CoreException(Q7ExtLaunchingPlugin.status("No "
					+ AJConstants.HOOK + " plugin"));
		}

		// Append all other properties from original config file
		OriginalOrderProperties properties = target.getConfigIniProperties();

		ArrayList<String> argsCopy = new ArrayList<>(args);
		args.clear();
		args.addAll(UpdateVMArgs.addHook(argsCopy, hook, properties.getProperty(OSGI_FRAMEWORK_EXTENSIONS)));

		args.addAll(vmSecurityArguments(config));
		
		args.add("-Declipse.vmargs=" + Joiner.on("\n").join(args) + "\n");
	}
	
	public static List<String> vmSecurityArguments(ILaunchConfiguration configuration) throws CoreException {
		// Magic constant from org.eclipse.jdt.internal.launching.environments.ExecutionEnvironmentAnalyzer
		ArrayList<String> result = new ArrayList<>();
		Set<String> envs = getMatchingEnvironments(configuration);
		if (envs.contains("JavaSE-11")) {
			result.addAll(Arrays.asList(
					"--add-opens", "java.base/java.lang=ALL-UNNAMED",
					"--add-modules=ALL-SYSTEM"));
			if (!envs.contains("JavaSE-17")) {
				result.add("--illegal-access=permit");
			}
		}
		if (envs.contains("JavaSE-12") && !envs.contains("JavaSE-17")) {
			result.add("-Djava.security.manager=allow");
		}
		
		return result;
	}
	
	private static Set<String> getMatchingEnvironments(ILaunchConfiguration configuration) throws CoreException {
		IVMInstall install = VMHelper.getVMInstall(configuration);
		if (install == null)
			return Collections.emptySet();

		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		return Arrays.stream(manager.getExecutionEnvironments())
				.filter(env -> Arrays.stream(env.getCompatibleVMs()).anyMatch(install::equals))
				.map(IExecutionEnvironment::getId)
				.collect(Collectors.toSet());
	}

	@Override
	protected void preLaunchCheck(final ILaunchConfiguration configuration,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		SubMonitor subm = SubMonitor.convert(monitor, 100);
		super.preLaunchCheck(configuration, launch, subm.newChild(50));
		if (monitor.isCanceled()) {
			return;
		}

		CachedInfo info = LaunchInfoCache.getInfo(configuration);
		ITargetPlatformHelper target = (ITargetPlatformHelper) info.target;

		BundlesToLaunch bundlesToLaunch = collectBundlesCheck(target.getQ7Target(), target.getTarget(), subm.split(50), configuration);

		setBundlesToLaunch(info, bundlesToLaunch);

		removeDuplicatedModels(bundlesToLaunch.fModels, target.getQ7Target());

		setDelegateFields(this, bundlesToLaunch.fModels, bundlesToLaunch.fAllBundles);

		// Copy all additional configuration area folders into PDE new
		// configuration location.
		copyConfiguratonFiles(configuration, info);
		monitor.done();
	}

	public static void removeDuplicatedModels(Map<IPluginModelBase, String> fModels, Q7Target target) {

		String path = target.getInstallLocation().getAbsolutePath();
		List<IPluginModelBase> keysForRemove = new ArrayList<IPluginModelBase>();
		Map<UniquePluginModel, IPluginModelBase> cache = new HashMap<UniquePluginModel, IPluginModelBase>();

		for (Entry<IPluginModelBase, String> entry : fModels.entrySet()) {
			IPluginModelBase model = entry.getKey();
			UniquePluginModel uniqueModel = new UniquePluginModel(model);
			IPluginModelBase secondModel = cache.get(uniqueModel);
			if (secondModel != null) {
				if (secondModel.getInstallLocation().contains(path)) {
					keysForRemove.add(secondModel);
				} else {
					keysForRemove.add(model);
				}
			} else {
				cache.put(uniqueModel, model);
			}
		}
		for (IPluginModelBase key : keysForRemove) {
			fModels.remove(key);
		}
	}

	public static class BundlesToLaunchCollector {
		private void addInstallationBundle(TargetBundle bundle,
				BundleStart hint, IProgressMonitor monitor) {
			for (IPluginModelBase base : getModels(bundle, monitor)) {
				addInstallationBundle(base, hint);
			}
		}

		public void addInstallationBundle(IPluginModelBase base,
				BundleStart hint) {
			String id = id(base);
			idsToSkip.add(id);
			put(base, getStartInfo(base, hint));
		}

		private void addPluginBundle(TargetBundle bundle, IProgressMonitor monitor) {
			for (IPluginModelBase base : getModels(bundle, monitor)) {
				String id = id(base);

				if (idsToSkip.contains(id)) {
					continue;
				}
				put(base, getStartInfo(base, BundleStart.DEFAULT));
			}
		}

		public void addExtraBundle(TargetBundle bundle, IProgressMonitor monitor) {
			for (IPluginModelBase base : getModels(bundle, monitor)) {
				put(base, getStartInfo(base, BundleStart.DEFAULT));
			}
		}

		public BundlesToLaunch getResult() {
			return new BundlesToLaunch(rejectedBundles, plugins, latestVersions);
		}

		private void put(IPluginModelBase plugin, BundleStart start) {

			/**
			 * Check for aspectj special plugins, they should be one version
			 * only.
			 **/
			final String id = id(plugin);
			if (id.equals(AJConstants.AJ_HOOK) || id.equals(AJConstants.AJ_RT)
					|| id.equals(AJConstants.HOOK)) {
				// remove previous bundles with specified id
				Iterables.removeIf(plugins.entrySet(), isEqualsId(id));
				latestVersions.remove(id);
			}

			plugins.put(plugin, start);
			IPluginModelBase existing = latestVersions.get(id);

			if (existing == null
					|| isGreater(version(plugin), version(existing))) {
				latestVersions.put(id, plugin);
			}
		}

		private Predicate<Entry<IPluginModelBase, BundleStart>> isEqualsId(
				final String id) {
			return new Predicate<Map.Entry<IPluginModelBase, BundleStart>>() {
				public boolean apply(Entry<IPluginModelBase, BundleStart> input) {
					if (id.equals(id(input.getKey()))) {
						return true;
					}
					return false;
				}
			};
		}

		private final Set<String> idsToSkip = new HashSet<String>();
		private final Set<String> rejectedBundles = new LinkedHashSet<String>();
		private final Map<IPluginModelBase, BundleStart> plugins = new HashMap<IPluginModelBase, BundleStart>();
		private final Map<String, IPluginModelBase> latestVersions = new HashMap<String, IPluginModelBase>();
	}

	public static boolean isQ7BundleContainer(ITargetLocation container) {
		if (!(container instanceof IUBundleContainer))
			return false;
		for (URI uri : ((IUBundleContainer) container).getRepositories()) {
			if (!uri.getScheme().equals("platform")
					|| !uri.getPath().startsWith("/plugin/org.eclipse.rcptt")) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAutConfigSimpleconfiguratorSet(Q7Target target) {
		return target.getInstall().configIniBundles().containsKey(TargetPlatformHelper.SIMPLECONFIGURATOR);
	}

	public static void setBundlesLevels(Q7Target target, Map<String, String> levelMap) {
		final ProfileBundleContainer profileBundleContainer = target.getInstall().container;
		if (profileBundleContainer != null) {
			for (TargetBundle bundle : target.getInstall().getBundles()) {
				String bundleLevels = levelMap.get(bundle.getBundleInfo().getSymbolicName());
				if (bundleLevels != null) {
					try {
						String[] strings = bundleLevels.split(":");
						int level = Integer.parseInt(strings[0]);
						boolean started = Boolean.parseBoolean(strings[1]);
						bundle.getBundleInfo().setStartLevel(level);
						bundle.getBundleInfo().setMarkedAsStarted(started);
					} catch (Exception e) {
						// ignore parsing exception
					}
				}
			}
		} else {
			log(status("Profile Bundle Container is EMPTY.")); //$NON-NLS-1$
		}

	}

	public static BundlesToLaunch collectBundlesCheck(Q7Target target, ITargetDefinition targetDefinition, IProgressMonitor monitor,
			ILaunchConfiguration configuration) {
		if (target.getInstall() != null && isAutConfigSimpleconfiguratorSet(target)) {
			final CachedInfo info = LaunchInfoCache.getInfo(configuration);
			TargetPlatformHelper targetHelper = (TargetPlatformHelper) ((ITargetPlatformHelper) info.target);
			Map<String, String> levelMap = targetHelper.getRunlevelsMap();
			setBundlesLevels(target, levelMap);
		}

		return collectBundles(target, targetDefinition, monitor);
	}

	public static BundlesToLaunch collectBundles(Q7Target target, ITargetDefinition targetDefinition, IProgressMonitor monitor) {
		BundlesToLaunchCollector collector = new BundlesToLaunchCollector();
		SubMonitor subm = SubMonitor.convert(monitor, "Collecting bundles", 3000);
		SubMonitor install = subm.split(1000);

		if (target.getInstall() != null) {
			Map<String, BundleStart> bundlesFromConfig = target.getInstall().configIniBundles();
			TargetBundle[] installationBundles = target.getInstall().getBundles();
			install.beginTask("Scanning " + target.getInstall().getInstallLocation(), installationBundles.length);
			for (TargetBundle bundle : target.getInstall().getBundles()) {
				BundleStart hint = MoreObjects.firstNonNull(bundlesFromConfig.get(bundle
						.getBundleInfo().getSymbolicName()),
						BundleStart.fromBundle(bundle.getBundleInfo()));
				collector.addInstallationBundle(bundle, hint, install.split(1));
			}
		}
		install.done();

		TargetBundle[] bundles = targetDefinition.getAllBundles();
		final SubMonitor plugins = SubMonitor.convert(subm.split(2000), bundles.length);
		for (TargetBundle bundle : bundles) {
			collector.addPluginBundle(bundle, plugins.split(1));
		}
		plugins.done();

		try {
			return new BundlesToLaunch(collector.rejectedBundles,
				collector.plugins, collector.latestVersions);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Represents result of collection of bundles to launch
	 *
	 * @author ivaninozemtsev
	 *
	 */
	public static class BundlesToLaunch {
		public BundlesToLaunch(Set<String> rejectedBundles,
				Map<IPluginModelBase, BundleStart> plugins,
				final Map<String, IPluginModelBase> latestVersions) {
			this.resolvedBundles = plugins;

			this.latestVersionsOnly = Maps.filterKeys(resolvedBundles,
					new Predicate<IPluginModelBase>() {
						public boolean apply(IPluginModelBase input) {
							// II: reference equality intentionally
							return latestVersions.get(id(input)) == input;
						}
					});

			ListMultimap<String, IPluginModelBase> multiMap = Multimaps.newListMultimap(new HashMap<String, Collection<IPluginModelBase>>(), ArrayList::new);
			for (IPluginModelBase plugin: plugins.keySet()) {
				multiMap.put(id(plugin), plugin);
			}
			fAllBundles = Maps.transformValues(multiMap.asMap(), ArrayList::new);
			fModels = new HashMap<IPluginModelBase, String>(Maps.transformValues(
					resolvedBundles, new Function<BundleStart, String>() {
						public String apply(BundleStart input) {
							return input.toModelString();
						}
					}));
		}

		public final Map<IPluginModelBase, BundleStart> resolvedBundles;
		public final Map<IPluginModelBase, BundleStart> latestVersionsOnly;
		public final Map<IPluginModelBase, String> fModels;
		public final Map<String, List<IPluginModelBase>> fAllBundles;
	}

	private static final String KEY_BUNDLES_TO_LAUNCH = "bundlesToLaunch";

	public static void setBundlesToLaunch(CachedInfo info,
			BundlesToLaunch bundles) {
		info.data.put(KEY_BUNDLES_TO_LAUNCH, bundles);
	}

	public static BundlesToLaunch getBundlesToLaunch(CachedInfo info) {
		return (BundlesToLaunch) info.data.get(KEY_BUNDLES_TO_LAUNCH);
	}

	private static IPluginModelBase[] getModels(TargetBundle bundle, IProgressMonitor monitor) {
		return new PDEState(new URI[] {
			bundle.getBundleInfo().getLocation() }, true, true,
				monitor).getTargetModels();
	}

	private static Version version(IPluginModelBase plugin) {
		try {
			return new Version(plugin.getPluginBase().getVersion());
		} catch (IllegalArgumentException e) {
			return Version.emptyVersion;
		}
	}

	private void copyConfiguratonFiles(
			final ILaunchConfiguration configuration, CachedInfo info) throws CoreException {
		String targetPlatformPath = ((ITargetPlatformHelper) info.target).getUserArea();
		if(targetPlatformPath == null)
			targetPlatformPath = ((ITargetPlatformHelper) info.target)
					.getTargetPlatformProfilePath();
		File configFolder = new File(targetPlatformPath, "configuration"); //$NON-NLS-1$
		if (!configFolder.exists())
			return;

		Set<String> filter = new HashSet<String>(Arrays.asList(new String(
				".p2;" + //
						"org.eclipse.core.runtime;" //
						+ "org.eclipse.equinox.app;" //
						+ "org.eclipse.equinox.simpleconfigurator;" //
						+ "org.eclipse.equinox.source;" //
						+ "org.eclipse.osgi;" //
						+ "org.eclipse.ui.intro.universal;" //
						+ "org.eclipse.update;" //
						+ "config.ini;" //
						+ ".settings;" //
						+ "org.eclipse.help.base"//
		).split(";")));
		File target = getConfigDir(configuration);
		File[] listFiles = configFolder.listFiles();
		for (File file : listFiles) {
			if (!filter.contains(file.getName())) {
				if (file.isDirectory()) {
					FileUtil.copyFiles(file, new File(target, file.getName()));
				} else {
					FileUtil.copyFiles(file, target);
				}
			}
		}
	}

	public static class UniquePluginModel {

		private String name;
		private Version version;

		public UniquePluginModel(IPluginModelBase model) {
			version = model.getBundleDescription().getVersion();
			name = model.getBundleDescription().getName();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int hashCode = 1;
			hashCode = prime * hashCode + (name == null ? 0 : name.hashCode());
			hashCode = prime * hashCode + (version == null ? 0 : version.hashCode());
			return 0;
		}

		@Override
		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (object == this)
				return true;
			if (!(object instanceof UniquePluginModel))
				return false;
			UniquePluginModel uniquePluginModel = (UniquePluginModel) object;
			if (uniquePluginModel.name.equals(this.name)
					&& uniquePluginModel.version.equals(this.version)) {
				return true;
			}
			return false;
		}
	}
}
