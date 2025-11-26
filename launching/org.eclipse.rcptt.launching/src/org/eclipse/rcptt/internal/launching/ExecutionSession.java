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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.DebugException;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.IExecutionSession;
import org.eclipse.rcptt.reporting.util.ReportUtils;
import org.eclipse.rcptt.sherlock.core.streams.SherlockReportSession;

import com.google.common.base.Preconditions;

public class ExecutionSession implements IExecutionSession {

	private final String name;
	private final Executable[] executables;
	private Date startTime;
	private final ListenerList<IExecutionSessionListener> listeners = new ListenerList<>();

	private Executable executable;

	private Date endTime;
	private final SherlockReportSession reportSession;
	private final Q7TestLaunch launch;
	private final int testCasesCount;
	private final AutLaunch aut;
	private volatile IStatus result = null;

	public ExecutionSession(String name, Executable[] executables, AutLaunch aut, Q7TestLaunch launch) {
		this.name = name;
		this.executables = executables;
		this.aut = aut;
		this.launch = launch;
		String reportID = ReportUtils.getID(name);
		File reportRoot = Q7LaunchingPlugin.getExecutionSessionRoot(reportID,
				this);
		reportSession = new SherlockReportSession(reportRoot);
		Executable[] testCases = getTestCases();
		for (Executable executable : testCases) {
			if (executable instanceof PrepareExecutionWrapper) {
				((PrepareExecutionWrapper) executable)
						.setReportSession(reportSession);
			}
		}
		testCasesCount = testCases.length;
	}

	@Override
	public Q7TestLaunch getLaunch() {
		return this.launch;
	}

	public SherlockReportSession getReportSession() {
		return reportSession;
	}

	@Override
	public Executable[] getExecutables() {
		return executables;
	}

	@Override
	public Executable[] getTestCases() {
		List<Executable> result = internalGetTestCases(executables);
		return result.toArray(new Executable[result.size()]);
	}

	private List<Executable> internalGetTestCases(Executable[] parents) {
		List<Executable> result = new ArrayList<Executable>();
		for (Executable executable : parents) {
			if (executable instanceof PrepareExecutionWrapper) {
				result.add(executable);
			} else if (executable instanceof TestSuiteExecutable) {
				result.addAll(internalGetTestCases(((TestSuiteExecutable) executable)
						.getChildren()));
			}
		}
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addListener(IExecutionSessionListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(IExecutionSessionListener listener) {
		this.listeners.remove(listener);
	}

	public synchronized void setActive(Executable executable) {
		this.executable = executable;
	}

	public synchronized Executable getActive() {
		return executable;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Override
	public Date getStartTime() {
		return startTime;
	}

	@Override
	public boolean isRunning() {
		return result == null;
	}

	public void stop(IStatus result) {
		// new Status(IStatus.CANCEL, PLUGIN_ID, "Execution is stopped")
		Preconditions.checkNotNull(result);
		synchronized (this) {
			if (this.result == null)
				this.result = result;
			result = this.result;
		}
		for (Executable ex : executables) {
			ex.cancel(result);
		}
		for (IExecutionSessionListener o : listeners) {
			o.executionFinished();
		}
		if (launch != null) {
			launch.setSession(null);
			try {
				launch.terminate();
			} catch (DebugException e) {
				Q7LaunchingPlugin.log(e);
			}
		}
		for (IExecutionSessionListener o : listeners) {
			o.statisticsUpdate();
		}
	}

	@Override
	public int getFailedCount() {
		return getScenariosByStatus(executables, IStatus.ERROR, false);
	}
	
	private int getScenariosByStatus(IExecutable[] executables, int status, boolean matchOk) {
		int count = 0;
		for (IExecutable executable : executables) {
			if (executable instanceof PrepareExecutionWrapper || executable instanceof RetryExecutable) {
				if (
					executable.getStatus() == IExecutable.State.COMPLETED &&
					(
						executable.getResultStatus().matches(status) ||
						(matchOk && executable.getResultStatus().isOK())
					)
				) {
					count++;
				}
			} else if (executable instanceof TestSuiteExecutable) {
				count += getScenariosByStatus(executable.getChildren(), status, matchOk);
			}
		}
		return count;
	}

	@Override
	public int getStoppedCount() {
		return getScenariosByStatus(executables, IStatus.CANCEL, false);
	}

	@Override
	public int getFinishedCount() {
		return getScenariosByStatus(executables, IStatus.ERROR | IStatus.INFO, true);
	}

	@Override
	public int getTotalCount() {
		return testCasesCount;
	}

	public void resetCounters() {
		for (IExecutionSessionListener o : listeners) {
			o.statisticsUpdate();
		}
	}

	public void oneFailed() {
		for (IExecutionSessionListener o : listeners) {
			o.statisticsUpdate();
		}
	}

	public void oneFinished() {
		for (IExecutionSessionListener o : listeners) {
			o.statisticsUpdate();
		}
	}

	@Override
	public long getTotalTime() {
		long total = 0;
		for (IExecutable executable : executables) {
			total += executable.getTime();
		}
		return total;
	}

	public void dispose() {
		if (launch != null) {
			launch.setSession(null);
		}
		if (executable != null) {
			executable.dispose();
		}
		if (executables != null) {
			for (Executable executable : executables) {
				executable.dispose();
			}
		}
		if (reportSession != null) {
			reportSession.dispose();
		}
	}

	public void setEndTime(Date date) {
		endTime = date;
	}

	@Override
	public Date getEndTime() {
		return endTime;
	}

	public boolean isDebugging(AutLaunch aut) {
		return isRunning() && aut.equals(this.aut);
	}

	@Override
	public IStatus getResultStatus() {
		return result;
	}
}
