/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.model.ModelInfo;
import org.eclipse.rcptt.internal.core.model.Openable;
import org.osgi.framework.FrameworkUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class ModelCache {
	private static final ILog LOG = Platform.getLog(FrameworkUtil.getBundle(ModelCache.class));
	public static final int DEFAULT_PROJECT_SIZE = 5; // average 25552 bytes
	// public static final int DEFAULT_ROOT_SIZE = 50; // average 2590 bytes per

	// public static final int DEFAULT_PKG_SIZE = 500; // average 1782 bytes per

	public static final int DEFAULT_OPENABLE_SIZE = 10000; // average 6629

	public static final int DEFAULT_CHILDREN_SIZE = 100 * 20; // average 20

	protected ModelInfo modelInfo;

	private final Cache<IQ7Element, Object> openableCache;

	public ModelCache() {
		RemovalListener<IQ7Element, Object> removalListener = new RemovalListener<>() {
			@Override
			public void onRemoval(RemovalNotification<IQ7Element, Object> notification) {
				Object value = notification.getValue();
				if (value instanceof Closeable) {
					try {
						((Closeable) value).close();
					} catch (IOException e) {
						LOG.log(error(e));
					}
				}
				IQ7Element key = notification.getKey();
				if (key instanceof Openable) {
					try {
						((Openable) key).close();
					} catch (ModelException e) {
						LOG.log(error(e));
					}
				}
			}

		};
		openableCache  = CacheBuilder.newBuilder().softValues().expireAfterAccess(10, TimeUnit.MINUTES).removalListener(removalListener).build();
	}

	/**
	 * Returns the info for the element.
	 */
	public <T> T getInfo(IQ7Element element, Class<T> clazz, Supplier<T> factory) {
		try {
			return clazz.cast(this.openableCache.get(element, () -> factory.get()));
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			}
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Returns the info for this element without disturbing the cache ordering.
	 */
	public Object peekAtInfo(IQ7Element element) {
		return this.openableCache.getIfPresent(element);
	}

	public void removeInfo(IQ7Element element) {
		this.openableCache.invalidate(element);
	}
	
	private IStatus error(Exception e) {
		if (e instanceof CoreException) {
			return new MultiStatus(LOG.getBundle().getSymbolicName(), 0, new IStatus[] {((CoreException) e).getStatus()}, e.getMessage(), e);
		}
		return new Status(IStatus.ERROR, LOG.getBundle().getSymbolicName(), 0, e.getMessage(), e);
	}

}
