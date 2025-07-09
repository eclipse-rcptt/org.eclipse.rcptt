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
package org.eclipse.rcptt.testrail.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.testrail.TestRailService;
import org.eclipse.rcptt.testrail.domain.TestRailTestCase;
import org.eclipse.rcptt.ui.controls.SuggestionItem;
import org.eclipse.rcptt.ui.editors.IScenarioPropertyProvider;

public class TestRailPropertySuggestionProvider implements IScenarioPropertyProvider {
	
	private final static long UPDATE_DELAY = 5000;

	private List<SuggestionItem> testCaseIdSuggestions;

	@Override
	public List<SuggestionItem> getProperties(Scenario scenario) {
		List<SuggestionItem> props = new ArrayList<SuggestionItem>();
		props.add(new SuggestionItem(TestRailService.TESTRAIL_ID_PARAM));
		return props;
	}

	@Override
	public List<SuggestionItem> getPropertyValues(String name) {
		switch (name) {
		case TestRailService.TESTRAIL_ID_PARAM:
			initializeSuggestions();
			return testCaseIdSuggestions;
		}
		return Collections.emptyList();
	}

	private void initializeSuggestions() {
		if (testCaseIdSuggestions == null) {
			testCaseIdSuggestions = getTestCaseIdSuggestions();
			scheduleSuggestionsUpdate();
		}
	}
	
	private void scheduleSuggestionsUpdate() {
		Job openJob = new Job("Connecting to the TestRail Host") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				testCaseIdSuggestions = getTestCaseIdSuggestions();
				schedule(UPDATE_DELAY);
				return Status.OK_STATUS;
			}

		};
		openJob.schedule(UPDATE_DELAY);
	}

	private List<SuggestionItem> getTestCaseIdSuggestions() {
		TestRailService service = new TestRailService();
		List<TestRailTestCase> testCases = service.getTestCases(true);
		if (testCases == null) {
			return Collections.emptyList();
		}
		testCaseIdSuggestions = testCases.stream()
				.map(testCase -> getTestCaseIdSuggestion(testCase))
				.collect(Collectors.toList());
		return testCaseIdSuggestions;
	}

	private SuggestionItem getTestCaseIdSuggestion(TestRailTestCase testCase) {
		String value = TestRailService.TESTRAIL_TESTCASEID_PREFIX + testCase.getId();
		String textDescription = testCase.getTextDescription();
		String htmlDescription = testCase.getHTMLDescription();
		SuggestionItem.SuggestionDescription description = new SuggestionItem.SuggestionDescription(textDescription,
				htmlDescription);
		return new SuggestionItem(value, description);
	}

}
