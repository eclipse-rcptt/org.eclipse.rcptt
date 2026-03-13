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
package org.eclipse.rcptt.tesla.nebula;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.GenericElementKind;
import org.eclipse.rcptt.tesla.internal.ui.player.AbstractSWTUIPlayerExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.ChildrenCollectingSession;
import org.eclipse.rcptt.tesla.internal.ui.player.IChildrenCollectingExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.ItemUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerSelectionFilter;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.nebula.ecl.NebulaElementKinds;
import org.eclipse.rcptt.tesla.nebula.grid.parts.EmptyArea;
import org.eclipse.rcptt.tesla.nebula.grid.parts.GridPart;
import org.eclipse.rcptt.tesla.nebula.grid.parts.ItemCell;
import org.eclipse.rcptt.tesla.nebula.grid.parts.RowHeader;
import org.eclipse.rcptt.tesla.nebula.viewers.NebulaViewers;
import org.eclipse.rcptt.util.swt.Bounds;
import org.eclipse.rcptt.util.swt.Events;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

public class NebulaUIPlayerExtension extends AbstractSWTUIPlayerExtension {

	@Override
	public SWTUIElement wrap(Object s, SWTUIPlayer p) {
		if (s instanceof Grid)
			return new NebulaUIElement((Widget) s, p);
		if (s instanceof GridPart)
			return new NebulaPartUIElement((GridPart) s, p);
		return null;
	}
	
	private static final class GridCell extends ItemUIElement {

		public GridCell(GridItem w, SWTUIPlayer p, int column) {
			super(w, p, column);
		}
				
		@Override
		public GridItem unwrap() {
			return (GridItem) super.unwrap();
		}
	}

	@Override
	public SWTUIElement select(SWTUIPlayer p, PlayerSelectionFilter f) {

		switch (f.kind.kind) {
		case ColumnHeader:
			return selectColumnHeader(p, f);
		case Item:
			Widget parentWidget = f.parent.unwrap();
			if(parentWidget instanceof Grid grid) {
				if (f.path != null) {
					GridItem item = (GridItem) NebulaViewers.searchGridItem(
							(NebulaUIElement) f.parent, f.path);
					if (item != null) {					
						return new GridCell(item, p, 0);
					}
				}
				if (f.indexes.length == 2) {
					int column = f.indexes[0];
					int row = f.indexes[1];
					var item = grid.getItem(row);
					if (item == null) {
						return null;
					}
					return new GridCell(item, p, column);
				}

			}

		case Custom:
			if (f.kind.is(NebulaElementKinds.GRID))
				return p.selectWidget(f.withoutPattern(), false, Grid.class);
			else if (f.kind.is(NebulaElementKinds.ROW_HEADER))
				return selectRowHeader(p, f);
			else if (f.kind.is(NebulaElementKinds.ITEM_CELL))
				return ItemCell.select(p, f);
			else if (f.kind.is(NebulaElementKinds.EMPTY_AREA))
				return EmptyArea.select(p, f);
		}

		return null;
	}
	
	@Override
	public boolean canClick(SWTUIElement widget, boolean isDefault, boolean doubleClick, boolean arrow) {
		return widget instanceof GridCell;
	}
	
	@Override
	public void click(SWTUIElement widget, boolean isDefault, boolean doubleClick, boolean arrow, int stateMask) {
		GridCell gridCell = (GridCell)widget;
		GridItem gridItem = gridCell.unwrap();
		Grid grid = gridItem.getParent();
		final Event[] event = Events.createClick(Bounds.centerAbs(gridItem.getBounds(gridCell.getColumn())));
		widget.getPlayer().exec("Performing click on a Nebula Grid cell", new Runnable() {
			@Override
			public void run() {
				widget.getPlayer().getEvents().sendAll(grid, event);
			}
		});				

	}

	@Override
	public GenericElementKind getKind(Object w) {
		if (w instanceof Grid)
			return new GenericElementKind(NebulaElementKinds.GRID);
		if (w instanceof GridItem)
			return new GenericElementKind(ElementKind.Item);
		if (w instanceof GridColumn)
			return new GenericElementKind(ElementKind.ColumnHeader);
		// if (w instanceof RowHeader)
		// return ElementKind.Custom;
		return null;
	}

	private SWTUIElement selectColumnHeader(SWTUIPlayer p,
			PlayerSelectionFilter f) {
		Widget unwrapped = PlayerWrapUtils.unwrapWidget(f.parent);
		if (!(unwrapped instanceof Grid))
			return null;

		return p.wrap(NebulaViewers.findColumn((Grid) unwrapped,
				f.pattern, f.index == null ? 0 : f.index));
	}

	private SWTUIElement selectRowHeader(SWTUIPlayer p, PlayerSelectionFilter f) {
		Widget item = PlayerWrapUtils.unwrapWidget(f.parent);
		if (!(item instanceof GridItem))
			return null;

		return p.wrap(new RowHeader((GridItem) item));
	}

	//

	@Override
	public IChildrenCollectingExtension getChildrenCollectingExtension(ChildrenCollectingSession s) {
		return new NebulaChildrenCollectingExtension(s);
	}

	@Override
	public SWTUIElement getShell(SWTUIElement element) {
		if (element instanceof NebulaPartUIElement) {
			GridPart part = ((NebulaPartUIElement) element).part;

			SWTUIPlayer player = element.getPlayer();
			return player.wrap(part.grid().getShell());
		}
		return null;
	}
	

}
