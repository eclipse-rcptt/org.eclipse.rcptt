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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.IElementProcessorMapper;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectResponse;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.core.protocol.raw.ResponseStatus;
import org.eclipse.rcptt.tesla.internal.core.processing.ElementGenerator;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor;
import org.eclipse.rcptt.tesla.internal.core.processing.ITeslaCommandProcessor.PreExecuteStatus;

public class TeslaProcessorManager {
	private final List<ITeslaCommandProcessor> processors;

	public TeslaProcessorManager() {
		processors = new ArrayList<ITeslaCommandProcessor>();

		IConfigurationElement[] elements = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(TeslaCore.PLUGIN_ID + ".processor");
		for (IConfigurationElement cfg : elements) {
			ITeslaCommandProcessor provider;
			try {
				provider = (ITeslaCommandProcessor) cfg
						.createExecutableExtension("class");
				processors.add(provider);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		Collections.sort(processors, new ProcessorComparator());
	}

	public void collectInformation(AdvancedInformation collector, Command command) {
		for (ITeslaCommandProcessor processor : processors) {
			processor.collectInformation(collector, command);
		}
	}

	public void initializeProcessors(AbstractTeslaClient client, String clientId) {
		for (ITeslaCommandProcessor processor : processors) {
			processor.initialize(client, clientId);
		}
	}

	public void terminate() {
		for (ITeslaCommandProcessor processor : processors) {
			processor.terminate();
		}
	}

	public void postSelect(Element element, IElementProcessorMapper mapper) {
		for (ITeslaCommandProcessor processor : processors) {
			processor.postSelect(element, mapper);
		}
	}

	public SelectResponse select(SelectCommand command, ElementGenerator generator, IElementProcessorMapper mapper) {
		final String kind = command.getData().getKind();
		for (ITeslaCommandProcessor processor : processors) {
			if (processor.isSelectorSupported(kind)) {
				SelectResponse response = processor.select(command, generator, mapper);
				if (response == null)
					continue;
				if (response.getStatus().equals(ResponseStatus.FAILED))
					return response;
				if (!response.getElements().isEmpty()) {
					for (Element uiElement : response.getElements()) {
						mapper.map(uiElement, processor);
					}
					return response;
				}
			}
		}
		return null;
	}

	public Response executeCommand(Command command, IElementProcessorMapper abstractTeslaClient,
			boolean returnOnFirstResult) {
		for (ITeslaCommandProcessor processor : processors) {
			if (!processor.isCommandSupported(command))
				continue;
			Response response = processor.executeCommand(command, abstractTeslaClient);
			if (response == null)
				continue;
			if (response.getStatus().equals(ResponseStatus.FAILED))
				return response;
			if (returnOnFirstResult)
				return response;
		}
		return null;
	}

	public boolean canProceed(Context context, Q7WaitInfoRoot info) {
		for (ITeslaCommandProcessor processor : processors) {
			if (!processor.canProceed(context, info))
				return false;
		}
		return true;
	}

	public void clean() {
		for (ITeslaCommandProcessor processor : processors) {
			processor.clean();
		}
	}

	public PreExecuteStatus preExecute(Command command, PreExecuteStatus previousStatus, Q7WaitInfoRoot info) {
		for (ITeslaCommandProcessor processor : processors) {
			PreExecuteStatus rv = processor.preExecute(command, previousStatus, info);
			if (rv != null)
				return rv;
		}
		return null;
	}

	public <T> T getProcessor(Class<T> clazz$) {
		for (ITeslaCommandProcessor processor : processors) {
			if (clazz$ == processor.getClass()) {
				return clazz$.cast(processor);
			}
		}
		return null;
	}

	public <T> List<T> getProcessors(Class<T> clazz$) {
		List<T> result = new ArrayList<T>();
		for (ITeslaCommandProcessor processor : processors) {
			if (clazz$.isInstance(processor)) {
				result.add(clazz$.cast(processor));
			}
		}
		return result;
	}

	public void notifyUI() {
		for (ITeslaCommandProcessor processor : processors) {
			processor.notifyUI();
		}
	}

	private static class ProcessorComparator implements Comparator<ITeslaCommandProcessor> {

		@Override
		public int compare(ITeslaCommandProcessor first, ITeslaCommandProcessor second) {
			return Integer.compare(first.getPriority(), second.getPriority());
		}

	}
}
