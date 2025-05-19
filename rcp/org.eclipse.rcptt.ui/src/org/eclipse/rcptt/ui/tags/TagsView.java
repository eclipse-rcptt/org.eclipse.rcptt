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
package org.eclipse.rcptt.ui.tags;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.tags.Tag;
import org.eclipse.rcptt.core.tags.TagsPackage;
import org.eclipse.rcptt.core.tags.TagsRegistry;
import org.eclipse.rcptt.core.utils.TagsUtil;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.actions.RenameAction;
import org.eclipse.rcptt.ui.actions.edit.DeleteAction;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class TagsView extends ViewPart {

	private EContentAdapter tagsLabelListener;

	private TagsFilterComposite tagsComposite;
	private TreeViewer tagsViewer;
	private TableViewer listViewer;

	private RenameAction renameAction;
	private DeleteAction deleteAction;
	private IAction replayAction;
	private IAction refreshAction;

	@Override
	public void createPartControl(Composite parent) {
		// Creates controls
		tagsComposite = new TagsFilterComposite(parent, true);
		tagsViewer = tagsComposite.getTagsViewer();
		listViewer = tagsComposite.getListViewer();
		
		// Adds listeners
		tagsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				listViewer.setSelection(null);
				updateSelection((IStructuredSelection) event.getSelection());
			}
		});
		tagsViewer.getControl().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tagsViewer.getSelection();
				if (!selection.isEmpty()) {
					updateSelection(selection);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				final IStructuredSelection iss = (IStructuredSelection) listViewer.getSelection();
				final Object element = iss.getFirstElement();
				if (element instanceof IQ7NamedElement) {
					IQ7NamedElement q7Element = (IQ7NamedElement) element;
					final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					if (activePage != null) {
						try {
							IDE.openEditor(activePage, (IFile) q7Element.getResource());
						} catch (PartInitException e) {
							Q7UIPlugin.log(e);
						}
					}
				}
			}
		});
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelection((IStructuredSelection) event.getSelection());
			}
		});
		listViewer.getControl().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
				if (!selection.isEmpty()) {
					updateSelection(selection);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		});
		
		// Adds actions and menus
		createActions();
		
		MenuManager menuTagsManager = new MenuManager();
		fillTagsMenu(menuTagsManager);
		tagsViewer.getControl().setMenu(menuTagsManager.createContextMenu(tagsViewer.getControl()));
		
		MenuManager menuListManager = new MenuManager();
		fillListMenu(menuListManager);
		listViewer.getControl().setMenu(menuListManager.createContextMenu(listViewer.getControl()));
		
		fillToolbar(getViewSite().getActionBars().getToolBarManager());
		
		// Initializes tags
		Job setInputJob = new UIJob(tagsViewer.getControl().getDisplay(), "Initialize tags view") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (tagsViewer.getControl().isDisposed()) {
					return Status.OK_STATUS;
				}
				final TagsRegistry tagsRegistry = Q7UIPlugin.getDefault().getTags();
				tagsLabelListener = new EContentAdapter() {
					@Override
					public void notifyChanged(final Notification notification) {
						super.notifyChanged(notification);
						if (notification.getFeature() == TagsPackage.Literals.TAG__REFS
								|| notification.getFeature() == TagsPackage.Literals.TAG__VALUE
								|| notification.getFeature() == TagsPackage.Literals.TAG__TAGS) {
							Q7UIPlugin.asyncExec(new Runnable() {
								@Override
								public void run() {
									if (!tagsViewer.getControl().isDisposed()) {
										Object parent = notification.getNotifier();
										while (parent instanceof Tag) {
											tagsViewer.refresh(parent, true);
											parent = TagsUtil.getParentTag((Tag) parent);
										}
										
										tagsComposite.updateList();
									}
								}
							});
						}
					}
				};
				synchronized (tagsRegistry) {
					tagsRegistry.eAdapters().add(tagsLabelListener);
				}

				tagsComposite.tags = EMFObservables.observeList(tagsRegistry, TagsPackage.Literals.TAGS_REGISTRY__TAGS);
				tagsViewer.setInput(tagsComposite.tags);
				updateSelection(StructuredSelection.EMPTY);
				return Status.OK_STATUS;
			}
		};
		setInputJob.schedule();
	}
	
	private void updateSelection(IStructuredSelection selection) {
		if (renameAction != null)
			renameAction.selectionChanged(selection);
		if (deleteAction != null)
			deleteAction.selectionChanged(selection);
		if (replayAction != null) {
			if (tagsViewer.getSelection().isEmpty() && listViewer.getSelection().isEmpty()) {
				replayAction.setEnabled(false);
			} else {
				replayAction.setEnabled(true);
			}
		}
	}

	private void createActions() {
		replayAction = new Action(Messages.TagsView_ReplayActionName, Images.getImageDescriptor(Images.PLAY)) {
			@Override
			public void run() {
				// Try to get selection by focus, if empty then another
				IStructuredSelection selection;
				if (tagsViewer.getControl().isFocusControl()) {
					selection = (IStructuredSelection) tagsViewer.getSelection();
					if (selection.isEmpty())
						selection = (IStructuredSelection) listViewer.getSelection();
				} else {
					selection = (IStructuredSelection) listViewer.getSelection();
					if (selection.isEmpty())
						selection = (IStructuredSelection) tagsViewer.getSelection();
				}
				
				LaunchUtils.launchContext(LaunchUtils.getContext(selection), "run"); //$NON-NLS-1$
			}
		};
		renameAction = new RenameAction(getSite(), tagsViewer.getTree());
		deleteAction = new DeleteAction(getSite());

		refreshAction = new Action(Messages.TagsView_RefreshActionName, Images.getImageDescriptor(Images.REFRESH)) {
			@Override
			public void run() {
				final Object element = ((IStructuredSelection) tagsViewer.getSelection()).getFirstElement();
				tagsViewer.refresh(element, true);
				tagsComposite.updateList();
			}
		};
	}

	private void fillTagsMenu(IMenuManager manager) {
		manager.add(replayAction);
		manager.add(new Separator());
		manager.add(renameAction);
		manager.add(deleteAction);
		manager.add(new Separator());
		manager.add(refreshAction);
	}

	private void fillListMenu(IMenuManager manager) {
		manager.add(replayAction);
	}
	
	private void fillToolbar(IToolBarManager manager) {
		manager.add(replayAction);
		manager.add(deleteAction);
		manager.add(refreshAction);
	}

	@Override
	public void setFocus() {
		if (listViewer.getSelection().isEmpty()) {
			tagsViewer.getControl().setFocus();
		} else {
			listViewer.getControl().setFocus();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		TagsRegistry registry = Q7UIPlugin.getDefault().getTags();
		synchronized (registry) {
			registry.eAdapters().remove(tagsLabelListener);
		}
	}
}
