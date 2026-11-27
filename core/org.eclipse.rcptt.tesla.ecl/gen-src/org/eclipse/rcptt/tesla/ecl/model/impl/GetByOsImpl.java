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

import org.eclipse.rcptt.tesla.ecl.model.GetByOs;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get By Os</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetByOsImpl#getDefault <em>Default</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetByOsImpl#getWin <em>Win</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetByOsImpl#getLinux <em>Linux</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetByOsImpl#getMacosx <em>Macosx</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GetByOsImpl extends CommandImpl implements GetByOs {
	/**
	 * The default value of the '{@link #getDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefault()
	 * @generated
	 * @ordered
	 */
	protected static final String DEFAULT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefault()
	 * @generated
	 * @ordered
	 */
	protected String default_ = DEFAULT_EDEFAULT;

	/**
	 * The default value of the '{@link #getWin() <em>Win</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWin()
	 * @generated
	 * @ordered
	 */
	protected static final String WIN_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getWin() <em>Win</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWin()
	 * @generated
	 * @ordered
	 */
	protected String win = WIN_EDEFAULT;

	/**
	 * The default value of the '{@link #getLinux() <em>Linux</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinux()
	 * @generated
	 * @ordered
	 */
	protected static final String LINUX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLinux() <em>Linux</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinux()
	 * @generated
	 * @ordered
	 */
	protected String linux = LINUX_EDEFAULT;

	/**
	 * The default value of the '{@link #getMacosx() <em>Macosx</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMacosx()
	 * @generated
	 * @ordered
	 */
	protected static final String MACOSX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMacosx() <em>Macosx</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMacosx()
	 * @generated
	 * @ordered
	 */
	protected String macosx = MACOSX_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GetByOsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.GET_BY_OS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDefault() {
		return default_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDefault(String newDefault) {
		String oldDefault = default_;
		default_ = newDefault;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_BY_OS__DEFAULT, oldDefault, default_));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getWin() {
		return win;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setWin(String newWin) {
		String oldWin = win;
		win = newWin;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_BY_OS__WIN, oldWin, win));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLinux() {
		return linux;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLinux(String newLinux) {
		String oldLinux = linux;
		linux = newLinux;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_BY_OS__LINUX, oldLinux, linux));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getMacosx() {
		return macosx;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMacosx(String newMacosx) {
		String oldMacosx = macosx;
		macosx = newMacosx;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_BY_OS__MACOSX, oldMacosx, macosx));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.GET_BY_OS__DEFAULT:
				return getDefault();
			case TeslaPackage.GET_BY_OS__WIN:
				return getWin();
			case TeslaPackage.GET_BY_OS__LINUX:
				return getLinux();
			case TeslaPackage.GET_BY_OS__MACOSX:
				return getMacosx();
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
			case TeslaPackage.GET_BY_OS__DEFAULT:
				setDefault((String)newValue);
				return;
			case TeslaPackage.GET_BY_OS__WIN:
				setWin((String)newValue);
				return;
			case TeslaPackage.GET_BY_OS__LINUX:
				setLinux((String)newValue);
				return;
			case TeslaPackage.GET_BY_OS__MACOSX:
				setMacosx((String)newValue);
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
			case TeslaPackage.GET_BY_OS__DEFAULT:
				setDefault(DEFAULT_EDEFAULT);
				return;
			case TeslaPackage.GET_BY_OS__WIN:
				setWin(WIN_EDEFAULT);
				return;
			case TeslaPackage.GET_BY_OS__LINUX:
				setLinux(LINUX_EDEFAULT);
				return;
			case TeslaPackage.GET_BY_OS__MACOSX:
				setMacosx(MACOSX_EDEFAULT);
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
			case TeslaPackage.GET_BY_OS__DEFAULT:
				return DEFAULT_EDEFAULT == null ? default_ != null : !DEFAULT_EDEFAULT.equals(default_);
			case TeslaPackage.GET_BY_OS__WIN:
				return WIN_EDEFAULT == null ? win != null : !WIN_EDEFAULT.equals(win);
			case TeslaPackage.GET_BY_OS__LINUX:
				return LINUX_EDEFAULT == null ? linux != null : !LINUX_EDEFAULT.equals(linux);
			case TeslaPackage.GET_BY_OS__MACOSX:
				return MACOSX_EDEFAULT == null ? macosx != null : !MACOSX_EDEFAULT.equals(macosx);
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
		result.append(" (default: ");
		result.append(default_);
		result.append(", win: ");
		result.append(win);
		result.append(", linux: ");
		result.append(linux);
		result.append(", macosx: ");
		result.append(macosx);
		result.append(')');
		return result.toString();
	}

} //GetByOsImpl
