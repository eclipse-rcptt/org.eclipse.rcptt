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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.persistence.plain.MapMaker;
import org.eclipse.rcptt.core.persistence.plain.PlainReader;
import org.eclipse.rcptt.core.persistence.plain.PlainReader.Entry;
import org.eclipse.rcptt.core.persistence.plain.PlainWriter;
import org.eclipse.rcptt.util.FileUtil;
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
		try (PlainWriter writer = new PlainWriter(bout, IPlainConstants.PLAIN_HEADER)) {
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
					
					try (ByteArrayOutputStream os = new ByteArrayOutputStream(); InputStream content = entry.getContent()) {
						FileUtil.copy(content, os);
						String contentType = entry.attributes.get(PlainReader.ATTR_CONTENT_TYPE);
						if (contentType.equals("text/plain")) {
							assertEquals(ecl_content, new String(os.toByteArray(), PlainReader.ENCODING));
						} else if (contentType.equals("q7/binary")) {
							assertTrue(Arrays.equals(raw_content, os.toByteArray()));
						} else {
							fail();
						}
					}
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
			try (InputStream is = e.getContent()) {
				Assert.assertTrue(toString(is).contains("verify-true"));
			}
		}
	}

	private String toString(InputStream is) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			FileUtil.copy(is, os);
			return new String(os.toByteArray(), PlainReader.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	private void assertEntryCount(int expected, PlainReader reader) throws IOException {
		int i = 0;
		while (reader.readEntry() != null) {  // should not throw
			i++;
		}
		assertEquals(expected, i);
	}
}
