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
package org.eclipse.rcptt.resources.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.resources.WSUtils;
import org.eclipse.rcptt.resources.internal.impl.Activator;
import org.eclipse.rcptt.workspace.WSFile;
import org.eclipse.rcptt.workspace.WSFolder;
import org.eclipse.rcptt.workspace.WSProject;
import org.eclipse.rcptt.workspace.WSRoot;
import org.eclipse.rcptt.workspace.WorkspaceData;
import org.eclipse.rcptt.workspace.WorkspaceFactory;

@SuppressWarnings("deprecation")
public class WSCaptureUtils {

	public static void capture(final IWorkspace workspace, final WorkspaceData data) throws CoreException {
		final WSRoot root = WorkspaceFactory.eINSTANCE.createWSRoot();
		data.setContent(root);
		workspace.run(new IWorkspaceRunnable() {
			public void run(final IProgressMonitor monitor)
					throws CoreException {
				final IProject[] projects = workspace.getRoot().getProjects();
				for (final IProject iProject : projects) {
					if (iProject.exists() && iProject.isOpen()) {
						// Do a project refresh, before import
						iProject.refreshLocal(IResource.DEPTH_INFINITE,
								new SubProgressMonitor(monitor, 1));

						final WSProject wsProject = WSUtils.getProject(root,
								iProject.getName(), true);
						root.getProjects().add(wsProject);
						doCapture(wsProject, iProject);
					}
				}
			}
		}, new NullProgressMonitor());
	}

	private static void doCapture(final WSFolder folder, final IContainer iContainer)
			throws CoreException {
		for (final IResource iResource : iContainer.members()) {
			final String name = iResource.getName();
			if (iResource instanceof IFolder) {
				final WSFolder child = WSUtils.getFolder(folder, name, true);
				doCapture(child, (IFolder) iResource);
			} else if (iResource instanceof IFile) {
				final IFile iFile = (IFile) iResource;
				IPath iPath = iFile.getLocation();

				if (!iFile.isLocal(IResource.DEPTH_ZERO)) {
					Activator.logWarn("Cannot retrieve contents of a file %s (%s). File skipped.",
							name, iFile.getLocation());
					continue;
				}

				final WSFile child = WSUtils.getFile(folder, name, true);
				if (null != iPath) {
					File jfile = iPath.toFile();
					if (jfile.canExecute()) {
						child.setExecutable(true);
					}
				}
				// IPath path = iFile.getFullPath();
				// child.setContentURI(path.toString());
				makeExecutableData(child, iFile);
			}
		}
	}

	private static void makeExecutableData(WSFile child, IFile iFile) {
		if ("false".equals(Q7Features.getInstance().getValue(
				Q7Features.Q7_CONTEXTS_RESOURCES_TRANSFER_CONTENT))) {
			return;
		}
		try {
			InputStream contents = iFile.getContents();
			final byte[] content = WSUtils.getStreamContent(contents);
			contents.close();
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
				child.setContent(bout.toByteArray());
			} else {
				child.setContent(content);
			}
		} catch (IOException e) {
			Activator.log(e);
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

}
