/*******************************************************************************
 * Copyright (c) 2009, 2014 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.runtime.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.core.OptionsHandler;
import org.eclipse.rcptt.core.ecl.core.model.SetOption;
import org.eclipse.rcptt.core.ecl.core.model.SetQ7Option;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;

public class SetOptionService implements ICommandService {

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		if (command instanceof SetOption) {
			SetOption setOption = (SetOption) command;
			applyOption(setOption.getKey(), setOption.getValue());
		} else if (command instanceof SetQ7Option) {
			SetQ7Option setQ7Option = (SetQ7Option) command;
			applyOption(setQ7Option.getKey(), setQ7Option.getValue());
		}
		return Status.OK_STATUS;
	}

	private static void applyOption(String name, String value) {
		new OptionsHandler().applyOption(name, value);
		if (name.equals(TeslaFeatures.STATUS_DIALOG_ALLOWED)) {
			TeslaEventManager.getManager().setStatusDialogModeAllowed(Boolean.valueOf(value));
		}
	}

}
