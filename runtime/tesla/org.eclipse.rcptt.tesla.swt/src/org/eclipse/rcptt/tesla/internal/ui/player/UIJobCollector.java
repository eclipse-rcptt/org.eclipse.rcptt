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
package org.eclipse.rcptt.tesla.internal.ui.player;

import static java.util.Arrays.asList;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.logging.Q7LoggingManager;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.reporting.Procedure1;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.tesla.core.context.ContextManagement;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.jobs.JobsManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;
import org.eclipse.rcptt.tesla.ui.IJobCollector;
import org.eclipse.rcptt.tesla.ui.IJobCollector.JobStatus;
import org.eclipse.rcptt.tesla.ui.SWTTeslaActivator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Manages jobs information and statuses.
 */
public class UIJobCollector implements IJobChangeListener {
	private static final boolean DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.rcptt.tesla.swt/debug/jobCollector"));
	private static final boolean DEBUG_REPORT_OUTPUT = "true".equals(Platform.getDebugOption("org.eclipse.rcptt.tesla.swt/debug/debugReportOutput"));
	private static final PrintWriter DEBUG_WRITER = new PrintWriter(System.out);
	private static final Object FAMILY = new Object();
	
	/** 
	 * Timeouts and intervals for active job detection
	 * 
	 * All values are in milliseconds  
	 * */
	public interface IParameters {
		int stepModeStartDelay();
		int stepInterval();
		int stepModeTimeout();
		int timeout();
		/** If a job is scheduled with a longer delay, ignore it immediately **/
		int delayToWaitFor(); 
	}
	
	private final class NormalizedParameters implements IParameters {
		private final IParameters master;

		public NormalizedParameters(IParameters master) {
			if (master == null)
				throw new NullPointerException();
			this.master = master;
		}

		@Override
		public int stepModeStartDelay() {
			return Math.max(Math.max(master.stepInterval(), master.stepModeStartDelay()), 1);
		}

		@Override
		public int stepInterval() {
			return Math.min(master.stepInterval(), stepModeStartDelay());
		}

		@Override
		public int stepModeTimeout() {
			return Math.max(master.stepModeTimeout(), master.timeout());
		}

		@Override
		public int timeout() {
			return Math.max(Math.min(master.timeout(), master.stepModeTimeout()), 1);
		}

		@Override
		public int delayToWaitFor() {
			return Math.min(master.delayToWaitFor(), timeout());
		}
	}

	private class JobInfo {
		private final Job job;
		private JobStatus status;
		/** If we do not know when job will wake, then ignore it */
		private long startingTime = System.currentTimeMillis() - 1;
		private long blockedTime = 0;
		private long rescheduleCounter = 0;
		private long lastStep = 0; 
		private boolean jobInStepMode = false;
//		private boolean checkForTimeout = true;
		private boolean blocked = false;
		private long runningTime = System.currentTimeMillis();
		private Thread lastThread = null;
		JobInfo(Job job) {
			this.job = job;
			this.status = calcJobStatus(job);
		}

		synchronized void awake() {
			startingTime = System.currentTimeMillis();
			runningTime = System.currentTimeMillis();
			status = calcJobStatus(job);
			debug(this + " is awake with " + status);
		}

		synchronized void sleeping() {
		}

		synchronized void done(boolean reschedule) {
			// Job's thread field is already null, but there is a good chance this listener is running in it's thread
			// This is needed to handle pre-2022 AUTs, where listeners were not serialized
			lastThread = Thread.currentThread();
			if (reschedule) {
				// Job will be rescheduled
				rescheduleCounter += 1;
			}
		}

		synchronized boolean isActive() {
			long delay = startingTime - System.currentTimeMillis();
			switch (status) {
			case IGNORED:
				return false;
			case UNKNOWN:
				if (Job.SLEEPING == job.getState()) {
					return delay >= 0 && delay <= parameters.delayToWaitFor(); 
				}
				return true;
			case REQUIRED:
				return true;
			default:
				return false;
			}
		}
		
		synchronized void scheduled(long delay) {
			status = calcJobStatus(job);
			startingTime = System.currentTimeMillis() + delay;
		}

		@Override
		public String toString() {
			String state = "NONE";
			switch (job.getState()) {
			case Job.RUNNING: state = "RUNNING"; break;
			case Job.SLEEPING: state = "SLEEPING"; break;
			case Job.WAITING: state = "WAITING"; break;
			default: state = "NONE"; break;
			}
			return String.format("%s (%s), %s, status: %8s, is active: %b, delay: %d, blocked for %d, running for %d, thread: %s",
					job.getClass().getName(), job.getName(), state, status, isActive(), startingTime - System.currentTimeMillis(),
					blocked ? System.currentTimeMillis() - blockedTime : 0,  blocked ? 0 : System.currentTimeMillis() - runningTime, lastThread);
		}

		public void blocked(boolean isBlocked) {
			if (blocked == isBlocked)
				return;
			blocked = isBlocked;
			debug(job.getName() + " is " + (isBlocked ? "blocked" : "unblocked"));
			if (!isBlocked) {
				runningTime = System.currentTimeMillis();
			} else {
				blockedTime = System.currentTimeMillis();
			}
			
		}
		
		public boolean isRunningFor(long time) {
			return !blocked &&  runningTime + time < System.currentTimeMillis();  
		}
		
		public boolean isActiveFor(long time) {
			return startingTime + time < System.currentTimeMillis();
		}
		
		public boolean isBlockedFor(long time) {
			return blocked &&  blockedTime + time < System.currentTimeMillis();
		}
		
		public boolean canStep(long timeSinceLastStep) {
			if (System.currentTimeMillis() > this.lastStep + timeSinceLastStep) {
				this.lastStep = System.currentTimeMillis();
				return true;
			}
			return false;
		}

		public void poke() {
			synchronized (this) {
				if (!isActive()) {
					return;
				}
				if (rescheduleCounter >= TeslaLimits.getJobNullifyRescheduleMaxValue()) {
					return;
				}
			}
			debug("Nullified: " + this);
			JobsManager.getInstance().nulifyTime(job);
		}

		public synchronized Thread getLastThread() {
			return lastThread;
		}

	}

	private final Map<Job, JobInfo> jobs = Collections.synchronizedMap(new IdentityHashMap<Job, JobInfo>());
	private boolean state;
	private boolean needDisable = false;
	private final IParameters parameters;
	

	private final Job removeCompletedJob = new Job("Eliminate completed jobs") {
		{
			setPriority(Job.INTERACTIVE);
			setSystem(true);
		}
		
		@Override
		protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
			while (!monitor.isCanceled()) {
				List<Job> doneJobs;
				synchronized (jobs) {
					doneJobs = jobs.keySet().stream().filter(j -> j.getState() == Job.NONE).collect(Collectors.toList());
				}
				boolean found = false;
				for (Job job: doneJobs) {
					if (job.getState() == Job.NONE ) {
						JobInfo info = jobs.get(job);
						if (info != null) {
							try {
								// Job can be DONE even when it is already rescheduled, see https://github.com/eclipse-platform/eclipse.platform/issues/2210
								// First join may return false positive when previous execution completes, but current one is still pending
								if (!job.join(1, monitor) || !job.join(1, monitor)) { 
									continue;
								}
								TeslaSWTAccess.waitListeners(job);
								
								// TeslaSWTAccess.waitListeners() is not enough in 2022-09 and older https://github.com/eclipse-rcptt/org.eclipse.rcptt/issues/65
								Thread lastThread = info.getLastThread();
								if (lastThread != null) {
									Context context = ContextManagement.makeContext(lastThread.getStackTrace());
									if (context.containsClass("org.eclipse.core.internal.jobs.JobListeners")) {
										continue;
									}
								}

								// Listeners may change the state, also the job may be rescheduled and synchronizing on listener queue helps to detect that
								if (!job.join(1, monitor) || !job.join(1, monitor)) {
									continue;
								}
							} catch (OperationCanceledException | InterruptedException e) {
								return Status.OK_STATUS;
							}
							jobs.remove(job, info);
							event("gone", info);
							found = true;
						}
					}
				}
				if (!found) {
					break;
				}
				
			}
			return Status.OK_STATUS;
		};
		@Override
		public boolean belongsTo(Object family) {
			return family == FAMILY;
		};
	};
	
	private JobInfo getOrCreateJobInfo(Job job) {
		if (job.belongsTo(FAMILY)) {
			throw new AssertionError("Can't work with an internal job");
		}
		synchronized (jobs) {
			JobInfo rv = jobs.get(job);
			if (rv == null) {
				rv = new JobInfo(job);
				if (TeslaFeatures.isActivityLogging()) {
					String msg = "waiting for job: " + job.getClass().getName();
					if (!IGNORED_BY_DEFAULT.contains(job.getClass().getName()))
						debug(msg);
					Q7LoggingManager.get("jobs").log(msg, null);
					ReportManager.appendLogExtra(msg);
				}
				jobs.put(job, rv);
				event("new", rv);
			}
			return rv;
		}
	}

	@Override
	public void aboutToRun(IJobChangeEvent event) {
	}

	@Override
	public void awake(IJobChangeEvent event) {
		Job job = event.getJob();
		if (job.belongsTo(FAMILY)) {
			return;
		}
		JobInfo info = getOrCreateJobInfo(job);
		info.awake();
		event("awake", info);
	}

	@Override
	public void done(IJobChangeEvent event) {
		Job job = event.getJob();
		if (job.belongsTo(FAMILY)) {
			return;
		}
		JobInfo info = null;
		synchronized (jobs) {
			boolean reschedule = TeslaSWTAccess.getJobEventReSchedule(event) && state;
			if (needDisable && isJoinEmpty()) {
				disable();
			}
			if (reschedule) {
				info = getOrCreateJobInfo(job);
				info.done(reschedule);
				event("rescheduled", info);
			} else {
				info = jobs.get(job);
				if (info != null) {
					info.done(reschedule);
					event("done", info);
				}
			}
			
		}
		if (info != null) {
			removeCompletedJob.schedule();
		}
	}

	@Override
	public void running(IJobChangeEvent event) {
	}

	@Override
	public void scheduled(IJobChangeEvent event) {
		synchronized (jobs) {
			if (!state) {
				return;
			}
		}
		Job job = event.getJob();
		if (job.belongsTo(FAMILY)) {
			return;
		}
		JobInfo jobInfo = getOrCreateJobInfo(job);
		jobInfo.scheduled(event.getDelay());
		event("scheduled", jobInfo);
		jobInfo.poke();
	}

	protected JobStatus calcJobStatus(Job job) {
		JobStatus result = detectJobStatus(job);
		if (result == null) {
			result = JobStatus.UNKNOWN;
		}
		return result;
	}

	private static final Set<String> IGNORED_BY_DEFAULT = Collections.unmodifiableSet(new HashSet<>(asList(
			"org.eclipse.jdt.internal.core.search.processing.JobManager$1$ProgressJob",
			"org.eclipse.ui.internal.progress.ProgressViewUpdater$1",
			"org.eclipse.ui.internal.progress.WorkbenchSiteProgressService$SiteUpdateJob",
			"org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.RefreshProgressMessageJob",
			"org.eclipse.ui.dialogs.FilteredItemsSelectionDialog$RefreshProgressMessageJob",
			"org.eclipse.ui.internal.progress.AnimationManager$1",
			"org.eclipse.ui.internal.progress.ProgressManager$6",
			"org.eclipse.ui.internal.progress.ProgressManager$2",
			"org.eclipse.ui.internal.progress.TaskBarProgressManager$1", // Since Oxygen
			"org.eclipse.ui.internal.progress.TaskBarProgressManager$2", // Before Oxygen
			"org.eclipse.rcptt.ecl.internal.core.Session$1",
			"org.eclipse.ui.internal.views.markers.CachedMarkerBuilder$1",
			"org.eclipse.core.internal.utils.StringPoolJob",
			"org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdateScheduler$1", // Update Job
			"org.eclipse.ui.internal.Workbench.lambda$3", // Appears on Jenkins, unclear nature
			"org.eclipse.ui.internal.Workbench$42", // Workbench Auto-Save Job
			"org.eclipse.debug.internal.ui.views.console.ProcessConsole$InputReadJob",
			"org.eclipse.rcptt.ecl.internal.debug.runtime.ServerSession",
			"org.eclipse.ui.internal.Workbench$40", // Workbench Auto-Save Job
			"org.eclipse.jface.text.reconciler.AbstractReconciler$BackgroundThread" // Since 2025-09
			))); 

	public static JobStatus detectJobStatus(Job job) {
		JobStatus status = null;
		if (job.belongsTo(FAMILY)) {
			return JobStatus.IGNORED;
		}
		IJobCollector[] collectors = JobCollectorExtensions.getDefault().getCollectors();

		// Take first status
		for (IJobCollector cl : collectors) {
			JobStatus jobStatus = cl.testJob(job);
			if (jobStatus != null && (!jobStatus.equals(JobStatus.UNKNOWN) && status == null)) {
				status = jobStatus;
				break;
			}
		}
		// Default behavior
		if (status == null) {
			if (job.belongsTo(getFamilyAutoBuild())) {
				status = JobStatus.REQUIRED;
			}
			if (job.belongsTo(TeslaSWTAccess.getDecoratorManagerFamily())) {
				status = JobStatus.REQUIRED;
			}
			if (job.isUser()) {
				status = JobStatus.REQUIRED;
			}
			
			// if (TeslaSWTAccess.isDefferedTreeContentProvider(job)) {
			// status = JobStatus.REQUIRED;
			// }
			String jClassName = job.getClass().getName();
			if (IGNORED_BY_DEFAULT.contains(jClassName))
				status = JobStatus.IGNORED;
		}
		return status;
	}
	
	public UIJobCollector() {
		this(new IParameters() {
			@Override
			public int stepInterval() {
				return TeslaLimits.getStepModeStepTime();
			}
			
			@Override
			public int stepModeStartDelay() {
				return TeslaLimits.getStepModeEnableTimeout();
			}

			@Override
			public int stepModeTimeout() {
				return TeslaLimits.getStepModeTimeout();
			}

			@Override
			public int timeout() {
				return TeslaLimits.getJobTimeout();
			}

			@Override
			public int delayToWaitFor() {
				return TeslaLimits.getJobWaitForDelayedTimeout();
			}
		});
	}
	
	public UIJobCollector(IParameters parameters) {
		if (parameters == null)
			throw new NullPointerException();
		this.parameters = new NormalizedParameters(parameters);
	}

	@Override
	public void sleeping(IJobChangeEvent event) {
		Job job = event.getJob();
		if (job.belongsTo(FAMILY)) {
			return;
		}
		JobInfo info = getOrCreateJobInfo(job);
		info.sleeping();
		event("sleeping", info);
	}

	private static boolean isModal(Shell shell) {
		return PlayerWidgetUtils.isModal(shell);
	}

	private long lastSuccessTime = 0;

	private String getJobMessage(Job job) {
		return getJobMessage(getOrCreateJobInfo(job));
	}

	private static String getJobMessage(JobInfo jobInfo) {
		Job job = jobInfo.job;
		StringBuilder msg = new StringBuilder();
		msg.append("Job: ").append(job.toString()).append("\n");

		msg.append("\tclass: ").append(job.getClass().getName()).append(" ")
				.append(DetailUtils.extractSupers(job.getClass())).append("\n");

		Long startTime = jobInfo.startingTime;

		long currentTimeMillis = System.currentTimeMillis();
		long time = 0;
		if (startTime != null) {
			time = currentTimeMillis - startTime.longValue();
		}

		msg.append("\tworking time: ").append(time).append("(ms)\n");
		msg.append("\tstate: ").append(job.getState()).append("\n");

		ISchedulingRule rule = job.getRule();
		if (rule != null) {
			if (rule instanceof IResource) {
				IPath location = ((IResource) rule).getLocation();
				msg.append("\trule is resource: ").append(location != null ? location.toOSString() : "/").append("\n");
			} else {
				msg.append("\trule: ").append(rule.toString()).append("\n");
			}
		} else {
			msg.append("\trule: Empty\n");
		}

		Thread thread = job.getThread();
		if (thread != null) {
			StackTraceElement[] trace = thread.getStackTrace();
			msg.append("\tstack trace: \n");
			// Print 5 lines of stack trace for job
			int c = 0;
			for (StackTraceElement stackTraceElement : trace) {
				if (c > 15) {
					msg.append("\t\t....");
					break;
				}
				c++;
				msg.append("\t\t" + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + ":"
						+ stackTraceElement.getLineNumber() + (stackTraceElement.getFileName() != null
								? (" [" + stackTraceElement.getFileName() + "]") : ""))
						.append("\n");

			}
		}
		return msg.toString();
	}

	private boolean logReturnResult(boolean result, List<Job> realJobs, List<Job> jobsInUI, Q7WaitInfoRoot info) {
		try {
			debug("Result: " + result);
			long curTime = System.currentTimeMillis();
			if (result) {
				lastSuccessTime = curTime;
				debug("No active jobs");
				return result;
			}
			for (Job job : jobsInUI) {
				Q7WaitUtils.updateInfo("job.ui", job.getClass().getName(), info);
			}
			for (Job job : realJobs) {
				Q7WaitUtils.updateInfo("job", job.getClass().getName(), info);
			}

			if (lastSuccessTime == 0) {
				lastSuccessTime = curTime;
				return result;
			}
			if (curTime - lastSuccessTime > TeslaLimits.getJobLoggingTimeout()) {
				lastSuccessTime = curTime;
				logJobInformation(realJobs, jobsInUI);
			}
			report("Active jobs: " +  realJobs);
		} catch (Exception e) {
			SWTTeslaActivator.log(e);
		}

		return result;
	}

	@SuppressWarnings("deprecation")
	static String getCurrentReportNodeName() {
		final String[] rv = new String[1];
		ReportManager.getCurrentReportNode().update(new Procedure1<Node>() {
			@Override
			public void apply(Node node) {
				rv[0] = node.getName();
			}
		});
		return rv[0];
	}

	private void logJobInformation(List<Job> realJobs, List<Job> jobsInUI) {
		List<Job> otherJobs = new ArrayList<>(jobs.keySet());
		Set<Job> jobInStepMode = new HashSet<>();
		for (Job job : otherJobs) {
			if (getOrCreateJobInfo(job).jobInStepMode)
				jobInStepMode.add(job);
		}
		otherJobs.removeAll(realJobs);
		otherJobs.removeAll(jobsInUI);
		otherJobs.removeAll(jobInStepMode);
		StringBuilder reportMessage = new StringBuilder();

		reportMessage.append("----->>> Waiting for Jobs during execution: ").append(getCurrentReportNodeName())
				.append(" -----<<<< \n");

		if (realJobs.size() > 0) {
			reportMessage.append("---> Standalone Jobs:\n");
			for (Job job : realJobs) {
				reportMessage.append(getJobMessage(job)).append("\n");
			}
		}
		if (jobInStepMode.size() > 0) {
			reportMessage.append("---> Jobs in Stepping mode:\n");
			for (Job job : jobInStepMode) {
				reportMessage.append(getJobMessage(job)).append("\n");
			}
		}
		if (jobsInUI.size() > 0) {
			reportMessage.append("---> Jobs doing UI:\n");
			for (Job job : jobsInUI) {
				reportMessage.append(getJobMessage(job)).append("\n");
			}
		}
		if (otherJobs.size() > 0) {
			reportMessage.append("---> Other jobs:\n");
			for (Job job : otherJobs) {
				reportMessage.append(getJobMessage(job)).append("\n");
			}
		}
		report(reportMessage.toString());
	}

	public boolean isEmpty(Context context, Q7WaitInfoRoot info) {
		// Filter already executed UI jobs with async finish status.
		List<Job> realJobs = new ArrayList<>();
		List<Job> jobsInUI = new ArrayList<>();
		final Display display = Display.getCurrent();

		
		// Remove all canceled jobs
		if (!removeCompletedJobs()) {
			return false;
		}
		synchronized (jobs) {
			if (jobs.isEmpty()) {
				debug("Nothing left");
				return logReturnResult(true, realJobs, jobsInUI, info);
			}
			for (JobInfo jobInfo : jobs.values()) {
				Job job = jobInfo.job;
//				if (!jobInfo.isActive()) {
//					if (DEBUG) {
//						String name = job.getClass().getName();
//						if (!IGNORED_BY_DEFAULT.contains(name))
//							debug("Not active: " + jobInfo);
//					}
//					continue;
//				}
				
				debug(String.format("Checking: %s", jobInfo.toString()));

				if (job.getClass().getName().contains("org.eclipse.debug.internal.ui.DebugUIPlugin$")) {
					// It looks like background launching job.
					Thread thread = job.getThread();
					if (thread == null) {
						report("Active job " + job.getName() + " has no thread");
						jobInfo.done(false);
					}
					Context ctx = ContextManagement.makeContext(thread.getStackTrace());
					// If Auto-build job is active and on lock and there is Modal
					// Dialog in main thread,
					// lets' skip job from important for us.
					if (ctx.contains("java.util.concurrent.locks.LockSupport", "park")
							|| ctx.contains("java.util.concurrent.locks.LockSupport", "parkNanos")
							|| ctx.contains("java.lang.Object", "wait")) {
						// We are waiting some stuff, let's check if main thread
						// have a MessageDialog and if so skip this job right
						// now.
						if (context.contains("org.eclipse.jface.dialogs.MessageDialogWithToggle", "open")
								|| context.contains("org.eclipse.jface.dialogs.MessageDialog", "open")) {
							// Skip job from processing since it in lock and
							// waits for a dialog.
							report("Active job " + job.getName()
									+ " has skipped since it in lock state and Message dialog are active");
							continue;
						}
					}
				}
				
				long timeout = parameters.timeout();
				if (job.belongsTo(getFamilyAutoBuild())) {
					timeout = TeslaLimits.getAutoBuildJobTimeout();
				}
				if (jobInfo.jobInStepMode) {
					timeout = parameters.stepModeTimeout();
				}
				if (job.getClass().getName().contains("org.eclipse.debug.internal.ui.DebugUIPlugin")) {
					timeout = TeslaLimits.getDebugJobTimeout();
				}
				
				if (jobInfo.isActiveFor(Math.max(1, timeout))) {
					report(String.format("Job has timed out after %d, skipping: %s", timeout, jobInfo));
					continue;
				}

				boolean isBlocked = isBlocked(job);
				jobInfo.blocked(isBlocked);
				
				if (jobInfo.isBlockedFor(parameters.stepModeStartDelay())) {
					if (!jobInfo.jobInStepMode) {
						jobInfo.jobInStepMode = true;
						report("Job is blocked for " + parameters.stepModeStartDelay() + ", step mode: " + jobInfo);
					}
				}
				
				if (jobInfo.jobInStepMode) {
					if (jobInfo.isRunningFor(parameters.timeout())) {
						// This job is doing calculations for too long to be considered a sleeping job
						report("Step timeout, skipping: " + jobInfo);
						continue;
					}
					if (jobInfo.isBlockedFor(parameters.stepInterval())) {
						if (jobInfo.canStep(parameters.stepInterval())) {
							report("Job is blocked for " + parameters.stepInterval() + ", stepping: " + jobInfo);
							continue;
						}
					}
				}
				
				if (display != null && display.getThread() == job.getThread()) {
					jobsInUI.add(job);
					continue;
				}
				
				if (context != null) {
					if (isAsyncSupported()) {
						// If we are executed from async finished job lets
						// filter it
						if (JobsManager.getInstance().isFinishedAsyncJob(job)) {
							if (context.containsClass(job.getClass().getName())) {
								jobsInUI.add(job);
								continue;
							}
						}
					}
					if (isSyncSupported()) {
						// Check for any other job running Display.sleep()
						if (context.contains(Display.class.getName(), "sleep")) {
														if (TeslaEventManager.getManager().isJobInSyncExec(job, context)) {
								// If and only if job is already in synchronizer
								Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
								Set<String> names = getSuperClassNames(job);
								Thread jobThread = null;
								Context jobContext = null;
								for (Map.Entry<Thread, StackTraceElement[]> thread : traces.entrySet()) {
									Context ctx = ContextManagement.makeContext(thread.getValue());
									for (String name : names) {
										if (ctx.contains("org.eclipse.core.internal.jobs.Worker", "run")
												&& ctx.contains(name, "run")) {
											jobThread = thread.getKey();
											jobContext = ctx;
										}
									}
								}
								if (jobThread != null && jobContext != null) {
									if (jobContext.contains("org.eclipse.ui.internal.UISynchronizer", "syncExec")
											&& (jobContext.contains("org.eclipse.ui.internal.Semaphore", "acquire")||
													jobContext.contains("org.eclipse.ui.internal.PendingSyncExec", "waitUntilExecuted"))) {
										if (!SWTUIPlayer.hasRunnables(PlatformUI.getWorkbench().getDisplay())) {
											List<Context> execs = TeslaEventManager.getManager().getSyncExecs();
											boolean toContinue = true;
											for (Context context2 : execs) {
												StackTraceElement[] stackTrace = context2.getStackTrace();
												String className = null;
												for (int i = 0; i < stackTrace.length; i++) {
													if (stackTrace[i].getClassName()
															.equals("org.eclipse.swt.widgets.Display")
															&& stackTrace[i].getMethodName().equals("syncExec")) {
														className = stackTrace[i + 1].getClassName();
														break;
													}
												}
												if (className.equals("org.eclipse.e4.ui.internal.workbench.swt.E4Application$1")) //
													continue;
												if (!context.containsClass(className)) {
													// There is a syncExec invocation, in another thread, assume they are waiting for UI, not this job
													if (DEBUG)
														debug("Another thread is waiting for UI thread: " + className);
													toContinue = false;
													break;
												}

											}
											if (toContinue) {
												jobsInUI.add(job);
												continue;
											}
										}
									}
								} else {
									debug("Could not find job context");
								}
							}
						}
					}
				}
				if (jobInfo.isActive())
					realJobs.add(job);
			}
		}
		if (!jobsInUI.isEmpty()) {
			if (realJobs.size() == 1 && realJobs.get(0).belongsTo(getFamilyAutoBuild())) {
				realJobs.clear();
			}
		}
		if (realJobs.size() == 1) {
			Job job = realJobs.iterator().next();
			if (job.belongsTo(getFamilyAutoBuild())) {
				// Check for modal dialogs are visible
				int flags = TeslaSWTAccess.getJobFlags(job);
				// Job is alone and blocked
				if ((flags & 0xFF) == 0x08) {
					return logReturnResult(true, realJobs, jobsInUI, info);
				}
				final boolean value[] = { false };
				if (display != null) {
					display.syncExec(new Runnable() {
	
						@Override
						public void run() {
							Shell[] shells = display.getShells();
							for (Shell shell : shells) {
								if (isModal(shell)) {
									value[0] = true;
								}
							}
						}
					});
				}
				if (value[0]) {
					return logReturnResult(true, realJobs, jobsInUI, info);
				}
				return logReturnResult(false, realJobs, jobsInUI, info);
			}
		}
		return logReturnResult(realJobs.isEmpty(), realJobs, jobsInUI, info);
	}

	private boolean isBlocked(Job job) {
		if (job.getState() == Job.RUNNING) {
			Thread thread = job.getThread();
			if (thread == null)
				return false;
			Context ctx = ContextManagement.makeContext(thread.getStackTrace());
			return TeslaEventManager.getManager().isJobInSyncExec(job, ctx) || ctx.contains("java.lang.Thread", "sleep") || ctx.contains("java.lang.Object", "wait")
					|| ctx.contains("java.util.concurrent.locks.LockSupport", "park") || ctx.contains("sun.nio.ch.SocketDispatcher", "read0");
		}
		return false;
	}

	private boolean removeCompletedJobs() {
		try {
			if (!removeCompletedJob.join(1, null)) {
				return false;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		synchronized (jobs) {
			if (jobs.keySet().stream().allMatch(j -> j.getState() != Job.NONE)) {
				return true;
			}
		}
		removeCompletedJob.schedule();
		return false;
	}

	private Set<String> getSuperClassNames(Job job) {
		Class<?> cl = job.getClass();
		Set<String> names = new HashSet<>();
		while (true) {
			if (cl.equals(Job.class)) {
				break;
			}
			names.add(cl.getName());
			Class<?> superclass = cl.getSuperclass();
			if (superclass == null) {
				break;
			}
			cl = superclass;
		}
		return names;
	}

	protected boolean isSyncSupported() {
		return true;
	}

	protected boolean isAsyncSupported() {
		return true;
	}

	public void enable() {
		debug("enable");
		// Add all current jobs to wait queue
		Collection<JobInfo> copy;
		synchronized (jobs) {
			this.state = true;
			this.needDisable = false;
			copy = new ArrayList<>(jobs.values());
		}
		for (JobInfo jobInfo: copy) {
			if (jobInfo.isActive())
				jobInfo.poke();
		}
	}

	private static Object getFamilyAutoBuild() {
		try {
			return ResourcesPlugin.FAMILY_AUTO_BUILD;
		} catch (Throwable e) {
			// Skip
		}
		return UIJobCollector.class;
	}

	public void disable() {
		synchronized (jobs) {
			this.state = false;
			this.needDisable = false;
		}
		debug("disable");
	}

	public void setNeedDisable() {
		this.needDisable = true;
	}

	public int getCount() {
		synchronized (jobs) {
			return jobs.size();
		}
	}

	public List<Job> getJobs() {
		synchronized (jobs) {
			ArrayList<Job> rv = new ArrayList<>();
			for (JobInfo info : jobs.values())
				if (info.isActive())
					rv.add(info.job);
			return rv;
		}
	}

	/**
	 * Wait untill will not be empty for timeout
	 * 
	 * @param timeout
	 * @throws InterruptedException
	 */
	public void join(long timeout, BooleanSupplier isCancelled) throws InterruptedException {
		SWTTeslaActivator.debugLog("UIJobCollector is going to join");
		long startTime = System.currentTimeMillis();
		// Context ctx = ContextManagement.currentContext();
		while (true) {
			long delta = System.currentTimeMillis() - startTime;
			if (delta > timeout) {
				break;
			}
			if (isCancelled.getAsBoolean()) {
				return;
			}
			if (removeCompletedJobs()) {
				if (isJoinEmpty()) {
					break;
				}
	
				List<Job> jobs2 = getJobs();
				for (Job job : jobs2) {
					SWTTeslaActivator.debugLog("Waiting for job:" + job.getName() + " " + job.getState());
				}
				SWTTeslaActivator.debugLog("UIJobCollector is going to join");
			}
			Thread.sleep(50);
		}
	}

	private boolean isJoinEmpty() {
		List<Job> realJobs = new ArrayList<>();
		synchronized (jobs) {
			if (jobs.isEmpty()) {
				return true;
			}
			for (JobInfo jobInfo : jobs.values()) {
				if (!jobInfo.isActive())
					continue;
				Job job = jobInfo.job;
				Set<String> names = getSuperClassNames(job);

				// Locate thread
				Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
				boolean toContinue = false;
				for (Map.Entry<Thread, StackTraceElement[]> thread : traces.entrySet()) {
					Context ctx = ContextManagement.makeContext(thread.getValue());
					for (String name : names) {
						if (ctx.contains(name, "run") || ctx.contains(name, "runInUIThread")) {
							// Job already running
							if (ctx.contains("org.eclipse.swt.widgets.Display", "sleep")) {
								// Job are showing dialog. Skip waiting for such
								// job.
								toContinue = true;
								break;
							}
						}
					}
				}
				if (toContinue) {
					continue;
				}
				realJobs.add(job);
			}
		}
		if (realJobs.size() == 1) {
			Job job = realJobs.iterator().next();
			if (job.belongsTo(getFamilyAutoBuild())) {
				// Check for modal dialogs are visible
				int flags = TeslaSWTAccess.getJobFlags(job);
				// Job is alone and blocked
				if (flags == 0x08) {
					return true;
				}
			}
		}
		return realJobs.isEmpty();
	}

	public void clean() {
		synchronized (jobs) {
			jobs.clear();
			debug("clean");
		}
	}

	private void debug(String message) {
		if (DEBUG) {
			DEBUG_WRITER.printf("%d: UIJobCollector(%d, %s): %s\n", System.currentTimeMillis(), System.identityHashCode(this), Thread.currentThread().getName(), message);
			DEBUG_WRITER.flush();
		}
	}

	private void report(String message) {
		SWTTeslaActivator.logToReport(message);
		if (DEBUG_REPORT_OUTPUT) {
			debug(message);
		}
	}
	
	private void event(String message, JobInfo job) {
		if (job == null) {
			return;
		}
		if (shouldDebug(job.job)) {
			debug(String.format("event: %11s: %s, size: %d", message, job, jobs.size()));
		}
	}
	
	private static boolean shouldDebug(Job job) {
		return DEBUG && !IGNORED_BY_DEFAULT.contains(job.getClass().getName());
	}
}
