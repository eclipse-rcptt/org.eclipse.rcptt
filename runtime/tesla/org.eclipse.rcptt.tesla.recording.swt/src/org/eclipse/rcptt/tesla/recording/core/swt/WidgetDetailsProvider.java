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
package org.eclipse.rcptt.tesla.recording.core.swt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rcptt.tesla.internal.ui.player.DetailUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

@SuppressWarnings("rawtypes")
public class WidgetDetailsProvider {
	private class Style {
		int value;
		String caption;

		public Style(int value, String caption) {
			super();
			this.value = value;
			this.caption = caption;
		}

		public String getCaption() {
			return caption;
		}

		public int getValue() {
			return value;
		}
	}

	// These two event types introduced only in 3.7:
	private static final int SWTTouch = 47; // SWT.Touch;
	private static final int SWTGesture = 48; // SWT.Gesture;

	private Map<Integer, String> eventTypes = new HashMap<Integer, String>();
	private Map<Class, List<Style>> widgetStyles = new HashMap<Class, List<WidgetDetailsProvider.Style>>();

	public WidgetDetailsProvider() {
		eventTypes.put(SWT.KeyDown, "KeyDown");
		eventTypes.put(SWT.KeyUp, "KeyUp");
		eventTypes.put(SWT.MouseDown, "MouseDown");
		eventTypes.put(SWT.MouseUp, "MouseUp");
		eventTypes.put(SWT.MouseMove, "MouseMove");
		eventTypes.put(SWT.MouseEnter, "MouseEnter");
		eventTypes.put(SWT.MouseExit, "MouseExit");
		eventTypes.put(SWT.MouseDoubleClick, "MouseDoubleClick");
		eventTypes.put(SWT.Paint, "Paint");
		eventTypes.put(SWT.Move, "Move");
		eventTypes.put(SWT.Resize, "Resize");
		eventTypes.put(SWT.Dispose, "Dispose");
		eventTypes.put(SWT.Selection, "Selection");
		eventTypes.put(SWT.DefaultSelection, "DefaultSelection");
		eventTypes.put(SWT.FocusIn, "FocusIn");
		eventTypes.put(SWT.FocusOut, "FocusOut");
		eventTypes.put(SWT.Expand, "Expand");
		eventTypes.put(SWT.Collapse, "Collapse");
		eventTypes.put(SWT.Iconify, "Iconify");
		eventTypes.put(SWT.Deiconify, "Deiconify");
		eventTypes.put(SWT.Close, "Close");
		eventTypes.put(SWT.Show, "Show");
		eventTypes.put(SWT.Hide, "Hide");
		eventTypes.put(SWT.Modify, "Modify");
		eventTypes.put(SWT.Verify, "Verify");
		eventTypes.put(SWT.Activate, "Activate");
		eventTypes.put(SWT.Deactivate, "Deactivate");
		eventTypes.put(SWT.Help, "Help");
		eventTypes.put(SWT.DragDetect, "DragDetect");
		eventTypes.put(SWT.Arm, "Arm");
		eventTypes.put(SWT.Traverse, "Traverse");
		eventTypes.put(SWT.MouseHover, "MouseHover");
		eventTypes.put(SWT.HardKeyDown, "HardKeyDown");
		eventTypes.put(SWT.HardKeyUp, "HardKeyUp");
		eventTypes.put(SWT.MenuDetect, "MenuDetect");
		eventTypes.put(SWT.SetData, "SetData");
		eventTypes.put(SWT.MouseVerticalWheel, "MouseVerticalWheel");
		eventTypes.put(SWT.MouseHorizontalWheel, "MouseHorizontalWheel");
		eventTypes.put(SWT.MouseWheel, "MouseWheel");
		eventTypes.put(SWT.Settings, "Settings");
		eventTypes.put(SWT.EraseItem, "EraseItem");
		eventTypes.put(SWT.MeasureItem, "MeasureItem");
		eventTypes.put(SWT.PaintItem, "PaintItem");
		eventTypes.put(SWT.ImeComposition, "ImeComposition");
		eventTypes.put(SWT.OrientationChange, "OrientationChange");
		eventTypes.put(SWT.Skin, "Skin");
		eventTypes.put(SWT.OpenDocument, "OpenDocument");
		eventTypes.put(SWTTouch, "Touch");
		eventTypes.put(SWTGesture, "Gesture");

		widgetStyles.put(
				Decorations.class,
				styles(//
				style(SWT.NO_TRIM, "NoTrim"),//
						style(SWT.CLOSE, "Close"),//
						style(SWT.TITLE, "Title"),//
						style(SWT.MIN, "Min"),//
						style(SWT.NO_TRIM, "NoTrim"),//
						style(SWT.ON_TOP, "OnTop"),//
						style(SWT.TOOL, "Tool"),//
						style(SWT.SHELL_TRIM, "ShellTrim"),//
						style(SWT.MAX, "Max"),//
						style(SWT.RESIZE, "Resize"),//
						style(SWT.MENU, "Menu"), //
						style(SWT.CLOSE, "Close"),//
						style(SWT.TRANSPARENT, "Transparent"),//
						style(SWT.SYSTEM_MODAL, "SystemModal"),//
						style(SWT.APPLICATION_MODAL, "ApplicationModal"),
						style(SWT.PRIMARY_MODAL, "PrimaryModal")));
		widgetStyles.put(
				Shell.class,
				styles(//
				style(SWT.TRANSPARENT, "Transparent"),//
						style(SWT.SYSTEM_MODAL, "SystemModal"),//
						style(SWT.APPLICATION_MODAL, "ApplicationModal"),
						style(SWT.PRIMARY_MODAL, "PrimaryModal")));
		widgetStyles.put(Button.class, styles(//
				style(SWT.PUSH, "Push"),//
				style(SWT.ARROW, "Arrow"),//
				style(SWT.CHECK, "Check"),//
				style(SWT.RADIO, "Radio"),//
				style(SWT.TOGGLE, "Toggle"),//
				style(SWT.CENTER, "Center"),//
				style(SWT.LEFT, "Left"),//
				style(SWT.RIGHT, "Right"),//
				style(SWT.UP, "Up"),//
				style(SWT.DOWN, "Down")//
				));
		widgetStyles.put(Label.class, styles(//
				style(SWT.SEPARATOR, "Separator"),//
				style(SWT.HORIZONTAL, "Horizontal"),//
				style(SWT.VERTICAL, "Vertical"),//
				style(SWT.SHADOW_IN, "ShadowIn"),//
				style(SWT.SHADOW_OUT, "ShadowOut"),//
				style(SWT.SHADOW_NONE, "ShadowNone"),//
				style(SWT.LEFT, "Left"),//
				style(SWT.RIGHT, "Right"),//
				style(SWT.CENTER, "Centre"),//
				style(SWT.WRAP, "Wrap")//
				));
		widgetStyles.put(CLabel.class, styles(//
				style(SWT.SHADOW_IN, "ShadowIn"),//
				style(SWT.SHADOW_OUT, "ShadowOut"),//
				style(SWT.SHADOW_NONE, "ShadowNone"),//
				style(SWT.LEFT, "Left"),//
				style(SWT.RIGHT, "Right"),//
				style(SWT.CENTER, "Centre")//
				));
		widgetStyles.put(Scrollable.class, styles(//
				style(SWT.H_SCROLL, "HScroll"),//
				style(SWT.V_SCROLL, "VScroll")//
				));
		widgetStyles.put(Composite.class, styles(//
				style(SWT.NO_BACKGROUND, "NoBackground"),//
				style(SWT.NO_FOCUS, "NoFocus"),//
				style(SWT.NO_MERGE_PAINTS, "NoMergePaints"),//
				style(SWT.NO_REDRAW_RESIZE, "NoRedrawResize"),//
				style(SWT.NO_RADIO_GROUP, "NoRadioGroup"),//
				style(SWT.EMBEDDED, "Embedded"),//
				style(SWT.DOUBLE_BUFFERED, "DoubleBuffered")//
				));
		widgetStyles.put(Control.class, styles(//
				style(SWT.BORDER, "Border"),//
				// style(SWT.LEFT_TO_RIGHT, "LeftToRight"),//
				style(SWT.RIGHT_TO_LEFT, "RightToLeft")//
				));
		widgetStyles.put(Table.class, styles(//
				style(SWT.SINGLE, "Single"),//
				style(SWT.MULTI, "Multi"),//
				style(SWT.CHECK, "Check"),//
				style(SWT.FULL_SELECTION, "FullSelection"),//
				style(SWT.HIDE_SELECTION, "HideSelection"),//
				style(SWT.VIRTUAL, "Virtual"),//
				style(SWT.NO_SCROLL, "NoScroll")//
				));
		widgetStyles.put(Tree.class, styles(//
				style(SWT.SINGLE, "Single"),//
				style(SWT.MULTI, "Multi"),//
				style(SWT.CHECK, "Check"),//
				style(SWT.FULL_SELECTION, "FullSelection"),//
				style(SWT.VIRTUAL, "Virtual"),//
				style(SWT.NO_SCROLL, "NoScroll")//
				));
		widgetStyles.put(Combo.class, styles(//
				style(SWT.DROP_DOWN, "DropDown"),//
				style(SWT.READ_ONLY, "ReadOnly"),//
				style(SWT.SIMPLE, "Simple")//
				));
		widgetStyles.put(CCombo.class, styles(//
				style(SWT.DROP_DOWN, "DropDown"),//
				style(SWT.READ_ONLY, "ReadOnly"),//
				style(SWT.FLAT, "Flat"),//
				style(SWT.SIMPLE, "Simple")//
				));
		widgetStyles.put(TabFolder.class, styles(//
				style(SWT.TOP, "Top"),//
				style(SWT.BOTTOM, "Bottom")//
				));
		widgetStyles.put(TabFolder.class, styles(//
				style(SWT.TOP, "Top"),//
				style(SWT.CLOSE, "Close"),//
				style(SWT.FLAT, "Flat"),//
				style(SWT.SINGLE, "Single"),//
				style(SWT.MULTI, "Multi"),//
				style(SWT.BOTTOM, "Bottom")//
				));
		widgetStyles.put(Group.class, styles(//
				style(SWT.SHADOW_ETCHED_IN, "ShadowEtchIn"),//
				style(SWT.SHADOW_ETCHED_OUT, "ShadowEtchOut"),//
				style(SWT.SHADOW_IN, "ShadowIn"),//
				style(SWT.SHADOW_OUT, "ShadowOut"),//
				style(SWT.SHADOW_NONE, "ShadowNone")//
				));
		widgetStyles.put(MenuItem.class, styles(//
				style(SWT.CHECK, "Check"),//
				style(SWT.CASCADE, "Cascade"),//
				style(SWT.PUSH, "Push"),//
				style(SWT.RADIO, "Radio"),//
				style(SWT.SEPARATOR, "Separator")//
				));
	}

	private static List<Style> styles(Style... style) {
		return Arrays.asList(style);
	}

	private Style style(int value, String name) {
		return new Style(value, name);
	}

	public String getDetails(Widget widget, SWTUIPlayer player) {
		StringBuilder result = new StringBuilder();
		result.append("Widget information\n\n");
		processWidget(widget, result, player);

		if (widget instanceof TreeItem) {
			result.append("Parent tree:\n");
			processWidget(((TreeItem) widget).getParent(), result, player);
		}
		if (widget instanceof TableItem) {
			result.append("Parent table:\n");
			processWidget(((TableItem) widget).getParent(), result, player);
		}

		return result.toString();
	}

	private void processWidget(Widget widget, StringBuilder result,
			SWTUIPlayer player) {
		result.append("Class: ");
		result.append(widget.getClass().getName()).append("\n");
		result.append("\t\t\t").append(DetailUtils.extractSupers(widget.getClass()))
				.append("\n\n");

		result.append("WidgetID: ")
				.append(SWTRecordingHelper.getHelper().getLocator()
						.getWidgetID(widget)).append("\n");

		int style = widget.getStyle();
		result.append("Style flags: ").append(style)
				.append(tryExtractFlags(widget, style)).append("\n");
		if (widget instanceof Control) {
			Control ctrl = (Control) widget;
			result.append("Enabled: ").append(ctrl.isEnabled()).append("\n");
			result.append("Focused: ").append(ctrl.isFocusControl())
					.append("\n");
			result.append("Visible: ").append(ctrl.isVisible()).append("\n");
			result.append("Reparentable: ").append(ctrl.isReparentable())
					.append("\n");
		}
		if (widget instanceof ToolItem) {
			ToolItem ctrl = (ToolItem) widget;
			result.append("Enabled: ").append(ctrl.isEnabled()).append("\n");
		}
		if (widget instanceof TreeItem) {
			TreeItem ctrl = (TreeItem) widget;
			result.append("Grayed: ").append(ctrl.getGrayed()).append("\n");
			result.append("Checked: ").append(ctrl.getChecked()).append("\n");
			result.append("Expanded: ").append(ctrl.getExpanded()).append("\n");
		}
		if (widget instanceof TableItem) {
			TableItem ctrl = (TableItem) widget;
			result.append("Grayed: ").append(ctrl.getGrayed()).append("\n");
			result.append("Checked: ").append(ctrl.getChecked()).append("\n");
		}

		String textValue = PlayerTextUtils.getText(player.wrap(widget));
		if (textValue != null) {
			result.append("Text value: ").append(textValue).append("\n");
		}
		result.append("\n");

		// Show registered listener classes
		Map<Object, Set<String>> listenerToTypes = new HashMap<Object, Set<String>>();

		for (Integer type : eventTypes.keySet()) {
			Listener[] listeners = widget.getListeners(type.intValue());
			if (listeners != null && listeners.length > 0) {
				for (Listener listener : listeners) {
					Object key = TeslaSWTAccess.tryUnwrapEventListener(listener);
					Set<String> list = listenerToTypes.get(key);
					if (list == null) {
						list = new HashSet<String>();
						listenerToTypes.put(key, list);
					}
					list.add("SWT." + eventTypes.get(type));
				}
			}
		}
		// Print out listeners
		if (listenerToTypes.size() > 0) {
			result.append("Registered event listeners:\n");
			for (Map.Entry<Object, Set<String>> e : listenerToTypes.entrySet()) {
				Class<? extends Object> cl = e.getKey().getClass();
				result.append("\t\tlistener class: ").append(cl.getName());
				result.append("\n\t\t\t").append(DetailUtils.extractSupers(cl));
				result.append("\n\t\t\tevents: ");
				// .append(Arrays.toString(e.getValue().toArray()))
				for (String type : e.getValue()) {
					result.append("\n\t\t\t\t").append(type);
				}
				result.append("\n");

			}
		}

		if (widget instanceof Control) {
			Viewer viewer = TeslaSWTAccess.getViewer((Control) widget);
			if (viewer != null) {
				result.append("\nWidget has associated viewer: ")
						.append(viewer.getClass().getName()).append("\n\t\t\t")
						.append(DetailUtils.extractSupers(viewer.getClass())).append("\n");
				if (viewer instanceof ContentViewer) {
					IBaseLabelProvider labelProvider = ((ContentViewer) viewer)
							.getLabelProvider();
					IContentProvider contentProvider = ((ContentViewer) viewer)
							.getContentProvider();
					if (labelProvider != null) {
						result.append("\tViewer Label Provider:")
								.append("\n\t\t")
								.append(labelProvider.getClass().getName())
								.append("\n\t\t\t")
								.append(DetailUtils.extractSupers(labelProvider.getClass()))
								.append("\n");
					}
					if (contentProvider != null) {
						result.append("\tViewer Content Provider:")
								.append("\n\t\t")
								.append(contentProvider.getClass().getName())
								.append("\n\t\t\t")
								.append(DetailUtils.extractSupers(contentProvider
										.getClass())).append("\n");
					}
				}

			}
		}

		// if (widget instanceof Composite) {
		// Control[] children = ((Composite) widget).getChildren();
		// if (children != null) {
		// for (Control child : children) {
		//
		// }
		// }
		// }

	}

	@SuppressWarnings("unchecked")
	private Object tryExtractFlags(Widget widget, int style) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Class, List<Style>> s : widgetStyles.entrySet()) {
			if (s.getKey().isAssignableFrom(widget.getClass())) {
				List<Style> value = s.getValue();
				for (Style ss : value) {
					if ((style & ss.getValue()) != 0) {
						if (builder.length() > 0) {
							builder.append("|");
						}
						builder.append(ss.getCaption());
					}
				}
			}
		}
		if (builder.length() > 0) {
			builder.insert(0, " (");
			builder.append(")");
		}
		return builder.toString();
	}
}
