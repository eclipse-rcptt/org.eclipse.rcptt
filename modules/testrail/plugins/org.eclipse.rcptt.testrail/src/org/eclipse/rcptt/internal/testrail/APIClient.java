/*******************************************************************************
 * Copyright (c) 2009, 2016 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.testrail;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.rcptt.util.Base64;

public class APIClient {
	private String url;
	private String username;
	private String password;
	private boolean useUnicode;

	public APIClient(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.useUnicode = false;
	}

	public void setUseUnicode(boolean useUnicode) {
		this.useUnicode = useUnicode;
	}

	public String sendGetRequest(String endpoint) {
		HttpGet request = new HttpGet(url + endpoint);
		return sendRequest(request);
	}

	public String sendPostRequest(String endpoint, String params) {
		HttpPost request = new HttpPost(url + endpoint);
		// Full Unicode support was added in TestRail 2.0,
		// so we use ISO-8859-1 by default,
		// but if Unicode is needed, it could be enabled in preferences
		if (useUnicode) {
			request.setEntity(new StringEntity(params, StandardCharsets.UTF_8));
		} else {
			request.setEntity(new StringEntity(params, StandardCharsets.ISO_8859_1));
		}
		TestRailPlugin.logInfo(MessageFormat.format(Messages.APIClient_GeneratedRequest, params));
		return sendRequest(request);
	}

	private String sendRequest(HttpUriRequest request) {
		HttpClient client = HttpClientBuilder.create().build();
		setUpHeaders(request);
		try {
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			String entity = EntityUtils.toString(response.getEntity());
			if (status.getStatusCode() != HttpStatus.SC_OK) {
				TestRailPlugin.log(MessageFormat.format(Messages.APIClient_HTTPError,
						status.getStatusCode(), entity.equals("") ? status.getReasonPhrase() : entity));
				return null;
			}
			TestRailPlugin
					.logInfo(MessageFormat.format(Messages.APIClient_RecievedResponse, entity));
			return entity;
		} catch (Exception e) {
			TestRailPlugin.log(Messages.APIClient_ErrorWhileSendingRequest, e);
			return null;
		}
	}

	private void setUpHeaders(HttpUriRequest request) {
		String credentials = username + ":" + password;
		String encodedCredentials = Base64.encode(credentials.getBytes(StandardCharsets.ISO_8859_1));
		String authorizationHeader = "Basic " + new String(encodedCredentials);
		request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);

		String contentTypeHeader = "application/json";
		request.addHeader(HttpHeaders.CONTENT_TYPE, contentTypeHeader);
	}
}
