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
package org.eclipse.rcptt.verifications.time.ui;

import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.ui.editors.IQ7Editor;
import org.eclipse.rcptt.ui.editors.IQ7Viewer;

public class TimeVerificationViewer implements IQ7Viewer<Verification> {

	@Override
	public String getLabel(Verification source) {
		return "Execution Time";
	}

	@Override
	public IQ7Editor<Verification> createEditor() {
		return new TimeVerificationEditor();
	}

	@Override
	public boolean isCaptureSupported() {
		return false;
	}

	@Override
	public boolean isApplySupported() {
		return false;
	}

}
