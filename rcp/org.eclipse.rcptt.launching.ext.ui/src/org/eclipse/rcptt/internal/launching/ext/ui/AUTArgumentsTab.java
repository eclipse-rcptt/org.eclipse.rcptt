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
package org.eclipse.rcptt.internal.launching.ext.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.internal.debug.ui.launcher.VMArgumentsBlock;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.rcptt.internal.launching.ext.UpdateVMArgs;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("restriction")
public class AUTArgumentsTab extends JavaArgumentsTab implements IAUTListener {
	Text vmArguments;
	Button secureStorage;
	private static final ILog LOG = Platform.getLog(AUTArgumentsTab.class);

	@Override
	protected VMArgumentsBlock createVMArgsBlock() {
		return new VMArgumentsBlock() {
			@Override
			public void createControl(Composite parent) {
				createSecureStorageBlock(parent);

				super.createControl(parent);
				vmArguments = fVMArgumentsText;
			}

			@Override
			public void setDefaults(
					ILaunchConfigurationWorkingCopy configuration) {
				super.setDefaults(configuration);
				configuration
						.removeAttribute(IQ7Launch.OVERRIDE_SECURE_STORAGE);
			}

			@Override
			public void initializeFrom(ILaunchConfiguration configuration) {
				super.initializeFrom(configuration);
				try {
					boolean value = configuration.getAttribute(
							IQ7Launch.OVERRIDE_SECURE_STORAGE, true);
					secureStorage.setSelection(value);
				} catch (CoreException e) {
					LOG.error("FAiled to read " + IQ7Launch.OVERRIDE_SECURE_STORAGE + "attribute"  , e);
					throw new IllegalArgumentException(e);
				}
			}

			@Override
			public void performApply(
					ILaunchConfigurationWorkingCopy configuration) {
				if (secureStorage.getSelection()) {
					configuration
							.removeAttribute(IQ7Launch.OVERRIDE_SECURE_STORAGE);
				} else {
					configuration.setAttribute(
							IQ7Launch.OVERRIDE_SECURE_STORAGE, false);
				}
				super.performApply(configuration);

			}
		};
	}

	public void update(ITargetPlatformHelper info) {
		String vmArgs = info.getIniVMArgs();
		if (vmArgs != null && vmArgs.trim().length() > 0) {
			String attr = UpdateVMArgs.updateAttr(vmArgs);
			if (attr != null) {
				vmArguments.setText(attr);
			}
		}
	}

	private void createSecureStorageBlock(Composite parent) {
		Group securityBlock = new Group(parent, SWT.NONE);
		securityBlock.setText("Secure Storage:");
		GridLayoutFactory.swtDefaults().applyTo(securityBlock);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(securityBlock);
		secureStorage = new Button(securityBlock, SWT.CHECK);
		secureStorage
				.setText("Override '-eclipse.keyring' with 'launch shortcut folder/secure_storage'.");
		new Label(securityBlock, SWT.NONE)
				.setText("Please be aware. If unset, global secure storage could be cleared by Preferences context apply.");
		secureStorage
				.setToolTipText("Use launch shortcut folder for secure storage");
		secureStorage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
				scheduleUpdateJob();
			}
		});
	}
}
