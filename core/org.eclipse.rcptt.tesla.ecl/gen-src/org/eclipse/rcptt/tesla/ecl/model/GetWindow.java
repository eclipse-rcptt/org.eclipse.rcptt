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
package org.eclipse.rcptt.tesla.ecl.model;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Get Window</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.GetWindow#getText <em>Text</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.GetWindow#getFrom <em>From</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.GetWindow#getClass_ <em>Class</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#getGetWindow()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Get window with defined name. If this window doesn\'t exist, then error is returned. Parent argument is ignored.' returns='window' recorded='true' example='get-window \"Select context\" | get-table | select \"ECL Context \\\\(Q7Project\\\\)\" | click -default'"
 * @generated
 */
public interface GetWindow extends Selector {

	/**
	 * Returns the value of the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Text</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Text</em>' attribute.
	 * @see #setText(String)
	 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#getGetWindow_Text()
	 * @model annotation="http://www.eclipse.org/ecl/docs description='Indicates the name of this window. This parameter is used by default, so &lt;code&gt;-text&lt;/code&gt; word is not written at the recording of the test.'"
	 * @generated
	 */
	String getText();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.tesla.ecl.model.GetWindow#getText <em>Text</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Text</em>' attribute.
	 * @see #getText()
	 * @generated
	 */
	void setText(String value);

	/**
	 * Returns the value of the '<em><b>From</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>From</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>From</em>' attribute.
	 * @see #setFrom(String)
	 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#getGetWindow_From()
	 * @model annotation="http://www.eclipse.org/ecl/docs description='Indicates the method (in the form of &lt;code&gt;\"ClassName.methodName()\"&lt;/code&gt;) where this window is created.'"
	 * @generated
	 */
	String getFrom();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.tesla.ecl.model.GetWindow#getFrom <em>From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>From</em>' attribute.
	 * @see #getFrom()
	 * @generated
	 */
	void setFrom(String value);

	/**
	 * Returns the value of the '<em><b>Class</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Class</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Class</em>' attribute.
	 * @see #setClass(String)
	 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#getGetWindow_Class()
	 * @model annotation="http://www.eclipse.org/ecl/docs description='Indicates name of the JFace Window subclass defining this window.'"
	 * @generated
	 */
	String getClass_();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.tesla.ecl.model.GetWindow#getClass_ <em>Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Class</em>' attribute.
	 * @see #getClass_()
	 * @generated
	 */
	void setClass(String value);
} // GetWindow
