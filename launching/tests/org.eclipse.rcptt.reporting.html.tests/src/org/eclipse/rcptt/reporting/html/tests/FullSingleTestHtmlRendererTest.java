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

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.html.FullSingleTestHtmlRenderer;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Screenshot;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Snaphot;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.verifications.status.EVerificationStatus;
import org.eclipse.rcptt.verifications.status.StatusFactory;
import org.eclipse.rcptt.verifications.status.VerificationStatusData;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;

public class FullSingleTestHtmlRendererTest {
	private int screenshotsCount = 0;
	private final Function<Screenshot, String> screenshotHandler = new Function<Screenshot, String>() {

		@Override
		public String apply(Screenshot input) {
			return "" + (screenshotsCount++) + ".png";
		}
	};

	@Before
	public void before() {
		screenshotsCount = 0;
	}


	@Test
	public void screenShots() {
		Report report = createReport("1", IStatus.ERROR);
		Snaphot sn = ReportFactory.eINSTANCE.createSnaphot();
		report.getRoot().getSnapshots().add(sn);
		Screenshot sc = ReportFactory.eINSTANCE.createScreenshot();
		sn.setData(sc);
		sc.setData(new byte[] { 1, 2 });
		String result = generate(report);
		Pattern image = Pattern.compile("\\<img src=\"0.png\"[^\\>]*/\\>");
		String[] sections = image.split(result);
		Assert.assertEquals("Only one image should be stored on FS", 1, screenshotsCount);
		Assert.assertEquals("There should be two links to image", 3, sections.length);
		Assert.assertFalse("One image should be in summary", sections[0].contains("Details"));
		Assert.assertTrue("One image should be in Details", sections[1].contains("Details"));
		Assert.assertFalse("One image should be in Details", sections[2].contains("Details"));
	}

	private Report createReport(String name, int severity) {
		Report report = ReportFactory.eINSTANCE.createReport();
		report.setRoot(createNode(name, severity));
		return report;
	}

	private Node createNode(String name, int severity) {
		Node node = ReportFactory.eINSTANCE.createNode();
		node.setName(name);
		ReportHelper.getInfo(node).setResult(RcpttPlugin.createProcessStatus(severity, "No message"));
		return node;
	}

	@Test
	public void passedChildOfFailedNodeShouldBeBlack() {
		Report report = createReport("1", IStatus.ERROR);
		Node node = createNode("2", IStatus.OK);
		report.getRoot().getChildren().add(node);
		String result = generate(report);
		Assert.assertTrue("Passed node header should have passed style", result.contains("<h4 class=\"passed\">2</h4>"));
	}

	@Test
	public void failedChildShouldBeRed() {
		Report report = createReport("1", IStatus.ERROR);
		Node node = createNode("2", IStatus.ERROR);
		report.getRoot().getChildren().add(node);
		String result = generate(report);
		Assert.assertTrue("Failed node header should have failure class",
				result.contains("<h4 class=\"failure\">2</h4>"));
	}

	@Test
	public void failedTestHeaderShouldBeRed() {
		Report report = createReport("1", IStatus.ERROR);
		String result = generate(report);
		Assert.assertTrue("Failed node header should have failure class",
				result.contains("<h2 class=\"failure\">1</h2>"));
	}

	@Test
	public void passedTestHeaderShouldBeGreen() {
		Report report = createReport("1", IStatus.OK);
		String result = generate(report);
		Assert.assertTrue("Failed node header should have failure class",
				result.contains("<h2 class=\"passed\">1</h2>"));
	}

	@Test
	public void testEmptyLogsAreNotShown() {
		Report report = ReportFactory.eINSTANCE.createReport();
		Node node = ReportFactory.eINSTANCE.createNode();
		report.setRoot(node);
		node.setName("2");
		ReportHelper.getInfo(node).setResult(RcpttPlugin.createProcessStatus(IStatus.ERROR, "Failed"));
		String result = generate(report);
		Assert.assertFalse("No logs header should be present when there are no logs", result.contains("Logs"));
		Assert.assertFalse("No null for empty log", result.contains("null"));
	}

	@Test
	public void testWaitInfo() {
		Report report = createReport("2", IStatus.ERROR);
		Q7WaitUtils.updateInfo("timer (ignored)", "someclass", ReportHelper.getWaitInfo(report.getRoot(), true));
		Q7WaitUtils.updateInfo("sync", "otherclass", ReportHelper.getWaitInfo(report.getRoot(), true));
		String result = generate(report);
		Assert.assertTrue("sync type should be seen", result.contains("sync"));
		Assert.assertTrue("sync type should be seen", result.contains("otherclass"));
		Assert.assertFalse("ignored type should not be seen", result.contains("timer"));
		Assert.assertFalse("ignored type should not be seen", result.contains("someclass"));
		Assert.assertFalse("All placeholders should be replaced with numbers", result.contains("$"));
		String stripped = result.replaceAll("\\s", "");
		Assert.assertFalse("Header without row", stripped.contains("<table><th>"));
		Assert.assertFalse("Cell without row", stripped.contains("<table><td>"));
	}
	
	@Test
	public void escapeVerificationsOnce() {
		EVerificationStatus verificationStatus = StatusFactory.eINSTANCE.createEVerificationStatus();
		VerificationStatusData data = StatusFactory.eINSTANCE.createVerificationStatusData();
		verificationStatus.setSeverity(IStatus.ERROR);
		verificationStatus.getData().add(data);
		verificationStatus.setMessage("Math error");
		data.setMessage("a > 2");
		data = EcoreUtil.copy(data);
		data.setMessage("b < 4");
		verificationStatus.getData().add(EcoreUtil.copy(data));

		Report report = createReport("2", IStatus.ERROR);
		ReportHelper.getInfo(report.getRoot()).getResult().getChildren().add(verificationStatus);

		String result = generate(report);
		
		assertThat(result, Matchers.stringContainsInOrder(
			"Math error",
			"<pre>",
			"a &gt; 2",
			"\n",
			"b &lt; 4",
			"</pre>",
			"Details"
		));
	}
	
	@Test
	public void escapeExecutionStatus() {
		Report report = createReport("2", IStatus.ERROR);
		ReportHelper.getInfo(report.getRoot()).getResult().setMessage("expected:<wait 200\\n> but was:<wait 200>");

		String result = generate(report);
		
		assertThat(result, Matchers.stringContainsInOrder(
				"Failure Reason",
				"expected:&lt;wait 200\\n&gt; but was:&lt;wait 200&gt;",
				"Details",  
				"expected:&lt;wait 200\\n&gt; but was:&lt;wait 200&gt;"
		));
	}

	

	private String generate(Report report) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		FullSingleTestHtmlRenderer renderer = new FullSingleTestHtmlRenderer(printWriter, new DecimalFormat(),
				screenshotHandler);
		renderer.render(report);
		printWriter.flush();
		return stringWriter.toString();
	}



}
