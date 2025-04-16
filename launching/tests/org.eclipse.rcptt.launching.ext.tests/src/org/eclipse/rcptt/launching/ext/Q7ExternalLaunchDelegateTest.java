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
package org.eclipse.rcptt.launching.ext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.parser.EclCoreParser;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.ext.tests.DownloadCache;
import org.eclipse.rcptt.launching.ext.tests.DownloadCache.Request;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.io.Closer;

public class Q7ExternalLaunchDelegateTest {
	private final String NAME = getClass().getName();
	private final Closer closer = Closer.create();
	
	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	private final ConsoleCapture consoleCapture = new ConsoleCapture();
	
	@SuppressWarnings("resource")
	@Before
	public void before() throws CoreException, IOException {
		Q7TargetPlatformManager.delete(getClass().getName());
		AutManager.INSTANCE.getAuts().stream().filter(aut -> NAME.equals(aut.getName())).forEach(Aut::delete);
		VmInstallMetaData install = VmInstallMetaData.register(Path.of(System.getProperty("java.home")));
		JavaRuntime.setDefaultVMInstall(install.install, null);
		closer.register(consoleCapture);
	}
	
	@After
	public void after() throws IOException {
		closer.close();
	}
	
	@Test
	public void runtimeIsInjected() throws CoreException, IOException, InterruptedException {
		Path installDir = expandAut();
		assertFalse(readBundlesInfo(installDir).contains("org.aspectj.weaver"));
		AutLaunch launch = startAut(installDir, List.of("-consoleLog"));
		assertPluginIsInstalled(launch, "org.aspectj.weaver");
		assertPluginIsInstalled(launch, "com.ibm.icu"); // Present in JDT, should not be eliminated by RCPTT
		
		Command command = parse("invoke-static -pluginId \"org.eclipse.ui.workbench\" -className \"org.eclipse.ui.internal.ConfigurationInfo\" -methodName \"getSystemSummary\"");
		String result = launch.execute(command).toString();
		Pattern pattern = Pattern.compile("^org.eclipse.rcptt.logging \\(.*$", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(result);
		ArrayList<String> lines = new ArrayList<>();
		while(matcher.find()) {
			lines.add(matcher.group());
		}
		
		Set<String> unique = Set.copyOf(lines);
		assertFalse(lines.isEmpty());
		assertTrue(lines.toString(), lines.size() == unique.size());
		
		String output = consoleCapture.getOutput();
		assertFalse(output+"\nSystemSummary:\n" + result, output.contains("Unresolved requirement"));
	}

	private void assertPluginIsInstalled(AutLaunch aut, String symbolicName) throws CoreException, InterruptedException {
		Command command = parse("invoke-static -pluginId \"org.eclipse.core.runtime\" -className org.eclipse.core.runtime.Platform -methodName getBundle -args \""+symbolicName+"\" | invoke toString");
		String result = aut.execute(command).toString();
		assertTrue(result, result.startsWith(symbolicName+ "_"));

	}

	@SuppressWarnings("resource")
	private AutLaunch startAut(Path installDir, List<String> commandLineArguments)
			throws CoreException, IOException, InterruptedException {
		Aut aut = createAut(installDir, commandLineArguments);
		closer.register(aut::delete);
		return aut.launch(null);			
	}
	
	
		
	private Aut createAut(Path installDir, List<String> arguments) throws CoreException, IOException {
		ITargetPlatformHelper target = Q7TargetPlatformManager.createTargetPlatform(installDir.toString(), new NullProgressMonitor());
		target.setTargetName(NAME);
		ILaunchConfigurationWorkingCopy workingCopy = Q7LaunchingUtil
				.createLaunchConfiguration(target, NAME);
		workingCopy.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				String.join(" ", arguments));
		workingCopy.setAttribute(IPDELauncherConstants.LOCATION, temporaryFolder.newFolder("ws").toString());
		IExecutionEnvironment ee = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-21");
		String containerPath = JavaRuntime.newJREContainerPath(ee).toPortableString();
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, containerPath);
		
		return AutManager.INSTANCE.getByLaunch(workingCopy);
	}

	private String readBundlesInfo(Path installDir) throws IOException {
		Path macPath = installDir.resolve( "Contents/Eclipse");
		if (Files.isDirectory(macPath)) {
			installDir = macPath;
		}
		Path infoPath = installDir.resolve("configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
		return Files.readString(infoPath, StandardCharsets.UTF_8);
	}

	private final static DownloadCache CACHE = new DownloadCache(DownloadCache.DEFAULT_CACHE_ROOT);
	private Path expandAut() throws IOException, InterruptedException {
		Request request = switch (Platform.getOS()) {
		case Platform.OS_MACOSX -> request("https://archive.eclipse.org/technology/epp/downloads/release/2024-03/R/eclipse-java-2024-03-R-macosx-cocoa-aarch64.dmg", "77ae164c4b11d18f162b1ff97b088865469b2267033d45169e4f7f14694767bb98a25a3697b33233ed8bc5bb17eb18f214c59913581938f757887ff8bdef960b");
		case Platform.OS_LINUX -> request("https://archive.eclipse.org/technology/epp/downloads/release/2024-03/R/eclipse-java-2024-03-R-win32-x86_64.zip", "e90eb939cef8caada36a058bbed3a3b14c53e496f5feb439abc2e53332a4c71d3d43c02b8d202d88356eb318395551bce32db9d8e5e2fd1fc9e152e378dc325f");
		case Platform.OS_WIN32 -> request("https://archive.eclipse.org/technology/epp/downloads/release/2024-03/R/eclipse-java-2024-03-R-linux-gtk-x86_64.tar.gz", "973c94a0a029c29d717823a53c3a65f99fa53d51f605872c5dd854620e16cd4cb2c2ff8a5892cd550a70ab19e1a0a0d2e792661e936bd3d2bef4532bc533048b");
		default -> throw new IllegalStateException();
		};
		Path distribution = CACHE.download(request);
		Path installation = temporaryFolder.newFolder("extracted").toPath();
		return extractApplicationTo(distribution, installation);
	}
	
	private Path extractApplicationTo(Path archive, Path targetDirectory) throws IOException, InterruptedException {
		String name = archive.getFileName().toString();
		if (name.endsWith(".zip")) {
			FileUtil.unzip(archive.toFile(), targetDirectory.toFile());
			return targetDirectory;
		} else if (name.endsWith(".dmg")) {
			DmgExtract.extract(archive, targetDirectory);
			Path result = targetDirectory.resolve("Eclipse.app");
			Path config = result.resolve("Contents/Eclipse/configuration/config.ini");
			if (!Files.isRegularFile(config)) {
				throw new AssertionError(config.toString());
			}
			return result;
		} else {
			throw new IllegalArgumentException(archive.toString());
		}
	}

	private DownloadCache.Request request(String uri, String sha512) {
		return new Request(URI.create(uri), sha512);
	}

	private Command parse(String input) throws CoreException {
		return EclCoreParser.newCommand(input);
	}
}
