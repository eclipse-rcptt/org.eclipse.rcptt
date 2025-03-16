/*******************************************************************************
 * Copyright (c) 2014, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.rcptt.ecl.platform.ui.commands;

import org.eclipse.rcptt.ecl.core.Command;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Create Working Set</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet#getType <em>Type</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage#getCreateWorkingSet()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Creates working set with a given name.' returns='Nothing' example='create-working-set \"MyWorkingSet\"'"
 * @generated
 */
public interface CreateWorkingSet extends Command {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage#getCreateWorkingSet_Name()
	 * @model annotation="http://www.eclipse.org/ecl/input"
	 *        annotation="http://www.eclipse.org/ecl/docs description='Working set name'"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The default value is <code>"org.eclipse.ui.resourceWorkingSetPage"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage#getCreateWorkingSet_Type()
	 * @model default="org.eclipse.ui.resourceWorkingSetPage"
	 *        annotation="http://www.eclipse.org/ecl/docs description='Working set type id. Default value is &lt;code&gt;org.eclipse.ui.resourceWorkingSetPage&lt;/code&gt;, which corresponds to Resource Working Set. The short name &lt;code&gt;java&lt;/code&gt; can be used to identify type &lt;code&gt;org.eclipse.jdt.ui.JavaWorkingSetPage&lt;/code&gt;'"
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.CreateWorkingSet#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

} // CreateWorkingSet
