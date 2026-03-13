/*******************************************************************************
 * Copyright (c) 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.reporting.util;

import static org.eclipse.rcptt.reporting.html.tests.HtmlReporterTest.createReport;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.sherlock.core.streams.SherlockReportOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class IndexedExecutionReportTest {
	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();
	

	@Test
	public void happyPath() throws ZipException, IOException {
		File f = temporaryFolder.newFile();
		try (
				var outputStream = Files.newOutputStream(f.toPath());
				var output = new SherlockReportOutputStream(outputStream);
				) {
			output.write(createReport("a", IStatus.OK));
			// how to flush?	
			try (var subject = new IndexedExecutionReport(f.toPath())) {
				assertEquals(IStatus.OK, subject.getById("a").getEntry().getStatusSeverity());
				assertNotNull(subject.getById("a").getReport());
				output.write(createReport("b", IStatus.ERROR));
				assertEquals(IStatus.ERROR, subject.getById("b").getEntry().getStatusSeverity());
				assertEquals(IStatus.OK, subject.getById("a").getEntry().getStatusSeverity());
				assertNotNull(subject.getById("b").getReport());
			}
		}
		try (var subject = new IndexedExecutionReport(f.toPath())) {
			assertEquals(IStatus.ERROR, subject.getById("b").getEntry().getStatusSeverity());
			assertEquals(IStatus.OK, subject.getById("a").getEntry().getStatusSeverity());
			assertNotNull(subject.getById("a").getReport());
			assertNotNull(subject.getById("b").getReport());
		}
	}
	
	
	@SuppressWarnings("resource")
	@Test
	public void incompleteToCompleteArchive() throws ZipException, IOException {
		File f = temporaryFolder.newFile();
		IndexedExecutionReport subject = null;
		try {
			try (var output = new SherlockReportOutputStream(Files.newOutputStream(f.toPath()))) {
				output.write(createReport("a", IStatus.OK));
				output.write(createReport("b", IStatus.ERROR));
				subject = new IndexedExecutionReport(f.toPath());
				assertEquals("b",  subject.getById("b").getReport().getRoot().getName());
			}
			System.gc(); // Does not actually clean the cache, just documents intent
			// To test in debugger, manually zero-out soft references
			// Entry read from incomplete archive should not break when archive is complete
			assertEquals("b",  subject.getById("b").getReport().getRoot().getName());
		} finally {
			if (subject != null) {
				subject.close();
			}
		}
	}
}
