/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.ui.launching;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public interface ITestEngineLaunchConfig {

	public void createControl(Composite parent, Listener listener);

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration);

	public void initializeFrom(ILaunchConfiguration configuration);

	public void performApply(ILaunchConfigurationWorkingCopy configuration);

	public String validatePage();

}
