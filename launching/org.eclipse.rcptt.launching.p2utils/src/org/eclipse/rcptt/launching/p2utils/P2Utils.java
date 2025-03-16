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
package org.eclipse.rcptt.launching.p2utils;

import static org.eclipse.rcptt.launching.p2utils.Q7P2UtilsActivator.PLUGIN_ID;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository;
import org.eclipse.equinox.internal.p2.director.PermissiveSlicer;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.Util;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.actions.CollectAction;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.internal.repository.mirroring.Mirroring;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRequest;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.rcptt.launching.injection.UpdateSite;
import org.eclipse.rcptt.util.FileUtil;

@SuppressWarnings("restriction")
public class P2Utils {
	public static interface ILogMonitor {
		void log(String message);
	}

	/**
	 * Return PDE target platform sercice.
	 * 
	 * @return
	 */
	public static ITargetPlatformService getTargetService() {
		Class<ITargetPlatformService> serviceClass = ITargetPlatformService.class;

		return getPDECoreService(serviceClass, serviceClass.getName());
	}

	private static <T> T getPDECoreService(Class<T> serviceClass, String serviceName) {
		PDECore pdeCore = PDECore.getDefault();
		try {
			Method strMethod = pdeCore.getClass().getMethod("acquireService", String.class);
			if (strMethod != null) {
				return (T) strMethod.invoke(pdeCore, serviceName);
			}

		} catch (Throwable ex) {
			// Q7P2UtilsActivator.log(new Status(Status.ERROR, Q7P2UtilsActivator.PLUGIN_ID,
			// ex.getMessage(), ex));
		}
		try {
			Method classMethod = pdeCore.getClass().getMethod("acquireService", Class.class);

			if (classMethod != null) {
				return (T) classMethod.invoke(pdeCore, serviceClass);
			}
		} catch (Throwable ex) {
			// Q7P2UtilsActivator.log(new Status(Status.ERROR, Q7P2UtilsActivator.PLUGIN_ID,
			// ex.getMessage(), ex));
		}
		return null;
	}

	/**
	 * Return P2 metadata repository manager.
	 * 
	 * @return
	 * @throws CoreException
	 */
	public static IMetadataRepositoryManager getRepositoryManager() throws CoreException {
		return getRepositoryManager(getProvisioningAgent());
	}

	public static IProvisioningAgent getProvisioningAgent() {
		return getPDECoreService(IProvisioningAgent.class, IProvisioningAgent.SERVICE_NAME);
	}

	/**
	 * Return P2 metadata repository manager.
	 * 
	 * @return
	 * @throws CoreException
	 */
	public static IMetadataRepositoryManager getRepositoryManager(IProvisioningAgent agent) throws CoreException {
		if (agent == null) {
			throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID,
					"Failed to locate repository. Can't get service " + IProvisioningAgent.SERVICE_NAME));
		}
		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) agent
				.getService(IMetadataRepositoryManager.SERVICE_NAME);
		if (manager == null) {
			throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID,
					"Failed to locate repository. No metadata repository manager found."));
		}
		return manager;
	}

	public static IArtifactRepositoryManager getArtifactRepositoryManager() throws CoreException {
		return getArtifactRepositoryManager(getProvisioningAgent());
	}

	public static IArtifactRepositoryManager getArtifactRepositoryManager(IProvisioningAgent agent)
			throws CoreException {

		if (agent == null)
			throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID,
					"Failed to locate repository. Can't get service " + IProvisioningAgent.SERVICE_NAME));
		IArtifactRepositoryManager manager = (IArtifactRepositoryManager) agent
				.getService(IArtifactRepositoryManager.SERVICE_NAME);
		if (manager == null) {
			throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID,
					"Failed to locate repository. No metadata repository manager found."));
		}
		return manager;
	}

	public static IMetadataRepository safeLoadRepository(URI uri, IProgressMonitor monitor, IProvisioningAgent agent)
			throws CoreException {
		try {
			// Be sure to read freash repository each time
			IMetadataRepositoryManager rm = getRepositoryManager(agent);
			rm.removeRepository(uri);
			try {
				rm.refreshRepository(uri, monitor);
			} catch (ProvisionException e) {
				// Ignore, this is ok, need to clean badRepo variable.
			}
			return rm.loadRepository(uri, monitor);
		} catch (RuntimeException e) {
			if (e instanceof OperationCanceledException || monitor.isCanceled()) {
				throw new CoreException(
						new Status(IStatus.CANCEL, Q7P2UtilsActivator.PLUGIN_ID, "Repository load cancelled", e));
			} else {
				throw e;
			}
		}
	}

	public static IArtifactRepository safeLoadArtifactRepository(URI uri, IProgressMonitor monitor,
			IProvisioningAgent agent) {
		try {
			// Be sure to read freash repository each time
			IArtifactRepositoryManager rm = getArtifactRepositoryManager(agent);
			rm.removeRepository(uri);
			try {
				rm.refreshRepository(uri, monitor);
			} catch (ProvisionException e) {
				// Ignore, this is ok, need to clean badRepo variable.
			}
			return rm.loadRepository(uri, monitor);
		} catch (Throwable e) {
			if (e instanceof OperationCanceledException || monitor.isCanceled()) {
				return null;
			}
			Q7P2UtilsActivator.getDefault().log(e);
		}
		return null;
	}

	public static Set<String> mapUnitsToId(Set<IInstallableUnit> availableUnits) {
		Set<String> unitIDs = new HashSet<String>();
		for (IInstallableUnit u : availableUnits) {
			unitIDs.add(u.getId());
		}
		return unitIDs;
	}

	public static List<IQuery<IInstallableUnit>> mapUnitsToQuery(Set<String> units) {
		List<IQuery<IInstallableUnit>> queries = new ArrayList<IQuery<IInstallableUnit>>();
		for (String id : units) {
			queries.add(QueryUtil.createIUQuery(id));
		}
		return queries;
	}

	public static List<IQuery<IInstallableUnit>> mapUnitsToCategoryQuery(Collection<IInstallableUnit> units) {
		List<IQuery<IInstallableUnit>> queries = new ArrayList<IQuery<IInstallableUnit>>();
		for (IInstallableUnit unit : units) {
			Collection<IRequirement> requirements = unit.getRequirements();
			for (IRequirement req : requirements) {
				queries.add(QueryUtil.createMatchQuery(req.getMatches(), new Object[] {}));
			}
		}
		return queries;
	}

	public static IQuery<IInstallableUnit> createQuery(UpdateSite site) {
		IQuery<IInstallableUnit> finalQuery = null;
		if (site.isAllUnits()) {
			finalQuery = QueryUtil.ALL_UNITS;
		} else {
			List<IQuery<IInstallableUnit>> items = P2Utils.mapUnitsToQuery(new HashSet<String>(site.getUnits()));
			finalQuery = QueryUtil.createLatestQuery(QueryUtil.createCompoundQuery(items, false));
		}
		return finalQuery;
	}

	public static IStatus validateBundle(File file) {
		if (!file.exists()) {
			return new Status(IStatus.ERROR, PLUGIN_ID, "File " + file + " does not exist");
		}
		if (file.isDirectory()) {
			return Status.OK_STATUS;
		}
		/**
		 * Validate file consistency check if this is valid zip archive if it is jar
		 */
		if (file.getName().toLowerCase().endsWith(".jar")) {
			BufferedInputStream stream = null;
			ZipInputStream zin = null;
			try {
				stream = new BufferedInputStream(new FileInputStream(file));
				zin = new ZipInputStream(stream);
				Set<String> names = new HashSet<String>();
				while (true) {
					ZipEntry entry = zin.getNextEntry();
					if (entry == null) {
						if (names.size() == 0) {
							return new Status(IStatus.ERROR, PLUGIN_ID, "Empty ZIP archive " + file);
						}
						return Status.OK_STATUS;
					}
					names.add(entry.getName());
				}
			} catch (Throwable e) {
				return new Status(IStatus.ERROR, PLUGIN_ID, "Failed to check " + file, e);
			} finally {
				FileUtil.safeClose(zin);
				FileUtil.safeClose(stream);
			}
		}
		return Status.OK_STATUS;
	}

	public static void installUnits(IProgressMonitor monitor, IArtifactRepository artifactRepository,
			IFileArtifactRepository filesRepository, List<IInstallableUnit> toInstall, int installTryCount,
			ILogMonitor logMonitor, boolean useRaw, IProvisioningAgent agent) throws CoreException {
		if (logMonitor != null) {
			logMonitor.log("Downloading artifacts to: " + filesRepository.getLocation().toString());
		}

		filesRepository.setProperty(SimpleArtifactRepository.PROP_FORCE_THREADING, "true");

		while (toInstall.size() > 0) {
			List<IArtifactKey> keys = new ArrayList<IArtifactKey>();

			for (IInstallableUnit unit : toInstall) {
				keys.addAll(unit.getArtifacts());
			}
			List<IArtifactRequest> requests = new ArrayList<IArtifactRequest>();
			if (useRaw) {
				rawMirror(artifactRepository, filesRepository, logMonitor, agent, keys);
			} else {
				// Collect requests
				for (IInstallableUnit unit : toInstall) {
					Collection<IArtifactKey> toDownload = unit.getArtifacts();
					for (IArtifactKey key : toDownload) {
						Map<String, String> repositoryProperties = CollectAction
								.createArtifactDescriptorProperties(unit);
						requests.add(Util.getArtifactRepositoryManager(agent).createMirrorRequest(key, filesRepository,
								null, repositoryProperties));
					}
				}
				if (requests.size() == 0) {
					break;// All is OK.
				}
				artifactRepository.getArtifacts(
						(IArtifactRequest[]) requests.toArray(new IArtifactRequest[requests.size()]), monitor);
			}

			MultiStatus rv = new MultiStatus(PLUGIN_ID, 0, "Failed to install bundles", null);
			// Check and validate installed units
			List<IInstallableUnit> installedOK = new ArrayList<IInstallableUnit>();
			for (IInstallableUnit unit : toInstall) {
				Collection<IArtifactKey> toDownload = unit.getArtifacts();

				if (toDownload == null || toDownload.size() == 0) {
					// This is most probable feature.
					installedOK.add(unit);
					continue;
				}

				for (IArtifactKey key : toDownload) {
					File file = filesRepository.getArtifactFile(key);
					if (file != null && file.exists()) {
						IStatus result = P2Utils.validateBundle(file);
						if (result.isOK()) {
							installedOK.add(unit);
							continue;
						} else {
							rv.add(result);
						}
					}
					// Remove key, since it is not valid
					filesRepository.removeDescriptor(key, monitor);
				}
			}
			toInstall.removeAll(installedOK);
			if (toInstall.size() == 0) {
				return;
			}
			installTryCount--;
			if (installTryCount < 0) {
				throw new CoreException(rv);
			}
			if (logMonitor != null) {
				logMonitor.log("Artifacts left on iteration: " + toInstall.size() + " retrying: " + installTryCount
						+ "delay: 5 seconds");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					throw new CoreException(Status.CANCEL_STATUS);
				}
			}
		}
	}

	private static void rawMirror(IArtifactRepository artifactRepository, IFileArtifactRepository filesRepository,
			ILogMonitor logMonitor, IProvisioningAgent agent, List<IArtifactKey> keys) {
		Mirroring mirror = new Mirroring(artifactRepository, filesRepository, true);
		mirror.setCompare(false);
		mirror.setComparatorId(null);
		mirror.setBaseline(null);
		mirror.setValidate(false);
		mirror.setCompareExclusions(null);
		mirror.setTransport((Transport) agent.getService(Transport.SERVICE_NAME));

		// If IUs have been specified then only they should be mirrored,
		// otherwise mirror everything.
		if (keys.size() > 0) {
			mirror.setArtifactKeys(keys.toArray(new IArtifactKey[keys.size()]));
		}

		IStatus result = mirror.run(false, false);
		if (result != null && !result.isOK() && logMonitor != null) {
			logMonitor.log("Mirroring is failed: " + result.getMessage());
		}
	}

	public static IQuery<IInstallableUnit> createContainmentQuery(Set<IInstallableUnit> availableUnits) {

		return QueryUtil.createCompoundQuery(mapUnitsToCategoryQuery(availableUnits), false);
	}

	public static void expandFeatures(IProgressMonitor monitor, IMetadataRepository repository,
			Set<IInstallableUnit> availableUnits) {
		PermissiveSlicer slicer = new PermissiveSlicer(repository, new Hashtable<String, String>(), false, // include
																											// optional
				// depenencies
				true, // is everything greedy
				true, // forceFilterTo,
				true, // considerStrictDependencyOnly,
				false); // slicingOptions.followOnlyFilteredRequirements());

		IQueryable<IInstallableUnit> slice = slicer
				.slice(availableUnits, monitor);
		availableUnits.addAll(slice.query(QueryUtil.ALL_UNITS, monitor).toSet());
	}
}
