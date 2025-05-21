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
package org.eclipse.rcptt.ui.editors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;

import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.IQ7Project;
import org.eclipse.rcptt.core.model.IQ7ProjectMetadata;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.AllProjectScope;
import org.eclipse.rcptt.core.model.search.ISearchScope;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.scenario.TestSuiteItem;
import org.eclipse.rcptt.internal.core.model.ReferencedProjectScope;
import org.eclipse.rcptt.ui.actions.Q7ElementLabelProvider;

public class NamedElementLabelProvider extends Q7ElementLabelProvider implements
		ILabelProvider {

	private final IQ7Project project;

	public NamedElementLabelProvider(IQ7Project project) {
		this.project = project;
		fullPath = false;
	}

	public static final Styler ERROR_STYLER = StyledString
			.createColorRegistryStyler(JFacePreferences.ERROR_COLOR, null);

	@Override
	public StyledString getStyledText(Object element, int rowIndex) {
		if (element instanceof IQ7ProjectMetadata) {
			return new StyledString("Project Settings");
		}
		IQ7NamedElement namedElement = getNamedElement(element);
		if (namedElement != null) {
			ISearchScope scope = null;
			if (project == null) {
				scope = new AllProjectScope();
			} else {
				scope = new ReferencedProjectScope(project);
			}
			if (!scope.contains(namedElement.getPath())) {
				StyledString value = super
						.getStyledText(namedElement, rowIndex);
				StyledString ss = new StyledString();
				ss.append("Possible resolution -> ");
				ss.setStyle(0, ss.length() - 3, StyledString.COUNTER_STYLER);
				ss.append(value);
				return ss;
			}
			return super.getStyledText(namedElement, rowIndex);
		}
		if (element instanceof String) {
			StyledString ss = new StyledString();
			ss.append("Unresolved");
			ss.setStyle(0, ss.length(), ERROR_STYLER);
			ss.append(" (");
			String v = (String) element;
			ss.append(v);
			ss.setStyle(ss.length() - v.length(), v.length(),
					StyledString.QUALIFIER_STYLER);
			ss.append(")");

			return ss;
		}
		return super.getStyledText(element, rowIndex);
	}

	@Override
	public String getText(Object element) {
		IQ7NamedElement namedElement = getNamedElement(element);

		if (namedElement instanceof IQ7ProjectMetadata) {
			return "Project Settings";
		}

		if (namedElement != null) {
			ReferencedProjectScope scope = new ReferencedProjectScope(project);
			if (!scope.contains(namedElement.getPath())) {
				return "Possible resolution -> " + super.getText(namedElement);
			}
			return super.getText(namedElement);
		}
		if (element instanceof String) {
			StringBuilder ss = new StringBuilder();
			ss.append("Unresolved");
			ss.append(" (");
			String v = (String) element;
			ss.append(v);
			ss.append(")");

			return ss.toString();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		IQ7NamedElement namedElement = getNamedElement(element);
		if (namedElement != null) {
			return super.getImage(namedElement);
		}
		return super.getImage(element);
	}

	protected IQ7NamedElement getNamedElement(Object element) {
		String elementId = null;
		if (element instanceof String) {
			elementId = (String) element;
		} else if (element instanceof TestSuiteItem) {
			elementId = ((TestSuiteItem) element).getNamedElementId();
		}
		if (elementId != null) {
			IQ7NamedElement[] elements = null;
			if (project != null) {
				elements = Q7SearchCore.findById((String) elementId,
						new ReferencedProjectScope(project),
						new NullProgressMonitor(), 1000);
			}
			if (elements == null || elements.length == 0) {
				elements = Q7SearchCore.findById((String) elementId,
						new AllProjectScope(), new NullProgressMonitor(), 1000);
			}
			if (elementId != null && elements.length > 0) {
				return elements[0];
			}
		}
		return null;
	}
}
