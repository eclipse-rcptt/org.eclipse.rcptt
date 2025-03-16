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
package org.eclipse.rcptt.verifications.text.ui;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.tesla.core.ui.StyleRangeEntry;
import org.eclipse.rcptt.ui.controls.SectionWithComposite;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.utils.RangeUtils;
import org.eclipse.rcptt.ui.verification.WidgetVerificationEditor;
import org.eclipse.rcptt.verifications.text.TextFactory;
import org.eclipse.rcptt.verifications.text.TextPackage;
import org.eclipse.rcptt.verifications.text.TextVerification;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class TextVerificationEditor extends WidgetVerificationEditor {

	public TextVerification getVerification() {
		try {
			return (TextVerification) super.getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return null;
		}
	}

	public void dispose() {
		super.dispose();
	}

	public Control create(Composite parent, FormToolkit toolkit,
			IWorkbenchSite site, EditorHeader header) {

		Section section = new SectionWithComposite("Widget Text",
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED)
				.create(parent, toolkit);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
		Composite client = (Composite) section.getClient();
		GridLayoutFactory.fillDefaults().
				numColumns(1).
				spacing(SWT.DEFAULT, 16).
				applyTo(client);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(client);
		createWidgetControls(client, toolkit, site, header);

		// --

		Composite box = toolkit.createComposite(client);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(box);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(box);
		createControls(toolkit, box);
		return section;
	}

	private void createControls(final FormToolkit toolkit,
			final Composite client) {
		Label introLabel = toolkit.createLabel(client,
				"Text should be:");
		introLabel
				.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		introLabel.setBackground(null);

		// --

		final StyledText text = new StyledText(client,
				SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		text.setFont(JFaceResources.getTextFont());
		GridDataFactory.fillDefaults().grab(true, true).
				hint(1, 1).applyTo(text);
		final Button ignoreStylingCheckbox = toolkit.createButton(client,
				"Ignore text styling and colors", SWT.CHECK);

		IObservableValue<String> verificationText = EMFObservables.observeValue(
		getVerification(),
		TextPackage.Literals.TEXT_VERIFICATION__TEXT);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(
				text), verificationText);

		IObservableList<StyleRangeEntry> observableStyles =
				EMFObservables.observeList(getVerification(),
						TextPackage.Literals.TEXT_VERIFICATION__STYLES);
		observableStyles.addListChangeListener(new IListChangeListener<StyleRangeEntry>() {
			@Override
			public void handleListChange(ListChangeEvent<? extends StyleRangeEntry> event) {
				updateStyling(text, ignoreStylingCheckbox);
			}
		});

		// --

		IObservableValue<Boolean> ignoreSyling = EMFObservables.observeValue(getVerification(),
				TextPackage.Literals.TEXT_VERIFICATION__IGNORE_STYLING);
		final Binding binding = dbc.bindValue(
				WidgetProperties.buttonSelection().observe(ignoreStylingCheckbox),
				ignoreSyling);
		binding.getModel().addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				updateStyling(text, ignoreStylingCheckbox);
			}
		});

		updateStyling(text, ignoreStylingCheckbox);
	}

	@Override
	public void setSelectionAtLine(int line) {
	}

	private void updateStyling(final StyledText text, final Button ignoreStylingCheckbox) {
		final EList<StyleRangeEntry> styles = getVerification().getStyles();
		final boolean noStyling = styles.size() == 0 || getVerification().isIgnoreStyling();

		ignoreStylingCheckbox.setEnabled(styles.size() > 0);

		text.setStyleRange(null);
		text.setEditable(noStyling);
		if (noStyling)
			return;

		StyleRange[] ranges = new StyleRange[styles.size()];
		for (int i = 0; i < styles.size(); ++i)
			ranges[i] = RangeUtils.fromEMF(styles.get(i), text.getDisplay());
		text.setStyleRanges(ranges);
	}

	@Override
	public EObject getCreateParam(AutLaunch launch) {
		TextVerification param = TextFactory.eINSTANCE.createTextVerification();
		param.setSelector(getVerification().getSelector());
		param.setIgnoreStyling(getVerification().isIgnoreStyling());
		return param;
	}
}
