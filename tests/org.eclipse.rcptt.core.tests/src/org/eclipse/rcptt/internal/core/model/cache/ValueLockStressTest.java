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
package org.eclipse.rcptt.internal.core.model.cache;

import static java.lang.System.currentTimeMillis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.rcptt.core.tests.Stress;
import org.junit.Assert;
import org.junit.Test;

/**
 * Concurrency properties of {@link ValueLock} without Eclipse runtime
 * involvement. See https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/176
 */
public class ValueLockStressTest {
	private static final int KEYS = 4;
	private static final int THREADS = 8;

	@Test(timeout = 60_000)
	public void mutualExclusionPerKey() throws InterruptedException, ExecutionException {
		ValueLock subject = new ValueLock();
		AtomicInteger[] holders = new AtomicInteger[KEYS];
		for (int i = 0; i < KEYS; i++) {
			holders[i] = new AtomicInteger();
		}
		AtomicLong iterations = new AtomicLong();
		long stop = currentTimeMillis() + Stress.millis(2000);
		List<CompletableFuture<Void>> workers = new ArrayList<>();
		for (int t = 0; t < THREADS; t++) {
			workers.add(CompletableFuture.runAsync(() -> {
				while (currentTimeMillis() < stop) {
					int key = ThreadLocalRandom.current().nextInt(KEYS);
					try {
						subject.exclusively(Integer.valueOf(key), 10_000, () -> {
							Assert.assertEquals("Key " + key + " is held by another thread", 1,
									holders[key].incrementAndGet());
							Thread.yield();
							holders[key].decrementAndGet();
							return null;
						});
					} catch (InterruptedException | TimeoutException e) {
						throw new AssertionError(e);
					}
					iterations.incrementAndGet();
				}
			}, runner()));
		}
		for (CompletableFuture<Void> worker : workers) {
			worker.get();
		}
		Assert.assertTrue(iterations.get() > 0);
	}

	@Test(timeout = 60_000)
	public void unrelatedKeyIsNotBlocked() throws Exception {
		ValueLock subject = new ValueLock();
		CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
		CompletableFuture<Void> holder = CompletableFuture.runAsync(() -> {
			try {
				subject.exclusively("A", 10_000, () -> {
					entered.countDown();
					try {
						release.await();
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					}
					return null;
				});
			} catch (InterruptedException | TimeoutException e) {
				throw new AssertionError(e);
			}
		}, runner());
		try {
			entered.await();
			long start = currentTimeMillis();
			subject.exclusively("B", 10_000, () -> null);
			long elapsed = currentTimeMillis() - start;
			Assert.assertTrue("Unrelated key was blocked for " + elapsed + " ms", elapsed < 1000);
		} finally {
			release.countDown();
			holder.get();
		}
	}

	@Test(timeout = 60_000)
	public void interruptedWaiterDoesNotAcquireOrReleaseLock() throws Exception {
		ValueLock subject = new ValueLock();
		CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
		AtomicInteger holders = new AtomicInteger();
		CompletableFuture<Void> holder = CompletableFuture.runAsync(() -> {
			try {
				subject.exclusively("A", 10_000, () -> {
					holders.incrementAndGet();
					entered.countDown();
					try {
						release.await();
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					}
					holders.decrementAndGet();
					return null;
				});
			} catch (InterruptedException | TimeoutException e) {
				throw new AssertionError(e);
			}
		}, runner());
		try {
			entered.await();
			AtomicReference<Throwable> waiterFailure = new AtomicReference<>();
			Thread waiter = new Thread(() -> {
				try {
					subject.exclusively("A", 30_000, () -> {
						throw new AssertionError("Lock should not be acquired");
					});
					waiterFailure.set(new AssertionError("InterruptedException is expected"));
				} catch (InterruptedException e) {
					// expected
				} catch (Throwable e) {
					waiterFailure.set(e);
				}
			});
			waiter.start();
			Thread.sleep(100);
			waiter.interrupt();
			waiter.join(10_000);
			Assert.assertFalse(waiter.isAlive());
			if (waiterFailure.get() != null) {
				throw new AssertionError(waiterFailure.get());
			}
			try {
				subject.exclusively("A", 10, () -> null);
				Assert.fail("Lock should still be held by the first thread");
			} catch (TimeoutException e) {
				// expected
			}
			Assert.assertEquals(1, holders.get());
		} finally {
			release.countDown();
			holder.get();
		}
		subject.exclusively("A", 10, () -> null); // released
	}

	private static java.util.concurrent.Executor runner() {
		return command -> {
			Thread thread = new Thread(command, ValueLockStressTest.class.getSimpleName());
			thread.setDaemon(true);
			thread.start();
		};
	}
}
