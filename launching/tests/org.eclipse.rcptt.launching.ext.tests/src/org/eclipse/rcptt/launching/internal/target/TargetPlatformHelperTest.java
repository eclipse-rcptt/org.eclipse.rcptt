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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class TargetPlatformHelperTest {
	private static final ILog LOG = Platform.getLog(TargetPlatformHelperTest.class);

	
	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	/**
	 * @throws IOException 
	 * @see https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/160
	 */
	@Test
	public void detectArchitecture() throws CoreException, IOException {
		ITargetPlatformService service = P2Utils.getTargetService();
		Path targetFile = temporaryFolder.newFile("target.target").toPath();
		try (InputStream is = getClass().getResourceAsStream("wrongLauncher.target")) {
			Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}
		ITargetHandle handle = service.getTarget(targetFile.toUri());
		ITargetDefinition definition = handle.getTargetDefinition();
		TargetPlatformHelper subject = new TargetPlatformHelper(definition);
		throwIfProblem(subject.resolve(null));
		Assert.assertEquals(OSArchitecture.aarch64, subject.detectArchitecture(null));
	}

	private void throwIfProblem(IStatus iStatus) throws CoreException {
		if (iStatus.matches(IStatus.ERROR|IStatus.CANCEL)) {
			LOG.log(iStatus);
			throw new CoreException(iStatus);
		}
	}

}
