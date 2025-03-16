package org.eclipse.rcptt.tesla.jobs.aspects;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.eclipse.core.internal.jobs.InternalJob;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.rcptt.tesla.core.am.AspectManager;
import org.eclipse.rcptt.tesla.jobs.JobsActivator;
import org.eclipse.rcptt.tesla.jobs.JobsManager;
import org.eclipse.rcptt.tesla.jobs.ProfilingJobsEventManager;

@SuppressWarnings("all")
public aspect JobsAspect {
	public JobsAspect() {
		AspectManager.activateAspect(JobsActivator.PLUGIN_ID, this.getClass()
				.getName());
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
			if (job instanceof Job) {
				JobsManager.getInstance().notifyJobDone((Job) job, status,
						notify);
			}
			ProfilingJobsEventManager.getDefault().endJob(job,status, notify);
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}

	/**
	 *  Used to determine finished ui job's.
	 *  Added for compatibility with Eclipse 4.26 changes to JobManager::endJob signature.
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.JobManager mgr,
			org.eclipse.core.internal.jobs.InternalJob job, IStatus status,
			boolean notify, boolean worker):
		execution(void org.eclipse.core.internal.jobs.JobManager.endJob(org.eclipse.core.internal.jobs.InternalJob, IStatus, boolean, boolean))
		&& target(mgr) && args(job, status, notify, worker) {
		try {
			if (job instanceof Job) {
				JobsManager.getInstance().notifyJobDone((Job) job, status,
						notify);
			}
			ProfilingJobsEventManager.getDefault().endJob(job,status, notify);
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(org.eclipse.core.internal.jobs.InternalJob job, long time):
		execution(void org.eclipse.core.internal.jobs.InternalJob.setStartTime(long)) 
		&& target(job) && args(time) {
		try {
			if (job instanceof Job) {
				JobsManager.getInstance().putJobTime(job, time);

				long newTime = JobsManager.getInstance().calculateNewTime(job,
						time);
				if (newTime >= 0) {
					return proceed(job, newTime);
				}
			}
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
		return proceed(job, time);
	}

	// Profiling
	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.InternalJob job, long delay,
			boolean reshedule):
				execution(void org.eclipse.core.internal.jobs.JobManager.schedule(InternalJob, long, boolean))
				&& args(job, delay, reshedule){
		try {
			ProfilingJobsEventManager.getDefault().jobSchedule(job, delay,
					reshedule);
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
			ProfilingJobsEventManager.getDefault().jobSchedule(job, delay,
					false);
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}


	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.InternalJob job, int newState):
		execution(void org.eclipse.core.internal.jobs.JobManager.changeState(InternalJob, int)) && args(job, newState) {
		try {
			ProfilingJobsEventManager.getDefault()
					.jobChangeState(job, newState);
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	before(org.eclipse.core.internal.jobs.InternalJob job):
		execution(void org.eclipse.core.internal.jobs.JobManager.cancel(InternalJob)) && args(job) {
		try {
			ProfilingJobsEventManager.getDefault().jobCanceled(job);
		} catch (Throwable e) {
			JobsActivator.log(e);
		}
	}
}
