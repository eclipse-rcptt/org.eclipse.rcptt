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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.util.Statuses;
import org.eclipse.rcptt.ecl.parser.EclCoreParser;
import org.eclipse.rcptt.ecl.parser.ScriptErrorStatus;
import org.eclipse.rcptt.ecl.runtime.EclRuntime;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.junit.rules.ExternalResource;

public final class SessionRule extends ExternalResource {
	private ISession session;
	
	
	@Override
	protected void before() throws Throwable {
		super.before();
		session = EclRuntime.createSession();
	}
	
	@Override
	protected void after() {
		try {
			session.close();
		} catch (CoreException e) {
			throw new AssertionError(e.getStatus().toString(), e);
		} finally {
			super.after();
		}
	}

	public static IStatus unwrap(IStatus status) {
		if (status instanceof ScriptErrorStatus) {
			return unwrap(((ScriptErrorStatus) status).getCause());
		}
		return status;
	}
	
	public static String toString(IStatus status) {
		return Statuses.format(status);
	}
	
	public void assertScriptOk(String script) {
		try {
			runScript(script);
		} catch (CoreException e) {
			throw new AssertionError(toString(e.getStatus()), e);
		}
	}
	
	public Object runScript(String script) throws CoreException {
		try {
			IPipe out = session.createPipe();
			IProcess process = session.execute(EclCoreParser.newCommand(script), null, out);
			IStatus status = process.waitFor();
			if (!status.isOK()) {
				if (status instanceof ScriptErrorStatus ses) {
					StringBuilder sb = new StringBuilder();
					sb.append("Failed on: ")
						.append(ses.getResource())
						.append(":").append(ses.getLine())
						.append(":").append(ses.getColumn())
						.append(script.split("\n")[ses.getLine() - 1]);
					ses.add(new Status(IStatus.INFO, getClass().getPackageName(), 0, sb.toString(), null));
				}
				throw new CoreException(status);
			}
			return out.take(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
