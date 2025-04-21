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
package org.eclipse.rcptt.internal.launching.aut;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.ILaunchExecutor;

public class BaseAut implements Aut {

	public static final int TERMINATE_CODE = 7788;
	private static final ILog LOG = Platform.getLog(BaseAut.class);

	public BaseAut(ILaunchConfiguration config, ILaunchExecutor executor) {
		this.config = config;
		this.executor = executor;
	}

	public ILaunchConfiguration getConfig() {
		return config;
	}

	public String getName() {
		return config.getName();
	}

	public List<AutLaunch> getLaunches() {
		return BaseAutManager.INSTANCE.getLaunches(this);
	}

	public AutLaunch launch(IProgressMonitor monitor) throws CoreException {
		ILaunch launch = executor.launch(ILaunchManager.RUN_MODE, config,
				monitor);
		return BaseAutManager.INSTANCE.getByLaunch(launch);
	}

	public BaseAutLaunch getActiveLaunch() {
		return BaseAutManager.INSTANCE.getCurrentLaunch(this);
	}

	public ILaunchExecutor getExecutor() {
		return executor;
	}

	private final ILaunchConfiguration config;
	private final ILaunchExecutor executor;

	@Override
	public void delete() {
		ILaunchConfiguration config2 = getConfig();
		try {
			LaunchInfoCache.remove(config2);
			BaseAutManager.INSTANCE.launchConfigurationRemoved(config2);			
			config2.delete();
		} catch (CoreException e) {
			LOG.log(e.getStatus());
		}
	}

}
