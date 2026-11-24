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

public class AllTagQueryPattern implements ISearchPattern {

	public void findIndexMatches(Index index, IProgressMonitor monitor,
			final IIndexRequestor requestor) {
		final IPath path = index.getPath();
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<QueryResult> list = index.query(
				new String[] { IQ7IndexConstants.TAG }, new IKeyQuery() {
					public boolean accept(String key, String value) {
						return true;
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
