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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

import org.eclipse.rcptt.tesla.ecl.model.ContainsImage;
import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Contains Image</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ContainsImageImpl#getControl <em>Control</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ContainsImageImpl#getImageURI <em>Image URI</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ContainsImageImpl#getRawImage <em>Raw Image</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ContainsImageImpl extends CommandImpl implements ContainsImage {
	/**
	 * The cached value of the '{@link #getControl() <em>Control</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getControl()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler control;

	/**
	 * The default value of the '{@link #getImageURI() <em>Image URI</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getImageURI()
	 * @generated
	 * @ordered
	 */
	protected static final String IMAGE_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getImageURI() <em>Image URI</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getImageURI()
	 * @generated
	 * @ordered
	 */
	protected String imageURI = IMAGE_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getRawImage() <em>Raw Image</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getRawImage()
	 * @generated
	 * @ordered
	 */
	protected static final String RAW_IMAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRawImage() <em>Raw Image</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getRawImage()
	 * @generated
	 * @ordered
	 */
	protected String rawImage = RAW_IMAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected ContainsImageImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.CONTAINS_IMAGE;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getImageURI() {
		return imageURI;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setImageURI(String newImageURI) {
		String oldImageURI = imageURI;
		imageURI = newImageURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTAINS_IMAGE__IMAGE_URI, oldImageURI, imageURI));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRawImage() {
		return rawImage;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRawImage(String newRawImage) {
		String oldRawImage = rawImage;
		rawImage = newRawImage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTAINS_IMAGE__RAW_IMAGE, oldRawImage, rawImage));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ControlHandler getControl() {
		if (control != null && control.eIsProxy()) {
			InternalEObject oldControl = (InternalEObject)control;
			control = (ControlHandler)eResolveProxy(oldControl);
			if (control != oldControl) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.CONTAINS_IMAGE__CONTROL, oldControl, control));
			}
		}
		return control;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler basicGetControl() {
		return control;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setControl(ControlHandler newControl) {
		ControlHandler oldControl = control;
		control = newControl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTAINS_IMAGE__CONTROL, oldControl, control));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.CONTAINS_IMAGE__CONTROL:
				if (resolve) return getControl();
				return basicGetControl();
			case TeslaPackage.CONTAINS_IMAGE__IMAGE_URI:
				return getImageURI();
			case TeslaPackage.CONTAINS_IMAGE__RAW_IMAGE:
				return getRawImage();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TeslaPackage.CONTAINS_IMAGE__CONTROL:
				setControl((ControlHandler)newValue);
				return;
			case TeslaPackage.CONTAINS_IMAGE__IMAGE_URI:
				setImageURI((String)newValue);
				return;
			case TeslaPackage.CONTAINS_IMAGE__RAW_IMAGE:
				setRawImage((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case TeslaPackage.CONTAINS_IMAGE__CONTROL:
				setControl((ControlHandler)null);
				return;
			case TeslaPackage.CONTAINS_IMAGE__IMAGE_URI:
				setImageURI(IMAGE_URI_EDEFAULT);
				return;
			case TeslaPackage.CONTAINS_IMAGE__RAW_IMAGE:
				setRawImage(RAW_IMAGE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case TeslaPackage.CONTAINS_IMAGE__CONTROL:
				return control != null;
			case TeslaPackage.CONTAINS_IMAGE__IMAGE_URI:
				return IMAGE_URI_EDEFAULT == null ? imageURI != null : !IMAGE_URI_EDEFAULT.equals(imageURI);
			case TeslaPackage.CONTAINS_IMAGE__RAW_IMAGE:
				return RAW_IMAGE_EDEFAULT == null ? rawImage != null : !RAW_IMAGE_EDEFAULT.equals(rawImage);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (imageURI: ");
		result.append(imageURI);
		result.append(", rawImage: ");
		result.append(rawImage);
		result.append(')');
		return result.toString();
	}

} // ContainsImageImpl
