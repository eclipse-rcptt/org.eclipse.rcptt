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
package org.eclipse.rcptt.core.persistence;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Prints an error if resource is garbage collected without being disposed.
 *  <p>
 *  Usage:
 *  <pre>
 *  class Resource implements Closeable {
 *    private final Runnable onClose = LeakDetector.INSTANCE.register(this);
 *    ...
 *    &#64;Override
 *    public close() {
 *      // actual disposal
 *      ...
 *      // Removing GC monitoring
 *      onClose.run(); 
 *    }
 *  }
 *  </pre>
 * */
public final class LeakDetector {
	public static final LeakDetector INSTANCE = new LeakDetector();
	
	/** @return - if not invoked, an exception is logged */
	public Runnable register(Object shouldBeClosed) {
		Handle handle = new Handle(shouldBeClosed.toString());
		cleaner.register(shouldBeClosed, handle);
		return handle::close;
	}
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(LeakDetector.class);
	private static final ILog LOG = Platform.getLog(BUNDLE);
	private final Cleaner cleaner = Cleaner.create();	
	
	private static class Handle implements Runnable {
		private final AtomicBoolean closed = new AtomicBoolean(false);
		private final Exception allocationTrace;

		public Handle(String name) {
			allocationTrace = new RuntimeException(name + " allocated here is not closed.");
		}

		public void close() {
			closed.set(true);
		}

		@Override
		public void run() {
			if (!closed.get()) {
				LOG.log(new Status(IStatus.ERROR, BUNDLE.getSymbolicName(), "", allocationTrace));
			}
		}
		
	}

	private LeakDetector() {
		
	}
}
