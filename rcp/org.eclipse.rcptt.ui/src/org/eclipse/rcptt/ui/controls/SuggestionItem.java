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
package org.eclipse.rcptt.ui.controls;

public class SuggestionItem {

	public static class SuggestionDescription {

		private String text;
		private String html;

		public SuggestionDescription() {
		}

		public SuggestionDescription(String text, String html) {
			this.text = text;
			this.html = html;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getHTML() {
			return html;
		}

		public void setHTML(String html) {
			this.html = html;
		}

	}

	private String name;
	private SuggestionDescription description;

	public SuggestionItem() {
	}

	public SuggestionItem(String name) {
		this.name = name;
	}

	public SuggestionItem(String name, SuggestionDescription description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SuggestionDescription getDescription() {
		return description;
	}

	public void setDescription(SuggestionDescription description) {
		this.description = description;
	}

}
