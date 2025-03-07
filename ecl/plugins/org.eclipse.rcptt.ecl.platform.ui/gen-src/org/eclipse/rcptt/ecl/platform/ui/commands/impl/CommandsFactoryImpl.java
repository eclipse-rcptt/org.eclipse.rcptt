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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.rcptt.ecl.platform.ui.commands.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class CommandsFactoryImpl extends EFactoryImpl implements CommandsFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static CommandsFactory init() {
		try {
			CommandsFactory theCommandsFactory = (CommandsFactory)EPackage.Registry.INSTANCE.getEFactory(CommandsPackage.eNS_URI);
			if (theCommandsFactory != null) {
				return theCommandsFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new CommandsFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CommandsFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case CommandsPackage.ALERT: return createAlert();
			case CommandsPackage.LIST_WORKING_SETS: return createListWorkingSets();
			case CommandsPackage.CREATE_WORKING_SET: return createCreateWorkingSet();
			case CommandsPackage.DELETE_WORKING_SET: return createDeleteWorkingSet();
			case CommandsPackage.GET_WORKING_SET: return createGetWorkingSet();
			case CommandsPackage.CLEAR_LOG_VIEW: return createClearLogView();
			case CommandsPackage.GET_HOTKEY: return createGetHotkey();
			case CommandsPackage.SAVE_SCREENSHOT: return createSaveScreenshot();
			case CommandsPackage.TO_CLIPBOARD: return createToClipboard();
			case CommandsPackage.FROM_CLIPBOARD: return createFromClipboard();
			case CommandsPackage.EXECUTE_COMMAND: return createExecuteCommand();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Alert createAlert() {
		AlertImpl alert = new AlertImpl();
		return alert;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ListWorkingSets createListWorkingSets() {
		ListWorkingSetsImpl listWorkingSets = new ListWorkingSetsImpl();
		return listWorkingSets;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CreateWorkingSet createCreateWorkingSet() {
		CreateWorkingSetImpl createWorkingSet = new CreateWorkingSetImpl();
		return createWorkingSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DeleteWorkingSet createDeleteWorkingSet() {
		DeleteWorkingSetImpl deleteWorkingSet = new DeleteWorkingSetImpl();
		return deleteWorkingSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetWorkingSet createGetWorkingSet() {
		GetWorkingSetImpl getWorkingSet = new GetWorkingSetImpl();
		return getWorkingSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ClearLogView createClearLogView() {
		ClearLogViewImpl clearLogView = new ClearLogViewImpl();
		return clearLogView;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetHotkey createGetHotkey() {
		GetHotkeyImpl getHotkey = new GetHotkeyImpl();
		return getHotkey;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SaveScreenshot createSaveScreenshot() {
		SaveScreenshotImpl saveScreenshot = new SaveScreenshotImpl();
		return saveScreenshot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ToClipboard createToClipboard() {
		ToClipboardImpl toClipboard = new ToClipboardImpl();
		return toClipboard;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FromClipboard createFromClipboard() {
		FromClipboardImpl fromClipboard = new FromClipboardImpl();
		return fromClipboard;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExecuteCommand createExecuteCommand() {
		ExecuteCommandImpl executeCommand = new ExecuteCommandImpl();
		return executeCommand;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CommandsPackage getCommandsPackage() {
		return (CommandsPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static CommandsPackage getPackage() {
		return CommandsPackage.eINSTANCE;
	}

} //CommandsFactoryImpl
