/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.tesla.ecl.internal.impl.commands;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.tesla.ecl.internal.impl.TeslaImplPlugin;
import org.eclipse.rcptt.tesla.ecl.rap.model.UploadFile;
import org.eclipse.rcptt.tesla.swt.dialogs.SWTDialogManager;
import org.eclipse.rcptt.util.Base64;
import org.eclipse.rcptt.util.FileSystemResolver;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.rcptt.util.PrefixScheme;
import org.eclipse.rcptt.util.StringUtils;

public class UploadFileService implements ICommandService {

	private FileSystemResolver resolver = makeResolver();

	@Override
	public IStatus service(Command command, IProcess context) throws InterruptedException, CoreException {
		final UploadFile cmd = (UploadFile) command;

		try {
			String path = null;
			if (!StringUtils.isEmpty(cmd.getBase64())) {
				path = saveFileFormContext(cmd.getBase64());
			} else {
				path = getPath(cmd.getFile());
			}
			final File file = getFile(path);
			SWTDialogManager.addFileDialogInfo(file.getAbsolutePath());

		} catch (CoreException ex) {
			return TeslaImplPlugin.err(ex.getMessage(), ex);
		}

		return Status.OK_STATUS;
	}

	private String saveFileFormContext(String base64) {

		try {
			File file = new File(createTempDirectory(), "tmp.file"); //$NON-NLS-1$
			file.createNewFile();
			ByteArrayInputStream input = new ByteArrayInputStream(Base64.decode(base64));
			FileOutputStream outputStream = new FileOutputStream(file);
			try {
				FileUtil.copy(input, file, true);
			} finally {
				outputStream.close();
			}

			return file.getAbsolutePath();

		} catch (IOException e) {
		}

		return null;
	}

	private static File createTempDirectory() throws IOException {
		File result = File.createTempFile("fileupload_", "");
		result.delete();
		if (result.mkdir())
			result.deleteOnExit();
		else {
			throw new IOException("Unable to create temp directory: " + result.getAbsolutePath());
		}
		return result;
	}

	private String getPath(String path) throws CoreException {

		if (StringUtils.isEmpty(path)) {
			throw new CoreException(TeslaImplPlugin.error("The path to file is empty"));
		}
		// Tries to substitute variables
		try {
			path = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(path, true);
		} catch (NoClassDefFoundError e) {
			throw new CoreException(TeslaImplPlugin.error("Invalid path to file."));
		}

		path = resolver.resolve(path);
		if (StringUtils.isEmpty(path)) {
			throw new CoreException(TeslaImplPlugin.error("The path is not resolved."));
		}

		return path;
	}

	private File getFile(String path) throws CoreException {

		final File file = new File(path);

		if (!file.exists()) {
			throw new CoreException(TeslaImplPlugin.error("The file does not exist."));
		}

		if (!file.isFile()) {
			throw new CoreException(TeslaImplPlugin.error("It is not a file path."));
		}

		return file;
	}

	private static FileSystemResolver makeResolver() {
		FileSystemResolver resolver = new FileSystemResolver();

		{ // workspace
			Location location = Platform.getInstanceLocation();
			if (location != null) {
				URL url = location.getURL();
				if (url != null) {
					try {
						File file = new File(url.toURI());
						resolver.register(new PrefixScheme(PrefixScheme.WORKSPACE, file.toString()));
					} catch (Exception e) {
					}
				}
			}
		}

		{ // aut
			Location location = Platform.getInstallLocation();
			if (location != null) {
				URL url = location.getURL();
				if (url != null) {
					try {
						File file = new File(url.toURI());
						resolver.register(new PrefixScheme(PrefixScheme.AUT, file.toString()));
					} catch (Exception e) {
					}
				}
			}
		}

		{ // file
			resolver.register(new PrefixScheme(PrefixScheme.FILE, null));
		}

		{ // home
			resolver.register(
					new PrefixScheme(PrefixScheme.HOME, new File(System.getProperty("user.home")).getAbsolutePath()));
		}
		return resolver;
	}

}
