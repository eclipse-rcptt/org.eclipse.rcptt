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
package org.eclipse.rcptt.ui.launching.aut;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.rcptt.internal.launching.aut.Q7LaunchValidatorManager;
import org.eclipse.rcptt.launching.Aut;

public class AutElement {

	private final Aut aut;

	public AutElement(Aut aut) {
		this.aut = aut;
	}

	public Aut getAut() {
		return aut;
	}

	public String getName() {
		return aut.getName();
	}

	/**
	 * Check if element is launched.
	 */
	boolean isLaunched() {
		return false;
	}

	/**
	 * Check if element could be launched.
	 */
	boolean couldBeLaunched() {
		// TODO: Some configuration could not be launched.
		return true;
	}

	/**
	 * Return true if element could be stopped.
	 */
	boolean couldBeStopped() {
		return false;
	}

	public boolean isValid() {
		return Q7LaunchValidatorManager.isValid(aut.getConfig());
	}

	public void remove() throws CoreException {
		aut.delete();
	}

	public void stop() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aut == null) ? 0 : aut.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AutElement other = (AutElement) obj;
		if (aut == null) {
			if (other.aut != null)
				return false;
		} else if (!aut.equals(other.aut))
			return false;
		return true;
	}

}
