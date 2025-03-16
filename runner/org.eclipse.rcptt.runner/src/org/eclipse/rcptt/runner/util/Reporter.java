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
package org.eclipse.rcptt.runner.util;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.reporting.core.IReportRenderer;
import org.eclipse.rcptt.reporting.html.HtmlReportRenderer;
import org.eclipse.rcptt.reporting.html.HtmlReporter;
import org.eclipse.rcptt.reporting.util.FileContentFactory;
import org.eclipse.rcptt.reporting.util.JUnitFileReportGenerator;
import org.eclipse.rcptt.reporting.util.Q7ReportIterator;
import org.eclipse.rcptt.runner.HeadlessRunner;
import org.eclipse.rcptt.runner.HeadlessRunnerPlugin;
import org.eclipse.rcptt.runner.RunnerConfiguration;

public class Reporter implements IReporter {

	// the field are used by TestsRunner for implicit returning
	public boolean displayTimeoutMessage = false;

	public void report(Q7ReportIterator reportIterator, HeadlessRunner caller,
			RunnerConfiguration conf) {

		if (reportIterator != null) {
			if (conf.htmlReportLocation != null) {
				if (conf.splitHTMLReport) {
					createReport(HtmlReporter.class, reportIterator,
							conf.htmlReportLocation);
				} else {
					createReport(HtmlReportRenderer.class, reportIterator,
							conf.htmlReportLocation);
				}
			}
			if (conf.junitReportLocation != null) {
				createReport(JUnitFileReportGenerator.class, reportIterator,
						conf.junitReportLocation);
			}
			if (conf.report != null) {
				IPath filePath = new Path(conf.report.path);
				conf.report.renderer.getRenderer().generateReport(
						new FileContentFactory(filePath.removeLastSegments(1)),
						noExt(filePath.lastSegment()), reportIterator);
			}
		}

		if (displayTimeoutMessage) {
			System.out
					.println("\n\nTimeout is happen during execution. Not all test are executed.\n"
							+ "Please increase value of timeout passed with '-timeout' command line argument.\n"
							+ "Current timeout value is: "
							+ conf.executionTimeout + " seconds.");
		}
	}

	//

	public void createReport(Class<? extends IReportRenderer> rendererClass,
			Q7ReportIterator report, String outFile) {
		IReportRenderer renderer;
		try {
			renderer = rendererClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			log("Error creating renderer", e);
			return;
		}
		createReport(renderer, report, outFile);
	}

	public void createReport(IReportRenderer renderer, Q7ReportIterator report,
			String outFile) {
		IPath filePath = Path.fromOSString(outFile);
		renderer.generateReport(
				new FileContentFactory(filePath.removeLastSegments(1)),
				noExt(filePath.lastSegment()), report);
	}

	public static void log(String message, Exception e) {
		HeadlessRunnerPlugin
				.getDefault()
				.getLog()
				.log(new Status(IStatus.ERROR, HeadlessRunnerPlugin.PLUGIN_ID,
						message, e));
	}

	private static String noExt(String name) {
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1) {
			return name;
		}
		return name.substring(0, dotIndex);
	}

}
