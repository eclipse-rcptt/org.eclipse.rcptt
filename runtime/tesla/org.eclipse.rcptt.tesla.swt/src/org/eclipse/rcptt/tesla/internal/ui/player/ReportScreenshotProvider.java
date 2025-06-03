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
package org.eclipse.rcptt.tesla.internal.ui.player;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.sherlock.core.INodeBuilder;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Screenshot;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ScreenshotKind;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ReportScreenshotProvider {
	private static SWTScreenCapturer screenCapturer;

	public static void takeScreenshot(final Display display,
			final boolean onError, final String messagePrefix) {
		if (screenCapturer == null) {
			screenCapturer = new SWTScreenCapturer();
		}
		final List<Shell> toProceed = new ArrayList<Shell>();

		display.syncExec(new Runnable() {
			public void run() {
				Shell[] shells = display.getShells();
				for (Shell shell : shells) {
					if (shell.isDisposed() || !shell.isVisible()) {
						continue;
					}
					toProceed.add(shell);
				}
			}
		});

		for (final Shell s : toProceed) {
			display.syncExec(new Runnable() {
				public void run() {
					if (!s.isDisposed() && s.isVisible()) {
						if (s.getMinimized()) {
							s.setMinimized(false);
						}
						s.setActive();
						try {
							ShellUtilsProvider.getShellUtils().forceActive(s);
						} catch (CoreException e) {
							throw new RuntimeException(e);
						}
					}
				}
			});
			display.syncExec(new Runnable() {
				public void run() {
					if (!s.isDisposed() && s.isVisible()) {
						makeScreenshot(display, s, messagePrefix, onError);
					}
				}
			});
		}
	}

	public static void makeScreenshot(Display display, Shell shell,
			String message, boolean onError) {
		if (shell.isDisposed()) {
			return;
		}
		final Rectangle bounds = display.getBounds();
		String finalMessage = message;
		String postFix = PlayerTextUtils.replaceNonUnicode(shell.getText());
		if (postFix != null) {
			finalMessage = message + "(" + postFix + ")";
		}

		// }
		byte[] shotData = screenCapturer.makeScreenShotData(shell, !onError);
		if (shotData != null) {
			INodeBuilder node = ReportManager.getCurrentReportNode();
			// Clean out previous screenshots with same name.
			// cleanScreenshots(report.getRoot(), finalMessage);

			Screenshot img = ReportFactory.eINSTANCE.createScreenshot();
			img.setKind(ScreenshotKind.PNG);
			img.setData(shotData);
			img.setMessage(finalMessage);
			// TODO: cleanup shots with the same name
			ReportHelper.addSnapshotWithData(node, img);
		}
	}
}
