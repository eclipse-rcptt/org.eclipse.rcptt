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
package org.eclipse.rcptt.ui.launching;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.rcptt.core.model.IQ7Element;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.core.model.ModelException;
import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.core.tags.Tag;
import org.eclipse.rcptt.core.utils.TagsUtil;
import org.eclipse.rcptt.core.workspace.Q7Utils;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.internal.launching.Q7LaunchManager;
import org.eclipse.rcptt.internal.ui.Messages;
import org.eclipse.rcptt.launching.Aut;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.AutManager;
import org.eclipse.rcptt.launching.Q7Launcher;
import org.eclipse.rcptt.launching.utils.TestSuiteUtils;
import org.eclipse.rcptt.ui.launching.aut.AUTConnectionErrorDialog;
import org.eclipse.rcptt.ui.launching.aut.AUTSelectionDialog;
import org.eclipse.rcptt.ui.utils.WorkbenchUtils;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;

import com.google.common.base.Strings;

public class LaunchUtils {
	public static final String PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH = IDebugUIConstants.PLUGIN_ID
			+ ".save_dirty_editors_before_launch"; //$NON-NLS-1$

	private static abstract class SaveableFilter implements ISaveableFilter {
		public abstract boolean select(Saveable saveable,
				IWorkbenchPart[] containingParts);
	}

	/**
	 * @return <code>false</code> if user canceled operation If user says 'no',
	 *         then returning value is still true
	 */
	public static boolean saveChanges(IResource[] context)
			throws ModelException {
		String savePromptMode = DebugUITools.getPreferenceStore().getString(PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
		if (Strings.isNullOrEmpty(savePromptMode))
			savePromptMode = MessageDialogWithToggle.PROMPT;

		if (MessageDialogWithToggle.NEVER.equals(savePromptMode))
			return true;

		// XXX it appears that there are may be many IResource's for the same
		// file, so the paths are used as unique attribute

		final HashSet<IPath> targets = new HashSet<IPath>();
		for (IResource r : context)
			targets.add(r.getFullPath());

		// -- add referenced contexts

		HashSet<IQ7Element> contexts = new HashSet<IQ7Element>();
		for (IResource r : context) {
			IQ7Element e = RcpttCore.create(r);
			if (!(e instanceof IQ7NamedElement))
				continue;

			RcpttCore.getInstance().findAllContexts((IQ7NamedElement) e, contexts);
			RcpttCore.getInstance().findAllVerifications((IQ7NamedElement) e, contexts);
		}

		for (IQ7Element c : contexts)
			targets.add(c.getResource().getFullPath());

		// --

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		return workbench.saveAll(window, window, new SaveableFilter() {

			@Override
			public boolean select(Saveable saveable,
					IWorkbenchPart[] containingParts) {
				for (IWorkbenchPart p : containingParts) {
					if (!(p instanceof IEditorPart))
						continue;

					IEditorPart editor = (IEditorPart) p;
					IResource[] editorContext = getContext(editor);
					for (IResource r : editorContext) {
						if (targets.contains(r.getFullPath()))
							return true;
					}
				}
				return false;
			}

		}, MessageDialogWithToggle.PROMPT.equals(savePromptMode));
	}

	public static IResource[] getContext() {
		IResource[] resources = null;
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
			if (page != null) {
				ISelection selection = page.getSelection();
				resources = getContext(selection);
				if (resources.length == 0) {
					IEditorPart part = page.getActiveEditor();
					if (part != null) {
						resources = getContext(part);
					}
				}
			}
		}
		return resources;
	}

	public static IResource[] getContext(ISelection selection) {
		List<IResource> resources = new ArrayList<IResource>();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Iterator<?> it = ss.iterator();
			while (it.hasNext()) {
				doGetContext(it.next(), resources);
			}
		}
		return resources.toArray(new IResource[resources.size()]);
	}

	private static void doGetContext(Object object, List<IResource> resources) {
		if (object instanceof Tag) {
			Tag tag = (Tag) object;
			for (IQ7NamedElement element : TagsUtil.getDistinctTagRefs(tag)) {
				doGetContext(element, resources);
			}
		} else if (object instanceof IQ7NamedElement) {
			IResource res = ((IQ7NamedElement) object).getResource();
			if (res != null) {
				resources.add(res);
			}
		} else if (object instanceof NamedElement) {
			IFile file = Q7Utils.getLocation((NamedElement) object);
			if (file != null) {
				resources.add(file);
			}
		} else {
			IResource res = getAdapter(IResource.class, object);
			if (res != null) {
				resources.add(res);
			}
		}
	}

	public static IResource[] getContext(IEditorPart part) {
		IResource res = (IResource) part.getAdapter(IResource.class);
		if (res == null)
			return new IResource[0];
		return new IResource[] { res };
	}

	public static <T> T getAdapter(Class<T> clazz, Object element) {
		if (element == null)
			return null;
		if (clazz.isInstance(element)) {
			return clazz.cast(element);
		}
		Object adapted = null;
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapted = adaptable.getAdapter(clazz);
		}
		if (adapted == null) {
			adapted = Platform.getAdapterManager().getAdapter(element, clazz);
		}
		return adapted == null ? null : clazz.cast(adapted);
	}

	public static void launchContext(IResource[] context, String mode,
			Map<String, List<List<String>>> variants) {
		launchContext(context, mode, true, variants);
	}

	public static void launchContext(IResource[] context, String mode) {
		launchContext(context, mode, true, null);
	}

	public static void launchVerification(IResource[] verification, String mode) {
		launchVerification(verification, mode, true, null);
	}

	public static void runResource(IResource res) {
		launchContextOrVerification(new IResource[] { res }, "run", false, false, null);
	}

	public static void launchContext(IResource[] context, String mode,
			boolean sort, Map<String, List<List<String>>> variants) {
		launchContextOrVerification(context, mode, sort, true, variants);
	}

	public static void launchVerification(IResource[] verification, String mode,
			boolean sort, Map<String, List<List<String>>> variants) {
		launchVerification(verification, mode, sort, true, variants);
	}

	public static void launchContext(IResource[] context, String mode,
			boolean sort) {
		launchContextOrVerification(context, mode, sort, true, null);
	}

	public static void launchVerification(IResource[] verification, String mode,
			boolean sort) {
		launchVerification(verification, mode, sort, true, null);
	}

	public static void launchContextOrVerification(IResource[] context, String mode,
			boolean sort, boolean promptForSave,
			Map<String, List<List<String>>> variants) {
		try {
			IQ7NamedElement[] elements = TestSuiteUtils.getElements(context,
					false, sort);
			if (elements.length == 0) {
				showNoTestsFoundDialog();
				return;
			}

			if (promptForSave && !saveChanges(context))
				return;

			ILaunchConfiguration config = getLaunchConfig(context, mode, sort,
					variants);
			if (config == null) {
				// This is because of cancel
				return;
			} else {
				DebugUITools.launch(config, mode);
			}
		} catch (CoreException e) {
			if (e.getStatus().getSeverity() == Status.CANCEL) {
				return;
			}
			ErrorDialog.openError(WorkbenchUtils.getShell(),
					Messages.LaunchUtils_CantCreateLaunchConfDialogTitle,
					Messages.LaunchUtils_CantCreateLaunchConfDialogMsg,
					e.getStatus());
		}
	}

	public static void launchVerification(IResource[] verification, String mode,
			boolean sort, boolean promptForSave,
			Map<String, List<List<String>>> variants) {
		launchContextOrVerification(verification, mode, sort, promptForSave, variants);
	}

	public static ILaunchConfiguration getLaunchConfig(IResource[] context,
			String mode, boolean sort, Map<String, List<List<String>>> variants)
			throws CoreException {
		IQ7NamedElement[] elements = TestSuiteUtils.getElements(context, false, sort);
		if (elements.length == 0) {
			return null;
		}
		return Q7Launcher.getLaunchConfiguration(elements, null,
				null, sort, variants);
	}

	public static AutLaunch selectAutLaunch() {
		return selectAutLaunch(null);
	}

	public static void waitForCompletion(ILaunch launch) {
		while (!launch.isTerminated()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void execute(IResource resource, boolean debug)
			throws CoreException {
		String mode = debug ? "debug" : "run";
		waitForCompletion(getLaunchConfig(new IResource[] { resource }, mode,
				false, null).launch(mode, new NullProgressMonitor()));
	}

	public static AutLaunch selectAutLaunch(Shell shell) {
		List<AutLaunch> launches = AutManager.INSTANCE.getLaunches();
		if (launches.size() == 1) {
			return launches.get(0);
		}
		if (shell == null) {
			shell = getShell();
		}
		final AtomicReference<AutLaunch> launch = new AtomicReference<AutLaunch>();
		if (shell == null) {
			shell = getShell();
		}
		final Shell s = shell;
		runInUI(s.getDisplay(), new Runnable() {
			public void run() {
				AUTSelectionDialog dialog = new AUTSelectionDialog(s);
				if (dialog.open() == Window.OK) {
					launch.set(dialog.getResult());
				}
			}
		});
		return launch.get();
	}

	public static Shell getShell() {
		return PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell();
	}

	public static AutLaunch launch(final Aut aut, final Shell shell) {
		final AtomicReference<AutLaunch> launch = new AtomicReference<AutLaunch>();
		runInUI(shell.getDisplay(), new Runnable() {
			public void run() {
				doLaunch(aut, shell, launch);
			}
		});
		return launch.get();
	}

	private static void doLaunch(final Aut aut, final Shell shell,
			final AtomicReference<AutLaunch> launch) {
		final ProgressMonitorDialog progress = new ProgressMonitorDialog(shell);
		final AtomicReference<IStatus> status = new AtomicReference<IStatus>();
		try {
			progress.run(true, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) {
					try {
						launch.set(aut.launch(monitor));
						status.set(Status.OK_STATUS);
					} catch (CoreException e) {
						status.set(e.getStatus());
					} finally {
						if (status.get() == null) {
							status.set(Status.CANCEL_STATUS);
						}
					}
				}
			});
		} catch (InvocationTargetException e1) {
			status.set(RcpttPlugin.createStatus("Failed to launch " + aut.getName(), e1.getCause()));
		} catch (InterruptedException e1) {
			return;
		}
		IStatus s = status.get();
		if (s.matches(IStatus.CANCEL)) {
			return;
		}
		if (!s.isOK()) {
			RcpttPlugin.getDefault().getLog().log(s);
			AUTConnectionErrorDialog.showAUTConnectionError(shell, s,
					aut.getConfig());
			return;
		}
		try {
			ShellUtilsProvider.getShellUtils().forceActive(shell);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static void runInUI(Display display, Runnable runnable) {
		if (display.getThread() == Thread.currentThread()) {
			runnable.run();
		} else {
			display.syncExec(runnable);
		}
	}

	private static void showNoTestsFoundDialog() {
		MessageDialog.openInformation(WorkbenchUtils.getShell(),
				Messages.LaunchUtils_NoQ7TestFoundTitle,
				Messages.LaunchUtils_NoQ7TestFoundDialogMsg);
	}

	public static boolean hasLaunchedTestCases() {
		ILaunchManager launchManager = DebugPlugin.getDefault()
				.getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		for (ILaunch launch : launches) {
			if (Q7LaunchManager.isTestSuiteLauch(launch)
					&& !launch.isTerminated()) {
				return true;
			}
		}
		return false;
	}

	public static Boolean checkCancelDebugSessions(Shell shell) {
		final Shell selectedShell = shell != null ? shell : getShell();
		final Boolean result[] = { Boolean.TRUE };
		runInUI(selectedShell.getDisplay(), new Runnable() {
			public void run() {
				result[0] = Boolean.valueOf(MessageDialog.openQuestion(
						selectedShell, "RCPTT Debug",
						"Debug session are in progress.\n"
								+ "Would you like to cancel it?"));

			}
		});
		return result[0];

	}
}
