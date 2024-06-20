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
package org.eclipse.rcptt.tesla.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.rcptt.tesla.core.ClosedException;
import org.eclipse.rcptt.tesla.core.Q7WaitUtils;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.InfoFactory;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.ElementCommand;
import org.eclipse.rcptt.tesla.core.protocol.GetStateResponse;
import org.eclipse.rcptt.tesla.core.protocol.IElementProcessorMapper;
import org.eclipse.rcptt.tesla.core.protocol.Nop;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolFactory;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolPackage;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectData;
import org.eclipse.rcptt.tesla.core.protocol.SelectResponse;
import org.eclipse.rcptt.tesla.core.protocol.TeslaStream;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.RawFactory;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.core.protocol.raw.ResponseStatus;
import org.eclipse.rcptt.tesla.internal.core.info.GeneralInformationCollector;
import org.eclipse.rcptt.tesla.internal.core.processing.ElementGenerator;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor.PreExecuteStatus;
import org.eclipse.rcptt.util.Exceptions;

public abstract class AbstractTeslaClient implements IElementProcessorMapper {
	private boolean processing = false;
	private final ElementGenerator generator = new ElementGenerator();
	protected final List<Command> localQueue = new ArrayList<Command>();
	private Map<String, Set<ITeslaCommandProcessor>> elementProcessors = new HashMap<String, Set<ITeslaCommandProcessor>>();
	private final Map<Command, ITeslaCommandProcessor.PreExecuteStatus> preStatuses = new HashMap<Command, ITeslaCommandProcessor.PreExecuteStatus>();
	private final TeslaProcessorManager processors = new TeslaProcessorManager();
	private Context currentContext;
	private boolean closed = false;

	private final String id;
	private final AtomicInteger hasEvents = new AtomicInteger(-1);
	private AdvancedInformation lastFailureInformation;

	public AbstractTeslaClient(String id) {
		super();
		this.id = id;
		processors.initializeProcessors(this, id);
	}

	public String getID() {
		return id;
	}

	synchronized public void shutdown() {
		processors.terminate();
		elementProcessors.clear();
		closed = true;
	}

	protected abstract TeslaStream stream();

	@Override
	synchronized public void map(Element element, ITeslaCommandProcessor processor) {
		if (element != null && processor != null) {
			putProcessor(element, processor);
			processors.postSelect(element, new IElementProcessorMapper() {
				@Override
				public void map(Element element, ITeslaCommandProcessor processor) {
					putProcessor(element, processor);
				}
			});
		}
	}

	synchronized private void putProcessor(Element element, ITeslaCommandProcessor processor) {
		final String key = makeKey(element);
		Set<ITeslaCommandProcessor> set = this.elementProcessors.get(key);
		if (set == null) {
			set = new LinkedHashSet<ITeslaCommandProcessor>();
		}
		Set<ITeslaCommandProcessor> newSet = new LinkedHashSet<ITeslaCommandProcessor>();
		newSet.add(processor);
		newSet.addAll(set);
		// II: So that processors are iterated in
		// reverse order, i.e. processor which has
		// been put last, is iterated at first
		this.elementProcessors.put(key, newSet);
	}

	private SelectResponse handleSelect(SelectCommand cmd) {
		try {
			// Check for extensions first
			String kind = cmd.getData().getKind();
			SelectResponse response = processors.select(cmd, generator, this);
			if (response == null) {
				response = ProtocolFactory.eINSTANCE.createSelectResponse();
				response.setStatus(ResponseStatus.FAILED);
				response.setMessage("No Element with kind:" + kind + " is available for selection...");
			}
			if (response.getStatus().equals(ResponseStatus.FAILED)) {
				handleFailedResponse(cmd, response);
			}
			return response;
		} catch (ClosedException e) {
			SelectResponse response = ProtocolFactory.eINSTANCE
					.createSelectResponse();
			response.setMessage("Disposed:\n" + Exceptions.toString(e));
			handleFailedResponse(cmd, response);
			return response;
		} catch (Exception e) {
			SelectData data = cmd.getData();
			Element parent = null;
			if (data != null) {
				parent = data.getParent();
			}
			throw new IllegalArgumentException("Can't select " + data + " in " + parent, e);
		}
	}

	private String makeKey(Element uiElement) {
		return uiElement.getKind() + ":" + uiElement.getId();
	}

	synchronized public boolean processNext(Context context, Q7WaitInfoRoot info) {
		// Skip this event if we are already processing another one.
		if (!closed && !processing && (hasCommand() || !localQueue.isEmpty()) && canProceed(context, info)) {
			return doOneCommand(context, info);
		}
		return false;
	}

	public void hasEvent(String kind, String name, Q7WaitInfoRoot info) {
		hasEvents.incrementAndGet();
		Q7WaitUtils.startInfo(kind, name, info);
	}

	protected boolean canProceed(Context context, Q7WaitInfoRoot info) {
		hasEvents.set(0);
		boolean result = processors.canProceed(context, info);
		if (hasEvents.get() > 0) {
			return false;
		}
		return result;
	}

	public void clean() {
		lastFailureInformation = null;
		processors.clean();
		for (PreExecuteStatus status : preStatuses.values()) {
			status.clean();
		}
		preStatuses.clear();
		elementProcessors.clear();
	}

	private boolean preExecute(Command command, Q7WaitInfoRoot info) {
		if (command instanceof ElementCommand) {
			ElementCommand cmd = (ElementCommand) command;
			Element element = cmd.getElement();
			if (element != null) {
				Set<ITeslaCommandProcessor> processors = elementProcessors.get(makeKey(element));
				if (processors != null) {
					PreExecuteStatus preStatus = preStatuses.get(command);
					for (ITeslaCommandProcessor processor : processors) {
						if (processor != null) {
							try {
								PreExecuteStatus status = processor.preExecute(command, preStatus, info);
								if (status != null) {
									preStatuses.put(command, status);
									if (preStatus != status) {
										cleanPreStatus(preStatus);
									}
									if (!status.canExecute) {
										return false;
									}
								} else {
									preStatuses.remove(command);
									cleanPreStatus(preStatus);
								}
							} catch (Throwable e) {
								TeslaCore.log(e);
							}
						}
					}
				}
			}
		} else {
			PreExecuteStatus status = processors.preExecute(command, preStatuses.get(command), info);
			if (status != null && !status.canExecute) {
				preStatuses.put(command, status);
				return false;
			}
		}
		return true;
	}

	private void cleanPreStatus(PreExecuteStatus preStatus) {
		if (preStatus != null) {
			try {
				// We need to call clean to be sure status is finalized all
				// things.
				preStatus.clean();
			} catch (Throwable ee) {
				TeslaCore.log(ee);
			}
		}
	}

	synchronized public void addCommand(Command cmd) {
		localQueue.add(cmd);
		notifyUI();
	}

	public void notifyUI() {
		processors.notifyUI();
	}

	private boolean doOneCommand(Context context, Q7WaitInfoRoot info) {
		synchronized (this) {
			processing = true;
		}
		this.currentContext = context;
		Command command = null;
		try {
			final boolean isLocalQueue;
			if (!localQueue.isEmpty()) {
				command = localQueue.remove(0);
				isLocalQueue = true;
			} else {
				command = stream().readCommand();
				if (command == null) {
					return false;
				}
				isLocalQueue = false;
			}
			try {
				if (!preExecute(command, info)) {
					if (isLocalQueue) {
						localQueue.add(0, command);
					} else {
						localQueue.add(command);
					}
					return false;
				}
				stream().writeResponse(execute(command));
			} catch (Throwable e) {
				logException(e);
				stream().writeResponse(createErrorResponse(e));
			}
		} finally {
			synchronized (this) {
				processing = false;
			}
			currentContext = null;
		}
		return true;
	}

	private Response execute(final Command command) {
		// long startTime = System.currentTimeMillis();
		if (!(command instanceof Nop)) {
			lastFailureInformation = null;
		}
		if (command instanceof ElementCommand) {
			ElementCommand cmd = (ElementCommand) command;
			Element element = cmd.getElement();

			if (element != null) {
				Set<ITeslaCommandProcessor> processors = elementProcessors.get(makeKey(element));
				if (processors != null) {
					for (ITeslaCommandProcessor processor : processors) {
						if (processor != null) {
							Response response = processor.executeCommand(command, this);
							if (response != null) {
								if (response.getStatus().equals(ResponseStatus.FAILED)) {
									handleFailedResponse(command, response);
								}
								return response;
							}
						}
					}
				}
			}
		} else {
			Response response = processors.executeCommand(command, this, true);
			if (response != null) {
				if (response.getStatus().equals(ResponseStatus.FAILED)) {
					handleFailedResponse(command, response);
				}
				return response;
			}
		}
		if (command.eClass().getEPackage().equals(ProtocolFactory.eINSTANCE.getEPackage())) {
			switch (command.eClass().getClassifierID()) {
			case ProtocolPackage.SELECT_COMMAND:
				return handleSelect((SelectCommand) command);
			case ProtocolPackage.SHUTDOWN:
				shutdown();
				return RawFactory.eINSTANCE.createResponse();
			case ProtocolPackage.NOP:
				return RawFactory.eINSTANCE.createResponse();
			case ProtocolPackage.GET_STATE:
				GetStateResponse res = ProtocolFactory.eINSTANCE.createGetStateResponse();
				Element state = RawFactory.eINSTANCE.createElement();
				String returnedState = currentContext.getID();
				// System.out.println("RETURNED WAIT STATE:" +
				// returnedState);
				state.setId(returnedState);
				state.setKind("State");
				res.setState(state);
				return res;
			case ProtocolPackage.WAIT_FOR_STATE:
				return RawFactory.eINSTANCE.createResponse();
			case ProtocolPackage.ROLLBACK_TO_STATE:
				// TODO
				return RawFactory.eINSTANCE.createResponse();
			}
		}
		Response failed = RawFactory.eINSTANCE.createResponse();
		failed.setStatus(ResponseStatus.FAILED);
		failed.setMessage("Unsupported command");
		return failed;
	}

	private Response createErrorResponse(Throwable e) {
		Response errorResponse = RawFactory.eINSTANCE.createResponse();
		errorResponse.setStatus(ResponseStatus.FAILED);
		errorResponse.setMessage("Exception happened:\n" + Exceptions.toString(e)); // class
																					// name
																					// and
																					// exception
																					// message
		return errorResponse;
	}

	synchronized public AdvancedInformation getLastFailureInformation() {
		return lastFailureInformation;
	}

	synchronized public void clearLastFailureInformation() {
		lastFailureInformation = null;
	}

	private void handleFailedResponse(Command command, Response response) {
		if (response != null) {
			// Collect advanced information for this error
			AdvancedInformation information = getAdvancedInformation(command);
			this.lastFailureInformation = information;
			response.setAdvancedInformation(EcoreUtil.copy(information));
			response.setStatus(ResponseStatus.FAILED);
		}
	}

	synchronized public AdvancedInformation getAdvancedInformation(Command command) {
		// Collect advanced information
		AdvancedInformation information = InfoFactory.eINSTANCE.createAdvancedInformation();
		processors.collectInformation(information, command);
		GeneralInformationCollector.collectInformation(information);

		return information;
	}

	synchronized public void collectLastFailureInformation() {
		AdvancedInformation information = getAdvancedInformation(null);
		this.lastFailureInformation = information;
	}

	public abstract void logException(Throwable t);

	public abstract boolean hasCommand();

	public <T> T getProcessor(Class<T> clazz$) {
		return processors.getProcessor(clazz$);
	}

	public <T> List<T> getProcessors(Class<T> clazz$) {
		return processors.getProcessors(clazz$);
	}

}