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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.rcptt.logging.IQ7ActivityLogs;
import org.eclipse.rcptt.logging.Q7LoggingManager;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.features.IMLFeatures;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolFactory;
import org.eclipse.rcptt.tesla.core.protocol.Type;
import org.eclipse.rcptt.tesla.core.protocol.raw.AssertionFocus;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawFactory;
import org.eclipse.rcptt.tesla.core.protocol.raw.SetMode;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor;
import org.eclipse.rcptt.tesla.recording.aspects.IAssertSWTEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.ISkipAwareEventListener;
import org.eclipse.rcptt.tesla.recording.aspects.SWTEventManager;
import org.eclipse.rcptt.tesla.recording.aspects.WorkbenchEventManager;
import org.eclipse.rcptt.tesla.recording.core.IRecordingHelper;
import org.eclipse.rcptt.tesla.recording.core.IRecordingProcessor;
import org.eclipse.rcptt.tesla.recording.core.TeslaRecorder;
import org.eclipse.rcptt.tesla.recording.core.swt.util.RecordedEvent;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

public class SWTAssertManager implements IRecordingProcessor,
		IAssertSWTEventListener, ISkipAwareEventListener {

	private Set<String> widgetClasses = new HashSet<String>();

	private TeslaRecorder recorder;

	private IRecordingDescriber freezedCtrl;

	private Widget freezedCtrlWidget;
	private Point freezedPoint;

	private Shell selectionShell;
	private IRecordingDescriber lastFocusedWidget;

	private SWTUIPlayer player;

	private List<IRecordingDescriber> widgetsOnMove = new ArrayList<IRecordingDescriber>();

	private Set<Shell> menuShells = new HashSet<Shell>();

	private Composite bar;

	private Map<MenuItem, MenuItem> menuMap = new HashMap<MenuItem, MenuItem>();
	private List<Menu> menus = new ArrayList<Menu>();

	private Control beforeFreezeFocus = null;

	public void clear() {
		lastFocusedWidget = null;
		freezedCtrl = null;
	}

	public void initialize(TeslaRecorder teslaRecorder) {
		if (TeslaFeatures.isActivityLogging()) {
			Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS, "");
		}
		this.recorder = teslaRecorder;
		player = SWTRecordingHelper.getHelper().getPlayer();
		SWTEventManager.addListener(this);
	}

	public int getInitLevel() {
		return 300;
	};

	public void setFreeze(final boolean value, SetMode command) {
		widgetClasses.clear();
		if (command != null)
			widgetClasses.addAll(command.getWidgetClasses());

		if (TeslaFeatures.isActivityLogging()) {
			Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
					"set freeze mode to: " + value);
		}
		final Display display = PlatformUI.getWorkbench().getDisplay();
		boolean oldValue = SWTEventManager.getFreeze();
		if (display.isDisposed()) {
			SWTEventManager.setFreeze(false);
			WorkbenchEventManager.setFreeze(false);
			return;
		}
		SWTEventManager.setIgnoreFreeze(player.getIgnored());
		SWTEventManager.setFreeze(value);
		WorkbenchEventManager.setFreeze(value);
		if (value == false) {
			display.asyncExec(new Runnable() {
				public void run() {
					callCreateHover(value, null);
					disposeMenuPopups();
					clearPopupMenus();
				}

			});
		}
		if (value != oldValue) {
			if (value == false && !display.isDisposed()) {
				display.asyncExec(new Runnable() {
					public void run() {
						Shell shell = display.getActiveShell();
						if (shell != null) {
							try {
								ShellUtilsProvider.getShellUtils().forceActive(shell);
							} catch (CoreException e) {
								throw new RuntimeException(e);
							}
							if (beforeFreezeFocus != null) {
								// beforeFreezeFocus.forceFocus();
								// beforeFreezeFocus = null;
								Shell[] shells = display.getShells();
								for (Shell s : shells) {
									String creationMethod = TeslaEventManager
											.getManager().getShellCreationMethod(s);
									if ("AbstractTableInformationControl.<init>()"
											.equals(creationMethod)) {
										try {
											ShellUtilsProvider.getShellUtils().forceActive(s);
										} catch (CoreException e) {
											throw new RuntimeException(e);
										}
										s.forceFocus();
										// new SWTEvents(display).sendEvent(s,
										// SWT.Activate);
									}
								}
							}
						}
					}
				});
			} else {
				display.asyncExec(new Runnable() {
					public void run() {
						beforeFreezeFocus = display.getFocusControl();
					}
				});
			}
		}
		if (value == false) {
			freezedCtrl = null;
			lastFocusedWidget = null;
		}
	}

	private void disposeMenuPopups() {
		if (TeslaFeatures.isActivityLogging()) {
			Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
					"dispose popup menus");
		}
		for (Shell menuShell : menuShells) {
			if (menuShell != null) {
				menuShell.dispose();
			}
		}
		menuShells.clear();
	}

	private Widget getChild(Widget parent, int x, int y) {
		if (!(parent instanceof Control)) {
			return parent;
		}
		Control control = (Control) parent;

		int relX = x - control.getBounds().x;
		int relY = y - control.getBounds().y;

		if (!(parent instanceof Composite)) {
			return parent;
		}
		Composite c = (Composite) parent;
		for (Control ctrl : c.getChildren()) {
			if (ctrl.getBounds().contains(relX, relY) && ctrl.isVisible()) {
				return getChild(ctrl, relX, relY);
			}
		}
		return parent;
	}

	public synchronized boolean handleEventInFreeze(Widget widget, int type,
			Event event) {
		if (TeslaFeatures.isActivityLogging()) {
			try {
				RecordedEvent toRecording = new RecordedEvent(
						recorder.getProcessor(SWTUIProcessor.class).getPlayer(),
						event, type, widget);
				Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS
						+ ".events", "event: " + toRecording.toString());
			} catch (Throwable e) {
				// ignore
			}
		}
		try {
			// Process events as usual then we are here
			SWTEventManager.setShouldProceed(true);
			// if( widget instanceof Menu && type == SWT.Show) {
			// // enable all menu items
			// player.getEvents().sendEvent((Menu)widget, SWT.Show);
			// ((Menu)widget).setVisible(true);
			// return false;
			// }

			// Send this command to recorder as a signal to switch mode
			// send request after KeyUp event (if sending after KeyDown, KeyUp
			// recorded as key-type into test case)
			if (type == SWT.KeyUp && event != null) {
				if (isShortcutRequest(event, recorder.getRecordModeShortcuts())) {
					if (TeslaFeatures.isActivityLogging()) {
						Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
								"send record mode request");
					}
					recorder.executeCommand(ProtocolFactory.eINSTANCE
							.createRecordingModeRequest());
				}
				if (isShortcutRequest(event, recorder.getStopRecordShortcuts())) {
					if (TeslaFeatures.isActivityLogging()) {
						Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
								"send stop record request");
					}
					Type stopType = ProtocolFactory.eINSTANCE.createType();
					stopType.setState(event.stateMask);
					stopType.setCharacter(event.character);
					stopType.setCode(event.keyCode);
					recorder.executeCommand(stopType);
				}
			}
			if (widget instanceof Control) {
				Control ctrl = (Control) widget;
				if (selectionShell != null) {
					if (ctrl.getShell().equals(selectionShell)) {
						if (Platform.getOS().equals(Platform.OS_MACOSX)
								&& lastFocusedWidget != null
								&& lastFocusedWidget.getWidget() != null
								&& type == SWT.MouseMove) {

							widget = getChild(lastFocusedWidget.getWidget(),
									event.x, event.y);
						} else {
							return true;
						}
					}
				}

				for (Shell menuShell : menuShells) {
					if (ctrl.getShell().equals(menuShell)) {
						return true;
					}
				}
			}
			if (widget instanceof Control) {
				if (!((Control) widget).isVisible()) {
					return false;
				}
			}
			if (event == null) {
				event = new Event();
			}

			callCreateHover(true, getShell(widget));
			if (widget instanceof MenuItem) {
				if (menuMap.containsKey(widget)) {
					widget = menuMap.get(widget);
				}
			}

			IRecordingDescriber assertDescr = selectAllowedParent(RecordingDescriberManager
					.getDescriber(widget, event.x, event.y, true));
			if (type == SWT.Selection && widget instanceof MenuItem) {
				if (TeslaFeatures.isActivityLogging()) {
					Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
							"assertion target locked for menu item: "
									+ ((MenuItem) widget).getText());
				}
				seachForElement(assertDescr.searchForElement(recorder), true,
						assertDescr);
				freezedCtrl = assertDescr;
			}
			if (type == SWT.KeyDown) {
				if (freezedCtrl != null) {
					switch (event.keyCode) {
					case SWT.ARROW_UP:
						if (TeslaFeatures.isActivityLogging()) {
							Q7LoggingManager
									.logMessage(IQ7ActivityLogs.ASSERTIONS,
											"switch assertion target lock to control parent");
						}
						freezedCtrl = freezedCtrl.getParent();
						updateHoverAccordingTo(freezedCtrl, freezedPoint.x,
								freezedPoint.y);
						break;
					case SWT.ARROW_DOWN:
						if (TeslaFeatures.isActivityLogging()) {
							Q7LoggingManager.logMessage(
									IQ7ActivityLogs.ASSERTIONS,
									"switch assertion target lock to child");
						}
						IRecordingDescriber child = RecordingDescriberManager
								.getDescriber(freezedCtrlWidget,
										freezedPoint.x, freezedPoint.y, true);
						if (!child.equals(freezedCtrl)) {
							while (true) {
								IRecordingDescriber parent = child.getParent();
								if (freezedCtrl.equals(parent)) {
									freezedCtrl = child;
									updateHoverAccordingTo(freezedCtrl,
											freezedPoint.x, freezedPoint.y);
									break;
								}
								if (parent.equals(child)) {
									freezedCtrl = child;
									updateHoverAccordingTo(freezedCtrl,
											freezedPoint.x, freezedPoint.y);
									break;
								}
								child = parent;
							}
						}
						break;
					}
				}
			}
			if (type == SWT.MouseUp) {
				for (Shell menuShell : menuShells) {
					if (menuShell != null) {
						if (widget instanceof Control
								&& !((Control) widget).getShell().equals(
										menuShell)) {
							menuShells.remove(menuShell);
							menuShell.dispose();
							// return false;
						}
					}
				}
				if (event.button == 1) {
					if (freezedCtrl == null) {
						if (TeslaFeatures.isActivityLogging()) {
							Q7LoggingManager.logMessage(
									IQ7ActivityLogs.ASSERTIONS,
									"assertion target locked");
						}
						freezedCtrl = assertDescr;
						freezedPoint = new Point(event.x, event.y);
						freezedCtrlWidget = widget;
						updateHoverAccordingTo(freezedCtrl, event.x, event.y);
					} else {
						if (TeslaFeatures.isActivityLogging()) {
							Q7LoggingManager.logMessage(
									IQ7ActivityLogs.ASSERTIONS,
									"unlock assertion target");
						}
						IRecordingDescriber lastFreezed = freezedCtrl;
						freezedCtrl = null;
						updateHoverAccordingTo(assertDescr, event.x, event.y);

						if (lastFreezed != null) {
							lastFreezed.redraw();
						}
					}
				} else if (event.button == 3) {
					// try to freeze control

					if (widget instanceof Control) {
						if (TeslaFeatures.isActivityLogging()) {
							Q7LoggingManager.logMessage(
									IQ7ActivityLogs.ASSERTIONS,
									"show popup menu");
						}
						showPopupMenu(event, (Control) widget, assertDescr);
					}
				}
			}
			if ((type == SWT.MouseMove || type == SWT.MouseEnter
					|| type == SWT.MouseExit || type == SWT.Arm || type == SWT.MeasureItem)
					&& freezedCtrl == null) {
				synchronized (widgetsOnMove) {
					widgetsOnMove.add(assertDescr);
				}
				final Event fevent = event;
				if (widget != null) {
					widget.getDisplay().timerExec(150, new Runnable() {
						public void run() {
							synchronized (widgetsOnMove) {
								if (widgetsOnMove.size() > 0) {
									if (freezedCtrl == null) {
										IRecordingDescriber lastWidget = widgetsOnMove
												.get(widgetsOnMove.size() - 1);
										if (!lastWidget
												.equals(lastFocusedWidget)
												|| !selectionShell.isVisible()) {
											Widget w = lastWidget.getWidget();
											if (!w.isDisposed()) {
												updateHoverAccordingTo(
														lastWidget, fevent.x,
														fevent.y);
											}
										}
										lastFocusedWidget = lastWidget;
									}
									widgetsOnMove.clear();
								}
							}
						}
					});
				}
			}
			if (type == SWT.MouseDown || type == SWT.MouseHover
					|| type == SWT.MouseMove || type == SWT.MouseUp
					|| type == SWT.KeyDown || type == SWT.KeyUp
					|| type == SWT.Selection || type == SWT.MouseDoubleClick
					|| type == SWT.MouseWheel || type == 38/*
															 * SWT.
															 * MouseVerticalWheel
															 */
					|| type == SWT.Deactivate || type == SWT.FocusIn
					|| type == SWT.DefaultSelection || type == SWT.Activate
					|| type == SWT.Expand || type == SWT.Collapse
					|| type == SWT.Iconify || type == SWT.Deiconify
					|| type == SWT.Deactivate || type == SWT.Modify
					|| type == SWT.MenuDetect || type == SWT.MouseEnter
					|| type == SWT.MouseExit || type == SWT.FocusOut
					|| type == SWT.Traverse) {
				return false;
			}
			if (type == SWT.MouseMove && widget instanceof Canvas) {
				return false;
			}
			return true;
		} finally {
			SWTEventManager.setShouldProceed(false);
		}
	}


	public boolean isShortcutRequest(Event e, String[] shortcuts) {
		if (shortcuts == null) {
			return false;
		}
		int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
		KeySequence sequence = KeySequence.getInstance(SWTKeySupport
				.convertAcceleratorToKeyStroke(accelerator));
		if (shortcuts != null) {
			for (String formatted : shortcuts) {
				try {
					KeySequence shortcut = KeySequence.getInstance(formatted);
					if (sequence.equals(shortcut)) {
						return true;
					}
				} catch (ParseException e1) {
					TeslaCore.log("Invalid shortcut: " + formatted);
				}
			}
		}
		return false;
	}

	private void showPopupMenu(Event event, Control c,
			IRecordingDescriber describer) {
		if (TeslaFeatures.isActivityLogging()) {
			Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
					"show popup menu for: " + c.getClass().toString());
		}
		Menu menu = null;
		player.getEvents().sendEvent(c, SWT.MenuDetect, event.x, event.y,
				SWT.BUTTON2);
		menu = c.getMenu();
		if (menu != null && !menu.isDisposed()) {
			player.getEvents().sendEvent(menu, SWT.Show);
			// menu.setVisible(true);
			Menu popupMenu = new Menu(getShell(c), SWT.POP_UP);
			clearPopupMenus();
			copyMenuTo(menu, popupMenu);
			menus.add(popupMenu);
			popupMenu.setVisible(true);

			// showMenuAt(event, c, menu);
		}
	}

	private void clearPopupMenus() {
		for (MenuItem item : menuMap.keySet()) {
			if (!item.isDisposed()) {
				item.dispose();
			}
		}
		menuMap.clear();
		for (Menu menu : menus) {
			if (!menu.isDisposed()) {
				menu.dispose();
			}
		}
		menus.clear();

	}

	private void copyMenuTo(Menu menu, Menu popupMenu) {
		if (TeslaFeatures.isActivityLogging()) {
			Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
					"copy popup menu");
		}
		for (MenuItem i : menu.getItems()) {
			MenuItem copy = new MenuItem(popupMenu, i.getStyle());
			menuMap.put(copy, i);

			String text = "" + (i.getText());
			String text2 = "";
			if (text.contains("\t")) {
				int tabIndex = text.indexOf('\t');
				text2 = text.substring(tabIndex + 1);
				text = text.substring(0, tabIndex);
			}
			copy.setText(text + (!i.getEnabled() ? " (disabled)" : "") + "\t"
					+ text2);
			if (i.getImage() != null) {
				copy.setImage(new Image(i.getDisplay(), i.getImage(), i
						.getEnabled() ? SWT.IMAGE_COPY : SWT.IMAGE_DISABLE));
			}
			copy.setSelection(i.getSelection());
			if ((copy.getStyle() & SWT.CASCADE) != 0) {
				player.getEvents().sendEvent(i, SWT.Arm);
				if (i.getMenu() != null) {
					player.getEvents().sendEvent(i.getMenu(), SWT.Show);
					Menu menuCopy = new Menu(copy);
					copyMenuTo(i.getMenu(), menuCopy);
					copy.setMenu(menuCopy);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void showMenuAt(Event event, Control c, Menu menu) {
		Shell menuShell = new Shell(getShell(c), SWT.NO_TRIM | SWT.TOOL
				| SWT.MODELESS);
		menuShells.add(menuShell);
		menuShell.setLayout(new FillLayout());
		bar = new Composite(menuShell, SWT.BORDER);
		bar.setLayout(new GridLayout(1, false));
		Color color = c.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		bar.setBackground(color);
		menuShell.setBackground(color);
		for (MenuItem i : menu.getItems()) {
			if (i.getStyle() == SWT.SEPARATOR) {
				Label l = new Label(bar, SWT.SEPARATOR | SWT.HORIZONTAL);
				GridDataFactory.fillDefaults().grab(true, false).span(1, 1)
						.applyTo(l);
				continue;
			}
			Composite line = new Composite(bar, SWT.NONE);
			GridLayoutFactory.fillDefaults().numColumns(3).applyTo(line);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(line);

			Label l = new Label(line, SWT.NONE);
			Image img = i.getImage();
			if (img != null) {
				l.setImage(new Image(c.getDisplay(), img, SWT.IMAGE_COPY));
			}
			Label l2 = new Label(line, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(l2);
			String text = "" + (i.getText());
			String text2 = "";
			if (text.contains("\t")) {
				int tabIndex = text.indexOf('\t');
				text2 = text.substring(tabIndex + 1);
				text = text.substring(0, tabIndex);
			}
			l2.setText(text);
			Label l3 = new Label(line, SWT.NONE);
			l3.setText(text2);
			l.setEnabled(i.getEnabled());
			l2.setEnabled(i.getEnabled());
			line.setBackground(color);
			l.setBackground(color);
			l2.setBackground(color);
			l3.setBackground(color);
		}
		Point pos = c.toDisplay(event.x, event.y);
		Point size = bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		menuShell.setBounds(pos.x, pos.y, size.x + 5, size.y + 5);
		// sh.setMenuBar(bar);
		menuShell.layout();
		menuShell.open();
	}

	private Shell getShell(Widget widget) {
		if (widget instanceof TreeItem) {
			return (((TreeItem) widget).getParent()).getShell();
		}
		if (widget instanceof TableItem) {
			return (((TableItem) widget).getParent()).getShell();
		}
		if (widget instanceof ToolItem) {
			return (((ToolItem) widget).getParent()).getShell();
		}
		if (widget instanceof CoolItem) {
			return (((CoolItem) widget).getParent()).getShell();
		}
		if (widget instanceof ScrollBar) {
			return ((ScrollBar) widget).getParent().getShell();
		}
		if (widget instanceof ToolTip) {
			return (((ToolTip) widget)).getParent();
		}
		if (widget instanceof Control) {
			return ((Control) widget).getShell();
		}
		if (widget instanceof Menu) {
			return ((Menu) widget).getShell();
		}
		if (widget instanceof MenuItem) {
			return ((MenuItem) widget).getParent().getShell();
		}

		return null;
	}

	private synchronized void updateHover(Rectangle bounds, Point point,
			boolean controlEquals, boolean inactive) {
		if (selectionShell == null
				|| selectionShell.isDisposed()
				|| (selectionShell.getParent() != null && selectionShell
						.getParent().isDisposed())) {
			selectionShell = null;
			freezedCtrl = null;
			return;
		}
		bounds.width += 6;
		bounds.height += 6;
		int px = point.x - 3;
		int py = point.y - 3;

		Rectangle displayBounds = selectionShell.getDisplay().getBounds();
		if (px + bounds.width > displayBounds.width) {
			bounds.width = displayBounds.width - px;
			if (bounds.width < 0) {
				bounds.width = 1;
			}
		}
		if (py + bounds.height > displayBounds.height) {
			bounds.height = displayBounds.height - py;
			if (bounds.height < 0) {
				bounds.height = 1;
			}
		}

		selectionShell.setBounds(px, py, bounds.width, bounds.height);
		Region region = new Region();

		int p0x = 7;
		int p0y = 7;
		int st = 2;

		region.add(0, 0, p0x, st);
		region.add(0, 0, st, p0x);

		region.add(bounds.width - p0x, 0, p0x, st);
		region.add(bounds.width - st, 0, st, p0y);

		region.add(bounds.width - p0x, bounds.height - st, p0x, st);
		region.add(bounds.width - st, bounds.height - p0y, 2, p0y);

		region.add(0, bounds.height - st, p0x, st);
		region.add(0, bounds.height - p0y, 2, p0y);

		if (controlEquals) {
			// p0x = bounds.width;
			// p0y = bounds.height;
			region.add(0, 0, bounds.width, 2);
			region.add(0, bounds.height - 2, bounds.width, 2);
			region.add(0, 0, 2, bounds.height);
			region.add(bounds.width - 2, 0, 2, bounds.height);
		}

		selectionShell.setBackground(selectionShell.getDisplay()
				.getSystemColor(!inactive ? SWT.COLOR_RED : SWT.COLOR_BLACK));
		// define the shape of the shell using setRegion
		Region oldRegion = selectionShell.getRegion();
		if (oldRegion != null && !oldRegion.isDisposed())
			oldRegion.dispose();
		selectionShell.setRegion(region);
		if (!selectionShell.isVisible()) {
			selectionShell.setVisible(true);
			// selectionShell.open();
		}
		selectionShell.redraw();
		// selectionShell.forceActive();
		selectionShell.moveAbove(null);
		selectionShell.setAlpha(127);
	}

	private synchronized void callCreateHover(final boolean value, Shell parent) {
		if (!menuShells.isEmpty()) {
			return;
		}
		if (selectionShell != null && !selectionShell.isDisposed()
				&& !selectionShell.getDisplay().equals(Display.getCurrent())) {
			selectionShell.getDisplay().syncExec(new Runnable() {
				public void run() {
					selectionShell.dispose();
				}
			});
			lastFocusedWidget = null;
			selectionShell = null;
			widgetsOnMove.clear();
		}
		if (selectionShell != null && selectionShell.isDisposed()) {
			selectionShell = null;
		} else {
			if (selectionShell != null) {
				Composite composite = selectionShell.getParent();
				if (composite != null && composite.isDisposed()) {
					selectionShell = null;
					freezedCtrl = null;
				}
			}
		}
		// if (selectionShell != null && value == true) {
		// Composite currentParent = selectionShell.getParent();
		// boolean reCreate = false;
		// if (currentParent != null) {
		// reCreate = !currentParent.equals(parent);
		// } else {
		// reCreate = (parent != null);
		// }
		// if (reCreate) {
		// selectionShell.dispose();
		// selectionShell = null;
		// }
		// }
		if (selectionShell == null && value == true) {
			// if (parent != null) {
			// selectionShell = new Shell(parent, SWT.NO_TRIM | SWT.ON_TOP |
			// SWT.TOOL);
			// } else {
			selectionShell = new Shell(SWT.NO_TRIM | SWT.ON_TOP | SWT.TOOL);
			// }

			selectionShell.setBackground(selectionShell.getDisplay()
					.getSystemColor(SWT.COLOR_RED));
			selectionShell.setText("Hover");
			// selectionShell.addMouseMoveListener(new MouseMoveListener() {
			// public void mouseMove(MouseEvent e) {
			// if (freezedCtrl != null && freezedCtrl instanceof Control) {
			// ((Control) freezedCtrl).getShell().setFocus();
			// }
			// }
			// });
		} else if (value == false) {
			if (selectionShell != null) {
				selectionShell.dispose();
				selectionShell = null;
			}
		}
	}

	private void updateHoverAccordingTo(IRecordingDescriber descr, int x, int y) {

		if (selectionShell == null) {
		} else {
			Rectangle rectangle = descr.getBounds();
			Point point = descr.getPoint();
			boolean fixed = descr.equals(freezedCtrl);
			if (point != null) {
				boolean isAllowedToSelect = isAllowedToSelect(descr);
				updateHover(rectangle, point, fixed, !isAllowedToSelect);
				if (isAllowedToSelect) {
					descr.setNeedLogging(fixed);
					seachForElement(descr.searchForElement(recorder), fixed, descr);
				}
			}
		}
	}

	private IRecordingDescriber selectAllowedParent(IRecordingDescriber widget) {
		if (widgetClasses.size() == 0) {
			return widget;
		}
		IRecordingDescriber descr = null;
		IRecordingDescriber parent = widget;
		do {
			if (isAllowedToSelect(parent)) {
				return parent;
			}
			descr = parent;
			parent = parent.getParent();
		} while (descr.getWidget() != parent.getWidget());
		return widget;
	}

	private boolean isAllowedToSelect(IRecordingDescriber descr) {
		if (widgetClasses.size() == 0)
			return true;
		return widgetClasses.contains(descr.getWidget().getClass().getCanonicalName());
	}

	private void seachForElement(Element element, boolean fixed,
			IRecordingDescriber descr) {
		if (element != null) {
			AssertionFocus focus = RawFactory.eINSTANCE.createAssertionFocus();
			// eclipse 3.4 compatibility:
			// EcoreUtil.copy raise exception if argument is null
			focus.setElement(EcoreUtil.copy(element));
			focus.setPointFixed(fixed);
			if (fixed) {
				if (TeslaFeatures.isActivityLogging()) {
					Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
							"assert properties of element: ");
				}
				if (TeslaFeatures.getInstance().isTrue(IMLFeatures.USE_IMAGING)) {
					Image img = descr.captureImage();
					if (img != null)
						focus.setImageCapture(SWTUIPlayer.captureControlImage(img));
				}
			}
			recorder.safeExecuteCommand(focus);
		}
	}

	public void resetAssertSelection() {
		if (TeslaFeatures.isActivityLogging()) {
			Q7LoggingManager.logMessage(IQ7ActivityLogs.ASSERTIONS,
					"reset assert target lock");
		}
		freezedCtrl = null;
		lastFocusedWidget = null;
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (selectionShell != null) {
					selectionShell.dispose();
					selectionShell = null;
				}
				selectionShell = new Shell(SWT.NO_TRIM | SWT.ON_TOP | SWT.TOOL);
				selectionShell.setBackground(selectionShell.getDisplay()
						.getSystemColor(SWT.COLOR_RED));
				selectionShell.setText("Hover");
				Shell copy = selectionShell;
				copy.addDisposeListener(ignored -> {
					Region region = copy.getRegion();
					if (region != null) {
						region.dispose();
					}
				});
				disposeMenuPopups();
				clearPopupMenus();
			}
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IRecordingHelper getHelper() {
		return null;
	}

	public boolean isSkipEvent(Widget widget, int type, Event event,
			boolean send) {
		if (event != null && type == SWT.KeyDown) {
			return isShortcutRequest(event, recorder.getAssertModeShortcuts());
		}
		return false;
	}
}
