/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching.target;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.launching.ext.OriginalOrderProperties;
import org.eclipse.rcptt.launching.injection.InjectionConfiguration;
import org.eclipse.rcptt.launching.internal.target.Q7Target;

/**
 * Class designed to access target platform features to configure it via easy
 * way.
 */
public interface ITargetPlatformHelper {
	Q7Target getQ7Target();

	String getUserArea();

	/**
	 * Return if target is already resolved.
	 * 
	 * @return
	 */
	boolean isResolved();

	/**
	 * Set target platform name.
	 * 
	 * @param string
	 */
	void setTargetName(String name);

	/**
	 * Save current target platform to PDE.
	 * @throws CoreException 
	 */
	void save() throws CoreException;

	/**
	 * Return target platform name.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Return target platform profile path.
	 * 
	 * @return
	 */
	String getTargetPlatformProfilePath();

	/**
	 * Remove target platform from PDE.
	 */
	void delete();

	/**
	 * Return list of available applications to target platform. If platform is
	 * not resolved, will return empty list.
	 * 
	 * @return
	 */
	String[] getApplications();

	/**
	 * Return list of available products to target platform. If platform is not
	 * resolved, will return empty list.
	 * 
	 * @return
	 */
	String[] getProducts();

	/**
	 * Return currently set default target platform application. Will read
	 * configuration file for this purpose.
	 * 
	 * @return
	 */
	String getDefaultApplication();

	/**
	 * Return currently set default target platform product. Will read
	 * configuration file for this purpose.
	 * 
	 * @return
	 */
	String getDefaultProduct();

	/**
	 * Will modify target platform with specified injection configuration.
	 * 
	 * Will remove previously added configuration injection.
	 * 
	 * @param configuration
	 * @return
	 */
	IStatus applyInjection(InjectionConfiguration configuration,
			IProgressMonitor monitor);

	/**
	 * Get initial environment variables from eclipse.ini of target platform.
	 */
	Map<String, String> getIniEnvironment();

	/**
	 * Get initial vmargs readed from eclipse.ini of target platform.
	 */
	String getIniVMArgs();

	/** java.home configured in AUT's INI file **/
	Optional<Path> getJavaHome();

	String getTemplateConfigLocation();

	String getBundlesList();

	OriginalOrderProperties getConfigIniProperties();

	OSArchitecture detectArchitecture(boolean preferCurrentVmArchitecture,
			StringBuilder detectMsg);

	String getRuntimeVersion();

	IStatus resolve(IProgressMonitor monitor);

	String getEquinoxStartupPath(String bundleEquinoxLauncher);

	public abstract IPluginModelBase getWeavingHook();

	IStatus getStatus();

	public ITargetDefinition getTarget();

	Map<String, Version> getVersions() throws CoreException;
}
