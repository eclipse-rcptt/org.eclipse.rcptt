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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Let;
import org.eclipse.rcptt.ecl.core.Val;
import org.eclipse.rcptt.ecl.core.util.ISessionPropertyConstants;
import org.eclipse.rcptt.ecl.internal.core.CorePlugin;
import org.eclipse.rcptt.ecl.internal.core.ProcessStatusConverter;
import org.eclipse.rcptt.ecl.operations.Try;
import org.eclipse.rcptt.ecl.runtime.CoreUtils;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;

public class TryService implements ICommandService, ISessionPropertyConstants {

	public IStatus service(Command command, IProcess process) throws InterruptedException, CoreException {
		Try t = (Try) command;
		Integer times = t.getTimes();
		Integer delay = t.getDelay();
		Val errorVal = t.getError();
		if (delay == null)
			delay = 100;
		if (delay < 0)
			return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Parameter 'delay' should not be negative");
		if (times != null && times <= 0)
			return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Parameter 'times' should be greater than zero");
		if (times == null) {
			times = Integer.valueOf(1);
		}
		final List<Object> inputContent = CoreUtils.readPipeContent(process.getInput());
		IStatus status = Status.OK_STATUS;
		ISession session = process.getSession();
		try {
			for (int i = 0; i < times; i++) {
				if (!process.isAlive()) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
				if (i + 1 == times && !t.isNoScreenshot()) {
					session.putProperty(NO_SCREENSHOT, null);
				} else {
					session.putProperty(NO_SCREENSHOT, Boolean.TRUE);
				}
				IPipe input = session.createPipe();
				for (Object o : inputContent)
					input.write(o);
				input.close(Status.OK_STATUS);
				IPipe output = session.createPipe();
				IProcess doProcess = session.execute(t.getCommand(), input, output);
				status = doProcess.waitFor();
				if (status.isOK()) {
					List<Object> outputContent = CoreUtils.readPipeContent(output);
					for (Object o : outputContent)
						process.getOutput().write(o);
					// return status;
					break;
				}
				if (delay > 0) {
					Thread.sleep(delay);
				}
			}
			// Do catch
			if (!status.isOK()) {
				if (t.getCatch() != null) {
					Let catchBlock = CoreFactory.eINSTANCE.createLet();
					catchBlock.setBody(EcoreUtil.copy(t.getCatch()));
					if (errorVal != null) {
						errorVal.setValue(ProcessStatusConverter.toProcessStatus(status));
						catchBlock.getVals().add(errorVal);
					}
					session.putProperty(NO_SCREENSHOT, null);
					IPipe input = session.createPipe();
					for (Object o : inputContent)
						input.write(o);
					input.close(Status.OK_STATUS);
					IPipe output = session.createPipe();
					IProcess doProcess = session.execute(catchBlock, input, output);
					IStatus status2 = doProcess.waitFor();
					if (status2.isOK()) {
						List<Object> outputContent = CoreUtils.readPipeContent(output);
						for (Object o : outputContent)
							process.getOutput().write(o);
						status = status2;
					} else {
						status = status2;
					}
				}
			}
		} finally {
			try {
				if (t.getFinally() != null) {
					session.putProperty(NO_SCREENSHOT, null);
					IPipe input = session.createPipe();
					for (Object o : inputContent)
						input.write(o);
					input.close(Status.OK_STATUS);
					IPipe output = session.createPipe();
					IProcess doProcess = session.execute(t.getFinally(), input, output);
					IStatus status2 = doProcess.waitFor();
					if (status2.isOK()) {
						List<Object> outputContent = CoreUtils.readPipeContent(output);
						for (Object o : outputContent)
							process.getOutput().write(o);
					} else {
						status = status2;
					}
				}
			} finally {
				session.putProperty(NO_SCREENSHOT, null);
			}

		}
		return status;
	}

}
