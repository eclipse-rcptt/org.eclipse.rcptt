/*******************************************************************************
 * /*******************************************************************************
 *  * Copyright (c) 2009, 2019 Xored Software Inc and others.
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v2.0
 *  * which accompanies this distribution, and is available at
 *  * https://www.eclipse.org/legal/epl-v20.html
 *  *
 *  * Contributors:
 *  *     Xored Software Inc - initial API and implementation and/or initial documentation
 *  *******************************************************************************/
package org.eclipse.rcptt.launching.rap;

import static org.eclipse.rcptt.internal.launching.ext.AJConstants.OSGI_FRAMEWORK_EXTENSIONS;
import static org.eclipse.rcptt.launching.rap.Activator.PLUGIN_ID;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.launching.PDEMessages;
import org.eclipse.pde.internal.launching.launcher.BundleLauncherHelper;
import org.eclipse.pde.internal.launching.launcher.LaunchArgumentsHelper;
import org.eclipse.pde.internal.launching.launcher.LaunchConfigurationHelper;
import org.eclipse.pde.internal.launching.launcher.LauncherUtils;
import org.eclipse.pde.internal.launching.launcher.VMHelper;
import org.eclipse.pde.launching.EquinoxLaunchConfiguration;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache;
import org.eclipse.rcptt.internal.launching.aut.LaunchInfoCache.CachedInfo;
import org.eclipse.rcptt.internal.launching.ext.AJConstants;
import org.eclipse.rcptt.internal.launching.ext.IBundlePoolConstansts;
import org.eclipse.rcptt.internal.launching.ext.JDTUtils;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.internal.launching.ext.UpdateVMArgs;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.common.Q7LaunchingCommon;
import org.eclipse.rcptt.launching.events.AutEventManager;
import org.eclipse.rcptt.launching.ext.BundleStart;
import org.eclipse.rcptt.launching.ext.OriginalOrderProperties;
import org.eclipse.rcptt.launching.ext.Q7ExternalLaunchDelegate;
import org.eclipse.rcptt.launching.ext.Q7ExternalLaunchDelegate.BundlesToLaunch;
import org.eclipse.rcptt.launching.ext.Q7LaunchDelegateUtils;
import org.eclipse.rcptt.launching.internal.target.TargetPlatformHelper;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.util.FileUtil;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@SuppressWarnings("restriction")
public class RcpttRapLaunchDelegate extends EquinoxLaunchConfiguration {
	private static final String OSGI_BUNDLES = "osgi.bundles"; //$NON-NLS-1$
	// VM argument contants
	private static final String VMARG_PORT = "-Dorg.osgi.service.http.port="; //$NON-NLS-1$
	private static final String VMARG_DEVELOPMENT_MODE = "-Dorg.eclipse.rap.rwt.developmentMode="; //$NON-NLS-1$
	private static final String VMARG_SESSION_TIMEOUT = "-Dorg.eclipse.equinox.http.jetty.context.sessioninactiveinterval="; //$NON-NLS-1$
	private static final String VMARG_CONTEXT_PATH = "-Dorg.eclipse.equinox.http.jetty.context.path="; //$NON-NLS-1$
	private static final int CONNECT_TIMEOUT = 20000; // 20 Seconds
	static final String SLASH = "/"; //$NON-NLS-1$

	private BrowserLauncher browser;
	private ILaunch launch;
	private RAPLaunchConfig config;
	private int port;
	private final boolean testMode;

	public RcpttRapLaunchDelegate() {
		this.testMode = false;
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		SubMonitor submonitor = doPreLaunch(configuration, launch, monitor);
		submonitor = SubMonitor.convert(submonitor, 2000);
		Q7RapLaunchMonitor waiter = new Q7RapLaunchMonitor(launch);

		try {
			super.launch(configuration, mode, launch, submonitor.newChild(1000));
			waiter.wait(submonitor.newChild(1000), TeslaLimits.getAUTStartupTimeout() / 1000, new Runnable() {
				@Override
				public void run() {
					try {
						if (config.getOpenBrowser()) {
							registerBrowserOpener();
						}
					} catch (CoreException e) {
						throw new RuntimeException("Browser not open"); //$NON-NLS-1$
					}
				}
			});
		} catch (CoreException e) {
			Activator.getDefault().errorLog("RCPTT: Failed to launch RAP AUT: " + configuration.getName(), e); //$NON-NLS-1$
			waiter.handle(e);
			// no need to throw exception in case of cancel
			if (!e.getStatus().matches(IStatus.CANCEL)) {
				throw e;
			}
		} catch (RuntimeException e) {
			Activator.getDefault().errorLog("RCPTT: Failed to launch RAP AUT: " + configuration.getName(), e); //$NON-NLS-1$
			waiter.handle(e);
			throw e;
		} finally {

			waiter.dispose();
			submonitor.done();
			monitor.done();
		}
	}

	@Override
	protected void manageLaunch(ILaunch launch) {
		// remove base PDE laucher
	}

	public SubMonitor doPreLaunch(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		this.launch = launch;
		this.browser = new BrowserLauncher();
		this.config = new RAPLaunchConfig(config);

		SubMonitor subMonitor;
		subMonitor = SubMonitor.convert(monitor, IProgressMonitor.UNKNOWN);
		terminateIfRunning(subMonitor);
		subMonitor = SubMonitor.convert(monitor, IProgressMonitor.UNKNOWN);
		warnIfPortBusy(subMonitor);
		subMonitor = SubMonitor.convert(monitor, IProgressMonitor.UNKNOWN);
		port = determinePort(subMonitor);

		return subMonitor;
	}



	protected void doPreLaunchCkeck(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException
	{
		boolean autoValidate = configuration.getAttribute(IPDELauncherConstants.AUTOMATIC_VALIDATE, false);
		SubMonitor subMonitor = SubMonitor.convert(monitor, autoValidate ? 3 : 4);
		if (autoValidate) {
			validatePluginDependencies(configuration, subMonitor.split(1));
		}
		validateProjectDependencies(configuration, subMonitor.split(1));
		clear(configuration, subMonitor.split(1));
		launch.setAttribute(IPDELauncherConstants.CONFIG_LOCATION, getConfigDir(configuration).toString());
		synchronizeManifests(configuration, subMonitor.split(1));
	}

	@Override
	protected void preLaunchCheck(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor subm = SubMonitor.convert(monitor, 100);
		doPreLaunchCkeck(configuration, launch, subm.newChild(50));

		if (monitor.isCanceled()) {
			return;
		}

		CachedInfo info = LaunchInfoCache.getInfo(configuration);
		ITargetPlatformHelper target = (ITargetPlatformHelper) info.target;

		BundlesToLaunch bundlesToLaunch = Q7ExternalLaunchDelegate.collectBundlesCheck(target.getQ7Target(),
				target.getTarget(), subm.newChild(50), configuration);

		Q7ExternalLaunchDelegate.setBundlesToLaunch(info, bundlesToLaunch);

		Q7ExternalLaunchDelegate.removeDuplicatedModels(bundlesToLaunch.fModels, target.getQ7Target());

		setDelegateFields(this, bundlesToLaunch.fModels, bundlesToLaunch.fAllBundles);

		// Copy all additional configuration area folders into PDE new
		// configuration location.
		copyConfiguratonFiles(configuration, info);
		monitor.done();
	}

	private void copyConfiguratonFiles(
			final ILaunchConfiguration configuration, CachedInfo info) throws CoreException {
		String targetPlatformPath = ((ITargetPlatformHelper) info.target)
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

	public static void setDelegateFields(
			EquinoxLaunchConfiguration delegate,
			Map<IPluginModelBase, String> models, Map<String, List<IPluginModelBase>> allBundles) throws CoreException {
		Throwable ex;
		try {
			Field field = EquinoxLaunchConfiguration.class
					.getDeclaredField("fModels");
			field.setAccessible(true);
			field.set(delegate, models);

			field = EquinoxLaunchConfiguration.class
					.getDeclaredField("fAllBundles");
			field.setAccessible(true);
			field.set(delegate, allBundles);

			return;
		} catch (IllegalAccessException e) {
			ex = e;
		} catch (SecurityException e) {
			ex = e;
		} catch (NoSuchFieldException e) {
			ex = e;
		}
		throw new CoreException(createStatus("Failed to inject bundles", ex)); //$NON-NLS-1$
	}

	private static Status createStatus(String message, Throwable ex) {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, ex);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		if (monitor.isCanceled()) {
			return false;
		}

		if (!super.preLaunchCheck(configuration, mode, monitor))
			return false;

		waitForClearBundlePool(monitor);

		final CachedInfo info = LaunchInfoCache.getInfo(configuration);
		if (info.target != null) {
			return true;
		}

		final ITargetPlatformHelper target = Q7TargetPlatformManager.getTarget(configuration,
				SubMonitor.convert(monitor, 2));

		if (monitor.isCanceled()) {
			removeTargetPlatform(configuration);
			return false;
		}

		info.target = target;
		final MultiStatus error = new MultiStatus(Q7ExtLaunchingPlugin.PLUGIN_ID, 0,
				"Target platform initialization failed  for " + configuration.getName(), null); //$NON-NLS-1$
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

		IVMInstall install = VMHelper.getVMInstall(configuration);

		OSArchitecture jvmArch = JDTUtils.detect(install);

		if (jvmArch.equals(architecture)
				|| (jvmArch.equals(OSArchitecture.x86_64) && (JDTUtils.canRun32bit(install)))) {
			haveAUT = true;
		}

		if (!haveAUT && architecture != OSArchitecture.Unknown
				&& target.detectArchitecture(false, new StringBuilder()) == OSArchitecture.Unknown) {
			haveAUT = true;
		}

		if (!haveAUT) {
			// Let's search for configuration and update JVM if possible.
			haveAUT = updateJVM(configuration, architecture, ((ITargetPlatformHelper) info.target));

			if (!haveAUT) {
				// try to register current JVM, it may help
				JDTUtils.registerCurrentJVM();
				haveAUT = updateJVM(configuration, architecture, ((ITargetPlatformHelper) info.target));
			}

		}
		if (!haveAUT) {
			removeTargetPlatform(configuration);
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "", null));
		}
		return true;
	}

	private void waitForClearBundlePool(IProgressMonitor monitor) {
		try {
			Job.getJobManager().join(IBundlePoolConstansts.CLEAN_BUNDLE_POOL_JOB, SubMonitor.convert(monitor, 1));
		} catch (Exception e1) {
		}
	}

	private void removeTargetPlatform(ILaunchConfiguration configuration)
			throws CoreException {
		String targetPlatformName = Q7TargetPlatformManager.getTargetPlatformName(configuration);
		Q7TargetPlatformManager.delete(targetPlatformName);
		LaunchInfoCache.remove(configuration);
		TargetPlatformManager.deleteTargetPlatform(targetPlatformName);
	}

	@Override
	public String[] getVMArguments(ILaunchConfiguration config) throws CoreException {
		CachedInfo info = LaunchInfoCache.getInfo(config);
		if (info.vmArgs != null) {
			return info.vmArgs;
		}

		List<String> args = new ArrayList<String>();
		// ORDER IS CRUCIAL HERE:
		// Override VM arguments that are specified manually with the values
		// necessary for the RAP launcher
		args.add("-Declipse.ignoreApp=true");
		args.add("-Dosgi.noShutdown=true");
		args.addAll(Arrays.asList(super.getVMArguments(config)));
		args.addAll(getRAPVMArguments());

		// Filter some PDE parameters
		Iterables.removeIf(args, new Predicate<String>() {
			public boolean apply(String input) {
				if (input.contains("-Declipse.pde.launch=true")) {
					return true;
				}
				return false;
			}
		});

		args.add("-Dq7id=" + launch.getAttribute(IQ7Launch.ATTR_AUT_ID));
		args.add("-Dq7EclPort=" + AutEventManager.INSTANCE.getPort());

		TargetPlatformHelper target = (TargetPlatformHelper) ((ITargetPlatformHelper) info.target);

		IPluginModelBase hook = target.getWeavingHook();
		if (hook == null) {
			throw new CoreException(Q7ExtLaunchingPlugin.status("No " + AJConstants.HOOK + " plugin")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Append all other properties from original config file
		OriginalOrderProperties properties = target.getConfigIniProperties();

		args = UpdateVMArgs.addHook(args, hook, properties.getProperty(OSGI_FRAMEWORK_EXTENSIONS));

		args.add("-Declipse.vmargs=" + Joiner.on("\n").join(args) + "\n");


		info.vmArgs = args.toArray(new String[args.size()]);
		return info.vmArgs;
	}

	@Override
	public String[] getProgramArguments(ILaunchConfiguration configuration) throws CoreException {

		CachedInfo info = LaunchInfoCache.getInfo(configuration);
		final ITargetPlatformHelper target = (ITargetPlatformHelper) info.target;
		if (info.programArgs != null) {
			return info.programArgs;
		}

		final List<String> programArguments = new ArrayList<String>();
		programArguments.addAll(Arrays.asList(super.getProgramArguments(configuration)));
		String dataLocationResolved = getResolvedDataLoacation();
		if (dataLocationResolved.length() > 0) {
			programArguments.addAll(Arrays.asList("-data", dataLocationResolved));
		}
		try {
			// Correct osgi.install.area property key
			File config = new File(getConfigDir(configuration), "config.ini");
			final Properties props = readProperty(config);

			File location = target.getQ7Target().getInstallLocation();
			if (location != null) {
				props.setProperty("osgi.install.area", location.getAbsolutePath()); //$NON-NLS-1$
			}

			// Append all other properties from original config file
			final OriginalOrderProperties properties = target.getConfigIniProperties();
			final String property = properties.getProperty(OSGI_BUNDLES);

			final boolean autostart = configuration.getAttribute(IPDELauncherConstants.DEFAULT_AUTO_START, true);
			props.setProperty(OSGI_BUNDLES, computeOSGiBundles(info, autostart, property)); // $NON-NLS-1$

			properties.setBeginAdd(true);
			properties.putAll(props);

			writeProperty(config, properties);
		} catch (IOException e) {
			throw new CoreException(Q7ExtLaunchingPlugin.status(e));
		}
		String override = configuration.getAttribute(
				IQ7Launch.OVERRIDE_SECURE_STORAGE, (String) null);
		if (override == null || "true".equals(override)) {
			// Override existing parameter
			programArguments.add("-eclipse.keyring");
			programArguments.add(getConfigDir(configuration).toString()
					+ IPath.SEPARATOR + SECURE_STORAGE_FILE_NAME);
		}

		IVMInstall install = VMHelper.getVMInstall(configuration);
		programArguments.add("-vm");
		programArguments.add(install.getInstallLocation().toString());

		info.programArgs = programArguments.toArray(new String[programArguments.size()]);

		return info.programArgs;

	}

	public static Map<IPluginModelBase, String> getTargetBundleMap(String bundles)
			throws CoreException {
		Map<IPluginModelBase, String> map = new HashMap<IPluginModelBase, String>();
		StringTokenizer tok = new StringTokenizer(bundles, ","); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			int index = token.indexOf('@');
			if (index < 0) { // if no start levels, assume default
				token = token.concat("@default:default"); //$NON-NLS-1$
				index = token.indexOf('@');
			}
			String idVersion = token.substring(0, index);
			int versionIndex = idVersion.indexOf(BundleLauncherHelper.VERSION_SEPARATOR);
			String id = (versionIndex > 0) ? idVersion.substring(0, versionIndex) : idVersion;
			String version = (versionIndex > 0) ? idVersion.substring(versionIndex + 1) : null;
			ModelEntry entry = PluginRegistry.findEntry(id);
			if (entry != null) {
				IPluginModelBase[] models = entry.getExternalModels();
				for (IPluginModelBase model : models) {
					if (model.isEnabled()) {
						IPluginBase base = model.getPluginBase();
						// match only if...
						// a) if we have the same version
						// b) no version
						// c) all else fails, if there's just one bundle available, use it
						if (base.getVersion().equals(version) || version == null || models.length == 1)
							addBundleToMap(map, model, token.substring(index + 1));
					}
				}
			}
		}
		return map;
	}

	private static void addBundleToMap(Map<IPluginModelBase, String> map, IPluginModelBase bundle, String sl) {
		BundleDescription desc = bundle.getBundleDescription();
		boolean defaultsl = (sl == null || sl.equals("default:default")); //$NON-NLS-1$
		if (desc != null && defaultsl) {
			String runLevelText = BundleLauncherHelper.resolveSystemRunLevelText(desc);
			String autoText = BundleLauncherHelper.resolveSystemAutoText(desc);
			if (runLevelText != null && autoText != null) {
				map.put(bundle, runLevelText + ":" + autoText); //$NON-NLS-1$
			} else {
				map.put(bundle, sl);
			}
		} else {
			map.put(bundle, sl);
		}

	}

	private static String computeOSGiBundles(CachedInfo info, boolean autostart, String defaultBundles)
			throws CoreException {
		final Map<IPluginModelBase, BundleStart> lastversion = getBundlesToLaunch(info).latestVersionsOnly;
		final Map<String, IPluginModelBase> bundles = new LinkedHashMap<String, IPluginModelBase>(lastversion.size());
		final Set<IPluginModelBase> models = lastversion.keySet();
		for (IPluginModelBase model : models) {
			bundles.put(model.getPluginBase().getId(), model);
		}

		if (bundles.containsKey(IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR)) {
			return Q7LaunchDelegateUtils.computeOSGiBundles(lastversion);
		} else {
			final StringBuffer buffer = new StringBuffer();
			final Iterator<IPluginModelBase> iter = models.iterator();

			Map<IPluginModelBase, String> defaultModels = getTargetBundleMap(defaultBundles);
			while (iter.hasNext()) {
				IPluginModelBase model = iter.next();
				String id = model.getPluginBase().getId();
				if (!IPDEBuildConstants.BUNDLE_OSGI.equals(id)) {
					if (buffer.length() > 0)
						buffer.append(","); //$NON-NLS-1$
					buffer.append(LaunchConfigurationHelper.getBundleURL(model, true));

					// fragments must not be started or have a start level
					if (model instanceof IFragmentModel)
						continue;

					String startData = getStartData(model, defaultModels);
					if (startData != null)
						appendStartData(buffer, startData, autostart);
				}
			}
			return buffer.toString();
		}
	}

	private static final String ASPECTJ_BUNDLE = "org.eclipse.equinox.weaving.aspectj";
	private static final String SERVLETBRIDGE = "org.eclipse.equinox.servletbridge.extensionbundle";

	private static String getStartData(IPluginModelBase model, Map<IPluginModelBase, String> defaultModels) {
		final String id = model.getBundleDescription().getName();
		if (SERVLETBRIDGE.equals(id)) {
			return null;
		}
		if ("org.apache.felix.scr".equals(id)) {
			return "1:true";
		}
		if (ASPECTJ_BUNDLE.equals(id)) {
			return "1:true";
		}

		if (defaultModels.containsKey(model)) {
			return defaultModels.get(model);
		}

		return "default:default";
	}

	private static void appendStartData(StringBuffer buffer, String startData, boolean defaultAuto) {
		int index = startData.indexOf(':');
		String level = index > 0 ? startData.substring(0, index) : "default"; //$NON-NLS-1$
		String auto = index > 0 && index < startData.length() - 1 ? startData.substring(index + 1) : "default"; //$NON-NLS-1$
		if ("default".equals(auto)) //$NON-NLS-1$
			auto = Boolean.toString(defaultAuto);
		if (!level.equals("default") || "true".equals(auto)) //$NON-NLS-1$ //$NON-NLS-2$
			buffer.append("@"); //$NON-NLS-1$

		if (!level.equals("default")) { //$NON-NLS-1$
			buffer.append(level);
			if ("true".equals(auto)) //$NON-NLS-1$
				buffer.append(":"); //$NON-NLS-1$
		}
		if ("true".equals(auto)) { //$NON-NLS-1$
			buffer.append("start"); //$NON-NLS-1$
		}
	}

	private void writeProperty(File config, OriginalOrderProperties properties)
			throws FileNotFoundException, IOException {
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(config));
		properties.store(out, "Configuration File");
		out.close();
	}

	private Properties readProperty(File config) throws FileNotFoundException, IOException {
		Properties props = new Properties();

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(config));
		props.load(in);
		in.close();
		return props;
	}

	private static final String SECURE_STORAGE_FILE_NAME = "secure_storage";

	public static BundlesToLaunch getBundlesToLaunch(CachedInfo info) {
		return (BundlesToLaunch) info.data.get("bundlesToLaunch");
	}

	private String getResolvedDataLoacation() throws CoreException {
		String dataLocation = config.getDataLocation();
		return resolveVariables(dataLocation);
	}

	private String resolveVariables(String dataLocation) throws CoreException {
		VariablesPlugin variablePlugin = VariablesPlugin.getDefault();
		IStringVariableManager stringVariableManager = variablePlugin.getStringVariableManager();
		return stringVariableManager.performStringSubstitution(dataLocation);
	}

	private List<String> getRAPVMArguments() throws CoreException {
		List<String> arguments = new ArrayList<String>();
		arguments.add(VMARG_PORT + port);
		arguments.add(VMARG_DEVELOPMENT_MODE + config.getDevelopmentMode());
		if (config.getUseSessionTimeout()) {
			arguments.add(VMARG_SESSION_TIMEOUT + config.getSessionTimeout());
		} else {
			arguments.add(VMARG_SESSION_TIMEOUT + RAPLaunchConfig.MIN_SESSION_TIMEOUT);
		}
		if (config.getUseManualContextPath()) {
			String contextPath = config.getContextPath();
			if (!contextPath.startsWith(SLASH)) {
				contextPath = SLASH + contextPath;
			}
			if (contextPath.endsWith(SLASH)) {
				contextPath = contextPath.substring(0, contextPath.length() - 1);
			}
			arguments.add(VMARG_CONTEXT_PATH + contextPath);
		}
		return arguments;
	}

	static final IStatus STATUS = new Status(IStatus.INFO, PLUGIN_ID, 601, "", null); //$NON-NLS-1$

	private void warnIfPortBusy(SubMonitor monitor) throws CoreException {
		monitor.beginTask("Checking manual port", IProgressMonitor.UNKNOWN);
		try {
			if (config.getUseManualPort() && isPortBusy(config.getPort())) {
				DebugPlugin debugPlugin = DebugPlugin.getDefault();
				IStatusHandler prompter = debugPlugin.getStatusHandler(promptStatus);
				if (prompter != null) {
					IStatus status = STATUS;
					Object resolution = prompter.handleStatus(status, config);
					if (Boolean.FALSE.equals(resolution)) {
						String text = "Port {0,number,\\#} in use. Launch ''{1}'' interrupted by user.";
						Object[] args = new Object[] {
								Integer.valueOf(config.getPort()),
								config.getName()
						};
						String msg = MessageFormat.format(text, args);
						String pluginId = PLUGIN_ID;
						Status infoStatus = new Status(IStatus.INFO, pluginId, msg);
						throw new CoreException(infoStatus);
					}
				}
			}
		} finally {
			monitor.done();
		}
	}

	private int determinePort(IProgressMonitor monitor) throws CoreException {
		int result;
		monitor.beginTask("Determining port number", IProgressMonitor.UNKNOWN);
		try {
			if (config.getUseManualPort()) {
				result = config.getPort();
			} else {
				result = findFreePort();
			}
		} finally {
			monitor.done();
		}
		return result;
	}

	private static int findFreePort() throws CoreException {
		try {
			ServerSocket server = new ServerSocket(0);
			try {
				return server.getLocalPort();
			} finally {
				server.close();
			}
		} catch (IOException e) {
			String msg = "Could not obtain a free port number."; //$NON-NLS-1$
			String pluginId = PLUGIN_ID;
			Status status = new Status(IStatus.ERROR, pluginId, msg, e);
			throw new CoreException(status);
		}
	}

	private static boolean isPortBusy(int port) {
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException e1) {
			// assume that port is occupied when getting here
		}
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return server == null;
	}

	private URL getUrl() throws CoreException {
		try {
			String url = URLBuilder.fromLaunchConfig(config, port, testMode);
			return new URL(url);
		} catch (MalformedURLException e) {
			String msg = "Invalid URL."; //$NON-NLS-1$
			String pluginId = PLUGIN_ID;
			Status status = new Status(IStatus.ERROR, pluginId, 0, msg, e);
			throw new CoreException(status);
		}
	}

	private void terminateIfRunning(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Terminating previous launch", IProgressMonitor.UNKNOWN);
		try {
			ILaunch runningLaunch = findRunning();
			if (runningLaunch != null) {
				terminate(runningLaunch);
			}
		} finally {
			monitor.done();
		}
	}

	private ILaunch findRunning() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] runningLaunches = launchManager.getLaunches();
		ILaunch result = null;
		for (int i = 0; result == null && i < runningLaunches.length; i++) {
			ILaunch runningLaunch = runningLaunches[i];
			if (runningLaunch != launch
					&& config.getName().equals(getLaunchName(runningLaunch))
					&& !runningLaunch.isTerminated()) {
				result = runningLaunches[i];
			}
		}
		return result;
	}

	private static String getLaunchName(ILaunch launch) {
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		// the launch config might be null (e.g. if deleted) even though there
		// still exists a launch for that config
		return launchConfiguration == null ? null : launchConfiguration.getName();
	}

	private static void terminate(final ILaunch previousLaunch) throws DebugException {
		final Object signal = new Object();
		final boolean[] terminated = { false };
		final DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.addDebugEventListener(new IDebugEventSetListener() {
			public void handleDebugEvents(DebugEvent[] events) {
				for (DebugEvent event : events) {
					if (isTerminateEventFor(event, previousLaunch)) {
						debugPlugin.removeDebugEventListener(this);
						synchronized (signal) {
							terminated[0] = true;
							signal.notifyAll();
						}
					}
				}
			}
		});
		previousLaunch.terminate();
		try {
			synchronized (signal) {
				if (!terminated[0]) {
					signal.wait();
				}
			}
		} catch (InterruptedException e) {
			// ignore
		}
	}

	private static boolean isTerminateEventFor(DebugEvent event, ILaunch launch) {
		boolean result = false;
		if (event.getKind() == DebugEvent.TERMINATE
				&& event.getSource() instanceof RuntimeProcess) {
			RuntimeProcess process = (RuntimeProcess) event.getSource();
			result = process.getLaunch() == launch;
		}
		return result;
	}

	private void waitForHttpService(IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
		subMonitor.beginTask("Waiting for HTTP service", IProgressMonitor.UNKNOWN);
		try {
			long start = System.currentTimeMillis();
			boolean canConnect = false;
			boolean interrupted = false;
			while (System.currentTimeMillis() - start <= CONNECT_TIMEOUT
					&& !canConnect
					&& !interrupted
					&& !monitor.isCanceled()
					&& !launch.isTerminated()) {
				try {
					Socket socket = new Socket(URLBuilder.getHost(), port);
					socket.close();
					canConnect = true;
				} catch (Exception e) {
					// http service not yet started - wait a bit
					try {
						Thread.sleep(200);
					} catch (InterruptedException ie) {
						interrupted = true;
					}
				}
			}
		} finally {
			subMonitor.done();
		}
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
	public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
		String[] classpath = constructClasspath(configuration);
		if (classpath == null) {
			String message = PDEMessages.WorkbenchLauncherConfigurationDelegate_noStartup;
			throw new CoreException(createStatus(message, null));
		}

		return classpath;

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

	private static String getSubstitutedString(String text)
			throws CoreException {
		if (text == null)
			return ""; //$NON-NLS-1$
		IStringVariableManager mgr = VariablesPlugin.getDefault()
				.getStringVariableManager();
		return mgr.performStringSubstitution(text);
	}

	@Override
	public void clear(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		clearDataLocation(configuration, monitor);
		super.clear(configuration, monitor);
	}

	private static boolean isHeadless(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration
				.getAttribute(IQ7Launch.ATTR_HEADLESS_LAUNCH, false);
	}

	private void clearDataLocation(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		String resolvedDataLocation = getResolvedDataLoacation();
		LauncherUtils.clearWorkspace(configuration, resolvedDataLocation, launch.getLaunchMode(), monitor);
	}

	private void registerBrowserOpener() {
		final String jobTaskName = "Starting client application";
		Job job = new Job(jobTaskName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String taskName = jobTaskName;
				monitor.beginTask(taskName, 2);
				try {
					waitForHttpService(monitor);
					monitor.worked(1);
					if (!launch.isTerminated()) {
						openBrowser(monitor);
					}
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void openBrowser(IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
		subMonitor.beginTask("Starting client application", IProgressMonitor.UNKNOWN);
		try {
			URL url = null;
			try {
				url = getUrl();
				browser.launch(url, config);
			} catch (CoreException e) {
				String msg = MessageFormat.format("Failed to open browser for URL ''{0}''.", new Object[] { url });
				Activator.getDefault().errorLog(msg, e);
			}
		} finally {
			subMonitor.done();
		}
	}

	private static boolean updateJVM(ILaunchConfiguration configuration,
			OSArchitecture architecture, ITargetPlatformHelper target) throws CoreException {
		IVMInstall jvmInstall = null;
		OSArchitecture jvmArch = OSArchitecture.Unknown;
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		boolean haveArch = false;
		for (IVMInstallType ivmInstallType : types) {
			IVMInstall[] installs = ivmInstallType.getVMInstalls();
			for (IVMInstall ivmInstall : installs) {
				jvmArch = JDTUtils.detect(ivmInstall);
				if (jvmArch.equals(architecture)
						|| (jvmArch.equals(OSArchitecture.x86_64) && JDTUtils
								.canRun32bit(ivmInstall))) {
					jvmInstall = ivmInstall;
					haveArch = true;
					break;
				}
			}
		}
		if (haveArch) {
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
		return false;
	}

	private static final String ATTR_D32 = "-d32";
}
