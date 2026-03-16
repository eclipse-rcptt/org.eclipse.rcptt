/*******************************************************************************
 * Copyright (c) 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.report.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.stream.Stream;

import com.google.common.io.Closer;

/** Converts Stream supplier to an Iterable that can only have one iterator at a time **/
public abstract class StreamIterableAdapter<T> implements Iterable<T>, Closeable {
	private final Closer closer = Closer.create();
	private boolean closed = false;

	@Override
	public void close() throws IOException {
		closed = true;
		closer.close();
	}

	@SuppressWarnings("resource")
	@Override
	public Iterator<T> iterator() {
		if (closed) {
			throw new IllegalStateException("Closed");
		}
		try {
			closer.close();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		Stream<T> s = stream();
		closer.register(s::close);
		return s.iterator();
	}
	
	public abstract Stream<T> stream();

}
