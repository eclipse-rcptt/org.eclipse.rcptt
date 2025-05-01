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

import static org.eclipse.rcptt.maven.RunnerVersionDispatcher.getDefaultArtifact;
import static org.eclipse.rcptt.maven.RunnerVersionDispatcher.getDefaultGroup;
import static org.eclipse.rcptt.maven.RunnerVersionDispatcher.getDefaultPaltform;
import static org.eclipse.rcptt.maven.RunnerVersionDispatcher.parseVersion;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.rcptt.maven.util.ArchiveUtil;
import org.eclipse.rcptt.maven.util.AutCoords;
import org.eclipse.rcptt.maven.util.CoordResolver;
import org.eclipse.rcptt.maven.util.RCPTTCoords;
import org.eclipse.rcptt.maven.util.TestEngine;
import org.eclipse.rcptt.maven.util.TestOptions;

public abstract class AbstractRCPTTMojo extends AbstractMojo {
	public static final String HAS_TESTS_KEY = "hasq7tests";
	public static final String Q7_TYPE = "rcpttTest";
	public static final String[] SCENARIO_EXTENSIONS = new String[] { "scenario", "test" };
	public static final String CONTEXT_EXTENSION = "ctx";
	public static final String JAR = "-jar";

	/**
	 * @parameter property="project"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * @parameter
	 * @required
	 */
	protected AutCoords aut;

	/**
	 * @parameter
	 */
	protected boolean splitHtmlReport = false;

	/**
	 * @parameter
	 */
	protected boolean memoryUsage = false;

	/**
	 * @parameter
	 */
	protected String autCount;

	/**
	 * @parameter property="maven.test.failure.ignore"
	 *            default-value="false"
	 */
	protected boolean testFailureIgnore = false;

	/**
	 * @parameter property="skipTests" default-value="false"
	 */
	protected boolean skipTests = false;
	/**
	 * Same as {@link #skipTests}
	 *
	 * @parameter property="maven.test.skip" default-value="false"
	 */
	private boolean skip;

	/**
	 * @parameter
	 */
	protected int limit = -1;

	/**
	 * @parameter
	 */
	private RCPTTCoords runner;

	protected RCPTTCoords getQ7Coords() {
		if (runner == null) {
			runner = new RCPTTCoords();
		}
		runner.setClassifier(computeClassifier());
		ComparableVersion version = parseVersion(runner.getVersion());
		if(runner.getPlatform() == null){
			runner.setPlatform(getDefaultPaltform(version));
		}
		if (runner.getArtifactId() == null) {
			runner.setArtifactId(getDefaultArtifact(version, runner.getPlatform()));
		}
		if (runner.getGroupId() == null) {
			runner.setGroupId(getDefaultGroup(version));
		}
		return runner;
	}

	private static String computeClassifier() {
		return org.eclipse.rcptt.maven.util.CoordResolver.computeLocalClassifier();
	}

	/**
	 * @parameter
	 */
	private Map<String, String> testOptions;

	protected Map<String, String> getTestOptions() {
		if (testOptions == null) {
			testOptions = TestOptions.createDefaults();
		}
		return testOptions;
	}

	/**
	 * @parameter
	 */
	private List<TestEngine> testEngines;

	protected List<TestEngine> getTestEngines() {
		return testEngines;
	}

	/**
	 * @parameter
	 */
	protected String[] projects;

	/**
	 * @parameter
	 */
	protected String[] skipTags;

	/**
	 * @parameter
	 */
	protected String[] suites;

	/**
	 * @parameter
	 */
	protected String[] tests;

	/**
	 * @parameter
	 */
	protected UserReport report;

	/**
	 * @parameter
	 */
	protected boolean useDefaultEclipseKeyring = false;

	/**
	 * Target directory.
	 *
	 * @parameter property="project.build.directory"
	 * @required
	 */
	protected File outDir;

	private File projectsDir;

	/**
	 * Returns directory where we should unpack projects
	 *
	 * @return
	 * @throws MojoFailureException
	 *             when directory can't be created
	 */
	protected File getProjectsDir() throws MojoFailureException {
		if (projectsDir == null) {
			projectsDir = new File(outDir, "projects");
			if (!projectsDir.exists() && !projectsDir.mkdirs()) {
				throw new MojoFailureException("Can't create directory " + projectsDir);
			}
		}
		return projectsDir;
	}

	private File q7WsDir;

	protected File getQ7WsDir() throws MojoFailureException {
		if (q7WsDir == null) {
			q7WsDir = new File(outDir, "runner-workspace");
			if (!q7WsDir.exists() && !q7WsDir.mkdirs()) {
				throw new MojoFailureException("Can't create directory " + projectsDir);
			}
		}
		return q7WsDir;
	}

	protected File getMetadataDir(File ws) {
		return new File(ws, ".metadata");
	}

	private File autLocation;

	private static final String EXPLICIT_AUT_KEY = "explicitAutLocation";

	protected File getAutDir() {
		if (project.getProperties().containsKey(EXPLICIT_AUT_KEY)) {
			return new File(project.getProperties().getProperty(EXPLICIT_AUT_KEY));
		}
		if (autLocation == null) {
			autLocation = new File(outDir, "aut");
		}
		return autLocation;
	}

	protected void setAutDir(File file) {
		project.getProperties().setProperty(EXPLICIT_AUT_KEY, file.getAbsolutePath());
	}

	protected File getResolvedAutDir() throws MojoFailureException {
		return getResolvedRcpLocation(getAutDir());
	}

	protected File getResolvedRcpLocation(File baseDir) throws MojoFailureException {
		Stack<File> stack = new Stack<File>();
		stack.push(baseDir);
		while (!stack.isEmpty()) {
			final File file = stack.pop();
			if (!file.exists() || !file.isDirectory()) {
				continue;
			}
			if (Arrays.asList(file.list()).contains("plugins")) {
				return file;
			}
			for (final File child : file.listFiles()) {
				stack.push(child);
			}
		}
		throw new MojoFailureException(String.format("Invalid RCP location %s", baseDir));
	}

	private File q7location;

	private static String EXPLICIT_Q7_KEY = "explicitRunnerLocation";

	protected final File getQ7Dir(String platform) {
		if (project.getProperties().containsKey(EXPLICIT_Q7_KEY)) {
			return new File(project.getProperties().getProperty(EXPLICIT_Q7_KEY));
		}
		if (q7location == null) {
			q7location = new File(outDir, "runner");
		}
		return q7location;
	}

	protected final void setQ7Dir(File q7) {
		project.getProperties().setProperty(EXPLICIT_Q7_KEY, q7.getAbsolutePath());
	}

	protected File getResolvedQ7Dir(String platform) throws MojoFailureException {
		return getResolvedRcpLocation(getQ7Dir(platform));
	}

	private File autWorkspacePrefix;

	protected File getAutWorkspacePrefix() {
		if (autWorkspacePrefix == null) {
			String name = aut.reuseExistingWorkspace() ? "aut-ws" : "aut-ws-";
			autWorkspacePrefix = new File(outDir, name);
		}
		return autWorkspacePrefix;
	}

	private File surefireDir;

	public File getSurefireDir() throws MojoFailureException {
		if (surefireDir == null) {
			surefireDir = new File(outDir, "surefire-reports");
			if (!surefireDir.exists() && !surefireDir.mkdirs()) {
				throw new MojoFailureException("Can't create directory " + surefireDir);
			}
		}
		return surefireDir;
	}

	private File resultsDir;

	protected File getResultsDir() throws MojoFailureException {
		if (resultsDir == null) {
			resultsDir = new File(outDir, "results");
			if (!resultsDir.exists() && !resultsDir.mkdirs()) {
				throw new MojoFailureException("Can't create directory " + resultsDir);
			}
		}
		return resultsDir;
	}

	private File resultsFile;

	protected File getQ7ReportFile() throws MojoFailureException {
		if (resultsFile == null) {
			resultsFile = new File(getResultsDir(), reportFile("report"));
		}
		return resultsFile;
	}

	private File htmlFile;

	protected File getHtmlReportFile() throws MojoFailureException {
		if (htmlFile == null) {
			htmlFile = new File(getResultsDir(), reportFile("html"));
		}
		return htmlFile;
	}

	private File junitFile;

	protected File getJUnitReportFile() throws MojoFailureException {
		if (junitFile == null) {
			junitFile = new File(getSurefireDir(), String.format("TEST-%s", reportFile("xml")));
		}
		return junitFile;
	}

	private String reportFile(String ext) {
		return String.format("%s.%s", project.getArtifactId(), ext);
	}

	private File autConsolePrefix;

	protected File getAutConsolePrefix() throws MojoFailureException {
		if (autConsolePrefix == null) {
			autConsolePrefix = new File(getResultsDir(), "aut-console-");
		}
		return autConsolePrefix;
	}

	protected File getAutConsoleOut(int num) throws MojoFailureException {
		return new File(getAutConsolePrefix().getParent(),
				String.format("%s%d_console.log", getAutConsolePrefix().getName(), num));
	}

	private File thisProjectDir;

	/**
	 * Returns the destination directory of current project in target so that we
	 * can copy it to this dir for further packaging as artifact
	 *
	 * @return
	 * @throws MojoFailureException
	 */
	protected File getThisProjectDir() throws MojoFailureException {
		if (thisProjectDir == null) {
			thisProjectDir = new File(getProjectsDir(), project.getArtifactId());
			if (!thisProjectDir.exists() && !thisProjectDir.mkdirs()) {
				throw new MojoFailureException("Can't create directory " + thisProjectDir);
			}
		}
		return thisProjectDir;
	}

	private File q7Out;

	protected File getQ7Out() throws MojoFailureException {
		if (q7Out == null) {
			q7Out = new File(getResultsDir(), "out.txt");
		}
		return q7Out;
	}

	private File q7Err;

	public File getQ7Err() throws MojoFailureException {
		if (q7Err == null) {
			q7Err = new File(getResultsDir(), "err.txt");
		}
		return q7Err;
	}

	protected boolean projectHasTests() {
		if (projects != null && projects.length > 0) {
			return true;
		}
		if (!project.getProperties().containsKey(HAS_TESTS_KEY)) {
			// Scenario file may be a resource of workspace context
			// check for such scenarios
			String baseDir = project.getBasedir().getAbsolutePath();
			String[] contexts = FileUtils.getFilesFromExtension(baseDir, new String[] { CONTEXT_EXTENSION });

			for (int i = 0; i < contexts.length; i++) {
				contexts[i] = FileUtils.getPath(contexts[i]) + File.separator + "."
						+ FileUtils.filename(contexts[i]).replace("." + CONTEXT_EXTENSION, CONTEXT_EXTENSION);
			}

			String[] scenarioFiles = FileUtils.getFilesFromExtension(baseDir, SCENARIO_EXTENSIONS);

			int contextScenarioCount = 0;
			for (String scenarioFile : scenarioFiles) {
				for (String ctxPath : contexts) {
					if (ctxPath.equals(FileUtils.getPath(scenarioFile))) {
						contextScenarioCount++;
						break;
					}
				}
			}
			project.getProperties().setProperty(HAS_TESTS_KEY,
					Boolean.toString(scenarioFiles.length > contextScenarioCount));
		}
		return (Boolean) Boolean.valueOf(project.getProperties().getProperty(HAS_TESTS_KEY));
	}

	/**
	 * Returns true when tests should be skipped.
	 *
	 * @return true if the tests should be skipped
	 */
	protected boolean skipTests() {
		return skip || skipTests;
	}

	/**
	 * Get equinox launcher jar
	 *
	 * @return
	 * @throws MojoFailureException
	 */
	protected File getEquinoxJar() throws MojoFailureException {
		File q7dir = getResolvedQ7Dir(getQ7Coords().getPlatform());
		File plugins = new File(q7dir, "plugins");
		if (!plugins.exists() || !plugins.isDirectory()) {
			throw new MojoFailureException("Invalid RCPTT location " + q7dir);
		}
		File[] result = plugins.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return false;
				if (!f.getName().endsWith(".jar"))
					return false;
				return f.getName().contains("equinox.launcher_");
			}
		});
		if (result.length == 0) {
			throw new MojoFailureException("Can't find equinox launcher in RCPTT dir at" + q7dir);
		}
		return result[result.length - 1];
	}

	/**
	 * To look up Archiver/UnArchiver implementations
	 *
	 * @component
	 */
	private ArchiverManager archiverManager;
	private ArchiveUtil archiveUtil;

	protected ArchiveUtil getArchiveUtil() {
		if (archiveUtil == null) {
			archiveUtil = new ArchiveUtil(archiverManager);
		}
		return archiveUtil;
	}

	private CoordResolver resolver;

	protected CoordResolver getCoordResolver() {
		if (resolver == null) {
			resolver = new CoordResolver(project.getBasedir(), this.outDir, repoSystem, repoSession, getLog());
		}
		return resolver;
	}

	/**
	 * The entry point to Aether, i.e. the component doing all the work.
	 *
	 * @component
	 */
	private RepositorySystem repoSystem;

	/**
	 * The current repository/network configuration of Maven.
	 *
	 * @parameter default-value="${repositorySystemSession}"
	 * @readonly
	 */
	private RepositorySystemSession repoSession;

}
