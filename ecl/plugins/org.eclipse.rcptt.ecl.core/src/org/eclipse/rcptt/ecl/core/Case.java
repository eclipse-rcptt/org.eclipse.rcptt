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
/**
 */
package org.eclipse.rcptt.ecl.core;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Case</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.core.Case#getCondition <em>Condition</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.core.Case#getThen <em>Then</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.ecl.core.CorePackage#getCase()
 * @model annotation="http://www.eclipse.org/ecl/docs description='Corresponds command from &lt;code&gt;then&lt;/code&gt; branch if an object from the &lt;code&gt;input&lt;/code&gt; matches the &lt;code&gt;condition&lt;/code&gt; object.' returns='An internal object describing the case. Can only be used by a containing &lt;code&gt;switch&lt;/code&gt; command.' example='list 1 2 3 | get 0 | switch [case 1 {\r\n\tlog -message \"First item is 1\"\r\n}] [case 2 {\n\tlog -message \"First item is 2\"\n}]'"
 * @generated
 */
public interface Case extends Command {
	/**
	 * Returns the value of the '<em><b>Condition</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Condition</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Condition</em>' reference.
	 * @see #setCondition(EObject)
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getCase_Condition()
	 * @model required="true"
	 *        annotation="http://www.eclipse.org/ecl/docs description='Object used to match to the &lt;code&gt;input&lt;/code&gt;. Accepted values are boolean, string (&lt;code&gt;true&lt;/code&gt; when case-insensitive equals to &lt;code&gt;true&lt;/code&gt;, &lt;code&gt;false&lt;/code&gt; otherwise) and number (&lt;code&gt;0&lt;/code&gt; for &lt;code&gt;false&lt;/code&gt;, &lt;code&gt;true&lt;/code&gt; otherwise). '"
	 * @generated
	 */
	EObject getCondition();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.core.Case#getCondition <em>Condition</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Condition</em>' reference.
	 * @see #getCondition()
	 * @generated
	 */
	void setCondition(EObject value);

	/**
	 * Returns the value of the '<em><b>Then</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Then</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Then</em>' containment reference.
	 * @see #setThen(Command)
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#getCase_Then()
	 * @model containment="true"
	 *        annotation="http://www.eclipse.org/ecl/docs description='Code to execute. &lt;code&gt;input&lt;/code&gt; is propagated from the command to this branch code.'"
	 * @generated
	 */
	Command getThen();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.ecl.core.Case#getThen <em>Then</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Then</em>' containment reference.
	 * @see #getThen()
	 * @generated
	 */
	void setThen(Command value);

} // Case
