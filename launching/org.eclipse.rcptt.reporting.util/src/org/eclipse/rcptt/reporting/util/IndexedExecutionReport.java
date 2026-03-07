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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.streams.SherlockReportFormat;

/** A collection of reports from one execution session grouped in a ZIP file
 * 
 * Lazy loading is implemented to prevent OOM on large collections.
 * Supports reading of incomplete archive when execution is still in progress or was aborted.
 * 
 * **/
public final class IndexedExecutionReport implements Closeable {
	private final Path zipPath;
	private final Map<String, Handle> idIndex = synchronizedMap(new HashMap<>());
	private final Map<String, Handle> entryIndex = synchronizedMap(new HashMap<>());
	private final AtomicReference<ZipFile> zipFile = new AtomicReference<>(null);

	public final class Handle {
		private final ZipEntry zipEntry;
		private ReportEntry reportEntry;
		private SoftReference<Report> cachedReport = new SoftReference<Report>(null);
		private Handle(ZipEntry entry) {
			this.zipEntry = requireNonNull(entry);
		}
		public String getId() throws IOException {
			return getEntry().id;
		}
		public Report getReport() throws IOException {
			var result = this.cachedReport.get();
			if (result != null) {
				return result;
			}
			try (var is = readEntry()) {
				result = populate(is);
				assert reportEntry != null;
				return result;
			}
		}
		private Report populate(InputStream is) throws IOException {
			Report report = cachedReport.get();
			if (report != null) {
				return report;
			}
			report = SherlockReportFormat.loadReport(is, false, true);
			this.cachedReport = new SoftReference<Report>(report);
			reportEntry = ReportEntry.create(report);
			idIndex.put(reportEntry.id, this);
			return report;
		}
		private InputStream readEntry() throws IOException {
			InputStream result;
			try {
				result = openZipFile().map(f -> {
					try {
						return f.getInputStream(zipEntry);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}).orElseGet(() -> {
					try {
						String name = zipEntry.getName();
						ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipPath));
						try {
							for (var entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
								if (entry.getName().equals(name)) {
									return zipInputStream;
								}
							}
							zipInputStream.close();
							throw new FileNotFoundException(zipPath + ":" + name);
						} catch (Throwable e) {
							zipInputStream.close();
							throw e;
						}
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (UncheckedIOException e) {
				throw e.getCause();
			}
			return result;
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

	public Stream<Handle> read() {
		try {
			Stream<Handle> resultStream = openZipFile().map(this::streamZipFile).orElseGet(this::streamZipInputStream);
			return resultStream.takeWhile(Objects::nonNull);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private Stream<Handle> streamZipFile(ZipFile file) {
		return file.stream().map(entry -> {
			var result = entryIndex.computeIfAbsent(entry.getName(), (ignored) -> new Handle(entry));
			if (result.reportEntry == null) {
				try (var is =  file.getInputStream(entry)) {
					result.populate(is);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
			return result;
		});
	}
	private Stream<Handle> streamZipInputStream() {
		ZipInputStream zipInputStream;
		try {
			zipInputStream = new ZipInputStream(Files.newInputStream(zipPath));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		Stream<Handle> resultStream = Stream.generate(() -> {
			try {
				synchronized(zipInputStream) {
					ZipEntry entry = zipInputStream.getNextEntry();
					if (entry == null) {
						return null;
					}
					var result = entryIndex.computeIfAbsent(entry.getName(), (ignored) -> new Handle(entry));
					if (result.reportEntry == null) {
						result.populate(zipInputStream);
					}
					return result;
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e); 
			}
		});
		resultStream.onClose(() -> {
			try {
				zipInputStream.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		return resultStream;
	}
	@Override
	public void close() throws IOException {
		idIndex.clear();
		entryIndex.clear();
		try (var close = zipFile.get()) {
			// closes if not null
		}
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
	
	@SuppressWarnings("resource")
	private Optional<ZipFile> openZipFile() throws IOException {
		try {
			ZipFile f = zipFile.get();
			if (f == null) {
				f = new ZipFile(zipPath.toFile());
				if (!zipFile.compareAndSet(null, f)) {
					f.close();
					f = zipFile.get();
					assert f != null;
				}
			}
			return Optional.of(f);
		} catch (ZipException e) {
			return Optional.empty();
		}
	}
}
