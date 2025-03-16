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
package org.eclipse.rcptt.core.ecl.context.internal.viewer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rcptt.core.ecl.context.viewer.Activator;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.editors.IQ7Editor;
import org.eclipse.rcptt.ui.editors.IQ7Viewer2;
import org.eclipse.ui.IEditorPart;

public class EclContextViewer implements IQ7Viewer2<Context> {

	@Override
	public IQ7Editor<Context> createEditor() {
		return new EclContextEditor();
	}

	public static ImageDescriptor getEclContextImage() {
		return Q7UIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
				"/icons/script.gif");
	}

	@Override
	public String getLabel(Context context) {
		return "Ecl Script Context";
	}

	@Override
	public IEditorPart createEditorPart() {
		return new org.eclipse.rcptt.core.ecl.context.editor.EclContextEditor();
	}

	@Override
	public boolean isEnableResourceTracking() {
		return false;
	}

	@Override
	public boolean isCaptureSupported() {
		return false;
	}

	@Override
	public boolean isApplySupported() {
		return true;
	}
}
