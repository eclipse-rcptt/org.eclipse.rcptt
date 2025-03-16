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
package org.eclipse.rcptt.ui.controls;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;

import org.eclipse.rcptt.ui.recording.RecordingSupport.RecordingMode;

public abstract class AbstractEmbeddedComposite implements IEmbeddedComposite {

	protected final DataBindingContext dbc = new DataBindingContext();
	protected final WritableValue<Boolean> visible = new WritableValue<>(true,
			Boolean.class);
	protected final WritableValue<RecordingMode> recordingMode = new WritableValue<>(
			RecordingMode.Stopped, RecordingMode.class);
	protected ToolBar toolBar;

	public boolean getVisible() {
		return (Boolean) visible.getValue();
	}

	public void setVisible(boolean value) {
		visible.setValue(value);
	}

	public IObservableValue<Boolean> observeVisible() {
		return visible;
	}

	public IObservableValue<RecordingMode> observeRecordingMode() {
		return recordingMode;
	}

	public void createControl(Composite parent) {
		parent.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				dispose();
			}
		});
	}

	public void createToolBar(Composite parent) {
		final ToolBarManager manager = new ToolBarManager(SWT.FLAT
				| SWT.HORIZONTAL);
		fillActions(manager);
		toolBar = manager.createControl(parent);
		toolBar.moveAbove(null);
		if (disableToolBarOnHide()) {
			dbc.bindValue(WidgetProperties.enabled().observe(toolBar), visible);
		}
	}

	protected boolean disableToolBarOnHide() {
		return true;
	}

	protected void fillActions(IToolBarManager manager) {
	}

	public ToolBar getToolBar() {
		return toolBar;
	}

	public void dispose() {
		dbc.dispose();
	}

}
