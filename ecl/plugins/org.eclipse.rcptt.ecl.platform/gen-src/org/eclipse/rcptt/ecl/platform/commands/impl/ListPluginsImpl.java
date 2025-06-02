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
import org.eclipse.rcptt.ecl.platform.commands.ListPlugins;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>List Plugins</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.ecl.platform.commands.impl.ListPluginsImpl#isIncludeDependencies <em>Include Dependencies</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ListPluginsImpl extends CommandImpl implements ListPlugins {
	/**
	 * The default value of the '{@link #isIncludeDependencies() <em>Include Dependencies</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIncludeDependencies()
	 * @generated
	 * @ordered
	 */
	protected static final boolean INCLUDE_DEPENDENCIES_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIncludeDependencies() <em>Include Dependencies</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIncludeDependencies()
	 * @generated
	 * @ordered
	 */
	protected boolean includeDependencies = INCLUDE_DEPENDENCIES_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ListPluginsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CommandsPackage.Literals.LIST_PLUGINS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIncludeDependencies() {
		return includeDependencies;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIncludeDependencies(boolean newIncludeDependencies) {
		boolean oldIncludeDependencies = includeDependencies;
		includeDependencies = newIncludeDependencies;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackage.LIST_PLUGINS__INCLUDE_DEPENDENCIES, oldIncludeDependencies, includeDependencies));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case CommandsPackage.LIST_PLUGINS__INCLUDE_DEPENDENCIES:
				return isIncludeDependencies();
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
			case CommandsPackage.LIST_PLUGINS__INCLUDE_DEPENDENCIES:
				setIncludeDependencies((Boolean)newValue);
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
			case CommandsPackage.LIST_PLUGINS__INCLUDE_DEPENDENCIES:
				setIncludeDependencies(INCLUDE_DEPENDENCIES_EDEFAULT);
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
			case CommandsPackage.LIST_PLUGINS__INCLUDE_DEPENDENCIES:
				return includeDependencies != INCLUDE_DEPENDENCIES_EDEFAULT;
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
		result.append(" (includeDependencies: ");
		result.append(includeDependencies);
		result.append(')');
		return result.toString();
	}

} //ListPluginsImpl
