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
package org.eclipse.rcptt.tesla.swt.reflection;

import static org.eclipse.rcptt.tesla.core.TeslaFeatures.isProtectedEnabled;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;

import org.eclipse.rcptt.logging.IQ7ActivityLogs;
import org.eclipse.rcptt.logging.Q7LoggingManager;
import org.eclipse.rcptt.util.ReflectionUtil;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.ui.PropertyNode;
import org.eclipse.rcptt.tesla.core.ui.PropertyNodeType;
import org.eclipse.rcptt.tesla.core.ui.UiFactory;

public class JavaMembersHelper {

	public static final String NULL_CONST = "null"; //$NON-NLS-1$
	public static final String VOID_CONST = "void"; //$NON-NLS-1$	
	public static final String CLASS_NAME_PROPERTY = "getClassName"; //$NON-NLS-1$
	public static final String CLASS_NAME_PROPERTY_CAPTION = CLASS_NAME_PROPERTY
			+ "()"; //$NON-NLS-1$
	public static final String ARRAY_LENGTH_PROPERTY_CAPTION = "length"; //$NON-NLS-1$	

	private final static List<String> allowedFileds = new ArrayList<String>();
	private final static List<String> excludedFileds = new ArrayList<String>();
	private final static List<String> allowedMethods = new ArrayList<String>();
	private final static List<String> excludedMethods = new ArrayList<String>();

	static {
		allowedFileds.add(".*"); //$NON-NLS-1$
		excludedFileds.add("handle"); //$NON-NLS-1$

		allowedMethods.add("get.*"); //$NON-NLS-1$
		allowedMethods.add("is.*"); //$NON-NLS-1$
		allowedMethods.add("has.*"); //$NON-NLS-1$
		allowedMethods.add("size"); //$NON-NLS-1$

		excludedMethods.add("getClass"); //$NON-NLS-1$
		excludedMethods.add("getDisplay"); //$NON-NLS-1$
		excludedMethods.add("getDevice"); //$NON-NLS-1$
		excludedMethods.add("getMonitor"); //$NON-NLS-1$
		excludedMethods.add("isDisposed"); //$NON-NLS-1$
		excludedMethods.add("java.lang.Object.hashCode"); //$NON-NLS-1$
		excludedMethods.add("org.eclipse.swt.widgets.Shell.getToolBar"); //$NON-NLS-1$
	}

	public static Object getRawPropertyValue(Object object, String nodePath)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		PropertyValue value = PropertyPath.fromString(nodePath)
				.getValue(object);
		return value.isProperty() ? value.getStringValue() : value.getRawValue();
	}

	public static String getPropertyValue(Object object, String nodePath)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		PropertyValue value = PropertyPath.fromString(nodePath)
				.getValue(object);
		return value.isProperty() ? value.getStringValue() : null;
	}

	public static boolean fillProperties(Object object, String nodePath,
			EList<PropertyNode> propertiesNodes)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Object node = PropertyPath.fromString(nodePath).getValue(object).rawValue;
		if (node != null) {
			fillProperties(node, propertiesNodes);
			return true;
		}
		return false;
	}

	public static void fillProperties(Object object,
			EList<PropertyNode> propertiesNodes)
			throws IllegalArgumentException {
		if (object == null) {
			return;
		}

		PropertyNode classNode = UiFactory.eINSTANCE.createPropertyNode();
		classNode.setName(CLASS_NAME_PROPERTY_CAPTION);
		classNode.setType(PropertyNodeType.PROPERTY);
		classNode.setValue(MemberValueHelper.getClassName(object));
		propertiesNodes.add(classNode);

		if (object.getClass().isArray()) {
			fillArrayProperties(object, propertiesNodes);
		}

		if (object instanceof List) {
			fillListProperties(object, propertiesNodes);
		}

		MembersContainer members = collectObjectMembers(object);
		for (Field field : members.getFields()) {
			if (TeslaFeatures.isActivityLogging()) {
				Q7LoggingManager.logMessage(IQ7ActivityLogs.PROPERTIES,
						"\t\tprocess field: " + object.getClass().getName()
								+ "." + field.getName());
			}
			processField(field, object, propertiesNodes);
		}

		for (Method method : members.getMethods()) {
			if (TeslaFeatures.isActivityLogging()) {
				Q7LoggingManager.logMessage(IQ7ActivityLogs.PROPERTIES,
						"\t\tprocess method: " + object.getClass().getName()
								+ "." + method.getName());
			}
			processMethod(method, object, propertiesNodes);
		}

		mapToString(object, propertiesNodes);
	}

	private static void mapToString(Object object, List<PropertyNode> nodes) {
		Class<?> clazz = object.getClass();
		if (clazz.isArray() || object instanceof List<?>) {
			return; // don't map toString for collections
		}
		if (nodes.size() > 1) {
			// return; // object has fields and/or getters
		}

		Method toString = ReflectionUtil.findMethod(object.getClass(), "toString");
		if (toString == null || toString.getDeclaringClass().equals(Object.class)) {
			return; // don't map unoverriden toString
		}

		String value = null;
		try {
			value = (String) toString.invoke(object);
		} catch (Exception e) {
			return; // ok, no luck
		}
		if (value != null && value.length() > 16384) {
			return; // don't bother with nulls or big strings
		}
		PropertyNode stringNode = UiFactory.eINSTANCE.createPropertyNode();
		stringNode.setName("toString()");
		stringNode.setType(PropertyNodeType.PROPERTY);
		stringNode.setValue(value == null ? NULL_CONST : value);
		nodes.add(stringNode);
	}

	private static void fillArrayProperties(Object object,
			EList<PropertyNode> propertiesNodes) {
		int length = Array.getLength(object);

		PropertyNode lenghtNode = UiFactory.eINSTANCE.createPropertyNode();
		lenghtNode.setName(ARRAY_LENGTH_PROPERTY_CAPTION);
		lenghtNode.setType(PropertyNodeType.PROPERTY);
		lenghtNode.setValue(Integer.toString(length));
		propertiesNodes.add(lenghtNode);

		Class<?> itemsType = object.getClass().getComponentType();
		for (int i = 0; i < length; i++) {
			Object item = Array.get(object, i);
			String strValue = MemberValueHelper.getFormattedValue(item, "",
					item.getClass(), object);
			PropertyNodeType type = MemberValueHelper.detectType(item
					.getClass());
			if (PropertyNodeType.REFERENCE.equals(type)) {
				if (NULL_CONST.equals(strValue)) {
					type = PropertyNodeType.PROPERTY;
				}
			}
			PropertyNode propertyNode = UiFactory.eINSTANCE
					.createPropertyNode();
			propertyNode.setName(itemsType.getSimpleName() + "[" + i + "]");
			propertyNode.setType(type);
			if (type.equals(PropertyNodeType.PROPERTY)) {
				propertyNode.setValue(strValue);
			}
			propertiesNodes.add(propertyNode);
		}
	}

	private static void fillListProperties(Object object,
			EList<PropertyNode> propertiesNodes) {
		List<?> list = (List<?>) object;

		int idx = 0;
		for (Object item : list) {
			String strValue = MemberValueHelper.getFormattedValue(item, "",
					item.getClass(), object);
			PropertyNodeType type = MemberValueHelper.detectType(item
					.getClass());
			if (PropertyNodeType.REFERENCE.equals(type)) {
				if (NULL_CONST.equals(strValue)) {
					type = PropertyNodeType.PROPERTY;
				}
			}
			PropertyNode propertyNode = UiFactory.eINSTANCE
					.createPropertyNode();
			propertyNode.setName(item.getClass().getSimpleName() + "[" + idx
					+ "]");
			propertyNode.setType(type);
			if (type.equals(PropertyNodeType.PROPERTY)) {
				propertyNode.setValue(strValue);
			}
			propertiesNodes.add(propertyNode);
			idx++;
		}
	}

	private static void processField(Field field, Object object,
			EList<PropertyNode> propertiesNodes) {
		Object value = null;
		String strValue = null;
		PropertyNodeType type = null;
		try {
			value = field.get(object);
		} catch (IllegalAccessException e) {
			return; // Ignore, skip field
		}
		strValue = MemberValueHelper.getFormattedValue(value, field.getName(),
				field.getType(), object);
		type = MemberValueHelper.detectType(field.getType());
		if (PropertyNodeType.REFERENCE.equals(type)) {
			if (NULL_CONST.equals(strValue)) {
				type = PropertyNodeType.PROPERTY;
			}
		}
		PropertyNode propertyNode = UiFactory.eINSTANCE.createPropertyNode();
		propertyNode.setName(field.getName());
		propertyNode.setType(type);
		if (type.equals(PropertyNodeType.PROPERTY)) {
			propertyNode.setValue(strValue);
		}
		propertiesNodes.add(propertyNode);
	}

	private static void processMethod(Method method, Object object,
			EList<PropertyNode> propertiesNodes) {
		Object value = null;
		String strValue = null;
		PropertyNodeType type = null;
		try {
			value = method.invoke(object);
		} catch (IllegalAccessException e) {
			return; // Ignore, skip method
		} catch (InvocationTargetException e) {
			return; // Ignore, skip method
		}
		strValue = MemberValueHelper.getFormattedValue(value, method.getName(),
				method.getReturnType(), object);
		type = MemberValueHelper.detectType(method.getReturnType());
		if (PropertyNodeType.REFERENCE.equals(type)) {
			if (NULL_CONST.equals(strValue)) {
				type = PropertyNodeType.PROPERTY;
			}
		}
		PropertyNode propertyNode = UiFactory.eINSTANCE.createPropertyNode();
		propertyNode.setName(method.getName() + "()");
		propertyNode.setType(type);
		if (type.equals(PropertyNodeType.PROPERTY)) {
			propertyNode.setValue(strValue);
		}
		propertiesNodes.add(propertyNode);
	}

	private static MembersContainer collectObjectMembers(Object object) {
		if (object == null) {
			return null;
		}
		MembersContainer container = new MembersContainer();
		collectMembers(object.getClass(), container);
		return container;
	}

	private static void collectMembers(Class<?> clazz, MembersContainer acc) {
		if ((clazz.getModifiers() & PUBLIC) != 0 || (isProtectedEnabled() && (clazz.getModifiers() & PROTECTED) != 0))  {
			for (Field field : clazz.getDeclaredFields()) {
				if (checkField(field) && field.trySetAccessible()) {
					acc.addField(field);
				}
			}
			for (Method method : clazz.getDeclaredMethods()) {
				if (checkMethod(method) && method.trySetAccessible()) {
					acc.addMethod(method);
				}
			}
		}
		Class<?> parent = clazz.getSuperclass();
		if (parent != null) {
			collectMembers(parent, acc);
		}
	}

	/**
	 * True if method/field is not static and either public, or protected and
	 * protected members are enabled
	 * 
	 * @param member
	 * @return
	 */
	private static boolean isVisible(Member member) {
		int modifiers = member.getModifiers();
		if ((modifiers & STATIC) != 0) {
			return false;
		}
		if ((modifiers & PUBLIC) != 0) {
			return true;
		}
		return (modifiers & PROTECTED) != 0 && isProtectedEnabled();
	}

	private static boolean checkField(Field field) {
		if (!isVisible(field)) {
			return false;
		}

		for (String pattern : excludedFileds) {
			if (matchesExclusion(field, pattern)) {
				return false;
			}
		}

		for (String pattern : allowedFileds) {
			if (field.getName().matches(pattern)) {
				return true;
			}
		}

		return true;
	}

	private static boolean checkMethod(Method method) {
		if (!isVisible(method)) {
			return false;
		}
		if (method.getParameterTypes().length != 0) {
			return false;
		}

		String name = method.getName();
		for (String pattern : excludedMethods) {
			if (matchesExclusion(method, pattern)) {
				return false;
			}
		}

		for (String pattern : allowedMethods) {
			if (name.matches(pattern)) {
				return true;
			}
		}
		return false;
	}

	private static boolean matchesExclusion(Member slot, String exclusion) {
		int lastDotIndex = exclusion.lastIndexOf(".");
		String methodName = lastDotIndex == -1 ? exclusion : exclusion
				.substring(lastDotIndex + 1);
		String className = lastDotIndex == -1 ? null : exclusion.substring(0,
				lastDotIndex);
		if (!methodName.equals(slot.getName()))
			return false;

		if (className == null)
			return true;

		return typeMatches(slot.getDeclaringClass(), className);
	}

	private static boolean typeMatches(Class<?> clazz, String name) {
		if (clazz == null) {
			return false;
		}
		if (clazz.getName().equals(name)) {
			return true;
		}
		if (typeMatches(clazz.getSuperclass(), name)) {
			return true;
		}

		for (Class<?> iface : clazz.getInterfaces()) {
			if (typeMatches(iface, name)) {
				return true;
			}
		}

		return false;
	}

	private static class MembersContainer {
		private final List<Field> fields = new ArrayList<Field>();
		private final List<Method> methods = new ArrayList<Method>();
		private final Set<String> memberNames = new HashSet<String>();

		public void addField(Field field) {
			String name = field.getName();
			if (memberNames.contains(name)) {
				return;
			}
			memberNames.add(name);
			fields.add(field);
		}

		public void addMethod(Method method) {
			String name = method.getName();
			if (memberNames.contains(name)) {
				return;
			}
			memberNames.add(name);
			methods.add(method);
		}

		public Iterable<Field> getFields() {
			return fields;
		}

		public Iterable<Method> getMethods() {
			return methods;
		}
	}

	private static final Pattern indexedPattern = Pattern
			.compile("(.*)\\[(\\d+)\\]");

	private static class PropertyPath {
		public static PropertyPath fromString(String nodePath) {
			if (nodePath.length() == 0) {
				return new PropertyPath(new Node[0]);
			}
			String[] items = nodePath.split("\\.");
			List<Node> nodes = new ArrayList<Node>();
			for (String item : items) {
				if (item.endsWith("()")) {
					nodes.add(new MethodNode(item.substring(0,
							item.length() - 2)));
					continue;
				}

				Matcher m = indexedPattern.matcher(item);
				if (m.matches()) {
					Node node = new IndexNode(nodes.isEmpty() ? null
							: nodes.get(nodes.size() - 1), m.group(1),
							Integer.parseInt(m.group(2)));
					nodes.add(node);
					continue;
				}

				nodes.add(new FieldNode(item));
			}

			return new PropertyPath(nodes.toArray(new Node[nodes.size()]));
		}

		private final Node[] nodes;

		private PropertyPath(Node[] nodes) {
			this.nodes = nodes;
		}

		public PropertyValue getValue(Object object)
				throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException {
			PropertyValue result = PropertyValue.fromObject(object);
			for (Node node : nodes) {
				result = node.getValue(result);
			}
			return result;
		}
	}

	private static abstract class Node {
		final String memberName;

		public Node(String memberName) {
			this.memberName = memberName;
		}

		abstract PropertyValue getValue(Object object)
				throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException;

		final PropertyValue getValue(PropertyValue parent)
				throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException {
			return getValue(parent.rawValue);
		}
	}

	private static class FieldNode extends Node {
		public FieldNode(String fieldName) {
			super(fieldName);
		}

		@Override
		PropertyValue getValue(Object object) throws IllegalArgumentException,
				IllegalAccessException {
			if (object.getClass().isArray()
					&& ARRAY_LENGTH_PROPERTY_CAPTION.equals(memberName)) {
				return new PropertyValue(object, Array.getLength(object),
						memberName, int.class);
			}
			Field field = ReflectionUtil.findField(object.getClass(),
					memberName);
			Object value = field.get(object);
			return new PropertyValue(object, value, memberName, field.getType());
		}
	}

	private static class MethodNode extends Node {
		public MethodNode(String methodName) {
			super(methodName);
		}

		@Override
		PropertyValue getValue(Object object) throws IllegalArgumentException,
				IllegalAccessException, InvocationTargetException {
			if (memberName.equals(CLASS_NAME_PROPERTY)) {
				return new PropertyValue(object,
						MemberValueHelper.getClassName(object),
						CLASS_NAME_PROPERTY, String.class);
			}

			Method method = ReflectionUtil.findMethod(object.getClass(),
					memberName);
			Object value = method.invoke(object);
			return new PropertyValue(object, value, memberName,
					method.getReturnType());
		}
	}

	private static class IndexNode extends Node {
		final int index;
		final String elementType;

		public IndexNode(Node parent, String elementType, int index) {
			super(parent == null ? "" : parent.memberName);
			this.elementType = elementType;
			this.index = index;
		}

		@Override
		PropertyValue getValue(Object object) throws IllegalArgumentException,
				IllegalAccessException, InvocationTargetException {
			Object value = null;
			if (object.getClass().isArray()) {
				value = Array.get(object, index);
			} else if (object instanceof List<?>) {
				value = ((List<?>) object).get(index);
			} else {
				throw new IllegalArgumentException(
						"Can't get indexed value from object of type "
								+ object.getClass().getName());
			}

			return new PropertyValue(object, value, elementType, object
					.getClass().isArray() ? object.getClass()
					.getComponentType() : (value == null ? Object.class
					: value.getClass()));
		}
	}

	private static class PropertyValue {
		final Object parent;
		final Object rawValue;
		final String memberName;
		/**
		 * For method values -- return type For field values -- declared type
		 * For list/array elements -- element type
		 */
		final Class<?> declaredType;

		public PropertyValue(Object parent, Object rawValue, String memberName,
				Class<?> declaredType) {
			this.rawValue = rawValue;
			this.memberName = memberName;
			this.declaredType = declaredType;
			this.parent = parent;
		}

		public static PropertyValue fromObject(Object value) {
			return new PropertyValue(null, value, "", value.getClass());
		}

		public boolean isProperty() {
			return rawValue == null
					|| (MemberValueHelper.detectType(declaredType) == PropertyNodeType.PROPERTY);
		}

		public String getStringValue() {
			return MemberValueHelper.getFormattedValue(rawValue, memberName,
					declaredType, parent);
		}

		public Object getRawValue() {
			return rawValue;
		}
	}
}
