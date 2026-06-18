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
package org.eclipse.rcptt.tesla.ecl.model.impl;

import org.eclipse.rcptt.tesla.ecl.model.ExecWithOptions;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;



import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Exec With Options</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ExecWithOptionsImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ExecWithOptionsImpl#isAllowStatusDialog <em>Allow Status Dialog</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ExecWithOptionsImpl#isDisableJobWaiting <em>Disable Job Waiting</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ExecWithOptionsImpl extends CommandImpl implements ExecWithOptions {
	/**
	 * The cached value of the '{@link #getCommand() <em>Command</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommand()
	 * @generated
	 * @ordered
	 */
	protected Command command;

	/**
	 * The default value of the '{@link #isAllowStatusDialog() <em>Allow Status Dialog</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowStatusDialog()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ALLOW_STATUS_DIALOG_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAllowStatusDialog() <em>Allow Status Dialog</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAllowStatusDialog()
	 * @generated
	 * @ordered
	 */
	protected boolean allowStatusDialog = ALLOW_STATUS_DIALOG_EDEFAULT;

	/**
	 * The default value of the '{@link #isDisableJobWaiting() <em>Disable Job Waiting</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDisableJobWaiting()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DISABLE_JOB_WAITING_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDisableJobWaiting() <em>Disable Job Waiting</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDisableJobWaiting()
	 * @generated
	 * @ordered
	 */
	protected boolean disableJobWaiting = DISABLE_JOB_WAITING_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ExecWithOptionsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.EXEC_WITH_OPTIONS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAllowStatusDialog() {
		return allowStatusDialog;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAllowStatusDialog(boolean newAllowStatusDialog) {
		boolean oldAllowStatusDialog = allowStatusDialog;
		allowStatusDialog = newAllowStatusDialog;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.EXEC_WITH_OPTIONS__ALLOW_STATUS_DIALOG, oldAllowStatusDialog, allowStatusDialog));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isDisableJobWaiting() {
		return disableJobWaiting;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDisableJobWaiting(boolean newDisableJobWaiting) {
		boolean oldDisableJobWaiting = disableJobWaiting;
		disableJobWaiting = newDisableJobWaiting;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.EXEC_WITH_OPTIONS__DISABLE_JOB_WAITING, oldDisableJobWaiting, disableJobWaiting));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Command getCommand() {
		if (command != null && command.eIsProxy()) {
			InternalEObject oldCommand = (InternalEObject)command;
			command = (Command)eResolveProxy(oldCommand);
			if (command != oldCommand) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.EXEC_WITH_OPTIONS__COMMAND, oldCommand, command));
			}
		}
		return command;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Command basicGetCommand() {
		return command;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCommand(Command newCommand) {
		Command oldCommand = command;
		command = newCommand;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.EXEC_WITH_OPTIONS__COMMAND, oldCommand, command));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.EXEC_WITH_OPTIONS__COMMAND:
				if (resolve) return getCommand();
				return basicGetCommand();
			case TeslaPackage.EXEC_WITH_OPTIONS__ALLOW_STATUS_DIALOG:
				return isAllowStatusDialog();
			case TeslaPackage.EXEC_WITH_OPTIONS__DISABLE_JOB_WAITING:
				return isDisableJobWaiting();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TeslaPackage.EXEC_WITH_OPTIONS__COMMAND:
				setCommand((Command)newValue);
				return;
			case TeslaPackage.EXEC_WITH_OPTIONS__ALLOW_STATUS_DIALOG:
				setAllowStatusDialog((Boolean)newValue);
				return;
			case TeslaPackage.EXEC_WITH_OPTIONS__DISABLE_JOB_WAITING:
				setDisableJobWaiting((Boolean)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case TeslaPackage.EXEC_WITH_OPTIONS__COMMAND:
				setCommand((Command)null);
				return;
			case TeslaPackage.EXEC_WITH_OPTIONS__ALLOW_STATUS_DIALOG:
				setAllowStatusDialog(ALLOW_STATUS_DIALOG_EDEFAULT);
				return;
			case TeslaPackage.EXEC_WITH_OPTIONS__DISABLE_JOB_WAITING:
				setDisableJobWaiting(DISABLE_JOB_WAITING_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case TeslaPackage.EXEC_WITH_OPTIONS__COMMAND:
				return command != null;
			case TeslaPackage.EXEC_WITH_OPTIONS__ALLOW_STATUS_DIALOG:
				return allowStatusDialog != ALLOW_STATUS_DIALOG_EDEFAULT;
			case TeslaPackage.EXEC_WITH_OPTIONS__DISABLE_JOB_WAITING:
				return disableJobWaiting != DISABLE_JOB_WAITING_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (allowStatusDialog: ");
		result.append(allowStatusDialog);
		result.append(", disableJobWaiting: ");
		result.append(disableJobWaiting);
		result.append(')');
		return result.toString();
	}

} //ExecWithOptionsImpl
