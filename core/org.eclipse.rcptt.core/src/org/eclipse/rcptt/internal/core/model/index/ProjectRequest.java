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

import org.eclipse.core.runtime.CoreException;

import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;

public class ProjectRequest extends IndexRequest {

	private final IQ7Project project;

	public ProjectRequest(IProjectIndexer indexer, IQ7Project project) {
		super(indexer);
		this.project = project;
	}

	@Override
	protected String getName() {
		return project.getName();
	}

	@Override
	protected void run() throws CoreException {
		IProjectIndexer.Internal indexer = getIndexer();
		final NamedElementCollector elementCollector = new NamedElementCollector();
		try {
			project.accept(elementCollector);
		} catch (ModelException | InterruptedException e) {
			throw new IllegalStateException(e);
		}
		indexer.request(new NamedElementsRequest(indexer, project,
				elementCollector.getElements(), true));
	}

	@Override
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(project.getProject().getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectRequest other = (ProjectRequest) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}
}
