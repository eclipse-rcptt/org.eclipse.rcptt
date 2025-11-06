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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.workspace.RcpttCore;

public class CommitWorkingCopyOperation extends Q7Operation {
	/**
	 * Constructs an operation to commit the contents of a working copy to its
	 * original compilation unit.
	 */
	public CommitWorkingCopyOperation(final IQ7NamedElement element,
			boolean force) {
		super(new IQ7Element[] { element }, force);
	}

	/**
	 * @exception ModelException
	 *                if setting the source of the original compilation unit
	 *                fails
	 */
	@Override
	protected void executeOperation() throws ModelException {
		try {
			beginTask("Commit working copy", 2);
			Q7NamedElement workingCopy = getNamedElement();

			IQ7NamedElement primary = workingCopy.getPrimary();

			IFile resource = (IFile) workingCopy.getResource();
			if (RcpttCore.isQ7File(resource.getFullPath())) {
				// force opening so that the delta builder can get the old info
				// if (resource.exists()) {
				// if (!primary.isOpen()) {
				// primary.open(null);
				// }
				// }

				workingCopy.writeWorkingCopy(info -> info.save());
			}

			setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);

			// make sure working copy is in sync
			workingCopy.updateTimeStamp((Q7NamedElement) primary);
			worked(1);
		} finally {
			done();
		}
	}

	/**
	 * Returns the source namedelement this operation is working on.
	 */
	protected Q7NamedElement getNamedElement() {
		return (Q7NamedElement) getElementToProcess();
	}

	@Override
	protected ISchedulingRule getSchedulingRule() {
		IResource resource = getElementToProcess().getResource();
		if (resource == null)
			return null;
		IWorkspace workspace = resource.getWorkspace();
		if (resource.exists()) {
			return workspace.getRuleFactory().modifyRule(resource);
		} else {
			return workspace.getRuleFactory().createRule(resource);
		}
	}

	@Override
	public Q7Status verify() {
		Q7NamedElement cu = getNamedElement();
		if (!cu.isWorkingCopy()) {
			return new Q7Status(0, "Is not a working copy");
		}
		try {
			if (cu.hasResourceChanged() && !this.force) {
				return new Q7Status(0, "Update conflict");
			}
		} catch (ModelException e) {
			return e.getQ7Status();
		}
		return Q7Status.OK;
	}
}
