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
package org.eclipse.rcptt.tesla.internal.ui.player;

import static org.eclipse.rcptt.util.swt.TableTreeUtil.getColumn;
import static org.eclipse.rcptt.util.swt.TableTreeUtil.getColumnCount;
import static org.eclipse.rcptt.util.swt.TableTreeUtil.getParent;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.rcptt.tesla.core.ui.Button;
import org.eclipse.rcptt.tesla.core.ui.ButtonKind;
import org.eclipse.rcptt.tesla.core.ui.Cell;
import org.eclipse.rcptt.tesla.core.ui.Combo;
import org.eclipse.rcptt.tesla.core.ui.Control;
import org.eclipse.rcptt.tesla.core.ui.ControlDecorator;
import org.eclipse.rcptt.tesla.core.ui.DateTime;
import org.eclipse.rcptt.tesla.core.ui.Editor;
import org.eclipse.rcptt.tesla.core.ui.Group;
import org.eclipse.rcptt.tesla.core.ui.Label;
import org.eclipse.rcptt.tesla.core.ui.PropertyNodeList;
import org.eclipse.rcptt.tesla.core.ui.Rectangle;
import org.eclipse.rcptt.tesla.core.ui.Selection;
import org.eclipse.rcptt.tesla.core.ui.Slider;
import org.eclipse.rcptt.tesla.core.ui.StyleRangeEntry;
import org.eclipse.rcptt.tesla.core.ui.TabFolder;
import org.eclipse.rcptt.tesla.core.ui.Table;
import org.eclipse.rcptt.tesla.core.ui.TableItem;
import org.eclipse.rcptt.tesla.core.ui.Text;
import org.eclipse.rcptt.tesla.core.ui.TextPosition;
import org.eclipse.rcptt.tesla.core.ui.ToolItem;
import org.eclipse.rcptt.tesla.core.ui.Tree;
import org.eclipse.rcptt.tesla.core.ui.TreeItem;
import org.eclipse.rcptt.tesla.core.ui.UiFactory;
import org.eclipse.rcptt.tesla.core.ui.View;
import org.eclipse.rcptt.tesla.core.ui.ViewerColumn;
import org.eclipse.rcptt.tesla.core.ui.Widget;
import org.eclipse.rcptt.tesla.core.ui.Window;
import org.eclipse.rcptt.tesla.core.ui.WithImage;
import org.eclipse.rcptt.tesla.internal.ui.player.viewers.Viewers;
import org.eclipse.rcptt.tesla.jface.ControlDecoratorRecordingHolder;
import org.eclipse.rcptt.tesla.jface.ImageSources;
import org.eclipse.rcptt.tesla.jface.ImageSources.CompositeSource;
import org.eclipse.rcptt.tesla.jface.ImageSources.ImageSource;
import org.eclipse.rcptt.tesla.jface.ImageSources.ResourceSource;
import org.eclipse.rcptt.tesla.swt.reflection.JavaMembersHelper;
import org.eclipse.rcptt.util.JSoupUtil;
import org.eclipse.rcptt.util.swt.TableTreeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPartReference;

public class SWTModelMapper {

	private static UiFactory factory = UiFactory.eINSTANCE;
	private static List<ISWTModelMapperExtension> extensions = null;

	public static Widget map(SWTUIElement element) {
		Widget result = internalMap(element);
		if (extensions != null) {
			for (ISWTModelMapperExtension ext : extensions) {
				result = ext.mapExtraValues(element, result);
			}
		}
		return result;
	}

	public static PropertyNodeList getPropertyNodes(SWTUIElement element,
			String nodePath) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		PropertyNodeList propertyNodeList = UiFactory.eINSTANCE
				.createPropertyNodeList();
		if (JavaMembersHelper.fillProperties(getMappedSource(element),
				nodePath, propertyNodeList.getPropertyNodes())) {
			return propertyNodeList;
		}
		return null;
	}

	public static String getPropertyValue(SWTUIElement element, String nodePath)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		return JavaMembersHelper.getPropertyValue(getMappedSource(element),
				nodePath);
	}

	public static Object getRawPropertyValue(SWTUIElement element, String nodePath)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		return JavaMembersHelper.getRawPropertyValue(getMappedSource(element),
				nodePath);
	}

	private static Object getMappedSource(SWTUIElement element) {
		if (element instanceof WorkbenchUIElement
				&& ((WorkbenchUIElement) element).reference != null) {
			return ((WorkbenchUIElement) element).reference;
		}
		return element.unwrap();
	}

	private static Widget internalMap(SWTUIElement element) {
		org.eclipse.swt.widgets.Widget widget = element.widget;
		switch (element.getKind().kind) {
		case Browser:
			return fillBrowser((Browser) widget);
		case Link:
			if (widget instanceof Link) {
				return fillLink((Link) widget);
			}
			break;
		case Button: {
			if (widget instanceof org.eclipse.swt.widgets.Button) {
				return fillButton(widget);
			} else if (widget instanceof org.eclipse.swt.widgets.ToolItem) {
				return fillToolItem(widget);
			}
			break;
		}
		case Item:
			if (widget instanceof org.eclipse.swt.widgets.TreeItem) {
				return fillTreeItem((org.eclipse.swt.widgets.TreeItem) widget);
			}
			if (widget instanceof org.eclipse.swt.widgets.TableItem) {
				return fillTableItem((org.eclipse.swt.widgets.TableItem) widget);
			}
			break;
		case Tree:
			return fillTree(widget, element.getPlayer());
		case Table:
			return fillTable(widget, element.getPlayer());
		case Label:
			if (widget instanceof org.eclipse.swt.widgets.Label) {
				return fillLabel(factory.createLabel(),
						(org.eclipse.swt.widgets.Label) widget);
			}
			if (widget instanceof CLabel) {
				Label label = factory.createLabel();
				label.setCaption(unify(((CLabel) widget).getText()));
				fillControl(label, (org.eclipse.swt.widgets.Control) widget);
				return label;
			}
			break;
		case Text:
		case TextViewer:
			return fillText(element, widget);
		case Slider:
			return fillSlider(element, widget);
		case DateTime:
			return fillDateTime(element, widget);
		case Group:
			return fillGroup(element, widget);
		case Combo:
			if (widget instanceof org.eclipse.swt.widgets.Combo) {
				return fillCombo(element,
						(org.eclipse.swt.widgets.Combo) widget);
			}
			if (widget instanceof CCombo) {
				return fillCCombo(element, (CCombo) widget);
			}
			break;
		case TabFolder:
			if (widget instanceof org.eclipse.swt.widgets.TabFolder) {
				return fillTabFolder((org.eclipse.swt.widgets.TabFolder) widget);
			} else if (widget instanceof CTabFolder) {
				return fillCTabFolder((CTabFolder) widget);
			}
		case Menu:
			if (widget instanceof MenuItem) {
				return fillMenuItem((MenuItem) widget);
			}
		case Editor:
			return fillEditor(element);
		case View:
			return fillView(element);
		case Window:
			return fillWindow(element);
		default:
			break;
		}

		if (widget instanceof org.eclipse.swt.widgets.Control) {
			Control ctrl = UiFactory.eINSTANCE.createControl();
			fillControl(ctrl, (org.eclipse.swt.widgets.Control) widget);
			return ctrl;
		}
		return UiFactory.eINSTANCE.createWidget();
	}

	private static Widget fillWindow(SWTUIElement element) {
		Shell shell = (Shell) element.unwrap();
		Window window = UiFactory.eINSTANCE.createWindow();
		fillControl(window, shell);
		window.setTitle(unify(shell.getText()));
		window.setSizeable((shell.getStyle() & SWT.RESIZE) != 0);
		window.setIsTool((shell.getStyle() & SWT.TOOL) != 0);
		window.setIsSheet((shell.getStyle() & SWT.SHEET) != 0);
		window.setIsPrimaryModal((shell.getStyle() & SWT.PRIMARY_MODAL) != 0);
		window.setIsApplicationModal((shell.getStyle() & SWT.APPLICATION_MODAL) != 0);
		window.setIsSystemModal((shell.getStyle() & SWT.SYSTEM_MODAL) != 0);
		window.setHasCloseButton((shell.getStyle() & SWT.CLOSE) != 0);
		window.setHasMinimizeButton((shell.getStyle() & SWT.MIN) != 0);
		window.setHasMaximizeButton((shell.getStyle() & SWT.MAX) != 0);
		window.setHasBorder((shell.getStyle() & SWT.BORDER) != 0);

		return window;
	}

	private static Widget fillView(SWTUIElement element) {
		View view = UiFactory.eINSTANCE.createView();
		if (element instanceof WorkbenchUIElement) {
			IWorkbenchPartReference reference = ((WorkbenchUIElement) element)
					.getReference();
			view.setTitle(unify(reference.getPartName()));
			fillControl(view,
					((org.eclipse.swt.widgets.Control) element.unwrap()));
		}
		return view;
	}

	private static Widget fillEditor(SWTUIElement element) {
		Editor editor = UiFactory.eINSTANCE.createEditor();
		if (element instanceof WorkbenchUIElement) {
			IWorkbenchPartReference reference = ((WorkbenchUIElement) element)
					.getReference();
			editor.setTitle(unify(reference.getPartName()));
			editor.setDirty(reference.isDirty());
			editor.setActive(reference.getPage().getActivePartReference() == reference);
			fillControl(editor,
					((org.eclipse.swt.widgets.Control) element.unwrap()));

		}
		return editor;
	}

	private static Widget fillMenuItem(MenuItem widget) {
		org.eclipse.rcptt.tesla.core.ui.MenuItem item = UiFactory.eINSTANCE
				.createMenuItem();
		String text = "" + (widget.getText());
		String text2 = "";
		if (text.contains("\t")) {
			int tabIndex = text.indexOf('\t');
			text2 = text.substring(tabIndex + 1);
			text = text.substring(0, tabIndex);
		}
		item.setCaption(unify(text));
		if (text2.trim().length() > 0) {
			item.setAccelerator(unify(text2));
		}
		item.setEnablement(widget.getEnabled());
		item.setSelection(widget.getSelection());
		item.setCascade((widget.getStyle() & SWT.CASCADE) != 0);
		Menu parent = widget.getParent();
		if (parent != null) {
			MenuItem[] items = parent.getItems();
			for (int i = 0; i < items.length; i++) {
				if (widget.equals(items[i])) {
					item.setIndex(i);
				}
			}
		}
		return item;
	}

	private static Widget fillBrowser(org.eclipse.swt.browser.Browser widget) {
		org.eclipse.rcptt.tesla.core.ui.Browser ctrl = UiFactory.eINSTANCE
				.createBrowser();
		fillControl(ctrl, widget);
		ctrl.setUri(widget.getUrl());
		String text = widget.getText();
		text = text.replace("\r\n", "\\n");
		text = text.replace("\n", "\\n");
		text = text.replace("\r", "\n");
		ctrl.setText(text);
		ctrl.setPlainText(JSoupUtil.parse(text));
		return ctrl;
	}

	private static Widget fillLink(Link widget) {
		org.eclipse.rcptt.tesla.core.ui.Link label = UiFactory.eINSTANCE.createLink();
		label.setCaption(unify(widget.getText()));
		label.setToltip(unify(widget.getToolTipText()));
		fillControl(label, widget);
		return label;
	}

	private static Widget fillCTabFolder(CTabFolder widget) {
		TabFolder folder = UiFactory.eINSTANCE.createTabFolder();
		CTabItem[] items = widget.getItems();
		folder.setTabCount(items.length);
		for (CTabItem cTabItem : items) {
			folder.getPages().add(cTabItem.getText());
		}
		CTabItem sel = widget.getSelection();
		if (sel != null) {
			folder.setActivePage(unify(sel.getText()));
		}
		return folder;
	}

	private static Widget fillTabFolder(org.eclipse.swt.widgets.TabFolder widget) {
		TabFolder folder = UiFactory.eINSTANCE.createTabFolder();
		TabItem[] items = widget.getItems();
		folder.setTabCount(items.length);
		for (TabItem cTabItem : items) {
			folder.getPages().add(cTabItem.getText());
		}
		int index = widget.getSelectionIndex();
		if (index != -1) {
			folder.setActivePage(unify(widget.getItem(index).getText()));
		}
		return folder;
	}

	private static Widget fillCombo(SWTUIElement element,
			org.eclipse.swt.widgets.Combo widget) {
		Combo text = factory.createCombo();
		String[] items = widget.getItems();
		int selIndex = widget.getSelectionIndex();
		if (selIndex != -1) {
			text.setSelection(unify(items[selIndex]));
		} else {
			text.setSelection(unify(widget.getText()));
		}
		text.setReadOnly((widget.getStyle() & SWT.READ_ONLY) != 0);
		for (String string : items) {
			text.getValues().add(unify(string));
		}
		fillControl(text, widget);
		return text;
	}

	private static Widget fillCCombo(SWTUIElement element, CCombo widget) {
		Combo text = factory.createCombo();
		String[] items = widget.getItems();
		int selIndex = widget.getSelectionIndex();
		if (selIndex != -1) {
			text.setSelection(unify(items[selIndex]));
		} else {
			text.setSelection(unify(widget.getText()));
		}
		text.setReadOnly((widget.getStyle() & SWT.READ_ONLY) != 0);
		for (String string : items) {
			text.getValues().add(unify(string));
		}
		fillControl(text, widget);
		return text;
	}

	public static String unify(String value) {
		return PlayerTextUtils.unifyMultilines(value);
	}

	private static Widget fillText(SWTUIElement element,
			org.eclipse.swt.widgets.Widget widget) {
		Text text = factory.createText();
		text.setValue(PlayerTextUtils.replaceNonUnicode(element.getText()));
		text.setText(PlayerTextUtils.unifyMultilines(element.getModificationText()));
		text.setRawValue(PlayerTextUtils.escape(element.getModificationText()));
		text.setReadOnly((widget.getStyle() & SWT.READ_ONLY) != 0);

		if (widget instanceof StyledText) {
			StyledText st = (StyledText) widget;
			int caretOffset = st.getCaretOffset();
			text.setCaretPosition(offsetToPosition(st, caretOffset));
			StyleRange[] sr = st.getStyleRanges();
			for (StyleRange styleRange : sr) {
				text.getStyleRanges().add(makeStyleRange(styleRange));
				String currText = PlayerTextUtils.unifyMultilines(st.getTextRange(styleRange.start,
						styleRange.length));
				text.getStyles().add(
						addStartEnd(st, makeTextStyle(styleRange, currText)));
			}

			setStyleAtOffset(text, st, caretOffset, sr);
		}

		fillControl(text, (org.eclipse.swt.widgets.Control) widget);
		return text;
	}

	public static TextPosition offsetToPosition(StyledText st, int offset) {
		int caretLine = st.getLineAtOffset(offset);
		int lineOffset = st.getOffsetAtLine(caretLine);
		int tabWidth = st.getTabs();

		int caretColumn = 0;

		if (offset != lineOffset) {
			char[] chars = st.getText(lineOffset, offset - 1).toCharArray();
			for (int i = 0; i < chars.length; i++) {
				caretColumn += chars[i] == '\t' ? tabWidth
						- (tabWidth == 0 ? 0 : caretColumn % tabWidth) : 1;
			}
		}

		TextPosition post = UiFactory.eINSTANCE.createTextPosition();
		post.setLine(caretLine + 1);
		post.setColumn(caretColumn + 1);
		return post;
	}

	private static void setStyleAtOffset(Text text, StyledText st,
			int caretOffset, StyleRange[] sr) {
		if (sr.length == 0) {
			return;
		}
		if (st.getCharCount() == 0) {
			return;
		}
		if (caretOffset < 0) {
			caretOffset = 0;
		}
		if (caretOffset >= st.getCharCount()) {
			caretOffset = st.getCharCount() - 1;
		}

		StyleRange styleRangeAtCaret = findRangeAtOffset(sr, caretOffset);
		if (styleRangeAtCaret == null) {
			try {
				styleRangeAtCaret = st.getStyleRangeAtOffset(caretOffset);
			} catch (IllegalArgumentException e) {
				return;
			}
		}
		if (styleRangeAtCaret != null) {
			String styleTextAtCaret = st.getTextRange(styleRangeAtCaret.start,
					styleRangeAtCaret.length);
			text.setStyleAtCaret(addStartEnd(st,
					makeTextStyle(styleRangeAtCaret, styleTextAtCaret)));
		}
	}

	/**
	 * We cannot use {@link StyledText#getStyleRangeAtOffset(int)} because it
	 * returns a new style range with a length 1, but we want to get an actual
	 * style range so that we know the text with the same style surrounding a
	 * cursor i.e. if I put cursor in the middle of word 'static', I should see
	 * in assertion properties that the whole word 'static' belongs to
	 * styleAtCaret
	 */
	private static StyleRange findRangeAtOffset(StyleRange[] ranges, int offset) {
		for (int i = 0; i < ranges.length; i++) {
			StyleRange range = ranges[i];
			int start = range.start;
			int end = start + range.length;
			if (start <= offset && offset < end) {
				return range;
			}
		}
		return null;
	}

	private static String makeStyleRange(StyleRange styleRange) {
		String val = styleRange.toString();
		String srt = "StyleRange ";
		if (val.startsWith(srt)) {
			val = val.substring(srt.length() + 1);
		}
		if (val.endsWith("}")) {
			val = val.substring(0, val.length() - 1);
		}
		return val;
	}

	/**
	 * Adds start/end posititon to {@link StyleRangeEntry}.
	 * 
	 * @param st
	 * @param entry
	 * @return entry argument
	 */
	private static StyleRangeEntry addStartEnd(StyledText st,
			StyleRangeEntry entry) {
		int start = entry.getStart();
		int end = entry.getLength() + start;
		entry.setStartPos(offsetToPosition(st, start));
		entry.setEndPos(offsetToPosition(st, end));
		return entry;
	}

	public static StyleRangeEntry makeStyleRangeEntry(StyleRange range) {
		return makeTextStyle(range, null);
	}

	public static StyleRangeEntry makeStyleRangeEntry(StyleRange range, int startOverride, int lengthOverride) {
		StyleRangeEntry result = makeTextStyle(range, null);
		result.setStart(startOverride);
		result.setLength(lengthOverride);
		return result;
	}

	private static StyleRangeEntry makeTextStyle(StyleRange styleRange,
			String text) {
		StyleRangeEntry style = factory.createStyleRangeEntry();
		style.setStart(styleRange.start);
		style.setLength(styleRange.length);
		if (text != null) {
			style.setText(text);
		}
		switch (styleRange.fontStyle) {
		case SWT.BOLD:
			style.setFontStyle("bold"); //$NON-NLS-1$
			break;
		case SWT.ITALIC:
			style.setFontStyle("italic"); //$NON-NLS-1$
			break;
		case SWT.BOLD | SWT.ITALIC:
			style.setFontStyle("bold-italic"); //$NON-NLS-1$
			break;
		default:
			style.setFontStyle("normal"); //$NON-NLS-1$
		}
		if (styleRange.font != null) {
			style.setFont(styleRange.font.toString());
		}
		if (styleRange.foreground != null) {
			style.setForegroundColor(makeColor(styleRange.foreground));
		}
		if (styleRange.background != null) {
			style.setBackgroundColor(makeColor(styleRange.background));
		}
		if (styleRange.underline) {
			switch (styleRange.underlineStyle) {
			case SWT.UNDERLINE_SINGLE:
				style.setUnderline("single");break; //$NON-NLS-1$
			case SWT.UNDERLINE_DOUBLE:
				style.setUnderline("double");break; //$NON-NLS-1$
			case SWT.UNDERLINE_SQUIGGLE:
				style.setUnderline("squiggle");break; //$NON-NLS-1$
			case SWT.UNDERLINE_ERROR:
				style.setUnderline("error");break; //$NON-NLS-1$
			case SWT.UNDERLINE_LINK:
				style.setUnderline("link");break; //$NON-NLS-1$
			}
			if (styleRange.underlineColor != null) {
				style.setUnderlineColor(makeColor(styleRange.underlineColor));
			}
		}
		if (styleRange.strikeout) {
			style.setStrikeout(true);
			if (styleRange.strikeoutColor != null) {
				style.setStrikeoutColor(makeColor(styleRange.strikeoutColor));
			}
		}
		if (styleRange.borderStyle != SWT.NONE) {
			switch (styleRange.borderStyle) {
			case SWT.BORDER_SOLID:
				style.setBorderStyle("solid");break; //$NON-NLS-1$
			case SWT.BORDER_DOT:
				style.setBorderStyle("dot");break; //$NON-NLS-1$
			case SWT.BORDER_DASH:
				style.setBorderStyle("dash");break; //$NON-NLS-1$
			}
			if (styleRange.borderColor != null) {
				style.setBorderColor(makeColor(styleRange.borderColor));
			}
		}
		if (styleRange.rise != 0) {
			style.setRise(styleRange.rise);
		}
		if (styleRange.metrics != null) {
			style.setMetrics(styleRange.metrics.toString());
		}
		return style;
	}

	private static Widget fillSlider(SWTUIElement element,
			org.eclipse.swt.widgets.Widget widget) {
		Slider slider = factory.createSlider();
		org.eclipse.swt.widgets.Slider sliderWidget = (org.eclipse.swt.widgets.Slider) widget;
		slider.setValue(sliderWidget.getSelection());
		fillControl(slider, (org.eclipse.swt.widgets.Control) widget);
		return slider;
	}

	private static Widget fillDateTime(SWTUIElement element,
			org.eclipse.swt.widgets.Widget widget) {
		DateTime text = factory.createDateTime();
		org.eclipse.swt.widgets.DateTime dt = (org.eclipse.swt.widgets.DateTime) widget;
		text.setDate(unify(PlayerTextUtils.getDateValue(dt)));
		text.setTime(unify(PlayerTextUtils.getTimeValue(dt)));
		text.setReadOnly((widget.getStyle() & SWT.READ_ONLY) != 0);
		fillControl(text, (org.eclipse.swt.widgets.Control) widget);
		return text;
	}

	private static Widget fillGroup(SWTUIElement element,
			org.eclipse.swt.widgets.Widget widget) {
		Group text = factory.createGroup();
		if (widget instanceof Composite) {
			Composite comp = (Composite) widget;
			text.setCaption(unify(PlayerTextUtils.replaceNonUnicode(element
					.getText())));
			text.setChildCount(comp.getChildren().length);
		}
		fillControl(text, (org.eclipse.swt.widgets.Control) widget);
		return text;
	}

	private static Widget fillLabel(Label label,
			org.eclipse.swt.widgets.Label widget) {
		label.setCaption(unify(widget.getText()));
		fillImage(label, widget.getImage());
		fillControl(label, widget);
		return label;
	}

	private static Tree fillTree(org.eclipse.swt.widgets.Widget widget,
			SWTUIPlayer player) {
		Tree tree = factory.createTree();
		org.eclipse.swt.widgets.Tree treeWidget = (org.eclipse.swt.widgets.Tree) widget;
		fillControl(tree, treeWidget);
		tree.setHeaderVisible(treeWidget.getHeaderVisible());
		tree.setLinesVisible(treeWidget.getLinesVisible());
		tree.setMultiSelection((treeWidget.getStyle() & SWT.MULTI) != 0);
		int count = treeWidget.getColumnCount();
		for (int i = 0; i < count; i++) {
			TreeColumn column = treeWidget.getColumn(i);
			ViewerColumn viewerColumn = factory.createViewerColumn();
			viewerColumn.setCaption(unify(column.getText()));
			viewerColumn.setTooltip(unify(column.getToolTipText()));
			viewerColumn.setMovable(column.getMoveable());
			viewerColumn.setResizable(column.getResizable());
			viewerColumn.setWidth(column.getWidth());
			viewerColumn.setIndex(i);
			tree.getColumns().add(viewerColumn);
		}
		String[][] selection = Viewers.getMultiPathSelection(player
				.wrap(treeWidget));
		if (selection != null) {
			for (int i = 0; i < selection.length; i++) {
				Selection sel = factory.createSelection();
				sel.getPath().addAll(Arrays.asList(selection[i]));
				tree.getSelection().add(sel);
			}
		}
		tree.setItemCount(treeWidget.getItemCount());
		// tree.getSelection().add(e);
		return tree;
	}

	private static Table fillTable(org.eclipse.swt.widgets.Widget widget,
			SWTUIPlayer player) {
		Table table = factory.createTable();
		org.eclipse.swt.widgets.Table tableWidget = (org.eclipse.swt.widgets.Table) widget;
		fillControl(table, tableWidget);
		table.setHeaderVisible(tableWidget.getHeaderVisible());
		table.setLinesVisible(tableWidget.getLinesVisible());
		table.setMultiSelection((tableWidget.getStyle() & SWT.MULTI) != 0);
		int count = tableWidget.getColumnCount();
		for (int i = 0; i < count; i++) {
			TableColumn column = tableWidget.getColumn(i);
			ViewerColumn viewerColumn = factory.createViewerColumn();
			viewerColumn.setCaption(unify(column.getText()));
			viewerColumn.setTooltip(unify(column.getToolTipText()));
			viewerColumn.setMovable(column.getMoveable());
			viewerColumn.setResizable(column.getResizable());
			viewerColumn.setWidth(column.getWidth());
			viewerColumn.setIndex(i);
			table.getColumns().add(viewerColumn);
		}
		String[][] selection = Viewers.getMultiPathSelection(player
				.wrap(tableWidget));
		if (selection != null) {
			for (int i = 0; i < selection.length; i++) {
				Selection sel = factory.createSelection();
				sel.getPath().addAll(Arrays.asList(selection[i]));
				table.getSelection().add(sel);
			}
		}
		table.setItemCount(tableWidget.getItemCount());
		return table;
	}

	private static TableItem fillTableItem(org.eclipse.swt.widgets.TableItem wItem) {
		TableItem tItem = factory.createTableItem();
		fillImage(tItem, wItem.getImage());
		fillCells(wItem, tItem);
		tItem.setCaption(unify(wItem.getText()));
		tItem.setBackgroundColor(makeColor(wItem.getBackground()));
		tItem.setForegroundColor(makeColor(wItem.getForeground()));
		tItem.setChecked(wItem.getChecked());
		org.eclipse.swt.widgets.TableItem[] selR = wItem.getParent()
				.getSelection();
		boolean selected = false;
		for (org.eclipse.swt.widgets.TableItem tableItem : selR) {
			if (tableItem.equals(wItem)) {
				selected = true;
				break;
			}
		}
		int columnCount = wItem.getParent().getColumnCount();
		org.eclipse.swt.widgets.Widget[] columns = TableTreeUtil.getColumns(wItem.getParent());
		if (columnCount > 0) {
			for (int i = 0; i < columnCount; i++) {
				String text = wItem.getText(i);
				if (text.equals("")) {
					Object value = getColumnValue(
							wItem, i);
					if (value != null) {
						text = value.toString();
					}
				}
				tItem.getValues().put(getTextOrToolTip((TableColumn) columns[i]), text);
				tItem.getColumns().add(text);
				tItem.getColumnsBackgroundColor().add(
						makeColor(wItem.getBackground(i)));
				tItem.getColumnsForegroundColor().add(
						makeColor(wItem.getForeground(i)));
			}
		}

		tItem.setSelected(selected);
		tItem.setBounds(makeBounds(wItem.getBounds()));
		org.eclipse.swt.widgets.TableItem[] items = wItem.getParent()
				.getItems();
		for (int i = 0; i < items.length; i++) {
			if (wItem.equals(items[i])) {
				tItem.setIndex(i);
			}
		}
		String keyPattern = Policy.JFACE + "styled_label_key_";

		int count = wItem.getParent().getColumnCount();
		if (count == 0) {
			count = 1;
		}
		for (int i = 0; i < count; i++) {
			Object data = wItem.getData(keyPattern + Integer.toString(i));
			if (data instanceof StyleRange[]) {
				// styles
				for (StyleRange styleRange : (StyleRange[]) data) {
					tItem.getStyleRanges().add(makeStyleRange(styleRange));
					String currText = null;
					if (styleRange.start >= 0
							&& styleRange.length > 0
							&& styleRange.start + styleRange.length < wItem
									.getText(i).length()) {
						currText = wItem.getText(i).substring(styleRange.start,
								styleRange.start + styleRange.length);
					}
					tItem.getStyles().add(makeTextStyle(styleRange, currText));
				}
			} else {
				// Construct style using font
				Font font = wItem.getFont(i);
				if (count == 1 && font == null) {
					font = wItem.getFont();
				}
				if (font != null) {
					tItem.getStyles().add(
							constructStyleFromFont(font, wItem.getText(i)));
				}
			}
		}
		return tItem;
	}

	private static void fillCells(org.eclipse.swt.widgets.Item swt, org.eclipse.rcptt.tesla.core.ui.Item model) {
		int columnCount = TableTreeUtil.getColumnCount(swt);
		for (int i = 0; i < columnCount; i++) {
			Cell cell = UiFactory.eINSTANCE.createCell();
			cell.setImage(mapImage(TableTreeUtil.getImage(swt, i)));
			model.getCells().add(cell);
		}
	}
	private static TreeItem fillTreeItem(org.eclipse.swt.widgets.TreeItem wItem) {
		TreeItem tItem = UiFactory.eINSTANCE.createTreeItem();
		fillImage(tItem, wItem.getImage());
		fillCells(wItem, tItem);
		tItem.setCaption(unify(wItem.getText()));
		tItem.setBackgroundColor(makeColor(wItem.getBackground()));
		tItem.setForegroundColor(makeColor(wItem.getForeground()));
		tItem.setChecked(wItem.getChecked());
		org.eclipse.swt.widgets.TreeItem[] selR = wItem.getParent()
				.getSelection();
		boolean selected = false;
		for (org.eclipse.swt.widgets.TreeItem tableItem : selR) {
			if (tableItem.equals(wItem)) {
				selected = true;
				break;
			}
		}

		int columnCount = getColumnCount(getParent(wItem));
		org.eclipse.swt.widgets.Widget[] columns = TableTreeUtil.getColumns(wItem.getParent());
		if (columnCount > 0) {
			for (int i = 0; i < columnCount; i++) {
				String text = wItem.getText(i);
				if (text.equals("")) {
					Object value = getColumnValue(
							wItem, i);
					if (value != null) {
						text = value.toString();
					}
				}
				tItem.getValues().put(getTextOrToolTip((TreeColumn) columns[i]), text);
				tItem.getColumns().add(text);
				tItem.getColumnsBackgroundColor().add(
						makeColor(wItem.getBackground(i)));
				tItem.getColumnsForegroundColor().add(
						makeColor(wItem.getForeground(i)));
			}
		}
		if (wItem.getExpanded() || wItem.getItemCount() == 0) {
			tItem.setChildCount(wItem.getItemCount());
		} else {
			tItem.setChildCount(null);
		}
		tItem.setSelected(selected);
		tItem.setBounds(makeBounds(wItem.getBounds()));

		org.eclipse.swt.widgets.TreeItem parentItem = wItem.getParentItem();
		org.eclipse.swt.widgets.TreeItem[] parentItems = parentItem != null ? parentItem
				.getItems() : wItem.getParent().getItems();
		for (int i = 0; i < parentItems.length; i++) {
			if (wItem.equals(parentItems[i])) {
				tItem.setIndex(i);
			}
		}

		String keyPattern = Policy.JFACE + "styled_label_key_";

		int count = wItem.getParent().getColumnCount();
		if (count == 0) {
			count = 1;
		}
		for (int i = 0; i < count; i++) {
			Object data = wItem.getData(keyPattern + Integer.toString(i));
			if (data instanceof StyleRange[]) {
				// styles
				for (StyleRange styleRange : (StyleRange[]) data) {
					tItem.getStyleRanges().add(makeStyleRange(styleRange));
					String currText = null;
					if (styleRange.start >= 0
							&& styleRange.length > 0
							&& styleRange.start + styleRange.length < wItem
									.getText(i).length()) {
						currText = wItem.getText(i).substring(styleRange.start,
								styleRange.start + styleRange.length);
					}
					tItem.getStyles().add(makeTextStyle(styleRange, currText));
				}
			} else {
				// Construct style using font
				Font font = wItem.getFont(i);
				if (count == 1 && font == null) {
					font = wItem.getFont();
				}
				if (font != null) {
					tItem.getStyles().add(
							constructStyleFromFont(font, wItem.getText(i)));
				}
			}
		}

		return tItem;
	}

	private static String getTextOrToolTip(TreeColumn column) {
		if (!column.getText().isEmpty()) {
			return column.getText();
		} else {
			return column.getToolTipText();
		}
	}

	private static String getTextOrToolTip(TableColumn column) {
		if (!column.getText().isEmpty()) {
			return column.getText();
		} else {
			return column.getToolTipText();
		}
	}

	private static Object getColumnValue(org.eclipse.swt.widgets.Widget widget,
			int index) {
		org.eclipse.swt.widgets.Widget column = getColumn(getParent(widget),
				index);
		Object columnViewer = column.getData(Policy.JFACE + ".columnViewer");
		EditingSupport es = TeslaSWTAccess.getField(EditingSupport.class,
				columnViewer, "editingSupport");
		Object value = null;
		if (es != null) {
			if ((Boolean)TeslaSWTAccess.callMethod(EditingSupport.class, es,
					"canEdit", new Class[] { Object.class }, ((Item) widget).getData())) {
				value = TeslaSWTAccess.callMethod(EditingSupport.class, es,
					"getValue", new Class[] { Object.class },
					((Item) widget).getData());
			}
		}
		return value;
	}

	private static StyleRangeEntry constructStyleFromFont(Font font,
			String caption) {
		StyleRangeEntry st = factory.createStyleRangeEntry();
		st.setStart(0);
		if (caption != null) {
			st.setLength(caption.length());
		}
		st.setText(caption);
		FontData[] fontDatas = font.getFontData();
		for (FontData fontData : fontDatas) {
			switch (fontData.getStyle()) {
			case SWT.BOLD:
				st.setFontStyle("bold"); //$NON-NLS-1$
				break;
			case SWT.ITALIC:
				st.setFontStyle("italic"); //$NON-NLS-1$
				break;
			case SWT.BOLD | SWT.ITALIC:
				st.setFontStyle("bold-italic"); //$NON-NLS-1$
				break;
			default:
				st.setFontStyle("normal"); //$NON-NLS-1$
			}
			st.setFont(fontData.getName());
		}
		return st;
	}

	private static Button fillToolItem(org.eclipse.swt.widgets.Widget widget) {
		org.eclipse.swt.widgets.ToolItem wToolItem = (org.eclipse.swt.widgets.ToolItem) widget;
		ToolItem toolItem = UiFactory.eINSTANCE.createToolItem();
		fillImage(toolItem, wToolItem.getImage());
		toolItem.setCaption(unify(wToolItem.getText()));
		toolItem.setTooltip(unify(wToolItem.getToolTipText()));
		toolItem.setSelected(wToolItem.getSelection());
		toolItem.setEnablement(wToolItem.isEnabled());
		toolItem.setKind(ButtonKind.TOOL);
		toolItem.setBackgroundColor(makeColor(wToolItem.getParent()
				.getBackground()));
		toolItem.setForegroundColor(makeColor(wToolItem.getParent()
				.getForeground()));
		org.eclipse.swt.widgets.ToolItem[] items = wToolItem.getParent()
				.getItems();
		for (int i = 0; i < items.length; i++) {
			if (wToolItem.equals(items[i])) {
				toolItem.setIndex(i);
			}
		}
		return toolItem;
	}

	private static Button fillButton(org.eclipse.swt.widgets.Widget widget) {
		org.eclipse.swt.widgets.Button b = (org.eclipse.swt.widgets.Button) widget;
		Button button = UiFactory.eINSTANCE.createButton();
		fillImage(button, b.getImage());
		button.setCaption(unify(b.getText()));
		button.setTooltip(unify(b.getToolTipText()));
		button.setGrayed(b.getGrayed());
		button.setSelected(b.getSelection());
		if ((b.getStyle() & SWT.RADIO) != 0) {
			button.setKind(ButtonKind.RADIO);
		}
		if ((b.getStyle() & SWT.PUSH) != 0) {
			button.setKind(ButtonKind.PUSH);
		}
		if ((b.getStyle() & SWT.CHECK) != 0) {
			button.setKind(ButtonKind.CHECK);
		}
		if ((b.getStyle() & SWT.ARROW) != 0) {
			button.setKind(ButtonKind.ARROW);
		}
		if ((b.getStyle() & SWT.TOGGLE) != 0) {
			button.setKind(ButtonKind.TOGGLE);
		}
		fillControl(button, (org.eclipse.swt.widgets.Control) widget);
		return button;
	}

	public static void fillControl(Control ctrl,
			org.eclipse.swt.widgets.Control widget) {
		if (widget == null) {
			return;// Probable disposed control
		}
		ctrl.setClassName(unify(widget.getClass().getName()));
		ctrl.setEnablement(widget.isEnabled());
		ctrl.setBounds(makeBounds(widget.getBounds()));
		ctrl.setContainMenu(widget.getMenu() != null);
		ctrl.setBorderWith(widget.getBorderWidth());
		ctrl.setBackgroundColor(makeColor(widget.getBackground()));
		ctrl.setForegroundColor(makeColor(widget.getForeground()));
		try {
			List<ControlDecoration> decorators = ControlDecoratorRecordingHolder
					.getDecorators(widget);
			for (ControlDecoration decor : decorators) {
				ControlDecorator decorator = UiFactory.eINSTANCE
						.createControlDecorator();
				decorator.setVisible(decor.isVisible());
				decorator.setDescription(decor.getDescriptionText());
				ctrl.getDecorators().add(decorator);
			}
		} catch (Throwable e) {

		}
	}

	public static Rectangle makeBounds(org.eclipse.swt.graphics.Rectangle bounds) {
		Rectangle myBounds = UiFactory.eINSTANCE.createRectangle();
		myBounds.setX(bounds.x);
		myBounds.setY(bounds.y);
		myBounds.setWidth(bounds.width);
		myBounds.setHeight(bounds.height);
		return myBounds;
	}

	public static org.eclipse.rcptt.tesla.core.ui.Color makeColor(
			org.eclipse.swt.graphics.Color swtColor) {
		return makeColor(swtColor, false);
	}

	public static org.eclipse.rcptt.tesla.core.ui.Color makeColor(
			org.eclipse.swt.graphics.Color swtColor, boolean allowNull) {
		if (swtColor == null && allowNull)
			return null;

		org.eclipse.rcptt.tesla.core.ui.Color color = UiFactory.eINSTANCE.createColor();
		if (swtColor != null) {
			color.setRed(swtColor.getRed());
			color.setGreen(swtColor.getGreen());
			color.setBlue(swtColor.getBlue());
		}
		return color;
	}

	public static void initializeExtensions(List<ISWTModelMapperExtension> exts) {
		extensions = exts;
	}

	public static void fillImage(WithImage widget, Image image) {
		if( image == null) {
			return;
		}
		org.eclipse.rcptt.tesla.core.ui.Image img = mapImage(image);
		if (img == null) {
			return;
		}
		widget.setImage(img);
	}

	public static org.eclipse.rcptt.tesla.core.ui.Image mapImage(Image image) {
		ImageSource source = ImageSources.INSTANCE.find(image);
		if (source == null) {
			return null;
		}
		return mapSource(source);
	}

	private static org.eclipse.rcptt.tesla.core.ui.Image mapSource(ImageSource source) {
		if (source == null) {
			return null;
		}
		if (source instanceof ResourceSource) {
			org.eclipse.rcptt.tesla.core.ui.Image result = UiFactory.eINSTANCE
					.createImage();
			result.setPath(((ResourceSource) source).source);
			return result;
		}

		if (source instanceof CompositeSource) {
			CompositeSource composite = (CompositeSource) source;
			if (composite.children.isEmpty()) {
				return null;
			}
			org.eclipse.rcptt.tesla.core.ui.Image result = mapSource(composite.children
					.get(0));
			if (result != null) {
				for (int i = 1; i < composite.children.size(); i++) {
					org.eclipse.rcptt.tesla.core.ui.Image decoration = mapSource(composite.children
							.get(i));
					if (decoration != null) {
						result.getDecorations().add(decoration);
					}
				}
			}
			return result;
		}

		throw new IllegalArgumentException("Unknown image source");
	}
}
