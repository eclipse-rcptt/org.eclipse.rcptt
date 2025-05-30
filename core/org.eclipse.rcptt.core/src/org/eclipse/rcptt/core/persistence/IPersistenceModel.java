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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.emf.ecore.resource.Resource;

public interface IPersistenceModel {

	/**
	 * Return true if contain some items.
	 * 
	 * @return
	 */
	boolean hasElements();

	/**
	 * Get all item names
	 * 
	 * @return
	 */
	String[] getNames();

	/**
	 * Dispose all created temporary info.
	 */
	void dispose();

	/**
	 * Return attachment input stream
	 * @throws IOException 
	 */
	InputStream read(String name);

	/**
	 * Delete attached input item
	 * 
	 * @param name
	 */
	void delete(String name);

	/**
	 * Restore item working copy from container
	 */
	boolean restore(String name);

	/**
	 * Return attachment output stream
	 */
	OutputStream store(String name);

	/**
	 * Save container to new created temporary file, for later use.
	 * 
	 * @return
	 */
	File storeToTemporaty();

	/**
	 * Remove all items and dispose
	 */
	void removeAll();

	void extractAll();

	/**
	 * Copy all content from one model to another
	 * 
	 * @param originalModel
	 * @return
	 */
	boolean copyFrom(IPersistenceModel originalModel);

	boolean isSupportMultiItems();

	boolean isModified();

	void updateMetadata();

	void updateAttributes();

	void setUnmodified();

	Resource getResource();

	long size(String teslaContentEntry);

	boolean isContentEntryRequired();

	boolean isAllowEmptyMetadataContent();

	void rename(String oldName, String newName);
}