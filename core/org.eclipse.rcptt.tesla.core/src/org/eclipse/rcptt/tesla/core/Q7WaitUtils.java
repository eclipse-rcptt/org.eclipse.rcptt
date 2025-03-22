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
package org.eclipse.rcptt.tesla.core;

import java.util.List;

import org.eclipse.rcptt.tesla.core.info.InfoFactory;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfo;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;

public class Q7WaitUtils {

	public static void startInfo(String kind, String className, Q7WaitInfoRoot root) {
		if (root == null || className == null) {
			return;
		}
		synchronized (root) {
			updateClassName(className, root);
			long currentTime = System.currentTimeMillis();

			Q7WaitInfo info = findInfo(kind, className, root);
			if (info != null) {
				if (infoNodeIsOpen(info)) {
					return;
				}
				root.setTick(root.getTick() + 1);
				info.setLastTick(root.getTick());
				info.setLastStartTime(currentTime);
				info.setTicks(info.getTicks() + 1);
				return;
			}
			info = createInfo(kind, className, root);
			info.setStartTime(currentTime);
			info.setLastStartTime(currentTime);
		}
	}

	public static void finishInfo(String kind, String className, Q7WaitInfoRoot root) {
		if (root == null || className == null) {
			return;
		}
		synchronized (root) {
			updateClassName(className, root);
			long currentTime = System.currentTimeMillis();

			Q7WaitInfo info = findInfo(kind, className, root);
			if (info != null) {
				info.setEndTime(currentTime);
				long duration = info.getEndTime() - info.getLastStartTime();
				info.setDuration(info.getDuration() + duration);
				return;
			}
			info = createInfo(kind, className, root);
			info.setStartTime(root.getStartTime());
			info.setLastStartTime(currentTime);
			info.setEndTime(currentTime);
		}
	}

	public static void updateInfo(String kind, String className, Q7WaitInfoRoot root) {
		if (root == null || className == null) {
			return;
		}
		synchronized (root) {
			updateClassName(className, root);
			long currentTick = root.getTick();
			long currentTime = System.currentTimeMillis();

			Q7WaitInfo info = findInfo(kind, className, root);
			if (info != null) {
				long lastTick = info.getLastTick();
				if (lastTick == currentTick) {
					// This is previous execution counter here
					info.setEndTime(currentTime);
					long duration = info.getEndTime() - info.getLastStartTime();
					info.setDuration(info.getDuration() + duration);
				}
				root.setTick(root.getTick() + 1);
				info.setLastTick(root.getTick());
				info.setLastStartTime(currentTime);
				return;
			}

			info = createInfo(kind, className, root);
			info.setStartTime(currentTime);
			info.setLastStartTime(currentTime);
			info.setEndTime(currentTime);
		}
	}

	private static Q7WaitInfo findInfo(String kind, String className, Q7WaitInfoRoot root) {
		int typeId = getID(kind, root.getTypesNames());
		int classId = getID(className, root.getClassNames());
		if (classId == -1 || typeId == -1) {
			return null;
		}

		for (Q7WaitInfo info : root.getInfos()) {
			if (info.getTypeId() == typeId && info.getClassId() == classId) {
				return info;
			}
		}
		return null;
	}

	private static Q7WaitInfo createInfo(String kind, String className, Q7WaitInfoRoot root) {
		int typeId = getID(kind, root.getTypesNames());
		int classId = getID(className, root.getClassNames());

		if (typeId == -1) {
			typeId = root.getTypesNames().size();
			root.getTypesNames().add(kind);
		}
		if (classId == -1) {
			classId = root.getClassNames().size();
			root.getClassNames().add(className);
		}

		Q7WaitInfo info = InfoFactory.eINSTANCE.createQ7WaitInfo();
		root.getInfos().add(info);
		root.setTick(root.getTick() + 1);

		info.setTypeId(typeId);
		info.setClassId(classId);
		info.setTicks(1);
		info.setLastTick(root.getTick());
		return info;
	}

	private static boolean infoNodeIsOpen(Q7WaitInfo info) {
		return info.getLastStartTime() > info.getEndTime();
	}

	private static int getID(String kind, List<String> values) {
		for (int i = 0; i < values.size(); i++) {
			String typeName = values.get(i);
			if (typeName.equals(kind)) {
				return i;
			}
		}
		return -1;
	}

	private static void updateClassName(String className, Q7WaitInfoRoot info) {
		// Update class name if contains $ to contain method name instead
		int pos = className.indexOf("$");
		if (pos != -1) {
			String methodValue = info.getInnerClassMap().get(className);
			if (methodValue == null) {
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				String clName = className.substring(0, pos);
				if (clName.equals("org.eclipse.jface.util.Throttler")) {
					boolean found = false;
					for (int i1 = 0; i1 < stackTrace.length; i1++) {
						boolean current = stackTrace[i1].getClassName().startsWith(clName);
						if (current) {
							found = true;
						}
						if (found && !current) {
							methodValue = stackTrace[i1].getClassName() + "." + methodAndLocation(stackTrace[i1]);
							break;
						}
					}
				} else {
					for (int i1 = 0; i1 < stackTrace.length; i1++) {
						if (stackTrace[i1].getClassName().startsWith(clName)) {
							methodValue = stackTrace[i1].getMethodName() + ":"
									+ stackTrace[i1].getLineNumber();
							break;
						}
						if (stackTrace[i1].getClassName().equals("org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer")
								&& stackTrace[i1].getMethodName().equals("exec")) {
							methodValue = methodAndLocation(stackTrace[i1 + 1]);
							break;
						}
					}
				}
				if (methodValue != null) {
					info.getInnerClassMap().put(className, /* className.substring(0, pos) + "." + */methodValue);
				}
			}
		}
	}
	
	private static String methodAndLocation(StackTraceElement element) {
		return element.getMethodName() + ":" + element.getLineNumber();
	}

}
