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
package org.eclipse.rcptt.ecl.parser.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.ecl.parser.EclCoreParser;
import org.eclipse.rcptt.ecl.parser.ScriptErrorStatus;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;

import com.google.common.io.CharStreams;

public class TestSession {
	private final ISession session;

	public TestSession() {
		this(EclRuntime.createSession());
	}

	public TestSession(ISession session) {
		this.session = session;
	}

	public static IStatus unwrap(IStatus status) {
		if (status instanceof ScriptErrorStatus) {
			return unwrap(((ScriptErrorStatus) status).getCause());
		}
		return status;
	}

	public Object runScript(String script) throws CoreException {
		try {
			IPipe out = session.createPipe();
			IProcess process = session.execute(EclCoreParser.newCommand(script), null, out);
			IStatus status = process.waitFor();
			if (!status.isOK())
				throw new CoreException(unwrap(status));
			return out.take(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(InputStream is) throws IOException {
		try {
			return CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8));
		} finally {
			is.close();
		}
	}

	void close() {
		try {
			session.close();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
}
