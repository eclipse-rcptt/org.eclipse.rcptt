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

import java.io.ByteArrayOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SWTScreenCapturer implements IScreenCapturer {

	public SWTScreenCapturer() {
	}
	

	private Image getImage(Control control) {
		Display display = control.getDisplay();
		Shell shell = control.getShell();
		GC gc = new GC(shell);
		try {
			Rectangle bounds = shell.getBounds();
			Image image = new Image(display, bounds.width, bounds.height);
			shell.print(gc);
			if (shell != control) {
				gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
				Rectangle arect =  display.map(control.getParent(), shell, control.getBounds());
				gc.drawRectangle(arect.x, arect.y, arect.width, arect.height);
			}
			gc.copyArea(image, 0, 0);
			return image;
		} finally {
			gc.dispose();
		}
	}

	public byte[] makeScreenShotData(Control control,  boolean scale_640_480) {
		Image image = getImage(control);
		if (scale_640_480) {
			int width = image.getBounds().width;
			int height = image.getBounds().height;
			double ra = Math.max((double) (width / 640.0),
					(double) (height / 480.0));
			
			int rx = (int)(width / ra);
			int ry = (int)(height / ra);

			Image newImage = new Image(control.getDisplay(), rx, ry);
			try {
				GC gc = new GC(newImage);
				try {
					gc.setAntialias(SWT.ON);
					gc.setInterpolation(SWT.HIGH);
					gc.drawImage(image, 0, 0, width, height, 0, 0, rx, ry);
				} finally {
					gc.dispose();
				}
			} finally {
				image.dispose();
			}
			image = newImage;
		}

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ImageLoader imageLoader = new ImageLoader();
		try {
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(bout, SWT.IMAGE_PNG);
		} finally {
			image.dispose();
		}
		return bout.toByteArray();
	}
}
