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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rcptt.core.launching.events.AutBundleState;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public abstract class BasicAUTComposite {

	public static interface ILaunchListener {

		public void launched(AutLaunch launch);

	}

	private ILaunchListener launchListener = null;

	public void setLaunchListener(ILaunchListener listener) {
		this.launchListener = listener;
	}

	protected class StyledAUTLabelProvider extends StyledCellLabelProvider {
		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			if (element instanceof AutElement) {
				AutElement e = (AutElement) element;
				cell.setImage(AUTProvider.getImage(e));
				StyledString styledString = new StyledString(
						AUTProvider.getText(e));
				cell.setText(styledString.toString());
				cell.setStyleRanges(styledString.getStyleRanges());
			}
			else if (element instanceof AutContentProvider.Bundles) {
				cell.setText("Bundles");
				cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
			}
			else if (element instanceof AutBundleState) {
				AutBundleState state = (AutBundleState) element;
				StyledString styledString = new StyledString(state.getId());
				styledString.append(" at " + state.getLocation(), StyledString.QUALIFIER_STYLER);
				cell.setText(styledString.toString());
				cell.setStyleRanges(styledString.getStyleRanges());
				cell.setImage(PlatformUI.getWorkbench().getSharedImages()
						.getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT));
			}
			else {
				cell.setText("Unknown element");
			}
			super.update(cell);
		}
	}

	protected IWizard getInitWizard(String kind) {
		IExtensionPoint wizard = Platform.getExtensionRegistry()
				.getExtensionPoint(Q7UIPlugin.PLUGIN_ID + ".autWizard"); //$NON-NLS-1$
		if (wizard != null) {
			IConfigurationElement[] elements = wizard
					.getConfigurationElements();
			for (IConfigurationElement iConfigurationElement : elements) {
				try {
					if (kind.equals(iConfigurationElement.getAttribute("kind"))) {
						return (IWizard) iConfigurationElement
								.createExecutableExtension("class"); //$NON-NLS-1$
					}
				} catch (Throwable e) {
					RcpttPlugin.log(e);
				}
			}
		}
		return null;
	}

	protected TreeViewer viewer;

	protected abstract void updateButtonsEnablement();

	public BasicAUTComposite() {
		super();
	}

	public void create(Composite parent, boolean border) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

		Composite viewerComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(viewerComposite);
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(viewerComposite);

		viewer = new TreeViewer(viewerComposite, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | (border ? SWT.BORDER : 0));
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(viewer.getControl());

		viewer.setLabelProvider(new StyledAUTLabelProvider());
		contentProvider = new AutContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setInput(new Object());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablement();
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				launch();
			}
		});

		createButtonsBar(composite);
		updateEnablement();
	}

	protected boolean launch() {
		launch = null;
		final AutElement element = getElement();
		if (element != null) {
			if (element.isLaunched() && element instanceof AutLaunchElement) {
				launch = ((AutLaunchElement) element).getLaunch();
			} else {
				Shell shell = viewer.getControl().getShell();
				launch = LaunchUtils.launch(element.getAut(), shell);
			}
		}

		if (launch != null && launchListener != null)
			launchListener.launched(launch);

		return launch != null;
	}

	public AutLaunch getLaunch() {
		return launch;
	}

	private AutLaunch launch;
	private AutContentProvider contentProvider;

	protected void configure() {
		final AutElement element = getElement();
		if (element != null) {
			// try to use configure wizard if available
			Shell shell = viewer.getControl().getShell();
			ILaunchConfiguration config = element.getAut().getConfig();
			if (config != null) {
				IWizard configWizard = getInitWizard("edit");
				if (configWizard != null
						&& configWizard instanceof IAUTConfigWizard) {
					((IAUTConfigWizard) configWizard)
							.setLaunchConfiguration(config);
					WizardDialog dialog = new WizardDialog(shell, configWizard) {
						@Override
						protected void configureShell(Shell newShell) {
							super.configureShell(newShell);
							newShell.setImage(Images.getImage(
									Images.AUT));
						}
					};
					((IAUTConfigWizard) configWizard).setWizardDialog(dialog);
					dialog.setHelpAvailable(false);
					dialog.open();
				} else {
					// use default launch configuration configure wizard
					DebugUITools.openLaunchConfigurationPropertiesDialog(shell,
							config, IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
				}
			}
			try {
				ShellUtilsProvider.getShellUtils().forceActive(shell);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void updateEnablement() {
		updateButtonsEnablement();
	}

	public void addListener(ISelectionChangedListener iSelectionChangedListener) {
		this.viewer.addSelectionChangedListener(iSelectionChangedListener);
	}

	protected void stop() {
		AutElement element = getElement();
		if (element != null) {
			element.stop();
		}
	}

	public void removeAUT() {
		List<AutElement> elements = new ArrayList<AutElement>();
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator<?> i = ((IStructuredSelection) selection).iterator();
			while (i.hasNext()) {
				Object next = i.next();
				elements.add((AutElement) next);
			}
		}
		if (elements.size() != 0) {
			StringBuilder names = new StringBuilder();
			for (AutElement iautElement : elements) {
				if (names.length() > 0) {
					names.append(", ");
				}
				names.append(iautElement.getName());
			}
			MessageDialog dialog = new MessageDialog(viewer.getControl()
					.getShell(), getMessageTitle(), null, MessageFormat.format(
					Messages.BasicAUTComposite_RemoveApprovalMsg,
					names.toString()), MessageDialog.QUESTION, new String[] {
					IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
					0) {
			};
			int value = dialog.open();
			if (value == MessageDialog.OK) {
				for (AutElement element : elements) {
					try {
						element.remove();
					} catch (CoreException e) {
						Q7UIPlugin.log(e);
					}
				}
			}
		}
	}

	protected String getMessageTitle() {
		return Messages.BasicAUTComposite_MessageTitle;
	}

	public void addAut() {
		Shell shell = viewer.getControl().getShell();
		WizardDialog dialog = new WizardDialog(shell, getInitWizard("new")) {
			@Override
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				newShell.setImage(Images.getImage(Images.AUT));
			}
		};
		dialog.setHelpAvailable(false);
		dialog.open();
		try {
			ShellUtilsProvider.getShellUtils().forceActive(shell);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	protected ImageDescriptor getDeleteImage() {
		return PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE);
	}

	protected ImageDescriptor getNewImage() {
		return PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD);
	}

	public boolean isSelectionOK() {
		return getElement() != null;
	}

	public boolean doOk() {
		return launch();
	}

	protected void createButtonsBar(Composite composite) {
	}

	protected AutElement getElement() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			if (element instanceof AutElement) {
				return (AutElement) element;
			}
		}
		return null;
	}

	public void setInitialSelection() {
		Object[] elements = contentProvider.getElements(null);
		if (elements.length > 0) {
			viewer.setSelection(new StructuredSelection(elements[0]));
		}
		updateEnablement();
	}
}
