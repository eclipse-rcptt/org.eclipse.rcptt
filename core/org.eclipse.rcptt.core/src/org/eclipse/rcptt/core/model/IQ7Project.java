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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public interface IQ7Project extends IQ7Element, IParent {
	final String METADATA_NAME = "rcptt.properties";
	final String LEGACY_METADATA_NAME = "q7.properties";

	<T> List<T> find(Class<T> class_, String id) throws ModelException;

	IProject getProject();

	IQ7Folder getFolder(IPath path);

	Object[] getForeignResources() throws ModelException, InterruptedException;

	public IQ7Project[] getReferences();

	IQ7NamedElement[] findNamedElement(String id) throws ModelException;

	IQ7Folder[] getFolders() throws ModelException, InterruptedException;

	IQ7Folder getRootFolder();

	IQ7Folder createFolder(Path path);

	IQ7ProjectMetadata getMetadata() throws ModelException;
}
