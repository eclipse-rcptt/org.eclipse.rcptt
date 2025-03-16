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
package org.eclipse.rcptt.ecl.internal.core;

import static org.eclipse.rcptt.ecl.internal.core.CorePlugin.err;
import static org.eclipse.rcptt.ecl.internal.core.CorePlugin.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.rcptt.ecl.core.Binding;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.CommandStack;
import org.eclipse.rcptt.ecl.core.ProcInstance;
import org.eclipse.rcptt.ecl.core.SessionListenerManager;
import org.eclipse.rcptt.ecl.runtime.CoreUtils;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IPipe;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.ecl.runtime.ISession;

public abstract class AbstractSession implements ISession {
	private Map<String, Object> properties = null;

	protected abstract void doExecute(final Command scriptlet, final ICommandService svc,
			final List<Object> inputContent, final Process process);

	protected abstract CommandStack getStack();

	public abstract AbstractRootSession getRoot();

	public IProcess execute(Command command) throws CoreException {
		return execute(command, null, null);
	}

	// public IProcess execute(final Command scriptlet, IPipe in, IPipe out)
	// throws CoreException
	public IProcess execute(Command scriptlet, IPipe in, IPipe out) throws CoreException {
		if (isClosed()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		final ICommandService svc = scriptlet instanceof ProcInstance ? new ProcInstanceService()
				: CorePlugin.getScriptletManager().getScriptletService(scriptlet);
		final IPipe tinput = in == null ? createPipe().close(Status.OK_STATUS) : in;
		final IPipe output = out == null ? createPipe() : out;
		final List<Object> inputContent = CoreUtils.readPipeContent(tinput);
		final IPipe input = createPipe();
		for (Object o : inputContent)
			input.write(o);
		input.close(Status.OK_STATUS);

		CommandSession session = new CommandSession(getRoot(), new CommandStack(scriptlet, getStack()), this);

		final Process process = new Process(session, input, output);
		doExecute(scriptlet, svc, inputContent, process);
		return process;
	}

	// protected void internalDoExecute(final Command scriptlet,
	// final ICommandService svc, final List<Object> inputContent,
	// final Process process)
	protected void internalDoExecute(Command scriptlet, final ICommandService svc, final List<Object> inputContent,
			final Process process) {
		IStatus s = null;
		CommandStack stack = ((AbstractSession) process.getSession()).getStack();
		try {
			resolveBindings(scriptlet, inputContent);
			setupInputFeature(scriptlet, inputContent);
			checkParams(scriptlet);

			CommandStack.fireEnter(stack);
			SessionListenerManager.beginCommand(scriptlet);
			s = svc.service(scriptlet, process);
		} catch (CoreException e) {
			s = e.getStatus();
		} catch (InterruptedException ie) {
			log(s = err(ie));
		} catch (Throwable t) {
			log(s = err(t));
		} finally {
			try {
				SessionListenerManager.endCommand(scriptlet, s);
			} catch (Throwable e) {
				log(s = err(e));
			}
			CommandStack.fireExit(stack);
			try {
				process.setStatus(s);
			} catch (CoreException ioe) {
				s = ioe.getStatus();
				try {
					process.setStatus(s);
				} catch (CoreException e) {
					// do nothing, just log
					CorePlugin.log(e.getStatus());
				}
			}
		}
	}

	protected void setupInputFeature(Command scriptlet, List<Object> inputContent) throws CoreException {
		EStructuralFeature inputFeature = null;
		for (EStructuralFeature feature : CoreUtils.getFeatures(scriptlet.eClass())) {
			if (feature.getEAnnotation(CoreUtils.INPUT_ANN) != null && !scriptlet.eIsSet(feature)) {
				if (inputFeature == null) {
					inputFeature = feature;
				} else {
					IStatus status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
							"Command has more than one input param");
					throw new CoreException(status);
				}
			}
		}
		if (inputFeature != null) {
			CoreUtils.featureSafeSet(scriptlet, inputFeature, inputContent);
		}
	}

	protected void resolveBindings(Command scriptlet, List<Object> inputContent)
			throws CoreException, InterruptedException {
		for (Binding binding : scriptlet.getBindings()) {
			EStructuralFeature feature = binding.getFeature();
			Command command = binding.getCommand();
			IPipe in = createPipe();
			for (Object o : inputContent)
				in.write(o);
			in.close(Status.OK_STATUS);
			IPipe out = createPipe();
			IProcess process = execute(command, in, out);
			IStatus status = process.waitFor();
			if (!status.isOK())
				throw new CoreException(status);
			CoreUtils.featureSafeSet(scriptlet, feature, CoreUtils.readPipeContent(out));
		}
	}

	protected void checkParams(Command scriptlet) throws CoreException {
		for (EStructuralFeature feature : scriptlet.eClass().getEStructuralFeatures()) {
			CoreUtils.checkBounds(feature, scriptlet.eGet(feature));
		}
	}

	public IPipe createPipe() {
		return new Pipe();
	}

	public synchronized void putProperty(String key, Object value) {
		if (value == null) {
			if (properties != null) {
				properties.remove(key);
			}
		} else {
			if (properties == null) {
				properties = new HashMap<String, Object>();
			}
			properties.put(key, value);
		}
	}

	public synchronized Object getProperty(String key) {
		if (properties != null) {
			return properties.get(key);
		}
		return null;
	}

}
