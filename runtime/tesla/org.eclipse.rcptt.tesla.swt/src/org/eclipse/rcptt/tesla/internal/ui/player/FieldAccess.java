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
package org.eclipse.rcptt.tesla.internal.ui.player;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Function;

public class FieldAccess<T, R> implements Function<T, R> {
	private final Field field;
	private Class<R> fieldType;
	public FieldAccess(Class<T> originalClass, Class<R> fieldType, String fieldName) throws NoSuchFieldException {
		this.fieldType = fieldType;
		Class<?> class_ = originalClass;
		Field result = null;
		while (true) {
			try {
				Field field = class_.getDeclaredField(fieldName);
				//TODO: Support java.lang.reflect.Type with generics.
				if (isAssignable(fieldType, field.getType())) {
					field.setAccessible(true);
					result = field;
					break;
				} else {
					throw new NoSuchFieldException("");
				}
			} catch (NoSuchFieldException e) {
				class_ = class_.getSuperclass();
				if (class_ == null) {
					throw new NoSuchFieldException("Can't find field of type" + fieldType.getName() + " in " + originalClass.getName());
				}
			}
		}
		field = Objects.requireNonNull(result);
	}

	@Override
	public R apply(T t) {
		try {
			return fieldType.cast(field.get(t));
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}
	
	private static boolean isAssignable(Class<?> to, Class<?> from) {
		if (to.isAssignableFrom(from)) {
			return true;
		}
		
		try {
			Class<?> primitive = (Class<?>) to.getField("TYPE").get(to);
			return primitive.isAssignableFrom(from);
		} catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
			return false;
		}
 	}
}
