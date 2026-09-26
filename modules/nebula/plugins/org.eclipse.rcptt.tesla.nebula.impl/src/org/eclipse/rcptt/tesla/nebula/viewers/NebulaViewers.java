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
package org.eclipse.rcptt.tesla.nebula.viewers;

import static java.util.Arrays.asList;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrapWidget;
import static org.eclipse.rcptt.util.ListUtil.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTEvents;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.nebula.NebulaUIElement;
import org.eclipse.rcptt.tesla.nebula.grid.parts.ColumnHeader;
import org.eclipse.rcptt.tesla.nebula.grid.parts.EmptyArea;
import org.eclipse.rcptt.tesla.nebula.grid.parts.GridPart;
import org.eclipse.rcptt.tesla.nebula.grid.parts.ItemCell;
import org.eclipse.rcptt.tesla.nebula.grid.parts.RowHeader;
import org.eclipse.rcptt.tesla.ui.IViewerItem;
import org.eclipse.rcptt.util.Predicate;
import org.eclipse.rcptt.util.TableTreeItemPathUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Widget;

public class NebulaViewers {

	public static String[] getPathSelection(SWTUIElement uiElement) {
		Widget widget = unwrapWidget(uiElement);

		if (widget instanceof Grid) {
			Grid grid = (Grid) widget;
			GridItem[] selection = grid.getSelection();
			if (selection != null && selection.length == 1) {
				return getPathByGridItem(selection[0]);
			}
		}

		return null;
	}

	public static String[] getPathByGridItem(GridItem selection) {
		List<String> path = new ArrayList<String>();
		path.add(SWTUIPlayer.toSelectionItem(getGridItemText(selection)));
		GridItem parentItem = selection.getParentItem();
		while (parentItem != null && !parentItem.isDisposed()) {
			path.add(0,
					SWTUIPlayer.toSelectionItem(getGridItemText(parentItem)));
			parentItem = parentItem.getParentItem();
		}
		return path.toArray(new String[path.size()]);
	}

	public static String[][] getMultiPathSelection(SWTUIElement uiElement) {
		Widget widget = unwrapWidget(uiElement);
		if (widget instanceof Grid) {
			Grid grid = (Grid) widget;
			GridItem[] selection = grid.getSelection();
			if (selection != null) {
				List<String[]> paths = new ArrayList<String[]>();
				for (int i = 0; i < selection.length; i++) {
					List<String> path = new ArrayList<String>();
					path.add(SWTUIPlayer
							.toSelectionItem(getGridItemText(selection[i])));
					GridItem parentItem = selection[i].getParentItem();
					while (parentItem != null) {
						path.add(0, SWTUIPlayer
								.toSelectionItem(getGridItemText(parentItem)));
						parentItem = parentItem.getParentItem();
					}
					paths.add(path.toArray(new String[path.size()]));
				}
				return paths.toArray(new String[paths.size()][]);
			}
		}
		return null;
	}

	static List<GridItem> getGridRootItems(Grid grid) {
		GridItem[] items = grid.getItems(); // all the items, not only root
		return filter(asList(items), new Predicate<GridItem>() {
			@Override
			public boolean apply(GridItem it) {
				return (it.getParentItem() == null);
			}
		});
	}

	public static int getGridRootItemsCount(Grid grid) {
		return getGridRootItems(grid).size();
	}

	public static int getGridItemIndex(GridItem item) {
		GridItem parent = item.getParentItem();
		if (parent != null)
			return parent.indexOf(item);

		return getGridRootItems(item.getParent()).indexOf(item);
	}

	public static String getGridItemText(GridItem gridItem) {
		Grid parent = gridItem.getParent();
		int columnCount = parent.getColumnCount();
		if (columnCount > 0) {
			// Also add same index to end
			return getIndexedItemText(new GridViewerItem(gridItem), true);
		}
		return getIndexedItemText(new GridViewerItem(gridItem), false);
	}

	// TODO should be refactored with Viewers.getIndexedItemText
	static String getIndexedItemText(IViewerItem item, boolean firstColumn) {
		String value = firstColumn ? item.getText(0) : item.getText();
		IViewerItem[] items = item.getParentItems();
		if (items != null) {
			int index = 0;
			for (int i = 0; i < items.length; i++) {
				if (item.matches(items[i])) {
					break;
				}
				String val2 = firstColumn ? items[i].getText(0) : items[i]
						.getText();
				if (val2.equals(value)) {
					index++;
				}
			}
			if (index > 0) {
				return value + "%" + index + "%";
			}
		}
		if (value.endsWith("%")) {
			return value + "%0%";
		}
		return value; // Indicate using first column
	}

	public static boolean checkItem(boolean state, NebulaUIElement element,
			String[] selection, String pattern, Integer index) {
		Boolean result = null;
		Widget widget = unwrapWidget(element);
		if (widget instanceof Grid) {
			if (selection == null && pattern != null) {
				selection = new String[] { pattern };
			}
			result = checkGridItem(state, element, selection);
		}
		if (result != null && !result.booleanValue()) {
			// makeScreenShot();
		}
		return result != null && result.booleanValue();
	}

	public static boolean checkGridItem(final boolean newState,
			final NebulaUIElement parent, final String[] path) {

		if (parent != null && unwrapWidget(parent) instanceof Grid
				&& path != null) {
			final SWTUIPlayer player = parent.getPlayer();
			final Grid grid = (Grid) unwrapWidget(parent);
			final GridItem current = (GridItem) searchGridItem(parent, path);
			if (current != null) {
				grid.forceFocus();
				player.exec("checkGridItem for tree", new Runnable() {
					public void run() {
						SWTEvents player = parent.getPlayer().getEvents();
						current.setChecked(newState);
						player.sendEvent(grid, current, SWT.Selection,
								SWT.CHECK);
					}
				});
				return true;
			}
		}
		return false;
	}

	public static GridItem searchGridItem(NebulaUIElement parent, String[] path) {
		return searchGridItem(parent, Arrays.asList(path));
	}

	public static GridItem searchGridItem(NebulaUIElement parent, List<String> path) {
		Object current = unwrapWidget(parent);
		for (String part : path) {
			GridItem[] items = getItems(current);
			if (items == null) {
				return null;
			}
			current = new PathSelectorPart(TableTreeItemPathUtil.excludeColumnName(part)).findIn(items);
			if (current == null)
				return null;
		}
		return (GridItem) current;
	}

	private static GridItem[] getItems(Object current) {
		GridItem[] items = null;
		if (current instanceof Grid) {
			items = getGridRootItems((Grid) current).toArray(new GridItem[] {});
		} else if (current instanceof GridItem) {
			GridItem item = (GridItem) current;
			items = item.getItems();
		}
		return items;
	}

	public static int expandSelection(NebulaUIElement element,
			String[] selection, String pattern, Integer index) {
		int result = -1;
		Widget widget = unwrapWidget(element);
		if (widget instanceof Grid) {
			if (selection == null && pattern != null) {
				selection = new String[] { pattern };
			}
			result = expandGridItem(element, selection);
		}
		return result;
	}

	public static int expandGridItem(final NebulaUIElement parent,
			final String[] path) {
		Widget widget = unwrapWidget(parent);
		if (widget instanceof Grid && path != null) {
			final Grid grid = (Grid) widget;
			Object current = grid;
			for (int i = 0; i < path.length; i++) {
				String part = path[i];
				GridItem[] items = getItems(current);

				GridItem gridItem = new PathSelectorPart(part).findIn(items);
				if (gridItem == null)
					return -1;
				else {
					if (gridItem.isExpanded() || i == path.length - 1)
						current = gridItem;
					else {
						// Pre expand
						expandGridItem(parent.getPlayer(), grid, gridItem);
						return 0; // Need to wait for expanding
					}
				}
			}
			return 1;
		}
		return -1;
	}

	public static void expandGridItem(final SWTUIPlayer player,
			final Grid grid, final GridItem gridItem) {
		player.exec("Run asyncs on nebula grid", new Runnable() {
			public void run() {
				try {
					Object data = gridItem.getData();
					if (TeslaSWTAccess.ignoreMarkersView(data)) {
						return;
					}
					player.getEvents().sendEvent(grid, gridItem, SWT.Expand);
					gridItem.setExpanded(true);
				} catch (Throwable e) {
					TeslaCore.log(e);
				}
			}
		});
	}

	public static GridColumn findColumn(Grid grid, String name, int index) {
		GridColumn[] columns = grid.getColumns();
		if (columns == null | columns.length == 0)
			return null;

		int current = 0;
		for (int i : grid.getColumnOrder()) {
			GridColumn c = columns[i];
			if (name == null || name.equals(c.getText()))
				if (current++ == index)
					return c;
		}

		return null;
	}

	/**
	 * CAUTION: only Y-coordinates (y & height) are correct in current
	 * implementation
	 */
	public static Rectangle getItemBounds(GridItem item) {
		Grid grid = item.getParent();
		
		// '-1000' here is a 'special' number from
		// GridItem.getBounds method implementation,
		// it is needed for scrolled grids handling:
		// -- taking the next column if current one is hidden
		for (int i = 0; i < grid.getColumnCount(); i++) {
			Rectangle itemBounds = item.getBounds(i);
			if (itemBounds.x != -1000)
				return itemBounds;
		}
		return null;
	}

	public static Rectangle getColumnHeaderBounds(GridColumn column) {
		Grid grid = column.getParent();
		if (!grid.getHeaderVisible())
			return null;

		int h = grid.getHeaderHeight();
		int x = grid.getItemHeaderWidth();

		int skipColumns = 0;
		ScrollBar bar = grid.getHorizontalBar();
		if (bar != null) {
			if (grid.getColumnScrolling())
				skipColumns = bar.getSelection(); // columns count
			else
				x -= bar.getSelection(); // position in pixels
		}
		
		GridColumn[] columns = grid.getColumns();
		for (int i : grid.getColumnOrder()) {
			GridColumn c = columns[i];

			// skip scrolled & hidden columns,
			// change the counter only if the column is visible
			if (!c.isVisible() || skipColumns-- > 0)
				continue;

			int w = c.getWidth();
			if (c == column)
				return new Rectangle(x, 0, w, h);
			x += w;
		}

		return null;
	}

	private static boolean isPointInColumnHeaderArea(Grid grid, Point point) {
		return grid.getHeaderVisible() && (point.y < grid.getHeaderHeight());
	}

	public static GridPart getGridPartFromPoint(Grid grid, Point point) {
		GridColumn column = grid.getColumn(point);
		if (isPointInColumnHeaderArea(grid, point)) {
			if (column != null)
				return new ColumnHeader(column);
			else if (point.x < grid.getItemHeaderWidth())
				return EmptyArea.corner(grid);
			else
				return EmptyArea.topRight(grid);
		}

		GridItem item = grid.getItem(point);
		if (point.x < grid.getItemHeaderWidth())
			return (item != null) ? new RowHeader(item) : EmptyArea.left(grid);
		if (column != null)
			return (item != null) ? new ItemCell(item, column) : EmptyArea.of(column);
			
		return (item != null) ? EmptyArea.of(item) : EmptyArea.right(grid);
	}

	public static ItemCell getItemCellFromCoords(Grid grid, Point coords) {
		return new ItemCell(grid.getItem(coords.y), grid.getColumn(coords.x));
	}

	public static GridColumn getGridLastColumn(Grid grid) {
		return grid.getColumn(grid.getColumnOrder()[grid.getColumnCount() - 1]);
	}

	public static Rectangle getLastVisibleItemBounds(Grid grid) {
		for (int i = grid.getItemCount() - 1; i >= 0; i--) {
			GridItem item = grid.getItem(i);
			Rectangle bounds = getItemBounds(item);
			if (bounds != null)
				return bounds;
		}
		return null;
	}

	public static int getColumnIndex(GridColumn column) {
		Grid grid = column.getParent();
		GridColumn[] cols = grid.getColumns();
		for (int i = 0; i < cols.length; i++)
			if (cols[i] == column)
				return i;
		throw new IllegalArgumentException("column??"); // should not be raised
	}

	public static int getColumnNewPosX(GridColumn col, int newPos) {
		Grid grid = col.getParent();
		if (getColumnCurrentPosition(col) < newPos)
			newPos++;

		if (newPos >= grid.getColumnCount()) {
			// the last position
			GridColumn placeCol = grid.getColumn(grid.getColumnOrder()[newPos - 1]);
			Rectangle b = NebulaViewers.getColumnHeaderBounds(placeCol);
			return b.x + b.width;
		}

		GridColumn placeCol = grid.getColumn(grid.getColumnOrder()[newPos]);
		return NebulaViewers.getColumnHeaderBounds(placeCol).x;
	}

	public static int getColumnCurrentPosition(GridColumn col) {
		Grid grid = col.getParent();
		int colIndex = getColumnIndex(col);
		int[] order = grid.getColumnOrder();
		
		for (int i = 0; i < order.length; i++)
			if (order[i] == colIndex)
				return i;

		throw new IllegalArgumentException("should not raise it! a error in the logic");
	}


}
