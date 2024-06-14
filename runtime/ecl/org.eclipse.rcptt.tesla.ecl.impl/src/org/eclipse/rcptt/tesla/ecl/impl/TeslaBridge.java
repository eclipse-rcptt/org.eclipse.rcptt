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
package org.eclipse.rcptt.tesla.ecl.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.util.ISessionPropertyConstants;
import org.eclipse.rcptt.ecl.internal.core.AbstractRootSession;
import org.eclipse.rcptt.ecl.internal.core.AbstractSession;
import org.eclipse.rcptt.ecl.internal.core.CorePlugin;
import org.eclipse.rcptt.ecl.parser.EclCoreParser;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.ControlUIElement;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.UISelector;
import org.eclipse.rcptt.tesla.core.protocol.WindowUIElement;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.core.utils.AbstractFeatureManager.Option;
import org.eclipse.rcptt.tesla.ecl.TeslaErrorStatus;
import org.eclipse.rcptt.tesla.ecl.internal.impl.TeslaImplPlugin;
import org.eclipse.rcptt.tesla.ecl.model.BoundControlHandle;
import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.internal.core.queue.TeslaQClient;
import org.eclipse.rcptt.tesla.internal.core.queue.TeslaQPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.ReportScreenshotProvider;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor;
import org.eclipse.rcptt.tesla.swt.events.ITeslaEventListener;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager.HasEventKind;
import org.eclipse.rcptt.util.Base64;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

public class TeslaBridge {
	private static TeslaQPlayer player;
	private static TeslaQClient client;
	private static ITeslaEventListener listener;
	private static AdvancedInformation lastInfo;

	public synchronized static void setup() {
		lastInfo = null;
		noErrorMode = false;
		lastControlUIElement = null;
		if (player != null) {
			try {
				player.shutdown();
				player = null;
			} catch (Throwable e) {
				TeslaImplPlugin.err("Failed to deinialize player", e);
			}
		}
		if (client != null) {
			client.shutdown();
			client = null;
		}
		client = new TeslaQClient();
		// To be sure all info are clear before test
		try {
			client.clean();
		} catch (Throwable e) {
			TeslaImplPlugin.err("Failed to clean client", e);
		}
		player = new TeslaQPlayer(client) {
			@Override
			protected void notifyUI() {
				client.notifyUI();
			};
		};
		if (listener != null) {
			TeslaEventManager.getManager().removeEventListener(listener);
		}
		listener = new ITeslaEventListener() {
			public boolean doProcessing(Context context) {
				Q7WaitInfoRoot info = getCurrentWaitInfo(true);
				TeslaQClient clientTemp = client;
				if (clientTemp != null && clientTemp.processNext(context, info)) {
					return true;
				}
				return false;
			}

			public void hasEvent(HasEventKind kind, String run) {
				if (client != null) {
					Q7WaitInfoRoot info = getCurrentWaitInfo(false);
					client.hasEvent(kind.name(), run, info);
				}
			}
		};
		TeslaEventManager.getManager().addEventListener(listener);
	}

	public static Q7WaitInfoRoot getCurrentWaitInfo(final boolean tick) {
		return ReportHelper.getWaitInfo(ReportManager.getCurrentReportNode());
	}

	public static void waitDelay() throws CoreException {
		Option option = TeslaFeatures.getInstance().getOption(TeslaFeatures.COMMAND_EXECUTION_DELAY);
		int delay = Integer.parseInt(option.getValue());
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				throw new CoreException(new Status(Status.ERROR, TeslaImplPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
	}

	public synchronized static void shutdown() {
		// HandleStore.getStore().printInfo();
		if (client != null) {
			// Clean all info after test completion
			lastInfo = client.getLastFailureInformation();
			client.clean();
		}
		if (player != null) {
			player.shutdown();
		}
		if (client != null) {
			client.shutdown();
		}
		if (listener != null) {
			TeslaEventManager.getManager().removeEventListener(listener);
		}
		player = null;
		client = null;
		listener = null;
		eclipseWindow = null;
		lastControlUIElement = null;
	}

	public static Element find(ControlHandler handler) throws CoreException {
		return find(handler, null);
	}

	/**
	 * @param handler
	 * @return element or null
	 * @throws CoreException
	 */
	public static Element find(ControlHandler handler, IProcess process) throws CoreException {
		try {
			return findOneshot(handler, process);
		} catch (CoreException e) {
			for(ControlHandler parent = handler; parent != null; parent = parent.getParent() ) {
				parent.setResolved(null);
			}
			return findOneshot(handler, process);
		}
		
	}
	private static Element findOneshot(ControlHandler handler, IProcess process) throws CoreException {
		Assert.isNotNull(handler);

		// Check if resolved already
		if (handler.getResolved() != null) {
			return handler.getResolved();
		}

		if (handler instanceof BoundControlHandle) {
			final BoundControlHandle boundHandle = (BoundControlHandle) handler;
			final Widget widget = BoundControlHandleRuntime.get(boundHandle.getWidgetId());
			if (widget == null)
				throw new CoreException(TeslaImplPlugin.err("Unable to resolve bound widget by ID."));
			final SWTUIProcessor processor = getClient().getProcessor(SWTUIProcessor.class);
			final Element element = processor.getMapper()
					.get(new SWTUIElement(widget, SWTUIPlayer.getPlayer(widget.getDisplay())));

			widget.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					getClient().map(element, processor);
				}
			});

			handler.setResolved(element);
			return element;
		}

		// Setup kind
		ElementKind kind = handler.getKind();
		UISelector<ControlUIElement> selector;
		if (kind == ElementKind.Custom)
			selector = new UISelector<ControlUIElement>(handler.getCustomKindId(), getPlayer(), ControlUIElement.class);
		else
			selector = new UISelector<ControlUIElement>(kind, getPlayer(), ControlUIElement.class);

		// Setup parent
		ControlHandler parent = handler.getParent();
		if (parent != null)
			selector = selector.parent(find(parent, process));
		else
			selector = selector.parent(eclipseWindow());

		// Setup after
		ControlHandler after = handler.getAfter();
		if (after != null) {
			selector = selector.after(find(after, process));
		}

		// Setup path
		String path = handler.getPath();
		if (path != null)
			selector = selector.pathList(parsePath(path));

		// Setup classPattern AKA type
		String type = handler.getType();
		selector = selector.classPattern(type);

		// Setup indexes
		String indexes = handler.getIndexes();
		if (indexes != null)
			selector = selector.indexesList(parseIndexes(indexes));
		else if (kind == ElementKind.Item && handler.getRow() != null)
			selector = selector.indexesList(Arrays.asList(handler.getColumn(), handler.getRow()));

		if (ElementKind.Region.equals(kind)) {
			String rawImage = handler.getRawImage();
			byte[] image = Base64.decode(rawImage);
			selector = selector.image(image).indexes(handler.getX(), handler.getY());
		}
		// Search by text and index
		ControlUIElement control = selector.find(handler.getText(), handler.getIndex());

		if (control != null) {
			Element element = control.getElement();
			handler.setResolved(element);
			return element;
		}

		// Error handling
		TeslaErrorStatus teslaFailure = getTeslaFailure();
		if (teslaFailure != null) {
			if (selector.getParent() != null) {
				lastControlUIElement = new ControlUIElement(selector.getParent(), getPlayer());
			}
			if (isAllowScreenshotOnError(process)) {
				TeslaBridge.makeScreenshot(true, teslaFailure.getMessage());
			}
			throw new CoreException(teslaFailure);
		}
		String kindName = kind.name().toLowerCase();
		if ("text".equals(kindName))
			kindName = "editbox";
		StringBuilder message = new StringBuilder("Cannot find ");
		message.append(kindName);
		if (handler.getText() != null)
			message.append(" \"").append(handler.getText()).append("\"");
		else if (handler.getPath() != null)
			message.append(" \"").append(handler.getPath()).append("\"");
		if (selector.getParent() != null) {
			lastControlUIElement = new ControlUIElement(selector.getParent(), getPlayer());
		}
		if (isAllowScreenshotOnError(process)) {
			TeslaBridge.makeScreenshot(true, message.toString());
		}
		throw new CoreException(TeslaImplPlugin.err(message.toString()));
	}

	public static TeslaErrorStatus getTeslaFailure() throws CoreException {
		List<Response> failures = getPlayer().getFailures();
		if (failures.isEmpty())
			return null;
		Response last = failures.get(failures.size() - 1);
		return new TeslaErrorStatus(last.getMessage(), last.getAdvancedInformation());
	}

	public static List<String> parsePath(String path) throws CoreException {
		List<String> segments = new ArrayList<String>();
		StringBuilder segment = new StringBuilder();

		for (int i = 0; i < path.length(); i++) {
			char ch = path.charAt(i);
			if (ch == '/') {
				segments.add(segment.toString());
				segment = new StringBuilder();
				continue;
			}
			if (ch == '\\') {
				if (i + 1 < path.length()) {
					char next = path.charAt(i + 1);
					if (next == '/') {
						i++;
						segment.append(next);
						continue;
					}
				}
			}
			segment.append(ch);
		}
		segments.add(segment.toString());
		return segments;
	}

	public static List<Integer> parseIndexes(String path) throws CoreException {
		try {
			List<String> segments = parsePath(path);
			List<Integer> indexes = new ArrayList<Integer>(segments.size());
			for (String s : segments) {
				indexes.add(Integer.parseInt(s));
			}
			return indexes;
		} catch (NumberFormatException e) {
			throw new CoreException(CorePlugin.err("Invalid path: " + path + ".\nMust contains integers only"));
		}
	}

	public static TeslaQPlayer getPlayer() throws CoreException {
		if (player == null)
			throw new CoreException(Status.CANCEL_STATUS);
		return player;
	}

	public static TeslaQClient getClient() {
		return client;
	}

	private static WindowUIElement eclipseWindow = null;
	private static ControlUIElement lastControlUIElement;
	private static boolean noErrorMode = false;

	public static Element eclipseWindow() throws CoreException {
		if (eclipseWindow == null) {
			eclipseWindow = getPlayer().eclipseWindow();
			if (eclipseWindow == null) {
				TeslaErrorStatus teslaFailure = getTeslaFailure();
				if (teslaFailure != null) {
					throw new CoreException(teslaFailure);
				}
				throw new CoreException(TeslaImplPlugin.err("Cannot find Eclipse window"));
			}
		}
		return eclipseWindow.getElement();
	}

	public static AdvancedInformation getLastInfo() {
		if (noErrorMode) {
			return null;
		}
		AdvancedInformation last = lastInfo;
		lastInfo = null;
		return last;
	}

	public static void waitExecution() {
		if (player != null) {
			player.nop();
		}
	}

	public static void makeScreenshot(boolean onError, String message) {
		if (noErrorMode && onError) {
			return;
		}
		if (TeslaFeatures.getInstance().isTrue(TeslaFeatures.CAPTURE_EXECUTION) || onError) {
			Display display = PlatformUI.getWorkbench().getDisplay();
			ReportScreenshotProvider.takeScreenshot(display, onError, message);
		}
	}

	public static void storeLastControlUIElement(ControlUIElement element) {
		lastControlUIElement = element;
	}

	public static ControlUIElement getLastControlUIElement() {
		return lastControlUIElement;
	}

	public static void beginNoErrorMode() {
		noErrorMode = true;
	}

	public static void endNoErrorMode() {
		noErrorMode = false;
	}

	public static Widget resolveWidget(String selector, IProcess process, String... classNames) throws CoreException {
		List<Class<?>> classes = new ArrayList<Class<?>>(classNames.length);
		for (int i = 0; i < classNames.length; ++i)
			try {
				classes.add(Class.forName(classNames[i]));
			} catch (ClassNotFoundException e) {
				// just skip it, the class may be not loaded
			}

		return resolveWidget(selector, process, classes.toArray(new Class<?>[classes.size()]));
	}

	public static Widget resolveWidget(String selector, IProcess process, Class<?>... classes) throws CoreException {
		Widget widget = resolveWidget(Widget.class, selector, process);
		for (Class<?> c : classes)
			if (c.isInstance(widget))
				return widget;

		//
		StringBuilder sb = new StringBuilder();
		for (Class<?> cl : classes) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(cl.getSimpleName());
		}
		String classesList = sb.toString();

		throw new CoreException(new Status(Status.ERROR, TeslaImplPlugin.PLUGIN_ID, String.format(
				"Wrong type of a widget. %s expected, got %s.", classesList, widget.getClass().getSimpleName())));
	}

	public static <T> T resolveWidget(Class<T> class_, String selector, IProcess process) throws CoreException {
		SWTUIElement swtuiElement = TeslaBridge.resolveSWTUIElement(selector, process);

		Widget widget = swtuiElement.widget;

		if (!class_.isInstance(widget))
			throw new CoreException(new Status(Status.ERROR, TeslaImplPlugin.PLUGIN_ID,
					String.format("Wrong type of a widget. %s expected, got %s.", class_.getSimpleName(),
							widget.getClass().getSimpleName())));

		return class_.cast(widget);
	}

	public static SWTUIElement resolveSWTUIElement(String selector, IProcess process) throws CoreException {
		ISession session = process.getSession();
		if (!(session instanceof AbstractSession))
			throw new CoreException(
					new Status(Status.ERROR, TeslaImplPlugin.PLUGIN_ID, "Unable to resolve widget (no session)."));
		AbstractRootSession rootSession = ((AbstractSession) session).getRoot();

		Command command = EclCoreParser.newCommand(selector);

		boolean doShutdown = false;
		if (client == null) {
			doShutdown = true;
			setup();
		}

		try {
			IProcess result = rootSession.execute(command);
			try {
				IStatus status = result.waitFor();
				if (!status.isOK())
					throw new CoreException(status);
			} catch (InterruptedException e) {
				throw new CoreException(
						new Status(Status.ERROR, TeslaImplPlugin.PLUGIN_ID, "Unable to resolve widget (timeout)."));
			}
			Object wannabeControlHandler = result.getOutput().take(ISession.DEFAULT_TAKE_TIMEOUT);

			if (!(wannabeControlHandler instanceof ControlHandler))
				throw new CoreException(new Status(Status.ERROR, TeslaImplPlugin.PLUGIN_ID,
						"Unable to resolve widget (not a ControlHandler)."));

			ControlHandler controlHandler = (ControlHandler) wannabeControlHandler;
			Element element = TeslaBridge.find(controlHandler, process);

			SWTUIElement swtuiElement = TeslaBridge.getClient().getProcessor(SWTUIProcessor.class).getMapper()
					.get(element);

			return swtuiElement;
		} finally {
			if (doShutdown)
				shutdown();
		}
	}

	public static boolean isAllowScreenshotOnError(IProcess context) {
		if (context == null) {
			return true;
		}
		Object noScreenshot = context.getSession().getProperty(ISessionPropertyConstants.NO_SCREENSHOT);

		boolean allowScreenshot = (noScreenshot == null);
		return allowScreenshot;
	}
}
