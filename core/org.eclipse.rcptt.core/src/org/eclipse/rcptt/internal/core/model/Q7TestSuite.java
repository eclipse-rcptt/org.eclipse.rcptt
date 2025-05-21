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

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;

import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.TestSuite;
import org.eclipse.rcptt.core.scenario.TestSuiteItem;

public class Q7TestSuite extends Q7NamedElement implements ITestSuite {

	public Q7TestSuite(Q7Element parent, String name)
			throws IllegalArgumentException {
		super(parent, name);
	}

	public HandleType getElementType() {
		return HandleType.TestSuite;
	}

	public TestSuite getTestSuite() throws ModelException {
		return (TestSuite) getNamedElement();
	}

	public TestSuiteItem[] getItems() throws ModelException {
		EList<TestSuiteItem> items = getTestSuite().getItems();
		return items.toArray(new TestSuiteItem[items.size()]);
	}

	@Override
	protected NamedElement createNamedElement() {
		return ((Q7Folder) getParent()).createTestSuite(new Path(name)
				.removeFileExtension().toString());
	}

	@Override
	protected Q7NamedElement createWorkingCopy() {
		return new Q7TestSuite(parent, name);
	}

	// Modification
	public void setItems(TestSuiteItem[] items) throws ModelException {
		if (isWorkingCopy()) {
			EList<TestSuiteItem> existentItems = getTestSuite().getItems();
			existentItems.clear();
			existentItems.addAll(Arrays.asList(items));
		}
	}
}
