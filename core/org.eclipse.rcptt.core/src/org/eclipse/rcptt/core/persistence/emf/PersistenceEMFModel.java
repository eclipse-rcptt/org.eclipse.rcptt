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
package org.eclipse.rcptt.core.persistence.emf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.rcptt.core.persistence.BasePersistenceModel;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.util.FileUtil;

public class PersistenceEMFModel extends BasePersistenceModel {
	public PersistenceEMFModel(Resource element) {
		super(element);
	}

	@Override
	protected synchronized void doExtractAll(InputStream contents)
			throws IOException {
		File file = files.get(PersistenceManager.CONTENT_ENTRY);
		if (file == null || !file.exists()) {
			doExtractFile(PersistenceManager.CONTENT_ENTRY, contents);
		}
	}

	@Override
	protected synchronized void doExtractFile(String fName, InputStream contents)
			throws IOException {
		if (fName.equals(PersistenceManager.CONTENT_ENTRY)) {
			OutputStream outputStream = internalStore(fName);
			FileUtil.copy(contents, outputStream);
			outputStream.close();
		}
	}

	@Override
	protected synchronized void doReadIndex(InputStream contents) {
		IPath filePath = rootPath.append(new Path(
				PersistenceManager.CONTENT_ENTRY));
		putFileItem(PersistenceManager.CONTENT_ENTRY, filePath);
	}

	@Override
	protected synchronized void doStoreTo(File file)
			throws FileNotFoundException, IOException {
		try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
				InputStream inputStream = read(PersistenceManager.CONTENT_ENTRY)) {
			if (inputStream != null) {
				FileUtil.copyNoClose(inputStream, out);
			}
		}
	}

	public boolean isSupportMultiItems() {
		return false;
	}

	public void updateAttributes(Resource element) {
		// TODO Auto-generated method stub

	}

	public void updateMetadata(Resource resource) {
		// TODO Auto-generated method stub

	}
}
