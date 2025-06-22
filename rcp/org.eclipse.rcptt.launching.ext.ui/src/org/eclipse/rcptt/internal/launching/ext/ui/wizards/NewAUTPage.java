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
package org.eclipse.rcptt.internal.launching.ext.ui.wizards;

import static java.util.Collections.disjoint;
import static org.eclipse.core.runtime.IProgressMonitor.done;
import static org.eclipse.core.runtime.Status.error;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.PLUGIN_ID;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.log;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.internal.debug.ui.jres.JREsPreferencePage;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.internal.launching.ext.PDELocationUtils;
import org.eclipse.rcptt.internal.launching.ext.Q7TargetPlatformManager;
import org.eclipse.rcptt.internal.launching.ext.ui.Activator;
import org.eclipse.rcptt.internal.launching.ext.ui.TimeTriggeredProgressMonitorDialog;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutLaunchState;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.CheckedExceptionWrapper;
import org.eclipse.rcptt.launching.ext.Q7LaunchDelegateUtils;
import org.eclipse.rcptt.launching.ext.Q7LaunchingUtil;
import org.eclipse.rcptt.launching.ext.VmInstallMetaData;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;
import org.eclipse.rcptt.ui.WidgetUtils;
import org.eclipse.rcptt.ui.commons.SWTFactory;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

@SuppressWarnings("restriction")
public class NewAUTPage extends WizardPage {
	private static final int FIELD_TIMEOUT = 500;
	private DataBindingContext dbc = new DataBindingContext();
	private Shell shell;

	private WritableValue<String> nameValue = new WritableValue<String>("", String.class);
	private WritableValue<String> locationValue = new WritableValue<String>("", String.class);
	private WritableValue<ITargetPlatformHelper> info = new WritableValue<ITargetPlatformHelper>(null, ITargetPlatformHelper.class);
	private WritableValue<Boolean> architectureError = new WritableValue<>(Boolean.FALSE, Boolean.class);
	private WritableValue<Boolean> showAdvanced = new WritableValue<Boolean>(Boolean.FALSE, Boolean.class);
	private WritableValue<String> warningMessageValue = new WritableValue<String>("", String.class);
	private WritableValue<Boolean> autolaunchValue = new WritableValue<Boolean>(Boolean.FALSE, Boolean.class);
	private WritableValue<String> autolaunchLabel = new WritableValue<String>("Launch AUT", String.class);

	private String currentName = null;
	private Runnable advancedHandler;
	private record State(String location, ITargetPlatformHelper target) {}
	private final UpdateJob<State> updateJob = new UpdateJob<NewAUTPage.State>("Validating AUT") {
		{
			setUser(false);
		}
		@Override
		protected void run(State input, IProgressMonitor monitor) {
			setStatus(NewAUTPage.this.cancel("Validating..."), false);
			setStatus(validate(input,monitor));
		}
	};

	protected NewAUTPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	void setStatus(final IStatus status) {
		setStatus(status, true);
	}

	void setStatus(final IStatus status, boolean logging) {
		if (!status.isOK() && !status.matches(IStatus.CANCEL) && logging) {
			log(status);
		}
		asyncExec(() -> {
				if (status.matches(IStatus.CANCEL)) {
					setMessage(status.getMessage());
					setErrorMessage(null);
					setPageComplete(false);
				} else if (status.matches(IStatus.ERROR)) {
					setMessage(null);
					setErrorMessage(status.getMessage());
					setPageComplete(false);
				} else if (status.isOK()) {
					setMessage(null);
					setErrorMessage(null);
					setPageComplete(true);
				} else {
					setMessage(status.getMessage());
					setErrorMessage(null);
					setPageComplete(true);
				}
		});
	}

	private State getState() {
		return new State(locationValue.getValue(), (ITargetPlatformHelper) info.getValue());
	}
	
	private IStatus cancel(String message) {
		return new Status(IStatus.CANCEL, getClass(), message);
	}
	
	private IStatus validate(State state, IProgressMonitor monitor) {
		final String location = state.location;
		if (location == null || location.trim().length() == 0) {
			return cancel("Please specify your Eclipse application installation directory.");
		}

		ITargetPlatformHelper helper = state.target();
		SubMonitor sm = SubMonitor.convert(monitor, 2);

		try {
			if (helper == null) {
				TargetPlatformManager.clearTargets();
				IStatus status = checkLocationExists(location);
				if (status.matches(IStatus.ERROR | IStatus.CANCEL)) {
					return status;
				}
	
				ITargetPlatformHelper platform = null;
				platform = Q7TargetPlatformManager.createTargetPlatform(location, sm.split(1, SubMonitor.SUPPRESS_NONE));
				status = platform.getStatus();
				if (status.matches(IStatus.ERROR | IStatus.CANCEL)) {
					platform.delete();
					return status;
				}
				helper = platform;
			}
			IStatus status = validatePlatform(helper, sm.split(1, SubMonitor.SUPPRESS_NONE));
			if (status.matches(IStatus.ERROR | IStatus.CANCEL)) {
				helper.delete();
				return status;
			}
			setValue(helper, info);
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		} finally {
			done(monitor);
		}
	}
	
	private void clean() {
		ITargetPlatformHelper helper = (ITargetPlatformHelper) info.getValue();
		info.setValue(null);
		if (helper != null) {
			helper.delete();
		}
	}

	protected static void throwIfError(IStatus status) throws CoreException {
		if (status.matches(IStatus.ERROR | IStatus.CANCEL)) {
			throw new CoreException(status);
		}
	}

	protected IStatus checkLocationExists(String location) {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			location = manager.performStringSubstitution(location);
			return PDELocationUtils.validateProductLocation(location);
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	private void setError(String message) {
		setStatus(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}

	private IStatus validatePlatform(ITargetPlatformHelper helper, IProgressMonitor monitor) {
		if (helper == null) {
			return cancel("Please specify correct Application installation directory...");
		}

		OSArchitecture architecture = helper.detectArchitecture(new StringBuilder());
		if (OSArchitecture.Unknown.equals(architecture)) {
			return Status.error("Unable to detect AUT's architecture.");
		}
		boolean haveArch = false;
		try {
			haveArch = findJVM(helper).isPresent();
		} catch (CoreException e1) {
			return e1.getStatus();
		}
		setValue(!haveArch, architectureError);

		if (!haveArch) {
			return error("The selected AUT requires " + architecture + " Java VM which cannot be found.");
		}
		
		return Q7LaunchDelegateUtils.validateForLaunch(helper, monitor);
	}

	private Optional<VmInstallMetaData> findJVM(ITargetPlatformHelper helper) throws CoreException {
		StringBuilder message = new StringBuilder();
		OSArchitecture arch = helper.detectArchitecture(message);
		if (OSArchitecture.Unknown.equals(arch)) {
			throw new CoreException(Status.error("Unable to detect AUT's architecture: " + message.toString()));
		}

		return VmInstallMetaData.all().filter(m -> m.arch.equals(arch)).filter(m -> isCompatible(helper, m)).findFirst();
	}

	private boolean isCompatible(ITargetPlatformHelper helper,  VmInstallMetaData m) {
		return disjoint(m.compatibleEnvironments, helper.getIncompatibleExecutionEnvironments());
	}
	
	private boolean validateAUTName() {
		if (((String) nameValue.getValue()).trim().length() == 0) {
			ITargetPlatformHelper helper = info.getValue();
			if (helper != null) {
				String defaultProduct = helper.getDefaultProduct();
				if (defaultProduct != null) {
					nameValue.setValue(helper.getDefaultProduct());
					int i = 2;
					while (!validateAUTName()) {
						nameValue.setValue(helper.getDefaultProduct()
								+ Integer.toString(i));
						i++;
					}
				}
			}
		}
		String name = ((String) nameValue.getValue()).trim();
		if (name.length() == 0) {
			setError("The name of Application Under Test (AUT) can not be empty.");
			return false;
		}
		for (char c : name.toCharArray()) {
			if (FileUtil.isInvalidFileNameChar(c)) {
				setError("Symbol \"" + c
						+ "\" is not acceptable in AUT name.");
				return false;
			}
		}
		if (currentName != null && currentName.equals(name)) {
			return true;
		}
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType type = launchManager
					.getLaunchConfigurationType(Q7LaunchingUtil.EXTERNAL_LAUNCH_TYPE);
			ILaunchConfiguration[] configurations = launchManager
					.getLaunchConfigurations(type);
			for (ILaunchConfiguration iLaunchConfiguration : configurations) {
				if (name.equals(iLaunchConfiguration.getName())) {
					setError(MessageFormat
							.format("Application {0} already exists. Please specify a different name.",
									name));
					return false;
				}
			}
		} catch (Throwable e) {
			Q7UIPlugin.log(e);
		}
		return true;
	}

	@Override
	public void createControl(Composite sparent) {
		initializeDialogUnits(sparent);
		this.shell = sparent.getShell();
		Composite parent = new Composite(sparent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);

		createControlLocation(parent);
		createControlName(parent);
		createControlArch(parent);
		createControlAdvanced(parent);
		createControlAutolaunch(parent);
		createControlWarning(parent);

		IChangeListener validatePlatformListener = new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				validateAUTName();
				updateJob.update(getState());
			}
		};
		info.addChangeListener(validatePlatformListener);

		setControl(parent);
		if (info.getValue() == null) {
			clean();
		}
		updateJob.update(getState());
		Dialog.applyDialogFont(parent);
	}

	private void createControlLocation(Composite parent) {
		// Label and Field
		Label locationLabel = new Label(parent, SWT.NONE);
		locationLabel.setText("Location:");

		Text locationField = new Text(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false)
				.align(SWT.FILL, SWT.CENTER)
				.hint(IDialogConstants.ENTRY_FIELD_WIDTH, SWT.DEFAULT)
				.applyTo(locationField);

		// On change sets page complete = false
		ISWTObservableValue<?> locationModifyObservable = WidgetProperties.text(SWT.Modify).observe(locationField);
		locationModifyObservable.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				setPageComplete(false);
			}
		});
		dbc.bindValue(
				Observables.observeDelayedValue(FIELD_TIMEOUT, locationModifyObservable),
				locationValue);
		// ... and runs validation after delay
		locationValue.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				clean();
				updateJob.update(getState());
			}
		});

		// Browse button
		Button fileLocationButton = new Button(parent, SWT.PUSH);
		fileLocationButton.setText("&Browse...");
		fileLocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSelectLocation();
			}
		});
		GridDataFactory.fillDefaults().grab(false, false).applyTo(fileLocationButton);
		SWTFactory.setButtonDimensionHint(fileLocationButton);
	}

	private void createControlName(Composite parent) {
		// Label and Field
		Label nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText("Name:");

		Text nameField = new Text(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false)
				.align(SWT.FILL, SWT.CENTER)
				.hint(IDialogConstants.ENTRY_FIELD_WIDTH, SWT.DEFAULT)
				.span(2, 1).applyTo(nameField);

		// On change sets page complete = false
		ISWTObservableValue<?> nameModifyObservable = WidgetProperties.text(SWT.Modify).observe(nameField);
		nameModifyObservable.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				setPageComplete(false);
			}
		});
		dbc.bindValue(
				Observables.observeDelayedValue(FIELD_TIMEOUT, nameModifyObservable),
				nameValue);
		// ... and runs validation after delay
		nameValue.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				validateAUTName();
			}
		});
	}

	private void createControlArch(final Composite parent) {
		final Link archLink = new Link(parent, SWT.UNDERLINE_LINK);
		archLink.setText("There is no appropriate JVM configured. <a>Configure JVM...</a>");
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(archLink);
		archLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferenceDialog dialog = PreferencesUtil
						.createPreferenceDialogOn(shell, JREsPreferencePage.ID,
								new String[] { JREsPreferencePage.ID }, null);
				if (dialog.open() == PreferenceDialog.OK) {
					updateJob.update(getState());
				}
			}
		});

		ISWTObservableValue<?> archLinkObservable = WidgetProperties.visible().observe(archLink);
		archLinkObservable.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// Hides container as well (like "display: none")
				GridData data = (GridData) archLink.getLayoutData();
				data.exclude = !archLink.getVisible();
				parent.layout(false);
			}
		});
		dbc.bindValue(archLinkObservable, architectureError);

	}

	private void createControlAdvanced(Composite parent) {
		final Link advanced = new Link(parent, SWT.UNDERLINE_LINK);
		advanced.setText("<a>Advanced...</a>");
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).span(3, 1).grab(true, false).applyTo(advanced);
		advanced.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (advancedHandler != null) {
					advancedHandler.run();
				}
			}
		});

		dbc.bindValue(WidgetProperties.visible().observe(advanced), showAdvanced);

	}

	private void createControlAutolaunch(Composite parent) {
		Button autolaunch = new Button(parent, SWT.CHECK);
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(autolaunch);
		autolaunch.setText("to del");
		autolaunch.setSelection(true);

		dbc.bindValue(WidgetProperties.buttonSelection().observe(autolaunch), autolaunchValue);
		dbc.bindValue(WidgetProperties.text().observe(autolaunch), autolaunchLabel);
	}

	private void createControlWarning(final Composite parent) {
		Label warning = new Label(parent, SWT.WRAP);
		warning.setText("");
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(warning);

		ISWTObservableValue<?> warningObservable = WidgetProperties.text().observe(warning);
		warningObservable.addChangeListener(new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// Corrects size of the label
				parent.layout(false);
			}
		});

		dbc.bindValue(warningObservable, warningMessageValue);
	}

	protected void handleSelectLocation() {
		DirectoryDialog fileDialog = new DirectoryDialog(shell, SWT.NONE);
		fileDialog.setFilterPath((String) locationValue.getValue());
		String text = fileDialog.open();
		if (text != null) {
			locationValue.setValue(text);
		}
	}

	public ITargetPlatformHelper getTarget() {
		return (ITargetPlatformHelper) info.getValue();
	}

	public String getTargetName() {
		return (String) nameValue.getValue();
	}

	public String getTargetLocation() {
		return (String) locationValue.getValue();
	}

	public IVMInstall getJVMInstall() throws CoreException {
		try {
			return Optional.ofNullable(info.getValue()).flatMap(CheckedExceptionWrapper.wrap(this::findJVM)).map(m -> m.install).orElse(null);
		} catch(CheckedExceptionWrapper e) {
			e.rethrow(CoreException.class);
			throw e;
		}
	}

	public OSArchitecture getJVMArch() throws CoreException {
		try {
			return Optional.ofNullable(info.getValue()).flatMap(CheckedExceptionWrapper.wrap(this::findJVM)).map(m -> m.arch).orElse(null);
		} catch(CheckedExceptionWrapper e) {
			e.rethrow(CoreException.class);
			throw e;
		}
	}

	public Boolean isLaunchNeeded() {
		return (Boolean) autolaunchValue.getValue();
	}

	public OSArchitecture getArchitecture() throws CoreException {
		StringBuilder message = new StringBuilder();
		ITargetPlatformHelper helper = info.getValue();
		if (helper == null) {
			throw new CoreException(error("AUT is not configured yet"));
		}
		OSArchitecture arch = helper.detectArchitecture(message);
		if (OSArchitecture.Unknown.equals(arch)) {
			throw new CoreException(error("Unable to detect AUT's architecture: " + message.toString()));
		}
		return arch;
	}

	public void initializeExisting(String configName, String autLocation, final ILaunchConfiguration configuration) {
		// Remove existing target platform
		this.currentName = configName;
		// TargetPlatformManager.deleteTargetPlatform(targetName);
		final ITargetPlatformHelper helper[] = { null };
		final TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(
				shell, 500);
		try {
			dialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						helper[0] = Q7TargetPlatformManager.findTarget(
								configuration, monitor);
					} catch (CoreException e) {
						RcpttPlugin.log(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			Activator.log(e);
		} catch (InterruptedException e) {
			Activator.log(e);
		}
		if (helper[0] != null && helper[0].getStatus().isOK()) {
			info.setValue(helper[0]);
		}
		nameValue.setValue(configName);
		locationValue.setValue(autLocation);
		Boolean isAutLaunched = false;
		if (configuration != null) {
			Aut launch = AutManager.INSTANCE.getByLaunch(configuration);
			if (launch != null) {
				List<AutLaunch> list = launch.getLaunches();
				for (AutLaunch autLaunch : list) {
					if (autLaunch.getState().equals(AutLaunchState.ACTIVE)) {
						warningMessageValue
								.setValue("Running AUT is detected.\nPlease restart AUT manually, after configuration update, if required.");
						isAutLaunched = true;
						break;
					}
				}
			}
		}

		if (isAutLaunched) {
			autolaunchLabel.setValue("Restart AUT");
		} else {
			autolaunchLabel.setValue("Launch AUT");
		}
	}
	
	private <T> void setValue(T value, WritableValue<T> destination) {
		destination.getRealm().exec(() -> {
			destination.setValue(value);
		});
	}

	public void addAdvancedHandler(Runnable runnable) {
		this.showAdvanced.setValue(Boolean.TRUE);
		this.advancedHandler = runnable;
	}
	
	private void asyncExec(Runnable runnable) {
		WidgetUtils.asyncExec(getControl(), runnable);
	}
}
