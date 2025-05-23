package org.eclipse.rcptt.launching.internal.target;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.launching.p2utils.P2Utils;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.io.Closer;

public class TargetPlatformHelperTest {
	private static final ILog LOG = Platform.getLog(TargetPlatformHelperTest.class);
	
	private final Closer closer = Closer.create();

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@After
	public void after() throws IOException {
		closer.close();
	}
	
	/**
	 * @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/160#issuecomment-2893733908
	 */
	@Test
	public void mavenType() throws CoreException, IOException {
		ITargetHandle handle = createTarget("maven.target");
		ITargetDefinition definition = handle.getTargetDefinition();
		TargetPlatformHelper subject = new TargetPlatformHelper(definition);
		throwIfProblem(subject.resolve(null));
		assertTrue(
				subject.getModels().map(TargetPlatformHelper.Model::model).map(IPluginModelBase::getBundleDescription)
						.map(BundleDescription::getSymbolicName).anyMatch(Predicate.isEqual("net.bytebuddy.byte-buddy")));
		assertTrue(
				subject.getModels().map(TargetPlatformHelper.Model::model).map(IPluginModelBase::getBundleDescription)
						.map(BundleDescription::getSymbolicName).anyMatch(Predicate.isEqual("net.bytebuddy.byte-buddy.source")));
	}

	/**
	 * @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/160
	 */
	@Test
	public void detectArchitecture() throws CoreException, IOException {
		String os = Platform.getOS();
		int count = 0;
		for (String arch: Platform.knownOSArchValues()) {
			String resourceName = String.format("%s_%s.target", os, arch);
			if (!resourceExists(resourceName)) {
				continue;
			}
			count++;
			ITargetHandle handle = createTarget(resourceName);
			ITargetDefinition definition = handle.getTargetDefinition();
			TargetPlatformHelper subject = new TargetPlatformHelper(definition);
			throwIfProblem(subject.resolve(null));
			Assert.assertEquals(OSArchitecture.valueOf(arch), subject.detectArchitecture(null));
		}
		Assume.assumeTrue(count > 0);
	}
	
	@Test
	public void zippedRepoisotry() throws CoreException, URISyntaxException, IOException {
        Class<? extends TargetPlatformHelperTest> clazz = getClass();
		Path bundleDir = getFragmentSourceDirectory(clazz);
		// /git/org.eclipse.rcptt/launching/tests/org.eclipse.rcptt.launching.ext.tests/about.html
		assertTrue(bundleDir.toString(), Files.exists(bundleDir.resolve("about.html")));
		// /git/org.eclipse.rcptt/repository/core/target/core-repository-2.6.0-SNAPSHOT.zip
		Path coreTarget = bundleDir.getParent().getParent().getParent().resolve("repository/core/target");
		assertTrue(coreTarget.toString(), Files.isDirectory(coreTarget));
		@SuppressWarnings("resource")
		FileSystem fileSystem = coreTarget.getFileSystem();
		PathMatcher matcher = fileSystem.getPathMatcher("glob:core-repository-*-SNAPSHOT.zip");
		Path repo;
		try (Stream<Path> stream = Files.find(coreTarget, 1, (path, ignored) -> matcher.matches(coreTarget.relativize(path)))) {
			repo = stream.findFirst().get();
		}
		assertTrue(Files.exists(repo));
		ITargetPlatformService service = P2Utils.getTargetService();
		// @see https://github.com/eclipse-pde/eclipse.pde/issues/1789 ITargetPlatformService.newIULocation() requires non-API flags
		int flags = IUBundleContainer.INCLUDE_REQUIRED |  IUBundleContainer.INCLUDE_CONFIGURE_PHASE;
		ITargetLocation location = service.newIULocation(new String[] {"com.ibm.icu"}, new String[] {"0.0.0"}, new URI[] {URI.create("jar:" + repo.toUri() + "!/")}, flags);
		ITargetDefinition definition = service.newTarget();
		definition.setTargetLocations(new ITargetLocation[] {location} );
		TargetPlatformHelper subject = new TargetPlatformHelper(definition);
		throwIfProblem(subject.resolve(null));
		assertTrue(subject.getModels().map(ITargetPlatformHelper.Model::model).map(IPluginModelBase::getBundleDescription).map(BundleDescription::getSymbolicName).anyMatch(Predicate.isEqual("com.ibm.icu")));
		
	}

	private Path getFragmentSourceDirectory(Class<?> clazz) throws URISyntaxException {
		CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        URL location2 = codeSource.getLocation();
        Path bundleDir = Path.of(location2.toURI());
		assertTrue(location2.toString(), Files.isDirectory(bundleDir));
		// /git/org.eclipse.rcptt/launching/tests/org.eclipse.rcptt.launching.ext.tests/target/classes
		if (bundleDir.endsWith("target/classes")) {
			bundleDir = bundleDir.getParent().getParent();
		}
		return bundleDir;
	}

	private boolean resourceExists(String resourceName) {
		try (InputStream is = getClass().getResourceAsStream(resourceName)) {
			return is != null;
		} catch(IOException e) {
			return false;
		}
	}

	@SuppressWarnings("resource")
	private ITargetHandle createTarget(String resource) {
		try {
			ITargetPlatformService service = P2Utils.getTargetService();
			Path targetFile = temporaryFolder.newFile().toPath();
			try (InputStream is = getClass().getResourceAsStream(resource)) {
				Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
			}
			ITargetHandle handle = service.getTarget(targetFile.toUri());
			closer.register(() -> {
				try {
					service.deleteTarget(handle);
				} catch (CoreException e) {
					LOG.log(e.getStatus());
					throw new IOException(e);
				}
			});
			return handle;
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private void throwIfProblem(IStatus iStatus) throws CoreException {
		if (iStatus.matches(IStatus.ERROR | IStatus.CANCEL)) {
			LOG.log(iStatus);
			throw new CoreException(iStatus);
		}
	}

}
