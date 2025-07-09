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

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Global</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.core.Global#getVals <em>Vals</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.core.Global#isOverride <em>Override</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.core.CorePackage#getGlobal()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Creates a new global variable declaration.' returns='An internal object, describing a variable' example='// declaring an empty list:\n\nglobal [val myList [list]]\n\n\n\n// filling the list with all New/... menu items:\n\nrepeat [val index] -times [get-elements-count] -command {\n\nlet [val oldlist [$myList]] [val itemName [get-item-property [$index] \"getText()\"]] {\n\n// check if the item is not separator or Other...\n\nif [and [$itemName | not-eq \"\"][$itemName | not-eq \"&amp;Other.*\"]] {\n\nglobal [val myList [append [$oldlist] [$itemName]]] -override\n\n}\n\n}\n\n}'"
 * @generated
 */
public interface Global extends Command {
	/**
	 * Returns the value of the '<em><b>Vals</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.rcptt.ecl.core.Declaration}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Vals</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Vals</em>' containment reference list.
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getGlobal_Vals()
	 * @model containment="true"
	 * @generated
	 */
	EList<Declaration> getVals();

	/**
	 * Returns the value of the '<em><b>Override</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Override</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Override</em>' attribute.
	 * @see #setOverride(boolean)
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getGlobal_Override()
	 * @model default="false"
	 * @generated
	 */
	boolean isOverride();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.core.Global#isOverride <em>Override</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Override</em>' attribute.
	 * @see #isOverride()
	 * @generated
	 */
	void setOverride(boolean value);

} // Global
