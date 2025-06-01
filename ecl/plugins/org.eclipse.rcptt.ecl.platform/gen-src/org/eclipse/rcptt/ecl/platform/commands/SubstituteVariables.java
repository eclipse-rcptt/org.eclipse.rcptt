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
 * A representation of the model object '<em><b>Substitute Variables</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.commands.SubstituteVariables#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.commands.SubstituteVariables#isIgnoreUndefined <em>Ignore Undefined</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getSubstituteVariables()
 * @model annotation="http://www.eclipse.org/ecl/docs description='&lt;a href=\'http://https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Freference%2Fpreferences%2Frun-debug%2Fref-string_substitution.htm\'&gt;Eclipse string substitution.&lt;/a&gt; Recursively resolves and replaces all variable references in the given expression with their corresponding values. Allows the client to control whether references to undefined variables are reported as an error (i.e. an exception is thrown). Unavailable if AUT does not have org.eclipse.core.variables plug-in.' returns='expression with variable references replaced with variable values' example='//writes prop val to AUT workspace log\nlog [substitute-variables \"${system_property:propertyName}\"] '"
 * @generated
 */
public interface SubstituteVariables extends Command {
	/**
	 * Returns the value of the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Expression</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Expression</em>' attribute.
	 * @see #setExpression(String)
	 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getSubstituteVariables_Expression()
	 * @model annotation="http://www.eclipse.org/ecl/docs description='expression referencing variables'"
	 * @generated
	 */
	String getExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.commands.SubstituteVariables#getExpression <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expression</em>' attribute.
	 * @see #getExpression()
	 * @generated
	 */
	void setExpression(String value);

	/**
	 * Returns the value of the '<em><b>Ignore Undefined</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ignore Undefined</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ignore Undefined</em>' attribute.
	 * @see #setIgnoreUndefined(boolean)
	 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getSubstituteVariables_IgnoreUndefined()
	 * @model default="false"
	 *        annotation="http://www.eclipse.org/ecl/docs description='whether a reference to an undefined variable is to be considered an error (i.e. throw an exception)'"
	 * @generated
	 */
	boolean isIgnoreUndefined();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.commands.SubstituteVariables#isIgnoreUndefined <em>Ignore Undefined</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ignore Undefined</em>' attribute.
	 * @see #isIgnoreUndefined()
	 * @generated
	 */
	void setIgnoreUndefined(boolean value);

} // SubstituteVariables
