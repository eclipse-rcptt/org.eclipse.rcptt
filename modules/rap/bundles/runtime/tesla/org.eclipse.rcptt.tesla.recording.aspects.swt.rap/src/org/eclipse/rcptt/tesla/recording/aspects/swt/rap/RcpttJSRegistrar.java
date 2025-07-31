/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.tesla.recording.aspects.swt.rap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rap.rwt.internal.service.StartupPage;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.SWT;

@SuppressWarnings("restriction")
public class RcpttJSRegistrar {

	private static final String[] CLIENT_JS_FILES = new String[] { "rcptt/RCPTT.js", "rcptt/Assertion.js" }; //$NON-NLS-1$

	public static void registrate(StartupPage page, ResourceManager manager) throws IOException {
		final ContentBuffer content = new ContentBuffer();

		for (String file : CLIENT_JS_FILES) {
			content.append(file);
		}

		registerJavascriptResource(page, manager, content, SWT.getVersion() + "/rcptt-client.js");
	}

	private static void registerJavascriptResource(StartupPage page, ResourceManager manager, ContentBuffer buffer,
			String name) throws IOException {
		InputStream inputStream = buffer.getContentAsStream();
		try {
			manager.register(name, inputStream);
		} finally {
			inputStream.close();
		}

		final String location = manager.getLocation(name);
		page.addJsLibrary(location);

	}

	private static class ContentBuffer {

		private final ByteArrayOutputStream bufferOutputStream;

		public ContentBuffer() {
			bufferOutputStream = new ByteArrayOutputStream();
		}

		public void append(String location) throws IOException {
			InputStream inputStream = openResourceStream(location);
			if (inputStream == null) {
				throw new IOException("Failed to load resource: " + location);
			}
			try {
				append(inputStream);
			} finally {
				inputStream.close();
			}
		}

		private void append(InputStream inputStream) throws IOException {
			byte[] buffer = new byte[40960];
			boolean contentWritten = false;
			int read = inputStream.read(buffer);
			while (read != -1) {
				bufferOutputStream.write(buffer, 0, read);
				read = inputStream.read(buffer);
				contentWritten = true;
			}
			if (contentWritten) {
				bufferOutputStream.write('\n');
			}
		}

		public InputStream getContentAsStream() {
			return new ByteArrayInputStream(bufferOutputStream.toByteArray());
		}

		private InputStream openResourceStream(String name) {
			return getClass().getClassLoader().getResourceAsStream(name);
		}
	}
}
