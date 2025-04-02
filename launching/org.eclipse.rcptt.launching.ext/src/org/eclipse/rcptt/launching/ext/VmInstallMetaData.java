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

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ext.JDTUtils;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;

import com.google.common.base.Preconditions;

public final class VmInstallMetaData {
	public final IVMInstall install;
	public final OSArchitecture arch;
	private VmInstallMetaData(IVMInstall install, OSArchitecture arch) {
		super();
		Preconditions.checkArgument(!OSArchitecture.Unknown.equals(arch));
		this.install = Objects.requireNonNull(install);
		this.arch = Objects.requireNonNull(arch);
	}
	
	public static Optional<VmInstallMetaData> adapt(IVMInstall install) {
		try {
			OSArchitecture jvmArch = JDTUtils.detect(install);
			if (OSArchitecture.Unknown.equals(jvmArch)) {
				return Optional.empty();
			}
			return Optional.of(new VmInstallMetaData(install, jvmArch));
		} catch (CoreException e) {
			RcpttPlugin.log(e);
			return Optional.empty();
		}
	}
	
	public String formatVmContainerPath() {
		return String.format("org.eclipse.jdt.launching.JRE_CONTAINER/%s/%s",
				install.getVMInstallType().getId(), install.getId());
	}
	
	public static Stream<VmInstallMetaData> all() {
		return JDTUtils.installedVms().map(VmInstallMetaData::adapt).flatMap(Optional::stream);
	}
	
	public static VmInstallMetaData register(Path location) throws CoreException {
		IVMInstall install = JDTUtils.registerVM(location.toFile());
		OSArchitecture arch = JDTUtils.detect(install);
		return new VmInstallMetaData(install, arch);
	}
}