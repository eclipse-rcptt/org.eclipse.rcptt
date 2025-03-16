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
package org.eclipse.rcptt.ui.panels;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.databinding.util.JFaceProperties;
import org.eclipse.jface.resource.ImageDescriptor;

public class Actions {
	public static final IObservableValue<Boolean> observeEnabled(IAction action) {
		return JFaceProperties.<IAction, Boolean>value(IAction.class, "enabled", IAction.ENABLED) //$NON-NLS-1$
				.observe(action);
	}

	public static final IObservableValue<Boolean> observeChecked(IAction action) {
		return JFaceProperties.<IAction, Boolean>value(IAction.class, "checked", IAction.CHECKED) //$NON-NLS-1$
				.observe(action);
	}

	public static final IObservableValue<ImageDescriptor> observeImageDescriptor(IAction action) {
		return JFaceProperties.<IAction, ImageDescriptor>value(IAction.class, "imageDescriptor", //$NON-NLS-1$
				IAction.IMAGE).observe(action);
	}

	public static final IObservableValue<String> observeToolTipText(IAction action) {
		return JFaceProperties.<IAction, String>value(IAction.class, "toolTipText", //$NON-NLS-1$
				IAction.TOOL_TIP_TEXT).observe(action);
	}
}
