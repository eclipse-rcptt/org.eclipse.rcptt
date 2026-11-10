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
package org.eclipse.rcptt.verifications.tree.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.rcptt.core.VerificationProcessor;
import org.eclipse.rcptt.core.ecl.core.model.CreateWidgetVerificationParam;
import org.eclipse.rcptt.core.scenario.Verification;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.internal.core.RcpttPlugin;
import org.eclipse.rcptt.tesla.core.ui.StyleRangeEntry;
import org.eclipse.rcptt.tesla.core.utils.WidgetModels;
import org.eclipse.rcptt.tesla.core.utils.WidgetModels.StyleDiff;
import org.eclipse.rcptt.tesla.ecl.impl.TeslaBridge;
import org.eclipse.rcptt.tesla.internal.ui.player.SWTUIElement;
import org.eclipse.rcptt.verifications.runtime.ErrorList;
import org.eclipse.rcptt.verifications.status.StatusFactory;
import org.eclipse.rcptt.verifications.status.TreeItemStyleVerificationError;
import org.eclipse.rcptt.verifications.status.TreeItemVerificationError;
import org.eclipse.rcptt.verifications.tree.CaptureTreeVerificationData;
import org.eclipse.rcptt.verifications.tree.Cell;
import org.eclipse.rcptt.verifications.tree.Column;
import org.eclipse.rcptt.verifications.tree.ModelTreeNodeAdapter;
import org.eclipse.rcptt.verifications.tree.Row;
import org.eclipse.rcptt.verifications.tree.Tree;
import org.eclipse.rcptt.verifications.tree.TreeComparison;
import org.eclipse.rcptt.verifications.tree.TreeFactory;
import org.eclipse.rcptt.verifications.tree.TreeVerificationUtils;
import org.eclipse.rcptt.verifications.tree.VerifyStyleType;
import org.eclipse.rcptt.verifications.tree.VerifyTreeData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

public class TreeVerificationProcessor extends VerificationProcessor {
	@Override
	public Object start(Verification verification, IProcess process) throws CoreException {
		return new Date();
	}

	@Override
	public void finish(Verification verification, Object data, IProcess process) throws CoreException {
		final VerifyTreeData treeVerification = (VerifyTreeData) verification;
		final SWTUIElement swtuiElement = TeslaBridge.resolveSWTUIElement(treeVerification.getSelector(), process);
		final Widget widget = swtuiElement.widget;
		final ErrorList errors = new ErrorList();

		final Throwable error[] = new Throwable[1];
		widget.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					Tree expectedTree = treeVerification.getTree();
					Tree actualTree = TreeVerificationModeller.getTreeData(widget, false);
					compareTrees(widget, errors, expectedTree, actualTree, treeVerification.isAllowUncapturedChildren(),
							treeVerification.isAllowExtraColumns(), treeVerification.isAllowMissingColumns(),
							treeVerification.isVerifyIcons(), treeVerification.getVerifyStyle(), treeVerification.getExcludedColumns());
				} catch (Throwable e) {
					error[0] = e;
				}
			}
		});
		if (error[0] != null)
			throw new CoreException(RcpttPlugin.createStatus(error[0]));
		errors.throwIfAny(String.format("Tree widget verification '%s' failed:", verification.getName()),
				this.getClass().getPackage().getName(), verification.getId());
	}

	private void compareTrees(final Widget widget, final ErrorList errors, final Tree expectedTree, Tree actualTree,
			final boolean allowUncapturedChildren, final boolean allowExtra, final boolean allowMissing,
			final boolean verifyIcons, final VerifyStyleType verifyStyle, final EList<Integer> excludedColumns) {
		if (expectedTree == null) {
			errors.add("Expected tree is undefined");
			return;
		}

		final EList<Column> expectingColumns = expectedTree.getColumns();
		EList<Column> actualColumns = actualTree.getColumns();
		final int[] mappedInds = (expectingColumns.size() > 0) ? new int[expectingColumns.size()] : new int[] { -1 };
		boolean[] mappedActualCols = new boolean[actualColumns.size()];
		boolean correctMapping = true;

		for (int i = 0; i < expectingColumns.size(); i++) {
			boolean mapped = false;

			for (int j = 0; j < actualColumns.size(); j++) {
				for (Integer excludedColumn : excludedColumns) {
					if (excludedColumn.equals(j)) {
						mappedInds[i] = -1;
						mappedActualCols[j] = true;
						mapped = true;
						break;
					}
				}
				if (!mappedActualCols[j] && expectingColumns.get(i).getName().equals(actualColumns.get(j).getName())
						&& isEqualStrings(expectingColumns.get(i).getTooltip(), actualColumns.get(j).getTooltip())) {
					mappedInds[i] = j;
					mappedActualCols[j] = true;
					mapped = true;
					break;
				}
			}
			if (!mapped) {
				if (allowMissing) {
					mappedInds[i] = -1;
				} else {
					correctMapping = false;
					errors.add("Expected column \"%s\" %s was not found in actual columns list ",
							expectingColumns.get(i).getName(), (expectingColumns.get(i).getTooltip() == null) ? ""
									: String.format("with tooltip", expectingColumns.get(i)));
				}
			}
		}
		for (int i = 0; i < mappedActualCols.length; i++) {
			if (!mappedActualCols[i] && !allowExtra) {
				correctMapping = false;
				errors.add("Column \"%s\" %s is undefined in expected columns list", actualColumns.get(i).getName(),
						(actualColumns.get(i).getTooltip() == null) ? ""
								: String.format("with tooltip", actualColumns.get(i)));
			}
		}
		if (correctMapping) {
			if (verifyIcons) {
				for (int i = 0; i < expectingColumns.size(); i++) {
					if (mappedInds[i] == -1) {
						break;
					}
					String expImgName = TreeVerificationUtils.getDecoratedImagePath(expectingColumns.get(i).getImage());
					String actImgName = TreeVerificationUtils
							.getDecoratedImagePath(expectingColumns.get(mappedInds[i]).getImage());

					if ((expImgName == null && actImgName != null)
							|| (expImgName != null && !expImgName.equals(actImgName))) {
						errors.add("Expected %s for column \"%s\" but was %s",
								(expImgName == null) ? "no icon" : String.format("icon \"%s\"", expImgName),
								expectingColumns.get(i).getName(),
								(actImgName == null) ? "no icon" : String.format("\"%s\"", actImgName));
					}
				}
			}

			TreeComparison<Row> comparison = new TreeComparison<Row>(false) {
				@Override
				public List<TreeItemVerificationError> compare(Row row1, Row row2) {
					List<TreeItemVerificationError> rv = new ArrayList<TreeItemVerificationError>();
					boolean equal = compareRows(rv, row1, row2, widget.getDisplay(), verifyStyle, verifyIcons,
							expectingColumns, mappedInds);
					Assert.isTrue(equal == rv.isEmpty());
					return rv;
				}

				@Override
				public boolean isChildrenVerificationRequired(Row expectedChild) {
					return !(allowUncapturedChildren && expectedChild.getChildren().isEmpty());
				}

				@Override
				public String getName(Row payload) {
					if (payload == null)
						return "";
					return getNonBlankItemText(expectedTree, payload);
				}
			};

			List<TreeItemVerificationError> treeResults = comparison.assertNode(new ModelTreeNodeAdapter(expectedTree),
					new WidgetTreeNodeAdapter(widget));
			for (TreeItemVerificationError treeItemVerificationError : treeResults) {
				errors.add(treeItemVerificationError);
			}

		}
	}

	private static String getNonBlankItemText(Tree tree, Row item) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < item.getValues().size(); i++) {
			if (!item.getValues().get(i).getData().getText().equals("")) {
				sb.append(item.getValues().get(i).getData().getText());
				if (i != 0) {
					sb.append(String.format("(column \"%s\")", tree.getColumns().get(i).getName()));
				}
				break;
			}
		}
		return sb.toString();
	}

	private boolean isEqualStrings(String left, String right) {
		return !((left == null && right != null) || (left != null && !left.equals(right)));
	}

	private boolean compareRows(List<TreeItemVerificationError> errors, Row expectedRow, Row actualRow, Display display,
			VerifyStyleType verifyStyle, boolean verifyIcons, List<Column> columns, int[] mappedInds) {
		boolean equal = true;
		if (expectedRow == actualRow)
			return true;
		if (expectedRow == null || actualRow == null) {
			TreeItemVerificationError error = StatusFactory.eINSTANCE.createTreeItemVerificationError();
			error.setMessage("Row undefined");
			errors.add(error);
		}

		if (expectedRow.isChecked() != actualRow.isChecked()) {
			equal = false;
			TreeItemVerificationError error = StatusFactory.eINSTANCE.createTreeItemVerificationError();
			error.setMessage(String.format("Expected to be %s, but actual row is %s",
					expectedRow.isChecked() ? "checked" : "unchecked",
					actualRow.isChecked() ? "checked" : "unchecked"));
			errors.add(error);
		}
		for (int j = 0; j < mappedInds.length; j++) {
			if (mappedInds[j] != -1) {
				Cell expVal = expectedRow.getValues().get(j);
				Cell actVal = actualRow.getValues().get(mappedInds[j]);
				if (verifyIcons) {
					String expImg = TreeVerificationUtils.getDecoratedImagePath(expVal.getData().getImage());
					String actImg = TreeVerificationUtils.getDecoratedImagePath(actVal.getData().getImage());
					if (!isEqualStrings(expImg, actImg)) {
						equal = false;
						TreeItemVerificationError error = StatusFactory.eINSTANCE.createTreeItemVerificationError();
						error.setMessage(String.format("Expected %s, but was %s",
								(expImg == null) ? "no icon" : String.format("icon \"%s\"", expImg),
								(actImg == null) ? "no icon" : String.format("\"%s\"", actImg)));
						if (columns.size() > j) {
							error.setColumn(columns.get(j).getName());
						}
						errors.add(error);
					}
				}

				Iterable<StyleRangeEntry> expectedStyles = expVal.getStyle();
				Iterable<StyleRangeEntry> actualStyles = actVal.getStyle();

				String expText = WidgetModels.getTextWithoutStyle(expectedStyles, expVal.getData().getText());
				String actText = WidgetModels.getTextWithoutStyle(actualStyles, actVal.getData().getText());

				if (verifyStyle == VerifyStyleType.ALL) {
					String expStyledText = WidgetModels.getStyledText(expectedStyles, expVal.getData().getText());
					String actStyledText = WidgetModels.getStyledText(actualStyles, actVal.getData().getText());

					if (!expStyledText.equals(actStyledText)) {
						equal = false;
						TreeItemVerificationError error = StatusFactory.eINSTANCE.createTreeItemVerificationError();
						error.setMessage(String.format("Expected styled text \"%s\", but was \"%s\"", expStyledText,
								actStyledText));
						if (columns.size() > j) {
							error.setColumn(columns.get(j).getName());
						}
						errors.add(error);
					} else {
						StyleDiff diff = WidgetModels.getDiff(0, expText.length(), expectedStyles, actualStyles);

						if (diff != null) {
							equal = false;
							TreeItemStyleVerificationError error = StatusFactory.eINSTANCE
									.createTreeItemStyleVerificationError();
							error.setMessage(String.format("Different text style from position %d.", diff.at));
							if (columns.size() > j) {
								error.setColumn(columns.get(j).getName());
							}
							error.setExpectedStyle(diff.expected);
							error.setActualStyle(diff.actual);
							errors.add(error);
						}
					}
				}

				if (!expText.equals(actText) && equal) {
					equal = false;
					try {
						equal = (actText).matches(expText);
						if (!equal) {
							TreeItemVerificationError error = StatusFactory.eINSTANCE.createTreeItemVerificationError();
							error.setMessage(String.format("Expected \"%s\", but was \"%s\"",
									expVal.getData().getText(), actVal.getData().getText()));
							if (columns.size() > j) {
								error.setColumn(columns.get(j).getName());
							}
							errors.add(error);
						}
					} catch (Throwable e) {
						equal = false;
						TreeItemVerificationError error = StatusFactory.eINSTANCE.createTreeItemVerificationError();
						error.setMessage(String.format("Assert failed with \"%s\" regexp expression, but was \"%s\"",
								expText, actText));
						if (columns.size() > j) {
							error.setColumn(columns.get(j).getName());
						}
						errors.add(error);
					}
				}

			}
		}
		return equal;
	}

	@Override
	public Verification create(EObject param, IProcess process) throws CoreException {
		final CreateWidgetVerificationParam p = (CreateWidgetVerificationParam) param;
		final SWTUIElement swtuiElement = TeslaBridge.resolveSWTUIElement(p.getSelector(), process);
		final Widget widget = swtuiElement.widget;
		final CaptureTreeVerificationData result = TreeFactory.eINSTANCE.createCaptureTreeVerificationData();

		widget.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				result.getImages().clear();
				result.getImages().putAll(TreeVerificationModeller.extractImages(widget));
				result.setSelector(p.getSelector());
				result.setTree(TreeVerificationModeller.getTreeData(widget, true));
			}
		});

		return result;
	}
}
