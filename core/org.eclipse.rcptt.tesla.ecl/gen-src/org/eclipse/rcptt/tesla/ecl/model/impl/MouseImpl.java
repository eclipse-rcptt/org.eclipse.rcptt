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

import org.eclipse.rcptt.tesla.ecl.model.Mouse;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Mouse</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.MouseImpl#getEvent <em>Event</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.MouseImpl#getButton <em>Button</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.MouseImpl#getWith <em>With</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.MouseImpl#getCount <em>Count</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.MouseImpl#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.MouseImpl#getY <em>Y</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MouseImpl extends ControlCommandImpl implements Mouse {
	/**
	 * The default value of the '{@link #getEvent() <em>Event</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEvent()
	 * @generated
	 * @ordered
	 */
	protected static final String EVENT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEvent() <em>Event</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEvent()
	 * @generated
	 * @ordered
	 */
	protected String event = EVENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getButton() <em>Button</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getButton()
	 * @generated
	 * @ordered
	 */
	protected static final String BUTTON_EDEFAULT = "Left";

	/**
	 * The cached value of the '{@link #getButton() <em>Button</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getButton()
	 * @generated
	 * @ordered
	 */
	protected String button = BUTTON_EDEFAULT;

	/**
	 * The default value of the '{@link #getWith() <em>With</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWith()
	 * @generated
	 * @ordered
	 */
	protected static final String WITH_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getWith() <em>With</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWith()
	 * @generated
	 * @ordered
	 */
	protected String with = WITH_EDEFAULT;

	/**
	 * The default value of the '{@link #getCount() <em>Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCount()
	 * @generated
	 * @ordered
	 */
	protected static final int COUNT_EDEFAULT = -1;

	/**
	 * The cached value of the '{@link #getCount() <em>Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCount()
	 * @generated
	 * @ordered
	 */
	protected int count = COUNT_EDEFAULT;

	/**
	 * The default value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected static final int X_EDEFAULT = -2147483648;

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
	protected static final int Y_EDEFAULT = -2147483648;

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MouseImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.MOUSE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getEvent() {
		return event;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEvent(String newEvent) {
		String oldEvent = event;
		event = newEvent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.MOUSE__EVENT, oldEvent, event));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getButton() {
		return button;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setButton(String newButton) {
		String oldButton = button;
		button = newButton;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.MOUSE__BUTTON, oldButton, button));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getWith() {
		return with;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setWith(String newWith) {
		String oldWith = with;
		with = newWith;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.MOUSE__WITH, oldWith, with));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getCount() {
		return count;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCount(int newCount) {
		int oldCount = count;
		count = newCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.MOUSE__COUNT, oldCount, count));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.MOUSE__X, oldX, x));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.MOUSE__Y, oldY, y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.MOUSE__EVENT:
				return getEvent();
			case TeslaPackage.MOUSE__BUTTON:
				return getButton();
			case TeslaPackage.MOUSE__WITH:
				return getWith();
			case TeslaPackage.MOUSE__COUNT:
				return getCount();
			case TeslaPackage.MOUSE__X:
				return getX();
			case TeslaPackage.MOUSE__Y:
				return getY();
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
			case TeslaPackage.MOUSE__EVENT:
				setEvent((String)newValue);
				return;
			case TeslaPackage.MOUSE__BUTTON:
				setButton((String)newValue);
				return;
			case TeslaPackage.MOUSE__WITH:
				setWith((String)newValue);
				return;
			case TeslaPackage.MOUSE__COUNT:
				setCount((Integer)newValue);
				return;
			case TeslaPackage.MOUSE__X:
				setX((Integer)newValue);
				return;
			case TeslaPackage.MOUSE__Y:
				setY((Integer)newValue);
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
			case TeslaPackage.MOUSE__EVENT:
				setEvent(EVENT_EDEFAULT);
				return;
			case TeslaPackage.MOUSE__BUTTON:
				setButton(BUTTON_EDEFAULT);
				return;
			case TeslaPackage.MOUSE__WITH:
				setWith(WITH_EDEFAULT);
				return;
			case TeslaPackage.MOUSE__COUNT:
				setCount(COUNT_EDEFAULT);
				return;
			case TeslaPackage.MOUSE__X:
				setX(X_EDEFAULT);
				return;
			case TeslaPackage.MOUSE__Y:
				setY(Y_EDEFAULT);
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
			case TeslaPackage.MOUSE__EVENT:
				return EVENT_EDEFAULT == null ? event != null : !EVENT_EDEFAULT.equals(event);
			case TeslaPackage.MOUSE__BUTTON:
				return BUTTON_EDEFAULT == null ? button != null : !BUTTON_EDEFAULT.equals(button);
			case TeslaPackage.MOUSE__WITH:
				return WITH_EDEFAULT == null ? with != null : !WITH_EDEFAULT.equals(with);
			case TeslaPackage.MOUSE__COUNT:
				return count != COUNT_EDEFAULT;
			case TeslaPackage.MOUSE__X:
				return x != X_EDEFAULT;
			case TeslaPackage.MOUSE__Y:
				return y != Y_EDEFAULT;
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
		result.append(" (event: ");
		result.append(event);
		result.append(", button: ");
		result.append(button);
		result.append(", with: ");
		result.append(with);
		result.append(", count: ");
		result.append(count);
		result.append(", x: ");
		result.append(x);
		result.append(", y: ");
		result.append(y);
		result.append(')');
		return result.toString();
	}

} //MouseImpl
