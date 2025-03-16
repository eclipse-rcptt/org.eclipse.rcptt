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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.scenario.GroupContext;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.internal.core.model.Q7Folder;
import org.eclipse.rcptt.internal.core.model.Q7ResourceInfo;
import org.eclipse.rcptt.internal.core.model.ReferencedProjectScope;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.utils.WriteAccessChecker;
import org.eclipse.rcptt.ui.wizards.LocationSelectionDialog;
import org.eclipse.rcptt.ui.wizards.plain.DestinationsBox.Selection;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.rcptt.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class Q7PortableFormatImportPage extends WizardPage implements
		IPlainConstants {
	private static final String CONTEXTS_PREFIX = "/contexts/";
	private static final String VERIFICATIONS_PREFIX = "/verifications/";
	private DestinationsBox destinations;
	private DataBindingContext dbc = new DataBindingContext();

	private WritableValue<String> previewValue = new WritableValue<>("", String.class);
	private boolean isValid = false;
	private WritableValue<String> locationValue = new WritableValue<>("", String.class);
	private WritableValue<String> testcaseNameValue = new WritableValue<>("", String.class);

	private IContainer initialContainer;

	protected Q7PortableFormatImportPage(String pageName, IContainer container) {
		super(pageName);
		setDescription("Select the source to import test case from");
		setTitle("RCPTT Portable Format");
		this.initialContainer = container;
		if (this.initialContainer != null) {
			this.locationValue.setValue(initialContainer.getFullPath()
					.toString());
		}
	}

	public void createControl(Composite parent) {
		Composite cp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(cp);

		Composite infoGroup = new Composite(cp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(infoGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(infoGroup);

		Label label = new Label(infoGroup, SWT.LEFT);
		label.setText(Messages.WizardExecutablePage_LocationLabel);

		Text locationText = new Text(infoGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(locationText);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(locationText),
				locationValue);

		Button browse = new Button(infoGroup, SWT.PUSH);
		browse.setText(Messages.WizardExecutablePage_BrowseButton);
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseLocation();
			}
		});
		setButtonLayoutData(browse);

		Label l = new Label(infoGroup, SWT.NONE);
		l.setText("Testcase name:");
		Text testCaseName = new Text(infoGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(testCaseName);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(testCaseName),
				testcaseNameValue);

		destinations = new DestinationsBox(dbc) {
			@Override
			protected String getFileTitle() {
				return "Import from File";
			}

			@Override
			protected String getClipboardTitle() {
				return "Import from Clipboard";
			}

			@Override
			protected int getFileKind() {
				return SWT.OPEN;
			}
		};

		destinations.create(cp);

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
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(text), previewValue);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(text);

		setControl(cp);

		updateImport();

		locationValue.addChangeListener(new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				validate();
			}
		});

		testcaseNameValue.addChangeListener(new IChangeListener() {
			public void handleChange(ChangeEvent event) {
				validate();
			}
		});

		destinations.addChangeListener(new Runnable() {
			public void run() {
				Selection kind = destinations.getKind();
				switch (kind) {
				case Clipboard:
					setErrorMessage(null);
					setPageComplete(true);
					updateImport();
					break;
				case Filesystem:
					String path = destinations.getFileSystemPath();
					if (path == null || path.trim().length() == 0
							|| (new File(path).isDirectory())
							|| !(new File(path).exists())) {
						setErrorMessage("Please specify correct file name to import testcase");
						setPageComplete(false);
					} else {
						setPageComplete(true);
						setErrorMessage(null);
						updateImport();
					}
					break;
				default:
					break;
				}
			}
		});
	}

	protected void browseLocation() {
		LocationSelectionDialog dialog = new LocationSelectionDialog(getShell());
		dialog.setLocation((String) locationValue.getValue());
		if (dialog.open() == LocationSelectionDialog.OK) {
			locationValue.setValue(dialog.getLocation());
		}
	}

	private void updateImport() {
		switch (destinations.getKind()) {
		case Clipboard:
			Clipboard clipboard = new Clipboard(Display.getCurrent());
			Object contents = clipboard.getContents(TextTransfer.getInstance());
			if (contents != null && contents instanceof String) {
				previewValue.setValue((String)contents);
			}
			clipboard.dispose();
			break;
		case Filesystem:
			File file = new File(destinations.getFileSystemPath());
			if (file.exists() && file.isFile()) {
				try {
					byte[] content = FileUtil
							.getStreamContent(new BufferedInputStream(
									new FileInputStream(file)));
					if (content != null) {
						previewValue.setValue(new String(content, ENCODING));
					}
				} catch (Exception e) {
					Q7UIPlugin.log(e);
				}
			}
			break;
		}
		if (previewValue.getValue() == null) {
			isValid = false;
			return;
		}
		// Validate content
		Q7ResourceInfo info = new Q7ResourceInfo(IPlainConstants.PLAIN_HEADER, URI.createURI("__compare__"));
		final IPersistenceModel model = PersistenceManager.getInstance()
				.getModel(
						StringUtils.getUtf8Bytes((String) previewValue
								.getValue()), info.getResource());
		if (model == null) {
			isValid = false;
			validate();
			return;
		}

		isValid = false;
		try {
			info.load(null);
			Scenario element = (Scenario) info.getNamedElement();
			if (element == null) {
				validate();
				return;
			}
			// if (((String) testcaseNameValue.getValue()).trim().isEmpty()) {
			// Update testcase name
			testcaseNameValue.setValue(element.getName());
			// }

			// Validate platform import of modified file

			// update ecl from attachment, to override ecl stored by
			// platform
			InputStream ecl = model.read(PersistenceManager.ECL_CONTENT_ENTRY);
			if (ecl == null) {
				if (element.getTeslaContent() != null
						|| element.getContent() != null) {
					setErrorMessage("Specified content is not valid RCPTT testcase");
					setPageComplete(false);
					return;
				}
			}
			FileUtil.safeClose(ecl);

			model.dispose();
			isValid = true;
		} catch (ModelException e) {
			// Don't log exceptions, because error message about
			// invalid content will be shown in the header
			// RcpttPlugin.log(e);
		}
		validate();
	}

	private void validate() {

		IPath containerPath = new Path((String) locationValue.getValue());// containerSelectionGroup.getContainerFullPath();
		IProject project = getProject();
		if ((null == project)
				|| !Path.EMPTY.isValidPath(containerPath.toString())) {
			setErrorMessage(Messages.WizardExecutablePage_EmptyParentMsg);
			setPageComplete(false);
			return;
		}
		IPath path = containerPath.removeFirstSegments(1);
		if (!path.isEmpty()) {
			IFolder folder = project.getFolder(path);
			if (!folder.exists()) {
				setErrorMessage(Messages.WizardExecutablePage_NotExistingFolderMsg);
				setPageComplete(false);
				return;
			}
		}
		// Validate content
		if (!isValid) {
			setErrorMessage("Specified content is not valid RCPTT testcase");
			setPageComplete(false);
			return;
		}
		String testName = (String) testcaseNameValue.getValue();
		if (testName.trim().length() == 0) {
			setErrorMessage("Please specify testcase name");
			setPageComplete(false);
			return;
		}

		IQ7Folder folder = getFolder();
		if (folder != null) {
			List<String> names = Arrays.asList(Q7SearchCore
					.findAllNames(new ReferencedProjectScope(folder
							.getQ7Project())));
			if (names.contains(testName)) {
				setErrorMessage("Testcase with same name already exists");
				setPageComplete(false);
				return;
			}
		}
		setErrorMessage(null);
		setPageComplete(true);
	}

	private IProject getProject() {
		IResource member = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path((String) locationValue.getValue()));
		if (member != null && member.exists()) {
			return member.getProject();
		}
		if (initialContainer != null) {
			return initialContainer.getProject();
		}
		return null;
	}

	private IQ7Folder getFolder() {
		IResource member = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path((String) locationValue.getValue()));
		IQ7Element el = RcpttCore.create(member);
		if (el != null && el instanceof IQ7Folder) {
			return (IQ7Folder) el;
		} else if (el instanceof IQ7Project) {
			return ((IQ7Project) el).getRootFolder();
		}
		return null;
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	public boolean finish() {
		// Create testcase
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IQ7Folder folder = getFolder();
					WriteAccessChecker writeAccessChecker = new WriteAccessChecker(getShell());
					if (!writeAccessChecker.makeResourceWritable(folder.getResource())) {
						return;
					}
					if (folder != null) {
						IContainer cl = (IContainer) folder.getResource();
						IFile newFile = ((Q7Folder) folder).getNewFile(cl,
								(String) testcaseNameValue.getValue(),
								IQ7Folder.TEST_FILE_EXTENSION);
						newFile.create(
								new ByteArrayInputStream(StringUtils
										.getUtf8Bytes((String) previewValue
												.getValue())), true, monitor);

						IQ7NamedElement newEl = (IQ7NamedElement) RcpttCore.create(newFile);
						ITestCase copy = (ITestCase) newEl
								.getWorkingCopy(new NullProgressMonitor());
						try {
							do {
								copy.setID(EcoreUtil.generateUUID());
							} while (folder.haveIDConflict(copy.getID()));
							copy.setElementName((String) testcaseNameValue
									.getValue());

							// Extract all missing contexts
							IPersistenceModel model = copy
									.getPersistenceModel();
							// Replace Ecl context for testcases exported in
							// platform
							InputStream ecl = model
									.read(PersistenceManager.ECL_CONTENT_ENTRY);
							if (ecl != null) {
								try {
									String streamContent = new String(FileUtil
											.getStreamContent(ecl), ENCODING);
									Script ecl2 = Scenarios.getEcl(copy);
									if (!streamContent
											.equals(ecl2.getContent())) {
										Scenarios.setEclContent((Scenario) copy
												.getNamedElement(),
												streamContent);
									}
								} catch (IOException e) {
									Q7UIPlugin.log(e);
								} finally {
									FileUtil.safeClose(ecl);
								}
							}

							String[] names = model.getNames();
							Map<String, String> contextIDUpdateMap = new HashMap<String, String>();
							Map<String, String> verificationIDUpdateMap = new HashMap<String, String>();
							List<IContext> requiredIDUpdate = new ArrayList<IContext>();
							for (String name : names) {
								if (name.startsWith(CONTEXTS_PREFIX)) {
									String id = name.substring(CONTEXTS_PREFIX
											.length());
									// if (contexts.contains(id)) {
									// Create missing context
									IQ7NamedElement[] ctx = Q7SearchCore
											.findById(
													id,
													new ReferencedProjectScope(
															folder.getQ7Project()),
													monitor);
									boolean extractContext = false;
									if (ctx.length == 0) {
										extractContext = true;
									} else {
										// Compare existing context with
										// extracted one to be same, and if
										// not same lets create new one
										try {
											IFile file = (IFile) ctx[0]
													.getResource();
											byte[] content = FileUtil
													.getStreamContent(new BufferedInputStream(
															file.getContents()));
											byte[] includedContent = FileUtil
													.getStreamContent(model
															.read(name));
											extractContext = !Arrays.equals(
													content, includedContent);
										} catch (Exception e) {
											Q7UIPlugin.log(e);
											extractContext = true;
										}
									}
									if (extractContext) {
										doCreateContext(model.read(name),
												folder, contextIDUpdateMap,
												requiredIDUpdate);
									}

									model.delete(name);
								} else if (name
										.startsWith(VERIFICATIONS_PREFIX)) {
									String id = name
											.substring(VERIFICATIONS_PREFIX
													.length());
									// Create missing verifications
									IQ7NamedElement[] verification = Q7SearchCore
											.findById(
													id,
													new ReferencedProjectScope(
															folder.getQ7Project()),
													monitor);
									boolean extractVerification = false;
									if (verification.length == 0) {
										extractVerification = true;
									} else {
										// Compare existing verification with
										// extracted one to be same, and if
										// not same lets create new one
										try {
											IFile file = (IFile) verification[0]
													.getResource();
											byte[] content = FileUtil
													.getStreamContent(new BufferedInputStream(
															file.getContents()));
											byte[] includedContent = FileUtil
													.getStreamContent(model
															.read(name));
											extractVerification = !Arrays
													.equals(content,
															includedContent);
										} catch (Exception e) {
											Q7UIPlugin.log(e);
											extractVerification = true;
										}
									}
									if (extractVerification) {
										doCreateVerification(model.read(name),
												folder, verificationIDUpdateMap);
									}

									model.delete(name);
								}
							}

							// Update context ids if requried
							List<String> newIds = updateIds(contextIDUpdateMap,
									Arrays.asList(copy.getContexts()));
							if (newIds != null) {
								copy.setContexts(newIds
										.toArray(new String[newIds.size()]));
							}

							newIds = updateIds(verificationIDUpdateMap,
									Arrays.asList(copy.getVerifications()));
							if (newIds != null) {
								copy.setVerifications(newIds
										.toArray(new String[newIds.size()]));
							}

							for (IContext cx : requiredIDUpdate) {
								IQ7NamedElement cxCopy = cx
										.getWorkingCopy(new NullProgressMonitor());
								try {
									GroupContext group = (GroupContext) cxCopy
											.getNamedElement();
									List<String> newGRCtxs = updateIds(
											contextIDUpdateMap,
											group.getContextReferences());
									if (newGRCtxs != null) {
										group.getContextReferences().clear();
										group.getContextReferences().addAll(
												newGRCtxs);

										cxCopy.commitWorkingCopy(true,
												new NullProgressMonitor());
									}
								} finally {
									cxCopy.discardWorkingCopy();
								}
							}

							// Used incremental file name, so replacing the file does not happen.
							copy.commitWorkingCopy(true,
									new NullProgressMonitor());
						} catch (ModelException e) {
							Q7UIPlugin.log(e);
						} finally {
							copy.discardWorkingCopy();
						}
					}
				}

				private void doCreateVerification(InputStream read,
						IQ7Folder folder,
						Map<String, String> verificationIDUpdateMap) {
					IVerification copy = null;
					try {
						IContainer cl = (IContainer) folder.getResource();
						IFile newFile = ((Q7Folder) folder).getNewFile(cl,
								((String) testcaseNameValue.getValue())
										+ "_imported",
								IQ7Folder.VERIFICATION_FILE_EXTENSION);
						newFile.create(read, true, new NullProgressMonitor());
						FileUtil.safeClose(read);

						IVerification newEl = (IVerification) RcpttCore
								.create(newFile);
						copy = (IVerification) newEl
								.getWorkingCopy(new NullProgressMonitor());
						String oldID = copy.getID();

						while (folder.haveIDConflict(copy.getID())) {
							copy.setID(EcoreUtil.generateUUID());
						}
						List<String> names = Arrays.asList(Q7SearchCore
								.findAllNames(new ReferencedProjectScope(folder
										.getQ7Project())));
						String name = copy.getElementName();
						int i = 2;
						while (names.contains(name)) {
							name = copy.getElementName() + "(" + i + ")";
							i++;
						}
						copy.setElementName(name);

						if (copy.hasUnsavedChanges()) {
							verificationIDUpdateMap.put(oldID, copy.getID());
							WriteAccessChecker writeAccessChecker = new WriteAccessChecker(getShell());
							if (writeAccessChecker.makeResourceWritable(copy)) {
								copy.commitWorkingCopy(true,
										new NullProgressMonitor());
							}
						}
					} catch (Exception e) {
						Q7UIPlugin.log(e);
					} finally {
						if (copy != null) {
							try {
								copy.discardWorkingCopy();
							} catch (ModelException e) {
								Q7UIPlugin.log(e);
							}
						}
					}
				}

				private void doCreateContext(InputStream read,
						IQ7Folder folder,
						Map<String, String> contextIDUpdateMap,
						List<IContext> requiredIDUpdate) {
					IContext copy = null;
					try {
						IContainer cl = (IContainer) folder.getResource();
						IFile newFile = ((Q7Folder) folder).getNewFile(cl,
								((String) testcaseNameValue.getValue())
										+ "_imported",
								IQ7Folder.CONTEXT_FILE_EXTENSION);
						newFile.create(read, true, new NullProgressMonitor());
						FileUtil.safeClose(read);

						IContext newEl = (IContext) RcpttCore.create(newFile);
						copy = (IContext) newEl
								.getWorkingCopy(new NullProgressMonitor());
						String oldID = copy.getID();

						while (folder.haveIDConflict(copy.getID())) {
							copy.setID(EcoreUtil.generateUUID());
						}
						List<String> names = Arrays.asList(Q7SearchCore
								.findAllNames(new ReferencedProjectScope(folder
										.getQ7Project())));
						String name = copy.getElementName();
						int i = 2;
						while (names.contains(name)) {
							name = copy.getElementName() + "(" + i + ")";
							i++;
						}
						copy.setElementName(name);

						// copy.setElementName(((String) testcaseNameValue
						// .getValue()) + "_" + copy.getElementName());
						if (copy.getNamedElement() instanceof GroupContext) {
							requiredIDUpdate.add(newEl);
						}
						if (copy.hasUnsavedChanges()) {
							contextIDUpdateMap.put(oldID, copy.getID());
							WriteAccessChecker writeAccessChecker = new WriteAccessChecker(getShell());
							if (writeAccessChecker.makeResourceWritable(copy)) {
								copy.commitWorkingCopy(true,
										new NullProgressMonitor());
							}
						}
					} catch (Exception e) {
						Q7UIPlugin.log(e);
					} finally {
						if (copy != null) {
							try {
								copy.discardWorkingCopy();
							} catch (ModelException e) {
								Q7UIPlugin.log(e);
							}
						}
					}
				}
			}, new NullProgressMonitor());
			return true;
		} catch (CoreException e) {
			Q7UIPlugin.log(e);
		}
		return false;
	}

	private List<String> updateIds(Map<String, String> idUpdateMap,
			List<String> ids) {
		boolean changed = false;
		List<String> newIds = new ArrayList<String>();
		for (String id : ids) {
			String newcx = idUpdateMap.get(id);
			if (newcx != null) {
				changed = true;
				newIds.add(newcx);
			} else {
				newIds.add(id);
			}
		}
		if (changed) {
			return newIds;
		}
		return null;
	}
}
