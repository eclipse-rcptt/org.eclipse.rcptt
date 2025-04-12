/********************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xored Software Inc - initial API and implementation
 ********************************************************************************/
package org.eclipse.rcptt.ui.editors;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.internal.ui.Q7UIPlugin;
import org.eclipse.rcptt.ui.controls.SuggestionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@SuppressWarnings("restriction")
public class PropertyCellEditor extends TextCellEditor {

	private Text text;
	private Shell shell;
	private List list;
	private Shell description;
	private Browser browser;
	private Button button;

	private java.util.List<SuggestionItem> suggestions;

	private String descriptionData;

	private boolean filterEnabled;
	private boolean valueIsSelectedFromList = false;

	private int listMaxHeight;
	private int descWidth;
	private int descHeight;

	private static final int LIST_MAX_HEIGHT = 200;
	private static final int DESC_WIDTH = 350;
	private static final int DESC_HEIGHT = 220;

	public PropertyCellEditor(Composite parent, java.util.List<SuggestionItem> suggestions) {
		super(parent);
		this.suggestions = suggestions;
		this.filterEnabled = false;
		text = (Text) super.getControl();
		createPopup(text);
		createButton();
		addListeners();
	}

	public void completeEdit() {

	}

	private void createPopup(Text textField) {
		text = textField;

		int popupStyle = SWT.TOOL | SWT.ON_TOP | SWT.NO_TRIM;
		int listStyle = SWT.SINGLE | SWT.V_SCROLL;

		shell = new Shell(text.getShell(), popupStyle);
		list = new List(shell, listStyle);
		description = new Shell(text.getShell(), popupStyle);
		description.setLayout(new FormLayout());
		description.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		description.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		browser = new Browser(description, SWT.NONE);
		browser.setJavascriptEnabled(false);
		browser.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		browser.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		browser.setMenu(new Menu(description, SWT.NONE));
		browser.addOpenWindowListener(new OpenWindowListener() {
			@Override
			public void open(WindowEvent event) {
				event.required = true;
			}
		});

		listMaxHeight = LIST_MAX_HEIGHT;
		descWidth = DESC_WIDTH;
		descHeight = DESC_HEIGHT;
	}

	private void createButton() {
		Composite composite = new Composite(description, SWT.NONE);
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		composite.setLayoutData(data);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 3;
		layout.marginHeight = 3;
		layout.verticalSpacing = 3;
		layout.horizontalSpacing = 3;
		composite.setLayout(layout);

		button = new Button(composite, SWT.PUSH | SWT.FLAT);
		button.setText("Copy to the clipboard");
		button.setImage(Images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY).createImage());
		Font font = button.getFont();
		FontData fontdata = font.getFontData()[0];
		fontdata.setHeight(fontdata.getHeight() - 2);
		button.setFont(new Font(font.getDevice(), fontdata));
	}

	@Override
	public void activate() {
		super.activate();
	}

	@Override
	public void deactivate() {
		if (shell == null) {
			return;
		}
		if (!isActive()) {
			super.deactivate();
			return;
		}
		addJob();
	}

	@Override
	protected void focusLost() {
	}

	@Override
	protected boolean dependsOnExternalFocusListener() {
		return false;
	}

	private void open() {
		if (!isActive()) {
			return;
		}
		setItems(suggestions);
		setShellBounds();
		setChildElementsBounds();
		filterValues(text.getText());
		shell.setVisible(true);
		text.setFocus();
	}

	private void close() {
		if (!shell.isDisposed()) {
			shell.setVisible(false);
		}
		if (!description.isDisposed()) {
			description.setVisible(false);
		}
	}

	private void addListeners() {
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// Don't apply filtering if the value is changed by selection
				// from list
				if (valueIsSelectedFromList) {
					valueIsSelectedFromList = false;
					return;
				}
				filterValues(text.getText());
			}

		});

		text.getShell().addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {
				// shell.setFocus();
				close();
			}

			@Override
			public void controlResized(ControlEvent e) {
				// shell.setFocus();
				close();
			}

		});

		shell.addControlListener(new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				setChildElementsBounds();
			}

		});

		list.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectValueFromList(list.getSelectionIndex());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		list.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				selectValueFromList(list.getSelectionIndex());
				completeEdit();
			}
		});

		text.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
				case SWT.TRAVERSE_ARROW_NEXT:
					if (e.keyCode == SWT.ARROW_DOWN) {
						int oldIndex = list.getSelectionIndex();
						selectValueFromList(Math.min(oldIndex + 1, list.getItemCount() - 1));

						e.detail = SWT.TRAVERSE_NONE;
						e.doit = false;
					}
					break;
				case SWT.TRAVERSE_ARROW_PREVIOUS:
					if (e.keyCode == SWT.ARROW_UP) {
						int oldIndex = list.getSelectionIndex();
						selectValueFromList(Math.max(oldIndex - 1, 0));

						e.detail = SWT.TRAVERSE_NONE;
						e.doit = false;
					}
					break;
				}
			}

		});

		button.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (descriptionData != null && !descriptionData.equals("")) {
					final Clipboard clipboard = new Clipboard(Display.getCurrent());
					TextTransfer transfer = TextTransfer.getInstance();
					clipboard.setContents(new Object[] { descriptionData }, new Transfer[] { transfer });
				}
			}

		});

		FocusListener focusListener = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				if (e.widget == text) {
					if (!isVisible()) {
						open();
						return;
					}
				}
				if (text != null) {
					cancelJobs();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
//				if (e.widget == text) {
//					Rectangle area = text.getClientArea();
//					Point point = text.getDisplay().getCursorLocation();
//					Point inCongrol = text.toControl(point.x, point.y);
//					if (area.contains(inCongrol)) {
//						System.out.println("###");
////						return;
//					}
//				}
				addJob();
			}

		};
		text.addFocusListener(focusListener);
		browser.addFocusListener(focusListener);
		list.addFocusListener(focusListener);
		button.addFocusListener(focusListener);
		shell.addFocusListener(focusListener);
		description.addFocusListener(focusListener);

		Listener keyDown = new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (e.character == '\r') { // Return key
					completeEdit();
				} else {
					text.notifyListeners(SWT.KeyDown, e);
				}
			}

		};
		list.addListener(SWT.KeyDown, keyDown);
		browser.addListener(SWT.KeyDown, keyDown);
		button.addListener(SWT.KeyDown, keyDown);

		Listener traverse = new Listener() {

			@Override
			public void handleEvent(Event e) {
				text.notifyListeners(SWT.Traverse, e);
			}

		};
		list.addListener(SWT.Traverse, traverse);
		browser.addListener(SWT.Traverse, traverse);
		button.addListener(SWT.Traverse, traverse);

		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handleDefaultSelection(e);
			}
		});
	}

	private void addJob() {
		cancelJobs();
		Job job = new Job("Deactivate") {

			@Override
			public boolean belongsTo(Object family) {
				return family == PropertyCellEditor.this;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						// If user clicks on the description (or scrolls it),
						// focusGained event is not fired for browser.
						// So, we check a current focus control before closing
						// the popup.
						// On windows current control - WebSite
						// and we check control.getParent().getParent() from it.
						Control control = Display.getDefault().getFocusControl();
						if (control == browser || (control != null && control.getParent() != null
								&& control.getParent().getParent() != null
								&& control.getParent().getParent() == browser)) {
							list.setFocus();
							return;
						}
						deactivateAll();
					}
				});
				return Status.OK_STATUS;
			}

		};
		job.schedule(100);
	}

	private void cancelJobs() {
		Job.getJobManager().cancel(PropertyCellEditor.this);
	}

	private void deactivateAll() {
		if (shell != null) {
			close();
		}
		super.fireApplyEditorValue();
		super.deactivate();
	}

	public boolean isVisible() {
		return shell.getVisible();
	}

	public boolean isActive() {
		return suggestions != null && !suggestions.isEmpty();
	}

	private void selectValueFromList(int index) {
		valueIsSelectedFromList = true;
		if (0 <= index && index < list.getItemCount()) {
			String value = list.getItem(index);
			text.setText(value);
			text.selectAll();
			list.select(index);
			list.showSelection();

			SuggestionItem.SuggestionDescription desc = getDescription(value);
			if (desc != null && desc.getText() != null && !desc.getText().equals("") && desc.getHTML() != null
					&& !desc.getHTML().equals("")) {
				descriptionData = desc.getText();
				browser.setText(applyStyling(desc.getHTML()));
				description.setVisible(true);
			} else {
				description.setVisible(false);
				descriptionData = "";
				browser.setText("");
			}
		}
	}

	private void filterValues(String value) {
		if (!filterEnabled) {
			return;
		}

		if (value == null || value.trim().length() == 0) {
			value = "";
		}
		java.util.List<String> items = new ArrayList<String>();
		for (SuggestionItem suggestion : suggestions) {
			String name = suggestion.getName();
			if (name.toLowerCase().startsWith(value.toLowerCase()) || value.isEmpty()) {
				items.add(name);
			}
		}
		list.setItems(items.toArray(new String[0]));
		list.deselectAll();
	}

	private void setItems(java.util.List<SuggestionItem> suggestions) {
		Set<String> set = new TreeSet<String>();
		for (SuggestionItem suggestion : suggestions) {
			set.add(suggestion.getName());
		}
		String[] items = set.toArray(new String[0]);
		list.setItems(items);
		this.suggestions = suggestions;
	}

	private SuggestionItem.SuggestionDescription getDescription(String name) {
		for (SuggestionItem suggestion : suggestions) {
			if (name.equals(suggestion.getName())) {
				return suggestion.getDescription();
			}
		}
		return null;
	}

	private static String applyStyling(String html) {
		String styles = loadStyles();
		FontData fontData = JFaceResources.getDialogFont().getFontData()[0];
		styles = HTMLPrinter.convertTopLevelFont(styles, fontData);

		StringBuffer b = new StringBuffer(html);
		b.insert(0, "</style></head><body>");
		b.insert(0, styles);
		b.insert(0, "<html><head><style type=\"text/css\">");
		b.append("</body></html>");

		return b.toString();
	}

	private static String styles = null;

	private static String loadStyles() {
		if (styles != null)
			return styles;

		try {
			URL url = Q7UIPlugin.getDefault().getBundle().getResource("/docs.css");
			// Check for missing styles resource.
			if (url == null) {
				return styles = "";
			}
			return styles = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			Q7UIPlugin.log(e);
			return styles = "";
		}
	}

	public boolean isFilterEnabled() {
		return filterEnabled;
	}

	public void setFilterEnabled(boolean filterEnabled) {
		this.filterEnabled = filterEnabled;
	}

	public int getListMaximumHeight() {
		return listMaxHeight;
	}

	public void setListMaximumHeight(int height) {
		listMaxHeight = height;
	}

	public int getDescriptionWidth() {
		return descWidth;
	}

	public void setDescriptionWidth(int width) {
		descWidth = width;
	}

	public int getDescriptionHeight() {
		return descHeight;
	}

	public void setDescriptionHeight(int height) {
		descHeight = height;
	}

	private void setShellBounds() {
		Rectangle textBounds = text.getBounds();
		Rectangle screenBounds = shell.getDisplay().getBounds();
		Point textLocation = text.getParent().toDisplay(textBounds.x, textBounds.y);
		Point popupSize = list.computeSize(textBounds.width, SWT.DEFAULT, false);

		int shellX = textLocation.x;
		int shellY = textLocation.y;

		popupSize.x = textBounds.width;
		if (popupSize.y > listMaxHeight) {
			popupSize.y = listMaxHeight;
		}

		int spaceBelow = screenBounds.height - (textLocation.y + textBounds.height) - 30;
		int spaceAbove = textLocation.y - 30;

		if (spaceAbove > spaceBelow && popupSize.y > spaceBelow) {
			if (popupSize.y > spaceAbove) {
				popupSize.y = spaceAbove;
			}
			shellY -= popupSize.y;

		} else {
			if (popupSize.y > spaceBelow) {
				popupSize.y = spaceBelow;
			}
			shellY += textBounds.height;
		}

		shell.setBounds(shellX, shellY, popupSize.x, popupSize.y);
	}

	private void setChildElementsBounds() {
		Rectangle shellBounds = shell.getBounds();
		Rectangle screenBounds = shell.getDisplay().getBounds();
		Point buttonSize = button.getParent().getSize();

		int descX = shellBounds.x;
		int descY = shellBounds.y;

		int spaceBefore = shellBounds.x - 30;
		int spaceAfter = screenBounds.width - (shellBounds.x + shellBounds.width) - 30;

		if (spaceBefore > spaceAfter && descWidth > spaceAfter) {
			if (descWidth > spaceBefore) {
				descWidth = spaceBefore;
			}
			descX -= descWidth;

		} else {
			if (descWidth > spaceAfter) {
				descWidth = spaceAfter;
			}
			descX += shellBounds.width;
		}

		list.setSize(shellBounds.width, shellBounds.height);
		description.setBounds(descX, descY, descWidth, descHeight);
		browser.setSize(descWidth, descHeight - buttonSize.y);
	}

}
