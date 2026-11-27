/*******************************************************************************
 * Copyright (c) 2009, 2014 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching.internal.target;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.rcptt.launching.p2utils.P2Utils;

@SuppressWarnings("restriction")
public class PDEHelper {
	/**
	 * Return PDE target platform sercice.
	 * 
	 * @return
	 */
	public static ITargetPlatformService getTargetService() {
		return P2Utils.getTargetService();
	}

	/**
	 * Return P2 metadata repository manager.
	 * 
	 * @return
	 * @throws CoreException
	 */
	public static IMetadataRepositoryManager getRepositoryManager() throws CoreException {
		return P2Utils.getRepositoryManager();
	}

	public static IArtifactRepositoryManager getArtifactRepositoryManager() throws CoreException {
		return P2Utils.getArtifactRepositoryManager();
	}

	public static IMetadataRepository safeLoadRepository(URI uri, IProgressMonitor monitor) throws CoreException {
		return P2Utils.safeLoadRepository(uri, monitor, P2Utils.getProvisioningAgent());
	}

	public static IArtifactRepository safeLoadArtifactRepository(URI uri, IProgressMonitor monitor) throws CoreException {
		return P2Utils.safeLoadArtifactRepository(uri, monitor, P2Utils.getProvisioningAgent());
	}
}
