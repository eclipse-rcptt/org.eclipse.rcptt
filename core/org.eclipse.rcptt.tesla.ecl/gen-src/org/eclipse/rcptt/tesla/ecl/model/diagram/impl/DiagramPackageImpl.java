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
package org.eclipse.rcptt.tesla.ecl.model.diagram.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.rcptt.ecl.core.CorePackage;

import org.eclipse.rcptt.tesla.core.info.InfoPackage;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolPackage;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawPackage;
import org.eclipse.rcptt.tesla.core.ui.UiPackage;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;
import org.eclipse.rcptt.tesla.ecl.model.diagram.ActivateDirectEdit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.CancelDirectEdit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.CommitDirectEdit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.DiagramFactory;
import org.eclipse.rcptt.tesla.ecl.model.diagram.DiagramPackage;
import org.eclipse.rcptt.tesla.ecl.model.diagram.DirectEdit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetConnectionHandle;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetDiagram;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetEditPart;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetEntry;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetFigure;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetHandle;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPalette;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPaletteEntry;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPopupBarItem;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetSourceConnection;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetTargetConnection;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseAction;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseDoubleClick;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseDrag;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseEnter;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseExit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseHover;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseMove;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MousePress;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseRelease;
import org.eclipse.rcptt.tesla.ecl.model.impl.TeslaPackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!--
 * end-user-doc -->
 * @generated
 */
public class DiagramPackageImpl extends EPackageImpl implements DiagramPackage {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getDiagramEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getPaletteEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getEditPartEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getFigureEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getEntryEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass directEditEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass activateDirectEditEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass commitDirectEditEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass cancelDirectEditEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseActionEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseMoveEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseDragEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mousePressEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseReleaseEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseDoubleClickEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseEnterEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseExitEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseHoverEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getSourceConnectionEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTargetConnectionEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getPopupBarItemEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getConnectionHandleEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getPaletteEntryEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getHandleEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the
	 * package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory
	 * method {@link #init init()}, which also performs initialization of the
	 * package, or returns the registered package, if one already exists. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.rcptt.tesla.ecl.model.diagram.DiagramPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private DiagramPackageImpl() {
		super(eNS_URI, DiagramFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model,
	 * and for any others upon which it depends.
	 * 
	 * <p>
	 * This method is used to initialize {@link DiagramPackage#eINSTANCE} when
	 * that field is accessed. Clients should not invoke it directly. Instead,
	 * they should simply access that field to obtain the package. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static DiagramPackage init() {
		if (isInited) return (DiagramPackage)EPackage.Registry.INSTANCE.getEPackage(DiagramPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredDiagramPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		DiagramPackageImpl theDiagramPackage = registeredDiagramPackage instanceof DiagramPackageImpl ? (DiagramPackageImpl)registeredDiagramPackage : new DiagramPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		UiPackage.eINSTANCE.eClass();
		CorePackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();
		InfoPackage.eINSTANCE.eClass();
		RawPackage.eINSTANCE.eClass();
		ProtocolPackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(TeslaPackage.eNS_URI);
		TeslaPackageImpl theTeslaPackage = (TeslaPackageImpl)(registeredPackage instanceof TeslaPackageImpl ? registeredPackage : TeslaPackage.eINSTANCE);

		// Create package meta-data objects
		theDiagramPackage.createPackageContents();
		theTeslaPackage.createPackageContents();

		// Initialize created meta-data
		theDiagramPackage.initializePackageContents();
		theTeslaPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theDiagramPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(DiagramPackage.eNS_URI, theDiagramPackage);
		return theDiagramPackage;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetDiagram() {
		return getDiagramEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetPalette() {
		return getPaletteEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetEditPart() {
		return getEditPartEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetEditPart_Path() {
		return (EAttribute)getEditPartEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetEditPart_Name() {
		return (EAttribute)getEditPartEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetEditPart_Feature() {
		return (EAttribute)getEditPartEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetEditPart_ClassName() {
		return (EAttribute)getEditPartEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetEditPart_Text() {
		return (EAttribute)getEditPartEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetEditPart_CustomId() {
		return (EAttribute)getEditPartEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetFigure() {
		return getFigureEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetEntry() {
		return getEntryEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDirectEdit() {
		return directEditEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDirectEdit_Control() {
		return (EReference)directEditEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getActivateDirectEdit() {
		return activateDirectEditEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCommitDirectEdit() {
		return commitDirectEditEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCancelDirectEdit() {
		return cancelDirectEditEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouseAction() {
		return mouseActionEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMouseAction_Control() {
		return (EReference)mouseActionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouseAction_X() {
		return (EAttribute)mouseActionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouseAction_Y() {
		return (EAttribute)mouseActionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouseAction_Mask() {
		return (EAttribute)mouseActionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouseAction_Button() {
		return (EAttribute)mouseActionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouseAction_Height() {
		return (EAttribute)mouseActionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouseAction_Width() {
		return (EAttribute)mouseActionEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouseMove() {
		return mouseMoveEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouseDrag() {
		return mouseDragEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMousePress() {
		return mousePressEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouseRelease() {
		return mouseReleaseEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouseDoubleClick() {
		return mouseDoubleClickEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouseEnter() {
		return mouseEnterEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouseExit() {
		return mouseExitEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouseHover() {
		return mouseHoverEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetSourceConnection() {
		return getSourceConnectionEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetTargetConnection() {
		return getTargetConnectionEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetPopupBarItem() {
		return getPopupBarItemEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetConnectionHandle() {
		return getConnectionHandleEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetPaletteEntry() {
		return getPaletteEntryEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetHandle() {
		return getHandleEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagramFactory getDiagramFactory() {
		return (DiagramFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
		getDiagramEClass = createEClass(GET_DIAGRAM);

		getPaletteEClass = createEClass(GET_PALETTE);

		getEditPartEClass = createEClass(GET_EDIT_PART);
		createEAttribute(getEditPartEClass, GET_EDIT_PART__PATH);
		createEAttribute(getEditPartEClass, GET_EDIT_PART__NAME);
		createEAttribute(getEditPartEClass, GET_EDIT_PART__FEATURE);
		createEAttribute(getEditPartEClass, GET_EDIT_PART__CLASS_NAME);
		createEAttribute(getEditPartEClass, GET_EDIT_PART__TEXT);
		createEAttribute(getEditPartEClass, GET_EDIT_PART__CUSTOM_ID);

		getFigureEClass = createEClass(GET_FIGURE);

		getEntryEClass = createEClass(GET_ENTRY);

		directEditEClass = createEClass(DIRECT_EDIT);
		createEReference(directEditEClass, DIRECT_EDIT__CONTROL);

		activateDirectEditEClass = createEClass(ACTIVATE_DIRECT_EDIT);

		commitDirectEditEClass = createEClass(COMMIT_DIRECT_EDIT);

		cancelDirectEditEClass = createEClass(CANCEL_DIRECT_EDIT);

		mouseActionEClass = createEClass(MOUSE_ACTION);
		createEReference(mouseActionEClass, MOUSE_ACTION__CONTROL);
		createEAttribute(mouseActionEClass, MOUSE_ACTION__X);
		createEAttribute(mouseActionEClass, MOUSE_ACTION__Y);
		createEAttribute(mouseActionEClass, MOUSE_ACTION__BUTTON);
		createEAttribute(mouseActionEClass, MOUSE_ACTION__MASK);
		createEAttribute(mouseActionEClass, MOUSE_ACTION__HEIGHT);
		createEAttribute(mouseActionEClass, MOUSE_ACTION__WIDTH);

		mouseMoveEClass = createEClass(MOUSE_MOVE);

		mouseDragEClass = createEClass(MOUSE_DRAG);

		mousePressEClass = createEClass(MOUSE_PRESS);

		mouseReleaseEClass = createEClass(MOUSE_RELEASE);

		mouseDoubleClickEClass = createEClass(MOUSE_DOUBLE_CLICK);

		mouseEnterEClass = createEClass(MOUSE_ENTER);

		mouseExitEClass = createEClass(MOUSE_EXIT);

		mouseHoverEClass = createEClass(MOUSE_HOVER);

		getSourceConnectionEClass = createEClass(GET_SOURCE_CONNECTION);

		getTargetConnectionEClass = createEClass(GET_TARGET_CONNECTION);

		getPopupBarItemEClass = createEClass(GET_POPUP_BAR_ITEM);

		getConnectionHandleEClass = createEClass(GET_CONNECTION_HANDLE);

		getPaletteEntryEClass = createEClass(GET_PALETTE_ENTRY);

		getHandleEClass = createEClass(GET_HANDLE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model. This
	 * method is guarded to have no affect on any invocation but its first. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
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
		TeslaPackage theTeslaPackage = (TeslaPackage)EPackage.Registry.INSTANCE.getEPackage(TeslaPackage.eNS_URI);
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);
		CorePackage theCorePackage = (CorePackage)EPackage.Registry.INSTANCE.getEPackage(CorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		getDiagramEClass.getESuperTypes().add(theTeslaPackage.getSelector());
		getPaletteEClass.getESuperTypes().add(theTeslaPackage.getSelector());
		getEditPartEClass.getESuperTypes().add(theTeslaPackage.getSelector());
		getFigureEClass.getESuperTypes().add(theTeslaPackage.getPathSelector());
		getEntryEClass.getESuperTypes().add(theTeslaPackage.getTextSelector());
		directEditEClass.getESuperTypes().add(theCorePackage.getCommand());
		activateDirectEditEClass.getESuperTypes().add(this.getDirectEdit());
		commitDirectEditEClass.getESuperTypes().add(this.getDirectEdit());
		cancelDirectEditEClass.getESuperTypes().add(this.getDirectEdit());
		mouseActionEClass.getESuperTypes().add(theCorePackage.getCommand());
		mouseMoveEClass.getESuperTypes().add(this.getMouseAction());
		mouseDragEClass.getESuperTypes().add(this.getMouseAction());
		mousePressEClass.getESuperTypes().add(this.getMouseAction());
		mouseReleaseEClass.getESuperTypes().add(this.getMouseAction());
		mouseDoubleClickEClass.getESuperTypes().add(this.getMouseAction());
		mouseEnterEClass.getESuperTypes().add(this.getMouseAction());
		mouseExitEClass.getESuperTypes().add(this.getMouseAction());
		mouseHoverEClass.getESuperTypes().add(this.getMouseAction());
		getSourceConnectionEClass.getESuperTypes().add(theTeslaPackage.getPathSelector());
		getTargetConnectionEClass.getESuperTypes().add(theTeslaPackage.getPathSelector());
		getPopupBarItemEClass.getESuperTypes().add(theTeslaPackage.getPathSelector());
		getConnectionHandleEClass.getESuperTypes().add(theTeslaPackage.getPathSelector());
		getPaletteEntryEClass.getESuperTypes().add(theTeslaPackage.getTextSelector());
		getHandleEClass.getESuperTypes().add(theTeslaPackage.getTextSelector());

		// Initialize classes and features; add operations and parameters
		initEClass(getDiagramEClass, GetDiagram.class, "GetDiagram", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getPaletteEClass, GetPalette.class, "GetPalette", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getEditPartEClass, GetEditPart.class, "GetEditPart", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetEditPart_Path(), theEcorePackage.getEString(), "path", null, 0, 1, GetEditPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetEditPart_Name(), theEcorePackage.getEString(), "name", null, 0, 1, GetEditPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetEditPart_Feature(), theEcorePackage.getEString(), "feature", null, 0, 1, GetEditPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetEditPart_ClassName(), theEcorePackage.getEString(), "className", null, 0, 1, GetEditPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetEditPart_Text(), theEcorePackage.getEString(), "text", null, 0, 1, GetEditPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetEditPart_CustomId(), theEcorePackage.getEString(), "customId", null, 0, 1, GetEditPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getFigureEClass, GetFigure.class, "GetFigure", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getEntryEClass, GetEntry.class, "GetEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(directEditEClass, DirectEdit.class, "DirectEdit", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDirectEdit_Control(), theTeslaPackage.getControlHandler(), null, "control", null, 1, 1, DirectEdit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(activateDirectEditEClass, ActivateDirectEdit.class, "ActivateDirectEdit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(commitDirectEditEClass, CommitDirectEdit.class, "CommitDirectEdit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(cancelDirectEditEClass, CancelDirectEdit.class, "CancelDirectEdit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(mouseActionEClass, MouseAction.class, "MouseAction", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMouseAction_Control(), theTeslaPackage.getControlHandler(), null, "control", null, 1, 1, MouseAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouseAction_X(), theEcorePackage.getEIntegerObject(), "x", null, 1, 1, MouseAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouseAction_Y(), theEcorePackage.getEIntegerObject(), "y", null, 1, 1, MouseAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouseAction_Button(), theTeslaPackage.getButton(), "button", null, 0, 1, MouseAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouseAction_Mask(), theEcorePackage.getEString(), "mask", null, 0, 1, MouseAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouseAction_Height(), theEcorePackage.getEIntegerObject(), "height", null, 0, 1, MouseAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouseAction_Width(), theEcorePackage.getEIntegerObject(), "width", null, 0, 1, MouseAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mouseMoveEClass, MouseMove.class, "MouseMove", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(mouseDragEClass, MouseDrag.class, "MouseDrag", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(mousePressEClass, MousePress.class, "MousePress", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(mouseReleaseEClass, MouseRelease.class, "MouseRelease", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(mouseDoubleClickEClass, MouseDoubleClick.class, "MouseDoubleClick", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(mouseEnterEClass, MouseEnter.class, "MouseEnter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(mouseExitEClass, MouseExit.class, "MouseExit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(mouseHoverEClass, MouseHover.class, "MouseHover", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getSourceConnectionEClass, GetSourceConnection.class, "GetSourceConnection", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getTargetConnectionEClass, GetTargetConnection.class, "GetTargetConnection", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getPopupBarItemEClass, GetPopupBarItem.class, "GetPopupBarItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getConnectionHandleEClass, GetConnectionHandle.class, "GetConnectionHandle", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getPaletteEntryEClass, GetPaletteEntry.class, "GetPaletteEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getHandleEClass, GetHandle.class, "GetHandle", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Create annotations
		// http://www.eclipse.org/ecl/docs
		createDocsAnnotations();
		// http://www.eclipse.org/ecl/input
		createInputAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/input</b>.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected void createInputAnnotations() {
		String source = "http://www.eclipse.org/ecl/input";
		addAnnotation
		  (getDirectEdit_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getMouseAction_Control(),
		   source,
		   new String[] {
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/docs</b>.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected void createDocsAnnotations() {
		String source = "http://www.eclipse.org/ecl/docs";
		addAnnotation
		  (getDiagramEClass,
		   source,
		   new String[] {
			   "description", "Gets diagram with specified parameter. If diagram doesn\'t exist, then error is returned.",
			   "returns", "diagram",
			   "recorded", "true",
			   "example", "get-editor Network | get-diagram | get-edit-part -name Network | mouse-hover 139 377 -height 609 -width 1034"
		   });
		addAnnotation
		  (getPaletteEClass,
		   source,
		   new String[] {
			   "description", "Gets palette with specified parameter. If palette doesn\'t exist, then error is returned.",
			   "returns", "palette",
			   "recorded", "true",
			   "example", "get-editor Diagram | get-palette | get-palette-entry CompositeLink | get-property text"
		   });
		addAnnotation
		  (getEditPartEClass,
		   source,
		   new String[] {
			   "description", "Gets EObject part that should be edited",
			   "returns", "Part of EObject",
			   "recorded", "true",
			   "example", "get-editor Network | get-diagram | get-edit-part -name Network | mouse-hover 139 377 -height 609 -width 1034"
		   });
		addAnnotation
		  (getGetEditPart_Path(),
		   source,
		   new String[] {
			   "description", "Path to EObject"
		   });
		addAnnotation
		  (getGetEditPart_Name(),
		   source,
		   new String[] {
			   "description", "Name of EObject"
		   });
		addAnnotation
		  (getFigureEClass,
		   source,
		   new String[] {
			   "description", "Gets figure with specified coordinates. If figure doesn\'t exist, then error is returned.",
			   "returns", "figure",
			   "recorded", "true",
			   "example", "get-editor Diagram | get-palette | get-figure \"0/3/1/0/0/0/0/1/1/0\""
		   });
		addAnnotation
		  (getEntryEClass,
		   source,
		   new String[] {
			   "description", "Gets entry from palette",
			   "returns", "Palette entry",
			   "recorded", "true"
		   });
		addAnnotation
		  (getDirectEdit_Control(),
		   source,
		   new String[] {
			   "description", "Edit part or figure."
		   });
		addAnnotation
		  (activateDirectEditEClass,
		   source,
		   new String[] {
			   "description", "Activates direct editing.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor SO | get-diagram | get-edit-part -className ProcessEditPart | get-edit-part -name Invoke \r\n    | activate-direct-edit"
		   });
		addAnnotation
		  (commitDirectEditEClass,
		   source,
		   new String[] {
			   "description", "Commits direct editing.",
			   "returns", "value of <code>control</code> parameter"
		   });
		addAnnotation
		  (cancelDirectEditEClass,
		   source,
		   new String[] {
			   "description", "Cancels direct editing.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor SO | get-diagram | get-edit-part -className ProcessEditPart | get-edit-part -name Invoke {<br> \r\n&nbsp activate-direct-edit<br>\r\n&nbsp cancel-direct-edit<br>}"
		   });
		addAnnotation
		  (mouseActionEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse actions"
		   });
		addAnnotation
		  (getMouseAction_Control(),
		   source,
		   new String[] {
			   "description", "Canvas, edit part or figure."
		   });
		addAnnotation
		  (getMouseAction_X(),
		   source,
		   new String[] {
			   "description", "X coordinate relative to left top corner of the control."
		   });
		addAnnotation
		  (getMouseAction_Y(),
		   source,
		   new String[] {
			   "description", "Y coordinate relative to left top corner of the control."
		   });
		addAnnotation
		  (getMouseAction_Button(),
		   source,
		   new String[] {
			   "description", "Mouse button pressed. Must be one of followings: none, button1, button2, button3.",
			   "default", "none"
		   });
		addAnnotation
		  (getMouseAction_Mask(),
		   source,
		   new String[] {
			   "default", "0",
			   "description", "Plus-separated pressed metakeys. Example: ALT+SHIFT. Also could be a numeric value of org.eclipse.swt.widgets.Event.stateMask."
		   });
		addAnnotation
		  (getMouseAction_Height(),
		   source,
		   new String[] {
			   "description", "Height of the control.",
			   "default", "0"
		   });
		addAnnotation
		  (getMouseAction_Width(),
		   source,
		   new String[] {
			   "description", "Width of the control.",
			   "default", "0"
		   });
		addAnnotation
		  (mouseMoveEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse move over the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor Test | get-diagram | get-figure \"0/1/0/0/0/1\" | mouse-move 62 167 -height 235 -width 525"
		   });
		addAnnotation
		  (mouseDragEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse drag on the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-editor Test | get-diagram] { <br> &nbsp mouse-press 77 311 button1 -height 689 -width 413 <br> &nbsp mouse-drag 77 311 -height 689 -width 413 <br> }"
		   });
		addAnnotation
		  (mousePressEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse press on the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor Test | get-diagram | mouse-press 77 311 button1 -height 689 -width 413"
		   });
		addAnnotation
		  (mouseReleaseEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse release on the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-editor Test | get-diagram] { <br> &nbsp mouse-press 77 311 button1 -height 689 -width 413 <br> &nbsp mouse-release 77 311 button1 -height 689 -width 413 <br> }"
		   });
		addAnnotation
		  (mouseDoubleClickEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse double click on the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-palette-entry \"Try/Catch/Finally\"] {<br> &nbsp mouse-release 32 18 button1 524288 -height 23 -width 125 <br> &nbsp mouse-press 32 18 button1 -height 23 -width 125 <br> &nbsp mouse-double-click 32 18 button1 -height 23 -width 125 <br> &nbsp mouse-release 32 18 button1 524288 -height 23 -width 125 <br> }"
		   });
		addAnnotation
		  (mouseEnterEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse entering to the control.",
			   "returns", "value of <code>control</code> parameter"
		   });
		addAnnotation
		  (mouseExitEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse leaving from the control.",
			   "returns", "value of <code>control</code> parameter"
		   });
		addAnnotation
		  (mouseHoverEClass,
		   source,
		   new String[] {
			   "description", "Emulates mouse hovering over the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-edit-part \"0/0/1/0/1/0/1/0\"] {<br> &nbsp mouse-release 13 17 button1 524288 -height 22 -width 88<br> &nbsp  mouse-move 14 16 -height 22 -width 88<br> &nbsp mouse-hover 14 16 -height 22 -width 88<br>}"
		   });
		addAnnotation
		  (getSourceConnectionEClass,
		   source,
		   new String[] {
			   "description", "Gets source connection on diagram. ",
			   "returns", "source connection",
			   "recorded", "true"
		   });
		addAnnotation
		  (getTargetConnectionEClass,
		   source,
		   new String[] {
			   "description", "Gets target connection on diagram. ",
			   "returns", "target connection",
			   "example", "get-editor Diagram | get-diagram | get-edit-part -name SO | get-edit-part -name SA | get-target-connection 0 | get-property foregroundColor | equals \"#F49316\" | verify-true",
			   "recorded", "true"
		   });
		addAnnotation
		  (getPopupBarItemEClass,
		   source,
		   new String[] {
			   "description", "Gets pop-up bar of GMF element. If it doesn\'t exist, then error is returned.",
			   "returns", "pop-up bar ",
			   "example", "get-editor Diagram | get-diagram  | get-edit-part -className DiagramEditPart | get-popup-bar-item | mouse-hover 16 9 -height 20 -width 30",
			   "recorded", "true"
		   });
		addAnnotation
		  (getConnectionHandleEClass,
		   source,
		   new String[] {
			   "description", "Gets connection handle on diagram. If the connection handle is not found, the error is returned.",
			   "returns", "connection handle",
			   "example", "get-editor Diagram | get-diagram | get-edit-part -className DiagramEditPart | get-connection-handle 0 | mouse-move 1 6 button1 -height 16 -width 20",
			   "recorded", "true"
		   });
		addAnnotation
		  (getPaletteEntryEClass,
		   source,
		   new String[] {
			   "description", "Gets entry from palette",
			   "returns", "Palette entry",
			   "example", "get-editor SO | get-palette | get-palette-entry Tools | get-figure \"0/0\" | drag-set-data",
			   "recorded", "true"
		   });
		addAnnotation
		  (getHandleEClass,
		   source,
		   new String[] {
			   "description", "Gets specified handle of GMF element. If this handle is not found, the error is returned.",
			   "returns", "handle",
			   "example", "get-editor Diagram | get-diagram | get-edit-part -className DiagramEditPart | get-handle ResizeHandle | mouse-hover 5 5 -height 7 -width 7",
			   "recorded", "true"
		   });
	}

} // DiagramPackageImpl
