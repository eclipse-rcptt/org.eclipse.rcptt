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

import org.eclipse.rcptt.tesla.ecl.model.ReturnFromOsDialog;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Return From Os Dialog</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ReturnFromOsDialogImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ReturnFromOsDialogImpl#getResult <em>Result</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReturnFromOsDialogImpl extends CommandImpl implements ReturnFromOsDialog {
	/**
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final String KIND_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected String kind = KIND_EDEFAULT;

	/**
	 * The cached value of the '{@link #getResult() <em>Result</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResult()
	 * @generated
	 * @ordered
	 */
	protected EList<String> result;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReturnFromOsDialogImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.RETURN_FROM_OS_DIALOG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(String newKind) {
		String oldKind = kind;
		kind = newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.RETURN_FROM_OS_DIALOG__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getResult() {
		if (result == null) {
			result = new EDataTypeUniqueEList<String>(String.class, this, TeslaPackage.RETURN_FROM_OS_DIALOG__RESULT);
		}
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.RETURN_FROM_OS_DIALOG__KIND:
				return getKind();
			case TeslaPackage.RETURN_FROM_OS_DIALOG__RESULT:
				return getResult();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TeslaPackage.RETURN_FROM_OS_DIALOG__KIND:
				setKind((String)newValue);
				return;
			case TeslaPackage.RETURN_FROM_OS_DIALOG__RESULT:
				getResult().clear();
				getResult().addAll((Collection<? extends String>)newValue);
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
			case TeslaPackage.RETURN_FROM_OS_DIALOG__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case TeslaPackage.RETURN_FROM_OS_DIALOG__RESULT:
				getResult().clear();
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
			case TeslaPackage.RETURN_FROM_OS_DIALOG__KIND:
				return KIND_EDEFAULT == null ? kind != null : !KIND_EDEFAULT.equals(kind);
			case TeslaPackage.RETURN_FROM_OS_DIALOG__RESULT:
				return result != null && !result.isEmpty();
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
		result.append(" (kind: ");
		result.append(kind);
		result.append(", result: ");
		result.append(result);
		result.append(')');
		return result.toString();
	}

} //ReturnFromOsDialogImpl
