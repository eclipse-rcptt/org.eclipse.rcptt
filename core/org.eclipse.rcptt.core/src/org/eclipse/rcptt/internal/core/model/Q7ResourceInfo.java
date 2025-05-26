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
package org.eclipse.rcptt.internal.core.model;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.Q7Status;
import org.eclipse.rcptt.core.model.Q7Status.Q7StatusCode;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.persistence.LeakDetector;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.persistence.plain.PlainTextPersistenceModel;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.internal.core.Q7LazyResource;
import org.eclipse.rcptt.internal.core.model.cache.ILRUCacheable;

import com.google.common.base.Preconditions;

public class Q7ResourceInfo extends OpenableElementInfo implements ILRUCacheable, Closeable {
	private final Resource resource;
	private NamedElement element;
	public long timestamp;
	private final String plainStoreFormat;
	private Runnable onClose = () -> {};
	private boolean closed = false;

	public Q7ResourceInfo(String storeFormat, URI uri) {
		this.plainStoreFormat = Preconditions.checkNotNull(storeFormat);
		Preconditions.checkNotNull(uri);
		resource = new Q7LazyResource(uri);
		resource.setTrackingModification(true);
	}

	// FIXME:  This method ignores thread safety to reads file without scheduling
	// JobManager.beginRule() deadlocks because of multi-threaded index implementation
	public void load(IFile file) throws ModelException {
		synchronized (this) {
			if (resource == null)
				throw new NullPointerException("Resource info " + plainStoreFormat + " can't be associated with a file");
			if (closed) {
				Q7Status status = new Q7Status(0, resource.getURI() + " is already closed");
				status.setStatusCode(Q7StatusCode.NotOpen);
				throw new ModelException(status);
			}
		}

		if (file != null) {
			timestamp = file.getModificationStamp();
		}
		URI uri = toURI(file);
		onClose = LeakDetector.INSTANCE.register(this);
		IPersistenceModel model = getPersistenceModel();

		if (file != null && !file.exists()) {
			throw newNotExistsException(file);
		}
		boolean allowEmptyMetadataContent = model.isAllowEmptyMetadataContent();
		try (InputStream metadataStream = openMetadata(model, file)) {
			if (metadataStream != null ) {
				resource.load(metadataStream, PersistenceManager.getOptions());
			}
				
			model.updateMetadata();
			EList<EObject> contents = resource.getContents();
			resource.setModified(false);
			if (contents.size() == 0 ) {
				throw new ModelException(new Q7Status(0, "Empty resource " + uri + ". Empty metadata is " + (allowEmptyMetadataContent ? "" : "not ") + "allowed. File: " + (file == null ? null : file.getFullPath())));
			}
			for (EObject eObject : contents) {
				if (eObject instanceof NamedElement) {
					element = (NamedElement) eObject;
				}
			}
			if (element == null) {
				throw new ModelException(new Q7Status(Q7Status.ERROR, "Illegal object type: " + contents.get(0).getClass().getName()));
			}
		} catch (IOException | CoreException e) {
			try {
				if (file != null && !file.exists()) {
					throw newNotExistsException(file);
				}
				ModelException modelException = new ModelException(e, Q7Status.ERROR);
				String content = null;
				try (InputStream is = openMetadata(model, file)) {
					if (is != null) {
						try (BufferedReader reader = new BufferedReader(new InputStreamReader(openMetadata(model, file), StandardCharsets.UTF_8))) {
							char[] buffer = new char[3000];
							int length = reader.read(buffer);
							if (length >= 0) {
								content = new String(buffer, 0, length);
							}
						}
					}
					modelException.addSuppressed(new RuntimeException("File content:\n" + content + "...\n"));
				} catch (IOException e1) {
					modelException.addSuppressed(modelException);
				} catch (CoreException e1) {
					modelException.addSuppressed(e1);
				}
				throw modelException;
			} finally {
				unload();
			}
		} catch (Throwable e) {
			unload();
			throw e;
		}
	}

	private ModelException newNotExistsException(IFile file) {
		Q7Status status = new Q7Status(Q7Status.ERROR, "Element: " + file.getFullPath()
				+ " doesn't exist");
		status.setStatusCode(Q7StatusCode.NotPressent);
		ModelException exception = new ModelException(status);
		return exception;
	}
	
	@Override
	public String toString() {
		return resource == null ? "null" : resource.getURI().toString();
	}

	public static URI toURI(IFile file) {
		return URI.createPlatformResourceURI(file != null ? file
				.getFullPath().toString() : "__uri__", true);
	}

	protected IPersistenceModel getPersistenceModel() {
		return PersistenceManager.getInstance().getModel(resource);
	}

	public void unload() {
		PersistenceManager.getInstance().remove(resource);
		synchronized (this) {
			element = null;
			timestamp = 0;
			onClose.run();
			closed = true;
		}
	}

	public NamedElement getNamedElement() {
		return element;
	}

	public void extractAllPersistence() {
		getPersistenceModel().extractAll();
	}

	public void save() {
		IPersistenceModel model = getPersistenceModel();
		if (model instanceof PlainTextPersistenceModel) {
			((PlainTextPersistenceModel)model).setSaveFormat(plainStoreFormat);
		}
		PersistenceManager.getInstance().saveResource(resource);
		resource.setModified(false);
		model.setUnmodified();
	}

	public IPersistenceModel getModel() {
		return getPersistenceModel();
	}

	public boolean hasChanges() {
		if (resource == null) {
			return true;
		}
		return resource.isModified() || getPersistenceModel().isModified();
	}

	public void createNamedElement(NamedElement createNamedElement) {
		this.element = createNamedElement;
		this.resource.getContents().add(this.element);
	}

	public Resource getResource() {
		return resource;
	}

	public void updatePersistenceModel(IPersistenceModel newModel) {
		PersistenceManager.getInstance().replaceModelWith(resource, newModel);
	}

	@Override
	public int getCacheFootprint() {
		Resource res = getResource();
		if (res == null) {
			return 0;
		}
		
		return PersistenceManager.getInstance().cachedSize(res);
	}

	@Override
	public void close() {
		unload();
	}
	
	private InputStream openMetadata(IPersistenceModel model, IFile file) throws CoreException {
		InputStream metadataStream = PersistenceManager.getInstance().loadMetadata(model);
		if (metadataStream != null) {
			return metadataStream;
		}
		if (file != null && !model.isAllowEmptyMetadataContent()) {
			return file.getContents();
		}
		return null;
	}
}
