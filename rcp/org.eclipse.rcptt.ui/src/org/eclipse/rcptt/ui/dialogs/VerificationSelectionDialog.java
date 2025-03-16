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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredList;

import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.search.AllProjectScope;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.ReferencedProjectScope;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.actions.Q7ElementLabelProvider;
import org.eclipse.rcptt.ui.commons.ModernElementListSelectionDialog;

public abstract class VerificationSelectionDialog extends
		ModernElementListSelectionDialog {

	private static final int NEW_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int CAPTURE_ID = IDialogConstants.CLIENT_ID + 2;
	private IVerification[] allVerifications;
	private IVerification[] fromRequiredProjects;

	private final IProject project;
	private final IQ7NamedElement element;
	private Label filterTextLabel;

	public VerificationSelectionDialog(Shell parent, IQ7NamedElement element,
			IProject project) {
		super(parent, createLabelProvider(element, project));
		this.project = project;
		this.element = element;
		setTitle(Messages.VerificationSelectionDialog_Title);
		setMessage(Messages.VerificationSelectionDialog_Msg);
		setMultipleSelection(true);
		setElements(hasProject() ? getFromRequiredProjects() : getAllVerifications());
	}

	private static ILabelProvider createLabelProvider(IQ7NamedElement element,
			IProject project) {
		return new Q7ElementLabelProvider(false);
	}

	private boolean hasProject() {
		return (element != null && element.getQ7Project() != null && element
				.getQ7Project().exists()) || project != null;
	}

	private IVerification[] getAllVerifications() {
		if (allVerifications == null) {
			IQ7NamedElement[] elements = Q7SearchCore
					.findAllElements(new AllProjectScope());

			Set<IVerification> result = new HashSet<IVerification>();
			for (IQ7NamedElement verification : elements) {
				if (verification instanceof IVerification) {
					if (!isValidToAdd((IVerification) verification)) {
						continue;
					}
					result.add((IVerification) verification);
				}
			}
			allVerifications = result.toArray(new IVerification[result.size()]);
		}
		return allVerifications;
	}

	private IVerification[] getFromRequiredProjects() {
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

			Set<IVerification> result = new HashSet<IVerification>();
			for (IQ7NamedElement verification : elements) {
				if (verification instanceof IVerification) {
					if (!isValidToAdd((IVerification) verification)) {
						continue;
					}
					result.add((IVerification) verification);
				}
			}
			fromRequiredProjects = result.toArray(new IVerification[result.size()]);
		}
		return fromRequiredProjects;
	}

	protected abstract boolean isValidToAdd(IVerification verification);

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

	private void updateFilteredList(boolean checked) {
		IVerification[] newElements = checked ? getFromRequiredProjects()
				: getAllVerifications();
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
				Messages.VerificationSelectionDialog_NewButtonText, false).setImage(
				Images.getImageDescriptor(Images.VERIFICATION).createImage());
		createButton(parent, CAPTURE_ID,
				Messages.VerificationSelectionDialog_CaptureButtonText, false)
				.setImage(
						Images.getImage(Images.SNAPSHOT));
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (NEW_ID == buttonId || CAPTURE_ID == buttonId) {
			try {
				IVerification verification =
						DialogUtil.openNewVerificationDialog(getShell(),
								CAPTURE_ID == buttonId);
				if (verification == null) {
					return;
				}
				setResult(Arrays.asList(verification));
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
		filterTextLabel.setText(Messages.VerificationSelectionDialog_FilterText);
		GridDataFactory.fillDefaults().applyTo(filterTextLabel);
		return super.createFilterText(parent);
	}

	@Override
	protected FilteredList createFilteredList(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.VerificationSelectionDialog_TopLabelText);
		GridDataFactory.fillDefaults().applyTo(label);

		FilteredList list = super.createFilteredList(parent);
		final Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText(Messages.VerificationSelectionDialog_ShowCheckboxText);
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
