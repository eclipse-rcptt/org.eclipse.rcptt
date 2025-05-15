/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *  
 * Contributors:
 * 	Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.reporting.html.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.reporting.core.IReportRenderer.IContentFactory;

public class VolatileContentFactory implements IContentFactory {
	private final Map<String, byte[]> data;
	private final String prefix;

	private VolatileContentFactory(Map<String, byte[]> data, String prefix) {
		super();
		this.data = data;
		this.prefix = prefix;
	}

	public VolatileContentFactory() {
		this(new HashMap<String, byte[]>(), "");
	}

	@Override
	public IContentFactory createFolder(String name) {
		return new VolatileContentFactory(data, createKey(name));
	}

	private String createKey(String name) {
		return prefix + "/" + name;
	}

	@Override
	public List<String> matchFiles(String fname) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isFileExist(String fileName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream createFileStream(String fname) throws CoreException {
		final String key = createKey(fname);
		if (data.get(key) != null)
			throw new CoreException(RcpttPlugin.createStatus("Second write to " + key));
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				if (data.put(key, toByteArray()) != null) {
					throw new IOException("Second write to " + key);
				}
			}
		};
	}

	@Override
	public void removeFileOrFolder(String name) throws CoreException {
		data.remove(createKey(name));
	}

	public String read(String path) {
		return new String(data.get(createKey(path)), StandardCharsets.UTF_8);
	}
}
