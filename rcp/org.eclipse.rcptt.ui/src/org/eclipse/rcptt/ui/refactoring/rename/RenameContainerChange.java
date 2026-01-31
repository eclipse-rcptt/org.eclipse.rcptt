/*******************************************************************************
 * Copyright (c) 2009, 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.refactoring.rename;

import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.ui.editors.NamedElementEditor;

public class RenameContainerChange extends ResourceChange {

	private static final String RENAME_MESSAGE = Messages.RenameContainerChange_Msg;
	private static final String RENAME_PROGRESS_MESSAGE = Messages.RenameContainerChange_ProgressMsg;

	private final String fNewName;
	private final IPath fResourcePath;
	private final long fStampToRestore;

	private ChangeDescriptor fDescriptor;

	/**
	 * Creates the change.
	 * 
	 * @param resourcePath
	 *            the path of the resource to rename
	 * @param newName
	 *            the new name. Must not be empty.
	 */
	public RenameContainerChange(IPath resourcePath, String newName) {
		this(resourcePath, newName, IResource.NULL_STAMP);
	}

	/**
	 * Creates the change with a time stamp to restore.
	 * 
	 * @param resourcePath
	 *            the path of the resource to rename
	 * @param newName
	 *            the new name. Must not be empty.
	 * @param stampToRestore
	 *            the time stamp to restore or {@link IResource#NULL_STAMP} to
	 *            not restore the time stamp.
	 */
	protected RenameContainerChange(IPath resourcePath, String newName,
			long stampToRestore) {
		if (resourcePath == null || newName == null || newName.length() == 0) {
			throw new IllegalArgumentException();
		}

		fResourcePath = resourcePath;
		fNewName = newName;
		fStampToRestore = stampToRestore;
		fDescriptor = null;
		setValidationMethod(VALIDATE_NOT_DIRTY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.core.refactoring.Change#getDescriptor()
	 */
	public ChangeDescriptor getDescriptor() {
		return fDescriptor;
	}

	/**
	 * Sets the change descriptor to be returned by
	 * {@link Change#getDescriptor()}.
	 * 
	 * @param descriptor
	 *            the change descriptor
	 */
	public void setDescriptor(ChangeDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ltk.core.refactoring.resource.ResourceChange#getModifiedResource
	 * ()
	 */
	protected IResource getModifiedResource() {
		return getResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName() {
		return MessageFormat.format(RENAME_MESSAGE, new Object[] {
				getPathLabel(fResourcePath), getResourceName(fNewName) });
	}

	private static String getPathLabel(IPath path) {
		String label = path.makeRelative().toString();

		return TextProcessor.process(label, "/\\:."); //$NON-NLS-1$
	}

	private static String getResourceName(String resourceName) {
		return TextProcessor.process(resourceName, ":."); //$NON-NLS-1$
	}

	/**
	 * Returns the new name.
	 * 
	 * @return return the new name
	 */
	public String getNewName() {
		return fNewName;
	}

	private IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot()
				.findMember(fResourcePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime
	 * .IProgressMonitor)
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		try {
			pm.beginTask(RENAME_PROGRESS_MESSAGE, 1);

			IResource resource = getResource();
			long currentStamp = resource.getModificationStamp();
			IPath newPath = renamedResourcePath(fResourcePath, fNewName);
			resource.move(newPath, IResource.SHALLOW, pm);
			if (fStampToRestore != IResource.NULL_STAMP) {
				IResource newResource = ResourcesPlugin.getWorkspace()
						.getRoot().findMember(newPath);
				newResource.revertModificationStamp(fStampToRestore);
			}
			String oldName = fResourcePath.lastSegment();

			updateEditorInputs(newPath);

			IResource renamedResource = getResource(newPath);
			if (renamedResource instanceof IProject) {
				updateProjectReferences(pm, (IProject) renamedResource);
			}

			return new RenameContainerChange(newPath, oldName, currentStamp);
		} finally {
			pm.done();
		}
	}

	private IResource getResource(IPath resourcePath) {
		return ResourcesPlugin.getWorkspace().getRoot()
				.findMember(resourcePath);
	}

	private static IPath renamedResourcePath(IPath path, String newName) {
		return path.removeLastSegments(1).append(newName);
	}

	private void updateEditorInputs(final IPath newPath) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return;
				IWorkbenchPage page = window.getActivePage();
				if (page == null)
					return;
				IEditorReference[] references = page.getEditorReferences();
				for (IEditorReference reference : references) {
					IEditorPart part = reference.getEditor(false);
					if (part instanceof NamedElementEditor) {
						final NamedElementEditor editor = (NamedElementEditor) part;
						updateEditorInput(editor, newPath);
					}
				}
			}
		});
	}

	private void updateEditorInput(final NamedElementEditor editor,
			final IPath newPath) {
		// if (editor.getEditorInput() instanceof FileEditorInput) {
		// FileEditorInput editorInput = (FileEditorInput) editor
		// .getEditorInput();
		// IPath fileOldPath = editorInput.getFile().getFullPath();
		// if (fResourcePath.isPrefixOf(fileOldPath)) {
		// IPath newFilePath = newPath.append(fileOldPath
		// .removeFirstSegments(fResourcePath.segmentCount()));
		// IFile newFile = ResourcesPlugin.getWorkspace().getRoot()
		// .getFile(newFilePath);
		// IQ7NamedElement newElement = null;
		// try {
		// newElement = Q7Core.getInstance().getContent(newFile);
		// } catch (IOException e) {
		// Q7UIPlugin.log(e);
		// }
		// if (newElement != null) {
		// editor.replaced(editor.getElement(), newElement);
		// }
		// editor.updateInput(new FileEditorInput(newFile));
		// }
		// }
	}

	private void updateProjectReferences(IProgressMonitor pm,
			IProject renamedProject) throws CoreException {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects(IContainer.INCLUDE_HIDDEN);
		for (IProject currentProject : projects) {
			IProjectDescription description = currentProject.getDescription();
			IProject[] referenceProject = description.getReferencedProjects();
			boolean changed = false;
			for (int i = 0; i < referenceProject.length; i++) {
				if (referenceProject[i].getFullPath().equals(fResourcePath)) {
					referenceProject[i] = renamedProject;
					changed = true;
				}
			}
			if (changed) {
				description.setReferencedProjects(referenceProject);
				currentProject.setDescription(description, pm);
			}
		}
	}

}
