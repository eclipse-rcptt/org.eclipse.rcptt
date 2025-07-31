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

import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrap;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.rcptt.util.swt.Bounds;
import org.eclipse.rcptt.util.swt.Events;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.ActivateCellEditor;
import org.eclipse.rcptt.tesla.core.protocol.ApplyCellEditor;
import org.eclipse.rcptt.tesla.core.protocol.BooleanResponse;
import org.eclipse.rcptt.tesla.core.protocol.CancelCellEditor;
import org.eclipse.rcptt.tesla.core.protocol.CheckItem;
import org.eclipse.rcptt.tesla.core.protocol.Click;
import org.eclipse.rcptt.tesla.core.protocol.ClickColumn;
import org.eclipse.rcptt.tesla.core.protocol.DoubleClick;
import org.eclipse.rcptt.tesla.core.protocol.DragCommand;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.IElementProcessorMapper;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolPackage;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectData;
import org.eclipse.rcptt.tesla.core.protocol.SetPosition;
import org.eclipse.rcptt.tesla.core.protocol.SetWidth;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.core.ui.Color;
import org.eclipse.rcptt.tesla.core.ui.Selection;
import org.eclipse.rcptt.tesla.core.ui.UiFactory;
import org.eclipse.rcptt.tesla.ecl.nebula.NebulaFactory;
import org.eclipse.rcptt.tesla.ecl.nebula.NebulaGrid;
import org.eclipse.rcptt.tesla.ecl.nebula.NebulaGridColumn;
import org.eclipse.rcptt.tesla.ecl.nebula.NebulaGridItem;
import org.eclipse.rcptt.tesla.internal.core.AbstractTeslaClient;
import org.eclipse.rcptt.tesla.internal.ui.SWTElementMapper;
import org.eclipse.rcptt.tesla.internal.ui.player.ISWTModelMapperExtension;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTEvents;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTModelMapper;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.internal.ui.processors.CellEditorSupport;
import org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor;
import org.eclipse.rcptt.tesla.jface.TeslaCellEditorManager;
import org.eclipse.rcptt.tesla.nebula.ecl.NebulaElementKinds;
import org.eclipse.rcptt.tesla.nebula.grid.GridScrollingHelper;
import org.eclipse.rcptt.tesla.nebula.grid.parts.GridPart;
import org.eclipse.rcptt.tesla.nebula.grid.parts.ItemPart;
import org.eclipse.rcptt.tesla.nebula.grid.selection.GridSetSelectionProcessor;
import org.eclipse.rcptt.tesla.nebula.viewers.NebulaViewers;
import org.eclipse.rcptt.tesla.protocol.nebula.NebulaPackage;
import org.eclipse.rcptt.tesla.protocol.nebula.SetSelectionEx;
import org.eclipse.rcptt.tesla.protocol.nebula.SetSelectionRange;

public class NebulaUIProcessor extends SWTUIProcessor implements
		ISWTModelMapperExtension {

	private final EClass[] commandsSupported = {
			ProtocolPackage.Literals.CLICK_COLUMN,
			ProtocolPackage.Literals.SELECT_COMMAND,
			ProtocolPackage.Literals.ACTIVATE_CELL_EDITOR,
			ProtocolPackage.Literals.DRAG_COMMAND,
			ProtocolPackage.Literals.CLICK,
			ProtocolPackage.Literals.DOUBLE_CLICK,
			NebulaPackage.Literals.SET_SELECTION_EX,
			NebulaPackage.Literals.SET_SELECTION_RANGE,
			ProtocolPackage.Literals.SET_WIDTH,
			ProtocolPackage.Literals.SET_POSITION };

	private AbstractTeslaClient client;

	private final NebulaUIPlayerExtension extension = new NebulaUIPlayerExtension();

	public NebulaUIProcessor() {
		SWTUIPlayer.addExtension(extension);
	}
	
	@Override
	public int getPriority() {
		return 114;
	}

	@Override
	public void terminate() {
		SWTUIPlayer.removeExtension(extension);
		super.terminate();
	}

	@Override
	public boolean isCommandSupported(Command cmd) {
		EClass ecl = cmd.eClass();
		for (EClass cl : commandsSupported) {
			if (cl.equals(ecl)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canProceed(Context context, Q7WaitInfoRoot info) {
		return true;
	}

	@Override
	public PreExecuteStatus preExecute(Command command, PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		if (command instanceof SelectCommand) {
			SelectData data = ((SelectCommand) command).getData();
			SWTUIElement parent = getMapper().get(data.getParent());

			GridScrollingHelper.scrollGridFor(data, parent);
		}
		// for SetSelectionEx see SelectingParts.mouseActions
		else if (command instanceof SetSelectionRange) {
			SetSelectionRange ssr = (SetSelectionRange) command;
			NebulaUIElement grid = (NebulaUIElement) getMapper().get(ssr.getElement());

			// in general we can't scroll to see both "from" & "to" items,
			// so we will scroll to see "to" item at the execution stage
			// (see SelectingRange[Ex].mouseAction method)

			if (ssr.getFrom() != null) {
				// cells
				ItemPart from = GridSetSelectionProcessor.gridPartFrom(ssr.getFrom(), grid);
				GridScrollingHelper.showPartIfHidden(from);
			} else {
				// items
				List<String> from = ssr.getPath();
				GridScrollingHelper.scrollGridFor(grid, from);
			}
		}
		else if (command instanceof SetWidth) {
			SetWidth sw = (SetWidth) command;
			SWTUIElement s = getMapper().get(sw.getElement());
			GridColumn col = (GridColumn) unwrap(s);

			GridScrollingHelper.showColumnIfHidden(col);
		}
		else if (command instanceof SetPosition) {
			SetPosition sp = (SetPosition) command;
			SWTUIElement s = getMapper().get(sp.getElement());
			GridColumn col = (GridColumn) unwrap(s);
			Grid grid = col.getParent();

			if (NebulaViewers.getColumnCurrentPosition(col) > sp.getIndex())
				col = grid.getColumn(grid.getColumnOrder()[sp.getIndex()]); // scroll to see both columns,
																			// or at least the left of them
			GridScrollingHelper.showColumnIfHidden(col);
		}

		return super.preExecute(command, previousStatus, info);
	}

	@Override
	public Response executeCommand(final Command command,
			final IElementProcessorMapper mapper) {
		EClass eClass = command.eClass();
		EPackage pkg = eClass.getEPackage();

		if (pkg.equals(ProtocolPackage.eINSTANCE))
			switch (eClass.getClassifierID()) {
			case ProtocolPackage.CLICK_COLUMN:
				return handleClickColumn((ClickColumn) command);
			case ProtocolPackage.DRAG_COMMAND:
				return client.getProcessor(SWTUIProcessor.class)
						.getDragSupport().handleDrag((DragCommand) command);
			case ProtocolPackage.CLICK:
				Response r = handleClick((Click) command);
				if (r != null)
					return r;
				else
					return super.handleClick((Click) command);
			case ProtocolPackage.DOUBLE_CLICK:
				return handleDoubleClick((DoubleClick) command);
			case ProtocolPackage.SET_WIDTH:
				return handleSetWidth((SetWidth) command);
			case ProtocolPackage.SET_POSITION:
				return handleSetPosition((SetPosition) command);
			}
		else if (pkg.equals(NebulaPackage.eINSTANCE))
			switch (eClass.getClassifierID()) {
			case NebulaPackage.SET_SELECTION_EX:
				return GridSetSelectionProcessor.executePartSelection((SetSelectionEx) command, getMapper());
			case NebulaPackage.SET_SELECTION_RANGE:
				return GridSetSelectionProcessor.executeRangeSelection((SetSelectionRange) command, getMapper());
			}

		return super.executeCommand(command, mapper);
	}

	@Override
	protected Response handleClick(final Click command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		if (element instanceof NebulaPartUIElement) {
			final GridPart part = ((NebulaPartUIElement) element).part;
			
			final Event[] event = Events.createClick(Bounds.centerAbs(part.bounds()));
			getPlayer().exec("Performing click on a Nebula Grid part", new Runnable() {
				@Override
				public void run() {
					getPlayer().getEvents().sendAll(part.grid(), event);
				}
			});

			return okResponse();
		} else if (command.getElement().getKind().equals(ElementKind.ColumnHeader.name())) {
			final Widget widget = PlayerWrapUtils.unwrapWidget(element);

			Event event = new Event();
			event.type = SWT.Selection;
			getPlayer().getEvents().sendEvent(widget, event);

			return okResponse();
		}
		return null;
	}

	@Override
	protected Response handleDoubleClick(DoubleClick command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		if (element instanceof NebulaPartUIElement) {
			final GridPart part = ((NebulaPartUIElement) element).part;

			final Event[] event = Events.createDoubleClick(Bounds.centerAbs(part.bounds()));
			getPlayer().exec("Performing double-click on a Nebula Grid part", new Runnable() {
				@Override
				public void run() {
					getPlayer().getEvents().sendFocus(part.grid());
					getPlayer().getEvents().sendAll(part.grid(), event);
					getPlayer().getEvents().sendUnfocus(part.grid());
				}
			});

			return okResponse();
		} else
			return super.handleDoubleClick(command);
	}

	protected Response handleSetWidth(final SetWidth command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		if (command.getElement().getKind().equals(ElementKind.ColumnHeader.name())) {
			final GridColumn col = (GridColumn) PlayerWrapUtils.unwrapWidget(element);
			if (!col.getResizeable())
				return failResponse("Can't resize column '" + col.getText() + "'");

			getPlayer().exec("Performing set-width on a Nebula Grid column", new Runnable() {
				@Override
				public void run() {
					int newWidth = command.getWidth();

					Grid grid = col.getParent();
					Rectangle b = NebulaViewers.getColumnHeaderBounds(col);

					final SWTEvents events = element.getPlayer().getEvents();
					events.sendEvent(grid, SWT.MouseMove, b.x + b.width, b.y + b.height / 2, 1);
					events.sendEvent(grid, SWT.MouseDown, b.x + b.width, b.y + b.height / 2, 1);
					events.sendEvent(grid, SWT.MouseMove, b.x + newWidth, b.y + b.height / 2, 1, SWT.BUTTON1);
					events.sendEvent(grid, SWT.MouseUp, b.x + newWidth, b.y + b.height / 2, 1);
				}
			});

			return okResponse();
		}
		return null;
	}

	protected Response handleSetPosition(final SetPosition command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		if (command.getElement().getKind().equals(ElementKind.ColumnHeader.name())) {
			final GridColumn col = (GridColumn) PlayerWrapUtils.unwrapWidget(element);
			if (!col.getMoveable())
				return failResponse("Column '" + col.getText() + "' isn't movable");

			final Grid grid = col.getParent();
			
			final int newPos = command.getIndex();
			if (0 > newPos || newPos >= grid.getColumnCount())
				return failResponse("Can't set position " + newPos + " for column '" + col.getText() + "'");

			getPlayer().exec("Performing set-position on a Nebula Grid column", new Runnable() {
				@Override
				public void run() {
					Point pt = Bounds.centerAbs(NebulaViewers.getColumnHeaderBounds(col));
					int newX = NebulaViewers.getColumnNewPosX(col, newPos);

					final SWTEvents events = element.getPlayer().getEvents();
					events.sendEvent(grid, SWT.MouseDown, pt.x, pt.y, 1);
					events.sendEvent(grid, SWT.MouseMove, pt.x + 4, pt.y, 1, SWT.BUTTON1);
					events.sendEvent(grid, SWT.MouseMove, newX, pt.y, 1, SWT.BUTTON1);
					events.sendEvent(grid, SWT.MouseUp, newX, pt.y, 1);
				}
			});

			return okResponse();
		}
		return null;
	}

	@Override
	protected CellEditorSupport createCellEditorSupport() {
		return new CellEditorSupport(this) {
			@Override
			protected Response handleActivateCellEditor(final ActivateCellEditor command) {
				Element element = command.getElement();
				if (!element.getKind().equals(NebulaElementKinds.GRID))
					return super.handleActivateCellEditor(command);

				SWTUIElement uiElement = getMapper().get(element);
				final Grid grid = (Grid) PlayerWrapUtils.unwrapWidget(uiElement);
				final ColumnViewer viewer = TeslaSWTAccess.getThis(ColumnViewer.class,
						grid, SWT.MouseDown);
				if (viewer != null) {
					getPlayer().exec("Activate editor in Nebula Grid", new Runnable() {
						@Override
						public void run() {
							Object item = ((IStructuredSelection) viewer.getSelection())
									.getFirstElement();
	
							if (item != null)
								viewer.editElement(item, command.getColumn());
						}
					});
				}
				return factory.createBooleanResponse();
			}

			@Override
			protected Response handleCancelCellEditor(final CancelCellEditor command) {
				Element element = command.getElement();
				if (!element.getKind().equals(NebulaElementKinds.GRID))
					return super.handleCancelCellEditor(command);

				SWTUIElement uiElement = getMapper().get(element);
				final Grid grid = (Grid) PlayerWrapUtils.unwrapWidget(uiElement);
				final ColumnViewer viewer = TeslaSWTAccess.getThis(ColumnViewer.class,
						grid, SWT.MouseDown);

				getPlayer().exec("Cancel editing in Nebula Grid", new Runnable() {
					@Override
					public void run() {
						viewer.cancelEditing();
					}
				});

				return factory.createBooleanResponse();
			}

			@Override
			protected Response handleApplyCellEditor(final ApplyCellEditor command) {
				Element element = command.getElement();
				if (!element.getKind().equals(NebulaElementKinds.GRID))
					return super.handleApplyCellEditor(command);

				getPlayer().exec("Apply editing in Nebula Grid", new Runnable() {
					@Override
					public void run() {
						CellEditor editor = TeslaCellEditorManager.getInstance()
								.getLastActivatedByAnyMethod();
						if (editor != null) {
							TeslaCellEditorManager.getInstance().forceRemove(editor);
							TeslaSWTAccess.callMethodValueChanged(editor,
									"valueChanged", editor.isValueValid(), true);
							TeslaSWTAccess.callMethod(editor, "fireApplyEditorValue");
							if (editor instanceof ComboBoxViewerCellEditor) {
								TeslaSWTAccess.callMethod(
										ComboBoxViewerCellEditor.class, editor,
										"applyEditorValueAndDeactivate");
							}
							editor.deactivate();
						}
					}
				});

				return factory.createBooleanResponse();
			}
		};
	}

	private Response handleClickColumn(ClickColumn command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final Widget widget = PlayerWrapUtils.unwrapWidget(element);
		if (!(widget instanceof Grid)) {
			return failResponse("Control is not a Nebula Grid");
		}
		final String name = command.getName();
		final int index = command.getIndex();

		Grid grid = (Grid) widget;
		GridColumn[] columns = grid.getColumns();
		Widget column = null;

		if (name == null || name.isEmpty()) {
			if (index < columns.length)
				column = columns[index];
		} else {
			int matches = 0;
			for (int i = 0; i < columns.length; ++i) {
				if (name.equals(columns[i].getText())) {
					if (index == matches) {
						column = columns[i];
						break;
					} else
						++matches;
				}
			}
		}

		if (column == null) {
			return failResponse(String.format(
					"Cannot find column with name (%s) and index(%d)", name,
					index));
		}
		Event event = new Event();
		event.type = SWT.Selection;
		getPlayer().getEvents().sendEvent(column, event);

		return okResponse();
	}

	@Override
	protected Response handleCheckItem(CheckItem command) {
		final SWTUIElement element = getMapper().get(command.getElement());
		final BooleanResponse response = factory.createBooleanResponse();
		if (element != null) {
			final EList<String> path = command.getPath();
			String aPath[] = null;
			if (!path.isEmpty()) {
				aPath = path.toArray(new String[path.size()]);
			}
			final boolean checkItem = NebulaViewers.checkItem(command.isState(),
					(NebulaUIElement) element, aPath, command.getPattern(), command.getIndex());
			response.setResult(checkItem);

			if (checkItem)
				return response;
		}
		return super.handleCheckItem(command);
	}

	private static final String[] allSelectors = {
			NebulaElementKinds.GRID,
			NebulaElementKinds.ROW_HEADER,
			NebulaElementKinds.ITEM_CELL,
			NebulaElementKinds.EMPTY_AREA,
			ElementKind.Item.name(),
			ElementKind.ColumnHeader.name() };

	@Override
	public boolean isSelectorSupported(String kind) {
		for (final String kindName : allSelectors) {
			if (kindName.equals(kind)) {
				return true;
			}
		}
		return false;
	}

	private SWTUIProcessor getSWTProcessor() {
		return client.getProcessor(SWTUIProcessor.class);
	}

	@Override
	public SWTElementMapper getMapper() {
		return getSWTProcessor().getMapper();
	}

	@Override
	public void initialize(AbstractTeslaClient client, String id) {
		super.initialize(client, id);
		this.client = client;
	}

	@Override
	public org.eclipse.rcptt.tesla.core.ui.Widget mapExtraValues(SWTUIElement element,
			org.eclipse.rcptt.tesla.core.ui.Widget result) {
		return mapWidget(element, result);
	}

	public static org.eclipse.rcptt.tesla.core.ui.Widget mapWidget(
			SWTUIElement element, org.eclipse.rcptt.tesla.core.ui.Widget result) {
		Widget widget = element.unwrap();
		if (widget instanceof Grid) {
			Grid grid = (Grid) widget;

			NebulaGrid nebulaGrid = NebulaFactory.eINSTANCE.createNebulaGrid();
			SWTModelMapper.fillControl(nebulaGrid, grid);
			nebulaGrid.setLinesVisible(grid.getLinesVisible());
			nebulaGrid.setMultiSelection((grid.getStyle() & SWT.MULTI) != 0);
			nebulaGrid.setHeaderVisible(grid.getHeaderVisible());
			nebulaGrid.setItemCount(NebulaViewers.getGridRootItemsCount(grid));

			GridItem[] items = grid.getSelection();
			EList<Selection> selection = nebulaGrid.getSelection();
			for (int i = 0; i < items.length; ++i) {
				String[] path = NebulaViewers.getPathByGridItem(items[i]);
				Selection sel = UiFactory.eINSTANCE.createSelection();
				sel.getPath().addAll(Arrays.asList(path));
				selection.add(sel);
			}

			GridColumn[] columns = grid.getColumns();
			EList<NebulaGridColumn> nebulaColumns = nebulaGrid.getColumns();
			for (int i = 0; i < columns.length; ++i) {
				NebulaGridColumn col = NebulaFactory.eINSTANCE
						.createNebulaGridColumn();
				col.setIndex(i);
				col.setCaption(columns[i].getText());
				col.setEnablement(true);
				col.setMovable(columns[i].getMoveable());
				col.setResizable(columns[i].getResizeable());
				col.setSelection(false);
				col.setTooltip(columns[i].getHeaderTooltip());
				col.setWidth(columns[i].getWidth());
				nebulaColumns.add(col);
			}

			return nebulaGrid;
		} else if (widget instanceof GridItem) {
			GridItem item = (GridItem) widget;
			Grid grid = item.getParent();

			NebulaGridItem nebulaItem = NebulaFactory.eINSTANCE
					.createNebulaGridItem();
			nebulaItem.setCaption(SWTModelMapper.unify(item.getText()));
			nebulaItem.setSelection(Arrays.asList(grid.getSelection())
					.contains(item));
			nebulaItem.setEnablement(true);
			nebulaItem.setIndex(NebulaViewers.getGridItemIndex(item));
			nebulaItem.setChecked(item.getChecked());
			nebulaItem.setBackgroundColor(SWTModelMapper.makeColor(item
					.getBackground()));
			nebulaItem.setForegroundColor(SWTModelMapper.makeColor(item
					.getForeground()));
			nebulaItem.setBounds(SWTModelMapper.makeBounds(NebulaDescriber
					.getItemBounds(item)));

			nebulaItem.setChildCount(
					(item.isExpanded() || item.getItemCount() == 0) ? item.getItemCount() : null);

			EList<String> columns = nebulaItem.getColumns();
			EList<Color> bgs = nebulaItem.getColumnsBackgroundColor();
			EList<Color> fgs = nebulaItem.getColumnsForegroundColor();
			for (int i = 0; i < grid.getColumnCount(); ++i) {
				columns.add(SWTModelMapper.unify(item.getText(i)));

				org.eclipse.swt.graphics.Color bg = item.getBackground(i);
				bgs.add(SWTModelMapper.makeColor(bg == null ? grid
						.getBackground() : bg));

				fgs.add(SWTModelMapper.makeColor(item.getForeground(i)));
			}

			return nebulaItem;
		}
		return result;
	}
}
