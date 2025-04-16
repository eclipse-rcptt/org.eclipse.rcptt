/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.Platform;

public class FileUtil {
	public static void deleteFile(File file, boolean deleteRootDir) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childs = file.listFiles();
			if (childs != null) {
				for (File child : childs) {
					deleteFile(child, true);
				}
			}
			if (deleteRootDir) {
				file.delete();
			}
		}
	}

	/** Reads all data from the stream and closes it. */
	public static byte[] getStreamContent(InputStream stream)
			throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[4096];
			int len = 0;
			while ((len = stream.read(buffer)) > 0) {
				output.write(buffer, 0, len);
			}
		} finally {
			safeClose(stream);
		}
		return output.toByteArray();
	}

	public static byte[] getStreamContent(InputStream stream, int bufferSize)
			throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[bufferSize];
			int len = stream.read(buffer, 0, bufferSize);
			if (len > 0) {
				output.write(buffer, 0, len);
			}
		} finally {
			safeClose(stream);
		}
		return output.toByteArray();
	}

	public static void safeClose(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (Exception e) {
		}
	}

	public static void copy(InputStream source, OutputStream destination)
			throws IOException {
		try {
			copyNoClose(source, destination);
		} finally {
			safeClose(source);
			safeClose(destination);
		}
	}

	public static void copyNoClose(InputStream source, OutputStream destination)
			throws IOException {
		byte[] buffer = new byte[4096];
		int len = 0;
		while ((len = source.read(buffer)) > 0) {
			destination.write(buffer, 0, len);
		}
	}

	public static boolean deleteFiles(File... listFiles) {
		if (listFiles == null) {
			return true;
		}
		boolean ret = true;
		for (File file : listFiles) {
			if (file != null) {
				if (file.isDirectory()) {
					ret &= deleteFiles(file.listFiles());
				}
				ret &= file.delete();
			}
		}
		return ret;
	}

	public static void copyFiles(File dir, File target) {
		if (dir == null) {
			return;
		}
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file != null) {
					File targetFile = new File(target, file.getName());
					if (file.isDirectory()) {
						targetFile.mkdirs();
						copyFiles(file, targetFile);
					} else {
						try {
							targetFile.getParentFile().mkdirs();
							copy(new BufferedInputStream(new FileInputStream(
									file)), new BufferedOutputStream(
									new FileOutputStream(targetFile)));
						} catch (Exception e) {
							UtilPlugin.log(
									"Failed to copy file:"
											+ file.getAbsolutePath()
											+ " to file:" + targetFile, e);
						}
					}
				}
			}
		} else {
			File targetFile = new File(target, dir.getName());
			try {
				copy(new BufferedInputStream(new FileInputStream(dir)),
						new BufferedOutputStream(new FileOutputStream(
								targetFile)));
			} catch (Exception e) {
				UtilPlugin.log("Failed to copy file:" + dir.getAbsolutePath()
						+ " to file:" + targetFile, e);
			}
		}
	}

	public static String getID(String value) {
		if (value == null) {
			return null;
		}
		StringBuilder textResult = new StringBuilder();
		for (char c : value.toCharArray()) {
			if (!Character.isLetterOrDigit(c)) {
				textResult.append('_');
			} else {
				textResult.append(c);
			}
		}
		return textResult.toString();
	}

	public static String limitSize(String id) {
		if (id.length() > 30) {
			return id.substring(id.length() - 30, id.length());
		}
		return id;
	}

	public static String rlimitSize(String id, int len) {
		if (id.length() > len) {
			return id.substring(0, len);
		}
		return id;
	}

	public static String escape(String text) {
		if (text == null) {
			return null;
		}
		StringBuilder textResult = new StringBuilder();
		for (char c : text.toCharArray()) {
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				if (c == '\r') {
					continue;
				}
			} else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
				if (c == '\r') {
					c = '\n';
				}
			}
			if (c == '\n') {
				textResult.append("\\n");
			} else {
				textResult.append(c);
			}
		}
		return textResult.toString();
	}

	public static String unescape(String substring) {
		return substring.replace("\\n", "\n");
	}

	public static void zipFolder(String srcFolder, String destZipFile)
			throws IOException {
		ZipOutputStream zip = null;
		OutputStream fileWriter = null;

		fileWriter = new BufferedOutputStream(new FileOutputStream(destZipFile));
		zip = new ZipOutputStream(fileWriter);

		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			addFileToZip("", srcFolder + "/" + fileName, zip);
		}
		zip.flush();
		zip.close();
	}

	private static void addFileToZip(String path, String srcFile,
			ZipOutputStream zip) throws IOException {

		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		} else {
			byte[] buf = new byte[4096];
			int len;
			InputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(srcFile));
				zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			} finally {
				StreamUtil.closeSilently(in);
			}
		}
	}

	private static void addFolderToZip(String path, String srcFolder,
			ZipOutputStream zip) throws IOException {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			} else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/"
						+ fileName, zip);
			}
		}
	}

	public static boolean deleteFile(File file) {
		if (file.isDirectory()) {
			deleteFiles(file.listFiles());
		}
		return file.delete();
	}

	public static void copy(InputStream in, OutputStream out, boolean closeIn)
			throws IOException {
		byte[] buffer = new byte[8 * 1024];
		int read = 0;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
		if (closeIn) {
			in.close();
		}
	}

	public static void copy(InputStream in, OutputStream out, boolean closeIn,
			long limit) throws IOException {
		byte[] buffer = new byte[8 * 1024];
		int read = 0;
		long cur = 0;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
			cur += read;
			if (cur > limit) {
				break;// Limit is reached
			}
		}
		if (closeIn) {
			in.close();
		}
	}

	public static void copy(InputStream in, File file, boolean closeIn)
			throws IOException {
		OutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
		copy(in, fos, closeIn);
		fos.flush();
		fos.close();
	}

	public static byte[] getContents(File file) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileInputStream in = new FileInputStream(file);
		copy(in, out, true);
		out.flush();
		return out.toByteArray();
	}

	public static byte[] getContents(File file, long limit) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileInputStream in = new FileInputStream(file);
		copy(in, out, true, limit);
		out.flush();
		return out.toByteArray();
	}

	public static String filename(String uri) {
		int lastSlash = uri.lastIndexOf('/');
		if (lastSlash == -1) {
			return "";
		}
		return uri.substring(lastSlash + 1);
	}

	public static int unzip(File zip, File dest) throws IOException {
		ZipInputStream z = new ZipInputStream(new BufferedInputStream(
				new FileInputStream(zip)));
		ZipEntry entry;
		int filesCount = 0;
		while ((entry = z.getNextEntry()) != null) {
			File entryFile = resolve(dest, entry.getName());
			if (entry.isDirectory()) {
				entryFile.mkdirs();
				continue;
			} else {
				copy(z, entryFile, false);
				filesCount++;
			}
		}
		z.close();
		return filesCount;
	}

	private static File resolve(File base, String name) {
		name = name.replace("/", PATH_SEPARATOR);
		File file = new File(String.format("%s%s%s", base.getAbsolutePath(),
				PATH_SEPARATOR, name));
		createFileParent(file.getParentFile());
		return file;
	}

	private static boolean createFileParent(File file) {
		if (!file.exists()) {
			createFileParent(file.getParentFile());
			file.mkdir();
			return true;
		}
		return true;
	}

	public static void unzipToFolder(InputStream stream, File outputFile) {
		try {
			ZipInputStream zin = new ZipInputStream(new BufferedInputStream(
					stream));
			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				File file = new File(outputFile, name);
				file.getParentFile().mkdirs();
				if (!entry.isDirectory()) {
					OutputStream fout = new BufferedOutputStream(
							new FileOutputStream(file));
					copyNoClose(zin, fout);
					StreamUtil.closeSilently(fout);
				} else {
					file.mkdir();
				}
				entry = zin.getNextEntry();
			}
			StreamUtil.closeSilently(zin);
		} catch (Exception e) {
			UtilPlugin.log(e.getMessage(), e);
		}
	}

	public static void copyNoClose(InputStream source,
			OutputStream destination, MessageDigest md) throws IOException {
		byte[] buffer = new byte[65536];
		int len = 0;
		while ((len = source.read(buffer)) > 0) {
			destination.write(buffer, 0, len);
			md.update(buffer, 0, len);
		}
	}

	public static interface ITraverseRunnable {
		boolean accept(File file, String name);
	}

	public static boolean traverse(File siteRoot, File root,
			ITraverseRunnable runnable) {
		if( siteRoot == null) {
			return true;
		}
		File[] list = siteRoot.listFiles();
		for (File f : list) {
			if (f.isDirectory()) {
				if (!traverse(f, root, runnable)) {
					return false;
				}
				runnable.accept(f, root.toURI().relativize(f.toURI()).getPath());
			} else {
				if (!runnable.accept(f, root.toURI().relativize(f.toURI())
						.getPath())) {
					return false;
				}
			}
		}
		return true;
	}

	private static final String PATH_SEPARATOR = System
			.getProperty("file.separator");

	private static final String invalidFileNameChars = "\\/:*?\"<>|";

	public static boolean isInvalidFileNameChar(char c) {
		return invalidFileNameChars.contains(String.valueOf(c));
	}

	public static String escapeFileName(String text, String extension) {
		StringBuilder result = new StringBuilder();

		for (char c : text.toCharArray())
			if (isInvalidFileNameChar(c))
				result.append("_"); //$NON-NLS-1$
			else
				result.append(c);

		if (extension != null)
			result.append("." + extension); //$NON-NLS-1$

		return result.toString();
	}

}
