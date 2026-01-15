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
package org.eclipse.rcptt.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.repository.RemoteRepository;

import org.eclipse.rcptt.maven.util.CoordResolver;

/**
 * Prepares resources for q7 launching: - copies project to target dir - unpacks
 * all dependencies to target dir - resolves and unpacks AUT - resolves and
 * unpacks q7 launcher
 *
 * @author ivaninozemtsev
 *
 * @goal resources
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */
public class PrepareMojo extends AbstractRCPTTMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skipTests()) {
			return;
		}

		unpackApps();
		if (getQ7Dir().exists()) {
			installPlugins(getResolvedQ7Dir(), getQ7Coords().getPlugins());
		}
		copyProjectToTarget();
		unpackDependencies();
	}

	private void installPlugins(File q7Dir, String[] plugins) throws MojoFailureException, MojoExecutionException {
		if (!q7Dir.exists()) {
			return;
		}
		if (plugins == null || plugins.length == 0) {
			return;
		}
		File pluginsDir = new File(q7Dir, "plugins");
		for (String plugin : plugins) {
			try {
				File pluginFile = new File(plugin);
				if (pluginFile.exists()) {
					if (pluginFile.isFile()) {
						FileUtils.copyFileToDirectory(pluginFile, pluginsDir);
					} else if (pluginFile.isDirectory()) {
						File[] listFiles = pluginFile.listFiles();
						for (File file : listFiles) {
							if (file.isFile()) {
								FileUtils.copyFileToDirectory(file, pluginsDir);
							}
						}
					}
				}

			} catch (IOException e) {
				throw new MojoExecutionException("Failed to copy project to target directory", e);
			}
		}
	}

	/**
	 * Copy project contents (except for the target dir) to target
	 *
	 * @throws MojoFailureException
	 * @throws MojoExecutionException
	 */
	private void copyProjectToTarget() throws MojoFailureException, MojoExecutionException {
		getLog().info("Copying project contents to target directory...");
		File destination = getThisProjectDir();
		try {
			for (File file : project.getBasedir().listFiles()) {
				if (file.equals(outDir)) {
					continue; // skip 'target' dir
				}
				if (file.isFile()) {
					FileUtils.copyFileToDirectory(file, destination);
				} else if (file.isDirectory()) {
					FileUtils.copyDirectoryStructure(file, new File(destination, file.getName()));
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to copy project to target directory", e);
		}
	}

	/**
	 * Resolve and extract Q7 and AUT to target directory
	 *
	 * @throws MojoFailureException
	 * @throws MojoExecutionException
	 */
	private void unpackApps() throws MojoFailureException, MojoExecutionException {
		if (!projectHasTests()) {
			getLog().info("No RCPTT tests found in project, skipping AUT and RCPTT resolution");
			return;
		}
		CoordResolver resolver = getCoordResolver();
		File autArchive = resolver.resolve("AUT", aut, remoteProjectRepos);
		File q7Archive = resolver.resolve("RCPTT runner", getQ7Coords(), remotePluginRepos);
		File autDir = getAutDir();
		File q7Dir = getQ7Dir();
		if (autArchive.isFile()) {
			getLog().info(String.format("Extracting AUT to %s", autDir));
			getArchiveUtil().extract(autArchive, getAutDir());
		} else {
			getLog().info(String.format("Using AUT from %s", autArchive));
			setAutDir(autArchive);
		}
		if (q7Archive.isFile()) {
			getLog().info(String.format("Extracting RCPTT runner from %s to %s", q7Archive, q7Dir));
			getArchiveUtil().extract(q7Archive, getQ7Dir());
		} else {
			getLog().info(String.format("Using RCPTT Runner from %s", q7Archive));
			setQ7Dir(q7Archive);
		}
	}

	/**
	 * Unpack all transitive dependencies to target/projects
	 *
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private void unpackDependencies() throws MojoExecutionException, MojoFailureException {
		if (!projectHasTests()) {
			getLog().info("No RCPTT tests found in project, skipping dependency unpacking");
			return;
		}

		for (org.apache.maven.artifact.Artifact a : project.getArtifacts()) {
			if (!Q7_TYPE.equals(a.getType())) {
				continue;
			}
			File artifact = a.getFile();

			// So that we won't get collision in rare case when
			// artifact ids are matching while group ids are different
			File destination = new File(getProjectsDir(), String.format("%s.%s", a.getGroupId(), a.getArtifactId()));
			getLog().info(String.format("Unpacking %s to %s", a, destination));
			getArchiveUtil().extract(artifact, destination);
		}
	}

	/**
	 * Remote repositories of current plugin, used to resolve q7 runner
	 *
	 * @parameter default-value="${project.remotePluginRepositories}"
	 * @readonly
	 */
	private List<RemoteRepository> remotePluginRepos;

	/**
	 * Remote repositories of project we are building, used to resolve aut
	 *
	 * @parameter default-value="${project.remoteProjectRepositories}"
	 * @readonly
	 */
	private List<RemoteRepository> remoteProjectRepos;

}
