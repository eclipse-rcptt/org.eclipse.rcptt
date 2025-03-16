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

import org.eclipse.rcptt.ecl.platform.ui.objects.WorkingSet;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Delete Working Set</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.ui.commands.DeleteWorkingSet#getInput <em>Input</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage#getDeleteWorkingSet()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Deletes working set' returns='Nothing' example='list-working-sets | foreach {delete-working-set}'"
 * @generated
 */
public interface DeleteWorkingSet extends Command {
	/**
	 * Returns the value of the '<em><b>Input</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Input</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Input</em>' reference.
	 * @see #setInput(WorkingSet)
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage#getDeleteWorkingSet_Input()
	 * @model annotation="http://www.eclipse.org/ecl/input"
	 *        annotation="http://www.eclipse.org/ecl/docs description='Working set object'"
	 * @generated
	 */
	WorkingSet getInput();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.DeleteWorkingSet#getInput <em>Input</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input</em>' reference.
	 * @see #getInput()
	 * @generated
	 */
	void setInput(WorkingSet value);

} // DeleteWorkingSet
