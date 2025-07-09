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
package org.eclipse.rcptt.internal.core.model.cache;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

public class ValueLockTest {

	@Test(timeout=10000)
	public void doNotRelaseExistingLockOnTimeout() throws IOException, InterruptedException, TimeoutException {
		Object key = new Object();
		var subject = new ValueLock();
		try (Closeable hold = hold(subject, key)) {
			try {
				subject.exclusively(key, 10, () -> null);
				Assert.fail("TimeoutException is expected");
			} catch (TimeoutException e) {
				// expected
			}
			try {
				subject.exclusively(key, 10, () -> null);
				Assert.fail("TimeoutException is expected");
			} catch (TimeoutException e) {
				// expected
			}
		}
		subject.exclusively(key, 10, () -> null); // Does not throw
	}
	
	private static Closeable hold(ValueLock subject, Object key) {
		CountDownLatch enter = new CountDownLatch(1), exit = new CountDownLatch(1);
		CompletableFuture<Void> result = CompletableFuture.runAsync(() -> {
			try {
				subject.exclusively(key, 10, () -> {
					enter.countDown();
					try {
						exit.await();
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					}
					return null;
				});
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError(e);
			} catch (TimeoutException e) {
				throw new AssertionError(e);
			}
		});
		try {
			enter.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError(e);
		}
		return () -> {
			exit.countDown();
			result.join();
		};
	}

}
