/*******************************************************************************
 * Copyright (c) 2026 DSA GmbH, Aachen and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     DSA GmbH, Aachen - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.core.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rcptt.util.ReflectionUtil;
import org.junit.Test;

public class ReflectionUtilTest {
	@Test
	public void testCallMethod1() {
		List<String> test = List.of("value");
		assertEquals(1, ReflectionUtil.callMethod(test, "size"));
	}
	
	@Test
	public void testCallMethod2() {
		List<String> test = Collections.unmodifiableList(List.of("value"));
		assertEquals(1, ReflectionUtil.callMethod(test, "size"));
	}

	@Test
	public void testFindMethod1() {
		assertNotNull(ReflectionUtil.findMethod(List.class, "size"));
	}

	@Test
	public void testFindMethod2() {
		List<String> test = Collections.unmodifiableList(List.of("value"));
		assertNotNull(ReflectionUtil.findMethod(test.getClass(), "size"));
	}

	@Test
	public void testGetField() {
		Path p = new Path("");
		assertEquals(IPath.SEPARATOR, ReflectionUtil.getField(p, "SEPARATOR"));
	}

	@Test
	public void testFindField() {
		assertNotNull(ReflectionUtil.findField(IFile.class, "DEPTH_INFINITE"));
	}
}
