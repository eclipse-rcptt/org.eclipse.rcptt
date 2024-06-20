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
package org.eclipse.rcptt.tesla.internal.ui.player;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import org.eclipse.rcptt.tesla.core.protocol.GenericElementKind;
import org.eclipse.rcptt.tesla.swt.workbench.EclipseWorkbenchProvider;

public class WorkbenchUIElement extends SWTUIElement {
	public IWorkbenchPartReference reference;

	public WorkbenchUIElement(IWorkbenchPartReference ref, SWTUIPlayer p) {
		super(null, p);
		this.reference = ref;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((reference == null) ? 0 : reference.hashCode());
		return result;
	}

	@Override
	public GenericElementKind getKind() {
		return SWTUIPlayer.getKind(reference);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkbenchUIElement other = (WorkbenchUIElement) obj;
		if (reference == null) {
			if (other.reference != null)
				return false;
		} else if (!reference.equals(other.reference))
			return false;
		return true;
	}

	public boolean match(Object c) {
		return reference != null && reference.equals(c);
	}

	public String toString() {
		return reference == null ? "Empty" : reference.getClass()
				.getSimpleName() + ":" + PlayerTextUtils.getText(this);
	}

	public String getClassName() {
		return reference == null ? "null" : reference.getClass()
				.getSimpleName();
	}

	public IWorkbenchPartReference getReference() {
		return reference;
	}

	public Menu getViewMenu() {
		if (isView() && reference != null) {
			IWorkbenchPart workbenchPart = reference.getPart(false);
			if (workbenchPart != null) {
				return EclipseWorkbenchProvider.getProvider().getViewMenu(
						workbenchPart, reference, true);
			}
		}
		return null;
	}

	@SuppressWarnings("restriction")
	@Override
	public Rectangle getBounds() {
		if (reference != null) {
			Control pane = ((org.eclipse.ui.internal.WorkbenchPartReference) reference).getPane()
					.getControl();
			if (pane != null && !pane.isDisposed()) {
				Rectangle bounds = pane.getBounds();
				if (pane.getParent() != null) {
					Point point = pane.toDisplay(bounds.x, bounds.y);
					bounds.x = point.x;
					bounds.y = point.y;
				}

				return bounds;
			}
		}
		return super.getBounds();
	}
	
	@Override
	public boolean isDisposed() {
		// Part can be restored from a reference, so even if control is disposed, it is still usable.
		return reference == null;
	}
}
