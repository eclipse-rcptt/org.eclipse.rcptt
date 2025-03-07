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

package org.eclipse.rcptt.ecl.platform.ui.commands;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.rcptt.ecl.core.CorePackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsFactory
 * @model kind="package"
 * @generated
 */
public interface CommandsPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "commands";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/ecl/platform.ui/commands.ecore";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "org.eclipse.rcptt.ecl.platform.ui";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	CommandsPackage eINSTANCE = org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.AlertImpl <em>Alert</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.AlertImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getAlert()
	 * @generated
	 */
	int ALERT = 0;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALERT__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALERT__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALERT__MESSAGE = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Alert</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ALERT_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ListWorkingSetsImpl <em>List Working Sets</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.ListWorkingSetsImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getListWorkingSets()
	 * @generated
	 */
	int LIST_WORKING_SETS = 1;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LIST_WORKING_SETS__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LIST_WORKING_SETS__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The number of structural features of the '<em>List Working Sets</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LIST_WORKING_SETS_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.CreateWorkingSetImpl <em>Create Working Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CreateWorkingSetImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getCreateWorkingSet()
	 * @generated
	 */
	int CREATE_WORKING_SET = 2;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_WORKING_SET__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_WORKING_SET__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_WORKING_SET__NAME = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_WORKING_SET__TYPE = CorePackage.COMMAND_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Create Working Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CREATE_WORKING_SET_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.DeleteWorkingSetImpl <em>Delete Working Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.DeleteWorkingSetImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getDeleteWorkingSet()
	 * @generated
	 */
	int DELETE_WORKING_SET = 3;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DELETE_WORKING_SET__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DELETE_WORKING_SET__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The feature id for the '<em><b>Input</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DELETE_WORKING_SET__INPUT = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Delete Working Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DELETE_WORKING_SET_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.GetWorkingSetImpl <em>Get Working Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.GetWorkingSetImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getGetWorkingSet()
	 * @generated
	 */
	int GET_WORKING_SET = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GET_WORKING_SET__NAME = 0;

	/**
	 * The number of structural features of the '<em>Get Working Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GET_WORKING_SET_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ClearLogViewImpl <em>Clear Log View</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.ClearLogViewImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getClearLogView()
	 * @generated
	 */
	int CLEAR_LOG_VIEW = 5;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLEAR_LOG_VIEW__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLEAR_LOG_VIEW__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The number of structural features of the '<em>Clear Log View</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLEAR_LOG_VIEW_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.GetHotkeyImpl <em>Get Hotkey</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.GetHotkeyImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getGetHotkey()
	 * @generated
	 */
	int GET_HOTKEY = 6;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GET_HOTKEY__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GET_HOTKEY__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The feature id for the '<em><b>Command Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GET_HOTKEY__COMMAND_ID = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Get Hotkey</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GET_HOTKEY_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.SaveScreenshotImpl <em>Save Screenshot</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.SaveScreenshotImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getSaveScreenshot()
	 * @generated
	 */
	int SAVE_SCREENSHOT = 7;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAVE_SCREENSHOT__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAVE_SCREENSHOT__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The feature id for the '<em><b>Out</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAVE_SCREENSHOT__OUT = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Save Screenshot</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SAVE_SCREENSHOT_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 1;


	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ToClipboardImpl <em>To Clipboard</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.ToClipboardImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getToClipboard()
	 * @generated
	 */
	int TO_CLIPBOARD = 8;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TO_CLIPBOARD__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TO_CLIPBOARD__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The feature id for the '<em><b>Input</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TO_CLIPBOARD__INPUT = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>To Clipboard</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TO_CLIPBOARD_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 1;


	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.FromClipboardImpl <em>From Clipboard</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.FromClipboardImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getFromClipboard()
	 * @generated
	 */
	int FROM_CLIPBOARD = 9;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FROM_CLIPBOARD__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FROM_CLIPBOARD__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The number of structural features of the '<em>From Clipboard</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FROM_CLIPBOARD_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ExecuteCommandImpl <em>Execute Command</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.ExecuteCommandImpl
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getExecuteCommand()
	 * @generated
	 */
	int EXECUTE_COMMAND = 10;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXECUTE_COMMAND__HOST = CorePackage.COMMAND__HOST;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXECUTE_COMMAND__BINDINGS = CorePackage.COMMAND__BINDINGS;

	/**
	 * The feature id for the '<em><b>Command Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXECUTE_COMMAND__COMMAND_ID = CorePackage.COMMAND_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Execute Command</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXECUTE_COMMAND_FEATURE_COUNT = CorePackage.COMMAND_FEATURE_COUNT + 1;

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.Alert <em>Alert</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Alert</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.Alert
	 * @generated
	 */
	EClass getAlert();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.rcptt.ecl.platform.ui.commands.Alert#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.Alert#getMessage()
	 * @see #getAlert()
	 * @generated
	 */
	EAttribute getAlert_Message();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.ListWorkingSets <em>List Working Sets</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>List Working Sets</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.ListWorkingSets
	 * @generated
	 */
	EClass getListWorkingSets();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet <em>Create Working Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Create Working Set</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet
	 * @generated
	 */
	EClass getCreateWorkingSet();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet#getName()
	 * @see #getCreateWorkingSet()
	 * @generated
	 */
	EAttribute getCreateWorkingSet_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet#getType()
	 * @see #getCreateWorkingSet()
	 * @generated
	 */
	EAttribute getCreateWorkingSet_Type();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.DeleteWorkingSet <em>Delete Working Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Delete Working Set</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.DeleteWorkingSet
	 * @generated
	 */
	EClass getDeleteWorkingSet();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.rcptt.ecl.platform.ui.commands.DeleteWorkingSet#getInput <em>Input</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Input</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.DeleteWorkingSet#getInput()
	 * @see #getDeleteWorkingSet()
	 * @generated
	 */
	EReference getDeleteWorkingSet_Input();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.GetWorkingSet <em>Get Working Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Get Working Set</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.GetWorkingSet
	 * @generated
	 */
	EClass getGetWorkingSet();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.rcptt.ecl.platform.ui.commands.GetWorkingSet#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.GetWorkingSet#getName()
	 * @see #getGetWorkingSet()
	 * @generated
	 */
	EAttribute getGetWorkingSet_Name();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.ClearLogView <em>Clear Log View</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Clear Log View</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.ClearLogView
	 * @generated
	 */
	EClass getClearLogView();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.GetHotkey <em>Get Hotkey</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Get Hotkey</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.GetHotkey
	 * @generated
	 */
	EClass getGetHotkey();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.rcptt.ecl.platform.ui.commands.GetHotkey#getCommandId <em>Command Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Command Id</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.GetHotkey#getCommandId()
	 * @see #getGetHotkey()
	 * @generated
	 */
	EAttribute getGetHotkey_CommandId();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.SaveScreenshot <em>Save Screenshot</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Save Screenshot</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.SaveScreenshot
	 * @generated
	 */
	EClass getSaveScreenshot();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.rcptt.ecl.platform.ui.commands.SaveScreenshot#getOut <em>Out</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Out</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.SaveScreenshot#getOut()
	 * @see #getSaveScreenshot()
	 * @generated
	 */
	EAttribute getSaveScreenshot_Out();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.ToClipboard <em>To Clipboard</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>To Clipboard</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.ToClipboard
	 * @generated
	 */
	EClass getToClipboard();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.rcptt.ecl.platform.ui.commands.ToClipboard#getInput <em>Input</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Input</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.ToClipboard#getInput()
	 * @see #getToClipboard()
	 * @generated
	 */
	EAttribute getToClipboard_Input();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.FromClipboard <em>From Clipboard</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>From Clipboard</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.FromClipboard
	 * @generated
	 */
	EClass getFromClipboard();

	/**
	 * Returns the meta object for class '{@link org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand <em>Execute Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Execute Command</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand
	 * @generated
	 */
	EClass getExecuteCommand();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand#getCommandId <em>Command Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Command Id</em>'.
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand#getCommandId()
	 * @see #getExecuteCommand()
	 * @generated
	 */
	EAttribute getExecuteCommand_CommandId();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	CommandsFactory getCommandsFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.AlertImpl <em>Alert</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.AlertImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getAlert()
		 * @generated
		 */
		EClass ALERT = eINSTANCE.getAlert();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ALERT__MESSAGE = eINSTANCE.getAlert_Message();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ListWorkingSetsImpl <em>List Working Sets</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.ListWorkingSetsImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getListWorkingSets()
		 * @generated
		 */
		EClass LIST_WORKING_SETS = eINSTANCE.getListWorkingSets();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.CreateWorkingSetImpl <em>Create Working Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CreateWorkingSetImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getCreateWorkingSet()
		 * @generated
		 */
		EClass CREATE_WORKING_SET = eINSTANCE.getCreateWorkingSet();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CREATE_WORKING_SET__NAME = eINSTANCE.getCreateWorkingSet_Name();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CREATE_WORKING_SET__TYPE = eINSTANCE.getCreateWorkingSet_Type();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.DeleteWorkingSetImpl <em>Delete Working Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.DeleteWorkingSetImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getDeleteWorkingSet()
		 * @generated
		 */
		EClass DELETE_WORKING_SET = eINSTANCE.getDeleteWorkingSet();

		/**
		 * The meta object literal for the '<em><b>Input</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DELETE_WORKING_SET__INPUT = eINSTANCE.getDeleteWorkingSet_Input();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.GetWorkingSetImpl <em>Get Working Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.GetWorkingSetImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getGetWorkingSet()
		 * @generated
		 */
		EClass GET_WORKING_SET = eINSTANCE.getGetWorkingSet();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GET_WORKING_SET__NAME = eINSTANCE.getGetWorkingSet_Name();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ClearLogViewImpl <em>Clear Log View</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.ClearLogViewImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getClearLogView()
		 * @generated
		 */
		EClass CLEAR_LOG_VIEW = eINSTANCE.getClearLogView();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.GetHotkeyImpl <em>Get Hotkey</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.GetHotkeyImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getGetHotkey()
		 * @generated
		 */
		EClass GET_HOTKEY = eINSTANCE.getGetHotkey();

		/**
		 * The meta object literal for the '<em><b>Command Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GET_HOTKEY__COMMAND_ID = eINSTANCE.getGetHotkey_CommandId();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.SaveScreenshotImpl <em>Save Screenshot</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.SaveScreenshotImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getSaveScreenshot()
		 * @generated
		 */
		EClass SAVE_SCREENSHOT = eINSTANCE.getSaveScreenshot();

		/**
		 * The meta object literal for the '<em><b>Out</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SAVE_SCREENSHOT__OUT = eINSTANCE.getSaveScreenshot_Out();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ToClipboardImpl <em>To Clipboard</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.ToClipboardImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getToClipboard()
		 * @generated
		 */
		EClass TO_CLIPBOARD = eINSTANCE.getToClipboard();

		/**
		 * The meta object literal for the '<em><b>Input</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TO_CLIPBOARD__INPUT = eINSTANCE.getToClipboard_Input();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.FromClipboardImpl <em>From Clipboard</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.FromClipboardImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getFromClipboard()
		 * @generated
		 */
		EClass FROM_CLIPBOARD = eINSTANCE.getFromClipboard();

		/**
		 * The meta object literal for the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ExecuteCommandImpl <em>Execute Command</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.ExecuteCommandImpl
		 * @see org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsPackageImpl#getExecuteCommand()
		 * @generated
		 */
		EClass EXECUTE_COMMAND = eINSTANCE.getExecuteCommand();

		/**
		 * The meta object literal for the '<em><b>Command Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXECUTE_COMMAND__COMMAND_ID = eINSTANCE.getExecuteCommand_CommandId();

	}

} //CommandsPackage
