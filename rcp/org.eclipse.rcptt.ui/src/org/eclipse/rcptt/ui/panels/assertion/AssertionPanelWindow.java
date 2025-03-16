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
package org.eclipse.rcptt.ui.panels.assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.rcptt.core.recording.CommandSet;
import org.eclipse.rcptt.ecl.core.BoxedValue;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.tesla.core.protocol.Assert;
import org.eclipse.rcptt.tesla.core.protocol.impl.AssertImpl;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.ui.Color;
import org.eclipse.rcptt.tesla.core.ui.DiagramItem;
import org.eclipse.rcptt.tesla.core.ui.UiPackage;
import org.eclipse.rcptt.tesla.core.ui.Widget;
import org.eclipse.rcptt.tesla.ecl.model.GetWidgetDetails;
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;
import org.eclipse.rcptt.tesla.internal.core.SimpleCommandPrinter;
import org.eclipse.rcptt.ui.panels.ActionToolbar;
import org.eclipse.rcptt.ui.panels.Actions;
import org.eclipse.rcptt.ui.panels.MenuToolbar;
import org.eclipse.rcptt.ui.recording.RecordingSupport;
import org.eclipse.rcptt.ui.recording.RecordingSupport.RecordingMode;
import org.eclipse.rcptt.ui.utils.ImageManager;
import org.eclipse.rcptt.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.ProgressManager;

@SuppressWarnings("restriction")
public class AssertionPanelWindow extends Dialog {

	private static final String SETTINGS_KEY = "AssertionPanelWindow"; //$NON-NLS-1$

	private final DataBindingContext dbc = new DataBindingContext();
	private final ImageManager imageManager = new ImageManager();
	private final WritableValue<Boolean> hasAssert = new WritableValue<Boolean>(false, Boolean.class);

	private final IAction collapseAll = new Action() {
		{
			setImageDescriptor(Images.getImageDescriptor(Images.PANEL_COLLAPSE_ALL));
			setToolTipText(Messages.AssertionPanelWindow_CollapseAllActionToolTip);
		}

		@Override
		public void run() {
			viewer.collapseAll();
		};
	};

	private final IAction expandAll = new Action() {
		{
			setImageDescriptor(Images.getImageDescriptor(Images.PANEL_EXPAND_ALL));
			setToolTipText(Messages.AssertionPanelWindow_ExpandAllActionToolTip);
		}

		@Override
		public void run() {
			expandAll(true);
		};
	};

	private final IAction selectAll = new Action(Messages.AssertionPanelWindow_SelectAllActionText) {
		{
			setImageDescriptor(Images.getImageDescriptor(Images.PANEL_SELECT_ALL));
			setDisabledImageDescriptor(Images.getImageDescriptor(Images.PANEL_SELECT_ALL_DISABLED));
			setToolTipText(Messages.AssertionPanelWindow_SelectAllActionToolTip);
		}

		@Override
		public void run() {
			for (AssertGroup obj : ((AssertInput) viewer.getInput()).getAsserts()) {
				checkedObservable.add(obj);
				checkedObservable.addAll(obj.getAsserts());
			}
		};
	};

	private final IAction deselectAll = new Action() {
		{
			setImageDescriptor(Images.getImageDescriptor(Images.PANEL_DESELECT_ALL));
			setDisabledImageDescriptor(Images.getImageDescriptor(Images.PANEL_DESELECT_ALL_DISABLED));
			setToolTipText(Messages.AssertionPanelWindow_DeselectAllActionToolTip);
		}

		@Override
		public void run() {
			checkedObservable.clear();
		};
	};

	private final RecordingSupport recordingSupport;
	private final Shell parentShell;

	private WidgetDetailsDialog widgetDetailsDialog;
	private CommandSet commands;
	private AssertInput currentInput;
	private IObservableSet<Object> checkedObservable;
	private Button appendButton;
	private List<TreeItem> collapsed;
	private List<TreeItem> references;
	private CCombo filterCombo;
	private Text filterText;
	private String filterValue = "";

	public AssertionPanelWindow(RecordingSupport recordingSupport, Shell parentShell) {
		super(parentShell);
		this.recordingSupport = recordingSupport;
		this.parentShell = parentShell;
		int style = SWT.RESIZE | SWT.CLOSE;
		if (!Platform.getOS().equals(Platform.OS_LINUX)) {
			style |= SWT.ON_TOP | SWT.TOOL;
		}
		setShellStyle(style);
	}

	@Override
	protected Shell getParentShell() {
		return parentShell;
	}

	protected boolean addImageAssert() {
		if (commands == null) {
			return false;
		}
		if (commands != null && commands.getImageCapture() == null) {
			return false;
		}
		AssertImageWindow win = new AssertImageWindow(getShell(), this);
		win.setInput(commands.getImageCapture());
		win.setBlockOnOpen(true);
		if (win.open() == Window.OK) {
			final Assert result = win.getAssert();
			result.setElement(EcoreUtil.copy(currentInput.getElement()));
			recordingSupport.getRecorder().addAsserts(commands, new Assert[] { result });
			return true;
		}
		return false;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.AssertionPanelWindow_Title);
		newShell.setLayout(new FillLayout());
		newShell.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.ESC) {
					closeAndContinueRecording();
				}
			}
		});
		newShell.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.character == SWT.ESC) {
					closeAndContinueRecording();
				}
			}
		});
		GridLayoutFactory.fillDefaults().equalWidth(true).spacing(0, 0).margins(0, 0).applyTo(newShell);

		newShell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				imageManager.dispose();
			}
		});
	}

	@Override
	public boolean close() {
		if (recordingSupport.getMode() == RecordingMode.Asserting) {
			recordingSupport.getRecorder().resetAssertSelection();
		}
		return super.close();
	}

	protected void closeAndContinueRecording() {
		close();
		recordingSupport.setMode(RecordingMode.Recording);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		final IDialogSettings root = Q7UIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = root.getSection(SETTINGS_KEY);
		if (section == null) {
			section = root.addNewSection(SETTINGS_KEY);
		}
		return section;
	}

	protected boolean hasDialogBoundsSettings() {
		final IDialogSettings root = Q7UIPlugin.getDefault().getDialogSettings();
		return root.getSection(SETTINGS_KEY) != null;
	}

	@Override
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTSIZE;
	}

	@Override
	protected Point getInitialLocation(Point size) {
		Point loc = Display.getDefault().getCursorLocation();
		Rectangle displayBounds = getDisplayBounds(loc);
		if (loc.x + size.x > displayBounds.x + displayBounds.width) {
			loc.x -= size.x; // Show on the other side of cursor
		}
		if (loc.y + size.y > displayBounds.y + displayBounds.height) {
			loc.y = displayBounds.y + displayBounds.height - size.y;
		}
		return loc;
	}

	protected Rectangle getDisplayBounds(Point loc) {
		Monitor[] ms = getShell().getDisplay().getMonitors();
		if (ms.length > 1) {
			Rectangle tmp;
			for (int i = 0; i < ms.length; i++) {
				tmp = ms[i].getBounds();
				if (tmp.contains(loc)) {
					return tmp;
				}
			}
		}
		return getShell().getDisplay().getBounds();
	}

	@Override
	protected Point getInitialSize() {
		if (hasDialogBoundsSettings()) {
			return super.getInitialSize();
		} else {
			Rectangle bounds = getShell().getDisplay().getBounds();
			int height = bounds.height / 2 - bounds.height / 10;
			return new Point(450, height);
		}
	}

	private CheckboxTreeViewer viewer;

	private FontMetrics fontMetrics;

	protected TreeViewerColumn createPropertyColumn() {
		final TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(Messages.AssertionPanelWindow_ColumnPropertyName);
		column.getColumn().setToolTipText(Messages.AssertionPanelWindow_ColumnPropertyToolTip);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String result = null;
				if (element instanceof AssertGroup) {
					result = ((AssertGroup) element).getName();
				} else if (element instanceof Assert) {
					final Assert a = (Assert) element;
					String attrName = a.getAttribute();
					int ind = StringUtils.getAttrLastSplitterInd(attrName);
					if (ind > 0) { // $NON-NLS-1$
						attrName = attrName.substring(attrName.lastIndexOf(".") + 1); //$NON-NLS-1$
					}
					if (attrName.indexOf('[') > 0) {
						attrName = attrName.substring(attrName.indexOf('['), attrName.indexOf(']') + 1);
					}
					result = attrName;
				}
				if (result != null) {
					result = result.replace("\n", "\\n").replace("\r", "");
				}
				return result;
			}
		});
		return column;
	}

	protected TreeViewerColumn createOperationColumn() {
		final TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(Messages.AssertionPanelWindow_ColumnOperationName);
		column.getColumn().setToolTipText(Messages.AssertionPanelWindow_ColumnOperationToolTip);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Assert) {
					return ((Assert) element).getKind().getLiteral();
				}
				return null;
			}
		});
		column.setEditingSupport(new AssertOperationEditingSupport(viewer));
		return column;
	}

	protected TreeViewerColumn createValueColumn() {
		final TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(Messages.AssertionPanelWindow_ValueOperationName);
		column.getColumn().setToolTipText(Messages.AssertionPanelWindow_ValueOperationToolTip);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String result = null;
				if (element instanceof Assert) {
					result = ((Assert) element).getValue();
				}
				if (result != null) {
					result = result.replace("\n", "\\n").replace("\r", "");
				}
				return result;
			}
		});
		column.setEditingSupport(new AssertValueEditingSupport(viewer));
		return column;
	}

	private void createColumnViewers(TreeColumnLayout layout) {
		layout.setColumnData(createPropertyColumn().getColumn(), new ColumnWeightData(20));
		layout.setColumnData(createOperationColumn().getColumn(), new ColumnWeightData(8));
		layout.setColumnData(createValueColumn().getColumn(), new ColumnWeightData(35));
	}

	protected void processCheck(Object element, boolean checked) {

		// -- go up the tree, to the branches
		if (element instanceof AssertGroup) {
			if (viewer.getExpandedState(element)) {
				for (Object a : ((AssertGroup) element).getAsserts()) {
					viewer.setChecked(a, checked);
					viewer.setGrayed(a, false);
					if (a instanceof Assert) {
						if (checked) {
							checkedObservable.add(a);
						} else {
							checkedObservable.remove(a);
						}
					} else {
						processCheck(a, checked);
					}
				}
				viewer.setGrayed(element, false);
			} else {
				viewer.setChecked(element, false);
				viewer.setGrayed(element, false);
			}
		}

		// -- go down the tree, to the roots

		AssertGroup parent = element instanceof Assert ? ((AssertInput) viewer.getInput()).getParent(element)
				: (AssertGroup) element;

		while (parent != null) {
			boolean hasChecked = false;
			boolean hasUnchecked = false;
			for (Object a : parent.getAsserts()) {
				if (viewer.getChecked(a)) {
					hasChecked = true;
					hasUnchecked |= viewer.getGrayed(a);
				} else {
					hasUnchecked = true;
				}
			}
			if (hasChecked) {
				viewer.setChecked(parent, true);
				viewer.setGrayed(parent, hasUnchecked);
				checkedObservable.add(parent);
			} else {
				viewer.setChecked(parent, false);
				viewer.setGrayed(parent, false);
				checkedObservable.remove(parent);
			}
			parent = ((AssertInput) viewer.getInput()).getParent(parent);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());

		final Composite toolbarComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(toolbarComposite);
		toolbarComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final ActionToolbar actions = new ActionToolbar() {
			@Override
			protected void fill(ToolBarManager manager) {
				manager.add(selectAll);
				manager.add(deselectAll);
				manager.add(new Separator());
				manager.add(expandAll);
				manager.add(collapseAll);
				// if
				// ("true".equalsIgnoreCase(Q7Features.getInstance().getValue(
				// Q7Features.Q7_IMAGE_ASSERT_SUPPORT))) {
				// manager.add(new Separator());
				// manager.add(newImageAssert);
				// }

				dbc.bindValue(Actions.observeEnabled(selectAll), hasAssert);
				dbc.bindValue(Actions.observeEnabled(deselectAll), hasAssert);
				dbc.bindValue(Actions.observeEnabled(expandAll), hasAssert);
				dbc.bindValue(Actions.observeEnabled(collapseAll), hasAssert);
			}
		};
		actions.create(toolbarComposite).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final MenuToolbar menu = new MenuToolbar() {
			@Override
			protected void fill(MenuManager manager) {
				manager.add(new Action(Messages.AssertionPanelWindow_CloseActionName) {
					@Override
					public void run() {
						close();
					}
				});
			};
		};
		menu.create(toolbarComposite).setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		createTreeViewer(composite).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createButtonPanel(composite).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return composite;
	}

	private void doFilter() {
		filterValue = filterText.getText().toLowerCase();
		viewer.refresh();
		expandAll(false);
	}

	protected Composite createFilterComposite(Composite parent) {
		final Composite filterComposite = new Composite(parent, SWT.BORDER);
		final GridLayout filteredCompositeLayout = new GridLayout(3, false);
		filteredCompositeLayout.marginWidth = 0;
		filteredCompositeLayout.marginHeight = 0;
		filterComposite.setLayout(filteredCompositeLayout);

		filterCombo = new CCombo(filterComposite, SWT.READ_ONLY);
		filterCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		filterCombo.setItems(new String[] { Messages.AssertionPanelWindow_FilterByProperty,
				Messages.AssertionPanelWindow_FilterByValue });
		filterCombo.select(1);
		filterCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFilter();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doFilter();
			}
		});

		filterText = new Text(filterComposite, SWT.SINGLE);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		filterText.setMessage(Messages.AssertionPanelWindow_FilterMessage);
		filterText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR) {
					doFilter();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		final ToolBar filterToolBar = new ToolBar(filterComposite, SWT.FLAT);
		filterToolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		final ToolItem filterItem = new ToolItem(filterToolBar, SWT.FLAT | SWT.PUSH);
		filterItem.setImage(Images.getImage(Images.PANEL_FILTER));
		filterItem.setToolTipText(Messages.AssertionPanelWindow_FilterToolTip);
		filterItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFilter();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doFilter();
			}
		});

		final org.eclipse.swt.graphics.Color background = filterText.getBackground();
		filterComposite.setBackground(background);
		filterCombo.setBackground(background);
		filterToolBar.setBackground(background);
		return filterComposite;
	}

	private interface ITreeViewerFilter {
		public boolean isVisible(AssertImpl object);
	}

	Composite treeViewerComposite = null;

	protected Control createTreeViewer(Composite parent) {
		treeViewerComposite = new Composite(parent, SWT.NONE);
		treeViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewerComposite.setLayout(new GridLayout());

		final Composite filterComposite = createFilterComposite(treeViewerComposite);
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		final Composite treeComposite = new Composite(treeViewerComposite, SWT.NONE);
		final TreeColumnLayout layout = new TreeColumnLayout();
		treeComposite.setLayout(layout);
		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Tree tree = new Tree(treeComposite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);

		dbc.bindValue(WidgetProperties.enabled().observe(tree), hasAssert);

		viewer = new CheckboxTreeViewer(tree);
		createColumnViewers(layout);
		viewer.setContentProvider(new AssertContentProvider(getAut()));
		viewer.setUseHashlookup(true);
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				processCheck(event.getElement(), event.getChecked());
			}
		});
		viewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (filterValue.isEmpty()) {
					return true;
				}
				if (filterCombo.getSelectionIndex() == 0) {
					return select((CheckboxTreeViewer) viewer, element, new ITreeViewerFilter() {
						@Override
						public boolean isVisible(AssertImpl object) {
							final String[] attributes = object.getAttribute().split("\\.");
							final String name = attributes[attributes.length - 1];
							return name.toLowerCase().contains(filterValue);
						}
					});
				}
				if (filterCombo.getSelectionIndex() == 1) {
					return select((CheckboxTreeViewer) viewer, element, new ITreeViewerFilter() {
						@Override
						public boolean isVisible(AssertImpl object) {
							return object.getValue().toLowerCase().contains(filterValue);
						}
					});
				}
				return true;
			}

			private boolean select(CheckboxTreeViewer viewer, Object element, ITreeViewerFilter filter) {
				if (element instanceof AssertGroup) {
					AssertGroup group = (AssertGroup) element;
					for (final Object child : group.getAsserts()) {
						if (select(viewer, child, filter)) {
							return true;
						}
					}
				} else if (element instanceof AssertImpl) {
					final AssertImpl assertElement = (AssertImpl) element;
					if (filter.isVisible(assertElement)) {
						return true;
					}
				} else {
					return true;
				}
				return false;
			}
		});
		viewer.getTree().addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
				if (filterValue.isEmpty() || (event.detail & SWT.SELECTED) != 0) {
					return;
				}
				if (((TreeItem) event.item).getData() instanceof AssertImpl) {
					final GC gc = event.gc;
					gc.setBackground(new org.eclipse.swt.graphics.Color(viewer.getControl().getDisplay(),
							new RGB(168, 182, 223)));
					gc.fillRectangle(0, event.y, viewer.getTree().getClientArea().width, event.height);
				}
			}
		});

		return treeViewerComposite;
	}

	public AutLaunch getAut() {
		return recordingSupport.getAUT();
	}

	@Override
	protected void initializeDialogUnits(Control control) {
		// Compute and store a font metric
		Shell sh = new Shell(control.getDisplay());
		sh.setVisible(false);
		GC gc = new GC(sh);
		gc.setFont(JFaceResources.getDialogFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
		sh.dispose();
	}

	@Override
	protected int convertHorizontalDLUsToPixels(int dlus) {
		// test for failure to initialize for backward compatibility
		if (fontMetrics == null) {
			return 0;
		}
		return convertHorizontalDLUsToPixels(fontMetrics, dlus);
	}

	protected int getButtonWidth(Button button) {
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		return minSize.x;
	}

	protected Control createButtonPanel(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

		final Button widgetInfo = new Button(composite, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(widgetInfo);
		widgetInfo.setText("Widget details...");
		widgetInfo.setImage(Images.getImage(Images.PANEL_NEW_IMAGE_WIDGET_DETAILS));

		widgetInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GetWidgetDetails getDetails = TeslaFactory.eINSTANCE.createGetWidgetDetails();
				getDetails.setElement(EcoreUtil.copy(commands.getElement()));
				Object result = null;
				try {
					result = getAut().execute(getDetails);
				} catch (Exception eeee) {
					Q7UIPlugin.log(eeee);
				}
				if (result != null && result instanceof BoxedValue) {
					String msg = BoxedValues.toString((BoxedValue) result);
					widgetDetailsDialog = new WidgetDetailsDialog(widgetInfo.getShell(), msg);
					widgetDetailsDialog.open();
				}
			}
		});

		// createVerificationsButton(composite);

		appendButton = new Button(composite, SWT.NONE);

		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(true, false).hint(120, SWT.DEFAULT)
				.applyTo(appendButton);

		
		appendButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
		appendButton.setText(Messages.AssertionPanelWindow_AddAssertButton);
		appendButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addButtonPressed();
				close();
			}
		});

		checkedObservable = ViewerProperties.<CheckboxTreeViewer, Object>checkedElements(Object.class).observe(viewer);
		ComputedValue<Boolean> computed = new ComputedValue<Boolean>(Boolean.TYPE) {
			@Override
			protected Boolean calculate() {
				if (!(Boolean) hasAssert.getValue()) {
					return Boolean.FALSE;
				}
				return !checkedObservable.isEmpty();
			}
		};
		dbc.bindValue(WidgetProperties.enabled().observe(appendButton), computed);
		return composite;
	}

	protected void addButtonPressed() {
		recordingSupport.getRecorder().addAsserts(commands, getCheckedAsserts().toArray(new Assert[0]));
		viewer.setCheckedElements(new Object[0]);
		// setInput(null);
	}

	private List<Assert> getCheckedAsserts() {
		final List<Assert> asserts = new ArrayList<Assert>();
		for (Object element : viewer.getCheckedElements()) {
			if (element instanceof Assert) {
				asserts.add((Assert) element);
			}
		}
		return asserts;
	}

	protected AssertInput createAssertTree(CommandSet commands) {
		final Widget widget = commands.getWidget();
		final Element element = commands.getElement();

		final List<AssertGroup> result = new ArrayList<AssertGroup>();

		// Widget basic properties
		final List<Object> basicGroup = new ArrayList<Object>();
		createWidgetPropertiesAssert(basicGroup, widget, element);
		createWidgetReferencesAssert(basicGroup, widget, element);
		if (!basicGroup.isEmpty()) {
			result.add(new AssertGroup(widget.eClass().getName() + " (Basic Properties)", basicGroup)); //$NON-NLS-1$
		}

		// Model properties
		if (widget instanceof DiagramItem) {
			final List<Object> modelGroup = new ArrayList<Object>();
			AssertionUtils.fillAdvancedPropertiesGroup(modelGroup, ((DiagramItem) widget).getModelPropertyNodes(),
					element, ""); //$NON-NLS-1$
			if (!modelGroup.isEmpty()) {
				result.add(new AssertGroup(widget.eClass().getName() + " (Model Properties)", modelGroup)); //$NON-NLS-1$
			}
		}

		// Advanced properties
		final List<Object> advancedGroup = new ArrayList<Object>();
		AssertionUtils.fillAdvancedPropertiesGroup(advancedGroup,
				((AssertContentProvider) viewer.getContentProvider()).loadChildren(element, "").getPropertyNodes(), //$NON-NLS-1$
				element, "");
		if (!advancedGroup.isEmpty()) {
			result.add(new AssertGroup(widget.eClass().getName() + " (Advanced Properties)", advancedGroup)); //$NON-NLS-1$
		}

		return new AssertInput(result, element);
	}

	private void createWidgetPropertiesAssert(List<Object> group, Widget widget, Element element) {
		createWidgetPropertiesAssert(group, widget, element, ""); //$NON-NLS-1$
	}

	private static final List<EAttribute> deprecated = Arrays.asList(UiPackage.Literals.TEXT__RAW_VALUE,
			UiPackage.Literals.TEXT__VALUE, UiPackage.Literals.TEXT__STYLE_RANGES,
			UiPackage.Literals.TREE_ITEM__STYLE_RANGES, UiPackage.Literals.TABLE_ITEM__STYLE_RANGES);

	private void createWidgetPropertiesAssert(List<Object> group, EObject widget, Element element,
			String propertyPath) {
		if (propertyPath.length() > 0) {
			propertyPath += "."; //$NON-NLS-1$
		}
		for (EAttribute attr : widget.eClass().getEAllAttributes()) {
			if (deprecated.contains(attr)) {
				continue;
			}
			final Object value = widget.eGet(attr);
			if (value == null) {
				continue;
			}
			final String attrName = attr.getName();

			if (attr.isMany()) {
				if (!(value instanceof List<?>)) {
					continue;
				}
				final List<?> list = (List<?>) value;
				if (list.isEmpty()) {
					continue;
				}

				final List<Object> children = new ArrayList<Object>();
				for (int i = 0; i < list.size(); i++) {
					String childPropertyPath = String.format("%s%s[%d]", propertyPath, attrName, i);
					children.add(AssertionUtils.createAssert(childPropertyPath, list.get(i), attr.getEType(), element,
							null));
				}
				group.add(new AssertGroup(attrName, children));
			} else {
				group.add(AssertionUtils.createAssert(propertyPath + attr.getName(), value, attr.getEType(), element,
						null));
			}
		}
	}

	private void createWidgetReferencesAssert(List<Object> group, Widget widget, Element element) {
		createWidgetReferencesAssert(group, widget, element, ""); //$NON-NLS-1$
	}

	private void createWidgetReferencesAssert(List<Object> group, EObject widget, Element element,
			String propertyPath) {
		if (propertyPath.length() > 0) {
			propertyPath += "."; //$NON-NLS-1$
		}
		for (EReference attr : widget.eClass().getEAllReferences()) {
			if (attr.equals(UiPackage.Literals.CONTROL__BOUNDS)) {
				continue;
			}

			if (!attr.isMany()) {
				String childPropertyPath = propertyPath + attr.getName();
				final Object value = widget.eGet(attr);
				if (value instanceof Color) {
					if (value != null && value instanceof EObject) {
						group.add(AssertionUtils.createAssert(childPropertyPath,
								SimpleCommandPrinter.toString((EObject) value, true).trim(),
								EcorePackage.eINSTANCE.getEString(), element, null));
					}
				} else if (value instanceof EObject) {
					final List<Object> children = new ArrayList<Object>();
					createWidgetPropertiesAssert(children, (EObject) value, element, childPropertyPath);
					createWidgetReferencesAssert(children, (EObject) value, element, childPropertyPath);
					group.add(new AssertGroup(attr.getName(), children));
				}
			} else {
				final Object elements = widget.eGet(attr);
				final List<Object> children = new ArrayList<Object>();
				if (elements instanceof EMap<?, ?>) {
					final EMap<?, ?> map = (EMap<?, ?>) elements;
					if (map.isEmpty()) {
						continue;
					}
					for (Entry<?, ?> entry : map.entrySet()) {
						bindMapEntry(entry, element, propertyPath, attr, children);
					}

				} else if (elements instanceof List<?>) {
					final List<?> list = (List<?>) elements;
					if (list.isEmpty()) {
						continue;
					}
					for (int i = 0; i < list.size(); i++) {
						Object v = list.get(i);
						String childPropertyName = String.format("%s%s[%d]", propertyPath, attr.getName(), i);
						if (v instanceof Color) {
							final Assert a = AssertionUtils.createAssert(childPropertyName,
									SimpleCommandPrinter.toString((EObject) v, true).trim(),
									EcorePackage.eINSTANCE.getEString(), element, null);
							children.add(a);
						} else if (v instanceof EObject) {
							final List<Object> widgetChildren = new ArrayList<Object>();
							createWidgetPropertiesAssert(widgetChildren, (EObject) v, element, childPropertyName);
							createWidgetReferencesAssert(widgetChildren, (EObject) v, element, childPropertyName);
							AssertGroup widgetGroup = new AssertGroup("[" + i + "]", //$NON-NLS-1$ //$NON-NLS-2$
									widgetChildren);
							children.add(widgetGroup);
						}
					}
				}
				if (!children.isEmpty()) {
					group.add(new AssertGroup(attr.getName(), children));
				}
			}
		}
	}

	private void bindMapEntry(Entry<?, ?> entry, Element element, String propertyPath, EReference attr,
			List<Object> children) {
		if (entry.getValue() instanceof List<?>) {
			final List<?> childNodes = (List<?>) entry.getValue();
			final List<Object> childNodesMarkers = new ArrayList<Object>();
			String lineNumber = "['" + entry.getKey() + "']";

			for (int i = 0; i < childNodes.size(); i++) {
				Object node = childNodes.get(i);
				String markerNumber = "[" + i + "]";
				if (node instanceof Color) {
					final Assert a = AssertionUtils.createAssert(markerNumber,
							SimpleCommandPrinter.toString((EObject) node, true).trim(),
							EcorePackage.eINSTANCE.getEString(), element, null);
					childNodesMarkers.add(a);
				} else if (node instanceof EObject) {
					final List<Object> childNodesMarkersView = new ArrayList<Object>();
					createWidgetPropertiesAssert(childNodesMarkersView, (EObject) node, element,
							propertyPath + attr.getName() + lineNumber + markerNumber);
					AssertGroup widgetGroup = new AssertGroup(markerNumber, childNodesMarkersView);
					childNodesMarkers.add(widgetGroup);
				}
			}

			AssertGroup widgetGroup = new AssertGroup(lineNumber, childNodesMarkers);
			children.add(widgetGroup);
		} else if (entry.getKey() != null) {
			String childPropertyName = String.format("%s%s['%s']", propertyPath, attr.getName(),
					entry.getKey().toString());
			final Assert a = AssertionUtils.createAssert(childPropertyName, entry.getValue().toString(),
					EcorePackage.eINSTANCE.getEString(), element, null);
			children.add(a);
		}
	}

	public void setInput(CommandSet commands) {
		if (widgetDetailsDialog != null) {
			widgetDetailsDialog.close();
		}
		this.commands = commands;
		hasAssert.setValue(commands != null);
		if (commands != null) {
			if (getShell() == null || getShell().isDisposed()) {
				setBlockOnOpen(false);
				open();
			} else {
				Point size = getShell().getSize();
				Point loc = getInitialLocation(size);
				getShell().setLocation(loc);
				getShell().setVisible(true);
			}
			currentInput = createAssertTree(commands);
			viewer.setInput(currentInput);
			if (currentInput != null && currentInput.getAsserts() != null && currentInput.getAsserts().size() > 0) {
				viewer.expandToLevel(currentInput.getAsserts().get(0), 1);
			} else {
				viewer.expandToLevel(1);
			}
			// updateVerificationButton();
		} else if (getShell() != null && !getShell().isDisposed()) {
			getShell().setVisible(false);
			viewer.setInput(AssertInput.EMPTY);
			currentInput = null;
		}
	}

	private void expandAll(boolean allowExpandReferences) {
		if (collapsed == null) {
			collapsed = new ArrayList<TreeItem>();
		}
		if (references == null) {
			references = new ArrayList<TreeItem>();
		}
		collectCollapsed(viewer.getTree().getItems());
		if (collapsed.isEmpty()) {
			if (allowExpandReferences) {
				final Display viewerDisplay = viewer.getControl().getDisplay();
				final Job job = new Job("Expand All") {
					@Override
					public IStatus run(IProgressMonitor monitor) {
						enableButtons(false);
						monitor.beginTask("Expand All", references.size());
						for (final TreeItem item : references) {
							if (monitor.isCanceled()) {
								references.clear();
								enableButtons(true);
								return Status.CANCEL_STATUS;
							}
							viewerDisplay.syncExec(new Runnable() {
								@Override
								public void run() {
									viewer.setExpandedState(item.getData(), true);
									viewer.refresh();
								}
							});
							monitor.worked(1);
						}
						monitor.done();
						references.clear();
						enableButtons(true);
						return Status.OK_STATUS;
					}

					private void enableButtons(final boolean enabled) {
						viewerDisplay.syncExec(new Runnable() {
							@Override
							public void run() {
								treeViewerComposite.setEnabled(enabled);
								selectAll.setEnabled(enabled);
								deselectAll.setEnabled(enabled);
								collapseAll.setEnabled(enabled);
								expandAll.setEnabled(enabled);
							}
						});
					}
				};
				ProgressManager.getInstance().showInDialog(getShell(), job);
				job.schedule();
			}
		} else {
			for (final TreeItem item : collapsed) {
				viewer.setExpandedState(item.getData(), true);
			}
			collapsed.clear();
			references.clear();
		}
	}

	private void collectCollapsed(final TreeItem[] treeItems) {
		if (treeItems == null) {
			return;
		}
		for (final TreeItem item : treeItems) {
			if (item != null && item.getData() instanceof AssertGroup) {
				if (item.getItems().length != 1 || item.getItems()[0] == null || item.getItems()[0].getData() != null) {
					collectCollapsed(item.getItems());
					if (!item.getExpanded()) {
						collapsed.add(item);
					}
				} else if (!item.getExpanded()) {
					references.add(item);
				}
			}
		}
	}
}
