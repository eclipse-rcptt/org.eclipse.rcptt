/*******************************************************************************
 * Copyright (c) 2009, 2015 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.ecl.model;

import org.eclipse.rcptt.ecl.core.Command;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Options</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.Options#isAllowStatusDialog <em>Allow Status Dialog</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.Options#getCommand <em>Command</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#getOptions()
 * @model
 * @generated
 */
public interface Options extends Command {
	/**
	 * Returns the value of the '<em><b>Allow Status Dialog</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Allow Status Dialog</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Allow Status Dialog</em>' attribute.
	 * @see #setAllowStatusDialog(boolean)
	 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#getOptions_AllowStatusDialog()
	 * @model
	 * @generated
	 */
	boolean isAllowStatusDialog();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.tesla.ecl.model.Options#isAllowStatusDialog <em>Allow Status Dialog</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Allow Status Dialog</em>' attribute.
	 * @see #isAllowStatusDialog()
	 * @generated
	 */
	void setAllowStatusDialog(boolean value);

	/**
	 * Returns the value of the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command</em>' reference.
	 * @see #setCommand(Command)
	 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#getOptions_Command()
	 * @model annotation="http://www.eclipse.org/ecl/docs description='Command to execute.'"
	 * @generated
	 */
	Command getCommand();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.tesla.ecl.model.Options#getCommand <em>Command</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command</em>' reference.
	 * @see #getCommand()
	 * @generated
	 */
	void setCommand(Command value);

} // Options
