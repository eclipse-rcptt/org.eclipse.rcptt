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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;

public class Q7InternalTestCase extends Q7TestCase {

	private final Scenario scenario;

	public Q7InternalTestCase(Q7Element parent, String name, Scenario scenario)
			throws IllegalArgumentException {
		super(parent, name);
		this.scenario = scenario;
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
	protected Scenario getScenario() {
		return scenario;
	}

	@Override
	public Scenario getNamedElement() {
		return getScenario();
	}

	@Override
	public NamedElement getModifiedNamedElement() {
		return getScenario();
	}

	@Override
	public String getElementName() throws ModelException {
		return scenario.getName();
	}

	@Override
	public String getDescription() throws ModelException {
		return scenario.getDescription();
	}

	@Override
	public String getVersion() throws ModelException {
		return scenario.getVersion();
	}

	@Override
	public NamedElement getMeta() throws ModelException {
		return scenario;
	}

	@Override
	public String getTags() throws ModelException {
		return scenario.getTags();
	}

	@Override
	public String getID() throws ModelException {
		return scenario.getId();
	}

	@Override
	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm,
			IResource underlyingResource) throws ModelException {
		return true;
	}
}
