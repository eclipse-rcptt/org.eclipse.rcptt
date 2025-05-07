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
package org.eclipse.rcptt.internal.core.model.cache;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.internal.core.model.ModelInfo;

@SuppressWarnings("rawtypes")
public class ModelCache {
	public static final int DEFAULT_PROJECT_SIZE = 5; // average 25552 bytes
	// public static final int DEFAULT_ROOT_SIZE = 50; // average 2590 bytes per

	// public static final int DEFAULT_PKG_SIZE = 500; // average 1782 bytes per

	public static final int DEFAULT_OPENABLE_SIZE = 100; // average 6629

	public static final int DEFAULT_CHILDREN_SIZE = 100 * 20; // average 20

	protected ModelInfo modelInfo;

	protected ElementCache openableCache;
	protected HashMap projectCache;

	protected Map childrenCache;

	public ModelCache() {

		long maxMemory = Runtime.getRuntime().maxMemory();
		double ratio = maxMemory == Long.MAX_VALUE ? 4d : maxMemory / 64000000; // 64000000
		this.projectCache = new HashMap(DEFAULT_PROJECT_SIZE); // NB: Don't use
		this.openableCache = new ElementCache((int) (DEFAULT_OPENABLE_SIZE * ratio));
		this.childrenCache = new HashMap((int) (DEFAULT_CHILDREN_SIZE * ratio));
	}

	/**
	 * Returns the info for the element.
	 */
	public Object getInfo(IQ7Element element) {
		switch (element.getElementType()) {
		case Model:
			return this.modelInfo;
		case Project:
			return this.projectCache.get(element);
		case TestCase:
		case Context:
		case TestSuite:
		case Verification:
		case ProjectMetadata:
			return this.openableCache.get(element);
		default:
			return this.childrenCache.get(element);
		}
	}

	/**
	 * Returns the info for this element without disturbing the cache ordering.
	 */
	public Object peekAtInfo(IQ7Element element) {
		switch (element.getElementType()) {
		case Model:
			return this.modelInfo;
		case Project:
			return this.projectCache.get(element);
		case TestCase:
		case Context:
		case TestSuite:
		case Verification:
		case ProjectMetadata:
			return this.openableCache.peek(element);
		default:
			return this.childrenCache.get(element);
		}
	}

	@SuppressWarnings("unchecked")
	public void putInfo(IQ7Element element, Object info) {
		switch (element.getElementType()) {
		case Model:
			this.modelInfo = (ModelInfo) info;
			break;
		case Project:
			this.projectCache.put(element, info);
			break;
		case Context:
		case TestCase:
		case TestSuite:
		case Verification:
		case ProjectMetadata:
			this.openableCache.put(element, info);
			break;
		default:
			this.childrenCache.put(element, info);
		}
	}

	public void removeInfo(IQ7Element element) {
		switch (element.getElementType()) {
		case Model:
			this.modelInfo = null;
			break;
		case Project:
			this.projectCache.remove(element);
			break;
		case TestCase:
		case Context:
		case TestSuite:
		case Verification:
		case ProjectMetadata:
			this.openableCache.remove(element);
			break;
		default:
			this.childrenCache.remove(element);
		}
	}
}
