/*******************************************************************************
 * Copyright (c) 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.osgi.framework.Version;

import com.google.common.collect.ListMultimap;

public final class DependencyResolver {

	private final ListMultimap<String, IPluginModelBase> dependecies;
	private final Map<IPluginModelBase, Boolean> visit = new HashMap<>();

	public DependencyResolver(ListMultimap<String, IPluginModelBase> fAllBundles) {
		Objects.requireNonNull(fAllBundles);
		this.dependecies = fAllBundles;
	}

	public Collection<IPluginModelBase> checkPlugins(Collection<IPluginModelBase> toCheckDeps) {
		Objects.requireNonNull(toCheckDeps);
		visit.clear();
		List<IPluginModelBase> toRemove = new ArrayList<>();

		toCheckDeps.forEach(dep -> {
			if (!checkPlugin(dep)) {
				toRemove.add(dep);
			}
		});
		return Collections.unmodifiableList(toRemove);
	}

	private boolean checkPlugin(IPluginModelBase plugin) {
		if (plugin == null) {
			return false;
		}
		if (visit.containsKey(plugin)) {
			return visit.get(plugin);
		}

		for (BundleSpecification dep : plugin.getBundleDescription().getRequiredBundles()) {
			if (dep.isOptional()) {
				continue;
			}
			if (!dependecies.containsKey(dep.getName())) {
				visit.put(plugin, false);
				return false;
			}
			
			List<IPluginModelBase> candidates = dependecies.get(dep.getName()).stream().filter(p -> dep.getVersionRange().isIncluded(new Version(p.getPluginBase().getVersion()))).toList();
			if (candidates.isEmpty()){
				visit.put(plugin, false);
				return false;
			}
			visit.put(plugin, true);
			
			if (!candidates.stream().anyMatch(this::checkPlugin)) {
				visit.put(plugin, false);
				return false;
			}
		}

		visit.put(plugin, true);
		return true;
	}

}
