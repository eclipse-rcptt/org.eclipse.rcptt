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
package org.eclipse.rcptt.ui.actions.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.ReferencedProjectScope;
import org.eclipse.rcptt.internal.core.model.index.NamedElementCollector;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.actions.RenameDialog;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.rcptt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.rcptt.ui.utils.WriteAccessChecker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.CopyProjectOperation;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.ide.undo.CopyResourcesOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;
import org.eclipse.ui.part.ResourceTransfer;

@SuppressWarnings("restriction")
public class PasteAction extends SelectionListenerAction {

	private final class Q7ResourceCopyOperation extends CopyResourcesOperation {
		private final List<IQ7NamedElement> namedElements;
		private final IPath[] destinationPath;

		private Q7ResourceCopyOperation(IResource[] resources,
				IPath[] destinationPaths, String label,
				List<IQ7NamedElement> namedElements) {
			super(resources, destinationPaths, label);
			this.namedElements = namedElements;
			this.destinationPath = destinationPaths;
		}

		private Q7ResourceCopyOperation(IResource[] resources,
				IPath destinationPath, String label,
				List<IQ7NamedElement> namedElements, IPath targetPaths[]) {
			super(resources, destinationPath, label);
			this.namedElements = namedElements;
			this.destinationPath = targetPaths;
		}

		@Override
		protected void copy(IProgressMonitor monitor, IAdaptable uiInfo)
				throws CoreException {
			super.copy(monitor, uiInfo);
			// We need to collect all new copied named
			// elements
			try {
				for (IPath dest : destinationPath) {
					IQ7Element element = RcpttCore.create(ResourcesPlugin
							.getWorkspace().getRoot().findMember(dest));
					if (element != null && element.exists()) {
						NamedElementCollector collector = new NamedElementCollector();
						element.accept(collector);
						namedElements.addAll(collector.getElements());
					}
				}
			} catch (InterruptedException e) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
		}
	}

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".PasteAction";//$NON-NLS-1$

	/**
	 * The shell in which to show any dialogs.
	 */
	private final Shell shell;

	/**
	 * System clipboard
	 */
	private final Clipboard clipboard;

	/**
	 * Creates a new action.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 * @param clipboard
	 *            the clipboard
	 */
	public PasteAction(Shell shell, Clipboard clipboard) {
		super(Messages.PasteAction_Text);
		Assert.isNotNull(shell);
		Assert.isNotNull(clipboard);
		this.shell = shell;
		this.clipboard = clipboard;
		setToolTipText(Messages.PasteAction_ToolTip);
		setId(PasteAction.ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, "HelpId"); //$NON-NLS-1$		
	}

	/**
	 * Returns the actual target of the paste action. Returns null if no valid
	 * target is selected.
	 * 
	 * @return the actual target of the paste action
	 */
	private IResource getTarget() {
		List<?> selectedResources = getSelectedResources();

		for (int i = 0; i < selectedResources.size(); i++) {
			IResource resource = (IResource) selectedResources.get(i);

			if (resource instanceof IProject && !((IProject) resource).isOpen()) {
				return null;
			}
			if (resource.getType() == IResource.FILE) {
				resource = resource.getParent();
			}
			if (resource != null) {
				return resource;
			}
		}
		return null;
	}

	/**
	 * Returns whether any of the given resources are linked resources.
	 * 
	 * @param resources
	 *            resource to check for linked type. may be null
	 * @return true=one or more resources are linked. false=none of the
	 *         resources are linked
	 */
	private boolean isLinked(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].isLinked()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	@Override
	public void run() {
		if (LaunchUtils.hasLaunchedTestCases()) {
			MessageDialog.openWarning(shell,
					Messages.PasteAction_ErrorDialogTitle,
					Messages.PasteAction_ErrorDialogMsg);
			return;
		}
		if (!RefactoringSaveHelper.checkDirtyEditors(shell)) {
			return;
		}
		// try a resource transfer
		ResourceTransfer resTransfer = ResourceTransfer.getInstance();
		IResource[] resourceData = (IResource[]) clipboard
				.getContents(resTransfer);

		if (resourceData != null && resourceData.length > 0) {
			if (resourceData[0].getType() == IResource.PROJECT) {
				// enablement checks for all projects
				for (int i = 0; i < resourceData.length; i++) {
					CopyProjectOperation operation = new CopyProjectOperation(
							this.shell);
					operation.copyProject((IProject) resourceData[i]);
				}
			} else {
				// enablement should ensure that we always have access to a
				// container
				IContainer container = getContainer();

				try {
					WriteAccessChecker writeAccessChecker = new WriteAccessChecker(this.shell);
					if (!writeAccessChecker.makeResourceWritable(container)) {
						return;
					}
				} catch (CoreException e) {
					Q7UIPlugin.log(e);
				}

				final List<IQ7NamedElement> namedElements = new ArrayList<IQ7NamedElement>();
				List<IResource> normalResources = new ArrayList<IResource>();
				for (IResource resource : resourceData) {
					if (resource instanceof IFile) {
						IQ7NamedElement namedElement = (IQ7NamedElement) RcpttCore
								.create((IFile) resource);
						if (namedElement != null) {
							namedElements.add(namedElement);
							continue;
						}
					}
					normalResources.add(resource);
				}
				// paste named elements, if resources was pasted normally
				IQ7NamedElement[] elements = namedElements
						.toArray(new IQ7NamedElement[namedElements.size()]);
				Map<IQ7NamedElement, IFile> map = RcpttCore.getInstance()
						.getCopyDestinationMap(elements, container);

				if (normalResources.size() > 0) {
					Q7CopyFilesAndFoldersOperation operation = new Q7CopyFilesAndFoldersOperation(
							this.shell) {
						protected org.eclipse.ui.ide.undo.AbstractWorkspaceOperation getUndoableCopyOrMoveOperation(
								IResource[] resources, boolean renamed,
								final IPath... destinationPath) {
							if (renamed) {
								return new Q7ResourceCopyOperation(
										resources,
										destinationPath,
										IDEWorkbenchMessages.CopyFilesAndFoldersOperation_copyTitle,
										namedElements);
							} else {
								List<IPath> dests = new ArrayList<IPath>();
								for (IResource res : resources) {
									dests.add(destinationPath[0].append(res
											.getName()));
								}
								return new Q7ResourceCopyOperation(
										resources,
										destinationPath[0],
										IDEWorkbenchMessages.CopyFilesAndFoldersOperation_copyTitle,
										namedElements,
										dests.toArray(new IPath[dests.size()]));
							}
						};
					};
					IResource[] result = operation.copyResources(
							normalResources
									.toArray(new IResource[normalResources
											.size()]), container);
					if (result.length == 0) {
						// a problem was occurred
						return;
					}
					if (namedElements.size() != elements.length) {
						// Update elements
						elements = namedElements
								.toArray(new IQ7NamedElement[namedElements
										.size()]);
					}
				}

				Map<IQ7NamedElement, String> newNames = new HashMap<IQ7NamedElement, String>();
				Set<String> names = CollectNames(container);
				Set<IPath> filesSkip = new HashSet<IPath>();
				List<IQ7NamedElement> skipElements = new ArrayList<IQ7NamedElement>();

				boolean isSameDestination = isDestinationSameAsSource(
						resourceData, container);
				int overwrite = IDialogConstants.NO_ID;
				IWorkspaceRoot workspaceRoot = container.getWorkspace()
						.getRoot();

				for (Map.Entry<IQ7NamedElement, IFile> e : map.entrySet()) {
					if (e.getValue().exists()) {
						if (isSameDestination) {
							try {
								RenameDialog dialog = new RenameDialog(
										shell,
										e.getKey().getElementName(),
										names,
										e.getValue(),
										Messages.PasteAction_DialogTitle,
										filesSkip,
										Messages.PasteAction_DialogNewNameProposal);
								if (dialog.open() == RenameDialog.OK) {
									newNames.put(e.getKey(),
											dialog.getResultName());
									names.add(dialog.getResultName());
									IPath newFilePath = dialog.getNewFileName();
									filesSkip.add(newFilePath);
									map.put(e.getKey(),
											ResourcesPlugin.getWorkspace()
													.getRoot()
													.getFile(newFilePath));
								} else {
									return; // Cancel operation
								}
							} catch (ModelException e2) {
								Q7UIPlugin.log(e2);
							}
						} else {
							if (overwrite != IDialogConstants.YES_TO_ALL_ID) {
								IResource source = e.getKey().getResource();
								IResource destination = workspaceRoot
										.findMember(e.getValue().getFullPath());
								overwrite = checkOverwrite(source, destination);
								if (overwrite == IDialogConstants.NO_ID) {
									skipElements.add(e.getKey());
								}
								if (overwrite == IDialogConstants.CANCEL_ID) {
									return; // Cancel operation
								}
							}
						}
					}
				}

				// remove skipped from copied elements
				if (skipElements.size() > 0) {
					for (IQ7NamedElement element : skipElements) {
						namedElements.remove(element);
						newNames.remove(element);
						map.remove(element);
					}
					elements = namedElements
							.toArray(new IQ7NamedElement[namedElements.size()]);
				}

				// Check and update file name
				try {
					RcpttCore.getInstance().copy(elements, map, newNames);
				} catch (CoreException e) {
					Q7UIPlugin.log(e);
				}
			}
			return;
		}

		// try a file transfer
		FileTransfer fileTransfer = FileTransfer.getInstance();
		String[] fileData = (String[]) clipboard.getContents(fileTransfer);

		if (fileData != null) {
			// enablement should ensure that we always have access to a
			// container
			IContainer container = getContainer();

			CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(
					this.shell);
			operation.copyFiles(fileData, container);
		}
	}

	/**
	 * Returns the container to hold the pasted resources.
	 */
	private IContainer getContainer() {
		List<?> selection = getSelectedResources();
		if (selection.get(0) instanceof IFile) {
			return ((IFile) selection.get(0)).getParent();
		}
		return (IContainer) selection.get(0);
	}

	/**
	 * The <code>PasteAction</code> implementation of this <code>SelectionListenerAction</code> method enables this
	 * action if a
	 * resource compatible with what is on the clipboard is selected.
	 * 
	 * -Clipboard must have IResource or java.io.File -Projects can always be
	 * pasted if they are open -Workspace folder may not be copied into itself
	 * -Files and folders may be pasted to a single selected folder in open
	 * project or multiple selected files in the same folder
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}

		final IResource[][] clipboardData = new IResource[1][];
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				// clipboard must have resources or files
				ResourceTransfer resTransfer = ResourceTransfer.getInstance();
				clipboardData[0] = (IResource[]) clipboard
						.getContents(resTransfer);
			}
		});
		IResource[] resourceData = clipboardData[0];
		boolean isProjectRes = resourceData != null && resourceData.length > 0
				&& resourceData[0].getType() == IResource.PROJECT;

		if (isProjectRes) {
			for (int i = 0; i < resourceData.length; i++) {
				// make sure all resource data are open projects
				// can paste open projects regardless of selection
				if (resourceData[i].getType() != IResource.PROJECT
						|| ((IProject) resourceData[i]).isOpen() == false) {
					return false;
				}
			}
			return true;
		}

		if (getSelectedNonResources().size() > 0) {
			return false;
		}

		IResource targetResource = getTarget();
		// targetResource is null if no valid target is selected (e.g., open
		// project)
		// or selection is empty
		if (targetResource == null) {
			return false;
		}

		// can paste files and folders to a single selection (file, folder,
		// open project) or multiple file selection with the same parent
		List<?> selectedResources = getSelectedResources();
		if (selectedResources.size() > 1) {
			for (int i = 0; i < selectedResources.size(); i++) {
				IResource resource = (IResource) selectedResources.get(i);
				if (resource.getType() != IResource.FILE) {
					return false;
				}
				if (!targetResource.equals(resource.getParent())) {
					return false;
				}
			}
		}
		if (resourceData != null) {
			// linked resources can only be pasted into projects
			if (isLinked(resourceData)
					&& targetResource.getType() != IResource.PROJECT
					&& targetResource.getType() != IResource.FOLDER) {
				return false;
			}

			if (targetResource.getType() == IResource.FOLDER) {
				// don't try to copy folder to self
				for (int i = 0; i < resourceData.length; i++) {
					if (targetResource.equals(resourceData[i])) {
						return false;
					}
				}
			}
			return true;
		}
		TransferData[] transfers = clipboard.getAvailableTypes();
		FileTransfer fileTransfer = FileTransfer.getInstance();
		for (int i = 0; i < transfers.length; i++) {
			if (fileTransfer.isSupportedType(transfers[i])) {
				return true;
			}
		}
		return false;
	}

	private Set<String> CollectNames(IContainer container) {
		Set<String> names = new HashSet<String>();

		IQ7Project project = RcpttCore.create(container.getProject());
		names.addAll(Arrays.asList(Q7SearchCore
				.findAllNames(new ReferencedProjectScope(project))));

		return names;
	}

	/**
	 * Returns whether any of the given source resources are being recopied to
	 * their current container. Copied from
	 * org.eclipse.ui.actions.CopyFilesAndFoldersOperation.
	 * 
	 * @param sourceResources
	 *            the source resources
	 * @param destination
	 *            the destination container
	 * @return <code>true</code> if at least one of the given source resource's
	 *         parent container is the same as the destination
	 */
	boolean isDestinationSameAsSource(IResource[] sourceResources,
			IContainer destination) {
		IPath destinationLocation = destination.getLocation();

		for (int i = 0; i < sourceResources.length; i++) {
			IResource sourceResource = sourceResources[i];
			if (sourceResource.getParent().equals(destination)) {
				return true;
			} else if (destinationLocation != null) {
				// do thorough check to catch linked resources. Fixes bug 29913.
				IPath sourceLocation = sourceResource.getLocation();
				IPath destinationResource = destinationLocation
						.append(sourceResource.getName());
				if (sourceLocation != null
						&& sourceLocation.isPrefixOf(destinationResource)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check if the user wishes to overwrite the supplied resource or all
	 * resources. Copied from
	 * org.eclipse.ui.actions.CopyFilesAndFoldersOperation.
	 * 
	 * @param source
	 *            the source resource
	 * @param destination
	 *            the resource to be overwritten
	 * @return one of IDialogConstants.YES_ID, IDialogConstants.YES_TO_ALL_ID,
	 *         IDialogConstants.NO_ID, IDialogConstants.CANCEL_ID indicating
	 *         whether the current resource or all resources can be overwritten,
	 *         or if the operation should be canceled.
	 */
	private int checkOverwrite(final IResource source,
			final IResource destination) {
		final int[] result = new int[1];

		// Dialogs need to be created and opened in the UI thread
		Runnable query = new Runnable() {
			public void run() {
				String message;
				int resultId[] = { IDialogConstants.YES_ID,
						IDialogConstants.YES_TO_ALL_ID, IDialogConstants.NO_ID,
						IDialogConstants.CANCEL_ID };
				String labels[] = new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.YES_TO_ALL_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL };

				String[] bindings = new String[] {
						IDEResourceInfoUtils.getLocationText(destination),
						IDEResourceInfoUtils.getDateStringValue(destination),
						IDEResourceInfoUtils.getLocationText(source),
						IDEResourceInfoUtils.getDateStringValue(source) };
				message = NLS
						.bind(IDEWorkbenchMessages.CopyFilesAndFoldersOperation_overwriteWithDetailsQuestion,
								bindings);

				MessageDialog dialog = new MessageDialog(
						shell,
						IDEWorkbenchMessages.CopyFilesAndFoldersOperation_resourceExists,
						null, message, MessageDialog.QUESTION, labels, 0) {
					protected int getShellStyle() {
						return super.getShellStyle() | SWT.SHEET;
					}
				};
				dialog.open();
				if (dialog.getReturnCode() == SWT.DEFAULT) {
					// A window close returns SWT.DEFAULT, which has to be
					// mapped to a cancel
					result[0] = IDialogConstants.CANCEL_ID;
				} else {
					result[0] = resultId[dialog.getReturnCode()];
				}
			}
		};
		shell.getDisplay().syncExec(query);
		return result[0];
	}
}