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
import org.eclipse.rcptt.tesla.ecl.model.GetRegionText;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get Region Text</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionTextImpl#getControl <em>Control</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionTextImpl#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionTextImpl#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionTextImpl#getSx <em>Sx</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionTextImpl#getSy <em>Sy</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionTextImpl#getWidth <em>Width</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetRegionTextImpl#getHeight <em>Height</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GetRegionTextImpl extends CommandImpl implements GetRegionText {
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
	 * The default value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected static final Integer X_EDEFAULT = new Integer(0);

	/**
	 * The cached value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected Integer x = X_EDEFAULT;

	/**
	 * The default value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected static final Integer Y_EDEFAULT = new Integer(0);

	/**
	 * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected Integer y = Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getSx() <em>Sx</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSx()
	 * @generated
	 * @ordered
	 */
	protected static final Integer SX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSx() <em>Sx</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSx()
	 * @generated
	 * @ordered
	 */
	protected Integer sx = SX_EDEFAULT;

	/**
	 * The default value of the '{@link #getSy() <em>Sy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSy()
	 * @generated
	 * @ordered
	 */
	protected static final Integer SY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSy() <em>Sy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSy()
	 * @generated
	 * @ordered
	 */
	protected Integer sy = SY_EDEFAULT;

	/**
	 * The default value of the '{@link #getWidth() <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidth()
	 * @generated
	 * @ordered
	 */
	protected static final Integer WIDTH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getWidth() <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidth()
	 * @generated
	 * @ordered
	 */
	protected Integer width = WIDTH_EDEFAULT;

	/**
	 * The default value of the '{@link #getHeight() <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeight()
	 * @generated
	 * @ordered
	 */
	protected static final Integer HEIGHT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getHeight() <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeight()
	 * @generated
	 * @ordered
	 */
	protected Integer height = HEIGHT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GetRegionTextImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.GET_REGION_TEXT;
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.GET_REGION_TEXT__CONTROL, oldControl, control));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION_TEXT__CONTROL, oldControl, control));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getX() {
		return x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setX(Integer newX) {
		Integer oldX = x;
		x = newX;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION_TEXT__X, oldX, x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getY() {
		return y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setY(Integer newY) {
		Integer oldY = y;
		y = newY;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION_TEXT__Y, oldY, y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getSx() {
		return sx;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSx(Integer newSx) {
		Integer oldSx = sx;
		sx = newSx;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION_TEXT__SX, oldSx, sx));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getSy() {
		return sy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSy(Integer newSy) {
		Integer oldSy = sy;
		sy = newSy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION_TEXT__SY, oldSy, sy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getWidth() {
		return width;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setWidth(Integer newWidth) {
		Integer oldWidth = width;
		width = newWidth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION_TEXT__WIDTH, oldWidth, width));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getHeight() {
		return height;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHeight(Integer newHeight) {
		Integer oldHeight = height;
		height = newHeight;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_REGION_TEXT__HEIGHT, oldHeight, height));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.GET_REGION_TEXT__CONTROL:
				if (resolve) return getControl();
				return basicGetControl();
			case TeslaPackage.GET_REGION_TEXT__X:
				return getX();
			case TeslaPackage.GET_REGION_TEXT__Y:
				return getY();
			case TeslaPackage.GET_REGION_TEXT__SX:
				return getSx();
			case TeslaPackage.GET_REGION_TEXT__SY:
				return getSy();
			case TeslaPackage.GET_REGION_TEXT__WIDTH:
				return getWidth();
			case TeslaPackage.GET_REGION_TEXT__HEIGHT:
				return getHeight();
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
			case TeslaPackage.GET_REGION_TEXT__CONTROL:
				setControl((ControlHandler)newValue);
				return;
			case TeslaPackage.GET_REGION_TEXT__X:
				setX((Integer)newValue);
				return;
			case TeslaPackage.GET_REGION_TEXT__Y:
				setY((Integer)newValue);
				return;
			case TeslaPackage.GET_REGION_TEXT__SX:
				setSx((Integer)newValue);
				return;
			case TeslaPackage.GET_REGION_TEXT__SY:
				setSy((Integer)newValue);
				return;
			case TeslaPackage.GET_REGION_TEXT__WIDTH:
				setWidth((Integer)newValue);
				return;
			case TeslaPackage.GET_REGION_TEXT__HEIGHT:
				setHeight((Integer)newValue);
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
			case TeslaPackage.GET_REGION_TEXT__CONTROL:
				setControl((ControlHandler)null);
				return;
			case TeslaPackage.GET_REGION_TEXT__X:
				setX(X_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION_TEXT__Y:
				setY(Y_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION_TEXT__SX:
				setSx(SX_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION_TEXT__SY:
				setSy(SY_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION_TEXT__WIDTH:
				setWidth(WIDTH_EDEFAULT);
				return;
			case TeslaPackage.GET_REGION_TEXT__HEIGHT:
				setHeight(HEIGHT_EDEFAULT);
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
			case TeslaPackage.GET_REGION_TEXT__CONTROL:
				return control != null;
			case TeslaPackage.GET_REGION_TEXT__X:
				return X_EDEFAULT == null ? x != null : !X_EDEFAULT.equals(x);
			case TeslaPackage.GET_REGION_TEXT__Y:
				return Y_EDEFAULT == null ? y != null : !Y_EDEFAULT.equals(y);
			case TeslaPackage.GET_REGION_TEXT__SX:
				return SX_EDEFAULT == null ? sx != null : !SX_EDEFAULT.equals(sx);
			case TeslaPackage.GET_REGION_TEXT__SY:
				return SY_EDEFAULT == null ? sy != null : !SY_EDEFAULT.equals(sy);
			case TeslaPackage.GET_REGION_TEXT__WIDTH:
				return WIDTH_EDEFAULT == null ? width != null : !WIDTH_EDEFAULT.equals(width);
			case TeslaPackage.GET_REGION_TEXT__HEIGHT:
				return HEIGHT_EDEFAULT == null ? height != null : !HEIGHT_EDEFAULT.equals(height);
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
		result.append(" (x: ");
		result.append(x);
		result.append(", y: ");
		result.append(y);
		result.append(", sx: ");
		result.append(sx);
		result.append(", sy: ");
		result.append(sy);
		result.append(", width: ");
		result.append(width);
		result.append(", height: ");
		result.append(height);
		result.append(')');
		return result.toString();
	}

} //GetRegionTextImpl
