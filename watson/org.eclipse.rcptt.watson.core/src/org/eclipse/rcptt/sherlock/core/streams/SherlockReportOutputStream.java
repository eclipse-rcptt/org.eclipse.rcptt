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
package org.eclipse.rcptt.sherlock.core.streams;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.rcptt.sherlock.core.SherlockCore;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

public class SherlockReportOutputStream implements Closeable {
	private ZipOutputStream stream;
	int index = 0;

	public SherlockReportOutputStream(OutputStream out) {
		this.stream = new ZipOutputStream(out);
	}

	/**
	 * Return access identifier
	 * 
	 * @param report
	 * @return
	 */
	public synchronized String write(Report report) {
		String postfix = "";
		if (report.getRoot().getName() != null) {
			postfix = SherlockCore.getID(report.getRoot().getName());
		}
		String id = "" + index + "_" + postfix + ".report";
		ZipEntry entry = new ZipEntry(id);
		entry.setTime(1);
		index++;
		try {
			stream.putNextEntry(entry);
			SherlockReportFormat.storeReport(report, stream, false);
		} catch (IOException e) {
			SherlockCore.log(e);
			return null;
		}
		return id;
	}

	public void close() {
		if (index > 0) {
			try {
				stream.close();
			} catch (IOException e) {
				SherlockCore.log(e);
			}
		}
	}
}