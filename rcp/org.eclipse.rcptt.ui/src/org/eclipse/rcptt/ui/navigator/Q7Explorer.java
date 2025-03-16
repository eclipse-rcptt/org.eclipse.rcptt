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
package org.eclipse.rcptt.ui.navigator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.commons.SearchControl;
import org.eclipse.rcptt.ui.navigator.SearchJob.ResultListener;

/**
 * @author RoadRoller
 * 
 */
public class Q7Explorer extends CommonNavigator {
	public static final String ID = "org.eclipse.rcptt.ui.explorer"; //$NON-NLS-1$
	private static final Set<String> TEST_CASE_EXTENSIONS = new HashSet<String>();

	private Q7CommonViewer viewer;
	private final SearchJob job = new SearchJob();

	public Q7Explorer() {
		TEST_CASE_EXTENSIONS.add("test"); //$NON-NLS-1$
		TEST_CASE_EXTENSIONS.add("scenario"); //$NON-NLS-1$
		TEST_CASE_EXTENSIONS.add("suite"); //$NON-NLS-1$
	}

	private final UIJob refreshViewerJob = new UIJob(PlatformUI.getWorkbench()
			.getDisplay(), Messages.Q7Explorer_RefreshJob) {

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (viewer != null) {
				viewer.refresh(false);
				viewer.expandAll();
			}
			return Status.OK_STATUS;
		}
	};

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return viewer = new Q7CommonViewer(getViewSite().getId(), aParent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	@Override
	public void createPartControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 1)
				.applyTo(composite);

		final SearchControl searchControl = new SearchControl(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).indent(1, 2).applyTo(searchControl);

		searchControl.getFilterControl().addFocusListener(
				new TextFocusListener(searchControl.getFilterControl()));

		final Composite container = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1)
				.applyTo(container);
		container.setLayout(new FillLayout());

		super.createPartControl(container);

		// viewer is created now

		// set search result control for search control can pass focus after
		// enter or down arrow
		searchControl.setSearchResultControl(viewer.getControl());
		final IObservableValue<String> searchObservable = 
				WidgetProperties.text(SWT.Modify).observeDelayed(400, searchControl.getFilterControl());
		job.addResultListener(new ResultListener() {
			public void resultAdded(IQ7NamedElement result) {
				final SearchFilter filter = findFilter();
				if (filter == null) {
					return;
				}

				filter.add(result);
				refreshViewerJob.cancel();
				refreshViewerJob.schedule(400);
			}
		});

		searchObservable.addValueChangeListener(new IValueChangeListener<String>() {

			@Override
			public void handleValueChange(ValueChangeEvent<? extends String> event) {
				final SearchFilter filter = findFilter();
				if (filter == null) {
					return;
				}

				job.cancel();
				refreshViewerJob.cancel();

				final String newValue = event.diff.getNewValue();
				if ("".equals(newValue.trim())) { //$NON-NLS-1$
					filter.setEnabled(false);
					filter.clear();
					viewer.refresh(false);
				} else {
					filter.setEnabled(true);
					filter.clear();
					viewer.refresh(false);

					job.setQuery(newValue);
					job.setUser(false);
					job.setPriority(Job.LONG);
					job.schedule();
				}
			}
		});
	}

	private SearchFilter findFilter() {
		for (final ViewerFilter filter : viewer.getFilters()) {
			if (filter instanceof SearchFilter) {
				return (SearchFilter) filter;
			}
		}
		return null;
	}

	public IResource[] filterSelectedResources() {
		IStructuredSelection selection = (IStructuredSelection) getCommonViewer()
				.getSelection();
		SearchFilter filter = findFilter();
		List<IResource> filtered = doFilterSelection(filter,
				selection.toArray());
		if (filtered.size() == 0
				&& selection.size() == 1
				&& selection.getFirstElement() instanceof IFile
				&& IQ7Folder.TEST_SUITE_FILE_EXTENSION
						.equals(((IFile) selection.getFirstElement())
								.getFileExtension())) {
			filtered.add((IFile) selection.getFirstElement());
		}
		return filtered.toArray(new IResource[filtered.size()]);
	}

	private List<IResource> doFilterSelection(SearchFilter filter,
			Object[] objects) {
		List<IResource> list = new ArrayList<IResource>();
		for (Object o : objects) {
			if (o instanceof IContainer) {
				IContainer c = (IContainer) o;
				try {
					if (c.isAccessible()) {
						IResource[] members = c.members();
						if (filter != null) {
							list.addAll(doFilterSelection(filter, filter
									.filter(getCommonViewer(), c, members)));
						} else {
							list.addAll(doFilterSelection(filter, members));
						}
					}
				} catch (CoreException e) {
					Q7UIPlugin.log(e);
				}
			} else if (o instanceof IFile
					&& TEST_CASE_EXTENSIONS.contains(((IFile) o)
							.getFileExtension())) {
				list.add((IFile) o);
			}
		}
		return list;
	}

	/**
	 * Support for text operation actions on search control
	 */
	class TextFocusListener implements FocusListener {
		private final Text fText;

		private Action cutAction;
		private Action copyAction;
		private Action pasteAction;
		private Action deleteAction;
		private Action selectAllAction;

		public TextFocusListener(Text text) {
			fText = text;
			makeActions();

			final FocusListener focusListener = this;
			fText.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					deactivateActions();
					fText.removeFocusListener(focusListener);
					fText.removeDisposeListener(this);
				}
			});
		}

		public void focusLost(FocusEvent e) {
			deactivateActions();
		}

		public void focusGained(FocusEvent e) {
			activateAction();
		}

		private void activateAction() {
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.CUT, cutAction);
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.COPY, copyAction);
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.PASTE, pasteAction);
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.DELETE, deleteAction);
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.SELECT_ALL, selectAllAction);

		}

		private void deactivateActions() {
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.CUT, null);
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.COPY, null);
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.PASTE, null);
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.DELETE, null);
			getViewSite().getActionBars().setGlobalActionHandler(
					ITextEditorActionConstants.SELECT_ALL, null);
		}

		private void makeActions() {
			cutAction = new Action(Messages.Q7Explorer_CutLabel) {
				@Override
				public void run() {
					fText.cut();
				};
			};
			cutAction
					.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);
			copyAction = new Action(Messages.Q7Explorer_CopyLabel) {
				@Override
				public void run() {
					fText.copy();
				};
			};
			copyAction
					.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
			pasteAction = new Action(Messages.Q7Explorer_PasteLabel) {
				@Override
				public void run() {
					fText.paste();
				};
			};
			pasteAction
					.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
			deleteAction = new Action(Messages.Q7Explorer_DeleteLabel) {
				@Override
				public void run() {
					if (fText.getSelectionCount() > 0) {
						String newText = fText.getText().substring(0,
								fText.getSelection().x);
						newText += fText.getText().substring(
								fText.getSelection().y);
						fText.setText(newText);
					}
				};
			};
			deleteAction
					.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
			selectAllAction = new Action(Messages.Q7Explorer_SelectAllLabel) {
				@Override
				public void run() {
					fText.selectAll();
				};
			};
			selectAllAction
					.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
		}
	};
}
