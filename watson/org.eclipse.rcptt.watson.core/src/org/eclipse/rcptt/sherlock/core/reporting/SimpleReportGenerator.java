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
package org.eclipse.rcptt.sherlock.core.reporting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Snaphot;
import org.eclipse.rcptt.tesla.core.TeslaFeatures;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfo;
import org.eclipse.rcptt.tesla.core.info.Q7WaitInfoRoot;

public class SimpleReportGenerator {
	private static final String LINE_SEPARATOR = "\n";

	public String generateContent(Report report) {
		StringBuilder builder = new StringBuilder();
		Node nodes = report.getRoot();
		builder.append("Report:").append(LINE_SEPARATOR);
		printNode(nodes, builder, 1, false);
		builder.append("Q7 Wait details");
		printNode(nodes, builder, 1, true);
		return builder.toString();
	}

	protected boolean printRawProperty(String key) {
		return true;
	}

	public void printNode(Node infoNode, StringBuilder stream, int tabs, boolean includeWaitDetails) {
		appendTabs(stream, tabs);
		stream.append(infoNode.getName())
				.append(" ")
				.append(Long.toString(infoNode.getDuration()));
		stream.append(" {").append(LINE_SEPARATOR);

		EMap<String, EObject> list = infoNode.getProperties();
		for (String key : list.keySet()) {
			if (!printRawProperty(key))
				continue;
			EObject value = list.get(key);
			if (value instanceof Q7WaitInfoRoot) {
				if (includeWaitDetails) {
					printWaitInfo(stream, tabs, key, (Q7WaitInfoRoot) value);
				}
			}
			else {
				if (!includeWaitDetails) {
					appendTabs(stream, tabs + 1).append(key).append("=")
							.append(asValue(value)).append(LINE_SEPARATOR);
				}
			}
		}
		
		printChildren(infoNode.getChildren(), stream, tabs, includeWaitDetails);

		try {
			if (!includeWaitDetails) {
				for (Snaphot child : infoNode.getSnapshots()) {
					printSnapshot(child, stream, tabs + 2);
				}
			}
		} catch (IOException e) {
			// String builder does not throw
			throw new RuntimeException(e);
		}
		appendTabs(stream, tabs).append("}").append(LINE_SEPARATOR);
	}

	protected void printChildren(List<Node> children, StringBuilder stream, int tabs, boolean includeWaitDetails) {
		for (Node child : children) {
			printNode(child, stream, tabs + 2, includeWaitDetails);
		}
	}

	public void printWaitInfo(StringBuilder stream, int tabs, String key, Q7WaitInfoRoot info) {
		List<Q7WaitInfo> infos = new ArrayList<Q7WaitInfo>(info.getInfos());
		if (infos.size() == 0) {
			return;
		}
		Comparator<Q7WaitInfo> comparator = new Comparator<Q7WaitInfo>() {
			@Override
			public int compare(Q7WaitInfo info1, Q7WaitInfo info2) {
				return Long.compare(info1.getDuration(), info2.getDuration());
			}
		};
		Collections.sort(infos, Collections.reverseOrder(comparator));

		boolean isEmpty = true;
		for (Q7WaitInfo q7WaitInfo : infos) {
			if (getType(info, q7WaitInfo) == null) {
				continue;
			}
			if (!TeslaFeatures.isIncludeEclipseMethodsWaitDetails()
					&& getClassName(info, q7WaitInfo).startsWith("org.eclipse")) { //$NON-NLS-1$
				continue;
			}
			if (q7WaitInfo.getDuration() == 0) {
				continue;
			}
			isEmpty = false;
			break;
		}
		if (isEmpty) {
			return;
		}
		appendTabs(stream, tabs + 4).append("--> Wait details <--")
				.append(LINE_SEPARATOR);
		for (Q7WaitInfo i : infos) {
			long totalTime = i.getDuration();
			String type = getType(info, i);
			String className = getClassName(info, i);
			if (type == null) {
				continue;
			}
			if (!TeslaFeatures.isIncludeEclipseMethodsWaitDetails()
					&& className.startsWith("org.eclipse")) { //$NON-NLS-1$
				continue;
			}
			if (totalTime == 0) {
				continue;
			}
			appendTabs(stream, tabs + 8).append(type).append(": ")
					.append(className);

			if (totalTime != 0)
				stream.append(", total time: ").append(Long.toString(totalTime));

			stream.append(LINE_SEPARATOR);
		}
	}

	public static String getType(Q7WaitInfo q7WaitInfo) {
		Q7WaitInfoRoot info = (Q7WaitInfoRoot) q7WaitInfo.eContainer();
		if (info == null)
			throw new NullPointerException("Unrooted object");
		return getType(info, q7WaitInfo);
	}

	public static String getType(Q7WaitInfoRoot info, Q7WaitInfo q7WaitInfo) {
		String type = info.getTypesNames().get(q7WaitInfo.getTypeId());
		if (!TeslaFeatures.isIncludeIgnoredWaitDetails() && type.contains("(ignored)")) {
			return null;
		}
		return type;
	}

	private String asValue(EObject eObject) {
		StringBuilder b = new StringBuilder();
		try {
			toString(b, 0, eObject);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return "(" + b.toString().replace("\n", " ") + ")";
	}

	public <T extends Appendable> T printSnapshot(Snaphot child, T stream, int tabs) throws IOException {
		appendTabs(stream, tabs);
		stream.append(" %");
		stream.append(" object:").append(LINE_SEPARATOR);
		toString(stream, tabs + 1, child.getData()).append(LINE_SEPARATOR);

		EMap<String, EObject> list = child.getProperties();
		for (String key : list.keySet()) {
			appendTabs(stream, tabs + 1).append(key).append("=")
					.append(asValue(list.get(key))).append(LINE_SEPARATOR);
		}
		appendTabs(stream, tabs).append("%").append(LINE_SEPARATOR);
		return stream;
	}

	protected static <T extends Appendable> T appendTabs(T stream, int tabs) {
		for (int i = 0; i < tabs; ++i) {
			try {
				stream.append("  ");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return stream;
	}

	public <T extends Appendable> T toString(T builder, int tabs, EObject obj,
			String... ignores) throws IOException {
		return toString(builder, tabs, obj, true, ignores);
	}

	@SuppressWarnings("rawtypes")
	public <T extends Appendable> T toString(T builder, int tabs, EObject obj,
			boolean skipDefaults, String... ignores) throws IOException {
		if (obj == null) {
			return builder;
		}
		EClass eClass = obj.eClass();
		EList<EStructuralFeature> features = eClass.getEAllStructuralFeatures();
		for (EStructuralFeature f : features) {
			Object eGet = obj.eGet(f);
			boolean needArg = true;
			Object defaultValue = f.getDefaultValue();
			needArg = (defaultValue == null || !defaultValue.equals(eGet));

			if (eGet instanceof List && ((List<?>) eGet).size() == 0) {
				needArg = false;
			}
			if (!skipDefaults) {
				needArg = true;
			}
			for (String ignore : ignores) {
				if (ignore.equals(f.getName())) {
					needArg = false;
					break;
				}
			}
			if (eGet != null && needArg) {
				appendTabs(builder, tabs).append(f.getName()).append('=');
				if (eGet instanceof EObject) {
					builder.append('{').append(LINE_SEPARATOR);
					toString(builder, tabs + 1, (EObject) eGet,
							"eFactoryInstance").append('}');
				} else if (eGet instanceof EList) {
					EList l = (EList) eGet;
					builder.append('[');
					for (int i = 0; i < l.size(); i++) {
						Object va = l.get(i);
						if (va instanceof EObject) {
							if (i != 0) {
								appendTabs(builder, tabs + 2);
							}
							builder.append('{').append(LINE_SEPARATOR);
							toString(builder, tabs + 4, (EObject) va,
									"eFactoryInstance");
							appendTabs(builder, tabs + 2).append('}').append(
									LINE_SEPARATOR);
						}
					}
					builder.append(']');
				} else {
					builder.append("" + eGet).append(LINE_SEPARATOR);
				}
			}
		}
		return builder;
	}

	public static String getClassName(Q7WaitInfoRoot waitRoot, Q7WaitInfo i) {
		String className = waitRoot.getClassNames().get(i.getClassId());
		if (className != null && waitRoot.getInnerClassMap().containsKey(className)) {
			String originalClassName = className;
			int pos = className.indexOf("$");
			if (pos != -1) {
				className = className.substring(0, pos);
			}
			className += '.' + waitRoot.getInnerClassMap().get(originalClassName);
			className += " (" + originalClassName + ")";
		}
		return className;
	}
}
