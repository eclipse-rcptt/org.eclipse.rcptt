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
package org.eclipse.rcptt.tesla.ecl.internal.impl.commands;

import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ShutdownAutService implements ICommandService {

	@Override
	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		try {
			tryAsyncExec(wb -> {
				for (IWorkbenchWindow window : wb.getWorkbenchWindows()) {
					for (IWorkbenchPage page : window.getPages()) {
						page.closeAllEditors(false); // Prevent editors from prompting to save on close, by closing them before they have chance
					}
				}
				// TODO: close E4 editors. See org.eclipse.ui.internal.Workbench.saveAllParts(boolean, boolean)
			});
		} catch (NoClassDefFoundError e) {
			// if plugin is not loaded, rely on other means of shutting down
		}
		
		try {
			tryAsyncExec(IWorkbench::close);
		} catch (NoClassDefFoundError e) {
			// if plugin is not loaded, rely on other means of shutting down
		}
		return Status.OK_STATUS;
	}
	
	private static final void tryAsyncExec(Consumer<IWorkbench> runnable) {
		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			if (!wb.isClosing()) {
				wb.getDisplay().asyncExec(() -> {
					if (!wb.isClosing()) {
						runnable.accept(wb);
					}
				}); 
			}
			
		} catch (NoClassDefFoundError e) {
			// workbench is not loaded, rely on other means of shutdown.
		}

	}
}
