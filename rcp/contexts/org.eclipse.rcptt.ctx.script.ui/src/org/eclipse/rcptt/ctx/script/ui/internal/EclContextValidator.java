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
package org.eclipse.rcptt.ctx.script.ui.internal;

import static org.eclipse.rcptt.ui.utils.ModelUtils.loadContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rcptt.ecl.core.Script;

import org.eclipse.rcptt.core.builder.IQ7ProblemReporter;
import org.eclipse.rcptt.core.builder.IQ7Validator;
import org.eclipse.rcptt.core.internal.validators.ScriptValidator;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.ecl.context.EclContext;
import org.eclipse.rcptt.internal.core.RcpttPlugin;

public class EclContextValidator implements IQ7Validator {
	@Override
	public void validate(IQ7NamedElement element, IQ7ProblemReporter reporter,
			IProgressMonitor monitor) {
		try {
			if (element instanceof IContext) {
				EclContext ctx = loadContext((IContext)element, EclContext.class);
				if (ctx == null)
					return;
				Script ecl = ctx.getScript();
				if (ecl != null && ecl.getContent() != null) {
					String eclCode = ecl.getContent();
					ScriptValidator.validateErrors(eclCode, reporter, (IFile) element.getResource());
					ScriptValidator.validateWarnings(eclCode, reporter, (IFile) element.getResource());
				}
			}
		} catch (Throwable e) {
			RcpttPlugin.log(e);
		}
	}

	@Override
	public void reconcile(IQ7NamedElement content, String eclCode,
			IQ7ProblemReporter reporter, IProgressMonitor nullProgressMonitor) {
	}

}
