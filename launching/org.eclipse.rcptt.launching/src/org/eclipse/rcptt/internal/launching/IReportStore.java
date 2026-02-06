/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.launching;

import static java.util.Objects.requireNonNull;

import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.streams.SherlockReportSession;

public interface IReportStore {
	interface IReportHandle {
		Report load();
	}
	public class InMemory implements IReportStore {
		private Report report = null;
		@Override
		public IReportHandle save(Report report) {
			if (this.report != null) {
				throw new IllegalStateException("Can't save multiple reports");
			}
			this.report = requireNonNull(report);
			
			return new IReportHandle() {
				@Override
				public Report load() {
					return requireNonNull(report);
				}
			};
		}
	}
	IReportHandle save(Report report);
	static IReportStore from(SherlockReportSession reportSession) {
		return new IReportStore() {

			@Override
			public IReportHandle save(Report report) {
				return new IReportHandle() {
					private final String id = reportSession.write(report);
					
					@Override
					public Report load() {
						return reportSession.getReport(id);
					}
				};
			}
			
		};
	}
	
}
