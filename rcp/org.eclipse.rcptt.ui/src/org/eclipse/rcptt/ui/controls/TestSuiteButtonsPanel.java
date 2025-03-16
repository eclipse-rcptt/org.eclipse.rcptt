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
package org.eclipse.rcptt.ui.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.ISearchScope;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.tags.Tag;
import org.eclipse.rcptt.core.utils.TagsUtil;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.ui.actions.Q7ElementLabelProvider;
import org.eclipse.rcptt.ui.commons.ModernElementListSelectionDialog;
import org.eclipse.rcptt.ui.tags.SelectTagsFilterDialog;
import org.eclipse.rcptt.ui.utils.WorkbenchUtils;

public class TestSuiteButtonsPanel extends Composite {

	private Button addTestCaseBtn;
	private Button addTaggedBtn;
	private Button addFolderBtn;
	private Button removeBtn;
	private Button removeAllBtn;

	private Button moveUpBtn;
	private Button moveDownBtn;

	private Link resetOrderLink;

	public TestSuiteButtonsPanel(Composite parent, boolean needOrderReset) {
		super(parent, SWT.NONE);
		setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.pack = false;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		setLayout(layout);
		createButtons(needOrderReset);
	}

	private void createButtons(boolean needOrderReset) {
		addTestCaseBtn = new Button(this, SWT.PUSH);
		addTestCaseBtn.setText(Messages.ScenariosLaunchTab_AddTestCaseButton);
		addTestCaseBtn.setImage(Images.getImage(Images.SCENARIO));

		addFolderBtn = new Button(this, SWT.PUSH);
		addFolderBtn.setText(Messages.ScenariosLaunchTab_AddFolderButton);
		addFolderBtn.setImage(Images.getImage(Images.NEW_FOLDER));

		addTaggedBtn = new Button(this, SWT.PUSH);
		addTaggedBtn.setText("Add with Tag");
		addTaggedBtn.setImage(Images.getImage(Images.TAG));

		removeBtn = new Button(this, SWT.PUSH);
		removeBtn.setText(Messages.ScenariosLaunchTab_RemoveButton);
		removeBtn.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_ETOOL_DELETE));

		removeAllBtn = new Button(this, SWT.PUSH);
		removeAllBtn.setText(Messages.ScenariosLaunchTab_RemoveAllButton);
		removeAllBtn.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_ETOOL_DELETE));

		new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
		moveUpBtn = new Button(this, SWT.PUSH);
		moveUpBtn.setText(Messages.ScenariosLaunchTab_MoveUpButton);

		moveDownBtn = new Button(this, SWT.PUSH);
		moveDownBtn.setText(Messages.ScenariosLaunchTab_MoveDownButton);

		if (needOrderReset) {
			Composite holder = new Composite(this, SWT.NONE);
			GridLayoutFactory.fillDefaults().applyTo(holder);
			resetOrderLink = new Link(holder, SWT.NONE);
			GridDataFactory.fillDefaults().
					grab(true, true).
					align(SWT.CENTER, SWT.CENTER).
					applyTo(resetOrderLink);
			resetOrderLink.setText("<a>Reset to default order</a>");
		}
	}

	public static IQ7NamedElement[] selectTestCase(ISearchScope scope,
			List<IQ7NamedElement> excludedElements, boolean includeTestSuites) {
		ModernElementListSelectionDialog dialog = new ModernElementListSelectionDialog(
				WorkbenchUtils.getShell(), new Q7ElementLabelProvider());
		Set<IQ7NamedElement> allElements = new HashSet<IQ7NamedElement>();

		IQ7NamedElement[] all = Q7SearchCore.findAllElements(scope);
		for (IQ7NamedElement iq7NamedElement : all) {
			if ((iq7NamedElement instanceof ITestCase || (iq7NamedElement instanceof ITestSuite && includeTestSuites))
					&& (excludedElements == null || !excludedElements
							.contains(iq7NamedElement))) {
				allElements.add(iq7NamedElement);
			}
		}
		dialog.setElements(allElements.toArray());
		dialog.setTitle(Messages.ScenariosLaunchTab_AddTestCaseDailogTitle);
		dialog.setMessage(Messages.ScenariosLaunchTab_AddTestCaseDailogMsg);
		dialog.setMultipleSelection(true);
		int result = dialog.open();
		if (result == Window.OK) {
			return Arrays.copyOf(dialog.getResult(), dialog.getResult().length,
					IQ7NamedElement[].class);
		}
		return new IQ7NamedElement[0];
	}

	public static IContainer selectFolder() {
		IContainer container = null;
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				WorkbenchUtils.getShell(), null, false,
				Messages.ScenariosLaunchTab_AddFolderDialogTitle);
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] result = dialog.getResult();
		if (result != null && result.length == 1) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			container = (IContainer) root.findMember((IPath) result[0]);
		}
		return container;
	}

	public static IQ7NamedElement[] selectTags(ISearchScope scope,
			List<IQ7NamedElement> excludedElements, boolean includeTestSuites) {
		Set<IQ7NamedElement> allElements = new HashSet<IQ7NamedElement>();
		SelectTagsFilterDialog dialog = new SelectTagsFilterDialog(WorkbenchUtils.getShell());

		int result = dialog.open();
		if (result == SelectTagsFilterDialog.OK) {
			List<Tag> tags = dialog.getSelectedTags();
			List<IQ7NamedElement> elements = dialog.getSelectedItems();
			IQ7NamedElement[] all = Q7SearchCore.findAllElements(scope);
			List<String> tagsValues = extractValues(tags);
			for (IQ7NamedElement iq7NamedElement : all) {
				try {
					if ((iq7NamedElement instanceof ITestCase || (iq7NamedElement instanceof ITestSuite && includeTestSuites))
							&& (excludedElements == null || !excludedElements.contains(iq7NamedElement))) {
						if (TagsUtil.hasAny(iq7NamedElement, tagsValues) || elements.contains(iq7NamedElement)) {
							allElements.add(iq7NamedElement);
						}
					}
				} catch (ModelException e) {
					e.printStackTrace();
				}
			}
		}

		return allElements.toArray(new IQ7NamedElement[0]);
	}

	private static List<String> extractValues(List<Tag> tags) {
		List<String> values = new ArrayList<String>();

		for (Tag tag : tags) {
			values.add(tag.getPath());
		}

		return values;
	}

	public void updateButtons(TableViewer viewer) {
		if (viewer != null && removeBtn != null && removeAllBtn != null) {
			Table table = viewer.getTable();
			int[] indices = table.getSelectionIndices();
			removeBtn.setEnabled(indices.length > 0);
			if (moveUpBtn != null && moveDownBtn != null) {
				moveUpBtn.setEnabled(indices.length > 0 && indices[0] > 0);
				moveDownBtn
						.setEnabled(indices.length > 0
								&& indices[indices.length - 1] < table
										.getItemCount() - 1);
			}
			removeAllBtn.setEnabled(table.getItemCount() > 0);
		}
	}

	public Button getAddTestCaseBtn() {
		return addTestCaseBtn;
	}

	public Button getAddFolderBtn() {
		return addFolderBtn;
	}

	public Button getAddTaggedBtn() {
		return addTaggedBtn;
	}

	public Button getRemoveBtn() {
		return removeBtn;
	}

	public Button getRemoveAllBtn() {
		return removeAllBtn;
	}

	public Button getMoveUpBtn() {
		return moveUpBtn;
	}

	public Button getMoveDownBtn() {
		return moveDownBtn;
	}

	public Link getResetOrderLink() {
		return resetOrderLink;
	}

}
