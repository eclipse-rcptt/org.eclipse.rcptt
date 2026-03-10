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

import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrap;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrapWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.rcptt.tesla.core.ui.StyleRangeEntry;
import org.eclipse.rcptt.tesla.core.ui.UiPackage;
import org.eclipse.rcptt.tesla.core.ui.impl.UiPackageImpl;
import org.eclipse.rcptt.tesla.swt.TeslaSWTMessages;
import org.eclipse.rcptt.tesla.ui.SWTTeslaActivator;
import org.eclipse.rcptt.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPartReference;

public class PlayerTextUtils {
	// for static utility methods only

	public static String getText(SWTUIElement uiElement) {
		String result = getRawText(uiElement);
		if (result != null) {
			String finalResult;
			Widget widget = unwrapWidget(uiElement);
			if (widget instanceof Text
					&& (((Text) widget).getStyle() & SWT.MULTI) != 0) {
				finalResult = replaceMultilines(result);
			} else {
				finalResult = result.replaceAll("\n|\r", "").trim();
			}
			finalResult = removeAcceleratorFromText(finalResult);
			return finalResult;
		}
		return result;
	}

	public static String getTrimmedText(SWTUIElement uiElement) {
		String result = getRawText(uiElement);
		if (result != null) {
			result = result.replaceAll("\n|\r", "").trim();
		}
		return result;
	}

	public static String getTextForVerification(SWTUIElement uiElement) {
		String result = getRawText(uiElement);
		if (result == null)
			return "";
		return replaceMultilinesNoTrim(result);
	}

	public static String replaceMultilines(String result) {
		if (result == null) {
			return null;
		}
		return replaceMultilinesNoTrim(result).trim();
	}

	public static String unifyMultilines(String result) {
		if (result == null) {
			return null;
		}
		// replace window-specific "\r\n" to common "\n"
		return result.replace("\r\n", "\n").trim();
	}

	public static String replaceMultilinesNoTrim(String result) {
		if (result == null) {
			return null;
		}
		if (Platform.getOS().equals(Platform.OS_WIN32) || Platform.getOS().equals(Platform.OS_MACOSX) ) {
			return result.replaceAll("\r\n", "\n");
		}
		return result;
	}

	public static String getRawText(SWTUIElement uiElement) {
		Object rawElement = unwrap(uiElement);
		String result = null;
		if (rawElement instanceof IWorkbenchPartReference) {
			return ((IWorkbenchPartReference) rawElement).getPartName();
		}

		Widget widget = unwrapWidget(uiElement);
		if (widget == null || widget.isDisposed()) {
			return TeslaSWTMessages.SWTUIPlayer_DisposedControl_RawText;
		}
		if (widget instanceof Decorations) {
			result = ((Decorations) widget).getText();
		}
		if (widget instanceof Button) {
			result = ((Button) widget).getText();
			if (StringUtils.isEmpty(result))
				result = ((Button) widget).getToolTipText();
		}
		if (widget instanceof ToolItem) {
			result = ((ToolItem) widget).getToolTipText();
			if (StringUtils.isEmpty(result))
				result = ((ToolItem) widget).getText();
		}
		if (widget instanceof CoolItem) {
			result = ((CoolItem) widget).getText();
		}
		if (widget instanceof Label) {
			result = ((Label) widget).getText();
		}
		if (widget instanceof CLabel) {
			result = ((CLabel) widget).getText();
		}
		if (widget instanceof Text) {
			result = ((Text) widget).getText();
		}
		if (widget instanceof StyledText) {
			result = ((StyledText) widget).getText();
		}
		if (widget instanceof Combo) {
			result = ((Combo) widget).getText();
		}
		if (widget instanceof CCombo) {
			result = ((CCombo) widget).getText();
		}
		if (widget instanceof Group) {
			result = ((Group) widget).getText();
		}
		if (widget instanceof Link) {
			result = ((Link) widget).getText();
		}
		if (widget instanceof MenuItem) {
			MenuItem menuitem = (MenuItem) widget;
			result = getMenuText(menuitem.getText());
		}

		if (widget instanceof CTabItem) {
			result = ((CTabItem) widget).getText();
		}

		if (widget instanceof TabItem) {
			result = ((TabItem) widget).getText();
		}
		if (widget instanceof TreeItem) {
			result = ((TreeItem) widget).getText();
		}
		if (widget instanceof TableItem) {
			result = ((TableItem) widget).getText();
		}
		if (widget instanceof Shell) {
			result = ((Shell) widget).getText();
		}
		if (widget instanceof Spinner) {
			result = Double.toString(((Spinner) widget).getSelection()
					/ Math.pow(10, ((Spinner) widget).getDigits()));
		}
		if (result == null) {
			for (ISWTUIPlayerExtension ext : SWTUIPlayer.getExtensions()) {
				String text = ext.getRawText(uiElement);
				if (text != null) {
					result = text;
					break;
				}
			}
		}
		return result;
	}

	public static String replaceNonUnicode(String finalResult) {
		if (finalResult == null) {
			return null;
		}
		StringBuilder textResult = new StringBuilder();
		for (char c : finalResult.toCharArray()) {
			if (Character.isISOControl(c) || !Character.isDefined(c)) {
				textResult.append(' ');
			} else {
				textResult.append(c);
			}
		}
		return textResult.toString();
	}

	public static String escape(String finalResult) {
		if (finalResult == null) {
			return null;
		}
		StringBuilder textResult = new StringBuilder();
		for (char c : finalResult.toCharArray()) {
			if (c == '\n') {
				textResult.append("\\n");
			} else {
				textResult.append(c);
			}
		}
		return textResult.toString();
	}

	public static String removeAcceleratorFromText(String text) {
		if (text == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			if (c == '\\') {
				builder.append(c);
			}
			if (c != '&') {
				builder.append(c);
			}
		}
		text = builder.toString().trim();
		return text;
	}

	public static String getMenuText(String elementName) {
		elementName = removeAcceleratorFromText(elementName);
		int tabIndex = elementName.indexOf("\t");
		if (tabIndex != -1) {
			elementName = elementName.substring(0, tabIndex);
		}
		return elementName;
	}

	public static String validateRegex(String pattern) {
		try {
			Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			return e.getMessage();
		}
		return null;
	}

	private static Map<String, Pattern> matchersCache = new HashMap<>();
	private static final int MATCHERS_CACHE_SIZE = 500;
	private static final boolean DEBUG = false;

	public static boolean safeMatches(String item, String pattern) {
		if (pattern == null) {
			return false;
		}
		if (item == null) {
			return false;
		}
		try {
			if (matchersCache.size() > MATCHERS_CACHE_SIZE) {
				matchersCache.clear();
			}
			Pattern compile = null;
			if (matchersCache.containsKey(pattern)) {
				compile = matchersCache.get(pattern);
			}
			if (compile == null) {
				compile = Pattern.compile(pattern);
				matchersCache.put(pattern, compile);
			}
			Matcher matcher = compile.matcher(item);
			return matcher.matches();
		} catch (Throwable e) {
			if (DEBUG) {
				SWTTeslaActivator.log(e);
			}
		}

		return false;
	}

	//

	public static String getDateTimeValue(DateTime dt) {
		return getDateValue(dt) + " " + getTimeValue(dt);
	}

	public static String getTimeValue(DateTime dt) {
		return "" + dt.getHours() + ":" + dt.getMinutes() + ":"
				+ dt.getSeconds();
	}

	public static String getDateValue(DateTime dt) {
		return "" + dt.getYear() + "/" + (dt.getMonth() + 1) + "/"
				+ dt.getDay();
	}

	private static final class RangeEquality extends EqualityHelper {
		private static final long serialVersionUID = 290959129375407943L;
		
		private static final EStructuralFeature[] ignoredFields;
		static {
			UiPackage pkg = UiPackageImpl.init();
			ignoredFields = new EStructuralFeature[] {
				pkg.getStyleRangeEntry_Start(),
				pkg.getStyleRangeEntry_Length(),
				pkg.getStyleRangeEntry_StartPos(),
				pkg.getStyleRangeEntry_EndPos()
			};
		}
		
		@Override
		protected boolean haveEqualFeature(EObject eObject1, EObject eObject2, EStructuralFeature featureArg) {
			for (EStructuralFeature feature: ignoredFields) {
				if (equals(feature, featureArg))
					return true;
			}
			return super.haveEqualFeature(eObject1, eObject2, featureArg);
		}		
	}
	
	public static void squashRanges(List<StyleRangeEntry> ranges) {
		Iterator<StyleRangeEntry> i = ranges.iterator();
		StyleRangeEntry next = null;
		while (i.hasNext()) {
			StyleRangeEntry prev = next;
			next = i.next();
			if (prev == null)
				continue;
			if (next.getStart() == prev.getStart() + prev.getLength()) {
				RangeEquality rangeEquality = new RangeEquality();
				if (rangeEquality.equals(prev, next)) {
					prev.setLength(prev.getLength() + next.getLength());
					prev.setEndPos(next.getEndPos());
					i.remove();
				}
			}
		}

	}
	
	public static List<StyleRangeEntry> captureStyleRanges(StyledText widget) {
		List<StyleRangeEntry> result = new ArrayList<>();
		StyledText styledText = widget;
		int totalDelta = 0;
		for (StyleRange r : styledText.getStyleRanges()) {
			int delta = countLineEndingConversions(styledText.getTextRange(r.start, r.length));
			StyleRangeEntry rangeEntry = SWTModelMapper.makeStyleRangeEntry(
					r, r.start - totalDelta, r.length - delta);
			rangeEntry.setStartPos(SWTModelMapper.offsetToPosition(widget, r.start));

			result.add(rangeEntry);
			totalDelta += delta;
		}
		return result;
	}

	private static final boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);

	private static int countLineEndingConversions(String text) {
		if (!isWindows)
			return 0;
		return countOccurrencesOfString(text, "\r\n");
	}

	private static int countOccurrencesOfString(String text, String substring) {
		int index = 0;
		int count = 0;

		while (true) {
			index = text.indexOf(substring, index);
			if (index == -1)
				return count;

			count++;
			index += substring.length();
		}
	}

}
