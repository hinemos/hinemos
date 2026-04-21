/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.viewer;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.bean.CheckBoxImageConstant;
import com.clustercontrol.jobmanagement.composite.ClearableDateTimeDialogCellEditor;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/*
 * ツリービューア向けに以下の拡張をしたユーティリティークラス
 * ・汎用性の高い標準表示モデルをビルドイン
 *   ・ツリー構造に対応
 *   ・セル毎にエディターを指定可能
 */
public abstract class AbstractHierarchicalElementViewer extends TreeViewer {
		
	private static class ElementLabelProvider extends ColumnLabelProvider {
		
		private final String property;
		
		public ElementLabelProvider(String property) {
			this.property = property;
		}
		
		@Override
		public Image getImage(Object element) {
			if (element instanceof  AbstractHierarchicalElementViewer.Element) {
				return ((AbstractHierarchicalElementViewer.Element)element).getImage(property);
			}
			return null;
		}
		@Override
		public String getText(Object element) {
			if (element instanceof  AbstractHierarchicalElementViewer.Element) {
				return ((AbstractHierarchicalElementViewer.Element)element).getLabel(property);
			}
			return null;
		}
	}
	
	private static class ElementContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Object[]) {
				return (Object[]) inputElement;
			}
			return new Object[0];
		}
		@Override
		public void dispose() {
		}
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof AbstractHierarchicalElementViewer.Element) {
				return ((AbstractHierarchicalElementViewer.Element) parentElement).getChildren();
			}
			return new Object[0];
		}
		@Override
		public Object getParent(Object element) {
			if (element instanceof AbstractHierarchicalElementViewer.Element) {
				return ((AbstractHierarchicalElementViewer.Element) element).getParent();
			}
			return null;
		}
		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof AbstractHierarchicalElementViewer.Element) {
				return ((AbstractHierarchicalElementViewer.Element) element).getChildren().length > 0;
			}
			return false;
		}
	}
	
	private static class FilterViewerClomunEditingSupport extends EditingSupport {
		
		private final Tree tree;
		private final String property;
		
		public FilterViewerClomunEditingSupport(TreeViewer viewer, String property) {
			super(viewer);
			this.property = property;
			this.tree = viewer.getTree();
		}
		@Override
		public boolean canEdit(Object element) {
			if (element instanceof AbstractHierarchicalElementViewer.Element) {
				return ((AbstractHierarchicalElementViewer.Element)element).canEdit(property);
			}
			return false;
		}
		@Override
		public CellEditor getCellEditor(Object element) {
			if (element instanceof AbstractHierarchicalElementViewer.Element) {
				CellEditor editor = ((AbstractHierarchicalElementViewer.Element)element).getCellEditor(property);
				editor.create(tree);
				return editor;
			}
			return null;
		}
		@Override
		public Object getValue(Object element) {
			if (element instanceof AbstractHierarchicalElementViewer.Element) {
				return ((AbstractHierarchicalElementViewer.Element)element).getValue(property);
			}
			return null;
		}
		@Override
		public void setValue(Object element, Object value) {
			if (element instanceof AbstractHierarchicalElementViewer.Element) {
				if (((AbstractHierarchicalElementViewer.Element)element).updateValue(value, property)) {
					getViewer().update(element, new String[]{property});
//						getViewer().refresh();
				}
			}
		}
	};
	
	public static class PropertyBinding {
		
		public final String property;
		
		public final Supplier<String> labelGet;
		public final Supplier<Image> imageGet;
		public final Supplier<Object> valueGet;
		public final Consumer<Object> valueSet;
		public final Supplier<CellEditor> editorGet;
		
		public PropertyBinding(String property, Supplier<String> labelGet, Supplier<Image> imageGet, Supplier<Object> valueGet, Consumer<Object> valueSet, Supplier<CellEditor> editorGet) {
			Objects.requireNonNull(property);
			this.property = property;
			
			this.labelGet = Optional.ofNullable(labelGet).orElse(()->null);
			this.imageGet = Optional.ofNullable(imageGet).orElse(()->null);
			this.valueGet = Optional.ofNullable(valueGet).orElse(()->null);
			this.valueSet = Optional.ofNullable(valueSet).orElse(v->{});
			this.editorGet = Optional.ofNullable(editorGet).orElse(()->null);
		}
		
		public static PropertyBinding build(String property, Supplier<String> labelGet) {
			labelGet = Objects.requireNonNull(labelGet);
			return new PropertyBinding(property, labelGet, null, null, null, null);
		}
		
		public static PropertyBinding build(String property, Supplier<String> labelGet, Supplier<Image> imageGet) {
			labelGet = Objects.requireNonNull(labelGet);
			imageGet = Objects.requireNonNull(imageGet);
			return new PropertyBinding(property, labelGet, imageGet, null, null, null);
		}
		
		public static PropertyBinding build(String property, Supplier<String> labelGet, Supplier<Image> imageGet, Supplier<Object> valueGet, Consumer<Object> valueSet, Supplier<CellEditor> editorGet) {
			return new PropertyBinding(property, labelGet, imageGet, valueGet, valueSet, editorGet);
		}
		
		public static PropertyBinding buildText(String property, Supplier<String> valueGet, Consumer<String> valueSet) {
			Supplier<String> vg = Objects.requireNonNull(valueGet);
			Consumer<String> vs = Objects.requireNonNull(valueSet);
			return PropertyBinding.build(property,
					()->{
						String text = vg.get();
						if (text != null && !text.isEmpty()) {
							return text;
						}
						return Messages.getString("dialog.column.input");
						},
					null,
					()->Optional.ofNullable(vg.get()).orElse(""),
					v->{if (v instanceof String) {vs.accept((String)v);}},
					()->new TextCellEditor()
				);
		}
		
		public static PropertyBinding buildDateTime(String property, Supplier<Date> valueGet, Consumer<Date> valueSet) {
			Supplier<Date> vg = Objects.requireNonNull(valueGet);
			Consumer<Date> vs = Objects.requireNonNull(valueSet);
			return PropertyBinding.build(property,
				()->Optional.ofNullable(vg.get()).map(t->TimezoneUtil.getSimpleDateFormat().format(t)).orElse(Messages.getString("dialog.column.input")),
				null,
				()->vg.get(),
				v->vs.accept((Date)v),
				()->new ClearableDateTimeDialogCellEditor()
				);
		}
		
		public static PropertyBinding buildCheckBox(String property, Supplier<Boolean> valueGet, Consumer<Boolean> valueSet) {
			Supplier<Boolean> vg = Objects.requireNonNull(valueGet);
			Consumer<Boolean> vs = Objects.requireNonNull(valueSet);
			return PropertyBinding.build(property,
				null,
				()->CheckBoxImageConstant.typeToImage(Optional.ofNullable(vg.get()).orElse(Boolean.FALSE)),
				()->vg.get(),
				v->{if (v instanceof Boolean) {vs.accept((Boolean)v);}},
				()->new CheckboxCellEditor()
				);
		}
	}
	
	public static class Element {
		private final Map<String, Optional<PropertyBinding>> propertyGetMap = new LinkedHashMap<>();
		
		private Element parent;
		private final List<Element> children = new LinkedList<>();
		
		public Element() {
		}
		
		public String getLabel(String property) {
			return propertyGetMap.getOrDefault(property, Optional.empty()).map(p->p.labelGet.get()).orElse("");
		}
		
		public Image getImage(String property) {
			return propertyGetMap.getOrDefault(property, Optional.empty()).map(p->p.imageGet.get()).orElse(null);
		}
		
		public Object getValue(String property) {
			return propertyGetMap.getOrDefault(property, Optional.empty()).map(p->p.valueGet.get()).orElse(null);
		}
		
		public boolean updateValue(Object value, String property) {
			propertyGetMap.getOrDefault(property, Optional.empty()).ifPresent(p->p.valueSet.accept(value));
			return Objects.equals(value, getValue(property));
		}
		
		public CellEditor getCellEditor(String property) {
			return propertyGetMap.getOrDefault(property, Optional.empty()).map(p->p.editorGet.get()).orElse(null);
		}
		
		public boolean canEdit(String property) {
			return getCellEditor(property) != null;
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public Element getParent() {
			return parent;
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Internal UI model.")
		public void setParent(Element parent) {
			this.parent = parent;
		}
		
		public Element[] getChildren() {
			return children.toArray(new Element[0]);
		}
		
		public Element addChild(Element...elements) {
			Arrays.asList(elements).stream().forEach(e -> {
				e.setParent(this);
				children.add(e);
			});
			return this;
		}
		
		public Element addChild(List<Element> elements) {
			elements.stream().forEach(e -> {
				e.setParent(this);
				children.add(e);
			});
			return this;
		}
		
		public Element addProperty(PropertyBinding...bindings) {
			Arrays.stream(bindings).forEach(b->propertyGetMap.put(b.property, Optional.of(b)));
			return this;
		}
		
		public Element addProperty(List<PropertyBinding> bindings) {
			bindings.stream().forEach(b->propertyGetMap.put(b.property, Optional.of(b)));
			return this;
		}
		
		public static Element build() {
			return new Element();
		}
	}
	
	public AbstractHierarchicalElementViewer(Composite parent) {
		super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		
		Tree tree = getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		setContentProvider(new ElementContentProvider());
	}

	protected final void initializeColumns(LinkedHashMap<String, TreeViewerColumn> columns) {
		Objects.requireNonNull(columns);

		columns.forEach((k, v)->{
			v.setEditingSupport(new FilterViewerClomunEditingSupport(this, k));
			v.setLabelProvider(new ElementLabelProvider(k));
			});
		
		setColumnProperties(columns.keySet().toArray(new String[0]));
	}
}
