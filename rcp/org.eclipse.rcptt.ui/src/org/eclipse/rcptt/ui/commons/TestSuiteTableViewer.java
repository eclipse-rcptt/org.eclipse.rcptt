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
package org.eclipse.rcptt.ui.commons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.model.search.AllProjectScope;
import org.eclipse.rcptt.core.model.search.ISearchScope;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.scenario.ScenarioFactory;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;
import org.eclipse.rcptt.core.scenario.TestSuite;
import org.eclipse.rcptt.core.scenario.TestSuiteItem;
import org.eclipse.rcptt.core.tags.Tag;
import org.eclipse.rcptt.core.workspace.Q7Utils;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.OneProjectScope;
import org.eclipse.rcptt.internal.core.model.ReferencedProjectScope;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.actions.SelectionAction;
import org.eclipse.rcptt.ui.editors.NamedElementLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ResourceTransfer;

public class TestSuiteTableViewer extends TableViewer implements IContentNamer, ISelectionActionsHandler {

	private static TestSuiteItemTransfer TRANSFER = null;
	private static TestSuiteItem[] EMPTY = new TestSuiteItem[0];

	private final ITestSuite testSuiteModel;
	private final TestSuite testSuite;

	private final DataBindingContext dbc = new DataBindingContext();
	private final IObservableValue<List<TestSuiteItem>> testSuiteObservable;

	private SelectionAction.RemoveAction removeAction;
	private SelectionAction.CutAction cutAction;
	private SelectionAction.CopyAction copyAction;
	private SelectionAction.PasteAction pasteAction;

	public TestSuiteTableViewer(Composite parent, ITestSuite testSuiteModel, TestSuite testSuite) {
		super(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

		this.testSuiteModel = testSuiteModel;
		this.testSuite = testSuite;

		GridData tableGridData = new GridData(GridData.FILL_BOTH);
		tableGridData.widthHint = 1;
		tableGridData.heightHint = 200;
		getTable().setLayoutData(tableGridData);
		getTable().setLinesVisible(true);
		getTable().setHeaderVisible(true);

		createTableColumns();

		setContentProvider(new TableContentProvider());

		testSuiteObservable = EMFObservables.observeValue(this.testSuite, ScenarioPackage.Literals.TEST_SUITE__ITEMS);
		dbc.bindValue(ViewerProperties.<TestSuiteTableViewer, List<TestSuiteItem>>input().observe(this), testSuiteObservable);

		addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doOpen();
			}
		});

		addKeyListeners();

		MenuManager popupMenuManager = new MenuManager();
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager mng) {
				fillContextMenu(mng);
			}
		};
		popupMenuManager.addMenuListener(listener);
		popupMenuManager.setRemoveAllWhenShown(true);
		Menu menu = popupMenuManager.createContextMenu(getControl());
		getControl().setMenu(menu);

		// test case names refresher
		RcpttCore.addElementChangedListener(workspaceMonitorListener);
		getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				RcpttCore.removeElementChangedListener(workspaceMonitorListener);
			}
		});

		initializeInternalDragAndDrop();
	}

	private final IElementChangedListener workspaceMonitorListener = new IElementChangedListener() {

		public void elementChanged(Q7ElementChangedEvent event) {
			if (getControl() == null || getControl().isDisposed())
				return;

			boolean needRefresh = false;
			try {
				TestSuiteItem[] displayingItems = testSuiteModel.getItems();

				for (IQ7NamedElement element : event.getDelta().getNamedElements()) {
					String elementID = Q7SearchCore.findIDByDocument(element);
					if (elementID != null) {

						for (TestSuiteItem item : displayingItems) {
							if (item.getNamedElementId().equals(elementID)) {
								needRefresh = true;
								break;
							}
						}
					}
				}
			} catch (ModelException e) {
				needRefresh = true;
			}

			if (needRefresh) {
				Q7UIPlugin.asyncExec(new Runnable() {
					public void run() {
						if (!getControl().isDisposed())
							refresh();
					}
				});
			}
		}
	};

	private void addKeyListeners() {
		getControl().addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if ((SWT.DEL == e.character) && (0 == e.stateMask)) {
					if (!getSelection().isEmpty()) {
						handleRemove();
						e.doit = false;
					}
				} else if ('\u0018' == e.character) {
					if (!getSelection().isEmpty()) {
						cut();
						e.doit = false;
					}
				} else if ('\u0003' == e.character) {
					if (!getSelection().isEmpty()) {
						copy();
						e.doit = false;
					}
				} else if ('\u0016' == e.character) {
					if (canPaste()) {
						paste();
						e.doit = false;
					}
				}
			}
		});
	}

	protected void fillContextMenu(IMenuManager manager) {
		removeAction = new SelectionAction.RemoveAction(this);
		cutAction = new SelectionAction.CutAction(this);
		copyAction = new SelectionAction.CopyAction(this);
		pasteAction = new SelectionAction.PasteAction(this);

		removeAction.update();
		cutAction.update();
		copyAction.update();
		pasteAction.update();

		manager.add(removeAction);
		manager.add(cutAction);
		manager.add(copyAction);
		manager.add(pasteAction);
	}

	public IObservableValue<List<TestSuiteItem>> getTestSuiteObservable() {
		return testSuiteObservable;
	}

	private void createTableColumns() {
		final TableViewerColumn columnTestCase = new TableViewerColumn(this, SWT.NONE);
		columnTestCase.getColumn().setText(Messages.ScenariosLaunchTab_ColumnTestCaseText);
		IQ7Project project = testSuiteModel.getQ7Project();

		final NamedElementLabelProvider testCaseLabelProvider = new NamedElementLabelProvider(project);
		columnTestCase.setLabelProvider(testCaseLabelProvider);

		final TableViewerColumn columnLocation = new TableViewerColumn(this, SWT.NONE);
		columnLocation.getColumn().setText(Messages.ScenariosLaunchTab_ColumnLocationText);
		columnLocation.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getPathName((TestSuiteItem) element);
			}
		});

		// new ColumnViewerSorter(this, columnTestCase) {
		//
		// @Override
		// protected int doCompare(Viewer viewer, Object element1,
		// Object element2) {
		// String name1 = testCaseLabelProvider.getText(element1);
		// String name2 = testCaseLabelProvider.getText(element2);
		// return name1.compareToIgnoreCase(name2);
		// }
		//
		// @Override
		// protected void afterSort() {
		// ReorderSourceList();
		// }
		// };

		// new ColumnViewerSorter(this, columnLocation) {
		//
		// @Override
		// protected int doCompare(Viewer viewer, Object element1,
		// Object element2) {
		// String name1 = getPathName((TestSuiteItem) element1);
		// String name2 = getPathName((TestSuiteItem) element2);
		// return name1.compareToIgnoreCase(name2);
		// }
		//
		// @Override
		// protected void afterSort() {
		// ReorderSourceList();
		// }
		//
		// };

		getTable().addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int width = getTable().getClientArea().width;
				int testCaseColWidth = columnTestCase.getColumn().getWidth();
				int locationColWidth = columnLocation.getColumn().getWidth();
				double ratio = 0.5;
				if (testCaseColWidth != 0 && locationColWidth != 0) {
					ratio = (double) testCaseColWidth / (testCaseColWidth + locationColWidth);
				}
				columnTestCase.getColumn().setWidth((int) (width * ratio));
				columnLocation.getColumn().setWidth((int) (width * (1 - ratio)));
			}
		});
	}

	private String getPathName(TestSuiteItem item) {
		IQ7NamedElement q7NamedElement = Q7SearchCore.getTestSuiteItemElement(item, getSearchScope());
		if (q7NamedElement != null) {
			String folders = q7NamedElement.getResource().getProjectRelativePath().removeLastSegments(1).toString();
			if (folders.length() > 0) {
				folders = "/" + folders; //$NON-NLS-1$
			}
			return q7NamedElement.getResource().getProject().getName() + folders;
		}
		return ""; //$NON-NLS-1$
	}

	private ISearchScope getSearchScope() {
		IQ7Project testSuiteProject = testSuiteModel.getQ7Project();
		ISearchScope scope = testSuiteProject != null ? new ReferencedProjectScope(testSuiteProject)
				: new AllProjectScope();
		return scope;
	}

	public void reorderSourceList() {
		for (int i = 0; i < getTable().getItemCount(); i++) {
			testSuite.getItems().move(i, (TestSuiteItem) getElementAt(i));
		}
	}

	private void doOpen() {
		final IStructuredSelection iss = (IStructuredSelection) getSelection();
		final Object item = iss.getFirstElement();
		IQ7NamedElement element = Q7SearchCore.getTestSuiteItemElement((TestSuiteItem) item, getSearchScope());
		if (element instanceof IQ7NamedElement) {
			IQ7NamedElement q7Element = element;
			final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (activePage != null) {
				try {
					IDE.openEditor(activePage, (IFile) q7Element.getResource());
				} catch (PartInitException e) {
					Q7UIPlugin.log(e);
				}
			}
		}
	}

	protected void initializeInternalDragAndDrop() {
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] { getContentTransfer() };
		addDragSupport(ops, transferTypes, new ContentDragListener(this));

		transferTypes = getDropTransferTypes();
		addDropSupport(ops, transferTypes, new ContentTableDropListener(this));
	}

	protected Transfer[] getDropTransferTypes() {
		return new Transfer[] { getContentTransfer(), ResourceTransfer.getInstance() };
	}

	protected Transfer getContentTransfer() {
		if (null == TRANSFER) {
			TRANSFER = new TestSuiteItemTransfer();
		}
		return TRANSFER;
	}

	protected boolean doPerformDrop(Object data, Object currentTarget, int currentLocation) {
		flushAndResetTableSort();
		testSuite.setManuallyOrdered(true);
		if (data instanceof IResource[]) {
			IResource[] resources = (IResource[]) data;
			int targetPosition = testSuite.getItems().size() - 1;
			if (currentTarget != null) {
				targetPosition = testSuite.getItems().indexOf(currentTarget);
			}
			if (currentLocation == ViewerDropAdapter.LOCATION_AFTER
					|| currentLocation == ViewerDropAdapter.LOCATION_NONE) {
				targetPosition++;
			}
			for (IResource resource : resources) {
				if (resource instanceof IFile) {
					IQ7NamedElement element = getNamedElementByResource(resource);
					if (isValidToAdd(element)) {
						addItem(element, targetPosition);
						targetPosition++;
					}
				} else if (resource instanceof IContainer) {
					addFolder((IContainer) resource, targetPosition);
				}
			}
			return true;
		} else {
			return doPerformInternalDrop(data, currentTarget, currentLocation);
		}
	}

	protected boolean doPerformInternalDrop(Object data, Object currentTarget, int currentLocation) {
		EList<TestSuiteItem> content = testSuite.getItems();

		int shiftLastPosition = 0;
		int targetPosition = content.size() - 1;

		if (currentTarget != null) {
			targetPosition = content.indexOf(currentTarget);
		}
		if (currentLocation == ViewerDropAdapter.LOCATION_AFTER || currentLocation == ViewerDropAdapter.LOCATION_NONE) {
			shiftLastPosition++;
		}

		int shiftNextLandingPosition = 0;
		List<TestSuiteItem> toDrop = convertStringsToItems(data);
		
		for (TestSuiteItem toDropObject : toDrop) {
			int fromPosition = testSuite.getItems().indexOf(toDropObject);
			int toPosition = targetPosition + shiftNextLandingPosition;

			if (fromPosition == -1) {
				testSuite.getItems().add(toPosition + shiftLastPosition, toDropObject);
				shiftNextLandingPosition++;
			} else {
				testSuite.getItems().move(toPosition, fromPosition);
				if (fromPosition >= targetPosition) {
					shiftNextLandingPosition++;
				}
			}
		}

		updateItems();
		return true;
	}

	private List<TestSuiteItem> convertStringsToItems(Object data) {
		List<TestSuiteItem> result = new ArrayList<TestSuiteItem>();
		for (Object itemDesc : (Object[]) data) {
			if (itemDesc instanceof String && ((String) itemDesc).lastIndexOf(';') != -1) {
				String itemDescStr = (String) itemDesc;
				String suiteId = itemDescStr.substring(0, itemDescStr.lastIndexOf(';'));
				int idx = Integer.parseInt(itemDescStr.substring(itemDescStr.lastIndexOf(';') + 1));
				if (testSuite.getId().equals(suiteId)) {
					result.add(testSuite.getItems().get(idx));
				} else {
					IQ7NamedElement[] elements = Q7SearchCore.findById(suiteId, getSearchScope(),
							new NullProgressMonitor());
					if (elements.length == 1 && elements[0] instanceof ITestSuite) {
						try {
							TestSuite sourceSuite = (TestSuite) ((ITestSuite) elements[0]).getModifiedNamedElement();
							if (!testSuite.getId().equals(sourceSuite.getItems().get(idx).getNamedElementId())) {
								result.add(EcoreUtil.copy(sourceSuite.getItems().get(idx)));
							}
						} catch (ModelException e) {
							Q7UIPlugin.log(e);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Validate drop operation.<br/>
	 * <br/>
	 * Valid drop items are IContainer, IQ7NamedElement.<br/>
	 * 
	 * @param target
	 * @param operation
	 * @param transferType
	 * @param currentLocation
	 * @return true|false
	 */
	protected boolean isDropValid(Object target, int operation, TransferData transferType, int currentLocation) {
		if (ResourceTransfer.getInstance().isSupportedType(transferType)) {
			IResource[] resources = getSelectedResources();
			for (IResource resource : resources) {
				if (resource instanceof IContainer) {
					return true;
				}
				IQ7NamedElement element = getNamedElementByResource(resource);
				if (isValidToAdd(element)) {
					return true;
				}
			}
			return false;
		}
		return getContentTransfer().isSupportedType(transferType)
		/* && currentLocation != ViewerDropAdapter.LOCATION_ON */;
	}

	/**
	 * Check is element instance of ITestSuite or ITestCase<br/>
	 * <br/>
	 * Add validation rules:<br/>
	 *  - we can't add self test suite,<br/>
	 *  - we can add any test case 1+ times,<br/>
	 *  - we can add any other named test suite 1+ times.<br/>
	 * 
	 * @param element
	 * @return boolean
	 */
	protected boolean isValidToAdd(IQ7Element element) {
		if ((!testSuiteModel.equals(element)) && (element instanceof ITestSuite || element instanceof ITestCase)) {
			return true;
		}
		return false;
	}

	public void addItem(IQ7NamedElement q7NamedElement) {
		addItem(q7NamedElement, -1);
	}

	public void addItems(List<IQ7NamedElement> items) {
		for (IQ7NamedElement item : items) {
			addItem(item);
		}
	}

	public void addTags(List<Tag> tags) {
		for (Tag tag : tags) {
			EList<IQ7NamedElement> refs = tag.getRefs();
			for (IQ7NamedElement e : refs) {
				addItem(e);
			}
			EList<Tag> childs = tag.getTags();
			if (childs.size() > 0) {
				addTags(childs);
			}
		}
	}

	public void addItem(IQ7NamedElement q7NamedElement, int position) {
		if (testSuiteModel.equals(q7NamedElement)) {
			return;
		}
		try {
			String id = q7NamedElement.getID();
			if (!isValidToAdd(q7NamedElement)) {
				return;
			}

			TestSuiteItem testItem = ScenarioFactory.eINSTANCE.createTestSuiteItem();
			testItem.setNamedElementId(id);
			testItem.setNamedElemetName(q7NamedElement.getElementName());
			if (q7NamedElement instanceof ITestCase) {
				testItem.setKind("test");
			} else if (q7NamedElement instanceof ITestSuite) {
				testItem.setKind("suite");
			}
			IPath path = testSuiteModel.getPath().removeLastSegments(1);
			IPath iPath = q7NamedElement.getPath();
			IPath relativeTo = iPath.makeRelativeTo(path);

			testItem.setPath(relativeTo.toString());
			if (position == -1) {
				testSuite.getItems().add(testItem);
			} else {
				testSuite.getItems().add(position, testItem);
			}
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return;
		}
	}

	public void addFolder(IContainer container) {
		addFolder(container, -1);
	}

	
	/**
	 * Add folder elements one by one.
	 * 
	 * @param container
	 * @param position
	 */
	public void addFolder(IContainer container, int position) {
		if (container != null) {
			IPath fullPath = container.getFullPath();
			IQ7NamedElement[] allElements = Q7SearchCore
					.findAllElements(new OneProjectScope(RcpttCore.create(container.getProject())));

			for (int i = 0; i < allElements.length; i++) {
				IQ7NamedElement el = allElements[i];
				 if (fullPath.isPrefixOf(el.getPath())) {
					if (isValidToAdd(el)) {
						addItem(el, position);
					}
				}
			}
		}
	}

	public void removeAll() {
		testSuite.getItems().clear();
	}

	public void moveUp() {
		flushAndResetTableSort();
		Table table = getTable();
		int indices[] = table.getSelectionIndices();
		int newSelection[] = new int[indices.length];
		for (int i = 0; i < indices.length; i++) {
			int index = indices[i];
			if (index > 0) {
				swapElements(index, index - 1);
				newSelection[i] = index - 1;
			} else if (index == 0) {
				newSelection[i] = 0;
			}
		}
		table.setSelection(newSelection);
	}

	public void moveDown() {
		flushAndResetTableSort();
		Table table = getTable();
		int[] indices = table.getSelectionIndices();
		if (indices.length < 1) {
			return;
		}
		int newSelection[] = new int[indices.length];
		int max = table.getItemCount() - 1;
		for (int i = indices.length - 1; i >= 0; i--) {
			int index = indices[i];
			if (index < max) {
				swapElements(index, index + 1);
				newSelection[i] = index + 1;
			} else if (index == max) {
				newSelection[i] = max;
			}
		}
		table.setSelection(newSelection);
	}

	public boolean canRemove() {
		return !getSelection().isEmpty();
	}

	public boolean canCut() {
		return !getSelection().isEmpty();
	}

	public boolean canCopy() {
		return !getSelection().isEmpty();
	}

	public boolean canPaste() {
		return canAdd(ClipboardUtils.getClipboardContents(getContentTransfer()))
				|| canAdd(ClipboardUtils.getClipboardContents(ResourceTransfer.getInstance()));
	}

	public void handleRemove() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		List<?> selectedElements = selection.toList();
		testSuite.getItems().removeAll(selectedElements);
	}

	public void cut() {
		copy();
		handleRemove();
	}

	public void copy() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		ClipboardUtils.setClipboardContents(serializeSelection(selection), getContentTransfer(), this);
	}

	public void paste() {
		doAdd(ClipboardUtils.getClipboardContents(getContentTransfer()));
		doAdd(ClipboardUtils.getClipboardContents(ResourceTransfer.getInstance()));
	}

	protected boolean canAdd(Object[] objects) {
		if ((null == objects) || (0 == objects.length)) {
			return false;
		}
		for (Object obj : objects) {
			if (obj instanceof IResource) {
				IQ7Element file = RcpttCore.create((IResource) obj);
				if (isValidToAdd(file)) {
					continue; // Can add this item
				}
			}
			if (!String.class.isInstance(obj)) {
				return false;
			}
		}
		return true;
	}

	protected void doAdd(Object[] objects) {
		if (null == objects) {
			return;
		}
		for (TestSuiteItem item : deserializeItems(objects)) {
			testSuite.getItems().add(EcoreUtil.copy(item));
		}
		for (Object obj : objects) {
			if (obj instanceof IResource) {
				IQ7Element file = RcpttCore.create((IResource) obj);
				if (file.exists() && isValidToAdd(file) && file instanceof IQ7NamedElement) {
					addItem((IQ7NamedElement) file);
				}
			}
		}
	}

	private void flushAndResetTableSort() {
		final TableItem[] tableItems = getTable().getItems();
		final EList<TestSuiteItem> items = testSuite.getItems();
		for (int i = 0; i < tableItems.length; ++i) {
			items.move(i, (TestSuiteItem) tableItems[i].getData());
		}
		getTable().setSortColumn(null);
		getTable().setSortDirection(ColumnViewerSorter.NONE);
		setComparator(null);
	}

	private void swapElements(int a, int b) {
		TestSuiteItem aItem = testSuite.getItems().get(a);
		TestSuiteItem bItem = testSuite.getItems().get(b);
		testSuite.getItems().move(a, bItem);
		testSuite.getItems().move(b, aItem);
	}

	private IResource[] getSelectedResources() {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		if (selection instanceof IStructuredSelection) {
			return getSelectedResources((IStructuredSelection) selection);
		}
		return new IResource[0];
	}

	private IResource[] getSelectedResources(IStructuredSelection selection) {
		ArrayList<IResource> selectedResources = new ArrayList<IResource>();

		for (Iterator<?> i = selection.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof IResource) {
				selectedResources.add((IResource) o);
			} else if (o instanceof IAdaptable) {
				IAdaptable a = (IAdaptable) o;
				IResource r = (IResource) a.getAdapter(IResource.class);
				if (r != null) {
					selectedResources.add(r);
				}
			}
		}
		return selectedResources.toArray(new IResource[selectedResources.size()]);
	}

	private IQ7NamedElement getNamedElementByResource(IResource resource) {
		if (resource.getType() == IResource.FILE) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IFile file = workspace.getRoot().getFile(resource.getFullPath());
			if (file != null && file.exists()) {
				return (IQ7NamedElement) RcpttCore.create(file);
			}
		}
		return null;
	}

	private String[] convertSelectionToStrings(IStructuredSelection selection) {
		List<String> selectedElements = new ArrayList<String>();
		int[] indices = getTable().getSelectionIndices();

		int pos = 0;
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object element = it.next();
			if (element instanceof TestSuiteItem) {
				// int idx = testSuite.getItems().indexOf(element);
				// selectedElements.add(testSuite.getId() + ";" + idx);
				selectedElements.add(testSuite.getId() + ";" + indices[pos]);
			}
			pos++;
		}
		return selectedElements.toArray(new String[selectedElements.size()]);
	}

	private String[] serializeSelection(IStructuredSelection selection) {
		List<String> selectedElements = new ArrayList<String>();
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object element = it.next();
			if (element instanceof TestSuiteItem) {
				try {
					selectedElements.add(Q7Utils.serializeToString((TestSuiteItem) element));
				} catch (IOException e) {
					Q7UIPlugin.log(e);
				}
			}
		}
		return selectedElements.toArray(new String[selectedElements.size()]);
	}

	private List<TestSuiteItem> deserializeItems(Object data) {
		List<TestSuiteItem> result = new ArrayList<TestSuiteItem>();
		if (data instanceof Object[]) {
			for (Object object : (Object[]) data) {
				if (object instanceof String) {
					try {
						result.add((TestSuiteItem) Q7Utils.deserializeFromstring((String) object));
					} catch (IOException e) {
						Q7UIPlugin.log(e);
					}
				}
			}
		}
		return result;
	}

	public Object[] getTableSelection() {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		return selection.toArray();
	}

	public String getContentName(Object content) {
		return content.toString();
	}

	private class ContentTableDropListener extends ViewerDropAdapter {

		public ContentTableDropListener(StructuredViewer structuredViewer) {
			super(structuredViewer);
		}

		@Override
		public boolean performDrop(Object data) {
			return doPerformDrop(data, getCurrentTarget(), getCurrentLocation());
		}

		@Override
		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			return isDropValid(target, operation, transferType, getCurrentLocation());
		}
	}

	private class ContentDragListener extends DragSourceAdapter {

		private final StructuredViewer structuredViewer;

		public ContentDragListener(StructuredViewer structuredViewer) {
			this.structuredViewer = structuredViewer;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) structuredViewer.getSelection();
			event.doit = !selection.isEmpty();
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) structuredViewer.getSelection();
			Object[] objects = convertSelectionToStrings(selection);
			Transfer transfer = getContentTransfer();
			if (transfer.isSupportedType(event.dataType)) {
				event.data = objects;
			}
		}
	}

	private class TestSuiteItemTransfer extends ArrayTransfer {
		private final String TYPE_NAME = "org.eclipse.rcptt.core.scenario.TestSuiteItem"; //$NON-NLS-1$
		private final int TYPE_ID = registerType(TYPE_NAME);

		@Override
		protected int[] getTypeIds() {
			return new int[] { TYPE_ID };
		}

		@Override
		protected String[] getTypeNames() {
			return new String[] { TYPE_NAME };
		}
	}

	private class TableContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List<?>) {
				return ((List<?>) inputElement).toArray();
			}
			return EMPTY;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public List<TestSuiteItem> getTestcases() {
		ArrayList<TestSuiteItem> result = new ArrayList<TestSuiteItem>();
		result.addAll(testSuite.getItems());
		return result;
	}

	public void updateItems() {
		for (TestSuiteItem item : testSuite.getItems()) {
			try {
				IQ7NamedElement[] element = testSuiteModel.getQ7Project().findNamedElement(item.getNamedElementId());
				if (element != null && element.length > 0) {
					String ename = element[0].getElementName();
					item.setNamedElemetName(ename);

					if (element[0] instanceof ITestCase) {
						item.setKind("test");
					} else if (element[0] instanceof ITestSuite) {
						item.setKind("suite");
					}
					IPath path = testSuiteModel.getPath().removeLastSegments(1);
					IPath iPath = element[0].getPath();
					IPath relativeTo = iPath.makeRelativeTo(path);

					item.setPath(relativeTo.toString());

				}
			} catch (ModelException e) {
				e.printStackTrace();
			}
		}
	}
}
