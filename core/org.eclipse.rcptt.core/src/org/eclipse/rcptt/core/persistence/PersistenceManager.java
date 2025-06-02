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
package org.eclipse.rcptt.core.persistence;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLInfoImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.persistence.plain.IPlainTextPersistenceExtension;
import org.eclipse.rcptt.core.persistence.plain.PlainTextPersistenceExtensionManager;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;
import org.eclipse.rcptt.core.workspace.Q7Utils;
import org.eclipse.rcptt.core.workspace.WorkspaceSynchronizer;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.internal.core.Q7LazyResource;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.tesla.core.TeslaSerializationOptions;
import org.eclipse.rcptt.tesla.core.info.InfoPackage;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolPackage;
import org.eclipse.rcptt.tesla.core.protocol.diagram.DiagramPackage;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawPackage;
import org.eclipse.rcptt.tesla.core.protocol.raw.TeslaScenario;
import org.eclipse.rcptt.tesla.core.ui.UiPackage;
import org.eclipse.rcptt.tesla.recording.core.ecl.EclRecorder;
import org.eclipse.rcptt.util.FileUtil;

public class PersistenceManager implements IPlainConstants {
	public static final String CONTENT_ENTRY = ".q7.content";
	public static final String ECL_CONTENT_ENTRY = ".content";
	public static final String DESCRIPTION_ENTRY = ".description";
	public static final String TESLA_CONTENT_ENTRY = ".content.raw";

	private static PersistenceManager persistenceManager;
	private Map<Resource, IPersistenceModel> resourceOnlyModels = Collections
			.synchronizedMap(new HashMap<Resource, IPersistenceModel>());

	private PersistenceManager() {
		File root = RcpttPlugin.getDefault().getStateLocation()
				.append("attachments").toFile();
		FileUtil.deleteFiles(root.listFiles());
	}

	public synchronized static PersistenceManager getInstance() {
		if (persistenceManager == null) {
			persistenceManager = new PersistenceManager();
		}
		return persistenceManager;
	}

	public IPersistenceModel getModel(Resource element) {
		IFile file = WorkspaceSynchronizer.getFile(element);
		return getModel(detectFormatProxy(file), element);
	}

	public synchronized IPersistenceModel getModel(
			IPersistenceModelFactory factory, Resource element) {
		if (element == null) {
			return null;
		}
		if (element.getURI() == null) {
			return null;
		}
		IPersistenceModel model = resourceOnlyModels.get(element);
		if (model == null) {
			model = factory.createModel(element);
			replaceModel(element, model);
		}
		return model;
	}

	private void internalRemove(Resource element) {
		IPersistenceModel old = resourceOnlyModels.remove(element);
		if (old != null)
			old.dispose();
	}

	private void replaceModel(Resource element,
			IPersistenceModel newModel) {
		IPersistenceModel old = resourceOnlyModels.put(element, newModel);
		if (old != null)
			old.dispose();
	}

	private static Map<String, String> legacyNamespaces = new HashMap<String, String>();
	static {
		legacyNamespaces.put("http://com/xored/q7/scenario.ecore", ScenarioPackage.eNS_URI);
		legacyNamespaces.put("http:///com/xored/q7/scenario.ecore", ScenarioPackage.eNS_URI);

		legacyNamespaces.put("http://com/xored/q7/filesystem.ecore", "http://eclipse.org/rcptt/ctx/filesystem");
		legacyNamespaces.put("http://xored.com/q7/debug", "http://eclipse.org/rcptt/ctx/debug");
		legacyNamespaces.put("http:///com/xored/q7/workspace.ecore", "http://eclipse.org/rcptt/ctx/workspace");
		legacyNamespaces.put("http://com/xored/q7/parameters.ecore", "http://eclipse.org/rcptt/ctx/parameters");
		legacyNamespaces.put("http:///com/xored/q7/preferences.ecore", "http://eclipse.org/rcptt/ctx/preferences");
		legacyNamespaces.put("http:///com/xored/q7/ecl/context.ecore", "http://eclipse.org/rcptt/ctx/ecl");
		legacyNamespaces.put("http://com/xored/q7/verifications/log.ecore",
				"http://eclipse.org/rcptt/verifications/log");
		legacyNamespaces.put("http://com/xored/q7/verifications/text.ecore",
				"http://eclipse.org/rcptt/verifications/text");
		legacyNamespaces.put("http://com/xored/q7/verifications/time.ecore",
				"http://eclipse.org/rcptt/verifications/time");
		legacyNamespaces.put("http://com/xored/q7/verifications/tree.ecore",
				"http://eclipse.org/rcptt/verifications/tree");

		legacyNamespaces.put("http:///com/xored/tesla/core/protocol/raw.ecore", RawPackage.eNS_URI);
		legacyNamespaces.put("http:///com/xored/tesla/core/ui.ecore", UiPackage.eNS_URI);
		legacyNamespaces.put("http:///com/xored/tesla/core/protocol.ecore", ProtocolPackage.eNS_URI);
		legacyNamespaces.put("http:///com/xored/tesla/core/info.ecore", InfoPackage.eNS_URI);
		legacyNamespaces.put("http:///com/xored/tesla/core/diagram.ecore", DiagramPackage.eNS_URI);

	}

	public static Map<String, Object> getOptions() {
		Map<String, Object> options = new HashMap<String, Object>();

		EAttribute content = org.eclipse.rcptt.ecl.core.CorePackage.eINSTANCE
				.getScript_Content();
		XMLMapImpl map = new XMLMapImpl();
		XMLInfoImpl x = new XMLInfoImpl();
		x.setXMLRepresentation(XMLInfoImpl.ELEMENT);
		map.add(content, x);

		TeslaSerializationOptions.fillOptions(map);
		options.put(XMLResource.OPTION_XML_MAP, map);

		options.put(XMLResource.OPTION_ENCODING, ENCODING);
		options.put(XMLResource.OPTION_ESCAPE_USING_CDATA, Boolean.TRUE);
		options.put(XMLResource.OPTION_SKIP_ESCAPE, Boolean.FALSE);
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, new BasicExtendedMetaData() {
			@Override
			public EPackage getPackage(String namespace) {
				if (legacyNamespaces.containsKey(namespace)) {
					namespace = legacyNamespaces.get(namespace);
				}
				return super.getPackage(namespace);
			}
		});
		return options;
	}

	public void saveResource(Resource element) {
		saveResource(element, false);
	}

	public void saveResource(Resource element, boolean doConvert) {
		if (element == null) {
			RcpttPlugin.log("Null resources is passed to Q7 resource monitor",
					null);
			return;
		}
		IPersistenceModel model = getModel(element);

		updateScenarioContent(element, model);
		// Convert to new container if required
		Set<String> names = new HashSet<String>(Arrays.asList(model.getNames()));
		names.add(CONTENT_ENTRY);
		if (/* (names.size() > 1) && */!model.isSupportMultiItems()) {
			// convert to container with multiple items support
			model = replaceModelWith(element, model);
		}
		// Extract all items not created, accessed before
		try {
			model.extractAll();
			model.updateAttributes();

			if (model.isContentEntryRequired()) {
				OutputStream store = model.store(CONTENT_ENTRY);
				saveResourceWithUpdate(element, store);
				store.close();
			} else {
				model.delete(CONTENT_ENTRY);
			}

			File file = model.storeToTemporaty();
			IFile location = Q7Utils.getLocation(element);
			BufferedInputStream stream = new BufferedInputStream(
					new FileInputStream(file));
			if (location.exists()) {
				location.setContents(stream, true, true,
						new NullProgressMonitor());
			} else {
				location.create(stream, true, new NullProgressMonitor());
			}
			stream.close();
			file.delete();
			model.setUnmodified();
		} catch (Throwable e) {
			RcpttPlugin.log(e);
		}
	}

	public IPersistenceModel replaceModelWith(Resource element,
			IPersistenceModel model) {
		IPersistenceModelFactory defaultFactory = PersistenceFactoryManager
				.getInstance().getDefaultFactory();
		IPersistenceModel modelCopy = defaultFactory.createModel(element);
		modelCopy.copyFrom(model);
		model.dispose();
		replaceModel(element, modelCopy);
		model = modelCopy;
		return model;
	}

	private void saveResourceWithUpdate(Resource element, OutputStream store) {
		try {
			XMIResourceImpl tempResource = new XMIResourceImpl(element.getURI());
			EList<EObject> contents = element.getContents();
			Collection<EObject> copyAll = EcoreUtil.copyAll(contents);
			tempResource.getContents().addAll(copyAll);
			for (EObject eObject : copyAll) {
				if (eObject instanceof Scenario) {
					((Scenario) eObject).setContent(null);
					((Scenario) eObject).setTeslaContent(null);
				}
				if (eObject instanceof NamedElement) {
					((NamedElement) eObject).setDescription(null);
				}
				IPlainTextPersistenceExtension[] extensions = PlainTextPersistenceExtensionManager
						.getInstance().getExtensions();
				for (IPlainTextPersistenceExtension ext : extensions) {
					ext.performObjectPreSaveUpdate(eObject);
				}
			}
			tempResource.save(store, getOptions());
		} catch (IOException e) {
			RcpttPlugin.log(e);
		}
	}

	/**
	 * Store scenario content/teslaContent in separate resources
	 *
	 * @param element
	 * @param model
	 */
	public void updateScenarioContent(Resource element, IPersistenceModel model) {
		EList<EObject> contents = element.getContents();
		Scenario sc = null;
		NamedElement ne = null;
		if (contents.size() == 1) {
			EObject val0 = contents.get(0);
			if (val0 instanceof Scenario) {
				sc = (Scenario) val0;
			}
			if (val0 instanceof NamedElement) {
				ne = (NamedElement) val0;
			}
		}
		if (ne != null) {
			String description = ne.getDescription();
			if (description != null) {
				// sc.setContent(null);
				storeEObjectIn(model.store(DESCRIPTION_ENTRY), description);
			} else {
				model.delete(DESCRIPTION_ENTRY);
			}
		}
		if (sc != null) {
			EObject teslaContent = sc.getTeslaContent();
			EObject content = sc.getContent();
			// Switch old format
			if (content instanceof TeslaScenario && teslaContent == null) {
				teslaContent = content;
				content = null;
			}

			if (teslaContent instanceof TeslaScenario && content == null) {
				Script script = CoreFactory.eINSTANCE.createScript();
				script.setContent(new EclRecorder()
						.generateCode((TeslaScenario) teslaContent));
				content = script;
			}
			teslaContent = null;
			model.delete(TESLA_CONTENT_ENTRY);

			if (content != null) {
				// sc.setContent(null);
				storeEObjectIn(model.store(ECL_CONTENT_ENTRY), content);
			} else {
				model.delete(ECL_CONTENT_ENTRY);
			}

		}
	}

	private void storeEObjectIn(OutputStream store, Object content) {
		if (content instanceof String) {
			String text = (String) content;
			try {
				OutputStreamWriter wr = new OutputStreamWriter(store, ENCODING);
				wr.write(text);
				wr.close();
			} catch (IOException e) {
				RcpttPlugin.log(e);
			}
			FileUtil.safeClose(store);
			return;
		} else if (content instanceof Script) {
			String text = ((Script) content).getContent();
			try {
				OutputStreamWriter wr = new OutputStreamWriter(store, ENCODING);
				wr.write(text);
				wr.close();
			} catch (IOException e) {
				RcpttPlugin.log(e);
			}
			FileUtil.safeClose(store);
			return;
		} else if (content instanceof EObject) { // Store teslaContent
			ResourceImpl resource = new XMIResourceImpl();
			resource.getContents().add(EcoreUtil.copy((EObject) content));
			try {
				resource.save(store, getOptions());
				FileUtil.safeClose(store);
			} catch (IOException e) {
				RcpttPlugin.log(e);
			}
		}
	}

	public String loadDescription(Q7LazyResource q7LazyResource, EObject self) {
		IPersistenceModel model = getModel(q7LazyResource);
		if (model != null) {
			InputStream stream = model.read(DESCRIPTION_ENTRY);
			if (stream != null) {
				try {
					String content = new String(
							FileUtil.getStreamContent(stream), ENCODING);
					if (content != null) {
						return content;
					}
				} catch (IOException e) {
					RcpttPlugin.log(e);
				}
			}
		}
		return null;
	}

	public EObject loadECLContent(Q7LazyResource q7LazyResource, EObject self) {
		IPersistenceModel model = getModel(q7LazyResource);
		if (model != null) {
			InputStream stream = model.read(ECL_CONTENT_ENTRY);
			if (stream != null) {
				try {
					String content = new String(
							FileUtil.getStreamContent(stream), ENCODING);
					if (content != null) {
						Script script = CoreFactory.eINSTANCE.createScript();
						script.setContent(content);
						return script;
					}
				} catch (IOException e) {
					RcpttPlugin.log(e);
				}
			}
		}
		return null;
	}

	public EObject loadTeslaContent(Q7LazyResource q7LazyResource, EObject self) {
		IPersistenceModel model = getModel(q7LazyResource);
		if (model != null) {
			try (InputStream stream = model.read(TESLA_CONTENT_ENTRY)) {
				if (stream != null) {
					XMIResourceImpl res = new XMIResourceImpl();
					res.load(stream, getOptions());
					if (res.getContents().size() == 1) {
						return res.getContents().get(0);
					}
				}
			} catch (IOException e) {
				RcpttPlugin.log(String.format("Error reading content for %s",
						q7LazyResource.getURI()), e);
			}
		}
		return null;
	}

	public InputStream loadMetadata(IFile file, Resource resource) {
		IPersistenceModel model = getModel(detectFormatProxy(file), resource);
		return loadMetadata(model);

	}

	public InputStream loadMetadata(IPersistenceModel model) {
		return model.read(CONTENT_ENTRY);

	}

	private IPersistenceModelFactory detectFormatProxy(final IFile file) {
		return new IPersistenceModelFactory() {

			public IPersistenceModel createModel(Resource resource) {
				return detectFormat(file, resource).createModel(resource);
			}

			public boolean isSupported(InputStream stream) {
				return false;
			}

			public boolean isSupported(File file) {
				return false;
			}
		};
	}

	private IPersistenceModelFactory detectFormat(IFile file, Resource resource) {
		IPersistenceModelFactory[] factories = PersistenceFactoryManager
				.getInstance().getFactories();
		if (!file.exists() || !file.isSynchronized(IResource.DEPTH_INFINITE)) {
			return PersistenceFactoryManager.getInstance().getDefaultFactory();
		}
		for (IPersistenceModelFactory factory : factories) {
			InputStream contents = null;
			try {
				contents = file.getContents();
				IPath loc = file.getLocation();
				if (loc != null && loc.toFile().exists()) {
					if (factory.isSupported(loc.toFile())) {
						return factory;
					}
				}
				if (factory.isSupported(contents)) {
					return factory;
				}
			} catch (CoreException e) {
				RcpttPlugin.log(e);
			} finally {
				FileUtil.safeClose(contents);
			}
		}
		return PersistenceFactoryManager.getInstance().getDefaultFactory();
	}

	public IPersistenceModelFactory detectFormat(byte[] file) {
		IPersistenceModelFactory[] factories = PersistenceFactoryManager
				.getInstance().getFactories();
		for (IPersistenceModelFactory factory : factories) {
			if (factory.isSupported(new ByteArrayInputStream(file))) {
				return factory;
			}
		}
		return PersistenceFactoryManager.getInstance().getDefaultFactory();
	}

	public synchronized static void shutdown() {
		if (persistenceManager != null) {
			persistenceManager.shutdownInternal();
			persistenceManager = null;
		}
	}

	private synchronized void shutdownInternal() {
		for (IPersistenceModel model : resourceOnlyModels.values()) {
			model.dispose();
		}
	}

	public void remove(Resource resource) {
		internalRemove(resource);
	}

	public IPersistenceModel getModel(byte[] content, Resource res) {
		IPersistenceModelFactory format = PersistenceManager.getInstance()
				.detectFormat(content);
		if (format == null) {
			return null;
		}
		IPersistenceModel model = format.createModel(res);
		if (model instanceof BasePersistenceModel) {
			((BasePersistenceModel) model).setInternalContent(content);
			replaceModel(res, model);

			return model;
		}
		return null;
	}

	public int cachedSize(Resource res) {
		IFile file = WorkspaceSynchronizer.getFile(res);
		if (!file.exists() || !file.isSynchronized(IResource.DEPTH_INFINITE)) {
			return 0;
		}
		File fs = file.getLocation().toFile();
		return (int) Math.min(fs.length(), Integer.MAX_VALUE);
	}
}
