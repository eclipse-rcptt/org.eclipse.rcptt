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
package org.eclipse.rcptt.reporting.core;

import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.ecl.core.BoxedValue;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.ecl.internal.core.ProcessStatusConverter;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.ReportingFactory;
import org.eclipse.rcptt.sherlock.core.INodeBuilder;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.LoggingCategory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.LoggingData;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Snaphot;
import org.eclipse.rcptt.sherlock.core.reporting.EventProviderManager;
import org.eclipse.rcptt.sherlock.core.reporting.Procedure1;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.info.InfoFactory;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;

public class ReportHelper {

	public static Q7Info getInfo(Node node) {
		synchronized (node) {
			Q7Info value = getInfoOnly(node);
			if (value == null) {
				value = ReportingFactory.eINSTANCE.createQ7Info();
				node.getProperties().put(IQ7ReportConstants.ROOT, value);
			}
			return (Q7Info) value;
		}
	}

	public static void setInfo(INodeBuilder node, Q7Info info) {
		assert info.getType() != null;
		assert info.getResult() == null;
		node.setProperty(IQ7ReportConstants.ROOT, info);
	}

	{
		// Prevents class loader lock in synchronized context. See http://jira4.xored.com/browse/QS-3201#comment-22683
		InfoFactory.eINSTANCE.createQ7WaitInfoRoot();
	}

	public static Q7WaitInfoRoot getWaitInfo(Node node, boolean create) {
		synchronized (node) {
			Q7WaitInfoRoot value = (Q7WaitInfoRoot) node.getProperties().get(
					IQ7ReportConstants.ROOT_WAIT);
			if (value != null) {
				return (Q7WaitInfoRoot) value;
			}
			if (value == null && create) {
				value = InfoFactory.eINSTANCE.createQ7WaitInfoRoot();
				value.setStartTime(System.currentTimeMillis());
				node.getProperties().put(IQ7ReportConstants.ROOT_WAIT, value);
			}
			return (Q7WaitInfoRoot) value;
		}
	}

	public static void putProperties(INodeBuilder node, Map<String, ? extends EObject> properties) {
		for (Map.Entry<String, ? extends EObject> entry : properties.entrySet()) {
			node.setProperty(entry.getKey(), entry.getValue());
		}
	}

	public static void appendLog(Node node, LoggingCategory category, String log) {
		String logKey = getLogCategoryKey(category);
		EMap<String, EObject> properties = node.getProperties();
		LoggingData data = (LoggingData) properties.get(logKey);
		if (data == null)
			properties.put(logKey, data = ReportFactory.eINSTANCE.createLoggingData());
		StringBuilder sb = new StringBuilder(data.getText());
		sb.append(log);
		sb.append("\n");
		data.setText(sb.toString());
	}

	private static String getLogCategoryKey(LoggingCategory category) {
		return "log_" + category.name();
	}

	public static Q7Info getInfoOnly(Node node) {
		synchronized (node) {
			EObject value = node.getProperties().get(IQ7ReportConstants.ROOT);
			if (value instanceof Q7Info) {
				return (Q7Info) value;
			}
			return null;
		}
	}

	public static Q7Info createInfo() {
		return ReportingFactory.eINSTANCE.createQ7Info();
	}

	public static void startWaitInfo(INodeBuilder node, final String kind, final String className) {
		Q7WaitUtils.startInfo(kind, className, getWaitInfo(node));
	}

	public static void finishWaitInfo(INodeBuilder node, final String kind, final String className) {
		Q7WaitUtils.finishInfo(kind, className, getWaitInfo(node));
	}

	public static void updateWaitInfo(INodeBuilder node, final String kind, final String className) {
		Q7WaitUtils.updateInfo(kind, className, getWaitInfo(node));
	}

	/** Leaks Q7WaitInfoRoot reference in unsynchronized context */
	public static Q7WaitInfoRoot getWaitInfo(INodeBuilder node) {

		final Q7WaitInfoRoot waitInfo[] = new Q7WaitInfoRoot[1];
		node.update(new Procedure1<Node>() {
			@Override
			public void apply(Node node) {
				// Leaking reference to unsynchronized context
				waitInfo[0] = getWaitInfo(node, true);
			}
		});
		return waitInfo[0]; // Might be null at this point if no report is active
	}
	
	public static void setResult(INodeBuilder node, final IStatus status) {
		setResult(node, ProcessStatusConverter.toProcessStatus(status));
	}

	public static void setResult(INodeBuilder node, ProcessStatus status) {
		if (status == null)
			throw new NullPointerException("Status can't be null");
		if (node == null)
			throw new NullPointerException("Node can't be null");

		final ProcessStatus status2 = EcoreUtil.copy(status);
		node.update(new Procedure1<Node>() {
			@Override
			public void apply(Node arg) {
				Q7Info info = getInfo(arg);
				if (info.getResult() != null)
					throw new IllegalStateException("Result is already set for " + arg.getName());
				info.setResult(status2);
			}
		});
	}

	public static void addSnapshotWithData(INodeBuilder node, EObject data) {
		Snaphot snapshot = ReportFactory.eINSTANCE.createSnaphot();
		snapshot.setData(Objects.requireNonNull(data));
		node.addSnapshot(snapshot);
	}

	public static void takeSnapshot(INodeBuilder node) {
		EventProviderManager.getInstance().takeSnapshot(node);
	}

	public static boolean isIterable(INodeBuilder node) {
		EObject eIsIterable = node.getProperty(IQ7ReportConstants.ITERABLE);
		if (eIsIterable == null) {
			return false;
		}
		assert eIsIterable instanceof BoxedValue;
		Object oIsIterable = BoxedValues.unbox((BoxedValue) eIsIterable);

		assert oIsIterable instanceof Boolean;
		return ((Boolean) oIsIterable).booleanValue();
	}

	public static void markIterable(INodeBuilder node) {
		node.setProperty(IQ7ReportConstants.ITERABLE, BoxedValues.box(true));
	}

}
