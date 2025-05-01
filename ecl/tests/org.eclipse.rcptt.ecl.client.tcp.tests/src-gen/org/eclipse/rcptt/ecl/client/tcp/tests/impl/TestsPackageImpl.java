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
package org.eclipse.rcptt.ecl.client.tcp.tests.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.rcptt.ecl.client.tcp.tests.EclInjectedCommand;
import org.eclipse.rcptt.ecl.client.tcp.tests.TestsFactory;
import org.eclipse.rcptt.ecl.client.tcp.tests.TestsPackage;

import org.eclipse.rcptt.ecl.core.CorePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class TestsPackageImpl extends EPackageImpl implements TestsPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclInjectedCommandEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.rcptt.ecl.client.tcp.tests.TestsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TestsPackageImpl() {
		super(eNS_URI, TestsFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link TestsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static TestsPackage init() {
		if (isInited) return (TestsPackage)EPackage.Registry.INSTANCE.getEPackage(TestsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredTestsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		TestsPackageImpl theTestsPackage = registeredTestsPackage instanceof TestsPackageImpl ? (TestsPackageImpl)registeredTestsPackage : new TestsPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		CorePackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theTestsPackage.createPackageContents();

		// Initialize created meta-data
		theTestsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTestsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(TestsPackage.eNS_URI, theTestsPackage);
		return theTestsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclInjectedCommand() {
		return eclInjectedCommandEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TestsFactory getTestsFactory() {
		return (TestsFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		eclInjectedCommandEClass = createEClass(ECL_INJECTED_COMMAND);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		CorePackage theCorePackage = (CorePackage)EPackage.Registry.INSTANCE.getEPackage(CorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		eclInjectedCommandEClass.getESuperTypes().add(theCorePackage.getCommand());

		// Initialize classes, features, and operations; add parameters
		initEClass(eclInjectedCommandEClass, EclInjectedCommand.class, "EclInjectedCommand", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //TestsPackageImpl
