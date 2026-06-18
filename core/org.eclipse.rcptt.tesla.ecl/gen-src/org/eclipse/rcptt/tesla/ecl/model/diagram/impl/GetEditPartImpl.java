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
package org.eclipse.rcptt.tesla.ecl.model.diagram.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.rcptt.ecl.core.impl.CommandImpl;

import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.diagram.DiagramPackage;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetEditPart;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Get Edit Part</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getAfter <em>After</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getIndex <em>Index</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getPath <em>Path</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getFeature <em>Feature</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getClassName <em>Class Name</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getText <em>Text</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.diagram.impl.GetEditPartImpl#getCustomId <em>Custom Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GetEditPartImpl extends CommandImpl implements GetEditPart {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAfter() <em>After</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getAfter()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler after;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected String type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getIndex() <em>Index</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getIndex()
	 * @generated
	 * @ordered
	 */
	protected static final Integer INDEX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIndex() <em>Index</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getIndex()
	 * @generated
	 * @ordered
	 */
	protected Integer index = INDEX_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParent() <em>Parent</em>}' reference.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getParent()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler parent;

	/**
	 * The default value of the '{@link #getPath() <em>Path</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getPath()
	 * @generated
	 * @ordered
	 */
	protected static final String PATH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPath() <em>Path</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getPath()
	 * @generated
	 * @ordered
	 */
	protected String path = PATH_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getFeature() <em>Feature</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFeature()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeature() <em>Feature</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getFeature()
	 * @generated
	 * @ordered
	 */
	protected String feature = FEATURE_EDEFAULT;

	/**
	 * The default value of the '{@link #getClassName() <em>Class Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getClassName()
	 * @generated
	 * @ordered
	 */
	protected static final String CLASS_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getClassName() <em>Class Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getClassName()
	 * @generated
	 * @ordered
	 */
	protected String className = CLASS_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getText() <em>Text</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getText()
	 * @generated
	 * @ordered
	 */
	protected static final String TEXT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getText() <em>Text</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getText()
	 * @generated
	 * @ordered
	 */
	protected String text = TEXT_EDEFAULT;

	/**
	 * The default value of the '{@link #getCustomId() <em>Custom Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCustomId()
	 * @generated
	 * @ordered
	 */
	protected static final String CUSTOM_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCustomId() <em>Custom Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCustomId()
	 * @generated
	 * @ordered
	 */
	protected String customId = CUSTOM_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected GetEditPartImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DiagramPackage.Literals.GET_EDIT_PART;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ControlHandler getAfter() {
		if (after != null && after.eIsProxy()) {
			InternalEObject oldAfter = (InternalEObject)after;
			after = (ControlHandler)eResolveProxy(oldAfter);
			if (after != oldAfter) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DiagramPackage.GET_EDIT_PART__AFTER, oldAfter, after));
			}
		}
		return after;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler basicGetAfter() {
		return after;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAfter(ControlHandler newAfter) {
		ControlHandler oldAfter = after;
		after = newAfter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__AFTER, oldAfter, after));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getIndex() {
		return index;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIndex(Integer newIndex) {
		Integer oldIndex = index;
		index = newIndex;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__INDEX, oldIndex, index));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ControlHandler getParent() {
		if (parent != null && parent.eIsProxy()) {
			InternalEObject oldParent = (InternalEObject)parent;
			parent = (ControlHandler)eResolveProxy(oldParent);
			if (parent != oldParent) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DiagramPackage.GET_EDIT_PART__PARENT, oldParent, parent));
			}
		}
		return parent;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler basicGetParent() {
		return parent;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setParent(ControlHandler newParent) {
		ControlHandler oldParent = parent;
		parent = newParent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__PARENT, oldParent, parent));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(String newType) {
		String oldType = type;
		type = newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPath(String newPath) {
		String oldPath = path;
		path = newPath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__PATH, oldPath, path));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFeature() {
		return feature;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeature(String newFeature) {
		String oldFeature = feature;
		feature = newFeature;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__FEATURE, oldFeature, feature));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getClassName() {
		return className;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setClassName(String newClassName) {
		String oldClassName = className;
		className = newClassName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__CLASS_NAME, oldClassName, className));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText() {
		return text;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setText(String newText) {
		String oldText = text;
		text = newText;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__TEXT, oldText, text));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCustomId() {
		return customId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCustomId(String newCustomId) {
		String oldCustomId = customId;
		customId = newCustomId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramPackage.GET_EDIT_PART__CUSTOM_ID, oldCustomId, customId));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DiagramPackage.GET_EDIT_PART__ID:
				return getId();
			case DiagramPackage.GET_EDIT_PART__AFTER:
				if (resolve) return getAfter();
				return basicGetAfter();
			case DiagramPackage.GET_EDIT_PART__TYPE:
				return getType();
			case DiagramPackage.GET_EDIT_PART__INDEX:
				return getIndex();
			case DiagramPackage.GET_EDIT_PART__PARENT:
				if (resolve) return getParent();
				return basicGetParent();
			case DiagramPackage.GET_EDIT_PART__PATH:
				return getPath();
			case DiagramPackage.GET_EDIT_PART__NAME:
				return getName();
			case DiagramPackage.GET_EDIT_PART__FEATURE:
				return getFeature();
			case DiagramPackage.GET_EDIT_PART__CLASS_NAME:
				return getClassName();
			case DiagramPackage.GET_EDIT_PART__TEXT:
				return getText();
			case DiagramPackage.GET_EDIT_PART__CUSTOM_ID:
				return getCustomId();
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
			case DiagramPackage.GET_EDIT_PART__ID:
				setId((String)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__AFTER:
				setAfter((ControlHandler)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__TYPE:
				setType((String)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__INDEX:
				setIndex((Integer)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__PARENT:
				setParent((ControlHandler)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__PATH:
				setPath((String)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__NAME:
				setName((String)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__FEATURE:
				setFeature((String)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__CLASS_NAME:
				setClassName((String)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__TEXT:
				setText((String)newValue);
				return;
			case DiagramPackage.GET_EDIT_PART__CUSTOM_ID:
				setCustomId((String)newValue);
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
			case DiagramPackage.GET_EDIT_PART__ID:
				setId(ID_EDEFAULT);
				return;
			case DiagramPackage.GET_EDIT_PART__AFTER:
				setAfter((ControlHandler)null);
				return;
			case DiagramPackage.GET_EDIT_PART__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case DiagramPackage.GET_EDIT_PART__INDEX:
				setIndex(INDEX_EDEFAULT);
				return;
			case DiagramPackage.GET_EDIT_PART__PARENT:
				setParent((ControlHandler)null);
				return;
			case DiagramPackage.GET_EDIT_PART__PATH:
				setPath(PATH_EDEFAULT);
				return;
			case DiagramPackage.GET_EDIT_PART__NAME:
				setName(NAME_EDEFAULT);
				return;
			case DiagramPackage.GET_EDIT_PART__FEATURE:
				setFeature(FEATURE_EDEFAULT);
				return;
			case DiagramPackage.GET_EDIT_PART__CLASS_NAME:
				setClassName(CLASS_NAME_EDEFAULT);
				return;
			case DiagramPackage.GET_EDIT_PART__TEXT:
				setText(TEXT_EDEFAULT);
				return;
			case DiagramPackage.GET_EDIT_PART__CUSTOM_ID:
				setCustomId(CUSTOM_ID_EDEFAULT);
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
			case DiagramPackage.GET_EDIT_PART__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case DiagramPackage.GET_EDIT_PART__AFTER:
				return after != null;
			case DiagramPackage.GET_EDIT_PART__TYPE:
				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
			case DiagramPackage.GET_EDIT_PART__INDEX:
				return INDEX_EDEFAULT == null ? index != null : !INDEX_EDEFAULT.equals(index);
			case DiagramPackage.GET_EDIT_PART__PARENT:
				return parent != null;
			case DiagramPackage.GET_EDIT_PART__PATH:
				return PATH_EDEFAULT == null ? path != null : !PATH_EDEFAULT.equals(path);
			case DiagramPackage.GET_EDIT_PART__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case DiagramPackage.GET_EDIT_PART__FEATURE:
				return FEATURE_EDEFAULT == null ? feature != null : !FEATURE_EDEFAULT.equals(feature);
			case DiagramPackage.GET_EDIT_PART__CLASS_NAME:
				return CLASS_NAME_EDEFAULT == null ? className != null : !CLASS_NAME_EDEFAULT.equals(className);
			case DiagramPackage.GET_EDIT_PART__TEXT:
				return TEXT_EDEFAULT == null ? text != null : !TEXT_EDEFAULT.equals(text);
			case DiagramPackage.GET_EDIT_PART__CUSTOM_ID:
				return CUSTOM_ID_EDEFAULT == null ? customId != null : !CUSTOM_ID_EDEFAULT.equals(customId);
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
		result.append(" (id: ");
		result.append(id);
		result.append(", type: ");
		result.append(type);
		result.append(", index: ");
		result.append(index);
		result.append(", path: ");
		result.append(path);
		result.append(", name: ");
		result.append(name);
		result.append(", feature: ");
		result.append(feature);
		result.append(", className: ");
		result.append(className);
		result.append(", text: ");
		result.append(text);
		result.append(", customId: ");
		result.append(customId);
		result.append(')');
		return result.toString();
	}

} // GetEditPartImpl
