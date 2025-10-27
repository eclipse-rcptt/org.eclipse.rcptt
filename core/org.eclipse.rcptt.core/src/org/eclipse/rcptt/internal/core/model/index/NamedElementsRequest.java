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
package org.eclipse.rcptt.internal.core.model.index;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

public class NamedElementsRequest extends IndexRequest {
	private final IQ7Project project;
	private final List<IQ7NamedElement> elements;
	private boolean clearOther = false;

	/**
	 * @param project
	 * @param elements
	 */
	public NamedElementsRequest(IProjectIndexer indexer, IQ7Project project,
			List<IQ7NamedElement> elements, boolean clearOther) {
		super(indexer);
		this.project = project;
		this.elements = elements;
		this.clearOther = clearOther;
	}

	@Override
	protected String getName() {
		return project.getName();
	}

	@Override
	protected void run() throws CoreException, IOException {
		final Index index = getIndexer().getProjectIndex(project);
		if (index == null) {
			RcpttPlugin.log("Index are null for:" + this.elements, null);
			return;
		}
		final List<?> changes = checkChanges(index, elements, clearOther);
		if (changes.isEmpty()) {
			return;
		}
		final ReadWriteMonitor imon = index.monitor;
		imon.enterWrite();
		try {
			ExecutorService executor = Executors.newFixedThreadPool(Runtime
					.getRuntime().availableProcessors() * 2 + 1);
			for (Iterator<?> i = changes.iterator(); !isCancelled
					&& i.hasNext();) {
				final Object change = i.next();
				if (change instanceof String) {
					index.remove((String) change);
				} else {
					executor.execute(new Runnable() {
						public void run() {
							getIndexer().indexNamedElement(index,
									(IQ7NamedElement) change);
						}
					});
				}
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
				if (isCancelled) {
					executor.shutdownNow();
					break;
				}
				Thread.sleep(50);
			}
		} catch (Throwable t) {
			RcpttPlugin.log(t);
		} finally {
			try {
				index.save();
			} catch (IOException e) {
				RcpttPlugin.log("error saving index", e); //$NON-NLS-1$
			} finally {
				imon.exitWrite();
			}
		}
	}

	@Override
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(project.getProject().getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedElementsRequest other = (NamedElementsRequest) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}
}
