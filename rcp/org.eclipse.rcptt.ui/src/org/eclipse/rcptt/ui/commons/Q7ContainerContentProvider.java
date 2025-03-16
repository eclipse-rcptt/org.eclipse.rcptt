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
package org.eclipse.rcptt.ui.commons;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.ide.misc.ContainerContentProvider;

import org.eclipse.rcptt.core.nature.RcpttNature;

@SuppressWarnings("restriction")
public class Q7ContainerContentProvider extends ContainerContentProvider {

	@Override
	public Object[] getChildren(Object element) {
		Object[] children = super.getChildren(element);
		List<Object> q7Children = new LinkedList<Object>();

		for (Object child : children) {
			if (child instanceof IProject) {
				try {
					if (!RcpttNature.isRcpttProject((IProject) child)) {
						continue;
					}
				} catch (CoreException e) {
					continue;
				}
			}
			if (child instanceof IFolder) {
				if (".settings".equals(((IFolder) child).getName())) { 
					continue;
				}
			}
			q7Children.add(child);
		}

		return q7Children.toArray();
	}

}
