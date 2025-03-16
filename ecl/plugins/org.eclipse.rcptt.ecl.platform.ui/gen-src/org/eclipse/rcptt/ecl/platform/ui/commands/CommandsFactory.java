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

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage
 * @generated
 */
public interface CommandsFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	CommandsFactory eINSTANCE = org.eclipse.rcptt.ecl.platform.ui.commands.impl.CommandsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Alert</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Alert</em>'.
	 * @generated
	 */
	Alert createAlert();

	/**
	 * Returns a new object of class '<em>List Working Sets</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>List Working Sets</em>'.
	 * @generated
	 */
	ListWorkingSets createListWorkingSets();

	/**
	 * Returns a new object of class '<em>Create Working Set</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Create Working Set</em>'.
	 * @generated
	 */
	CreateWorkingSet createCreateWorkingSet();

	/**
	 * Returns a new object of class '<em>Delete Working Set</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Delete Working Set</em>'.
	 * @generated
	 */
	DeleteWorkingSet createDeleteWorkingSet();

	/**
	 * Returns a new object of class '<em>Get Working Set</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Get Working Set</em>'.
	 * @generated
	 */
	GetWorkingSet createGetWorkingSet();

	/**
	 * Returns a new object of class '<em>Clear Log View</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Clear Log View</em>'.
	 * @generated
	 */
	ClearLogView createClearLogView();

	/**
	 * Returns a new object of class '<em>Get Hotkey</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Get Hotkey</em>'.
	 * @generated
	 */
	GetHotkey createGetHotkey();

	/**
	 * Returns a new object of class '<em>Save Screenshot</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Save Screenshot</em>'.
	 * @generated
	 */
	SaveScreenshot createSaveScreenshot();

	/**
	 * Returns a new object of class '<em>To Clipboard</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>To Clipboard</em>'.
	 * @generated
	 */
	ToClipboard createToClipboard();

	/**
	 * Returns a new object of class '<em>From Clipboard</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>From Clipboard</em>'.
	 * @generated
	 */
	FromClipboard createFromClipboard();

	/**
	 * Returns a new object of class '<em>Execute Command</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Execute Command</em>'.
	 * @generated
	 */
	ExecuteCommand createExecuteCommand();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	CommandsPackage getCommandsPackage();

} //CommandsFactory
