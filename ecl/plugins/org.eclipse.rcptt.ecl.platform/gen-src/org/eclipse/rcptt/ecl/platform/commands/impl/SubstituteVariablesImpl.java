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
package org.eclipse.rcptt.ecl.platform.commands.impl;



import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;
import org.eclipse.rcptt.ecl.platform.commands.CommandsPackage;
import org.eclipse.rcptt.ecl.platform.commands.SubstituteVariables;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Substitute Variables</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.commands.impl.SubstituteVariablesImpl#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.commands.impl.SubstituteVariablesImpl#isIgnoreUndefined <em>Ignore Undefined</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SubstituteVariablesImpl extends CommandImpl implements SubstituteVariables {
	/**
	 * The default value of the '{@link #getExpression() <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExpression()
	 * @generated
	 * @ordered
	 */
	protected static final String EXPRESSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getExpression() <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExpression()
	 * @generated
	 * @ordered
	 */
	protected String expression = EXPRESSION_EDEFAULT;

	/**
	 * The default value of the '{@link #isIgnoreUndefined() <em>Ignore Undefined</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIgnoreUndefined()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IGNORE_UNDEFINED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIgnoreUndefined() <em>Ignore Undefined</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIgnoreUndefined()
	 * @generated
	 * @ordered
	 */
	protected boolean ignoreUndefined = IGNORE_UNDEFINED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SubstituteVariablesImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CommandsPackage.Literals.SUBSTITUTE_VARIABLES;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getExpression() {
		return expression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExpression(String newExpression) {
		String oldExpression = expression;
		expression = newExpression;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackage.SUBSTITUTE_VARIABLES__EXPRESSION, oldExpression, expression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIgnoreUndefined() {
		return ignoreUndefined;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIgnoreUndefined(boolean newIgnoreUndefined) {
		boolean oldIgnoreUndefined = ignoreUndefined;
		ignoreUndefined = newIgnoreUndefined;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackage.SUBSTITUTE_VARIABLES__IGNORE_UNDEFINED, oldIgnoreUndefined, ignoreUndefined));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case CommandsPackage.SUBSTITUTE_VARIABLES__EXPRESSION:
				return getExpression();
			case CommandsPackage.SUBSTITUTE_VARIABLES__IGNORE_UNDEFINED:
				return isIgnoreUndefined();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case CommandsPackage.SUBSTITUTE_VARIABLES__EXPRESSION:
				setExpression((String)newValue);
				return;
			case CommandsPackage.SUBSTITUTE_VARIABLES__IGNORE_UNDEFINED:
				setIgnoreUndefined((Boolean)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case CommandsPackage.SUBSTITUTE_VARIABLES__EXPRESSION:
				setExpression(EXPRESSION_EDEFAULT);
				return;
			case CommandsPackage.SUBSTITUTE_VARIABLES__IGNORE_UNDEFINED:
				setIgnoreUndefined(IGNORE_UNDEFINED_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case CommandsPackage.SUBSTITUTE_VARIABLES__EXPRESSION:
				return EXPRESSION_EDEFAULT == null ? expression != null : !EXPRESSION_EDEFAULT.equals(expression);
			case CommandsPackage.SUBSTITUTE_VARIABLES__IGNORE_UNDEFINED:
				return ignoreUndefined != IGNORE_UNDEFINED_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (expression: ");
		result.append(expression);
		result.append(", ignoreUndefined: ");
		result.append(ignoreUndefined);
		result.append(')');
		return result.toString();
	}

} //SubstituteVariablesImpl
