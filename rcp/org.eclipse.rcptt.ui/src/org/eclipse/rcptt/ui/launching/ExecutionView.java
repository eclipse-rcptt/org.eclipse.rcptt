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
package org.eclipse.rcptt.ui.launching;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.ecl.parser.ScriptErrorStatus;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.Q7Context;
import org.eclipse.rcptt.internal.launching.EclStackTrace;
import org.eclipse.rcptt.internal.launching.ExecutionStatus;
import org.eclipse.rcptt.internal.launching.PrepareExecutionWrapper;
import org.eclipse.rcptt.internal.launching.Q7LaunchManager;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.IExecutable.State;
import org.eclipse.rcptt.launching.IExecutionSession;
import org.eclipse.rcptt.launching.IExecutionSession.IExecutionSessionListener;
import org.eclipse.rcptt.launching.Q7Launcher;
import org.eclipse.rcptt.reporting.core.SimpleSeverity;
import org.eclipse.rcptt.reporting.util.IndentedWriter;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.ui.StyleRangeEntry;
import org.eclipse.rcptt.ui.actions.CollapseAllAction;
import org.eclipse.rcptt.ui.actions.LockSelectionAction;
import org.eclipse.rcptt.ui.actions.Q7ExecutionViewAction;
import org.eclipse.rcptt.ui.actions.RunFailedAction;
import org.eclipse.rcptt.ui.actions.RunSelectedAction;
import org.eclipse.rcptt.ui.actions.StopAction;
import org.eclipse.rcptt.ui.actions.StopOnFirstFailAction;
import org.eclipse.rcptt.ui.editors.context.ContextEditor;
import org.eclipse.rcptt.ui.editors.ecl.EclEditor;
import org.eclipse.rcptt.ui.editors.verification.VerificationEditor;
import org.eclipse.rcptt.ui.editors.verification.VerificationEditorPage;
import org.eclipse.rcptt.ui.history.ViewHistory;
import org.eclipse.rcptt.ui.report.ReportAction;
import org.eclipse.rcptt.ui.utils.Executables;
import org.eclipse.rcptt.ui.utils.RangeUtils;
import org.eclipse.rcptt.verifications.runtime.StyledMessage;
import org.eclipse.rcptt.verifications.runtime.VerificationReporter;
import org.eclipse.rcptt.verifications.runtime.VerificationStatus;
import org.eclipse.rcptt.verifications.status.VerificationStatusData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

import com.google.common.io.CharStreams;

public class ExecutionView extends ViewPart implements IExecutionSessionListener {

	public static final String ID = "org.eclipse.rcptt.ui.views.execution"; //$NON-NLS-1$

	private static final String DETAILS_LABEL = Messages.ExecutionView_MessageLabel;
	private static final String ADVANCED_INFO_LABEL = Messages.ExecutionView_DetailsLabel;

	private TreeViewer viewer;
	private StyledText failureTrace;
	private final StopAction stopAction = new StopAction();
	private final StopOnFirstFailAction stopOnFirstFailAction = new StopOnFirstFailAction(
			false);
	private Q7ExecutionViewAction runSelectedAction;
	private final RunFailedAction runFailedAction = new RunFailedAction();
	private ReportAction reportAction;
	private StatisticPanel statisticPanel;
	private ExecutionViewHistory viewHistory;
	private IExecutionSession activeSession;

	private Composite evParent;

	private Report report;
	private Button advInfoButton;

	private boolean scrollState = false;

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator"); //$NON-NLS-1$
	public static List<ExecutionView> views = new ArrayList<ExecutionView>();

	public ExecutionView() {
		synchronized (views) {
			views.add(this);
		}
	}

	static boolean hasEclScipt(IQ7NamedElement element) {
		if (element instanceof ITestCase)
			return true;
		if (element instanceof Q7Context) {
			try {
				ContextType type = ((Q7Context) element).getType();
				if (type.getId().equals("org.eclipse.rcptt.core.ecl.context"))
					return true;
			} catch (ModelException e) {
				return false;
			}
		}
		return false;
	}

	static void jump(EclEditor scenarioEditor, ExecutionStatus es) {
		if (es.hasLocation()) {
			scenarioEditor.setSelection(es.getLine(),
					es.getColumn(), es.getLength());
		} else {
			scenarioEditor.setSelection(1, 1, 1);
		}
	}

	private final Listener mouseUp = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (!(event.widget instanceof StyledText)) {
				return;
			}

			StyledText st = (StyledText) event.widget;
			int offset = st.getCaretOffset();

			Object data = null;
			for (StyleRange range : st.getStyleRanges()) {
				if (range.start > offset || (range.length + range.start) <= offset) {
					continue;
				}
				data = range.data;
				break;
			}

			if (data instanceof ScriptErrorStatus) {
				ScriptErrorStatus status = (ScriptErrorStatus) data;
				IQ7NamedElement element = Q7SearchCore.findById(status.getResource());
				openElement(element, status.getLine(), status.getColumn(), status.getLength());
			} else if (data instanceof VerificationStatusData) {
				VerificationStatusData statusData = (VerificationStatusData) data;
				IQ7NamedElement element = Q7SearchCore.findById(statusData.getResourceId());
				if (element == null) {
					return;
				}
				IFile resource = (IFile) element.getResource();
				if (resource == null) {
					return;
				}
				IEditorPart editor = null;
				try {
					editor = IDE.openEditor(getSite().getPage(), resource);
				} catch (PartInitException e) {
					return;
				}
				if (editor instanceof VerificationEditor) {
					Object page = ((VerificationEditor) editor).getSelectedPage();
					if (page instanceof VerificationEditorPage) {
						((VerificationEditorPage) page).select(statusData);
					}
				}
			}
		}
	};
	private final IResourceChangeListener workspaceListener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			// Refresh when project description changes
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				Q7UIPlugin.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!viewer.getControl().isDisposed())
							viewer.refresh();
					}
				});
			}
		}
	};

	@Override
	public void createPartControl(Composite parent) {
		evParent = parent;

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);

		viewHistory = new ExecutionViewHistory();

		Composite spComposite = createStatisticPanel(parent);
		spComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));

		SashForm form = new SashForm(parent, SWT.VERTICAL);
		form.setLayoutData(new GridData(GridData.FILL_BOTH));

		Tree tree = new Tree(form, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

		viewer = new TreeViewer(tree);
		viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ExecutionLabelProvider()));
		final ExecutionContentProvider cp = new ExecutionContentProvider();
		viewer.setContentProvider(cp);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				runSelectedAction.updateEnablement(activeSession);
				failureTrace.setText(""); //$NON-NLS-1$
				Object element = ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				if (element instanceof IExecutable) {
					IExecutable exec = (IExecutable) element;
					IStatus status = exec.getResultStatus();
					if (isConnectionTerminatedStatus(exec)) {
						failureTrace
								.setText(Messages.ExecutionView_NoConnectionEMsg);
						advInfoButton.setEnabled(false);
					} else if (status != null && !status.isOK()) {
						setMessage(exec.getActualElement(), status, 0);
						if (status instanceof ExecutionStatus) {
							ExecutionStatus st = (ExecutionStatus) status;
							AdvancedInformation info = st.getInfo();
							if (info != null) {
								advInfoButton.setEnabled(true);
							} else {
								advInfoButton.setEnabled(false);
							}
						}
					} else {
						advInfoButton.setEnabled(false);
					}
					// Also enable advanced info if report are available
					Object ee = element;
					while (ee != null
							&& !(ee instanceof PrepareExecutionWrapper)) {
						ee = cp.getParent(ee);
					}
					if (ee instanceof PrepareExecutionWrapper) {
						Report resultReport = ((PrepareExecutionWrapper) ee)
								.getResultReport();
						if (resultReport != null) {
							report = resultReport;
							advInfoButton.setEnabled(true);
						} else {
							report = null;
						}
					}

					// }
				}
			}
		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				Object element = ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				openElement(element);
			}
		});

		IToolBarManager manager = getViewSite().getActionBars()
				.getToolBarManager();
		runSelectedAction = new RunSelectedAction(viewer);

		manager.add(new CollapseAllAction(viewer));
		manager.add(new LockSelectionAction(this));
		manager.add(new Separator());
		manager.add(runSelectedAction);
		manager.add(runFailedAction);
		manager.add(stopAction);
		manager.add(stopOnFirstFailAction);
		manager.add(new Separator());
		manager.add(reportAction = new ReportAction(getSite()));
		manager.add(viewHistory.createHistoryDropDownAction());
		runSelectedAction.updateEnablement(null);
		runFailedAction.updateEnablement(null);
		stopAction.updateEnablement(null);

		// Create popup menu
		MenuManager mgr = new MenuManager();
		mgr.add(new Action("&Open") {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				if (!selection.isEmpty()
						&& selection instanceof IStructuredSelection) {
					openElement(((IStructuredSelection) selection)
							.getFirstElement());
				}
			}
		});
		mgr.add(runSelectedAction);
		mgr.add(stopAction);
		Menu menu = mgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		createFailTrace(form);

		form.setWeights(new int[] { 50, 50 });

		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				workspaceListener);

		initializeDnD();
	}

	private class ContentDragListener extends DragSourceAdapter {

		private final StructuredViewer structuredViewer;

		public ContentDragListener(StructuredViewer structuredViewer) {
			this.structuredViewer = structuredViewer;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void dragStart(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) structuredViewer
					.getSelection();
			List list = getSelection(event, selection);
			event.doit = !list.isEmpty();
			if (!list.isEmpty()) {
				LocalSelectionTransfer.getTransfer().setSelection(
						new StructuredSelection(list));
			}

		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void dragSetData(DragSourceEvent event) {
			if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
				ISelection selection = LocalSelectionTransfer.getTransfer()
						.getSelection();
				if (selection instanceof IStructuredSelection) {
					List list = ((IStructuredSelection) selection).toList();
					event.data = list.toArray(new IResource[list.size()]);
				}
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private List getSelection(DragSourceEvent event,
				IStructuredSelection selection) {
			List selectedElements = new ArrayList();
			List list = selection.toList();
			for (Object o : list) {
				if (o instanceof PrepareExecutionWrapper) {
					IQ7NamedElement element = ((PrepareExecutionWrapper) o)
							.getActualElement();
					if (element instanceof ITestCase) {
						if (element.getResource() != null) {
							selectedElements.add(element.getResource());
						}
					}
				}
			}
			return selectedElements;
		}
	}

	private void initializeDnD() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] {
				ResourceTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() };
		viewer.addDragSupport(ops, transferTypes, new ContentDragListener(
				viewer));

	}


	private boolean isConnectionTerminatedStatus(IExecutable exec) {
		IStatus status = exec.getResultStatus();
		
		if (Q7LaunchManager.isConnectionException(status)) {
			if (Messages.ExecutionView_ConnectionResetMsg.equals(status
					.getMessage())
					|| Messages.ExecutionView_ConnectionRefusedMsg
							.equals(status.getMessage())) {
				return true;
			}
			for (IExecutable execChild : exec.getChildren()) {
				if (isConnectionTerminatedStatus(execChild)) {
					return true;
				}
			}
		}
		return false;
	}

	private IRegion append(StringBuilder sb, String text) {
		int begin = sb.length();
		sb.append(text);
		int end = sb.length();
		return new Region(begin, end - begin);
	}

	private static StyleRange bold(IRegion region) {
		return new StyleRange(region.getOffset(), region.getLength(), null, null, SWT.BOLD);
	}

	private static StyleRange link(IRegion region, Object data) {
		StyleRange result = new StyleRange(region.getOffset(), region.getLength(), JFaceColors.getHyperlinkText(Display
				.getDefault()), null, SWT.NONE);
		result.data = data;
		result.underline = true;
		result.underlineStyle = SWT.UNDERLINE_LINK;
		return result;
	}

	private void setMessage(IQ7NamedElement namedElement, IStatus status,
			int level) {
		StringBuilder buffer = new StringBuilder();
		List<StyleRange> ranges = print(status, level, buffer);

		buffer.append(LINE_SEPARATOR);

		Throwable t = status.getException();
		if (t != null) {
			processThrowableMsg(level, buffer, t);
		}

		failureTrace.setText(buffer.toString());
		failureTrace.setStyleRanges(ranges.toArray(new StyleRange[ranges.size()]));
		failureTrace.removeListener(SWT.MouseUp, mouseUp);
		failureTrace.addListener(SWT.MouseUp, mouseUp);
	}

	private List<StyleRange> print(IStatus status, int level, StringBuilder buffer) {
		String message = status.getMessage();
		if (message == null || message.length() == 0) {
			if (status.matches(IStatus.CANCEL)) {
				message = "Execution is canceled.";
			} else if (status.matches(IStatus.ERROR)) {
				message = Messages.ExecutionView_ExecutionFailedMsg;
			} else {
				message = "Execution result is malformed.";
			}
		}
		List<StyleRange> ranges = new ArrayList<StyleRange>();
		
		if (status instanceof ExecutionStatus) {
			for (IStatus child : status.getChildren()) {
				ranges.addAll(print(child, level, buffer));
			}
			return ranges;
		}
		
		appendTabs(buffer, level);


		if (status instanceof ScriptErrorStatus) {
			EclStackTrace trace = EclStackTrace.fromScriptStatus(status);
			append(buffer, trace.getDisplayMessage());
			if (trace.frames.length != 0) {
				append(buffer, LINE_SEPARATOR);
			}
			for (ScriptErrorStatus frame : trace.frames) {
				append(buffer, "\tat ");
				ranges.add(bold(append(buffer, frame.getMessage())));
				append(buffer, " (");
				ranges.add(link(append(buffer, EclStackTrace.getLocation(frame)), frame));
				append(buffer, ")\n");
			}
			return ranges;
		} else if (status instanceof VerificationStatus) {
			VerificationStatus verStatus = (VerificationStatus) status;
			StyledMessage styledMsg = VerificationReporter.getStyledMessage(verStatus);

			buffer.append(styledMsg.getMessage());
			for (Entry<StyleRangeEntry, Object> style : styledMsg.getStyles().entrySet()) {
				ranges.add(makeMessageStyleRange(style.getKey(), style.getValue()));
			}
		} else {
			buffer.append(message);
			buffer.append(LINE_SEPARATOR);
		}
		for (IStatus child : status.getChildren()) {
			print(child, level + 1, buffer);
		}
		return ranges;
	}

	private StyleRange makeMessageStyleRange(StyleRangeEntry entry, Object data) {
		if (entry.getUnderline() != null && entry.getUnderline().equals("verification_link")) {
			StyleRange result = new StyleRange(entry.getStart(), entry.getLength(),
					JFaceColors.getHyperlinkText(Display
							.getDefault()), null, SWT.NONE);
			result.data = data;
			result.underline = true;
			result.underlineStyle = SWT.UNDERLINE_LINK;
			return result;
		} else {
			StyleRange result = RangeUtils.fromEMF(entry, evParent.getDisplay());

			if (result.underlineStyle == SWT.UNDERLINE_LINK) {
				result.underlineStyle = SWT.UNDERLINE_SINGLE;
			}
			result.start = entry.getStart();
			result.length = entry.getLength();
			result.data = data;
			return result;
		}
	}

	private void processThrowableMsg(final int level, StringBuilder buffer,
			Throwable t) {
		PrintWriter printWriter = new IndentedWriter(CharStreams.asWriter(buffer)) {
			@Override
			public void writeIndent() {
				appendTabs(this, level + 1);
			}
		};
		
		t.printStackTrace(printWriter);
		printWriter.close();

		// StackTraceElement[] trace = t.getStackTrace();
		// for (int i = 0; i < trace.length; i++) {
		// appendTabs(buffer, level + 1);
		//			buffer.append("at "); //$NON-NLS-1$
		// buffer.append(trace[i]);
		// buffer.append(LINE_SEPARATOR);
		// }
		// if (t.getCause() != null) {
		// buffer.append("Caused by: " + t.getCause().getMessage()).append(
		// "\n");
		// processThrowableMsg(level + 1, buffer, t.getCause());
		// }
	}

	private static <T extends Appendable> T appendTabs(T stream, int tabs) {
		for (int i = 0; i < tabs; ++i) {
			try {
				stream.append("  ");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return stream;
	}

	private Composite createStatisticPanel(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		statisticPanel = new StatisticPanel(composite);
		statisticPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		return composite;
	}

	private void createFailTrace(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		Composite detailsHeader = new Composite(composite, SWT.NONE);
		GridLayout detailsHeaderLayout = new GridLayout(2, false);
		detailsHeaderLayout.marginWidth = 5;
		detailsHeaderLayout.marginHeight = 0;
		detailsHeader.setLayout(detailsHeaderLayout);
		detailsHeader
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		Label label = new Label(detailsHeader, SWT.LEFT);
		label.setText(DETAILS_LABEL);
		advInfoButton = new Button(detailsHeader, SWT.PUSH);
		advInfoButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_END));
		advInfoButton.setText(ADVANCED_INFO_LABEL);
		advInfoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Dialog dialog = new DetailsDialog(failureTrace.getShell(), report);
				dialog.open();
			}
		});
		advInfoButton.setEnabled(false);

		failureTrace = new StyledText(composite, SWT.BORDER | SWT.READ_ONLY
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);
		failureTrace
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public void update(IExecutable... executables) {
		int i = 0;
		for (IExecutable executable : executables) {
			i++;
			State state = executable.getStatus();
			IStatus result = executable.getResultStatus();
			boolean failed = result.matches(~IStatus.INFO);
			// scroll only to last element
			if (!scrollState && i == executables.length && !result.matches(IStatus.CANCEL)) {
				viewer.setSelection(new StructuredSelection(executable), true);
			}
			if (IExecutable.State.COMPLETED == state && !failed) {
				// Collapse item if passed
				viewer.collapseToLevel(executable, TreeViewer.ALL_LEVELS);
			} else if (IExecutable.State.RUNNING == state) {
				viewer.expandToLevel(executable, 1);
			}

			if (failed && stopOnFirstFailAction.getValue()) {
				MultiStatus status = new MultiStatus(RcpttPlugin.PLUGIN_ID, 0, new IStatus[] { result },
						"Stopped after previous failure", null) {
					{
						setSeverity(IStatus.CANCEL);
					}
				};
				Q7Launcher.getInstance().stop(status);
			}
			viewer.refresh(executable);
		}
		if (activeSession != null) {
			statisticPanel.update(activeSession.getTotalCount(),
					activeSession.getFinishedCount(),
					activeSession.getFailedCount(),
					activeSession.getStoppedCount(),
					activeSession.getTotalTime());
		}
		runSelectedAction.updateEnablement(activeSession);
		runFailedAction.updateEnablement(activeSession);
	}

	@Override
	public void executionFinished() {
		activeSession.removeListener(this);
	}

	@Override
	public void statisticsUpdate() {
		if (activeSession == null) {
			return;
		}
		if (statisticPanel != null && statisticPanel.isDisposed()) {
			return;
		}
		statisticPanel.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				statisticPanel.update(activeSession.getTotalCount(),
						activeSession.getFinishedCount(), activeSession.getFailedCount(),
						activeSession.getStoppedCount(), activeSession.getTotalTime());
			}
		});
	}

	public void setInput(IExecutionSession session) {
		activeSession = session;
		if (activeSession != null) {
			activeSession.addListener(this);
		}
		statisticPanel.reset();

		IExecutable[] executables = new IExecutable[] {};
		if (session != null) {
			executables = session.getExecutables();
			statisticsUpdate();
		}
		Executables input = new Executables(executables);
		viewer.setSelection(null);
		viewer.setInput(input);

		runSelectedAction.updateEnablement(session);
		runSelectedAction.inputChanged(input);
		stopAction.updateEnablement(session);
		runFailedAction.updateEnablement(session);
		reportAction.setSession(session);
	}

	public void finished() {
		statisticsUpdate();
		if (activeSession != null) {
			activeSession.removeListener(this);
		}
		runSelectedAction.updateEnablement(activeSession);
		stopAction.updateEnablement(activeSession);
		runFailedAction.updateEnablement(activeSession);
	}

	@Override
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	private class ExecutionViewHistory extends ViewHistory {

		@Override
		public void configureHistoryListAction(IAction action) {
			action.setText(Messages.ExecutionView_HistoryActionText);
		}

		@Override
		public void configureHistoryDropDownAction(IAction action) {
			action.setToolTipText(Messages.ExecutionView_ExecutionHistoryActionToolTip);
			action.setImageDescriptor(ImageDescriptor.createFromImage(Images
					.getImage(Images.HISTORY)));
		}

		@Override
		public Action getClearAction() {
			return new ClearAction();
		}

		@Override
		public String getHistoryListDialogTitle() {
			return Messages.ExecutionView_HistoryListDialogTitle;
		}

		@Override
		public String getHistoryListDialogMessage() {
			return Messages.ExecutionView_HistoryListDialogMsg;
		}

		@Override
		public Shell getShell() {
			return evParent.getShell();
		}

		@Override
		public List<IExecutionSession> getHistoryEntries() {
			return Arrays.asList(Q7Launcher.getInstance()
					.getExecutionSessions());
		}

		@Override
		public Object getCurrentEntry() {
			return activeSession;
		}

		@Override
		public void setActiveEntry(Object entry) {
			setInput((IExecutionSession) entry);
		}

		@Override
		public void setHistoryEntries(List<?> remainingEntries,
				Object activeEntry) {
			setInput((IExecutionSession) activeEntry);

			IExecutionSession[] sessions = Q7Launcher.getInstance()
					.getExecutionSessions();
			for (IExecutionSession session : sessions) {
				if (!remainingEntries.contains(session)) {
					Q7Launcher.getInstance().removeExecutionSession(session);
				}
			}
		}

		@Override
		public ImageDescriptor getImageDescriptor(Object element) {
			IExecutionSession session = (IExecutionSession) element;
			if (session.isRunning()) {
				return ImageDescriptor.createFromImage(Images
						.getImage(Images.EXECUTION_SESSION_RUN));
			}

			SimpleSeverity severity = SimpleSeverity.create(session.getResultStatus());
			switch (severity) {
			case OK:
				return ImageDescriptor.createFromImage(Images
						.getImage(Images.EXECUTION_SESSION_OK));
			case ERROR:
				return ImageDescriptor.createFromImage(Images
						.getImage(Images.EXECUTION_SESSION_FAIL));
			case CANCEL:
				return ImageDescriptor.createFromImage(Images
						.getImage(Images.EXECUTION_SESSION));
			}
			throw new IllegalStateException("Unknown SimpleSeverity: " + severity);

		}

		@Override
		public String getText(Object element) {
			IExecutionSession session = (IExecutionSession) element;
			String startTime = DateFormat.getDateTimeInstance().format(
					session.getStartTime());
			return Messages.bind("{0} ({1})", session.getName(), startTime); //$NON-NLS-1$
		}

		@Override
		public void addMenuEntries(MenuManager manager) {
			// add additional menu entries here
		}

		@Override
		public String getMaxEntriesMessage() {
			return Messages.ExecutionView_MaxEntriesMsg;
		}

		@Override
		public int getMaxEntries() {
			return Q7Launcher.getInstance().getMaxHistoryEntries();
		}

		@Override
		public void setMaxEntries(int maxEntries) {
			Q7Launcher.getInstance().setMaxHistoryEntries(maxEntries);
		}
	}

	private class ClearAction extends Action {
		public ClearAction() {
			setText(Messages.ExecutionView_ClearActionText);

			boolean enabled = false;
			IExecutionSession[] sessions = Q7Launcher.getInstance()
					.getExecutionSessions();
			for (IExecutionSession session : sessions) {
				if (!session.isRunning()) {
					enabled = true;
					break;
				}
			}
			setEnabled(enabled);
		}

		@Override
		public void run() {
			List<IExecutionSession> runningSessions = getRunningSessions();
			Object first = runningSessions.isEmpty() ? null : runningSessions
					.get(0);
			viewHistory.setHistoryEntries(runningSessions, first);
		}

		private List<IExecutionSession> getRunningSessions() {
			IExecutionSession[] sessions = Q7Launcher.getInstance()
					.getExecutionSessions();
			List<IExecutionSession> runningSessions = new ArrayList<IExecutionSession>();
			for (IExecutionSession session : sessions) {
				if (session.isRunning()) {
					runningSessions.add(session);
				}
			}
			return runningSessions;
		}
	}

	public boolean getScrollState() {
		return scrollState;
	}

	public boolean updateScrollState() {
		scrollState = !scrollState;
		return scrollState;
	}

	@Override
	public void dispose() {
		synchronized (views) {
			views.remove(this);
		}
		super.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
				workspaceListener);
	}

	private void openElement(IQ7NamedElement element, int line, int column, int length) {
		if (element == null) {
			return;
		}

		IFile resource = (IFile) element.getResource();
		if (resource == null) {
			return;
		}
		IEditorPart editor = null;
		try {
			editor = IDE.openEditor(getSite().getPage(), resource);
		} catch (PartInitException e) {
			return;
		}

		if (line == -1 || column == -1 || length == -1) {
			return;
		}
		if (editor instanceof ContextEditor) {
			editor = ((ContextEditor) editor).getEditor();
		}

		if (editor instanceof EclEditor) {
			((EclEditor) editor).setSelection(line, column, length);
		}
	}

	private void openElement(Object element) {
		if (!(element instanceof IExecutable)) {
			return;
		}
		IExecutable exec = (IExecutable) element;
		int line = 1;
		int column = 1;
		int length = 0;
		if (exec.getResultStatus() instanceof ExecutionStatus) {
			ExecutionStatus st = (ExecutionStatus) exec.getResultStatus();
			line = st.getLine();
			column = st.getColumn();
			length = st.getLength();
		}
		openElement(exec.getActualElement(), line, column, length);
	}
}
