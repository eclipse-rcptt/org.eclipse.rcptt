/*******************************************************************************
 * Copyright (c) 2009, 2009, 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.internal.resources.Activator;
import org.eclipse.rcptt.workspace.WSFile;
import org.eclipse.rcptt.workspace.WSFileLink;
import org.eclipse.rcptt.workspace.WSFolder;
import org.eclipse.rcptt.workspace.WSFolderLink;
import org.eclipse.rcptt.workspace.WSProject;
import org.eclipse.rcptt.workspace.WSRoot;
import org.eclipse.rcptt.workspace.WorkspaceData;

public class WorkspaceDataMaker {

	public void makeSelfSufficientData(WorkspaceData data,
			IPersistenceModel model, IQ7NamedElement source)
			throws ModelException {
		if (model != null) {
			model.extractAll();
		}
		// Load and store all required resources into context object
		WSRoot root = data.getContent();
		if (root != null) {
			for (WSProject wsProject : root.getProjects()) {
				String location = source.getParent().getResource()
						.getLocation().toString();
				fillFilesContent(wsProject.getFiles(), location, model);
				fillFileLinksContent(wsProject.getFileLinks());

				EList<WSFolder> folders = wsProject.getFolders();
				processFolders(folders, location, model);

				fillFolderLinksContent(wsProject.getFolderLinks(), model);
			}

			fillFolderLinksContent(root.getProjectLinks(), model);
		}
	}

	private void processFolders(EList<WSFolder> folders, String location,
			IPersistenceModel model) throws ModelException {
		for (WSFolder wsFolder : folders) {
			EList<WSFolder> list = wsFolder.getFolders();
			processFolders(list, location, model);

			EList<WSFile> files = wsFolder.getFiles();
			fillFilesContent(files, location, model);

			EList<WSFileLink> fileLinks = wsFolder.getFileLinks();
			fillFileLinksContent(fileLinks);

			fillFolderLinksContent(wsFolder.getFolderLinks(), model);
		}
	}

	private void fillFilesContent(EList<WSFile> files, String location,
			IPersistenceModel model) {
		for (WSFile wsFile : files) {
			fillFile(location, wsFile, model);
		}
	}

	private void fillFileLinksContent(EList<WSFileLink> fileLinks)
			throws ModelException {
		for (WSFileLink fileLink : fileLinks)
			fillFileLink(fileLink);
	}

	private <T extends WSFolderLink> void fillFolderLinksContent(
			EList<T> folderLinks, IPersistenceModel model)
			throws ModelException {
		for (WSFolderLink folderLink : folderLinks)
			fillFolderLink(folderLink, model);
	}

	private void fillFolderLink(WSFolderLink folderLink, IPersistenceModel model)
			throws ModelException {
		try {
			WSUtils.loadFolderLinkWithContent(folderLink);
		} catch (IOException e) {
			Activator.log(e);
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private void fillFile(String location, WSFile wsFile,
			IPersistenceModel model) {
		try {
			final byte[] content = WSUtils.getFileContent(location, wsFile,
					model);
			if (Q7Features.getInstance().isTrue(
					Q7Features.Q7_CONTEXTS_RESOURCES_ZIPPED_TRANSFER)) {

				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ZipOutputStream zout = new ZipOutputStream(bout);
				zout.setLevel(Deflater.BEST_SPEED);
				zout.setMethod(ZipOutputStream.DEFLATED);
				ZipEntry entry = new ZipEntry("content");
				entry.setTime(1);
				zout.putNextEntry(entry);
				zout.write(content);

				zout.close();
				wsFile.setContent(bout.toByteArray());
			} else {
				wsFile.setContent(content);
			}
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	private void fillFileLink(WSFileLink fileLink) throws ModelException {
		WSUtils.loadFileLinkWithContent(fileLink);
	}

}
