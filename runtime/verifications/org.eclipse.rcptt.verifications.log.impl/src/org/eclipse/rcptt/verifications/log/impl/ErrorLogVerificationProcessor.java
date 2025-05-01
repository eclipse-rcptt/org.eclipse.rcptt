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
package org.eclipse.rcptt.verifications.log.impl;

import static java.lang.String.format;
import static org.eclipse.rcptt.verifications.log.tools.ErrorLogUtil.createMatchingPredicate;
import static org.eclipse.rcptt.verifications.log.tools.ErrorLogUtil.describe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.core.VerificationProcessor;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.reporting.ItemKind;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.core.IQ7ReportConstants;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.sherlock.core.INodeBuilder;
import org.eclipse.rcptt.verifications.log.ErrorLogVerification;
import org.eclipse.rcptt.verifications.log.LogEntryPredicate;
import org.eclipse.rcptt.verifications.log.LogFactory;
import org.eclipse.rcptt.verifications.log.tools.ErrorLogUtil;
import org.eclipse.rcptt.verifications.runtime.ErrorList;

public class ErrorLogVerificationProcessor extends VerificationProcessor implements ILogListener {

	/**
	 * An entry in the error log, containing the error's {@link IStatus} and the {@link INodeBuilder} at the time of the
	 * error.
	 */
	private static class LogEntry {
		final IStatus status;
		private boolean isContext;

		LogEntry(IStatus status, boolean isContext) {
			this.isContext = isContext;
			if( status == null) {
				throw new NullPointerException("Status should not be null"); //$NON-NLS-1$
			}
			this.status = status;
			
		}
	}

	private final List<LogEntry> testLog = new ArrayList<LogEntry>();

	public ErrorLogVerificationProcessor() {
		Platform.addLogListener(this);
	}
	
	@Override
	synchronized public Object start(Verification verification, IProcess process) {
		testLog.clear();
		return null;
	}

	@Override
	synchronized public Object run(Verification verification, Object data, IProcess process) {
		return data;
	}

	@Override
	public void finish(Verification verification, Object data, IProcess process) throws CoreException {
		ErrorList errors = findErrors((ErrorLogVerification) verification);
		errors.throwIfAny(String.format("Error log verification '%s' failed:", verification.getName()), this.getClass()
				.getPackage().getName(), verification.getId());
	}

	private ErrorList findErrors(ErrorLogVerification logVerification) {
		List<LogEntryPredicate> whiteList = new ArrayList<>();
		whiteList.addAll(logVerification.getAllowed());
		whiteList.addAll(logVerification.getRequired());
		ErrorList errors = new ErrorList();
		for (LogEntry entry : testLog) {
			boolean ignoreContext = !logVerification.isIncludeContexts() && entry.isContext;
			if (ignoreContext || isWhiteListed(whiteList, entry.status)) {
				continue;
			}
			LogEntryPredicate denied = ErrorLogUtil.find(logVerification.getDenied(), entry.status);
			if (denied != null) {
				errors.add(
						"Log entry\n%s\nis denied by predicate\n%s",
						describe(entry.status),
						describe(denied));
			}
		}

		for (LogEntryPredicate predicate: logVerification.getRequired()) {
			if (!contains(testLog, predicate)) {
				errors.add("Required \n%s\nnot found", describe(predicate));
			}
		}
		return errors;
	}

	private static boolean contains(Collection<LogEntry> entries, LogEntryPredicate predicate) {
		for (LogEntry entry : entries) {
			if (ErrorLogUtil.match(predicate, entry.status)) {
				return true;
			}
		}
		return false;
	}

	@Override
	synchronized public void logging(IStatus status, String plugin) {
		INodeBuilder node = ReportManager.getCurrentReportNode();
		testLog.add(new LogEntry(status, isContext(node)));
	}
	
	private boolean isWhiteListed(Iterable<LogEntryPredicate> whiteList, IStatus status) {
		LogEntryPredicate rv = ErrorLogUtil.find(whiteList, status);
		if (rv != null) {
			ErrorLogVerification verification = (ErrorLogVerification) rv.eContainer();
			ReportManager.appendLog(format("Log entry %s is allowed by verification %s with %s",
					describe(status),
					verification.getName(),
					describe(rv)
					));
		}
		return rv != null;
	}

	@Override
	public Verification create(EObject param, IProcess process) throws CoreException {
		ErrorLogVerification rv = LogFactory.eINSTANCE.createErrorLogVerification();
		if (param instanceof ErrorLogVerification) {
			rv.setIncludeContexts(((ErrorLogVerification) param).isIncludeContexts());
		}
		for (LogEntry entry : testLog) {
			if (!rv.isIncludeContexts() && entry.isContext) {
				continue; // Error happened during context execution and the verification is configured to ignore them.
			}
			rv.getAllowed().add(createMatchingPredicate(entry.status));
		}
		return rv;
	}

	private static boolean isContext(INodeBuilder node) {
		while (node != null) {
			Q7Info info = (Q7Info) node.getProperty(IQ7ReportConstants.ROOT);
			if (info != null && ItemKind.CONTEXT.equals(info.getType())) {
				return true;
			}
			node = node.getParent();
		}
		return false;
	}
	
}
