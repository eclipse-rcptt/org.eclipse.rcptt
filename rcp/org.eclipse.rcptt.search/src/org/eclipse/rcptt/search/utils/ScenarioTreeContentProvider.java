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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.internal.ui.text.IFileSearchContentProvider;
import org.eclipse.search.internal.ui.text.LineElement;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

import org.eclipse.rcptt.search.Q7SearchQuery;
import org.eclipse.rcptt.search.Q7SearchResult;
import org.eclipse.rcptt.search.Q7SearchResultPage;

@SuppressWarnings({ "restriction", "rawtypes" })
public class ScenarioTreeContentProvider implements ITreeContentProvider,
		IFileSearchContentProvider {

	private final Object[] EMPTY_ARR = new Object[0];

	private AbstractTextSearchResult fResult;
	private final Q7SearchResultPage fPage;
	private final AbstractTreeViewer fTreeViewer;

	private Map fChildrenMap;

	public ScenarioTreeContentProvider(Q7SearchResultPage page,
			AbstractTreeViewer viewer) {
		fPage = page;
		fTreeViewer = viewer;
	}

	public Object[] getElements(Object inputElement) {
		Object[] children = getChildren(inputElement);
		int elementLimit = getElementLimit();
		if (elementLimit != -1 && elementLimit < children.length) {
			Object[] limitedChildren = new Object[elementLimit];
			System.arraycopy(children, 0, limitedChildren, 0, elementLimit);
			return limitedChildren;
		}
		return children;
	}

	private int getElementLimit() {
		return fPage.getElementLimit().intValue();
	}

	public void dispose() {
		// nothing to do
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Q7SearchResult) {
			initialize((Q7SearchResult) newInput);
		}
	}

	private synchronized void initialize(AbstractTextSearchResult result) {
		fResult = result;
		fChildrenMap = new HashMap();
		boolean showLineMatches = !((Q7SearchQuery) fResult.getQuery())
				.isFileNameSearch();

		if (result != null) {
			Object[] elements = result.getElements();
			for (int i = 0; i < elements.length; i++) {
				if (showLineMatches) {
					Match[] matches = result.getMatches(elements[i]);
					for (int j = 0; j < matches.length; j++) {
						insert(((org.eclipse.search.internal.ui.text.FileMatch) matches[j]).getLineElement(), false);
					}
				} else {
					insert(elements[i], false);
				}
			}
		}
	}

	private void insert(Object child, boolean refreshViewer) {
		int searchFor = ((Q7SearchQuery) fResult.getQuery()).getSearchFor();
		boolean isDependencySearch = searchFor == Q7SearchQuery.CONTEXTS_BY_ID
				|| searchFor == Q7SearchQuery.TESTCASE_BY_ID
				|| searchFor == Q7SearchQuery.VERIFICATION_BY_ID;
		if (isDependencySearch) {
			child = getParent(child);
		}
		Object parent = getParent(child);
		while (parent != null) {
			if (insertChild(parent, child)) {
				if (refreshViewer)
					fTreeViewer.add(parent, child);
			} else {
				if (refreshViewer)
					fTreeViewer.refresh(parent);
				return;
			}
			child = parent;
			parent = getParent(child);
		}
		if (insertChild(fResult, child)) {
			if (refreshViewer)
				fTreeViewer.add(fResult, child);
		}
	}

	/**
	 * Adds the child to the parent.
	 * 
	 * @param parent
	 *            the parent
	 * @param child
	 *            the child
	 * @return <code>true</code> if this set did not already contain the
	 *         specified element
	 */
	@SuppressWarnings("unchecked")
	private boolean insertChild(Object parent, Object child) {
		Set children = (Set) fChildrenMap.get(parent);
		if (children == null) {
			children = new HashSet();
			fChildrenMap.put(parent, children);
		}
		return children.add(child);
	}

	private boolean hasChild(Object parent, Object child) {
		Set children = (Set) fChildrenMap.get(parent);
		return children != null && children.contains(child);
	}

	private void remove(Object element, boolean refreshViewer) {
		// precondition here: fResult.getMatchCount(child) <= 0

		if (hasChildren(element)) {
			if (refreshViewer)
				fTreeViewer.refresh(element);
		} else {
			if (!hasMatches(element)) {
				fChildrenMap.remove(element);
				Object parent = getParent(element);
				if (parent != null) {
					removeFromSiblings(element, parent);
					remove(parent, refreshViewer);
				} else {
					removeFromSiblings(element, fResult);
					if (refreshViewer)
						fTreeViewer.refresh();
				}
			} else {
				if (refreshViewer) {
					fTreeViewer.refresh(element);
				}
			}
		}
	}

	private boolean hasMatches(Object element) {
		if (element instanceof LineElement) {
			LineElement lineElement = (LineElement) element;
			return lineElement.getNumberOfMatches(fResult) > 0;
		}
		return fResult.getMatchCount(element) > 0;
	}

	private void removeFromSiblings(Object element, Object parent) {
		Set siblings = (Set) fChildrenMap.get(parent);
		if (siblings != null) {
			siblings.remove(element);
		}
	}

	public Object[] getChildren(Object parentElement) {
		Set children = (Set) fChildrenMap.get(parentElement);
		if (children == null)
			return EMPTY_ARR;
		return children.toArray();
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.internal.ui.text.IFileSearchContentProvider#
	 * elementsChanged(java.lang.Object[])
	 */
	public synchronized void elementsChanged(Object[] updatedElements) {
		for (int i = 0; i < updatedElements.length; i++) {
			if (!(updatedElements[i] instanceof LineElement)) {
				// change events to elements are reported in file search
				if (fResult.getMatchCount(updatedElements[i]) > 0)
					insert(updatedElements[i], true);
				else
					remove(updatedElements[i], true);
			} else {
				// change events to line elements are reported in text search
				LineElement lineElement = (LineElement) updatedElements[i];
				int nMatches = lineElement.getNumberOfMatches(fResult);
				if (nMatches > 0) {
					if (hasChild(lineElement.getParent(), lineElement)) {
						fTreeViewer.update(new Object[] { lineElement,
								lineElement.getParent() }, null);
					} else {
						insert(lineElement, true);
					}
				} else {
					remove(lineElement, true);
				}
			}
		}
	}

	public void clear() {
		initialize(fResult);
		fTreeViewer.refresh();
	}

	public Object getParent(Object element) {
		if (element instanceof IProject)
			return null;
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			return resource.getParent();
		}
		if (element instanceof LineElement) {
			return ((LineElement) element).getParent();
		}

		if (element instanceof org.eclipse.search.internal.ui.text.FileMatch) {
			org.eclipse.search.internal.ui.text.FileMatch match = (org.eclipse.search.internal.ui.text.FileMatch) element;
			return match.getLineElement();
		}
		return null;
	}
	
	@Override
	public int getLeafCount(Object parentElement) {
		Object[] children = getChildren(parentElement);
		if (children.length == 0) {
			return 0;
		}
		int count = 0;
		for (Object object : children) {
			boolean leaf = !hasChildren(object);
			if (leaf) {
				count++;
			} else {
				count += getLeafCount(object);
			}
		}
		return count;
	}

}
