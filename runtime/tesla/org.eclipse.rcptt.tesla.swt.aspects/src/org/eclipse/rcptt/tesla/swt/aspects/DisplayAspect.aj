package org.eclipse.rcptt.tesla.swt.aspects;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rcptt.sherlock.core.SherlockTimerRunnable;
import org.eclipse.rcptt.tesla.core.am.AspectManager;
import org.eclipse.rcptt.tesla.core.context.ContextManagement;
import org.eclipse.rcptt.tesla.core.context.ContextManagement.Context;
import org.eclipse.rcptt.tesla.swt.dialogs.SWTDialogManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager;
import org.eclipse.rcptt.tesla.swt.events.TeslaEventManager.HasEventKind;
import org.eclipse.rcptt.tesla.swt.events.TeslaTimerExecManager;
import org.eclipse.rcptt.tesla.swt.profiling.ProfilingEventManager;

public aspect DisplayAspect {
	private static final String WEH = "org.eclipse.ui.statushandlers.WorkbenchErrorHandler";
	private static final String WIN_CL = "org.eclipse.jface.window.Window";
	private static final String MODAL_CTX = "org.eclipse.jface.operation.ModalContext$ModalContextThread";

	public DisplayAspect() {
		AspectManager.activateAspect(SWTAspectActivator.PLUGIN_ID, this
				.getClass().getName());
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(Display display): execution(boolean Display.sleep()) &&
	 target(display) {
		if (TeslaEventManager.getManager().hasListeners()) {
			// Try to skip BusyIndicator context.
			Display current = Display.getCurrent();
			if (current == null) {
				return proceed(display);
			}
			try {
				ContextManagement.enterContext();
				Context currentContext = ContextManagement.peekContext();

				// Filter modal context blocks
				boolean inModalContext = currentContext.contains(MODAL_CTX,
						"block");
				boolean inWindowLoop = currentContext.contains(WIN_CL,
						"runEventLoop");
				boolean inIDEWorkbenchErrorHandler = currentContext.contains(
						WEH, "showStatusAdapter");
				if (inModalContext && inWindowLoop) {
					int modalCtx = currentContext.indexOf(MODAL_CTX, "block");
					int windowLoop = currentContext.indexOf(WIN_CL,
							"runEventLoop");
					if (windowLoop < modalCtx) {
						inModalContext = false;
					}
				}
				if (inModalContext && inIDEWorkbenchErrorHandler) {
					int modalCtx = currentContext.indexOf(MODAL_CTX, "block");
					int windowLoop = currentContext.indexOf(
							WEH, "showStatusAdapter");
					if (windowLoop < modalCtx) {
						inModalContext = false;
					}
				}
				boolean nonModalContext = (!inModalContext
						&& !currentContext
								.contains(
										"org.eclipse.ui.dialogs.ElementTreeSelectionDialog",
										"create") && !currentContext.contains(
						WIN_CL, "create"));

				if (nonModalContext
						|| TeslaEventManager.getManager().isNoWaitForJob()) {
					boolean result = TeslaEventManager.getManager()
							.doProcessing(currentContext);
					if (!result) {
						Thread.sleep(10);
						return false;
					}
				}
				return false;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return true;
			} finally {
				ContextManagement.exitContext();
			}
		}
		return proceed(display);
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	String around(FileDialog dialog): execution(String FileDialog.open()) && target(dialog) {
		if (TeslaEventManager.getManager().hasListeners()) {
			if (SWTDialogManager.hasFileDialogInfo()) {
				try {
					String dialogResult = null;
					dialogResult = SWTDialogManager.getFileDialogResult();
					String filterPath = SWTDialogManager
							.getFileDialogFilterPath();
					Field filterPathField = FileDialog.class
							.getDeclaredField("filterPath");
					filterPathField.setAccessible(true);
					filterPathField.set(dialog, filterPath);

					String[] files = SWTDialogManager
							.getFileDialogFilesList(filterPath);
					Field fileNamesField = FileDialog.class
							.getDeclaredField("fileNames");
					fileNamesField.setAccessible(true);
					fileNamesField.set(dialog, files);

					if (files.length > 0) {
						Field fileNameField = FileDialog.class
								.getDeclaredField("fileName");
						fileNameField.setAccessible(true);
						fileNameField.set(dialog, files[0]);
					}

					return dialogResult;
				} catch (Throwable e) {
					SWTAspectActivator.log(e);
				}

				TeslaEventManager.getManager().unhandledNativeDialog(
						FileDialog.class, dialog.getText());
				return "Unrecorded file dialog result";
			}
		}
		return proceed(dialog);
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	String around(DirectoryDialog dialog): execution(String DirectoryDialog.open()) && target(dialog) {
		if (TeslaEventManager.getManager().hasListeners()) {
			try {
				if (SWTDialogManager.hasFolderDialogInfo()) {
					return SWTDialogManager.getFolderDialogResult();
				}
				TeslaEventManager.getManager().unhandledNativeDialog(
						DirectoryDialog.class, dialog.getText());
				return "Unrecorded directory dialog result";
			} catch (Throwable e) {
				SWTAspectActivator.log(e);
			}
		}
		return proceed(dialog);
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(MessageBox dialog): execution(int MessageBox.open()) && target(dialog) {
		if (!TeslaEventManager.getManager().getShowingAlert()) {
			try {
				if (TeslaEventManager.getManager().hasListeners()) {
					SWTDialogManager.setMessageBoxInfo(dialog);
					if (SWTDialogManager.hasMessageBoxInfo()) {
						return SWTDialogManager.getMessageBoxResult();
					}
					TeslaEventManager.getManager().unhandledNativeDialog(
							MessageBox.class,
							"Unrecorded message dialog: " + dialog.getMessage()
									+ ":" + dialog.getText());
					return SWT.NO;
				}
			} catch (Throwable e) {
				SWTAspectActivator.log(e);
			}
		}
		return proceed(dialog);
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(FontDialog dialog): execution(org.eclipse.swt.graphics.FontData FontDialog.open()) && target(dialog) {
		try {
			if (TeslaEventManager.getManager().hasListeners()) {
				if (SWTDialogManager.hasFontInfo()) {
					return SWTDialogManager.getFontResult();
				}
				TeslaEventManager.getManager().unhandledNativeDialog(
						MessageBox.class,
						"Unrecorded font dialog: " + dialog.getText());
				return null;
			}
		} catch (Throwable e) {
			SWTAspectActivator.log(e);
		}
		return proceed(dialog);
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(ColorDialog dialog): execution(org.eclipse.swt.graphics.RGB ColorDialog.open()) && target(dialog) {
		try {
			if (TeslaEventManager.getManager().hasListeners()) {
				if (SWTDialogManager.hasColorInfo()) {
					RGB value = SWTDialogManager.getColorResult();
					dialog.setRGB(value);
					return value;
				}
			}
		} catch (Throwable e) {
			SWTAspectActivator.log(e);
		}
		return proceed(dialog);
	}

	// Mouse cursor positioning tweak
	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(Display display): execution(org.eclipse.swt.graphics.Point Display.getCursorLocation()) &&
	 target(display) {
		org.eclipse.swt.graphics.Point point = (org.eclipse.swt.graphics.Point) proceed(display);
		try {
			if (TeslaEventManager.getManager().hasListeners()) {
				if (!TeslaEventManager.getManager().hasListeners()) {
					return point;
				}
				Point location = TeslaEventManager.getManager()
						.getCursotLocation(point);
				return location;
			}
		} catch (Throwable e) {
			SWTAspectActivator.log(e);
		}
		return point;
	}

	// Active shell tweak
	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(Display display): execution(org.eclipse.swt.widgets.Shell Display.getActiveShell()) && target(display) {
		Shell activeShell = (Shell) proceed(display);
		if (TeslaEventManager.getManager().hasListeners()) {
			try {
				if (activeShell == null) {
					Shell activeShell2 = TeslaEventManager.getActiveShell();
					if (activeShell2 != null && !activeShell2.isDisposed()) {
						return activeShell2;
					}
					if (activeShell2 != null && activeShell2.isDisposed()) {
						TeslaEventManager.setActiveShell(null);
					}
					// Check for first SDK window or any visible window with
					// title.
					Shell[] shells = display.getShells();
					for (Shell shell : shells) {
						String pattern = shell.getText();
						int sdkIndex = pattern.indexOf("- Eclipse SDK");
						if (!shell.isDisposed() && sdkIndex != -1
								&& shell.isVisible()) {
							return shell;
						}
					}
					for (Shell shell : shells) {
						String pattern = shell.getText();
						if (!shell.isDisposed() && shell.isVisible()
								&& pattern.trim().length() > 0) {
							return shell;
						}
					}
				}

				activeShell = fixInvisibleShell(activeShell);

				if (activeShell != null && !activeShell.isDisposed()) {
					return activeShell;
				}
				return null;
			} catch (Throwable e) {
				SWTAspectActivator.log(e);
			}
		}
		return activeShell;
	}

	private static Set<String> shellsToFix = new HashSet<String>();
	static {
		shellsToFix
				.add("org.eclipse.ui.internal.cheatsheets.dialogs.CheatSheetCategoryBasedSelectionDialog");
	}

	private static Shell fixInvisibleShell(Shell shell) {
		if (shell == null || shell.isVisible())
			return shell;

		Object data = shell.getData();
		if (data == null)
			return shell;
		if (!shellsToFix.contains(data.getClass().getName()))
			return shell;

		Composite parent = shell.getParent();
		if (parent != null)
			return parent.getShell();
		return shell;
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(Display display, Runnable async): execution(void Display.syncExec(Runnable)) && target( display ) && args(async) {
		Context ctx = null;
		if (TeslaEventManager.getManager().hasListeners()) {
			try {
				ProfilingEventManager.getDefault().asyncAdded(async, true);
				// Runnable called
				ctx = ContextManagement.currentContext();
				String eventName = getRunnableName(async);
				if (eventName != null && !TeslaTimerExecManager.isTimerIgnored(eventName)) {
					TeslaEventManager.getManager().hasEvent(HasEventKind.sync, eventName);
				}
				TeslaEventManager.getManager().syncExecCalled(ctx);
			} catch (Throwable e) {
				SWTAspectActivator.log(e);
			}
		}
		try {
			Object value = proceed(display, async);
			return value;
		} finally {
			if (TeslaEventManager.getManager().hasListeners()) {
				try {
					TeslaEventManager.getManager().syncExecEnd(ctx);
					ProfilingEventManager.getDefault().asyncDone(async);
				} catch (Throwable e) {
					SWTAspectActivator.log(e);
				}
			}
		}
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(): execution(void org.eclipse.swt.widgets.RunnableLock.run()) {
		Runnable value = null;
		if (TeslaEventManager.getManager().hasListeners()) {
			try {
				int len = ProfilingEventManager.getDefault()
						.getListenersLength();
				if (len > 0) {
					try {
						Object target = thisJoinPoint.getTarget();
						Field runable = target.getClass().getDeclaredField(
								"runnable");
						runable.setAccessible(true);
						value = (Runnable) runable.get(target);
						ProfilingEventManager.getDefault().asyncRunning(value);
					} catch (Throwable e) {
						SWTAspectActivator.log(e);
					}
				}
			} catch (Throwable e) {
				SWTAspectActivator.log(e);
			}
		}
		Object result = proceed();
		if (TeslaEventManager.getManager().hasListeners()) {
			try {
				int len = ProfilingEventManager.getDefault()
						.getListenersLength();
				if (len > 0 && value != null) {
					ProfilingEventManager.getDefault().asyncDone(value);
				}
			} catch (Throwable e) {
				SWTAspectActivator.log(e);
			}
		}
		return result;
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	before(Display display, Runnable async): execution(void Display.asyncExec(Runnable)) && target( display ) && args(async) {
		// Runnable called
		try {
			if (TeslaEventManager.getManager().hasListeners()) {
				String eventName = getRunnableName(async);
				if (eventName != null && !TeslaTimerExecManager.isTimerIgnored(eventName)) {
					TeslaEventManager.getManager().hasEvent(HasEventKind.async, eventName);
				}

				if (async != null) {
					ProfilingEventManager.getDefault().asyncAdded(async, false);
				}
			}
		} catch (Throwable e) {
			SWTAspectActivator.log(e);
		}
	}

	// GTK
	@SuppressAjWarnings({ "adviceDidNotMatch", })
	Object around(): execution(void
			org.eclipse.swt.internal.gtk.OS.gdk_window_focus(int, int)) {
		if (TeslaEventManager.getManager().hasListeners()) {
			return 0;
		}
		return proceed();
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(): execution(void
			org.eclipse.swt.internal.gtk.OS.XSetInputFocus(int, int, int, int)) {
		if (TeslaEventManager.getManager().hasListeners()) {
			return 0;
		}
		return proceed();
	}

	boolean around(Display display):
		execution(boolean org.eclipse.swt.widgets.Display.runTimers()) && target(display) {
		boolean value = proceed(display);
		if (TeslaEventManager.getManager().hasListeners()) {
			TeslaTimerExecManager.getManager().afterRunTimers(display);
		}
		return value;
	}

	// timer Exec
	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(Display display, int time, Runnable run):execution(void org.eclipse.swt.widgets.Display.timerExec(int, Runnable)) && target(display) && args(time,run) {
		try {
			if (TeslaEventManager.getManager().hasListeners()) {
				boolean asTimer = true;
				try {
					if (TeslaEventManager.getManager().hasListeners()) {
						if (time != 0) {
							TeslaTimerExecManager.getManager().recordTimerExec(
									run, time);
							String name = getRunnableName(run);
							if (name != null && name.startsWith("org.eclipse.jface")) {
								TeslaEventManager.getManager().hasEvent(HasEventKind.timer, name);
								display.asyncExec(run);
								asTimer = false;
								return null;
								// return proceed(0, run);
							}
							if (name != null && TeslaTimerExecManager.getManager()
									.isNeedNullify(run, time)) {
								TeslaEventManager.getManager().hasEvent(HasEventKind.timer, name);
								asTimer = false;
								display.asyncExec(run);
								// return proceed(0, run);
								return null;
							}
							if (name != null && !TeslaTimerExecManager.isTimerIgnored(name)) {
								TeslaEventManager.getManager().hasEvent(HasEventKind.timer, name);
							}
						}
					}
				} finally {
					if (asTimer) {
						ProfilingEventManager.getDefault().timerAdded(run);
					}
				}
			}
		} catch (Throwable e) {
			SWTAspectActivator.log(e);
		}
		return proceed(display, time, run);
	}

	private String getRunnableName(Runnable run) {
		if (run == null) {
			return null;
		}
		String name = run.getClass().getName();
		if (run instanceof SherlockTimerRunnable) {
			name = ((SherlockTimerRunnable) run)
					.getRunnable().getClass().getName();
		}
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			if (stackTrace[i].getClassName().startsWith(name)) {
				name = stackTrace[i].getClassName() + "." + stackTrace[i].getMethodName() + ":"
						+ stackTrace[i].getLineNumber();
				break;
			}
			if (stackTrace[i].getClassName().equals("org.eclipse.rcptt.tesla.internal.ui.player.SWTUIPlayer")
					&& stackTrace[i].getMethodName().equals("exec")) {
				name = stackTrace[i + 1].getClassName() + "." + stackTrace[i + 1].getMethodName() + ":"
						+ stackTrace[i + 1].getLineNumber();
				break;
			}
		}
		return name;
	}

	Object around(Control ctrl):
		execution( boolean org.eclipse.swt.widgets.Control.forceFocus()) && target(ctrl) {
		Boolean focus = (Boolean) proceed(ctrl);
		TeslaEventManager.getManager().setForceFocusControl(ctrl);
		return focus;
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(Display display): execution( Control org.eclipse.swt.widgets.Display.getFocusControl() ) && target(display) {
		if (TeslaEventManager.getManager().hasListeners()) {
			try {
				TeslaEventManager.updateActiveShell();
			} catch (Throwable e) {
				SWTAspectActivator.log(e);
			}
		}
		return proceed(display);
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	Object around(MessageBox box):
		execution(int org.eclipse.swt.widgets.MessageBox.open()) && target(box) {
		if (SWTDialogManager.isCancelAllMessageBoxes()) {
			return SWT.NO;
		}
		return proceed(box);
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	before(Shell shell): execution(org.eclipse.swt.widgets.Shell.new(..)) && target(shell) {
		try {
			Exception e = new Exception();
			StackTraceElement[] trace = e.getStackTrace();
			for (int i = 1; i < trace.length; i++) {
				String clName = trace[i].getClassName();
				if (!clName.contains("Shell")
						&& !clName.contains("sun.reflect")
						&& !clName.contains("java.lang.reflect")) {
					int pos = clName.lastIndexOf('.');
					if (pos != -1) {
						clName = clName.substring(pos + 1);
					}
					TeslaEventManager.getManager().setShellCreationMethod(shell,
							clName, trace[i].getMethodName() + "()");
					return;
				}
			}
		} catch (Throwable e) {
			SWTAspectActivator.log(e);
		}
	}

	// DO NOT BEEP on replay.
	@SuppressAjWarnings("adviceDidNotMatch")
	void around():
		execution(public void org.eclipse.swt.widgets.Display.beep()) {
		if (TeslaEventManager.getManager().hasListeners()) {
			return;
		}
		proceed();
	}
}
