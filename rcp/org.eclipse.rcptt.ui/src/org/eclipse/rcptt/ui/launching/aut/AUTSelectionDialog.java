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
package org.eclipse.rcptt.ui.launching.aut;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.ui.launching.aut.BasicAUTComposite.ILaunchListener;

public class AUTSelectionDialog extends Dialog implements ILaunchListener {

	private static final String SETTINGS_KEY = "AUTSelectionDialog"; //$NON-NLS-1$

	private BasicAUTComposite autComposite;
	private AutLaunch result;

	public AUTSelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.AUTSelectionDialog_Title);
		newShell.setImage(Images.getImage(Images.AUT));
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		final IDialogSettings root = Q7UIPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings section = root.getSection(SETTINGS_KEY);
		if (section == null) {
			section = root.addNewSection(SETTINGS_KEY);
		}
		return section;
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTSIZE;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		autComposite = createAUTComposite();
		autComposite.create(composite, true);
		autComposite.addListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablement(autComposite.isSelectionOK());
			}
		});
		autComposite.setInitialSelection();
		autComposite.setLaunchListener(this);
		updateEnablement(autComposite.isSelectionOK());
		return composite;
	}

	protected BasicAUTComposite createAUTComposite() {
		return new AUTComposite();
	}

	protected void configure() {
		if (Window.OK == DebugUITools.openLaunchConfigurationDialogOnGroup(
				getShell(), new StructuredSelection(),
				"org.eclipse.debug.ui.launchGroup.run", null)) { //$NON-NLS-1$
		}
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected void okPressed() {
		if (autComposite.doOk()) {
			result = autComposite.getLaunch();
			super.okPressed();
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		if (autComposite == null) {
			updateEnablement(false);
		} else {
			updateEnablement(autComposite.isSelectionOK());
		}
		return result;
	}

	private void updateEnablement(boolean state) {
		if (buttonBar != null) {
			Button button = getButton(OK);
			button.setEnabled(state);
		}
	}

	public AutLaunch getResult() {
		return result;
	}

	protected Point getInitialSize() {
		Point calculatedSize = super.getInitialSize();
		Rectangle bounds = getShell().getDisplay().getBounds();
		int minHeight = bounds.height / 2 - bounds.height / 10 - 50;
		if (calculatedSize.x < 450) {
			calculatedSize.x = 450;
		}
		if (calculatedSize.y < minHeight) {
			calculatedSize.y = minHeight;
		}
		return calculatedSize;
	}

	public void launched(AutLaunch launch) {
		result = launch;
		super.okPressed();
	}
}
