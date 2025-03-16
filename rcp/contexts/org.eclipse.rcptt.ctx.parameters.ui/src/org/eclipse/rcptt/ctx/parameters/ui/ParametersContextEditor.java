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
package org.eclipse.rcptt.ctx.parameters.ui;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.parameters.Parameter;
import org.eclipse.rcptt.parameters.ParametersContext;
import org.eclipse.rcptt.parameters.ParametersFactory;
import org.eclipse.rcptt.ui.commons.ArrayTreeContentProvider;
import org.eclipse.rcptt.ui.context.BaseContextEditor;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.editors.IQ7EditorActionsProvider;
import org.eclipse.rcptt.ui.editors.NamedElementEditorActions.INamedElementActions;
import org.eclipse.rcptt.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class ParametersContextEditor extends BaseContextEditor implements
		IQ7EditorActionsProvider {

	private static final String NAME_PROPERTY = "name";
	private static final String VALUE_PROPERTY = "value";
	private static final String[] NAME_PROPERTIES = new String[] { NAME_PROPERTY };
	private static final String[] VALUE_PROPERTIES = new String[] { VALUE_PROPERTY };

	private static Parameter newParameterItem;
	static {
		newParameterItem = ParametersFactory.eINSTANCE.createParameter();
		newParameterItem.setName("Add new parameter");
		newParameterItem.setValue("");
	}

	private Table table;
	private TableViewer viewer;
	private ToolTip tip;
	private String lastNewName = "";

	private TableColumn nameColumn;
	private TableColumn valueColumn;

	// to preserve default sort order while keeping newParameterItem on bottom
	private HashMap<String, Integer> nameToIndex = new HashMap<String, Integer>();

	@Override
	public void setSelectionAtLine(int line) {
		if (table != null) {
			if (line < table.getItemCount()) {
				table.deselectAll();
				table.select(line);
				table.setFocus();
			}
		}
	}

	public ParametersContext getContextElement() {
		try {
			return (ParametersContext) super.getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return null;
		}
	}

	public void dispose() {
		tip.setVisible(false);
		super.dispose();
	}

	public Control create(Composite parent, FormToolkit toolkit,
			IWorkbenchSite site, EditorHeader header) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.TWISTIE | Section.EXPANDED
				| Section.NO_TITLE_FOCUS_BOX);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
		section.setText("Parameters");

		Composite composite = toolkit.createComposite(section);
		section.setClient(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		viewer = createTable(composite, toolkit);
		createPanel(composite, toolkit);

		tip = new ToolTip(parent.getShell(), SWT.BALLOON);
		tip.setText("Error");

		// Composite forToolbar = toolkit.createComposite(section);
		Clipboard clipboard = new Clipboard(parent.getDisplay());
		pasteAction = new PasteAction(clipboard);
		copyAction = new CopyAction(clipboard);
		ToolBarManager toolBarManager = new ToolBarManager(SWT.HORIZONTAL);
		toolBarManager.add(copyAction);
		toolBarManager.add(pasteAction);
		section.setTextClient(toolBarManager.createControl(section));
		MenuManager menu = new MenuManager();
		menu.add(copyAction);
		menu.add(pasteAction);
		menu.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				copyAction.update();
				pasteAction.update();
			}
		});
		menu.update(true);
		viewer.getControl()
				.setMenu(menu.createContextMenu(viewer.getControl()));
		return section;
	}

	private void sortBy(TableColumn column) {
		if (table.getSortColumn() != column) {
			table.setSortColumn(column);
			table.setSortDirection(SWT.UP);
			viewer.setComparator(new Comparator(column, SWT.UP));
		} else if (table.getSortDirection() == SWT.UP) {
			table.setSortDirection(SWT.DOWN);
			viewer.setComparator(new Comparator(column, SWT.DOWN));
		} else {
			table.setSortColumn(null);
			table.setSortDirection(SWT.NONE);
			viewer.setComparator(new Comparator());
		}

		viewer.refresh();
	}

	private static final ISharedImages sharedImages = PlatformUI.getWorkbench()
			.getSharedImages();

	class CopyAction extends Action {
		private final Clipboard clipboard;

		public CopyAction(Clipboard clipboard) {
			super("Copy selected parameters", sharedImages
					.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
			setDisabledImageDescriptor(sharedImages
					.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
			this.clipboard = clipboard;
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					update();
				}
			});
			update();
		}

		@Override
		public void run() {
			IStructuredSelection selection = (IStructuredSelection) viewer
					.getSelection();
			Parameter[] parameters = new Parameter[selection.size()];
			Object[] selected = selection.toArray();
			Properties properties = new Properties();
			for (int i = 0; i < selection.size(); i++) {
				Parameter parameter = (Parameter) selected[i];
				parameters[i] = parameter;
				properties.put(parameter.getName(), parameter.getValue());
			}
			StringWriter writer = new StringWriter();
			try {
				properties.store(writer, "");
			} catch (IOException e) {
				RcpttPlugin.log(e);
				writer = new StringWriter();
			}

			clipboard.setContents(new Object[] { parameters,
					writer.getBuffer().toString() }, new Transfer[] {
					ParametersTransfer.INSTANCE, TextTransfer.getInstance() });
		}

		Object[] getSelection() {
			IStructuredSelection selection = (IStructuredSelection) viewer
					.getSelection();
			return selection.toArray();
		}

		void update() {
			setEnabled(getSelection().length > 0);
		}

	}

	static class Conflict {
		public final String name;
		public final String value1;
		public final String value2;

		public Conflict(String name, String value1, String value2) {
			super();
			this.name = name;
			this.value1 = value1;
			this.value2 = value2;
		}
	}

	private static Map<String, String> toDictionary(
			Collection<Parameter> parameters) {
		Map<String, String> rv = new HashMap<String, String>();
		for (Parameter parameter : parameters) {
			rv.put(parameter.getName(), parameter.getValue());
		}
		return rv;
	}

	private static Collection<Conflict> findConflicts(
			Map<String, String> first, Map<String, String> second) {
		List<Conflict> rv = new ArrayList<Conflict>();
		
		for (Object key: (Diffs.computeMapDiff(first, second).getChangedKeys())) {
			String name = (String) key;
			String value1 = first.get(key);
			String value2 = second.get(name);
			rv.add(new Conflict(name, value1, value2));
		}
		return rv;
	}

	/**
	 * Asks user to select parameters to overwrite
	 * 
	 * @return conflicts to overwrite
	 */
	private Collection<Conflict> resolveConflicts(
			final Collection<Conflict> conflict) {

		CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(
				getForm().getForm().getShell(), new LabelProvider(),
				new ArrayTreeContentProvider()) {

			@Override
			protected CheckboxTreeViewer createTreeViewer(Composite parent) {
				final CheckboxTreeViewer rv = super.createTreeViewer(parent);
				TreeViewerColumn column = new TreeViewerColumn(rv, SWT.NONE);
				column.getColumn().setText("Name");
				column.setLabelProvider(new ColumnLabelProvider() {

					@Override
					public String getText(Object element) {
						return ((Conflict) element).name;
					}

				});
				column = new TreeViewerColumn(rv, SWT.NONE);
				column.getColumn().setText("Original value");
				column.setLabelProvider(new ColumnLabelProvider() {

					@Override
					public String getText(Object element) {
						return ((Conflict) element).value1;
					}

				});
				column = new TreeViewerColumn(rv, SWT.NONE);
				column.getColumn().setText("New value");
				column.setLabelProvider(new ColumnLabelProvider() {

					@Override
					public String getText(Object element) {
						return ((Conflict) element).value2;
					}

				});
				rv.getTree().setHeaderVisible(true);
				rv.refresh();
				for (TreeColumn col : rv.getTree().getColumns()) {
					col.pack();
				}
				rv.addSelectionChangedListener(new ISelectionChangedListener() {
				    @Override
				    public void selectionChanged(final SelectionChangedEvent event) {
				        if (!event.getSelection().isEmpty()) {
				             rv.setSelection(StructuredSelection.EMPTY);
				        }
				    }
				});
				return rv;
			}
		};

		dialog.setTitle("Parameters name conflict");
		dialog.setMessage("Select parameters to overwrite with new values");
		dialog.setInput(conflict);
		if (dialog.open() != Dialog.OK)
			return null;
		Object[] result = dialog.getResult();
		if (result == null)
			return null;
		List<Conflict> rv = new ArrayList<Conflict>();
		for (Object object : result) {
			rv.add((Conflict) object);
		}
		return rv;
	}

	class PasteAction extends Action {
		private final Clipboard clipboard;

		public PasteAction(Clipboard clipboard) {
			super("Paste", PlatformUI.getWorkbench().getSharedImages()
					.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
			setDisabledImageDescriptor(sharedImages
					.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
			this.clipboard = clipboard;
			viewer.getControl().addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					update();
				}

				@Override
				public void focusGained(FocusEvent e) {
					update();
				}
			});
			update();
		}

		protected void update() {
			setEnabled(getPasted().length > 0);
		}

		@Override
		public void run() {
			ParametersContext element = getContextElement();
			List<Parameter> newParameters = asList(getPasted());

			Map<String, String> origMap = toDictionary(element.getParameters());
			Map<String, String> newMap = toDictionary(newParameters);

			
			Collection<Conflict> conflicts = findConflicts(origMap, newMap);

			if (conflicts.size() > 0) {
				// only overwrite conflicts selected by user
				for (Conflict conflict : conflicts) {
					newMap.remove(conflict.name);
				}
				conflicts = resolveConflicts(conflicts);
				if (conflicts == null)
					return;
				for (Conflict conflict : conflicts) {
					newMap.put(conflict.name, conflict.value2);
				}
			}

			Map<String, Parameter> paramMap = new HashMap<String, Parameter>();
			for (Parameter parameter : element.getParameters()) {
				paramMap.put(parameter.getName(), parameter);
			}

			for (Map.Entry<String, String> entry : newMap.entrySet()) {
				Parameter parameter = paramMap.get(entry.getKey());
				if (parameter == null) {
					parameter = ParametersFactory.eINSTANCE.createParameter();
					parameter.setName(entry.getKey());
					element.getParameters().add(parameter);
				}
				parameter.setValue(entry.getValue());
			}

			updateIndexes();
			viewer.refresh();
		}

		private Parameter[] getPasted() {
			Parameter[] rv = (Parameter[]) clipboard
					.getContents(ParametersTransfer.INSTANCE);
			if (rv == null)
				rv = new Parameter[0];
			return rv;
		}
	}

	PasteAction pasteAction;
	CopyAction copyAction;

	private TableViewer createTable(Composite parent, FormToolkit toolkit) {
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).hint(100, 50)
				.applyTo(table);
		table.setHeaderVisible(true);
		toolkit.adapt(table);
		table.setLinesVisible(true);

		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(200);
		nameColumn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortBy(nameColumn);
			}
		});

		valueColumn = new TableColumn(table, SWT.LEFT);
		valueColumn.setText("Value");
		valueColumn.setWidth(350);
		valueColumn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortBy(valueColumn);
			}
		});

		table.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.DEL:
				case SWT.BS:
					removeSelected();
					break;
				case SWT.INSERT:
					addNew();
					break;
				case SWT.CR:
				case SWT.F2:
					lastNewName = "";
					Object el = ((StructuredSelection) viewer.getSelection())
							.getFirstElement();
					if (el != null)
						viewer.editElement(el, 0);
					else
						viewer.editElement(newParameterItem, 0);
					break;
				}
			}
		});

		final TableViewer viewer = new TableViewer(table);
		viewer.setContentProvider(new IStructuredContentProvider() {

			public void dispose() {
				// do nothing
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// do nothing
			}

			public Object[] getElements(Object inputElement) {
				ParametersContext ctx = (ParametersContext) inputElement;

				List<Parameter> elements = new ArrayList<Parameter>();
				elements.addAll(ctx.getParameters());
				elements.add(newParameterItem);

				return elements.toArray();
			}

		});

		// --

		TableViewerColumn nameViewerColumn = new TableViewerColumn(viewer,
				nameColumn);
		nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				Parameter p = (Parameter) element;
				return p.getName();
			}

			public Image getImage(Object element) {
				return element == newParameterItem ? Images.getImageDescriptor(
						ISharedImages.IMG_OBJ_ADD).createImage() : Images
						.getImage(Images.PARAMETER);
			}

			public Font getFont(Object element) {
				if (element == newParameterItem) {
					Font tableFont = table.getFont();
					FontData fd = tableFont.getFontData()[0];
					fd.setStyle(fd.getStyle() | SWT.ITALIC);

					return new Font(tableFont.getDevice(), fd);
				} else
					return null;
			}
		});

		nameViewerColumn.setEditingSupport(new EditingSupport(viewer) {

			protected CellEditor getCellEditor(final Object element) {
				final TextCellEditor editor = new TextCellEditor(table);
				editor.getControl().addTraverseListener(new TraverseListener() {

					public void keyTraversed(TraverseEvent e) {
						Text text = (Text) editor.getControl();

						switch (e.detail) {
						case SWT.TRAVERSE_TAB_NEXT:
							if (validateName(element, text.getText()))
								viewer.editElement(element, 1);
							e.detail = SWT.TRAVERSE_NONE;
							e.doit = false;
							break;
						case SWT.TRAVERSE_ARROW_NEXT:
							if (e.keyCode == SWT.ARROW_DOWN) {
								if (table.getItemCount() > table
										.getSelectionIndex() + 1
										&& (isNewAndBlank(element,
												text.getText()) || validateName(
												element, text.getText())))
									viewer.editElement(viewer
											.getElementAt(table
													.getSelectionIndex() + 1),
											0);
								e.detail = SWT.TRAVERSE_NONE;
								e.doit = false;
							}
							break;
						case SWT.TRAVERSE_ARROW_PREVIOUS:
							if (e.keyCode == SWT.ARROW_UP) {
								if (table.getSelectionIndex() > 0
										&& (isNewAndBlank(element,
												text.getText()) || validateName(
												element, text.getText())))
									viewer.editElement(viewer
											.getElementAt(table
													.getSelectionIndex() - 1),
											0);
								e.detail = SWT.TRAVERSE_NONE;
								e.doit = false;
							}
							break;
						}
					}

				});
				return editor;
			}

			protected boolean canEdit(Object element) {
				return true;
			}

			protected Object getValue(Object element) {
				if (lastNewName.length() > 0)
					return lastNewName;
				else if (element == newParameterItem)
					return "";
				else {
					Parameter p = (Parameter) element;
					return p.getName();
				}
			}

			protected void setValue(final Object element, Object value) {
				String name = (String) value;
				Parameter param = (Parameter) element;
				ParametersContext ctx = getContextElement();

				if (isNewAndBlank(element, name)) {
					lastNewName = "";
					return;
				}

				if (!validateName(element, name)) {
					table.getDisplay().asyncExec(new Runnable() {

						public void run() {
							if (!table.isDisposed())
								viewer.editElement(element, 0);
						}
					});

					return;
				}

				if (param != newParameterItem) {
					if (!param.getName().equals(name)) {
						Integer index = nameToIndex.get(param.getName());
						nameToIndex.remove(param.getName());
						nameToIndex.put(name, index);

						param.setName(name);
						viewer.update(element, NAME_PROPERTIES);
					}
				} else {
					final Parameter newParam = ParametersFactory.eINSTANCE
							.createParameter();
					newParam.setName(name);
					newParam.setValue("");

					nameToIndex.put(name, table.getItemCount() - 1);

					ctx.getParameters().add(newParam);
					viewer.add(newParam);

					viewer.setSelection(new StructuredSelection(newParam), true);
					table.getDisplay().asyncExec(new Runnable() {

						public void run() {
							viewer.editElement(newParam, 1);
						}
					});
				}
			}

		});

		// --

		TableViewerColumn valueViewerColumn = new TableViewerColumn(viewer,
				valueColumn);
		valueViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				Parameter p = (Parameter) element;
				return p.getValue();
			}
		});

		valueViewerColumn.setEditingSupport(new EditingSupport(viewer) {

			protected CellEditor getCellEditor(final Object element) {
				final TextCellEditor editor = new TextCellEditor(table);
				editor.getControl().addTraverseListener(new TraverseListener() {

					public void keyTraversed(TraverseEvent e) {
						Text text = (Text) editor.getControl();

						switch (e.detail) {
						case SWT.TRAVERSE_TAB_PREVIOUS:
							if (validateName(element, text.getText()))
								viewer.editElement(element, 0);
							e.detail = SWT.TRAVERSE_NONE;
							e.doit = false;
							break;
						case SWT.TRAVERSE_ARROW_NEXT:
							if (e.keyCode == SWT.ARROW_DOWN) {
								if (table.getItemCount() > table
										.getSelectionIndex() + 1)
									viewer.editElement(viewer
											.getElementAt(table
													.getSelectionIndex() + 1),
											1);
								e.detail = SWT.TRAVERSE_NONE;
								e.doit = false;
							}
							break;
						case SWT.TRAVERSE_ARROW_PREVIOUS:
							if (e.keyCode == SWT.ARROW_UP) {
								if (table.getSelectionIndex() > 0)
									viewer.editElement(viewer
											.getElementAt(table
													.getSelectionIndex() - 1),
											1);
								e.detail = SWT.TRAVERSE_NONE;
								e.doit = false;
							}
							break;
						}
					}

				});
				return editor;
			}

			protected boolean canEdit(Object element) {
				return element != newParameterItem;
			}

			protected Object getValue(Object element) {
				Parameter p = (Parameter) element;
				return p.getValue();
			}

			protected void setValue(Object element, Object value) {
				Parameter p = (Parameter) element;
				String v = (String) value;
				if (!p.getValue().equals(v)) {
					p.setValue((String) value);
					viewer.update(element, VALUE_PROPERTIES);
				}
			}

		});

		// --

		updateIndexes();
		viewer.setComparator(new Comparator());

		// --

		viewer.setInput(getContextElement());

		return viewer;
	}

	private void updateIndexes() {
		nameToIndex.clear();
		EList<Parameter> params = getContextElement().getParameters();
		for (int i = 0; i < params.size(); ++i)
			nameToIndex.put(params.get(i).getName(), i);
	}

	private void createPanel(Composite parent, FormToolkit toolkit) {
		Composite panel = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().applyTo(panel);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(panel);

		Button addButton = toolkit.createButton(panel, "Add", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addButton);
		addButton.setImage(Images.getImageDescriptor(ISharedImages.IMG_OBJ_ADD)
				.createImage());
		addButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				addNew();
			}
		});

		Button removeButton = toolkit.createButton(panel, "Remove", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(removeButton);
		removeButton.setImage(Images.getImageDescriptor(
				ISharedImages.IMG_ETOOL_DELETE).createImage());
		dbc.bindValue(WidgetProperties.enabled().observe(removeButton),
				new ComputedValue<Boolean>() {

					protected Boolean calculate() {
						IObservableList<Parameter> selection = ViewerProperties.<TableViewer, Parameter>multipleSelection()
								.observe(viewer);
						if (selection.size() == 1
								&& selection.get(0) == newParameterItem)
							return false;
						else
							return !selection.isEmpty();
					}
				});
		removeButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				removeSelected();
			}
		});
	}

	protected void addNew() {
		lastNewName = "";
		viewer.editElement(newParameterItem, 0);
	}

	protected void removeSelected() {
		ParametersContext ctx = getContextElement();
		Object[] selection = ((IStructuredSelection) viewer.getSelection())
				.toArray();

		ArrayList<Object> removed = new ArrayList<Object>();
		for (Object o : selection) {
			if (o == newParameterItem)
				continue;
			ctx.getParameters().remove(o);
			nameToIndex.remove(((Parameter) o).getName());
			removed.add(o);
		}

		viewer.remove(removed.toArray());
	}

	private boolean validateName(final Object element, String name) {
		ParametersContext ctx = getContextElement();
		Parameter param = (Parameter) element;
		String error = null;

		if (StringUtils.isBlank(name))
			error = "Blank name";
		else
			for (Parameter p : ctx.getParameters())
				if (p != param && p.getName().equals(name)) {
					error = "Duplicate parameter name";
					break;
				}

		if (error != null) {
			lastNewName = name;

			Rectangle b = table.getSelection()[0].getBounds();
			Point p = table.toDisplay(b.x, b.y);
			tip.setLocation(p.x + 16, p.y + b.height);

			tip.setMessage(error);
			tip.setVisible(false);
			tip.setVisible(true);

			return false;
		} else {
			tip.setVisible(false);
			lastNewName = "";
			return true;
		}
	}

	private boolean isNewAndBlank(Object element, String name) {
		return element == newParameterItem && StringUtils.isBlank(name);
	}

	private class Comparator extends ViewerComparator {

		private final TableColumn column;
		private final int direction;

		public Comparator() {
			this(null, SWT.NONE);
		}

		public Comparator(TableColumn column, int direction) {
			this.column = column;
			this.direction = direction;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			Parameter a = (Parameter) e1;
			Parameter b = (Parameter) e2;

			if (a == newParameterItem)
				return +1;
			else if (b == newParameterItem)
				return -1;

			if (column == null) {
				return nameToIndex.get(a.getName())
						- nameToIndex.get(b.getName());
			} else {
				String strA = column == nameColumn ? a.getName() : a.getValue();
				String strB = column == nameColumn ? b.getName() : b.getValue();
				int r = strA.compareTo(strB);
				return direction == SWT.UP ? r : -r;
			}
		}

		@Override
		public boolean isSorterProperty(Object element, String property) {
			if (column == nameColumn)
				return NAME_PROPERTY.equals(property);
			if (column == valueColumn)
				return VALUE_PROPERTY.equals(property);
			return false;
		}
	}

	private final INamedElementActions actions = new INamedElementActions() {

		@Override
		public void undo() {
			// TODO Auto-generated method stub

		}

		@Override
		public void redo() {
			// TODO Auto-generated method stub

		}

		@Override
		public void paste() {
			pasteAction.run();
		}

		@Override
		public void cut() {
			// TODO Auto-generated method stub

		}

		@Override
		public void copy() {
			copyAction.run();
		}

		@Override
		public boolean canUndo() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canRedo() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canPaste() {
			pasteAction.update();
			return pasteAction.isEnabled();
		}

		@Override
		public boolean canCut() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canCopy() {
			pasteAction.update();
			return copyAction.isEnabled();
		}
	};

	@Override
	public INamedElementActions createActions() {
		return actions;
	}

}
