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

import org.eclipse.rcptt.tesla.core.protocol.ElementKind;

import org.eclipse.rcptt.tesla.core.protocol.raw.Element;

import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Control Handler</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getCustomKindId <em>Custom Kind Id</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getText <em>Text</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getIndex <em>Index</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getPath <em>Path</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getIndexes <em>Indexes</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getAfter <em>After</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getResolved <em>Resolved</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getRow <em>Row</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getColumn <em>Column</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.rcptt.tesla.ecl.model.impl.ControlHandlerImpl#getRawImage <em>Raw Image</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ControlHandlerImpl extends EObjectImpl implements ControlHandler {
	/**
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final ElementKind KIND_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected ElementKind kind = KIND_EDEFAULT;

	/**
	 * The default value of the '{@link #getCustomKindId() <em>Custom Kind Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCustomKindId()
	 * @generated
	 * @ordered
	 */
	protected static final String CUSTOM_KIND_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCustomKindId() <em>Custom Kind Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCustomKindId()
	 * @generated
	 * @ordered
	 */
	protected String customKindId = CUSTOM_KIND_ID_EDEFAULT;

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
	 * The default value of the '{@link #getPath() <em>Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPath()
	 * @generated
	 * @ordered
	 */
	protected static final String PATH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPath() <em>Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPath()
	 * @generated
	 * @ordered
	 */
	protected String path = PATH_EDEFAULT;

	/**
	 * The default value of the '{@link #getIndexes() <em>Indexes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndexes()
	 * @generated
	 * @ordered
	 */
	protected static final String INDEXES_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIndexes() <em>Indexes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndexes()
	 * @generated
	 * @ordered
	 */
	protected String indexes = INDEXES_EDEFAULT;

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
	 * The cached value of the '{@link #getParent() <em>Parent</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParent()
	 * @generated
	 * @ordered
	 */
	protected ControlHandler parent;

	/**
	 * The cached value of the '{@link #getResolved() <em>Resolved</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResolved()
	 * @generated
	 * @ordered
	 */
	protected Element resolved;

	/**
	 * The default value of the '{@link #getRow() <em>Row</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRow()
	 * @generated
	 * @ordered
	 */
	protected static final Integer ROW_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRow() <em>Row</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRow()
	 * @generated
	 * @ordered
	 */
	protected Integer row = ROW_EDEFAULT;

	/**
	 * The default value of the '{@link #getColumn() <em>Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumn()
	 * @generated
	 * @ordered
	 */
	protected static final Integer COLUMN_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getColumn() <em>Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumn()
	 * @generated
	 * @ordered
	 */
	protected Integer column = COLUMN_EDEFAULT;

	/**
	 * The default value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected static final int X_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected int x = X_EDEFAULT;

	/**
	 * The default value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected static final int Y_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected int y = Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getRawImage() <em>Raw Image</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRawImage()
	 * @generated
	 * @ordered
	 */
	protected static final String RAW_IMAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRawImage() <em>Raw Image</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRawImage()
	 * @generated
	 * @ordered
	 */
	protected String rawImage = RAW_IMAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ControlHandlerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TeslaPackage.Literals.CONTROL_HANDLER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ElementKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(ElementKind newKind) {
		ElementKind oldKind = kind;
		kind = newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCustomKindId() {
		return customKindId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCustomKindId(String newCustomKindId) {
		String oldCustomKindId = customKindId;
		customKindId = newCustomKindId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__CUSTOM_KIND_ID, oldCustomKindId, customKindId));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__TEXT, oldText, text));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__TYPE, oldType, type));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__INDEX, oldIndex, index));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPath(String newPath) {
		String oldPath = path;
		path = newPath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__PATH, oldPath, path));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getIndexes() {
		return indexes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIndexes(String newIndexes) {
		String oldIndexes = indexes;
		indexes = newIndexes;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__INDEXES, oldIndexes, indexes));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.CONTROL_HANDLER__AFTER, oldAfter, after));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__AFTER, oldAfter, after));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.CONTROL_HANDLER__PARENT, oldParent, parent));
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
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__PARENT, oldParent, parent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Element getResolved() {
		if (resolved != null && resolved.eIsProxy()) {
			InternalEObject oldResolved = (InternalEObject)resolved;
			resolved = (Element)eResolveProxy(oldResolved);
			if (resolved != oldResolved) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TeslaPackage.CONTROL_HANDLER__RESOLVED, oldResolved, resolved));
			}
		}
		return resolved;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Element basicGetResolved() {
		return resolved;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResolved(Element newResolved) {
		Element oldResolved = resolved;
		resolved = newResolved;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__RESOLVED, oldResolved, resolved));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getRow() {
		return row;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRow(Integer newRow) {
		Integer oldRow = row;
		row = newRow;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__ROW, oldRow, row));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Integer getColumn() {
		return column;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setColumn(Integer newColumn) {
		Integer oldColumn = column;
		column = newColumn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__COLUMN, oldColumn, column));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getX() {
		return x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setX(int newX) {
		int oldX = x;
		x = newX;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__X, oldX, x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getY() {
		return y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setY(int newY) {
		int oldY = y;
		y = newY;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__Y, oldY, y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRawImage() {
		return rawImage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRawImage(String newRawImage) {
		String oldRawImage = rawImage;
		rawImage = newRawImage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TeslaPackage.CONTROL_HANDLER__RAW_IMAGE, oldRawImage, rawImage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case TeslaPackage.CONTROL_HANDLER__KIND:
				return getKind();
			case TeslaPackage.CONTROL_HANDLER__CUSTOM_KIND_ID:
				return getCustomKindId();
			case TeslaPackage.CONTROL_HANDLER__TEXT:
				return getText();
			case TeslaPackage.CONTROL_HANDLER__TYPE:
				return getType();
			case TeslaPackage.CONTROL_HANDLER__INDEX:
				return getIndex();
			case TeslaPackage.CONTROL_HANDLER__PATH:
				return getPath();
			case TeslaPackage.CONTROL_HANDLER__INDEXES:
				return getIndexes();
			case TeslaPackage.CONTROL_HANDLER__AFTER:
				if (resolve) return getAfter();
				return basicGetAfter();
			case TeslaPackage.CONTROL_HANDLER__PARENT:
				if (resolve) return getParent();
				return basicGetParent();
			case TeslaPackage.CONTROL_HANDLER__RESOLVED:
				if (resolve) return getResolved();
				return basicGetResolved();
			case TeslaPackage.CONTROL_HANDLER__ROW:
				return getRow();
			case TeslaPackage.CONTROL_HANDLER__COLUMN:
				return getColumn();
			case TeslaPackage.CONTROL_HANDLER__X:
				return getX();
			case TeslaPackage.CONTROL_HANDLER__Y:
				return getY();
			case TeslaPackage.CONTROL_HANDLER__RAW_IMAGE:
				return getRawImage();
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
			case TeslaPackage.CONTROL_HANDLER__KIND:
				setKind((ElementKind)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__CUSTOM_KIND_ID:
				setCustomKindId((String)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__TEXT:
				setText((String)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__TYPE:
				setType((String)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__INDEX:
				setIndex((Integer)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__PATH:
				setPath((String)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__INDEXES:
				setIndexes((String)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__AFTER:
				setAfter((ControlHandler)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__PARENT:
				setParent((ControlHandler)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__RESOLVED:
				setResolved((Element)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__ROW:
				setRow((Integer)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__COLUMN:
				setColumn((Integer)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__X:
				setX((Integer)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__Y:
				setY((Integer)newValue);
				return;
			case TeslaPackage.CONTROL_HANDLER__RAW_IMAGE:
				setRawImage((String)newValue);
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
			case TeslaPackage.CONTROL_HANDLER__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__CUSTOM_KIND_ID:
				setCustomKindId(CUSTOM_KIND_ID_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__TEXT:
				setText(TEXT_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__INDEX:
				setIndex(INDEX_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__PATH:
				setPath(PATH_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__INDEXES:
				setIndexes(INDEXES_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__AFTER:
				setAfter((ControlHandler)null);
				return;
			case TeslaPackage.CONTROL_HANDLER__PARENT:
				setParent((ControlHandler)null);
				return;
			case TeslaPackage.CONTROL_HANDLER__RESOLVED:
				setResolved((Element)null);
				return;
			case TeslaPackage.CONTROL_HANDLER__ROW:
				setRow(ROW_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__COLUMN:
				setColumn(COLUMN_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__X:
				setX(X_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__Y:
				setY(Y_EDEFAULT);
				return;
			case TeslaPackage.CONTROL_HANDLER__RAW_IMAGE:
				setRawImage(RAW_IMAGE_EDEFAULT);
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
			case TeslaPackage.CONTROL_HANDLER__KIND:
				return KIND_EDEFAULT == null ? kind != null : !KIND_EDEFAULT.equals(kind);
			case TeslaPackage.CONTROL_HANDLER__CUSTOM_KIND_ID:
				return CUSTOM_KIND_ID_EDEFAULT == null ? customKindId != null : !CUSTOM_KIND_ID_EDEFAULT.equals(customKindId);
			case TeslaPackage.CONTROL_HANDLER__TEXT:
				return TEXT_EDEFAULT == null ? text != null : !TEXT_EDEFAULT.equals(text);
			case TeslaPackage.CONTROL_HANDLER__TYPE:
				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
			case TeslaPackage.CONTROL_HANDLER__INDEX:
				return INDEX_EDEFAULT == null ? index != null : !INDEX_EDEFAULT.equals(index);
			case TeslaPackage.CONTROL_HANDLER__PATH:
				return PATH_EDEFAULT == null ? path != null : !PATH_EDEFAULT.equals(path);
			case TeslaPackage.CONTROL_HANDLER__INDEXES:
				return INDEXES_EDEFAULT == null ? indexes != null : !INDEXES_EDEFAULT.equals(indexes);
			case TeslaPackage.CONTROL_HANDLER__AFTER:
				return after != null;
			case TeslaPackage.CONTROL_HANDLER__PARENT:
				return parent != null;
			case TeslaPackage.CONTROL_HANDLER__RESOLVED:
				return resolved != null;
			case TeslaPackage.CONTROL_HANDLER__ROW:
				return ROW_EDEFAULT == null ? row != null : !ROW_EDEFAULT.equals(row);
			case TeslaPackage.CONTROL_HANDLER__COLUMN:
				return COLUMN_EDEFAULT == null ? column != null : !COLUMN_EDEFAULT.equals(column);
			case TeslaPackage.CONTROL_HANDLER__X:
				return x != X_EDEFAULT;
			case TeslaPackage.CONTROL_HANDLER__Y:
				return y != Y_EDEFAULT;
			case TeslaPackage.CONTROL_HANDLER__RAW_IMAGE:
				return RAW_IMAGE_EDEFAULT == null ? rawImage != null : !RAW_IMAGE_EDEFAULT.equals(rawImage);
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
		result.append(" (kind: ");
		result.append(kind);
		result.append(", customKindId: ");
		result.append(customKindId);
		result.append(", text: ");
		result.append(text);
		result.append(", type: ");
		result.append(type);
		result.append(", index: ");
		result.append(index);
		result.append(", path: ");
		result.append(path);
		result.append(", indexes: ");
		result.append(indexes);
		result.append(", row: ");
		result.append(row);
		result.append(", column: ");
		result.append(column);
		result.append(", x: ");
		result.append(x);
		result.append(", y: ");
		result.append(y);
		result.append(", rawImage: ");
		result.append(rawImage);
		result.append(')');
		return result.toString();
	}

} //ControlHandlerImpl
