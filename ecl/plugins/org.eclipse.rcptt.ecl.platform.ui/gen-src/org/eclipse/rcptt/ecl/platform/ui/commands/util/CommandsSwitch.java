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

package org.eclipse.rcptt.ecl.platform.ui.commands.util;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.rcptt.ecl.core.Command;

import org.eclipse.rcptt.ecl.platform.ui.commands.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage
 * @generated
 */
public class CommandsSwitch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static CommandsPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CommandsSwitch() {
		if (modelPackage == null) {
			modelPackage = CommandsPackage.eINSTANCE;
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	public T doSwitch(EObject theEObject) {
		return doSwitch(theEObject.eClass(), theEObject);
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(EClass theEClass, EObject theEObject) {
		if (theEClass.eContainer() == modelPackage) {
			return doSwitch(theEClass.getClassifierID(), theEObject);
		}
		else {
			List<EClass> eSuperTypes = theEClass.getESuperTypes();
			return
				eSuperTypes.isEmpty() ?
					defaultCase(theEObject) :
					doSwitch(eSuperTypes.get(0), theEObject);
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case CommandsPackage.ALERT: {
				Alert alert = (Alert)theEObject;
				T result = caseAlert(alert);
				if (result == null) result = caseCommand(alert);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.LIST_WORKING_SETS: {
				ListWorkingSets listWorkingSets = (ListWorkingSets)theEObject;
				T result = caseListWorkingSets(listWorkingSets);
				if (result == null) result = caseCommand(listWorkingSets);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.CREATE_WORKING_SET: {
				CreateWorkingSet createWorkingSet = (CreateWorkingSet)theEObject;
				T result = caseCreateWorkingSet(createWorkingSet);
				if (result == null) result = caseCommand(createWorkingSet);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.DELETE_WORKING_SET: {
				DeleteWorkingSet deleteWorkingSet = (DeleteWorkingSet)theEObject;
				T result = caseDeleteWorkingSet(deleteWorkingSet);
				if (result == null) result = caseCommand(deleteWorkingSet);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.GET_WORKING_SET: {
				GetWorkingSet getWorkingSet = (GetWorkingSet)theEObject;
				T result = caseGetWorkingSet(getWorkingSet);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.CLEAR_LOG_VIEW: {
				ClearLogView clearLogView = (ClearLogView)theEObject;
				T result = caseClearLogView(clearLogView);
				if (result == null) result = caseCommand(clearLogView);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.GET_HOTKEY: {
				GetHotkey getHotkey = (GetHotkey)theEObject;
				T result = caseGetHotkey(getHotkey);
				if (result == null) result = caseCommand(getHotkey);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.SAVE_SCREENSHOT: {
				SaveScreenshot saveScreenshot = (SaveScreenshot)theEObject;
				T result = caseSaveScreenshot(saveScreenshot);
				if (result == null) result = caseCommand(saveScreenshot);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.TO_CLIPBOARD: {
				ToClipboard toClipboard = (ToClipboard)theEObject;
				T result = caseToClipboard(toClipboard);
				if (result == null) result = caseCommand(toClipboard);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.FROM_CLIPBOARD: {
				FromClipboard fromClipboard = (FromClipboard)theEObject;
				T result = caseFromClipboard(fromClipboard);
				if (result == null) result = caseCommand(fromClipboard);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case CommandsPackage.EXECUTE_COMMAND: {
				ExecuteCommand executeCommand = (ExecuteCommand)theEObject;
				T result = caseExecuteCommand(executeCommand);
				if (result == null) result = caseCommand(executeCommand);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Alert</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Alert</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAlert(Alert object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>List Working Sets</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>List Working Sets</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseListWorkingSets(ListWorkingSets object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Create Working Set</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Create Working Set</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCreateWorkingSet(CreateWorkingSet object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Delete Working Set</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Delete Working Set</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDeleteWorkingSet(DeleteWorkingSet object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Working Set</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Working Set</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetWorkingSet(GetWorkingSet object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Clear Log View</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Clear Log View</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClearLogView(ClearLogView object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Hotkey</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Hotkey</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetHotkey(GetHotkey object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Save Screenshot</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Save Screenshot</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSaveScreenshot(SaveScreenshot object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>To Clipboard</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>To Clipboard</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseToClipboard(ToClipboard object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>From Clipboard</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>From Clipboard</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFromClipboard(FromClipboard object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Execute Command</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Execute Command</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExecuteCommand(ExecuteCommand object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Command</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Command</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCommand(Command object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	public T defaultCase(EObject object) {
		return null;
	}

} //CommandsSwitch
