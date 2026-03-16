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
package org.eclipse.rcptt.ui.preferences;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.utils.AbstractFeatureManager;
import org.eclipse.rcptt.tesla.core.utils.AbstractFeatureManager.Option;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class FeaturesComposite {
	private Map<Option, Control> optionFields = new HashMap<Option, Control>();

	private Runnable optionChangeCallback;

	private boolean groupping = true;

	private ScrolledComposite scrolledContainer;

	public void setOptionChangeCallback(Runnable optionChangeCallback) {
		this.optionChangeCallback = optionChangeCallback;
	}

	public Control createOptions(final Composite content, String group,
			boolean autoApply, FormToolkit toolkit) {
		scrolledContainer = new ScrolledComposite(content, SWT.V_SCROLL);
		scrolledContainer.setExpandHorizontal(true);
		if (toolkit != null)
			toolkit.adapt(scrolledContainer);
		scrolledContainer.getVerticalBar().setIncrement(10); // Increase speed


		final Composite compositeContainer = new Composite(scrolledContainer, SWT.NONE);
		if (toolkit != null)
			toolkit.adapt(compositeContainer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(compositeContainer);
		GridLayoutFactory.fillDefaults().applyTo(compositeContainer);

		scrolledContainer.setContent(compositeContainer);

		// Adds features
		createFeatures(compositeContainer, Messages.FeaturesComposite_CoreFeaturesGroup,
				Q7Features.getInstance(), group, autoApply, toolkit);
		createFeatures(compositeContainer, Messages.FeaturesComposite_RuntimeFeaturesGroup,
				TeslaFeatures.getInstance(), group, autoApply, toolkit);


		compositeContainer.setSize(compositeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		IExpansionListener listener = new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
			}

			public void expansionStateChanged(ExpansionEvent e) {
				Point min = compositeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				compositeContainer.setSize(min);
			}
		};
		recursivelyAddListener(compositeContainer, listener);

		return scrolledContainer;
	}
	
	void recursivelyAddListener(Control control, IExpansionListener listener) {
		if (control instanceof ExpandableComposite) {
			((ExpandableComposite) control).addExpansionListener(listener);
		}
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				recursivelyAddListener(child, listener);
			}
		}
	}

	/**
	 * Resets controls to default values.
	 */
	public void setDefaults() {
		Control value;
		Option option;
		for (Map.Entry<Option, Control> opt : optionFields.entrySet()) {
			value = opt.getValue();
			option = opt.getKey();
			try {
				if (value instanceof Button) {
					((Button) value).setSelection("true".equals(option.getDefaultValue()));
				} else if (value instanceof Spinner) {
					((Spinner) value).setSelection(Integer.parseInt(option.getDefaultValue()));
				} else if (value instanceof Combo) {
					((Combo) value).setText(option.getDefaultValue());
				} else if (value instanceof Text) {
					((Text) value).setText(option.getDefaultValue());
				}
			} catch (Exception e) {
				throw new IllegalStateException(format("Failed to process option %s, value %s", option.getName(), value), e);
			}
		}
	}

	public void apply() {
		for (Map.Entry<Option, Control> opt : optionFields.entrySet()) {
			applyValue(opt.getKey(), opt.getValue());
		}

		Q7Features.getInstance().saveToPreferences(RcpttPlugin.getPreferences());
		TeslaFeatures.getInstance().saveToPreferences(
				TeslaCore.getPreferences());
	}

	private void applyValue(Option option, Control value) {
		if (value instanceof Button) {
			boolean selection = ((Button) value).getSelection();
			option.setValue(selection ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (value instanceof Spinner) {
			option.setValue(Integer.toString(((Spinner) value).getSelection()));
		} else if (value instanceof Combo) {
			int index = ((Combo) value).getSelectionIndex();
			option.setValue(option.getValues()[index]);
		} else if (value instanceof Text) {
			option.setValue(((Text) value).getText());
		}
		if (optionChangeCallback != null) {
			optionChangeCallback.run();
		}
	}

	public void createFeatures(final Composite content, String title,
			AbstractFeatureManager features, String cfgGroup,
			boolean autoApply, FormToolkit toolkit) {
		Option[] options = features.getOptions();
		Map<String, List<Option>> categories = new TreeMap<String, List<Option>>();
		Map<String, Boolean> expanded = new HashMap<String, Boolean>();
		for (final Option key : options) {
			if (key.isEditable() && key.isShowIn(cfgGroup)) {
				String category = key.getCategory();
				if (category == null) {
					category = ""; //$NON-NLS-1$
				}
				if (category.startsWith("$")) { //$NON-NLS-1$
					category = category.substring(1);
					expanded.put(category, Boolean.TRUE);
				}
				List<Option> optionsList = categories.get(category);
				if (optionsList == null) {
					optionsList = new ArrayList<AbstractFeatureManager.Option>();
				}
				categories.put(category, optionsList);
				optionsList.add(key);
			}
		}
		if (categories.isEmpty()) {
			return;
		}
		// sort each category content
		for (List<Option> optionsList : categories.values()) {
			Collections.sort(optionsList, new Comparator<Option>() {
				public int compare(Option o1, Option o2) {
					return Integer.valueOf(o1.getOrder()).compareTo(
							o2.getOrder());
				}
			});
		}
		Composite parent = content;
		if( groupping ) {
			Group mg = new Group(content, SWT.NONE);
			if (toolkit != null)
				toolkit.adapt(mg);
			mg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			mg.setText(title);
			mg.setLayout(new GridLayout(2, false));
			parent = mg;
		}
		for (Map.Entry<String, List<Option>> entry : categories.entrySet()) {
			if (entry.getValue().size() == 0) {
				continue;
			}
			Composite g = null;
			if (entry.getKey().length() > 0 && groupping) {
				// g = new Group(mg, SWT.NONE);
				// ((Group)g).setText(entry.getKey());
				boolean expValue = expanded.containsKey(entry.getKey());
				ExpandableComposite exp = new ExpandableComposite(parent, SWT.NONE, SWT.BORDER | Section.TWISTIE
						| (expValue ? Section.EXPANDED : 0));
				if (toolkit != null)
					toolkit.adapt(exp);
				exp.setText(entry.getKey());
				exp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
						2, 1));
				g = new Composite(exp, SWT.NONE);
				if (toolkit != null)
					toolkit.adapt(g);
				g.setLayout(new GridLayout(2, false));
				exp.setClient(g);
			} else {
				g = parent;
			}
			for (final Option key : entry.getValue()) {
				if (key.isEditable() && key.isShowIn(cfgGroup)) {
					String[] values = key.getValues();
					if (values != null
							&& values.equals(AbstractFeatureManager.INT_VALUES)) {
						Label l = new Label(g, SWT.WRAP);
						l.setText(key.getName());
						if (toolkit != null)
							toolkit.adapt(l, false, false);
						final Spinner b = new Spinner(g, SWT.CHECK | SWT.BORDER);
						if (toolkit != null)
							toolkit.adapt(b);
						b.setValues(0, 0, Integer.MAX_VALUE, 0, 1, 1000);
						b.setSelection(Integer.parseInt(key.getValue()));
						b.setToolTipText(key.getDescription());
						b.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
								true, false, 2, 1));
						optionFields.put(key, b);

						if (key.getDescription() != null) {
							l.setToolTipText(key.getDescription());
						}

						if (autoApply) {
							b.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									applyValue(key, b);
								}
							});
						}
					} else if (values != null
							&& values
									.equals(AbstractFeatureManager.BOOLEAN_VALUES)) {
						final Button b = new Button(g, SWT.CHECK);
						b.setText(key.getName());
						if (toolkit != null)
							toolkit.adapt(b, true, true);
						b.setToolTipText(key.getDescription());
						b.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
								true, false, 2, 1));
						boolean value = "true".equals(key.getValue()); //$NON-NLS-1$
						b.setSelection(value);
						optionFields.put(key, b);
						if (autoApply) {
							b.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									applyValue(key, b);
								}
							});
						}
					} else if (values != null) {
						Label l = new Label(g, SWT.NONE);
						l.setText(key.getName());
						if (toolkit != null)
							toolkit.adapt(l, false, false);
						final Combo b = new Combo(g, SWT.SIMPLE | SWT.DROP_DOWN
								| SWT.READ_ONLY);
						if (toolkit != null)
							toolkit.adapt(b);
						b.setItems(values);
						b.setText(key.getValue());
						if (key.getDescription() != null) {
							b.setToolTipText(key.getDescription());
							l.setToolTipText(key.getDescription());
						}
						b.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
								true, false, 1, 1));
						optionFields.put(key, b);
						if (autoApply) {
							b.addModifyListener(new ModifyListener() {
								public void modifyText(ModifyEvent e) {
									applyValue(key, b);
								}
							});
						}
					} else {
						Label l = new Label(g, SWT.NONE);
						l.setText(key.getName());
						if (toolkit != null)
							toolkit.adapt(l, false, false);
						final Text value = new Text(g, SWT.BORDER);
						if (toolkit != null)
							toolkit.adapt(value, true, true);
						if (key.getDescription() != null) {
							value.setToolTipText(key.getDescription());
							l.setToolTipText(key.getDescription());
						}
						value.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT,
								true, false));
						if (key.getValue() != null) {
							value.setText(key.getValue());
						}
						optionFields.put(key, value);
						if (autoApply) {
							value.addModifyListener(new ModifyListener() {
								public void modifyText(ModifyEvent e) {
									applyValue(key, value);
								}
							});
						}
					}
				}
			}
		}
	}

	public void setGroupping(boolean b) {
		this.groupping  = b;
	}
}
