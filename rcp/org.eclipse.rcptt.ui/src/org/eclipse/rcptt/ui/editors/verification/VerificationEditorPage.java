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
package org.eclipse.rcptt.ui.editors.verification;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.ide.IGotoMarker;

import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.VerificationTypeManager;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.ui.actions.VerificationSnapshotAction;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.editors.IQ7Editor;
import org.eclipse.rcptt.ui.editors.IQ7EditorActionsProvider;
import org.eclipse.rcptt.ui.editors.NamedElementEditorPage;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.rcptt.ui.utils.ImageManager;
import org.eclipse.rcptt.ui.verification.VerificationUIManager;
import org.eclipse.rcptt.ui.verification.VerificationViewer;

public class VerificationEditorPage extends NamedElementEditorPage implements IGotoMarker {

	private ImageManager imageManager = new ImageManager();
	private IQ7Editor<Verification> editor;

	public VerificationEditorPage(VerificationEditor editor) {
		super(editor, "verification", Messages.VerificationEditorPage_Name);
	}

	public Verification getElement() {
		return (Verification) super.getElement();
	}

	@Override
	protected EditorHeader createHeader() {
		return new EditorHeader(getModel(), getElement()) {

			protected Button createRecordButton(Composite composite, FormToolkit toolkit) {
				Button button = toolkit.createButton(composite,
						Messages.VerificationEditorPage_CaptureButtonText, SWT.PUSH);
				button.setImage(Images.getImage(Images.SNAPSHOT));
				button.setBackground(null);
				GridDataFactory.fillDefaults().applyTo(button);
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						record();
					}
				});

				VerificationType type = VerificationTypeManager.getInstance().getTypeByVerification(
						(Verification) getElement());
				VerificationViewer viewer = VerificationUIManager.getInstance().getViewer(type);
				if (viewer != null)
					button.setEnabled(viewer.getViewer().isCaptureSupported());

				return button;
			}

			protected Button createReplayButton(Composite composite, FormToolkit toolkit) {
				Button button = toolkit.createButton(composite,
						Messages.VerificationEditorPage_VerifyButtonText, SWT.PUSH);
				button.setImage(DebugUITools.getImage(
						IDebugUIConstants.IMG_ACT_RUN));
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
				VerificationType type = VerificationTypeManager.getInstance().getTypeByVerification(
						(Verification) getElement());
				VerificationViewer viewer = VerificationUIManager.getInstance().getViewer(type);
				if (viewer != null) {
					button.setEnabled(viewer.getViewer().isApplySupported());
				}
				return button;
			}

			@Override
			protected void record() {
				VerificationEditorPage.this.record();
			}

			@Override
			protected void replay() {
				VerificationEditorPage.this.replay();
			}
		};
	}

	@Override
	protected void createSpecificContent(Composite parent, FormToolkit toolkit,
			IManagedForm form) {
		VerificationType type = VerificationTypeManager.getInstance()
				.getTypeByVerification(getElement());
		VerificationViewer viewer =
				VerificationUIManager.getInstance().getViewer(type);
		if (viewer != null) {
			editor = viewer.getViewer().createEditor();
			editor.setElement(getModel());
			editor.setForm(form);
			editor.create(parent, toolkit, getSite(), getHeader());

			if (editor instanceof IQ7EditorActionsProvider) {
				((VerificationEditor) getEditor()).setVerificationActions(((IQ7EditorActionsProvider) editor)
						.createActions());
			}
			ImageDescriptor descriptor = viewer.getIcon();
			if (descriptor != null) {
				Image img = imageManager.getImage(descriptor);
				((VerificationEditor) getEditor()).setEditorImage(img);
			}
		}
	}

	@Override
	protected void record() {
		new VerificationSnapshotAction(null, editor, null).run();
	}

	@Override
	protected void replay() {
		IResource res = getModel().getResource();
		if (res == null)
			return;
		LaunchUtils.launchVerification(new IResource[] { res }, "run"); //$NON-NLS-1$
	}

	public void save() throws CoreException {
		editor.save();
	}

	public void select(Object data) {
		if (editor != null) {
			editor.select(data);
		}
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
		if (getEditor() instanceof VerificationEditor) {
			((VerificationEditor) getEditor()).setEditorImage(Images.getImage(Images.VERIFICATION));
		}
		if (imageManager != null) {
			imageManager.dispose();
			imageManager = null;
		}
	}

	@Override
	public void gotoMarker(IMarker marker) {
		IGotoMarker adapter = (IGotoMarker) Platform.getAdapterManager().getAdapter(editor, IGotoMarker.class);
		if (adapter != null)
			adapter.gotoMarker(marker);
	}
}
