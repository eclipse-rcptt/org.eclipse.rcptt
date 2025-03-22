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
package org.eclipse.rcptt.ctx.preferences.ui.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IExportedPreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.internal.preferences.PrefUtils;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.preferences.PrefNode;
import org.eclipse.rcptt.preferences.PreferencesContext;
import org.eclipse.rcptt.preferences.PreferencesFactory;
import org.eclipse.rcptt.ui.editors.IQ7Editor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.prefs.BackingStoreException;

public class WizardPreferencesPage extends WizardPage implements Listener {

	private Text destinationNameField;

	private Button destinationBrowseButton;

	private String currentMessage;

	public WizardPreferencesPage() {
		super("preferencesImportPage");
		setTitle("Import Preferences");
		setDescription("Import preferences from the local file system.");
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		createDestinationComposite(composite);

		updatePageCompletion();

		setControl(composite);

		destinationNameField.setFocus();
		Dialog.applyDialogFont(composite);
	}

	private void createDestinationComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label dest = new Label(composite, SWT.NONE);
		dest.setText("From preference file:");

		destinationNameField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		destinationNameField.addListener(SWT.Modify, this);
		destinationNameField.addListener(SWT.Selection, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		destinationNameField.setLayoutData(data);

		destinationBrowseButton = new Button(composite, SWT.PUSH);
		destinationBrowseButton.setText("Browse...");
		destinationBrowseButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL));
		destinationBrowseButton.addListener(SWT.Selection, this);

		new Label(parent, SWT.NONE);
	}

	public boolean finish(IQ7Editor<Context> contextEditor) {
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(getDestinationValue());
		} catch (FileNotFoundException e) {
			Q7UIPlugin.log(e.getMessage(), e);
			MessageDialog.open(MessageDialog.ERROR, getControl().getShell(),
					new String(), e.getLocalizedMessage(), SWT.SHEET);
			return false;
		}
		IPreferencesService service = Platform.getPreferencesService();
		try {
			IExportedPreferences prefs;
			prefs = service.readPreferences(fis);
			PreferencesContext context = PrefUtils.toContext(prefs);

			contextEditor.copyContentFrom(context, new NullProgressMonitor());

		} catch (CoreException e) {
			Q7UIPlugin.log(e.getMessage(), e);
		} catch (BackingStoreException e) {
			Q7UIPlugin.log(e.getMessage(), e);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				Q7UIPlugin.log(e.getMessage(), e);
			}
		}
		return true;
	}

	public void handleEvent(Event e) {
		Widget source = e.widget;

		if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		}

		updatePageCompletion();
	}

	private void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.OPEN
				| SWT.SHEET);
		dialog.setText("Import from File");
		dialog.setFilterPath(getDestinationValue());
		dialog.setFilterExtensions(new String[] { "*.epf", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		String selectedFileName = dialog.open();

		if (selectedFileName != null) {
			destinationNameField.setText(selectedFileName);
		}
	}

	private String getDestinationValue() {
		return destinationNameField.getText().trim();
	}

	private void updatePageCompletion() {
		boolean pageComplete = determinePageCompletion();
		setPageComplete(pageComplete);
		if (pageComplete) {
			setMessage(null);
		}
	}

	private boolean determinePageCompletion() {
		if (isDestinationValid()) {
			setErrorMessage(null);
			return true;
		}

		setErrorMessage(currentMessage);
		return false;
	}

	private boolean isDestinationValid() {
		String destination = getDestinationValue();
		if (destination.length() == 0) {
			currentMessage = "Please select a preference file.";
			return false;
		}

		File file = new File(getDestinationValue());
		if (!file.exists() || file.isDirectory()) {
			currentMessage = "Preference file does not exist or is a directory.";
			return false;
		}
		return true;
	}
}
