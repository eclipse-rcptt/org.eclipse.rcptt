/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;

public interface IQ7Viewer<T> {

	public String getLabel(T source);

	public ImageDescriptor getImage(T source);

	public IQ7Editor<T> createEditor();

	public boolean isCaptureSupported();

	public boolean isApplySupported();

}
