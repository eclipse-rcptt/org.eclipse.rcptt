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
package org.eclipse.rcptt.reporting.html.tests;

import static java.util.Arrays.asList;

import java.io.File;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.html.HtmlReporter;
import org.eclipse.rcptt.reporting.util.FileContentFactory;
import org.eclipse.rcptt.reporting.util.Q7ReportIterator;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HtmlReporterTest {
	private VolatileContentFactory contentFactory;
	private HtmlReporter renderer;

	@Before
	public void before() {
		contentFactory = new VolatileContentFactory();
		renderer = new HtmlReporter();
	}

	@Test
	public void testNoPlaceholders() {
		String result = generate(Collections.<Report> emptyList());
		Assert.assertTrue("Even empty report should have summary", result.length() > 300);
		Assert.assertFalse("All placeholders should be replaced with numbers", result.contains("$"));
	}

	@Test
	public void testNoRoot() {
		Report report = ReportFactory.eINSTANCE.createReport();
		String result = generate(report);
		Assert.assertFalse("Broken report should be represented as nice as possible", result.contains("NullPointer"));
	}


	private Report createReport(String name, int severity) {
		Report report = ReportFactory.eINSTANCE.createReport();
		Node node = createNode(name, severity);
		ReportHelper.getInfo(node).setId(name);
		report.setRoot(node);
		return report;
	}

	private Node createNode(String name, int severity) {
		Node node = ReportFactory.eINSTANCE.createNode();
		node.setName(name);
		ReportHelper.getInfo(node).setResult(RcpttPlugin.createProcessStatus(severity, "No message"));
		return node;
	}

	@Test
	public void testStylingOfDifferentResults() {
		String result = generate(Arrays.asList(createReport("1", IStatus.ERROR), createReport("2", IStatus.OK),
				createReport("3", IStatus.CANCEL))).replaceAll("\\s+", " ");
		Assert.assertTrue("Failed tests has red titles", result.contains("<h1 class=\"failure\">Failed Tests (1)</h1>"));
		Assert.assertTrue("Failed tests has red titles", result.contains("<table class=\"failure"));
		Assert.assertTrue("Test 1 should be in a table", result.contains("<td> <a href=\"_1_0.html\">1</a> </td>"));
		Assert.assertTrue("Failed tests has red titles",
				result.contains("<h1 class=\"skipped\">Skipped Tests (1)</h1>"));
		Assert.assertTrue("Test 3 should be in a table", result.contains("<td>3</td>"));
		Assert.assertTrue("Failed tests has red titles", result.contains("<h1 class=\"passed\">Passed Tests (1)</h1>"));
		Assert.assertTrue("Test 2 should be in a table", result.contains("<td> <a href=\"_2_2.html\">2</a> </td>"));
	}

	private String generate(Iterable<Report> reports) {
		IStatus status = renderer.generateReport(contentFactory, "1", reports);
		if (status.matches(IStatus.ERROR | IStatus.CANCEL)) {
			throw new AssertionError(status.toString(), status.getException());
		}
		String result = contentFactory.read("1.html");
		Assert.assertTrue("Whole report should be generated", result.contains("Passed Tests ("));
		return result;
	}

	private String generate(Report report) {
		return generate(asList(report));
	}


	@Test
	public void testStyle() {
		String result = generate(Collections.<Report> emptyList());
		String style = contentFactory.read("rcptt.css");
		Assert.assertTrue("Style file should be written", style.length() > 0);
		Assert.assertTrue("Style file should be referenced", result.contains("<link href=\"rcptt.css\""));
	}

	@Test
	public void testScript() {
		String result = generate(Collections.<Report> emptyList());
		String style = contentFactory.read("rcptt.js");
		Assert.assertTrue("Script file should be written", style.length() > 0);
		Assert.assertTrue("Script file should be referenced",
				result.contains("<script type=\"text/javascript\" src =\"rcptt.js\">"));
		Assert.assertTrue("Script should be ivoked",
				result.contains("onload=\"installDetailsWorkaround()\""));
	}

	@Test
	public void testScriptInChild() {
		generate(asList(createReport("abc", IStatus.ERROR)));
		String result = contentFactory.read("_abc_0.html");
		Assert.assertTrue("Script file should be referenced",
				result.contains("<script type=\"text/javascript\" src =\"rcptt.js\">"));
		Assert.assertTrue("Script should be ivoked",
				result.contains("onload=\"installDetailsWorkaround()\""));
	}


	@Test
	public void testElapsed() {
		Report report = createReport("1", IStatus.OK);
		report.getRoot().setStartTime(1000);
		report.getRoot().setEndTime(3500);
		report.getRoot().setDuration(report.getRoot().getEndTime() - report.getRoot().getStartTime());
		Report report2 = createReport("2", IStatus.OK);
		report2.getRoot().setStartTime(4000);
		report2.getRoot().setEndTime(6000);
		report2.getRoot().setDuration(report2.getRoot().getEndTime() - report2.getRoot().getStartTime());
		String result = generate(asList(report, report2)).replaceAll("\\s", "");
		char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
		Assert.assertTrue("Statistics should have proper Execution time",
				result.contains("ExecutionTime</th><td>4" + separator + "5s"));
	}

	// @Test
	public void renderZip() {
		try (Q7ReportIterator iterator = new Q7ReportIterator(new File("C:\\Users\\vasili\\Downloads\\tests.report"))) {
			renderer.generateReport(new FileContentFactory(new File("C:\\temp\\reportHtmlTest")), "Name", iterator);
		}
	}
}
