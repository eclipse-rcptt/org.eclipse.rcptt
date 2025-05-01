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
package org.eclipse.rcptt.reporting.impl;

import org.eclipse.rcptt.reporting.Q7Statistics;
import org.eclipse.rcptt.reporting.ReportingPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Q7 Statistics</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.reporting.impl.Q7StatisticsImpl#getTotal <em>Total</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.impl.Q7StatisticsImpl#getFailed <em>Failed</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.impl.Q7StatisticsImpl#getTime <em>Time</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.impl.Q7StatisticsImpl#getSkipped <em>Skipped</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.impl.Q7StatisticsImpl#getPassed <em>Passed</em>}</li>
 * </ul>
 *
 * @generated
 */
public class Q7StatisticsImpl extends EObjectImpl implements Q7Statistics {
	/**
	 * The default value of the '{@link #getTotal() <em>Total</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotal()
	 * @generated
	 * @ordered
	 */
	protected static final int TOTAL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTotal() <em>Total</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotal()
	 * @generated
	 * @ordered
	 */
	protected int total = TOTAL_EDEFAULT;

	/**
	 * The default value of the '{@link #getFailed() <em>Failed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFailed()
	 * @generated
	 * @ordered
	 */
	protected static final int FAILED_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getFailed() <em>Failed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFailed()
	 * @generated
	 * @ordered
	 */
	protected int failed = FAILED_EDEFAULT;

	/**
	 * The default value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected static final int TIME_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected int time = TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getSkipped() <em>Skipped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSkipped()
	 * @generated
	 * @ordered
	 */
	protected static final int SKIPPED_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getSkipped() <em>Skipped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSkipped()
	 * @generated
	 * @ordered
	 */
	protected int skipped = SKIPPED_EDEFAULT;

	/**
	 * The default value of the '{@link #getPassed() <em>Passed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPassed()
	 * @generated
	 * @ordered
	 */
	protected static final int PASSED_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPassed() <em>Passed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPassed()
	 * @generated
	 * @ordered
	 */
	protected int passed = PASSED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Q7StatisticsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ReportingPackage.Literals.Q7_STATISTICS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getTotal() {
		return total;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTotal(int newTotal) {
		int oldTotal = total;
		total = newTotal;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReportingPackage.Q7_STATISTICS__TOTAL, oldTotal, total));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getFailed() {
		return failed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFailed(int newFailed) {
		int oldFailed = failed;
		failed = newFailed;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReportingPackage.Q7_STATISTICS__FAILED, oldFailed, failed));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getTime() {
		return time;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTime(int newTime) {
		int oldTime = time;
		time = newTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReportingPackage.Q7_STATISTICS__TIME, oldTime, time));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getSkipped() {
		return skipped;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSkipped(int newSkipped) {
		int oldSkipped = skipped;
		skipped = newSkipped;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReportingPackage.Q7_STATISTICS__SKIPPED, oldSkipped, skipped));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getPassed() {
		return passed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPassed(int newPassed) {
		int oldPassed = passed;
		passed = newPassed;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ReportingPackage.Q7_STATISTICS__PASSED, oldPassed, passed));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ReportingPackage.Q7_STATISTICS__TOTAL:
				return getTotal();
			case ReportingPackage.Q7_STATISTICS__FAILED:
				return getFailed();
			case ReportingPackage.Q7_STATISTICS__TIME:
				return getTime();
			case ReportingPackage.Q7_STATISTICS__SKIPPED:
				return getSkipped();
			case ReportingPackage.Q7_STATISTICS__PASSED:
				return getPassed();
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
			case ReportingPackage.Q7_STATISTICS__TOTAL:
				setTotal((Integer)newValue);
				return;
			case ReportingPackage.Q7_STATISTICS__FAILED:
				setFailed((Integer)newValue);
				return;
			case ReportingPackage.Q7_STATISTICS__TIME:
				setTime((Integer)newValue);
				return;
			case ReportingPackage.Q7_STATISTICS__SKIPPED:
				setSkipped((Integer)newValue);
				return;
			case ReportingPackage.Q7_STATISTICS__PASSED:
				setPassed((Integer)newValue);
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
			case ReportingPackage.Q7_STATISTICS__TOTAL:
				setTotal(TOTAL_EDEFAULT);
				return;
			case ReportingPackage.Q7_STATISTICS__FAILED:
				setFailed(FAILED_EDEFAULT);
				return;
			case ReportingPackage.Q7_STATISTICS__TIME:
				setTime(TIME_EDEFAULT);
				return;
			case ReportingPackage.Q7_STATISTICS__SKIPPED:
				setSkipped(SKIPPED_EDEFAULT);
				return;
			case ReportingPackage.Q7_STATISTICS__PASSED:
				setPassed(PASSED_EDEFAULT);
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
			case ReportingPackage.Q7_STATISTICS__TOTAL:
				return total != TOTAL_EDEFAULT;
			case ReportingPackage.Q7_STATISTICS__FAILED:
				return failed != FAILED_EDEFAULT;
			case ReportingPackage.Q7_STATISTICS__TIME:
				return time != TIME_EDEFAULT;
			case ReportingPackage.Q7_STATISTICS__SKIPPED:
				return skipped != SKIPPED_EDEFAULT;
			case ReportingPackage.Q7_STATISTICS__PASSED:
				return passed != PASSED_EDEFAULT;
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
		result.append(" (total: ");
		result.append(total);
		result.append(", failed: ");
		result.append(failed);
		result.append(", time: ");
		result.append(time);
		result.append(", skipped: ");
		result.append(skipped);
		result.append(", passed: ");
		result.append(passed);
		result.append(')');
		return result.toString();
	}

} //Q7StatisticsImpl
