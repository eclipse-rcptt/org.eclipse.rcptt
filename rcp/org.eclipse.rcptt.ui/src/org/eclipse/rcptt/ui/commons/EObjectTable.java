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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.viewers.IViewerObservableList;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Element.HandleType;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.ModelManager;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.actions.SelectionAction;
import org.eclipse.rcptt.ui.controls.AbstractEmbeddedComposite;
import org.eclipse.rcptt.ui.editors.NamedElementEditorActions.INamedElementActions;
import org.eclipse.rcptt.ui.panels.Actions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;

import com.google.common.collect.Iterables;

public abstract class EObjectTable extends AbstractEmbeddedComposite implements
		IContentNamer, ISelectionActionsHandler, INamedElementActions {

	protected TableViewer viewer;

	protected EObject content;
	private final EStructuralFeature feature;
	private ToolBar toolBar;

	public EObjectTable(EObject content, EStructuralFeature feature) {
		this.content = content;
		this.feature = feature;
	}

	protected int getStyle() {
		return SWT.BORDER | SWT.MULTI;
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Table table = new Table(parent, getStyle());
		viewer = new TableViewer(table);
		configureViewer(viewer);

		viewer.getControl().addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if ((SWT.DEL == e.character) && (0 == e.stateMask)
						&& canRemove()) {
					handleRemove();
					e.doit = false;
				} else if (isCharAndModifier(e, SWT.MOD1, 'x') && canCut()) {
					cut();
					e.doit = false;
				} else if (isCharAndModifier(e, SWT.MOD1, 'c') && canCopy()) {
					copy();
					e.doit = false;
				} else if (isCharAndModifier(e, SWT.MOD1, 'v') && canPaste()) {
					paste();
					e.doit = false;
				} else if (e.stateMask == SWT.MOD1 && e.keyCode == SWT.ARROW_UP) {
					handleUp();
					e.doit = false;
				} else if (e.stateMask == SWT.MOD1
						&& e.keyCode == SWT.ARROW_DOWN) {
					handleDown();
					e.doit = false;
				}
			}
		});
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		if (isInternalDragAndDropEnabled()) {
			initializeInternalDrapAndDrop();
		}
		MenuManager popupMenuManager = new MenuManager();
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager mng) {
				fillContextMenu(mng);
			}
		};
		popupMenuManager.addMenuListener(listener);
		popupMenuManager.setRemoveAllWhenShown(true);
		Menu menu = popupMenuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// update(content);
	}

	protected void configureViewer(TableViewer viewer) {
		viewer.setLabelProvider(createLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
	}

	public void update(EObject content) {
		this.content = content;
		// if (viewerBinding != null) {
		// viewerBinding.dispose();
		// }
		// viewerBinding =
		// dbc.bindValue(ViewersObservables.observeInput(viewer),
		// EMFObservables.observeValue(content, feature));

		if (contentObservable != null) {
			contentObservable.dispose();
		}

		contentObservable = EMFObservables.observeValue(content, feature);
		contentObservable.addChangeListener(new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				refresh(false);
			}
		});
		contentObservable.addValueChangeListener(new IValueChangeListener<Object>() {
			public void handleValueChange(ValueChangeEvent<? extends Object> event) {
				refresh(false);
			}
		});

		viewer.setInput(getViewerContents());
		if (removeToolBinding != null) {
			removeToolBinding.dispose();
		}
		removeToolBinding = dbc.bindValue(Actions.observeEnabled(removeTool),
				removeToolComputedValue);
		if (upToolBinding != null) {
			upToolBinding.dispose();
		}
		upToolBinding = dbc.bindValue(Actions.observeEnabled(upTool),
				upToolComputedValue);
		if (downToolBinding != null) {
			downToolBinding.dispose();
		}
		downToolBinding = dbc.bindValue(Actions.observeEnabled(downTool),
				downToolComputedValue);

		// Add refresh job for indexing

		new Job("Update references after indexing") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ModelManager.getModelManager().getIndexManager()
						.waitUntilReady(monitor);
				refresh(false);
				return Status.OK_STATUS;
			}

		}.schedule();
	}

	private static boolean isCharAndModifier(KeyEvent e, int modifiers, int ch) {
		return Character.toLowerCase(e.keyCode) == ch
				&& e.stateMask == modifiers;
	}

	public Control getControl() {
		return viewer.getControl();
	}

	public void refresh() {
		refresh(true);
	}

	private void refresh(final boolean setInput) {
		if (getControl().isDisposed()) {
			return;
		}
		getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!viewer.getTable().isDisposed()) {
					if (setInput) {
						viewer.setInput(getViewerContents());
					}
					viewer.refresh(true);
				}
			}
		});
	}

	@Override
	public void createToolBar(Composite parent) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT
				| SWT.HORIZONTAL);
		fillActions(toolBarManager);
		toolBar = toolBarManager.createControl(parent);
		toolBar.moveAbove(null);
	}

	@Override
	public ToolBar getToolBar() {
		return toolBar;
	}

	protected boolean isInternalDragAndDropEnabled() {
		return true;
	}

	protected void initializeInternalDrapAndDrop() {
		int ops = DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[] { getContentTransfer() };
		viewer.addDragSupport(ops, transferTypes, new ContentDragListener(
				viewer));

		transferTypes = getDropTransferTypes();
		viewer.addDropSupport(ops, transferTypes, new ContentTableDropListener(
				viewer));
	}

	protected Transfer[] getDropTransferTypes() {
		return new Transfer[] { getContentTransfer() };
	}

	protected abstract ILabelProvider createLabelProvider();

	@Override
	protected void fillActions(IToolBarManager toolBarManager) {
		viewerSelection = ViewerProperties.multipleSelection().observe(viewer);

		addTool = new Action() {
			@Override
			public void run() {
				Object[] newItems = handleAdd();
				doAdd(newItems);
			}
		};
		addTool.setText(Messages.EObjectTable_Add);
		addTool.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
		toolBarManager.add(addTool);

		// Remove action
		removeTool = new Action() {
			@Override
			public void run() {
				handleRemove();
			}
		};
		removeTool.setText(Messages.EObjectTable_Remove);
		removeTool.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));
		removeTool.setDisabledImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE_DISABLED));

		removeToolComputedValue = new ComputedValue<Boolean>() {
			@Override
			protected Boolean calculate() {
				return canRemove();
			}
		};

		toolBarManager.add(removeTool);

		upToolComputedValue = new ComputedValue<Boolean>() {
			@Override
			protected Boolean calculate() {
				return calculateUpToolEnablement();
			}
		};
		upTool = new Action() {
			@Override
			public void run() {
				handleUp();
			}
		};
		upTool.setText(Messages.EObjectTable_Up);
		upTool.setImageDescriptor(Images.getImageDescriptor(Images.UP));

		toolBarManager.add(upTool);

		downToolComputedValue = new ComputedValue<Boolean>() {
			@Override
			protected Boolean calculate() {
				return calculateDownToolEnablement();
			}
		};
		downTool = new Action() {
			@Override
			public void run() {
				handleDown();
			}
		};
		downTool.setText(Messages.EObjectTable_Down);
		downTool.setImageDescriptor(Images.getImageDescriptor(Images.DOWN));
		toolBarManager.add(downTool);

		update(content);
	}

	@SuppressWarnings("unchecked")
	protected EList<Object> getContents() {
		return (EList<Object>) content.eGet(feature);
	}

	protected List<Object> getViewerContents() {
		return getContents();
	}

	protected abstract Object[] handleAdd();

	public Object[] getSelection() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		return selection.toArray();
	}

	public void setSelection(Object[] elements, boolean reveal) {
		IStructuredSelection selection = new StructuredSelection(elements);
		viewer.setSelection(selection, reveal);
	}

	private SelectionAction.RemoveAction removeAction;
	private SelectionAction.CutAction cutAction;
	private SelectionAction.CopyAction copyAction;
	private SelectionAction.PasteAction pasteAction;

	private Action addTool;
	private Action removeTool;
	private Action upTool;
	private Action downTool;

	// private Binding viewerBinding;

	private IViewerObservableList<Object> viewerSelection;

	private ComputedValue<Boolean> removeToolComputedValue;

	private ComputedValue<Boolean> upToolComputedValue;

	private ComputedValue<Boolean> downToolComputedValue;

	private Binding removeToolBinding;

	private Binding upToolBinding;

	private Binding downToolBinding;

	private IObservableValue<?> contentObservable;

	protected abstract Class<?> getContentsType();

	protected abstract Transfer getContentTransfer();

	public void handleRemove() {
		Object[] items = getSelection();
		getContents().removeAll(Arrays.asList(items));
		viewer.setInput(getViewerContents());
	}

	public void cut() {
		copy();
		handleRemove();
	}

	public void copy() {
		ClipboardUtils.setClipboardContents(getSelection(),
				getContentTransfer(), this);
	}

	public void paste() {
		doAdd(ClipboardUtils.getClipboardContents(getContentTransfer()));
		doAdd(ClipboardUtils.getClipboardContents(ResourceTransfer
				.getInstance()));
		refresh();
	}

	protected boolean canAdd(Object[] objects) {
		if ((null == objects) || (0 == objects.length)) {
			return false;
		}
		for (Object obj : objects) {
			if (obj instanceof IResource) {
				IQ7Element file = RcpttCore.create((IResource) obj);
				if (file.getElementType().equals(getAppropriateHandleType())) {
					if (file.exists()) {
						return true;
					}
				}
			}
			if (!getContentsType().isInstance(obj)) {
				return false;
			}
		}
		return true;
	}

	protected abstract HandleType getAppropriateHandleType();

	protected void doAdd(Object[] objects) {
		if (null == objects) {
			return;
		}
		List<Object> content = getContents();
		for (Object obj : objects) {
			Object val = obj;
			if (obj instanceof IResource) {
				IQ7Element file = RcpttCore.create((IResource) obj);
				if (file.getElementType().equals(getAppropriateHandleType())) {
					if (file.exists()) {
						try {
							val = ((IQ7NamedElement) file).getID();
						} catch (ModelException e) {
							Q7UIPlugin.log(e);
						}
					}
				}
			}

			if (!content.contains(val)) {
				content.add(val);
			}
		}
		viewer.setInput(getViewerContents());
		setSelection(objects, true);
	}

	public boolean canRemove() {
		if (viewerSelection.isEmpty() || viewerSelection.isDisposed()) {
			return false;
		}
		Object object = viewerSelection.get(0);
		EList<Object> contents = getContents();
		List<Object> viewerContents = new ArrayList<Object>(getViewerContents());
		viewerContents.removeAll(contents);
		if (viewerContents.contains(object)) {
			return false;
		}
		return !viewer.getSelection().isEmpty();
	}

	public boolean canCut() {
		if (viewerSelection.isEmpty() || viewerSelection.isDisposed()) {
			return false;
		}
		Object object = viewerSelection.get(0);
		EList<Object> contents = getContents();
		List<Object> viewerContents = new ArrayList<Object>(getViewerContents());
		viewerContents.removeAll(contents);
		if (viewerContents.contains(object)) {
			return false;
		}
		return viewer.getControl().isFocusControl() && !viewer.getSelection().isEmpty();
	}

	public boolean canCopy() {
		return viewer.getControl().isFocusControl() && !viewer.getSelection().isEmpty();
	}

	public boolean canPaste() {
		return viewer.getControl().isFocusControl() && canAdd(ClipboardUtils.getClipboardContents(getContentTransfer()))
				|| canAdd(ClipboardUtils.getClipboardContents(ResourceTransfer
						.getInstance()));
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

	public String getContentName(Object content) {
		return content.toString();
	}

	private class ContentDragListener extends DragSourceAdapter {

		private final StructuredViewer structuredViewer;

		public ContentDragListener(StructuredViewer structuredViewer) {
			this.structuredViewer = structuredViewer;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			event.doit = allowDrag((IStructuredSelection) structuredViewer
					.getSelection());
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) structuredViewer
					.getSelection();
			List<?> selectedElements = selection.toList();
			Object[] objects = selectedElements
					.toArray(new Object[] { selection.size() });
			Transfer transfer = getContentTransfer();
			if (transfer.isSupportedType(event.dataType)) {
				event.data = objects;
			}
		}
	}

	protected boolean allowDrag(IStructuredSelection selection) {
		return selection.size() == 1;
	}

	protected boolean doPerformDrop(Object data, Object currentTarget,
			int currentLocation) {
		Object[] toDrop = (Object[]) data;
		for (Object toDropObject : toDrop) {
			if (toDropObject.equals(currentTarget)) {
				return false;
			}
		}

		EList<Object> content = getContents();
		int targetPosition = content.size() - 1;
		if (currentTarget != null) {
			targetPosition = content.indexOf(currentTarget);
		}

		for (int i = 0; i < toDrop.length; i++) {
			Object toDropObject = toDrop[i];
			int oldPosition = content.indexOf(toDropObject);

			if (oldPosition == -1) {
				if (currentLocation == ViewerDropAdapter.LOCATION_AFTER
						|| currentLocation == ViewerDropAdapter.LOCATION_NONE) {
					targetPosition++;
				}
				if (targetPosition != -1) {
					content.add(targetPosition, toDropObject);
				} else {
					content.add(toDropObject);
				}
			} else if (oldPosition < targetPosition
					&& currentLocation == ViewerDropAdapter.LOCATION_BEFORE) {
				content.move(targetPosition - 1, oldPosition);
			} else if (oldPosition > targetPosition
					&& currentLocation == ViewerDropAdapter.LOCATION_AFTER) {
				content.move(targetPosition + 1, oldPosition);
			} else {
				if (targetPosition < 0) {
					return false;
				}
				content.move(targetPosition, oldPosition);
			}
		}
		refresh();
		return true;
	}

	protected boolean doValidateDrop(Object target, int operation,
			TransferData transferType, int currentLocation) {
		return getContentTransfer().isSupportedType(transferType)
				&& currentLocation != ViewerDropAdapter.LOCATION_ON;
	}

	private void handleUp() {
		Object item = getSelection()[0];
		EList<Object> list = getContents();
		int index = list.indexOf(item);
		getContents().move(index - 1, index);
		viewerSelection.clear();
		viewerSelection.add(item);
		viewer.setInput(getViewerContents());
	}

	private void handleDown() {
		Object item = getSelection()[0];
		EList<Object> list = getContents();
		int index = list.indexOf(item);
		getContents().move(index + 1, index);
		viewerSelection.clear();
		viewerSelection.add(item);
		viewer.setInput(getViewerContents());
	}

	protected boolean calculateUpToolEnablement() {
		if (viewerSelection.isEmpty() || viewerSelection.isDisposed()) {
			return false;
		}

		Object object = viewerSelection.get(0);
		EList<Object> contents = getContents();
		List<Object> viewerContents = new ArrayList<Object>(getViewerContents());
		viewerContents.removeAll(contents);
		if (viewerContents.contains(object)) {
			return false;
		}
		Object first = Iterables.getFirst(contents, null);
		return object != first;
	}

	private boolean calculateDownToolEnablement() {
		if (viewerSelection.isEmpty() || viewerSelection.isDisposed()) {
			return false;
		}
		Object object = viewerSelection.get(viewerSelection.size() - 1);
		EList<Object> contents = getContents();
		List<Object> viewerContents = new ArrayList<Object>(getViewerContents());
		viewerContents.removeAll(contents);
		if (viewerContents.contains(object)) {
			return false;
		}
		Object last = Iterables.getLast(contents, null);
		return object != last;
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
		public boolean validateDrop(Object target, int operation,
				TransferData transferType) {
			return doValidateDrop(target, operation, transferType,
					getCurrentLocation());
		}
	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void redo() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public void dispose() {
		if (contentObservable != null) {
			contentObservable.dispose();
		}
		super.dispose();
	}
	
}
