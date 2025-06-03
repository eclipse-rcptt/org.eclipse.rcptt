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
package org.eclipse.rcptt.debug.runtime;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.rcptt.core.IContextProcessor;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.debug.Breakpoint;
import org.eclipse.rcptt.debug.BreakpointResource;
import org.eclipse.rcptt.debug.DebugContext;
import org.eclipse.rcptt.debug.DebugFactory;
import org.eclipse.rcptt.debug.Launch;
import org.eclipse.rcptt.debug.LaunchConfiguration;
import org.eclipse.rcptt.debug.LaunchType;
import org.eclipse.rcptt.debug.ListValue;
import org.eclipse.rcptt.debug.MapValue;
import org.eclipse.rcptt.debug.PrimitiveValue;
import org.eclipse.rcptt.debug.SetValue;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.tesla.ecl.impl.UIRunnable;
import org.eclipse.rcptt.tesla.internal.ui.player.UIJobCollector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

public class DebugContextProcessor implements IContextProcessor {

	public boolean isApplied(final Context context) {
		return context instanceof DebugContext;
	}

	// Caches configurations and launch types for quick access
	private class ContextHelper {
		private final Map<String, LaunchType> types = new HashMap<String, LaunchType>();
		private final Map<String, LaunchConfiguration> configurations = new HashMap<String, LaunchConfiguration>();
		private final List<Launch> runningLaunches = new ArrayList<Launch>();
		public final DebugPlugin debug = DebugPlugin.getDefault();
		public final ILaunchManager launchManager = debug.getLaunchManager();

		void setFrom(DebugContext context) {
			for (LaunchType type : context.getLaunchTypes()) {
				types.put(type.getId(), type);
				for (LaunchConfiguration conf : type.getConfigurations()) {
					conf.setTypeId(type.getId());
					configurations.put(conf.getName(), conf);
				}
			}
		}

		public List<Launch> captureLaunches() throws CoreException {
			ILaunch[] launches = launchManager.getLaunches();
			List<Launch> rv = new ArrayList<Launch>(launches.length);
			for (ILaunch launch : launches) {
				boolean terminated = true;
				for (IProcess process : launch.getProcesses()) {
					if (!process.isTerminated()) {
						terminated = false;
						break;
					}
				}
				if (terminated)
					continue;
				rv.add(captureLaunch(launch));
			}
			return rv;
		}

		private LaunchConfiguration getOrCapture(ILaunchConfiguration configuration) throws CoreException {
			LaunchConfiguration modelConfiguration = configurations.get(configuration.getName());
			if (modelConfiguration == null) {
				modelConfiguration = captureLaunchConfiguration(configuration);
			}
			return modelConfiguration;
		}

		private Launch captureLaunch(ILaunch launch) throws CoreException {

			if (launch.getLaunchConfiguration() == null) {
				throw new UnsupportedOperationException("Launches without configuration are not suppported");
			}
			LaunchConfiguration modelConfiguration = getOrCapture(launch.getLaunchConfiguration());
			LaunchType modelType = (LaunchType) modelConfiguration.eContainer();
			if (!modelType.getId().equals(launch.getLaunchConfiguration().getType().getIdentifier()))
				throw new IllegalArgumentException("Launch type differs from the one stored in model");
			LaunchConfiguration workingCopy = captureLaunchConfigurationRaw(launch.getLaunchConfiguration());
			removeEqualAttributes(modelConfiguration.getAttributes(), workingCopy.getAttributes());
			Launch rv = DebugFactory.eINSTANCE.createLaunch();
			rv.setConfiguration(modelConfiguration);
			assert rv.getConfiguration().getName() != null;
			rv.setConfigurationName(rv.getConfiguration().getName());
			rv.setMode(launch.getLaunchMode());
			rv.getConfigurationDelta().addAll(workingCopy.getAttributes());
			return rv;
		}

		public void applyLaunches(Collection<Launch> launches) throws CoreException {
			runningLaunches.clear();
			runningLaunches.addAll(captureLaunches());
			for (Launch modelLaunch : launches) {
				assert modelLaunch.getConfiguration().getName() != null : "Malfromed launch";
				applyLaunch(modelLaunch);
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void applyLaunch(Launch modelLaunch) throws CoreException {
			ILaunchConfiguration configuration = launchConfigurationByName(modelLaunch.getConfiguration().getName());
			for (Launch running : runningLaunches) {
				EqualityHelper helper = new EqualityHelper();
				if (helper.equals(running, modelLaunch))
					return;
			}
			ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
			Map attributes = workingCopy.getAttributes();
			for (NamedElement e : modelLaunch.getConfigurationDelta()) {
				attributes.put(e.getName(), unbox(e));
			}
			workingCopy.setAttributes(attributes);
			workingCopy.launch(modelLaunch.getMode(), new NullProgressMonitor());
		}

		private LaunchType getOrCapture(ILaunchConfigurationType type) {
			LaunchType rv = types.get(type.getIdentifier());
			if (rv == null)
				rv = captureLaunchType(type);
			return rv;

		}

		private LaunchType captureLaunchType(ILaunchConfigurationType type) {
			// WARN: using global cache
			LaunchType etype = DebugFactory.eINSTANCE.createLaunchType();
			etype.setId(type.getIdentifier());
			etype.setName(type.getName());
			Image image = DebugUITools.getImage(type.getIdentifier());
			etype.setImage(captureImage(image));
			types.put(etype.getId(), etype);
			return etype;
		}

		private LaunchConfiguration captureLaunchConfigurationRaw(ILaunchConfiguration configuration)
				throws CoreException {
			LaunchConfiguration rv = DebugFactory.eINSTANCE.createLaunchConfiguration();
			rv.setName(configuration.getName());
			rv.setTypeId(configuration.getType().getIdentifier());
			Map<?, ?> capturedAttributes = configuration.getAttributes();
			for (Map.Entry<?, ?> e : capturedAttributes.entrySet()) {
				String name = (String) e.getKey();
				Object value = e.getValue();
				rv.getAttributes().add(box(name, value));
			}
			return rv;
		}

		private LaunchConfiguration captureLaunchConfiguration(ILaunchConfiguration configuration)
				throws CoreException {
			LaunchConfiguration rv = captureLaunchConfigurationRaw(configuration);
			getOrCapture(configuration.getType()).getConfigurations().add(rv);
			configurations.put(rv.getName(), rv);
			return rv;
		}

		public void saveTo(DebugContext context) {
			context.getLaunchTypes().addAll(types.values());
		}
	}

	@Override
	public void apply(final Context ctx, BooleanSupplier isCancelled) throws CoreException {

		if (!(ctx instanceof DebugContext)) {
			return;
		}
		final UIJobCollector collector = new UIJobCollector();
		Job.getJobManager().addJobChangeListener(collector);
		try {
			UIRunnable.exec(new UIRunnable<Object>() {
				@Override
				public Object run() throws CoreException {
					collector.enable();
					return null;
				}
			});

			final DebugContext context = (DebugContext) ctx;

			if (context.isNoBreakpoints()) {
				cleanBreakPoints();
			}

			if (context.isNoLaunches()) {
				cleanLaunches(context.getLaunchExceptions());
			}
			if (context.isNoLaunchShortcuts()) {
				cleanLaunchShortcuts(context.getLaunchShortcutExceptions());
			}
			for (LaunchType modelType : context.getLaunchTypes())
				for (LaunchConfiguration modelConf : modelType.getConfigurations())
					assert modelConf.getName() != null : "Malfromed launch";
			for (Launch modelLaunch : context.getLaunches()) {
				assert modelLaunch.getConfiguration().getName() != null : "Malfromed launch";
			}

			applyLaunchConfigurations(context.getLaunchTypes());
			applyBreakpoints(context.getBreakpointResources());
			UIRunnable.exec(new UIRunnable<Object>() {
				@Override
				public Object run() throws CoreException {

					collector.setNeedDisable();
					return null;
				}
			});
			collector.join(TeslaLimits.getContextJoinTimeout());
			ContextHelper helper = new ContextHelper();
			helper.setFrom(context);
			helper.applyLaunches(context.getLaunches());
		} catch (InterruptedException e) {
			throw new CoreException(RcpttPlugin.createStatus("Launch context was interrupted", e));
		} finally {
			Job.getJobManager().removeJobChangeListener(collector);
		}
	}

	private void applyLaunchConfigurations(EList<LaunchType> launchTypes) throws CoreException {
		for (LaunchType modelType : launchTypes) {
			ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurationType(modelType.getId());
			if (type == null)
				throw new CoreException(
						createError("Launch configuration type " + modelType.getId() + " is not installed."));
			for (LaunchConfiguration configuration : modelType.getConfigurations())
				applyLaunchConfiguration(type, configuration);
		}
	}

	private IStatus createError(String message) {
		return new Status(IStatus.ERROR, Q7DebugRuntime.PLUGIN_ID, message);
	}

	private void cleanLaunches(final String exceptions) throws CoreException, InterruptedException {
		final MultiStatus result = new MultiStatus(Q7DebugRuntime.PLUGIN_ID, 0, "Failed to remove launches", null);
		Thread t = new Thread(new Runnable() {
			public void run() {
				Patterns patterns = new Patterns();
				patterns.setFromCommaSeparated(exceptions);
				final ILaunchManager launches = DebugPlugin.getDefault().getLaunchManager();
				for (final ILaunch launch : launches.getLaunches()) {
					String name = "";
					if (launch.getLaunchConfiguration() != null) {
						name = launch.getLaunchConfiguration().getName();
						if (patterns.matches(name))
							continue;
					}
					try {
						for (int i = 0; i < 3; i++) {
							if (launch.isTerminated()) {
								break;
							}
							try {
								launch.terminate();
							} catch (Exception e) {
								result.add(new Status(Status.ERROR, Q7DebugRuntime.PLUGIN_ID,
										"Failed to terminate " + name, e));
							}
						}
					} finally {
						launches.removeLaunch(launch);
					}
				}
			}
		}, "Debug Context: Clean launches");
		t.start();
		t.join(TeslaLimits.getContextJoinTimeout());
		if (!result.isOK()) {
			throw new CoreException(result);
		}
	}

	static class Patterns {
		ArrayList<Pattern> payload = new ArrayList<Pattern>();

		static public Pattern compile(String simplePattern) {
			return Pattern.compile(simplePattern.replace('\\', '/').replace("*", ".*").trim());
		}

		void setFromCommaSeparated(String commaSeparatedPatterns) {
			if (commaSeparatedPatterns == null)
				return;
			for (String simplePattern : commaSeparatedPatterns.split(",")) {
				payload.add(compile(simplePattern));
			}
		}

		boolean matches(String data) {
			for (Pattern pattern : payload) {
				if (pattern.matcher(data).matches())
					return true;
			}
			return false;
		}
	}

	@SuppressWarnings("unused")
	private static boolean isIgnored(String fileName, String[] ignoredPatterns) {
		if (ignoredPatterns == null) {
			return false;
		}
		for (String pattern : ignoredPatterns) {
			fileName = fileName.replace('\\', '/');
			String fileNameWithSlash = fileName + "/";
			if (fileName.matches(pattern) || fileNameWithSlash.matches(pattern)) {
				return true;
			}
		}
		return false;
	}

	private void cleanLaunchShortcuts(final String exceptions) throws CoreException {
		final Exception[] resultE = new Exception[] { null };
		Thread t = new Thread(new Runnable() {
			public void run() {
				Patterns exceptPatterns = new Patterns();
				exceptPatterns.setFromCommaSeparated(exceptions);
				final ILaunchManager launches = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfiguration[] configurations;
				try {
					configurations = launches.getLaunchConfigurations();
					for (ILaunchConfiguration cfg : configurations) {
						String name = cfg.getName();
						if (exceptPatterns.matches(name))
							continue;
						IFile file = cfg.getFile();
						// Delete only configuration is metadata.
						if (file == null) {
							cfg.delete();
						}
					}
				} catch (CoreException e1) {
					resultE[0] = e1;
				}
			}
		}, "Debug Context: Clean launch shortcuts");
		t.start();
		try {
			t.join(TeslaLimits.getContextJoinTimeout());
		} catch (InterruptedException e) {
			RcpttPlugin.log(e);
		}
		if (resultE[0] != null) {
			throw new CoreException(
					new Status(Status.ERROR, Q7DebugRuntime.PLUGIN_ID, resultE[0].getMessage(), resultE[0]));
		}
	}

	private void cleanBreakPoints() throws CoreException {
		final Exception[] resultE = new Exception[] { null };
		Thread t = new Thread(new Runnable() {
			public void run() {
				final IBreakpointManager breakpoints = DebugPlugin.getDefault().getBreakpointManager();
				try {
					breakpoints.removeBreakpoints(breakpoints.getBreakpoints(), true);
				} catch (Exception e) {
					resultE[0] = e;
				}
			}
		}, "Debug Context: Clean breakpoints");
		t.start();
		try {
			t.join(TeslaLimits.getContextJoinTimeout());
		} catch (InterruptedException e) {
			RcpttPlugin.log(e);
		}
		if (resultE[0] != null) {
			throw new CoreException(
					new Status(Status.ERROR, Q7DebugRuntime.PLUGIN_ID, resultE[0].getMessage(), resultE[0]));
		}
	}

	public Context create(EObject param) throws CoreException {
		return UIRunnable.exec(new UIRunnable<DebugContext>() {
			@Override
			public DebugContext run() throws CoreException {
				return doCreate();
			}
		});
	}

	static private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	private NamedElement box(String name, Object value) throws CoreException {
		if (value instanceof Map) {
			MapValue rv = DebugFactory.eINSTANCE.createMapValue();
			rv.setName(name);
			for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
				rv.getEntries().add(box((String) e.getKey(), e.getValue()));
			}
			return rv;
		} else if (value instanceof List) {
			ListValue rv = DebugFactory.eINSTANCE.createListValue();
			rv.setName(name);
			for (Object entry : (List<?>) value) {
				rv.getEntries().add(box("", entry));
			}
			return rv;
		} else if (value instanceof Set) {
			SetValue rv = DebugFactory.eINSTANCE.createSetValue();
			rv.setName(name);
			for (Object entry : (Set<?>) value) {
				rv.getEntries().add(box("", entry));
			}
			return rv;
		} else {
			PrimitiveValue rv = DebugFactory.eINSTANCE.createPrimitiveValue();
			rv.setName(name);
			rv.setValue(BoxedValues.box(value));
			return rv;
		}
	}

	private Object unbox(NamedElement value) throws CoreException {
		if (value instanceof MapValue) {
			// Addtional unboxing is required for EMap as it implements List,
			// not Map
			Map<String, String> rv = new HashMap<String, String>();
			for (NamedElement e : ((MapValue) value).getEntries())
				rv.put(e.getName(), (String) unbox(e));
			return rv;
		} else if (value instanceof SetValue) {
			Set<String> rv = new HashSet<String>();
			for (NamedElement e : ((SetValue) value).getEntries())
				rv.add((String) unbox(e));
			return rv;
		} else if (value instanceof ListValue) {
			ArrayList<String> rv = new ArrayList<String>();
			for (NamedElement e : ((ListValue) value).getEntries())
				rv.add((String) unbox(e));
			return rv;
		} else if (value instanceof PrimitiveValue) {
			Object rv = BoxedValues.unbox(((PrimitiveValue) value).getValue());
			if (rv instanceof String) {
				rv = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution((String) rv,
						false);
			}
			return rv;
		}
		throw new IllegalArgumentException("Can't unbox " + value.getClass().getName());
	}

	private void removeEqualAttributes(EList<NamedElement> pattern, EList<NamedElement> cleaned) {
		ArrayList<NamedElement> toRemove = new ArrayList<NamedElement>();
		HashMap<String, NamedElement> patternMap = new HashMap<String, NamedElement>(pattern.size());
		for (NamedElement a : pattern) {
			patternMap.put(a.getName(), a);
		}
		for (NamedElement e : cleaned) {
			NamedElement oldValue = patternMap.get(e.getName());
			EqualityHelper helper = new EqualityHelper();
			if (helper.equals(e, oldValue)) {
				toRemove.add(e);
			}
		}
		for (NamedElement key : toRemove) {
			cleaned.remove(key);
		}
	}

	private ILaunchConfiguration launchConfigurationByName(String name) throws CoreException {
		for (ILaunchConfiguration conf : getLaunchManager().getLaunchConfigurations()) {
			if (conf.getName().equals(name))
				return conf;
		}
		return null;
	}

	private static byte[] captureImage(Image image) {
		if (image == null) {
			return null;
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { image.getImageData() };
		loader.save(stream, SWT.IMAGE_PNG);
		return stream.toByteArray();
	}

	private void applyLaunchConfiguration(ILaunchConfigurationType type, LaunchConfiguration launch)
			throws CoreException {
		if (type == null)
			throw new NullPointerException("Null type");
		if (launch == null)
			throw new NullPointerException("Null launch");
		ILaunchManager manager = getLaunchManager();
		if (launch.getName() == null)
			throw new IllegalArgumentException("Null launch name");
		for (ILaunchConfiguration conf : manager.getLaunchConfigurations()) {
			if (conf.getName().equals(launch.getName()))
				conf.delete();
		}
		ILaunchConfigurationWorkingCopy conf = type.newInstance(null, launch.getName());

		Map<String, Object> attributes = new HashMap<String, Object>();
		for (NamedElement e : launch.getAttributes()) {
			attributes.put(e.getName(), unbox(e));
		}
		conf.setAttributes(attributes);
		conf.doSave();
	}

	private Breakpoint captureBreakpoint(IBreakpoint breakpoint) throws CoreException {
		// See org.eclipse.debug.ui.actions.ExportBreakpointsOperation.run() for
		// native implementation
		Breakpoint rv = DebugFactory.eINSTANCE.createBreakpoint();
		IMarker marker = breakpoint.getMarker();
		rv.setEnabled(breakpoint.isEnabled());
		rv.setRegistered(breakpoint.isRegistered());
		rv.setPersisted(breakpoint.isPersisted());
		rv.setType(marker.getType());
		// core.resources in Helios uses raw map for attributes
		for (Object entry : marker.getAttributes().entrySet()) {
			@SuppressWarnings("unchecked")
			Entry<String, Object> e = (Entry<String, Object>) entry;
			String key = e.getKey();
			Object value = e.getValue();
			rv.getAttributes().add(box(key, value));
		}
		IWorkingSetManager mgr = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet[] sets = mgr.getWorkingSets();
		for (int i = 0; i < sets.length; i++) {
			if (IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(sets[i].getId())
					&& containsBreakpoint(sets[i], breakpoint)) {
				rv.getWorkingSets().add(sets[i].getName());
			}
		}
		return rv;
	}

	@SuppressWarnings({ "restriction", "unused" })
	private void applyBreakpoints(List<BreakpointResource> list) throws CoreException {
		org.eclipse.debug.internal.core.BreakpointManager manager = (org.eclipse.debug.internal.core.BreakpointManager) DebugPlugin
				.getDefault().getBreakpointManager();
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		HashSet<String> pathSet = new HashSet<String>();
		for (BreakpointResource modelResource : list) {
			pathSet.add(modelResource.getPath());
		}
		for (IBreakpoint existing : manager.getBreakpoints()) {
			if (pathSet.contains(existing.getMarker().getResource().getFullPath().toPortableString()))
				existing.delete();
		}
		for (BreakpointResource modelResource : list) {
			IResource resource = workspace.findMember(modelResource.getPath());
			if (resource == null) {
				throw new IllegalArgumentException("Can't find resource " + modelResource.getPath());
			}
			for (Breakpoint eobject : modelResource.getBreakpoints()) {
				if (eobject.getType() == null)
					throw new NullPointerException("Breakpoint has null marker type");
				IMarker marker = resource.createMarker(eobject.getType());
				Map<String, Object> attributes = new HashMap<String, Object>();
				for (NamedElement e : eobject.getAttributes()) {
					marker.setAttribute(e.getName(), unbox(e));
				}
				IBreakpoint breakpoint = manager.createBreakpoint(marker);
				breakpoint.setEnabled(eobject.isEnabled());
				breakpoint.setPersisted(eobject.isPersisted());
				breakpoint.setRegistered(false); // Enforces registration on
													// next line
				breakpoint.setRegistered(eobject.isRegistered());
				// TODO: restore working sets
			}
		}
	}

	private boolean containsBreakpoint(IWorkingSet set, IBreakpoint breakpoint) {
		IAdaptable[] elements = set.getElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].equals(breakpoint)) {
				return true;
			}
		}
		return false;
	}

	private DebugContext doCreate() throws CoreException {
		DebugContext context = DebugFactory.eINSTANCE.createDebugContext();
		DebugPlugin debug = DebugPlugin.getDefault();
		ContextHelper helper = new ContextHelper();
		for (ILaunchConfiguration configuration : debug.getLaunchManager().getLaunchConfigurations()) {
			try {
				helper.captureLaunchConfiguration(configuration);
			} catch (Exception e) {
				RcpttPlugin.log(e);
			}
		}
		for (IBreakpoint breakpoint : debug.getBreakpointManager().getBreakpoints()) {
			BreakpointResource resource = null;
			// TODO: optimize search inefficiency
			for (BreakpointResource r : context.getBreakpointResources()) {
				if (r.getPath().equals(breakpoint.getMarker().getResource().getFullPath().toPortableString()))
					resource = r;
			}
			if (resource == null) {
				resource = DebugFactory.eINSTANCE.createBreakpointResource();
				resource.setPath(breakpoint.getMarker().getResource().getFullPath().toPortableString());
				context.getBreakpointResources().add(resource);
			}
			try {
				resource.getBreakpoints().add(captureBreakpoint(breakpoint));
			} catch (Exception e) {
				RcpttPlugin.log(e);
			}
		}
		context.getLaunches().addAll(helper.captureLaunches());
		helper.saveTo(context);
		return context;
	}

}
