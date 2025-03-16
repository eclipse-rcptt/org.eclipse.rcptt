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
package org.eclipse.rcptt.ecl.core;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ecl Exception</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.core.EclException#getClassName <em>Class Name</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.core.EclException#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.core.EclException#getStackTrace <em>Stack Trace</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.core.EclException#getThrowable <em>Throwable</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.core.EclException#getCause <em>Cause</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.core.EclException#getStatus <em>Status</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.core.CorePackage#getEclException()
 * @model
 * @generated
 */
public interface EclException extends EObject {
	/**
	 * Returns the value of the '<em><b>Class Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Class Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Class Name</em>' attribute.
	 * @see #setClassName(String)
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getEclException_ClassName()
	 * @model
	 * @generated
	 */
	String getClassName();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.core.EclException#getClassName <em>Class Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Class Name</em>' attribute.
	 * @see #getClassName()
	 * @generated
	 */
	void setClassName(String value);

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Message</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getEclException_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.core.EclException#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

	/**
	 * Returns the value of the '<em><b>Stack Trace</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.rcptt.ecl.core.EclStackTraceEntry}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Stack Trace</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stack Trace</em>' containment reference list.
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getEclException_StackTrace()
	 * @model containment="true"
	 * @generated
	 */
	EList<EclStackTraceEntry> getStackTrace();

	/**
	 * Returns the value of the '<em><b>Throwable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Throwable</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Throwable</em>' attribute.
	 * @see #setThrowable(Throwable)
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getEclException_Throwable()
	 * @model dataType="org.eclipse.rcptt.ecl.core.Throwable" transient="true"
	 * @generated
	 */
	Throwable getThrowable();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.core.EclException#getThrowable <em>Throwable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Throwable</em>' attribute.
	 * @see #getThrowable()
	 * @generated
	 */
	void setThrowable(Throwable value);

	/**
	 * Returns the value of the '<em><b>Cause</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Cause</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Cause</em>' containment reference.
	 * @see #setCause(EclException)
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getEclException_Cause()
	 * @model containment="true"
	 * @generated
	 */
	EclException getCause();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.core.EclException#getCause <em>Cause</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cause</em>' containment reference.
	 * @see #getCause()
	 * @generated
	 */
	void setCause(EclException value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' containment reference.
	 * @see #setStatus(ProcessStatus)
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getEclException_Status()
	 * @model containment="true"
	 * @generated
	 */
	ProcessStatus getStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.core.EclException#getStatus <em>Status</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' containment reference.
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(ProcessStatus value);

} // EclException
