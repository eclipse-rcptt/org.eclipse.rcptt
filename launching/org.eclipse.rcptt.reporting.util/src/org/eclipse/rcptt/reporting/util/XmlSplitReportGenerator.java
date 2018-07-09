/*******************************************************************************
 * Copyright (c) 2009, 2015 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.reporting.util;

import static org.eclipse.rcptt.util.FileUtil.escapeFileName;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.reporting.core.IReportRenderer;
import org.eclipse.rcptt.reporting.util.internal.Plugin;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.rcptt.util.StringUtils;

public class XmlSplitReportGenerator implements IReportRenderer {

	public IStatus generateReport(IContentFactory factory, String reportName,
			Iterable<Report> reports) {
		try {
			factory.removeFileOrFolder(reportName);
			factory = factory.createFolder(reportName);
			Iterator<Report> report = reports.iterator();
			while (report.hasNext()) {
				Report test = report.next();
				writeContents(factory.createFileStream(getFileName(test.getRoot()
						.getName(), factory)),
						new XMLReportGenerator().generateContent(test));
			}
		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	public String[] getGeneratedFileNames(String reportName) {
		return new String[] { reportName };
	}

	private static void writeContents(OutputStream out, String contents) {
		try {
			out.write(StringUtils.getUtf8Bytes(contents));
		} catch (IOException e) {
			Plugin.UTILS.log(e);
		} finally {
			FileUtil.safeClose(out);
		}
	}

	private static String getFileName(String name, IContentFactory content) {
		int count = 0;
		String escapedName = escapeFileName(name, null);

		String result;
		while (content.isFileExist(result = getFileName(escapedName, "xml", count++)))
			;
		return result;

	}

	private static String getFileName(String baseName, String extension, int count) {
		if (count == 0) {
			return String.format("%s.%s", baseName, extension);
		}
		return String.format("%s (%d).%s", baseName, count, extension);

	}

}
