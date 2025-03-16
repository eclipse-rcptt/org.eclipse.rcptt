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
package org.eclipse.rcptt.ctx.preferences.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.common.util.EList;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.eclipse.rcptt.preferences.PrefData;
import org.eclipse.rcptt.preferences.PrefNode;
import org.eclipse.rcptt.preferences.StringPrefData;

public class PreferencesAdapterFactory implements IAdapterFactory {

	private static final Class<?>[] ADAPTER_LIST = new Class<?>[] { IPropertySource.class };

	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == IPropertySource.class
				&& adaptableObject instanceof PrefNode) {
			return adapterType.cast(new PrefNodePropertySource((PrefNode) adaptableObject));
		}
		return null;
	}

	public Class<?>[] getAdapterList() {
		return ADAPTER_LIST;
	}

	// TODO: add ListPrefData support
	private class PrefNodePropertySource implements IPropertySource {

		private final PrefNode prefNode;
		private IPropertyDescriptor[] descriptorsCache;
		private Map<String, PrefData> dataCache;

		public PrefNodePropertySource(PrefNode prefNode) {
			this.prefNode = prefNode;
		}

		public Object getEditableValue() {
			return null;
		}

		public IPropertyDescriptor[] getPropertyDescriptors() {
			EList<PrefData> datas = prefNode.getData();
			if (datas.size() > 0) {
				if (descriptorsCache == null) {
					initializeCaches();
				}
				return descriptorsCache;
			}
			return new IPropertyDescriptor[0];
		}

		public Object getPropertyValue(Object id) {
			PrefData data = getPrefData(id);
			if (data != null) {
				return ((StringPrefData) data).getValue();
			}
			return null;
		}

		public void setPropertyValue(Object id, Object value) {
			PrefData data = getPrefData(id);
			if (data != null && data instanceof StringPrefData) {
				((StringPrefData) data).setValue((String) value);
			}
		}

		public boolean isPropertySet(Object id) {
			return false;
		}

		public void resetPropertyValue(Object id) {
		}

		private PrefData getPrefData(Object key) {
			return dataCache.get(key);
		}

		private void initializeCaches() {
			EList<PrefData> datas = prefNode.getData();
			int len = datas.size();
			descriptorsCache = new IPropertyDescriptor[len];
			dataCache = new HashMap<String, PrefData>();
			for (int i = 0; i < len; i++) {
				PrefData data = datas.get(i);
				if (data instanceof StringPrefData) {
					descriptorsCache[i] = new TextPropertyDescriptor(data
							.getKey(), data.getKey());
					dataCache.put(data.getKey(), data);
				}
			}

		}
	}
}
