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
package org.eclipse.rcptt.tesla.workbench.texteditor;

import static java.lang.Integer.parseInt;
import static org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor.failResponse;
import static org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor.okResponse;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.ClickText;
import org.eclipse.rcptt.tesla.core.protocol.DoubleClickText;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.GenericElementKind;
import org.eclipse.rcptt.tesla.core.protocol.HoverAtText;
import org.eclipse.rcptt.tesla.core.protocol.IElementProcessorMapper;
import org.eclipse.rcptt.tesla.core.protocol.OpenDeclaration;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolFactory;
import org.eclipse.rcptt.tesla.core.protocol.RulerClick;
import org.eclipse.rcptt.tesla.core.protocol.RulerDoubleClick;
import org.eclipse.rcptt.tesla.core.protocol.RulerHover;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectResponse;
import org.eclipse.rcptt.tesla.core.protocol.SetCaretPosition;
import org.eclipse.rcptt.tesla.core.protocol.SetCursorOffset;
import org.eclipse.rcptt.tesla.core.protocol.SetTextSelection2;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawFactory;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.core.protocol.raw.ResponseStatus;
import org.eclipse.rcptt.tesla.core.utils.TeslaUtils;
import org.eclipse.rcptt.tesla.ecl.model.SelectRange;
import org.eclipse.rcptt.tesla.internal.core.AbstractTeslaClient;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.core.processing.ElementGenerator;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor;
import org.eclipse.rcptt.tesla.internal.ui.player.AbstractSWTUIPlayerExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.ISWTModelMapperExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.ISWTUIPlayerExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerSelectionFilter;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTEvents;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor;
import org.eclipse.rcptt.tesla.jface.text.JFaceTextManager;
import org.eclipse.rcptt.tesla.jface.text.JFaceTextProcessor;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.rcptt.util.swt.Events;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

public class WorkbenchTexteditorProcessor implements ITeslaCommandProcessor,
		ISWTModelMapperExtension {
	private AbstractTeslaClient client;
	private SWTUIProcessor swtUIProcessor;
	private final AtomicInteger useTextViewer = new AtomicInteger(0);

	// private String id;

	private ISWTUIPlayerExtension extension = new AbstractSWTUIPlayerExtension() {
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

		public SWTUIElement select(SWTUIPlayer swtuiPlayer,
				PlayerSelectionFilter f) {
			if (f.kind.is(ElementKind.TextViewer)) {
				return swtuiPlayer.selectWidget(f, StyledText.class);
			}
			if (f.kind.in(ElementKind.LeftRuler, ElementKind.RightRuler,
					ElementKind.VerticalRuler, ElementKind.VerticalColumn)) {
				return swtuiPlayer.selectWidget(f, Canvas.class);
			}
			return null;
		}

		public String getRawText(SWTUIElement element) {
			return getTextEditorRulerText(element.unwrap());
		}
	};

	public WorkbenchTexteditorProcessor() {
		SWTUIPlayer.addExtension(extension);
	}

	@Override
	public int getPriority() {
		return 25;
	}

	private static ElementKind getTextEditorRulerKind(Object widget) {
		if (widget instanceof Canvas) {
			List<IVerticalRuler> rulers = JFaceTextManager.getRulers();
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
					@SuppressWarnings("rawtypes")
					Iterator iterator = ((CompositeRuler) ruler)
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
			List<IVerticalRuler> rulers = JFaceTextManager.getRulers();
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
					@SuppressWarnings("rawtypes")
					Iterator iterator = ((CompositeRuler) ruler)
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

	/**
	 *
	 * @param widget
	 * @return Ruler object corresponding to given widget. <code>null</code> if
	 *         widget is not a canvas or ruler cannot be found
	 */
	private static IVerticalRuler getTextEditorRuler(Object widget) {
		if (widget instanceof Canvas) {
			List<IVerticalRuler> rulers = JFaceTextManager.getRulers();
			for (IVerticalRuler ruler : rulers) {
				if (widget.equals(ruler.getControl())) {
					return ruler;
				}
				if (ruler instanceof CompositeRuler) {
					@SuppressWarnings("rawtypes")
					Iterator iterator = ((CompositeRuler) ruler)
							.getDecoratorIterator();
					while (iterator.hasNext()) {
						Object next = iterator.next();
						if (next instanceof IVerticalRulerColumn) {
							IVerticalRulerColumn column = (IVerticalRulerColumn) next;
							if (widget.equals(column.getControl())) {
								return ruler;
							}
						}
					}
				}
			}
		}
		return null;
	}

	protected boolean isTextEditorStyledText(Object widget) {
		if (widget instanceof StyledText) {
			TextViewer viewer = JFaceTextManager.getViewer((StyledText) widget);
			if (viewer != null) {
				return true;
			}
		}
		return false;
	}

	public String getFeatureID() {
		return "swt.workbench.texteditor";
	}

	public boolean isSelectorSupported(String kind) {
		if (kind.equals(ElementKind.TextViewer.name())) {
			return true;
		}
		if (kind.equals(ElementKind.LeftRuler.name())
				|| kind.equals(ElementKind.RightRuler.name())
				|| kind.equals(ElementKind.VerticalRuler.name())
				|| kind.equals(ElementKind.VerticalColumn.name())) {
			return true;
		}
		return false;
	}

	public SelectResponse select(SelectCommand cmd, ElementGenerator generator,
			IElementProcessorMapper mapper) {
		try {
			useTextViewer.set(1);
			return getSWTUIProcessor().select(cmd, generator, mapper);
		} finally {
			useTextViewer.set(0);
		}
	}

	public boolean isCommandSupported(Command cmd) {
		if (cmd instanceof SetCaretPosition || cmd instanceof SelectRange
				|| cmd instanceof HoverAtText || cmd instanceof OpenDeclaration
				|| cmd instanceof RulerClick || cmd instanceof RulerDoubleClick
				|| cmd instanceof RulerHover) {
			return true;
		}

		return getSWTUIProcessor().isCommandSupported(cmd);
	}

	private SWTUIProcessor getSWTUIProcessor() {
		if (swtUIProcessor == null) {
			swtUIProcessor = client.getProcessor(SWTUIProcessor.class);
		}
		return swtUIProcessor;
	}

	public Response executeCommand(Command command,
			IElementProcessorMapper mapper) {
		if (command instanceof SetCursorOffset) {
			return handleSetCursorOffset((SetCursorOffset) command);
		} else if (command instanceof SetTextSelection2) {
			return handleSetTextSelection((SetTextSelection2) command);
		} else if (command instanceof HoverAtText) {
			return handleHoverAtText((HoverAtText) command);
		} else if (command instanceof OpenDeclaration) {
			return handleOpenDeclaration((OpenDeclaration) command);
		} else if (command instanceof RulerClick) {
			return handleRulerClick((RulerClick) command);
		} else if (command instanceof RulerDoubleClick) {
			return handleRulerDoubleClick((RulerDoubleClick) command);
		} else if (command instanceof RulerHover) {
			return handleRulerHover((RulerHover) command);
		} else if (command instanceof ClickText) {
			return handleClickText((ClickText) command);
		} else if (command instanceof DoubleClickText) {
			return handleDoubleClickText((DoubleClickText) command);
		}

		Response result = getSWTUIProcessor().executeCommand(command, mapper);
		if (result == null) {
			result = client.getProcessor(JFaceTextProcessor.class)
					.executeCommand(command, mapper);
		}
		return result;
	}

	private static final Pattern clickText = Pattern
			.compile("(\\d+)(:(\\d+))?");

	/**
	 * x is line, y is column. If there is only one digit then line is set to 1
	 *
	 * @param str
	 * @return
	 */
	private static Point parseLineColumnCoords(String str) {
		Matcher m = clickText.matcher(str);
		if (!m.matches()) {
			return null;
		}
		if (m.group(3) == null) {
			return new Point(1, parseInt(str));
		}
		return new Point(parseInt(m.group(1)), parseInt(m.group(3)));
	}

	private Response handleClickText(ClickText command) {
		String start = command.getStart();
		String end = command.getEnd();
		Element element = command.getElement();
		boolean isRange = end != null && end.length() > 0;

		Point startPoint = parseLineColumnCoords(start);
		Point endPoint = null;
		if (startPoint == null) {
			return failResponse("Cannot parse position " + start);
		}

		if (isRange) {
			endPoint = parseLineColumnCoords(end);
			if (endPoint == null) {
				return failResponse("Cannot parse position " + end);
			}
			SetTextSelection2 cmd = ProtocolFactory.eINSTANCE
					.createSetTextSelection2();
			cmd.setElement(element);
			cmd.setBlockMode(false);
			cmd.setEndLine(endPoint.x);
			cmd.setEndOffset(endPoint.y);
			cmd.setStartLine(startPoint.x);
			cmd.setEndLine(startPoint.y);
			return handleSetTextSelection(cmd);
		}

		final SWTUIElement widget = getSWTUIProcessor().getMapper().get(
				command.getElement());
		final Widget control = PlayerWrapUtils.unwrapWidget(widget);
		if (!(control instanceof StyledText)) {
			return failResponse("Control is not a styled text");
		}

		final StyledText styledText = (StyledText) control;
		final int visibleOffset = getVisibleCaretOffset(styledText,
				startPoint.x, startPoint.y);

		if (visibleOffset == -1) {
			return failResponse("Cannot click text at position %d:%d - it is either "
					+ "hidden, or out of document bounds");

		}

		getPlayer().exec("Click text", new Runnable() {
			public void run() {
				int line = styledText.getLineAtOffset(visibleOffset);
				styledText.setTopIndex(line);

				Point point = styledText.getLocationAtOffset(visibleOffset);
				SWTEvents events = getPlayer().getEvents();
				events.sendFocus(styledText);
				events.sendAll(styledText, Events.createClick(point));
				events.sendUnfocus(styledText);
			}
		});

		return okResponse();
	}

	private Response handleDoubleClickText(DoubleClickText command) {
		String pos = command.getPosition();
		final int button = TeslaUtils.buttonNameToInt(command.getButton());
		Element element = command.getElement();

		Point lineCol = parseLineColumnCoords(pos);
		if (lineCol == null)
			return failResponse("Cannot parse position " + pos);

		final SWTUIElement widget = getSWTUIProcessor().getMapper()
				.get(element);
		final Widget control = PlayerWrapUtils.unwrapWidget(widget);
		if (!(control instanceof StyledText)) {
			return failResponse("Control is not a styled text");
		}

		final StyledText styledText = (StyledText) control;
		final int visibleOffset = getVisibleCaretOffset(styledText, lineCol.x,
				lineCol.y);

		if (visibleOffset == -1)
			return failResponse("Cannot double-click text at position %d:%d - it is either "
					+ "hidden, or out of document bounds");

		getPlayer().exec("Double-click text", new Runnable() {
			public void run() {
				int line = styledText.getLineAtOffset(visibleOffset);
				styledText.setTopIndex(line);

				Point point = styledText.getLocationAtOffset(visibleOffset);
				SWTEvents events = getPlayer().getEvents();
				events.sendFocus(styledText);
				events.sendAll(styledText, Events.createDoubleClick(button,
						Events.EMPTY_MASK, point.x, point.y));
				events.sendUnfocus(styledText);
			}
		});

		return okResponse();
	}

	private Response handleRulerClick(final RulerClick command) {
		return sendRulerEvents(command.getElement(), command.getLine(),
				"Click ruler", Events.createClick(command.getButton(),
						command.getStateMask(), 0, 0));
	}

	/**
	 * Sends events to a ruler on a given line described by element. The
	 * following event fields are set automatically prior to sending:
	 * <ul>
	 * <li>{@link Event#widget}</li>
	 * <li>{@link Event#display}</li>
	 * <li>{@link Event#time}</li>
	 * <li>{@link Event#x}</li>
	 * <li>{@link Event#y}</li>
	 * </ul>
	 * Before sending, sets
	 *
	 */
	private Response sendRulerEvents(Element element, final int line,
			String operationName, final Event... events) {
		Response result = ProtocolFactory.eINSTANCE.createBooleanResponse();
		final SWTUIElement widget = getSWTUIProcessor().getMapper()
				.get(element);
		final Widget control = PlayerWrapUtils.unwrapWidget(widget);
		final IVerticalRuler ruler = getTextEditorRuler(control);
		if (ruler == null) {
			return result;
		}

		final StyledText styledText = JFaceTextManager.getViewer(ruler)
				.getTextWidget();
		final int x = ((Canvas) control).getBounds().width / 2;
		final int y;
		try {
			y = getYCoordFromLine(styledText, line, ruler);
		} catch (CoreException e) {
			result.setStatus(ResponseStatus.FAILED);
			result.setMessage(e.getMessage());
			return result;
		}

		getPlayer().exec(operationName, new Runnable() {

			public void run() {
				SWTUIPlayer player = getPlayer();
				Display display = player.getDisplay();
				SWTEvents eventSender = player.getEvents();

				player.addMouseWidgetInfo(control, x, y);
				styledText.forceFocus();
				try {
					ShellUtilsProvider.getShellUtils().forceActive(styledText.getShell());
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
				while (display.readAndDispatch())
					;
				eventSender.sendFocus(control);
				for (Event event : events) {
					event.widget = control;
					event.display = display;
					event.time = (int) System.currentTimeMillis();
					event.x = x;
					event.y = y;
					eventSender.sendEvent(control, event);
				}
				// eventSender.sendUnfocus(control);
			}
		});
		return result;
	}

	private Response handleRulerDoubleClick(final RulerDoubleClick command) {
		return sendRulerEvents(
				command.getElement(),
				command.getLine(),
				"Double-click ruler",
				Events.createDoubleClick(command.getButton(),
						command.getStateMask(), 0, 0));
	}

	private Response handleRulerHover(final RulerHover command) {
		Event mouseMove = new Event();
		mouseMove.stateMask = command.getStateMask();
		mouseMove.type = SWT.MouseMove;

		Event mouseHover = SWTEvents.clone(mouseMove);
		mouseHover.type = SWT.MouseHover;

		return sendRulerEvents(command.getElement(), command.getLine(),
				"Hover ruler", mouseMove, mouseHover);
	}

	private static IStatus createInvalidRulerLineErr(int line) {
		return new Status(
				IStatus.ERROR,
				WorkbenchTexteditorActivator.PLUGIN_ID,
				String.format(
						"Line %d is not present on ruler. Probably it is folded or removed?",
						line));
	}

	private int getYCoordFromLine(StyledText text, int line,
			IVerticalRuler ruler) throws CoreException {
		if (ruler instanceof OverviewRuler) {
			OverviewRuler overviewRuler = (OverviewRuler) ruler;
			for (int y = 0; y < overviewRuler.getControl().getBounds().height; y++) {
				int lineAtPixel = overviewRuler.toDocumentLineNumber(y) + 1;
				if (lineAtPixel == line) {
					return y;
				}
				if (lineAtPixel > line) {
					break; // no need to iterate further
				}
			}

			throw new CoreException(createInvalidRulerLineErr(line));
		}
		int offset = getVisibleCaretOffset(text, line, 1);
		if (offset == -1) {
			throw new CoreException(createInvalidRulerLineErr(line));
		}
		text.setTopIndex(text.getLineAtOffset(offset));
		return text.getLocationAtOffset(offset).y + text.getLineHeight() / 2;
	}

	private Response handleOpenDeclaration(OpenDeclaration command) {
		final Element element = command.getElement();
		final SWTUIElement widget = getSWTUIProcessor().getMapper()
				.get(element);
		if (widget != null) {
			final Widget control = PlayerWrapUtils.unwrapWidget(widget);
			if (control instanceof StyledText) {
				getPlayer().exec("Open declaration", new Runnable() {
					public void run() {
						StyledText styledText = (StyledText) control;
						int currentOffset = styledText.getCaretOffset();
						int line = styledText.getLineAtOffset(currentOffset);
						styledText.setTopIndex(line);
						Point point = styledText
								.getLocationAtOffset(currentOffset);
						SWTEvents events = getPlayer().getEvents();
						final Event e = events.createEvent(styledText);
						e.x = point.x;
						e.y = point.y;
						getPlayer().addMouseWidgetInfo(styledText, e.x, e.y);
						try {
							ShellUtilsProvider.getShellUtils().forceActive(styledText.getShell());
						} catch (CoreException exc) {
							throw new RuntimeException(exc);
						}
						styledText.forceFocus();
						e.stateMask = SWT.MOD1;
						e.count = 1;
						e.type = SWT.MouseMove;
						events.sendEvent(styledText, e);
						e.type = SWT.MouseHover;
						events.sendEvent(styledText, e);
						e.button = 1;
						e.type = SWT.MouseDown;
						events.sendEvent(styledText, e);
						events.sendEvent(styledText, SWT.Selection);
						e.type = SWT.MouseUp;
						events.sendEvent(styledText, e);
					}
				});
			}
		}
		return ProtocolFactory.eINSTANCE.createBooleanResponse();
	}

	private Response handleHoverAtText(final HoverAtText command) {
		Response result = ProtocolFactory.eINSTANCE.createBooleanResponse();

		final SWTUIElement widget = getSWTUIProcessor().getMapper().get(
				command.getElement());
		final Widget control = PlayerWrapUtils.unwrapWidget(widget);
		if (!(control instanceof StyledText)) {
			return result;
		}

		final StyledText styledText = (StyledText) control;
		final int visibleOffset = getVisibleCaretOffset(styledText,
				command.getLine(), command.getOffset());
		if (visibleOffset == -1) {
			result.setStatus(ResponseStatus.FAILED);
			result.setMessage(String.format(
					"Cannot hover text at position %d:%d - it is either "
							+ "hidden, or out of document bounds",
					command.getLine(), command.getOffset()));
			return result;
		}
		getPlayer().exec("Hover at text offset", new Runnable() {
			public void run() {
				int line = styledText.getLineAtOffset(visibleOffset);
				styledText.setTopIndex(line);
				Point point = styledText.getLocationAtOffset(visibleOffset);
				SWTEvents events = getPlayer().getEvents();
				final Event e = events.createEvent(styledText);
				e.x = point.x;
				e.y = point.y;
				getPlayer().addMouseWidgetInfo(styledText, e.x, e.y);
				events.sendEvent(styledText.getShell(), SWT.Deactivate);
				try {
					ShellUtilsProvider.getShellUtils().forceActive(styledText.getShell());
				} catch (CoreException exc) {
					throw new RuntimeException(exc);
				}
				styledText.forceFocus();
				e.stateMask = command.getStateMask();
				e.type = SWT.MouseMove;
				events.sendEvent(styledText, e);
				e.type = SWT.MouseHover;
				events.sendEvent(styledText, e);
			}
		});
		return result;
	}

	protected Response handleSetTextSelection(final SetTextSelection2 command) {
		final SWTUIElement element = getSWTUIProcessor().getMapper().get(
				command.getElement());
		Response response = RawFactory.eINSTANCE.createResponse();
		if (element == null) {
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}

		getPlayer().exec("set text selection", new Runnable() {

			public void run() {

				Widget rawWidget = element.widget;

				int startOffset = getVisibleCaretOffset((StyledText) rawWidget,
						command.getStartLine(), command.getStartOffset());
				int endOffset = getVisibleCaretOffset((StyledText) rawWidget,
						command.getEndLine(), command.getEndOffset());

				StyledText text = (StyledText) rawWidget;
				text.forceFocus();
				// text.setCaretOffset(end);
				text.setSelection(startOffset, endOffset);
				if (text.getAccessible() != null) {
					text.getAccessible().textSelectionChanged();
				}
				Event event = new Event();
				event.x = startOffset;
				event.y = endOffset - startOffset;
				event.type = SWT.Selection;
				getPlayer().getEvents().sendEvent(text, event);
			}
		});

		return response;
	}

	private Response handleSetCursorOffset(final SetCursorOffset command) {
		final Element element = command.getElement();
		final SWTUIElement widget = getSWTUIProcessor().getMapper()
				.get(element);
		if (widget != null) {
			final Widget control = PlayerWrapUtils.unwrapWidget(widget);
			if (control instanceof StyledText) {
				getPlayer().exec("Set text offset", new Runnable() {
					public void run() {
						int offset = command.getOffset();
						int line = command.getLine();

						int caretOffset = getVisibleCaretOffset(
								(StyledText) control, line, offset);

						StyledText styledText = (StyledText) control;
						styledText.setFocus();// forced focus (see QS-910)
						// Point selectionRange = styledText.getSelection();
						styledText.setCaretOffset(caretOffset);
						if (styledText.getAccessible() != null) {
							styledText.getAccessible().textCaretMoved(
									caretOffset);
						}
						// styledText.setSelection(selectionRange.x,
						// selectionRange.y);
					}
				});
			}
		}
		return ProtocolFactory.eINSTANCE.createBooleanResponse();
	}

	/**
	 * Transforms user-friendly coords into coords corresponding to styled text
	 * control
	 *
	 * @param control
	 *            Styled text
	 * @param line
	 *            user-friendly line
	 * @param offset
	 *            user-frindely offset
	 * @return offset inside control or -1 if given position is hidden or out of
	 *         bounds
	 */
	protected int getVisibleCaretOffset(StyledText control, int line, int offset) {

		TextViewer viewer = JFaceTextManager.getViewer(control);

		IDocument document = viewer.getDocument();
		try {
			int lineOffset = document.getLineOffset(line - 1);
			int len = document.getLineLength(line - 1);
			document.get(lineOffset, len);
			int tabs = control.getTabs();
			int column = 0;
			for (int i = lineOffset; i <= lineOffset + len; i++) {
				if (column == offset - 1) {
					column = i - lineOffset;
					break;
				}
				if ('\t' == document.getChar(i))
					column += tabs - (tabs == 0 ? 0 : column % tabs);
				else
					column++;
			}
			return viewer.modelOffset2WidgetOffset(lineOffset + column);
		} catch (BadLocationException e) {
			// no need to log - it is fine
			return -1;
		} catch (Exception e) {
			TeslaCore.log(e);
			return -1;
		}
	}

	private SWTUIPlayer getPlayer() {
		return getSWTUIProcessor().getPlayer();
	}

	public PreExecuteStatus preExecute(Command command,
			PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		return null;
	}

	public void initialize(AbstractTeslaClient client, String id) {
		this.client = client;
	}

	public void postSelect(Element element, IElementProcessorMapper mapper) {
	}

	public boolean isInactivityRequired() {
		return false;
	}

	public boolean canProceed(Context context, Q7WaitInfoRoot info) {
		List<AbstractHoverInformationControlManager> managers = JFaceTextManager.isSomeHoverManagerActive();
		for (AbstractHoverInformationControlManager mgr : managers) {
			Q7WaitUtils.updateInfo("text.hover", mgr.getClass().getName(), info);
		}
		return managers.isEmpty();
	}

	public void clean() {
	}

	public void terminate() {
		SWTUIPlayer.removeExtension(extension);
	}

	public void checkHang() {
	}

	public void collectInformation(AdvancedInformation information,
			Command lastCommand) {
	}

	public void notifyUI() {
	}

	@SuppressWarnings("unused")
	private int[] getCursorOffset(StyledText text, int offset) {
		int result[] = null;
		try {
			TextViewer viewer = JFaceTextManager.getViewer(text);
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

	public org.eclipse.rcptt.tesla.core.ui.Widget mapExtraValues(SWTUIElement element,
			org.eclipse.rcptt.tesla.core.ui.Widget result) {
		return TextEditorMapper.mapExtraValues(element, result);
	}
}
