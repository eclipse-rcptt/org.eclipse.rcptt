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
package org.eclipse.rcptt.tesla.ecl.internal.impl.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.dispatch.ServiceDispatchingUtils;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.rcptt.tesla.core.protocol.ElementKind;
import org.eclipse.rcptt.tesla.ecl.TeslaScriptletFactory;
import org.eclipse.rcptt.tesla.ecl.impl.TeslaBridge;
import org.eclipse.rcptt.tesla.ecl.model.ControlHandler;
import org.eclipse.rcptt.tesla.ecl.model.GetAboutMenu;
import org.eclipse.rcptt.tesla.ecl.model.GetBanner;
import org.eclipse.rcptt.tesla.ecl.model.GetButton;
import org.eclipse.rcptt.tesla.ecl.model.GetCanvas;
import org.eclipse.rcptt.tesla.ecl.model.GetCell;
import org.eclipse.rcptt.tesla.ecl.model.GetCheckbox;
import org.eclipse.rcptt.tesla.ecl.model.GetColumnHeader;
import org.eclipse.rcptt.tesla.ecl.model.GetCombo;
import org.eclipse.rcptt.tesla.ecl.model.GetControl;
import org.eclipse.rcptt.tesla.ecl.model.GetCoolbar;
import org.eclipse.rcptt.tesla.ecl.model.GetDateTime;
import org.eclipse.rcptt.tesla.ecl.model.GetEclipseWindow;
import org.eclipse.rcptt.tesla.ecl.model.GetEditbox;
import org.eclipse.rcptt.tesla.ecl.model.GetEditor;
import org.eclipse.rcptt.tesla.ecl.model.GetGroup;
import org.eclipse.rcptt.tesla.ecl.model.GetItem;
import org.eclipse.rcptt.tesla.ecl.model.GetLabel;
import org.eclipse.rcptt.tesla.ecl.model.GetLeftRuler;
import org.eclipse.rcptt.tesla.ecl.model.GetLink;
import org.eclipse.rcptt.tesla.ecl.model.GetList;
import org.eclipse.rcptt.tesla.ecl.model.GetMenu;
import org.eclipse.rcptt.tesla.ecl.model.GetPreferencesMenu;
import org.eclipse.rcptt.tesla.ecl.model.GetPropertyTab;
import org.eclipse.rcptt.tesla.ecl.model.GetQuickAccess;
import org.eclipse.rcptt.tesla.ecl.model.GetRegion;
import org.eclipse.rcptt.tesla.ecl.model.GetRightRuler;
import org.eclipse.rcptt.tesla.ecl.model.GetRulerColumn;
import org.eclipse.rcptt.tesla.ecl.model.GetSection;
import org.eclipse.rcptt.tesla.ecl.model.GetSlider;
import org.eclipse.rcptt.tesla.ecl.model.GetTabFolder;
import org.eclipse.rcptt.tesla.ecl.model.GetTabItem;
import org.eclipse.rcptt.tesla.ecl.model.GetTable;
import org.eclipse.rcptt.tesla.ecl.model.GetTextViewer;
import org.eclipse.rcptt.tesla.ecl.model.GetToolbar;
import org.eclipse.rcptt.tesla.ecl.model.GetTree;
import org.eclipse.rcptt.tesla.ecl.model.GetVerticalRuler;
import org.eclipse.rcptt.tesla.ecl.model.GetView;
import org.eclipse.rcptt.tesla.ecl.model.GetWindow;
import org.eclipse.rcptt.tesla.ecl.model.PathSelector;
import org.eclipse.rcptt.tesla.ecl.model.Selector;
import org.eclipse.rcptt.tesla.ecl.model.TeslaFactory;
import org.eclipse.rcptt.tesla.ecl.model.TextSelector;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetConnectionHandle;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetDiagram;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetEditPart;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetEntry;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetFigure;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetHandle;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPalette;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPaletteEntry;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetPopupBarItem;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetSourceConnection;
import org.eclipse.rcptt.tesla.ecl.model.diagram.GetTargetConnection;
import org.eclipse.rcptt.tesla.swt.util.GetWindowUtil;
import org.eclipse.rcptt.util.TableTreeItemPathUtil;

public class SelectorService implements ICommandService {

	@Override
	public IStatus service(Command command, IProcess context)
			throws InterruptedException, CoreException {
		//
		IStatus result = ServiceDispatchingUtils.handleWithExtensions(command, context);
		if (result != null)
			return result;
		//

		TeslaBridge.waitDelay();

		ControlHandler handler = TeslaFactory.eINSTANCE.createControlHandler();

		if (command instanceof GetEclipseWindow) {
			handler.setKind(ElementKind.EclipseWindow);
			handler.setIndex(((GetEclipseWindow) command).getIndex());
			context.getOutput().write(handler);
			return Status.OK_STATUS;
		} else if (command instanceof GetAboutMenu) {
			handler.setKind(ElementKind.AboutMenu);
			context.getOutput().write(handler);
			return Status.OK_STATUS;
		} else if (command instanceof GetPreferencesMenu) {
			handler.setKind(ElementKind.PreferencesMenu);
			context.getOutput().write(handler);
			return Status.OK_STATUS;
		} else if (command instanceof GetQuickAccess) {
			handler.setKind(ElementKind.QuickAccess);
			context.getOutput().write(handler);
			return Status.OK_STATUS;
		}

		Selector selector = (Selector) command;
		ControlHandler parent = selector.getParent();

		if (selector instanceof GetItem
				&& ((GetItem) selector).getPath() != null
				&& ElementKind.Item.equals(parent.getKind())
				&& parent.getPath() != null) {
			String path = parent.getPath();
			path += "/" + ((GetItem) selector).getPath();
			Integer index = ((GetItem) selector).getIndex();
			String column = ((GetItem) selector).getColumn();
			if (index != null) {
				path += "%" + index + "%";
			}
			if (column != null) {
				path += TableTreeItemPathUtil.COLUMN_DELIMITER
						+ column
						+ TableTreeItemPathUtil.COLUMN_DELIMITER;
			}
			handler.setKind(ElementKind.Item);
			handler.setParent(parent.getParent());
			handler.setPath(path);
			TeslaBridge.find(handler, context);
			context.getOutput().write(handler);
			return Status.OK_STATUS;
		}

		handler.setParent(selector.getParent());
		handler.setAfter(selector.getAfter());
		if (selector instanceof GetItem) {
			GetItem ge = (GetItem) selector;
			if (ge.getPath() == null)
				handler.setColumn((selector.getIndex() == null) ? 0 : selector.getIndex());
			else {
				if (selector.getIndex() != null) {
					ge.setPath(ge.getPath() + "%" + selector.getIndex() + "%");
				}
			}
			String column = ge.getColumn();
			if (column != null) {
				ge.setPath(TableTreeItemPathUtil.appendFullPathColumnName(ge.getPath(), column));
			}
		} else {
			if (selector.getIndex() != null) {
				handler.setIndex(selector.getIndex());
			}
		}

		if (selector instanceof TextSelector)
			handler.setText(((TextSelector) selector).getText());
		if (selector instanceof PathSelector) {
			handler.setPath(((PathSelector) selector).getPath());
		}

		if (selector instanceof GetControl) {
			String kindId = ((GetControl) selector).getKind();
			ElementKind kind = ElementKind.kindOf(kindId);
			handler.setKind(kind);
			if (kind == ElementKind.Custom) {
				handler.setCustomKindId(kindId);
			}
		} else if (selector instanceof GetButton)
			handler.setKind(ElementKind.Button);
		else if (selector instanceof GetCheckbox)
			handler.setKind(ElementKind.Button);
		else if (selector instanceof GetCombo)
			handler.setKind(ElementKind.Combo);
		else if (selector instanceof GetEditbox)
			handler.setKind(ElementKind.Text);
		else if (selector instanceof GetGroup)
			handler.setKind(ElementKind.Group);
		else if (selector instanceof GetItem) {
			handler.setKind(ElementKind.Item);
			handler.setPath(((GetItem) selector).getPath());
		} else if (selector instanceof GetDateTime)
			handler.setKind(ElementKind.DateTime);
		else if (selector instanceof GetSlider)
			handler.setKind(ElementKind.Slider);
		else if (selector instanceof GetLabel)
			handler.setKind(ElementKind.Label);
		else if (selector instanceof GetLink)
			handler.setKind(ElementKind.Link);
		else if (selector instanceof GetMenu)
			handler.setKind(ElementKind.Menu);
		else if (selector instanceof GetTextViewer)
			handler.setKind(ElementKind.TextViewer);
		else if (selector instanceof GetVerticalRuler)
			handler.setKind(ElementKind.VerticalRuler);
		else if (selector instanceof GetRulerColumn) {
			handler.setKind(ElementKind.VerticalColumn);
			handler.setText(((GetRulerColumn) selector).getText());
		} else if (selector instanceof GetRightRuler)
			handler.setKind(ElementKind.RightRuler);
		else if (selector instanceof GetLeftRuler)
			handler.setKind(ElementKind.LeftRuler);
		else if (selector instanceof GetTabFolder)
			handler.setKind(ElementKind.TabFolder);
		else if (selector instanceof GetTable)
			handler.setKind(ElementKind.Table);
		else if (selector instanceof GetToolbar)
			handler.setKind(ElementKind.Toolbar);
		else if (selector instanceof GetTree)
			handler.setKind(ElementKind.Tree);
		else if (selector instanceof GetView)
			handler.setKind(ElementKind.View);
		else if (selector instanceof GetEditor) {
			handler.setType(((GetEditor) command).getType());
			handler.setKind(ElementKind.Editor);
		} else if (selector instanceof GetTabItem)
			handler.setKind(ElementKind.TabItem);
		else if (selector instanceof GetWindow) {
			handler.setKind(ElementKind.Window);
			GetWindow getWindow = (GetWindow) selector;
			handler.setParent(null);

			String text = getWindow.getText();
			if (text != null) {
				handler.setText(((GetWindow) selector).getText());
			}

			String from = getWindow.getFrom();
			if (from != null) {
				from = TeslaScriptletFactory.getWindowFromByTitle(from);
				handler.setPath(GetWindowUtil.encodeFrom(from));
				handler.setText(null);
			}

			String klass = getWindow.getClass_();
			if (klass != null) {
				handler.setPath(GetWindowUtil.encodeClass(klass));
				handler.setText(null);
			}

		} else if (selector instanceof GetCoolbar)
			handler.setKind(ElementKind.CoolBar);
		else if (selector instanceof GetBanner)
			handler.setKind(ElementKind.CBanner);
		else if (selector instanceof GetCanvas)
			handler.setKind(ElementKind.Canvas);
		else if (selector instanceof GetList)
			handler.setKind(ElementKind.List);
		else if (selector instanceof GetSection)
			handler.setKind(ElementKind.Expandable);
		else if (selector instanceof GetDiagram)
			handler.setKind(ElementKind.DiagramViewer);
		else if (selector instanceof GetPalette)
			handler.setKind(ElementKind.PaletteViewer);
		else if (selector instanceof GetFigure) {
			handler.setKind(ElementKind.DiagramFigure);
			handler.setIndexes(handler.getPath());
			handler.setPath("rawFigure/address");
		} else if (selector instanceof GetPopupBarItem) {
			handler.setKind(ElementKind.DiagramFigure);
			handler.setIndexes("-3/" + handler.getPath());
			handler.setPath("rawFigure/address");
		} else if (selector instanceof GetConnectionHandle) {
			handler.setKind(ElementKind.DiagramFigure);
			handler.setIndexes("-2/" + handler.getPath());
			handler.setPath("rawFigure/address");
		} else if (selector instanceof GetHandle) {
			handler.setKind(ElementKind.DiagramFigure);
			handler.setPath("handle/class");
		} else if (selector instanceof GetEditPart) {
			handler.setKind(ElementKind.DiagramFigure);
			GetEditPart partSelector = (GetEditPart) selector;
			handler.setIndexes(partSelector.getPath());
			if (partSelector.getName() != null) {
				handler.setPath("editpart/name");
				handler.setText(partSelector.getName());
			} else if (partSelector.getText() != null) {
				handler.setPath("editpart/text");
				handler.setText(partSelector.getText());
			} else if (partSelector.getCustomId() != null) {
				handler.setPath("editpart/customId");
				handler.setText(partSelector.getCustomId());
			} else if (partSelector.getClassName() != null) {
				handler.setPath("editpart/classname");
				handler.setText(partSelector.getClassName());
			} else if (partSelector.getFeature() != null) {
				String[] split = partSelector.getFeature().split(":");
				handler.setPath("editpart.feature/" + split[0]);
				handler.setText(split[1]);
			} else {
				handler.setPath("editpart/address");
			}
		} else if (selector instanceof GetSourceConnection) {
			handler.setKind(ElementKind.DiagramFigure);
			handler.setIndexes(handler.getPath());
			handler.setPath("editpart.source.connection/address");
		} else if (selector instanceof GetTargetConnection) {
			handler.setKind(ElementKind.DiagramFigure);
			handler.setIndexes(handler.getPath());
			handler.setPath("editpart.target.connection/address");
		} else if (selector instanceof GetEntry)
			handler.setKind(ElementKind.PaletteEntry);
		else if (selector instanceof GetCell) {
			handler.setKind(ElementKind.Item);
			GetCell gc = (GetCell) selector;
			handler.setRow(gc.getRow());
			handler.setColumn(gc.getColumn());
		} else if (selector instanceof GetPaletteEntry) {
			handler.setKind(ElementKind.PaletteEntry);
		} else if (selector instanceof GetRegion) {
			handler.setKind(ElementKind.Region);
			handler.setRawImage(((GetRegion) selector).getRawImage());
			handler.setX(((GetRegion) selector).getX());
			handler.setY(((GetRegion) selector).getY());
		} else if (selector instanceof GetColumnHeader) {
			handler.setKind(ElementKind.ColumnHeader);
		} else if (selector instanceof GetPropertyTab) {
			handler.setKind(ElementKind.PropertyTab);
		}
		
		TeslaBridge.find(handler, context);

		context.getOutput().write(handler);
		TeslaBridge.waitExecution();
		return Status.OK_STATUS;
	}
}
