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
package org.eclipse.rcptt.resources;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.resources.Activator;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.rcptt.util.Predicate;
import org.eclipse.rcptt.workspace.WSContainer;
import org.eclipse.rcptt.workspace.WSFile;
import org.eclipse.rcptt.workspace.WSFileLink;
import org.eclipse.rcptt.workspace.WSFolder;
import org.eclipse.rcptt.workspace.WSFolderLink;
import org.eclipse.rcptt.workspace.WSLink;
import org.eclipse.rcptt.workspace.WSProject;
import org.eclipse.rcptt.workspace.WSResource;
import org.eclipse.rcptt.workspace.WSRoot;
import org.eclipse.rcptt.workspace.WorkspaceData;
import org.eclipse.rcptt.workspace.WorkspaceFactory;
import org.osgi.framework.Bundle;

public class WSUtils {

	public static WSProject getProject(WSRoot root, String name, boolean create) {
		WSProject project = getResource(root.getProjects(), name);
		if (project == null && create) {
			project = WorkspaceFactory.eINSTANCE.createWSProject();
			project.setName(name);
			root.getProjects().add(project);
		}
		return project;
	}

	public static IPath getPath(WSResource resource) {
		if (resource instanceof WSRoot) {
			return Path.EMPTY;
		}
		EObject eObject = resource.eContainer();
		if (eObject instanceof WSFolder) {
			IPath parentPath = getPath((WSFolder) eObject);
			return parentPath.append(resource.getName());
		} else {
			return new Path(resource.getName());
		}
	}

	public static WSResource getResource(WSContainer container, IPath path) {
		String[] segments = path.segments();
		if (segments.length == 0)
			return container;
		WSResource result = null;
		int index = 0;
		for (; index < segments.length; index++) {
			String name = segments[index];
			result = getResource(getContents(container), name);
			if (result instanceof WSFolder) {
				container = (WSFolder) result;
			} else {
				break;
			}
		}
		if (index == segments.length)
			return result;
		return null;
	}

	public static List<WSResource> getContents(WSContainer container) {
		if (container instanceof WSRoot) {
			BasicEList<WSResource> resources = new BasicEList<WSResource>();
			WSRoot root = (WSRoot) container;
			resources.addAll(root.getProjects());
			resources.addAll(root.getProjectLinks());
			return resources;
		} else {
			WSFolder folder = (WSFolder) container;
			BasicEList<WSResource> resources = new BasicEList<WSResource>();
			resources.addAll(folder.getFolders());
			resources.addAll(folder.getFolderLinks());
			resources.addAll(folder.getFiles());
			resources.addAll(folder.getFileLinks());
			return resources;
		}
	}

	public static WSContainer getContainer(WSContainer container, String name,
			boolean create) {
		if (container instanceof WSRoot) {
			WSRoot root = (WSRoot) container;
			return getProject(root, name, create);
		} else {
			WSFolder folder = (WSFolder) container;
			return getFolder(folder, name, create);
		}
	}

	public static WSFile getFile(WSFolder folder, String name, boolean create) {
		WSFile file = getResource(folder.getFiles(), name);
		if (file == null && create) {
			file = WorkspaceFactory.eINSTANCE.createWSFile();
			file.setName(name);
			folder.getFiles().add(file);
		}
		return file;
	}

	public static WSFolder getFolder(WSFolder folder, String name,
			boolean create) {
		WSFolder result = getResource(folder.getFolders(), name);
		if (result == null && create) {
			result = WorkspaceFactory.eINSTANCE.createWSFolder();
			result.setName(name);
			folder.getFolders().add(result);
		}
		return result;
	}

	public static void remove(WSResource resource) {
		EReference ref = resource.eContainmentFeature();
		List<?> list = (List<?>) resource.eContainer().eGet(ref);
		list.remove(resource);
	}

	public static <T extends WSResource> T getResource(List<T> resources,
			String name) {
		for (T resorce : resources) {
			if (name.equals(resorce.getName())) {
				return resorce;
			}
		}
		return null;
	}

	public static byte[] getFileContent(String location, WSFile file)
			throws IOException {
		InputStream stream = getFileStream(location, file, null);
		return getStreamContent(stream);
	}

	public static byte[] getFileContent(String location, WSFile file,
			IPersistenceModel model) throws IOException {
		InputStream stream = getFileStream(location, file, model);
		return getStreamContent(stream);
	}

	public static byte[] getStreamContent(InputStream stream)
			throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[4096];
			int len = 0;
			while ((len = stream.read(buffer)) > 0) {
				output.write(buffer, 0, len);
			}
		} finally {
			safeClose(stream);
		}
		return output.toByteArray();
	}

	public static void safeClose(Closeable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {
		}
	}

	public static InputStream getFileStream(String location, WSFile file,
			IPersistenceModel model) throws FileNotFoundException {

		if (model != null) {
			String name = ImportUtils.getName(file);
			InputStream inputStream = model.read(name);
			if (inputStream != null) {
				return inputStream;
			}

		}
		if (location == null) {
			location = getContextFile(file);
			if (location == null) {
				location = Platform.getLocation().toString();
			}
		}
		File parentFile = new File(location);
		if (file.getContentURI() == null) {
			throw new FileNotFoundException(
					"Q7 could not find content for file: " + file.getName()
							+ "\nPlease recapture Workspace context.");
		}
		File childFile = new File(parentFile, file.getContentURI());
		return new BufferedInputStream(new FileInputStream(childFile));
	}

	private static String getContextFile(WSFile file) {
		Resource res = file.eResource();
		if (res != null) {
			try {
				// ResourceSet resources = res.getResourceSet();
				// if (resources != null) {
				URI uri = res.getURI();
				File oFile = null;
				if (uri == null) {
					return null;
				} else if (uri.isPlatformResource()) {
					IPath path = new Path(uri.toPlatformString(true));
					IFile iFile = ResourcesPlugin.getWorkspace().getRoot()
							.getFile(path);
					oFile = iFile.getLocation().toFile();
				} else if (uri.isFile() && !uri.isRelative()) {
					oFile = new File(uri.toFileString());
				} else if (uri.isPlatformPlugin()) {
					IPath path = new Path(uri.toPlatformString(true));
					if (path.segmentCount() > 1) {
						Bundle bundle = Platform.getBundle(path.segment(0));
						path = path.removeFirstSegments(1);
						URL url = FileLocator.find(bundle, path, null);
						url = FileLocator.toFileURL(url);
						oFile = new File(url.getFile());
					}
				}
				if (oFile != null) {
					return oFile.getParentFile().getAbsolutePath();
				}
				// }
			} catch (Exception e) {
				RcpttPlugin.log(e);
			}
		}
		return null;
	}

	public static IFile getLinkResource(WSLink link) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(link.getProject());
		if (project == null || !project.exists())
			return null;

		IResource member = project.findMember(link.getPath());
		if (member == null || !member.isAccessible())
			return null;
		if( member instanceof IFile) {
			return (IFile) member;
		}

		return null;
	}

	public static IContainer getLinkContainer(WSLink link) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(link.getProject());
		if (project == null)
			return null;

		IResource res = project.findMember(link.getPath());
		if (!(res instanceof IContainer) || !res.isAccessible())
			return null;

		return (IContainer) res;
	}

	public static byte[] loadFileLinkContent(WSFileLink fileLink)
			throws IOException, ModelException {

		IResource path = getLinkResource(fileLink);
		if (path == null || !(path instanceof IFile))
			throw new ModelException(new Q7Status(Status.ERROR,
					String.format(
							"File of link %s/%s is not found.",
							fileLink.getProject(), fileLink.getPath())));

		try {
			return FileUtil.getStreamContent(((IFile) path).getContents());
		} catch (CoreException e) {
			throw new ModelException(new Q7Status(Status.ERROR, new IllegalStateException(
					String.format(
							"Failed to load content of linked file %s/%s",
							fileLink.getProject(), fileLink.getPath()), e)));
		}
	}

	public static void loadFileLinkWithContent(WSFileLink fileLink)
			throws ModelException {
		try {
			final byte[] content = loadFileLinkContent(fileLink);
			if (Q7Features.getInstance().isTrue(
					Q7Features.Q7_CONTEXTS_RESOURCES_ZIPPED_TRANSFER)) {

				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ZipOutputStream zout = new ZipOutputStream(bout);
				zout.setLevel(9);
				zout.setMethod(ZipOutputStream.DEFLATED);
				ZipEntry entry = new ZipEntry("content");
				entry.setTime(1);
				zout.putNextEntry(entry);
				zout.write(content);

				zout.close();
				fileLink.setContent(bout.toByteArray());
			} else {
				fileLink.setContent(content);
			}
		} catch (IOException e) {
			Activator.log(e);
		}

	}

	public static void loadFolderLinkWithContent(WSFolderLink folderLink)
			throws IOException, CoreException {

		IContainer container = getLinkContainer(folderLink);
		if (container == null)
			throw new ModelException(new Q7Status(Status.ERROR, String.format(
							"Can not resolve folder link %s/%s.",
							folderLink.getProject(), folderLink.getPath())));

		IResource[] members = container.members();
		for (IResource r : members) {
			if (r instanceof IFolder) {
				WSFolderLink newFolderLink = WorkspaceFactory.eINSTANCE
						.createWSFolderLink();
				newFolderLink.setName(r.getName());
				newFolderLink.setProject(r.getProject().getName());
				newFolderLink.setPath(r.getProjectRelativePath()
						.toPortableString());

				folderLink.getFolders().add(newFolderLink);
				loadFolderLinkWithContent(newFolderLink);
			} else if (r instanceof IFile) {
				WSFileLink newFileLink = WorkspaceFactory.eINSTANCE
						.createWSFileLink();
				newFileLink.setName(r.getName());
				newFileLink.setProject(r.getProject().getName());
				newFileLink.setPath(r.getProjectRelativePath()
						.toPortableString());

				folderLink.getFiles().add(newFileLink);
				loadFileLinkWithContent(newFileLink);
			}
		}
	}

	public static List<WSLink> listLinks(WSResource content) {
		List<WSLink> result = new ArrayList<WSLink>();
		if (content instanceof WSRoot) {
			result.addAll(((WSRoot) content).getProjectLinks());
			EList<WSProject> projects = ((WSRoot) content).getProjects();
			for (WSProject wsProject : projects) {
				result.addAll(listLinks(wsProject));
			}
		}
		if (content instanceof WSFolder) {
			result.addAll(((WSFolder) content).getFolderLinks());
			result.addAll(((WSFolder) content).getFileLinks());
			EList<WSFolder> folders = ((WSFolder) content).getFolders();
			for (WSFolder wsFolder : folders) {
				result.addAll(listLinks(wsFolder));
			}
		}
		return result;
	}

	/**
	 * Iterates over workspace applying a visitor to each element.
	 * @param visitor - process is interrupted if predicate is true
	 * @param monitor 
	 */
	public static void visitWorkspace(WorkspaceData data, SubMonitor monitor, Predicate<WSResource> visitor) {
		if (visitor.apply(data.getContent()))
			return;
		visitRoot(data.getContent(), visitor, monitor);
	}

	private static void visitRoot(WSRoot content, Predicate<WSResource> visitor, SubMonitor monitor) {
		monitor.beginTask("Projects", content.getProjects().size());
		try {
			for (WSProject project : content.getProjects()) {
				if (visitor.apply(project))
					break;
				if (visitProject(project, visitor, monitor.newChild(1)))
					break;
			}
		} finally {
			monitor.done();
		}
	}

	public static boolean visitProject(WSProject project, Predicate<WSResource> visitor, SubMonitor monitor) {
		return visitFolder(project, visitor, monitor);
	}

	public static boolean visitFolder(WSFolder folder,
			Predicate<WSResource> visitor, SubMonitor monitor) {
		monitor.beginTask(folder.getName(),
				folder.getFiles().size()
				+ folder.getFileLinks().size()
				+ folder.getFolders().size()
				+ folder.getFolderLinks().size()
				);
		
		if (monitor.isCanceled())
			return true;
		
		try {
			for (WSFile entry: folder.getFiles()) {
				if (visitor.apply(entry))
					return true;
				monitor.worked(1);
			}
			
			for (WSFolder entry: folder.getFolders()) {
				if (visitor.apply(entry))
					return true;
				if (visitFolder(entry, visitor, monitor.newChild(1)))
					return true;
			}
			
			for (WSLink entry: folder.getFileLinks()) {
				if (visitor.apply(entry))
					return true;
				monitor.worked(1);
	
			}
			
			for (WSLink entry: folder.getFolderLinks()) {
				if (visitor.apply(entry))
					return true;
				monitor.worked(1);
			}
			
			return false;
		} finally {
			monitor.done();
		}
	}
}
