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
package org.eclipse.rcptt.core.ecl.context.internal.viewer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.ecl.context.EclContext;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.context.BaseContextEditor;
import org.eclipse.rcptt.ui.controls.SectionWithComposite;
import org.eclipse.rcptt.ui.editors.EditorHeader;
import org.eclipse.rcptt.ui.editors.ecl.EclSourceViewer;
import org.eclipse.rcptt.ui.editors.ecl.EclSourceViewerConfiguration;

public class EclContextEditor extends BaseContextEditor {

	private static final String EMPTY_SCRIPT = "";
	private EclSourceViewer viewer;

	public EclContext getContextElement() {
		try {
			return (EclContext) super.getElement().getNamedElement();
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
			return null;
		}
	}

	@Override
	public Control create(Composite parent, FormToolkit toolkit,
			IWorkbenchSite site, EditorHeader header) {
		Section section = new SectionWithComposite("Script", Section.TITLE_BAR
				| Section.TWISTIE | Section.EXPANDED).create(parent, toolkit);
		Composite composite = (Composite) section.getClient();
		CompositeRuler ruler = new CompositeRuler();
		LineNumberRulerColumn column = new LineNumberRulerColumn() {
			@Override
			public int getWidth() {
				return super.getWidth() + 10;
			}
		};
		Font font = JFaceResources.getTextFont();
		column.setFont(font);
		column.setForeground(parent.getDisplay().getSystemColor(
				SWT.COLOR_DARK_GRAY));
		ruler.addDecorator(0, column);
		viewer = new EclSourceViewer(composite, ruler, null, false, SWT.MULTI
				| SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setEditable(true);
		viewer.setDocument(new Document(getScript()));
		viewer.addTextListener(new ITextListener() {
			@Override
			public void textChanged(TextEvent event) {
				if (!event.getText().equals(event.getReplacedText())) {
					setScript(viewer.getDocument().get());
				}
			}
		});

		toolkit.adapt(viewer.getControl());
		viewer.configure(new EclSourceViewerConfiguration());
		viewer.getTextWidget().setFont(font);
		GridDataFactory.fillDefaults().grab(true, true)
				.applyTo(viewer.getControl());
		return section;
	}

	private String getScript() {
		Script script = getContextElement().getScript();
		if (script == null)
			return EMPTY_SCRIPT;
		String text = script.getContent();
		if (text != null)
			return text;
		return EMPTY_SCRIPT;
	}

	private void setScript(String content) {
		if (getContextElement().getScript() == null)
			getContextElement().setScript(CoreFactory.eINSTANCE.createScript());
		getContextElement().getScript().setContent(content);
	}
}
