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
package org.eclipse.rcptt.internal.launching.aut;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.pde.internal.launching.IPDEConstants;
import org.eclipse.rcptt.ecl.client.tcp.EclTcpClientManager;
import org.eclipse.rcptt.ecl.runtime.ISession;
import org.eclipse.rcptt.internal.core.WorkspaceMonitor;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.internal.launching.aut.BaseAutLaunch.Context;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutLaunchState;
import org.eclipse.rcptt.launching.AutListener;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.ILaunchExecutor;
import org.eclipse.rcptt.launching.IQ7Launch;

@SuppressWarnings("restriction")
public enum BaseAutManager implements AutManager, ILaunchConfigurationListener,
		ILaunchesListener, ILaunchesListener2, IDebugEventSetListener {

	INSTANCE;

	private BaseAutManager() {
		executors = readExecutors();
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		manager.addLaunchConfigurationListener(this);
		manager.addLaunchListener(this);
		DebugPlugin debugPlugin = DebugPlugin.getDefault();
		debugPlugin.addDebugEventListener(this);

		addListener(new OutputCaptureListener());
		for (Entry<String, ILaunchExecutor> entry : executors.entrySet()) {
			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(entry.getKey());
			try {
				ILaunchConfiguration[] configs = manager
						.getLaunchConfigurations(type);
				for (ILaunchConfiguration config : configs) {
					addAut(config, entry.getValue());
				}
			} catch (CoreException e) {
				Q7LaunchingPlugin.log("Couldn't load base AUTs", e);
			}
		}
		launchesAdded(manager.getLaunches());
	}

	public List<Aut> getAuts() {
		return auts.getAll();
	}

	public List<AutLaunch> getLaunches() {
		return launches.getAll();
	}

	public BaseAut getByName(String name) {
		return auts.getByName(name);
	}

	public AutLaunch getById(String id) {
		return launches.getById(id);
	}

	public BaseAut getByLaunch(ILaunchConfiguration config) {
		BaseAut result = auts.getByLaunch(config);
		if (result == null) {
			launchConfigurationAdded(config);
			return auts.getByLaunch(config);
		}
		return result;
	}

	public BaseAutLaunch getByLaunch(ILaunch launch) {
		return launches.getByLaunch(launch);
	}

	public List<AutLaunch> getLaunches(BaseAut aut) {
		return launches.getByAut(aut);
	}

	public BaseAutLaunch getCurrentLaunch(BaseAut aut) {
		return launches.getCurrent(aut);
	}

	public void launchConfigurationAdded(ILaunchConfiguration config) {
		ILaunchExecutor executor = getExecutor(config);
		if (executor != null) {
			addAut(config, executor);
		}
	}

	public void launchConfigurationRemoved(ILaunchConfiguration config) {
		BaseAut aut = auts.removeByLaunch(config);
		if (aut != null) {
			for (AutListener listener : listeners) {
				listener.autRemoved(aut);
			}
			// remove corresponding launches
			List<AutLaunch> list = launches.getByAut(aut);
			for (AutLaunch autLaunch : list) {
				if (this.launches.remove((BaseAutLaunch) autLaunch)) {
					for (AutListener listener : listeners) {
						listener.launchRemoved(autLaunch);
					}
					autLaunch.shutdown();
				}
			}
		}
	}

	public void launchConfigurationChanged(ILaunchConfiguration config) {
		BaseAut aut = auts.getByLaunch(config);
		if (aut == null) {
			return;
		}
		for (AutListener listener : listeners) {
			listener.autChanged(aut);
		}
	}

	public void launchesAdded(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			BaseAut aut = auts.getByLaunch(launch);
			if (aut != null) {
				if (!handleRestart(launch)) {
					Context context = new Context() {
						@Override
						public ISession connect(String host, int port) throws IOException {
							return EclTcpClientManager.Instance.startClientSession(InetAddress.getByName(host), port);
						}
					};
					BaseAutLaunch autLaunch = new BaseAutLaunch(launch, aut, context);
					this.launches.add(autLaunch);
					for (AutListener listener : listeners) {
						listener.launchAdded(autLaunch);
					}
				}
			}
		}
	}

	private boolean handleRestart(ILaunch launch) {
		String id = getRestartId(launch.getLaunchConfiguration());
		if (id != null) {
			BaseAutLaunch aut = launches.getById(id);
			if (aut == null) {
				Q7LaunchingPlugin.log("No AUT launch found for launch: "
						+ launch + " with id = " + id);
			} else {
				ILaunch oldLaunch = aut.setLaunch(launch);
				launches.replaceLaunch(aut, launch, oldLaunch);
			}
			return true;
		}
		return false;
	}

	public void launchesRemoved(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			BaseAut aut = auts.getByLaunch(launch);
			if (aut != null) {
				BaseAutLaunch autLaunch = this.launches.getByLaunch(launch);
				if (autLaunch != null) {
					if (autLaunch.getState() == AutLaunchState.RESTART) {
						// do not remove launch if we restart it
						continue;
					}
					autLaunch.terminated(0);
					if (this.launches.remove(autLaunch)) {
						for (AutListener listener : listeners) {
							listener.launchRemoved(autLaunch);
						}
					}
				}
			}
		}
	}

	public void launchesTerminated(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			BaseAutLaunch aut = this.launches.getByLaunch(launch);
			if (aut != null) {
				aut.terminated(Status.OK_STATUS);
				if (this.launches.remove(aut)) {
					for (AutListener listener : listeners) {
						listener.launchRemoved(aut);
					}
				}

			}
		}
	}

	public void launchesChanged(ILaunch[] launches) {
		// ignore changes
	}

	public void addListener(AutListener listener) {
		listeners.addIfAbsent(listener);
	}

	public void removeListener(AutListener listener) {
		listeners.remove(listener);
	}

	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			Object source = event.getSource();
			if (source instanceof IProcess
					&& event.getKind() == DebugEvent.TERMINATE) {
				IProcess process = (IProcess) source;
				ILaunch launch = process.getLaunch();
				if (launch != null) {
					try {
						BaseAutLaunch aut = launches.getByLaunch(launch);
						if (aut != null) {
							aut.terminateProcess(process.getExitValue());
						}
					} catch (DebugException e) {
						Q7LaunchingPlugin.log(e.getStatus());
					}
				}
			}
		}
	}

	private ILaunchExecutor getExecutor(ILaunchConfiguration config) {
		try {
			String id = config.getType().getIdentifier();
			return executors.get(id);
		} catch (CoreException e) {
			// ignore problem configuration
			return null;
		}
	}

	private void addAut(ILaunchConfiguration config, ILaunchExecutor executor) {
		IFile file = config.getFile();
		if (file != null) {
			// Stored configuration. Ignore hidden configurations
			if (WorkspaceMonitor.isIgnored(file)) {
				return;
			}
		}
		BaseAut aut = new BaseAut(config, executor);
		auts.add(aut);
		for (AutListener listener : listeners) {
			listener.autAdded(aut);
		}
	}

	private static String getRestartId(ILaunchConfiguration config) {
		if (config != null) {
			try {
				if (config.getAttribute(IPDEConstants.RESTART, false)) {
					String id = config.getAttribute(IQ7Launch.ATTR_AUT_ID, "");
					if (!id.isEmpty()) {
						return id;
					}
				}
			} catch (CoreException e) {
				// continue processing
			}
		}
		return null;
	}

	private static Map<String, ILaunchExecutor> readExecutors() {
		Map<String, ILaunchExecutor> executors = new HashMap<String, ILaunchExecutor>();
		IConfigurationElement[] configs = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTPT_EXECUTORS);
		for (IConfigurationElement config : configs) {
			try {
				ILaunchExecutor executor = (ILaunchExecutor) config
						.createExecutableExtension(ATTR_CLASS);
				String type = config.getAttribute(ATTR_TYPE);
				executors.put(type, executor);
			} catch (Exception e) {
				StringBuilder builder = new StringBuilder();
				builder.append("Problem while loading '");
				builder.append(EXTPT_EXECUTORS);
				builder.append("' extension");
				try {
					String plugin = config.getNamespaceIdentifier();
					builder.append(" from the '");
					builder.append(plugin);
					builder.append("' plugin");
				} catch (Exception exc) {
					// ignore exc
				}
				builder.append(": ");
				builder.append(e.getMessage());
				Q7LaunchingPlugin.log(builder.toString(), e);
			}
		}
		return executors;
	}

	public List<AutLaunch> storeAUTLaunches(ILaunchConfiguration configuration) {
		BaseAut aut = getByLaunch(configuration);
		if (aut != null) {
			return aut.getLaunches();
		}
		return null;
	}

	public void updateAUTLaunches(ILaunchConfiguration newConfig,
			List<AutLaunch> launches) {
		BaseAut launch = getByLaunch(newConfig);
		if (launch != null) {
			for (AutLaunch autLaunch : launches) {
				if (autLaunch instanceof BaseAutLaunch
						&& autLaunch instanceof IBaseAutLaunchRetarget) {
					((IBaseAutLaunchRetarget) autLaunch).retarget(launch);
					this.launches.remove((BaseAutLaunch) autLaunch);
					this.launches.add((BaseAutLaunch) autLaunch);
				}
			}
		}
	}

	private Map<String, ILaunchExecutor> executors;

	private AutStorage auts = new AutStorage();
	private AutLaunchStorage launches = new AutLaunchStorage();
	private CopyOnWriteArrayList<AutListener> listeners = new CopyOnWriteArrayList<AutListener>();

	private static final String ATTR_CLASS = "class";
	private static final String ATTR_TYPE = "type";

	private static final String EXTPT_EXECUTORS = Q7LaunchingPlugin.PLUGIN_ID
			+ ".executors";

	public void handleRestart(BaseAutLaunch aut, ILaunch oldLaunch,
			ILaunch launch) {
		launches.replaceLaunch(aut, launch, oldLaunch);
	}
}
