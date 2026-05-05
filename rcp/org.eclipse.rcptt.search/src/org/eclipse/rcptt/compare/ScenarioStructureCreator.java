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

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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

	/** Prefix that starts every MIME boundary line in RCPTT plain-text files. */
	private static final String MIME_BOUNDARY_PREFIX = "\n------=_"; //$NON-NLS-1$

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
					IDocument document = new Document(scenarioContent);
					// Find offsets of each section in the raw file text so that
					// clicking items in the Outline view navigates the text viewer
					// to the corresponding section (analogous to JDT's behavior).
					int nameOffset = findHeaderOffset(scenarioContent, "Element-Name"); //$NON-NLS-1$
					int tagsOffset = findHeaderOffset(scenarioContent, "Tags"); //$NON-NLS-1$
					int extRefOffset = findHeaderOffset(scenarioContent, "External-Reference"); //$NON-NLS-1$
					int descOffset = findBodyOffset(scenarioContent, ".description"); //$NON-NLS-1$
					int scriptOffset = findBodyOffset(scenarioContent, ".content"); //$NON-NLS-1$
					// Length 1 is sufficient: TextMergeViewer.findDiff() only uses the
					// offset to locate the nearest text diff; the length is irrelevant
					// for navigation purposes.
					final int rangeLength = 1;
					// Root node
					ScenarioRoot root = new ScenarioRoot("", document, 0, 0); //$NON-NLS-1$
					// Scenario node
					ScenarioRoot scenario = root
							.createScenarioContainer(CONTENT_TYPE_SCENARIO, document, 0, scenarioContent.length());
					scenario.setStringContents(scenarioContent);
					// Name
					ScenarioPart name = scenario
							.createPartContainer(CONTENT_TYPE_NAME, document, nameOffset, rangeLength);
					name.setStringContents(sc.getName());
					// Tags
					ScenarioPart tags = scenario
							.createPartContainer(CONTENT_TYPE_TAGS, document, tagsOffset, rangeLength);
					tags.setStringContents(sc.getTags());
					// External references
					ScenarioPart extRef = scenario
							.createPartContainer(CONTENT_TYPE_EXTERNALREF, document, extRefOffset, rangeLength);
					extRef.setStringContents(sc.getExternalReference());
					// Description
					ScenarioPart desc = scenario
							.createPartContainer(CONTENT_TYPE_DESCRIPTION, document, descOffset, rangeLength);
					desc.setStringContents(sc.getDescription());
					// Script
					ScenarioPart script = scenario
							.createPartContainer(CONTENT_TYPE_SCRIPT, document, scriptOffset, rangeLength);
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

	/**
	 * Finds the character offset of a header attribute line (e.g. "Element-Name: ")
	 * within the raw scenario file text. Returns 0 if not found, which causes
	 * navigation to fall back to the beginning of the file - an acceptable
	 * degradation for sections that are not present in the file.
	 */
	private static int findHeaderOffset(String text, String attributeKey) {
		// Restrict search to the header section (before the first MIME boundary)
		int bodyStart = text.indexOf(MIME_BOUNDARY_PREFIX);
		String header = bodyStart >= 0 ? text.substring(0, bodyStart) : text;
		String pattern = "\n" + attributeKey + ":"; //$NON-NLS-1$ //$NON-NLS-2$
		int idx = header.indexOf(pattern);
		if (idx >= 0) {
			return idx + 1; // skip the leading newline, point to start of "Key: " line
		}
		// Try at the very start of text (no leading newline)
		if (header.startsWith(attributeKey + ":")) { //$NON-NLS-1$
			return 0;
		}
		return 0;
	}

	/**
	 * Finds the character offset of a MIME body part identified by its
	 * "Entry-Name" within the raw scenario file text. Returns 0 if not found,
	 * which causes navigation to fall back to the beginning of the file - an
	 * acceptable degradation for body parts that are not present in the file.
	 */
	private static int findBodyOffset(String text, String entryName) {
		String pattern = "Entry-Name: " + entryName; //$NON-NLS-1$
		int idx = text.indexOf(pattern);
		if (idx >= 0) {
			// Return the start of the MIME boundary line (------=_...) before this entry
			int boundaryStart = text.lastIndexOf(MIME_BOUNDARY_PREFIX, idx);
			if (boundaryStart >= 0) {
				return boundaryStart + 1; // skip the leading newline
			}
			return idx;
		}
		return 0;
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

	/**
	 * A structure node for a scenario element that extends {@link DocumentRangeNode} so
	 * that the Eclipse compare framework can navigate the text viewer to the
	 * corresponding position in the raw file when the user clicks on this node in
	 * the Outline view (analogous to JDT's {@code JavaNode} / {@code DocumentRangeNode}
	 * approach).
	 */
	static class ScenarioPart extends DocumentRangeNode implements ITypedElement, IStreamContentAccessor {

		private String fContents;

		ScenarioPart(String name, IDocument doc, int start, int length) {
			super(0, name, doc, start, length);
		}

		@Override
		public String getName() {
			return getId();
		}

		@Override
		public String getType() {
			if (CONTENT_TYPE_SCRIPT.equals(getName()))
				return TYPE_ECL;

			return ITypedElement.TEXT_TYPE;
		}

		@Override
		public Image getImage() {
			if (CONTENT_TYPE_SCENARIO.equals(getName())) {
				return Images.getImage(Images.SCENARIO);
			}
			if (CONTENT_TYPE_SCRIPT.equals(getName())) {
				return Images.getImage(Images.PANEL_SCENARIO);
			}
			return CompareUI.getImage(getType());
		}

		/** Returns the extracted section content (not the raw document range). */
		@Override
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

		ScenarioRoot(String name, IDocument doc, int start, int length) {
			super(name, doc, start, length);
		}

		@Override
		public String getType() {
			if (CONTENT_TYPE_SCENARIO.equals(getName())) {
				return ITypedElement.TEXT_TYPE;
			}

			return ITypedElement.FOLDER_TYPE;
		}

		ScenarioRoot createScenarioContainer(String name, IDocument doc, int start, int length) {
			ScenarioRoot child = new ScenarioRoot(name, doc, start, length);
			addChild(child);
			return child;
		}

		ScenarioPart createPartContainer(String type, IDocument doc, int start, int length) {
			ScenarioPart part = new ScenarioPart(type, doc, start, length);
			addChild(part);
			return part;
		}
	}

}
