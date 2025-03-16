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
package org.eclipse.rcptt.ctx.preferences.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.rcptt.core.IContextProcessor;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.preferences.PrefUtils;
import org.eclipse.rcptt.preferences.PrefData;
import org.eclipse.rcptt.preferences.PrefNode;
import org.eclipse.rcptt.preferences.PreferencesContext;
import org.eclipse.rcptt.preferences.PreferencesFactory;
import org.eclipse.rcptt.preferences.SecurePrefNode;
import org.eclipse.rcptt.preferences.SettingsNode;
import org.eclipse.rcptt.preferences.StringPrefData;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.tesla.ecl.impl.UIRunnable;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer;
import org.eclipse.rcptt.tesla.internal.ui.player.UIJobCollector;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class PreferencesContextProcessor implements IContextProcessor {

	private static final String DIALOG_SETTINGS_NODE_NAME = "settings";
	private static final String SECURE_PREFERENCES_NODE_NAME = "secureStorage";

	private static final String[] EXCLUDE_SCOPE_LIST = new String[] {
			DefaultScope.SCOPE, ConfigurationScope.SCOPE };

	public void apply(final Context contextToApply) throws CoreException {
		final UIJobCollector collector = new UIJobCollector();
		Job.getJobManager().addJobChangeListener(collector);
		SWTUIPlayer.disableMessageDialogs();
		try {
			UIRunnable.exec(new UIRunnable<Object>() {
				@Override
				public Object run() throws CoreException {
					collector.enable();
					doApply((PreferencesContext) contextToApply);
					return null;
				}
			});
			UIRunnable.exec(new UIRunnable<Object>() {
				@Override
				public Object run() throws CoreException {
					collector.setNeedDisable();
					return null;
				}
			});
			collector.join(TeslaLimits.getJobTimeout());
		} catch (Exception e) {
			CoreException ee = new CoreException(RcpttPlugin.createStatus(
					"Failed to execute context: " + contextToApply.getName()
							+ " Cause: " + e.getMessage(), e));
			RcpttPlugin.log(e);
			throw ee;
		} finally {
			SWTUIPlayer.enableMessageDialogs();
			Job.getJobManager().removeJobChangeListener(collector);
		}
	}

	public Context create(EObject param) throws CoreException {
		UIRunnable.exec(new UIRunnable<PreferencesContext>() {
			@Override
			public PreferencesContext run() throws CoreException {
				// Wait for available moment
				return null;
			}
		});
		return doCreate();
	}

	private void doApply(PreferencesContext context) throws CoreException {
		IPreferencesService service = Platform.getPreferencesService();
		IEclipsePreferences rootPreferences = service.getRootNode();

		if (context.isCleanPreferences()) {
			// Clear all dialog settings
			Bundle[] bundles = Activator.getDefault().getBundle()
					.getBundleContext().getBundles();
			for (Bundle bundle : bundles) {
				DialogSettingsUtils.clearDialogSettings(bundle);
			}
		}
		HashSet<String> touchedNodes = new HashSet<>();
		try {
			for (PrefNode prefNode : context.getContent()) {
				try {
					if (prefNode instanceof SecurePrefNode) {
						try {
							SecuritySupport.applySecureSettings(
									(SecurePrefNode) prefNode,
									context.isCleanPreferences());
						} catch (Throwable e) {
							RcpttPlugin.log(e);
						}
					}
					String name = PrefUtils.substituteVariables(prefNode.getName());
					applyPreferences(name, prefNode, rootPreferences,	context.isCleanPreferences());
					touchedNodes.add(name);
					rootPreferences.flush();
				} catch (Throwable e) {
					String nodeName = prefNode.getName();
					if (nodeName.equals("project")) {
						// Report to not existed projects then try to apply
						// project preferences
						EList<PrefNode> childs = prefNode.getChilds();
						for (PrefNode child : childs) {
							try {
								ResourcesSupport
										.checkProjectExistance(child, e);
							} catch (CoreException e4) {
								throw e4;
							} catch (Throwable e3) {
								RcpttPlugin.log(e3);
							}
						}
					}
					throw new CoreException(Activator.createStatus(
							"Error during preferences apply for node: \""
									+ nodeName + "\" caused by: ("
									+ e.getMessage() + ")", e));
					// try recover preferences node
				}
			}

			SettingsNode settingsNode = context.getSettings();
			if (settingsNode != null) {
				for (PrefNode prefNode : settingsNode.getChilds()) {
					applyDialogSettings(prefNode);
				}
			}
			
			if (context.isCleanPreferences()) {
				// If values or nodes are removed and then re-added event listeners that require a value to always be present and valid fail
				// We have to set new values immediately, without removing nodes first
				// To cleanup unneeded nodes, we first compute and visit all needed ones and remove all the rest.
				for (String key: rootPreferences.keys()) {
					if (!touchedNodes.contains(key)) {
						applyPreferences(key, PreferencesFactory.eINSTANCE.createPrefNode(), rootPreferences,	context.isCleanPreferences());
					}
				}
			}

			rootPreferences.flush();
		} catch (IOException e) {
			throw new CoreException(Activator.createStatus(
					"Error during dialog settings applying.", e));
		} catch (BackingStoreException e) {
			throw new CoreException(Activator.createStatus(
					"Error during dialog settings applying.", e));
		}
	}

	private String[] getPDECoreRequiredPreferencesList() {
		String keys[] = new String[] { "org.eclipse.pde.ui.arch", "vm_args",
				"program_args", "org.eclipse.pde.ui.ws",
				"workspace_target_handle", "platform_path",
				"org.eclipse.pde.ui.os", "target_mode", "target.profile",
				"org.eclipse.pde.ui.nl", "pooled_urls", "pooled_bundles" };
		return keys;
	}

	private String[] getJDTCoreRequiredPreferencesList() {
		String keys[] = new String[] {
				"org.eclipse.jdt.core.classpathVariable.JRE_LIB",
				"org.eclipse.jdt.core.classpathVariable.JRE_SRC",
				"org.eclipse.jdt.core.classpathVariable.JRE_SRCROOT" };
		return keys;
	}

	private PreferencesContext doCreate() throws CoreException {
		try {
			final PreferencesContext context = PreferencesFactory.eINSTANCE
					.createPreferencesContext();
			List<PrefNode> prefNodes = collectPreferences();
			context.getContent().addAll(prefNodes);

			SettingsNode settings = collectDialogSettings();
			if (settings.getChilds().size() > 0) {
				settings.setName(DIALOG_SETTINGS_NODE_NAME);
				context.setSettings(settings);
			}
			try {
				SecurePrefNode secureNode = SecuritySupport
						.collectSecureSettings();
				if (secureNode.getChilds().size() > 0) {
					secureNode.setName(SECURE_PREFERENCES_NODE_NAME);
					context.getContent().add(secureNode);
				}
			} catch (Throwable e) {
				Activator.log(e);
			}
			return context;
		} catch (BackingStoreException e) {
			throw new CoreException(Activator.createStatus(
					"Error during preferences processing.", e));
		} catch (IOException e) {
			throw new CoreException(Activator.createStatus(
					"Error during dialog settings processing.", e));
		}
	}

	public boolean isApplied(Context context) {
		throw new UnsupportedOperationException();
	}

	public boolean isCreateAllowed() {
		return true;
	}

	private void applyPreferences(String nodeName, PrefNode currentPrefNode,
			Preferences parentPreferences, boolean clean)
			throws BackingStoreException {
		Preferences preferences = parentPreferences.node(nodeName);

		Set<String> datasSet = new HashSet<String>();
		if (nodeName.equals("org.eclipse.pde.core")) {
			datasSet.addAll(Arrays.asList(getPDECoreRequiredPreferencesList()));
		}
		if (nodeName.equals("org.eclipse.jdt.core")) {
			datasSet.addAll(Arrays.asList(getJDTCoreRequiredPreferencesList()));
		}

		preferences = parentPreferences.node(nodeName);

		EList<PrefData> datas = currentPrefNode.getData();
		for (PrefData data : datas) {
			StringPrefData prefData = (StringPrefData) data;
			if (prefData
					.getKey()
					.startsWith(
							"org.eclipse.ui.internal.views.markers.CachedMarkerBuilder")) {
				Bundle ideBundle = Platform.getBundle("org.eclipse.ui.ide");
				// eclipse 3.4 compatibility:
				// getVersion().toString() replaced with
				// getHeaders().get("Bundle-Version")
				if (ideBundle != null
						&& (ideBundle.getHeaders().get("Bundle-Version")
								.toString().startsWith("3.5") || ideBundle
								.getHeaders().get("Bundle-Version").toString()
								.startsWith("3.4"))) {
					continue;
				}
			}
			PrefUtils.decodePrefData(prefData);
			PrefUtils.decodeWorkspaceLocation(prefData);
			PrefUtils.substituteVariables(prefData);
			datasSet.add(prefData.getKey());
			try {
				preferences.put(prefData.getKey(), prefData.getValue());
			} catch (Throwable e) {
				Activator.log(e);
			}
		}
		
		if (clean) {
			for (String key: preferences.keys()) {
				if (!datasSet.contains(key)) {
					preferences.remove(key);
				}
			}
		}
		
		
		if (nodeName.equals("org.eclipse.ui.workbench")
				|| datasSet.contains("OPEN_ON_SINGLE_CLICK")) {
			processOpenStrategy(preferences);
		}

		datasSet.clear();
		EList<PrefNode> childs = currentPrefNode.getChilds();
		for (PrefNode child : childs) {
			try {
				String name = PrefUtils.substituteVariables(child.getName());
				datasSet.add(name);
				applyPreferences(name, child, preferences, false);
			} catch (Throwable e) {
				Activator.log(e);
			}
		}
		
		if (clean) {
			for (String key: preferences.childrenNames()) {
				if (!datasSet.contains(key)) {
					applyPreferences(key, PreferencesFactory.eINSTANCE.createPrefNode(), preferences, clean);
				}
			}
		}
	}

	private void processOpenStrategy(Preferences preferences) {
		// Check some special cases
		boolean openOnSingleClick = preferences.getBoolean(
				"OPEN_ON_SINGLE_CLICK", false);
		boolean selectOnHover = preferences
				.getBoolean("SELECT_ON_HOVER", false);
		boolean openAfterDelay = preferences.getBoolean("OPEN_AFTER_DELAY",
				false);
		int singleClickMethod = openOnSingleClick ? OpenStrategy.SINGLE_CLICK
				: OpenStrategy.DOUBLE_CLICK;
		if (openOnSingleClick) {
			if (selectOnHover) {
				singleClickMethod |= OpenStrategy.SELECT_ON_HOVER;
			}
			if (openAfterDelay) {
				singleClickMethod |= OpenStrategy.ARROW_KEYS_OPEN;
			}
		}
		OpenStrategy.setOpenMethod(singleClickMethod);
	}

	private List<PrefNode> collectPreferences() throws BackingStoreException {
		List<PrefNode> result = new ArrayList<PrefNode>();

		IPreferencesService service = Platform.getPreferencesService();
		Preferences root = service.getRootNode();
		String[] childNames = root.childrenNames();

		for (String childName : childNames) {
			Preferences child = root.node(childName);
			if (shouldProcess(child)) {
				PrefNode prefNode = PrefUtils.convertPreferences(child);
				if (prefNode != null) {
					result.add(prefNode);
				}
			}
		}

		return result;
	}

	private boolean shouldProcess(Preferences preferences) {
		for (String name : EXCLUDE_SCOPE_LIST) {
			if (name.equals(preferences.name())) {
				return false;
			}
		}
		return true;
	}

	private SettingsNode collectDialogSettings() throws IOException {
		SettingsNode settings = PreferencesFactory.eINSTANCE
				.createSettingsNode();

		settings.setName(DIALOG_SETTINGS_NODE_NAME);

		Bundle[] bundles = Activator.getDefault().getBundle()
				.getBundleContext().getBundles();

		for (Bundle bundle : bundles) {
			PrefNode prefNode = DialogSettingsUtils
					.collectDialogSettings(bundle);
			if (prefNode != null) {
				prefNode.setName(bundle.getSymbolicName());
				settings.getChilds().add(prefNode);
			}
		}
		return settings;
	}

	private void applyDialogSettings(PrefNode prefNode) throws IOException {
		Bundle bundle = Platform.getBundle(prefNode.getName());
		if (bundle != null) {
			DialogSettingsUtils.applyDialogSettings(bundle, prefNode);
		}
	}
}
