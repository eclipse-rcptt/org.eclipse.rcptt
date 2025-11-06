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

import static org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin.PLUGIN_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.ecl.core.model.ExecutionPhase;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.ISearchScope;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.scenario.GroupContext;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.SuperContext;
import org.eclipse.rcptt.core.scenario.TestSuiteItem;
import org.eclipse.rcptt.core.scenario.UnresolvedContext;
import org.eclipse.rcptt.core.scenario.UnresolvedVerification;
import org.eclipse.rcptt.core.utils.ModelCycleDetector;
import org.eclipse.rcptt.core.utils.SortingUtils;
import org.eclipse.rcptt.core.workspace.IWorkspaceFinder;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.core.workspace.WorkspaceFinder;
import org.eclipse.rcptt.ecl.debug.core.DebuggerTransport;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ecl.EclContextExecutable;
import org.eclipse.rcptt.internal.launching.ecl.EclDebugContextExecutable;
import org.eclipse.rcptt.internal.launching.ecl.EclDebugTestExecutable;
import org.eclipse.rcptt.internal.launching.ecl.EclDebugVerificationExecutable;
import org.eclipse.rcptt.internal.launching.ecl.EclScenarioExecutable;
import org.eclipse.rcptt.internal.launching.ecl.EclVerificationExecutable;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.IExecutable.State;
import org.eclipse.rcptt.launching.IExecutionSession;
import org.eclipse.rcptt.launching.ILaunchListener;
import org.eclipse.rcptt.launching.TestCaseDebugger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class Q7LaunchManager {

	public static final String Q7_TEST_SUITE_LAUNCH_ID = "org.eclipse.rcptt.launching.scenarios"; //$NON-NLS-1$

	public static class SessionRunnable implements Runnable, IDebugEventSetListener {
		private final String launchId;
		private final ExecutionSession session;
		private final Q7Process q7Process;

		public SessionRunnable(final String launchId,
				final ExecutionSession session, final Q7Process q7Process) {
			this.launchId = launchId;
			this.session = session;
			this.q7Process = q7Process;
		}

		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent event : events) {
				Object source = event.getSource();
				if (q7Process == source
						&& event.getKind() == DebugEvent.TERMINATE) {
					Q7LaunchManager.getInstance()
							.stop(new Status(IStatus.CANCEL, PLUGIN_ID, "Aut has been terminated"));
				}
			}
		}

		@Override
		public void run() {
			session.resetCounters();
			session.setStartTime(new Date());
			DebugPlugin.getDefault().addDebugEventListener(this);
			final Executable[] executables = session.getExecutables();

			Q7LaunchManager.getInstance().fireStarted(session);
			TestEngineManager.getInstance().fireSessionStarted(session);
			IStatus result = Status.OK_STATUS;
			try {
				List<Executable> massUpdateOnTerminate = new ArrayList<Executable>();
				final Executable.Listener listener = new Executable.Listener() {
					@Override
					public void onStatusChange(Executable executable) {
						Q7LaunchManager.getInstance().fireLaunchStatusChanged(executable);
						if (executable.getStatus() == State.RUNNING)
							session.setActive(executable);
					}

					@Override
					public void updateSessionCounters(Executable executable, IStatus status) {
						Q7LaunchManager.getInstance().updateSessionCounters(session, status);
					}
				};
				for (final Executable executable : executables) {
					if (!session.isRunning()) {
						// break full execution
						massUpdateOnTerminate.add(executable);
						// fireLaunchStatusChanged(executable);
						executable.cancel(session.getResultStatus());
						continue;
					}
					executable.addListener(listener);
					try {
						executable.executeAndRememberResult();
						if (result.isOK()) {
							result = executable.getResultStatus();
						}
					} finally {
						executable.removeListener(listener);
						session.setActive(null);
						Q7LaunchManager.getInstance().fireLaunchStatusChanged(executable);
					}
					Q7LaunchManager.getInstance().fireLaunchStatusChanged(executable);
				}
				if (massUpdateOnTerminate.size() > 0) {
					Q7LaunchManager.getInstance().fireLaunchStatusChanged(massUpdateOnTerminate
							.toArray(new Executable[massUpdateOnTerminate
									.size()]));
				}
				TestEngineManager.getInstance().fireSessionCompleted(session);
			} catch (final Throwable e) {
				result = RcpttPlugin.createStatus(e);
			} finally {
				Preconditions.checkNotNull(result);
				DebugPlugin.getDefault().removeDebugEventListener(this);
				synchronized (Q7LaunchManager.getInstance()) {
					Q7LaunchManager.getInstance().threads.remove(launchId);
				}
				session.stop(result);
				session.setEndTime(new Date());
				Q7LaunchManager.getInstance().fireFinished(session);
			}
		}
	}

	public static boolean isConnectionException(IStatus status) {
		if (status.getMessage().equals("Connection reset")
				|| status.getMessage().equals("Connection refused: connect")) {
			return true;
		}
		return false;
	}

	private static class SingletonHolder {
		private static Q7LaunchManager INSTANCE = null;
	}

	private static boolean headless = false;

	public static void makeHeadless() {
		headless = true;
	}

	public synchronized static Q7LaunchManager getInstance() {
		if (SingletonHolder.INSTANCE == null) {
			SingletonHolder.INSTANCE = new Q7LaunchManager();
		}
		return SingletonHolder.INSTANCE;
	}

	private final Map<String, ExecThread> threads = new HashMap<String, ExecThread>();
	private final List<ILaunchListener> listeners;

	private Q7LaunchManager() {
		final List<ILaunchListener> list = new ArrayList<ILaunchListener>();
		final IConfigurationElement[] elements = Platform
				.getExtensionRegistry().getConfigurationElementsFor(EXPT_ID);
		for (final IConfigurationElement element : elements) {
			String uiAttr = element.getAttribute("ui");
			if (headless) {
				if ("true".equals(uiAttr)) {
					continue;// skip ui listener
				}
			}
			try {
				final ILaunchListener listener = (ILaunchListener) element
						.createExecutableExtension(ATTR_CLASS);
				list.add(listener);
			} catch (final CoreException e) {
				Q7LaunchingPlugin.log(e);
			}
		}
		listeners = list;
	}

	public synchronized boolean isRunning() {
		return !threads.isEmpty();
	}

	public synchronized void stop(IStatus result) {
		for (final String launch : new HashSet<String>(threads.keySet())) {
			ExecThread thread = threads.get(launch);
			if (thread != null) {
				thread.stop(result);
				threads.remove(launch);
			}
		}
	}

	public synchronized void addListener(final ILaunchListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeListener(final ILaunchListener listener) {
		listeners.add(listener);
	}

	public void execute(IQ7NamedElement[] elements, AutLaunch aut,
			Q7TestLaunch launch, IWorkspaceFinder finder,
			Map<IQ7NamedElement, List<List<String>>> namedVariants, 
			BiFunction<String, Integer, DebuggerTransport> debugTransport)
			throws CoreException {
		// avoid circles
		if (ModelCycleDetector.hasCycles(elements)) {
			throw new CoreException(new Status(IStatus.ERROR,
					Q7LaunchingPlugin.PLUGIN_ID,
					"Can't execute testcases/testsuites. Circles detected."));
		}
		// init finder
		if (finder == null) {
			finder = WorkspaceFinder.getInstance();
		}

		// create process
		Q7Process process = new Q7Process(launch, aut, debugTransport);

		// create executable elements
		ExecutableFactory executableFabric = new ExecutableFactory(aut, finder, process.getDebugger());
		Executable[] executables = executableFabric.map(elements, namedVariants, aut.getCapability());
		if (executableFabric.unresolvedItems.size() > 0) {
			if (!checkContinueOnUnresolved())
				return;
		}

		// create session
		final ExecutionSession session = new ExecutionSession(launch
				.getLaunchConfiguration().getName(), executables, aut, launch);
		final int maxEntries = getMaxHistoryEntries();
		while (sessions.size() >= maxEntries) {
			final ExecutionSession rSession = sessions.remove(0);
			rSession.dispose();
		}

		// Cancel debug for this AUT.
		if (!cancelDebugExecution(aut, null)) {
			// If user choose not to cancel
			launch.terminate();
			return;
		}

		launch.setSession(session);

		// start execution
		execute(session, process);
	}

	private boolean checkContinueOnUnresolved() {
		IStatus status = new Status(
				IStatus.ERROR,
				Q7LaunchingPlugin.PLUGIN_ID,
				AutStatusConstants.UNRESOLVED_REFERENCES,
				"There are unresolved test suite items. Build the project to find them.",
				null);
		IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(
				status);
		if (prompter != null) {
			try {
				Object result = prompter.handleStatus(status, null);
				if (result instanceof Boolean) {
					return (Boolean) result;
				}
			} catch (CoreException e) {
				Q7LaunchingPlugin.log(e);
			}
		}
		return true;
	}

	public boolean cancelDebugExecution(AutLaunch aut, Object shell)
			throws DebugException {
		// Terminate launched debug sessions for same aut
		List<ExecutionSession> toDispose = new ArrayList<ExecutionSession>();
		for (ExecutionSession rSession : sessions) {
			if (rSession.isDebugging(aut)) {
				toDispose.add(rSession);
			}
		}
		if (toDispose.size() > 0) {
			// Ask user for disposing.
			if (!checkCancelDebugExecutionStatus(shell)) {
				return false;
			}
		}

		sessions.removeAll(toDispose);
		for (ExecutionSession executionSession : toDispose) {
			executionSession.getLaunch().terminate();
			while (executionSession.isRunning()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Q7LaunchingPlugin.log(e);
				}
			}
		}
		return true;
	}

	private boolean checkCancelDebugExecutionStatus(Object shell) {
		IStatus status = new Status(IStatus.INFO, Q7LaunchingPlugin.PLUGIN_ID,
				AutStatusConstants.CANCEL_DEBUG_SESSIONS_CODE,
				"Debug session are active.", null);
		IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(
				status);
		if (prompter != null) {
			try {
				Object result = prompter.handleStatus(status, shell);
				if (result instanceof Boolean && !((Boolean) result)) {
					return false;
				}
			} catch (CoreException e) {
				Q7LaunchingPlugin.log(e);
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public int getMaxHistoryEntries() {
		final IEclipsePreferences node = new InstanceScope()
				.getNode(Q7LaunchingPlugin.PLUGIN_ID);
		int count = node.getInt(PREFERENCE_HISTORY_MAX_EXECUTIONS_COUNT, 0);
		if (count == 0) {
			count = HISTORY_MAX_EXECUTIONS_DEFAULT;
		}
		return count;
	}

	@SuppressWarnings("deprecation")
	public void setMaxHistoryEntries(final int maxEntries) {
		final IEclipsePreferences node = new InstanceScope()
				.getNode(Q7LaunchingPlugin.PLUGIN_ID);
		node.put(PREFERENCE_HISTORY_MAX_EXECUTIONS_COUNT, maxEntries + "");
	}

	public IExecutionSession[] getExecutionSessions() {
		return sessions.toArray(new IExecutionSession[sessions.size()]);
	}

	public boolean removeExecutionSession(final IExecutionSession session) {
		final ExecutionSession es = (ExecutionSession) session;
		final boolean result = sessions.remove(es);
		if (result) {
			es.dispose();
		}
		return result;
	}

	public void shutdown() {
		for (ExecutionSession session : sessions) {
			session.dispose();
		}
	}

	private synchronized void execute(final ExecutionSession session,
			Q7Process process) {
		final String launchId = process.getLaunch().getLaunchConfiguration()
				.getName();

		final SessionRunnable sessionRunnable = new SessionRunnable(launchId,
				session, process);

		execute(launchId, session, sessionRunnable);
	}

	public void execute(String launchId, ExecutionSession session, Runnable runnable) {
		sessions.add(session);
		// if something is already running on that SUT, terminate it
		final ExecThread existing = threads.get(launchId);
		if (existing != null) {
			existing.stop(new Status(IStatus.CANCEL, RcpttPlugin.PLUGIN_ID, "Launching execution of " + launchId));
		}

		final ExecThread execThread = new ExecThread(session, runnable,
				launchId);
		threads.put(launchId, execThread);
		execThread.start();
	}

	public static boolean isTestSuiteLauch(ILaunch launch) {
		if (launch != null && launch.getLaunchConfiguration() != null) {
			try {
				return launch.getLaunchConfiguration().getType()
						.getIdentifier().equals(Q7_TEST_SUITE_LAUNCH_ID);
			} catch (CoreException e) {
				Q7LaunchingPlugin.log(e);
			}
		}
		return false;
	}

	/**
	 * A combination of variant choices for all super contexts.
	 */
	private static class ContextVariant {
		/** The names of the chosen context variants. */
		List<String> name;
		/** The map assigning to each super context the chosen variant. */
		Map<IContext, IContext> choices;

		ContextVariant() {
			this.name = new ArrayList<String>();
			this.choices = new HashMap<IContext, IContext>();
		}

		ContextVariant(String name, IContext superContext, IContext childContext) {
			this.name = ImmutableList.of(name);
			this.choices = ImmutableMap.of(superContext, childContext);
		}

		ContextVariant(ContextVariant first, ContextVariant second) {
			this.name = ImmutableList.<String> builder().addAll(first.name).addAll(second.name).build();
			this.choices = ImmutableMap.<IContext, IContext> builder().putAll(first.choices).putAll(second.choices)
					.build();
		}
	}

	public static class ExecutableFactory {
		private final AutLaunch launch;
		private final IWorkspaceFinder finder;
		private final TestCaseDebugger debugger;
		private final Set<ITestSuite> unresolvedItems = new HashSet<ITestSuite>();

		public ExecutableFactory(AutLaunch launch, IWorkspaceFinder finder, TestCaseDebugger debugger) {
			this.launch = launch;
			this.finder = finder;
			this.debugger = debugger;
		}

		private Executable makeContextExecutable(IContext context, ContextVariant variant)
				throws ModelException {
			boolean debug = debugger != null;
			NamedElement element = context.getNamedElement();
			if (element instanceof SuperContext) {
				// Super context was not replaced, so it must be empty.
				return new EmptySuperContextExecutable(launch, context, debug);
			} else if (element instanceof GroupContext) {
				List<IContext> children = new ArrayList<IContext>();
				for (String contextId : ((GroupContext) element).getContextReferences()) {
					children.add(RcpttCore.getInstance().findContext(context, false, contextId, finder));
				}
				List<Executable> plan = new ArrayList<Executable>();
				addContextExecutables(plan, children, variant);
				Executable root = !debug ? new EclContextExecutable(launch, context, debug)
						: new EclDebugContextExecutable(launch, context, debugger);
				return new GroupExecutable(root, plan);
			} else if (element instanceof UnresolvedContext) {
				return new UnresolvedContextExecutable(launch, context, debug);
			} else {
				return !debug ? new EclContextExecutable(launch, context, debug)
						: new EclDebugContextExecutable(launch, context, debugger);
			}
		}

		private void addContextExecutables(Collection<Executable> plan, List<IContext> contexts, ContextVariant variant)
				throws ModelException {
			for (IContext c : contexts) {
				while (variant.choices.containsKey(c)) {
					c = variant.choices.get(c); // Replace super contexts according to choices.
				}
				plan.add(makeContextExecutable(c, variant));
			}
		}

		private Executable makeVerificationExecutable(IVerification verification, ExecutionPhase phase)
				throws ModelException {
			boolean debug = debugger != null;
			if (verification.getNamedElement() instanceof UnresolvedVerification) {
				return new UnresolvedVerificationExecutable(launch, verification, debug, phase);
			} else {
				return !debug ? new EclVerificationExecutable(launch, verification, debug, phase)
						: new EclDebugVerificationExecutable(launch, verification, debugger, phase);
			}
		}

		private void addVerificationExecutables(Collection<Executable> plan, Executable parent,
				IVerification[] verifications, String verificationPhase, ExecutionPhase phase) {
			for (IVerification v : verifications)
				try {
					if (v.getType().supportsPhase(verificationPhase)) {
						plan.add(makeVerificationExecutable(v, phase));
					}
				} catch (ModelException e) {
					Q7LaunchingPlugin.log("Failed to populate verifications for executable: " + parent.getName(), e);
				}
		}

		private List<ContextVariant> getContextVariants(IContext context) {
			try {
				NamedElement element = context.getNamedElement();
				if (element instanceof SuperContext) {
					SuperContext superContext = (SuperContext) element;
					List<ContextVariant> variants = new ArrayList<ContextVariant>();
					for (String contextRef : superContext.getContextReferences()) {
						IContext result = RcpttCore.getInstance().findContext(context, false, contextRef, finder);
						for (ContextVariant subVariant : getContextVariants(result)) {
							if (result != null) {
								ContextVariant selectResultVariant = new ContextVariant(result.getElementName(),
										context, result);
								variants.add(new ContextVariant(selectResultVariant, subVariant));
							}
						}
					}
					if (!variants.isEmpty()) {
						return variants;
					}
				} else if (element instanceof GroupContext) {
					List<IContext> children = new ArrayList<IContext>();
					for (String contextId : ((GroupContext) element).getContextReferences()) {
						children.add(RcpttCore.getInstance().findContext(context, false, contextId, finder));
					}
					return getContextVariants(children);
				}
			} catch (ModelException e) {
				RcpttPlugin.log(e);
			}
			return ImmutableList.of(new ContextVariant());
		}

		private List<ContextVariant> getContextVariants(Collection<IContext> contexts) {
			List<ContextVariant> variants = new ArrayList<ContextVariant>();
			variants.add(new ContextVariant());
			for (IContext context : contexts) {
				List<ContextVariant> ctxVariants = getContextVariants(context);
				List<ContextVariant> oldVariants = variants;
				variants = new ArrayList<ContextVariant>(variants.size() * ctxVariants.size());
				for (ContextVariant oldVariant : oldVariants) {
					for (ContextVariant ctxVariant : ctxVariants) {
						variants.add(new ContextVariant(oldVariant, ctxVariant));
					}
				}
			}
			return variants;
		}

		private List<Executable> makeExecutionPlans(ITestCase test, IContext[] contexts,
				IVerification[] verifications, List<List<String>> supportedVariants) {
			if (contexts == null)
				contexts = new IContext[0];
			if (verifications == null)
				verifications = new IVerification[0];
			List<Executable> result = new ArrayList<Executable>();
			for (ContextVariant variant : getContextVariants(Arrays.asList(contexts))) {
				List<Executable> plan = new ArrayList<Executable>();
				if (supportedVariants != null && !supportedVariants.contains(variant.name)) {
					continue; // Skip variant if it is not required.
				}
				EclScenarioExecutable parent = debugger == null ? new EclScenarioExecutable(launch, test)
						: new EclDebugTestExecutable(launch, test, debugger);
				parent.setVariantName(Lists.newArrayList(variant.name));
				addVerificationExecutables(plan, parent, verifications, VerificationType.PHASE_START,
						ExecutionPhase.START);
				try {
					addContextExecutables(plan, Lists.newArrayList(contexts), variant);
				} catch (ModelException e) {
					Q7LaunchingPlugin.log("Failed to populate contexts for executable: " + parent.getName(), e);
				}
				addVerificationExecutables(plan, parent, verifications, VerificationType.PHASE_RUN, ExecutionPhase.RUN);
				plan.add(parent);
				addVerificationExecutables(plan, parent, verifications, VerificationType.PHASE_FINISH,
						ExecutionPhase.FINISH);
				result.add(plan.size() > 1 ? new GroupExecutable(parent, plan) : parent);
			}
			return result;
		}

		public Executable[] map(final IQ7NamedElement[] elements,
				Map<IQ7NamedElement, List<List<String>>> namedVariants, String capability)
				throws CoreException {
			List<Executable> executables = new ArrayList<Executable>();
			boolean debug = debugger != null;
			for (int i = 0; i < elements.length; i++) {
				final IQ7NamedElement element = elements[i];
				List<List<String>> supportedVariants = null;
				if (namedVariants != null) {
					supportedVariants = namedVariants.get(element);
				}
				try {
					if (element instanceof ITestCase) {
						final ITestCase test = (ITestCase) element;

						IContext[] contexts = RcpttCore.getInstance().getContexts(test, finder, false, capability);

						IVerification[] verifications = RcpttCore.getInstance().getVerifications(test, finder, false);
						assert !Arrays.asList(verifications).contains(null) : "Null verification in "
								+ test.getElementName();
						executables.addAll(makeExecutionPlans(test, contexts, verifications, supportedVariants));
					} else if (element instanceof ITestSuite) {
						ITestSuite suite = (ITestSuite) element;
						ISearchScope scope = Q7SearchCore.getSearchScope(suite);
						ArrayList<IQ7NamedElement> testSuiteItems = new ArrayList<IQ7NamedElement>();
						for (TestSuiteItem item : suite.getItems()) {
							IQ7NamedElement q7Element = Q7SearchCore.getTestSuiteItemElement(item, scope);
							if (q7Element == null) {
								unresolvedItems.add(suite);
								continue;
							}
							testSuiteItems.add(q7Element);
						}

						if (!suite.getTestSuite().isManuallyOrdered())
							SortingUtils.sortNamedElements(testSuiteItems);

						Executable[] children = map(
								testSuiteItems.toArray(new IQ7NamedElement[testSuiteItems.size()]), namedVariants,
								capability);
						executables.add(new TestSuiteExecutable(launch, (ITestSuite) element, children, debug));
					} else if (element instanceof IContext) {
						IContext context = (IContext) element;
						for (ContextVariant variant : getContextVariants(context)) {
							executables.add(makeContextExecutable(context, variant));
						}
					} else {
						// Call prepare context for execution logic

						IVerification verification = (IVerification) element;
						executables.add(debugger == null ? new EclVerificationExecutable(
								launch, verification, debug, ExecutionPhase.AUTO)
								: new EclDebugVerificationExecutable(launch, verification,
										debugger, ExecutionPhase.AUTO));
					}
				} catch (Throwable e) {
					throw new CoreException(RcpttPlugin.createStatus("Failed to process " + element.getName(), e));
				}

			}
			for (int i = 0; i < executables.size(); i++) {
				if (!(executables.get(i) instanceof TestSuiteExecutable)) {
					executables.set(i, new PrepareExecutionWrapper(launch,
							executables.get(i)));
				}
			}
			return executables.toArray(new Executable[executables.size()]);
		}
	}

	private void updateSessionCounters(final ExecutionSession session, IStatus status) {
		if (!status.isOK()) {
			session.oneFailed();
		} else {
			session.oneFinished();
		}
	}

	private final List<ExecutionSession> sessions = new ArrayList<ExecutionSession>();

	private static final String EXPT_ID = Q7LaunchingPlugin.PLUGIN_ID
			+ ".listeners";
	private static final String ATTR_CLASS = "class";

	private static final String PREFERENCE_HISTORY_MAX_EXECUTIONS_COUNT = "history_max_executions_count";
	private static final int HISTORY_MAX_EXECUTIONS_DEFAULT = 30;

	private static class ExecThread {

		// public final String launchId;
		public final ExecutionSession session;
		public final Thread thread;
		public final Job job;
		private boolean complete = false;

		public ExecThread(final ExecutionSession session,
				final Runnable runnable, final String launchId) {
			super();
			this.session = session;
			this.thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						runnable.run();
					} catch (Throwable e) {
						session.stop( RcpttPlugin.createStatus(e));
					} finally {
						complete = true;
					}
				}
			}, "RCPTT Execution-" + launchId);
			this.job = new Job("Executing " + launchId) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					thread.start();
					monitor.beginTask(getName(), -1);
					IStatus result = null;
					try {
						while (!complete) {
							if (monitor.isCanceled()) {
								return result = Status.CANCEL_STATUS;
							}
							Thread.sleep(100);
							monitor.worked(-1);
						}
						result = Status.OK_STATUS;
					} catch (Throwable e) {
						result = RcpttPlugin.createStatus(e);
					} finally {
						stop(result);
					}
					return Status.OK_STATUS;
				}
			};
			// this.launchId = launchId;
		}

		public void start() {
			this.job.schedule();
		}

		public void stop(IStatus result) {
			session.stop(result);
			// thread.interrupt();
			job.cancel();
		}
	}

	// --

	private boolean debuggingActive = false;

	public synchronized boolean isDebuggingActive() {
		return debuggingActive;
	}

	private void updateDebuggingActive(IExecutionSession exclude) {
		for (IExecutionSession es : sessions) {
			if (!es.isRunning() || es == exclude)
				continue;
			for (IExecutable e : es.getExecutables()) {
				if (e.isDebug()) {
					debuggingActive = true;
					return;
				}
			}
		}

		debuggingActive = false;
	}

	public synchronized boolean isElementUnderDebugging(IQ7Element element) {
		for (ExecutionSession es : sessions) {
			if (!es.isRunning())
				continue;
			for (IExecutable e : es.getExecutables()) {
				if (e.isDebug()
						&& e.getActualElement().getResource()
								.equals(element.getResource())) {
					return true;
				}
			}
		}

		return false;
	}

	// --

	private synchronized void fireStarted(final IExecutionSession session) {
		updateDebuggingActive(null);
		for (final ILaunchListener listener : listeners) {
			listener.started(session);
		}
	}

	private synchronized void fireLaunchStatusChanged(
			final IExecutable... executable) {
		for (final ILaunchListener listener : listeners) {
			listener.launchStatusChanged(executable);
		}
	}

	private synchronized void fireFinished(final IExecutionSession session) {
		updateDebuggingActive(session);
		for (final ILaunchListener listener : listeners) {
			listener.finished();
		}
	}

	public synchronized boolean isDebuggingActive(AutLaunch aut) {
		for (ExecutionSession s : sessions) {
			if (s.isDebugging(aut)) {
				return true;
			}
		}
		return false;
	}
}
