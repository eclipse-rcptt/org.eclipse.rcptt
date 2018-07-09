/*******************************************************************************
 * Copyright (c) 2009, 2015 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.HoverRuler;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Ruler Hover</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.HoverRulerImpl#getControl <em>Control</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.HoverRulerImpl#getLine <em>Line</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.HoverRulerImpl#getWith <em>With</em>}</li>
 * </ul>
 *
 * @generated
 */
public class HoverRulerImpl extends CommandImpl implements HoverRuler {
	/**
	 * The cached value of the '{@link #getControl() <em>Control</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getControl()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler control;

	/**
	 * The default value of the '{@link #getLine() <em>Line</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getLine()
	 * @generated
	 * @ordered
	 */
	protected static final int LINE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getLine() <em>Line</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getLine()
	 * @generated
	 * @ordered
	 */
	protected int line = LINE_EDEFAULT;

	/**
	 * The default value of the '{@link #getWith() <em>With</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getWith()
	 * @generated
	 * @ordered
	 */
	protected static final String WITH_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getWith() <em>With</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getWith()
	 * @generated
	 * @ordered
	 */
	protected String with = WITH_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected HoverRulerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.HOVER_RULER;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler getControl() {
		if (control != null && control.eIsProxy()) {
			InternalEObject oldControl = (InternalEObject)control;
			control = (ControlHandler)eResolveProxy(oldControl);
			if (control != oldControl) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.HOVER_RULER__CONTROL, oldControl, control));
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
	public void setControl(ControlHandler newControl) {
		ControlHandler oldControl = control;
		control = newControl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.HOVER_RULER__CONTROL, oldControl, control));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public int getLine() {
		return line;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setLine(int newLine) {
		int oldLine = line;
		line = newLine;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.HOVER_RULER__LINE, oldLine, line));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public String getWith() {
		return with;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public void setWith(String newWith) {
		String oldWith = with;
		with = newWith;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.HOVER_RULER__WITH, oldWith, with));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.HOVER_RULER__CONTROL:
				if (resolve) return getControl();
				return basicGetControl();
			case TeslaPackage.HOVER_RULER__LINE:
				return getLine();
			case TeslaPackage.HOVER_RULER__WITH:
				return getWith();
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
			case TeslaPackage.HOVER_RULER__CONTROL:
				setControl((ControlHandler)newValue);
				return;
			case TeslaPackage.HOVER_RULER__LINE:
				setLine((Integer)newValue);
				return;
			case TeslaPackage.HOVER_RULER__WITH:
				setWith((String)newValue);
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
			case TeslaPackage.HOVER_RULER__CONTROL:
				setControl((ControlHandler)null);
				return;
			case TeslaPackage.HOVER_RULER__LINE:
				setLine(LINE_EDEFAULT);
				return;
			case TeslaPackage.HOVER_RULER__WITH:
				setWith(WITH_EDEFAULT);
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
			case TeslaPackage.HOVER_RULER__CONTROL:
				return control != null;
			case TeslaPackage.HOVER_RULER__LINE:
				return line != LINE_EDEFAULT;
			case TeslaPackage.HOVER_RULER__WITH:
				return WITH_EDEFAULT == null ? with != null : !WITH_EDEFAULT.equals(with);
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

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (line: ");
		result.append(line);
		result.append(", with: ");
		result.append(with);
		result.append(')');
		return result.toString();
	}

} // RulerHoverImpl
