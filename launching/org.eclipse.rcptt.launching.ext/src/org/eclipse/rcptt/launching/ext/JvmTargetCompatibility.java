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

import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static org.eclipse.core.runtime.IProgressMonitor.done;
import static org.eclipse.core.runtime.Status.error;
import static org.osgi.framework.Version.parseVersion;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.launching.CheckedExceptionWrapper;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper.Model;
import org.osgi.framework.Version;

import com.google.common.base.Joiner;

public final class JvmTargetCompatibility {
	private final ITargetPlatformHelper target;
	private OSArchitecture architecture = OSArchitecture.Unknown;
	
	public JvmTargetCompatibility(ITargetPlatformHelper target) throws CoreException {
		this.target = Objects.requireNonNull(target);
		if (!target.isResolved()) {
			throw new IllegalArgumentException("Resolved target expected");
		}
	}
	
	private boolean isCompatible(VmInstallMetaData jvm) {
		try {
			if (getArchitecture() != jvm.arch) {
				return false;
			}
		} catch (CoreException e) {
			return false;
		}
		
		if (!findCompatibilityProblems(jvm.compatibleEnvironments).isEmpty()) {
			return false;
		}
		
		return true;
	}
	
	public IStatus checkCompatibilty(IVMInstall install) {
		if (install == null) {
			return error("The selected JVM can not be found. Ensure it is installed.");
		}
		try {
			// Do not do the scan below if isCompatible() would always be false
			getArchitecture();
		} catch (CoreException e) {
			return e.getStatus();
		}
		MultiStatus result = new MultiStatus(getClass(), 0, "Compatibility for " + install.getInstallLocation() + " and " + target.getName());
		List<VmInstallMetaData> architectures = VmInstallMetaData.adapt(install).toList();
		Optional<VmInstallMetaData> vmInstallOption = architectures.stream()
			.filter(this::isCompatible)
			.findFirst();
		
		if (!vmInstallOption.isPresent()) {
			architectures.stream().map(this::checkCompatibilty).forEach(result::add);
			assert result.matches(IStatus.ERROR);
			result.add(recommendJVM());
			return result;
		}
		return Status.OK_STATUS;
	}

	private IStatus recommendJVM() {
		return findVM().findFirst().map(vm -> 
			Status.info("Recommended JVM is : " + vm.install.getName() + " (" +vm.install.getInstallLocation() + ")")
		).orElse(Status.error("There are no compatible JVMs among installed. Install and configure a new one"));
	}
	
	public VmInstallMetaData selectCompatibile(Collection<VmInstallMetaData> architectures) throws CoreException {
		if (architectures.isEmpty()) {
			throw new CoreException(error("Please select a JVM"));
		}

		MultiStatus result = new MultiStatus(getClass(), 0, "Checking if selected JVM is compatible with " + target.getName());
		Optional<VmInstallMetaData> vmInstallOption = architectures.stream()
			.filter(this::isCompatible)
			.findFirst();
		
		if (vmInstallOption.isPresent()) {
			return vmInstallOption.get();
		}
		architectures.stream().map(this::checkCompatibilty).forEach(result::add);
		assert result.matches(IStatus.ERROR);
		result.add(recommendJVM());
		throw new CoreException(result);
	}
	
	private IStatus checkCompatibilty(VmInstallMetaData install) {
		MultiStatus result = new MultiStatus(getClass(), 0, "Compatibility for " + install.install.getInstallLocation() + " and " + target.getName());
		try {
			result.add(Status.info("AUT requirements: " + explainJvmRequirements()));
		} catch (CoreException e) {
			result.add(e.getStatus());
			return result;
		}
		result.add(Status.info("JVM architecture: " + install.arch));
		try {
			if (getArchitecture() != install.arch) {
				result.add(Status.error("Architecture mismatch, AUT has " + getArchitecture() + " architecture"));
				return result;
			}
		} catch (CoreException e) {
			result.add(e.getStatus());
			return result;
		}
		String compatibilityProblems = findCompatibilityProblems(install.compatibleEnvironments);
		if (!compatibilityProblems.isEmpty()) {
			result.add(Status.error(compatibilityProblems));
			return result;
		}
		
		return result;
	}
	
	private OSArchitecture getArchitecture() throws CoreException {
		if (architecture != OSArchitecture.Unknown) {
			return architecture;
		}
		StringBuilder error = new StringBuilder();
		this.architecture = target.detectArchitecture(error);
		if (this.architecture == OSArchitecture.Unknown || this.architecture == null) {
			throw new CoreException(Status.error(error.toString()));
		}
		return architecture;
	}
	
	public boolean isCompatibleJvmPresent() {
		return findVM().findAny().isPresent();
	}
	
	public Stream<VmInstallMetaData> findVM() {
		return VmInstallMetaData.all()
			.filter(this::isCompatible);
	}

	public IStatus validate(IProgressMonitor monitor) {
		SubMonitor sm = SubMonitor.convert(monitor, 2);
		try {
			IStatus status = target.resolve(sm.split(1, SubMonitor.SUPPRESS_NONE));
			if (status.matches(IStatus.ERROR | IStatus.CANCEL)) {
				return status;
			}
			
			Optional<VmInstallMetaData> vmOptional = findVM().findAny();
			if (vmOptional.isEmpty()) {
				return Status.error("No compatible JVM is configured:\n" + explainJvmRequirements());
			}
			return Q7LaunchDelegateUtils.validateForLaunch(target, sm.split(1, SubMonitor.SUPPRESS_NONE), vmOptional.get().install);
		} catch (CheckedExceptionWrapper e) {
			if (e.getCause() instanceof CoreException ce) {
				return ce.getStatus();
			}
			e.rethrowUnchecked();
			throw e;
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
			done(monitor);
		}
	}
	
	private String findCompatibilityProblems(Set<String> ids) {
		if (ids.contains("JavaSE-25")) {
			if (Objects.equals(target.systemProperties().get("java.security.manager"), "allow")) {
				return "A configuration option (from eclipse.ini or configuration/config.ini) has attempted to allow or enable the Security Manager. Enabling a Security Manager is not supported since Java 25";
			}
		}
		List<String> problems = target.getModels().map(Model::model).map(model -> isCompatible(model, ids)).filter(not(String::isEmpty)).limit(10).toList();
		if (!problems.isEmpty()) {
			return Joiner.on("\n").join(problems);
		}
		return "";
	}

	public String explainJvmRequirements() throws CoreException {
		StringBuilder result = new StringBuilder();
		OSArchitecture arch = getArchitecture();
		result.append("Architecture: ").append(arch).append("\n");
		Set<String> validEnvironments = target.getModels().map(Model::model).map(JvmTargetCompatibility::getExecutionEnironments).flatMap(Arrays::stream).collect(Collectors.toCollection(HashSet::new));
		validEnvironments.removeIf( e -> 
			!findCompatibilityProblems(Set.of(e)).isEmpty()
		);
		result.append("Plugins require one of ").append(validEnvironments).append("\n");
		return result.toString();
	}
	
	
	
	private static final String[] EMPTY = new String[0];
	private static String[] getExecutionEnironments(IPluginModelBase plugin) {
		if (plugin.isFragmentModel()) {
			return EMPTY;
		}
		
		BundleDescription description = plugin.getBundleDescription();
		if (description == null) {
			return EMPTY;
		}
		String[] executionEnvironments = description.getExecutionEnvironments();
		return executionEnvironments;
	}

	/** @see org.eclipse.pde.internal.launching.launcher.VMHelper.getDefaultEEName(Set<IPluginModelBase>) **/
	private static String isCompatible(IPluginModelBase plugin, Set<String> providedEnvironments) {
		String id = plugin.getPluginBase().getId();
		Version version = parseVersion(plugin.getPluginBase().getVersion());
		
		if (id.equals("org.aspectj.weaver")) {
			if (version.compareTo(parseVersion("1.9.22.202405141514")) <= 0) {
				if (providedEnvironments.contains("JavaSE-23")) {
					return incompatibleMessage(plugin, "JavaSE-23");
				}
			}
			if (version.compareTo(parseVersion("1.9.24.202511030914")) < 0) {
				if (providedEnvironments.contains("JavaSE-25")) {
					return incompatibleMessage(plugin, "JavaSE-25");
				}
			}
		}

		String[] executionEnvironments = getExecutionEnironments(plugin);
		if (executionEnvironments.length == 0) {
			return "";
		}
		if (!stream(executionEnvironments).anyMatch(providedEnvironments::contains)) {
			return "Plugin " + plugin.getPluginBase().getId() + " is only compatible with " + Arrays.toString(executionEnvironments);
		}
		
		return "";
	}
	
	private static String incompatibleMessage(IPluginModelBase plugin, String subject) {
		return "Plugin " + plugin.getPluginBase().getId()+"_" + plugin.getPluginBase().getVersion() + " is incompatible with " + subject;
	}

}
