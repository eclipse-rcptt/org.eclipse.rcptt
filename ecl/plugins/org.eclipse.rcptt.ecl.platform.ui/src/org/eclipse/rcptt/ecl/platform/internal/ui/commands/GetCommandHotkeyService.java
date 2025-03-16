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
package org.eclipse.rcptt.ecl.platform.internal.ui.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.formatting.KeyFormatterFactory;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.platform.ui.commands.GetHotkey;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

public class GetCommandHotkeyService implements ICommandService {

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		if (!(command instanceof GetHotkey)) {
			return Status.CANCEL_STATUS;
		}
		GetHotkey getCommand = (GetHotkey) command;
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		for (TriggerSequence binding : bindingService.getActiveBindingsFor(getCommand.getCommandId())) {
			for (Trigger trigger : binding.getTriggers()) {
				if (!(trigger instanceof KeyStroke)) {
					continue;
				}
				KeyStroke keyStroke = (KeyStroke) trigger;
				// context.getOutput().write(formatKeyWithMeta(0,
				// keyStroke.getNaturalKey(), keyStroke.getModifierKeys()));
				context.getOutput().write(keyStroke.toString());
				return Status.OK_STATUS;
			}
		}
		return Utils.error("No keyboard hotkey is defined for "
				+ getCommand.getCommandId() + " in current context");
	}

	public static String formatKeyWithMeta(int mask, int keyCode, int meta) {
		if (meta == 0) {
			return KeyFormatterFactory.getFormalKeyFormatter().format(
					KeyStroke.getInstance(mask, keyCode));
		} else {
			String strKey = getMeta(meta);
			if (!strKey.equals(""))
				strKey += "+";
			strKey += KeyFormatterFactory.getFormalKeyFormatter().format(
					keyCode);
			return strKey;
		}
	}

	static class MetaBuilder {
		final StringBuilder sb = new StringBuilder();

		void addMeta(String key) {
			if (sb.length() > 0)
				sb.append("+");
			sb.append(key);
		}

		void tryMeta(int stateMask, int keyMask, String key) {
			if ((stateMask & keyMask) != 0)
				addMeta(key);
		}

		void process(int stateMask) {
			if (Platform.getOS().equals(Platform.OS_MACOSX)) {
				tryMeta(stateMask, SWT.COMMAND, "M1");
				tryMeta(stateMask, SWT.SHIFT, "M2");
				tryMeta(stateMask, SWT.ALT, "M3");
				tryMeta(stateMask, SWT.CTRL, "M4");
			} else {
				tryMeta(stateMask, SWT.CTRL, "M1");
				tryMeta(stateMask, SWT.SHIFT, "M2");
				tryMeta(stateMask, SWT.ALT, "M3");
			}
		}

		public String toString() {
			return sb.toString();
		}
	}

	public static String getMeta(int stateMask) {
		MetaBuilder rv = new MetaBuilder();
		rv.process(stateMask);
		return rv.toString();
	}
}
