/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.rcptt.ecl.internal.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.With;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;

public class WithService implements ICommandService {

	@Override
	public IStatus service(Command command, IProcess process)
			throws InterruptedException, CoreException {
		With with = (With) command;
		ISession session = process.getSession();
		IPipe pipe = session.createPipe();
		pipe.write(with.getObject());
		pipe.close(Status.OK_STATUS);
		IProcess doProcess = session.execute(with.getDo(), pipe, process.getOutput());
		return doProcess.waitFor();
	}
}
