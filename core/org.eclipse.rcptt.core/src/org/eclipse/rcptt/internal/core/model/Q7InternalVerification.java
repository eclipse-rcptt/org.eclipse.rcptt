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

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.VerificationTypeManager;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Verification;

public class Q7InternalVerification extends Q7Verification {

	private final Verification verification;

	public Q7InternalVerification(Q7Element parent, String name, Verification verification)
			throws IllegalArgumentException {
		super(parent, name);
		this.verification = verification;
	}

	@Override
	public IPath getPath() {
		return new Path("");
	}

	@Override
	public IQ7Project getQ7Project() {
		return ModelManager.getModelManager().getModel()
				.getProject("__internal__project___");
	}

	@Override
	public IFile getResource() {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public NamedElement getNamedElement() throws ModelException {
		return verification;
	}

	@Override
	public NamedElement getModifiedNamedElement() throws ModelException {
		return verification;
	}

	@Override
	public String getElementName() throws ModelException {
		return verification.getName();
	}

	@Override
	public String getDescription() throws ModelException {
		return verification.getDescription();
	}

	@Override
	public String getVersion() throws ModelException {
		return verification.getVersion();
	}

	@Override
	public NamedElement getMeta() throws ModelException {
		return verification;
	}

	@Override
	public String getTags() throws ModelException {
		return verification.getTags();
	}

	@Override
	public String getID() throws ModelException {
		return verification.getId();
	}

	@Override
	public VerificationType getType() throws ModelException {
		return VerificationTypeManager.getInstance().getTypeByVerification(verification);
	}

	@Override
	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm,
			IResource underlyingResource) throws ModelException {
		return true;
	}
}
