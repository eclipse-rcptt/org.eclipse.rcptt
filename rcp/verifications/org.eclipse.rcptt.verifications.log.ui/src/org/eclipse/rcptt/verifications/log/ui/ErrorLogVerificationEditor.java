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
package org.eclipse.rcptt.verifications.log.ui;


import static com.google.common.base.Strings.nullToEmpty;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.ui.commons.EObjectTable;
import org.eclipse.rcptt.ui.controls.SectionWithToolbar;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.jface.providers.BooleanLabelProvider;
import org.eclipse.rcptt.ui.verification.BaseVerificationEditor;
import org.eclipse.rcptt.verifications.log.ErrorLogVerification;
import org.eclipse.rcptt.verifications.log.LogEntryPredicate;
import org.eclipse.rcptt.verifications.log.LogFactory;
import org.eclipse.rcptt.verifications.log.LogPackage;
import org.eclipse.rcptt.verifications.log.LogPackage.Literals;
import org.eclipse.rcptt.verifications.log.tools.ErrorLogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IGotoMarker;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

public class ErrorLogVerificationEditor extends BaseVerificationEditor implements IGotoMarker {

	private static final String TRANSFER_TYPE = "org.eclipse.rcptt.verifications.log.ui.ErrorLogVerificationEditor.LOG_ENTRY_PREDICATE_TRANSFER";
	private static final Transfer LOG_ENTRY_PREDICATE_TRANSFER = new LocalTransfer() {
		private final int ID = registerType(TRANSFER_TYPE);

		@Override
		protected int[] getTypeIds() {
			return new int[] { ID };
		}

		@Override
		public String[] getTypeNames() {
			return new String[] { TRANSFER_TYPE };
		}

	};

	private ErrorLogVerification getVerification() {
		try {
			return (ErrorLogVerification) super.getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return null;
		}
	}
	
	private static final ICellEditorValidator INTEGER_VALIDATOR = new ICellEditorValidator() {
		@Override
		public String isValid(Object value) {
			String strValue = (String) value;
			if (Strings.isNullOrEmpty(strValue))
				return null;
			strValue = strValue.replaceAll(" ", "");
			try {
				Integer.parseInt(strValue);
			} catch (NumberFormatException e) {
				return e.getLocalizedMessage();
			}
			return null;
		}
	};
	
	class PredicateTable extends EObjectTable {

		private final String title;

		public PredicateTable(EStructuralFeature feature, String title) {
			super(getVerification(), feature);
			this.title = title;
		}
		
		@Override
		public String getName() {
			return title;
		}

		@Override
		protected int getStyle() {
			return super.getStyle() | SWT.FULL_SELECTION;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		protected boolean allowDrag(IStructuredSelection selection) {
			return !selection.isEmpty();
		}

		class SeverityLabelProvider extends BooleanLabelProvider {

			private final int mask;

			public SeverityLabelProvider(int mask) {
				super();
				this.mask = mask;
			}

			@Override
			public boolean isChecked(Object element) {
				return (((LogEntryPredicate)element).getSeverityMask() & mask) != 0;
			}
		}

		class SeverityEditingSupport extends EditingSupport {

			private final int mask;

			public SeverityEditingSupport(ColumnViewer viewer, int mask) {
				super(viewer);
				this.mask = mask;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				return (((LogEntryPredicate) element).getSeverityMask() & mask) != 0;
			}

			@Override
			protected void setValue(Object element, Object value) {
				LogEntryPredicate entry = (LogEntryPredicate) element;
				// TODO: command stack
				if ((Boolean) value) {
					entry.setSeverityMask(entry.getSeverityMask() | mask);
				} else {
					entry.setSeverityMask(entry.getSeverityMask() & ~mask);
				}
				viewer.refresh(element);
			}

		}

		abstract class AttributeEditingSupport extends EditingSupport {

			protected final EAttribute property;

			public AttributeEditingSupport(ColumnViewer viewer, EAttribute property) {
				super(viewer);
				this.property = property;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}			
		}
		
		class StringEditingSupport extends AttributeEditingSupport {

			public StringEditingSupport(ColumnViewer viewer, EAttribute property) {
				super(viewer, property);
			}
			
			@Override
			protected Object getValue(Object element) {
				return MoreObjects.firstNonNull(((EObject) element).eGet(property),
						"");
			}


			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor((Composite) getViewer().getControl());
			}
			
			@Override
			protected void setValue(Object element, Object value) {
				// TODO: command stack
				((EObject) element).eSet(property, value);
				getViewer().refresh(element);
			}

		}


		class IntegerEditingSupport extends AttributeEditingSupport {
			
			public IntegerEditingSupport(ColumnViewer viewer,
					EAttribute property) {
				super(viewer, property);
				// TODO Auto-generated constructor stub
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				TextCellEditor rv = new TextCellEditor((Composite) getViewer().getControl());
				rv.setValidator(INTEGER_VALIDATOR);
				return rv;
			}

			@Override
			protected Object getValue(Object element) {
				int rv = (Integer) ((EObject) element).eGet(property);
				if (property.getDefaultValue().equals(rv))
					return "";
				return String.valueOf(rv);
			}

			@Override
			protected void setValue(Object element, Object value) {
				int code = (Integer) property.getDefaultValue();
				String strValue = nullToEmpty((String) value).replaceAll(" ", "");
				if (!Strings.isNullOrEmpty(strValue)) {
					code = Integer.valueOf(strValue);
				}
				((EObject) element).eSet(property, code);
				getViewer().refresh(element);
			}

		}

		@Override
		protected void configureViewer(TableViewer viewer) {
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.getTable().setHeaderVisible(true);

			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText("Error");
			column.getColumn().setWidth(30);
			column.setLabelProvider(new SeverityLabelProvider(IStatus.ERROR));
			column.setEditingSupport(new SeverityEditingSupport(viewer,
					IStatus.ERROR));

			column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setWidth(30);
			column.getColumn().setText("Warning");
			column.setLabelProvider(new SeverityLabelProvider(IStatus.WARNING));
			column.setEditingSupport(new SeverityEditingSupport(viewer,
					IStatus.WARNING));

			column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText("Information");
			column.getColumn().setWidth(30);
			column.setLabelProvider(new SeverityLabelProvider(IStatus.INFO));
			column.setEditingSupport(new SeverityEditingSupport(viewer,
					IStatus.INFO));

			column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText("Plugin Pattern");
			column.getColumn().setWidth(100);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return ((LogEntryPredicate) element).getPluginPattern();
				}
			});
			column.setEditingSupport(new StringEditingSupport(viewer,
					LogPackage.Literals.LOG_ENTRY_PREDICATE__PLUGIN_PATTERN) {

						@Override
						protected void setValue(Object element, Object value) {
							super.setValue(element, nullToEmpty((String)value).replaceAll(" ", ""));
						}
				
			});

			column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText("Message Pattern");
			column.getColumn().setWidth(300);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return ((LogEntryPredicate) element).getMessagePattern();
				}
			});
			column.setEditingSupport(new StringEditingSupport(viewer,
					LogPackage.Literals.LOG_ENTRY_PREDICATE__MESSAGE_PATTERN));
			
			column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText("Plugin Code");
			column.getColumn().setWidth(50);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					int code = ((LogEntryPredicate) element).getCode();
					if (LogPackage.Literals.LOG_ENTRY_PREDICATE__CODE.getDefaultValue().equals(code))
						return "";
					return ""+code;
				}
			});
			column.setEditingSupport(new IntegerEditingSupport(viewer,
				LogPackage.Literals.LOG_ENTRY_PREDICATE__CODE));


		}

		@Override
		protected ILabelProvider createLabelProvider() {
			return null;
		}

		@Override
		protected Object[] handleAdd() {
			LogEntryPredicate rv = LogFactory.eINSTANCE
					.createLogEntryPredicate();
			rv.setSeverityMask(IStatus.ERROR | IStatus.WARNING | IStatus.INFO);
			rv.setMessagePattern(".*");
			rv.setPluginPattern(".*");
			return new Object[] { rv };
		}

		@Override
		protected Class<?> getContentsType() {
			return LogEntryPredicate.class;
		}

		@Override
		protected Transfer getContentTransfer() {
			return LOG_ENTRY_PREDICATE_TRANSFER;
		}

		@Override
		protected HandleType getAppropriateHandleType() {
			return null;
		}

	}

	PredicateTable requiredTable;
	PredicateTable allowedTable;
	PredicateTable deniedTable;
	
	@Override
	public Control create(Composite parent, FormToolkit toolkit, IWorkbenchSite site, EditorHeader header) {
		int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED;
		Button includeContextsCheckbox = toolkit.createButton(parent, "Take into account Context events", SWT.CHECK);
		GridDataFactory.swtDefaults().hint(SWT.DEFAULT, 50).applyTo(includeContextsCheckbox);
		IObservableValue<Boolean> includeContexts = EMFObservables.observeValue(
				getVerification(), LogPackage.Literals.ERROR_LOG_VERIFICATION__INCLUDE_CONTEXTS);
		dbc.bindValue(WidgetProperties.buttonSelection().observe(includeContextsCheckbox), includeContexts);
		requiredTable = new PredicateTable(
				LogPackage.Literals.ERROR_LOG_VERIFICATION__REQUIRED,
				"Require"
				);
		new SectionWithToolbar(requiredTable, style).create(parent, toolkit)
				.setToolTipText("Every pattern should match at least one log entry for verification to succeed");
		allowedTable = new PredicateTable(
				LogPackage.Literals.ERROR_LOG_VERIFICATION__ALLOWED,
				"Allow");
		new SectionWithToolbar(allowedTable, style).create(parent, toolkit)
				.setToolTipText("These patterns are not denied by next section");
		deniedTable = new PredicateTable(
				LogPackage.Literals.ERROR_LOG_VERIFICATION__DENIED,
				"Deny");
		new SectionWithToolbar(deniedTable, style).create(parent, toolkit).setToolTipText(
				"A log entry matching any of these patterns causes verification to fail if it is not allowed by the previous section");
		return null;
	}
	
	PredicateTable tableByFeature(EReference feature) {
		assert requiredTable != null;
		assert allowedTable != null;
		assert deniedTable != null;
		if (feature == Literals.ERROR_LOG_VERIFICATION__REQUIRED) {
			return requiredTable;
		} else if (feature == Literals.ERROR_LOG_VERIFICATION__ALLOWED) {
			return allowedTable;
		} else if (feature == Literals.ERROR_LOG_VERIFICATION__DENIED) {
			return deniedTable;
		}
		throw new IllegalArgumentException("Not a predicate reference");
	}

	@Override
	public void setSelectionAtLine(int line) {
		LogEntryPredicate predicate = ErrorLogUtil.getPredicateByIndex(getVerification(), line);
		if (predicate == null)
			return;
		EReference feature = (EReference) predicate.eContainingFeature();
		PredicateTable table = tableByFeature(feature);
		table.setSelection(new Object[]{predicate}, true);
		table.getControl().setFocus();
	}

	@Override
	public void gotoMarker(IMarker marker) {
		try {
			Integer line = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			if (line != null)
				setSelectionAtLine(line);
		} catch (CoreException e) {
			RcpttPlugin.log(e);
		}
	}

	@Override
	public void dispose() {
		allowedTable.dispose();
		deniedTable.dispose();
		requiredTable.dispose();
		super.dispose();
	}

	@Override
	public EObject getCreateParam(AutLaunch launch) {
		ErrorLogVerification param = LogFactory.eINSTANCE.createErrorLogVerification();
		param.setIncludeContexts(getVerification().isIncludeContexts());
		return param;
	}

}
