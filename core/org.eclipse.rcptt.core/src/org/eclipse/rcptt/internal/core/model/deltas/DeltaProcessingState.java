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
package org.eclipse.rcptt.internal.core.model.deltas;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.internal.core.model.ModelManager;

public class DeltaProcessingState implements IResourceChangeListener {

	public IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];
	public int[] elementChangedListenerMasks = new int[5];
	public int elementChangedListenerCount = 0;

	public IResourceChangeListener[] preResourceChangeListeners = new IResourceChangeListener[1];
	public int[] preResourceChangeEventMasks = new int[1];
	public int preResourceChangeListenerCount = 0;

	private ThreadLocal<DeltaProcessor> deltaProcessors = new ThreadLocal<DeltaProcessor>();
	@SuppressWarnings("rawtypes")
	public HashMap projectDependencies = new HashMap();

	// private Set initializingThreads = Collections
	// .synchronizedSet(new HashSet());

	private HashSet<String> scriptProjectNamesCache;

	public synchronized void addElementChangedListener(
			IElementChangedListener listener, int eventMask) {
		for (int i = 0; i < this.elementChangedListenerCount; i++) {
			if (this.elementChangedListeners[i].equals(listener)) {

				// only clone the masks, since we could be in the middle of
				// notifications and one listener decide to change
				// any event mask of another listeners (yet not notified).
				int cloneLength = this.elementChangedListenerMasks.length;
				System.arraycopy(
						this.elementChangedListenerMasks,
						0,
						this.elementChangedListenerMasks = new int[cloneLength],
						0, cloneLength);
				this.elementChangedListenerMasks[i] = eventMask; // could be
				// different
				return;
			}
		}
		// may need to grow, no need to clone, since iterators will have cached
		// original arrays and max boundary and we only add to the end.
		int length;
		if ((length = this.elementChangedListeners.length) == this.elementChangedListenerCount) {
			System.arraycopy(
					this.elementChangedListeners,
					0,
					this.elementChangedListeners = new IElementChangedListener[length * 2],
					0, length);
			System.arraycopy(this.elementChangedListenerMasks, 0,
					this.elementChangedListenerMasks = new int[length * 2], 0,
					length);
		}
		this.elementChangedListeners[this.elementChangedListenerCount] = listener;
		this.elementChangedListenerMasks[this.elementChangedListenerCount] = eventMask;
		this.elementChangedListenerCount++;
	}

	public void addPreResourceChangedListener(IResourceChangeListener listener,
			int eventMask) {
		for (int i = 0; i < this.preResourceChangeListenerCount; i++) {
			if (this.preResourceChangeListeners[i].equals(listener)) {
				this.preResourceChangeEventMasks[i] |= eventMask;
				return;
			}
		}
		// may need to grow, no need to clone, since iterators will have cached
		// original arrays and max boundary and we only add to the end.
		int length;
		if ((length = this.preResourceChangeListeners.length) == this.preResourceChangeListenerCount) {
			System.arraycopy(
					this.preResourceChangeListeners,
					0,
					this.preResourceChangeListeners = new IResourceChangeListener[length * 2],
					0, length);
			System.arraycopy(this.preResourceChangeEventMasks, 0,
					this.preResourceChangeEventMasks = new int[length * 2], 0,
					length);
		}
		this.preResourceChangeListeners[this.preResourceChangeListenerCount] = listener;
		this.preResourceChangeEventMasks[this.preResourceChangeListenerCount] = eventMask;
		this.preResourceChangeListenerCount++;
	}

	public DeltaProcessor getDeltaProcessor() {
		DeltaProcessor deltaProcessor = (DeltaProcessor) this.deltaProcessors
				.get();
		if (deltaProcessor != null)
			return deltaProcessor;
		deltaProcessor = new DeltaProcessor(this,
				ModelManager.getModelManager());
		this.deltaProcessors.set(deltaProcessor);
		return deltaProcessor;
	}

	public synchronized void removeElementChangedListener(
			IElementChangedListener listener) {

		for (int i = 0; i < this.elementChangedListenerCount; i++) {

			if (this.elementChangedListeners[i].equals(listener)) {

				// need to clone defensively since we might be in the middle of
				// listener notifications (#fire)
				int length = this.elementChangedListeners.length;
				IElementChangedListener[] newListeners = new IElementChangedListener[length];
				System.arraycopy(this.elementChangedListeners, 0, newListeners,
						0, i);
				int[] newMasks = new int[length];
				System.arraycopy(this.elementChangedListenerMasks, 0, newMasks,
						0, i);

				// copy trailing listeners
				int trailingLength = this.elementChangedListenerCount - i - 1;
				if (trailingLength > 0) {
					System.arraycopy(this.elementChangedListeners, i + 1,
							newListeners, i, trailingLength);
					System.arraycopy(this.elementChangedListenerMasks, i + 1,
							newMasks, i, trailingLength);
				}

				// update manager listener state (#fire need to iterate over
				// original listeners through a local variable to hold onto
				// the original ones)
				this.elementChangedListeners = newListeners;
				this.elementChangedListenerMasks = newMasks;
				this.elementChangedListenerCount--;
				return;
			}
		}
	}

	public void removePreResourceChangedListener(
			IResourceChangeListener listener) {

		for (int i = 0; i < this.preResourceChangeListenerCount; i++) {

			if (this.preResourceChangeListeners[i].equals(listener)) {

				// need to clone defensively since we might be in the middle of
				// listener notifications (#fire)
				int length = this.preResourceChangeListeners.length;
				IResourceChangeListener[] newListeners = new IResourceChangeListener[length];
				int[] newEventMasks = new int[length];
				System.arraycopy(this.preResourceChangeListeners, 0,
						newListeners, 0, i);
				System.arraycopy(this.preResourceChangeEventMasks, 0,
						newEventMasks, 0, i);

				// copy trailing listeners
				int trailingLength = this.preResourceChangeListenerCount - i
						- 1;
				if (trailingLength > 0) {
					System.arraycopy(this.preResourceChangeListeners, i + 1,
							newListeners, i, trailingLength);
					System.arraycopy(this.preResourceChangeEventMasks, i + 1,
							newEventMasks, i, trailingLength);
				}

				// update manager listener state (#fire need to iterate over
				// original listeners through a local variable to hold onto
				// the original ones)
				this.preResourceChangeListeners = newListeners;
				this.preResourceChangeEventMasks = newEventMasks;
				this.preResourceChangeListenerCount--;
				return;
			}
		}
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		for (int i = 0; i < this.preResourceChangeListenerCount; i++) {
			// wrap callbacks with Safe runnable for subsequent listeners to be
			// called when some are causing grief
			final IResourceChangeListener listener = this.preResourceChangeListeners[i];
			if ((this.preResourceChangeEventMasks[i] & event.getType()) != 0)
				SafeRunner.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
						exception.printStackTrace();
					}

					public void run() throws Exception {
						listener.resourceChanged(event);
					}
				});
		}
		try {
			getDeltaProcessor().resourceChanged(event);
		} catch (InterruptedException e) {
			OperationCanceledException result = new OperationCanceledException();
			result.initCause(e);
			throw result;
		} finally {
			// TODO (jerome) see 47631, may want to get rid of following so as
			// to reuse delta processor ?
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				this.deltaProcessors.set(null);
			}
		}

	}

	public IQ7Project findProject(String name) throws InterruptedException {
		if (getOldProjectNames().contains(name))
			return ModelManager.getModelManager().getModel().getProject(name);
		return null;
	}

	public synchronized HashSet<String> getOldProjectNames() throws InterruptedException {
		if (this.scriptProjectNamesCache == null) {
			HashSet<String> result = new HashSet<String>();
			IQ7Project[] projects;
			try {
				projects = ModelManager.getModelManager().getModel()
						.getProjects();
			} catch (ModelException e) {
				return this.scriptProjectNamesCache;
			}
			for (int i = 0, length = projects.length; i < length; i++) {
				IQ7Project project = projects[i];
				result.add(project.getName());
			}
			return this.scriptProjectNamesCache = result;
		}
		return this.scriptProjectNamesCache;
	}

	public synchronized void resetOldProjectNames() {
		this.scriptProjectNamesCache = null;
	}
}
