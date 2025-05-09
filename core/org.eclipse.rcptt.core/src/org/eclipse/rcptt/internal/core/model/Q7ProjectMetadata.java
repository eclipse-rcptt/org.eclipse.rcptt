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

import org.eclipse.emf.common.util.EList;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IQ7ProjectMetadata;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.ProjectMetadata;

public class Q7ProjectMetadata extends Q7NamedElement implements
		IQ7ProjectMetadata {

	public Q7ProjectMetadata(Q7Element parent) throws IllegalArgumentException {
		super(parent, IQ7Project.METADATA_NAME);
	}

	public HandleType getElementType() {
		return HandleType.ProjectMetadata;
	}

	@Override
	protected NamedElement createNamedElement() {
		return ((Q7Folder) getParent()).createMetadata();
	}

	public String[] getContexts() throws ModelException {
		EList<String> contexts = ((ProjectMetadata) getNamedElement())
				.getContexts();
		return contexts.toArray(new String[contexts.size()]);
	}

	public String[] getVerifications() throws ModelException {
		EList<String> verifications = ((ProjectMetadata) getNamedElement())
				.getVerifications();
		return verifications.toArray(new String[verifications.size()]);
	}

	@Override
	protected Q7ResourceInfo createElementInfo() {
		return new Q7ResourceInfo(IPlainConstants.PLAIN_METADATA, Q7ResourceInfo.toURI(getResource()));
	}

	@Override
	protected Q7NamedElement createWorkingCopy() {
		return new Q7ProjectMetadata(parent);
	}
}
