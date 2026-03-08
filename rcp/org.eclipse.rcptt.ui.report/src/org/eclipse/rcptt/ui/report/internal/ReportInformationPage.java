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
package org.eclipse.rcptt.ui.report.internal;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rcptt.reporting.util.ReportEntry;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.ui.controls.SectionWithToolbar;
import org.eclipse.rcptt.ui.report.ReportWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.forms.widgets.FormImages;

@SuppressWarnings("restriction")
public abstract class ReportInformationPage extends FormPage {
	DataBindingContext dbc = new DataBindingContext();
	private Label nameLabel;
	private Text nameText;
	private Section descriptionSection;
	private final IObservableValue<Iterable<ReportEntry>> reportIterator;

	public ReportInformationPage(FormEditor editor, IObservableValue<Iterable<ReportEntry>> reportIterator, String id, String title) {
		super(editor, id, title);
		this.reportIterator = reportIterator;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		Composite parent = form.getBody();
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0)
				.equalWidth(true).applyTo(parent);

		createGeneralContent(parent, toolkit);

		Composite composite = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().margins(5, 5).spacing(0, 0)
				.equalWidth(false).applyTo(composite);

		final StatisticsComposite statistics = new StatisticsComposite();
		descriptionSection = new SectionWithToolbar(statistics, Section.TITLE_BAR).create(
				composite, toolkit);
		
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(descriptionSection);

		SashForm composite2 = new SashForm(composite, SWT.HORIZONTAL | SWT.SMOOTH);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite2);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5)
				.spacing(0, 0).equalWidth(true).applyTo(composite2);
		// descriptionSection.setExpanded(true);
		final DetailsComposite detailsComposite = new DetailsComposite();
		final TestCasesComposite testCases = new TestCasesComposite() {
			@Override
			protected void doOpen(String id, String title) {
				openReport(id, title);
			}

			@Override
			protected void doSelection(String id, String name, String msg) {
				detailsComposite.updateText(msg);
			}
		};
		new SectionWithToolbar(testCases, Section.TITLE_BAR).create(composite2, toolkit);
		new SectionWithToolbar(detailsComposite, Section.TITLE_BAR).create(
				composite2, toolkit);
		composite2.setWeights(new int[] { 60, 40 });
		reportIterator.addValueChangeListener(new IValueChangeListener<Iterable<ReportEntry>>() {
			@Override
			public void handleValueChange(ValueChangeEvent<? extends Iterable<ReportEntry>> event) {
				final Iterable<ReportEntry> value = (Iterable<ReportEntry>)event.diff.getNewValue();
				testCases.setReports(value);
				statistics.setReports(value);
			}
		});
		reportIterator.addDisposeListener(new IDisposeListener() {

			@Override
			public void handleDispose(org.eclipse.core.databinding.observable.DisposeEvent event) {
				statistics.setReports(null);
				testCases.setReports(null);
			}
		});
		reportIterator.getRealm().asyncExec(new Runnable() {

			@Override
			public void run() {
				final Iterable<ReportEntry> iterator = reportIterator.getValue();
				testCases.setReports(iterator);
				statistics.setReports(iterator);
			}
		});
	}

	protected void openReport(String id, String title) {
		getReportEditor().openReport(id, title);
	}

	protected Composite createGeneralContent(Composite parent,
			FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(parent);

		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(composite);

		createNameControl(composite, toolkit);
		createSaveButton(composite, toolkit);
		// createTagControl(composite, toolkit);
		// createReplayButton(composite, toolkit);

		int height = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		Color top = toolkit.getColors().getColor(IFormColors.H_GRADIENT_END);
		Color bot = toolkit.getColors().getColor(IFormColors.H_GRADIENT_START);

		Image gradientImage = FormImages.getInstance().getGradient(
				new Color[] { top, bot }, new int[] { 100 }, height, true,
				toolkit.getColors().getColor(IFormColors.TB_BG),
				composite.getDisplay());
		// No need to dispose the gradient image cause it's created and stored
		// in resource manager
		composite.setBackgroundImage(gradientImage);

		Composite c1 = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 1).span(4, 1)
				.grab(true, false).applyTo(c1);
		c1.setBackground(toolkit.getColors().getColor(
				IFormColors.H_BOTTOM_KEYLINE1));

		Composite c2 = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 1).span(4, 1)
				.grab(true, false).applyTo(c2);
		c2.setBackground(toolkit.getColors().getColor(
				IFormColors.H_BOTTOM_KEYLINE2));

		if (!Platform.getOS().equals(Platform.OS_MACOSX)) {
			// We need to simulate transparent background for labels
			composite.layout();
			// setBackgroundToHeaderLabel(nameLabel, gradientImage);
			// setBackgroundToHeaderLabel(tagsLabel, gradientImage);
		}

		return composite;
	}

	protected void setBackgroundToHeaderLabel(Label label, Image backgroundImage) {
		Rectangle bounds = label.getBounds();
		bounds.x = 0;
		bounds.width = 1;
		final Image image = new Image(label.getDisplay(), bounds.width, bounds.height);
		GC gc = new GC(image);
		gc.drawImage(backgroundImage, bounds.x, bounds.y, bounds.width,
				bounds.height, 0, 0, bounds.width, bounds.height);
		gc.dispose();
		label.setBackgroundImage(image);
		label.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}
		});
	}

	protected void createNameControl(Composite parent, FormToolkit toolkit) {
		nameLabel = createLabel(parent, toolkit, "Name:");
		nameText = createWrappedText(parent, toolkit,
				getEditor().getPartName(), 2);
		nameText.setEditable(false);
		nameText.setBackground(null);
	}

	protected Label createLabel(Composite parent, FormToolkit toolkit,
			final String label) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);
		labelControl.setForeground(toolkit.getColors().getColor(
				IFormColors.TITLE));
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
				.applyTo(labelControl);
		labelControl.setBackground(null);
		return labelControl;
	}

	protected Text createWrappedText(Composite parent, FormToolkit toolkit,
			String text, int hSpan) {
		Composite composite = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().equalWidth(true).applyTo(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.span(hSpan, 1).grab(true, false).applyTo(composite);
		Text textControl = toolkit.createText(composite, text, SWT.BORDER
				/* | SWT.WRAP | SWT.MULTI */);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).hint(111, SWT.DEFAULT).applyTo(textControl);
		textControl.setBackground(null);
		return textControl;
	}

	protected Button createSaveButton(Composite composite, FormToolkit toolkit) {
		Button button = toolkit.createButton(composite, "Save as...", SWT.PUSH);
		button.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT)
				.createImage());
		button.setBackground(null);
		GridDataFactory.fillDefaults().applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ReportWizard wizard = new ReportWizard(getAllReports(),
						new Path(getEditorInput().getName())
								.removeFileExtension().toString());
				wizard.setDisabledReports("report");
				wizard.setInitialWorkspaceLocation(getReportEditor()
						.getInitialWorkspaceLocation());
				wizard.setNeedsProgressMonitor(true);
				WizardDialog dialog = new WizardDialog(nameLabel.getShell(),
						wizard);
				dialog.open();
			}

		});
		return button;
	}

	protected abstract Iterable<Report> getAllReports();

	private RcpttReportEditor getReportEditor() {
		return ((RcpttReportEditor) getEditor());
	}
}
