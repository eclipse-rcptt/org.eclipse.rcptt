/*******************************************************************************
 * Copyright (c) 2009, 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.core.model.search;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.index.IQ7IndexConstants;
import org.eclipse.rcptt.core.model.search.SearchPattern.IKeyQuery;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.index.Index;
import org.eclipse.rcptt.internal.core.model.index.QueryResult;

public class AllIDQueryPattern implements ISearchPattern {
	public AllIDQueryPattern() {
	}

	public void findIndexMatches(Index index, IProgressMonitor monitor,
			final IIndexRequestor requestor) {
		final IPath path = index.getPath();
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<QueryResult> query = index.query(
				new String[] { IQ7IndexConstants.ID }, new IKeyQuery() {
					public boolean accept(String key, String value) {
						// requestor.acceptMatch(null, IQ7IndexConstants.ID,
						// value);
						return true;
					}
				}, monitor);
		for (QueryResult queryResult : query) {
			Path fullPath = new Path(queryResult.path);
			IResource resource = root.findMember(path.append(fullPath));
			if (resource != null && resource.exists()) {
				IQ7Element q7Element = RcpttCore.create(resource);
				requestor.acceptMatch(q7Element, IQ7IndexConstants.ID,
						queryResult.value);
			}
		}
	}
}
