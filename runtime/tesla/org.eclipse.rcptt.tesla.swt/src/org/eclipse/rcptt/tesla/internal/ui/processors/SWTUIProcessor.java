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
package org.eclipse.rcptt.tesla.internal.ui.processors;

import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.replaceMultilines;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.safeMatches;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.unifyMultilines;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.validateRegex;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWidgetUtils.canClick;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWidgetUtils.canClickView;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWidgetUtils.getModalChild;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWidgetUtils.isDisabled;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrap;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrapWidget;
import static org.eclipse.rcptt.tesla.swt.util.GetWindowUtil.getShellCreationMethodName;
import static org.eclipse.rcptt.util.swt.Bounds.centerAbs;
import static org.eclipse.rcptt.util.swt.Bounds.centerRel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rcptt.tesla.core.ClosedException;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.ActivateCellEditor;
import org.eclipse.rcptt.tesla.core.protocol.ApplyCellEditor;
import org.eclipse.rcptt.tesla.core.protocol.Assert;
import org.eclipse.rcptt.tesla.core.protocol.AssertImageData;
import org.eclipse.rcptt.tesla.core.protocol.AssertKind;
import org.eclipse.rcptt.tesla.core.protocol.AssertResponse;
import org.eclipse.rcptt.tesla.core.protocol.BooleanResponse;
import org.eclipse.rcptt.tesla.core.protocol.BoundsResponse;
import org.eclipse.rcptt.tesla.core.protocol.CancelCellEditor;
import org.eclipse.rcptt.tesla.core.protocol.CellClick;
import org.eclipse.rcptt.tesla.core.protocol.Check;
import org.eclipse.rcptt.tesla.core.protocol.CheckItem;
import org.eclipse.rcptt.tesla.core.protocol.Children;
import org.eclipse.rcptt.tesla.core.protocol.Click;
import org.eclipse.rcptt.tesla.core.protocol.ClickAboutMenu;
import org.eclipse.rcptt.tesla.core.protocol.ClickColumn;
import org.eclipse.rcptt.tesla.core.protocol.ClickLink;
import org.eclipse.rcptt.tesla.core.protocol.ClickPreferencesMenu;
import org.eclipse.rcptt.tesla.core.protocol.ClickText;
import org.eclipse.rcptt.tesla.core.protocol.Close;
import org.eclipse.rcptt.tesla.core.protocol.Collapse;
import org.eclipse.rcptt.tesla.core.protocol.CopyTextSelection;
import org.eclipse.rcptt.tesla.core.protocol.CountItems;
import org.eclipse.rcptt.tesla.core.protocol.CutTextSelection;
import org.eclipse.rcptt.tesla.core.protocol.DeactivateCellEditor;
import org.eclipse.rcptt.tesla.core.protocol.DoubleClick;
import org.eclipse.rcptt.tesla.core.protocol.DoubleClickText;
import org.eclipse.rcptt.tesla.core.protocol.DragCommand;
import org.eclipse.rcptt.tesla.core.protocol.ElementCommand;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.Expand;
import org.eclipse.rcptt.tesla.core.protocol.GenericElementKind;
import org.eclipse.rcptt.tesla.core.protocol.GetBounds;
import org.eclipse.rcptt.tesla.core.protocol.GetItems;
import org.eclipse.rcptt.tesla.core.protocol.GetItemsResponse;
import org.eclipse.rcptt.tesla.core.protocol.GetPropertyValue;
import org.eclipse.rcptt.tesla.core.protocol.GetRegionText;
import org.eclipse.rcptt.tesla.core.protocol.GetSelection;
import org.eclipse.rcptt.tesla.core.protocol.GetText;
import org.eclipse.rcptt.tesla.core.protocol.GetTextLine;
import org.eclipse.rcptt.tesla.core.protocol.GetTextLineLength;
import org.eclipse.rcptt.tesla.core.protocol.GetTextLineOffset;
import org.eclipse.rcptt.tesla.core.protocol.GetTextRange;
import org.eclipse.rcptt.tesla.core.protocol.GetTextResponse;
import org.eclipse.rcptt.tesla.core.protocol.GetTextSelection;
import org.eclipse.rcptt.tesla.core.protocol.GoToTextLine;
import org.eclipse.rcptt.tesla.core.protocol.HoverAtTextOffset;
import org.eclipse.rcptt.tesla.core.protocol.IElementProcessorMapper;
import org.eclipse.rcptt.tesla.core.protocol.IntResponse;
import org.eclipse.rcptt.tesla.core.protocol.IsDirty;
import org.eclipse.rcptt.tesla.core.protocol.IsDisposed;
import org.eclipse.rcptt.tesla.core.protocol.IsEnabled;
import org.eclipse.rcptt.tesla.core.protocol.LinkUIElement;
import org.eclipse.rcptt.tesla.core.protocol.Maximize;
import org.eclipse.rcptt.tesla.core.protocol.Minimize;
import org.eclipse.rcptt.tesla.core.protocol.MouseEvent;
import org.eclipse.rcptt.tesla.core.protocol.MouseEventKind;
import org.eclipse.rcptt.tesla.core.protocol.MultiSelectionItem;
import org.eclipse.rcptt.tesla.core.protocol.ObjectResponse;
import org.eclipse.rcptt.tesla.core.protocol.PasteTextSelection;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolFactory;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolPackage;
import org.eclipse.rcptt.tesla.core.protocol.ReplaceTextSelection;
import org.eclipse.rcptt.tesla.core.protocol.Restore;
import org.eclipse.rcptt.tesla.core.protocol.Save;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectData;
import org.eclipse.rcptt.tesla.core.protocol.SelectResponse;
import org.eclipse.rcptt.tesla.core.protocol.SelectTextLine;
import org.eclipse.rcptt.tesla.core.protocol.SelectionItem;
import org.eclipse.rcptt.tesla.core.protocol.SelectionResponse;
import org.eclipse.rcptt.tesla.core.protocol.SetFocus;
import org.eclipse.rcptt.tesla.core.protocol.SetSWTDialogInfo;
import org.eclipse.rcptt.tesla.core.protocol.SetSelection;
import org.eclipse.rcptt.tesla.core.protocol.SetStatusDialogMode;
import org.eclipse.rcptt.tesla.core.protocol.SetText;
import org.eclipse.rcptt.tesla.core.protocol.SetTextOffset;
import org.eclipse.rcptt.tesla.core.protocol.SetTextSelection;
import org.eclipse.rcptt.tesla.core.protocol.Show;
import org.eclipse.rcptt.tesla.core.protocol.ShowSelection;
import org.eclipse.rcptt.tesla.core.protocol.ShowTabList;
import org.eclipse.rcptt.tesla.core.protocol.TextSelectionResponse;
import org.eclipse.rcptt.tesla.core.protocol.Type;
import org.eclipse.rcptt.tesla.core.protocol.TypeAction;
import org.eclipse.rcptt.tesla.core.protocol.TypeText;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawFactory;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.core.protocol.raw.ResponseStatus;
import org.eclipse.rcptt.tesla.core.utils.TeslaUtils;
import org.eclipse.rcptt.tesla.internal.core.AbstractTeslaClient;
import org.eclipse.rcptt.tesla.internal.core.SimpleCommandPrinter;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.core.info.InfoUtils;
import org.eclipse.rcptt.tesla.internal.core.info.InfoUtils.Node;
import org.eclipse.rcptt.tesla.internal.core.processing.ElementGenerator;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor;
import org.eclipse.rcptt.tesla.internal.ui.SWTElementMapper;
import org.eclipse.rcptt.tesla.internal.ui.player.ISWTModelMapperExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.ItemUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.PerspectiveUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerSelectionFilter;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTEvents;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTModelMapper;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.internal.ui.player.UIJobCollector;
import org.eclipse.rcptt.tesla.internal.ui.player.WorkbenchUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.viewers.Viewers;
import org.eclipse.rcptt.tesla.jface.TeslaCellEditorManager;
import org.eclipse.rcptt.tesla.jobs.JobsManager;
import org.eclipse.rcptt.tesla.swt.TeslaSWTMessages;
import org.eclipse.rcptt.tesla.swt.dialogs.SWTDialogManager;
import org.eclipse.rcptt.tesla.swt.dnd.LocalClipboard;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager.IUnhandledNativeDialogHandler;
import org.eclipse.rcptt.tesla.swt.events.TeslaTimerExecManager;
import org.eclipse.rcptt.tesla.swt.workbench.EclipseWorkbenchProvider;
import org.eclipse.rcptt.tesla.ui.IImageAssertSupport;
import org.eclipse.rcptt.tesla.ui.SWTTeslaActivator;
import org.eclipse.rcptt.tesla.ui.describers.IWidgetDescriber;
import org.eclipse.rcptt.tesla.ui.describers.WidgetDescriber;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.rcptt.util.StringUtils;
import org.eclipse.rcptt.util.swt.Events;
import org.eclipse.rcptt.util.swt.StringLines;
import org.eclipse.rcptt.util.swt.TableTreeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerService;

public class SWTUIProcessor implements ITeslaCommandProcessor,
		IModelMapperHelper {
	private static final ElementKind[] allSelectors = { ElementKind.Window,
			ElementKind.Menu, ElementKind.Button, ElementKind.Tree,
			ElementKind.List, ElementKind.Label, ElementKind.Table,
			ElementKind.TabItem, ElementKind.View, ElementKind.Link,
			ElementKind.FormText, ElementKind.Combo, ElementKind.TabFolder,
			ElementKind.Group, ElementKind.Expandable, ElementKind.Toolbar,
			ElementKind.CBanner, ElementKind.CoolBar,
			ElementKind.EclipseWindow, ElementKind.Any, ElementKind.Item,
			ElementKind.Canvas, ElementKind.Browser, ElementKind.Editor,
			ElementKind.Text, ElementKind.DateTime, ElementKind.Slider,
			ElementKind.QuickAccess, ElementKind.ColumnHeader };
	private static final EClass[] supportedCommands = {
			ProtocolPackage.Literals.GET_BOUNDS,
			ProtocolPackage.Literals.COUNT_ITEMS,
			ProtocolPackage.Literals.GET_TEXT,
			ProtocolPackage.Literals.IS_DISPOSED,
			ProtocolPackage.Literals.IS_ENABLED,
			ProtocolPackage.Literals.SET_SELECTION,
			ProtocolPackage.Literals.SET_TEXT,
			ProtocolPackage.Literals.CLICK,
			ProtocolPackage.Literals.CHECK,
			ProtocolPackage.Literals.DOUBLE_CLICK,
			ProtocolPackage.Literals.CLOSE,
			ProtocolPackage.Literals.TYPE_TEXT,
			ProtocolPackage.Literals.TYPE,
			ProtocolPackage.Literals.TYPE_ACTION,
			ProtocolPackage.Literals.SHOW,
			ProtocolPackage.Literals.GET_ITEMS,

			// Cell editor commands
			ProtocolPackage.Literals.ACTIVATE_CELL_EDITOR,
			ProtocolPackage.Literals.APPLY_CELL_EDITOR,
			ProtocolPackage.Literals.CANCEL_CELL_EDITOR,
			ProtocolPackage.Literals.DEACTIVATE_CELL_EDITOR,

			//
			ProtocolPackage.Literals.ASSERT,
			ProtocolPackage.Literals.SET_SWT_DIALOG_INFO,
			ProtocolPackage.Literals.SET_TEXT_OFFSET,
			ProtocolPackage.Literals.HOVER_AT_TEXT_OFFSET,
			ProtocolPackage.Literals.GET_SELECTION,
			ProtocolPackage.Literals.DRAG_COMMAND,
			ProtocolPackage.Literals.CELL_CLICK,
			ProtocolPackage.Literals.GET_REGION_TEXT,
			ProtocolPackage.Literals.CLICK_ABOUT_MENU,
			ProtocolPackage.Literals.CLICK_PREFERENCES_MENU,
			ProtocolPackage.Literals.SET_STATUS_DIALOG_MODE,
			ProtocolPackage.Literals.SET_FOCUS,
			ProtocolPackage.Literals.CLICK_TEXT,

			// Editor commands
			ProtocolPackage.Literals.SAVE,
			ProtocolPackage.Literals.IS_DIRTY,
			// Text commands
			ProtocolPackage.Literals.SET_TEXT_SELECTION,
			ProtocolPackage.Literals.SHOW_SELECTION,
			ProtocolPackage.Literals.GET_TEXT_SELECTION,
			ProtocolPackage.Literals.GO_TO_TEXT_LINE,
			ProtocolPackage.Literals.GET_TEXT_LINE_OFFSET,
			ProtocolPackage.Literals.GET_TEXT_LINE_LENGTH,
			ProtocolPackage.Literals.SELECT_TEXT_LINE,
			ProtocolPackage.Literals.GET_TEXT_LINE,
			ProtocolPackage.Literals.GET_TEXT_RANGE,
			ProtocolPackage.Literals.COPY_TEXT_SELECTION,
			ProtocolPackage.Literals.PASTE_TEXT_SELECTION,
			ProtocolPackage.Literals.CUT_TEXT_SELECTION,
			ProtocolPackage.Literals.REPLACE_TEXT_SELECTION,

			// Link commands
			ProtocolPackage.Literals.CLICK_LINK,

			ProtocolPackage.Literals.CLICK_COLUMN,
			ProtocolPackage.Literals.MOUSE_EVENT };

	private static final Set<String> SKIP_ACTIVATION_FOR_SHELLS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"ContextInformationPopup.createContextInfoPopup()")));

	protected static ProtocolFactory factory = ProtocolFactory.eINSTANCE;
	private AbstractTeslaClient client;
	private String id;
	private DragSupport dragSupport = new DragSupport();
	protected CellEditorSupport cellEditSupport = null;
	private SWTUIPlayer internalPlayer;
	private String failNextCommandBecauseOf = null;

	// private Set<Job> times = new HashSet<Job>();

	private static class ExpandSelectionStatus extends PreExecuteStatus {
		public ExpandSelectionStatus(final boolean canExecute) {
			super(canExecute);
		}

		int nextLevel = 0;
	}

	private static class DeVirtualizeStatus extends PreExecuteStatus {
		int position = 0;

		public DeVirtualizeStatus(boolean canExecute, int position) {
			super(canExecute);
			this.position = position;
		}
	}

	@Override
	public int getPriority() {
		return 100;
	}

	@Override
	public PreExecuteStatus preExecute(final Command command,
			final PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		if (command instanceof ElementCommand) {
			if (!(command instanceof GetPropertyValue)) {
				final ElementCommand cmd = (ElementCommand) command;
				if (!activateViewEditor(cmd.getElement(), false, info)) {
					return new PreExecuteStatus(false);
				}
			}
		}
		PreExecuteStatus resultStatus = preExecuteAssert(command,
				previousStatus, info);
		if (resultStatus != null) {
			return resultStatus;
		}
		resultStatus = preExecuteSetSelection(command, previousStatus, info);
		if (resultStatus != null) {
			return resultStatus;
		}
		resultStatus = preExecuteCheckItem(command, previousStatus, info);
		if (resultStatus != null) {
			return resultStatus;
		}
		resultStatus = preExecuteSelect(command, previousStatus, info);
		if (resultStatus != null) {
			return resultStatus;
		}
		return null;
	}

	private PreExecuteStatus preExecuteSelect(final Command command,
			final PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		if (command instanceof SelectCommand) {
			final SelectCommand selectCmd = (SelectCommand) command;
			final SelectData data = selectCmd.getData();
			// XXX no need activate parent part if kind is window
			if (data.getParent() != null
					&& !data.getKind().equals(ElementKind.Window.name())) {
				boolean onlyOpen = true;
				// Issue: Menu is not accessible if view is not activated
				if (data.getKind().equals(ElementKind.Menu.name())) {
					onlyOpen = false;
					if (getPlayer().cleanMenus(info)) {
						return new PreExecuteStatus(false); // Clean previous
															// menus.
					}
				}
				if (data.getKind().equals(ElementKind.Item.name())) {
					onlyOpen = false;
				}
				if (!activateViewEditor(data.getParent(), onlyOpen, info)) {
					return new PreExecuteStatus(false);
				}
			}
			if (data.getKind().equals(ElementKind.Item.name())) {

				// In case of item
				final EList<String> path = data.getPath();
				final String[] aPath = path.toArray(new String[path.size()]);
				SWTUIElement parentElement = getMapper().get(data.getParent());
				final int value = Viewers.expandSelection(parentElement, aPath,
						null, null);
				if (value == 0) {
					final ExpandSelectionStatus ess = new ExpandSelectionStatus(
							false);
					if (previousStatus instanceof ExpandSelectionStatus) {
						ess.nextLevel = 1 + ((ExpandSelectionStatus) previousStatus).nextLevel;
						if (ess.nextLevel > aPath.length + 1) {
							return null;
						}
					}
					Q7WaitUtils.updateInfo("automatic.expand",
							Arrays.toString(aPath), info);
					return ess;
				}
				if (value == -1) {
					if (previousStatus == null) {
						Q7WaitUtils.updateInfo("automatic.expand",
								Arrays.toString(aPath), info);
						return new PreExecuteStatus(false);
					}
				}
				return doDevirtualize(previousStatus, parentElement, aPath,
						info);
			}
		}
		return null;
	}

	private PreExecuteStatus preExecuteSetSelection(final Command command,
			final PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		if (command instanceof SetSelection) {
			// Update virtual item if required

			final SetSelection cmd = (SetSelection) command;
			final SWTUIElement element = getMapper().get(cmd.getElement());

			String[] itemPath = asStringArray(cmd.getPath());
			int value = element.expandSelection(itemPath, null, null);
			final EList<MultiSelectionItem> additionals = cmd
					.getAdditionalItems();
			if (value == 0) {
				final ExpandSelectionStatus ess = new ExpandSelectionStatus(
						false);
				if (previousStatus instanceof ExpandSelectionStatus) {
					ess.nextLevel = 1 + ((ExpandSelectionStatus) previousStatus).nextLevel;
					if (ess.nextLevel > cmd.getPath().size() + 1) {
						return doDevirtualize(previousStatus, element,
								itemPath, info);
					}
				}
				Q7WaitUtils.updateInfo("automatic.expand",
						Arrays.toString(itemPath), info);
				return ess;
			}
			if (additionals.size() > 0) {
				// Also expand all additional elements
				for (final MultiSelectionItem item : additionals) {
					String[] extraItemPath = asStringArray(item.getPath());
					value = element.expandSelection(extraItemPath, null, null);
					if (value == 0) {
						final ExpandSelectionStatus ess = new ExpandSelectionStatus(
								false);
						if (previousStatus instanceof ExpandSelectionStatus) {
							ess.nextLevel = 1 + ((ExpandSelectionStatus) previousStatus).nextLevel;
							if (ess.nextLevel > item.getPath().size() + 1) {
								return doDevirtualize(previousStatus, element,
										extraItemPath, info);
							}
						}
						Q7WaitUtils.updateInfo("automatic.expand",
								Arrays.toString(itemPath), info);
						return ess;
					}
				}
			}
			return doDevirtualize(previousStatus, element, itemPath, info);
		}
		return null;
	}

	private PreExecuteStatus doDevirtualize(
			final PreExecuteStatus previousStatus, final SWTUIElement element,
			String[] itemPath, Q7WaitInfoRoot info) {
		final Control tableOrTree = (Control) unwrapWidget(element);
		if (!(tableOrTree instanceof Tree || tableOrTree instanceof Table))
			return null;
		if (!TableTreeUtil.isVirtual(element.unwrap())) {
			return null;
		}

		int prevPos = getPrevPosition(previousStatus);
		Set<Item> selection = Viewers.findItems(new String[][] { itemPath },
				tableOrTree, false, prevPos);
		if (selection.isEmpty()) {
			int newpos = Viewers.updateVirtualTableTree(element.unwrap(), prevPos);
			if (newpos == -1) {
				return null;
			}
			// if (previousStatus instanceof DeVirtualizeStatus) {
			// return null; // Already tryed to do devirtualize
			// }
			Q7WaitUtils.updateInfo("automatic.expand.virtual",
					Arrays.toString(itemPath), info);
			return new DeVirtualizeStatus(false, newpos);
		}
		return null;
	}

	private int getPrevPosition(PreExecuteStatus previousStatus) {
		if (previousStatus instanceof DeVirtualizeStatus) {
			return ((DeVirtualizeStatus) previousStatus).position;
		}
		return 0;
	}

	// private PreExecuteStatus doDevirtualize(
	// final PreExecuteStatus previousStatus, final SWTUIElement element) {
	// if (TableTreeUtil.isVirtual(element.unwrap())) {
	// Viewers.updateVirtualTableTree(element.unwrap());
	// if (previousStatus instanceof DeVirtualizeStatus) {
	// return null; // Already tryed to do devirtualize
	// }
	// return new DeVirtualizeStatus(false);
	// }
	// return null;
	// }

	private PreExecuteStatus preExecuteCheckItem(final Command command,
			final PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		if (command instanceof CheckItem) {
			final CheckItem cmd = (CheckItem) command;
			final SWTUIElement element = getMapper().get(cmd.getElement());
			String[] path = asStringArray(cmd.getPath());
			int value = Viewers.expandSelection(element, path, null, null);
			if (value == 0) {
				final ExpandSelectionStatus ess = new ExpandSelectionStatus(
						false);
				if (previousStatus instanceof ExpandSelectionStatus) {
					ess.nextLevel = 1 + ((ExpandSelectionStatus) previousStatus).nextLevel;
					if (ess.nextLevel > cmd.getPath().size() + 1) {
						return null;
					}
				}
				Q7WaitUtils.updateInfo("automatic.expand",
						Arrays.toString(path), info);
				return ess;
			}
		}
		return null;
	}

	private PreExecuteStatus preExecuteAssert(final Command command,
			final PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		if (command instanceof Assert) {
			final Assert assertCmd = (Assert) command;
			if (assertCmd.getElement().getKind()
					.equalsIgnoreCase(ElementKind.Item.name())) {
				final SWTUIElement element = getMapper().get(
						assertCmd.getElement());

				final Widget item = unwrapWidget(element);
				if (item instanceof Item
						&& !assertCmd.getAttribute().equals("getExpanded()")) {
					getPlayer().exec("Expand asserted item", new Runnable() {
						@Override
						public void run() {
							if (item instanceof TreeItem && !item.isDisposed()
									&& !((TreeItem) item).getExpanded()) {
								Viewers.expandTreeItem(getPlayer(),
										((TreeItem) item).getParent(),
										(TreeItem) item);
							}
						}
					});
				}

				if (previousStatus instanceof ExpandSelectionStatus) {
					if (((ExpandSelectionStatus) previousStatus).nextLevel == 1) {
						// Already waited here.
						return null;
					}
				}
				// Wait for one op
				final ExpandSelectionStatus st = new ExpandSelectionStatus(
						false);
				st.nextLevel = 1;
				Q7WaitUtils.updateInfo("automatic.expand.assert",
						element.getClassName(), info);
				return st;
			}
		}
		return null;
	}

	public SWTElementMapper getMapper() {
		return SWTElementMapper.getMapper(id);
	}

	@Override
	public void initialize(final AbstractTeslaClient client, final String id) {
		this.client = client;
		this.id = id;
		dragSupport.initialize(client, id);
		LocalClipboard.setEnabled(TeslaFeatures.isUseInternalClipboard());
		TeslaEventManager.getManager().setUnhandledNativeDialogHandler(
				new IUnhandledNativeDialogHandler() {
					@Override
					@SuppressWarnings("rawtypes")
					public void handle(Class clazz, String message) {
						failNextCommandBecauseOf = "Failed because of unexpected native dialog is shown: "
								+ clazz.getName() + " message: " + message;
					}
				});
	}

	public SWTUIProcessor() {
	}

	protected CellEditorSupport createCellEditorSupport() {
		return new CellEditorSupport(this);
	}

	public String getId() {
		return id;
	}

	public ElementCommand convertDiagramToCanvasCommand(ElementCommand command,
			Widget figureCanvas) {
		SWTUIElement swtuiElement = getPlayer().wrap(figureCanvas);
		Element element = getMapper().get(swtuiElement);
		ElementCommand redirectedCommand = EcoreUtil.copy(command);
		redirectedCommand.setElement(element);
		return redirectedCommand;
	}

	@Override
	public Response executeCommand(final Command command,
			final IElementProcessorMapper mapper) {
		Throwable error = getPlayer().getError();
		if (error != null) {
			throw new RuntimeException("Previous command has failed", error);
		}
		try {
			if (command instanceof GetBounds) {
				Element element = ((ElementCommand) command).getElement();
				SWTUIElement widget = getMapper().get(element);
				Rectangle bounds = null;
				if (widget != null) {
					bounds = widget.getBounds();
				}
				return boundsResponse(bounds);
			}
	
			if (command instanceof ElementCommand) {
				Element element = ((ElementCommand) command).getElement();
				SWTUIElement widget = getMapper().get(element);
					
				if (widget != null && widget.isDisposed()) {
					return failResponse(NLS
							.bind(TeslaSWTMessages.CommandProcessor_CannotExecuteCommandOverDisposedElement,
									widget.toString()));
				}
	
				if (!skipModalDialogCheck(command)) {
					Response response = checkForModalDialogs(widget);
					if (response != null) {
						return response;
					}
				}
			}
			if (command.eClass().getEPackage().equals(ProtocolPackage.eINSTANCE)) {
				switch (command.eClass().getClassifierID()) {
				case ProtocolPackage.COUNT_ITEMS:
					return handleCountItems((CountItems) command);
				case ProtocolPackage.GET_TEXT:
					return handleGetText((GetText) command);
				case ProtocolPackage.IS_DISPOSED:
					return handleIsDisposed((IsDisposed) command);
				case ProtocolPackage.IS_ENABLED:
					return handleIsEnabled((IsEnabled) command);
				case ProtocolPackage.SET_SELECTION:
					return handleSetSelection((SetSelection) command);
				case ProtocolPackage.CHECK_ITEM:
					return handleCheckItem((CheckItem) command);
				case ProtocolPackage.SET_TEXT:
					return handleSetText((SetText) command);
				case ProtocolPackage.CLICK:
					return handleClick((Click) command);
				case ProtocolPackage.CHECK:
					return handleCheck((Check) command);
				case ProtocolPackage.DOUBLE_CLICK:
					return handleDoubleClick((DoubleClick) command);
				case ProtocolPackage.CLOSE:
					return handleSendClose((Close) command);
				case ProtocolPackage.TYPE_TEXT:
					return handleTypeText((TypeText) command);
				case ProtocolPackage.TYPE:
					return handleType((Type) command);
				case ProtocolPackage.TYPE_ACTION:
					return handleTypeAction((TypeAction) command);
				case ProtocolPackage.SHOW:
					return handleShow((Show) command);
				case ProtocolPackage.DRAG_COMMAND:
					return handleDrag((DragCommand) command);
				case ProtocolPackage.CHILDREN:
					return handleChildren((Children) command);
				case ProtocolPackage.ACTIVATE_CELL_EDITOR:
					return getCellEditorSupport().handleActivateCellEditor(
							(ActivateCellEditor) command);
				case ProtocolPackage.APPLY_CELL_EDITOR:
					return getCellEditorSupport().handleApplyCellEditor(
							(ApplyCellEditor) command);
				case ProtocolPackage.CANCEL_CELL_EDITOR:
					return getCellEditorSupport().handleCancelCellEditor(
							(CancelCellEditor) command);
				case ProtocolPackage.DEACTIVATE_CELL_EDITOR:
					return getCellEditorSupport().handleDeactivateCellEditor(
							(DeactivateCellEditor) command);
				case ProtocolPackage.SET_SWT_DIALOG_INFO:
					return handleSetSWTDialogInfo((SetSWTDialogInfo) command);
				case ProtocolPackage.SET_TEXT_OFFSET:
					return handleSetTextOffset((SetTextOffset) command);
				case ProtocolPackage.HOVER_AT_TEXT_OFFSET:
					return handleHoverAtTextOffset((HoverAtTextOffset) command);
				case ProtocolPackage.GET_SELECTION:
					return handleGetSelection((GetSelection) command);
				case ProtocolPackage.CELL_CLICK:
					return handleCellClick((CellClick) command);
				case ProtocolPackage.CLICK_ABOUT_MENU:
					return handleClickAboutMenu((ClickAboutMenu) command);
				case ProtocolPackage.CLICK_PREFERENCES_MENU:
					return handleClickPreferencesMenu((ClickPreferencesMenu) command);
				case ProtocolPackage.MINIMIZE:
					return handleMinimize((Minimize) command);
				case ProtocolPackage.MAXIMIZE:
					return handleMaximize((Maximize) command);
				case ProtocolPackage.RESTORE:
					return handleRestore((Restore) command);
				case ProtocolPackage.SHOW_TAB_LIST:
					return handleShowTabList((ShowTabList) command);
				case ProtocolPackage.SET_STATUS_DIALOG_MODE:
					return handleSetStatusDialogMode((SetStatusDialogMode) command);
				case ProtocolPackage.CLICK_LINK:
					return handleClickLink((ClickLink) command);
				case ProtocolPackage.SET_FOCUS:
					return handleSetFocus((SetFocus) command);
				case ProtocolPackage.CLICK_TEXT:
					return handleClickText((ClickText) command);
				case ProtocolPackage.DOUBLE_CLICK_TEXT:
					return handleDoubleClickText((DoubleClickText) command);
				case ProtocolPackage.SAVE:
					return handleSave((Save) command);
				case ProtocolPackage.IS_DIRTY:
					return handleIsDirty((IsDirty) command);
				case ProtocolPackage.SHOW_SELECTION:
					return handleShowSelection((ShowSelection) command);
				case ProtocolPackage.SET_TEXT_SELECTION:
					return handleSetTextSelection((SetTextSelection) command);
				case ProtocolPackage.GET_TEXT_SELECTION:
					return handleGetTextSelection((GetTextSelection) command);
				case ProtocolPackage.GO_TO_TEXT_LINE:
					return handleGoToTextLine((GoToTextLine) command);
				case ProtocolPackage.GET_TEXT_LINE_OFFSET:
					return handleGetTextLineOffset((GetTextLineOffset) command);
				case ProtocolPackage.GET_TEXT_LINE_LENGTH:
					return handleGetTextLineLength((GetTextLineLength) command);
				case ProtocolPackage.SELECT_TEXT_LINE:
					return handleSelectTextLine((SelectTextLine) command);
				case ProtocolPackage.GET_TEXT_LINE:
					return handleGetTextLine((GetTextLine) command);
				case ProtocolPackage.GET_TEXT_RANGE:
					return handleGetTextRange((GetTextRange) command);
				case ProtocolPackage.PASTE_TEXT_SELECTION:
					return handlePasteText((PasteTextSelection) command);
				case ProtocolPackage.COPY_TEXT_SELECTION:
					return handleCopyText((CopyTextSelection) command);
				case ProtocolPackage.CUT_TEXT_SELECTION:
					return handleCutText((CutTextSelection) command);
				case ProtocolPackage.REPLACE_TEXT_SELECTION:
					return handleReplaceText((ReplaceTextSelection) command);
				case ProtocolPackage.ASSERT:
					return handleAssert((Assert) command);
				case ProtocolPackage.GET_REGION_TEXT:
					return handleGetRegionText((GetRegionText) command);
				case ProtocolPackage.GET_PROPERTY_VALUE:
					return handleGetPropertyValue((GetPropertyValue) command);
				case ProtocolPackage.EXPAND:
					return handleExpand((Expand) command);
				case ProtocolPackage.COLLAPSE:
					return handleCollapse((Collapse) command);
				case ProtocolPackage.CLICK_COLUMN:
					return handleClickColumn((ClickColumn) command);
				case ProtocolPackage.MOUSE_EVENT:
					return handleMouseEvent((MouseEvent) command);
				case ProtocolPackage.GET_ITEMS:
					return handleGetItems((GetItems) command);
				}
			}
			return null;
		} catch (ClosedException e) {
			return failResponse("Disposed");
		}

	}

	private Response handleMouseEvent(final MouseEvent command) {
		final SWTUIElement element = getMapper().get(command.getElement());

		Widget widget = unwrapWidget(element);
		if (!isWidgetSupported(widget)) {
			return failResponse(String.format(
					"Unsupported widget '%s'. 'mouse' supports only"
							+ " controls and table/tree items",
					widget
							.getClass().getName()));
		}

		final boolean isItem = widget instanceof Item;
		Point pos = getMouseEventPoint(command, element.getBounds(), isItem);
		final Widget parent = isItem ? getItemParent(widget) : widget;
		final Widget item = isItem ? widget : null;

		final Event event = new Event();
		event.count = command.getCount();
		event.button = command.getButton();
		event.x = pos.x;
		event.y = pos.y;
		event.stateMask = command.getStateMask();
		event.type = mouseToEventType.get(command.getKind());

		final SWTUIPlayer player = getPlayer();
		player.exec("Sending mouse event", new Runnable() {
			@Override
			public void run() {
				if ((parent instanceof Table || parent instanceof Tree)
						&& isItem && TableTreeUtil.getHeaderVisible(parent)
						&& element instanceof ItemUIElement) {

					// getPlayer().getEvents().setFocusControl(parent);
					ItemUIElement itemUIElement = (ItemUIElement) element;

					TableTreeUtil.showItem(item);
					TableTreeUtil.showColumn(TableTreeUtil.getColumn(parent,
							itemUIElement.getColumn()));
					List<Object> selection = Arrays.asList(TableTreeUtil
							.getSelection(parent));
					if (!selection.contains(item)) {
						TableTreeUtil.setSelection(parent, item);
					}

					// handle all the new events
					while (parent.getDisplay().readAndDispatch())
						; // nothing to do

					if (!parent.isDisposed() && !item.isDisposed()) {
						Point newPos = getMouseEventPoint(command,
								element.getBounds(), isItem);
						event.x = newPos.x;
						event.y = newPos.y;
					}
				}

				player.getEvents().sendEvent(parent, item, event);
			}
		});

		return okResponse();
	}

	private static Widget getItemParent(Widget widget) {
		return TableTreeUtil.getParent(widget);
	}

	private static boolean isWidgetSupported(Widget widget) {
		return widget instanceof Item ? (widget instanceof TableItem || widget instanceof TreeItem)
				: true;
	}

	private static Point getMouseEventPoint(MouseEvent cmd, Rectangle bounds,
			boolean isItem) {
		int x = cmd.getX();
		int y = cmd.getY();
		Point center = isItem ? centerAbs(bounds) : centerRel(bounds);
		Point topLeft = isItem ? new Point(bounds.x, bounds.y)
				: new Point(0, 0);
		x = x == Integer.MIN_VALUE ? center.x : (x + topLeft.x);
		y = y == Integer.MIN_VALUE ? center.y : (y + topLeft.y);
		return new Point(x, y);
	}

	private static final Map<MouseEventKind, Integer> mouseToEventType = new HashMap<>();

	static {
		mouseToEventType.put(MouseEventKind.DOUBLE_CLICK, SWT.MouseDoubleClick);
		mouseToEventType.put(MouseEventKind.DOWN, SWT.MouseDown);
		mouseToEventType.put(MouseEventKind.ENTER, SWT.MouseEnter);
		mouseToEventType.put(MouseEventKind.EXIT, SWT.MouseExit);
		mouseToEventType.put(MouseEventKind.MOVE, SWT.MouseMove);
		mouseToEventType.put(MouseEventKind.UP, SWT.MouseUp);
		mouseToEventType.put(MouseEventKind.HOVER, SWT.MouseHover);
	}

	private Response handleClickColumn(ClickColumn command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Widget widget = unwrapWidget(element);
		if (!(widget instanceof Tree) && !(widget instanceof Table)) {
			return failResponse("Can click column header only on table or tree");
		}
		final String columnName = command.getName();
		final int columnIndex = command.getIndex();

		Widget toClick = TableTreeUtil.findColumn(widget, columnName,
				columnIndex);
		if (toClick == null) {
			return failResponse(String.format(
					"Cannot find column with name (%s) and index(%d)",
					columnName, columnIndex));
		}
		Event event = new Event();
		event.type = SWT.Selection;
		getPlayer().getEvents().sendEvent(toClick, event);

		return okResponse();
	}

	protected Response handleExpand(final Expand command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Widget widget = unwrapWidget(element);
		if (!(widget instanceof TreeItem)) {
			return failResponse("Can only expand tree item");
		}

		Viewers.expandTreeItem(getPlayer(), ((TreeItem) widget).getParent(),
				(TreeItem) widget);

		return okResponse();
	}

	protected Response handleCollapse(final Collapse command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Widget widget = unwrapWidget(element);
		if (!(widget instanceof TreeItem)) {
			return failResponse("Can only collapse tree item");
		}
		Viewers.collapseTreeItem(getPlayer(), ((TreeItem) widget).getParent(),
				(TreeItem) widget);
		return okResponse();
	}

	protected Response handleClickLink(final ClickLink command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final String text = element.getText();
		final String ref = command.getRef() == null ? LinkUIElement
				.firstRef(text) : command.getRef();

		if (ref == null) {
			return failResponse(TeslaSWTMessages.SWTUIProcessor_NoLinkFragments);
		}

		if (!(element.widget instanceof Link)) {
			return failResponse(TeslaSWTMessages.SWTUIProcessor_WidgetIsNotLink);
		}

		if (!(element.getText().contains(ref))) {
			return failResponse(NLS.bind(
					TeslaSWTMessages.SWTUIProcessor_InvalidLinkRef, ref));
		}

		if (element.isDisabled()) {
			return failResponse(TeslaSWTMessages.SWTUIProcessor_LinkDisabled);
		}

		getPlayer().exec("clickLink", new Runnable() { //$NON-NLS-1$

			@Override
			public void run() {
				// TODO Auto-generated method stub
				SWTEvents events = new SWTEvents(getPlayer()
						.getDisplay());
				Event event = new Event();
				event.type = SWT.Selection;
				event.text = ref;
				events.sendEvent(element, event);
			}
		});
		return okResponse();
	}

	protected Response handleSetFocus(final SetFocus command) {
		final SWTUIElement element = getMapper().get(command.getElement());

		if (!(element.widget instanceof Control)) {
			return failResponse(TeslaSWTMessages.SWTUIProcessor_SetFocusWidgetIsNotControl);
		}
		final Control control = (Control) element.widget;
		getPlayer().exec("setFocus", new Runnable() { //$NON-NLS-1$
			@Override
			public void run() {
				SWTEvents events = new SWTEvents(getPlayer()
						.getDisplay());
				if (command.isValue()) {
					try {
						org.eclipse.rcptt.util.ShellUtilsProvider.getShellUtils().forceActive(control.getShell());
					}
					catch (CoreException e) {
						throw new RuntimeException(e);
					}
					control.setFocus();
					control.forceFocus();
					events.sendEvent(control, SWT.MouseEnter);
					events.sendEvent(control, SWT.MouseMove);
					events.sendEvent(control, SWT.Activate);
					events.sendEvent(control, SWT.FocusIn);
				}
				else {
					if (!control.isDisposed()) {
						events.sendEvent(control, SWT.FocusOut);
					}
					if (!control.isDisposed()) {
						events.sendEvent(control, SWT.Deactivate);
					}
					if (!control.isDisposed()) {
						events.sendEvent(control, SWT.MouseMove);
					}
					if (!control.isDisposed()) {
						events.sendEvent(control, SWT.MouseExit);
					}
				}
			}
		});
		return okResponse();
	}

	protected Response handleClickText(final ClickText command) {
		final SWTUIElement element = getMapper().get(command.getElement());

		if (!(element.widget instanceof Text)) {
			return failResponse("Unsupported widget.");
		}
		final Text control = (Text) element.widget;
		final int start;
		final int end;
		if (command.getStart() != null && !command.getStart().isEmpty()) {
			boolean multi = (control.getStyle() & SWT.MULTI) != 0;
			if (multi) {
				StringLines lines = new StringLines(control.getText());
				start = lines.calcOffset(command.getStart());
				end = ((command.getEnd() != null && command.getEnd().trim()
						.length() > 0) ? lines.calcOffset(command.getEnd())
								: start);
			}
			else { // single line case
				start = StringLines.parseSingleLineCoord(command.getStart());
				end = ((command.getEnd() != null && command.getEnd().trim()
						.length() > 0) ? StringLines
								.parseSingleLineCoord(command.getEnd()) : start);
			}
		}
		else {
			Point selection = control.getSelection();
			start = selection.x;
			end = selection.y;
		}
		if (start == -1 || control.getCharCount() < start) {
			return failResponse("Invalid start location is specified for click-text: "
					+ command.getStart());
		}
		if (end == -1 || control.getCharCount() < end) {
			return failResponse("Invalid end location is specified for click-text: "
					+ command.getEnd());
		}

		getPlayer().exec("clickText", new Runnable() { //$NON-NLS-1$
			@Override
			public void run() {
				SWTEvents events = new SWTEvents(getPlayer()
						.getDisplay());
				try {
					ShellUtilsProvider.getShellUtils().forceActive(control.getShell());
				}
				catch (CoreException e) {
					throw new RuntimeException(e);
				}
				events.sendFocus(control);
				control.setSelection(start, end);
				// Do a mouse Down/Up for a selected position
				int button = TeslaUtils.buttonNameToInt(command
						.getButton());
				Point size = control.getSize();
				events.sendEvent(control, SWT.MouseDown, size.x / 2, size.y / 2, button);
				events.sendEvent(control, SWT.MouseUp, size.x / 2, size.y / 2, button);
				// Do a selection set one more time.
				control.setSelection(start, end);
			}

		});
		return okResponse();
	}

	protected Response handleDoubleClickText(final DoubleClickText command) {
		final SWTUIElement element = getMapper().get(command.getElement());

		if (!(element.widget instanceof Text)) {
			return failResponse("Unsupported widget.");
		}
		final Text control = (Text) element.widget;
		final String lineCol = command.getPosition();
		final int pos;
		if (lineCol != null && !lineCol.isEmpty()) {
			boolean multi = (control.getStyle() & SWT.MULTI) != 0;
			pos = multi ? new StringLines(control.getText())
					.calcOffset(lineCol) : StringLines
							.parseSingleLineCoord(lineCol);
		}
		else
			pos = -1;

		if (pos == -1 || control.getCharCount() < pos)
			return failResponse("Invalid location is specified for double-click-text: "
					+ lineCol);

		// -- find the start and the end, emulates behavior on windows;
		// the case of clicking at line beginning when it starts with
		// whitespaces
		// and the case of mixed spaces & tabs are not covered

		String text = control.getText();
		int start = (pos > 0) ? pos - 1 : 0;
		while (start > 0 && isOneLineSpace(text.charAt(start)))
			--start;
		while (start > 0 && !Character.isWhitespace(text.charAt(start - 1)))
			--start;
		int end = (pos > 0) ? pos - 1 : 0;
		while (end < text.length() && !Character.isWhitespace(text.charAt(end)))
			++end;
		while (end < text.length() && isOneLineSpace(text.charAt(end)))
			++end;

		// --

		final int finalStart = Math.max(0, start);
		final int finalEnd = Math.min(text.length(), end);

		getPlayer().exec("double-click-text", new Runnable() { //$NON-NLS-1$
			@Override
			public void run() {
				SWTEvents events = new SWTEvents(getPlayer()
						.getDisplay());
				try {
					ShellUtilsProvider.getShellUtils().forceActive(control.getShell());
				}
				catch (CoreException e) {
					throw new RuntimeException(e);
				}
				events.sendFocus(control);
				control.setSelection(finalStart, finalEnd);

				int button = TeslaUtils.buttonNameToInt(command
						.getButton());
				Point size = control.getSize();
				events.sendEvent(control, SWT.MouseDown, size.x / 2, size.y / 2, button);
				events.sendEvent(control, SWT.MouseUp, size.x / 2, size.y / 2, button);
				events.sendEvent(control, SWT.MouseDown, size.x / 2, size.y / 2, button);
				events.sendEvent(control, SWT.MouseUp, size.x / 2, size.y / 2, button);

				control.setSelection(finalStart, finalEnd);
				events.sendEvent(control, SWT.MouseDoubleClick, size.x / 2, size.y / 2,
						button);
			}

		});
		return okResponse();
	}

	/**
	 * @return true if character is whitespace, but not newline
	 */
	private boolean isOneLineSpace(char c) {
		return Character.isWhitespace(c) && c != '\r' && c != '\n';
	}

	public static Response failResponse(String message) {
		Response result = RawFactory.eINSTANCE.createResponse();
		result.setStatus(ResponseStatus.FAILED);
		result.setMessage(message);
		return result;
	}

	public static Response boundsResponse(Rectangle bounds) {
		BoundsResponse response = ProtocolFactory.eINSTANCE
				.createBoundsResponse();
		if (bounds != null) {
			response.setX(bounds.x);
			response.setY(bounds.y);
			response.setWidth(bounds.width);
			response.setHeight(bounds.height);
		}
		return response;
	}

	public static Response okResponse() {
		Response response = RawFactory.eINSTANCE.createResponse();
		response.setStatus(ResponseStatus.OK);
		return response;
	}

	private Response handleSetStatusDialogMode(SetStatusDialogMode command) {
		Response response = RawFactory.eINSTANCE.createResponse();
		TeslaEventManager.getManager().setStatusDialogModeAllowed(
				command.isEnabled());
		return response;
	}

	private Response handleShowTabList(ShowTabList command) {
		SWTUIElement element = getMapper().get(command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			getPlayer().showTabList(element);
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	private Response handleRestore(Restore command) {
		SWTUIElement element = getMapper().get(command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			getPlayer().restore(element);
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	private Response handleMaximize(Maximize command) {
		SWTUIElement element = getMapper().get(command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			if (element instanceof WorkbenchUIElement) {
				getPlayer().click(element);
			}
			getPlayer().maximize(element);
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	private Response handleMinimize(Minimize command) {
		SWTUIElement element = getMapper().get(command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			getPlayer().minimize(element);
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleSave(Save command) {
		SWTUIElement element = getMapper().get(command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			getPlayer().save(element);
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleIsDirty(IsDirty command) {
		SWTUIElement element = getMapper().get(command.getElement());
		BooleanResponse response = factory.createBooleanResponse();
		if (element != null) {
			response.setResult(getPlayer().isDirty(element));
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	private Response handleGetRegionText(GetRegionText command) {
		final Element element = command.getElement();
		final SWTUIElement uiElement = getMapper().get(element);
		if (uiElement == null) {
			return assertResponse(ResponseStatus.FAILED,
					TeslaSWTMessages.CommandProcessor_CannotFindWidget);
		}
		IWidgetDescriber descr = new WidgetDescriber(uiElement.unwrap());
		return handleGetRegionText(command, descr);
	}

	public Response handleGetRegionText(GetRegionText command,
			IWidgetDescriber descr) {
		List<IImageAssertSupport> processors = client
				.getProcessors(IImageAssertSupport.class);
		if (processors.isEmpty()) {
			return assertResponse(ResponseStatus.FAILED,
					TeslaSWTMessages.SWTUIProcessor_OcrNotSupported);
		}
		Image image = descr.captureImage();
		try {
			Rectangle bounds = image.getBounds();
			Rectangle subRect = new Rectangle(0, 0, 0, 0);
			double mult_x = 1.0f * bounds.width / command.getWidth();
			if (Math.abs(1 - mult_x) < 0.03f) {
				mult_x = 1;
			}
			double mult_y = 1.0f * bounds.width / command.getWidth();
			if (Math.abs(1 - mult_y) < 0.03f) {
				mult_y = 1;
			}
			subRect.x = (int) (command.getX() * mult_x);
			subRect.width = (int) (command.getSx() * mult_x);
			subRect.y = (int) (command.getY() * mult_y);
			subRect.height = (int) (command.getSy() * mult_y);
			Image sub = SWTUIPlayer.copyImagePart(image, subRect);
			for (IImageAssertSupport support : processors) {
				String text = support.recognize(sub);
				text = replaceMultilines(text);
				if (text != null) {
					GetTextResponse response = ProtocolFactory.eINSTANCE
							.createGetTextResponse();
					response.setText(text);
					response.setStatus(ResponseStatus.OK);
					return response;
				}
			}
		}
		finally {
			image.dispose();
		}
		return failResponse(TeslaSWTMessages.SWTUIProcessor_CannotRecognizeText);
	}

	private boolean skipModalDialogCheck(Command command) {
		if (command instanceof DragCommand
				|| command instanceof ApplyCellEditor) {
			return true;
		}
		return false;
	}

	public static Response checkForModalDialogs(SWTUIElement element) {
		if (element == null) {
			return null;
		}
		Shell modal = getModalChild(element);
		if (modal == null) {
			return null;
		}
		return failResponse(TeslaSWTMessages
				.bind(TeslaSWTMessages.CommandProcessor_CannotExecuteCommandBecauseOfModalDialog,
						modal.getText(), ((Shell) modal.getParent()).getText()));
	}

	private Response handleCellClick(CellClick command) {
		final Response response = RawFactory.eINSTANCE.createResponse();
		final SWTUIElement element = getMapper().get(command.getElement());
		if (element != null) {
			Object unwrap = unwrap(element);
			if (unwrap instanceof Control) {
				Viewer viewer = TeslaSWTAccess.getViewer((Control) unwrap);
				if (viewer instanceof ColumnViewer) {
					ColumnViewer cv = (ColumnViewer) viewer;
					Control control = cv.getControl();
					if (control instanceof Table) {
						Table tab = (Table) control;
						TableItem[] items = tab.getSelection();
						if (items.length == 1) {
							Rectangle bounds = items[0].getBounds(command
									.getColumn());
							getPlayer().getEvents().sendEvent(element,
									SWT.MouseDown, bounds.x + bounds.width / 2,
									bounds.y + bounds.height / 2, 1);
							getPlayer().getEvents().sendEvent(element,
									SWT.MouseUp, bounds.x + bounds.width / 2,
									bounds.y + bounds.height / 2, 1);
						}
					}
				}
			}
			else if (unwrap instanceof Item) {
				if (unwrap instanceof TableItem) {
					TableItem item = (TableItem) unwrap;
					Rectangle bounds = item.getBounds(command.getColumn());
					getPlayer().getEvents().sendEvent(item.getParent(),
							SWT.MouseDown, bounds.x + bounds.width / 2,
							bounds.y + bounds.height / 2, 1);
					getPlayer().getEvents().sendEvent(item.getParent(),
							SWT.MouseUp, bounds.x + bounds.width / 2,
							bounds.y + bounds.height / 2, 1);
				}
				else if (unwrap instanceof TreeItem) {
					TreeItem item = (TreeItem) unwrap;
					Rectangle bounds = item.getBounds(command.getColumn());
					getPlayer().getEvents().sendEvent(item.getParent(),
							SWT.MouseDown, bounds.x + bounds.width / 2,
							bounds.y + bounds.height / 2, 1);
					getPlayer().getEvents().sendEvent(item.getParent(),
							SWT.MouseUp, bounds.x + bounds.width / 2,
							bounds.y + bounds.height / 2, 1);
				}
			}
		}
		return response;
	}

	private Response handleGetSelection(GetSelection command) {
		SWTUIElement element = getMapper().get(command.getElement());
		SelectionResponse response = factory.createSelectionResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		String[][] selection = Viewers.getMultiPathSelection(element);
		for (String[] strings : selection) {
			SelectionItem item = factory.createSelectionItem();
			item.getText().addAll(Arrays.asList(strings));
			response.getValues().add(item);
		}
		return response;
	}

	private Response handleSetTextOffset(final SetTextOffset command) {
		Element element = command.getElement();
		SWTUIElement widget = getMapper().get(element);
		Widget control = unwrapWidget(widget);
		if (control instanceof StyledText) {
			getPlayer().setTextOffset((StyledText) control, command.getOffset(), command.getLine());
		}
		return factory.createBooleanResponse();
	}

	private Response handleHoverAtTextOffset(final HoverAtTextOffset command) {
		final Response result = factory.createBooleanResponse();
		final Element element = command.getElement();
		final SWTUIElement widget = getMapper().get(element);
		final Widget control = unwrapWidget(widget);

		if (!(control instanceof StyledText)) {
			return result;
		}

		final StyledText styledText = (StyledText) control;
		final int line = command.getLine();
		if (line < 0 || line >= styledText.getLineCount()) {
			result.setStatus(ResponseStatus.FAILED);
			result.setMessage(String.format(
					"Line %d is out of range - must be in range 0..%d", line,
					styledText.getLineCount() - 1));
			return result;
		}

		final int offset = command.getOffset()
				+ styledText.getOffsetAtLine(line);
		if (offset < 0 || offset >= styledText.getContent().getCharCount()) {
			result.setStatus(ResponseStatus.FAILED);
			result.setMessage(String.format(
					"Final offset %d is out of range, must be in range 0..%d",
					offset, styledText.getCharCount() - 1));
			return result;
		}

		getPlayer().exec("Hover at text offset", new Runnable() {
			@Override
			public void run() {
				styledText.setTopIndex(line);
				Point point = styledText.getLocationAtOffset(offset);
				SWTEvents events = getPlayer().getEvents();
				final Event e = events.createEvent(styledText);
				e.x = point.x;
				e.y = point.y;
				getPlayer().addMouseWidgetInfo(styledText, e.x, e.y);
				events.sendEvent(styledText.getShell(), SWT.Deactivate);
				try {
					ShellUtilsProvider.getShellUtils().forceActive(styledText.getShell());
				}
				catch (CoreException exc) {
					throw new RuntimeException(exc);
				}
				styledText.forceFocus();
				e.type = SWT.MouseMove;
				events.sendEvent(styledText, e);
				e.type = SWT.MouseHover;
				events.sendEvent(styledText, e);
			}
		});

		return result;
	}

	public boolean activateViewEditor(final Element cmdElement,
			boolean onlyOpen, Q7WaitInfoRoot info) {
		try {
			final SWTUIElement element = getMapper().get(cmdElement);
			if (element != null) {
				return activateViewEditor(element, onlyOpen, info);
			}
		} catch (ClosedException e) {
			return true;
		}
		return true;
	}

	public boolean activateViewEditor(final SWTUIElement element,
			boolean onlyOpen, Q7WaitInfoRoot info) {
		// final SWTUIPlayer player = element.getPlayer();
		// final Widget widget = unwrapWidget(element);

		SWTUIElement sh = SWTUIPlayer.getShell(element);
		if (sh == null || sh.widget == null) {
			TeslaCore.log("Failed to locate shell for:"
					+ ((element != null) ? element.toString() : ""));
			return true;
		}
		final Shell shell = (Shell) sh.widget;
		if (SKIP_ACTIVATION_FOR_SHELLS.contains(getShellCreationMethodName(shell))) {
			return true;
		}
		Shell activeShell = TeslaEventManager.getActiveShell();
		if (activeShell != shell) {
			Q7WaitUtils.updateInfo("shell.activate",
					shell.getClass().getName(), info);
			getPlayer().exec("Activate shell", new Runnable() {
				@Override
				public void run() {
					// Do deactivate for all other shells

					Display display = shell.getDisplay();
					Shell[] shells = display.getShells();
					for (Shell sh : shells) {
						if (!shell.equals(sh)) {
							if (sh.isVisible()) {
								// getPlayer().getEvents().sendEvent(sh,
								// SWT.FocusOut);
								getPlayer().getEvents().sendEvent(sh,
										SWT.Deactivate);
							}
						}
					}

					shell.setActive();
					// shell.forceActive();
					TeslaEventManager.setActiveShell(shell);

					// seems like for linux systems event triggered by shell.setActive().
					// the following code prevent triggering of the same event twice.
					if (!org.eclipse.jface.util.Util.isLinux()) {
						getPlayer().getEvents().sendEvent(shell, SWT.Activate);
					}

				}
			});
			return false;
		}

		List<SWTUIElement> parentsList = getPlayer().getParentsList(element);
		parentsList.add(element);
		for (SWTUIElement e : parentsList) {
			final GenericElementKind kind = e.getKind();
			if (kind.is(ElementKind.View) || kind.is(ElementKind.Editor)) {
				if (e instanceof WorkbenchUIElement) {
					final IWorkbenchPartReference reference = ((WorkbenchUIElement) e)
							.getReference();
					final boolean visible = EclipseWorkbenchProvider
							.getProvider().isVisible(reference);
					if (!visible) {
						IWorkbenchPage page = reference.getPage();
						if (!EclipseWorkbenchProvider.getProvider()
								.isActiveContainsView(page, reference)) {
							return true;
						}
						Q7WaitUtils.updateInfo("view.activate",
								reference.getId(), info);
						e.click();
						return false;
					}
					if (!onlyOpen) {
						IWorkbenchPart part = reference.getPart(true);
						if (part != null) {
							IWorkbenchPage page = reference.getPage();
							IWorkbenchPart activePart = page.getActivePart();
							if (!(part.equals(activePart))) {
								page.activate(part);
								part.setFocus();
								break;
							}
							final CellEditor[] editors = TeslaCellEditorManager
									.getInstance().getEditors();
							if (editors.length == 0) {
								if (part != null && !part.equals(activePart)) {
									if (!(part.equals(activePart))) {
										try {
											page.activate(part);
											part.setFocus();
										}
										catch (RuntimeException re) {
											TeslaCore.log(re);
											return true;
										}
									}
									Q7WaitUtils.updateInfo("editor.activate",
											reference.getId(), info);
									return false; // lets do it in next
									// cycle
								}
							}
						}
					}
				}
				break;
			}
		}
		return true;
	}

	private Response handleSetSWTDialogInfo(final SetSWTDialogInfo command) {
		final boolean isCanceled = command.getPath() == null || command.getPath().size() == 0;
		switch (command.getKind()) {
		case FILE_SELECTOR:
			if (isCanceled) {
				SWTDialogManager.addFileDialogInfo(null);
			}
			else {
				boolean fileProcessSuccess = true;
				String problemPath = "";
				for (String currentPath : command.getPath()) {
					fileProcessSuccess = fileProcessSuccess
							&& processFileDialogValue(currentPath);
					if (!fileProcessSuccess) {
						problemPath = currentPath;
						break;
					}
				}

				if (!fileProcessSuccess) {
					SWTDialogManager.resetFileDialogInfo();
					final BooleanResponse response = factory
							.createBooleanResponse();
					response.setResult(false);
					response.setMessage("Failed to map file location:"
							+ problemPath + " to workspace location");
					response.setStatus(ResponseStatus.FAILED);
					return response;
				}
			}
			break;
		case FOLDER_SELECTOR:
			if (isCanceled) {
				SWTDialogManager.addFolderDialogInfo(null);
			}
			else {
				String resolvedPath = resolvePath(command.getPath().get(0));
				if (resolvedPath != null) {
					SWTDialogManager.addFolderDialogInfo(resolvedPath);
				}
			}
			break;
		case MESSAGE_BOX:
			if (isCanceled) {
				SWTDialogManager.addMessageBoxInfo(SWT.CANCEL);
			}
			else {
				SWTDialogManager.addMessageBoxInfo(Integer.valueOf(command
						.getPath().get(0)));
			}
			break;
		case FONT_DIALOG:
			if (isCanceled) {
				SWTDialogManager.addFontInfo(null);
			}
			else {
				SWTDialogManager
						.addFontInfo(new FontData(command.getPath().get(0)));
			}
			break;
		case COLOR:
			if (isCanceled) {
				SWTDialogManager.addColorInfo(null);
			}
			else {
				SWTDialogManager.addColorInfo(getColor(command.getPath().get(0)));
			}
			break;
		}
		final BooleanResponse response = factory.createBooleanResponse();
		response.setResult(true);
		return response;
	}

	private static final String PLATFORM_SCHEMA = "platform:";
	private static final String PLATFORM_RESOURCE = String.format(
			"%s/resource/", PLATFORM_SCHEMA);

	/**
	 * If path starts with platform: resolves it, otherwise returns it as is. If path == null or resolution fails, returns null
	 *
	 * @param path
	 * @return
	 */
	private String resolvePath(String path) {
		if (path == null || !(path.startsWith(PLATFORM_SCHEMA))) {
			return path;
		}

		// Try to resolve using FileLocator
		try {
			return new Path(FileLocator.toFileURL(new URL(path)).getPath())
					.toOSString();
		}
		catch (MalformedURLException e) {
			return null; // can we throw an exception with meaningful message?
		}
		catch (IOException e) {
			if (!path.startsWith(PLATFORM_RESOURCE)) {
				return null;
			}
		}

		return ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.append(new Path(path.substring(PLATFORM_RESOURCE.length())))
				.toOSString();
	}

	private boolean processFileDialogValue(String path) {
		String resolvedPath = resolvePath(path);
		if (resolvedPath == null) {
			return false;
		}
		SWTDialogManager.addFileDialogInfo(resolvedPath);
		return true;
	}

	private RGB getColor(final String path) {
		final String[] split = path.split(",");
		return new RGB(Integer.valueOf(split[0]), Integer.valueOf(split[1]),
				Integer.valueOf(split[2]));
	}

	protected Response handleGetPropertyValue(final GetPropertyValue command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		if (element == null) {
			return null;
		}
		ObjectResponse result = ProtocolFactory.eINSTANCE
				.createObjectResponse();

		try {
			result.setResult(getPropertyValue(element, command.getName(),
					command.getIndex(), command.getAllowRawValues()));
		}
		catch (CoreException e) {
			result.setStatus(ResponseStatus.FAILED);
			result.setResult(e.getStatus());
			result.setMessage(e.getMessage());
		}
		return result;
	}

	private Object getPropertyValue(SWTUIElement element, String property,
			Integer index, boolean allowRawValues) throws CoreException {
		Object value = getAttrValue(SWTModelMapper.map(element), property,
				index);
		if (value != null) {
			return value;
		}
		try {
			return (allowRawValues) ? SWTModelMapper.getRawPropertyValue(
					element, property) : SWTModelMapper.getPropertyValue(
							element, property);
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SWTTeslaActivator.PLUGIN_ID, String.format(
							"Failed to get property '%s'", property),
					e));
		}
	}

	private Response handleAssert(final Assert assertCmd) {
		final Element element = assertCmd.getElement();
		final SWTUIElement uiElement = getMapper().get(element);
		if (uiElement == null) {
			// return assertResponse(ResponseStatus.FAILED,
			// "Widget are not found");
			return null;
		}
		if (uiElement.isDisposed()) {
			return assertResponse(ResponseStatus.FAILED, "Assertion "
					+ assertCmd.getAttribute()
					+ " failed. Widget is already disposed.");
		}
		if (assertCmd.getKind().equals(AssertKind.CONTAINS_IMAGE)
				|| assertCmd.getKind().equals(AssertKind.IMAGE_CONTAINS_TEXT)) {
			return doImageAssert(assertCmd,
					new WidgetDescriber(uiElement.unwrap()));
		}
		String attrValue = assertCmd.getValue();
		if (attrValue == null) {
			attrValue = "";
		}
		final org.eclipse.rcptt.tesla.core.ui.Widget model = uiElement.getModel();
		Object value = getAttrValue(model, assertCmd.getAttribute(),
				assertCmd.getIndex());
		if (value == null) {
			try {
				value = SWTModelMapper.getPropertyValue(uiElement,
						assertCmd.getAttribute());
			}
			catch (Exception e) {
				String reason = e.getMessage();
				if (reason == null || reason.length() == 0) {
					StackTraceElement stackTraceElement = e.getStackTrace()[1];
					reason = e.getClass().getSimpleName() + " in " //$NON-NLS-1$
							+ stackTraceElement.getClassName() + "." //$NON-NLS-1$
							+ stackTraceElement.getMethodName() + ":" //$NON-NLS-1$
							+ stackTraceElement.getLineNumber();
				}
				reason = NLS.bind(TeslaSWTMessages.SWTUIProcessor_AssertFailed,
						assertCmd.getAttribute()) + " " + reason; //$NON-NLS-1$
				return assertResponse(ResponseStatus.FAILED, reason);
			}
		}
		if (value == null) {
			return assertResponse(ResponseStatus.FAILED, NLS.bind(
					TeslaSWTMessages.CommandProcessor_CannotFindProperty,
					assertCmd.getAttribute()));
		}
		String strValue = value.toString();
		if (value instanceof EObject) {
			strValue = SimpleCommandPrinter.toString((EObject) value, true)
					.trim();
		}
		return performAssert(assertCmd, attrValue, value, strValue);
	}

	private static final Pattern indexedAttr = Pattern
			.compile("(.*)\\[(\\d+)\\]");

	private static final Pattern stringIndexedAttr = Pattern
			.compile("(.*)\\[\\'(.*)\\'\\]");

	private static final Pattern stringAndNumIndexedAttr = Pattern
			.compile("(.*)\\[\\'(.*)\\'\\]\\[(\\d+)\\]");

	public static Object getAttrValue(EObject object, String attrName,
			Integer index) {
		int lastDotIndex = StringUtils.getAttrLastSplitterInd(attrName);
		if (lastDotIndex != -1) {
			Object parent = getAttrValue(object,
					attrName.substring(0, lastDotIndex), index);
			if (!(parent instanceof EObject)) {
				return null; // consistency error
			}
			object = (EObject) parent;
			attrName = attrName.substring(lastDotIndex + 1);
		}
		String stringKey = null;

		Matcher matcher = stringAndNumIndexedAttr.matcher(attrName);
		if (matcher.matches()) {
			attrName = matcher.group(1);
			stringKey = matcher.group(2);
			index = Integer.parseInt(matcher.group(3));
		}
		else {
			matcher = indexedAttr.matcher(attrName);
			if (matcher.matches()) {
				attrName = matcher.group(1);
				index = Integer.parseInt(matcher.group(2));
			}
			else {
				Matcher stringMatcher = stringIndexedAttr.matcher(attrName);
				if (stringMatcher.matches()) {
					attrName = stringMatcher.group(1);
					stringKey = stringMatcher.group(2);
				}
			}
		}

		final EStructuralFeature feature = object.eClass()
				.getEStructuralFeature(attrName);

		if (feature == null) {
			return null;
		}

		Object value = object.eGet(feature);

		if (value instanceof EMap<?, ?> && stringKey != null && index != null) {
			EMap<?, ?> map = (EMap<?, ?>) value;
			if (!map.containsKey(stringKey)) {
				return null;
			}
			value = map.get(stringKey);
			if (value instanceof EList) {
				EList<?> list = (EList<?>) value;
				if (index >= list.size()) {
					return null;
				}
				value = list.get(index);
			}
			else {
				return null;
			}
		}
		else if (value instanceof EMap<?, ?> && stringKey != null) {
			EMap<?, ?> map = (EMap<?, ?>) value;
			if (!map.containsKey(stringKey)) {
				return null;
			}
			value = map.get(stringKey);
		}
		else if (value instanceof EList<?> && index != null) {
			EList<?> list = (EList<?>) value;
			if (index >= list.size()) {
				return null;
			}
			value = list.get(index);
		}

		return value == null ? "" : value;
	}

	public Response doImageAssert(Assert assertCmd, IWidgetDescriber control) {
		List<IImageAssertSupport> processors = client
				.getProcessors(IImageAssertSupport.class);
		if (!processors.isEmpty()) {
			AssertImageData data = assertCmd.getImageData();
			switch (assertCmd.getKind()) {
			case IMAGE_CONTAINS_TEXT:
				Image image = control.captureImage();
				try {
					Rectangle bounds = image.getBounds();
					Rectangle subRect = new Rectangle(0, 0, 0, 0);
					double mult_x = 1.0f * bounds.width / data.getWidth();
					double mult_y = 1.0f * bounds.width / data.getWidth();
					subRect.x = (int) (data.getX() * mult_x);
					subRect.width = (int) (data.getSx() * mult_x);
					subRect.y = (int) (data.getY() * mult_y);
					subRect.height = (int) (data.getSy() * mult_y);
					Image sub = SWTUIPlayer.copyImagePart(image, subRect);
					try {
						for (IImageAssertSupport support : processors) {
							String text = support.recognize(sub);
							text = replaceMultilines(text);
							String expectedText = assertCmd.getValue();
							if (text != null
									&& (text.contains(expectedText) || text
											.matches(expectedText))) {
								return assertResponse(ResponseStatus.OK, "OK");
							}
							else {
								return assertResponse(
										ResponseStatus.FAILED,
										NLS.bind(
												TeslaSWTMessages.SWTUIProcessor_RecognizedTextNotContainsValue,
												text, assertCmd.getValue()));
							}
						}
					}
					finally {
						sub.dispose();
					}
				}
				catch (Throwable e) {
				}
				finally {
					image.dispose();
				}
				break;
			case CONTAINS_IMAGE:
				for (IImageAssertSupport support : processors) {
					if (support
							.containsImage(control, assertCmd.getImageData())) {
						return assertResponse(ResponseStatus.OK, "OK");
					}
				}
				return assertResponse(ResponseStatus.FAILED,
						TeslaSWTMessages.SWTUIProcessor_ControlNotContainsImage);
			}
		}
		return assertResponse(ResponseStatus.FAILED,
				TeslaSWTMessages.SWTUIProcessor_ImlNotSupported);
	}

	public Response performAssert(final Assert assertCmd, String attrValue,
			final Object value, String strValue) {
		// Support of indexed values
		if (value instanceof List<?> && assertCmd.getIndex() != null) {
			final List<?> valueList = (List<?>) value;
			int index = assertCmd.getIndex().intValue();
			if (valueList.size() <= index) {
				final AssertResponse response = assertResponse(
						ResponseStatus.FAILED,
						NLS.bind(
								valueList.size() > 0 ? TeslaSWTMessages.SWTUIProcessor_AssertIndexOutOfBounds
										: TeslaSWTMessages.SWTUIProcessor_AssertIndexOutOfBoundsListEmpty,
								index, valueList.size() - 1));
				return response;
			}
			Object item = valueList.get(index);
			strValue = item.toString();
			if (item instanceof EObject) {
				strValue = SimpleCommandPrinter.toString((EObject) item, true)
						.trim();
			}
		}
		// Convert both values to unified \n format.
		if (!assertCmd.getKind().equals(AssertKind.REGEXP)) {
			if (attrValue != null) {
				attrValue = unifyMultilines(attrValue);
			}
			if (strValue != null) {
				strValue = unifyMultilines(strValue);
			}
		}

		boolean result = false;
		String message = null;
		switch (assertCmd.getKind()) {
		case ASSERT_FALSE:
			result = Boolean.FALSE.toString().equalsIgnoreCase(strValue);
			if (!result) {
				message = NLS.bind(
						TeslaSWTMessages.SWTUIProcessor_AssertFalseFailed,
						assertCmd.getAttribute());
			}
			break;
		case ASSERT_TRUE:
			result = Boolean.TRUE.toString().equalsIgnoreCase(strValue);
			if (!result) {
				message = NLS.bind(
						TeslaSWTMessages.SWTUIProcessor_AssertTrueFailed,
						assertCmd.getAttribute());
			}
			break;
		case EQUALS:
			result = attrValue.equals(strValue)
					|| safeMatches(strValue, attrValue);
			if (!result) {
				String expected = attrValue;
				String actual = strValue;
				message = NLS.bind(
						TeslaSWTMessages.SWTUIProcessor_AssertEqualsFailed,
						assertCmd.getAttribute(),
						StringUtils.junitStyleDiff(actual, expected));
			}
			break;
		case NOT_EQUALS:
			result = !attrValue.equals(strValue)
					&& !safeMatches(strValue, attrValue);
			if (!result) {
				message = NLS.bind(
						TeslaSWTMessages.SWTUIProcessor_AssertNotEqualsFailed,
						assertCmd.getAttribute(), attrValue);
			}
			break;
		case IS_EMPTY:
			result = value == null || strValue.length() == 0;
			if (!result) {
				message = NLS.bind(
						TeslaSWTMessages.SWTUIProcessor_AssertEmptyFailed,
						assertCmd.getAttribute(), strValue);
			}
			break;
		case NOT_EMPTY:
			result = value != null && strValue.length() > 0;
			if (!result) {
				message = NLS.bind(
						TeslaSWTMessages.SWTUIProcessor_AssertNotEmptyFailed,
						assertCmd.getAttribute());
			}
			break;
		case CONTAINS:
			result = strValue != null && strValue.contains(attrValue);
			if (!result) {
				message = NLS.bind(
						TeslaSWTMessages.SWTUIProcessor_AssertContainsFailed,
						assertCmd.getAttribute(), strValue + " !~ " + attrValue);
			}
			break;
		case NOT_CONTAINS:
			result = strValue == null || !strValue.contains(attrValue);
			if (!result) {
				message = NLS
						.bind(TeslaSWTMessages.SWTUIProcessor_AssertNotContainsFailed,
								assertCmd.getAttribute(), attrValue);
			}
			break;
		case REGEXP:
			message = validateRegex(attrValue);
			if (message != null) {
				result = false;
				break;
			}
			result = strValue != null && strValue.matches(attrValue);
			if (!result) {
				message = NLS.bind(
						TeslaSWTMessages.SWTUIProcessor_AssertMatchRegexFailed,
						assertCmd.getAttribute(), attrValue + " != " + strValue);
			}
			break;
		case NOT_REGEXP:
			message = validateRegex(attrValue);
			if (message != null) {
				result = false;
				break;
			}
			result = strValue == null || !strValue.matches(attrValue);
			if (!result) {
				message = NLS
						.bind(TeslaSWTMessages.SWTUIProcessor_AssertNotMatchRegexFailed,
								assertCmd.getAttribute(), attrValue);
			}
			break;
		}
		if (message == null) {
			message = NLS.bind(
					result ? TeslaSWTMessages.SWTUIProcessor_AssertSucceed
							: TeslaSWTMessages.SWTUIProcessor_AssertFailed,
					assertCmd.getAttribute());
		}
		final AssertResponse response = assertResponse(
				result ? ResponseStatus.OK : ResponseStatus.FAILED, message);
		return response;
	}

	private static AssertResponse assertResponse(final ResponseStatus status,
			final String msg) {
		final AssertResponse response = ProtocolFactory.eINSTANCE
				.createAssertResponse();
		response.setStatus(status);
		response.setMessage(msg);
		return response;
	}

	private Response handleChildren(final Children command) {
		// final Element element = command.getElement();
		return factory.createBooleanResponse();
	}

	private Response handleDrag(final DragCommand command) {
		return dragSupport.handleDrag(command);
	}

	@Override
	public String getFeatureID() {
		return "org.eclipse.rcptt.tesla.swt.swt";
	}

	@Override
	public boolean isSelectorSupported(final String kind) {
		for (final ElementKind e : allSelectors) {
			if (e.name().equals(kind)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCommandSupported(final Command cmd) {
		final EClass cl0 = cmd.eClass();
		for (final EClass cl : supportedCommands) {
			if (cl0.equals(cl)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public SelectResponse select(final SelectCommand command,
			final ElementGenerator generator,
			final IElementProcessorMapper mapper) {
		if (failNextCommandBecauseOf != null) {
			SelectResponse response = ProtocolFactory.eINSTANCE
					.createSelectResponse();
			response.setMessage(failNextCommandBecauseOf);
			response.setStatus(ResponseStatus.FAILED);
			failNextCommandBecauseOf = null;
			return response;
		}
		getPlayer().getCollector().enable();
		final SelectData data = command.getData();
		// TODO extract selection filter creation from SelectData
		SWTUIElement parent = getMapper().get(data.getParent());
		final SWTUIElement after = getMapper().get(data.getAfter());
		final EList<String> path = data.getPath();
		String[] pathArray = null;
		if (path.size() > 0) {
			pathArray = path.toArray(new String[path.size()]);
		}
		if (parent == null && after != null) {
			parent = getPlayer().getParentElement(after);
		}
		EList<Integer> indexes = data.getIndexes();
		final SWTUIElement result = getPlayer().select(
				new PlayerSelectionFilter(parent, GenericElementKind
						.fromString(data.getKind()), data.getPattern(),
						pathArray, data.getIndex(), after,
						indexes.toArray(new Integer[data.getIndexes().size()]),
						data.getClassPattern()));

		PerspectiveUIElement perspective = null;
		if (result == null && parent != null && parent.isWorkbenchWindow()
				&& data.getKind().equals(ElementKind.Button.toString())
				&& data.getPattern() != null
				&& data.getPattern().endsWith(" perspective")) {
			String perspectiveName = data.getPattern().substring(0,
					data.getPattern().lastIndexOf(" perspective"));
			perspective = new PerspectiveUIElement(perspectiveName);
		}

		SelectResponse response = ProtocolFactory.eINSTANCE
				.createSelectResponse();

		if (result != null) {
			response.getElements().add(getMapper().get(result));
		}
		else if (perspective != null && perspective.isPerspeciveFind()) {
			Element element = RawFactory.eINSTANCE.createElement();
			element.setKind(perspective.getGenerationKind());
			element.setId(perspective.getPerspectiveId());
			response.getElements().add(element);
		}
		else {
			StringBuilder qualifier = new StringBuilder();
			if (data.getPattern() != null) {
				qualifier.append(data.getPattern());
			} else if (!data.getPath().isEmpty()) {
				if (qualifier.length() > 0) {
					qualifier.append(", ");
				}
				qualifier.append("path: ").append(data.getPath());
			} else if (!data.getIndexes().isEmpty()) {
				if (qualifier.length() > 0) {
					qualifier.append(", ");
				}
				qualifier.append("indexes: ").append(data.getPath());
			} else if (data.getIndex() != null) {
				if (qualifier.length() > 0) {
					qualifier.append(", ");
				}
				qualifier.append("index: ").append(data.getIndex());
			}
			
			response.setMessage(NLS.bind(
					TeslaSWTMessages.SWTUIProcessor_CannotFindControl,
					data.getKind(),
					qualifier));
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleSetText(final SetText command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();

		if (element != null) {
			if (element.isDisabled()) {
				response.setStatus(ResponseStatus.FAILED);
				response.setMessage(TeslaSWTMessages.SWTUIProcessor_CannotSetText_DisabledControl);
			}
			else {
				Widget widget = unwrapWidget(element);
				String value = command.getValue();
				if (widget instanceof Slider) {
					try {
						Integer.parseInt(value);
					}
					catch (NumberFormatException e) {
						response.setStatus(ResponseStatus.FAILED);
						response.setMessage(NLS
								.bind(TeslaSWTMessages.SWTUIProcessor_CannotSetSliderValue_IncorrectFormat,
										value));
						return response;
					}
				}
				if (widget instanceof Spinner) {
					try {
						Double.parseDouble(value);
					}
					catch (NumberFormatException e) {
						response.setStatus(ResponseStatus.FAILED);
						response.setMessage(NLS
								.bind(TeslaSWTMessages.SWTUIProcessor_CannotSetSpinnerValue_IncorrectFormat,
										value));
						return response;
					}
				}
				if (widget instanceof Combo) {
					Combo combo = (Combo) widget;
					if ((combo.getStyle() & SWT.READ_ONLY) != 0
							|| command.isSelect()) {

						// force lazy combo to publish its items
						getPlayer().getEvents().sendFocus(widget);

						if (SWTUIPlayer.findComboItem(combo.getItems(), value,
								command.isSelect()) == SWTUIPlayer.COMBO_ITEM_NOT_FOUND) {
							response.setStatus(ResponseStatus.FAILED);
							response.setMessage(getSetComboValueErrorMessage(
									value, combo.getItems()));
							return response;
						}
					}
				}
				if (widget instanceof CCombo) {
					CCombo combo = (CCombo) widget;
					if ((combo.getStyle() & SWT.READ_ONLY) != 0
							|| command.isSelect()) {

						// force lazy ccombo to publish its items
						getPlayer().getEvents().sendFocus(widget);

						if (SWTUIPlayer.findComboItem(combo.getItems(), value,
								command.isSelect()) == SWTUIPlayer.COMBO_ITEM_NOT_FOUND) {
							response.setStatus(ResponseStatus.FAILED);
							response.setMessage(getSetComboValueErrorMessage(
									value, combo.getItems()));
							return response;
						}
					}
				}
				if (element.getKind().is(ElementKind.DateTime)) {
					int spPos = value.indexOf(" ");
					if (spPos != -1) {
						String[] date = value.substring(0, spPos).split("/");
						String[] time = value.substring(spPos + 1).split(":");

						try {
							if (date.length == 3 && time.length == 3) {
								int dateValue[] = new int[3];
								int timeValue[] = new int[3];

								for (int i = 0; i < 3; i++) {
									dateValue[i] = Integer.parseInt(date[i]
											.trim());
									timeValue[i] = Integer.parseInt(time[i]
											.trim());
								}
								getPlayer().setDateTime(element, dateValue[0],
										dateValue[1], dateValue[2],
										timeValue[0], timeValue[1],
										timeValue[2]);
								return response;
							}
						}
						catch (NumberFormatException e) {
							// Exception will be on next line
						}
					}
					response.setStatus(ResponseStatus.FAILED);
					response.setMessage(NLS
							.bind(TeslaSWTMessages.SWTUIProcessor_CannotSetDataTimeValue_IncorrectFormat,
									value));
					return response;
				}
				getPlayer().setText(element, value, command.isSelect());
			}
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	private static final String NEWLINE = String.format("%n");

	private String getSetComboValueErrorMessage(String value, String[] items) {

		StringBuilder values = new StringBuilder();
		for (String item : items) {
			values.append(encodeValue(item)).append(NEWLINE);
		}
		String message = String.format("Can't set value %s into combo. "
				+ "Value not found.%nAvailable values:%n%s",
				encodeValue(value), values);
		return message;
	}

	private static String encodeValue(String value) {
		if (value == null)
			return "'null'";
		StringBuilder result = new StringBuilder();
		result.append('\'');
		for (char ch : value.toCharArray()) {
			if (Character.isISOControl(ch)) {
				result.append(String.format("\\%d", (int) ch));
			}
			else {
				result.append(ch);
			}
		}
		result.append('\'');
		return result.toString();
	}

	protected Response handleClick(final Click command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			if (!canClick(element)) {
				if (!canClickView(element)) {
					response.setMessage(NLS
							.bind(TeslaSWTMessages.SWTUIProcessor_CannotClick_PerspectiveNotContainsView,
									element.getText()));
				}
				else {
					response.setMessage(NLS
							.bind(TeslaSWTMessages.SWTUIProcessor_CannotClick_ControlDisabledDisposedOrInvisible,
									element.getText()));
				}
				response.setStatus(ResponseStatus.FAILED);
			}
			else {
				getPlayer().click(element, command.isDefault(), false, command.isArrow(), command.getMetaKeys());
			}
		}
		else if (ElementKind.Perspective.toString().equals(
				command.getElement().getKind())) {
			getPlayer().setPerspective(command.getElement().getId());
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleCheck(final Check command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			if (!canClick(element)) {
				response.setMessage(NLS
						.bind(TeslaSWTMessages.SWTUIProcessor_CannotClick_ControlDisabledDisposedOrInvisible,
								element.getText()));
				response.setStatus(ResponseStatus.FAILED);
			}
			else {
				getPlayer().check(element, command.isState());
			}
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleDoubleClick(final DoubleClick command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			if (!canClick(element)) {
				response.setMessage(NLS
						.bind(TeslaSWTMessages.SWTUIProcessor_CannotDoubleClick_ControlDisabledDisposedOrInvisible,
								element.getText()));
				response.setStatus(ResponseStatus.FAILED);
			}
			else {
				getPlayer().click(element, false, true, false, Events.EMPTY_MASK);
			}
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleSendClose(final Close command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			if (element.widget instanceof StyledText) {
				return failResponse("Trying to close "
						+ element.widget.getClass().getName()
						+ ". StyledText should not be closed.");
			}
			getPlayer().close(element);
			getMapper().remove(command.getElement());
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleSetSelection(final SetSelection command) {
		getPlayer().getCollector().enable();
		final SWTUIElement element = getMapper().get(command.getElement());
		final BooleanResponse response = factory.createBooleanResponse();
		if (element != null) {
			// if (cellEditorActivations.containsKey(element.widget)) {
			// // Ignore set selection bug.
			// return response;
			// }
			final String[] aPath = asStringArray(command.getPath());
			final EList<MultiSelectionItem> items = command
					.getAdditionalItems();
			if (items.size() > 0) {
				if (!element.isSupportMultipleSelections()) {
					response.setResult(false);
					response.setStatus(ResponseStatus.FAILED);
					response.setMessage("Widget does not allow multiple selection.");
					return response;
				}

				final List<String[]> sels = new ArrayList<>();
				sels.add(asStringArray(command.getPath()));
				for (final MultiSelectionItem item : items) {
					sels.add(asStringArray(item.getPath()));
				}
				final boolean setSelection = element.setSelection(sels,
						command.isAll());

				response.setResult(setSelection);
				if (!setSelection) {
					response.setStatus(ResponseStatus.FAILED);
					updateSelectionFailedMessage(command, response);
				}
			}
			else {
				final boolean setSelection = element.setSelection(aPath,
						command.getPattern(), command.getIndex(),
						command.isAll());

				response.setResult(setSelection);
				if (!setSelection) {
					updateSelectionFailedMessage(command, response);
					response.setStatus(ResponseStatus.FAILED);
				}
			}
		}
		else {
			updateSelectionFailedMessage(command, response);
			response.setStatus(ResponseStatus.FAILED);
			response.setResult(false);
		}
		return response;
	}

	private void updateSelectionFailedMessage(final SetSelection command,
			final BooleanResponse response) {
		if (command.getPattern() != null) {
			response.setMessage(NLS.bind(
					TeslaSWTMessages.SWTUIProcessor_CannotSetSelection,
					command.getPattern()));
		}
		else if (!command.getPath().isEmpty()
				|| !command.getAdditionalItems().isEmpty()) {

			List<String[]> items = new ArrayList<>();
			String[] path = asStringArray(command.getPath());
			if (path != null)
				items.add(path);
			for (MultiSelectionItem i : command.getAdditionalItems()) {
				path = asStringArray(i.getPath());
				if (path != null)
					items.add(path);
			}

			response.setMessage(NLS.bind(
					TeslaSWTMessages.SWTUIProcessor_CannotSetSelection,
					Arrays.deepToString(items.toArray())));
		}
	}

	private String[] asStringArray(final List<String> path) {
		String aPath[] = null;
		if (!path.isEmpty()) {
			aPath = path.toArray(new String[path.size()]);
		}
		return aPath;
	}

	protected Response handleCheckItem(final CheckItem command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final BooleanResponse response = factory.createBooleanResponse();
		if (element != null) {
			final EList<String> path = command.getPath();
			String aPath[] = null;
			if (!path.isEmpty()) {
				aPath = path.toArray(new String[path.size()]);
			}
			final boolean checkItem = Viewers.checkItem(command.isState(),
					element, aPath, command.getPattern(), command.getIndex());
			response.setResult(checkItem);
			if (!checkItem) {
				response.setStatus(ResponseStatus.FAILED);
			}
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
			response.setResult(false);
		}
		return response;
	}

	protected Response handleIsDisposed(final IsDisposed command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final BooleanResponse response = factory.createBooleanResponse();
		if (element != null) {
			response.setResult(getPlayer().isDisposed(element));
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
			response.setResult(true);
		}
		return response;
	}

	protected Response handleIsEnabled(final IsEnabled command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final BooleanResponse response = factory.createBooleanResponse();
		if (element != null) {
			response.setResult(!isDisabled(element));
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
			response.setResult(false);
		}
		return response;
	}

	protected Response handleCountItems(final CountItems command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final IntResponse response = factory.createIntResponse();
		if (element != null) {
			final EList<String> path = command.getPath();
			String aPath[] = null;
			if (!path.isEmpty()) {
				aPath = path.toArray(new String[path.size()]);
			}
			response.setResult(getPlayer().countItems(element, aPath));
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
			response.setResult(0);
		}
		return response;
	}

	protected Response handleGetText(final GetText command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final GetTextResponse response = factory.createGetTextResponse();
		if (element != null) {
			response.setText(PlayerTextUtils.getText(element));
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleTypeText(final TypeText command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			Integer state = command.getState();
			if (state < 0)
				state = 0;
			if (command.isFromDisplay()) {
				// when send type events from display
				// if first time send, after eclipse started
				// events are ignored, send fake SHIFT for this cases
				getPlayer().type(element, 0, SWT.SHIFT, true, '\u0000', 1);
			}
			getPlayer().typeText(element, command.getText(), state,
					command.isFromDisplay());
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleClickPreferencesMenu(ClickPreferencesMenu command) {
		runCommand(ActionFactory.PREFERENCES);
		return RawFactory.eINSTANCE.createResponse();
	}

	protected Response handleClickAboutMenu(ClickAboutMenu command) {
		runCommand(ActionFactory.ABOUT);
		return RawFactory.eINSTANCE.createResponse();
	}

	private void runCommand(final ActionFactory action) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IHandlerService service = (IHandlerService) workbench
						.getService(IHandlerService.class);
				try {
					service.executeCommand(action.getCommandId(), null);
				}
				catch (Exception e) {
					SWTTeslaActivator.log(e);
				}
			}
		});
	}

	protected Response handleType(final Type command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			Integer state = command.getState();
			if (state < 0)
				state = 0;
			if (!command.isTraverse()) {
				if (command.isFromDisplay()) {
					// when send type events from display
					// if first time send, after eclipse started
					// events are ignored, send fake SHIFT for this cases
					getPlayer().type(element, 0, SWT.SHIFT, true, '\u0000', 1);
				}
				getPlayer().type(element, command.getCode(), state,
						command.isFromDisplay(), command.getCharacter(),
						command.getTimes());
			}
			else {
				getPlayer().traverse(element, command.getCode(),
						command.getCharacter(), command.getTimes());
			}
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleTypeAction(final TypeAction command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			getPlayer().typeAction(element, command.getActionId());
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	protected Response handleShow(final Show command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element != null) {
			if (element.widget instanceof MenuItem) {
				// List<Menu> menuList = new ArrayList<Menu>();
				// Menu menu = ((MenuItem) element.widget).getMenu();
				// if (menu == null)
				// menu = ((MenuItem) element.widget).getParent();
				// while (menu != null) {
				// menu.setVisible(true);
				// try {
				// Thread.sleep(2000);
				// } catch (InterruptedException e) {
				// }
				// menu = menu.getParentMenu();
				// }
			}
			// getPlayer().show(element);
		}
		else {
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	public synchronized SWTUIPlayer getPlayer() {
		if (internalPlayer == null) {
			internalPlayer = SWTUIPlayer.getPlayer(PlatformUI.getWorkbench()
					.getDisplay());
		}
		return internalPlayer;
	}

	@Override
	public void postSelect(final Element element,
			final IElementProcessorMapper mapper) {
	}

	@Override
	public boolean isInactivityRequired() {
		return true;
	}

	@Override
	public boolean canProceed(final Context context, Q7WaitInfoRoot info) {
		if (client == null) {
			return false;
		}
		List<ISWTWaitPolicy> processors = client
				.getProcessors(ISWTWaitPolicy.class);
		for (ISWTWaitPolicy iswtWaitPolicy : processors) {
			if (!iswtWaitPolicy.isReadyToProceed(context, info)) {
				return false;
			}
		}
		return getPlayer().canProceed(context, info);
	}

	@Override
	public void clean() {
		getMapper().clear();
		SWTModelMapper.initializeExtensions(client
				.getProcessors(ISWTModelMapperExtension.class));
		getPlayer().clean();
		getCellEditorSupport().clear();
		TeslaCellEditorManager.getInstance().clean();
		SWTDialogManager.clear();
		JobsManager.getInstance().clean();
		TeslaEventManager.getManager().setNoWaitForJobs(false);
		failNextCommandBecauseOf = null;
		TeslaTimerExecManager.getManager().clearTimers();
	}

	private synchronized CellEditorSupport getCellEditorSupport() {
		if (cellEditSupport == null) {
			cellEditSupport = createCellEditorSupport();
		}
		return cellEditSupport;
	}

	@Override
	public void terminate() {
		SWTModelMapper.initializeExtensions(null);
		SWTElementMapper.remove(getId());
		if (internalPlayer != null) {
			SWTUIPlayer.shutdown(internalPlayer);
		}
		internalPlayer = null;
		dragSupport = null;
		getCellEditorSupport().clear();
		client = null;

	}

	public boolean callMasterProcess(final Context currentContext) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				client.processNext(currentContext, null);
			}
		});
		return true;
	}

	public DragSupport getDragSupport() {
		return dragSupport;
	}

	@Override
	public void checkHang() {
		// if (!getClass().equals(SWTUIProcessor.class)) {
		// return;
		// }
		// SWTUIPlayer player = getPlayer();
		// UIJobCollector collector = player.getCollector();
		// List<Job> jobs = collector.getJobs();
		// for (Job job : jobs) {
		// if (job.getState() == Job.RUNNING) {
		// if (times.contains(job)) {
		// // 60 seconds. Lets remove job from waiting.
		// TeslaCore.log("Job Lock Detected. Job class:"
		// + job.getClass().getName());
		// collector.removeJob(job);
		// } else {
		// times.add(job);
		// }
		// }
		// }
	}

	@Override
	public void collectInformation(AdvancedInformation information,
			Command lastCommand) {
		Node root = InfoUtils.newNode("swt.info").add(information);
		Element element = null;
		if (lastCommand instanceof ElementCommand) {
			element = ((ElementCommand) lastCommand).getElement();
		}
		else if (lastCommand instanceof Assert) {
			element = ((Assert) lastCommand).getElement();
		}
		else if (lastCommand instanceof SelectCommand) {
			element = ((SelectCommand) lastCommand).getData().getParent();
		}
		// Required element hierarchy
		try {
			SWTUIElement uiElement = null;
			if (element != null) {
				uiElement = getMapper().get(element);
			}
			if (uiElement != null) {
				processChildren(uiElement, root, new HashSet<SWTUIElement>());
			}
		} catch (ClosedException e) {
			// Can't collect information on disposed elements
		}
		Node root2 = root.child("eclipse.windows");

		// Use all eclipse windows as roots
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (IWorkbenchWindow win : windows) {
			Set<SWTUIElement> processed = new HashSet<>();
			processChildren(win, root2, processed);
		}

		Node player = InfoUtils.newNode("swt.player").add(information);
		SWTUIPlayer swtuiPlayer = getPlayer();
		UIJobCollector collector = swtuiPlayer.getCollector();
		List<Job> jobs = collector.getJobs();
		if (jobs.size() > 0) {
			Node jobsNode = player.child("ui.job.collector.jobs");
			for (Job job : jobs) {
				Node child = jobsNode.child("job:" + job.getName());
				child.property("job.class", job.getClass().getName());
			}
		}
	}

	private void processChildren(IWorkbenchWindow win, Node root2,
			Set<SWTUIElement> processed) {
		IWorkbenchPage[] pages = win.getPages();
		processed.add(getPlayer().wrap(win.getShell()));
		for (IWorkbenchPage page : pages) {
			Node pageNode = root2.child("page:" + page.getLabel());
			IEditorReference[] references = page.getEditorReferences();
			if (references.length > 0) {
				Node editorsNode = pageNode.child("editors");
				for (IEditorReference ref : references) {
					processEditor(editorsNode, ref, processed);
				}
			}

			IViewReference[] viewReferences = page.getViewReferences();
			if (viewReferences.length > 0) {
				Node viewsNode = pageNode.child("views");
				for (IViewReference ref : viewReferences) {
					processView(viewsNode, ref, processed);
				}
			}
		}
		Shell[] shells = win.getShell().getShells();
		for (Shell shell : shells) {
			Node shellNode = root2.child("shells");
			processChildren(getPlayer().wrap(shell), shellNode, processed);
		}
	}

	private void processEditor(Node editorsNode, IEditorReference ref,
			Set<SWTUIElement> processed) {
		Node child = editorsNode.child("editor:" + ref.getName());
		child.property("id", ref.getId());
		child.property("dirty", ref.isDirty());
		child.property("pinned", ref.isPinned());
		try {
			IEditorInput editorInput = ref.getEditorInput();
			if (editorInput != null) {
				child.property("editorInput.name", editorInput.getName());
			}
		}
		catch (Throwable t) {
			// Ignore
		}
		processChildren(getPlayer().wrap(ref), child, processed);
	}

	private void processView(Node editorsNode, IViewReference ref,
			Set<SWTUIElement> processed) {
		String refName = "";
		try {
			refName = ref.getPartName();
		}
		catch (Exception e) {
			// seems disposed
			return;
		}
		Node child = editorsNode.child("view:" + refName);
		child.property("id", ref.getId());
		child.property("dirty", isViewDirty(ref));
		child.property("fastView", ref.isFastView());
		processChildren(getPlayer().wrap(ref), child, processed);
	}

	/**
	 * Added because of QS-2489: CommonNavigator#isDirty() was throwing a NPE
	 *
	 * @return boolean or error message
	 */
	private static Object isViewDirty(IViewReference ref) {
		try {
			return ref.isDirty();
		}
		catch (NullPointerException e) {
			return e.toString();
		}
	}

	private void processChildren(SWTUIElement uiElement, Node root,
			Set<SWTUIElement> processed) {
		if (processed.contains(uiElement)) {
			return;
		}
		processed.add(uiElement);
		if (uiElement.isDisposed()) {
			return;
		}
		String text = uiElement.getText();
		Node nde = root;
		if (!uiElement.getKind().is(ElementKind.Unknown)) {
			nde = root.child(uiElement.getKind().name() + "("
					+ (text != null ? text.trim() : "") + ")");
			// Adds decorators
			for (ControlDecoration decorator : uiElement.getDecorators()) {
				if (decorator.isVisible())
					nde.child("ControlDecoration(" + decorator.getDescriptionText() + ")");
			}
		}
		try {
			SWTUIElement[] children = getPlayer().children.collectFor(
					uiElement, null, false, null, null);
			for (SWTUIElement swtuiElement : children) {
				if (!swtuiElement.isDisposed()) {
					processChildren(swtuiElement, nde, processed);
				}
			}
		}
		catch (Throwable e) {
			nde.property("ERROR", e.getMessage());
		}
	}

	// Text commands
	protected Response handleShowSelection(ShowSelection command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		getPlayer().exec("show selection", new Runnable() {

			@Override
			public void run() {
				Widget rawWidget = element.widget;
				if (rawWidget instanceof Text) {
					((Text) rawWidget).showSelection();

				}

				if (rawWidget instanceof StyledText) {
					((StyledText) rawWidget).showSelection();
				}

			}
		});
		return response;
	}

	protected Response handleSetTextSelection(final SetTextSelection command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}

		getPlayer().exec("set text selection", new Runnable() {

			@Override
			public void run() {

				Widget rawWidget = element.widget;
				int start = command.getOffset() != null ? command.getOffset()
						.intValue() : 0;
				int end = start
						+ (command.getLength() != null ? command.getLength()
								.intValue() : 0);

				if (command.getEndline() != null
						&& command.getEndoffset() != null
						&& command.getStartLine() != null) {

					int startLine = command.getStartLine();
					int startOffset = command.getOffset();
					int endLine = command.getEndline();
					int endOffset = command.getEndoffset();

					if (rawWidget instanceof StyledText) {
						StyledText text = (StyledText) rawWidget;

						start = startOffset;
						start += text.getOffsetAtLine(startLine);

						end = endOffset;
						end += text.getOffsetAtLine(endLine);
					}
				}

				if (start == end) {
					// This is set caret position command
					if (rawWidget instanceof Text) {
						Text text = (Text) rawWidget;
						text.forceFocus();
						text.setSelection(start, end);
					}

					if (rawWidget instanceof StyledText) {
						StyledText text = (StyledText) rawWidget;
						text.forceFocus();
						text.setCaretOffset(end);
						Event event = new Event();
						event.x = start;
						event.y = end;
						event.type = SWT.Selection;
						getPlayer().getEvents().sendEvent(text, event);
					}
					return;
				}

				if (rawWidget instanceof Text) {
					Text text = (Text) rawWidget;
					text.forceFocus();
					text.setSelection(start, end);
				}

				if (rawWidget instanceof StyledText) {
					StyledText text = (StyledText) rawWidget;
					// text.setCaretOffset(end);
					text.forceFocus();
					text.setSelection(start, end);
					if (text.getAccessible() != null) {
						text.getAccessible().textSelectionChanged();
					}
					Event event = new Event();
					event.x = start;
					event.y = end;
					event.type = SWT.Selection;
					getPlayer().getEvents().sendEvent(text, event);
				}
			}
		});

		return response;
	}

	protected Response handleGetTextSelection(GetTextSelection command) {
		SWTUIElement element = getMapper().get(command.getElement());
		TextSelectionResponse response = factory.createTextSelectionResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		Widget rawWidget = element.widget;
		Point selection = new Point(0, 0);
		String selectionText = "";
		if (rawWidget instanceof Text) {
			Text text = (Text) rawWidget;
			selection = text.getSelection();
			selectionText = text.getSelectionText();
		}

		if (rawWidget instanceof StyledText) {
			StyledText styledText = (StyledText) rawWidget;
			selection = styledText.getSelection();
			selectionText = styledText.getSelectionText();
		}
		response.setX(selection.x);
		response.setY(selection.y);
		response.setText(selectionText);
		return response;
	}

	protected Response handleGoToTextLine(final GoToTextLine command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}

		getPlayer().exec("goto line", new Runnable() {

			@Override
			public void run() {
				Widget rawWidget = element.widget;
				int line = command.getLine();
				if (rawWidget instanceof Text) {
					Text text = (Text) rawWidget;

					String lineSeparator = text.getLineDelimiter();
					String[] content = text.getText().split(lineSeparator);
					int offsetAtLine = -1;
					for (int i = 0; i < content.length && i < line; i++)
						offsetAtLine += content[i].length()
								+ lineSeparator.length();

					if (offsetAtLine >= 0) {
						text.setSelection(offsetAtLine, offsetAtLine);
						text.showSelection();
					}

				}

				if (rawWidget instanceof StyledText) {
					StyledText text = (StyledText) rawWidget;
					if (line < text.getLineCount()) {
						int offsetAtLine = text.getOffsetAtLine(line);
						text.setSelection(offsetAtLine, offsetAtLine);
						text.showSelection();
					}
				}
			}
		});
		return response;
	}

	protected Response handleGetTextLineOffset(final GetTextLineOffset command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final IntResponse response = factory.createIntResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}

		Widget rawWidget = element.widget;
		int line = command.getLine();
		if (rawWidget instanceof Text) {
			Text text = (Text) rawWidget;

			String lineSeparator = text.getLineDelimiter();
			String[] content = text.getText().split(lineSeparator);
			int offsetAtLine = -1;
			for (int i = 0; i < content.length && i < line; i++)
				offsetAtLine += content[i].length() + lineSeparator.length();
			response.setResult(offsetAtLine);

		}

		if (rawWidget instanceof StyledText) {
			StyledText text = (StyledText) rawWidget;
			if (line < text.getLineCount())
				response.setResult(text.getOffsetAtLine(line));
		}
		return response;
	}

	protected Response handleGetTextLineLength(final GetTextLineLength command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final IntResponse response = factory.createIntResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		response.setResult(-1);

		Widget rawWidget = element.widget;
		int line = command.getLine();
		if (rawWidget instanceof Text) {
			Text text = (Text) rawWidget;

			String lineSeparator = text.getLineDelimiter();
			String[] content = text.getText().split(lineSeparator);
			if (line < content.length)
				response.setResult(content[line].length());

		}

		if (rawWidget instanceof StyledText) {
			StyledText text = (StyledText) rawWidget;
			if (line < text.getLineCount())
				response.setResult(text.getLine(line).length());
		}
		return response;
	}

	protected Response handleSelectTextLine(final SelectTextLine command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}

		getPlayer().exec("selectLine", new Runnable() {

			@Override
			public void run() {
				Widget rawWidget = element.widget;
				int line = command.getLine();
				if (rawWidget instanceof Text) {
					Text text = (Text) rawWidget;

					String lineSeparator = text.getLineDelimiter();
					String[] content = text.getText().split(lineSeparator);
					int offsetAtLine = -1;
					int lineLength = 0;
					if (line >= content.length)
						return;
					for (int i = 0; i < content.length && i < line; i++) {
						lineLength = content[i].length()
								+ lineSeparator.length();
						offsetAtLine += lineLength;
					}
					text.setSelection(offsetAtLine, offsetAtLine + lineLength);
					text.showSelection();

				}

				if (rawWidget instanceof StyledText) {
					StyledText text = (StyledText) rawWidget;

					if (line < text.getLineCount()) {
						int offsetAtLine = text.getOffsetAtLine(line);
						text.setSelection(offsetAtLine, offsetAtLine
								+ text.getLine(line).length());
						text.showSelection();
					}
				}
			}
		});
		return response;
	}

	protected Response handleGetTextLine(final GetTextLine command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final GetTextResponse response = factory.createGetTextResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		Widget rawWidget = element.widget;
		int line = command.getLine();
		if (rawWidget instanceof Text) {
			Text text = (Text) rawWidget;

			String lineSeparator = text.getLineDelimiter();
			String[] content = text.getText().split(lineSeparator);
			if (line >= content.length)
				return response;
			response.setText(content[line]);

		}

		if (rawWidget instanceof StyledText) {
			StyledText text = (StyledText) rawWidget;

			if (line < text.getLineCount()) {
				response.setText(text.getLine(line));
			}
		}
		return response;
	}

	protected Response handleGetTextRange(final GetTextRange command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final GetTextResponse response = factory.createGetTextResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		Widget rawWidget = element.widget;
		int start = command.getStartOffset();
		int end = command.getEndOffset();
		if (rawWidget instanceof Text) {
			Text text = (Text) rawWidget;
			if (end <= text.getCharCount())
				response.setText(text.getText(start, end));

		}

		if (rawWidget instanceof StyledText) {
			StyledText text = (StyledText) rawWidget;

			if (end <= text.getCharCount())
				response.setText(text.getText(start, end));
		}
		return response;
	}

	protected Response handleGetItems(GetItems command) {
		final GetItemsResponse response = factory.createGetItemsResponse();
		final SWTUIElement element = getMapper().get(command.getElement());

		final Widget combo = element.widget;
		final List<String> items = getItems(combo);
		if (items == null) {
			response.setStatus(ResponseStatus.FAILED);
			response.setMessage("Control is not supported");
			return response;
		}
		response.getResult().addAll(items);
		return response;
	}

	private static List<String> getItems(Widget widget) {
		if (widget instanceof Combo) {
			Combo combo = (Combo) widget;
			return Arrays.asList(combo.getItems());
		} else if (widget instanceof CCombo) {
			CCombo combo = (CCombo) widget;
			return Arrays.asList(combo.getItems());
		}
		return null;
	}

	protected Response handleCopyText(final CopyTextSelection command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		Widget rawWidget = element.widget;
		if (rawWidget instanceof Text) {
			Text text = (Text) rawWidget;
			text.copy();
		}

		if (rawWidget instanceof StyledText) {
			StyledText text = (StyledText) rawWidget;
			text.copy();
		}
		return response;
	}

	protected Response handleCutText(final CutTextSelection command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		getPlayer().exec("cutSelection", new Runnable() {

			@Override
			public void run() {

				Widget rawWidget = element.widget;
				if (rawWidget instanceof Text) {
					Text text = (Text) rawWidget;
					text.cut();
				}

				if (rawWidget instanceof StyledText) {
					StyledText text = (StyledText) rawWidget;
					text.cut();
				}
			}
		});
		return response;
	}

	protected Response handlePasteText(final PasteTextSelection command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		getPlayer().exec("pasteSelection", new Runnable() {

			@Override
			public void run() {
				Widget rawWidget = element.widget;
				if (rawWidget instanceof Text) {
					Text text = (Text) rawWidget;
					text.paste();
				}

				if (rawWidget instanceof StyledText) {
					StyledText text = (StyledText) rawWidget;
					text.paste();
				}
			}
		});
		return response;
	}

	protected Response handleReplaceText(final ReplaceTextSelection command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		getPlayer().exec("replaceSelection", new Runnable() {

			@Override
			public void run() {
				Widget rawWidget = element.widget;
				String replace = command.getText();
				if (rawWidget instanceof Text) {
					Text text = (Text) rawWidget;
					Point selection = text.getSelection();
					String text2 = text.getText();
					text.setText(text2.substring(0, selection.x)
							+ text2.substring(selection.y));
				}

				if (rawWidget instanceof StyledText) {
					StyledText text = (StyledText) rawWidget;
					Point selection = text.getSelection();
					text.replaceTextRange(selection.x, selection.y
							- selection.x, replace);
				}
			}
		});
		return response;
	}

	@Override
	public void notifyUI() {
		getPlayer().wakeup();

	}

	@Override
	public EObject getElementModel(Element element) {
		SWTUIElement swtuiElement = getMapper().get(element);
		if (swtuiElement != null) {
			return swtuiElement.getModel();
		}
		return null;
	}
}
