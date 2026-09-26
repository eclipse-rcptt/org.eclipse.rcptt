/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 */
package org.eclipse.rcptt.tesla.ecl.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

import org.eclipse.rcptt.tesla.ecl.model.CheckDownloadResult;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Check Download Result</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.CheckDownloadResultImpl#getContentOnBase64 <em>Content On Base64</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.CheckDownloadResultImpl#getFileName <em>File Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CheckDownloadResultImpl extends CommandImpl implements CheckDownloadResult {
	/**
	 * The default value of the '{@link #getContentOnBase64() <em>Content On Base64</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContentOnBase64()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTENT_ON_BASE64_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContentOnBase64() <em>Content On Base64</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContentOnBase64()
	 * @generated
	 * @ordered
	 */
	protected String contentOnBase64 = CONTENT_ON_BASE64_EDEFAULT;

	/**
	 * The default value of the '{@link #getFileName() <em>File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileName()
	 * @generated
	 * @ordered
	 */
	protected static final String FILE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFileName() <em>File Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFileName()
	 * @generated
	 * @ordered
	 */
	protected String fileName = FILE_NAME_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CheckDownloadResultImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.CHECK_DOWNLOAD_RESULT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getContentOnBase64() {
		return contentOnBase64;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContentOnBase64(String newContentOnBase64) {
		String oldContentOnBase64 = contentOnBase64;
		contentOnBase64 = newContentOnBase64;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CHECK_DOWNLOAD_RESULT__CONTENT_ON_BASE64, oldContentOnBase64, contentOnBase64));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFileName() {
		return fileName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFileName(String newFileName) {
		String oldFileName = fileName;
		fileName = newFileName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CHECK_DOWNLOAD_RESULT__FILE_NAME, oldFileName, fileName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.CHECK_DOWNLOAD_RESULT__CONTENT_ON_BASE64:
				return getContentOnBase64();
			case TeslaPackage.CHECK_DOWNLOAD_RESULT__FILE_NAME:
				return getFileName();
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
			case TeslaPackage.CHECK_DOWNLOAD_RESULT__CONTENT_ON_BASE64:
				setContentOnBase64((String)newValue);
				return;
			case TeslaPackage.CHECK_DOWNLOAD_RESULT__FILE_NAME:
				setFileName((String)newValue);
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
			case TeslaPackage.CHECK_DOWNLOAD_RESULT__CONTENT_ON_BASE64:
				setContentOnBase64(CONTENT_ON_BASE64_EDEFAULT);
				return;
			case TeslaPackage.CHECK_DOWNLOAD_RESULT__FILE_NAME:
				setFileName(FILE_NAME_EDEFAULT);
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
			case TeslaPackage.CHECK_DOWNLOAD_RESULT__CONTENT_ON_BASE64:
				return CONTENT_ON_BASE64_EDEFAULT == null ? contentOnBase64 != null : !CONTENT_ON_BASE64_EDEFAULT.equals(contentOnBase64);
			case TeslaPackage.CHECK_DOWNLOAD_RESULT__FILE_NAME:
				return FILE_NAME_EDEFAULT == null ? fileName != null : !FILE_NAME_EDEFAULT.equals(fileName);
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
		result.append(" (contentOnBase64: ");
		result.append(contentOnBase64);
		result.append(", fileName: ");
		result.append(fileName);
		result.append(')');
		return result.toString();
	}

} //CheckDownloadResultImpl
