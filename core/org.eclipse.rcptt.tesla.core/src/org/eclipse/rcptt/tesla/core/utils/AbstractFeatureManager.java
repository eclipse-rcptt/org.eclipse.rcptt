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
package org.eclipse.rcptt.tesla.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Class to manager features and different recording/replay options for tesla.
 * New features should be placed here, and will be disabled by default.
 * 
 * Class should allow enabling routines via preferences. So some testing could
 * be performed.
 * 
 * Options are only runtime, and could not be persisted.
 * 
 * All options will be passed to tesla server, then invoke from client.
 * 
 */
public class AbstractFeatureManager {
	// Implementation
	public static class Option {
		private String description;
		private String value = "";
		private String defaultValue = "";
		private String name;
		private String values[];
		private String category = null;
		boolean editable = true;
		private int order = 0;
		Set<String> showIn = null;

		public Option() {
		}

		public boolean isEditable() {
			return editable;
		}

		public Option name(String name) {
			this.name = name;
			return this;
		}

		public Option order(int order) {
			this.order = order;
			return this;
		}

		public Option description(String value) {
			this.description = value;
			return this;
		}

		public Option category(String value) {
			this.category = value;
			return this;
		}

		public Option editable(boolean value) {
			this.editable = value;
			return this;
		}

		public Option value(String value) {
			this.value = value;
			return this;
		}

		public Option defaultValue(String defValue) {
			this.defaultValue = defValue;
			return this;
		}

		public Option values(String... values) {
			this.values = values;
			return this;
		}

		public Option showIn(String... value) {
			if (this.showIn == null) {
				this.showIn = new HashSet<String>();
			}
			for (String string : value) {
				this.showIn.add(string);
			}
			return this;
		}

		public String getDescription() {
			return description;
		}

		public String getValue() {
			return value;
		}

		public String[] getValues() {
			return values;
		}

		public String getName() {
			return name;
		}

		public String getCategory() {
			return category;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public boolean isShowIn(String value) {
			if (showIn == null) {
				return true;
			}
			return showIn.contains(value);
		}

		public int getOrder() {
			return order;
		}
	}

	public static final String[] BOOLEAN_VALUES = new String[] { "true",
			"false" };

	public static final String[] INT_VALUES = new String[] {};

	private Map<String, Option> options = new TreeMap<String, AbstractFeatureManager.Option>();
	private Map<String, String> replaceKeys = new HashMap<String, String>();

	public Option option(String id) {
		Option op = new Option();
		options.put(id, op);
		return op;
	}

	public Option boolOption(String id, boolean values) {
		Option op = new Option().values(BOOLEAN_VALUES)
				.defaultValue(Boolean.toString(values))
				.value(Boolean.toString(values));
		options.put(id, op);
		return op;
	}

	public Option intOption(String id, int defaultValue) {
		Option op = new Option().values(INT_VALUES)
				.defaultValue(Integer.toString(defaultValue))
				.value(Integer.toString(defaultValue));
		options.put(id, op);
		return op;
	}

	// Access
	public Option[] getOptions() {
		return options.values().toArray(new Option[options.size()]);
	}

	public Option getOption(String id) {
		if (options.containsKey(id)) {
			return options.get(id);
		}
		return null;
	}

	public String[] getOptionNames() {
		return options.keySet().toArray(new String[options.size()]);
	}

	public String getValue(String id) {
		Option op = getOption(id);
		if (op != null) {
			return op.getValue();
		}
		return null;
	}

	public int getIntValue(String id) {
		return Integer.parseInt(getOption(id).getValue());
	}

	public boolean isTrue(String id) {
		String value = getValue(id);
		if (value == null) {
			return false;
		}
		return "true".equalsIgnoreCase(value);
	}

	/**
	 * Store all values into key="value"
	 * 
	 * @param values
	 */
	public void storeValues(List<String> values) {
		Set<String> set = options.keySet();
		for (String key : set) {
			Option option = options.get(key);
			if (option.editable) {
				values.add(key + '=' + option.value);
			}
		}
	}

	public void setValues(List<String> values) {
		for (String vals : values) {
			int pos = vals.indexOf('=');
			String key = replaceKey(vals.substring(0, pos));
			String value = vals.substring(pos + 1);
			if (options.containsKey(key)) {
				Option option = options.get(key);
				option.value = value;
			}
		}
	}

	private String replaceKey(String key) {
		if (replaceKeys.containsKey(key)) {
			return replaceKeys.get(key);
		}
		return key;
	}

	public void saveToPreferences(IEclipsePreferences prefs) {
		Set<String> set = options.keySet();
		for (String key : set) {
			Option option = options.get(key);
			if (!option.editable || option.value.equals(option.defaultValue)) {
				prefs.remove(key);
				continue;
			}

			prefs.put(key, option.value);
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void loadFromPreferences(IEclipsePreferences prefs) {
		Set<String> set = options.keySet();
		for (String key : set) {
			key = replaceKey(key);
			Option option = options.get(key);
			if (!option.editable)
				continue;

			option.value = prefs.get(key, option.value);
		}
	}

	public void addReplace(String oldKey, String newKey) {
		replaceKeys.put(oldKey, newKey);
	}

}
