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

package org.eclipse.rcptt.internal.launching.ext;

import org.junit.Assert;
import org.junit.Test;

public class UpdateVMArgsTest {

	@Test
	public void keepImportantQuotes() {
		String quotedArg = "\"-XX:CompileCommand=exclude org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteAnalyzer::getExtendedRange\"";
		String input = "-Dblah " + quotedArg;
		String actual = UpdateVMArgs.updateAttr(input);
		Assert.assertTrue(actual, actual.contains(quotedArg));
	}

}
