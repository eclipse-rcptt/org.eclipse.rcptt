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

import org.eclipse.rcptt.tesla.ecl.model.GetTableData;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get Table Data</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetTableDataImpl#isIncludeChecked <em>Include Checked</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetTableDataImpl#isExcludeHidden <em>Exclude Hidden</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GetTableDataImpl extends ControlCommandImpl implements GetTableData {
	/**
	 * The default value of the '{@link #isIncludeChecked() <em>Include Checked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIncludeChecked()
	 * @generated
	 * @ordered
	 */
	protected static final boolean INCLUDE_CHECKED_EDEFAULT = false;
	/**
	 * The cached value of the '{@link #isIncludeChecked() <em>Include Checked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIncludeChecked()
	 * @generated
	 * @ordered
	 */
	protected boolean includeChecked = INCLUDE_CHECKED_EDEFAULT;

	/**
	 * The default value of the '{@link #isExcludeHidden() <em>Exclude Hidden</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExcludeHidden()
	 * @generated
	 * @ordered
	 */
	protected static final boolean EXCLUDE_HIDDEN_EDEFAULT = false;
	/**
	 * The cached value of the '{@link #isExcludeHidden() <em>Exclude Hidden</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isExcludeHidden()
	 * @generated
	 * @ordered
	 */
	protected boolean excludeHidden = EXCLUDE_HIDDEN_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GetTableDataImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.GET_TABLE_DATA;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIncludeChecked() {
		return includeChecked;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIncludeChecked(boolean newIncludeChecked) {
		boolean oldIncludeChecked = includeChecked;
		includeChecked = newIncludeChecked;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_TABLE_DATA__INCLUDE_CHECKED, oldIncludeChecked, includeChecked));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isExcludeHidden() {
		return excludeHidden;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExcludeHidden(boolean newExcludeHidden) {
		boolean oldExcludeHidden = excludeHidden;
		excludeHidden = newExcludeHidden;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_TABLE_DATA__EXCLUDE_HIDDEN, oldExcludeHidden, excludeHidden));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.GET_TABLE_DATA__INCLUDE_CHECKED:
				return isIncludeChecked();
			case TeslaPackage.GET_TABLE_DATA__EXCLUDE_HIDDEN:
				return isExcludeHidden();
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
			case TeslaPackage.GET_TABLE_DATA__INCLUDE_CHECKED:
				setIncludeChecked((Boolean)newValue);
				return;
			case TeslaPackage.GET_TABLE_DATA__EXCLUDE_HIDDEN:
				setExcludeHidden((Boolean)newValue);
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
			case TeslaPackage.GET_TABLE_DATA__INCLUDE_CHECKED:
				setIncludeChecked(INCLUDE_CHECKED_EDEFAULT);
				return;
			case TeslaPackage.GET_TABLE_DATA__EXCLUDE_HIDDEN:
				setExcludeHidden(EXCLUDE_HIDDEN_EDEFAULT);
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
			case TeslaPackage.GET_TABLE_DATA__INCLUDE_CHECKED:
				return includeChecked != INCLUDE_CHECKED_EDEFAULT;
			case TeslaPackage.GET_TABLE_DATA__EXCLUDE_HIDDEN:
				return excludeHidden != EXCLUDE_HIDDEN_EDEFAULT;
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
		result.append(" (includeChecked: ");
		result.append(includeChecked);
		result.append(", excludeHidden: ");
		result.append(excludeHidden);
		result.append(')');
		return result.toString();
	}

} //GetTableDataImpl
