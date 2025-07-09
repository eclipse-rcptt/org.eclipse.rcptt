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
package org.eclipse.rcptt.ctx.parameters.impl.rap;

import java.util.function.BooleanSupplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Declaration;
import org.eclipse.rcptt.ecl.core.Global;
import org.eclipse.rcptt.ecl.core.Val;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.ecl.runtime.ISession;

import org.eclipse.rcptt.core.IContextProcessor;
import org.eclipse.rcptt.core.IEclAwareProcessor;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.parameters.Parameter;
import org.eclipse.rcptt.parameters.ParametersContext;

public class ParametersContextProcessor implements IContextProcessor, IEclAwareProcessor {

	public boolean isApplied(Context context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void apply(Context context, BooleanSupplier isCancelled) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				getClass().getName() + " is ECL-aware and must be passed a session object", null));
	}

	public Context create(EObject param) throws CoreException {
		throw new UnsupportedOperationException();
	}

	public void apply(Context context, ISession session) throws CoreException {
		ParametersContext ctx = (ParametersContext) context;
		for (Parameter p : ctx.getParameters()) {
			ParametersRuntime.setParam(p.getName(), p.getValue());
		}
		declareVariables(session, ctx.getParameters());

	}

	private void declareVariables(ISession session, Iterable<Parameter> params) throws CoreException {
		if (session == null) {
			return;
		}

		Global global = CoreFactory.eINSTANCE.createGlobal();
		global.setOverride(true);
		EList<Declaration> vals = global.getVals();
		for (Parameter p : params) {
			Val val = CoreFactory.eINSTANCE.createVal();
			val.setName(p.getName());
			val.setValue(BoxedValues.box(p.getValue()));
			vals.add(val);
		}
		try {
			IStatus status = session.execute(global).waitFor();
			if (!status.isOK()) {
				throw new CoreException(status);
			}
		} catch (InterruptedException e) {
			throw new CoreException(Activator.createErr(e, "Global variables declaration has been interrupted"));
		}
	}

}
