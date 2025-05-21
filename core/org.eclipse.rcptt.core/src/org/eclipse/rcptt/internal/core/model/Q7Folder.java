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
package org.eclipse.rcptt.internal.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IQ7ProjectMetadata;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.core.scenario.ProjectMetadata;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.scenario.TestSuite;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.Q7LazyResource;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

public class Q7Folder extends Openable implements IQ7Folder {
	private final IPath path;

	protected Q7Folder(Q7Element parent, IPath path) throws IllegalArgumentException {
		super(parent);
		if (parent == null)
			throw new NullPointerException();
		this.path = path;
	}

	public HandleType getElementType() {
		return HandleType.Folder;
	}

	public boolean isRootFolder() {
		return path.segmentCount() == 0;
	}

	public IResource getResource() {
		if (isRootFolder()) {
			return this.getParent().getResource();
		}
		return ((IContainer) this.getParent().getResource()).getFolder(path);
	}

	public String getName() {
		if (path.segmentCount() == 0) {
			return "";
		}
		return path.lastSegment();
	}

	public IPath getPath() {
		return getParent().getPath().append(path);
	}

	@Override
	protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, IResource underlyingResource)
			throws ModelException {
		// check whether this folder can be opened
		if (!underlyingResource.isAccessible())
			throw newNotPresentException();

		Set<IQ7Element> vChildren = new HashSet<IQ7Element>();
		try {
			IResource[] members = ((IContainer) underlyingResource).members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource child = members[i];
				if (child.getType() != IResource.FOLDER) {
					IPath childPath = child.getFullPath();
					if (RcpttCore.isQ7File(childPath)) {
						if (RcpttCore.isQ7Context(childPath)) {
							vChildren.add(getContext(child.getName()));
						} else if (RcpttCore.isQ7Verification(childPath)) {
							vChildren.add(getVerification(child.getName()));
						} else if (RcpttCore.isQ7TestSuite(childPath)) {
							vChildren.add(getTestSuite(child.getName()));
						} else if (RcpttCore.isQ7ProjectMetadata(childPath)) {
							vChildren.add(getMetadata());
						} else {
							vChildren.add(getTestCase(child.getName()));
						}
					}
				}
			}
		} catch (CoreException e) {
			throw new ModelException(e);
		}
		info.setChildren(vChildren.toArray(new IQ7Element[vChildren.size()]));
		return true;
	}

	public ITestCase getTestCase(String name) {
		return new Q7TestCase(this, name);
	}

	public IContext getContext(String name) {
		return new Q7Context(this, name);
	}

	public ITestCase[] getTestCases() throws ModelException, InterruptedException {
		List<IQ7Element> list = getChildrenOfType(HandleType.TestCase);
		return list.toArray(new ITestCase[list.size()]);
	}

	public IContext[] getContexts() throws ModelException, InterruptedException {
		List<IQ7Element> list = getChildrenOfType(HandleType.Context);
		return list.toArray(new IContext[list.size()]);
	}

	public Object[] getForeignResources() throws ModelException, InterruptedException {
		return accessFolderInfo(info -> info.getForeignResources(getResource()));
	}

	public boolean hasSubfolders() throws ModelException, InterruptedException {
		IQ7Element[] packages = ((IQ7Project) getParent()).getChildren();
		int namesLength = this.path.segmentCount();
		nextPackage: for (int i = 0, length = packages.length; i < length; i++) {
			IPath otherNames = null;
			if (packages[i] instanceof Q7Folder) {
				otherNames = ((Q7Folder) packages[i]).path;
				if (otherNames.segmentCount() <= namesLength)
					continue nextPackage;
				for (int j = 0; j < namesLength; j++)
					if (!this.path.segment(j).equals(otherNames.segment(j)))
						continue nextPackage;
				return true;
			}
		}
		return false;
	}

	public boolean containsQ7Resources() throws ModelException, InterruptedException {
		return accessFolderInfo(Q7FolderInfo::containsQ7Resources);
	}

	public IFile getNewFile(IContainer container, String label, String extension) {
		label = Util.convert(label);
		String name = label + "." + extension;
		if (Util.isValidName(container, name)) {
			return container.getFile(new Path(name));
		}
		for (int i = 1;; i++) {
			name = label + i + "." + extension;
			if (Util.isValidName(container, name)) {
				return container.getFile(new Path(name));
			}
		}
	}

	public ITestCase createTestCase(String name, boolean force, IProgressMonitor monitor)
			throws ModelException {
		IContainer parentContainer = (IContainer) getResource();
		IFile file = getNewFile(parentContainer, name, TEST_FILE_EXTENSION);
		Resource resource = createResource(file);
		Scenario scenario = createScenario(name);
		resource.getContents().add(scenario);
		PersistenceManager.getInstance().saveResource(resource);
		return getTestCase(file.getName());
	}

	protected synchronized void createMetadata(boolean force,
			IProgressMonitor monitor) throws ModelException {
		IContainer parentContainer = (IContainer) getResource();
		IFile file = parentContainer.getFile(new Path(IQ7Project.METADATA_NAME));
		try {
			file.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			return; // no luck, maybe next time we'll create it
		}
		if (file.exists()) {
			return;
		}
		Resource resource = createResource(file);
		ProjectMetadata md = createMetadata();
		resource.getContents().add(md);
		PersistenceManager.getInstance().saveResource(resource);
	}

	public ProjectMetadata createMetadata() {
		ProjectMetadata md = ScenarioFactory.eINSTANCE.createProjectMetadata();
		md.setName("Project Settings");
		md.setId(EcoreUtil.generateUUID());
		return md;
	}

	protected Scenario createScenario(String name) {
		Scenario scenario = ScenarioFactory.eINSTANCE.createScenario();
		scenario.setVersion(Double.toString(RcpttCore.SCENARIO_VERSION));
		do {
			scenario.setId(EcoreUtil.generateUUID());
		} while (haveIDConflict(scenario.getId()));
		scenario.setName(name);
		return scenario;
	}

	protected TestSuite createTestSuite(String name) {
		TestSuite testSuite = ScenarioFactory.eINSTANCE.createTestSuite();
		do {
			testSuite.setId(EcoreUtil.generateUUID());
		} while (haveIDConflict(testSuite.getId()));
		testSuite.setName(name);
		return testSuite;
	}

	private Resource createResource(IFile file) {
		return new Q7LazyResource(URI.createPlatformResourceURI(file.getFullPath()
				.toString(), true));
	}

	public IContext createContext(String name, ContextType type, boolean force,
			IProgressMonitor monitor) throws ModelException {
		IContainer parentContainer = (IContainer) getResource();
		IFile file = getNewFile(parentContainer, name, CONTEXT_FILE_EXTENSION);
		Resource resource = createResource(file);
		createContext(name, type, resource);
		PersistenceManager.getInstance().saveResource(resource);
		return getContext(file.getName());
	}

	private void createContext(String name, ContextType type, Resource resource) {
		Context context = type.create(resource, name);
		do {
			context.setId(EcoreUtil.generateUUID());
		} while (haveIDConflict(context.getId()));
	}

	public boolean haveIDConflict(String id) {
		try {
			IQ7NamedElement[] resources = getQ7Project().findNamedElement(id);
			if (resources.length == 0) {
				return false;
			}
		} catch (ModelException e) {
			RcpttPlugin.log(e);
		}
		return true;
	}

	@Override
	protected Q7FolderInfo createElementInfo() {
		return new Q7FolderInfo();
	}

	public IQ7NamedElement getNamedElement(String name) {
		IPath filePath = getPath().append(name);
		if (RcpttCore.isQ7File(filePath)) {
			if (RcpttCore.isQ7Context(filePath))
				return getContext(name);
			else if (RcpttCore.isQ7Verification(filePath))
				return getVerification(name);
			else if (RcpttCore.isQ7TestSuite(filePath))
				return getTestSuite(name);
			else if (RcpttCore.isQ7ProjectMetadata(filePath))
				return getMetadata();
			else
				return getTestCase(name);
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Q7Folder))
			return false;

		Q7Folder other = (Q7Folder) o;
		return this.path.equals(other.path) && this.parent.equals(other.parent);
	}

	@Override
	public int hashCode() {
		return Util.combineHashCodes(parent.hashCode(), path.toString().hashCode());
	}

	@Override
	protected void toStringName(StringBuffer buffer) {
		String elementName = getName();
		if (elementName.length() == 0) {
			buffer.append("<default>"); //$NON-NLS-1$
		} else {
			buffer.append(elementName);
		}
	}

	public ITestSuite getTestSuite(String name) {
		return new Q7TestSuite(this, name);
	}

	public IQ7ProjectMetadata getMetadata() {
		return new Q7ProjectMetadata(this);
	}

	public ITestSuite[] getTestSuites() throws ModelException, InterruptedException {
		List<IQ7Element> list = getChildrenOfType(HandleType.TestSuite);
		return list.toArray(new ITestSuite[list.size()]);
	}

	public ITestSuite createTestSuite(String name, boolean force, IProgressMonitor monitor)
			throws ModelException {
		IContainer parentContainer = (IContainer) getResource();
		IFile file = getNewFile(parentContainer, name, TEST_SUITE_FILE_EXTENSION);
		Resource resource = createResource(file);
		TestSuite testSuite = createTestSuite(name);
		resource.getContents().add(testSuite);
		PersistenceManager.getInstance().saveResource(resource);
		return getTestSuite(file.getName());
	}

	public IVerification createVerification(String name, VerificationType type,
			boolean force, IProgressMonitor monitor) throws ModelException {
		IContainer parentContainer = (IContainer) getResource();
		IFile file = getNewFile(parentContainer, name, VERIFICATION_FILE_EXTENSION);
		Resource resource = createResource(file);
		createVerification(name, type, resource);
		PersistenceManager.getInstance().saveResource(resource);
		return getVerification(file.getName());
	}

	private void createVerification(String name, VerificationType type, Resource resource) {
		Verification verification = type.create(resource, name);
		do {
			verification.setId(EcoreUtil.generateUUID());
		} while (haveIDConflict(verification.getId()));
	}

	public IVerification getVerification(String name) {
		return new Q7Verification(this, name);
	}
	private <V> V accessFolderInfo(ThrowingFunction<Q7FolderInfo, V> infoToValue) throws ModelException, InterruptedException {
		return openAndAccessInfo(info -> {
			return infoToValue.apply((Q7FolderInfo)info);
		}, null);
	}
}
