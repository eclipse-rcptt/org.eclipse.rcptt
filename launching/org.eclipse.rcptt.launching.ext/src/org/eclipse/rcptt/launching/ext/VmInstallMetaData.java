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

import static java.util.Arrays.stream;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
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
		Preconditions.checkArgument(!environments.isEmpty());
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
		IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
		Multimap<IVMInstall, IExecutionEnvironment> environments = HashMultimap.create();
		stream(manager.getExecutionEnvironments())
			.forEach(env -> stream(env.getCompatibleVMs()).forEach( vm -> environments.put(vm, env) ));
		return JDTUtils.installedVms().map(vm -> adapt(vm, environments.get(vm))).flatMap(Optional::stream);
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