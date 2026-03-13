/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.report.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rcptt.reporting.util.ReportEntry;
import org.eclipse.rcptt.ui.WidgetUtils;

class ReportEntryContentProvider implements IStructuredContentProvider {
	@Override
	public void dispose() {
		updateJob.cancel();
	}

	private List<ReportEntry> entries = Collections.emptyList();
	private Viewer viewer = null;
	private Iterable<ReportEntry> reports = null;
	private final Job updateJob = new Job("Reading report list") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final List<ReportEntry> entries = new ArrayList<ReportEntry>();
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			synchronized (reports) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				Iterable<ReportEntry> reportsLocal = reports;
				if (reportsLocal != null) {
					Iterator<ReportEntry> iterator = reportsLocal.iterator();
					try {
						while (iterator.hasNext()) {
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;
							ReportEntry next = iterator.next();
							if (next == null) {
								break;
							}
							monitor.subTask(next.name);
							{
								entries.add(next);
							}
						}
					} catch (Exception e) {
						if (monitor.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
						throw e;
					}
				}
			}
			ReportEntryContentProvider.this.entries = entries;
			WidgetUtils.asyncExec(viewer.getControl(), () -> {
				viewer.refresh();
			});
			return Status.OK_STATUS;
		}

	};

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		updateJob.cancel();
		if (newInput instanceof Iterable r) {
			reports = r;
			updateJob.schedule();
		} else {
			entries.clear();
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return entries.toArray();
	}

}