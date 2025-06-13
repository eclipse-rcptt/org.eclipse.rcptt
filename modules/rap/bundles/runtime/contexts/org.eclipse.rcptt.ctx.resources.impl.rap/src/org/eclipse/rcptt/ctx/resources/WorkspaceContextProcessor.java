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
package org.eclipse.rcptt.ctx.resources;

import static org.eclipse.core.commands.operations.OperationHistoryFactory.getOperationHistory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.function.BooleanSupplier;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.core.IContextProcessor;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.ctx.impl.internal.resources.Activator;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.resources.WSUtils;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.tesla.ecl.impl.rap.UIRunnable;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.UIJobCollector;
import org.eclipse.rcptt.tesla.ui.RWTUtils;
import org.eclipse.rcptt.util.StringUtils;
import org.eclipse.rcptt.util.resources.ResourcesUtil;
import org.eclipse.rcptt.workspace.WSFile;
import org.eclipse.rcptt.workspace.WSFileLink;
import org.eclipse.rcptt.workspace.WSFolder;
import org.eclipse.rcptt.workspace.WSFolderLink;
import org.eclipse.rcptt.workspace.WSProject;
import org.eclipse.rcptt.workspace.WSProjectLink;
import org.eclipse.rcptt.workspace.WSRoot;
import org.eclipse.rcptt.workspace.WorkspaceContext;
import org.eclipse.rcptt.workspace.WorkspaceFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class WorkspaceContextProcessor implements IContextProcessor {

	private static IWorkspaceRunnable refreshWorkspace = new IWorkspaceRunnable() {
		public void run(final IProgressMonitor monitor) throws CoreException {
			// refresh in case of SUT was working messy
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
					.getRoot();

			// Automatically remove non existing projects.
			autoDeleteUnexistingProjects(root);

			try {
				root.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				RcpttPlugin.log("Failed to refresh workspace on first time cause: + " + e.getMessage(), e);
				root.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
			// Wait for all auto build jobs
			Job[] autobuildJobs = Job.getJobManager().find(
					ResourcesPlugin.FAMILY_AUTO_BUILD);
			for (Job job : autobuildJobs) {
				job.schedule();
			}
		}
	};

	@Override
	public void apply(final Context context, BooleanSupplier isCancelled) throws CoreException {
		final WorkspaceContext wc = (WorkspaceContext) context;

		// Smart cancel/close jobs with showed UI interactions.
		final UIJobCollector collector = new UIJobCollector();
		Job.getJobManager().addJobChangeListener(collector);

		try {
			final IWorkspace ws = ResourcesPlugin.getWorkspace();
			final Display display = RWTUtils.findDisplay();

			disableMessageDialogsAndEnableCollector(collector);

			ws.run(refreshWorkspace, null, IWorkspace.AVOID_UPDATE, null);

			if (wc.isClearWorkspace()) {
				clearWorkspace(wc);
			}

			final CoreException ee[] = { null };
			display.syncExec(new Runnable() {
				public void run() {
					try {
						ws.run(new IWorkspaceRunnable() {
							public void run(final IProgressMonitor monitor)
									throws CoreException {
								fit(wc.getLocation(), wc.getContent(), true);
							}
						}, null, IWorkspace.AVOID_UPDATE, null);
					} catch (CoreException e) {
						ee[0] = e;
					}
				}
			});

			if (ee[0] != null) {
				throw ee[0];
			}

			ws.run(refreshWorkspace, null, IWorkspace.AVOID_UPDATE, null);

			UIRunnable.exec(new UIRunnable<Object>() {
				@Override
				public Object run() throws CoreException {

					collector.setNeedDisable();
					return null;
				}
			});
			collector.join(TeslaLimits.getContextJoinTimeout());
		} catch (Exception e) {
			CoreException ee = new CoreException(RcpttPlugin.createStatus(
					"Failed to execute context: " + wc.getName() + " Cause: "
							+ e.getMessage(),
					e));
			RcpttPlugin.log(e);
			throw ee;
		} finally {
			SWTUIPlayer.enableMessageDialogs();
			Job.getJobManager().removeJobChangeListener(collector);
		}
	}

	private void clearWorkspace(final WorkspaceContext wc) throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		// close all editor with input to resource
		UIRunnable.exec(closeEditorsWithResources);
		UIRunnable.exec(clearAllFileBuffers);
		UIRunnable.exec(clearHistory);

		while (UIRunnable.exec(clearOperationHistoryEntry))
			;

		final CoreException ee[] = { null };
		UIRunnable.exec(new UIRunnable<Object>() {
			@Override
			public Object run() throws CoreException {
				try {
					ws.run(new IWorkspaceRunnable() {
						public void run(final IProgressMonitor monitor)
								throws CoreException {
							removeWorkspaceFiles(wc);
						}
					}, null, IWorkspace.AVOID_UPDATE, null);
				} catch (CoreException e) {
					ee[0] = e;
				}
				return null;
			}
		});

		if (ee[0] != null) {
			throw ee[0];
		}
	}

	private static void removeWorkspaceFiles(WorkspaceContext wc)
			throws CoreException {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject[] projects = root.getProjects();
		final String contextName = wc.getName();
		String ignoredPattern = wc.getIgnoredByClearPattern();

		String[] resolveIgnoredPattern = resolveIgnoredPattern("",
				ignoredPattern);
		for (final IProject project : projects) {
			try {
				if (!isIgnored(project.getName(), resolveIgnoredPattern)) {
					project.delete(false, true, new NullProgressMonitor());
				}
			} catch (CoreException e) {
				throw new CoreException(createProjectDeleteFailStatus(contextName, project.getName(), e));
			}
		}
		// Remove workspace content on file system,
		// do not touch metadata
		IPath path = root.getLocation();
		if (path == null) {
			return;
		}

		File file = path.toFile();
		try {
			tryDeleteFilesExceptMetadata(file,
					resolveIgnoredPattern(file.getPath(), ignoredPattern));
		} catch (IOException e) {
			throw new CoreException(createContextFailStatus(contextName, e));
		}
	}

	private void disableMessageDialogsAndEnableCollector(
			final UIJobCollector collector) throws CoreException {
		UIRunnable.exec(new UIRunnable<Object>() {
			@Override
			public Object run() throws CoreException {
				SWTUIPlayer.disableMessageDialogs();
				collector.enable();
				return null;
			}
		});
	}

	public Context create(EObject param) throws CoreException {
		final WorkspaceContext context = WorkspaceFactory.eINSTANCE
				.createWorkspaceContext();
		final WSRoot root = WorkspaceFactory.eINSTANCE.createWSRoot();
		context.setContent(root);
		final WorkspaceContextMaker maker = new WorkspaceContextMaker();
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(final IProgressMonitor monitor)
					throws CoreException {
				final IProject[] projects = ResourcesPlugin.getWorkspace()
						.getRoot().getProjects();
				for (final IProject iProject : projects) {
					if (iProject.exists() && iProject.isOpen()) {
						// Do a project refresh, before import
						iProject.refreshLocal(IResource.DEPTH_INFINITE,
								new SubProgressMonitor(monitor, 1));

						final WSProject wsProject = WSUtils.getProject(root,
								iProject.getName(), true);
						root.getProjects().add(wsProject);
						doCreate(wsProject, iProject, maker);
					}
				}
			}
		}, new NullProgressMonitor());
		return context;
	}

	@SuppressWarnings("deprecation")
	private void doCreate(final WSFolder folder, final IContainer iContainer,
			WorkspaceContextMaker maker) throws CoreException {
		for (final IResource iResource : iContainer.members()) {
			final String name = iResource.getName();
			if (iResource instanceof IFolder) {
				final WSFolder child = WSUtils.getFolder(folder, name, true);
				doCreate(child, (IFolder) iResource, maker);
			} else if (iResource instanceof IFile) {
				final IFile iFile = (IFile) iResource;
				IPath iPath = iFile.getLocation();

				if (!iFile.isLocal(IResource.DEPTH_ZERO)) {
					Activator.logWarn("Cannot retrieve contents of a file %s (%s). File skipped.", name,
							iFile.getLocation());
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
				makeExecutableContext(child, iFile);
			}
		}
	}

	private void makeExecutableContext(WSFile child, IFile iFile) {
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
				zout.setLevel(9);
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

	// private static final int STEP_TOTAL = 31;
	// private static final int STEP_LAST = 16;

	private static void tryDeleteFilesExceptMetadata(final File folder,
			String[] ignoredPatterns) throws IOException {
		List<File> undeletedResource = new ArrayList<File>();
		deleteFilesExceptMetadata(folder, ignoredPatterns, undeletedResource,
				true);

		if (undeletedResource.size() > 0
				&& Q7Features.getInstance().isTrue(
						Q7Features.Q7_WORKSPACE_FORCE_GC)) {
			System.gc();
			System.runFinalization();
			undeletedResource.clear();
			deleteFilesExceptMetadata(folder, ignoredPatterns,
					undeletedResource, true);
		}

		if (undeletedResource.size() > 0) {
			StringBuilder message = new StringBuilder(
					"Possible resource leak in AUT. Files below are locked and cannot be deleted:");
			for (File file : undeletedResource) {
				appendFileToList(file, message);
			}
			message.append(String.format("%nExclusion patterns:"));
			if (ignoredPatterns == null || ignoredPatterns.length == 0) {
				message.append(String.format("%n\t<none>"));
			} else {
				for (String pattern : ignoredPatterns) {
					message.append(String.format("%n\t'%s'", pattern));
				}
			}

			throw new IOException(message.toString());
		}
	}

	private static void appendFileToList(File file, StringBuilder sb) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				appendFileToList(child, sb);
			}
		}
		sb.append(String.format("%n\t'%s'", file.getAbsolutePath()));
	}

	private static boolean deleteFilesExceptMetadata(final File folder,
			final String[] ignoredPatterns, final List<File> undeletedResource,
			boolean root) {
		boolean haveIgnoredChild = false;
		for (final File file : folder.listFiles()) {
			if ((file.getName().equals(".metadata") && root)
					|| isIgnored(file.getPath(), ignoredPatterns)) {
				haveIgnoredChild = true;
				continue;
			}
			boolean ignoreBecauseOfChild = false;
			if (file.isDirectory()) {
				ignoreBecauseOfChild = deleteFilesExceptMetadata(file,
						ignoredPatterns, undeletedResource, false);
				haveIgnoredChild = haveIgnoredChild || ignoreBecauseOfChild;
			}
			if (!ignoreBecauseOfChild && !file.delete()) {
				undeletedResource.add(file);
			}
		}
		return haveIgnoredChild;
	}

	private static String[] resolveIgnoredPattern(String root,
			String ignoredPattern) {
		if (ignoredPattern == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		String prefix = StringUtils.isEmpty(root) ? "" : Pattern.quote(root.replace('\\', '/'));
		for (String pattern : ignoredPattern.split(",")) {
			pattern = pattern.trim();
			// as our patterns don't support escaping, backslashes
			// only can appear as windows-style path separators
			pattern = pattern.replace('\\', '/');
			if (pattern.isEmpty()) {
				continue;
			}

			// remove leading slashes
			while (pattern.charAt(0) == '/') {
				pattern = pattern.substring(1);
			}
			pattern = StringUtils.globToRegex(pattern);
			result.add(StringUtils.isEmpty(prefix) ? pattern : String.format("%s/%s", prefix, pattern));
		}
		return result.toArray(new String[result.size()]);
	}

	private static boolean isIgnored(String fileName, String[] ignoredPatterns) {
		if (ignoredPatterns == null) {
			return false;
		}
		for (String pattern : ignoredPatterns) {
			fileName = fileName.replace('\\', '/');
			String fileNameWithSlash = fileName + "/";
			if (fileName.matches(pattern) || fileNameWithSlash.matches(pattern)) {
				return true;
			}
		}
		return false;
	}

	private static IStatus createProjectDeleteFailStatus(String contextName, String projectName, Exception cause) {
		return RcpttPlugin.createStatus(String.format("Context '%s' failed: cannot remove project '%s' from workspace",
				contextName, projectName), cause);
	}

	private static IStatus createContextFailStatus(String contextName,
			Exception e) {
		return RcpttPlugin.createStatus("Failed to apply context \"" + contextName
				+ "\" caused by:\n" + e.getMessage());
	}

	private static void autoDeleteUnexistingProjects(final IWorkspaceRoot root) throws CoreException {
		IProject[] projects = root.getProjects();
		for (IProject prj : projects) {
			URI uri = prj.getLocationURI();
			if (uri == null || uri.getScheme() == null) {
				continue;
			}
			IFileStore store = EFS.getStore(uri);
			if (store == null) {
				continue;
			}
			IFileInfo fileInfo = store.fetchInfo();
			if (fileInfo != null && !fileInfo.exists()) {
				prj.delete(true, true, null);
				RcpttPlugin.infoLog("Q7 workspace context, automatic delete of unexisting project: " + prj.getName());
			}
		}
	}

	public boolean isApplied(final Context context) {
		try {
			final WorkspaceContext wc = (WorkspaceContext) context;
			return fit(wc.getLocation(), wc.getContent(), false);
		} catch (final Exception e) {
			// any exceptions mean context not applied
			return false;
		}
	}

	public boolean isCreateAllowed() {
		return true;
	}

	private boolean fit(final String location, final WSRoot root,
			final boolean modify) throws CoreException {
		boolean success = true;

		for (final WSProject project : root.getProjects()) {
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject rProject = ResourcesUtil.getCaseInsensitiveChild(wsRoot,
					project.getName(), IProject.class);
			if (rProject == null) {
				rProject = wsRoot.getProject(project.getName());
			}
			final IProject rProjectFinal = rProject;
			success &= fitProject(location, project, rProjectFinal, modify);
		}

		for (final WSProjectLink project : root.getProjectLinks()) {
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject rProject = ResourcesUtil.getCaseInsensitiveChild(wsRoot,
					project.getName(), IProject.class);
			if (rProject == null) {
				rProject = wsRoot.getProject(project.getName());
			}
			final IProject rProjectFinal = rProject;
			success &= fitProjectLink(location, project, rProjectFinal, modify);
		}

		return success;
	}

	private boolean fitProject(final String location, final WSProject project,
			final IProject rProject, final boolean modify) throws CoreException {
		if (!rProject.exists()) {
			if (!modify)
				return false;
			rProject.create(new NullProgressMonitor());
		}
		if (!rProject.isOpen()) {
			if (!modify)
				return false;
			rProject.open(new NullProgressMonitor());
		}
		return fitContent(location, project, rProject, modify);
	}

	private boolean fitProjectLink(final String location,
			final WSProjectLink project, final IProject rProject,
			final boolean modify) throws CoreException {
		if (!rProject.exists()) {
			if (!modify)
				return false;
			rProject.create(new NullProgressMonitor());
		}
		if (!rProject.isOpen()) {
			if (!modify)
				return false;
			rProject.open(new NullProgressMonitor());
		}
		return fitContent(location, project, rProject, modify);
	}

	private boolean fitContent(final String location, final WSFolder folder,
			final IContainer rFolder, final boolean modify)
			throws CoreException {
		boolean success = true;
		for (final WSFolder child : folder.getFolders()) {
			final IFolder rChild = rFolder.getFolder(new Path(child.getName()));
			success &= fitFolder(location, child, rChild, modify);
		}
		for (final WSFolderLink child : folder.getFolderLinks()) {
			final IFolder rChild = rFolder.getFolder(new Path(child.getName()));
			success &= fitFolderLink(location, child, rChild, modify);
		}
		for (final WSFile child : folder.getFiles()) {
			final IFile rChild = rFolder.getFile(new Path(child.getName()));
			success &= fitFile(location, child, rChild, modify);
		}
		for (final WSFileLink child : folder.getFileLinks()) {
			final IFile rChild = rFolder.getFile(new Path(child.getName()));
			success &= fitFileLink(location, child, rChild, modify);
		}

		return success;
	}

	private boolean fitContent(final String location,
			final WSFolderLink folder, final IContainer rFolder,
			final boolean modify) throws CoreException {

		boolean success = true;
		for (final WSFolderLink child : folder.getFolders()) {
			final IFolder rChild = rFolder.getFolder(new Path(child.getName()));
			success &= fitFolderLink(location, child, rChild, modify);
		}
		for (final WSFileLink child : folder.getFiles()) {
			final IFile rChild = rFolder.getFile(new Path(child.getName()));
			success &= fitFileLink(location, child, rChild, modify);
		}

		return success;
	}

	private boolean fitFolder(final String location, final WSFolder folder,
			final IFolder rFolder, final boolean modify) throws CoreException {
		if (!rFolder.exists()) {
			if (!modify)
				return false;
			rFolder.create(true, true, new NullProgressMonitor());
		}
		return fitContent(location, folder, rFolder, modify);
	}

	private boolean fitFolderLink(final String location,
			final WSFolderLink folder, final IFolder rFolder,
			final boolean modify) throws CoreException {
		if (!rFolder.exists()) {
			if (!modify)
				return false;
			rFolder.create(true, true, new NullProgressMonitor());
		}
		return fitContent(location, folder, rFolder, modify);
	}

	private boolean fitFile(final String location, final WSFile file,
			final IFile rFile, final boolean modify) throws CoreException {
		if (!rFile.exists() && !modify)
			return false;
		try {
			byte[] content = null;
			if (file.getContent() == null) {
				content = WSUtils.getFileContent(location, file);
			} else {
				if (Q7Features.getInstance().isTrue(
						Q7Features.Q7_CONTEXTS_RESOURCES_ZIPPED_TRANSFER)) {

					ZipInputStream zin = new ZipInputStream(
							new ByteArrayInputStream(file.getContent()));
					zin.getNextEntry();
					content = WSUtils.getStreamContent(zin);
				} else {
					content = file.getContent();
				}
			}
			final ByteArrayInputStream input = new ByteArrayInputStream(content);
			if (rFile.exists()) {
				InputStream fileContent = rFile.getContents();
				try {
					if (isEquals(content, fileContent))
						return true;
				} finally {
					WSUtils.safeClose(fileContent);
				}
				if (!modify)
					return false;
				rFile.setContents(input, false, false,
						new NullProgressMonitor());
				if (file.isExecutable()) {
					updateFilePermissions(rFile);
				}
				// sync file buffer for location
				FileBuffersUtils.getFileBuffers().syncLocation(
						rFile.getFullPath());
				return true;
			}
			rFile.create(input, false, new NullProgressMonitor());
			if (file.isExecutable()) {
				updateFilePermissions(rFile);
			}
			// sync file buffer for location
			FileBuffersUtils.getFileBuffers().syncLocation(rFile.getFullPath());
			return true;
		} catch (final IOException e) {
			final Status status = new Status(IStatus.ERROR,
					Activator.PLUGIN_ID, e.getMessage(), e);
			throw new CoreException(status);
		}
	}

	private void updateFilePermissions(IFile rFile) {
		IPath fileLocation = rFile.getLocation();
		if (null != fileLocation) {
			File jFile = fileLocation.toFile();
			try {
				boolean result = jFile.setExecutable(true, true);
				if (!result) {
					RcpttPlugin.log(
							String.format("Failed to set executable permission to file: %s", jFile.getAbsolutePath()),
							null);
				}
			} catch (SecurityException e) {
				RcpttPlugin.log(
						String.format("Failed to set executable permission to file: %s", jFile.getAbsolutePath()),
						e);
			}
		}
	}

	private boolean fitFileLink(final String location,
			final WSFileLink fileLink, final IFile rFile, final boolean modify)
			throws CoreException {
		if (!rFile.exists() && !modify)
			return false;
		try {
			byte[] content = fileLink.getContent();
			if (Q7Features.getInstance().isTrue(
					Q7Features.Q7_CONTEXTS_RESOURCES_ZIPPED_TRANSFER)) {

				ZipInputStream zin = new ZipInputStream(
						new ByteArrayInputStream(content));
				zin.getNextEntry();
				content = WSUtils.getStreamContent(zin);
			}

			final ByteArrayInputStream input = new ByteArrayInputStream(content);
			if (rFile.exists()) {
				InputStream fileContent = rFile.getContents();
				try {
					if (isEquals(content, fileContent))
						return true;
				} finally {
					WSUtils.safeClose(fileContent);
				}
				if (!modify)
					return false;
				rFile.setContents(input, false, false,
						new NullProgressMonitor());
				return true;
			}
			rFile.create(input, false, new NullProgressMonitor());
			return true;
		} catch (final IOException e) {
			final Status status = new Status(IStatus.ERROR,
					Activator.PLUGIN_ID, e.getMessage(), e);
			throw new CoreException(status);
		}
	}

	private boolean isEquals(final byte[] content, final InputStream stream)
			throws IOException {
		final byte[] buffer = new byte[4096];
		int len = 0, offset = 0;
		while ((len = stream.read(buffer)) > 0) {
			for (int i = 0; i < len; i++) {
				if (buffer[i] != content[i + offset])
					return false;
			}
			offset += len;
		}
		return true;
	}

	private static UIRunnable<Object> clearHistory = new UIRunnable<Object>() {
		@Override
		public Object run() throws CoreException {
			ResourcesPlugin.getWorkspace().getRoot()
					.clearHistory(new NullProgressMonitor());
			return null;
		}
	};

	private static UIRunnable<Object> clearAllFileBuffers = new UIRunnable<Object>() {
		@Override
		public Object run() throws CoreException {
			FileBuffersUtils.getFileBuffers().clearAll();
			return null;
		}
	};

	private static UIRunnable<Object> closeEditorsWithResources = new UIRunnable<Object>() {
		@Override
		public Object run() throws CoreException {
			IWorkbenchWindow[] workbenchWindows = RWTUtils.getWorkbenchWindows();
			for (IWorkbenchWindow win : workbenchWindows) {
				IWorkbenchPage[] pages = win.getPages();
				for (IWorkbenchPage page : pages) {
					IEditorReference[] refs = page.getEditorReferences();
					for (IEditorReference ref : refs) {
						closeEditor(page, ref);
					}
				}
			}

			return null;
		}

		private void closeEditor(IWorkbenchPage page, IEditorReference ref)
				throws PartInitException {
			IEditorInput editorInput = ref.getEditorInput();
			if (!editorInput.exists()) {
				return;
			}
			int attemptCount = 10;
			while (attemptCount-- > 0) {
				IWorkbenchPart part = ref.getPart(false);
				if (part == null) {
					return;
				}
				try {
					page.closeEditor((IEditorPart) part, false);
				} catch (Throwable e) {
					RcpttPlugin.log("Failed to close editor", e);
				}
			}
		}
	};

	private static UIRunnable<Boolean> clearOperationHistoryEntry = new UIRunnable<Boolean>() {
		public Boolean run() throws CoreException {
			try {
				IOperationHistory history = getOperationHistory();

				for (IUndoableOperation op : history.getUndoHistory(anyContext)) {
					history.replaceOperation(op, new IUndoableOperation[0]);
					return true;
				}

				for (IUndoableOperation op : history.getRedoHistory(anyContext)) {
					history.replaceOperation(op, new IUndoableOperation[0]);
					return true;
				}
			} catch (Throwable e) {
				RcpttPlugin.log(e);
			}
			return false;
		}

		private IUndoContext anyContext = new IUndoContext() {
			public boolean matches(IUndoContext context) {
				return true;
			}

			public String getLabel() {
				return "any";
			}
		};
	};

}
