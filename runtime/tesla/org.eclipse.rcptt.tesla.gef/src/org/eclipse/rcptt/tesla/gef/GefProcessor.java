/*******************************************************************************
 * Copyright (c) 2009, 2024 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.gef;

import static org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor.boundsResponse;
import static org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor.failResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.EventDispatcher;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowFigure;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPartViewer.Conditional;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.Tool;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ConnectionCreationTool;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.TeslaMessages;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.Assert;
import org.eclipse.rcptt.tesla.core.protocol.AssertKind;
import org.eclipse.rcptt.tesla.core.protocol.ChildrenResponse;
import org.eclipse.rcptt.tesla.core.protocol.DragCommand;
import org.eclipse.rcptt.tesla.core.protocol.ElementCommand;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.GetBounds;
import org.eclipse.rcptt.tesla.core.protocol.GetPropertyValue;
import org.eclipse.rcptt.tesla.core.protocol.GetRegionText;
import org.eclipse.rcptt.tesla.core.protocol.GetText;
import org.eclipse.rcptt.tesla.core.protocol.IElementProcessorMapper;
import org.eclipse.rcptt.tesla.core.protocol.ObjectResponse;
import org.eclipse.rcptt.tesla.core.protocol.ParentResponse;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolFactory;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolPackage;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectData;
import org.eclipse.rcptt.tesla.core.protocol.SelectResponse;
import org.eclipse.rcptt.tesla.core.protocol.diagram.ActivateDirectEdit;
import org.eclipse.rcptt.tesla.core.protocol.diagram.AssertFigureExists;
import org.eclipse.rcptt.tesla.core.protocol.diagram.CancelDirectEdit;
import org.eclipse.rcptt.tesla.core.protocol.diagram.CommitDirectEdit;
import org.eclipse.rcptt.tesla.core.protocol.diagram.CreateConnection;
import org.eclipse.rcptt.tesla.core.protocol.diagram.CreateConnectionResponse;
import org.eclipse.rcptt.tesla.core.protocol.diagram.CreateFigure;
import org.eclipse.rcptt.tesla.core.protocol.diagram.CreateFigureResponse;
import org.eclipse.rcptt.tesla.core.protocol.diagram.DiagramFactory;
import org.eclipse.rcptt.tesla.core.protocol.diagram.DiagramPackage;
import org.eclipse.rcptt.tesla.core.protocol.diagram.FigureMouseCommand;
import org.eclipse.rcptt.tesla.core.protocol.diagram.MouseCommand;
import org.eclipse.rcptt.tesla.core.protocol.diagram.MouseCommandKind;
import org.eclipse.rcptt.tesla.core.protocol.diagram.MoveFigure;
import org.eclipse.rcptt.tesla.core.protocol.diagram.ResizeFigure;
import org.eclipse.rcptt.tesla.core.protocol.diagram.SetFigureSelection;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawFactory;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.core.protocol.raw.ResponseStatus;
import org.eclipse.rcptt.tesla.core.ui.DiagramItem;
import org.eclipse.rcptt.tesla.gef.describers.EditPartDescriber;
import org.eclipse.rcptt.tesla.internal.core.AbstractTeslaClient;
import org.eclipse.rcptt.tesla.internal.core.SimpleCommandPrinter;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.core.info.InfoUtils;
import org.eclipse.rcptt.tesla.internal.core.info.InfoUtils.Node;
import org.eclipse.rcptt.tesla.internal.core.processing.ElementGenerator;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor;
import org.eclipse.rcptt.tesla.internal.ui.SWTElementMapper;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.internal.ui.player.WorkbenchUIElement;
import org.eclipse.rcptt.tesla.internal.ui.processors.IModelMapperHelper;
import org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor;
import org.eclipse.rcptt.tesla.swt.util.IdentifyObjectUtil;
import org.eclipse.rcptt.tesla.ui.SWTTeslaActivator;
import org.eclipse.rcptt.util.Function;
import org.eclipse.rcptt.util.ListUtil;
import org.eclipse.rcptt.util.Predicate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

public class GefProcessor implements ITeslaCommandProcessor, IModelMapperHelper {
	private final EClass[] commandsSupported = {
			DiagramPackage.Literals.CREATE_FIGURE,
			DiagramPackage.Literals.MOVE_FIGURE,
			DiagramPackage.Literals.GET_FIGURES,
			DiagramPackage.Literals.SET_FIGURE_SELECTION,
			DiagramPackage.Literals.ACTIVATE_DIRECT_EDIT,
			DiagramPackage.Literals.COMMIT_DIRECT_EDIT,
			DiagramPackage.Literals.CANCEL_DIRECT_EDIT,
			DiagramPackage.Literals.CREATE_CONNECTION,
			DiagramPackage.Literals.RESIZE_FIGURE,
			DiagramPackage.Literals.MOUSE_COMMAND,
			DiagramPackage.Literals.FIGURE_MOUSE_COMMAND,
			DiagramPackage.Literals.ASSERT_FIGURE_EXISTS,
			// Protocol commands
			ProtocolPackage.Literals.ASSERT, ProtocolPackage.Literals.GET_TEXT,
			ProtocolPackage.Literals.PARENT, ProtocolPackage.Literals.CHILDREN,
			ProtocolPackage.Literals.DRAG_COMMAND,
			ProtocolPackage.Literals.GET_REGION_TEXT,
			ProtocolPackage.Literals.GET_BOUNDS };

	private AbstractTeslaClient client;

	private final Map<Widget, DirectEditorContainer> openedDirectEdits = new HashMap<Widget, DirectEditorContainer>();
	private String id;
	private final Set<EditPart> dragParts = new HashSet<EditPart>();

	@Override
	public String getFeatureID() {
		return "org.eclipse.rcptt.tesla.gef";
	}

	@Override
	public int getPriority() {
		return 250;
	}

	private SelectResponse toResponse(Element e) {
		SelectResponse response = ProtocolFactory.eINSTANCE
				.createSelectResponse();
		response.getElements().add(e);
		return response;
	}

	private SWTUIProcessor getSWTProcessor() {
		return client.getProcessor(SWTUIProcessor.class);
	}

	@Override
	public PreExecuteStatus preExecute(Command command,
			PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		if (command instanceof SelectCommand) {
			SelectCommand cmd = (SelectCommand) command;
			SelectData data = cmd.getData();
			Element element = data.getParent();
			DiagramViewerUIElement diagram = getDiagram(element);
			if (diagram != null) {
				if (!getSWTProcessor().activateViewEditor(
						getPlayer().wrap(diagram.getCanvas()), false, info)) {
					return new PreExecuteStatus(false);
				}
			}
		}

		if (command instanceof FigureMouseCommand
				|| command instanceof MouseCommand) {
			final ElementCommand cmd = (ElementCommand) command;

			Element element = cmd.getElement();
			DiagramViewerUIElement diagram = getDiagram(element);
			if (diagram != null) {
				if (!getSWTProcessor().activateViewEditor(
						getPlayer().wrap(diagram.getCanvas()), false, info)) {
					return new PreExecuteStatus(false);
				}
			}
		}
		return null;
	}

	@Override
	public void initialize(AbstractTeslaClient client, String id) {
		this.client = client;
		this.id = id;
	}

	@Override
	public boolean isCommandSupported(Command cmd) {
		EClass ecl = cmd.eClass();
		for (EClass cl : commandsSupported) {
			if (cl.equals(ecl)) {
				return true;
			}
		}

		if (cmd instanceof ElementCommand) {
			ElementCommand elementCmd = (ElementCommand) cmd;
			Element element = elementCmd.getElement();
			if (element.getKind().equals(ElementKind.DiagramViewer.name())) {
				return getSWTProcessor().isCommandSupported(elementCmd);
			}
		}

		return false;
	}

	private static boolean skipModalDialogCheck(Command command) {
		if (command instanceof FigureMouseCommand) {
			MouseCommandKind kind = ((FigureMouseCommand) command).getKind();
			return kind == MouseCommandKind.UP
					|| kind == MouseCommandKind.HOVER;
		}
		return false;
	}

	@Override
	public Response executeCommand(Command command,
			IElementProcessorMapper mapper) {
		if (command instanceof GetBounds) {
			Element element = ((ElementCommand) command).getElement();
			FigureUIElement widget = getMapper().get(element);
			org.eclipse.swt.graphics.Rectangle bounds = null;
			if (widget != null) {
				bounds = widget.getDiagram().getBounds();
			}
			return boundsResponse(bounds);
		}

		// System.err.println("EXECUTE COMMAND:" + command);
		EClass eClass = command.eClass();
		EPackage pkg = eClass.getEPackage();
		Response response = null;
		DiagramViewerUIElement swtElement = null;

		if (command instanceof ElementCommand) {
			Element element = ((ElementCommand) command).getElement();
			swtElement = getDiagram(element);

			if (!skipModalDialogCheck(command)) {
				response = SWTUIProcessor.checkForModalDialogs(swtElement);
				if (response != null) {
					return response;
				}
			}
		}

		if (pkg.equals(DiagramPackage.eINSTANCE)) {
			switch (eClass.getClassifierID()) {
			case DiagramPackage.CREATE_FIGURE:
				response = handleCreateFigure((CreateFigure) command, mapper);
				break;
			case DiagramPackage.SET_FIGURE_SELECTION:
				response = handleSetFigureSelection((SetFigureSelection) command);
				break;
			case DiagramPackage.ACTIVATE_DIRECT_EDIT:
				response = handleActivateDirectEdit(
						(ActivateDirectEdit) command, mapper);
				break;
			case DiagramPackage.MOVE_FIGURE:
				response = handleFigureMove((MoveFigure) command);
				break;
			case DiagramPackage.RESIZE_FIGURE:
				response = handleFigureResize((ResizeFigure) command);
				break;
			case DiagramPackage.COMMIT_DIRECT_EDIT:
				response = handleCommitDirectEdit((CommitDirectEdit) command);
				break;
			case DiagramPackage.CANCEL_DIRECT_EDIT:
				response = handleCancelDirectEdit((CancelDirectEdit) command);
				break;
			case DiagramPackage.CREATE_CONNECTION:
				response = handleCreateConnection((CreateConnection) command,
						mapper);
				break;
			case DiagramPackage.MOUSE_COMMAND:
				response = handleMouseCommand((MouseCommand) command);
				break;
			case DiagramPackage.FIGURE_MOUSE_COMMAND:
				response = handleFigureMouseCommand((FigureMouseCommand) command);
				break;
			case DiagramPackage.ASSERT_FIGURE_EXISTS:
				response = handleAssertFigureExists((AssertFigureExists) command);
				break;
			}
		}
		if (pkg.equals(ProtocolPackage.eINSTANCE)) {
			switch (eClass.getClassifierID()) {
			case ProtocolPackage.CHILDREN:
				response = handleChildrenCommand((ElementCommand) command,
						mapper);
				break;
			case ProtocolPackage.PARENT:
				response = handleParentCommand((ElementCommand) command, mapper);
				break;
			case ProtocolPackage.GET_TEXT:
				response = handleGetTextCommand((GetText) command);
				break;
			case ProtocolPackage.ASSERT:
				response = handleAssert((Assert) command);
				break;
			case ProtocolPackage.GET_REGION_TEXT:
				response = handleGetRegionText((GetRegionText) command);
				break;
			case ProtocolPackage.DRAG_COMMAND:
				response = client.getProcessor(SWTUIProcessor.class)
						.getDragSupport().handleDrag((DragCommand) command);
				break;
			case ProtocolPackage.GET_PROPERTY_VALUE:
				response = handleGetPropertyValue((GetPropertyValue) command);
				break;
			default:
				if (command instanceof ElementCommand && swtElement != null) {
					ElementCommand redirectedCmd = getSWTProcessor()
							.convertDiagramToCanvasCommand(
									(ElementCommand) command,
									swtElement.getCanvas());
					response = getSWTProcessor().executeCommand(redirectedCmd,
							mapper);
				}
			}
		}
		if (response != null) {
			return response;
		}
		return null;
	}

	protected Response handleGetPropertyValue(final GetPropertyValue command) {
		final Element element = command.getElement();
		if (element == null) {
			return null;
		}
		ObjectResponse result = ProtocolFactory.eINSTANCE
				.createObjectResponse();

		try {
			result.setResult(getPropertyValue(element, command.getName(),
					command.getIndex()));
		} catch (CoreException e) {
			result.setStatus(ResponseStatus.FAILED);
			result.setResult(e.getStatus());
			result.setMessage(e.getMessage());
		}
		return result;
	}

	private Object getPropertyValue(Element element, String property,
			Integer index) throws CoreException {
		final FigureUIElement uiElement = getMapper().get(element);
		final RawFigureUIElement figureUiElement = getFigureMapper().get(
				element);
		if (uiElement == null && figureUiElement == null) {
			// return assertResponse(ResponseStatus.FAILED,
			// "Widget are not found");
			return null;
		}

		org.eclipse.rcptt.tesla.core.ui.Widget model;
		if (uiElement != null) {
			model = getMappedModel(uiElement.getPart());
		} else {
			model = GefModelMapper.figureMap(figureUiElement.getFigure());
		}

		Object value = SWTUIProcessor.getAttrValue(model, property, index);
		if (value != null)
			return value;

		try {
			if (uiElement != null)
				value = getPropertyValue(uiElement.getPart(), property);
			else
				value = GefModelMapper.getPropertyValue(
						figureUiElement.getFigure(), property);
			return value;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SWTTeslaActivator.PLUGIN_ID, String.format(
							"Failed to get propety '%s'", property),
					e));
		}
	}

	private Response handleGetRegionText(GetRegionText command) {
		SWTUIProcessor processor = client.getProcessor(SWTUIProcessor.class);
		final Element element = command.getElement();
		final FigureUIElement uiElement = getMapper().get(element);
		if (uiElement == null) {
			return failResponse(TeslaGefMessages.CommandProcessor_CannotFindWidget);
		}
		EditPartDescriber describer = new EditPartDescriber(
				uiElement.getPart(), uiElement.getDiagram().getViewer());
		return processor.handleGetRegionText(command, describer);
	}

	private Response handleAssert(Assert assertCmd) {
		SWTUIProcessor processor = client.getProcessor(SWTUIProcessor.class);
		final Element element = assertCmd.getElement();
		final FigureUIElement uiElement = getMapper().get(element);
		final RawFigureUIElement figureUiElement = getFigureMapper().get(
				element);
		if (uiElement == null && figureUiElement == null) {
			// return assertResponse(ResponseStatus.FAILED,
			// "Widget are not found");
			return null;
		}
		if (assertCmd.getKind().equals(AssertKind.CONTAINS_IMAGE)
				|| assertCmd.getKind().equals(AssertKind.IMAGE_CONTAINS_TEXT)) {
			return processor.doImageAssert(assertCmd, new EditPartDescriber(
					uiElement.getPart(), uiElement.getDiagram().getViewer()));
		}
		org.eclipse.rcptt.tesla.core.ui.Widget model;
		if (uiElement != null) {
			model = getMappedModel(uiElement.getPart());
		} else {
			model = GefModelMapper.figureMap(figureUiElement.getFigure());
		}
		Object value = null;

		String attrValue = assertCmd.getValue();
		if (attrValue == null) {
			attrValue = "";
		}
		String attrName = assertCmd.getAttribute();

		if (!"property".equals(assertCmd.getCategory())) {
			value = SWTUIProcessor.getAttrValue(model, attrName,
					assertCmd.getIndex());
		}
		if (value == null) {
			try {
				if (uiElement != null) {
					value = getPropertyValue(uiElement.getPart(), attrName);
				} else {
					value = GefModelMapper.getPropertyValue(
							figureUiElement.getFigure(), attrName);
				}
			} catch (Exception e) {
				String reason = e.getMessage();
				if (reason == null || reason.length() == 0) {
					StackTraceElement stackTraceElement = e.getStackTrace()[1];
					reason = e.getClass().getSimpleName() + " in " //$NON-NLS-1$
							+ stackTraceElement.getClassName() + "." //$NON-NLS-1$
							+ stackTraceElement.getMethodName() + ":" //$NON-NLS-1$
							+ stackTraceElement.getLineNumber();
				}
				reason = NLS.bind(TeslaGefMessages.GefProcessor_AssertFailed,
						assertCmd.getAttribute()) + " " + reason; //$NON-NLS-1$
				return failResponse(reason);
			}
		}
		if (value == null) {
			return failResponse(NLS.bind(
					TeslaGefMessages.CommandProcessor_CannotFindProperty,
					attrName));

		}

		String strValue = value.toString();
		if (value instanceof EObject) {
			strValue = SimpleCommandPrinter.toString((EObject) value, true)
					.trim();
		}
		return processor.performAssert(assertCmd, attrValue, value, strValue);
	}

	private org.eclipse.rcptt.tesla.core.ui.Widget getMappedModel(final EditPart part) {
		SWTUIProcessor processor = client.getProcessor(SWTUIProcessor.class);
		List<IGefReplayHelper> helpers = client
				.getProcessors(IGefReplayHelper.class);
		org.eclipse.rcptt.tesla.core.ui.Widget model = null;
		if (model == null) {
			for (IGefReplayHelper helper : helpers) {
				model = helper.getModel(part);
			}
		}
		SWTUIPlayer player = processor.getPlayer();
		EditPartViewer viewer = part.getViewer();
		SWTUIElement semanticParent = player.getParentElement(player
				.wrap(viewer.getControl()));

		if (semanticParent instanceof WorkbenchUIElement) {
			DiagramItem map = GefModelMapper.basicMap(part);

			if (PropertySourceBasedModelMapper.map(
					((WorkbenchUIElement) semanticParent).reference
							.getPart(true),
					part, map)) {
				model = map;
			}
		}
		if (model == null) {
			model = GefModelMapper.map(part, getPlayer());
		}
		return model;
	}

	public String getPropertyValue(EditPart part, String nodePath)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		// Search of property must be performed in same order as we mapped model
		// in GefFigureLocator.mapModel method
		SWTUIProcessor processor = client.getProcessor(SWTUIProcessor.class);
		SWTUIPlayer player = processor.getPlayer();
		EditPartViewer viewer = part.getViewer();
		SWTUIElement semanticParent = player.getParentElement(player
				.wrap(viewer.getControl()));

		if (semanticParent instanceof WorkbenchUIElement) {
			if (PropertySourceBasedModelMapper.isPropertyBasedMapped(
					((WorkbenchUIElement) semanticParent).reference
							.getPart(true),
					part)) {
				String value = PropertySourceBasedModelMapper.getPropertyValue(
						((WorkbenchUIElement) semanticParent).reference
								.getPart(true),
						part, nodePath);
				if (value != null) {
					return value;
				}
			}
		}

		List<IGefReplayHelper> helpers = client
				.getProcessors(IGefReplayHelper.class);
		for (IGefReplayHelper helper : helpers) {
			if (helper.isGMFMapped(part)) {
				return helper.getPropertyValue(part, nodePath);
			}
		}

		return GefModelMapper.getPropertyValue(part, nodePath);
	}

	@SuppressWarnings("rawtypes")
	private Response handleAssertFigureExists(AssertFigureExists command) {
		Element element = command.getElement();
		DiagramViewerUIElement diagram = getDiagram(element);
		// diagram.
		RootEditPart part = diagram.getViewer().getRootEditPart();
		EditPart requiredPart = getPart(command.getPartPath(), part);
		if (requiredPart == null) {
			EditPart te = part;
			for (Integer integer : command.getPartPath()) {
				List children = filterFigures(te.getChildren());
				if (children.size() <= integer.intValue()) {
					break;
				}
				System.out.println("Failed parts:" + te.getClass().getName());
				te = (EditPart) children.get(integer.intValue());
			}
			Response response = RawFactory.eINSTANCE.createResponse();
			response.setStatus(ResponseStatus.FAILED);
			return response;
		}
		Response response = RawFactory.eINSTANCE.createResponse();
		response.setStatus(ResponseStatus.OK);
		return response;
	}

	private Response handleMouseCommand(final MouseCommand command) {
		Element element = command.getElement();
		DiagramViewerUIElement diagram = getDiagram(element);
		if (diagram == null) {
			return null;
		}
		// final GraphicalViewer viewer = diagram.getViewer();
		// final EditDomain domain = viewer.getEditDomain();
		final EventDispatcher dispatcher = diagram.getDispatcher();
		Event e = new Event();
		final Canvas canvas = diagram.getCanvas();
		e.widget = canvas;
		e.time = (int) System.currentTimeMillis();
		e.x = command.getX();
		e.y = command.getY();
		e.stateMask = command.getStateMask();
		e.button = command.getButton();
		final org.eclipse.swt.graphics.Rectangle oldBounds = canvas.getBounds();

		final MouseEvent mouseEvent = new MouseEvent(e);
		getPlayer().exec("Set bounds and location", new Runnable() {

			@Override
			public void run() {
				if (command.getBoundsHeight() != 0
						&& command.getBoundsWidth() != 0) {
					canvas.setRedraw(false);
					canvas.setBounds(0, 0, command.getBoundsWidth(),
							command.getBoundsHeight());
				}
			}
		});
		getPlayer().exec("Tesla Gef runnable", new Runnable() {

			@Override
			public void run() {
				getPlayer().addMouseWidgetInfo(canvas, mouseEvent.x,
						mouseEvent.y);
				switch (command.getKind()) {
				case DOWN:
					dispatcher.dispatchMousePressed(mouseEvent);
					break;
				case ENTER:
					dispatcher.dispatchMouseEntered(mouseEvent);
					break;
				case EXIT:
					dispatcher.dispatchMouseExited(mouseEvent);
					break;
				case UP:
					dispatcher.dispatchMouseReleased(mouseEvent);
					break;
				case MOVE:
					mouseEvent.stateMask = 0;
					dispatcher.dispatchMouseMoved(mouseEvent);
					// domain.mouseMove(mouseEvent, viewer);
					break;
				case DRAG:
					mouseEvent.stateMask = SWT.BUTTON_MASK;
					dispatcher.dispatchMouseMoved(mouseEvent);
					// domain.mouseDrag(mouseEvent, viewer);
					// domain.mouseMove(mouseEvent, viewer);
					break;
				case HOVER:
					dispatcher.dispatchMouseHover(mouseEvent);
					// mouseHoverExec(dispatcher, mouseEvent, 10);
					break;
				case DOUBLE_CLICK:
					dispatcher.dispatchMouseDoubleClicked(mouseEvent);
					break;
				case NATIVE_DRAG_FINISHED:
					// domain.nativeDragFinished(null, viewer);
					break;
				case NATIVE_DRAG_STARTED:
					// domain.nativeDragStarted(null, viewer);
					break;
				}
				canvas.setBounds(oldBounds);
				canvas.setRedraw(true);
			}

			@SuppressWarnings("unused")
			private void mouseHoverExec(final EventDispatcher dispatcher,
					final MouseEvent mouseEvent, final int tryCount) {
				if (tryCount == 0) {
					return;
				}
				getPlayer().exec("One more hover event", new Runnable() {

					@Override
					public void run() {
						dispatcher.dispatchMouseHover(mouseEvent);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mouseHoverExec(dispatcher, mouseEvent, tryCount - 1);
					}
				});
			}
		});
		return RawFactory.eINSTANCE.createResponse();
	}

	private Response handleFigureMouseCommand(final FigureMouseCommand command) {
		Element element = command.getElement();
		DiagramViewerUIElement diagram = getDiagram(element);
		if (diagram == null) {
			return proceedFigureCanvas(command, element);
		}
		final GraphicalViewer viewer = diagram.getViewer();
		// final EditDomain domain = viewer.getEditDomain();
		// final EventDispatcher dispatcher = diagram.getDispatcher();
		final Canvas canvas = diagram.getCanvas();

		// Assume it is always graphical edit part
		final GraphicalEditPart part;
		FigureUIElement mapped = getMapper().get(element);
		RawFigureUIElement rawMapped = null;
		if (mapped != null) {
			part = (GraphicalEditPart) mapped.getPart();
		} else {
			rawMapped = getRawMapper().get(element);
			if (rawMapped != null) {
				part = (GraphicalEditPart) rawMapped.getPart();
			} else {
				part = getEditPartFromPath(command.getPartPath(), viewer);
			}
		}
		if (part == null && command.getPartPath().size() > 0) {
			return createFailed(TeslaGefMessages.GefProcessor_CannotFindEditPart);
		}
		IFigure partFigure = null;
		if (part != null) {
			partFigure = part.getFigure();
		} else {
			partFigure = diagram.getFigureContents();
		}
		EList<Integer> figurePath = command.getFigurePath();
		// String out = figurePath.toString();
		// System.out.println(out);
		final IFigure figure;
		if (rawMapped != null) {
			figure = rawMapped.getFigure();
		} else {
			figure = getFigure(figurePath, partFigure, part);
		}
		if (figure == null) {
			return createFailed(TeslaGefMessages.GefProcessor_CannotFindFigure);
		}
		final EditPart finalPart = part;
		// final org.eclipse.swt.graphics.Rectangle oldBounds =
		// canvas.getBounds();
		getPlayer().exec("Tesla Gef runnable", new Runnable() {

			@Override
			@SuppressWarnings("restriction")
			public void run() {
				int horizontalBarVisibility = -1;
				int verticalBarVisibility = -1;
				if (finalPart != null) {
					org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart drawer = findContainingDrawer(finalPart
							.getParent());
					if (drawer != null) {
						horizontalBarVisibility = getDrawerFigure(drawer)
								.getScrollpane()
								.getHorizontalScrollBarVisibility();
						verticalBarVisibility = getDrawerFigure(drawer)
								.getScrollpane()
								.getVerticalScrollBarVisibility();
						getDrawerFigure(drawer).getScrollpane()
								.setHorizontalScrollBarVisibility(0);
						getDrawerFigure(drawer).getScrollpane()
								.setVerticalScrollBarVisibility(0);
					}
					viewer.reveal(finalPart);
				} else {
					if (canvas instanceof FigureCanvas) {
						scrollToFigure((FigureCanvas) canvas, figure);
					}
				}

				// Resize figure to recorded width and height.
				// Figure must resized after scrollToFigure, because
				// scrollToFigure may validate diagram and reset size
				Dimension origMinSize = getMinSize(figure);
				Dimension origMaxSize = getMaxSize(figure);
				Dimension origSize = getSize(figure);
				int recordedWidth = command.getFigureWidth();
				int recordedHeight = command.getFigureHeight();
				boolean isSizeChanged = false;
				if (recordedWidth != 0
						&& recordedHeight != 0
						&& (origSize.width != recordedWidth || origSize.height != recordedHeight)) {
					try {
						if (origMinSize != null) {
							figure.setMinimumSize(new Dimension(recordedWidth,
									recordedHeight));
						}
						if (origMaxSize != null) {
							figure.setMaximumSize(new Dimension(recordedWidth,
									recordedHeight));
						}
						figure.setSize(recordedWidth, recordedHeight);
						figure.validate();
						if (getSize(figure) != null
								&& getSize(figure).width == recordedWidth
								&& getSize(figure).height == recordedHeight) {
							isSizeChanged = true;
						}
					} catch (Exception e1) {
						TeslaCore.log(e1);
					}
				}

				if (canvas instanceof FigureCanvas) {
					((FigureCanvas) canvas).getViewport();
				}
				Event e = new Event();
				Rectangle bounds = figure.getBounds().getCopy();

				int x = bounds.x + command.getX();
				int y = bounds.y + command.getY();

				if (!isSizeChanged) {
					// translate to relative coordinates
					x = (int) (bounds.x + bounds.width
							* (1.0 * command.getX() / command.getFigureWidth()));
					y = (int) (bounds.y + bounds.height
							* (1.0 * command.getY() / command.getFigureHeight()));
				}
				if (canvas instanceof FigureCanvas) {
					scrollToPosition((FigureCanvas) canvas, x, y);
				}

				// We need to be sure proposed point is inside correct figure,
				// if not we need to move it to some there in figure
				if (canvas instanceof FigureCanvas) {
					Point p = correctFigurePosition(x, y, part, figure, viewer,
							(FigureCanvas) canvas);
					if (p != null) {
						x = p.x;
						y = p.y;
					}
				}
				Point translatedPoint = new Point(x, y);
				figure.translateToAbsolute(translatedPoint);
				int nx = translatedPoint.x;
				int ny = translatedPoint.y;
				e.widget = canvas;
				e.time = (int) System.currentTimeMillis();
				e.x = nx;
				e.y = ny;

				// DEBUG: draw point at mouse position
				// final int fX = nx;
				// final int fY = ny;
				// final Rectangle fBounds = bounds;
				// canvas.addPaintListener(new PaintListener() {
				//
				// public void paintControl(PaintEvent e) {
				// e.gc.setBackground(canvas.getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
				// e.gc.drawRectangle(fBounds.x, fBounds.y, fBounds.width,
				// fBounds.height);
				// e.gc.fillOval(fX - 5, fY - 5, 10, 10);
				// }
				// });
				// System.out.println("x=" + fX + ";y=" + fY);

				e.stateMask = command.getStateMask();
				e.button = command.getButton();
				e.count = 1;
				getPlayer().addMouseWidgetInfo(canvas, e.x, e.y);
				switch (command.getKind()) {
				case DOWN:
					e.type = SWT.MouseDown;
					getPlayer().getEvents().sendEvent(canvas, e);
					if (finalPart != null) {
						DNDSupport.fillDragParts(finalPart, dragParts, viewer,
								client);
					} else {
						DNDSupport.fillDragParts(null, dragParts, viewer,
								client);
					}
					break;
				case ENTER:
					e.type = SWT.MouseEnter;
					getPlayer().getEvents().sendEvent(canvas, e);
					break;
				case EXIT:
					e.type = SWT.MouseExit;
					getPlayer().getEvents().sendEvent(canvas, e);
					break;
				case UP:
					e.type = SWT.MouseUp;
					getPlayer().getEvents().sendEvent(canvas, e);
					dragParts.clear();
					break;
				case MOVE:
					e.type = SWT.MouseMove;
					getPlayer().getEvents().sendEvent(canvas, e);
					break;
				case DRAG:
					if ((e.stateMask & SWT.BUTTON_MASK) == 0) {
						e.stateMask |= SWT.BUTTON_MASK;
					}
					e.type = SWT.MouseMove;
					getPlayer().getEvents().sendEvent(canvas, e);
					break;
				case HOVER:
					e.type = SWT.MouseHover;
					getPlayer().getEvents().sendEvent(canvas, e);
					break;
				case DOUBLE_CLICK:
					e.type = SWT.MouseDoubleClick;
					getPlayer().getEvents().sendEvent(canvas, e);
					break;
				case NATIVE_DRAG_FINISHED:
					// domain.nativeDragFinished(null, viewer);
					break;
				case NATIVE_DRAG_STARTED:
					// domain.nativeDragStarted(null, viewer);
					break;
				}
				// Restore original size of figure and scrollbars visibility
				if (origMinSize != null && origMinSize != getMinSize(figure)) {
					try {
						figure.setMinimumSize(origMinSize);
					} catch (Exception e1) {
						TeslaCore.log(e1);
					}
				}
				if (origMaxSize != null && origMaxSize != getMaxSize(figure)) {
					try {
						figure.setMaximumSize(origMaxSize);
					} catch (Exception e1) {
						TeslaCore.log(e1);
					}
				}
				if (origSize != null && origSize != getSize(figure)) {
					try {
						figure.setSize(origSize);
					} catch (Exception e1) {
						TeslaCore.log(e1);
					}
				}
				if (finalPart != null) {
					org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart drawer = findContainingDrawer(finalPart
							.getParent());
					if (drawer != null && horizontalBarVisibility != -1
							&& verticalBarVisibility != -1) {
						getDrawerFigure(drawer)
								.getScrollpane()
								.setHorizontalScrollBarVisibility(
										horizontalBarVisibility);
						getDrawerFigure(drawer)
								.getScrollpane()
								.setVerticalScrollBarVisibility(
										verticalBarVisibility);
					}
				}
			}
		});
		return RawFactory.eINSTANCE.createResponse();
	}
	
	@SuppressWarnings("restriction")
	private org.eclipse.gef.internal.ui.palette.editparts.DrawerFigure getDrawerFigure(org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart drawer) {
		return (org.eclipse.gef.internal.ui.palette.editparts.DrawerFigure) drawer.getFigure();
	}

	@SuppressWarnings("restriction")
	private org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart findContainingDrawer(EditPart part) {
		if (part == null)
			return null;
		if (part instanceof org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart)
			return (org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart) part;
		return findContainingDrawer(part.getParent());
	}

	private Dimension getMinSize(IFigure figure) {
		if (figure instanceof Label && figure.getFont() == null) {
			return null;
		}
		Dimension minSize = null;
		try {
			minSize = figure.getMinimumSize().getCopy();
		} catch (Exception e) {
			TeslaCore.log(e);
		}
		return minSize;
	}

	private Dimension getMaxSize(IFigure figure) {
		if (figure instanceof Label && figure.getFont() == null) {
			return null;
		}
		Dimension maxSize = null;
		try {
			maxSize = figure.getMaximumSize().getCopy();
		} catch (Exception e) {
			TeslaCore.log(e);
		}
		return maxSize;
	}

	private Dimension getSize(IFigure figure) {
		Dimension size = null;
		try {
			size = figure.getSize().getCopy();
		} catch (Exception e) {
			TeslaCore.log(e);
		}
		return size;
	}

	@SuppressWarnings("rawtypes")
	protected Point correctFigurePosition(int nx, int ny,
			GraphicalEditPart part, IFigure figure, GraphicalViewer viewer,
			FigureCanvas canvas) {
		if (isPartFiltered(part)) {
			return null;
		}
		if (dragParts.contains(part)) {
			return null;
		}
		Set exclude = new HashSet();
		Rectangle bounds = getBounds(figure);
		if (isInside(canvas, viewer, part, figure, nx, ny, exclude)) {
			return null;
		}

		// Search nearest point inside bounds
		int newX = nx;
		if (nx < bounds.x) {
			newX = bounds.x + 1;
		} else if (nx > bounds.x + bounds.width) {
			newX = bounds.x + bounds.width - 1;
		}
		int newY = ny;
		if (ny < bounds.y) {
			newY = bounds.y + 1;
		} else if (ny > bounds.y + bounds.height) {
			newY = bounds.y + bounds.height - 1;
		}

		if (isInside(canvas, viewer, part, figure, newX, newY, exclude)) {
			return new Point(newX, newY);
		}
		// Point inside bounds sometimes not inside figure, for example arrows
		// in this case brutforce search
		// Going to left

		Point absoluteEventPoint = new Point(nx, ny);
		figure.translateToAbsolute(absoluteEventPoint);

		Rectangle absoluteBounds = figure.getBounds().getCopy();
		figure.translateToAbsolute(absoluteBounds);

		for (int x = absoluteEventPoint.x; x > absoluteBounds.x; x--) {
			// Going up
			for (int y = absoluteEventPoint.y; y > absoluteBounds.y; y--) {
				Point relativeEventPoint = new Point(x, y);
				figure.translateToRelative(relativeEventPoint);
				if (isInside(canvas, viewer, part, figure,
						relativeEventPoint.x, relativeEventPoint.y, exclude)) {
					return relativeEventPoint;
				}
			}
			// Going down
			for (int y = absoluteEventPoint.y; y < absoluteBounds.y
					+ absoluteBounds.height; y++) {
				Point relativeEventPoint = new Point(x, y);
				figure.translateToRelative(relativeEventPoint);
				if (isInside(canvas, viewer, part, figure,
						relativeEventPoint.x, relativeEventPoint.y, exclude)) {
					return relativeEventPoint;
				}
			}
		}
		// Going to right
		for (int x = absoluteEventPoint.x; x < absoluteBounds.x
				+ absoluteBounds.width; x++) {
			// Going up
			for (int y = absoluteEventPoint.y; y > absoluteBounds.y; y--) {
				Point relativeEventPoint = new Point(x, y);
				figure.translateToRelative(relativeEventPoint);
				if (isInside(canvas, viewer, part, figure,
						relativeEventPoint.x, relativeEventPoint.y, exclude)) {
					return relativeEventPoint;
				}
			}
			// Going down
			for (int y = absoluteEventPoint.y; y < absoluteBounds.y
					+ absoluteBounds.height; y++) {
				Point relativeEventPoint = new Point(x, y);
				figure.translateToRelative(relativeEventPoint);
				if (isInside(canvas, viewer, part, figure,
						relativeEventPoint.x, relativeEventPoint.y, exclude)) {
					return relativeEventPoint;
				}
			}
		}
		return null;
	}

	private boolean isPartFiltered(GraphicalEditPart part) {
		if (part != null) {
			GraphicalEditPart editpart = part;
			while (editpart != null) {
				if (dragParts.contains(editpart)) {
					return true;
				}
				editpart = (GraphicalEditPart) editpart.getParent();
			}
		}
		return false;
	}

	private Rectangle getBounds(IFigure figure) {
		Rectangle bounds = figure.getBounds();
		if (bounds.x == 0 && bounds.y == 0) {
			// return parent bound
			IFigure p = figure.getParent();
			if (p != null) {
				return getBounds(p);
			}
		}
		return bounds;
	}

	@SuppressWarnings("rawtypes")
	private boolean isInside(FigureCanvas canvas, GraphicalViewer viewer,
			GraphicalEditPart part, IFigure figure, int nx, int ny, Set exclude) {
		Point translatedPoint = new Point(nx, ny);
		figure.translateToAbsolute(translatedPoint);
		// Check for some inner figure inside.
		if (figure instanceof Layer && figure.getBounds().contains(nx, ny)) {
			if (part != null) {
				EditPart at = viewer.findObjectAt(new Point(translatedPoint.x,
						translatedPoint.y));
				if (at != null && !at.equals(part)) {
					return false; // Have some part inside
				}
			} else {
				EditPart at = findAt(viewer, translatedPoint, exclude);
				if (at != null) {
					if (!at.equals(part)
							&& !at.equals(viewer.getRootEditPart())) { // Some
																		// child
																		// part
						return false;
					}
				}
			}

			return true;
		}
		if (!figure.containsPoint(nx, ny)) {
			return false;
		}
		IFigure figureRoot = viewer.getRootEditPart() != null ? ((GraphicalEditPart) viewer.getContents())
				.getFigure() : canvas.getContents();
		if (part != null) {
			EditPart at = viewer.findObjectAt(new Point(translatedPoint.x,
					translatedPoint.y));
			if (at != null && !at.equals(part)) {
				return false;
			}
			figureRoot = part.getFigure();
		} else {
			if (viewer.getRootEditPart() != null) {
				EditPart at = findAt(viewer, translatedPoint, exclude);
				if (at != null) {
					EditPart parentPart = viewer.getContents();
					boolean partFound = at.equals(parentPart);
					while (!partFound && parentPart != null) {
						parentPart = parentPart.getParent();
						partFound = at.equals(parentPart);
					}
					return partFound;
				}
			}
		}
		IFigure figureAt = figureRoot.findFigureAt(nx, ny);
		while (figureAt instanceof FlowFigure) {
			// org.eclipse.draw2d.geometry.Rectangle bounds =
			// figureAt.getBounds();
			figureAt = figureAt.getParent();
			if (figure.equals(figureAt)) {
				break;
			}
		}

		boolean figureFind = figure.equals(figureAt);
		IFigure parentFugure = figureAt;

		while (!figureFind && parentFugure != null) {
			parentFugure = parentFugure.getParent();
			figureFind = figure.equals(parentFugure);
		}

		return figureFind;
	}

	Conditional findAtCondition = new Conditional() {
		@Override
		public boolean evaluate(EditPart editpart) {
			while (editpart != null) {
				if (dragParts.contains(editpart)) {
					return false;
				}
				editpart = editpart.getParent();
			}
			// Check parents of edit part to not be
			// dragPart
			return true;
		}
	};

	@SuppressWarnings("rawtypes")
	private EditPart findAt(GraphicalViewer viewer, Point translatedPoint,
			Set exclude) {
		EditPart at = viewer.findObjectAtExcluding(new Point(translatedPoint.x,
				translatedPoint.y), exclude, findAtCondition);
		return at;
	}

	@SuppressWarnings("rawtypes")
	public GraphicalEditPart getEditPartFromPath(final List<Integer> partPath,
			final GraphicalViewer viewer) {
		GraphicalEditPart part = null;
		if (partPath.size() > 0) {
			if (partPath.get(0) == -2) {
				// Source connection based path
				int index = partPath.get(1);
				List<Integer> address = new ArrayList<Integer>();
				List<Integer> subPart = new ArrayList<Integer>();
				fillAddresses(partPath, address, subPart);
				GraphicalEditPart sourcePart = (GraphicalEditPart) getPart(
						address, viewer.getRootEditPart());
				List list = sourcePart.getSourceConnections();
				if (list.size() <= index) {
					return null;
				}
				part = (GraphicalEditPart) list.get(index);
				if (part != null && subPart.size() > 0) {
					return (GraphicalEditPart) getPart(subPart, part);
				}
			} else if (partPath.get(0) == -3) {
				// target connection based
				int index = partPath.get(1);
				List<Integer> address = new ArrayList<Integer>();
				List<Integer> subPart = new ArrayList<Integer>();
				fillAddresses(partPath, address, subPart);
				GraphicalEditPart sourcePart = (GraphicalEditPart) getPart(
						address, viewer.getRootEditPart());
				List list = sourcePart.getTargetConnections();
				if (list.size() <= index) {
					return null;
				}
				part = (GraphicalEditPart) list.get(index);
				if (part != null && subPart.size() > 0) {
					return (GraphicalEditPart) getPart(subPart, part);
				}
			} else {
				part = (GraphicalEditPart) getPart(partPath,
						viewer.getRootEditPart());
			}
		}
		return part;
	}

	private void fillAddresses(final List<Integer> partPath,
			List<Integer> address, List<Integer> subPart) {
		boolean sub = false;
		for (int i = 2; i < partPath.size(); i++) {
			Integer val = partPath.get(i);
			if (val.intValue() == -1) {
				sub = true;
				continue;
			}
			if (!sub) {
				address.add(val);
			} else {
				subPart.add(val);
			}
		}
	}

	private Response proceedFigureCanvas(final FigureMouseCommand command,
			Element element) {
		SWTUIElement canvasElement = SWTElementMapper.getMapper(id)
				.get(element);
		IFigure resultFigure = null;
		if (canvasElement == null) {
			RawFigureUIElement rawFigure = getRawMapper().get(element);
			if (rawFigure != null) {
				resultFigure = rawFigure.getFigure();
				canvasElement = getPlayer().wrap(rawFigure.getCanvas());
			}
		}
		if (canvasElement != null) {
			Widget widget = PlayerWrapUtils.unwrapWidget(canvasElement);
			if (widget instanceof FigureCanvas) {
				final FigureCanvas canvas = (FigureCanvas) widget;
				final IFigure figure;
				if (resultFigure == null) {
					figure = getFigure(command.getFigurePath(),
							canvas.getContents(), null);
				} else {
					figure = resultFigure;
				}
				if (figure == null) {
					return createFailed(TeslaGefMessages.GefProcessor_CannotFindFigure);
				}
				final EventDispatcher dispatcher = canvas
						.getLightweightSystem().getRootFigure()
						.internalGetEventDispatcher();
				getPlayer().exec("Set bounds and location", new Runnable() {

					@Override
					public void run() {
						scrollToFigure(canvas, figure);
					}
				});

				// final org.eclipse.swt.graphics.Rectangle oldBounds =
				// canvas.getBounds();

				getPlayer().exec("Tesla Gef runnable", new Runnable() {

					@Override
					public void run() {
						Event e = new Event();
						Rectangle bounds = figure.getBounds();
						double px = 1.0 * command.getX()
								/ command.getFigureWidth();
						double py = 1.0 * command.getY()
								/ command.getFigureHeight();
						Point translatedPoint = new Point(
								(int) (bounds.x + bounds.width * px),
								(int) (bounds.y + bounds.height * py));
						figure.translateToAbsolute(translatedPoint);
						int nx = translatedPoint.x;
						int ny = translatedPoint.y;
						e.widget = canvas;
						e.time = (int) System.currentTimeMillis();
						e.x = nx;
						e.y = ny;
						e.stateMask = command.getStateMask();
						e.button = command.getButton();

						final MouseEvent mouseEvent = new MouseEvent(e);
						switch (command.getKind()) {
						case DOWN:
							dispatcher.dispatchMousePressed(mouseEvent);
							break;
						case ENTER:
							dispatcher.dispatchMouseEntered(mouseEvent);
							break;
						case EXIT:
							dispatcher.dispatchMouseExited(mouseEvent);
							break;
						case UP:
							dispatcher.dispatchMouseReleased(mouseEvent);
							break;
						case MOVE:
							dispatcher.dispatchMouseMoved(mouseEvent);
							// domain.mouseMove(mouseEvent, viewer);
							break;
						case DRAG:
							dispatcher.dispatchMouseMoved(mouseEvent);
							// domain.mouseDrag(mouseEvent, viewer);
							// domain.mouseMove(mouseEvent, viewer);
							break;
						case HOVER:
							dispatcher.dispatchMouseHover(mouseEvent);
							break;
						case DOUBLE_CLICK:
							dispatcher.dispatchMouseDoubleClicked(mouseEvent);
							break;
						}
					}
				});
				return RawFactory.eINSTANCE.createResponse();
			}
		}
		return createFailed(TeslaGefMessages.GefProcessor_CannotFindDiagramOrCanvas);
	}

	public static Response createFailed(String msg) {
		Response response = ProtocolFactory.eINSTANCE.createBooleanResponse();
		response.setStatus(ResponseStatus.FAILED);
		response.setMessage(msg);
		return response;
	}

	public DiagramViewerUIElement getDiagram(Element element) {
		DiagramViewerUIElement diagram;
		FigureUIElement figureUIElement = getMapper().get(element);
		if (figureUIElement != null) {
			diagram = figureUIElement.getDiagram();
		} else {
			RawFigureUIElement rawFigureUIElement = getRawMapper().get(element);
			if (rawFigureUIElement != null) {
				GraphicalViewer viewer = rawFigureUIElement.getViewer();
				if (viewer != null) {
					return new DiagramViewerUIElement(getPlayer(), viewer);
				}
			}
			SWTUIElement swtuiElement = SWTElementMapper.getMapper(id).get(
					element);
			if (swtuiElement instanceof DiagramViewerUIElement) {
				diagram = (DiagramViewerUIElement) swtuiElement;
			} else {
				return null;
			}
		}
		return diagram;
	}

	@SuppressWarnings("rawtypes")
	private EditPart getPart(List<Integer> partPath, EditPart rootPart) {
		EditPart te = rootPart;
		for (Integer integer : partPath) {
			List children = te.getChildren();
			List realChildren = getPartChildren(children);
			if (realChildren.size() <= integer.intValue()
					|| integer.intValue() < 0) {
				return null;
			}
			te = (EditPart) realChildren.get(integer.intValue());
		}
		return te;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List getPartChildren(List children) {
		List realChildren = new ArrayList();
		for (Object object : children) {
			if (object instanceof GraphicalEditPart) {
				IFigure figure = ((GraphicalEditPart) object).getFigure();
				if (!figure.isVisible()) {
					continue;
				}
			}
			realChildren.add(object);
		}
		return realChildren;
	}

	private IFigure getFigure(List<Integer> path, IFigure figure,
			GraphicalEditPart part) {
		List<Integer> copyList = new ArrayList<Integer>(path);
		if (copyList.size() > 0) {
			Integer p0 = copyList.get(0);
			if (p0.intValue() < 0) {
				if (p0.equals(Integer.valueOf(-1))) {
					// Do look from Root
					while (figure.getParent() != null) {
						figure = figure.getParent();
					}
					copyList.remove(0);
				}
				if (part != null) {
					// Other values
					List<IGefReplayHelper> list = client
							.getProcessors(IGefReplayHelper.class);
					for (IGefReplayHelper helper : list) {
						IFigure result = helper.getFigure(path, part);
						if (result != null) {
							return result;
						}
					}
				}
			}
		}
		IFigure te = getFigureFilter(figure, copyList, false);
		if (te == null) {
			te = getFigureFilter(figure, copyList, true);
		}
		return te;
	}

	@SuppressWarnings("rawtypes")
	private IFigure getFigureFilter(IFigure figure, List<Integer> copyList,
			boolean filter) {
		IFigure te = figure;
		for (Integer integer : copyList) {
			List children = filter ? filterFigures(te.getChildren()) : te
					.getChildren();
			if (children.size() <= integer.intValue()) {
				// Compatibility
				children = te.getChildren();
				if (children.size() <= integer.intValue()) {
					return null;
				}
			}
			te = (IFigure) children.get(integer.intValue());
		}
		return te;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List filterFigures(List children) {
		List result = new ArrayList();
		for (Object object : children) {
			if (object instanceof IFigure) {
				if (!((IFigure) object).isVisible()) {
					continue;
				}
			}
			result.add(object);
		}
		return result;
	}

	private Response handleGetTextCommand(GetText command) {
		return null;
	}

	private Response handleFigureResize(ResizeFigure command) {
		FigureUIElement element = getMapper().get(command.getElement());
		EditPart part = element.getPart();
		if (part instanceof GraphicalEditPart) {
			GraphicalEditPart gep = (GraphicalEditPart) part;
			Rectangle bounds = gep.getFigure().getBounds();
			ChangeBoundsRequest request = new ChangeBoundsRequest(
					RequestConstants.REQ_RESIZE);
			int x = command.getX();
			int y = command.getY();
			request.setSizeDelta(new Dimension(
					x != -1 ? (x - bounds.width) : 0,
					y != -1 ? (y - bounds.height) : 0));
			request.setEditParts(part);
			org.eclipse.gef.commands.Command cmd = part.getCommand(request);
			if (cmd != null && cmd.canExecute()) {
				element.getDiagram().getDomain().getCommandStack().execute(cmd);
			}
			return RawFactory.eINSTANCE.createResponse();
		}
		return returnFailed(RawFactory.eINSTANCE.createResponse());
	}

	private Response handleFigureMove(MoveFigure command) {
		FigureUIElement element = getMapper().get(command.getElement());
		EditPart part = element.getPart();
		if (part instanceof GraphicalEditPart) {
			GraphicalEditPart gep = (GraphicalEditPart) part;
			Rectangle bounds = gep.getFigure().getBounds();
			ChangeBoundsRequest request = new ChangeBoundsRequest(
					RequestConstants.REQ_MOVE);
			int x = command.getX();
			int y = command.getY();
			request.setMoveDelta(new Point(x != -1 ? x - bounds.x : 0,
					y != -1 ? y - bounds.y : 0));
			request.setEditParts(part);
			org.eclipse.gef.commands.Command cmd = part.getCommand(request);
			if (cmd != null && cmd.canExecute()) {
				element.getDiagram().getDomain().getCommandStack().execute(cmd);
			}
			// part.
			return RawFactory.eINSTANCE.createResponse();
		}
		return returnFailed(RawFactory.eINSTANCE.createResponse());

	}

	private Response handleCommitDirectEdit(CommitDirectEdit command) {
		// SWTUI
		SWTUIElement element = SWTElementMapper.getMapper(id).get(
				command.getElement());
		DirectEditorContainer[] editors = TeslaDirectEditManager.getInstance()
				.getEditors();
		if (element != null) {
			if (element instanceof DiagramViewerUIElement) {
				// Deactive all direct edits
				for (final DirectEditorContainer container : editors) {
					CellEditor editor = container.getCellEditor();
					if (editor != null) {
						TeslaDirectEditManager.getInstance().forceRemove(
								container.getManager());
						if (container.getCellEditor().getControl() != null) {
							getPlayer().exec("Commit direct edit",
									new Runnable() {

										@Override
										public void run() {
											container.commit();
										}
									});
						}
					}
				}
				return RawFactory.eINSTANCE.createResponse();
			} else {
				for (final DirectEditorContainer container : editors) {
					CellEditor editor = container.getCellEditor();
					if (editor != null) {
						Control control = editor.getControl();
						if (control.equals(element.widget)) {
							TeslaDirectEditManager.getInstance().forceRemove(
									container.getManager());
							getPlayer().exec("Commit direct edit",
									new Runnable() {

										@Override
										public void run() {
											container.commit();
										}
									});
							return RawFactory.eINSTANCE.createResponse();
						}
					}
				}
			}
		} else {
			// Deactive all direct edits
			for (DirectEditorContainer container : editors) {
				CellEditor editor = container.getCellEditor();
				if (editor != null) {
					TeslaDirectEditManager.getInstance().forceRemove(
							container.getManager());
					container.commit();
				}
			}
			return RawFactory.eINSTANCE.createResponse();
		}
		return null;
	}

	private Response handleCancelDirectEdit(CancelDirectEdit command) {
		// SWTUI
		SWTUIElement element = SWTElementMapper.getMapper(id).get(
				command.getElement());
		DirectEditorContainer[] editors = TeslaDirectEditManager.getInstance()
				.getEditors();
		if (element != null) {
			if (element instanceof DiagramViewerUIElement) {
				// Deactive all direct edits
				for (final DirectEditorContainer container : editors) {
					CellEditor editor = container.getCellEditor();
					if (editor != null) {
						TeslaDirectEditManager.getInstance().forceRemove(
								container.getManager());
						if (container.getCellEditor().getControl() != null) {
							getPlayer().exec("Cancel direct edit",
									new Runnable() {

										@Override
										public void run() {
											container.bringDown();
										}
									});
						}
					}
				}
				return RawFactory.eINSTANCE.createResponse();
			} else {
				for (final DirectEditorContainer container : editors) {
					CellEditor editor = container.getCellEditor();
					if (editor != null) {
						Control control = editor.getControl();
						if (control.equals(element.widget)) {
							TeslaDirectEditManager.getInstance().forceRemove(
									container.getManager());
							getPlayer().exec("Cancel direct edit",
									new Runnable() {

										@Override
										public void run() {
											container.bringDown();
										}
									});
							return RawFactory.eINSTANCE.createResponse();
						}
					}
				}
			}
		} else {
			// Deactive all direct edits
			for (DirectEditorContainer container : editors) {
				CellEditor editor = container.getCellEditor();
				if (editor != null) {
					TeslaDirectEditManager.getInstance().forceRemove(
							container.getManager());
					container.bringDown();
				}
			}
			return RawFactory.eINSTANCE.createResponse();
		}
		return null;
	}

	private Response handleActivateDirectEdit(ActivateDirectEdit command,
			final IElementProcessorMapper mapper) {
		SWTUIElement swtuiElement = SWTElementMapper.getMapper(id).get(
				command.getElement());
		if (swtuiElement != null
				&& swtuiElement instanceof DiagramViewerUIElement) {
			DiagramViewerUIElement diagram = (DiagramViewerUIElement) swtuiElement;

			final GraphicalViewer viewer = diagram.getViewer();

			// Assume it is always graphical edit part
			if (command.getPartPath().size() > 0) {
				final GraphicalEditPart part = getEditPartFromPath(
						command.getPartPath(), viewer);
				if (part == null) {
					return createFailed(TeslaGefMessages.GefProcessor_CannotFindEditPart);
				}
				performDirectEdit(mapper, (FigureCanvas) viewer.getControl(),
						part);
			}
			return RawFactory.eINSTANCE.createResponse();
		} else {

			FigureUIElement figure = getMapper().get(command.getElement());
			if (figure == null) {
				return returnFailed(null);
			}
			EditPart part = figure.getPart();
			SelectResponse response = performDirectEdit(mapper, figure
					.getDiagram().getCanvas(), part);
			if (response != null) {
				return response;
			}
			return returnFailed(null);
		}
	}

	private SelectResponse performDirectEdit(
			final IElementProcessorMapper mapper, final Canvas canvas,
			final EditPart part) {
		EditPolicy policy = part.getEditPolicy(EditPolicy.DIRECT_EDIT_ROLE);
		DirectEditorContainer[] editorContainers = TeslaDirectEditManager
				.getInstance().getEditors();
		boolean activeDE = false;
		Object partModel = part.getModel();
		for (DirectEditorContainer directEditorContainer : editorContainers) {
			CellEditor cellEditor = directEditorContainer.getCellEditor();
			if ((cellEditor != null && cellEditor.getControl() != null && cellEditor
					.getControl().isDisposed()) || cellEditor == null) {
				continue;
			}

			DirectEditManager manager = directEditorContainer.getManager();
			EditPart sourcePart = getSourceEditPart(manager);
			Object model = sourcePart != null ? sourcePart.getModel() : null;
			if (sourcePart != null
					&& sourcePart.getParent().equals(part.getParent())) {
				Object model2 = sourcePart.getParent().getModel();
				Object model3 = part.getParent().getModel();
				if (model2 != null && model2.equals(model3)) {
					activeDE = true;
					break;
				}
			}
			if (sourcePart != null
					&& sourcePart.equals(part)
					|| (model != null && partModel != null
							&& model instanceof EObject
							&& partModel instanceof EObject && EcoreUtil
									.equals((EObject) model, (EObject) partModel))) {
				activeDE = true;
				break;
			} else {
				if (cellEditor.getControl().getParent().equals(canvas)) {
					directEditorContainer.bringDown();
				}
			}
		}
		if (policy != null) {
			final GefProcessor This = this;
			if (!activeDE) {
				getPlayer().exec("Perform direct edit", new Runnable() {

					@Override
					public void run() {
						// Just force request
						Request request = new Request(
								RequestConstants.REQ_DIRECT_EDIT);
						part.performRequest(request);
						DirectEditorContainer[] editors = TeslaDirectEditManager
								.getInstance().getEditors();
						for (DirectEditorContainer container : editors) {
							CellEditor editor = container.getCellEditor();
							if (editor != null && editor.getControl() != null) {
								Control control = editor.getControl();
								if (!control.isDisposed()
										&& control.getParent().equals(canvas)) {
									TeslaDirectEditManager.getInstance()
											.forceEdit(container.getManager());
									Element element = SWTElementMapper
											.getMapper(id).get(
													getPlayer().wrap(control));
									mapper.map(element, This);
									mapper.map(element, client
											.getProcessor(SWTUIProcessor.class));
									// SelectResponse response =
									// ProtocolFactory.eINSTANCE
									// .createSelectResponse();
									// response.getElements().add(element);
									// return response;
								}
							}
						}
					}
				});
			}
		} else {
			if (!activeDE) {
				getPlayer().exec("Perform direct edit", new Runnable() {

					@Override
					public void run() {
						// Just force request
						Request request = new Request(
								RequestConstants.REQ_DIRECT_EDIT);
						part.performRequest(request);
					}
				});
			}
		}
		SelectResponse response = ProtocolFactory.eINSTANCE
				.createSelectResponse();
		return response;
	}

	private EditPart getSourceEditPart(DirectEditManager manager) {
		return TeslaGefAccess.getSourceEditPart(manager);
	}

	private Response handleSetFigureSelection(SetFigureSelection command) {
		// TODO Auto-generated method stub
		return null;
	}

	private Response handleParentCommand(ElementCommand command,
			IElementProcessorMapper mapper) {
		FigureUIElement editPart = getMapper().get(command.getElement());
		if (editPart != null) {
			ParentResponse response = ProtocolFactory.eINSTANCE
					.createParentResponse();
			EditPart parent = editPart.getPart().getParent();
			if (editPart.getDiagram().getViewer().getRootEditPart()
					.equals(parent)) {
				Element element = SWTElementMapper.getMapper(id).get(
						editPart.getDiagram());
				response.setParent(element);
				mapper.map(element, this);

			} else {
				Element element = getMapper().get(
						new FigureUIElement(parent, editPart.getDiagram()));
				response.setParent(element);
				mapper.map(element, this);
			}
			return response;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private Response handleChildrenCommand(ElementCommand command,
			IElementProcessorMapper mapper) {
		Element element = command.getElement();

		SWTUIElement uiElement = SWTElementMapper.getMapper(id).get(element);
		FigureUIElement editPart = getMapper().get(element);
		if (uiElement != null && uiElement instanceof DiagramViewerUIElement) {
			DiagramViewerUIElement e = (DiagramViewerUIElement) uiElement;
			List children = e.getViewer().getContents().getChildren();
			ChildrenResponse response = ProtocolFactory.eINSTANCE
					.createChildrenResponse();
			for (Object object : children) {
				if (object instanceof EditPart) {
					Element child = getMapper().get(
							new FigureUIElement((EditPart) object, e));
					response.getChildren().add(child);
					mapper.map(child, this);
				}
			}
			return response;
		}
		if (editPart != null) {
			List children = editPart.getPart().getChildren();
			ChildrenResponse response = ProtocolFactory.eINSTANCE
					.createChildrenResponse();
			for (Object object : children) {
				if (object instanceof EditPart) {
					Element child = getMapper().get(
							new FigureUIElement((EditPart) object, editPart
									.getDiagram()));
					response.getChildren().add(child);
					mapper.map(child, this);
				}
			}
			return response;
		}

		return null;
	}

	private Response handleCreateFigure(CreateFigure command,
			IElementProcessorMapper mapper) {
		SWTUIElement element = SWTElementMapper.getMapper(id).get(
				command.getElement());
		CreateFigureResponse response = DiagramFactory.eINSTANCE
				.createCreateFigureResponse();

		final DiagramViewerUIElement viewer;
		FigureUIElement e = getMapper().get(command.getElement());
		if (element instanceof DiagramViewerUIElement) {
			viewer = (DiagramViewerUIElement) element;
		} else if (e != null) {
			viewer = e.getDiagram();
		} else {
			return returnFailed(response);
		}

		if (viewer == null || viewer.isDisposed()) {
			return returnFailed(response);
		}
		List<ToolEntry> list = viewer.listToolbarEntries();
		String pattern = command.getPattern();
		ToolEntry requiredEntry = searchToolEntry(list, pattern);
		if (requiredEntry == null) {
			return returnFailed(response);
		}
		Tool tool = requiredEntry.createTool();
		if (!(tool instanceof CreationTool)) {
			return returnFailed(response);
		}
		CreationTool createTool = (CreationTool) tool;

		EditDomain domain = viewer.getDomain();

		Canvas canvas = viewer.getCanvas();
		Display display = canvas.getDisplay();
		EventDispatcher dispatcher = viewer.getDispatcher();
		int x = command.getX();
		int y = command.getY();

		// RootEditPart rootEditPart = viewer.getViewer().getRootEditPart();

		EditPart performAt = viewer.getViewer().findObjectAt(new Point(x, y));
		if (element instanceof DiagramViewerUIElement) {
			// if (beforeAt != null) { // Failed to create figure over some
			// other
			// // figure.
			// response.setStatus(ResponseStatus.FAILED);
			// return response;
			// }
			// Correct location to correct position relative to viewport
			if (canvas instanceof FigureCanvas) {
				Viewport viewport = ((FigureCanvas) canvas).getViewport();
				Point loc = viewport.getViewLocation();
				x -= loc.x;
				y -= loc.y;
			}
			performAt = viewer.getViewer().getRootEditPart();
		} else {
			if (e.getPart() instanceof GraphicalEditPart) {
				GraphicalEditPart gep = (GraphicalEditPart) e.getPart();
				Rectangle bounds = gep.getFigure().getBounds();
				x += bounds.x + 1;
				y += bounds.y + 1;
			}
			performAt = e.getPart();
		}

		// Correct location to relative to
		EditPartChangeCollector collector = new EditPartChangeCollector();
		RootEditPart root = viewer.getViewer().getRootEditPart();
		collector.associate(root);

		// Firstly try to create using requests
		boolean fine = false;
		try {
			fine = TeslaGefAccess.createTargetRequest(createTool, x, y,
					performAt);
		} catch (Throwable e1) {
			e1.printStackTrace();
			fine = false;
		}
		if (!fine || collector.getAddedParts().isEmpty()) {
			domain.setActiveTool(createTool);
			dispatcher.dispatchMouseMoved(new MouseEvent(getPlayer()
					.getEvents().createMouseEvent(display, canvas, x, y, 0, 0,
							0)));
			dispatcher.dispatchMousePressed(new MouseEvent(getPlayer()
					.getEvents().createMouseEvent(display, canvas, x, y, 1,
							SWT.BUTTON1, 1)));
			dispatcher.dispatchMouseReleased(new MouseEvent(getPlayer()
					.getEvents().createMouseEvent(display, canvas, x, y, 1,
							SWT.BUTTON1, 1)));
			domain.setActiveTool(domain.getDefaultTool());
		}
		collector.deAssociate();
		// We need to deassociate all direct edit's
		List<EditPart> addedParts = collector.getAddedParts();
		for (EditPart editPart : addedParts) {
			FigureUIElement ee = new FigureUIElement(editPart, viewer);
			Element fig = getMapper().get(ee);
			mapper.map(fig, this);
			response.getFigure().add(fig);
		}
		viewer.getViewer().deselectAll();
		getPlayer().exec("Hide direct editors", new Runnable() {

			@Override
			public void run() {
				DirectEditorContainer[] editors = TeslaDirectEditManager
						.getInstance().getEditors();
				for (DirectEditorContainer container : editors) {
					CellEditor editor = container.getCellEditor();
					if (editor != null) {
						Control control = editor.getControl();
						if (control != null
								&& control.getParent().equals(
										viewer.getCanvas())) {
							// System.out.println("!");
							container.bringDown();
						}
					}
				}
			}
		});
		return response;
	}

	private Response handleCreateConnection(CreateConnection command,
			IElementProcessorMapper mapper) {
		SWTUIElement element = SWTElementMapper.getMapper(id).get(
				command.getElement());
		CreateConnectionResponse response = DiagramFactory.eINSTANCE
				.createCreateConnectionResponse();

		final DiagramViewerUIElement viewer;
		FigureUIElement e = getMapper().get(command.getElement());

		FigureUIElement source = getMapper().get(command.getFrom());
		FigureUIElement target = getMapper().get(command.getTo());

		if (element instanceof DiagramViewerUIElement) {
			viewer = (DiagramViewerUIElement) element;
		} else if (e != null) {
			viewer = e.getDiagram();
		} else {
			return returnFailed(response);
		}

		if (viewer == null || viewer.isDisposed()) {
			return returnFailed(response);
		}
		List<ToolEntry> list = viewer.listToolbarEntries();
		String pattern = command.getPattern();
		ToolEntry requiredEntry = searchToolEntry(list, pattern);
		if (requiredEntry == null) {
			return returnFailed(response);
		}
		Tool tool = requiredEntry.createTool();
		if (!(tool instanceof ConnectionCreationTool)) {
			return returnFailed(response);
		}
		ConnectionCreationTool createTool = (ConnectionCreationTool) tool;

		EditDomain domain = viewer.getDomain();

		// FigureCanvas canvas = viewer.getCanvas();
		// Display display = canvas.getDisplay();
		// EventDispatcher dispatcher = viewer.getDispatcher();

		// Correct location to relative to
		EditPartChangeCollector collector = new EditPartChangeCollector();
		RootEditPart root = viewer.getViewer().getRootEditPart();
		collector.associate(root);

		EditPart performAt = target.getPart();

		// Firstly try to create using requests
		boolean fine = false;
		try {
			fine = TeslaGefAccess.createTargetRequest(source, target,
					createTool, domain, performAt);
		} catch (Throwable e1) {
			e1.printStackTrace();
			fine = false;
		}
		if (!fine || collector.getAddedConnections().isEmpty()) {
			// TODO: Add fail safe method using drag from and to
			// domain.setActiveTool(createTool);
			//
			// EditPart sourcePart = source.getPart();
			// EditPart targetPart = target.getPart();
			//
			// domain.setActiveTool(domain.getDefaultTool());
		}
		collector.deAssociate();
		// We need to deassociate all direct edit's
		List<ConnectionEditPart> addedParts = collector.getAddedConnections();
		for (EditPart editPart : addedParts) {
			FigureUIElement ee = new FigureUIElement(editPart, viewer);
			Element fig = getMapper().get(ee);
			mapper.map(fig, this);
			response.getFigure().add(fig);
		}
		viewer.getViewer().deselectAll();
		getPlayer().exec("Hide direct editors", new Runnable() {

			@Override
			public void run() {
				DirectEditorContainer[] editors = TeslaDirectEditManager
						.getInstance().getEditors();
				for (DirectEditorContainer container : editors) {
					CellEditor editor = container.getCellEditor();
					if (editor != null) {
						Control control = editor.getControl();
						if (control != null
								&& control.getParent().equals(
										viewer.getCanvas())) {
							// System.out.println("!");
							container.bringDown();
						}
					}
				}
			}
		});
		return response;
	}

	private Response returnFailed(Response response) {
		if (response == null) {
			response = RawFactory.eINSTANCE.createResponse();
		}
		response.setStatus(ResponseStatus.FAILED);
		return response;
	}

	private ToolEntry searchToolEntry(List<ToolEntry> list, String pattern) {
		ToolEntry requiredEntry = null;
		for (ToolEntry toolEntry : list) {
			String label = toolEntry.getLabel();
			if (label.equals(pattern) || label.matches(pattern)) {
				requiredEntry = toolEntry;
				break;
			}
		}
		return requiredEntry;
	}

	@Override
	public boolean isSelectorSupported(String kind) {
		if (kind.equals(ElementKind.DiagramViewer.name())
				|| kind.equals(ElementKind.DiagramFigure.name())
				|| kind.equals(ElementKind.PaletteViewer.name())
				|| kind.equals(ElementKind.PaletteEntry.name())) {
			return true;
		}
		return false;
	}

	@Override
	public SelectResponse select(SelectCommand cmd, ElementGenerator generator,
			IElementProcessorMapper mapper) {
		String kind = cmd.getData().getKind();
		if (kind.equals(ElementKind.DiagramViewer.name())) {
			return selectDiagramViewer(cmd);
		} else if (kind.equals(ElementKind.PaletteViewer.name())) {
			return selectPalette(cmd);
		} else if (kind.equals(ElementKind.DiagramFigure.name())) {
			return selectDiagramFigure(cmd);
		} else if (kind.equals(ElementKind.PaletteEntry.name())) {
			return selectPaletteEntry(cmd);
		}
		return createFailedSelect(TeslaGefMessages.GefProcessor_CannotSelect);
	}

	private SelectResponse selectPaletteEntry(SelectCommand cmd) {
		Element parent = cmd.getData().getParent();
		DiagramViewerUIElement uiEl = (DiagramViewerUIElement) SWTElementMapper
				.getMapper(id).get(parent);
		String pattern = cmd.getData().getPattern();
		if (uiEl != null) {
			GraphicalViewer viewer = uiEl.getViewer();
			if (viewer instanceof PaletteViewer) {
				PaletteViewer palette = (PaletteViewer) viewer;

				List<PaletteEntry> entries = new ArrayList<PaletteEntry>();
				GefProcessor.fillPaletteEntries(entries,
						palette.getPaletteRoot());
				PaletteEntry requiredEntry = null;
				int index = 0;
				Integer dataIndex = cmd.getData().getIndex();
				for (PaletteEntry paletteEntry : entries) {
					String label = paletteEntry.getLabel();
					if ((label != null && label.equals(pattern))
							|| PlayerTextUtils.safeMatches(label, pattern)) {
						if ((dataIndex != null && dataIndex == index)
								|| dataIndex == null) {
							requiredEntry = paletteEntry;
							break;
						} else {
							index++;
						}
					}
				}
				boolean patternExists = (pattern != null && pattern.length() > 0);
				if (requiredEntry != null || !patternExists) {
					EditPart part = findPaletteEntry(palette.getRootEditPart(),
							requiredEntry);
					if (part != null) {
						Element element = getMapper().get(
								new PaletteUIElement(part, uiEl));
						return toResponse(element);
					}
				}
			}
		}
		return createFailedSelect(NLS.bind(
				TeslaGefMessages.GefProcessor_CannotFindPaletteTool, pattern));
	}

	@SuppressWarnings("rawtypes")
	public static void fillPaletteEntries(List<PaletteEntry> list,
			PaletteContainer container) {
		List children = container.getChildren();
		for (Object object : children) {
			if (object instanceof PaletteEntry
					&& ((PaletteEntry) object).isVisible()) {
				if (object instanceof PaletteContainer) {
					list.add((PaletteEntry) object);
					fillPaletteEntries(list, (PaletteContainer) object);
				} else {
					list.add((PaletteEntry) object);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private EditPart findPaletteEntry(EditPart entry, PaletteEntry pattern) {
		// eclipse 3.4 support: PaletteEditPart in different packages
		if (pattern == null) {
			return entry;
		}
		if (TeslaGefAccess.isPalleteEditPart(entry)) {
			Object model = entry.getModel();
			if (model != null && model instanceof PaletteEntry) {
				if (model.equals(pattern)) {
					return entry;
				}
			}
		}
		List children = entry.getChildren();
		for (Object object : children) {
			if (object instanceof EditPart) {
				EditPart res = findPaletteEntry((EditPart) object, pattern);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private SelectResponse selectDiagramFigure(SelectCommand cmd) {
		Element parent = cmd.getData().getParent();
		FigureUIElement figureParent = getMapper().get(parent);
		DiagramViewerUIElement uiEl = getDiagram(parent);
		if (figureParent != null) {
			SelectData data = cmd.getData();
			String classPattern = data.getClassPattern();
			DiagramViewerUIElement diagram = figureParent.getDiagram();
			GraphicalEditPart part = (GraphicalEditPart) figureParent.getPart();
			if (classPattern != null) {
				return selectEditPartByClassPattern(cmd, classPattern, diagram,
						part);
			}
			EList<String> path = data.getPath();
			if (path.size() == 2) {
				String p0 = path.get(0);
				String p1 = path.get(1);
				EList<Integer> indexes = data.getIndexes();
				if (p0.equals("editpart") && p1.equals("address")) {
					GraphicalEditPart editPart = (GraphicalEditPart) getPart(
							indexes, part);
					return selectEditPart(
							uiEl,
							"by address: " + Arrays.toString(indexes.toArray()),
							editPart);
				}
				if (p0.equals("editpart") && p1.equals("name")) {
					List children = part.getChildren();
					return selectByName(uiEl, data, children);
				}
				if (p0.equals("editpart") && p1.equals("text")) {
					List children = part.getChildren();
					return selectByText(uiEl, data, children);
				}
				if (p0.equals("editpart") && p1.equals("customId")) {
					if (!TeslaFeatures.isIdentifyMethodsProvided()) {
						String failMessage = NLS.bind(TeslaGefMessages.GefProcessor_CannotFindEditPart_DetailedMsg,
								TeslaMessages.TeslaFeatures_IdentifyMethodsNotProvided);
						return createFailedSelect(failMessage);
					}
					List children = part.getChildren();
					return selectByCustomId(uiEl, data, children);
				}
				if (p0.equals("editpart") && p1.equals("classname")) {
					List children = part.getChildren();
					return selectByClassName(uiEl, data, children);
				}
				if (p0.equals("handle") && p1.equals("class")) {
					return selectHandle(part, data.getPattern(),
							data.getIndex());
				}
				if (p0.equals("editpart.feature")) {
					List children = part.getChildren();
					return selectByFeature(p1, uiEl, data, children);
				}
				if (p0.equals("editpart.source.connection")
						&& p1.equals("address")) {
					List connections = part.getSourceConnections();
					if (connections.size() <= indexes.get(0)) {
						return createFailedSelect(TeslaGefMessages.GefProcessor_CannotSelect);
					}
					GraphicalEditPart editPart = (GraphicalEditPart) connections
							.get(indexes.get(0));
					return selectEditPart(
							uiEl,
							"by address: " + Arrays.toString(indexes.toArray()),
							editPart);
				}
				if (p0.equals("editpart.target.connection")
						&& p1.equals("address")) {
					List connections = part.getTargetConnections();
					if (connections.size() <= indexes.get(0)) {
						return createFailedSelect(TeslaGefMessages.GefProcessor_CannotSelect);
					}
					GraphicalEditPart editPart = (GraphicalEditPart) connections
							.get(indexes.get(0));
					return selectEditPart(
							uiEl,
							"by address: " + Arrays.toString(indexes.toArray()),
							editPart);
				}
				if (p0.equals("rawFigure") && p1.equals("address")) {
					return selectRawFigure(indexes,
							getFigure(indexes, part.getFigure(), part), part,
							diagram.getViewer());
				}
			}
		}
		if (uiEl != null) {
			SelectData data = cmd.getData();
			EList<String> path = data.getPath();
			if (path.size() == 2) {
				String p0 = path.get(0);
				String p1 = path.get(1);
				if (p0.equals("editpart") && p1.equals("address")) {
					GraphicalEditPart part = getEditPartFromPath(
							data.getIndexes(), uiEl.getViewer());
					return selectEditPart(
							uiEl,
							"by address: "
									+ Arrays.toString(data.getIndexes()
											.toArray()),
							part);
				}
				if (p0.equals("editpart") && p1.equals("name")) {
					List children = uiEl.getViewer().getRootEditPart()
							.getChildren();
					return selectByName(uiEl, data, children);
				}
				if (p0.equals("editpart") && p1.equals("text")) {
					List children = uiEl.getViewer().getRootEditPart()
							.getChildren();
					return selectByText(uiEl, data, children);
				}
				if (p0.equals("handle") && p1.equals("class")) {
					return selectHandle((GraphicalEditPart) uiEl.getViewer()
							.getRootEditPart(), data.getPattern(),
							data.getIndex());
				}
				if (p0.equals("editpart.feature")) {
					List children = uiEl.getViewer().getRootEditPart()
							.getChildren();
					return selectByFeature(p1, uiEl, data, children);
				}
				if (p0.equals("editpart") && p1.equals("customId")) {
					if (!TeslaFeatures.isIdentifyMethodsProvided()) {
						String failMessage = NLS.bind(TeslaGefMessages.GefProcessor_CannotFindEditPart_DetailedMsg,
								TeslaMessages.TeslaFeatures_IdentifyMethodsNotProvided);
						return createFailedSelect(failMessage);
					}
					List children = uiEl.getViewer().getRootEditPart()
							.getChildren();
					return selectByCustomId(uiEl, data, children);
				}
				if (p0.equals("editpart") && p1.equals("classname")) {
					List children = uiEl.getViewer().getRootEditPart()
							.getChildren();
					return selectByClassName(uiEl, data, children);
				}
				if (p0.equals("rawFigure") && p1.equals("address")) {
					IFigure figure = getFigure(data.getIndexes(), uiEl
							.getFigureContents(), null);
					return selectRawFigure(data.getIndexes(), figure, null,
							uiEl.getViewer());
				}
			}
		} else {
			SelectData data = cmd.getData();
			SWTUIElement canvasElement = SWTElementMapper.getMapper(id).get(
					parent);
			if (canvasElement != null) {
				if (!(canvasElement.unwrap() instanceof FigureCanvas)) {
					return createFailedSelect(TeslaGefMessages.GefProcessor_CannotSelect);
				}
				return selectCanvasFigure(data, canvasElement);
			}
		}
		return createFailedSelect(TeslaGefMessages.GefProcessor_CannotSelect);
	}

	private SelectResponse selectHandle(GraphicalEditPart part, String pattern,
			Integer index) {
		EditPartViewer viewer = part.getViewer();

		List<AbstractHandle> handles = findAbstractHandles((GraphicalViewer) viewer);
		int ind = 0;
		AbstractHandle result = null;
		for (AbstractHandle object : handles) {
			EditPart editPart = TeslaGefAccess.getEditPart(object);
			if (part.equals(editPart)) {
				if (object.getClass().getSimpleName().equals(pattern)) {
					if (index == null || index.intValue() == ind) {
						result = object;
						break;
					}
					ind++;
				}
			}
		}
		if (result != null) {
			Element element = getRawMapper().get(
					new RawFigureUIElement(viewer.getRootEditPart(), result,
							(GraphicalViewer) viewer));
			return toResponse(element);
		}
		return createFailedSelect(NLS.bind(
				TeslaGefMessages.GefProcessor_CannotFindHandle, pattern, ind));
	}

	public static List<AbstractHandle> findAbstractHandles(
			GraphicalViewer viewer) {
		GraphicalEditPart part = (GraphicalEditPart) viewer.getRootEditPart();
		IFigure figure = part.getFigure();
		return traverseFigures(figure);
	}

	@SuppressWarnings("rawtypes")
	public static List<AbstractHandle> traverseFigures(IFigure figure) {
		List<AbstractHandle> result = new ArrayList<AbstractHandle>();
		List children = figure.getChildren();
		for (Object object : children) {
			if (object instanceof AbstractHandle) {
				result.add((AbstractHandle) object);
			}
			if (object instanceof IFigure) {
				result.addAll(traverseFigures((IFigure) object));
			}
		}
		return result;
	}

	private SelectResponse selectBy(DiagramViewerUIElement uiEl,
			SelectData data, List<EditPart> children, String selectorName,
			Function<EditPart, String> toStr) {
		int index = data.getIndex() == null ? 0 : data.getIndex();
		String pattern = PlayerTextUtils.unifyMultilines(data.getPattern());
		EditPart result = find(
				ListUtil.filter(children, new Predicate<EditPart>() {

					@Override
					public boolean apply(EditPart input) {
						return input instanceof GraphicalEditPart;
					}
				}), pattern, index, toStr);

		String indexInfo = data.getIndex() == null ? "" : String.format(
				" by index: %d", data.getIndex());
		return selectEditPart(uiEl,
				String.format("by %s: %s%s", selectorName, pattern, indexInfo),
				(GraphicalEditPart) result);

	}

	/**
	 * Attempts to find value by exact match or regex
	 *
	 * @param values
	 * @param pattern
	 * @param index
	 * @return
	 */
	private static final <T> T find(Collection<T> values, final String pattern,
			int index, final Function<T, String> toStr) {
		Predicate<T> exact = new Predicate<T>() {

			@Override
			public boolean apply(T input) {
				return pattern.equals(PlayerTextUtils.unifyMultilines(toStr
						.apply(input)));
			}
		};

		Predicate<T> regex = new Predicate<T>() {

			@Override
			public boolean apply(T input) {
				String str = PlayerTextUtils.unifyMultilines(toStr.apply(input));
				return str != null && PlayerTextUtils.safeMatches(str, pattern);
			}
		};

		// usual match
		Collection<T> matches = ListUtil.filter(values, exact);
		if (matches.isEmpty()) {
			matches = ListUtil.filter(values, regex);
		}

		if (matches.isEmpty()) {
			return null;
		}

		Iterator<T> iterator = matches.iterator();
		while (index > 0) {
			if (!iterator.hasNext()) {
				return null;
			}
			iterator.next();
			index--;
		}
		try {
			return iterator.next();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SelectResponse selectByName(DiagramViewerUIElement uiEl,
			SelectData data, List children) {
		return selectBy(uiEl, data, children, "name",
				new Function<EditPart, String>() {

					@Override
					public String apply(EditPart input) {
						return getPartEMFName(getModelObject(input), "name",
								true);
					}
				});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SelectResponse selectByText(DiagramViewerUIElement uiEl,
			SelectData data, List children) {
		return selectBy(uiEl, data, children, "text",
				new Function<EditPart, String>() {

					@Override
					public String apply(EditPart input) {
						return getPartText(getMappedModel(input));
					}
				});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SelectResponse selectByCustomId(DiagramViewerUIElement uiEl,
			SelectData data, List children) {
		return selectBy(uiEl, data, children, "customId",
				new Function<EditPart, String>() {

					@Override
					public String apply(EditPart input) {
						return getPartCustomId(input);
					}
				});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SelectResponse selectByClassName(DiagramViewerUIElement uiEl,
			SelectData data, List children) {
		return selectBy(uiEl, data, children, "classname",
				new Function<EditPart, String>() {

					@Override
					public String apply(EditPart input) {
						return getPartClassName(input);
					}
				});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SelectResponse selectByFeature(final String feature,
			DiagramViewerUIElement uiEl, SelectData data, List children) {
		return selectBy(uiEl, data, children,
				String.format("feature %s", feature),
				new Function<EditPart, String>() {

					@Override
					public String apply(EditPart input) {
						return getPartEMFName(getModelObject(input), feature,
								false);
					}
				});
	}

	public static String getPartEMFName(EObject modelObject, String feature,
			boolean noCase) {
		if (modelObject != null) {
			EClass eClass = modelObject.eClass();
			EList<EAttribute> attributes = eClass.getEAllAttributes();
			for (EAttribute eAttribute : attributes) {
				String fName = eAttribute.getName();
				if ((noCase && fName.equalsIgnoreCase(feature))
						|| (!noCase && fName.equals(feature))) {
					Object value = modelObject.eGet(eAttribute);
					if (value != null && value instanceof String) {
						return (String) value;
					}
				}
			}
		}
		return null;
	}

	private EObject getModelObject(EditPart part) {
		List<IGefReplayHelper> list = client
				.getProcessors(IGefReplayHelper.class);
		Object model = part.getModel();
		EObject modelObject = null;
		if (model != null && model instanceof EObject) {
			modelObject = (EObject) model;
		}
		for (IGefReplayHelper helper : list) {
			EObject emfModel = helper.getEMFMode(part);
			if (emfModel != null) {
				modelObject = emfModel;
			}
		}
		return modelObject;
	}

	@Override
	public EObject getElementModel(Element element) {
		final FigureUIElement uiElement = getMapper().get(element);
		final RawFigureUIElement figureUiElement = getFigureMapper().get(
				element);
		if (uiElement == null && figureUiElement == null) {
			// return assertResponse(ResponseStatus.FAILED,
			// "Widget are not found");
			return null;
		}
		org.eclipse.rcptt.tesla.core.ui.Widget model;
		if (uiElement != null) {
			model = getMappedModel(uiElement.getPart());
		} else {
			model = GefModelMapper.figureMap(figureUiElement.getFigure());
		}
		return model;
	}

	@SuppressWarnings("rawtypes")
	private SelectResponse selectEditPartByClassPattern(SelectCommand cmd,
			String classPattern, DiagramViewerUIElement diagram,
			GraphicalEditPart part) {
		List children = getPartChildren(part.getChildren());
		Integer intIndex = cmd.getData().getIndex();
		int i = 0;
		for (Object object : children) {
			if (object instanceof EditPart) {
				String className = object.getClass().getSimpleName();
				if (className.equals(classPattern)
						|| className.matches(".*" + classPattern + ".*")) {
					if (intIndex == null || i == intIndex.intValue()) {
						return toResponse(getMapper()
								.get(new FigureUIElement((EditPart) object,
										diagram)));
					}
					i++;
				}
			}
		}
		// Compatibility
		children = getPartChildren(part.getChildren());
		intIndex = cmd.getData().getIndex();
		i = 0;
		for (Object object : children) {
			if (object instanceof EditPart) {
				String className = object.getClass().getSimpleName();
				if (className.equals(classPattern)
						|| className.matches(".*" + classPattern + ".*")) {
					if (intIndex == null || i == intIndex.intValue()) {
						return toResponse(getMapper()
								.get(new FigureUIElement((EditPart) object,
										diagram)));
					}
					i++;
				}
			}
		}
		return createFailedSelect(NLS.bind(
				TeslaGefMessages.GefProcessor_CannotFindEditPartByClass,
				classPattern));
	}

	private SelectResponse selectCanvasFigure(SelectData data,
			SWTUIElement canvasElement) {
		FigureCanvas figureCanvas = (FigureCanvas) canvasElement.unwrap();
		IFigure figure = getFigure(data.getIndexes(),
				figureCanvas.getContents(), null);
		if (figure != null) {
			RawFigureUIElement result = new RawFigureUIElement(null, figure,
					null);
			result.setCanvas(figureCanvas);
			Element element = getRawMapper().get(result);
			return toResponse(element);
		}
		return createFailedSelect(NLS.bind(
				TeslaGefMessages.GefProcessor_CannotFindFigureByAddress,
				Arrays.toString(data.getIndexes().toArray())));
	}

	private SelectResponse selectRawFigure(List<Integer> indexes,
			IFigure figure, EditPart part, GraphicalViewer viewer) {
		if (figure != null) {
			Element element = getRawMapper().get(
					new RawFigureUIElement(part, figure, viewer));
			return toResponse(element);
		}
		return createFailedSelect(NLS.bind(
				TeslaGefMessages.GefProcessor_CannotFindFigureByAddress,
				Arrays.toString(indexes.toArray())));
	}

	private SelectResponse selectEditPart(DiagramViewerUIElement uiEl,
			String failMessage, GraphicalEditPart editPart) {
		if (editPart != null) {
			Element element = getMapper().get(
					new FigureUIElement(editPart, uiEl));
			return toResponse(element);
		}
		return createFailedSelect(NLS.bind(
				TeslaGefMessages.GefProcessor_CannotFindFigure_DetailedMsg,
				failMessage));
	}

	private SelectResponse selectPalette(SelectCommand cmd) {
		SelectData data = cmd.getData();
		SWTUIElement result = SWTElementMapper.getMapper(id).get(
				data.getParent());
		if (result != null && result instanceof WorkbenchUIElement) {
			DiagramViewerUIElement element = getPaletteViewerElement((WorkbenchUIElement) result);
			if (element != null) {
				return toResponse(SWTElementMapper.getMapper(id).get(element));
			}
		}
		if (result != null) {
			SWTUIElement[] children = getPlayer().children.collectFor(result, null, true,
					FigureCanvas.class);
			SWTUIElement figureCanvas = null;
			int index = -1;
			if (data.getIndex() != null) {
				index = data.getIndex();
			}
			if (index != -1 && children.length > index) {
				figureCanvas = children[index];
			}
			if (figureCanvas != null) {
				return toResponse(findDiagramViewerElement(figureCanvas,
						PaletteViewer.class, null, getPlayer()));
			} else {
				// Iterate all for first appropriate
				for (SWTUIElement canvas : children) {
					Element e = findDiagramViewerElement(canvas,
							PaletteViewer.class, null, getPlayer());
					if (e != null) {
						return toResponse(e);
					}
				}
			}
		}
		return createFailedSelect(TeslaGefMessages.GefProcessor_CannotFindPalette);
	}

	private SelectResponse selectDiagramViewer(SelectCommand cmd) {
		SelectData data = cmd.getData();
		SWTUIElement result = SWTElementMapper.getMapper(id).get(
				data.getParent());
		if (result != null && result instanceof WorkbenchUIElement) {
			DiagramViewerUIElement element = getDiagramViewerElement((WorkbenchUIElement) result);
			if (element != null) {
				return toResponse(SWTElementMapper.getMapper(id).get(element));
			}
		}
		if (result != null) {
			List<SWTUIElement> children = new ArrayList<SWTUIElement>(Arrays.asList(getPlayer().children.collectFor(
					result, null, true,
					FigureCanvas.class, Canvas.class)));
			// Filter out non graphical viewer canvases
			for (Iterator<SWTUIElement> iterator = children.iterator(); iterator.hasNext();) {
				SWTUIElement swtuiElement = iterator.next();
				Widget widget = swtuiElement.unwrap();
				if (!(widget instanceof FigureCanvas)) {
					GraphicalViewer vv = TeslaSWTAccess.getThis(GraphicalViewer.class, widget,
							SWT.Dispose);
					if (vv == null) {
						iterator.remove();
					}
				}
			}
			SWTUIElement figureCanvas = null;
			int index = -1;
			if (data.getIndex() != null) {
				index = data.getIndex();
			}
			if (index != -1 && children.size() > index) {
				figureCanvas = children.get(index);
			}
			if (figureCanvas != null) {
				return toResponse(findDiagramViewerElement(figureCanvas,
						GraphicalViewer.class, PaletteViewer.class, getPlayer()));
			} else {
				// Iterate all for first appropriate
				for (SWTUIElement canvas : children) {
					Element e = findDiagramViewerElement(canvas,
							GraphicalViewer.class, PaletteViewer.class,
							getPlayer());
					if (e != null) {
						return toResponse(e);
					}
				}
			}
		}
		return createFailedSelect(TeslaGefMessages.GefProcessor_CannotFindDiagram);
	}

	private SelectResponse createFailedSelect(String msg) {
		SelectResponse response = ProtocolFactory.eINSTANCE
				.createSelectResponse();
		response.setStatus(ResponseStatus.FAILED);
		response.setMessage(msg);
		return response;
	}

	@SuppressWarnings({ "rawtypes", "restriction" })
	public static GraphicalViewer findDiagramViewer(SWTUIElement figureCanvas,
			Class cl, Class notcl, SWTUIPlayer player) {
		Object viewer = null;
		viewer = getViewer(figureCanvas.widget);
		if (viewer == null) {
			// Try iterate listeners for graphical viewer one.
			Listener[] listeners = figureCanvas.widget
					.getListeners(SWT.Dispose);
			for (Listener listener : listeners) {
				if (listener instanceof TypedListener) {
					TypedListener tl = (TypedListener) listener;
					org.eclipse.swt.internal.SWTEventListener eventListener = tl.getEventListener();
					Object o = getViewer(eventListener);
					if (cl.isInstance(o)
							&& (notcl == null || !notcl.isInstance(o))) {
						viewer = o;
						break;
					}
				} else {
					Object o = getViewer(listener);
					if (cl.isInstance(o)
							&& (notcl == null || !notcl.isInstance(o))) {
						viewer = o;
						break;
					}
				}
			}
		}
		if (cl.isInstance(viewer)
				&& (notcl == null || !notcl.isInstance(viewer))) {
			return (GraphicalViewer) viewer;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Element findDiagramViewerElement(SWTUIElement figureCanvas,
			Class cl, Class notcl, SWTUIPlayer player) {

		GraphicalViewer graphicalViewer = findDiagramViewer(figureCanvas, cl,
				notcl, player);
		if (graphicalViewer != null) {
			DiagramViewerUIElement element = new DiagramViewerUIElement(player,
					graphicalViewer);
			if (element != null) {
				return SWTElementMapper.getMapper(id).get(element);
			}
		}
		return null;
	}

	private static Object getViewer(Object widget) {
		// Anonymous class
		Object viewer = TeslaGefAccess.getThis(widget);
		if (viewer != null) {
			return viewer;
		}
		// Method-Reference
		return TeslaGefAccess.getArg(widget);
	}

	public static DiagramViewerUIElement getDiagramViewerElement(
			WorkbenchUIElement result) {
		GraphicalEditor editor = getGraphicalEditor(result);
		if (editor != null) {
			GraphicalViewer viewer = (GraphicalViewer) editor
					.getAdapter(GraphicalViewer.class);
			return new DiagramViewerUIElement(result.getPlayer(), viewer);
		}
		return null;
	}

	public static DiagramViewerUIElement getPaletteViewerElement(
			WorkbenchUIElement result) {
		GraphicalEditor editor = getGraphicalEditor(result);
		if (editor != null) {
			GraphicalViewer viewer = (GraphicalViewer) editor
					.getAdapter(GraphicalViewer.class);
			PaletteViewer paletteViewer = viewer.getEditDomain()
					.getPaletteViewer();
			return new DiagramViewerUIElement(result.getPlayer(), paletteViewer);
		}
		return null;
	}

	private SWTUIPlayer getPlayer() {
		return client.getProcessor(SWTUIProcessor.class).getPlayer();
	}

	private static GraphicalEditor getGraphicalEditor(
			WorkbenchUIElement swtuiElement) {
		WorkbenchUIElement e = swtuiElement;
		IWorkbenchPartReference reference = e.getReference();
		IWorkbenchPart part = reference.getPart(true);
		if (part instanceof GraphicalEditor) {
			return (GraphicalEditor) part;
		}
		return null;
	}

	@Override
	public void postSelect(Element element, IElementProcessorMapper mapper) {
		SWTUIProcessor processor = client.getProcessor(SWTUIProcessor.class);
		SWTElementMapper elementMapper = SWTElementMapper.getMapper(processor
				.getId());
		SWTUIElement swtuiElement = elementMapper.get(element);
		if (swtuiElement != null && swtuiElement.widget instanceof FigureCanvas) {
			mapper.map(element, this);
		}
	}

	@Override
	public boolean isInactivityRequired() {
		return true;
	}

	@Override
	public boolean canProceed(Context context, Q7WaitInfoRoot info) {
		return true;
	}

	@Override
	public void clean() {
		getMapper().clear();
		getRawMapper().clear();
		openedDirectEdits.clear();
		dragParts.clear();
		AutoExpandLevels.clear();
		TeslaDirectEditManager.getInstance().clean();
	}

	public FigureElementMapper getMapper() {
		return FigureElementMapper.getMapper(getFeatureID());
	}

	public RawFigureElementMapper getFigureMapper() {
		return RawFigureElementMapper.getMapper(getFeatureID());
	}

	@Override
	public void terminate() {
		FigureElementMapper.remove(getFeatureID());
		RawFigureElementMapper.remove(getFeatureID());
		client = null;
		dragParts.clear();
		openedDirectEdits.clear();
	}

	private void scrollToPosition(final FigureCanvas canvas, int targetX,
			int targetY) {

		Rectangle clientArea = canvas.getViewport().getClientArea();
		if (targetX < clientArea.x || targetY < clientArea.y
				|| targetX > clientArea.x + clientArea.width
				|| targetY > clientArea.y + clientArea.height) {

			if (targetX < clientArea.x) {
				targetX = Math.max(0, targetX - 5);
			}
			if (targetX > clientArea.x + clientArea.width) {
				targetX = Math
						.min(clientArea.x + clientArea.width, targetX + 5);
			}
			if (targetY < clientArea.y) {
				targetY = Math.max(0, targetY - 5);
			}
			if (targetY > clientArea.y + clientArea.height) {
				targetY = Math.min(clientArea.y + clientArea.height,
						targetY + 5);
			}

			canvas.scrollSmoothTo(targetX, targetY);
		}
	}

	private void scrollToFigure(final FigureCanvas canvas, final IFigure figure) {
		Viewport port = canvas.getViewport();
		IFigure target = figure;
		Rectangle exposeRegion = target.getBounds().getCopy();
		target = target.getParent();
		while (target != null && target != port) {
			target.translateToParent(exposeRegion);
			target = target.getParent();
		}
		exposeRegion.expand(5, 5);

		Dimension viewportSize = port.getClientArea().getSize();

		Point topLeft = exposeRegion.getTopLeft();
		Point bottomRight = exposeRegion.getBottomRight().translate(
				viewportSize.getNegated());
		Point finalLocation = new Point();
		if (viewportSize.width < exposeRegion.width)
			finalLocation.x = Math.min(bottomRight.x,
					Math.max(topLeft.x, port.getViewLocation().x));
		else
			finalLocation.x = Math.min(topLeft.x,
					Math.max(bottomRight.x, port.getViewLocation().x));

		if (viewportSize.height < exposeRegion.height)
			finalLocation.y = Math.min(bottomRight.y,
					Math.max(topLeft.y, port.getViewLocation().y));
		else
			finalLocation.y = Math.min(topLeft.y,
					Math.max(bottomRight.y, port.getViewLocation().y));

		canvas.scrollSmoothTo(finalLocation.x, finalLocation.y);
	}

	public boolean callMasterProcess(Context currentContext) {
		return false;
	}

	public RawFigureElementMapper getRawMapper() {
		return RawFigureElementMapper.getMapper(getFeatureID());
	}

	@Override
	public void checkHang() {
	}

	@Override
	public void collectInformation(AdvancedInformation information,
			Command lastCommand) {
		Node root = InfoUtils.newNode("gef.editparts").add(information);
		Element element = null;
		if (lastCommand instanceof ElementCommand) {
			element = ((ElementCommand) lastCommand).getElement();
		} else if (lastCommand instanceof Assert) {
			element = ((Assert) lastCommand).getElement();
		} else if (lastCommand instanceof SelectCommand) {
			element = ((SelectCommand) lastCommand).getData().getParent();
		}
		if (element != null) {

			DiagramViewerUIElement diagram;
			FigureUIElement figureUIElement = getMapper().get(element);
			if (figureUIElement != null) {
				diagram = figureUIElement.getDiagram();
			} else {
				SWTUIElement swtuiElement = SWTElementMapper.getMapper(id).get(
						element);
				if (swtuiElement instanceof DiagramViewerUIElement) {
					diagram = (DiagramViewerUIElement) swtuiElement;
				} else {
					return;
				}
			}
			if (diagram != null) {
				// Lets list all edit parts availible to diagram
				GraphicalViewer viewer = diagram.getViewer();
				RootEditPart part = viewer.getRootEditPart();
				// Make list of edit part figures
				Map<IFigure, EditPart> allEditPartFigures = new HashMap<IFigure, EditPart>();
				fillEditPartFigures(allEditPartFigures, viewer.getContents());

				collectPartInfo(root, part, allEditPartFigures);
			}
		}
	}

	private void fillEditPartFigures(Map<IFigure, EditPart> allEditPartFigures,
			EditPart contents) {
		List<? extends EditPart> children = contents.getChildren();
		for (EditPart editPart : children) {
			if (editPart instanceof GraphicalEditPart) {
				allEditPartFigures.put(
						((GraphicalEditPart) editPart).getFigure(), editPart);
			}
			fillEditPartFigures(allEditPartFigures, editPart);
		}
	}

	@SuppressWarnings("rawtypes")
	private void collectPartInfo(Node root, EditPart part,
			Map<IFigure, EditPart> allEditPartFigures) {
		if (part instanceof GraphicalEditPart) {
			GraphicalEditPart p = (GraphicalEditPart) part;
			IFigure figure = p.getFigure();
			if (figure != null) {
				List<Integer> address = GefUtils.getAddressRaw(p);
				Node infoNode = root.child("edit part:"
						+ Arrays.toString(address.toArray()));
				infoNode.property("class", part.getClass().getSimpleName());
				// infoNode.property("class", figure.getClass().getName());
				if (figure.isVisible() != figure.isShowing()) {
					infoNode.property("visible.not.showing", "true");
				}
				if (!figure.isVisible()) {
					infoNode.property("visible", figure.isVisible());
				}
				if (!figure.isShowing()) {
					infoNode.property("showing", figure.isShowing());
				}
				if (figure.isOpaque()) {
					infoNode.property("opaque", figure.isOpaque());
				}
				// infoNode.property("enabled", figure.isEnabled());
				processFigures(infoNode.child("figures"), figure, figure,
						allEditPartFigures, false);
			}
		}
		List children = part.getChildren();
		for (Object object : children) {
			if (object instanceof EditPart) {
				collectPartInfo(root, (EditPart) object, allEditPartFigures);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void processFigures(Node figuresNode, IFigure iFigure,
			IFigure parent, Map<IFigure, EditPart> allEditPartFigures,
			boolean filter) {
		Class<? extends IFigure> clazz = iFigure.getClass();
		String simpleName = clazz.getSimpleName();
		List<Integer> address = GefUtils.getAddress(iFigure, parent);
		Node child = figuresNode.child("figure:"
				+ Arrays.toString(address.toArray()));
		child.property("class ", simpleName);
		if (iFigure instanceof Label) {
			child.property("text", ((Label) iFigure).getText());
		}
		if (iFigure instanceof TextFlow) {
			child.property("text", ((TextFlow) iFigure).getText());
		}
		List children = iFigure.getChildren();
		for (Object object : children) {
			if (!(allEditPartFigures.containsKey(object)) || !filter) {
				processFigures(figuresNode, (IFigure) object, parent,
						allEditPartFigures, true);
			}
		}
	}

	public static String getPartCustomId(EditPart part) {
		try {
			return IdentifyObjectUtil.getObjectIdByClassMethods(part);
		} catch (CoreException e) {
			GefActivator.log(e);
		}
		return null;
	}

	public static String getPartClassName(EditPart part) {
		String className = part.getClass().getSimpleName();
		// String EDIT_PART_SUFFIX = "EditPart";
		// if (className.endsWith(EDIT_PART_SUFFIX)) {
		// className = className.substring(0, className.length()
		// - EDIT_PART_SUFFIX.length());
		// }
		return className;
	}

	public static String getPartText(org.eclipse.rcptt.tesla.core.ui.Widget model) {
		if (model instanceof DiagramItem) {
			DiagramItem item = (DiagramItem) model;
			String text = item.getText();
			if (text != null && !text.trim().isEmpty()) {
				return text;
			}
		}
		return null;
	}

	@Override
	public void notifyUI() {
		// TODO Auto-generated method stub

	}
}
