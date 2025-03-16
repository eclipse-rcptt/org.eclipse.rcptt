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
package org.eclipse.rcptt.internal.launching.ext;

import static org.eclipse.rcptt.internal.launching.ext.AJConstants.OSGI_FRAMEWORK_EXTENSIONS;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.pde.core.plugin.IPluginModelBase;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class UpdateVMArgs {
	public static String escapeCommandArg(String arg) {
		if (arg == null || arg.length() == 0) {
			return "\"\""; // empty string encoded
		}
		// escape backslashes and quotes
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			arg = arg.replace("\\", "\\\\");
		arg = arg.replace("\"", "\\\"");
		return arg.contains(" ") ? String.format("\"%s\"", arg) : arg;
	}

	public static final Function<String, String> ESCAPE = new Function<String, String>() {

		@Override
		public String apply(String input) {
			return escapeCommandArg(input);
		}
	};
	public static void updateVMArgs(
			ILaunchConfigurationWorkingCopy configuration) {

		try {
			String attribute = configuration.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
			String finalValue = updateAttr(attribute);
			if (finalValue != null) {
				configuration.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
						finalValue);
			}
		} catch (CoreException e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
		}
	}

	// Adds a single argument if similar is not already present in the set
	public static void addIfAbsent(List<String> target, String prefix, String value) {
		if (!Iterables.any(target, new StartsWith(prefix))) {
			target.add(prefix + value);
		}
	}


	public static String updateAttr(String arguments) {
		return Joiner.on(" ").join(updateAttr(Arrays.asList(DebugPlugin.parseArguments(arguments))));
	}

	private static String addWeavingHook(String extensions, IPluginModelBase hook) {
		Preconditions.checkNotNull(hook);

		String ajref = String.format("reference:file:%s", hook.getInstallLocation());

		if (extensions == null) {
			return ajref;
		}

		// otherwise split and search for a duplicate AJ hook:
		StringBuilder result = new StringBuilder();

		for (String extension : extensions.split(",")) {
			if (extension.contains(AJConstants.HOOK)) {
				continue;
			}
			if (!extension.isEmpty())
				result.append(extension).append(',');
		}
		result.append(ajref).append(',');
		result.setLength(result.length() - 1);
		return result.toString();
	}

	public static List<String> addHook(List<String> vmArguments, IPluginModelBase hook, String defaultExtensions) {
		List<String> result = Lists.newArrayList(vmArguments);
		final String extensionsPrefix = "-D" + OSGI_FRAMEWORK_EXTENSIONS + "=";
		String extensions = "";
		Iterator<String> argIter = result.iterator();
		while (argIter.hasNext()) {
			String argument = argIter.next();
			if (argument.startsWith(extensionsPrefix)) {
				extensions = argument.substring(extensionsPrefix.length());
				argIter.remove();
			}
		}
		if (extensions.isEmpty())
			extensions = Strings.nullToEmpty(defaultExtensions);
		result.add(extensionsPrefix + addWeavingHook(extensions, hook));
		return result;
	}

	public static List<String> updateAttr(List<String> attribute) {
		List<String> result = Lists.newArrayList(attribute);
		// -XstartOnFirstThread
		// -Dorg.eclipse.swt.internal.carbon.smallFonts
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			addIfAbsent(result, "-XstartOnFirstThread", "");
			addIfAbsent(result, "-Dorg.eclipse.swt.internal.carbon.smallFonts", "");
		}
		return result;
	}
}
