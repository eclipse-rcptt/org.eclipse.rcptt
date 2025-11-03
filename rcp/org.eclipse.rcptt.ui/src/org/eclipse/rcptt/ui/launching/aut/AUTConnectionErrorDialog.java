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

import static org.eclipse.rcptt.launching.Q7LaunchUtils.format;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.Q7LaunchUtils;

public class AUTConnectionErrorDialog extends MessageDialog {

	private static String DEFAULT_ERROR_MESSAGE = "Oops! Couldn't connect to the application."; //$NON-NLS-1$
	private static String DIALOG_TITLE = "AUT Connection Error"; //$NON-NLS-1$
	private static String SHOW_FILES_QUESTION = "Would you like to view log file?"; //$NON-NLS-1$

	private static String TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$		

	private AUTConnectionErrorDialog(Shell parentShell, String message,
			boolean showLogFile) {
		super(parentShell, DIALOG_TITLE, null,
				getMessage(showLogFile, message), MessageDialog.ERROR,
				getDialogButtonsLabels(showLogFile), 0);
	}

	private static String getMessage(boolean showLogFile, String errorMessage) {
		if (showLogFile) {
			return errorMessage + "\r\n\r\n" + SHOW_FILES_QUESTION; //$NON-NLS-1$
		}
		return errorMessage;
	}

	private static String[] getDialogButtonsLabels(boolean showLogFile) {
		if (showLogFile) {
			return new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL };
		}
		return new String[] { IDialogConstants.OK_LABEL };
	}

	public static void showAUTConnectionError(Shell parentShell,
			IStatus status, ILaunchConfiguration launch) {
		String message = DEFAULT_ERROR_MESSAGE;
		if (status != null && !status.isOK()
				&& !status.equals(Status.CANCEL_STATUS)) {
			message = format(status);
		}
		if (status == null
				|| (!status.isOK() && !status.equals(Status.CANCEL_STATUS))) {
			File logFile = getConfigLog(launch);
			if (logFile == null) {
				logFile = getWorkspaceLog(launch);
			}
			AUTConnectionErrorDialog dialog = new AUTConnectionErrorDialog(
					parentShell, message, logFile != null);
			if (dialog.open() == 0) {
				openInEditor(logFile);
			}
		}
	}

	private static File getConfigLog(ILaunchConfiguration launch) {
		String filesLocation = Q7LaunchUtils.getConfigFilesLocation(launch);
		File logFile = getLogFile(new File(filesLocation));
		if (logFile != null && logFile.exists() && logFile.isFile()) {
			return logFile;
		}
		return null;
	}

	private static File getWorkspaceLog(ILaunchConfiguration launch) {
		File file = Q7LaunchUtils.getWorkspaceMeta(launch);
		if (file != null) {
			return getLogFile(file);
		}
		return null;
	}

	private static File getLogFile(File baseDirFile) {
		File lastModifiedFile = null;
		if (baseDirFile.exists()) {
			File[] files = baseDirFile.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".log"); //$NON-NLS-1$
				}
			});
			for (File file : files) {
				if (lastModifiedFile == null
						|| lastModifiedFile.lastModified() < file
								.lastModified()) {
					lastModifiedFile = file;
				}
			}
		}
		return lastModifiedFile;
	}

	private static void openInEditor(File fileToOpen) {
		if (fileToOpen != null && fileToOpen.exists() && fileToOpen.isFile()) {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditor(page, fileToOpen.toURI(), TEXT_EDITOR_ID, true);
			} catch (PartInitException e) {
				Q7UIPlugin.log(e);
			}
		}
	}
}