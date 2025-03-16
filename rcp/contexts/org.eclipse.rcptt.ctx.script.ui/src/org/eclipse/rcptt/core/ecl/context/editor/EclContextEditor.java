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
package org.eclipse.rcptt.core.ecl.context.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.rcptt.core.ecl.context.ContextPackage;
import org.eclipse.rcptt.core.ecl.context.EclContext;
import org.eclipse.rcptt.core.ecl.context.internal.viewer.EclContextViewer;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.editors.EditorContent;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.editors.ecl.EclDocumentProvider;
import org.eclipse.rcptt.ui.editors.ecl.EclEditor;
import org.eclipse.rcptt.ui.editors.ecl.EclEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class EclContextEditor extends EclEditor {
	private final List<Runnable> onDispose = new ArrayList<>();
	public EclContextEditor() {
		onDispose.add(0, super::dispose);
		Image image = EclContextViewer.getEclContextImage().createImage();
		setTitleImage(image);
		onDispose.add(0, image::dispose);
	}
	
	@Override
	public void dispose() {
		RuntimeException result = null;
		for (Runnable runnable: onDispose) {
			try {
				runnable.run();
			} catch (RuntimeException e) {
				if (result == null) {
					result = e;
				} else {
					result.addSuppressed(e);
				}
			}
		}
		if (result != null) {
			throw result;
		}
	}

	@Override
	protected String getEclContext(NamedElement element) {
		if (element instanceof EclContext) {
			Script script = ((EclContext) element).getScript();
			if (script != null) {
				return script.getContent();
			}
		}
		return "";
	}

	@Override
	protected void setEclContext(NamedElement element, String value) {
		if (element instanceof EclContext) {
			Script sc = CoreFactory.eINSTANCE.createScript();
			sc.setContent(value);
			((EclContext) element).setScript(sc);
		}
	}

	@Override
	protected void checkReferences() {
		// do nothing
	}

	@Override
	protected EditorHeader createEditorHeader() {
		return new EditorHeader(getModel(), getElement()) {

			@Override
			protected Button createRecordButton(Composite composite,
					FormToolkit toolkit) {
				Button button = toolkit.createButton(composite,
						Messages.ContextEditorPage_CaptureButtonText, SWT.PUSH);
				button.setImage(Images.getImage(Images.SNAPSHOT));
				button.setBackground(null);
				GridDataFactory.fillDefaults().applyTo(button);
				button.setEnabled(false);
				return button;
			}

			@Override
			protected Button createReplayButton(Composite composite,
					FormToolkit toolkit) {
				Button button = toolkit.createButton(composite,
						Messages.ContextEditorPage_ApplyButtonText, SWT.PUSH);
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
				return button;
			}

			@Override
			protected void record() {
				// TODO Auto-generated method stub

			}
		};
	}

	@Override
	protected EditorContent createEditorContent() {
		return new EditorContent(header, false, true);
	}

	@Override
	protected EclDocumentProvider createDocumentProvider() {
		return new EclDocumentProvider() {
			@Override
			protected String getScriptContent(EclEditorInput element)
					throws ModelException {
				if (element.getElement() instanceof IContext) {
					IContext eclContext = (IContext) element.getElement();
					return getEclContext(eclContext.getNamedElement());
				}
				return "";
			}
		};
	}

	@Override
	protected void bindScriptContent() {
		super.bindScriptContent();
		IChangeListener scenarioContentListener = new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				String script;
				try {
					script = getEclContext(getElement());
				} catch (Exception e) {
					script = "Ecl context loading failed. See error log for details.";
					Q7UIPlugin.log(e);
				}
				// in some cases after hit upon here from document listener
				// viewer.getDocument() is null
				if (viewer.getDocument() != null) {
					String doc = viewer.getDocument().get();
					if (!doc.equals(script)) {
						viewer.getDocument().set(script);
					}
				}
			}
		};
		@SuppressWarnings("unchecked")
		IObservableValue<Script> scriptContent = EMFObservables.observeValue(getElement(),
				ContextPackage.Literals.ECL_CONTEXT__SCRIPT);
		scriptContent.addChangeListener(scenarioContentListener);
	}
	
	
}
