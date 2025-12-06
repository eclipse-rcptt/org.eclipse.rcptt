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

import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.log;
import static org.eclipse.rcptt.launching.Q7LaunchUtils.format;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.pde.internal.core.util.VMUtil;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.launcher.JREBlock;
import org.eclipse.pde.internal.ui.launcher.ProgramBlock;
import org.eclipse.pde.ui.launcher.MainTab;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.ext.JvmTargetCompatibility;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class ExternalAUTMainTab extends MainTab {

	private AUTLocationBlock fLocationBlock;
	private AUTProgramBlock fAUTProgramBlock;
	private ITargetPlatformHelper currentTargetPlatform;
	// private ILaunchConfigurationTab[] tabs;
	private ExternalTabGroup group;

	public ExternalAUTMainTab(ExternalTabGroup group) {
		super();
		this.group = group;
		createLocationBlock();
	}

	private void createLocationBlock() {
		fLocationBlock = new AUTLocationBlock(this);
	}

	@Override
	protected void createJREBlock() {
		fJreBlock = new JREBlock(this) {

			IVMInstall getSelectedJVM() {
				try {
					Button fJreButton = (Button) getSuperfield("fJreButton");
					Combo fJreCombo = (Combo) getSuperfield("fJreCombo");
					Combo fEeCombo = (Combo) getSuperfield("fEeCombo");
					if (fJreButton.getSelection()) {
						if (fJreCombo.getSelectionIndex() != -1) {
							String jreName = fJreCombo.getText();

							IVMInstall install = VMUtil.getVMInstall(jreName);
							return install;
						}
					} else {
						if (fEeCombo.getSelectionIndex() != -1) {
							IExecutionEnvironment environment = VMUtil
									.getExecutionEnvironment(parseEESelection(fEeCombo
											.getText()));
							if (environment != null) {
								IVMInstall result = environment.getDefaultVM();
								if (result != null) {
									return result;
								}
								return Arrays.stream(environment.getCompatibleVMs())
										.filter(i -> environment.isStrictlyCompatible(i)).findFirst().orElse(null);
							}
						}
					}
				} catch (Throwable e) {
					Q7UIPlugin.log(e);
				}
				return null;
			}

			private String parseEESelection(String selection) {
				int index = selection.indexOf(" ("); //$NON-NLS-1$
				if (index == -1)
					return selection;
				return selection.substring(0, index);
			}

			private Object getSuperfield(String name)
					throws NoSuchFieldException, IllegalAccessException {
				Field field = JREBlock.class.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(this);
			}

			@Override
			public String validate() {
				String value = super.validate();
				if (value != null) {
					return value;
				}
				if (currentTargetPlatform == null) {
					return null;
				}
				
				IStatus result;
				JvmTargetCompatibility compatibility;
				try {
					compatibility = new JvmTargetCompatibility(currentTargetPlatform);
					result =  compatibility.checkCompatibilty(getSelectedJVM());
				} catch (CoreException e) {
					result = e.getStatus();
				}
				
				if (result.matches(IStatus.ERROR | IStatus.CANCEL)) {
					return format(result);
				}
				return null;
			}
		};
	}

	@Override
	protected void createProgramBlock() {
		fProgramBlock = new ProgramBlock(this) {
			@Override
			public void initializeFrom(ILaunchConfiguration config)
					throws CoreException {
				// Do nothing
			}

			@Override
			public void performApply(ILaunchConfigurationWorkingCopy config) {
				// Do nothing
			};
		};
		fAUTProgramBlock = new AUTProgramBlock(this);
	}

	@Override
	public void createControl(Composite parent) {
		final ScrolledComposite scrollContainer = new ScrolledComposite(parent,
				SWT.V_SCROLL);
		scrollContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite composite = new Composite(scrollContainer, SWT.NONE);
		scrollContainer.setContent(composite);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fLocationBlock.createControl(composite);
		fDataBlock.createControl(composite);
		// fProgramBlock.createControl(composite);
		fAUTProgramBlock.createControl(composite);
		fJreBlock.createControl(composite);

		// Add listener for each control to recalculate scroll bar when it is
		// entered.
		// This results in scrollbar scrolling when user tabs to a control that
		// is not in the field of view.
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				Control child = (Control) e.widget;
				Rectangle bounds = child.getBounds();
				Rectangle area = scrollContainer.getClientArea();
				Point origin = scrollContainer.getOrigin();
				if (origin.x > bounds.x)
					origin.x = Math.max(0, bounds.x);
				if (origin.y > bounds.y)
					origin.y = Math.max(0, bounds.y);
				if (origin.x + area.width < bounds.x + bounds.width)
					origin.x = Math
							.max(0, bounds.x + bounds.width - area.width);
				if (origin.y + area.height < bounds.y + bounds.height)
					origin.y = Math.max(0, bounds.y + bounds.height
							- area.height);
				scrollContainer.setOrigin(origin);
			}
		};
		Control[] controls = composite.getChildren();
		for (int i = 0; i < controls.length; i++)
			controls[i].addListener(SWT.Activate, listener);

		Dialog.applyDialogFont(composite);
		composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrollContainer.setExpandHorizontal(true);
		setControl(scrollContainer);
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(composite, IHelpContextIds.LAUNCHER_BASIC);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		super.initializeFrom(config);
		try {
			fLocationBlock.initializeFrom(config);
			fAUTProgramBlock.initializeFrom(config);
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	void setStatus(final IStatus status) {
		if (!status.isOK() && !status.matches(IStatus.CANCEL)) {
			log(status);
		}
		getShell().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				if (status.isOK()) {
					setErrorMessage(null);
					setMessage(null);
				} else if (status.matches(IStatus.ERROR)) {
					setMessage(null);
					setErrorMessage(status.getMessage());
				} else {
					setMessage(status.getMessage());
					setErrorMessage(null);
				}
			}
		});

	}

	@Override
	public void validateTab() {
		super.validateTab();
		if (getErrorMessage() == null) {
			setStatus(fLocationBlock.getStatus());
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		super.performApply(config);
		try {
			fLocationBlock.performApply(config);
			fAUTProgramBlock.performApply(config);
		} catch (CoreException e) {
			setStatus(e.getStatus());
		}
	}

	public void setCurrentTargetPlatform(ITargetPlatformHelper info) {
		this.currentTargetPlatform = info;
		fAUTProgramBlock.updateInfo(currentTargetPlatform);
	}

	public ITargetPlatformHelper getTarget() {
		return currentTargetPlatform;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		fAUTProgramBlock.setDefaults(config);
	}

	public void doUpdate(ITargetPlatformHelper info) {
		group.doUpdate(info);
	}
}
