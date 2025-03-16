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
package org.eclipse.rcptt.ctx.preferences.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableList;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.ctx.preferences.ui.wizard.PreferencesAddWizard;
import org.eclipse.rcptt.ctx.preferences.ui.wizard.PreferencesImportWizard;
import org.eclipse.rcptt.internal.preferences.PrefUtils;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.preferences.ListPrefData;
import org.eclipse.rcptt.preferences.PrefData;
import org.eclipse.rcptt.preferences.PrefNode;
import org.eclipse.rcptt.preferences.PreferencesContext;
import org.eclipse.rcptt.preferences.PreferencesPackage;
import org.eclipse.rcptt.preferences.SecurePrefNode;
import org.eclipse.rcptt.preferences.SettingsNode;
import org.eclipse.rcptt.preferences.StringPrefData;
import org.eclipse.rcptt.ui.commons.listcelleditor.StringListCellEditor;
import org.eclipse.rcptt.ui.context.BaseContextEditor;
import org.eclipse.rcptt.ui.controls.SectionWithComposite;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.utils.UIContentAdapter;
import org.eclipse.rcptt.ui.utils.WorkbenchUtils;

public class PreferencesContextEditor extends BaseContextEditor {
	private TreeViewer viewer;
	private EContentAdapter adapter;

	public PreferencesContext getContextElement() {
		try {
			return (PreferencesContext) super.getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return null;
		}
	}

	@SuppressWarnings("unused")
	public Control create(Composite parent, FormToolkit toolkit,
			IWorkbenchSite site, EditorHeader header) {
		Section section = new SectionWithComposite("Preferences",
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED)
				.numColumns(2).create(parent, toolkit);
		Composite composite = (Composite) section.getClient();
		Button cleanPreferencesButton = toolkit.createButton(composite,
				"Clear preferences", SWT.CHECK);
		IObservableValue<Boolean> cleanPreferences = EMFObservables
				.observeValue(
						getContextElement(),
						PreferencesPackage.Literals.PREFERENCES_CONTEXT__CLEAN_PREFERENCES);
		dbc.bindValue(
				WidgetProperties.buttonSelection().observe(cleanPreferencesButton),
				cleanPreferences);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(cleanPreferencesButton);
		Tree tree = createTree(composite, toolkit);

		Composite panel = createPanel(composite, toolkit);
		// panel.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridDataFactory.fillDefaults().grab(false, true).applyTo(panel);

		return section;
	}

	private Tree createTree(Composite parent, FormToolkit toolkit) {
		final Tree tree = new Tree(parent, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).hint(100, 50)
				.applyTo(tree);
		tree.setHeaderVisible(true);
		toolkit.adapt(tree);
		tree.setLinesVisible(true);

		viewer = new TreeViewer(tree);

		viewer.setContentProvider(new PreferencesContentProvider());
		viewer.setLabelProvider(new PreferencesLabelProvider());

		TreeColumn nodeColumn = new TreeColumn(tree, SWT.LEFT);
		nodeColumn.setText("Preference");
		nodeColumn.setWidth(350);

		TreeColumn valueColumn = new TreeColumn(tree, SWT.LEFT);
		valueColumn.setText("Value");
		valueColumn.setWidth(200);

		TreeViewerColumn column = new TreeViewerColumn(viewer, valueColumn);
		column.setEditingSupport(new PreferenceEditingSupport(viewer));
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				if (element instanceof StringPrefData) {
					return ((StringPrefData) element).getValue();
				} else if (element instanceof ListPrefData) {
					return getValuesRepresentation((ListPrefData) element);
				}
				return null;
			}
		});
		viewer.setInput(getContextElement());
		adapter = new UIContentAdapter() {

			protected void changed(Notification notification) {
				viewer.refresh();
			}
		};
		getContextElement().eAdapters().add(adapter);

		viewer.getControl().addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if ((SWT.DEL == e.character) && (0 == e.stateMask)) {
					if (!viewer.getSelection().isEmpty()) {
						removeSelected();
						e.doit = false;
					}
				}
			}
		});
		return tree;
	}

	private Composite createPanel(Composite parent, FormToolkit toolkit) {
		Composite panel = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().applyTo(panel);

		Button importButton = toolkit.createButton(panel,
				"Import Preferences...", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(importButton);
		importButton.setImage(Images.getImage(Images.PREFERENCES_IMPORT));

		importButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				PreferencesImportWizard wizard = new PreferencesImportWizard(
						PreferencesContextEditor.this);
				WizardDialog dialog = new WizardDialog(WorkbenchUtils
						.getShell(), wizard);
				dialog.open();
			}
		});

		Button addButton = toolkit.createButton(panel, "Add...", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addButton);
		addButton.setImage(Images.getImage(Images.PREFERENCES_IMPORT));

		addButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				PreferencesAddWizard wizard = new PreferencesAddWizard(
						PreferencesContextEditor.this);
				WizardDialog dialog = new WizardDialog(WorkbenchUtils
						.getShell(), wizard);
				dialog.open();
			}
		});

		Button removeButton = toolkit.createButton(panel, "Remove", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(removeButton);
		removeButton.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_ETOOL_DELETE));
		dbc.bindValue(WidgetProperties.enabled().observe(removeButton),
				new ComputedValue<Boolean>() {

					protected Boolean calculate() {
						IViewerObservableList<?> selection = ViewerProperties.multipleSelection().observe(viewer);
						return !selection.isEmpty();
					}
				});
		removeButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				removeSelected();
			}
		});
		return panel;
	}

	protected void removeSelected() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		for (Object o : selection.toList()) {
			if (o instanceof SettingsNode) {
				getContextElement().setSettings(null);
				viewer.remove(o);
			} else {
				PrefUtils.remove((EObject) o);
			}
		}
	}

	private class PreferenceEditingSupport extends EditingSupport {

		public PreferenceEditingSupport(TreeViewer treeViewer) {
			super(treeViewer);
		}

		protected boolean canEdit(Object element) {
			return element instanceof StringPrefData
					|| element instanceof ListPrefData;
		}

		protected CellEditor getCellEditor(Object element) {
			if (element instanceof StringPrefData) {
				return new TextCellEditor((Composite) getViewer().getControl());
			} else if (element instanceof ListPrefData) {
				ListPrefData listPrefData = (ListPrefData) element;
				return new StringListCellEditor((Composite) getViewer()
						.getControl(), listPrefData.getValues(),
						listPrefData.getKey());
			}
			return null;
		}

		protected Object getValue(Object element) {
			if (element instanceof StringPrefData) {
				return ((StringPrefData) element).getValue();
			} else if (element instanceof ListPrefData) {
				return getValuesRepresentation((ListPrefData) element);
			}
			return null;
		}

		protected void setValue(Object element, Object value) {
			if (element instanceof StringPrefData) {
				StringPrefData prefData = (StringPrefData) element;
				String input = (String) value;
				if (!input.equals(prefData.getValue())) {
					prefData.setValue(input);
				}
			} else if (element instanceof ListPrefData) {
				ListPrefData listPrefData = (ListPrefData) element;
				List<?> newValues = (List<?>) value;

				listPrefData.getValues().clear();
				for (Iterator<?> it = newValues.iterator(); it.hasNext();) {
					String newVal = (String) it.next();
					listPrefData.getValues().add(newVal);
				}
			}
		}
	}

	private class PreferencesContentProvider implements ITreeContentProvider {

		public boolean hasChildren(Object element) {
			if (element instanceof PrefNode) {
				PrefNode prefNode = (PrefNode) element;
				if (prefNode.getChilds().size() > 0
						|| prefNode.getData().size() > 0) {
					return true;
				}
			}

			return false;
		}

		public Object[] getChildren(Object element) {
			if (element instanceof PrefNode) {
				PrefNode prefNode = (PrefNode) element;
				List<Object> result = new ArrayList<Object>();

				result.addAll(prefNode.getChilds());
				if (result.size() > 0) {
					Collections.sort(result, new Comparator<Object>() {

						public int compare(Object o1, Object o2) {
							return ((PrefNode) o1).getName().compareTo(
									((PrefNode) o2).getName());
						}

					});
				}
				result.addAll(getSortedPrefData(prefNode));

				if (result.size() > 0) {
					return result.toArray();
				}
			}
			return null;
		}

		private List<PrefData> getSortedPrefData(PrefNode prefNode) {
			List<PrefData> result = new ArrayList<PrefData>();
			result.addAll(prefNode.getData());
			if (result.size() > 0) {
				Collections.sort(result, new Comparator<PrefData>() {

					public int compare(PrefData o1, PrefData o2) {
						return o1.getKey().compareTo(o2.getKey());
					}
				});
			}
			return result;
		}

		public Object[] getElements(Object inputElement) {
			PreferencesContext context = (PreferencesContext) inputElement;
			List<PrefNode> result = new ArrayList<PrefNode>();
			result.addAll(context.getContent());
			if (context.getSettings() != null) {
				result.add(context.getSettings());
			}
			return result.toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class PreferencesLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				if (element instanceof SettingsNode) {
					return Images.getImage(Images.SETTINGS);
				} else if (element instanceof SecurePrefNode) {
					return Images.getImage(Images.SECURE_PREFERENCE_NODE);
				} else if (element instanceof PrefNode) {
					PrefNode prefNode = (PrefNode) element;
					if (isRootNode(prefNode)) {
						if (ProjectScope.SCOPE.equals(prefNode.getName())) {
							return Images
									.getImage(Images.PREFERENCES_SCOPE_PROJECT);
						} else if (InstanceScope.SCOPE.equals(prefNode
								.getName())) {
							return Images
									.getImage(Images.PREFERENCES_SCOPE_INSTANCE);
						}
						return Images
								.getImage(Images.PREFERENCES_SCOPE_UNKNOWN);
					}
					return Images.getImage(Images.PREFERENCES_NODE);
				}
				return Images.getImage(Images.PREFERENCES_DATA);
			}
			return null;
		}

		private boolean isRootNode(PrefNode prefNode) {
			PreferencesContext context = getContextElement();
			for (PrefNode node : context.getContent()) {
				if (node.equals(prefNode)) {
					return true;
				}
			}
			return false;
		}

		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				if (element instanceof PrefNode) {
					PrefNode prefNode = (PrefNode) element;
					// workspace preferences
					if (isRootNode(prefNode)
							&& prefNode.getName().equals(InstanceScope.SCOPE)) {
						return "general";
					}
					return prefNode.getName();
				} else if (element instanceof PrefData) {
					return ((PrefData) element).getKey();
				}
				break;
			}
			return null;
		}
	}

	private String getValuesRepresentation(ListPrefData listPrefData) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<String> it = listPrefData.getValues().iterator(); it
				.hasNext();) {
			builder.append(it.next());
			if (it.hasNext()) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	public void addContext(PreferencesContext context) {
		PreferencesContext current = getContextElement();
		mergePrefNodes(current.getContent(), context.getContent());
	}

	private void mergePrefNodes(EList<PrefNode> content,
			EList<PrefNode> newContent) {
		List<PrefNode> toAdd = new ArrayList<PrefNode>();
		for (PrefNode prefNode : newContent) {
			boolean newNode = true;
			for (PrefNode oldPrefNode : content) {
				if (oldPrefNode.getName().equals(prefNode.getName())) {
					// merge
					newNode = false;
					mergePrefNodes(oldPrefNode.getChilds(),
							prefNode.getChilds());
					// Merge data
					mergePrefData(prefNode, oldPrefNode);

				}
			}
			if (newNode) {
				toAdd.add(prefNode);
			}
		}
		content.addAll(toAdd);
	}

	private void mergePrefData(PrefNode prefNode, PrefNode oldPrefNode) {
		EList<PrefData> data = oldPrefNode.getData();
		EList<PrefData> newData = prefNode.getData();
		List<PrefData> toAdd = new ArrayList<PrefData>();
		for (PrefData prefData : newData) {
			boolean ndata = true;
			for (PrefData oldPrefData : data) {
				if (oldPrefData.getKey().equals(prefData.getKey())
						&& oldPrefData.eClass().equals(prefData.eClass())) {
					ndata = false;
					if (oldPrefData instanceof ListPrefData) {
						EList<String> oldValues = ((ListPrefData) oldPrefData)
								.getValues();
						oldValues.clear();
						oldValues.addAll(((ListPrefData) prefData).getValues());
					} else if (oldPrefData instanceof StringPrefData) {
						((StringPrefData) oldPrefData)
								.setValue(((StringPrefData) prefData)
										.getValue());
					}
				}
			}
			if (ndata) {
				toAdd.add(prefData);
			}
		}
		data.addAll(toAdd);
	}
}
