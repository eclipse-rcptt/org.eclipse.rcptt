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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class AllContextWithLinksQueryPattern implements ISearchPattern {

	final Set<IPath> linksFilterPaths;

	public AllContextWithLinksQueryPattern() {
		linksFilterPaths = new HashSet<IPath>();
	}

	/**
	 * Constructor with injected filter for context
	 * 
	 * @param linksFilterPaths
	 */
	public AllContextWithLinksQueryPattern(Set<IPath> linksFilterPaths) {
		this.linksFilterPaths = linksFilterPaths;
	}

	public void findIndexMatches(Index index, IProgressMonitor monitor,
			final IIndexRequestor requestor) {
		final IPath path = index.getPath();
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<QueryResult> list = index.query(
				new String[] { IQ7IndexConstants.WS_LINK_REF },
				new IKeyQuery() {
					public boolean accept(String key, String value) {
						// return all contexts if filter is not specified
						if (linksFilterPaths.isEmpty()) {
							return true;
						}


						// if filter is specified compare it with linked elements
						IPath path = new Path(value);

						return linksFilterPaths.contains(path) ? true : false;
					}
				}, monitor);
		for (QueryResult queryResult : list) {
			Path fullPath = new Path(queryResult.path);
			IResource res = root.findMember(path.append(fullPath));
			if (res != null && res.exists()) {
				IQ7Element q7Element = RcpttCore.create(res);
				requestor.acceptMatch(q7Element, queryResult.key,
						queryResult.value);
			}
		}
	}
}
