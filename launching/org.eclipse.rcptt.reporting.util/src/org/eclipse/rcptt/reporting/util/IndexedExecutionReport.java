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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.streams.SherlockReportFormat;

import com.google.common.io.Closer;

/** A collection of reports from one execution session grouped in a ZIP file **/
public final class IndexedExecutionReport implements Closeable {
	private final Path zipPath;
	private final Map<String, Handle> idIndex = synchronizedMap(new HashMap<>());
	private final Map<String, Handle> entryIndex = synchronizedMap(new HashMap<>());

	private static final class ClosingInputStream extends FilterInputStream {
		protected ClosingInputStream(InputStream in) {
			super(in);
		}
		public Closer closer = Closer.create();
		@Override
		public void close() throws IOException {
			closer.register(super::close);
			closer.close();
		}
	}
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
			try (var is = readEntry()) {
				var result = populate(is);
				assert reportEntry != null;
				return result;
			}
		}
		private Report populate(InputStream is) throws IOException {
			Report report = SherlockReportFormat.loadReport(is, false, true);
			reportEntry = ReportEntry.create(report);
			idIndex.put(reportEntry.id, this);
			return report;
		}
		private InputStream readEntry() throws IOException {
			Closer closer = Closer.create();
			InputStream result;
			try { 
				ZipFile zipFile = closer.register(new ZipFile(zipPath.toFile()));
					result = zipFile.getInputStream(zipEntry);
			} catch (ZipException e) {
				String name = zipEntry.getName();
				ZipInputStream zipInputStream = closer.register(new ZipInputStream(Files.newInputStream(zipPath)));
				Stream<ZipEntry> entries = entries(zipInputStream);
				closer.register(entries::close);
				ZipEntry target = entries.filter(entry -> entry.getName().equals(name)).findAny().get();
				result = zipInputStream;
			} catch (Throwable e) {
				closer.close();
				throw e;
			}
			var closing = new ClosingInputStream(result);
			closing.closer.register(closer);
			return closing;
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
		zipPath = path;
	}
	private static final Stream<ZipEntry> entries(ZipInputStream zipInputStream) {
		var result = Stream.generate(() -> {
			try {
				return zipInputStream.getNextEntry();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		})
		.takeWhile(Objects::nonNull);
		result.onClose(() -> {
			try {
				zipInputStream.close();
			} catch (IOException e1) {
				throw new UncheckedIOException(e1);
			}
		});
		return result;
	}
	public Stream<Handle> read() {
		ZipInputStream zipInputStream;
		try {
			zipInputStream = new ZipInputStream(Files.newInputStream(zipPath));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return entries(zipInputStream).map(e -> {
			var result = entryIndex.computeIfAbsent(e.getName(), (ignored) -> new Handle(e));
			if (result.reportEntry == null) {
				try {
					result.populate(zipInputStream);
				} catch (IOException e1) {
					throw new UncheckedIOException(e1);
				}
			}
			return result;
		});
	}
	@Override
	public void close() throws IOException {
		idIndex.clear();
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
