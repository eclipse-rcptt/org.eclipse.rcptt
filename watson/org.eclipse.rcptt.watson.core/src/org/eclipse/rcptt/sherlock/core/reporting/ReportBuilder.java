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
package org.eclipse.rcptt.sherlock.core.reporting;

import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.sherlock.core.INodeBuilder;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Event;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.EventSource;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.LoggingCategory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.LoggingData;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportBuilderStore;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Snaphot;

/**
 * Build a complex report.
 */
public class ReportBuilder implements IReportBuilder {
	private static final String NODE_INDEX_PROPERTY = "rcptt.watson.node.index";
	private static final String NODE_LASTSTARTTIME_PROPERTY = "rcptt.watson.node.last-start-time";

	private final Report report;
	private NodeBuilder currentNode;
	
	//Provides synchronized access to report nodes
	private class NodeBuilder implements INodeBuilder {
		private final Node node;
		private NodeBuilder parent;
		private int currentChildIndex = 0;

		private NodeBuilder(NodeBuilder parent, Node node){
			if (parent == null) {
				EObject container = node.eContainer();
				if (container instanceof Node) {
					parent = new NodeBuilder(null, (Node) container);
				}
			}
			this.parent = parent;
			this.node = node;
		}

		/**
		 * Will add new node to current one and go one level down.
		 */
		@Override
		public INodeBuilder beginTask(String name) {
			NodeBuilder childNode = findChildNode(name);
			if (childNode != null) {
				return childNode;
			}
			Node child = ReportFactory.eINSTANCE.createNode();
			child.setName(name);
			child.setStartTime(getTime());
			setNodeIndex(child, currentChildIndex);
			setNodeLastStartTime(child, child.getStartTime());
			synchronized (report) {
				node.getChildren().add(getChildPosition(name), child);
				currentNode = new NodeBuilder(this, child);
			}
			currentChildIndex++;
			return currentNode;
		}
		
		private NodeBuilder findChildNode(String name) {
			for (Node child : node.getChildren()) {
				if (name.equals(child.getName())
						&& currentChildIndex == getNodeIndex(child)) {
					setNodeLastStartTime(child, getTime());
					synchronized (report) {
						currentNode = new NodeBuilder(this, child);
					}
					currentChildIndex++;
					return currentNode;
				}
			}
			return null;
		}

		/**
		 * Find existing subnode by name and index and make it active.
		 */
		public INodeBuilder appendTask(String name) {
			for (Node child : node.getChildren()) {
				if (name.equals(child.getName())) {
					setNodeLastStartTime(child, getTime());
					synchronized (report) {
						currentNode = new NodeBuilder(this, child);
					}
					return currentNode;
				}
			}
			return beginTask(name);
		}

		/**
		 * Will go one level up.
		 */
		@Override
		public void endTask() {
			synchronized (report) {
				node.setEndTime(getTime());
				long duration = node.getEndTime() - getNodeLastStartTime(node);
				node.setDuration(node.getDuration() + duration);
				if (parent == null)
					throw new IllegalStateException("Root report node can't be closed.");
				currentNode = parent;
			}
		}
		
		@Override
		public void createEvent(Event event) {
			synchronized (report) {
				Event childEvent = findChildEvent(event);
				if (childEvent != null) {
					childEvent.setCount(childEvent.getCount() + 1);
					return;
				}
				Event copy = EcoreUtil.copy(event);
				copy.setTime(getTime());
				node.getEvents().add(copy);
			}
		}

		private Event findChildEvent(Event event) {
			for (Event childEvent : node.getEvents()) {
				if (EcoreUtil.equals(event.getData(), childEvent.getData())) {
					return childEvent;
				}
			}
			return null;
		}

		/*
		 * Add or append existing log entry into current node.
		 */
		@Override
		public void appendLog(LoggingCategory category, String text) {
			String log_key = getLogCategoryKey(category);
			synchronized (report) {
				EMap<String, EObject> properties = node.getProperties();
				LoggingData data = (LoggingData) properties.get(log_key);
				if (data == null)
					properties.put(log_key, data = ReportFactory.eINSTANCE.createLoggingData());
				StringBuilder sb = new StringBuilder(data.getText());
				sb.append(text);
				sb.append("\n");
				data.setText(sb.toString());
			}
		}
		
		@Override
		public void setProperty(String key, EObject value) {
			EObject copy = EcoreUtil.copy(value);
			synchronized (report) {
				node.getProperties().put(key, copy);
			}
		}
		
		@Override
		public EObject getProperty(String key) {
			synchronized (report) {
				return node.getProperties().get(key);
			}
		}

		@Override
		public NodeBuilder getParent() {
			return parent;
		}

		@Override
		public void addSnapshot(Snaphot snapshot) {
			Snaphot copy = EcoreUtil.copy(snapshot);
			copy.setTime(getTime());
			synchronized (report) {
				node.getSnapshots().add(copy);
			}
		}
		
		@Override
		public void update(Procedure1<Node> runnable) {
			synchronized (report) {
				runnable.apply(node);
			}
		}

		@Override
		public String getName() {
			return node.getName();
		}

		@Override
		public String toString() {
			return node.getName();
		}

		private int getChildPosition(String name) {
			int position = -1;
			int size = node.getChildren().size();
			for (int i = 0; i < size; i++) {
				Node child = node.getChildren().get(i);
				if (currentChildIndex == getNodeIndex(child)) {
					position = i;
				}
			}
			return position == -1 ? size : (position + 1);
		}

	}

	static private Report createReport() {
		Report report = ReportFactory.eINSTANCE.createReport();
		Node root = ReportFactory.eINSTANCE.createNode();
		report.setRoot(root);
		root.setName("root");
		root.setStartTime(getTime());
		return report;
	}

	private ReportBuilder(Report report, Node currentNode) {
		this.report = report;
		this.currentNode = new NodeBuilder(null, currentNode);
		if (report.getRoot() == null) 
			throw new NullPointerException();
		if (currentNode == null)
			throw new NullPointerException();
		Node root = currentNode;
		
		while (root.getParent() != null)
			root = root.getParent();
		if (root != report.getRoot())
			throw new IllegalArgumentException();
	}

	public EventSource registerEventSource(String name) {
		EventSource source = ReportFactory.eINSTANCE.createEventSource();
		source.setName(name);
		synchronized (report) {
			report.getSources().add(source);
		}
		return source;
	}


//	@Override
//	public void withCurrentNode(Procedure1<Node> procedure) {
//		synchronized (report) {
//			procedure.apply(currentNode);
//		}
//	}
	
//	 Report getReport() {
//		synchronized (report) {
//			report.getRoot().setEndTime(getTime());
//		}
//		return report;
//	}

	public Report getReportCopy() {
		synchronized (report) {
			Report reportCopy = EcoreUtil.copy(report);
			Node root = reportCopy.getRoot();
			root.setEndTime(getTime());
			root.setDuration(root.getEndTime() - root.getStartTime());
			return reportCopy;
		}
	}

	@Override
	public INodeBuilder getCurrent() {
		return currentNode;
	}

	public static long getTime() {
		return System.currentTimeMillis();
	}

	public void registerProviders(String... id) {
		if (id.length == 0) {
			EventProviderManager.getInstance().register(this, null);
		} else {
			for (String lid : id) {
				EventProviderManager.getInstance().register(this, lid);
			}
		}
	}

	public void unregisterProviders(String... id) {
		if (id.length == 0) {
			EventProviderManager.getInstance().unregister(this, null);
		} else {
			for (String lid : id) {
				EventProviderManager.getInstance().unregister(this, lid);
			}
		}
	}

	/**
	 * Search for event source with equals eobject specified in property
	 */
	public EventSource findSource(String attr, EObject info) {
		synchronized (report) {
			EList<EventSource> sources = report.getSources();
			for (EventSource eventSource : sources) {
				EObject object = eventSource.getProperties().get(attr);
				if (object != null && EcoreUtil.equals(object, info)) {
					return eventSource;
				}
			}
		}
		return null;
	}


	private static String getLogCategoryKey(LoggingCategory category) {
		return "log_" + category.name();
	}

	public static String getLogs(Node node) {
		StringBuilder result = new StringBuilder();
		String logs = null;
		for (LoggingCategory cat : LoggingCategory.VALUES) {
			logs = getLogs(node, cat);
			if (logs != null) {
				result.append(logs);
			}
		}
		return result.toString();
	}
	public static String getLogs(Node node, LoggingCategory cat) {
		EObject object = node.getProperties().get(getLogCategoryKey(cat));
		if (object != null && object instanceof LoggingData) {
			return ((LoggingData) object).getText();
		}
		return null;
	}
	
	
	public static ReportBuilder create(String title) {
		Report report = createReport();
		report.getRoot().setName(title);
		return new ReportBuilder(report, report.getRoot());
	}
	
	public ReportBuilderStore save() {
		ReportBuilderStore store = ReportFactory.eINSTANCE.createReportBuilderStore();
		synchronized (report) {
			EcoreUtil.Copier copier = new Copier();
			store.setReport((Report) copier.copy(report));
			store.setCurrentNode((Node) copier.get(currentNode.node));
		}
		assert store.getCurrentNode() != null;
		assert store.getCurrentNode().eContainer() != null;
		Node root = store.getReport().getRoot();
		root.setEndTime(getTime());
		root.setDuration(root.getEndTime() - root.getStartTime());
		return store;
	}
	
	public static ReportBuilder load(ReportBuilderStore store) {
		return new ReportBuilder(store.getReport(), store.getCurrentNode());
	}

	public static Snaphot createSnapshot(EObject data, Map<String, EObject> properties) {
		Snaphot snapshot = ReportFactory.eINSTANCE.createSnaphot();
		snapshot.setTime(getTime());
		snapshot.setData(Objects.requireNonNull(data));
		if (properties != null) {
			snapshot.getProperties().addAll(properties.entrySet());
		}
		return snapshot;
	}

	private static int getNodeIndex(Node node) {
		Object index = BoxedValues.unbox(node.getProperties().get(NODE_INDEX_PROPERTY));
		assert index != null;
		assert index instanceof Integer;
		return ((Integer) index).intValue();
	}

	private static void setNodeIndex(Node node, int index) {
		node.getProperties().put(NODE_INDEX_PROPERTY, BoxedValues.box(index));
	}

	private static long getNodeLastStartTime(Node node) {
		Object index = BoxedValues.unbox(node.getProperties().get(NODE_LASTSTARTTIME_PROPERTY));
		assert index != null;
		assert index instanceof Long;
		return ((Long) index).longValue();
	}

	private static void setNodeLastStartTime(Node node, long startTime) {
		node.getProperties().put(NODE_LASTSTARTTIME_PROPERTY, BoxedValues.box(startTime));
	}

}
