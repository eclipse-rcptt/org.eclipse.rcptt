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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rcptt.core.model.IOpenable;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementVisitor;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.model.Q7Status.Q7StatusCode;

import com.google.common.base.Throwables;

public abstract class Openable extends Q7Element implements IOpenable {

	protected Openable(Q7Element parent) throws IllegalArgumentException {
		super(parent);
	}
	
	@Override
	public boolean exists() {
		try {
			return openAndAccessInfo(info -> true, null);
		} catch (ModelException e) {
			if (e.getStatus() instanceof Q7Status)
				if (((Q7Status) e.getStatus()).getStatusCode() == Q7StatusCode.NotPressent)
					return false;
			throw new RuntimeException(e); 
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return true;
		}
	}
	
	public IQ7Element[] getChildren() throws ModelException, InterruptedException {
		return getChildren(null);
	}

	public IQ7Element[] getChildren(IProgressMonitor monitor)
			throws ModelException, InterruptedException {
		return openAndAccessInfo(info -> info.getChildren(), monitor);
	}

	protected List<IQ7Element> getChildrenOfType(HandleType type)
			throws ModelException, InterruptedException {
		return getChildrenOfType(type, null);
	}

	protected List<IQ7Element> getChildrenOfType(HandleType type,
			IProgressMonitor monitor) throws ModelException, InterruptedException {
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

	public boolean hasChildren() throws ModelException, InterruptedException {
		return getChildren().length > 0;
	}

	public void accept(IQ7ElementVisitor visitor) throws ModelException, InterruptedException {
		if (visitor.visit(this)) {
			IQ7Element[] elements = getChildren();
			for (int i = 0; i < elements.length; ++i) {
				elements[i].accept(visitor);
			}
			visitor.endVisit(this);
		}
	}


	protected abstract boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm,
			IResource underlyingResource) throws ModelException;

	protected boolean parentExists() {
		IQ7Element parentElement = getParent();
		if (parentElement == null)
			return true;
		return parentElement.exists();
	}

	protected void generateInfos(OpenableElementInfo info, IProgressMonitor monitor)
			throws ModelException {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();


		// build the structure of the openable (this will open the buffer if
		// needed)
		OpenableElementInfo openableElementInfo = (OpenableElementInfo) info;
		boolean isStructureKnown = buildStructure(openableElementInfo,
				monitor, getResource());
		openableElementInfo.setIsStructureKnown(isStructureKnown);
	}

	
	public final <V> Optional<V> accessInfoIfOpened(Function<OpenableElementInfo, V> infoToValue) throws InterruptedException {
		return this.<V>peekInfo(info -> {
			OpenableElementInfo openable = (OpenableElementInfo)info;
			if (!openable.isStructureKnown()) {
				return null;
			}
			return infoToValue.apply(openable);
		});
	}

	protected boolean resourceExists() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null)
			return false;
		return Q7Model.getTarget(workspace.getRoot(), this.getPath()
				.makeRelative(), true) != null;
	}

	@Override
	public IOpenable getOpenable() {
		return this;
	}

	private static final class PrivateException extends RuntimeException {
		private static final long serialVersionUID = -3766482554685794452L;
		public PrivateException(Exception checked) {
			super(checked);
		}
	}
	
	public interface ThrowingFunction<T, V> {
		V apply(T openable) throws ModelException;
	}
	
	public final <V> V openAndAccessInfo(ThrowingFunction<OpenableElementInfo, V> infoToValue, IProgressMonitor monitor) throws InterruptedException, ModelException {
		try {
			return accessInfo(info -> {
				OpenableElementInfo openable = (OpenableElementInfo)info;
				if (!openable.isStructureKnown()) {
					try {
						generateInfos(openable, monitor);
					} catch (ModelException e) {
						throw new PrivateException(e);
					}
				}
				try {
					return infoToValue.apply(openable);
				} catch (ModelException e) {
					throw new PrivateException(e);
				}
			});
		} catch (PrivateException e) {
			Throwables.throwIfInstanceOf(e.getCause(), ModelException.class);
			Throwables.throwIfInstanceOf(e.getCause(), InterruptedException.class);
			throw new AssertionError(e);
		}
	}
}
