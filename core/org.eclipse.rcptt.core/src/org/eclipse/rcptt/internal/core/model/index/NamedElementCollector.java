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
package org.eclipse.rcptt.internal.core.model.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7ElementVisitor;
import org.eclipse.rcptt.core.model.IQ7NamedElement;

public class NamedElementCollector implements IQ7ElementVisitor {
	private final List<IQ7NamedElement> elements = new ArrayList<IQ7NamedElement>();
	private final Set<HandleType> filter;

	public NamedElementCollector() {
		filter = new HashSet<HandleType>(Arrays.asList(HandleType.Context,
				HandleType.TestCase, HandleType.TestSuite, HandleType.Verification, HandleType.ProjectMetadata));
	}

	public NamedElementCollector(HandleType... types) {
		filter = new HashSet<HandleType>(Arrays.asList(types));
	}

	@Override
	public boolean visit(IQ7Element element) {
		HandleType elementType = element.getElementType();
		if (filter.contains(elementType)) {
			elements.add((IQ7NamedElement) element);
			return false;
		}
		return true;
	}

	@Override
	public boolean endVisit(IQ7Element element) {
		return false;
	}

	public List<IQ7NamedElement> getElements() {
		return elements;
	}
}