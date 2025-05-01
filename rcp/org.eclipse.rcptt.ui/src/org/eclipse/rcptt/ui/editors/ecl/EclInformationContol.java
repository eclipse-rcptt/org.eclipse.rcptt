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

import java.io.IOException;
import java.net.URL;

import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;

@SuppressWarnings("restriction")
public class EclInformationContol extends BrowserInformationControl {

	public EclInformationContol(Shell parent, boolean resizeable) {
		super(parent, JFaceResources.DIALOG_FONT, resizeable);
	}

	public static boolean isAvailable(Shell parent) {
		if (parent == null || parent.isDisposed()) {
			return false;
		}
		return BrowserInformationControl.isAvailable(parent);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setInformation(final String content) {
		super.setInformation(applyStyling(content));
	}

	public static String applyStyling(String html) {
		String styles = loadStyles();
		FontData fontData = JFaceResources.getDialogFont().getFontData()[0];
		styles = HTMLPrinter.convertTopLevelFont(styles, fontData);

		StringBuffer b = new StringBuffer(html);
		b.insert(0, "</style></head><body>");
		b.insert(0, styles);
		b.insert(0, "<html><head><style type=\"text/css\">");
		b.append("</body></html>");

		return b.toString();
	}

	private static String styles = null;

	private static String loadStyles() {
		if (styles != null)
			return styles;

		try {
			URL url = Q7UIPlugin.getDefault().getBundle()
					.getResource("/docs.css");
			// Check for missing styles resource.
			if (url == null) {
				return styles = "";
			}
			return styles = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			Q7UIPlugin.log(e);
			return styles = "";
		}
	}
}
