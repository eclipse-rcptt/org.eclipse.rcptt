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
package org.eclipse.rcptt.ecl.data.apache.poi.impl.internal.commands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rcptt.ecl.data.apache.poi.impl.internal.EclDataApachePOIImplPlugin;
import org.eclipse.rcptt.ecl.filesystem.EclFile;

public class ExcelFileService {

	private static final String XLS_EXTENSION = "xls";
	private static final String XLSX_EXTENSION = "xlsx";

	public static Workbook createBook(EclFile file, String uri) throws CoreException {
		Workbook book;
		if (uri.endsWith(XLS_EXTENSION)) {
			book = new HSSFWorkbook();
		} else if (uri.endsWith(XLSX_EXTENSION)) {
			book = new XSSFWorkbook();
		} else {
			throw new CoreException(EclDataApachePOIImplPlugin.createErr(
					"Error getting extension of file %s. Only 'xls' and 'xlsx' are supported.", file.toURI()));
		}
		return book;
	}

	public static Workbook readBook(EclFile file) throws CoreException {
		Workbook book;
		try (FileInputStream stream = new FileInputStream(file.toFile())) {
			book = WorkbookFactory.create(stream);
			return book;
		} catch (FileNotFoundException e) {
			throw new CoreException(EclDataApachePOIImplPlugin.createErr(
					e, "File not found %s", file.toURI()));
		} catch (IOException e) {
			throw new CoreException(EclDataApachePOIImplPlugin.createErr(
					e, "Error reading file %s", file.toURI()));
		}
	}

	public static void writeBook(Workbook book, EclFile file) throws CoreException {
		try (FileOutputStream stream = new FileOutputStream(file.toFile())) {
			book.write(stream);
		} catch (FileNotFoundException e) {
			throw new CoreException(EclDataApachePOIImplPlugin.createErr(
					e, "File not found %s", file.toURI()));
		} catch (IOException e) {
			throw new CoreException(EclDataApachePOIImplPlugin.createErr(
					e, "Error writing file %s", file.toURI()));
		}
	}

	public static String getCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			String number = String.valueOf(cell.getNumericCellValue());
			number = number.replaceAll(".0$", "");
			return number;
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return "=" + cell.getCellFormula();
		default:
			return "";
		}
	}

}
