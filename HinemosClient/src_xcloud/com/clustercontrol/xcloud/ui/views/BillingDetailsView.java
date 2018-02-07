/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import static com.clustercontrol.xcloud.common.CloudConstants.bundle_messages;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.clustercontrol.ws.xcloud.BillingResult;
import com.clustercontrol.ws.xcloud.DataPoint;
import com.clustercontrol.ws.xcloud.FacilityBilling;
import com.clustercontrol.ws.xcloud.ResourceBilling;
import com.clustercontrol.ws.xcloud.TargetType;
import com.clustercontrol.xcloud.common.CloudStringConstants;


/**
 */
public class BillingDetailsView extends AbstractCloudViewPart implements CloudStringConstants {
	public static final String Id = BillingDetailsView.class.getName();
	
	private static Pattern pattern = Pattern.compile("^(\\d*\\.\\d*[1-9]|\\d*)0*$");

	public interface DataHolder {
		int getYear();
		int getMonth();
		BillingResult getData();
		DataHandler getDataHandler();
	}
	
	public interface DataProvider {
		DataHolder getData(int year, int month);
	}

	public static class State {
		public int year;
		public int month;
		public DataHolder dataHolder;
		public BillingResultWrapper result;
		public BillingDetailRenderer renderer;
	}
	
	
	public static final int Prop_billing_detail_target = 0;
	
	private DataProvider dataProvider;
	private Label lblHeader;
	private Composite base;
	private StackLayout layout;

	private Map<Object, State> rendererMap = new HashMap<>();
	private State currentState;
	private State nullState;
	
	private ISelectionProvider provider = new ISelectionProvider() {
		private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

		ISelection theSelection = StructuredSelection.EMPTY;

		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}

		public ISelection getSelection() {
			return theSelection;
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.remove(listener);
		}

		public void setSelection(ISelection selection) {
			theSelection = selection;
			SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
			for (ISelectionChangedListener l: listeners) {
				l.selectionChanged(e);
			}
		}
	};
	
	public BillingDetailsView() {
 		super();
	}
	
	@Override
	protected void internalCreatePartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout gl_composite = new GridLayout(1, true);
		gl_composite.horizontalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		
		lblHeader = new Label(composite, SWT.NONE);
		lblHeader.setSize(lblHeader.getSize().x, 80);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		lblHeader.setLayoutData(gridData);
		
		base = new Composite(composite, SWT.NONE);
		layout = new StackLayout();
		base.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		base.setLayoutData(gridData);
		
		nullState = new State();
		nullState.renderer = new BillingDetailRenderer(base);
		
		currentState = nullState;
		updateControls(nullState);
		
		getSite().setSelectionProvider(provider);
	}
	
	@Override
	public void setFocus() {
		base.setFocus();
	}

	public void update() {
		if (currentState == nullState) return;

		// TODO
		// あとで更新実装しないと。
		
//		if (currentRenderer != null) {
//			currentRenderer.dispose();
//			currentRenderer = null;
//		}
//
//		BillingResult result = null;
//		if (currentDataHolder != null) {
//			result = currentDataHolder.getData();
//		}
	}
	
	private static class BillingDetailRenderer {
		private enum ViewColumn{
			facility_id(
				bundle_messages.getString("word.facility_id"),
				new ColumnPixelData(260, true, true),
				new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						if(element instanceof FacilityBillingWrapper){
							FacilityBillingWrapper billing = (FacilityBillingWrapper)element;
							if ("others".equals(billing.getFacilityId())) {
								return bundle_messages.getString("word.billing_others");
							}
							else {
								return billing.getFacilityId();
							}
						}
						return null;
					}
				}
			),
			facility_name(
				bundle_messages.getString("word.facility_name"),
				new ColumnPixelData(160, true, true),
				new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						if(element instanceof FacilityBillingWrapper){
							FacilityBillingWrapper billing = (FacilityBillingWrapper)element;
							if ("others".equals(billing.getFacilityId())) {
								return "-";
							}
							else {
								return billing.getFacilityName();
							}
						}
						return null;
					}
				}
			),
			account_resource(
				strCloudScopeId,
				new ColumnPixelData(100, true, true),
				new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						if(element instanceof ResourceBillingWrapper){
							ResourceBillingWrapper billing = (ResourceBillingWrapper)element;
							return billing.getCloudScopeId();
						}
						return null;
					}
				}
			),
			resource_id(
				bundle_messages.getString("word.billing_resource_id"),
				new ColumnPixelData(160, true, true),
				new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						if(element instanceof ResourceBillingWrapper){
							ResourceBillingWrapper billing = (ResourceBillingWrapper)element;
							return billing.getResourceId();
						}
						return null;
					}
				}
			),
			category(
				bundle_messages.getString("word.billing_category"),
				new ColumnPixelData(220, true, true),
				new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						if(element instanceof ResourceBillingWrapper){
							ResourceBillingWrapper billing = (ResourceBillingWrapper)element;
							if (billing.getCategoryDetail() != null) {
								return billing.getCategory() + "(" + billing.getCategoryDetail() + ")";
							}
							else {
								return billing.getCategory();
							}
						}
						return null;
					}
				}
			),
			display_name(
				bundle_messages.getString("word.billing_display_name"),
				new ColumnPixelData(200, true, true),
				new ColumnLabelProvider(){
					@Override
					public String getText(Object element) {
						if(element instanceof ResourceBillingWrapper){
							ResourceBillingWrapper billing = (ResourceBillingWrapper)element;
							return billing.getDisplayName();
						}
						return null;
					}
				}
			),
			currency(
					bundle_messages.getString("word.billing_unit"),
					new ColumnPixelData(40, true, true),
					new ColumnLabelProvider(){
						@Override
						public String getText(Object element) {
							if(element instanceof ResourceBillingWrapper){
								ResourceBillingWrapper billing = (ResourceBillingWrapper)element;
								return billing.getUnit();
							}
							return null;
						}
					}
				);

			private String label;
			private ColumnLabelProvider provider;
			private ColumnPixelData pixelData;
			
			ViewColumn(String label, ColumnPixelData pixelData, ColumnLabelProvider provider){
				this.label = label;
				this.pixelData = pixelData;
				this.provider = provider;
			}

			public String getLabel() {
				return label;
			}

			public ColumnPixelData getPixelData() {
				return pixelData;
			}

			public ColumnLabelProvider getProvider() {
				return provider;
			}
		}

		private static class TreeContentProvider implements ITreeContentProvider{
			public Object[] getChildren(Object parentElement) {
				if(parentElement instanceof FacilityBillingWrapper){
					FacilityBillingWrapper facility = (FacilityBillingWrapper)parentElement;
					return facility.getResources().toArray();
				}
				return null;
			}

			public Object getParent(Object element) {
				if (element instanceof BillingResultWrapper) {
					return null;
				}
				else if (element instanceof FacilityBillingWrapper) {
					return ((FacilityBillingWrapper)element).getRoot();
				}
				else if (element instanceof ResourceBillingWrapper) {
					return ((ResourceBillingWrapper)element).getParent();
				}
				return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof BillingResultWrapper) {
					return !((BillingResultWrapper)element).getFacilities().isEmpty();
				}
				else if (element instanceof FacilityBillingWrapper) {
					return !((FacilityBillingWrapper)element).getResources().isEmpty();
				}
				return false;
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof BillingResultWrapper) {
					return ((BillingResultWrapper)inputElement).getFacilities().toArray();
				}
				return null;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		}
		
		private static class CostLabelProvider extends ColumnLabelProvider {
			private int month;
			private int day;
			
			public CostLabelProvider(int month, int day) {
				this.month = month;
				this.day = day;
			}
			
			@Override
			public String getText(Object element) {
				if(element instanceof FacilityBillingWrapper){
					FacilityBillingWrapper billing = (FacilityBillingWrapper)element;
					DataPointWrapper p = billing.getTotalsPerDate(month, day);
					
					if (p != null) {
						// とりあえず、AWS の 課金の csv が、8 桁になっています。
						String value = new BigDecimal(p.getPrice()).setScale(8, BigDecimal.ROUND_UP).toPlainString();
						Matcher m = pattern.matcher(value);
						return m.matches() ? m.group(1): ("0.00000000".equals(value) ? "0": value);
					}
					else {
						return "-";
					}
				}
				else if(element instanceof ResourceBillingWrapper){
					ResourceBillingWrapper billing = (ResourceBillingWrapper)element;
					DataPointWrapper p = billing.getPrice(month, day);

					if (p != null) {
						// とりあえず、AWS の 課金の csv が、8 桁になっています。
						String value = new BigDecimal(p.getPrice()).setScale(8, BigDecimal.ROUND_UP).toPlainString();
						Matcher m = pattern.matcher(value);
						return m.matches() ? m.group(1): ("0.00000000".equals(value) ? "0": value);
					}
					else {
						return "-";
					}
				}
				return null;
			}
		}
		
		
		private TreeViewer viwer;
		private Composite control;
		
		public BillingDetailRenderer(Composite base) {
			control = new Composite(base, SWT.NONE);

			TreeColumnLayout tcl = new TreeColumnLayout();
			control.setLayout(tcl);
			
			viwer = new TreeViewer(control, SWT.BORDER);
			Tree tree = viwer.getTree();
			tree.setLinesVisible(true);
			tree.setHeaderVisible(true);
			tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			
			for (ViewColumn column: ViewColumn.values()) {
				TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viwer, SWT.NONE);
				TreeColumn trclmnAaa = treeViewerColumn.getColumn();
				trclmnAaa.setText(column.getLabel());
				treeViewerColumn.setLabelProvider(column.getProvider());
				tcl.setColumnData(trclmnAaa, column.getPixelData());
			}
			base.layout();
		}
		
		public BillingDetailRenderer(Composite base, BillingResultWrapper result) {
			control = new Composite(base, SWT.NONE);

			TreeColumnLayout tcl = new TreeColumnLayout();
			control.setLayout(tcl);
			
			viwer = new TreeViewer(control, SWT.BORDER);
			Tree tree = viwer.getTree();
			tree.setLinesVisible(true);
			tree.setHeaderVisible(true);
			tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			
			for (ViewColumn column: ViewColumn.values()) {
				TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viwer, SWT.NONE);
				TreeColumn trclmnAaa = treeViewerColumn.getColumn();
				trclmnAaa.setText(column.getLabel());
				treeViewerColumn.setLabelProvider(column.getProvider());
				tcl.setColumnData(trclmnAaa, column.getPixelData());
			}
			
			// 取得した結果の中から終了日を取得
			LocalDateTime beginDateTime = result.getBeginDate();
			LocalDateTime endDateTime = result.getEndDate();
			
			// 最初の日付の前まで回ったらstop
			LocalDateTime previous = null;
			for (LocalDateTime dateTime = beginDateTime; dateTime.isBefore(endDateTime); dateTime = dateTime.plusDays(1)) {
				TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viwer, SWT.NONE);
				TreeColumn trclmnAaa = treeViewerColumn.getColumn();
				
				// 最初の日と月が変わったら、日付に月を付与する。
				if (previous == null || previous.getMonthValue() != dateTime.getMonthValue()) {
					trclmnAaa.setText(Integer.toString(dateTime.getMonthValue()) + "/" + Integer.toString(dateTime.getDayOfMonth()));
				} else {
					trclmnAaa.setText(Integer.toString(dateTime.getDayOfMonth()));
				}
				
				trclmnAaa.setAlignment(SWT.RIGHT);
				treeViewerColumn.setLabelProvider(new CostLabelProvider(dateTime.getMonthValue(), dateTime.getDayOfMonth()));
				tcl.setColumnData(trclmnAaa, new ColumnPixelData(80, true, true));
				
				previous = dateTime;
			}
			
			viwer.setContentProvider(new TreeContentProvider());
			viwer.setInput(result);
		}
		
		public Control getControl() {
			return control;
		}
		
		public void dispose() {
			control.dispose();
		}
	}
	
	public static class BillingResultWrapper {
		protected BillingResult data;
		protected List<FacilityBillingWrapper> children;
		
		public BillingResultWrapper(BillingResult data) {
			this.data = data;
		}
		
		public Instant getBeginTime() {
			return Instant.ofEpochMilli(data.getBeginTime());
		}
		
		public Instant getEndTime() {
			return Instant.ofEpochMilli(data.getEndTime());
		}
		
		public LocalDateTime getBeginDate() {
			return LocalDateTime.ofInstant(getBeginTime(), ZoneOffset.UTC);
		}
		
		public LocalDateTime getEndDate() {
			return LocalDateTime.ofInstant(getEndTime(), ZoneOffset.UTC);
		}
		
		public List<FacilityBillingWrapper> getFacilities() {
			if (children == null) {
				children = new ArrayList<>();
				for (int i = data.getFacilities().size() - 1; i >= 0; --i) {
					if ("etc".equals(data.getFacilities().get(i).getFacilityId())) {
						children.add(createFacilityBillingWrapper(data.getFacilities().get(i)));
					}
					else {
						children.add(0, createFacilityBillingWrapper(data.getFacilities().get(i)));
					}
				}
			}
			return children;
		}

		public String getTargetId() {
			return data.getTargetId();
		}

		public Integer getTargetMonth() {
			return data.getTargetMonth();
		}

		public String getTargetName() {
			return data.getTargetName();
		}

		public Integer getTargetYear() {
			return data.getTargetYear();
		}

		public String getUnit() {
			return data.getUnit();
		}

		public TargetType getType() {
			return data.getType();
		}
		
		protected FacilityBillingWrapper createFacilityBillingWrapper(FacilityBilling data) {
			return new FacilityBillingWrapper(this, data);
		}
	}
	
	public static class FacilityBillingWrapper {
		protected BillingResultWrapper root;
		protected FacilityBilling data;
		protected List<ResourceBillingWrapper> resources;
		protected Map<List<Integer>, DataPointWrapper> pricesMap;
		
		public FacilityBillingWrapper(BillingResultWrapper root, FacilityBilling data) {
			this.root = root;
			this.data = data;
		}
		
		public List<ResourceBillingWrapper> getResources() {
			if (resources == null) {
				int etcIndex = 0;
				resources = new ArrayList<>();
				for (ResourceBilling r: data.getResources()) {
					ResourceBillingWrapper billingWrapper = createResourceBillingWrapper(r);
					if ("etc".equals(r.getCategory())) {
						resources.add(billingWrapper);
					}
					else {
						resources.add(etcIndex++, billingWrapper);
					}
				}
			}
			return resources;
		}
		
		public DataPointWrapper getTotalsPerDate(int month, int day) {
			if (pricesMap == null) {
				pricesMap = new HashMap<>();
				for (DataPoint price: data.getTotalsPerDate()) {
					DataPointWrapper d = createDataPointWrapper(price);
					pricesMap.put(Arrays.asList(d.getMonth(), d.getDay()), d);
				}
			}
			return pricesMap.get(Arrays.asList(month, day));
		}

		public String getFacilityId() {
			return data.getFacilityId();
		}

		public String getFacilityName() {
			return data.getFacilityName();
		}

		public BillingResultWrapper getRoot() {
			return root;
		}
		
		protected ResourceBillingWrapper createResourceBillingWrapper(ResourceBilling data) {
			return new ResourceBillingWrapper(this, data);
		}
		
		protected DataPointWrapper createDataPointWrapper(DataPoint data) {
			return new DataPointWrapper(data);
		}
	}
	
	public static class ResourceBillingWrapper {
		protected FacilityBillingWrapper parent;
		protected ResourceBilling data;
		protected Map<List<Integer>, DataPointWrapper> pricesMap;
		
		public ResourceBillingWrapper(FacilityBillingWrapper parent, ResourceBilling data) {
			this.parent = parent;
			this.data = data;
		}
		
		public DataPointWrapper getPrice(int month, int day) {
			if (pricesMap == null) {
				pricesMap = new HashMap<>();
				for (DataPoint price: data.getPrices()) {
					DataPointWrapper d = createDataPointWrapper(price);
					pricesMap.put(Arrays.asList(d.getMonth(), d.getDay()), d);
				}
			}
			return pricesMap.get(Arrays.asList(month, day));
		}
		
		public FacilityBillingWrapper getParent() {
			return parent;
		}

		public String getUnit() {
			return data.getUnit();
		}

		public String getCategory() {
			return data.getCategory();
		}

		public String getCategoryDetail() {
			return data.getCategoryDetail();
		}

		public String getCloudScopeId() {
			return data.getCloudScopeId();
		}

		public String getCloudScopeName() {
			return data.getCloudScopeName();
		}

		public String getDisplayName() {
			return data.getDisplayName();
		}

		public String getResourceId() {
			return data.getResourceId();
		}
		
		protected DataPointWrapper createDataPointWrapper(DataPoint data) {
			return new DataPointWrapper(data);
		}
	}
	
	public static class DataPointWrapper {
		protected DataPoint data;
		
		public DataPointWrapper(DataPoint data) {
			this.data = data;
		}
		
		public int getDay() {
			return data.getDay();
		}

		public int getMonth() {
			return data.getMonth();
		}

		public double getPrice() {
			return data.getPrice();
		}
	}
	
	public void update(DataProvider provider, int year, int month) {
		assert provider != null;
		
		setState(nullState);

		// 全ての情報を破棄。
		for (State s: rendererMap.values()) {
			if (s.renderer != nullState.renderer) {
				s.renderer.dispose();
			}
		}
		rendererMap.clear();
		
		this.dataProvider = provider;
		
		update(year, month);
	}
	
	public State getCurrentState() {
		return currentState == nullState ? null: currentState;
	}
	
	public void update(int year, int month) {
		Object key = Arrays.asList(year, month);
		State s = rendererMap.get(key);
		if (s == null) {
			s = new State();
			s.year = year;
			s.month = month;
			s.dataHolder = dataProvider.getData(year, month);
			s.result = createBillingResultWrapper(s.dataHolder.getData());
			s.renderer = new BillingDetailRenderer(base, s.result);
			rendererMap.put(key, s);
		}
		setState(s);
	}

	private void setState(State s) {
		if (s != currentState) {
			currentState = s;
			updateControls(currentState);
			
			firePropertyChange(Prop_billing_detail_target);
		}
	}

	private void updateControls(State s) {
		layout.topControl = s.renderer.getControl();
		
		if (s.result != null) {
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			switch (s.dataHolder.getData().getType()) {
			case CLOUD_SCOPE:
				lblHeader.setText(String.format(bundle_messages.getString("word.billing_date_format_cloudscope"), s.year, s.month, s.result.getTargetId(),
						df.format(new Date(s.result.getBeginTime().toEpochMilli())),
						df.format(new Date(s.result.getEndTime().minus(1, ChronoUnit.DAYS).toEpochMilli() - 1))));
				break;
			case FACILITY:
				lblHeader.setText(String.format(bundle_messages.getString("word.billing_date_format_facility"), s.year, s.month, s.result.getTargetId(),
						df.format(new Date(s.result.getBeginTime().toEpochMilli())),
						df.format(new Date(s.result.getEndTime().minus(1, ChronoUnit.DAYS).toEpochMilli() - 1))));
				break;
			}
		}
		else {
			lblHeader.setText(bundle_messages.getString("word.billing_display_date") + " " + bundle_messages.getString("caption.title_separator") + " ");
		}
 
		base.layout();
	}
	
	public void nextMonth() {
		if (currentState == nullState) return;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(currentState.year, currentState.month - 1, 1);
		calendar.add(Calendar.MONTH, 1);
		
		update(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
	}
	
	public void previousMonth() {
		if (currentState == nullState) return;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(currentState.year, currentState.month - 1, 1);
		calendar.add(Calendar.MONTH, -1);
		
		update(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
	}

	@Override
	protected StructuredViewer getViewer() {
		return currentState.renderer.viwer;
	}

	@Override
	public String getId() {
		return Id;
	}
	
	protected BillingResultWrapper createBillingResultWrapper(BillingResult result) {
		return new BillingResultWrapper(result);
	}
}
