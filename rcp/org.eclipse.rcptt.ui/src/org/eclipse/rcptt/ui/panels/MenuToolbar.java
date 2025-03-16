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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.rcptt.internal.ui.Images;

public class MenuToolbar {
	public Control create(Composite parent) {
		final ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.HORIZONTAL);

		final ToolItem item = new ToolItem(toolbar, SWT.NONE);
		Image image = Images.getImageDescriptor(Images.PANEL_MENU)
				.createImage();
		item.addDisposeListener(event -> image.dispose());
		item.setImage(image);

		final MenuManager manager = new MenuManager();
		fill(manager);
		final Menu menu = manager.createContextMenu(toolbar); // TODO: dispose?
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final Rectangle rect = item.getBounds();
				menu.setLocation(toolbar.toDisplay(new Point(rect.x, rect.y
						+ rect.height)));
				menu.setVisible(true);
			}
		});

		return toolbar;
	}

	protected void fill(MenuManager manager) {

	}
}
