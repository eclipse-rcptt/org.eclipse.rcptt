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

import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.rcptt.internal.launching.ext.JDTUtils;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;

public final class VmInstallMetaData {
	public final IVMInstall install;
	public final OSArchitecture arch;
	public VmInstallMetaData(IVMInstall install, OSArchitecture arch) {
		super();
		this.install = Objects.requireNonNull(install);
		this.arch = Objects.requireNonNull(arch);
	}
	
	public static Stream<VmInstallMetaData> all() {
		return JDTUtils.installedVms();
	}
}