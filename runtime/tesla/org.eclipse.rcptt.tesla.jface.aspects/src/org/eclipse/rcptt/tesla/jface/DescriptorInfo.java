/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.internal.misc.ExternalProgramImageDescriptor;
import org.osgi.framework.Bundle;

public enum DescriptorInfo {
	BUNDLE_URL() {
		public final Pattern pattern = Pattern.compile("URLImageDescriptor\\(((bundleentry|bundleresource).*)\\)");

		@Override
		public String extract(String descriptorStr) {
			Matcher matcher = pattern.matcher(descriptorStr);
			if (matcher.matches()) {
				String uriStr = matcher.group(1);
				URI bundleUri = null;
				try {
					bundleUri = new URI(uriStr);
				} catch (URISyntaxException e) {
					return "InvalidUri(" + uriStr + ")";
				}

				String host = bundleUri.getHost();
				int bundleIdEndIndex = host.indexOf(".fwk");
				if (bundleIdEndIndex == -1) {
					return "UnknownBundleId(" + uriStr + ")";
				}

				int bundleId = -1;
				try {
					bundleId = Integer.parseInt(host.substring(0, bundleIdEndIndex));
				} catch (NumberFormatException e) {
					return "UnknownBundleId(" + uriStr + ")";
				}

				Bundle imageBundle = JFaceAspectsActivator.getDefault().getBundle().getBundleContext()
						.getBundle(bundleId);
				String bundleName = imageBundle == null ? "unknownBundle" : imageBundle.getSymbolicName();
				return String.format("%s%s", bundleName, bundleUri.getPath());
			} else {
				return null;
			}
		}

		@Override
		String extract(ImageDescriptor descriptor) {
			return extract(descriptor.toString());
		}
	},

	ABSOLUTE_URL() {
		public final Pattern pattern = Pattern.compile("URLImageDescriptor\\((file:/|platform:/plugin/)(.*)\\)");

		@Override
		public String extract(String descriptorStr) {
			Matcher matcher = pattern.matcher(descriptorStr);
			if (matcher.matches()) {
				return matcher.group(2);
			} else {
				return null;
			}
		}

		@Override
		String extract(ImageDescriptor descriptor) {
			return extract(descriptor.toString());
		}
	},

	FILE_CLASS() {
		public final Pattern pattern = Pattern.compile("FileImageDescriptor\\(location=class (.*), name=(.*)\\)");

		@Override
		public String extract(String descriptorStr) {
			Matcher matcher = pattern.matcher(descriptorStr);
			if (matcher.matches()) {
				return String.format("%s%s", matcher.group(1), matcher.group(2));
			} else {
				return null;
			}
		}

		@Override
		String extract(ImageDescriptor descriptor) {
			return extract(descriptor.toString());
		}
	},

	/**
	 * Gets info from program because ExternalProgramImageDescriptor.toString() has no useful information
	 */
	EXTERNAL_PROGRAM() {
		private final boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);
		
		@Override
		String extract(ImageDescriptor descriptor) {
			if (descriptor instanceof ExternalProgramImageDescriptor) {
				if (isWindows) {
					try {
						Program p = (Program) getField(descriptor, "program", true);
	
						String extension = (String) getField(p, "extension", true);
						if (extension != null && !extension.isEmpty()) {
							return extension;
						}
	
						String iconName = (String) getField(p, "iconName", true);
						if (iconName != null && !iconName.isEmpty()) {
							return iconName;
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

	public String extract(String descriptorStr) {
		return null;
	}

	abstract String extract(ImageDescriptor descriptor);
}
