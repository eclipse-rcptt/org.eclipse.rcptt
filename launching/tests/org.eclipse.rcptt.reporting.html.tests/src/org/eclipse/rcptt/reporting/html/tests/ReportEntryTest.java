/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.reporting.html.tests;

import static org.junit.Assert.*;

import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.internal.core.ProcessStatusConverter;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.ReportingFactory;
import org.eclipse.rcptt.reporting.core.IQ7ReportConstants;
import org.eclipse.rcptt.reporting.util.ReportEntry;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.junit.Assert;
import org.junit.Test;

public class ReportEntryTest {
	private WeakReference<Report> reference = null;
	
	@Test
	public void test() throws InterruptedException {
		ReportEntry subject;
		{
			subject = createWeakReportIsolatedInMethodScope();
		}
		while (reference.get() != null) {
			System.gc();
			Thread.sleep(1000);
		}
		Assert.assertNotNull(subject); // We need subject reference to ensure it is not collected for the duration of
										// this test
	}

	private ReportEntry createWeakReportIsolatedInMethodScope() {
		ReportEntry subject;
		Report report = ReportFactory.eINSTANCE.createReport();
		Node node = ReportFactory.eINSTANCE.createNode();
		report.setRoot(node);
		node.setName("name");
		Q7Info info = ReportingFactory.eINSTANCE.createQ7Info();
		node.getProperties().put(IQ7ReportConstants.ROOT, info);
		info.setResult(ProcessStatusConverter.toProcessStatus(Status.OK_STATUS));
		info.setId("id");
		reference = new WeakReference<>(report);
		subject = ReportEntry.create(report);
		return subject;
	}

}
