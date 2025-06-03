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
package org.eclipse.rcptt.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

public class ContextTypeManager {

	private static ContextTypeManager instance;

	public static ContextTypeManager getInstance() {
		if (instance == null) {
			instance = new ContextTypeManager();
		}
		return instance;
	}

	private static final String EXPT_ID = RcpttPlugin.PLUGIN_ID + ".contextTypes";

	private static final String PROCESSOR_EXPT_ID = RcpttPlugin.PLUGIN_ID
			+ ".contextProcessors";

	private static final String MAKER_EXPT_ID = RcpttPlugin.PLUGIN_ID
			+ ".contextMaker";

	private static final String ATTR_TYPE = "type";
	private static final String ATTR_CLASS = "class";

	private static Map<String, String> legacyTypes = new HashMap<String, String>();
	static {
		legacyTypes.put("com.xored.q7.debug.context", "org.eclipse.rcptt.ctx.debug");
		legacyTypes.put("com.xored.q7.filesystem.context", "org.eclipse.rcptt.ctx.filesystem");
		legacyTypes.put("com.xored.q7.ui.context.group", "org.eclipse.rcptt.ctx.group");
		legacyTypes.put("com.xored.q7.ui.context.superc", "org.eclipse.rcptt.ctx.super");
		legacyTypes.put("com.xored.q7.ui.context.workbench", "org.eclipse.rcptt.ctx.workbench");
		legacyTypes.put("com.xored.q7.workspace.context", "org.eclipse.rcptt.ctx.workspace");
		legacyTypes.put("com.xored.q7.parameters.context", "org.eclipse.rcptt.ctx.parameters");
		legacyTypes.put("com.xored.q7.preferences.context", "org.eclipse.rcptt.ctx.preferences");
		legacyTypes.put("com.xored.q7.ecl.context", "org.eclipse.rcptt.ctx.ecl");
	}

	public ContextType getTypeByContext(Context context) {
		init();
		return classToType.get(context.eClass());
	}

	public ContextType getTypeById(String id) {
		init();
		if (legacyTypes.containsKey(id)) {
			id = legacyTypes.get(id);
		}
		return idToType.get(id);
	}

	public ContextType[] getTypes() {
		init();
		return types;
	}

	/**
	 * Apply context
	 *
	 * @param context
	 * @return
	 * @throws CoreException
	 */
	public void apply(Context context, ISession session) throws CoreException {
		ContextType type = getTypeByContext(context);
		IContextProcessor processor = getProcessor(type.getId());
		if (processor == null) {
			throw new CoreException(
					RcpttPlugin.createStatus("Failed to locate processor for context:"
							+ context.getId()));
		}

		if (processor instanceof IEclAwareProcessor) {
			((IEclAwareProcessor) processor).apply(context, session);
		} else {
			processor.apply(context, () -> session.isClosed());
		}
	}

	public Context createContext(String contextType, EObject param)
			throws CoreException {
		ContextType type = getTypeById(contextType);
		IContextProcessor processor = getProcessor(type.getId());
		return processor.create(param);
	}

	public synchronized IContextMaker getMaker(String contextTypeId) {
		if (idToMaker == null) {
			idToMaker = new HashMap<String, IContextMaker>();
			IConfigurationElement[] elements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(MAKER_EXPT_ID);
			for (IConfigurationElement element : elements) {
				try {
					String id = element.getAttribute(ATTR_TYPE);
					IContextMaker processor = (IContextMaker) element
							.createExecutableExtension(ATTR_CLASS);
					idToMaker.put(id, processor);
				} catch (CoreException e) {
					RcpttPlugin.log(e);
				}
			}
		}
		IContextMaker maker = idToMaker.get(contextTypeId);
		if (maker == null) {
			return new DefaultContextMaker();
		}
		return maker;
	}

	public synchronized IContextProcessor getProcessor(String contextTypeId) {
		if (idToProcessor == null) {
			idToProcessor = new HashMap<String, IContextProcessor>();
			IConfigurationElement[] elements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(PROCESSOR_EXPT_ID);
			for (IConfigurationElement element : elements) {
				try {
					String id = element.getAttribute(ATTR_TYPE);
					IContextProcessor processor = (IContextProcessor) element
							.createExecutableExtension(ATTR_CLASS);
					idToProcessor.put(id, processor);
				} catch (CoreException e) {
					RcpttPlugin.log(e);
				}
			}
		}
		return idToProcessor.get(contextTypeId);
	}

	private synchronized void init() {
		if (classToType != null) {
			return;
		}
		List<ContextType> list = new ArrayList<ContextType>();
		classToType = new HashMap<EClass, ContextType>();
		idToType = new HashMap<String, ContextType>();

		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXPT_ID);
		for (IConfigurationElement element : elements) {
			try {
				ContextType type = new ContextType(element);
				list.add(type);
				classToType.put(type.getEClass(), type);
				idToType.put(type.getId(), type);
			} catch (CoreException e) {
				RcpttPlugin.log(e);
			}
		}
		types = list.toArray(new ContextType[list.size()]);
		Arrays.sort(types, new Comparator<ContextType>() {
			public int compare(ContextType t1, ContextType t2) {
				return -((Integer) t1.getPriority()).compareTo(t2.getPriority());
			}
		});
	}

	private ContextType[] types;
	private Map<EClass, ContextType> classToType;
	private Map<String, ContextType> idToType;
	private Map<String, IContextProcessor> idToProcessor;
	private Map<String, IContextMaker> idToMaker;
}
