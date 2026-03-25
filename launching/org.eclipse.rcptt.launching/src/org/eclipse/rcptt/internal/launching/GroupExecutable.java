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
package org.eclipse.rcptt.internal.launching;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.launching.AutLaunch;

public class GroupExecutable extends Executable {

	private final Executable[] kids;
	private String name;
	private final Executable root;

	public GroupExecutable(Executable root, List<Executable> group) {
		super(root.isDebug());

		this.root = root;
		this.kids = group.toArray(new Executable[group.size()]);
	}

	public AutLaunch getAut() {
		return getRoot().getAut();
	}

	@Override
	public void startLaunching() {
		if (kids.length > 0) {
			kids[0].startLaunching();
		}
	}

	public IQ7NamedElement getActualElement() {
		return getRoot().getActualElement();
	}

	public Executable getRoot() {
		return root;
	}

	@Override
	public Executable[] getChildren() {
		return kids;
	}

	public String getName() {
		if (name != null) {
			return name;
		}
		return getRoot().getName();
	}

	public int getType() {
		return getRoot().getType();
	}

	@Override
	public String getId() {
		return getRoot().getId();
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Group: " + root.toString();
	}

}
