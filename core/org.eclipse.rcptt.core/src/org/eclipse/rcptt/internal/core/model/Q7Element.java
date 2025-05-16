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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.rcptt.core.model.IOpenable;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementVisitor;
import org.eclipse.rcptt.core.model.IQ7Model;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.model.Q7Status.Q7StatusCode;

public abstract class Q7Element extends PlatformObject implements IQ7Element {

	public static final IQ7Element[] NO_ELEMENTS = new IQ7Element[0];
	protected static final Object NO_INFO = new Object();
	protected Q7Element parent;

	protected Q7Element(Q7Element parent) throws IllegalArgumentException {
		this.parent = parent;
	}

	public Object getElementInfo() throws ModelException {
		return getElementInfo(null);
	}

	public Object getElementInfo(IProgressMonitor monitor)
			throws ModelException {
		ModelManager manager = ModelManager.getModelManager();
		Object info = manager.getInfo(this);
		if (info != null)
			return info;
		Object result = createElementInfo();
		try {
			return openWhenClosed(result, monitor);
		} catch (Throwable e) {
			closing(result);
			throw e;
		}
	}

	protected abstract Object createElementInfo();

	protected boolean isInWorkingCopyMode() {
		return false;
	}

	protected abstract void generateInfos(Object info,
			Map<IQ7Element, Object> newElements, IProgressMonitor pm)
			throws ModelException;

	public IOpenable getOpenable() {
		return this.getOpenableParent();
	}

	public IOpenable getOpenableParent() {
		return (IOpenable) this.parent;
	}

	protected final synchronized Object openWhenClosed(Object info,
			IProgressMonitor monitor) throws ModelException {
		ModelManager manager = ModelManager.getModelManager();
		Map<IQ7Element, Object> newElements = new HashMap<IQ7Element, Object>();
		generateInfos(info, newElements, monitor);
		if (info == null) {
			info = newElements.get(this);
		}
		if (info == null) {
			throw newNotPresentException();
		}
		manager.putInfos(this, newElements);
		Object info2 = manager.getInfo(this);
		return info2;
	}

	public void close() throws ModelException {
		ModelManager.getModelManager().removeInfoAndChildren(this);
	}

	protected abstract void closing(Object info) throws ModelException;

	public boolean exists() {
		try {
			getElementInfo();
			return true;
		} catch (ModelException e) {
			if (e.getStatus() instanceof Q7Status)
				if (((Q7Status) e.getStatus()).getStatusCode() == Q7StatusCode.NotPressent)
					return false;
			throw new RuntimeException(e); 
		}
	}

	public IQ7Element getAncestor(HandleType type) {
		IQ7Element element = this;
		while (element != null) {
			if (element.getElementType().equals(type))
				return element;
			element = element.getParent();
		}
		return null;
	}

	public ModelException newNotPresentException() {
		Q7Status status = new Q7Status(Q7Status.ERROR, "Element: " + getPath()
				+ " doesn't exist");
		status.setStatusCode(Q7StatusCode.NotPressent);
		return new ModelException(status);
	}

	public IQ7Element getParent() {
		return this.parent;
	}

	public IQ7Project getQ7Project() {
		IQ7Element current = this;
		do {
			if (current instanceof IQ7Project)
				return (IQ7Project) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	public IQ7Element[] getChildren() throws ModelException {
		return getChildren(null);
	}

	public IQ7Element[] getChildren(IProgressMonitor monitor)
			throws ModelException {
		Object elementInfo = getElementInfo(monitor);
		if (elementInfo instanceof Q7ElementInfo) {
			return ((Q7ElementInfo) elementInfo).getChildren();
		} else {
			return NO_ELEMENTS;
		}
	}

	/**
	 * @see IModelElement
	 */
	public IQ7Model getModel() {
		IQ7Element current = this;
		do {
			if (current instanceof IQ7Model)
				return (IQ7Model) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	protected List<IQ7Element> getChildrenOfType(HandleType type)
			throws ModelException {
		return getChildrenOfType(type, null);
	}

	protected List<IQ7Element> getChildrenOfType(HandleType type,
			IProgressMonitor monitor) throws ModelException {
		IQ7Element[] children = getChildren(monitor);
		int size = children.length;
		List<IQ7Element> list = new ArrayList<IQ7Element>(size);
		for (int i = 0; i < size; ++i) {
			IQ7Element elt = children[i];
			if (elt.getElementType().equals(type)) {
				list.add(elt);
			}
		}
		return list;
	}

	public boolean hasChildren() throws ModelException {
		Object elementInfo = ModelManager.getModelManager().getInfo(this);
		if (elementInfo instanceof Q7ElementInfo) {
			return ((Q7ElementInfo) elementInfo).getChildren().length > 0;
		} else {
			return true;
		}
	}

	public void accept(IQ7ElementVisitor visitor) throws ModelException {
		if (visitor.visit(this)) {
			IQ7Element[] elements = getChildren();
			for (int i = 0; i < elements.length; ++i) {
				elements[i].accept(visitor);
			}
			visitor.endVisit(this);
		}
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		// model parent is null
		if (this.parent == null) {
			return super.equals(o);
		}
		if (o == null) {
			return false;
		}
		// assume instanceof check is done in subclass
		final Q7Element other = (Q7Element) o;
		return getName().equals(other.getName())
				&& this.parent.equals(other.parent);
	}

	public int hashCode() {
		if (this.parent == null)
			return super.hashCode();
		return Util.combineHashCodes(getName().hashCode(),
				this.parent.hashCode());
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		toString(0, buffer);
		return buffer.toString();
	}

	protected void toString(int tab, StringBuffer buffer) {
		Object info = this.toStringInfo(tab, buffer);
		if (tab == 0) {
			this.toStringAncestors(buffer);
		}
		this.toStringChildren(tab, buffer, info);
	}

	public Object toStringInfo(int tab, StringBuffer buffer) {
		Object info = ModelManager.getModelManager().peekAtInfo(this);
		this.toStringInfo(tab, buffer, info, true/* show resolved info */);
		return info;
	}

	protected void toStringInfo(int tab, StringBuffer buffer, Object info,
			boolean showResolvedInfo) {
		buffer.append(this.tabString(tab));
		toStringName(buffer);
		if (info == null) {
			buffer.append(" (not open)"); //$NON-NLS-1$
		}
	}

	protected void toStringAncestors(StringBuffer buffer) {
		Q7Element parentElement = (Q7Element) this.getParent();
		if (parentElement != null && parentElement.getParent() != null) {
			buffer.append(" [in "); //$NON-NLS-1$
			parentElement.toStringInfo(0, buffer, NO_INFO, false); // don't show
			// resolved
			// info
			parentElement.toStringAncestors(buffer);
			buffer.append("]"); //$NON-NLS-1$
		}
	}

	protected void toStringName(StringBuffer buffer) {
		buffer.append(getName());
	}

	protected String tabString(int tab) {
		StringBuffer buffer = new StringBuffer();
		for (int i = tab; i > 0; i--)
			buffer.append("  "); //$NON-NLS-1$
		return buffer.toString();
	}

	protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
		if (info == null || !(info instanceof Q7ElementInfo))
			return;
		IQ7Element[] children = ((Q7ElementInfo) info).getChildren();
		for (int i = 0; i < children.length; i++) {
			buffer.append("\n"); //$NON-NLS-1$
			((Q7Element) children[i]).toString(tab + 1, buffer);
		}
	}

	public String toDebugString() {
		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, NO_INFO, true/* show resolved info */);
		return buffer.toString();
	}

	public String toStringWithAncestors() {
		return toStringWithAncestors(true/* show resolved info */);
	}

	public String toStringWithAncestors(boolean showResolvedInfo) {
		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, NO_INFO, showResolvedInfo);
		this.toStringAncestors(buffer);
		return buffer.toString();
	}
}
