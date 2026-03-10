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

import org.eclipse.rcptt.tesla.ecl.model.diagram.*;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.rcptt.tesla.ecl.model.diagram.ActivateDirectEdit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.CancelDirectEdit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.CommitDirectEdit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.DiagramFactory;
import org.eclipse.rcptt.tesla.ecl.model.diagram.DiagramPackage;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetConnectionHandle;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetDiagram;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetEditPart;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetEntry;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetFigure;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetHandle;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPalette;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPaletteEntry;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPopupBarItem;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetSourceConnection;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetTargetConnection;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseAction;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseDoubleClick;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseDrag;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseEnter;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseExit;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseHover;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseMove;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MousePress;
import org.eclipse.rcptt.tesla.ecl.model.diagram.MouseRelease;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!--
 * end-user-doc -->
 * @generated
 */
public class DiagramFactoryImpl extends EFactoryImpl implements DiagramFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	public static DiagramFactory init() {
		try {
			DiagramFactory theDiagramFactory = (DiagramFactory)EPackage.Registry.INSTANCE.getEFactory(DiagramPackage.eNS_URI);
			if (theDiagramFactory != null) {
				return theDiagramFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new DiagramFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	public DiagramFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case DiagramPackage.GET_DIAGRAM: return createGetDiagram();
			case DiagramPackage.GET_PALETTE: return createGetPalette();
			case DiagramPackage.GET_EDIT_PART: return createGetEditPart();
			case DiagramPackage.GET_FIGURE: return createGetFigure();
			case DiagramPackage.GET_ENTRY: return createGetEntry();
			case DiagramPackage.ACTIVATE_DIRECT_EDIT: return createActivateDirectEdit();
			case DiagramPackage.COMMIT_DIRECT_EDIT: return createCommitDirectEdit();
			case DiagramPackage.CANCEL_DIRECT_EDIT: return createCancelDirectEdit();
			case DiagramPackage.MOUSE_ACTION: return createMouseAction();
			case DiagramPackage.MOUSE_MOVE: return createMouseMove();
			case DiagramPackage.MOUSE_DRAG: return createMouseDrag();
			case DiagramPackage.MOUSE_PRESS: return createMousePress();
			case DiagramPackage.MOUSE_RELEASE: return createMouseRelease();
			case DiagramPackage.MOUSE_DOUBLE_CLICK: return createMouseDoubleClick();
			case DiagramPackage.MOUSE_ENTER: return createMouseEnter();
			case DiagramPackage.MOUSE_EXIT: return createMouseExit();
			case DiagramPackage.MOUSE_HOVER: return createMouseHover();
			case DiagramPackage.GET_SOURCE_CONNECTION: return createGetSourceConnection();
			case DiagramPackage.GET_TARGET_CONNECTION: return createGetTargetConnection();
			case DiagramPackage.GET_POPUP_BAR_ITEM: return createGetPopupBarItem();
			case DiagramPackage.GET_CONNECTION_HANDLE: return createGetConnectionHandle();
			case DiagramPackage.GET_PALETTE_ENTRY: return createGetPaletteEntry();
			case DiagramPackage.GET_HANDLE: return createGetHandle();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetDiagram createGetDiagram() {
		GetDiagramImpl getDiagram = new GetDiagramImpl();
		return getDiagram;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetPalette createGetPalette() {
		GetPaletteImpl getPalette = new GetPaletteImpl();
		return getPalette;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetEditPart createGetEditPart() {
		GetEditPartImpl getEditPart = new GetEditPartImpl();
		return getEditPart;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetFigure createGetFigure() {
		GetFigureImpl getFigure = new GetFigureImpl();
		return getFigure;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetEntry createGetEntry() {
		GetEntryImpl getEntry = new GetEntryImpl();
		return getEntry;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ActivateDirectEdit createActivateDirectEdit() {
		ActivateDirectEditImpl activateDirectEdit = new ActivateDirectEditImpl();
		return activateDirectEdit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CommitDirectEdit createCommitDirectEdit() {
		CommitDirectEditImpl commitDirectEdit = new CommitDirectEditImpl();
		return commitDirectEdit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CancelDirectEdit createCancelDirectEdit() {
		CancelDirectEditImpl cancelDirectEdit = new CancelDirectEditImpl();
		return cancelDirectEdit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MouseAction createMouseAction() {
		MouseActionImpl mouseAction = new MouseActionImpl();
		return mouseAction;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MouseMove createMouseMove() {
		MouseMoveImpl mouseMove = new MouseMoveImpl();
		return mouseMove;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MouseDrag createMouseDrag() {
		MouseDragImpl mouseDrag = new MouseDragImpl();
		return mouseDrag;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MousePress createMousePress() {
		MousePressImpl mousePress = new MousePressImpl();
		return mousePress;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MouseRelease createMouseRelease() {
		MouseReleaseImpl mouseRelease = new MouseReleaseImpl();
		return mouseRelease;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MouseDoubleClick createMouseDoubleClick() {
		MouseDoubleClickImpl mouseDoubleClick = new MouseDoubleClickImpl();
		return mouseDoubleClick;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MouseEnter createMouseEnter() {
		MouseEnterImpl mouseEnter = new MouseEnterImpl();
		return mouseEnter;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MouseExit createMouseExit() {
		MouseExitImpl mouseExit = new MouseExitImpl();
		return mouseExit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MouseHover createMouseHover() {
		MouseHoverImpl mouseHover = new MouseHoverImpl();
		return mouseHover;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetSourceConnection createGetSourceConnection() {
		GetSourceConnectionImpl getSourceConnection = new GetSourceConnectionImpl();
		return getSourceConnection;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetTargetConnection createGetTargetConnection() {
		GetTargetConnectionImpl getTargetConnection = new GetTargetConnectionImpl();
		return getTargetConnection;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetPopupBarItem createGetPopupBarItem() {
		GetPopupBarItemImpl getPopupBarItem = new GetPopupBarItemImpl();
		return getPopupBarItem;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetConnectionHandle createGetConnectionHandle() {
		GetConnectionHandleImpl getConnectionHandle = new GetConnectionHandleImpl();
		return getConnectionHandle;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetPaletteEntry createGetPaletteEntry() {
		GetPaletteEntryImpl getPaletteEntry = new GetPaletteEntryImpl();
		return getPaletteEntry;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetHandle createGetHandle() {
		GetHandleImpl getHandle = new GetHandleImpl();
		return getHandle;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagramPackage getDiagramPackage() {
		return (DiagramPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static DiagramPackage getPackage() {
		return DiagramPackage.eINSTANCE;
	}

} // DiagramFactoryImpl
