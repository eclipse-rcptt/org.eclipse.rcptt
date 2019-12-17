/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.util;

public class ObjectUtil {
	public static <T> T firstNotNull(T... args) {
		for (T arg : args) {
			if (arg != null) {
				return arg;
			}
		}
		throw new NullPointerException();
	}

	public static boolean equal(Object a, Object b) {
		return a == b || (a != null && a.equals(b));
	}
}
