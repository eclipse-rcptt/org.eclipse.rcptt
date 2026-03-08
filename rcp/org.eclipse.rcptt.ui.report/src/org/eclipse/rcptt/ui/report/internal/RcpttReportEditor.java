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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.rcptt.ecl.internal.core.EMFConverterManager;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.reporting.Q7Info;
import org.eclipse.rcptt.reporting.core.IQ7ReportConstants;
import org.eclipse.rcptt.reporting.util.IndexedExecutionReport;
import org.eclipse.rcptt.reporting.util.ReportEntry;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.ui.report.Q7UIReportPlugin;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import com.google.common.io.Closer;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class RcpttReportEditor extends FormEditor {
	private final Closer closer = Closer.create();
	private final WritableValue<IndexedExecutionReport> reportListObservable = new WritableValue<>(null, IndexedExecutionReport.class) {
		public void doSetValue(IndexedExecutionReport value) {
			try (IndexedExecutionReport old = getValue()) {
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			super.doSetValue(value);
		};	
		@Override
		public void dispose() {
			try (IndexedExecutionReport old = getValue()) {
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} finally {			
				super.dispose();
			}
		}
	};
	private String initialWorkspaceLocation;
	private final StreamIterableAdapter<ReportEntry> entryIterable = new StreamIterableAdapter<ReportEntry>() {
		@SuppressWarnings("resource")
		@Override
		public Stream<ReportEntry> stream() {
			var report = RealmExecutor.syncGetValue(reportListObservable); 
			if (report == null) {
				return Stream.empty();
			}
			return report.read().map( t -> {
				try {
					return t.getEntry();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}
	};
	private final java.nio.file.Path temp_file;

	@SuppressWarnings("resource")
	public RcpttReportEditor() throws IOException {
		temp_file = Files.createTempFile("report_", ".report");
		closer.register(() -> Files.delete(temp_file));
		closer.register(reportListObservable::dispose);
		closer.register(entryIterable);
	}

	@SuppressWarnings("resource")
	@Override
	protected void addPages() {
		try {
			DataBindingContext dbc = new DataBindingContext();
			closer.register(dbc::dispose);
			WritableValue<Iterable<ReportEntry>> reports = new WritableValue<>();
			closer.register(reports::dispose);
			
			UpdateValueStrategy<IndexedExecutionReport, Iterable<ReportEntry>> updateStrategy = new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_UPDATE);
			updateStrategy.setConverter(IConverter.<IndexedExecutionReport, Iterable<ReportEntry>>create(IndexedExecutionReport.class, Iterable.class, (ignored) -> entryIterable::iterator));
			
			dbc.bindValue(reports, reportListObservable, UpdateValueStrategy.never(), updateStrategy);
			addPage(new ReportInformationPage(this, reports, "rcptt.report.info.page",
					"General") {

						@Override
						protected Iterable<Report> getAllReports() {
							IndexedExecutionReport[] r = new IndexedExecutionReport[1];
							reportListObservable.getRealm().exec(() -> r[0] = reportListObservable.getValue());
							return () -> Stream.ofNullable(r[0]).flatMap(IndexedExecutionReport::read).map(t -> {
								try {
									return t.getReport();
								} catch (IOException e) {
									throw new UncheckedIOException(e);
								}
							}).iterator();
						}
				
			});
		} catch (PartInitException e) {
			Q7UIReportPlugin.log(e);
		}
	}

	@SuppressWarnings("resource")
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		// Copy file
		reportListObservable.setValue(null); // close previously opened file
		if (input instanceof IStorageEditorInput) {
			try (InputStream source = ((IStorageEditorInput) input).getStorage().getContents()) {
				Files.copy(source, temp_file, StandardCopyOption.REPLACE_EXISTING);
			} catch (Throwable e) {
				Q7UIReportPlugin.log(e);
			}
		}
		if (input instanceof IURIEditorInput) {
			URI uri = ((IURIEditorInput) input).getURI();
			try (InputStream stream = uri.toURL().openStream()) {
				Files.copy(stream, temp_file, StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				Q7UIReportPlugin.log(e);
			}
		}
		if (input instanceof IFileEditorInput) {
			initialWorkspaceLocation = ((IFileEditorInput) input).getFile()
					.getFullPath().removeLastSegments(1).toString();
		}
		try {
			reportListObservable.setValue(new IndexedExecutionReport(temp_file));
		} catch (IOException e) {
			throw new UncheckedExecutionException(e);
		}

		setPartName(new Path(input.getName()).removeFileExtension().toString());
	}

	public String getInitialWorkspaceLocation() {
		return initialWorkspaceLocation;
	}

	@Override
	public void dispose() {
		try (var c = closer) {
		 
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} finally {
			super.dispose();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void openReport(final String id, final String title) {
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
				getSite().getShell());
		try {
			@SuppressWarnings("resource")
			final IndexedExecutionReport reportList = reportListObservable.getValue();
			if (reportList == null)
				return;
			dialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Opening report...",
							IProgressMonitor.UNKNOWN);
					Report report;
					try {
						report = reportList.getById(id).getReport();
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
					Node root = report.getRoot();
					EMap<String, EObject> properties = root.getProperties();
					final Q7Info info = (Q7Info) properties
							.get(IQ7ReportConstants.ROOT);

					if (info != null) {
						if (info.getId().equals(id)) {
							dialog.getShell().getDisplay()
									.asyncExec(() -> 
											openReportPage(id, title, report,
													info)
									);
							return;
						}
					}
					monitor.done();
				}

			});
		} catch (Throwable e) {
			Q7UIReportPlugin.log(e);
		}
	}

	private void openReportPage(final String id, final String title,
			Report next, Q7Info info) {
		String pageId = "report:" + id;

		IFormPage existingPage = findPage(pageId);
		if (existingPage != null) {
			setActivePage(pageId);
			return;
		}

		ReportPage page = new ReportPage(RcpttReportEditor.this, pageId, title, next);
		try {
			int item = addPage(page);
			if (getContainer() instanceof CTabFolder) {
				CTabItem tabItem = ((CTabFolder) getContainer()).getItem(item);
				tabItem.setShowClose(true);
				IStatus status;
				try {
					status = (IStatus) EMFConverterManager.INSTANCE.fromEObject(info.getResult());
				} catch (ClassCastException e) {
					status = new Status(IStatus.ERROR, Q7UIReportPlugin.PLUGIN_ID, "Invalid test result", e);
				} catch (CoreException e) {
					status = e.getStatus();
				}
				if (status.isOK()) {
					tabItem.setImage(Images.getImageDescriptor(
							Images.SCENARIO_PASS).createImage());
				} else if (status.matches(IStatus.ERROR)) {
					tabItem.setImage(Images.getImageDescriptor(
							Images.SCENARIO_FAIL).createImage());
				} else {
					tabItem.setImage(Images.getImageDescriptor(Images.SCENARIO)
							.createImage());
				}
			}
			setActivePage(item);
		} catch (PartInitException e) {
			Q7UIReportPlugin.log(e);
		}
	}
}
