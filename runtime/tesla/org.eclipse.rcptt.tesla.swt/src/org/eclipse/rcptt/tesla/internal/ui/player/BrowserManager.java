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

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;

import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.context.ContextManagement;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.swt.events.ITeslaBrowserListener;
import org.eclipse.rcptt.tesla.swt.events.TeslaBrowserManager;

public class BrowserManager implements ITeslaBrowserListener {
	private static final long TIMEOUT = 20 * 1000; // 10 seconds for browser to
	private static BrowserManager browserManager;
	// load content.
	private Map<Browser, Long> timeouts = new WeakHashMap<Browser, Long>();

	public BrowserManager() {
		Browser[] browsers = TeslaBrowserManager.getManager().getBrowsers();
		for (Browser browser : browsers) {
			browserAdded(browser);
		}
	}

	ProgressListener progressListener = new ProgressListener() {
		public void completed(ProgressEvent event) {
			if (event.getSource() instanceof Browser) {
				Browser br = (Browser) event.getSource();
				synchronized (timeouts) {
					timeouts.remove(br);
				}
			}
		}

		public void changed(ProgressEvent event) {
		}
	};
	LocationListener locationListener = new LocationListener() {
		public void changing(LocationEvent event) {
		}

		public void changed(LocationEvent event) {
			if (event.getSource() instanceof Browser) {
				Browser br = (Browser) event.getSource();
				if (ContextManagement.currentContext().contains(
						"org.eclipse.swt.browser.Browser", "setText")) {
					return;
				}
				if (Objects.equals(event.location, "about:blank")) {
					return;
				}
				makeChanged(br);
			}
		}
	};
	DisposeListener disposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			if (e.getSource() instanceof Browser) {
				Browser br = (Browser) e.getSource();
				disconnect(br);
			}
		}
	};

	public synchronized void browserAdded(final Browser br) {
		if (br.isDisposed()) {
			return;
		}
		if (Display.getCurrent() == br.getDisplay()) {
			br.addProgressListener(progressListener);
			br.addLocationListener(locationListener);
			br.addDisposeListener(disposeListener);
		} else {
			br.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (br.isDisposed()) {
						return;
					}
					br.addProgressListener(progressListener);
					br.addLocationListener(locationListener);
					br.addDisposeListener(disposeListener);
				}
			});
		}
	}

	public synchronized boolean isExecutionAllowed(Q7WaitInfoRoot info) {
		Set<Browser> result = new HashSet<Browser>();
		long currentTimeMillis = System.currentTimeMillis();
		synchronized (timeouts) {
			for (Browser browser : timeouts.keySet()) {
				if (browser != null && !browser.isDisposed()) {
					Long t = timeouts.get(browser);
					if (currentTimeMillis > (t.longValue() + TIMEOUT)) {
						continue;// Skip browser
					}
					Q7WaitUtils.updateInfo("browser", ""+ browser.getUrl(), info);
					result.add(browser);
				}
			}
		}

		return result.isEmpty();
	}

	public void clear() {
		// timeouts.clear();
	}

	public void browserSetText(Browser browser) {
		synchronized (timeouts) {
			timeouts.remove(browser);
		}
	}

	public void browserChanged(Browser browser) {
		makeChanged(browser);
	}

	private void makeChanged(Browser br) {
		synchronized (timeouts) {
			timeouts.put(br, Long.valueOf(System.currentTimeMillis()));
		}
	}

	public void shutdown() {
		Browser[] browsers = TeslaBrowserManager.getManager().getBrowsers();
		for (Browser browser : browsers) {
			disconnect(browser);
		}
	}

	private void disconnect(final Browser br) {
		if (br == null || br.isDisposed()) {
			return;
		}
		synchronized (timeouts) {
			timeouts.remove(br);
		}
		br.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!br.isDisposed()) {
					br.removeProgressListener(progressListener);
					br.removeLocationListener(locationListener);
					br.removeDisposeListener(disposeListener);
				}
			}
		});
	}

	public synchronized static BrowserManager getInstance() {
		if (browserManager == null) {
			browserManager = new BrowserManager();
			TeslaBrowserManager.getManager().addEventListener(browserManager);
		}
		return browserManager;
	}
}
