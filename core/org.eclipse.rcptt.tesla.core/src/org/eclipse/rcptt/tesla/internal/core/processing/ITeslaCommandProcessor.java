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
package org.eclipse.rcptt.tesla.internal.core.processing;

import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.core.info.AdvancedInformation;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.core.protocol.IElementProcessorMapper;
import org.eclipse.rcptt.tesla.core.protocol.SelectCommand;
import org.eclipse.rcptt.tesla.core.protocol.SelectResponse;
import org.eclipse.rcptt.tesla.core.protocol.raw.Command;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.internal.core.AbstractTeslaClient;

/**
 * @author haiodo
 *
 *         This class are used to implement individual tesla command processors.
 *         Processors are used to execute tesla commands.
 *
 */
public interface ITeslaCommandProcessor {
	static class PreExecuteStatus {
		public boolean canExecute = false;

		public PreExecuteStatus(boolean canExecute) {
			super();
			this.canExecute = canExecute;
		}

		public void clean() {
		}
	}

    int getPriority();

	String getFeatureID();

	/**
	 * Return true if following selector are supported by this processor.
	 *
	 * @see ElementKind for details of selectors.
	 * @param kind
	 * @return
	 */
	boolean isSelectorSupported(String kind);

	/**
	 * Locate element using select command.
	 *
	 * @param cmd
	 *            - command to obtain selection values from.
	 * @param generator
	 *            - use generator to provide unique element credentials.
	 * @param mapper
	 *            - use mapper to map element to this processor. Will be used in
	 *            execution of commands with returned element.
	 * @return
	 */
	SelectResponse select(SelectCommand cmd, ElementGenerator generator,
			IElementProcessorMapper mapper);

	/**
	 * Return true if command execution are supported by this processor.
	 *
	 * @param cmd
	 * @return
	 */
	boolean isCommandSupported(Command cmd);

	/**
	 * Execute command.
	 *
	 * @param command
	 * @param mapper
	 * @return
	 */
	Response executeCommand(Command command, IElementProcessorMapper mapper);

	/**
	 * Called before command are executed. If return PreExecuteStatus with
	 * canExecute== false then execution will not be performed, before
	 * canExecute is not true or return value is non null.
	 *
	 * @param command
	 * @param previousStatus
	 * @param info TODO
	 * @param info
	 * @return
	 */
	PreExecuteStatus preExecute(Command command, PreExecuteStatus previousStatus, Q7WaitInfoRoot info);

	/**
	 * Initialize processor. Id is per client identifier.
	 *
	 * @param client
	 * @param id
	 */
	void initialize(AbstractTeslaClient client, String id);

	/**
	 * Called for each processor after some select command return element. Allow
	 * to update element or to map this element also to this processor. It is a
	 * only way to extend some element commands set.
	 *
	 * @param element
	 * @param mapper
	 */
	void postSelect(Element element, IElementProcessorMapper mapper);

	/**
	 * Return true if execution of command in this processor require inactivity.
	 *
	 * @return
	 */
	boolean isInactivityRequired();

	/**
	 * Answers the question: "Should the current command be processed now or
	 * later?" Returns {@code true} if processing of commands can be continued.
	 * Using this method it is possible to implement custom wait states.
	 *
	 * <p>
	 * <b>Contract:</b> If this method returns {@code false}, the command will
	 * be polled again. Implementations must ensure that {@code false} is only
	 * returned when the caller will eventually have an opportunity to poll again
	 * (e.g. from {@code Display.sleep()}). Returning {@code false} indefinitely
	 * or in situations where polling will never resume will cause a live-lock.
	 * </p>
	 *
	 * @param context
	 * @param info
	 * @return {@code true} if the current command should be processed
	 *         immediately, {@code false} to defer processing until the next poll
	 */
	boolean canProceed(Context context, Q7WaitInfoRoot info);

	public void clean(); // Do some cleanup between sessions

	/**
	 * Called on client termination.
	 */
	void terminate();

	void checkHang();

	/**
	 * Method to collect additional information.
	 *
	 * @param information
	 * @param lastCommand
	 *            TODO
	 */
	void collectInformation(AdvancedInformation information, Command lastCommand);

	void notifyUI();
}
