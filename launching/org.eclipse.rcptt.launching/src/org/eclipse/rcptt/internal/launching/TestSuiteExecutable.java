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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.IExecutable;

public class TestSuiteExecutable extends Executable {

	private final AutLaunch launch;
	private final ITestSuite testSuite;
	private final Executable[] kids;

	public TestSuiteExecutable(AutLaunch launch, ITestSuite testSuite,
			Executable[] children, boolean debug) {
		super(debug);
		this.launch = launch;
		this.testSuite = testSuite;
		kids = new Executable[children.length];
		if (children.length > 0) {
			System.arraycopy(children, 0, kids, 0, children.length);
		}
	}

	public String getName() {
		String nameByDocument = Q7SearchCore.findNameByDocument(testSuite);
		if (nameByDocument != null) {
			return nameByDocument;
		}

		try {
			return testSuite.getElementName();
		} catch (ModelException e) {
			return "Unresolved element";
		}
	}

	public AutLaunch getAut() {
		return launch;
	}

	public IQ7NamedElement getActualElement() {
		return testSuite;
	}
	
	public int getType() {
		return TYPE_TESTSUITE;
	}

	@Override
	public Executable[] getChildren() {
		return kids;
	}

	@Override
	public void startLaunching() {
		if (kids.length > 0) {
			kids[0].startLaunching();
		}
	}

	@Override
	protected IStatus handleChildResult(IStatus resultStatus) {
		if (resultStatus.matches(IStatus.CANCEL))
			return resultStatus;
		return Status.OK_STATUS;
	}

	@Override
	protected IStatus postExecute(IStatus result) {
		for (IExecutable child : getChildren()) {
			IStatus status = child.getResultStatus();
			if (status != null && !status.isOK()) {
				result = status;
				break;
			}
		}
		return result;
	}

}
