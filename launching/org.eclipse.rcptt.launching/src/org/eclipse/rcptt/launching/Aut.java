/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * This interface represents application under test (AUT). Each AUT associated
 * with {@link ILaunchConfiguration} and can be identified by this configuration
 * and unique launch configuration name.<br>
 * <br>
 * This interface allows to start and manage AUT, see
 * {@link #launch(IProgressMonitor)}. Usually AUT should have only one active
 * launch. However it's possible to start several applications based on the same
 * AUT configuration.
 * 
 * @author Yuri Strot
 * @see AutManager
 * @see AutLaunch
 */
public interface Aut {

	/**
	 * Shortcut for {@code getCofig().getName() }
	 * 
	 * @return launch configuration name
	 */
	String getName();

	/**
	 * Return launch configuration associated with this AUT
	 * 
	 * @return launch configuration
	 */
	ILaunchConfiguration getConfig();

	/**
	 * Launch application under test and returns corresponding launch
	 * 
	 * @param monitor
	 *            progress monitor
	 * @return AUT launch or null, if launch was cancelled
	 * @throws CoreException
	 *             when AUT couldn't be launched
	 */
	AutLaunch launch(IProgressMonitor monitor) throws CoreException;

	/**
	 * Return active launch or null if AUT is not launched. If there are several
	 * active launches this method returns the last started.
	 * 
	 * @return active launch
	 */
	AutLaunch getActiveLaunch();

	/**
	 * Return list of all active launches. This list always sorted by launch
	 * time.
	 * 
	 * @return list of active launches
	 */
	List<AutLaunch> getLaunches();

	void delete();

}
