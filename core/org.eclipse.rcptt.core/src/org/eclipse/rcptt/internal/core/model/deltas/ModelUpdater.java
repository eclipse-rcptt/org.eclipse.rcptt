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

package org.eclipse.rcptt.internal.core.model.deltas;

import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.core.model.Openable;
import org.eclipse.rcptt.internal.core.model.Q7ElementInfo;
import org.eclipse.rcptt.internal.core.model.Q7Project;

public class ModelUpdater {

	protected void addToParentInfo(Openable child) {

		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			Q7ElementInfo info = (Q7ElementInfo) parent.getElementInfo();
			info.addChild(child);
		}
	}

	protected static void close(Openable element) {

		try {
			element.close();
		} catch (ModelException e) {
			// do nothing
		}
	}

	protected void elementAdded(Openable element) {

		HandleType elementType = element.getElementType();
		if (elementType.equals(HandleType.Project)) {

			addToParentInfo(element);
		} else {
			addToParentInfo(element);

			close(element);
		}
	}

	protected void elementChanged(Openable element) {

		close(element);
	}

	protected void elementRemoved(Openable element) {

		if (element.isOpen()) {
			close(element);
		}
		removeFromParentInfo(element);
		HandleType elementType = element.getElementType();

		switch (elementType) {
		case Model:
			ModelManager.getModelManager().getIndexManager().reset();
			break;
		case Project:
			// ModelManager manager = ModelManager.getModelManager();
			// Q7Project q7Project = (Q7Project) element;
			// manager.containerRemove(q7Project);
			break;
		case Folder:
			// get rid of package fragment cache
			Q7Project project = (Q7Project) element.getQ7Project();
			project.resetCaches();
			break;
		}
	}

	/**
	 * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code>
	 * into the corresponding set of <code>IModelElementDelta</code>, rooted in
	 * the relevant <code>Model</code>s.
	 */
	public void processDelta(IQ7ElementDelta delta) {

		// if (DeltaProcessor.VERBOSE){
		// System.out.println("UPDATING Model with Delta: ["+Thread.currentThread()+":"
		// + delta + "]:");
		// }

		try {
			this.traverseDelta(delta, null); // traverse delta
		} finally {
		}
	}

	/**
	 * Removes the given element from its parents cache of children. If the
	 * element does not have a parent, or the parent is not currently open, this
	 * has no effect.
	 */
	protected void removeFromParentInfo(Openable child) {

		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			Q7ElementInfo info = (Q7ElementInfo) parent.getElementInfo();
			info.removeChild(child);
		}
	}

	/**
	 * Converts an <code>IResourceDelta</code> and its children into the
	 * corresponding <code>IModelElementDelta</code>s. Return whether the delta
	 * corresponds to a resource on the buildpath. If it is not a resource on
	 * the buildpath, it will be added as a non-java resource by the sender of
	 * this method.
	 */
	protected void traverseDelta(IQ7ElementDelta delta, IQ7Project project) {

		boolean processChildren = true;

		Openable element = (Openable) delta.getElement();
		switch (element.getElementType()) {
		case Project:
			project = (IQ7Project) element;
			break;
		}

		switch (delta.getKind()) {
		case IQ7ElementDelta.ADDED:
			elementAdded(element);
			break;
		case IQ7ElementDelta.REMOVED:
			elementRemoved(element);
			break;
		case IQ7ElementDelta.CHANGED:
			if ((delta.getFlags() & IQ7ElementDelta.F_CONTENT) != 0) {
				elementChanged(element);
			}
			break;
		}
		if (processChildren) {
			IQ7ElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++) {
				IQ7ElementDelta childDelta = children[i];
				this.traverseDelta(childDelta, project);
			}
		}
	}
}
