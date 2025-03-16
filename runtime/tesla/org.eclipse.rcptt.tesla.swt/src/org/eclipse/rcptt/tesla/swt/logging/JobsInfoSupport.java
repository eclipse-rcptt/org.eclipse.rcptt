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
package org.eclipse.rcptt.tesla.swt.logging;

import org.eclipse.core.internal.jobs.InternalJob;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.sherlock.aspects.jobs.internal.IJobsEventListener;
import org.eclipse.rcptt.sherlock.core.reporting.IReportBuilder;
import org.eclipse.rcptt.tesla.internal.ui.player.UIJobCollector;
import org.eclipse.rcptt.tesla.ui.IJobCollector.JobStatus;

@SuppressWarnings("restriction")
public class JobsInfoSupport implements IJobsEventListener,
		IJobChangeListener {
	static {
		ReportHelper.class.getName(); // Prevents class loading in job listeners, which may cause deadlocks.
	}

	private JobsInfoProvider provider;

	public JobsInfoSupport(JobsInfoProvider jobsEventProvider) {
		this.provider = jobsEventProvider;
	}

	@Override
	public void jobSchedule(InternalJob job, long delay, boolean reshedule) {
		jobStart(job);
	}

	@Override
	public void jobChangeStatus(InternalJob job, int newState) {
		jobUpdate(job);
	}

	@Override
	public void endJob(InternalJob job, IStatus status, boolean notify) {
		jobFinish(job);
	}

	@Override
	public void jobCanceled(InternalJob job) {
		jobFinish(job);
	}

	@Override
	public void aboutToRun(IJobChangeEvent event) {
	}

	@Override
	public void awake(IJobChangeEvent event) {
	}

	@Override
	public void done(IJobChangeEvent event) {
	}

	@Override
	public void running(IJobChangeEvent event) {
	}

	@Override
	public void scheduled(IJobChangeEvent event) {
	}

	@Override
	public void sleeping(IJobChangeEvent event) {
	}

	public void register() {
		Job.getJobManager().addJobChangeListener(this);
	}

	public void unregister() {
		Job.getJobManager().removeJobChangeListener(this);
	}

	private void jobStart(final InternalJob job) {
		JobStatus status = UIJobCollector.detectJobStatus((Job) job);
		if (JobStatus.IGNORED.equals(status)) {
			return;
		}
		IReportBuilder[] builders = provider.getListeners();
		for (IReportBuilder builder : builders) {
			ReportHelper.startWaitInfo(builder.getCurrent(), "job", job.getClass().getName());
		}
	}

	private void jobFinish(final InternalJob job) {
		JobStatus status = UIJobCollector.detectJobStatus((Job) job);
		if (JobStatus.IGNORED.equals(status)) {
			return;
		}
		IReportBuilder[] builders = provider.getListeners();
		for (IReportBuilder builder : builders) {
			ReportHelper.finishWaitInfo(builder.getCurrent(), "job", job.getClass().getName());
		}
	}

	private void jobUpdate(final InternalJob job) {
		JobStatus status = UIJobCollector.detectJobStatus((Job) job);
		if (JobStatus.IGNORED.equals(status)) {
			return;
		}
		IReportBuilder[] builders = provider.getListeners();
		for (IReportBuilder builder : builders) {
			ReportHelper.updateWaitInfo(builder.getCurrent(), "job", job.getClass().getName());
		}
	}

	public void clear() {

	}
}
