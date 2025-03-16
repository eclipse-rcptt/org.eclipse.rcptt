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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.filter;
import static org.eclipse.rcptt.reporting.html.internal.Plugin.UTILS;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.Q7Statistics;
import org.eclipse.rcptt.reporting.core.IQ7ReportConstants;
import org.eclipse.rcptt.reporting.core.IReportRenderer;
import org.eclipse.rcptt.reporting.html.internal.Plugin;
import org.eclipse.rcptt.reporting.util.ReportUtils;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Screenshot;
import org.eclipse.rcptt.util.FileUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.html.HtmlEscapers;

public class HtmlReportRenderer implements IReportRenderer {

	private final String summaryTemplate = loadAsString("/templates/summary.html");

	protected final NumberFormat durationFormat = NumberFormat.getNumberInstance();
	{
		durationFormat.setMaximumFractionDigits(1);
		durationFormat.setMinimumFractionDigits(1);
	}

	static String loadAsString(String path) {
		try {
			byte[] content = FileUtil.getStreamContent(HtmlReportRenderer.class.getResourceAsStream(path));
			return new String(content, UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load " + path);
		}
	}

	@Override
	public IStatus generateReport(IContentFactory factory, String reportName,
			Iterable<Report> reportList) {
		PrintWriter writer = null;
		try {
			OutputStream stream = factory.createFileStream(reportName + ".html");
			writer = new PrintWriter(new OutputStreamWriter(stream, Charsets.UTF_8));
			renderReport(writer, reportList, factory);
		} catch (Exception e) {
			return Plugin.UTILS.createError(e);
		} finally {
			FileUtil.safeClose(writer);
		}
		return Status.OK_STATUS;
	}



	protected void renderReport(PrintWriter writer, Iterable<Report> reports, IContentFactory content)
			throws CoreException {
		writer.println("<html>");
		renderHead(writer, null);
		writer.println("<body onload=\"installDetailsWorkaround()\">");
		Q7Statistics statistics = ReportUtils.calculateStatistics(reports.iterator());
		renderSummary(writer, reports, statistics);
		Iterable<Report> passedReports = filter(reports, compose(equalTo(IStatus.OK), reportStatus));
		Iterable<Report> skippedReports = filter(reports, compose(matches(IStatus.CANCEL), reportStatus));
		Iterable<Report> failedReports = filter(reports,
				compose(not(or(equalTo(IStatus.OK), matches(IStatus.CANCEL))), reportStatus));

		writer.println("<h1 class=\"failure\">Failed Tests (" + statistics.getFailed() + ")</h1>");
		renderFailed(writer, content, failedReports);
		writer.println("<h1 class=\"skipped\">Skipped Tests (" + statistics.getSkipped() + ")</h1>");
		writer.println("<table class=\"skipped\">");
		for (Report report : skippedReports) {
			renderNameAndDuration(writer, report, content);
		}
		writer.println("</table>");

		writer.println("<details class=\"closed\"><summary><h1 class=\"passed\">Passed Tests ("
				+ statistics.getPassed()
				+ ")</h1></summary>");
		writer.println("<table class=\"passed detailsContent\">");
		for (Report report : passedReports) {
			renderNameAndDuration(writer, report, content);
		}
		writer.println("</table>");
		
		writer.println("</body></html>");
	}

	protected void renderHead(PrintWriter writer, String title) {
		writer.println("<head>");
		if (!Strings.isNullOrEmpty(title))
			writer.println("<title>" + title + "</title>");
		writer.println("<meta charset=\"utf-8\">");
		writer.println("<style>");
		writer.println(loadAsString("/templates/rcptt.css"));
		writer.println("</style>");
		writer.println("<script type=\"text/javascript\">");
		writer.println(loadAsString("/templates/rcptt.js"));
		writer.println("</script>");
		writer.println("</head>");
	}

	protected void renderFailed(PrintWriter writer, IContentFactory content, Iterable<Report> failedReports)
			throws CoreException {
		for (Report report:failedReports) {
			try {
				renderFailed(writer, report, content);
				writer.println("<hr/>");
			} catch (Exception e) {
				UTILS.log(UTILS.createError(e));
				e.printStackTrace(writer);
			}
		}
	}

	protected void renderNameAndDuration(PrintWriter writer, Report report, IContentFactory content)
			throws CoreException {
		Node root = report.getRoot();
		long millseconds = root.getDuration();
		String duration = durationFormat.format((float) (millseconds) / 1000f);
		writer.println("<tr><td>" + escape(root.getName()) + "</td><td>" + duration + " s</td></tr>");
	}

	private int screenshotCount = 0;

	protected void renderFailed(PrintWriter writer, Report report, final IContentFactory content) {
		final IContentFactory images = content.createFolder("images");
		FullSingleTestHtmlRenderer renderer = new FullSingleTestHtmlRenderer(writer, durationFormat, new Function<Screenshot, String>() {
			@Override
			public String apply(Screenshot input) {
				return "images/" + writeScreenshot(images, String.format("%3d", screenshotCount++), input);
			}
		});
		renderer.render(report);
	}

	private void renderSummary(PrintWriter writer, Iterable<Report> reports, Q7Statistics statistics) {
		renderStatistics(writer, statistics);
	}

	private void renderStatistics(PrintWriter writer, Q7Statistics statistics) {
		Replacer r = new Replacer(summaryTemplate);
		fillStatistics(statistics, r);
		writer.append(r.toString());
	}

	private int percent(int value, int total) {
		return (int) (((float) value / total) * 100);
	}
	private void fillStatistics(Q7Statistics statistics, Replacer r) {
		r.replace("totalCount", statistics.getTotal());
		r.replace("failedCount", statistics.getFailed());
		r.replace("failedPercent", percent(statistics.getFailed(), statistics.getTotal()));
		r.replace("skippedPercent", percent(statistics.getSkipped(), statistics.getTotal()));
		r.replace("skippedCount", statistics.getSkipped());
		r.replace("elapsed", durationFormat.format((float) (statistics.getTime()) / 1000f));
	}

	private static class Replacer {
		private String current;

		public Replacer(String current) {
			super();
			this.current = current;
		}

		/**
		 * Replaces ${key} with value
		 */
		public void replace(String key, String value) {
			current = current.replaceAll("\\$\\{" + key + "\\}", value);
		}

		public void replace(String key, int value) {
			replace(key, "" + value);
		}

		@Override
		public String toString() {
			return current;
		}
	}

	public String[] getGeneratedFileNames(String reportName) {
		return new String[] { reportName + ".html" };
	}

	private static final Function<Report, Integer> reportStatus = new Function<Report, Integer>() {
		@Override
		public Integer apply(Report input) {
			if (input.getRoot() == null)
				return IStatus.ERROR;
			Q7Info info = (Q7Info) input.getRoot().getProperties().get(IQ7ReportConstants.ROOT);
			if (info == null || info.getResult() == null)
				return IStatus.ERROR;
			return info.getResult().getSeverity();
		}
	};

	Predicate<Integer> matches(final int mask) {
		return new Predicate<Integer>() {
			@Override
			public boolean apply(Integer input) {
				return (input & mask) != 0;
			}
		};
	}


	private String writeScreenshot(IContentFactory images, String key,
			Screenshot value) {
		String ext = value.getKind().name().toLowerCase();
		String fileName = key + "." + ext;

		OutputStream stream = null;
		try {
			stream = images.createFileStream(fileName);
			stream.write(value.getData());
		} catch (Exception e) {
			throw new RuntimeException("Failed to write screenshot " + key);
		} finally {
			FileUtil.safeClose(stream);
		}
		return fileName;
	}

	protected String getNodeName(Node node) {
		String name = node.getName();

		Q7Info info = (Q7Info) node.getProperties().get("q7");
		if (info != null && info.getVariant() != null && info.getVariant().size() != 0) {
			name += "_" + ReportUtils.combineNames(info.getVariant(), ",");
		}
		name = name.trim().replace(" ", "");
		return FileUtil.rlimitSize(FileUtil.getID(name), 20).toLowerCase();
	}
	
	private String escape(String input) {
		if (input == null) {
			return null;
		}
		return HtmlEscapers.htmlEscaper().escape(input);
	}
}
