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
package org.eclipse.rcptt.tesla.recording.jface.text;

import static org.eclipse.rcptt.util.ArraysUtil.contains;
import static org.eclipse.swt.SWT.MouseDoubleClick;
import static org.eclipse.swt.SWT.MouseUp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.rcptt.tesla.core.context.ContextManagement;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.protocol.ControlUIElement;
import org.eclipse.rcptt.tesla.core.protocol.ElementCommand;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.GenericElementKind;
import org.eclipse.rcptt.tesla.core.protocol.OpenDeclaration;
import org.eclipse.rcptt.tesla.core.protocol.RulerClick;
import org.eclipse.rcptt.tesla.core.protocol.RulerDoubleClick;
import org.eclipse.rcptt.tesla.core.protocol.SetCaretPosition;
import org.eclipse.rcptt.tesla.core.protocol.SetTextSelection2;
import org.eclipse.rcptt.tesla.core.protocol.TextUIElement;
import org.eclipse.rcptt.tesla.core.protocol.Type;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.SetMode;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.ui.player.AbstractSWTUIPlayerExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.FindResult;
import org.eclipse.rcptt.tesla.internal.ui.player.ISWTModelMapperExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.recording.aspects.IExtendedSWTEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.IWorkbenchTexteditorEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.SWTEventManager;
import org.eclipse.rcptt.tesla.recording.aspects.WorkbenchTexteditorEventManager;
import org.eclipse.rcptt.tesla.recording.aspects.jface.text.IJFaceTextEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.jface.text.JFaceTextEventManager;
import org.eclipse.rcptt.tesla.recording.core.IRecordingHelper;
import org.eclipse.rcptt.tesla.recording.core.IRecordingProcessor;
import org.eclipse.rcptt.tesla.recording.core.IRecordingProcessorExtension;
import org.eclipse.rcptt.tesla.recording.core.TeslaRecorder;
import org.eclipse.rcptt.tesla.recording.core.swt.BasicRecordingHelper.ElementEntry;
import org.eclipse.rcptt.tesla.recording.core.swt.SWTEventRecorder;
import org.eclipse.rcptt.tesla.recording.core.swt.SWTRecordingHelper;
import org.eclipse.rcptt.tesla.recording.core.swt.util.LastEvents;
import org.eclipse.rcptt.tesla.recording.core.swt.util.RecordedEvent;
import org.eclipse.rcptt.tesla.workbench.texteditor.TextEditorMapper;
import org.eclipse.rcptt.util.KeysAndButtons;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

public class WorkbenchTexteditorRecordingProcessor implements
		IRecordingProcessor, IWorkbenchTexteditorEventListener,
		IRecordingProcessorExtension, IExtendedSWTEventListener,
		IJFaceTextEventListener, ISWTModelMapperExtension {

	private TeslaRecorder recorder;
	private SWTEventRecorder eventRecorder;
	private boolean inStyledTextAction = false;
	private Event lastEvent = null;
	private final LastEvents lastEvents = new LastEvents();
	private final Set<String> pressed = new HashSet<String>();
	private Event currentEvent = null;

	// For compatibility
	final AtomicInteger useTextViewer = new AtomicInteger(0);

	public WorkbenchTexteditorRecordingProcessor() {

		processor = new AbstractSWTUIPlayerExtension() {
			public GenericElementKind getKind(Object w) {
				if (useTextViewer.get() == 1) {
					if (isTextEditorStyledText(w)) {
						return new GenericElementKind(ElementKind.TextViewer);
					}
					ElementKind rulerKind = getTextEditorRulerKind(w);
					if (rulerKind != null) {
						return new GenericElementKind(rulerKind);
					}
				}
				return null;
			}

			public String getRawText(SWTUIElement element) {
				return getTextEditorRulerText(element.unwrap());
			}
		};
		SWTUIPlayer.addExtension(processor);
	}

	public void clear() {
	}

	public void initialize(TeslaRecorder teslaRecorder) {
		this.recorder = teslaRecorder;
		WorkbenchTexteditorEventManager.addListener(this);
		SWTEventManager.addListener(this);
		JFaceTextEventManager.addListener(this);
	}

	public int getInitLevel() {
		return 1000;
	};

	public TeslaRecorder getRecorder() {
		return recorder;
	}

	public void setFreeze(boolean value, SetMode command) {
	}

	private synchronized SWTEventRecorder getSWTEventRecorder() {
		if (eventRecorder == null) {
			eventRecorder = recorder.getProcessor(SWTEventRecorder.class);
		}
		return eventRecorder;
	}

	public IRecordingHelper<?> getHelper() {
		return null;
	}

	public void resetAssertSelection() {
	}

	public boolean isIgnored(Object widget, int type, Object event) {
		return isNotCanvas(widget, type, event);
	}

	public boolean isNotCanvas(Object widget, int type, Object event) {
		return isTextEditorStyledText(widget)
				|| (getTextEditorRulerKind(widget) != null);
	}

	@Override
	public boolean isNotDraw2d(Object widget) {
		return false;
	}

	private static ElementKind getTextEditorRulerKind(Object widget) {
		if (widget instanceof Canvas) {
			List<IVerticalRuler> rulers = JFaceTextEventManager.getRulers();
			for (IVerticalRuler ruler : rulers) {
				if (widget.equals(ruler.getControl())) {
					if (ruler instanceof CompositeRuler) {
						return ElementKind.LeftRuler;
					} else if (ruler instanceof OverviewRuler) {
						return ElementKind.RightRuler;
					}
					return ElementKind.VerticalRuler;
				}
				if (ruler instanceof CompositeRuler) {
					Iterator<?> iterator = ((CompositeRuler) ruler)
							.getDecoratorIterator();
					while (iterator.hasNext()) {
						Object next = iterator.next();
						if (next instanceof IVerticalRulerColumn) {
							IVerticalRulerColumn column = (IVerticalRulerColumn) next;
							if (widget.equals(column.getControl())) {
								return ElementKind.VerticalColumn;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static String getTextEditorRulerText(Object widget) {
		if (widget instanceof Canvas) {
			List<IVerticalRuler> rulers = JFaceTextEventManager.getRulers();
			for (IVerticalRuler ruler : rulers) {
				if (widget.equals(ruler.getControl())) {
					if (ruler instanceof CompositeRuler) {
						return null;
					} else if (ruler instanceof OverviewRuler) {
						return null;
					}
					return null;
				}
				if (ruler instanceof CompositeRuler) {
					Iterator<?> iterator = ((CompositeRuler) ruler)
							.getDecoratorIterator();
					while (iterator.hasNext()) {
						Object next = iterator.next();
						if (next instanceof IVerticalRulerColumn) {
							IVerticalRulerColumn column = (IVerticalRulerColumn) next;
							if (widget.equals(column.getControl())) {
								return column.getClass().getSimpleName();
							}
						}
					}
				}
			}
		}
		return null;
	}

	private int getTextEditorRulerLine(Object widget, int y) {
		if (widget instanceof Canvas) {
			List<IVerticalRuler> rulers = JFaceTextEventManager.getRulers();
			for (IVerticalRuler ruler : rulers) {
				if (widget.equals(ruler.getControl())) {
					if (ruler instanceof CompositeRuler) {
						int line = ((CompositeRuler) ruler)
								.getLineOfLastMouseButtonActivity();
						return line == -1 ? -1 : line + 1;
					} else if (ruler instanceof OverviewRuler) {
						int line = ((OverviewRuler) ruler)
								.toDocumentLineNumber(y);
						return line == -1 ? -1 : line + 1;
					} else if (ruler instanceof VerticalRuler) {
						int line = ((VerticalRuler) ruler)
								.getLineOfLastMouseButtonActivity();
						return line == -1 ? -1 : line + 1;
					}
				}
				if (ruler instanceof CompositeRuler) {
					Iterator<?> iterator = ((CompositeRuler) ruler)
							.getDecoratorIterator();
					while (iterator.hasNext()) {
						Object next = iterator.next();
						if (next instanceof IVerticalRulerColumn
								&& next instanceof IVerticalRulerInfo) {
							IVerticalRulerColumn column = (IVerticalRulerColumn) next;
							if (widget.equals(column.getControl())) {
								int line = ((IVerticalRulerInfo) next)
										.getLineOfLastMouseButtonActivity();
								return line == -1 ? -1 : line + 1;
							}
						}
					}
				}
			}
		}
		return -1;
	}

	private static boolean isTextEditorStyledText(Object widget) {
		if (widget instanceof StyledText) {
			TextViewer viewer = JFaceTextEventManager
					.getViewer((StyledText) widget);
			if (viewer != null) {
				return true;
			}
		}
		return false;
	}

	public boolean isExclusiveEventHandle(Widget widget, int type, Event event) {
		return false;
	}

	public void recordEvent(Widget widget, int type, Event event) {
		if (getRecorder() == null) {
			return;
		}
		if (!getRecorder().hasListeners()) {
			return;
		}
		if (getSWTEventRecorder() == null
				|| getSWTEventRecorder().isIgnored(widget)) {
			return;
		}
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			if (event != null && lastEvent != null) {
				if (event.time == lastEvent.time
						&& event.widget == lastEvent.widget
						&& event.type == lastEvent.type) {
					// Same event captured twise
					// System.out.println("ALREADY_PROCESSED_EVENT");
					return;
				}
			}
			lastEvent = event;
		}
		if (isIgnored(widget, type, event)) {
			// This is our text editor event
			try {
				useTextViewer.set(1);
				cleanTextElementIfAlreadyUsed(widget);
				recordEvent_(widget, type, event);
			} finally {
				useTextViewer.set(0);
			}
		}
	}

	private Event lastTraverseEvent = null;

	protected void recordEvent_(Widget widget, int type, Event event) {
		if (type == DND.DragStart || type == DND.DragEnd
				|| type == DND.DragEnter || type == DND.DragOver
				|| type == DND.DragLeave || type == DND.Drop
				|| type == DND.DragOperationChanged || type == DND.DropAccept
				|| type == DND.DragSetData) {
			if (event.widget == null) {
				event.widget = widget;
			}
			event.type = type;
			recordDragEvent(event);
			return;
		}
		RecordedEvent toRecording = new RecordedEvent(getPlayer(), event, type, widget);

		Context ctx = ContextManagement.currentContext();
		// Filter not window proc contexts

		ElementKind rulerKind = getTextEditorRulerKind(widget);
		if (rulerKind != null) {
			if (type != SWT.MouseEnter && type != SWT.MouseExit) {
				FindResult canvas = getSWTEventRecorder().getLocator()
						.findElement(widget, false, false, false);
				if (canvas != null) {
					processRulerCanvas(widget, type, event, canvas);
				}
			}
		}

		if (getSWTEventRecorder().isNotNative(widget)) {
			return;
		}
		if ((type == SWT.Selection || type == SWT.DefaultSelection)) {
			processSelection(widget, type);
		} else if (type == SWT.Activate) {
			lastEvents.add(toRecording);
			// processActivateEvent(widget, toRecording);
		} else if (type == SWT.Deactivate) {
			lastEvents.add(toRecording);
		} else if (type == SWT.KeyDown) {
			if (ctx.contains(
					"com.xored.swt.internal.composer.GeckoEditor$KeyAdapter",
					"handleEvent")) {
				return; // this event is duplicate, see QS-882
			}
			addToPressed(event);
			processKeyDown(widget, event);
			lastEvents.add(toRecording);
		} else if (type == SWT.KeyUp) {
			if (ctx.contains(
					"com.xored.swt.internal.composer.GeckoEditor$KeyAdapter",
					"handleEvent")) {
				return; // this event is duplicate, see QS-882
			}
			String v = KeyStroke.getInstance(event.stateMask, event.keyCode)
					.toString().toLowerCase();
			String[] values = v.split("\\+");
			boolean contains = false;
			boolean containsAll = true;
			for (String val : values) {
				if (pressed.contains(val))
					contains = true;
				else
					containsAll = false;
			}
			// On MacOSX after press Enter in dialog with default selection,
			// no
			// KeyDown and Traverse event but unnecessary KeyUp
			boolean isEnterOnMac = Platform.getOS().equals(Platform.OS_MACOSX)
					&& (event.character == SWT.CR) && event.stateMask == 0;
			boolean fixTraverse = lastTraverseEvent != null
					&& lastTraverseEvent.stateMask == event.stateMask
					&& lastTraverseEvent.keyCode == event.keyCode;

			if (!contains && !isEnterOnMac || fixTraverse && !containsAll) {
				lastTraverseEvent = null;
				// This is just key-up event
				processKeyDown(widget, event);
			}

			for (String val : values) {
				pressed.remove(val);
			}

			lastEvents.add(toRecording);
			// }
		} else if (type == SWT.Traverse) {
			// Don't record traverse events for Browser - key pressing recorded
			// on KeyUp event
			if (!(widget instanceof Browser)) {
				if (isTextEditorStyledText(widget) && !event.doit) {
					// FindResult result = getSWTEventRecorder().getLocator()
					// .findElement(widget, true, false, false);
					// if (result != null) {
					// ControlUIElement ctrl = new ControlUIElement(
					// result.element, getRecorder());
					switch (event.detail) {
					case SWT.TRAVERSE_ARROW_NEXT:
					case SWT.TRAVERSE_ARROW_PREVIOUS:
						lastTraverseEvent = event;
						break;
					}
					// }
				} else {
					addToPressed(event);
					processTraverse(widget, event);
					lastEvents.add(toRecording);
				}
			}
		} else if (type == SWT.MouseDown) {
			processMouseDown(widget, event);
			lastEvents.add(toRecording);
		} else {
			lastEvents.add(toRecording);
		}
	}

	private static final int[] rulerEvents = new int[] { MouseUp,
			MouseDoubleClick };
	private AbstractSWTUIPlayerExtension processor;

	private void processRulerCanvas(Widget widget, int type, Event event,
			FindResult canvas) {

		if (!contains(rulerEvents, type) || canvas == null || event == null) {
			return;
		}

		ControlUIElement canvasCtrl = new ControlUIElement(canvas.element,
				getRecorder());

		int line = getTextEditorRulerLine(widget, event.y);
		if (line == -1) {
			return;
		}

		Command last = getRecorder().getContainer().getLast();
		Element lastElement = (last instanceof ElementCommand) ? ((ElementCommand) last)
				.getElement() : null;

		switch (type) {
		case SWT.MouseUp:
			if (last instanceof RulerDoubleClick
					&& EcoreUtil.equals(lastElement, canvas.element)) {
				return; // MouseUp corresponds to previous double click
			}
			canvasCtrl.RulerClick(line, event.button, event.stateMask
					& ~KeysAndButtons.getButtonMaskSafe(event.button));
			break;
		case SWT.MouseDoubleClick:
			if (last instanceof RulerClick
					&& EcoreUtil.equals(lastElement, canvas.element)) {
				getRecorder().removeLast(); // Prev recorded click was part of
											// doubleclick
			}
			canvasCtrl.RulerDoubleClick(line, event.button, event.stateMask);
			break;
		}
	}

	private SWTUIPlayer getPlayer() {
		return getSWTEventRecorder().getPlayer();
	}

	private void processSelection(Widget widget, int type) {
		if (SWTEventRecorder.isIgnoreSelection(widget, type)) {
			return;
		}
		try {
			if (!inStyledTextAction) {
				// Object this1 = TeslaSWTAccess.getThis(widget);

				FindResult result = getSWTEventRecorder().getLocator()
						.findElement(widget, true, false, false);
				if (result != null) {
					TextUIElement textCtrl = new TextUIElement(result.element,
							getRecorder());
					if (widget instanceof StyledText) {
						StyledText styledText = ((StyledText) widget);
						Point selection = styledText.getSelectionRange();
						if (selection.y != 0) {
							Command last = getRecorder().getContainer()
									.getLast();
							if (last instanceof SetCaretPosition) {
								SetCaretPosition offset = (SetCaretPosition) last;
								if (EcoreUtil.equals(offset.getElement(),
										result.element)) {
									getRecorder().removeLast();
								}
							}

							int start[] = getCursorOffset(styledText,
									selection.x);
							int end[] = getCursorOffset(styledText, selection.x
									+ selection.y);

							int caretOffset = styledText.getCaretOffset();
							if (caretOffset == selection.x) {
								textCtrl.setSelection2(end[0], end[1],
										start[0], start[1]);
							} else {
								textCtrl.setSelection2(start[0], start[1],
										end[0], end[1]);
							}
						}
					}
				}
			}
		} finally {
			lastEvents.clear();
		}
	}

	private void processTraverse(Widget widget, Event event) {
		getSWTEventRecorder().processTraverse(widget, event);
	}

	private void processKeyDown(Widget widget, Event event) {
		FindResult result = getSWTEventRecorder().getLocator().findElement(
				widget, true, false, false);
		if (result != null) {
			ControlUIElement ctrl = new ControlUIElement(result.element,
					getRecorder());
			if (!(event.keyCode == SWT.SHIFT || event.keyCode == SWT.CTRL
					|| event.keyCode == SWT.ALT || event.keyCode == SWT.COMMAND)) {
				if ((event.keyCode & SWT.KEYCODE_BIT) == 0
						&& event.stateMask == 0
						&& !Character.isISOControl(event.character)) {
					if (widget instanceof StyledText
							|| widget instanceof Browser
							|| widget.getListeners(SWT.KeyDown).length > 0
							|| widget.getListeners(SWT.KeyUp).length > 0) {
						ctrl.type(new String(new char[] { event.character }),
								widget instanceof Browser);
						getSWTEventRecorder().addToPressed(event);
					}
				} else {
					if (widget instanceof StyledText
							|| widget instanceof Browser) {
						if (event.stateMask == SWT.SHIFT
								&& !Character.isISOControl(event.character)) {
							ctrl.type(
									new String(new char[] { event.character }),
									widget instanceof Browser);
							getSWTEventRecorder().addToPressed(event);
						} else {
							ctrl.press(event.keyCode, event.stateMask,
									widget instanceof Browser, event.character,
									SWTEventRecorder.getMeta(event.stateMask));
							getSWTEventRecorder().addToPressed(event);
						}
					} else {
						ctrl.press(event.keyCode, event.stateMask,
								widget instanceof Browser, event.character,
								SWTEventRecorder.getMeta(event.stateMask));
						getSWTEventRecorder().addToPressed(event);
					}
					// }
				}
			}
			lastEvents.clear();
		}
		// }
	}

	private void addToPressed(Event event) {
		KeyStroke strk = KeyStroke.getInstance(event.stateMask, event.keyCode);
		String split = strk.toString().toLowerCase();
		String[] values = split.split("\\+");
		for (String val : values) {
			if (!pressed.contains(val))
				pressed.add(val);
		}
	}

	public void recordDragEvent(Event event) {
	}

	public void recordSWTDialog(Dialog dialog, Object result) {
	}

	public void recordStyledTextOffset(StyledText text) {
		try {
			useTextViewer.set(1);

			if (!isTextEditorStyledText(text))
				return;

			if (inStyledTextAction) {
				return;
			}
			if (getRecorder() == null) {
				return;
			}
			if (!getRecorder().hasListeners()) {
				return;
			}
			cleanTextElementIfAlreadyUsed(text);
			// Check for extension to ignore recording against one of controls
			if (!isIgnored(text, SWT.Selection, null)) {
				return;
			}
			if (getSWTEventRecorder().isIgnored(text)) {
				return;
			}
			Context context = ContextManagement.currentContext();
			if (!context.contains("org.eclipse.swt.custom.StyledText",
					"doContent")
					&& !context.contains("org.eclipse.swt.custom.StyledText",
							"setContent")
					&& !context.contains("org.eclipse.swt.custom.StyledText",
							"reset")
					&& !context.contains("org.eclipse.swt.custom.StyledText",
							"handleKey")
					&& !context.contains("org.eclipse.swt.custom.StyledText",
							"setSelectionRange")
					&& !context.contains("org.eclipse.swt.custom.StyledText",
							"setSelection")
					&& !context.contains("org.eclipse.swt.custom.StyledText",
							"setStyleRanges")
					&& !context.contains(
							"org.eclipse.ui.internal.keys.WorkbenchKeyboard",
							"press")) {
				Command last = getRecorder().getContainer().getLast();
				if (last instanceof Type) {
					Type type = (Type) last;
					if (type.getCode() != null
							&& type.getCode().intValue() == SWT.BS
							&& type.getState() != null
							&& type.getState().intValue() == SWT.CTRL) {
						// This is Ctrl+Backspace. Ignore position change.
						// return;
					}
					if (type.getCode() != null
							&& type.getCode().intValue() == SWT.TAB
							&& type.getState() != null
							&& type.getState().intValue() == 0) {
						// This is Tab. Ignore position change.
						// return;
					}
				}
				if (text.getText().length() > 0) {
					FindResult element = getSWTEventRecorder().getLocator()
							.findElement(text, false, false, false);
					if (element != null) {
						TextUIElement textCtrl = new TextUIElement(
								element.element, getRecorder());
						int[] result = getCursorOffset(text,
								text.getCaretOffset());

						if (!((last instanceof SetTextSelection2) && EcoreUtil
								.equals(((SetTextSelection2) last).getElement(),
										element.element))
								&& !((last instanceof OpenDeclaration) && EcoreUtil
										.equals(((OpenDeclaration) last)
												.getElement(), element.element))) {
							// Ignore after setSelection
							textCtrl.setTextCursor(result[0], result[1]);
						}
					}
				}
			}
		} finally {
			useTextViewer.set(0);
		}
	}

	private int[] getCursorOffset(StyledText text, int offset) {
		int result[] = null;
		try {
			TextViewer viewer = JFaceTextEventManager.getViewer(text);
			IDocument document = viewer.getDocument();
			int realOffset = viewer.widgetOffset2ModelOffset(offset);
			int lineAtOffset = document.getLineOfOffset(realOffset);
			int offsetAtLine = document.getLineOffset(lineAtOffset);
			int tabWidth = text.getTabs();
			int column = 0;
			for (int i = offsetAtLine; i < realOffset; i++) {
				if ('\t' == document.getChar(i))
					column += tabWidth
							- (tabWidth == 0 ? 0 : column % tabWidth);
				else
					column++;
			}
			result = new int[] { lineAtOffset + 1, column + 1 };
		} catch (BadLocationException e) {
			TeslaCore.log(e);
		}
		return result;
	}

	public void recordStyledTextActionBefore(StyledText text, int action) {
		inStyledTextAction = true;
	}

	public void recordStyledTextActionAfter(StyledText text, int action) {
		inStyledTextAction = false;
	}

	public void recordTabFolderEvent(Control tabControl, int eventId) {
	}

	public void showContentAssist(Object control, boolean autoActivation) {

	}

	public void activateInfomationWindow(Widget widget, Rectangle subjectArea) {
		try {
			useTextViewer.set(1);
			if (!isIgnored(widget, SWT.Selection, null)) {
				return;
			}
			Context ctx = ContextManagement.currentContext();
			if (ctx.contains("org.eclipse.ui.internal.keys.WorkbenchKeyboard",
					"press")) {
				return;
			}
			cleanTextElementIfAlreadyUsed(widget);

			FindResult findResult = getSWTEventRecorder().getLocator()
					.findElement(widget, false, false, true);
			if (findResult != null) {
				int stateMask = currentEvent != null ? currentEvent.stateMask
						: 0;
				if (widget instanceof StyledText) {
					StyledText text = (StyledText) widget;
					Point center = new Point(subjectArea.x + subjectArea.width
							/ 2, subjectArea.y + subjectArea.height / 2);
					int offset = ((StyledText) widget)
							.getOffsetAtPoint(center);

					int[] result = getCursorOffset(text, offset);

					TextUIElement textElement = new TextUIElement(
							findResult.element, getRecorder());
					textElement.hoverAtText(result[0], result[1], stateMask);
				} else {
					int y = currentEvent != null ? currentEvent.y : 0;
					if (currentEvent != null
							&& currentEvent.type == SWT.MouseHover
							&& getTextEditorRulerKind(currentEvent.widget) != null) {
						widget = currentEvent.widget;
						findResult = getSWTEventRecorder().getLocator()
								.findElement(widget, false, false, true);
						if (findResult == null) {
							return;
						}
					}
					int line = getTextEditorRulerLine(widget, y);
					if (line != -1) {
						ControlUIElement rulerCtrl = new ControlUIElement(
								findResult.element, getRecorder());
						rulerCtrl.RulerHover(line, stateMask);
					}
				}
			}
		} finally {
			useTextViewer.set(0);
		}
	}

	private void cleanTextElementIfAlreadyUsed(Widget widget) {
		SWTUIElement uiElement = getSWTEventRecorder().getPlayer().wrap(widget);
		ElementEntry entry = SWTRecordingHelper.getHelper().get(uiElement);
		if (entry != null && entry.getElement() != null
				&& entry.getElement().getKind().equals(ElementKind.Text.name())) {
			SWTRecordingHelper.getHelper().remove(uiElement);
		}
	}

	private void processMouseDown(Widget widget, Event event) {

		if (!isNotCanvas(widget, SWT.Selection, null)) {
			return;
		}

		if ((event.stateMask & SWT.MOD1) != 0) {
			FindResult findResult = getSWTEventRecorder().getLocator()
					.findElement(widget, false, false, true);
			if (widget instanceof StyledText && findResult != null) {
				StyledText text = (StyledText) widget;
				int offset = text.getOffsetAtPoint(new Point(event.x,
						event.y));
				int[] position = getCursorOffset(text, offset);
				TextUIElement textElement = new TextUIElement(
						findResult.element, getRecorder());
				textElement.setTextCursor(position[0], position[1]);
				textElement.openDeclaration();
			}
		}
	}

	public void setCurrentEvent(Event event) {
		currentEvent = event;
	}

	public org.eclipse.rcptt.tesla.core.ui.Widget mapExtraValues(SWTUIElement element,
			org.eclipse.rcptt.tesla.core.ui.Widget result) {
		return TextEditorMapper.mapExtraValues(element, result);
	}

	@Override
	public boolean isPartOfParent(Object widget, Object parent) {
		return false;
	}

	@Override
	public void removeClosedShell(SWTUIElement wrappedShell) {		
	}
}
