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
package org.eclipse.rcptt.compare;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.persistence.IPersistenceModel;
import org.eclipse.rcptt.core.persistence.PersistenceManager;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.core.model.Q7ResourceInfo;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class ScenarioStructureCreator implements IStructureCreator {

	public static final String STRUCTURE_COMPARE_TITLE = "Test Case Structure Compare"; //$NON-NLS-1$
	public static final String CONTENT_TYPE_SCENARIO = "Test Case"; //$NON-NLS-1$
	public static final String CONTENT_TYPE_NAME = "Name"; //$NON-NLS-1$
	public static final String CONTENT_TYPE_TAGS = "Tags"; //$NON-NLS-1$
	public static final String CONTENT_TYPE_EXTERNALREF = "External References"; //$NON-NLS-1$
	public static final String CONTENT_TYPE_DESCRIPTION = "Description"; //$NON-NLS-1$
	public static final String CONTENT_TYPE_SCRIPT = "Script"; //$NON-NLS-1$

	public static final String TYPE_ECL = "ecl"; //$NON-NLS-1$	

	private final String fTitle;

	public ScenarioStructureCreator() {
		this(STRUCTURE_COMPARE_TITLE);
	}

	public ScenarioStructureCreator(String title) {
		fTitle = title;
	}

	public String getName() {
		return fTitle;
	}

	public IStructureComparator getStructure(Object input) {
		if (input instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca = (IStreamContentAccessor) input;
			try {
				String scenarioContent = Utilities.readString(sca);
				InputStream is = sca.getContents();

				try {
					byte[] content = FileUtil.getStreamContent(is);

					Q7ResourceInfo info = new Q7ResourceInfo(IPlainConstants.PLAIN_HEADER, URI.createURI("__compare__"));
					final IPersistenceModel model = PersistenceManager
							.getInstance()
							.getModel(content, info.getResource());
					if (model == null) {
						return null;
					}

					try {
						info.load(null);
					} catch (ModelException e){
						RcpttPlugin.log(e);
						return null;
					}

					Scenario sc = (Scenario) info.getNamedElement();
					// Root node
					ScenarioRoot root = new ScenarioRoot("");
					// Scenario node
					ScenarioRoot scenario = root
							.createScenarioContainer(CONTENT_TYPE_SCENARIO);
					scenario.setStringContents(scenarioContent);
					// Name
					ScenarioPart name = scenario
							.createPartContainer(CONTENT_TYPE_NAME);
					name.setStringContents(sc.getName());
					// Tags
					ScenarioPart tags = scenario
							.createPartContainer(CONTENT_TYPE_TAGS);
					tags.setStringContents(sc.getTags());
					// External references
					ScenarioPart extRef = scenario
							.createPartContainer(CONTENT_TYPE_EXTERNALREF);
					extRef.setStringContents(sc.getExternalReference());
					// Description
					ScenarioPart desc = scenario
							.createPartContainer(CONTENT_TYPE_DESCRIPTION);
					desc.setStringContents(sc.getDescription());
					// Script
					ScenarioPart script = scenario
							.createPartContainer(CONTENT_TYPE_SCRIPT);
					script.setStringContents(Scenarios.getScriptContent(sc));

					return root;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public IStructureComparator locate(Object path, Object input) {
		return null;
	}

	public String getContents(Object node, boolean ignoreWhitespace) {
		if (node instanceof ScenarioPart) {
			return ((ScenarioPart) node).getStringContents();
		}
		return null;
	}

	public void save(IStructureComparator node, Object input) {
		Assert.isTrue(false); // Cannot update scenario file
	}

	static abstract class ScenarioResource implements IStructureComparator,
			ITypedElement {

		private final String fName;

		ScenarioResource(String name) {
			fName = name;
		}

		public String getName() {
			return fName;
		}

		public Image getImage() {
			if (CONTENT_TYPE_SCENARIO.equals(getName())) {
				return Images.getImage(Images.SCENARIO);
			}
			if (CONTENT_TYPE_SCRIPT.equals(getName())) {
				return Images.getImage(Images.PANEL_SCENARIO);
			}
			return CompareUI.getImage(getType());
		}

		/*
		 * Returns true if other is ITypedElement and names are equal.
		 * 
		 * @see IComparator#equals
		 */
		@Override
		public boolean equals(Object other) {
			if (other instanceof ITypedElement)
				return fName.equals(((ITypedElement) other).getName());
			return super.equals(other);
		}

		@Override
		public int hashCode() {
			return fName.hashCode();
		}
	}

	static class ScenarioPart extends ScenarioResource implements
			IStreamContentAccessor {

		private String fContents;

		ScenarioPart(String name) {
			super(name);
		}

		public String getType() {
			if (CONTENT_TYPE_SCRIPT.equals(getName()))
				return TYPE_ECL;

			return ITypedElement.TEXT_TYPE;
		}

		public Object[] getChildren() {
			return null;
		}

		public InputStream getContents() {
			return new ByteArrayInputStream(fContents.getBytes(StandardCharsets.UTF_8));
		}

		String getStringContents() {
			return fContents;
		}

		void setStringContents(String contents) {
			fContents = contents;
			if (fContents == null) {
				fContents = "";
			}
		}
	}

	static class ScenarioRoot extends ScenarioPart {

		private final HashMap<String, ScenarioPart> fChildren = new HashMap<String, ScenarioPart>(
				10);

		ScenarioRoot(String name) {
			super(name);
		}

		@Override
		public String getType() {
			if (CONTENT_TYPE_SCENARIO.equals(getName())) {
				return ITypedElement.TEXT_TYPE;
			}

			return ITypedElement.FOLDER_TYPE;
		}

		@Override
		public Object[] getChildren() {
			Object[] children = new Object[fChildren.size()];
			Iterator<ScenarioPart> iter = fChildren.values().iterator();
			for (int i = 0; iter.hasNext(); i++)
				children[i] = iter.next();
			return children;
		}

		ScenarioRoot createScenarioContainer(String name) {
			ScenarioRoot scenario = new ScenarioRoot(name);
			fChildren.put(name, scenario);
			return scenario;
		}

		ScenarioPart createPartContainer(String type) {
			ScenarioPart part = new ScenarioPart(type);
			fChildren.put(type, part);
			return part;
		}
	}

}
