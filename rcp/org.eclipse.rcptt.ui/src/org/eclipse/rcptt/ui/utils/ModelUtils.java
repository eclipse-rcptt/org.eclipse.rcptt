/*******************************************************************************
 * Copyright (c) 2009, 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.utils;

import static org.eclipse.rcptt.core.model.search.Q7SearchCore.findContextTypeByDocument;
import static org.eclipse.rcptt.core.model.search.Q7SearchCore.findVerificationTypeByDocument;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rcptt.core.ContextType;
import org.eclipse.rcptt.core.ContextTypeManager;
import org.eclipse.rcptt.core.Scenarios;
import org.eclipse.rcptt.core.VerificationType;
import org.eclipse.rcptt.core.VerificationTypeManager;
import org.eclipse.rcptt.core.model.IContext;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ITestCase;
import org.eclipse.rcptt.core.model.ITestSuite;
import org.eclipse.rcptt.core.model.IVerification;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.model.search.Q7SearchCore;
import org.eclipse.rcptt.core.scenario.Context;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.scenario.Scenario;
import org.eclipse.rcptt.core.scenario.SuperContext;
import org.eclipse.rcptt.core.scenario.TestSuite;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.core.workspace.Q7Utils;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.ui.Images;
import org.eclipse.rcptt.ui.context.ContextUIManager;
import org.eclipse.rcptt.ui.context.ContextViewer;
import org.eclipse.rcptt.ui.editors.INamedElementEditor;
import org.eclipse.rcptt.ui.verification.VerificationUIManager;
import org.eclipse.rcptt.ui.verification.VerificationViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class ModelUtils {

	public static String getText(NamedElement element) {
		return element.getName();
	}

	public static Image getImage(NamedElement element) {
		if (element instanceof Scenario) {
			Scenario scenario = (Scenario) element;
			return Images.getImage(Scenarios.isEmpty(scenario) ? Images.SCENARIO_EMPTY
					: Images.SCENARIO);
		}
		if (element instanceof Context) {
			ImageDescriptor image = getContextImage((Context) element);
			if (image == null) {
				return Images.getImage(Images.CONTEXT);
			}
			return manager.getImage(image);
		}
		if (element instanceof TestSuite) {
			return Images.getImage(Images.EXECUTION_SESSION);
		}
		return null;
	}

	public static String getText(IQ7Element element) {
		if (element instanceof IQ7NamedElement) {
			try {
				String nameByDocument = Q7SearchCore.findNameByDocument(element);
				if (nameByDocument != null) {
					return nameByDocument;
				}
				return ((IQ7NamedElement) element).getElementName();
			} catch (ModelException e) {
				// Q7UIPlugin.log(e);
			}
		}
		return element.getName();
	}

	public static Image getImage(IQ7Element element) {
		if (element instanceof ITestCase) {
			ITestCase tc = (ITestCase) element;
			return Images.getImage(Scenarios.isEmpty(tc) ? Images.SCENARIO_EMPTY
					: Images.SCENARIO);
		}

		if (element instanceof IContext) {
			ImageDescriptor image = getContextImage((IContext) element);
			if (image == null) {
				return Images.getImage(Images.CONTEXT);
			}
			return manager.getImage(image);
		}

		if (element instanceof IVerification) {
			ImageDescriptor image = getVerificationImage((IVerification) element);
			if (image == null)
				return Images.getImage(Images.VERIFICATION);
			return manager.getImage(image);
		}

		if (element instanceof ITestSuite) {
			return Images.getImage(Images.EXECUTION_SESSION);
		}

		return null;
	}

	public static ImageDescriptor getContextImage(IContext context) {
		String id = Q7SearchCore.findContextTypeByDocument(context);
		if (id != null) {
			ContextType type = ContextTypeManager.getInstance().getTypeById(id);

			try {
				NamedElement ne = context.getNamedElement();
				if (ne instanceof SuperContext) {
					return getSuperContextImage(ne);
				}
			} catch (Exception e) {
				RcpttPlugin.log(e);
			}
			if (type != null) {
				ContextViewer viewer = ContextUIManager.getInstance().getViewer(type);
				if (viewer != null) {
					return viewer.getIcon();
				}
			}
		}
		try {
			return getContextImage((Context) context.getNamedElement());
		} catch (ModelException e) {
			RcpttPlugin.log(e);
		}
		return null;
	}

	public static ImageDescriptor getVerificationImage(IVerification verification) {
		String id = Q7SearchCore.findVerificationTypeByDocument(verification);
		if (id != null) {
			VerificationType type = VerificationTypeManager.getInstance().getTypeById(id);
			if (type != null) {
				VerificationViewer viewer = VerificationUIManager.getInstance()
						.getViewer(type);
				if (viewer != null)
					return viewer.getIcon();
			}
		}

		try {
			return getVerificationImage((Verification) verification.getNamedElement());
		} catch (ModelException e) {
			RcpttPlugin.log(e);
		}

		return null;
	}

	public static ImageDescriptor getVerificationImage(Verification verification) {
		VerificationType type = VerificationTypeManager.getInstance()
				.getTypeByVerification(verification);
		if (type != null) {
			VerificationViewer viewer = VerificationUIManager.getInstance().getViewer(
					type);
			if (viewer != null)
				return viewer.getIcon();
		}
		return null;
	}

	private static ImageDescriptor getSuperContextImage(NamedElement ne) {
		ImageDescriptor result = null;
		ContextType childTypeType = ContextTypeManager.getInstance().getTypeById(
				((SuperContext) ne).getChildType());
		if (childTypeType != null) {
			ContextViewer viewer = ContextUIManager.getInstance()
					.getViewer(childTypeType);
			if (viewer != null) {
				result = viewer.getIcon();
			}
			if (result != null) {
				result = Images.getOverlayImageBottomRight(result,
						"q7_super_context_prefix" + childTypeType.getId(),
						Images.OVERLAY_SUPER_CTX);
			}

		}
		if (result == null) {
			result = getContextImage((Context) ne);
		}

		return result;
	}

	public static ImageDescriptor getContextImage(Context context) {
		ContextType type = ContextTypeManager.getInstance().getTypeByContext(context);
		if (type != null) {
			ContextViewer viewer = ContextUIManager.getInstance().getViewer(type);
			if (viewer != null) {
				return viewer.getIcon();
			}
		}
		return null;
	}

	public static INamedElementEditor findEditor(IQ7NamedElement element) {
		final IFile file = (IFile) element.getResource();
		return findEditor(file);
	}

	public static INamedElementEditor findEditor(NamedElement element) {
		final IFile file = Q7Utils.getLocation(element);
		return findEditor(file);
	}

	private static INamedElementEditor findEditor(final IFile file) {
		if (file != null) {
			final INamedElementEditor[] editor = new INamedElementEditor[1];
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					FileEditorInput input = new FileEditorInput(file);
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null)
						return;
					IWorkbenchPage page = window.getActivePage();
					if (page == null)
						return;
					IEditorReference[] references = page.findEditors(input, null,
							IWorkbenchPage.MATCH_INPUT);
					for (IEditorReference reference : references) {
						IEditorPart part = reference.getEditor(true);
						if (part instanceof INamedElementEditor) {
							editor[0] = (INamedElementEditor) part;
							return;
						}
					}
				}
			});
			return editor[0];
		}
		return null;
	}

	/** Loads context lazily, checking its type first */
	public static <T> T loadContext(IContext context, Class<T> clazz) {
		try {
			String typeId = findContextTypeByDocument(context);
			ContextType type = ContextTypeManager.getInstance().getTypeById(typeId);
			if (type == null)
				return null;
			if (!clazz.isAssignableFrom(type.getEClass().getInstanceClass()))
				return null;
			NamedElement namedElement = context.getNamedElement();
			return clazz.cast(namedElement);
		} catch (ModelException e) {
			RcpttPlugin.log("Can't load " + context.getName() + " as " + clazz.getSimpleName(), e);
			return null;
		}
	}

	/** Loads verification lazily, checking its type first */
	public static <T> T loadVerification(IVerification verification, Class<T> clazz) {
		try {
			String typeId = findVerificationTypeByDocument(verification);
			VerificationType type = VerificationTypeManager.getInstance().getTypeById(typeId);
			if (type == null)
				return null;
			if (!clazz.isAssignableFrom(type.getEClass().getInstanceClass()))
				return null;
			NamedElement namedElement = verification.getNamedElement();
			return clazz.cast(namedElement);
		} catch (ModelException e) {
			RcpttPlugin.log("Can't load " + verification.getName() + " as " + clazz.getSimpleName(), e);
			return null;
		}
	}

	private static ImageManager manager = new ImageManager();

}
