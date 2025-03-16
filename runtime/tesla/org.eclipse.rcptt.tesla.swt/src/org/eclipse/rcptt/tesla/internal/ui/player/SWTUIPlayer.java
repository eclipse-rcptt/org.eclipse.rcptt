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

import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.getMenuText;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.getRawText;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.getText;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.removeAcceleratorFromText;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils.safeMatches;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWidgetUtils.canClick;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrap;
import static org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils.unwrapWidget;
import static org.eclipse.rcptt.util.swt.Bounds.centerAbs;
import static org.eclipse.rcptt.util.swt.Bounds.centerRel;
import static org.eclipse.rcptt.util.swt.Events.createClick;
import static org.eclipse.rcptt.util.swt.Events.createMouseDown;
import static org.eclipse.rcptt.util.swt.Events.createMouseUp;
import static org.eclipse.rcptt.util.swt.Events.createSelection;
import static org.eclipse.rcptt.util.swt.TableTreeUtil.getItemBounds;
import static org.eclipse.rcptt.util.swt.Widgets.isToggleButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rcptt.sherlock.core.SherlockTimerRunnable;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.tesla.core.context.ContextManagement;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.GenericElementKind;
import org.eclipse.rcptt.tesla.core.protocol.UIColor;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.core.TeslaExecutionFailedException;
import org.eclipse.rcptt.tesla.internal.ui.player.specific.GetWindowPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.viewers.TableViewerItem;
import org.eclipse.rcptt.tesla.internal.ui.player.viewers.Viewers;
import org.eclipse.rcptt.tesla.jface.TeslaCellEditorManager;
import org.eclipse.rcptt.tesla.swt.TeslaSWTMessages;
import org.eclipse.rcptt.tesla.swt.dialogs.SWTDialogManager;
import org.eclipse.rcptt.tesla.swt.events.ITimerExecHelper;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaTimerExecManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaTimerExecManager.TimerInfo;
import org.eclipse.rcptt.tesla.swt.workbench.EclipseWorkbenchProvider;
import org.eclipse.rcptt.tesla.ui.IViewerItem;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.rcptt.util.swt.Bounds;
import org.eclipse.rcptt.util.swt.Events;
import org.eclipse.rcptt.util.swt.TabCTabUtil;
import org.eclipse.rcptt.util.swt.TableTreeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Synchronizer;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.registry.EditorRegistry;

@SuppressWarnings("restriction")
public final class SWTUIPlayer {
	private static final boolean DEBUG_PROCEED = "true"
			.equals(Platform.getDebugOption("org.eclipse.rcptt.tesla.swt/debug/proceed"));
	final Display display;
	private SWTUIElement[] ignoreWindows;
	private Shell[] ignoredShells;
	private final List<File> screenshotsDuringSession = null;
	private final SWTEvents events;
	private volatile Throwable error = null;

	public Throwable getError() {
		return error;
	}

	private static SWTKeyboard keyboard = new SWTKeyboard();

	private final Map<Context, List<Runnable>> runnables = new HashMap<Context, List<Runnable>>();
	private static Map<Display, SWTUIPlayer> players = new HashMap<Display, SWTUIPlayer>();

	protected static Map<Class<?>, ElementKind> elementKinds = new LinkedHashMap<Class<?>, ElementKind>();
	private final ITimerExecHelper timerListener;
	private static List<ISWTUIPlayerExtension> extensions = new ArrayList<ISWTUIPlayerExtension>();
	static {
		elementKinds.put(Shell.class, ElementKind.Window);
		elementKinds.put(CBanner.class, ElementKind.CBanner);
		elementKinds.put(ToolBar.class, ElementKind.Toolbar);
		elementKinds.put(CoolBar.class, ElementKind.CoolBar);
		elementKinds.put(Button.class, ElementKind.Button);
		elementKinds.put(ToolItem.class, ElementKind.Button);
		elementKinds.put(Label.class, ElementKind.Label);
		elementKinds.put(CLabel.class, ElementKind.Label);
		elementKinds.put(Group.class, ElementKind.Group);
		elementKinds.put(TabFolder.class, ElementKind.TabFolder);
		elementKinds.put(CTabFolder.class, ElementKind.TabFolder);
		elementKinds.put(Text.class, ElementKind.Text);
		elementKinds.put(StyledText.class, ElementKind.Text);
		elementKinds.put(Spinner.class, ElementKind.Text);
		elementKinds.put(Link.class, ElementKind.Link);
		elementKinds.put(Combo.class, ElementKind.Combo);
		elementKinds.put(CCombo.class, ElementKind.Combo);
		elementKinds.put(Tree.class, ElementKind.Tree);
		elementKinds.put(org.eclipse.swt.widgets.List.class, ElementKind.List);
		elementKinds.put(MenuItem.class, ElementKind.Menu);
		elementKinds.put(Menu.class, ElementKind.Menu);
		elementKinds.put(Table.class, ElementKind.Table);
		elementKinds.put(CTabItem.class, ElementKind.TabItem);
		elementKinds.put(TabItem.class, ElementKind.TabItem);
		elementKinds.put(IViewReference.class, ElementKind.View);
		elementKinds.put(IEditorReference.class, ElementKind.Editor);
		elementKinds.put(DateTime.class, ElementKind.DateTime);
		elementKinds.put(Slider.class, ElementKind.Slider);
		elementKinds.put(Link.class, ElementKind.Link);
		elementKinds.put(Shell.class, ElementKind.Window);
		elementKinds.put(TreeItem.class, ElementKind.Item);
		elementKinds.put(TableItem.class, ElementKind.Item);
		elementKinds.put(Canvas.class, ElementKind.Canvas);
		elementKinds.put(Browser.class, ElementKind.Browser);
		elementKinds.put(TreeColumn.class, ElementKind.ColumnHeader);
		elementKinds.put(TableColumn.class, ElementKind.ColumnHeader);

		// Debug check right order of classes in elementKings Map
		// checkIntegrity(elementKinds.keySet().toArray(new Class<?>[0]));
	}

	public Shell[] getIgnored() {
		return ignoredShells;
	}

	protected SWTUIPlayer(Display display, Shell... ignoreWindows) {
		this.display = display;
		this.events = new SWTEvents(display);
		if (ignoreWindows != null) {
			this.ignoreWindows = new SWTUIElement[ignoreWindows.length];
			this.ignoredShells = ignoreWindows;
			int i = 0;
			for (Shell shell : ignoreWindows) {
				this.ignoreWindows[i++] = wrap(shell);
			}
		}
		collector = new UIJobCollector();
		Job.getJobManager().addJobChangeListener(collector);
		timerListener = getTimerExecHelper();
		TeslaTimerExecManager.getManager().addEventListener(timerListener);
	}

	private ITimerExecHelper getTimerExecHelper() {
		return new ITimerExecHelper() {
			@Override
			public boolean needNullify(Runnable run, int time) {
				String clName = null;
				if (run instanceof SherlockTimerRunnable) {
					clName = ((SherlockTimerRunnable) run).getRunnable().getClass().getName();
				} else {
					clName = run.getClass().getName();
				}
				if (!clName.contains("org.eclipse.swt") && !clName.contains("org.eclipse.gmf")
						&& !clName.contains("org.eclipse.jface") && !clName.contains("org.eclipse.gef")) {
					if (clName.contains(Display.class.getName())) {
						return false;
					}
					if (clName.startsWith("org.eclipse.nebula.widgets.oscilloscope.OscilloscopeDispatcher")) {
						return false;
					}
					if (clName.startsWith("org.eclipse.tm.internal.terminal.textcanvas.PollingTextCanvasModel")) {
						return false;
					}

					if (time < TeslaLimits.getTimerExecsWaitNullify()) {
						// Check if same thread is not already in stack trace.
						Context currentCtx = ContextManagement.makeContext(Thread.currentThread().getStackTrace());
						if (currentCtx.containsClass(clName)) {
							// Do not allow to nullity timers executed from
							// timers.
							return false;
						}
						// System.out.println("Nullifying timerexec:" + clName);
						return true;
					}
				}
				return false;
			}
		};
	}

	private SWTUIPlayer(Display display) {
		this(display, (Shell[]) null);
	}

	private static Class<?>[] one(Class<?> cl) {
		return new Class[] { cl };
	}

	/**
	 * Determines the UI element matching the filter.
	 * <p>
	 * Can be extended by
	 * {@link ISWTUIPlayerExtension#select(SWTUIPlayer, PlayerSelectionFilter)}
	 */
	public SWTUIElement select(PlayerSelectionFilter filter) {
		SWTUIElement result = null;

		for (ISWTUIPlayerExtension ext : getExtensions()) {
			result = ext.select(this, filter);
			if (result != null) {
				return result;
			}
		}

		switch (filter.kind.kind) {
		case Unknown:
			result = null;
			break;
		case EclipseWindow:
			result = selectEclipseWindow(filter.index);
			break;
		case QuickAccess:
			result = selectQuickAccess();
			break;
		case Window:
			result = new GetWindowPlayer(this, ignoreWindows).selectShell(filter);
			break;
		case Menu:
			result = selectMenu(filter);
			break;
		case Button:
			result = selectWidget(filter, Button.class, ToolItem.class);
			if (result == null) {
				result = selectButton(filter);
			}
			break;
		case Group:
			result = selectWidget(filter, Group.class);
			break;
		case Text:
			result = selectWidget(filter, Text.class, StyledText.class, Spinner.class);
			break;
		case Combo:
			result = selectWidget(filter, Combo.class, CCombo.class);
			break;
		case Tree:
			result = selectWidget(filter.withoutPattern(), false, Tree.class);
			break;
		case Toolbar:
			result = selectWidget(filter.withoutPattern(), false, ToolBar.class);
			break;
		case CBanner:
			result = selectWidget(filter.withoutPattern(), false, CBanner.class);
			break;
		case CoolBar:
			result = selectWidget(filter.withoutPattern(), false, CoolBar.class);
			break;
		case Canvas:
			result = selectWidget(filter.withoutPattern(), false, Canvas.class);
			break;
		case List:
			result = selectWidget(filter.withoutPattern(), false, org.eclipse.swt.widgets.List.class);
			break;
		case Table:
			result = selectWidget(filter.withoutPattern(), false, Table.class);
			break;
		case Label:
			result = selectWidget(filter, Label.class, CLabel.class);
			break;
		case TabItem:
			result = selectWidget(filter, CTabItem.class, TabItem.class);
			break;
		case TabFolder:
			result = selectWidget(filter, CTabFolder.class, TabFolder.class);
			break;
		case View:
			result = selectView(filter);
			break;
		case Editor:
			result = selectEditor(filter);
			break;
		case Any:
			result = selectWidget(filter.withoutKind());
			break;
		case Item:
			result = selectItem(filter);
			break;
		case Browser:
			result = selectWidget(filter.withoutPattern(), Browser.class);
			break;
		case DateTime:
			result = selectWidget(filter.withoutPattern(), DateTime.class);
			break;
		case Slider:
			result = selectWidget(filter.withoutPattern(), Slider.class);
			break;
		case ColumnHeader:
			result = selectColumnHeader(filter);
			break;
		}
		// if (result != null) {
		// if (isDisabled(result)) {
		// // System.out.println("#");
		// }
		// }
		if (result == null) {
			makeScreenShot();
		}
		return result;
	}

	private SWTUIElement selectColumnHeader(PlayerSelectionFilter f) {
		Widget unwrapped = unwrapWidget(f.parent);
		if (!(unwrapped instanceof Tree || unwrapped instanceof Table))
			return null;

		return wrap(TableTreeUtil.findColumn(unwrapped, f.pattern, f.index == null ? 0 : f.index));
	}

	private SWTUIElement selectButton(PlayerSelectionFilter f) {
		final String pattern = f.pattern;
		if (pattern != null && pattern.contains("(") && pattern.endsWith(")")) {
			// With accelerator
			int pos = pattern.indexOf('(');
			String prefix = pattern.substring(0, pos);
			String accel = pattern.substring(pos + 1, pattern.length() - 1);
			String[] split = accel.split("\\+");
			Arrays.sort(split);
			if (split.length > 2) {
				SWTUIElement[] children = this.children.collectFor(f.parent, ignoreWindows, true,
						new Class[] { Button.class, ToolItem.class }, f.after);
				// Ctrl + Alt + S like combinations
				int cur = 0;
				for (SWTUIElement widget : children) {
					String text = getText(widget);
					if (text != null) {
						text = removeAcceleratorFromText(text);
						if (text != null && text.contains("(") && text.endsWith(")")) {
							int pos2 = text.indexOf('(');
							String prefix2 = text.substring(0, pos2);
							String accel2 = text.substring(pos2 + 1, text.length() - 1);
							String[] split2 = accel2.split("\\+");
							Arrays.sort(split2);

							if (prefix2.equals(prefix) && Arrays.equals(split2, split)) {
								if ((f.index != null && cur == f.index.intValue()) || f.index == null) {
									return widget;
								}
								cur++;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	private SWTUIElement selectItem(PlayerSelectionFilter f) {
		final SWTUIElement parent = f.parent;
		if (parent != null && unwrapWidget(parent) instanceof Tree && f.path != null) {
			final Tree tree = (Tree) unwrapWidget(parent);
			TreeItem current = (TreeItem) Viewers.firstMatch(f.path, tree);
			if (current != null) {
				return wrap(current);
			}
		} else if (parent != null && unwrapWidget(parent) instanceof Tree && f.indexes != null
				&& f.indexes.length == 2) {
			// Select item with column
			final Tree tree = (Tree) unwrapWidget(parent);
			TreeItem[] items = getExpandedTreeItems(tree);
			if (items.length > f.indexes[1]) {
				return new ItemUIElement(items[f.indexes[1]], this, f.indexes[0]);
			}
		}
		if (parent != null && unwrapWidget(parent) instanceof Table && f.path != null && f.path.length > 0) {
			final Table table = (Table) unwrapWidget(parent);
			Object current = unwrapWidget(parent);
			TableItem[] items = ((Table) current).getItems();
			if (items == null) {
				return null;
			}

			IViewerItem[] viewerItems = Viewers.getViewerItems(items);
			for (String part : f.path) {
				for (TableItem item : items) {
					if (item.isDisposed()) {
						continue; // Skip disposed items
					}
					String itemText = toSelectionItem(
							Viewers.getTableItemText(new TableViewerItem(item), part, viewerItems));
					if (itemText != null) {
						if (itemText.equals(part) || safeMatches(itemText, part)) {
							return wrap(item);
						}
					}
				}
			}

		} else if (parent != null && unwrapWidget(parent) instanceof Table && f.indexes != null
				&& f.indexes.length == 2) {
			Object current = unwrapWidget(parent);
			TableItem[] items = ((Table) current).getItems();
			if (items.length > f.indexes[1]) {
				return new ItemUIElement(items[f.indexes[1]], this, f.indexes[0]);
			}
		}
		return null;
	}

	public static TreeItem[] getExpandedTreeItems(Tree tree) {
		List<TreeItem> items = new ArrayList<TreeItem>();
		for (int i = 0; i < tree.getItemCount(); i++) {
			TreeItem currentItem = tree.getItem(i);
			if (currentItem.isDisposed()) {
				continue;// Skip disposed items
			}
			items.add(currentItem);
			if (currentItem.getExpanded()) {
				items.addAll(getExpandedTreeItems(currentItem));
			}
		}
		return items.toArray(new TreeItem[items.size()]);
	}

	private static List<TreeItem> getExpandedTreeItems(TreeItem item) {
		List<TreeItem> items = new ArrayList<TreeItem>();
		for (int i = 0; i < item.getItemCount(); i++) {
			TreeItem currentItem = item.getItem(i);
			items.add(currentItem);
			if (currentItem.getExpanded()) {
				items.addAll(getExpandedTreeItems(currentItem));
			}
		}
		return items;
	}

	private SWTUIElement selectQuickAccess() {
		Text quickAccess = EclipseWorkbenchProvider.getProvider().getQuickAccess();
		return quickAccess == null ? null : wrap(quickAccess);
	}

	private SWTUIElement selectEclipseWindow(Integer index) {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		if (index == null) {
			return wrap(windows[0].getShell());
		} else if (index.intValue() < windows.length) {
			return wrap(windows[index.intValue()].getShell());
		}
		return null;
	}

	// private IScreenCapturer screenCapturer = null;
	private Context context;
	private final UIJobCollector collector;
	private final Map<Widget, Point> widgetToMouseForMenus = new HashMap<Widget, Point>();
	private static Set<WeakReference<Menu>> shownMenus = new HashSet<WeakReference<Menu>>();

	public void makeScreenShot() {
		// Disable making of screenshots

		// if (screenCapturer == null) {
		// screenCapturer = new AWTScreenCapturer(TeslaCore.getDefault()
		// .getStateLocation().append("screenshots").toFile());
		// }
		// final Rectangle bounds = display.getBounds();
		// screenshotsDuringSession.add(screenCapturer.makeScreenShot(bounds.x,
		// bounds.y, bounds.width, bounds.height));
	}

	public UIColor getSelectedColor(SWTUIElement uiElement) {
		Widget widget = unwrapWidget(uiElement);
		if (widget instanceof Button) {
			ColorSelector colorSelector = TeslaSWTAccess.getColorSelector((Button) widget);
			if (colorSelector != null) {
				return new UIColor(colorSelector.getColorValue().red, colorSelector.getColorValue().green,
						colorSelector.getColorValue().blue);// colorSelector.getColorValue();
			}
		}
		return null;

	}

	public void setSelectedColor(SWTUIElement uiElement, final UIColor color) {
		Widget widget = unwrapWidget(uiElement);
		if (widget instanceof Button) {
			ColorSelector colorSelector = TeslaSWTAccess.getColorSelector((Button) widget);
			if (colorSelector != null) {
				colorSelector.setColorValue(new RGB(color.r, color.g, color.b));
			}
		}
	}

	public SWTUIElement selectWidget(PlayerSelectionFilter filter, Class<?>... classes) {
		return selectWidget(filter, true, classes);
	}

	public SWTUIElement selectWidget(PlayerSelectionFilter f, boolean checkText, Class<?>... classes) {

		SWTUIElement[] children = this.children.collectFor(f.parent, ignoreWindows, true, classes, f.after);

		final GenericElementKind kind = f.kind;

		// filter the children by kind
		if (kind != null) {
			List<SWTUIElement> filteredByKind = new ArrayList<SWTUIElement>();
			for (SWTUIElement element : children) {
				if (element.isSuitableForKind(kind)) {
					filteredByKind.add(element);
				}
			}
			children = filteredByKind.toArray(new SWTUIElement[filteredByKind.size()]);
		}
		//

		if (f.pattern == null) {

			if (f.index == null && children.length > 0) {
				// Locate first button without title
				if (classes != null) {
					List<Class<?>> classesList = Arrays.asList(classes);
					if (classesList.contains(Button.class)) {

						for (SWTUIElement swtuiElement : children) {
							String text = swtuiElement.getText();
							if (text != null && text.trim().length() == 0) {
								return swtuiElement;
							}
						}
					}
				}
			}
			//

			int index = (f.index == null) ? 0 : f.index;
			if (children.length > index) {
				// Search index same as calculated when record in
				// SWTWidgetLocator

				// the children array is already filtered
				SWTUIElement child = children[index];

				// TODO analyze usages & eliminate kind overriding
				if (kind != null && !kind.is(child.getKind()))
					child.overrideKind(kind);

				return child;
			}
		}

		if (checkText) {
			int cur = 0;
			Map<SWTUIElement, String> wtMap = new HashMap<SWTUIElement, String>();
			if (f.pattern != null) {
				for (SWTUIElement widget : children) {
					String text = getText(widget);
					if (text != null) {
						text = removeAcceleratorFromText(text);
						wtMap.put(widget, text);
						if (text != null && text.equals(f.pattern)) {
							if ((f.index != null && cur == f.index.intValue()) || f.index == null) {
								return widget;
							}
							if (kind == null || widget.getKind().is(kind)) {
								cur++;
							}
						}
					}
				}
				cur = 0;
				for (SWTUIElement widget : children) {
					String text = getText(widget);
					if (text != null) {
						text = removeAcceleratorFromText(text);
						wtMap.put(widget, text);
						if (text != null && (safeMatches(text, f.pattern))) {
							if ((f.index != null && cur == f.index.intValue()) || f.index == null) {
								return widget;
							}
							if (kind == null || widget.getKind().is(kind)) {
								cur++;
							}
						}
					}
				}
			}
		}
		return null;
	}

	// stable view is a view that do not change its title, so
	// we can skip it while gathering actual titles of views
	private static final Set<String> stableViews = new HashSet<String>();
	static {
		stableViews.add("org.eclipse.ui.views.PropertySheet");
		stableViews.add("org.eclipse.ui.views.ProblemView");
	}

	// @SuppressWarnings("restriction")
	public SWTUIElement selectView(PlayerSelectionFilter f) {
		final String pattern = f.pattern;

		// IViewDescriptor[] views =
		// PlatformUI.getWorkbench().getViewRegistry().getViews();
		IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences();
		int currIdx = 0;
		for (IViewReference iViewRef : views) {
			try {
				String label = iViewRef.getPartName();
				String id = iViewRef.getId();

				if ((label != null && (label.equals(pattern) || safeMatches(label, pattern)))
						|| (id != null && (id.equals(pattern) || safeMatches(id, pattern)))) {
					if (f.index == null || f.index.equals(currIdx))
						return wrap(iViewRef);
					currIdx++;
				}
			} catch (Exception e) {
				// Skip brokeb parts.
				TeslaCore.log(e);
			}
		}

		TeslaCore.log("Can not find view by pattern \"" + pattern + "\". Activating views...");

		// Not found, lets go with resolve of view parts, it will initialize
		// titles.
		currIdx = 0;
		for (IViewReference iViewRef : views) {
			// try to skip well-known buggy views with immutable titles
			if (stableViews.contains(iViewRef.getId()))
				continue;

			IWorkbenchPart part = iViewRef.getPart(true);
			String title = part != null ? part.getTitle() : null;

			if ((title != null && (title.equals(pattern) || safeMatches(title, pattern)))) {
				if (f.index == null || f.index.equals(currIdx))
					return wrap(iViewRef);
				currIdx++;
			}
		}
		return null;
	}

	private static boolean matches(String value, String pattern) {
		return pattern == null || (value != null && (value.equals(pattern) || safeMatches(value, pattern)));
	}

	private static boolean matches(Integer value, Integer pattern) {
		return pattern == null || (value != null && value.equals(pattern));
	}

	public SWTUIElement selectEditor(PlayerSelectionFilter f) {
		String title = f.pattern;
		if (title != null && title.length() == 0)
			title = null;

		String type = f.classPattern;
		if (type != null && type.length() == 0)
			type = null;

		//

		IEditorReference[] refs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();

		if (f.index != null && f.index < 0)
			return null;

		// -- old style

		if (title != null && type == null && f.index == null) {
			for (IEditorReference ref : refs)
				if (matches(ref.getPartName(), title) || matches(ref.getId(), title))
					return wrap(ref);
			return null;
		}

		// -- new style

		String id = null;
		if (type != null)
			for (IEditorDescriptor desc : ((EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry())
					.getSortedEditorsFromPlugins())
				if (matches(desc.getLabel(), type))
					id = desc.getId();

		int counter = 0;
		for (IEditorReference ref : refs)
			if (matches(ref.getPartName(), title) && matches(ref.getId(), id))
				if (matches(counter++, f.index))
					return wrap(ref);

		return null;
	}

	/**
	 * Put the caret at the given position in the styled text widget by clicking
	 * there. This also clears the current text selection.
	 */
	public void setTextOffset(final StyledText styledText, final int offset, final int line) {
		exec("Set text offset", new Runnable() {
			@Override
			public void run() {
				// styledText.setFocus();// forced focus (see QS-910)
				// Point selectionRange = styledText.getSelection();
				events.sendFocus(styledText);
				int actualOffset = offset;
				if (line != -1) {
					actualOffset += styledText.getOffsetAtLine(line);
				}
				Point clickPoint = styledText.getLocationAtOffset(actualOffset);
				styledText.setCaretOffset(actualOffset);
				styledText.getAccessible().textCaretMoved(offset);
				events.sendEvent(styledText, SWT.MouseDown, clickPoint, 1);
				events.sendEvent(styledText, SWT.MouseUp, clickPoint, 1);
				styledText.setSelectionRange(actualOffset, 0);
				// styledText.setSelection(selectionRange.x,
				// selectionRange.y);
			}
		});
	}

	public void click(final SWTUIElement w) {
		click(w, false, false, false, Events.EMPTY_MASK);
	}

	public void click(final SWTUIElement w, final boolean isDefault, final boolean doubleClick, final boolean arrow,
			final int mask) {
		exec("click", new Runnable() {
			@Override
			public void run() {
				for (ISWTUIPlayerExtension ext : getExtensions()) {
					if (ext.canClick(w, isDefault, doubleClick, arrow)) {
						ext.click(w, isDefault, doubleClick, arrow, mask);
						return;
					}
				}

				if (!canClick(w)) {
					failClick(w);
				}

				IWorkbenchPage page = getTargetPage();
				switch (w.getKind().kind) {
				case View:
					clickView(w, page);
					break;
				case Editor:
					clickEditor(w, page);
					break;
				case TabItem:
					clickTabItem(w, isDefault);
					break;
				case Link:
					clickLink(w, doubleClick, mask);
					break;
				case Item:
					clickTableTreeItem(w, doubleClick);
					break;
				case Label:
					clickLabel(w, mask);
					break;
				default:
					Widget widget = unwrapWidget(w);
					if (widget.isDisposed()) {
						break;
					}

					if (doubleClick) {
						events.sendFocus(widget);
						events.sendAll(widget, Events.createDoubleClick());
						events.sendUnfocus(widget);
						break;
					}

					if (widget instanceof MenuItem) {
						clickMenuItem(w, isDefault, widget);
						break;
					}
					final boolean isButton = widget instanceof Button;
					final boolean isRadioButton = isButton && (widget.getStyle() & SWT.RADIO) != 0;
					final boolean isWin32 = Platform.getOS().equals(Platform.OS_WIN32);
					final boolean isSelectionButton = isButton && ((Button) widget).getSelection();
					events.sendFocus(widget);
					if (isRadioButton && (!isWin32 || !isSelectionButton)) {
						sendEventsToRadioButtons(widget);
					}
					if (isButton && ((widget.getStyle() & SWT.CHECK) != 0)) {
						Button b = (Button) widget;
						b.setSelection(!b.getSelection());
					}
					if (widget instanceof ToolItem
							&& ((widget.getStyle() & SWT.CHECK) != 0 || (widget.getStyle() & SWT.RADIO) != 0)) {
						ToolItem b = (ToolItem) widget;
						b.setSelection(!b.getSelection());
					}

					Point clickPoint = getClickPoint(w);

					events.sendEvent(w, SWT.MouseEnter, mask);
					events.sendEvent(w, SWT.MouseHover, mask);
					events.sendEvent(w, SWT.MouseDown, clickPoint, 1, mask);

					if (isToggleButton(widget)) {
						((Button) widget).setSelection(!((Button) widget).getSelection());
					}
					if (!isRadioButton || !isWin32 || isSelectionButton) {
						Event event = events.createEvent(w);
						if (arrow) {
							event.detail = SWT.ARROW;
						}
						event.type = isDefault ? SWT.DefaultSelection : SWT.Selection;
						event.stateMask = mask;
						events.sendEvent(w, event);
					}

					events.sendEvent(w, SWT.MouseUp, clickPoint, 1, mask);
					events.sendEvent(w, SWT.MouseExit);
					events.sendUnfocus(widget);

					break;
				}
			}

			private void sendEventsToRadioButtons(Widget widget) {
				Button button = (Button) widget;
				sendEventPreviousSelected(button);
				button.setSelection(true);
			}

			private void sendEventPreviousSelected(Button button) {
				SWTUIElement[] siblings = null;
				int parentStyle = button.getParent().getStyle();
				if ((parentStyle & SWT.NO_RADIO_GROUP) == 0) {
					siblings = children.collectFor(wrap(button.getParent()), new SWTUIElement[] { w }, false,
							Button.class);
				}
				if (siblings == null) {
					return;
				}
				for (SWTUIElement element : siblings) {
					Button previousButton = (Button) unwrap(element);
					if ((previousButton.getStyle() & SWT.RADIO) != 0 && previousButton.getSelection()) {
						previousButton.setSelection(false);
						events.sendEvent(element, SWT.Selection);
						return;
					}
				}
			}

		});
	}

	private static final Point LEGACY_CLICK_POINT = new Point(0, 0);

	private static Point getMiddleClickPoint(Control w) {
		Point size = w.getSize();
		return new Point(size.x / 2, size.y / 2);
	}

	private static Point getClickPoint(SWTUIElement element) {
		Widget widget = unwrapWidget(element);
		switch (element.getKind().kind) {
		case Tree:
			Tree tree = (Tree) widget;
			TreeItem[] selection = tree.getSelection();
			if (selection.length < 1)
				return LEGACY_CLICK_POINT;

			TreeItem item = selection[0];
			Point point = Viewers.getSafeToClickPoint(item);
			if (point == Viewers.UNSAFE_CLICK_POINT)
				return LEGACY_CLICK_POINT;
			return point;
		default:
			if (widget instanceof Control)
				return getMiddleClickPoint((Control) widget);
			return LEGACY_CLICK_POINT;
		}
	}

	private void clickMenuItem(final SWTUIElement w, final boolean isDefault, Widget widget) {
		MenuItem menuItem = (MenuItem) widget;
		hidePopupMenus(menuItem);
		// Radio MenuItem
		if ((menuItem.getStyle() & SWT.RADIO) != 0) {
			Menu parentMenu = menuItem.getParent();
			if (parentMenu != null && !parentMenu.isDisposed() && (parentMenu.getStyle() & SWT.NO_RADIO_GROUP) == 0) {
				int index = 0;
				MenuItem[] items = parentMenu.getItems();

				while (index < items.length && items[index] != menuItem)
					index++;
				int lowBound = index - 1;
				while (lowBound >= 0 && (items[lowBound].getStyle() & SWT.RADIO) != 0
						&& !items[lowBound].isDisposed()) {
					items[lowBound].setSelection(false);
					--lowBound;
				}
				int upperBound = index + 1;
				while (upperBound < items.length && (items[upperBound].getStyle() & SWT.RADIO) != 0
						&& !items[upperBound].isDisposed()) {
					items[upperBound].setSelection(false);
					++upperBound;
				}
			}
			if (!menuItem.isDisposed()) {
				menuItem.setSelection(true);
				events.sendEvent(menuItem, SWT.Selection);
			}
		}
		// Other MenuItem
		else {
			boolean newSelection = menuItem.getSelection();
			if ((menuItem.getStyle() & SWT.CHECK) != 0) {
				newSelection = !menuItem.getSelection();
				menuItem.setSelection(newSelection);
			}

			events.sendEvent(w, isDefault ? SWT.DefaultSelection : SWT.Selection);

			if (!menuItem.isDisposed()) {
				menuItem.setSelection(newSelection);
			}
		}

		// -- simulate normal flow of events

		if (menuItem.isDisposed())
			return;
		Menu parent = menuItem.getParent();
		while (parent != null) {
			if (parent.isDisposed())
				return;
			events.sendEvent(parent, SWT.Hide);
			if (parent.isDisposed())
				return;
			parent = parent.getParentMenu();
		}
	}

	private void hidePopupMenus(MenuItem menuItem) {
		Menu parent = menuItem.getParent();
		while (parent != null) {
			List<WeakReference<Menu>> popupMenus = TeslaEventManager.getManager().getPopupMenus();
			for (WeakReference<Menu> weakReference : popupMenus) {
				Menu reffered = weakReference.get();
				if (parent.equals(reffered)) {
					popupMenus.remove(weakReference);
					break;
				}
			}
			parent = parent.getParentMenu();
		}
	}

	private void failClick(final SWTUIElement w) {
		setBackgroundColor(w, SWT.COLOR_RED);
		makeScreenShot();
		throw new RuntimeException(NLS.bind(TeslaSWTMessages.SWTUIPlayer_CannotClickOnDisabledControl, w.toString()));
	}

	private IWorkbenchPage getTargetPage() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page == null) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
			page = window.getActivePage();
			if (page == null) {
				page = window.getPages()[0];
			}
		}
		return page;
	}

	private void clickView(final SWTUIElement w, IWorkbenchPage page) {
		IViewReference view = (IViewReference) (((WorkbenchUIElement) w).reference);

		IWorkbenchPart part = view.getPart(true);
		if (part == null) {
			return;
		}
		page.activate(part);
		IWorkbenchPart activePart = page.getActivePart();
		if (!part.equals(activePart)) {
			throw new RuntimeException(NLS.bind(TeslaSWTMessages.SWTUIPlayer_WorkbenchPartNotActivated, w.toString()));
		}
	}

	private void clickLink(final SWTUIElement w, boolean doubleClick, int mask) {
		Widget widget = unwrapWidget(w);
		if (widget.isDisposed()) {
			return;
		}
		if (doubleClick) {
			events.sendFocus(widget);
			events.sendAll(widget, Events.createDoubleClick());
			events.sendUnfocus(widget);
		} else {
			events.sendAll(widget,
					new Event[] { createMouseDown(Events.DEFAULT_BUTTON, Events.SINGLE_COUNT, mask, 0, 0),
							createSelection(false, mask), createMouseUp() });
		}
	}

	private void clickEditor(final SWTUIElement w, IWorkbenchPage page) {
		IEditorReference editor = (IEditorReference) (((WorkbenchUIElement) w).reference);
		IWorkbenchPart editorPart = editor.getPart(true);
		page.bringToTop(editorPart);
		page.activate(editorPart);
	}

	private void clickTabItem(final SWTUIElement w, final boolean isDefault) {
		Item rawItem = (Item) unwrapWidget(w);
		Composite tabParent = TabCTabUtil.getParent(rawItem);
		TabCTabUtil.setSelection(tabParent, rawItem);
		Point pos = Bounds.centerAbs(TabCTabUtil.getBounds(rawItem));
		events.sendAll(tabParent, rawItem, new Event[] { Events.createMouseDown(pos),
				Events.createSelection(isDefault, Events.EMPTY_MASK), 
				Events.createMouseUp(pos) });
	}

	private void clickLabel(final SWTUIElement w, int stateMask) {
		Widget widget = w.unwrap();
		events.sendFocus(widget);
		for (Event event : createClick(Events.DEFAULT_BUTTON, stateMask, centerRel(w.getBounds()).x,
				centerRel(w.getBounds()).y)) {
			events.sendEvent(w, event);
		}
		events.sendUnfocus(widget);
	}

	private void clickTableTreeItem(final SWTUIElement w, final boolean doubleClick) {
		final int column = w instanceof ItemUIElement ? ((ItemUIElement) w).getColumn() : -1;
		final Widget item = (Widget) unwrap(w);
		final Widget itemParent = TableTreeUtil.getParent(item);
		Viewers.selectItem(w, false);
		final Point itemCenter = centerAbs(column >= 0 ? getItemBounds(item, column) : getItemBounds(item));
		w.getPlayer().exec("click cell", new Runnable() {
			@Override
			public void run() {
				if (doubleClick) {
					getEvents().sendFocus(itemParent);
					getEvents().sendAll(itemParent, Events.createDoubleClick(itemCenter));
					getEvents().sendUnfocus(itemParent);
				} else {
					getEvents().sendEvent(itemParent, Events.createMouseDown(itemCenter));
					getEvents().sendEvent(itemParent, Events.createMouseUp(itemCenter));
				}
			}
		});
	}

	private static void checkCell(Item item, int column, boolean state) {
		TeslaCellEditorManager.getInstance().setNextCellEditorActivateEventType(ColumnViewerEditorActivationEvent.PROGRAMMATIC);
		if (item instanceof TreeItem) {
			Tree tree = ((TreeItem) item).getParent();
			TreeViewer v = (TreeViewer) TeslaSWTAccess.getViewer(tree);
			if (v == null)
				throw new RuntimeException("No tree viewer.");
			if (column < 0 || column >= tree.getColumnCount())
				throw new RuntimeException("Invalid tree column index.");
			TreeColumn c = tree.getColumn(column);
			Object data = c.getData("org.eclipse.jface.columnViewer");
			if (!(data instanceof ViewerColumn))
				throw new RuntimeException("No tree column viewer.");
			ViewerColumn cv = (ViewerColumn) data;
			EditingSupport es = TeslaSWTAccess.getField(EditingSupport.class, cv, "editingSupport");
			if (es == null)
				throw new RuntimeException("No editing support for tree column viewer.");
			Object value = TeslaSWTAccess.callMethod(EditingSupport.class, es, "getValue", new Class[] { Object.class },
					item.getData());
			if (!(value instanceof Boolean))
				throw new RuntimeException("Does not look like a checkbox tree column.");
			if ((Boolean) value == state)
				return;
			v.editElement(item.getData(), column);
		} else if (item instanceof TableItem) {
			Table table = ((TableItem) item).getParent();
			TableViewer v = (TableViewer) TeslaSWTAccess.getViewer(table);
			if (v == null)
				throw new RuntimeException("No table viewer.");
			if (column < 0 || column >= table.getColumnCount())
				throw new RuntimeException("Invalid table column index.");
			TableColumn c = table.getColumn(column);
			Object data = c.getData("org.eclipse.jface.columnViewer");
			if (!(data instanceof ViewerColumn))
				throw new RuntimeException("No table column viewer.");
			ViewerColumn cv = (ViewerColumn) data;
			EditingSupport es = TeslaSWTAccess.getField(EditingSupport.class, cv, "editingSupport");
			if (es == null)
				throw new RuntimeException("No editing support for table column viewer.");
			Object value = TeslaSWTAccess.callMethod(EditingSupport.class, es, "getValue", new Class[] { Object.class },
					item.getData());
			if (!(value instanceof Boolean))
				throw new RuntimeException("Does not look like a checkbox table column.");
			if ((Boolean) value == state)
				return;
			v.editElement(item.getData(), column);
		}
	}

	public void check(final SWTUIElement w, final boolean state) {
		Widget widget = unwrapWidget(w);
		if (!widget.isDisposed()) {
			if (w instanceof ItemUIElement && widget instanceof Item) {
				checkCell((Item) widget, ((ItemUIElement) w).getColumn(), state);
			} else if ((widget.getStyle() & SWT.CHECK) != 0) {
				if (widget instanceof Button) {
					if (((Button) widget).getSelection() != state) {
						click(w);
					}
				} else if (widget instanceof MenuItem) {
					if (((MenuItem) widget).getSelection() != state) {
						click(w);
					}
				} else if (widget instanceof ToolItem) {
					if (((ToolItem) widget).getSelection() != state) {
						click(w);
					}
				}
			}
		}
	}

	/**
	 * Wraps an UI element (e.g. SWT widget, but not only them) into a
	 * convenient wrap to perform various actions on it with the wrap's methods.
	 * <p>
	 * The wrapping layer also serves as an unification of various kinds of UI
	 * elements.
	 * <p>
	 * The functionality of this method can be extended by
	 * {@link ISWTUIPlayerExtension#wrap(Object, SWTUIPlayer)} method.
	 *
	 * @see SWTUIElement
	 */
	public SWTUIElement wrap(Object s) {

		for (ISWTUIPlayerExtension ext : getExtensions()) {
			SWTUIElement result = ext.wrap(s, this);
			if (result != null) {
				return result;
			}
		}

		//

		if (s instanceof SWTUIElement) {
			return (SWTUIElement) s;
		}

		if (s instanceof IWorkbenchPart) {
			IWorkbenchPart part = (IWorkbenchPart) s;
			IWorkbenchPartSite site = part.getSite();
			if (site != null) {
				// System.out.println("Site is not null");
				IWorkbenchWindow window = site.getWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IWorkbenchPartReference reference = page.getReference(part);
					if (reference != null) {
						return new WorkbenchUIElement(reference, this);
					}
				}
				IWorkbenchPage[] pages = window.getPages();
				for (IWorkbenchPage wp : pages) {
					IWorkbenchPartReference ref = wp.getReference(part);
					if (ref != null) {
						return new WorkbenchUIElement(ref, this);
					}
				}
			} else {
				// System.out.println("Site is null");
			}
			// Obtain using initialization
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow win : windows) {
				IWorkbenchPage[] pages = win.getPages();
				for (IWorkbenchPage wp : pages) {
					IWorkbenchPartReference reference = wp.getReference(part);
					if (reference != null) {
						return new WorkbenchUIElement(reference, this);
					}
				}
			}
			// System.out.println("Failed to create wrap for item:"
			// + part.getTitle());
		}
		if (s instanceof IWorkbenchPartReference) {
			return new WorkbenchUIElement((IWorkbenchPartReference) s, this);
		}
		if (s instanceof Widget) {
			return new SWTUIElement((Widget) s, this);
		}
		return null;
	}

	public final ChildrenCollector children = new ChildrenCollector(this);

	Point getMousePos(Widget c) {
		Point xy = new Point(0, 0);
		Point point = widgetToMouseForMenus.get(c);
		if (point != null) {
			xy.x = point.x;
			xy.y = point.y;
		} else {
			// Calculate correct position
			if (c instanceof TabFolder) {
				TabItem[] items = ((TabFolder) c).getSelection();
				if (items.length > 0) {
					Rectangle bounds = items[0].getBounds();
					return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
				}
			} else if (c instanceof CTabFolder) {
				CTabItem selection = ((CTabFolder) c).getSelection();
				if (selection != null) {
					Rectangle bounds = selection.getBounds();
					xy.x = bounds.x + bounds.width / 2;
					xy.y = bounds.y + bounds.height / 2;
					return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
				}
			} else if (c instanceof Table) {
				if (((Table) c).getItemCount() == 0) {
					// Calculate center by x, and 1/10 from below
					Rectangle bounds = ((Table) c).getBounds();
					return new Point(bounds.width / 2, 9 * (bounds.height / 10));
				}
			} else if (c instanceof Tree) {
				if (((Tree) c).getItemCount() == 0) {
					// Calculate center by x, and 1/10 from below
					Rectangle bounds = ((Tree) c).getBounds();
					return new Point(bounds.width / 2, 9 * (bounds.height / 10));
				}
			}
		}
		return xy;
	}

	public UIColor getSystemColor(int color) {
		return new SWTUIColor(display.getSystemColor(color));
	}

	public SWTUIElement selectMenu(PlayerSelectionFilter f) {
		if (f.path == null) {
			return null;
		}

		SWTUIElement currentParent = f.parent;
		if (currentParent.getKind().is(ElementKind.Item)) {
			// so that get-menu can be invoked in tree and table items
			Widget unwrapped = unwrapWidget(currentParent);
			if (unwrapped instanceof TreeItem || unwrapped instanceof TableItem) {
				currentParent = getParentElement(currentParent);
			}
		} else if (currentParent.getKind().is(ElementKind.Button)) {
			// support button drop-downs that are not children of Button itself

			while (true) {
				SWTUIElement[] children = this.children.collectFor(currentParent, ignoreWindows, false,
						one(MenuItem.class), f.after);
				if (children.length > 0)
					break;

				currentParent = getParentElement(currentParent);
				if (currentParent == null) {
					currentParent = f.parent;
					break;
				}
			}

		}

		List<String> parts = Arrays.asList(f.path);
		for (int i = 0, count = parts.size(); i < count; i++) {
			String part = parts.get(i);
			SWTUIElement[] children = this.children.collectFor(currentParent,
					ignoreWindows, false, one(MenuItem.class), f.after);

			boolean isLast = i == count - 1;
			List<SWTUIElement> menuItems = findMenuItems(children, part, !isLast);
			if (menuItems.isEmpty()) {
				return null;
			}

			if (isLast && f.index != null) {
				if (f.index < 0 || f.index >= menuItems.size()) {
					return null;
				}
				currentParent = menuItems.get(f.index);
			} else {
				currentParent = menuItems.get(0);
			}
		}

		return currentParent;
	}

	private List<SWTUIElement> findMenuItems(SWTUIElement[] items, String name, boolean returnFirstElement) {
		List<SWTUIElement> result = new ArrayList<SWTUIElement>();
		for (SWTUIElement uiElement : items) {
			String elementName = getText(uiElement);
			if (elementName == null) {
				continue;
			}
			elementName = getMenuText(elementName);
			if (elementName != null && (elementName.equals(name) || safeMatches(elementName, name))) {
				// -- simulate normal flow of events

				// disabled since probably it is not the best place to do
				// such things, needs discussion

				/*
				 * MenuItem menuItem = (MenuItem) unwrapWidget(uiElement);
				 * Menu menu = menuItem.getMenu();
				 * events.sendEvent(uiElement, SWT.Arm); if (menu != null) {
				 * events.sendEvent(menu, SWT.Show);
				 * menuItem.setSelection(true); events.sendEvent(menuItem,
				 * SWT.Selection); }
				 */
				// --

				result.add(uiElement);
				if (returnFirstElement) {
					return result;
				}
			}
		}
		return result;
	}

	public UIColor getBackgroundColor(SWTUIElement uiElement) {

		final Widget widget = unwrapWidget(uiElement);
		if (widget instanceof Control) {
			return new SWTUIColor(((Control) widget).getBackground());

		}
		return null;
	}

	public int countItems(SWTUIElement element, String[] selection) {
		final Object widget = unwrap(element);
		if (widget instanceof Tree) {
			return Viewers.countTreeItemChildren(element, selection);
		} else if (widget instanceof Table) {
			return ((Table) widget).getItemCount();
		} else if (widget instanceof org.eclipse.swt.widgets.List) {
			return ((org.eclipse.swt.widgets.List) widget).getItemCount();
		} else if (widget instanceof TabFolder) {
			return ((TabFolder) widget).getItemCount();
		} else if (widget instanceof CTabFolder) {
			return ((CTabFolder) widget).getItemCount();
		}
		// TODO other controls?
		return 0;
	}

	public void setBackgroundColor(final SWTUIElement uiElement, final int color) {

		setBackgroundColor(uiElement, new SWTUIColor(display.getSystemColor(color)));
	}

	public void setBackgroundColor(SWTUIElement uiElement, final UIColor color) {
		final Widget widget = unwrapWidget(uiElement);
		if (widget instanceof Control && !widget.isDisposed()) {
			exec("setBackground", new Runnable() {
				@Override
				public void run() {
					if (!widget.isDisposed()) {
						((Control) widget).setBackground(((SWTUIColor) color).getColor());
					}
				}
			});
		}
	}

	public void setDateTime(final SWTUIElement uiElement, final int year, final int month, final int day,
			final int hours, final int minutes, final int second) {
		final Widget widget = unwrapWidget(uiElement);
		exec("setDateTime", new Runnable() {
			@Override
			public void run() {
				if (widget.isDisposed()) {
					return;
				}
				if (widget instanceof DateTime) {
					DateTime dt = (DateTime) widget;
					events.sendFocus(widget);
					dt.setDate(year, month - 1, day);
					dt.setTime(hours, minutes, second);
					events.sendEvent(uiElement, SWT.Selection);
					events.sendUnfocus(widget);
				}
			}
		});
	}

	public void setText(final SWTUIElement uiElement, final String text) {
		setText(uiElement, text, false);
	}

	public static final int COMBO_ITEM_NOT_FOUND = -1;

	public static int findComboItem(String[] items, String item, boolean select) {
		for (int i = 0; i < items.length; ++i)
			if (items[i].equals(item))
				return i;

		if (!select)
			return COMBO_ITEM_NOT_FOUND;

		for (int i = 0; i < items.length; ++i)
			if (safeMatches(items[i], item))
				return i;

		item = item.toLowerCase();
		String[] lowerItems = new String[items.length];
		for (int i = 0; i < items.length; ++i)
			lowerItems[i] = items[i].toLowerCase();

		for (int i = 0; i < items.length; ++i)
			if (lowerItems[i].equals(item))
				return i;

		for (int i = 0; i < items.length; ++i)
			if (safeMatches(lowerItems[i], item))
				return i;

		return COMBO_ITEM_NOT_FOUND;
	}

	public void setText(final SWTUIElement uiElement, final String text, final boolean select) {
		final Widget widget = unwrapWidget(uiElement);
		exec("setText", new Runnable() {
			@Override
			public void run() {
				if (widget.isDisposed()) {
					return;
				}
				if (widget instanceof Text) {
					Text textElement = (Text) widget;
					events.sendFocus(widget);
					textElement.setText(text);
					events.sendEvent(uiElement, SWT.Verify);
					events.sendEvent(uiElement, SWT.Modify);
					events.sendUnfocus(widget);
				}
				if (widget instanceof Spinner) {
					events.sendFocus(widget);
					int Val = (int) (Double.parseDouble(text) * Math.pow(10, ((Spinner) widget).getDigits()));
					((Spinner) widget).setSelection(Val);
					events.sendEvent(uiElement, SWT.Modify);
					events.sendUnfocus(widget);
				}
				if (widget instanceof Slider) {
					events.sendFocus(widget);
					int Val = Integer.parseInt(text);
					((Slider) widget).setSelection(Val);
					events.sendEvent(uiElement, SWT.Modify);
					events.sendEvent(uiElement, SWT.Selection);
					events.sendUnfocus(widget);
				}
				if (widget instanceof StyledText) {
					events.sendFocus(widget);
					((StyledText) widget).setText(text);
					if (widget instanceof StyledText) {
						StyledText tt = (StyledText) widget;
						int offset = tt.getCaretOffset();
						String allText = tt.getText();
						tt.setText(allText);
						tt.setCaretOffset(offset);
					}
					events.sendUnfocus(widget);
				}
				if (widget instanceof Combo) {
					Combo combo = (Combo) widget;
					events.sendFocus(widget);

					int itemIndex = findComboItem(combo.getItems(), text, select);
					if (!select)
						combo.setText(text);
					if (itemIndex != COMBO_ITEM_NOT_FOUND)
						combo.select(itemIndex);

					events.sendEvent(uiElement, SWT.Modify);
					events.sendEvent(uiElement, SWT.Selection);
					events.sendUnfocus(widget);
				}
				if (widget instanceof CCombo) {
					CCombo combo = (CCombo) widget;
					events.sendFocus(widget);

					int itemIndex = findComboItem(combo.getItems(), text, select);
					if (!select)
						combo.setText(text);
					if (itemIndex != COMBO_ITEM_NOT_FOUND)
						combo.select(itemIndex);

					events.sendEvent(uiElement, SWT.Modify);
					events.sendEvent(uiElement, SWT.Selection);
					events.sendUnfocus(widget);
				}
				if (widget instanceof Browser) {
					Browser browser = (Browser) widget;
					events.sendFocus(widget);
					browser.setText(text);
					events.sendEvent(uiElement, SWT.Verify);
					events.sendEvent(uiElement, SWT.Modify);
					events.sendUnfocus(widget);
				}
			}

		});
	}

	protected boolean checkContainsControl(Control control, Combo combo) {
		return false;
	}

	public void show(SWTUIElement uiElement, int x, int y) {
		Menu menu = (Menu) uiElement.unwrap();
		Point pos = new Point(x, y);
		if (pos.x == -1 && pos.y == -1) {
			pos = getMousePos(menu);
		}

		if ((menu.getStyle() & SWT.BAR) == 0) { // Not a menu bar
			synchronized (shownMenus) {
				shownMenus.add(new WeakReference<Menu>(menu));
			}
		}

		events.sendEvent(uiElement, SWT.Show, pos.x, pos.y, 0);
	}

	public boolean isDisposed(SWTUIElement uiElement) {
		Widget widget = unwrapWidget(uiElement);
		return widget == null || widget.isDisposed();
	}

	protected static void checkIntegrity(Class<?>[] classes) {
		for (int i = 0; i < classes.length; i++) {
			for (int j = i + 1; j < classes.length; j++) {
				Class<?> ci = classes[i];
				Class<?> cj = classes[j];
				if (ci.isAssignableFrom(cj)) {
					System.out.println(String.format("Achtung! %s should be after %s", ci.getName(), cj.getName()));
				}

			}
		}
	}

	public static Class<?> getSearchableClass(Object widget) {
		ElementKind kind = elementKinds.get(widget.getClass());
		if (kind == null) {
			// Try to find superclass for custom widget in extensions
			for (ISWTUIPlayerExtension extension : getExtensions()) {
				Class<?> searchableClass = extension.getSearchableClass(widget);
				if (searchableClass != null) {
					return searchableClass;
				}
			}
			// Try to find superclass for custom widget in "default" elements
			for (Map.Entry<Class<?>, ElementKind> entry : elementKinds.entrySet()) {
				Class<?> key = entry.getKey();
				if (key.isInstance(widget)) {
					return key;
				}
			}
		}
		return widget.getClass();
	}

	public static GenericElementKind getKind(Object w) {
		if (w == null)
			return GenericElementKind.Unknown;

		for (ISWTUIPlayerExtension extension : getExtensions()) {
			GenericElementKind kind = extension.getKind(w);
			if (kind != null) {
				return kind;
			}
		}

		ElementKind kind = elementKinds.get(w.getClass());
		if (kind == null) {
			for (Map.Entry<Class<?>, ElementKind> entry : elementKinds.entrySet()) {
				Class<?> key = entry.getKey();
				if (key.isInstance(w)) {
					return new GenericElementKind(entry.getValue());
				}
			}
		}
		if (kind != null) {
			return new GenericElementKind(kind);
		}
		return GenericElementKind.Unknown;
	}

	public void close(final SWTUIElement uiElement) {
		exec("close", new Runnable() {
			@Override
			public void run() {
				if (uiElement instanceof WorkbenchUIElement) {
					IWorkbenchPartReference reference = ((WorkbenchUIElement) uiElement).getReference();
					if (reference == null) {
						return;
					}
					IWorkbenchPart part = reference.getPart(false);
					if (part != null) {
						IWorkbenchPage page = part.getSite().getPage();
						if (part instanceof IEditorPart) {
							page.closeEditor((IEditorPart) part, true);
						} else if (part instanceof IViewPart) {
							IViewPart vp = (IViewPart) part;
							page.hideView(vp);
							// hideView already call dispose for ViewPart
							// vp.dispose();
						}
					}
				} else {
					Widget widget = unwrapWidget(uiElement);
					if (widget instanceof CTabItem) {
						CTabItem item = (CTabItem) widget;
						Rectangle rect = TeslaSWTAccess.getCTabItemCloseRect(item);
						if (rect != null && rect.width > 0 && rect.height > 0) {
							events.sendEvent(item.getParent(), SWT.MouseDown, rect.x + 1, +rect.y + 1, 1);
							events.sendEvent(item.getParent(), SWT.MouseUp, rect.x + 1, rect.y + 1, 1);
						}

					} else if (widget instanceof Shell) {
						getEvents().sendEvent(widget, SWT.Deactivate);
						((Shell) widget).close();
					} else {
						Event e = events.sendEvent(uiElement, SWT.Close);
						if (e != null && e.doit) {
							if (widget != null && !widget.isDisposed()) {
								widget.dispose();
							}
						}
					}
				}
			}
		});
	}

	public List<File> getScreenshots() {
		if (screenshotsDuringSession != null) {
			return new ArrayList<File>(screenshotsDuringSession);
		}
		return new ArrayList<File>();
	}

	/**
	 * NOTE: check that used widgets are not disposed. See
	 * {@link Display#asyncExec(Runnable)} for details.
	 */
	public void exec(final String msg, final Runnable runnable) {
		final Exception e = new Exception();
		StackTraceElement stackTraceElement = e.getStackTrace()[1];
		final String errorMethod = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + ":"
				+ stackTraceElement.getLineNumber();

		final Context currentContext = context;
		Runnable myr = new ExecRunnable(msg, currentContext, runnable, errorMethod);
		if (context != null) {
			synchronized (runnables) {
				List<Runnable> runs = runnables.get(context);
				if (runs == null) {
					runs = new ArrayList<Runnable>();
					runnables.put(context, runs);
				}
				runs.add(myr);
			}
		}
		display.asyncExec(myr);
		display.wake();
		// runnable.run();
	}

	public boolean canProceed(Context context, Q7WaitInfoRoot info) {
		if (!display.equals(Display.getCurrent())) {
			// Q7WaitUtils.updateInfo("display", "non current", info);
			debugProceed("Wrong display");
			return false;
		}
		// Return false if we have SWT observable in timers
		if (hasTimers(display, info)) {
			debugProceed("Timers active");
			return false;
		}
		// Check for asyncs in synchronizer
		if (!TeslaEventManager.getManager().isNoWaitForJob() && hasRunnables(display)) {
			// Q7WaitUtils.updateInfo("display", "runnables", info);
			debugProceed("Display has runnables");
			return false;
		}
		if (!BrowserManager.getInstance().isExecutionAllowed(info)) {
			debugProceed("Browser active");
			return false;
		}

		synchronized (runnables) {
			this.context = context;
			List<Runnable> runs = runnables.get(context);
			if (runs != null && !runs.isEmpty()) {
				debugProceed("Previous tsk is still pending");
				return false;
			}
			if (!TeslaEventManager.getManager().isNoWaitForJob() && !collector.isEmpty(context, info)) {
				debugProceed("There are active jobs");
				return false;
			}
		}

		debugProceed("Can proceed");
		return true;
	}

	public static boolean hasRunnables(Display display) {
		Synchronizer synchronizer = display.getSynchronizer();
		if (TeslaSWTAccess.getRunnables(synchronizer) > 0) {
			return true;
		}
		return false;
	}

	private static long lastTimersSucess = 0;
	private static long lastTimeSucessStep = 0;

	public static boolean hasTimers(Display display, Q7WaitInfoRoot infoRoot) {
		Runnable[] timers = TeslaSWTAccess.getTimers(display);
		List<TimerInfo> map = TeslaTimerExecManager.getManager().getTimers();

		List<TimerInfo> waitFor = new ArrayList<TimerInfo>();
		for (Runnable runnable : timers) {
			if (runnable instanceof SherlockTimerRunnable) {
				runnable = ((SherlockTimerRunnable) runnable).getRunnable();
			}
			TimerInfo current = null;
			for (TimerInfo t : map) {
				if (t.hasRunnable(runnable)) {
					current = t;
					break;
				}
			}
			if (current == null) {
				continue;
			}

			Class<? extends Runnable> cl = runnable.getClass();
			String clName = cl.getName();

			// Return true if some of delayed observables are pressent.
			if (clName.startsWith("org.eclipse.core.internal.databinding.observable.DelayedObservableValue")) {
				waitFor.add(current);
				break;
			}

			if (clName.contains("org.eclipse.jface")) { // Wait
				/*
				 * for all jface timer execs
				 */
				waitFor.add(current);
				break;
			}
			if (isTimerIgnored(clName)) {
				continue;
			}

			if (TeslaLimits.getTimerExecsWait() > 0) {
				// Check if runnable in timers list and timeout is less then job
				// timeout
				boolean add = false;
				for (TimerInfo info : map) {
					if (info.hasRunnable(runnable)) {
						if (info.time < TeslaLimits.getTimerExecsWait()) {
							if ((System.currentTimeMillis() - info.firstSheduleTime) > TeslaLimits.getTimerExecsSkip()
									&& info.resheduleCounter > 0) {
								// Skip this runnable, since it rescheduling
								// itself to much.
								// System.out.println("Skip TimerExec:" +
								// info.getRunnable().getClass().getName());
								printTimers(map);
								continue;
							}
							add = true;
							break;
						}
					}
				}
				if (add) {
					waitFor.add(current);
				}
			}
		}
		if (waitFor.size() > 0 && lastTimersSucess != 0) {
			if ((System.currentTimeMillis() - lastTimersSucess) > TeslaLimits.getTimerExecsTotalWaitTime()) {
				// Enable timers step mode.
				if (lastTimeSucessStep == 0) {
					lastTimeSucessStep = System.currentTimeMillis();
					printTimers(waitFor);
					return false; // Do a step mode.
				} else {
					if ((System.currentTimeMillis() - lastTimeSucessStep) > TeslaLimits
							.getTimerExecsTotalWaitTimeStep()) {
						lastTimeSucessStep = System.currentTimeMillis();
						printTimers(waitFor);
						return false;// Do a step mode.
					}
				}
			}
		}
		if (waitFor.size() > 0) {
			for (TimerInfo timerInfo : waitFor) {
				String timerClassName = timerInfo.getRunnable().getClass().getName();
				synchronized (infoRoot) {
					if (timerInfo.execQualifier != null) {
						infoRoot.getInnerClassMap().put(timerClassName, timerInfo.execQualifier);
					}
				}
				Q7WaitUtils.updateInfo("timer", timerClassName, infoRoot);
			}
			return true;
		}

		lastTimersSucess = System.currentTimeMillis();
		lastTimeSucessStep = 0;
		return false;
	}

	public static boolean isTimerIgnored(String clName) {
		return TeslaTimerExecManager.isTimerIgnored(clName);
	}

	private static void printTimers(List<TimerInfo> map) {
		// StringBuilder bb = new StringBuilder();
		// bb.append("\n<<<<<<---- Timer execs:\n");
		// for (TimerInfo timer : map) {
		// bb.append(timer.getRunnable().getClass().getName())
		// .append(" delay: ")
		// .append(timer.time)
		// .append(" scheduled for time: " + (System.currentTimeMillis() -
		// timer.firstSheduleTime)
		// + " reshedules: " + timer.resheduleCounter)
		// .append("\n");
		// }
		// String msg =
		// "---->>> TimerExec Waiting timeout exceed then execute: "
		// + ReportManager.getBuilder().getCurrentNode()
		// .getName() + bb.toString() + " <<---\n(stepping)";
		// System.err.println(msg);
		// SWTTeslaActivator
		// .log(msg);
	}

	public static String toSelectionItem(String toPattern) {
		if (toPattern != null) {
			toPattern = toPattern.replaceAll("\\+", "\\\\+");
			toPattern = toPattern.replaceAll("\\(", "\\\\(");
			toPattern = toPattern.replaceAll("\\)", "\\\\)");
			toPattern = toPattern.replaceAll("\\[", "\\\\[");
			toPattern = toPattern.replaceAll("\\]", "\\\\]");
			toPattern = toPattern.replaceAll("\\*", "\\\\*");
		}
		return toPattern;
	}

	public static String buildPathFragment(String text, int index) {
		return String.format("%s%%%d%%", toSelectionItem(text).replaceAll("/", "\\\\/"), index);
	}

	public static String buildPathFragment(String text) {
		return toSelectionItem(text).replaceAll("/", "\\\\/");
	}

	public void setIgnores(Shell... ignoreWindows) {
		if (ignoreWindows != null) {
			this.ignoreWindows = new SWTUIElement[ignoreWindows.length];
			this.ignoredShells = ignoreWindows;
			int i = 0;
			for (Shell shell : ignoreWindows) {
				this.ignoreWindows[i++] = wrap(shell);
			}
		}
	}

	public void typeText(final SWTUIElement element, final String text, final int mask, final boolean fromDisplay) {
		exec("typeText", new Runnable() {
			@Override
			public void run() {
				switch (element.getKind().kind) {
				default:
					Widget widget = unwrapWidget(element);
					if (!(widget instanceof Control))
						break;
					Control ctrl = (Control) widget;
					if (fromDisplay) {
						// Necessary for setting focus to control
						try {
							ShellUtilsProvider.getShellUtils().forceActive(ctrl.getShell());
						} catch (CoreException e) {
							throw new RuntimeException(e);
						}
					}
					events.sendFocus(widget);
					if (ctrl instanceof CCombo) {
						CCombo combo = (CCombo) widget;
						Text tW = getCComboText(combo);
						events.sendFocus(tW);
						tW.insert(text);
						events.sendEvent(wrap(tW), SWT.Modify);
						events.sendEvent(wrap(tW), SWT.Deactivate);
					} else if (ctrl instanceof Text) {
						Text tW = (Text) ctrl;
						events.sendFocus(tW);
						tW.insert(text);
						type(element, 0, SWT.SHIFT, false, '\u0000', 1); // fake
						events.sendEvent(wrap(tW), SWT.Modify);
						events.sendEvent(wrap(tW), SWT.Deactivate);
					} else {
						sendKeysToWidget(text, mask, fromDisplay, widget);
						events.sendEvent(wrap(widget), SWT.Deactivate);
					}
					break;
				}
			}

			private void sendKeysToWidget(final String text, final int mask, final boolean fromDisplay, Widget widget) {
				SWTUIElement element = wrap(widget);
				if (!widget.isDisposed()) {
					for (char ch : text.toCharArray()) {
						updateStyledTextPos(widget);
						List<KeyStroke> keys = keyboard.splitToKeys(KeyStroke.getInstance(mask, KeyStroke.NO_KEY));
						if (fromDisplay) {
							keys.add(SWTKeyboardLayout.getKeyboardLayout().keyStrokeFor(ch));
						} else {
							if (Character.isUpperCase(ch)) {
								ch = Character.toLowerCase(ch);
								keys.add(KeyStroke.getInstance(SWT.SHIFT, ch));
							} else
								keys.add(KeyStroke.getInstance(ch));
						}
						keyboard.typeKeys(widget, fromDisplay, keys.toArray(new KeyStroke[0]));

						// For StyledText, modifyContent method will be called
						// after MouseDown event and send correct
						// Verify and Modify events
						if (widget instanceof Text || widget instanceof Combo || widget instanceof CCombo) {
							String textValue = getRawText(element);
							Event ee = events.createEvent(element);
							ee.start = 0;
							ee.end = textValue.length();
							ee.text = textValue;
							ee.type = SWT.Verify;
							events.sendEvent(element, ee);
							events.sendEvent(element, SWT.Modify);
						}
					}
				}
			}
		});
	}

	public void traverse(final SWTUIElement element, final int code, final char character, final int times) {
		exec("traverse", new Runnable() {
			@Override
			public void run() {
				Widget widget = unwrapWidget(element);
				if (!(widget instanceof Control))
					return;

				for (int i = 0; i < times; i++) {
					Control control = (Control) widget;
					boolean traverseResult = false;
					boolean isShellProcessed = false;
					// send traverse event to control and ancestors up to Shell
					// while the traversal succeeded
					while (!traverseResult && !isShellProcessed && control != null) {
						if (!control.isDisposed()) {
							Event ee = new Event();
							if (code == SWT.TRAVERSE_TAB_NEXT) {
								ee.keyCode = SWT.TAB;
								ee.stateMask = 0;
							} else if (code == SWT.TRAVERSE_TAB_PREVIOUS) {
								ee.keyCode = SWT.TAB;
								ee.stateMask = SWT.SHIFT;
							}
							ee.widget = control;
							ee.time = (int) System.currentTimeMillis();
							ee.doit = true;
							ee.detail = code;

							try {
								traverseResult = control.traverse(code, ee);
							} catch (Exception e) {
								// In case of invalid method
								try {
									Method method = Control.class.getDeclaredMethod("traverse", Event.class);
									method.setAccessible(true);
									method.invoke(control, ee);
								} catch (Exception e1) {
									traverseResult = control.traverse(code);
								}
							}
							// method traverse(int, Event) since eclipse 3.6
							// traverseResult = control.traverse(code,
							// traverseEvent);
						}
						isShellProcessed = control instanceof Shell;
						if (!control.isDisposed()) {
							control = control.getParent();
						} else {
							control = null;
						}
					}
				}
			}
		});
	}

	// * The field is set from
	// org.eclipse.rcptt.tesla.workbench.aspects.WorkbenchAspect.
	//
	// * The field located here to break dependency loop
	// with org.eclipse.rcptt.tesla.workbench.aspects.
	//
	// * There is only one Workbench per Eclipse instance,
	// so for now that is pretty OK to have static field.
	public static volatile Boolean lastWorkbenchKeyboardPressResult;

	public void type(final SWTUIElement element, final int code, final int mask, final boolean fromDisplay,
			final char character, final int times) {
		exec("type", new Runnable() {
			@Override
			public void run() {
				switch (element.getKind().kind) {
				default:
					Widget widget = unwrapWidget(element);
					if (!(widget instanceof Control))
						break;
					if (fromDisplay) {
						// Necessary for setting focus to control
						try {
							ShellUtilsProvider.getShellUtils().forceActive(((Control) widget).getShell());
						} catch (CoreException e) {
							throw new RuntimeException(e);
						}
					}
					events.sendFocus(widget);

					lastWorkbenchKeyboardPressResult = null;
					boolean doModify = false;

					if (widget instanceof CCombo) {
						CCombo combo = (CCombo) widget;
						Text text = getCComboText(combo);
						for (int i = 0; i < times; i++) {
							sendKeyEvent(code, mask, fromDisplay, character, text);
						}
					} else {
						for (int i = 0; i < times; i++) {
							sendKeyEvent(code, mask, fromDisplay, character, widget);
							doModify |= lastWorkbenchKeyboardPressResult != null && !lastWorkbenchKeyboardPressResult;
						}
					}
					if (widget instanceof Text) {
						if (doModify)
							events.sendEvent(element, SWT.Modify);

						if (code == 13 && mask == 0)
							events.sendEvent(element, SWT.DefaultSelection);
					}
					// events.sendEvent(element, SWT.Deactivate);
					// events.sendEvent(element, SWT.FocusOut);
					break;
				}
			}

			private void sendKeyEvent(final int code, final int mask, final boolean fromDisplay, final char character,
					Widget widget) {

				if (widget.isDisposed())
					return;

				if (fromDisplay) {
					KeyStroke[] keys = new KeyStroke[] { KeyStroke.getInstance(mask, code) };
					keyboard.typeKeys(widget, fromDisplay, keys);
				} else {
					Event createEvent = keyboard.createEvent(code, mask, character);
					createEvent.widget = widget;
					updateStyledTextPos(widget);
					events.sendEventRaw(SWT.KeyDown, createEvent);
					if (widget.isDisposed())
						return;

					events.sendEventRaw(SWT.KeyUp, createEvent);
				}
			}
		});
	}

	public void typeAction(final SWTUIElement element, final String actionId) {
		exec("typeAction", new Runnable() {
			@Override
			public void run() {
				@SuppressWarnings("cast") // IServiceLocator.getService was not
											// generic in Eclipse 4.4 and older.
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
						.getService(IHandlerService.class);
				try {
					handlerService.executeCommand(actionId, null);
				} catch (Exception e) {
					TeslaCore.log(e);
				}
			}
		});
	}

	protected Text getCComboText(CCombo combo) {
		return TeslaSWTAccess.getCComboText(combo);
	}

	public void clickAndWait(SWTUIElement w) {
		click(w);
	}

	private void shutdown() {
		Job.getJobManager().removeJobChangeListener(collector);
		BrowserManager.getInstance().clear();
		TeslaTimerExecManager.getManager().removeEventListener(timerListener);
	}

	public void waitFinish() {
		// TODO Auto-generated method stub
	}

	public void save(final SWTUIElement w) {
		exec("save", new Runnable() {

			@Override
			public void run() {
				if (w.getKind().kind == ElementKind.Editor) {
					IEditorReference editor = (IEditorReference) (((WorkbenchUIElement) w).reference);
					IEditorPart editorPart = editor.getEditor(false);
					if (editorPart != null) {
						editorPart.doSave(new NullProgressMonitor());
					}
				}

			}
		});

	}

	public boolean isDirty(final SWTUIElement w) {
		if (w.getKind().kind == ElementKind.Editor) {
			IEditorReference editor = (IEditorReference) (((WorkbenchUIElement) w).reference);
			return editor.isDirty();
		}
		return false;
	}

	private static Widget parentFromExtension(Widget current) {
		for (ISWTUIPlayerExtension ext : getExtensions()) {
			Widget result = ext.getIndirectParent(current);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static List<Widget> collectParents(Widget widget, Map<Control, SWTUIElement> references, Widget... stopAt) {
		List<Widget> parents = new ArrayList<Widget>();
		if (widget == null || widget.isDisposed()) {
			return parents;
		}
		Widget current = widget;
		Widget prev = null;
		while (current != null) {
			prev = current;
			Widget indirectParent = parentFromExtension(current);
			if (indirectParent != null) {
				current = indirectParent;
			} else if (current instanceof Control) {
				if (current instanceof ToolBar) {
					/*
					 * Check work view/editor toolbars, they have different
					 * parent
					 */
					if (references != null && references.containsKey(current)) {
						current = references.get(current).unwrap();
					} else {
						current = ((Control) current).getParent();
					}
				} else {
					current = ((Control) current).getParent();
				}
			} else if (current instanceof TreeItem) {
				current = ((TreeItem) current).getParent();
			} else if (current instanceof TableItem) {
				current = ((TableItem) current).getParent();
			} else if (current instanceof MenuItem) {
				current = ((MenuItem) current).getParent();
			} else if (current instanceof ToolItem) {
				current = ((ToolItem) current).getParent();
			} else if (current instanceof Menu) {
				Menu parentMenu = ((Menu) current).getParentMenu();
				if (parentMenu == null) {
					current = ((Menu) current).getParent();
				} else {
					current = parentMenu;
				}
			} else if (current instanceof ScrollBar) {
				current = ((ScrollBar) current).getParent();
			} else if (current instanceof ToolTip) {
				current = ((ToolTip) current).getParent();
			} else {
				current = null;
			}
			if (current != null) {
				if (current instanceof CoolBar) {
					CoolBar bar = (CoolBar) current;
					CoolItem[] items = bar.getItems();
					for (CoolItem coolItem : items) {
						if (coolItem != null) {
							Control control = coolItem.getControl();
							if (control != null && control.equals(prev)) {
								parents.add(coolItem);
							}
						}
					}
				}
				if (stopAt != null) {
					boolean breakRequired = false;
					for (Widget w : stopAt) {
						if (w.equals(current)) {
							breakRequired = true;
							break;
						}
					}
					if (breakRequired) {
						break;
					}
				}
				parents.add(current);
			}
		}
		return parents;
	}

	public String getSelectedTabItem(SWTUIElement swtuiElement) {
		Widget w = unwrapWidget(swtuiElement);
		if (w instanceof CTabFolder) {
			CTabFolder tf = (CTabFolder) w;
			CTabItem item = tf.getSelection();
			return item.getText();
		} else if (w instanceof TabFolder) {
			TabFolder tf = (TabFolder) w;
			TabItem[] selection = tf.getSelection();
			if (selection.length > 0) {
				return selection[0].getText();
			}
		}
		return null;
	}

	public static boolean isVisible(SWTUIElement swtuiElement) {
		Widget widget = unwrapWidget(swtuiElement);
		if (widget instanceof Control) {
			return ((Control) widget).isVisible();
		}
		return true;
	}

	public static SWTUIElement getShell(SWTUIElement element) {

		for (ISWTUIPlayerExtension ext : getExtensions()) {
			SWTUIElement result = ext.getShell(element);
			if (result != null) {
				return result;
			}
		}

		//

		Widget widget = unwrapWidget(element);

		// PartPane.control == null after disposal in case of WorkbenchUIElement
		if (widget == null || widget.isDisposed()) {
			return null;
		}

		SWTUIPlayer player = element.getPlayer();
		if (widget instanceof Control) {
			return player.wrap(((Control) widget).getShell());
		} else if (widget instanceof MenuItem) {
			return player.wrap(((MenuItem) widget).getParent().getShell());
		} else if (widget instanceof ToolItem) {
			return player.wrap(((ToolItem) widget).getParent().getShell());
		} else if (widget instanceof TreeItem) {
			return player.wrap(((TreeItem) widget).getParent().getShell());
		} else if (widget instanceof TableItem) {
			return player.wrap(((TableItem) widget).getParent().getShell());
		} else if (widget instanceof CTabItem) {
			return player.wrap(((CTabItem) widget).getParent().getShell());
		} else if (widget instanceof CoolItem) {
			return player.wrap(((CoolItem) widget).getParent().getShell());
		} else if (widget instanceof TabItem) {
			return player.wrap(((TabItem) widget).getParent().getShell());
		} else if (widget instanceof TreeColumn) {
			return player.wrap(((TreeColumn) widget).getParent().getShell());
		} else if (widget instanceof TableColumn) {
			return player.wrap(((TableColumn) widget).getParent().getShell());
		} else if (widget instanceof Item) {
			return player.wrap(((Item) widget).getDisplay().getActiveShell());
		} else if (widget instanceof ScrollBar) {
			return player.wrap(((ScrollBar) widget).getParent().getShell());
		}
		return null;
	}

	public void addMouseWidgetInfo(final Widget canvas, int x, int y) {
		TeslaEventManager.getManager().setLastWidget(canvas, x, y);
		widgetToMouseForMenus.put(canvas, new Point(x, y));
		canvas.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				widgetToMouseForMenus.remove(canvas);
				try {
					canvas.removeDisposeListener(this);
				} catch (Throwable e2) {
				}
			}
		});
	}

	public SWTEvents getEvents() {
		return events;
	}

	public List<SWTUIElement> getParentsList(SWTUIElement swtuiElement) {
		Map<Control, SWTUIElement> references = EclipseWorkbenchProvider.getProvider().getWorkbenchReference(this);
		List<Widget> parents = collectParents(unwrapWidget(swtuiElement), references);
		List<SWTUIElement> elements = new ArrayList<SWTUIElement>();
		for (Widget widget : parents) {
			SWTUIElement e = null;
			if (references.containsKey(widget)) {
				e = references.get(widget);
			} else {
				e = wrap(widget);
			}
			if (e != null) {
				GenericElementKind kind = e.getKind();
				if (kind.is(ElementKind.Any) || kind.is(ElementKind.Unknown) || kind.is(ElementKind.Toolbar)
						|| kind.is(ElementKind.CoolBar) || kind.is(ElementKind.CBanner)
						|| kind.is(ElementKind.TabFolder) || kind.is(ElementKind.Canvas)
						|| kind.is(ElementKind.Combo)) {
					continue;
				}
				if (isVisible(e))
					elements.add(e);
			}
		}
		return elements;
	}

	public UIJobCollector getCollector() {
		return collector;
	}

	public SWTUIElement getParentElement(SWTUIElement uiElement) {
		List<SWTUIElement> parentsList = getParentsList(uiElement);
		if (parentsList.size() > 0) {
			return parentsList.get(0);
		}
		return null;
	}

	public BrowserManager getBrowserManager() {
		return BrowserManager.getInstance();
	}

	private void updateStyledTextPos(Widget widget) {
		if (widget instanceof StyledText) {
			// Update viewers based on textViewer about styled
			// text position. So popups will not be hided on
			// update
			StyledText t = (StyledText) widget;
			int topPixel = t.getTopPixel();
			Viewer viewer = TeslaSWTAccess.getViewer(t);
			if (viewer != null) {
				JFaceTextSupport.setLastPixels(viewer, topPixel);
			}
		}
	}

	public static byte[] captureControlImage(Control ctrl, Rectangle subImageBounds) {
		// selectionShell.setVisible(false);
		// Rectangle selBounds = selectionShell.getBounds();
		Image image = captureControlImageRaw(ctrl, subImageBounds);
		ImageData data = image.getImageData();
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { data };
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		loader.save(stream, SWT.IMAGE_PNG);
		image.dispose();
		return stream.toByteArray();
	}

	public static Image captureControlImageRaw(Control ctrl, Rectangle subImageBounds) {
		ctrl.getShell().setActive();
		if (ctrl instanceof Label) {
			Image image = ((Label) ctrl).getImage();
			if (image != null) {
				return new Image(image.getDevice(), image, SWT.IMAGE_COPY);
			}
		}
		GC gc = new GC(ctrl);
		final Rectangle bounds = ctrl.getBounds();
		Image image = null;
		if (!bounds.equals(subImageBounds)) {
			image = new Image(ctrl.getDisplay(), subImageBounds.width, subImageBounds.height);
			gc.copyArea(image, subImageBounds.x, subImageBounds.y);
			gc.dispose();
		} else {
			image = new Image(ctrl.getDisplay(), bounds.width, bounds.height);
			gc.copyArea(image, 0, 0);
			ScreenshotSupport.saveImage(image.getImageData(), "");
			gc.dispose();
		}
		return image;
	}

	public static Image copyImagePart(Image img, Rectangle subImageBounds) {
		GC gc = new GC(img);
		final Rectangle bounds = img.getBounds();
		Image image = null;
		if (!bounds.equals(subImageBounds)) {
			image = new Image(img.getDevice(), subImageBounds.width, subImageBounds.height);
			gc.copyArea(image, subImageBounds.x, subImageBounds.y);
			gc.dispose();
		} else {
			gc.dispose();
			image = img;
		}
		ScreenshotSupport.saveImage(image.getImageData(), "copy_img");
		return image;
	}

	public static byte[] captureControlImage(Image img) {
		ImageData data = img.getImageData();
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { data };
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		loader.save(stream, SWT.IMAGE_PNG);
		img.dispose();
		return stream.toByteArray();
	}

	public static Image prepateImageForOCR(byte[] image, int x, int y, int width, int height) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Image img = new Image(display, new ByteArrayInputStream(image));
		ScreenshotSupport.saveImage(img.getImageData(), "ocr");
		Image scaled = prepareImageForOCR(x, y, width, height, img);
		ScreenshotSupport.saveImage(scaled.getImageData(), "ocr_scaled");
		img.dispose();
		return scaled;
	}

	public static Image prepareImageForOCR(int x, int y, int width, int height, Image img) {
		int mult = 2;
		Image scaled = new Image(img.getDevice(), width * mult, height * mult);
		GC gc = new GC(scaled);
		gc.drawImage(img, x, y, width, height, 0, 0, width * mult, height * mult);
		ScreenshotSupport.saveImage(scaled.getImageData(), "ocr_part");
		gc.dispose();
		return scaled;
	}

	public static void disableMessageDialogs() {
		SWTDialogManager.setCancelMessageBoxesDisplay(true);
	}

	public static void enableMessageDialogs() {
		SWTDialogManager.setCancelMessageBoxesDisplay(false);
	}

	public synchronized static SWTUIPlayer getPlayer(Display display) {
		SWTUIPlayer p = players.get(display);
		if (p == null) {
			p = new SWTUIPlayer(display);
			players.put(display, p);
		}
		return p;
	}

	public synchronized static SWTUIPlayer getPlayer() {
		return SWTUIPlayer.getPlayer(PlatformUI.getWorkbench().getDisplay());
	}

	public synchronized static void shutdown(SWTUIPlayer internalPlayer) {
		if (internalPlayer != null) {
			players.remove(internalPlayer.getDisplay());
			internalPlayer.shutdown();
		}
	}

	public Display getDisplay() {
		return display;
	}

	public void minimize(SWTUIElement uiElement) {
		final Widget widget = unwrapWidget(uiElement);
		exec("minimize", new Runnable() {
			@Override
			public void run() {
				processTabFolderButton(widget, IWorkbenchPage.STATE_MINIMIZED);
			}
		});
	}

	public void maximize(SWTUIElement uiElement) {
		final Widget widget = unwrapWidget(uiElement);

		exec("maximize", new Runnable() {
			@Override
			public void run() {
				if (widget instanceof Shell) {
					((Shell) widget).setMaximized(true);
					try {
						ShellUtilsProvider.getShellUtils().forceActive((Shell) widget);
					} catch (CoreException e) {
						throw new RuntimeException(e);
					}
				}
				processTabFolderButton(widget, IWorkbenchPage.STATE_MAXIMIZED);
			}
		});
	}

	public void restore(SWTUIElement uiElement) {
		final Widget widget = unwrapWidget(uiElement);
		exec("restore", new Runnable() {
			@Override
			public void run() {
				processTabFolderButton(widget, IWorkbenchPage.STATE_RESTORED);
			}
		});
	}

	private void processTabFolderButton(Widget widget, int buttonId) {
		EclipseWorkbenchProvider.getProvider().processTabFolderButton(widget, buttonId);
	}

	public void showTabList(SWTUIElement uiElement) {
		final Widget widget = unwrapWidget(uiElement);
		exec("showTabList", new Runnable() {
			@Override
			public void run() {
				processTabShowList(widget);
			}
		});
	}

	private void processTabShowList(Widget widget) {
		EclipseWorkbenchProvider.getProvider().processTabShowList(widget);
	}

	public void setPerspective(final String perspectiveId) {
		exec("setPerspective", new Runnable() {
			@Override
			public void run() {
				IPerspectiveDescriptor persectiveDescriptor = PlatformUI.getWorkbench().getPerspectiveRegistry()
						.findPerspectiveWithId(perspectiveId);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.setPerspective(persectiveDescriptor);
			}
		});
	}

	public void wakeup() {
		notifyUI(getDisplay());
	}

	public final class ExecRunnable implements Runnable {
		private final Context currentContext;
		private final Runnable runnable;
		private final String errorMethod;
		private String msg;

		private ExecRunnable(String msg, Context currentContext, Runnable runnable, String errorMethod) {
			this.msg = msg;
			this.currentContext = currentContext;
			this.runnable = runnable;
			this.errorMethod = errorMethod;
		}

		@Override
		public String toString() {
			return errorMethod;
		}

		@Override
		public void run() {
			// ReportBuilder builder = ReportManager.getBuilder();
			// if (builder != null) {
			// builder.beginTask("Running raw command:" + msg);
			// }
			collector.enable();
			try {
				runnable.run();
				// Wait for all jobs
				display.wake();
			} catch (Throwable t) {
				if (error != null)
					error = t;
				TeslaCore.log(new TeslaExecutionFailedException(errorMethod, t));
			} finally {
				// if (builder != null) {
				// builder.endTask();
				// }
				// collector.setNeedDisable();
				// Will be called from next sleep state.
				synchronized (runnables) {
					if (currentContext != null) {
						List<Runnable> runs = runnables.get(currentContext);
						runs.remove(this);
					}
				}
			}
		}
	}

	private static class NotifyUINullRunnable implements Runnable {
		@Override
		public void run() {
		}
	};

	private static NotifyUINullRunnable notifyUINullRunnable;

	public static void notifyUI(Display display) {
		display.wake();
		display.asyncExec(notifyUINullRunnable);
	}

	public static synchronized void addExtension(ISWTUIPlayerExtension extension) {
		extensions.add(extension);
	}

	public static synchronized void removeExtension(ISWTUIPlayerExtension extension) {
		extensions.remove(extension);
	}

	public static synchronized List<ISWTUIPlayerExtension> getExtensions() {
		return new ArrayList<ISWTUIPlayerExtension>(extensions);
	}

	public boolean cleanMenus(final Q7WaitInfoRoot info) {
		final boolean result[] = { false };

		Display curDisplay = PlatformUI.getWorkbench().getDisplay();
		if (curDisplay == null || curDisplay.isDisposed()) {
			return false;
		}
		final List<Menu> menusToProceed = new ArrayList<>();

		synchronized (shownMenus) {
			for (WeakReference<Menu> weakReference : shownMenus) {
				Menu menu = weakReference.get();
				if (menu == null) {
					continue;
				}
				menusToProceed.add(menu);
			}
			shownMenus.clear();
		}
		if (!menusToProceed.isEmpty()) {
			Q7WaitUtils.updateInfo("menu", "hide", info);
			curDisplay.syncExec(new Runnable() {
				@Override
				public void run() {
					for (Menu menu : menusToProceed) {
						// We also need to hide all parent menus.
						while (menu != null && !menu.isDisposed()) {
							events.sendEvent(menu, SWT.Hide);
							menu = menu.getParentMenu();
						}

					}
				}
			});
		}
		synchronized (shownMenus) {
			result[0] = !shownMenus.isEmpty();
		}
		return result[0];
	}

	public void clean() {
		getCollector().clean();
		getBrowserManager().clear();
		cleanMenus(null);
		error = null;
	}

	public boolean isCollectable(SWTUIElement element, Class<?>[] classes) {
		if (element == null)
			return false;

		if (classes == null)
			return true;

		for (ISWTUIPlayerExtension extension : getExtensions()) {
			if (extension.isCollectable(element, classes)) {
				return true;
			}
		}

		return false;
	}

	private static void debugProceed(String message) {
		if (DEBUG_PROCEED) {
			System.out.println("SWTUIPlayer: " + message);
			System.out.flush();
		}
	}
}
