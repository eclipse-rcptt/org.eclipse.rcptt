/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.debug.core;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;

public class NullDebuggerTransport implements DebuggerTransport {

	public void request(EObject event) {
	}

	public void setCallback(DebuggerCallback callback) {
	}

	@Override
	public void close() throws IOException {
	}

}
