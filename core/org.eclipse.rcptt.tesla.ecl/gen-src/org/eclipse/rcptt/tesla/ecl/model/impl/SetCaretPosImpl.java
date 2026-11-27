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
import org.eclipse.rcptt.tesla.ecl.model.SetCaretPos;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Set Caret Pos</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.SetCaretPosImpl#getControl <em>Control</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.SetCaretPosImpl#getLine <em>Line</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.SetCaretPosImpl#getColumn <em>Column</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SetCaretPosImpl extends CommandImpl implements SetCaretPos {
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
	 * The default value of the '{@link #getLine() <em>Line</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLine()
	 * @generated
	 * @ordered
	 */
	protected static final int LINE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getLine() <em>Line</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLine()
	 * @generated
	 * @ordered
	 */
	protected int line = LINE_EDEFAULT;

	/**
	 * The default value of the '{@link #getColumn() <em>Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumn()
	 * @generated
	 * @ordered
	 */
	protected static final int COLUMN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getColumn() <em>Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumn()
	 * @generated
	 * @ordered
	 */
	protected int column = COLUMN_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SetCaretPosImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.SET_CARET_POS;
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.SET_CARET_POS__CONTROL, oldControl, control));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.SET_CARET_POS__CONTROL, oldControl, control));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getLine() {
		return line;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLine(int newLine) {
		int oldLine = line;
		line = newLine;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.SET_CARET_POS__LINE, oldLine, line));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getColumn() {
		return column;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setColumn(int newColumn) {
		int oldColumn = column;
		column = newColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.SET_CARET_POS__COLUMN, oldColumn, column));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.SET_CARET_POS__CONTROL:
				if (resolve) return getControl();
				return basicGetControl();
			case TeslaPackage.SET_CARET_POS__LINE:
				return getLine();
			case TeslaPackage.SET_CARET_POS__COLUMN:
				return getColumn();
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
			case TeslaPackage.SET_CARET_POS__CONTROL:
				setControl((ControlHandler)newValue);
				return;
			case TeslaPackage.SET_CARET_POS__LINE:
				setLine((Integer)newValue);
				return;
			case TeslaPackage.SET_CARET_POS__COLUMN:
				setColumn((Integer)newValue);
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
			case TeslaPackage.SET_CARET_POS__CONTROL:
				setControl((ControlHandler)null);
				return;
			case TeslaPackage.SET_CARET_POS__LINE:
				setLine(LINE_EDEFAULT);
				return;
			case TeslaPackage.SET_CARET_POS__COLUMN:
				setColumn(COLUMN_EDEFAULT);
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
			case TeslaPackage.SET_CARET_POS__CONTROL:
				return control != null;
			case TeslaPackage.SET_CARET_POS__LINE:
				return line != LINE_EDEFAULT;
			case TeslaPackage.SET_CARET_POS__COLUMN:
				return column != COLUMN_EDEFAULT;
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
		result.append(" (line: ");
		result.append(line);
		result.append(", column: ");
		result.append(column);
		result.append(')');
		return result.toString();
	}

} //SetCaretPosImpl
