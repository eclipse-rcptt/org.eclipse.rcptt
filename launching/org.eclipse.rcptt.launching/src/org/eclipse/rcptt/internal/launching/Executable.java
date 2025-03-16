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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.core.ecl.core.model.ExecutionPhase;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.aut.ConsoleOutputListener;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public abstract class Executable implements IExecutable {
	protected static final Executable[] EMPTY = new Executable[0];
	private static final IStatus cancelledForPreviousFailures = new Status(IStatus.CANCEL, PLUGIN_ID,
			"Execution was canceled due to previous failures");
	protected Listener.Composite listeners = new Listener.Composite();
	private final ConsoleOutputListener consoleListener = new ConsoleOutputListener();

	private final boolean debug;
	private final boolean collectLog;
	private volatile IStatus result;
	private State state = State.WAITING;
	private long time;

	private final ExecutionPhase phase;

	public abstract Executable[] getChildren();

	public interface Listener {
		void onStatusChange(Executable executable);

		void updateSessionCounters(Executable executable, IStatus status);

		static class Adapter implements Listener {
			@Override
			public void onStatusChange(Executable executable) {
			}

			@Override
			public void updateSessionCounters(Executable executable, IStatus status) {
			}
		}

		static class Composite implements Listener {
			private List<Listener> listeners = Collections.synchronizedList(new ArrayList<Listener>(2));

			public void add(Listener listener) {
				listeners.add(listener);
			}

			public void remove(Listener listener) {
				listeners.remove(listener);
			}

			private Listener[] copy() {
				synchronized (listeners) {
					return Iterables.toArray(listeners, Listener.class);
				}
			}

			@Override
			public void onStatusChange(Executable executable) {
				for (Listener listener : copy()) {
					listener.onStatusChange(executable);
				}
			}

			@Override
			public void updateSessionCounters(Executable executable, IStatus status) {
				for (Listener listener : copy()) {
					listener.updateSessionCounters(executable, status);
				}
			}
		}
	}

	private void setResult(IStatus result) {
		boolean changed = false;
		synchronized (this) {
			if (this.result == null) {
				this.result = result;
				changed = true;
				state = State.COMPLETED;
			}
		}
		if (changed)
			listeners.onStatusChange(this);
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	final void executeAndRememberResult() throws InterruptedException {
		if (state != State.WAITING)
			throw new IllegalStateException("Can't start in " + state + " state");
		if (collectLog)
			consoleListener.startLogging(getAut());
		state = State.RUNNING;
		long startTime = System.currentTimeMillis();
		IStatus localResult = null;
		try {
			startLaunching();
			listeners.onStatusChange(this);
			localResult = execute();
			Preconditions.checkNotNull(localResult);
			for (final Executable child : getChildren()) {
				if (result != null)
					localResult = result;
				if (!localResult.isOK()) {
					child.cancel(cancelledForPreviousFailures);
					continue;
				}
				child.addListener(listeners);
				try {
					child.executeAndRememberResult();
					IStatus rv = handleChildResult(child.getResultStatus());
					if (!rv.isOK()) {
						localResult = rv;
					}
				} finally {
					child.removeListener(listeners);
				}
			}
		} catch (InterruptedException e) {
			localResult = RcpttPlugin.createStatus("Execution was unexpectedly terminated", e);
			throw e;
		} catch (Throwable e) {
			localResult = internalFailure(e);
		} finally {
			time = System.currentTimeMillis() - startTime;
			try {
				Preconditions.checkNotNull(localResult);
				try {
					localResult = postExecute(localResult);
				} catch (Throwable e) {
					localResult = internalFailure(e);
				}
			} finally {
				setResult(localResult);

			}
			consoleListener.stopLogging();
		}
	}
	
	protected IStatus internalFailure(Throwable e) {
		return new Status(IStatus.ERROR, getClass(), IProcess.INTERNAL_AUT_FAILURE, "Internal failure", e);
	}

	public void cancel(IStatus status) {
		setResult(status);

		for (final Executable child : getChildren()) {
			child.addListener(listeners);
			try {
				child.cancel(status);
			} finally {
				child.removeListener(listeners);
			}
		}
	}

	/** @return OK status if execution should continue, resulting parent status otherwise */
	protected IStatus handleChildResult(IStatus resultStatus) {
		return resultStatus;
	}

	/** Should only be called from org.eclipse.rcptt.internal.launching.Executable.executeAndRememberResult() */
	protected abstract IStatus execute() throws InterruptedException;

	protected Executable(boolean debug) {
		this(debug, ExecutionPhase.AUTO, false);
	}

	protected Executable(boolean debug, ExecutionPhase phase, boolean collectLog) {
		this.debug = debug;
		this.phase = phase;
		this.collectLog = collectLog;
	}

	public boolean isDebug() {
		return debug;
	}

	public void dispose() {
		cancel(new Status(IStatus.ERROR, PLUGIN_ID, "Disposed"));
		Executable[] childs = getChildren();
		if (childs != null) {
			for (Executable child : childs) {
				child.dispose();
			}
		}
	}

	/**
	 * Should only be called from org.eclipse.rcptt.internal.launching.Executable.postExecuteAndRememberResult()
	 * @param result
	 */
	protected IStatus postExecute(IStatus result) {
		return result;
	}

	public Report getResultReport() {
		return null;
	}

	public String getId() {
		return null;
	}


	public ExecutionPhase getPhase() {
		return phase;
	}

	public static State max(State a, State b) {
		return a.ordinal() >= b.ordinal() ? a : b;
	}

	public void startLaunching() {
	}

	@Override
	public final State getStatus() {
		State status = state;
		for (IExecutable child : getChildren()) {
			status = max(status, child.getStatus());
		}
		return status;
	}

	@Override
	public synchronized final IStatus getResultStatus() {
		IStatus localResult = this.result;
		Preconditions.checkState(localResult != null || state != State.COMPLETED,
				"Result should always be set in COMPLETED state");
		if (localResult != null)
			return localResult;
		return Status.OK_STATUS;
	}

	@Override
	public final long getTime() {
		return time;
	}

	public String getLog() {
		Preconditions.checkState(collectLog);
		return consoleListener.getLog();
	}
}
