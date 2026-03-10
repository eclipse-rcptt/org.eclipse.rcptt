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
package org.eclipse.rcptt.ui.report.internal;

import java.io.UncheckedIOException;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.reporting.Q7Statistics;
import org.eclipse.rcptt.reporting.ReportingFactory;
import org.eclipse.rcptt.reporting.util.ReportEntry;
import org.eclipse.rcptt.reporting.util.ReportUtils;
import org.eclipse.rcptt.ui.controls.AbstractEmbeddedComposite;
import org.eclipse.rcptt.ui.launching.StatisticPanel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class StatisticsComposite extends AbstractEmbeddedComposite {

	private Composite control;
	private StatisticPanel panel;
	private Q7Statistics statistics = ReportingFactory.eINSTANCE.createQ7Statistics();

	Iterable<ReportEntry> iterator = Collections.<ReportEntry>emptyList();

	private final Job updateJob = new Job("Calculating report statistics") {

		@Override
		public boolean belongsTo(Object family) {
			return family == StatisticsComposite.this;
		}


		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final Q7Statistics statistics;
			try {
				synchronized (iterator) {
					statistics = ReportUtils.calculateStatistics(iterator.iterator());
				}
			} catch (UncheckedIOException | NullPointerException e) { // indicates external iterruption
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				throw e;
			}
			updateStatistics(statistics);
			return Status.OK_STATUS;
		}
	};

	private void updateStatistics(Q7Statistics statistics) {
		this.statistics = statistics;
		refresh();

	}

	@Override
	protected void fillActions(IToolBarManager manager) {
	}

	public StatisticsComposite() {
	}
	
	@Override
	public void dispose() {
		updateJob.cancel();
		super.dispose();
	}

	void setReports(Iterable<ReportEntry> iterator) {
		updateJob.cancel();
		this.iterator = iterator;
		if (iterator != null)
			updateJob.schedule();
	}

	@Override
	public String getName() {
		return "Execution statistics";
	}

	@Override
	public Image getImage() {
		return Images.getImage(Images.FILE);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		control = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false)
				.applyTo(control);
		panel = new StatisticPanel(control);
		refresh();
	}

	private synchronized void refresh() {
		if (panel == null)
			return;
		if (panel.isDisposed())
			return;
		Q7Statistics tmp2 = statistics;
		if (tmp2 == null) {
			tmp2 = ReportingFactory.eINSTANCE.createQ7Statistics();
		}
		final Q7Statistics tmp = tmp2;
		panel.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (panel.isDisposed())
					return;
				panel.update(tmp.getTotal(), tmp.getPassed()
						+ tmp.getFailed() + tmp.getSkipped(),
						tmp.getFailed(), tmp.getSkipped(),
						tmp.getTime());
			}

		});
	}

	@Override
	public Control getControl() {
		return control;
	}

}
