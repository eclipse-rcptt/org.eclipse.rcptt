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
package org.eclipse.rcptt.ui.editors.context;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.ui.actions.ContextSnapshotAction;
import org.eclipse.rcptt.ui.context.ContextUIManager;
import org.eclipse.rcptt.ui.context.ContextViewer;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.editors.IQ7Editor;
import org.eclipse.rcptt.ui.editors.IQ7EditorActionsProvider;
import org.eclipse.rcptt.ui.editors.NamedElementEditorPage;
import org.eclipse.rcptt.ui.utils.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ContextEditorPage extends NamedElementEditorPage {

	private ImageManager imageManager = new ImageManager();
	private IQ7Editor<Context> editor;

	public void setSelectionAtLine(int line) {
		if (editor != null) {
			editor.setSelectionAtLine(line);
		}
	}

	public ContextEditorPage(ContextEditor editor) {
		super(editor, "context", Messages.ContextEditorPage_Name); //$NON-NLS-1$
	}

	public Context getElement() {
		return (Context) super.getElement();
	}

	@Override
	protected EditorHeader createHeader() {
		return new EditorHeader(getModel(), getElement()) {

			protected Button createRecordButton(Composite composite, FormToolkit toolkit) {
				Button button = toolkit.createButton(composite,
						Messages.ContextEditorPage_CaptureButtonText, SWT.PUSH);
				button.setImage(Images.getImage(Images.SNAPSHOT));
				button.setBackground(null);
				GridDataFactory.fillDefaults().applyTo(button);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						record();
					}
				});

				ContextType type = ContextTypeManager.getInstance().getTypeByContext(
						(Context) getElement());
				ContextViewer viewer = ContextUIManager.getInstance().getViewer(type);
				if (viewer != null) {
					button.setEnabled(viewer.getViewer().isCaptureSupported());
				}

				return button;
			}

			protected Button createReplayButton(Composite composite, FormToolkit toolkit) {
				Button button = toolkit.createButton(composite,
						Messages.ContextEditorPage_ApplyButtonText, SWT.PUSH);
				button.setImage(DebugUITools.getImage(IDebugUIConstants.IMG_ACT_RUN));
				button.setBackground(null);
				GridDataFactory.fillDefaults().applyTo(button);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						if ((e.stateMask & SWT.SHIFT) != 0)
							multiReplay();
						else
							replay();
					}
				});
				ContextType type = ContextTypeManager.getInstance().getTypeByContext(
						(Context) getElement());
				ContextViewer viewer = ContextUIManager.getInstance().getViewer(type);
				if (viewer != null) {
					button.setEnabled(viewer.getViewer().isApplySupported());
				}
				return button;
			}

			@Override
			protected void record() {
				ContextEditorPage.this.record();
			}
		};
	}

	@Override
	protected void createSpecificContent(Composite parent, FormToolkit toolkit,
			IManagedForm form) {
		ContextType type = ContextTypeManager.getInstance()
				.getTypeByContext(getElement());
		ContextViewer viewer = ContextUIManager.getInstance().getViewer(type);
		if (viewer == null)
			throw new RuntimeException("While opening " + getElement().getName() + ", failed to find viewer of "
					+ type.getId() + " context type.");
		if (viewer != null) {
			editor = viewer.getViewer().createEditor();
			editor.setElement(getModel());
			editor.setForm(form);
			editor.create(parent, toolkit, getSite(), getHeader());

			if (editor instanceof IQ7EditorActionsProvider) {
				((ContextEditor) getEditor()).setCtxActions(((IQ7EditorActionsProvider) editor).createActions());
			}
			ImageDescriptor descriptor = viewer.getIcon();
			if (descriptor != null) {
				Image img = imageManager.getImage(descriptor);
				((ContextEditor) getEditor()).setEditorImage(img);
			}
		}
	}

	@Override
	protected void record() {
		new ContextSnapshotAction(editor).run();
	}

	public void save() throws CoreException {
		editor.save();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (editor != null) {
			editor.dispose();
			editor = null;
		}
		// We set image to container editor from imageManager,
		// this image will be disposed after imageManager dispose,
		// reset editor image to default
		if (getEditor() instanceof ContextEditor) {
			((ContextEditor) getEditor()).setEditorImage(Images.getImage(Images.CONTEXT));
		}
		if (imageManager != null) {
			imageManager.dispose();
			imageManager = null;
		}

	}
}
