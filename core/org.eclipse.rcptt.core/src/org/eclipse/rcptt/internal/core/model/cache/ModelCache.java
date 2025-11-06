/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.internal.core.model.ModelInfo;
import org.eclipse.rcptt.internal.core.model.Q7ResourceInfo;
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class ModelCache {
	private static final int TIMEOUT_MS = 100_000;
	private static final ILog LOG = Platform.getLog(FrameworkUtil.getBundle(ModelCache.class));

	protected ModelInfo modelInfo;

	private final Cache<IQ7Element, Object> openableCache;

	public ModelCache(long approxSizeInBytes) {
		RemovalListener<IQ7Element, Object> removalListener = new RemovalListener<>() {
			@Override
			public void onRemoval(RemovalNotification<IQ7Element, Object> notification) {
				Object value = notification.getValue();
				closeValue(value);
			}
		};
		Weigher<IQ7Element, Object> weigher = (ignored, info) -> {
			if (info instanceof Q7ResourceInfo) {
				return ((Q7ResourceInfo) info).getCacheFootprint();
			}
			return 1000;
		};
		openableCache  = CacheBuilder.newBuilder().weigher(weigher).maximumWeight(approxSizeInBytes).expireAfterAccess(10, TimeUnit.MINUTES).removalListener(removalListener).build();
	}

	private final ValueLock locks = new ValueLock();
	/**
	 * Returns the info for the element.
	 */
	@SuppressWarnings("unchecked")
	public <T, V> V accessInfo(IQ7Element element, Class<T> clazz, Supplier<T> infoFactory, Function<T, V> infoToValue) throws InterruptedException {
		try {
			return locks.exclusively(element, TIMEOUT_MS, () -> {
				try {
					Object[] result = new Object[1]; // info2 may be immediately deallocated, compute the value while it lasts
					boolean[] found = new boolean[] {false};
					T info = clazz.cast(this.openableCache.get(element, () -> {
						T info3 = infoFactory.get();
						try {
							T info2 = clazz.cast(info3);
							found[0] = true;
							result[0] = infoToValue.apply(info2); 
							return info2;
						} catch (Throwable e) {
							closeValue(info3);
							throw e;
						}
					}));
					if (found[0]) {
						return (V)result[0];
					}
					// info was not allocated now, using old value
					return infoToValue.apply(info);
				} catch (ExecutionException e) {
					Throwable cause = e.getCause();
					Throwables.throwIfUnchecked(cause);
					throw new IllegalStateException(e);
				}
			});
		} catch (TimeoutException e) {
			throw new IllegalStateException(e);
		} catch (UncheckedExecutionException e) {
			Throwables.throwIfInstanceOf(e.getCause(), InterruptedException.class);
			Throwables.throwIfUnchecked(e.getCause());
			throw e;
		}
	}

	/**
	 * Returns the info for this element without disturbing the cache ordering.
	 * @return Optional.empty() when cache is busy, has no matching key, or infoToValue returns null, wraps infoToValue result otherwise
	 */
	public <T, V> Optional<V> peekInfo(IQ7Element element, Class<T> clazz, Function<T, V> infoToValue) {
		try {
			return locks.<Optional<V>>exclusively(element, 10, () -> {
				return Optional.ofNullable(this.openableCache.getIfPresent(element)).map(clazz::cast).map(infoToValue);
			});
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		} catch (TimeoutException e) {
			return Optional.empty();
		}
	}

	public void removeInfo(IQ7Element element) throws InterruptedException {
		try {
			locks.exclusively(element, TIMEOUT_MS, () -> {
				this.openableCache.invalidate(element);
				return null;
			});
		} catch (TimeoutException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private IStatus error(Exception e) {
		if (e instanceof CoreException) {
			return new MultiStatus(LOG.getBundle().getSymbolicName(), 0, new IStatus[] {((CoreException) e).getStatus()}, e.getMessage(), e);
		}
		return new Status(IStatus.ERROR, LOG.getBundle().getSymbolicName(), 0, e.getMessage(), e);
	}

	private void closeValue(Object value) {
		if (value instanceof Closeable) {
			try {
				((Closeable) value).close();
			} catch (IOException e) {
				LOG.log(error(e));
			}
		}
	}

}
