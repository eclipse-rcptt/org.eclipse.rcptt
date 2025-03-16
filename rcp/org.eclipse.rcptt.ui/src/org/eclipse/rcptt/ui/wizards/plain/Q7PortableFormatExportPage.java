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
package org.eclipse.rcptt.ui.wizards.plain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.persistence.plain.PlainTextPersistenceModel;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.wizards.plain.DestinationsBox.Selection;
import org.eclipse.rcptt.util.FileUtil;

public class Q7PortableFormatExportPage extends WizardPage implements
		IPlainConstants {
	private DestinationsBox destinations;
	private DataBindingContext dbc = new DataBindingContext();

	private ITestCase element;
	private PlainTextPersistenceModel tempModel = null;

	private WritableValue<String> previewValue = new WritableValue<>("", String.class);

	protected Q7PortableFormatExportPage(String pageName, ITestCase element) {
		super(pageName);
		setDescription("Select the destination where RCPTT should store exported content");
		setTitle("RCPTT Portable Format");
		this.element = element;
	}

	public void createControl(Composite parent) {
		Composite cp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(cp);

		Group preview = new Group(cp, SWT.NONE);
		preview.setText("Preview");
		GridDataFactory.fillDefaults().hint(200, 200).grab(true, true)
				.applyTo(preview);
		GridLayoutFactory.swtDefaults().applyTo(preview);
		StyledText text = new StyledText(preview, SWT.READ_ONLY | SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		text.setCaret(null);
		FontData dt = new FontData("monospace", 10, 0);
		text.setFont(new Font(text.getDisplay(), dt));
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(text),
				previewValue);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);

		destinations = new DestinationsBox(dbc) {
			@Override
			protected String getFileTitle() {
				return "Export to File";
			}

			@Override
			protected String getClipboardTitle() {
				return "Export to Clipboard";
			}

			@Override
			protected int getFileKind() {
				return SWT.SAVE;
			}
		};

		destinations.create(cp);
		setControl(cp);

		updateExport();

		destinations.addChangeListener(new Runnable() {
			public void run() {
				Selection kind = destinations.getKind();
				switch (kind) {
				case Clipboard:
					setErrorMessage(null);
					setPageComplete(true);
					break;
				case Filesystem:
					String path = destinations.getFileSystemPath();
					if (path == null || path.trim().length() == 0) {
						setErrorMessage("Please specify correct file name to export testcase");
						setPageComplete(false);
					} /*
					 * else if ((new File(path).exists())) {
					 * setErrorMessage("Specified file doesn't exist");
					 * setPageComplete(false); }
					 */else if ((new File(path).isDirectory())) {
						setErrorMessage("Please specify correct file name to export ");
						setPageComplete(false);
					} else if (!(new File(path).getParentFile().exists())) {
						setErrorMessage("Please specify correct folder to store file in");
						setPageComplete(false);
					} else {
						setPageComplete(true);
						setErrorMessage(null);
					}
					break;
				default:
					break;
				}
			}
		});
	}

	private void saveResourceWithUpdate(Resource element, OutputStream store) {
		try {
			XMIResourceImpl tempResource = new XMIResourceImpl(element.getURI());
			EList<EObject> contents = element.getContents();
			Collection<EObject> copyAll = EcoreUtil.copyAll(contents);
			tempResource.getContents().addAll(copyAll);
			for (EObject eObject : copyAll) {
				if (eObject instanceof Scenario) {
					((Scenario) eObject).setContent(null);
				}
				if (eObject instanceof NamedElement) {
					((NamedElement) eObject).setDescription(null);
				}
			}
			tempResource.save(store, PersistenceManager.getOptions());
		} catch (IOException e) {
			RcpttPlugin.log(e);
		}
	}

	private void updateExport() {
		if (tempModel != null) {
			tempModel.dispose();
		}

		try {
			Resource res = new XMIResourceImpl();
			Scenario elementCopy = (Scenario) EcoreUtil.copy(element
					.getNamedElement());
			res.getContents().add(elementCopy);
			tempModel = new PlainTextPersistenceModel(res);
			IPersistenceModel model = element.getPersistenceModel();
			tempModel.copyFrom(model);
			tempModel.updateAttributes();
			Script ecl = Scenarios.getEcl(element);
			elementCopy.setContent(ecl);
			PersistenceManager.getInstance().updateScenarioContent(res,
					tempModel);

			OutputStream store = tempModel
					.store(PersistenceManager.CONTENT_ENTRY);
			saveResourceWithUpdate(res, store);
			store.close();

			// Attach all contexts
			Set<IContext> ctxs = new HashSet<IContext>();
			RcpttCore.getInstance().findAllContexts(element, ctxs);
			for (IContext iContext : ctxs) {
				OutputStream stream = tempModel.store("/contexts/"
						+ iContext.getID());
				try {
					FileUtil.copy(
							new BufferedInputStream(((IFile) iContext
									.getResource()).getContents()), stream);
				} catch (CoreException e) {
					Q7UIPlugin.log(e);
				}
			}

			// Attach all verifications
			Set<IVerification> verifications = new HashSet<IVerification>();
			RcpttCore.getInstance().findAllVerifications(element, verifications);
			for (IVerification v : verifications) {
				OutputStream stream = tempModel.store("/verifications/"
						+ v.getID());
				try {
					FileUtil.copy(
							new BufferedInputStream(((IFile) v
									.getResource()).getContents()), stream);
				} catch (CoreException e) {
					Q7UIPlugin.log(e);
				}
			}

			File temporaty = tempModel.storeToTemporaty();
			String content = new String(
					FileUtil.getStreamContent(new BufferedInputStream(
							new FileInputStream(temporaty))), ENCODING);
			previewValue.setValue(content);
		} catch (ModelException e) {
			Q7UIPlugin.log(e);
		} catch (IOException e) {
			Q7UIPlugin.log(e);
		}
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	public boolean finish() {
		Selection kind = destinations.getKind();
		switch (kind) {
		case Filesystem:
			try {
				File f = new File(destinations.getFileSystemPath());
				BufferedOutputStream bout = new BufferedOutputStream(
						new FileOutputStream(f));
				OutputStreamWriter wr = new OutputStreamWriter(bout, ENCODING);
				wr.write((String) previewValue.getValue());
				wr.close();
				FileUtil.safeClose(bout);
			} catch (Exception e) {
				Q7UIPlugin.log(e);
				return false;
			}
			break;
		case Clipboard:
			Clipboard clipboard = new Clipboard(Display.getCurrent());
			clipboard.setContents(new Object[] { previewValue.getValue() },
					new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
			break;
		}
		return true;
	}
}
