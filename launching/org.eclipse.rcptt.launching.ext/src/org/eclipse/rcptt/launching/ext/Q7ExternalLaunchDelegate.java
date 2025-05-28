/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
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
import static org.eclipse.core.runtime.IProgressMonitor.done;
import static org.eclipse.rcptt.internal.launching.ext.AJConstants.OSGI_FRAMEWORK_EXTENSIONS;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.log;
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
import java.util.List;
import java.util.Map;
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
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
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
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper.Model;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.util.FileUtil;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
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
		
		SubMonitor sm = SubMonitor.convert(monitor, 100);
		
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

		final ITargetPlatformHelper target = Q7TargetPlatformManager.findTarget(configuration,
				sm.split(1));
		if (target == null) {
			throw new CoreException(Status.error("RCPTT has been updated since AUT " + configuration.getName() + " was created. Edit the AUT to restore compatibility."));
		}
		

		if (monitor.isCanceled()) {
			return false;
		}

		info.target = target;
		final MultiStatus error = new MultiStatus(Q7ExtLaunchingPlugin.PLUGIN_ID, 0,
				"Target platform initialization failed  for "
						+ configuration.getName() + " edit the AUT to retry",
				null);
		error.add(target.resolve(sm.split(98)));

		if (!error.isOK()) {
			Q7ExtLaunchingPlugin.log(error);
		}

		if (error.matches(IStatus.ERROR)) {
			if (monitor.isCanceled()) {
				removeTargetPlatform(configuration);
				return false;
			}
			removeTargetPlatform(configuration);
			throw new CoreException(error);
		}

		boolean jvmFound = false;

		OSArchitecture configArch = null;
		StringBuilder detectMsg = new StringBuilder();

		OSArchitecture architecture = ((configArch == null) ? ((ITargetPlatformHelper) info.target)
				.detectArchitecture(detectMsg) : configArch);

		Q7ExtLaunchingPlugin.getDefault().info(
				Q7_LAUNCHING_AUT + configuration.getName()
						+ ": Detected AUT architecture is "
						+ architecture.name() + ". " + detectMsg.toString());

		IVMInstall install = getVMInstall(configuration, target);

		OSArchitecture jvmArch = JDTUtils.detect(install);

		Q7ExtLaunchingPlugin.getDefault().info(
				Q7_LAUNCHING_AUT + configuration.getName()
						+ ": Selected JVM is "
						+ install.getInstallLocation().toString()
						+ " detected architecture is " + jvmArch.name());

		if (jvmArch.equals(architecture)) {
			jvmFound = true;
		}

		if (!jvmFound
				&& architecture != OSArchitecture.Unknown
				&& target.detectArchitecture(new StringBuilder()) == OSArchitecture.Unknown) {
			Q7ExtLaunchingPlugin
					.getDefault()
					.info("Cannot determine AUT architecture, sticking to architecture of selected JVM, which is "
							+ jvmArch.name());
			jvmFound = true;
		}

		Q7ExtLaunchingPlugin
				.getDefault()
				.info(Q7_LAUNCHING_AUT
						+ configuration.getName()
						+ ": JVM and AUT architectures are compatible: "
						+ jvmFound
						+ ".");
		if (!jvmFound) {
			// Let's search for configuration and update JVM if possible.
			ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
			jvmFound = updateJVM(workingCopy, architecture,  ((ITargetPlatformHelper) info.target));

			if (jvmFound) {
				workingCopy.doSave();
				Q7ExtLaunchingPlugin
						.getDefault()
						.info(Q7_LAUNCHING_AUT
								+ configuration.getName()
								+ "JVM configuration is updated to compatible one: "
								+ getVMInstall(configuration, target)
										.getInstallLocation());
			}

		}
		if (!jvmFound) {
			String errorMessage = String.format("Select a compatible Runtime JRE. Architecture: %s, incompatible with: ", architecture, target.getIncompatibleExecutionEnvironments());
			Q7ExtLaunchingPlugin.getDefault().log(errorMessage, null);
			removeTargetPlatform(configuration);
			throw new CoreException(new Status(IStatus.ERROR,
					Q7ExtLaunchingPlugin.PLUGIN_ID, errorMessage, null));
		}

		monitor.done();
		return true;
	}

	public static IVMInstall getVMInstall(ILaunchConfiguration configuration, ITargetPlatformHelper target) throws CoreException {
		return VMHelper.getVMInstall(configuration, target.getModels().map(m -> m.model()).collect(Collectors.toSet()));
	}

	private void removeTargetPlatform(ILaunchConfiguration configuration)
			throws CoreException {
		Q7TargetPlatformManager.delete(configuration);
		LaunchInfoCache.remove(configuration);
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
					SubMonitor.convert(monitor, 1));
		} catch (Exception e1) {
			Q7ExtLaunchingPlugin.getDefault().log(
					"Failed to wait for bundle pool clear job", e1);
		}
	}

	public static boolean updateJVM(ILaunchConfigurationWorkingCopy workingCopy,
			OSArchitecture architecture, ITargetPlatformHelper target) throws CoreException {
		
		VmInstallMetaData jvm = VmInstallMetaData.all().filter(m -> isCompatible(m, architecture, target.getIncompatibleExecutionEnvironments())).findFirst().orElse(null);
		if (jvm == null) {
			return false;
		}
		IVMInstall jvmInstall = jvm.install;
		workingCopy
				.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
						String.format(
								"org.eclipse.jdt.launching.JRE_CONTAINER/%s/%s",
								jvmInstall.getVMInstallType().getId(),
								jvmInstall.getName()));
		return true;
	}

	private static boolean isCompatible(VmInstallMetaData m, OSArchitecture architecture, Set<String> incompatibleExecutionEnvironments) {
		return m.arch.equals(architecture) && Collections.disjoint(incompatibleExecutionEnvironments, m.compatibleEnvironments);
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

			try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(config))) {
				props.load(in);
			}

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

			try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(config))) {
				properties.store(out, "Configuration File");
			}
			// Workaround for https://github.com/eclipse-oomph/oomph/issues/152
			// Create a configured directory so that Oomph can ensure it is writable and use it and not fall back to default 
			new File(config.getParent(), ".p2").mkdirs();
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

		IVMInstall install = getVMInstall(configuration, target);
		programArgs.add("-vm");
		programArgs.add(install.getInstallLocation().toString());
		
		// org.eclipse.pde.launching.AbstractPDELaunchConfiguration.getProgramArguments(ILaunchConfiguration) uses incorrect architecture from TargetPlatform.getOSArch()
		removeKey(programArgs, "-arch");
		// Development mode does not support restart 
		// @see org.eclipse.ui.internal.Workbench.setRestartArguments(String)
		// @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/177
		removeKey(programArgs, "-dev");

		info.programArgs = programArgs.toArray(new String[programArgs.size()]);
		Q7ExtLaunchingPlugin.getDefault().info(
				Q7_LAUNCHING_AUT + configuration.getName()
						+ ": AUT command line arguments is set to: "
						+ Arrays.toString(info.programArgs));
		return info.programArgs;
	}

	private void removeKey(ArrayList<String> programArgs, String key) {
		for (int archIndex = programArgs.indexOf(key); archIndex >= 0;  archIndex = programArgs.indexOf(key)) {
			programArgs.remove(archIndex);
			programArgs.remove(archIndex);
		}
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

		args.addAll(vmSecurityArguments(config, target));
		
		ArrayList<String> copy = new ArrayList<>(args);
		args.clear();
		args.addAll(UpdateVMArgs.updateAttr(copy));
		
		args.add("-Declipse.vmargs=" + Joiner.on("\n").join(args) + "\n");
	}
	
	public static List<String> vmSecurityArguments(ILaunchConfiguration configuration, ITargetPlatformHelper target) throws CoreException {
		// Magic constant from org.eclipse.jdt.internal.launching.environments.ExecutionEnvironmentAnalyzer
		ArrayList<String> result = new ArrayList<>();
		Set<String> envs = getMatchingEnvironments(configuration, target);
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
	
	private static Set<String> getMatchingEnvironments(ILaunchConfiguration configuration, ITargetPlatformHelper target) throws CoreException {
		IVMInstall install = getVMInstall(configuration, target);
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

		BundlesToLaunch bundlesToLaunch = collectBundles(target, subm.split(50));

		setBundlesToLaunch(info, bundlesToLaunch);

		removeUnresolved(bundlesToLaunch);
		setDelegateFields(this, bundlesToLaunch.fModels, Maps.transformValues(bundlesToLaunch.fAllBundles.asMap(), ArrayList::new));

		// Copy all additional configuration area folders into PDE new
		// configuration location.
		copyConfiguratonFiles(configuration, info);
		monitor.done();
	}
	
	public static void removeUnresolved(BundlesToLaunch launch) {
		DependencyResolver resolver = new DependencyResolver(launch.fAllBundles);
		Collection<IPluginModelBase> toDelete = resolver.checkPlugins(launch.fModels.keySet());
		toDelete.forEach(plugin->{
			launch.fAllBundles.remove(plugin.getBundleDescription().getName(), plugin);
			launch.fModels.remove(plugin);
		});
		String message  = "Following bundles were unresolved:\n" + 
		toDelete.stream().map(p -> p.getBundleDescription().getName()+"_"+p.getPluginBase().getVersion()).collect(Collectors.joining("\n"));
		log(Status.info(message));
	}

	public static class BundlesToLaunchCollector {

		public void addInstallationBundle(IPluginModelBase base,
				BundleStart hint) throws BundleException, IOException {
			put(base, getStartInfo(StartLevelSupport.loadManifest(base.getInstallLocation()), hint));
		}
		
		public void addPluginBundle(IPluginModelBase bundle, BundleStart startlevel) {
			put(bundle, startlevel);
		}

		public BundlesToLaunch getResult() {
			return new BundlesToLaunch(plugins, latestVersions);
		}

		private void put(IPluginModelBase plugin, BundleStart start) {
			final String id = id(plugin);
			if (!uniqueModels.add(new UniquePluginModel(plugin))) {
				return;
			}
			
			IPluginModelBase existing = latestVersions.get(id);
			boolean newer = existing == null || isGreater(version(plugin), version(existing)); 
			if (isSingleton(plugin)) {
				if (!newer) {
					return;
				}
				plugins.remove(existing);
			}

			plugins.put(plugin, start);
			if (newer) {
				latestVersions.put(id, plugin);
			}
		}
		
		private static boolean isSingleton(IPluginModelBase plugin) {
			/**
			 * Check for aspectj special plugins, they should be one version
			 * only.
			 **/
			final String id = id(plugin);
			return plugin.getBundleDescription().isSingleton() || id.equals(AJConstants.AJ_HOOK) || id.equals(AJConstants.AJ_RT)
					|| id.equals(AJConstants.HOOK);
		}
		
		private final Map<IPluginModelBase, BundleStart> plugins = new HashMap<>();
		private final Map<String, IPluginModelBase> latestVersions = new HashMap<>();
		private final Set<UniquePluginModel> uniqueModels = new HashSet<>();
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


	public static BundlesToLaunch collectBundles(ITargetPlatformHelper targetDefinition, IProgressMonitor monitor) {
		BundlesToLaunchCollector collector = new BundlesToLaunchCollector();
		SubMonitor subm = SubMonitor.convert(monitor, "Collecting bundles", targetDefinition.size());
		try {
			for (Model m: ((Iterable<ITargetPlatformHelper.Model>)(targetDefinition.getModels()::iterator))) {
				subm.split(1);
				collector.addPluginBundle(m.model(), m.startLevel());
			}
			return new BundlesToLaunch(collector.plugins, collector.latestVersions);
		} finally {
			done(monitor);
		}
	}

	/**
	 * Represents result of collection of bundles to launch
	 *
	 * @author ivaninozemtsev
	 *
	 */
	public static class BundlesToLaunch {
		public BundlesToLaunch(Map<IPluginModelBase, BundleStart> plugins,
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
			fAllBundles = multiMap;
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
		public final ListMultimap<String, IPluginModelBase> fAllBundles;
	}

	private static final String KEY_BUNDLES_TO_LAUNCH = "bundlesToLaunch";

	public static void setBundlesToLaunch(CachedInfo info,
			BundlesToLaunch bundles) {
		info.data.put(KEY_BUNDLES_TO_LAUNCH, bundles);
	}

	public static BundlesToLaunch getBundlesToLaunch(CachedInfo info) {
		return (BundlesToLaunch) info.data.get(KEY_BUNDLES_TO_LAUNCH);
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

		public UniquePluginModel(TargetBundle bundle) {
			version = Version.parseVersion(bundle.getBundleInfo().getVersion());
			name = bundle.getBundleInfo().getSymbolicName();
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
