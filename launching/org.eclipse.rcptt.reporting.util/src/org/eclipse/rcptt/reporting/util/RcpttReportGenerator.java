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
package org.eclipse.rcptt.reporting.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.ecl.core.EclException;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.ecl.internal.core.ProcessStatusConverter;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.reporting.ItemKind;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.core.ImageEntry;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.core.SimpleSeverity;
import org.eclipse.rcptt.reporting.core.TimeFormatHelper;
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
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfo;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.utils.AdvancedInformationGenerator;
import org.eclipse.rcptt.util.StringUtils;

import com.google.common.base.Joiner;

public class RcpttReportGenerator {
	private final SimpleReportGenerator simpleReportGenerator = new SimpleReportGenerator();
	private final List<ImageEntry> images;
	private final PrintWriter writer;
	private long startTime = 0;

	private final Map<String, Long> totalWaitTime = new HashMap<String, Long>();
	private int maxMethodNameLength = 0;
	private int maxTotalTimeLength = 0;

	public RcpttReportGenerator(PrintWriter writer, List<ImageEntry> images) {
		this.writer = writer;
		this.images = images;
	}

	protected PrintWriter writeTabs(int tabs) {
		return writeTabs(writer, tabs);
	}

	public void writeReport(Report report, int tabs) {
		startTime = report.getRoot().getStartTime();
		printNode(report.getRoot(), tabs);
		printTotalWaitTime();
	}

	protected static <T extends Appendable> T writeTabs(T writer, int tabs) {
		for (int i = 0; i < tabs; ++i) {
			try {
				writer.append("  ");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return writer;
	}

	// @Override
	// public StringBuilder toString(StringBuilder builder,
	// int tabs, org.eclipse.emf.ecore.EObject obj,
	// String... ignores) {
	// if (obj instanceof AdvancedInformation) {
	// String content = new AdvancedInformationGenerator()
	// .generateContent((AdvancedInformation) obj);
	// builder.append(content);
	// return builder;
	// }
	// return super.toString(builder, tabs, obj, ignores);
	// };
	//
	public void printNode(Node infoNode, int tabs) {
		writeQ7Info(tabs, infoNode);

		writeQ7WaitInfo(tabs, infoNode);

		writeLogsFromNode(tabs, infoNode);

		try {
			for (Event child : infoNode.getEvents()) {
				writeEvent(child, tabs + 1);
			}

			for (Snaphot child : infoNode.getSnapshots()) {
				writeSnapshot(child, tabs + 1);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		printChildren(tabs, infoNode);
	}

	private void writeEvent(Event event, int tabs) throws IOException {
		if (event.getCount() == 1) {
			writeTabs(tabs + 1)
					.append("Event at ")
					.println(TimeFormatHelper.format(event.getTime() - startTime));
		} else {
			writeTabs(tabs + 1).append("Event: ")
					.append(String.valueOf(event.getCount()))
					.append(" times, first at ")
					.append(TimeFormatHelper.format(event.getTime() - startTime))
					.println();
		}
		printObject(event.getData(), tabs + 2);
	}

	public void printObject(EObject object, int tabs) throws IOException {
		if (object instanceof ProcessStatus) {
			printStatus((ProcessStatus) object, tabs);
		} else if (object instanceof Snaphot) {
			writeSnapshot((Snaphot) object, tabs);
		} else {
			simpleReportGenerator.toString(writer, tabs, object);
		}

	}

	private void writeSnapshot(Snaphot snapshot, int tabs) throws IOException {
		if (snapshot.getData() instanceof Screenshot) {
			Screenshot shot = (Screenshot) snapshot
					.getData();
			String description = shot.getMessage()
					+ ": " //$NON-NLS-1$
					+ TimeFormatHelper.format(snapshot
							.getTime()
							- startTime);
			images.add(new ImageEntry(shot.getData(), description));
			return;
		} else if (snapshot.getData() instanceof AdvancedInformation) {
			printAdvanced((AdvancedInformation) snapshot.getData(), tabs);
			return;
		}
		writeTabs(tabs).println("Snapshot " + TimeFormatHelper.format(snapshot.getTime() - startTime));
		printObject(snapshot.getData(), tabs + 1);
	}

	private void printAdvanced(AdvancedInformation data, int tabs) {
		new AdvancedInformationGenerator(writer).writeAdvanced(data, tabs);
	}

	private void printStatus(ProcessStatus status, int tabs) throws IOException {
		writeResult(writer, tabs, status);
	}

	protected void printChildren(int tabs, Node infoNode) {
		for (Node child : infoNode.getChildren()) {
			printNode(child, tabs + 4);
		}
	}

	private void writeQ7WaitInfo(int tabs, Node infoNode) {
		Q7WaitInfoRoot waitInfo = ReportHelper.getWaitInfo(infoNode, false);
		if (waitInfo != null) {
			writeQ7WaitInfo(tabs, waitInfo); //$NON-NLS-1$
		}
	}

	public static String getType(Q7WaitInfoRoot info, Q7WaitInfo q7WaitInfo) {
		String type = info.getTypesNames().get(q7WaitInfo.getTypeId());
		if (!TeslaFeatures.isIncludeIgnoredWaitDetails() && type.contains("(ignored)")) {
			return null;
		}
		return type;
	}

	public void writeQ7WaitInfo(int tabs, Q7WaitInfoRoot info) {
		List<Q7WaitInfo> infos = new ArrayList<Q7WaitInfo>(info.getInfos());
		if (infos.size() == 0) {
			return;
		}
		Comparator<Q7WaitInfo> comparator = new Comparator<Q7WaitInfo>() {
			@Override
			public int compare(Q7WaitInfo info1, Q7WaitInfo info2) {
				return Long.compare(info1.getDuration(), info2.getDuration());
			}
		};
		Collections.sort(infos, Collections.reverseOrder(comparator));

		String classNameColumn = "Method name"; //$NON-NLS-1$
		String totalTimeColumn = "Time"; //$NON-NLS-1$
		int classNameLength = classNameColumn.length();
		int totalTimeLength = totalTimeColumn.length();
		boolean isEmpty = true;

		for (Q7WaitInfo q7WaitInfo : infos) {
			long totalTime = q7WaitInfo.getDuration();
			String type = getType(info, q7WaitInfo);
			String className = SimpleReportGenerator.getClassName(info, q7WaitInfo);
			if (type == null) {
				continue;
			}
			if (!TeslaFeatures.isIncludeEclipseMethodsWaitDetails()
					&& className.startsWith("org.eclipse")) { //$NON-NLS-1$
				continue;
			}
			if (totalTime == 0) {
				continue;
			}

			// calculate column length
			String methodName = String.format("%s: %s", type, className);
			if (methodName.length() > classNameLength) {
				classNameLength = methodName.length();
			}
			if (String.valueOf(totalTime).length() > totalTimeLength) {
				totalTimeLength = String.valueOf(totalTime).length();
			}

			isEmpty = false;
		}
		if (isEmpty) {
			return;
		}

		writeTabs(tabs + 4).println("--> Wait details <--");
		writeTabs(tabs + 8)
				.append(String.format("%" + -classNameLength + "s", classNameColumn))
				.append("   ")
				.append(String.format("%" + -totalTimeLength + "s", totalTimeColumn))
				.println();

		for (Q7WaitInfo i : infos) {
			long totalTime = i.getDuration();
			String type = getType(info, i);
			String className = SimpleReportGenerator.getClassName(info, i);
			if (type == null) {
				continue;
			}
			if (!TeslaFeatures.isIncludeEclipseMethodsWaitDetails()
					&& className.startsWith("org.eclipse")) { //$NON-NLS-1$
				continue;
			}
			if (totalTime == 0) {
				continue;
			}

			String methodName = String.format("%s: %s", type, className);
			writeTabs(tabs + 8)
					.append(String.format("%" + -classNameLength + "s", methodName))
					.append("   ")
					.append(String.format("%" + totalTimeLength + "s", totalTime))
					.println();

			addWaitTime(type, className, totalTime);
		}
	}

	String kindToString(ItemKind kind, String name) {
		switch (kind) {
		case CONTEXT:
			return "Context *" + name + "*";
		case ECL_COMMAND:
			return name;
		case SCRIPT:
			return "Script *" + name + "*";
		case TESTCASE:
			return "Test case *" + name + "*";
		case TEST_SUITE:
			return "Test suite *" + name + "*";
		case VERIFICATION:
			return "Verification *" + name + "*";
		}
		return name;
	}

	private void writeQ7Info(int tabs, Node infoNode) {
		Q7Info q7Info = ReportHelper.getInfo(infoNode);
		writeTabs(tabs);
		if (q7Info != null) {
			writer.append(kindToString(q7Info.getType(), infoNode.getName()));
			if (!q7Info.getVariant().isEmpty()) {
				writer
				.append(" (")
				.append(Joiner.on(", ").join(q7Info.getVariant()))
				.append(")");
			}
			writer
				.append(" time: ")
				.append(TimeFormatHelper.format(infoNode.getDuration()))
				.println();
			writeResult(writer, tabs + 1, q7Info.getResult());
		}
	}

	public static void writeResult(Writer writer, int tabs, ProcessStatus result) {
		if (result == null)
			result = RcpttPlugin.createProcessStatus(IStatus.ERROR, "Null result");
		if (SimpleSeverity.create(result) == SimpleSeverity.OK)
			return;
		try {
			writeTabs(writer, tabs).append("Result: ")
					.append(SimpleSeverity.create(result).name())
					.append(", message: ")
					.append(ReportUtils.getDirectFailMessage(result, ReportUtils.DEFAULT_DATUM_TO_MESSAGE))
					.append("\n");
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		writeException(writer, tabs + 1, result.getException());
		for (ProcessStatus child : result.getChildren()) {
			writeResult(writer, tabs + 1, child);
		}
	}

	private static void writeException(Writer writer, final int tabs, EclException exception) {
		if (exception == null)
			return;
		IndentedWriter iwriter = new IndentedWriter(writer) {
			@Override
			public void writeIndent() {
				writeTabs(this, tabs);
			}
		};
		Throwable throwable = ProcessStatusConverter.getThrowable(exception);
		throwable.printStackTrace(iwriter);
		ProcessStatus status = exception.getStatus();
		if (status != null) {
			writeResult(iwriter, 1, status);
		}
			
		iwriter.println();
		iwriter.flush();
	}

	public void writeLogsFromNode(int tabs, Node infoNode) {
		boolean haveEntries = false;
		for (LoggingCategory logCategory : LoggingCategory.VALUES) {
			String log = ReportBuilder.getLogs(infoNode, logCategory);
			if (!StringUtils.isEmpty(log)) {
				if (!haveEntries) {
					haveEntries = true;
					writeTabs(tabs)
							.println("--------------Logs BEGIN-------------------"); //$NON-NLS-1$
				}

				for (String logLine : log.split("[\r\n]+")) {
					writeTabs(tabs)
							.append(logLine)
							.println();
				}
			}
		}

		if (haveEntries) {
			writeTabs(tabs)
					.append("--------------Logs END-------------------") //$NON-NLS-1$
					.println();
		}
	}

	private void addWaitTime(String type, String className, long totalTime) {
		if (!(type.equals("job") || type.equals("sync") || type.equals("async"))) { //$NON-NLS-1$
			return;
		}
		if (!TeslaFeatures.isIncludeEclipseMethodsWaitDetails()
				&& className.startsWith("org.eclipse")) { //$NON-NLS-1$
			return;
		}

		String methodName = String.format("%s: %s", type, className);
		if (totalWaitTime.containsKey(methodName)) {
			totalTime += totalWaitTime.get(methodName);
		}
		totalWaitTime.put(methodName, totalTime);

		int methodNameLength = methodName.length();
		if (methodNameLength > maxMethodNameLength) {
			maxMethodNameLength = methodNameLength;
		}
		int totalTimeLength = String.valueOf(totalTime).length();
		if (totalTimeLength > maxTotalTimeLength) {
			maxTotalTimeLength = totalTimeLength;
		}
	}

	private void printTotalWaitTime() {
		String totalWaitTimeTable = "Total wait time:"; //$NON-NLS-1$
		String methodNameColumn = "Method name"; //$NON-NLS-1$
		String totalTimeColumn = "Time"; //$NON-NLS-1$
		String noWaitInfoMessage = TeslaFeatures.isIncludeEclipseMethodsWaitDetails()
				? "There were no methods RCPTT was waiting for"
				: "There were no third-party methods RCPTT was waiting for";

		int methodNameLength = methodNameColumn.length();
		if (methodNameLength > maxMethodNameLength) {
			maxMethodNameLength = methodNameLength;
		}
		int totalTimeLength = totalTimeColumn.length();
		if (totalTimeLength > maxTotalTimeLength) {
			maxTotalTimeLength = totalTimeLength;
		}

		writer.println();
		writer.append(totalWaitTimeTable).println();
		
		if (totalWaitTime.isEmpty()) {
			writer.append("  ")
					.append(noWaitInfoMessage) // $NON-NLS-1$
					.println();
			return;
		}
		
		writer.append("  ")
				.append(String.format("%" + -maxMethodNameLength + "s", methodNameColumn))
				.append("   ")
				.append(String.format("%" + -maxTotalTimeLength + "s", totalTimeColumn))
				.println();
		List<Map.Entry<String, Long>> sortedTable = getSortedTimeTable(totalWaitTime);
		for (Map.Entry<String, Long> entry : sortedTable) {
			writer.append("  ")
					.append(String.format("%" + -maxMethodNameLength + "s", entry.getKey()))
					.append("   ")
					.append(String.format("%" + maxTotalTimeLength + "s", entry.getValue()))
					.println();
		}
	}

	private List<Map.Entry<String, Long>> getSortedTimeTable(Map<String, Long> unsortedMap) {
		Comparator<Map.Entry<String, Long>> comparator = new Comparator<Map.Entry<String, Long>>() {

			@Override
			public int compare(Map.Entry<String, Long> entry1, Map.Entry<String, Long> entry2) {
				return entry1.getValue().compareTo(entry2.getValue());
			}

		};

		List<Map.Entry<String, Long>> sortedTable = new ArrayList<>(unsortedMap.entrySet());
		Collections.sort(sortedTable, Collections.reverseOrder(comparator));
		return sortedTable;
	}

}