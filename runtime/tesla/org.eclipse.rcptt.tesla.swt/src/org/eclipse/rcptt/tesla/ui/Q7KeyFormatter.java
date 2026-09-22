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
package org.eclipse.rcptt.tesla.ui;

import static org.eclipse.rcptt.util.KeysAndButtons.getModifiers;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.formatting.AbstractKeyFormatter;
import org.eclipse.jface.bindings.keys.formatting.KeyFormatterFactory;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

@SuppressWarnings("restriction")
public class Q7KeyFormatter extends AbstractKeyFormatter {
	@Override
	protected String getKeyDelimiter() {
		return KeyStroke.KEY_DELIMITER;
	}

	@Override
	protected String getKeyStrokeDelimiter() {
		return KeySequence.KEY_STROKE_DELIMITER;
	}

	@Override
	protected int[] sortModifierKeys(int modifierKeys) {
		final int[] sortedKeys = new int[getModifiers().size()];
		int index = 0;
		for (int i : getModifiers().keySet()) {
			if ((modifierKeys & i) != 0) {
				sortedKeys[index++] = i;
			}
		}
		return sortedKeys;
	}

	@Override
	public String format(int key) {
		String result = getModifiers().get(key);
		return result == null ? super.format(key) : result;
	}

	public static void installQ7Formatter() {
		KeyFormatterFactory.setDefault(new Q7KeyFormatter());
		IWorkbench workbench = PlatformUI.getWorkbench();
		for (IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
			if (!(w instanceof WorkbenchWindow))
				continue;

			final WorkbenchWindow ww = (WorkbenchWindow) w;
			final Shell shell = ww.getShell();
			try {
				final ICoolBarManager coolBarManager = ww.getCoolBarManager2();

				if (shell != null && coolBarManager != null)
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							try {
								shell.setLayoutDeferred(true);
								coolBarManager.update(true);
							} finally {
								shell.setLayoutDeferred(false);
							}
						}
					});
			} catch (Exception e) {
				// Ignore exception.
			}
		}
	}
}
