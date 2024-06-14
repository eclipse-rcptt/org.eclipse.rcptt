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
package org.eclipse.rcptt.tesla.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rcptt.tesla.core.ClosedException;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.internal.core.processing.ElementGenerator;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;

public class BasicElementMapper<T extends IBasicMappingNode> {
	protected Map<String, T> elements = new HashMap<String, T>();
	private Map<T, Element> backElements = new HashMap<T, Element>();
	private ElementGenerator generator = new ElementGenerator();
	
	public ElementGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(ElementGenerator generator) {
		this.generator = generator;
	}

	private String makeKey(Element uiElement) {
		if (uiElement != null) {
			return uiElement.getKind() + ":" + uiElement.getId();
		}
		return null;
	}

	/**
	 * Gets the UI element (like an {@link SWTUIElement}) corresponding to the
	 * {@link Element} identifier.
	 * <p>
	 * Generally, {@link SelectCommand} via some {@link ITeslaCommandProcessor}
	 * finds requesting UI element and stores it here by calling
	 * {@link #get(IBasicMappingNode)} method, then it returns generated Element
	 * identifier within the response.
	 */
	public T get(Element element) throws ClosedException {
		if (element == null) {
			return null;
		}
		T result = elements.get(makeKey(element));
		if (result == null) {
			throw new ClosedException();
		}
		return result;
	}

	public Element get(T result) {
		Element element = backElements.get(result);
		if (element != null) {
			return element;
		}
		Element resultElement = generator.generate(((IBasicMappingNode) result)
				.getGenerationKind());
		elements.put(makeKey(resultElement), result);
		backElements.put(result, resultElement);
		return resultElement;
	}

	public void put(T result, Element element) {
		elements.put(makeKey(element), result);
		backElements.put(result, element);
	}

	public void remove(Element element) {
		T remove = elements.remove(makeKey(element));
		if (remove != null) {
			backElements.remove(remove);
		}
	}

	public void clear() {
		this.backElements.clear();
		this.elements.clear();
		// generator = new ElementGenerator();
	}

	public BasicElementMapper() {
		super();
	}
}