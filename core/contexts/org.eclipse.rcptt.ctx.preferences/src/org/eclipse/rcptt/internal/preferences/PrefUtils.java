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
package org.eclipse.rcptt.internal.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IExportedPreferences;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.rcptt.preferences.PrefData;
import org.eclipse.rcptt.preferences.PrefNode;
import org.eclipse.rcptt.preferences.PreferencesContext;
import org.eclipse.rcptt.preferences.PreferencesFactory;
import org.eclipse.rcptt.preferences.SettingsNode;
import org.eclipse.rcptt.preferences.StringPrefData;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public final class PrefUtils {
	
	private static final SubstitutionHelper helper = resolveHelper();
	public static final String DIALOG_SETTINGS_NODE_NAME = "settings";

	private PrefUtils() {
	}

	public static void remove(EObject object) {
		EReference ref = object.eContainmentFeature();
		List<?> list = (List<?>) object.eContainer().eGet(ref);
		list.remove(object);
	}

	public static PrefNode convertPreferences(Preferences preferences)
			throws BackingStoreException {
		return doConvertPreferences(preferences);
	}
	
	public static PreferencesContext toContext(IExportedPreferences preferences) throws BackingStoreException {
		PrefNode prefNode = PrefUtils.convertPreferences(preferences);
		PreferencesContext context = PreferencesFactory.eINSTANCE
				.createPreferencesContext();
		if (prefNode != null) {
			for (PrefNode i: new ArrayList<>(prefNode.getChilds())) {
				if ("settings".equals(i.getName())) {
					SettingsNode settings = PreferencesFactory.eINSTANCE
							.createSettingsNode();
					settings.getChilds().addAll(i.getChilds());
					context.setSettings(settings);
				} else {
					context.getContent().add(i);
				}
			}
		}
		return context;
	}

	public static void encodePrefData(StringPrefData prefData) {
		String value = prefData.getValue();

		CharSequence nullSymbol = Character.toString('\0');
		if (value.contains(nullSymbol)) {
			value = value.replace(nullSymbol, "\\u0000");
			// System.out.println(value);
			prefData.setValue(value);
		}
	}

	public static void encodeWorkspaceLocation(StringPrefData prefData) {
		prefData.setValue(encodeWorkspaceLocation(prefData.getValue()));
	}

	public static String encodeWorkspaceLocation(String value) {
		if (value == null) {
			return null;
		}
		try {
			File location = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toFile();
			String absolutePath = location.getAbsolutePath();
			String canonicalPath = location.getCanonicalPath();
			String absolutePathSlash = absolutePath.replace('\\', '/');
			String canonicalPathSlash = canonicalPath.replace('\\', '/');
			if (value.contains(absolutePath)) {
				value = value.replace(absolutePath, "${workspace_loc}");
			}
			if (value.contains(canonicalPath)) {
				value = value.replace(canonicalPath, "${workspace_loc}");
			}
			if (value.contains(absolutePathSlash)) {
				value = value.replace(absolutePathSlash, "${workspace_loc}");
			}
			if (value.contains(canonicalPathSlash)) {
				value = value.replace(canonicalPathSlash, "${workspace_loc}");
			}
		} catch (Throwable e) {
			// Ignore
		}
		return value;
	}

	public static void decodeWorkspaceLocation(StringPrefData prefData) {
		prefData.setValue(decodeWorkspaceLocation(prefData.getValue()));
	}

	public static String decodeWorkspaceLocation(String value) {
		if (value == null) {
			return null;
		}
		try {
			File location = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toFile();
			String canonicalPath = location.getCanonicalPath();
			if (value.contains("${workspace_loc}")) {
				value = value.replace("${workspace_loc}", canonicalPath);
			}
		} catch (Throwable e) {
			// Ignore
		}
		return value;
	}

	public static void decodePrefData(StringPrefData prefData) {
		String value = prefData.getValue();

		if (value.contains("\\u0000")) {
			value = value.replace("\\u0000", Character.toString('\0'));
			prefData.setValue(value);
		}
	}

	public static void substituteVariables(StringPrefData prefData) {
		String key = substituteVariables(prefData.getKey());
		prefData.setKey(key);
		String value = substituteVariables(prefData.getValue());
		prefData.setValue(value);
	}

	public static String substituteVariables(String string) {
		return helper.substituteVariables(string);
	}

	private static PrefNode doConvertPreferences(Preferences preferences)
			throws BackingStoreException {

		List<PrefNode> childPrefNodes = doConvertChildPreferences(preferences);
		List<PrefData> prefDatas = collectPrefData(preferences);

		if (childPrefNodes.size() > 0 || prefDatas.size() > 0) {
			PrefNode prefNode = PreferencesFactory.eINSTANCE.createPrefNode();
			prefNode.setName(preferences.name());
			prefNode.getChilds().addAll(childPrefNodes);
			prefNode.getData().addAll(prefDatas);

			return prefNode;
		}

		return null;
	}

	private static List<PrefData> collectPrefData(Preferences preferences)
			throws BackingStoreException {
		List<PrefData> result = new ArrayList<PrefData>();

		String[] keys = preferences.keys();
		for (String key : keys) {
			String value = preferences.get(key, null);

			if (value != null) {
				// Replace workspace path with
				StringPrefData prefData = PreferencesFactory.eINSTANCE
						.createStringPrefData();
				prefData.setKey(key);
				prefData.setValue(value);
				encodeWorkspaceLocation(prefData);
				encodePrefData(prefData);
				result.add(prefData);
			}
		}
		return result;
	}

	private static List<PrefNode> doConvertChildPreferences(
			Preferences preferences) throws BackingStoreException {
		List<PrefNode> prefNodes = new ArrayList<PrefNode>();

		String[] childrenNames = preferences.childrenNames();
		for (String childrenName : childrenNames) {
			Preferences childPreferences = preferences.node(childrenName);
			PrefNode prefNode = doConvertPreferences(childPreferences);
			if (prefNode != null) {
				prefNodes.add(prefNode);
			}
		}

		return prefNodes;
	}
	
	private interface SubstitutionHelper {

		public String substituteVariables(String input);

	}
	
	private static SubstitutionHelper resolveHelper() {
		try {
			// try to load the class
			VariablesPlugin.getDefault();
			
			return new SubstitutionHelper() {

				@Override
				public String substituteVariables(String input) {
					try {
						return VariablesPlugin
								.getDefault()
								.getStringVariableManager()
								.performStringSubstitution(input, true);
					} catch (CoreException e) {
						Activator.log(e);
					}
					return input;
				}

			};
		} catch (NoClassDefFoundError e) {
			// go to the fallback below
		}
		
		return new SubstitutionHelper() {

			@Override
			public String substituteVariables(String input) {
				return input;
			}

		};
	}
	
}
