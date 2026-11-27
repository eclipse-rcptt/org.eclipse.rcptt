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

import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.GetWindow;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Get Window</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetWindowImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetWindowImpl#getAfter <em>After</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetWindowImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetWindowImpl#getIndex <em>Index</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetWindowImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetWindowImpl#getText <em>Text</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetWindowImpl#getFrom <em>From</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.GetWindowImpl#getClass_ <em>Class</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GetWindowImpl extends CommandImpl implements GetWindow {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAfter() <em>After</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAfter()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler after;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected String type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getIndex() <em>Index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndex()
	 * @generated
	 * @ordered
	 */
	protected static final Integer INDEX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIndex() <em>Index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndex()
	 * @generated
	 * @ordered
	 */
	protected Integer index = INDEX_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParent() <em>Parent</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParent()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler parent;

	/**
	 * The default value of the '{@link #getText() <em>Text</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getText()
	 * @generated
	 * @ordered
	 */
	protected static final String TEXT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getText() <em>Text</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getText()
	 * @generated
	 * @ordered
	 */
	protected String text = TEXT_EDEFAULT;

	/**
	 * The default value of the '{@link #getFrom() <em>From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFrom()
	 * @generated
	 * @ordered
	 */
	protected static final String FROM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFrom() <em>From</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFrom()
	 * @generated
	 * @ordered
	 */
	protected String from = FROM_EDEFAULT;

	/**
	 * The default value of the '{@link #getClass_() <em>Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClass_()
	 * @generated
	 * @ordered
	 */
	protected static final String CLASS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getClass_() <em>Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClass_()
	 * @generated
	 * @ordered
	 */
	protected String class_ = CLASS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GetWindowImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.GET_WINDOW;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_WINDOW__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ControlHandler getAfter() {
		if (after != null && after.eIsProxy()) {
			InternalEObject oldAfter = (InternalEObject)after;
			after = (ControlHandler)eResolveProxy(oldAfter);
			if (after != oldAfter) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.GET_WINDOW__AFTER, oldAfter, after));
			}
		}
		return after;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler basicGetAfter() {
		return after;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAfter(ControlHandler newAfter) {
		ControlHandler oldAfter = after;
		after = newAfter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_WINDOW__AFTER, oldAfter, after));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getIndex() {
		return index;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIndex(Integer newIndex) {
		Integer oldIndex = index;
		index = newIndex;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_WINDOW__INDEX, oldIndex, index));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ControlHandler getParent() {
		if (parent != null && parent.eIsProxy()) {
			InternalEObject oldParent = (InternalEObject)parent;
			parent = (ControlHandler)eResolveProxy(oldParent);
			if (parent != oldParent) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.GET_WINDOW__PARENT, oldParent, parent));
			}
		}
		return parent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler basicGetParent() {
		return parent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setParent(ControlHandler newParent) {
		ControlHandler oldParent = parent;
		parent = newParent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_WINDOW__PARENT, oldParent, parent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(String newType) {
		String oldType = type;
		type = newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_WINDOW__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setText(String newText) {
		String oldText = text;
		text = newText;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_WINDOW__TEXT, oldText, text));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFrom() {
		return from;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFrom(String newFrom) {
		String oldFrom = from;
		from = newFrom;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_WINDOW__FROM, oldFrom, from));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getClass_() {
		return class_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setClass(String newClass) {
		String oldClass = class_;
		class_ = newClass;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.GET_WINDOW__CLASS, oldClass, class_));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.GET_WINDOW__ID:
				return getId();
			case TeslaPackage.GET_WINDOW__AFTER:
				if (resolve) return getAfter();
				return basicGetAfter();
			case TeslaPackage.GET_WINDOW__TYPE:
				return getType();
			case TeslaPackage.GET_WINDOW__INDEX:
				return getIndex();
			case TeslaPackage.GET_WINDOW__PARENT:
				if (resolve) return getParent();
				return basicGetParent();
			case TeslaPackage.GET_WINDOW__TEXT:
				return getText();
			case TeslaPackage.GET_WINDOW__FROM:
				return getFrom();
			case TeslaPackage.GET_WINDOW__CLASS:
				return getClass_();
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
			case TeslaPackage.GET_WINDOW__ID:
				setId((String)newValue);
				return;
			case TeslaPackage.GET_WINDOW__AFTER:
				setAfter((ControlHandler)newValue);
				return;
			case TeslaPackage.GET_WINDOW__TYPE:
				setType((String)newValue);
				return;
			case TeslaPackage.GET_WINDOW__INDEX:
				setIndex((Integer)newValue);
				return;
			case TeslaPackage.GET_WINDOW__PARENT:
				setParent((ControlHandler)newValue);
				return;
			case TeslaPackage.GET_WINDOW__TEXT:
				setText((String)newValue);
				return;
			case TeslaPackage.GET_WINDOW__FROM:
				setFrom((String)newValue);
				return;
			case TeslaPackage.GET_WINDOW__CLASS:
				setClass((String)newValue);
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
			case TeslaPackage.GET_WINDOW__ID:
				setId(ID_EDEFAULT);
				return;
			case TeslaPackage.GET_WINDOW__AFTER:
				setAfter((ControlHandler)null);
				return;
			case TeslaPackage.GET_WINDOW__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case TeslaPackage.GET_WINDOW__INDEX:
				setIndex(INDEX_EDEFAULT);
				return;
			case TeslaPackage.GET_WINDOW__PARENT:
				setParent((ControlHandler)null);
				return;
			case TeslaPackage.GET_WINDOW__TEXT:
				setText(TEXT_EDEFAULT);
				return;
			case TeslaPackage.GET_WINDOW__FROM:
				setFrom(FROM_EDEFAULT);
				return;
			case TeslaPackage.GET_WINDOW__CLASS:
				setClass(CLASS_EDEFAULT);
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
			case TeslaPackage.GET_WINDOW__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case TeslaPackage.GET_WINDOW__AFTER:
				return after != null;
			case TeslaPackage.GET_WINDOW__TYPE:
				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
			case TeslaPackage.GET_WINDOW__INDEX:
				return INDEX_EDEFAULT == null ? index != null : !INDEX_EDEFAULT.equals(index);
			case TeslaPackage.GET_WINDOW__PARENT:
				return parent != null;
			case TeslaPackage.GET_WINDOW__TEXT:
				return TEXT_EDEFAULT == null ? text != null : !TEXT_EDEFAULT.equals(text);
			case TeslaPackage.GET_WINDOW__FROM:
				return FROM_EDEFAULT == null ? from != null : !FROM_EDEFAULT.equals(from);
			case TeslaPackage.GET_WINDOW__CLASS:
				return CLASS_EDEFAULT == null ? class_ != null : !CLASS_EDEFAULT.equals(class_);
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
		result.append(" (id: ");
		result.append(id);
		result.append(", type: ");
		result.append(type);
		result.append(", index: ");
		result.append(index);
		result.append(", text: ");
		result.append(text);
		result.append(", from: ");
		result.append(from);
		result.append(", class: ");
		result.append(class_);
		result.append(')');
		return result.toString();
	}

} //GetWindowImpl
