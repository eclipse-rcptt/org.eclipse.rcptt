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
package org.eclipse.rcptt.ecl.platform.commands;

import org.eclipse.rcptt.ecl.core.Command;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Echo</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.commands.Echo#getStr <em>Str</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getEcho()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Takes a string argument and writes it into an output pipe. ' returns='value of &lt;code&gt;str&lt;/code&gt; argument' example='echo \"MyStr\" | log'"
 * @generated
 */
public interface Echo extends Command {
	/**
	 * Returns the value of the '<em><b>Str</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Str</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Str</em>' attribute.
	 * @see #setStr(String)
	 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getEcho_Str()
	 * @model default=""
	 *        annotation="http://www.eclipse.org/ecl/input"
	 * @generated
	 */
	String getStr();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.commands.Echo#getStr <em>Str</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Str</em>' attribute.
	 * @see #getStr()
	 * @generated
	 */
	void setStr(String value);

} // Echo
