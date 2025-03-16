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
package org.eclipse.rcptt.reporting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A kind of an operation being executed.
 * <!-- end-user-doc -->
 * @see org.eclipse.rcptt.reporting.ReportingPackage#getItemKind()
 * @model
 * @generated
 */
public enum ItemKind implements Enumerator {
	/**
	 * The '<em><b>Context</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CONTEXT_VALUE
	 * @generated
	 * @ordered
	 */
	CONTEXT(0, "Context", "Context"),

	/**
	 * The '<em><b>Testcase</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TESTCASE_VALUE
	 * @generated
	 * @ordered
	 */
	TESTCASE(1, "Testcase", "Testcase"), /**
	 * The '<em><b>Ecl Command</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ECL_COMMAND_VALUE
	 * @generated
	 * @ordered
	 */
	ECL_COMMAND(2, "EclCommand", "EclCommand"), /**
	 * The '<em><b>Script</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #SCRIPT_VALUE
	 * @generated
	 * @ordered
	 */
	SCRIPT(3, "Script", "Script"), /**
	 * The '<em><b>Test Suite</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TEST_SUITE_VALUE
	 * @generated
	 * @ordered
	 */
	TEST_SUITE(4, "TestSuite", "TestSuite"), /**
	 * The '<em><b>Verification</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #VERIFICATION_VALUE
	 * @generated
	 * @ordered
	 */
	VERIFICATION(5, "Verification", "Verification");

	/**
	 * The '<em><b>Context</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Context</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CONTEXT
	 * @model name="Context"
	 * @generated
	 * @ordered
	 */
	public static final int CONTEXT_VALUE = 0;

	/**
	 * The '<em><b>Testcase</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Testcase</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TESTCASE
	 * @model name="Testcase"
	 * @generated
	 * @ordered
	 */
	public static final int TESTCASE_VALUE = 1;

	/**
	 * The '<em><b>Ecl Command</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Ecl Command</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ECL_COMMAND
	 * @model name="EclCommand"
	 * @generated
	 * @ordered
	 */
	public static final int ECL_COMMAND_VALUE = 2;

	/**
	 * The '<em><b>Script</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Script</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #SCRIPT
	 * @model name="Script"
	 * @generated
	 * @ordered
	 */
	public static final int SCRIPT_VALUE = 3;

	/**
	 * The '<em><b>Test Suite</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Test Suite</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TEST_SUITE
	 * @model name="TestSuite"
	 * @generated
	 * @ordered
	 */
	public static final int TEST_SUITE_VALUE = 4;

	/**
	 * The '<em><b>Verification</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Verification</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #VERIFICATION
	 * @model name="Verification"
	 * @generated
	 * @ordered
	 */
	public static final int VERIFICATION_VALUE = 5;

	/**
	 * An array of all the '<em><b>Item Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ItemKind[] VALUES_ARRAY =
		new ItemKind[] {
			CONTEXT,
			TESTCASE,
			ECL_COMMAND,
			SCRIPT,
			TEST_SUITE,
			VERIFICATION,
		};

	/**
	 * A public read-only list of all the '<em><b>Item Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<ItemKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Item Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ItemKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ItemKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Item Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ItemKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ItemKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Item Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ItemKind get(int value) {
		switch (value) {
			case CONTEXT_VALUE: return CONTEXT;
			case TESTCASE_VALUE: return TESTCASE;
			case ECL_COMMAND_VALUE: return ECL_COMMAND;
			case SCRIPT_VALUE: return SCRIPT;
			case TEST_SUITE_VALUE: return TEST_SUITE;
			case VERIFICATION_VALUE: return VERIFICATION;
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
	private ItemKind(int value, String name, String literal) {
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
	
} //ItemKind
