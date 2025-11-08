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
package org.eclipse.rcptt.ctx.script.impl;

import java.util.function.BooleanSupplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.core.IContextProcessor;
import org.eclipse.rcptt.core.IEclAwareProcessor;
import org.eclipse.rcptt.core.ecl.context.EclContext;
import org.eclipse.rcptt.core.ecl.internal.context.EclContextPlugin;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.ecl.parser.ScriptErrorStatus;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;

public class EclContextProcessor implements IContextProcessor, IEclAwareProcessor {

	private boolean applied = false;

	@Override
	public Context create(EObject param) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void apply(Context context, final ISession session) throws CoreException {
		final EclContext eclContext = (EclContext) context;
		final Exception[] resultE = new Exception[] { null };
		final Boolean[] finished = new Boolean[] { false };
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					session.execute(TeslaFactory.eINSTANCE.createSetupPlayer()).waitFor();
					// eclipse 3.4 compatibility:
					// EcoreUtil.copy raise exception if argument is null
					Script eclScript = (eclContext.getScript() != null) ? (Script) EcoreUtil
							.copy(eclContext.getScript()) : null;
					IStatus status = session.execute(eclScript).waitFor();
					if (!status.isOK() && status instanceof ScriptErrorStatus) {
						ScriptErrorStatus st = (ScriptErrorStatus) status;
						String msg = st.getCause().getMessage()
								+ "\nWhile executing context "
								+ eclContext.getName() + " at line:"
								+ st.getLine();

						ScriptErrorStatus sse = new ScriptErrorStatus(st.getPlugin(), msg,
								st.getResource(), st.getLine(), st.getColumn(), st.getLength());
						sse.add(st.getCause());
						resultE[0] = new CoreException(sse);
					} else if (!status.isOK()) {
						resultE[0] = new CoreException(status);
					}
				} catch (Exception e) {
					resultE[0] = new CoreException(new Status(IStatus.ERROR,
							EclContextPlugin.PLUGIN_ID,
							"Failed to launch ECL context", e));
				} finally {
					finished[0] = true;
					try {
						IProcess process = session
								.execute(TeslaFactory.eINSTANCE
										.createShoutdownPlayer());
						process.waitFor();
					} catch (Exception e) {
						resultE[0] = new CoreException(new Status(
								IStatus.ERROR, EclContextPlugin.PLUGIN_ID,
								"Failed to launch ECL context", e));
					}
				}
			}
		};
		Thread t = new Thread(r, "ECL Context apply");
		t.start();
		try {
			t.join(TeslaLimits.getContextRunnableTimeout());
		} catch (InterruptedException e) {
			RcpttPlugin.log(e);
		}
		if (!finished[0]) {
			throw new CoreException(new Status(IStatus.ERROR,
					EclContextPlugin.PLUGIN_ID,
					"Timeout exceeded during Ecl context execution..."));
		}
		if (resultE[0] != null) {
			if (!(resultE[0] instanceof CoreException)) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.PLUGIN_ID, resultE[0].getMessage(),
						resultE[0]));
			} else {
				throw (CoreException) resultE[0];
			}
		}
		applied = true;
	}

	@Override
	public void apply(Context context, BooleanSupplier isCancelled) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				"EclContextProcessor is ECL-aware and must be passed a session object", null));
	}

	@Override
	public boolean isApplied(Context context) {
		return applied;
	}

}
