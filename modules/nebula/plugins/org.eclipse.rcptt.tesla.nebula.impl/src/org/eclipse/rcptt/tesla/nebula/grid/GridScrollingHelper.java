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
package org.eclipse.rcptt.tesla.nebula.grid;

import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrapWidget;

import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.rcptt.tesla.core.protocol.SelectData;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.nebula.NebulaUIElement;
import org.eclipse.rcptt.tesla.nebula.ecl.NebulaElementKinds;
import org.eclipse.rcptt.tesla.nebula.grid.parts.EmptyArea;
import org.eclipse.rcptt.tesla.nebula.grid.parts.ItemCell;
import org.eclipse.rcptt.tesla.nebula.grid.parts.ItemPart;
import org.eclipse.rcptt.tesla.nebula.viewers.NebulaViewers;

public class GridScrollingHelper {

	public static void scrollGridFor(NebulaUIElement grid, List<String> path) {
		GridItem item = NebulaViewers.searchGridItem(grid, path);
		showItemIfHidden(item);
	}

	public static boolean scrollGridFor(SelectData data, SWTUIElement parent) {
		Widget parentWidget = unwrapWidget(parent);

		// TODO implement it for whole item selection case (e.g. select "Item #3")

		if (parentWidget instanceof Grid grid) {
			GridItem item = (GridItem) NebulaViewers.searchGridItem(
					(NebulaUIElement) parent, data.getPath());
			if (!showItemIfHidden(item)) {
				return false;
			}
			if (data.getKind().contentEquals(NebulaElementKinds.ITEM_CELL)) {
				ItemCell cell = ItemCell.from(data, item, data.getIndex());
				return showColumnIfHidden(cell.column); // scroll horizontally to the column
			}
		} else if (parentWidget instanceof GridItem item) {
			if (!showItemIfHidden(item)) {
				return false;
			}

			if (data.getKind().contentEquals(NebulaElementKinds.ITEM_CELL)) {
				ItemCell cell = ItemCell.from(data, item, data.getIndex());
				return showColumnIfHidden(cell.column); // scroll horizontally to the column
			}
		}
		else if (data.getKind().contentEquals(NebulaElementKinds.EMPTY_AREA)) {
			Grid grid = (Grid) unwrapWidget(parent); // GridItem case is handled above

			EmptyArea area = EmptyArea.fromPath(data.getPath().toArray(new String[] {}), grid);
			if (!area.top) {
				// scroll grid to bottom to see the empty area
				grid.setTopIndex(grid.getItemCount() - 1);
				return false;
			}

			// scroll horizontally
			if (area.column != null) 
				return showColumnIfHidden(area.column); // to see the column
			else if (!area.left)
				return showColumnIfHidden(NebulaViewers.getGridLastColumn(grid)); // to see an empty area on the right
		}
		return true;
	}

	//

	public static boolean showItemIfHidden(GridItem item) {
		if (NebulaViewers.getItemBounds(item) == null || !item.isVisible()) {
			Grid grid = item.getParent();
			grid.showItem(item);
			return false;
		}
		
		return true;
	}

	public static boolean showColumnIfHidden(GridColumn column) {
		Grid grid = column.getParent();
		Rectangle bounds = NebulaViewers.getColumnHeaderBounds(column);

		if (bounds == null || bounds.x > grid.getBounds().width) {
			grid.showColumn(column);
			return false;
		}
		return true;
	}

	public static void showPartIfHidden(ItemPart part) {
		showItemIfHidden(part.item);
		if (part instanceof ItemCell)
			showColumnIfHidden(((ItemCell) part).column);
	}

}
