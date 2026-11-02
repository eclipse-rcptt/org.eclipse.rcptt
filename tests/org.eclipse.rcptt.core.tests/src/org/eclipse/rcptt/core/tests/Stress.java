/*******************************************************************************
 * Copyright (c) 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.core.tests;

import org.junit.Assume;

/**
 * Knobs for concurrency reproducers. All are controlled with system properties
 * of the test JVM (see pom.xml and README.md of this bundle).
 */
public final class Stress {
	/**
	 * Enables tests that are known to fail because of
	 * https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176
	 */
	public static final String ISSUE_176_PROPERTY = "rcptt.issue176";
	/** Multiplier for durations and iteration counts of time-boxed tests. */
	public static final String FACTOR_PROPERTY = "rcptt.stress.factor";

	public static final boolean ISSUE_176 = Boolean.getBoolean(ISSUE_176_PROPERTY);
	public static final double FACTOR = parseFactor(System.getProperty(FACTOR_PROPERTY));

	private Stress() {
	}

	/**
	 * Skips current test unless known failures of
	 * https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176 are
	 * requested with -Drcptt.issue176=true
	 */
	public static void assumeIssue176Enabled() {
		Assume.assumeTrue("Known failure https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176. Enable with -D"
				+ ISSUE_176_PROPERTY + "=true", ISSUE_176);
	}

	/** Duration of a time-boxed loop, scaled by -Drcptt.stress.factor */
	public static long millis(long baseMillis) {
		return Math.max(1, Math.round(baseMillis * FACTOR));
	}

	/** Iteration count of a loop, scaled by -Drcptt.stress.factor */
	public static int iterations(int baseIterations) {
		return (int) Math.max(1, Math.min(Integer.MAX_VALUE, Math.round(baseIterations * FACTOR)));
	}

	private static double parseFactor(String value) {
		if (value == null || value.isBlank()) {
			return 1;
		}
		double result = Double.parseDouble(value.trim());
		if (!(result > 0)) {
			throw new IllegalArgumentException(FACTOR_PROPERTY + " should be positive, but is " + value);
		}
		return result;
	}
}
