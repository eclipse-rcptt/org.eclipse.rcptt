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
package org.eclipse.rcptt.maven.util;

/**
 * Returns various parameters of the platform we are running on
 */
public class PlatformUtil {
	/**
	 * 
	 * @return The classifier of current platform in form of os.ws.arch
	 */
	public static String getEclipseClassifier() {
		return String.format("%s.%s", getOS().classifier, getArch()); //$NON-NLS-1$
	}

	public static String getArch() {
		String result = System.getProperty("os.arch"); //$NON-NLS-1$
		if (result != null) {
			result = result.toLowerCase();
			switch (result) {
			case "x86": //$NON-NLS-1$
			case "x86_64": //$NON-NLS-1$
			case "aarch64": //$NON-NLS-1$
				return result;
			case "i386": //$NON-NLS-1$
			case "i486": //$NON-NLS-1$
			case "i586": //$NON-NLS-1$
			case "i686": //$NON-NLS-1$
			case "pentium": //$NON-NLS-1$
				return "x86"; //$NON-NLS-1$
			case "amd64": //$NON-NLS-1$
			case "em64t": //$NON-NLS-1$
			case "universal": //$NON-NLS-1$
				return "x86_64"; //$NON-NLS-1$
			default:
				break;
			}
		}
		throw new IllegalStateException(String.format("Unsupported \"os.arch\": %s", result)); //$NON-NLS-1$
	}

	public static OS getOS() {
		String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
		if (osName.contains("win")) { //$NON-NLS-1$
			return OS.WIN;
		}
		if (osName.contains("mac")) { //$NON-NLS-1$
			return OS.MACOSX;
		}
		return OS.LINUX;
	}
}
