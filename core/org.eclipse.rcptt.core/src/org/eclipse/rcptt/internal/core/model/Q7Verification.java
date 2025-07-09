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
package org.eclipse.rcptt.internal.core.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.VerificationTypeManager;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Verification;

public class Q7Verification extends Q7NamedElement implements IVerification {
	protected Q7Verification(Q7Element parent, String name)
			throws IllegalArgumentException {
		super(parent, name);
	}

	public HandleType getElementType() {
		return HandleType.Verification;
	}

	public VerificationType getType() throws ModelException {
		NamedElement element = getNamedElement();
		return VerificationTypeManager.getInstance().getTypeByVerification(
				(Verification) element);
	}

	@Override
	protected Q7NamedElement createWorkingCopy() {
		return new Q7Verification(parent, name);
	}

	@Override
	protected Q7ResourceInfo createElementInfo() {
		return new Q7ResourceInfo(IPlainConstants.PLAIN_VERIFICATION, Q7ResourceInfo.toURI(getResource()));
	}

	@Override
	protected NamedElement createNamedElement() {
		return null;
	}

	@Override
	public IQ7NamedElement getWorkingCopy(IProgressMonitor monitor) throws ModelException {
		if (!getResource().exists()) {
			throw new ModelException(new Q7Status(0,
					"Could not create verification working copy with unexisted resource"));
		}
		return super.getWorkingCopy(monitor);
	}
}
