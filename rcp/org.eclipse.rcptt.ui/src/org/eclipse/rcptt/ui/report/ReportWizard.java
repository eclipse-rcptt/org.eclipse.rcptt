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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.reporting.core.IReportRenderer;
import org.eclipse.rcptt.reporting.core.IReportRenderer.IContentFactory;
import org.eclipse.rcptt.reporting.core.ReportRendererManager.ReportRenderer;
import org.eclipse.rcptt.reporting.util.FileContentFactory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.swt.widgets.MessageBox;

public class ReportWizard extends Wizard {
	private ReportMainPage mainPage;
	private Iterable<Report> report;
	private String name;
	private String[] disabled;
	private String initialWorkspaceLocatio;

	public ReportWizard(Iterable<Report> iterable, String name) {
		this.report = iterable;
		this.name = name;
	}

	public Iterable<Report> getReport() {
		return report;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean performFinish() {
		final ReportRenderer renderer = mainPage.getRenderer();
		final String reportName = mainPage.getReportName();
		final boolean toWorkspace = mainPage.isToWorkspace();
		final String location = mainPage.getLocation();

		final IReportRenderer reportRenderer = renderer.getRenderer();
		final IContentFactory factory;
		if (toWorkspace) {
			factory = new WorkspaceContentFactory(new Path(location));
		} else {
			factory = new FileContentFactory(new Path(location));
		}
		if (!checkForFileOverwrite(factory, reportName, reportRenderer)) {
			return false;
		}
		try {
			getContainer().run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					if (reportRenderer instanceof IReportRenderer.ProgressMonitorSupport) {
						((IReportRenderer.ProgressMonitorSupport) reportRenderer)
								.setProgressMonitor(monitor);
					}
					final IStatus status = reportRenderer.generateReport(
							factory, reportName, getReport());
					if (!status.isOK()) {
						getShell().getDisplay().asyncExec(new Runnable() {

							public void run() {
								MessageBox box = new MessageBox(getShell());
								box.setMessage(status.getMessage());
								box.setText(getShell().getText());
								box.open();
							}
						});
					}
				}
			});
		} catch (Throwable e) {
			Q7UIPlugin.log(e);
			return false;
		}
		return true;
	}

	private boolean checkForFileOverwrite(IContentFactory factory,
			String reportName, IReportRenderer reportRenderer) {
		String[] fileNames = reportRenderer.getGeneratedFileNames(reportName);
		StringBuilder existingFiles = new StringBuilder();
		int existingFilesCount = 0;
		for (String fname : fileNames) {
			List<String> existFiles = factory.matchFiles(fname);

			if (!existFiles.isEmpty()) {
				if (existingFilesCount > 0) {
					existingFiles.append(",\n"); //$NON-NLS-1$
				}
				existingFilesCount++;
				existingFiles.append('\'').append(fname).append("(")
						.append(existFiles.size()).append(")").append("\'");
			}
		}
		if (existingFilesCount > 0) {
			String message;
			if (existingFilesCount == 1) {
				message = Messages.bind(
						Messages.ReportWizard_FileOverwriteDialogMsg_OneFile,
						existingFiles);
			} else {
				message = Messages.bind(
						Messages.ReportWizard_FileOverwriteDialogMsg_ManyFiles,
						existingFiles);
			}
			return MessageDialog.openQuestion(getShell(),
					Messages.ReportWizard_FileOverwriteDialogTitle, message);
		}
		return true;
	}

	@Override
	public void addPages() {
		addPage(mainPage = new ReportMainPage(this, disabled,
				initialWorkspaceLocatio));
	}

	public void setDisabledReports(String... disabled) {
		this.disabled = disabled;
	}

	public void setInitialWorkspaceLocation(String initialWorkspaceLocation) {
		this.initialWorkspaceLocatio = initialWorkspaceLocation;
	}
}
