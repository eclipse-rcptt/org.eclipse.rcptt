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
package org.eclipse.rcptt.verifications.log.persistence;

import static java.lang.String.format;
import static org.eclipse.rcptt.util.StringUtils.nullToEmpty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.core.persistence.plain.IPlainConstants;
import org.eclipse.rcptt.core.persistence.plain.IPlainTextPersistenceExtension;
import org.eclipse.rcptt.core.persistence.plain.PlainTextPersistenceModel;
import org.eclipse.rcptt.util.FileUtil;
import org.eclipse.rcptt.verifications.log.ErrorLogVerification;
import org.eclipse.rcptt.verifications.log.LogEntryPredicate;
import org.eclipse.rcptt.verifications.log.LogFactory;

public class ErrorLogVerificationPlainExtension implements
		IPlainTextPersistenceExtension {

	private static final String ERROR_LOG_VERIFICATION_FILE = ".errorlog.verification";
	private static final String INCLUDE_CONTEXTS = "INCLUDE CONTEXTS: ";
	
	public ErrorLogVerificationPlainExtension() {
	}

	@Override
	public boolean isContentEntryOptional(EList<EObject> contents) {
		return contents.get(0) instanceof ErrorLogVerification;
	}

	private static String predicateToLine(LogEntryPredicate p) {
		//Message separator marks last field, so that we are able to extend format adding fields before it.
		return format("%d %d %s Message:%s",
				p.getSeverityMask(),
				p.getCode(),
				nullToEmpty(p.getPluginPattern()).replaceAll("\n| ", ""),
				nullToEmpty(p.getMessagePattern()).replaceAll("\n", " ")
				);
	}
	
	private static LogEntryPredicate parsePredicate(String line) throws IOException {
		String data[] = line.split(" ", 4);
		if (data.length != 4) {
			throw new IOException("Invalid format "+line);
		}
		LogEntryPredicate rv = LogFactory.eINSTANCE.createLogEntryPredicate();
		rv.setSeverityMask(Integer.valueOf(data[0]));
		rv.setCode(Integer.valueOf(data[1]));
		rv.setPluginPattern(data[2]);
		if (!data[3].startsWith("Message:")) {
			throw new IOException("No message found in "+line);
		}
		rv.setMessagePattern(data[3].substring(8));
		return rv;
	}
 
	private static void savePredicates(List<LogEntryPredicate> predicates, Writer output) throws IOException {
		for (LogEntryPredicate predicate: predicates) {
			String line = predicateToLine(predicate);
			assert !line.contains("\n");
			output.write(line);
			output.write("\n");
		}
	}
	
	@Override
	public void updateAttributes(
			PlainTextPersistenceModel plainTextPersistenceModel,
			Map<String, String> masterAttributes, EObject eObject) {
		if (eObject instanceof ErrorLogVerification) {
			ErrorLogVerification elv = (ErrorLogVerification) eObject;
			OutputStream store = plainTextPersistenceModel
					.store(ERROR_LOG_VERIFICATION_FILE);
			OutputStreamWriter writer = new OutputStreamWriter(store, IPlainConstants.ENCODING_OBJECT);
			try {
				writer.write(INCLUDE_CONTEXTS + elv.isIncludeContexts() + "\n");
				if (!elv.getRequired().isEmpty()) {
					writer.write("REQUIRED:\n");
					savePredicates(elv.getRequired(), writer);
				}
				if (!elv.getAllowed().isEmpty()) {
					writer.write("ALLOWED:\n");
					savePredicates(elv.getAllowed(), writer);
				}
				if (!elv.getDenied().isEmpty()) {
					writer.write("DENIED:\n");
					savePredicates(elv.getDenied(), writer);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				FileUtil.safeClose(writer);
			}
		}
	}

	@Override
	public void updateMetadata(
			PlainTextPersistenceModel plainTextPersistenceModel,
			Map<String, String> header, EObject eObject) {
		if (eObject instanceof ErrorLogVerification) {
			ErrorLogVerification elv = (ErrorLogVerification) eObject;
			InputStream stream = plainTextPersistenceModel
					.read(ERROR_LOG_VERIFICATION_FILE);
			if (stream == null) {
				throw new RuntimeException("Can't read verification contents");
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, IPlainConstants.ENCODING_OBJECT));
			try {
				List<LogEntryPredicate> predicates = elv.getRequired();
				while (reader.ready()) {
					String line = reader.readLine();
					if (line == null)
						return;
					else if (line.startsWith(INCLUDE_CONTEXTS))
						elv.setIncludeContexts(Boolean.parseBoolean(line.substring(INCLUDE_CONTEXTS.length())));
					else if (line.contentEquals("REQUIRED:"))
						predicates = elv.getRequired();
					else if (line.contentEquals("ALLOWED:"))
						predicates = elv.getAllowed();
					else if (line.contentEquals("DENIED:"))
						predicates = elv.getDenied();
					else if (!line.isEmpty()) {
						predicates.add(parsePredicate(line));
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				FileUtil.safeClose(reader);
			}
		}
	}

	@Override
	public void performObjectPreSaveUpdate(EObject eObject) {
		if (eObject instanceof ErrorLogVerification) {
			ErrorLogVerification elv = (ErrorLogVerification) eObject;
			elv.getAllowed().clear();
			elv.getDenied().clear();
			elv.getRequired().clear();
		}
	}

	@Override
	public String getTextContentType(String fName) {
		if (ERROR_LOG_VERIFICATION_FILE.equals(fName)) {
			return "text/errorlog-verification";
		}
		return null;
	}
}
