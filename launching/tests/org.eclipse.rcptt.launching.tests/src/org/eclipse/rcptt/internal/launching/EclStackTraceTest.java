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
package org.eclipse.rcptt.internal.launching;

import org.eclipse.rcptt.ecl.parser.ScriptErrorStatus;
import org.junit.Assert;
import org.junit.Test;


public class EclStackTraceTest {

	@Test
	public void ensureMessage() {
		ExecutionStatus status = new ExecutionStatus(new ScriptErrorStatus("plugin", "message1", "1.test", 1, 1, 1));
		EclStackTrace subject = EclStackTrace.fromExecStatus(status);
		Assert.assertEquals("message1\n", subject.print());
	}

}
