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
package org.eclipse.rcptt.launching.ext;

import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.pde.launching.EclipseApplicationLaunchConfiguration;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;

/**
 * Describes bundle start level and autostart property
 * 
 * @author ivaninozemtsev
 * 
 */
public class BundleStart {
	private BundleStart(int level, boolean autoStart, boolean levelIsDefault,
			boolean autostartIsDefault) {
		this.level = level;
		this.levelIsDefault = levelIsDefault;
		this.autoStart = autoStart;
		this.autoStartIsDefault = autostartIsDefault;
	}

	public static BundleStart DEFAULT = new BundleStart(-1, false, true, true);

	public boolean isDefault() {
		return levelIsDefault && autoStartIsDefault;
	}

	/**
	 * string in form suitable for osgi.bundles property of config.ini. More
	 * precisely, returns the following:
	 * <table>
	 * <tr>
	 * <th>default level?</th>
	 * <th>default start?</th>
	 * <th>result</th>
	 * </tr>
	 * <tr>
	 * <td>yes</td>
	 * <td>yes</td>
	 * <td>empty string</td>
	 * </tr>
	 * <tr>
	 * <td>yes</td>
	 * <td>no</td>
	 * <td>@4:start</td>
	 * </tr>
	 * <tr>
	 * <td>no</td>
	 * <td>yes</td>
	 * <td>@3</td>
	 * </tr>
	 * <tr>
	 * <td>no</td>
	 * <td>no</td>
	 * <td>@2:start</td>
	 * </tr>
	 * </table>
	 * 
	 * @return
	 */
	public String toOsgiString(int defaultStartLevel) {
		if (levelIsDefault && autoStartIsDefault) {
			return "";
		}
		if (autoStartIsDefault) {
			return String.format("@%d", level);
		}

		int resolvedLevel = levelIsDefault ? defaultStartLevel : level;
		String resolvedStart = autoStart ? ":start" : "";
		return String.format("@%d%s", resolvedLevel, resolvedStart);
	}

	public static BundleStart fromOsgiString(String str) {
		if (str == null) {
			return DEFAULT;
		}
		if (str.startsWith("@")) {
			str = str.substring(1);
		}
		if (str.length() == 0) {
			return DEFAULT;
		}

		int sep = str.indexOf(':');
		String levelStr = sep == -1 ? str : str.substring(0, sep);
		String autoStartStr = sep == -1 ? "" : str.substring(sep + 1);

		if (START_STR.equals(levelStr) && autoStartStr.length() == 0) {
			// bundleId@start specified without preceding colon
			return new BundleStart(-1, true, true, false);
		}

		boolean isDefaultLevel = levelStr.length() == 0;
		int level = isDefaultLevel ? -1 : Integer.parseInt(levelStr);

		boolean isDefaultAutoStart = sep == -1;
		boolean autoStart = START_STR.equals(autoStartStr);

		return new BundleStart(level, autoStart, isDefaultLevel,
				isDefaultAutoStart);
	}

	/**
	 * @return string in form suitable for
	 *         {@link EclipseApplicationLaunchConfiguration}'s
	 *         <code>fModels</code> field.
	 */
	public String toModelString() {
		return String.format("%s:%s",
				levelIsDefault ? DEF_STR : Integer.toString(level),
				autoStartIsDefault ? DEF_STR : Boolean.toString(autoStart));
	}

	public static BundleStart fromModelString(String str) {
		if (str == null || str.length() == 0) {
			return DEFAULT;
		}
		int sep = str.indexOf(':');
		String levelStr = sep == -1 ? str : str.substring(0, sep);
		String startStr = sep == -1 ? DEF_STR : str.substring(sep + 1);

		boolean isDefaultLevel = DEF_STR.equals(levelStr);
		boolean isDefaultStart = DEF_STR.equals(startStr);
		return new BundleStart(
				isDefaultLevel ? -1 : Integer.parseInt(levelStr),
				isDefaultStart ? false : Boolean.parseBoolean(startStr),
				isDefaultLevel, isDefaultStart);
	}

	public static BundleStart fromBundle(BundleInfo info) {
		return new BundleStart(info.getStartLevel(), info.isMarkedAsStarted(),
				info.getStartLevel() == BundleInfo.NO_LEVEL,
				!info.isMarkedAsStarted());
	}

	public final int level;
	public final boolean autoStart;

	private boolean levelIsDefault;
	private boolean autoStartIsDefault;

	private static final String DEF_STR = "default";
	private static final String START_STR = "start";

	public static BundleStart fromQ7Headers(String q7sl, String q7as) {
		return fromModelString(Joiner.on(':').join(
				MoreObjects.firstNonNull(q7sl, DEF_STR),
				MoreObjects.firstNonNull(q7as, DEF_STR)));
	}
}
