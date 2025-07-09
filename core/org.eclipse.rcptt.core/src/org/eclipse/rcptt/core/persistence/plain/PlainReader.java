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

import static java.util.Arrays.asList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FilterReader;
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
import java.util.Objects;
import java.util.zip.ZipInputStream;

import org.eclipse.rcptt.core.persistence.plain.SeparatorReader.Separator;
import org.eclipse.rcptt.util.FileUtil;

import com.google.common.io.BaseEncoding;
import com.google.common.io.CharSource;

public class PlainReader implements IPlainConstants, Closeable {
	private BufferedReader reader;
	private final InputStream in;
	public Reader currentSegment;

	public static class Entry {
		public final String name;
		private final CharSource segment;
		public final Map<String, String> attributes;

		public Entry(String name, Map<String, String> attributes, CharSource segment) {
			super();
			this.name = Objects.requireNonNull(name);
			this.attributes = Objects.requireNonNull(attributes);
			this.segment = Objects.requireNonNull(segment);
		}

		public InputStream getContent() throws IOException {
			String contentType = attributes.get(ATTR_CONTENT_TYPE);
			if (contentType != null && contentType.contains("text")) {
				// Text mode content, remove trailing new line 
				String content = segment.read();
				// Normalize EOL. Yes, this is not an obvious design decision.
				// The idea is probably to use EOLs specific to target like workspace resource or OS
				content = content.replaceAll("\r\n", "\n");
				String suffix = "\n";
				if (content.endsWith(suffix)) {
					content = content.substring(0, content.length() - suffix.length());
				}
				return new ByteArrayInputStream(content.getBytes(ENCODING_OBJECT));
			} else if (contentType != null && contentType.contains("binary")) {
				// Zipped, base64 encoded content, may be very large
				InputStream segmentBytes = segment.asByteSource(ENCODING_OBJECT).openStream();
				InputStream decoded = Base64.getMimeDecoder().wrap(segmentBytes);
				ZipInputStream zin = new ZipInputStream(decoded);
				zin.getNextEntry();
				return zin;
			}
			throw new PlainFormatException("Entry " + name + " has unknown content type");
		}
		
		@Override
		public String toString() {
			return name;
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
		if (currentSegment != null) {
			currentSegment.skip(Long.MAX_VALUE);
			currentSegment.close();
			currentSegment = null;
		}
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
			Map<String, String> attributes = readAttributes();
			String name = attributes.get(ATTR_ENTRY_NAME);
			SeparatorReader separatorReader = new SeparatorReader(reader,
					new Separator.Any(new Separator.Exact(entryHeader + NODE_POSTFIX + "\n"),
							new Separator.Exact(entryHeader + NODE_POSTFIX + "\r\n")));
			currentSegment = separatorReader;
			CharSource charSource = new CharSource() {
				@Override
				public Reader openStream() throws IOException {
					return new FilterReader(separatorReader) {
						@Override
						public void close() throws IOException {
							// Do not close, next entry still needs to skip the rest of current segment
						}
					};
				}
			};
			return new Entry(name, attributes, charSource);
		} else {
			throw new PlainFormatException("Wrong RCPTT plain format. Invalid entry header: " + entryHeader);
		}
	}

	@Override
	public void close() {
		FileUtil.safeClose(reader);
		FileUtil.safeClose(in);
	}
}
