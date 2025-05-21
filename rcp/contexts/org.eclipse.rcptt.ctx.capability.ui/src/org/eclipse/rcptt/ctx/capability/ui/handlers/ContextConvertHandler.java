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
package org.eclipse.rcptt.ctx.capability.ui.handlers;

import java.util.Arrays;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.scenario.CapabilityContext;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.ctx.capability.CapabilityManager;
import org.eclipse.rcptt.internal.core.model.ReferencedProjectScope;
import org.eclipse.rcptt.ui.utils.WriteAccessChecker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

public class ContextConvertHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);

		final IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);

		try {
			IFile element = (IFile) selection.getFirstElement();

			IPath resourcePath = element.getFullPath();

			final WriteAccessChecker writeAccessChecker = new WriteAccessChecker(shell);
			IQ7Project create = RcpttCore.create(element.getProject());
			IPath path = resourcePath.removeFirstSegments(1).removeLastSegments(1);
			final IQ7Folder folder = (path.segmentCount() >= 1) ? create.getFolder(path) : create.getFolders()[0];
			if (!writeAccessChecker.makeResourceWritable(folder)) {
				return null;
			}
			final IContext context = folder.getContext(element.getName());

			final NewCapabilityContextDialog dialog = new NewCapabilityContextDialog(shell, folder.getQ7Project());
			if (dialog.open() == Window.OK) {

				IContext capability = folder.createContext(dialog.getName(), ContextTypeManager.getInstance()
						.getTypeById("org.eclipse.rcptt.ctx.capability"), true,
						new NullProgressMonitor());

				IQ7NamedElement capabilityCopy = capability.getWorkingCopy(new NullProgressMonitor());
				IQ7NamedElement copy = context.getWorkingCopy(new NullProgressMonitor());

				try {
					String id1 = capabilityCopy.getID();
					String id2 = copy.getID();
					copy.setID(id1);
					capabilityCopy.setID(id2);

					final CapabilityContext capabilityCtx = (CapabilityContext) capabilityCopy.getNamedElement();

					CapabilityManager.getManager().addCapability(capabilityCtx, dialog.getCapabilities())
							.getContextReferences().add(id1);

					if (!writeAccessChecker.makeResourceWritable(capabilityCopy)) {
						return false;
					}
					if (!writeAccessChecker.makeResourceWritable(copy)) {
						return false;
					}

					capabilityCopy.commitWorkingCopy(true, new NullProgressMonitor());
					copy.commitWorkingCopy(true, new NullProgressMonitor());
				} finally {
					copy.discardWorkingCopy();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static class NewCapabilityContextDialog extends TitleAreaDialog {

		private final IQ7Project project;

		private String name;
		private String[] selected;

		public NewCapabilityContextDialog(Shell shell, IQ7Project project) {
			super(shell);
			this.project = project;
			setTitle("Create capability context.");
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);

			newShell.setText("Create capability context");
		}

		@Override
		protected Point getInitialSize() {
			return new Point(300, 400);
		}

		public String getName() {
			return name;
		}

		public String[] getCapabilities() {
			return selected;
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite control = (Composite) super.createDialogArea(parent);

			Composite composite = new Composite(control, SWT.NONE);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

			Label label = new Label(composite, SWT.LEFT);
			label.setText("Name:");
			final Text text = new Text(composite, SWT.BORDER);
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					name = text.getText();
					validate();
				}
			});

			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(false, false).applyTo(label);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(false, false).applyTo(text);

			final CheckboxTableViewer listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
			listViewer.setContentProvider(ArrayContentProvider.getInstance());
			listViewer.setLabelProvider(new LabelProvider());
			listViewer.setInput(CapabilityManager.getManager().getAllCapabilities());
			listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					final Object[] array = listViewer.getCheckedElements();
					selected = Arrays.copyOf(array, array.length, String[].class);
					validate();
				}
			});

			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1)
					.applyTo(listViewer.getControl());

			return control;
		}

		@Override
		protected Control createContents(Composite parent) {
			Control result = super.createContents(parent);
			validate();
			return result;
		}

		protected void validate() {
			boolean valid = doValidate();
			if (valid) {
				setErrorMessage(null);
				setMessage("Create capability context:");
			}
		}

		protected boolean doValidate() {
			if (name == null || !Path.EMPTY.isValidSegment(name)) {
				setErrorMessage("Context name must be specified");
				return false;
			}

			IQ7NamedElement[] elements = Q7SearchCore.findByName(name,
					new ReferencedProjectScope(project), new NullProgressMonitor());

			for (IQ7NamedElement iq7NamedElement : elements) {
				if (iq7NamedElement instanceof IContext) {
					setErrorMessage("Context with this name already exists.");
					return false;
				}
			}

			if (selected == null || selected.length == 0) {
				setErrorMessage("Context has to contain at least one capability value.");
				return false;
			}

			return true;
		}

	}

}
