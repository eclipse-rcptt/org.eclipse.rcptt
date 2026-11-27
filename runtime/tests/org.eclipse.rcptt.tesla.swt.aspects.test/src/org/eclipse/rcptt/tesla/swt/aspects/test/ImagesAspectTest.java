/*******************************************************************************
 * Copyright (c) 2018, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.swt.aspects.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.rcptt.tesla.jface.ImageSources;
import org.eclipse.rcptt.tesla.jface.ImageSources.CompositeSource;
import org.eclipse.rcptt.tesla.jface.ImageSources.ImageSource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ImagesAspectTest {

	private static final ISharedImages SHARED_IMAGES = PlatformUI.getWorkbench().getSharedImages();

	@Test
	public void newImageFromImageData() {
		final Image image = new Image(Display.getCurrent(),
				SHARED_IMAGES.getImage(ISharedImages.IMG_OBJ_FOLDER).getImageData());
		try {
			Assert.assertEquals("org.eclipse.ui/icons/full/obj16/fldr_obj",
					removeExtensions(ImageSources.INSTANCE.find(image).toString()));
		} finally {
			image.dispose();
		}
	}

	@Test
	public void newImageFromImageDataSourceAndImageDataMask() {
		final Image image = new Image(Display.getCurrent(),
				SHARED_IMAGES.getImage(ISharedImages.IMG_OBJ_FOLDER).getImageData(),
				SHARED_IMAGES.getImage(ISharedImages.IMG_OBJ_FILE).getImageData());
		try {
			Assert.assertEquals("org.eclipse.ui/icons/full/obj16/fldr_obj",
					removeExtensions(ImageSources.INSTANCE.find(image).toString()));
		} finally {
			image.dispose();
		}
	}

	@Test
	public void createImage() {
		final Image folder = SHARED_IMAGES.getImage(ISharedImages.IMG_OBJ_FOLDER);
		final ImageDescriptor[] overlays = new ImageDescriptor[IDecoration.BOTTOM_RIGHT + 1];
		final URL url = Platform.getBundle("org.eclipse.ui").getEntry("icons/full/ovr16/warning_ovr.png");
		overlays[IDecoration.BOTTOM_RIGHT] = ImageDescriptor.createFromURL(url);

		final Image iconImage = new DecorationOverlayIcon(folder, overlays).createImage();

		try {
			final CompositeSource composite = (CompositeSource) ImageSources.INSTANCE.find(iconImage);
			final List<String> strings = new ArrayList<String>();
			for (ImageSource source : composite.children) {
				strings.add(removeExtensions(source.toString()));
			}
			Assert.assertEquals(ImmutableList.of("org.eclipse.ui/icons/full/obj16/fldr_obj",
					"org.eclipse.ui/icons/full/ovr16/warning_ovr"), strings);
		} finally {
			iconImage.dispose();
		}
	}

	private static String removeExtensions(String path) {
		path = removeSuffix(path, ".png");
		path = removeSuffix(path, ".svg");
		return path;
	}

	private static String removeSuffix(String path, String suffix) {
		if (path.endsWith(suffix)) {
			path = path.substring(0, path.length() - suffix.length());
		}
		return path;
	}

}
