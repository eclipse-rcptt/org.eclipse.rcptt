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
package org.eclipse.rcptt.ui.refactoring.delete;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.resource.DeleteResourcesProcessor;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.BackReferencesProjectScope;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.refactoring.ResourceAccessChange;
import org.eclipse.rcptt.ui.utils.WriteAccessChecker;
import org.eclipse.ui.internal.UIPlugin;

@SuppressWarnings("restriction")
public class DeleteQ7ElementProcessor extends DeleteResourcesProcessor {

	private final IResource[] resources;
	private final Map<IQ7NamedElement, IQ7Element[]> references = new HashMap<IQ7NamedElement, IQ7Element[]>();
	private int referencesCount;
	private boolean deleteReferences = true;

	private class ReferenceCollector implements IRunnableWithProgress {

		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {

			// TODO figure out how to predict the amount of work
			monitor.beginTask(Messages.DeleteContextWizard_ProgressText,
					IProgressMonitor.UNKNOWN);

			// XXX it is very bad to access data from separate thread,
			// but it is OK here, since the code is called from
			// DeleteQ7ElementProcessor's constructor and no other
			// threads can have access to the data at this time
			referencesCount = countReferences(resources, resources);

			monitor.done();
		}

	}

	public DeleteQ7ElementProcessor(IResource[] resources) {
		super(resources);
		this.resources = resources;

		try {
			UIPlugin.getDefault().getWorkbench().getProgressService().
					run(true, false, new ReferenceCollector());

			/*
			 * new ProgressMonitorDialog(
			 * UIPlugin.getDefault().getWorkbench().getDisplay
			 * ().getActiveShell()). run(true, false, new ReferenceCollector());
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int countReferences(IResource[] resources, IResource[] toDelete) throws InterruptedException {

		int counter = 0;
		for (IResource resource : resources) {
			if (resource instanceof IFile && resource.isAccessible()) {
				if (RcpttCore.isQ7File(resource.getFullPath())) {
					IQ7Element element = RcpttCore.create(resource);
					if (element != null) {
						counter += resolveReferences(element, toDelete);
					}
				}
			}
			if (resource instanceof IContainer && resource.isAccessible()) {
				try {
					IResource[] members = ((IContainer) resource).members();
					counter += countReferences(members, toDelete);
				} catch (CoreException e) {
					Q7UIPlugin.log(e);
				}
			}
		}

		return counter;
	}

	private List<IQ7NamedElement> computeRealUsage(IQ7NamedElement[] persistedUsage,
			IQ7NamedElement[] unsavedUsage, IResource[] ignoredResources) {
		List<IQ7NamedElement> result = new ArrayList<IQ7NamedElement>(
				persistedUsage.length + unsavedUsage.length);

		Set<IPath> unsavedSet = new HashSet<IPath>();
		for (IQ7NamedElement e : unsavedUsage)
			unsavedSet.add(e.getPath());

		if (persistedUsage != null)
			for (IQ7NamedElement e : persistedUsage)
				if (!isToBeDeleted(e.getResource(), ignoredResources)
						&& !unsavedSet.contains(e.getPath()))
					result.add(e);

		for (IQ7NamedElement e : unsavedUsage)
			if (!isToBeDeleted(e.getResource(), ignoredResources))
				result.add(e);

		return result;
	}

	private int resolveReferences(IQ7Element element, IResource[] ignore) throws InterruptedException {
		if (element instanceof IContext) {
			try {
				IContext ctx = (IContext) element;

				List<IQ7NamedElement> realUsage = computeRealUsage(
						Q7SearchCore.findContextUsage(
								ctx.getID(),
								new BackReferencesProjectScope(element.getQ7Project()),
								new NullProgressMonitor())
						, ModelManager.getModelManager().findContextUsageInWorkingCopies(
								ctx.getID(),
								new BackReferencesProjectScope(element.getQ7Project()))
						, ignore);

				if (realUsage.size() > 0)
					references.put(ctx, realUsage.toArray(new IQ7NamedElement[realUsage.size()]));

				return realUsage.size();
			} catch (ModelException e) {
				Q7UIPlugin.log(e);
			}
		} else if (element instanceof IVerification) {
			try {
				IQ7NamedElement namedElement = (IQ7NamedElement) element;
				List<IQ7NamedElement> realUsage = computeRealUsage(
						Q7SearchCore.findVerificationUsage(
								namedElement.getID(),
								new BackReferencesProjectScope(element.getQ7Project()),
								new NullProgressMonitor())
						, ModelManager.getModelManager().findVerificationUsageInWorkingCopies(
								namedElement.getID(),
								new BackReferencesProjectScope(element.getQ7Project()))
						, ignore);

				if (realUsage.size() > 0)
					references.put(namedElement, realUsage.toArray(new IQ7NamedElement[realUsage.size()]));

				return realUsage.size();
			} catch (ModelException e) {
				Q7UIPlugin.log(e);
			}
		} else if (element instanceof ITestCase
				|| element instanceof ITestSuite) {
			try {
				ITestSuite[] testSuites = Q7SearchCore
						.findAllTestSuites(new BackReferencesProjectScope(
								element.getQ7Project()));
				List<IQ7NamedElement> realUsage = new ArrayList<IQ7NamedElement>();
				String elementID = ((IQ7NamedElement) element).getID();
				for (ITestSuite suite : testSuites) {
					for (IQ7Element testSuiteElement : Q7SearchCore
							.getTestSuiteContent(suite)) {
						if (testSuiteElement instanceof IQ7NamedElement
								&& elementID
										.equals(((IQ7NamedElement) testSuiteElement)
												.getID())) {
							if (!isToBeDeleted(suite.getResource(), ignore)) {
								realUsage.add(suite);
							}
						}
					}
				}

				IQ7NamedElement[] unsavedUsage = element instanceof ITestCase ?
						ModelManager.getModelManager().findTestCaseUsageInWorkingCopies(
								elementID,
								new BackReferencesProjectScope(element.getQ7Project())) :
						ModelManager.getModelManager().findTestSuiteUsageInWorkingCopies(
								elementID,
								new BackReferencesProjectScope(element.getQ7Project()));

				realUsage = computeRealUsage(realUsage.toArray(new IQ7NamedElement[0]), unsavedUsage, ignore);

				if (realUsage.size() > 0)
					references.put((IQ7NamedElement) element, realUsage.toArray(new IQ7NamedElement[realUsage.size()]));

				return realUsage.size();
			} catch (ModelException e) {
				Q7UIPlugin.log(e);
			}
		}
		return 0;
	}

	private boolean isToBeDeleted(IResource resource, IResource[] toDelete) {
		if (resource != null) {
			IPath resourcePath = resource.getFullPath();
			for (IResource resourceToDelete : toDelete) {
				if (resourceToDelete.getFullPath().isPrefixOf(resourcePath)) {
					return true;
				}
			}
		}
		return false;
	}

	public IResource[] getResources() {
		return resources;
	}

	public Object[] getReferencedElements() {
		Set<IQ7NamedElement> keys = references.keySet();
		if (keys != null) {
			return keys.toArray();
		}
		return null;
	}

	public Object[] getReferencingElements(Object object) {
		return references.get(object);
	}

	public int getReferencesCount() {
		return referencesCount;
	}

	public boolean isDeleteReferences() {
		return deleteReferences;
	}

	public void setDeleteReferences(boolean deleteReferences) {
		this.deleteReferences = deleteReferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor#
	 * checkInitialConditions(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException {
		RefactoringStatus status = super.checkInitialConditions(pm);
		if (!status.isOK()) {
			return status;
		}
		List<IResource> resources = new ArrayList<IResource>();
		for (Entry<IQ7NamedElement, IQ7Element[]> entry : references.entrySet()) {
			for (IQ7Element element : entry.getValue()) {
				resources.add(element.getResource());
			}
		}
		return RefactoringStatus.create(Resources.checkInSync(resources
				.toArray(new IResource[resources.size()])));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor#
	 * createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		if (deleteReferences) {
			pm.beginTask("", references.size() + 1); //$NON-NLS-1$
			try {
				Change deleteChange = super.createChange(pm);
				pm.worked(1);
				CompositeChange change = new CompositeChange(
						Messages.DeleteContextProcessor_ChangeName);
				change.markAsSynthetic();

				for (Entry<IQ7NamedElement, IQ7Element[]> entry : references
						.entrySet()) {
					for (IQ7Element element : entry.getValue()) {
						IQ7Element parent = element.getParent();
						checkParentResourceAccess(change, parent.getResource());
						if (WriteAccessChecker.isReadOnly(element)) {
							change.add(new ResourceAccessChange(element.getResource(), true));
						}
						if (entry.getKey() instanceof IContext) {
							IContext ctx = (IContext) entry.getKey();
							change.add(new DeleteContextReferenceChange(
									element, ctx, ctx.getID()));
						}
						else if (entry.getKey() instanceof IVerification) {
							IVerification verification = (IVerification)
									entry.getKey();
							change.add(new DeleteVerificationReferenceChange(
									element, verification, verification.getID()));
						} else {
							change.add(new DeleteTestReferenceChange(
									(ITestSuite) element, entry.getKey()));
						}
						pm.worked(1);
					}
				}
				for (IResource recource : resources) {
					IResource parent = recource.getParent();
					if (parent != null && WriteAccessChecker.isReadOnly(parent)) {
						change.add(new ResourceAccessChange(parent, true));
					}
				}
				change.add(deleteChange);
				return change;
			} finally {
				pm.done();
			}
		} else {
			CompositeChange deleteChange = (CompositeChange) super.createChange(pm);
			CompositeChange change = new CompositeChange(deleteChange.getName());
			for (IResource recource : resources) {
				IResource parent = recource.getParent();
				checkParentResourceAccess(change, parent);
			}
			change.merge(deleteChange);
			return change;
		}
	}

	private void checkParentResourceAccess(CompositeChange change, IResource parent) {
		if (parent != null && WriteAccessChecker.isReadOnly(parent)) {
			boolean exists = false;
			ResourceAccessChange changeParent = new ResourceAccessChange(parent, true);
			for (Change child : change.getChildren()) {
				if (child.getModifiedElement().equals(changeParent.getModifiedElement())) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				change.add(changeParent);
			}
		}
	}
}
