/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.core.persistence.plain;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.rcptt.util.Base64;
import org.eclipse.rcptt.util.FileUtil;

public class PlainWriter implements IPlainConstants, Closeable {
	private OutputStream out;
	private OutputStreamWriter writer;
	private String plainStoreFormat;

	public PlainWriter(OutputStream bout, String plainHeader)
			throws IOException {
		this.out = bout;
		this.plainStoreFormat = plainHeader;
		this.writer = new OutputStreamWriter(new BufferedOutputStream(out),
				ENCODING);
	}

	private void write(String str) throws IOException {
		writer.write(str + "\n");
	}

	public void writeHeader(Map<String, String> attrs) throws IOException {
		write(getPlainFormat());
		writeAttributes(new MapMaker<String, String>().set(ATTR_FORMAT_VERSION, FORMAT_VERSION));
		writeAttributes(attrs);
		write("");
	}

	protected String getPlainFormat() {
		return plainStoreFormat;
	}

	private void writeAttributes(Map<String, String> attrs) throws IOException {
		if (attrs != null) {
			TreeMap<String, String> sorted = new TreeMap<String, String>();
			sorted.putAll(attrs);
			for (Map.Entry<String, String> entry : sorted.entrySet()) {
				write(entry.getKey() + ": "
						+ FileUtil.escape(entry.getValue()));
			}
		}
	}

	public void writeNode(String name, Map<String, String> attrs, Object content)
			throws IOException {
		UUID id = UUID.nameUUIDFromBytes(name.getBytes(ENCODING));
		String currentNode = name + "-" + id.toString();
		write(NODE_PREFIX + currentNode);
		if (attrs == null) {
			attrs = new HashMap<String, String>();
		}
		attrs.put(ATTR_ENTRY_NAME, name);
		String curType = attrs.get(ATTR_CONTENT_TYPE);
		if (content instanceof String) {
			if (curType == null || !curType.contains("text")) {
				attrs.put(ATTR_CONTENT_TYPE, "text/plain");
			}
		} else if (content instanceof byte[]) {
			if (curType == null || !curType.contains("binary")) {
				attrs.put(ATTR_CONTENT_TYPE, "q7/binary");
			}
		}
		writeAttributes(attrs);
		write("");
		if (content instanceof String) {
			writeContent((String) content);
		} else if (content instanceof byte[]) {
			writeContent((byte[]) content);
		}
		write(NODE_PREFIX + currentNode + NODE_POSTFIX);

	}

	private void writeContent(String content) throws IOException {
		writer.write(content);
		write("");
	}

	private void writeContent(byte[] binary) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (ZipOutputStream zout = new ZipOutputStream(bout)) {
			ZipEntry e = new ZipEntry(".content");
			e.setTime(1);
			zout.putNextEntry(e);
			zout.write(binary);
			zout.close();
			String encode = Base64.encode(bout.toByteArray());
			int len = encode.length();
			int chunks = len / STRIP_LEN;
			for (int i = 0; i < chunks; i++) {
				write(encode.substring(i * STRIP_LEN, (i + 1) * STRIP_LEN));
			}
			write(encode.substring(chunks * STRIP_LEN));
		}
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}
