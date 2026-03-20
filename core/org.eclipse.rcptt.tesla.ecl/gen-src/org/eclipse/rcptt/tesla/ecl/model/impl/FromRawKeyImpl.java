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

import org.eclipse.rcptt.tesla.ecl.model.FromRawKey;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>From Raw Key</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.FromRawKeyImpl#getMask <em>Mask</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.FromRawKeyImpl#getKeyCode <em>Key Code</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.FromRawKeyImpl#getMeta <em>Meta</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FromRawKeyImpl extends CommandImpl implements FromRawKey {
	/**
	 * The default value of the '{@link #getMask() <em>Mask</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMask()
	 * @generated
	 * @ordered
	 */
	protected static final Integer MASK_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMask() <em>Mask</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMask()
	 * @generated
	 * @ordered
	 */
	protected Integer mask = MASK_EDEFAULT;

	/**
	 * The default value of the '{@link #getKeyCode() <em>Key Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeyCode()
	 * @generated
	 * @ordered
	 */
	protected static final Integer KEY_CODE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getKeyCode() <em>Key Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeyCode()
	 * @generated
	 * @ordered
	 */
	protected Integer keyCode = KEY_CODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getMeta() <em>Meta</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMeta()
	 * @generated
	 * @ordered
	 */
	protected static final Integer META_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMeta() <em>Meta</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMeta()
	 * @generated
	 * @ordered
	 */
	protected Integer meta = META_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FromRawKeyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.FROM_RAW_KEY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getMask() {
		return mask;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMask(Integer newMask) {
		Integer oldMask = mask;
		mask = newMask;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.FROM_RAW_KEY__MASK, oldMask, mask));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getKeyCode() {
		return keyCode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKeyCode(Integer newKeyCode) {
		Integer oldKeyCode = keyCode;
		keyCode = newKeyCode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.FROM_RAW_KEY__KEY_CODE, oldKeyCode, keyCode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getMeta() {
		return meta;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMeta(Integer newMeta) {
		Integer oldMeta = meta;
		meta = newMeta;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.FROM_RAW_KEY__META, oldMeta, meta));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.FROM_RAW_KEY__MASK:
				return getMask();
			case TeslaPackage.FROM_RAW_KEY__KEY_CODE:
				return getKeyCode();
			case TeslaPackage.FROM_RAW_KEY__META:
				return getMeta();
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
			case TeslaPackage.FROM_RAW_KEY__MASK:
				setMask((Integer)newValue);
				return;
			case TeslaPackage.FROM_RAW_KEY__KEY_CODE:
				setKeyCode((Integer)newValue);
				return;
			case TeslaPackage.FROM_RAW_KEY__META:
				setMeta((Integer)newValue);
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
			case TeslaPackage.FROM_RAW_KEY__MASK:
				setMask(MASK_EDEFAULT);
				return;
			case TeslaPackage.FROM_RAW_KEY__KEY_CODE:
				setKeyCode(KEY_CODE_EDEFAULT);
				return;
			case TeslaPackage.FROM_RAW_KEY__META:
				setMeta(META_EDEFAULT);
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
			case TeslaPackage.FROM_RAW_KEY__MASK:
				return MASK_EDEFAULT == null ? mask != null : !MASK_EDEFAULT.equals(mask);
			case TeslaPackage.FROM_RAW_KEY__KEY_CODE:
				return KEY_CODE_EDEFAULT == null ? keyCode != null : !KEY_CODE_EDEFAULT.equals(keyCode);
			case TeslaPackage.FROM_RAW_KEY__META:
				return META_EDEFAULT == null ? meta != null : !META_EDEFAULT.equals(meta);
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
		result.append(" (mask: ");
		result.append(mask);
		result.append(", keyCode: ");
		result.append(keyCode);
		result.append(", meta: ");
		result.append(meta);
		result.append(')');
		return result.toString();
	}

} //FromRawKeyImpl
