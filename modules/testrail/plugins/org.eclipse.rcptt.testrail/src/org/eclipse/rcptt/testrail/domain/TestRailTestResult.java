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
package org.eclipse.rcptt.testrail.domain;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TestRailTestResult {
	private String runId;
	private String caseId;
	@Expose
	@SerializedName("status_id")
	private String status;
	@Expose
	private String elapsed;
	@Expose
	private String comment;
	@Expose
	@SerializedName("custom_step_results")
	private List<TestRailStepResult> stepResults;

	public TestRailTestResult() {
	}

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setElapsed(String elapsed) {
		this.elapsed = elapsed;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setStepResults(List<TestRailStepResult> stepResults) {
		this.stepResults = stepResults;
	}
}
