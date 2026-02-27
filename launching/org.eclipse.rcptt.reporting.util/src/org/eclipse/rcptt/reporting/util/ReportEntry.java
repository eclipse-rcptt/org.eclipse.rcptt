/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.reporting.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.core.SimpleSeverity;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

/**
 * One-line representation of report to show in lists
 */
public final class ReportEntry {
	public final String name;
	public final String id;
	private final int status;
	public int time;
	public final String message;
	private final SimpleSeverity severity;
	private Instant start, end;

	private ReportEntry(String name, String id, int status, String message, SimpleSeverity severity, Instant start, Instant end) {
		super();
		checkNotNull(name);
		checkNotNull(id);
		checkNotNull(status);
		checkNotNull(message);
		this.message = message;
		this.name = name;
		this.id = id;
		this.status = status;
		this.severity = checkNotNull(severity);
		this.start = checkNotNull(start);
		this.end = checkNotNull(end);
		this.time = Math.toIntExact(Duration.between(start, end).toMillis());
	}

	public String getMessage() {
		return message;
	}
	
	public SimpleSeverity getSimpleSeverity() {
		return SimpleSeverity.create(status);
	}
	
	/** IStatus constants bitwise mix */
	public int getStatusSeverity() {
		return status;
	}
	
	public static ReportEntry create(Report next) {
		Node root = next.getRoot();
		if (root == null) {
			return new ReportEntry("Broken report", "Broken report", 0, "A report is missing RCPTT required metadata. This is likely coused by early termination of test runner.", SimpleSeverity.ERROR, Instant.EPOCH, Instant.EPOCH); 
		}
		Q7Info info = ReportHelper.getInfo(root);
		StringWriter writer = new StringWriter();
		RcpttReportGenerator.writeResult(new PrintWriter(writer), 0, info.getResult());
		ReportEntry entry = new ReportEntry(root.getName(), info.getId(), info.getResult().getSeverity(),
				writer.toString(), SimpleSeverity.create(info), Instant.ofEpochMilli(root.getStartTime()), Instant.ofEpochMilli(root.getEndTime()));
		return entry;
	}

	public SimpleSeverity severity() {
		return severity;
	}

	public Instant getStart() {
		return start;
	}
	
	public Instant getEnd() {
		return end;
	}


}