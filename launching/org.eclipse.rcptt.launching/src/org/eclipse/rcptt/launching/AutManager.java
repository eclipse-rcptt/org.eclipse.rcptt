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

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.rcptt.internal.launching.aut.BaseAutManager;

/**
 * This interface provide access to AUT and AUT launches
 * 
 * @author Yuri Strot
 * @see Aut
 * @see AutLaunch
 */
public interface AutManager {

	/**
	 * Shared thread-safe instance of the {@link AutManager}
	 */
	static final AutManager INSTANCE = BaseAutManager.INSTANCE;

	/**
	 * Return list of all registered AUTs. AUT will be created automatically
	 * when corresponding {@link ILaunchConfiguration} will be created.
	 * 
	 * @return list of AUTs
	 */
	List<Aut> getAuts();

	/**
	 * Return list of all active AUT launches.
	 * 
	 * @return list of active launches
	 */
	List<AutLaunch> getLaunches();

	/**
	 * Return AUT by name. Each AUT has unique name.
	 * 
	 * @param name
	 *            AUT name
	 * @return corresponding AUT
	 */
	Aut getByName(String name);

	/**
	 * Return AUT launch by id. Each launch has unique automatically generated
	 * identifier.
	 * 
	 * @param id
	 *            AUT identifier
	 * @return corresponding AUT launch
	 */
	AutLaunch getById(String id);

	/**
	 * Return AUT by corresponding launch configuration.
	 * 
	 * @param config
	 *            launch configuration
	 * @return corresponding AUT
	 */
	Aut getByLaunch(ILaunchConfiguration config);

	/**
	 * Return AUT launch by {@link ILaunch}
	 * 
	 * @param launch
	 * @return AUT launch
	 */
	AutLaunch getByLaunch(ILaunch launch);

	/**
	 * Add AUT listener
	 * 
	 * @param listener
	 */
	void addListener(AutListener listener);

	/**
	 * Remove AUT listener
	 * 
	 * @param listener
	 */
	void removeListener(AutListener listener);

	List<AutLaunch> storeAUTLaunches(ILaunchConfiguration configuration);

	void updateAUTLaunches(ILaunchConfiguration newConfig,
			List<AutLaunch> launches);
}
