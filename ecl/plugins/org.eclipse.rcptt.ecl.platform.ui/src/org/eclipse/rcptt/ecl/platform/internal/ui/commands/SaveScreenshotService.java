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
package org.eclipse.rcptt.ecl.platform.internal.ui.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.platform.ui.commands.SaveScreenshot;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.util.ShellUtilsProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

public class SaveScreenshotService implements ICommandService {

	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		SaveScreenshot saveScreenshot = (SaveScreenshot) command;
		if (saveScreenshot.getOut()==null) {
			return createError("No output specified");
		}
		File output = new File(saveScreenshot.getOut());
		if (output.exists() && !output.isFile()) {
			return createError("File %s exists and is a directory",
					output.getAbsolutePath());
		}

		File parent = output.getParentFile();
		if (parent.exists() && !parent.isDirectory()) {
			return createError("File %s exists and not a directory",
					parent.getAbsolutePath());
		}

		if (!parent.exists() && !parent.mkdirs()) {
			return createError("Cannot create directory %s",
					parent.getAbsolutePath());
		}

		Object input = context.getInput().take(10000);
		if (!isValidInput(input)) {
			return createError(
					"Expected control or tree/table item, but got %s from input pipe",
					input);
		}

		return writeData(
				output,
				takeScreenshot(input, formatFromExt(ext(output), SWT.IMAGE_PNG)));
	}

	private static IStatus writeData(File file, byte[] data) {
		try (FileOutputStream out = new FileOutputStream(file)) {
			out.write(data);
			return Status.OK_STATUS;
		} catch (IOException e) {
			return Utils.error("Error saving screenshot: " + e.getMessage(), e);
		}
	}

	private static Map<String, Integer> exts = new HashMap<String, Integer>();
	static {
		exts.put(".png", SWT.IMAGE_PNG);
		exts.put(".jpg", SWT.IMAGE_JPEG);
		exts.put(".bmp", SWT.IMAGE_BMP);
	}

	private static int formatFromExt(String ext, int def) {
		ext = ext.toLowerCase();
		return exts.containsKey(ext) ? exts.get(ext) : def;
	}

	private static String ext(File file) {
		String name = file.getName();
		int lastDot = name.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		}
		return name.substring(lastDot);
	}

	private static boolean isValidInput(Object input) {
		return input instanceof Control || input instanceof TreeItem
				|| input instanceof TableItem || input instanceof ToolItem;
	}

	private static Rectangle getScreenBounds(Object input) {
		if (input instanceof Shell) {
			return ((Shell) input).getBounds();
		}
		if (input instanceof Control) {
			Rectangle bounds = getBounds(input);
			return shift(new Rectangle(0, 0, bounds.width, bounds.height), ((Control) input).toDisplay(0, 0));
		}

		Object parent = getParent(input);
		Point location = parent == null ? new Point(0, 0)
				: topLeft(getScreenBounds(parent));
		return shift(getBounds(input), location);
	}

	private static Rectangle getBounds(Object input) {
		if (input instanceof Control) {
			((Control) input).getLocation();
			return ((Control) input).getBounds();
		}
		if (input instanceof TreeItem) {
			return ((TreeItem) input).getBounds();
		}
		if (input instanceof TableItem) {
			return ((TableItem) input).getBounds();
		}
		if (input instanceof ToolItem) {
			return ((ToolItem) input).getBounds();
		}
		throw new IllegalArgumentException(input.getClass().toString());
	}

	private static Point topLeft(Rectangle bounds) {
		return new Point(bounds.x, bounds.y);
	}

	private static Object getParent(Object input) {
		if (input instanceof Shell) {
			return null;
		}
		if (input instanceof Control) {
			return ((Control) input).getParent();
		}
		if (input instanceof TreeItem) {
			return ((TreeItem) input).getParent();
		}
		if (input instanceof TableItem) {
			return ((TableItem) input).getParent();
		}

		if (input instanceof ToolItem) {
			return ((ToolItem) input).getParent();
		}
		throw new IllegalArgumentException(input.getClass().toString());
	}

	public static Shell getShell(Object input) {
		if (input instanceof Control) {
			return ((Control) input).getShell();
		}
		if (input instanceof TreeItem) {
			return ((TreeItem) input).getParent().getShell();
		}
		if (input instanceof TableItem) {
			return ((TableItem) input).getParent().getShell();
		}
		if (input instanceof ToolItem) {
			return ((ToolItem) input).getParent().getShell();
		}
		throw new IllegalArgumentException(input.getClass().toString());
	}

	private static Rectangle shift(Rectangle rect, Point distance) {
		return new Rectangle(rect.x + distance.x, rect.y + distance.y,
				rect.width, rect.height);
	}

	private byte[] takeScreenshot(final Object input, final int format) {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final AtomicReference<byte[]> result = new AtomicReference<byte[]>();
		display.syncExec(new Runnable() {
			public void run() {
				Shell shell = getShell(input);
				if (shell != null) {
					try {
						ShellUtilsProvider.getShellUtils().forceActive(shell);
					} catch (CoreException e) {
						throw new RuntimeException(e);
					}
					display.readAndDispatch();
				}
				Rectangle displayBounds = display.getBounds();
				Rectangle bounds = getScreenBounds(input);
				Rectangle screenshotBounds = displayBounds.intersection(bounds);
				Image image = new Image(display, screenshotBounds.width,
						screenshotBounds.height);
				GC gc = new GC(display);
				gc.copyArea(image, screenshotBounds.x, screenshotBounds.y);
				gc.dispose();

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageLoader imageLoader = new ImageLoader();
				imageLoader.data = new ImageData[] { image.getImageData() };
				imageLoader.save(out, format);
				image.dispose();
				result.set(out.toByteArray());
			}
		});
		return result.get();
	}
	
	private static IStatus createError(String message) {
		return Utils.error(message);
	}
	
	private static IStatus createError(String format, Object... args) {
		return createError(null, format, args);
	}
}
