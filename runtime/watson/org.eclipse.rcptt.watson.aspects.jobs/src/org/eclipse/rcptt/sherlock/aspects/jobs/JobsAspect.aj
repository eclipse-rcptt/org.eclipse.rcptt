package org.eclipse.rcptt.sherlock.aspects.jobs;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.eclipse.core.internal.jobs.InternalJob;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.rcptt.sherlock.aspects.jobs.internal.IJobsEventListener;
import org.eclipse.rcptt.sherlock.aspects.jobs.internal.JobsActivator;
import org.eclipse.rcptt.sherlock.aspects.jobs.internal.JobsEventManager;

@SuppressWarnings("all")
public aspect JobsAspect {
	public JobsAspect() {
	}

	/**
	 * Used to determine finished ui job's.
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.JobManager mgr,
			org.eclipse.core.internal.jobs.InternalJob job, IStatus status,
			boolean notify):
		execution(void org.eclipse.core.internal.jobs.JobManager.endJob(org.eclipse.core.internal.jobs.InternalJob, IStatus, boolean)) 
		&& target(mgr) && args(job, status, notify) {
		try {
			IJobsEventListener[] listeners = JobsEventManager.getDefault()
					.getListeners();
			for (IJobsEventListener l : listeners) {
				l.endJob(job, status, notify);
			}
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}

	/**
	 * Used to determine finished ui job's.
	 * Added for compatibility with Eclipse 4.26 changes to JobManager::endJob signature.
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.JobManager mgr,
			org.eclipse.core.internal.jobs.InternalJob job, IStatus status,
			boolean notify):
		execution(void org.eclipse.core.internal.jobs.JobManager.endJob(org.eclipse.core.internal.jobs.InternalJob, IStatus, boolean, boolean))
		&& target(mgr) && args(job, status, notify) {
		try {
			IJobsEventListener[] listeners = JobsEventManager.getDefault()
					.getListeners();
			for (IJobsEventListener l : listeners) {
				l.endJob(job, status, notify);
			}
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}

	// Profiling
	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.InternalJob job, long delay,
			boolean reshedule):
				execution(void org.eclipse.core.internal.jobs.JobManager.schedule(InternalJob, long, boolean))
				&& args(job, delay, reshedule){
		try {
			IJobsEventListener[] listeners = JobsEventManager.getDefault()
					.getListeners();
			for (IJobsEventListener l : listeners) {
				l.jobSchedule(job, delay, reshedule);
			}
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}

	/*
	 * Profiling.
	 * Added for compatibility with Eclipse 4.26 changes to JobManager::schedule signature.
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.InternalJob job, long delay):
				execution(void org.eclipse.core.internal.jobs.JobManager.schedule(InternalJob, long))
				&& args(job, delay){
		try {
			IJobsEventListener[] listeners = JobsEventManager.getDefault()
					.getListeners();
			for (IJobsEventListener l : listeners) {
				l.jobSchedule(job, delay, false);
			}
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.InternalJob job, int newState):
		execution(void org.eclipse.core.internal.jobs.JobManager.changeState(InternalJob, int)) && args(job, newState) {
		try {
			IJobsEventListener[] listeners = JobsEventManager.getDefault()
					.getListeners();
			for (IJobsEventListener l : listeners) {
				l.jobChangeStatus(job, newState);
			}
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.InternalJob job):
		execution(void org.eclipse.core.internal.jobs.JobManager.cancel(InternalJob)) && args(job) {
		try {
			IJobsEventListener[] listeners = JobsEventManager.getDefault()
					.getListeners();
			for (IJobsEventListener l : listeners) {
				l.jobCanceled(job);
			}
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}
}
