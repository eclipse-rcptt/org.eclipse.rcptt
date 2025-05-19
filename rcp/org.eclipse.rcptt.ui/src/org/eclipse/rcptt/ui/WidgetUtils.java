/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

public final class WidgetUtils {
	private WidgetUtils() {}
	public static void asyncExec(Widget widget, Runnable runnable) {
		if (widget == null) {
			return;
		}
		if (widget.isDisposed()) {
			return;
		}
		try {
			Display display = widget.getDisplay();
			display.asyncExec(() -> {
				if (widget.isDisposed()) {
					return;
				}
				runnable.run();
			});
		} catch (SWTException e) {
			if (e.code == SWT.ERROR_WIDGET_DISPOSED) {
				return;
			}
			throw e;
		}
	}
}
