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
package org.eclipse.rcptt.reporting;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.ecl.core.ProcessStatus;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Q7 Info</b></em>'.
 * Represents operation result description with additional data like line number, etc.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getLine <em>Line</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getTags <em>Tags</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getSubtype <em>Subtype</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getVariant <em>Variant</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getPhase <em>Phase</em>}</li>
 *   <li>{@link org.eclipse.rcptt.reporting.Q7Info#getResult <em>Result</em>}</li>
 * </ul>
 *
 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info()
 * @model
 * @generated
 */
public interface Q7Info extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Info#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.rcptt.reporting.ItemKind}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.rcptt.reporting.ItemKind
	 * @see #setType(ItemKind)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Type()
	 * @model
	 * @generated
	 */
	ItemKind getType();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Info#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.rcptt.reporting.ItemKind
	 * @see #getType()
	 * @generated
	 */
	void setType(ItemKind value);

	/**
	 * Returns the value of the '<em><b>Result</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Result</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Result</em>' containment reference.
	 * @see #setResult(ProcessStatus)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Result()
	 * @model containment="true"
	 * @generated
	 */
	ProcessStatus getResult();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Info#getResult <em>Result</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Result</em>' containment reference.
	 * @see #getResult()
	 * @generated
	 */
	void setResult(ProcessStatus value);

	/**
	 * Returns the value of the '<em><b>Line</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Line</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Line</em>' attribute.
	 * @see #setLine(int)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Line()
	 * @model
	 * @generated
	 */
	int getLine();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Info#getLine <em>Line</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Line</em>' attribute.
	 * @see #getLine()
	 * @generated
	 */
	void setLine(int value);

	/**
	 * Returns the value of the '<em><b>Tags</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tags</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tags</em>' attribute.
	 * @see #setTags(String)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Tags()
	 * @model default=""
	 * @generated
	 */
	String getTags();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Info#getTags <em>Tags</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tags</em>' attribute.
	 * @see #getTags()
	 * @generated
	 */
	void setTags(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Info#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Subtype</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Subtype</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Subtype</em>' attribute.
	 * @see #setSubtype(String)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Subtype()
	 * @model
	 * @generated
	 */
	String getSubtype();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Info#getSubtype <em>Subtype</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Subtype</em>' attribute.
	 * @see #getSubtype()
	 * @generated
	 */
	void setSubtype(String value);

	/**
	 * Returns the value of the '<em><b>Variant</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Variant</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Variant</em>' attribute list.
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Variant()
	 * @model
	 * @generated
	 */
	EList<String> getVariant();

	/**
	 * Returns the value of the '<em><b>Phase</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Phase</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Phase</em>' attribute.
	 * @see #setPhase(String)
	 * @see org.eclipse.rcptt.reporting.ReportingPackage#getQ7Info_Phase()
	 * @model
	 * @generated
	 */
	String getPhase();

	/**
	 * Sets the value of the '{@link org.eclipse.rcptt.reporting.Q7Info#getPhase <em>Phase</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Phase</em>' attribute.
	 * @see #getPhase()
	 * @generated
	 */
	void setPhase(String value);

} // Q7Info
