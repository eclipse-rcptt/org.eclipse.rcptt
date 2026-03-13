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
package org.eclipse.rcptt.tesla.recording.nebula;

import static org.eclipse.rcptt.util.swt.Events.pointFrom;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.rcptt.tesla.core.protocol.ControlUIElement;
import org.eclipse.rcptt.tesla.core.protocol.ViewerUIElement;
import org.eclipse.rcptt.tesla.core.protocol.raw.SetMode;
import org.eclipse.rcptt.tesla.internal.ui.player.FindResult;
import org.eclipse.rcptt.tesla.internal.ui.player.ISWTModelMapperExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTModelMapper;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.nebula.NebulaUIProcessor;
import org.eclipse.rcptt.tesla.nebula.ecl.NebulaElementKinds;
import org.eclipse.rcptt.tesla.nebula.grid.parts.ColumnHeader;
import org.eclipse.rcptt.tesla.nebula.grid.parts.GridPart;
import org.eclipse.rcptt.tesla.nebula.viewers.NebulaViewers;
import org.eclipse.rcptt.tesla.recording.aspects.IBasicSWTEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.SWTEventManager;
import org.eclipse.rcptt.tesla.recording.core.ICellEditsProcessor;
import org.eclipse.rcptt.tesla.recording.core.IRecordingHelper;
import org.eclipse.rcptt.tesla.recording.core.IRecordingProcessor;
import org.eclipse.rcptt.tesla.recording.core.TeslaRecorder;
import org.eclipse.rcptt.tesla.recording.core.swt.peg.CommandPostProcessor;
import org.eclipse.rcptt.tesla.recording.nebula.rules.RemoveRedundantEnterAfterApplyCellEditor;
import org.eclipse.rcptt.tesla.recording.nebula.rules.RemoveRedundantSetSelectionsBeforeActivateCellEditor;
import org.eclipse.rcptt.tesla.recording.nebula.rules.RemoveSetSelectionBeforeSetSelectionRange;

public class NebulaRecordingProcessor extends NebulaRecordingDoodah
		implements IRecordingProcessor, IBasicSWTEventListener, ISWTModelMapperExtension, ICellEditsProcessor {

	private static int[] interestingEvents = { SWT.Selection, SWT.DefaultSelection,
			SWT.Show, SWT.MouseDoubleClick, SWT.MenuDetect, SWT.MouseDown };

	private Event lastEvent;

	public NebulaRecordingProcessor() {
		super();
		SWTEventManager.addListener(this);
		getLocator().addExtension(new NebulaWidgetLocatorExtension(getLocator()));
	}

	@Override
	public void recordEvent(Widget widget, int type, Event event) {
		// ignoring not interesting events,
		// it is essential to filter some event types like focusing
		// to correctly determine menu source
		if (isInterestingEvent(type)) {
			switch (type) {
			case SWT.Selection:
				// case SWT.DefaultSelection:
				NebulaGridSelectionRecorder.processSelection(widget, event, type);
				break;
			case SWT.Show:
				// try to find a part of the grid, from where the menu has been
				// called
				if (widget instanceof Menu) {
					if (lastEvent != null && lastEvent.widget instanceof Grid) {
						Point p = pointFrom(lastEvent);
						Point point = (lastEvent.type == SWT.MenuDetect) ?
								lastEvent.widget.getDisplay().map(null, (Control) lastEvent.widget, p) : p;
						GridPart menuSource = NebulaViewers.getGridPartFromPoint((Grid) lastEvent.widget, point);
						if (menuSource != null)
							SWTEventManager.setMenuSource((Menu) widget, menuSource);
					}
				}
				break;
			case SWT.MouseDoubleClick:
				Listener[] listeners = widget.getListeners(SWT.MouseDoubleClick);
				if (listeners.length != 0) {
					if (widget instanceof Grid) {
						GridPart part = NebulaViewers.getGridPartFromPoint((Grid) widget, pointFrom(event));
						if (part instanceof ColumnHeader)
							break; // QS-3113 :: ignore double-clicks on column-headers
						SWTUIElement wrappedPart = getLocator().getPlayer().wrap(part);
						
						FindResult result = getLocator().findElement(wrappedPart, true, false, false);
						if (result != null) {
							// ?? if (result.realElement instanceof NebulaPartUIElement) { ...
							ControlUIElement c = new ControlUIElement(result.element, getRecorder());
							c.doubleClick();
						}
					}
				}
				break;
			}
			lastEvent = event;
		}
	}

	private boolean isInterestingEvent(int type) {
		for (Integer u : interestingEvents)
			if (u == type)
				return true;
		return false;
	}


	// IRecordingProcessor implementation

	@Override
	public void initialize(TeslaRecorder teslaRecorder) {
		CommandPostProcessor.addRule(new RemoveSetSelectionBeforeSetSelectionRange());
		CommandPostProcessor.addRule(new RemoveRedundantSetSelectionsBeforeActivateCellEditor());
		CommandPostProcessor.addRule(new RemoveRedundantEnterAfterApplyCellEditor());
	}

	@Override
	public void clear() {
		lastEvent = null;
		NebulaRecordingHelper.getHelper().clear();
		SWTModelMapper.initializeExtensions(getRecorder().getProcessors(
				ISWTModelMapperExtension.class));
		getLocator().cleanMenuSources();
	}

	@Override
	public void setFreeze(boolean value, SetMode command) {
	}

	@Override
	public void resetAssertSelection() {
	}

	@Override
	public IRecordingHelper<?> getHelper() {
		return NebulaRecordingHelper.getHelper();
	}

	@Override
	public boolean isExclusiveEventHandle(Widget widget, int type, Event event) {
		if (widget instanceof Grid
				&& type != SWT.KeyDown && type != SWT.KeyUp) // let SWTEventRecorder handle them
			return true;
		else if (widget instanceof Menu
				&& lastEvent != null && lastEvent.widget instanceof Grid)
			return true;

		return false;
	}

	@Override
	public int getInitLevel() {
		return 20;
	}

	//


	@Override
	public org.eclipse.rcptt.tesla.core.ui.Widget mapExtraValues(SWTUIElement element,
			org.eclipse.rcptt.tesla.core.ui.Widget result) {
		return mapWidget(element, result);
	}

	public static org.eclipse.rcptt.tesla.core.ui.Widget mapWidget(
			SWTUIElement element, org.eclipse.rcptt.tesla.core.ui.Widget result) {
		return NebulaUIProcessor.mapWidget(element, result);
	}


	// ICellEditsProcessor implementation methods

	@Override
	public boolean activateCellEdit(Object oeditor, Object oevent) {
		CellEditor editor = (CellEditor) oeditor;
		ColumnViewerEditorActivationEvent event = (ColumnViewerEditorActivationEvent) oevent;
		Control control = editor.getControl();
		if (control == null)
			return false;

		Composite parent = control.getParent();
		if (!(parent instanceof Grid))
			return false;

		Grid grid = (Grid) parent;

		String[][] selection = NebulaViewers.getMultiPathSelection(getLocator()
				.getPlayer().wrap(parent));

		ControlEditor controlEditor = TeslaSWTAccess.getControlEditor(grid);
		int column = -1;
		if (controlEditor instanceof GridEditor)
			column = ((GridEditor) controlEditor).getColumn();
		if (column == -1 && event != null
				&& event.getSource() instanceof ViewerCell)
			column = ((ViewerCell) event.getSource()).getColumnIndex();
		if (column == -1)
			return false;

		FindResult result = getLocator().findElement(grid, true, false, false);
		if (!result.realElement.getKind().is(NebulaElementKinds.GRID))
			return false;
		ViewerUIElement e = new ViewerUIElement(result.element, getRecorder());
		e.setMultiSelection(selection);
		e.activateCellEditor(column);

		return true;
	}

	@Override
	public boolean applyCellEdit(Object oeditor) {
		CellEditor editor = (CellEditor) oeditor;
		Control control = editor.getControl();
		if (control == null)
			return false;

		Composite parent = control.getParent();
		if (!(parent instanceof Grid))
			return false;

		Grid grid = (Grid) parent;

		FindResult result = getLocator().findElement(grid, true, false, false);
		if (!result.realElement.getKind().is(NebulaElementKinds.GRID))
			return false;
		ViewerUIElement e = new ViewerUIElement(result.element, getRecorder());
		// actually, the column value is never used during playback
		e.applyCellEditor();

		return true;
	}

	@Override
	public boolean cancelCellEdit(Object oeditor) {
		CellEditor editor = (CellEditor) oeditor;
		Control control = editor.getControl();
		if (control == null)
			return false;

		Composite parent = control.getParent();
		if (!(parent instanceof Grid))
			return false;

		Grid grid = (Grid) parent;

		FindResult result = getLocator().findElement(grid, true, false, false);
		if (!result.realElement.getKind().is(NebulaElementKinds.GRID))
			return false;
		ViewerUIElement e = new ViewerUIElement(result.element, getRecorder());
		// actually, the column value is never used during playback
		e.cancelCellEditor();

		return true;
	}
	@Override
	public boolean deactivateCellEdit(Object oeditor) {
		CellEditor editor = (CellEditor) oeditor;
		Control control = editor.getControl();
		if (control == null)
			return false;

		Composite parent = control.getParent();
		if (!(parent instanceof Grid))
			return false;

		Grid grid = (Grid) parent;

		FindResult result = getLocator().findElement(grid, true, false, false);
		if (!result.realElement.getKind().is(NebulaElementKinds.GRID))
			return false;
		ViewerUIElement e = new ViewerUIElement(result.element, getRecorder());
		// actually, the column value is never used during playback
		e.deactivateCellEditor();

		return true;
	}

	//

}
