/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.swt.reflection;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.rcptt.tesla.core.ui.PropertyNode;
import org.junit.Test;

public class JavaMembersHelperTest {

	@Test
	public void collectProperties() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Set<Integer> protectedObject = Set.of(4);
		EList<PropertyNode> nodes = new BasicEList<PropertyNode>();
		JavaMembersHelper.fillProperties(protectedObject, nodes); // should not throw
		// Should use size() method from a non-private super class
		assertEquals(List.of("1"), nodes.stream().filter(n -> n.getName().equals("size()")).map(PropertyNode::getValue).toList() );
		nodes.clear();
		JavaMembersHelper.fillProperties(protectedObject, "size()", nodes); // should not throw
		
		assertEquals("1", nodes.stream().filter(n -> n.getName().equals("toString()")).findFirst().get().getValue());
		assertEquals("1", JavaMembersHelper.getPropertyValue(protectedObject, "size()"));
	}

}
