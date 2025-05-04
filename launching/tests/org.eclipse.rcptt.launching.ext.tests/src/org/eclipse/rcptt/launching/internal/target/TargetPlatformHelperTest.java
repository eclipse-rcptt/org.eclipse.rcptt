package org.eclipse.rcptt.launching.internal.target;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.launching.p2utils.P2Utils;
import org.junit.After;
import org.junit.Assert;
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

	@Test
	public void detectArchitecture() throws CoreException, IOException {
		ITargetHandle handle = createTarget("wrongLauncher.target");
		ITargetDefinition definition = handle.getTargetDefinition();
		TargetPlatformHelper subject = new TargetPlatformHelper(definition);
		throwIfProblem(subject.resolve(null));
		Assert.assertEquals(OSArchitecture.aarch64, subject.detectArchitecture(null));
	}
	
	/**
	 * @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/160
	 */
	@Test
	public void respectExplicitArchitecture() throws CoreException, IOException {
		ITargetHandle handle = createTarget("wrongLauncher.target");
		ITargetDefinition definition = handle.getTargetDefinition();
		definition.setArch("x86_64");
		TargetPlatformHelper subject = new TargetPlatformHelper(definition);
		throwIfProblem(subject.resolve(null));
		Assert.assertEquals(OSArchitecture.x86_64, subject.detectArchitecture(null));
	}


	@SuppressWarnings("resource")
	private ITargetHandle createTarget(String resource) {
		try {
			ITargetPlatformService service = P2Utils.getTargetService();
			Path targetFile = temporaryFolder.newFile("target.target").toPath();
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
