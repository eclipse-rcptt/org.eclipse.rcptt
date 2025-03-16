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
package org.eclipse.rcptt.verifications.tree.ui;

import org.eclipse.rcptt.core.scenario.WidgetVerification;
import org.eclipse.rcptt.ui.editors.IQ7Editor;
import org.eclipse.rcptt.ui.editors.IQ7Viewer;

public class TreeVerificationViewer implements IQ7Viewer<WidgetVerification> {

	@Override
	public String getLabel(WidgetVerification source) {
		return "Widget Tree";
	}

	@Override
	public IQ7Editor<WidgetVerification> createEditor() {
		return new TreeVerificationEditor();
	}

	@Override
	public boolean isCaptureSupported() {
		return true;
	}

	@Override
	public boolean isApplySupported() {
		return true;
	}

}
