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
package org.eclipse.rcptt.reporting.util;

import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.Q7Statistics;
import org.eclipse.rcptt.reporting.core.IQ7ReportConstants;
import org.eclipse.rcptt.reporting.core.SimpleSeverity;
import org.eclipse.rcptt.reporting.util.internal.Plugin;
import org.eclipse.rcptt.reporting.util.internal.Reports;
import org.eclipse.rcptt.reporting.util.internal.XMLUtils;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

public class JUnitXMLReportGenerator {

	private static final String formatTime(Node nde) {
		return ReportUtils.formatTime(nde.getDuration());
	}

	public void writeContent(Reports reports, OutputStream stream)
			throws CoreException {
		XMLStreamWriter writer = XMLUtils.createWriter(stream);
		try {
			writer.writeStartDocument();
			writer.writeStartElement("testsuites");

			for (String suite : reports.getSuiteIds()) {
				Q7Statistics statistics = ReportUtils
						.calculateStatistics(reports.getReports(suite));
				writeSuite(writer, suite, reports.getReports(suite), statistics);
			}

			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
		} catch (Exception ex) {
			throw new CoreException(Plugin.UTILS.createError(ex));
		} finally {
			XMLUtils.closeWriter(writer);
		}
	}

	public void writeSuite(XMLStreamWriter writer, String name,
			Iterator<Report> reportList, Q7Statistics stats)
			throws XMLStreamException {

		writer.writeStartElement("testsuite");

		writer.writeAttribute("name", name);
		writer.writeAttribute("time", ReportUtils.formatTime(stats.getTime()));

		writer.writeAttribute("failures", Integer.toString(stats.getFailed()));
		writer.writeAttribute("tests", Integer.toString(stats.getTotal()));
		writer.writeAttribute("skipped", Integer.toString(stats.getSkipped()));
		writer.writeAttribute("errors", "0");

		while (reportList.hasNext()) {
			writeTestcase(writer, reportList.next());
		}

		writer.writeEndElement();
	}

	public void writeTestcase(XMLStreamWriter writer, Report report)
			throws XMLStreamException {
		Node item = report.getRoot();
		Q7Info info = (Q7Info) item.getProperties()
				.get(IQ7ReportConstants.ROOT);
		String name;

		if (info != null && info.getVariant() != null && info.getVariant().size() != 0) {
			name = item.getName() + "_" + ReportUtils.combineNames(info.getVariant(), "_");
		} else {
			name = item.getName();
		}

		writer.writeStartElement("testcase");
		writer.writeAttribute("name", name);
		writer.writeAttribute("time", formatTime(item));

		SimpleSeverity severity = SimpleSeverity.create(info);
		switch (severity) {
		case OK:
			break;
		case CANCEL:
			writer.writeAttribute("incomplete", "true");
			writer.writeStartElement("skipped");
			writer.writeAttribute("type", "testcase");
			writer.writeAttribute("message",  ReportUtils.getFailMessage(item));
			writer.writeEndElement();
			break;
		case ERROR:
			writer.writeStartElement("failure");

			writer.writeAttribute("type", "testcase");
			String msg = ReportUtils.getFailMessage(item);
			writer.writeAttribute("message", msg);

			String data = ReportUtils.getDetails(item).trim();
			if (data != null && !data.trim().isEmpty()) {
				writer.writeCData(msg + data);
			}

			writer.writeEndElement();
			break;
		}
		writer.writeEndElement();
	}

	public static void collectFailures(Node item, StringBuilder result) {
		Q7Info info = (Q7Info) item.getProperties().get(IQ7ReportConstants.ROOT);
		if (info != null && info.getResult().getSeverity() != 0) {
			String msg = info.getResult().getMessage();
			if (msg.contains("]]>")) {
				msg = msg.replace("]]>", "]] >");
			}
			if (msg != null) {
				result.append(msg).append(" [" + item.getName() + "]")
						.append("\n");
			}
		}
		EList<Node> children = item.getChildren();
		for (Node node : children) {
			collectFailures(node, result);
		}
	}
}
