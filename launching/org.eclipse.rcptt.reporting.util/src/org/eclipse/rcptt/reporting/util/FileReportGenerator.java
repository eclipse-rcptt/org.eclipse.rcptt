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

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.reporting.Q7Statistics;
import org.eclipse.rcptt.reporting.core.IReportRenderer;
import org.eclipse.rcptt.reporting.util.internal.Plugin;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

public class FileReportGenerator implements IReportRenderer {

	public FileReportGenerator() {
	}

	
	@Override
	public IStatus generateReport(IContentFactory factory, String reportName,
			Iterable<Report> report) {

		Q7Statistics statistics = ReportUtils.calculateStatisticsSlow(report.iterator());
		
		try (OutputStream stream = factory.createFileStream(reportName + ".xml")) {
			new XMLReportGenerator().generateContent(stream, reportName,
					report, statistics);
		} catch (CoreException cex) {
			return cex.getStatus();
		} catch (IOException e) {
			return Plugin.UTILS.createError(e);
		}
		return Status.OK_STATUS;
	}

	
	@Override
	public String[] getGeneratedFileNames(String reportName) {
		return new String[] { reportName + ".xml" };
	}
}
