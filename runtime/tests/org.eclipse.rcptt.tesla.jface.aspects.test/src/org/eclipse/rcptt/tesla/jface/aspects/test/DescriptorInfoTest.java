/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.jface.aspects.test;

import static org.eclipse.rcptt.tesla.jface.DescriptorInfo.getInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Test;

public class DescriptorInfoTest {

	@Test
	public void testInvalid() throws MalformedURLException {
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(new URL("http:invalid"));
		assertNull(getInfo(descriptor));
	}

	@Test
	public void testBundleResource() throws MalformedURLException {
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(new URL("bundleresource://5724.fwk1864311781:1/icons/icon.png"));
		assertEquals("unknownBundle/icons/icon.png", getInfo(descriptor));
	}

	@Test
	public void testBundleEntry() throws MalformedURLException {
		long id = Platform.getBundle("org.eclipse.rcptt.tesla.jface.aspects.test").getBundleId();
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(new URL("bundleentry://"+id+".fwk1873444284/icons/full/eview16/filenav_nav.gif"));
		assertEquals("org.eclipse.rcptt.tesla.jface.aspects.test/icons/full/eview16/filenav_nav.gif",
				getInfo(descriptor));
	}

	@Test
	public void testAbsolutePath() throws MalformedURLException {
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(new URL("file:/C:/rcptt/icons/icon.png"));
		assertEquals("C:/rcptt/icons/icon.png", getInfo(descriptor));
	}

	@Test
	public void testPlatformPlugin() throws MalformedURLException {
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(new URL("platform:/plugin/org.eclipse.rcptt/icons/scroll_lock.png"));
		assertEquals("org.eclipse.rcptt/icons/scroll_lock.png", getInfo(descriptor));
	}

	@Test
	public void testFileClass() {
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(org.eclipse.jface.action.Separator.class, "images/stop.gif");
		assertEquals("org.eclipse.jface.action.Separatorimages/stop.gif", getInfo(descriptor));
	}
	
	@Test
	public void testProgram() {
		boolean match = false;
		match |= assertEditor("Safari");
		match |= assertEditor("Python (v3.12)");
		match |= assertEditor("Python");
		match |= assertEditor("Notepad", "txt"); // blind test for Windows. modify to fit.
		assertTrue("No editors suitable for test are found", match);
	}

	private boolean assertEditor(String editorId) {
		return assertEditor(editorId, editorId);
	}
	private boolean assertEditor(String editorId, String expected) {
		IEditorDescriptor editor = PlatformUI.getWorkbench().getEditorRegistry().findEditor(editorId);
		if (editor == null) {
			return false;
		}
		ImageDescriptor descriptor = editor.getImageDescriptor();
		Assert.assertEquals("org.eclipse.ui.internal.misc.ExternalProgramImageDescriptor", descriptor.getClass().getName());
		assertEquals(expected, getInfo(descriptor));
		return true;
	}
	

}
