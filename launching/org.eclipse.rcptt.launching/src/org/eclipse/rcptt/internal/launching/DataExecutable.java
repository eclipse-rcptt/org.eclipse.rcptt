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
package org.eclipse.rcptt.internal.launching;

import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rcptt.core.ecl.core.model.ExecutionPhase;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.launching.AutLaunch;

import com.google.common.base.Preconditions;

public abstract class DataExecutable extends Executable {

	protected final IQ7NamedElement element;

	protected AutLaunch launch;
	protected boolean terminateUser = false;
	protected NullProgressMonitor executionMonitor;

	public DataExecutable(AutLaunch launch, IQ7NamedElement element,
			boolean debug) {
		this(launch, element, debug, ExecutionPhase.AUTO);
	}

	public DataExecutable(AutLaunch launch, IQ7NamedElement element,
			boolean debug, ExecutionPhase phase) {
		super(debug, phase, false);
		this.element = Objects.requireNonNull(element);
		try {
			Preconditions.checkNotNull(element.getID());
		} catch (ModelException e) {
			throw new IllegalArgumentException(e);
		}
		this.launch = launch;
		executionMonitor = new NullProgressMonitor();
		addListener(new Listener() {

			@Override
			public void onStatusChange(Executable executable) {
				if (getStatus() == State.COMPLETED) {
					executionMonitor.setCanceled(true);
				}
			}

			@Override
			public void updateSessionCounters(Executable executable, IStatus status) {
			}
			
		});
	}

	public AutLaunch getAut() {
		return launch;
	}

	public String getName() {
		String nameByDocument = Q7SearchCore.findNameByDocument(element);
		if (nameByDocument != null) {
			return nameByDocument;
		}

		try {
			return element.getElementName();
		} catch (ModelException e) {
			if (!element.getResource().exists()) {
				return "Resource: "
						+ element.getResource().getFullPath()
						+ " doesn't exist.";
			}
			// Q7Plugin.log(e);
			return "Unresolved element";
		}
	}

	@Override
	public String getId() {
		try {
			String id = Q7SearchCore.findIDByDocument(element);
			if (id == null) {
				id = element.getID();
			}
			return id;
		} catch (ModelException e) {
			Q7LaunchingPlugin.log(e);
		}
		return null;
	}

	@Override
	public Executable[] getChildren() {
		return EMPTY;
	}

	
	@Override
	public IStatus execute() throws InterruptedException {
		try {
			return doExecute();
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	public boolean isTerminateUser() {
		return terminateUser;
	}

	protected abstract IStatus doExecute() throws CoreException,
			InterruptedException;

	public IQ7NamedElement getActualElement() {
		return element;
	}
}
