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
package org.eclipse.rcptt.ecl.platform.internal.ui.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ClearLogViewService implements ICommandService {
	private static final String LOG_ID = "org.eclipse.pde.runtime.LogView";

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		final Exception[] exceptions = new Exception[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					IViewPart view = page.findView(LOG_ID);
					boolean viewOpened = view != null;
					if (viewOpened) {
						page.hideView(view);
					}
					Platform.getLogFileLocation().toFile().delete();
					if (viewOpened) {
						page.showView(LOG_ID);
					}
				} catch (PartInitException e) {
					exceptions[0] = e;
				}
			}
		});
		if (exceptions[0] != null) {
			return Utils.error("Error clearing log view", exceptions[0]);
		}
		return Status.OK_STATUS;
	}

}
