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

import com.google.common.collect.ImmutableList;

import static org.osgi.framework.FrameworkUtil.getBundle;
import static org.osgi.framework.Version.parseVersion;

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
import org.osgi.framework.Version;

public class ImagesAspectTest {

	private static final ISharedImages SHARED_IMAGES = PlatformUI.getWorkbench().getSharedImages();
	private static final String IMAGE_EXTENSION;
	static {
		// Version is not exact here, ideally it should be set to a version introducing SVG icons
		Version workbenchVersion = getBundle(SHARED_IMAGES.getClass()).getVersion();
		if (workbenchVersion.compareTo(parseVersion("3.130.0")) > 0) {
			IMAGE_EXTENSION = ".svg";
		} else {
			IMAGE_EXTENSION = ".png";
		}
	}

	@Test
	public void newImageFromImageData() {
		final Image image = new Image(Display.getCurrent(),
				SHARED_IMAGES.getImage(ISharedImages.IMG_OBJ_FOLDER).getImageData());
		try {
			Assert.assertEquals("org.eclipse.ui/icons/full/obj16/fldr_obj" + IMAGE_EXTENSION,
					ImageSources.INSTANCE.find(image).toString());
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
			Assert.assertEquals("org.eclipse.ui/icons/full/obj16/fldr_obj" + IMAGE_EXTENSION,
					ImageSources.INSTANCE.find(image).toString());
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
				strings.add(source.toString());
			}
			Assert.assertEquals(ImmutableList.of("org.eclipse.ui/icons/full/obj16/fldr_obj" + IMAGE_EXTENSION,
					"org.eclipse.ui/icons/full/ovr16/warning_ovr.png"), strings);
		} finally {
			iconImage.dispose();
		}
	}

}
