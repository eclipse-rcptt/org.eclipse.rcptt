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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.persistence.plain.MapMaker;
import org.eclipse.rcptt.core.persistence.plain.PlainReader;
import org.eclipse.rcptt.core.persistence.plain.PlainReader.Entry;
import org.eclipse.rcptt.core.persistence.plain.PlainWriter;

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
		PlainReader reader = new PlainReader(bin);
		Map<String, String> map = reader.readHeader();
		TestCase.assertNotNull(map);
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
				TestCase.assertEquals(ecl_content, (String) entry.getContent());
			} else if (entry.getContent() instanceof byte[]) {
				// System.out.println(new String((byte[]) entry.getContent()));
				TestCase.assertTrue(Arrays.equals(raw_content,
						(byte[]) entry.getContent()));
			}
		}
	}
	
	@Test
	// Yes, normalization is not reasonable. It sucks.
	// Will have to keep for backward compatibility.
	// Discovered while fixing memory consumption in PlainTextPersistenceModel 
	public void textFormatNormalizesNewLines() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PlainWriter writer = new PlainWriter(bout, IPlainConstants.PLAIN_HEADER);
		writer.writeHeader(Collections.emptyMap());
		writer.writeNode("text_data_with_carriage_return", null, "line 1\r\nline 2\r\n");
		writer.close();
		PlainReader reader = new PlainReader(new ByteArrayInputStream(bout.toByteArray()));
		reader.readHeader();
		Entry entry = reader.readEntry();
		assertEquals("text_data_with_carriage_return", entry.name);
		assertEquals("line 1\nline 2\n", entry.getContent());
		
		
		
	}
}
