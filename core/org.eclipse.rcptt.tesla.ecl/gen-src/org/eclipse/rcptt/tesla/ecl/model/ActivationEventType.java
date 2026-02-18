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
package org.eclipse.rcptt.tesla.ecl.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Activation Event Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#getActivationEventType()
 * @model
 * @generated
 */
public enum ActivationEventType implements Enumerator {
	/**
	 * The '<em><b>Programmatic</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #PROGRAMMATIC_VALUE
	 * @generated
	 * @ordered
	 */
	PROGRAMMATIC(4, "Programmatic", "Programmatic"),

	/**
	 * The '<em><b>Key Pressed</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #KEY_PRESSED_VALUE
	 * @generated
	 * @ordered
	 */
	KEY_PRESSED(1, "KeyPressed", "KeyPressed"),

	/**
	 * The '<em><b>Mouse Click Selection</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #MOUSE_CLICK_SELECTION_VALUE
	 * @generated
	 * @ordered
	 */
	MOUSE_CLICK_SELECTION(2, "MouseClickSelection", "MouseClickSelection"),

	/**
	 * The '<em><b>Mouse Double Click Selection</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #MOUSE_DOUBLE_CLICK_SELECTION_VALUE
	 * @generated
	 * @ordered
	 */
	MOUSE_DOUBLE_CLICK_SELECTION(3, "MouseDoubleClickSelection", "MouseDoubleClickSelection"),

	/**
	 * The '<em><b>Traversal</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TRAVERSAL_VALUE
	 * @generated
	 * @ordered
	 */
	TRAVERSAL(5, "Traversal", "Traversal");

	/**
	 * The '<em><b>Programmatic</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Programmatic</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #PROGRAMMATIC
	 * @model name="Programmatic"
	 * @generated
	 * @ordered
	 */
	public static final int PROGRAMMATIC_VALUE = 4;

	/**
	 * The '<em><b>Key Pressed</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Key Pressed</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #KEY_PRESSED
	 * @model name="KeyPressed"
	 * @generated
	 * @ordered
	 */
	public static final int KEY_PRESSED_VALUE = 1;

	/**
	 * The '<em><b>Mouse Click Selection</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Mouse Click Selection</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #MOUSE_CLICK_SELECTION
	 * @model name="MouseClickSelection"
	 * @generated
	 * @ordered
	 */
	public static final int MOUSE_CLICK_SELECTION_VALUE = 2;

	/**
	 * The '<em><b>Mouse Double Click Selection</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Mouse Double Click Selection</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #MOUSE_DOUBLE_CLICK_SELECTION
	 * @model name="MouseDoubleClickSelection"
	 * @generated
	 * @ordered
	 */
	public static final int MOUSE_DOUBLE_CLICK_SELECTION_VALUE = 3;

	/**
	 * The '<em><b>Traversal</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Traversal</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TRAVERSAL
	 * @model name="Traversal"
	 * @generated
	 * @ordered
	 */
	public static final int TRAVERSAL_VALUE = 5;

	/**
	 * An array of all the '<em><b>Activation Event Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ActivationEventType[] VALUES_ARRAY =
		new ActivationEventType[] {
			PROGRAMMATIC,
			KEY_PRESSED,
			MOUSE_CLICK_SELECTION,
			MOUSE_DOUBLE_CLICK_SELECTION,
			TRAVERSAL,
		};

	/**
	 * A public read-only list of all the '<em><b>Activation Event Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<ActivationEventType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Activation Event Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ActivationEventType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ActivationEventType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Activation Event Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ActivationEventType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ActivationEventType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Activation Event Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ActivationEventType get(int value) {
		switch (value) {
			case PROGRAMMATIC_VALUE: return PROGRAMMATIC;
			case KEY_PRESSED_VALUE: return KEY_PRESSED;
			case MOUSE_CLICK_SELECTION_VALUE: return MOUSE_CLICK_SELECTION;
			case MOUSE_DOUBLE_CLICK_SELECTION_VALUE: return MOUSE_DOUBLE_CLICK_SELECTION;
			case TRAVERSAL_VALUE: return TRAVERSAL;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private ActivationEventType(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //ActivationEventType
