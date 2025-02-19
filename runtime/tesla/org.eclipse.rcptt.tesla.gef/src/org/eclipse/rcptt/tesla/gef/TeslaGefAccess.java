/*******************************************************************************
 * Copyright (c) 2009, 2024 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.tesla.gef;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.Tool;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.gef.tools.AbstractTool.Input;
import org.eclipse.gef.tools.ConnectionCreationTool;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.gef.tools.SelectionTool;

import org.eclipse.rcptt.tesla.core.utils.TeslaUtils;
import org.eclipse.rcptt.tesla.internal.core.TeslaCore;

public class TeslaGefAccess {

	public static boolean isPalleteEditPart(EditPart part) {

		if (part == null)
			return false;

		Class<?> palleteEditPartClass = null;
		try {
			if (TeslaUtils.getGEFVersion().getMajor() >= 3
					&& TeslaUtils.getGEFVersion().getMinor() >= 4) {
				palleteEditPartClass = Class
						.forName("org.eclipse.gef.ui.palette.editparts.PaletteEditPart");
			} else {
				palleteEditPartClass = Class
						.forName("org.eclipse.gef.internal.ui.palette.editparts.PaletteEditPart");
			}
		} catch (ClassNotFoundException e) {
			GefActivator.log(e);
		}

		if (palleteEditPartClass != null) {
			return palleteEditPartClass.isInstance(part);
		}

		return false;
	}

	public static EditPart getSourceEditPart(DirectEditManager manager) {
		Field field;
		try {
			field = DirectEditManager.class.getDeclaredField("source");
			field.setAccessible(true);
			Object source = field.get(manager);
			if (source instanceof EditPart) {
				return (EditPart) source;
			}
		} catch (Throwable e) {
			GefActivator.log(e);
		}
		return null;
	}

	private static Object get(Object widget, String fieldName) {
		Field f;
		try {
			Class<? extends Object> cl = widget.getClass();
			f = cl.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(widget);
		} catch (NoSuchFieldException e) {
			return null;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Object getThis(Object widget) {
		return get(widget, "this$0");
	}

	public static Object getArg(Object widget) {
		return get(widget, "arg$1");
	}

	@SuppressWarnings("rawtypes")
	private static Object getField(Object sTool, String fieldName, Class oclass) {
		try {
			Field field = oclass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(sTool);
		} catch (Throwable e) {
			GefActivator.log(e);
		}
		return null;
	}

	public static DragTracker getDragTracker(Tool tool) {
		return (DragTracker) getField(tool, "dragTracker", SelectionTool.class);
	}

	public static Object getDirectEditSource(DirectEditManager manager) {
		Object source = null;
		try {
			Field field;
			field = DirectEditManager.class.getDeclaredField("source");
			field.setAccessible(true);
			source = field.get(manager);
			// System.out.println("@");
		} catch (Throwable e) {
			GefActivator.log(e);
		}
		return source;
	}

	public static EditPart getEditPart(Object helper) {
		EditPart value = null;
		try {
			Field field = helper.getClass().getDeclaredField("part");
			field.setAccessible(true);
			value = (EditPart) field.get(helper);
		} catch (Throwable e) {
			GefActivator.log(e);
		}
		return value;
	}

	public static Object getActiveHelper(Object helper) {
		Object activeHelper = null;
		try {
			Field field = helper.getClass().getDeclaredField("activeHelper");
			field.setAccessible(true);
			activeHelper = field.get(helper);
		} catch (Throwable e) {
			GefActivator.log(e);
		}
		return activeHelper;
	}

	public static Object getEditPartField(DragTracker dragTracker) {
		return getField(dragTracker, "editpart", SelectEditPartTracker.class);
	}

	@SuppressWarnings("rawtypes")
	public static List getOperationSet(DragTracker dragTracker) {
		return (List) getField(dragTracker, "operationSet", AbstractTool.class);
	}

	static boolean createTargetRequest(CreationTool createTool, int x, int y,
			EditPart performAt) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Class<? extends CreationTool> toolClass = createTool.getClass();
		Method method = toolClass.getDeclaredMethod("createTargetRequest",
				new Class<?>[] {});
		if (method != null) {
			method.setAccessible(true);
			CreateRequest creationRequest = (CreateRequest) method.invoke(
					createTool, new Object[0]);
			creationRequest.setLocation(new Point(x, y));
			org.eclipse.gef.commands.Command gefCommand = performAt
					.getCommand(creationRequest);
			// If command are not valid, then try to click
			if ((gefCommand != null && gefCommand.canExecute())) {
				performAt.performRequest(creationRequest);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	static boolean createTargetRequest(FigureUIElement source,
			FigureUIElement target, ConnectionCreationTool createTool,
			EditDomain domain, EditPart performAt)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Class<? extends ConnectionCreationTool> toolClass = createTool
				.getClass();
		Method method = toolClass.getDeclaredMethod("createTargetRequest",
				new Class<?>[] {});
		boolean fine = false;
		if (method != null) {
			method.setAccessible(true);

			// Try out connection command
			CreateConnectionRequest creationRequest = (CreateConnectionRequest) method
					.invoke(createTool, new Object[0]);
			creationRequest.setSourceEditPart(source.getPart());
			creationRequest.setTargetEditPart(target.getPart());
			creationRequest.setLocation(new Point(0, 0));

			org.eclipse.gef.commands.Command gefCommand = target.getPart()
					.getCommand(creationRequest);

			// If command are not valid, then try to click
			if ((gefCommand != null && gefCommand.canExecute())) {
				performAt.performRequest(creationRequest);
				fine = true;
			} else {
				fine = false;
			}
			if (!fine) {
				// Try our connection begin, connection end commands
				CreateConnectionRequest connectionRequest = (CreateConnectionRequest) method
						.invoke(createTool, new Object[0]);
				connectionRequest
						.setType(RequestConstants.REQ_CONNECTION_START);
				connectionRequest.setLocation(new Point(0, 0));
				if (source.getPart().getCommand(connectionRequest) != null) {
					connectionRequest.setSourceEditPart(source.getPart());
					connectionRequest.setTargetEditPart(target.getPart());
					connectionRequest
							.setType(RequestConstants.REQ_CONNECTION_END);
					org.eclipse.gef.commands.Command cmd = target.getPart()
							.getCommand(connectionRequest);
					if (cmd != null && cmd.canExecute()) {
						domain.getCommandStack().execute(cmd);
					}
				}
			}
		}
		return fine;
	}

	public static Input getCurrentInput(AbstractTool tool) {
		try {
			Field currentField = AbstractTool.class.getDeclaredField("current");
			currentField.setAccessible(true);
			return (Input) currentField.get(tool);
		} catch (Throwable e) {
			GefActivator.log(e);
		}
		return null;
	}

	public static EditPart getEditPart(AbstractHandle figure) {
		return (EditPart) getField(figure, AbstractHandle.class, "editpart");
	}

	public static Object getField(Object t, Class<?> cl, String name) {
		try {
			Field field = cl.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(t);
		} catch (Throwable e) {
			TeslaCore.log(e);
		}
		return null;
	}
}
