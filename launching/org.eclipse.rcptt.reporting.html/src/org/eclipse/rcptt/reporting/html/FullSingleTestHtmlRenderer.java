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

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static org.eclipse.rcptt.reporting.util.ReportUtils.replaceLineBreaks;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.ecl.core.EclException;
import org.eclipse.rcptt.ecl.core.EclString;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.ecl.internal.core.ProcessStatusConverter;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.core.SimpleSeverity;
import org.eclipse.rcptt.reporting.util.ReportUtils;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Event;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.LoggingCategory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Screenshot;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Snaphot;
import org.eclipse.rcptt.sherlock.core.reporting.ReportBuilder;
import org.eclipse.rcptt.sherlock.core.reporting.SimpleReportGenerator;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.InfoNode;
import org.eclipse.rcptt.tesla.core.info.JobEntry;
import org.eclipse.rcptt.tesla.core.info.NodeProperty;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfo;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.info.StackTraceEntry;
import org.eclipse.rcptt.verifications.status.VerificationStatusData;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.html.HtmlEscapers;

public class FullSingleTestHtmlRenderer {
	private final PrintWriter writer;
	private final NumberFormat durationFormat;
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
	private final Function<Screenshot, String> imageStorage;
	private static final ILog LOG = Platform.getLog(FullSingleTestHtmlRenderer.class);

	public static final Function<EObject, String> HTML_DATUM_TO_MESSAGE = new Function<EObject, String>() {

		@Override
		public String apply(EObject input) {
			String message = ReportUtils.DEFAULT_DATUM_TO_MESSAGE.apply(input);
			message = escapeMultiline(message);

			if (input instanceof VerificationStatusData) {
				return String.format("<pre>%s</pre>", message);
			}
			return message;
		}

	};

	/**
	 * @param writer
	 *            - output
	 * @param durationFormat
	 *            - format of test duration in seconds
	 * @param imageStorage
	 *            - stores an image and returns full path to it
	 */
	public FullSingleTestHtmlRenderer(PrintWriter writer, NumberFormat durationFormat, Function<Screenshot, String> imageStorage) {
		super();
		this.writer = Objects.requireNonNull(writer);
		this.durationFormat = Objects.requireNonNull(durationFormat);
		this.imageStorage = Objects.requireNonNull(imageStorage);
	}

	private void renderHeader(int level, String title, String classes) {
		level += 2;
		if (level > 6)
			level = 6;
		writer.println(String.format("<h%d class=\"%s\">%s</h%d>", level, classes, escape(title), level));
	}

	public void render(Report report) {
		Node root = report.getRoot();
		if (root == null) {
			renderHeader(0, "Empty report", "failure");
			return;
		}
		renderHeader(0, root.getName(), toFailureClass(root));
		renderMain(root);
		openDetails(1, "Details", "");
		renderNode(root);
		closeDetails();
	}

	private void renderNode(Node node) {
		Q7Info info = ReportHelper.getInfo(node);
		renderResult(info.getResult());
		EList<Node> children = node.getChildren();
		Q7WaitInfoRoot waitInfo = ReportHelper.getWaitInfo(node, false);
		if (waitInfo != null) {
			renderWaitInfo(waitInfo);
		}
		writer.println("<div class=\"childNode\">");
		for (Node child : children) {
			openDetails(2, child.getName(), toFailureClass(child));
			renderNode(child);
			closeDetails();
		}
		writer.println("</div>");
		renderEvents(node.getEvents());
		renderSnapshots(node.getSnapshots());
		StringBuilder logs = new StringBuilder();
		for (LoggingCategory logCategory : LoggingCategory.VALUES) {
			String logs2 = ReportBuilder.getLogs(node, logCategory);
			if (logs2 != null)
				logs.append(logs2);
		}
		if (logs.length() > 2) {
			renderHeader(2, "Logs", "");
			writer.println("<pre>");
			writer.println(logs);
			writer.println("</pre>");
		}
	}

	private String toFailureClass(Q7Info info) {
		SimpleSeverity severity = SimpleSeverity.create(info);
		switch (severity) {
		case CANCEL:
			return "skipped";
		case ERROR:
			return "failure";
		case OK:
			return "passed";
		}
		return "";
	}

	private void renderSnapshots(EList<Snaphot> snapshots) {
		if (snapshots.isEmpty())
			return;
		renderHeader(2, "Snapshot", "");
		for (Snaphot e : snapshots) {
			renderHeader(3, dateFormat.format(e.getTime()), "");
			renderEvent(e.getData());
		}
	}

	private static final Predicate<Q7WaitInfo> isNotIgnored = new Predicate<Q7WaitInfo>() {

		@Override
		public boolean apply(Q7WaitInfo input) {
			return SimpleReportGenerator.getType(input) != null;
		}
	};

	private void renderWaitInfo(Q7WaitInfoRoot root) {
		Collection<Q7WaitInfo> nonIgnored = Collections2.filter(root.getInfos(), isNotIgnored);
		if (nonIgnored.isEmpty())
			return;
		renderHeader(2, "Profile", "");
		writer.println("<table><tr>");
		writer.println("<th>Task type</th>");
		writer.println("<th>Class</th>");
		writer.println("<th>Time taken (ms)</th></tr>");
		for (Q7WaitInfo info : nonIgnored) {
			long duration = info.getDuration();
			String type = SimpleReportGenerator.getType(root, info);
			assert type != null : "Should be prefiltered";
			String className = SimpleReportGenerator.getClassName(root, info);
			if (!TeslaFeatures.isIncludeEclipseMethodsWaitDetails()
					&& className.startsWith("org.eclipse")) { //$NON-NLS-1$
				continue;
			}
			row(type, className, "" + duration);
		}
		writer.println("</table>");
	}

	private void renderEvents(Collection<Event> events) {
		if (events.isEmpty())
			return;
		renderHeader(2, "Events", "");
		for (Event e : events) {
			if (e.getCount() == 1) {
				renderHeader(3, "Event at " + dateFormat.format(e.getTime()), "");
			} else {
				String header = "Event: "
						+ e.getCount()
						+ " times, first at "
						+ dateFormat.format(e.getTime());
				renderHeader(3, header, "");
			}
			renderEvent(e.getData());
		}
	}

	private void renderEvent(EObject eObject) {
		if (eObject == null) {
			writer.println("null");
		} else if (eObject instanceof ProcessStatus) {
			renderResult((ProcessStatus) eObject);
		} else if (eObject instanceof Screenshot) {
			renderScreenShot((Screenshot) eObject, "");
		} else if (eObject instanceof AdvancedInformation) {
			renderAdvanced((AdvancedInformation) eObject);
		} else if (eObject == null) {
			writer.println("Event contains no data. This indicates a premature AUT termination.");
		} else if (eObject instanceof EclString) {
			writer.println("<pre>");
			writer.println(escape(((EclString) eObject).getValue()));
			writer.println("</pre>");
		} else {
			writer.println(eObject.eClass().getName());
		}
	}

	public void renderAdvanced(AdvancedInformation info) {
		EList<InfoNode> nodes = info.getNodes();
		for (InfoNode infoNode : nodes) {
			renderNode(infoNode);
		}
		// Append job information
		EList<JobEntry> jobs = info.getJobs();
		if (!jobs.isEmpty()) {
			renderHeader(5, "Jobs information", "");
			for (JobEntry jobEntry : jobs) {
				writer.println(jobEntry.getName());
				writer.println(": class=" + jobEntry.getJobClass());
				writer.println(", state=" + jobEntry.getState());
				if (jobEntry.getRule() != null) {
					writer.println("rule=" + jobEntry.getRule());
				}
				writer.println("<br>");
			}
		}
		// Append thread information
		EList<StackTraceEntry> threads = info.getThreads();
		if (!threads.isEmpty()) {
			renderHeader(5, "Thread information", "");
			writer.println("<div class=\"childNode\">");
			for (StackTraceEntry trace : threads) {
				if (trace.getThreadClass().equals(
						"org.eclipse.core.internal.jobs.Worker")
						&& trace.getStackTrace().size() <= 5) {
					// Skip Worker threads sleep state
					continue;
				}
				renderHeader(5, trace.getThreadName(), "");
				writer.append("class=").append(trace.getThreadClass()).println("<br>");
				EList<String> list = trace.getStackTrace();
				for (int i = 0; i < list.size(); i++) {
					writer.append(Integer.toString(list.size() - i - 1))
							.append(": ").append(list.get(i))
							.println("<br>");
				}
			}
			writer.println("</div>");
		}
	}

	private void renderNode(InfoNode infoNode) {
		writer.println(infoNode.getName());
		EList<NodeProperty> list = infoNode.getProperties();
		EList<InfoNode> childs = infoNode.getChildren();
		if (!list.isEmpty() || !childs.isEmpty()) {
			writer.println(" {");
			writer.println("<div class=\"childNode\">");
			// Out properties
			if (list.size() != 0) {
				for (NodeProperty nodeProperty : list) {
					writer.println(nodeProperty.getName() + "=" + nodeProperty.getValue() + " <br>");
				}
			}
			if (childs.size() != 0) {
				for (InfoNode child : childs) {
					renderNode(child);
				}
			}
			writer.println("}");
			writer.println("</div>");
		}
	}

	private void renderScreenShot(Screenshot shot, String attributes) {
		String path = getOrCreateScreenshot(shot);
		writer.println(String.format("<a href=\"%s\">", path));
		if (!Strings.isNullOrEmpty(attributes)) {
			attributes = " " + attributes;
		}
		writer.println(String.format("<img src=\"%s\"" + attributes + "/>", path));
		writer.println("</a>");
	}

	private final Map<Screenshot, String> cache = new HashMap<Screenshot, String>();

	private String getOrCreateScreenshot(Screenshot shot) {
		String rv = cache.get(shot);
		if (rv != null)
			return rv;
		cache.put(shot, rv = imageStorage.apply(shot));
		return rv;
	}

	private void openDetails(int level, String title, String classes) {
		writer.print("<details class=\"closed\">");
		writer.print("<summary>");
		renderHeader(level, title, classes);
		writer.println("</summary>");
		writer.println("<div class=\"detailsContent\">");
	}

	private void closeDetails() {
		writer.println("</div>");
		writer.println("</details>");
	}

	private void renderResult(ProcessStatus result) {
		if (result == null) {
			writer.println("Result is null");
		} else {
			writer.println(SimpleSeverity.create(result).toString());
			if (!Strings.isNullOrEmpty(result.getMessage()))
				writer.println(", message: " + escape(result.getMessage()) + "<br>");
			if (result.getException() != null) {
				openDetails(5, "Exception", "");
				renderException(result.getException());
				closeDetails();
			}
			writer.println("<div class=\"childNode\">");
			for (ProcessStatus child : result.getChildren()) {
				if (child.getSeverity() != 0)
					renderResult(child);
			}
			writer.println("</div>");
		}
	}

	private static String escape(String input) {
		if (input == null) {
			return null;
		}
		return HtmlEscapers.htmlEscaper().escape(input);
	}
	
	private static String escapeMultiline(String input) {
		if (input == null) {
			return null;
		}
		input = escape(input);
		return ReportUtils.replaceLineBreaks(input);
	}
	
	private void renderException(EclException exception) {
		Throwable throwable = ProcessStatusConverter.getThrowable(exception);
		writer.println(escape(exception.getClassName())+": "+ escape(exception.getMessage()) + " <br>");
		StringWriter trace = new StringWriter(); 
		try {
			try (PrintWriter w = new PrintWriter(trace)) {
				throwable.printStackTrace(w);
			}
		} catch (Exception e) {
			LOG.error("Failed to parse report exception", e);
		}
		writer.println("<pre>");
		writer.println(escape(trace.toString()));
		writer.println("</pre>");
		if (exception.getStatus() != null) {
			renderResult(exception.getStatus());
		}
	}

	private void titledRow(String key, String value) {
		Preconditions.checkNotNull(key);
		if (value != null)
			writer.println(String.format("<tr><th>%s</th><td>%s</td></tr>", key, escapeMultiline(value)));
	}

	private void row(String key, String value1, String value2) {
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(value1);
		Preconditions.checkNotNull(value2);
		writer.println(String.format("<tr><td>\"%s\"</td><td>%s</td><td>%s</td></tr>", key, value1, value2));
	}

	private void renderMain(Node root) {
		String message = ReportUtils.getFailMessage(root, HTML_DATUM_TO_MESSAGE);
		writer.println("<table class=\"" + toFailureClass(root) + "\">");
		titledRow("Failure Reason", message);
		String tags = ReportUtils.getScenarioTags(root);
		tags = replaceLineBreaks(tags).trim();
		titledRow("Tags", Strings.emptyToNull(tags));
		titledRow("Duration", durationFormat.format(durationSeconds(root)));
		String desc = ReportUtils.getScenarioDescription(root);
		titledRow("Description", desc);
		writer.println("</table>");
		renderScreenShots(root);
	}

	private String toFailureClass(Node root) {
		return toFailureClass(ReportHelper.getInfo(root));
	}

	private void renderScreenShots(Node root) {
		List<Screenshot> screens = Lists.newArrayList(concat(transform(recursiveChildren.apply(root), screenShots)));
		if (screens.isEmpty())
			return;
		renderHeader(1, "Screenshots", "");
		for (Screenshot screen : screens) {
			renderScreenShot(screen, "width=\"100px\"");
		}

	}

	private final static Function<Node, Iterable<Screenshot>> screenShots = new Function<Node, Iterable<Screenshot>>() {

		@Override
		public Iterable<Screenshot> apply(Node input) {
			List<Screenshot> acc = new ArrayList<Screenshot>(0);
			for (Snaphot snapshot : input.getSnapshots()) {
				if (snapshot.getData() instanceof Screenshot) {
					acc.add((Screenshot) snapshot.getData());
				}
			}
			return acc;
		}
	};

	private final static Function<Node, Iterable<Node>> recursiveChildren = new Function<Node, Iterable<Node>>() {
		@Override
		public Iterable<Node> apply(Node input) {
			Iterable<Node> children = Iterables.<Node> concat(Collections2.transform(input.getChildren(),
					recursiveChildren));
			return Iterables.<Node> concat(Arrays.asList(input), children);
		}
	};

	private static float durationSeconds(Node node) {
		long millseconds = node.getDuration();
		return (millseconds) / 1000f;
	}

}
