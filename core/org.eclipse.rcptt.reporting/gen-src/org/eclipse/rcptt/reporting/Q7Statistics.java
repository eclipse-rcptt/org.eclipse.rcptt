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
package org.eclipse.rcptt.reporting;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Q7 Statistics</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Statistics#getTotal <em>Total</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Statistics#getFailed <em>Failed</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Statistics#getTime <em>Time</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Statistics#getSkipped <em>Skipped</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Statistics#getPassed <em>Passed</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Statistics()
 * @model
 * @generated
 */
public interface Q7Statistics extends EObject {
	/**
	 * Returns the value of the '<em><b>Total</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Total</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total</em>' attribute.
	 * @see #setTotal(int)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Statistics_Total()
	 * @model
	 * @generated
	 */
	int getTotal();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Statistics#getTotal <em>Total</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total</em>' attribute.
	 * @see #getTotal()
	 * @generated
	 */
	void setTotal(int value);

	/**
	 * Returns the value of the '<em><b>Failed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Failed</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Failed</em>' attribute.
	 * @see #setFailed(int)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Statistics_Failed()
	 * @model
	 * @generated
	 */
	int getFailed();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Statistics#getFailed <em>Failed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Failed</em>' attribute.
	 * @see #getFailed()
	 * @generated
	 */
	void setFailed(int value);

	/**
	 * Returns the value of the '<em><b>Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Time</em>' attribute.
	 * @see #setTime(int)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Statistics_Time()
	 * @model
	 * @generated
	 */
	int getTime();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Statistics#getTime <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Time</em>' attribute.
	 * @see #getTime()
	 * @generated
	 */
	void setTime(int value);

	/**
	 * Returns the value of the '<em><b>Skipped</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Skipped</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Skipped</em>' attribute.
	 * @see #setSkipped(int)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Statistics_Skipped()
	 * @model
	 * @generated
	 */
	int getSkipped();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Statistics#getSkipped <em>Skipped</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Skipped</em>' attribute.
	 * @see #getSkipped()
	 * @generated
	 */
	void setSkipped(int value);

	/**
	 * Returns the value of the '<em><b>Passed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Passed</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Passed</em>' attribute.
	 * @see #setPassed(int)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Statistics_Passed()
	 * @model
	 * @generated
	 */
	int getPassed();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Statistics#getPassed <em>Passed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Passed</em>' attribute.
	 * @see #getPassed()
	 * @generated
	 */
	void setPassed(int value);

} // Q7Statistics
