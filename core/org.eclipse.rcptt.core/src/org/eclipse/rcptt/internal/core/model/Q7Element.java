/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.core.model;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.rcptt.core.model.IOpenable;
import org.eclipse.rcptt.core.model.IQ7Element;
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

	protected abstract Q7ElementInfo createElementInfo();

	protected boolean isInWorkingCopyMode() {
		return false;
	}

	public IOpenable getOpenable() {
		return this.getOpenableParent();
	}

	public IOpenable getOpenableParent() {
		return (IOpenable) this.parent;
	}

	public void close() throws ModelException, InterruptedException {
		ModelManager.getModelManager().removeInfoAndChildren(this);
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
	
	public final <V> V accessInfo(Function<Q7ElementInfo, V> infoTovalue) throws InterruptedException {
		return ModelManager.getModelManager().accessInfo(this, infoTovalue);
	}

	public final <V> Optional<V> peekInfo(Function<Q7ElementInfo, V> infoTovalue) throws InterruptedException {
		return ModelManager.getModelManager().peekInfo(this, infoTovalue);
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
		try {
			if (!peekInfo(info -> {
				this.toStringInfo(tab, buffer, info);
				return true;
			}).isPresent()) {
				this.toStringInfo(tab, buffer, null);
			}
		} catch (InterruptedException e) {
			OperationCanceledException result = new OperationCanceledException();
			result.initCause(e);
			throw result;
		}
	}

	protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
		buffer.append(this.tabString(tab));
		toStringName(buffer);
		if (info == null) {
			buffer.append(" (not open)"); //$NON-NLS-1$
		}
		this.toStringChildren(tab, buffer, info);

	}

	protected void toStringAncestors(StringBuffer buffer) {
		Q7Element parentElement = (Q7Element) this.getParent();
		if (parentElement != null && parentElement.getParent() != null) {
			buffer.append(" [in "); //$NON-NLS-1$
			parentElement.toStringInfo(0, buffer, NO_INFO);
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
		this.toStringInfo(0, buffer, NO_INFO);
		return buffer.toString();
	}

	public String toStringWithAncestors() {
		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, NO_INFO);
		this.toStringAncestors(buffer);
		return buffer.toString();
	}
}
