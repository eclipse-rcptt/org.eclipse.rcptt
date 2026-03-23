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

package org.eclipse.rcptt.ui.launching;

import static org.eclipse.debug.core.ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION;
import static org.eclipse.rcptt.ui.launching.LaunchConfigurationMigration.parse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.ui.IStartup;
import org.w3c.dom.Document;

import com.google.common.base.Strings;
import com.google.common.io.Files;

/** Migrates launch configurations stored as workspace resources */
@SuppressWarnings("restriction")
// LaunchManager API is needed to notify about migrated launches
public class ResourceLaunchConfigurationMigration implements IStartup {
	private static final IPath LOCAL_LOCATION = DebugPlugin.getDefault()
			.getStateLocation().append(".launches");

	class MigrationJob extends Job {
		private final IResource resource;

		public MigrationJob(IResource resource) {
			super("Q7 launch migration: " + resource.getName());
			this.resource = resource;
			setRule(resource);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				resource.accept(resourceVisitor);
			} catch (CoreException e) {
				return e.getStatus();
			}
			return Status.OK_STATUS;
		}

	}

	public void migrate(IFile resource) throws CoreException {
		if (!ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION
				.equalsIgnoreCase(resource.getFileExtension()))
			return;
		if (!resource.isSynchronized(IResource.DEPTH_ZERO))
			return;
		Document document;
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(resource.getContents());
			document = parse(reader);
		} finally {
			FileUtil.safeClose(reader);
		}
		LaunchConfigurationMigration migration = new LaunchConfigurationMigration();
		if (migration.isMigrationRequired(document) && migration.migrate(document)) {
			Writer writer = new StringWriter();
			LaunchConfigurationMigration.write(document, writer);
			resource.setContents(new ByteArrayInputStream(writer.toString()
					.getBytes(StandardCharsets.UTF_8)), 0, null);
		}
	}

	private static File tempDir = Files.createTempDir();
	{
		tempDir.deleteOnExit();
	}

	public void migrate(File file) {
		String fileExtension = Path.fromOSString(file.getName())
				.getFileExtension();
		if (!LAUNCH_CONFIGURATION_FILE_EXTENSION
				.equalsIgnoreCase(fileExtension))
			return;
		FileReader reader = null;
		Document document = null;
		try {
			reader = new FileReader(file);
			document = parse(reader);
		} catch (FileNotFoundException e) {
			FileUtil.safeClose(reader);
		}

		LaunchConfigurationMigration migration = new LaunchConfigurationMigration();
		if (!migration.isMigrationRequired(document)) {
			return;
		}
		File backup = new File(file.getAbsolutePath() + ".q7_backup");
		file.renameTo(backup);
		File temp = null;
		try {

			if (migration.migrate(document)) {
				temp = new File(tempDir, file.getName());
				FileWriter writer = new FileWriter(temp);
				try {
					LaunchConfigurationMigration.write(document, writer);
				} finally {
					FileUtil.safeClose(writer);
				}
				((LaunchManager) DebugPlugin.getDefault().getLaunchManager()).importConfigurations(new File[] { temp },
						null);
			}
		} catch (Exception e) {
			file.delete();
			backup.renameTo(file);
			throw new RuntimeException(e);
		} finally {
			if (temp != null)
				temp.delete();
		}
	}

	protected final IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {

		@Override
		public boolean visit(final IResourceDelta delta) throws CoreException {
			if (delta.getKind() == IResourceDelta.REMOVED)
				return false;
			if (delta.getResource() instanceof IProject) {
				IProject project = (IProject) delta.getResource();
				return project.isOpen() && RcpttNature.isRcpttProject(project);
			}
			if (delta.getResource() instanceof IContainer)
				return true;
			if (delta.getResource() instanceof IFile) {
				final IFile file = (IFile) delta.getResource();
				if (!Strings.nullToEmpty(file.getFullPath().getFileExtension())
						.equalsIgnoreCase(LAUNCH_CONFIGURATION_FILE_EXTENSION))
					return false;

				new MigrationJob(file).schedule();
			}
			return false;
		}
	};

	private final IResourceVisitor resourceVisitor = new IResourceVisitor() {

		@Override
		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IContainer)
				return resource.isAccessible();
			if (resource instanceof IFile) {
				try {
					migrate((IFile) resource);
				} catch (Exception e) {
					Q7UIPlugin
							.log("Failed to migrate " + resource.getName(), e);
				}
			}
			return false;
		}
	};

	private final IResourceChangeListener resourceListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				if (event.getDelta() != null)
					event.getDelta().accept(deltaVisitor);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
	};

	@Override
	public void earlyStartup() {
		migrateResources();
		migrateLocal();
	}

	private void migrateLocal() {
		new Job("Migrate local Q7 launches") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IPath containerPath = LOCAL_LOCATION;
				final File directory = containerPath.toFile();
				if (!directory.isDirectory())
					return Status.OK_STATUS;
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return dir.equals(directory)
								&& name.endsWith(ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION);
					}
				};
				File[] files = directory.listFiles(filter);
				SubMonitor subMonitor = SubMonitor.convert(monitor);
				subMonitor.beginTask(getName(), files.length);
				for (int i = 0; i < files.length; i++) {
					SubMonitor childMonitor = subMonitor.newChild(1,
							SubMonitor.SUPPRESS_NONE);
					childMonitor.beginTask(files[i].getName(), 1);
					try {
						migrate(files[i]);
					} catch (Exception e) {
						Q7UIPlugin.log(
								"Failed to migrate " + files[i].getName(), e);
					} finally {
						childMonitor.done();
					}
				}
				subMonitor.done();
				return Status.OK_STATUS;
			}

		}.schedule();
	}

	private void migrateResources() {
		new MigrationJob(ResourcesPlugin.getWorkspace().getRoot())
				.schedule();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceListener, IResourceChangeEvent.POST_CHANGE);
	}

}
