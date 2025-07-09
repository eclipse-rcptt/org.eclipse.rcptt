package org.eclipse.rcptt.launching;
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

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Throwables;

public final class CheckedExceptionWrapper extends RuntimeException {
	private static final long serialVersionUID = -2641861448641156035L;
	
	public CheckedExceptionWrapper(Exception e) {
		super(e);
	}
	
	public interface ThrowingRunnable {
		void run() throws Exception;
	}
	
	public static Runnable wrap(ThrowingRunnable throwing) {
		return () -> {
			try {
				throwing.run();
			} catch (Exception e) {
				Throwables.throwIfUnchecked(e);
				throw new CheckedExceptionWrapper(e);
			} 
		};
	}
	
	public interface ThrowingConsumer<T> {
		void accept(T e) throws Exception;
	}
	
	public static <T> Consumer<T> wrap(ThrowingConsumer<T> throwing) {
		return (t) -> {
			try {
				throwing.accept(t);
			} catch (Exception e) {
				Throwables.throwIfUnchecked(e);
				throw new CheckedExceptionWrapper(e);
			} 
		};
	}
	
	public interface ThrowingFunction<T, R> {
		R apply(T t) throws Exception;
	}
	
	public static <T, R> Function<T, R> wrap(ThrowingFunction<T, R> throwing) {
		return (t) -> {
			try {
				return throwing.apply(t);
			} catch (Exception e) {
				Throwables.throwIfUnchecked(e);
				throw new CheckedExceptionWrapper(e);
			} 
		};
	}

	
	public <E extends Exception> void rethrow(Class<E> clazz) throws E {
		Throwables.throwIfInstanceOf(getCause(), clazz);
	}
	
	public void rethrowUnchecked() {
		Throwables.throwIfUnchecked(getCause());
	}
}
