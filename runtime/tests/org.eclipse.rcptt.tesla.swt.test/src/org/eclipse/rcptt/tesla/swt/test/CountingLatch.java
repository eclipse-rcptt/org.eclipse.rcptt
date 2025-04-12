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

package org.eclipse.rcptt.tesla.swt.test;

import java.io.Closeable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class CountingLatch implements Closeable {
	private final CountDownLatch release = new CountDownLatch(1);
	private final Semaphore counter = new Semaphore(0);
	
	public void await() throws InterruptedException {
		counter.release();
		release.await();
	}
	
	public void awaitParties(int count) throws InterruptedException {
		while(!counter.tryAcquire(count, 1, TimeUnit.SECONDS)) {
			if (release.await(0, TimeUnit.MILLISECONDS)) {
				throw new InterruptedException();
			}
		}
	}
	
	@Override
	public void close() {
		release.countDown();
	}
}
