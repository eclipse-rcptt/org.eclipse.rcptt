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
package org.eclipse.rcptt.tesla.recording.gef;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.text.FlowFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.rcptt.tesla.gef.GefProcessor;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.recording.core.TeslaRecorder;
import org.eclipse.rcptt.tesla.recording.core.swt.IRecorderDescriberExtension;
import org.eclipse.rcptt.tesla.recording.core.swt.IRecordingDescriber;
import org.eclipse.rcptt.tesla.recording.core.swt.RecordingWidgetDescriber;
import org.eclipse.rcptt.tesla.recording.core.swt.SWTEventRecorder;
import org.eclipse.swt.widgets.Widget;

public class GEFDescriberRecorderExtension implements
		IRecorderDescriberExtension {

	private SWTEventRecorder swtProcessor;

	public GEFDescriberRecorderExtension() {
	}

	private IFigure skipFlowFigures(IFigure figureAt, IFigure figure) {
		while (figureAt instanceof FlowFigure) {
			figureAt = figureAt.getParent();
			if (figure.equals(figureAt)) {
				break;
			}
		}
		return figureAt;
	}

	@Override
	public IRecordingDescriber getDescriber(Widget widget,
			IRecordingDescriber previous, int x, int y, boolean fromAsser) {
		if (widget instanceof FigureCanvas) {
			FigureCanvas canvas = (FigureCanvas) widget;
			SWTUIPlayer player = getSWTProcessor(TeslaRecorder.getInstance()).getPlayer();
			GraphicalViewer viewer = GefProcessor.findDiagramViewer(
					player.wrap(canvas), GraphicalViewer.class, null);
			EditPart editPart = viewer == null ? null : viewer.findObjectAt(new Point(x, y));
			if (editPart instanceof RootEditPart) {
				return new RecordingEditPartDescriber(viewer.getContents(), viewer);
			}

			if (editPart != null) {
				IFigure figure = ((GraphicalEditPart) editPart).getFigure();
				IFigure figureAt = null;

				// Gets viewer location (scrolling offset)
				Point location = null;
				if (viewer != null) {
					FigureCanvas figureCanvas = (FigureCanvas) viewer.getControl();
					if (figureCanvas != null) {
						location = figureCanvas.getViewport().getViewLocation();
					}
				}
				// Searches figure
				if (location != null) {
					figureAt = figure.findFigureAt(x + location.x, y + location.y);
				} else {
					figureAt = figure.findFigureAt(x, y);
				}

				if (figureAt != null && !figure.equals(figureAt)) {
					figureAt = skipFlowFigures(figureAt, figure);
					return new RecordingFigureDescriber(figureAt, editPart, viewer, canvas);
				}
				return new RecordingEditPartDescriber(editPart, viewer);
			}
			// Try to locate figure
			// IFigure figure = canvas.getContents();
			// IFigure figureAt = figure.findFigureAt(x, y);
			// if (figureAt != null && figureAt.isShowing()) {
			// return new RecordingFigureDescriber(figureAt, null, viewer,
			// canvas);
			// }
		}
		if (previous != null) {
			return previous;
		}
		return new RecordingWidgetDescriber(widget);
	}

	private SWTEventRecorder getSWTProcessor(TeslaRecorder recorder) {
		if (swtProcessor == null) {
			swtProcessor = recorder.getProcessor(SWTEventRecorder.class);
		}
		return swtProcessor;
	}
}
