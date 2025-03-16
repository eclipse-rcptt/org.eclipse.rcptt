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
package org.eclipse.rcptt.ui.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.launching.ContextExecutable;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.ui.utils.Executables;

public class ExecutionContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IExecutable) {
			IExecutable exec = (IExecutable) parentElement;
			List<IExecutable> childs = new ArrayList<IExecutable>();
			childs.addAll(Arrays.asList(exec.getChildren()));
			// Filter out default context
			if (!childs.isEmpty()) {
				IExecutable child = childs.get(0);
				if (child instanceof ContextExecutable) {
					String id = child.getId();
					if (RcpttCore.DEFAULT_WORKBENCH_CONTEXT_ID.equals(id)) {
						childs.remove(0);
					}
				}
			}
			return getExistingExecutables(childs.toArray(new IExecutable[childs
					.size()]));
		}
		return new Object[] {};
	}

	public Object getParent(Object element) {
		if (executables == null) {
			return null;
		}
		return executables.getParent((IExecutable) element);
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		IExecutable[] roots = ((Executables) inputElement).getRoots();
		return getExistingExecutables(roots);
	}

	private Object[] getExistingExecutables(IExecutable[] roots) {
		List<IExecutable> list = new ArrayList<IExecutable>();
		for (IExecutable e : roots) {
			IQ7NamedElement actualElement = e.getActualElement();
			if (actualElement != null && actualElement.getResource() != null) {
				list.add(e);
			}
		}
		return list.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		executables = (Executables) newInput;
	}

	private Executables executables;
}
