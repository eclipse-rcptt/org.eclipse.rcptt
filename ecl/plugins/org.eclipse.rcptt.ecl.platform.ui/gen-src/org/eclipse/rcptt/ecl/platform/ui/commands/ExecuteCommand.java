/**
 */
package org.eclipse.rcptt.ecl.platform.ui.commands;

import org.eclipse.rcptt.ecl.core.Command;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Execute Command</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand#getCommandId <em>Command Id</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage#getExecuteCommand()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Executes an Eclipse command. ' example='execute-command org.eclipse.ui.help.installationDialog'"
 * @generated
 */
public interface ExecuteCommand extends Command {
	/**
	 * Returns the value of the '<em><b>Command Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command Id</em>' attribute.
	 * @see #setCommandId(String)
	 * @see org.eclipse.rcptt.ecl.platform.ui.commands.CommandsPackage#getExecuteCommand_CommandId()
	 * @model
	 * @generated
	 */
	String getCommandId();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand#getCommandId <em>Command Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command Id</em>' attribute.
	 * @see #getCommandId()
	 * @generated
	 */
	void setCommandId(String value);

} // ExecuteCommand
