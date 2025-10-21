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
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.model.ModelManager.PerWorkingCopyInfo;
import org.eclipse.rcptt.internal.core.model.deltas.Q7ElementDelta;

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
			// synchronize not to leak resourceInfo for readOnly operations, which work outside of workspace locks
			boolean changed = false;
			synchronized (copyInfo) {
				if (copyInfo.resourceInfo == null) { 
					copyInfo.resourceInfo = (Q7ResourceInfo) workingCopy
							.createElementInfo();
					workingCopy.generateInfos(copyInfo.resourceInfo, this.progressMonitor);
					changed =  true;
				}
			}
			// workingCopy.extractAllPersistence();
			if (changed) {
				if (workingCopy.getResource().isAccessible()) {
					// report a F_PRIMARY_WORKING_COPY change delta for a primary
					// working copy
					if (!indexing) {
						Q7ElementDelta delta = new Q7ElementDelta(this.getModel());
						delta.changed(workingCopy, IQ7ElementDelta.F_WORKING_COPY);
						addDelta(delta);
					}
				} else {
					if (!indexing) {
						// report an ADDED delta
						Q7ElementDelta delta = new Q7ElementDelta(this.getModel());
						delta.added(workingCopy, IQ7ElementDelta.F_WORKING_COPY);
						addDelta(delta);
					}
				}
			}

			this.resultElements = new IQ7Element[] { workingCopy };
		} catch (ModelException e) {
			workingCopy.discardWorkingCopy();
			throw e;
		} catch (Throwable e) { 
			workingCopy.discardWorkingCopy();
			throw e;
		} finally {
			synchronized (copyInfo) {
				copyInfo.complete = true;
				copyInfo.notifyAll();
			}
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
