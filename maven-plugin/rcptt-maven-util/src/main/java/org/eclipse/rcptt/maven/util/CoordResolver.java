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
package org.eclipse.rcptt.maven.util;

import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.URLInputStreamFacade;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.metadata.Metadata.Nature;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.MetadataRequest;
import org.eclipse.aether.resolution.MetadataResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.metadata.DefaultMetadata;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;

public class CoordResolver {
	private final RepositorySystem repoSys;
	private final RepositorySystemSession repoSession;
	private final File baseDir;
	private final File targetDir;
	private final Log log;

	public CoordResolver(File baseDir, File targetDir, RepositorySystem repoSys, RepositorySystemSession repoSession,
			Log log) {
		this.repoSys = repoSys;
		this.repoSession = repoSession;
		this.baseDir = baseDir;
		this.targetDir = targetDir;
		this.log = log;
	}

	/**
	 * Resolves given coordinates against given repos. Returns absolute location
	 * of artifact archive If artifact is not resolved, throws
	 * MojoExecutionException, never returns {@code null}
	 * 
	 * @param codename
	 *            The name of artifact denoted by coords, for friendly messages
	 *            in log
	 * @param coords
	 *            Artifact coords
	 * @param repos
	 *            List or remote repositories
	 * @return
	 * @throws MojoFailureException
	 *             in case of failure
	 * @throws MojoExecutionException
	 */
	public File resolve(String codename, Coords coords, List<RemoteRepository> repos)
			throws MojoFailureException, MojoExecutionException {
		return coords.getExplicit() == null ? resolveArtifact(codename, coords, repos)
				: resolveFile(coords.getExplicit());
	}

	private File resolveArtifact(String codename, Coords coords, List<RemoteRepository> repos)
			throws MojoExecutionException, MojoFailureException {

		if (coords.getExtension() == null) {
			coords.setExtension(ArchiveUtil.DEFAULT_EXTENSION);
			logDefaultValue(codename, "extension", coords.getExtension());
		}
		if (coords.getClassifier() == null) {
			coords.setClassifier(PlatformUtil.getEclipseClassifier());
			log.info("Sys arch is " + System.getProperty("os.arch"));
			logDefaultValue(codename, "classifier", coords.getClassifier());
		}

		Artifact artifact = new DefaultArtifact(coords.getGroupId(), coords.getArtifactId(), coords.getClassifier(),
				coords.getExtension(), coords.getVersion());

		if (coords.getVersion() == null) {
			artifact = resolveVersion(artifact, repos);
			logDefaultValue(codename, "version", artifact.getVersion());
		}

		if (artifact.isSnapshot() && hasRemoteUpdates(artifact, repos)) {
			log.info(String.format("%s is at snapshot version and has remote updates, purging local versions...",
					codename));
			purgeLocalVersion(artifact);
		}

		log.info(String.format("Resolving artifact %s, this may take a while ...", artifact));
		ArtifactRequest request = new ArtifactRequest(artifact, repos, null);
		try {
			ArtifactResult result = repoSys.resolveArtifact(repoSession, request);
			File resultFile = result.getArtifact().getFile();
			log.info(String.format("Artifact %s is resolved to %s", artifact, resultFile));
			return resultFile;
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(String.format("Failed to resolve artifact %s", artifact), e);
		}
	}

	private void purgeLocalVersion(Artifact artifact) throws MojoExecutionException {
		LocalRepositoryManager localManager = repoSession.getLocalRepositoryManager();
		File dir = new File(localManager.getRepository().getBasedir(), localManager.getPathForLocalArtifact(artifact))
				.getParentFile();
		log.info("Removing directory " + dir);
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			throw new MojoExecutionException("FAiled to clean repo", e);
		}
	}

	private final VersionScheme scheme = new GenericVersionScheme();

	private boolean hasRemoteUpdates(Artifact artifact, List<RemoteRepository> repos) {
		List<MetadataRequest> metadataRequests = new ArrayList<MetadataRequest>();
		for (RemoteRepository repo : repos) {
			Metadata metadata = new DefaultMetadata(artifact.getGroupId(), artifact.getArtifactId(),
					artifact.getVersion(), "", Nature.SNAPSHOT);
			MetadataRequest req = new MetadataRequest(metadata, repo, null);
			metadataRequests.add(req);
		}

		List<MetadataResult> results = repoSys.resolveMetadata(repoSession, metadataRequests);

		boolean haveUpdates = false;
		try {
			Version currentVersion = scheme.parseVersion(artifact.getVersion());

			for (MetadataResult result : results) {
				if (result.getException() == null && !result.isMissing() && result.isResolved() && result.isUpdated()) {

					haveUpdates |= scheme.parseVersion(result.getMetadata().getVersion()).compareTo(currentVersion) > 0;
				}
			}
		} catch (InvalidVersionSpecificationException e) {
			log.warn(String.format("Cannot parse version %s", e.getVersion()));
			return false;
		}
		return haveUpdates;
	}

	/**
	 * For given artifact, resolves available versions and returns the same
	 * artifact with version set to the latest
	 * 
	 * @param artifact
	 * @param repos
	 * @return
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	private Artifact resolveVersion(Artifact artifact, List<RemoteRepository> repos)
			throws MojoExecutionException, MojoFailureException {
		artifact = artifact.setVersion("[0.0.1,)");
		VersionRangeRequest versionRequest = new VersionRangeRequest();
		versionRequest.setArtifact(artifact);
		versionRequest.setRepositories(repos);
		try {
			VersionRangeResult versionResult = repoSys.resolveVersionRange(repoSession, versionRequest);
			List<Version> versions = versionResult.getVersions();
			if (versions.isEmpty()) {
				throw new MojoFailureException(String.format("Failed to resolve artifact %s", artifact));
			}
			return artifact.setVersion(versions.get(versions.size() - 1).toString());
		} catch (VersionRangeResolutionException e) {
			throw new MojoExecutionException(String.format("Failed to resolve versions for artifact %s", artifact), e);
		}
	}

	private File resolveFile(String path) throws MojoFailureException, MojoExecutionException {
		path = performSubstitution(path);
		// dummy http downloader
		if (path.startsWith("http://") || path.startsWith("https://")) {
			return downloadFromHttp(path);
		}
		File result = new File(path);
		if (!result.isAbsolute()) {
			result = new File(baseDir, path);
		}
		if (!result.exists()) {
			throw new MojoFailureException(String.format("The explicit location %s is not valid", result));
		}
		return result;
	}

	private File downloadFromHttp(String path) throws MojoExecutionException {
		URL url;
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			throw new MojoExecutionException(String.format("Malformed URL in explicit AUT location: %s", path), e);
		}

		int lastSlashIndex = path.lastIndexOf('/');
		String fileName = lastSlashIndex == -1 ? "unknown_file" : path.substring(lastSlashIndex);
		File resultFile = new File(targetDir, fileName);

		try {
			log.info(String.format("Downloading artifact from %s...", path));
			FileUtils.copyStreamToFile(new URLInputStreamFacade(url), resultFile);
		} catch (IOException e) {
			throw new MojoExecutionException("Download failed", e);
		}
		return resultFile;
	}

	private String performSubstitution(String str) {
		if (str.indexOf("[platform]") > 0) {
			str = str.replaceAll("\\[platform\\]", PlatformUtil.getEclipseClassifier());
		}
		if (str.indexOf("[classifier]") > 0) {
			str = str.replaceAll("\\[classifier\\]", PlatformUtil.getEclipseClassifier());
		}

		return str;

	}

	/**
	 * Displays information about value specified for omitted properties
	 * 
	 * @param codename
	 * @param property
	 * @param value
	 * @return
	 */
	private void logDefaultValue(String codename, String property, String value) {
		log.info(String.format("%s is not specified for %s, setting to %s by default", property, codename, value));
	}
	
	public static String computeLocalClassifier() {
		Properties properties = System.getProperties();
		
		return String.format("%s.%s.%s", PlatformPropertiesUtils.getOS(properties), PlatformPropertiesUtils.getWS(properties), PlatformPropertiesUtils.getArch(properties)); 
	}

}
