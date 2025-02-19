/*******************************************************************************
 * Copyright (c) 2009, 2020 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.recording.core.swt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rcptt.tesla.core.am.RecordingModeFeature;
import org.eclipse.rcptt.tesla.core.context.ContextManagement;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.protocol.Click;
import org.eclipse.rcptt.tesla.core.protocol.CompositeUIElement;
import org.eclipse.rcptt.tesla.core.protocol.ControlUIElement;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.LinkUIElement;
import org.eclipse.rcptt.tesla.core.protocol.MouseEvent;
import org.eclipse.rcptt.tesla.core.protocol.MouseEventKind;
import org.eclipse.rcptt.tesla.core.protocol.PartUIElement;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolFactory;
import org.eclipse.rcptt.tesla.core.protocol.SWTDialogKind;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectData;
import org.eclipse.rcptt.tesla.core.protocol.SetSWTDialogInfo;
import org.eclipse.rcptt.tesla.core.protocol.SetSelection;
import org.eclipse.rcptt.tesla.core.protocol.SetTextOffset;
import org.eclipse.rcptt.tesla.core.protocol.SetTextSelection;
import org.eclipse.rcptt.tesla.core.protocol.TextUIElement;
import org.eclipse.rcptt.tesla.core.protocol.Type;
import org.eclipse.rcptt.tesla.core.protocol.ViewerUIElement;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawEvent;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawFactory;
import org.eclipse.rcptt.tesla.core.protocol.raw.SetMode;
import org.eclipse.rcptt.tesla.core.utils.TeslaUtils;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.ui.player.FindResult;
import org.eclipse.rcptt.tesla.internal.ui.player.ISWTModelMapperExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTModelMapper;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.internal.ui.player.viewers.Viewers;
import org.eclipse.rcptt.tesla.recording.aspects.IExtendedSWTEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.SWTEventManager;
import org.eclipse.rcptt.tesla.recording.core.IRecordingHelper;
import org.eclipse.rcptt.tesla.recording.core.IRecordingModeListener;
import org.eclipse.rcptt.tesla.recording.core.IRecordingProcessor;
import org.eclipse.rcptt.tesla.recording.core.IRecordingProcessorExtension;
import org.eclipse.rcptt.tesla.recording.core.TeslaRecorder;
import org.eclipse.rcptt.tesla.recording.core.swt.peg.CommandPostProcessor;
import org.eclipse.rcptt.tesla.recording.core.swt.util.LastEvents;
import org.eclipse.rcptt.tesla.recording.core.swt.util.RecordedEvent;
import org.eclipse.rcptt.tesla.swt.workbench.EclipseWorkbenchProvider;
import org.eclipse.rcptt.util.swt.StringLines;
import org.eclipse.rcptt.util.swt.TableTreeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.keys.IBindingService;
import org.osgi.framework.Bundle;

/**
 * Simple event collector Each used widget will be added as variable.
 *
 */
@SuppressWarnings("restriction")
public class SWTEventRecorder implements IRecordingProcessor, IExtendedSWTEventListener, IRecordingModeListener {
	// private Map<Widget, String> variables = new HashMap<Widget, String>();
	// private Map<String, Integer> varCounters = new HashMap<String,
	// Integer>();

	private LastEvents lastEvents = new LastEvents();

	private boolean enabled = true;
	private final IPartListener listener;
	private final IPageListener pageListener;
	private final IWindowListener windowListener;
	private String beforeTextState = null;
	private DNDSupport dragSupport;
	private final Map<Widget, String> lastTabItemSelection = new HashMap<Widget, String>();
	private final Set<String> pressed = new HashSet<String>();
	private boolean inStyledTextAction = false;
	private Event currentEvent;

	private final class PartListener implements IPartListener {

		public void partOpened(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
			if (getLocator().getRecorder() == null) {
				return;
			}
			if (!getRecorder().hasListeners()) {
				return;
			}
			Context context = ContextManagement.currentContext();
			// TODO: This is Eclipse version dependent test
			if (!context.contains("org.eclipse.swt.custom.CTabFolder", "onMouse")) {
				return;
			}
			if (!TeslaCore.isEclipse4()) {
				// Skip for editors, will be done from different place.
				if (part instanceof IEditorPart) {
					return;
				}
			}
			Display display = PlatformUI.getWorkbench().getDisplay();
			Shell[] shells = display.getShells();
			for (Shell shell : shells) {
				if (isModal(shell)) {
					return;
				}
			}
			PartUIElement resultPart = getLocator().findPartElement(part, false);
			if (resultPart != null) {
				resultPart.close();
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partActivated(IWorkbenchPart part) {
			if (getRecorder() == null) {
				return;
			}
			if (!getRecorder().hasListeners()) {
				return;
			}
			boolean hashMouseUp = false;
			Context context = ContextManagement.currentContext();

			if (context.contains("org.eclipse.swt.custom.CTabFolder", "onMouse")) {
				hashMouseUp = true;
			}
			if (!hashMouseUp) {
				StackTraceElement[] stack = context.getStackTrace();
				for (StackTraceElement e : stack) {
					if (e.getMethodName().equals("mouseUp") || e.getMethodName().equals("handleEvent")
							|| e.getMethodName().equals("activate")) {
						String className = e.getClassName();
						int dollarPos = className.indexOf('$');
						if (dollarPos > 0)
							className = className.substring(0, dollarPos);
						if ("org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer".equals(className)) {
							hashMouseUp = true;
							break;
						}
					}
				}
			}
			if (!hashMouseUp) {
				return;// Skip, because not used click
			}

			if (context.contains("org.eclipse.ui.internal.WorkbenchPage", "hideView"))

			{
				// View was activated programmatically, so ignore.
				return;
			}
			Display display = PlatformUI.getWorkbench().getDisplay();
			Shell[] shells = display.getShells();
			for (Shell shell : shells) {
				if (isModal(shell)) {
					return;
				}
			}

			PartUIElement resultPart = getLocator().findPartElement(part, false);
			if (resultPart != null) {
				resultPart.click();
			}
		}

	}

	public SWTWidgetLocator getLocator() {
		return SWTRecordingHelper.getHelper().getLocator();
	}

	public static boolean isModal(Shell shell) {
		int style = shell.getStyle();
		int mask = SWT.SYSTEM_MODAL | SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL | SWT.MODELESS;
		String text = shell.getText();
		if (text != null && (text.trim().equalsIgnoreCase("problem occurred")
				|| text.trim().equalsIgnoreCase("Operation failed"))) {
			return true;
		}
		return (style & mask) > 0;
	}

	public TeslaRecorder getRecorder() {
		return recorder;
	}

	public SWTEventRecorder() {
		super();
		dragSupport = new DNDSupport();
		listener = new PartListener();

		pageListener = new IPageListener() {

			public void pageOpened(IWorkbenchPage page) {
				page.addPartListener(listener);
			}

			public void pageClosed(IWorkbenchPage page) {
			}

			public void pageActivated(IWorkbenchPage page) {
				page.addPartListener(listener);
			}
		};
		windowListener = new IWindowListener() {

			public void windowOpened(IWorkbenchWindow window) {
				window.addPageListener(pageListener);
				IWorkbenchPage[] pages = window.getPages();
				for (IWorkbenchPage page : pages) {
					page.addPartListener(listener);
				}
			}

			public void windowDeactivated(IWorkbenchWindow window) {
			}

			public void windowClosed(IWorkbenchWindow window) {
			}

			public void windowActivated(IWorkbenchWindow window) {
				window.addPageListener(pageListener);
			}
		};
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.addWindowListener(windowListener);
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (IWorkbenchWindow win : windows) {
			win.addPageListener(pageListener);
			IWorkbenchPage[] pages = win.getPages();
			for (IWorkbenchPage page : pages) {
				page.addPartListener(listener);
			}
		}

		SWTEventManager.addListener(this);
		// JFaceEventManager.addListener(this);
		// WorkbenchEventManager.addListener(this);
		// FormsEventManager.addListener(this);
	}

	private void enable() {
		enabled = true;
	}

	public boolean doProcessing(Context context, boolean contextChanged) {
		return false;
	}

	private Event lastEvent = null;

	public synchronized void recordEvent(Widget widget, int type, Event event) {
		if (getRecorder() == null) {
			return;
		}
		if (!getRecorder().hasListeners()) {
			return;
		}
		if (!enabled) {
			enable();
		}

		if (type == SWT.KeyDown || type == SWT.KeyUp) {
			keysSeen = true;

			if (type == SWT.KeyUp) {
				if (lastTraverseEvent != null) {
					RecordedEvent toRecordingTraverse = new RecordedEvent(getPlayer(), event, type, widget);
					lastTraverseEvent.detail = lastTraverseDetail;

					// Don't record traverse events for Browser - key pressing
					// recorded on KeyUp event
					if (!(widget instanceof Browser)) {
						addToPressed(lastTraverseEvent);
						processTraverse(lastTraverseEvent.widget, lastTraverseEvent, toRecordingTraverse);
						lastEvents.add(toRecordingTraverse);
					}
					lastTraverseEvent = null;
				}
			}
		}

		// fix of fix on Windows, see
		// org.eclipse.swt.widgets.Text.setText(String)
		if (type == SWT.Modify && widget instanceof Text && Platform.getOS().equals(Platform.OS_WIN32)) {
			Text text = (Text) widget;
			if ((text.getStyle() & SWT.MULTI) != 0
					&& ContextManagement.currentContext().contains("org.eclipse.swt.widgets.Text", "setText"))
				return;
		}

		// Check for extension to ignore recording against one of controls
		List<IRecordingProcessorExtension> list = getRecorder().getProcessors(IRecordingProcessorExtension.class);
		for (IRecordingProcessorExtension ext : list) {
			if (ext.isIgnored(widget, type, event)) {
				return;
			}
		}

		if (isIgnored(widget)) {
			return;
		}
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			if (event != null && lastEvent != null) {
				if (event.time == lastEvent.time && event.widget == lastEvent.widget && event.type == lastEvent.type) {
					// Same event captured twice
					// System.out.println("ALREADY_PROCESSED_EVENT");
					return;
				}
			}
		}

		if (widget instanceof Menu && type == SWT.Show) {
			// special case when menu is shown on MouseDown, so we can not
			// record click properly on "some" platforms

			if (lastEvent != null && lastEvent.widget instanceof Button && lastEvent.type == SWT.MouseDown) {
				SWTEventManager.setMenuSource((Menu) widget, lastEvent.widget);

				if (!Platform.getOS().equals(Platform.OS_MACOSX)) {
					// it is recorded on Mac "automatically"
					FindResult result = getLocator().findElement(lastEvent.widget, false, false, true);
					if (result != null) {
						ControlUIElement e = new ControlUIElement(result.element, getRecorder());
						e.clickAndWait();
					}
				}
			} else if (lastEvent != null && lastEvent.widget instanceof Button && lastEvent.type == SWT.Selection) {
				SWTEventManager.setMenuSource((Menu) widget, lastEvent.widget);
			}
		}

		if (event != null)
			lastEvent = event;

		recordRawEvent(widget, type, event);
		recordEvent_(widget, type, event);
	}

	private void recordRawEvent(Widget widget, int type, Event event) {
		if (!getRecorder().isRawEventsEnabled()) {
			return;
		}
		// Record raw event
		RawEvent rawEvent = RawFactory.eINSTANCE.createRawEvent();
		rawEvent.setType(type);
		if (event != null) {
			rawEvent.setButton(event.button);
			rawEvent.setCharacter(event.character);
			rawEvent.setCount(event.count);
			rawEvent.setData(event.data == null ? null : event.data.toString());
			rawEvent.setDetail(event.detail);
			rawEvent.setEnd(event.end);
			rawEvent.setHeight(event.height);
			rawEvent.setIndex(event.index);
			rawEvent.setKeyCode(event.keyCode);
			rawEvent.setStart(event.start);
			rawEvent.setStateMask(event.stateMask);
			if (event.text != null) {
				String textValue = event.text;
				StringBuilder textResult = new StringBuilder();
				for (char c : textValue.toCharArray()) {
					if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
						textResult.append(' ');
					} else {
						textResult.append(c);
					}
				}
				rawEvent.setText(textResult.toString());
			}
			rawEvent.setTime(event.time);
			rawEvent.setWidth(event.width);
			rawEvent.setX(event.x);
			rawEvent.setY(event.y);
			rawEvent.getItem().add(getLocator().getWidgetID(event.item));
			rawEvent.setNativeEvent(!isNotNative(widget));
		}
		rawEvent.getWidget().add(getLocator().getWidgetID(widget));

		getRecorder().addRawEvent(rawEvent);
	}

	public boolean isIgnored(Widget widget) {
		Shell[] ignored = getPlayer().getIgnored();
		if (ignored == null) {
			return false;
		}
		Shell s = null;
		if (widget instanceof Control) {
			s = ((Control) widget).getShell();
		}
		if (widget instanceof MenuItem) {
			s = ((MenuItem) widget).getParent().getShell();
		}
		if (widget instanceof Menu) {
			s = ((Menu) widget).getShell();
		}
		if (widget instanceof ToolItem) {
			s = ((ToolItem) widget).getParent().getShell();
		}
		if (s != null) {
			for (Shell shell : ignored) {
				if (shell == s) {
					return true;
				}
			}
		}

		return false;
	}

	public SWTUIPlayer getPlayer() {
		return getLocator().getPlayer();
	}

	public static boolean keysSeen = false;
	public static Event lastTraverseEvent = null;
	public static int lastTraverseDetail = 0;

	private void recordEvent_(Widget widget, int type, Event event) {
		if (type == DND.DragStart || type == DND.DragEnd || type == DND.DragEnter || type == DND.DragOver
				|| type == DND.DragLeave || type == DND.Drop || type == DND.DragOperationChanged
				|| type == DND.DropAccept || type == DND.DragSetData) {
			if (event.widget == null) {
				event.widget = widget;
			}
			event.type = type;
			recordDragEvent(event);
			return;
		}
		RecordedEvent toRecording = new RecordedEvent(getPlayer(), event, type, widget);

		if (type == SWT.Show && widget instanceof Menu) {
			processMenuShow(widget, toRecording);
		} else if (type == SWT.Hide && widget instanceof Menu) {
			// menuSources.remove(widget);
			lastEvents.add(toRecording);
		}
		Context ctx = ContextManagement.currentContext();
		// Filter not window proc contexts

		if (isNotNative(widget)) {
			return;
		}

		// Check and ignore DataTime subcontrol events, exclusive to
		// SWT.Selection
		if (widget instanceof Control) {
			Control c = (Control) widget;
			if (c.getParent() instanceof DateTime) {
				processDateTime(c.getParent(), c, type, event);
				return;
			} else if (c instanceof DateTime) {
				processDateTime(c, c, type, event);
				return;
			}
		}

		// Check for ccombo based items
		CCombo[] combo = SWTEventManager.getCombo();
		for (CCombo cCombo : combo) {
			if (CComboSupport.isComboWidget(cCombo, widget)) {
				processCCombo(widget, cCombo, type, event);
				return;
			}
		}
		if (widget instanceof CCombo) {
			return; // Set text will be recorded elsewhere
		}

		if ((type == SWT.Selection || type == SWT.DefaultSelection)) {
			processSelection(widget, event, type, ctx);
		} else if (type == SWT.Modify && isSimpleTextControl(widget)) {
			// TODO: In case of combo update called from tree selection, we need
			// to make sure of correct flow filtering.
			if (ctx.contains("org.eclipse.swt.widgets.Combo", "removeAll")
					|| ctx.contains("org.eclipse.swt.widgets.Combo", "setItems")
					|| ctx.contains("org.eclipse.swt.widgets.Combo", "deselectAll")
					|| ctx.contains("org.eclipse.swt.widgets.Combo", "select")
					|| ctx.contains("org.eclipse.swt.widgets.Combo", "setText")
					|| ctx.contains("org.eclipse.jface.internal.databinding.swt.TextTextProperty", "doSetStringValue")
					|| ctx.contains("org.eclipse.swt.widgets.Text", "paste")) {
				return;
			}
			processModify(widget);
		} else if (type == SWT.Activate) {
			lastEvents.add(toRecording);
			// processActivateEvent(widget, toRecording);
		} else if (type == SWT.Deactivate) {
			lastEvents.add(toRecording);
		} else if (type == SWT.MouseUp) {
			processMouseUp(widget, event, toRecording);
		} else if (type == SWT.MouseDown) {
			processMouseDown(widget, event, toRecording);
		} else if (type == SWT.KeyDown) {
			if (ctx.contains("com.xored.swt.internal.composer.GeckoEditor$KeyAdapter", "handleEvent")) {
				return; // this event is duplicate, see QS-882
			}

			if (widget instanceof Combo)
				return;

			addToPressed(event);
			processKeyDown(widget, event);

			lastEvents.add(toRecording);
		} else if (type == SWT.KeyUp) {
			if (ctx.contains("com.xored.swt.internal.composer.GeckoEditor$KeyAdapter", "handleEvent")) {
				return; // this event is duplicate, see QS-882
			}

			if (widget instanceof Combo)
				return;

			String v = KeyStroke.getInstance(event.stateMask, event.keyCode).toString().toLowerCase();
			String[] values = v.split("\\+");
			boolean contains = false;
			for (String val : values) {
				if (pressed.contains(val)) {
					contains = true;
				}
			}
			// On MacOSX after press Enter in dialog with default selection,
			// no
			// KeyDown and Traverse event but unnecessary KeyUp
			boolean isEnterOnMac = Platform.getOS().equals(Platform.OS_MACOSX) && (event.character == SWT.CR)
					&& event.stateMask == 0;

			if (!contains && !isEnterOnMac) {
				// This is just key-up event
				processKeyDown(widget, event);
			}
			for (String val : values) {
				pressed.remove(val);
			}

			if (isSimpleTextControl(widget)) {
				beforeTextState = null;
			}

			lastEvents.add(toRecording);
			// }
		} else if (type == SWT.Traverse) {
			keysSeen = false;

			if (event.detail != SWT.TRAVERSE_PAGE_NEXT && event.detail != SWT.TRAVERSE_PAGE_PREVIOUS) {
				// Don't record traverse events for Browser - key pressing
				// recorded on KeyUp event
				if (!(widget instanceof Browser)) {
					addToPressed(event);
					processTraverse(widget, event, toRecording);
					lastEvents.add(toRecording);
				}
			} else if (event.detail != SWT.TRAVERSE_NONE) {
				event.widget = widget;
				lastTraverseEvent = event;
				lastTraverseDetail = event.detail;
			}
		} else if (type == SWT.MouseDoubleClick) {
			Listener[] listeners = widget.getListeners(SWT.MouseDoubleClick);
			if (listeners.length != 0 && !(widget instanceof ViewForm) && !(widget instanceof Sash)) {
				FindResult result = getLocator().findElement(widget, true, false, false);

				// System.out.println("RESULT is:" + result);
				if (result != null && result.element != null) {
					if (result.realElement.isTree() || result.realElement.isTable() || result.realElement.isList()) {
						String[] sel = result.realElement.getPathSelection();
						ViewerUIElement v = new ViewerUIElement(result.element, getRecorder());
						// Do not pass set selection if previous command is
						// click,
						// so double click will override it
						Command last = getRecorder().getContainer().getLast();
						if (sel != null && !(last instanceof Click && ((Click) last).isDefault())) {
							v.setSelection(sel);
						}
						v.doubleClick();
					} else {
						// Check for not a canvas
						if (!(isCanvas(widget, type))
								&& !widget.getClass().getName().equals("org.eclipse.draw2d.FigureCanvas")) {
							ControlUIElement e = new ControlUIElement(result.element, getRecorder());
							e.doubleClickAndWait();
						}
					}
				}
			}
			lastEvents.add(toRecording);
		} else if (type == SWT.Close) {
			processClose(widget);
		} else if (type == SWT.FocusIn) {
			processFocusIn(widget);
		}
		// else if (type == SWT.FocusOut) {
		// processFocusOut(widget);
		// }
		// else if (StyledTextSupport.isCaretEvent(widget.getClass(), type)
		// && widget instanceof StyledText) {
		// StyledText text = (StyledText) widget;
		// int offset = text.getCaretOffset();
		// FindResult element = findElement(widget, false, false, false);
		// TextUIElement textElement = new TextUIElement(element.element,
		// getRecorder());
		// textElement.selectLine(line);
		// }
		else {
			lastEvents.add(toRecording);
		}
	}

	private static boolean hasListeners(Widget widget, int event) {
		return hasNonPlatformListeners(widget.getListeners(event), event);
	}

	private void processFocusIn(Widget widget) {
		if (!(widget instanceof Text) || !hasListeners(widget, SWT.FocusIn)) {
			return;
		}
		// See http://jira4.xored.com/browse/QS-2100
		// The issue is that jface WizardDialog sets focus to the
		// focus control before stopping. Looks like there's no
		// other way to handle this situation besides
		// checking current stack trace and searching whether
		// WizardDialog#stopped is invoked
		Context ctx = ContextManagement.currentContext();
		if (ctx.contains(WizardDialog.class.getName(), "stopped")) {
			return;
		}
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			if (ctx.contains(Display.class.getName(), "checkFocus")) {
				return;
			}
		}
		FindResult element = getLocator().findElement(widget, true, false, true);
		if (element == null || element.element == null) {
			return;
		}

		new ControlUIElement(element.element, getRecorder()).setFocus();
	}

	public void addToPressed(Event event) {
		KeyStroke strk = KeyStroke.getInstance(event.stateMask, event.keyCode);
		String split = strk.toString().toLowerCase();
		String[] values = split.split("\\+");
		for (String val : values) {
			if (!pressed.contains(val))
				pressed.add(val);
		}
	}

	private void processDateTime(Control dateTime, Control c, int type, Event event) {
		DateTime dt = (DateTime) dateTime;
		FindResult element;
		switch (type) {
		case SWT.Selection:
			element = getLocator().findElement(dateTime, false, false, true);
			if (element != null && element.element != null) {
				TextUIElement e = new TextUIElement(element.element, getRecorder());
				e.setText(PlayerTextUtils.getDateTimeValue(dt));
			}
			break;
		case SWT.MouseDoubleClick:
			element = getLocator().findElement(dateTime, false, false, true);
			if (element != null && element.element != null) {
				ControlUIElement e = new ControlUIElement(element.element, getRecorder());
				e.doubleClickAndWait();
			}
			break;
		case SWT.KeyDown:
			processKeyDown(dt, event);
			break;
		}
	}

	public boolean isCanvas(Widget widget, int type) {
		List<IRecordingProcessorExtension> list = getRecorder().getProcessors(IRecordingProcessorExtension.class);
		for (IRecordingProcessorExtension ext : list) {
			if (widget instanceof Canvas && ext.isNotCanvas(widget, type, null)) {
				return false;
			}
		}

		if (widget instanceof Canvas && !(widget instanceof Shell) && !(widget instanceof StyledText)
				&& !(widget instanceof Browser) && !(widget instanceof CLabel)) {
			// Fiter events came to EditDomain
			if (type == SWT.MouseDown || type == SWT.MouseUp || type == SWT.MouseMove || type == SWT.MouseDoubleClick
					|| type == SWT.MouseEnter || type == SWT.MouseExit || type == SWT.MouseHover
					|| type == SWT.MouseWheel) {
				String clName = widget.getClass().getName();
				if (!clName.equals("org.eclipse.draw2d.FigureCanvas")) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isNotNative(Widget widget) {
		Context ctx = ContextManagement.currentContext();
		// if (ctx.hasCall("org.eclipse.swt.widgets.Display",
		// "runDeferredEvents",
		// 6)
		// || ctx
		// .hasCall("org.eclipse.swt.widgets.Widget", "sendEvent",
		// 6)) {
		// return false;
		// }
		if (ctx.contains("org.eclipse.swt.widgets.Composite", "setFocus")) {
			return true;
		}
		if (ctx.contains("org.eclipse.swt.widgets.Shell", "close")) {
			return true;
		}
		if (ctx.contains("org.eclipse.swt.widgets.TreeItem", "setExpanded")) {
			return true;
		}
		if (ctx.contains("org.eclipse.ui.views.properties.PropertySheetViewer", "removeItem")) {
			return true;
		}

		if (ctx.contains("org.eclipse.swt.widgets.TabFolder", "createItem")
				|| ctx.contains("org.eclipse.swt.widgets.TabFolder", "destroyItem")) {
			return true;
		}
		if (ctx.containsClass("org.eclipse.swt.custom.CCombo") && !(widget instanceof CCombo)) {
			return true;
		}

		if (!getRecorder().isNotNativeEventsIgnored()) {
			return false;
		}

		// if (Platform.getOS().equals(Platform.OS_MACOSX)) {
		if (!ctx.contains("org.eclipse.swt.widgets.Display", "windowProc")
				&& !ctx.contains("org.eclipse.swt.widgets.Display", "runDeferredEvents")
				&& !ctx.contains("org.eclipse.swt.widgets.Control", "traverse")) {
			return true;
		}
		// } else {
		// if (!ctx.contains("org.eclipse.swt.widgets.Display", "windowProc")) {
		// return true;
		// }
		// }

		return false;
	}

	private void processCCombo(Widget widget, CCombo cCombo, int type, Event event) {
		switch (type) {
		case SWT.Selection:
			if (widget instanceof org.eclipse.swt.widgets.List) {
				FindResult result = getLocator().findElement(cCombo, true, false, false);
				if (result != null) {
					org.eclipse.swt.widgets.List l = (org.eclipse.swt.widgets.List) widget;
					TextUIElement e = new TextUIElement(result.element, getRecorder());
					String[] selection = l.getSelection();
					if (selection.length == 1) {
						String value = selection[0];
						processComboSelection(e, cCombo.getItems(), value);
					}
				}
			}
			break;

		// case SWT.KeyDown:
		// // if (!(widget instanceof Text)) {
		// processKeyDown(cCombo, event);
		// lastEvents.add(toRecording);
		// // }
		// break;
		//
		// case SWT.KeyUp:
		// if (isSimpleTextControl(widget)) {
		// beforeTextState = null;
		// }
		// lastEvents.add(toRecording);
		// break;

		case SWT.Modify:
			FindResult result = getLocator().findElement(cCombo, true, false, false);
			if (result != null) {
				TextUIElement e = new TextUIElement(result.element, getRecorder());
				String text = cCombo.getText();
				if (Arrays.asList(cCombo.getItems()).contains(text))
					e.select(text);
				else
					e.setText(text);
			}
			break;
		}
	}

	@SuppressWarnings("unused")
	private boolean isDebug() {
		Bundle bundle = Platform.getBundle("org.eclipse.rcptt.tesla.recording.ui.dev");
		return bundle != null;
	}

	public static boolean isIgnoreSelection(Widget widget, Event event, int type, Context ctx) {
		SWTUIElement swtuiElement = SWTRecordingHelper.getHelper().getLocator().getPlayer().wrap(widget);
		if (swtuiElement != null && swtuiElement.getKind().is(ElementKind.Unknown))
			return true;

		if (widget instanceof Combo && Arrays.asList(SWT.Selection, SWT.DefaultSelection).contains(type)) {
			return true;
		}

		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			if (widget instanceof Sash) {
				return true;
			}

			// Ignore radio buttons it's recorded on MouseUp (on some radio on
			// MacOSX selection event not appear)
			// for example see radio buttons on Install/Update preference page
			if (widget instanceof Button && (((Button) widget).getStyle() & SWT.RADIO) != 0) {
				return true;
			}
		}

		return false;
	}

	private void processSelection(Widget widget, Event event, int type, Context ctx) {
		boolean isRadioWidget = widget instanceof Button && (((Button) widget).getStyle() & SWT.RADIO) != 0;
		boolean isTabFolder = widget instanceof TabFolder || widget instanceof CTabFolder;
		boolean isTreeOrTableOrList = (widget instanceof Tree || widget instanceof Table
				|| widget instanceof org.eclipse.swt.widgets.List);
		boolean clean = true;

		if (EclipseWorkbenchProvider.getProvider().isViewOrEditorButton(widget)) {
			ToolItem button = (ToolItem) widget;
			String tip = button.getToolTipText();
			CTabFolder ctabFolder = EclipseWorkbenchProvider.getProvider().getTabFolderFromButton(button);
			if (ctabFolder != null && tip != null) {
				if (tip.equals("Maximize")) {
					SWTEventManager.recordTabFolderEvent(ctabFolder, SWTEventManager.EVENT_TAB_MAXIMIZE);
				} else if (tip.equals("Minimize")) {
					SWTEventManager.recordTabFolderEvent(ctabFolder, SWTEventManager.EVENT_TAB_MINIMIZE);
				} else if (tip.equals("Restore")) {
					SWTEventManager.recordTabFolderEvent(ctabFolder, SWTEventManager.EVENT_TAB_RESTORE);
				} else if (tip.equals("View Menu")) {
					// do nothing
				} else if (tip.equals("Show List")) {
					SWTEventManager.recordTabFolderEvent(ctabFolder, SWTEventManager.EVENT_TAB_SHOW_LIST);
				}
				return;
			}
		}

		if (isIgnoreSelection(widget, event, type, ctx)) {
			return;
		}

		if (WidgetClassifier.isAColumn(widget)) {
			processColumnHeaderSelection(widget);
			return;
		}

		if (widget instanceof org.eclipse.swt.widgets.Link) {
			FindResult result = getLocator().findElement(widget, true, false, false);
			if (result != null && result.element != null) {
				LinkUIElement e = new LinkUIElement(result.element, getRecorder());
				String[] links = LinkUIElement.parse(((org.eclipse.swt.widgets.Link) widget).getText());
				if (links.length == 1 && links[0].equals(event.text)) {
					// There's only one clickable segment in the
					// link, so 'click-link' command will be recorded without
					// argument
					e.clickLink(null);
				} else {
					e.clickLink(event.text);
				}
			}
			return;
		}

		if ((widget instanceof MenuItem) && SWTWidgetLocator.isCTabFolderListMenuItem((MenuItem) widget)) {
			CTabFolder miTabFolder = SWTWidgetLocator.getCTabFolder((MenuItem) widget);
			FindResult result = getLocator().findElement(miTabFolder, true, false, false);
			ProcessCTabFolderItemSelection(miTabFolder, result, ((MenuItem) widget).getText());
			return;
		}

		if (widget instanceof Slider) {
			FindResult result = getLocator().findElement(widget, true, false, false);
			if (result != null && result.element != null) {
				TextUIElement e = new TextUIElement(result.element, getRecorder());
				e.setText(Integer.toString(((Slider) widget).getSelection()));
			}
			return;
		}

		boolean isCheckable = Platform.getOS().equals(Platform.OS_WIN32) && isTreeOrTableOrList;
		boolean isMouseOrKeyOps = lastEvents.checkType(widget, SWT.MouseUp, SWT.MouseDown, SWT.Arm, SWT.KeyDown,
				SWT.KeyUp, SWT.MouseDoubleClick, SWT.MouseMove);

		boolean isFromFilteredItemsSelectionDialog = ctx.contains("org.eclipse.ui.dialogs.FilteredItemsSelectionDialog",
				"refresh");
		if (isFromFilteredItemsSelectionDialog)
			return;

		boolean isWidgetSendSelectionNonWin32 = ctx.contains("org.eclipse.swt.widgets.Widget", "sendSelectionEvent")
				&& !Platform.getOS().equals(Platform.OS_WIN32);

		boolean isRunDefferedEventsOSX = ctx.contains("org.eclipse.swt.widgets.Display", "runDeferredEvents")
				&& Platform.getOS().equals(Platform.OS_MACOSX);

		boolean isButtonFocusEvent = widget instanceof Button
				&& (lastEvents.checkType(widget, SWT.FocusIn) || ((Button) widget).isFocusControl());
		boolean isListTreeTableActivate = (widget instanceof Table || widget instanceof Tree
				|| widget instanceof org.eclipse.swt.widgets.List) && lastEvents.checkType(widget, SWT.Activate);
		if (widget instanceof Sash) {
			return;
		}
		try {
			if (widget instanceof Spinner) {
				FindResult result = getLocator().findElement(widget, true, false, false);
				// System.out.println("RESULT is:" + result);
				if (result != null && result.element != null) {
					TextUIElement e = new TextUIElement(result.element, getRecorder());
					e.setText(Integer.toString(((Spinner) widget).getSelection()));
				}
			}
			if (widget instanceof Text || widget instanceof StyledText || widget instanceof Browser) {
				processTextWidgetSelectionEvent(widget);
				return;
			}
			// Radio MenuItem (first event from previous selected item, second
			// event from item that was selected)
			if (widget instanceof MenuItem && (((MenuItem) widget).getStyle() & SWT.RADIO) != 0) {
				FindResult result = getLocator().findElement(widget, true, false, false);
				if (((MenuItem) widget).getSelection() && result != null && result.element != null) {
					ControlUIElement e = new ControlUIElement(result.element, getRecorder());
					e.clickAndWait(type == SWT.DefaultSelection, event.stateMask);
				}
			} else if ((isMouseOrKeyOps || isWidgetSendSelectionNonWin32 || isRunDefferedEventsOSX || isButtonFocusEvent
					|| isListTreeTableActivate || isCheckable) && !(isRadioWidget && !((Button) widget).getSelection())
					&& !isTabFolder || (type == SWT.DefaultSelection && isTreeOrTableOrList)) {
				if (widget instanceof MenuItem) {
					MenuItem mi = (MenuItem) widget;
					Object data = mi.getData();
					if (data instanceof ActionContributionItem) {
						ActionContributionItem aci = (ActionContributionItem) data;
						if (isIgnoredAction(aci.getAction())) {
							return; // ignore Preferences and About menu item
						}
					}
				}
				FindResult result = getLocator().findElement(widget, true, false, false);

				if (result != null && result.element != null) {
					if (result.realElement.isTree() || result.realElement.isTable() || result.realElement.isList()) {
						processViewers(event, type, result);
					} else {
						if (!result.realElement.isText()) {
							ControlUIElement e = new ControlUIElement(result.element, getRecorder());
							boolean skipEvent = false;
							if (Platform.getOS().equals(Platform.OS_MACOSX)) {
								if (widget instanceof Button) {
									if ((widget.getStyle() & SWT.CHECK) != 0) {
										skipEvent = true;
									}
								}
							}
							if (!skipEvent) {
								if (event != null && event.detail == SWT.ARROW) {
									e.clickArrowAndWait(type == SWT.DefaultSelection);
								} else if (widget instanceof Button && (widget.getStyle() & SWT.CHECK) != 0) {
									e.check(((Button) widget).getSelection());
								} else {
									e.clickAndWait(type == SWT.DefaultSelection, event.stateMask);
								}
							}
						}
					}
				}
			} else {
				if (widget instanceof ToolItem && (lastEvents.checkType(((ToolItem) widget).getParent(), SWT.MouseUp)
						|| Platform.getOS().equals(Platform.OS_WIN32))) {
					processToolItem(widget, event);
				} else if (widget instanceof CoolItem
						&& (lastEvents.checkType(((CoolItem) widget).getParent(), SWT.MouseUp)
								|| Platform.getOS().equals(Platform.OS_WIN32))) {
					processCoolItem(widget);
				} else if (isRadioWidget) {
					if (!Platform.getOS().equals(Platform.OS_WIN32)) {
						Button radio = (Button) widget;
						if (radio.getSelection()) {
							FindResult result = getLocator().findElement(widget, true, false, false);
							// System.out.println("RESULT is:" + result);
							ControlUIElement e = new ControlUIElement(result.element, getRecorder());
							e.clickAndWait();
							lastEvents.clear();
							// System.out.println("_1_Clean Events" + index++);
							clean = false;
						}
					} else {
						clean = false;
					}
				}
			}
			isTabFolder = processTabFolder(widget, isTabFolder);
		} finally {
			if (clean && !isTabFolder) {
				lastEvents.clear();
				// menuSources.clear();
				// System.out.println("_2_Clean Events" + index++);
			}
		}
	}

	private void processColumnHeaderSelection(Widget widget) {
		FindResult result = getLocator().findElement(widget, false, false, false);
		new ControlUIElement(result.element, getRecorder()).click();
	}

	private boolean isIgnoredAction(IAction action) {
		if (action.getId() != null && action.getId().equals("preferences")) {
			if ((action.getActionDefinitionId() != null
					&& action.getActionDefinitionId().equals("org.eclipse.ui.window.preferences"))
					&& !action.getClass().getName().equals("org.eclipse.ui.internal.OpenPreferencesAction")) {
				return true;
			}
		} else if (action.getId() != null && action.getId().equals("about")) {
			if (action.getActionDefinitionId() != null
					&& action.getActionDefinitionId().equals("org.eclipse.ui.help.aboutAction")) {
				return true;
			}
		}
		if (action.getClass().getName().equals("org.eclipse.ui.internal.OpenPreferencesAction")) {
			// Do
			// getRecorder().safeExecuteCommand(
			// ProtocolFactory.eINSTANCE.createClickPreferencesMenu());
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private void processTextWidgetSelectionEvent(Widget widget) {
		if (!inStyledTextAction) {

			Object this1 = TeslaSWTAccess.getThis(widget);

			FindResult result = getLocator().findElement(widget, true, false, false);
			if (result != null) {
				TextUIElement textCtrl = new TextUIElement(result.element, getRecorder());
				if (widget instanceof StyledText) {
					StyledText styledText = ((StyledText) widget);
					Point selection = styledText.getSelectionRange();
					if (selection.y != 0) {
						Command last = getRecorder().getContainer().getLast();
						if (last instanceof SetTextOffset) {
							SetTextOffset offset = (SetTextOffset) last;
							if (EcoreUtil.equals(offset.getElement(), result.element)) {
								getRecorder().removeLast();
							}
						}

						int startLine = styledText.getLineAtOffset(selection.x);
						int startOffset = selection.x - styledText.getOffsetAtLine(startLine);

						int endLine = styledText.getLineAtOffset(selection.x + selection.y);
						int endOffset = (selection.x + selection.y) - styledText.getOffsetAtLine(endLine);

						int caretOffset = styledText.getCaretOffset();
						if (caretOffset == selection.x) {
							textCtrl.setSelection(endLine, endOffset, startLine, startOffset);
						} else {
							textCtrl.setSelection(startLine, startOffset, endLine, endOffset);
						}
					}
				}
			}
		}
	}

	private boolean processTabFolder(Widget widget, boolean tabFolder) {
		if (!(widget instanceof TabFolder) && !(widget instanceof CTabFolder)) {
			return tabFolder;
		}
		FindResult result = getLocator().findElement(widget, true, false, false);

		if (result == null) {
			return tabFolder;
		}

		String tabName = null;
		if (widget instanceof TabFolder) {
			TabFolder tabFolderWidget = (TabFolder) widget;
			TabItem selectedItem = null;

			int selectionIndex = tabFolderWidget.getSelectionIndex();
			if (selectionIndex != -1) {
				selectedItem = tabFolderWidget.getItem(selectionIndex);

			}

			// Ivan Inozemtsev: On Linux if there are no selection listeners,
			// then selection event happens before updating
			// selection index, but after real tab switching
			// therefore, we need to manually iterate through
			// tabs in order to find which one is visible
			for (TabItem item : tabFolderWidget.getItems()) {
				if (item.isDisposed()) {
					continue;
				}

				Control content = item.getControl();
				if (content == null || content.isDisposed()) {
					continue;
				}

				if (content.isVisible()) {
					selectedItem = item;
					break;
				}
			}

			if (selectedItem == null) {
				return tabFolder;
			}
			tabName = PlayerTextUtils.removeAcceleratorFromText(selectedItem.getText());
		} else {
			CTabFolder tabFolderWidget = (CTabFolder) widget;
			CTabItem selectedItem = null;

			int selectionIndex = tabFolderWidget.getSelectionIndex();
			if (selectionIndex != -1) {
				selectedItem = tabFolderWidget.getItem(selectionIndex);

			}

			// Ivan Inozemtsev: On Linux if there are no selection listeners,
			// then selection event happens before updating
			// selection index, but after real tab switching
			// therefore, we need to manually iterate through
			// tabs in order to find which one is visible
			for (CTabItem item : tabFolderWidget.getItems()) {
				if (item.isDisposed()) {
					continue;
				}

				Control content = item.getControl();
				if (content == null || content.isDisposed()) {
					continue;
				}

				if (content.isVisible()) {
					selectedItem = item;
					break;
				}
			}

			if (selectedItem == null) {
				return tabFolder;
			}
			tabName = PlayerTextUtils.removeAcceleratorFromText(selectedItem.getText());
		}

		if (lastTabItemSelection.containsKey(widget)) {
			String v = lastTabItemSelection.get(widget);
			if (v.equals(tabName)) {
				return tabFolder;
			}
		}

		CompositeUIElement v = new CompositeUIElement(result.element, getRecorder());
		ControlUIElement tabItem = v.tabItem(tabName);
		tabItem.clickAndWait();
		return false;

	}

	private void processCoolItem(Widget widget) {
		// System.out.println("$");
		FindResult result = getLocator().findElement(widget, false, false, false);
		if (result != null) {
			ControlUIElement ctrl = new ControlUIElement(result.element, getRecorder());
			ctrl.clickAndWait();
		}
	}

	private void processToolItem(Widget widget, Event event) {
		ToolItem item = (ToolItem) widget;
		if ((item.getStyle() & SWT.RADIO) != 0 && !item.getSelection())
			return;

		boolean arrow = (event != null && event.detail == SWT.ARROW);
		FindResult result = getLocator().findElement(widget, false, false, false);
		if (result != null && result.element != null) {
			ControlUIElement ctrl = new ControlUIElement(result.element, getRecorder());
			if (arrow) {
				ctrl.clickArrowAndWait();
			} else {
				ctrl.clickAndWait();
			}
		}
	}

	private void processViewers(Event event, int type, FindResult result) {
		if (result.realElement.isSupportMultipleSelections()) {
			ViewerUIElement v = new ViewerUIElement(result.element, getRecorder());
			if (event.detail == SWT.CHECK && event.item != null) {
				String[] sel = null;
				if (event.item instanceof TableItem) {
					TableItem tItem = (TableItem) event.item;
					sel = Viewers.getPathByTableItem(tItem);
				} else if (event.item instanceof TreeItem) {
					TreeItem trItem = (TreeItem) event.item;
					sel = Viewers.getPathByTreeItem(trItem);
				}
				boolean checked = true;
				if (event.item instanceof TreeItem) {
					checked = ((TreeItem) event.item).getChecked();
				} else if (event.item instanceof TableItem) {
					checked = ((TableItem) event.item).getChecked();
				}
				v.checkItem(checked, sel);
			} else {
				String[][] selection = result.realElement.getMultiPathSelection();
				if (selection != null && selection.length > 0) {
					if (!lastEvents.checkType(result.realElement.widget, SWT.MouseDoubleClick)) {
						if (type == SWT.DefaultSelection) {
							v.click(true);
						} else {
							if (recordClickInSeletion) {
								recordClickInSeletion = false;
								String[] path = Viewers.getPathByItem(event.item);
								v.item(path).click();
							} else {
								v.setMultiSelection(selection);
							}
						}
					}
				}
			}
		} else {
			String sel[] = null;
			if (sel == null) {
				sel = result.realElement.getPathSelection();
			}
			if (event != null && event.item != null && event.detail == SWT.CHECK) {
				// Try to extract selection from event.
				if (event.item instanceof TableItem) {
					TableItem tItem = (TableItem) event.item;
					sel = Viewers.getPathByTableItem(tItem);
				} else if (event.item instanceof TreeItem) {
					TreeItem trItem = (TreeItem) event.item;
					sel = Viewers.getPathByTreeItem(trItem);
				}
			}
			if (sel != null && sel.length > 0) {
				ViewerUIElement v = new ViewerUIElement(result.element, getRecorder());
				if (event != null && event.detail == SWT.CHECK) {
					boolean checked = true;
					if (event.item instanceof TreeItem) {
						checked = ((TreeItem) event.item).getChecked();
					} else if (event.item instanceof TableItem) {
						checked = ((TableItem) event.item).getChecked();
					}
					v.checkItem(checked, sel);
				} else {
					if (!lastEvents.checkType(result.realElement.widget, SWT.MouseDoubleClick)) {
						if (type == SWT.DefaultSelection) {
							v.click(true);
						} else {
							if (recordClickInSeletion) {
								recordClickInSeletion = false;
								String[] path = Viewers.getPathByItem(event.item);
								v.item(path).click();
							} else
								v.setSelection(sel);
						}
					}
				}
			}
		}
	}

	private static void processComboSelection(TextUIElement e, String[] items, String text) {
		if (Arrays.asList(items).contains(text))
			e.select(text);
		else
			e.setText(text);
	}

	private void processModify(Widget widget) {
		SWTUIElement wrap = getPlayer().wrap(widget);

		if (wrap.isDisabled()) { // Do not record modifications of disabled
			// fields.
			return;
		}
		if (lastEvents.checkType(widget, SWT.KeyDown, SWT.KeyUp, SWT.MouseDown) && !(widget instanceof Combo)) {
			FindResult element = getLocator().findElement(widget, true, false, false);
			if (element != null) {
				TextUIElement e = new TextUIElement(element.element, getRecorder());
				String currentText = element.realElement.getModificationText();
				if (currentText.trim().length() == 0) {
					// try obtain one character from keydown
					RecordedEvent e2 = lastEvents.getEvent(widget, SWT.KeyDown);
					if (e2 != null && !Character.isISOControl(e2.event.character)) {
						currentText = new String(new char[] { e2.event.character });
					}
				}
				if (beforeTextState == null || !beforeTextState.equals(currentText)) {
					if (widget != JFaceRecordingProcessor.lastCellEditorControl) {
						if (!(widget instanceof Combo)) {
							e.setText(currentText, false, isPasswordField(widget));
						} else {
							processComboSelection(e, ((Combo) widget).getItems(), currentText);
						}
					}
				}
			}
			beforeTextState = null;
			lastEvents.clear();
			pressed.clear();
		} else if (widget instanceof Combo) {
			Control ctrl = (Control) widget; // Skip events from invisible
			// shells.
			if (!ctrl.getShell().isVisible()) {
				return;
			}
			FindResult element = getLocator().findElement(widget, true, false, false);
			if (element != null && element.element != null) {
				TextUIElement e = new TextUIElement(element.element, getRecorder());
				processComboSelection(e, ((Combo) widget).getItems(), element.realElement.getModificationText());
				lastEvents.clear();
				// System.out.println("_4_Clean Events" + index++);
			}
			// System.out.println("Clean Events" + index++);
		}
	}

	private static boolean isPasswordField(Widget widget) {
		if ((widget.getStyle() & SWT.PASSWORD) != 0)
			return true;
		if (widget instanceof Text) {
			return ((Text) widget).getEchoChar() != '\0';
		}
		return false;
	}

	private void processMenuShow(Widget widget, RecordedEvent toRecording) {
		Object menuSource = null;

		// try to find column
		if (lastEvent != null && lastEvent.type == SWT.MenuDetect
				&& (lastEvent.widget instanceof Tree || lastEvent.widget instanceof Table)) {
			Point point = lastEvent.widget.getDisplay().map(null, (Control) lastEvent.widget, lastEvent.x, lastEvent.y);
			menuSource = TableTreeUtil.getColumnFromPoint(lastEvent.widget, point);
		}

		if (menuSource == null) {
			// Save click widget
			// Last last event with specified menu
			for (RecordedEvent e : lastEvents.fromMostRecent()) {
				Widget wid = e.widget;
				if (wid instanceof Control && !wid.isDisposed()) {
					Menu widMenu = ((Control) wid).getMenu();
					if (widMenu != null && widMenu.equals(widget)) {
						menuSource = wid;
						break;
					}
				}
			}
		}

		if (menuSource == null) {
			// Go over all control and try to locate menu
			if (widget instanceof Menu) {
				Decorations parent = ((Menu) widget).getParent();
				menuSource = findMenuSource(parent, widget);

				if (menuSource == null) {
					menuSource = SWTWidgetLocator.findViewMenuSource(widget);
				}
			}
		}
		if (menuSource != null) {
			SWTEventManager.setMenuSource((Menu) widget, menuSource);

		}
		getLocator().cleanMenuSources();
		lastEvents.add(toRecording);

		// Add selector for menu, for cases user show/hide menu, and some stuff
		// in APP are changed.
		getLocator().findElement(widget, false, false, false);
	}

	private Widget findMenuSource(Composite parent, Widget widget) {

		Menu menu = parent.getMenu();

		if (menu != null && menu.equals(widget)) {
			return parent;
		}
		Control[] children = parent.getChildren();
		for (Control control : children) {
			menu = control.getMenu();
			if (menu != null && menu.equals(widget)) {
				return control;
			}
			if (control instanceof Composite) {
				Widget result = findMenuSource((Composite) control, widget);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	public void processKeyDown(Widget widget, Event event) {
		// if (checkType(widget, SWT.KeyDown)) {
		boolean shift = (event.stateMask & SWT.SHIFT) != 0;
		boolean ctrlb = (event.stateMask & SWT.CTRL) != 0;
		boolean alt = (event.stateMask & SWT.ALT) != 0;
		boolean check1 = ((event.stateMask & SWT.MODIFIER_MASK) != 0) && (!shift || ctrlb || alt);
		boolean enter = (event.character == SWT.CR) && event.stateMask == 0;
		boolean helpKey = event.keyCode == SWT.F1;
		boolean arrayKey = event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN
				|| event.keyCode == SWT.ARROW_LEFT || event.keyCode == SWT.ARROW_RIGHT;

		FindResult result = getLocator().findElement(widget, true, false, false);
		if (result != null) {
			if (isSimpleTextControl(widget)) {
				beforeTextState = result.realElement.getModificationText();
				if (enter || helpKey || arrayKey) {
					ControlUIElement ctrl = new ControlUIElement(result.element, getRecorder());
					ctrl.press(event.keyCode, event.stateMask, widget instanceof Browser || helpKey, event.character,
							getMeta(event.stateMask));
					return;
				}
			}
			if (!isSimpleTextControl(widget) || check1) {
				ControlUIElement ctrl = new ControlUIElement(result.element, getRecorder());
				if (!(event.keyCode == SWT.SHIFT || event.keyCode == SWT.CTRL || event.keyCode == SWT.ALT
						|| event.keyCode == SWT.COMMAND)) {
					if ((event.keyCode & SWT.KEYCODE_BIT) == 0 && event.stateMask == 0
							&& !Character.isISOControl(event.character)) {
						if (widget instanceof StyledText || widget instanceof Browser
								|| widget.getListeners(SWT.KeyDown).length > 0
								|| widget.getListeners(SWT.KeyUp).length > 0) {
							ctrl.type(new String(new char[] { event.character }), widget instanceof Browser);
						}
					} else {
						// TODO record shortcut with command binding
						// String typeActionId = checkForActionCommand(
						// event.stateMask, event.keyCode);
						// if (typeActionId != null) {
						// ctrl.typeAction(typeActionId);
						// } else {
						if (widget instanceof StyledText || widget instanceof Browser) {
							if (event.stateMask == SWT.SHIFT && !Character.isISOControl(event.character)) {
								ctrl.type(new String(new char[] { event.character }), widget instanceof Browser);
							} else {
								ctrl.press(event.keyCode, event.stateMask, widget instanceof Browser, event.character,
										getMeta(event.stateMask));
							}
						} else {
							ctrl.press(event.keyCode, event.stateMask, widget instanceof Browser, event.character,
									getMeta(event.stateMask));
						}
						// }
					}
				}
			}
			lastEvents.clear();
		}
		// }
	}

	private static boolean isSimpleTextControl(Widget widget) {
		if (widget instanceof Combo || widget instanceof Spinner)
			return true;
		if (widget instanceof Text)
			// && (((Text) widget).getStyle() & SWT.SINGLE) != 0)
			return true;
		return false;
	}

	public void processTraverse(Widget widget, Event event, RecordedEvent toRecording) {
		// === traverse types ===
		// - SWT.TRAVERSE_NONE
		// * SWT.TRAVERSE_ESCAPE (event chain)
		// - SWT.TRAVERSE_RETURN (event chain)
		// * SWT.TRAVERSE_TAB_NEXT
		// * SWT.TRAVERSE_TAB_PREVIOUS
		// * SWT.TRAVERSE_ARROW_NEXT
		// * SWT.TRAVERSE_ARROW_PREVIOUS
		// * SWT.TRAVERSE_MNEMONIC
		// * SWT.TRAVERSE_PAGE_NEXT
		// * SWT.TRAVERSE_PAGE_PREVIOUS

		// detecting assert/record mode shortcuts
		if (event.detail == SWT.TRAVERSE_MNEMONIC) {
			TeslaRecorder recorder = getRecorder();
			if (recorder == null) {
				return;
			}
			String[] assertShortcuts = recorder.getAssertModeShortcuts();
			String[] recordShortcuts = recorder.getRecordModeShortcuts();
			String[] startRecordShortcuts = recorder.getStartRecordShortcuts();
			String[] stopRecordShortcuts = recorder.getStopRecordShortcuts();
			String[] replayShortcuts = recorder.getReplayShortcuts();

			SWTAssertManager assertManager = recorder.getProcessor(SWTAssertManager.class);

			if (assertManager != null) {
				boolean assertRequest = assertManager.isShortcutRequest(event, assertShortcuts);
				boolean recordingRequest = assertManager.isShortcutRequest(event, recordShortcuts);
				boolean startRecordRequest = assertManager.isShortcutRequest(event, startRecordShortcuts);
				boolean stopRecordRequest = assertManager.isShortcutRequest(event, stopRecordShortcuts);
				boolean replayRequest = assertManager.isShortcutRequest(event, replayShortcuts);

				if (assertRequest || recordingRequest || startRecordRequest || stopRecordRequest || replayRequest) {
					assertManager.setFreeze(assertRequest, null);

					Type type = ProtocolFactory.eINSTANCE.createType();
					type.setState(event.stateMask);
					type.setCharacter(event.character);
					type.setCode(event.keyCode);
					getRecorder().safeExecuteCommand(type);

					event.detail = SWT.TRAVERSE_NONE;
					event.doit = false;
					return;
				}
			}
		}

		boolean radioWidgetIgnored = widget instanceof Button && (((Button) widget).getStyle() & SWT.RADIO) != 0
				&& (event.detail == SWT.TRAVERSE_ARROW_NEXT || event.detail == SWT.TRAVERSE_ARROW_PREVIOUS);

		boolean isEscapeChain = false;

		RecordedEvent lastRecorded = lastEvents.lastRecorded();
		if (lastRecorded != null) {

			isEscapeChain = event.detail == SWT.TRAVERSE_ESCAPE && lastRecorded.type == SWT.Traverse
					&& lastRecorded.event.doit == true && lastRecorded.event.detail == SWT.TRAVERSE_ESCAPE;

			// boolean isReturnChain = event.detail == SWT.TRAVERSE_RETURN
			// && lastRecorded.type == SWT.Traverse
			// && lastRecorded.event.doit == true
			// && lastRecorded.event.detail == SWT.TRAVERSE_RETURN;
		}

		if (event.doit && event.detail != SWT.TRAVERSE_NONE && event.detail != SWT.TRAVERSE_RETURN && !isEscapeChain
				&& !radioWidgetIgnored) {
			FindResult result = getLocator().findElement(widget, true, false, false);
			if (result != null) {
				ControlUIElement ctrl = new ControlUIElement(result.element, getRecorder());
				ctrl.traverse(event.detail, event.character);
			}
		}
	}

	public static int getMeta(int stateMask) {

		// The recognized modifiers keys are M1, M2, M3, M4, ALT, COMMAND, CTRL,
		// and SHIFT. The "M" modifier keys are a platform-independent way of
		// representing keys, and these are generally preferred. M1 is the
		// COMMAND key on MacOS X, and the CTRL key on most other platforms. M2
		// is the SHIFT key. M3 is the Option key on MacOS X, and the ALT key on
		// most other platforms. M4 is the CTRL key on MacOS X, and is undefined
		// on other platforms. (from Eclipse SDK Help)
		final int M1 = 1;
		final int M2 = 2;
		final int M3 = 4;
		final int M4 = 8;

		int meta = 0;

		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			if ((stateMask & SWT.COMMAND) != 0)
				meta |= M1;
			if ((stateMask & SWT.SHIFT) != 0)
				meta |= M2;
			if ((stateMask & SWT.ALT) != 0) // what is Option key on Mac, it's a
				// synonym ALT?
				meta |= M3;
			if ((stateMask & SWT.CTRL) != 0)
				meta |= M4;

		} else {
			if ((stateMask & SWT.CTRL) != 0)
				meta |= M1;
			if ((stateMask & SWT.SHIFT) != 0)
				meta |= M2;
			if ((stateMask & SWT.ALT) != 0)
				meta |= M3;
		}

		return meta;
	}

	@SuppressWarnings("unused")
	private String checkForActionCommand(int mask, int keyCode) {
		KeyStroke hotKey = KeyStroke.getInstance(mask, Character.toUpperCase(keyCode));
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		Binding binding = bindingService.getPerfectMatch(KeySequence.getInstance(hotKey));
		if (binding != null && binding.getParameterizedCommand() != null
				&& binding.getParameterizedCommand().getId() != null) {
			return binding.getParameterizedCommand().getId();
		}
		return null;
	}

	private void processMouseUp(Widget widget, Event event, RecordedEvent toRecording) {
		if (widget instanceof Table || widget instanceof Tree) {
			Viewer viewer = TeslaSWTAccess.getViewer((Control) widget);
			if (viewer != null && viewer instanceof ColumnViewer) {
				ColumnViewer cview = (ColumnViewer) viewer;
				CellEditor[] editors = cview.getCellEditors();
				// Collect click column
				ViewerCell cell = cview.getCell(new Point(event.x, event.y));
				if (cell != null && editors != null) {
					int index = cell.getColumnIndex();
					if (editors[index] != null && editors[index] instanceof CheckboxCellEditor) {
						recordCellAccess(widget, event, RecordCellAccessSource.MouseUp);
						return;
					}
				}
			}
			Listener[] listenersUp = widget.getListeners(SWT.MouseUp);
			if ((listenersUp != null && hasNonPlatformListeners(listenersUp, SWT.MouseUp)) || mouseDownRecorded) {
				mouseDownRecorded = false;
				recordCellAccess(widget, event, RecordCellAccessSource.MouseUp);
			}
		}
		// Listener[] listenersDown = widget.getListeners(SWT.MouseDown);
		// if ((listenersDown != null && containsNonPlatform(listenersDown,
		// SWT.MouseDown))) {
		recordTextSetFocus(widget, event.button);
		// }
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			if (widget instanceof Button) {
				if ((widget.getStyle() & SWT.CHECK) != 0) {
					FindResult result = getLocator().findElement(widget, false, false, true);
					if (result != null) {
						ControlUIElement e = new ControlUIElement(result.element, getRecorder());
						e.check(((Button) widget).getSelection());
					}
				} else if ((widget.getStyle() & SWT.RADIO) != 0) {
					FindResult result = getLocator().findElement(widget, false, false, true);
					if (result != null) {
						ControlUIElement e = new ControlUIElement(result.element, getRecorder());
						e.clickAndWait();
					}
				}
			}
		}

		// click on FilteredTree or Q7 SearchControl clear button
		if (widget instanceof Label && isSearchControlLabel((Label) widget)
				&& lastEvents.checkType(widget, SWT.MouseDown)) {
			FindResult result = getLocator().findElement(widget, true, false, false);
			if (result != null) {
				ControlUIElement e = new ControlUIElement(result.element, getRecorder());
				e.clickAndWait();
				beforeTextState = "";
			}
		}

		lastEvents.add(toRecording);
	}

	private boolean isSearchControlLabel(Label label) {
		if (label.getParent() instanceof Composite && label.getParent().getParent() != null
				&& label.getParent().getParent().getClass().getName().equals("org.eclipse.ui.dialogs.FilteredTree")) {
			return true;
		}
		if (label.getParent() instanceof Composite && label.getParent().getParent() != null && label.getParent()
				.getParent().getClass().getName().equals("org.eclipse.rcptt.ui.commons.SearchControl")) {
			return true;
		}
		return false;
	}

	private boolean mouseDownRecorded = false;
	private boolean recordClickInSeletion = false;

	private enum RecordCellAccessSource {
		MouseDown, MouseUp
	}

	private void recordCellAccess(Widget widget, Event event, RecordCellAccessSource source) {
		if (!(widget instanceof Table) && !(widget instanceof Tree)) {
			return;
		}

		Point pt = new Point(event.x, event.y);
		int row = TableTreeUtil.findRow(widget, pt);
		if (row == -1)
			return; // click on an icon or expansion sign

		boolean hasColumns = TableTreeUtil.getColumnCount(widget) > 0;

		FindResult result = getLocator().findElement(widget, true, false, false);

		// if (isDublicateCellClick(column, row)) {
		// return;
		// }
		ViewerUIElement ve = new ViewerUIElement(result.element, getRecorder());

		if (source == RecordCellAccessSource.MouseDown) {
			mouseDownRecorded = true;

			// record click if it has no columns and it is single selection or
			// multiple but selection is replaced
			recordClickInSeletion = !hasColumns && ((widget.getStyle() & SWT.MULTI) == 0
					|| ((event.stateMask & SWT.SHIFT) == 0 && (event.stateMask & SWT.CTRL) == 0));

			if (widget instanceof Tree && hasColumns) {
				List<String> path = Arrays.asList(Viewers.getPathByItem(TableTreeUtil.getItem(widget, row)));

				if (path.size() > 1) {
					ve.item(path.subList(0, path.size() - 1).toArray(new String[0])).expand();
				}
			}
		}

		if (hasColumns) {
			Point cell = TableTreeUtil.findCell(widget, pt);
			if (cell == null) {
				return;
			}
			row = cell.x;
			int column = cell.y;

			recordMouseEvent(ve.item(column, row), event,
					source == RecordCellAccessSource.MouseDown ? SWT.MouseDown : SWT.MouseUp);
			// ve.item(column, row).click();
		} else if (source == RecordCellAccessSource.MouseUp && recordClickInSeletion) {
			recordClickInSeletion = false;
			Object[] selection = TableTreeUtil.getSelection(widget);

			List<String> path;
			if (selection.length == 1)
				path = Arrays.asList(Viewers.getPathByItem(selection[0]));
			else
				path = Arrays.asList(Viewers.getPathByItem(TableTreeUtil.getItem(widget, row)));
			ve.item(path.toArray(new String[0])).click();

		}
	}

	private void recordTextSetFocus(Widget widget, int button) {
		if (widget instanceof Text) {
			// just record click and we are done

			String start = null;
			String end = null;

			Text t = (Text) widget;

			if (!t.getText().isEmpty()) {
				boolean multi = (t.getStyle() & SWT.MULTI) != 0;
				if (multi) {
					int iSelEnd = t.getCaretPosition();
					int iSelStart;
					Point selection = t.getSelection();
					if (iSelEnd != selection.y) {
						iSelStart = selection.y;
					} else {
						iSelStart = selection.x;
					}
					StringLines lines = new StringLines(t.getText());
					if (iSelStart != iSelEnd) {
						start = lines.calcLineColumn(iSelStart);
						end = lines.calcLineColumn(iSelEnd);
					} else {
						start = lines.calcLineColumn(iSelEnd);
					}
				} else {
					int iSelEnd = t.getCaretPosition();
					int iSelStart;
					Point point = t.getSelection();
					if (point != null) {
						if (iSelEnd != point.y) {
							iSelStart = point.y;
						} else {
							iSelStart = point.x;
						}
						if (iSelStart != iSelEnd) {
							start = Integer.toString(iSelStart + 1);
							end = Integer.toString(iSelEnd + 1);
						} else {
							start = Integer.toString(iSelEnd + 1);
						}

					}
				}
			}

			new ControlUIElement(getLocator().findElement(widget, true, false, false).element, getRecorder())
					.clickText(start, end, TeslaUtils.intToButtonName(button));
		}
	}

	private void recordMouseEvent(ControlUIElement element, Event event, int type) {
		MouseEvent me = ProtocolFactory.eINSTANCE.createMouseEvent();
		me.setButton(event.button);
		me.setCount(isDefaultCount(event) ? -1 : event.count);
		me.setElement(element.getElement());
		me.setKind(mouseToEventKind.get(event.type == 0 ? type : event.type));
		me.setStateMask(event.stateMask);
		me.setX(Integer.MIN_VALUE);
		me.setY(Integer.MIN_VALUE);
		getRecorder().safeExecuteCommand(me);
	}

	private static boolean isDefaultCount(Event event) {
		if (!defaultCounts.containsKey(event.type)) {
			return false;
		}
		return defaultCounts.get(event.type) == event.count;
	}

	private static final Map<Integer, MouseEventKind> mouseToEventKind = new HashMap<Integer, MouseEventKind>();
	static {
		mouseToEventKind.put(SWT.MouseDoubleClick, MouseEventKind.DOUBLE_CLICK);
		mouseToEventKind.put(SWT.MouseDown, MouseEventKind.DOWN);
		mouseToEventKind.put(SWT.MouseEnter, MouseEventKind.ENTER);
		mouseToEventKind.put(SWT.MouseExit, MouseEventKind.EXIT);
		mouseToEventKind.put(SWT.MouseMove, MouseEventKind.MOVE);
		mouseToEventKind.put(SWT.MouseUp, MouseEventKind.UP);
		mouseToEventKind.put(SWT.MouseHover, MouseEventKind.HOVER);
	}
	private static final Map<Integer, Integer> defaultCounts = new HashMap<Integer, Integer>();
	static {
		defaultCounts.put(SWT.MouseDoubleClick, 2);
		defaultCounts.put(SWT.MouseDown, 1);
		defaultCounts.put(SWT.MouseEnter, 0);
		defaultCounts.put(SWT.MouseExit, 0);
		defaultCounts.put(SWT.MouseMove, 0);
		defaultCounts.put(SWT.MouseUp, 1);
		defaultCounts.put(SWT.MouseHover, 0);
	}

	@SuppressWarnings("unused")
	private boolean isDublicateCellClick(int col, int row) {
		boolean itemClickCmdFound = false;
		for (int i = 1; i <= getRecorder().getContainer().size(); i++) {
			Command cmd = getRecorder().getContainer().getLastCommand(i);
			if (cmd instanceof Click && ElementKind.Item.toString().equals(((Click) cmd).getElement().getKind())) {
				itemClickCmdFound = true;
				continue;
			}
			if (cmd instanceof SelectCommand && itemClickCmdFound) {
				SelectData data = ((SelectCommand) cmd).getData();
				if (data.getIndexes() != null && data.getIndexes().size() == 2 && data.getIndexes().get(0) == col
						&& data.getIndexes().get(1) == row) {
					return true;
				}
			}
			if (!(cmd instanceof SetSelection || cmd instanceof SelectCommand)) {
				return false;
			}
		}
		return false;
	}

	private static boolean hasNonPlatformListeners(Listener[] listenersDown, int kind) {
		if (listenersDown == null) {
			return false;
		}
		for (Listener listener : listenersDown) {
			if (listener instanceof TypedListener) {
				Object eventListener = ((TypedListener) listener).getEventListener();
				if (eventListener != null) {
					String lName = eventListener.getClass().getName();
					if (kind == SWT.MouseUp
							&& lName.contains("org.eclipse.ui.internal.quickaccess.QuickAccessDialog")) {
						return true;
					}
					if (lName.contains("org.eclipse.swt") || lName.contains("org.eclipse.ui")
							|| lName.contains("org.eclipse.jface")) {
						continue;
					}
				}
				return true;
			}
			String lName = listener.getClass().getName();
			if (lName.contains("org.eclipse.swt") || lName.contains("org.eclipse.ui")
					|| lName.contains("org.eclipse.jface")) {
				continue;
			}
			return true;
		}
		return false;
	}

	private void processMouseDown(Widget widget, Event event, RecordedEvent toRecording) {
		if ((widget instanceof TabFolder || widget instanceof CTabFolder)) {
			// Check for workbench internal element click
			boolean skip = false;
			if (TeslaCore.isEclipse4() && widget instanceof CTabFolder) {
				// eclipse 4.x
				skip = EclipseWorkbenchProvider.getProvider().extractViewOrEditorControl((CTabFolder) widget) != null;
			}

			if (!skip) {
				FindResult result = getLocator().findElement(widget, true, false, false);
				if (result != null && result.realElement.getKind().is(ElementKind.TabFolder)) {
					String tabName = null;
					boolean closeAction = false;
					if (widget instanceof TabFolder) {
						TabItem[] items = ((TabFolder) widget).getItems();
						for (TabItem tabItem : items) {
							Rectangle bounds = tabItem.getBounds();
							if (bounds.contains(event.x, event.y)) {
								// Set selection on selected widget.
								tabName = PlayerTextUtils.removeAcceleratorFromText(tabItem.getText());
								break;
							}
						}

					} else if (widget instanceof CTabFolder) {
						CTabItem[] items = ((CTabFolder) widget).getItems();
						for (CTabItem tabItem : items) {
							Rectangle bounds = tabItem.getBounds();
							if (bounds.contains(event.x, event.y)) {
								// Check for tab item close rect and record
								// close command for tab item.
								// Set selection on selected widget.
								tabName = PlayerTextUtils.removeAcceleratorFromText(tabItem.getText());
								Rectangle rect = TeslaSWTAccess.getCTabItemCloseRect(tabItem);
								if (rect != null) {
									if (rect.contains(event.x, event.y)) {
										closeAction = true;
									}
								}
								break;
							}
						}
					}
					if (tabName != null) {
						if (!closeAction) {
							if (widget instanceof CTabFolder) {
								ProcessCTabFolderItemSelection((CTabFolder) widget, result, tabName);
							}
						} else {
							CompositeUIElement v = new CompositeUIElement(result.element, getRecorder());
							ControlUIElement tabItem = v.tabItem(tabName);
							tabItem.close();
						}
					}
				}
			}
		}
		recordTextSetFocus(widget, event.button);

		if (widget instanceof org.eclipse.swt.widgets.List && ((Control) widget).isVisible())
			// This forces the element mapping to be created so we can retrieve
			// it later when it becomes invisible
			getLocator().findElement(widget, false, false, false);

		Listener[] listenersDown = widget.getListeners(SWT.MouseDown);
		if ((listenersDown != null && hasNonPlatformListeners(listenersDown, SWT.MouseDown))) {
			recordCellAccess(widget, event, RecordCellAccessSource.MouseDown);
		}
		Listener[] listenersUp = widget.getListeners(SWT.MouseUp);
		if (widget instanceof Label && ((listenersDown != null && hasNonPlatformListeners(listenersDown, SWT.MouseDown))
				|| (listenersUp != null && hasNonPlatformListeners(listenersUp, SWT.MouseUp)))) {
			FindResult result = getLocator().findElement(widget, true, false, false);
			if (result != null) {
				ControlUIElement e = new ControlUIElement(result.element, getRecorder());
				e.clickAndWait(false, event.stateMask);
				beforeTextState = "";
			}
		}
		lastEvents.add(toRecording);
	}

	private void ProcessCTabFolderItemSelection(CTabFolder tabFolder, FindResult result, String tabName) {
		boolean needSelection = true;
		if (lastTabItemSelection.containsKey(tabFolder)) {
			String v = lastTabItemSelection.get(tabFolder);
			if (v.equals(tabName)) {
				needSelection = false;
			}
		}
		if (needSelection) {
			lastTabItemSelection.put(tabFolder, tabName);
			CompositeUIElement v = new CompositeUIElement(result.element, getRecorder());
			ControlUIElement tabItem = v.tabItem(tabName);
			tabItem.clickAndWait();
		}
	}

	private void processClose(Widget widget) {
		Command lastCmd = getRecorder().getContainer().getLast();
		if (lastCmd instanceof Type && ((Type) lastCmd).isTraverse()
				&& ((Type) lastCmd).getCode() == SWT.TRAVERSE_ESCAPE) {
			return;
		}
		FindResult result = getLocator().findElement(widget, true, false, false);
		if (result != null) {
			ControlUIElement e = new ControlUIElement(result.element, getRecorder());
			e.close();
		}
	}

	// private void processActivateEvent(Widget widget, RecordedEvent
	// toRecording) {
	// boolean radioWidget = widget instanceof Button
	// && (((Button) widget).getStyle() & SWT.RADIO) != 0;
	// if (radioWidget) {
	// if (Platform.getOS().equals(Platform.OS_WIN32)
	// || checkType(widget, SWT.MouseUp, SWT.MouseDown)) {
	// FindResult result = getLocator().findElement(widget, true,
	// false, false);
	// if (result != null) {
	// // System.out.println("RESULT is:" + result);
	// ControlUIElement e = new ControlUIElement(result.element,
	// getRecorder());
	// e.clickAndWait();
	// lastEvents.clear();
	// }
	// // System.out.println("_5_Clean Events" + index++);
	// }
	// }
	// if (widget instanceof ExpandableComposite) {
	// System.out.println("Activate");
	// }
	// lastEvents.add(toRecording);
	// }

	List<RecordedEvent> takeMenuEvents(List<RecordedEvent> lastEvents2) {
		List<RecordedEvent> result = new ArrayList<RecordedEvent>();
		for (RecordedEvent e : lastEvents2) {
			if (e.widget instanceof Menu || e.widget instanceof MenuItem) {
				if (!(e.widget.isDisposed())) {
					result.add(e);
				}
			}
		}
		// Filter Hides for menus.
		return result;
	}

	public synchronized void clear() {
		SWTRecordingHelper.getHelper().clear();
		SWTModelMapper.initializeExtensions(getRecorder().getProcessors(ISWTModelMapperExtension.class));
		this.lastEvents.clear();
		// this.varCounters.clear();
		// this.variables.clear();
		getLocator().cleanMenuSources();
		lastTabItemSelection.clear();
		// activeCellEditors.clear();
		inStyledTextAction = false;
		lastEvent = null;
		lastTraverseEvent = null;
		pressed.clear();
	}

	public void recordDragEvent(Event event) {
		// System.err.println("DND:" + event);
		if (getRecorder() == null) {
			return;
		}
		if (!getRecorder().hasListeners()) {
			return;
		}
		dragSupport.processDND(event);
	}

	public void recordSWTDialog(Dialog dialog, Object result) {
		if (getRecorder() == null) {
			return;
		}
		if (!getRecorder().hasListeners()) {
			return;
		}
		SetSWTDialogInfo info = ProtocolFactory.eINSTANCE.createSetSWTDialogInfo();
		if (dialog instanceof FileDialog) {
			info.setKind(SWTDialogKind.FILE_SELECTOR);
		} else if (dialog instanceof DirectoryDialog) {
			info.setKind(SWTDialogKind.FOLDER_SELECTOR);
		} else if (dialog instanceof MessageBox) {
			info.setKind(SWTDialogKind.MESSAGE_BOX);
		} else if (dialog instanceof FontDialog) {
			info.setKind(SWTDialogKind.FONT_DIALOG);
		} else if (dialog instanceof ColorDialog) {
			info.setKind(SWTDialogKind.COLOR);
		} else {
			return;
		}
		if (result instanceof String[]) {
			for (String currentStr : (String[]) result) {
				if (dialog instanceof FileDialog) {
					processFileDialogValue(info, currentStr);
				} else {
					info.getPath().add(currentStr);
				}
			}
		} else if (result instanceof String) {
			if (dialog instanceof DirectoryDialog) {
				processFileDialogValue(info, (String) result);
			} else {
				info.getPath().add((String) result);
			}
		} else if (result instanceof Integer) {
			info.getPath().add(Integer.toString((Integer) result));
		} else if (result instanceof FontData) {
			info.getPath().add(((FontData) result).toString());
		} else if (result instanceof RGB) {
			RGB rgb = (RGB) result;
			info.getPath().add("" + rgb.red + "," + rgb.green + "," + rgb.blue);
		} else {
			info.getPath().clear();
		}
		getRecorder().safeExecuteCommand(info);
	}

	private void processFileDialogValue(SetSWTDialogInfo info, String pathStr) {
		// Make workspace relative path, if file are from workspace
		// and
		// add "workspace://" prefix
		try {
			Class.forName("org.eclipse.core.resources.ResourcesPlugin");
		} catch (Throwable e) {
			info.getPath().add(pathStr);
			return;
		}
		try {
			IPath workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			File f = new File(pathStr);
			IPath path = new Path(f.getCanonicalPath());
			if (workspaceLocation.isPrefixOf(path)) {
				info.getPath().add("platform:/resource/"
						+ path.removeFirstSegments(workspaceLocation.segmentCount()).setDevice(null));
			} else {
				info.getPath().add(pathStr);
			}
		} catch (IOException e) {
			// TeslaCore.log(e);
		}
	}

	public void setFreeze(final boolean value, SetMode command) {
		// Display display = PlatformUI.getWorkbench().getDisplay();
		// if (display.isDisposed()) {
		// SWTEventManager.setFreeze(false);
		// return;
		// }
		// SWTEventManager.setIgnoreFreeze(player.getIgnored());
		// SWTEventManager.setFreeze(value);
	}

	public void recordStyledTextOffset(StyledText text) {
		if (inStyledTextAction) {
			return;
		}
		if (getRecorder() == null) {
			return;
		}
		if (!getRecorder().hasListeners()) {
			return;
		}
		if (!enabled) {
			enable();
		}
		// Check for extension to ignore recording against one of controls
		List<IRecordingProcessorExtension> list = getRecorder().getProcessors(IRecordingProcessorExtension.class);
		for (IRecordingProcessorExtension ext : list) {
			if (ext.isIgnored(text, SWT.Selection, null)) {
				return;
			}
		}
		if (isIgnored(text)) {
			return;
		}
		Context context = ContextManagement.currentContext();
		if (!context.contains("org.eclipse.swt.custom.StyledText", "doContent")
				&& !context.contains("org.eclipse.swt.custom.StyledText", "setContent")
				&& !context.contains("org.eclipse.swt.custom.StyledText", "reset")
				&& !context.contains("org.eclipse.swt.custom.StyledText", "handleKey")
				&& !context.contains("org.eclipse.swt.custom.StyledText", "setSelectionRange")
				&& !context.contains("org.eclipse.swt.custom.StyledText", "setSelection")
				&& !context.contains("org.eclipse.swt.custom.StyledText", "setStyleRanges")) {
			Command last = getRecorder().getContainer().getLast();
			if (last instanceof Type) {
				Type type = (Type) last;
				if (type.getCode() != null && type.getCode().intValue() == SWT.BS && type.getState() != null
						&& type.getState().intValue() == SWT.CTRL) {
					// This is Ctrl+Backspace. Ignore position change.
					return;
				}
				if (type.getCode() != null && type.getCode().intValue() == SWT.TAB && type.getState() != null
						&& type.getState().intValue() == 0) {
					// This is Tab. Ignore position change.
					return;
				}
			}
			FindResult element = getLocator().findElement(text, false, false, false);
			if (element != null) {
				TextUIElement textCtrl = new TextUIElement(element.element, getRecorder());
				int offset = text.getCaretOffset();
				int lineAtOffset = text.getLineAtOffset(offset);
				int offsetAtLine = text.getOffsetAtLine(lineAtOffset);
				if (!(last instanceof SetTextSelection
						&& EcoreUtil.equals(((SetTextSelection) last).getElement(), element.element))) {
					// Ignore after setSelection
					textCtrl.setTextOffset(lineAtOffset, offset - offsetAtLine);

				}
			}
		}
	}

	public void recordStyledTextActionAfter(StyledText text, int action) {
		inStyledTextAction = false;
	}

	public void recordStyledTextActionBefore(StyledText text, int action) {
		inStyledTextAction = true;
	}

	private TeslaRecorder recorder = null;

	public void initialize(final TeslaRecorder teslaRecorder) {
		this.recorder = teslaRecorder;
		this.dragSupport.setRecorder(teslaRecorder);
		// getLocator();// .initialize(teslaRecorder);
		teslaRecorder.addListener(this);
		final CommandPostProcessor pp = new CommandPostProcessor(teslaRecorder);
		teslaRecorder.addListener(new IRecordingModeListener() {

			public void changeMode(boolean inRecording) {
				if (inRecording) {
					teslaRecorder.addListener(pp);
				} else {
					teslaRecorder.removeListener(pp);
				}
			}
		});
	}

	public int getInitLevel() {
		return 10;
	};

	public void changeMode(boolean inRecording) {
		RecordingModeFeature.setRecordingModeActive(inRecording);
	}

	public void recordTabFolderEvent(Control tabControl, int eventId) {
		if (getRecorder() == null || !getRecorder().hasListeners()) {
			return;
		}

		FindResult result = null;

		if (tabControl instanceof CTabFolder) {
			CTabFolder tabFolder = (CTabFolder) tabControl;
			Widget w = EclipseWorkbenchProvider.getProvider().extractViewOrEditorControl(tabFolder);
			if (w != null) {
				Map<Control, SWTUIElement> references = EclipseWorkbenchProvider.getProvider()
						.getWorkbenchReference(getPlayer());
				SWTUIElement element = references.get(w);

				result = getLocator().findElement(element, false, false, true);
			}
		}

		if (result == null)
			result = getLocator().findElement(tabControl, false, false, true);

		if (result != null) {
			ControlUIElement ctrl = new ControlUIElement(result.element, getRecorder());
			switch (eventId) {
			case SWTEventManager.EVENT_TAB_MINIMIZE:
				ctrl.minimize();
				break;
			case SWTEventManager.EVENT_TAB_MAXIMIZE:
				ctrl.maximize();
				break;
			case SWTEventManager.EVENT_TAB_RESTORE:
				ctrl.restore();
				break;
			case SWTEventManager.EVENT_TAB_SHOW_LIST:
				ctrl.showTabList();
				break;
			}

		}
	}

	public void resetAssertSelection() {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IRecordingHelper getHelper() {
		return SWTRecordingHelper.getHelper();
	}

	public boolean isExclusiveEventHandle(Widget widget, int type, Event event) {
		return false;
	}

	public Event getCurrentEvent() {
		return currentEvent;
	}

	public void setCurrentEvent(Event event) {
		currentEvent = event;
	}

	@Override
	public void removeClosedShell(SWTUIElement wrappedShell) {
		SWTRecordingHelper.getHelper().remove(wrappedShell);		
	}
}
