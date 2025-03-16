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
package org.eclipse.rcptt.verifications.time.ui;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationUpdater;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.controls.SectionWithComposite;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.verification.BaseVerificationEditor;
import org.eclipse.rcptt.verifications.time.TimePackage;
import org.eclipse.rcptt.verifications.time.TimeVerification;

public class TimeVerificationEditor extends BaseVerificationEditor {

	public TimeVerification getVerification() {
		try {
			return (TimeVerification) super.getElement().getNamedElement();
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
		Section section = new SectionWithComposite("Execution Time",
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED)
				.create(parent, toolkit);

		Composite client = (Composite) section.getClient();
		GridLayoutFactory.fillDefaults().numColumns(4).
				spacing(8, 16).applyTo(client);
		createMinutesAndSecondsControls(toolkit, client);
		createIncludeContextsControls(toolkit, client);
		return section;
	}

	private void createIncludeContextsControls(FormToolkit toolkit, Composite client) {
		final Button includeContextsCheckbox = toolkit.createButton(client,
				"Take into account the time spent executing contexts", SWT.CHECK);
		GridDataFactory.fillDefaults().span(4, 1).applyTo(includeContextsCheckbox);

		IObservableValue<Boolean> includeContexts = EMFObservables.observeValue(
		getVerification(),
		TimePackage.Literals.TIME_VERIFICATION__INCLUDE_CONTEXTS);
		dbc.bindValue(WidgetProperties.buttonSelection().observe(
				includeContextsCheckbox), includeContexts);
	}

	private void createMinutesAndSecondsControls(final FormToolkit toolkit,
			final Composite client) {
		Label introLabel = toolkit.createLabel(client,
				"Test case execution time should be less than:");
		introLabel
				.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		introLabel.setBackground(null);
		GridDataFactory.fillDefaults().span(4, 1).applyTo(introLabel);

		Text minutesText = toolkit.createText(client, "00" /* for computeSize */,
				SWT.BORDER | SWT.RIGHT);
		GridDataFactory.fillDefaults().
				hint(minutesText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x, SWT.DEFAULT).
				indent(32, 0).applyTo(minutesText);

		IObservableValue<Integer> minutes = EMFObservables.observeValue(
		getVerification(),
		TimePackage.Literals.TIME_VERIFICATION__MINUTES);
		@SuppressWarnings("unchecked")
		UpdateValueStrategy<Integer, String> numberToString = new EMFUpdateValueStrategy();
		Binding minutesBinding = dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(
				minutesText), minutes,
				minutesStrategy, numberToString);
		ControlDecorationSupport.create(minutesBinding, SWT.TOP | SWT.LEFT, client,
				new ControlDecorationUpdater() {
					@Override
					protected void update(ControlDecoration decoration,
							IStatus status) {
						decoration.setMarginWidth(2);
						super.update(decoration, status);
					}
				});

		Label minutesLabel = toolkit.createLabel(client, "minute(s) and");
		minutesLabel
				.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		minutesLabel.setBackground(null);

		Text secondsText = toolkit.createText(client, "00" /* for computeSize */,
				SWT.BORDER | SWT.RIGHT);
		GridDataFactory.fillDefaults().
				hint(secondsText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x, SWT.DEFAULT).
				applyTo(secondsText);
		IObservableValue<Integer> seconds = EMFObservables.observeValue(
		getVerification(),
		TimePackage.Literals.TIME_VERIFICATION__SECONDS);
		Binding secondsBinding = dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(
				secondsText), seconds,
				secondsStrategy, numberToString);
		ControlDecorationSupport.create(secondsBinding, SWT.TOP | SWT.LEFT, client,
				new ControlDecorationUpdater() {
					@Override
					protected void update(ControlDecoration decoration,
							IStatus status) {
						decoration.setMarginWidth(2);
						super.update(decoration, status);
					}
				});

		Label secondsLabel = toolkit.createLabel(client, "second(s)");
		secondsLabel
				.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		secondsLabel.setBackground(null);
	}

	@SuppressWarnings("unchecked")
	private UpdateValueStrategy<String, Integer> minutesStrategy = new EMFUpdateValueStrategy() {
		{
			UpdateValueStrategy<String, Integer> thisTyped = this;
			thisTyped.setBeforeSetValidator(new IValidator<Integer>() {
				public IStatus validate(Integer value) {
					Integer minutes = value;

					if (minutes < 0 || minutes > 99)
						return ValidationStatus
								.error("Value should be in [0, 99] interval.");

					return ValidationStatus.ok();
				}
			});
		}
	};

	@SuppressWarnings("unchecked")
	private UpdateValueStrategy<String, Integer> secondsStrategy = new EMFUpdateValueStrategy() {
		{
			UpdateValueStrategy<String, Integer> thisTyped = this;
			thisTyped.setBeforeSetValidator(new IValidator<Integer>() {
				@Override
				public IStatus validate(Integer value) {
					Integer seconds = (Integer) value;

					if (seconds < 0 || seconds > 60)
						return ValidationStatus
								.error("Value should be in [0, 60] interval.");

					return ValidationStatus.ok();
				}
			});
		}
	};

	@Override
	public void setSelectionAtLine(int line) {
	}

}
