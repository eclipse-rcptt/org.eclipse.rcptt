/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ctx.resources;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.DefaultContextMaker;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.resources.ImportUtils;
import org.eclipse.rcptt.resources.WSUtils;
import org.eclipse.rcptt.resources.WorkspaceDataImporter;
import org.eclipse.rcptt.resources.WorkspaceDataMaker;
import org.eclipse.rcptt.workspace.WSProject;
import org.eclipse.rcptt.workspace.WSRoot;
import org.eclipse.rcptt.workspace.WorkspaceContext;
import org.eclipse.rcptt.workspace.WorkspaceData;
import org.eclipse.rcptt.workspace.WorkspaceFactory;

public class WorkspaceContextMaker extends DefaultContextMaker {

	private WorkspaceDataMaker maker;

	public WorkspaceContextMaker() {
		this.maker = new WorkspaceDataMaker();
	}

	@Override
	public Context create(Resource resource, ContextType type, String name) {
		WorkspaceContext context = (WorkspaceContext) super.create(resource,
				type, name);
		context.setContent(WorkspaceFactory.eINSTANCE.createWSRoot());
		return context;
	}

	@Override
	public void makeExecutable(Context context, IQ7NamedElement source)
			throws ModelException {
		IQ7NamedElement copy = source.getIndexingWorkingCopy(null);
		try {
			maker.makeSelfSufficientData((WorkspaceData) context,
					copy.getModifiedPersistenceModel(), source);
		} finally {
			copy.discardWorkingCopy();
		}
	}

	@Override
	public void captureContents(IContext context, Context data,
			IProgressMonitor monitor) throws CoreException {
		WorkspaceContext wsContext = (WorkspaceContext) data;
		WorkspaceDataImporter importer = new WorkspaceDataImporter(wsContext);

		IPersistenceModel model = context.getPersistenceModel();
		model.removeAll();
		try {
			WSRoot ownRoot = ((WorkspaceContext) context.getNamedElement())
					.getContent();
			ownRoot.getProjects().clear();
			List<WSProject> projects = wsContext.getContent().getProjects();
			if (projects.size() > 0) {
				monitor.beginTask("Copy projects...", projects.size());
				for (WSProject project : projects) {
					WSProject ownProject = WSUtils.getProject(ownRoot,
							project.getName(), true);
					ImportUtils.importFiles(ownProject, project.getFiles(),
							model, importer);
					ImportUtils.importFolders(ownProject, project.getFolders(),
							model, importer);
				}
			}
		} catch (Exception e) {
			throw new CoreException(RcpttPlugin.createStatus(e));
		}
	}
}
