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
package org.eclipse.rcptt.search;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.EList;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.ecl.context.EclContext;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.core.workspace.WorkspaceFinder;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.internal.core.model.Q7InternalContext;
import org.eclipse.rcptt.internal.core.model.Q7InternalVerification;
import org.eclipse.rcptt.parameters.Parameter;
import org.eclipse.rcptt.parameters.ParametersContext;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.internal.core.text.FileCharSequenceProvider;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.NewSearchUI;

/**
 * The visitor that does the actual work.
 */
@SuppressWarnings("restriction")
public class Q7SearchVisitor {

	private static final String JOB_NAME = "Searching...";

	private static final String SEARCH_CONTEXT_REFERENCES_TASK = "Searching references to context \"{0}\""; // TODO
																											// change
																											// description

	private static final String SEARCH_TESTCASE_REFERENCES_TASK = "Searching references to \"{0}\"";
	@SuppressWarnings("unused")
	private static final int N_THREADS = 8;

	public static class ReusableMatchAccess extends TextSearchMatchAccess {

		private int fOffset;
		private int fLength;
		private IFile fFile;
		private CharSequence fContent;

		public void initialize(IFile file, int offset, int length,
				CharSequence content) {
			fFile = file;
			fOffset = offset;
			fLength = length;
			fContent = content;
		}

		@Override
		public IFile getFile() {
			return fFile;
		}

		@Override
		public int getMatchOffset() {
			return fOffset;
		}

		@Override
		public int getMatchLength() {
			return fLength;
		}

		@Override
		public int getFileContentLength() {
			return fContent.length();
		}

		@Override
		public char getFileContentChar(int offset) {
			return fContent.charAt(offset);
		}

		@Override
		public String getFileContent(int offset, int length) {
			return fContent.subSequence(offset, offset + length).toString(); // must
																				// pass
																				// a
																				// copy!
		}
	}

	private final TextSearchRequestor fCollector;

	private IProgressMonitor fProgressMonitor;

	private int fNumberOfScannedFiles;
	private int fNumberOfFilesToScan;
	private IFile fCurrentFile;

	private final MultiStatus fStatus;

	private final FileCharSequenceProvider fFileCharSequenceProvider;

	private final ReusableMatchAccess fMatchAccess;

	private int fSearchFor;
	private String fQ7ElementName;
	private Pattern searchPattern;

	public Q7SearchVisitor(TextSearchRequestor collector,
			Pattern searchPattern, int searchFor, String q7ElementName) {
		this(collector, searchPattern);
		fSearchFor = searchFor;
		fQ7ElementName = q7ElementName;
	}

	public Q7SearchVisitor(TextSearchRequestor collector, Pattern searchPattern) {
		fCollector = collector;
		fStatus = new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK,
				SearchMessages.TextSearchEngine_statusMessage, null);

		this.searchPattern = searchPattern;
		fFileCharSequenceProvider = new FileCharSequenceProvider();
		fMatchAccess = new ReusableMatchAccess();
		fSearchFor = 0;
	}

	private Matcher getMatcher() {
		return searchPattern.pattern().length() == 0 ? null : searchPattern
				.matcher(new String());
	}

	public IStatus search(IFile[] files, IProgressMonitor monitor) {
		fProgressMonitor = monitor == null ? new NullProgressMonitor()
				: monitor;
		fNumberOfScannedFiles = 0;
		fNumberOfFilesToScan = files.length;
		fCurrentFile = null;

		Job monitorUpdateJob = new Job(JOB_NAME) {
			private int fLastNumberOfScannedFiles = 0;

			@Override
			public IStatus run(IProgressMonitor inner) {
				while (!inner.isCanceled()) {
					IFile file = null;
					synchronized (Q7SearchVisitor.this) {
						file = fCurrentFile;
					}
					if (file != null) {
						String fileName = file.getName();
						Object[] args = { fileName,
								Integer.valueOf(fNumberOfScannedFiles),
								Integer.valueOf(fNumberOfFilesToScan) };
						fProgressMonitor
								.subTask(Messages
										.format(SearchMessages.TextSearchVisitor_scanning,
												args));
						int steps = fNumberOfScannedFiles
								- fLastNumberOfScannedFiles;
						fProgressMonitor.worked(steps);
						fLastNumberOfScannedFiles += steps;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return Status.OK_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		};

		try {
			String taskName = getMatcher() == null ? SearchMessages.TextSearchVisitor_filesearch_task_label
					: Messages
							.format(SearchMessages.TextSearchVisitor_textsearch_task_label,
									getMatcher().pattern().pattern());
			if (fSearchFor == Q7SearchQuery.CONTEXTS_BY_ID) {
				taskName = Messages.format(SEARCH_CONTEXT_REFERENCES_TASK,
						fQ7ElementName);
			} else if (fSearchFor == Q7SearchQuery.VERIFICATION_BY_ID) {
				taskName = Messages.format(SEARCH_CONTEXT_REFERENCES_TASK,
						fQ7ElementName);
			} else if (fSearchFor == Q7SearchQuery.TESTCASE_BY_ID) {
				taskName = Messages.format(SEARCH_TESTCASE_REFERENCES_TASK,
						fQ7ElementName);
			}
			fProgressMonitor.beginTask(taskName, fNumberOfFilesToScan);
			monitorUpdateJob.setSystem(true);
			monitorUpdateJob.schedule();
			try {
				fCollector.beginReporting();
				processFiles(files, monitor);
				return fStatus;
			} finally {
				monitorUpdateJob.cancel();
			}
		} finally {
			fProgressMonitor.done();
			fCollector.endReporting();
		}
	}

	public IStatus search(TextSearchScope scope, IProgressMonitor monitor) {
		return search(scope.evaluateFilesInScope(fStatus), monitor);
	}

	private void processFiles(final IFile[] files,
			final IProgressMonitor monitor) {
		// ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
		for (int i = 0; i < files.length; i++) {
			final IFile curFile = files[i];
			// executor.execute(new Runnable() {
			// public void run() {
			if (monitor.isCanceled()) {
				return;
			}
			synchronized (this) {
				fCurrentFile = curFile;
			}
			processFile(curFile);
			// }
			// });
		}
		// executor.shutdown();
		// while (!executor.isTerminated()) {
		// try {
		// Thread.sleep(50);
		// } catch (Throwable e) {
		// // ignore
		// }
		// }
	}

	private String getScenarioContexts(ITestCase scenario)
			throws ModelException {
		String contextList = null;
		IContext[] contexts = RcpttCore.getInstance().getContexts(scenario,
				WorkspaceFinder.getInstance(), true);
		String[] contextsRef = Q7SearchCore.findContextsByDocument(scenario);
		if (contextsRef == null) {
			contextsRef = scenario.getContexts();
		}
		List<String> ctx = Arrays.asList(contextsRef);
		for (IContext contextId : contexts) {
			if (contextId instanceof Q7InternalContext) {
				continue;
			}
			if (contextId != null && ctx.contains(contextId.getID())) {
				if (contextList == null) {
					contextList = contextId.getElementName();
				} else {
					contextList += ", " + contextId.getElementName();
				}
			}
		}
		return contextList;
	}

	private String getScenarioVerifications(ITestCase scenario)
			throws ModelException {
		String verificationList = null;
		IVerification[] verifications = RcpttCore.getInstance().getVerifications(scenario,
				WorkspaceFinder.getInstance(), true);
		String[] verificationRefs = Q7SearchCore.findVerificationsByDocument(scenario);
		if (verificationRefs == null) {
			verificationRefs = scenario.getVerifications();
		}
		List<String> verificationIds = Arrays.asList(verificationRefs);
		for (IVerification v : verifications) {
			if (v instanceof Q7InternalVerification) {
				continue;
			}
			if (v != null && verificationIds.contains(v.getID())) {
				if (verificationList == null) {
					verificationList = v.getElementName();
				} else {
					verificationList += ", " + v.getElementName();
				}
			}
		}
		return verificationList;
	}

	private String getContextParameters(IContext context) throws CoreException {
		StringBuilder parametersList = new StringBuilder();
		NamedElement element = context.getNamedElement();
		if (element instanceof ParametersContext) {
			EList<Parameter> pars = ((ParametersContext) element).getParameters();
			for (Parameter par : pars) {
				parametersList.append(par.getName()).append(" = ")
						.append(par.getValue()).append('\n');
			}
		}
		return parametersList.toString();
	}

	private String getScenarioContextsIds(IQ7NamedElement scenario)
			throws ModelException {
		String ContextList = "";
		IContext[] contexts = RcpttCore.getInstance().getContexts(scenario, WorkspaceFinder.getInstance(), true);
		for (IContext contextId : contexts) {
			if (contextId != null
					&& !(contextId instanceof Q7InternalContext)) {
				ContextList += contextId.getID() + " ";
			}
		}
		return ContextList;
	}

	private String getScenarioVerificationIds(IQ7NamedElement scenario)
			throws ModelException {
		String verificationsList = "";
		if (scenario instanceof ITestCase) {
			IVerification[] verifications = RcpttCore.getInstance().getVerifications((ITestCase) scenario,
					WorkspaceFinder.getInstance(), true);
			for (IVerification v : verifications) {
				if (v != null && !(v instanceof Q7InternalVerification)) {
					verificationsList += v.getID() + " ";
				}
			}
		}
		return verificationsList;
	}

	private String getTestSuiteIds(ITestSuite testSuite) throws ModelException {
		String elementsList = "";
		IQ7NamedElement[] elements = Q7SearchCore
				.getTestSuiteContent(testSuite);
		for (IQ7NamedElement element : elements) {
			if (element != null) {
				elementsList += element.getID() + " ";
			}
		}
		return elementsList;
	}

	public boolean processFile(IFile file) {
		try {
			if (!fCollector.acceptFile(file) || getMatcher() == null
					|| isFileEmpty(file)) {
				return true;
			}
			CharSequence seq = null;
			try {
				IQ7NamedElement q7element = ((IQ7NamedElement) RcpttCore
						.create(file))
						.getIndexingWorkingCopy(new NullProgressMonitor());
				try {
					if (q7element != null) {
						String searchPart = null;
						switch (fSearchFor) {
						case Q7SearchQuery.DESCRIPTION:
							searchPart = q7element.getDescription();
							break;
						case Q7SearchQuery.TAGS:
							searchPart = q7element.getTags();
							break;
						case Q7SearchQuery.CONTEXTS:
							if (q7element instanceof ITestCase) {
								searchPart = getScenarioContexts((ITestCase) q7element);
							} else if (q7element instanceof IContext) {
								searchPart = getContextParameters((IContext) q7element);
							}
							break;
						case Q7SearchQuery.VERIFICATIONS:
							if (q7element instanceof ITestCase) {
								searchPart = getScenarioVerifications((ITestCase) q7element);
							}
							break;
						case Q7SearchQuery.CONTEXTS_BY_ID:
							searchPart = getScenarioContextsIds(q7element);
							break;
						case Q7SearchQuery.VERIFICATION_BY_ID:
							searchPart = getScenarioVerificationIds(q7element);
							break;
						case Q7SearchQuery.TESTCASE_BY_ID:
							if (q7element instanceof ITestSuite) {
								searchPart = getTestSuiteIds((ITestSuite) q7element);
							}
							break;
						default:
							try {
								if (q7element instanceof ITestCase) {
									searchPart = Scenarios
											.getScriptContent((Scenario) q7element
													.getNamedElement());
								} else if (q7element instanceof IContext) {
									IContext ctx = (IContext) q7element;
									if (ctx.getNamedElement() instanceof EclContext) {
										Script script = ((EclContext) ctx.getNamedElement()).getScript();
										if (script == null) {
											searchPart = "";
										}
										searchPart = script.getContent();
									}
								}
							} catch (NullPointerException e) {
								String[] args = {
										getExceptionMessage(e),
										file.getFullPath().makeRelative()
												.toString() };
								String message = Messages
										.format(SearchMessages.TextSearchVisitor_error,
												args);
								fStatus.add(new Status(IStatus.ERROR,
										NewSearchUI.PLUGIN_ID,
										IStatus.ERROR, message, e));
							}
							break;
						}
						seq = (searchPart == null) ? "" : searchPart;
						locateMatches(file, seq);
					}
				} finally {
					q7element.discardWorkingCopy();
				}
			} catch (FileCharSequenceProvider.FileCharSequenceException e) {
				Throwable cause = e.getCause();
				if (cause instanceof IOException) {
					throw (IOException) cause;
				}
				if (cause instanceof CoreException) {
					throw (CoreException) cause;
				}
				throw e;
			} finally {
				if (seq != null) {
					try {
						fFileCharSequenceProvider.releaseCharSequence(seq);
					} catch (IOException e) {
						SearchPlugin.log(e);
					}
				}
			}
		} catch (UnsupportedCharsetException e) {
			String[] args = { getCharSetName(file),
					file.getFullPath().makeRelative().toString() };
			String message = Messages.format(
					SearchMessages.TextSearchVisitor_unsupportedcharset, args);
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID,
					IStatus.ERROR, message, e));
		} catch (IllegalCharsetNameException e) {
			String[] args = { getCharSetName(file),
					file.getFullPath().makeRelative().toString() };
			String message = Messages.format(
					SearchMessages.TextSearchVisitor_illegalcharset, args);
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID,
					IStatus.ERROR, message, e));
		} catch (IOException e) {
			String[] args = { getExceptionMessage(e),
					file.getFullPath().makeRelative().toString() };
			String message = Messages.format(
					SearchMessages.TextSearchVisitor_error, args);
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID,
					IStatus.ERROR, message, e));
		} catch (CoreException e) {
			String[] args = { getExceptionMessage(e),
					file.getFullPath().makeRelative().toString() };
			String message = Messages.format(
					SearchMessages.TextSearchVisitor_error, args);
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID,
					IStatus.ERROR, message, e));
		} catch (StackOverflowError e) {
			String message = SearchMessages.TextSearchVisitor_patterntoocomplex0;
			fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID,
					IStatus.ERROR, message, e));
			return false;
		} finally {
			fNumberOfScannedFiles++;
		}
		if (fProgressMonitor.isCanceled())
			throw new OperationCanceledException(
					SearchMessages.TextSearchVisitor_canceled);

		return true;
	}

	private void locateMatches(IFile file, CharSequence searchInput)
			throws CoreException {
		try {
			Matcher matcher = getMatcher();
			matcher.reset(searchInput);
			int k = 0;
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				if (end != start) { // don't report 0-length matches
					fMatchAccess.initialize(file, start, end - start,
							searchInput);
					boolean res = fCollector.acceptPatternMatch(fMatchAccess);
					if (!res) {
						return; // no further reporting requested
					}
				}
				if (k++ == 20) {
					if (fProgressMonitor.isCanceled()) {
						throw new OperationCanceledException(
								SearchMessages.TextSearchVisitor_canceled);
					}
					k = 0;
				}
			}
		} finally {
			fMatchAccess.initialize(null, 0, 0, new String()); // clear
																// references
		}
	}

	private String getExceptionMessage(Exception e) {
		String message = e.getLocalizedMessage();
		if (message == null) {
			return e.getClass().getName();
		}
		return message;
	}

	private String getCharSetName(IFile file) {
		try {
			return file.getCharset();
		} catch (CoreException e) {
			return "unknown"; //$NON-NLS-1$
		}
	}

	private boolean isFileEmpty(IFile file) throws IOException, CoreException {
		boolean isEmpty = false;
		InputStream stream = new BufferedInputStream(file.getContents());
		try {
			int firstByte = stream.read();
			isEmpty = (firstByte == -1);
		} finally {
			stream.close();
		}
		return isEmpty;
	}

}
