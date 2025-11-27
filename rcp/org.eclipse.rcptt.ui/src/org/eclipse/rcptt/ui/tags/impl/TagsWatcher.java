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
package org.eclipse.rcptt.ui.tags.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rcptt.core.model.IElementChangedListener;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7ElementDelta;
import org.eclipse.rcptt.core.model.IQ7Folder;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.Q7ElementChangedEvent;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.tags.Tag;
import org.eclipse.rcptt.core.tags.TagsFactory;
import org.eclipse.rcptt.core.tags.TagsRegistry;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;

public class TagsWatcher implements IElementChangedListener {

	// separator regex for the hierarchical tags
	public static final String HIERARCHY_SEP = "[/]"; //$NON-NLS-1$

	private final TagsRegistry tags;
	private final Multimap<IQ7NamedElement, String> TagsRefsMap = Multimaps.synchronizedMultimap(MultimapBuilder.hashKeys().arrayListValues().build());
	private final Job reloadTagsJob = Job.create("Index RCPTT tags", ignored -> {reloadTags(); return Status.OK_STATUS; });
	private final QueueJob<IQ7NamedElement> reindexElement = new QueueJob<IQ7NamedElement>("Index RCPTT element tags", new QueueJob.UniqueQueue<>()) {

		@Override
		protected void process(IQ7NamedElement item, SubMonitor monitor) {
			monitor.beginTask(item.getPath().toPortableString(), 1);
			processElementChanged(item);
			monitor.done();
		}
		
	};
	

	public TagsWatcher(final TagsRegistry tags) {
		this.tags = tags;
		reloadTagsJob.schedule();
	}

	public void elementChanged(Q7ElementChangedEvent event) {
		IQ7ElementDelta delta = event.getDelta();
		// container changes
		if (hasContainerChanges(delta)) {
			reindexElement.cancel();
			reloadTagsJob.schedule();
			return;
		}
		// named element changes
		IQ7NamedElement[] namedElements = delta.getNamedElements();
		for (final IQ7NamedElement changedElement : namedElements) {
			IQ7ElementDelta childDelta = delta.getChildBy(changedElement);
			switch (childDelta.getKind()) {
			case IQ7ElementDelta.REMOVED:
				processElementRemoved(changedElement);
				break;
			case IQ7ElementDelta.ADDED:
				reindexElement.add(changedElement);
				break;
			case IQ7ElementDelta.CHANGED:
				if ((childDelta.getFlags() & IQ7ElementDelta.F_CONTENT) != 0) {
					reindexElement.add(changedElement);
				}
				break;
			}
		}
		// Check for project opened/closed flags
		IQ7ElementDelta[] addedChildren = delta.getAffectedChildren();
		for (IQ7ElementDelta iq7ElementDelta : addedChildren) {
			if (iq7ElementDelta.getElement() instanceof IQ7Project) {
				if ((iq7ElementDelta.getFlags() & IQ7ElementDelta.F_OPENED) != 0
						|| (iq7ElementDelta.getFlags() & IQ7ElementDelta.F_CLOSED) != 0) {
					reloadTags();
					break;
				}
			}
		}
	}

	private boolean hasContainerChanges(IQ7ElementDelta delta) {
		IQ7Element element = delta.getElement();
		if (element instanceof IQ7Project || element instanceof IQ7Folder) {
			if (delta.getKind() == IQ7ElementDelta.REMOVED
					|| delta.getKind() == IQ7ElementDelta.ADDED) {
				return true;
			}
		}
		IQ7ElementDelta[] deltas = delta.getAffectedChildren();
		for (IQ7ElementDelta childDelta : deltas) {
			return hasContainerChanges(childDelta);
		}
		return false;
	}

	private void processElementRemoved(IQ7NamedElement element) {
		Collection<String> tagsToRemove = TagsRefsMap.removeAll(element);
		for (String tag : tagsToRemove) {
			removeTagRef(tag, element);
		}
	}

	private void processElementChanged(IQ7NamedElement element) {
		String[] currentTags = Q7SearchCore.findTagsByDocument(element);
		setTags(element, asList(currentTags));
	}

	private void setTags(IQ7NamedElement element, Collection<String> currentTags) {
		Set<String> oldTags = new HashSet<>(TagsRefsMap.get(element));
		for (String newTag : currentTags) {
			if (TagsRefsMap.put(element, newTag)) {
				addTagRef(newTag, element);
			}
			oldTags.remove(newTag);
		}

		for (String oldTag : oldTags) {
			if (TagsRefsMap.remove(element, oldTag)) {
				removeTagRef(oldTag, element);
			}
		}
	}

	private void reloadTags() {
		Map<IQ7NamedElement, List<String>> ActualTagsRefsMap = Q7SearchCore
				.findAllTagReferences();

		ActualTagsRefsMap.forEach((k, v) -> {
			setTags(k, v);
		});
		ArrayList<IQ7NamedElement> removed = new ArrayList<>();
		TagsRefsMap.keySet().removeIf(k -> {
			if (ActualTagsRefsMap.containsKey(k)) {
				return false;
			}
			removed.add(k);
			return true;
		});
		removed.forEach(k -> {
			setTags(k, emptyList());
		});
	}

	private void addTagRef(final String tagString, final IQ7NamedElement ref) {
		Object container = tags;
		Tag tag = null;
		for (final String name : tagString.split(HIERARCHY_SEP)) {
			synchronized (container) {
				tag = findTag(getTags(container), name);
			}

			if (tag == null) {
				tag = TagsFactory.eINSTANCE.createTag();
				tag.setValue(name);
				synchronized (container) {
					getTags(container).add(tag);
				}
			}
			container = tag;
		}
		synchronized (tag) {
			tag.getRefs().add(ref);
		}
	}

	private void removeTagRef(final String tagString, final IQ7NamedElement ref) {
		Object container = tags;
		Tag tag = null;
		for (final String name : tagString.split(HIERARCHY_SEP)) {
			synchronized (container) {
				tag = findTag(getTags(container), name);
			}
			if (tag == null) {
				return;
			}
			container = tag;
		}
		synchronized (tag) {
			tag.getRefs().remove(ref);
		}

		while (true) {
			synchronized (tag) {
				if (!(tag.getRefs().isEmpty() && tag.getTags().isEmpty())) {
					return;
				}
			}
			container = tag.eContainer();
			synchronized (container) {
				getTags(container).remove(tag);
			}
			if (container instanceof Tag) {
				tag = (Tag) container;
			} else {
				return;
			}
		}

	}

	public Tag findTag(List<Tag> tags, final String tagString) {
		for (final Tag test : tags) {
			if (test.getValue().equals(tagString)) {
				return test;
			}
		}
		return null;
	}

	private List<Tag> getTags(Object container) {
		List<Tag> tags = null;
		if (container instanceof TagsRegistry) {
			tags = ((TagsRegistry) container).getTags();
		} else {
			tags = ((Tag) container).getTags();
		}
		return tags;
	}

}
