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
package org.eclipse.rcptt.ui.verification;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationUpdater;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.VerificationTypeManager;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;
import org.eclipse.rcptt.core.scenario.WidgetVerification;
import org.eclipse.rcptt.core.ecl.core.model.CreateWidgetVerificationParam;
import org.eclipse.rcptt.core.ecl.core.model.Q7CoreFactory;
import org.eclipse.rcptt.internal.launching.aut.BaseAutLaunch;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.editors.IQ7Editor;
import org.eclipse.rcptt.ui.editors.IQ7EditorActionsProvider;
import org.eclipse.rcptt.ui.editors.StyledTextNamedElementActions;
import org.eclipse.rcptt.ui.editors.NamedElementEditorActions.INamedElementActions;
import org.eclipse.rcptt.ui.editors.ecl.EclStyledTextHighlighter;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.rcptt.ui.recording.WidgetPicker;
import org.eclipse.rcptt.util.StringUtils;

public abstract class WidgetVerificationEditor implements IQ7Editor<WidgetVerification>, IQ7EditorActionsProvider {

	protected final DataBindingContext dbc = new DataBindingContext();

	private IVerification verification;

	private IManagedForm form;

	private EditorHeader header;

	public IVerification getElement() {
		return verification;
	}

	public WidgetVerification getWidgetVerification() {
		try {
			return (WidgetVerification) getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return null;
		}
	}

	protected Control createWidgetControls(Composite parent, FormToolkit toolkit, IWorkbenchSite site,
			final EditorHeader header) {
		this.header = header;

		Composite box = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().
				grab(true, false).
				applyTo(box);
		GridLayoutFactory.fillDefaults().
				numColumns(3).
				applyTo(box);

		Label label = toolkit.createLabel(box, "Widget:");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		label.setBackground(null);

		selectorText = new StyledText(box, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).
				hint(1, SWT.DEFAULT).applyTo(selectorText);
		selectorText.setFont(JFaceResources.getTextFont());
		new EclStyledTextHighlighter().install(selectorText);
		IObservableValue<String> selector = EMFObservables.observeValue(
		getWidgetVerification(),
		ScenarioPackage.Literals.WIDGET_VERIFICATION__SELECTOR);
		Binding selectorBinding = dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(
				selectorText), selector,
				selectorStrategy, selectorStrategy);
		ControlDecorationSupport.create(selectorBinding, SWT.TOP | SWT.LEFT, box,
				new ControlDecorationUpdater() {
					@Override
					protected void update(ControlDecoration decoration,
							IStatus status) {
						decoration.setMarginWidth(2);
						super.update(decoration, status);
					}
				});

		final Button button = toolkit.createButton(box, "Pick...", SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AutLaunch launch = LaunchUtils.selectAutLaunch();
				if (launch == null)
					return;
				VerificationType type = VerificationTypeManager.getInstance().
						getTypeByVerification(getWidgetVerification());
				String selector = WidgetPicker.activate(null,
						(BaseAutLaunch) launch, type);
				if (selector != null) {
					selectorText.setText(selector);
					header.getRecordButton().notifyListeners(SWT.Selection, new Event());
				}
			}
		});
		return box;
	}

	public void setElement(IQ7NamedElement verification) {
		this.verification = (IVerification) verification;
	}

	public void copyContentFrom(WidgetVerification verification, IProgressMonitor monitor)
			throws CoreException {
		getElement().getType().getMaker().captureContents(
				getElement(), verification, monitor);
	}

	public void save() throws CoreException {
		// Default empty implementation
	}

	public void dispose() {
		dbc.dispose();
	}

	public void select(Object data) {

	}

	public EObject getCreateParam(AutLaunch launch) {
		CreateWidgetVerificationParam p =
				Q7CoreFactory.eINSTANCE.createCreateWidgetVerificationParam();
		p.setSelector(getWidgetVerification().getSelector());
		return p;
	}

	public void setForm(IManagedForm form) {
		this.form = form;
	}

	public IManagedForm getForm() {
		return form;
	}

	@SuppressWarnings("unchecked")
	private final UpdateValueStrategy<String, String> selectorStrategy = new EMFUpdateValueStrategy() {
		{
			UpdateValueStrategy<String, String> typed = this;
			typed.setBeforeSetValidator(new IValidator<String>() {
				public IStatus validate(String value) {
					String selector = (String) value;

					if (StringUtils.isBlank(selector)) {
						header.getReplayButton().setEnabled(false);
						return ValidationStatus
								.warning("Empty widget selector.");
					}

					header.getReplayButton().setEnabled(true);

					return ValidationStatus.ok();
				}
			});
		}
	};

	StyledText selectorText;

	@Override
	public INamedElementActions createActions() {
		return new StyledTextNamedElementActions(selectorText);
	}
}
