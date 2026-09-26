/*******************************************************************************
 * Copyright (c) 2009 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ecl.platform.internal.commands;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.ecl.core.BoxedValue;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.platform.commands.SortBy;
import org.eclipse.rcptt.ecl.runtime.CoreUtils;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;

public class SortByService implements ICommandService {

	
	@Override
	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		SortBy sb = (SortBy) command;
		final String field = sb.getField();
		List<Object> pipe = CoreUtils.readPipeContent(context.getInput());
		Collections.sort(pipe, new Comparator<Object>() {

			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			
			public int compare(Object o1, Object o2) {
				if(!(o1 instanceof EObject) || !(o2 instanceof EObject)) {
					return 0;
				}
				EObject eo1 = (EObject) o1;
				EObject eo2 = (EObject) o2;
				Object attr1 = getAttrVal(eo1, field);
				Object attr2 = getAttrVal(eo2, field);
				if(attr1 == null) return -1;
				try {
					if(attr1 instanceof Comparable) {
						return ((Comparable) attr1).compareTo(attr2); 
					}
				} catch(Exception e) {
					return attr1.toString().compareTo(attr2 == null ? "" : attr2.toString());
				}
				return 0;
			}
		});
		for(Object o : pipe) {
			context.getOutput().write(o);
		}
		return Status.OK_STATUS;
	}
	
	private Object getAttrVal(EObject o, String name) {
		EAttribute e = findAttr(o, name);
		if(e == null) {
			return null;
		}
		
		return o.eGet(e);
	}
	private EAttribute findAttr(EObject o, String name) {
		if (name == null && o instanceof BoxedValue ) {
			name = "value";
		}
		for(EAttribute e : o.eClass().getEAllAttributes()) {
			if(e.getName().equals(name)) {
				return e;
			}
		}
		return null;
	}

}
