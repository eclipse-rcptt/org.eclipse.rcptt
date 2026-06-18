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
package org.eclipse.rcptt.tesla.ecl.model.util;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.tesla.ecl.model.ActivateCellEdit;
import org.eclipse.rcptt.tesla.ecl.model.ApplyCellEdit;
import org.eclipse.rcptt.tesla.ecl.model.BoundControlHandle;
import org.eclipse.rcptt.tesla.ecl.model.CancelCellEdit;
import org.eclipse.rcptt.tesla.ecl.model.CellEdit;
import org.eclipse.rcptt.tesla.ecl.model.Check;
import org.eclipse.rcptt.tesla.ecl.model.CheckDownloadResult;
import org.eclipse.rcptt.tesla.ecl.model.Click;
import org.eclipse.rcptt.tesla.ecl.model.ClickColumn;
import org.eclipse.rcptt.tesla.ecl.model.ClickLink;
import org.eclipse.rcptt.tesla.ecl.model.ClickRuler;
import org.eclipse.rcptt.tesla.ecl.model.ClickText;
import org.eclipse.rcptt.tesla.ecl.model.Close;
import org.eclipse.rcptt.tesla.ecl.model.Collapse;
import org.eclipse.rcptt.tesla.ecl.model.CollapseAll;
import org.eclipse.rcptt.tesla.ecl.model.Contains;
import org.eclipse.rcptt.tesla.ecl.model.ContainsImage;
import org.eclipse.rcptt.tesla.ecl.model.ControlCommand;
import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.ControlNotFound;
import org.eclipse.rcptt.tesla.ecl.model.DeactivateCellEdit;
import org.eclipse.rcptt.tesla.ecl.model.Decrypt;
import org.eclipse.rcptt.tesla.ecl.model.DecryptResult;
import org.eclipse.rcptt.tesla.ecl.model.DoubleClick;
import org.eclipse.rcptt.tesla.ecl.model.DoubleClickRuler;
import org.eclipse.rcptt.tesla.ecl.model.DoubleClickText;
import org.eclipse.rcptt.tesla.ecl.model.DragAccept;
import org.eclipse.rcptt.tesla.ecl.model.DragAction;
import org.eclipse.rcptt.tesla.ecl.model.DragDetect;
import org.eclipse.rcptt.tesla.ecl.model.DragEnd;
import org.eclipse.rcptt.tesla.ecl.model.DragEnter;
import org.eclipse.rcptt.tesla.ecl.model.DragExit;
import org.eclipse.rcptt.tesla.ecl.model.DragOver;
import org.eclipse.rcptt.tesla.ecl.model.DragSetData;
import org.eclipse.rcptt.tesla.ecl.model.DragStart;
import org.eclipse.rcptt.tesla.ecl.model.Drop;
import org.eclipse.rcptt.tesla.ecl.model.Equals;
import org.eclipse.rcptt.tesla.ecl.model.ExecWithOptions;
import org.eclipse.rcptt.tesla.ecl.model.Expand;
import org.eclipse.rcptt.tesla.ecl.model.ExpandAll;
import org.eclipse.rcptt.tesla.ecl.model.FromRawKey;
import org.eclipse.rcptt.tesla.ecl.model.GetAboutMenu;
import org.eclipse.rcptt.tesla.ecl.model.GetAdvancedInfo;
import org.eclipse.rcptt.tesla.ecl.model.GetBanner;
import org.eclipse.rcptt.tesla.ecl.model.GetButton;
import org.eclipse.rcptt.tesla.ecl.model.GetByOs;
import org.eclipse.rcptt.tesla.ecl.model.GetCanvas;
import org.eclipse.rcptt.tesla.ecl.model.GetCell;
import org.eclipse.rcptt.tesla.ecl.model.GetCheckbox;
import org.eclipse.rcptt.tesla.ecl.model.GetColorSelector;
import org.eclipse.rcptt.tesla.ecl.model.GetColumnHeader;
import org.eclipse.rcptt.tesla.ecl.model.GetCombo;
import org.eclipse.rcptt.tesla.ecl.model.GetComboItems;
import org.eclipse.rcptt.tesla.ecl.model.GetControl;
import org.eclipse.rcptt.tesla.ecl.model.GetCoolbar;
import org.eclipse.rcptt.tesla.ecl.model.GetDateTime;
import org.eclipse.rcptt.tesla.ecl.model.GetEclipseWindow;
import org.eclipse.rcptt.tesla.ecl.model.GetEditbox;
import org.eclipse.rcptt.tesla.ecl.model.GetEditor;
import org.eclipse.rcptt.tesla.ecl.model.GetGroup;
import org.eclipse.rcptt.tesla.ecl.model.GetItem;
import org.eclipse.rcptt.tesla.ecl.model.GetItems;
import org.eclipse.rcptt.tesla.ecl.model.GetLabel;
import org.eclipse.rcptt.tesla.ecl.model.GetLastMessageBox;
import org.eclipse.rcptt.tesla.ecl.model.GetLeftRuler;
import org.eclipse.rcptt.tesla.ecl.model.GetLink;
import org.eclipse.rcptt.tesla.ecl.model.GetList;
import org.eclipse.rcptt.tesla.ecl.model.GetMenu;
import org.eclipse.rcptt.tesla.ecl.model.GetObject;
import org.eclipse.rcptt.tesla.ecl.model.GetPreferencesMenu;
import org.eclipse.rcptt.tesla.ecl.model.GetProperty;
import org.eclipse.rcptt.tesla.ecl.model.GetPropertyNodes;
import org.eclipse.rcptt.tesla.ecl.model.GetPropertyTab;
import org.eclipse.rcptt.tesla.ecl.model.GetQuickAccess;
import org.eclipse.rcptt.tesla.ecl.model.GetRegion;
import org.eclipse.rcptt.tesla.ecl.model.GetRegionText;
import org.eclipse.rcptt.tesla.ecl.model.GetRightRuler;
import org.eclipse.rcptt.tesla.ecl.model.GetRulerColumn;
import org.eclipse.rcptt.tesla.ecl.model.GetRuntimeTarget;
import org.eclipse.rcptt.tesla.ecl.model.GetSection;
import org.eclipse.rcptt.tesla.ecl.model.GetSlider;
import org.eclipse.rcptt.tesla.ecl.model.GetTabFolder;
import org.eclipse.rcptt.tesla.ecl.model.GetTabItem;
import org.eclipse.rcptt.tesla.ecl.model.GetTable;
import org.eclipse.rcptt.tesla.ecl.model.GetTableData;
import org.eclipse.rcptt.tesla.ecl.model.GetTestCaseName;
import org.eclipse.rcptt.tesla.ecl.model.GetText;
import org.eclipse.rcptt.tesla.ecl.model.GetTextViewer;
import org.eclipse.rcptt.tesla.ecl.model.GetToolbar;
import org.eclipse.rcptt.tesla.ecl.model.GetTree;
import org.eclipse.rcptt.tesla.ecl.model.GetVerticalRuler;
import org.eclipse.rcptt.tesla.ecl.model.GetView;
import org.eclipse.rcptt.tesla.ecl.model.GetWidgetDetails;
import org.eclipse.rcptt.tesla.ecl.model.GetWindow;
import org.eclipse.rcptt.tesla.ecl.model.HoverAtTextOffset;
import org.eclipse.rcptt.tesla.ecl.model.HoverRuler;
import org.eclipse.rcptt.tesla.ecl.model.HoverText;
import org.eclipse.rcptt.tesla.ecl.model.IsDisabled;
import org.eclipse.rcptt.tesla.ecl.model.IsDisposed;
import org.eclipse.rcptt.tesla.ecl.model.IsEmpty;
import org.eclipse.rcptt.tesla.ecl.model.KeyType;
import org.eclipse.rcptt.tesla.ecl.model.Matches;
import org.eclipse.rcptt.tesla.ecl.model.Maximize;
import org.eclipse.rcptt.tesla.ecl.model.MessageBoxInfo;
import org.eclipse.rcptt.tesla.ecl.model.Minimize;
import org.eclipse.rcptt.tesla.ecl.model.Mouse;
import org.eclipse.rcptt.tesla.ecl.model.OpenDeclaration;
import org.eclipse.rcptt.tesla.ecl.model.Options;
import org.eclipse.rcptt.tesla.ecl.model.PathSelector;
import org.eclipse.rcptt.tesla.ecl.model.Recognize;
import org.eclipse.rcptt.tesla.ecl.model.RecognizeResponse;
import org.eclipse.rcptt.tesla.ecl.model.RestartAut;
import org.eclipse.rcptt.tesla.ecl.model.Restore;
import org.eclipse.rcptt.tesla.ecl.model.ReturnFromOsDialog;
import org.eclipse.rcptt.tesla.ecl.model.Select;
import org.eclipse.rcptt.tesla.ecl.model.SelectItem;
import org.eclipse.rcptt.tesla.ecl.model.SelectRange;
import org.eclipse.rcptt.tesla.ecl.model.Selector;
import org.eclipse.rcptt.tesla.ecl.model.SetCaretPos;
import org.eclipse.rcptt.tesla.ecl.model.SetDialogResult;
import org.eclipse.rcptt.tesla.ecl.model.SetFocus;
import org.eclipse.rcptt.tesla.ecl.model.SetPosition;
import org.eclipse.rcptt.tesla.ecl.model.SetText;
import org.eclipse.rcptt.tesla.ecl.model.SetTextOffset;
import org.eclipse.rcptt.tesla.ecl.model.SetTextSelection;
import org.eclipse.rcptt.tesla.ecl.model.SetValue;
import org.eclipse.rcptt.tesla.ecl.model.SetWidth;
import org.eclipse.rcptt.tesla.ecl.model.SetupPlayer;
import org.eclipse.rcptt.tesla.ecl.model.ShoutdownPlayer;
import org.eclipse.rcptt.tesla.ecl.model.ShowAlert;
import org.eclipse.rcptt.tesla.ecl.model.ShowContentAssist;
import org.eclipse.rcptt.tesla.ecl.model.ShowTabList;
import org.eclipse.rcptt.tesla.ecl.model.ShutdownAut;
import org.eclipse.rcptt.tesla.ecl.model.TakeScreenshot;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;
import org.eclipse.rcptt.tesla.ecl.model.TeslaProcessStatus;
import org.eclipse.rcptt.tesla.ecl.model.TextSelector;
import org.eclipse.rcptt.tesla.ecl.model.ToControlHandle;
import org.eclipse.rcptt.tesla.ecl.model.Trace;
import org.eclipse.rcptt.tesla.ecl.model.TypeCommandKey;
import org.eclipse.rcptt.tesla.ecl.model.TypeText;
import org.eclipse.rcptt.tesla.ecl.model.Uncheck;
import org.eclipse.rcptt.tesla.ecl.model.Unfocus;
import org.eclipse.rcptt.tesla.ecl.model.Unsupported;
import org.eclipse.rcptt.tesla.ecl.model.VerifyFalse;
import org.eclipse.rcptt.tesla.ecl.model.VerifyHandler;
import org.eclipse.rcptt.tesla.ecl.model.VerifyTrue;
import org.eclipse.rcptt.tesla.ecl.model.Wait;
import org.eclipse.rcptt.tesla.ecl.model.WaitUntilEclipseIsReady;
import org.eclipse.rcptt.tesla.ecl.model.Wrapper;
import org.eclipse.rcptt.tesla.ecl.model.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage
 * @generated
 */
public class TeslaSwitch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static TeslaPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TeslaSwitch() {
		if (modelPackage == null) {
			modelPackage = TeslaPackage.eINSTANCE;
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	public T doSwitch(EObject theEObject) {
		return doSwitch(theEObject.eClass(), theEObject);
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(EClass theEClass, EObject theEObject) {
		if (theEClass.eContainer() == modelPackage) {
			return doSwitch(theEClass.getClassifierID(), theEObject);
		}
		else {
			List<EClass> eSuperTypes = theEClass.getESuperTypes();
			return
				eSuperTypes.isEmpty() ?
					defaultCase(theEObject) :
					doSwitch(eSuperTypes.get(0), theEObject);
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case TeslaPackage.WAIT: {
				Wait wait = (Wait)theEObject;
				T result = caseWait(wait);
				if (result == null) result = caseCommand(wait);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SETUP_PLAYER: {
				SetupPlayer setupPlayer = (SetupPlayer)theEObject;
				T result = caseSetupPlayer(setupPlayer);
				if (result == null) result = caseCommand(setupPlayer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SHOUTDOWN_PLAYER: {
				ShoutdownPlayer shoutdownPlayer = (ShoutdownPlayer)theEObject;
				T result = caseShoutdownPlayer(shoutdownPlayer);
				if (result == null) result = caseCommand(shoutdownPlayer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.TESLA_PROCESS_STATUS: {
				TeslaProcessStatus teslaProcessStatus = (TeslaProcessStatus)theEObject;
				T result = caseTeslaProcessStatus(teslaProcessStatus);
				if (result == null) result = caseProcessStatus(teslaProcessStatus);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.WRAPPER: {
				Wrapper wrapper = (Wrapper)theEObject;
				T result = caseWrapper(wrapper);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_PROPERTY: {
				GetProperty getProperty = (GetProperty)theEObject;
				T result = caseGetProperty(getProperty);
				if (result == null) result = caseCommand(getProperty);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.VERIFY_TRUE: {
				VerifyTrue verifyTrue = (VerifyTrue)theEObject;
				T result = caseVerifyTrue(verifyTrue);
				if (result == null) result = caseCommand(verifyTrue);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.VERIFY_FALSE: {
				VerifyFalse verifyFalse = (VerifyFalse)theEObject;
				T result = caseVerifyFalse(verifyFalse);
				if (result == null) result = caseCommand(verifyFalse);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.VERIFY_HANDLER: {
				VerifyHandler verifyHandler = (VerifyHandler)theEObject;
				T result = caseVerifyHandler(verifyHandler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.VERIFY_ERROR: {
				org.eclipse.rcptt.tesla.ecl.model.VerifyError verifyError = (org.eclipse.rcptt.tesla.ecl.model.VerifyError)theEObject;
				T result = caseVerifyError(verifyError);
				if (result == null) result = caseCommand(verifyError);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.EQUALS: {
				Equals equals = (Equals)theEObject;
				T result = caseEquals(equals);
				if (result == null) result = caseCommand(equals);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CONTAINS: {
				Contains contains = (Contains)theEObject;
				T result = caseContains(contains);
				if (result == null) result = caseCommand(contains);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.MATCHES: {
				Matches matches = (Matches)theEObject;
				T result = caseMatches(matches);
				if (result == null) result = caseCommand(matches);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.IS_EMPTY: {
				IsEmpty isEmpty = (IsEmpty)theEObject;
				T result = caseIsEmpty(isEmpty);
				if (result == null) result = caseCommand(isEmpty);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.UNSUPPORTED: {
				Unsupported unsupported = (Unsupported)theEObject;
				T result = caseUnsupported(unsupported);
				if (result == null) result = caseCommand(unsupported);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CONTROL_NOT_FOUND: {
				ControlNotFound controlNotFound = (ControlNotFound)theEObject;
				T result = caseControlNotFound(controlNotFound);
				if (result == null) result = caseCommand(controlNotFound);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CONTROL_HANDLER: {
				ControlHandler controlHandler = (ControlHandler)theEObject;
				T result = caseControlHandler(controlHandler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_ECLIPSE_WINDOW: {
				GetEclipseWindow getEclipseWindow = (GetEclipseWindow)theEObject;
				T result = caseGetEclipseWindow(getEclipseWindow);
				if (result == null) result = caseSelector(getEclipseWindow);
				if (result == null) result = caseCommand(getEclipseWindow);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SELECTOR: {
				Selector selector = (Selector)theEObject;
				T result = caseSelector(selector);
				if (result == null) result = caseCommand(selector);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.PATH_SELECTOR: {
				PathSelector pathSelector = (PathSelector)theEObject;
				T result = casePathSelector(pathSelector);
				if (result == null) result = caseSelector(pathSelector);
				if (result == null) result = caseCommand(pathSelector);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.TEXT_SELECTOR: {
				TextSelector textSelector = (TextSelector)theEObject;
				T result = caseTextSelector(textSelector);
				if (result == null) result = caseSelector(textSelector);
				if (result == null) result = caseCommand(textSelector);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_CONTROL: {
				GetControl getControl = (GetControl)theEObject;
				T result = caseGetControl(getControl);
				if (result == null) result = caseTextSelector(getControl);
				if (result == null) result = casePathSelector(getControl);
				if (result == null) result = caseSelector(getControl);
				if (result == null) result = caseCommand(getControl);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_BUTTON: {
				GetButton getButton = (GetButton)theEObject;
				T result = caseGetButton(getButton);
				if (result == null) result = caseTextSelector(getButton);
				if (result == null) result = caseSelector(getButton);
				if (result == null) result = caseCommand(getButton);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_CANVAS: {
				GetCanvas getCanvas = (GetCanvas)theEObject;
				T result = caseGetCanvas(getCanvas);
				if (result == null) result = caseSelector(getCanvas);
				if (result == null) result = caseCommand(getCanvas);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_CHECKBOX: {
				GetCheckbox getCheckbox = (GetCheckbox)theEObject;
				T result = caseGetCheckbox(getCheckbox);
				if (result == null) result = caseTextSelector(getCheckbox);
				if (result == null) result = caseSelector(getCheckbox);
				if (result == null) result = caseCommand(getCheckbox);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_COMBO: {
				GetCombo getCombo = (GetCombo)theEObject;
				T result = caseGetCombo(getCombo);
				if (result == null) result = caseSelector(getCombo);
				if (result == null) result = caseCommand(getCombo);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_EDITBOX: {
				GetEditbox getEditbox = (GetEditbox)theEObject;
				T result = caseGetEditbox(getEditbox);
				if (result == null) result = caseSelector(getEditbox);
				if (result == null) result = caseCommand(getEditbox);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_GROUP: {
				GetGroup getGroup = (GetGroup)theEObject;
				T result = caseGetGroup(getGroup);
				if (result == null) result = caseTextSelector(getGroup);
				if (result == null) result = caseSelector(getGroup);
				if (result == null) result = caseCommand(getGroup);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_ITEM: {
				GetItem getItem = (GetItem)theEObject;
				T result = caseGetItem(getItem);
				if (result == null) result = caseSelector(getItem);
				if (result == null) result = caseCommand(getItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_LABEL: {
				GetLabel getLabel = (GetLabel)theEObject;
				T result = caseGetLabel(getLabel);
				if (result == null) result = caseTextSelector(getLabel);
				if (result == null) result = caseSelector(getLabel);
				if (result == null) result = caseCommand(getLabel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_LINK: {
				GetLink getLink = (GetLink)theEObject;
				T result = caseGetLink(getLink);
				if (result == null) result = caseTextSelector(getLink);
				if (result == null) result = caseSelector(getLink);
				if (result == null) result = caseCommand(getLink);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_LIST: {
				GetList getList = (GetList)theEObject;
				T result = caseGetList(getList);
				if (result == null) result = caseSelector(getList);
				if (result == null) result = caseCommand(getList);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_MENU: {
				GetMenu getMenu = (GetMenu)theEObject;
				T result = caseGetMenu(getMenu);
				if (result == null) result = casePathSelector(getMenu);
				if (result == null) result = caseSelector(getMenu);
				if (result == null) result = caseCommand(getMenu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TAB_FOLDER: {
				GetTabFolder getTabFolder = (GetTabFolder)theEObject;
				T result = caseGetTabFolder(getTabFolder);
				if (result == null) result = caseSelector(getTabFolder);
				if (result == null) result = caseCommand(getTabFolder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TAB_ITEM: {
				GetTabItem getTabItem = (GetTabItem)theEObject;
				T result = caseGetTabItem(getTabItem);
				if (result == null) result = caseTextSelector(getTabItem);
				if (result == null) result = caseSelector(getTabItem);
				if (result == null) result = caseCommand(getTabItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TABLE: {
				GetTable getTable = (GetTable)theEObject;
				T result = caseGetTable(getTable);
				if (result == null) result = caseSelector(getTable);
				if (result == null) result = caseCommand(getTable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TOOLBAR: {
				GetToolbar getToolbar = (GetToolbar)theEObject;
				T result = caseGetToolbar(getToolbar);
				if (result == null) result = caseSelector(getToolbar);
				if (result == null) result = caseCommand(getToolbar);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TREE: {
				GetTree getTree = (GetTree)theEObject;
				T result = caseGetTree(getTree);
				if (result == null) result = caseSelector(getTree);
				if (result == null) result = caseCommand(getTree);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_WINDOW: {
				GetWindow getWindow = (GetWindow)theEObject;
				T result = caseGetWindow(getWindow);
				if (result == null) result = caseSelector(getWindow);
				if (result == null) result = caseCommand(getWindow);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_VIEW: {
				GetView getView = (GetView)theEObject;
				T result = caseGetView(getView);
				if (result == null) result = caseTextSelector(getView);
				if (result == null) result = caseSelector(getView);
				if (result == null) result = caseCommand(getView);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_EDITOR: {
				GetEditor getEditor = (GetEditor)theEObject;
				T result = caseGetEditor(getEditor);
				if (result == null) result = caseTextSelector(getEditor);
				if (result == null) result = caseSelector(getEditor);
				if (result == null) result = caseCommand(getEditor);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_SECTION: {
				GetSection getSection = (GetSection)theEObject;
				T result = caseGetSection(getSection);
				if (result == null) result = caseTextSelector(getSection);
				if (result == null) result = caseSelector(getSection);
				if (result == null) result = caseCommand(getSection);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_BANNER: {
				GetBanner getBanner = (GetBanner)theEObject;
				T result = caseGetBanner(getBanner);
				if (result == null) result = caseSelector(getBanner);
				if (result == null) result = caseCommand(getBanner);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_COOLBAR: {
				GetCoolbar getCoolbar = (GetCoolbar)theEObject;
				T result = caseGetCoolbar(getCoolbar);
				if (result == null) result = caseSelector(getCoolbar);
				if (result == null) result = caseCommand(getCoolbar);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_COLOR_SELECTOR: {
				GetColorSelector getColorSelector = (GetColorSelector)theEObject;
				T result = caseGetColorSelector(getColorSelector);
				if (result == null) result = caseSelector(getColorSelector);
				if (result == null) result = caseCommand(getColorSelector);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_CELL: {
				GetCell getCell = (GetCell)theEObject;
				T result = caseGetCell(getCell);
				if (result == null) result = caseSelector(getCell);
				if (result == null) result = caseCommand(getCell);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CLICK: {
				Click click = (Click)theEObject;
				T result = caseClick(click);
				if (result == null) result = caseCommand(click);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DOUBLE_CLICK: {
				DoubleClick doubleClick = (DoubleClick)theEObject;
				T result = caseDoubleClick(doubleClick);
				if (result == null) result = caseClick(doubleClick);
				if (result == null) result = caseCommand(doubleClick);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TEXT: {
				GetText getText = (GetText)theEObject;
				T result = caseGetText(getText);
				if (result == null) result = caseCommand(getText);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.IS_DISABLED: {
				IsDisabled isDisabled = (IsDisabled)theEObject;
				T result = caseIsDisabled(isDisabled);
				if (result == null) result = caseCommand(isDisabled);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.IS_DISPOSED: {
				IsDisposed isDisposed = (IsDisposed)theEObject;
				T result = caseIsDisposed(isDisposed);
				if (result == null) result = caseCommand(isDisposed);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.TYPE_TEXT: {
				TypeText typeText = (TypeText)theEObject;
				T result = caseTypeText(typeText);
				if (result == null) result = caseCommand(typeText);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.KEY_TYPE: {
				KeyType keyType = (KeyType)theEObject;
				T result = caseKeyType(keyType);
				if (result == null) result = caseCommand(keyType);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.TYPE_COMMAND_KEY: {
				TypeCommandKey typeCommandKey = (TypeCommandKey)theEObject;
				T result = caseTypeCommandKey(typeCommandKey);
				if (result == null) result = caseCommand(typeCommandKey);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_TEXT: {
				SetText setText = (SetText)theEObject;
				T result = caseSetText(setText);
				if (result == null) result = caseCommand(setText);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_TEXT_SELECTION: {
				SetTextSelection setTextSelection = (SetTextSelection)theEObject;
				T result = caseSetTextSelection(setTextSelection);
				if (result == null) result = caseCommand(setTextSelection);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_TEXT_OFFSET: {
				SetTextOffset setTextOffset = (SetTextOffset)theEObject;
				T result = caseSetTextOffset(setTextOffset);
				if (result == null) result = caseCommand(setTextOffset);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CHECK: {
				Check check = (Check)theEObject;
				T result = caseCheck(check);
				if (result == null) result = caseCommand(check);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.UNCHECK: {
				Uncheck uncheck = (Uncheck)theEObject;
				T result = caseUncheck(uncheck);
				if (result == null) result = caseCommand(uncheck);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SELECT: {
				Select select = (Select)theEObject;
				T result = caseSelect(select);
				if (result == null) result = caseCommand(select);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CELL_EDIT: {
				CellEdit cellEdit = (CellEdit)theEObject;
				T result = caseCellEdit(cellEdit);
				if (result == null) result = caseCommand(cellEdit);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.ACTIVATE_CELL_EDIT: {
				ActivateCellEdit activateCellEdit = (ActivateCellEdit)theEObject;
				T result = caseActivateCellEdit(activateCellEdit);
				if (result == null) result = caseCellEdit(activateCellEdit);
				if (result == null) result = caseCommand(activateCellEdit);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.APPLY_CELL_EDIT: {
				ApplyCellEdit applyCellEdit = (ApplyCellEdit)theEObject;
				T result = caseApplyCellEdit(applyCellEdit);
				if (result == null) result = caseCellEdit(applyCellEdit);
				if (result == null) result = caseCommand(applyCellEdit);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CANCEL_CELL_EDIT: {
				CancelCellEdit cancelCellEdit = (CancelCellEdit)theEObject;
				T result = caseCancelCellEdit(cancelCellEdit);
				if (result == null) result = caseCellEdit(cancelCellEdit);
				if (result == null) result = caseCommand(cancelCellEdit);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DEACTIVATE_CELL_EDIT: {
				DeactivateCellEdit deactivateCellEdit = (DeactivateCellEdit)theEObject;
				T result = caseDeactivateCellEdit(deactivateCellEdit);
				if (result == null) result = caseCellEdit(deactivateCellEdit);
				if (result == null) result = caseCommand(deactivateCellEdit);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CLOSE: {
				Close close = (Close)theEObject;
				T result = caseClose(close);
				if (result == null) result = caseCommand(close);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.RETURN_FROM_OS_DIALOG: {
				ReturnFromOsDialog returnFromOsDialog = (ReturnFromOsDialog)theEObject;
				T result = caseReturnFromOsDialog(returnFromOsDialog);
				if (result == null) result = caseCommand(returnFromOsDialog);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.WAIT_UNTIL_ECLIPSE_IS_READY: {
				WaitUntilEclipseIsReady waitUntilEclipseIsReady = (WaitUntilEclipseIsReady)theEObject;
				T result = caseWaitUntilEclipseIsReady(waitUntilEclipseIsReady);
				if (result == null) result = caseCommand(waitUntilEclipseIsReady);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SHOW_CONTENT_ASSIST: {
				ShowContentAssist showContentAssist = (ShowContentAssist)theEObject;
				T result = caseShowContentAssist(showContentAssist);
				if (result == null) result = caseCommand(showContentAssist);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_ACTION: {
				DragAction dragAction = (DragAction)theEObject;
				T result = caseDragAction(dragAction);
				if (result == null) result = caseCommand(dragAction);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_START: {
				DragStart dragStart = (DragStart)theEObject;
				T result = caseDragStart(dragStart);
				if (result == null) result = caseDragAction(dragStart);
				if (result == null) result = caseCommand(dragStart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_END: {
				DragEnd dragEnd = (DragEnd)theEObject;
				T result = caseDragEnd(dragEnd);
				if (result == null) result = caseDragAction(dragEnd);
				if (result == null) result = caseCommand(dragEnd);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_ENTER: {
				DragEnter dragEnter = (DragEnter)theEObject;
				T result = caseDragEnter(dragEnter);
				if (result == null) result = caseDragAction(dragEnter);
				if (result == null) result = caseCommand(dragEnter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_EXIT: {
				DragExit dragExit = (DragExit)theEObject;
				T result = caseDragExit(dragExit);
				if (result == null) result = caseDragAction(dragExit);
				if (result == null) result = caseCommand(dragExit);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_SET_DATA: {
				DragSetData dragSetData = (DragSetData)theEObject;
				T result = caseDragSetData(dragSetData);
				if (result == null) result = caseDragAction(dragSetData);
				if (result == null) result = caseCommand(dragSetData);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_ACCEPT: {
				DragAccept dragAccept = (DragAccept)theEObject;
				T result = caseDragAccept(dragAccept);
				if (result == null) result = caseDragAction(dragAccept);
				if (result == null) result = caseCommand(dragAccept);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_DETECT: {
				DragDetect dragDetect = (DragDetect)theEObject;
				T result = caseDragDetect(dragDetect);
				if (result == null) result = caseDragAction(dragDetect);
				if (result == null) result = caseCommand(dragDetect);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DRAG_OVER: {
				DragOver dragOver = (DragOver)theEObject;
				T result = caseDragOver(dragOver);
				if (result == null) result = caseDragAction(dragOver);
				if (result == null) result = caseCommand(dragOver);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DROP: {
				Drop drop = (Drop)theEObject;
				T result = caseDrop(drop);
				if (result == null) result = caseDragAction(drop);
				if (result == null) result = caseCommand(drop);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.RECOGNIZE: {
				Recognize recognize = (Recognize)theEObject;
				T result = caseRecognize(recognize);
				if (result == null) result = caseCommand(recognize);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.RECOGNIZE_RESPONSE: {
				RecognizeResponse recognizeResponse = (RecognizeResponse)theEObject;
				T result = caseRecognizeResponse(recognizeResponse);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CONTAINS_IMAGE: {
				ContainsImage containsImage = (ContainsImage)theEObject;
				T result = caseContainsImage(containsImage);
				if (result == null) result = caseCommand(containsImage);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_REGION_TEXT: {
				GetRegionText getRegionText = (GetRegionText)theEObject;
				T result = caseGetRegionText(getRegionText);
				if (result == null) result = caseCommand(getRegionText);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_REGION: {
				GetRegion getRegion = (GetRegion)theEObject;
				T result = caseGetRegion(getRegion);
				if (result == null) result = caseSelector(getRegion);
				if (result == null) result = caseCommand(getRegion);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_ADVANCED_INFO: {
				GetAdvancedInfo getAdvancedInfo = (GetAdvancedInfo)theEObject;
				T result = caseGetAdvancedInfo(getAdvancedInfo);
				if (result == null) result = caseCommand(getAdvancedInfo);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_ABOUT_MENU: {
				GetAboutMenu getAboutMenu = (GetAboutMenu)theEObject;
				T result = caseGetAboutMenu(getAboutMenu);
				if (result == null) result = caseCommand(getAboutMenu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_PREFERENCES_MENU: {
				GetPreferencesMenu getPreferencesMenu = (GetPreferencesMenu)theEObject;
				T result = caseGetPreferencesMenu(getPreferencesMenu);
				if (result == null) result = caseCommand(getPreferencesMenu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_DATE_TIME: {
				GetDateTime getDateTime = (GetDateTime)theEObject;
				T result = caseGetDateTime(getDateTime);
				if (result == null) result = caseTextSelector(getDateTime);
				if (result == null) result = caseSelector(getDateTime);
				if (result == null) result = caseCommand(getDateTime);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_SLIDER: {
				GetSlider getSlider = (GetSlider)theEObject;
				T result = caseGetSlider(getSlider);
				if (result == null) result = caseTextSelector(getSlider);
				if (result == null) result = caseSelector(getSlider);
				if (result == null) result = caseCommand(getSlider);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_VALUE: {
				SetValue setValue = (SetValue)theEObject;
				T result = caseSetValue(setValue);
				if (result == null) result = caseCommand(setValue);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.MINIMIZE: {
				Minimize minimize = (Minimize)theEObject;
				T result = caseMinimize(minimize);
				if (result == null) result = caseCommand(minimize);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.MAXIMIZE: {
				Maximize maximize = (Maximize)theEObject;
				T result = caseMaximize(maximize);
				if (result == null) result = caseCommand(maximize);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.RESTORE: {
				Restore restore = (Restore)theEObject;
				T result = caseRestore(restore);
				if (result == null) result = caseCommand(restore);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SHOW_TAB_LIST: {
				ShowTabList showTabList = (ShowTabList)theEObject;
				T result = caseShowTabList(showTabList);
				if (result == null) result = caseCommand(showTabList);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.OPTIONS: {
				Options options = (Options)theEObject;
				T result = caseOptions(options);
				if (result == null) result = caseCommand(options);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.EXEC_WITH_OPTIONS: {
				ExecWithOptions execWithOptions = (ExecWithOptions)theEObject;
				T result = caseExecWithOptions(execWithOptions);
				if (result == null) result = caseCommand(execWithOptions);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SHUTDOWN_AUT: {
				ShutdownAut shutdownAut = (ShutdownAut)theEObject;
				T result = caseShutdownAut(shutdownAut);
				if (result == null) result = caseCommand(shutdownAut);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_DIALOG_RESULT: {
				SetDialogResult setDialogResult = (SetDialogResult)theEObject;
				T result = caseSetDialogResult(setDialogResult);
				if (result == null) result = caseCommand(setDialogResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_PROPERTY_NODES: {
				GetPropertyNodes getPropertyNodes = (GetPropertyNodes)theEObject;
				T result = caseGetPropertyNodes(getPropertyNodes);
				if (result == null) result = caseCommand(getPropertyNodes);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_BY_OS: {
				GetByOs getByOs = (GetByOs)theEObject;
				T result = caseGetByOs(getByOs);
				if (result == null) result = caseCommand(getByOs);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.FROM_RAW_KEY: {
				FromRawKey fromRawKey = (FromRawKey)theEObject;
				T result = caseFromRawKey(fromRawKey);
				if (result == null) result = caseCommand(fromRawKey);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.HOVER_AT_TEXT_OFFSET: {
				HoverAtTextOffset hoverAtTextOffset = (HoverAtTextOffset)theEObject;
				T result = caseHoverAtTextOffset(hoverAtTextOffset);
				if (result == null) result = caseCommand(hoverAtTextOffset);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TEXT_VIEWER: {
				GetTextViewer getTextViewer = (GetTextViewer)theEObject;
				T result = caseGetTextViewer(getTextViewer);
				if (result == null) result = caseSelector(getTextViewer);
				if (result == null) result = caseCommand(getTextViewer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SELECT_RANGE: {
				SelectRange selectRange = (SelectRange)theEObject;
				T result = caseSelectRange(selectRange);
				if (result == null) result = caseCommand(selectRange);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_CARET_POS: {
				SetCaretPos setCaretPos = (SetCaretPos)theEObject;
				T result = caseSetCaretPos(setCaretPos);
				if (result == null) result = caseCommand(setCaretPos);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.HOVER_TEXT: {
				HoverText hoverText = (HoverText)theEObject;
				T result = caseHoverText(hoverText);
				if (result == null) result = caseCommand(hoverText);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.OPEN_DECLARATION: {
				OpenDeclaration openDeclaration = (OpenDeclaration)theEObject;
				T result = caseOpenDeclaration(openDeclaration);
				if (result == null) result = caseCommand(openDeclaration);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_VERTICAL_RULER: {
				GetVerticalRuler getVerticalRuler = (GetVerticalRuler)theEObject;
				T result = caseGetVerticalRuler(getVerticalRuler);
				if (result == null) result = caseSelector(getVerticalRuler);
				if (result == null) result = caseCommand(getVerticalRuler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_LEFT_RULER: {
				GetLeftRuler getLeftRuler = (GetLeftRuler)theEObject;
				T result = caseGetLeftRuler(getLeftRuler);
				if (result == null) result = caseSelector(getLeftRuler);
				if (result == null) result = caseCommand(getLeftRuler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_RULER_COLUMN: {
				GetRulerColumn getRulerColumn = (GetRulerColumn)theEObject;
				T result = caseGetRulerColumn(getRulerColumn);
				if (result == null) result = caseSelector(getRulerColumn);
				if (result == null) result = caseCommand(getRulerColumn);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_RIGHT_RULER: {
				GetRightRuler getRightRuler = (GetRightRuler)theEObject;
				T result = caseGetRightRuler(getRightRuler);
				if (result == null) result = caseSelector(getRightRuler);
				if (result == null) result = caseCommand(getRightRuler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CLICK_RULER: {
				ClickRuler clickRuler = (ClickRuler)theEObject;
				T result = caseClickRuler(clickRuler);
				if (result == null) result = caseCommand(clickRuler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DOUBLE_CLICK_RULER: {
				DoubleClickRuler doubleClickRuler = (DoubleClickRuler)theEObject;
				T result = caseDoubleClickRuler(doubleClickRuler);
				if (result == null) result = caseCommand(doubleClickRuler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.HOVER_RULER: {
				HoverRuler hoverRuler = (HoverRuler)theEObject;
				T result = caseHoverRuler(hoverRuler);
				if (result == null) result = caseCommand(hoverRuler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CONTROL_COMMAND: {
				ControlCommand controlCommand = (ControlCommand)theEObject;
				T result = caseControlCommand(controlCommand);
				if (result == null) result = caseCommand(controlCommand);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CLICK_LINK: {
				ClickLink clickLink = (ClickLink)theEObject;
				T result = caseClickLink(clickLink);
				if (result == null) result = caseControlCommand(clickLink);
				if (result == null) result = caseCommand(clickLink);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.EXPAND: {
				Expand expand = (Expand)theEObject;
				T result = caseExpand(expand);
				if (result == null) result = caseControlCommand(expand);
				if (result == null) result = caseCommand(expand);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.COLLAPSE: {
				Collapse collapse = (Collapse)theEObject;
				T result = caseCollapse(collapse);
				if (result == null) result = caseControlCommand(collapse);
				if (result == null) result = caseCommand(collapse);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_FOCUS: {
				SetFocus setFocus = (SetFocus)theEObject;
				T result = caseSetFocus(setFocus);
				if (result == null) result = caseControlCommand(setFocus);
				if (result == null) result = caseCommand(setFocus);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TABLE_DATA: {
				GetTableData getTableData = (GetTableData)theEObject;
				T result = caseGetTableData(getTableData);
				if (result == null) result = caseControlCommand(getTableData);
				if (result == null) result = caseCommand(getTableData);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CLICK_COLUMN: {
				ClickColumn clickColumn = (ClickColumn)theEObject;
				T result = caseClickColumn(clickColumn);
				if (result == null) result = caseControlCommand(clickColumn);
				if (result == null) result = caseCommand(clickColumn);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.TRACE: {
				Trace trace = (Trace)theEObject;
				T result = caseTrace(trace);
				if (result == null) result = caseCommand(trace);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.MOUSE: {
				Mouse mouse = (Mouse)theEObject;
				T result = caseMouse(mouse);
				if (result == null) result = caseControlCommand(mouse);
				if (result == null) result = caseCommand(mouse);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_OBJECT: {
				GetObject getObject = (GetObject)theEObject;
				T result = caseGetObject(getObject);
				if (result == null) result = caseCommand(getObject);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_ITEMS: {
				GetItems getItems = (GetItems)theEObject;
				T result = caseGetItems(getItems);
				if (result == null) result = caseControlCommand(getItems);
				if (result == null) result = caseCommand(getItems);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.EXPAND_ALL: {
				ExpandAll expandAll = (ExpandAll)theEObject;
				T result = caseExpandAll(expandAll);
				if (result == null) result = caseControlCommand(expandAll);
				if (result == null) result = caseCommand(expandAll);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.COLLAPSE_ALL: {
				CollapseAll collapseAll = (CollapseAll)theEObject;
				T result = caseCollapseAll(collapseAll);
				if (result == null) result = caseControlCommand(collapseAll);
				if (result == null) result = caseCommand(collapseAll);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.TAKE_SCREENSHOT: {
				TakeScreenshot takeScreenshot = (TakeScreenshot)theEObject;
				T result = caseTakeScreenshot(takeScreenshot);
				if (result == null) result = caseCommand(takeScreenshot);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SELECT_ITEM: {
				SelectItem selectItem = (SelectItem)theEObject;
				T result = caseSelectItem(selectItem);
				if (result == null) result = caseControlCommand(selectItem);
				if (result == null) result = caseCommand(selectItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_WIDGET_DETAILS: {
				GetWidgetDetails getWidgetDetails = (GetWidgetDetails)theEObject;
				T result = caseGetWidgetDetails(getWidgetDetails);
				if (result == null) result = caseCommand(getWidgetDetails);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CLICK_TEXT: {
				ClickText clickText = (ClickText)theEObject;
				T result = caseClickText(clickText);
				if (result == null) result = caseControlCommand(clickText);
				if (result == null) result = caseCommand(clickText);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_QUICK_ACCESS: {
				GetQuickAccess getQuickAccess = (GetQuickAccess)theEObject;
				T result = caseGetQuickAccess(getQuickAccess);
				if (result == null) result = caseSelector(getQuickAccess);
				if (result == null) result = caseCommand(getQuickAccess);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_COLUMN_HEADER: {
				GetColumnHeader getColumnHeader = (GetColumnHeader)theEObject;
				T result = caseGetColumnHeader(getColumnHeader);
				if (result == null) result = caseTextSelector(getColumnHeader);
				if (result == null) result = caseSelector(getColumnHeader);
				if (result == null) result = caseCommand(getColumnHeader);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_POSITION: {
				SetPosition setPosition = (SetPosition)theEObject;
				T result = caseSetPosition(setPosition);
				if (result == null) result = caseControlCommand(setPosition);
				if (result == null) result = caseCommand(setPosition);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SET_WIDTH: {
				SetWidth setWidth = (SetWidth)theEObject;
				T result = caseSetWidth(setWidth);
				if (result == null) result = caseControlCommand(setWidth);
				if (result == null) result = caseCommand(setWidth);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_PROPERTY_TAB: {
				GetPropertyTab getPropertyTab = (GetPropertyTab)theEObject;
				T result = caseGetPropertyTab(getPropertyTab);
				if (result == null) result = caseTextSelector(getPropertyTab);
				if (result == null) result = caseSelector(getPropertyTab);
				if (result == null) result = caseCommand(getPropertyTab);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.SHOW_ALERT: {
				ShowAlert showAlert = (ShowAlert)theEObject;
				T result = caseShowAlert(showAlert);
				if (result == null) result = caseCommand(showAlert);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DOUBLE_CLICK_TEXT: {
				DoubleClickText doubleClickText = (DoubleClickText)theEObject;
				T result = caseDoubleClickText(doubleClickText);
				if (result == null) result = caseControlCommand(doubleClickText);
				if (result == null) result = caseCommand(doubleClickText);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.TO_CONTROL_HANDLE: {
				ToControlHandle toControlHandle = (ToControlHandle)theEObject;
				T result = caseToControlHandle(toControlHandle);
				if (result == null) result = caseCommand(toControlHandle);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.BOUND_CONTROL_HANDLE: {
				BoundControlHandle boundControlHandle = (BoundControlHandle)theEObject;
				T result = caseBoundControlHandle(boundControlHandle);
				if (result == null) result = caseControlHandler(boundControlHandle);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.UNFOCUS: {
				Unfocus unfocus = (Unfocus)theEObject;
				T result = caseUnfocus(unfocus);
				if (result == null) result = caseControlCommand(unfocus);
				if (result == null) result = caseCommand(unfocus);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DECRYPT: {
				Decrypt decrypt = (Decrypt)theEObject;
				T result = caseDecrypt(decrypt);
				if (result == null) result = caseCommand(decrypt);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.DECRYPT_RESULT: {
				DecryptResult decryptResult = (DecryptResult)theEObject;
				T result = caseDecryptResult(decryptResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.RESTART_AUT: {
				RestartAut restartAut = (RestartAut)theEObject;
				T result = caseRestartAut(restartAut);
				if (result == null) result = caseCommand(restartAut);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.MESSAGE_BOX_INFO: {
				MessageBoxInfo messageBoxInfo = (MessageBoxInfo)theEObject;
				T result = caseMessageBoxInfo(messageBoxInfo);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_LAST_MESSAGE_BOX: {
				GetLastMessageBox getLastMessageBox = (GetLastMessageBox)theEObject;
				T result = caseGetLastMessageBox(getLastMessageBox);
				if (result == null) result = caseCommand(getLastMessageBox);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.CHECK_DOWNLOAD_RESULT: {
				CheckDownloadResult checkDownloadResult = (CheckDownloadResult)theEObject;
				T result = caseCheckDownloadResult(checkDownloadResult);
				if (result == null) result = caseCommand(checkDownloadResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_RUNTIME_TARGET: {
				GetRuntimeTarget getRuntimeTarget = (GetRuntimeTarget)theEObject;
				T result = caseGetRuntimeTarget(getRuntimeTarget);
				if (result == null) result = caseCommand(getRuntimeTarget);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_TEST_CASE_NAME: {
				GetTestCaseName getTestCaseName = (GetTestCaseName)theEObject;
				T result = caseGetTestCaseName(getTestCaseName);
				if (result == null) result = caseCommand(getTestCaseName);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TeslaPackage.GET_COMBO_ITEMS: {
				GetComboItems getComboItems = (GetComboItems)theEObject;
				T result = caseGetComboItems(getComboItems);
				if (result == null) result = caseControlCommand(getComboItems);
				if (result == null) result = caseCommand(getComboItems);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Wait</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Wait</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseWait(Wait object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Setup Player</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Setup Player</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetupPlayer(SetupPlayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Shoutdown Player</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Shoutdown Player</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShoutdownPlayer(ShoutdownPlayer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Process Status</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Process Status</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTeslaProcessStatus(TeslaProcessStatus object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Wrapper</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Wrapper</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseWrapper(Wrapper object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Property</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Property</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetProperty(GetProperty object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Verify True</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Verify True</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseVerifyTrue(VerifyTrue object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Verify False</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Verify False</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseVerifyFalse(VerifyFalse object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Verify Handler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Verify Handler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseVerifyHandler(VerifyHandler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Verify Error</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Verify Error</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseVerifyError(org.eclipse.rcptt.tesla.ecl.model.VerifyError object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Equals</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Equals</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseEquals(Equals object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Contains</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Contains</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseContains(Contains object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Matches</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Matches</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMatches(Matches object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Is Empty</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Is Empty</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIsEmpty(IsEmpty object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Unsupported</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Unsupported</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseUnsupported(Unsupported object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Control Not Found</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Control Not Found</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseControlNotFound(ControlNotFound object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Control Handler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Control Handler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseControlHandler(ControlHandler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Eclipse Window</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Eclipse Window</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetEclipseWindow(GetEclipseWindow object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Selector</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Selector</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSelector(Selector object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Path Selector</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Path Selector</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePathSelector(PathSelector object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Text Selector</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Text Selector</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTextSelector(TextSelector object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Control</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Control</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetControl(GetControl object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Button</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Button</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetButton(GetButton object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Canvas</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Canvas</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetCanvas(GetCanvas object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Checkbox</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Checkbox</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetCheckbox(GetCheckbox object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Combo</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Combo</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetCombo(GetCombo object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Editbox</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Editbox</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetEditbox(GetEditbox object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Group</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Group</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetGroup(GetGroup object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetItem(GetItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Label</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Label</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetLabel(GetLabel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Link</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Link</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetLink(GetLink object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get List</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get List</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetList(GetList object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Menu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Menu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetMenu(GetMenu object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Tab Folder</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Tab Folder</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetTabFolder(GetTabFolder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Tab Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Tab Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetTabItem(GetTabItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Table</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Table</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetTable(GetTable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Toolbar</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Toolbar</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetToolbar(GetToolbar object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Tree</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Tree</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetTree(GetTree object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Window</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Window</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetWindow(GetWindow object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get View</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get View</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetView(GetView object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Editor</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Editor</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetEditor(GetEditor object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Section</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Section</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetSection(GetSection object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Banner</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Banner</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetBanner(GetBanner object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Coolbar</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Coolbar</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetCoolbar(GetCoolbar object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Color Selector</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Color Selector</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetColorSelector(GetColorSelector object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Cell</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Cell</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetCell(GetCell object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Click</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Click</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClick(Click object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Double Click</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Double Click</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDoubleClick(DoubleClick object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Text</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Text</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetText(GetText object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Is Disabled</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Is Disabled</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIsDisabled(IsDisabled object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Is Disposed</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Is Disposed</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIsDisposed(IsDisposed object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Text</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Text</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeText(TypeText object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Key Type</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Key Type</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseKeyType(KeyType object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Type Command Key</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Type Command Key</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTypeCommandKey(TypeCommandKey object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Text</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Text</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetText(SetText object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Text Selection</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Text Selection</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetTextSelection(SetTextSelection object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Text Offset</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Text Offset</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetTextOffset(SetTextOffset object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Check</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Check</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCheck(Check object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Uncheck</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Uncheck</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseUncheck(Uncheck object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Select</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Select</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSelect(Select object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Cell Edit</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Cell Edit</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCellEdit(CellEdit object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Activate Cell Edit</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Activate Cell Edit</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseActivateCellEdit(ActivateCellEdit object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Apply Cell Edit</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Apply Cell Edit</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseApplyCellEdit(ApplyCellEdit object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Cancel Cell Edit</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Cancel Cell Edit</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCancelCellEdit(CancelCellEdit object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Deactivate Cell Edit</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Deactivate Cell Edit</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDeactivateCellEdit(DeactivateCellEdit object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Close</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Close</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClose(Close object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Return From Os Dialog</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Return From Os Dialog</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReturnFromOsDialog(ReturnFromOsDialog object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Wait Until Eclipse Is Ready</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Wait Until Eclipse Is Ready</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseWaitUntilEclipseIsReady(WaitUntilEclipseIsReady object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Show Content Assist</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Show Content Assist</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShowContentAssist(ShowContentAssist object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag Action</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag Action</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragAction(DragAction object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag Start</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag Start</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragStart(DragStart object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag End</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag End</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragEnd(DragEnd object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag Enter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag Enter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragEnter(DragEnter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag Exit</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag Exit</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragExit(DragExit object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag Set Data</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag Set Data</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragSetData(DragSetData object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag Accept</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag Accept</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragAccept(DragAccept object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag Detect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag Detect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragDetect(DragDetect object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drag Over</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drag Over</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDragOver(DragOver object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Drop</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Drop</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDrop(Drop object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Recognize</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Recognize</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRecognize(Recognize object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Recognize Response</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Recognize Response</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRecognizeResponse(RecognizeResponse object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Contains Image</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Contains Image</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseContainsImage(ContainsImage object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Region Text</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Region Text</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetRegionText(GetRegionText object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Region</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Region</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetRegion(GetRegion object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Advanced Info</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Advanced Info</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetAdvancedInfo(GetAdvancedInfo object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get About Menu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get About Menu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetAboutMenu(GetAboutMenu object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Preferences Menu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Preferences Menu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetPreferencesMenu(GetPreferencesMenu object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Date Time</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Date Time</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetDateTime(GetDateTime object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Slider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Slider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetSlider(GetSlider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Value</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Value</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetValue(SetValue object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Minimize</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Minimize</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMinimize(Minimize object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Maximize</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Maximize</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMaximize(Maximize object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Restore</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Restore</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRestore(Restore object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Show Tab List</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Show Tab List</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShowTabList(ShowTabList object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Options</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Options</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOptions(Options object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Exec With Options</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Exec With Options</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExecWithOptions(ExecWithOptions object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Shutdown Aut</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Shutdown Aut</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShutdownAut(ShutdownAut object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Dialog Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Dialog Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetDialogResult(SetDialogResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Property Nodes</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Property Nodes</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetPropertyNodes(GetPropertyNodes object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get By Os</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get By Os</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetByOs(GetByOs object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>From Raw Key</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>From Raw Key</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFromRawKey(FromRawKey object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Hover At Text Offset</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Hover At Text Offset</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHoverAtTextOffset(HoverAtTextOffset object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Text Viewer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Text Viewer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetTextViewer(GetTextViewer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Select Range</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Select Range</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSelectRange(SelectRange object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Caret Pos</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Caret Pos</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetCaretPos(SetCaretPos object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Hover Text</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Hover Text</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHoverText(HoverText object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Open Declaration</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Open Declaration</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOpenDeclaration(OpenDeclaration object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Vertical Ruler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Vertical Ruler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetVerticalRuler(GetVerticalRuler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Left Ruler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Left Ruler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetLeftRuler(GetLeftRuler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Ruler Column</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Ruler Column</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetRulerColumn(GetRulerColumn object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Right Ruler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Right Ruler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetRightRuler(GetRightRuler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Click Ruler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Click Ruler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClickRuler(ClickRuler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Double Click Ruler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Double Click Ruler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDoubleClickRuler(DoubleClickRuler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Hover Ruler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Hover Ruler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHoverRuler(HoverRuler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Control Command</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Control Command</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseControlCommand(ControlCommand object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Click Link</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Click Link</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClickLink(ClickLink object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Expand</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Expand</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExpand(Expand object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Collapse</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Collapse</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCollapse(Collapse object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Focus</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Focus</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetFocus(SetFocus object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Table Data</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Table Data</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetTableData(GetTableData object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Click Column</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Click Column</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClickColumn(ClickColumn object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Trace</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Trace</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTrace(Trace object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Mouse</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Mouse</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMouse(Mouse object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Object</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Object</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetObject(GetObject object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Items</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Items</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetItems(GetItems object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Expand All</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Expand All</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExpandAll(ExpandAll object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Collapse All</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Collapse All</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCollapseAll(CollapseAll object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Take Screenshot</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Take Screenshot</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTakeScreenshot(TakeScreenshot object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Select Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Select Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSelectItem(SelectItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Widget Details</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Widget Details</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetWidgetDetails(GetWidgetDetails object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Click Text</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Click Text</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClickText(ClickText object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Quick Access</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Quick Access</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetQuickAccess(GetQuickAccess object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Column Header</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Column Header</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetColumnHeader(GetColumnHeader object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Position</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Position</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetPosition(SetPosition object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Set Width</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Set Width</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSetWidth(SetWidth object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Property Tab</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Property Tab</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetPropertyTab(GetPropertyTab object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Show Alert</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Show Alert</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseShowAlert(ShowAlert object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Double Click Text</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Double Click Text</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDoubleClickText(DoubleClickText object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>To Control Handle</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>To Control Handle</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseToControlHandle(ToControlHandle object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Bound Control Handle</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Bound Control Handle</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBoundControlHandle(BoundControlHandle object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Unfocus</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Unfocus</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseUnfocus(Unfocus object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Decrypt</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Decrypt</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDecrypt(Decrypt object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Decrypt Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Decrypt Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDecryptResult(DecryptResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Restart Aut</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Restart Aut</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRestartAut(RestartAut object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Message Box Info</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Message Box Info</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMessageBoxInfo(MessageBoxInfo object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Last Message Box</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Last Message Box</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetLastMessageBox(GetLastMessageBox object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Check Download Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Check Download Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCheckDownloadResult(CheckDownloadResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Runtime Target</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Runtime Target</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetRuntimeTarget(GetRuntimeTarget object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Test Case Name</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Test Case Name</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetTestCaseName(GetTestCaseName object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Get Combo Items</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Get Combo Items</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGetComboItems(GetComboItems object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Command</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Command</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCommand(Command object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Process Status</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Process Status</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseProcessStatus(ProcessStatus object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	public T defaultCase(EObject object) {
		return null;
	}

} //TeslaSwitch
