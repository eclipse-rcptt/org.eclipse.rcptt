/*******************************************************************************
 * Copyright (c) 2009, 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.jface.aspects.test;

import static org.osgi.framework.FrameworkUtil.getBundle;
import static org.osgi.framework.Version.parseVersion;

import java.io.Closeable;
import java.io.IOException;
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
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;

public class ImageSourcesTest {
	private static final ISharedImages SHARED_IMAGES = PlatformUI.getWorkbench().getSharedImages();
	private final Closer closer = Closer.create();
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
	public void getImageData() {
		String ext = IMAGE_EXTENSION;
		Image folder = SHARED_IMAGES.getImage(ISharedImages.IMG_OBJ_FOLDER);
		ImageDescriptor[] overlays = new ImageDescriptor[IDecoration.BOTTOM_RIGHT + 1];
		URL url = Platform.getBundle("org.eclipse.ui").getEntry("icons/full/ovr16/warning_ovr.png");
		overlays[IDecoration.BOTTOM_RIGHT] = ImageDescriptor.createFromURL(url);
		
		DecorationOverlayIcon icon = new DecorationOverlayIcon(folder, overlays);
		Image iconImage = (Image)icon.createResource(Display.getCurrent());
		try {
			Assert.assertEquals(
			ImmutableList.of("org.eclipse.ui/icons/full/obj16/fldr_obj" + ext, "org.eclipse.ui/icons/full/ovr16/warning_ovr.png"), 
			extractStrings(iconImage));
		} finally {
			iconImage.dispose();
		}
	}



	private List<String> extractStrings(Image iconImage) {
		ImageSource imageSource = ImageSources.INSTANCE.find(iconImage);
		CompositeSource composite = (CompositeSource) imageSource;
		List<String> strings = new ArrayList<String>();
		for (ImageSource source: composite.children) {
			strings.add(source.toString());	
		}
		return strings;
	}
	
	
	
	@Test
	public void compositeOverComposite() {
		String ext = IMAGE_EXTENSION;
		ImageDescriptor folder = SHARED_IMAGES.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
		ImageDescriptor file = SHARED_IMAGES.getImageDescriptor(ISharedImages.IMG_OBJ_FILE);
		ImageDescriptor error = SHARED_IMAGES.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR);
		ImageDescriptor warning = SHARED_IMAGES.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_WARNING);

		folder = compose(folder, error);
		file = compose(file, warning);
		
		Image target = compose(folder, file).createImage();
		try {
			Assert.assertEquals(
			ImmutableList.of(
							"org.eclipse.ui/icons/full/obj16/fldr_obj"+ext,
							"org.eclipse.ui/icons/full/ovr16/error_ovr"+ext,
							"org.eclipse.ui/icons/full/obj16/file_obj"+ext,
							"org.eclipse.ui/icons/full/ovr16/warning_ovr"+ext
			)
			,
			 extractStrings(target)
			);
			
		} finally {
			target.dispose();
		}
	}
	
	ImageDescriptor compose(ImageDescriptor image1, ImageDescriptor image2) {
		ImageDescriptor[] overlays = new ImageDescriptor[IDecoration.BOTTOM_RIGHT + 1];
		overlays[IDecoration.BOTTOM_RIGHT] = image2;
		return new DecorationOverlayIcon(disposeAfter(image1.createImage()), overlays);
	}

	private <T extends Resource> T disposeAfter(final T disposable) {
		closer.register(new Closeable() {
			
			@Override
			public void close() throws IOException {
				disposable.dispose();
			}
		});
		return disposable;
	}

	@After
	public void after() throws IOException {
		closer.close();
	}

}
