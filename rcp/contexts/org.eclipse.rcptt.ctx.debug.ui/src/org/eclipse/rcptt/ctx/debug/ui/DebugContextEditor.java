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
package org.eclipse.rcptt.ctx.debug.ui;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.debug.Collection;
import org.eclipse.rcptt.debug.DebugContext;
import org.eclipse.rcptt.debug.DebugFactory;
import org.eclipse.rcptt.debug.DebugPackage;
import org.eclipse.rcptt.debug.MapValue;
import org.eclipse.rcptt.debug.PrimitiveValue;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.context.BaseContextEditor;
import org.eclipse.rcptt.ui.controls.AbstractEmbeddedComposite;
import org.eclipse.rcptt.ui.controls.SectionWithComposite;
import org.eclipse.rcptt.ui.controls.SectionWithToolbar;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class DebugContextEditor extends BaseContextEditor {
	TreeViewer viewer;

	public DebugContext getContextElement() {
		try {
			return (DebugContext) super.getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return null;
		}
	}

	@Override
	public void copyContentFrom(Context context, IProgressMonitor monitor)
			throws CoreException {
		super.copyContentFrom(context, monitor);
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh(true);
			}
		});
	}

	@Override
	public Control create(Composite parent, FormToolkit toolkit,
			IWorkbenchSite site, EditorHeader header) {
		Section section = new SectionWithComposite("Options", Section.TITLE_BAR
				| Section.TWISTIE | Section.EXPANDED).grabVertical(false)
				.create(parent, toolkit);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);

		Composite composite = (Composite) section.getClient();
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(composite);
		Button noLaunches = toolkit.createButton(composite, "Terminate existing launches", SWT.CHECK);
		@SuppressWarnings("unchecked")
		IObservableValue<Boolean> emfObservable = EMFObservables.observeValue(getContextElement(),
				DebugPackage.Literals.DEBUG_CONTEXT__NO_LAUNCHES);
		ISWTObservableValue<Boolean> noLaunchesSelection = WidgetProperties.buttonSelection().observe(noLaunches);
		dbc.bindValue(noLaunchesSelection,
				emfObservable);

		toolkit.createLabel(composite, "Do not terminate launches of following configurations:");
		Text exceptionsText = toolkit.createText(composite, "", SWT.BORDER);

		GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT)
				.applyTo(exceptionsText);
		@SuppressWarnings("unchecked")
		IObservableValue<String> launchesToLeaveRunning = EMFObservables.observeValue(getContextElement(),
				DebugPackage.Literals.DEBUG_CONTEXT__LAUNCH_EXCEPTIONS);
		ISWTObservableValue<String> launchErrorText = WidgetProperties.text(SWT.Modify).observe(exceptionsText);
		dbc.bindValue(launchErrorText,
				launchesToLeaveRunning);
		dbc.bindValue(WidgetProperties.enabled().observe(exceptionsText), noLaunchesSelection);

		Button noLaunchShortcuts = toolkit.createButton(composite,
				"Clear launch configurations", SWT.CHECK);

		@SuppressWarnings("unchecked")
		IObservableValue<Boolean> noLaunchShortcutsValue = EMFObservables
				.observeValue(
						getContextElement(),
						DebugPackage.Literals.DEBUG_CONTEXT__NO_LAUNCH_SHORTCUTS);
		dbc.bindValue(
				WidgetProperties.buttonSelection().observe(noLaunchShortcuts),
				noLaunchShortcutsValue);

		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		toolkit.createLabel(composite, "Do not delete following configurations:");
		exceptionsText = toolkit.createText(composite, "", SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(exceptionsText);
		@SuppressWarnings("unchecked")
		IObservableValue<String> launchShortcutToLeave = EMFObservables
				.observeValue(
						getContextElement(),
						DebugPackage.Literals.DEBUG_CONTEXT__LAUNCH_SHORTCUT_EXCEPTIONS);
		launchErrorText = WidgetProperties.text(SWT.Modify).observe(exceptionsText);
		dbc.bindValue(
				launchErrorText,
				launchShortcutToLeave);
		dbc.bindValue(WidgetProperties.enabled().observe(exceptionsText), WidgetProperties.buttonSelection().observe(noLaunchShortcuts));
		Button noBreakpoints = toolkit.createButton(composite,
				"Clear breakpoints", SWT.CHECK);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(noBreakpoints);

		@SuppressWarnings("unchecked")
		IObservableValue<Boolean> noBreakpointsValue = EMFObservables.observeValue(getContextElement(),
				DebugPackage.Literals.DEBUG_CONTEXT__NO_BREAKPOINTS);
		dbc.bindValue(WidgetProperties.buttonSelection().observe(noBreakpoints),
				noBreakpointsValue);
		Section section5 = new SectionWithToolbar(new EditableTree("Launch configurations", new ContentProvider()),
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED
				).create(parent, toolkit);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(section5);
		return section;
	}

	private class EditableTree extends AbstractEmbeddedComposite {
		final String name;
		final Image image;
		Tree tree;
		final ContentProvider contentProvider;

		public EditableTree(String name, ContentProvider contentProvider) {
			this.name = name;
			this.contentProvider = contentProvider;
			this.image = null;
		}

		@Override
		public String getName() {
			return name;
		}

		private final Action addTool = new Action() {
			{
				// TODO: is this label adequate?
				setText(Messages.EObjectTable_Add);
				setImageDescriptor(PlatformUI.getWorkbench()
						.getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
				setEnabled(false);
			}

			@Override
			public void run() {
				Collection expandable = getSelectedExpandable();
				if (expandable == null)
					return;
				PrimitiveValue value = DebugFactory.eINSTANCE.createPrimitiveValue();
				if (expandable instanceof MapValue)
					value.setName("New element");
				value.setValue(BoxedValues.box(""));
				expandable.getEntries().add(value);
				viewer.refresh(expandable, true);
				viewer.editElement(value, 1);
			}
		};
		private final Action removeTool = new Action() {
			{
				// TODO: is this label adequate?
				setText(Messages.EObjectTable_Remove);
				setImageDescriptor(PlatformUI.getWorkbench()
						.getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));
				setDisabledImageDescriptor(PlatformUI.getWorkbench()
						.getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE_DISABLED));
				setEnabled(false);
			}

			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Object toSelect = null;
				for (Object o : selection.toList()) {
					if (contentProvider.isRemovable(o)) {
						Object parent = contentProvider.getParent(o);
						// Look for object to be selected instead of removed one
						// Convenient for sequential removals
						toSelect = parent;
						if (parent != null) {
							Object[] children = contentProvider.getChildren(parent);
							for (int position = 0; position < children.length; position++) {
								if (children[position] == o) {
									if (position - 1 >= 0) {
										toSelect = children[position - 1];
									} else if (position + 1 < children.length) {
										toSelect = children[position + 1];
									}
									break;
								}
							}
						}
						contentProvider.remove(o);
						viewer.refresh();
					}
				}
				if (toSelect != null)
					viewer.setSelection(new StructuredSelection(toSelect));
			}
		};

		private Collection getSelectedExpandable() {
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			Object o = selection.getFirstElement();
			while (o != null) {
				if (o instanceof Collection)
					return (Collection) o;
				o = contentProvider.getParent(o);
			}
			return null;
		}

		@Override
		public void createControl(Composite parent) {
			final Tree tree = new Tree(parent, SWT.BORDER | SWT.FULL_SELECTION
					| SWT.MULTI);
			tree.setHeaderVisible(true);
			tree.setLinesVisible(true);
			// toolkit.adapt(tree);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(tree);

			viewer = new TreeViewer(tree);
			contentProvider.install(viewer);
			viewer.setInput(getContextElement());
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					addTool.setEnabled(getSelectedExpandable() != null);
					boolean removable = false;
					if (event.getSelection() instanceof IStructuredSelection) {
						IStructuredSelection selection = (IStructuredSelection) event.getSelection();
						for (Object o : selection.toList()) {
							if (contentProvider.isRemovable(o)) {
								removable = true;
								break;
							}
						}
					}
					removeTool.setEnabled(removable);
				}
			});
		}

		@Override
		public Control getControl() {
			return tree;
		}

		@Override
		protected void fillActions(IToolBarManager manager) {
			manager.add(addTool);
			manager.add(removeTool);

		}

		@Override
		public Image getImage() {
			return image;
		}
	}
}