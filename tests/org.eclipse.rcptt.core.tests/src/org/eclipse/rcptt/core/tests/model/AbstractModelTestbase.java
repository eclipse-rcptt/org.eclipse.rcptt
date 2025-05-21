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
package org.eclipse.rcptt.core.tests.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IParent;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7Model;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.nature.RcpttNature;
import org.eclipse.rcptt.core.tests.SuiteOfTestCases;
import org.eclipse.rcptt.core.tests.Util;
import org.eclipse.rcptt.core.tests.Util.Comparer;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.ModelManager;

public abstract class AbstractModelTestbase extends SuiteOfTestCases {
	protected boolean displayName = false;
	protected String endChar = ",";

	// infos for invalid results
	protected int tabs = 2;

	protected String fTestProjectName = "org.eclipse.rcptt.core.tests";

	class DeltaListener implements IElementChangedListener {
		IQ7ElementDelta[] deltas;

		public void elementChanged(Q7ElementChangedEvent ev) {
			IQ7ElementDelta delta = ev.getDelta();
			if ((delta.getFlags() & IQ7ElementDelta.F_WORKING_COPY) != 0) {
				return; // Skip working copy delta here
			}
			IQ7ElementDelta[] copy = new IQ7ElementDelta[deltas.length + 1];
			System.arraycopy(deltas, 0, copy, 0, deltas.length);
			copy[deltas.length] = ev.getDelta();
			deltas = copy;
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0, length = this.deltas.length; i < length; i++) {
				IQ7ElementDelta delta = this.deltas[i];
				IQ7ElementDelta[] children = delta.getAffectedChildren();
				int childrenLength = children.length;
				IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
				int resourceDeltasLength = resourceDeltas == null ? 0
						: resourceDeltas.length;
				if (childrenLength == 0 && resourceDeltasLength == 0) {
					buffer.append(delta);
				} else {
					sortDeltas(children);
					for (int j = 0; j < childrenLength; j++) {
						buffer.append(children[j]);
						if (j != childrenLength - 1) {
							buffer.append("\n");
						}
					}
					for (int j = 0; j < resourceDeltasLength; j++) {
						if (j == 0 && buffer.length() != 0) {
							buffer.append("\n");
						}
						buffer.append(resourceDeltas[j]);
						if (j != resourceDeltasLength - 1) {
							buffer.append("\n");
						}
					}
				}
				if (i != length - 1) {
					buffer.append("\n\n");

				}
			}
			return buffer.toString();
		}

		protected void sortDeltas(IQ7ElementDelta[] elementDeltas) {
			Comparer comparer = new Comparer() {
				public int compare(Object a, Object b) {
					IQ7ElementDelta deltaA = (IQ7ElementDelta) a;
					IQ7ElementDelta deltaB = (IQ7ElementDelta) b;
					return deltaA.getElement().getName()
							.compareTo(deltaB.getElement().getName());
				}
			};
			Util.sort(elementDeltas, comparer);
		}
	}

	protected DeltaListener deltaListener = new DeltaListener();

	public AbstractModelTestbase(String testProjectName, String name) {
		super(name);
		this.fTestProjectName = testProjectName;
	}

	public AbstractModelTestbase(String testProjectName, String name, int tabs) {
		super(name);
		this.fTestProjectName = testProjectName;
		this.tabs = tabs;
	}

	protected void setUp() throws Exception {
		super.setUp();
		// if (this.discard) {
		// this.workingCopies = null;
		// }
		// this.discard = true;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		// if (this.discard && this.workingCopies != null) {
		// discardWorkingCopies(this.workingCopies);
		// this.wcOwner = null;
		// }
	}

	public File getSourceWorkspacePath() {
		return new File(getPluginDirectoryPath(), "workspace");
	}

	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected File getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle(this.fTestProjectName)
					.getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Copy the given source directory (and all its contents) to the given
	 * target directory.
	 */
	protected void copyDirectory(File source, File target) throws IOException {
		if (!target.exists()) {
			target.mkdirs();
		}
		File[] files = source.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			File sourceChild = files[i];
			String name = sourceChild.getName();
			if (name.equals("CVS") || name.equals(".svn"))
				continue;
			File targetChild = new File(target, name);
			if (sourceChild.isDirectory()) {
				copyDirectory(sourceChild, targetChild);
			} else {
				copy(sourceChild, targetChild);
			}
		}
	}

	/**
	 * Copy file from src (path to the original file) to dest (path to the
	 * destination file).
	 */
	public static void copy(File src, File dest) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		byte[] buffer = new byte[12 * 1024];
		int read;

		try {
			in = new FileInputStream(src);

			try {
				out = new FileOutputStream(dest);

				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public IProject setUpProject(final String projectName)
			throws CoreException, IOException {
		return setUpProjectTo(projectName, projectName);
	}

	protected IQ7Project setUpQ7ProjectTo(final String projectName,
			final String fromName) throws CoreException, IOException {
		final IProject project = setUpProjectTo(projectName, fromName);
		return RcpttCore.create(project);
	}

	protected IProject setUpProjectTo(final String projectName,
			final String fromName) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		final File sourceWorkspacePath = getSourceWorkspacePath();
		final File targetWorkspacePath = getWorkspaceRoot().getLocation()
				.toFile();

		copyDirectory(new File(sourceWorkspacePath, fromName), new File(
				targetWorkspacePath, projectName));

		return createProject(projectName);
	}

	protected IQ7Project setUpQ7Project(final String projectName)
			throws CoreException, IOException {
		final IProject project = setUpProject(projectName);
		return RcpttCore.create(project);
	}

	public IQ7NamedElement getSourceModule(String projectName, IPath path)
			throws ModelException {
		IQ7Folder folder = getQ7Folder(projectName, path.removeLastSegments(1));
		if (folder == null) {
			return null;
		}
		return folder.getNamedElement(path.lastSegment());
	}

	public IQ7NamedElement getSourceModule(String projectName, String path)
			throws ModelException {
		IQ7Folder folder = getQ7Folder(projectName,
				new Path(path).removeLastSegments(1));
		if (folder == null) {
			return null;
		}
		return folder.getNamedElement(new Path(path).lastSegment().toString());
	}

	public IQ7Folder getQ7Folder(String projectName, IPath path)
			throws ModelException {
		IQ7Project root = getQ7Project(projectName);
		if (root == null) {
			return null;
		}
		return root.getFolder(path);
	}

	/**
	 * Returns the Q7 Project with the given name in this test suite's model.
	 * This is a convenience method.
	 */
	public IQ7Project getQ7Project(String name) {
		IProject project = getProject(name);
		return RcpttCore.create(project);
	}

	// protected void discardWorkingCopies(IQ7NamedElement[] units)
	// throws ModelException {
	// if (units == null)
	// return;
	// for (int i = 0, length = units.length; i < length; i++)
	// if (units[i] != null)
	// units[i].discardWorkingCopy();
	// }

	public void setUpSuite() throws Exception {
		super.setUpSuite();

		// ensure autobuilding is turned off
		disableAutoBulid();
	}

	public static void disableAutoBulid() throws CoreException {
		IWorkspaceDescription deQ7ion = getWorkspace().getDescription();
		if (deQ7ion.isAutoBuilding()) {
			deQ7ion.setAutoBuilding(false);
			getWorkspace().setDescription(deQ7ion);
		}
	}

	protected IQ7NamedElement getSourceModule(String path) {
		return (IQ7NamedElement) RcpttCore.create(getFile(path));
	}

	protected IFile getFile(String path) {
		return getWorkspaceRoot().getFile(new Path(path));
	}

	protected IFolder getFolder(IPath path) {
		return getWorkspaceRoot().getFolder(path);
	}

	protected void deleteFolder(IPath folderPath) throws CoreException {
		deleteResource(getFolder(folderPath));
	}

	/**
	 * Returns the IWorkspace this test suite is running on.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}

	public static IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}

	/*
	 * Create simple project.
	 */
	protected IProject createProject(final String projectName)
			throws CoreException {
		final IProject project = getProject(projectName);
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(create, null);
		return project;
	}

	protected IQ7Project createQ7Project(final String projectName,
			final String[] projects) throws CoreException {
		final IQ7Project[] result = new IQ7Project[1];
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				// create project
				createProject(projectName);

				IProject project = getWorkspaceRoot().getProject(projectName);

				// add natures
				IProjectDescription deQ7ion = project.getDescription();
				deQ7ion.setNatureIds(new String[] { RcpttNature.NATURE_ID });
				project.setDescription(deQ7ion, null);
				result[0] = RcpttCore.create(project);
			}
		};
		getWorkspace().run(create, null);
		return result[0];
	}

	/**
	 * Delete this resource.
	 */
	public void deleteResource(IResource resource) throws CoreException {
		CoreException lastException = null;
		try {
			resource.delete(true, null);
		} catch (CoreException e) {
			lastException = e;
			// just print for info
			System.out.println(e.getMessage());
		} catch (IllegalArgumentException iae) {
			// just print for info
			System.out.println(iae.getMessage());
		}
		int retryCount = 60; // wait 1 minute at most
		while (resource.isAccessible() && --retryCount >= 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			try {
				resource.delete(true, null);
			} catch (CoreException e) {
				lastException = e;
				// just print for info
				System.out.println("Retry " + retryCount + ": "
						+ e.getMessage());
			} catch (IllegalArgumentException iae) {
				// just print for info
				System.out.println("Retry " + retryCount + ": "
						+ iae.getMessage());
			}
		}
		if (!resource.isAccessible())
			return;
		System.err.println("Failed to delete " + resource.getFullPath());
		if (lastException != null) {
			throw lastException;
		}
	}

	protected IFolder createFolder(IPath path) throws CoreException {
		final IFolder folder = getWorkspaceRoot().getFolder(path);
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IContainer parent = folder.getParent();
				if (parent instanceof IFolder && !parent.exists()) {
					createFolder(parent.getFullPath());
				}
				folder.create(true, true, null);
			}
		}, null);

		return folder;
	}

	public void deleteProject(String projectName) throws CoreException {
		IProject project = AbstractModelTestbase.getProject(projectName);
		if (project.exists() && !project.isOpen()) { // force opening so that
			// project can be
			// deleted without
			// logging (see bug
			// 23629)
			project.open(null);
		}
		deleteResource(project);
	}

	/**
	 * Batch deletion of projects
	 */
	protected void deleteProjects(final String[] projectNames)
			throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (projectNames != null) {
					for (int i = 0, max = projectNames.length; i < max; i++) {
						if (projectNames[i] != null)
							deleteProject(projectNames[i]);
					}
				}
			}
		}, null);
	}

	/**
	 * Starts listening to element deltas, and queues them in fgDeltas.
	 */
	public void startDeltas() {
		waitUntilIndexesReady();
		waitForAutoBuild();
		clearDeltas();
		RcpttCore.addElementChangedListener(this.deltaListener);
	}

	/**
	 * Stops listening to element deltas, and clears the current deltas.
	 */
	public void stopDeltas() {
		RcpttCore.removeElementChangedListener(this.deltaListener);
		clearDeltas();
	}

	/**
	 * Empties the current deltas.
	 */
	public void clearDeltas() {
		this.deltaListener.deltas = new IQ7ElementDelta[0];
	}

	protected void sortResources(Object[] resources) {
		Util.Comparer comparer = new Util.Comparer() {
			public int compare(Object a, Object b) {
				IResource resourceA = (IResource) a;
				IResource resourceB = (IResource) b;
				return resourceA.getFullPath().toString()
						.compareTo(resourceB.getFullPath().toString());
			}
		};
		Util.sort(resources, comparer);
	}

	protected void assertDeltas(String message, String expected) {
		String actual = this.deltaListener.toString();
		if (!expected.equals(actual)) {
			System.out.println(actual);
		}
		assertEquals(message, expected, actual);
	}

	protected void assertDeltas(String message, String expected,
			IQ7ElementDelta delta) {
		String actual = delta == null ? "<null>" : delta.toString();
		if (!expected.equals(actual)) {
			System.out.println(actual);
		}
		assertEquals(message, expected, actual);
	}

	/**
	 * Returns the last delta for the given element from the cached delta.
	 */
	protected IQ7ElementDelta getDeltaFor(IQ7Element element) {
		return getDeltaFor(element, false);
	}

	/**
	 * Returns the delta for the given element from the cached delta. If the
	 * boolean is true returns the first delta found.
	 */
	protected IQ7ElementDelta getDeltaFor(IQ7Element element,
			boolean returnFirst) {
		IQ7ElementDelta[] deltas = this.deltaListener.deltas;
		if (deltas == null)
			return null;
		IQ7ElementDelta result = null;
		for (int i = 0; i < deltas.length; i++) {
			IQ7ElementDelta delta = searchForDelta(element,
					this.deltaListener.deltas[i]);
			if (delta != null) {
				if (returnFirst) {
					return delta;
				}
				result = delta;
			}
		}
		return result;
	}

	/**
	 * Returns a delta for the given element in the delta tree
	 */
	protected IQ7ElementDelta searchForDelta(IQ7Element element,
			IQ7ElementDelta delta) {

		if (delta == null) {
			return null;
		}
		if (delta.getElement().equals(element)) {
			return delta;
		}
		for (int i = 0; i < delta.getAffectedChildren().length; i++) {
			IQ7ElementDelta child = searchForDelta(element,
					delta.getAffectedChildren()[i]);
			if (child != null) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Ensures the elements are present after creation.
	 */
	public void assertCreation(IQ7Element[] newElements) {
		for (int i = 0; i < newElements.length; i++) {
			IQ7Element newElement = newElements[i];
			assertTrue("Element should be present after creation",
					newElement.exists());
		}
	}

	/**
	 * Ensures the element is present after creation.
	 */
	public void assertCreation(IQ7Element newElement) {
		assertCreation(new IQ7Element[] { newElement });
	}

	/**
	 * Returns the Q7 Model this test suite is running on.
	 */
	public IQ7Model getQ7Model() {
		return RcpttCore.create(getWorkspaceRoot());
	}

	/**
	 * Wait for autobuild notification to occur
	 */
	public static void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				IJobManager jobManager = Job.getJobManager();
				Job[] jobs = Job.getJobManager().find(
						ResourcesPlugin.FAMILY_AUTO_BUILD);
				jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				jobs = Job.getJobManager().find(
						ResourcesPlugin.FAMILY_AUTO_BUILD);
				for (int j = 0; j < jobs.length; j++) {
					System.out.println("#2" + jobs[j]);
				}
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}

	public void ensureCorrectPositioning(IParent container, IQ7Element sibling,
			IQ7Element positioned) throws ModelException, InterruptedException {
		IQ7Element[] children = container.getChildren();
		if (sibling != null) {
			// find the sibling
			boolean found = false;
			for (int i = 0; i < children.length; i++) {
				if (children[i].equals(sibling)) {
					assertTrue("element should be before sibling", i > 0
							&& children[i - 1].equals(positioned));
					found = true;
					break;
				}
			}
			assertTrue("Did not find sibling", found);
		}
	}

	public static void waitUntilIndexesReady() {
		ModelManager.getModelManager().getIndexManager().waitUntilReady(new NullProgressMonitor());
	}

	public static void storeFile(File dest, URL url) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new BufferedInputStream(url.openStream());

			output = new BufferedOutputStream(new FileOutputStream(dest));

			// Simple copy
			int ch = -1;
			while ((ch = input.read()) != -1) {
				output.write(ch);
			}
		} finally {
			if (input != null) {
				input.close();
			}

			if (output != null) {
				output.close();
			}
		}
	}

	public static File storeToMetadata(Bundle bundle, String name, String path)
			throws IOException {
		File pathFile = RcpttPlugin.getDefault().getStateLocation().append(name)
				.toFile();
		storeFile(pathFile, FileLocator.resolve(bundle.getEntry(path)));
		return pathFile;
	}
}
