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
package org.eclipse.rcptt.verifications.text.impl;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.VerificationProcessor;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.ui.StyleRangeEntry;
import org.eclipse.rcptt.tesla.core.ui.TextPosition;
import org.eclipse.rcptt.tesla.ecl.impl.TeslaBridge;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerTextUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.recording.aspects.jface.text.JFaceTextEventManager;
import org.eclipse.rcptt.verifications.runtime.ErrorList;
import org.eclipse.rcptt.verifications.text.TextFactory;
import org.eclipse.rcptt.verifications.text.TextVerification;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Widget;

public class TextVerificationProcessor extends VerificationProcessor {

	@Override
	public void finish(Verification verification, Object data, IProcess process) throws CoreException {
		final TextVerification textVerification = EcoreUtil.copy((TextVerification)	verification);
		final SWTUIElement swtuiElement =
				TeslaBridge.resolveSWTUIElement(textVerification.getSelector(), process);
		final Widget widget = swtuiElement.widget;

		final ErrorList errors = new ErrorList();
		
		boolean strictLineEndings = Q7Features.getInstance().isTrue(Q7Features.VERIFY_LINE_ENDINGS);

		widget.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				String expectedText = textVerification.getText();
				if (expectedText == null)
					expectedText = "";

				if (widget instanceof StyledText)
					unfold((StyledText) widget);
				String actualText = PlayerTextUtils.getTextForVerification(swtuiElement);
				if (actualText == null)
					actualText = "";
				if (!strictLineEndings) {
					actualText = normalizeLineEndings(actualText);
					expectedText = normalizeLineEndings(expectedText);
				}
				if (!actualText.equals(expectedText)) {
					errors.add("Expected text is \"%s\", but it was \"%s\".",
							expectedText, actualText);
				}

				List<StyleRangeEntry> expectedStyleEntries = textVerification.getStyles();
				PlayerTextUtils.squashRanges(expectedStyleEntries);
				
				if (expectedStyleEntries.size() > 0 && !textVerification.isIgnoreStyling()) {
					if (!(widget instanceof StyledText))
						errors.add("Expected StyledText widget, got %s.",
								widget.getClass().getName());
					else {
						StyledText styledText = (StyledText) widget;
						List<StyleRangeEntry> actualStyleEntries = PlayerTextUtils.captureStyleRanges(styledText);
						PlayerTextUtils.squashRanges(actualStyleEntries);

						boolean failed = false;
						for (int i = 0; i < Math.min(expectedStyleEntries.size(), actualStyleEntries.size()); ++i) {
							StyleRangeEntry expected = expectedStyleEntries.get(i);
							StyleRangeEntry actual = actualStyleEntries.get(i);

							if (!EcoreUtil.equals(expected, actual)) {
								errors.add(
										"Difference in text styling found at %s.",
										posToString(earliestPos(expected.getStartPos(), actual.getStartPos())));
								failed = true;
								break;
							}
						}

						if (!failed && expectedStyleEntries.size() != actualStyleEntries.size()) {
							if (actualStyleEntries.size() > expectedStyleEntries.size())
								// actual text contains more style ranges
								errors.add("Difference in text styling found at %s.",
										posToString(actualStyleEntries.get(expectedStyleEntries.size()).getStartPos()));
							else
								// expected text contains more style ranges
								errors.add(
										"Difference in text styling found at %s.",
										posToString(actualStyleEntries.get(actualStyleEntries.size() - 1).getStartPos()));

						}
					}
				}
			}
		});

		errors.throwIfAny(String.format("Widget text verification '%s' failed:", verification.getName()),
				this.getClass().getPackage().getName(), verification.getId());
	}
	
	private static final String normalizeLineEndings(String input) {
		return input.replace("\r\n", "\n").replace("\r", "\n");
	}

	@Override
	public Verification create(EObject param, IProcess process) throws CoreException {
		final TextVerification p = (TextVerification) param;
		final SWTUIElement swtuiElement =
				TeslaBridge.resolveSWTUIElement(p.getSelector(), process);
		final Widget widget = swtuiElement.widget;

		final TextVerification result = TextFactory.eINSTANCE.createTextVerification();

		widget.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				result.setSelector(p.getSelector());
				result.setText(PlayerTextUtils.getTextForVerification(swtuiElement));

				if (widget instanceof StyledText) {
					StyledText styledText = (StyledText) widget;
					if (unfold(styledText))
						result.setText(PlayerTextUtils.getTextForVerification(swtuiElement));
					List<StyleRangeEntry> captured = PlayerTextUtils.captureStyleRanges(styledText);
					PlayerTextUtils.squashRanges(captured);
					result.getStyles().addAll(captured);
					result.setIgnoreStyling(result.getStyles().size() > 0 ? p.isIgnoreStyling() : true);
				} else
					result.setIgnoreStyling(true);
			}
		});

		return result;
	}

	private static boolean unfold(StyledText styledText) {
		TextViewer viewer = JFaceTextEventManager.getViewer(styledText);
		if (viewer != null && viewer instanceof SourceViewer) {
			viewer.doOperation(ProjectionViewer.EXPAND_ALL);
			AbstractReconciler reconciler =
					TeslaSWTAccess.getField(AbstractReconciler.class,
							viewer, "fReconciler");
			if (reconciler != null) {
				TeslaSWTAccess.call(reconciler, "forceReconciling");

				Object thread = TeslaSWTAccess.getField(Object.class,
						reconciler, "fThread");
				if (thread != null)
					TeslaSWTAccess.call(thread, "suspendCallerWhileDirty");
			}

			return true;
		}

		return false;
	}

	private static TextPosition earliestPos(TextPosition a, TextPosition b) {
		if (a.getLine() < b.getLine())
			return a;
		else if (a.getLine() > b.getLine())
			return b;
		else if (a.getColumn() < b.getColumn())
			return a;
		else
			return b;
	}

	private static String posToString(TextPosition p) {
		return String.format("line %s column %s", p.getLine(), p.getColumn());
	}
}
