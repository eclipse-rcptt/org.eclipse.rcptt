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
package org.eclipse.rcptt.tesla.jface;

import static org.eclipse.rcptt.util.ReflectionUtil.getField;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.internal.misc.ExternalProgramImageDescriptor;
import org.osgi.framework.Bundle;

public enum DescriptorInfo {	
	EXTERNAL_PROGRAM_ADAPTER() {
		@Override
		String extract(ImageDescriptor descriptor) {
			Program p = Adapters.adapt(descriptor, Program.class, false);
			return extractFromProgram(p);
		}
		
	},
	
	BUNDLE_URL() {
		private final Pattern pattern = Pattern.compile("URLImageDescriptor\\(((bundleentry|bundleresource).*)\\)");

		@Override
		String extract(ImageDescriptor descriptor) {
			Matcher matcher = pattern.matcher(descriptor.toString());
			if (!matcher.matches()) {
				return null;
			}
			String uriStr = matcher.group(1);
			URI bundleUri = null;
			try {
				bundleUri = new URI(uriStr);
			} catch (URISyntaxException e) {
				return "InvalidUri(" + uriStr + ")";
			}
			return extractFromUri(bundleUri);
		}
	},

	ABSOLUTE_URL() {
		private final Pattern pattern = Pattern.compile("URLImageDescriptor\\((file:/|platform:/plugin/)(.*)\\)");
		@Override
		String extract(ImageDescriptor descriptor) {
			Matcher matcher = pattern.matcher(descriptor.toString());
			if (matcher.matches()) {
				return matcher.group(2);
			} else {
				return null;
			}
		}
	},

	FILE_CLASS() {
		private final Pattern pattern = Pattern.compile("FileImageDescriptor\\(location=class (.*), name=(.*)\\)");
		@Override
		String extract(ImageDescriptor descriptor) {
			Matcher matcher = pattern.matcher(descriptor.toString());
			if (matcher.matches()) {
				return String.format("%s%s", matcher.group(1), matcher.group(2));
			} else {
				return null;
			}
		}
	},
	
	URL_ADAPTER() {
		@Override
		String extract(ImageDescriptor descriptor) {
			URL url = Adapters.adapt(descriptor, URL.class, false);
			if (url != null) {
				try {
					return extractFromUri(url.toURI());
				} catch (URISyntaxException e) {
					JFaceAspectsActivator.log(e);
					return null;
				}
			}
			return null;
		}
		
	},

	/**
	 * Gets info from program because ExternalProgramImageDescriptor.toString() has no useful information
	 */
	EXTERNAL_PROGRAM() {
		@Override
		String extract(ImageDescriptor descriptor) {
			if (descriptor instanceof ExternalProgramImageDescriptor) {
				try {
					return extractFromProgram( (Program) getField(descriptor, "program", true) );
				} catch (NoSuchFieldException | IllegalAccessException e) {
					JFaceAspectsActivator.log(e);
					return null;
				}
			}
			return null;
		}
	};
	


	public static String getInfo(ImageDescriptor descriptor) {
		for (DescriptorInfo i : DescriptorInfo.values()) {
			String info = i.extract(descriptor);
			if (info != null)
				return info;
		}

		return null;
	}

	abstract String extract(ImageDescriptor descriptor);
	
	private static final boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);
	private static String extractFromProgram(Program p) {
		if (p == null) {
			return null;
		}
		try {
			if (isWindows) {
				String extension = (String) getField(p, "extension", true);
				if (extension != null && !extension.isEmpty()) {
					return extension;
				}
				
				String iconName = (String) getField(p, "iconName", true);
				if (iconName != null && !iconName.isEmpty()) {
					return iconName;
				}
			}
		} catch (IllegalArgumentException e) {
			JFaceAspectsActivator.log(e);
		} catch (IllegalAccessException e) {
			JFaceAspectsActivator.log(e);
		} catch (NoSuchFieldException e) {
			JFaceAspectsActivator.log(e);
		} catch (SecurityException e) {
			JFaceAspectsActivator.log(e);
		}
		return p.getName().isEmpty() ? null : p.getName();
	}
	private static String extractFromUri(URI bundleUri) {
		String host = bundleUri.getHost();
		if (host == null) {
			return null;
		}
		int bundleIdEndIndex = host.indexOf(".fwk");
		if (bundleIdEndIndex == -1) {
			return "UnknownBundleId(" + bundleUri + ")";
		}

		int bundleId = -1;
		try {
			bundleId = Integer.parseInt(host.substring(0, bundleIdEndIndex));
		} catch (NumberFormatException e) {
			return "UnknownBundleId(" + bundleUri + ")";
		}

		Bundle imageBundle = JFaceAspectsActivator.getDefault().getBundle().getBundleContext()
				.getBundle(bundleId);
		String bundleName = imageBundle == null ? "unknownBundle" : imageBundle.getSymbolicName();
		return String.format("%s%s", bundleName, bundleUri.getPath());
	}

}
