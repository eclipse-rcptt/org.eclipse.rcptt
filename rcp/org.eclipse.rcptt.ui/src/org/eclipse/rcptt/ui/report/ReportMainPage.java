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
package org.eclipse.rcptt.ui.report;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.reporting.core.ReportRendererManager;
import org.eclipse.rcptt.reporting.core.ReportRendererManager.ReportRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ReportMainPage extends WizardPage {
	private final class InverseBooleanConverter extends UpdateValueStrategy<Boolean, Boolean> {
		@Override
		public Boolean convert(Boolean value) {
			return Boolean.valueOf(!value.booleanValue());
		}
	}

	DataBindingContext dbc = new DataBindingContext();
	WritableValue<String> reportName = new WritableValue<>("", String.class); //$NON-NLS-1$
	WritableValue<ReportRenderer> reportFormat = new WritableValue<>(null, ReportRenderer.class);
	WritableList<ReportRenderer> reports = new WritableList<>(new ArrayList<ReportRenderer>(),
			ReportRenderer.class);
	WritableValue<Boolean> generateToWorkspace = new WritableValue<>(Boolean.TRUE,
			Boolean.class);

	WritableValue<String> workspaceLocation = new WritableValue<>("", String.class); //$NON-NLS-1$
	WritableValue<String> filesystemLocation = new WritableValue<>("", String.class); //$NON-NLS-1$

	private ReportWizard wizard;
	private String initialWorkspaceLocation;

	protected ReportMainPage(ReportWizard reportWizard, String[] disabled,
			String initialWorkspaceLocation) {
		super("ReportMainPage", Messages.ReportMainPage_Name, null); //$NON-NLS-1$
		this.wizard = reportWizard;
		setDescription(Messages.ReportMainPage_Description);
		ReportRenderer[] extensions = ReportRendererManager.getInstance()
				.getExtensions();
		for (ReportRenderer reportRenderer : extensions) {
//			if (!Q7UIPlugin.isInternalEdition()
//					&& reportRenderer.getName().equals("Q7 Report")) { //$NON-NLS-1$
//				continue;
//			}
			String extension = reportRenderer.getExtension();
			boolean found = false;
			if (disabled != null) {
				for (String d : disabled) {
					if (d.equals(extension)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				reports.add(reportRenderer);
				if (reportFormat.getValue() == null) {
					reportFormat.setValue(reportRenderer);
				}
			}
		}
		reportName.setValue(wizard.getName());
		this.initialWorkspaceLocation = initialWorkspaceLocation;
	}

	public String getReportName() {
		return (String) reportName.getValue();
	}

	public ReportRenderer getRenderer() {
		return (ReportRenderer) reportFormat.getValue();
	}

	public boolean isToWorkspace() {
		return ((Boolean) generateToWorkspace.getValue()).booleanValue();
	}

	public String getLocation() {
		if (isToWorkspace()) {
			String value = (String) workspaceLocation.getValue();
			IDialogSettings ds = getDialogSettings();
			if (ds != null) {
				ds.put("report.dialog.workspace.location", value); //$NON-NLS-1$
			}
			return value;
		} else {
			return (String) filesystemLocation.getValue();
		}
	}

	public void createControl(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(panel);

		// Report name
		Label l = new Label(panel, SWT.NONE);
		l.setText(Messages.ReportMainPage_NameLabel);
		final Text reportName = new Text(panel, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(reportName);

		final ControlDecoration controlDecoration = new ControlDecoration(
				reportName, SWT.LEFT | SWT.TOP);
		controlDecoration.setDescriptionText(""); //$NON-NLS-1$
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecoration.setImage(fieldDecoration.getImage());
		controlDecoration.hide();

		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(reportName), this.reportName);

		this.reportName.addValueChangeListener(new IValueChangeListener<String>() {
			@Override
			public void handleValueChange(ValueChangeEvent<? extends String> event) {
				String name = (String) event.getObservableValue().getValue();
				if (name.trim().length() == 0) {
					controlDecoration.show();
					controlDecoration
							.showHoverText(Messages.ReportMainPage_EmptyReportNameMsg);
					validate();
					return;
				}
				controlDecoration.hide();
				validate();
			}
		});
		// Output format:
		l = new Label(panel, SWT.NONE);
		l.setText(Messages.ReportMainPage_FormatLabel);
		Combo reportFormat = new Combo(panel, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().grab(false, false).applyTo(reportFormat);
		IConverter<String, ReportRenderer> nameToRenderer = IConverter.create(String.class,
				ReportRenderer.class,
				this::convertReport);
		IConverter<ReportRenderer, String> rendererToName = IConverter.create(ReportRenderer.class,
				String.class,
				ReportRenderer::getName);
		dbc.bindList(WidgetProperties.items().observe(reportFormat),
				this.reports,
				UpdateListStrategy.create(nameToRenderer),
				UpdateListStrategy.create(rendererToName));
		dbc.bindValue(WidgetProperties.comboSelection().observe(reportFormat),
				this.reportFormat,
				UpdateValueStrategy.create(nameToRenderer),
				UpdateValueStrategy.create(rendererToName));
		ReportRenderer defaultRenderer = null;
		for (int i = 0; i < reports.size(); i++) {
			ReportRenderer rr = (ReportRenderer) reports.get(i);
			if (rr.getExtension().equals("html")) { //$NON-NLS-1$
				defaultRenderer = rr;
				break;
			}
		}
		if (defaultRenderer != null) {
			this.reportFormat.setValue(defaultRenderer);
		} else {
			this.reportFormat.setValue(reports.get(0));
		}

		// Output location

		l = new Label(panel, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(l);
		Composite sPanel = new Composite(panel, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(sPanel);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sPanel);
		l = new Label(sPanel, SWT.NONE);
		l.setText(Messages.ReportMainPage_OutputLabel);
		l = new Label(sPanel, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(l);

		// Generate to workspace
		createGenerateWorkspace(panel);

		// Generate to file system
		createGenerateToFilesystem(panel);

		setControl(panel);
		IDialogSettings ds = getDialogSettings();
		if (ds != null) {
			String loc = ds.get("report.dialog.workspace.location"); //$NON-NLS-1$
			if (loc != null) {
				workspaceLocation.setValue(loc);
			}
		}
		if (initialWorkspaceLocation != null) {
			workspaceLocation.setValue(initialWorkspaceLocation);
		}
		validate();
	}

	protected boolean validate() {
		String repName = (String) reportName.getValue();
		if (repName.trim().length() == 0) {
			setPageComplete(false);
			return false;
		}

		boolean generateToWorkspace = ((Boolean) this.generateToWorkspace
				.getValue()).booleanValue();
		if (generateToWorkspace) {
			if (!isValidWorkspaceLocation()) {
				setPageComplete(false);
				return false;
			}
		} else {
			if (!isValidFileLocation()) {
				setPageComplete(false);
				return false;
			}
		}

		setPageComplete(true);
		return true;
	}

	private boolean isValidWorkspaceLocation() {
		String wLoc = (String) workspaceLocation.getValue();
		if (wLoc.trim().length() == 0) {
			return false;
		}
		boolean exist = (ResourcesPlugin.getWorkspace().getRoot()
				.exists(new Path(wLoc)));
		if (!exist) {
			return false;
		}
		return true;
	}

	private boolean isValidFileLocation() {
		String wLoc = (String) filesystemLocation.getValue();
		if (wLoc.trim().length() == 0) {
			return false;
		}
		if (!(new File(wLoc).exists())) {
			return false;
		}
		return true;
	}

	private void createGenerateToFilesystem(Composite panel) {
		Button generateToFilesystem = new Button(panel, SWT.RADIO);
		generateToFilesystem
				.setText(Messages.ReportMainPage_GenerateInFileSystemButton);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(generateToFilesystem);

		Composite sPanelFileSystem = new Composite(panel, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3)
				.extendedMargins(20, 0, 0, 0).applyTo(sPanelFileSystem);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sPanelFileSystem);

		Label sFileSystemLabel = new Label(sPanelFileSystem, SWT.NONE);
		sFileSystemLabel.setText(Messages.ReportMainPage_LocationLabel);
		Text filesystemLocation = new Text(sPanelFileSystem, SWT.BORDER);

		final ControlDecoration controlDecoration = new ControlDecoration(
				filesystemLocation, SWT.LEFT | SWT.TOP);
		controlDecoration.setDescriptionText(""); //$NON-NLS-1$
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecoration.setImage(fieldDecoration.getImage());
		controlDecoration.hide();

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(filesystemLocation);
		Button browseFilesystem = new Button(sPanelFileSystem, SWT.PUSH);
		browseFilesystem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage(Messages.ReportMainPage_SelectLocationDialogText);
				dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot()
						.getLocation().removeFirstSegments(1).toString());
				dialog.setText(Messages.ReportMainPage_SelectLocationDialogText);
				String location = dialog.open();
				if (location != null) {
					ReportMainPage.this.filesystemLocation.setValue(location);
				}
			}
		});
		browseFilesystem.setText(Messages.ReportMainPage_BrowseButton);
		setButtonLayoutData(browseFilesystem);

		dbc.bindValue(WidgetProperties.enabled().observe(browseFilesystem),
				this.generateToWorkspace, UpdateValueStrategy.never(),
				new InverseBooleanConverter());
		dbc.bindValue(WidgetProperties.enabled().observe(filesystemLocation),
				this.generateToWorkspace, UpdateValueStrategy.never(),
				new InverseBooleanConverter());

		dbc.bindValue(WidgetProperties.enabled().observe(sFileSystemLabel),
				this.generateToWorkspace, UpdateValueStrategy.never(),
				new InverseBooleanConverter());

		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(filesystemLocation),
				this.filesystemLocation);

		this.filesystemLocation
				.addValueChangeListener(new IValueChangeListener<String>() {
					public void handleValueChange(ValueChangeEvent<? extends String> event) {
						if (!isValidFileLocation()) {
							controlDecoration.show();
							controlDecoration
									.showHoverText(Messages.ReportMainPage_NoOutputLocationMsg);
							validate();
							return;
						}
						controlDecoration.hide();
						validate();
					}
				});
	}

	private void createGenerateWorkspace(Composite panel) {
		Button generateToWorkspace = new Button(panel, SWT.RADIO);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(generateToWorkspace);
		generateToWorkspace
				.setText(Messages.ReportMainPage_GenerateInWorkspaceButton);

		Composite sPanelWorkspace = new Composite(panel, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3)
				.extendedMargins(20, 0, 0, 0).applyTo(sPanelWorkspace);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(sPanelWorkspace);

		Label sWorkspaceLanel = new Label(sPanelWorkspace, SWT.NONE);
		sWorkspaceLanel.setText(Messages.ReportMainPage_LocationLabel);
		Text workspaceLocation = new Text(sPanelWorkspace, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false)
				.align(SWT.FILL, SWT.CENTER).applyTo(workspaceLocation);

		final ControlDecoration controlDecoration = new ControlDecoration(
				workspaceLocation, SWT.LEFT | SWT.TOP);
		controlDecoration.setDescriptionText(""); //$NON-NLS-1$
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecoration.setImage(fieldDecoration.getImage());
		controlDecoration.hide();

		Button browseWorkspace = new Button(sPanelWorkspace, SWT.PUSH);
		browseWorkspace.setText(Messages.ReportMainPage_BrowseButton);

		browseWorkspace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FolderSelectionDialog dialog = new FolderSelectionDialog(
						getShell(), new WorkbenchLabelProvider(),
						new WorkbenchContentProvider()) {
					@Override
					protected TreeViewer createTreeViewer(Composite parent) {
						TreeViewer viewer = super.createTreeViewer(parent);
						viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
							@Override
							public boolean select(Viewer viewer,
									Object parentElement, Object element) {
								if (element instanceof IResource) {
									if (((IResource) element).getType() == IResource.FILE) {
										return false;
									}
								}
								return true;
							}

						} });
						return viewer;
					}
				};
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				dialog.setAllowMultiple(false);
				dialog.setMessage(Messages.ReportMainPage_SelectLocationDialogText);
				dialog.setBlockOnOpen(true);
				if (dialog.open() == ResourceSelectionDialog.OK) {
					Object[] result = dialog.getResult();
					if (result.length == 1) {
						ReportMainPage.this.workspaceLocation
								.setValue(((IResource) result[0]).getFullPath()
										.toString());
					}
				}
			}
		});
		setButtonLayoutData(browseWorkspace);

		dbc.bindValue(WidgetProperties.enabled().observe(browseWorkspace),
				this.generateToWorkspace);
		dbc.bindValue(WidgetProperties.enabled().observe(workspaceLocation),
				this.generateToWorkspace);

		dbc.bindValue(WidgetProperties.enabled().observe(sWorkspaceLanel),
				this.generateToWorkspace);

		dbc.bindValue(WidgetProperties.enabled().observe(generateToWorkspace),
				this.generateToWorkspace);

		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(workspaceLocation),
				this.workspaceLocation);
		this.workspaceLocation
				.addValueChangeListener(new IValueChangeListener<String>() {
					public void handleValueChange(ValueChangeEvent<? extends String> event) {
						if (!isValidWorkspaceLocation()) {
							controlDecoration.show();
							controlDecoration
									.showHoverText(Messages.ReportMainPage_IncorrectLocationMsg);
							validate();
							return;
						}
						controlDecoration.hide();
						validate();
					}
				});
		this.generateToWorkspace
				.addValueChangeListener(new IValueChangeListener<Boolean>() {
					public void handleValueChange(ValueChangeEvent<? extends Boolean> event) {
						validate();
					}
				});
	}

	private ReportRenderer convertReport(String value) {
		for (int i = 0; i < reports.size(); i++) {
			ReportRenderer ren = (ReportRenderer) reports.get(i);
			if (ren.getName().equals(value)) {
				return ren;
			}
		}
		return null;
	}

	@Override
	public boolean isPageComplete() {
		return super.isPageComplete();
	}
}
