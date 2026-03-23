/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.reporting.html;

import static org.eclipse.rcptt.reporting.html.internal.Plugin.UTILS;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.util.FileUtil;

import com.google.common.base.Strings;

public class HtmlReporter extends HtmlReportRenderer {
	private int nameIndex = 0;

	private String getNewFileName(Node node, IContentFactory factory) {
		String prefix = "_" + getNodeName(node);
		String fname = prefix + ".html";
		fname = prefix + "_" + (nameIndex++) + ".html";
		return fname;
	}

	@Override
	protected void renderFailed(PrintWriter writer, IContentFactory content, Iterable<Report> failedReports)
			throws CoreException {
		writer.println("<table class=\"failure\">");
		for (Report report: failedReports) {
			try {
				renderNameAndDuration(writer, report, content);
			} catch (Exception e) {
				UTILS.log(UTILS.createError(e));
				e.printStackTrace(writer);
			}
		}
		writer.println("</table>");
	}

	protected static void copyResource(IContentFactory content, IPath path) throws CoreException {
		try {
			FileUtil.copy(HtmlReportRenderer.class.getResourceAsStream(path.toPortableString()),
					content.createFileStream(path.lastSegment()));
		} catch (IOException e) {
			throw new CoreException(UTILS.createError(e));
		}
	}
	
	@Override
	protected void renderNameAndDuration(PrintWriter writer, Report report, IContentFactory content)
			throws CoreException {
		Node root = report.getRoot();
		if (root == null) {
			writer.println("<tr><td>Empty report</td><td></td></tr>");
			return;
		}
		long millseconds = root.getDuration();
		String duration = durationFormat.format((float) (millseconds) / 1000f);
		writer.println("<tr><td>");
		String fileName = getNewFileName(root, content);
		PrintWriter childWriter = null;
		try {
			OutputStream os = content.createFileStream(fileName);
			childWriter = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
			childWriter.println("<html>");
			renderHead(childWriter, root.getName());
			childWriter.println("<body onload=\"installDetailsWorkaround()\">");
			renderFailed(childWriter, report, content);
			childWriter.println("</body></html>");
		} finally {
			FileUtil.safeClose(childWriter);
		}
		writer.println(String.format("<a href=\"%s\">%s</a>", fileName, root.getName()));
		writer.println("</td><td>" + duration + " s</td></tr>");
	}

	@Override
	protected void renderHead(PrintWriter writer, String title) {
		writer.println("<head>");
		if (!Strings.isNullOrEmpty(title))
			writer.println("<title>" + title + "</title>");
		writer.println("<link href=\"rcptt.css\" rel=\"stylesheet\">");
		writer.println("<script type=\"text/javascript\" src =\"rcptt.js\"></script>");
		writer.println("</head>");
	}

	@Override
	protected void renderReport(PrintWriter writer, Iterable<Report> reports, IContentFactory content)
			throws CoreException {
		copyResource(content, Path.fromPortableString("/templates/rcptt.css"));
		copyResource(content, Path.fromPortableString("/templates/rcptt.js"));
		super.renderReport(writer, reports, content);
	}

}
