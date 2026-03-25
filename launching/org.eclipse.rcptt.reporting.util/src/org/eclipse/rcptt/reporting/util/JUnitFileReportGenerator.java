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

import static org.eclipse.rcptt.reporting.util.internal.Plugin.UTILS;

import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.reporting.Q7Statistics;
import org.eclipse.rcptt.reporting.core.IReportRenderer;
import org.eclipse.rcptt.reporting.util.internal.XMLUtils;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

public class JUnitFileReportGenerator implements IReportRenderer {

	public JUnitFileReportGenerator() {
	}

	@Override
	public IStatus generateReport(IContentFactory factory, String reportName,
			Iterable<Report> report) {

		OutputStream stream = null;

		XMLStreamWriter writer = null;
		try {
			stream = factory.createFileStream(reportName
					+ ".junit.xml");
			writer = XMLUtils.createWriter(stream);
			writer.writeStartDocument();

			Q7Statistics statistics = ReportUtils.calculateStatisticsSlow(report.iterator());
			new JUnitXMLReportGenerator().writeSuite(writer, reportName,
					report.iterator(), statistics);

			writer.writeEndDocument();
			writer.flush();
		} catch (XMLStreamException ex) {
			return UTILS.createError(ex);
		} catch (CoreException ex) {
			return ex.getStatus();
		} finally {
			if (writer != null) {
				XMLUtils.closeWriter(writer);
			}
			try {
				stream.close();
			} catch (Exception exIgnore) {
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public String[] getGeneratedFileNames(String reportName) {
		return new String[] { reportName + ".junit.xml" };
	}
}
