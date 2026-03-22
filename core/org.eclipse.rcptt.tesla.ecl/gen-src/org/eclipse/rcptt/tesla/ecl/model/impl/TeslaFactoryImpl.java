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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.ecl.model.ActivateCellEdit;
import org.eclipse.rcptt.tesla.ecl.model.ActivationEventType;
import org.eclipse.rcptt.tesla.ecl.model.ApplyCellEdit;
import org.eclipse.rcptt.tesla.ecl.model.BoundControlHandle;
import org.eclipse.rcptt.tesla.ecl.model.Button;
import org.eclipse.rcptt.tesla.ecl.model.CancelCellEdit;
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
import org.eclipse.rcptt.tesla.ecl.model.Recognize;
import org.eclipse.rcptt.tesla.ecl.model.RecognizeResponse;
import org.eclipse.rcptt.tesla.ecl.model.RestartAut;
import org.eclipse.rcptt.tesla.ecl.model.Restore;
import org.eclipse.rcptt.tesla.ecl.model.ReturnFromOsDialog;
import org.eclipse.rcptt.tesla.ecl.model.Select;
import org.eclipse.rcptt.tesla.ecl.model.SelectItem;
import org.eclipse.rcptt.tesla.ecl.model.SelectRange;
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
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;
import org.eclipse.rcptt.tesla.ecl.model.TeslaPackage;
import org.eclipse.rcptt.tesla.ecl.model.TeslaProcessStatus;
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
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!--
 * end-user-doc -->
 * @generated
 */
public class TeslaFactoryImpl extends EFactoryImpl implements TeslaFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	public static TeslaFactory init() {
		try {
			TeslaFactory theTeslaFactory = (TeslaFactory)EPackage.Registry.INSTANCE.getEFactory(TeslaPackage.eNS_URI);
			if (theTeslaFactory != null) {
				return theTeslaFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new TeslaFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	public TeslaFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case TeslaPackage.WAIT: return createWait();
			case TeslaPackage.SETUP_PLAYER: return createSetupPlayer();
			case TeslaPackage.SHOUTDOWN_PLAYER: return createShoutdownPlayer();
			case TeslaPackage.TESLA_PROCESS_STATUS: return createTeslaProcessStatus();
			case TeslaPackage.WRAPPER: return createWrapper();
			case TeslaPackage.GET_PROPERTY: return createGetProperty();
			case TeslaPackage.VERIFY_TRUE: return createVerifyTrue();
			case TeslaPackage.VERIFY_FALSE: return createVerifyFalse();
			case TeslaPackage.VERIFY_HANDLER: return createVerifyHandler();
			case TeslaPackage.VERIFY_ERROR: return createVerifyError();
			case TeslaPackage.EQUALS: return createEquals();
			case TeslaPackage.CONTAINS: return createContains();
			case TeslaPackage.MATCHES: return createMatches();
			case TeslaPackage.IS_EMPTY: return createIsEmpty();
			case TeslaPackage.UNSUPPORTED: return createUnsupported();
			case TeslaPackage.CONTROL_NOT_FOUND: return createControlNotFound();
			case TeslaPackage.CONTROL_HANDLER: return createControlHandler();
			case TeslaPackage.GET_ECLIPSE_WINDOW: return createGetEclipseWindow();
			case TeslaPackage.GET_CONTROL: return createGetControl();
			case TeslaPackage.GET_BUTTON: return createGetButton();
			case TeslaPackage.GET_CANVAS: return createGetCanvas();
			case TeslaPackage.GET_CHECKBOX: return createGetCheckbox();
			case TeslaPackage.GET_COMBO: return createGetCombo();
			case TeslaPackage.GET_EDITBOX: return createGetEditbox();
			case TeslaPackage.GET_GROUP: return createGetGroup();
			case TeslaPackage.GET_ITEM: return createGetItem();
			case TeslaPackage.GET_LABEL: return createGetLabel();
			case TeslaPackage.GET_LINK: return createGetLink();
			case TeslaPackage.GET_LIST: return createGetList();
			case TeslaPackage.GET_MENU: return createGetMenu();
			case TeslaPackage.GET_TAB_FOLDER: return createGetTabFolder();
			case TeslaPackage.GET_TAB_ITEM: return createGetTabItem();
			case TeslaPackage.GET_TABLE: return createGetTable();
			case TeslaPackage.GET_TOOLBAR: return createGetToolbar();
			case TeslaPackage.GET_TREE: return createGetTree();
			case TeslaPackage.GET_WINDOW: return createGetWindow();
			case TeslaPackage.GET_VIEW: return createGetView();
			case TeslaPackage.GET_EDITOR: return createGetEditor();
			case TeslaPackage.GET_SECTION: return createGetSection();
			case TeslaPackage.GET_BANNER: return createGetBanner();
			case TeslaPackage.GET_COOLBAR: return createGetCoolbar();
			case TeslaPackage.GET_COLOR_SELECTOR: return createGetColorSelector();
			case TeslaPackage.GET_CELL: return createGetCell();
			case TeslaPackage.CLICK: return createClick();
			case TeslaPackage.DOUBLE_CLICK: return createDoubleClick();
			case TeslaPackage.GET_TEXT: return createGetText();
			case TeslaPackage.IS_DISABLED: return createIsDisabled();
			case TeslaPackage.IS_DISPOSED: return createIsDisposed();
			case TeslaPackage.TYPE_TEXT: return createTypeText();
			case TeslaPackage.KEY_TYPE: return createKeyType();
			case TeslaPackage.TYPE_COMMAND_KEY: return createTypeCommandKey();
			case TeslaPackage.SET_TEXT: return createSetText();
			case TeslaPackage.SET_TEXT_SELECTION: return createSetTextSelection();
			case TeslaPackage.SET_TEXT_OFFSET: return createSetTextOffset();
			case TeslaPackage.CHECK: return createCheck();
			case TeslaPackage.UNCHECK: return createUncheck();
			case TeslaPackage.SELECT: return createSelect();
			case TeslaPackage.ACTIVATE_CELL_EDIT: return createActivateCellEdit();
			case TeslaPackage.APPLY_CELL_EDIT: return createApplyCellEdit();
			case TeslaPackage.CANCEL_CELL_EDIT: return createCancelCellEdit();
			case TeslaPackage.DEACTIVATE_CELL_EDIT: return createDeactivateCellEdit();
			case TeslaPackage.CLOSE: return createClose();
			case TeslaPackage.RETURN_FROM_OS_DIALOG: return createReturnFromOsDialog();
			case TeslaPackage.WAIT_UNTIL_ECLIPSE_IS_READY: return createWaitUntilEclipseIsReady();
			case TeslaPackage.SHOW_CONTENT_ASSIST: return createShowContentAssist();
			case TeslaPackage.DRAG_ACTION: return createDragAction();
			case TeslaPackage.DRAG_START: return createDragStart();
			case TeslaPackage.DRAG_END: return createDragEnd();
			case TeslaPackage.DRAG_ENTER: return createDragEnter();
			case TeslaPackage.DRAG_EXIT: return createDragExit();
			case TeslaPackage.DRAG_SET_DATA: return createDragSetData();
			case TeslaPackage.DRAG_ACCEPT: return createDragAccept();
			case TeslaPackage.DRAG_DETECT: return createDragDetect();
			case TeslaPackage.DRAG_OVER: return createDragOver();
			case TeslaPackage.DROP: return createDrop();
			case TeslaPackage.RECOGNIZE: return createRecognize();
			case TeslaPackage.RECOGNIZE_RESPONSE: return createRecognizeResponse();
			case TeslaPackage.CONTAINS_IMAGE: return createContainsImage();
			case TeslaPackage.GET_REGION_TEXT: return createGetRegionText();
			case TeslaPackage.GET_REGION: return createGetRegion();
			case TeslaPackage.GET_ADVANCED_INFO: return createGetAdvancedInfo();
			case TeslaPackage.GET_ABOUT_MENU: return createGetAboutMenu();
			case TeslaPackage.GET_PREFERENCES_MENU: return createGetPreferencesMenu();
			case TeslaPackage.GET_DATE_TIME: return createGetDateTime();
			case TeslaPackage.GET_SLIDER: return createGetSlider();
			case TeslaPackage.SET_VALUE: return createSetValue();
			case TeslaPackage.MINIMIZE: return createMinimize();
			case TeslaPackage.MAXIMIZE: return createMaximize();
			case TeslaPackage.RESTORE: return createRestore();
			case TeslaPackage.SHOW_TAB_LIST: return createShowTabList();
			case TeslaPackage.OPTIONS: return createOptions();
			case TeslaPackage.EXEC_WITH_OPTIONS: return createExecWithOptions();
			case TeslaPackage.SHUTDOWN_AUT: return createShutdownAut();
			case TeslaPackage.SET_DIALOG_RESULT: return createSetDialogResult();
			case TeslaPackage.GET_PROPERTY_NODES: return createGetPropertyNodes();
			case TeslaPackage.GET_BY_OS: return createGetByOs();
			case TeslaPackage.FROM_RAW_KEY: return createFromRawKey();
			case TeslaPackage.HOVER_AT_TEXT_OFFSET: return createHoverAtTextOffset();
			case TeslaPackage.GET_TEXT_VIEWER: return createGetTextViewer();
			case TeslaPackage.SELECT_RANGE: return createSelectRange();
			case TeslaPackage.SET_CARET_POS: return createSetCaretPos();
			case TeslaPackage.HOVER_TEXT: return createHoverText();
			case TeslaPackage.OPEN_DECLARATION: return createOpenDeclaration();
			case TeslaPackage.GET_VERTICAL_RULER: return createGetVerticalRuler();
			case TeslaPackage.GET_LEFT_RULER: return createGetLeftRuler();
			case TeslaPackage.GET_RULER_COLUMN: return createGetRulerColumn();
			case TeslaPackage.GET_RIGHT_RULER: return createGetRightRuler();
			case TeslaPackage.CLICK_RULER: return createClickRuler();
			case TeslaPackage.DOUBLE_CLICK_RULER: return createDoubleClickRuler();
			case TeslaPackage.HOVER_RULER: return createHoverRuler();
			case TeslaPackage.CLICK_LINK: return createClickLink();
			case TeslaPackage.EXPAND: return createExpand();
			case TeslaPackage.COLLAPSE: return createCollapse();
			case TeslaPackage.SET_FOCUS: return createSetFocus();
			case TeslaPackage.GET_TABLE_DATA: return createGetTableData();
			case TeslaPackage.CLICK_COLUMN: return createClickColumn();
			case TeslaPackage.TRACE: return createTrace();
			case TeslaPackage.MOUSE: return createMouse();
			case TeslaPackage.GET_OBJECT: return createGetObject();
			case TeslaPackage.GET_ITEMS: return createGetItems();
			case TeslaPackage.EXPAND_ALL: return createExpandAll();
			case TeslaPackage.COLLAPSE_ALL: return createCollapseAll();
			case TeslaPackage.TAKE_SCREENSHOT: return createTakeScreenshot();
			case TeslaPackage.SELECT_ITEM: return createSelectItem();
			case TeslaPackage.GET_WIDGET_DETAILS: return createGetWidgetDetails();
			case TeslaPackage.CLICK_TEXT: return createClickText();
			case TeslaPackage.GET_QUICK_ACCESS: return createGetQuickAccess();
			case TeslaPackage.GET_COLUMN_HEADER: return createGetColumnHeader();
			case TeslaPackage.SET_POSITION: return createSetPosition();
			case TeslaPackage.SET_WIDTH: return createSetWidth();
			case TeslaPackage.GET_PROPERTY_TAB: return createGetPropertyTab();
			case TeslaPackage.SHOW_ALERT: return createShowAlert();
			case TeslaPackage.DOUBLE_CLICK_TEXT: return createDoubleClickText();
			case TeslaPackage.TO_CONTROL_HANDLE: return createToControlHandle();
			case TeslaPackage.BOUND_CONTROL_HANDLE: return createBoundControlHandle();
			case TeslaPackage.UNFOCUS: return createUnfocus();
			case TeslaPackage.DECRYPT: return createDecrypt();
			case TeslaPackage.DECRYPT_RESULT: return createDecryptResult();
			case TeslaPackage.RESTART_AUT: return createRestartAut();
			case TeslaPackage.MESSAGE_BOX_INFO: return createMessageBoxInfo();
			case TeslaPackage.GET_LAST_MESSAGE_BOX: return createGetLastMessageBox();
			case TeslaPackage.CHECK_DOWNLOAD_RESULT: return createCheckDownloadResult();
			case TeslaPackage.GET_RUNTIME_TARGET: return createGetRuntimeTarget();
			case TeslaPackage.GET_TEST_CASE_NAME: return createGetTestCaseName();
			case TeslaPackage.GET_COMBO_ITEMS: return createGetComboItems();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case TeslaPackage.BUTTON:
				return createButtonFromString(eDataType, initialValue);
			case TeslaPackage.ACTIVATION_EVENT_TYPE:
				return createActivationEventTypeFromString(eDataType, initialValue);
			case TeslaPackage.ELEMENT_KIND:
				return createElementKindFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case TeslaPackage.BUTTON:
				return convertButtonToString(eDataType, instanceValue);
			case TeslaPackage.ACTIVATION_EVENT_TYPE:
				return convertActivationEventTypeToString(eDataType, instanceValue);
			case TeslaPackage.ELEMENT_KIND:
				return convertElementKindToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Wait createWait() {
		WaitImpl wait = new WaitImpl();
		return wait;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SetupPlayer createSetupPlayer() {
		SetupPlayerImpl setupPlayer = new SetupPlayerImpl();
		return setupPlayer;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ShoutdownPlayer createShoutdownPlayer() {
		ShoutdownPlayerImpl shoutdownPlayer = new ShoutdownPlayerImpl();
		return shoutdownPlayer;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public TeslaProcessStatus createTeslaProcessStatus() {
		TeslaProcessStatusImpl teslaProcessStatus = new TeslaProcessStatusImpl();
		return teslaProcessStatus;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Wrapper createWrapper() {
		WrapperImpl wrapper = new WrapperImpl();
		return wrapper;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetProperty createGetProperty() {
		GetPropertyImpl getProperty = new GetPropertyImpl();
		return getProperty;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public VerifyTrue createVerifyTrue() {
		VerifyTrueImpl verifyTrue = new VerifyTrueImpl();
		return verifyTrue;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public VerifyFalse createVerifyFalse() {
		VerifyFalseImpl verifyFalse = new VerifyFalseImpl();
		return verifyFalse;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public VerifyHandler createVerifyHandler() {
		VerifyHandlerImpl verifyHandler = new VerifyHandlerImpl();
		return verifyHandler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public org.eclipse.rcptt.tesla.ecl.model.VerifyError createVerifyError() {
		VerifyErrorImpl verifyError = new VerifyErrorImpl();
		return verifyError;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Equals createEquals() {
		EqualsImpl equals = new EqualsImpl();
		return equals;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Contains createContains() {
		ContainsImpl contains = new ContainsImpl();
		return contains;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Matches createMatches() {
		MatchesImpl matches = new MatchesImpl();
		return matches;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public IsEmpty createIsEmpty() {
		IsEmptyImpl isEmpty = new IsEmptyImpl();
		return isEmpty;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Unsupported createUnsupported() {
		UnsupportedImpl unsupported = new UnsupportedImpl();
		return unsupported;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ControlNotFound createControlNotFound() {
		ControlNotFoundImpl controlNotFound = new ControlNotFoundImpl();
		return controlNotFound;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ControlHandler createControlHandler() {
		ControlHandlerImpl controlHandler = new ControlHandlerImpl();
		return controlHandler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetEclipseWindow createGetEclipseWindow() {
		GetEclipseWindowImpl getEclipseWindow = new GetEclipseWindowImpl();
		return getEclipseWindow;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetControl createGetControl() {
		GetControlImpl getControl = new GetControlImpl();
		return getControl;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetButton createGetButton() {
		GetButtonImpl getButton = new GetButtonImpl();
		return getButton;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetCanvas createGetCanvas() {
		GetCanvasImpl getCanvas = new GetCanvasImpl();
		return getCanvas;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetCheckbox createGetCheckbox() {
		GetCheckboxImpl getCheckbox = new GetCheckboxImpl();
		return getCheckbox;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetCombo createGetCombo() {
		GetComboImpl getCombo = new GetComboImpl();
		return getCombo;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetEditbox createGetEditbox() {
		GetEditboxImpl getEditbox = new GetEditboxImpl();
		return getEditbox;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetGroup createGetGroup() {
		GetGroupImpl getGroup = new GetGroupImpl();
		return getGroup;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetItem createGetItem() {
		GetItemImpl getItem = new GetItemImpl();
		return getItem;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetLabel createGetLabel() {
		GetLabelImpl getLabel = new GetLabelImpl();
		return getLabel;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetLink createGetLink() {
		GetLinkImpl getLink = new GetLinkImpl();
		return getLink;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetList createGetList() {
		GetListImpl getList = new GetListImpl();
		return getList;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetMenu createGetMenu() {
		GetMenuImpl getMenu = new GetMenuImpl();
		return getMenu;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetTabFolder createGetTabFolder() {
		GetTabFolderImpl getTabFolder = new GetTabFolderImpl();
		return getTabFolder;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetTabItem createGetTabItem() {
		GetTabItemImpl getTabItem = new GetTabItemImpl();
		return getTabItem;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetTable createGetTable() {
		GetTableImpl getTable = new GetTableImpl();
		return getTable;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetToolbar createGetToolbar() {
		GetToolbarImpl getToolbar = new GetToolbarImpl();
		return getToolbar;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetTree createGetTree() {
		GetTreeImpl getTree = new GetTreeImpl();
		return getTree;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetWindow createGetWindow() {
		GetWindowImpl getWindow = new GetWindowImpl();
		return getWindow;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetView createGetView() {
		GetViewImpl getView = new GetViewImpl();
		return getView;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetEditor createGetEditor() {
		GetEditorImpl getEditor = new GetEditorImpl();
		return getEditor;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetSection createGetSection() {
		GetSectionImpl getSection = new GetSectionImpl();
		return getSection;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetBanner createGetBanner() {
		GetBannerImpl getBanner = new GetBannerImpl();
		return getBanner;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetCoolbar createGetCoolbar() {
		GetCoolbarImpl getCoolbar = new GetCoolbarImpl();
		return getCoolbar;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetColorSelector createGetColorSelector() {
		GetColorSelectorImpl getColorSelector = new GetColorSelectorImpl();
		return getColorSelector;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetCell createGetCell() {
		GetCellImpl getCell = new GetCellImpl();
		return getCell;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Click createClick() {
		ClickImpl click = new ClickImpl();
		return click;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DoubleClick createDoubleClick() {
		DoubleClickImpl doubleClick = new DoubleClickImpl();
		return doubleClick;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetText createGetText() {
		GetTextImpl getText = new GetTextImpl();
		return getText;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public IsDisabled createIsDisabled() {
		IsDisabledImpl isDisabled = new IsDisabledImpl();
		return isDisabled;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public IsDisposed createIsDisposed() {
		IsDisposedImpl isDisposed = new IsDisposedImpl();
		return isDisposed;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public TypeText createTypeText() {
		TypeTextImpl typeText = new TypeTextImpl();
		return typeText;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public KeyType createKeyType() {
		KeyTypeImpl keyType = new KeyTypeImpl();
		return keyType;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public TypeCommandKey createTypeCommandKey() {
		TypeCommandKeyImpl typeCommandKey = new TypeCommandKeyImpl();
		return typeCommandKey;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SetText createSetText() {
		SetTextImpl setText = new SetTextImpl();
		return setText;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SetTextSelection createSetTextSelection() {
		SetTextSelectionImpl setTextSelection = new SetTextSelectionImpl();
		return setTextSelection;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SetTextOffset createSetTextOffset() {
		SetTextOffsetImpl setTextOffset = new SetTextOffsetImpl();
		return setTextOffset;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Check createCheck() {
		CheckImpl check = new CheckImpl();
		return check;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Uncheck createUncheck() {
		UncheckImpl uncheck = new UncheckImpl();
		return uncheck;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Select createSelect() {
		SelectImpl select = new SelectImpl();
		return select;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ActivateCellEdit createActivateCellEdit() {
		ActivateCellEditImpl activateCellEdit = new ActivateCellEditImpl();
		return activateCellEdit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ApplyCellEdit createApplyCellEdit() {
		ApplyCellEditImpl applyCellEdit = new ApplyCellEditImpl();
		return applyCellEdit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public CancelCellEdit createCancelCellEdit() {
		CancelCellEditImpl cancelCellEdit = new CancelCellEditImpl();
		return cancelCellEdit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DeactivateCellEdit createDeactivateCellEdit() {
		DeactivateCellEditImpl deactivateCellEdit = new DeactivateCellEditImpl();
		return deactivateCellEdit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Close createClose() {
		CloseImpl close = new CloseImpl();
		return close;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ReturnFromOsDialog createReturnFromOsDialog() {
		ReturnFromOsDialogImpl returnFromOsDialog = new ReturnFromOsDialogImpl();
		return returnFromOsDialog;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public WaitUntilEclipseIsReady createWaitUntilEclipseIsReady() {
		WaitUntilEclipseIsReadyImpl waitUntilEclipseIsReady = new WaitUntilEclipseIsReadyImpl();
		return waitUntilEclipseIsReady;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ShowContentAssist createShowContentAssist() {
		ShowContentAssistImpl showContentAssist = new ShowContentAssistImpl();
		return showContentAssist;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragAction createDragAction() {
		DragActionImpl dragAction = new DragActionImpl();
		return dragAction;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragStart createDragStart() {
		DragStartImpl dragStart = new DragStartImpl();
		return dragStart;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragEnd createDragEnd() {
		DragEndImpl dragEnd = new DragEndImpl();
		return dragEnd;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragEnter createDragEnter() {
		DragEnterImpl dragEnter = new DragEnterImpl();
		return dragEnter;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragExit createDragExit() {
		DragExitImpl dragExit = new DragExitImpl();
		return dragExit;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragSetData createDragSetData() {
		DragSetDataImpl dragSetData = new DragSetDataImpl();
		return dragSetData;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragAccept createDragAccept() {
		DragAcceptImpl dragAccept = new DragAcceptImpl();
		return dragAccept;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragDetect createDragDetect() {
		DragDetectImpl dragDetect = new DragDetectImpl();
		return dragDetect;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DragOver createDragOver() {
		DragOverImpl dragOver = new DragOverImpl();
		return dragOver;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Drop createDrop() {
		DropImpl drop = new DropImpl();
		return drop;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Recognize createRecognize() {
		RecognizeImpl recognize = new RecognizeImpl();
		return recognize;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public RecognizeResponse createRecognizeResponse() {
		RecognizeResponseImpl recognizeResponse = new RecognizeResponseImpl();
		return recognizeResponse;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ContainsImage createContainsImage() {
		ContainsImageImpl containsImage = new ContainsImageImpl();
		return containsImage;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetRegionText createGetRegionText() {
		GetRegionTextImpl getRegionText = new GetRegionTextImpl();
		return getRegionText;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetRegion createGetRegion() {
		GetRegionImpl getRegion = new GetRegionImpl();
		return getRegion;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetAdvancedInfo createGetAdvancedInfo() {
		GetAdvancedInfoImpl getAdvancedInfo = new GetAdvancedInfoImpl();
		return getAdvancedInfo;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetAboutMenu createGetAboutMenu() {
		GetAboutMenuImpl getAboutMenu = new GetAboutMenuImpl();
		return getAboutMenu;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetPreferencesMenu createGetPreferencesMenu() {
		GetPreferencesMenuImpl getPreferencesMenu = new GetPreferencesMenuImpl();
		return getPreferencesMenu;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetDateTime createGetDateTime() {
		GetDateTimeImpl getDateTime = new GetDateTimeImpl();
		return getDateTime;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetSlider createGetSlider() {
		GetSliderImpl getSlider = new GetSliderImpl();
		return getSlider;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SetValue createSetValue() {
		SetValueImpl setValue = new SetValueImpl();
		return setValue;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Minimize createMinimize() {
		MinimizeImpl minimize = new MinimizeImpl();
		return minimize;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Maximize createMaximize() {
		MaximizeImpl maximize = new MaximizeImpl();
		return maximize;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Restore createRestore() {
		RestoreImpl restore = new RestoreImpl();
		return restore;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ShowTabList createShowTabList() {
		ShowTabListImpl showTabList = new ShowTabListImpl();
		return showTabList;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Options createOptions() {
		OptionsImpl options = new OptionsImpl();
		return options;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ExecWithOptions createExecWithOptions() {
		ExecWithOptionsImpl execWithOptions = new ExecWithOptionsImpl();
		return execWithOptions;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ShutdownAut createShutdownAut() {
		ShutdownAutImpl shutdownAut = new ShutdownAutImpl();
		return shutdownAut;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SetDialogResult createSetDialogResult() {
		SetDialogResultImpl setDialogResult = new SetDialogResultImpl();
		return setDialogResult;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetPropertyNodes createGetPropertyNodes() {
		GetPropertyNodesImpl getPropertyNodes = new GetPropertyNodesImpl();
		return getPropertyNodes;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetByOs createGetByOs() {
		GetByOsImpl getByOs = new GetByOsImpl();
		return getByOs;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public FromRawKey createFromRawKey() {
		FromRawKeyImpl fromRawKey = new FromRawKeyImpl();
		return fromRawKey;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public HoverAtTextOffset createHoverAtTextOffset() {
		HoverAtTextOffsetImpl hoverAtTextOffset = new HoverAtTextOffsetImpl();
		return hoverAtTextOffset;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetTextViewer createGetTextViewer() {
		GetTextViewerImpl getTextViewer = new GetTextViewerImpl();
		return getTextViewer;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SelectRange createSelectRange() {
		SelectRangeImpl selectRange = new SelectRangeImpl();
		return selectRange;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SetCaretPos createSetCaretPos() {
		SetCaretPosImpl setCaretPos = new SetCaretPosImpl();
		return setCaretPos;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public HoverText createHoverText() {
		HoverTextImpl hoverText = new HoverTextImpl();
		return hoverText;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public OpenDeclaration createOpenDeclaration() {
		OpenDeclarationImpl openDeclaration = new OpenDeclarationImpl();
		return openDeclaration;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetVerticalRuler createGetVerticalRuler() {
		GetVerticalRulerImpl getVerticalRuler = new GetVerticalRulerImpl();
		return getVerticalRuler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetLeftRuler createGetLeftRuler() {
		GetLeftRulerImpl getLeftRuler = new GetLeftRulerImpl();
		return getLeftRuler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetRulerColumn createGetRulerColumn() {
		GetRulerColumnImpl getRulerColumn = new GetRulerColumnImpl();
		return getRulerColumn;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetRightRuler createGetRightRuler() {
		GetRightRulerImpl getRightRuler = new GetRightRulerImpl();
		return getRightRuler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ClickRuler createClickRuler() {
		ClickRulerImpl clickRuler = new ClickRulerImpl();
		return clickRuler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DoubleClickRuler createDoubleClickRuler() {
		DoubleClickRulerImpl doubleClickRuler = new DoubleClickRulerImpl();
		return doubleClickRuler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public HoverRuler createHoverRuler() {
		HoverRulerImpl hoverRuler = new HoverRulerImpl();
		return hoverRuler;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ClickLink createClickLink() {
		ClickLinkImpl clickLink = new ClickLinkImpl();
		return clickLink;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Expand createExpand() {
		ExpandImpl expand = new ExpandImpl();
		return expand;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Collapse createCollapse() {
		CollapseImpl collapse = new CollapseImpl();
		return collapse;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SetFocus createSetFocus() {
		SetFocusImpl setFocus = new SetFocusImpl();
		return setFocus;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetTableData createGetTableData() {
		GetTableDataImpl getTableData = new GetTableDataImpl();
		return getTableData;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ClickColumn createClickColumn() {
		ClickColumnImpl clickColumn = new ClickColumnImpl();
		return clickColumn;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Trace createTrace() {
		TraceImpl trace = new TraceImpl();
		return trace;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Mouse createMouse() {
		MouseImpl mouse = new MouseImpl();
		return mouse;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetObject createGetObject() {
		GetObjectImpl getObject = new GetObjectImpl();
		return getObject;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetItems createGetItems() {
		GetItemsImpl getItems = new GetItemsImpl();
		return getItems;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ExpandAll createExpandAll() {
		ExpandAllImpl expandAll = new ExpandAllImpl();
		return expandAll;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public CollapseAll createCollapseAll() {
		CollapseAllImpl collapseAll = new CollapseAllImpl();
		return collapseAll;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public TakeScreenshot createTakeScreenshot() {
		TakeScreenshotImpl takeScreenshot = new TakeScreenshotImpl();
		return takeScreenshot;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public SelectItem createSelectItem() {
		SelectItemImpl selectItem = new SelectItemImpl();
		return selectItem;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetWidgetDetails createGetWidgetDetails() {
		GetWidgetDetailsImpl getWidgetDetails = new GetWidgetDetailsImpl();
		return getWidgetDetails;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ClickText createClickText() {
		ClickTextImpl clickText = new ClickTextImpl();
		return clickText;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetQuickAccess createGetQuickAccess() {
		GetQuickAccessImpl getQuickAccess = new GetQuickAccessImpl();
		return getQuickAccess;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetColumnHeader createGetColumnHeader() {
		GetColumnHeaderImpl getColumnHeader = new GetColumnHeaderImpl();
		return getColumnHeader;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SetPosition createSetPosition() {
		SetPositionImpl setPosition = new SetPositionImpl();
		return setPosition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SetWidth createSetWidth() {
		SetWidthImpl setWidth = new SetWidthImpl();
		return setWidth;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public GetPropertyTab createGetPropertyTab() {
		GetPropertyTabImpl getPropertyTab = new GetPropertyTabImpl();
		return getPropertyTab;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ShowAlert createShowAlert() {
		ShowAlertImpl showAlert = new ShowAlertImpl();
		return showAlert;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DoubleClickText createDoubleClickText() {
		DoubleClickTextImpl doubleClickText = new DoubleClickTextImpl();
		return doubleClickText;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ToControlHandle createToControlHandle() {
		ToControlHandleImpl toControlHandle = new ToControlHandleImpl();
		return toControlHandle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BoundControlHandle createBoundControlHandle() {
		BoundControlHandleImpl boundControlHandle = new BoundControlHandleImpl();
		return boundControlHandle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Unfocus createUnfocus() {
		UnfocusImpl unfocus = new UnfocusImpl();
		return unfocus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Decrypt createDecrypt() {
		DecryptImpl decrypt = new DecryptImpl();
		return decrypt;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DecryptResult createDecryptResult() {
		DecryptResultImpl decryptResult = new DecryptResultImpl();
		return decryptResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RestartAut createRestartAut() {
		RestartAutImpl restartAut = new RestartAutImpl();
		return restartAut;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MessageBoxInfo createMessageBoxInfo() {
		MessageBoxInfoImpl messageBoxInfo = new MessageBoxInfoImpl();
		return messageBoxInfo;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GetLastMessageBox createGetLastMessageBox() {
		GetLastMessageBoxImpl getLastMessageBox = new GetLastMessageBoxImpl();
		return getLastMessageBox;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CheckDownloadResult createCheckDownloadResult() {
		CheckDownloadResultImpl checkDownloadResult = new CheckDownloadResultImpl();
		return checkDownloadResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GetRuntimeTarget createGetRuntimeTarget() {
		GetRuntimeTargetImpl getRuntimeTarget = new GetRuntimeTargetImpl();
		return getRuntimeTarget;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GetTestCaseName createGetTestCaseName() {
		GetTestCaseNameImpl getTestCaseName = new GetTestCaseNameImpl();
		return getTestCaseName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GetComboItems createGetComboItems() {
		GetComboItemsImpl getComboItems = new GetComboItemsImpl();
		return getComboItems;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public Button createButtonFromString(EDataType eDataType,
			String initialValue) {
		Button result = Button.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public String convertButtonToString(EDataType eDataType,
			Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ActivationEventType createActivationEventTypeFromString(EDataType eDataType, String initialValue) {
		ActivationEventType result = ActivationEventType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertActivationEventTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public ElementKind createElementKindFromString(EDataType eDataType,
			String initialValue) {
		return (ElementKind)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public String convertElementKindToString(EDataType eDataType,
			Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public TeslaPackage getTeslaPackage() {
		return (TeslaPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static TeslaPackage getPackage() {
		return TeslaPackage.eINSTANCE;
	}

} // TeslaFactoryImpl
