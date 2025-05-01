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
/**
 */
package org.eclipse.rcptt.ecl.client.tcp.tests;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.rcptt.ecl.core.CorePackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.rcptt.ecl.client.tcp.tests.TestsFactory
 * @model kind="package"
 * @generated
 */
public interface TestsPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "tests";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/ecl/testcommands.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "org.eclipse.rcptt.ecl.client.tcp.tests";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	TestsPackage eINSTANCE = org.eclipse.rcptt.ecl.client.tcp.tests.impl.TestsPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.client.tcp.tests.impl.EclInjectedCommandImpl <em>Ecl Injected Command</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.client.tcp.tests.impl.EclInjectedCommandImpl
	 * @see org.eclipse.rcptt.ecl.client.tcp.tests.impl.TestsPackageImpl#getEclInjectedCommand()
	 * @generated
	 */
	int ECL_INJECTED_COMMAND = 0;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECL_INJECTED_COMMAND__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECL_INJECTED_COMMAND__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The number of structural features of the '<em>Ecl Injected Command</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECL_INJECTED_COMMAND_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.client.tcp.tests.EclInjectedCommand <em>Ecl Injected Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Ecl Injected Command</em>'.
	 * @see org.eclipse.rcptt.ecl.client.tcp.tests.EclInjectedCommand
	 * @generated
	 */
	EClass getEclInjectedCommand();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	TestsFactory getTestsFactory();

} //TestsPackage
