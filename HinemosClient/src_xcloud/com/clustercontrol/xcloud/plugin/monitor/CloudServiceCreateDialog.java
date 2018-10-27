/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.monitor;

import static com.clustercontrol.xcloud.common.CloudConstants.bundle_messages;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite;
import com.clustercontrol.monitor.run.composite.MonitorRuleComposite;
import com.clustercontrol.monitor.run.composite.TruthValueInfoComposite;
import com.clustercontrol.monitor.run.dialog.CommonMonitorTruthDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorPluginStringInfo;
import com.clustercontrol.ws.monitor.PluginCheckInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.CloudScope;
import com.clustercontrol.ws.xcloud.HCloudScopeRootScope;
import com.clustercontrol.ws.xcloud.HCloudScopeScope;
import com.clustercontrol.ws.xcloud.HFacility;
import com.clustercontrol.ws.xcloud.HLocationScope;
import com.clustercontrol.ws.xcloud.HRepository;
import com.clustercontrol.ws.xcloud.HScope;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.ws.xcloud.PlatformServiceCondition;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.InvalidStateException;
import com.clustercontrol.xcloud.model.cloud.ICloudPlatform;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.ControlUtil;
import com.clustercontrol.xcloud.util.CloudUtil;

/**
 * クラウドサービス監視作成・変更ダイアログクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.0.0
 */
public class CloudServiceCreateDialog extends CommonMonitorTruthDialog implements CloudStringConstants {
	public static final long serialVersionUID = 1L;
	public class FacilityTreeItemWrapper extends FacilityTreeItem {
		private HScope scope;
		private FacilityTreeItem item;

		public FacilityTreeItemWrapper(HScope scope, FacilityTreeItem item) {
			this.scope = scope;
			this.item = item;
		}

		public List<FacilityTreeItem> getChildren() {
			if (children == null) {
				children = new ArrayList<>();
				
				List<FacilityTreeItem> items = new ArrayList<FacilityTreeItem>(item.getChildren());
				Iterator<Object> hFacilityIter = new ArrayList<>(scope.getFacilities()).iterator();
				while(hFacilityIter.hasNext()) {
					HFacility f = (HFacility)hFacilityIter.next();
					if (f instanceof HCloudScopeScope || f instanceof HLocationScope) {
						Iterator<FacilityTreeItem> itemIter = items.iterator();
						while(itemIter.hasNext()) {
							FacilityTreeItem i = (FacilityTreeItem)itemIter.next();
							if (f.getId().equals(i.getData().getFacilityId())) {
								hScopeMap.put(f.getId(), (HScope)f);
								
								FacilityTreeItem wrapper = new FacilityTreeItemWrapper((HScope)f, i);
								wrapper.setParent(this);
								children.add(wrapper);
								hFacilityIter.remove();
								itemIter.remove();
								break;
							}
						}
					}
				}
			}
			return this.children;
		}
		
		public FacilityInfo getData() {
			return item.getData();
		}
		
		public HScope getHScope() {
			return scope;
		}
	};

	private String msgSelectTarget = bundle_messages.getString("message.must_select_service");

	// ログ
	//	private static Log logger = LogFactory.getLog( AgentCreateDialog.class );
	private static final Log logger = LogFactory.getLog(CloudServiceCreateDialog.class);

	private Combo targetCombo;

	private String currentFacilityId;
	private Map<String, HScope> hScopeMap = new HashMap<>();
	private Map<String, PlatformServiceCondition> serviceMap = new HashMap<>();
	private Map<String, String> selectedServiceMap = new HashMap<>();

	// ----- コンストラクタ ----- //

	/**
	 * 作成用ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 *            親のシェルオブジェクト
	 * @wbp.parser.constructor
	 */
	public CloudServiceCreateDialog(Shell parent) {
		super(parent);
	}

	/**
	 * 変更用ダイアログのインスタンスを返します。
	 * 
	 * @param parent
	 *            親のシェルオブジェクト
	 * @param monitorId
	 *            変更する監視項目ID
	 * @param updateFlg
	 *            更新するか否か（true:変更、false:新規登録）
	 * 
	 */
	public CloudServiceCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName, monitorId);

		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
		this.managerName = managerName;
	}

	// ----- instance メソッド ----- //

	/**
	 * ダイアログエリアを生成します。
	 * 
	 * @param parent
	 *            親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 15;
		parent.setLayout(layout);

		// 監視基本情報
		m_monitorBasic = new MonitorBasicScopeComposite(parent, SWT.NONE, m_unregistered, this);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_monitorBasic.setLayoutData(gridData);
		if(this.managerName != null) {
			m_monitorBasic.getManagerListComposite().setText(this.managerName);
		}

		for (Listener l: m_monitorBasic.getButtonScope().getListeners(SWT.Selection)) {
			m_monitorBasic.getButtonScope().removeSelectionListener((SelectionListener)((TypedListener)l).getEventListener());
		}

		m_monitorBasic.getButtonScope().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Field m_facilityId = null;
				boolean m_facilityIdAccesible = false;
				Field m_textScope = null;
				boolean m_textScopeAccesible = false;
				try {
					m_facilityId = m_monitorBasic.getClass().getDeclaredField("m_facilityId");
					m_facilityIdAccesible = m_facilityId.isAccessible();
					m_facilityId.setAccessible(true);

					m_textScope = m_monitorBasic.getClass().getDeclaredField("m_textScope");
					m_textScopeAccesible = m_textScope.isAccessible();
					m_textScope.setAccessible(true);

					CloudServiceCreateDialog.this.managerName = m_monitorBasic.getManagerListComposite().getText();
					final ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, m_monitorBasic.getOwnerRoleId(), true, false) {
						@Override
						protected void customizeDialog(Composite parent) {
							Field treeComposite = null;
							boolean treeCompositeAccesible = false;
							try {
								treeComposite = ScopeTreeDialog.class.getDeclaredField("treeComposite");
								treeCompositeAccesible = treeComposite.isAccessible();
								treeComposite.setAccessible(true);

								// タイトル
								parent.getShell().setText(Messages.getString("select.scope"));

								GridLayout layout = new GridLayout(5, true);
								parent.setLayout(layout);
								layout.marginHeight = 0;
								layout.marginWidth = 0;

								treeComposite.set(this, new FacilityTreeComposite(parent, SWT.NONE, getManagerName(), m_monitorBasic.getOwnerRoleId(), true) {
									/**
									 * ビューの表示内容を更新します。
									 */
									@Override
									public void update() {
										final CloudEndpoint endpoint = ClusterControlPlugin.getDefault().getHinemosManager(managerName).getEndpoint(CloudEndpoint.class);
										HRepository cloudRepository;
										try {
											cloudRepository = endpoint.getRepositoryByRole(m_monitorBasic.getOwnerRoleId());
										} catch (
												CloudManagerException
												| com.clustercontrol.ws.xcloud.InvalidRole_Exception
												| InvalidUserPass_Exception e) {
											logger.warn(e.getMessage(), e);
											throw new InvalidStateException(e.getMessage(), e);
										}
										
										FacilityTreeItem treeItem;
										try {
											treeItem = RepositoryEndpointWrapper.getWrapper(managerName).getFacilityTree(m_monitorBasic.getOwnerRoleId());
											if (treeItem != null && treeItem.getChildren() != null && treeItem.getChildren().get(0) != null) {
												Collections.sort(treeItem.getChildren().get(0).getChildren(), new Comparator<FacilityTreeItem>() {
													@Override
													public int compare(FacilityTreeItem o1, FacilityTreeItem o2) {
														FacilityInfo info1 = ((FacilityTreeItem) o1).getData();
														FacilityInfo info2 = ((FacilityTreeItem) o2).getData();
														int order1 =  info1.getDisplaySortOrder();
														int order2 =  info2.getDisplaySortOrder();
														if(order1 == order2 ){
															String object1 = info1.getFacilityId();
															String object2 = info2.getFacilityId();
															return object1.compareTo(object2);
														}
														else {
															return (order1 - order2);
														}
													}
												});
											}
										} catch (
												HinemosUnknown_Exception
												| com.clustercontrol.ws.repository.InvalidRole_Exception
												| com.clustercontrol.ws.repository.InvalidUserPass_Exception e) {
											logger.warn(e.getMessage(), e);
											throw new InvalidStateException(e.getMessage(), e);
										}
										
										final FacilityTreeItem treeItemWrapper = addEmptyParent(createTreeItemWrapper(cloudRepository, treeItem));

										if(!this.isDisposed()){
											logger.trace("FacilityTreeComposite.checkAsyncExec() is true");
											getDisplay().asyncExec(new Runnable(){
												@Override
												public void run() {
													logger.trace("FacilityTreeComposite.checkAsyncExec() do runnnable");

													FacilityTreeItem oldTreeItem = (FacilityTreeItem)treeViewer.getInput();
													logger.debug("run() oldTreeItem=" + oldTreeItem);
													if( null != oldTreeItem ){
														if (!oldTreeItem.equals(treeItemWrapper)) {
															ArrayList<String> expandIdList = new ArrayList<String>();
															for (Object item : treeViewer.getExpandedElements()) {
																expandIdList.add(((FacilityTreeItem)item).getData().getFacilityId());
															}
															treeViewer.setInput(treeItemWrapper);
															treeViewer.refresh();
															expand(treeItemWrapper, expandIdList);
														}
													}else{
														logger.info("oldTreeItem is null");
														treeViewer.setInput(treeItemWrapper);
														List<FacilityTreeItem> selectItem = treeItemWrapper.getChildren();
														if (!selectItem.isEmpty()) {
															treeViewer.setSelection(new StructuredSelection(selectItem.get(0)), true);
															//スコープのレベルまで展開
															treeViewer.expandToLevel(4);
														}
													}
												}

												private void expand(FacilityTreeItem item, List<String> expandIdList) {
													if (expandIdList.contains(item.getData().getFacilityId())) {
														treeViewer.expandToLevel(item, 1);
													}
													for (FacilityTreeItem child : item.getChildren()) {
														expand(child, expandIdList);
													}
												}
											});
										}
										else{
											logger.trace("FacilityTreeComposite.checkAsyncExec() is false");
										}
									}
								});

								FacilityTreeComposite w = (FacilityTreeComposite)treeComposite.get(this);
								WidgetTestUtil.setTestId(this, null, w);

								GridData gridData = new GridData();
								gridData.horizontalAlignment = GridData.FILL;
								gridData.verticalAlignment = GridData.FILL;
								gridData.grabExcessHorizontalSpace = true;
								gridData.grabExcessVerticalSpace = true;
								gridData.horizontalSpan = 5;
								w.setLayoutData(gridData);

								// アイテムをダブルクリックした場合、それを選択したこととする。
								w.getTreeViewer().addDoubleClickListener(
										new IDoubleClickListener() {
											@Override
											public void doubleClick(DoubleClickEvent event) {
												okPressed();
											}
										});
							} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
								e1.printStackTrace();
							} finally {
								if (treeComposite != null)
									treeComposite.setAccessible(treeCompositeAccesible);
							}
						}
						
						private FacilityTreeItem addEmptyParent(FacilityTreeItem childTree) {
							FacilityTreeItem rootTree = null;

							if (childTree != null) {
								// 木構造最上位インスタンスの生成
								rootTree = new FacilityTreeItem();
								FacilityInfo rootInfo = new FacilityInfo();
								rootInfo.setBuiltInFlg(true);
								rootInfo.setFacilityName(FacilityConstant.STRING_COMPOSITE);
								rootInfo.setFacilityType(FacilityConstant.TYPE_COMPOSITE);
								rootTree.setData(rootInfo);
								childTree.setParent(rootTree);
								rootTree.getChildren().add(childTree);
							}
							
							return rootTree;
						}
						
						@Override
						protected ValidateResult validate() {
							ValidateResult result = null;
							FacilityTreeItem item = this.getSelectItem();

							// ノード・スコープが選択可能な場合
							if (item == null
									|| item.getData().isNotReferFlg()
									|| item.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE
									|| item.getData().getFacilityType() == FacilityConstant.TYPE_MANAGER) {
								// 未選択の場合エラー
								// 参照不可のスコープを選択した場合はエラー
								// ルートを選択した場合はエラー
								result = new ValidateResult();
								result.setValid(false);
								result.setID(Messages.getString("message.hinemos.1"));
								result.setMessage(Messages.getString("message.repository.47"));
							} else if (item instanceof FacilityTreeItemWrapper) {
								FacilityTreeItemWrapper wrapper = (FacilityTreeItemWrapper)item;
								if (wrapper.getHScope() != null && wrapper.getHScope() instanceof HCloudScopeRootScope) {
									result = new ValidateResult();
									result.setValid(false);
									result.setID(Messages.getString("message.hinemos.1"));
									result.setMessage(CloudConstants.bundle_messages.getString("message.cant_select_cloud_root_scope"));
								}
							}
							return result;
						}
					};
					if (dialog.open() == IDialogConstants.OK_ID) {
						FacilityTreeItem item = dialog.getSelectItem();
						FacilityInfo info = item.getData();
						m_facilityId.set(m_monitorBasic, info.getFacilityId());
						
						FacilityPath path = new FacilityPath(
								ClusterControlPlugin.getDefault()
								.getSeparator());
						((Text)m_textScope.get(m_monitorBasic)).setText(path.getPath(item));
						
						changeSelection(info.getFacilityId());
					}
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
				} finally {
					if (m_facilityId != null)
						m_facilityId.setAccessible(m_facilityIdAccesible);

					if (m_textScope != null)
						m_textScope.setAccessible(m_textScopeAccesible);
				}
			}
			
		});

		/*
		 * 条件グループ
		 */
		groupRule = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupRule.setLayoutData(gridData);
		groupRule.setText(Messages.getString("monitor.rule"));

		m_monitorRule = new MonitorRuleComposite(groupRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_monitorRule.setLayoutData(gridData);

		/*
		 * ターゲット
		 */
		// ラベル
		label = new Label(groupRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(strCloudServiceName + strSeparator);

		targetCombo = new Combo(groupRule, SWT.BORDER | SWT.HIDE_SELECTION | SWT.READ_ONLY);
		GridData gd_templateJobTable = new GridData(SWT.FILL, SWT.FILL, true, true, 8, 1);
		targetCombo.setLayoutData(gd_templateJobTable);
		targetCombo.setVisibleItemCount(15);
		ControlUtil.setRequired(targetCombo);
		
		/*
		 * 監視グループ
		 */
		groupMonitor = new Group(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupMonitor.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupMonitor.setLayoutData(gridData);
		groupMonitor.setText(Messages.getString("monitor.run"));

		// 監視（有効／無効）
		this.confirmMonitorValid = new Button(groupMonitor, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.confirmMonitorValid.setLayoutData(gridData);
		this.confirmMonitorValid.setText(Messages.getString("monitor.run"));
		this.confirmMonitorValid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 判定、通知部分を有効/無効化
				if(confirmMonitorValid.getSelection()){
					setMonitorEnabled(true);
				}else{
					setMonitorEnabled(false);
				}
			}
		});

		/*
		 * 判定グループ（監視グループの子グループ）
		 * なお、判定内容は継承先のクラスにて実装する。
		 */
		groupDetermine = new Group(groupMonitor, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		groupDetermine.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupDetermine.setLayoutData(gridData);
		groupDetermine.setText(Messages.getString("determine"));

		/*
		 * 通知グループ（監視グループの子グループ）
		 */
		groupNotifyAttribute = new Group(groupMonitor, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		groupNotifyAttribute.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupNotifyAttribute.setLayoutData(gridData);
		groupNotifyAttribute.setText(Messages.getString("notify.attribute"));
		this.m_notifyInfo = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_notifyInfo.setLayoutData(gridData);

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);


		// 真偽値判定定義情報
		m_truthValueInfo = new TruthValueInfoComposite(groupDetermine,
				SWT.NONE,
				true,
				Messages.getString("OK"),
				Messages.getString("NG"));
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_truthValueInfo.setLayoutData(gridData);


		// タイトル
		shell.setText(dlgCloudServiceCreateModify);

		// ダイアログを調整
		this.adjustDialog();

		// 初期表示
		MonitorInfo info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
			this.setInputData(info);
		} else {
			// 変更の場合、情報取得
			try {
				info = MonitorSettingEndpointWrapper.getWrapper(managerName).getMonitor(this.monitorId);
				this.setInputData(info);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				// 上記以外の例外
				logger.warn("customizeDialog() getMonitor, " + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());

			}
		}
	}

	/**
	 * 各項目に入力値を設定します。
	 * 
	 * @param monitor
	 *            設定値として用いる通知情報
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		m_truthValueInfo.setInputData(monitor);

		if (monitor.getPluginCheckInfo() != null &&
			monitor.getPluginCheckInfo().getMonitorPluginStringInfoList() != null &&
			!monitor.getPluginCheckInfo().getMonitorPluginStringInfoList().isEmpty()){
			
			CloudEndpoint endpoint = ClusterControlPlugin.getDefault().getHinemosManager(managerName).getEndpoint(CloudEndpoint.class);
			HRepository cloudRepository;
			try {
				cloudRepository = endpoint.getRepositoryByRole(m_monitorBasic.getOwnerRoleId());
			} catch (
					CloudManagerException
					| com.clustercontrol.ws.xcloud.InvalidRole_Exception
					| InvalidUserPass_Exception e) {
				logger.warn(e.getMessage(), e);
				throw new InvalidStateException(e.getMessage(), e);
			}

			try {
				FacilityTreeItem treeItem = RepositoryEndpointWrapper.getWrapper(managerName).getFacilityTree(m_monitorBasic.getOwnerRoleId());
				FacilityTreeItem wrapper = createTreeItemWrapper(cloudRepository, treeItem);
				CloudUtil.walkFacilityTree(wrapper, new CloudUtil.IFacilityTreeVisitor() {
					@Override
					public void visitTreeItem(FacilityTreeItem item) {
					}
				});
			} catch (
					HinemosUnknown_Exception
					| com.clustercontrol.ws.repository.InvalidRole_Exception
					| com.clustercontrol.ws.repository.InvalidUserPass_Exception e) {
				logger.warn(e.getMessage(), e);
				throw new InvalidStateException(e.getMessage(), e);
			}
			
			MonitorPluginStringInfo stringInfo = null; 
			for (MonitorPluginStringInfo s: monitor.getPluginCheckInfo().getMonitorPluginStringInfoList()) {
				if ("targets".equals(s.getKey())) {
					stringInfo = s;
					break;
				}
			}
			
			if (stringInfo == null)
				throw new InvalidStateException();
			
			String facilityId = monitor.getFacilityId();
			changeSelection(facilityId);
			
			for (PlatformServiceCondition c: serviceMap.values()) {
				if (c.getId().equals(stringInfo.getValue())) {
					int selected = targetCombo.indexOf(c.getServiceName());
					if (selected != -1) {
						targetCombo.select(selected);
					}
					break;
				}
			}
		}
	}
	
	protected FacilityTreeItem createTreeItemWrapper(final HRepository cloudRepository, final FacilityTreeItem treeItem) {
		hScopeMap.clear();
		return  new FacilityTreeItem() {
			public List<FacilityTreeItem> getChildren() {
				if (children == null) {
					children = new ArrayList<>();
					
					List<FacilityTreeItem> items = new ArrayList<FacilityTreeItem>(treeItem.getChildren());
					Iterator<Object> hFacilityIter = new ArrayList<>(cloudRepository.getFacilities()).iterator();
					while(hFacilityIter.hasNext()) {
						HFacility f = (HFacility)hFacilityIter.next();
						if (f instanceof HCloudScopeRootScope) {
							Iterator<FacilityTreeItem> itemIter = items.iterator();
							while(itemIter.hasNext()) {
								FacilityTreeItem i = (FacilityTreeItem)itemIter.next();
								if (f.getId().equals(i.getData().getFacilityId())) {
									FacilityTreeItem wrapper = new FacilityTreeItemWrapper((HScope)f, i);
									wrapper.setParent(this);
									children.add(wrapper);
									hFacilityIter.remove();
									itemIter.remove();
									break;
								}
							}
						}
					}
				}
				return children;
			}

			public FacilityInfo getData() {
				return treeItem.getData();
			}
		};
	}
	
	protected void changeSelection(String facilityId) {
		if (currentFacilityId != null) {
			String selectedService = targetCombo.getText();
			if (selectedService != null && !selectedService.isEmpty()) {
				selectedServiceMap.put(currentFacilityId, selectedService);
			}
		}

		List<PlatformServiceCondition> conditions = Collections.emptyList();
		try {
			IHinemosManager manager = ClusterControlPlugin.getDefault().getHinemosManager(managerName);
			HScope selectedScope = hScopeMap.get(facilityId);
			if (selectedScope instanceof HCloudScopeScope) {
				ICloudPlatform p = manager.getCloudPlatform(((CloudScope)((HCloudScopeScope)selectedScope).getCloudScope()).getPlatformId());
				if (p.getCloudSpec().isCloudServiceMonitorEnabled())
					conditions = manager.getEndpoint(CloudEndpoint.class).getPlatformServiceConditionsByRole(((CloudScope)((HCloudScopeScope)selectedScope).getCloudScope()).getId(), m_monitorBasic.getOwnerRoleId());
			} else if (selectedScope instanceof HLocationScope) {
				HLocationScope location = (HLocationScope)selectedScope;
				ICloudPlatform p = manager.getCloudPlatform(((CloudScope)((HCloudScopeScope)location.getParent()).getCloudScope()).getPlatformId());
				if (p.getCloudSpec().isCloudServiceMonitorEnabled())
					conditions = manager.getEndpoint(CloudEndpoint.class).getPlatformServiceConditionsByLocationAndRole(((CloudScope)((HCloudScopeScope)location.getParent()).getCloudScope()).getId(), location.getLocation().getId(), m_monitorBasic.getOwnerRoleId());
			}
		} catch (
				CloudManagerException
				| com.clustercontrol.ws.xcloud.InvalidRole_Exception
				| InvalidUserPass_Exception e1) {
			throw new CloudModelException(e1.getMessage(), e1);
		}
		
		serviceMap.clear();
		targetCombo.removeAll();
		currentFacilityId = facilityId;
		for (PlatformServiceCondition condition: conditions) {
			targetCombo.add(condition.getServiceName());
			serviceMap.put(condition.getServiceName(), condition);
		}
		if (!conditions.isEmpty()) {
			String selectedService = selectedServiceMap.get(currentFacilityId);
			if (selectedService != null) {
				int selected = targetCombo.indexOf(selectedService);
				if (selected != -1) {
					targetCombo.select(selected);
				}
			} else {
				targetCombo.select(0);
			}
		}
	}
	
	/**
	 * 入力値を用いて通知情報を生成します。
	 * 
	 * @return 入力値を保持した通知情報
	 */
	@Override
	protected MonitorInfo createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// 監視固有情報を設定
		monitorInfo.setMonitorTypeId(CloudServiceMonitorPlugin.monitorPluginId);

		validateResult = m_truthValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}


		// 通知関連情報とアプリケーションの設定
		// 通知グループIDの設定
		//		monitorInfo.setNotifyGroupId(NotifyGroupIdGenerator.createNotifyGroupId(CloudMonitor.monitorPluginId, monitorInfo.getMonitorId()));
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if(validateResult.getID() == null){	// 通知ID警告用出力
				if(!displayQuestion(validateResult)){
					validateResult = null;
					return null;
				}
			}
			else{	// アプリケーション未入力チェック
				return null;
			}
		}

		return monitorInfo;
	}

	/**
	 * 入力値をマネージャに登録します。
	 * 
	 * @return true：正常、false：異常
	 * 
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		MonitorInfo info = this.inputData;

		if(targetCombo.getSelectionIndex() == -1){
			MessageDialog.openInformation(
					null,
					Messages.getString("failed"),
					msgSelectTarget);
			return false;
		}

		{
			PluginCheckInfo pluginCheckInfo = new PluginCheckInfo();
			pluginCheckInfo.setMonitorId(info.getMonitorId());
			pluginCheckInfo.setMonitorTypeId(info.getMonitorTypeId());
			List<MonitorPluginStringInfo> stringInfos = pluginCheckInfo.getMonitorPluginStringInfoList();
			MonitorPluginStringInfo stringInfo = new MonitorPluginStringInfo();
			stringInfo.setMonitorId(info.getMonitorId());
			stringInfo.setKey("targets");
			stringInfo.setValue(serviceMap.get(targetCombo.getText()).getId());
			stringInfo.setMonitorId(info.getMonitorId());
			stringInfos.add(stringInfo);
			info.setPluginCheckInfo(pluginCheckInfo);
		}

		String[] args = { info.getMonitorId(), managerName };
		if(!this.updateFlg){
			// 作成の場合
			try {
				result = MonitorSettingEndpointWrapper.getWrapper(managerName).addMonitor(info);

				if(result){
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
				} else {
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args));
				}
			} catch (MonitorDuplicate_Exception e) {
				// 監視項目IDが重複している場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.monitor.53", args));
			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole_Exception) {
					// アクセス権なしの場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					errMessage = ", " + e.getMessage();
				}

				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.monitor.34", args) + errMessage);
			}
		} else {
			// 変更の場合
			String errMessage = "";
			try {
				result = MonitorSettingEndpointWrapper.getWrapper(managerName).modifyMonitor(info);
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				errMessage = ", " + e.getMessage();
			}

			if(result) {
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.monitor.35", args));
			} else {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.monitor.36", args) + errMessage);
			}
		}

		return result;
	}
}
