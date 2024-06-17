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
package org.eclipse.rcptt.tesla.ecl.internal.impl.commands;

import java.lang.reflect.Field;
import java.util.AbstractList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;

import org.eclipse.rcptt.tesla.core.protocol.GetPropertyValue;
import org.eclipse.rcptt.tesla.core.protocol.ObjectResponse;
import org.eclipse.rcptt.tesla.core.protocol.ProtocolFactory;
import org.eclipse.rcptt.tesla.core.protocol.raw.Element;
import org.eclipse.rcptt.tesla.core.protocol.raw.Response;
import org.eclipse.rcptt.tesla.ecl.impl.TeslaBridge;
import org.eclipse.rcptt.tesla.ecl.internal.impl.TeslaImplPlugin;
import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.GetProperty;
import org.eclipse.rcptt.tesla.ecl.model.MessageBoxInfo;
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;
import org.eclipse.rcptt.tesla.ecl.model.VerifyHandler;

public class GetPropertyService implements ICommandService {

	public IStatus service(Command command, IProcess context) throws InterruptedException, CoreException {
		TeslaBridge.waitDelay();
		GetProperty gp = (GetProperty) command;
		EObject object = gp.getObject();
		if (object instanceof ControlHandler) {
			if (gp.isRaw()) {
				return serviceRawGet(gp, context);
			}
			VerifyHandler handler = TeslaFactory.eINSTANCE.createVerifyHandler();
			Element element = TeslaBridge.find((ControlHandler) object, context);
			handler.setElement(element);
			handler.setAttribute(gp.getName());
			handler.setIndex(gp.getIndex());
			context.getOutput().write(handler);
			return Status.OK_STATUS;
		} else if (object instanceof MessageBoxInfo) {
			final MessageBoxInfo info = (MessageBoxInfo) object;
			try {
				final Field field = info.getClass().getDeclaredField(gp.getName());
				field.setAccessible(true);
				context.getOutput().write(field.get(info).toString());
			} catch (Exception e) {
				return propertyGetError(gp.getName(), e);
			}
			return Status.OK_STATUS;
		}
		return propertyGetError(gp.getName(), new ClassCastException("Can't handle " + object));
	}

	private IStatus propertyGetError(String name, Exception error) {
		return TeslaImplPlugin.err(String.format("Failed to retrieve property '%s'", name), error);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IStatus serviceRawGet(GetProperty gp, IProcess cx) throws CoreException {
		if (!(gp.getObject() instanceof ControlHandler)) {
			return propertyGetError(gp.getName(), new ClassCastException("Can't handle " + gp.getObject()));
		}
		GetPropertyValue gv = ProtocolFactory.eINSTANCE.createGetPropertyValue();
		gv.setElement(TeslaBridge.find((ControlHandler) gp.getObject(), cx));
		gv.setIndex(gp.getIndex());
		gv.setName(gp.getName());
		gv.setAllowRawValues(true);
		Response response = TeslaBridge.getPlayer().safeExecuteCommand(gv);
		if (!(response instanceof ObjectResponse) || ((ObjectResponse) response).getResult() == null) {
			return propertyGetError(gp.getName(), new Exception(response.getMessage()));
		}
		ObjectResponse r = (ObjectResponse) response;
		if (r.getResult() instanceof IStatus) {
			return (IStatus) r.getResult();
		} else if (r.getResult() instanceof AbstractList) {
			AbstractList<Object> data = (AbstractList) r.getResult();
			for (Object elem : data) {
				cx.getOutput().write(elem);
			}
		} else if (r.getResult() instanceof Object[]) {
			Object[] data = (Object[]) r.getResult();
			for (Object elem : data) {
				cx.getOutput().write(elem);
			}
		} else {
			cx.getOutput().write(r.getResult());
		}
		cx.getOutput().close(Status.OK_STATUS);
		return Status.OK_STATUS;
	}
}