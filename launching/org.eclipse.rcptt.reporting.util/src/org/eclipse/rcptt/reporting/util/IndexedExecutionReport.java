/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.reporting.util;

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.streams.SherlockReportFormat;

/** A collection of reports from one execution session grouped in a ZIP file **/
public final class IndexedExecutionReport implements Closeable {
	private final ZipFile zipFile;
	private final Map<String, Handle> idIndex = synchronizedMap(new HashMap<>());
	private final Map<String, Handle> entryIndex = synchronizedMap(new HashMap<>());
	public final class Handle {
		private final ZipEntry zipEntry;
		private ReportEntry reportEntry;
		private Handle(ZipEntry entry) {
			this.zipEntry = requireNonNull(entry);
		}
		public String getId() throws IOException {
			return getEntry().id;
		}
		public Report getReport() throws IOException {
			try (InputStream is = zipFile.getInputStream(zipEntry)) {
				Report report = SherlockReportFormat.loadReport(is, true, true);
				reportEntry = ReportEntry.create(report);
				idIndex.put(reportEntry.id, this);
				return report; 
			}
		}
		public ReportEntry getEntry() throws IOException {
			if (reportEntry == null) {
				getReport();
			}
			assert reportEntry != null;
			return reportEntry;
		}
	}
	public IndexedExecutionReport(Path path) throws ZipException, IOException {
		zipFile = new ZipFile(path.toFile());
	}
	public Stream<Handle> read() {
		if (zipFile.size() == entryIndex.size()) {
			return entryIndex.values().stream();
		}
		return zipFile.stream().map(e -> entryIndex.computeIfAbsent(e.getName(), (ignored) -> new Handle(e)) );
	}
	@Override
	public void close() throws IOException {
		idIndex.clear();
		zipFile.close();
	}
	public Handle getById(String id) {
		Handle result = idIndex.get(id);
		if (result != null) {
			return result;
		}
		try (Stream<Handle> s = read()) {
			return s.filter(h -> {
				try {
					return id.equals(h.getId());
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}).findAny().get();
		}
	}
}
