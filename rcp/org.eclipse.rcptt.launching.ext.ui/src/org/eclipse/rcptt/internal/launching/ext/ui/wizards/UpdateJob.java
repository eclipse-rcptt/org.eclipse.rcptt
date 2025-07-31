/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.launching.ext.ui.wizards;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class UpdateJob<T> extends Job {
	public UpdateJob(String name) {
		super(name);
	}
	
	public final void update(T newInput) {
		T oldValue = input.getAndSet(newInput);
		if (!Objects.equals(oldValue, newInput)) {
			cancel();
			schedule();
		}
	}

	@Override
	protected final IStatus run(IProgressMonitor monitor) {
		run(input.get(), monitor);
		return Status.OK_STATUS;
	}
	
	protected abstract void run(T input, IProgressMonitor monitor);

	private final AtomicReference<T> input = new AtomicReference<>(); 
}
