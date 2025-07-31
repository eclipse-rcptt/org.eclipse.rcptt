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

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.scenario.NamedElement;

public interface IQ7NamedElement extends IQ7Element, IOpenable {
	String getID() throws ModelException;

	String getVersion() throws ModelException;

	String getTags() throws ModelException;

	String getDescription() throws ModelException;

	String getElementName() throws ModelException;

	IQ7NamedElement getWorkingCopy(IProgressMonitor monitor)
			throws ModelException;
	IQ7NamedElement getIndexingWorkingCopy(IProgressMonitor monitor)
			throws ModelException;
	void commitWorkingCopy(boolean force, IProgressMonitor monitor)
			throws ModelException;

	void discardWorkingCopy() throws ModelException;

	IPersistenceModel getPersistenceModel() throws ModelException;

	boolean hasResourceChanged() throws ModelException;

	boolean hasUnsavedChanges() throws ModelException;

	// Modifications via
	// Only valid for working copies
	NamedElement getNamedElement() throws ModelException;

	NamedElement getModifiedNamedElement() throws ModelException;

	IPersistenceModel getModifiedPersistenceModel() throws ModelException;

	void setElementName(String name) throws ModelException;

	void setDescription(String description) throws ModelException;

	void setVersion(String version) throws ModelException;

	void setID(String id) throws ModelException;

	void setTags(String tags) throws ModelException;

	boolean isWorkingCopy();
}
