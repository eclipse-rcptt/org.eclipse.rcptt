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
package org.eclipse.rcptt.tesla.ecl.model.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.ecl.model.GetPropertyNodes;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Get Property Nodes</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetPropertyNodesImpl#getElement <em>Element</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetPropertyNodesImpl#getNodePath <em>Node Path</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GetPropertyNodesImpl extends CommandImpl implements GetPropertyNodes {
	/**
	 * The cached value of the '{@link #getElement() <em>Element</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getElement()
	 * @generated
	 * @ordered
	 */
	protected Element element;

	/**
	 * The default value of the '{@link #getNodePath() <em>Node Path</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getNodePath()
	 * @generated
	 * @ordered
	 */
	protected static final String NODE_PATH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNodePath() <em>Node Path</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getNodePath()
	 * @generated
	 * @ordered
	 */
	protected String nodePath = NODE_PATH_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected GetPropertyNodesImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.GET_PROPERTY_NODES;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Element getElement() {
		if (element != null && element.eIsProxy()) {
			InternalEObject oldElement = (InternalEObject)element;
			element = (Element)eResolveProxy(oldElement);
			if (element != oldElement) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.GET_PROPERTY_NODES__ELEMENT, oldElement, element));
			}
		}
		return element;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Element basicGetElement() {
		return element;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setElement(Element newElement) {
		Element oldElement = element;
		element = newElement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_PROPERTY_NODES__ELEMENT, oldElement, element));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNodePath() {
		return nodePath;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNodePath(String newNodePath) {
		String oldNodePath = nodePath;
		nodePath = newNodePath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_PROPERTY_NODES__NODE_PATH, oldNodePath, nodePath));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.GET_PROPERTY_NODES__ELEMENT:
				if (resolve) return getElement();
				return basicGetElement();
			case TeslaPackage.GET_PROPERTY_NODES__NODE_PATH:
				return getNodePath();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case TeslaPackage.GET_PROPERTY_NODES__ELEMENT:
				setElement((Element)newValue);
				return;
			case TeslaPackage.GET_PROPERTY_NODES__NODE_PATH:
				setNodePath((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case TeslaPackage.GET_PROPERTY_NODES__ELEMENT:
				setElement((Element)null);
				return;
			case TeslaPackage.GET_PROPERTY_NODES__NODE_PATH:
				setNodePath(NODE_PATH_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case TeslaPackage.GET_PROPERTY_NODES__ELEMENT:
				return element != null;
			case TeslaPackage.GET_PROPERTY_NODES__NODE_PATH:
				return NODE_PATH_EDEFAULT == null ? nodePath != null : !NODE_PATH_EDEFAULT.equals(nodePath);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (nodePath: ");
		result.append(nodePath);
		result.append(')');
		return result.toString();
	}

} // GetPropertyNodesImpl
