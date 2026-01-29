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

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.core.runtime.IProgressMonitor.done;
import static org.eclipse.pde.internal.build.IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR;
import static org.eclipse.pde.internal.launching.launcher.LaunchConfigurationHelper.getBundleURL;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.launching.launcher.LaunchArgumentsHelper;
import org.eclipse.pde.internal.launching.launcher.LaunchValidationOperation;
import org.eclipse.pde.launching.EclipseApplicationLaunchConfiguration;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin;
import org.eclipse.rcptt.internal.launching.ext.UpdateVMArgs;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper.Model;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class Q7LaunchDelegateUtils {
	
	public static IStatus validateForLaunch(ITargetPlatformHelper target, IProgressMonitor monitor, IVMInstall vm) {
		SubMonitor sm = SubMonitor.convert(monitor, "Validating bundles", 3);
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			IStatus status = target.resolve(sm.split(1));
			if (status.matches(IStatus.ERROR | IStatus.CANCEL)) {
				return status;
			}
			wc = Q7LaunchingUtil.createLaunchConfiguration(target);
			StringBuilder message = new StringBuilder();
			OSArchitecture architecture = target.detectArchitecture(message);
			if (architecture == null || architecture == OSArchitecture.Unknown) {
				return Status.error(message.toString());
			}
			Q7ExternalLaunchDelegate.updateJVM(wc, vm);
		} catch (CoreException e) {
			return e.getStatus();
		}

		LaunchValidationOperation validation = new LaunchValidationOperation(wc, target.getModels().map(Model::model).collect(toSet())) {
			@Override
			protected IExecutionEnvironment[] getMatchingEnvironments()
					throws CoreException {
				IExecutionEnvironmentsManager manager = JavaRuntime
						.getExecutionEnvironmentsManager();
				IExecutionEnvironment[] envs = manager
						.getExecutionEnvironments();
				return envs;
			}
		};
		try {
			wc.delete();
		} catch (CoreException e1) {
			return e1.getStatus();
		}
		try {
			StringBuilder b = new StringBuilder();
			validation.run(sm.split(1));
			Map<Object, Object[]> input = validation.getInput();
			for (Map.Entry<Object, Object[]> e : input.entrySet()) {
				Object value = e.getKey();
				if (value instanceof ResolverError) {
					b.append(value.toString()).append("\n");
				}
			}
			if (b.length() > 0) {
				return Status.error("Bundle validation failed: " + b.toString());
			}
			done(monitor);
		} catch (CoreException e) {
			return e.getStatus();
		} catch (OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	private static String getEntry(IPluginModelBase bundle, String startLevel) {
		StringBuilder result = new StringBuilder("reference:");
		result.append(getBundleURL(bundle, false));
		result.append(startLevel);
		return result.toString();
	}

	private static IPluginModelBase getSimpleConfigurator(
			Map<String, Object> bundles) {
		return (IPluginModelBase) bundles
				.get(IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR);
	}

	private static final Predicate<IPluginModelBase> isSimpleConfigurator = new Predicate<IPluginModelBase>() {
		@Override
		public boolean apply(IPluginModelBase input) {
			return BUNDLE_SIMPLE_CONFIGURATOR.equals(id(input));
		}
	};

	private static IPluginModelBase findSimpleConfigurator(
			Iterable<IPluginModelBase> plugins) {
		return find(plugins, isSimpleConfigurator, null);
	}

	public static String computeOSGiBundles(Map<String, Object> bundles,
			final Map<Object, String> bundlesWithStartLevels) {
		return computeOSGiBundles(bundles, bundlesWithStartLevels, 4);
	}

	public static String computeOSGiBundles(
			Map<IPluginModelBase, BundleStart> bundles) {
		return computeOSGiBundles(bundles, 4);
	}

	public static String computeOSGiBundles(
			Map<IPluginModelBase, BundleStart> bundles,
			final int defaultStartLevel) {
		IPluginModelBase sc = findSimpleConfigurator(bundles.keySet());
		if (sc != null) {
			return getEntry(sc, "@1:start");
		}

		StringBuilder sb = new StringBuilder();
		for (Entry<IPluginModelBase, BundleStart> entry : bundles.entrySet()) {
			if (EXCLUDE.contains(id(entry.getKey()))) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(",");
			}

			sb.append(getEntry(entry.getKey(),
					entry.getValue().toOsgiString(defaultStartLevel)));
		}
		return sb.toString();

	}

	private static List<String> EXCLUDE = asList(IPDEBuildConstants.BUNDLE_OSGI);
	private static final Predicate<Object> keepBundle = new Predicate<Object>() {

		@Override
		public boolean apply(Object input) {
			return !EXCLUDE.contains(id((IPluginModelBase) input));
		}
	};

	private static final String DEFAULT = "default";

	private static final String resolveStartLevel(String sl,
			int defaultStartLevel) {
		int sep = sl.indexOf(':');
		String levelStr = sep == -1 ? sl : sl.substring(0, sep);
		String startStr = sep == -1 ? DEFAULT : sl.substring(sep + 1);

		boolean defaultLevel = DEFAULT.equals(levelStr);
		boolean defaultStart = DEFAULT.equals(startStr);
		if (defaultLevel && defaultStart) {
			return "";
		}

		if (defaultStart) {
			return "@" + levelStr;
		}

		String actualLevelStr = defaultLevel ? Integer
				.toString(defaultStartLevel) : levelStr;
		String actualStartStr = Boolean.parseBoolean(startStr) ? ":start" : "";
		return "@" + actualLevelStr + actualStartStr;
	}

	public static String computeOSGiBundles(Map<String, Object> bundles,
			final Map<Object, String> bundlesWithStartLevels,
			final int defaultStartLevel) {

		IPluginModelBase simpleConfigurator = getSimpleConfigurator(bundles);
		if (simpleConfigurator != null) {
			return getEntry(simpleConfigurator, "@1:start");
		}

		return Joiner.on(",").join(
				transform(filter(bundles.values(), keepBundle),
						new Function<Object, String>() {
							@Override
							public String apply(Object input) {
								IPluginModelBase plugin = (IPluginModelBase) input;
								return getEntry(
										plugin,
										resolveStartLevel(
												bundlesWithStartLevels
														.get(plugin),
												defaultStartLevel));

							}
						}));
	}

	public static String id(IPluginModelBase plugin) {
		return plugin.getPluginBase().getId();
	}

	public static void setDelegateFields(
			EclipseApplicationLaunchConfiguration delegate,
			Map<IPluginModelBase, String> models, Map<String, List<IPluginModelBase>> allBundles) throws CoreException {
		try {
			Field field = EclipseApplicationLaunchConfiguration.class
					.getDeclaredField("fModels");
			field.setAccessible(true);
			field.set(delegate, models);

			field = EclipseApplicationLaunchConfiguration.class
					.getDeclaredField("fAllBundles");
			field.setAccessible(true);
			field.set(delegate, allBundles);
		} catch (IllegalAccessException e) {
			throw new CoreException(RcpttPlugin.createStatus("Failed to inject bundles", e));
		} catch (SecurityException e) {
			throw new CoreException(RcpttPlugin.createStatus("Failed to inject bundles", e));
		} catch (NoSuchFieldException e) {
			throw new CoreException(RcpttPlugin.createStatus("Failed to inject bundles", e));
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<IPluginModelBase, String> getEclipseApplicationModels(
			EclipseApplicationLaunchConfiguration delegate) {
		try {
			Field field = EclipseApplicationLaunchConfiguration.class
					.getDeclaredField("fModels");
			field.setAccessible(true);
			return (Map<IPluginModelBase, String>) field.get(delegate);
		} catch (Throwable e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
		}
		return null;
	}

	public static File getWorkingDirectory(File autLocation) {
		if (!Platform.getOS().equals(Platform.OS_MACOSX)) {
			return autLocation;
		}
		if (autLocation == null || !autLocation.exists()
				|| !autLocation.isDirectory()) {
			return autLocation;
		}
		for (File child : autLocation.listFiles()) {
			if (!child.isDirectory() || !child.getName().endsWith(".app")) {
				continue;
			}
			File result = new File(new File(child, "Contents"), "MacOS");
			if (result.exists()) {
				return result;
			}
		}
		return autLocation;
	}

	public static String getAUTArgs(String[] args) {
		return getAUTArgs(args == null ? Collections.<String> emptyList()
				: Arrays.asList(args));
	}

	public static String getJoinedVMArgs(ITargetPlatformHelper aut, Collection<String> userArgs) {
		return joinCommandArgs(getVMArgs(aut, userArgs));
	}

	public static List<String> getVMArgs(ITargetPlatformHelper aut, Collection<String> userArgs) {
		List<String> iniArgs = aut.getIniVMArgs();
		if (iniArgs == null) {
			iniArgs =   Lists.newArrayList(DebugPlugin.parseArguments(LaunchArgumentsHelper.getInitialVMArguments().trim()));
		}
		if (userArgs != null)
			iniArgs.addAll(userArgs);
		return UpdateVMArgs.updateAttr(iniArgs);
	}

	/** Adds a key value pair, if this key is not already present */
	private static void addIfAbsent(Collection<String> arguments, String key, String value) {
		Preconditions.checkNotNull(key);
		if (!Iterables.any(arguments, Predicates.equalTo(key))) {
			arguments.add(key);
			if (value != null)
				arguments.add(value);
		}
	}

	public static String getAUTArgs(Collection<String> userArgs) {
		List<String> allArgs = new ArrayList<String>();
		if (userArgs != null) {
			allArgs.addAll(userArgs);
		}
		addIfAbsent(allArgs, "-os", "${target.os}");
		addIfAbsent(allArgs, "-arch", "${target.arch}");
		addIfAbsent(allArgs, "-consoleLog", null);
		return joinCommandArgs(allArgs);
	}

	public static String joinCommandArgs(Collection<String> args) {
		if (args == null || args.isEmpty()) {
			return "";
		}
		return args.stream().map(Q7LaunchDelegateUtils::escapeCommandArg).collect(Collectors.joining(" "));
	}

	public static String escapeCommandArg(String argument) {
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            // https://stackoverflow.com/questions/29213106/how-to-securely-escape-command-line-arguments-for-the-cmd-exe-shell-on-windows
            if (argument.isEmpty()) {
                return "\"\"";
            }

            return "\"" + argument.replaceAll("\\\\\"", "\\\\\\\\\"").replaceAll("\\\\$", "\\\\\\\\").replaceAll("\"", "\\\\\"") + "\"";

        } else {
            return "\"" + argument.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + "\"";
        }
	}

	public static String joinCommandArgs(String[] args) {
		return joinCommandArgs(args == null ? Collections.<String> emptyList()
				: Arrays.asList(args));
	}
}
