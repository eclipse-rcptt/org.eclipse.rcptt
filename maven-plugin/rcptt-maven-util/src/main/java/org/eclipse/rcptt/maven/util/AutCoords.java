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
package org.eclipse.rcptt.maven.util;

import org.eclipse.rcptt.maven.util.Coords;

/**
 * AUT specific data.
 */
public class AutCoords extends Coords {
	private Injection[] injections;
	private boolean ignoreOtherInjections;
	private String[] args;
	private String vm;
	private boolean reuseExistingWorkspace = false;
	private Rap rap;
	private boolean enableSoftwareInstallation = false;

	public AutCoords() {
		super();
	}

	public Injection[] getInjections() {
		return injections;
	}

	public void setInjections(Injection[] injections) {
		this.injections = injections;
	}

	public boolean isIgnoreOtherInjections() {
		return ignoreOtherInjections;
	}

	public void setIgnoreOtherInjections(boolean ignoreOtherInjections) {
		this.ignoreOtherInjections = ignoreOtherInjections;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	public String getVm() {
		return vm;
	}

	public void setVm(String vm) {
		this.vm = vm;
	}

	public boolean reuseExistingWorkspace() {
		return reuseExistingWorkspace;
	}

	public void setReuseExistingWorkspace(boolean reuseExistingWorkspace) {
		this.reuseExistingWorkspace = reuseExistingWorkspace;
	}

	public Rap getRap() {
		return rap;
	}

	public void setRap(Rap rap) {
		this.rap = rap;
	}

	public boolean enableSoftwareInstallation()
	{
		return enableSoftwareInstallation;
	}

	public void setEnableSoftwareInstallation(boolean enableSoftwareInstallation)
	{
		this.enableSoftwareInstallation = enableSoftwareInstallation;
	}

}
