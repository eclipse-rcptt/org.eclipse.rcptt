/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.launching.ext;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.launching.DetectVMInstallationsJob;
import org.eclipse.jdt.internal.launching.LaunchingPlugin;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

import com.google.common.base.Preconditions;

@SuppressWarnings("restriction")
public class JDTUtils {
	private static final String SUN_ARCH_DATA_MODEL = "sun.arch.data.model";
	private static final String JAVA_VM_INFO = "java.vm.info";
	private static final String OS_ARCH = "os.arch";
	private static final ILog LOG = Platform.getLog(JDTUtils.class);

	public static IVMInstall registerCurrentJVM() {
		try {
			return registerVM(new File(System.getProperty("java.home")));
		} catch (CoreException e) {
			LOG.log(e.getStatus());
			throw new IllegalStateException(e);
		}
	}

	static class GetPropertiesThread extends Thread {

		private Map<String, String> map = new HashMap<String, String>();
		private CoreException coreException;

		private final AbstractVMInstall install;
		private final IProgressMonitor monitor;
		private final boolean d32;

		public Map<String, String> getMap() {
			return map;
		}

		public CoreException getCoreException() {
			return coreException;
		}

		public GetPropertiesThread(AbstractVMInstall install,
				IProgressMonitor monitor, boolean d32) {
			this.install = install;
			this.monitor = new ProgressMonitorWrapper(monitor) {
				@Override
				public boolean isCanceled() {
					return super.isCanceled() || Thread.interrupted();
				}
			};
			this.d32 = d32;
		}

		@Override
		public void run() {
			try {
				map = evaluateSystemProperties(install, monitor, d32);
			} catch (CoreException e) {
				coreException = e;
			}
		}
	}

	public static Map<String, String> evaluateSystemPropertiesInThread(
			AbstractVMInstall install, IProgressMonitor monitor, boolean d32,
			int timeout) throws CoreException {
		GetPropertiesThread thread = new GetPropertiesThread(install, monitor,
				d32);
		thread.start();
		try {
			thread.join(timeout);
		} catch (InterruptedException e) {
			RcpttPlugin.log(e);
		}
		thread.interrupt();
		if (thread.getCoreException() != null) {
			throw thread.getCoreException();
		}
		return thread.getMap();
	}

	protected static void abort(String message, Throwable exception, int code)
			throws CoreException {
		throw new CoreException(
				new Status(IStatus.ERROR,
						LaunchingPlugin.getUniqueIdentifier(), code, message,
						exception));
	}

	public static Map<String, String> evaluateSystemProperties(
			AbstractVMInstall install, IProgressMonitor monitor, boolean d32)
			throws CoreException {
		String[] properties = new String[] { OS_ARCH, "java.version",
				"java.specification.name", "java.specification.version",
				JAVA_VM_INFO, "java.vm.name", "java.vm.version",
				"java.runtime.name", "java.runtime.version",
				SUN_ARCH_DATA_MODEL };
		return install.evaluateSystemProperties(properties, monitor);
	}

	public static Map<String, String> getProperties(IVMInstall install) throws CoreException {
		if (install instanceof AbstractVMInstall) {
			AbstractVMInstall avi = (AbstractVMInstall) install;
			Map<String, String> properties = evaluateSystemPropertiesInThread(
					avi, new NullProgressMonitor(), false, 60000);
			return properties;
		}
		throw new CoreException(
				RcpttPlugin.createStatus("Failed to get JVM properties. Unknown JVM installation type: "
						+ install.getClass().getName()));
	}

	public static OSArchitecture detect(IVMInstall install) throws CoreException {
		Preconditions.checkNotNull(install);
		try {
			return detect(getProperties(install));
		} catch (CoreException e) {
			throw new CoreException(RcpttPlugin.createStatus("Failed to get JVM arhitecture for "
					+ install.getInstallLocation(), e));
		}
	}

	private static OSArchitecture detect(Map<String, String> properties) throws CoreException {
		Object arch = properties.get(OS_ARCH);
		if ("amd64".equals(arch)) {
			return OSArchitecture.x86_64;
		}
		if ("x86_64".equals(arch)) {
			return OSArchitecture.x86_64;
		}
		if ("i386".equals(arch)) {
			return OSArchitecture.x86;
		}
		if ("i686".equals(arch)) {
			return OSArchitecture.x86;
		}
		if ("i586".equals(arch)) {
			return OSArchitecture.x86;
		}
		if ("aarch64".equals(arch)) {
			return OSArchitecture.aarch64;
		}
		
		// Fallback to deprecated fields
		Object model = properties.get(SUN_ARCH_DATA_MODEL);
		if (model != null && "32".equals(model)) {
			return OSArchitecture.x86;
		}
		if (model != null && "64".equals(model)) {
			return OSArchitecture.x86_64;
		}

		String message = String.format("Unknown combination:  %s = %s and %s = %s", SUN_ARCH_DATA_MODEL, model,
				OS_ARCH,
				arch);
		throw new CoreException(RcpttPlugin.createStatus(message));
	}

	public static boolean canRun32bit(IVMInstall install) {
		return false;
	}
	
	private static final AtomicInteger FREE_ID = new AtomicInteger(1);
	public static IVMInstall registerVM(File jvmInstallationLocation) throws CoreException {
		File jvmInstallationLocationCopy;
		try {
			jvmInstallationLocationCopy = jvmInstallationLocation.getCanonicalFile();
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid VM path " + jvmInstallationLocation, e);
		}
		if (!jvmInstallationLocationCopy.exists()) {
			throw new CoreException(Status.error("JVM location " + jvmInstallationLocationCopy + " does not exist"));
		}
		Optional<IVMInstall> first = installedVms().filter(i -> {
			try {
				return i.getInstallLocation().getCanonicalFile().equals(jvmInstallationLocationCopy);
			} catch (IOException e) {
				LOG.error("Invalid JVM path " + i.getInstallLocation());
				return false;
			}
		}).findFirst();
		if (first.isPresent()) {
			return first.get();
		}
		
		MultiStatus multiStatus = new MultiStatus(JDTUtils.class, 0, "Can't register JVM " + jvmInstallationLocationCopy);
		for (IVMInstallType type: JavaRuntime.getVMInstallTypes()) {
			IStatus status = type.validateInstallLocation(jvmInstallationLocationCopy);
			if (status.matches(IStatus.CANCEL)) {
				throw new CoreException(status);
			}
			multiStatus.add(status);
			if (status.matches(IStatus.ERROR)) {
				continue;
			}
			String id, name;
			Object found;
			do {
				name = "RCPTT JVM " + FREE_ID.getAndIncrement();
				id = name.replace(' ', '_');
				found = type.findVMInstall(id);
			} while (found != null);
			IVMInstall install = type.createVMInstall(id);
			install.setName(name);
			install.setInstallLocation(jvmInstallationLocationCopy);
			return install;
		}
		multiStatus.add(Status.error("No compatible VM types found"));
		throw new CoreException(multiStatus);
	}


	public static final Stream<IVMInstall> installedVms() {
		Stream<IVMInstall> preinstalled = registeredInstalls();
		Stream<IVMInstall> currentJVM = Stream.generate(JDTUtils::registerCurrentJVM).limit(1);
		return Stream.concat(preinstalled, currentJVM);
		
	}
	
	private static Stream<IVMInstall> registeredInstalls() {
		JavaRuntime.getVMInstallTypes();
		try {
			Job.getJobManager().join(DetectVMInstallationsJob.class, null);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new OperationCanceledException();
		}
		return Arrays.stream(JavaRuntime.getVMInstallTypes())
		.map(IVMInstallType::getVMInstalls)
		.flatMap(Arrays::stream);
	}
	

		
}
