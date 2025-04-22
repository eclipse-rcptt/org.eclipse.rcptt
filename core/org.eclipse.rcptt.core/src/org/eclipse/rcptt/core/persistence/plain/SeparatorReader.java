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


/** Reads a markable stream until terminator
 *  Terminator is consumed.
 * **/
public class SeparatorReader extends  java.io.Reader {
	private final String terminator;
	private final Reader reader;
	private final StringBuilder tail = new StringBuilder();
	private char[] tempBuffer = new char[5];

	public SeparatorReader(Reader reader, String separator) {
		this.terminator = separator;
		this.reader = reader;
		if (terminator.length() == 0) {
			throw new IllegalArgumentException("Empty separator");
		}
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
		int position = tail.indexOf(terminator);
		if (position >= 0) {
			reader.reset();
			int read = reader.read(tempBuffer, 0, position + terminator.length() - oldTail);
			assert read == position + terminator.length();
		}
		return readTail(cbuf, off, len);
	}
	
	private int readTail(char[] cbuf, int off, int len) {
		if (tail.length() >= terminator.length()) {
			int position = tail.indexOf(terminator);
			if (position == 0) {
				return -1;
			} else if (position > 0) {
				len = Math.min(position, len);
				return cutTail(cbuf, off, len);
			} else {
				len = Math.min(len, tail.length() - terminator.length());
				return cutTail(cbuf, off, len);
			}
		}
		return 0;
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
