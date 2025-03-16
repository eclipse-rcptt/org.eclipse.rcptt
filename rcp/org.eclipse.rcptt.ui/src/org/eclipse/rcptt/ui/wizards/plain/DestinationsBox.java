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
package org.eclipse.rcptt.ui.wizards.plain;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.rcptt.ui.commons.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class DestinationsBox {
	private DataBindingContext dbc;

	private final class ModelToSelection extends UpdateValueStrategy<Selection, Boolean> {
		private Selection sel;

		public ModelToSelection(Selection sel) {
			this.sel = sel;
		}

		@Override
		public Boolean convert(Selection value) {
			if (sel.equals(value)) {
				return true;
			}
			return false;
		}
	}

	private final class SelectionToModel extends UpdateValueStrategy<Boolean, Selection> {
		private Selection sel;

		public SelectionToModel(Selection sel) {
			this.sel = sel;
		}

		@Override
		public Selection convert(Boolean value) {
			return sel;
		}
	}

	enum Selection {
		Filesystem, /* Workspace, */Clipboard
	};

	private WritableValue<Selection> fsSelected = new WritableValue<>(Selection.Clipboard,
			Selection.class);

	private WritableValue<String> filesystemPathValue = new WritableValue<>("",
			String.class);

	private Shell shell;

	public DestinationsBox(DataBindingContext dbc) {
		this.dbc = dbc;
	}

	public Composite create(Composite tab) {
		this.shell = tab.getShell();
		Composite cp = new Composite(tab, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(cp);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(cp);

		Button clipboardRadio = new Button(cp, SWT.RADIO);
		clipboardRadio.setText(getClipboardTitle());
		clipboardRadio.setSelection(true);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(clipboardRadio);

		Button filesystemRadio = new Button(cp, SWT.RADIO);
		filesystemRadio.setText(getFileTitle());
		GridDataFactory.fillDefaults().span(2, 1).applyTo(filesystemRadio);
		Text filesystemPath = new Text(cp, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(filesystemPath);
		Button browseFilesystem = new Button(cp, SWT.PUSH);
		browseFilesystem.setText("Browse...");
		browseFilesystem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowseFilesystem();
			}
		});
		GridDataFactory.swtDefaults().applyTo(browseFilesystem);
		SWTFactory.setButtonDimensionHint(browseFilesystem);

		dbc.bindValue(WidgetProperties.buttonSelection().observe(filesystemRadio),
				fsSelected, new SelectionToModel(Selection.Filesystem), new ModelToSelection(Selection.Filesystem));
		dbc.bindValue(WidgetProperties.buttonSelection().observe(clipboardRadio),
				fsSelected, new SelectionToModel(Selection.Clipboard), new ModelToSelection(Selection.Clipboard));

		dbc.bindValue(WidgetProperties.enabled().observe(browseFilesystem),
				fsSelected, new SelectionToModel(Selection.Filesystem), new ModelToSelection(Selection.Filesystem));
		dbc.bindValue(WidgetProperties.enabled().observe(filesystemPath),
				fsSelected, new SelectionToModel(Selection.Filesystem), new ModelToSelection(Selection.Filesystem));

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(filesystemPath),
				filesystemPathValue);
		return cp;
	}

	protected abstract String getFileTitle();

	protected abstract String getClipboardTitle();

	protected void handleBrowseFilesystem() {
		FileDialog fileDialog = new FileDialog(shell, getFileKind());
		fileDialog.setFilterExtensions(new String[] { "*.test", "*.*" });
		fileDialog.setFilterNames(new String[] { "RCPTT Testcase", "All files" });
		fileDialog.setFilterPath(new Path((String) filesystemPathValue
				.getValue()).removeLastSegments(1).toOSString());
		String fileName = fileDialog.open();
		if (fileName != null) {
			filesystemPathValue.setValue(fileName);
		}
	}

	protected abstract int getFileKind();

	public void addChangeListener(final Runnable runnable) {
		IChangeListener listener = new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						runnable.run();
					}
				});
			}
		};
		fsSelected.addChangeListener(listener);
		filesystemPathValue.addChangeListener(listener);
	}

	public Selection getKind() {
		return (Selection) fsSelected.getValue();
	}

	public String getFileSystemPath() {
		return (String) filesystemPathValue.getValue();
	}
}
