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
package org.eclipse.rcptt.core;

import java.util.function.BooleanSupplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.rcptt.core.scenario.Context;

/**
 * Context processor
 */
public interface IContextProcessor {

	public boolean isApplied(Context context);

	/**
	 * Apply context
	 * 
	 * @param context
	 * @param isCancelled TODO
	 * @return
	 * @throws CoreException
	 */
	public void apply(Context context, BooleanSupplier isCancelled) throws CoreException;

	/**
	 * Create context from the current environment
	 * 
	 * @return
	 * @throws CoreException
	 */
	public Context create(EObject param) throws CoreException;

}
