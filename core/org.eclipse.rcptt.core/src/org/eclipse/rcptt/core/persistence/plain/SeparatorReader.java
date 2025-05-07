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

import static java.util.Arrays.stream;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


/** Reads a markable stream until terminator
 *  Terminator is consumed.
 * **/
public class SeparatorReader extends java.io.Reader {
	private static final boolean DEBUG = false;
	private final Separator terminator;
	private final Reader reader;
	private final StringBuilder tail = new StringBuilder();
	private char[] tempBuffer = new char[5];
	private boolean done = false;
	private final int term_length;
	
	public static final class Match {
		public final int start, end;
		public Match(int start, int end) {
			this.start=start;
			this.end = end;
			if (start >= end) {
				throw new IllegalArgumentException(start + " >= " + end);
			}
		}
	}
	/** Stateful detector of a delimiter in a stream */
	interface Separator {
		/** @return a position of delimiter start. Negative if delimiter started before current input.*/
		Optional<Match> accept(char[] input, int len);
		int length();
		public static final class Any implements Separator {
			private final Separator[] delegates;
			public Any(Separator ... delegates) {
				this.delegates = Arrays.copyOf(delegates, delegates.length);
				if (this.delegates.length == 0) {
					throw new IllegalArgumentException("Can't be empty");
				}
				if (stream(this.delegates).anyMatch(Objects::isNull)) {
					throw new NullPointerException();
				}
			}

			@Override
			public Optional<Match> accept(char[] input, int len) {
				return stream(delegates)
					.map(d -> d.accept(input, len))
					.flatMap(m -> m.map(Stream::of).orElse(Stream.empty()))
					.min(Comparator.comparingInt(m -> m.start));
			}
			
			@Override
			public int length() {
				return stream(delegates).mapToInt(Separator::length).max().orElseThrow(() -> new AssertionError());
			}
		}

		public static final class Exact implements Separator {
			private final char[] separator;
			private int position = 0;

			public Exact(String string) {
				this.separator = string.toCharArray();
				if (string.isEmpty()) {
					throw new IllegalArgumentException("Separator can't be empty");
				}
			}

			@Override
			public Optional<Match> accept(char[] input, int len) {
				if (position >= separator.length) {
					throw new IllegalStateException("Can't operate in COMPLETE state");
				}
				for (int i = 0; i < len; i++ ) {
					if (input[i] == separator[position]) {
						position++;
						if (position >= separator.length) {
							return Optional.of(new Match(i - position + 1, i + 1));
						}
					} else {
						position = input[i] == separator[0] ? 1 : 0;
					}
				}
				return Optional.empty();
			}

			@Override
			public int length() {
				return separator.length;
			}
		}
	}


	public SeparatorReader(Reader reader, Separator separator) {
		this.terminator = Objects.requireNonNull(separator);
		this.term_length = terminator.length();
		if (term_length <=0) {
			throw new IllegalArgumentException("Delimiter can't be empty");
		}
		this.reader = reader;
		if (!reader.markSupported()) {
			throw new IllegalArgumentException("mark() support is required");
		}
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		for (;;) {
			if (done) {
				return consumeTail(cbuf, off, len);
			}
			
			if (tail.length() > term_length) {
				len = Math.min(len, tail.length() - terminator.length());
				return consumeTail(cbuf, off, len);
			}
			
			if (tempBuffer.length < len) {
				tempBuffer = new char[len];
			}
			
			reader.mark(tempBuffer.length+1);
			int result = reader.read(tempBuffer, 0, tempBuffer.length);
			if (result < 0) {
				done = true;
				return consumeTail(cbuf, off, len);
			} 
			Optional<Match> position = terminator.accept(tempBuffer, result);
			if (position.isPresent()) {
				done = true;
				result = Math.min(position.get().start, result);
			}
			assert tail.length() + result >= 0;
			assert result > -terminator.length();
			if (result > 0) {
				tail.append(tempBuffer, 0, result);
			} else if (result < 0) {
				tail.delete(tail.length() + result, tail.length());
			}
			if (done) {
				reader.reset();
				long read = reader.skip(position.get().end);
				assert read == position.get().end;
				if (DEBUG) {
					reader.mark(tempBuffer.length+1);
					result = reader.read(tempBuffer, 0, Math.min(tempBuffer.length, 150));
					reader.reset();
					if (result > 0) {
						System.out.println("After segment: " + new String(tempBuffer, 0, result));
					}
				}
				return consumeTail(cbuf, off, len);
			}
		}
	}

	private int consumeTail(char[] cbuf, int off, int len) {
		assert len > 0;
		if (tail.length() == 0) {
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
