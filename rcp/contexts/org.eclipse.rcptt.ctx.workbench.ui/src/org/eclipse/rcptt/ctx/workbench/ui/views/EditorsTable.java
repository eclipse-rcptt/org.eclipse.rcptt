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
package org.eclipse.rcptt.ctx.workbench.ui.views;

import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.scenario.FileEditor;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;
import org.eclipse.rcptt.core.scenario.WorkbenchContext;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.core.workspace.Q7Utils;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.ui.commons.ArrayTransfer;
import org.eclipse.rcptt.ui.commons.EObjectTable;

public class EditorsTable extends EObjectTable {

	private Button closeEditorsButton;

	public EditorsTable(WorkbenchContext context) {
		super(context, ScenarioPackage.Literals.WORKBENCH_CONTEXT__EDITORS);
	}

	public String getName() {
		return "Editors";
	}

	public Image getImage() {
		return null;
	}

	@Override
	public void createControl(Composite parent) {
		closeEditorsButton = new Button(parent, SWT.CHECK);
		closeEditorsButton.setText("Close opened editors");
		IObservableValue<Boolean> closeEditors = EMFObservables.observeValue(getContent(),
				ScenarioPackage.eINSTANCE
						.getWorkbenchContext_CloseEditors());
		dbc.bindValue(WidgetProperties.buttonSelection().observe(closeEditorsButton),
				closeEditors);
		super.createControl(parent);
	}

	public WorkbenchContext getContent() {
		return (WorkbenchContext) content;
	}

	private static class EditorsLabelProvider extends BaseLabelProvider
			implements ILabelProvider {

		public Image getImage(Object element) {
			return Images.getImage(Images.FILE);
		}

		public String getText(Object element) {
			FileEditor editor = (FileEditor) element;
			return editor.getPath();
		}

	}

	@Override
	protected ILabelProvider createLabelProvider() {
		return new EditorsLabelProvider();
	}

	@Override
	protected Object[] handleAdd() {
		List<Object> content = getContents();
		SelectEditorFileDialog dialog = new SelectEditorFileDialog(
				viewer.getControl().getShell(),
				RcpttCore.create(Q7Utils.getLocation(
						getContent()).getProject()),
				content);
		if (dialog.open() == SelectEditorFileDialog.OK) {
			FileEditor editor = ScenarioFactory.eINSTANCE.createFileEditor();
			editor.setPath(dialog.getPath());
			return new FileEditor[] { editor };
		}
		return null;
	}

	private class EditorsTransfer extends ArrayTransfer {
		private final String TYPE_NAME = "org.eclipse.rcptt.core.scenario.FileEditor";
		private final int TYPE_ID = registerType(TYPE_NAME);

		@Override
		protected int[] getTypeIds() {
			return new int[] { TYPE_ID };
		}

		@Override
		protected String[] getTypeNames() {
			return new String[] { TYPE_NAME };
		}

		@Override
		public void javaToNative(Object object, TransferData transferData) {
			Object[] editors = (Object[]) object;
			String[] paths = new String[editors.length];
			for (int i = 0; i < editors.length; i++) {
				paths[i] = ((FileEditor) editors[i]).getPath();
			}
			super.javaToNative(paths, transferData);
		}

		@Override
		public Object nativeToJava(TransferData transferData) {
			Object[] paths = (Object[]) super.nativeToJava(transferData);
			FileEditor[] editors = new FileEditor[paths.length];

			for (int i = 0; i < paths.length; i++) {
				editors[i] = ScenarioFactory.eINSTANCE.createFileEditor();
				editors[i].setPath((String) paths[i]);
			}
			return editors;
		}
	}

	private static EditorsTransfer TRANSFER = null;

	@Override
	protected Class<?> getContentsType() {
		return FileEditor.class;
	}

	@Override
	protected Transfer getContentTransfer() {
		if (null == TRANSFER) {
			TRANSFER = new EditorsTransfer();
		}
		return TRANSFER;
	}

	public String getContentName(Object content) {
		return ((FileEditor) content).getPath();
	}

	public void setCloseEditorsControlEnabled(boolean enabled) {
		if (closeEditorsButton != null && !closeEditorsButton.isDisposed()) {
			closeEditorsButton.setEnabled(enabled);
		}
	}

	@Override
	protected HandleType getAppropriateHandleType() {
		return null;
	}
}
