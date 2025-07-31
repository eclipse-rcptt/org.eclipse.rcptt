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
package org.eclipse.rcptt.ctx.filesystem;

import java.io.File;
import java.net.URL;
import java.util.function.BooleanSupplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.rcptt.core.IContextProcessor;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.filesystem.FSCaptureParam;
import org.eclipse.rcptt.filesystem.FilesystemContext;
import org.eclipse.rcptt.filesystem.FilesystemFactory;
import org.eclipse.rcptt.util.FileSystemResolver;
import org.eclipse.rcptt.util.PrefixScheme;

public class FilesystemContextProcessor implements IContextProcessor {

	private static FileSystemResolver makeResolver() {
		FileSystemResolver resolver = new FileSystemResolver();

		{ // workspace
			Location location = Platform.getInstanceLocation();
			if (location != null) {
				URL url = location.getURL();
				if (url != null) {
					try {
						File file = new File(url.toURI());
						resolver.register(new PrefixScheme(
								PrefixScheme.WORKSPACE, file.toString()));
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
						resolver.register(new PrefixScheme(PrefixScheme.AUT,
								file.toString()));
					} catch (Exception e) {
					}
				}
			}
		}

		{ // file
			resolver.register(new PrefixScheme(PrefixScheme.FILE, null));
		}

		{ // home
			resolver.register(new PrefixScheme(PrefixScheme.HOME, new File(
					System.getProperty("user.home")).getAbsolutePath()));
		}
		return resolver;
	}

	private static final FileSystemResolver resolver = makeResolver();

	public boolean isApplied(Context context) {
		throw new UnsupportedOperationException();
	}

	public void apply(Context context, BooleanSupplier isCancelled) throws CoreException {
		FSUtils.apply((FilesystemContext) context, resolver);
	}

	public Context create(EObject param) throws CoreException {
		FilesystemContext result = FilesystemFactory.eINSTANCE
				.createFilesystemContext();
		FSCaptureParam captureParam = (FSCaptureParam) param;
		FSUtils.capture(result,
				captureParam == null ? null : captureParam.getPath(), resolver);

		if (captureParam != null) {
			result.setPath(captureParam.getPath() == null ? "" : captureParam.getPath());
			result.setClear(captureParam.isClear());
		}

		return result;
	}

}
