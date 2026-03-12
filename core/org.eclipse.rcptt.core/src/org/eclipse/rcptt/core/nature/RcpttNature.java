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
package org.eclipse.rcptt.core.nature;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

/**
 * Rcptt Project Nature
 * TODO: it makes sense to move nature to another plug-in because
 * this class needn't on target platform
 */
public class RcpttNature implements IProjectNature {

	public static final String NATURE_ID = RcpttPlugin.PLUGIN_ID + ".rcpttnature";
	public static final String LEGACY_NATURE_ID = "com.xored.q7.core.q7nature";
	public static final String BUILDER_ID = "org.eclipse.rcptt.core.builder.q7Builder";
	public static final String LEGACY_BUILDER_ID = "com.xored.q7.core.builder.q7Builder";

	private IProject project;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	@Override
	public void configure() throws CoreException {
		if (!hasBuilder(project))
			enableFor(project);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	@Override
	public void deconfigure() throws CoreException {
		//Builder is associated with nature and will be deactivated automatically
	}

	public static boolean isRcpttProject(IProject project) throws CoreException {
		return project.hasNature(NATURE_ID);
	}

	public static void updateProjectNature(IProject project, boolean add)
			throws CoreException {
		List<String> natures = getNewNatures(project, add);
		if (natures != null) {
			IProjectDescription description = project.getDescription();
			String[] naturesArray = natures.toArray(new String[natures.size()]);
			description.setNatureIds(naturesArray);
			project.setDescription(description, new NullProgressMonitor());
		}
	}

	public static List<String> getNewNatures(IProject project, boolean add)
			throws CoreException {
		List<String> newNatures = new ArrayList<String>();
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		for (String nature : natures) {
			if (NATURE_ID.equals(nature)) {
				// need to add nature and we already have it -> nothing to do
				if (add)
					return null;
				// need to remove nature -> do not copy it to the result list
				else
					continue;
			}
			newNatures.add(nature);
		}
		if (add) {
			// we need to add nature
			newNatures.add(NATURE_ID);
		}
		// need to remove nature and there is no out nature - nothing to do
		else if (newNatures.size() == natures.length) {
			return null;
		}
		return newNatures;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	@Override
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core
	 * .resources.IProject)
	 */
	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	private static boolean hasBuilder(IProject iProject) throws CoreException {
		IProjectDescription description = iProject.getDescription();
		ICommand[] buildSpec = description.getBuildSpec();
		for (ICommand iCommand : buildSpec) {
			if (iCommand.getBuilderName().equals(BUILDER_ID)) {
				return true;
			}
		}
		return false;
	}

	private static void enableFor(IProject iProject) throws CoreException {
		IProjectDescription description = iProject.getDescription();
		ICommand[] buildSpec = description.getBuildSpec();
		ICommand[] newSpecs = new ICommand[buildSpec.length + 1];
		System.arraycopy(buildSpec, 0, newSpecs, 0, buildSpec.length);
		newSpecs[buildSpec.length] = description.newCommand();
		newSpecs[buildSpec.length].setBuilderName(BUILDER_ID);
		// newSpecs[buildSpec.length].setArguments(args)
		description.setBuildSpec(newSpecs);
		iProject.setDescription(description, new NullProgressMonitor());
	}

}
