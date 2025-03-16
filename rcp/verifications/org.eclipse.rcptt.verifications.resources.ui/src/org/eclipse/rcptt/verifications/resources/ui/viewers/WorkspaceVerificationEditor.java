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
package org.eclipse.rcptt.verifications.resources.ui.viewers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.core.workspace.Q7Utils;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.resources.ImportUtils;
import org.eclipse.rcptt.resources.WSUtils;
import org.eclipse.rcptt.resources.WorkspaceDataImporter;
import org.eclipse.rcptt.resources.ui.actions.WSAction;
import org.eclipse.rcptt.resources.ui.viewers.WorkspaceContentProvider;
import org.eclipse.rcptt.resources.ui.viewers.WorkspaceLabelProvider;
import org.eclipse.rcptt.ui.controls.SectionWithComposite;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.utils.DefaultTreeViewerEditStrategy;
import org.eclipse.rcptt.ui.utils.UIContentAdapter;
import org.eclipse.rcptt.ui.verification.BaseVerificationEditor;
import org.eclipse.rcptt.workspace.WSContainer;
import org.eclipse.rcptt.workspace.WSFile;
import org.eclipse.rcptt.workspace.WSFileLink;
import org.eclipse.rcptt.workspace.WSFolder;
import org.eclipse.rcptt.workspace.WSProject;
import org.eclipse.rcptt.workspace.WSProjectLink;
import org.eclipse.rcptt.workspace.WSResource;
import org.eclipse.rcptt.workspace.WSRoot;
import org.eclipse.rcptt.workspace.WorkspacePackage;
import org.eclipse.rcptt.workspace.WorkspaceVerification;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class WorkspaceVerificationEditor extends BaseVerificationEditor {

	private boolean corrected = false;

	private EContentAdapter adapter;
	private TreeViewer viewer;
	private final List<Button> buttons = new ArrayList<Button>();

	private WSAction.Remove removeAction;
	private WSAction.OpenFile openFileAction;

	public WorkspaceVerification getVerificationElement() {
		try {
			return (WorkspaceVerification) super.getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
		}
		return null;
	}

	private static IFolder getFolder(IFile file) {
		return getFolder(file, "");
	}

	private static IFolder getFolder(IFile file, String postfix) {
		String folderName = file.getName().replaceAll("\\.", "");
		return file.getParent().getFolder(new Path("." + folderName + postfix));
	}

	private IResource[] getRelativeResources(Verification verification, IFile iFile) {
		IFile file = null;
		if (iFile == null) {
			file = Q7Utils.getLocation(verification);
		} else {
			file = iFile;
		}

		if (file != null) {
			List<IResource> result = new ArrayList<IResource>();
			IFolder folder = getFolder(file);
			if (folder.exists()) {
				result.add(folder);
			}
			folder = getFolder(file, "wc");
			if (folder.exists()) {
				result.add(folder);
			}

			return result.toArray(new IResource[result.size()]);
		}
		return new IFolder[0];
	}

	@Override
	public void setElement(IQ7NamedElement context) {
		super.setElement(context);
		corrected = false;
		// Convert previous scenario to new one format
		IResource[] resources = getRelativeResources(getVerificationElement(), null);
		if (resources != null && resources.length > 0) {
			IPersistenceModel model = PersistenceManager.getInstance()
					.getModel(getVerificationElement().eResource());
			// Convert resources to new format
			WorkspaceDataImporter importer = new WorkspaceDataImporter(
					getVerificationElement());

			EList<WSProject> projects = getVerificationElement().getContent()
					.getProjects();
			for (WSProject wsProject : projects) {
				WSProject ownProject = WSUtils.getProject(getVerificationElement()
						.getContent(), wsProject.getName(), true);
				ImportUtils.importFiles(ownProject, wsProject.getFiles(),
						model, importer);
				ImportUtils.importFolders(ownProject, wsProject.getFolders(),
						model, importer);
			}
			corrected = true;
		}
	}

	@Override
	public void setSelectionAtLine(int line) {
	}

	@Override
	public void save() throws CoreException {
		if (corrected) {
			IResource[] resources = getRelativeResources(getVerificationElement(),
					null);
			if (resources != null && resources.length > 0) {
				// Remove old resources, since we already corrected context
				for (IResource iResource : resources) {
					if (iResource.exists()) {
						iResource.delete(true, new NullProgressMonitor());
					}
				}
			}
		}
		super.save();
	}

	@Override
	public Control create(Composite parent, FormToolkit toolkit,
			IWorkbenchSite site, EditorHeader header) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR
				| Section.TWISTIE | Section.EXPANDED
				| Section.NO_TITLE_FOCUS_BOX);
		section.setText("Workspace Options");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
		Composite optionsComposite = toolkit.createComposite(section);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5)
				.numColumns(1).applyTo(optionsComposite);
		section.setClient(optionsComposite);

		createOptionsControls(optionsComposite, toolkit);

		Section wsSection = new SectionWithComposite("Workspace",
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED)
				.numColumns(2).create(parent, toolkit);
		Composite wsComposite = (Composite) wsSection.getClient();

		createTree(wsComposite, toolkit);
		createPanel(wsComposite, toolkit);

		return section;
	}

	private void createOptionsControls(Composite parent, FormToolkit toolkit) {
		createAllowUncapturedControls(parent, toolkit);
		createIgnoreWhiteSpaceControls(parent, toolkit);
		createIgnoredLinesControls(parent, toolkit);
	}
	
	private void createAllowUncapturedControls(Composite parent, FormToolkit toolkit) {
		Button allowUncapturedFilesCheckbox = toolkit.createButton(parent, "Allow uncaptured files", SWT.CHECK);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(allowUncapturedFilesCheckbox);

		Label notAllowedPatternsLabel = new Label(parent, SWT.NONE);
		notAllowedPatternsLabel.setText(
				"Do not allow following uncaptured files or files in following folders (use a comma as separator, * = any string):");
		notAllowedPatternsLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		notAllowedPatternsLabel.setBackground(null);
		GridDataFactory.fillDefaults().span(2, 1).hint(100, SWT.DEFAULT)
				.applyTo(notAllowedPatternsLabel);

		Text notAllowedPatternsText = toolkit.createText(parent, "", SWT.BORDER);
		notAllowedPatternsText.setBackground(null);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
				.hint(100, SWT.DEFAULT).applyTo(notAllowedPatternsText);

		IObservableValue<Boolean> allowUncapturedFiles = EMFObservables.observeValue(
				getVerificationElement(),
				WorkspacePackage.Literals.WORKSPACE_VERIFICATION__ALLOW_UNCAPTURED_FILES);
		dbc.bindValue(WidgetProperties.enabled().observe(notAllowedPatternsText),
				allowUncapturedFiles);

		dbc.bindValue(WidgetProperties.enabled().observe(notAllowedPatternsLabel),
				allowUncapturedFiles);

		dbc.bindValue(
				WidgetProperties.buttonSelection().observe(allowUncapturedFilesCheckbox),
				allowUncapturedFiles);

		IObservableValue<String> notAllowedPatterns = EMFObservables.observeValue(
						getVerificationElement(),
						WorkspacePackage.Literals.WORKSPACE_VERIFICATION__NOT_ALLOWED_PATTERNS);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(notAllowedPatternsText),
				notAllowedPatterns);
	}

	private void createIgnoreWhiteSpaceControls(Composite parent, FormToolkit toolkit) {
		Button ignoreWhiteSpaceCheckbox = toolkit.createButton(parent, "Ignore white space", SWT.CHECK);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(ignoreWhiteSpaceCheckbox);

		IObservableValue<Boolean> ignoerWhiteSpaceCheckbox = EMFObservables.observeValue(
				getVerificationElement(),
				WorkspacePackage.Literals.WORKSPACE_VERIFICATION__IGNORE_WHITE_SPACE);
		dbc.bindValue(
				WidgetProperties.buttonSelection().observe(ignoreWhiteSpaceCheckbox),
				ignoerWhiteSpaceCheckbox);
	}

	private void createIgnoredLinesControls(Composite parent, FormToolkit toolkit) {
		Label ignoredLinePatternsLabel = new Label(parent, SWT.NONE);
		ignoredLinePatternsLabel.setText(
				"Ignore lines matched by following patterns (one regex per line):");
		ignoredLinePatternsLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		ignoredLinePatternsLabel.setBackground(null);
		GridDataFactory.fillDefaults().span(2, 1).hint(100, SWT.DEFAULT)
				.applyTo(ignoredLinePatternsLabel);

		Text ignoredLinePatternsText = toolkit.createText(parent, "", SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		ignoredLinePatternsText.setBackground(null);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 6)
				.hint(100, SWT.DEFAULT).applyTo(ignoredLinePatternsText);

		IObservableValue<String> ignoredLinePatterns = EMFObservables.observeValue(
				getVerificationElement(),
				WorkspacePackage.Literals.WORKSPACE_VERIFICATION__IGNORED_LINES);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(ignoredLinePatternsText),
				ignoredLinePatterns);
	}

	private Tree createTree(Composite parent, FormToolkit toolkit) {
		Tree tree = new Tree(parent, SWT.BORDER | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).hint(100, 50).applyTo(tree);

		viewer = new TreeViewer(tree);
		TreeViewerEditor.create(viewer, new DefaultTreeViewerEditStrategy(viewer), ColumnViewerEditor.DEFAULT);
		viewer.setCellEditors(new CellEditor[] { new TextCellEditor(tree) });
		viewer.setCellModifier(new ICellModifier() {
			public void modify(Object element, String property, Object value) {
				TreeItem item = (TreeItem) element;
				WSResource res = (WSResource) item.getData();
				WSContainer parent = (WSContainer) res.eContainer();
				Set<String> allNames = getAllNames(parent);
				allNames.remove(res.getName());

				String newValue = (String) value;
				if (allNames.contains(newValue)) {
					return;
				}
				if (newValue != null && !newValue.equals(res.getName())) {
					try {
						IPersistenceModel model = getElement()
								.getPersistenceModel();

						if (res instanceof WSFile) {
							String oldName = ImportUtils.getName((WSFile) res);
							res.setName(newValue);
							String newName = ImportUtils.getName((WSFile) res);
							model.rename(oldName, newName);
						} else if (res instanceof WSContainer) {
							// Rename all stored files.
							List<String> oldNames = new ArrayList<String>();
							List<WSFile> allFiles = findChildFiles((WSContainer) res);
							for (WSFile wsFile : allFiles) {
								oldNames.add(ImportUtils.getName(wsFile));
							}
							res.setName(newValue);
							for (int i = 0; i < oldNames.size(); i++) {
								String oldName = oldNames.get(i);
								WSFile file = allFiles.get(i);
								String newName = ImportUtils.getName(file);
								model.rename(oldName, newName);
							}
						}
					} catch (Exception e) {
						Q7UIPlugin.log(e);
					}
				}
			}

			public Object getValue(Object element, String property) {
				return ((WSResource) element).getName();
			}

			public boolean canModify(Object element, String property) {
				return true;
			}
		});
		viewer.setColumnProperties(new String[] { "o" });
		viewer.setContentProvider(new WorkspaceContentProvider());
		ILabelDecorator decorator = PlatformUI.getWorkbench()
				.getDecoratorManager().getLabelDecorator();
		viewer.setLabelProvider(new DecoratingStyledCellLabelProvider(
				new WorkspaceLabelProvider(), decorator, null));
		viewer.setInput(getVerificationElement());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Object[] res = sel.toArray();
				WSResource[] resources = new WSResource[res.length];
				for (int i = 0 ; i < res.length; i++)
					resources[i] = (WSResource) res[i];
				setSelection(resources);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			/**
			 * Opens file or expands/collapses folder.
			 */
			public void doubleClick(DoubleClickEvent event) {
				if (openFileAction.isEnabled()) {
					openFileAction.run();
				} else {
					TreeViewer viewer = (TreeViewer) event.getViewer();
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					Object selectedNode = selection.getFirstElement();
					viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
				}
			}
		});
		viewer.getControl().addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if ((SWT.DEL == e.character) && (0 == e.stateMask)) {
					if (removeAction.isEnabled()) {
						removeAction.run();
						e.doit = false;
					}
				}
			}
		});

		adapter = new UIContentAdapter() {

			protected void changed(Notification notification) {
				if (viewer.getControl().isDisposed()) {
					return;
				}
				viewer.refresh();
			}
		};
		getVerificationElement().eAdapters().add(adapter);

		toolkit.adapt(tree);

		return tree;
	}

	protected List<WSFile> findChildFiles(WSContainer res) {
		List<WSFile> allFiles = new ArrayList<WSFile>();
		if (res instanceof WSFolder) {
			EList<WSFolder> folders = ((WSFolder) res).getFolders();
			allFiles.addAll(((WSFolder) res).getFiles());
			for (WSFolder folder : folders) {
				allFiles.addAll(findChildFiles(folder));
			}
		}
		return allFiles;
	}

	private Composite createPanel(Composite parent, FormToolkit toolkit) {
		Composite panel = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(panel);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.pack = false;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginTop = 0;
		layout.marginRight = 0;
		panel.setLayout(layout);
		createButton(panel, toolkit, openFileAction = new WSAction.OpenFile());
		createButton(panel, toolkit, removeAction = new WSAction.Remove());

		setSelection(null);
		return panel;
	}

	private void createButton(Composite parent, FormToolkit toolkit,
			final WSAction action) {
		action.setViewer(viewer);
		Button button = toolkit.createButton(parent, action.getText(), SWT.PUSH);
		button.setData(action);
		Image image = action.getImageDescriptor().createImage();
		button.addDisposeListener(event -> image.dispose());
		button.setImage(image);
		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				action.run();
			}
		});
		buttons.add(button);
	}

	private void setSelection(WSResource[] selection) {
		for (Button button : buttons) {
			WSAction action = (WSAction) button.getData();
			action.setSelection(selection);
			button.setEnabled(action.isEnabled());
		}
	}

	public boolean isAutoSaveEnabled() {
		return true;
	}

	private Set<String> getAllNames(WSContainer parent) {
		Set<String> allNames = new HashSet<String>();
		if (parent instanceof WSFolder) {
			EList<WSFolder> folders = ((WSFolder) parent).getFolders();
			EList<WSFile> files = ((WSFolder) parent).getFiles();
			EList<WSFileLink> fileLinks = ((WSFolder) parent).getFileLinks();
			for (WSFileLink link : fileLinks) {
				allNames.add(link.getName());
			}
			for (WSFile wsFile : files) {
				allNames.add(wsFile.getName());
			}
			for (WSFolder folder : folders) {
				allNames.add(folder.getName());
			}
		} else if (parent instanceof WSRoot) {
			EList<WSProject> projects = ((WSRoot) parent).getProjects();
			for (WSProject wsProject : projects) {
				allNames.add(wsProject.getName());
			}
			EList<WSProjectLink> links = ((WSRoot) parent).getProjectLinks();
			for (WSProjectLink wsProjectLink : links) {
				allNames.add(wsProjectLink.getName());
			}
		}
		return allNames;
	}

}
