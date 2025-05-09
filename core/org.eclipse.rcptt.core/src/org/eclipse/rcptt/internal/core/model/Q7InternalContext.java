/*******************************************************************************
 * Copyright (c) 2009, 2020 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.core.model;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.core.scenario.NamedElement;

public class Q7InternalContext extends Q7Context {

	private final Context ctx;

	public Q7InternalContext(Q7Element parent, String name, Context ctx)
			throws IllegalArgumentException {
		super(parent, name);
		this.ctx = ctx;
	}

	@Override
	public IPath getPath() {
		return new Path("");
	}

	@Override
	public IQ7Project getQ7Project() {
		return ModelManager.getModelManager().getModel()
				.getProject("__internal__project___");
	}

	@Override
	public IFile getResource() {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public NamedElement getNamedElement() throws ModelException {
		return ctx;
	}

	@Override
	public NamedElement getModifiedNamedElement() throws ModelException {
		return ctx;
	}

	@Override
	public String getElementName() throws ModelException {
		return ctx.getName();
	}

	@Override
	public String getDescription() throws ModelException {
		return ctx.getDescription();
	}

	@Override
	public String getVersion() throws ModelException {
		return ctx.getVersion();
	}

	@Override
	public NamedElement getMeta() throws ModelException {
		return ctx;
	}

	@Override
	public String getTags() throws ModelException {
		return ctx.getTags();
	}

	@Override
	public String getID() throws ModelException {
		return ctx.getId();
	}

	@Override
	public ContextType getType() throws ModelException {
		return ContextTypeManager.getInstance().getTypeByContext(ctx);
	}

	@Override
	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm,
			IResource underlyingResource) throws ModelException {
		return true;
	}
	
	@Override
	public boolean isBuiltin() {
		return true;
	}
}
