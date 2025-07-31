/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.watson.test;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.ecl.core.EclStackTraceEntry;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.sherlock.core.INodeBuilder;
import org.eclipse.rcptt.sherlock.core.SherlockCore;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Event;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.EventSource;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.LoggingCategory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Snaphot;
import org.eclipse.rcptt.sherlock.core.reporting.EventProviderManager;
import org.eclipse.rcptt.sherlock.core.reporting.IReportBuilder;
import org.eclipse.rcptt.sherlock.core.reporting.Procedure1;
import org.junit.Assert;
import org.junit.Test;

public class ReportProviderManagerTest {
	
	private class FlatReport implements IReportBuilder {
		public final Node node = org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory.eINSTANCE.createNode();

		@Override
		public EventSource registerEventSource(String name) {
			return null;
		}

		@Override
		public EventSource findSource(String attr, EObject info) {
			return null;
		}

		@Override
		public INodeBuilder getCurrent() {
			return new INodeBuilder() {
				
				@Override
				public void update(Procedure1<Node> runnable) {
					runnable.apply(node);
				}
				
				@Override
				public void setProperty(String key, EObject value) {
					node.getProperties().put(key, value);
				}
				
				@Override
				public EObject getProperty(String key) {
					return node.getProperties().get(key);
				}
				
				@Override
				public INodeBuilder getParent() {
					return this;
				}
				
				@Override
				public String getName() {
					return "";
				}
				
				@Override
				public void endTask() {
					
				}
				
				@Override
				public void createEvent(Event event) {
					node.getEvents().add(event);
				}
				
				@Override
				public INodeBuilder beginTask(String name) {
					return this;
				}
				
				@Override
				public INodeBuilder appendTask(String name) {
					return this;
				}
				
				@Override
				public void appendLog(LoggingCategory category, String text) {
				}
				
				@Override
				public void addSnapshot(Snaphot snapshot) {
				}
			};
		}

	}

	@Test
	public void statusStackTraceIsLogged() {
		FlatReport flatReport = new FlatReport(); 
		EventProviderManager manager = EventProviderManager.getInstance();
		String id = "org.eclipse.rcptt.sherlock.core.log.provider";
		manager.register(flatReport, id);
		try {
			loggingMethod();
		} finally {
			manager.unregister(flatReport, id);
		}
		
		ProcessStatus status = (ProcessStatus) flatReport.node.getEvents().get(0).getData();
		for (EclStackTraceEntry frame: status.getException().getStackTrace()) {
			if (frame.getMethodName().equals("loggingMethod"))
				return;
		}
		Assert.fail(status.getException().getStackTrace().toString());
		
	}

	private void loggingMethod() {
		org.eclipse.rcptt.sherlock.core.SherlockCore.log(new Exception());
	}

}
