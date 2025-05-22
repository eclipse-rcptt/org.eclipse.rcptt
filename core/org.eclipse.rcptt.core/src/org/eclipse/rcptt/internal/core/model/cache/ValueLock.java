/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.internal.core.model.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

public final class ValueLock {
	private final Set<Object> lockedKeys = new HashSet<>();

	private void lock(Object key, int timeout_ms) throws InterruptedException, TimeoutException {
		Preconditions.checkArgument(timeout_ms >= 0);
		long stop = System.currentTimeMillis() + timeout_ms;
		boolean timeout = false;
		synchronized (lockedKeys) {
			while (!lockedKeys.add(key)) {
				long timeLeft = stop - System.currentTimeMillis();
				if (timeLeft <= 0) {
					timeout = true;
					break;
				}
				lockedKeys.wait(timeLeft);
			}
		}
		if (timeout) {
			throw new TimeoutException("Timeout while locking " + key.getClass().getName());
		}
	}

	private void unlock(Object key) {
		synchronized (lockedKeys) {
			lockedKeys.remove(key);
			lockedKeys.notifyAll();
		}
	}

	public <V> V exclusively(Object key, int timeout_ms,  Supplier<V> supplier) throws InterruptedException, TimeoutException {
		lock(key, timeout_ms);
		try {
			return supplier.get();
		} finally {
			unlock(key);
		}
	}
}
