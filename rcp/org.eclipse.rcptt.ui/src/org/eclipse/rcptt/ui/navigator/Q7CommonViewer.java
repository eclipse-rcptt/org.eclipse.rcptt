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
package org.eclipse.rcptt.ui.navigator;

import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.navigator.CommonViewer;

public class Q7CommonViewer extends CommonViewer {

	public Q7CommonViewer(String aViewerId, Composite aParent, int aStyle) {
		super(aViewerId, aParent, aStyle);
		super.setComparator(new Q7ViewerSorter());
	}
	
	@Override
	public void setComparator(ViewerComparator comparator) {
		// do not allow to change sorter
	}

	@Override
	public void setSorter(ViewerSorter sorter) {
		// do not allow to change sorter
	}

}
