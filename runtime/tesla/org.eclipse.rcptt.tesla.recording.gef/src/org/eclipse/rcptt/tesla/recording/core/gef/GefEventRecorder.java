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
package org.eclipse.rcptt.tesla.recording.core.gef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.EventListenerList;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.SWTEventDispatcher;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.text.FlowFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.EditPartViewer.Conditional;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.Tool;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.gef.tools.AbstractTool.Input;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.features.IMLFeatures;
import org.eclipse.rcptt.tesla.core.protocol.diagram.DiagramFeatures;
import org.eclipse.rcptt.tesla.core.protocol.diagram.DiagramFeatures.MouseMoveMode;
import org.eclipse.rcptt.tesla.core.protocol.diagram.DiagramViewerUIElement;
import org.eclipse.rcptt.tesla.core.protocol.diagram.FigureUIElement;
import org.eclipse.rcptt.tesla.core.protocol.diagram.MouseCommandKind;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.SetMode;
import org.eclipse.rcptt.tesla.gef.DirectEditorContainer;
import org.eclipse.rcptt.tesla.gef.GefProcessor;
import org.eclipse.rcptt.tesla.gef.GefUtils;
import org.eclipse.rcptt.tesla.gef.GefUtils.FigureAddress;
import org.eclipse.rcptt.tesla.gef.TeslaGefAccess;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.recording.aspects.draw2d.Draw2DEventManager;
import org.eclipse.rcptt.tesla.recording.aspects.draw2d.IDraw2DEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.gef.GefEventManager;
import org.eclipse.rcptt.tesla.recording.aspects.gef.IGefEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.gef.RecordingGefActivator;
import org.eclipse.rcptt.tesla.recording.core.IRecordingHelper;
import org.eclipse.rcptt.tesla.recording.core.IRecordingProcessor;
import org.eclipse.rcptt.tesla.recording.core.IRecordingProcessorExtension;
import org.eclipse.rcptt.tesla.recording.core.TeslaRecorder;
import org.eclipse.rcptt.tesla.recording.core.gef.GefRecordingHelper.GraphicalViewerEntry;
import org.eclipse.rcptt.tesla.recording.core.swt.BasicRecordingHelper.ElementEntry;
import org.eclipse.rcptt.tesla.recording.core.swt.ICanvasDiagramHelper;
import org.eclipse.rcptt.tesla.recording.core.swt.SWTEventRecorder;
import org.eclipse.rcptt.util.ReflectionUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

public class GefEventRecorder implements IRecordingProcessor,
		IGefEventListener, ICanvasDiagramHelper, IRecordingProcessorExtension {
	private TeslaRecorder recorder;
	private boolean collectChildren = false;
	private final List<EditPart> childrens = new ArrayList<EditPart>();
	private SWTEventRecorder eventRecorder = null;
	private final Set<EditPartViewer> workingWidgets = new HashSet<EditPartViewer>();
	@SuppressWarnings("unused")
	private boolean down = false;
	private final GefFigureLocator locator = new GefFigureLocator(this);
	private boolean wasMouseMove = false;

	@SuppressWarnings("unused")
	private static class GefViewerCacheEntry {
		SWTUIElement lowerParent;
		DiagramViewerUIElement diagramViewer;
	}

	public GefEventRecorder() {
		GefEventManager.addListener(this);
		Draw2DEventManager.addListener(new IDraw2DEventListener() {

			@Override
			public void mouseReleased(SWTEventDispatcher disp, MouseEvent event) {
				try {
					GraphicalViewer viewer = getGraphicalViewer(event.widget);
					if (viewer != null) {
						GefEventRecorder.this.mouseUp(null, event, viewer);
					}
				} catch (Throwable e) {
					RecordingGefActivator.log(e);
				}
			}

			@Override
			public void mousePressed(SWTEventDispatcher disp, MouseEvent event) {
				try {
					GraphicalViewer viewer = getGraphicalViewer(event.widget);
					if (viewer != null) {
						GefEventRecorder.this.mouseDown(null, event, viewer);
					}
				} catch (Throwable e) {
					RecordingGefActivator.log(e);
				}

			}

			@Override
			public void mouseMoved(SWTEventDispatcher disp, MouseEvent event) {
				try {
					GraphicalViewer viewer = getGraphicalViewer(event.widget);
					if (viewer != null) {
						GefEventRecorder.this.mouseMove(null, event, viewer);
					}
				} catch (Throwable e) {
					RecordingGefActivator.log(e);
				}
			}

			@Override
			public void mouseHover(SWTEventDispatcher disp, MouseEvent event) {
				try {
					GraphicalViewer viewer = getGraphicalViewer(event.widget);
					if (viewer != null) {
						GefEventRecorder.this.mouseHover(null, event, viewer);
					}
				} catch (Throwable e) {
					RecordingGefActivator.log(e);
				}
			}

			@Override
			public void mouseExited(SWTEventDispatcher disp, MouseEvent event) {
			}

			@Override
			public void mouseEntered(SWTEventDispatcher disp, MouseEvent event) {
			}

			@Override
			public void mouseDoubleClick(SWTEventDispatcher disp, MouseEvent event) {
				try {
					GraphicalViewer viewer = getGraphicalViewer(event.widget);
					if (viewer != null) {
						GefEventRecorder.this.mouseDoubleClick(null, event, viewer);
					}
				} catch (Throwable e) {
					RecordingGefActivator.log(e);
				}
			}
		});
	}

	private GraphicalViewer getGraphicalViewer(Widget widget) {
		if (widget instanceof Canvas) {
			return TeslaSWTAccess.getThis(GraphicalViewer.class, widget, SWT.Dispose);
		}
		return null;
	}

	public void performRequest(EditPart part, Request request) {
	}

	public void toolActivate(Tool tool) {
	}

	public void setActiveTool(EditDomain domain, Tool tool) {
	}

	public int getInitLevel() {
		return 1000;
	};

	public void notifyAddChild(AbstractEditPart part, EditPart child, int index) {
		if (collectChildren) {
			childrens.add(child);
		}
		// Because addresses could be different right after this
		GefRecordingHelper.getHelper().clear();
	}

	public void notifyRemoveChild(AbstractEditPart part, EditPart child,
			int index) {
		// Because addresses could be different right after this
		GefRecordingHelper.getHelper().clear();
	}

	public interface IDiagramEventHandler {
		void run(SWTUIPlayer player, DiagramViewerUIElement diagramViewer,
				SWTUIElement parent);
	}

	public synchronized void doDiagramEventOp(EditDomain domain,
			EditPartViewer viewer, IDiagramEventHandler handler) {
		if (isSkipDiagramOperations()) {
			return;
		}

		GraphicalViewerEntry entry = locator.getDiagramUIElement(viewer);
		if (entry != null) {
			SWTUIPlayer player = eventRecorder.getPlayer();
			handler.run(player, entry.diagramViewer, entry.lowerParent);
		}
	}

	public boolean isSkipDiagramOperations() {
		return TeslaFeatures.getInstance().isTrue(IMLFeatures.USE_IMAGING)
				&& !TeslaFeatures.getInstance().isTrue(
						IMLFeatures.USE_IMAGING_RAW_CANVAS);
	}

	public synchronized SWTEventRecorder getEventRecorder() {
		if (eventRecorder == null) {
			eventRecorder = recorder.getProcessor(SWTEventRecorder.class);
		}
		return eventRecorder;
	}

	public void mouseDown(EditDomain domain, final MouseEvent mouseEvent,
			final EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		boolean cts = workingWidgets.contains(viewer);
		workingWidgets.add(viewer);
		down = true;
		if (!cts) {
			mouseMove(domain, mouseEvent, viewer);
		}
		if (wasMouseMove) {
			mouseMove(domain, mouseEvent, viewer, true);
		}
		if (isSkipDiagramOperations()) {
			return;
		}
		GraphicalViewerEntry entry = locator.getDiagramUIElement(viewer);
		if (entry != null) {
			EditPart editPart = viewer.getRootEditPart() == null ? null : viewer.findObjectAt(new Point(mouseEvent.x,
					mouseEvent.y));
			doMouseFigureOperation(mouseEvent, entry.diagramViewer, editPart,
					MouseCommandKind.DOWN, viewer);
		}
		wasMouseMove = false;
	}

	public IGMFRecordingProcessor[] getHelpers() {
		List<IGMFRecordingProcessor> processors = recorder
				.getProcessors(IGMFRecordingProcessor.class);
		return processors
				.toArray(new IGMFRecordingProcessor[processors.size()]);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doMouseFigureOperation(final MouseEvent mouseEvent,
			DiagramViewerUIElement diagramViewer, EditPart editPart,
			MouseCommandKind op, EditPartViewer viewer) {
		Canvas cv = (Canvas) viewer.getControl();
		if (editPart != null) {
			Map visualPartMap = viewer.getVisualPartMap();
			Set<Map.Entry> keySet = visualPartMap.entrySet();
			AbstractHandle handle = null;
			for (Map.Entry object : keySet) {
				if (object.getValue().equals(editPart)) {
					if (object.getKey() instanceof AbstractHandle) {
						AbstractHandle h = (AbstractHandle) object.getKey();
						if (h.containsPoint(mouseEvent.x, mouseEvent.y)) {
							handle = h;
							break;
						}
					}
				}
			}
			if (handle == null) {
				if (editPart.equals(viewer.getRootEditPart()) && cv instanceof FigureCanvas) {
					IFigure figure = ((FigureCanvas) cv).getContents();

					IFigure resultFigure = figure.findFigureAt(mouseEvent.x,
							mouseEvent.y);
					// Do not go into edit part figures
					// resultFigure = skipEditPartFigures(resultFigure, figure,
					// viewer);
					if (resultFigure instanceof AbstractHandle) {
						handle = (AbstractHandle) resultFigure;
					}
				}
			}
			if (handle != null) {
				FigureAddress hfa = GefUtils.getAddress(handle);
				for (IGMFRecordingProcessor helper : getHelpers()) {
					if (helper.isIgnored(handle)) {
						return;
					}
					// if (handle instanceof MoveHandle
					// || handle instanceof ResizeHandle) {
					// return;
					// }
					FigureAddress addr = helper.getHandleAddress(handle);
					if (addr != null) {
						EditPart part = helper.getEditPart(handle);
						if (part != null) {
							doEditPartOp(mouseEvent, diagramViewer, editPart,
									op, locator.find(part, viewer,
											diagramViewer, false), handle,
									addr.path);
							return;
						}
						hfa = addr;
					}
				}
				if (handle instanceof AbstractHandle) {
					EditPart part = TeslaGefAccess
							.getEditPart(handle);
					FigureUIElement element = getLocator().find(part, viewer,
							diagramViewer, false);
					int indexOf = 0;

					List<AbstractHandle> children = GefProcessor
							.findAbstractHandles((GraphicalViewer) viewer);
					for (Object object : children) {
						if (object.equals(handle)) {
							break;
						}
						if (object instanceof AbstractHandle) {
							if (((AbstractHandle) object).isVisible()) {
								if (object.getClass().equals(handle.getClass())
										&& part.equals(TeslaGefAccess
												.getEditPart((AbstractHandle) object))) {
									indexOf++;
								}
							}
						}
					}

					FigureUIElement handleFigure = getHandleFigure(handle,
							element, indexOf);
					org.eclipse.draw2d.geometry.Rectangle bounds = handle
							.getBounds().getCopy();
					handle.translateToAbsolute(bounds);
					int px = mouseEvent.x - bounds.x;
					int py = mouseEvent.y - bounds.y;
					// Click over diagram operation.
					Rectangle bounds2 = cv.getBounds();
					handleFigure.executeFigureMouseCommand(px, py,
							mouseEvent.button, op, null, null, bounds2.width,
							bounds2.height, bounds.width, bounds.height,
							mouseEvent.stateMask);
					return;
				}
				if (hfa != null) {
					performOp(mouseEvent, diagramViewer, op, cv,
							hfa.lastFigure, hfa.path);
					return;
				}
			}
			// Lets figure out edit part figure address.
			// List<Integer> address = GefUtils.getAddress(editPart);
			if (editPart != null && editPart != viewer.getRootEditPart()) {
				FigureUIElement address = locator.find(editPart, viewer,
						diagramViewer, false);
				GraphicalEditPart gep = (GraphicalEditPart) editPart;
				IFigure figure = gep.getFigure();
				IFigure figureAt = figure.findFigureAt(mouseEvent.x,
						mouseEvent.y);
				figureAt = skipFlowFigures(figureAt, figure);
				figureAt = skipEditPartFigures(figureAt, figure, viewer);
				List<Integer> faAddress = GefUtils.getAddress(figureAt, figure);
				if (figureAt != null && !figureAt.getParent().isEnabled()) {
					figureAt = figure;
					faAddress.clear();
				}
				if (figureAt == null) {
					figureAt = figure;
				}
				doEditPartOp(mouseEvent, diagramViewer, editPart, op, address,
						figureAt, faAddress);
				return;
			}
		}
		IFigure figure = null;// cv.getContents();
		if (cv instanceof FigureCanvas) {
			figure = ((FigureCanvas) cv).getContents();
		}
		else {
			GraphicalViewer graphicalViewer = TeslaSWTAccess.getThis(GraphicalViewer.class, cv, SWT.Dispose);
			if (graphicalViewer != null) {
				figure = TeslaSWTAccess.getField(IFigure.class, graphicalViewer, "rootFigure");
			}
		}
		if (figure != null) {

			IFigure resultFigure = figure.findFigureAt(mouseEvent.x, mouseEvent.y);
			// Do not go into edit part figures
			resultFigure = skipEditPartFigures(resultFigure, figure, viewer);
			List<Integer> faAddress = GefUtils.getAddress(resultFigure, figure);
			for (IGMFRecordingProcessor helper : getHelpers()) {
				if (helper.isIgnored(resultFigure)) {
					return;
				}
				List<Integer> addr = helper.getFigureAddress(resultFigure);
				if (addr != null) {
					EditPart part = helper.getEditPart(resultFigure);
					if (part != null) {
						// Special case
						doEditPartOp(mouseEvent, diagramViewer, part, op,
								locator.find(part, viewer, diagramViewer, false),
								resultFigure, addr);
						return;
					}
					faAddress = addr;
				}
			}
			if (resultFigure != null && !resultFigure.getParent().isEnabled()
					|| (editPart == null && op.equals(MouseCommandKind.DRAG))) {
				resultFigure = figure;
				faAddress.clear();
			}
			if (resultFigure == null) {
				resultFigure = figure;
			}
			performOp(mouseEvent, diagramViewer, op, cv, resultFigure, faAddress);
		}
	}

	private FigureUIElement getHandleFigure(AbstractHandle handle,
			FigureUIElement parentElement, int indexOf) {

		ElementEntry el = GefRecordingHelper.getHelper().get(handle);
		FigureUIElement element = null;
		if (el != null) {
			element = new FigureUIElement(el.getElement(), recorder);
		} else {
			element = parentElement.handleByClass(handle.getClass()
					.getSimpleName(), indexOf == 0 ? null : indexOf);
			GefRecordingHelper.getHelper().put(handle,
					new ElementEntry(element.getElement()));
		}
		return element;
	}

	private IFigure skipEditPartFigures(IFigure resultFigure, IFigure figure,
			EditPartViewer viewer) {
		Set<IFigure> allEditPartFigures = new HashSet<IFigure>();
		if (viewer.getRootEditPart() != null) {
			fillEditPartFigures(allEditPartFigures, viewer.getContents());
		}
		// allEditPartFigures.add(((GraphicalEditPart) viewer.getContents())
		// .getFigure());
		List<IFigure> parentsList = new ArrayList<IFigure>();
		IFigure i = resultFigure;
		while (i != null) {
			parentsList.add(0, i);
			if (i.equals(figure)) {
				break;
			}
			i = i.getParent();
		}
		for (IFigure iFigure : parentsList) {
			if (allEditPartFigures.contains(iFigure) || !iFigure.isEnabled()) {
				return figure;
			}
		}
		return resultFigure;
	}

	@SuppressWarnings("unchecked")
	private void fillEditPartFigures(Set<IFigure> allEditPartFigures,
			EditPart contents) {
		List<EditPart> children = contents.getChildren();
		for (EditPart editPart : children) {
			if (editPart instanceof GraphicalEditPart) {
				allEditPartFigures.add(((GraphicalEditPart) editPart)
						.getFigure());
			}
			fillEditPartFigures(allEditPartFigures, editPart);
		}
	}

	private IFigure skipFlowFigures(IFigure figureAt, IFigure figure) {
		while (figureAt instanceof FlowFigure) {
			// org.eclipse.draw2d.geometry.Rectangle bounds =
			// figureAt.getBounds();
			figureAt = figureAt.getParent();
			if (figure.equals(figureAt)) {
				break;
			}
		}
		return figureAt;
	}

	private void doEditPartOp(final MouseEvent mouseEvent,
			DiagramViewerUIElement diagramViewer, EditPart editPart,
			MouseCommandKind op, FigureUIElement address, IFigure figureAt,
			List<Integer> faAddress) {
		Control control = editPart.getViewer().getControl();
		FigureCanvas canvas = (FigureCanvas) control;
		org.eclipse.draw2d.geometry.Rectangle bounds = figureAt.getBounds()
				.getCopy();

		Point mouseEventRelativeCoords = new Point(mouseEvent.x, mouseEvent.y);
		figureAt.translateToRelative(mouseEventRelativeCoords);

		int px = mouseEventRelativeCoords.x - bounds.x;
		int py = mouseEventRelativeCoords.y - bounds.y;

		figureAt.translateToRelative(mouseEventRelativeCoords);

		// Click over diagram operation.
		Rectangle bounds2 = canvas.getBounds();
		if (address != null) {
			// System.out.println("Part by address:" + address);
			if (faAddress != null && faAddress.size() > 0) {
				FigureUIElement element = getFigureByAddress(address, figureAt,
						faAddress);
				element.executeFigureMouseCommand(px, py, mouseEvent.button,
						op, null, null, bounds2.width, bounds2.height,
						bounds.width, bounds.height, mouseEvent.stateMask);
				return;
			} else {
				address.executeFigureMouseCommand(px, py, mouseEvent.button,
						op, null, null, bounds2.width, bounds2.height,
						bounds.width, bounds.height, mouseEvent.stateMask);
				return;
			}
		}
		diagramViewer.executeFigureMouseCommand(px, py, mouseEvent.button, op,
				null, faAddress, bounds2.width, bounds2.height, bounds.width,
				bounds.height, mouseEvent.stateMask);
	}

	private FigureUIElement getFigureByAddress(FigureUIElement diagramViewer,
			IFigure figure, List<Integer> address) {
		ElementEntry el = GefRecordingHelper.getHelper().get(figure);
		FigureUIElement element = null;
		if (el != null) {
			element = new FigureUIElement(el.getElement(), recorder);
		} else {
			element = diagramViewer.rawFigure(address);
			GefRecordingHelper.getHelper().put(figure,
					new ElementEntry(element.getElement()));
		}
		return element;
	}

	private void performOp(final MouseEvent mouseEvent,
			DiagramViewerUIElement diagramViewer, MouseCommandKind op,
			Canvas cv, IFigure lastFigure, List<Integer> figureAddress) {
		org.eclipse.draw2d.geometry.Rectangle bounds = lastFigure.getBounds()
				.getCopy();
		// lastFigure.translateToAbsolute(bounds);
		int px = mouseEvent.x - bounds.x;
		int py = mouseEvent.y - bounds.y;
		// Click over diagram operation.
		Rectangle bounds2 = cv.getBounds();
		if (figureAddress != null && figureAddress.size() > 0) {
			FigureUIElement element = getFigureByAddress(diagramViewer,
					lastFigure, figureAddress);
			element.executeFigureMouseCommand(px, py, mouseEvent.button, op,
					null, null, bounds2.width, bounds2.height, bounds.width,
					bounds.height, mouseEvent.stateMask);
			return;
		}
		diagramViewer.executeFigureMouseCommand(px, py, mouseEvent.button, op,
				null, figureAddress, bounds2.width, bounds2.height,
				bounds.width, bounds.height, mouseEvent.stateMask);
	}

	@SuppressWarnings("rawtypes")
	public void mouseUp(EditDomain domain, final MouseEvent mouseEvent,
			final EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		workingWidgets.add(viewer);
		// if (directEditPart != null) {
		// return;
		// }
		// System.out.println("Mouse up:" + mouseEvent.x + "," + mouseEvent.y
		// + " " + mouseEvent.widget);
		down = false;
		if (isSkipDiagramOperations()) {
			return;
		}

		GraphicalViewerEntry entry = locator.getDiagramUIElement(viewer);
		if (entry != null) {
			EditPart editPart = null;
			if (inDrag > 0) {
				// Check drag parts
				// Add also from drag traker
				Set exclude = new HashSet();
				editPart = viewer.findObjectAtExcluding(new Point(mouseEvent.x,
						mouseEvent.y), exclude, new Conditional() {
					public boolean evaluate(EditPart editpart) {
						while (editpart != null) {
							if (dragPart.contains(editpart)) {
								return false;
							}
							editpart = editpart.getParent();
						}
						// Check parents of edit part to not be
						// dragPart
						return true;
					}
				});
				doMouseFigureOperation(mouseEvent, entry.diagramViewer,
						editPart, MouseCommandKind.DRAG, viewer);
			}
			if (editPart == null && viewer.getRootEditPart() != null) {
				editPart = viewer.findObjectAt(new Point(mouseEvent.x,
						mouseEvent.y));
			}
			doMouseFigureOperation(mouseEvent, entry.diagramViewer, editPart,
					MouseCommandKind.UP, viewer);
			inDrag = 0;
		}
		collectChildren = true;
		childrens.clear();
		wasMouseMove = false;
		// workingWidgets.remove(viewer);
	}

	public void afterMouseUp(EditDomain domain, MouseEvent mouseEvent,
			EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		// doDiagramEventOp(domain, viewer, new IDiagramEventHandler() {
		// public void run(SWTUIPlayer player,
		// DiagramViewerUIElement diagramViewer, SWTUIElement parent) {
		// // Perform children creation checks
		// collectChildren = false;

		// // Filter childs
		// List<EditPart> real = new ArrayList<EditPart>();
		// for (EditPart child : childrens) {
		// if (!childrens.contains(child.getParent())) {
		// real.add(child);
		// }
		// }
		// for (EditPart child : real) {
		// System.out.println("Collected:" + child);
		// List<Integer> address = GefUtils.getAddress(child);
		// if (address != null) {
		// // diagramViewer.assertFigureExists(address);
		// }
		// }
		// }
		// });
	}

	public void mouseDoubleClick(EditDomain domain,
			final MouseEvent mouseEvent, final EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		workingWidgets.add(viewer);
		// if (directEditPart != null) {
		// return;
		// }
		// System.out.println("Mouse up:" + mouseEvent.x + "," + mouseEvent.y
		// + " " + mouseEvent.widget);
		if (isSkipDiagramOperations()) {
			return;
		}

		GraphicalViewerEntry entry = locator.getDiagramUIElement(viewer);
		if (entry != null) {
			EditPart editPart = viewer.findObjectAt(new Point(mouseEvent.x,
					mouseEvent.y));
			doMouseFigureOperation(mouseEvent, entry.diagramViewer, editPart,
					MouseCommandKind.DOUBLE_CLICK, viewer);
		}
	}

	private int inDrag = 0;
	protected Set<EditPart> dragPart = new HashSet<EditPart>();
	private EditPart directEditPart = null;

	public void mouseDrag(EditDomain domain, final MouseEvent mouseEvent,
			final EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		workingWidgets.add(viewer);
		// if (directEditPart != null) {
		// return;
		// }
		if (inDrag == 0) {
			if (isSkipDiagramOperations()) {
				return;
			}

			GraphicalViewerEntry entry = locator.getDiagramUIElement(viewer);
			if (entry != null) {
				inDrag++;
				EditPart editPart = viewer.findObjectAt(new Point(mouseEvent.x,
						mouseEvent.y));

				// Add also from drag traker
				fillDragParts(editPart, dragPart, viewer);

				doMouseFigureOperation(mouseEvent, entry.diagramViewer,
						editPart, MouseCommandKind.DRAG, viewer);
				mouseEvent.x += 6;
				mouseEvent.y += 6;
				doMouseFigureOperation(mouseEvent, entry.diagramViewer, null,
						MouseCommandKind.DRAG, viewer);
			}
		} else {
			fillDragParts(null, dragPart, viewer);
		}
	}

	public void mouseHover(EditDomain domain, final MouseEvent mouseEvent,
			final EditPartViewer viewer) {
		if (inDrag > 0) {
			return; // Skip hovering then we are in drag
		}
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		if (!TeslaFeatures.getInstance().isTrue(
				DiagramFeatures.MOUSE_HOVER_SUPPORT)) {
			return;
		}
		if (!workingWidgets.contains(viewer)) {
			return;
		}
		if (isSkipDiagramOperations()) {
			return;
		}

		GraphicalViewerEntry entry = locator.getDiagramUIElement(viewer);
		if (entry != null) {
			EditPart editPart = viewer.findObjectAt(new Point(mouseEvent.x,
					mouseEvent.y));
			doMouseFigureOperation(mouseEvent, entry.diagramViewer, editPart,
					MouseCommandKind.HOVER, viewer);
		}
	}

	public void mouseMove(EditDomain domain, final MouseEvent mouseEvent,
			final EditPartViewer viewer) {
		mouseMove(domain, mouseEvent, viewer, false);
	}

	private static MouseMoveMode getMouseMoveMode() {
		return MouseMoveMode.valueOf(TeslaFeatures.getInstance().getValue(
				DiagramFeatures.MOUSE_MOVE_MODE));
	}

	public void mouseMove(EditDomain domain, final MouseEvent mouseEvent,
			final EditPartViewer viewer, boolean forceRecord) {
		wasMouseMove = true;
		MouseMoveMode mode = getMouseMoveMode();
		boolean shouldRecord = forceRecord // mouseDown asked us to record this
											// explicitly
				|| (mouseEvent.stateMask & SWT.BUTTON_MASK) != 0 // button
																	// pressed
				|| mode == MouseMoveMode.Always; // user wants to record all
													// mouse move events

		if (inDrag > 0) {
			if (domain != null && domain.getActiveTool() instanceof AbstractTool) {
				AbstractTool tool = (AbstractTool) domain.getActiveTool();
				Input input = TeslaGefAccess.getCurrentInput(tool);
				if (input == null) {
					input = new Input();
				}
				if (!isInputSynched(input, mouseEvent)) {
					boolean b1 = input.isMouseButtonDown(1);
					boolean b2 = input.isMouseButtonDown(2);
					boolean b3 = input.isMouseButtonDown(3);
					boolean b4 = input.isMouseButtonDown(4);
					boolean b5 = input.isMouseButtonDown(5);
					if (b1) {
						mouseEvent.button = 1;
						mouseUp(domain, mouseEvent, viewer);
					}
					if (b2) {
						mouseEvent.button = 2;
						mouseUp(domain, mouseEvent, viewer);
					}
					if (b3) {
						mouseEvent.button = 3;
						mouseUp(domain, mouseEvent, viewer);
					}
					if (b4) {
						mouseEvent.button = 4;
						mouseUp(domain, mouseEvent, viewer);
					}
					if (b5) {
						mouseEvent.button = 5;
						mouseUp(domain, mouseEvent, viewer);
					}
					return;
				}
			}
			return;// Skip mouse move then we in drag
		}
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}

		if (isSkipDiagramOperations()) {
			return;
		}

		GraphicalViewerEntry entry = locator.getDiagramUIElement(viewer);
		if (entry != null && viewer.getRootEditPart() != null) {
			EditPart editPart = viewer.findObjectAt(new Point(mouseEvent.x,
					mouseEvent.y));

			// don't check listeners if we have already decided to record this
			if (editPart instanceof GraphicalEditPart && !shouldRecord) {
				IFigure figure = ((GraphicalEditPart) editPart).getFigure();
				EventListenerList listeners = (EventListenerList) ReflectionUtil
						.getField(figure, "eventListeners");

				if (listeners != null
						&& listeners.getListeners(MouseMotionListener.class)
								.hasNext()) {
					shouldRecord = mode == MouseMoveMode.Smart;
				}
			}
			if (shouldRecord) {
				doMouseFigureOperation(mouseEvent, entry.diagramViewer,
						editPart, MouseCommandKind.MOVE, viewer);
			}
		}
	}

	private boolean isInputSynched(Input input, MouseEvent event) {
		return input.isMouseButtonDown(1) == ((event.stateMask & SWT.BUTTON1) != 0)
				&& input.isMouseButtonDown(2) == ((event.stateMask & SWT.BUTTON2) != 0)
				&& input.isMouseButtonDown(3) == ((event.stateMask & SWT.BUTTON3) != 0)
				&& input.isMouseButtonDown(4) == ((event.stateMask & SWT.BUTTON4) != 0)
				&& input.isMouseButtonDown(5) == ((event.stateMask & SWT.BUTTON5) != 0);
	}

	public void mouseWheelScrolled(EditDomain domain, MouseEvent mouseEvent,
			EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		// System.out.println("Mouse wheel scrolled");

	}

	public void afterMouseDown(EditDomain domain, MouseEvent mouseEvent,
			EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
	}

	public void executeCommand(Command command) {
		// System.out.println("$$$$ Execute command:" + command.toString());

	}

	public void initialize(TeslaRecorder teslaRecorder) {
		this.recorder = teslaRecorder;
	}

	public void clear() {
		GefRecordingHelper.getHelper().clear();
		// createdParts.clear();
		// activeTools.clear();
		// entries.clear();
		childrens.clear();
		inDrag = 0;
		directEditPart = null;
		workingWidgets.clear();
	}

	public void nativeDragFinished(EditDomain domain,
			final DragSourceEvent event, final EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		// doDiagramEventOp(domain, viewer, new IDiagramEventHandler() {
		// public void run(SWTUIPlayer player,
		// DiagramViewerUIElement diagramViewer, SWTUIElement parent) {
		// // Click over diagram operation.
		// diagramViewer.mouseNativeDragFinished(0, 0);
		// }
		// });
	}

	public void nativeDragStarted(EditDomain domain,
			final DragSourceEvent event, final EditPartViewer viewer) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		// doDiagramEventOp(domain, viewer, new IDiagramEventHandler() {
		// public void run(SWTUIPlayer player,
		// DiagramViewerUIElement diagramViewer, SWTUIElement parent) {
		// // Click over diagram operation.
		// diagramViewer.mouseNativeDragStarted(0, 0);
		// }
		// });
	}

	public void forceDirectEdit(DirectEditManager manager) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		Object source = getDirectEditSource(manager);
		if (source != null) {
			if (source instanceof EditPart) {
				EditPart part = (EditPart) source;
				// eclipse 3.4 support: PaletteEditPart in different packages
				if (TeslaGefAccess.isPalleteEditPart(part)
						&& TeslaFeatures.getInstance().isTrue(
								DiagramFeatures.STORE_PALETTE_AS_NAME)) {
					return;
				}
				if (directEditPart != null && directEditPart.equals(part)) {
					return; // Already in direct edit
				}
				directEditPart = part;
				// final List<Integer> address = GefUtils.getAddress(part);
				EditPartViewer viewer = part.getViewer();
				if (isSkipDiagramOperations()) {
					return;
				}

				GraphicalViewerEntry entry = locator
						.getDiagramUIElement(viewer);
				if (entry != null) {
					FigureUIElement partElement = locator.find(part, viewer,
							entry.diagramViewer, false);
					if (partElement != null) {
						partElement.forceDirectEdit(null);
					}
				}
			}
		}
	}

	private Object getDirectEditSource(DirectEditManager manager) {
		return TeslaGefAccess.getDirectEditSource(manager);
	}

	public void cancelDirectEdit(DirectEditorContainer container) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		CellEditor cellEditor = container.getCellEditor();
		if (cellEditor != null) {
			Control control = cellEditor.getControl();
			if (control != null) {
				Composite parent = control.getParent();
				if (parent instanceof FigureCanvas) {
					SWTEventRecorder eventRecorder = getEventRecorder();
					SWTUIPlayer player = eventRecorder.getPlayer();
					GraphicalViewer viewer = GefProcessor.findDiagramViewer(
							player.wrap(parent),
							GraphicalViewer.class, null);
					if (viewer != null) {
						if (isSkipDiagramOperations()) {
							return;
						}

						GraphicalViewerEntry entry = locator
								.getDiagramUIElement(viewer);
						if (entry != null) {
							entry.diagramViewer.cancelDirectEdit();
						}
					}
				}
			}
		} else {
			if (directEditPart != null) {
				GraphicalViewer viewer = (GraphicalViewer) directEditPart
						.getViewer();
				if (viewer != null) {
					if (isSkipDiagramOperations()) {
						return;
					}

					GraphicalViewerEntry entry = locator
							.getDiagramUIElement(viewer);
					if (entry != null) {
						entry.diagramViewer.cancelDirectEdit();
					}
				}
			}
		}
		directEditPart = null;
	}

	public void commitDirectEdit(DirectEditorContainer container) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		CellEditor cellEditor = container.getCellEditor();
		if (cellEditor != null) {
			Control control = cellEditor.getControl();
			if (control != null) {
				Composite parent = control.getParent();
				if (parent instanceof FigureCanvas) {
					SWTEventRecorder eventRecorder = getEventRecorder();
					SWTUIPlayer player = eventRecorder.getPlayer();
					GraphicalViewer viewer = GefProcessor.findDiagramViewer(
							player.wrap(parent),
							GraphicalViewer.class, null);
					if (viewer != null) {
						if (isSkipDiagramOperations()) {
							return;
						}

						GraphicalViewerEntry entry = locator
								.getDiagramUIElement(viewer);
						if (entry != null) {
							entry.diagramViewer.commitDirectEdit();
						}
					}
				}
			}
		} else if (directEditPart != null) {
			GraphicalViewer viewer = (GraphicalViewer) directEditPart
					.getViewer();
			if (viewer != null) {
				if (isSkipDiagramOperations()) {
					return;
				}

				GraphicalViewerEntry entry = locator
						.getDiagramUIElement(viewer);
				if (entry != null) {
					entry.diagramViewer.commitDirectEdit();
				}
			}
		}
		directEditPart = null;
	}

	public void setFreeze(boolean value, SetMode command) {
	}

	@SuppressWarnings("rawtypes")
	private void fillDragParts(EditPart editPart, Set<EditPart> dragParts,
			EditPartViewer viewer) {
		if (editPart != null) {
			dragParts.clear();
			dragParts.add(editPart);
		}
		// Add all selected edit parts to list
		// List parts = viewer.getSelectedEditParts();
		// dragParts.addAll(parts);

		if (viewer.getEditDomain() != null) {
			Tool tool = viewer.getEditDomain().getActiveTool();
			if (tool != null && tool instanceof SelectionTool) {
				DragTracker dragTracker = getDragTracker(tool);
				if (dragTracker != null) {
					if (dragTracker instanceof SelectEditPartTracker) {
						Object field = getEditPartField(dragTracker);
						if (field != null) {
							dragParts.add((EditPart) field);
						}
					}
					List<IGMFRecordingProcessor> processors = recorder
							.getProcessors(IGMFRecordingProcessor.class);
					for (IGMFRecordingProcessor pr : processors) {
						pr.updateDragParts(dragParts, dragTracker);
					}

					// Check abstarct tool operations also
					List operationSet = getOperationSet(dragTracker);
					if (operationSet != null) {
						for (Object object : operationSet) {
							if (object instanceof EditPart) {
								dragParts.add((EditPart) object);
							}
						}
					}
				}
			}
		}
		// Remove diagram itself
		dragParts.remove(viewer.getContents());
	}

	@SuppressWarnings("rawtypes")
	private List getOperationSet(DragTracker dragTracker) {
		return TeslaGefAccess.getOperationSet(dragTracker);
	}

	private Object getEditPartField(DragTracker dragTracker) {
		return TeslaGefAccess.getEditPartField(dragTracker);
	}

	private DragTracker getDragTracker(Tool tool) {
		return TeslaGefAccess.getDragTracker(tool);
	}

	public void handleDelayedDirectEdit(Object helper) {
		if (recorder == null) {
			return;
		}
		if (!recorder.hasListeners()) {
			return;
		}
		final EditPart value = getEditPart(helper);
		if (directEditPart != null && directEditPart.equals(value)) {
			return; // Already in direct edit
		}
		// eclipse 3.4 support: PaletteEditPart in different packages
		if (TeslaGefAccess.isPalleteEditPart(value)
				&& TeslaFeatures.getInstance().isTrue(
						DiagramFeatures.STORE_PALETTE_AS_NAME)) {
			return;
		}
		directEditPart = value;
		final Object activeHelper = getActiveHelper(helper);

		if (activeHelper != null && value != null && value.isActive()) {
			EditPartViewer viewer = value.getViewer();
			if (isSkipDiagramOperations()) {
				return;
			}

			GraphicalViewerEntry entry = locator.getDiagramUIElement(viewer);
			if (entry != null) {
				FigureUIElement partElement = locator.find(value, viewer,
						entry.diagramViewer, false);
				if (partElement != null) {
					partElement.activateDirectEdit(null);
				}
			}
		}
	}

	private Object getActiveHelper(Object helper) {
		return TeslaGefAccess.getActiveHelper(helper);
	}

	private EditPart getEditPart(Object helper) {
		return TeslaGefAccess.getEditPart(helper);
	}

	public TeslaRecorder getRecorder() {
		return recorder;
	}

	public GefFigureLocator getLocator() {
		return locator;
	}

	public Element findDiagramElement(Canvas canvas) {
		if (canvas instanceof FigureCanvas) {
			Object canvasOwner = TeslaSWTAccess.getThis(EditPartViewer.class,
					canvas, SWT.Dispose);
			if (canvasOwner instanceof EditPartViewer) {
				GraphicalViewerEntry entry = locator
						.getDiagramUIElement((EditPartViewer) canvasOwner);
				if (entry != null) {
					return entry.diagramViewer.getElement();
				}
			}
		}
		return null;
	}

	public void resetAssertSelection() {
	}

	public IRecordingHelper<?> getHelper() {
		return GefRecordingHelper.getHelper();
	}

	@Override
	public boolean isNotCanvas(Object widget, int type, Object event) {
		return isNotDraw2d(widget);
	}

	@Override
	public boolean isIgnored(Object widget, int type, Object event) {
		return false;
	}

	@Override
	public boolean isNotDraw2d(Object widget) {
		if (widget instanceof Widget && getGraphicalViewer((Widget) widget) != null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isPartOfParent(Object widget, Object parent) {
		return false;
	}
}
