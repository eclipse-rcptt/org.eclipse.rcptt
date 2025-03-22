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

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IExportedPreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.ctx.preferences.ui.PreferencesContextEditor;
import org.eclipse.rcptt.internal.preferences.PrefUtils;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.preferences.PreferencesContext;
import org.eclipse.rcptt.ui.editors.IQ7Editor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

public class AddWizardPreferencesPage extends WizardPage implements Listener {

	private Text addContentField;

	private String currentMessage;

	public AddWizardPreferencesPage() {
		super("preferencesImportPage");
		setTitle("Add Preferences");
		setDescription("Add preferences.");
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

		addContentField.setFocus();
		Dialog.applyDialogFont(composite);
	}

	private void createDestinationComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label dest = new Label(composite, SWT.NONE);
		dest.setText("Preferences to add:");

		addContentField = new Text(composite, SWT.MULTI | SWT.BORDER);
		addContentField.addListener(SWT.Modify, this);
		addContentField.addListener(SWT.Selection, this);
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(addContentField);

		Label l = new Label(composite, SWT.WRAP);
		l.setText("Plase use following syntax (same as Eclipse Preferences export format):\n"
				+ "/instance/plugin.name/key=value\n"
				+ "/configuration/plugin.name/L9ServerURL=value");
	}

	public boolean finish(IQ7Editor<Context> contextEditor) {
		IPreferencesService service = Platform.getPreferencesService();
		try {
			IExportedPreferences prefs;
			String text = addContentField.getText();
			if (!text.contains("file_export_version=3.0")) {
				text = text + "\n" + "file_export_version=3.0";
			}
			prefs = service.readPreferences(new ByteArrayInputStream(text
					.getBytes()));
			PreferencesContext context = PrefUtils.toContext(prefs);
			((PreferencesContextEditor) contextEditor).addContext(context);
			// contextEditor.copyContentFrom(context, new
			// NullProgressMonitor());

		} catch (CoreException e) {
			Q7UIPlugin.log(e.getMessage(), e);
		} catch (BackingStoreException e) {
			Q7UIPlugin.log(e.getMessage(), e);
		}
		return true;
	}

	public void handleEvent(Event e) {
		updatePageCompletion();
	}

	private boolean isValidToFinish() {
		return addContentField.getText().trim().length() > 0;
	}

	private void updatePageCompletion() {
		boolean pageComplete = determinePageCompletion();
		setPageComplete(pageComplete);
		if (pageComplete) {
			setMessage(null);
		}
	}

	private boolean determinePageCompletion() {
		if (isValidToFinish()) {
			setErrorMessage(null);
			return true;
		}

		setErrorMessage(currentMessage);
		return false;
	}
}
