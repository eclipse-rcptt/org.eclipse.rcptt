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
package org.eclipse.rcptt.ecl.operations.internal.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Let;
import org.eclipse.rcptt.ecl.core.Val;
import org.eclipse.rcptt.ecl.internal.core.CorePlugin;
import org.eclipse.rcptt.ecl.operations.Repeat;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.ecl.runtime.CoreUtils;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;

public class RepeatService implements ICommandService {

	public IStatus service(Command command, IProcess process)
			throws InterruptedException, CoreException {
		Repeat t = (Repeat) command;
		int times = t.getTimes();
		int delay = t.getDelay();
		if (delay < 0)
			return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
					"Illegal parameter 'delay'");
		if (times <= 0)
			return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
					"Illegal parameter 'times'");
		List<Object> content = CoreUtils.readPipeContent(process.getInput());
		IStatus status = Status.OK_STATUS;

		Val indexVal = t.getIndex();
		List<Object> contentOutput = new ArrayList<Object>();

		for (int i = 0; i < times; i++) {
			if (!process.isAlive()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			IPipe input = process.getSession().createPipe();
			for (Object o : content)
				input.write(o);
			input.close(Status.OK_STATUS);
			IPipe output = process.getSession().createPipe();

			IProcess doProcess = process.getSession().execute(wrapBody(i, indexVal, t.getCommand()),
					input, output);
			status = doProcess.waitFor();
			if (status.isOK()) {
				contentOutput.addAll(CoreUtils.readPipeContent(output));
			} else {
				break;
			}
			if (delay > 0) {
				Thread.sleep(delay);
			}
		}
		if (status.isOK()) {
			for (Object o : contentOutput) {
				process.getOutput().write(o);
			}
		}
		return status;
	}

	private static Command wrapBody(int i, Val indexVal, Command body) {
		Let let = CoreFactory.eINSTANCE.createLet();
		let.setBody(body);

		if (indexVal != null) {
			indexVal.setValue(BoxedValues.box(i));
			let.getVals().add(indexVal);
		}
		return let;
	}

}
