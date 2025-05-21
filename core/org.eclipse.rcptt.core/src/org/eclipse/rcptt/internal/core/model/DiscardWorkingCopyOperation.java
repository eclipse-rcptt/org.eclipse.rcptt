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

import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.model.deltas.Q7ElementDelta;

public class DiscardWorkingCopyOperation extends Q7Operation {

	private boolean indexing;

	public DiscardWorkingCopyOperation(IQ7NamedElement workingCopy,
			boolean indexing) {
		super(new IQ7Element[] { workingCopy });
		this.indexing = indexing;
	}

	protected void executeOperation() throws ModelException, InterruptedException {
		Q7NamedElement workingCopy = getNamedElement();
		ModelManager manager = ModelManager.getModelManager();
		int useCount = manager.discardPerWorkingCopyInfo(workingCopy);
		if (useCount == 0) {
			if (workingCopy.getResource().isAccessible()) {
				// report a F_PRIMARY_WORKING_COPY change delta for a
				// primary working copy
				if (!indexing) {
					Q7ElementDelta delta = new Q7ElementDelta(this.getModel());
					delta.changed(workingCopy, IQ7ElementDelta.F_WORKING_COPY);
					addDelta(delta);
				}
			} else {
				// report a REMOVED delta
				if (!indexing) {
					Q7ElementDelta delta = new Q7ElementDelta(this.getModel());
					delta.removed(workingCopy, IQ7ElementDelta.F_WORKING_COPY);
					addDelta(delta);
				}
			}
		}
	}

	/**
	 * Returns the working copy this operation is working on.
	 */
	protected Q7NamedElement getNamedElement() {
		return (Q7NamedElement) getElementToProcess();
	}

	/**
	 * @see ModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}
}
