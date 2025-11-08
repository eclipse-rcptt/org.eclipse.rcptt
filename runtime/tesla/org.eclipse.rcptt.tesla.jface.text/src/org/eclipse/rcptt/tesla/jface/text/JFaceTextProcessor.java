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
package org.eclipse.rcptt.tesla.jface.text;

import java.lang.Thread.State;
import java.lang.reflect.Field;
import java.util.List;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.TeslaLimits;
import org.eclipse.rcptt.tesla.core.TeslaMessages;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.BooleanResponse;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.IElementProcessorMapper;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolFactory;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectResponse;
import org.eclipse.rcptt.tesla.core.protocol.ShowContentAssist;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.core.protocol.raw.ResponseStatus;
import org.eclipse.rcptt.tesla.internal.core.AbstractTeslaClient;
import org.eclipse.rcptt.tesla.internal.core.processing.ElementGenerator;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor;
import org.eclipse.rcptt.tesla.internal.ui.player.PlayerWrapUtils;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.tesla.internal.ui.player.TeslaSWTAccess;
import org.eclipse.rcptt.tesla.internal.ui.processors.SWTUIProcessor;
import org.eclipse.rcptt.tesla.jface.TextReconcilerManager;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Widget;

public class JFaceTextProcessor implements ITeslaCommandProcessor {

	private AbstractTeslaClient client;

	private WeakHashMap<AbstractReconciler, Long> reconcilerTimeours = new WeakHashMap<AbstractReconciler, Long>();

	// private String id;

	public JFaceTextProcessor() {
	}

	@Override
	public int getPriority() {
		return 150;
	}

	@Override
	public String getFeatureID() {
		return "jface.text";
	}

	@Override
	public boolean isSelectorSupported(String kind) {
		return false;
	}

	@Override
	public SelectResponse select(SelectCommand cmd, ElementGenerator generator,
			IElementProcessorMapper mapper) {
		return null;
	}

	@Override
	public boolean isCommandSupported(Command cmd) {
		if (cmd instanceof ShowContentAssist) {
			return true;
		}
		return false;
	}

	@Override
	public Response executeCommand(Command command,
			IElementProcessorMapper mapper) {
		if (command instanceof ShowContentAssist) {
			return handleShowContentAssist((ShowContentAssist) command);
		}
		return null;
	}

	private Response handleShowContentAssist(ShowContentAssist command) {
		SWTUIElement swtuiElement = getSWTProcessor().getMapper().get(
				command.getElement());
		BooleanResponse response = ProtocolFactory.eINSTANCE
				.createBooleanResponse();
		if (swtuiElement != null) {
			Widget widget = PlayerWrapUtils.unwrapWidget(swtuiElement);
			if (widget instanceof StyledText) {
				Viewer thisControl = TeslaSWTAccess
						.getViewer((StyledText) widget);
				if (thisControl != null && thisControl instanceof SourceViewer) {
					SourceViewer textViewer = (SourceViewer) thisControl;
					textViewer
							.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				}
			}
		} else {
			response.setMessage(TeslaMessages.CommandProcessor_CannotFindWidget);
			response.setStatus(ResponseStatus.FAILED);
		}
		return response;
	}

	private SWTUIProcessor getSWTProcessor() {
		return client.getProcessor(SWTUIProcessor.class);
	}

	@Override
	public PreExecuteStatus preExecute(Command command,
			PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		return null;
	}

	@Override
	public void initialize(AbstractTeslaClient client, String id) {
		this.client = client;
		// this.id = id;
	}

	@Override
	public void postSelect(Element element, IElementProcessorMapper mapper) {
		// Map text elements to support this processor operations
		if (element.getKind().equals(ElementKind.Text.name())) {
			mapper.map(element, this);
		}
	}

	@Override
	public boolean isInactivityRequired() {
		return false;
	}

	@Override
	public boolean canProceed(Context context, Q7WaitInfoRoot info) {
		List<AbstractReconciler> reconcilers = TextReconcilerManager
				.getInstance().getReconcilers();
		for (AbstractReconciler reconciler : reconcilers) {
			boolean needWait = false;
			
			Thread thread = null;
			
			Object worker = TeslaSWTAccess.getField(Object.class, reconciler, "fThread");
			if (worker == null) {
				worker = TeslaSWTAccess.getField(Object.class, reconciler, "fWorker");
			}
			if (worker == null) {
				continue;
			}
			if (worker instanceof Thread t) {
				thread = t;
			} else if (worker instanceof Job job) {
				thread = job.getThread();
			} else {
				thread = TeslaSWTAccess.getField(Thread.class, worker, "fThread");
			}
			if (thread != null) {
				State state = thread.getState();
				if (!(state.equals(State.BLOCKED)
						|| state.equals(State.WAITING)
						|| state.equals(State.TIMED_WAITING) || state
								.equals(State.TERMINATED))) {
					// Reconciler are in execution of some action phase
					Q7WaitUtils.updateInfo("reconciler.thread", reconciler.getClass().getName(), info);
					needWait = true;
				}
			}
			if (!needWait && worker != null) {
				try {
					Field field = worker.getClass().getDeclaredField("fIsDirty");
					field.setAccessible(true);
					boolean fDirty = field.getBoolean(worker);
	
					field = worker.getClass().getDeclaredField("fCanceled");
					field.setAccessible(true);
					boolean fCanceled = field.getBoolean(worker);
					if (fDirty && !fCanceled) {
						Q7WaitUtils.updateInfo("reconciler.thread.dirty", reconciler.getClass().getName(), info);
						needWait = true;
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
			}
			if (needWait) {
				Long firstTime = reconcilerTimeours.get(reconciler);
				if (firstTime == null) {
					reconcilerTimeours.put(reconciler, Long.valueOf(System.currentTimeMillis()));
				} else if (System.currentTimeMillis() - firstTime.longValue() > TeslaLimits
						.getReconcilerTimeout()) {
					Q7WaitUtils.updateInfo("reconciler.thread.skip", reconciler.getClass().getName(), info);
					// Ignore if timeout
					return true;
				}
				return false;
			}
		}

		return true;
	}

	@Override
	public void clean() {
		this.reconcilerTimeours.clear();
	}

	@Override
	public void terminate() {
		client = null;
	}

	@Override
	public void checkHang() {
	}

	@Override
	public void collectInformation(AdvancedInformation information,
			Command lastCommand) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyUI() {
		// TODO Auto-generated method stub

	}
}
