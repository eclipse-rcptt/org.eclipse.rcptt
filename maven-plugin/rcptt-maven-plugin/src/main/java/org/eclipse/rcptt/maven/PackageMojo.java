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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Packages two artifacts: - project with q7 tests itself - test results -
 * q7report.xml - q7 stdout/stderr - workspace log - aut stdout/stderr
 *
 * @author ivaninozemtsev
 *
 * @goal package
 * @phase package
 */
public class PackageMojo extends AbstractRCPTTMojo {
	/**
	 * @component
	 */
	private MavenProjectHelper helper;
	private static final String RESULTS_CLASSIFIER = "results";

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		createMainArtifact();
		if (skipTests()) {
			return;
		}
		if (!projectHasTests()) {
			getLog().info("No RCPTT tests found in project, skipping result artifact packing");
			return;
		}
		createResultsArtifact();
		failBuildIfFailingTests();
	}

	private void failBuildIfFailingTests() throws MojoFailureException {
		if (testFailureIgnore) {
			return;
		}
		File junitReport = resolveJunitFile();
		if (!junitReport.exists()) {
			throw readError(junitReport);
		}

		try (FileInputStream inputStream = new FileInputStream(junitReport)) {
			XmlPullParser parser = new MXParser();
			parser.setInput(inputStream, "UTF-8");
			int tag = parser.nextTag();
			if (tag != XmlPullParser.START_TAG || !parser.getName().equals("testsuite")) {
				throw invalidFormat(junitReport);
			}

			Integer failures = parseInt(parser.getAttributeValue(null, "failures"));
			if (failures == null) {
				throw invalidFormat(junitReport);
			}
			if (failures > 0) {
				throw new MojoFailureException("There are test failures");
			}

		} catch (IOException e) {
			throw readError(junitReport);
		} catch (XmlPullParserException e) {
			throw readError(junitReport);
		}
	}

	private final File resolveJunitFile() throws MojoFailureException {
		File file = getJUnitReportFile();
		if (file.exists()) {
			return file;
		}
		// Q7 runner alters JUnit file name, so we need to convert a new name
		// to what runner returns
		String name = file.getName();
		String newName = String.format("%s.junit.xml", name.substring(0, name.length() - ".xml".length()));
		return new File(file.getParentFile(), newName);
	}

	private static Integer parseInt(String val) {
		if (val == null) {
			return null;
		}
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static MojoFailureException readError(File file) {
		return new MojoFailureException(String.format("Could not read test results file %s", file));
	}

	private static MojoFailureException invalidFormat(File file) {
		return new MojoFailureException(String.format("Invalid JUnit XML format in file %s", file.getAbsolutePath()));
	}

	private void createResultsArtifact() throws MojoExecutionException {
		// now, we need to create an extra artifact with test results
		File resultsDest = new File(outDir, String.format("%s-%s-%s-result.zip", project.getGroupId(),
				project.getArtifactId(), project.getVersion()));
		Archiver inflater = getArchiveUtil().getArchiver();
		inflater.setDestFile(resultsDest);

		try {
			addQ7Logs(inflater);
			addAutWsLogs(inflater);
			addAutOuts(inflater);
			addLaunchInfo(inflater);

			inflater.createArchive();
		} catch (Exception e) {
			throw new MojoExecutionException("Error packaging test results", e);
		}
		helper.attachArtifact(project, resultsDest, RESULTS_CLASSIFIER);
	}

	private void addLaunchInfo(Archiver inflater) throws MojoFailureException, ArchiverException {
		File launchDir = new File(
				new File(new File(getMetadataDir(getQ7WsDir()), ".plugins"), "org.eclipse.debug.core"), ".launches");
		if (!launchDir.exists() || !launchDir.isDirectory()) {
			return;
		}
		for (File launchFile : launchDir.listFiles()) {
			int extIndex = launchFile.getName().lastIndexOf(".launch");
			if (extIndex == -1) {
				continue;
			}

			String name = launchFile.getName().substring(0, extIndex);
			inflater.addFile(launchFile, String.format("launch/%s/launch.xml", name));

			File launchInfoDir = new File(
					new File(new File(getMetadataDir(getQ7WsDir()), ".plugins"), "org.eclipse.pde.core"), name);
			if (!launchInfoDir.exists()) {
				continue;
			}
			DefaultFileSet set = new DefaultFileSet();
			set.setDirectory(launchInfoDir);
			set.setPrefix(String.format("launch/%s/", name));
			inflater.addFileSet(set);
		}
	}

	private void addQ7Logs(Archiver inflater) throws MojoFailureException, ArchiverException {
		if (getQ7ReportFile().exists()) {
			inflater.addFile(getQ7ReportFile(), "rcptt/rcptt.report");
		}
		if (getHtmlReportFile().exists()) {
			inflater.addFile(getHtmlReportFile(), "rcptt/report.html");
		}
		inflater.addFile(getQ7Out(), "rcptt/stdout.txt");
		inflater.addFile(getQ7Err(), "rcptt/stderr.txt");
		addLogs(inflater, getQ7WsDir(), "rcptt", "log");
	}

	private void addAutOuts(Archiver inflater) throws MojoFailureException, ArchiverException, IOException {
		String consolePrefix = getAutConsolePrefix().getName();
		File consoleDir = getAutConsolePrefix().getParentFile();
		final Pattern autOutPattern = Pattern.compile(String.format("%s(\\d+).+", consolePrefix));
		for (File file : consoleDir.listFiles()) {
			Matcher m = autOutPattern.matcher(file.getName());
			if (!m.find()) {
				continue;
			}
			int wsIndex = Integer.parseInt(m.group(1));
			inflater.addFile(file, String.format("aut%d/out.txt", wsIndex + 1));
		}
	}

	private void addAutWsLogs(Archiver inflater) throws ArchiverException {
		// collect all AUT workspaces (including restarts)
		String autPrefix = getAutWorkspacePrefix().getName();
		File autWsDir = getAutWorkspacePrefix().getParentFile();

		final Pattern autPattern = Pattern.compile(String.format("%s(\\d+)(_restarted_(\\d+))?", autPrefix));

		for (File file : autWsDir.listFiles()) {
			Matcher m = autPattern.matcher(file.getName());
			if (!m.find()) {
				continue;
			}
			int wsIndex = Integer.parseInt(m.group(1));
			String destDir = String.format("aut%d", wsIndex + 1);
			String destPrefix = "log";
			if (m.group(3) != null) {
				int restartIndex = Integer.parseInt(m.group(3));
				destPrefix = String.format("restart%dlog", restartIndex);
			}
			addLogs(inflater, file, destDir, destPrefix);
		}
	}

	private static final Pattern logPattern = Pattern.compile("(.bak_(\\d+))?.log");

	private void addLogs(Archiver inflater, File wsDir, String destDir, String filePrefix) throws ArchiverException {
		if (!getMetadataDir(wsDir).exists()) {
			return;
		}

		for (File file : getMetadataDir(wsDir).listFiles()) {
			Matcher m = logPattern.matcher(file.getName());
			if (!m.find()) {
				continue;
			}
			String name = String.format("%s.txt", filePrefix);
			if (m.group(2) != null) {
				name = String.format("%s%s.txt", filePrefix, m.group(2));
			}
			inflater.addFile(file, String.format("%s/%s", destDir, name));
		}
	}

	private void createMainArtifact() throws MojoFailureException, MojoExecutionException {
		File projectDir = getThisProjectDir();
		File destination = new File(projectDir.getParent(),
				String.format("%s-%s.zip", project.getArtifactId(), project.getVersion()));
		getArchiveUtil().compressDir(projectDir, destination);
		// main artifact
		project.getArtifact().setFile(destination);
	}

}
