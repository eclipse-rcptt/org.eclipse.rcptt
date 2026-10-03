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
package org.eclipse.ui.internal.progress.test;

/**
 * Test helper that stands in for {@code ProgressManager::notifyListeners}. It
 * lives in a sub-package of {@code org.eclipse.ui.internal.progress} so that its
 * class name is recognized by
 * {@code TeslaTimerExecManager.isProgressViewUpdateTimer} (which matches the
 * {@code org.eclipse.ui.internal.progress} package), mirroring the real progress
 * view update throttler without colliding with the real workbench package.
 */
public class FakeProgressViewTarget implements Runnable {
	@Override
	public void run() {
		// Nothing: only the class name/package matters for the detection test.
	}
}
