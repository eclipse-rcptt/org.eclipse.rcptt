/*******************************************************************************
 * Copyright (c) 2019-2024 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.swt.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.info.InfoFactory;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfo;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.internal.ui.player.UIJobCollector;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import com.google.common.io.Closer;

public class UIJobCollectorTest {
	private static final int schedulingTolerance = 500;
	private static final IJobManager MANAGER = Job.getJobManager();
	private final Closer closer = Closer.create();
	@Rule
	public final TestRule noErrorsInLog = new NoErrorsInLog(Job.class); 
	private final Job sleepingJob = Job.create("sleep", (ICoreRunnable)monitor -> {
		while (!monitor.isCanceled()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new AssertionError(e);
			}
		}
	});
	private final Job oscillatingJob = Job.create("oscillating", (ICoreRunnable)monitor -> {
		long step = 500;
		while (!monitor.isCanceled()) {
			try {
				Thread.sleep(step);
				long stop = System.currentTimeMillis() + step;
				while (System.currentTimeMillis() < stop && !monitor.isCanceled())
					Thread.yield();
			} catch (InterruptedException e) {
				throw new AssertionError(e);
			}
		}
	});
	
	private final Job busyLoop = Job.create("busyloop", (ICoreRunnable)monitor -> {
		if (monitor.isCanceled()) {
			debug("Busyloop - cancelled upon start");	
			return;
		}
		debug("Busyloop - start");
		while (!monitor.isCanceled()) {
			Thread.yield();
		}
		debug("Busyloop - end");
	});
	
	private final Job rescheduling = new Job("rescheduling") {
		protected IStatus run(IProgressMonitor monitor) {
			rescheduling.schedule(10000);
			return Status.OK_STATUS;
		}
	};
	
	{
		rescheduling.setPriority(Job.INTERACTIVE);
		sleepingJob.setPriority(Job.INTERACTIVE);
		oscillatingJob.setPriority(Job.INTERACTIVE);
	}

	@Before
	public void before() {
		TeslaFeatures.getInstance().getOption(TeslaFeatures.REPORT_INCLUDE_ECLIPSE_METHODS_WAIT_DETAILS).setValue("true");
	}
	
	@Before
	public void waitForAllJobs() throws InterruptedException, CoreException {
		rescheduling.cancel();
		sleepingJob.cancel();
		oscillatingJob.cancel();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription d = workspace.getDescription();
		d.setAutoBuilding(false);
		workspace.setDescription(d);
		Display display = Display.getCurrent();
		while (display != null && display.readAndDispatch()) {
		}
		UIJobCollector subject = new UIJobCollector();
		MANAGER.addJobChangeListener(subject);
		try {
			subject.enable();
			boolean found = true;
			while (found) {
				idle();
				found = MANAGER.find(org.eclipse.ui.internal.decorators.DecoratorManager.FAMILY_DECORATE).length > 0
						|| !isEmpty(subject);
			}
		} finally {
			MANAGER.removeJobChangeListener(subject);
		}
	}

	private void idle() {
		Display display = Display.getCurrent();
		int sleepTime = schedulingTolerance / 10;
		try {
			if (display != null) {
				long stop = System.currentTimeMillis() + sleepTime;
				while (display.readAndDispatch() || System.currentTimeMillis() < stop) {
					Thread.sleep(1);
				}
			} else {
				Thread.sleep(sleepTime);
			}
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
	}

	@Test
	public void emptyWhenNoJobs() {
		UIJobCollector subject = new UIJobCollector();
		addListener(subject);
		subject.enable();
		assertEmpty("No relevant jobs are expected on start", subject);
	}

	@Test
	public void stepMode() throws InterruptedException {
		Parameters parameters = new Parameters();
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		long start = System.currentTimeMillis();
		sleepingJob.schedule();
		Assert.assertFalse("Should not step immediately", isEmpty(subject));
		sleepUntil(start + parameters.stepModeStartDelay - schedulingTolerance);
		Assert.assertFalse("Should not step until delay", isEmpty(subject));
		join(subject, System.currentTimeMillis() - start + parameters.stepModeStartDelay);
		Assert.assertFalse("Should not step twice", isEmpty(subject));
		Thread.sleep(parameters.stepModeStepInterval + schedulingTolerance);
		assertEmpty("Should step after step interval", subject);
	}
		
	@Test
	public void waitSecondRunAfterReschedule() throws InterruptedException {
		Parameters parameters = new Parameters();
		parameters.timeout = 60000;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		Job job = busyLoop;
		for (int i = 0; i < 10; i++) {
			final int attempt = i;
			Assert.assertTrue(shutdown(job, 10000));
			join(subject, 10000);
			CountDownLatch startedOnce = new CountDownLatch(1);
			CountDownLatch completedOnce = new CountDownLatch(1);
			IJobChangeListener jobListener = new JobChangeAdapter() {
				@Override
				public void scheduled(IJobChangeEvent event) {
					super.scheduled(event);
					debug("Attempt " + attempt + ". Scheduled " + event.getJob());
				}
				@Override
				public void running(IJobChangeEvent event) {
					super.running(event);
					debug("Attempt " + attempt + ". Running " + event.getJob());
					startedOnce.countDown();
				}
				@Override
				public void done(IJobChangeEvent event) {
					super.done(event);
					debug("Attempt " + attempt + ". Done " + event.getJob());
					completedOnce.countDown();
				}
			};
			addListener(job, jobListener);
			debug("Attempt " + i);
			assertEmpty("No jobs on start", subject);
			while (job.getState() == Job.NONE) { // Sometimes the job is spuriously cancelled (by previous cycles?)				
				job.schedule();
			}
			startedOnce.await();
			job.cancel();
			job.schedule();
			completedOnce.await();
			Thread.sleep(schedulingTolerance);
			boolean result = isEmpty(subject);
			Assert.assertFalse("Should not step immediately", result);
			Assert.assertNotEquals(Job.NONE, job.getState());
			debug("End of attempt " + i);
			job.removeJobChangeListener(jobListener);
			job.cancel();
		}
	}
	
	@Test
	public void doNotWaitForJobsRescheduledInFuture() throws InterruptedException {
		Parameters parameters = new Parameters();
		parameters.timeout = 60000;
		parameters.stepModeTimeout = 120000;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		assertEmpty("No jobs on start", subject);
		rescheduling.schedule(0);
		long start = System.currentTimeMillis();
		rescheduling.join();
		Thread.sleep(schedulingTolerance);
		assertEmpty("Skip jobs scheduled for distant future", subject);
		Assert.assertTrue(System.currentTimeMillis() < start + 1000);
	}
	
	@Test
	public void doNotWaitForPrescheduledRescheduledInFuture() throws InterruptedException {
		Parameters parameters = new Parameters();
		parameters.timeout = 60000;
		parameters.stepModeTimeout = 120000;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		subject.disable();
		addListener(rescheduling, new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				subject.enable();
		}});
		rescheduling.schedule(100);
		Thread.sleep(50);
		rescheduling.join();
		Thread.sleep(schedulingTolerance);
		assertEmpty("Skip jobs scheduled for distant future", subject);
	}
	
	@Test
	public void doNotWaitForCancelledScheduledLater() throws InterruptedException {
		Parameters parameters = new Parameters();
		parameters.timeout = 60000;
		parameters.stepModeTimeout = 120000;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		subject.disable();
		addListener(rescheduling, new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				subject.enable();
		}});
		rescheduling.schedule(10000);
		rescheduling.cancel();
		rescheduling.schedule(10000);
		Thread.sleep(schedulingTolerance);
		assertEmpty("Skip jobs scheduled for distant future", subject);
	}
	
	@Test
	public void doWaitForCancelledRescheduled() throws InterruptedException {
		Parameters parameters = new Parameters();
		parameters.timeout = 60000;
		parameters.stepModeTimeout = 120000;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		busyLoop.schedule(parameters.timeout);
		busyLoop.cancel();
		busyLoop.schedule(schedulingTolerance);
		Assert.assertFalse(isEmpty(subject));
	}
	
	@Test(timeout = 60000)
	public void waitForAllListeners() throws InterruptedException {
		Assume.assumeTrue(FrameworkUtil.getBundle(Job.class).getVersion().compareTo(Version.parseVersion("3.15")) > 0);
		Parameters parameters = new Parameters();
		parameters.timeout = 60000;
		parameters.stepModeTimeout = 120000;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch stop = new CountDownLatch(1);
		addListener(busyLoop, new JobChangeAdapter() {public void done(IJobChangeEvent event) {
			try {
				System.out.println("Job is cancelled");
				start.countDown();
				stop.await();
				System.out.println("Job is done");
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError(e);
			}
		};});
		busyLoop.schedule();
		while (busyLoop.getState() != Job.RUNNING) {
			Thread.yield();
		}
		busyLoop.cancel();
		start.await();
		Assert.assertEquals(Job.NONE, busyLoop.getState());
		Assert.assertFalse(isEmpty(subject));
		stop.countDown();
		join(subject, 0);
	}	
	
	private boolean shutdown(Job job, int timeoutInSeconds) throws InterruptedException {
		long stop = System.currentTimeMillis() + timeoutInSeconds * 1000;
		job.cancel();
		while (job.getState() != Job.NONE && System.currentTimeMillis() < stop) {
			job.cancel();
			job.join(1, null);
		}
		return job.getState() == Job.NONE;
	}
	
	
	@Test
	public void waitForCancelledJobs() throws InterruptedException {
		Parameters parameters = new Parameters();
		parameters.timeout = 60000;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		CountDownLatch startedOnce = new CountDownLatch(1);
		IJobChangeListener jobListener = new JobChangeAdapter() {
			@Override
			public void running(IJobChangeEvent event) {
				super.running(event);
				startedOnce.countDown();
			}
		};
		Job job = Job.create("ignoringCancel", (ICoreRunnable)monitor -> {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
		});
		
		closeJobAfterTest(job);
		
		addListener(job, jobListener);
		job.schedule();
		startedOnce.await();
		job.cancel();
		boolean result = isEmpty(subject);
		Assert.assertNotEquals(Job.NONE, job.getState());
		Assert.assertFalse("Should not step immediately", result);
	}
	
	

	private void prepare(UIJobCollector subject) {
		addListener(subject);
		subject.enable();
		idle(); // Waiting for irrelevant jobs to start
		join(subject, 10000); // Waiting for irrelevant jobs to complete
	}

	@Test
	public void stepAfterDelay() throws InterruptedException {
		Parameters parameters = new Parameters();
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		sleepingJob.schedule();
		idle();
		Assert.assertFalse("Should not step immediately", isEmpty(subject));
		join(subject, parameters.stepModeStartDelay);
	}
	
	@Test
	public void stepRepeatedly() {
		Parameters parameters = new Parameters();
		parameters.stepModeStartDelay = 1;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		sleepingJob.schedule();
		idle();
		int expected = 7;
		int waitTime = parameters.stepInterval() * expected;
		Assert.assertTrue("This test relies on stepModeTimeout to be big enough", waitTime < parameters.stepModeTimeout);
		long stop = System.currentTimeMillis() + waitTime;
		int stepCount = 0;
		while (System.currentTimeMillis() < stop) {
			idle();
			join(subject, parameters.stepInterval());
			stepCount++;
		}
		Assert.assertTrue("Step count expected " +expected + " but was: " + stepCount, expected - 2 < stepCount && stepCount < expected + 2);
	}
	
	@Test
	public void abortStepMode() throws InterruptedException {
		Parameters parameters = new Parameters();
		parameters.stepModeStartDelay = parameters.stepInterval();
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		long sleepTime = parameters.stepModeStartDelay() + parameters.stepInterval() * 2;
		Job job = Job.create("runaway", (ICoreRunnable)monitor -> {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				throw new AssertionError(e);
			}
			// We slept for a while, and now are working indefinitely.
			while (!monitor.isCanceled()) {}
		});
		job.setPriority(Job.INTERACTIVE);
		closeJobAfterTest(job);
		job.schedule();
		join(subject, parameters.stepInterval());
		join(subject, parameters.stepInterval());
		Thread.sleep(sleepTime);
		Assert.assertFalse(isEmpty(subject)); // Job is recognized as running now, steps should no longer happen
		join(subject, parameters.timeout);
	}

	@Test
	public void doNotWaitForPreviouslyScheduledJob() throws InterruptedException {
		Parameters parameters = new Parameters();
		busyLoop.schedule(parameters.timeout);
		UIJobCollector subject = new UIJobCollector(parameters);
		addListener(subject);
		subject.enable();
		assertEmpty("Ignore old jobs, schduled before collector starts", subject);
	}

	@Test
	public void doNotStepTwice() throws InterruptedException {
		Parameters parameters = new Parameters();
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		sleepingJob.schedule();
		join(subject, parameters.stepModeStartDelay);
		Thread.sleep(schedulingTolerance);
		Assert.assertFalse("Should not step twice", isEmpty(subject));
	}

	@Test
	public void respectStepModeTimeout() {
		Parameters parameters = new Parameters();
		parameters.delayToWaitFor = 0;
		parameters.stepModeTimeout = parameters.timeout + schedulingTolerance;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		sleepingJob.schedule();
		long stop = System.currentTimeMillis() + parameters.stepModeTimeout;
		join(subject, parameters.stepModeStartDelay);
		while (System.currentTimeMillis() < stop) {
			join(subject, parameters.stepModeStepInterval);
		}

		// Do not block after timeout
		while (System.currentTimeMillis() < stop + parameters.stepModeStepInterval * 10) {
			idle();
			assertEmpty("Do not wait for jobs afer step mode timeout", subject);;
		}

	}

	@Test
	public void stepInSyncExec() {
		Display display = Display.getCurrent();
		Assume.assumeNotNull(display);
		Parameters parameters = new Parameters();
		parameters.stepModeStartDelay = schedulingTolerance * 3;
		parameters.stepModeStepInterval = schedulingTolerance;
		UIJobCollector subject = new UIJobCollector(parameters);
		addListener(subject);
		MessageDialog dialog = new MessageDialog(null, "hello", null, "press X", 0, 0, new String[] { "X" });
		Job closeDialog = Job.create("close dialog", monitor -> {
			while (!monitor.isCanceled() && dialog.getShell() == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
			}
			if (monitor.isCanceled())
				return;
			join(subject, parameters.stepModeStartDelay);
			dialog.getShell().getDisplay().syncExec(dialog::close);
		});
		closeJobAfterTest(closeDialog);
		closeDialog.schedule();
		Job showDialog = Job.create("show dialog", monitor -> {
			long start = System.currentTimeMillis();
			display.syncExec(() -> dialog.open());
			Assert.assertTrue(System.currentTimeMillis() < start + parameters.stepModeStartDelay + schedulingTolerance);
		});
		closeJobAfterTest(showDialog);
		showDialog.schedule();
		while (dialog.getShell() != null) {
			idle();
		}
		join(subject, parameters.stepModeStartDelay);
	}
	
	@Test
	public void ignorePseudoJobsInUI() throws InterruptedException {
		Display display = Display.getCurrent();
		Assume.assumeNotNull(display);
		Parameters parameters = new Parameters();
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
			IJobManager jobManager = Job.getJobManager();
			IWorkspaceRoot rule = ResourcesPlugin.getWorkspace().getRoot();
		try {
			jobManager.beginRule(rule, null);
			subject.enable();
			Thread.sleep(schedulingTolerance);
			assertEmpty("Do not wait for UI jobs", subject);
		} finally {
			jobManager.endRule(rule);
		}
	}
	
	
	private static final class ShortJob extends Job {
		public ShortJob() {
			super("short");
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			setUser(false);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			return Status.OK_STATUS;
		}
		
	}
	@Test
	public void doNotScheduleInALock() {
		Parameters parameters = new Parameters();
		parameters.delayToWaitFor = 0;
		parameters.stepModeTimeout = parameters.timeout + schedulingTolerance;
		UIJobCollector subject = new UIJobCollector(parameters);
		prepare(subject);
		
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j<100; j++ ) {
				closeJobAfterTest(new ShortJob()).schedule();
			}
			long start = System.nanoTime();
			isEmpty(subject);
			long stop = System.nanoTime();
			double seconds = (double)(stop-start)/1e9;
			Assert.assertTrue(String.format("Job collector should not deadlock, but waited for %f seconds",  seconds), seconds < 1);
		}
	}

	@After
	public void after() throws IOException {
		closeJobAfterTest(sleepingJob);
		closeJobAfterTest(oscillatingJob);
		closeJobAfterTest(busyLoop);
		closeJobAfterTest(rescheduling);
		closer.close();
	}

	private void join(UIJobCollector subject, long timeout) {
		long stop = System.currentTimeMillis() + timeout + schedulingTolerance;
		while (System.currentTimeMillis() < stop) {
			idle();
			long moment = System.currentTimeMillis();
			if (isEmpty(subject))
				return;
		}
		throw new AssertionError("timeout after " + timeout);
	}

	private void sleepUntil(long moment) {
		while (System.currentTimeMillis() < moment) {
			idle();
		}
	}

	private static boolean isEmpty(UIJobCollector subject) {
		return subject.isEmpty(new org.eclipse.rcptt.tesla.core.context.ContextManagement.Context(),
				InfoFactory.eINSTANCE.createQ7WaitInfoRoot());
	}

	@SuppressWarnings("resource")
	private void addListener(IJobChangeListener listener) {
		MANAGER.addJobChangeListener(listener);
		closer.register(() -> MANAGER.removeJobChangeListener(listener));
	}
	
	@SuppressWarnings("resource")
	private void addListener(Job job, IJobChangeListener listener) {
		job.addJobChangeListener(listener);
		closer.register(() -> job.removeJobChangeListener(listener));
	}

	@SuppressWarnings("resource")
	private Job  closeJobAfterTest(Job job) {
		closer.register(() -> cancel(job));
		return job;
	}

	private void cancel(Job job) {
		job.cancel();
		if (job.getState() == Job.NONE)
			return;
		try {
			for (int i = 0; i < 10000 && !job.join(1, null); i++) {
				idle();
			}
			if (!job.join(1, null)) {
				throw new TimeoutException("Job " + job.getName() + " failed to complete");
			}
			IStatus result = job.getResult();
			if (result.matches(IStatus.WARNING | IStatus.ERROR)) {
				throw new CoreException(result);
			}
		} catch (Throwable e) {
			throw new AssertionError("Job " + job.getName() + " failed to complete", e);
		}
	}

	private final class Parameters implements UIJobCollector.IParameters {
		public int stepModeStepInterval = schedulingTolerance * 2;
		public int stepModeStartDelay = stepModeStepInterval * 2;
		public int timeout = stepModeStartDelay * 5;
		public int stepModeTimeout = timeout * 2;
		public int delayToWaitFor = schedulingTolerance*3;

		@Override
		public int stepModeStartDelay() {
			return stepModeStartDelay;
		}

		@Override
		public int stepInterval() {
			return stepModeStepInterval;
		}

		@Override
		public int stepModeTimeout() {
			return stepModeTimeout;
		}

		@Override
		public int timeout() {
			return timeout;
		}

		@Override
		public int delayToWaitFor() {
			return delayToWaitFor;
		}

	}
	
	private void assertEmpty(String message, UIJobCollector subject) {
		Q7WaitInfoRoot waitInfo = InfoFactory.eINSTANCE.createQ7WaitInfoRoot();
		boolean result = subject.isEmpty(new org.eclipse.rcptt.tesla.core.context.ContextManagement.Context(), waitInfo);
		Assert.assertTrue(message + ", but found jobs:\n" + toString(waitInfo), result);
	}

	private static String toString(Q7WaitInfoRoot root) {
		StringBuilder stream = new StringBuilder();
		for (Q7WaitInfo i : root.getInfos()) {
			long totalTime = i.getDuration();
			String type = getType(root, i);
			String className = getClassName(root, i);
			if (type == null) {
				continue;
			}
			if (!TeslaFeatures.isIncludeEclipseMethodsWaitDetails()
					&& className.startsWith("org.eclipse")) { //$NON-NLS-1$
				continue;
			}
			stream.append(type).append(": ").append(className);

			if (totalTime != 0)
				stream.append(", total time: ").append(Long.toString(totalTime));
		}
		return stream.toString();
	}
	
	public static String getClassName(Q7WaitInfoRoot waitRoot, Q7WaitInfo i) {
		String className = waitRoot.getClassNames().get(i.getClassId());
		String originalClassName = className;
		if (className != null && waitRoot.getInnerClassMap().containsKey(className)) {
			int pos = className.indexOf("$");
			if (pos != -1) {
				className = className.substring(0, pos);
			}
			className += '.' + waitRoot.getInnerClassMap().get(originalClassName);
		}
		return className + "(" + originalClassName + ")";
	}
	
	public static String getType(Q7WaitInfoRoot info, Q7WaitInfo q7WaitInfo) {
		String type = info.getTypesNames().get(q7WaitInfo.getTypeId());
		if (!TeslaFeatures.isIncludeIgnoredWaitDetails() && type.contains("(ignored)")) {
			return null;
		}
		return type;
	}

	private void debug(String message) {
//		System.out.printf("Junit Test: %s\n", message);
	}

}
