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
package org.eclipse.rcptt.ui.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.ui.panels.Actions;
import org.eclipse.rcptt.ui.wizards.LocationComposite;
import org.eclipse.rcptt.ui.wizards.NewQ7ProjectWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;

public class Q7ContainerSelectionGroup extends Composite {
	private Listener listener;
	private IContainer selectedContainer;

	DataBindingContext dbc = new DataBindingContext();

	private TreeViewer treeViewer;

	public Q7ContainerSelectionGroup(Composite parent, Listener listener,
			String message, int heightHint, int widthHint) {
		super(parent, SWT.NONE);
		this.listener = listener;
		createContents(message, heightHint, widthHint);
	}

	public void containerSelectionChanged(IContainer container) {
		selectedContainer = container;

		// fire an event so the parent can update its controls
		if (listener != null) {
			Event changeEvent = new Event();
			changeEvent.type = SWT.Selection;
			changeEvent.widget = this;
			listener.handleEvent(changeEvent);
		}
	}

	/**
	 * Creates the contents of the composite.
	 * 
	 * @param message
	 * @param heightHint
	 * @param widthHint
	 */
	public void createContents(String message, int heightHint, int widthHint) {
		GridLayoutFactory.fillDefaults().applyTo(this);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(this, SWT.WRAP);
		label.setText(message);
		label.setFont(this.getFont());

		createTreeViewer(heightHint);
		Dialog.applyDialogFont(this);
	}

	private void createNewFolder() {
		BasicNewFolderResourceWizard wizard = new BasicNewFolderResourceWizard();
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, new StructuredSelection(ResourcesPlugin
				.getWorkspace().getRoot()));
		wizard.setWindowTitle(Messages.Q7ContainerSelectionGroup_NewFolder_WindowTitle);
		wizard.init(PlatformUI.getWorkbench(),
				(IStructuredSelection) treeViewer.getSelection());
		WizardDialog dialog = new WizardDialog(getParent().getShell(), wizard);
		if (dialog.open() == WizardDialog.OK) {
			ISelection selection = treeViewer.getSelection();
			treeViewer.refresh();
			treeViewer.setSelection(selection);
		}
	}

	private void createNewProject() {
		NewQ7ProjectWizard wizard = new NewQ7ProjectWizard();
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, new StructuredSelection(ResourcesPlugin
				.getWorkspace().getRoot()));
		wizard.setWindowTitle(Messages.Q7ContainerSelectionGroup_NewQ7Project_WindowTitle);
		WizardDialog dialog = new WizardDialog(getParent().getShell(), wizard);
		if (dialog.open() == WizardDialog.OK) {
			IProject newProject = wizard.getNewProject();
			if (newProject != null) {
				treeViewer.refresh();
				treeViewer.setSelection(new StructuredSelection(newProject));
			}
		}
	}

	@SuppressWarnings("restriction")
	protected void createTreeViewer(int heightHint) {
		// Create drill down.
		LocationComposite drillDown = new LocationComposite(this, SWT.NONE) {
			public void fillToolItems(
					org.eclipse.jface.action.ToolBarManager toolBarMgr) {
				Action newProject = new Action(Messages.Q7ContainerSelectionGroup_NewProject_ActionName) {
					@Override
					public void run() {
						createNewProject();
					}
				};
				newProject.setImageDescriptor(Images
						.getImageDescriptor(Images.NEW_PROJECT));
				toolBarMgr.add(newProject);

				Action newFolder = new Action(Messages.Q7ContainerSelectionGroup_NewFolder_ActionName) {
					@Override
					public void run() {
						createNewFolder();
					}
				};
				newFolder.setImageDescriptor(Images
						.getImageDescriptor(Images.NEW_FOLDER));
				toolBarMgr.add(newFolder);

				DeleteResourceAction deleteAction = new DeleteResourceAction(
						new SameShellProvider(this)) {
					public void run() {
						super.run();
						treeViewer.refresh();
					};
				};
				deleteAction.setImageDescriptor(PlatformUI.getWorkbench()
						.getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));
				deleteAction.setText(""); //$NON-NLS-1$
				deleteAction.setEnabled(false);
				toolBarMgr.add(deleteAction);
				treeViewer.addSelectionChangedListener(deleteAction);

				toolBarMgr.add(new Separator());
				UpdateValueStrategy<Object, Boolean> enablement = UpdateValueStrategy.create(IConverter.create(Object.class, Boolean.class, Objects::nonNull)); 
				dbc.bindValue(Actions.observeEnabled(newFolder),
						ViewerProperties.singleSelection().observe(treeViewer),
						UpdateValueStrategy.never(), enablement);
				// dbc.bindValue(Actions.observeEnabled(deleteAction),
				// ViewersObservables.observeSingleSelection(treeViewer),
				// null, enablement);
			};
		};
		GridData spec = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		spec.widthHint = 300;
		// spec.heightHint = heightHint;
		drillDown.setLayoutData(spec);

		// Create tree viewer inside drill down.
		treeViewer = new TreeViewer(this, SWT.BORDER);
		drillDown.setChildTree(treeViewer);
		Q7ContainerContentProvider cp = new Q7ContainerContentProvider();
		cp.showClosedProjects(false);
		treeViewer.setContentProvider(cp);
		treeViewer.setLabelProvider(WorkbenchLabelProvider
				.getDecoratingWorkbenchLabelProvider());
		treeViewer.setComparator(new ViewerComparator());
		treeViewer.setUseHashlookup(true);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				containerSelectionChanged((IContainer) selection
						.getFirstElement()); // allow null
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object item = ((IStructuredSelection) selection)
							.getFirstElement();
					if (item == null) {
						return;
					}
					if (treeViewer.getExpandedState(item)) {
						treeViewer.collapseToLevel(item, 1);
					} else {
						treeViewer.expandToLevel(item, 1);
					}
				}
			}
		});

		// This has to be done after the viewer has been laid out
		treeViewer.setInput(ResourcesPlugin.getWorkspace());
	}

	public IPath getContainerFullPath() {
		if (selectedContainer == null)
			return null;
		return selectedContainer.getFullPath();

	}

	public void setInitialFocus() {
		treeViewer.getTree().setFocus();
	}

	public void setSelectedContainer(IContainer container) {
		selectedContainer = container;

		// expand to and select the specified container
		List<IContainer> itemsToExpand = new ArrayList<IContainer>();
		IContainer parent = container.getParent();
		while (parent != null) {
			itemsToExpand.add(0, parent);
			parent = parent.getParent();
		}
		treeViewer.setExpandedElements(itemsToExpand.toArray());
		treeViewer.setSelection(new StructuredSelection(container), true);
	}

}
