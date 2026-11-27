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
import org.eclipse.rcptt.tesla.ecl.model.SelectRange;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Select Range</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.SelectRangeImpl#getControl <em>Control</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.SelectRangeImpl#getLine <em>Line</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.SelectRangeImpl#getColumn <em>Column</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.SelectRangeImpl#getEndLine <em>End Line</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.SelectRangeImpl#getEndColumn <em>End Column</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SelectRangeImpl extends CommandImpl implements SelectRange {
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
	 * The default value of the '{@link #getEndLine() <em>End Line</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndLine()
	 * @generated
	 * @ordered
	 */
	protected static final int END_LINE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getEndLine() <em>End Line</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndLine()
	 * @generated
	 * @ordered
	 */
	protected int endLine = END_LINE_EDEFAULT;

	/**
	 * The default value of the '{@link #getEndColumn() <em>End Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndColumn()
	 * @generated
	 * @ordered
	 */
	protected static final int END_COLUMN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getEndColumn() <em>End Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEndColumn()
	 * @generated
	 * @ordered
	 */
	protected int endColumn = END_COLUMN_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SelectRangeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.SELECT_RANGE;
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.SELECT_RANGE__CONTROL, oldControl, control));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.SELECT_RANGE__CONTROL, oldControl, control));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.SELECT_RANGE__LINE, oldLine, line));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.SELECT_RANGE__COLUMN, oldColumn, column));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getEndLine() {
		return endLine;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEndLine(int newEndLine) {
		int oldEndLine = endLine;
		endLine = newEndLine;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.SELECT_RANGE__END_LINE, oldEndLine, endLine));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getEndColumn() {
		return endColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEndColumn(int newEndColumn) {
		int oldEndColumn = endColumn;
		endColumn = newEndColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.SELECT_RANGE__END_COLUMN, oldEndColumn, endColumn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.SELECT_RANGE__CONTROL:
				if (resolve) return getControl();
				return basicGetControl();
			case TeslaPackage.SELECT_RANGE__LINE:
				return getLine();
			case TeslaPackage.SELECT_RANGE__COLUMN:
				return getColumn();
			case TeslaPackage.SELECT_RANGE__END_LINE:
				return getEndLine();
			case TeslaPackage.SELECT_RANGE__END_COLUMN:
				return getEndColumn();
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
			case TeslaPackage.SELECT_RANGE__CONTROL:
				setControl((ControlHandler)newValue);
				return;
			case TeslaPackage.SELECT_RANGE__LINE:
				setLine((Integer)newValue);
				return;
			case TeslaPackage.SELECT_RANGE__COLUMN:
				setColumn((Integer)newValue);
				return;
			case TeslaPackage.SELECT_RANGE__END_LINE:
				setEndLine((Integer)newValue);
				return;
			case TeslaPackage.SELECT_RANGE__END_COLUMN:
				setEndColumn((Integer)newValue);
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
			case TeslaPackage.SELECT_RANGE__CONTROL:
				setControl((ControlHandler)null);
				return;
			case TeslaPackage.SELECT_RANGE__LINE:
				setLine(LINE_EDEFAULT);
				return;
			case TeslaPackage.SELECT_RANGE__COLUMN:
				setColumn(COLUMN_EDEFAULT);
				return;
			case TeslaPackage.SELECT_RANGE__END_LINE:
				setEndLine(END_LINE_EDEFAULT);
				return;
			case TeslaPackage.SELECT_RANGE__END_COLUMN:
				setEndColumn(END_COLUMN_EDEFAULT);
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
			case TeslaPackage.SELECT_RANGE__CONTROL:
				return control != null;
			case TeslaPackage.SELECT_RANGE__LINE:
				return line != LINE_EDEFAULT;
			case TeslaPackage.SELECT_RANGE__COLUMN:
				return column != COLUMN_EDEFAULT;
			case TeslaPackage.SELECT_RANGE__END_LINE:
				return endLine != END_LINE_EDEFAULT;
			case TeslaPackage.SELECT_RANGE__END_COLUMN:
				return endColumn != END_COLUMN_EDEFAULT;
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
		result.append(", endLine: ");
		result.append(endLine);
		result.append(", endColumn: ");
		result.append(endColumn);
		result.append(')');
		return result.toString();
	}

} //SelectRangeImpl
