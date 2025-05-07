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
package org.eclipse.rcptt.internal.core;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.scenario.ILazyEMFResolver;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;

public class Q7LazyResource extends XMIResourceImpl implements ILazyEMFResolver {
	public Q7LazyResource(URI uri) {
		super(uri);
	}

	public EObject resolveReference(EReference ref, EObject self) {
		if (ScenarioPackage.Literals.SCENARIO__CONTENT.equals(ref)) {
			return PersistenceManager.getInstance().loadECLContent(this, self);
			
		} else if (ScenarioPackage.Literals.SCENARIO__TESLA_CONTENT.equals(ref)) {
			return PersistenceManager.getInstance().loadTeslaContent(this, self);
		}
		return null;
	}

	public String resolveStringValue(EAttribute ref, EObject self) {
		if( ScenarioPackage.Literals.NAMED_ELEMENT__DESCRIPTION.equals(ref)) {
			return PersistenceManager.getInstance().loadDescription(this, self);
		}
		return null;
	}
}
