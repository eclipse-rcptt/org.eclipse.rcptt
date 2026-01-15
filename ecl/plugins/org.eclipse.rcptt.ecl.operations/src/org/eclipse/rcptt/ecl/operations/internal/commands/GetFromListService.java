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
package org.eclipse.rcptt.ecl.operations.internal.commands;

import static org.eclipse.rcptt.ecl.operations.internal.OperationsPlugin.createErr;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.EclList;
import org.eclipse.rcptt.ecl.core.Get;
import org.eclipse.rcptt.ecl.dispatch.IScriptletExtension;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.ecl.runtime.IProcess;

public class GetFromListService implements IScriptletExtension {

	@Override
	public IStatus service(Command command, IProcess context) throws InterruptedException, CoreException {
		Get get = (Get) command;
		EclList list = (EclList) get.getInput();
		Object indexObject = BoxedValues.unbox(get.getKey());
		if (!(indexObject instanceof Integer)) {
			return createErr("Invalid key: %s. Only integers can be used to get values from lists", indexObject);
		}

		int index = (Integer) indexObject;
		if (index < 0 || index >= list.getElements().size()) {
			if (get.getDefault() == null) {
				return createErr("Index %d is out of bounds and no default value given", index);
			}

			context.getOutput().write(get.getDefault());
			return Status.OK_STATUS;
		}

		context.getOutput().write(EcoreUtil.copy(list.getElements().get(index)));
		return Status.OK_STATUS;
	}

	@Override
	public boolean canHandle(Command c) {
		return c instanceof Get && ((Get) c).getInput() instanceof EclList;
	}

}
