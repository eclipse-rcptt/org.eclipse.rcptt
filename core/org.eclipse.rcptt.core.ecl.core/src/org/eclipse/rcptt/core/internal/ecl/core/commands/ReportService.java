/*******************************************************************************
 * Copyright (c) 2009, 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.core.internal.ecl.core.commands;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.core.ecl.core.model.BeginReportNode;
import org.eclipse.rcptt.core.ecl.core.model.CreateReport;
import org.eclipse.rcptt.core.ecl.core.model.EndReportNode;
import org.eclipse.rcptt.core.ecl.core.model.GetReport;
import org.eclipse.rcptt.core.ecl.core.model.ReportAppend;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.util.ECLBinaryResourceImpl;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.reporting.core.ReportHelper;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.sherlock.core.INodeBuilder;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportContainer;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.ReportFactory;

public class ReportService implements ICommandService {
	private static final Stack<INodeBuilder> stack = new Stack<INodeBuilder>();

	public ReportService() {
	}

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		if (command instanceof CreateReport) {
			final CreateReport cr = (CreateReport) command;
			ReportManager.createReport(cr.getName(), cr.getQ7info());
		} else if (command instanceof GetReport) {
			Report reportCopy = ReportManager.getReportCopy();
			ReportManager.clear();
			stack.clear();
			ECLBinaryResourceImpl res = new ECLBinaryResourceImpl();
			if (reportCopy != null) {
				res.getContents().add(reportCopy);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ZipOutputStream zout = new ZipOutputStream(bout);
				zout.setLevel(Deflater.BEST_SPEED);
				try {
					ZipEntry e = new ZipEntry("data");
					e.setTime(1);
					zout.putNextEntry(e);
					res.save(zout, null);
					zout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ReportContainer container = ReportFactory.eINSTANCE
						.createReportContainer();
				container.setContent(bout.toByteArray());
				context.getOutput().write(container);
			}
		} else {
			if (command instanceof BeginReportNode) {
				BeginReportNode node = (BeginReportNode) command;
				INodeBuilder nde = ReportManager.getCurrentReportNode().beginTask(node.getName());
				ReportHelper.putProperties(nde, node.getProperties().map());
				stack.push(nde);
			} else if (command instanceof EndReportNode) {
				INodeBuilder nde = null;
				try {
					nde = stack.pop();
				} catch (EmptyStackException e) {
					nde = ReportManager.getCurrentReportNode();
				}
				assert nde != null;
				ReportHelper.takeSnapshot(nde);
				ReportHelper.setResult(nde, ((EndReportNode) command).getResult());
				nde.endTask();
			} else if (command instanceof ReportAppend) {
				ReportAppend cmd = (ReportAppend) command;
				EList<EObject> objects = cmd.getObjects();
				for (EObject eObject : objects) {
					ReportHelper.addSnapshotWithData(ReportManager.getCurrentReportNode(), eObject);
				}
			}
		}
		return Status.OK_STATUS;
	}

}
