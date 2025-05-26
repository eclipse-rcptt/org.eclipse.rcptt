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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status.Q7StatusCode;

public class Q7FolderInfo extends OpenableElementInfo {
	private Object[] foreignResources;

	public void setForeignResources(Object[] resources) {
		foreignResources = resources;
	}

	public Object[] getForeignResources(IResource resource) throws ModelException {
		if (this.foreignResources == null) {
			try {
				this.foreignResources = Q7ProjectInfo
						.computeFolderForeignResources((IContainer) resource);
			} catch (ModelException e) {
				if (e.getQ7Status().getStatusCode() != Q7StatusCode.NotPressent) {
					throw e;
				}
				// root doesn't exist: consider package has no
				this.foreignResources = NO_NON_Q7_RESOURCES;
			}
		}
		return this.foreignResources;
	}

	public boolean containsQ7Resources() {
		return size() != 0;
	}
}
