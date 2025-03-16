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
package org.eclipse.rcptt.ui.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableList;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.scenario.ScenarioProperty;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.PropertySuggestionManager;
import org.eclipse.rcptt.ui.controls.AbstractEmbeddedComposite;
import org.eclipse.rcptt.ui.controls.SuggestionItem;
import org.eclipse.rcptt.ui.editors.NamedElementEditorActions.INamedElementActions;
import org.eclipse.rcptt.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ScenarioProperties extends AbstractEmbeddedComposite implements IQ7EditorActionsProvider {

	private static final String NAME_PROPERTY = "name";
	private static final String VALUE_PROPERTY = "value";
	private static final String[] NAME_PROPERTIES = new String[] { NAME_PROPERTY };
	private static final String[] VALUE_PROPERTIES = new String[] { VALUE_PROPERTY };

	private static ScenarioProperty newParameterItem;
	static {
		newParameterItem = ScenarioFactory.eINSTANCE.createScenarioProperty();
		newParameterItem.setName("Add new property");
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
	private Scenario namedElement;
	
	protected final DataBindingContext dbc = new DataBindingContext();
	private FormToolkit toolkit;

	public void setSelectionAtLine(int line) {
		if (table != null) {
			if (line < table.getItemCount()) {
				table.deselectAll();
				table.select(line);
				table.setFocus();
			}
		}
	}
	
	public ScenarioProperties(Scenario scenario, FormToolkit toolkit) {
		setNamedElement(scenario);
		this.toolkit = toolkit;
	}

	public void dispose() {
		tip.setVisible(false);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		
		control = toolkit.createComposite(parent);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(control);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(control);
		
		viewer = createTable(control, toolkit);
		createPanel(control, toolkit);

		tip = new ToolTip(parent.getShell(), SWT.BALLOON);
		tip.setText("Error");
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


	private TableViewer createTable(Composite parent, FormToolkit toolkit) {
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).hint(100, 50).applyTo(table);
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
					removeSelected();
					break;
				case SWT.INSERT:
					addNew();
					break;
				case SWT.CR:
				case SWT.F2:
					lastNewName = "";
					Object el = ((StructuredSelection) viewer.getSelection()).getFirstElement();
					if (el != null)
						viewer.editElement(el, 0);
					else
						viewer.editElement(newParameterItem, 0);
					break;
				}
			}
		});
		

		final TableViewer viewer = new TableViewer(table);
		
		table.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				TableItem item = table.getItem( new Point(e.x, e.y));
				if( item == null) {
					viewer.cancelEditing();
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		viewer.setContentProvider(new IStructuredContentProvider() {

			public void dispose() {
				// do nothing
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public Object[] getElements(Object inputElement) {
				Scenario ctx = (Scenario) inputElement;

				List<ScenarioProperty> elements = new ArrayList<ScenarioProperty>();
				elements.addAll(ctx.getProperties());
				elements.add(newParameterItem);

				return elements.toArray();
			}

		});

		// --

		TableViewerColumn nameViewerColumn = new TableViewerColumn(viewer, nameColumn);
		Font tableFont = table.getFont();
		FontData fd = tableFont.getFontData()[0];
		fd.setHeight(fd.getHeight()-2);

		Font newParameterFont = new Font(tableFont.getDevice(), fd);
		
		table.addDisposeListener(event -> newParameterFont.dispose());
		nameViewerColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				ScenarioProperty p = (ScenarioProperty) element;
				return p.getName();
			}

			@Override
			public Image getImage(Object element) {
				return element == newParameterItem ? Images.getImage(ISharedImages.IMG_OBJ_ADD)
						: Images.getImage(Images.PARAMETER);
			}

			@Override
			public Font getFont(Object element) {
				if (element == newParameterItem) {
					return newParameterFont;
				} else
					return null;
			}
		});

		nameViewerColumn.setEditingSupport(new EditingSupport(viewer) {

			protected CellEditor getCellEditor(final Object element) {
				final Scenario scenario = getNamedElement();
				final List<SuggestionItem> suggestions = PropertySuggestionManager.getInstance()
						.getScenarioProperties(scenario);
				final PropertyCellEditor editor = new PropertyCellEditor(table, suggestions) {
					public void completeEdit() {
						viewer.applyEditorValue();
					};
				};
				editor.setFilterEnabled(false);
				editor.getControl().addTraverseListener(new TraverseListener() {

					public void keyTraversed(TraverseEvent e) {

						switch (e.detail) {
						case SWT.TRAVERSE_TAB_NEXT:
							viewer.editElement(element, 1);
							e.detail = SWT.TRAVERSE_NONE;
							e.doit = false;
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
					ScenarioProperty p = (ScenarioProperty) element;
					return p.getName();
				}
			}

			protected void setValue(final Object element, Object value) {
				String name = (String) value;
				ScenarioProperty param = (ScenarioProperty) element;
				Scenario ctx = getNamedElement();

				if (isNewAndBlank(element, name)) {
					lastNewName = "";
					return;
				}

//				if (!validateName(element, name)) {
//					table.getDisplay().asyncExec(new Runnable() {
//
//						public void run() {
//							if (!table.isDisposed())
//								viewer.editElement(element, 0);
//						}
//					});

//					return;
//				}

				if (param != newParameterItem) {
					if (!param.getName().equals(name)) {
						Integer index = nameToIndex.get(param.getName());
						nameToIndex.remove(param.getName());
						nameToIndex.put(name, index);

						param.setName(name);
						viewer.update(element, NAME_PROPERTIES);
					}
				} else {
					final ScenarioProperty newParam = ScenarioFactory.eINSTANCE.createScenarioProperty();
					newParam.setName(name);
					newParam.setValue("");

					nameToIndex.put(name, table.getItemCount() - 1);

					ctx.getProperties().add(newParam);
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

		TableViewerColumn valueViewerColumn = new TableViewerColumn(viewer, valueColumn);
		valueViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				ScenarioProperty p = (ScenarioProperty) element;
				return p.getValue();
			}
		});

		valueViewerColumn.setEditingSupport(new EditingSupport(viewer) {

			protected CellEditor getCellEditor(final Object element) {
				final ScenarioProperty param = (ScenarioProperty) element;
				final List<SuggestionItem> suggestions = PropertySuggestionManager.getInstance()
						.getScenarioPropertySuggestions(param.getName());
				final PropertyCellEditor editor = new PropertyCellEditor(table, suggestions) {
					public void completeEdit() {
						viewer.applyEditorValue();
					};
				};
				editor.setFilterEnabled(true);
				editor.getControl().addTraverseListener(new TraverseListener() {

					public void keyTraversed(TraverseEvent e) {
						switch (e.detail) {
						case SWT.TRAVERSE_TAB_PREVIOUS:
								viewer.editElement(element, 0);
							e.detail = SWT.TRAVERSE_NONE;
							e.doit = false;
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
				ScenarioProperty p = (ScenarioProperty) element;
				return p.getValue();
			}

			protected void setValue(Object element, Object value) {
				ScenarioProperty p = (ScenarioProperty) element;
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

		viewer.setInput(getNamedElement());

		return viewer;
	}

	public Scenario getNamedElement() {
		return namedElement;
	}
	
	public void setNamedElement(Scenario namedElement) {
		this.namedElement = namedElement;
	}
	

	private void updateIndexes() {
		nameToIndex.clear();
		EList<ScenarioProperty> params = getNamedElement().getProperties();
		for (int i = 0; i < params.size(); ++i)
			nameToIndex.put(params.get(i).getName(), i);
	}

	private void createPanel(Composite parent, FormToolkit toolkit) {
		Composite panel = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().applyTo(panel);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(panel);

		Button addButton = toolkit.createButton(panel, "Add", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addButton);
		addButton.setImage(Images.getImage(ISharedImages.IMG_OBJ_ADD));
		addButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				addNew();
			}
		});

		Button removeButton = toolkit.createButton(panel, "Remove", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(removeButton);
		removeButton.setImage(Images.getImage(ISharedImages.IMG_ETOOL_DELETE));
		dbc.bindValue(WidgetProperties.enabled().observe(removeButton), new ComputedValue<Boolean>() {

			protected Boolean calculate() {
				IViewerObservableList<?> selection = ViewerProperties.multipleSelection().observe(viewer);
				if (selection.size() == 1 && selection.get(0) == newParameterItem)
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
		Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();

		ArrayList<Object> removed = new ArrayList<Object>();
		for (Object o : selection) {
			if (o == newParameterItem)
				continue;
			getNamedElement().getProperties().remove(o);
			nameToIndex.remove(((ScenarioProperty) o).getName());
			removed.add(o);
		}

		viewer.remove(removed.toArray());
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
			ScenarioProperty a = (ScenarioProperty) e1;
			ScenarioProperty b = (ScenarioProperty) e2;

			if (a == newParameterItem)
				return +1;
			else if (b == newParameterItem)
				return -1;

			if (column == null && nameToIndex.containsKey(a.getName()) && nameToIndex.containsKey(b.getName())) {
				return nameToIndex.get(a.getName()) - nameToIndex.get(b.getName());
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
		}

		@Override
		public void cut() {
			// TODO Auto-generated method stub

		}

		@Override
		public void copy() {
		}

		@Override
		public boolean canUndo() {
			return false;
		}

		@Override
		public boolean canRedo() {
			return false;
		}

		@Override
		public boolean canPaste() {
			return false;
		}

		@Override
		public boolean canCut() {
			return false;
		}

		@Override
		public boolean canCopy() {
			return false;
		}
	};
	private Composite control;

	@Override
	public INamedElementActions createActions() {
		return actions;
	}

	@Override
	public String getName() {
		return "Properties";
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public Control getControl() {
		return control;
	}

	public void update(Scenario element) {
		setNamedElement(element);
		viewer.setInput(element);
	}

}
