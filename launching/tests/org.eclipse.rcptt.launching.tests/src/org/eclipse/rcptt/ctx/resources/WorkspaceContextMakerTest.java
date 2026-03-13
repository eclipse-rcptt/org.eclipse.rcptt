/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ctx.resources;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.core.workspace.ProjectUtil;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.launching.CheckedExceptionWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class WorkspaceContextMakerTest {
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();

	/** @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/204 **/
	@Test
	public void survivesLowMemory() throws InterruptedException, ExecutionException, CoreException {
		 WorkspaceContextMaker maker = new WorkspaceContextMaker();
		 IQ7Project project = RcpttCore.create(importProject("largeWorkspaceContext"));
		 IContext context = project.getRootFolder().getContext("largeWorkspaceContext.ctx");
		 Context output = (Context) EcoreUtil.copy(context.getModifiedNamedElement());
		 
		 Job job = Job.create("Imitate memory shortage", monitor -> {
			while (!monitor.isCanceled()) {
				 try {
					context.close();
					Thread.yield();
				 } catch (InterruptedException e) {
					return Status.OK_STATUS;
				} catch (ModelException e) {
					throw new CheckedExceptionWrapper(e);
				}
			}
			return Status.OK_STATUS;
		 });
		 job.setPriority(Job.INTERACTIVE);
		 job.schedule();
		 try {
			 long stop = currentTimeMillis() + 1000;
			 while (currentTimeMillis() < stop) {
				 maker.makeExecutable(output, context); // Should not throw
			 }
		 } finally {
			 job.cancel();
			 job.join();
			 if (!job.getResult().isOK()) {
				 throw new CoreException(job.getResult());
			 }
		 }
	}
	
	private static final IProject importProject(String name) {
		try {
			Bundle bundle = FrameworkUtil.getBundle(WorkspaceContextMakerTest.class);
			URL url = FileLocator.toFileURL(FileLocator.resolve(bundle.getResource("resources/"+name)));
			ProjectUtil.importProjects(new File[] {new File(url.toURI())}, System.out);
			IProject result = WORKSPACE.getRoot().getProject(name);
			Assert.assertTrue(result.isAccessible());
			return result;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

}
