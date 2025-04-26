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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.rcptt.core.ecl.model.Q7ElementContainer;
import org.eclipse.rcptt.core.ecl.parser.ast.Node;
import org.eclipse.rcptt.core.ecl.parser.model.Finder;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class MarkAllOccurencesJob extends UIJob {

	private ITextSelection selection;
	private boolean isCanceled = false;
	private final EclEditor eclEditor;

	public MarkAllOccurencesJob(EclEditor eclEditor) {
		super("ECL_EDITOR_MARK_ALL_OCCURENCES_JOB");
		this.eclEditor = eclEditor;
	}

	void requestMark(ITextSelection selection) {
		cancel();
		this.selection = selection;
		if (selection != null) {
			schedule();
		}
		
	}
	void doCancel() {
		isCanceled = true;
		cancel();
	}

	private boolean isCanceled(IProgressMonitor progressMonitor) {
		return isCanceled || progressMonitor.isCanceled();
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor progressMonitor) {
		if (isCanceled(progressMonitor))
			return Status.CANCEL_STATUS;

		ITextViewer textViewer = eclEditor.getViewer();
		if (textViewer == null)
			return Status.CANCEL_STATUS;

		IDocument document = textViewer.getDocument();
		if (document == null)
			return Status.CANCEL_STATUS;

		IDocumentProvider documentProvider = eclEditor.getDocumentProvider();
		if (documentProvider == null)
			return Status.CANCEL_STATUS;

		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(eclEditor.getEditorInput());
		if (annotationModel == null)
			return Status.CANCEL_STATUS;

		int offset = selection.getOffset();
		Q7ElementContainer container = ((EclSourceViewer)textViewer).getElementContainer();
		container.includeBuildins(true);
		container.setOffset(offset);
		Finder finder = new Finder(ECLEditorPlugin.getMarkSettings());

		// Add occurrence annotations
		Node[] locations = finder.find(container.script, container.offset, container);
		int length = locations.length;
		Map<Annotation, Position> annotationMap = new HashMap<Annotation, Position>(length);
		for (int i = 0; i < length; i++) {

			if (isCanceled(progressMonitor))
				return Status.CANCEL_STATUS;

			Position position = new Position(locations[i].begin, locations[i].length());

			annotationMap.put(new Annotation("org.eclipse.rcptt.ui.occurrences", false, ""), position);
		}

		if (isCanceled(progressMonitor))
			return Status.CANCEL_STATUS;

		synchronized (eclEditor.getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				eclEditor.replaceOccurencesAnnotation(annotationModel, annotationMap);
			} else {
				eclEditor.removeOccurrenceAnnotations();
				Iterator<Entry<Annotation, Position>> iter = annotationMap.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Annotation, Position> mapEntry = iter.next();
					annotationModel.addAnnotation(mapEntry.getKey(), mapEntry.getValue());
				}
			}
			eclEditor.setOccurencesAnnotation(annotationMap.keySet().toArray(
					new Annotation[annotationMap.keySet().size()]));
		}

		return Status.OK_STATUS;
	}
}
