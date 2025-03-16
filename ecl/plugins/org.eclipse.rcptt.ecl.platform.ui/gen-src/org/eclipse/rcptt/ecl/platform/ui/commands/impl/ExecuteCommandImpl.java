/**
 */
package org.eclipse.rcptt.ecl.platform.ui.commands.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

import org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage;
import org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Execute Command</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.ui.commands.impl.ExecuteCommandImpl#getCommandId <em>Command Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ExecuteCommandImpl extends CommandImpl implements ExecuteCommand {
	/**
	 * The default value of the '{@link #getCommandId() <em>Command Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommandId()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMAND_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCommandId() <em>Command Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommandId()
	 * @generated
	 * @ordered
	 */
	protected String commandId = COMMAND_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ExecuteCommandImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CommandsPackage.Literals.EXECUTE_COMMAND;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCommandId() {
		return commandId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCommandId(String newCommandId) {
		String oldCommandId = commandId;
		commandId = newCommandId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackage.EXECUTE_COMMAND__COMMAND_ID, oldCommandId, commandId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case CommandsPackage.EXECUTE_COMMAND__COMMAND_ID:
				return getCommandId();
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
			case CommandsPackage.EXECUTE_COMMAND__COMMAND_ID:
				setCommandId((String)newValue);
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
			case CommandsPackage.EXECUTE_COMMAND__COMMAND_ID:
				setCommandId(COMMAND_ID_EDEFAULT);
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
			case CommandsPackage.EXECUTE_COMMAND__COMMAND_ID:
				return COMMAND_ID_EDEFAULT == null ? commandId != null : !COMMAND_ID_EDEFAULT.equals(commandId);
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
		result.append(" (commandId: ");
		result.append(commandId);
		result.append(')');
		return result.toString();
	}

} //ExecuteCommandImpl
