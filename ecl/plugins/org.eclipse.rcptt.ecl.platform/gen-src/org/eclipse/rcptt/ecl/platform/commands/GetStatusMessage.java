/*******************************************************************************
 * Copyright (c) 2009, 2009, 2019 Xored Software Inc and others.
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
import org.eclipse.rcptt.ecl.core.ProcessStatus;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Get Status Message</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.commands.GetStatusMessage#getStatus <em>Status</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getGetStatusMessage()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Takes ProcessStatus from input and returns first root cause.' returns='String with first root cause.' example='try {\n\tthrow-error \"Error ocurred\"\n} -error [val e] -catch {\n\t$e | get-status-trace | log\n}'"
 * @generated
 */
public interface GetStatusMessage extends Command {
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
	 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#getGetStatusMessage_Status()
	 * @model required="true"
	 *        annotation="http://www.eclipse.org/ecl/input"
	 * @generated
	 */
	ProcessStatus getStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.commands.GetStatusMessage#getStatus <em>Status</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' reference.
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(ProcessStatus value);

} // GetStatusMessage
