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
package org.eclipse.rcptt.core.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

public interface IQ7Element extends IAdaptable {
	public enum HandleType {
		Model, Project, TestCase, Context, Folder, TestSuite, ProjectMetadata, Verification, NonQ7
	};

	String getName();

	HandleType getElementType();

	IResource getResource();

	boolean exists();

	IQ7Element getAncestor(HandleType type);

	IQ7Element getParent();

	IPath getPath();

	void accept(IQ7ElementVisitor visitor) throws ModelException, InterruptedException;

	IQ7Model getModel();

	IQ7Project getQ7Project();

	IOpenable getOpenable();
}
