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
package org.eclipse.rcptt.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * NOTE: this class is for internal use only.
 */
public final class StringUtils {

	public static String junitStyleDiff(String actual, String expected) {
		return new ComparisonCompactor(20, expected, actual).compact();
	}

	private static byte[] safeGetBytes(String data, Charset encoding) {
		return data.getBytes(encoding);
	}

	public static byte[] getUtf8Bytes(String s) {
		return safeGetBytes(s, StandardCharsets.UTF_8);
	}

	public static byte[] getAscii7Bytes(String s) {
		return safeGetBytes(s, StandardCharsets.US_ASCII);
	}

	public static boolean isBlank(String string) {
		// TODO: there is no reason to create new string on trimming just to
		// check it is empty, but who cares
		return string == null || string.length() == 0
				|| string.trim().length() == 0;
	}

	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public static String safeToString(Object object) {
		return object == null ? null : object.toString();
	}

	public static String join(char sep, Iterable<String> strings) {
		return join(Character.toString(sep), strings);
	}

	public static String join(char sep, Iterator<String> strings) {
		return join(Character.toString(sep), strings);
	}

	public static String join(String sep, Iterable<String> strings) {
		return join(sep, strings.iterator());
	}

	public static String join(String sep, Iterator<String> strings) {
		StringBuilder sb = new StringBuilder();
		while (strings.hasNext()) {
			sb.append(strings.next());
			sb.append(sep);
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - sep.length());
		}
		return sb.toString();
	}

	public static List<String> split(char sep, String input) {
		return split(Character.toString(sep), input);
	}

	public static List<String> split(String sep, String input) {
		List<String> result = new ArrayList<String>();
		int start = 0;
		int nextSep;
		int sepLength = sep.length();
		while ((nextSep = input.indexOf(sep, start)) != -1) {
			result.add(input.substring(start, nextSep));
			start = nextSep + sepLength;
		}
		result.add(input.substring(start));
		return result;
	}

	/**
	 * not a real glob -- supports only * (any number of any chars)
	 * and ? (exactly one any char)
	 */
	public static String globToRegex(String glob) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < glob.length(); ++i)
		{
			final char c = glob.charAt(i);
			switch (c)
			{
			case '*':
				sb.append(".*");
				break;
			case '?':
				sb.append('.');
				break;
			case '.':
			case '^':
			case '$':
			case '+':
			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '\\':
			case '|':
				sb.append('\\').append(c);
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static int getAttrLastSplitterInd(String attrName) {
		int ind = attrName.length() - 1;
		boolean inBracket = false;
		boolean considerQuote = false;
		while (ind >= 0) {
			if (inBracket) {
				if (attrName.charAt(ind) == '['
						&& (!considerQuote || attrName.charAt(ind + 1) == '\'')) {
					inBracket = false;
				}
			} else if (attrName.charAt(ind) == '.') {
				return ind;
			} else if (attrName.charAt(ind) == ']') {
				inBracket = true;
				if (ind > 0 && attrName.charAt(ind - 1) == '\'') {
					considerQuote = true;
				}
			}
			ind--;
		}
		return ind;
	}
	
	  /**
	 * Returns the given string if it is non-null; the empty string otherwise.
	 * This method has been taken from Google Guava library.
	 * 
	 * @param string
	 *            the string to test and possibly return
	 * @return {@code string} itself if it is non-null; {@code ""} if it is null
	 */
	  public static String nullToEmpty(String string) {
	    return (string == null) ? "" : string;
	  }

}
