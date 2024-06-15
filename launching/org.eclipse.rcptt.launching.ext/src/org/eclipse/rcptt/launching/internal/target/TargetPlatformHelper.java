/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching.internal.target;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.PLUGIN_ID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.emf.common.util.EList;
import org.eclipse.equinox.frameworkadmin.BundleInfo;
import org.eclipse.equinox.internal.launcher.Constants;
import org.eclipse.equinox.internal.simpleconfigurator.utils.SimpleConfiguratorUtils;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEExtensionRegistry;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.target.IUBundleContainer;
import org.eclipse.pde.internal.core.target.P2TargetUtils;
import org.eclipse.pde.internal.core.target.ProfileBundleContainer;
import org.eclipse.pde.internal.launching.launcher.LaunchValidationOperation;
import org.eclipse.rcptt.internal.launching.Q7LaunchingPlugin;
import org.eclipse.rcptt.internal.launching.ext.AJConstants;
import org.eclipse.rcptt.internal.launching.ext.JDTUtils;
import org.eclipse.rcptt.internal.launching.ext.OSArchitecture;
import org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin;
import org.eclipse.rcptt.launching.ext.AUTInformation;
import org.eclipse.rcptt.launching.ext.OriginalOrderProperties;
import org.eclipse.rcptt.launching.ext.Q7LaunchDelegateUtils;
import org.eclipse.rcptt.launching.ext.Q7LaunchingUtil;
import org.eclipse.rcptt.launching.ext.VmInstallMetaData;
import org.eclipse.rcptt.launching.injection.Directory;
import org.eclipse.rcptt.launching.injection.Entry;
import org.eclipse.rcptt.launching.injection.InjectionConfiguration;
import org.eclipse.rcptt.launching.injection.UpdateSite;
import org.eclipse.rcptt.launching.internal.target.Q7Target.AutInstall;
import org.eclipse.rcptt.launching.p2utils.P2Utils;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;
import org.eclipse.rcptt.launching.target.TargetPlatformManager;
import org.eclipse.rcptt.util.FileUtil;
import org.osgi.framework.Version;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

@SuppressWarnings("restriction")
public class TargetPlatformHelper implements ITargetPlatformHelper {
	private static final boolean DEBUG = "true"
			.equals(Platform.getDebugOption("org.eclipse.rcptt.launching.ext/debug"));
	private static final boolean DEBUG_BUNDLES = "true"
			.equals(Platform.getDebugOption("org.eclipse.rcptt.launching.ext/debug/bundles"));
	public static final String IDE_APPLICATION = "org.eclipse.ui.ide.workbench";
	public static final String APPLICATION_PROPERTY = "eclipse.application"; //$NON-NLS-1$
	public static final String PRODUCT_PROPERTY = "eclipse.product"; //$NON-NLS-1$
	private static final String ID_PROPERTY = "id"; //$NON-NLS-1$
	private static final String OSGI_BUNDLES = "osgi.bundles";
	public static final String SIMPLECONFIGURATOR = "org.eclipse.equinox.simpleconfigurator"; //$NON-NLS-1$
	private static final String SC_BUNDLES_PATH = "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info"; //$NON-NLS-1$
	private IStatus status = Status.OK_STATUS;
	private final ITargetDefinition target;
	private final ArrayList<ITargetLocation> extra = new ArrayList<ITargetLocation>();
	private IPluginModelBase[] models;
	private PDEExtensionRegistry registry;

	private Q7Target q7target = new Q7Target();

	public TargetPlatformHelper(ITargetDefinition target) {
		Preconditions.checkNotNull(target);
		this.target = target;
		initialize();
	}

	public Q7Target getQ7Target() {
		return q7target;
	}

	public String getUserArea() {
		final AutInstall install = getAutInstall();
		if (install == null) {
			return null;
		}
		return install.userArea;
	}

	@Deprecated
	/** Use getStatus().isOK() instead */
	public boolean isValid() {
		return getStatus().isOK();
	}

	public boolean isResolved() {
		if (target != null) {
			return target.isResolved();
		}
		return false;
	}

	public boolean isInstanceContainerValid() {
		return getInstanceContainer() != null
				&& getInstanceContainer().getStatus().isOK();
	}

	public IStatus getStatus() {
		if (status == null) {
			return new Status(IStatus.ERROR, PLUGIN_ID, "Target platform is unset");
		}
		return status;
	}

	@Deprecated
	public String getErrorMessage() {
		return getNonMultiStatusMessage(getStatus());
	}

	public String getWarningMessage() {
		if (target != null) {
			IStatus bundleStatus = getBundleWarningStatus();
			return getNonMultiStatusMessage(bundleStatus);
		}
		return null;
	}

	public IStatus getBundleStatus() {
		ITargetLocation[] containers = getBundleContainers();
		if (containers != null) {
			// Check if the containers have any resolution problems
			MultiStatus result = new MultiStatus(
					PDECore.PLUGIN_ID,
					0,
					org.eclipse.pde.internal.core.target.Messages.TargetDefinition_5,
					null);
			for (int i = 0; i < containers.length; i++) {
				ITargetLocation container = containers[i];
				IStatus containerStatus = container.getStatus();
				if (containerStatus != null && !containerStatus.isOK()) {
					result.add(containerStatus);
				}
			}
			if (!result.isOK()) {
				return result;
			}

			// Check if any of the included bundles have problems
			// build status from bundle list
			TargetBundle[] bundles = target.getBundles();
			for (int i = 0; i < bundles.length; i++) {
				TargetBundle bundle = bundles[i];
				if (!bundle.getStatus().isOK()) {
					BundleInfo info = bundle.getBundleInfo();
					URI uri = info.getLocation();
					if (uri != null) {
						File file = new File(uri);
						if (!file.exists()) {
							reportUnexistingFile(info);
							continue;
						}
					}
					result.add(bundle.getStatus());
				}
			}

			if (result.isOK()) {
				// Return generic ok status instead of problem multi-status with
				// no children
				return Status.OK_STATUS;
			}
			return result;
		}
		return Status.OK_STATUS;
	}

	private void reportUnexistingFile(BundleInfo info) {
		// Skip problem of nonexistent file.
		Q7LaunchingPlugin.log(Q7LaunchingPlugin.createStatus(IStatus.WARNING,
				"Skip nonexistent bundle: " + info.getSymbolicName()
						+ ", then resolve configuration.",
				null));
	}

	public IStatus getBundleWarningStatus() {
		ITargetLocation[] containers = getBundleContainers();
		if (containers != null) {
			// Check if the containers have any resolution problems
			MultiStatus result = new MultiStatus(
					PDECore.PLUGIN_ID,
					0,
					org.eclipse.pde.internal.core.target.Messages.TargetDefinition_5,
					null);

			// Check if any of the included bundles have problems
			// build status from bundle list
			TargetBundle[] bundles = target.getBundles();
			for (int i = 0; i < bundles.length; i++) {
				TargetBundle bundle = bundles[i];
				IStatus status = bundle.getStatus();
				if (!status.isOK()) {
					BundleInfo info = bundle.getBundleInfo();
					URI uri = info.getLocation();
					if (uri != null) {
						File file = new File(uri);
						if (!file.exists()) {
							// Skip problem of nonexistent file.
							IStatus st = new Status(IStatus.WARNING,
									status.getPlugin(), status.getMessage(),
									status.getException());
							result.add(st);
						}
					}

				}
			}

			if (result.isOK()) {
				// Return generic ok status instead of problem multi-status with
				// no children
				return Status.OK_STATUS;
			}
			return result;
		}
		return Status.OK_STATUS;
	}

	private String getNonMultiStatusMessage(IStatus st) {
		if (st != null && st.isMultiStatus()) {
			StringBuilder buf = new StringBuilder();
			IStatus[] children = st.getChildren();
			for (IStatus iStatus : children) {
				if (iStatus.isMultiStatus()) {
					String msg = getNonMultiStatusMessage(iStatus);
					if (msg != null) {
						buf.append(msg).append('\n');
					}
				}
				if (!iStatus.isOK()) {
					buf.append(iStatus.getMessage()).append('\n');
				}
			}
			return buf.toString();
		}
		return st == null ? "" : st.getMessage();
	}

	private void initialize() {
		extra.clear();
		q7target = new Q7Target();
		if (this.target != null && this.target.getTargetLocations() != null) {
			ITargetLocation[] containers = this.target.getTargetLocations();
			for (ITargetLocation iUBundleContainer : containers) {
				if (iUBundleContainer instanceof ProfileBundleContainer) {
					getQ7Target().setInstall(iUBundleContainer);
				}
				extra.add(iUBundleContainer);
			}
		}
	}

	public void setTargetName(String name) {
		if (target != null) {
			target.setName(name);
		}
	}

	public String getName() {
		if (target != null) {
			return target.getName();
		}
		return "";
	}

	public void save() throws CoreException {
		if (!getStatus().isOK())
			throw new CoreException(getStatus());
		// Remove previous target platforms with same name.
		TargetPlatformManager.deleteTargetPlatform(target.getName());
		try {
			ITargetPlatformService service = PDEHelper.getTargetService();
			service.saveTargetDefinition(target);
		} catch (CoreException e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
		}
	}

	public String getTargetPlatformProfilePath() {
		ProfileBundleContainer container = (ProfileBundleContainer) getInstanceContainer();
		if (container == null) {
			return "";
		}
		try {
			return container.getLocation(true).toString();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	public void delete() {
		try {
			if (target != null && target.getHandle() != null) {
				PDEHelper.getTargetService().deleteTarget(target.getHandle());
			}
		} catch (CoreException e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
		}
	}

	public ITargetDefinition getTarget() {
		return target;
	}

	public void update() {
		List<ITargetLocation> newContainers = new ArrayList<ITargetLocation>();
		newContainers.addAll(Arrays.asList(target.getTargetLocations()));

		if (extra != null) {
			newContainers.addAll(extra);
		}
		target.setTargetLocations(newContainers
				.toArray(new ITargetLocation[newContainers.size()]));
		models = null;
		registry = null;
	}

	private void addLocations(Collection<ITargetLocation> newLocations) throws CoreException {
		List<ITargetLocation> newContainers = new ArrayList<ITargetLocation>();
		newContainers.addAll(newLocations);
		
		ITargetLocation[] targetLocations = target.getTargetLocations();
		List<ITargetLocation> existingLocations = targetLocations != null ? Arrays.asList(targetLocations) : Collections.emptyList();
		
		for (ITargetLocation location: newLocations) {
			if (existingLocations.contains(location)) {
				String str = location.getLocation(false);
				throw new IllegalStateException("Location " + str + " is already present in target platform");
			}
		}
		newContainers.addAll(existingLocations);
		newContainers.addAll(newLocations);
		
		target.setTargetLocations(newContainers
				.toArray(new ITargetLocation[newContainers.size()]));
		models = null;
		registry = null;
	}

	private IPluginModelBase[] getTargetModels() {
		calcModels();
		return models;
	}

	@Override
	public IPluginModelBase getWeavingHook() {
		calcModels();
		return weavingHook;
	}

	private void calcModels() {
		if (models == null) {
			List<IPluginModelBase> bundles = sumBundles();
			weavingHook = filterHooks(bundles);
			models = bundles.toArray(new IPluginModelBase[0]);
		}
	}

	private List<IPluginModelBase> sumBundles() {
		TargetBundle[] bundles = getTarget().getAllBundles();
		if (bundles == null) {
			return new ArrayList<IPluginModelBase>();
		}
		List<URI> uris = new ArrayList<URI>();
		for (TargetBundle bundle : bundles) {
			uris.add(bundle.getBundleInfo().getLocation());
		}

		PDEState state = new PDEState(uris.toArray(new URI[uris
				.size()]), true, true, new NullProgressMonitor());
		final List<IPluginModelBase> targetModels = new ArrayList<IPluginModelBase>(Arrays.asList(state
				.getTargetModels()));

		if (DEBUG_BUNDLES) {
			final List<String> targetModelsLocations = new ArrayList<String>();
			for (final IPluginModelBase model : targetModels) {
				targetModelsLocations.add(model.getInstallLocation());
			}
			debug("Bundles: " + targetModelsLocations);
		}

		return targetModels;
	}

	private IPluginModelBase filterHooks(List<IPluginModelBase> models) {
		List<IPluginModelBase> hooks = new ArrayList<IPluginModelBase>();
		for (IPluginModelBase model : models) {
			String name = model.getBundleDescription().getSymbolicName();
			if (Objects.equal(name, AJConstants.HOOK)) {
				hooks.add(model);
			}
		}
		switch (hooks.size()) {
		case 0:
			// no weaving hook
			return null;
		case 1:
			// one weaving hook, nothing to filter
			return hooks.get(0);
		default:
			// find a hook with the latest version, remove other hooks
			IPluginModelBase maxHook = hooks.get(0);
			for (int i = 1; i < hooks.size(); i++) {
				IPluginModelBase hook = hooks.get(i);
				if (hook.getBundleDescription().getVersion()
						.compareTo(maxHook.getBundleDescription().getVersion()) > 0) {
					maxHook = hook;
				}
			}
			hooks.remove(maxHook);
			models.removeAll(hooks);
			return maxHook;
		}
	}

	private IPluginModelBase weavingHook;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private IStatus validateBundles(IProgressMonitor monitor) {
		ILaunchConfigurationWorkingCopy wc;
		try {
			wc = Q7LaunchingUtil.createLaunchConfiguration(this);
		} catch (CoreException e) {
			return e.getStatus();
		}
		StringBuilder message = new StringBuilder();
		OSArchitecture architecture = detectArchitecture(true, message);
		if (architecture == null || architecture == OSArchitecture.Unknown) {
			return error(message.toString());
		}
		VmInstallMetaData jvm;
		try {
			jvm = JDTUtils.findVM(architecture);
		} catch (CoreException e2) {
			return e2.getStatus();
		}
		if (jvm == null) {
			return error ("No JVM for architecture " + architecture + " is registered");
		}
		
		wc.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
				String.format(
						"org.eclipse.jdt.launching.JRE_CONTAINER/%s/%s",
						jvm.install.getVMInstallType().getId(),
						jvm.install.getName()));
		
		LaunchValidationOperation validation = new LaunchValidationOperation(wc,
				new HashSet(Arrays.asList(getTargetModels()))) {
			@Override
			protected IExecutionEnvironment[] getMatchingEnvironments()
					throws CoreException {
				IExecutionEnvironmentsManager manager = JavaRuntime
						.getExecutionEnvironmentsManager();
				IExecutionEnvironment[] envs = manager
						.getExecutionEnvironments();
				return envs;
			}
		};
		try {
			wc.delete();
		} catch (CoreException e1) {
			return e1.getStatus();
		}
		try {
			StringBuilder b = new StringBuilder();
			validation.run(monitor);
			Map input = validation.getInput();
			Set<Map.Entry> entrySet = input.entrySet();
			for (Map.Entry e : entrySet) {
				Object value = e.getKey();
				if (value instanceof ResolverError) {
					b.append(value.toString()).append("\n");
				}
			}
			if (b.length() > 0) {
				return error("Bundle validation failed: " + b.toString());
			}
		} catch (CoreException e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
			return status = e.getStatus();
		} catch (OperationCanceledException e) {
			return status = Status.CANCEL_STATUS;
		}
		return status = Status.OK_STATUS;
	}

	private Status error(String message) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message);
	}

	public String[] getProducts() {
		if (!isValid()) {
			return new String[0];
		}
		PDEExtensionRegistry reg = getRegistry();
		Set<String> result = new TreeSet<String>();
		IExtension[] extensions = reg.findExtensions("org.eclipse.core.runtime.products", false); //$NON-NLS-1$
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i]
					.getConfigurationElements();
			if (elements.length != 1)
				continue;
			if (!"product".equals(elements[0].getName())) //$NON-NLS-1$
				continue;
			String id = extensions[i].getUniqueIdentifier();
			if (id != null && id.trim().length() > 0)
				result.add(id);
		}
		return result.toArray(new String[result.size()]);
	}

	public String[] getApplications() {
		if (!isValid()) {
			return new String[0];
		}
		PDEExtensionRegistry reg = getRegistry();

		Set<String> result = new TreeSet<String>();
		IExtension[] extensions = reg.findExtensions("org.eclipse.core.runtime.applications", false); //$NON-NLS-1$
		for (int i = 0; i < extensions.length; i++) {
			String id = extensions[i].getUniqueIdentifier();
			IConfigurationElement[] elements = extensions[i]
					.getConfigurationElements();
			if (elements.length != 1)
				continue;
			String visiblity = elements[0].getAttribute("visible"); //$NON-NLS-1$
			boolean visible = visiblity == null ? true
					: Boolean.valueOf(visiblity).booleanValue();
			if (id != null && visible) {
				result.add(id);
			}
		}
		// result.add(IDE_APPLICATION); //$NON-NLS-1$
		return result.toArray(new String[result.size()]);
	}

	private PDEExtensionRegistry getRegistry() {
		if (registry == null) {
			registry = new PDEExtensionRegistry(getTargetModels());
		}
		return registry;
	}

	public IStatus resolve(IProgressMonitor monitor) {
		ITargetDefinition target = getTarget();
		SubMonitor m = SubMonitor.convert(monitor, "Resolving " + getName(), 2);
		try {
			status = target.resolve(m.split(1, SubMonitor.SUPPRESS_NONE));
			if (!status.isOK())
				return status;
			status = validateBundles(m.split(1, SubMonitor.SUPPRESS_NONE));
			if (!status.isOK())
				return status;
			return status = getBundleStatus();
		} finally {
			m.done();
		}
	}

	AutInstall getAutInstall() {
		final Q7Target target = getQ7Target();
		if (target == null) {
			return null;
		}
		return target.getInstall();
	}

	public OriginalOrderProperties getConfigIniProperties() {
		AutInstall autInstall = getAutInstall();
		if (autInstall == null)
			return new OriginalOrderProperties();
		return autInstall.getConfig();
	}

	protected String getEclipseProductFileProperty(String name) {
		Properties props = getEclipseProductFileProperties();
		return props == null ? null : props.getProperty(name);
	}

	public Properties getEclipseProductFileProperties() {
		File iniFile = new File(getTargetPlatformProfilePath(), ".eclipseproduct"); //$NON-NLS-1$
		if (!iniFile.exists())
			return null;
		Properties pini = new Properties();
		
		try (FileInputStream fis = new FileInputStream(iniFile)) {
			pini.load(fis);
			fis.close();
			return pini;
		} catch (IOException e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
		}
		return null;
	}

	public String getDefaultApplication() {
		String app = getConfigIniProperty(APPLICATION_PROPERTY);
		debug("Application from config.ini: " + app);

		HashSet<String> apps = new HashSet<String>(
				Arrays.asList(getApplications()));
		debug("Valid applications: " + apps);

		return isValidId(app, apps) ? app
				: (isValidId(IDE_APPLICATION, apps) ? IDE_APPLICATION : null);
	}

	public String getBundlesList() {
		String bundles = getConfigIniProperty(OSGI_BUNDLES);
		return bundles == null ? org.eclipse.pde.internal.core.TargetPlatformHelper
				.getDefaultBundleList()
				: org.eclipse.pde.internal.core.TargetPlatformHelper
						.stripPathInformation(bundles);
	}

	protected String getConfigIniProperty(String name) {
		Properties config = getConfigIniProperties();
		if (config == null) {
			return null;
		}
		return config.getProperty(name);
	}

	protected boolean isValidId(String id, Set<String> available) {
		return id == null ? false : available.contains(id);
	}

	public String getDefaultProduct() {
		Set<String> values = new HashSet<String>(Arrays.asList(getProducts()));
		debug("Valid products: " + values);

		String product = null;
		String productProperty = getConfigIniProperty(PRODUCT_PROPERTY);
		if (isValidId(productProperty, values)) {
			product = productProperty;
		}
		debug(PRODUCT_PROPERTY + " from config.ini: " + productProperty);

		// Try to load .eclipseproduct file
		productProperty = getEclipseProductFileProperty(ID_PROPERTY);
		if (isValidId(productProperty, values)) {
			product = productProperty;
		}
		debug(ID_PROPERTY + " from config.ini: " + productProperty);

		// Try to load from application ini file
		List<File> iniFiles = getAppIniFiles();
		for (File file : iniFiles) {
			productProperty = readProductFromIniFile(file);
			if (isValidId(productProperty, values)) {
				product = productProperty;
			}
			debug("Product from " + file + ": " + productProperty);
		}

		return product;
	}

	public ITargetLocation[] getBundleContainers() {
		if (target == null) {
			return new ITargetLocation[0];
		}
		ITargetLocation[] bundleContainers = target.getTargetLocations();
		if (bundleContainers == null) {
			return new ITargetLocation[0];
		}
		return bundleContainers;
	}

	// Repository for all update site injections
	@SuppressWarnings("unused")
	private IFileArtifactRepository repository;

	public IStatus applyInjection(InjectionConfiguration configuration,
			IProgressMonitor monitor) {

		EList<Entry> entries = configuration.getEntries();
		List<ITargetLocation> additionalLocations = new ArrayList<>(); 
		SubMonitor sm = SubMonitor.convert(monitor, "Apply injection plugins", 20 + entries.size() * 20);
		for (Entry entry : entries) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			IStatus result = new Status(IStatus.ERROR, PLUGIN_ID, "Unknown injection type: "
					+ entry.getClass().getName());
			if (entry instanceof UpdateSite) {
				result = processUpdateSite(sm.newChild(20), (UpdateSite) entry, additionalLocations::add);
			} else if (entry instanceof Directory) {
				result = processDirectory((Directory) entry, additionalLocations::add);
			}
			if (result.matches(IStatus.ERROR | IStatus.CANCEL)) {
				return result;
			}
		}
		try {
			addLocations(additionalLocations);
		} catch (CoreException e1) {
			return e1.getStatus();
		}
		IStatus resolveStatus = resolve(sm.split(20, SubMonitor.SUPPRESS_NONE));
		if (!resolveStatus.isOK()) {
			return resolveStatus;
		}
		if (getWeavingHook() == null) {
			return status = createError("No org.eclipse.weaving hook plugin");
		}
		try {
			save();
		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	private static IStatus processDirectory(Directory entry, Consumer<ITargetLocation> collector) {
		String path = entry.getPath();
		MultiStatus rv = new MultiStatus(PLUGIN_ID, 0, "Processing " + path, null);
		if (path.startsWith("platform:///")) {
			// if path is platform uri, lets resolve it to file
			try {
				URL resolve = FileLocator.resolve(URI.create(path).toURL());
				path = resolve.getPath();
			} catch (MalformedURLException e) {
				rv.add(new Status(IStatus.WARNING, PLUGIN_ID, e.getMessage(), e));
			} catch (IOException e) {
				rv.add(new Status(IStatus.WARNING, PLUGIN_ID, e.getMessage(), e));
			}
		}
		ITargetLocation container = PDEHelper.getTargetService()
				.newDirectoryLocation(path);
		collector.accept(container);
		return Status.OK_STATUS;
	}
	
	private static final IStatus createError(String message) {
		return createError(message, null);
	}

	private static final IStatus createError(String message, Throwable error) {
		if (message == null && error != null) {
			message = error.getMessage();
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, message, error);
	}

	private static IStatus processUpdateSite(IProgressMonitor monitor, UpdateSite site, Consumer<ITargetLocation> collector ) {

		try {
			URI uri = URI.create(
					Platform.getOS().equals(Platform.OS_WIN32) ? site.getUri().replace('\\', '/') : site.getUri());
			IMetadataRepository repository = PDEHelper.safeLoadRepository(uri,
					monitor);
			IArtifactRepository artifactRepository = PDEHelper
					.safeLoadArtifactRepository(uri, monitor);
			if (repository == null || artifactRepository == null) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				return createError("Failed to load update site:" + uri);
			}

			List<IInstallableUnit> unitsToInstall = new ArrayList<IInstallableUnit>();

			// Query for all entries and then choose required.
			IQuery<IInstallableUnit> finalQuery = P2Utils.createQuery(site);

			IQueryResult<IInstallableUnit> result = repository.query(finalQuery,
					monitor);
			Set<IInstallableUnit> availableUnits = result.toSet();

			if (site.isAllUnits()) {
				unitsToInstall.addAll(availableUnits);
			} else {
				P2Utils.expandFeatures(monitor, repository, availableUnits);

				Set<String> unitIDs = P2Utils.mapUnitsToId(availableUnits);
				Set<String> missingUnits = new HashSet<String>(site.getUnits());
				missingUnits.removeAll(unitIDs);
				if (!missingUnits.isEmpty()) { // Some units are not available
					return createError("Few units are not available:"
							+ Arrays.toString(missingUnits.toArray()));
				}
				unitsToInstall.addAll(availableUnits);
			}
			if (unitsToInstall.size() > 0) {
				IInstallableUnit[] unitsAsArray = unitsToInstall
						.toArray(new IInstallableUnit[unitsToInstall.size()]);
				URI[] uriArray = new URI[] { uri };

				ITargetLocation container = PDEHelper.getTargetService()
						.newIULocation(unitsAsArray, uriArray,
								IUBundleContainer.INCLUDE_ALL_ENVIRONMENTS);

				collector.accept(container);
			}

			// Lets mirror all required artifacts into bundle pool, since we don't
			// really trust P2.

			IFileArtifactRepository filesRepository = P2TargetUtils
					.getBundlePool();

			List<IInstallableUnit> toInstall = new ArrayList<IInstallableUnit>();
			toInstall.addAll(unitsToInstall);
			P2Utils.installUnits(monitor, artifactRepository, filesRepository,
					toInstall, 10, null, false, P2Utils.getProvisioningAgent());
			MultiStatus rv = new MultiStatus(PLUGIN_ID, 0, "Failed to install next units from repository: "
					+ repository.getName(), null);
			if (toInstall.size() > 0) {
				// Not all plugins are installed.
				for (IInstallableUnit u : toInstall) {
					rv.add(createError(u.getId()));
				}
				return rv;
			}
		} catch (CoreException e) {
			return e.getStatus();
		}

		return Status.OK_STATUS;
	}

	public Map<String, String> getIniEnvironment() {
		List<File> iniFiles = getAppIniFiles();
		Map<String, String> result = new HashMap<String, String>();
		for (File file : iniFiles) {
			Map<String, String> envs = readEnvironmentFromIniFile(file);
			if (envs != null) {
				result.putAll(envs);
			}
		}
		return result;
	}

	public String getIniVMArgs() {
		List<File> iniFiles = getAppIniFiles();
		for (File file : iniFiles) {
			String vmArgs = readVMArgsFromIniFile(file);
			if (vmArgs != null) {
				return vmArgs;
			}
		}
		return null;
	}

	private List<File> getAppIniFiles() {
		List<File> iniFiles = new ArrayList<File>();
		if (getInstanceContainer() == null) {
			return iniFiles;
		}
		File installDirectory = new File(getTargetPlatformProfilePath());

		if (!installDirectory.exists()) {
			return iniFiles;
		}

		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			File[] listFiles = installDirectory.listFiles();
			for (File file : listFiles) {
				if (file.getName().contains(".app")) {
					installDirectory = new File(file, "/Contents/MacOS");
					break;
				}
			}
		}

		File eclipseIniFile = new File(installDirectory, "eclipse.ini"); //$NON-NLS-1$
		if (eclipseIniFile.exists()) {
			iniFiles.add(eclipseIniFile);
			return iniFiles;
		}
		File[] list = installDirectory.listFiles();
		if (list != null) {
			for (File file : list) {
				if (file.getName().endsWith(".ini")) {
					iniFiles.add(file);
				}
			}
		}
		return iniFiles;
	}

	private String readProductFromIniFile(File eclipseIniFile) {
		BufferedReader in = null;
		if (eclipseIniFile.exists()) {
			try {
				in = new BufferedReader(new FileReader(eclipseIniFile));
				String str;
				while ((str = in.readLine()) != null) {
					if (str.trim().equals("-product")) { //$NON-NLS-1$
						String product = in.readLine();
						if (product != null) {
							return product.trim();
						}
					}
				}
			} catch (IOException e) {
				Q7ExtLaunchingPlugin.getDefault().log(e);
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						Q7ExtLaunchingPlugin.getDefault().log(e);
					}
			}
		}
		return null;
	}

	private String readLauncherLibraryFromIniFile(File eclipseIniFile) {
		BufferedReader in = null;
		try {
			if (eclipseIniFile.exists()) {
				in = new BufferedReader(new FileReader(eclipseIniFile));
				String str;
				while ((str = in.readLine()) != null) {
					if (str.trim().equals("--launcher.library")) { //$NON-NLS-1$
						String result = in.readLine();
						if (result != null) {
							return result.trim();
						}
					}
				}
			}
		} catch (IOException e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
		} finally {
			FileUtil.safeClose(in);
		}
		return null;
	}

	private static final String GTK_VERSION = "--launcher.GTK_version";
	private static final String VMARGS = "-vmargs";
	private static final String VM = "-vm";

	private List<String> parseIniFile(File eclipseIni) {
		if (eclipseIni == null || !eclipseIni.exists()) {
			return emptyList();
		}

		List<String> result = new ArrayList<String>();
		try {
			for (String line : Files.readLines(eclipseIni, UTF_8)) {
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) != '#') {
					result.add(line);
				}
			}
		} catch (IOException e) {
			Q7ExtLaunchingPlugin.getDefault().log(e);
		}
		return result;
	}

	private String getVmArg(File eclipseIniFile) {
		List<String> lines = parseIniFile(eclipseIniFile);
		int vmIndex = lines.indexOf("-vm") + 1;
		return vmIndex == 0 || vmIndex == lines.size() ? null
				: lines.get(vmIndex);
	}

	public String getVmFromIniFile() {
		for (File iniFile : getAppIniFiles()) {
			String result = getVmArg(iniFile);
			if (result == null) {
				continue;
			}
			Path iniPath = iniFile.toPath();
			Path vmPath = Paths.get(result);
			if (!vmPath.isAbsolute()) {
				vmPath = iniPath.getParent().resolve(vmPath);
			}
			return vmPath.toString();
		}
		return null;
	}

	private Map<String, String> readEnvironmentFromIniFile(File eclipseIniFile) {
		if (!eclipseIniFile.exists()) {
			return null;
		}

		List<String> lines = parseIniFile(eclipseIniFile);
		Map<String, String> envs = new HashMap<String, String>();

		// mirror GTK_VERSION parameter processing
		// from org.eclipse.equinox.executable.feature / library / eclipse.c
		// see more:
		// https://git.eclipse.org/r/plugins/gitiles/equinox/rt.equinox.framework/+/master/features/org.eclipse.equinox.executable.feature/library/eclipse.c
		int gtkVersionIdx = lines.indexOf(GTK_VERSION);
		if (gtkVersionIdx != -1 && Platform.getOS().equals(Platform.OS_LINUX)) {
			String gtkVersion = lines.get(gtkVersionIdx + 1);
			if ("2".equals(gtkVersion)) {
				envs.put("SWT_GTK3", "0");
			} else {
				envs.put("SWT_GTK3", "1");
			}
		}
		return envs;
	}

	private String readVMArgsFromIniFile(File eclipseIniFile) {
		if (!eclipseIniFile.exists()) {
			return null;
		}

		List<String> lines = parseIniFile(eclipseIniFile);
		int vmargsIndex = lines.indexOf(VMARGS);
		if (vmargsIndex == -1) {
			return null;
		}
		lines = lines.subList(vmargsIndex + 1, lines.size());
		int vmIndex = lines.indexOf(VM);
		if (vmIndex != -1) {
			lines.subList(vmIndex, Math.min(vmIndex + 2, lines.size())).clear();
		}

		if (lines.isEmpty()) {
			return null;
		}

		removeUnsupportedVMArgs(lines);
		addUnresolvedVMArgs(lines);

		return Q7LaunchDelegateUtils.joinCommandArgs(lines);
	}

	private static final String VMARG_ADD_MODULES = "--add-modules";
	private static final String VMARG_PERMIT_ILLEGAL_ACCESS = "--permit-illegal-access";
	private static final String VMARG_ADD_OPENS = "--add-opens";
	private static final String VMARG_ALL_UNNAMED = "ALL-UNNAMED";

	private void removeUnsupportedVMArgs(List<String> lines) {
		String[] javaVersions = getJavaVersions();

		// remove args was added into .ini file to support java 9
		// see more: https://bugs.eclipse.org/bugs/show_bug.cgi?id=493761
		if ((getMajorVersion(javaVersions) == 1 && getMinorVersion(javaVersions) < 9)
				|| getMajorVersion(javaVersions) < 9) {
			Iterator<String> iterator = lines.iterator();
			while (iterator.hasNext()) {
				String line = iterator.next();
				if (line.startsWith(VMARG_ADD_MODULES)
						|| line.startsWith(VMARG_PERMIT_ILLEGAL_ACCESS)
						|| line.startsWith(VMARG_ADD_OPENS)) {
					iterator.remove();
				}
			}
		}
	}

	private void addUnresolvedVMArgs(List<String> lines) {
		int startIndex = lines.indexOf(VMARG_ADD_OPENS);
		if (startIndex != -1) {		
			for (int i = startIndex; i < lines.size(); i++) {
				if (lines.get(i).contains(VMARG_ALL_UNNAMED) && !lines.get(i-1).startsWith(VMARG_ADD_OPENS)) {
					lines.add(i, VMARG_ADD_OPENS);
				}
			}
		}
	}

	private static String[] getJavaVersions() {
		String javaVersion = System.getProperty("java.version");
		return javaVersion.split("\\.|-"); // '.' and '-' are separators
	}

	private static int getMajorVersion(String[] versions) {
		if (versions.length < 1) {
			return 0;
		}
		try {
			return Integer.valueOf(versions[0]);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static int getMinorVersion(String[] versions) {
		if (versions.length < 2) {
			return 0;
		}
		try {
			return Integer.valueOf(versions[1]);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String getTemplateConfigLocation() {
		File iniFile = new File(getTargetPlatformProfilePath(), "configuration/config.ini"); //$NON-NLS-1$
		if (!iniFile.exists())
			return null;
		return iniFile.getAbsolutePath();
	}

	public OSArchitecture detectArchitecture(
			boolean preferCurrentVmArchitecture, StringBuilder detectMsg) {

		String os = Platform.getOS();
		TargetBundle[] bundles = target.getAllBundles();
		for (TargetBundle b : bundles) {
			BundleInfo info = b.getBundleInfo();
			String name = info.getSymbolicName();
			if (name != null && name.startsWith("org.eclipse.equinox.launcher")) {
				if (!name.contains(os)) {
					continue;
				}
				URI location = info.getLocation();
				if (location == null || location.getPath() == null) {
					continue;
				}
					
				if (name.contains("aarch64")) {
					if (detectMsg != null) {
						detectMsg.append("aarch64 arch is selected because AUT uses " + name);
					}
					return OSArchitecture.aarch64;
				} else if (name.contains("x86_64")) {
					if (detectMsg != null) {
						detectMsg.append("x86_64 arch is selected because AUT uses " + name);
					}
					return OSArchitecture.x86_64;
				} else if (name.contains("x86")) {
					if (detectMsg != null) {
						detectMsg.append("x86 arch is selected because AUT uses " + name);
					}
					return OSArchitecture.x86;
					
				}
			}
		}

		return OSArchitecture.Unknown;
	}

	private Map<String, String> getRunlevelsFromSimpleConfigurator() {
		Map<String, String> result = new HashMap<String, String>();
		if (getTargetPlatformProfilePath() == null) {
			return result;
		}
		File infoFile = new File(getTargetPlatformProfilePath(),
				SC_BUNDLES_PATH);
		if (!infoFile.exists()) {
			return result;
		}
		InputStream input = null;
		try {
			input = new FileInputStream(infoFile);
			for (org.eclipse.equinox.internal.simpleconfigurator.utils.BundleInfo bi : (List<org.eclipse.equinox.internal.simpleconfigurator.utils.BundleInfo>) SimpleConfiguratorUtils
					.readConfiguration(input, infoFile.toURI())) {
				String name = bi.getSymbolicName();
				if (name != null) {
					result.put(name, String.format("%d:%b", bi.getStartLevel(), bi.isMarkedAsStarted()));
				}
			}
		} catch (Throwable e) {
			FileUtil.safeClose(input);
		}
		return result;
	}

	public Map<String, String> getRunlevelsMap() {
		Map<String, String> result = new HashMap<String, String>();
		String osgiBundles = getConfigIniProperty(OSGI_BUNDLES);
		if (osgiBundles == null) {
			return result;
		}

		if (osgiBundles.contains(SIMPLECONFIGURATOR)) {
			return getRunlevelsFromSimpleConfigurator();
		}

		// AUT uses bundles specified in osgiBundlesProperty
		StringTokenizer tokenizer = new StringTokenizer(osgiBundles, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int index = token.indexOf('@');
			String id = index != -1 ? token.substring(0, index) : token;
			String tk = token.substring(index + 1);
			if (tk.indexOf(":") == -1) {
				tk = tk + ":default";
			}
			result.put(id, tk);
		}
		return result;
	}

	public String getRuntimeVersion() {
		if (target != null && target.isResolved()) {
			String version = "";
			Version runtimeBundle = getBundleVersion("org.eclipse.rcptt.tesla.swt");
			if (runtimeBundle != null) {
				version = runtimeBundle.toString();
				if (runtimeBundle.getQualifier().equals("qualifier")) {
					// This is for development
					version = version + System.currentTimeMillis();
				}
			}
			return version;
		}
		return "unknown";
	}

	private Version getBundleVersion(String name) {
		if (target != null && target.isResolved()) {
			ITargetLocation[] containers = target.getTargetLocations();
			for (ITargetLocation iBundleContainer : containers) {
				TargetBundle[] bundles = iBundleContainer.getBundles();
				if (bundles != null) {
					for (TargetBundle iResolvedBundle : bundles) {
						BundleInfo info = iResolvedBundle.getBundleInfo();
						if (Objects.equal(info.getSymbolicName(), name)) {
							return new Version(info.getVersion());
						}
					}
				}
				if (iBundleContainer instanceof IUBundleContainer) {
					IUBundleContainer ct = (IUBundleContainer) iBundleContainer;
					try {
						Field ids = IUBundleContainer.class
								.getDeclaredField("fIds");
						Field versions = IUBundleContainer.class
								.getDeclaredField("fVersions");
						ids.setAccessible(true);
						versions.setAccessible(true);
						String[] idsValue = (String[]) ids.get(ct);
						org.eclipse.equinox.p2.metadata.Version[] versionsValue = (org.eclipse.equinox.p2.metadata.Version[]) versions
								.get(ct);
						for (int i = 0; i < idsValue.length; i++) {
							String n = idsValue[i];
							if (n != null && n.equals(name)) {
								return new Version(versionsValue[i].toString());
							}
						}
					} catch (Throwable e) {
						Q7ExtLaunchingPlugin.status(
								"Failed to check runtime version", e);
					}
				}
			}
		}
		return null;
	}

	public String getEquinoxStartupPath(String packageName) {
		IPluginModelBase[] targetModels = getTargetModels();
		for (IPluginModelBase plugin : targetModels) {
			if (plugin.getPluginBase().getId().equals(packageName)) {
				String location = plugin.getInstallLocation();
				if (new File(location).isFile()) {
					return location;
				}
			}
		}
		return null;
	}

	ProfileBundleContainer getInstanceContainer() {
		final AutInstall install = getAutInstall();
		if (install == null) {
			return null;
		}
		return install.container;
	}

	public void setBundleContainers(ITargetLocation[] containers) {
		target.setTargetLocations(containers);
		initialize();
	}

	@Override
	public String toString() {
		return (getName() == null ? "No name" : getName()) + " " + getTargetPlatformProfilePath();
	}

	@Override
	public Map<String, org.eclipse.equinox.p2.metadata.Version> getVersions() throws CoreException {
		if (!getStatus().isOK())
			throw new CoreException(getStatus());
		return AUTInformation.getInformationMap(target);
	}

	private static final String INTERNAL_ARCH_I386 = "i386"; //$NON-NLS-1$
	private static final String PROP_CONFIG_AREA = "osgi.configuration.area"; //$NON-NLS-1$
	private static final String PROP_SHARED_CONFIG_AREA = "osgi.sharedConfiguration.area"; //$NON-NLS-1$
	private static final String PROP_CONFIG_CASCADED = "osgi.configuration.cascaded"; //$NON-NLS-1$

	// for variable substitution
	public static final String VARIABLE_DELIM_STRING = "$"; //$NON-NLS-1$
	public static final char VARIABLE_DELIM_CHAR = '$';

	private static final String ECLIPSE = "eclipse"; //$NON-NLS-1$
	private static final String PRODUCT_SITE_MARKER = ".eclipseproduct"; //$NON-NLS-1$
	private static final String PRODUCT_SITE_ID = "id"; //$NON-NLS-1$
	private static final String PRODUCT_SITE_VERSION = "version"; //$NON-NLS-1$

	private static final String PROP_OS = "osgi.os"; //$NON-NLS-1$
	private static final String PROP_WS = "osgi.ws"; //$NON-NLS-1$
	private static final String PROP_ARCH = "osgi.arch"; //$NON-NLS-1$

	private static final String CONFIG_DIR = "configuration/"; //$NON-NLS-1$
	private static final String CONFIG_FILE = "config.ini"; //$NON-NLS-1$
	private static final String NONE = "@none"; //$NON-NLS-1$
	private static final String NO_DEFAULT = "@noDefault"; //$NON-NLS-1$
	private static final String USER_HOME = "@user.home"; //$NON-NLS-1$
	private static final String USER_DIR = "@user.dir"; //$NON-NLS-1$
	private static final String PROP_USER_HOME = "user.home"; //$NON-NLS-1$
	private static final String PROP_USER_DIR = "user.dir"; //$NON-NLS-1$
	private static final String FILE_SCHEME = "file:"; //$NON-NLS-1$

	public static OriginalOrderProperties processConfiguration(AutInstall autInstall) {
		URL baseConfigurationLocation = null;
		OriginalOrderProperties baseConfiguration = null;
		String location = null;
		if (baseConfigurationLocation == null)
			try {
				baseConfigurationLocation = new URL(autInstall.getInstallLocationURL(), CONFIG_DIR);
			} catch (MalformedURLException e) {
				// leave baseConfigurationLocation null
			}
		baseConfiguration = loadConfiguration(baseConfigurationLocation);
		if (baseConfiguration != null) {
			// if the base sets the install area then use that value if the property. We know the
			// property is not already set.
			location = baseConfiguration.getProperty(PROP_CONFIG_AREA);
		}

		// Now we know where the base configuration is supposed to be. Go ahead and load
		// it and merge into the System properties. Then, if cascaded, read the parent configuration.
		// Note that in a cascaded situation, the user configuration may be ignored if the parent
		// configuration has changed since the user configuration has been written.
		// Note that the parent may or may not be the same parent as we read above since the
		// base can define its parent. The first parent we read was either defined by the user
		// on the command line or was the one in the install dir.
		// if the config or parent we are about to read is the same as the base config we read above,
		// just reuse the base
		OriginalOrderProperties configuration = baseConfiguration;
		final URL configUrl = getConfigurationLocation(location, autInstall.getInstallLocation());

		if (configuration == null || !configUrl.equals(baseConfigurationLocation))
			configuration = loadConfiguration(configUrl);

		if (configuration == null) {
			Q7ExtLaunchingPlugin.getDefault().info("File config.ini from folder \"" + configUrl + "\" was not read");
			return baseConfiguration;
		}

		String configurationArea = configUrl.getFile();
		int index = configurationArea.lastIndexOf("/");
		if (index == configurationArea.length() - 1) {
			configurationArea = configurationArea.substring(0, index);
			index = configurationArea.lastIndexOf("/");
		}
		autInstall.userArea = configurationArea.substring(0, index + 1);

		if ("false".equalsIgnoreCase(configuration.getProperty(PROP_CONFIG_CASCADED))) { //$NON-NLS-1$
			configuration.remove(PROP_SHARED_CONFIG_AREA);
			return configuration;
		} else {

			URL sharedConfigURL = buildLocation(configuration.getProperty(PROP_SHARED_CONFIG_AREA), null, "", //$NON-NLS-1$
					autInstall.getInstallLocation().getAbsolutePath());
			if (sharedConfigURL == null)
				try {
					// there is no shared config value so compute one
					sharedConfigURL = new URL(autInstall.getInstallLocationURL(), CONFIG_DIR);
				} catch (MalformedURLException e) {
					// leave sharedConfigurationLocation null
				}
			// if the parent location is different from the config location, read it too.
			if (sharedConfigURL != null) {
				if (sharedConfigURL.equals(configUrl)) {
					// After all we are not in a shared configuration setup.
					// - remove the property to show that we do not have a parent
					// - merge configuration with the system properties
					return configuration;
				} else {
					// if the parent we are about to read is the same as the base config we read above,
					// just reuse the base
					OriginalOrderProperties sharedConfiguration = baseConfiguration;
					if (!sharedConfigURL.equals(baseConfigurationLocation)) {
						sharedConfiguration = loadConfiguration(sharedConfigURL);
					}
					configuration.remove(PROP_SHARED_CONFIG_AREA);

					return mergeProperties(sharedConfiguration, configuration);
				}
			}
		}
		return null;
	}

	private static OriginalOrderProperties loadConfiguration(URL url) {
		OriginalOrderProperties result = null;
		try {
			url = new URL(url, CONFIG_FILE);
		} catch (MalformedURLException e) {
			return result;
		}
		try {
			if (url != null)
				result = OriginalOrderProperties.load(url);
		} catch (IOException e) {
		}
		return substituteVars(result);
	}

	private static OriginalOrderProperties substituteVars(OriginalOrderProperties result) {
		if (result == null) {
			// nothing todo.
			return null;
		}
		for (Enumeration<?> eKeys = result.keys(); eKeys.hasMoreElements();) {
			Object key = eKeys.nextElement();
			if (key instanceof String) {
				String value = result.getProperty((String) key);
				if (value != null)
					result.put(key, substituteVars(value));
			}
		}
		return result;
	}

	private static String substituteVars(String path) {
		StringBuffer buf = new StringBuffer(path.length());
		StringTokenizer st = new StringTokenizer(path, VARIABLE_DELIM_STRING, true);
		boolean varStarted = false; // indicates we are processing a var subtitute
		String var = null; // the current var key
		while (st.hasMoreElements()) {
			String tok = st.nextToken();
			if (VARIABLE_DELIM_STRING.equals(tok)) {
				if (!varStarted) {
					varStarted = true; // we found the start of a var
					var = ""; //$NON-NLS-1$
				} else {
					// we have found the end of a var
					String prop = null;
					// get the value of the var from system properties
					if (var != null && var.length() > 0)
						prop = System.getProperty(var);
					if (prop == null) {
						try {
							// try using the System.getenv method if it exists (bug 126921)
							Method getenv = System.class.getMethod("getenv", new Class[] { String.class }); //$NON-NLS-1$
							prop = (String) getenv.invoke(null, new Object[] { var });
						} catch (Throwable t) {
							// do nothing;
							// on 1.4 VMs this throws an error
							// on J2ME this method does not exist
						}
					}
					if (prop != null) {
						// found a value; use it
						buf.append(prop);
					} else {
						// could not find a value append the var; keep delemiters
						buf.append(VARIABLE_DELIM_CHAR);
						buf.append(var == null ? "" : var); //$NON-NLS-1$
						buf.append(VARIABLE_DELIM_CHAR);
					}
					varStarted = false;
					var = null;
				}
			} else {
				if (!varStarted)
					buf.append(tok); // the token is not part of a var
				else
					var = tok; // the token is the var key; save the key to process when we find the end token
			}
		}
		if (var != null)
			// found a case of $var at the end of the path with no trailing $; just append it as is.
			buf.append(VARIABLE_DELIM_CHAR).append(var);
		return buf.toString();
	}

	private static URL getConfigurationLocation(String configurationArea, File installLocation) {
		URL configurationLocation = buildLocation(configurationArea, null, "", installLocation.getAbsolutePath()); //$NON-NLS-1$
		if (configurationLocation == null)
			configurationLocation = buildURL(computeDefaultConfigurationLocation(installLocation), true,
					installLocation.getAbsolutePath());

		return configurationLocation;
	}

	private static URL buildLocation(String location, URL defaultLocation, String userDefaultAppendage,
			String installArea) {
		URL result = null;
		// if the instance location is not set, predict where the workspace will be and
		// put the instance area inside the workspace meta area.
		if (location == null)
			result = defaultLocation;
		else if (location.equalsIgnoreCase(NONE))
			return null;
		else if (location.equalsIgnoreCase(NO_DEFAULT))
			result = buildURL(location, true, installArea);
		else {
			if (location.startsWith(USER_HOME)) {
				String base = substituteVar(location, USER_HOME, PROP_USER_HOME);
				location = new File(base, userDefaultAppendage).getAbsolutePath();
			} else if (location.startsWith(USER_DIR)) {
				String base = substituteVar(location, USER_DIR, PROP_USER_DIR);
				location = new File(base, userDefaultAppendage).getAbsolutePath();
			}
			result = buildURL(location, true, installArea);
		}
		return result;
	}

	private static String substituteVar(String source, String var, String prop) {
		String value = System.getProperty(prop, ""); //$NON-NLS-1$
		return value + source.substring(var.length());
	}

	public static URL buildURL(String spec, boolean trailingSlash, String installArea) {
		if (spec == null)
			return null;
		if (File.separatorChar == '\\')
			spec = spec.trim();
		boolean isFile = spec.startsWith(FILE_SCHEME);
		try {
			if (isFile) {
				File toAdjust = new File(spec.substring(5));
				toAdjust = resolveFile(toAdjust, installArea);
				if (toAdjust.isDirectory())
					return adjustTrailingSlash(toAdjust.toURI().toURL(), trailingSlash);
				return toAdjust.toURI().toURL();
			}
			return new URL(spec);
		} catch (MalformedURLException e) {
			// if we failed and it is a file spec, there is nothing more we can do
			// otherwise, try to make the spec into a file URL.
			if (isFile)
				return null;
			try {
				File toAdjust = new File(spec);
				if (toAdjust.isDirectory())
					return adjustTrailingSlash(toAdjust.toURI().toURL(), trailingSlash);
				return toAdjust.toURI().toURL();
			} catch (MalformedURLException e1) {
				return null;
			}
		}
	}

	private static File resolveFile(File toAdjust, String installArea) {
		if (!toAdjust.isAbsolute()) {
			if (installArea != null) {
				if (installArea.startsWith(FILE_SCHEME))
					toAdjust = new File(installArea.substring(5), toAdjust.getPath());
				else if (new File(installArea).exists())
					toAdjust = new File(installArea, toAdjust.getPath());
			}
		}
		return toAdjust;
	}

	private static URL adjustTrailingSlash(URL url, boolean trailingSlash) throws MalformedURLException {
		String file = url.getFile();
		if (trailingSlash == (file.endsWith("/"))) //$NON-NLS-1$
			return url;
		file = trailingSlash ? file + "/" : file.substring(0, file.length() - 1); //$NON-NLS-1$
		return new URL(url.getProtocol(), url.getHost(), file);
	}

	private static String computeDefaultConfigurationLocation(File installLocation) {
		// 1) We store the config state relative to the 'eclipse' directory if possible
		// 2) If this directory is read-only
		// we store the state in <user.home>/.eclipse/<application-id>_<version> where <user.home>
		// is unique for each local user, and <application-id> is the one
		// defined in .eclipseproduct marker file. If .eclipseproduct does not
		// exist, use "eclipse" as the application-id.

		String configurationLocation = null;
		if (canWrite(installLocation))
			configurationLocation = installLocation.getAbsolutePath();
		else
			// We can't write in the eclipse install dir so try for some place in the user's home dir
			configurationLocation = computeDefaultUserAreaLocation(installLocation);

		return configurationLocation + File.separator + CONFIG_DIR;
	}

	private static boolean canWrite(File installDir) {
		if (installDir.canWrite() == false)
			return false;

		if (!installDir.isDirectory())
			return false;

		File fileTest = null;
		try {
			// we use the .dll suffix to properly test on Vista virtual directories
			// on Vista you are not allowed to write executable files on virtual directories like "Program Files"
			fileTest = File.createTempFile("writableArea", ".dll", installDir); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			return false;
		} finally {
			if (fileTest != null)
				fileTest.delete();
		}
		return true;
	}

	private static String computeDefaultUserAreaLocation(File installDir) {
		// we store the state in <user.home>/.eclipse/<application-id>_<version> where <user.home>
		// is unique for each local user, and <application-id> is the one
		// defined in .eclipseproduct marker file. If .eclipseproduct does not
		// exist, use "eclipse" as the application-id.
		String installDirHash = getInstallDirHash(installDir);

		String appName = "." + ECLIPSE; //$NON-NLS-1$
		File eclipseProduct = new File(installDir, PRODUCT_SITE_MARKER);
		if (eclipseProduct.exists()) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(eclipseProduct));
				String appId = props.getProperty(PRODUCT_SITE_ID);
				if (appId == null || appId.trim().length() == 0)
					appId = ECLIPSE;
				String appVersion = props.getProperty(PRODUCT_SITE_VERSION);
				if (appVersion == null || appVersion.trim().length() == 0)
					appVersion = ""; //$NON-NLS-1$
				appName += File.separator + appId + "_" + appVersion + "_" + installDirHash; //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {
				// Do nothing if we get an exception. We will default to a standard location
				// in the user's home dir.
				// add the hash to help prevent collisions
				appName += File.separator + installDirHash;
			}
		} else {
			// add the hash to help prevent collisions
			appName += File.separator + installDirHash;
		}
		appName += '_' + OS_WS_ARCHToString();
		String userHome = System.getProperty(PROP_USER_HOME);
		return new File(userHome, appName).getAbsolutePath(); // $NON-NLS-1$
	}

	private static String OS_WS_ARCHToString() {
		return getOS() + '_' + getWS() + '_' + getArch();
	}

	private static String getWS() {
		String osgiWs = System.getProperty(PROP_WS);
		if (osgiWs != null)
			return osgiWs;
		String osName = getOS();
		if (osName.equals(Constants.OS_WIN32))
			return Constants.WS_WIN32;
		if (osName.equals(Constants.OS_LINUX))
			return Constants.WS_GTK;
		if (osName.equals(Constants.OS_MACOSX))
			return Constants.WS_COCOA;
		if (osName.equals(Constants.OS_HPUX))
			return Constants.WS_GTK;
		if (osName.equals(Constants.OS_AIX))
			return Constants.WS_GTK;
		if (osName.equals(Constants.OS_SOLARIS))
			return Constants.WS_GTK;
		if (osName.equals(Constants.OS_QNX))
			return Constants.WS_PHOTON;
		return Constants.WS_UNKNOWN;
	}

	private static String getOS() {
		String osgiOs = System.getProperty(PROP_OS);
		if (osgiOs != null)
			return osgiOs;
		String osName = System.getProperties().getProperty("os.name"); //$NON-NLS-1$
		if (osName.regionMatches(true, 0, Constants.OS_WIN32, 0, 3))
			return Constants.OS_WIN32;
		// EXCEPTION: All mappings of SunOS convert to Solaris
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_SUNOS))
			return Constants.OS_SOLARIS;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_LINUX))
			return Constants.OS_LINUX;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_QNX))
			return Constants.OS_QNX;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_AIX))
			return Constants.OS_AIX;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_HPUX))
			return Constants.OS_HPUX;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_OS400))
			return Constants.OS_OS400;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_OS390))
			return Constants.OS_OS390;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_ZOS))
			return Constants.OS_ZOS;
		// os.name on Mac OS can be either Mac OS or Mac OS X
		if (osName.regionMatches(true, 0, Constants.INTERNAL_OS_MACOSX, 0, Constants.INTERNAL_OS_MACOSX.length()))
			return Constants.OS_MACOSX;
		return Constants.OS_UNKNOWN;
	}

	private static String getArch() {
		String osgiArch = System.getProperty(PROP_ARCH);
		if (osgiArch != null)
			return osgiArch;
		String name = System.getProperties().getProperty("os.arch");//$NON-NLS-1$
		// Map i386 architecture to x86
		if (name.equalsIgnoreCase(INTERNAL_ARCH_I386))
			return Constants.ARCH_X86;
		// Map amd64 architecture to x86_64
		else if (name.equalsIgnoreCase(Constants.INTERNAL_AMD64))
			return Constants.ARCH_X86_64;

		return name;
	}

	private static String getInstallDirHash(File installDir) {
		// compute an install dir hash to prevent configuration area collisions with other eclipse installs
		int hashCode;
		try {
			hashCode = installDir.getCanonicalPath().hashCode();
		} catch (IOException ioe) {
			// fall back to absolute path
			hashCode = installDir.getAbsolutePath().hashCode();
		}
		if (hashCode < 0)
			hashCode = -(hashCode);
		String installDirHash = String.valueOf(hashCode);
		return installDirHash;
	}

	private static OriginalOrderProperties mergeProperties(OriginalOrderProperties source,
			OriginalOrderProperties userConfiguration) {
		if (userConfiguration != null) {
			source.setBeginAdd(true);
			source.putAll(userConfiguration);
		}
		return source;
	}

	private static void debug(String message) {
		if (DEBUG) {
			Q7ExtLaunchingPlugin.getDefault().info(message);
		}
	}
}
