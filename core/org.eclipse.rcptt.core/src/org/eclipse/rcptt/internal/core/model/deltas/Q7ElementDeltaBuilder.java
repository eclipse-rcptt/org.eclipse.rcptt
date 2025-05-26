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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.rcptt.core.model.IParent;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.model.Q7Status.Q7StatusCode;
import org.eclipse.rcptt.internal.core.model.Openable;
import org.eclipse.rcptt.internal.core.model.Q7Element;
import org.eclipse.rcptt.internal.core.model.Q7ElementInfo;
import org.eclipse.rcptt.internal.core.model.Q7Model;
import org.eclipse.rcptt.internal.core.model.Q7ResourceInfo;

@SuppressWarnings("rawtypes")
public class Q7ElementDeltaBuilder {
	/**
	 * The model element handle
	 */
	IQ7Element modelElement;

	/**
	 * The maximum depth in the q7 element children we should look into
	 */
	int maxDepth = Integer.MAX_VALUE;

	/**
	 * The old handle to info relationships
	 */
	Map infos;

	/**
	 * The old position info
	 */
	Map oldPositions;

	/**
	 * The new position info
	 */
	Map newPositions;

	/**
	 * Change delta
	 */
	public Q7ElementDelta delta;

	/**
	 * List of added elements
	 */
	ArrayList added;

	/**
	 * List of removed elements
	 */
	ArrayList removed;

	/**
	 * Doubly linked list item
	 */
	static class ListItem {
		public IQ7Element previous;
		public IQ7Element next;

		public ListItem(IQ7Element previous, IQ7Element next) {
			this.previous = previous;
			this.next = next;
		}
	}

	/**
	 * Creates a q7 element comparator on a q7 element looking as deep as
	 * necessary.
	 * @throws InterruptedException 
	 */
	public Q7ElementDeltaBuilder(IQ7Element modelElement) throws InterruptedException {
		this.modelElement = modelElement;
		this.initialize();
		this.recordElementInfo(modelElement, (Q7Model) this.modelElement.getModel(), 0);
	}

	/**
	 * Creates a q7 element comparator on a q7 element looking only 'maxDepth'
	 * levels deep.
	 * @throws InterruptedException 
	 */
	public Q7ElementDeltaBuilder(IQ7Element modelElement, int maxDepth) throws InterruptedException {
		this.modelElement = modelElement;
		this.maxDepth = maxDepth;
		this.initialize();
		this.recordElementInfo(modelElement, (Q7Model) this.modelElement.getModel(), 0);
	}

	/**
	 * Repairs the positioning information after an element has been added
	 */
	@SuppressWarnings("unchecked")
	private void added(IQ7Element element) {
		this.added.add(element);
		ListItem current = this.getNewPosition(element);
		ListItem previous = null, next = null;
		if (current.previous != null)
			previous = this.getNewPosition(current.previous);
		if (current.next != null)
			next = this.getNewPosition(current.next);
		if (previous != null)
			previous.next = current.next;
		if (next != null)
			next.previous = current.previous;
	}

	/**
	 * Builds the q7 element deltas between the old content of the compilation
	 * unit and its new content.
	 */
	public void buildDeltas() throws InterruptedException {
		this.delta = new Q7ElementDelta(modelElement);

		// if building a delta on a compilation unit or below,
		// it's a fine grained delta
		if (modelElement.getElementType().equals(HandleType.Context)
				|| modelElement.getElementType().equals(HandleType.Verification)
				|| modelElement.getElementType().equals(HandleType.TestCase)
				|| modelElement.getElementType().equals(HandleType.TestSuite)) {
			this.delta.fineGrained();
		}
		this.recordNewPositions(this.modelElement, 0);
		this.findAdditions(this.modelElement, 0);
		this.findDeletions();
		this.findChangesInPositioning(this.modelElement, 0);
		this.trimDelta(this.delta);
		if (this.delta.getAffectedChildren().length == 0) {
			// this is a fine grained but not children affected -> mark as
			// content changed
			this.delta.contentChanged();
		}
	}

	/**
	 * Finds elements which have been added or changed.
	 * @throws InterruptedException 
	 */
	private void findAdditions(IQ7Element newElement, int depth) throws InterruptedException {
		Q7ElementInfo oldInfo = this.getElementInfo(newElement);
		if (oldInfo == null && depth < this.maxDepth) {
			this.delta.added(newElement);
			added(newElement);
		} else {
			this.removeElementInfo(newElement);
		}

		if (depth >= this.maxDepth) {
			// mark element as changed
			this.delta.changed(newElement, IQ7ElementDelta.F_CONTENT);
			return;
		}

		Q7ElementInfo newInfo = leakInfo(newElement);

		this.findContentChange(oldInfo, newInfo, newElement);

		if (oldInfo != null && newElement instanceof IParent) {

			IQ7Element[] children = newInfo.getChildren();
			if (children != null) {
				int length = children.length;
				for (int i = 0; i < length; i++) {
					this.findAdditions(children[i], depth + 1);
				}
			}
		}
	}

	private Q7ElementInfo leakInfo(IQ7Element newElement) throws InterruptedException {
		Q7ElementInfo newInfo;
		try {
			((Openable)newElement.getOpenable()).openAndAccessInfo(parentInfo -> {return null;}, null);
			newInfo = ((Q7Element) newElement).accessInfo(info -> info);
		} catch (ModelException e) {
			throw new IllegalStateException(e);
		}
		return newInfo;
	}

	/**
	 * Looks for changed positioning of elements.
	 * @throws InterruptedException 
	 */
	private void findChangesInPositioning(IQ7Element element, int depth) throws InterruptedException {
		if (depth >= this.maxDepth || this.added.contains(element)
				|| this.removed.contains(element))
			return;

		if (!isPositionedCorrectly(element)) {
			this.delta.changed(element, IQ7ElementDelta.F_REORDER);
		}

		if (element instanceof IParent) {
			Q7ElementInfo info = leakInfo(element);
			IQ7Element[] children = info.getChildren();
			if (children != null) {
				int length = children.length;
				for (int i = 0; i < length; i++) {
					this.findChangesInPositioning(children[i], depth + 1);
				}
			}
		}
	}

	/**
	 * The elements are equivalent, but might have content changes.
	 */
	private void findContentChange(Q7ElementInfo oldInfo, Q7ElementInfo newInfo,
			IQ7Element newElement) {
		if (oldInfo instanceof Q7ResourceInfo && newInfo instanceof Q7ResourceInfo) {
			this.delta.changed(newElement, IQ7ElementDelta.F_CONTENT);

		}
	}

	/**
	 * Adds removed deltas for any handles left in the table
	 */
	private void findDeletions() {
		Iterator iter = this.infos.keySet().iterator();
		while (iter.hasNext()) {
			IQ7Element element = (IQ7Element) iter.next();
			this.delta.removed(element);
			this.removed(element);
		}
	}

	private Q7ElementInfo getElementInfo(IQ7Element element) {
		return (Q7ElementInfo) this.infos.get(element);
	}

	private ListItem getNewPosition(IQ7Element element) {
		return (ListItem) this.newPositions.get(element);
	}

	private ListItem getOldPosition(IQ7Element element) {
		return (ListItem) this.oldPositions.get(element);
	}

	private void initialize() {
		this.infos = new HashMap(20);
		this.oldPositions = new HashMap(20);
		this.newPositions = new HashMap(20);
		this.putOldPosition(this.modelElement, new ListItem(null, null));
		this.putNewPosition(this.modelElement, new ListItem(null, null));

		this.added = new ArrayList(5);
		this.removed = new ArrayList(5);
	}

	/**
	 * Inserts position information for the elements into the new or old
	 * positions table
	 */
	private void insertPositions(IQ7Element[] elements, boolean isNew) {
		int length = elements.length;
		IQ7Element previous = null, current = null, next = (length > 0) ? elements[0]
				: null;
		for (int i = 0; i < length; i++) {
			previous = current;
			current = next;
			next = (i + 1 < length) ? elements[i + 1] : null;
			if (isNew) {
				this.putNewPosition(current, new ListItem(previous, next));
			} else {
				this.putOldPosition(current, new ListItem(previous, next));
			}
		}
	}

	/**
	 * Returns whether the elements position has not changed.
	 */
	private boolean isPositionedCorrectly(IQ7Element element) {
		ListItem oldListItem = this.getOldPosition(element);
		if (oldListItem == null)
			return false;

		ListItem newListItem = this.getNewPosition(element);
		if (newListItem == null)
			return false;

		IQ7Element oldPrevious = oldListItem.previous;
		IQ7Element newPrevious = newListItem.previous;
		if (oldPrevious == null) {
			return newPrevious == null;
		} else {
			return oldPrevious.equals(newPrevious);
		}
	}

	@SuppressWarnings("unchecked")
	private void putElementInfo(IQ7Element element, Q7ElementInfo info) {
		this.infos.put(element, info);
	}

	@SuppressWarnings("unchecked")
	private void putNewPosition(IQ7Element element, ListItem position) {
		this.newPositions.put(element, position);
	}

	@SuppressWarnings("unchecked")
	private void putOldPosition(IQ7Element element, ListItem position) {
		this.oldPositions.put(element, position);
	}

	/**
	 * Records this elements info, and attempts to record the info for the
	 * children.
	 */
	private void recordElementInfo(IQ7Element element, Q7Model model, int depth) throws InterruptedException {
		if (depth >= this.maxDepth) {
			return;
		}
		Q7ElementInfo info;
		try {
			((Openable)element.getOpenable()).openAndAccessInfo(parentInfo -> {return null;}, null);
			info = ((Q7Element) element).accessInfo(info1 -> info1);
			assert info != null;
		} catch (ModelException e1) {
			if (e1.getStatus() instanceof Q7Status) {
				if (((Q7Status) e1.getStatus()).getStatusCode() == Q7StatusCode.NotPressent) {
					return;
				}
			}
			throw new RuntimeException(e1); 
		}
		this.putElementInfo(element, info);

		if (element instanceof IParent) {
			IQ7Element[] children = info.getChildren();
			if (children != null) {
				insertPositions(children, false);
				for (int i = 0, length = children.length; i < length; i++)
					recordElementInfo(children[i], model, depth + 1);
			}
		}
	}

	/**
	 * Fills the newPositions hashtable with the new position information
	 * @throws InterruptedException 
	 */
	private void recordNewPositions(IQ7Element newElement, int depth) throws InterruptedException {
		if (depth < this.maxDepth && newElement instanceof IParent) {
			Q7ElementInfo info = leakInfo(newElement);

			IQ7Element[] children = info.getChildren();
			if (children != null) {
				insertPositions(children, true);
				for (int i = 0, length = children.length; i < length; i++) {
					recordNewPositions(children[i], depth + 1);
				}
			}
		}
	}

	/**
	 * Repairs the positioning information after an element has been removed
	 */
	@SuppressWarnings("unchecked")
	private void removed(IQ7Element element) {
		this.removed.add(element);
		ListItem current = this.getOldPosition(element);
		ListItem previous = null, next = null;
		if (current.previous != null)
			previous = this.getOldPosition(current.previous);
		if (current.next != null)
			next = this.getOldPosition(current.next);
		if (previous != null)
			previous.next = current.next;
		if (next != null)
			next.previous = current.previous;

	}

	private void removeElementInfo(IQ7Element element) {
		this.infos.remove(element);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Built delta:\n"); //$NON-NLS-1$
		buffer.append(this.delta.toString());
		return buffer.toString();
	}

	/**
	 * Trims deletion deltas to only report the highest level of deletion
	 */
	private void trimDelta(Q7ElementDelta elementDelta) {
		if (elementDelta.getKind() == IQ7ElementDelta.REMOVED) {
			IQ7ElementDelta[] children = elementDelta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				elementDelta.removeAffectedChild((Q7ElementDelta) children[i]);
			}
		} else {
			IQ7ElementDelta[] children = elementDelta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				trimDelta((Q7ElementDelta) children[i]);
			}
		}
	}
}
