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
package org.eclipse.rcptt.sherlock.aspects.asyncs;

public interface IAsyncEventListener {
	// Async methods
	void asyncAdded(Runnable async, boolean sync);

	void timerAdded(Runnable async);

	void timerCanceled(Runnable timer);

	void asyncRunning(Runnable async);

	void asyncDone(Runnable async);

	Runnable processTimerProc(Runnable newRunnable);

	Runnable cancelTimerProc(Runnable runnable);
}
