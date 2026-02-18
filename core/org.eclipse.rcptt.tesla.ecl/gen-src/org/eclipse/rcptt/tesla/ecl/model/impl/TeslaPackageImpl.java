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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.rcptt.ecl.core.CorePackage;
import org.eclipse.rcptt.tesla.core.info.InfoPackage;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolPackage;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawPackage;
import org.eclipse.rcptt.tesla.core.ui.UiPackage;
import org.eclipse.rcptt.tesla.ecl.model.ActivateCellEdit;
import org.eclipse.rcptt.tesla.ecl.model.ActivationEventType;
import org.eclipse.rcptt.tesla.ecl.model.ApplyCellEdit;
import org.eclipse.rcptt.tesla.ecl.model.BoundControlHandle;
import org.eclipse.rcptt.tesla.ecl.model.Button;
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
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;
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
import org.eclipse.rcptt.tesla.ecl.model.diagram.DiagramPackage;
import org.eclipse.rcptt.tesla.ecl.model.diagram.impl.DiagramPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class TeslaPackageImpl extends EPackageImpl implements TeslaPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass waitEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setupPlayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass shoutdownPlayerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass teslaProcessStatusEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass wrapperEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getPropertyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass verifyTrueEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass verifyFalseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass verifyHandlerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass verifyErrorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass equalsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass containsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass matchesEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass isEmptyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass unsupportedEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass controlNotFoundEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass controlHandlerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getEclipseWindowEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass selectorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass pathSelectorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass textSelectorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getControlEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getButtonEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getCanvasEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getCheckboxEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getComboEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getEditboxEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getGroupEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getLabelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getLinkEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getListEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getMenuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTabFolderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTabItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTableEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getToolbarEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTreeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getWindowEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getViewEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getEditorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getSectionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getBannerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getCoolbarEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getColorSelectorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getCellEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clickEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass doubleClickEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass isDisabledEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass isDisposedEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeTextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass keyTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass typeCommandKeyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setTextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setTextSelectionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setTextOffsetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass checkEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass uncheckEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass selectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass cellEditEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass activateCellEditEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass applyCellEditEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass cancelCellEditEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass deactivateCellEditEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass closeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass returnFromOsDialogEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass waitUntilEclipseIsReadyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass showContentAssistEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragActionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragStartEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragEndEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragEnterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragExitEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragSetDataEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragAcceptEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragDetectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dragOverEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dropEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass recognizeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass recognizeResponseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass containsImageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getRegionTextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getRegionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getAdvancedInfoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getAboutMenuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getPreferencesMenuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getDateTimeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getSliderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setValueEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass minimizeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass maximizeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass restoreEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass showTabListEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass optionsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass execWithOptionsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass shutdownAutEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setDialogResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getPropertyNodesEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getByOsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass fromRawKeyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass hoverAtTextOffsetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTextViewerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass selectRangeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setCaretPosEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass hoverTextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass openDeclarationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getVerticalRulerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getLeftRulerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getRulerColumnEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getRightRulerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clickRulerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass doubleClickRulerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass hoverRulerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass controlCommandEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clickLinkEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass expandEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass collapseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setFocusEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTableDataEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clickColumnEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass traceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mouseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getObjectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getItemsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass expandAllEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass collapseAllEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass takeScreenshotEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass selectItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getWidgetDetailsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clickTextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getQuickAccessEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getColumnHeaderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setPositionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass setWidthEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getPropertyTabEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass showAlertEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass doubleClickTextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toControlHandleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass boundControlHandleEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass unfocusEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass decryptEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass decryptResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass restartAutEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass messageBoxInfoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getLastMessageBoxEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass checkDownloadResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getRuntimeTargetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getTestCaseNameEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getComboItemsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum buttonEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum activationEventTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType elementKindEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TeslaPackageImpl() {
		super(eNS_URI, TeslaFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link TeslaPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static TeslaPackage init() {
		if (isInited) return (TeslaPackage)EPackage.Registry.INSTANCE.getEPackage(TeslaPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredTeslaPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		TeslaPackageImpl theTeslaPackage = registeredTeslaPackage instanceof TeslaPackageImpl ? (TeslaPackageImpl)registeredTeslaPackage : new TeslaPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		UiPackage.eINSTANCE.eClass();
		CorePackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();
		InfoPackage.eINSTANCE.eClass();
		RawPackage.eINSTANCE.eClass();
		ProtocolPackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DiagramPackage.eNS_URI);
		DiagramPackageImpl theDiagramPackage = (DiagramPackageImpl)(registeredPackage instanceof DiagramPackageImpl ? registeredPackage : DiagramPackage.eINSTANCE);

		// Create package meta-data objects
		theTeslaPackage.createPackageContents();
		theDiagramPackage.createPackageContents();

		// Initialize created meta-data
		theTeslaPackage.initializePackageContents();
		theDiagramPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTeslaPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(TeslaPackage.eNS_URI, theTeslaPackage);
		return theTeslaPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWait() {
		return waitEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getWait_Ms() {
		return (EAttribute)waitEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetupPlayer() {
		return setupPlayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getShoutdownPlayer() {
		return shoutdownPlayerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTeslaProcessStatus() {
		return teslaProcessStatusEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTeslaProcessStatus_Info() {
		return (EReference)teslaProcessStatusEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWrapper() {
		return wrapperEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getWrapper_Object() {
		return (EAttribute)wrapperEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetProperty() {
		return getPropertyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGetProperty_Object() {
		return (EReference)getPropertyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetProperty_Name() {
		return (EAttribute)getPropertyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetProperty_Index() {
		return (EAttribute)getPropertyEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetProperty_Raw() {
		return (EAttribute)getPropertyEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getVerifyTrue() {
		return verifyTrueEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVerifyTrue_Condition() {
		return (EReference)verifyTrueEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getVerifyFalse() {
		return verifyFalseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVerifyFalse_Condition() {
		return (EReference)verifyFalseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getVerifyHandler() {
		return verifyHandlerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVerifyHandler_Element() {
		return (EReference)verifyHandlerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVerifyHandler_Attribute() {
		return (EAttribute)verifyHandlerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVerifyHandler_Value() {
		return (EAttribute)verifyHandlerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVerifyHandler_Kind() {
		return (EAttribute)verifyHandlerEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVerifyHandler_Index() {
		return (EAttribute)verifyHandlerEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getVerifyError() {
		return verifyErrorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVerifyError_Command() {
		return (EReference)verifyErrorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEquals() {
		return equalsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEquals_Input() {
		return (EReference)equalsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEquals_Value() {
		return (EAttribute)equalsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getContains() {
		return containsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getContains_Input() {
		return (EReference)containsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getContains_Value() {
		return (EAttribute)containsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMatches() {
		return matchesEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMatches_Input() {
		return (EReference)matchesEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMatches_Value() {
		return (EAttribute)matchesEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIsEmpty() {
		return isEmptyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIsEmpty_Input() {
		return (EReference)isEmptyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getUnsupported() {
		return unsupportedEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getUnsupported_Desc() {
		return (EAttribute)unsupportedEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getControlNotFound() {
		return controlNotFoundEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlNotFound_Id() {
		return (EAttribute)controlNotFoundEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getControlHandler() {
		return controlHandlerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Kind() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_CustomKindId() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Text() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Type() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Index() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Path() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Indexes() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getControlHandler_After() {
		return (EReference)controlHandlerEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getControlHandler_Parent() {
		return (EReference)controlHandlerEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getControlHandler_Resolved() {
		return (EReference)controlHandlerEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Row() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Column() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_X() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_Y() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getControlHandler_RawImage() {
		return (EAttribute)controlHandlerEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetEclipseWindow() {
		return getEclipseWindowEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSelector() {
		return selectorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelector_Id() {
		return (EAttribute)selectorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSelector_After() {
		return (EReference)selectorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelector_Type() {
		return (EAttribute)selectorEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelector_Index() {
		return (EAttribute)selectorEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSelector_Parent() {
		return (EReference)selectorEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPathSelector() {
		return pathSelectorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPathSelector_Path() {
		return (EAttribute)pathSelectorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTextSelector() {
		return textSelectorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTextSelector_Text() {
		return (EAttribute)textSelectorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetControl() {
		return getControlEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetControl_Kind() {
		return (EAttribute)getControlEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetButton() {
		return getButtonEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetCanvas() {
		return getCanvasEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetCheckbox() {
		return getCheckboxEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetCombo() {
		return getComboEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetEditbox() {
		return getEditboxEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetGroup() {
		return getGroupEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetItem() {
		return getItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetItem_Path() {
		return (EAttribute)getItemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetItem_Column() {
		return (EAttribute)getItemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetLabel() {
		return getLabelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetLink() {
		return getLinkEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetList() {
		return getListEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetMenu() {
		return getMenuEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetTabFolder() {
		return getTabFolderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetTabItem() {
		return getTabItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetTable() {
		return getTableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetToolbar() {
		return getToolbarEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetTree() {
		return getTreeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetWindow() {
		return getWindowEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetWindow_Text() {
		return (EAttribute)getWindowEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetWindow_From() {
		return (EAttribute)getWindowEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetWindow_Class() {
		return (EAttribute)getWindowEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetView() {
		return getViewEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetEditor() {
		return getEditorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetSection() {
		return getSectionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetBanner() {
		return getBannerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetCoolbar() {
		return getCoolbarEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetColorSelector() {
		return getColorSelectorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetCell() {
		return getCellEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetCell_Row() {
		return (EAttribute)getCellEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetCell_Column() {
		return (EAttribute)getCellEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClick() {
		return clickEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getClick_Control() {
		return (EReference)clickEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClick_Nowait() {
		return (EAttribute)clickEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClick_Default() {
		return (EAttribute)clickEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClick_Arrow() {
		return (EAttribute)clickEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClick_MetaKeys() {
		return (EAttribute)clickEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDoubleClick() {
		return doubleClickEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetText() {
		return getTextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGetText_Control() {
		return (EReference)getTextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIsDisabled() {
		return isDisabledEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIsDisabled_Control() {
		return (EReference)isDisabledEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIsDisposed() {
		return isDisposedEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIsDisposed_Control() {
		return (EReference)isDisposedEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTypeText() {
		return typeTextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTypeText_Control() {
		return (EReference)typeTextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTypeText_Text() {
		return (EAttribute)typeTextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTypeText_Display() {
		return (EAttribute)typeTextEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getKeyType() {
		return keyTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getKeyType_Control() {
		return (EReference)keyTypeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getKeyType_Key() {
		return (EAttribute)keyTypeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getKeyType_Char() {
		return (EAttribute)keyTypeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getKeyType_Display() {
		return (EAttribute)keyTypeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getKeyType_Times() {
		return (EAttribute)keyTypeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTypeCommandKey() {
		return typeCommandKeyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTypeCommandKey_Control() {
		return (EReference)typeCommandKeyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTypeCommandKey_CommandId() {
		return (EAttribute)typeCommandKeyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetText() {
		return setTextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSetText_Control() {
		return (EReference)setTextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetText_Text() {
		return (EAttribute)setTextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetTextSelection() {
		return setTextSelectionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSetTextSelection_Control() {
		return (EReference)setTextSelectionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetTextSelection_Offset() {
		return (EAttribute)setTextSelectionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetTextSelection_Length() {
		return (EAttribute)setTextSelectionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetTextSelection_StartLine() {
		return (EAttribute)setTextSelectionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetTextSelection_EndLine() {
		return (EAttribute)setTextSelectionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetTextSelection_EndOffset() {
		return (EAttribute)setTextSelectionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetTextOffset() {
		return setTextOffsetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSetTextOffset_Control() {
		return (EReference)setTextOffsetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetTextOffset_Value() {
		return (EAttribute)setTextOffsetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetTextOffset_Line() {
		return (EAttribute)setTextOffsetEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCheck() {
		return checkEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCheck_Control() {
		return (EReference)checkEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getUncheck() {
		return uncheckEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getUncheck_Control() {
		return (EReference)uncheckEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSelect() {
		return selectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSelect_Control() {
		return (EReference)selectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelect_Items() {
		return (EAttribute)selectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelect_All() {
		return (EAttribute)selectEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelect_Column() {
		return (EAttribute)selectEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCellEdit() {
		return cellEditEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCellEdit_Control() {
		return (EReference)cellEditEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getActivateCellEdit() {
		return activateCellEditEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getActivateCellEdit_Column() {
		return (EAttribute)activateCellEditEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getActivateCellEdit_Type() {
		return (EAttribute)activateCellEditEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getActivateCellEdit_Button() {
		return (EAttribute)activateCellEditEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getApplyCellEdit() {
		return applyCellEditEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getApplyCellEdit_Deactivate() {
		return (EAttribute)applyCellEditEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCancelCellEdit() {
		return cancelCellEditEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDeactivateCellEdit() {
		return deactivateCellEditEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClose() {
		return closeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getClose_Control() {
		return (EReference)closeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReturnFromOsDialog() {
		return returnFromOsDialogEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReturnFromOsDialog_Kind() {
		return (EAttribute)returnFromOsDialogEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReturnFromOsDialog_Result() {
		return (EAttribute)returnFromOsDialogEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWaitUntilEclipseIsReady() {
		return waitUntilEclipseIsReadyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getShowContentAssist() {
		return showContentAssistEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getShowContentAssist_Control() {
		return (EReference)showContentAssistEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragAction() {
		return dragActionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDragAction_Control() {
		return (EReference)dragActionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDragAction_X() {
		return (EAttribute)dragActionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDragAction_Y() {
		return (EAttribute)dragActionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDragAction_Button() {
		return (EAttribute)dragActionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDragAction_Mask() {
		return (EAttribute)dragActionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDragAction_Detail() {
		return (EAttribute)dragActionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDragAction_Operation() {
		return (EAttribute)dragActionEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragStart() {
		return dragStartEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragEnd() {
		return dragEndEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragEnter() {
		return dragEnterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragExit() {
		return dragExitEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragSetData() {
		return dragSetDataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragAccept() {
		return dragAcceptEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragDetect() {
		return dragDetectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDragOver() {
		return dragOverEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDrop() {
		return dropEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRecognize() {
		return recognizeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRecognize_Image() {
		return (EAttribute)recognizeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRecognize_X() {
		return (EAttribute)recognizeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRecognize_Y() {
		return (EAttribute)recognizeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRecognize_Width() {
		return (EAttribute)recognizeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRecognize_Height() {
		return (EAttribute)recognizeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRecognizeResponse() {
		return recognizeResponseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRecognizeResponse_Text() {
		return (EAttribute)recognizeResponseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getContainsImage() {
		return containsImageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getContainsImage_Control() {
		return (EReference)containsImageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getContainsImage_ImageURI() {
		return (EAttribute)containsImageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getContainsImage_RawImage() {
		return (EAttribute)containsImageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetRegionText() {
		return getRegionTextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGetRegionText_Control() {
		return (EReference)getRegionTextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegionText_X() {
		return (EAttribute)getRegionTextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegionText_Y() {
		return (EAttribute)getRegionTextEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegionText_Sx() {
		return (EAttribute)getRegionTextEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegionText_Sy() {
		return (EAttribute)getRegionTextEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegionText_Width() {
		return (EAttribute)getRegionTextEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegionText_Height() {
		return (EAttribute)getRegionTextEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetRegion() {
		return getRegionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegion_X() {
		return (EAttribute)getRegionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegion_Y() {
		return (EAttribute)getRegionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegion_ImageURI() {
		return (EAttribute)getRegionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRegion_RawImage() {
		return (EAttribute)getRegionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetAdvancedInfo() {
		return getAdvancedInfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetAboutMenu() {
		return getAboutMenuEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetPreferencesMenu() {
		return getPreferencesMenuEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetDateTime() {
		return getDateTimeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetSlider() {
		return getSliderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetValue() {
		return setValueEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSetValue_Control() {
		return (EReference)setValueEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetValue_Value() {
		return (EAttribute)setValueEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMinimize() {
		return minimizeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMinimize_Control() {
		return (EReference)minimizeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMaximize() {
		return maximizeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMaximize_Control() {
		return (EReference)maximizeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRestore() {
		return restoreEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRestore_Control() {
		return (EReference)restoreEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getShowTabList() {
		return showTabListEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getShowTabList_Control() {
		return (EReference)showTabListEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOptions() {
		return optionsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOptions_AllowStatusDialog() {
		return (EAttribute)optionsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOptions_Command() {
		return (EReference)optionsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExecWithOptions() {
		return execWithOptionsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getExecWithOptions_Command() {
		return (EReference)execWithOptionsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExecWithOptions_AllowStatusDialog() {
		return (EAttribute)execWithOptionsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExecWithOptions_DisableJobWaiting() {
		return (EAttribute)execWithOptionsEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getShutdownAut() {
		return shutdownAutEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetDialogResult() {
		return setDialogResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetDialogResult_Kind() {
		return (EAttribute)setDialogResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetDialogResult_Result() {
		return (EAttribute)setDialogResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetPropertyNodes() {
		return getPropertyNodesEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGetPropertyNodes_Element() {
		return (EReference)getPropertyNodesEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetPropertyNodes_NodePath() {
		return (EAttribute)getPropertyNodesEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetByOs() {
		return getByOsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetByOs_Default() {
		return (EAttribute)getByOsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetByOs_Win() {
		return (EAttribute)getByOsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetByOs_Linux() {
		return (EAttribute)getByOsEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetByOs_Macosx() {
		return (EAttribute)getByOsEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFromRawKey() {
		return fromRawKeyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFromRawKey_Mask() {
		return (EAttribute)fromRawKeyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFromRawKey_KeyCode() {
		return (EAttribute)fromRawKeyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFromRawKey_Meta() {
		return (EAttribute)fromRawKeyEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHoverAtTextOffset() {
		return hoverAtTextOffsetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getHoverAtTextOffset_Control() {
		return (EReference)hoverAtTextOffsetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHoverAtTextOffset_Offset() {
		return (EAttribute)hoverAtTextOffsetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHoverAtTextOffset_Line() {
		return (EAttribute)hoverAtTextOffsetEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetTextViewer() {
		return getTextViewerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSelectRange() {
		return selectRangeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSelectRange_Control() {
		return (EReference)selectRangeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelectRange_Line() {
		return (EAttribute)selectRangeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelectRange_Column() {
		return (EAttribute)selectRangeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelectRange_EndLine() {
		return (EAttribute)selectRangeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSelectRange_EndColumn() {
		return (EAttribute)selectRangeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetCaretPos() {
		return setCaretPosEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSetCaretPos_Control() {
		return (EReference)setCaretPosEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetCaretPos_Line() {
		return (EAttribute)setCaretPosEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetCaretPos_Column() {
		return (EAttribute)setCaretPosEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHoverText() {
		return hoverTextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getHoverText_Control() {
		return (EReference)hoverTextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHoverText_Line() {
		return (EAttribute)hoverTextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHoverText_Column() {
		return (EAttribute)hoverTextEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHoverText_With() {
		return (EAttribute)hoverTextEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOpenDeclaration() {
		return openDeclarationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOpenDeclaration_Control() {
		return (EReference)openDeclarationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetVerticalRuler() {
		return getVerticalRulerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetLeftRuler() {
		return getLeftRulerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetRulerColumn() {
		return getRulerColumnEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetRulerColumn_Text() {
		return (EAttribute)getRulerColumnEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetRightRuler() {
		return getRightRulerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClickRuler() {
		return clickRulerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getClickRuler_Control() {
		return (EReference)clickRulerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickRuler_Line() {
		return (EAttribute)clickRulerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickRuler_Button() {
		return (EAttribute)clickRulerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickRuler_With() {
		return (EAttribute)clickRulerEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDoubleClickRuler() {
		return doubleClickRulerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDoubleClickRuler_Control() {
		return (EReference)doubleClickRulerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDoubleClickRuler_Line() {
		return (EAttribute)doubleClickRulerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDoubleClickRuler_Button() {
		return (EAttribute)doubleClickRulerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDoubleClickRuler_With() {
		return (EAttribute)doubleClickRulerEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHoverRuler() {
		return hoverRulerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getHoverRuler_Control() {
		return (EReference)hoverRulerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHoverRuler_Line() {
		return (EAttribute)hoverRulerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHoverRuler_With() {
		return (EAttribute)hoverRulerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getControlCommand() {
		return controlCommandEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getControlCommand_Control() {
		return (EReference)controlCommandEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClickLink() {
		return clickLinkEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickLink_Ref() {
		return (EAttribute)clickLinkEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExpand() {
		return expandEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCollapse() {
		return collapseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetFocus() {
		return setFocusEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetTableData() {
		return getTableDataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetTableData_IncludeChecked() {
		return (EAttribute)getTableDataEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetTableData_ExcludeHidden() {
		return (EAttribute)getTableDataEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClickColumn() {
		return clickColumnEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickColumn_Name() {
		return (EAttribute)clickColumnEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickColumn_Index() {
		return (EAttribute)clickColumnEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTrace() {
		return traceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTrace_Message() {
		return (EAttribute)traceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMouse() {
		return mouseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouse_Event() {
		return (EAttribute)mouseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouse_Button() {
		return (EAttribute)mouseEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouse_With() {
		return (EAttribute)mouseEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouse_Count() {
		return (EAttribute)mouseEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouse_X() {
		return (EAttribute)mouseEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMouse_Y() {
		return (EAttribute)mouseEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetObject() {
		return getObjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGetObject_Object() {
		return (EReference)getObjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetItems() {
		return getItemsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExpandAll() {
		return expandAllEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCollapseAll() {
		return collapseAllEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTakeScreenshot() {
		return takeScreenshotEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTakeScreenshot_Message() {
		return (EAttribute)takeScreenshotEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSelectItem() {
		return selectItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetWidgetDetails() {
		return getWidgetDetailsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGetWidgetDetails_Element() {
		return (EReference)getWidgetDetailsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClickText() {
		return clickTextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickText_Start() {
		return (EAttribute)clickTextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickText_End() {
		return (EAttribute)clickTextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClickText_Button() {
		return (EAttribute)clickTextEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetQuickAccess() {
		return getQuickAccessEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetColumnHeader() {
		return getColumnHeaderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetPosition() {
		return setPositionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetPosition_Index() {
		return (EAttribute)setPositionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSetWidth() {
		return setWidthEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSetWidth_Width() {
		return (EAttribute)setWidthEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetPropertyTab() {
		return getPropertyTabEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getShowAlert() {
		return showAlertEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getShowAlert_Message() {
		return (EAttribute)showAlertEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDoubleClickText() {
		return doubleClickTextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDoubleClickText_Position() {
		return (EAttribute)doubleClickTextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDoubleClickText_Button() {
		return (EAttribute)doubleClickTextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getToControlHandle() {
		return toControlHandleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getToControlHandle_Widget() {
		return (EAttribute)toControlHandleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBoundControlHandle() {
		return boundControlHandleEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBoundControlHandle_WidgetId() {
		return (EAttribute)boundControlHandleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getUnfocus() {
		return unfocusEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDecrypt() {
		return decryptEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDecrypt_Value() {
		return (EAttribute)decryptEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDecryptResult() {
		return decryptResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDecryptResult_Value() {
		return (EAttribute)decryptResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRestartAut() {
		return restartAutEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMessageBoxInfo() {
		return messageBoxInfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMessageBoxInfo_Title() {
		return (EAttribute)messageBoxInfoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMessageBoxInfo_Message() {
		return (EAttribute)messageBoxInfoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetLastMessageBox() {
		return getLastMessageBoxEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCheckDownloadResult() {
		return checkDownloadResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCheckDownloadResult_ContentOnBase64() {
		return (EAttribute)checkDownloadResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCheckDownloadResult_FileName() {
		return (EAttribute)checkDownloadResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetRuntimeTarget() {
		return getRuntimeTargetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetTestCaseName() {
		return getTestCaseNameEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetComboItems() {
		return getComboItemsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getButton() {
		return buttonEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getActivationEventType() {
		return activationEventTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getElementKind() {
		return elementKindEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TeslaFactory getTeslaFactory() {
		return (TeslaFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		waitEClass = createEClass(WAIT);
		createEAttribute(waitEClass, WAIT__MS);

		setupPlayerEClass = createEClass(SETUP_PLAYER);

		shoutdownPlayerEClass = createEClass(SHOUTDOWN_PLAYER);

		teslaProcessStatusEClass = createEClass(TESLA_PROCESS_STATUS);
		createEReference(teslaProcessStatusEClass, TESLA_PROCESS_STATUS__INFO);

		wrapperEClass = createEClass(WRAPPER);
		createEAttribute(wrapperEClass, WRAPPER__OBJECT);

		getPropertyEClass = createEClass(GET_PROPERTY);
		createEReference(getPropertyEClass, GET_PROPERTY__OBJECT);
		createEAttribute(getPropertyEClass, GET_PROPERTY__NAME);
		createEAttribute(getPropertyEClass, GET_PROPERTY__INDEX);
		createEAttribute(getPropertyEClass, GET_PROPERTY__RAW);

		verifyTrueEClass = createEClass(VERIFY_TRUE);
		createEReference(verifyTrueEClass, VERIFY_TRUE__CONDITION);

		verifyFalseEClass = createEClass(VERIFY_FALSE);
		createEReference(verifyFalseEClass, VERIFY_FALSE__CONDITION);

		verifyHandlerEClass = createEClass(VERIFY_HANDLER);
		createEReference(verifyHandlerEClass, VERIFY_HANDLER__ELEMENT);
		createEAttribute(verifyHandlerEClass, VERIFY_HANDLER__ATTRIBUTE);
		createEAttribute(verifyHandlerEClass, VERIFY_HANDLER__VALUE);
		createEAttribute(verifyHandlerEClass, VERIFY_HANDLER__KIND);
		createEAttribute(verifyHandlerEClass, VERIFY_HANDLER__INDEX);

		verifyErrorEClass = createEClass(VERIFY_ERROR);
		createEReference(verifyErrorEClass, VERIFY_ERROR__COMMAND);

		equalsEClass = createEClass(EQUALS);
		createEReference(equalsEClass, EQUALS__INPUT);
		createEAttribute(equalsEClass, EQUALS__VALUE);

		containsEClass = createEClass(CONTAINS);
		createEReference(containsEClass, CONTAINS__INPUT);
		createEAttribute(containsEClass, CONTAINS__VALUE);

		matchesEClass = createEClass(MATCHES);
		createEReference(matchesEClass, MATCHES__INPUT);
		createEAttribute(matchesEClass, MATCHES__VALUE);

		isEmptyEClass = createEClass(IS_EMPTY);
		createEReference(isEmptyEClass, IS_EMPTY__INPUT);

		unsupportedEClass = createEClass(UNSUPPORTED);
		createEAttribute(unsupportedEClass, UNSUPPORTED__DESC);

		controlNotFoundEClass = createEClass(CONTROL_NOT_FOUND);
		createEAttribute(controlNotFoundEClass, CONTROL_NOT_FOUND__ID);

		controlHandlerEClass = createEClass(CONTROL_HANDLER);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__KIND);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__CUSTOM_KIND_ID);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__TEXT);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__TYPE);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__INDEX);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__PATH);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__INDEXES);
		createEReference(controlHandlerEClass, CONTROL_HANDLER__AFTER);
		createEReference(controlHandlerEClass, CONTROL_HANDLER__PARENT);
		createEReference(controlHandlerEClass, CONTROL_HANDLER__RESOLVED);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__ROW);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__COLUMN);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__X);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__Y);
		createEAttribute(controlHandlerEClass, CONTROL_HANDLER__RAW_IMAGE);

		getEclipseWindowEClass = createEClass(GET_ECLIPSE_WINDOW);

		selectorEClass = createEClass(SELECTOR);
		createEAttribute(selectorEClass, SELECTOR__ID);
		createEReference(selectorEClass, SELECTOR__AFTER);
		createEAttribute(selectorEClass, SELECTOR__TYPE);
		createEAttribute(selectorEClass, SELECTOR__INDEX);
		createEReference(selectorEClass, SELECTOR__PARENT);

		pathSelectorEClass = createEClass(PATH_SELECTOR);
		createEAttribute(pathSelectorEClass, PATH_SELECTOR__PATH);

		textSelectorEClass = createEClass(TEXT_SELECTOR);
		createEAttribute(textSelectorEClass, TEXT_SELECTOR__TEXT);

		getControlEClass = createEClass(GET_CONTROL);
		createEAttribute(getControlEClass, GET_CONTROL__KIND);

		getButtonEClass = createEClass(GET_BUTTON);

		getCanvasEClass = createEClass(GET_CANVAS);

		getCheckboxEClass = createEClass(GET_CHECKBOX);

		getComboEClass = createEClass(GET_COMBO);

		getEditboxEClass = createEClass(GET_EDITBOX);

		getGroupEClass = createEClass(GET_GROUP);

		getItemEClass = createEClass(GET_ITEM);
		createEAttribute(getItemEClass, GET_ITEM__PATH);
		createEAttribute(getItemEClass, GET_ITEM__COLUMN);

		getLabelEClass = createEClass(GET_LABEL);

		getLinkEClass = createEClass(GET_LINK);

		getListEClass = createEClass(GET_LIST);

		getMenuEClass = createEClass(GET_MENU);

		getTabFolderEClass = createEClass(GET_TAB_FOLDER);

		getTabItemEClass = createEClass(GET_TAB_ITEM);

		getTableEClass = createEClass(GET_TABLE);

		getToolbarEClass = createEClass(GET_TOOLBAR);

		getTreeEClass = createEClass(GET_TREE);

		getWindowEClass = createEClass(GET_WINDOW);
		createEAttribute(getWindowEClass, GET_WINDOW__TEXT);
		createEAttribute(getWindowEClass, GET_WINDOW__FROM);
		createEAttribute(getWindowEClass, GET_WINDOW__CLASS);

		getViewEClass = createEClass(GET_VIEW);

		getEditorEClass = createEClass(GET_EDITOR);

		getSectionEClass = createEClass(GET_SECTION);

		getBannerEClass = createEClass(GET_BANNER);

		getCoolbarEClass = createEClass(GET_COOLBAR);

		getColorSelectorEClass = createEClass(GET_COLOR_SELECTOR);

		getCellEClass = createEClass(GET_CELL);
		createEAttribute(getCellEClass, GET_CELL__ROW);
		createEAttribute(getCellEClass, GET_CELL__COLUMN);

		clickEClass = createEClass(CLICK);
		createEReference(clickEClass, CLICK__CONTROL);
		createEAttribute(clickEClass, CLICK__NOWAIT);
		createEAttribute(clickEClass, CLICK__DEFAULT);
		createEAttribute(clickEClass, CLICK__ARROW);
		createEAttribute(clickEClass, CLICK__META_KEYS);

		doubleClickEClass = createEClass(DOUBLE_CLICK);

		getTextEClass = createEClass(GET_TEXT);
		createEReference(getTextEClass, GET_TEXT__CONTROL);

		isDisabledEClass = createEClass(IS_DISABLED);
		createEReference(isDisabledEClass, IS_DISABLED__CONTROL);

		isDisposedEClass = createEClass(IS_DISPOSED);
		createEReference(isDisposedEClass, IS_DISPOSED__CONTROL);

		typeTextEClass = createEClass(TYPE_TEXT);
		createEReference(typeTextEClass, TYPE_TEXT__CONTROL);
		createEAttribute(typeTextEClass, TYPE_TEXT__TEXT);
		createEAttribute(typeTextEClass, TYPE_TEXT__DISPLAY);

		keyTypeEClass = createEClass(KEY_TYPE);
		createEReference(keyTypeEClass, KEY_TYPE__CONTROL);
		createEAttribute(keyTypeEClass, KEY_TYPE__KEY);
		createEAttribute(keyTypeEClass, KEY_TYPE__CHAR);
		createEAttribute(keyTypeEClass, KEY_TYPE__DISPLAY);
		createEAttribute(keyTypeEClass, KEY_TYPE__TIMES);

		typeCommandKeyEClass = createEClass(TYPE_COMMAND_KEY);
		createEReference(typeCommandKeyEClass, TYPE_COMMAND_KEY__CONTROL);
		createEAttribute(typeCommandKeyEClass, TYPE_COMMAND_KEY__COMMAND_ID);

		setTextEClass = createEClass(SET_TEXT);
		createEReference(setTextEClass, SET_TEXT__CONTROL);
		createEAttribute(setTextEClass, SET_TEXT__TEXT);

		setTextSelectionEClass = createEClass(SET_TEXT_SELECTION);
		createEReference(setTextSelectionEClass, SET_TEXT_SELECTION__CONTROL);
		createEAttribute(setTextSelectionEClass, SET_TEXT_SELECTION__OFFSET);
		createEAttribute(setTextSelectionEClass, SET_TEXT_SELECTION__LENGTH);
		createEAttribute(setTextSelectionEClass, SET_TEXT_SELECTION__START_LINE);
		createEAttribute(setTextSelectionEClass, SET_TEXT_SELECTION__END_LINE);
		createEAttribute(setTextSelectionEClass, SET_TEXT_SELECTION__END_OFFSET);

		setTextOffsetEClass = createEClass(SET_TEXT_OFFSET);
		createEReference(setTextOffsetEClass, SET_TEXT_OFFSET__CONTROL);
		createEAttribute(setTextOffsetEClass, SET_TEXT_OFFSET__VALUE);
		createEAttribute(setTextOffsetEClass, SET_TEXT_OFFSET__LINE);

		checkEClass = createEClass(CHECK);
		createEReference(checkEClass, CHECK__CONTROL);

		uncheckEClass = createEClass(UNCHECK);
		createEReference(uncheckEClass, UNCHECK__CONTROL);

		selectEClass = createEClass(SELECT);
		createEReference(selectEClass, SELECT__CONTROL);
		createEAttribute(selectEClass, SELECT__ITEMS);
		createEAttribute(selectEClass, SELECT__ALL);
		createEAttribute(selectEClass, SELECT__COLUMN);

		cellEditEClass = createEClass(CELL_EDIT);
		createEReference(cellEditEClass, CELL_EDIT__CONTROL);

		activateCellEditEClass = createEClass(ACTIVATE_CELL_EDIT);
		createEAttribute(activateCellEditEClass, ACTIVATE_CELL_EDIT__COLUMN);
		createEAttribute(activateCellEditEClass, ACTIVATE_CELL_EDIT__TYPE);
		createEAttribute(activateCellEditEClass, ACTIVATE_CELL_EDIT__BUTTON);

		applyCellEditEClass = createEClass(APPLY_CELL_EDIT);
		createEAttribute(applyCellEditEClass, APPLY_CELL_EDIT__DEACTIVATE);

		cancelCellEditEClass = createEClass(CANCEL_CELL_EDIT);

		deactivateCellEditEClass = createEClass(DEACTIVATE_CELL_EDIT);

		closeEClass = createEClass(CLOSE);
		createEReference(closeEClass, CLOSE__CONTROL);

		returnFromOsDialogEClass = createEClass(RETURN_FROM_OS_DIALOG);
		createEAttribute(returnFromOsDialogEClass, RETURN_FROM_OS_DIALOG__KIND);
		createEAttribute(returnFromOsDialogEClass, RETURN_FROM_OS_DIALOG__RESULT);

		waitUntilEclipseIsReadyEClass = createEClass(WAIT_UNTIL_ECLIPSE_IS_READY);

		showContentAssistEClass = createEClass(SHOW_CONTENT_ASSIST);
		createEReference(showContentAssistEClass, SHOW_CONTENT_ASSIST__CONTROL);

		dragActionEClass = createEClass(DRAG_ACTION);
		createEReference(dragActionEClass, DRAG_ACTION__CONTROL);
		createEAttribute(dragActionEClass, DRAG_ACTION__X);
		createEAttribute(dragActionEClass, DRAG_ACTION__Y);
		createEAttribute(dragActionEClass, DRAG_ACTION__BUTTON);
		createEAttribute(dragActionEClass, DRAG_ACTION__MASK);
		createEAttribute(dragActionEClass, DRAG_ACTION__DETAIL);
		createEAttribute(dragActionEClass, DRAG_ACTION__OPERATION);

		dragStartEClass = createEClass(DRAG_START);

		dragEndEClass = createEClass(DRAG_END);

		dragEnterEClass = createEClass(DRAG_ENTER);

		dragExitEClass = createEClass(DRAG_EXIT);

		dragSetDataEClass = createEClass(DRAG_SET_DATA);

		dragAcceptEClass = createEClass(DRAG_ACCEPT);

		dragDetectEClass = createEClass(DRAG_DETECT);

		dragOverEClass = createEClass(DRAG_OVER);

		dropEClass = createEClass(DROP);

		recognizeEClass = createEClass(RECOGNIZE);
		createEAttribute(recognizeEClass, RECOGNIZE__IMAGE);
		createEAttribute(recognizeEClass, RECOGNIZE__X);
		createEAttribute(recognizeEClass, RECOGNIZE__Y);
		createEAttribute(recognizeEClass, RECOGNIZE__WIDTH);
		createEAttribute(recognizeEClass, RECOGNIZE__HEIGHT);

		recognizeResponseEClass = createEClass(RECOGNIZE_RESPONSE);
		createEAttribute(recognizeResponseEClass, RECOGNIZE_RESPONSE__TEXT);

		containsImageEClass = createEClass(CONTAINS_IMAGE);
		createEReference(containsImageEClass, CONTAINS_IMAGE__CONTROL);
		createEAttribute(containsImageEClass, CONTAINS_IMAGE__IMAGE_URI);
		createEAttribute(containsImageEClass, CONTAINS_IMAGE__RAW_IMAGE);

		getRegionTextEClass = createEClass(GET_REGION_TEXT);
		createEReference(getRegionTextEClass, GET_REGION_TEXT__CONTROL);
		createEAttribute(getRegionTextEClass, GET_REGION_TEXT__X);
		createEAttribute(getRegionTextEClass, GET_REGION_TEXT__Y);
		createEAttribute(getRegionTextEClass, GET_REGION_TEXT__SX);
		createEAttribute(getRegionTextEClass, GET_REGION_TEXT__SY);
		createEAttribute(getRegionTextEClass, GET_REGION_TEXT__WIDTH);
		createEAttribute(getRegionTextEClass, GET_REGION_TEXT__HEIGHT);

		getRegionEClass = createEClass(GET_REGION);
		createEAttribute(getRegionEClass, GET_REGION__X);
		createEAttribute(getRegionEClass, GET_REGION__Y);
		createEAttribute(getRegionEClass, GET_REGION__IMAGE_URI);
		createEAttribute(getRegionEClass, GET_REGION__RAW_IMAGE);

		getAdvancedInfoEClass = createEClass(GET_ADVANCED_INFO);

		getAboutMenuEClass = createEClass(GET_ABOUT_MENU);

		getPreferencesMenuEClass = createEClass(GET_PREFERENCES_MENU);

		getDateTimeEClass = createEClass(GET_DATE_TIME);

		getSliderEClass = createEClass(GET_SLIDER);

		setValueEClass = createEClass(SET_VALUE);
		createEReference(setValueEClass, SET_VALUE__CONTROL);
		createEAttribute(setValueEClass, SET_VALUE__VALUE);

		minimizeEClass = createEClass(MINIMIZE);
		createEReference(minimizeEClass, MINIMIZE__CONTROL);

		maximizeEClass = createEClass(MAXIMIZE);
		createEReference(maximizeEClass, MAXIMIZE__CONTROL);

		restoreEClass = createEClass(RESTORE);
		createEReference(restoreEClass, RESTORE__CONTROL);

		showTabListEClass = createEClass(SHOW_TAB_LIST);
		createEReference(showTabListEClass, SHOW_TAB_LIST__CONTROL);

		optionsEClass = createEClass(OPTIONS);
		createEAttribute(optionsEClass, OPTIONS__ALLOW_STATUS_DIALOG);
		createEReference(optionsEClass, OPTIONS__COMMAND);

		execWithOptionsEClass = createEClass(EXEC_WITH_OPTIONS);
		createEReference(execWithOptionsEClass, EXEC_WITH_OPTIONS__COMMAND);
		createEAttribute(execWithOptionsEClass, EXEC_WITH_OPTIONS__ALLOW_STATUS_DIALOG);
		createEAttribute(execWithOptionsEClass, EXEC_WITH_OPTIONS__DISABLE_JOB_WAITING);

		shutdownAutEClass = createEClass(SHUTDOWN_AUT);

		setDialogResultEClass = createEClass(SET_DIALOG_RESULT);
		createEAttribute(setDialogResultEClass, SET_DIALOG_RESULT__KIND);
		createEAttribute(setDialogResultEClass, SET_DIALOG_RESULT__RESULT);

		getPropertyNodesEClass = createEClass(GET_PROPERTY_NODES);
		createEReference(getPropertyNodesEClass, GET_PROPERTY_NODES__ELEMENT);
		createEAttribute(getPropertyNodesEClass, GET_PROPERTY_NODES__NODE_PATH);

		getByOsEClass = createEClass(GET_BY_OS);
		createEAttribute(getByOsEClass, GET_BY_OS__DEFAULT);
		createEAttribute(getByOsEClass, GET_BY_OS__WIN);
		createEAttribute(getByOsEClass, GET_BY_OS__LINUX);
		createEAttribute(getByOsEClass, GET_BY_OS__MACOSX);

		fromRawKeyEClass = createEClass(FROM_RAW_KEY);
		createEAttribute(fromRawKeyEClass, FROM_RAW_KEY__MASK);
		createEAttribute(fromRawKeyEClass, FROM_RAW_KEY__KEY_CODE);
		createEAttribute(fromRawKeyEClass, FROM_RAW_KEY__META);

		hoverAtTextOffsetEClass = createEClass(HOVER_AT_TEXT_OFFSET);
		createEReference(hoverAtTextOffsetEClass, HOVER_AT_TEXT_OFFSET__CONTROL);
		createEAttribute(hoverAtTextOffsetEClass, HOVER_AT_TEXT_OFFSET__OFFSET);
		createEAttribute(hoverAtTextOffsetEClass, HOVER_AT_TEXT_OFFSET__LINE);

		getTextViewerEClass = createEClass(GET_TEXT_VIEWER);

		selectRangeEClass = createEClass(SELECT_RANGE);
		createEReference(selectRangeEClass, SELECT_RANGE__CONTROL);
		createEAttribute(selectRangeEClass, SELECT_RANGE__LINE);
		createEAttribute(selectRangeEClass, SELECT_RANGE__COLUMN);
		createEAttribute(selectRangeEClass, SELECT_RANGE__END_LINE);
		createEAttribute(selectRangeEClass, SELECT_RANGE__END_COLUMN);

		setCaretPosEClass = createEClass(SET_CARET_POS);
		createEReference(setCaretPosEClass, SET_CARET_POS__CONTROL);
		createEAttribute(setCaretPosEClass, SET_CARET_POS__LINE);
		createEAttribute(setCaretPosEClass, SET_CARET_POS__COLUMN);

		hoverTextEClass = createEClass(HOVER_TEXT);
		createEReference(hoverTextEClass, HOVER_TEXT__CONTROL);
		createEAttribute(hoverTextEClass, HOVER_TEXT__LINE);
		createEAttribute(hoverTextEClass, HOVER_TEXT__COLUMN);
		createEAttribute(hoverTextEClass, HOVER_TEXT__WITH);

		openDeclarationEClass = createEClass(OPEN_DECLARATION);
		createEReference(openDeclarationEClass, OPEN_DECLARATION__CONTROL);

		getVerticalRulerEClass = createEClass(GET_VERTICAL_RULER);

		getLeftRulerEClass = createEClass(GET_LEFT_RULER);

		getRulerColumnEClass = createEClass(GET_RULER_COLUMN);
		createEAttribute(getRulerColumnEClass, GET_RULER_COLUMN__TEXT);

		getRightRulerEClass = createEClass(GET_RIGHT_RULER);

		clickRulerEClass = createEClass(CLICK_RULER);
		createEReference(clickRulerEClass, CLICK_RULER__CONTROL);
		createEAttribute(clickRulerEClass, CLICK_RULER__LINE);
		createEAttribute(clickRulerEClass, CLICK_RULER__BUTTON);
		createEAttribute(clickRulerEClass, CLICK_RULER__WITH);

		doubleClickRulerEClass = createEClass(DOUBLE_CLICK_RULER);
		createEReference(doubleClickRulerEClass, DOUBLE_CLICK_RULER__CONTROL);
		createEAttribute(doubleClickRulerEClass, DOUBLE_CLICK_RULER__LINE);
		createEAttribute(doubleClickRulerEClass, DOUBLE_CLICK_RULER__BUTTON);
		createEAttribute(doubleClickRulerEClass, DOUBLE_CLICK_RULER__WITH);

		hoverRulerEClass = createEClass(HOVER_RULER);
		createEReference(hoverRulerEClass, HOVER_RULER__CONTROL);
		createEAttribute(hoverRulerEClass, HOVER_RULER__LINE);
		createEAttribute(hoverRulerEClass, HOVER_RULER__WITH);

		controlCommandEClass = createEClass(CONTROL_COMMAND);
		createEReference(controlCommandEClass, CONTROL_COMMAND__CONTROL);

		clickLinkEClass = createEClass(CLICK_LINK);
		createEAttribute(clickLinkEClass, CLICK_LINK__REF);

		expandEClass = createEClass(EXPAND);

		collapseEClass = createEClass(COLLAPSE);

		setFocusEClass = createEClass(SET_FOCUS);

		getTableDataEClass = createEClass(GET_TABLE_DATA);
		createEAttribute(getTableDataEClass, GET_TABLE_DATA__INCLUDE_CHECKED);
		createEAttribute(getTableDataEClass, GET_TABLE_DATA__EXCLUDE_HIDDEN);

		clickColumnEClass = createEClass(CLICK_COLUMN);
		createEAttribute(clickColumnEClass, CLICK_COLUMN__NAME);
		createEAttribute(clickColumnEClass, CLICK_COLUMN__INDEX);

		traceEClass = createEClass(TRACE);
		createEAttribute(traceEClass, TRACE__MESSAGE);

		mouseEClass = createEClass(MOUSE);
		createEAttribute(mouseEClass, MOUSE__EVENT);
		createEAttribute(mouseEClass, MOUSE__BUTTON);
		createEAttribute(mouseEClass, MOUSE__WITH);
		createEAttribute(mouseEClass, MOUSE__COUNT);
		createEAttribute(mouseEClass, MOUSE__X);
		createEAttribute(mouseEClass, MOUSE__Y);

		getObjectEClass = createEClass(GET_OBJECT);
		createEReference(getObjectEClass, GET_OBJECT__OBJECT);

		getItemsEClass = createEClass(GET_ITEMS);

		expandAllEClass = createEClass(EXPAND_ALL);

		collapseAllEClass = createEClass(COLLAPSE_ALL);

		takeScreenshotEClass = createEClass(TAKE_SCREENSHOT);
		createEAttribute(takeScreenshotEClass, TAKE_SCREENSHOT__MESSAGE);

		selectItemEClass = createEClass(SELECT_ITEM);

		getWidgetDetailsEClass = createEClass(GET_WIDGET_DETAILS);
		createEReference(getWidgetDetailsEClass, GET_WIDGET_DETAILS__ELEMENT);

		clickTextEClass = createEClass(CLICK_TEXT);
		createEAttribute(clickTextEClass, CLICK_TEXT__START);
		createEAttribute(clickTextEClass, CLICK_TEXT__END);
		createEAttribute(clickTextEClass, CLICK_TEXT__BUTTON);

		getQuickAccessEClass = createEClass(GET_QUICK_ACCESS);

		getColumnHeaderEClass = createEClass(GET_COLUMN_HEADER);

		setPositionEClass = createEClass(SET_POSITION);
		createEAttribute(setPositionEClass, SET_POSITION__INDEX);

		setWidthEClass = createEClass(SET_WIDTH);
		createEAttribute(setWidthEClass, SET_WIDTH__WIDTH);

		getPropertyTabEClass = createEClass(GET_PROPERTY_TAB);

		showAlertEClass = createEClass(SHOW_ALERT);
		createEAttribute(showAlertEClass, SHOW_ALERT__MESSAGE);

		doubleClickTextEClass = createEClass(DOUBLE_CLICK_TEXT);
		createEAttribute(doubleClickTextEClass, DOUBLE_CLICK_TEXT__POSITION);
		createEAttribute(doubleClickTextEClass, DOUBLE_CLICK_TEXT__BUTTON);

		toControlHandleEClass = createEClass(TO_CONTROL_HANDLE);
		createEAttribute(toControlHandleEClass, TO_CONTROL_HANDLE__WIDGET);

		boundControlHandleEClass = createEClass(BOUND_CONTROL_HANDLE);
		createEAttribute(boundControlHandleEClass, BOUND_CONTROL_HANDLE__WIDGET_ID);

		unfocusEClass = createEClass(UNFOCUS);

		decryptEClass = createEClass(DECRYPT);
		createEAttribute(decryptEClass, DECRYPT__VALUE);

		decryptResultEClass = createEClass(DECRYPT_RESULT);
		createEAttribute(decryptResultEClass, DECRYPT_RESULT__VALUE);

		restartAutEClass = createEClass(RESTART_AUT);

		messageBoxInfoEClass = createEClass(MESSAGE_BOX_INFO);
		createEAttribute(messageBoxInfoEClass, MESSAGE_BOX_INFO__TITLE);
		createEAttribute(messageBoxInfoEClass, MESSAGE_BOX_INFO__MESSAGE);

		getLastMessageBoxEClass = createEClass(GET_LAST_MESSAGE_BOX);

		checkDownloadResultEClass = createEClass(CHECK_DOWNLOAD_RESULT);
		createEAttribute(checkDownloadResultEClass, CHECK_DOWNLOAD_RESULT__CONTENT_ON_BASE64);
		createEAttribute(checkDownloadResultEClass, CHECK_DOWNLOAD_RESULT__FILE_NAME);

		getRuntimeTargetEClass = createEClass(GET_RUNTIME_TARGET);

		getTestCaseNameEClass = createEClass(GET_TEST_CASE_NAME);

		getComboItemsEClass = createEClass(GET_COMBO_ITEMS);

		// Create enums
		buttonEEnum = createEEnum(BUTTON);
		activationEventTypeEEnum = createEEnum(ACTIVATION_EVENT_TYPE);

		// Create data types
		elementKindEDataType = createEDataType(ELEMENT_KIND);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		DiagramPackage theDiagramPackage = (DiagramPackage)EPackage.Registry.INSTANCE.getEPackage(DiagramPackage.eNS_URI);
		CorePackage theCorePackage = (CorePackage)EPackage.Registry.INSTANCE.getEPackage(CorePackage.eNS_URI);
		InfoPackage theInfoPackage = (InfoPackage)EPackage.Registry.INSTANCE.getEPackage(InfoPackage.eNS_URI);
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);
		RawPackage theRawPackage = (RawPackage)EPackage.Registry.INSTANCE.getEPackage(RawPackage.eNS_URI);
		ProtocolPackage theProtocolPackage = (ProtocolPackage)EPackage.Registry.INSTANCE.getEPackage(ProtocolPackage.eNS_URI);

		// Add subpackages
		getESubpackages().add(theDiagramPackage);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		waitEClass.getESuperTypes().add(theCorePackage.getCommand());
		setupPlayerEClass.getESuperTypes().add(theCorePackage.getCommand());
		shoutdownPlayerEClass.getESuperTypes().add(theCorePackage.getCommand());
		teslaProcessStatusEClass.getESuperTypes().add(theCorePackage.getProcessStatus());
		getPropertyEClass.getESuperTypes().add(theCorePackage.getCommand());
		verifyTrueEClass.getESuperTypes().add(theCorePackage.getCommand());
		verifyFalseEClass.getESuperTypes().add(theCorePackage.getCommand());
		verifyErrorEClass.getESuperTypes().add(theCorePackage.getCommand());
		equalsEClass.getESuperTypes().add(theCorePackage.getCommand());
		containsEClass.getESuperTypes().add(theCorePackage.getCommand());
		matchesEClass.getESuperTypes().add(theCorePackage.getCommand());
		isEmptyEClass.getESuperTypes().add(theCorePackage.getCommand());
		unsupportedEClass.getESuperTypes().add(theCorePackage.getCommand());
		controlNotFoundEClass.getESuperTypes().add(theCorePackage.getCommand());
		getEclipseWindowEClass.getESuperTypes().add(this.getSelector());
		selectorEClass.getESuperTypes().add(theCorePackage.getCommand());
		pathSelectorEClass.getESuperTypes().add(this.getSelector());
		textSelectorEClass.getESuperTypes().add(this.getSelector());
		getControlEClass.getESuperTypes().add(this.getTextSelector());
		getControlEClass.getESuperTypes().add(this.getPathSelector());
		getButtonEClass.getESuperTypes().add(this.getTextSelector());
		getCanvasEClass.getESuperTypes().add(this.getSelector());
		getCheckboxEClass.getESuperTypes().add(this.getTextSelector());
		getComboEClass.getESuperTypes().add(this.getSelector());
		getEditboxEClass.getESuperTypes().add(this.getSelector());
		getGroupEClass.getESuperTypes().add(this.getTextSelector());
		getItemEClass.getESuperTypes().add(this.getSelector());
		getLabelEClass.getESuperTypes().add(this.getTextSelector());
		getLinkEClass.getESuperTypes().add(this.getTextSelector());
		getListEClass.getESuperTypes().add(this.getSelector());
		getMenuEClass.getESuperTypes().add(this.getPathSelector());
		getTabFolderEClass.getESuperTypes().add(this.getSelector());
		getTabItemEClass.getESuperTypes().add(this.getTextSelector());
		getTableEClass.getESuperTypes().add(this.getSelector());
		getToolbarEClass.getESuperTypes().add(this.getSelector());
		getTreeEClass.getESuperTypes().add(this.getSelector());
		getWindowEClass.getESuperTypes().add(this.getSelector());
		getViewEClass.getESuperTypes().add(this.getTextSelector());
		getEditorEClass.getESuperTypes().add(this.getTextSelector());
		getSectionEClass.getESuperTypes().add(this.getTextSelector());
		getBannerEClass.getESuperTypes().add(this.getSelector());
		getCoolbarEClass.getESuperTypes().add(this.getSelector());
		getColorSelectorEClass.getESuperTypes().add(this.getSelector());
		getCellEClass.getESuperTypes().add(this.getSelector());
		clickEClass.getESuperTypes().add(theCorePackage.getCommand());
		doubleClickEClass.getESuperTypes().add(this.getClick());
		getTextEClass.getESuperTypes().add(theCorePackage.getCommand());
		isDisabledEClass.getESuperTypes().add(theCorePackage.getCommand());
		isDisposedEClass.getESuperTypes().add(theCorePackage.getCommand());
		typeTextEClass.getESuperTypes().add(theCorePackage.getCommand());
		keyTypeEClass.getESuperTypes().add(theCorePackage.getCommand());
		typeCommandKeyEClass.getESuperTypes().add(theCorePackage.getCommand());
		setTextEClass.getESuperTypes().add(theCorePackage.getCommand());
		setTextSelectionEClass.getESuperTypes().add(theCorePackage.getCommand());
		setTextOffsetEClass.getESuperTypes().add(theCorePackage.getCommand());
		checkEClass.getESuperTypes().add(theCorePackage.getCommand());
		uncheckEClass.getESuperTypes().add(theCorePackage.getCommand());
		selectEClass.getESuperTypes().add(theCorePackage.getCommand());
		cellEditEClass.getESuperTypes().add(theCorePackage.getCommand());
		activateCellEditEClass.getESuperTypes().add(this.getCellEdit());
		applyCellEditEClass.getESuperTypes().add(this.getCellEdit());
		cancelCellEditEClass.getESuperTypes().add(this.getCellEdit());
		deactivateCellEditEClass.getESuperTypes().add(this.getCellEdit());
		closeEClass.getESuperTypes().add(theCorePackage.getCommand());
		returnFromOsDialogEClass.getESuperTypes().add(theCorePackage.getCommand());
		waitUntilEclipseIsReadyEClass.getESuperTypes().add(theCorePackage.getCommand());
		showContentAssistEClass.getESuperTypes().add(theCorePackage.getCommand());
		dragActionEClass.getESuperTypes().add(theCorePackage.getCommand());
		dragStartEClass.getESuperTypes().add(this.getDragAction());
		dragEndEClass.getESuperTypes().add(this.getDragAction());
		dragEnterEClass.getESuperTypes().add(this.getDragAction());
		dragExitEClass.getESuperTypes().add(this.getDragAction());
		dragSetDataEClass.getESuperTypes().add(this.getDragAction());
		dragAcceptEClass.getESuperTypes().add(this.getDragAction());
		dragDetectEClass.getESuperTypes().add(this.getDragAction());
		dragOverEClass.getESuperTypes().add(this.getDragAction());
		dropEClass.getESuperTypes().add(this.getDragAction());
		recognizeEClass.getESuperTypes().add(theCorePackage.getCommand());
		containsImageEClass.getESuperTypes().add(theCorePackage.getCommand());
		getRegionTextEClass.getESuperTypes().add(theCorePackage.getCommand());
		getRegionEClass.getESuperTypes().add(this.getSelector());
		getAdvancedInfoEClass.getESuperTypes().add(theCorePackage.getCommand());
		getAboutMenuEClass.getESuperTypes().add(theCorePackage.getCommand());
		getPreferencesMenuEClass.getESuperTypes().add(theCorePackage.getCommand());
		getDateTimeEClass.getESuperTypes().add(this.getTextSelector());
		getSliderEClass.getESuperTypes().add(this.getTextSelector());
		setValueEClass.getESuperTypes().add(theCorePackage.getCommand());
		minimizeEClass.getESuperTypes().add(theCorePackage.getCommand());
		maximizeEClass.getESuperTypes().add(theCorePackage.getCommand());
		restoreEClass.getESuperTypes().add(theCorePackage.getCommand());
		showTabListEClass.getESuperTypes().add(theCorePackage.getCommand());
		optionsEClass.getESuperTypes().add(theCorePackage.getCommand());
		execWithOptionsEClass.getESuperTypes().add(theCorePackage.getCommand());
		shutdownAutEClass.getESuperTypes().add(theCorePackage.getCommand());
		setDialogResultEClass.getESuperTypes().add(theCorePackage.getCommand());
		getPropertyNodesEClass.getESuperTypes().add(theCorePackage.getCommand());
		getByOsEClass.getESuperTypes().add(theCorePackage.getCommand());
		fromRawKeyEClass.getESuperTypes().add(theCorePackage.getCommand());
		hoverAtTextOffsetEClass.getESuperTypes().add(theCorePackage.getCommand());
		getTextViewerEClass.getESuperTypes().add(this.getSelector());
		selectRangeEClass.getESuperTypes().add(theCorePackage.getCommand());
		setCaretPosEClass.getESuperTypes().add(theCorePackage.getCommand());
		hoverTextEClass.getESuperTypes().add(theCorePackage.getCommand());
		openDeclarationEClass.getESuperTypes().add(theCorePackage.getCommand());
		getVerticalRulerEClass.getESuperTypes().add(this.getSelector());
		getLeftRulerEClass.getESuperTypes().add(this.getSelector());
		getRulerColumnEClass.getESuperTypes().add(this.getSelector());
		getRightRulerEClass.getESuperTypes().add(this.getSelector());
		clickRulerEClass.getESuperTypes().add(theCorePackage.getCommand());
		doubleClickRulerEClass.getESuperTypes().add(theCorePackage.getCommand());
		hoverRulerEClass.getESuperTypes().add(theCorePackage.getCommand());
		controlCommandEClass.getESuperTypes().add(theCorePackage.getCommand());
		clickLinkEClass.getESuperTypes().add(this.getControlCommand());
		expandEClass.getESuperTypes().add(this.getControlCommand());
		collapseEClass.getESuperTypes().add(this.getControlCommand());
		setFocusEClass.getESuperTypes().add(this.getControlCommand());
		getTableDataEClass.getESuperTypes().add(this.getControlCommand());
		clickColumnEClass.getESuperTypes().add(this.getControlCommand());
		traceEClass.getESuperTypes().add(theCorePackage.getCommand());
		mouseEClass.getESuperTypes().add(this.getControlCommand());
		getObjectEClass.getESuperTypes().add(theCorePackage.getCommand());
		getItemsEClass.getESuperTypes().add(this.getControlCommand());
		expandAllEClass.getESuperTypes().add(this.getControlCommand());
		collapseAllEClass.getESuperTypes().add(this.getControlCommand());
		takeScreenshotEClass.getESuperTypes().add(theCorePackage.getCommand());
		selectItemEClass.getESuperTypes().add(this.getControlCommand());
		getWidgetDetailsEClass.getESuperTypes().add(theCorePackage.getCommand());
		clickTextEClass.getESuperTypes().add(this.getControlCommand());
		getQuickAccessEClass.getESuperTypes().add(this.getSelector());
		getColumnHeaderEClass.getESuperTypes().add(this.getTextSelector());
		setPositionEClass.getESuperTypes().add(this.getControlCommand());
		setWidthEClass.getESuperTypes().add(this.getControlCommand());
		getPropertyTabEClass.getESuperTypes().add(this.getTextSelector());
		showAlertEClass.getESuperTypes().add(theCorePackage.getCommand());
		doubleClickTextEClass.getESuperTypes().add(this.getControlCommand());
		toControlHandleEClass.getESuperTypes().add(theCorePackage.getCommand());
		boundControlHandleEClass.getESuperTypes().add(this.getControlHandler());
		unfocusEClass.getESuperTypes().add(this.getControlCommand());
		decryptEClass.getESuperTypes().add(theCorePackage.getCommand());
		restartAutEClass.getESuperTypes().add(theCorePackage.getCommand());
		getLastMessageBoxEClass.getESuperTypes().add(theCorePackage.getCommand());
		checkDownloadResultEClass.getESuperTypes().add(theCorePackage.getCommand());
		getRuntimeTargetEClass.getESuperTypes().add(theCorePackage.getCommand());
		getTestCaseNameEClass.getESuperTypes().add(theCorePackage.getCommand());
		getComboItemsEClass.getESuperTypes().add(this.getControlCommand());

		// Initialize classes and features; add operations and parameters
		initEClass(waitEClass, Wait.class, "Wait", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getWait_Ms(), ecorePackage.getEInt(), "ms", null, 1, 1, Wait.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(setupPlayerEClass, SetupPlayer.class, "SetupPlayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(shoutdownPlayerEClass, ShoutdownPlayer.class, "ShoutdownPlayer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(teslaProcessStatusEClass, TeslaProcessStatus.class, "TeslaProcessStatus", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTeslaProcessStatus_Info(), theInfoPackage.getAdvancedInformation(), null, "info", null, 0, 1, TeslaProcessStatus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(wrapperEClass, Wrapper.class, "Wrapper", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getWrapper_Object(), ecorePackage.getEJavaObject(), "object", null, 1, 1, Wrapper.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getPropertyEClass, GetProperty.class, "GetProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGetProperty_Object(), theEcorePackage.getEObject(), null, "object", null, 1, 1, GetProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetProperty_Name(), theEcorePackage.getEString(), "name", null, 1, 1, GetProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetProperty_Index(), ecorePackage.getEIntegerObject(), "index", null, 0, 1, GetProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetProperty_Raw(), theEcorePackage.getEBoolean(), "raw", "false", 0, 1, GetProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(verifyTrueEClass, VerifyTrue.class, "VerifyTrue", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getVerifyTrue_Condition(), theEcorePackage.getEObject(), null, "condition", null, 1, 1, VerifyTrue.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(verifyFalseEClass, VerifyFalse.class, "VerifyFalse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getVerifyFalse_Condition(), theEcorePackage.getEObject(), null, "condition", null, 1, 1, VerifyFalse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(verifyHandlerEClass, VerifyHandler.class, "VerifyHandler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getVerifyHandler_Element(), theRawPackage.getElement(), null, "element", null, 0, 1, VerifyHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVerifyHandler_Attribute(), theEcorePackage.getEString(), "attribute", null, 0, 1, VerifyHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVerifyHandler_Value(), theEcorePackage.getEString(), "value", null, 0, 1, VerifyHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVerifyHandler_Kind(), theProtocolPackage.getAssertKind(), "kind", null, 0, 1, VerifyHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVerifyHandler_Index(), ecorePackage.getEIntegerObject(), "index", null, 0, 1, VerifyHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(verifyErrorEClass, org.eclipse.rcptt.tesla.ecl.model.VerifyError.class, "VerifyError", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getVerifyError_Command(), theCorePackage.getCommand(), null, "command", null, 1, 1, org.eclipse.rcptt.tesla.ecl.model.VerifyError.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(equalsEClass, Equals.class, "Equals", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getEquals_Input(), theEcorePackage.getEObject(), null, "input", null, 1, 1, Equals.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEquals_Value(), theEcorePackage.getEString(), "value", null, 1, 1, Equals.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(containsEClass, Contains.class, "Contains", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getContains_Input(), theEcorePackage.getEObject(), null, "input", null, 1, 1, Contains.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getContains_Value(), theEcorePackage.getEString(), "value", null, 1, 1, Contains.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(matchesEClass, Matches.class, "Matches", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMatches_Input(), theEcorePackage.getEObject(), null, "input", null, 1, 1, Matches.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMatches_Value(), theEcorePackage.getEString(), "value", null, 1, 1, Matches.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(isEmptyEClass, IsEmpty.class, "IsEmpty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIsEmpty_Input(), theEcorePackage.getEObject(), null, "input", null, 1, 1, IsEmpty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(unsupportedEClass, Unsupported.class, "Unsupported", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getUnsupported_Desc(), theEcorePackage.getEString(), "desc", null, 0, 1, Unsupported.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(controlNotFoundEClass, ControlNotFound.class, "ControlNotFound", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getControlNotFound_Id(), theEcorePackage.getEString(), "id", null, 0, 1, ControlNotFound.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(controlHandlerEClass, ControlHandler.class, "ControlHandler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getControlHandler_Kind(), this.getElementKind(), "kind", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_CustomKindId(), theEcorePackage.getEString(), "customKindId", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_Text(), theEcorePackage.getEString(), "text", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_Type(), theEcorePackage.getEString(), "type", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_Index(), theEcorePackage.getEIntegerObject(), "index", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_Path(), theEcorePackage.getEString(), "path", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_Indexes(), theEcorePackage.getEString(), "indexes", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getControlHandler_After(), this.getControlHandler(), null, "after", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getControlHandler_Parent(), this.getControlHandler(), null, "parent", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getControlHandler_Resolved(), theRawPackage.getElement(), null, "resolved", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_Row(), theEcorePackage.getEIntegerObject(), "row", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_Column(), theEcorePackage.getEIntegerObject(), "column", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_X(), theEcorePackage.getEInt(), "x", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_Y(), theEcorePackage.getEInt(), "y", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getControlHandler_RawImage(), theEcorePackage.getEString(), "rawImage", null, 0, 1, ControlHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getEclipseWindowEClass, GetEclipseWindow.class, "GetEclipseWindow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(selectorEClass, Selector.class, "Selector", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSelector_Id(), theEcorePackage.getEString(), "id", null, 0, 1, Selector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSelector_After(), this.getControlHandler(), null, "after", null, 0, 1, Selector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelector_Type(), theEcorePackage.getEString(), "type", null, 0, 1, Selector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelector_Index(), ecorePackage.getEIntegerObject(), "index", null, 0, 1, Selector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSelector_Parent(), this.getControlHandler(), null, "parent", null, 0, 1, Selector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(pathSelectorEClass, PathSelector.class, "PathSelector", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPathSelector_Path(), theEcorePackage.getEString(), "path", null, 1, 1, PathSelector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(textSelectorEClass, TextSelector.class, "TextSelector", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTextSelector_Text(), theEcorePackage.getEString(), "text", null, 0, 1, TextSelector.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getControlEClass, GetControl.class, "GetControl", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetControl_Kind(), theEcorePackage.getEString(), "kind", null, 1, 1, GetControl.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getButtonEClass, GetButton.class, "GetButton", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getCanvasEClass, GetCanvas.class, "GetCanvas", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getCheckboxEClass, GetCheckbox.class, "GetCheckbox", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getComboEClass, GetCombo.class, "GetCombo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getEditboxEClass, GetEditbox.class, "GetEditbox", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getGroupEClass, GetGroup.class, "GetGroup", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getItemEClass, GetItem.class, "GetItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetItem_Path(), theEcorePackage.getEString(), "path", null, 0, 1, GetItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetItem_Column(), theEcorePackage.getEString(), "column", null, 0, 1, GetItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getLabelEClass, GetLabel.class, "GetLabel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getLinkEClass, GetLink.class, "GetLink", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getListEClass, GetList.class, "GetList", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getMenuEClass, GetMenu.class, "GetMenu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getTabFolderEClass, GetTabFolder.class, "GetTabFolder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getTabItemEClass, GetTabItem.class, "GetTabItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getTableEClass, GetTable.class, "GetTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getToolbarEClass, GetToolbar.class, "GetToolbar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getTreeEClass, GetTree.class, "GetTree", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getWindowEClass, GetWindow.class, "GetWindow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetWindow_Text(), theEcorePackage.getEString(), "text", null, 0, 1, GetWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetWindow_From(), theEcorePackage.getEString(), "from", null, 0, 1, GetWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetWindow_Class(), ecorePackage.getEString(), "class", null, 0, 1, GetWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getViewEClass, GetView.class, "GetView", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getEditorEClass, GetEditor.class, "GetEditor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getSectionEClass, GetSection.class, "GetSection", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getBannerEClass, GetBanner.class, "GetBanner", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getCoolbarEClass, GetCoolbar.class, "GetCoolbar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getColorSelectorEClass, GetColorSelector.class, "GetColorSelector", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getCellEClass, GetCell.class, "GetCell", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetCell_Row(), theEcorePackage.getEIntegerObject(), "row", null, 1, 1, GetCell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetCell_Column(), ecorePackage.getEIntegerObject(), "column", null, 1, 1, GetCell.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(clickEClass, Click.class, "Click", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getClick_Control(), this.getControlHandler(), null, "control", null, 1, 1, Click.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClick_Nowait(), theEcorePackage.getEBoolean(), "nowait", null, 0, 1, Click.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClick_Default(), theEcorePackage.getEBoolean(), "default", null, 0, 1, Click.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClick_Arrow(), theEcorePackage.getEBoolean(), "arrow", null, 0, 1, Click.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClick_MetaKeys(), ecorePackage.getEString(), "metaKeys", null, 0, 1, Click.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(doubleClickEClass, DoubleClick.class, "DoubleClick", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getTextEClass, GetText.class, "GetText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGetText_Control(), this.getControlHandler(), null, "control", null, 1, 1, GetText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(isDisabledEClass, IsDisabled.class, "IsDisabled", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIsDisabled_Control(), this.getControlHandler(), null, "control", null, 1, 1, IsDisabled.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(isDisposedEClass, IsDisposed.class, "IsDisposed", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIsDisposed_Control(), this.getControlHandler(), null, "control", null, 1, 1, IsDisposed.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeTextEClass, TypeText.class, "TypeText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTypeText_Control(), this.getControlHandler(), null, "control", null, 1, 1, TypeText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeText_Text(), theEcorePackage.getEString(), "text", null, 1, 1, TypeText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeText_Display(), theEcorePackage.getEBoolean(), "display", null, 0, 1, TypeText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(keyTypeEClass, KeyType.class, "KeyType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getKeyType_Control(), this.getControlHandler(), null, "control", null, 1, 1, KeyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getKeyType_Key(), theEcorePackage.getEString(), "key", null, 1, 1, KeyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getKeyType_Char(), theEcorePackage.getEString(), "char", null, 0, 1, KeyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getKeyType_Display(), theEcorePackage.getEBoolean(), "display", null, 0, 1, KeyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getKeyType_Times(), theEcorePackage.getEIntegerObject(), "times", "1", 0, 1, KeyType.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(typeCommandKeyEClass, TypeCommandKey.class, "TypeCommandKey", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTypeCommandKey_Control(), this.getControlHandler(), null, "control", null, 1, 1, TypeCommandKey.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTypeCommandKey_CommandId(), theEcorePackage.getEString(), "commandId", null, 1, 1, TypeCommandKey.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(setTextEClass, SetText.class, "SetText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSetText_Control(), this.getControlHandler(), null, "control", null, 1, 1, SetText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetText_Text(), theEcorePackage.getEString(), "text", null, 1, 1, SetText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(setTextSelectionEClass, SetTextSelection.class, "SetTextSelection", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSetTextSelection_Control(), this.getControlHandler(), null, "control", null, 1, 1, SetTextSelection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetTextSelection_Offset(), ecorePackage.getEIntegerObject(), "offset", null, 1, 1, SetTextSelection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetTextSelection_Length(), ecorePackage.getEIntegerObject(), "length", null, 0, 1, SetTextSelection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetTextSelection_StartLine(), ecorePackage.getEIntegerObject(), "startLine", null, 0, 1, SetTextSelection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetTextSelection_EndLine(), ecorePackage.getEIntegerObject(), "endLine", null, 0, 1, SetTextSelection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetTextSelection_EndOffset(), ecorePackage.getEIntegerObject(), "endOffset", null, 0, 1, SetTextSelection.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(setTextOffsetEClass, SetTextOffset.class, "SetTextOffset", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSetTextOffset_Control(), this.getControlHandler(), null, "control", null, 1, 1, SetTextOffset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetTextOffset_Value(), theEcorePackage.getEIntegerObject(), "value", null, 1, 1, SetTextOffset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetTextOffset_Line(), theEcorePackage.getEIntegerObject(), "line", null, 0, 1, SetTextOffset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(checkEClass, Check.class, "Check", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCheck_Control(), this.getControlHandler(), null, "control", null, 1, 1, Check.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(uncheckEClass, Uncheck.class, "Uncheck", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getUncheck_Control(), this.getControlHandler(), null, "control", null, 1, 1, Uncheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(selectEClass, Select.class, "Select", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSelect_Control(), this.getControlHandler(), null, "control", null, 1, 1, Select.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelect_Items(), ecorePackage.getEJavaObject(), "items", null, 1, -1, Select.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelect_All(), ecorePackage.getEBoolean(), "all", "false", 0, 1, Select.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelect_Column(), theEcorePackage.getEString(), "column", null, 0, 1, Select.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(cellEditEClass, CellEdit.class, "CellEdit", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCellEdit_Control(), this.getControlHandler(), null, "control", null, 1, 1, CellEdit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(activateCellEditEClass, ActivateCellEdit.class, "ActivateCellEdit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getActivateCellEdit_Column(), ecorePackage.getEInt(), "column", null, 1, 1, ActivateCellEdit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getActivateCellEdit_Type(), this.getActivationEventType(), "type", null, 0, 1, ActivateCellEdit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getActivateCellEdit_Button(), theEcorePackage.getEInt(), "button", "1", 0, 1, ActivateCellEdit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(applyCellEditEClass, ApplyCellEdit.class, "ApplyCellEdit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getApplyCellEdit_Deactivate(), theEcorePackage.getEBoolean(), "deactivate", "false", 0, 1, ApplyCellEdit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(cancelCellEditEClass, CancelCellEdit.class, "CancelCellEdit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(deactivateCellEditEClass, DeactivateCellEdit.class, "DeactivateCellEdit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(closeEClass, Close.class, "Close", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getClose_Control(), this.getControlHandler(), null, "control", null, 1, 1, Close.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(returnFromOsDialogEClass, ReturnFromOsDialog.class, "ReturnFromOsDialog", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getReturnFromOsDialog_Kind(), theEcorePackage.getEString(), "kind", null, 1, 1, ReturnFromOsDialog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReturnFromOsDialog_Result(), theEcorePackage.getEString(), "result", null, 0, -1, ReturnFromOsDialog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(waitUntilEclipseIsReadyEClass, WaitUntilEclipseIsReady.class, "WaitUntilEclipseIsReady", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(showContentAssistEClass, ShowContentAssist.class, "ShowContentAssist", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getShowContentAssist_Control(), this.getControlHandler(), null, "control", null, 1, 1, ShowContentAssist.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dragActionEClass, DragAction.class, "DragAction", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDragAction_Control(), this.getControlHandler(), null, "control", null, 1, 1, DragAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDragAction_X(), theEcorePackage.getEIntegerObject(), "x", null, 0, 1, DragAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDragAction_Y(), theEcorePackage.getEIntegerObject(), "y", null, 0, 1, DragAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDragAction_Button(), this.getButton(), "button", "none", 0, 1, DragAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDragAction_Mask(), theEcorePackage.getEString(), "mask", "", 0, 1, DragAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDragAction_Detail(), ecorePackage.getEString(), "detail", "none", 0, 1, DragAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDragAction_Operation(), theEcorePackage.getEIntegerObject(), "operation", "-1", 0, 1, DragAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dragStartEClass, DragStart.class, "DragStart", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dragEndEClass, DragEnd.class, "DragEnd", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dragEnterEClass, DragEnter.class, "DragEnter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dragExitEClass, DragExit.class, "DragExit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dragSetDataEClass, DragSetData.class, "DragSetData", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dragAcceptEClass, DragAccept.class, "DragAccept", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dragDetectEClass, DragDetect.class, "DragDetect", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dragOverEClass, DragOver.class, "DragOver", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(dropEClass, Drop.class, "Drop", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(recognizeEClass, Recognize.class, "Recognize", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRecognize_Image(), theEcorePackage.getEByteArray(), "image", null, 0, 1, Recognize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRecognize_X(), theEcorePackage.getEInt(), "x", null, 0, 1, Recognize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRecognize_Y(), theEcorePackage.getEInt(), "y", null, 0, 1, Recognize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRecognize_Width(), theEcorePackage.getEInt(), "width", null, 0, 1, Recognize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRecognize_Height(), theEcorePackage.getEInt(), "height", null, 0, 1, Recognize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(recognizeResponseEClass, RecognizeResponse.class, "RecognizeResponse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRecognizeResponse_Text(), theEcorePackage.getEString(), "text", null, 0, 1, RecognizeResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(containsImageEClass, ContainsImage.class, "ContainsImage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getContainsImage_Control(), this.getControlHandler(), null, "control", null, 1, 1, ContainsImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getContainsImage_ImageURI(), theEcorePackage.getEString(), "imageURI", null, 1, 1, ContainsImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getContainsImage_RawImage(), theEcorePackage.getEString(), "rawImage", null, 0, 1, ContainsImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getRegionTextEClass, GetRegionText.class, "GetRegionText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGetRegionText_Control(), this.getControlHandler(), null, "control", null, 1, 1, GetRegionText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegionText_X(), theEcorePackage.getEIntegerObject(), "x", "0", 1, 1, GetRegionText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegionText_Y(), theEcorePackage.getEIntegerObject(), "y", "0", 1, 1, GetRegionText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegionText_Sx(), theEcorePackage.getEIntegerObject(), "sx", null, 1, 1, GetRegionText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegionText_Sy(), theEcorePackage.getEIntegerObject(), "sy", null, 1, 1, GetRegionText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegionText_Width(), theEcorePackage.getEIntegerObject(), "width", null, 1, 1, GetRegionText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegionText_Height(), theEcorePackage.getEIntegerObject(), "height", null, 1, 1, GetRegionText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getRegionEClass, GetRegion.class, "GetRegion", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetRegion_X(), theEcorePackage.getEInt(), "x", null, 1, 1, GetRegion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegion_Y(), theEcorePackage.getEInt(), "y", null, 1, 1, GetRegion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegion_ImageURI(), theEcorePackage.getEString(), "imageURI", null, 1, 1, GetRegion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetRegion_RawImage(), theEcorePackage.getEString(), "rawImage", null, 0, 1, GetRegion.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getAdvancedInfoEClass, GetAdvancedInfo.class, "GetAdvancedInfo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getAboutMenuEClass, GetAboutMenu.class, "GetAboutMenu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getPreferencesMenuEClass, GetPreferencesMenu.class, "GetPreferencesMenu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getDateTimeEClass, GetDateTime.class, "GetDateTime", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getSliderEClass, GetSlider.class, "GetSlider", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(setValueEClass, SetValue.class, "SetValue", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSetValue_Control(), this.getControlHandler(), null, "control", null, 1, 1, SetValue.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetValue_Value(), theEcorePackage.getEString(), "value", null, 1, 1, SetValue.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(minimizeEClass, Minimize.class, "Minimize", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMinimize_Control(), this.getControlHandler(), null, "control", null, 1, 1, Minimize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(maximizeEClass, Maximize.class, "Maximize", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMaximize_Control(), this.getControlHandler(), null, "control", null, 1, 1, Maximize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(restoreEClass, Restore.class, "Restore", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRestore_Control(), this.getControlHandler(), null, "control", null, 1, 1, Restore.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(showTabListEClass, ShowTabList.class, "ShowTabList", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getShowTabList_Control(), this.getControlHandler(), null, "control", null, 1, 1, ShowTabList.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(optionsEClass, Options.class, "Options", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOptions_AllowStatusDialog(), theEcorePackage.getEBoolean(), "allowStatusDialog", null, 0, 1, Options.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOptions_Command(), theCorePackage.getCommand(), null, "command", null, 0, 1, Options.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(execWithOptionsEClass, ExecWithOptions.class, "ExecWithOptions", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getExecWithOptions_Command(), theCorePackage.getCommand(), null, "command", null, 1, 1, ExecWithOptions.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getExecWithOptions_AllowStatusDialog(), theEcorePackage.getEBoolean(), "allowStatusDialog", null, 0, 1, ExecWithOptions.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getExecWithOptions_DisableJobWaiting(), theEcorePackage.getEBoolean(), "disableJobWaiting", null, 0, 1, ExecWithOptions.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(shutdownAutEClass, ShutdownAut.class, "ShutdownAut", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(setDialogResultEClass, SetDialogResult.class, "SetDialogResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSetDialogResult_Kind(), theEcorePackage.getEString(), "kind", null, 1, 1, SetDialogResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetDialogResult_Result(), theEcorePackage.getEString(), "result", null, 0, -1, SetDialogResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getPropertyNodesEClass, GetPropertyNodes.class, "GetPropertyNodes", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGetPropertyNodes_Element(), theRawPackage.getElement(), null, "element", null, 0, 1, GetPropertyNodes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetPropertyNodes_NodePath(), theEcorePackage.getEString(), "nodePath", null, 0, 1, GetPropertyNodes.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getByOsEClass, GetByOs.class, "GetByOs", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetByOs_Default(), theEcorePackage.getEString(), "default", null, 0, 1, GetByOs.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetByOs_Win(), theEcorePackage.getEString(), "win", null, 0, 1, GetByOs.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetByOs_Linux(), theEcorePackage.getEString(), "linux", null, 0, 1, GetByOs.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetByOs_Macosx(), theEcorePackage.getEString(), "macosx", null, 0, 1, GetByOs.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(fromRawKeyEClass, FromRawKey.class, "FromRawKey", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFromRawKey_Mask(), ecorePackage.getEIntegerObject(), "mask", null, 0, 1, FromRawKey.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFromRawKey_KeyCode(), ecorePackage.getEIntegerObject(), "keyCode", null, 0, 1, FromRawKey.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFromRawKey_Meta(), ecorePackage.getEIntegerObject(), "meta", null, 0, 1, FromRawKey.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(hoverAtTextOffsetEClass, HoverAtTextOffset.class, "HoverAtTextOffset", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getHoverAtTextOffset_Control(), this.getControlHandler(), null, "control", null, 1, 1, HoverAtTextOffset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHoverAtTextOffset_Offset(), theEcorePackage.getEIntegerObject(), "offset", null, 1, 1, HoverAtTextOffset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHoverAtTextOffset_Line(), theEcorePackage.getEIntegerObject(), "line", null, 0, 1, HoverAtTextOffset.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getTextViewerEClass, GetTextViewer.class, "GetTextViewer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(selectRangeEClass, SelectRange.class, "SelectRange", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSelectRange_Control(), this.getControlHandler(), null, "control", null, 1, 1, SelectRange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelectRange_Line(), theEcorePackage.getEInt(), "line", null, 0, 1, SelectRange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelectRange_Column(), theEcorePackage.getEInt(), "column", null, 0, 1, SelectRange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelectRange_EndLine(), theEcorePackage.getEInt(), "endLine", null, 0, 1, SelectRange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSelectRange_EndColumn(), theEcorePackage.getEInt(), "endColumn", null, 0, 1, SelectRange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(setCaretPosEClass, SetCaretPos.class, "SetCaretPos", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSetCaretPos_Control(), this.getControlHandler(), null, "control", null, 1, 1, SetCaretPos.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetCaretPos_Line(), theEcorePackage.getEInt(), "line", null, 0, 1, SetCaretPos.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSetCaretPos_Column(), theEcorePackage.getEInt(), "column", null, 0, 1, SetCaretPos.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(hoverTextEClass, HoverText.class, "HoverText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getHoverText_Control(), this.getControlHandler(), null, "control", null, 1, 1, HoverText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHoverText_Line(), theEcorePackage.getEInt(), "line", null, 0, 1, HoverText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHoverText_Column(), theEcorePackage.getEInt(), "column", null, 0, 1, HoverText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHoverText_With(), theEcorePackage.getEString(), "with", "", 0, 1, HoverText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(openDeclarationEClass, OpenDeclaration.class, "OpenDeclaration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getOpenDeclaration_Control(), this.getControlHandler(), null, "control", null, 1, 1, OpenDeclaration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getVerticalRulerEClass, GetVerticalRuler.class, "GetVerticalRuler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getLeftRulerEClass, GetLeftRuler.class, "GetLeftRuler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getRulerColumnEClass, GetRulerColumn.class, "GetRulerColumn", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetRulerColumn_Text(), theEcorePackage.getEString(), "text", null, 0, 1, GetRulerColumn.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getRightRulerEClass, GetRightRuler.class, "GetRightRuler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(clickRulerEClass, ClickRuler.class, "ClickRuler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getClickRuler_Control(), this.getControlHandler(), null, "control", null, 1, 1, ClickRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClickRuler_Line(), theEcorePackage.getEInt(), "line", null, 0, 1, ClickRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClickRuler_Button(), theEcorePackage.getEString(), "button", "Left", 0, 1, ClickRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClickRuler_With(), theEcorePackage.getEString(), "with", "", 0, 1, ClickRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(doubleClickRulerEClass, DoubleClickRuler.class, "DoubleClickRuler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDoubleClickRuler_Control(), this.getControlHandler(), null, "control", null, 1, 1, DoubleClickRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDoubleClickRuler_Line(), theEcorePackage.getEInt(), "line", null, 0, 1, DoubleClickRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDoubleClickRuler_Button(), theEcorePackage.getEString(), "button", "Left", 0, 1, DoubleClickRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDoubleClickRuler_With(), theEcorePackage.getEString(), "with", "", 0, 1, DoubleClickRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(hoverRulerEClass, HoverRuler.class, "HoverRuler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getHoverRuler_Control(), this.getControlHandler(), null, "control", null, 1, 1, HoverRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHoverRuler_Line(), theEcorePackage.getEInt(), "line", null, 0, 1, HoverRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHoverRuler_With(), theEcorePackage.getEString(), "with", "", 0, 1, HoverRuler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(controlCommandEClass, ControlCommand.class, "ControlCommand", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getControlCommand_Control(), this.getControlHandler(), null, "control", null, 0, 1, ControlCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(clickLinkEClass, ClickLink.class, "ClickLink", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getClickLink_Ref(), theEcorePackage.getEString(), "ref", null, 0, 1, ClickLink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(expandEClass, Expand.class, "Expand", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(collapseEClass, Collapse.class, "Collapse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(setFocusEClass, SetFocus.class, "SetFocus", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getTableDataEClass, GetTableData.class, "GetTableData", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetTableData_IncludeChecked(), theEcorePackage.getEBoolean(), "includeChecked", "false", 0, 1, GetTableData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetTableData_ExcludeHidden(), theEcorePackage.getEBoolean(), "excludeHidden", "false", 0, 1, GetTableData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(clickColumnEClass, ClickColumn.class, "ClickColumn", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getClickColumn_Name(), theEcorePackage.getEString(), "name", null, 0, 1, ClickColumn.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClickColumn_Index(), theEcorePackage.getEInt(), "index", null, 0, 1, ClickColumn.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(traceEClass, Trace.class, "Trace", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTrace_Message(), theEcorePackage.getEString(), "message", null, 0, 1, Trace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mouseEClass, Mouse.class, "Mouse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMouse_Event(), theEcorePackage.getEString(), "event", null, 0, 1, Mouse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouse_Button(), theEcorePackage.getEString(), "button", "Left", 0, 1, Mouse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouse_With(), theEcorePackage.getEString(), "with", "", 0, 1, Mouse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouse_Count(), theEcorePackage.getEInt(), "count", "-1", 0, 1, Mouse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouse_X(), theEcorePackage.getEInt(), "x", "-2147483648", 0, 1, Mouse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMouse_Y(), theEcorePackage.getEInt(), "y", "-2147483648", 0, 1, Mouse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getObjectEClass, GetObject.class, "GetObject", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGetObject_Object(), theEcorePackage.getEObject(), null, "object", null, 1, 1, GetObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getItemsEClass, GetItems.class, "GetItems", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(expandAllEClass, ExpandAll.class, "ExpandAll", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(collapseAllEClass, CollapseAll.class, "CollapseAll", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(takeScreenshotEClass, TakeScreenshot.class, "TakeScreenshot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTakeScreenshot_Message(), theEcorePackage.getEString(), "message", "", 0, 1, TakeScreenshot.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(selectItemEClass, SelectItem.class, "SelectItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getWidgetDetailsEClass, GetWidgetDetails.class, "GetWidgetDetails", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGetWidgetDetails_Element(), theRawPackage.getElement(), null, "element", null, 0, 1, GetWidgetDetails.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(clickTextEClass, ClickText.class, "ClickText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getClickText_Start(), theEcorePackage.getEString(), "start", "", 0, 1, ClickText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClickText_End(), theEcorePackage.getEString(), "end", "", 0, 1, ClickText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClickText_Button(), theEcorePackage.getEString(), "button", "left", 0, 1, ClickText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getQuickAccessEClass, GetQuickAccess.class, "GetQuickAccess", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getColumnHeaderEClass, GetColumnHeader.class, "GetColumnHeader", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(setPositionEClass, SetPosition.class, "SetPosition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSetPosition_Index(), theEcorePackage.getEInt(), "index", null, 1, 1, SetPosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(setWidthEClass, SetWidth.class, "SetWidth", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSetWidth_Width(), theEcorePackage.getEInt(), "width", null, 1, 1, SetWidth.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getPropertyTabEClass, GetPropertyTab.class, "GetPropertyTab", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(showAlertEClass, ShowAlert.class, "ShowAlert", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getShowAlert_Message(), theEcorePackage.getEString(), "message", null, 0, 1, ShowAlert.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(doubleClickTextEClass, DoubleClickText.class, "DoubleClickText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDoubleClickText_Position(), theEcorePackage.getEString(), "position", null, 1, 1, DoubleClickText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDoubleClickText_Button(), theEcorePackage.getEString(), "button", "left", 0, 1, DoubleClickText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(toControlHandleEClass, ToControlHandle.class, "ToControlHandle", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getToControlHandle_Widget(), theEcorePackage.getEJavaObject(), "widget", null, 1, 1, ToControlHandle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(boundControlHandleEClass, BoundControlHandle.class, "BoundControlHandle", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBoundControlHandle_WidgetId(), theEcorePackage.getELong(), "widgetId", null, 1, 1, BoundControlHandle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(unfocusEClass, Unfocus.class, "Unfocus", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(decryptEClass, Decrypt.class, "Decrypt", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDecrypt_Value(), theEcorePackage.getEString(), "value", null, 1, 1, Decrypt.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(decryptResultEClass, DecryptResult.class, "DecryptResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDecryptResult_Value(), theEcorePackage.getEString(), "value", null, 1, 1, DecryptResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(restartAutEClass, RestartAut.class, "RestartAut", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(messageBoxInfoEClass, MessageBoxInfo.class, "MessageBoxInfo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMessageBoxInfo_Title(), theEcorePackage.getEString(), "title", null, 0, 1, MessageBoxInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMessageBoxInfo_Message(), theEcorePackage.getEString(), "message", null, 0, 1, MessageBoxInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getLastMessageBoxEClass, GetLastMessageBox.class, "GetLastMessageBox", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(checkDownloadResultEClass, CheckDownloadResult.class, "CheckDownloadResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCheckDownloadResult_ContentOnBase64(), theEcorePackage.getEString(), "contentOnBase64", null, 1, 1, CheckDownloadResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCheckDownloadResult_FileName(), theEcorePackage.getEString(), "fileName", null, 0, 1, CheckDownloadResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getRuntimeTargetEClass, GetRuntimeTarget.class, "GetRuntimeTarget", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getTestCaseNameEClass, GetTestCaseName.class, "GetTestCaseName", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getComboItemsEClass, GetComboItems.class, "GetComboItems", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Initialize enums and add enum literals
		initEEnum(buttonEEnum, Button.class, "Button");
		addEEnumLiteral(buttonEEnum, Button.NONE);
		addEEnumLiteral(buttonEEnum, Button.BUTTON1);
		addEEnumLiteral(buttonEEnum, Button.BUTTON2);
		addEEnumLiteral(buttonEEnum, Button.BUTTON3);

		initEEnum(activationEventTypeEEnum, ActivationEventType.class, "ActivationEventType");
		addEEnumLiteral(activationEventTypeEEnum, ActivationEventType.PROGRAMMATIC);
		addEEnumLiteral(activationEventTypeEEnum, ActivationEventType.KEY_PRESSED);
		addEEnumLiteral(activationEventTypeEEnum, ActivationEventType.MOUSE_CLICK_SELECTION);
		addEEnumLiteral(activationEventTypeEEnum, ActivationEventType.MOUSE_DOUBLE_CLICK_SELECTION);
		addEEnumLiteral(activationEventTypeEEnum, ActivationEventType.TRAVERSAL);

		// Initialize data types
		initEDataType(elementKindEDataType, ElementKind.class, "ElementKind", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/ecl/docs
		createDocsAnnotations();
		// http://www.eclipse.org/ecl/input
		createInputAnnotations();
		// http://www.eclipse.org/ecl/internal
		createInternalAnnotations();
		// http://www.eclipse.org/ecl/depricated
		createDepricatedAnnotations();
		// http://www.eclipse.org/ecl/meta
		createMetaAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/docs</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createDocsAnnotations() {
		String source = "http://www.eclipse.org/ecl/docs";
		addAnnotation
		  (waitEClass,
		   source,
		   new String[] {
			   "description", "Suspend execution for a given number of milliseconds.",
			   "returns", "nothing",
			   "recorded", "false",
			   "example", "wait 100"
		   });
		addAnnotation
		  (getWait_Ms(),
		   source,
		   new String[] {
			   "description", "Number of milliseconds. Must be integer value greater than zero."
		   });
		addAnnotation
		  (getPropertyEClass,
		   source,
		   new String[] {
			   "description", "Gets property of UI control. Assertion mode and assertion window can be used for discovering of actual property names",
			   "returns", "Property handler for further verification (like during recording), or actual property value as string, when <code>-raw</code> option is specified",
			   "recorded", "true",
			   "example", "get-view Tasks | get-tree | get-property itemCount | equals 1 | verify-true"
		   });
		addAnnotation
		  (getGetProperty_Object(),
		   source,
		   new String[] {
			   "description", "UI control to get property from"
		   });
		addAnnotation
		  (getGetProperty_Name(),
		   source,
		   new String[] {
			   "description", "Property name"
		   });
		addAnnotation
		  (getGetProperty_Index(),
		   source,
		   new String[] {
			   "description", "Property index for list values"
		   });
		addAnnotation
		  (getGetProperty_Raw(),
		   source,
		   new String[] {
			   "description", "When true, command returns property value\nWhen false, command returns internal property verification handle"
		   });
		addAnnotation
		  (verifyTrueEClass,
		   source,
		   new String[] {
			   "description", "Checks <code>input</code> condition\'s verity. If verity is not confirmed, then error is returned",
			   "input", "EObject condition",
			   "example", "get-editor \"Test scenario\" | get-section Script | get-editbox | get-property value | equals test | verify-true"
		   });
		addAnnotation
		  (getVerifyTrue_Condition(),
		   source,
		   new String[] {
			   "description", "<code>input</code> condition that has to be true"
		   });
		addAnnotation
		  (verifyFalseEClass,
		   source,
		   new String[] {
			   "description", "Checks <code>input</code> condition\'s falsity. If falsity is not confirmed, then error is returned",
			   "input", "EObject condition",
			   "example", "get-editor \"Test scenario\" | get-section Script | get-editbox | get-property value | equals test | verify-false"
		   });
		addAnnotation
		  (getVerifyFalse_Condition(),
		   source,
		   new String[] {
			   "description", "<code>input</code> condition that has to be false"
		   });
		addAnnotation
		  (verifyErrorEClass,
		   source,
		   new String[] {
			   "description", "Checks <code>input</code> conditions. \r\nIf the specified condition is not met (for example, the object is not returned), the verification is passed. \r\nIf the condition is met (for example, the object is selected), the verification is failed.",
			   "input", "EObject condition",
			   "example", "get-view \"Project Explorer\" | get-tree | verify-error {get-item \"TestProject\"}"
		   });
		addAnnotation
		  (equalsEClass,
		   source,
		   new String[] {
			   "description", "Compares <code>input</code> with <code>value</code>",
			   "input", "EObject input",
			   "returns", "true if values are equal or false otherwise",
			   "example", "get-view \"Q7 Explorer\" | get-tree | get-item Project | get-property childCount | equals 3 | verify-true"
		   });
		addAnnotation
		  (getEquals_Input(),
		   source,
		   new String[] {
			   "description", "Property value extracted with <code>get-property</code>"
		   });
		addAnnotation
		  (getEquals_Value(),
		   source,
		   new String[] {
			   "description", "Expected property value"
		   });
		addAnnotation
		  (containsEClass,
		   source,
		   new String[] {
			   "description", "Checks if <code>input</code> has <code>value</code> within  ",
			   "input", "EObject input",
			   "returns", "true if the content of control contains the specified value or false otherwise",
			   "example", "get-editor TestCase1 | get-editbox | get-property value | contains a22 | verify-true"
		   });
		addAnnotation
		  (getContains_Input(),
		   source,
		   new String[] {
			   "description", "Property value extracted with <code>get-property</code>"
		   });
		addAnnotation
		  (getContains_Value(),
		   source,
		   new String[] {
			   "description", "Value, which is expected to be included"
		   });
		addAnnotation
		  (matchesEClass,
		   source,
		   new String[] {
			   "description", "Checks if <code>input</code> matches to a regular expression <code>value</code>",
			   "input", "Result of <code>get-property</code> command",
			   "example", "get-editor TestCase1 | get-editbox | get-property value | matches \"\\\\d+\" | verify-true"
		   });
		addAnnotation
		  (isEmptyEClass,
		   source,
		   new String[] {
			   "description", "Checks if content of control is empty",
			   "returns", "true if the content of control is empty or false otherwise",
			   "example", "get-editor WorkbenchContext |  get-section Workbench | get-editbox -after [get-label \"Perspective id:\"] | get-property text | is-empty  | verify-false"
		   });
		addAnnotation
		  (getIsEmpty_Input(),
		   source,
		   new String[] {
			   "description", "Path to control which emptyness has to be checked"
		   });
		addAnnotation
		  (getControlHandler_Type(),
		   source,
		   new String[] {
			   "description", "Indicates the type of a control, if there are any types."
		   });
		addAnnotation
		  (getEclipseWindowEClass,
		   source,
		   new String[] {
			   "description", "Gets Eclipse window.",
			   "returns", "Eclipse window",
			   "recorded", "true",
			   "example", "get-eclipse-window | key-type F8"
		   });
		addAnnotation
		  (getSelector_After(),
		   source,
		   new String[] {
			   "description", "Indicates the element after which the necessary element is present. "
		   });
		addAnnotation
		  (getSelector_Type(),
		   source,
		   new String[] {
			   "description", "Indicates the type of a control, if there are any types."
		   });
		addAnnotation
		  (getSelector_Index(),
		   source,
		   new String[] {
			   "description", "Indicates the number of control in list. If element is first in list, then this parameter is not present and default is equal 0."
		   });
		addAnnotation
		  (getSelector_Parent(),
		   source,
		   new String[] {
			   "description", ""
		   });
		addAnnotation
		  (getPathSelector_Path(),
		   source,
		   new String[] {
			   "description", "Indicates the path to the item. This parameter is used by default, so <code>-path</code> word is not written at the recording of the test."
		   });
		addAnnotation
		  (getTextSelector_Text(),
		   source,
		   new String[] {
			   "description", "Indicates the text\\name of this element. This parameter is used by default, so <code>-text</code> word is not written at the recording of the test."
		   });
		addAnnotation
		  (getControlEClass,
		   source,
		   new String[] {
			   "description", "Gets control with defined caption or index. If this control doesn\'t exist, then error is returned.",
			   "returns", "link to the control",
			   "recorded", "true",
			   "example", "//writes \'Perspective id:\' into log\nget-editor WorkbanchContext | get-section Workbench | get-control -kind \"Label\" -index 1 | get-property text -raw| str | log"
		   });
		addAnnotation
		  (getButtonEClass,
		   source,
		   new String[] {
			   "description", "Gets button with defined caption. If this button doesn\'t exist, then error is returned.",
			   "returns", "link to the button",
			   "recorded", "true",
			   "example", "get-button Replay | click"
		   });
		addAnnotation
		  (getCanvasEClass,
		   source,
		   new String[] {
			   "description", "",
			   "returns", "",
			   "recorded", "true",
			   "example", "with [get-editor CreateQ7Project | get-section Script | get-canvas] {<br>\r\n        mouse-press 24 53 button1<br>\r\n        mouse-release 24 53 button1 524288<br>\r\n    }"
		   });
		addAnnotation
		  (getCheckboxEClass,
		   source,
		   new String[] {
			   "description", "Gets checkbox with defined parameter. If this checkbox doesn\'t exist, then error is returned.",
			   "returns", "link to the checkbox",
			   "recorded", "true",
			   "example", "get-editor Wb | get-section Workbench | get-checkbox -text \"Clear clipboard\" | check"
		   });
		addAnnotation
		  (getComboEClass,
		   source,
		   new String[] {
			   "description", "Gets combobox with defined parameter. If this combobox doesn\'t exist, then error is returned.",
			   "returns", "link to the combobox",
			   "recorded", "true",
			   "example", "get-window \"Q7 Control Panel - Test\" | get-window \"Find/Replace\" | get-combo -after [get-label \"Replace with:\"] | get-property enablement | equals false"
		   });
		addAnnotation
		  (getEditboxEClass,
		   source,
		   new String[] {
			   "description", "Gets editbox with defined parameter. If this editbox doesn\'t exist, then error is returned.",
			   "returns", "link to the editbox",
			   "recorded", "true",
			   "example", "get-view \"Execution View\" | get-editbox -after [get-label \"Message:\"] | get-property text | equals \"\" | verify-true"
		   });
		addAnnotation
		  (getGroupEClass,
		   source,
		   new String[] {
			   "description", "Gets group with defined parameter. If this group doesn\'t exist, then error is returned.",
			   "returns", "group ",
			   "recorded", "true",
			   "example", "get-window Search | get-group \"Search in\" | get-button Tags | click"
		   });
		addAnnotation
		  (getItemEClass,
		   source,
		   new String[] {
			   "description", "Gets item at the specified path. If this item doesn\'t exist, then error is returned.",
			   "returns", "item",
			   "recorded", "true",
			   "example", "get-view Breakpoints | get-tree  |  get-item CreateJavaProject  | get-property checked | equals false | verify-true"
		   });
		addAnnotation
		  (getGetItem_Path(),
		   source,
		   new String[] {
			   "description", "Indicates the path to the item. This parameter is used by default, so <code>-path</code> word is not written at the recording of the test."
		   });
		addAnnotation
		  (getLabelEClass,
		   source,
		   new String[] {
			   "description", "Gets label with defined text. If this label doesn\'t exist, then error is returned.",
			   "returns", "link to the label",
			   "recorded", "true",
			   "example", "get-view \"Execution View\" | get-label -after [get-label \"Runs:\"] | get-property caption | equals \"3/3\" | verify-true"
		   });
		addAnnotation
		  (getLinkEClass,
		   source,
		   new String[] {
			   "description", "Gets link with defined text. If this link doesn\'t exist, then error is returned.",
			   "returns", "link",
			   "recorded", "true",
			   "example", "get-window \"Delete Resources\" | get-link \"See 3 reference(s) found.\" | click"
		   });
		addAnnotation
		  (getListEClass,
		   source,
		   new String[] {
			   "description", "Gets list with specified parameter. If this list doesn\'t exist, then error is returned.",
			   "returns", "list",
			   "recorded", "true"
		   });
		addAnnotation
		  (getMenuEClass,
		   source,
		   new String[] {
			   "description", "Gets the path to the section menu. If this section menu doesn\'t exist, then error is returned.",
			   "returns", "path to the section menu",
			   "recorded", "true",
			   "example", "get-window \"Q7 Control Panel - Test\" |  get-toolbar | get-menu \"Save As...\" | click"
		   });
		addAnnotation
		  (getTabFolderEClass,
		   source,
		   new String[] {
			   "description", "Gets all tabs of editor. If this editor doesn\'t contain tabs, then error is returned.",
			   "returns", "all tabs of editor",
			   "recorded", "true",
			   "example", "get-editor SO | get-tab-folder | get-tab-item Documentation | click"
		   });
		addAnnotation
		  (getTabItemEClass,
		   source,
		   new String[] {
			   "description", "Gets tab of editor with defined name. If this tab doesn\'t exist, then error is returned.",
			   "returns", "tab",
			   "recorded", "true",
			   "example", "get-editor SO | get-tab-folder | get-tab-item Documentation | click"
		   });
		addAnnotation
		  (getTableEClass,
		   source,
		   new String[] {
			   "description", "Gets the table with specified parameter. If this table doesn\'t exist, then error is returned.",
			   "returns", "table",
			   "recorded", "true",
			   "example", "get-editor test |  get-section Contexts | get-table | get-property itemCount | equals 2 | verify-true"
		   });
		addAnnotation
		  (getToolbarEClass,
		   source,
		   new String[] {
			   "description", "Gets toolbar with specified parameter. If toolbar doesn\'t exist, then error is returned.",
			   "returns", "toolbar",
			   "recorded", "true",
			   "example", "get-window \"Q7 Control Panel - Test\" |   get-toolbar | get-menu Save | click <br> get-toolbar -index 11 | key-type \"M1+s\" "
		   });
		addAnnotation
		  (getTreeEClass,
		   source,
		   new String[] {
			   "description", "Gets the tree with specified parameter. If this tree doesn\'t exist, then error is returned.",
			   "returns", "tree",
			   "recorded", "true",
			   "example", "get-view \"Q7 Explorer\" | get-tree | select \"Q7Project/ECL Context\" | double-click"
		   });
		addAnnotation
		  (getWindowEClass,
		   source,
		   new String[] {
			   "description", "Get window with defined name. If this window doesn\'t exist, then error is returned.",
			   "returns", "window",
			   "recorded", "true",
			   "example", "get-window \"Select context\" | get-table | select \"ECL Context \\\\(Q7Project\\\\)\" | click -default"
		   });
		addAnnotation
		  (getGetWindow_Text(),
		   source,
		   new String[] {
			   "description", "Indicates the name of this window. This parameter is used by default, so <code>-text</code> word is not written at the recording of the test."
		   });
		addAnnotation
		  (getGetWindow_From(),
		   source,
		   new String[] {
			   "description", "Indicates the method (in the form of <code>\"ClassName.methodName()\"</code>) where this window is created."
		   });
		addAnnotation
		  (getGetWindow_Class(),
		   source,
		   new String[] {
			   "description", "Indicates name of the JFace Window subclass defining this window."
		   });
		addAnnotation
		  (getViewEClass,
		   source,
		   new String[] {
			   "description", "Gets view with defined name. If this view doesn\'t exist, then error is returned.",
			   "returns", "view",
			   "recorded", "true",
			   "example", "get-view \"Q7 Explorer\" | get-tree | select \"Q7Project/CheckSetCommand\" | double-click"
		   });
		addAnnotation
		  (getEditorEClass,
		   source,
		   new String[] {
			   "description", "Gets editor with defined parameter. If editor doesn\'t exist, then error is returned.",
			   "returns", "editor",
			   "recorded", "true",
			   "example", "get-editor EclContext | get-section Script | get-editbox | get-property text | equals \"wait 1000\" | verify-true"
		   });
		addAnnotation
		  (getSectionEClass,
		   source,
		   new String[] {
			   "description", "Gets section with defined parameter. If this section doesn\'t exist, then error is returned.",
			   "returns", "section",
			   "recorded", "true",
			   "example", "get-editor CheckSetCommand |  get-section Contexts | key-type \"M1+s\""
		   });
		addAnnotation
		  (getCellEClass,
		   source,
		   new String[] {
			   "description", "Gets cell with defined parameter. If this cell doesn\'t exist, then error is returned.",
			   "returns", "cell",
			   "recorded", "true",
			   "example", "get-editor context | get-section Parameters | get-table | get-cell -row 1 -column 1 | \n get-property text -raw | str  | log"
		   });
		addAnnotation
		  (clickEClass,
		   source,
		   new String[] {
			   "description", "Clicks on a control.",
			   "returns", "value of <code>control</code> parameter",
			   "recorded", "true",
			   "example", "get-editor TestSuite | get-section \"Test Cases\" | get-button \"Add Test Case\" | click"
		   });
		addAnnotation
		  (getClick_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (getClick_Default(),
		   source,
		   new String[] {
			   "description", "Specifies if clicking causes default selection.",
			   "default", "false"
		   });
		addAnnotation
		  (getClick_Arrow(),
		   source,
		   new String[] {
			   "description", "Specifies arrow button behavior.",
			   "default", "false"
		   });
		addAnnotation
		  (getClick_MetaKeys(),
		   source,
		   new String[] {
			   "description", "Plus-separated pressed metakeys. Example: ALT+SHIFT",
			   "default", ""
		   });
		addAnnotation
		  (doubleClickEClass,
		   source,
		   new String[] {
			   "description", "Double clicks on a control.",
			   "returns", "value of <code>control</code> parameter",
			   "recorded", "true",
			   "example", "get-view \"Q7 Explorer\" | get-tree | select \"TestProject/Test\" | double-click"
		   });
		addAnnotation
		  (getTextEClass,
		   source,
		   new String[] {
			   "description", "Gets text content of a control. If this text doesn\'t exist, then error is returned.",
			   "returns", "text content of a control",
			   "recorded", "true",
			   "example", "get-editor \"WorkbenchContext\" | get-control -kind \"Label\" | get-text | equals \"Name:\" | verify-true"
		   });
		addAnnotation
		  (getGetText_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (isDisabledEClass,
		   source,
		   new String[] {
			   "description", "Checks if the control is disabled or not",
			   "returns", "true if the control is disabled or false otherwise",
			   "recorded", "true",
			   "example", "get-menu \"File/Save\" | is-disabled | assert-true"
		   });
		addAnnotation
		  (getIsDisabled_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (isDisposedEClass,
		   source,
		   new String[] {
			   "description", "Checks if the control is disposed or not",
			   "returns", "true if the control is disposed or false otherwise",
			   "recorded", "true",
			   "example", "get-editor \"WorkbenchContext\" | is-disposed | verify-false"
		   });
		addAnnotation
		  (getIsDisposed_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (typeTextEClass,
		   source,
		   new String[] {
			   "description", "Types text to the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor \"Test scenario\" | get-section Script | get-editbox |  type-text \"wait 200\""
		   });
		addAnnotation
		  (getTypeText_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (getTypeText_Text(),
		   source,
		   new String[] {
			   "description", "Text to type."
		   });
		addAnnotation
		  (keyTypeEClass,
		   source,
		   new String[] {
			   "description", "Type key to the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor \"Test scenario\" | get-section Script | get-editbox |  key-type \"M1+s\""
		   });
		addAnnotation
		  (getKeyType_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (getKeyType_Key(),
		   source,
		   new String[] {
			   "description", "Key to type."
		   });
		addAnnotation
		  (getKeyType_Char(),
		   source,
		   new String[] {
			   "description", "Character that corresponds to the *key*.  In most cases this parameter is not necessary.",
			   "default", "calculated from the *key*"
		   });
		addAnnotation
		  (typeCommandKeyEClass,
		   source,
		   new String[] {
			   "description", "Type key to the control.",
			   "returns", "value of <code>control</code> parameter"
		   });
		addAnnotation
		  (getTypeCommandKey_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (setTextEClass,
		   source,
		   new String[] {
			   "description", "Sets text content of the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-window Properties |  get-combo -after [get-label \"Priority:\"] | set-text High"
		   });
		addAnnotation
		  (getSetText_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (getSetText_Text(),
		   source,
		   new String[] {
			   "description", "Text to set."
		   });
		addAnnotation
		  (setTextSelectionEClass,
		   source,
		   new String[] {
			   "description", "Sets the selection to the range specified by the given offset and length. If specified four parameters sets the selection by start line, offset in start line, end line and offset in end line.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor Q7Project | get-section Script | get-editbox |  set-text-selection -offset 0 -startLine 1 -endLine 4 -endOffset 1"
		   });
		addAnnotation
		  (getSetTextSelection_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (getSetTextSelection_Offset(),
		   source,
		   new String[] {
			   "description", "Zero-based selection starting position. Must be in <code>0..length-1</code> range"
		   });
		addAnnotation
		  (getSetTextSelection_Length(),
		   source,
		   new String[] {
			   "description", "Length of selection. Must not be less that zero."
		   });
		addAnnotation
		  (getSetTextSelection_StartLine(),
		   source,
		   new String[] {
			   "description", "For multiline text boxes the starting line of a selection. If set, then the <code>offset</code> value will be relative to given line."
		   });
		addAnnotation
		  (getSetTextSelection_EndLine(),
		   source,
		   new String[] {
			   "description", "For multiline textboxes the ending line of a selection."
		   });
		addAnnotation
		  (getSetTextSelection_EndOffset(),
		   source,
		   new String[] {
			   "description", "Selection end offset relative to <code>endLine</code>. Don\'t use this argument along with <code>length</code> argument."
		   });
		addAnnotation
		  (setTextOffsetEClass,
		   source,
		   new String[] {
			   "description", "Sets the text offset.\nFor specified line.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-window \"Q7 Control Panel - Test\" | get-editbox |  set-text-offset 8 0"
		   });
		addAnnotation
		  (getSetTextOffset_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (getSetTextOffset_Value(),
		   source,
		   new String[] {
			   "description", "Value of offset to be set. Must not be less than 0 and greater than the number of characters in the text control. "
		   });
		addAnnotation
		  (getSetTextOffset_Line(),
		   source,
		   new String[] {
			   "description", "Value of offset to be set. Must not be less than 0 and greater than the number of characters in the text control. "
		   });
		addAnnotation
		  (checkEClass,
		   source,
		   new String[] {
			   "description", "Checks the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-editor Wb | get-section Workbench] {\n    get-button \"Clear clipboard\" | check\n}"
		   });
		addAnnotation
		  (getCheck_Control(),
		   source,
		   new String[] {
			   "description", "Item of table, list or tree."
		   });
		addAnnotation
		  (uncheckEClass,
		   source,
		   new String[] {
			   "description", "Unchecks the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-editor Wb | get-section Workbench] {\n    get-button \"Close all modal dialogs\" | uncheck\n    get-button \"Clear clipboard\" | uncheck\n}"
		   });
		addAnnotation
		  (getUncheck_Control(),
		   source,
		   new String[] {
			   "description", "Item of table, list or tree."
		   });
		addAnnotation
		  (selectEClass,
		   source,
		   new String[] {
			   "description", "Selects items.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-view \"Q7 Explorer\" | get-tree | select \"TestQ7Prj/Test scenario\" | double-click"
		   });
		addAnnotation
		  (getSelect_Control(),
		   source,
		   new String[] {
			   "description", "Table, list or tree."
		   });
		addAnnotation
		  (getSelect_Items(),
		   source,
		   new String[] {
			   "description", "Path of item to select."
		   });
		addAnnotation
		  (getSelect_All(),
		   source,
		   new String[] {
			   "description", "If true, command selects all items matched by provided regular expression. For Example, <code>get-tree | select \"Foo.*/Bar.*\" -all</code> will select all items starting with \"Bar\", if their parents starts with \"Foo\"."
		   });
		addAnnotation
		  (getCellEdit_Control(),
		   source,
		   new String[] {
			   "description", "Item of table, list or tree."
		   });
		addAnnotation
		  (activateCellEditEClass,
		   source,
		   new String[] {
			   "description", "Activates cell editing.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor Test | get-section Preferences | get-tree |  select \"EXECUTION_TIMEOUT\" | activate-cell-edit -column 1"
		   });
		addAnnotation
		  (getActivateCellEdit_Column(),
		   source,
		   new String[] {
			   "description", ""
		   });
		addAnnotation
		  (getActivateCellEdit_Type(),
		   source,
		   new String[] {
			   "description", ""
		   });
		addAnnotation
		  (getActivateCellEdit_Button(),
		   source,
		   new String[] {
			   "description", ""
		   });
		addAnnotation
		  (applyCellEditEClass,
		   source,
		   new String[] {
			   "description", "Applies cell editing.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-editor context | get-section Parameters | get-table] {\n    select parameter1 | activate-cell-edit -column 1\n    get-editbox | set-text value\n    apply-cell-edit -deactivate\n}"
		   });
		addAnnotation
		  (cancelCellEditEClass,
		   source,
		   new String[] {
			   "description", "Cancels cell editing.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-editor context | get-section Parameters | get-table] {\n    select \"Add new parameter\" | activate-cell-edit\n    get-editbox | set-text \"this text won\'t be applied\"\n    cancel-cell-edit\n   }"
		   });
		addAnnotation
		  (deactivateCellEditEClass,
		   source,
		   new String[] {
			   "description", "Applies cell editing.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-editor context2 | get-section Parameters | get-table] {\n    select parameter | activate-cell-edit -column 1\n    get-editbox | set-text \"value\"\n    cancel-cell-edit\n    deactivate-cell-edit\n}"
		   });
		addAnnotation
		  (closeEClass,
		   source,
		   new String[] {
			   "description", "Closes the control.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-window \"Q7 Control Panel - Test\" | close"
		   });
		addAnnotation
		  (getClose_Control(),
		   source,
		   new String[] {
			   "description", "Window, view or editor."
		   });
		addAnnotation
		  (returnFromOsDialogEClass,
		   source,
		   new String[] {
			   "description", "Emulates result returning from native dialog.",
			   "returns", "nothing"
		   });
		addAnnotation
		  (getReturnFromOsDialog_Kind(),
		   source,
		   new String[] {
			   "description", "Must be one of followings: FILE_SELECTOR, FOLDER_SELECTOR, MESSAGE_BOX, FONT_DIALOG, COLOR"
		   });
		addAnnotation
		  (getReturnFromOsDialog_Result(),
		   source,
		   new String[] {
			   "description", "String presentation of returned value (platform specific)."
		   });
		addAnnotation
		  (waitUntilEclipseIsReadyEClass,
		   source,
		   new String[] {
			   "description", "Suspend execution until Eclipse is ready. Can only be used in the top-most scope of a Test or ECL Context (no loops, conditionals, procedures, etc.). Should be used after every command that restarts the AUT (i.e.: ",
			   "returns", "nothing",
			   "recorded", "true",
			   "example", "get-menu \"File/Restart\" | click\nwait-until-eclipse-is-ready\nget-view \"Q7 Explorer\" | get-tree | select Project"
		   });
		addAnnotation
		  (showContentAssistEClass,
		   source,
		   new String[] {
			   "description", "Shows content assist.",
			   "returns", "value of <code>control</code> parameter",
			   "recorded", "true",
			   "example", "with [get-editor t | get-section Script | get-text-viewer] {\n    set-caret-pos 1 1\n    type-text \"get-\"\n    show-content-assist\n}"
		   });
		addAnnotation
		  (getShowContentAssist_Control(),
		   source,
		   new String[] {
			   "description", "Must be editor."
		   });
		addAnnotation
		  (dragActionEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag action event.",
			   "returns", "value of <code>control</code> parameter"
		   });
		addAnnotation
		  (getDragAction_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (getDragAction_X(),
		   source,
		   new String[] {
			   "description", "X coordinate relative to left top corner of the control.",
			   "default", "0"
		   });
		addAnnotation
		  (getDragAction_Y(),
		   source,
		   new String[] {
			   "description", "Y coordinate relative to left top corner of the control.",
			   "default", "0"
		   });
		addAnnotation
		  (getDragAction_Button(),
		   source,
		   new String[] {
			   "description", "Mouse button pressed. Must be one of followings: none, button1, button2, button3.",
			   "default", "none"
		   });
		addAnnotation
		  (getDragAction_Mask(),
		   source,
		   new String[] {
			   "default", "0",
			   "description", "Plus-separated pressed metakeys. Example: ALT+SHIFT"
		   });
		addAnnotation
		  (getDragAction_Detail(),
		   source,
		   new String[] {
			   "description", "Kind of DND operation to perform: <code>copy</code>, <code>move</code>, <code>link</code>, <code>target-move</code> (move to non-SWT application) or <code>none</code> (if not applicable)."
		   });
		addAnnotation
		  (dragStartEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag start event.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-item \"Project/t.test\" | drag-start 23 5"
		   });
		addAnnotation
		  (dragEndEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag end event.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-view \"Project Explorer\" | get-tree] {\n    drag-end -detail copy\n    select \"Project/Folder/t.test\" | get-menu Delete | click\n}"
		   });
		addAnnotation
		  (dragEnterEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag enter event.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-item \"Project/t.test\" | drag-enter 23 11 -detail copy"
		   });
		addAnnotation
		  (dragExitEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag exit event.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-item \"Project/t.test\" | drag-exit"
		   });
		addAnnotation
		  (dragSetDataEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag set event.",
			   "returns", "value of <code>control</code> parameter"
		   });
		addAnnotation
		  (dragAcceptEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag accept event.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-item \"Project/Folder\" | drag-accept 18 11 -detail copy"
		   });
		addAnnotation
		  (dragDetectEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag detect event.",
			   "returns", "value of <code>control</code> parameter"
		   });
		addAnnotation
		  (dragOverEClass,
		   source,
		   new String[] {
			   "description", "Emulates drag over event.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "with [get-view \"Project Explorer\" | get-tree] {\n    select \"Project/t.test\"\n    get-item \"Project/t.test\" | drag-start 23 5\n    get-item \"Project/t.test\" | drag-enter 23 11 -detail copy\n    get-item \"Project/t.test\" | drag-over 23 11 -detail copy\n    get-item \"Project/q7.properties\" | drag-over 21 2 -detail copy\n    drag-over 70 84 -detail copy\n    get-item \"Project/file.csv\" | drag-over 19 0 -detail copy\n    get-item \"Project/context2.ctx\" | drag-over 18 0 -detail copy\n    drag-over 69 33 -detail copy\n    get-item \"Project/Folder\" | drag-over 18 11 -detail copy\n    get-item \"Project/t.test\" | drag-exit\n    get-item \"Project/Folder\" | drag-accept 18 11 -detail copy\n    drag-set-data\n    get-item \"Project/Folder\" | drop 18 11 -detail copy\n}"
		   });
		addAnnotation
		  (dropEClass,
		   source,
		   new String[] {
			   "description", "Emulates drop event.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor t | get-section Contexts | get-table | drop 111 52 -detail move"
		   });
		addAnnotation
		  (getContainsImage_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (getGetRegionText_Control(),
		   source,
		   new String[] {
			   "description", "Any control is appropriate."
		   });
		addAnnotation
		  (getGetRegionText_X(),
		   source,
		   new String[] {
			   "description", "X coordinate relative to left top corner of the control.",
			   "default", "0"
		   });
		addAnnotation
		  (getGetRegionText_Y(),
		   source,
		   new String[] {
			   "description", "Y coordinate relative to left top corner of the control.",
			   "default", "0"
		   });
		addAnnotation
		  (getAboutMenuEClass,
		   source,
		   new String[] {
			   "description", "Returns the \"About\" menu.",
			   "returns", "About menu\r\n",
			   "recorded", "true",
			   "example", "get-about-menu | click\nget-window \"About Q7\" | get-button OK | click"
		   });
		addAnnotation
		  (getPreferencesMenuEClass,
		   source,
		   new String[] {
			   "description", "Returns the \"Preferences\" menu.",
			   "returns", "Preferences menu",
			   "recorded", "true",
			   "example", "get-preferences-menu | click\nwith [get-window Preferences] {\n    get-tree | select \"General/Content Types\"\n    get-button Cancel | click\n}"
		   });
		addAnnotation
		  (getDateTimeEClass,
		   source,
		   new String[] {
			   "description", "Gets pop-up window of calendar. If this pop-up window doesn\'t exist, then error is returned.",
			   "returns", "calendar pop-up window ",
			   "recorded", "true"
		   });
		addAnnotation
		  (getSliderEClass,
		   source,
		   new String[] {
			   "description", "",
			   "returns", "",
			   "recorded", "true"
		   });
		addAnnotation
		  (setValueEClass,
		   source,
		   new String[] {
			   "description", "Set value of the control.",
			   "returns", "",
			   "example", "get-combo -after [get-label \"Mouse move recording mode\"] | set-value Never"
		   });
		addAnnotation
		  (getSetValue_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (getSetValue_Value(),
		   source,
		   new String[] {
			   "description", "Text to set."
		   });
		addAnnotation
		  (minimizeEClass,
		   source,
		   new String[] {
			   "description", "Minimize the tab folder.\nSee more details about <a href=\"http://xored.freshdesk.com/solution/categories/58375/folders/95402/articles/3000008200-resize-a\">How to resize a window.</a>",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-window \"New Project\" | minimize"
		   });
		addAnnotation
		  (getMinimize_Control(),
		   source,
		   new String[] {
			   "description", "Window, view or editor."
		   });
		addAnnotation
		  (maximizeEClass,
		   source,
		   new String[] {
			   "description", "Maximize the tab folder.\nSee more details about <a href=\"http://xored.freshdesk.com/solution/categories/58375/folders/95402/articles/3000008200-resize-a\">How to resize a window.</a>",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-window \"New Project\" | maximize\n\n"
		   });
		addAnnotation
		  (getMaximize_Control(),
		   source,
		   new String[] {
			   "description", "Window, view or editor."
		   });
		addAnnotation
		  (restoreEClass,
		   source,
		   new String[] {
			   "description", "Restore the tab folder.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-view \"Q7 Explorer\" | minimize\nget-view \"Q7 Explorer\" | restore"
		   });
		addAnnotation
		  (getRestore_Control(),
		   source,
		   new String[] {
			   "description", "Window, view or editor."
		   });
		addAnnotation
		  (showTabListEClass,
		   source,
		   new String[] {
			   "description", "Show the list of tabs from tab folder.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-editor SO | show-tab-list "
		   });
		addAnnotation
		  (getShowTabList_Control(),
		   source,
		   new String[] {
			   "description", "Window, view or editor."
		   });
		addAnnotation
		  (getOptions_Command(),
		   source,
		   new String[] {
			   "description", "Command to execute."
		   });
		addAnnotation
		  (getExecWithOptions_Command(),
		   source,
		   new String[] {
			   "description", "Command to execute."
		   });
		addAnnotation
		  (setDialogResultEClass,
		   source,
		   new String[] {
			   "description", "Emulates result returning from native dialog.",
			   "returns", "nothing",
			   "example", "set-dialog-result File \"/Users/Ulik_MacAir/Downloads/MyLicense.license\"\nget-menu \"File/Open File...\" | click"
		   });
		addAnnotation
		  (getSetDialogResult_Kind(),
		   source,
		   new String[] {
			   "description", "Must be one of followings: File, Folder, MessageBox, Font, Color"
		   });
		addAnnotation
		  (getSetDialogResult_Result(),
		   source,
		   new String[] {
			   "description", "String presentation of returned value (platform specific)."
		   });
		addAnnotation
		  (getByOsEClass,
		   source,
		   new String[] {
			   "description", "<p>Returns value depending on current operating system. This allows to create cross-platform tests in cases when assertion values slightly differ on various operating system &ndash; this command can be used as an argument for commands like <code>equals</code>:</p>\r\n<pre>\r\n... | get-property \"text\" | equals [\r\n      get-by-os -macosx \"Mac value\" -win \"Windows value\" -linux \"Linux value\"\r\n    ] | verify-true\r\n</pre>\r\n<p>When two platforms have the same value, it is convenient to use <code>-default</code> argument. For example, if some value is the same on Linux and Mac OS X, but differs on Windows, <code>get-by-os</code> can be used like this:</p>\r\n<pre>\r\n  get-by-os -win \"Windows value\" -default \"Mac OS X and Linux value\"\r\n</pre>\r\n",
			   "returns", "Value of an argument corresponding to current platform, or value of <code>default</code> argument."
		   });
		addAnnotation
		  (getGetByOs_Default(),
		   source,
		   new String[] {
			   "description", "Value to use when no platform-specific argument specified"
		   });
		addAnnotation
		  (getGetByOs_Win(),
		   source,
		   new String[] {
			   "description", "Value to use on Windows"
		   });
		addAnnotation
		  (getGetByOs_Linux(),
		   source,
		   new String[] {
			   "description", "Value to use on Linux"
		   });
		addAnnotation
		  (getGetByOs_Macosx(),
		   source,
		   new String[] {
			   "description", "Value to use on Mac OS X"
		   });
		addAnnotation
		  (hoverAtTextOffsetEClass,
		   source,
		   new String[] {
			   "description", "Perform mouse hover at specific text position.",
			   "returns", "value of <code>control</code> parameter"
		   });
		addAnnotation
		  (getHoverAtTextOffset_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (getHoverAtTextOffset_Offset(),
		   source,
		   new String[] {
			   "description", "Value of offset to be set. Must not be less than 0 and greater than the number of characters in the text control. "
		   });
		addAnnotation
		  (getHoverAtTextOffset_Line(),
		   source,
		   new String[] {
			   "description", "Value of offset to be set. Must not be less than 0 and greater than the number of characters in the text control. "
		   });
		addAnnotation
		  (getTextViewerEClass,
		   source,
		   new String[] {
			   "description", "Gets the text viewer. If text viewer doesn\'t exist, then error is returned.",
			   "returns", "text viewer",
			   "recorded", "true",
			   "example", "with [get-section Script | get-text-viewer] {\n        set-caret-pos 1 20\n        type-text MyText\n    }"
		   });
		addAnnotation
		  (selectRangeEClass,
		   source,
		   new String[] {
			   "description", "Sets the selection to the range specified by the given offset and length. If specified four parameters sets the selection by start line, offset in start line, end line and offset in end line.",
			   "returns", "value of *control* parameter",
			   "example", "with [get-editor \"file.txt\" | get-text-viewer] {\n    select-range 1 2 5 8\n    get-menu Cut | click\n}"
		   });
		addAnnotation
		  (getSelectRange_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (setCaretPosEClass,
		   source,
		   new String[] {
			   "description", "Sets the text offset.\nFor specified line.",
			   "returns", "value of *control* parameter",
			   "example", "with [get-editor \"q7enterprise.license\" | get-text-viewer] {\n    set-caret-pos 3 5\n}"
		   });
		addAnnotation
		  (getSetCaretPos_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (hoverTextEClass,
		   source,
		   new String[] {
			   "description", "Perform mouse hover at specific text position.",
			   "returns", "value of *control* parameter"
		   });
		addAnnotation
		  (getHoverText_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (getHoverText_With(),
		   source,
		   new String[] {
			   "desciption", "Modfier keys joined by \'+\', for example \'M1+M2\', or \'M3\'. "
		   });
		addAnnotation
		  (openDeclarationEClass,
		   source,
		   new String[] {
			   "description", "Perform open declaration command in the current text position (CTRL+Left click)",
			   "returns", "value of *control* parameter",
			   "example", "with [get-editor t | get-section Script | get-text-viewer] {\n    \tset-caret-pos 4 3\n\topen-declaration\n}"
		   });
		addAnnotation
		  (getOpenDeclaration_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (getVerticalRulerEClass,
		   source,
		   new String[] {
			   "description", "Gets the vertical ruler of editor.",
			   "returns", "vertical ruler",
			   "recorded", "true"
		   });
		addAnnotation
		  (getLeftRulerEClass,
		   source,
		   new String[] {
			   "description", "Gets the left ruler of editor.",
			   "returns", "left ruler",
			   "recorded", "true",
			   "example", "get-editor CreateProject | get-section Script | get-left-ruler | get-ruler-column -index 1 | click-ruler -line 4"
		   });
		addAnnotation
		  (getRulerColumnEClass,
		   source,
		   new String[] {
			   "description", "Gets the ruler column of the editor.",
			   "returns", "ruler column",
			   "recorded", "true",
			   "example", "get-editor CreateProject | get-section Script | get-left-ruler | get-ruler-column -index 1 | click-ruler -line 4"
		   });
		addAnnotation
		  (getRightRulerEClass,
		   source,
		   new String[] {
			   "description", "Gets the right ruler of editor.",
			   "returns", "right ruler",
			   "recorded", "true"
		   });
		addAnnotation
		  (clickRulerEClass,
		   source,
		   new String[] {
			   "description", "Clicks a ruler at given position with specified button and modifiers",
			   "returns", "value of control parameter",
			   "example", "with [get-left-ruler | get-ruler-column AnnotationColumn] {\n        click-ruler -line 2\n    }"
		   });
		addAnnotation
		  (getClickRuler_Control(),
		   source,
		   new String[] {
			   "description", "Editor ruler"
		   });
		addAnnotation
		  (getClickRuler_Button(),
		   source,
		   new String[] {
			   "desciption", "Mouse button: Left, Right or Middle"
		   });
		addAnnotation
		  (getClickRuler_With(),
		   source,
		   new String[] {
			   "desciption", "Modfier keys joined by \'+\', for example \'M1+M2\', or \'M3\'. "
		   });
		addAnnotation
		  (doubleClickRulerEClass,
		   source,
		   new String[] {
			   "description", "Double clicks ruler on given line with specified modifiers and mouse button",
			   "returns", "value of control parameter",
			   "example", "with [get-editor t | get-section Script | get-left-ruler | get-ruler-column AnnotationColumn] {\n    double-click-ruler 1\n}"
		   });
		addAnnotation
		  (getDoubleClickRuler_Control(),
		   source,
		   new String[] {
			   "description", "Editor ruler"
		   });
		addAnnotation
		  (getDoubleClickRuler_Button(),
		   source,
		   new String[] {
			   "desciption", "Mouse button: Left, Right or Middle"
		   });
		addAnnotation
		  (getDoubleClickRuler_With(),
		   source,
		   new String[] {
			   "desciption", "Modfier keys joined by \'+\', for example \'M1+M2\', or \'M3\'. "
		   });
		addAnnotation
		  (hoverRulerEClass,
		   source,
		   new String[] {
			   "description", "Perform mouse hover at specific text position.",
			   "returns", "value of *control* parameter"
		   });
		addAnnotation
		  (getHoverRuler_Control(),
		   source,
		   new String[] {
			   "description", "Text control: editbox, editor or combo."
		   });
		addAnnotation
		  (getHoverRuler_With(),
		   source,
		   new String[] {
			   "desciption", "Modfier keys joined by \'+\', for example \'M1+M2\', or \'M3\'. "
		   });
		addAnnotation
		  (clickLinkEClass,
		   source,
		   new String[] {
			   "description", "Clicks on a link.",
			   "returns", "value of <code>control</code> parameter",
			   "recorded", "true",
			   "example", "with [get-window Preferences] {\n    get-tree | select \"Ant/Editor\"\n    get-link \"See <a href=\\\"org.eclipse.ui.preferencePages.GeneralTextEditor\\\">\\\'Text Editors\\\'</a> for general text editor preferences and <a href=\\\"org.eclipse.ui.preferencePages.ColorsAndFonts\\\">\\\'Colors and Fonts\\\'</a> to configure the font.\" \n        | click-link \"org.eclipse.ui.preferencePages.GeneralTextEditor\"\n    get-button OK | click\n}"
		   });
		addAnnotation
		  (expandEClass,
		   source,
		   new String[] {
			   "description", "Expands a tree item. Item must be acquired by using <code>get-item</code> command. ",
			   "returns", "value of <code>control</code> parameter",
			   "recorded", "false",
			   "example", "get-view \"Q7 Explorer\" | get-tree | get-item Project | expand"
		   });
		addAnnotation
		  (collapseEClass,
		   source,
		   new String[] {
			   "description", "Collapses a tree item. Item must be acquired by using <code>get-item</code> command. ",
			   "returns", "value of <code>control</code> parameter",
			   "recorded", "false",
			   "example", "get-view \"Q7 Explorer\" | get-tree | get-item \"Project/folder\" | collapse"
		   });
		addAnnotation
		  (setFocusEClass,
		   source,
		   new String[] {
			   "description", "Sets the focus in specified editbox.",
			   "returns", "Nothing",
			   "recorded", "true",
			   "example", "get-view \"Q7 Testing\" | get-section \"CellEdit Tree Test\" | get-tree | get-editbox | set-focus"
		   });
		addAnnotation
		  (getTableDataEClass,
		   source,
		   new String[] {
			   "description", "Takes a data from table or tree and returns <code>Table</code> EMF object. When table or tree does not have columns, then it is assumed that there\'s one column named \'text\'.",
			   "returns", "<code>Table</code> object",
			   "recorded", "false",
			   "example", "get-table | get-table-data | write-csv-file \"workspace:/project/file.csv\""
		   });
		addAnnotation
		  (getGetTableData_IncludeChecked(),
		   source,
		   new String[] {
			   "description", "When true, adds a column named \'checked\' which contains <code>true</code> or <code>false</code> depending on checked state of row"
		   });
		addAnnotation
		  (getGetTableData_ExcludeHidden(),
		   source,
		   new String[] {
			   "description", "When true, output table does not include columns with width=0"
		   });
		addAnnotation
		  (clickColumnEClass,
		   source,
		   new String[] {
			   "description", "Clicks on a header of table or tree column. The column is identified by <code>name</code> and <code>index</code>. If name is empty, then column is identified by zero-based index. If there is more than one column with a given name, then <code>index</code> can be used. In this case <code>index</code> is used to identify the column within all other columns with the same name. If there are no columns with given name, then command tries to interpret <code>name</code> as Java regular expression pattern and search for a column. ",
			   "returns", "<code>Table</code> object or <code>Tree</code> control",
			   "recorded", "true",
			   "example", "get-table | click-column Description //clicks on a header of column Description\nget-table | click-column -index 5 //clicks on 6th column header (since index is 0-based)\nget-table | click-column -name \"D.*\" -index 1 //clicks on a second column starting with \"D\" "
		   });
		addAnnotation
		  (getClickColumn_Name(),
		   source,
		   new String[] {
			   "desciption", "Column name or regular expression pattern. When empty, column is identified by <code>index</code>."
		   });
		addAnnotation
		  (getClickColumn_Index(),
		   source,
		   new String[] {
			   "desciption", "Index of a column within all columns matching a given name. When <code>name</code> is empty, this is a column index in table or tree"
		   });
		addAnnotation
		  (traceEClass,
		   source,
		   new String[] {
			   "description", "Appends a tracing message to Q7 report collected during execution, so that it can be used later in report renderer.",
			   "example", "trace \"Project created\"\ntrace [get-view \"Package Explorer\" | get-tree | get-item \"Other Projects/sample\" | get-property \"getData().isOpen()\" -raw]"
		   });
		addAnnotation
		  (mouseEClass,
		   source,
		   new String[] {
			   "description", "Sends a mouse event to control. Returns the same control, so that mouse commands can be chained: <pre>... | get-text \"Foo\" | mouse down -button Right | mouse up -buton Right</pre>",
			   "returns", "value of control parameter"
		   });
		addAnnotation
		  (getMouse_Event(),
		   source,
		   new String[] {
			   "description", "Mouse event: <ul><li>up</li><li>down</li><li>move</li><li>enter</li><li>exit</li><li>hover</li><li>double-click</li></ul>"
		   });
		addAnnotation
		  (getMouse_Button(),
		   source,
		   new String[] {
			   "description", "Mouse button: Left, Right or Middle"
		   });
		addAnnotation
		  (getMouse_With(),
		   source,
		   new String[] {
			   "description", "Modfier keys and holded mouse buttons joined by \'+\', for example \'M1+M2\', or \'M3\'. "
		   });
		addAnnotation
		  (getMouse_Count(),
		   source,
		   new String[] {
			   "description", "The \'count\' to be set in mouse event. When omitted, reasonable default is used"
		   });
		addAnnotation
		  (getMouse_X(),
		   source,
		   new String[] {
			   "description", "The \'x\' coordinate of mouse event. Can be omitted"
		   });
		addAnnotation
		  (getMouse_Y(),
		   source,
		   new String[] {
			   "description", "The \'y\' coordinate of mouse event. Can be omitted"
		   });
		addAnnotation
		  (getObjectEClass,
		   source,
		   new String[] {
			   "description", "Gets interop object from UI control.",
			   "returns", "Interop object.",
			   "example", "get-view \"Package Explorer\" | get-tree | get-object | my-custom-tree-command"
		   });
		addAnnotation
		  (getGetObject_Object(),
		   source,
		   new String[] {
			   "description", "UI control to get object for."
		   });
		addAnnotation
		  (getItemsEClass,
		   source,
		   new String[] {
			   "description", "Gets item list of a table, or visible items of a tree, or visible children of a tree item.",
			   "returns", "List of items.",
			   "example", "get-view \"Q7 Explorer\" | get-tree | get-items | foreach {get-property caption -raw | str | log}"
		   });
		addAnnotation
		  (expandAllEClass,
		   source,
		   new String[] {
			   "description", "Expands a tree item and all its children, or a whole tree.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-view \"Q7 Explorer\" | get-tree | get-item Project | expand-all"
		   });
		addAnnotation
		  (collapseAllEClass,
		   source,
		   new String[] {
			   "description", "Collapses a tree item and all its children, or a whole tree.",
			   "returns", "value of <code>control</code> parameter",
			   "example", "get-view \"Q7 Explorer\" | get-tree | collapse-all"
		   });
		addAnnotation
		  (takeScreenshotEClass,
		   source,
		   new String[] {
			   "description", "Takes a screenshot and attaches it to test report.\nNote: please make sure you enabled \"Include \'trace\' and \'take-screenshot\' in HTML report\" option (Q7 Advanced options/Reporting). ",
			   "returns", "Nothing."
		   });
		addAnnotation
		  (getTakeScreenshot_Message(),
		   source,
		   new String[] {
			   "description", "Optional message to be attached to the screenshot."
		   });
		addAnnotation
		  (selectItemEClass,
		   source,
		   new String[] {
			   "description", "Takes a table or tree item and sets it as a selection of tree or table. Usage: <pre>get-tree | get-item \"foo\" | select-item</pre>",
			   "returns", "input item"
		   });
		addAnnotation
		  (clickTextEClass,
		   source,
		   new String[] {
			   "description", "<p>Clicks on editbox to set cursor position (when only <code>start</code> argument specified) or text selection (when both <code>start</code> and <code>end</code> are set). When no arguments given, sets cursor in the beginning of an editbox. More verbosely, it does the following:</p>\r\n<ol>\r\n  <li>Sets focus to editbox if not yet focused</li>\r\n  <li>Sends mouse down and up events</li>\r\n  <li>Sets caret position or text selection based on arguments</li>\r\n</ol>\r\n<p>\r\n  <code>start</code> and <code>end</code> arguments have different forms depending on editbox kind:\r\n  <ul>\r\n    <li>For single line editboxes &ndash; 1-based indices in text (e.g. <code>1</code>, <code>5</code>)</li>\r\n    <li>For multi line editboxes &ndash; 1-based line and colum indices separated by colon (e.g <code>1:32</code>, <code>30:1</code>).</li>\r\n  </ul>\r\n</p>",
			   "returns", "input item",
			   "example", "with [get-editbox -after [get-label \"Location:\"]] {\n        click-text 3\n    }"
		   });
		addAnnotation
		  (getClickText_Start(),
		   source,
		   new String[] {
			   "description", "Cursor location when <code>end</code> is not set or selection start otherwise"
		   });
		addAnnotation
		  (getClickText_End(),
		   source,
		   new String[] {
			   "description", "Selection end"
		   });
		addAnnotation
		  (getClickText_Button(),
		   source,
		   new String[] {
			   "description", "Mouse button: left, right or middle."
		   });
		addAnnotation
		  (getQuickAccessEClass,
		   source,
		   new String[] {
			   "description", "Gets Eclipse 4 quick access text box.",
			   "returns", "quick access text box",
			   "recorded", "true",
			   "example", "get-quick-access | set-text \"Open Resource\""
		   });
		addAnnotation
		  (getColumnHeaderEClass,
		   source,
		   new String[] {
			   "description", "Gets column of a table or tree control.",
			   "returns", "column",
			   "example", "get-table | get-column-header Name | click // sorts by Name column",
			   "recorded", "true"
		   });
		addAnnotation
		  (setPositionEClass,
		   source,
		   new String[] {
			   "description", "Sets position of a table column.",
			   "returns", "value of <code>control</code> parameter",
			   "recorded", "false",
			   "example", "get-nebula-grid | get-column-header Name | set-position 4"
		   });
		addAnnotation
		  (setWidthEClass,
		   source,
		   new String[] {
			   "description", "Sets width of a table column.",
			   "returns", "value of <code>control</code> parameter",
			   "recorded", "false",
			   "example", "get-nebula-grid | get-column-header Name | set-width 256"
		   });
		addAnnotation
		  (getPropertyTabEClass,
		   source,
		   new String[] {
			   "description", "Gets the tab of the Properties View.",
			   "returns", "The tab of the Properties View.",
			   "example", "with [get-view Properties] {\n    get-property-tab Description | click\n}"
		   });
		addAnnotation
		  (showAlertEClass,
		   source,
		   new String[] {
			   "description", "Displays a message.",
			   "returns", "Nothing.",
			   "example", "show-alert \"Hello!\""
		   });
		addAnnotation
		  (getShowAlert_Message(),
		   source,
		   new String[] {
			   "description", "Message to display."
		   });
		addAnnotation
		  (doubleClickTextEClass,
		   source,
		   new String[] {
			   "description", "<p>Double-clicks on editbox to set cursor position and/or selection. More verbosely, it does the following:</p>\r\n<ol>\r\n  <li>Sets focus to editbox if not yet focused</li>\r\n  <li>Sends double-click event</li>\r\n</ol>\r\n<p>\r\n  <code>position</code> argument have different form depending on editbox kind:\r\n  <ul>\r\n    <li>For single line editboxes &ndash; 1-based indices in text (e.g. <code>1</code>, <code>5</code>)</li>\r\n    <li>For multi line editboxes &ndash; 1-based line and colum indices separated by colon (e.g <code>1:32</code>, <code>30:1</code>).</li>\r\n  </ul>\r\n</p>",
			   "returns", "input item",
			   "example", "with [get-editbox -after [get-label \"Project name:\"]] {\n        set-text Project1\n        double-click-text 9 1\n    }"
		   });
		addAnnotation
		  (getDoubleClickText_Position(),
		   source,
		   new String[] {
			   "description", "Cursor location where the double-click will occur."
		   });
		addAnnotation
		  (getDoubleClickText_Button(),
		   source,
		   new String[] {
			   "description", "Mouse button: left, right or middle."
		   });
		addAnnotation
		  (toControlHandleEClass,
		   source,
		   new String[] {
			   "description", "Gets UI control handle from widget.",
			   "returns", "UI control handle."
		   });
		addAnnotation
		  (getToControlHandle_Widget(),
		   source,
		   new String[] {
			   "description", "Widget to get UI control for."
		   });
		addAnnotation
		  (unfocusEClass,
		   source,
		   new String[] {
			   "description", "Sets the focus in specified editbox.",
			   "returns", "Nothing",
			   "recorded", "true",
			   "example", "get-view \"Q7 Testing\" | get-section \"CellEdit Tree Test\" | get-tree | get-editbox | set-focus"
		   });
		addAnnotation
		  (decryptEClass,
		   source,
		   new String[] {
			   "description", "Decrypts an encrypted string",
			   "returns", "Decrypted string",
			   "recorded", "true"
		   });
		addAnnotation
		  (restartAutEClass,
		   source,
		   new String[] {
			   "description", "Restarts AUT.\nCan be used if there in no File/Restart option in AUT.\n\nPlease note that <a href=\"#wait-until-eclipse-is-ready\">wait-until-eclipse-is-ready</a> command should be used \nimmidiately after restart-aut command. ",
			   "example", "get-view \"Package Explorer\" | get-tree | get-menu \"New/Java Project\" | click\nwith [get-window \"New Java Project\"] {\n    with [get-editbox -after [get-label \"Project name:\"]] {\n        set-text MyProjec\n        set-text MyProject\n    }\n    get-button Finish | click\n}\n\nrestart-aut\n\nwait-until-eclipse-is-ready\n\nget-view \"Package Explorer\" | get-tree | get-property itemCount | equals 1 | verify-true"
		   });
		addAnnotation
		  (getLastMessageBoxEClass,
		   source,
		   new String[] {
			   "description", "Gets last shown MessageBox info. If MessageBox was not shown, then error is returned.",
			   "returns", "MessageBoxInfo",
			   "example", "set-dialog-result MessageBox 128\r\nget-view \"Q7 Quality Mockups\" | get-group \"MessageBox Test\" | get-button \"Message Box with YES/NO Buttons\" | click\r\nwith [get-last-message-box] {\r\n    get-property title | equals \"Warning\" | verify-true\r\n    get-property message | equals \"Yes or No?\" | verify-true\r\n}"
		   });
		addAnnotation
		  (getTestCaseNameEClass,
		   source,
		   new String[] {
			   "description", "Returns current test case name",
			   "returns", "Current test case name",
			   "example", "get-test-case-name | log"
		   });
		addAnnotation
		  (getComboItemsEClass,
		   source,
		   new String[] {
			   "description", "Gets combo box items and writes them into output pipe",
			   "returns", "combo box items",
			   "recorded", "false",
			   "example", "get-combo | get-combo-items | to-list | each [val item] {\n\tlog $item\n}"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/input</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createInputAnnotations() {
		String source = "http://www.eclipse.org/ecl/input";
		addAnnotation
		  (getGetProperty_Object(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getVerifyTrue_Condition(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getVerifyFalse_Condition(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getEquals_Input(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getContains_Input(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getMatches_Input(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getIsEmpty_Input(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSelector_Parent(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getClick_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getGetText_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getIsDisabled_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getIsDisposed_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getTypeText_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getKeyType_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getTypeCommandKey_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSetText_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSetTextSelection_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSetTextOffset_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getCheck_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getUncheck_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSelect_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getCellEdit_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getClose_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getShowContentAssist_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getDragAction_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getContainsImage_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getGetRegionText_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSetValue_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getMinimize_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getMaximize_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getRestore_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getShowTabList_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getHoverAtTextOffset_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSelectRange_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSetCaretPos_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getHoverText_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getOpenDeclaration_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getClickRuler_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getDoubleClickRuler_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getHoverRuler_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getControlCommand_Control(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getGetObject_Object(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getShowAlert_Message(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getToControlHandle_Widget(),
		   source,
		   new String[] {
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/internal</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createInternalAnnotations() {
		String source = "http://www.eclipse.org/ecl/internal";
		addAnnotation
		  (getSelector_Id(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (returnFromOsDialogEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getAdvancedInfoEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (optionsEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (execWithOptionsEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (shutdownAutEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getPropertyNodesEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (fromRawKeyEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getWidgetDetailsEClass,
		   source,
		   new String[] {
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/depricated</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createDepricatedAnnotations() {
		String source = "http://www.eclipse.org/ecl/depricated";
		addAnnotation
		  (getClick_Nowait(),
		   source,
		   new String[] {
			   "description", "Specifies if execution must wait for all UI jobs to complete after clicking.",
			   "default", "false"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/meta</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createMetaAnnotations() {
		String source = "http://www.eclipse.org/ecl/meta";
		addAnnotation
		  (getSelect_Items(),
		   source,
		   new String[] {
			   "type", "string | org.eclipse.rcptt.tesla.ecl.model.ControlHandler"
		   });
	}

} //TeslaPackageImpl
