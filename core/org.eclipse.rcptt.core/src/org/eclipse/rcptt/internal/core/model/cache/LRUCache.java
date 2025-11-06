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
package org.eclipse.rcptt.internal.core.model.cache;

import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.rcptt.internal.core.model.Q7Element;

public class LRUCache implements Cloneable {

	protected static class LRUCacheEntry {

		public Object _fKey;

		public Object _fValue;

		public int _fTimestamp;

		public int _fSpace;

		public LRUCacheEntry _fPrevious;

		public LRUCacheEntry _fNext;

		public LRUCacheEntry(Object key, Object value, int space) {
			_fKey = key;
			_fValue = value;
			_fSpace = space;
		}

		public String toString() {

			return "LRUCacheEntry [" + _fKey + "-->" + _fValue + "]"; //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected int fCurrentSpace;

	protected int fSpaceLimit;

	protected int fTimestampCounter;

	protected Hashtable<Object, LRUCacheEntry> fEntryTable;

	protected LRUCacheEntry fEntryQueue;

	protected LRUCacheEntry fEntryQueueTail;

	protected static final int DEFAULT_SPACELIMIT = 100;

	public LRUCache() {

		this(DEFAULT_SPACELIMIT);
	}

	public LRUCache(int size) {

		fTimestampCounter = fCurrentSpace = 0;
		fEntryQueue = fEntryQueueTail = null;
		fEntryTable = new Hashtable<>(size);
		fSpaceLimit = size;
	}

	public Object clone() {

		LRUCache newCache = newInstance(fSpaceLimit);
		LRUCacheEntry qEntry;

		/* Preserve order of entries by copying from oldest to newest */
		qEntry = this.fEntryQueueTail;
		while (qEntry != null) {
			newCache.privateAdd(qEntry._fKey, qEntry._fValue, qEntry._fSpace);
			qEntry = qEntry._fPrevious;
		}
		return newCache;
	}

	public double fillingRatio() {
		return (fCurrentSpace) * 100.0 / fSpaceLimit;
	}

	public void flush() {

		fCurrentSpace = 0;
		LRUCacheEntry entry = fEntryQueueTail; // Remember last entry
		fEntryTable = new Hashtable<>(); // Clear it out
		fEntryQueue = fEntryQueueTail = null;
		while (entry != null) { // send deletion notifications in LRU order
			privateNotifyDeletionFromCache(entry);
			entry = entry._fPrevious;
		}
	}

	public void flush(Object key) {

		LRUCacheEntry entry;

		entry = (LRUCacheEntry) fEntryTable.get(key);

		/* If entry does not exist, return */
		if (entry == null)
			return;

		this.privateRemoveEntry(entry, false);
	}

	public Object get(Object key) {

		LRUCacheEntry entry = (LRUCacheEntry) fEntryTable.get(key);
		if (entry == null) {
			return null;
		}

		this.updateTimestamp(entry);
		return entry._fValue;
	}

	public int getCurrentSpace() {
		return fCurrentSpace;
	}

	public int getSpaceLimit() {
		return fSpaceLimit;
	}

	public Enumeration<Object> keys() {

		return fEntryTable.keys();
	}

	public ICacheEnumeration keysAndValues() {
		return new ICacheEnumeration() {

			Enumeration<LRUCacheEntry> fValues = fEntryTable.elements();
			LRUCacheEntry fEntry;

			public boolean hasMoreElements() {
				return fValues.hasMoreElements();
			}

			public Object nextElement() {
				fEntry = (LRUCacheEntry) fValues.nextElement();
				return fEntry._fKey;
			}

			public Object getValue() {
				if (fEntry == null) {
					throw new java.util.NoSuchElementException();
				}
				return fEntry._fValue;
			}
		};
	}

	protected boolean makeSpace(int space) {

		int limit;

		limit = this.getSpaceLimit();

		/* if space is already available */
		if (fCurrentSpace + space <= limit) {
			return true;
		}

		/* if entry is too big for cache */
		if (space > limit) {
			return false;
		}

		/* Free up space by removing oldest entries */
		while (fCurrentSpace + space > limit && fEntryQueueTail != null) {
			this.privateRemoveEntry(fEntryQueueTail, false);
		}
		return true;
	}

	protected LRUCache newInstance(int size) {
		return new LRUCache(size);
	}

	public Object peek(Object key) {
		LRUCacheEntry entry = (LRUCacheEntry) fEntryTable.get(key);
		if (entry == null) {
			return null;
		}
		return entry._fValue;
	}

	protected void privateAdd(Object key, Object value, int space) {

		LRUCacheEntry entry;

		entry = new LRUCacheEntry(key, value, space);
		this.privateAddEntry(entry, false);
	}

	protected void privateAddEntry(LRUCacheEntry entry, boolean shuffle) {

		if (!shuffle) {
			LRUCacheEntry oldEntry = fEntryTable.put(entry._fKey, entry);
			assert oldEntry == null;
			fCurrentSpace += entry._fSpace;
		}

		entry._fTimestamp = fTimestampCounter++;
		entry._fNext = this.fEntryQueue;
		entry._fPrevious = null;

		if (fEntryQueue == null) {
			/* this is the first and last entry */
			fEntryQueueTail = entry;
		} else {
			fEntryQueue._fPrevious = entry;
		}

		fEntryQueue = entry;
	}

	protected void privateNotifyDeletionFromCache(LRUCacheEntry entry) {
		// Default is NOP.
	}

	protected void privateRemoveEntry(LRUCacheEntry entry, boolean shuffle) {

		LRUCacheEntry previous, next;

		previous = entry._fPrevious;
		next = entry._fNext;

		if (!shuffle) {
			fEntryTable.remove(entry._fKey);
			fCurrentSpace -= entry._fSpace;
			privateNotifyDeletionFromCache(entry);
		}

		/* if this was the first entry */
		if (previous == null) {
			fEntryQueue = next;
		} else {
			previous._fNext = next;
		}

		/* if this was the last entry */
		if (next == null) {
			fEntryQueueTail = previous;
		} else {
			next._fPrevious = previous;
		}
	}

	public Object put(Object key, Object value) {

		int newSpace, oldSpace, newTotal;
		LRUCacheEntry entry;

		/* Check whether there's an entry in the cache */
		newSpace = spaceFor(value);
		entry = (LRUCacheEntry) fEntryTable.get(key);

		if (entry != null) {

			/**
			 * Replace the entry in the cache if it would not overflow the
			 * cache. Otherwise flush the entry and re-add it so as to keep
			 * cache within budget
			 */
			oldSpace = entry._fSpace;
			newTotal = getCurrentSpace() - oldSpace + newSpace;
			if (newTotal <= getSpaceLimit()) {
				updateTimestamp(entry);
				entry._fValue = value;
				entry._fSpace = newSpace;
				this.fCurrentSpace = newTotal;
				return value;
			} else {
				privateRemoveEntry(entry, false);
			}
		}
		if (makeSpace(newSpace)) {
			privateAdd(key, value, newSpace);
		}
		return value;
	}

	public Object removeKey(Object key) {

		LRUCacheEntry entry = (LRUCacheEntry) fEntryTable.get(key);
		if (entry == null) {
			return null;
		}
		Object value = entry._fValue;
		this.privateRemoveEntry(entry, false);
		return value;
	}

	public void setSpaceLimit(int limit) {
		if (limit < fSpaceLimit) {
			makeSpace(fSpaceLimit - limit);
		}
		fSpaceLimit = limit;
	}

	protected int spaceFor(Object value) {

		if (value instanceof ILRUCacheable) {
			return ((ILRUCacheable) value).getCacheFootprint();
		} else {
			return 1;
		}
	}

	public String toString() {
		return toStringFillingRation("LRUCache") + //$NON-NLS-1$
				toStringContents();
	}

	protected String toStringContents() {
		StringBuffer result = new StringBuffer();
		int length = fEntryTable.size();
		Object[] unsortedKeys = new Object[length];
		String[] unsortedToStrings = new String[length];
		Enumeration<Object> e = this.keys();
		for (int i = 0; i < length; i++) {
			Object key = e.nextElement();
			unsortedKeys[i] = key;
			unsortedToStrings[i] = (key instanceof Q7Element) ? ((Q7Element) key)
					.getName() : key.toString();
		}
		ToStringSorter sorter = new ToStringSorter();
		sorter.sort(unsortedKeys, unsortedToStrings);
		for (int i = 0; i < length; i++) {
			String toString = sorter.sortedStrings[i];
			Object value = this.get(sorter.sortedObjects[i]);
			result.append(toString);
			result.append(" -> "); //$NON-NLS-1$
			result.append(value);
			result.append("\n"); //$NON-NLS-1$
		}
		return result.toString();
	}

	public String toStringFillingRation(String cacheName) {
		StringBuffer buffer = new StringBuffer(cacheName);
		buffer.append('[');
		buffer.append(getSpaceLimit());
		buffer.append("]: "); //$NON-NLS-1$
		buffer.append(NumberFormat.getInstance().format(fillingRatio()));
		buffer.append("% full"); //$NON-NLS-1$
		return buffer.toString();
	}

	protected void updateTimestamp(LRUCacheEntry entry) {

		entry._fTimestamp = fTimestampCounter++;
		if (fEntryQueue != entry) {
			this.privateRemoveEntry(entry, true);
			this.privateAddEntry(entry, true);
		}
		return;
	}
}
