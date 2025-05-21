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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioProperty;

public class Q7TestCase extends Q7NamedElement implements ITestCase {

	protected Q7TestCase(Q7Element parent, String name) throws IllegalArgumentException {
		super(parent, name);
	}

	public HandleType getElementType() {
		return HandleType.TestCase;
	}

	protected Scenario getScenario() throws ModelException {
		return   (Scenario) getNamedElement();
	}

	public EObject getContent() throws ModelException {
		return getScenario().getContent();
	}

	public String[] getContexts() throws ModelException {
		EList<String> contexts = getScenario().getContexts();
		return contexts.toArray(new String[contexts.size()]);
	}

	public String[] getVerifications() throws ModelException {
		EList<String> verifications = getScenario().getVerifications();
		return verifications.toArray(new String[verifications.size()]);
	}

	public EObject getTesla() throws ModelException {
		return getScenario().getTeslaContent();
	}

	public String getExternalReference() throws ModelException {
		return getScenario().getExternalReference();
	}

	public Map<String, String> getProperties() throws ModelException {
		Map<String, String> result = new HashMap<String, String>();
		for (ScenarioProperty p : getScenario().getProperties()) {
			result.put(p.getName(), p.getValue());
		}
		return result;
	}

	public String getType() throws ModelException {
		return getScenario().getType();
	}

	@Override
	protected Q7NamedElement createWorkingCopy() {
		return new Q7TestCase(parent, name);
	}

	@Override
	protected NamedElement createNamedElement() {
		return ((Q7Folder) getParent()).createScenario(new Path(name).removeFileExtension().toString());
	}

	@Override
	public Scenario getNamedElement() throws ModelException {
		return (Scenario) super.getNamedElement();
	}

	// Modifications
	public void setContent(EObject content) throws ModelException {
		if (isWorkingCopy()) {
			getNamedElement().setContent(content);
		}
	}

	public void setContexts(String[] contexts) throws ModelException {
		if (isWorkingCopy()) {
			EList<String> ctxs = getNamedElement().getContexts();
			ctxs.clear();
			ctxs.addAll(Arrays.asList(contexts));
		}
	}

	public void setVerifications(String[] verificationsToSet) throws ModelException {
		if (isWorkingCopy()) {
			EList<String> verifications = getNamedElement().getVerifications();
			verifications.clear();
			verifications.addAll(Arrays.asList(verificationsToSet));
		}
	}

	public void setTesla(EObject tesla) throws ModelException {
		if (isWorkingCopy()) {
			getNamedElement().setTeslaContent(tesla);
		}
	}

	public void setType(String type) throws ModelException {
		if (isWorkingCopy()) {
			getNamedElement().setType(type);
		}
	}

	public void setExternalReference(String ref) throws ModelException {
		if (isWorkingCopy()) {
			getNamedElement().setExternalReference(ref);
		}
	}
}
