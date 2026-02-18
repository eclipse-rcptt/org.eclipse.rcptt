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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
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
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaPackage
 * @generated
 */
public class TeslaAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static TeslaPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TeslaAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = TeslaPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TeslaSwitch<Adapter> modelSwitch =
		new TeslaSwitch<Adapter>() {
			@Override
			public Adapter caseWait(Wait object) {
				return createWaitAdapter();
			}
			@Override
			public Adapter caseSetupPlayer(SetupPlayer object) {
				return createSetupPlayerAdapter();
			}
			@Override
			public Adapter caseShoutdownPlayer(ShoutdownPlayer object) {
				return createShoutdownPlayerAdapter();
			}
			@Override
			public Adapter caseTeslaProcessStatus(TeslaProcessStatus object) {
				return createTeslaProcessStatusAdapter();
			}
			@Override
			public Adapter caseWrapper(Wrapper object) {
				return createWrapperAdapter();
			}
			@Override
			public Adapter caseGetProperty(GetProperty object) {
				return createGetPropertyAdapter();
			}
			@Override
			public Adapter caseVerifyTrue(VerifyTrue object) {
				return createVerifyTrueAdapter();
			}
			@Override
			public Adapter caseVerifyFalse(VerifyFalse object) {
				return createVerifyFalseAdapter();
			}
			@Override
			public Adapter caseVerifyHandler(VerifyHandler object) {
				return createVerifyHandlerAdapter();
			}
			@Override
			public Adapter caseVerifyError(org.eclipse.rcptt.tesla.ecl.model.VerifyError object) {
				return createVerifyErrorAdapter();
			}
			@Override
			public Adapter caseEquals(Equals object) {
				return createEqualsAdapter();
			}
			@Override
			public Adapter caseContains(Contains object) {
				return createContainsAdapter();
			}
			@Override
			public Adapter caseMatches(Matches object) {
				return createMatchesAdapter();
			}
			@Override
			public Adapter caseIsEmpty(IsEmpty object) {
				return createIsEmptyAdapter();
			}
			@Override
			public Adapter caseUnsupported(Unsupported object) {
				return createUnsupportedAdapter();
			}
			@Override
			public Adapter caseControlNotFound(ControlNotFound object) {
				return createControlNotFoundAdapter();
			}
			@Override
			public Adapter caseControlHandler(ControlHandler object) {
				return createControlHandlerAdapter();
			}
			@Override
			public Adapter caseGetEclipseWindow(GetEclipseWindow object) {
				return createGetEclipseWindowAdapter();
			}
			@Override
			public Adapter caseSelector(Selector object) {
				return createSelectorAdapter();
			}
			@Override
			public Adapter casePathSelector(PathSelector object) {
				return createPathSelectorAdapter();
			}
			@Override
			public Adapter caseTextSelector(TextSelector object) {
				return createTextSelectorAdapter();
			}
			@Override
			public Adapter caseGetControl(GetControl object) {
				return createGetControlAdapter();
			}
			@Override
			public Adapter caseGetButton(GetButton object) {
				return createGetButtonAdapter();
			}
			@Override
			public Adapter caseGetCanvas(GetCanvas object) {
				return createGetCanvasAdapter();
			}
			@Override
			public Adapter caseGetCheckbox(GetCheckbox object) {
				return createGetCheckboxAdapter();
			}
			@Override
			public Adapter caseGetCombo(GetCombo object) {
				return createGetComboAdapter();
			}
			@Override
			public Adapter caseGetEditbox(GetEditbox object) {
				return createGetEditboxAdapter();
			}
			@Override
			public Adapter caseGetGroup(GetGroup object) {
				return createGetGroupAdapter();
			}
			@Override
			public Adapter caseGetItem(GetItem object) {
				return createGetItemAdapter();
			}
			@Override
			public Adapter caseGetLabel(GetLabel object) {
				return createGetLabelAdapter();
			}
			@Override
			public Adapter caseGetLink(GetLink object) {
				return createGetLinkAdapter();
			}
			@Override
			public Adapter caseGetList(GetList object) {
				return createGetListAdapter();
			}
			@Override
			public Adapter caseGetMenu(GetMenu object) {
				return createGetMenuAdapter();
			}
			@Override
			public Adapter caseGetTabFolder(GetTabFolder object) {
				return createGetTabFolderAdapter();
			}
			@Override
			public Adapter caseGetTabItem(GetTabItem object) {
				return createGetTabItemAdapter();
			}
			@Override
			public Adapter caseGetTable(GetTable object) {
				return createGetTableAdapter();
			}
			@Override
			public Adapter caseGetToolbar(GetToolbar object) {
				return createGetToolbarAdapter();
			}
			@Override
			public Adapter caseGetTree(GetTree object) {
				return createGetTreeAdapter();
			}
			@Override
			public Adapter caseGetWindow(GetWindow object) {
				return createGetWindowAdapter();
			}
			@Override
			public Adapter caseGetView(GetView object) {
				return createGetViewAdapter();
			}
			@Override
			public Adapter caseGetEditor(GetEditor object) {
				return createGetEditorAdapter();
			}
			@Override
			public Adapter caseGetSection(GetSection object) {
				return createGetSectionAdapter();
			}
			@Override
			public Adapter caseGetBanner(GetBanner object) {
				return createGetBannerAdapter();
			}
			@Override
			public Adapter caseGetCoolbar(GetCoolbar object) {
				return createGetCoolbarAdapter();
			}
			@Override
			public Adapter caseGetColorSelector(GetColorSelector object) {
				return createGetColorSelectorAdapter();
			}
			@Override
			public Adapter caseGetCell(GetCell object) {
				return createGetCellAdapter();
			}
			@Override
			public Adapter caseClick(Click object) {
				return createClickAdapter();
			}
			@Override
			public Adapter caseDoubleClick(DoubleClick object) {
				return createDoubleClickAdapter();
			}
			@Override
			public Adapter caseGetText(GetText object) {
				return createGetTextAdapter();
			}
			@Override
			public Adapter caseIsDisabled(IsDisabled object) {
				return createIsDisabledAdapter();
			}
			@Override
			public Adapter caseIsDisposed(IsDisposed object) {
				return createIsDisposedAdapter();
			}
			@Override
			public Adapter caseTypeText(TypeText object) {
				return createTypeTextAdapter();
			}
			@Override
			public Adapter caseKeyType(KeyType object) {
				return createKeyTypeAdapter();
			}
			@Override
			public Adapter caseTypeCommandKey(TypeCommandKey object) {
				return createTypeCommandKeyAdapter();
			}
			@Override
			public Adapter caseSetText(SetText object) {
				return createSetTextAdapter();
			}
			@Override
			public Adapter caseSetTextSelection(SetTextSelection object) {
				return createSetTextSelectionAdapter();
			}
			@Override
			public Adapter caseSetTextOffset(SetTextOffset object) {
				return createSetTextOffsetAdapter();
			}
			@Override
			public Adapter caseCheck(Check object) {
				return createCheckAdapter();
			}
			@Override
			public Adapter caseUncheck(Uncheck object) {
				return createUncheckAdapter();
			}
			@Override
			public Adapter caseSelect(Select object) {
				return createSelectAdapter();
			}
			@Override
			public Adapter caseCellEdit(CellEdit object) {
				return createCellEditAdapter();
			}
			@Override
			public Adapter caseActivateCellEdit(ActivateCellEdit object) {
				return createActivateCellEditAdapter();
			}
			@Override
			public Adapter caseApplyCellEdit(ApplyCellEdit object) {
				return createApplyCellEditAdapter();
			}
			@Override
			public Adapter caseCancelCellEdit(CancelCellEdit object) {
				return createCancelCellEditAdapter();
			}
			@Override
			public Adapter caseDeactivateCellEdit(DeactivateCellEdit object) {
				return createDeactivateCellEditAdapter();
			}
			@Override
			public Adapter caseClose(Close object) {
				return createCloseAdapter();
			}
			@Override
			public Adapter caseReturnFromOsDialog(ReturnFromOsDialog object) {
				return createReturnFromOsDialogAdapter();
			}
			@Override
			public Adapter caseWaitUntilEclipseIsReady(WaitUntilEclipseIsReady object) {
				return createWaitUntilEclipseIsReadyAdapter();
			}
			@Override
			public Adapter caseShowContentAssist(ShowContentAssist object) {
				return createShowContentAssistAdapter();
			}
			@Override
			public Adapter caseDragAction(DragAction object) {
				return createDragActionAdapter();
			}
			@Override
			public Adapter caseDragStart(DragStart object) {
				return createDragStartAdapter();
			}
			@Override
			public Adapter caseDragEnd(DragEnd object) {
				return createDragEndAdapter();
			}
			@Override
			public Adapter caseDragEnter(DragEnter object) {
				return createDragEnterAdapter();
			}
			@Override
			public Adapter caseDragExit(DragExit object) {
				return createDragExitAdapter();
			}
			@Override
			public Adapter caseDragSetData(DragSetData object) {
				return createDragSetDataAdapter();
			}
			@Override
			public Adapter caseDragAccept(DragAccept object) {
				return createDragAcceptAdapter();
			}
			@Override
			public Adapter caseDragDetect(DragDetect object) {
				return createDragDetectAdapter();
			}
			@Override
			public Adapter caseDragOver(DragOver object) {
				return createDragOverAdapter();
			}
			@Override
			public Adapter caseDrop(Drop object) {
				return createDropAdapter();
			}
			@Override
			public Adapter caseRecognize(Recognize object) {
				return createRecognizeAdapter();
			}
			@Override
			public Adapter caseRecognizeResponse(RecognizeResponse object) {
				return createRecognizeResponseAdapter();
			}
			@Override
			public Adapter caseContainsImage(ContainsImage object) {
				return createContainsImageAdapter();
			}
			@Override
			public Adapter caseGetRegionText(GetRegionText object) {
				return createGetRegionTextAdapter();
			}
			@Override
			public Adapter caseGetRegion(GetRegion object) {
				return createGetRegionAdapter();
			}
			@Override
			public Adapter caseGetAdvancedInfo(GetAdvancedInfo object) {
				return createGetAdvancedInfoAdapter();
			}
			@Override
			public Adapter caseGetAboutMenu(GetAboutMenu object) {
				return createGetAboutMenuAdapter();
			}
			@Override
			public Adapter caseGetPreferencesMenu(GetPreferencesMenu object) {
				return createGetPreferencesMenuAdapter();
			}
			@Override
			public Adapter caseGetDateTime(GetDateTime object) {
				return createGetDateTimeAdapter();
			}
			@Override
			public Adapter caseGetSlider(GetSlider object) {
				return createGetSliderAdapter();
			}
			@Override
			public Adapter caseSetValue(SetValue object) {
				return createSetValueAdapter();
			}
			@Override
			public Adapter caseMinimize(Minimize object) {
				return createMinimizeAdapter();
			}
			@Override
			public Adapter caseMaximize(Maximize object) {
				return createMaximizeAdapter();
			}
			@Override
			public Adapter caseRestore(Restore object) {
				return createRestoreAdapter();
			}
			@Override
			public Adapter caseShowTabList(ShowTabList object) {
				return createShowTabListAdapter();
			}
			@Override
			public Adapter caseOptions(Options object) {
				return createOptionsAdapter();
			}
			@Override
			public Adapter caseExecWithOptions(ExecWithOptions object) {
				return createExecWithOptionsAdapter();
			}
			@Override
			public Adapter caseShutdownAut(ShutdownAut object) {
				return createShutdownAutAdapter();
			}
			@Override
			public Adapter caseSetDialogResult(SetDialogResult object) {
				return createSetDialogResultAdapter();
			}
			@Override
			public Adapter caseGetPropertyNodes(GetPropertyNodes object) {
				return createGetPropertyNodesAdapter();
			}
			@Override
			public Adapter caseGetByOs(GetByOs object) {
				return createGetByOsAdapter();
			}
			@Override
			public Adapter caseFromRawKey(FromRawKey object) {
				return createFromRawKeyAdapter();
			}
			@Override
			public Adapter caseHoverAtTextOffset(HoverAtTextOffset object) {
				return createHoverAtTextOffsetAdapter();
			}
			@Override
			public Adapter caseGetTextViewer(GetTextViewer object) {
				return createGetTextViewerAdapter();
			}
			@Override
			public Adapter caseSelectRange(SelectRange object) {
				return createSelectRangeAdapter();
			}
			@Override
			public Adapter caseSetCaretPos(SetCaretPos object) {
				return createSetCaretPosAdapter();
			}
			@Override
			public Adapter caseHoverText(HoverText object) {
				return createHoverTextAdapter();
			}
			@Override
			public Adapter caseOpenDeclaration(OpenDeclaration object) {
				return createOpenDeclarationAdapter();
			}
			@Override
			public Adapter caseGetVerticalRuler(GetVerticalRuler object) {
				return createGetVerticalRulerAdapter();
			}
			@Override
			public Adapter caseGetLeftRuler(GetLeftRuler object) {
				return createGetLeftRulerAdapter();
			}
			@Override
			public Adapter caseGetRulerColumn(GetRulerColumn object) {
				return createGetRulerColumnAdapter();
			}
			@Override
			public Adapter caseGetRightRuler(GetRightRuler object) {
				return createGetRightRulerAdapter();
			}
			@Override
			public Adapter caseClickRuler(ClickRuler object) {
				return createClickRulerAdapter();
			}
			@Override
			public Adapter caseDoubleClickRuler(DoubleClickRuler object) {
				return createDoubleClickRulerAdapter();
			}
			@Override
			public Adapter caseHoverRuler(HoverRuler object) {
				return createHoverRulerAdapter();
			}
			@Override
			public Adapter caseControlCommand(ControlCommand object) {
				return createControlCommandAdapter();
			}
			@Override
			public Adapter caseClickLink(ClickLink object) {
				return createClickLinkAdapter();
			}
			@Override
			public Adapter caseExpand(Expand object) {
				return createExpandAdapter();
			}
			@Override
			public Adapter caseCollapse(Collapse object) {
				return createCollapseAdapter();
			}
			@Override
			public Adapter caseSetFocus(SetFocus object) {
				return createSetFocusAdapter();
			}
			@Override
			public Adapter caseGetTableData(GetTableData object) {
				return createGetTableDataAdapter();
			}
			@Override
			public Adapter caseClickColumn(ClickColumn object) {
				return createClickColumnAdapter();
			}
			@Override
			public Adapter caseTrace(Trace object) {
				return createTraceAdapter();
			}
			@Override
			public Adapter caseMouse(Mouse object) {
				return createMouseAdapter();
			}
			@Override
			public Adapter caseGetObject(GetObject object) {
				return createGetObjectAdapter();
			}
			@Override
			public Adapter caseGetItems(GetItems object) {
				return createGetItemsAdapter();
			}
			@Override
			public Adapter caseExpandAll(ExpandAll object) {
				return createExpandAllAdapter();
			}
			@Override
			public Adapter caseCollapseAll(CollapseAll object) {
				return createCollapseAllAdapter();
			}
			@Override
			public Adapter caseTakeScreenshot(TakeScreenshot object) {
				return createTakeScreenshotAdapter();
			}
			@Override
			public Adapter caseSelectItem(SelectItem object) {
				return createSelectItemAdapter();
			}
			@Override
			public Adapter caseGetWidgetDetails(GetWidgetDetails object) {
				return createGetWidgetDetailsAdapter();
			}
			@Override
			public Adapter caseClickText(ClickText object) {
				return createClickTextAdapter();
			}
			@Override
			public Adapter caseGetQuickAccess(GetQuickAccess object) {
				return createGetQuickAccessAdapter();
			}
			@Override
			public Adapter caseGetColumnHeader(GetColumnHeader object) {
				return createGetColumnHeaderAdapter();
			}
			@Override
			public Adapter caseSetPosition(SetPosition object) {
				return createSetPositionAdapter();
			}
			@Override
			public Adapter caseSetWidth(SetWidth object) {
				return createSetWidthAdapter();
			}
			@Override
			public Adapter caseGetPropertyTab(GetPropertyTab object) {
				return createGetPropertyTabAdapter();
			}
			@Override
			public Adapter caseShowAlert(ShowAlert object) {
				return createShowAlertAdapter();
			}
			@Override
			public Adapter caseDoubleClickText(DoubleClickText object) {
				return createDoubleClickTextAdapter();
			}
			@Override
			public Adapter caseToControlHandle(ToControlHandle object) {
				return createToControlHandleAdapter();
			}
			@Override
			public Adapter caseBoundControlHandle(BoundControlHandle object) {
				return createBoundControlHandleAdapter();
			}
			@Override
			public Adapter caseUnfocus(Unfocus object) {
				return createUnfocusAdapter();
			}
			@Override
			public Adapter caseDecrypt(Decrypt object) {
				return createDecryptAdapter();
			}
			@Override
			public Adapter caseDecryptResult(DecryptResult object) {
				return createDecryptResultAdapter();
			}
			@Override
			public Adapter caseRestartAut(RestartAut object) {
				return createRestartAutAdapter();
			}
			@Override
			public Adapter caseMessageBoxInfo(MessageBoxInfo object) {
				return createMessageBoxInfoAdapter();
			}
			@Override
			public Adapter caseGetLastMessageBox(GetLastMessageBox object) {
				return createGetLastMessageBoxAdapter();
			}
			@Override
			public Adapter caseCheckDownloadResult(CheckDownloadResult object) {
				return createCheckDownloadResultAdapter();
			}
			@Override
			public Adapter caseGetRuntimeTarget(GetRuntimeTarget object) {
				return createGetRuntimeTargetAdapter();
			}
			@Override
			public Adapter caseGetTestCaseName(GetTestCaseName object) {
				return createGetTestCaseNameAdapter();
			}
			@Override
			public Adapter caseGetComboItems(GetComboItems object) {
				return createGetComboItemsAdapter();
			}
			@Override
			public Adapter caseCommand(Command object) {
				return createCommandAdapter();
			}
			@Override
			public Adapter caseProcessStatus(ProcessStatus object) {
				return createProcessStatusAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Wait <em>Wait</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Wait
	 * @generated
	 */
	public Adapter createWaitAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetupPlayer <em>Setup Player</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetupPlayer
	 * @generated
	 */
	public Adapter createSetupPlayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ShoutdownPlayer <em>Shoutdown Player</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ShoutdownPlayer
	 * @generated
	 */
	public Adapter createShoutdownPlayerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.TeslaProcessStatus <em>Process Status</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.TeslaProcessStatus
	 * @generated
	 */
	public Adapter createTeslaProcessStatusAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Wrapper <em>Wrapper</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Wrapper
	 * @generated
	 */
	public Adapter createWrapperAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetProperty <em>Get Property</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetProperty
	 * @generated
	 */
	public Adapter createGetPropertyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.VerifyTrue <em>Verify True</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.VerifyTrue
	 * @generated
	 */
	public Adapter createVerifyTrueAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.VerifyFalse <em>Verify False</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.VerifyFalse
	 * @generated
	 */
	public Adapter createVerifyFalseAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.VerifyHandler <em>Verify Handler</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.VerifyHandler
	 * @generated
	 */
	public Adapter createVerifyHandlerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.VerifyError <em>Verify Error</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.VerifyError
	 * @generated
	 */
	public Adapter createVerifyErrorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Equals <em>Equals</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Equals
	 * @generated
	 */
	public Adapter createEqualsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Contains <em>Contains</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Contains
	 * @generated
	 */
	public Adapter createContainsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Matches <em>Matches</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Matches
	 * @generated
	 */
	public Adapter createMatchesAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.IsEmpty <em>Is Empty</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.IsEmpty
	 * @generated
	 */
	public Adapter createIsEmptyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Unsupported <em>Unsupported</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Unsupported
	 * @generated
	 */
	public Adapter createUnsupportedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ControlNotFound <em>Control Not Found</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ControlNotFound
	 * @generated
	 */
	public Adapter createControlNotFoundAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ControlHandler <em>Control Handler</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ControlHandler
	 * @generated
	 */
	public Adapter createControlHandlerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetEclipseWindow <em>Get Eclipse Window</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetEclipseWindow
	 * @generated
	 */
	public Adapter createGetEclipseWindowAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Selector <em>Selector</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Selector
	 * @generated
	 */
	public Adapter createSelectorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.PathSelector <em>Path Selector</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.PathSelector
	 * @generated
	 */
	public Adapter createPathSelectorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.TextSelector <em>Text Selector</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.TextSelector
	 * @generated
	 */
	public Adapter createTextSelectorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetControl <em>Get Control</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetControl
	 * @generated
	 */
	public Adapter createGetControlAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetButton <em>Get Button</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetButton
	 * @generated
	 */
	public Adapter createGetButtonAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetCanvas <em>Get Canvas</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetCanvas
	 * @generated
	 */
	public Adapter createGetCanvasAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetCheckbox <em>Get Checkbox</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetCheckbox
	 * @generated
	 */
	public Adapter createGetCheckboxAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetCombo <em>Get Combo</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetCombo
	 * @generated
	 */
	public Adapter createGetComboAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetEditbox <em>Get Editbox</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetEditbox
	 * @generated
	 */
	public Adapter createGetEditboxAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetGroup <em>Get Group</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetGroup
	 * @generated
	 */
	public Adapter createGetGroupAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetItem <em>Get Item</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetItem
	 * @generated
	 */
	public Adapter createGetItemAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetLabel <em>Get Label</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetLabel
	 * @generated
	 */
	public Adapter createGetLabelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetLink <em>Get Link</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetLink
	 * @generated
	 */
	public Adapter createGetLinkAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetList <em>Get List</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetList
	 * @generated
	 */
	public Adapter createGetListAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetMenu <em>Get Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetMenu
	 * @generated
	 */
	public Adapter createGetMenuAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetTabFolder <em>Get Tab Folder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetTabFolder
	 * @generated
	 */
	public Adapter createGetTabFolderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetTabItem <em>Get Tab Item</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetTabItem
	 * @generated
	 */
	public Adapter createGetTabItemAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetTable <em>Get Table</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetTable
	 * @generated
	 */
	public Adapter createGetTableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetToolbar <em>Get Toolbar</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetToolbar
	 * @generated
	 */
	public Adapter createGetToolbarAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetTree <em>Get Tree</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetTree
	 * @generated
	 */
	public Adapter createGetTreeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetWindow <em>Get Window</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetWindow
	 * @generated
	 */
	public Adapter createGetWindowAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetView <em>Get View</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetView
	 * @generated
	 */
	public Adapter createGetViewAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetEditor <em>Get Editor</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetEditor
	 * @generated
	 */
	public Adapter createGetEditorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetSection <em>Get Section</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetSection
	 * @generated
	 */
	public Adapter createGetSectionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetBanner <em>Get Banner</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetBanner
	 * @generated
	 */
	public Adapter createGetBannerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetCoolbar <em>Get Coolbar</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetCoolbar
	 * @generated
	 */
	public Adapter createGetCoolbarAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetColorSelector <em>Get Color Selector</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetColorSelector
	 * @generated
	 */
	public Adapter createGetColorSelectorAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetCell <em>Get Cell</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetCell
	 * @generated
	 */
	public Adapter createGetCellAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Click <em>Click</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Click
	 * @generated
	 */
	public Adapter createClickAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DoubleClick <em>Double Click</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DoubleClick
	 * @generated
	 */
	public Adapter createDoubleClickAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetText <em>Get Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetText
	 * @generated
	 */
	public Adapter createGetTextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.IsDisabled <em>Is Disabled</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.IsDisabled
	 * @generated
	 */
	public Adapter createIsDisabledAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.IsDisposed <em>Is Disposed</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.IsDisposed
	 * @generated
	 */
	public Adapter createIsDisposedAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.TypeText <em>Type Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.TypeText
	 * @generated
	 */
	public Adapter createTypeTextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.KeyType <em>Key Type</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.KeyType
	 * @generated
	 */
	public Adapter createKeyTypeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.TypeCommandKey <em>Type Command Key</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.TypeCommandKey
	 * @generated
	 */
	public Adapter createTypeCommandKeyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetText <em>Set Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetText
	 * @generated
	 */
	public Adapter createSetTextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetTextSelection <em>Set Text Selection</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetTextSelection
	 * @generated
	 */
	public Adapter createSetTextSelectionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetTextOffset <em>Set Text Offset</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetTextOffset
	 * @generated
	 */
	public Adapter createSetTextOffsetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Check <em>Check</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Check
	 * @generated
	 */
	public Adapter createCheckAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Uncheck <em>Uncheck</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Uncheck
	 * @generated
	 */
	public Adapter createUncheckAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Select <em>Select</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Select
	 * @generated
	 */
	public Adapter createSelectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.CellEdit <em>Cell Edit</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.CellEdit
	 * @generated
	 */
	public Adapter createCellEditAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ActivateCellEdit <em>Activate Cell Edit</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ActivateCellEdit
	 * @generated
	 */
	public Adapter createActivateCellEditAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ApplyCellEdit <em>Apply Cell Edit</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ApplyCellEdit
	 * @generated
	 */
	public Adapter createApplyCellEditAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.CancelCellEdit <em>Cancel Cell Edit</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.CancelCellEdit
	 * @generated
	 */
	public Adapter createCancelCellEditAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DeactivateCellEdit <em>Deactivate Cell Edit</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DeactivateCellEdit
	 * @generated
	 */
	public Adapter createDeactivateCellEditAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Close <em>Close</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Close
	 * @generated
	 */
	public Adapter createCloseAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ReturnFromOsDialog <em>Return From Os Dialog</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ReturnFromOsDialog
	 * @generated
	 */
	public Adapter createReturnFromOsDialogAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.WaitUntilEclipseIsReady <em>Wait Until Eclipse Is Ready</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.WaitUntilEclipseIsReady
	 * @generated
	 */
	public Adapter createWaitUntilEclipseIsReadyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ShowContentAssist <em>Show Content Assist</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ShowContentAssist
	 * @generated
	 */
	public Adapter createShowContentAssistAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragAction <em>Drag Action</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragAction
	 * @generated
	 */
	public Adapter createDragActionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragStart <em>Drag Start</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragStart
	 * @generated
	 */
	public Adapter createDragStartAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragEnd <em>Drag End</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragEnd
	 * @generated
	 */
	public Adapter createDragEndAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragEnter <em>Drag Enter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragEnter
	 * @generated
	 */
	public Adapter createDragEnterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragExit <em>Drag Exit</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragExit
	 * @generated
	 */
	public Adapter createDragExitAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragSetData <em>Drag Set Data</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragSetData
	 * @generated
	 */
	public Adapter createDragSetDataAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragAccept <em>Drag Accept</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragAccept
	 * @generated
	 */
	public Adapter createDragAcceptAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragDetect <em>Drag Detect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragDetect
	 * @generated
	 */
	public Adapter createDragDetectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DragOver <em>Drag Over</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DragOver
	 * @generated
	 */
	public Adapter createDragOverAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Drop <em>Drop</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Drop
	 * @generated
	 */
	public Adapter createDropAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Recognize <em>Recognize</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Recognize
	 * @generated
	 */
	public Adapter createRecognizeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.RecognizeResponse <em>Recognize Response</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.RecognizeResponse
	 * @generated
	 */
	public Adapter createRecognizeResponseAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ContainsImage <em>Contains Image</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ContainsImage
	 * @generated
	 */
	public Adapter createContainsImageAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetRegionText <em>Get Region Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetRegionText
	 * @generated
	 */
	public Adapter createGetRegionTextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetRegion <em>Get Region</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetRegion
	 * @generated
	 */
	public Adapter createGetRegionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetAdvancedInfo <em>Get Advanced Info</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetAdvancedInfo
	 * @generated
	 */
	public Adapter createGetAdvancedInfoAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetAboutMenu <em>Get About Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetAboutMenu
	 * @generated
	 */
	public Adapter createGetAboutMenuAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetPreferencesMenu <em>Get Preferences Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetPreferencesMenu
	 * @generated
	 */
	public Adapter createGetPreferencesMenuAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetDateTime <em>Get Date Time</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetDateTime
	 * @generated
	 */
	public Adapter createGetDateTimeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetSlider <em>Get Slider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetSlider
	 * @generated
	 */
	public Adapter createGetSliderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetValue <em>Set Value</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetValue
	 * @generated
	 */
	public Adapter createSetValueAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Minimize <em>Minimize</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Minimize
	 * @generated
	 */
	public Adapter createMinimizeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Maximize <em>Maximize</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Maximize
	 * @generated
	 */
	public Adapter createMaximizeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Restore <em>Restore</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Restore
	 * @generated
	 */
	public Adapter createRestoreAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ShowTabList <em>Show Tab List</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ShowTabList
	 * @generated
	 */
	public Adapter createShowTabListAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Options <em>Options</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Options
	 * @generated
	 */
	public Adapter createOptionsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ExecWithOptions <em>Exec With Options</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ExecWithOptions
	 * @generated
	 */
	public Adapter createExecWithOptionsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ShutdownAut <em>Shutdown Aut</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ShutdownAut
	 * @generated
	 */
	public Adapter createShutdownAutAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetDialogResult <em>Set Dialog Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetDialogResult
	 * @generated
	 */
	public Adapter createSetDialogResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetPropertyNodes <em>Get Property Nodes</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetPropertyNodes
	 * @generated
	 */
	public Adapter createGetPropertyNodesAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetByOs <em>Get By Os</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetByOs
	 * @generated
	 */
	public Adapter createGetByOsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.FromRawKey <em>From Raw Key</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.FromRawKey
	 * @generated
	 */
	public Adapter createFromRawKeyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.HoverAtTextOffset <em>Hover At Text Offset</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.HoverAtTextOffset
	 * @generated
	 */
	public Adapter createHoverAtTextOffsetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetTextViewer <em>Get Text Viewer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetTextViewer
	 * @generated
	 */
	public Adapter createGetTextViewerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SelectRange <em>Select Range</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SelectRange
	 * @generated
	 */
	public Adapter createSelectRangeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetCaretPos <em>Set Caret Pos</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetCaretPos
	 * @generated
	 */
	public Adapter createSetCaretPosAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.HoverText <em>Hover Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.HoverText
	 * @generated
	 */
	public Adapter createHoverTextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.OpenDeclaration <em>Open Declaration</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.OpenDeclaration
	 * @generated
	 */
	public Adapter createOpenDeclarationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetVerticalRuler <em>Get Vertical Ruler</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetVerticalRuler
	 * @generated
	 */
	public Adapter createGetVerticalRulerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetLeftRuler <em>Get Left Ruler</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetLeftRuler
	 * @generated
	 */
	public Adapter createGetLeftRulerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetRulerColumn <em>Get Ruler Column</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetRulerColumn
	 * @generated
	 */
	public Adapter createGetRulerColumnAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetRightRuler <em>Get Right Ruler</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetRightRuler
	 * @generated
	 */
	public Adapter createGetRightRulerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ClickRuler <em>Click Ruler</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ClickRuler
	 * @generated
	 */
	public Adapter createClickRulerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DoubleClickRuler <em>Double Click Ruler</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DoubleClickRuler
	 * @generated
	 */
	public Adapter createDoubleClickRulerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.HoverRuler <em>Hover Ruler</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.HoverRuler
	 * @generated
	 */
	public Adapter createHoverRulerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ControlCommand <em>Control Command</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ControlCommand
	 * @generated
	 */
	public Adapter createControlCommandAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ClickLink <em>Click Link</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ClickLink
	 * @generated
	 */
	public Adapter createClickLinkAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Expand <em>Expand</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Expand
	 * @generated
	 */
	public Adapter createExpandAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Collapse <em>Collapse</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Collapse
	 * @generated
	 */
	public Adapter createCollapseAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetFocus <em>Set Focus</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetFocus
	 * @generated
	 */
	public Adapter createSetFocusAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetTableData <em>Get Table Data</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetTableData
	 * @generated
	 */
	public Adapter createGetTableDataAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ClickColumn <em>Click Column</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ClickColumn
	 * @generated
	 */
	public Adapter createClickColumnAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Trace <em>Trace</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Trace
	 * @generated
	 */
	public Adapter createTraceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Mouse <em>Mouse</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Mouse
	 * @generated
	 */
	public Adapter createMouseAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetObject <em>Get Object</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetObject
	 * @generated
	 */
	public Adapter createGetObjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetItems <em>Get Items</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetItems
	 * @generated
	 */
	public Adapter createGetItemsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ExpandAll <em>Expand All</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ExpandAll
	 * @generated
	 */
	public Adapter createExpandAllAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.CollapseAll <em>Collapse All</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.CollapseAll
	 * @generated
	 */
	public Adapter createCollapseAllAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.TakeScreenshot <em>Take Screenshot</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.TakeScreenshot
	 * @generated
	 */
	public Adapter createTakeScreenshotAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SelectItem <em>Select Item</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SelectItem
	 * @generated
	 */
	public Adapter createSelectItemAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetWidgetDetails <em>Get Widget Details</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetWidgetDetails
	 * @generated
	 */
	public Adapter createGetWidgetDetailsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ClickText <em>Click Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ClickText
	 * @generated
	 */
	public Adapter createClickTextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetQuickAccess <em>Get Quick Access</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetQuickAccess
	 * @generated
	 */
	public Adapter createGetQuickAccessAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetColumnHeader <em>Get Column Header</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetColumnHeader
	 * @generated
	 */
	public Adapter createGetColumnHeaderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetPosition <em>Set Position</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetPosition
	 * @generated
	 */
	public Adapter createSetPositionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.SetWidth <em>Set Width</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.SetWidth
	 * @generated
	 */
	public Adapter createSetWidthAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetPropertyTab <em>Get Property Tab</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetPropertyTab
	 * @generated
	 */
	public Adapter createGetPropertyTabAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ShowAlert <em>Show Alert</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ShowAlert
	 * @generated
	 */
	public Adapter createShowAlertAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DoubleClickText <em>Double Click Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DoubleClickText
	 * @generated
	 */
	public Adapter createDoubleClickTextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.ToControlHandle <em>To Control Handle</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.ToControlHandle
	 * @generated
	 */
	public Adapter createToControlHandleAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.BoundControlHandle <em>Bound Control Handle</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.BoundControlHandle
	 * @generated
	 */
	public Adapter createBoundControlHandleAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Unfocus <em>Unfocus</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Unfocus
	 * @generated
	 */
	public Adapter createUnfocusAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.Decrypt <em>Decrypt</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.Decrypt
	 * @generated
	 */
	public Adapter createDecryptAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.DecryptResult <em>Decrypt Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.DecryptResult
	 * @generated
	 */
	public Adapter createDecryptResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.RestartAut <em>Restart Aut</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.RestartAut
	 * @generated
	 */
	public Adapter createRestartAutAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.MessageBoxInfo <em>Message Box Info</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.MessageBoxInfo
	 * @generated
	 */
	public Adapter createMessageBoxInfoAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetLastMessageBox <em>Get Last Message Box</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetLastMessageBox
	 * @generated
	 */
	public Adapter createGetLastMessageBoxAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.CheckDownloadResult <em>Check Download Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.CheckDownloadResult
	 * @generated
	 */
	public Adapter createCheckDownloadResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetRuntimeTarget <em>Get Runtime Target</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetRuntimeTarget
	 * @generated
	 */
	public Adapter createGetRuntimeTargetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetTestCaseName <em>Get Test Case Name</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetTestCaseName
	 * @generated
	 */
	public Adapter createGetTestCaseNameAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.tesla.ecl.model.GetComboItems <em>Get Combo Items</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.tesla.ecl.model.GetComboItems
	 * @generated
	 */
	public Adapter createGetComboItemsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.ecl.core.Command <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.ecl.core.Command
	 * @generated
	 */
	public Adapter createCommandAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.rcptt.ecl.core.ProcessStatus <em>Process Status</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.rcptt.ecl.core.ProcessStatus
	 * @generated
	 */
	public Adapter createProcessStatusAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //TeslaAdapterFactory
