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
package org.eclipse.rcptt.ui.report.internal;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.reporting.core.SimpleSeverity;
import org.eclipse.rcptt.reporting.core.TimeFormatHelper;
import org.eclipse.rcptt.reporting.util.ReportEntry;
import org.eclipse.rcptt.ui.actions.AbstractRunAction;
import org.eclipse.rcptt.ui.actions.AbstractRunFailedAction;
import org.eclipse.rcptt.ui.controls.AbstractEmbeddedComposite;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class TestCasesComposite extends AbstractEmbeddedComposite {
	private Composite control;

	private class ReportEntryLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			ReportEntry entry = (ReportEntry) element;
			if (columnIndex == 0) {
				if (entry.getSimpleSeverity() == SimpleSeverity.OK) {
					return Images.getImage(Images.SCENARIO_PASS);
				} else if (entry.getSimpleSeverity() == SimpleSeverity.CANCEL) {
					return Images.getImage(Images.SCENARIO);
				} else {
					return Images.getImage(Images.SCENARIO_FAIL);
				}
			}
			if (columnIndex == 3) {
				if ((entry.getStatusSeverity() & IStatus.WARNING) != 0) {
					return PlatformUI.getWorkbench().getSharedImages()
							.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				}
			}

			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ReportEntry entry = (ReportEntry) element;
			switch (columnIndex) {
			case 0:// name
				return entry.name;
			case 1:// status
				return entry.getSimpleSeverity().name();
			case 2:// time
				return TimeFormatHelper.format(entry.time);
				// case 3:// location
				// return entry.message.isEmpty() ? "" : "Has details";
			}
			return "";
		}
	}

	private final static Function<ReportEntry, String> toId = new Function<ReportEntry, String>() {
		@Override
		public String apply(ReportEntry input) {
			return input.id;
		}
	};

	private final static Function<ReportEntry, Integer> reportToStatus = new Function<ReportEntry, Integer>() {
		@Override
		public Integer apply(ReportEntry input) {
			return input.getStatusSeverity();
		}
	};

	private final static Function<String, IQ7NamedElement> elementById = new Function<String, IQ7NamedElement>() {
		@Override
		public IQ7NamedElement apply(String input) {
			return Q7SearchCore.findById(input);
		}
	};

	private final static Function<IQ7NamedElement, IResource> resourceFromElement = new Function<IQ7NamedElement, IResource>() {
		@Override
		public IResource apply(IQ7NamedElement input) {
			return input.getResource();
		}
	};

	private final BaseSelectionListenerAction runAction = new AbstractRunAction() {

		@Override
		public void run() {
			final Iterable<ReportEntry> reports = filter(filter(getStructuredSelection().toList(), ReportEntry.class),
					notNull());
			final Iterable<String> ids = filter(transform(reports, toId), notNull());
			final Iterable<IQ7NamedElement> elements = filter(transform(ids, elementById), notNull());
			final Iterable<IResource> resources = transform(elements, resourceFromElement);
			LaunchUtils.launchContext(Iterables.toArray(resources, IResource.class), "run");
		}

	};

	Predicate<Integer> matches(final int mask) {
		return new Predicate<Integer>() {

			@Override
			public boolean apply(Integer input) {
				return (input & mask) != 0;
			}
		};

	}
	private final BaseSelectionListenerAction runFailedAction = new AbstractRunFailedAction() {

		@Override
		public void run() {
			Object[] data = ((IStructuredContentProvider) viewer.getContentProvider()).getElements(viewer.getInput());
			final Iterable<ReportEntry> reports = filter(
					filter(asList(data), ReportEntry.class),
					Predicates.and(notNull(), Predicates.compose(matches(IStatus.ERROR), reportToStatus)));
			final Iterable<String> ids = filter(transform(reports, toId), notNull());
			final Iterable<IQ7NamedElement> elements = filter(transform(ids, elementById), notNull());
			final Iterable<IResource> resources = transform(elements, resourceFromElement);
			LaunchUtils.launchContext(Iterables.toArray(resources, IResource.class), "run");
		}

	};
	private TableViewer viewer;

	@Override
	protected void fillActions(IToolBarManager manager) {
		manager.add(runAction);
		manager.add(runFailedAction);
	}

	public TestCasesComposite() {
	}



	@Override
	public String getName() {
		return "Test cases";
	}

	@Override
	public Image getImage() {
		return Images.getImage(Images.FILE);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		control = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(control);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false)
				.applyTo(control);

		viewer = new TableViewer(control, SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION
				| SWT.DOUBLE_BUFFERED);
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 100)
				.applyTo(viewer.getControl());
		TableColumn testcaseColumn = new TableColumn(table, SWT.NONE);
		testcaseColumn.setText("Test case");
		testcaseColumn.setWidth(400);

		TableColumn statusColumn = new TableColumn(table, SWT.NONE);
		statusColumn.setText("Status");
		statusColumn.setWidth(60);

		TableColumn timeColumn = new TableColumn(table, SWT.NONE);
		timeColumn.setText("Time");
		timeColumn.setWidth(80);

		TableColumn detColumn = new TableColumn(table, SWT.NONE);
		detColumn.setText("Details");
		detColumn.setWidth(20);

		viewer.setLabelProvider(new ReportEntryLabelProvider());
		viewer.setContentProvider(new ReportEntryContentProvider());

		viewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				ReportEntry element = (ReportEntry) selection.getFirstElement();
				if (element != null) {
					doOpen(element.id, element.name);
				}
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				ReportEntry element = (ReportEntry) selection.getFirstElement();
				if (element != null) {
					doSelection(element.id, element.name, element.getMessage());
				}
			}
		});
		viewer.addSelectionChangedListener(runAction);
		viewer.addSelectionChangedListener(runFailedAction);
	}

	protected void doSelection(String id, String name, String msg) {
	}

	protected void doOpen(String id, String title) {

	}

	@Override
	public Control getControl() {
		return control;
	}

	public void setReports(final Iterable<ReportEntry> iterator) {
		if (viewer.getControl().isDisposed())
			return;
		viewer.getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				viewer.setInput(iterator);
			}
		});
	}

}
