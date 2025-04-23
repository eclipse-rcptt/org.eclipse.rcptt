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
package org.eclipse.rcptt.core.tests.model.plainformat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.persistence.plain.MapMaker;
import org.eclipse.rcptt.core.persistence.plain.PlainReader;
import org.eclipse.rcptt.core.persistence.plain.PlainReader.Entry;
import org.eclipse.rcptt.core.persistence.plain.PlainWriter;
import org.junit.Assert;
import org.junit.Test;

public class PlainFormatTests {
	@Test
	public void testWritePlainFile() throws Throwable {
		String ecl_content = "my ecl script\nmy second ecl command";
		byte[] raw_content = ("my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n"
				+ "my ecl script\nmy second ecl command\n")
						.getBytes(IPlainConstants.ENCODING);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PlainWriter writer = new PlainWriter(bout, IPlainConstants.PLAIN_HEADER);
		writer.writeHeader(new MapMaker<String, String>().set("CreationTime",
				Long.toString(System.currentTimeMillis())));

		writer.writeNode("ecl", null, ecl_content);
		writer.writeNode("q7.raw", null, raw_content);
		writer.close();

		// String content = bout.toString();

		// System.out.println(content);
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		try (PlainReader reader = new PlainReader(bin)) {
			Map<String, String> map = reader.readHeader();
			Assert.assertNotNull(map);
			// System.out.println(map);
			while (true) {
				Entry entry = reader.readEntry();
				if (entry == null) {
					break;
				}
				// System.out.println("entry:" + entry.name);
				// System.out.println(entry.attributes);
				if (entry.getContent() instanceof String) {
					// System.out.println(entry.getContent());
					assertEquals(ecl_content, (String) entry.getContent());
				} else if (entry.getContent() instanceof byte[]) {
					// System.out.println(new String((byte[]) entry.getContent()));
					assertTrue(Arrays.equals(raw_content,
							(byte[]) entry.getContent()));
				}
			}
		}
	}

	@Test
	public void invalidEntryHeaderReproducer() throws IOException {
		try (PlainReader reader = new PlainReader(
				getClass().getResourceAsStream("ConfWithSybsystemsForComparison.ctx"))) {
			reader.readHeader();
			assertEntryCount(13, reader);
		}
	}

	@Test
	public void illegalBase64EndingSequenceReproducer() throws IOException {
		try (PlainReader reader = new PlainReader(
				getClass().getResourceAsStream("ProjectWithParametersContext.ctx"))) {
			reader.readHeader();
			assertEntryCount(4, reader);
		}
	}
	
	@Test
	public void illegalBase64EndingSequenceReproducer2() throws IOException {
		try (PlainReader reader = new PlainReader(
				getClass().getResourceAsStream("ProjectWithParametersContext2.txt"))) {
			assertEntryCount(4, reader);
		}
	}


	@Test
	public void footerShouldOnlyBeRecognizedOnAseparateLine() throws Exception {
		String data = """
				--- RCPTT testcase ---
				Format-Version: 1.0
				Save-Time: 1/16/18 8:50 PM
				Testcase-Type: ecl

				------=_.content-0a7243a0-75d3-3d5f-9791-539de0e5b7ac
				Content-Type: text/ecl
				Entry-Name: .content

				>------=_.content-0a7243a0-75d3-3d5f-9791-539de0e5b7ac--" | verify-true
				------=_.content-0a7243a0-75d3-3d5f-9791-539de0e5b7ac--
				""";
		try (PlainReader reader = new PlainReader(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)))) {
			reader.readHeader();
			Entry e = reader.readEntry(); // should not throw
			Assert.assertTrue(((String) e.getContent()).contains("verify-true"));
		}
	}

	// @Test
	// public void file() throws Exception {
	// try (InputStream is = Files
	// .newInputStream(Path.of("/Users/vasiligulevich/git/org.eclipse.rcptt",
	// "rcpttTests/ECL_IDE_module/selfAUTTests/verifications/Workspace/VerifyWorkspace.test"));) {
	// PlainReader reader = new PlainReader(is);
	// Map<String, String> header = reader.readHeader();
	// for (Entry e = reader.readEntry(); e != null; e = reader.readEntry()) {
	// System.out.println(e.name);
	// }
	// }
	// }

	private void assertEntryCount(int expected, PlainReader reader) throws IOException {
		int i = 0;
		while (reader.readEntry() != null) {  // should not throw
			i++;
		}
		assertEquals(expected, i);
	}
	
}
