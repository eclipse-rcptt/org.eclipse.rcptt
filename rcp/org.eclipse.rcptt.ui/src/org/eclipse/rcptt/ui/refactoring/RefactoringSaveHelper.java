/*******************************************************************************
 * Copyright (c) 2009, 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

import org.eclipse.rcptt.internal.ui.Messages;

public class RefactoringSaveHelper {

	private final static String RefactoringStarter_save_all_resources = Messages.RefactoringSaveHelper_SaveAllResourcesMsg;
	private final static String RefactoringStarter_must_save = Messages.RefactoringSaveHelper_MustSaveMsg;

	public static boolean checkDirtyEditors(Shell parentShell) {
		final IEditorPart[] dirtyEditors = getDirtyEditors();
		if (dirtyEditors.length > 0) {
			if (askSaveAllDirtyEditors(parentShell, dirtyEditors)) {
				saveDirtyEditors(dirtyEditors);
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	public static void saveDirtyEditors(final IEditorPart[] dirtyEditors) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				for (IEditorPart part : dirtyEditors) {
					part.doSave(new NullProgressMonitor());
				}
			}
		});
	}

	public static IEditorPart[] getDirtyEditors() {

		final List<IEditorPart> dirtyEditors = new ArrayList<IEditorPart>();

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null)
					return;
				IWorkbenchPage page = window.getActivePage();
				if (page == null)
					return;
				IEditorReference[] references = page.getEditorReferences();
				for (IEditorReference reference : references) {
					IEditorPart part = reference.getEditor(false);
					if (part != null && part.isDirty()) {
						dirtyEditors.add(part);
					}
				}
			}
		});

		return dirtyEditors.toArray(new IEditorPart[dirtyEditors.size()]);
	}

	public static boolean askSaveAllDirtyEditors(Shell shell,
			IEditorPart[] dirtyEditors) {

		ListDialog dialog = new ListDialog(shell) {
			{
				setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
			}
		};
		dialog.setTitle(RefactoringStarter_save_all_resources);
		dialog.setLabelProvider(createDialogLabelProvider());
		dialog.setMessage(RefactoringStarter_must_save);
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setInput(Arrays.asList(dirtyEditors));
		dialog.setHelpAvailable(false);
		return dialog.open() == Window.OK;
	}

	private static ILabelProvider createDialogLabelProvider() {
		return new LabelProvider() {
			public Image getImage(Object element) {
				return ((IEditorPart) element).getTitleImage();
			}

			public String getText(Object element) {
				return ((IEditorPart) element).getTitle();
			}
		};
	}
}
