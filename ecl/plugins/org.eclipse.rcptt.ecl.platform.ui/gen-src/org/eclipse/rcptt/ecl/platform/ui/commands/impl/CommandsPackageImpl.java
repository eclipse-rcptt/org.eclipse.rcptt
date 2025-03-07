/*******************************************************************************
 * Copyright (c) 2014, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.rcptt.ecl.platform.ui.commands.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.rcptt.ecl.core.CorePackage;

import org.eclipse.rcptt.ecl.platform.ui.commands.Alert;
import org.eclipse.rcptt.ecl.platform.ui.commands.ClearLogView;
import org.eclipse.rcptt.ecl.platform.ui.commands.CommandsFactory;
import org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage;
import org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet;
import org.eclipse.rcptt.ecl.platform.ui.commands.DeleteWorkingSet;
import org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand;
import org.eclipse.rcptt.ecl.platform.ui.commands.FromClipboard;
import org.eclipse.rcptt.ecl.platform.ui.commands.GetHotkey;
import org.eclipse.rcptt.ecl.platform.ui.commands.GetWorkingSet;
import org.eclipse.rcptt.ecl.platform.ui.commands.ListWorkingSets;
import org.eclipse.rcptt.ecl.platform.ui.commands.SaveScreenshot;

import org.eclipse.rcptt.ecl.platform.ui.commands.ToClipboard;
import org.eclipse.rcptt.ecl.platform.ui.objects.ObjectsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class CommandsPackageImpl extends EPackageImpl implements CommandsPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass alertEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass listWorkingSetsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass createWorkingSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass deleteWorkingSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getWorkingSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clearLogViewEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getHotkeyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass saveScreenshotEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toClipboardEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass fromClipboardEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass executeCommandEClass = null;

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
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private CommandsPackageImpl() {
		super(eNS_URI, CommandsFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link CommandsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static CommandsPackage init() {
		if (isInited) return (CommandsPackage)EPackage.Registry.INSTANCE.getEPackage(CommandsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredCommandsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		CommandsPackageImpl theCommandsPackage = registeredCommandsPackage instanceof CommandsPackageImpl ? (CommandsPackageImpl)registeredCommandsPackage : new CommandsPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		CorePackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();
		ObjectsPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theCommandsPackage.createPackageContents();

		// Initialize created meta-data
		theCommandsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCommandsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(CommandsPackage.eNS_URI, theCommandsPackage);
		return theCommandsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAlert() {
		return alertEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAlert_Message() {
		return (EAttribute)alertEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getListWorkingSets() {
		return listWorkingSetsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCreateWorkingSet() {
		return createWorkingSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCreateWorkingSet_Name() {
		return (EAttribute)createWorkingSetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCreateWorkingSet_Type() {
		return (EAttribute)createWorkingSetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDeleteWorkingSet() {
		return deleteWorkingSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDeleteWorkingSet_Input() {
		return (EReference)deleteWorkingSetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetWorkingSet() {
		return getWorkingSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetWorkingSet_Name() {
		return (EAttribute)getWorkingSetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClearLogView() {
		return clearLogViewEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetHotkey() {
		return getHotkeyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetHotkey_CommandId() {
		return (EAttribute)getHotkeyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSaveScreenshot() {
		return saveScreenshotEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSaveScreenshot_Out() {
		return (EAttribute)saveScreenshotEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getToClipboard() {
		return toClipboardEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getToClipboard_Input() {
		return (EAttribute)toClipboardEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFromClipboard() {
		return fromClipboardEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExecuteCommand() {
		return executeCommandEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExecuteCommand_CommandId() {
		return (EAttribute)executeCommandEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CommandsFactory getCommandsFactory() {
		return (CommandsFactory)getEFactoryInstance();
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
		alertEClass = createEClass(ALERT);
		createEAttribute(alertEClass, ALERT__MESSAGE);

		listWorkingSetsEClass = createEClass(LIST_WORKING_SETS);

		createWorkingSetEClass = createEClass(CREATE_WORKING_SET);
		createEAttribute(createWorkingSetEClass, CREATE_WORKING_SET__NAME);
		createEAttribute(createWorkingSetEClass, CREATE_WORKING_SET__TYPE);

		deleteWorkingSetEClass = createEClass(DELETE_WORKING_SET);
		createEReference(deleteWorkingSetEClass, DELETE_WORKING_SET__INPUT);

		getWorkingSetEClass = createEClass(GET_WORKING_SET);
		createEAttribute(getWorkingSetEClass, GET_WORKING_SET__NAME);

		clearLogViewEClass = createEClass(CLEAR_LOG_VIEW);

		getHotkeyEClass = createEClass(GET_HOTKEY);
		createEAttribute(getHotkeyEClass, GET_HOTKEY__COMMAND_ID);

		saveScreenshotEClass = createEClass(SAVE_SCREENSHOT);
		createEAttribute(saveScreenshotEClass, SAVE_SCREENSHOT__OUT);

		toClipboardEClass = createEClass(TO_CLIPBOARD);
		createEAttribute(toClipboardEClass, TO_CLIPBOARD__INPUT);

		fromClipboardEClass = createEClass(FROM_CLIPBOARD);

		executeCommandEClass = createEClass(EXECUTE_COMMAND);
		createEAttribute(executeCommandEClass, EXECUTE_COMMAND__COMMAND_ID);
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
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);
		ObjectsPackage theObjectsPackage = (ObjectsPackage)EPackage.Registry.INSTANCE.getEPackage(ObjectsPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		alertEClass.getESuperTypes().add(theCorePackage.getCommand());
		listWorkingSetsEClass.getESuperTypes().add(theCorePackage.getCommand());
		createWorkingSetEClass.getESuperTypes().add(theCorePackage.getCommand());
		deleteWorkingSetEClass.getESuperTypes().add(theCorePackage.getCommand());
		clearLogViewEClass.getESuperTypes().add(theCorePackage.getCommand());
		getHotkeyEClass.getESuperTypes().add(theCorePackage.getCommand());
		saveScreenshotEClass.getESuperTypes().add(theCorePackage.getCommand());
		toClipboardEClass.getESuperTypes().add(theCorePackage.getCommand());
		fromClipboardEClass.getESuperTypes().add(theCorePackage.getCommand());
		executeCommandEClass.getESuperTypes().add(theCorePackage.getCommand());

		// Initialize classes and features; add operations and parameters
		initEClass(alertEClass, Alert.class, "Alert", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAlert_Message(), theEcorePackage.getEString(), "message", null, 0, 1, Alert.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(listWorkingSetsEClass, ListWorkingSets.class, "ListWorkingSets", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(createWorkingSetEClass, CreateWorkingSet.class, "CreateWorkingSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCreateWorkingSet_Name(), theEcorePackage.getEString(), "name", null, 0, 1, CreateWorkingSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCreateWorkingSet_Type(), theEcorePackage.getEString(), "type", "org.eclipse.ui.resourceWorkingSetPage", 0, 1, CreateWorkingSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(deleteWorkingSetEClass, DeleteWorkingSet.class, "DeleteWorkingSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDeleteWorkingSet_Input(), theObjectsPackage.getWorkingSet(), null, "input", null, 0, 1, DeleteWorkingSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getWorkingSetEClass, GetWorkingSet.class, "GetWorkingSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetWorkingSet_Name(), theEcorePackage.getEString(), "name", null, 0, 1, GetWorkingSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(clearLogViewEClass, ClearLogView.class, "ClearLogView", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getHotkeyEClass, GetHotkey.class, "GetHotkey", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetHotkey_CommandId(), theEcorePackage.getEString(), "commandId", null, 0, 1, GetHotkey.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(saveScreenshotEClass, SaveScreenshot.class, "SaveScreenshot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSaveScreenshot_Out(), theEcorePackage.getEString(), "out", null, 0, 1, SaveScreenshot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(toClipboardEClass, ToClipboard.class, "ToClipboard", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getToClipboard_Input(), theEcorePackage.getEString(), "input", null, 0, 1, ToClipboard.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(fromClipboardEClass, FromClipboard.class, "FromClipboard", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(executeCommandEClass, ExecuteCommand.class, "ExecuteCommand", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getExecuteCommand_CommandId(), theEcorePackage.getEString(), "commandId", null, 0, 1, ExecuteCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/ecl/docs
		createDocsAnnotations();
		// http://www.eclipse.org/ecl/input
		createInputAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/docs</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createDocsAnnotations() {
		String source = "http://www.eclipse.org/ecl/docs";
		addAnnotation
		  (listWorkingSetsEClass,
		   source,
		   new String[] {
			   "description", "Returns a list of all working sets available",
			   "returns", "Working set objects",
			   "example", "list-working-sets | foreach {delete-working-set}"
		   });
		addAnnotation
		  (createWorkingSetEClass,
		   source,
		   new String[] {
			   "description", "Creates working set with a given name.",
			   "returns", "Nothing",
			   "example", "create-working-set \"MyWorkingSet\""
		   });
		addAnnotation
		  (getCreateWorkingSet_Name(),
		   source,
		   new String[] {
			   "description", "Working set name"
		   });
		addAnnotation
		  (getCreateWorkingSet_Type(),
		   source,
		   new String[] {
			   "description", "Working set type id. Default value is <code>org.eclipse.ui.resourceWorkingSetPage</code>, which corresponds to Resource Working Set. The short name <code>java</code> can be used to identify type <code>org.eclipse.jdt.ui.JavaWorkingSetPage</code>"
		   });
		addAnnotation
		  (deleteWorkingSetEClass,
		   source,
		   new String[] {
			   "description", "Deletes working set",
			   "returns", "Nothing",
			   "example", "list-working-sets | foreach {delete-working-set}"
		   });
		addAnnotation
		  (getDeleteWorkingSet_Input(),
		   source,
		   new String[] {
			   "description", "Working set object"
		   });
		addAnnotation
		  (getWorkingSetEClass,
		   source,
		   new String[] {
			   "description", "Finds working set by name",
			   "returns", "Working set object"
		   });
		addAnnotation
		  (getGetWorkingSet_Name(),
		   source,
		   new String[] {
			   "description", "Working set name"
		   });
		addAnnotation
		  (clearLogViewEClass,
		   source,
		   new String[] {
			   "description", "Clears error log. If Error log is open, then it is closed before log clearing and then open again.",
			   "returns", "Nothing"
		   });
		addAnnotation
		  (saveScreenshotEClass,
		   source,
		   new String[] {
			   "description", "Saves the screenshot.",
			   "returns", "Nothing",
			   "example", "get-eclipse-window | get-object | save-screenshot [substitute-variables \"${workspace_loc}/images/actual.png\"]"
		   });
		addAnnotation
		  (toClipboardEClass,
		   source,
		   new String[] {
			   "description", "Puts input string to the clipboard.",
			   "returns", "Nothing",
			   "example", "to-clipboard \"Content\""
		   });
		addAnnotation
		  (fromClipboardEClass,
		   source,
		   new String[] {
			   "description", "Gets input string from the clipboard.",
			   "returns", "Return current clipboard content or empty string if no content",
			   "example", "show-alert [from-clipboard]"
		   });
		addAnnotation
		  (executeCommandEClass,
		   source,
		   new String[] {
			   "description", "Executes an Eclipse command. ",
			   "example", "execute-command org.eclipse.ui.help.installationDialog"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/input</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createInputAnnotations() {
		String source = "http://www.eclipse.org/ecl/input";
		addAnnotation
		  (getCreateWorkingSet_Name(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getDeleteWorkingSet_Input(),
		   source,
		   new String[] {
		   });
	}

} //CommandsPackageImpl
