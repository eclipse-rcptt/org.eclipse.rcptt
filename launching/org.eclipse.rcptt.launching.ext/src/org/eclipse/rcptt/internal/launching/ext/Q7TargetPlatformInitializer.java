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
package org.eclipse.rcptt.internal.launching.ext;

import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.rcptt.internal.launching.ext.Q7ExtLaunchingPlugin.PLUGIN_ID;
import static org.eclipse.rcptt.internal.launching.ext.Q7UpdateSiteExtensions.Q7RuntimeInfo.RAP_PLATFORM;
import static org.eclipse.rcptt.internal.launching.ext.Q7UpdateSiteExtensions.Q7RuntimeInfo.SWT_PLATFORM;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.rcptt.internal.launching.ext.Q7UpdateSiteExtensions.Q7RuntimeInfo;
import org.eclipse.rcptt.launching.ext.AUTInformation;
import org.eclipse.rcptt.launching.injection.InjectionConfiguration;
import org.eclipse.rcptt.launching.injection.InjectionFactory;
import org.eclipse.rcptt.launching.injection.UpdateSite;
import org.eclipse.rcptt.launching.internal.target.PDEHelper;
import org.eclipse.rcptt.launching.internal.target.TargetPlatformHelper;
import org.eclipse.rcptt.launching.target.ITargetPlatformHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class Q7TargetPlatformInitializer {

	private static final String GMF = "gmf";
	private static final String DRAW2D = "draw2d";
	private static final String GEF = "gef";
	private static final String EMF_FEATURE_GROUP = "org.eclipse.emf.feature.group";
	private static final String EQUINOX_EXECUTABLE_FEATURE_GROUP = "org.eclipse.equinox.executable.feature.group";
	private static final String EMF_VALIDATION_FEATURE_GROUP = "org.eclipse.emf.validation.feature.group";
	private static final String EMF_TRANSACTION_FEATURE_GROUP = "org.eclipse.emf.transaction.feature.group";
	public static final String P2_GROUP_FEATURE = "org.eclipse.equinox.p2.type.group";
	public static final String P2_CATEGORY_FEATURE = "org.eclipse.equinox.p2.type.category";

	public static class Q7Info {
		public final URI q7;
		public final URI aspectj;
		public final URI deps;
		public final List<URI> extra;

		public Q7Info(URI q7, URI aspectj, URI deps, List<URI> extra) {
			checkArgument(q7 != null);
			checkArgument(aspectj != null);
			checkArgument(deps != null);
			this.q7 = q7;
			this.aspectj = aspectj;
			this.deps = deps;
			this.extra = extra == null ? Collections.<URI> emptyList() : extra;
		}
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

	public static IStatus initialize(ITargetPlatformHelper target,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor sm = SubMonitor.convert(monitor, "Initialize AUT configuration", 100);
		if (sm.isCanceled())
			return Status.CANCEL_STATUS;

		{
			IStatus rv = target.getStatus();
			if (rv.matches(IStatus.ERROR | IStatus.CANCEL))
				return rv;
		}

		Map<String, Version> map = target.getVersions();

		Q7Info q7Info = getInfo(target, map);
		sm.worked(20);

		try {
			// Check for dependencies
			IMetadataRepository repository = PDEHelper.safeLoadRepository(
					q7Info.q7, sm.split(20, SubMonitor.SUPPRESS_NONE));
			if (repository == null) {
				if (sm.isCanceled())
					return Status.CANCEL_STATUS;
				return createError("Failed to load repository from " + q7Info.q7);
			}

			InjectionConfiguration injectionConfiguration = createInjectionConfiguration(
					new NullProgressMonitor(), q7Info, map);
			MultiStatus rv = new MultiStatus(PLUGIN_ID, 0, "Runtime injection failed for target platform " + target,
					null);
			if (injectionConfiguration != null) {
				rv.add(target.applyInjection(injectionConfiguration, sm.split(60, SubMonitor.SUPPRESS_NONE)));
				if (rv.matches(IStatus.CANCEL))
					return rv;
			}
			if (!rv.isOK())
				return rv;
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	public static InjectionConfiguration createInjectionConfiguration(
			IProgressMonitor monitor, Q7Info q7Info, Map<String, Version> map) {
		boolean hasEMF = map.containsKey(AUTInformation.EMF);
		boolean hasEMFWorkspace = map.containsKey(AUTInformation.EMF_WORKSPACE);
		boolean hasEMFTransaction = map
				.containsKey(AUTInformation.EMF_TRANSACTION);
		boolean hasEMFValidation = map
				.containsKey(AUTInformation.EMF_VALIDATION);
		boolean hasRAP = map.containsKey(AUTInformation.RAP);

		InjectionConfiguration injectionConfiguration = InjectionFactory.eINSTANCE
				.createInjectionConfiguration();

		// Add Q7 plugins
		UpdateSite q7Site = InjectionFactory.eINSTANCE.createUpdateSite();
		q7Site.setUri(q7Info.q7.toString());
		injectionConfiguration.getEntries().add(q7Site);

		// Add aspectj plugins
		UpdateSite aspectsSite = InjectionFactory.eINSTANCE.createUpdateSite();
		aspectsSite.setUri(q7Info.aspectj.toString());
		injectionConfiguration.getEntries().add(aspectsSite);

		UpdateSite q7Deps = InjectionFactory.eINSTANCE.createUpdateSite();
		q7Deps.setUri(q7Info.deps.toString());

		if (!hasRAP) {
			if (!hasEMFTransaction) {
				q7Deps.getUnits().add(EMF_TRANSACTION_FEATURE_GROUP);
			}
			if (!hasEMFValidation) {
				q7Deps.getUnits().add(EMF_VALIDATION_FEATURE_GROUP);
			}
			if (!hasEMF) {
				q7Deps.getUnits().add(EMF_FEATURE_GROUP);
			}
		}
		if (hasRAP) {
			q7Deps.getUnits().add(EQUINOX_EXECUTABLE_FEATURE_GROUP);
			q7Deps.getUnits().add("org.eclipse.rap.equinox.target.feature.feature.group");
			q7Deps.getUnits().add("org.eclipse.rap.feature.feature.group");
			q7Deps.getUnits().add("org.eclipse.emf.common");
			q7Deps.getUnits().add("org.eclipse.emf.ecore");
			q7Deps.getUnits().add("org.eclipse.emf.edit");
			q7Deps.getUnits().add("org.eclipse.emf.ecore.edit");
			// q7Deps.getUnits().add("org.eclipse.emf.codegen");
			// q7Deps.getUnits().add("org.eclipse.emf.codegen.ecore");
			// q7Deps.getUnits().add("org.eclipse.emf.converter");
			q7Deps.getUnits().add("org.eclipse.emf.databinding");
			q7Deps.getUnits().add("org.eclipse.emf.mapping");
			q7Deps.getUnits().add("org.eclipse.emf.mapping.ecore");
			q7Deps.getUnits().add("org.eclipse.emf.databinding.edit");
			q7Deps.getUnits().add("org.eclipse.emf");
			// q7Deps.getUnits().add("org.eclipse.emf.validation");
			// q7Deps.getUnits().add("org.eclipse.emf.transaction");
			q7Deps.getUnits().add("org.eclipse.emf.ecore.change");
			q7Deps.getUnits().add("org.eclipse.emf.ecore.xmi");

			// q7Deps.getUnits().add("org.eclipse.core.filesystem");
			// q7Deps.getUnits().add("org.eclipse.core.filebuffers");
			// q7Deps.getUnits().add("org.eclipse.core.resources");
			// q7Deps.getUnits().add("org.eclipse.core.variables");
			// q7Deps.getUnits().add("org.eclipse.debug.core");
			// q7Deps.getUnits().add("org.eclipse.equinox.security");
		}

		injectionConfiguration.getEntries().add(q7Deps);
		// Add all from extra
		for (URI extraURI : q7Info.extra) {
			if (monitor.isCanceled()) {
				return null;
			}
			UpdateSite site = InjectionFactory.eINSTANCE.createUpdateSite();
			site.setUri(extraURI.toString());
			site.setAllUnits(true);
			injectionConfiguration.getEntries().add(site);
		}

		return injectionConfiguration;
	}

	@SuppressWarnings("unused")
	private static List<String> collectQ7InstallIDs(IProgressMonitor monitor,
			boolean hasGEF, boolean hasGMF, IMetadataRepository repository) {
		IQueryResult<IInstallableUnit> result = repository.query(
				QueryUtil.ALL_UNITS, monitor);
		List<String> q7Units = new ArrayList<String>();
		for (IInstallableUnit unit : result.toSet()) {
			if (hasProperty(unit, P2_GROUP_FEATURE, "true")) {
				continue;
			}
			if (hasProperty(unit, P2_CATEGORY_FEATURE, "true")) {
				continue;
			}

			// Skip gef/ gmf if not pressent
			String unitId = unit.getId();
			if (!hasGEF) {
				if (unitId.contains(GEF)) {
					continue;
				}
				if (unitId.contains(DRAW2D)) {
					continue;
				}
				if (unitId.contains(GMF)) {
					continue;
				}
			}
			if (!hasGMF) {
				if (unitId.contains(GMF)) {
					continue;
				}
			}
			q7Units.add(unitId);
		}
		return q7Units;
	}

	public static void logError(TargetPlatformHelper info) {
		Q7ExtLaunchingPlugin.log(new MultiStatus(PLUGIN_ID, 0, new IStatus[] { info.getStatus() },
				"Target platform initialization error", null));
	}

	public static Q7Info getInfo(ITargetPlatformHelper target, Map<String, Version> versions) throws CoreException {
		Map<String, Version> map = versions;
		MultiStatus status = new MultiStatus(PLUGIN_ID, 0, "Invalid eclipse target platform: " + target.toString(),
				null);

		Version platform = map.get(AUTInformation.VERSION);
		String platformName = map.get(AUTInformation.RAP) == null ? SWT_PLATFORM : RAP_PLATFORM;

		Version osgi = map.get(AUTInformation.OSGI);
		if (platform == null)
			status.add(createError("Failed to detect platform version"));
		if (osgi == null)
			status.add(createError("Failed to detect OSGI version"));
		if (!status.isOK())
			throw new CoreException(status);
		return collectQ7Information(platform, osgi, platformName);
	}

	public static Q7Info collectQ7Information(Version platform, Version osgi, String platformName) {
		Collection<Q7RuntimeInfo> updates = Q7UpdateSiteExtensions.getDefault().getRuntimes();
		URI q7 = null, aspectj = null, deps = null;
		Builder<URI> extra = ImmutableList.builder();
		// Initialize updates
		for (Q7RuntimeInfo q7RuntimeInfo : updates) {
			boolean platformValid = q7RuntimeInfo.version.isIncluded(platform);
			boolean osgiValid = q7RuntimeInfo.version.isIncluded(osgi);
			if (platformValid && q7RuntimeInfo.platform.equals(platformName)) {
				final String kind = q7RuntimeInfo.kind;
				if ("runtime".equals(kind)) {
					checkRuntimeInfo(platform, q7, "runtimes"); //$NON-NLS-1$
					q7 = q7RuntimeInfo.path;
				} else if ("dependency".equals(kind)) {
					checkRuntimeInfo(platform, deps, "dependencies"); //$NON-NLS-1$
					deps = q7RuntimeInfo.path;
				} else if ("extra".equals(kind)) {
					extra.add(q7RuntimeInfo.path);
				}

			}
			if (osgiValid) {
				if ("aspectj".equals(q7RuntimeInfo.kind)) {
					aspectj = q7RuntimeInfo.path;
				}
			}
		}
		if (q7 == null)
			throw new NullPointerException("Can't find runtime for platform " + platform);
		if (deps == null)
			throw new NullPointerException("Can't find dependencies for platform " + platform);
		if (aspectj == null)
			throw new NullPointerException("Can't find aspectj for osgi " + osgi);
		return new Q7Info(q7, aspectj, deps, extra.build());

	}

	private static void checkRuntimeInfo(Version platform, URI q7, String name) {
		if (q7 != null)
			throw new IllegalStateException("Multiple " + name + " providers for platform " + platform);
	}

	public static boolean hasProperty(IInstallableUnit unit, String prop,
			String value) {
		if (unit.getProperties().containsKey(prop)) {
			if (unit.getProperties().get(prop).equals(value)) {
				return true;
			}
		}
		return false;
	}

	public static InjectionConfiguration getAspectJInjection(Q7Info q7Info,
			IProgressMonitor progressMonitor) throws CoreException {
		InjectionConfiguration injectionConfiguration = InjectionFactory.eINSTANCE.createInjectionConfiguration();
		UpdateSite aspectsSite = InjectionFactory.eINSTANCE.createUpdateSite();
		aspectsSite.setUri(q7Info.aspectj.toString());
		injectionConfiguration.getEntries().add(aspectsSite);
		return injectionConfiguration;
	}
}
