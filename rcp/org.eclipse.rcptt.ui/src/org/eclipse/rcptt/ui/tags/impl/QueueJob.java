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
package org.eclipse.rcptt.ui.tags.impl;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

abstract class QueueJob<T> extends Job {
	public interface IQueue<T> {
		T poll();
		int estimateSize();
		void push(T item);
		void clear();
	}
	
	public static final class UniqueQueue<T> implements IQueue<T> {
		private final LinkedHashSet<T> queue = new LinkedHashSet<T>();

		@Override
		public synchronized T poll() {
			Iterator<T> i = queue.iterator();
			if (i.hasNext()) {
				try {
					return i.next();
				} finally {
					i.remove();
				}
			}
			return null;
		}

		@Override
		public synchronized int estimateSize() {
			return queue.size();
		}

		@Override
		public synchronized void push(T item) {
			queue.add(item);
		}

		@Override
		public synchronized void clear() {
			queue.clear();
		}
		
	}
	
	public QueueJob(String name, IQueue<T> queue) {
		super(name);
		this.queue = requireNonNull(queue);
	}
	
	public final void add(T item) {
		queue.push(item);
		schedule();
	}

	protected abstract void process(T item, SubMonitor monitor);
	
	@Override
	protected final IStatus run(IProgressMonitor monitor) {
		SubMonitor sm = SubMonitor.convert(monitor, getName(), queue.estimateSize());
		for (T i = queue.poll(); i != null; i = queue.poll()) {
			sm.setWorkRemaining(queue.estimateSize());
			process(i, sm.split(1, SubMonitor.SUPPRESS_NONE));
		}
		return null;
	}
	
	@Override
	protected void canceling() {
		queue.clear();
		super.canceling();
	}

	private final IQueue<T> queue;
}
