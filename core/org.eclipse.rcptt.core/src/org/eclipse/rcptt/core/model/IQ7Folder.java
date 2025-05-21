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
package org.eclipse.rcptt.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.VerificationType;

public interface IQ7Folder extends IOpenable, IParent, IQ7Element {
	public static final String CONTEXT_FILE_EXTENSION = "ctx";
	public static final String SCENARIO_FILE_EXTENSION = "scenario";
	public static final String TEST_FILE_EXTENSION = "test";
	public static final String TEST_SUITE_FILE_EXTENSION = "suite";
	public static final String VERIFICATION_FILE_EXTENSION = "verification";

	public static final String ROOT_FOLDER_NAME = ""; //$NON-NLS-1$
	public static final String DEFAULT_FOLDER_NAME = ""; //$NON-NLS-1$
	public static final char PACKAGE_DELIMITER = '/';
	public static final String PACKAGE_DELIMETER_STR = "/"; //$NON-NLS-1$

	ITestCase getTestCase(String name);

	IContext getContext(String name);

	ITestSuite getTestSuite(String name);

	boolean isRootFolder();

	ITestCase[] getTestCases() throws ModelException, InterruptedException;

	IContext[] getContexts() throws ModelException, InterruptedException;

	ITestSuite[] getTestSuites() throws ModelException, InterruptedException;

	public Object[] getForeignResources() throws ModelException, InterruptedException;

	boolean hasSubfolders() throws ModelException, InterruptedException;

	boolean containsQ7Resources() throws ModelException, InterruptedException;

	ITestCase createTestCase(String name, boolean force, IProgressMonitor monitor)
			throws ModelException;

	IContext createContext(String name, ContextType type, boolean force,
			IProgressMonitor monitor) throws ModelException;

	IVerification createVerification(String name, VerificationType type, boolean force,
			IProgressMonitor monitor) throws ModelException;

	ITestSuite createTestSuite(String name, boolean force, IProgressMonitor monitor)
			throws ModelException;

	IQ7NamedElement getNamedElement(String name);

	boolean haveIDConflict(String id);
}
