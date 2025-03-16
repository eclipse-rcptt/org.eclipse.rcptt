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

import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.PLUGIN_ID;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.internal.ui.SWTFactory;
import org.eclipse.rcptt.internal.launching.ext.PDELocationUtils;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.launching.IQ7Launch;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("restriction")
public class AUTLocationBlock {

	private ExternalAUTMainTab fTab;
	private Listener fListener = new Listener();
	private final WritableValue<IStatus> status = new WritableValue<>(Status.OK_STATUS, IStatus.class);

	private void setStatus(final IStatus newStatus) {
		status.getRealm().asyncExec(new Runnable() {
			@Override
			public void run() {
				status.setValue(newStatus);
			}
		});
	}

	private Text locationField;
	private Button fileLocationButton;
	// private TableViewer informationViewer;
	private ITargetPlatformHelper info;
	// private String errorInfo;
	private boolean needUpdate = true;

	private class Listener extends SelectionAdapter implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			if (needUpdate) {
				updateInfo();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fileLocationButton) {
				handleFileLocationButtonSelected();
			}
		}
	}

	public AUTLocationBlock(ExternalAUTMainTab tab) {
		fTab = tab;
	}

	public void updateInfo() {
		// errorInfo = null;
		final String location = getLocation();
		boolean valid = PDELocationUtils.validateProductLocation(location).isOK();
		if (!valid) {
			// errorInfo =
			// "AUT location should point to existing directory";
		}

		if (needUpdate && valid) {
			info = null;
			runInDialog(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						info = Q7TargetPlatformManager.createTargetPlatform(location, monitor);
						assert info.getStatus().isOK();
					} catch (CoreException e) {
						setStatus(e.getStatus());
					}
				}
			});
		}
		if (info != null) {
			fTab.doUpdate(info);
		}
		fTab.setCurrentTargetPlatform(info);
		fTab.scheduleUpdateJob();
	}

	public void createControl(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		String locationLabel = "&Application Under Test (AUT)";
		group.setText(locationLabel);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayout(layout);
		group.setLayoutData(gridData);

		Label l = new Label(group, SWT.WRAP);
		l.setText("Location:");
		locationField = new Text(group, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		locationField.setLayoutData(gridData);
		locationField.addModifyListener(fListener);
		ControlAccessibleListener.addListener(locationField, group.getText());

		Composite buttonComposite = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(gridData);
		buttonComposite.setFont(parent.getFont());
		fileLocationButton = SWTFactory.createPushButton(buttonComposite,
				"File Syste&m...", null);
		fileLocationButton.addSelectionListener(fListener);
		ControlAccessibleListener.addListener(fileLocationButton,
				group.getText() + " " + fileLocationButton.getText()); //$NON-NLS-1$

	}

	/**
	 * Prompts the user to choose a location from the filesystem and sets the
	 * location as the full path of the selected file.
	 */
	private void handleFileLocationButtonSelected() {
		DirectoryDialog fileDialog = new DirectoryDialog(fTab.getControl()
				.getShell(), SWT.NONE);
		fileDialog.setFilterPath(locationField.getText());
		String text = fileDialog.open();
		if (text != null) {
			locationField.setText(text);
		}
	}

	IStatus createError(String message) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message);
	}

	public IStatus getStatus() {
		if (locationField.getText().trim().length() == 0) {
			return createError("Please specify Application installation directory...");
		}
		if (info == null) {
			return createError("Please specify correct Application installation directory...");
		} else {
			return info.getStatus();
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) throws CoreException {
		config.setAttribute(IQ7Launch.AUT_LOCATION, getLocation());
		if (info != null) {
			info.setTargetName(Q7TargetPlatformManager
					.getTargetPlatformName(config));
			info.save();
			config.setAttribute(IQ7Launch.TARGET_PLATFORM, info.getName());
			Q7TargetPlatformManager.setHelper(info.getName(), info);
		}
		config.setAttribute(IQ7Launch.UPDATE_TARGET_SUPPORTED, true);
	}

	private String getLocation() {
		return locationField.getText().trim();
	}

	public void initializeFrom(final ILaunchConfiguration config) {
		String location = null;
		try {
			location = config.getAttribute(IQ7Launch.AUT_LOCATION, "");
		} catch (CoreException e) {
			Activator.log(e);
		}
		needUpdate = false;
		runInDialog(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				try {
					info = Q7TargetPlatformManager.findTarget(config, monitor);
					if (info == null) {
						info = Q7TargetPlatformManager.getTarget(config,
								monitor);
					}
				} catch (CoreException e) {
					Activator.log(e);
				}

			}
		});
		locationField.setText(location);
		updateInfo();
		needUpdate = true;
	}
	
	private void runInDialog(final IRunnableWithProgress run) {
		final TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(
				fTab.getControl().getShell(), 500);
		try {
			dialog.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					run.run(new SyncProgressMonitor(monitor, dialog.getShell()
							.getDisplay()));
				}
			});
		} catch (InvocationTargetException e) {
			Activator.log(e);
		} catch (InterruptedException e) {
			Activator.log(e);
		}
	}
}
