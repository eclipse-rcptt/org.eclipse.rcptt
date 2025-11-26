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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;
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

public class TagsWatcher implements IElementChangedListener {

	// separator regex for the hierarchical tags
	public static final String HIERARCHY_SEP = "[/]"; //$NON-NLS-1$

	private final TagsRegistry tags;
	private Map<IQ7NamedElement, List<String>> TagsRefsMap;

	public TagsWatcher(final TagsRegistry tags) {
		this.tags = tags;
		// Initial tags load
		TagsRefsMap = Q7SearchCore.findAllTagReferences();
		for (Map.Entry<IQ7NamedElement, List<String>> entry : TagsRefsMap
				.entrySet()) {
			for (String tag : entry.getValue()) {
				addTagRef(tag, entry.getKey());
			}
		}
	}

	private final Job reloadTagsJob = Job.create("Index RCPTT tags", ignored -> {reloadTags(); return Status.OK_STATUS; });
	public void elementChanged(Q7ElementChangedEvent event) {
		IQ7ElementDelta delta = event.getDelta();
		// container changes
		if (hasContainerChanges(delta)) {
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
				processElementChanged(changedElement);
				break;
			case IQ7ElementDelta.CHANGED:
				if ((childDelta.getFlags() & IQ7ElementDelta.F_CONTENT) != 0) {
					processElementChanged(changedElement);
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
		List<String> tagsToRemove = TagsRefsMap.get(element);
		if (tagsToRemove != null) {
			for (String tag : tagsToRemove) {
				removeTagRef(tag, element);
			}
		}
		TagsRefsMap.remove(element);
	}

	private void processElementChanged(IQ7NamedElement element) {
		List<String> oldTags = TagsRefsMap.get(element);
		if (oldTags == null) {
			oldTags = new ArrayList<String>();
		}
		List<String> newTags = new ArrayList<String>();

		for (String newTag : Q7SearchCore.findTagsByDocument(element)) {
			if (!oldTags.contains(newTag)) {
				addTagRef(newTag, element);
			}
			newTags.add(newTag);
		}

		for (String oldTag : oldTags) {
			if (!newTags.contains(oldTag)) {
				removeTagRef(oldTag, element);
			}
		}
		TagsRefsMap.put(element, newTags);
	}

	private void reloadTags() {
		Map<IQ7NamedElement, List<String>> ActualTagsRefsMap = Q7SearchCore
				.findAllTagReferences();

		for (Map.Entry<IQ7NamedElement, List<String>> actualEntry : ActualTagsRefsMap
				.entrySet()) {
			if (TagsRefsMap.get(actualEntry.getKey()) == null) {
				// new named element added
				for (String tag : actualEntry.getValue()) {
					addTagRef(tag, actualEntry.getKey());
				}
			} else {
				// existing element
				processElementChanged(actualEntry.getKey());
			}
		}

		for (Map.Entry<IQ7NamedElement, List<String>> oldEntry : TagsRefsMap
				.entrySet()) {
			if (ActualTagsRefsMap.get(oldEntry.getKey()) == null) {
				// named element deleted
				for (String tag : oldEntry.getValue()) {
					removeTagRef(tag, oldEntry.getKey());
				}
			}
		}

		TagsRefsMap = ActualTagsRefsMap;
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
