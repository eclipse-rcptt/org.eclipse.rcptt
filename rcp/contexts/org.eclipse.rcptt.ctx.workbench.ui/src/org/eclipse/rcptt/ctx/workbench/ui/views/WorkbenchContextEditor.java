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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;
import org.eclipse.rcptt.core.scenario.WorkbenchContext;
import org.eclipse.rcptt.core.ecl.core.model.GetPerspectives;
import org.eclipse.rcptt.core.ecl.core.model.PerspectiveInfo;
import org.eclipse.rcptt.core.ecl.core.model.PerspectivesList;
import org.eclipse.rcptt.core.ecl.core.model.Q7CoreFactory;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.ui.commons.ModernElementListSelectionDialog;
import org.eclipse.rcptt.ui.context.BaseContextEditor;
import org.eclipse.rcptt.ui.controls.SectionWithComposite;
import org.eclipse.rcptt.ui.controls.SectionWithToolbar;
import org.eclipse.rcptt.ui.editors.CompositeNamedElementActions;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.editors.IQ7EditorActionsProvider;
import org.eclipse.rcptt.ui.editors.TextNamedElementActions;
import org.eclipse.rcptt.ui.editors.NamedElementEditorActions.INamedElementActions;
import org.eclipse.rcptt.ui.launching.LaunchUtils;

public class WorkbenchContextEditor extends BaseContextEditor implements IQ7EditorActionsProvider {

	private final DataBindingContext dbc = new DataBindingContext();
	private Text perspective;
	private Button resetPerspective;
	private Text nameText;
	private EditorsTable editorsTable;
	private ViewsTable viewsTable;

	public WorkbenchContext getContextElement() {
		try {
			return (WorkbenchContext) super.getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return null;
		}
	}

	public Control create(Composite parent, FormToolkit toolkit,
			IWorkbenchSite site, EditorHeader header) {
		Composite composite = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.spacing(0, 0).applyTo(composite);

		Section section = new SectionWithComposite("Workbench",
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED)
				.numColumns(3).grabVertical(false).create(composite, toolkit);
		Composite c = (Composite) section.getClient();

		createPerspective(c, toolkit);
		createResetPerspectiveOption(c, toolkit);
		createDialogsOption(c, toolkit);
		createClipboardOption(c, toolkit);

		Composite composite2 = toolkit.createComposite(composite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite2);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0)
				.spacing(0, 0).applyTo(composite2);

		viewsTable = new ViewsTable(getContextElement());
		new SectionWithToolbar(viewsTable,
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED).create(
				composite2, toolkit);

		editorsTable = new EditorsTable(getContextElement());
		new SectionWithToolbar(editorsTable, Section.TITLE_BAR
				| Section.TWISTIE | Section.EXPANDED).create(composite2,
				toolkit);

		editorsTable
				.setCloseEditorsControlEnabled(!(isPerspectiveSelected() && resetPerspective
						.getSelection()));

		return composite;
	}

	private void createPerspective(Composite parent, FormToolkit toolkit) {
		Label label = toolkit.createLabel(parent, "Perspective id: ");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		String id = getContextElement().getPerspectiveId();
		if (id == null) {
			id = "";
		}
		perspective = toolkit.createText(parent, id, SWT.BORDER);
		perspective.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		IObservableValue<String> perspectiveId = EMFObservables.observeValue(getContextElement(),
				ScenarioPackage.eINSTANCE
						.getWorkbenchContext_PerspectiveId());
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(perspective),
				perspectiveId,
				new PerspectiveChangeListener(),
				new PerspectiveChangeListener());
		Button button = toolkit.createButton(parent, "Browse...", SWT.PUSH);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = perspective.getShell();
				ModernElementListSelectionDialog dialog = new ModernElementListSelectionDialog(
						shell, new PerspectiveLabelProvider());
				PerspectivesList perspectives = getPerspectives(shell);
				if (perspectives == null) {
					return;
				}
				dialog.setElements(perspectives.getPerspectives().toArray());
				dialog.setTitle("Select a perspective");
				dialog.setMessage("Select a perspective which should be opened");
				dialog.setMultipleSelection(false);
				dialog.setBlockOnOpen(true);
				int result = dialog.open();
				if (result == Window.OK) {
					PerspectiveInfo p = (PerspectiveInfo) dialog
							.getFirstResult();
					getContextElement().setPerspectiveId(p.getId());
				}
			}
		});
	}

	private PerspectivesList getPerspectives(Shell shell) {
		AutLaunch aut = LaunchUtils.selectAutLaunch(shell);
		if (aut == null) {
			return null;
		}

		try {
			GetPerspectives getPerspectives = Q7CoreFactory.eINSTANCE
					.createGetPerspectives();
			Object object = aut.execute(getPerspectives);
			if (!(object instanceof PerspectivesList)) {
				throw new CoreException(
						Q7UIPlugin.createStatus("Unexpected object: " + object
								+ ". Context is expected"));
			}
			return (PerspectivesList) object;
		} catch (InterruptedException e) {
			// Ignore? Really?
			return Q7CoreFactory.eINSTANCE.createPerspectivesList();
		} catch (CoreException e) {
			ErrorDialog.openError(shell, "AUT access error",
					"Couldn't get list of known perspective from AUT",
					e.getStatus());
			return null;
		}
	}

	private void createResetPerspectiveOption(Composite parent,
			FormToolkit toolkit) {
		resetPerspective = toolkit.createButton(parent, "Reset perspective",
				SWT.CHECK);

		resetPerspective.setEnabled(isPerspectiveSelected());

		IObservableValue<Boolean> resetPerspectiveValue = EMFObservables.observeValue(getContextElement(),
				ScenarioPackage.eINSTANCE
						.getWorkbenchContext_ResetPerspective());
		dbc.bindValue(WidgetProperties.buttonSelection().observe(resetPerspective),
				resetPerspectiveValue,
				new ResetPerspectiveChangeListener(),
				new ResetPerspectiveChangeListener());
		GridData data = new GridData();
		data.horizontalSpan = 3;
		resetPerspective.setLayoutData(data);
	}

	private void createDialogsOption(Composite parent, FormToolkit toolkit) {
		final Button noModalDialogs = toolkit.createButton(parent,
				"Close all modal dialogs", SWT.CHECK);
		IObservableValue<Boolean> noModalDialogsValue = EMFObservables.observeValue(getContextElement(),
				ScenarioPackage.eINSTANCE
						.getWorkbenchContext_NoModalDialogs());
		dbc.bindValue(WidgetProperties.buttonSelection().observe(noModalDialogs), noModalDialogsValue);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		noModalDialogs.setLayoutData(data);
	}

	private void createClipboardOption(Composite parent, FormToolkit toolkit) {
		final Button clearClipboard = toolkit.createButton(parent,
				"Clear clipboard", SWT.CHECK);
		IObservableValue<Boolean> clearClipboardValue = EMFObservables.observeValue(getContextElement(),
				ScenarioPackage.eINSTANCE
						.getWorkbenchContext_ClearClipboard());
		dbc.bindValue(WidgetProperties.buttonSelection().observe(clearClipboard),
				clearClipboardValue);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		clearClipboard.setLayoutData(data);
	}

	@Override
	public void dispose() {
		super.dispose();
		dbc.dispose();
	}

	public void setFocus() {
		nameText.setFocus();
	}

	private class PerspectiveChangeListener extends UpdateValueStrategy<String, String> {
		@Override
		public IStatus validateBeforeSet(String value) {
			String sValue = (String) value;
			boolean isPerspectiveSelected = sValue != null
					&& sValue.trim().length() != 0;
			if (resetPerspective != null && !resetPerspective.isDisposed()) {
				resetPerspective.setEnabled(isPerspectiveSelected);
				if (editorsTable != null) {
					editorsTable
							.setCloseEditorsControlEnabled(!(isPerspectiveSelected && resetPerspective
									.getSelection()));
				}
			}

			return super.validateBeforeSet(value);
		}
	}

	private class ResetPerspectiveChangeListener extends UpdateValueStrategy<Boolean, Boolean> {
		@Override
		public IStatus validateBeforeSet(Boolean value) {
			Boolean sValue = (Boolean) value;
			if (editorsTable != null) {
				editorsTable
						.setCloseEditorsControlEnabled(!(isPerspectiveSelected() && sValue));
			}
			return super.validateBeforeSet(value);
		}
	}

	private boolean isPerspectiveSelected() {
		String perspectiveId = getContextElement().getPerspectiveId();
		return perspectiveId != null && perspectiveId.trim().length() > 0;
	}

	@Override
	public INamedElementActions createActions() {
		return new CompositeNamedElementActions(this.editorsTable, this.viewsTable, new TextNamedElementActions(
				perspective)) {
			@Override
			public void update() {
			}
		};
	}
}
