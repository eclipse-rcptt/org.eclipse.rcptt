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
package org.eclipse.rcptt.ecl.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Foreach;
import org.eclipse.rcptt.ecl.core.Let;
import org.eclipse.rcptt.ecl.core.Val;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.ecl.runtime.CoreUtils;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;

public class ForeachService implements ICommandService {

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		Foreach foreach = (Foreach) command;
		IStatus status = Status.OK_STATUS;
		Val val = foreach.getItem();
		Val indexVal = foreach.getIndex();
		boolean valSet = val != null;

		EList<EObject> objects = foreach.getInput();
		for (int i = 0, size = objects.size(); i < size; i++) {
			if (!context.isAlive()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			EObject o = objects.get(i);
			ISession session = context.getSession();
			IPipe in = null;
			if (valSet) {
				val.setValue(o);
				LetService.getLocals(context).declare(val.getName(), val, true);
			} else {
				in = session.createPipe();
				in.write(o);
				in.close(Status.OK_STATUS);
			}
			IPipe out = session.createPipe();
			Command doCommand = foreach.getDo();

			status = session.execute(wrapBody(i, indexVal, doCommand), in, out).waitFor();
			if (status.getSeverity() != IStatus.OK) {
				break;
			}
			for (Object outObj : CoreUtils.readPipeContent(out)) {
				context.getOutput().write(outObj);
			}
		}
		return status;
	}

	private static Command wrapBody(int i, Val indexVal, Command body) {
		Let let = CoreFactory.eINSTANCE.createLet();
		let.setBody(EcoreUtil.copy(body));

		if (indexVal != null) {
			indexVal.setValue(BoxedValues.box(i));
			let.getVals().add(indexVal);
		}
		return let;
	}

}
