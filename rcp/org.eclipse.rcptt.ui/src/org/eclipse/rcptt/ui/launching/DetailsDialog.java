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
package org.eclipse.rcptt.ui.launching;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.reporting.core.ImageEntry;
import org.eclipse.rcptt.reporting.util.RcpttReportGenerator;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public final class DetailsDialog extends Dialog {
	static final String LINE_SEPARATOR = System
			.getProperty("line.separator"); //$NON-NLS-1$
	private StyledText text;
	private Report report;

	public DetailsDialog(Shell parentShell, Report report) {
		super(parentShell);
		this.report = report;
		setShellStyle(getShellStyle() | SWT.SHEET);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private List<ImageEntry> images = new ArrayList<ImageEntry>();
	private int current = -1;
	private Composite bar;
	private Button nextButton;
	private Button prevButton;
	private Label imageIndex;
	private Label descriptionField;
	private Label imageControl;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite content = (Composite) super.createDialogArea(parent);
		CTabFolder folder = new CTabFolder(content, SWT.NONE);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		CTabItem screenshots = new CTabItem(folder, SWT.NONE);
		screenshots.setText("Screenshots");

		CTabItem item = new CTabItem(folder, SWT.NONE);
		text = new StyledText(folder, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		text.setFont(JFaceResources.getTextFont());
		item.setControl(text);
		item.setText("Description");
		final Composite ct = new Composite(folder, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(ct);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(ct);

		generateContent();

		if (images.size() == 0) {
			screenshots.dispose();
		}

		final ScrolledComposite sc;
		if (images.size() > 0) {
			sc = new ScrolledComposite(ct, SWT.HORIZONTAL | SWT.VERTICAL
					| SWT.BORDER);
			GridDataFactory.swtDefaults().grab(true, true).applyTo(sc);
			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(sc);
			imageControl = new Label(sc, SWT.BORDER);
			imageControl.setText(" ");
			GridDataFactory.fillDefaults().grab(true, true)
					.applyTo(imageControl);
			sc.setContent(imageControl);

			screenshots.setControl(ct);
		} else {
			sc = null;
		}
		Label info = new Label(ct, SWT.NONE);
		info.setText("Details:");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(info);
		descriptionField = new Label(ct, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(descriptionField);

		if (sc != null) {
			bar = null;
			if (images.size() > 1) {
				bar = new Composite(ct, SWT.NONE);
				GridDataFactory.fillDefaults().grab(false, false)
						.align(SWT.RIGHT, SWT.CENTER).applyTo(bar);
				GridLayoutFactory.fillDefaults().numColumns(4).applyTo(bar);

				imageIndex = new Label(bar, SWT.NONE);

				prevButton = new Button(bar, SWT.PUSH);
				prevButton.setText("Previous");
				nextButton = new Button(bar, SWT.PUSH);
				nextButton.setText("Next");

				setButtonLayoutData(prevButton);
				setButtonLayoutData(nextButton);

				nextButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						updateImage(sc, 1);
					}
				});
				prevButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						updateImage(sc, -1);
					}
				});
			}
		}

		if (sc != null) {
			updateImage(sc, 1);
		}
		folder.setSelection(0);
		return content;
	}

	private void showImage(Label description, final Label imageControl,
			int selection) {
		if (current != selection) {
			if (images.size() > selection) {
				ImageEntry imgData = images.get(selection);
				description.setText(imgData.description);
				Image img = new Image(imageControl.getDisplay(),
						new ByteArrayInputStream(imgData.data));
				imageControl.setImage(img);
				imageControl.addDisposeListener(event -> img.dispose());
				current = selection;
			}
			// Update button enablement
			if (nextButton != null && prevButton != null && imageIndex != null) {
				prevButton.setEnabled(current != 0);
				nextButton.setEnabled(current != images.size() - 1);
				imageIndex.setText("Screenshot " + (current + 1) + " from "
						+ images.size());
				bar.layout();
			}
		}
	}

	private void generateContent() {
		String value = ""; //$NON-NLS-1$
		// if (info != null) {
		// value = new AdvancedInformationGenerator().generateContent(info);
		// }
		if (report != null) {
			StringWriter writer = new StringWriter();

			PrintWriter printWriter = new PrintWriter(writer);
			printWriter.println("Report:");
			new RcpttReportGenerator(printWriter, this.images).writeReport(report, 1);
			value = writer.toString();
		}
		text.setText(value);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.DetailsDialog_Title);
		Rectangle bounds = newShell.getBounds();
		newShell.setBounds(bounds.x, bounds.y, 800, 600);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	private void updateImage(final ScrolledComposite sc, int change) {
		int selection = current + change;
		if (selection < 0 || selection >= images.size()) {
			return;
		}
		showImage(descriptionField, imageControl, selection);
		Point size = imageControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		sc.setMinSize(size);
	}
}
