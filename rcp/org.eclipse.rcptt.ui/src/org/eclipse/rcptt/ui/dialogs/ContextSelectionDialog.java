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
package org.eclipse.rcptt.ui.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredList;

import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.AllProjectScope;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.scenario.SuperContext;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.ReferencedProjectScope;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.actions.Q7ElementLabelProvider;
import org.eclipse.rcptt.ui.commons.ModernElementListSelectionDialog;

public abstract class ContextSelectionDialog extends
		ModernElementListSelectionDialog {

	private static final int NEW_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int CAPTURE_ID = IDialogConstants.CLIENT_ID + 2;
	private IContext[] allContexts;
	private IContext[] fromRequiredProjects;

	private final IProject project;
	private final IQ7NamedElement element;
	private Label filterTextLabel;
	ContextType type = null;

	public ContextSelectionDialog(Shell parent, IQ7NamedElement element,
			IProject project) {
		super(parent, createLabelProvider(element, project));
		this.project = project;
		this.element = element;
		try {
			if (element != null
					&& element.getNamedElement() instanceof SuperContext) {
				String child = ((SuperContext) element.getNamedElement()).getChildType();
				type = ContextTypeManager.getInstance().getTypeById(child);
			}
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
		}
		setTitle(Messages.ContextSelectionDialog_Title);
		setMessage(Messages.ContextSelectionDialog_Msg);
		setMultipleSelection(true);
		setElements(hasProject() ? getFromRequiredProjects() : getAllContexts());
	}

	private static ILabelProvider createLabelProvider(IQ7NamedElement element,
			IProject project) {
		return new Q7ElementLabelProvider(false);
	}

	private boolean hasProject() {
		return (element != null && element.getQ7Project() != null && element
				.getQ7Project().exists()) || project != null;
	}

	private IContext[] getAllContexts() {
		if (allContexts == null) {
			IQ7NamedElement[] elements = Q7SearchCore
					.findAllElements(new AllProjectScope());

			Set<IContext> result = new HashSet<IContext>();
			for (IQ7NamedElement context : elements) {
				if (context instanceof IContext) {
					if (!isValidToAdd((IContext) context)) {
						continue;
					}
					result.add((IContext) context);
				}
			}
			allContexts = result.toArray(new IContext[result.size()]);
		}
		return allContexts;
	}

	private IContext[] getFromRequiredProjects() {
		if (fromRequiredProjects == null) {

			IQ7NamedElement[] elements = null;
			if (project == null) {
				elements = Q7SearchCore
						.findAllElements(new ReferencedProjectScope(element
								.getQ7Project()));
			} else {
				elements = Q7SearchCore
						.findAllElements(new ReferencedProjectScope(RcpttCore
								.create(project)));
			}

			Set<IContext> result = new HashSet<IContext>();
			for (IQ7NamedElement context : elements) {
				if (context instanceof IContext) {
					if (!isValidToAdd((IContext) context)) {
						continue;
					}
					result.add((IContext) context);
				}
			}
			fromRequiredProjects = result.toArray(new IContext[result.size()]);
		}
		return fromRequiredProjects;
	}

	protected abstract boolean isValidToAdd(IContext context);

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	private void updateFilteredList(boolean checked) {
		IContext[] newElements = checked ? getFromRequiredProjects()
				: getAllContexts();
		setListElements(newElements);
	}

	@Override
	protected void handleEmptyList() {
		filterTextLabel.setEnabled(false);
		super.handleEmptyList();
	}

	@Override
	protected void handleNonemptyList() {
		filterTextLabel.setEnabled(true);
		super.handleNonemptyList();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, NEW_ID,
				Messages.ContextSelectionDialog_NewButtonText, false).setImage(
						Images.getImage(Images.CONTEXT));

		Button captureBtn = createButton(parent, CAPTURE_ID,
				Messages.ContextSelectionDialog_CaptureButtonText, false);
		captureBtn.setImage(
				Images.getImage(Images.SNAPSHOT));
		if (type != null) {
			captureBtn.setEnabled(type.canCreate());
		}
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (NEW_ID == buttonId || CAPTURE_ID == buttonId) {
			try {
				IContext context = DialogUtil.openNewContextDialog(getShell(),
						CAPTURE_ID == buttonId, type);
				if (context == null) {
					return;
				}
				setResult(Arrays.asList(context));
				setReturnCode(OK);
				close();
			} catch (Throwable e) {
				Q7UIPlugin.log(e);
			}
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected Text createFilterText(Composite parent) {
		filterTextLabel = new Label(parent, SWT.NONE);
		filterTextLabel.setText(Messages.ContextSelectionDialog_FilterText);
		GridDataFactory.fillDefaults().applyTo(filterTextLabel);
		return super.createFilterText(parent);
	}

	@Override
	protected FilteredList createFilteredList(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.ContextSelectionDialog_TopLabelText);
		GridDataFactory.fillDefaults().applyTo(label);

		FilteredList list = super.createFilteredList(parent);
		final Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText(Messages.ContextSelectionDialog_ShowCheckboxText);
		boolean hasProject = hasProject();
		checkbox.setSelection(hasProject);
		checkbox.setEnabled(hasProject);
		updateFilteredList(hasProject);
		checkbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFilteredList(checkbox.getSelection());
			}
		});
		return list;
	}
}
