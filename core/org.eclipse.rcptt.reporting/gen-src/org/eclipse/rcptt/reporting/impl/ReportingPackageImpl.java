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
package org.eclipse.rcptt.reporting.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.rcptt.ecl.core.CorePackage;
import org.eclipse.rcptt.reporting.ItemKind;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.Q7Statistics;
import org.eclipse.rcptt.reporting.ReportingFactory;
import org.eclipse.rcptt.reporting.ReportingPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ReportingPackageImpl extends EPackageImpl implements ReportingPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass q7InfoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass q7StatisticsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum itemKindEEnum = null;

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
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ReportingPackageImpl() {
		super(eNS_URI, ReportingFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ReportingPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ReportingPackage init() {
		if (isInited) return (ReportingPackage)EPackage.Registry.INSTANCE.getEPackage(ReportingPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredReportingPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ReportingPackageImpl theReportingPackage = registeredReportingPackage instanceof ReportingPackageImpl ? (ReportingPackageImpl)registeredReportingPackage : new ReportingPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		CorePackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theReportingPackage.createPackageContents();

		// Initialize created meta-data
		theReportingPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theReportingPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ReportingPackage.eNS_URI, theReportingPackage);
		return theReportingPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getQ7Info() {
		return q7InfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Info_Id() {
		return (EAttribute)q7InfoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Info_Type() {
		return (EAttribute)q7InfoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getQ7Info_Result() {
		return (EReference)q7InfoEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Info_Line() {
		return (EAttribute)q7InfoEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Info_Tags() {
		return (EAttribute)q7InfoEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Info_Description() {
		return (EAttribute)q7InfoEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Info_Subtype() {
		return (EAttribute)q7InfoEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Info_Variant() {
		return (EAttribute)q7InfoEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Info_Phase() {
		return (EAttribute)q7InfoEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getQ7Statistics() {
		return q7StatisticsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Statistics_Total() {
		return (EAttribute)q7StatisticsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Statistics_Failed() {
		return (EAttribute)q7StatisticsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Statistics_Time() {
		return (EAttribute)q7StatisticsEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Statistics_Skipped() {
		return (EAttribute)q7StatisticsEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getQ7Statistics_Passed() {
		return (EAttribute)q7StatisticsEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getItemKind() {
		return itemKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReportingFactory getReportingFactory() {
		return (ReportingFactory)getEFactoryInstance();
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
		q7InfoEClass = createEClass(Q7_INFO);
		createEAttribute(q7InfoEClass, Q7_INFO__ID);
		createEAttribute(q7InfoEClass, Q7_INFO__TYPE);
		createEAttribute(q7InfoEClass, Q7_INFO__LINE);
		createEAttribute(q7InfoEClass, Q7_INFO__TAGS);
		createEAttribute(q7InfoEClass, Q7_INFO__DESCRIPTION);
		createEAttribute(q7InfoEClass, Q7_INFO__SUBTYPE);
		createEAttribute(q7InfoEClass, Q7_INFO__VARIANT);
		createEAttribute(q7InfoEClass, Q7_INFO__PHASE);
		createEReference(q7InfoEClass, Q7_INFO__RESULT);

		q7StatisticsEClass = createEClass(Q7_STATISTICS);
		createEAttribute(q7StatisticsEClass, Q7_STATISTICS__TOTAL);
		createEAttribute(q7StatisticsEClass, Q7_STATISTICS__FAILED);
		createEAttribute(q7StatisticsEClass, Q7_STATISTICS__TIME);
		createEAttribute(q7StatisticsEClass, Q7_STATISTICS__SKIPPED);
		createEAttribute(q7StatisticsEClass, Q7_STATISTICS__PASSED);

		// Create enums
		itemKindEEnum = createEEnum(ITEM_KIND);
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

		// Initialize classes and features; add operations and parameters
		initEClass(q7InfoEClass, Q7Info.class, "Q7Info", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getQ7Info_Id(), ecorePackage.getEString(), "id", null, 0, 1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Info_Type(), this.getItemKind(), "type", null, 0, 1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Info_Line(), ecorePackage.getEInt(), "line", null, 0, 1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Info_Tags(), ecorePackage.getEString(), "tags", "", 0, 1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Info_Description(), ecorePackage.getEString(), "description", null, 0, 1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Info_Subtype(), ecorePackage.getEString(), "subtype", null, 0, 1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Info_Variant(), ecorePackage.getEString(), "variant", null, 0, -1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Info_Phase(), ecorePackage.getEString(), "phase", null, 0, 1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getQ7Info_Result(), theCorePackage.getProcessStatus(), null, "result", null, 0, 1, Q7Info.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(q7StatisticsEClass, Q7Statistics.class, "Q7Statistics", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getQ7Statistics_Total(), ecorePackage.getEInt(), "total", null, 0, 1, Q7Statistics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Statistics_Failed(), ecorePackage.getEInt(), "failed", null, 0, 1, Q7Statistics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Statistics_Time(), ecorePackage.getEInt(), "time", null, 0, 1, Q7Statistics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Statistics_Skipped(), ecorePackage.getEInt(), "skipped", null, 0, 1, Q7Statistics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getQ7Statistics_Passed(), ecorePackage.getEInt(), "passed", null, 0, 1, Q7Statistics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(itemKindEEnum, ItemKind.class, "ItemKind");
		addEEnumLiteral(itemKindEEnum, ItemKind.CONTEXT);
		addEEnumLiteral(itemKindEEnum, ItemKind.TESTCASE);
		addEEnumLiteral(itemKindEEnum, ItemKind.ECL_COMMAND);
		addEEnumLiteral(itemKindEEnum, ItemKind.SCRIPT);
		addEEnumLiteral(itemKindEEnum, ItemKind.TEST_SUITE);
		addEEnumLiteral(itemKindEEnum, ItemKind.VERIFICATION);

		// Create resource
		createResource(eNS_URI);
	}

} //ReportingPackageImpl
