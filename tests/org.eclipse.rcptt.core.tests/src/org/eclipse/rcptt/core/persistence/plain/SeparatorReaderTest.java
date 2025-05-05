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

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.eclipse.rcptt.core.persistence.plain.SeparatorReader.Separator;
import org.junit.Test;

import com.google.common.io.CharStreams;

public class SeparatorReaderTest {


	@Test
	public void middle() throws IOException {
		assertValidSplit("abc", "def", "abcSEPdef");
	}
	
	@Test
	public void start() throws IOException {
		assertValidSplit("", "def", "SEPdef");
		assertValidSplit("EPdef", "", "EPdef");
	}
	
	@Test
	public void end() throws IOException {
		assertValidSplit("abc", "", "abcSEP");
		assertValidSplit("abcSE", "", "abcSE");
	}

	@Test
	public void multiple() throws IOException {
		assertValidSplit("abc", "SEPdef", "abcSEPSEPdef");
	}
	
	@Test
	public void abutted() {
		assertValidSplit("abcSE", "def", "abcSESEPdef");
	}
	
	@Test
	public void large() throws IOException {
		int prime = 7919; // Larger than Guava's buffers
		String prefix = "a".repeat(prime);
		String suffix = "a test suffix";
		assertValidSplit(prefix, suffix, prefix + "SEP" + suffix);
	}
	
	@Test
	public void fitBufferExactly() throws IOException {
		int bufSize = 10; // Larger than default SeparatorReader buffer
		String suffix = "a test suffix".repeat(20);
		for (int firstSize = 0; firstSize < bufSize; firstSize++) {
			for (int secondSize = 0; secondSize < bufSize; secondSize++) {
				for (int thirdSize = 0; thirdSize < bufSize; thirdSize++) {
					for (int readSize = 0; readSize < 5; readSize++) {
					String first = "a".repeat(firstSize);
					String second = "b".repeat(secondSize);
					String third = "c".repeat(thirdSize);
					StringReader reader = new StringReader(first + "SEP" + second + "SEP"+ third + "SEP" +suffix);
					try (
							BufferedReader breader = new BufferedReader(reader, bufSize);
							) {
						breader.read(new char[firstSize+3]);
						String message = format("%s, %s, %s", first, second, third);
						assertSegment(message,  second, breader, bufSize);
						assertSegment(message, third, breader, bufSize);
					}
				}
				}
			}
		}
	}

	private static void assertSegment(String message, String expected, BufferedReader reader, int readSize) throws IOException {
		try (SeparatorReader subject = new SeparatorReader(reader, SEPARATOR())) {
			StringBuilder sb = new StringBuilder();
			char[] b= new char[readSize];
			for (;;) {
				int c = subject.read(b);
				if (c == -1) {
					assertEquals(message, expected, sb.toString());
					break;
				}
				sb.append(b, 0, c);
			}
		}
	}

	private void assertValidSplit(String prefix, String suffix, String input) {
		StringReader reader = new StringReader(input);
		try (SeparatorReader subject = new SeparatorReader(reader, SEPARATOR())) {
			assertEquals(prefix, CharStreams.toString(subject));
			assertEquals(suffix, CharStreams.toString(reader));
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	private static Separator SEPARATOR() {
		return new Separator.Exact("SEP");
	}


}
