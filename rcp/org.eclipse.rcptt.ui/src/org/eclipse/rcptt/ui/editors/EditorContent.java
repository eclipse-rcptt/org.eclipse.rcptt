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

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.controls.ContextsTable;
import org.eclipse.rcptt.ui.controls.DescriptionComposite;
import org.eclipse.rcptt.ui.controls.SectionWithToolbar;
import org.eclipse.rcptt.ui.controls.VerificationsTable;
import org.eclipse.rcptt.ui.editors.NamedElementEditorActions.INamedElementActions;
import org.eclipse.rcptt.ui.editors.ecl.EclSourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.views.properties.IPropertySource;

public class EditorContent implements INamedElementActions {

	private final EditorHeader header;
	private final IPropertySource scenarioPropertySource;

	private ScriptSection script;
	private EclSourceViewer viewer;

	private Section contextSection;
	private ContextsTable contextTable;

	private Section descriptionSection;

	private ScenarioProperties propertiesSection;

	private ExternalReference externalRef;
	private boolean scenarioEditor;
	private final boolean supportRefs;

	private VerificationsTable verificationsTable;
	private Section verificationsSection;

	public EditorContent(EditorHeader header, boolean refs, boolean scenario) {
		this.header = header;
		this.scenarioPropertySource = createPropertySource();
		this.supportRefs = refs;
		this.scenarioEditor = scenario;
	}

	public ScriptSection getScriptSection() {
		return script;
	}

	public Section getVerificationsSection() {
		return verificationsSection;
	}

	public IQ7NamedElement getModel() {
		return header.getModel();
	}

	public NamedElement getElement() {
		return header.getElement();
	}

	public void createSections(Composite parent, final FormToolkit toolkit) {
		descriptionComposite = new DescriptionComposite(getElement()) {
			@Override
			public void createControl(Composite parent) {
				super.createControl(parent);
				if (supportRefs) {
					createExternalReference((Composite) getControl(), toolkit);
				}
			}
		};
		descriptionSection = new SectionWithToolbar(descriptionComposite,
				Section.TITLE_BAR | Section.TWISTIE | getDescriptionFlags()).create(parent, toolkit);
		// descriptionControl = descriptionComposite.getDescriptionControl();
		// TextViewer descrViewer = descriptionComposite.getDescriptionViewer();
		if (supportRefs) {
			try {
				contextTable = new ContextsTable((ITestCase) getModel());
			} catch (ModelException e) {
				Q7UIPlugin.log(e);
			}
			contextSection = new SectionWithToolbar(contextTable, Section.TITLE_BAR | Section.TWISTIE).create(parent,
					toolkit);
		}

		if (scenarioEditor) {

			try {
				NamedElement namedElement = getModel().getNamedElement();
				if (namedElement instanceof Scenario) {
					propertiesSection = new ScenarioProperties((Scenario) namedElement, toolkit);

					new SectionWithToolbar(propertiesSection,
							Section.TITLE_BAR | Section.TWISTIE).create(parent, toolkit);
				}
			} catch (ModelException e) {
				Q7UIPlugin.log(e);
			}

			script = new ScriptSection();
			script.create(parent, toolkit);
		}

		if (supportRefs) {
			try {
				verificationsTable = new VerificationsTable((ITestCase) getModel());
			} catch (ModelException e) {
				Q7UIPlugin.log(e);
			}

			verificationsSection = new SectionWithToolbar(verificationsTable, Section.TITLE_BAR | Section.TWISTIE)
					.create(parent, toolkit);
		}
	}

	protected int getDescriptionFlags() {
		return 0;
	}

	public void setViewer(EclSourceViewer viewer) {
		this.viewer = viewer;
	}

	public void setSelectionProvider(IWorkbenchPartSite site) {
		site.setSelectionProvider(new ISelectionProvider() {
			public void setSelection(ISelection selection) {
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public ISelection getSelection() {
				if (scenarioPropertySource != null) {
					return new StructuredSelection(scenarioPropertySource);
				}
				return StructuredSelection.EMPTY;
			}

			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}
		});
	}

	public void showSearchMatch(int scenarioPart, int offset, int length) {
		final int SCRIPT = 0;
		final int DESCRIPTION = 1;
		final int TAGS = 2;
		final int CONTEXTS = 3;

		if (scenarioPart == SCRIPT) {
			if (!script.getSection().isExpanded()) {
				script.getSection().setExpanded(true);
			}
			if (viewer != null) {
				viewer.getTextWidget().setFocus();
				viewer.revealRange(offset, length);
				viewer.setSelectedRange(offset, length);
			}
		} else if (scenarioPart == DESCRIPTION) {
			if (!descriptionSection.isExpanded()) {
				descriptionSection.setExpanded(true);
			}
			descriptionComposite.getDescriptionControl().setFocus();
			descriptionComposite.getDescriptionControl().setSelection(offset, offset + length);
		} else if (scenarioPart == TAGS) {
			StyledText tagsControl = header.getTagsControl();
			tagsControl.setFocus();
			tagsControl.setSelection(offset, offset + length);
		} else if (scenarioPart == CONTEXTS) {
			if (contextSection != null) {
				if (!contextSection.isExpanded()) {
					contextSection.setExpanded(true);
				}
			}
		}
	}

	private final class ExternalReference {

		private final TextViewer textViewer;
		private final INamedElementActions actions;

		public ExternalReference(final TextViewer textViewer, final Hyperlink externalRefLabel) {
			this.textViewer = textViewer;
			externalRefLabel.setToolTipText(EXT_REF_TOOLTIP);
			externalRefLabel.setUnderlined(true);
			externalRefLabel.addHyperlinkListener(new IHyperlinkListener() {

				public void linkExited(HyperlinkEvent e) {
				}

				public void linkEntered(HyperlinkEvent e) {
				}

				public void linkActivated(HyperlinkEvent e) {
					if (!"".equals(externalRef.getControl().getText().trim())) { //$NON-NLS-1$
						Program.launch(externalRef.getControl().getText());
					}
				}
			});

			getControl().setToolTipText(EXT_REF_TOOLTIP);
			getControl().addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					externalRefLabel.setEnabled(!"".equals(getControl() //$NON-NLS-1$
							.getText().trim()));
				}
			});
			actions = new TextViewerActions(textViewer);
		}

		public INamedElementActions getActions() {
			return actions;
		}

		public StyledText getControl() {
			return textViewer.getTextWidget();
		}
	}

	private void createExternalReference(Composite parent, FormToolkit toolkit) {
		final Composite composite = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(composite);

		final Hyperlink externalRefLabel = toolkit.createHyperlink(composite,
				Messages.ScenarioEditorPage_ExternalReferenceLabel, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(externalRefLabel);

		final TextViewer textViewer = new TextViewer(composite,
				SWT.BORDER | SWT.SINGLE | toolkit.getBorderStyle() | toolkit.getOrientation());
		toolkit.adapt(textViewer.getTextWidget());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textViewer.getTextWidget());

		externalRef = new ExternalReference(textViewer, externalRefLabel);
		update(getModel(), getElement());
	}

	private IPropertySource createPropertySource() {
		return (IPropertySource) Platform.getAdapterManager().getAdapter(getElement(), IPropertySource.class);
	}

	private static final String EXT_REF_TOOLTIP = Messages.ScenarioEditorPage_ExternalReferenceToolTip;

	private DescriptionComposite descriptionComposite;
	private Binding externalRefBinding;

	public void gotoMarker(IMarker marker) {
		try {
			Object lineObj = marker.getAttribute(IMarker.LINE_NUMBER);
			if (lineObj instanceof Integer) {
				int line = (Integer) lineObj;
				if (!script.getSection().isExpanded()) {
					script.getSection().setExpanded(true);
				}
				if (viewer != null) {
					viewer.getTextWidget().setFocus();
					IRegion region = viewer.getDocument().getLineInformation(line - 1);
					viewer.setSelection(new TextSelection(region.getOffset(), region.getLength()), true);
				}
			}
		} catch (Exception e) {
			Q7UIPlugin.log(e);
		}
	}

	public boolean isInFocus() {
		return descriptionComposite.getDescriptionControl().isFocusControl()
				|| (externalRef != null && externalRef.getControl().isFocusControl())
				|| (contextTable != null && contextTable.getControl().isFocusControl())
				|| (verificationsTable != null && verificationsTable.getControl().isFocusControl())
				|| header.isInFocus() || (propertiesSection != null && propertiesSection.getControl().isFocusControl());
	}

	public void doSelectAll() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			descriptionComposite.getDescriptionControl().selectAll();
		} else if (externalRef.getControl().isFocusControl()) {
			externalRef.getControl().selectAll();
		} else if (contextTable != null && contextTable.getControl().isFocusControl()) {
			((Table) contextTable.getControl()).selectAll();
		} else if (verificationsTable != null && verificationsTable.getControl().isFocusControl()) {
			((Table) verificationsTable.getControl()).selectAll();
		}
	}

	public void copy() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			descriptionComposite.getActions().copy();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			externalRef.getActions().copy();
		} else if (contextTable != null && contextTable.getControl().isFocusControl()) {
			if (contextTable.canCopy()) {
				contextTable.copy();
			}
		} else if (verificationsTable != null && verificationsTable.getControl().isFocusControl()) {
			if (verificationsTable.canCopy()) {
				verificationsTable.copy();
			}
		} else if (header.isInFocus()) {
			header.copy();
		}
	}

	public boolean canCopy() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			return descriptionComposite.getActions().canCopy();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			return externalRef.getActions().canCopy();
		} else if (contextTable != null && contextTable.getControl().isFocusControl()) {
			return contextTable.canCopy();
		} else if (verificationsTable != null && verificationsTable.getControl().isFocusControl()) {
			return verificationsTable.canCopy();
		} else if (header.isInFocus()) {
			return header.canCopy();
		}
		return false;
	}

	public boolean canPaste() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			return descriptionComposite.getActions().canPaste();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			return externalRef.getActions().canPaste();
		} else if (contextTable != null && contextTable.getControl().isFocusControl()) {
			return contextTable.canPaste();
		} else if (verificationsTable != null && verificationsTable.getControl().isFocusControl()) {
			return verificationsTable.canPaste();
		} else if (header.isInFocus()) {
			return header.canPaste();
		}
		return false;
	}

	public boolean canCut() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			return descriptionComposite.getActions().canCut();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			return externalRef.getActions().canCut();
		} else if (contextTable != null && contextTable.getControl().isFocusControl()) {
			return contextTable.canCut();
		} else if (verificationsTable != null && verificationsTable.getControl().isFocusControl()) {
			return verificationsTable.canCut();
		} else if (header.isInFocus()) {
			return header.canCut();
		}
		return false;
	}

	public void paste() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			descriptionComposite.getActions().paste();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			externalRef.getActions().paste();
		} else if (contextTable != null && contextTable.getControl().isFocusControl()) {
			if (contextTable.canPaste()) {
				contextTable.paste();
			}
		} else if (verificationsTable != null && verificationsTable.getControl().isFocusControl()) {
			if (verificationsTable.canPaste()) {
				verificationsTable.paste();
			}
		} else if (header.isInFocus()) {
			header.paste();
		}
	}

	public void cut() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			descriptionComposite.getActions().cut();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			externalRef.getActions().cut();
		} else if (contextTable != null && contextTable.getControl().isFocusControl()) {
			if (contextTable.canCut()) {
				contextTable.cut();
			}
		} else if (verificationsTable != null && verificationsTable.getControl().isFocusControl()) {
			if (verificationsTable.canCut()) {
				verificationsTable.cut();
			}
		} else if (header.isInFocus()) {
			header.cut();
		}
	}

	public void update(IQ7NamedElement model, NamedElement element) {
		if (externalRefBinding != null) {
			externalRefBinding.dispose();
		}
		if (externalRef != null) {
			IObservableValue<String> externalReference = EMFProperties.value(ScenarioPackage.Literals.SCENARIO__EXTERNAL_REFERENCE).observe(getElement());
			externalRefBinding = header.getDataBindingContext().bindValue(
					WidgetProperties.text(SWT.Modify).observe(externalRef.getControl()),
					externalReference);
		}
		descriptionComposite.update(element);
		if (contextTable != null) {
			contextTable.update(element);
		}
		if (propertiesSection != null) {
			propertiesSection.update((Scenario) element);
		}
		if (verificationsTable != null) {
			verificationsTable.update(element);
		}
	}

	public void doTextCommand(int fAction) {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			descriptionComposite.getDescriptionControl().invokeAction(fAction);
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			externalRef.getControl().invokeAction(fAction);
		}
	}

	public TextViewer getTextViewer() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			return descriptionComposite.getDescriptionViewer();
		}
		return null;
	}

	public boolean canUndo() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			return descriptionComposite.getActions().canUndo();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			return externalRef.getActions().canUndo();
		}
		return false;
	}

	public boolean canRedo() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			return descriptionComposite.getActions().canRedo();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			return externalRef.getActions().canRedo();
		}
		return false;
	}

	public void undo() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			descriptionComposite.getActions().undo();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			externalRef.getActions().undo();
		}
	}

	public void redo() {
		if (descriptionComposite.getDescriptionControl().isFocusControl()) {
			descriptionComposite.getActions().redo();
		} else if (externalRef != null && externalRef.getControl().isFocusControl()) {
			externalRef.getActions().redo();
		}
	}
}
