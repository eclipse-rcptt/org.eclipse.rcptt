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
package org.eclipse.rcptt.tesla.swt.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.time.Duration;

import org.eclipse.jface.util.Throttler;
import org.eclipse.rcptt.tesla.swt.events.TeslaTimerExecManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.progress.test.FakeProgressViewTarget;
import org.junit.Assume;
import org.junit.Test;

/**
 * Verifies that RCPTT recognizes and ignores the progress view update throttler
 * introduced in Eclipse 2026-09 (eclipse-platform/eclipse.platform.ui#4131),
 * which otherwise keeps rescheduling itself and slows tests down dramatically
 * (eclipse-rcptt/org.eclipse.rcptt#342).
 */
public class ProgressViewUpdateTimerTest {

	private static Display display() {
		Display display = Display.getCurrent();
		Assume.assumeTrue("A Display is required for this test", display != null);
		return display;
	}

	/**
	 * Returns the runnable the {@link Throttler} schedules through
	 * {@code Display.timerExec}, i.e. its internal {@code timerExec} lambda.
	 */
	private static Runnable scheduledRunnable(Throttler throttler) throws Exception {
		for (Field field : Throttler.class.getDeclaredFields()) {
			if (Runnable.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				return (Runnable) field.get(throttler);
			}
		}
		throw new AssertionError("Throttler has no Runnable field anymore");
	}

	@Test
	public void detectsProgressViewUpdateThrottler() throws Exception {
		Throttler throttler = new Throttler(display(), Duration.ofMillis(100), new FakeProgressViewTarget());
		assertTrue("The progress view update throttler must be ignored",
				TeslaTimerExecManager.isProgressViewUpdateTimer(scheduledRunnable(throttler)));
	}

	@Test
	public void ignoresUnrelatedThrottler() throws Exception {
		Runnable unrelated = () -> {
			// A throttler that does not belong to the progress view.
		};
		Throttler throttler = new Throttler(display(), Duration.ofMillis(100), unrelated);
		assertFalse("Only the progress view throttler must be treated specially",
				TeslaTimerExecManager.isProgressViewUpdateTimer(scheduledRunnable(throttler)));
	}

	@Test
	public void ignoresPlainRunnable() {
		assertFalse(TeslaTimerExecManager.isProgressViewUpdateTimer(new FakeProgressViewTarget()));
		assertFalse(TeslaTimerExecManager.isProgressViewUpdateTimer(() -> {
		}));
		assertFalse(TeslaTimerExecManager.isProgressViewUpdateTimer(null));
	}
}
