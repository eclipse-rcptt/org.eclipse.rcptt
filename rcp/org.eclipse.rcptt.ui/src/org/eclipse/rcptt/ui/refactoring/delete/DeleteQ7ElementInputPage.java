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
package org.eclipse.rcptt.ui.refactoring.delete;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.actions.Q7ElementLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class DeleteQ7ElementInputPage extends UserInputWizardPage {

	private final DeleteQ7ElementProcessor refactoringProcessor;
	private Button fDeleteContentsButton;
	private Button deleteReferencesButton;

	public DeleteQ7ElementInputPage(DeleteQ7ElementProcessor processor) {
		super("RenameResourceRefactoringInputPage"); //$NON-NLS-1$
		refactoringProcessor = processor;
	}

	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		Point defaultSpacing = LayoutConstants.getSpacing();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2)
				.spacing(defaultSpacing.x * 2, defaultSpacing.y)
				.applyTo(composite);
		composite.setFont(parent.getFont());

		Display display = parent.getDisplay();
		Image image = new Image(display,
				display.getSystemImage(SWT.ICON_QUESTION), SWT.IMAGE_COPY);
		Label imageLabel = new Label(composite, SWT.NULL);
		imageLabel.setBackground(image.getBackground());
		imageLabel.setImage(image);
		imageLabel.addDisposeListener(event -> image.dispose());
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.BEGINNING)
				.span(1, 2).applyTo(imageLabel);

		IResource[] resources = refactoringProcessor.getResources();
		String message;
		if (resources.length == 1) {
			IResource resource = resources[0];
			message = Messages
					.bind(Messages.DeleteContextInputPage_OneElementMsg,
							getName(resource));
		} else {
			message = Messages.bind(
					Messages.DeleteContextInputPage_ManyElementsMsg,
					resources.length);
		}
		createLabel(composite, message);

		createSupportArea(composite);

		setControl(composite);
	}

	private String getName(IResource resource) {
		try {
			if (resource instanceof IFile) {
				IQ7NamedElement element = (IQ7NamedElement) RcpttCore.create(resource);
				if (element != null) {
					return element.getElementName();
				} else {
					return resource.getName();
				}
			}
		} catch (Exception e) {
			Q7UIPlugin.log(e);
		}
		return resource.getName();
	}

	private void createSupportArea(Composite composite) {
		Composite supportArea = new Composite(composite, SWT.NONE);
		supportArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				2, 1));
		Point defaultSpacing = LayoutConstants.getSpacing();
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = defaultSpacing.x * 2;
		gridLayout.verticalSpacing = defaultSpacing.y;
		supportArea.setLayout(gridLayout);

		if (containsOnlyProjects()) {
			fDeleteContentsButton = new Button(supportArea, SWT.CHECK);
			fDeleteContentsButton.setFont(composite.getFont());
			fDeleteContentsButton.setLayoutData(new GridData(SWT.BEGINNING,
					SWT.CENTER, false, false, 1, 1));
			fDeleteContentsButton.setText(Messages.DeleteContextInputPage_DeleteContentCheckbox);
			fDeleteContentsButton.setFocus();
		}

		final int count = refactoringProcessor.getReferencesCount();
		if (count > 0) {
			Composite deleteReferencesComposite = new Composite(supportArea,
					SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true)
					.applyTo(deleteReferencesComposite);
			GridLayoutFactory.fillDefaults().numColumns(2)
					.applyTo(deleteReferencesComposite);
			deleteReferencesButton = new Button(deleteReferencesComposite,
					SWT.CHECK);
			deleteReferencesButton.setFont(composite.getFont());
			deleteReferencesButton.setLayoutData(new GridData(SWT.BEGINNING,
					SWT.CENTER, false, false, 1, 1));
			deleteReferencesButton.setText(Messages.DeleteContextInputPage_DeleteReferencesLabel);
			deleteReferencesButton.setSelection(refactoringProcessor
					.isDeleteReferences());
			deleteReferencesButton.setFocus();

			Hyperlink link = new Hyperlink(deleteReferencesComposite, SWT.WRAP);
			String message = Messages
					.bind(Messages.DeleteContextInputPage_SeeReferencesLink, count);
			link.setText(message);
			link.setUnderlined(true);
			link.setHref(message.substring(0, message.length() - 2));
			link.setForeground(JFaceColors.getHyperlinkText(link.getShell()
					.getDisplay()));

			final StructuredViewer viewer = new TreeViewer(supportArea);
			final Control control = viewer.getControl();
			GridDataFactory.fillDefaults().span(2, 1).hint(420, 150)
					.grab(true, true).exclude(true).applyTo(control);

			viewer.setContentProvider(new ReferencesContentProvider());
			viewer.setInput(new Object());
			viewer.setLabelProvider(new Q7ElementLabelProvider());

			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					Control control = viewer.getControl();
					((GridData) control.getLayoutData()).exclude = false;
					if (viewer instanceof TreeViewer) {
						((TreeViewer) viewer).expandAll();
					}
					control.getShell().pack();
				}
			});
		}
	}

	private void createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.WRAP);
		label.setFont(parent.getFont());
		label.setText(text);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label.setLayoutData(gridData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.ui.refactoring.UserInputWizardPage#performFinish()
	 */
	@Override
	protected boolean performFinish() {
		initializeRefactoring();
		storeSettings();
		return super.performFinish();
	}

	@Override
	public IWizardPage getNextPage() {
		initializeRefactoring();
		storeSettings();
		return super.getNextPage();
	}

	private void storeSettings() {
	}

	@SuppressWarnings("restriction")
	private void initializeRefactoring() {
		refactoringProcessor
				.setDeleteContents(fDeleteContentsButton == null ? false
						: fDeleteContentsButton.getSelection());
		refactoringProcessor
				.setDeleteReferences(deleteReferencesButton == null ? false
						: deleteReferencesButton.getSelection());
	}

	private final class ReferencesContentProvider implements
			ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return refactoringProcessor.getReferencedElements();
		}

		public Object[] getChildren(Object parentElement) {
			return refactoringProcessor.getReferencingElements(parentElement);
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element) != null;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	@SuppressWarnings("restriction")
	public boolean containsOnlyProjects() {
		return Resources.containsOnlyProjects(refactoringProcessor
				.getResourcesToDelete());
	}
}
