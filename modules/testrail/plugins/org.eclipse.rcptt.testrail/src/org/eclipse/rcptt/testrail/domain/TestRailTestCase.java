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
package org.eclipse.rcptt.testrail.domain;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TestRailTestCase {
	@Expose(serialize = false)
	private String id;
	@Expose
	@SerializedName("title")
	private String name;
	@Expose(serialize = false, deserialize = false)
	private String textDescription;
	@Expose(serialize = false, deserialize = false)
	private String htmlDescription;

	public TestRailTestCase() {
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setTextDescription(String textDescription) {
		this.textDescription = textDescription;
	}

	public String getTextDescription() {
		return textDescription;
	}

	public void setHTMLDescription(String htmlDescription) {
		this.htmlDescription = htmlDescription;
	}

	public String getHTMLDescription() {
		return htmlDescription;
	}
}
