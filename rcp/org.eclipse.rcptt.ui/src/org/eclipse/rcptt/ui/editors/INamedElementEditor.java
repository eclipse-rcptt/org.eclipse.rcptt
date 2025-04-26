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
package org.eclipse.rcptt.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;

public interface INamedElementEditor {

	boolean isDirty();

	void doSave(IProgressMonitor monitor);

	public <T> T getAdapter(Class<T> adapter);

	void renamed(boolean b);

	void setTags(String tags);

}
