/*******************************************************************************
 * Copyright (c) 2009, 2020 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.core.scenario.NamedElement;

public class Q7Context extends Q7NamedElement implements IContext {
	protected Q7Context(Q7Element parent, String name)
			throws IllegalArgumentException {
		super(parent, name);
	}

	public HandleType getElementType() {
		return HandleType.Context;
	}

	public ContextType getType() throws ModelException {
		NamedElement element = getNamedElement();
		return ContextTypeManager.getInstance().getTypeByContext(
				(Context) element);
	}

	@Override
	protected Q7NamedElement createWorkingCopy() {
		return new Q7Context(parent, name);
	}

	@Override
	protected NamedElement createNamedElement() {
		return null;
	}

	@Override
	public IQ7NamedElement getWorkingCopy(IProgressMonitor monitor)
			throws ModelException {
		if (!getResource().exists()) {
			throw new ModelException(
					new Q7Status(0,
							"Could not create context working copy with unexisted resource"));
		}
		return super.getWorkingCopy(monitor);
	}
	
	@Override
	public boolean isBuiltin() {
		return false;
	}
}
