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
package org.eclipse.rcptt.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;

public class Images {

	private static final String PREFIX = "icons/"; //$NON-NLS-1$

	public static final String CONTEXT = PREFIX + "context.gif"; //$NON-NLS-1$
	public static final String VERIFICATION = PREFIX + "verification.gif"; //$NON-NLS-1$
	public static final String CONTEXT_NEW = PREFIX + "context_new.gif"; //$NON-NLS-1$
	public static final String SCENARIO = PREFIX + "scenario.gif"; //$NON-NLS-1$
	public static final String SCENARIO_EMPTY = PREFIX + "scenario_empty.gif"; //$NON-NLS-1$
	public static final String PROJECT = PREFIX + "project.gif"; //$NON-NLS-1$
	public static final String FOLDER = PREFIX + "folder.gif"; //$NON-NLS-1$
	public static final String FILE = PREFIX + "file.gif"; //$NON-NLS-1$
	public static final String NEW_PROJECT = PREFIX + "newProject.gif"; //$NON-NLS-1$
	public static final String NEW_FOLDER = PREFIX + "newFolder.gif"; //$NON-NLS-1$
	public static final String NEW_FILE = PREFIX + "newFile.gif"; //$NON-NLS-1$
	public static final String IMPORT_PROJECTS = PREFIX + "importProjects.gif"; //$NON-NLS-1$
	public static final String IMPORT_FILES = PREFIX + "importFiles.gif"; //$NON-NLS-1$
	public static final String LINK_PROJECT = PREFIX + "link-project.gif"; //$NON-NLS-1$
	public static final String LINK_FOLDER = PREFIX + "link-folder.gif"; //$NON-NLS-1$
	public static final String LINK_FILE = PREFIX + "link-file.gif"; //$NON-NLS-1$
	public static final String LINK_OVERLAY = PREFIX + "link-ovelay.gif"; //$NON-NLS-1$
	public static final String LINK_BROKEN = PREFIX + "link-broken.gif"; //$NON-NLS-1$	
	public static final String WORKSPACE = PREFIX + "workspace.gif"; //$NON-NLS-1$
	public static final String UNKNOWN = PREFIX + "unknown.gif"; //$NON-NLS-1$
	public static final String SNAPSHOT = PREFIX + "camera.png"; //$NON-NLS-1$
	public static final String SNAPSHOT_NEW = PREFIX + "camera_new.png"; //$NON-NLS-1$
	public static final String PREFERENCES = PREFIX + "preferences.gif"; //$NON-NLS-1$
	public static final String PARAMETERS = PREFIX + "parameters.gif"; //$NON-NLS-1$
	public static final String PARAMETER = PREFIX + "parameter.gif"; //$NON-NLS-1$
	public static final String GEARS = PREFIX + "releng_gears.gif"; //$NON-NLS-1$
	public static final String FILESYSTEM = PREFIX + "filesystem.png"; //$NON-NLS-1$

	public static final String UP = PREFIX + "up.gif"; //$NON-NLS-1$
	public static final String DOWN = PREFIX + "down.gif"; //$NON-NLS-1$
	public static final String AUT = PREFIX + "q7.gif"; //$NON-NLS-1$

	public static final String REFRESH = PREFIX + "refresh.gif"; //$NON-NLS-1$
	public static final String TAG = PREFIX + "tag.gif"; //$NON-NLS-1$

	public static final String REPORT = PREFIX + "report.gif"; //$NON-NLS-1$

	public static final String SEARCH_CLEAR = PREFIX + "search_clear.gif"; //$NON-NLS-1$
	public static final String DISABLED_SEARCH_CLEAR = PREFIX
			+ "search_clear_disabled.gif"; //$NON-NLS-1$
	//
	// Panel
	//
	public static final String PANEL_SAVE = PREFIX + "panel/save.gif"; //$NON-NLS-1$
	public static final String PANEL_CLEAR = PREFIX + "panel/clear.gif"; //$NON-NLS-1$
	public static final String PANEL_SCROLL_LOCK = PREFIX + "panel/scroll_lock.gif"; //$NON-NLS-1$
	public static final String PANEL_MODE_RECORD = PREFIX + "panel/mode_click.gif"; //$NON-NLS-1$
	public static final String PANEL_MODE_ASSERT = PREFIX + "panel/mode_assert.gif"; //$NON-NLS-1$
	public static final String PANEL_MODE_IML = PREFIX + "panel/mode_iml.png"; //$NON-NLS-1$
	public static final String PANEL_NEW_IMAGE_ASSERT = PREFIX
			+ "panel/new_img_assert.png"; //$NON-NLS-1$
	public static final String PANEL_NEW_IMAGE_WIDGET_DETAILS = PREFIX
			+ "panel/widget_details.gif"; //$NON-NLS-1$
	public static final String PANEL_HOME = PREFIX + "panel/home.gif"; //$NON-NLS-1$
	public static final String PANEL_RECORD = PREFIX + "panel/record.gif"; //$NON-NLS-1$
	public static final String PANEL_IMLMODE = PREFIX + "panel/record.gif"; //$NON-NLS-1$
	public static final String PANEL_STOP = PREFIX + "panel/stop.gif"; //$NON-NLS-1$
	public static final String PANEL_MENU = PREFIX + "panel/menu.gif"; //$NON-NLS-1$
	public static final String PANEL_SCENARIO = PREFIX + "panel/scenario.gif"; //$NON-NLS-1$
	public static final String PANEL_Q7 = PREFIX + "panel/q7.gif"; //$NON-NLS-1$
	public static final String PANEL_EXPAND_ALL = PREFIX + "panel/expandall.gif"; //$NON-NLS-1$
	public static final String PANEL_COLLAPSE_ALL = PREFIX + "panel/collapseall.gif"; //$NON-NLS-1$
	public static final String PANEL_SELECT_ALL = PREFIX + "panel/selectall.gif"; //$NON-NLS-1$
	public static final String PANEL_SELECT_ALL_DISABLED = PREFIX
			+ "panel/d-selectall.gif"; //$NON-NLS-1$
	public static final String PANEL_DESELECT_ALL = PREFIX + "panel/deselectall.gif"; //$NON-NLS-1$
	public static final String PANEL_DESELECT_ALL_DISABLED = PREFIX
			+ "panel/d-deselectall.gif"; //$NON-NLS-1$
	public static final String PANEL_FILTER = PREFIX + "panel/filter.gif"; //$NON-NLS-1$

	//
	// Preferences
	//
	private static final String PREFIX_PREFERENCES = PREFIX + "preferences/"; //$NON-NLS-1$
	public static final String PREFERENCES_SCOPE_UNKNOWN = PREFIX_PREFERENCES
			+ "scope_unknown.gif"; //$NON-NLS-1$
	public static final String PREFERENCES_SCOPE_INSTANCE = PREFIX_PREFERENCES
			+ "scope_instance.gif"; //$NON-NLS-1$
	public static final String PREFERENCES_SCOPE_PROJECT = PREFIX_PREFERENCES
			+ "scope_project.gif"; //$NON-NLS-1$
	public static final String PREFERENCES_NODE = PREFIX_PREFERENCES + "pref_node.gif"; //$NON-NLS-1$
	public static final String PREFERENCES_DATA = PREFIX_PREFERENCES + "pref_data.gif"; //$NON-NLS-1$
	public static final String PREFERENCES_IMPORT = PREFIX_PREFERENCES + "import.gif"; //$NON-NLS-1$
	public static final String SETTINGS = PREFIX_PREFERENCES + "settings.gif"; //$NON-NLS-1$

	public static final String SECURE_PREFERENCE_NODE = PREFIX_PREFERENCES
			+ "secure_pref_node.gif"; //$NON-NLS-1$

	//
	// Launching
	//
	private static final String PREFIX_LAUNCH = PREFIX + "launching/"; //$NON-NLS-1$

	public static final String SCENARIO_WAIT = PREFIX_LAUNCH + "test.gif"; //$NON-NLS-1$
	public static final String SCENARIO_RUN = PREFIX_LAUNCH + "testrun.gif"; //$NON-NLS-1$
	public static final String SCENARIO_STOP = PREFIX_LAUNCH + "teststop.gif"; //$NON-NLS-1$
	public static final String SCENARIO_FAIL = PREFIX_LAUNCH + "testerr.gif"; //$NON-NLS-1$
	public static final String SCENARIO_FAIL_STATISTIC = PREFIX_LAUNCH + "error_ovr.gif"; //$NON-NLS-1$
	public static final String SCENARIO_STOP_STATISTIC = PREFIX_LAUNCH + "stop_ovr.gif"; //$NON-NLS-1$
	public static final String SCENARIO_PASS = PREFIX_LAUNCH + "testok.gif"; //$NON-NLS-1$
	public static final String SCENARIO_PASS_STATISTIC = PREFIX_LAUNCH
			+ "success_ovr.gif"; //$NON-NLS-1$
	public static final String CONTEXT_WAIT = PREFIX_LAUNCH + "context.gif"; //$NON-NLS-1$
	public static final String CONTEXT_RUN = PREFIX_LAUNCH + "contextrun.gif"; //$NON-NLS-1$
	public static final String CONTEXT_STOP = PREFIX_LAUNCH + "contextstop.gif"; //$NON-NLS-1$
	public static final String CONTEXT_FAIL = PREFIX_LAUNCH + "contexterr.gif"; //$NON-NLS-1$
	public static final String CONTEXT_PASS = PREFIX_LAUNCH + "contextok.gif"; //$NON-NLS-1$
	public static final String HISTORY = PREFIX_LAUNCH + "history_list.gif"; //$NON-NLS-1$
	public static final String RUN_FAILED = PREFIX_LAUNCH + "run_failed.gif"; //$NON-NLS-1$
	public static final String RUN_FAILED_D = PREFIX_LAUNCH + "d_run_failed.gif"; //$NON-NLS-1$

	public static final String VERIFICATION_STOP = PREFIX_LAUNCH + "verification-stop.png"; //$NON-NLS-1$
	public static final String VERIFICATION_WAIT = PREFIX_LAUNCH + "verification.gif"; //$NON-NLS-1$
	public static final String VERIFICATION_RUN = PREFIX_LAUNCH + "verification-run.png"; //$NON-NLS-1$
	public static final String VERIFICATION_PASS = PREFIX_LAUNCH + "verification-pass.png"; //$NON-NLS-1$
	public static final String VERIFICATION_FAIL = PREFIX_LAUNCH + "verification-fail.png"; //$NON-NLS-1$

	public static final String EXECUTION_SESSION = PREFIX_LAUNCH + "tsuite.gif"; //$NON-NLS-1$
	public static final String EXECUTION_SESSION_RUN = PREFIX_LAUNCH + "tsuiterun.gif"; //$NON-NLS-1$
	public static final String EXECUTION_SESSION_FAIL = PREFIX_LAUNCH + "tsuiteerror.gif"; //$NON-NLS-1$
	public static final String EXECUTION_SESSION_OK = PREFIX_LAUNCH + "tsuiteok.gif"; //$NON-NLS-1$
	public static final String EXECUTION_SESSION_STOP = PREFIX_LAUNCH + "tsuitestop.gif"; //$NON-NLS-1$	
	public static final String CONFIG = PREFIX_LAUNCH + "externalEclipse.gif"; //$NON-NLS-1$
	public static final String CONFIG_RUNNING = PREFIX_LAUNCH + "running.gif"; //$NON-NLS-1$

	public static final String APP_RUN = PREFIX_LAUNCH + "run_exc.gif"; //$NON-NLS-1$
	public static final String APP_STOP = PREFIX_LAUNCH + "terminate_co.gif"; //$NON-NLS-1$
	public static final String BREAKPOINT = PREFIX_LAUNCH + "breakpoint.gif"; //$NON-NLS-1$

	//
	// Recording
	//
	private static final String PREFIX_RECORD = PREFIX + "recording/"; //$NON-NLS-1$

	public static final String CLEAR = PREFIX_RECORD + "clear.gif"; //$NON-NLS-1$
	public static final String CLEAR_D = PREFIX_RECORD + "d_clear.gif"; //$NON-NLS-1$
	public static final String PLAY = PREFIX_RECORD + "play.gif"; //$NON-NLS-1$
	public static final String PLAY_D = PREFIX_RECORD + "d_play.gif"; //$NON-NLS-1$
	public static final String STOP = PREFIX_RECORD + "stop.gif"; //$NON-NLS-1$
	public static final String STOP_D = PREFIX_RECORD + "d_stop.gif"; //$NON-NLS-1$
	public static final String RECORD = PREFIX_RECORD + "record.gif"; //$NON-NLS-1$
	public static final String RECORD_D = PREFIX_RECORD + "d_record.gif"; //$NON-NLS-1$

	public static final String COLLAPSEALL = PREFIX_RECORD + "collapseall.gif"; //$NON-NLS-1$
	public static final String SCROLL_LOCK = PREFIX_RECORD + "lock_co.gif"; //$NON-NLS-1$

	//
	// General
	//
	public static final String ECLIPSE = PREFIX + "eclipse.gif"; //$NON-NLS-1$
	public static final String MAIN_TAB = PREFIX + "main_tab.gif"; //$NON-NLS-1$
	public static final String ASSERT = PREFIX + "assert.gif"; //$NON-NLS-1$
	public static final String Q7_META = PREFIX + "q7.png"; //$NON-NLS-1$

	// Overlays
	public static final String OVERLAY_PREFIX = PREFIX + "overlay/"; //$NON-NLS-1$
	public static final String OVERLAY_RUNNING = OVERLAY_PREFIX + "run_co.gif"; //$NON-NLS-1$
	public static final String OVERLAY_ERROR = OVERLAY_PREFIX + "error_co.gif"; //$NON-NLS-1$
	public static final String OVERLAY_WARNING = OVERLAY_PREFIX + "warning_co.gif"; //$NON-NLS-1$
	public static final String OVERLAY_SUPER_CTX = OVERLAY_PREFIX + "super_ctx.gif"; //$NON-NLS-1$

	// ECL
	public static final String ECL_PREFIX = PREFIX + "ecl/"; //$NON-NLS-1$
	public static final String ECL_COMMAND = ECL_PREFIX + "cmd.gif"; //$NON-NLS-1$
	public static final String ECL_ATTRIBUTE = ECL_PREFIX + "attr.gif"; //$NON-NLS-1$
	public static final String ECL_INPUT_ATTRIBUTE = ECL_PREFIX + "input_attr.gif"; //$NON-NLS-1$
	public static final String ECL_PROC = ECL_PREFIX + "proc.gif"; //$NON-NLS-1$
	public static final String ECL_LOCAL = ECL_PREFIX + "local.gif"; //$NON-NLS-1$
	public static final String ECL_GLOBAL = ECL_PREFIX + "global.gif"; //$NON-NLS-1$

	public static final String REPORT_PREFIX = PREFIX + "reporting/"; //$NON-NLS-1$
	public static final String ZOOM_IN = REPORT_PREFIX + "zoom_in.gif"; //$NON-NLS-1$
	public static final String ZOOM_OUT = REPORT_PREFIX + "zoom_out.gif"; //$NON-NLS-1$

	public synchronized static Image getImage(String key) {
		ImageRegistry registry = Q7UIPlugin.getDefault().getImageRegistry();
		Image image = registry.get(key);
		if (image == null || image.isDisposed()) {
			registry.remove(key);
			registry.put(key, getImageDescriptor(key));
			image = registry.get(key);
		}
		return image;
	}

	public synchronized static Image getOverlayImageBottomRight(String key, String decor) {
		ImageRegistry registry = Q7UIPlugin.getDefault().getImageRegistry();
		Image image = registry.get(key + ".decorator.br" + decor); //$NON-NLS-1$
		if (image == null) {
			ImageDescriptor descriptor = getOverlayImageDescriptor(key, new String[] {
					null, null, null, decor, null });
			image = descriptor.createImage();
			registry.put(key + ".decorator.br" + decor, image); //$NON-NLS-1$
		}
		return image;
	}

	public synchronized static ImageDescriptor getOverlayImageBottomRight(
			ImageDescriptor original, String key, String decor) {
		ImageRegistry registry = Q7UIPlugin.getDefault().getImageRegistry();
		ImageDescriptor image = registry.getDescriptor(key + ".decorator.br" + decor); //$NON-NLS-1$
		if (image == null) {
			ImageDescriptor descriptor = getOverlayImageDescriptor(original, key,
					new String[] { null, null, null, decor, null });
			registry.put(key + ".decorator.br" + decor, descriptor); //$NON-NLS-1$
		}
		return image;
	}

	public synchronized static Image getOverlayImageBottomLeft(String key,
			String descriptorKey) {
		ImageRegistry registry = Q7UIPlugin.getDefault().getImageRegistry();
		Image image = registry.get(key + ".decorator.bl" + descriptorKey); //$NON-NLS-1$
		if (image == null) {
			ImageDescriptor descriptor = getOverlayImageDescriptor(key, new String[] {
					null, null, descriptorKey, null, null });
			image = descriptor.createImage();
			registry.put(key + ".decorator.bl" + descriptorKey, image); //$NON-NLS-1$
		}
		return image;
	}

	private synchronized static ImageDescriptor getOverlayImageDescriptor(String key,
			String[] descriptorKeys) {
		ImageDescriptor[] decorators = new ImageDescriptor[descriptorKeys.length];
		for (int i = 0; i < decorators.length; i++) {
			if (descriptorKeys[i] != null) {
				decorators[i] = getImageDescriptor(descriptorKeys[i]);
			}
		}
		return new DecorationOverlayIcon(getImage(key), decorators);
	}

	private synchronized static ImageDescriptor getOverlayImageDescriptor(
			ImageDescriptor original, String key, String[] descriptorKeys) {
		ImageDescriptor[] decorators = new ImageDescriptor[descriptorKeys.length];
		for (int i = 0; i < decorators.length; i++) {
			if (descriptorKeys[i] != null) {
				decorators[i] = getImageDescriptor(descriptorKeys[i]);
			}
		}
		return new DecorationOverlayIcon(original.createImage(), decorators);
	}

	public static ImageDescriptor getImageDescriptor(String key) {
		return Q7UIPlugin.imageDescriptorFromPlugin(Q7UIPlugin.PLUGIN_ID, key);
	}

}
