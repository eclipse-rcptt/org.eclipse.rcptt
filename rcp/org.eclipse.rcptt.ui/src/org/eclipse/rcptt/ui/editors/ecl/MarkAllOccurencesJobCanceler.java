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
package org.eclipse.rcptt.ui.editors.ecl;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class MarkAllOccurencesJobCanceler implements IDocumentListener, ITextInputListener {
	private EclEditor editor;
	
	public void install(EclEditor editor) {
		this.editor = editor;
		ISourceViewer sourceViewer= editor.getViewer();
		if (sourceViewer == null)
			return;

		StyledText text= sourceViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		sourceViewer.addTextInputListener(this);

		IDocument document= sourceViewer.getDocument();
		if (document != null)
			document.addDocumentListener(this);
	}

	public void uninstall(EclEditor editor) {
		ISourceViewer sourceViewer= editor.getViewer();
		if (sourceViewer != null)
			sourceViewer.removeTextInputListener(this);

		IDocumentProvider documentProvider= editor.getDocumentProvider();
		if (documentProvider != null) {
			IDocument document= documentProvider.getDocument(editor.getEditorInput());
			if (document != null)
				document.removeDocumentListener(this);
		}
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
		editor.getMarkAllOccurencesJob().cancel();
	}

	public void documentChanged(DocumentEvent event) {
	}

	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (oldInput == null)
			return;

		oldInput.removeDocumentListener(this);
	}

	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput == null)
			return;
		newInput.addDocumentListener(this);
	}
}