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
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.model.ModelManager.PerWorkingCopyInfo;

public class BecomeWorkingCopyOperation extends Q7Operation {

	private boolean indexing = false;

	public BecomeWorkingCopyOperation(Q7NamedElement workingCopy,
			boolean indexing) {
		super(new IQ7Element[] { workingCopy });
		this.indexing = indexing;
	}

	@Override
	protected void executeOperation() throws ModelException {

		// open the working copy now to ensure contents are that of the current
		// state of this element
		Q7NamedElement workingCopy = getWorkingCopy();
		workingCopy.workingCopyMode = true;
		workingCopy.setIndexing(indexing);
		// create if needed, record usage
		PerWorkingCopyInfo copyInfo = ModelManager.getModelManager()
				.getPerWorkingCopyInfo(workingCopy, true, true);
		
		
		try {
			copyInfo.populate(this.progressMonitor);
			this.resultElements = new IQ7Element[] { workingCopy };
		} catch (ModelException e) {
			workingCopy.discardWorkingCopy();
			throw e;
		} catch (Throwable e) { 
			workingCopy.discardWorkingCopy();
			throw e;
		}
	}

	/*
	 * Returns the working copy this operation is working on.
	 */
	protected Q7NamedElement getWorkingCopy() {
		return (Q7NamedElement) getElementToProcess();
	}

	/*
	 * @see ModelOperation#isReadOnly
	 */
	@Override
	public boolean isReadOnly() {
		return true;
	}

}
