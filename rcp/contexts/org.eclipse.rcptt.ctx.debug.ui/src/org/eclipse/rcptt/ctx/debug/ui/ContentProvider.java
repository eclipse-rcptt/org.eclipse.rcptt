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
package org.eclipse.rcptt.ctx.debug.ui;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rcptt.ecl.core.EclBoolean;
import org.eclipse.rcptt.ecl.core.EclInteger;
import org.eclipse.rcptt.ecl.core.EclString;
import org.eclipse.rcptt.ecl.runtime.BoxedValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;

import org.eclipse.rcptt.core.scenario.NamedElement;
import org.eclipse.rcptt.debug.Breakpoint;
import org.eclipse.rcptt.debug.BreakpointResource;
import org.eclipse.rcptt.debug.Collection;
import org.eclipse.rcptt.debug.DebugContext;
import org.eclipse.rcptt.debug.DebugContextAdapter;
import org.eclipse.rcptt.debug.DebugPackage;
import org.eclipse.rcptt.debug.Launch;
import org.eclipse.rcptt.debug.LaunchConfiguration;
import org.eclipse.rcptt.debug.LaunchType;
import org.eclipse.rcptt.debug.ListValue;
import org.eclipse.rcptt.debug.MapValue;
import org.eclipse.rcptt.debug.PrimitiveValue;
import org.eclipse.rcptt.debug.SetValue;
import org.eclipse.rcptt.internal.ui.Images;

public class ContentProvider implements ITreeContentProvider {
	private DebugContext content;
	private static final Object[] none = new Object[0];
	protected final ImageRegistry imageRegistry = new ImageRegistry();

	protected Image getCachedImage(String id, byte[] bytes) {
		Image image = imageRegistry.get(id);
		if (image == null) {
			// TODO: check for more highlevel solution
			if (bytes == null || bytes.length == 0)
				return null;
			ImageData imageData = new ImageData(new ByteArrayInputStream(bytes));
			image = new Image(Display.getDefault(), imageData);
			imageRegistry.put(id, image);
		}
		return image;
	}

	@Override
	public void dispose() {
		imageRegistry.dispose();
	}

	class ValueEditing extends EditingSupport {

		public ValueEditing(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			if (element instanceof PrimitiveValue) {
				Object value = ((PrimitiveValue) element).getValue();
				if (value instanceof EclString)
					return new TextCellEditor((Composite) getViewer().getControl());
				if (value instanceof EclInteger) {
					TextCellEditor rv = new TextCellEditor((Composite) getViewer().getControl());
					rv.setValidator(new ICellEditorValidator() {
						@Override
						public String isValid(Object value) {
							try {
								if (value instanceof Integer)
									return null;
								Integer.parseInt((String) value);
								return null;
							} catch (NumberFormatException e) {
								return e.getMessage();
							}
						}
					});
					return rv;
				}
				if (value instanceof EclBoolean) {
					return new CheckboxCellEditor(((Composite) getViewer().getControl()));
				}
			}
			if (element instanceof AttributeOfEObject) {
				return ((AttributeOfEObject) element).getCellEditor(((Composite) getViewer().getControl()));
			}
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			return element instanceof PrimitiveValue || element instanceof AttributeOfEObject;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof PrimitiveValue) {
				Object value = ((PrimitiveValue) element).getValue();
				if (value instanceof EclInteger)
					return Integer.toString(((EclInteger) value).getValue());
				return BoxedValues.unbox(value);
			}
			if (element instanceof AttributeOfEObject)
				return ((AttributeOfEObject) element).getValue();
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof PrimitiveValue) {
				EObject boxed = ((PrimitiveValue) element).getValue();
				if (boxed instanceof EclInteger) {
					((EclInteger) boxed).setValue(Integer.parseInt((String) value));
				} else if (boxed instanceof EclString) {
					((EclString) boxed).setValue((String) value);
				} else if (boxed instanceof EclBoolean) {
					((EclBoolean) boxed).setValue((Boolean) value);
				} else {
					assert false : "Unknown primitive value type";
				}

			} else if (element instanceof AttributeOfEObject) {
				((AttributeOfEObject) element).setValue(value);
			}
			getViewer().refresh(element);
		}

	}

	abstract class FeatureOfEObject<T extends EStructuralFeature> {
		public FeatureOfEObject(EObject object, T attribute) {
			super();
			this.object = object;
			this.attribute = attribute;
		}

		final EObject object;
		final T attribute;

		@Override
		public int hashCode() {
			return object.hashCode() ^ attribute.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FeatureOfEObject<?>))
				return false;
			FeatureOfEObject<?> that = (FeatureOfEObject<?>) obj;
			return object.equals(that.object) && attribute.equals(that.attribute);
		}

		public String getName() {
			return attribute.getName();
		}

		public abstract String getValueLabel();

		public abstract Object[] getChildren();

		public abstract Image getImage();
	}

	class AttributeOfEObject extends FeatureOfEObject<EAttribute> {
		public AttributeOfEObject(EObject object, EAttribute attribute) {
			super(object, attribute);
		}

		@Override
		public String getValueLabel() {
			return object.eGet(attribute).toString();
		}

		public Object getValue() {
			Object value = object.eGet(attribute);
			Class<?> c = attribute.getEAttributeType().getInstanceClass();
			if (c == Integer.class) {
				return Integer.toString((Integer) value);
			}
			return value;

		}

		public void setValue(Object value) {
			EDataType c = attribute.getEAttributeType();
			if (c == EcorePackage.Literals.EINT) {
				value = Integer.parseInt((String) value);
			}
			object.eSet(attribute, value);
		}

		public CellEditor getCellEditor(Composite composite) {
			EDataType c = attribute.getEAttributeType();
			if (c == EcorePackage.Literals.EBOOLEAN)
				return new CheckboxCellEditor(composite);
			if (c == EcorePackage.Literals.ESTRING)
				return new TextCellEditor(composite, SWT.MULTI);
			return new TextCellEditor(composite);
		}

		@Override
		public Object[] getChildren() {
			return none;
		}

		@Override
		public Image getImage() {
			if (object.eGet(attribute) instanceof String)
				return Images.getImage(Images.PREFERENCES_DATA);
			return Images.getImage(Images.ECL_ATTRIBUTE);
		}

	}

	class ReferenceOfEObject extends FeatureOfEObject<EReference> {
		public ReferenceOfEObject(EObject object, EReference attribute) {
			super(object, attribute);
		}

		@Override
		public String getValueLabel() {
			return null;
		}

		@Override
		public Object[] getChildren() {
			if (attribute.getUpperBound() == -1)
				return ((EList<?>) object.eGet(attribute)).toArray();
			return new Object[] { object.eGet(attribute) };
		}

		@Override
		public Image getImage() {
			return Images.getImage(Images.PREFERENCES_NODE);
		}

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		content = (DebugContext) newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		DebugContext context = (DebugContext) inputElement;
		List<EList<?>> rootNodes = new ArrayList<EList<?>>();
		DebugContextAdapter adapter = new DebugContextAdapter(context);
		if (adapter.getNonEmptyLaunchTypes().size() > 0) {
			rootNodes.add(context.getLaunchTypes());
		}
		if (context.getLaunches().size() > 0)
			rootNodes.add(context.getLaunches());
		if (adapter.getNonEmptyBreakpointResources().size() > 0) {
			rootNodes.add(context.getBreakpointResources());
		}

		return rootNodes.toArray();
	}

	public Object[] getChildrenFromCollection(EObject value) {
		if (value instanceof Collection)
			return ((Collection) value).getEntries().toArray();
		return none;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof EList) {
			List<Object> rv = new ArrayList<Object>();
			for (Object child : (EList<?>) parentElement) {
				if (getChildren(child).length > 0)
					rv.add(child);
			}
			return rv.toArray();
		}
		if (parentElement instanceof Collection) {
			Collection attribute = ((Collection) parentElement);
			return attribute.getEntries().toArray();
		}
		if (parentElement instanceof FeatureOfEObject) {
			return ((FeatureOfEObject<?>) parentElement).getChildren();
		}
		if (parentElement instanceof EObject) {
			EObject eObject = ((EObject) parentElement);
			List<Object> rv = new ArrayList<Object>();
			if (eObject instanceof Breakpoint) {
				rv.add(new AttributeOfEObject(eObject, DebugPackage.Literals.BREAKPOINT__ENABLED));
				rv.add(new AttributeOfEObject(eObject, DebugPackage.Literals.BREAKPOINT__PERSISTED));
				rv.add(new AttributeOfEObject(eObject, DebugPackage.Literals.BREAKPOINT__REGISTERED));
				rv.add(new AttributeOfEObject(eObject, DebugPackage.Literals.BREAKPOINT__TYPE));
			}
			if (eObject instanceof Launch) {
				rv.add(new ReferenceOfEObject(eObject, DebugPackage.Literals.LAUNCH__CONFIGURATION));
				rv.add(new AttributeOfEObject(eObject, DebugPackage.Literals.LAUNCH__MODE));
				rv.add(new ReferenceOfEObject(eObject, DebugPackage.Literals.LAUNCH__CONFIGURATION_DELTA));
			}
			if (eObject instanceof LaunchType) {
				rv.addAll(((EList<?>) eObject.eGet(DebugPackage.Literals.LAUNCH_TYPE__CONFIGURATIONS)));
			}
			if (eObject instanceof BreakpointResource) {
				rv.addAll(((EList<?>) eObject.eGet(DebugPackage.Literals.BREAKPOINT_RESOURCE__BREAKPOINTS)));
			}

			for (EReference reference : eObject.eClass().getEAllContainments()) {
				if (reference.getName().equals("attributes"))
					rv.addAll(((EList<?>) eObject.eGet(reference)));
			}
			return rv.toArray();
		}
		return none;
	}

	@Override
	public Object getParent(Object element) {
		if (element == content.getBreakpointResources()
				|| element == content.getLaunches()
				|| element == content.getLaunchTypes())
			return content;
		if (element instanceof EObject)
			if (((EObject) element).eContainer() != null)
				return ((EObject) element).eContainer();
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof EList) {
			return ((EList<?>) element).size() > 0;
		}
		if (element instanceof PrimitiveValue) {
			return false;
		}
		if (element instanceof ReferenceOfEObject) {
			return true;
		}

		if (element instanceof EObject)
			return true;
		return false;
	}

	private final ColumnLabelProvider nameLabelProvider = new ColumnLabelProvider() {

		@Override
		public Image getImage(Object element) {
			if (element instanceof EList) {
				if (element == content.getBreakpointResources())
					return Images.getImage(Images.BREAKPOINT);
				if (element == content.getLaunches())
					return Images.getImage(Images.APP_RUN);
				if (element == content.getLaunchTypes())
					return Images.getImage(Images.PREFERENCES);
				return Images.getImage(Images.PREFERENCES_DATA);
			}
			if (element instanceof LaunchType)
				return getCachedImage(((LaunchType) element).getId(), ((LaunchType) element).getImage());
			if (element instanceof LaunchConfiguration)
				return getImage(((LaunchConfiguration) element).eContainer());
			if (element instanceof Launch)
				return getImage(((Launch) element).getConfiguration());
			if (element instanceof BreakpointResource)
				return Images.getImage(Images.FILE);
			if (element instanceof Breakpoint)
				return Images.getImage(Images.BREAKPOINT);
			if (element instanceof ListValue)
				return Images.getImage(Images.PREFERENCES_DATA);
			if (element instanceof MapValue)
				return Images.getImage(Images.PREFERENCES_NODE);
			if (element instanceof SetValue)
				return Images.getImage(Images.PREFERENCES_DATA);
			if (element instanceof PrimitiveValue) {
				EObject value = ((PrimitiveValue) element).getValue();
				if (value instanceof EclString)
					return Images.getImage(Images.PREFERENCES_DATA);
				if (value instanceof EclInteger)
					return Images.getImage(Images.ECL_ATTRIBUTE);
				if (value instanceof EclBoolean)
					return Images.getImage(Images.ECL_ATTRIBUTE);
			}
			if (element instanceof FeatureOfEObject<?>) {
				return ((FeatureOfEObject<?>) element).getImage();
			}
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof EList) {
				if (element == content.getBreakpointResources())
					return "Breakpoints";
				if (element == content.getLaunches())
					return "Launches";
				if (element == content.getLaunchTypes())
					return "Launch configurations";
			}
			if (element instanceof AttributeOfEObject)
				return ((AttributeOfEObject) element).getName();
			if (element instanceof BreakpointResource) {
				return ((BreakpointResource) element).getPath();
			}
			if (element instanceof Launch) {
				return ((Launch) element).getConfiguration().getName();
			}
			if (element instanceof FeatureOfEObject<?>) {
				return ((FeatureOfEObject<?>) element).getName();
			}
			if (element instanceof Breakpoint) {
				Breakpoint breakpoint = (Breakpoint) element;
				// Breakpoint only has primitive attributes
				PrimitiveValue messageAttr = (PrimitiveValue) DebugContextAdapter.findAttribute(
						breakpoint.getAttributes(), IMarker.MESSAGE);
				if (messageAttr == null)
					return null;
				return (String) BoxedValues.unbox(messageAttr.getValue());
			}
			if (element instanceof NamedElement) {
				return ((NamedElement) element).getName();
			}
			return super.getText(element);
		}
	};
	private final ColumnLabelProvider valueLabelProvider = new ColumnLabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof PrimitiveValue) {
				return BoxedValues.unbox(((PrimitiveValue) element).getValue()).toString();
			}
			if (element instanceof AttributeOfEObject) {
				Object value = ((AttributeOfEObject) element).getValueLabel();
				if (value != null)
					return value.toString();
				return null;
			}

			return null;
		}
	};

	public boolean isRemovable(Object object) {
		if (object instanceof Breakpoint || object instanceof LaunchType || object instanceof BreakpointResource
				|| object instanceof Launch)
			return true;
		if (object instanceof EObject) {
			EReference reference = ((EObject) object).eContainmentFeature();
			return reference.getUpperBound() == -1;
		}
		Object parent = getParent(object);
		if (parent instanceof DebugContext
				|| parent instanceof Collection)
			return true;
		return false;
	}

	public void remove(Object object) {
		if (object instanceof EObject) {
			EReference reference = ((EObject) object).eContainmentFeature();
			EObject parent = ((EObject) object).eContainer();
			if (reference == null || reference.getUpperBound() != -1)
				return;
			EList<?> list = (EList<?>) parent.eGet(reference);
			ArrayList<Launch> toRemove = new ArrayList<Launch>();
			if (object instanceof LaunchConfiguration) {
				for (Launch launch : content.getLaunches()) {
					if (launch.getConfiguration().getName().equals(((LaunchConfiguration) object).getName())) {
						toRemove.add(launch);
					}
				}
			}
			if (object instanceof LaunchType) {
				for (Launch launch : content.getLaunches()) {
					LaunchType type = (LaunchType) launch.getConfiguration().eContainer();
					if (type == object)
						toRemove.add(launch);
				}
			}
			content.getLaunches().removeAll(toRemove);
			list.remove(object);
		}
		if (object instanceof EList) {
			((EList<?>) object).clear();
		}
	}

	public void install(TreeViewer viewer) {
		viewer.setContentProvider(this);
		TreeColumn treeColumn = new TreeColumn(viewer.getTree(), SWT.LEFT);
		treeColumn.setText("Attribute");
		treeColumn.setWidth(350);
		TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, treeColumn);
		// viewerColumn.setEditingSupport(new NameEditing(viewer));
		viewerColumn.setLabelProvider(nameLabelProvider);

		treeColumn = new TreeColumn(viewer.getTree(), SWT.LEFT);
		treeColumn.setText("Value");
		treeColumn.setWidth(200);
		viewerColumn = new TreeViewerColumn(viewer, treeColumn);
		viewerColumn.setEditingSupport(new ValueEditing(viewer));
		viewerColumn.setLabelProvider(valueLabelProvider);
	}

}
