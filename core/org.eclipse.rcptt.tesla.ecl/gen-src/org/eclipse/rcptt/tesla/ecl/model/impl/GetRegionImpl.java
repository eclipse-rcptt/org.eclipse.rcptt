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

import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.GetRegion;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get Region</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getAfter <em>After</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getIndex <em>Index</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getImageURI <em>Image URI</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionImpl#getRawImage <em>Raw Image</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GetRegionImpl extends CommandImpl implements GetRegion {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAfter() <em>After</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAfter()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler after;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected String type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getIndex() <em>Index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndex()
	 * @generated
	 * @ordered
	 */
	protected static final Integer INDEX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIndex() <em>Index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndex()
	 * @generated
	 * @ordered
	 */
	protected Integer index = INDEX_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParent() <em>Parent</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParent()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler parent;

	/**
	 * The default value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected static final int X_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected int x = X_EDEFAULT;

	/**
	 * The default value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected static final int Y_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected int y = Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getImageURI() <em>Image URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageURI()
	 * @generated
	 * @ordered
	 */
	protected static final String IMAGE_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getImageURI() <em>Image URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImageURI()
	 * @generated
	 * @ordered
	 */
	protected String imageURI = IMAGE_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getRawImage() <em>Raw Image</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRawImage()
	 * @generated
	 * @ordered
	 */
	protected static final String RAW_IMAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRawImage() <em>Raw Image</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRawImage()
	 * @generated
	 * @ordered
	 */
	protected String rawImage = RAW_IMAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GetRegionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.GET_REGION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ControlHandler getAfter() {
		if (after != null && after.eIsProxy()) {
			InternalEObject oldAfter = (InternalEObject)after;
			after = (ControlHandler)eResolveProxy(oldAfter);
			if (after != oldAfter) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.GET_REGION__AFTER, oldAfter, after));
			}
		}
		return after;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler basicGetAfter() {
		return after;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAfter(ControlHandler newAfter) {
		ControlHandler oldAfter = after;
		after = newAfter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__AFTER, oldAfter, after));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getIndex() {
		return index;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIndex(Integer newIndex) {
		Integer oldIndex = index;
		index = newIndex;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__INDEX, oldIndex, index));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ControlHandler getParent() {
		if (parent != null && parent.eIsProxy()) {
			InternalEObject oldParent = (InternalEObject)parent;
			parent = (ControlHandler)eResolveProxy(oldParent);
			if (parent != oldParent) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.GET_REGION__PARENT, oldParent, parent));
			}
		}
		return parent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler basicGetParent() {
		return parent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setParent(ControlHandler newParent) {
		ControlHandler oldParent = parent;
		parent = newParent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__PARENT, oldParent, parent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(String newType) {
		String oldType = type;
		type = newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRawImage() {
		return rawImage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRawImage(String newRawImage) {
		String oldRawImage = rawImage;
		rawImage = newRawImage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__RAW_IMAGE, oldRawImage, rawImage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getX() {
		return x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setX(int newX) {
		int oldX = x;
		x = newX;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__X, oldX, x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getY() {
		return y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setY(int newY) {
		int oldY = y;
		y = newY;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__Y, oldY, y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getImageURI() {
		return imageURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setImageURI(String newImageURI) {
		String oldImageURI = imageURI;
		imageURI = newImageURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION__IMAGE_URI, oldImageURI, imageURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.GET_REGION__ID:
				return getId();
			case TeslaPackage.GET_REGION__AFTER:
				if (resolve) return getAfter();
				return basicGetAfter();
			case TeslaPackage.GET_REGION__TYPE:
				return getType();
			case TeslaPackage.GET_REGION__INDEX:
				return getIndex();
			case TeslaPackage.GET_REGION__PARENT:
				if (resolve) return getParent();
				return basicGetParent();
			case TeslaPackage.GET_REGION__X:
				return getX();
			case TeslaPackage.GET_REGION__Y:
				return getY();
			case TeslaPackage.GET_REGION__IMAGE_URI:
				return getImageURI();
			case TeslaPackage.GET_REGION__RAW_IMAGE:
				return getRawImage();
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
			case TeslaPackage.GET_REGION__ID:
				setId((String)newValue);
				return;
			case TeslaPackage.GET_REGION__AFTER:
				setAfter((ControlHandler)newValue);
				return;
			case TeslaPackage.GET_REGION__TYPE:
				setType((String)newValue);
				return;
			case TeslaPackage.GET_REGION__INDEX:
				setIndex((Integer)newValue);
				return;
			case TeslaPackage.GET_REGION__PARENT:
				setParent((ControlHandler)newValue);
				return;
			case TeslaPackage.GET_REGION__X:
				setX((Integer)newValue);
				return;
			case TeslaPackage.GET_REGION__Y:
				setY((Integer)newValue);
				return;
			case TeslaPackage.GET_REGION__IMAGE_URI:
				setImageURI((String)newValue);
				return;
			case TeslaPackage.GET_REGION__RAW_IMAGE:
				setRawImage((String)newValue);
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
			case TeslaPackage.GET_REGION__ID:
				setId(ID_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION__AFTER:
				setAfter((ControlHandler)null);
				return;
			case TeslaPackage.GET_REGION__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION__INDEX:
				setIndex(INDEX_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION__PARENT:
				setParent((ControlHandler)null);
				return;
			case TeslaPackage.GET_REGION__X:
				setX(X_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION__Y:
				setY(Y_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION__IMAGE_URI:
				setImageURI(IMAGE_URI_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION__RAW_IMAGE:
				setRawImage(RAW_IMAGE_EDEFAULT);
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
			case TeslaPackage.GET_REGION__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case TeslaPackage.GET_REGION__AFTER:
				return after != null;
			case TeslaPackage.GET_REGION__TYPE:
				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
			case TeslaPackage.GET_REGION__INDEX:
				return INDEX_EDEFAULT == null ? index != null : !INDEX_EDEFAULT.equals(index);
			case TeslaPackage.GET_REGION__PARENT:
				return parent != null;
			case TeslaPackage.GET_REGION__X:
				return x != X_EDEFAULT;
			case TeslaPackage.GET_REGION__Y:
				return y != Y_EDEFAULT;
			case TeslaPackage.GET_REGION__IMAGE_URI:
				return IMAGE_URI_EDEFAULT == null ? imageURI != null : !IMAGE_URI_EDEFAULT.equals(imageURI);
			case TeslaPackage.GET_REGION__RAW_IMAGE:
				return RAW_IMAGE_EDEFAULT == null ? rawImage != null : !RAW_IMAGE_EDEFAULT.equals(rawImage);
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
		result.append(" (id: ");
		result.append(id);
		result.append(", type: ");
		result.append(type);
		result.append(", index: ");
		result.append(index);
		result.append(", x: ");
		result.append(x);
		result.append(", y: ");
		result.append(y);
		result.append(", imageURI: ");
		result.append(imageURI);
		result.append(", rawImage: ");
		result.append(rawImage);
		result.append(')');
		return result.toString();
	}

} //GetRegionImpl
