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
package org.eclipse.rcptt.ecl.doc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class GenerateDocApp implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] args = Platform.getApplicationArgs();
		if (args == null || args.length == 0) {
			printUsage();
			return EXIT_OK;
		}

		String output;

		try {
			output = getOutputArg(args);
		} catch (ArgException e) {
			return printErrUsageAndGetExitCode(e.getMessage());
		}

		File outputFile = new File(output);
		if (!outputFile.isAbsolute()) {
			outputFile = new File(new File(System.getProperty("user.dir")),
					output);
		}

		if (!outputFile.getName().endsWith(".html")) {
			outputFile = new File(outputFile, DEFAULT_OUTPUT);
		}

		if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
			return printErrUsageAndGetExitCode("Cannot create parent folder for output");
		}

		if (outputFile.exists() && !outputFile.canWrite()) {
			return printErrUsageAndGetExitCode("Cannot write to output location");
		}

		FileOutputStream out = new FileOutputStream(outputFile);
		OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

		EclDocWriter w = new EclDocWriter(writer);
		new EclDocAllCommandsWriter().write(w);
		w.finish();

		System.out.println("Documentation generated to " + outputFile.getAbsolutePath());
		return EXIT_OK;
	}

	private static final Integer printErrUsageAndGetExitCode(String errorMessage) {
		System.out.println(errorMessage);
		System.out.println();
		printUsage();
		return EXIT_ERR;
	}

	private static final String DEFAULT_OUTPUT = "output.html";
	private static final Integer EXIT_ERR = new Integer(1);
	private static final String ARG_OUTPUT = "-output";

	private String getOutputArg(String[] args) throws ArgException {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (ARG_OUTPUT.equals(arg)) {
				if (i == args.length - 1) {
					throw new ArgException(
							"No value given for option '-output'");
				}
				return args[i + 1];
			}
		}
		throw new ArgException("Must specify '-output' option");
	}

	private static void printUsage() {
		System.out
				.println("ECL Doc generator. Generates single HTML file with docs for all ECL commands in current installation.");
		System.out.println("Usage: eclipse -application org.eclipse.rcptt.ecl.doc.generator -output /path/to/file");
		System.out.println("\t-output");
		System.out.println("\tPath to put generated documentation at. ");
		System.out.println("\t\tIf it is relative, then it is resolved relative to PWD.");
		System.out.println("\t\tIf it ends with an .html, then it is interpreted as a file, otherwise, as a directory");
		System.out.println("\t\tAll necessary parent directories are created automatically");
	}

	@Override
	public void stop() {
	}

	private static class ArgException extends Exception {
		private static final long serialVersionUID = 1L;

		public ArgException(String message) {
			super(message);
		}
	}

}
