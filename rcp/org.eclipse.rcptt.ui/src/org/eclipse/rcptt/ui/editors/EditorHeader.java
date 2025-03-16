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

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.eclipse.rcptt.ui.launching.LaunchUtils.runResource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.ScenarioPackage;
import org.eclipse.rcptt.core.tags.Tag;
import org.eclipse.rcptt.core.utils.TagsUtil;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.ILaunchListener;
import org.eclipse.rcptt.launching.LaunchAdapter;
import org.eclipse.rcptt.launching.Q7Launcher;
import org.eclipse.rcptt.ui.commons.OneSelectionListener;
import org.eclipse.rcptt.ui.launching.LaunchUtils;
import org.eclipse.rcptt.ui.tags.SelectTagDialog;
import org.eclipse.rcptt.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.forms.widgets.FormImages;

@SuppressWarnings("restriction")
public abstract class EditorHeader {

	protected DataBindingContext dbc = new DataBindingContext();
	protected StyledText nameText;
	protected ControlDecoration nameDecoration;
	protected Button addTag;
	protected StyledText tagsControl;

	private Button recordButton;
	private Button replayButton;

	private IQ7NamedElement model;
	private NamedElement element;
	private Binding nameBinding;
	private Binding tagsBinging;

	public EditorHeader(IQ7NamedElement model, NamedElement element) {
		this.model = model;
		this.element = element;
	}

	public IQ7NamedElement getModel() {
		return model;
	}

	public NamedElement getElement() {
		return element;
	}

	public DataBindingContext getDataBindingContext() {
		return dbc;
	}

	public StyledText getTagsControl() {
		return tagsControl;
	}

	public Button getRecordButton() {
		return recordButton;
	}

	public Button getReplayButton() {
		return replayButton;
	}

	public Composite createContent(Composite parent, FormToolkit toolkit) {
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0)
				.equalWidth(true).applyTo(parent);

		createGeneral(parent, toolkit);

		Composite composite = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().margins(5, 5).spacing(0, 0)
				.equalWidth(true).applyTo(composite);
		update(this.model, this.element);

		return composite;
	}

	protected void createGeneral(Composite parent, FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(parent);

		GridLayoutFactory.swtDefaults().numColumns(getTotalColumns())
				.spacing(10, 4).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(composite);

		createNameControl(composite, toolkit);
		recordButton = createRecordButton(composite, toolkit);
		createTagControl(composite, toolkit);
		replayButton = createReplayButton(composite, toolkit);

		int height = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

		Color top = toolkit.getColors().getColor(IFormColors.H_GRADIENT_END);
		Color bot = toolkit.getColors().getColor(IFormColors.H_GRADIENT_START);

		Image gradientImage = FormImages.getInstance().getGradient(
				new Color[] { top, bot }, new int[] { 100 }, height, true,
				toolkit.getColors().getColor(IFormColors.TB_BG),
				composite.getDisplay());
		// No need to dispose the gradient image cause it's created and stored
		// in resource manager
		composite.setBackgroundImage(gradientImage);

		Composite c1 = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 1).span(4, 1)
				.grab(true, false).applyTo(c1);
		c1.setBackground(toolkit.getColors().getColor(
				IFormColors.H_BOTTOM_KEYLINE1));

		Composite c2 = toolkit.createComposite(parent);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 1).span(4, 1)
				.grab(true, false).applyTo(c2);
		c2.setBackground(toolkit.getColors().getColor(
				IFormColors.H_BOTTOM_KEYLINE2));

		// if (!Platform.getOS().equals(Platform.OS_MACOSX)) {
		// // We need to simulate transparent background for labels
		// composite.layout();
		// setBackgroundToHeaderLabel(nameLabel, gradientImage);
		// setBackgroundToHeaderLabel(tagsLabel, gradientImage);
		// }
	}

	protected int getTotalColumns() {
		return 4;
	}

	public void focus() {
		nameText.setFocus();
	}

	protected void createNameControl(Composite parent, FormToolkit toolkit) {
		createLabel(parent, toolkit,
				Messages.NamedElementEditorPage_NameLabel);
		nameText = createWrappedText(parent, toolkit, element.getName(), 2);
		nameText.setBackground(null);
		nameDecoration = new ControlDecoration(nameText, SWT.TOP | SWT.LEFT);

		FieldDecoration errorFieldIndicator = FieldDecorationRegistry
				.getDefault().getFieldDecoration(
						FieldDecorationRegistry.DEC_ERROR);
		nameDecoration.setImage(errorFieldIndicator.getImage());

		nameDecoration.setMarginWidth(2);
		nameDecoration.setShowHover(true);
		nameDecoration.hide();
	}

	protected Menu createTextMenu(final StyledText text) {
		Menu menu = new Menu(text);

		MenuItem copy = new MenuItem(menu, SWT.PUSH);
		copy.setText("Copy");
		copy.addSelectionListener(new OneSelectionListener() {
			@Override
			public void selected(SelectionEvent e) {
				if (TextUtils.canCopy(text))
					TextUtils.copy(text);
			}
		});

		MenuItem paste = new MenuItem(menu, SWT.PUSH);
		paste.setText("Paste");
		paste.addSelectionListener(new OneSelectionListener() {
			@Override
			public void selected(SelectionEvent e) {
				if (TextUtils.canPaste(text))
					TextUtils.paste(text);
			}
		});

		MenuItem cut = new MenuItem(menu, SWT.PUSH);
		cut.setText("Cut");
		cut.addSelectionListener(new OneSelectionListener() {
			@Override
			public void selected(SelectionEvent e) {
				if (TextUtils.canCut(text))
					TextUtils.cut(text);
			}
		});

		return menu;
	}

	protected StyledText createWrappedText(Composite parent,
			FormToolkit toolkit, String text, int hSpan) {
		Composite composite = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().equalWidth(true).applyTo(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.span(hSpan, 1).grab(true, false).applyTo(composite);
		StyledText textControl = new StyledText(composite, SWT.BORDER
				| SWT.SINGLE | toolkit.getBorderStyle()
				| toolkit.getOrientation());
		textControl.setText(text);
		toolkit.adapt(textControl);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).hint(111, SWT.DEFAULT).applyTo(textControl);
		textControl.setBackground(null);

		textControl.setMenu(createTextMenu(textControl));

		return textControl;
	}

	protected void createTagControl(Composite parent, FormToolkit toolkit) {
		createLabel(parent, toolkit,
				Messages.NamedElementEditorPage_TagsLabel);
		Composite tags = toolkit.createComposite(parent, SWT.NONE);
		tags.setBackground(null);
		GridLayoutFactory.fillDefaults().spacing(3, 0).numColumns(2)
				.margins(0, 0).applyTo(tags);
		GridDataFactory.fillDefaults().grab(true, false)
				.span(getTagsSpacing(), 1).applyTo(tags);

		tagsControl = new StyledText(tags, SWT.BORDER | SWT.SINGLE
				| toolkit.getBorderStyle() | toolkit.getOrientation());
		toolkit.adapt(tagsControl);
		tagsControl.setBackground(null);
		tagsControl.setMenu(createTextMenu(tagsControl));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(tagsControl);
		addTag = toolkit.createButton(tags, "", SWT.PUSH); //$NON-NLS-1$
		addTag.setToolTipText(Messages.NamedElementEditorPage_AddTagButton);
		addTag.setBackground(null);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER)
				.applyTo(addTag);
		addTag.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJ_ADD));
		addTag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final List<String> existingTags = TagsUtil.extractTags(element
						.getTags());
				final SelectTagDialog dialog = new SelectTagDialog(tagsControl
						.getShell(), existingTags);

				if (dialog.open() == SelectTagDialog.OK) {
					final StringBuilder sb = new StringBuilder(element
							.getTags());
					boolean first = existingTags.isEmpty();
					if (!dialog.getResult().isEmpty()) {
						for (final Tag tag : dialog.getResult()) {
							if (!first) {
								sb.append(", "); //$NON-NLS-1$
							} else {
								first = false;
							}
							sb.append(tag.getPath());
						}
						element.setTags(sb.toString());
					} else if (dialog.getTagName() != null) {
						if (sb.toString().length() != 0) {
							sb.append(", "); //$NON-NLS-1$
						}
						sb.append(dialog.getTagName().trim());
						element.setTags(sb.toString());
					}
				}
			}
		});
	}

	protected int getTagsSpacing() {
		return 2;
	}

	protected Button createRecordButton(Composite composite, FormToolkit toolkit) {
		Button button = toolkit.createButton(composite,
				Messages.NamedElementEditorPage_RecordButton, SWT.PUSH);
		button.setImage(Images.getImage(Images.RECORD));
		button.setBackground(null);
		GridDataFactory.fillDefaults().applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				record();
			}
		});
		return button;
	}

	protected Button createReplayButton(Composite composite, FormToolkit toolkit) {
		Button button = toolkit.createButton(composite,
				Messages.NamedElementEditorPage_ReplayButton, SWT.PUSH);
		button.setImage(DebugUITools.getImage(IDebugUIConstants.IMG_ACT_RUN));
		button.setBackground(null);
		GridDataFactory.fillDefaults().applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if ((e.stateMask & SWT.SHIFT) != 0)
					multiReplay();
				else
					replay();
			}
		});
		return button;
	}

	protected Label createLabel(Composite parent, FormToolkit toolkit,
			final String label) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);
		labelControl.setForeground(toolkit.getColors().getColor(
				IFormColors.TITLE));
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
				.applyTo(labelControl);
		labelControl.setBackground(null);
		return labelControl;
	}

	protected void setBackgroundToHeaderLabel(Label label, Image backgroundImage) {
		Rectangle bounds = label.getBounds();
		bounds.x = 0;
		bounds.width = 1;
		final Image image = new Image(label.getDisplay(), bounds);
		GC gc = new GC(image);
		gc.drawImage(backgroundImage, bounds.x, bounds.y, bounds.width,
				bounds.height, 0, 0, bounds.width, bounds.height);
		gc.dispose();
		label.setBackgroundImage(image);
		label.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}
		});
	}

	protected abstract void record();

	protected void replay() {
		IResource res = model.getResource();
		if (res == null)
			return;
		LaunchUtils.launchContext(new IResource[] { res }, "run"); //$NON-NLS-1$
	}

	private static int getReplayCount() {
		InputDialog in = new InputDialog(null,
				Messages.NamedElementEditorPage_RunCounterDialogTitle,
				Messages.NamedElementEditorPage_RunCounterDialogLabel,
				Messages.NamedElementEditorPage_RunCounterDialogDefaultValue,
				new IInputValidator() {
					public String isValid(String newText) {
						try {
							int value = Integer.parseInt(newText);
							if (value <= 0) {
								return Messages.NamedElementEditorPage_RunCounterDialogNeagtiveIntErrorMsg;
							}
						} catch (Throwable e) {
							return Messages.NamedElementEditorPage_RunCounterDialogIncorrectIntErrorMsg;
						}
						return null;
					}
				});
		if (in.open() != Window.OK) {
			return -1;
		}
		return Integer.parseInt(in.getValue());
	}

	private static boolean resourceSaved(IResource res) {
		try {
			return LaunchUtils.saveChanges(new IResource[] { res });
		} catch (ModelException e) {
			return false;
		}
	}

	protected void multiReplay() {
		final IResource res = model.getResource();
		if (res == null) {
			return;
		}
		int count = getReplayCount();
		if (count == -1) {
			return;
		}
		if (!resourceSaved(res)) {
			return;
		}

		final AtomicInteger countAtom = new AtomicInteger(count);
		final Job execJob = new Job("Times execute") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				runResource(res);
				return Status.OK_STATUS;
			}
		};

		final ILaunchListener listener = new LaunchAdapter() {

			public void launchStatusChanged(IExecutable... executable) {
				for (IExecutable e : executable) {
					if (!e.getResultStatus().isOK()) {
						countAtom.set(0);
					}
				}
			}

			public void finished() {
				if (countAtom.decrementAndGet() > 0) {
					execJob.schedule(500);
				}
			}
		};

		Job cancelationJob = new Job("Times execution") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				int count = countAtom.get();
				monitor.beginTask("Times", count); //$NON-NLS-1$
				while (!monitor.isCanceled() && (count = countAtom.get()) > 0) {
					sleepUninterruptibly(100, MILLISECONDS);
					monitor.worked(count - countAtom.get());
				}
				countAtom.set(0);
				Q7Launcher.getInstance().removeListener(listener);
				return Status.OK_STATUS;
			}
		};

		cancelationJob.schedule();
		cancelationJob.setPriority(Job.LONG);
		Q7Launcher.getInstance().addListener(listener);
		execJob.schedule();

	}

	public boolean isInFocus() {
		if (nameText.isFocusControl() || tagsControl.isFocusControl()) {
			return true;
		}
		return false;
	}

	public void doSelectAll() {
		if (nameText.isFocusControl()) {
			nameText.selectAll();
		} else if (tagsControl.isFocusControl()) {
			tagsControl.selectAll();
		}
	}

	public void copy() {
		if (nameText.isFocusControl()) {
			TextUtils.copy(nameText);
		} else if (tagsControl.isFocusControl()) {
			TextUtils.copy(tagsControl);
		}
	}

	public boolean canCopy() {
		if (nameText.isFocusControl()) {
			return TextUtils.canCopy(nameText);
		} else if (tagsControl.isFocusControl()) {
			return TextUtils.canCopy(tagsControl);
		}
		return false;
	}

	public boolean canPaste() {
		if (nameText.isFocusControl()) {
			return TextUtils.canPaste(nameText);
		} else if (tagsControl.isFocusControl()) {
			return TextUtils.canPaste(tagsControl);
		}
		return false;
	}

	public boolean canCut() {
		if (nameText.isFocusControl()) {
			return TextUtils.canCut(nameText);
		} else if (tagsControl.isFocusControl()) {
			return TextUtils.canCut(tagsControl);
		}
		return false;
	}

	public void paste() {
		if (nameText.isFocusControl()) {
			TextUtils.paste(nameText);
		} else if (tagsControl.isFocusControl()) {
			TextUtils.paste(tagsControl);
		}
	}

	public void cut() {
		if (nameText.isFocusControl()) {
			TextUtils.cut(nameText);
		} else if (tagsControl.isFocusControl()) {
			TextUtils.cut(tagsControl);
		}
	}

	private IStatus nameFail(String message) {
		nameDecoration.setDescriptionText(message);
		nameDecoration.show();
		return Status.CANCEL_STATUS;
	}

	private IStatus nameWin() {
		nameDecoration.hide();
		return Status.OK_STATUS;
	}

	public void update(final IQ7NamedElement model, NamedElement element) {
		this.model = model;
		this.element = element;
		if (nameText != null) {
			if (nameBinding != null) {
				nameBinding.dispose();
			}

			UpdateValueStrategy<String, String> strategy = new UpdateValueStrategy<>();
			strategy.setBeforeSetValidator(new IValidator<String>() {
				public IStatus validate(String value) {
					String text = value;

					if (StringUtils.isBlank(text))
						return nameFail("Name must be non-blank string.");

					// that is toooo slooooow
					// IQ7NamedElement[] found = Q7SearchCore.findByName(text,
					// new OneProjectScope(model.getQ7Project()),
					// new NullProgressMonitor());
					// for (IQ7NamedElement e : found)
					// if (e != model && e.getClass() == model.getClass())
					// return nameFail("Name is already in use.");

					return nameWin();
				}
			});
			IObservableValue<String> name = EMFObservables.observeValue(element,
					ScenarioPackage.eINSTANCE.getNamedElement_Name());
			nameBinding = dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(nameText), name,
					strategy, null);
		}

		if (tagsControl != null) {
			if (tagsBinging != null) {
				tagsBinging.dispose();
			}
			IObservableValue<String> tags = EMFProperties.value(
					ScenarioPackage.Literals.NAMED_ELEMENT__TAGS)
					.observe(element);
			tagsBinging = dbc.bindValue(
					WidgetProperties.text(SWT.Modify).observe(tagsControl),
					tags);
		}
	}

	public void doTextCommand(int fAction) {

		if (nameText.isFocusControl()) {
			nameText.invokeAction(fAction);
		} else if (tagsControl.isFocusControl()) {
			tagsControl.invokeAction(fAction);
		}
	}

	public boolean canUndo() {
		return false;
	}

	public boolean canRedo() {
		return false;
	}

	public void undo() {
	}

	public void redo() {
	}

	protected StyledText getNameControl() {
		return nameText;
	}
}
