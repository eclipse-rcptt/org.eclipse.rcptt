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
package org.eclipse.rcptt.tesla.recording.aspects.swt.rap;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.scripting.ClientListener;

public class AssertionClientListener extends ClientListener {

	private static final long serialVersionUID = 1L;

	public static AssertionClientListener getInstance() {
		return SingletonUtil.getSessionInstance(AssertionClientListener.class);
	}

	private AssertionClientListener() {
		super(getCode());
	}

	private static String getCode() {
		return "var handleEvent = function( event ) {"
				+ "console.log(event.widget.getData('myOtherWidget'));"
				+" var id = rwt.remote.ObjectRegistry.getId( event.widget); " + System.lineSeparator()
				+ "rwt.client.rcptt.Assertion.getInstance().setActiveWidget(id);" + " };";
	}

}
