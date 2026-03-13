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
package org.eclipse.rcptt.launching.ext;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Stream.generate;
import static java.util.stream.Stream.ofNullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ext.JDTUtils;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public final class VmInstallMetaData {
	public final IVMInstall install;
	public final OSArchitecture arch;
	public final Set<String> compatibleEnvironments;
	private VmInstallMetaData(IVMInstall install, OSArchitecture arch, Collection<String> environments) {
		super();
		Preconditions.checkArgument(!OSArchitecture.Unknown.equals(arch));
		this.install = Objects.requireNonNull(install);
		this.arch = Objects.requireNonNull(arch);
		Preconditions.checkArgument(!environments.isEmpty(), install.getInstallLocation() + " provides no execution environments");
		this.compatibleEnvironments = Set.copyOf(environments);
	}
	
	/** Converts a given install into a set of all supported architectures.
	 * Old JVMs could switch between x86_64 and x86 execution modes with a flag.
	 * This flag is no longer available, but we keep managing JVM architecture for historical reasons.
	 *  **/
	public static Stream<VmInstallMetaData> adapt(IVMInstall install) {
		try {
			OSArchitecture jvmArch = JDTUtils.detect(install);
			if (OSArchitecture.Unknown.equals(jvmArch)) {
				return Stream.empty();
			}
			IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
			Set<String> environments = Arrays.stream(manager.getExecutionEnvironments())
					.filter(env -> Arrays.stream(env.getCompatibleVMs()).anyMatch(install::equals))
					.map(IExecutionEnvironment::getId)
					.collect(Collectors.toSet());
			if (environments.isEmpty()) {
				return Stream.empty();
			}
			return Stream.of(new VmInstallMetaData(install, jvmArch, environments));
		} catch (CoreException e) {
			RcpttPlugin.log(e);
			return Stream.empty();
		}
	}
	
	public String formatVmContainerPath() {
		return String.format("org.eclipse.jdt.launching.JRE_CONTAINER/%s/%s",
				install.getVMInstallType().getId(), install.getName());
	}
	
	public static Stream<VmInstallMetaData> all() {
		var compatibilities = new Compatibilities();
		return JDTUtils.installedVms().map(compatibilities::adapt).flatMap(Optional::stream);

	}
	
	public static Stream<VmInstallMetaData> forEnvironment(String executionEnvironmentId) {
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		IExecutionEnvironment environment = manager.getEnvironment(executionEnvironmentId);
		var compatibilities = new Compatibilities();
		Stream<IVMInstall> currentJvm = generate(JDTUtils::registerCurrentJVM).limit(1).filter(install -> asList(environment.getCompatibleVMs()).contains(install));
		return List.<Stream<IVMInstall>>of(ofNullable(environment.getDefaultVM()), stream(environment.getCompatibleVMs()), currentJvm)
				.stream()
				.flatMap(identity())
				.distinct()
				.map(compatibilities::adapt)
				.flatMap(Optional::stream);
	}
	
	private static class Compatibilities {
		private Multimap<IVMInstall, IExecutionEnvironment> environments = HashMultimap.create();
		private final IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		{
			stream(manager.getExecutionEnvironments())
				.forEach(env -> stream(env.getCompatibleVMs()).forEach( vm -> environments.put(vm, env) ));
		}
		public Optional<VmInstallMetaData> adapt(IVMInstall install) {
			Collection<IExecutionEnvironment> compatible = environments.get(install);
			if (compatible.isEmpty()) {
				compatible = stream(manager.getExecutionEnvironments()).filter(env -> asList(env.getCompatibleVMs()).contains(install)).toList();
				assert !compatible.isEmpty() : install + " does not support any environments";
				environments.putAll(install, compatible);
			}
			return VmInstallMetaData.adapt(install, compatible); 
		}
	}

	
	private static Optional<VmInstallMetaData>  adapt(IVMInstall install, Collection<IExecutionEnvironment> environments) {
		try {
			OSArchitecture jvmArch = JDTUtils.detect(install);
			if (OSArchitecture.Unknown.equals(jvmArch)) {
				return Optional.empty();
			}
			return Optional.of(new VmInstallMetaData(install, jvmArch, environments.stream().map(IExecutionEnvironment::getId).toList()));
		} catch (CoreException e) {
			RcpttPlugin.log(e);
			return Optional.empty();
		}
	}

	public static Stream<VmInstallMetaData> register(Path location) throws CoreException {
		IVMInstall install = JDTUtils.registerVM(location.toFile());
		return adapt(install);
	}

}