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
package org.eclipse.rcptt.core.persistence.plain;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import org.eclipse.rcptt.util.FileUtil;

import com.google.common.io.CharSource;

public class PlainReader implements IPlainConstants {
	private BufferedReader reader;
	private InputStream in;

	public static class Entry {
		public String name;
		private Object content;
		public Map<String, String> attributes;

		public Object getContent() {
			return content;
		}
	}

	public PlainReader(InputStream stream) throws IOException {
		this.in = stream;
		reader = new BufferedReader(new InputStreamReader(
				new BufferedInputStream(stream), ENCODING));
	}

	private static final List<String> VALID_HEADERS = Arrays.asList(PLAIN_HEADER, PLAIN_METADATA, PLAIN_VERIFICATION,
			LEGACY_PLAIN_HEADER, LEGACY_PLAIN_METADATA, LEGACY_PLAIN_VERIFICATION);
	public Map<String, String> readHeader() throws IOException {
		String header = reader.readLine();
		if (header == null) {
			return null;
		}
		header = header.trim();
		while( header.startsWith("#")) {
			header = reader.readLine();
			if( header == null) {
				return null;
			}
			header = header.trim();
		}
		boolean headerOK = false;
		for (String validHeader : VALID_HEADERS) {
			if (validHeader.equalsIgnoreCase(header)) {
				headerOK = true;
				break;
			}
		}
		if (!headerOK) {
			// Not a plain file
			return null;
		}
		Map<String, String> map = readAttributes();
		return map;
	}

	private Map<String, String> readAttributes() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		while (true) {
			String line = reader.readLine();
			if (line == null || line.trim().length() == 0) {
				break;
			}
			int pos = line.indexOf(": ");
			if (pos != -1) {
				String key = line.substring(0, pos);
				String value = FileUtil.unescape(line.substring(pos + 2));
				if (map.containsKey(key)) {
					throw new PlainFormatException(
							"Wrong RCPTT plain format, duplicate attribute entry.");
				}
				map.put(key, value);
			} else {
				if (line.trim().endsWith(":")) {
					// Null Value, skip it
				} else {
					throw new PlainFormatException("Wrong RCPTT plain format");
				}
			}
		}
		return map;
	}

	/**
	 * Return next entry and null if end of stream are detected
	 * 
	 * @return
	 * @throws IOException
	 */
	public Entry readEntry() throws IOException {
		String entryHeader = reader.readLine();
		if (entryHeader == null) {
			return null;
		}
		// Skip newlines.
		while (entryHeader != null && entryHeader.trim().length() == 0) {
			entryHeader = reader.readLine();
		}
		if (entryHeader == null) {
			return null;
		}
		if (entryHeader.startsWith(NODE_PREFIX)) {
			Entry entry = new Entry();
			entry.attributes = readAttributes();
			entry.name = entry.attributes.get(ATTR_ENTRY_NAME);
			String contentType = entry.attributes.get(ATTR_CONTENT_TYPE);
			try (SeparatorReader separatorReader = new SeparatorReader(reader, entryHeader + NODE_POSTFIX)) {
				if (contentType != null && contentType.contains("text")) {
					// Text mode content
					try (Stream<String> lines = new BufferedReader(separatorReader).lines()) {
						StringBuilder builder = new StringBuilder();
						lines.forEach(s -> builder.append(s).append("\n"));
						String resultStr = builder.toString();
						if (resultStr.endsWith("\n")) {
							resultStr = resultStr.substring(0, resultStr.length()
									- "\n".length());
						}
						entry.content = resultStr;
					}
				} else if (contentType != null && contentType.contains("binary")) {
					// Base64 encoded content
					CharSource charSource = new CharSource() {
						@Override
						public Reader openStream() throws IOException {
							return separatorReader;
						}
					};
					try (InputStream base64 = charSource.asByteSource(StandardCharsets.UTF_8).openStream();
							InputStream decoded = Base64.getMimeDecoder().wrap(base64);
							ZipInputStream zin = new ZipInputStream(decoded);
							) {
						zin.getNextEntry();
						entry.content = FileUtil.getStreamContent(zin);
					}
	
				}
			}
			reader.readLine();

			return entry;

		} else {
			throw new PlainFormatException("Wrong RCPTT plain format. Invalid entry header: " + entryHeader);
		}
	}

	public void close() {
		FileUtil.safeClose(reader);
		FileUtil.safeClose(in);
	}
}
