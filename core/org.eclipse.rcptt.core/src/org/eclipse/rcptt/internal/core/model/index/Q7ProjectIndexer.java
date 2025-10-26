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
package org.eclipse.rcptt.internal.core.model.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.VerificationTypeManager;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IQ7ProjectMetadata;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.model.Q7Status.Q7StatusCode;
import org.eclipse.rcptt.core.model.index.IIndexDocument;
import org.eclipse.rcptt.core.model.index.IIndexer;
import org.eclipse.rcptt.core.model.index.IQ7IndexConstants;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.core.utils.TagsUtil;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.jobs.IJob;
import org.eclipse.rcptt.internal.core.model.ModelManager;

public class Q7ProjectIndexer implements IProjectIndexer, IProjectIndexer.Internal {

	private final IndexManager manager = ModelManager.getModelManager().getIndexManager();

	public void request(IJob job) {
		manager.request(job);
	}

	public void requestIfNotWaiting(IJob job) {
		manager.requestIfNotWaiting(job);
	}

	public IndexManager getIndexManager() {
		return manager;
	}

	public void indexProject(IQ7Project project) {
		final ProjectRequest request = new ProjectRequest(this, project);
		requestIfNotWaiting(request);
	}

	public void indexNamedElement(IQ7NamedElement element) {
		request(new NamedElementRequest(this, element));
	}

	public void indexNamedElements(Collection<IQ7NamedElement> toIndex) {
		if (toIndex.size() < 10) {
			for (IQ7NamedElement e : toIndex) {
				indexNamedElement(e);
			}
			return;
		}
		// Split per project
		Map<IQ7Project, List<IQ7NamedElement>> prjs = new HashMap<IQ7Project, List<IQ7NamedElement>>();
		for (IQ7NamedElement iq7NamedElement : toIndex) {
			IQ7Project project = iq7NamedElement.getQ7Project();
			List<IQ7NamedElement> list = prjs.get(project);
			if (list == null) {
				list = new ArrayList<IQ7NamedElement>();
				prjs.put(project, list);
			}
			list.add(iq7NamedElement);
		}
		for (Map.Entry<IQ7Project, List<IQ7NamedElement>> e : prjs.entrySet()) {
			request(new NamedElementsRequest(this, e.getKey(), e.getValue(), false));
		}
	}

	public void removeNamedElement(IQ7Project project, String path) {
		request(new NamedElementRemoveRequest(this, project, path));
	}

	public void removeProject(IPath projectPath) {
		requestIfNotWaiting(new RemoveIndexRequest(this, projectPath));
	}

	public boolean wantRefreshOnStart() {
		return true;
	}

	private Set<IQ7NamedElement> indexingSet = new HashSet<IQ7NamedElement>();

	public void indexNamedElement(Index index, IQ7NamedElement element) {
		IQ7NamedElement originalElement = element;
		final IIndexDocument document = new IndexDocument(element, index);
		try {
			synchronized (indexingSet) {
				while (indexingSet.contains(originalElement)) {
					try {
						indexingSet.wait(50);
					} catch (InterruptedException e) {
						// Wait
					}
				}
				indexingSet.add(originalElement);
			}
			try {
				if( !element.exists()) {
					return;
				}
			} catch (Exception e) {
				RcpttPlugin.log(e);
				return;
			}
			element = element.getIndexingWorkingCopy(new NullProgressMonitor());
			long stamp = element.getResource().getModificationStamp();
			index.remove(document.getContainerRelativePath());
			document.updateModificationStamp(stamp);
			doIndexing(document);
		} catch (ModelException e) {
			if (e.getStatus() instanceof Q7Status) {
				Q7StatusCode code = ((Q7Status) e.getStatus()).getStatusCode();
				if (code.equals(Q7StatusCode.NotPressent)
						|| code.equals(Q7StatusCode.NotOpen)) {
					index.remove(document.getContainerRelativePath());
					// Ignore not existing or not opened resource exceptions
					return;
				}
			}

			RcpttPlugin.log(e);
		} catch (Throwable e) {
			RcpttPlugin.log(e);
		} finally {
			synchronized (indexingSet) {
				indexingSet.remove(originalElement);
				indexingSet.notifyAll();
			}
			if (element != null) {
				try {
					element.discardWorkingCopy();
				} catch (ModelException e) {
					RcpttPlugin.log(e);
				}
			}
		}
	}

	public void doIndexing(IIndexDocument document) {
		IQ7NamedElement element = document.getElement();
		try {
			document.addKey(IQ7IndexConstants.ID, element.getID());
			document.addKey(IQ7IndexConstants.NAME, element.getElementName());
			if (element instanceof ITestCase) {
				document.addKey(IQ7IndexConstants.IS_EMPTY, Boolean
						.toString(Scenarios.isEmpty((Scenario) element
								.getNamedElement())));
			}
			String tags = element.getTags();
			if (tags != null && tags.length() > 0) {
				List<String> list = TagsUtil.extractTags(tags);
				for (String tag : list) {
					document.addKey(IQ7IndexConstants.TAG, tag);
				}
			}

			if (element instanceof IVerification) {
				NamedElement namedElement = element.getNamedElement();
				VerificationType type = VerificationTypeManager.getInstance()
						.getTypeByVerification((Verification) namedElement);
				if (type != null)
					document.addKey(IQ7IndexConstants.VERIFICATION_TYPE, type.getId());
			} else if (element instanceof IContext) {
				NamedElement namedElement = element.getNamedElement();
				ContextType type = ContextTypeManager.getInstance().getTypeByContext(
						(Context) namedElement);
				if (type != null) {
					document.addKey(IQ7IndexConstants.CONTEXT_TYPE, type.getId());
				}
			} else if (element instanceof ITestCase) {
				ITestCase testcase = (ITestCase) element;
				String[] contexts = testcase.getContexts();
				for (String ctx : contexts) {
					document.addKey(IQ7IndexConstants.CONTEXT_REF, ctx);
				}
				String[] verifications = testcase.getVerifications();
				for (String v : verifications) {
					document.addKey(IQ7IndexConstants.VERIFICATION_REF, v);
				}
				String reference = testcase.getExternalReference();
				if (reference != null) {
					document.addKey(IQ7IndexConstants.EXTERN_REF, reference);
				}
			} else if (element instanceof IQ7ProjectMetadata) {
				IQ7ProjectMetadata meta = (IQ7ProjectMetadata) element;
				String[] contexts = meta.getContexts();
				for (String ctx : contexts) {
					document.addKey(IQ7IndexConstants.CONTEXT_REF, ctx);
				}
				String[] verifications = meta.getVerifications();
				for (String v : verifications) {
					document.addKey(IQ7IndexConstants.VERIFICATION_REF, v);
				}
			}
			IIndexer[] indexers = IndexerManager.getInstance().getIndexers();
			for (IIndexer iIndexer : indexers) {
				iIndexer.index(document);
			}
		} catch (Throwable e) {
			boolean known = false;
			if (e instanceof ModelException) {
				ModelException me = (ModelException) e;
				if (me.getQ7Status().getStatusCode() == Q7StatusCode.NotPressent) {
					known = true;
				}
			}
			// could cause error dialog display, and hang
			if (!known) {
				Status status = new Status(Status.WARNING, RcpttPlugin.PLUGIN_ID,
						e.getMessage(), e);
				RcpttPlugin.getDefault().getLog().log(status);
			}
			document.remove();
		}
	}

	public Index getProjectIndex(IQ7Project project) {
		return getIndexManager().getIndex(project.getProject().getFullPath());
	}
}
