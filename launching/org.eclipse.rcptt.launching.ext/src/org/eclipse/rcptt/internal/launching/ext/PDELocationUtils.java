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
/**
* 
*/
package org.eclipse.rcptt.internal.launching.ext;

import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.PLUGIN_ID;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.google.common.base.Strings;

public final class PDELocationUtils {

	private static final String CONFIGURATION_FOLDER = "configuration"; //$NON-NLS-1$
	private static final String PLUGINS_FOLDER = "plugins"; //$NON-NLS-1$
	private static final String PRODUCT_PATH_IN_E4_OSX = "Contents/Eclipse"; //$NON-NLS-1$

	public static File getPluginFolder(String location) throws CoreException {
		final IPath path = new Path(location);
		File plugin = null;
		if (PLUGINS_FOLDER.equals(path.lastSegment())) {
			plugin = path.toFile();
		} else {
			plugin = path.append(PLUGINS_FOLDER).toFile();
		}

		if (!plugin.exists() && Platform.getOS().equals(Platform.OS_MACOSX)) {
			plugin = getOSXProductE4Folder(path).append(PLUGINS_FOLDER).toFile();
		}

		IStatus status = validateDirectoryPath(plugin);
		if (status.isOK()) {
			return plugin;
		} else {
			throw new CoreException(status);
		}
	}

	public static File getProductLocation(String location) throws CoreException {
		IPath path = new Path(location);

		File pluginDir = path.append(PLUGINS_FOLDER).toFile();
		if (!pluginDir.exists() && Platform.getOS().equals(Platform.OS_MACOSX)) {
			path = getOSXProductE4Folder(path);
		}

		IStatus status = validateProductLocation(path.toString());
		if (status.isOK()) {
			return path.toFile();
		}

		throw new CoreException(status);
	}

	public static IStatus validateProductLocation(String location) {
		if (Strings.isNullOrEmpty(location) || location.trim().length() == 0)
			return new Status(IStatus.ERROR, PLUGIN_ID, "AUT instalation directory is not set."); //$NON-NLS-1$

		IPath productPath = new Path(location);
		File pluginDir = productPath.append(PLUGINS_FOLDER).toFile();
		if (!pluginDir.exists() && Platform.getOS().equals(Platform.OS_MACOSX)) {
			try {
				productPath = getOSXProductE4Folder(productPath);
			} catch (CoreException e) {
				return e.getStatus();
			}
		}

		IStatus status = validateDirectoryPath(productPath.toFile());
		if (!status.isOK()) {
			return status;
		}

		status = validateDirectoryPath(productPath.append(PLUGINS_FOLDER).toFile());
		if (!status.isOK()) {
			return status;
		}
		status = validateDirectoryPath(productPath.append(CONFIGURATION_FOLDER).toFile());
		if (!status.isOK()) {
			return status;
		}

		return Status.OK_STATUS;
	}

	private static IStatus validateDirectoryPath(File file) {
		if (file == null || !file.exists()) {
			return new Status(IStatus.ERROR, PLUGIN_ID, EFS.ERROR_NOT_EXISTS,
					"Directory \"" + (file != null ? file.getPath() : "null") + "\" does not exist.", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		if (!file.isDirectory()) {
			return new Status(IStatus.ERROR, PLUGIN_ID,
					"The specified path \"" + file.getPath() + "\" is not a directory."); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (!file.canRead()) {
			return new Status(IStatus.ERROR, PLUGIN_ID,
					"Cannot read directory \"" + file.getPath() + "\": Permission denied"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return Status.OK_STATUS;
	}

	private static IPath getOSXProductE4Folder(final IPath path) throws CoreException {
		IPath plugin = path.append(PRODUCT_PATH_IN_E4_OSX);
		if (plugin.toFile().exists()) {
			return plugin;
		}
		java.nio.file.Path root = path.toFile().toPath();
		try {
			return Path.fromOSString(Files.list(root).filter(Files::isDirectory).filter(PDELocationUtils::endsWithApp).findFirst().map(appDir -> 
				appDir. resolve("Contents").resolve("Eclipse")
			).filter(Files::isDirectory).orElseThrow(() -> new  NoSuchElementException("Can't find Eclipse directory in " + path)).toString());
		} catch (IOException e) {
			throw new CoreException(Status.error("Can't find Eclipse directory in " + path, e));
		}
	}

	
	private static boolean endsWithApp(java.nio.file.Path path) {
		return path.getFileName().toString().endsWith(".app");
	}
}
