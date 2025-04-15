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
package org.eclipse.rcptt.launching.ext.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.parser.EclCoreParser;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.ext.Q7LaunchingUtil;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.util.FileUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class Q7ExternalLaunchDelegateTest {
	private final String NAME = getClass().getName();
	
	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Before
	public void before() throws CoreException {
		Q7TargetPlatformManager.delete(getClass().getName());
		AutManager.INSTANCE.getAuts().stream().filter(aut -> NAME.equals(aut.getName())).forEach(Aut::delete);
	}

	@Test
	public void aspectjIsInjected() throws CoreException, IOException, InterruptedException {
		Path installDir = expandAut();
		assertFalse(readBundlesInfo(installDir).contains("org.aspectj.weaver"));
		Aut aut = createAut(installDir);
		try {
			Command failingCommand = parse("invoke-static -pluginId \"org.eclipse.core.runtime\" -className org.eclipse.core.runtime.Platform -methodName getBundle -args \"org.aspectj.weaver\" | invoke toString");
			String result = aut.launch(null).execute(failingCommand).toString();
			assertTrue(result, result.startsWith("org.aspectj.weaver_"));
		} finally {
			aut.delete();
		}
	}
	
	private Aut createAut(Path installDir) throws CoreException, IOException {
		ITargetPlatformHelper target = Q7TargetPlatformManager.createTargetPlatform(installDir.toString(), new NullProgressMonitor());
		target.setTargetName(NAME);
		ILaunchConfigurationWorkingCopy workingCopy = Q7LaunchingUtil
				.createLaunchConfiguration(target, NAME);
		workingCopy.setAttribute(IPDELauncherConstants.LOCATION, temporaryFolder.newFolder("ws").toString());
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

	private Path expandAut() throws IOException {
		Path subPath = switch (Platform.getOS()) {
		case Platform.OS_MACOSX -> Path.of("macosx/cocoa/aarch64/rcptt.app");
		case Platform.OS_LINUX -> Path.of("linux/gtk/x86_64/rcptt");
		case Platform.OS_WIN32 -> Path.of("win32/win32/x86_64/rcptt");
		default -> throw new IllegalStateException();
		};
		Path originalAut = Path.of(
				"/Users/vasiligulevich/git/org.eclipse.rcptt/repository/full/target/products/org.eclipse.rcptt.platform.product").resolve(subPath);
		File autCopy = temporaryFolder.newFolder("aut");
		FileUtil.copyFiles(originalAut.toFile(), autCopy);
		return autCopy.toPath();
	}

	private Command parse(String input) throws CoreException {
		return EclCoreParser.newCommand(input);
	}
}
