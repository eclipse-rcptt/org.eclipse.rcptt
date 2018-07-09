/*******************************************************************************
 * Copyright (c) 2009, 2017 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.platform.commands;

import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.ProcessStatus;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Get Status Trace</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.commands.GetStatusTrace#getStatus <em>Status</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getGetStatusTrace()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Takes ProcessStatus from input and returns trace.' returns='String with status trace.' example='try {\n\tthrow-error \"Error ocurred\"\n} -error [val e] -catch {\n\t$e | get-status-trace | log\n}'"
 * @generated
 */
public interface GetStatusTrace extends Command {
	/**
	 * Returns the value of the '<em><b>Status</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Status</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' reference.
	 * @see #setStatus(ProcessStatus)
	 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getGetStatusTrace_Status()
	 * @model required="true"
	 * @generated
	 */
	ProcessStatus getStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.commands.GetStatusTrace#getStatus <em>Status</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' reference.
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(ProcessStatus value);

} // GetStatusTrace
