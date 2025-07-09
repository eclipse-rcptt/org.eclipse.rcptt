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

import static org.eclipse.rcptt.launching.ext.BundleStart.fromModelString;
import static org.eclipse.rcptt.util.StringUtils.safeToString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.launching.launcher.BundleLauncherHelper;
import org.eclipse.rcptt.internal.launching.ext.AJConstants;
import org.eclipse.rcptt.internal.launching.ext.PDEUtils;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin;
import org.eclipse.rcptt.util.StreamUtil;
import org.osgi.framework.BundleException;

import com.google.common.io.Closer;

@SuppressWarnings("restriction")
public class StartLevelSupport {
	public static final String START_LEVEL_ATTR = "Runtime-StartLevel";
	public static final String AUTO_START_ATTR = "Runtime-AutoStart";

	private static final Map<String,  BundleStart> predefined = new HashMap<>();
	static {
		predefined.put(AJConstants.AJ_HOOK, fromModelString("1:true"));
		predefined.put(AJConstants.AJ_RT, fromModelString("1:true"));
		predefined.put(IPDEBuildConstants.BUNDLE_OSGI, fromModelString("-1:true"));
		predefined.put(IPDEBuildConstants.BUNDLE_DS, fromModelString("2:true"));
		predefined.put(IPDEBuildConstants.BUNDLE_EQUINOX_COMMON, fromModelString("2:true"));
		predefined.put(IPDEBuildConstants.BUNDLE_SIMPLE_CONFIGURATOR, fromModelString("1:true"));
		predefined
				.put(PDEUtils.BUNDLE_UPDATE_CONFIGURATOR,
						fromModelString(String.format(
								"%s:%s",
								BundleLauncherHelper.DEFAULT_UPDATE_CONFIGURATOR_START_LEVEL_TEXT,
								BundleLauncherHelper.DEFAULT_UPDATE_CONFIGURATOR_AUTO_START_TEXT)));
		predefined.put(IPDEBuildConstants.BUNDLE_CORE_RUNTIME, fromModelString("default:true"));
	}

	public static BundleStart getStartInfo(String manifest,
			BundleStart hint) throws BundleException {
		// first, check for Q7 headers
		String q7sl = null;
		String q7as = null;
		Map<String, String> headers;
		try {
			headers = ManifestElement.parseBundleManifest(new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8)), new HashMap<String, String>(10));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read memmory array", e);
		}
		if (headers != null) {
			q7sl = safeToString(headers.get(START_LEVEL_ATTR));
			q7as = safeToString(headers.get(AUTO_START_ATTR));
		} else {
			throw new IllegalArgumentException("Could not parse manifest:\n" + manifest);
		}
		if (q7sl != null || q7as != null) {
			return BundleStart.fromQ7Headers(q7sl, q7as);
		}
		
		if (!hint.isDefault()) {
			return hint;
		}
		
		// if hint is default, check for predefined bundle
		String id = headers.get("Bundle-SymbolicName");
		BundleStart result = predefined.getOrDefault(id, hint);
		return result;
	}

	@SuppressWarnings("resource")
	public static String loadManifest(String path) throws IOException {
		try (Closer closer = Closer.create()) {
			InputStream manifestStream = null;
			File dirOrJar = new File(path);
			String extension = new Path(path).getFileExtension();
			if (extension != null
					&& extension.equals("jar") && dirOrJar.isFile()) { //$NON-NLS-1$
				ZipFile jarFile = closer.register(new ZipFile(dirOrJar, ZipFile.OPEN_READ));
				ZipEntry manifestEntry = jarFile
						.getEntry(JarFile.MANIFEST_NAME);
				if (manifestEntry != null) {
					manifestStream = closer.register(jarFile.getInputStream(manifestEntry));
				}
			} else {
				File file = new File(dirOrJar, JarFile.MANIFEST_NAME);
				if (file.exists())
					manifestStream = closer.register(new FileInputStream(file));
			}
			if (manifestStream != null) {
				return new Scanner(manifestStream).useDelimiter("\\A").next();
			}
			return "";
		}
	}

}
