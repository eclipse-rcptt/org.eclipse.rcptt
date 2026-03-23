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
package org.eclipse.rcptt.ecl.server.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.emf.ecore.util.EcoreUtil;

public class SessionManager {
	private ExecutorService executor;
	int count = 0;
	private boolean useJobs = false;;
	private final Map<String, Object> properties = Collections.synchronizedMap(new HashMap<>()); 

	public SessionManager(boolean useJobs) {
		executor = Executors.newCachedThreadPool();
		this.useJobs = useJobs;
	}

	public void acceptNewConnection(Socket client) throws IOException {
		count++;
		String uuid = initRecover(client);
		if (uuid != null) {
			executor.execute(new SessionRequestHandler(client, useJobs, properties));
		} else {
			client.close();
		}
	}
	
	public Object setProperty(String key, Object value) {
		return properties.put(key, value);
	}

	private String initRecover(Socket client) throws IOException {
		InputStream stream = client.getInputStream();
		DataInputStream din = new DataInputStream(stream);
		DataOutputStream dout = new DataOutputStream(client.getOutputStream());
		String utf = din.readUTF();
		if ("newsession".equals(utf)) {
			String uuid = EcoreUtil.generateUUID();
			uuid = EcoreUtil.generateUUID();
			dout.writeUTF(uuid);
			dout.flush();
			return uuid;

		}
		return null;
	}
}
