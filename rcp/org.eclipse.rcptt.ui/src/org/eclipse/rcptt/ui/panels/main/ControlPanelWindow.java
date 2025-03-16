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
package org.eclipse.rcptt.ui.panels.main;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BlockTextSelection;
import org.eclipse.jface.text.IBlockTextSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.recording.CommandSet;
import org.eclipse.rcptt.core.recording.IRecordingListener;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.core.model.Q7InternalTestCase;
import org.eclipse.rcptt.internal.launching.EclStackTrace;
import org.eclipse.rcptt.internal.launching.ExecutionStatus;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.ui.controls.ContextsTable;
import org.eclipse.rcptt.ui.controls.DescriptionComposite;
import org.eclipse.rcptt.ui.controls.EmbeddedTabFolder;
import org.eclipse.rcptt.ui.controls.OptionsComposite;
import org.eclipse.rcptt.ui.controls.ScriptComposite;
import org.eclipse.rcptt.ui.controls.StatusBarComposite;
import org.eclipse.rcptt.ui.controls.VerificationsTable;
import org.eclipse.rcptt.ui.dialogs.DialogUtil;
import org.eclipse.rcptt.ui.editors.ecl.EclSourceViewer;
import org.eclipse.rcptt.ui.panels.ActionMenuCreator;
import org.eclipse.rcptt.ui.panels.Actions;
import org.eclipse.rcptt.ui.panels.assertion.AssertionPanelWindow;
import org.eclipse.rcptt.ui.recording.RecordingContextManager;
import org.eclipse.rcptt.ui.recording.RecordingSupport;
import org.eclipse.rcptt.ui.recording.RecordingSupport.RecordingMode;
import org.eclipse.rcptt.ui.utils.WorkbenchUtils;
import org.eclipse.rcptt.ui.utils.WriteAccessChecker;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;

@SuppressWarnings("restriction")
public class ControlPanelWindow extends Dialog {
	private static final String SETTINGS_KEY = "ControlPanelWindow"; //$NON-NLS-1$

	private final DataBindingContext dbc = new DataBindingContext();
	private final RecordingSupport recordingSupport;
	private final Shell parentShell;

	private final Scenario scenario = ScenarioFactory.eINSTANCE
			.createScenario();
	private final WritableValue<ITestCase> testCase = new WritableValue<>(null,
			ITestCase.class);

	private AssertionPanelWindow assertionWindow;
	private EmbeddedTabFolder tabFolder;
	private CoolBar coolBar;
	private StatusBarComposite statusBar;
	private ContextsTable contextsTable;

	private final ShellAdapter closeListener = new ParentShellCloseListener();
	private final Listener keyListener = new RecordingShortcutListener();

	private VerificationsTable verificationsTable;

	private static ControlPanelWindow window;

	public static synchronized ControlPanelWindow openControlPanel(
			Shell parentShell, ITestCase testCase) throws ModelException {
		if (window != null && window.getShell() != null
				&& !window.getShell().isDisposed()) {
			if (!window.close()) {
				return null;
			}
		}
		window = new ControlPanelWindow(parentShell, testCase);
		window.setBlockOnOpen(false);
		window.open();
		return window;
	}

	public static synchronized ControlPanelWindow getOpenedControlPanel() {
		if (window != null && window.getShell() != null
				&& !window.getShell().isDisposed()) {
			return window;
		}
		return null;
	}

	private ControlPanelWindow(Shell parentShell, ITestCase testCase)
			throws ModelException {
		super((Shell) null);

		this.parentShell = parentShell;
		setShellStyle(SWT.RESIZE /* | SWT.TOOL */| SWT.CLOSE /* | SWT.ON_TOP */);
		if (testCase != null) {
			setModel(testCase);
			copyContent((Scenario) testCase.getNamedElement(), this.scenario);
		}
		recordingSupport = new RecordingSupport(this, this.scenario,
				testCase != null ? (Scenario) testCase.getNamedElement() : null);
		recordingSupport.observeMode().addChangeListener(
				new RecordingModeListener());
		recordingSupport.addRecordingListener(new AssertAddedListener());
	}

	public RecordingSupport getRecordingSupport() {
		return recordingSupport;
	}

	private ITestCase getModel() {
		return (ITestCase) testCase.getValue();
	}

	private void setModel(ITestCase value) {
		testCase.setValue(value);
	}

	@Override
	public Shell getParentShell() {
		return null;
	}

	@Override
	public int open() {
		final int result = super.open();
		parentShell.setMinimized(true);
		getShell().setVisible(true);
		recordingSupport.setMode(RecordingMode.Connecting);
		return result;
	}

	@Override
	protected Control createContents(Composite parent) {
		Q7UIPlugin.getDisplay().addFilter(SWT.KeyUp, keyListener);

		createToolBar(parent);
		createTabFolder(parent);

		ITestCase model = getModel();
		Q7InternalTestCase internalModel = new Q7InternalTestCase(
				ModelManager.getModelManager().getModel(),
				"recording_snippet", scenario);

		scriptPart = new ScriptComposite(internalModel,
				model != null ? (IFile) getModel().getResource() : null,
				getShell(), SWT.SCROLL_LOCK);
		ComputedValue<RecordingMode> recordingMode = new ComputedValue<RecordingMode>() {
			@Override
			protected RecordingMode calculate() {
				return recordingSupport.getMode();
			}
		};
		dbc.bindValue(scriptPart.observeRecordingMode(), recordingMode);
		script = tabFolder.addTab(scriptPart);
		try {
			contextsTable = new ContextsTable(internalModel);
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
		}
		contextsTable.setProject(getSavedProject());
		dbc.bindValue(contextsTable.observeRecordingMode(), recordingMode);
		tabFolder.addTab(contextsTable);

		try {
			verificationsTable = new VerificationsTable(internalModel);
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
		}
		verificationsTable.setProject(getSavedProject());
		dbc.bindValue(verificationsTable.observeRecordingMode(), recordingMode);
		tabFolder.addTab(verificationsTable);

		DescriptionComposite descPart = new DescriptionComposite(scenario);
		dbc.bindValue(descPart.observeRecordingMode(), recordingMode);
		tabFolder.addTab(descPart);
		OptionsComposite optionsComposite = new OptionsComposite();
		tabFolder.addTab(optionsComposite);
		optionsComposite.setOptionChangeCallback(new Runnable() {
			public void run() {
				if (recordingSupport.getMode() == RecordingMode.Recording) {
					recordingSupport.sendFeatures();
				}
			}
		});

		selectTab(script);

		statusBar = new StatusBarComposite() {
			@Override
			protected void layout() {
				super.layout();
				tabFolder.layout(true);
			}

			@Override
			protected void handleError(final String taskMessage, Throwable e) {
				if (e instanceof CoreException
						&& (e.getMessage().equals("Connection reset") || e //$NON-NLS-1$
								.getMessage().equals("Connection refused: connect"))) { //$NON-NLS-1$
					return; // Ignore connection lost exception, this situation
							// handled in RecordingSupport class
				}
				if (e instanceof CoreException) {
					final IStatus status = ((CoreException) e).getStatus();
					if (status instanceof ExecutionStatus
							&& PlatformUI.isWorkbenchRunning()) {
						WorkbenchUtils.uiRun(new Runnable() {
							public void run() {
								displayExecutionFailure((ExecutionStatus) status);
							}
						});
						return;
					}
				}
				super.handleError(taskMessage, e);
			};
		};
		statusBar.createControl(parent);
		// statusBar.getControl().addKeyListener(keyListener);
		dbc.bindValue(org.eclipse.jface.databinding.swt.typed.WidgetProperties.text().observe(getShell()),
				new ComputedValue<String>() {
					@Override
					protected String calculate() {
						StringBuilder text = new StringBuilder(
								Messages.ControlPanelWindow_Title);
						ITestCase model = getModel();
						if (model != null && model.exists()) {
							try {
								text.append(" - ").append(model.getElementName()); //$NON-NLS-1$
								text.append(" (") //$NON-NLS-1$
										.append(model.getQ7Project().getName())
										.append(")"); //$NON-NLS-1$
							} catch (ModelException e) {
								Q7UIPlugin.log(e);
							}
						}
						AutLaunch aut = recordingSupport.getAUT();
						if (aut != null) {
							text.append(" - ").append(aut.getAut().getName()); //$NON-NLS-1$
						}
						switch (recordingSupport.getMode()) {
						case Connecting:
							text.append(Messages.ControlPanelWindow_StatusConnection);
							break;
						case Recording:
							text.append(Messages.ControlPanelWindow_StatusRecording);
							break;
						case Asserting:
							text.append(Messages.ControlPanelWindow_StatusAsserting);
							break;
						case ImageRecognition:
							text.append(Messages.ControlPanelWindow_StatusImageRecognition);
							break;
						case Replaying:
							text.append(Messages.ControlPanelWindow_StatusReplaying);
							break;
						case WaitingForAUTRestart:
							text.append(Messages.ControlPanelWindow_StatusWaitingAUTToRestart);
							break;
						default:
							break;
						}
						return text.toString();
					}
				});

		tabFolder.setFocus();

		return tabFolder;
	}

	private void displayExecutionFailure(final ExecutionStatus status) {
		if (ExecutionErrorDialog.showExecutionError(
				ControlPanelWindow.this.getShell(), status)) {
			selectTab(script);
			EclSourceViewer viewer = scriptPart.getViewer();
			int line = status.getLine();
			int length = status.getLength();
			int column = status.getColumn();
			IBlockTextSelection selection = new BlockTextSelection(
					viewer.getDocument(), line - 1, column - 1, line - 1,
					column + length - 1, 0);
			viewer.setSelection(selection, true);
		}
	}

	private CTabFolder createTabFolder(Composite parent) {
		final Composite body = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0)
				.applyTo(body);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(body);

		tabFolder = new EmbeddedTabFolder(body, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(tabFolder);

		IPreferenceStore store = PlatformUI.getPreferenceStore();
		int minimumCharacters = store
				.getInt(IWorkbenchPreferenceConstants.VIEW_MINIMUM_CHARACTERS);
		if (minimumCharacters >= 0) {
			tabFolder.setMinimumCharacters(minimumCharacters);
		}

		tabFolder.setUnselectedCloseVisible(false);
		tabFolder.setUnselectedImageVisible(true);

		// ThemeManagerAdapter themePreferences = new ThemeManagerAdapter(
		// PlatformUI.getWorkbench().getThemeManager());
		//
		// DefaultThemeListener themeListener = new DefaultThemeListener(
		// tabFolder, themePreferences);
		// themePreferences.addListener(themeListener);
		// PreferenceStoreAdapter apiPreferences = new PreferenceStoreAdapter(
		// PrefUtil.getAPIPreferenceStore());
		// new DefaultSimpleTabListener(apiPreferences,
		// IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS,
		// tabFolder);

		final Color c1 = new Color(Display.getCurrent(), 232, 238, 244);
		final Color c2 = new Color(Display.getCurrent(), 153, 180, 209);
		CTabFolder control = (CTabFolder) tabFolder;
		control.setSelectionBackground(new Color[] { c1, c2 },
				new int[] { 100 }, true);
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				c1.dispose();
				c2.dispose();
			}
		});

		// tabFolder.getControl().addKeyListener(keyListener);
		return tabFolder;
	}

	private void createToolBar(Composite parent) {
		final Composite toolbarComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(toolbarComposite);
		toolbarComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));

		CoolBarManager coolBarManager = new CoolBarManager(SWT.HORIZONTAL
				| SWT.FLAT);
		{
			ToolBarManager manager = new ToolBarManager(SWT.HORIZONTAL
					| SWT.FLAT);
			IAction homeAction = new Action() {
				@Override
				public void run() {
					close();
				}
			};
			homeAction
					.setToolTipText(Messages.ControlPanelWindow_ReturnActionTooltip);
			homeAction.setImageDescriptor(Images
					.getImageDescriptor(Images.PANEL_HOME));
			manager.add(homeAction);

			IAction saveAction = new Action() {
				@Override
				public void run() {
					if (getModel() != null) {
						save();
					} else {
						saveAs();
					}
				}
			};

			saveAction.setMenuCreator(new ActionMenuCreator() {
				@Override
				protected void fill(MenuManager manager) {
					manager.add(new Action(
							Messages.ControlPanelWindow_SaveAction) {
						{
							dbc.bindValue(Actions.observeEnabled(this),
									new ComputedValue<Boolean>() {
										@Override
										protected Boolean calculate() {
											return getModel() != null;
										}
									});
						}

						@Override
						public void run() {
							save();
						}
					});
					manager.add(new Action(
							Messages.ControlPanelWindow_SaveAsAction) {
						@Override
						public void run() {
							saveAs();
						}
					});
				}
			});
			dbc.bindValue(Actions.observeToolTipText(saveAction),
					new ComputedValue<String>() {
						@Override
						protected String calculate() {
							if (getModel() != null) {
								return Messages.ControlPanelWindow_SaveAction;
							} else {
								return Messages.ControlPanelWindow_SaveAsAction;
							}
						}
					});
			ISharedImages sharedImages = WorkbenchPlugin.getDefault()
					.getSharedImages();
			saveAction.setImageDescriptor(sharedImages
					.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
			saveAction
					.setDisabledImageDescriptor(sharedImages
							.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT_DISABLED));
			manager.add(saveAction);
			coolBarManager.add(manager);
		}
		{
			ToolBarManager manager = new ToolBarManager(SWT.HORIZONTAL
					| SWT.FLAT);
			manager.add(createRecordAction());
			manager.add(createReplayAction());
			coolBarManager.add(manager);
		}
		{
			ToolBarManager manager = new ToolBarManager(SWT.HORIZONTAL
					| SWT.FLAT);
			manager.add(createRecordingModeAction());
			manager.add(createAssertingModeAction());
			if (Q7UIPlugin.isImageRecognitionAllowed()) {
				manager.add(createImageRecognitionModeAction());
			}
			coolBarManager.add(manager);
		}
		coolBar = coolBarManager.createControl(toolbarComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(coolBar);
		coolBar.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				getShell().layout();
			}
		});
	}

	private IAction createReplayAction() {
		IAction action = new Action() {
			@Override
			public void run() {
				if (recordingSupport.getMode() == RecordingMode.Stopped) {
					recordingSupport.setMode(RecordingMode.Replaying);
				} else {
					recordingSupport.setMode(RecordingMode.Stopped);
				}
			};
		};
		dbc.bindValue(Actions.observeEnabled(action), new ComputedValue<Boolean>(
				Boolean.TYPE) {
			@Override
			protected Boolean calculate() {
				RecordingMode mode = recordingSupport.getMode();
				return mode == RecordingMode.Stopped
						|| mode == RecordingMode.Replaying;
			}
		});
		dbc.bindValue(Actions.observeImageDescriptor(action),
				new ComputedValue<ImageDescriptor>() {
					@Override
					protected ImageDescriptor calculate() {
						if (recordingSupport.getMode() == RecordingMode.Replaying) {
							return Images.getImageDescriptor(Images.STOP);
						} else {
							return Images.getImageDescriptor(Images.PLAY);
						}
					}
				});
		dbc.bindValue(Actions.observeToolTipText(action), new ComputedValue<String>() {
			@Override
			protected String calculate() {
				if (recordingSupport.getMode() == RecordingMode.Replaying) {
					return Messages.ControlPanelWindow_StopAction;
				} else {
					return Messages.ControlPanelWindow_ReplayAction;
				}
			}
		});
		return action;
	}

	private IAction createRecordAction() {
		IAction action = new Action() {
			@Override
			public void run() {
				if (recordingSupport.getMode() == RecordingMode.Stopped) {
					recordingSupport.setMode(RecordingMode.Connecting);
				} else {
					recordingSupport.setMode(RecordingMode.Stopped);
				}
			};
		};
		dbc.bindValue(Actions.observeEnabled(action), new ComputedValue<Boolean>(
				Boolean.TYPE) {
			@Override
			protected Boolean calculate() {
				RecordingMode mode = recordingSupport.getMode();
				return mode == RecordingMode.Stopped
						|| mode != RecordingMode.Replaying;
			}
		});
		dbc.bindValue(Actions.observeImageDescriptor(action),
				new ComputedValue<ImageDescriptor>() {
					@Override
					protected ImageDescriptor calculate() {
						RecordingMode mode = recordingSupport.getMode();
						if (mode == RecordingMode.Stopped
								|| mode == RecordingMode.Replaying) {
							return Images.getImageDescriptor(Images.RECORD);
						} else {
							return Images.getImageDescriptor(Images.STOP);
						}
					}
				});
		dbc.bindValue(Actions.observeToolTipText(action), new ComputedValue<String>() {
			@Override
			protected String calculate() {
				RecordingMode mode = recordingSupport.getMode();
				if (mode == RecordingMode.Recording
						|| mode == RecordingMode.Connecting) {
					return Messages.ControlPanelWindow_StopAction;
				} else {
					return Messages.ControlPanelWindow_RecordAction;
				}
			}
		});
		return action;
	}

	private final ComputedValue<Boolean> modeEnablementObservable = new ComputedValue<Boolean>(
			Boolean.TYPE) {
		@Override
		protected Boolean calculate() {
			RecordingMode mode = recordingSupport.getMode();
			return mode != RecordingMode.Stopped
					&& mode != RecordingMode.Replaying
					&& mode != RecordingMode.Connecting;
		}
	};

	private CTabItem script;

	private ScriptComposite scriptPart;

	private IAction createRecordingModeAction() {
		IAction action = new Action("", IAction.AS_RADIO_BUTTON) {}; //$NON-NLS-1$
		action.setImageDescriptor(Images
				.getImageDescriptor(Images.PANEL_MODE_RECORD));
		action.setToolTipText(Messages.ControlPanelWindow_SwitchToRecordModeActionToolTip);
		dbc.bindValue(Actions.observeEnabled(action), modeEnablementObservable);
		dbc.bindValue(Actions.observeChecked(action), new ComputedValue<Boolean>(
				Boolean.TYPE) {
			@Override
			protected Boolean calculate() {
				return recordingSupport.getMode() == RecordingMode.Recording;
			}
			
			@Override
			protected void doSetValue(Boolean value) {
				if (value) {
					recordingSupport.setMode(RecordingMode.Recording);
				}
			}
		});
		return action;
	}

	private IAction createAssertingModeAction() {
		IAction action = new Action("", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
		};
		action.setImageDescriptor(Images
				.getImageDescriptor(Images.PANEL_MODE_ASSERT));
		action.setToolTipText(Messages.ControlPanelWindow_SwitchToAssertModeActionToolTip);
		dbc.bindValue(Actions.observeEnabled(action), modeEnablementObservable);
		dbc.bindValue(Actions.observeChecked(action), new ComputedValue<Boolean>(
				Boolean.TYPE) {
			@Override
			protected Boolean calculate() {
				return recordingSupport.getMode() == RecordingMode.Asserting;
			}
			@Override
			protected void doSetValue(Boolean value) {
				if (value) {
					recordingSupport.setMode(RecordingMode.Asserting);
				}
			}
		});
		return action;
	}

	private IAction createImageRecognitionModeAction() {
		IAction action = new Action("", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
		};
		action.setImageDescriptor(Images
				.getImageDescriptor(Images.PANEL_MODE_IML));
		action.setToolTipText(Messages.ControlPanelWindow_SwitchToRecognitionModeActionToolTip);
		dbc.bindValue(Actions.observeEnabled(action), modeEnablementObservable);
		dbc.bindValue(Actions.observeChecked(action), new ComputedValue<Boolean>(
				Boolean.TYPE) {
			@Override
			protected Boolean calculate() {
				return recordingSupport.getMode() == RecordingMode.ImageRecognition;
			}
			@Override
			protected void doSetValue(Boolean value) {
				if (value) {
					recordingSupport.setMode(RecordingMode.ImageRecognition);
				}
			}
		});
		return action;
	}

	private boolean isDirty() {
		ITestCase model = getModel();
		if (model != null && model.exists()) {
			try {
				Scenario saved = (Scenario) model.getNamedElement();
				if (saved != null) {
					if (!EcoreUtil.equals(saved.getContent(),
							scenario.getContent()))
						return true;

					// special case when underlying Script model object is still
					// not created
					if (saved.getContent() == null) {
						if (!EcoreUtil.equals(saved.getTeslaContent(),
								scenario.getTeslaContent()))
							return true;
					}

					if (saved.getContexts().size() != scenario.getContexts()
							.size())
						return true;
					int size = saved.getContexts().size();
					for (int i = 0; i < size; i++) {
						if (!saved.getContexts().get(i)
								.equals(scenario.getContexts().get(i)))
							return true;
					}

					if (saved.getVerifications().size() != scenario.getVerifications()
							.size())
						return true;
					size = saved.getVerifications().size();
					for (int i = 0; i < size; i++) {
						if (!saved.getVerifications().get(i)
								.equals(scenario.getVerifications().get(i)))
							return true;
					}

					String savedDesc = saved.getDescription();
					String desc = scenario.getDescription();
					if (savedDesc == null && desc != null)
						return true;
					if (savedDesc != null && !savedDesc.equals(desc))
						return true;
					return false;
				}
				return model.hasUnsavedChanges();
			} catch (ModelException e) {
				Q7UIPlugin.log(e);
			}
		}
		String content = Scenarios.getScriptContent(scenario);
		return content != null && content.length() > 0;
	}

	private IProject getSavedProject() {
		ITestCase model = getModel();
		if (model == null)
			return null;
		return model.getQ7Project().getProject();
	}

	private boolean saveAs() {
		IQ7Folder folder = null;
		if (getModel() != null) {
			IQ7Element parent = getModel().getParent();
			if (parent != null && parent.exists()
					&& parent instanceof IQ7Folder) {
				folder = (IQ7Folder) parent;
			}
		}
		ITestCase newScenario = DialogUtil.openNewScenarioDialog(getShell(),
				false, folder);
		if (newScenario == null)
			return false;
		IFile file = (IFile) newScenario.getResource();
		if (file == null)
			return false;
		IQ7Element newModel = RcpttCore.create(file);
		if (newModel == null)
			return false;
		setModel((ITestCase) newModel);
		save();
		return true;
	}

	private void save() {
		try {
			ITestCase model = getModel();
			NullProgressMonitor monitor = new NullProgressMonitor();
			if (model.exists()) {
				model = (ITestCase) model.getWorkingCopy(monitor);
				copyContent(scenario, (Scenario) model.getNamedElement());
				WriteAccessChecker writeAccessChecker = new WriteAccessChecker(getShell());

				try {
					if (!writeAccessChecker.makeResourceWritable(model)) {
						return;
					}
					model.commitWorkingCopy(true, monitor);
				} catch (CoreException e) {
					Q7UIPlugin.log(e);
				} finally {
					model.discardWorkingCopy();
				}
				contextsTable.setProject(getSavedProject());
				verificationsTable.setProject(getSavedProject());
				copyContent((Scenario) model.getNamedElement(), this.scenario);
			} else {
				MessageDialog dialog = new MessageDialog(
						getShell(),
						Messages.ControlPanelWindow_SaveDialogTitle,
						null,
						"Failed to save testcase because underlying resources is not exist.",
						MessageDialog.ERROR, new String[] {
								IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL,
								IDialogConstants.CANCEL_LABEL }, 0);
				int value = dialog.open(); // Number of pressed button
				if (value == 0) {
					saveAs();
				}
			}
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
		}
	}

	private void copyContent(Scenario source, Scenario target) {
		target.setContent(EcoreUtil.copy(source.getContent()));
		target.setTeslaContent(EcoreUtil.copy(source.getTeslaContent()));
		target.getAttachments().clear();
		target.getAttachments().addAll(
				EcoreUtil.copyAll(source.getAttachments()));
		target.getContexts().clear();
		target.getContexts().addAll(source.getContexts());
		target.getVerifications().clear();
		target.getVerifications().addAll(source.getVerifications());
		target.setDescription(source.getDescription());
	}

	private boolean openSaveDialog() {
		if (getModel() != null && !getModel().exists()) {
			return true;
		}
		MessageDialog dialog = new MessageDialog(getShell(),
				Messages.ControlPanelWindow_SaveDialogTitle, null,
				Messages.ControlPanelWindow_SaveDialogMsg,
				MessageDialog.QUESTION_WITH_CANCEL, new String[] {
						IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		int returnCode = dialog.open(); // Number of pressed button
		if (returnCode == 0) {
			if (getModel() != null) {
				save();
			} else {
				if (!saveAs()) {
					return false;
				}
			}
		}
		switch (returnCode) {
		case 2:
			return false; // User clicked Cancel button
		default:
			return true;
		}
	}

	private void selectTab(CTabItem tab) {
		tabFolder.setSelection(tab);
	}



	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		final IDialogSettings root = Q7UIPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings section = root.getSection(SETTINGS_KEY);
		if (section == null) {
			section = root.addNewSection(SETTINGS_KEY);
		}
		return section;
	}

	private boolean hasDialogBoundsSettings() {
		final IDialogSettings root = Q7UIPlugin.getDefault()
				.getDialogSettings();
		return root.getSection(SETTINGS_KEY) != null;
	}

	@Override
	public boolean close() {
		recordingSupport.setMode(RecordingMode.Stopped);
		if (isDirty()) {
			if (!openSaveDialog()) {
				return false;
			}
		}
		final boolean result = super.close();
		if (result && !parentShell.isDisposed()) {
			dbc.dispose();
			parentShell.removeShellListener(closeListener);
			parentShell.setMinimized(false);
			try {
				ShellUtilsProvider.getShellUtils().forceActive(parentShell);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
			parentShell.setFocus();
			ITestCase model = getModel();
			if (model != null && model.exists()) {
				IFile location = (IFile) model.getResource();
				if (location != null) {
					try {
						IDE.openEditor(Q7UIPlugin.getActiveWindow()
								.getActivePage(), location);
					} catch (PartInitException e) {
						Q7UIPlugin.log(e);
					}
				}
			}
		}
		RecordingContextManager.Instance.deactivateContext();
		return result;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		GridLayoutFactory.fillDefaults().equalWidth(true).spacing(0, 0)
				.margins(0, 0).applyTo(newShell);
		parentShell.addShellListener(closeListener);
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		if (hasDialogBoundsSettings()) {
			return super.getInitialLocation(initialSize);
		} else {
			return new Point(0, 0);
		}
	}

	@Override
	protected Point getInitialSize() {
		if (hasDialogBoundsSettings()) {
			return super.getInitialSize();
		} else {
			Rectangle bounds = parentShell.getDisplay().getBounds();
			int height = bounds.height / 2 - bounds.height / 10;
			return new Point(580, height);
		}
	}

	public void runWithProgress(String name, IRunnableWithProgress runnable,
			boolean fork) {
		statusBar.runWithProgress(name, runnable, fork);
	}

	private final class ParentShellCloseListener extends ShellAdapter {
		@Override
		public void shellClosed(ShellEvent e) {
			close();
		}
	}

	private final class RecordingModeListener implements IChangeListener {
		public void handleChange(ChangeEvent event) {
			Q7UIPlugin.getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateStatusBar();
					updateAssetionWindow();
				}
			});
		}

		private void updateStatusBar() {
			if (statusBar != null && statusBar.getControl() != null
					&& !statusBar.getControl().isDisposed()) {
				StringBuilder message = new StringBuilder();

				message.append("Press ");
				switch (recordingSupport.getMode()) {
				case Recording:
					TriggerSequence[] shortcuts = RecordingContextManager.Instance
							.getAssertModeShortcuts();
					if (shortcuts != null && shortcuts.length > 0) {
						message.append(Messages
								.bind(Messages.ControlPanelWindow_AssertShortcutToolTip,
										shortcuts[0].format()));
						message.append(", ").append(getStopRecordShortcutMessage());
					}
					break;
				case Asserting:
					shortcuts = RecordingContextManager.Instance
							.getRecordModeShortcuts();
					if (shortcuts != null && shortcuts.length > 0) {
						message.append(Messages
								.bind(Messages.ControlPanelWindow_RecordShortcutToolTip,
										shortcuts[0].format()));
						message.append(", ").append(getStopRecordShortcutMessage());
						break;
					}
				case Stopped:
					TriggerSequence[] startShortcuts = RecordingContextManager.Instance
							.getStartRecordShortcuts();
					if (startShortcuts != null && startShortcuts.length > 0) {
						message.append(Messages
								.bind(Messages.ControlPanelWindow_StartRecordShortcutToolTip,
										startShortcuts[0].format()));
						message.append(", ");
					}
					TriggerSequence[] replayShortcuts = RecordingContextManager.Instance
							.getReplayShortcuts();
					if (replayShortcuts != null && replayShortcuts.length > 0) {
						message.append(Messages
								.bind(Messages.ControlPanelWindow_ReplayShortcutToolTip,
										replayShortcuts[0].format()));
					}
					break;
				default:
					statusBar.showMessage(null);
					return;
				}
				statusBar.showMessage(message.toString());
			}
		}

		private String getStopRecordShortcutMessage() {
			TriggerSequence[] shortcuts = RecordingContextManager.Instance
					.getStopRecordShortcuts();
			if (shortcuts != null && shortcuts.length > 0) {
				return Messages.bind(Messages.ControlPanelWindow_StopRecordShortcutToolTip,
						shortcuts[0].format());
			}
			return "";
		}

		private void updateAssetionWindow() {
			switch (recordingSupport.getMode()) {
			case Asserting:
				break;
			default:
				if (assertionWindow != null) {
					assertionWindow.close();
					assertionWindow = null;
				}
				break;
			}
		}
	}

	private final class AssertAddedListener implements IRecordingListener {
		public void handleContainerChange() {
		}

		public void disconnected() {
		}

		public void recordMode() {
		}

		public void assertMode() {
		}

		public void startRecord() {
		}

		public void stopRecord() {
		}

		public void replay() {
		}

		public void assertAdded(final CommandSet commands) {
			Q7UIPlugin.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (commands != null) {
						if (assertionWindow == null
								|| assertionWindow.getShell() == null
								|| assertionWindow.getShell().isDisposed()) {
							assertionWindow = new AssertionPanelWindow(
									recordingSupport, getShell());
						}
						assertionWindow.setInput(commands);
					}
				}
			});
		}
	}

	private final class RecordingShortcutListener implements Listener {
		public void handleEvent(Event event) {
			if (recordingSupport != null) {
				int accelerator = SWTKeySupport
						.convertEventToUnmodifiedAccelerator(event);
				KeySequence sequence = KeySequence.getInstance(SWTKeySupport
						.convertAcceleratorToKeyStroke(accelerator));
				switch (recordingSupport.getMode()) {
				case Recording:
					TriggerSequence[] assertShortcuts = RecordingContextManager.Instance
							.getAssertModeShortcuts();
					if (assertShortcuts != null) {
						for (TriggerSequence s : assertShortcuts) {
							if (sequence.equals(s)) {
								recordingSupport
										.setMode(RecordingMode.Asserting);
							}
						}
					}
					handleStopRecordShortcutPressed(sequence);
					break;
				case Asserting:
					TriggerSequence[] recordShortcuts = RecordingContextManager.Instance
							.getRecordModeShortcuts();
					if (recordShortcuts != null) {
						for (TriggerSequence s : recordShortcuts) {
							if (sequence.equals(s)) {
								recordingSupport
										.setMode(RecordingMode.Recording);
							}
						}
					}
					handleStopRecordShortcutPressed(sequence);
					break;
				case Stopped:
					TriggerSequence[] startShortcuts = RecordingContextManager.Instance
							.getStartRecordShortcuts();
					if (startShortcuts != null) {
						for (TriggerSequence s : startShortcuts) {
							if (sequence.equals(s)) {
								recordingSupport
										.setMode(RecordingMode.Connecting);
							}
						}
					}
					TriggerSequence[] replayShortcuts = RecordingContextManager.Instance
							.getReplayShortcuts();
					if (replayShortcuts != null) {
						for (TriggerSequence s : replayShortcuts) {
							if (sequence.equals(s)) {
								recordingSupport
										.setMode(RecordingMode.Replaying);
							}
						}
					}
					break;
				case Connecting:
				case ImageRecognition:
				case Replaying:
				case WaitingForAUTRestart:
				default:
					break;
				}

			}
		}
	}

	private void handleStopRecordShortcutPressed(KeySequence sequence) {
		TriggerSequence[] shortcuts = RecordingContextManager.Instance
				.getStopRecordShortcuts();
		if (shortcuts != null) {
			for (TriggerSequence s : shortcuts) {
				if (sequence.equals(s)) {
					recordingSupport
							.setMode(RecordingMode.Stopped);
				}
			}
		}
	}

	private static final class ExecutionErrorDialog extends MessageDialog {

		private ExecutionErrorDialog(Shell parentShell, ExecutionStatus status) {
			super(parentShell, getTitle(status), null, getMessage(status),
					MessageDialog.ERROR, getDialogButtonsLabels(status), 0);
		}

		private static String getTitle(ExecutionStatus status) {
			IQ7NamedElement element = status.getElement();
			if (element != null) {
				if (element instanceof IContext) {
					try {
						return NLS
								.bind(Messages.ControlPanelWindow_ExecutionFailedDialogTitle_Context,
										element.getElementName());
					} catch (ModelException e) {
						Q7UIPlugin.log(e);
					}
				}
			}
			return Messages.ControlPanelWindow_ExecutionFailedDialogTitle_Script;
		}

		private static String getMessage(ExecutionStatus status) {
			return EclStackTrace.fromExecStatus(status).print();
		}

		private static String[] getDialogButtonsLabels(ExecutionStatus status) {
			if (status.hasLocation()) {
				return new String[] {
						Messages.ControlPanelWindow_ErrorLocationButton,
						IDialogConstants.OK_LABEL };
			}
			return new String[] { IDialogConstants.OK_LABEL };
		}

		private static boolean showExecutionError(Shell parentShell,
				ExecutionStatus status) {
			ExecutionErrorDialog dialog = new ExecutionErrorDialog(parentShell,
					status);
			return dialog.open() == 0 && status.hasLocation();
		}
	}
}
