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
package org.eclipse.rcptt.search.utils;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.internal.ui.text.IFileSearchContentProvider;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

import org.eclipse.rcptt.search.Q7SearchResult;
import org.eclipse.rcptt.search.Q7SearchResultPage;

@SuppressWarnings("restriction")
public class ScenarioTableContentProvider implements
		IStructuredContentProvider, IFileSearchContentProvider {

	private final Object[] EMPTY_ARR = new Object[0];

	private Q7SearchResultPage fPage;
	private AbstractTextSearchResult fResult;

	public ScenarioTableContentProvider(Q7SearchResultPage page) {
		fPage = page;
	}

	public void dispose() {
		// nothing to do
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Q7SearchResult) {
			int elementLimit = getElementLimit();
			Object[] elements = ((Q7SearchResult) inputElement).getElements();
			if (elementLimit != -1 && elements.length > elementLimit) {
				Object[] shownElements = new Object[elementLimit];
				System.arraycopy(elements, 0, shownElements, 0, elementLimit);
				return shownElements;
			}
			return elements;
		}
		return EMPTY_ARR;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Q7SearchResult) {
			fResult = (Q7SearchResult) newInput;
		}
	}

	public void elementsChanged(Object[] updatedElements) {
		TableViewer viewer = getViewer();
		int elementLimit = getElementLimit();
		boolean tableLimited = elementLimit != -1;
		for (int i = 0; i < updatedElements.length; i++) {
			if (fResult.getMatchCount(updatedElements[i]) > 0) {
				if (viewer.testFindItem(updatedElements[i]) != null)
					viewer.update(updatedElements[i], null);
				else {
					if (!tableLimited
							|| viewer.getTable().getItemCount() < elementLimit)
						viewer.add(updatedElements[i]);
				}
			} else
				viewer.remove(updatedElements[i]);
		}
	}

	private int getElementLimit() {
		return fPage.getElementLimit().intValue();
	}

	private TableViewer getViewer() {
		return (TableViewer) fPage.getViewer();
	}

	public void clear() {
		getViewer().refresh();
	}

	@Override
	public int getLeafCount(Object parentElement) {
		if (parentElement instanceof Q7SearchResult typed) {
			return typed.getMatchCount();
		}
		return 0;
	}
}
