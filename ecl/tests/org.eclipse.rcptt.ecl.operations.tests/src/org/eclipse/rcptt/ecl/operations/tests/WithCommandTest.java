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
package org.eclipse.rcptt.ecl.operations.tests;

import org.eclipse.core.runtime.CoreException;
import org.junit.Rule;
import org.junit.Test;

public class WithCommandTest {

	@Rule
	public final SessionRule session = new SessionRule();
	
	@Test
	public void withShouldNotModifyInput() throws CoreException {
		session.assertScriptOk("""
			with [list zero one two three] {
				get 0 | eq zero | assert-true
				get 1 | eq one | assert-true
				get 2 | eq two | assert-true
				get 3 | eq three | assert-true
				with [get 2] {
					eq two | assert-true
				}
				// and now the list lacks the element "two":
				get 0 | eq zero | assert-true
				get 1 | eq one | assert-true
				get 2 | eq two | assert-true
				get 3 | eq three | assert-true
			}
		""");
	}

}
