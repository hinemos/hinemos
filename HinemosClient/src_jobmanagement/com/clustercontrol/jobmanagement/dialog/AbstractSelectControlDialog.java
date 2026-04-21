/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.dialog;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.JobOperationPropResponse.AvailableOperationListEnum;
import org.openapitools.client.model.JobOperationRequest.EndStatusEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.EndStatusImageConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.JobImageConstant;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.jobmanagement.JobMessage;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.bean.StatusImageConstant;
import com.clustercontrol.jobmanagement.composite.DetailComposite.JobElement;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.AbstractHierarchicalElementViewer;
import com.clustercontrol.xcloud.ui.dialogs.DialogConstants;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/*
 * ジョブ履歴[ジョブ詳細]ビューで選択されたジョブの開始・停止ダイアログの基底
 */
@SuppressFBWarnings(
	value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
	justification = "viewer is initialized in createView() before SWT layout callbacks are used."
)
public abstract class AbstractSelectControlDialog extends CommonDialog {
	// 内部の階層データモデルのルート
	public static class Root {
		// テーブルコントロールに表示される際の階層構造のルート要素リスト
		protected final List<Item> items = new LinkedList<>();
		
		protected Optional<AvailableOperationListEnum> operatiotn = Optional.empty();
		protected Optional<EndStatusEnum> endStatus = Optional.empty();
		protected Optional<Integer> endValue = Optional.empty();
		
		protected final Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOperation;
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Internal UI model.")
		public Root(Set<JobElement> jobs, Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOperation) {
			jobs = Objects.requireNonNull(jobs);

			this.jobMapByOperation = Objects.requireNonNull(jobMapByOperation);
			
			Map<JobElement, Item> itemMap = new LinkedHashMap<>();
			
			// 無作為に選択されたジョブの要素を階層化
			for (JobElement s: jobs) {
				// 処理済みの要素か？
				if (itemMap.containsKey(s)) {
					continue;
				}
				
				// 未処理なので、仮想化処理をすすめる
				Item current = new Item(this, s);
				
				// 処理済みマップに登録
				itemMap.put(s, current);
				
				JobElement c = s;
				JobElement p;
				
				// 親要素をたどっていく
				while ((p = c.getParent()) != null) {
					// 親要素が処理済みか？
					if (itemMap.containsKey(p)) {
						// 親要素が処理済みなら、親要素にカレントの要素を追加
						itemMap.get(p).addChild(current);
						break;
					// 親要素が未処理済みか？
					} else {
						// 親要素が選択項目に含まれているか？
						if (jobs.contains(p)) {
							// 親要素かつ選択項目なので、親要素の階層化処理をすすめる
							Item i = new Item(this, p);
							itemMap.put(p, i);
							i.addChild(current);
							current = i;
						}
					}
					c = p;
				}
				
				// このダイアログで表示する選択済み項目に親要素が含まれていないので表示時のルートのひとつとして扱う
				if (p == null) {
					items.add(current);
				}
			}
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public List<Item> getItems() {
			return items;
		}
		
		public void setOperation(AvailableOperationListEnum operation) {
			this.operatiotn = Optional.ofNullable(operation);
		}
		
		public Optional<AvailableOperationListEnum> getOperation() {
			return operatiotn;
		}
		
		public void setNewEndStatus(EndStatusEnum endStatus) {
			this.endStatus = Optional.ofNullable(endStatus);
		}
		
		public Optional<EndStatusEnum> getNewEndStatus() {
			return endStatus;
		}
		
		public void setNewEndValue(Integer endStatus) {
			this.endValue = Optional.ofNullable(endStatus);
		}
		
		public Optional<Integer> getNewEndValue() {
			return endValue;
		}
		
		protected boolean isOperationTarget(Item target) {
			// 対象の実行可能な操作に指定された操作が含まれているか？
			return jobMapByOperation.getOrDefault(operatiotn.orElse(null), Collections.emptySet()).contains(target.getJobElement());
		}
	}
	
	/*
	 * 内部の階層データモデルの子要素
	 */
	public static class Item {
		protected final JobElement element;
		protected final Root root;
		
		protected Item parent;
		
		protected final List<Item> children = new LinkedList<>();;
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Internal UI model.")
		public Item(Root root, JobElement element) {
			root = Objects.requireNonNull(root);
			element = Objects.requireNonNull(element);

			this.element = element;
			this.root = root;
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public Root getRoot() {
			return root;
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Internal UI model.")
		public void setParent(Item parent) {
			this.parent = parent;
		}

		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public Item getParent() {
			return parent;
		}
		
		public List<Item> getChildren() {
			return new LinkedList<>(children);
		}
		
		public void addChild(Item child) {
			child.setParent(this);
			children.add(child);
		}
		
		public boolean isOperationTarget() {
			return root.isOperationTarget(this);
		}
		
		@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
		public JobElement getJobElement() {
			return element;
		}
	}
	
	/*
	 * 選択されたジョブを表示するテーブル
	 */
	protected static abstract class AbstractViewer extends AbstractHierarchicalElementViewer {
		protected final static String CLMN_JOB_NAME = "job.name";
		protected final static String CLMN_JOB_ID = "job.id";
		protected final static String CLMN_TYPE = "type";
		
		protected static class AdditionalColumnInfo {
			protected final String name;
			protected final int style;
			protected final int width;
			
			protected AdditionalColumnInfo(String id, int style, int width) {
				this.name = id;
				this.style = style;
				this.width = width;
			}
		}
		
		protected final Root root;
		
		protected AbstractViewer(Composite parent, Root root) {
			super(parent);
			this.root = Objects.requireNonNull(root);
			initializeColumns(setupColumn());
			setInput(buildInput());
		}
		
		protected final Element[] buildInput() {
			List<Element> tops = root.getItems().stream().map(c->recursiveNewElement(c)).collect(Collectors.toList());
			return tops.toArray(new Element[]{});
		}
		
		protected Element recursiveNewElement(Item item) {
			Element l = newElement(item);
			l.addChild(item.getChildren().stream().map(c->recursiveNewElement(c)).collect(Collectors.toList()));
			return l;
		}
		
		protected final LinkedHashMap<String, TreeViewerColumn> setupColumn() {
			Locale locale = Locale.getDefault();
			
			LinkedHashMap<String, TreeViewerColumn> map = new LinkedHashMap<>();
			
			int i = 0;
			
			TreeViewerColumn clmnJobName = new TreeViewerColumn(this, SWT.NONE, i++);
			clmnJobName.getColumn().setText(Messages.getString(CLMN_JOB_NAME, locale));
			clmnJobName.getColumn().setWidth(200);
			map.put(CLMN_JOB_NAME, clmnJobName);
			
			TreeViewerColumn clmnJobId = new TreeViewerColumn(this, SWT.NONE, i++);
			clmnJobId.getColumn().setText(Messages.getString(CLMN_JOB_ID, locale));
			clmnJobId.getColumn().setWidth(200);
			map.put(CLMN_JOB_ID, clmnJobId);

			TreeViewerColumn clmnTyoe = new TreeViewerColumn(this, SWT.NONE, i++);
			clmnTyoe.getColumn().setText(Messages.getString(CLMN_TYPE, locale));
			clmnTyoe.getColumn().setWidth(150);
			map.put(CLMN_TYPE, clmnTyoe);
			
			for (AdditionalColumnInfo ci: getAdditionalColumn()) {
				TreeViewerColumn clmnRunStatus = new TreeViewerColumn(this, ci.style, i++);
				clmnRunStatus.getColumn().setText(Messages.getString(ci.name, locale));
				clmnRunStatus.getColumn().setWidth(ci.width);
				map.put(ci.name, clmnRunStatus);
			}
			
			return map;
		}
		
		protected Element newElement(Item item) {
			Element element = Element.build()
				.addProperty(
					PropertyBinding.build(CLMN_JOB_NAME, ()->item.getJobElement().getItem().getData().getName(),
							()->getImageDescriptor(item.isOperationTarget())
						),
					PropertyBinding.build(CLMN_JOB_ID, ()->item.getJobElement().getItem().getData().getId()),
					PropertyBinding.build(CLMN_TYPE, ()->JobMessage.typeEnumValueToString(item.getJobElement().getItem().getData().getType().getValue()),
							()->JobImageConstant.typeEnumValueToImage(item.getJobElement().getItem().getData().getType().getValue())
						)
				);
			
			return element.addProperty(createAdditionalPropertyBinding(item));
		}
		
		protected static List<AdditionalColumnInfo> getAdditionalColumn() {
			return Arrays.asList(new AdditionalColumnInfo[]{
					new AdditionalColumnInfo("run.status", SWT.NONE, 80),
					new AdditionalColumnInfo("end.status", SWT.NONE, 80),
					new AdditionalColumnInfo("end.value", SWT.NONE, 80)
					});
		}
		
		protected List<PropertyBinding> createAdditionalPropertyBinding(Item item) {
			PropertyBinding p1 = PropertyBinding.build(
					"run.status",
					()->Optional.ofNullable(item.getJobElement().getItem().getDetail().getStatus())
							.map(s->StatusMessage.typeEnumValueToString(s.getValue()))
							.orElse(""),
					()->Optional.ofNullable(item.getJobElement().getItem().getDetail().getStatus())
							.map(s->StatusImageConstant.typeEnumValueToImage(s.getValue()))
							.orElse(null)
					);
			PropertyBinding p2 = PropertyBinding.build(
					"end.status",
					()->Optional.ofNullable(item.getJobElement().getItem().getDetail().getEndStatus())
							.map(s->EndStatusMessage.typeEnumValueToString(s.getValue()))
							.orElse(""),
					()->Optional.ofNullable(item.getJobElement().getItem().getDetail().getEndStatus())
							.map(s->EndStatusImageConstant.typeEnumValueToImage(s.getValue()))
							.orElse(null)
					);
			PropertyBinding p3 = PropertyBinding.build(
					"end.value",
					()->Optional.ofNullable(item.getJobElement().getItem().getDetail().getEndValue()).map(v->Integer.toString(v)).orElse(""),
					()->null
					);
			return Arrays.asList(new PropertyBinding[]{p1, p2, p3});
		}
		
		protected abstract String getImageKey(boolean enable);
		
		protected abstract String getImagePath(boolean enable);
		
		protected Image getImageDescriptor(boolean enable) {
			ImageRegistry reg = JFaceResources.getImageRegistry();
			
			String key = getImageKey(enable);
			Image cached = reg.get(key);
			if (cached == null) {
				reg.put(key, imageDescriptor(getImagePath(enable)));
				cached = reg.get(key);
			}
			return cached;
		}
		
		protected ImageDescriptor imageDescriptor(String pluguinRelationPath) {
			try {
				URL url = new URL(ClusterControlPlugin.getDefault().getBundle().getEntry("/"), pluguinRelationPath);
				return ImageDescriptor.createFromURL(url);
			} catch (Exception e) {
				getLogger().debug(e.getMessage(), e);
			}
			return null;
		}
		
		protected abstract Log getLogger();
	}
	
	protected AbstractViewer viewer;
	
	protected final Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOperation;
	
	protected final Root root;
	
	@SuppressFBWarnings(
		value = {"CT_CONSTRUCTOR_THROW", "EI_EXPOSE_REP2"},
		justification = "Empty operation map is a caller contract violation and should fail fast."
	)
	public AbstractSelectControlDialog(Shell parent, Set<JobElement> selected, Map<AvailableOperationListEnum, Set<JobElement>> jobMapByOpe) {
		super(parent);
		Objects.requireNonNull(selected);
		Objects.requireNonNull(jobMapByOpe);
		
		if (jobMapByOpe.isEmpty()) {
			// 空のマップを指定するのは拒否する
			// 空ならダイアログを作成する前に回避しておく！！
			throw new IllegalStateException("No available operations for AbstractSelectControlDialog.");
		}
		
		this.jobMapByOperation = jobMapByOpe;
		this.root = new Root(selected, jobMapByOpe);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getDialogTitle());
	}
	
	@Override
	protected void customizeDialog(Composite parent) {
		Locale locale = Locale.getDefault();
		
		GridData gd_parent = new GridData(SWT.FILL, SWT.FILL, true, false);
		parent.setLayoutData(gd_parent);
		
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.marginRight = 10;
		layout.marginLeft = 10;
		parent.setLayout(layout);
		
		Label lblControl = new Label(parent, SWT.RIGHT);
		lblControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblControl.setText(Messages.getString("control", locale) + Messages.getString("caption.title_separator", locale));
		
		Combo cmbControl = new Combo(parent, SWT.READ_ONLY);
		cmbControl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		
		jobMapByOperation.keySet().stream().forEach(k->{
				String ope = OperationMessage.enumToString(k);
				cmbControl.add(ope);
				cmbControl.setData(ope, k);
			});
			
		if (cmbControl.getItemCount() > 0) {
			cmbControl.select(0);
			AvailableOperationListEnum ope = (AvailableOperationListEnum)cmbControl.getData(cmbControl.getItem(cmbControl.getSelectionIndex()));
			root.setOperation(ope);
			
			// 制御が変更されたテーブルの表示を更新
			cmbControl.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					AvailableOperationListEnum o = (AvailableOperationListEnum)cmbControl.getData(cmbControl.getItem(cmbControl.getSelectionIndex()));
					root.setOperation(o);
					viewer.refresh();
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		
		Label lblTargets = new Label(parent, SWT.RIGHT);
		lblTargets.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		lblTargets.setText(Messages.getString("job.selected.jobs", locale) + Messages.getString("caption.title_separator", locale));
		
		viewer = createViewer(parent, root);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		viewer.expandAll();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, DialogConstants.OK_ID, DialogConstants.OK_LABEL, true);
		createButton(parent, DialogConstants.CANCEL_ID, DialogConstants.CANCEL_LABEL, false);
	}
	
	@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Internal UI model.")
	public Root getModel() {
		return root;
	}
	
	protected abstract String getDialogTitle();
	
	protected abstract Log getLogger();
	
	protected abstract AbstractViewer createViewer(Composite parent, Root root);
}
