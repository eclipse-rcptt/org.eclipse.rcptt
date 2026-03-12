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
package org.eclipse.rcptt.util;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class ReflectionUtil {
	public static Object callMethod(Object instance, String name,
			List<Class<?>> argTypes, List<Object> args, boolean checked)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Class<?> clazz = instance.getClass();
		Method method = findMethod(clazz, name, argTypes);
		if (method == null) {
			if (checked) {
				throw new NoSuchMethodException(format("Clas: $s, method: %s",
						clazz, name));
			}
			return null;
		}

		try {
			return method.invoke(instance,
					args.toArray(new Object[args.size()]));
		} catch (IllegalAccessException e) {
			if (checked) {
				throw e;
			}
			return null;
		} catch (InvocationTargetException e) {
			if (checked) {
				throw e;
			}
			return null;
		}
	}

	/**
	 * Safe call for a method without args, returns null in case of any error
	 * 
	 * @param instance
	 * @param name
	 * @return
	 */
	public static Object callMethod(Object instance, String name) {
		try {
			return callMethod(instance, name,
					Collections.<Class<?>> emptyList(), emptyList(), false);
		} catch (Exception e) {
			return null;
		}
	}

	public static Object getField(Object instance, String name) {
		try {
			return getField(instance, name, false);
		} catch (NoSuchFieldException e) {
			return null; // Can't happen
		} catch (IllegalAccessException e) {
			return null; // Can't happen
		}
	}

	/**
	 * Returns field value.
	 * 
	 * @param instance
	 * @param name
	 * @param checked
	 *            Controls error handling when field is not found or value
	 *            cannot be retrieved. When <code>true</code>, throws
	 *            {@link NoSuchFieldException} or {@link IllegalAccessException}
	 *            , otherwise returns <code>null</code>
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static Object getField(Object instance, String name, boolean checked)
			throws NoSuchFieldException, IllegalAccessException {
		Class<? extends Object> clazz = instance.getClass();
		Field field = findField(clazz, name);

		if (field == null) {
			if (checked) {
				throw new NoSuchFieldException(format("Class: %s, field: %s",
						clazz, name));
			}
			return null;
		}
		try {
			return field.get(instance);
		} catch (IllegalAccessException e) {
			if (checked) {
				throw e;
			}
			return null;
		}
	}

	/**
	 * searches for declared field in given class and all superclasses
	 * 
	 * @param clazz
	 * @param name
	 * @return {@link Field} or <code>null</code>
	 */
	public static Field findField(Class<?> clazz, String name) {
		if (clazz == null) {
			return null;
		}
		try {
			Field result = clazz.getDeclaredField(name);
			if (result.trySetAccessible()) {
				return result;
			}
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchFieldException e) {
			// check superclass
		}
		return findField(clazz.getSuperclass(), name);
	}

	public static Method findMethod(Class<?> clazz, String name) {
		return findMethod(clazz, name, Collections.<Class<?>> emptyList());
	}

	public static Method findMethod(Class<?> clazz, String name,
			List<Class<?>> params) {
		if (clazz == null) {
			return null;
		}

		try {
			Method result = clazz.getDeclaredMethod(name,
					params.toArray(new Class<?>[params.size()]));
			if (result.trySetAccessible()) {
				return result;
			}
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			// check superclass
		}
		return findMethod(clazz.getSuperclass(), name, params);
	}

}
