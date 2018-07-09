/*******************************************************************************
 * Copyright (c) 2009, 2017 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.jface.aspects.test;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

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

import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;

public class ImageSourcesTest {
	private static final ISharedImages SHARED_IMAGES = PlatformUI.getWorkbench().getSharedImages();
	private final Closer closer = Closer.create();
	
	@Test
	public void getImageData() {
		Image folder = SHARED_IMAGES.getImage(ISharedImages.IMG_OBJ_FOLDER);
		ImageDescriptor[] overlays = new ImageDescriptor[IDecoration.BOTTOM_RIGHT + 1];
		URL url = Platform.getBundle("org.eclipse.ui").getEntry("icons/full/ovr16/warning_ovr.png");
		overlays[IDecoration.BOTTOM_RIGHT] = ImageDescriptor.createFromURL(url);
		
		DecorationOverlayIcon icon = new DecorationOverlayIcon(folder, overlays);
		Image iconImage = (Image)icon.createResource(Display.getCurrent());
		try {
			Assert.assertEquals(
			ImmutableList.of("org.eclipse.ui/icons/full/obj16/fldr_obj.png", "org.eclipse.ui/icons/full/ovr16/warning_ovr.png"), 
			extractStrings(iconImage));
		} finally {
			iconImage.dispose();
		}
	}



	private List<String> extractStrings(Image iconImage) {
		ImageSource imageSource = ImageSources.INSTANCE.find(iconImage);
		CompositeSource composite = (CompositeSource) imageSource;
		List<String> strings = composite.children.stream().map(Object::toString).collect(Collectors.toList());
		return strings;
	}
	
	
	
	@Test
	public void compositeOverComposite() {
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
							"org.eclipse.ui/icons/full/obj16/fldr_obj.png",
							"org.eclipse.ui/icons/full/ovr16/error_ovr.png",
							"org.eclipse.ui/icons/full/obj16/file_obj.png",
							"org.eclipse.ui/icons/full/ovr16/warning_ovr.png"
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

	private <T extends Resource> T disposeAfter(T disposable) {
		closer.register(() -> disposable.dispose());
		return disposable;
	}

	@After
	public void after() throws IOException {
		closer.close();
	}

}
