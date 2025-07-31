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
package org.eclipse.rcptt.launching.ext.tests;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

/** Downloads and persists files across runs **/
public final class DownloadCache implements Closeable {
	private final HttpClient client = HttpClient.newHttpClient();
	public static final Path DEFAULT_CACHE_ROOT = Path.of(System.getProperty("java.io.tmpdir", "/tmp"))
			.resolve(DownloadCache.class.getName());
	private final Path cacheRoot;
	private final Set<Path> verifiedFiles = Collections.synchronizedSet(new HashSet<>());

	public DownloadCache(Path cacheRoot) {
		super();
		this.cacheRoot = cacheRoot;
		try {
			Files.createDirectories(cacheRoot);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public record Request(URI uri, String sha512) {
	}

	/**
	 * @return a local file to read. Name matches the original URI, path does not. The file should not be modified by
	 *         clients.
	 **/
	public Path download(Request request) throws IOException, InterruptedException {
		Path target = cacheRoot.resolve(request.sha512).resolve(filename(request.uri.getPath()));
		Path tmp = target.getParent().resolve("tmp");
		if (Files.isRegularFile(target)) {
			if (!verifiedFiles.contains(target)) {
				String actualHash = computeSHA512(target);
				if (!actualHash.equals(request.sha512())) {
					throw new IOException("Cached " + request.uri + " has SHA512 hash " + actualHash + " but "
							+ request.sha512() + " is expected");
				}
				verifiedFiles.add(target);
			}
			return target;
		}
		if (Files.exists(tmp)) {
			throw new IllegalStateException("Concurrent access to " + tmp + " while downloading " + request.uri
					+ ". Concurrent downloads are not supported yet.");
		}
		HttpRequest httpsRequest = HttpRequest.newBuilder()
				.uri(request.uri())
				.GET()
				.build();
		Files.createDirectories(target.getParent());
		try {
			HttpResponse<Path> response = client.send(httpsRequest, HttpResponse.BodyHandlers.ofFile(tmp));

			if (response.statusCode() != 200) {
				throw new IOException("Failed to download file. HTTP status code: " + response.statusCode());
			}
			String actualHash = computeSHA512(tmp);
			if (!actualHash.equals(request.sha512())) {
				throw new IOException("Downloaded " + request.uri + " has SHA512 hash " + actualHash + " but "
						+ request.sha512() + " is expected");
			}

			Files.move(tmp, target);
			verifiedFiles.add(target);
			return target;
		} catch (Throwable e) {
			Files.deleteIfExists(tmp);
			Files.deleteIfExists(target);
			Files.delete(target.getParent());
			throw e;
		}
	}

	private static String computeSHA512(Path input) throws IOException {
		try (InputStream fis = Files.newInputStream(input)) {
			MessageDigest sha512 = MessageDigest.getInstance("SHA-512");

			// Use DigestInputStream for automatic digest updating
			try (DigestInputStream dis = new DigestInputStream(fis, sha512);
					OutputStream nos = OutputStream.nullOutputStream()) {
				// Digest updated automatically by DigestInputStream
				dis.transferTo(nos);
			}

			return HexFormat.of().formatHex(sha512.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}

	private String filename(String uripath) {
		int pos = uripath.lastIndexOf("/");
		if (pos < 0) {
			return uripath;
		}
		return uripath.substring(pos + 1);
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

}
