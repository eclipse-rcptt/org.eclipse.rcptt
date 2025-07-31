/*******************************************************************************
 * Copyright (c) 2009, 2020 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.editors.ecl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BlockTextSelection;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IBlockTextSelection;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.ecl.parser.model.OccurrencesSettings;
import org.eclipse.rcptt.core.internal.builder.Q7Builder;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IQ7ProjectMetadata;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;
import org.eclipse.rcptt.core.workspace.Q7Utils;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.launching.Q7LaunchManager;
import org.eclipse.rcptt.internal.ui.HookUtils;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.builder.NamedElementReferencesResolver;
import org.eclipse.rcptt.ui.debug.EclLineBreakpointAdapter;
import org.eclipse.rcptt.ui.debug.ToggleBreakpointEnablementAction;
import org.eclipse.rcptt.ui.dialogs.AddProjectReferencesDialog;
import org.eclipse.rcptt.ui.dialogs.ChangesWarningDialog;
import org.eclipse.rcptt.ui.dialogs.RemoveProjectReferencesDialog;
import org.eclipse.rcptt.ui.editors.EditorContent;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.editors.FilenameUpdater;
import org.eclipse.rcptt.ui.editors.INamedElementEditor;
import org.eclipse.rcptt.ui.editors.ScriptSection;
import org.eclipse.rcptt.ui.editors.context.ContextEditor;
import org.eclipse.rcptt.ui.editors.ecl.actions.EclContentAssistAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.EclOpenDeclarationAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.EclScrollLinesAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.EclTextNavigationAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.FormatAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.NavigateNextSubWordAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.NavigatePreviousSubWordAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.SelectNextSubWordAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.SelectPreviousSubWordAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.SmartLineEndAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.SmartLineStartAction;
import org.eclipse.rcptt.ui.editors.ecl.actions.ToggleCommentAction;
import org.eclipse.rcptt.ui.panels.main.ControlPanelWindow;
import org.eclipse.rcptt.ui.utils.WorkbenchUtils;
import org.eclipse.rcptt.ui.utils.WriteAccessChecker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.operations.OperationHistoryActionHandler;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextOperationAction;

import com.google.common.io.Closer;

public class EclEditor extends AbstractDecoratedTextEditor implements
		IElementChangedListener, IGotoMarker, INamedElementEditor, IPreferenceChangeListener {
	public static final ILog LOG = Platform.getLog(EclEditor.class); 

	public EclSourceViewer viewer;
	private final Closer closer = Closer.create();

	private IQ7NamedElement model;
	private NamedElement element;

	private IQ7NamedElement handle;
	private FilenameUpdater filenameUpdater;

	private List<IAction> actions = new ArrayList<IAction>();

	private final Object reconcilerLock = new Object();

	private Annotation[] occurrencesAnnotations = null;
	private MarkAllOccurencesJob occurrencesFinderJob = new MarkAllOccurencesJob(this);
	private MarkAllOccurencesJobCanceler occurrencesFinderJobCanceler = new MarkAllOccurencesJobCanceler();
	private final ISelectionListener occurrencesPostSelectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if ((part instanceof EclEditor) || (part instanceof ContextEditor)) {
				if (selection instanceof ITextSelection) {
					updateOccurrenceAnnotations((ITextSelection) selection);
				}
			}
		}
	};;

	public void selectLine(int start, int length) {
		// start = this.getViewer().getDocument().getLineOfOffset(start) + 1;
		this.selectAndReveal(start, length);
		this.setFocus();
	}

	public MarkAllOccurencesJob getMarkAllOccurencesJob() {
		return occurrencesFinderJob;
	}

	public void replaceOccurencesAnnotation(IAnnotationModel annotationModel,
			Map<Annotation, Position> annotationsToAdd) {
		((IAnnotationModelExtension) annotationModel).replaceAnnotations(occurrencesAnnotations, annotationsToAdd);
	}

	public void setOccurencesAnnotation(Annotation[] arr) {
		occurrencesAnnotations = arr;
	}

	public void removeOccurrenceAnnotations() {
		IDocumentProvider documentProvider = getDocumentProvider();
		if (documentProvider == null)
			return;

		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(getEditorInput());
		if (annotationModel == null || occurrencesAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(occurrencesAnnotations, null);
			} else {
				for (int i = 0, length = occurrencesAnnotations.length; i < length; i++)
					annotationModel.removeAnnotation(occurrencesAnnotations[i]);
			}
			occurrencesAnnotations = null;
		}
	}

	public Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	private void updateOccurrenceAnnotations(ITextSelection selection) {
		OccurrencesSettings settings = ECLEditorPlugin.getMarkSettings();
		occurrencesFinderJob.cancel();
		if (!settings.enabled) {
			removeOccurrenceAnnotations();
			return;
		}

		ISourceViewer viewer = getSourceViewer();
		if (viewer == null) {
			return;
		}
		IDocument document = viewer.getDocument();
		if (document == null)
			return;

		occurrencesFinderJob.requestMark(selection);
	}

	private void installOccurrencesFinder() {
		ISelectionService service = this.getEditorSite().getService(ISelectionService.class);
		service.addPostSelectionListener(occurrencesPostSelectionListener);
		closer.register(() -> service.removePostSelectionListener(occurrencesPostSelectionListener) );
		occurrencesFinderJobCanceler.install(this);
		closer.register(() -> occurrencesFinderJobCanceler.uninstall(this));
	}

	@Override
	protected IVerticalRulerColumn createAnnotationRulerColumn(
			CompositeRuler ruler) {
		AnnotationRulerColumn column = (AnnotationRulerColumn) super.createAnnotationRulerColumn(ruler);
		column.addAnnotationType(Q7Builder.MARKER_TYPE);
		return column;
	}

	private int highlightedLine = -1;

	public void highlightLine(int line) {
		highlightedLine = line;
		getControl().redraw();

		// getDocumentProvider().getAnnotationModel(getEditorInput())
		// .addAnnotation(
		// new InstructionPointerAnnotation(null,
		// "org.eclipse.debug.ui.currentIP",
		// "Current executed line", null),
		// new Position(0, 10));
	}

	public void resetLineHighlighting() {
		highlightedLine = -1;
		getControl().redraw();
	}

	private class ContentAdapter extends EContentAdapter {

		private final NamedElement element;

		public ContentAdapter(NamedElement element) {
			this.element = element;
			element.eAdapters().add(this);
		}

		@Override
		public void notifyChanged(Notification msg) {
			super.notifyChanged(msg);
			setPartName(element.getName());
			if (msg.getEventType() == Notification.RESOLVE
					|| msg.getEventType() == Notification.REMOVING_ADAPTER)
				return;
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					firePropertyChange(PROP_DIRTY);
				};
			});

			if (msg.getFeature() != null) {
				if (msg.getFeature().equals(
						ScenarioPackage.Literals.SCENARIO__CONTEXTS)
						|| msg.getFeature().equals(ScenarioPackage.Literals.SCENARIO__VERIFICATIONS)
						|| msg.getFeature()
								.equals(ScenarioPackage.Literals.GROUP_CONTEXT__CONTEXT_REFERENCES)) {
					checkReferences();
				}
			}
		}
	}

	public EclEditor() {
		super();
		setDocumentProvider(createDocumentProvider());
		setSourceViewerConfiguration(new EclSourceViewerConfiguration(this));
		ECLEditorPlugin.getPreferences().addPreferenceChangeListener(this);
		closer.register(() -> occurrencesFinderJob.cancel() );
	}

	protected EclDocumentProvider createDocumentProvider() {
		return new EclDocumentProvider();
	}

	@Override
	public boolean isDirty() {
		try {
			if (model == null) {
				return false;
			}
			return model.hasUnsavedChanges();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return true;
		}
	}

	public EclSourceViewer getViewer() {
		return viewer;
	}

	private IFile getInputFile(IEditorInput input) {
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile();
		}
		return null;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IToggleBreakpointsTarget.class.isAssignableFrom(adapter) &&
				model != null) {
			return adapter.cast(new EclLineBreakpointAdapter(getModel().getResource()));
		}
		if (IGotoMarker.class.isAssignableFrom(adapter)) {
			return adapter.cast(this);
		}
		if (IResource.class.isAssignableFrom(adapter) &&
				model != null) {
			return adapter.cast(model.getResource());
		}
		if (ISourceViewer.class.isAssignableFrom(adapter)) {
			return adapter.cast(getSourceViewer());
		}

		return super.getAdapter(adapter);
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		try {
			IFile inputFile = getInputFile(input);
			if (inputFile == null) {
				throw new PartInitException(
						Messages.NamedElementEditor_InvalidInputType);
			}
			handle = (IQ7NamedElement) RcpttCore.create(inputFile);
			filenameUpdater = new FilenameUpdater(handle);
			updateElement();

			RcpttCore.addElementChangedListener(this);
			closer.register(() -> RcpttCore.removeElementChangedListener(this));
			setPartName(element.getName());
			scheduleCheckReferences();

			IEditorInput oldInput = getEditorInput();
			EclEditorInput newInput = createEditorInput();
			super.doSetInput(newInput);
			if (oldInput != null && !oldInput.equals(newInput)) {
				update(getModel(), new NullProgressMonitor());
			}
		} catch (Throwable e) {
			throw new PartInitException(
					Messages.NamedElementEditor_LoadingFailed, e);
		}
	}

	protected EclEditorInput createEditorInput() {
		return new EclEditorInput(model);
	}

	public IQ7NamedElement getModel() {
		return model;
	}

	public NamedElement getElement() {
		return element;
	}

	protected void afterMissingReferencesAdded() {
	}

	public void renamed(boolean wasDirty) {

	}

	private void scheduleCheckReferences() {
		new Job(Messages.NamedElementEditor_Job) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				checkReferences();
				return Status.OK_STATUS;
			}
		}.schedule(500);
	}

	protected void checkReferences() {
		try {
			// Check project references

			if (getModel() == null)
				return;

			IQ7Project prj = getModel().getQ7Project();
			IQ7ProjectMetadata metadata = prj.getMetadata();

			if (metadata != null && metadata.exists()) {
				final List<String> diff = new ArrayList<String>();

				String[] contexts = metadata.getContexts();
				if (contexts != null && contexts.length > 0) {
					Scenario el = (Scenario) getElement();
					EList<String> ctxs = el.getContexts();
					for (String c : contexts) {
						if (ctxs.contains(c)) {
							diff.add(c);
						}
					}
				}

				String[] verifications = metadata.getVerifications();
				if (verifications != null && verifications.length > 0) {
					Scenario el = (Scenario) getElement();
					EList<String> scenarioVerifications = el.getVerifications();
					for (String v : verifications) {
						if (scenarioVerifications.contains(v)) {
							diff.add(v);
						}
					}
				}

				if (diff.size() > 0) {
					getSite().getShell().getDisplay()
							.asyncExec(new Runnable() {
								public void run() {
									if (RemoveProjectReferencesDialog.open(
											getSite().getShell(),
											getModel(), diff)) {
										editorContent.update(getModel(),
												getElement());
									}
								}
							});
				}

			}

			final Map<IProject, Set<IQ7NamedElement>> missing = NamedElementReferencesResolver
					.resolveMissingReferences(getModel());
			if (!missing.isEmpty()) {
				IFile file = Q7Utils.getLocation(getElement());
				final IProject project = file.getProject();
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (AddProjectReferencesDialog.open(getSite()
								.getShell(), project, missing)) {
							afterMissingReferencesAdded();
						}
					}
				});
			}
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
		}
	}

	private void updateElement() throws ModelException, PartInitException {
		makeWorkingCopy();
		element = model.getNamedElement();
		if (element == null) {
			discardWorkingCopy();
			throw new PartInitException(
					Messages.NamedElementEditor_InvalidInputFormat);
		}
		// element.eAdapters().add(new AdapterImpl() {
		// public void notifyChanged(Notification notification) {
		// firePropertyChange(PROP_DIRTY);
		// }
		//
		// });
		new ContentAdapter(element);
		firePropertyChange(IWorkbenchPartConstants.PROP_PART_NAME);
	}

	private void discardWorkingCopy() throws ModelException {
		if (model != null) {
			model.discardWorkingCopy();
			model = null;
		}
	}

	public void removed(NamedElement element) {
		WorkbenchUtils.uiRun(new Runnable() {
			public void run() {
				getSite().getPage().closeEditor(EclEditor.this, false);
			}
		});
	}

	private void makeWorkingCopy() throws ModelException {
		RcpttCore.removeElementChangedListener(this);
		if (model != null) {
			discardWorkingCopy();
		}
		model = handle.getWorkingCopy(new NullProgressMonitor());
		if (viewer != null) {
			viewer.setElement(model);
		}
		RcpttCore.addElementChangedListener(this);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (Q7LaunchManager.getInstance().isElementUnderDebugging(model)) {
			new ChangesWarningDialog(getSite().getShell()).open();
		}

		try {
			RcpttCore.removeElementChangedListener(this);
			Shell shell = getSite().getShell();
			WriteAccessChecker writeAccessChecker = new WriteAccessChecker(shell);
			if (!writeAccessChecker.makeResourceWritable(model))
				return;
			normalizeDelimeters(viewer.getDocument());
			model.commitWorkingCopy(true, monitor);
			setPartName(element.getName());
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			Q7UIPlugin.log(e);
		} finally {
			RcpttCore.addElementChangedListener(this);
		}

		try {
			filenameUpdater.update(model);
		} catch (Exception e) {
			Q7UIPlugin.log(e);
		}
	}

	private void normalizeDelimeters(IDocument doc) throws BadLocationException {
		for (int i = 0; i < doc.getNumberOfLines(); i++) {
			String delim = doc.getLineDelimiter(i);
			if (delim != null && delim.length() > 1) {
				int offset = doc.getLineOffset(i);
				int len = doc.getLineLength(i);

				doc.replace(offset + len - delim.length(), delim.length(), "\n");
			}
		}
	}

	@Override
	public void createPartControl(Composite masterParent) {

		masterParent.setLayout(new FillLayout());
		Composite parent = new Composite(masterParent, SWT.NONE);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(parent);
		// parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite top = toolkit.createComposite(parent);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		parent.setData("my_layout_should_be_grid", true);

		header = createEditorHeader();

		Composite content = header.createContent(top, toolkit);

		editorContent = createEditorContent();

		editorContent.createSections(content, toolkit);
		editorContent.setSelectionProvider(getSite());

		ScriptSection section = editorContent.getScriptSection();
		if (section != null) {
			final ToolBarManager manager = new ToolBarManager(SWT.FLAT
					| SWT.HORIZONTAL);
			ToolBar toolBar = manager.createControl(section.getToolbar());
			toolBar.moveAbove(null);

			final IAction clearAction = new Action() {
				@Override
				public void run() {
					getViewer().getDocument().set("");
					// Scenarios.setEclContent(element, "");
				}
			};
			clearAction.setImageDescriptor(Images.getImageDescriptor(Images.PANEL_CLEAR));
			clearAction.setToolTipText(Messages.ScriptComposite_ClearActionToolTip);
			section.getSection().addExpansionListener(new IExpansionListener() {
				@Override
				public void expansionStateChanging(ExpansionEvent e) {
					clearAction.setEnabled(e.getState());
				}

				@Override
				public void expansionStateChanged(ExpansionEvent e) {
				}

			});
			manager.add(clearAction);

			manager.update(true);

			super.createPartControl(section.getContent());
		}
		editorContent.setViewer(viewer);
		configureViewer();

		HookUtils.textHook(parent, new Runnable() {

			@Override
			public void run() {
				updateActions();
			}
		});

		getControl().addLineBackgroundListener(new LineBackgroundListener() {
			public void lineGetBackground(LineBackgroundEvent event) {
				if (highlightedLine != -1 && event.lineBackground == null) {
					StyledText text = getControl();
					if (highlightedLine == text
							.getLineAtOffset(event.lineOffset)) {
						event.lineBackground = getControl().getDisplay()
								.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
					}
				}
			}
		});

		installOccurrencesFinder();
	}

	protected EditorContent createEditorContent() {
		return new EditorContent(header, true, true);
	}

	protected EditorHeader createEditorHeader() {
		return new EditorHeader(model, element) {

			@Override
			protected void record() {
				if (model instanceof ITestCase) {
					try {
						ControlPanelWindow.openControlPanel(getEditorSite()
								.getShell(), (ITestCase) model);
					} catch (ModelException e) {
						Q7UIPlugin.log(e);
						MessageDialog.openConfirm(getSite().getShell(),
								Messages.ShowControlPanel_ErrorDialogTitle,
								Messages.ShowControlPanel_ErrorDialogMsg);
					}
				}
			}
		};
	}

	@Override
	public void setAction(String actionID, IAction action) {
		super.setAction(actionID, action);
		this.actions.add(action);
	}

	private void updateActions() {
		updateContentDependentActions();
		updateSelectionDependentActions();
		updatePropertyDependentActions();
		updateStateDependentActions();
		boolean editorsActive = (viewer == null || !(viewer.getControl() != null && viewer
				.getControl().isFocusControl()))
				&& (header.isInFocus() || editorContent.isInFocus());

		updateEnabled(!editorsActive,
				ITextEditorActionConstants.MOVE_LINE_DOWN,
				ITextEditorActionConstants.MOVE_LINE_UP,
				ITextEditorActionConstants.COPY_LINE_UP,
				ITextEditorActionConstants.COPY_LINE_DOWN,
				ITextEditorActionConstants.CUT_LINE,
				ITextEditorActionConstants.CUT_LINE_TO_BEGINNING,
				ITextEditorActionConstants.CUT_LINE_TO_END);

		updateEnabled(!editorsActive, ITextEditorActionConstants.CONTENT_ASSIST);
	}

	private void updateEnabled(boolean editorsActive, String... ids) {
		for (String id : ids) {
			IAction action = getAction(id);
			if (action != null) {
				action.setEnabled(editorsActive);
			}
		}
	}

	@Override
	public void close(boolean save) {
		super.close(save);
		try {
			discardWorkingCopy();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
		}
	}

	@Override
	public void dispose() {
		try {
			discardWorkingCopy();
		} catch (ModelException e) {
			LOG.log(e.getStatus());
			throw new IllegalStateException(e);
		} finally {
			try {
				closer.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			} finally {
				super.dispose();
			}
		}
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			final IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		// ISourceViewer viewer= new SourceViewer(parent, ruler,
		// getOverviewRuler(), isOverviewRulerVisible(), styles);
		viewer = new EclSourceViewer(parent, ruler, getOverviewRuler(),
				isOverviewRulerVisible(), styles) {
			@Override
			public void doOperation(int operation) {
				boolean headerInFocus = header.isInFocus();
				boolean editorInFocus = editorContent.isInFocus();
				if (!viewer.getControl().isFocusControl()
						&& (headerInFocus || editorInFocus)) {
					try {
						switch (operation) {
						case SELECT_ALL:
							if (headerInFocus) {
								header.doSelectAll();
								return;
							}
							if (editorInFocus) {
								editorContent.doSelectAll();
								return;
							}
							break;
						case COPY:
							if (headerInFocus) {
								header.copy();
								return;
							}
							if (editorInFocus) {
								editorContent.copy();
								return;
							}
							break;
						case CUT:
							if (headerInFocus) {
								header.cut();
								return;
							}
							if (editorInFocus) {
								editorContent.cut();
								return;
							}
							break;
						case PASTE:
							if (headerInFocus) {
								header.paste();
								return;
							}
							if (editorInFocus) {
								editorContent.paste();
								return;
							}
							break;
						case UNDO:
							if (headerInFocus) {
								header.undo();
								return;
							}
							if (editorInFocus) {
								editorContent.undo();
								return;
							}
						case REDO:
							if (headerInFocus) {
								header.redo();
								return;
							}
							if (editorInFocus) {
								editorContent.redo();
								return;
							}
							return;
						}
					} finally {
						updateActions();
					}
				}

				super.doOperation(operation);
			}

			@Override
			public boolean canDoOperation(int operation) {
				if (!viewer.getControl().isFocusControl()
						&& (header.isInFocus() || editorContent.isInFocus())) {
					switch (operation) {
					case SELECT_ALL:
						return true;
					case COPY:
						return (header.isInFocus() && header.canCopy())
								|| (editorContent.isInFocus() && editorContent
										.canCopy());
					case PASTE:
						return (header.isInFocus() && header.canPaste())
								|| (editorContent.isInFocus() && editorContent
										.canPaste());
					case CUT:
						return (header.isInFocus() && header.canCut())
								|| (editorContent.isInFocus() && editorContent
										.canCut());
					case UNDO:
						return (header.isInFocus() && header.canUndo())
								|| (editorContent.isInFocus() && editorContent
										.canUndo());
					case REDO:
						return (header.isInFocus() && header.canRedo())
								|| (editorContent.isInFocus() && editorContent
										.canRedo());
					}
					return false;
				} else {
					switch (operation) {
					case UNDO:
						IUndoContext ctx = getUndoContext();
						if (ctx != null) {
							return getHistory().canUndo(ctx);
						}
						break;
					case REDO:
						ctx = getUndoContext();
						if (ctx != null) {
							return getHistory().canRedo(ctx);
						}
						break;
					}
				}

				return super.canDoOperation(operation);
			}
		};

		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		Control rulerControl = ruler.getControl();
		rulerControl.addMouseListener(new MouseListener() {

			public void mouseUp(final MouseEvent e) {
			}

			public void mouseDoubleClick(MouseEvent e) {
				if (1 == e.button) {
					ToggleBreakpointAction action = new ToggleBreakpointAction(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.getActivePart(),
							getViewer().getDocument(), ruler);
					action.update();
					action.run();
				}
			}

			public void mouseDown(MouseEvent e) {
			}
		});

		viewer.setElement(this.getModel());

		return viewer;
	}

	IOperationHistory getHistory() {
		if (PlatformUI.getWorkbench() == null) {
			return null;
		}

		return PlatformUI.getWorkbench().getOperationSupport()
				.getOperationHistory();
	}

	private IUndoContext getUndoContext() {
		if (getViewer() instanceof ITextViewerExtension6) {
			IUndoManager undoManager = ((ITextViewerExtension6) getViewer())
					.getUndoManager();
			if (undoManager instanceof IUndoManagerExtension)
				return ((IUndoManagerExtension) undoManager).getUndoContext();
		}
		return null;
	}

	private void registerUndoRedoAction(String actionId, ResourceAction action) {
		IAction oldAction = getAction(actionId);
		if (oldAction instanceof OperationHistoryActionHandler)
			((OperationHistoryActionHandler) oldAction).dispose();

		if (action == null)
			return;

		setAction(actionId, action);

		IActionBars actionBars = getEditorSite().getActionBars();
		if (actionBars != null)
			actionBars.setGlobalActionHandler(actionId, action);
	}

	protected void createUndoRedoActions() {
		ResourceAction action;
		ResourceBundle bundle = ResourceBundle
				.getBundle("org.eclipse.rcptt.ui.editors.ecl.EclEditorPluginResources"); //$NON-NLS-1$
		if (getAction(ITextEditorActionConstants.UNDO) == null) {
			action = new TextOperationAction(bundle, "Editor.Undo.", this, ITextOperationTarget.UNDO); //$NON-NLS-1$
			action.setHelpContextId(IAbstractTextEditorHelpContextIds.UNDO_ACTION);
			action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);
			setAction(ITextEditorActionConstants.UNDO, action);
			registerUndoRedoAction(ITextEditorActionConstants.UNDO, action);

		}

		if (getAction(ITextEditorActionConstants.REDO) == null) {
			action = new TextOperationAction(bundle, "Editor.Redo.", this, ITextOperationTarget.REDO); //$NON-NLS-1$
			action.setHelpContextId(IAbstractTextEditorHelpContextIds.REDO_ACTION);
			action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);
			setAction(ITextEditorActionConstants.REDO, action);
			registerUndoRedoAction(ITextEditorActionConstants.REDO, action);

		}
	}

	private void configureViewer() {
		viewer.setEditable(true);
		bindScriptContent();
	}

	public StyledText getControl() {
		return viewer.getTextWidget();
	}

	public void setSelection(int line, int column, int length) {
		IBlockTextSelection selection = new BlockTextSelection(
				viewer.getDocument(), line - 1, column - 1, line - 1, column
						+ length - 1, 0);
		viewer.setSelection(selection, true);
	}

	@Override
	protected void createActions() {
		super.createActions();
		ResourceBundle bundle = ResourceBundle
				.getBundle("org.eclipse.rcptt.ui.editors.ecl.EclEditorPluginResources"); //$NON-NLS-1$
		setAction("ContentAssistProposal", new TextOperationAction(bundle, //$NON-NLS-1$
				"ContentAssistProposal.", this, //$NON-NLS-1$
				ISourceViewer.CONTENTASSIST_PROPOSALS));
		// action.setActionDefinitionId(CONTENTASSIST_PROPOSAL_ID);
		setActionActivationCode("ContentAssistProposal", ' ', -1, SWT.CTRL); //$NON-NLS-1$

		setAction("ContentAssistTip", new TextOperationAction(bundle, //$NON-NLS-1$
				"ContentAssistTip.", this, //$NON-NLS-1$
				ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION));

		IAction action = new TextOperationAction(bundle, "Uncomment.", this,
				ITextOperationTarget.STRIP_PREFIX); // $NON-NaLS-1$
		action.setActionDefinitionId("org.eclipse.rcptt.ui.edit.text.ecl.uncomment");
		setAction("Uncomment", action); //$NON-NLS-1$
		markAsStateDependentAction("Uncomment", true); //$NON-NLS-1$

		action = new ToggleCommentAction(bundle, "ToggleComment.", this); //$NON-NLS-1$
		action.setActionDefinitionId("org.eclipse.rcptt.ui.edit.text.ecl.toggle.comment");
		setAction("ToggleComment", action); //$NON-NLS-1$
		markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
		configureToggleCommentAction();

		action = new EclContentAssistAction(bundle, "Editor.ContentAssistProposal.", this); //$NON-NLS-1$
		// action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONTENT_ASSIST_ACTION);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction(ITextEditorActionConstants.CONTENT_ASSIST, action);
		markAsStateDependentAction(ITextEditorActionConstants.CONTENT_ASSIST,
				true);

		addFormatAction(null);
	}

	private void configureToggleCommentAction() {
		IAction action = getAction("ToggleComment"); //$NON-NLS-1$
		if (action instanceof ToggleCommentAction) {
			ISourceViewer sourceViewer = getSourceViewer();
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			((ToggleCommentAction) action).configure(sourceViewer,
					configuration);
		}
	}

	@Override
	protected void createNavigationActions() {
		super.createNavigationActions();

		IAction action;

		final StyledText textWidget = getSourceViewer().getTextWidget();
		for (int i = 0; i < ACTION_MAP.length; i++) {
			IdMapEntry entry = ACTION_MAP[i];
			action = new EclTextNavigationAction(this, textWidget,
					entry.getAction());
			action.setActionDefinitionId(entry.getActionId());
			setAction(entry.getActionId(), action);
		}

		action = new SmartLineStartAction(this, textWidget, false);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_START);
		setAction(ITextEditorActionDefinitionIds.LINE_START, action);

		action = new SmartLineStartAction(this, textWidget, true);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_LINE_START);
		setAction(ITextEditorActionDefinitionIds.SELECT_LINE_START, action);

		action = new SmartLineEndAction(this, textWidget, true);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_LINE_END);
		setAction(ITextEditorActionDefinitionIds.SELECT_LINE_END, action);

		action = new NavigatePreviousSubWordAction(this);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_LEFT, SWT.NULL);

		action = new NavigateNextSubWordAction(this);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_RIGHT, SWT.NULL);

		action = new SelectPreviousSubWordAction(this);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_LEFT,
				SWT.NULL);

		action = new SelectNextSubWordAction(this);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_RIGHT,
				SWT.NULL);

		action = new EclScrollLinesAction(this, -1);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SCROLL_LINE_UP);
		setAction(ITextEditorActionDefinitionIds.SCROLL_LINE_UP, action);

		action = new EclScrollLinesAction(this, 1);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SCROLL_LINE_DOWN);
		setAction(ITextEditorActionDefinitionIds.SCROLL_LINE_DOWN, action);

		action = new EclOpenDeclarationAction(this);
		action.setActionDefinitionId(GO_TO_DECLARATION_ACTION);
		setAction(GO_TO_DECLARATION_ACTION, action);
	}

	private static final String SCENARIO_ERROR = "Test case contains errors. Please see error log for details."; //$NON-NLS-1$

	public EditorHeader header;

	public EditorContent editorContent;

	private boolean hasContainerChanges(IQ7ElementDelta delta) {
		IQ7Element element = delta.getElement();
		if (element instanceof IQ7Project || element instanceof IQ7Folder) {
			if (delta.getKind() == IQ7ElementDelta.REMOVED) {
				IPath p1 = delta.getElement().getPath();
				IPath p2 = model.getPath();
				if (p1.isPrefixOf(p2)) {
					return true;
				}
			}
		}
		IQ7ElementDelta[] deltas = delta.getAffectedChildren();
		for (IQ7ElementDelta childDelta : deltas) {
			return hasContainerChanges(childDelta);
		}
		return false;
	}

	private IQ7Element[] hasContainerMoveChanges(IQ7ElementDelta delta) {
		IQ7Element element = delta.getElement();
		if (element instanceof IQ7Project || element instanceof IQ7Folder) {
			if (delta.getKind() == IQ7ElementDelta.REMOVED) {
				IPath p1 = delta.getElement().getPath();
				IPath p2 = model.getPath();
				if (p1.isPrefixOf(p2)) {
					if (((delta.getFlags() & IQ7ElementDelta.F_MOVED_FROM) != 0 || (delta
							.getFlags() & IQ7ElementDelta.F_MOVED_TO) != 0)) {
						return new IQ7Element[] { delta.getElement(),
								delta.getMovedToElement() };
					}
					return null;
				}
			}
		}
		IQ7ElementDelta[] deltas = delta.getAffectedChildren();
		for (IQ7ElementDelta childDelta : deltas) {
			IQ7Element el[] = hasContainerMoveChanges(childDelta);
			if (el != null) {
				return el;
			}
		}
		return null;
	}

	public void elementChanged(Q7ElementChangedEvent event) {
		if (model == null) {
			return;
		}
		IQ7ElementDelta delta = event.getDelta();
		IQ7Element[] moveTo = hasContainerMoveChanges(delta);
		if (moveTo != null) {
			IPath relativePath = model.getPath().removeFirstSegments(
					moveTo[0].getPath().segmentCount());
			IPath newPath = moveTo[1].getPath().append(relativePath);
			replaceResource(RcpttCore.create(ResourcesPlugin.getWorkspace()
					.getRoot().findMember(newPath)));
			return;
		}
		IQ7NamedElement[] namedElements = delta.getNamedElements();
		for (final IQ7NamedElement e : namedElements) {
			IQ7ElementDelta childDelta = delta.getChildBy(e);
			if (model.equals(e)) {
				switch (childDelta.getKind()) {
				case IQ7ElementDelta.REMOVED:
					if (!((childDelta.getFlags() & IQ7ElementDelta.F_MOVED_FROM) != 0 || (childDelta
							.getFlags() & IQ7ElementDelta.F_MOVED_TO) != 0)) {
						resourceRemoved(e);
						break;
					}
				case IQ7ElementDelta.CHANGED:
					if ((childDelta.getFlags() & IQ7ElementDelta.F_CONTENT) != 0
							|| (childDelta.getFlags() & IQ7ElementDelta.F_MOVED_FROM) != 0
							|| (childDelta.getFlags() & IQ7ElementDelta.F_MOVED_TO) != 0) {
						if ((childDelta.getFlags() & IQ7ElementDelta.F_MOVED_TO) != 0) {
							final IQ7Element el = childDelta
									.getMovedToElement();
							if (el != null && el instanceof IQ7NamedElement) {
								replaceResource(el);
								return;
							}
						}
						resourceChanged(e);
						return;
					}
					break;
				}
			}
		}

		IQ7ElementDelta[] children = delta.getAffectedChildren();
		IQ7Project project = model.getQ7Project();
		for (IQ7ElementDelta c : children) {
			if (c.getElement().equals(project)
					&& (c.getFlags() & IQ7ElementDelta.F_CLOSED) != 0) {
				resourceClosed(model);
			}
		}

		if (hasContainerChanges(delta)) {
			resourceRemoved(model);
			return;
		}
	}

	private void replaceResource(final IQ7Element el) {
		try {
			RcpttCore.removeElementChangedListener(this);
			discardWorkingCopy();
			RcpttCore.addElementChangedListener(this);
			WorkbenchUtils.uiRun(new Runnable() {
				public void run() {
					// init(getEditorSite(),
					// new FileEditorInput((IFile) el.getResource()));
					update((IQ7NamedElement) el, new NullProgressMonitor());
				}
			});
		} catch (ModelException e2) {
			Q7UIPlugin.log(e2);
		}
		resourceChanged((IQ7NamedElement) el);
		return;
	}

	private void resourceRemoved(final IQ7NamedElement e) {
		WorkbenchUtils.uiRun(new Runnable() {
			public void run() {
				// String title =
				// Messages.NamedElementEditor_ResourceDeletedDialogTitle;
				// String msg = Messages.bind(
				// Messages.NamedElementEditor_ResourceDeletedDialogMsg,
				// getInputFile().getName());
				// String[] buttons = {
				// Messages.NamedElementEditor_ResourceDeletedDialogButtonSave,
				// Messages.NamedElementEditor_ResourceDeletedDialogButtonClose,
				// };
				//
				// MessageDialog dialog = new
				// MessageDialog(getSite().getShell(),
				// title, null, msg, MessageDialog.QUESTION, buttons, 0);
				// if (isDirty() && dialog.open() == 0) {
				// doSave(getProgressMonitor());
				// } else {
				close(false);
				// }
			}
		});
	}

	private void resourceClosed(final IQ7NamedElement e) {
		WorkbenchUtils.uiRun(new Runnable() {
			public void run() {
				close(true);
			}
		});
	}

	private void update(IQ7NamedElement newOrigin, IProgressMonitor monitor) {
		try {
			RcpttCore.removeElementChangedListener(this);
			discardWorkingCopy();
			handle = newOrigin;
			// Copy content from new to old
			updateElement();

			WorkbenchUtils.uiRun(new Runnable() {
				public void run() {
					try {
						EclEditor.super.doSetInput(createEditorInput());
					} catch (CoreException e) {
						Q7UIPlugin.log(e);
					}
					header.update(model, element);
					editorContent.update(model, element);
					setPartName(element.getName());
					scheduleCheckReferences();
					bindScriptContent();
				}
			});

			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			WorkbenchUtils.uiRun(new Runnable() {
				public void run() {
					close(false);
				}
			});
			Q7UIPlugin.log(e);
		} finally {
			RcpttCore.addElementChangedListener(this);
		}
	}

	protected void bindScriptContent() {
		IDocument document = getDocumentProvider()
				.getDocument(getEditorInput());
		document.addDocumentListener(new IDocumentListener() {
			public void documentChanged(DocumentEvent event) {
				String value = viewer.getDocument().get();
				setEclContext(element, value);
			}

			public void documentAboutToBeChanged(DocumentEvent event) {
				// Do nothing
			}
		});

		IChangeListener scenarioContentListener = new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				String script;
				try {
					script = getEclContext(element);
				} catch (Exception e) {
					script = SCENARIO_ERROR;
					Q7UIPlugin.log(e);
				}
				// in some cases after hit upon here from document listener
				// viewer.getDocument() is null
				if (viewer.getDocument() != null) {
					String doc = viewer.getDocument().get();
					if (!doc.equals(script)) {
						viewer.getDocument().set(script);
					}
				}
			}
		};
		IObservableValue<?> scriptContent = EMFObservables.observeValue(element,
				ScenarioPackage.Literals.SCENARIO__CONTENT);
		scriptContent.addChangeListener(scenarioContentListener);

		IObservableValue<?> teslaContent = EMFObservables.observeValue(element,
				ScenarioPackage.Literals.SCENARIO__TESLA_CONTENT);
		teslaContent.addChangeListener(scenarioContentListener);
	}

	private void resourceChanged(final IQ7NamedElement e) {
		WorkbenchUtils.uiRun(new Runnable() {
			public void run() {
				String title = Messages.NamedElementEditor_ResourceChangedDialogTitle;
				String msg = Messages.bind(
						Messages.NamedElementEditor_ResourceChangedDialogMsg,
						getInputFile(getEditorInput()).getName());
				if (isDirty()) {
					if (MessageDialog.openQuestion(getSite().getShell(), title,
							msg)) {
						update(e, getProgressMonitor());
						setPartName(getElement().getName());
						firePropertyChange(PROP_DIRTY);
					} else {
						firePropertyChange(PROP_DIRTY);
					}
				} else {
					update(e, getProgressMonitor());
				}
				scheduleCheckReferences();
			}
		});
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT,
				ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT,
				ITextEditorActionConstants.SHIFT_LEFT);

		addGoToDeclarationAction(menu);
		addFormatAction(menu);
	}

	private void addFormatAction(IMenuManager menu) {
		removeActionActivationCode(FORMAT_ACTION);
		FormatAction action = new FormatAction(doGetSourceViewer());
		action.setActionDefinitionId(FORMAT_ACTION);
		setAction(FORMAT_ACTION, action);
		if (menu != null)
			addAction(menu, ITextEditorActionConstants.GROUP_EDIT, FORMAT_ACTION);
	}

	private void addGoToDeclarationAction(IMenuManager menu) {
		removeActionActivationCode(GO_TO_DECLARATION_ACTION);
		EclOpenDeclarationAction action = new EclOpenDeclarationAction(this);
		action.setActionDefinitionId(GO_TO_DECLARATION_ACTION);
		setAction(GO_TO_DECLARATION_ACTION, action);

		if (menu != null) {
			addGroup(menu, ITextEditorActionConstants.GROUP_EDIT, GO_TO_DECLARATION_GROUP);
			addAction(menu, GO_TO_DECLARATION_GROUP, GO_TO_DECLARATION_ACTION);
		}
	}

	private static final String GO_TO_DECLARATION_ACTION = "org.eclipse.rcptt.ui.actions.open.declaration";
	private static final String GO_TO_DECLARATION_GROUP = "GROUP_DECLARATION";
	private static final String FORMAT_ACTION = "org.eclipse.rcptt.ui.edit.text.ecl.format.source";

	@Override
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);

		if (viewer != null && viewer.getTextWidget() != null)
			viewer.getTextWidget().setFocus();

		ToggleBreakpointAction action1 = new ToggleBreakpointAction(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart(), getViewer().getDocument(), getVerticalRuler());
		action1.update();
		menu.appendToGroup("debug", action1);

		ToggleBreakpointEnablementAction action2 = new ToggleBreakpointEnablementAction(
				viewer, getVerticalRuler());

		action2.update();
		// if (action2.isEnabled()) {
		menu.appendToGroup("debug", action2);
		// }
	}

	public void showSearchMatch(int searchFor, int offset, int length) {
		editorContent.showSearchMatch(searchFor, offset, length);
	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.eclipse.rcptt.ui.eclEditor" });
	}

	public IPreferenceStore doGetPreferenceStore() {
		return getPreferenceStore();
	}

	public TextViewer doGetSourceViewer() {
		return (TextViewer) getSourceViewer();
	}

	public int doWidgetOffset2ModelOffset(TextViewer viewer, int caretOffset) {
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
			return extension.widgetOffset2ModelOffset(caretOffset);
		}
		return caretOffset + viewer.getVisibleRegion().getOffset();
	}

	public int doModelOffset2WidgetOffset(TextViewer viewer, int modelOffset) {
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
			return extension.modelOffset2WidgetOffset(modelOffset);
		}
		return modelOffset - viewer.getVisibleRegion().getOffset();
	}

	protected void setEclContext(NamedElement element, String value) {
		if (element instanceof Scenario) {
			Scenarios.setEclContent((Scenario) element, value);
		}
	}

	protected String getEclContext(NamedElement element) {
		if (element instanceof Scenario) {
			return Scenarios.getScriptContent((Scenario) element);
		}
		return "";
	}

	public void setTags(String tags) {
		if (element != null) {
			element.setTags(tags);
		}
	}

	public Object getReconcilerLock() {
		return reconcilerLock;
	}

	public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets";
	public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor";

	@Override
	protected void configureSourceViewerDecorationSupport(
			SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
		char[] matchChars = { '[', ']', '{', '}' }; // which brackets
													// to match
		ICharacterPairMatcher matcher = new DefaultCharacterPairMatcher(
				matchChars, IDocumentExtension3.DEFAULT_PARTITIONING);
		support.setCharacterPairMatcher(matcher);
		support.setMatchingCharacterPainterPreferenceKeys(
				EDITOR_MATCHING_BRACKETS, EDITOR_MATCHING_BRACKETS_COLOR);

		// Enable bracket highlighting in the preference store
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(EDITOR_MATCHING_BRACKETS, true);
		store.setDefault(EDITOR_MATCHING_BRACKETS_COLOR, "128,128,128");
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		EnhancedSourceViewer enhancedSourceview = (EnhancedSourceViewer) getSourceViewer();
		if (enhancedSourceview == null) {
			return;
		}
		ContentAssistant currentContentAssistant = (ContentAssistant) enhancedSourceview.getContentAssistant();

		String propertyToChange = event.getKey();

		if (propertyToChange.compareTo(ECLEditorPlugin.ECL_CONTENT_ASSIST_ACTIVATIONSTATE) == 0) {
			currentContentAssistant.enableAutoActivation(ECLEditorPlugin.getECLScriptContentAssistActivationState());
		} else if (propertyToChange.compareTo(ECLEditorPlugin.ECL_CONTENT_ASSIST_DELAY) == 0) {
			currentContentAssistant.setAutoActivationDelay(ECLEditorPlugin.getECLScriptContentAssistDelay());
		} else if (propertyToChange.compareTo(ECLEditorPlugin.ECL_CONTENT_ASSIST_TRIGGERS) == 0) {
			IContentAssistProcessor generalContentAssistProcessor = new EclContentAssistProcessor();
			currentContentAssistant.setContentAssistProcessor(generalContentAssistProcessor,
					IDocument.DEFAULT_CONTENT_TYPE);
		}
	}
}
