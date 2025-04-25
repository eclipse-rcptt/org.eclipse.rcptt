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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface IPlainConstants {
	public static final String ENCODING = "utf-8";
	public static final Charset ENCODING_OBJECT = StandardCharsets.UTF_8;
	public static final int STRIP_LEN = 80;
	public static final String NODE_POSTFIX = "--";
	public static final String ATTR_ENTRY_NAME = "Entry-Name";
	public static final String NODE_PREFIX = "------=_";
	public static final String FORMAT_VERSION = "1.0";
	public static final String ATTR_FORMAT_VERSION = "Format-Version";
	public static final String PLAIN_HEADER = "--- RCPTT testcase ---";
	public static final String PLAIN_METADATA = "--- RCPTT project settings ---";
	public static final String PLAIN_VERIFICATION = "--- RCPTT verification ---";
	public static final String LEGACY_PLAIN_HEADER = "--- Q7 testcase ---";
	public static final String LEGACY_PLAIN_METADATA = "--- Q7 project settings ---";
	public static final String LEGACY_PLAIN_VERIFICATION = "--- Q7 verification ---";

	public static final String ATTR_CONTENT_TYPE = "Content-Type";
}