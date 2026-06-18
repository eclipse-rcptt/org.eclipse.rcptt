/*******************************************************************************
 * Copyright (c) 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.platform.tests;

import org.junit.Rule;
import org.junit.Test;

public class SortByTest {
	
	@Rule
	public final SessionRule session = new SessionRule();

	@Test
	public void testStringValueField() {
		session.assertScriptOk("""
			emit beta alpha gamma | sort-by -field value | to-list | eq [list alpha beta gamma] | assert-true
		""");
	}

	@Test
	public void testEclString() {
		session.assertScriptOk("""
			emit beta alpha gamma | sort-by | to-list | eq [list alpha beta gamma] | assert-true
		""");
	}

}
