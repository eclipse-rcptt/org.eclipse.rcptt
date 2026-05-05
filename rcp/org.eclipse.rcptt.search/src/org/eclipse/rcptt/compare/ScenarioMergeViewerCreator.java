/*******************************************************************************
 * Copyright (c) 2009, 2024 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.OutlineViewerCreator;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.rcptt.compare.ScenarioStructureCreator.ScenarioPart;

/**
 * Content merge viewer creator for RCPTT test case ({@code .test} /
 * {@code .scenario}) files.
 *
 * <p>In addition to showing the raw file diff (same as the default
 * {@link TextMergeViewer}), this viewer provides Outline view support: when the
 * user clicks a structure node (e.g. "Description" or "Script") in the Outline
 * view, the text viewer scrolls to the corresponding section in the raw file.
 * This is done without requiring the structure nodes to extend
 * {@code DocumentRangeNode} (which would break the structure-pane compare by
 * causing {@code TextMergeViewer} to use an incorrect 1-character document
 * range for field content display).</p>
 */
@SuppressWarnings("restriction")
public class ScenarioMergeViewerCreator implements IViewerCreator {

	@Override
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		return new ScenarioMergeViewer(parent, config);
	}

	/**
	 * A {@link TextMergeViewer} for {@code .test}/{@code .scenario} files that
	 * overrides the {@link OutlineViewerCreator} to provide Outline view navigation
	 * to the correct raw-file section when a {@link ScenarioPart} is selected.
	 */
	static class ScenarioMergeViewer extends TextMergeViewer {

		/**
		 * Source viewers captured in {@link #createSourceViewer} (call order:
		 * 1 = ancestor, 2 = left, 3 = right).
		 */
		private SourceViewer fLeftSV;
		private SourceViewer fRightSV;
		private int fSourceViewerCount = 0;

		private ScenarioOutlineViewerCreator fOutlineCreator;

		ScenarioMergeViewer(Composite parent, CompareConfiguration config) {
			super(parent, config);
		}

		/**
		 * Captures the left and right {@link SourceViewer} instances as they are
		 * created by the superclass. {@code TextMergeViewer} always creates three
		 * viewers in this order: ancestor (1), left (2), right (3).
		 */
		@Override
		protected SourceViewer createSourceViewer(Composite parent, int textOrientation) {
			SourceViewer sv = super.createSourceViewer(parent, textOrientation);
			fSourceViewerCount++;
			if (fSourceViewerCount == 2) {
				fLeftSV = sv;
			} else if (fSourceViewerCount == 3) {
				fRightSV = sv;
			}
			return sv;
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter == OutlineViewerCreator.class) {
				if (fOutlineCreator == null) {
					fOutlineCreator = new ScenarioOutlineViewerCreator();
				}
				return adapter.cast(fOutlineCreator);
			}
			return super.getAdapter(adapter);
		}

		/**
		 * Scrolls both the left and right text viewers so that the character at
		 * {@code rawOffset} in the raw scenario file content is visible. This is called
		 * when the user clicks a {@link ScenarioPart} node in the Outline view.
		 */
		void revealOffset(int rawOffset) {
			if (fLeftSV != null) {
				fLeftSV.revealRange(rawOffset, 1);
			}
			if (fRightSV != null) {
				fRightSV.revealRange(rawOffset, 1);
			}
		}

		/**
		 * {@link OutlineViewerCreator} that creates a structure viewer for the Outline
		 * view and, when the user selects a {@link ScenarioPart}, scrolls
		 * {@link ScenarioMergeViewer} to the corresponding section in the raw file.
		 */
		private class ScenarioOutlineViewerCreator extends OutlineViewerCreator
				implements ISelectionChangedListener {

			@Override
			public Viewer findStructureViewer(Viewer oldViewer, ICompareInput input,
					Composite parent, CompareConfiguration configuration) {
				if (input != getInput()) {
					return null;
				}
				Viewer v = CompareUI.findStructureViewer(oldViewer, input, parent, configuration);
				if (v != null) {
					v.addSelectionChangedListener(this);
					v.getControl().addDisposeListener(e -> v.removeSelectionChangedListener(this));
				}
				return v;
			}

			@Override
			public boolean hasViewerFor(Object input) {
				return input instanceof ICompareInput;
			}

			@Override
			public Object getInput() {
				return ScenarioMergeViewer.this.getInput();
			}

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				if (!(sel instanceof IStructuredSelection)) {
					return;
				}
				Object elem = ((IStructuredSelection) sel).getFirstElement();
				if (!(elem instanceof ICompareInput)) {
					return;
				}
				ICompareInput ci = (ICompareInput) elem;
				int offset = getRawOffset(ci.getLeft());
				if (offset < 0) {
					offset = getRawOffset(ci.getRight());
				}
				if (offset >= 0) {
					revealOffset(offset);
				}
			}

			private int getRawOffset(ITypedElement element) {
				if (element instanceof ScenarioPart) {
					return ((ScenarioPart) element).getRawOffset();
				}
				return -1;
			}
		}
	}
}
