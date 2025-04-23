/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.core.persistence.plain;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;


/** Reads a markable stream until terminator
 *  Terminator is consumed.
 * **/
public class SeparatorReader extends java.io.Reader {
	private static final boolean DEBUG = false;
	private final Separator terminator;
	private final Reader reader;
	private final StringBuilder tail = new StringBuilder();
	private char[] tempBuffer = new char[5];
	
	public static class Match {
		public final int start, end;
		public Match(int start, int end) {
			this.start = start;
			this.end = end;
			if (start < 0 || end <= start) {
				throw new IllegalArgumentException();
			}
		}
	}
	
	interface Separator {
		int length();
		Optional<Match> find(CharSequence input);
		
		public class WithSuffix implements Separator {
			private final Separator separator;
			private final Collection<String> suffixes;
			private final int length;
			public WithSuffix(Separator separator, Collection<String> suffixes) {
				this.suffixes = new ArrayList<>(suffixes);
				this.separator = Objects.requireNonNull(separator);
				this.length = separator.length() + suffixes.stream().mapToInt(String::length).max().orElseThrow(() -> new IllegalArgumentException("suffixes can't be empty"));
			}
			@Override
			public Optional<Match> find(CharSequence input) {
				return separator.find(input).flatMap(match -> {
					CharSequence actualSuffix = input.subSequence(match.end, input.length());
					return suffixes.stream().filter(s -> startsWith(actualSuffix, s)).findFirst().map(s -> new Match(match.start, match.end + s.length()));
				});
			}
			@Override
			public int length() {
				return length;
			}
		}
		
		public class Exact implements Separator {
			private String separator;

			public Exact(String string) {
				this.separator = string;
				if (string.isEmpty()) {
					throw new IllegalArgumentException("Separator can't be empty");
				}
			}

			@Override
			public int length() {
				return separator.length();
			}

			@Override
			public Optional<Match> find(CharSequence input) {
				// TODO optimize?
				int position = input.toString().indexOf(separator);
				if (position < 0) {
					return Optional.empty();
				}
				return Optional.of(new Match(position, position+separator.length()));
			}
			
		}
	}
	
	private static boolean startsWith(CharSequence input, String prefix) {
		for (int i = 0; i < input.length() && i < prefix.length(); i++) {
			if (input.charAt(i) != prefix.charAt(i)) {
				return false;
			}
		}
		return true;
	}
	

	public SeparatorReader(Reader reader, Separator separator) {
		this.terminator = Objects.requireNonNull(separator);
		this.reader = reader;
		if (!reader.markSupported()) {
			throw new IllegalArgumentException("mark() support is required");
		}
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int result = readTail(cbuf, off, len);
		if (result != 0) {
			return result;
		}
		if (tempBuffer.length < len) {
			tempBuffer = new char[len];
		}
		reader.mark(tempBuffer.length+1);
		result = reader.read(tempBuffer, 0, tempBuffer.length);
		if (result < 0) {
			return cutTail(cbuf, off, len);
		} 
		int oldTail = tail.length();
		tail.append(tempBuffer, 0, result);
		Match match = terminator.find(tail).orElse(null);
		if (match != null) {
			reader.reset();
			long read = reader.skip(match.end - oldTail);
			assert read == match.end - oldTail;
			if (DEBUG) {
				reader.mark(tempBuffer.length+1);
				result = reader.read(tempBuffer, 0, Math.min(tempBuffer.length, 150));
				reader.reset();
				if (result > 0) {
					System.out.println("After segment: " + new String(tempBuffer, 0, result));
				}
			}
		}
		return readTail(cbuf, off, len);
	}
	
	private int readTail(char[] cbuf, int off, int len) {
		Match match = terminator.find(tail).orElse(null);
		if (match == null) {
			len = Math.min(len, tail.length() - terminator.length());
			if (len <= 0) {
				return 0;
			}
			return cutTail(cbuf, off, len);
		}
		if (match.start == 0) {
			return -1;
		}
		len = Math.min(match.start, len);
		return cutTail(cbuf, off, len);
	}

	private int cutTail(char[] cbuf, int off, int len) {
		assert len > 0;
		if (tail.isEmpty()) {
			return -1;
		}
		len = Math.min(tail.length(), len);
		tail.getChars(0, len, cbuf, off);
		tail.delete(0, len);
		return len;
	}

	@Override
	public final void close() throws IOException {
		// Parent is not supposed to be closed.
		// A next segment is potentially following this one.
	}
	
}
