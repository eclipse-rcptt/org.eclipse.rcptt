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
package org.eclipse.rcptt.core.model.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.jobs.IJob;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.core.model.index.Index;
import org.eclipse.rcptt.internal.core.model.index.IndexManager;
import org.eclipse.rcptt.internal.core.model.index.ReadWriteMonitor;

public class PatternSearchJob implements IJob {
	protected boolean areIndexesReady;

	protected long executionTime = 0;

	private ISearchScope scope;
	private ISearchPattern pattern;
	private IIndexRequestor requestor;

	public PatternSearchJob(ISearchPattern pattern, ISearchScope scope,
			IIndexRequestor requestor) {
		this.pattern = pattern;
		this.scope = scope;
		this.requestor = requestor;
	}

	public boolean belongsTo(String jobFamily) {
		return true;
	}

	public void cancel() {
		// search job is cancelled through progress
	}

	public void ensureReadyToRun() {
		if (!this.areIndexesReady)
			getIndexes(null/* progress */); // may trigger some index
												// recreation
	}

	public boolean execute(IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();

		boolean isComplete = COMPLETE;
		executionTime = 0;
		Index[] indexes = getIndexes(progressMonitor);
		try {
			int max = indexes.length;
			if (progressMonitor != null)
				progressMonitor.beginTask("", max); //$NON-NLS-1$
			for (int i = 0; i < max; i++) {
				isComplete &= search(indexes[i], progressMonitor);
				if (progressMonitor != null) {
					if (progressMonitor.isCanceled())
						throw new OperationCanceledException();
					progressMonitor.worked(1);
				}
			}
			return isComplete;
		} finally {
			if (progressMonitor != null)
				progressMonitor.done();
		}
	}

	public Index[] getIndexes(IProgressMonitor progressMonitor) {
		List<Index> indexes = new ArrayList<Index>();
		IndexManager indexManager = ModelManager.getModelManager()
				.getIndexManager();
		IPath[] paths = scope.getPaths();
		for (IPath iPath : paths) {
			Index index = indexManager.getIndex(iPath);
			if (index != null) {
				indexes.add(index);
			}
		}
		return indexes.toArray(new Index[indexes.size()]);
	}

	public boolean search(Index index, IProgressMonitor progressMonitor) {
		if (index == null)
			return COMPLETE;
		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();

		ReadWriteMonitor monitor = index.monitor;
		if (monitor == null)
			return COMPLETE; // index got deleted since acquired
		try {
			monitor.enterRead(); // ask permission to read
			long start = System.currentTimeMillis();
			pattern.findIndexMatches(index, progressMonitor, requestor);
			executionTime += System.currentTimeMillis() - start;
			return COMPLETE;
		} finally {
			monitor.exitRead(); // finished reading
		}
	}

	public String toString() {
		return "searching " + pattern.toString(); //$NON-NLS-1$
	}
}
