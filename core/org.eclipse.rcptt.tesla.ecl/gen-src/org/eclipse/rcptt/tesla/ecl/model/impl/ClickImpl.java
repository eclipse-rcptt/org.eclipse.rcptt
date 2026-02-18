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

import org.eclipse.rcptt.tesla.ecl.model.Click;
import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Click</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ClickImpl#getControl <em>Control</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ClickImpl#isNowait <em>Nowait</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ClickImpl#isDefault <em>Default</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ClickImpl#isArrow <em>Arrow</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ClickImpl#getMetaKeys <em>Meta Keys</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ClickImpl extends CommandImpl implements Click {
	/**
	 * The cached value of the '{@link #getControl() <em>Control</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getControl()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler control;

	/**
	 * The default value of the '{@link #isNowait() <em>Nowait</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNowait()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NOWAIT_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isNowait() <em>Nowait</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNowait()
	 * @generated
	 * @ordered
	 */
	protected boolean nowait = NOWAIT_EDEFAULT;

	/**
	 * The default value of the '{@link #isDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDefault()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DEFAULT_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDefault()
	 * @generated
	 * @ordered
	 */
	protected boolean default_ = DEFAULT_EDEFAULT;

	/**
	 * The default value of the '{@link #isArrow() <em>Arrow</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isArrow()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ARROW_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isArrow() <em>Arrow</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isArrow()
	 * @generated
	 * @ordered
	 */
	protected boolean arrow = ARROW_EDEFAULT;

	/**
	 * The default value of the '{@link #getMetaKeys() <em>Meta Keys</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMetaKeys()
	 * @generated
	 * @ordered
	 */
	protected static final String META_KEYS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMetaKeys() <em>Meta Keys</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMetaKeys()
	 * @generated
	 * @ordered
	 */
	protected String metaKeys = META_KEYS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ClickImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.CLICK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ControlHandler getControl() {
		if (control != null && control.eIsProxy()) {
			InternalEObject oldControl = (InternalEObject)control;
			control = (ControlHandler)eResolveProxy(oldControl);
			if (control != oldControl) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.CLICK__CONTROL, oldControl, control));
			}
		}
		return control;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler basicGetControl() {
		return control;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setControl(ControlHandler newControl) {
		ControlHandler oldControl = control;
		control = newControl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CLICK__CONTROL, oldControl, control));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isNowait() {
		return nowait;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNowait(boolean newNowait) {
		boolean oldNowait = nowait;
		nowait = newNowait;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CLICK__NOWAIT, oldNowait, nowait));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isDefault() {
		return default_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDefault(boolean newDefault) {
		boolean oldDefault = default_;
		default_ = newDefault;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CLICK__DEFAULT, oldDefault, default_));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isArrow() {
		return arrow;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setArrow(boolean newArrow) {
		boolean oldArrow = arrow;
		arrow = newArrow;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CLICK__ARROW, oldArrow, arrow));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getMetaKeys() {
		return metaKeys;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMetaKeys(String newMetaKeys) {
		String oldMetaKeys = metaKeys;
		metaKeys = newMetaKeys;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CLICK__META_KEYS, oldMetaKeys, metaKeys));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.CLICK__CONTROL:
				if (resolve) return getControl();
				return basicGetControl();
			case TeslaPackage.CLICK__NOWAIT:
				return isNowait();
			case TeslaPackage.CLICK__DEFAULT:
				return isDefault();
			case TeslaPackage.CLICK__ARROW:
				return isArrow();
			case TeslaPackage.CLICK__META_KEYS:
				return getMetaKeys();
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
			case TeslaPackage.CLICK__CONTROL:
				setControl((ControlHandler)newValue);
				return;
			case TeslaPackage.CLICK__NOWAIT:
				setNowait((Boolean)newValue);
				return;
			case TeslaPackage.CLICK__DEFAULT:
				setDefault((Boolean)newValue);
				return;
			case TeslaPackage.CLICK__ARROW:
				setArrow((Boolean)newValue);
				return;
			case TeslaPackage.CLICK__META_KEYS:
				setMetaKeys((String)newValue);
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
			case TeslaPackage.CLICK__CONTROL:
				setControl((ControlHandler)null);
				return;
			case TeslaPackage.CLICK__NOWAIT:
				setNowait(NOWAIT_EDEFAULT);
				return;
			case TeslaPackage.CLICK__DEFAULT:
				setDefault(DEFAULT_EDEFAULT);
				return;
			case TeslaPackage.CLICK__ARROW:
				setArrow(ARROW_EDEFAULT);
				return;
			case TeslaPackage.CLICK__META_KEYS:
				setMetaKeys(META_KEYS_EDEFAULT);
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
			case TeslaPackage.CLICK__CONTROL:
				return control != null;
			case TeslaPackage.CLICK__NOWAIT:
				return nowait != NOWAIT_EDEFAULT;
			case TeslaPackage.CLICK__DEFAULT:
				return default_ != DEFAULT_EDEFAULT;
			case TeslaPackage.CLICK__ARROW:
				return arrow != ARROW_EDEFAULT;
			case TeslaPackage.CLICK__META_KEYS:
				return META_KEYS_EDEFAULT == null ? metaKeys != null : !META_KEYS_EDEFAULT.equals(metaKeys);
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
		result.append(" (nowait: ");
		result.append(nowait);
		result.append(", default: ");
		result.append(default_);
		result.append(", arrow: ");
		result.append(arrow);
		result.append(", metaKeys: ");
		result.append(metaKeys);
		result.append(')');
		return result.toString();
	}

} //ClickImpl
