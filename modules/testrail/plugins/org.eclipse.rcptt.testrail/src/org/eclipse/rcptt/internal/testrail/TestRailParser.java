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
package org.eclipse.rcptt.internal.testrail;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.eclipse.rcptt.testrail.domain.TestRailTestCase;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TestRailParser {

	public static void parseTestCases(List<TestRailTestCase> testCases, JsonArray array) {
		for (JsonElement element : array) {
			if (element instanceof JsonObject) {
				JsonObject object = (JsonObject) element;
				StringBuilder textSb = new StringBuilder();
				StringBuilder htmlSb = new StringBuilder();
				String id = "";
				for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
					JsonElement valueElement = entry.getValue();
					if (valueElement == null
							|| valueElement.toString().equals("")
							|| valueElement.toString().equals("[]")
							|| valueElement.toString().equals("null")) {
						continue;
					}

					String name = entry.getKey();
					String value = valueElement.toString();
					switch (name) {
					case "id":
						id = value;
						continue;
					case "section_id":
					case "template_id":
					case "type_id":
					case "priority_id":
					case "milestone_id":
					case "created_by":
					case "updated_by":
					case "suite_id":
						continue;
					}

					if (name.startsWith("custom_")) {
						name = name.substring(7);
					}
					name = name.replaceAll("_", " ");
					String firstChar = name.substring(0, 1);
					firstChar = firstChar.toUpperCase();
					name = firstChar + name.substring(1);
					if (textSb.length() > 0) {
						textSb.append("\n");
					}
					textSb.append(name + ": ");
					if (htmlSb.length() > 0) {
						htmlSb.append("<br>");
					}
					htmlSb.append("<b>");
					htmlSb.append(name + ": ");
					htmlSb.append("</b>");
					
					switch (name) {
					case "Created on":
					case "Updated on":
						DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
						LocalDateTime localDate = LocalDateTime.ofInstant(
								Instant.ofEpochSecond(Integer.valueOf(value)),
								ZoneId.systemDefault());
						value = dateFormatter.format(localDate);
						textSb.append(value);
						htmlSb.append(value);
						continue;
					}

					parseTestCaseCustomAttribute(valueElement, textSb, htmlSb);
				}
				final String testCaseId = id;
				if (textSb.length() > 0 && htmlSb.length() > 0 && !testCaseId.equals("")) {
					TestRailTestCase testCase = testCases.stream()
							.filter(tcase -> testCaseId.equals(tcase.getId()))
							.findFirst().orElse(null);
					if (testCase != null) {
						testCase.setTextDescription(textSb.toString());
						testCase.setHTMLDescription(htmlSb.toString());
					}
				}
			}
		}
	}

	private static String parseTestCaseCustomAttribute(JsonElement element, StringBuilder textSb,
			StringBuilder htmlSb) {
		if (element == null
				|| element.toString().equals("")
				|| element.toString().equals("null")) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (element instanceof JsonPrimitive) {
			String value = element.toString();
			value = value.replaceAll("\"", "");
			value = value.replaceAll("\\\\r", "\r");
			value = value.replaceAll("\\\\n", "\n");
			if (value.contains("\n")) {
				textSb.append("\n");
			}
			textSb.append(value);
			value = value.replaceAll("\r", "");
			value = value.replaceAll("\n", "<br>");
			if (value.contains("<br>")) {
				htmlSb.append("<br>");
			}
			htmlSb.append(value);
		} else if (element instanceof JsonObject) {
			JsonObject object = (JsonObject) element;
			StringBuilder subTextSb = new StringBuilder();
			StringBuilder subHtmlSb = new StringBuilder();
			for (Map.Entry<String, JsonElement> subEntry : object.entrySet()) {
				if (subEntry.getValue() == null) {
					continue;
				}
				if (subTextSb.length() > 0) {
					subTextSb.append(", ");
				}
				subTextSb.append(subEntry.getKey().toString() + ": ");
				if (subHtmlSb.length() > 0) {
					subHtmlSb.append(", ");
				}
				subHtmlSb.append("<i>");
				subHtmlSb.append(subEntry.getKey().toString() + ": ");
				subHtmlSb.append("</i>");
				parseTestCaseCustomAttribute(subEntry.getValue(), subTextSb, subHtmlSb);
			}
			textSb.append(subTextSb.toString());
			htmlSb.append(subHtmlSb.toString());
		} else if (element instanceof JsonArray) {
			JsonArray array = (JsonArray) element;
			int i = 1;
			htmlSb.append("<ol>");
			for (JsonElement subElement : array) {
				textSb.append("\n");
				textSb.append(i + ". ");
				htmlSb.append("<li>");
				parseTestCaseCustomAttribute(subElement, textSb, htmlSb);
				i++;
				htmlSb.append("</li>");
			}
			htmlSb.append("</ol>");
		}
		return sb.toString();
	}

}
