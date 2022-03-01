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
import org.openapitools.client.model.AddCloudserviceMonitorRequest;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.HFacilityResponse;
import org.openapitools.client.model.HFacilityResponse.TypeEnum;
import org.openapitools.client.model.HRepositoryResponse;
import org.openapitools.client.model.ModifyCloudserviceMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorPluginStringInfoResponse;
import org.openapitools.client.model.MonitorTruthValueInfoRequest;
import org.openapitools.client.model.PlatformServiceConditionResponse;
import org.openapitools.client.model.PluginCheckInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorDuplicate;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite;
import com.clustercontrol.monitor.run.composite.MonitorRuleComposite;
import com.clustercontrol.monitor.run.composite.TruthValueInfoComposite;
import com.clustercontrol.monitor.run.dialog.CommonMonitorTruthDialog;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.notify.bean.PriChangeFailSelectTypeConstant;
import com.clustercontrol.notify.composite.NotifyInfoComposite;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.InvalidStateException;
import com.clustercontrol.xcloud.model.cloud.ICloudPlatform;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.ControlUtil;

/**
 * クラウドサービス監視作成・変更ダイアログクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.0.0
 */
public class CloudServiceCreateDialog extends CommonMonitorTruthDialog implements CloudStringConstants {
	public static final long serialVersionUID = 1L;
	public class FacilityTreeItemWrapper extends FacilityTreeItemResponse {
		private HFacilityResponse scope;
		private FacilityTreeItemResponse item;

		public FacilityTreeItemWrapper(HFacilityResponse scope, FacilityTreeItemResponse item) {
			this.scope = scope;
			this.item = item;
		}

		public List<FacilityTreeItemResponse> getChildren() {
			if (super.getChildren().isEmpty()) {
				List<FacilityTreeItemResponse> items = new ArrayList<FacilityTreeItemResponse>(item.getChildren());
				Iterator<HFacilityResponse> hFacilityIter = new ArrayList<>(scope.getFacilities()).iterator();
				while(hFacilityIter.hasNext()) {
					HFacilityResponse f = hFacilityIter.next();
					if (f.getType() == TypeEnum.CLOUDSCOPE || f.getType() == TypeEnum.LOCATION) {
						Iterator<FacilityTreeItemResponse> itemIter = items.iterator();
						while(itemIter.hasNext()) {
							FacilityTreeItemResponse i = (FacilityTreeItemResponse)itemIter.next();
							if (f.getId().equals(i.getData().getFacilityId())) {
								hScopeMap.put(f.getId(), f);
								
								FacilityTreeItemResponse wrapper = new FacilityTreeItemWrapper(f, i);
								wrapper.setParent(this);
								super.getChildren().add(wrapper);
								hFacilityIter.remove();
								itemIter.remove();
								break;
							}
						}
					}
				}
			}
			return super.getChildren();
		}
		
		public FacilityInfoResponse getData() {
			return item.getData();
		}
		
		public HFacilityResponse getHScope() {
			return scope;
		}
	};

	private String msgSelectTarget = bundle_messages.getString("message.must_select_service");

	// ログ
	//	private static Log logger = LogFactory.getLog( AgentCreateDialog.class );
	private static final Log logger = LogFactory.getLog(CloudServiceCreateDialog.class);

	private Combo targetCombo;

	private String currentFacilityId;
	private Map<String, HFacilityResponse> hScopeMap = new HashMap<>();
	private Map<String, PlatformServiceConditionResponse> serviceMap = new HashMap<>();
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
		this.priorityChangeFailSelect = PriChangeFailSelectTypeConstant.TYPE_GET;
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
		this.priorityChangeFailSelect = PriChangeFailSelectTypeConstant.TYPE_GET;
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
					// エンドポイントがPublishされていることを確認
					ValidateResult result = validateEndpoint(CloudServiceCreateDialog.this.managerName);
					if (result != null) {
						displayError(result);
						return;
					}
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
										final CloudRestClientWrapper wrapper = ClusterControlPlugin.getDefault().getHinemosManager(managerName).getWrapper();
										HRepositoryResponse cloudRepository;
										try {
											cloudRepository = wrapper.getRepository(m_monitorBasic.getOwnerRoleId());
										} catch (CloudManagerException |InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
											logger.warn(e.getMessage(), e);
											throw new InvalidStateException(e.getMessage(), e);
										}
										
										FacilityTreeItemResponse treeItem;
										try {
											treeItem = RepositoryRestClientWrapper.getWrapper(managerName).getFacilityTree(m_monitorBasic.getOwnerRoleId());
											if (treeItem != null && treeItem.getChildren() != null && treeItem.getChildren().get(0) != null) {
												Collections.sort(treeItem.getChildren().get(0).getChildren(), new Comparator<FacilityTreeItemResponse>() {
													@Override
													public int compare(FacilityTreeItemResponse o1, FacilityTreeItemResponse o2) {
														FacilityInfoResponse info1 = ((FacilityTreeItemResponse) o1).getData();
														FacilityInfoResponse info2 = ((FacilityTreeItemResponse) o2).getData();
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
										} catch (HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
											logger.warn(e.getMessage(), e);
											throw new InvalidStateException(e.getMessage(), e);
										}
										
										final FacilityTreeItemResponse treeItemWrapper = addEmptyParent(createTreeItemWrapper(cloudRepository, treeItem));

										if(!this.isDisposed()){
											logger.trace("FacilityTreeComposite.checkAsyncExec() is true");
											getDisplay().asyncExec(new Runnable(){
												@Override
												public void run() {
													logger.trace("FacilityTreeComposite.checkAsyncExec() do runnnable");

													FacilityTreeItemResponse oldTreeItem = (FacilityTreeItemResponse)treeViewer.getInput();
													logger.debug("run() oldTreeItem=" + oldTreeItem);
													if( null != oldTreeItem ){
														if (!oldTreeItem.equals(treeItemWrapper)) {
															ArrayList<String> expandIdList = new ArrayList<String>();
															for (Object item : treeViewer.getExpandedElements()) {
																expandIdList.add(((FacilityTreeItemResponse)item).getData().getFacilityId());
															}
															treeViewer.setInput(treeItemWrapper);
															treeViewer.refresh();
															expand(treeItemWrapper, expandIdList);
														}
													}else{
														logger.info("oldTreeItem is null");
														treeViewer.setInput(treeItemWrapper);
														List<FacilityTreeItemResponse> selectItem = treeItemWrapper.getChildren();
														if (!selectItem.isEmpty()) {
															treeViewer.setSelection(new StructuredSelection(selectItem.get(0)), true);
															//スコープのレベルまで展開
															treeViewer.expandToLevel(4);
														}
													}
												}

												private void expand(FacilityTreeItemResponse item, List<String> expandIdList) {
													if (expandIdList.contains(item.getData().getFacilityId())) {
														treeViewer.expandToLevel(item, 1);
													}
													for (FacilityTreeItemResponse child : item.getChildren()) {
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
						
						@Override
						protected ValidateResult validate() {
							ValidateResult result = null;
							FacilityTreeItemResponse item = this.getSelectItem();

							// ノード・スコープが選択可能な場合
							if (item == null
									|| item.getData().getNotReferFlg()
									|| item.getData().getFacilityType() == FacilityTypeEnum.COMPOSITE
									|| item.getData().getFacilityType() == FacilityTypeEnum.MANAGER) {
								// 未選択の場合エラー
								// 参照不可のスコープを選択した場合はエラー
								// ルートを選択した場合はエラー
								result = new ValidateResult();
								result.setValid(false);
								result.setID(Messages.getString("message.hinemos.1"));
								result.setMessage(Messages.getString("message.repository.47"));
							} else if (item instanceof FacilityTreeItemWrapper) {
								FacilityTreeItemWrapper wrapper = (FacilityTreeItemWrapper)item;
								if (wrapper.getHScope() != null && wrapper.getHScope().getType() == TypeEnum.ROOT) {
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
						FacilityTreeItemResponse item = dialog.getSelectItem();
						FacilityInfoResponse info = item.getData();
						m_facilityId.set(m_monitorBasic, info.getFacilityId());
						
						FacilityPath path = new FacilityPath(
								ClusterControlPlugin.getDefault()
								.getSeparator());
						((Text)m_textScope.get(m_monitorBasic)).setText(path.getPath(item));
						
						changeSelection(info.getFacilityId());
					}
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
					logger.warn(e1);
				} catch (InvalidStateException e2){
					// クラウド仮想化のアクセス権がない場合など
					// クラウドスコープの取得に失敗した場合はダイアログを出す
					MessageDialog.openInformation(null, Messages.getString("message"), e2.getMessage());
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
		this.m_notifyInfo = new NotifyInfoComposite(groupNotifyAttribute, SWT.NONE,priorityChangeJudgeSelect ,priorityChangeFailSelect);
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
		MonitorInfoResponse info = null;
		if(this.monitorId == null){
			// 作成の場合
			info = new MonitorInfoResponse();
			this.setInfoInitialValue(info);
			this.setInputData(info);
		} else {
			// 変更の場合、情報取得
			try {
				info = MonitorsettingRestClientWrapper.getWrapper(managerName).getMonitor(this.monitorId);
				this.setInputData(info);
			} catch (InvalidRole e) {
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
	protected void setInputData(MonitorInfoResponse monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		m_truthValueInfo.setInputData(monitor);

		if (monitor.getPluginCheckInfo() != null &&
			monitor.getPluginCheckInfo().getMonitorPluginStringInfoList() != null &&
			!monitor.getPluginCheckInfo().getMonitorPluginStringInfoList().isEmpty()){
			
			CloudRestClientWrapper wrapper = ClusterControlPlugin.getDefault().getHinemosManager(managerName).getWrapper();
			HRepositoryResponse cloudRepository;
			try {
				cloudRepository = wrapper.getRepository(m_monitorBasic.getOwnerRoleId());
			} catch (CloudManagerException |InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
				logger.warn(e.getMessage(), e);
				throw new InvalidStateException(e.getMessage(), e);
			}

			try {
				FacilityTreeItemResponse treeItem = RepositoryRestClientWrapper.getWrapper(managerName).getFacilityTree(m_monitorBasic.getOwnerRoleId());
				FacilityTreeItemResponse treeItemWrapper = createTreeItemWrapper(cloudRepository, treeItem);
				CloudUtil.walkFacilityTree(treeItemWrapper, new CloudUtil.IFacilityTreeVisitor() {
					@Override
					public void visitTreeItem(FacilityTreeItemResponse item) {
					}
				});
			} catch (HinemosUnknown | InvalidRole | InvalidUserPass | RestConnectFailed e) {
				logger.warn(e.getMessage(), e);
				throw new InvalidStateException(e.getMessage(), e);
			}
			
			MonitorPluginStringInfoResponse stringInfo = null; 
			for (MonitorPluginStringInfoResponse s: monitor.getPluginCheckInfo().getMonitorPluginStringInfoList()) {
				if ("targets".equals(s.getKey())) {
					stringInfo = s;
					break;
				}
			}
			
			if (stringInfo == null)
				throw new InvalidStateException();
			
			String facilityId = monitor.getFacilityId();
			changeSelection(facilityId);
			
			for (PlatformServiceConditionResponse c: serviceMap.values()) {
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
	
	protected FacilityTreeItemResponse createTreeItemWrapper(final HRepositoryResponse cloudRepository, final FacilityTreeItemResponse treeItem) {
		hScopeMap.clear();
		return  new FacilityTreeItemResponse() {
			public List<FacilityTreeItemResponse> getChildren() {
				if (super.getChildren().isEmpty()) {
					List<FacilityTreeItemResponse> items = new ArrayList<FacilityTreeItemResponse>(treeItem.getChildren());
					Iterator<HFacilityResponse> hFacilityIter = new ArrayList<>(cloudRepository.getFacilities()).iterator();
					while(hFacilityIter.hasNext()) {
						HFacilityResponse f = hFacilityIter.next();
						if (f.getType() == TypeEnum.ROOT) {
							Iterator<FacilityTreeItemResponse> itemIter = items.iterator();
							while(itemIter.hasNext()) {
								FacilityTreeItemResponse i = (FacilityTreeItemResponse)itemIter.next();
								if (f.getId().equals(i.getData().getFacilityId())) {
									FacilityTreeItemResponse wrapper = new FacilityTreeItemWrapper(f, i);
									wrapper.setParent(this);
									super.getChildren().add(wrapper);
									hFacilityIter.remove();
									itemIter.remove();
									break;
								}
							}
						}
					}
				}
				return super.getChildren();
			}

			public FacilityInfoResponse getData() {
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

		List<PlatformServiceConditionResponse> conditions = Collections.emptyList();
		try {
			IHinemosManager manager = ClusterControlPlugin.getDefault().getHinemosManager(managerName);
			HFacilityResponse selectedScope = hScopeMap.get(facilityId);
			if (selectedScope.getType() == TypeEnum.CLOUDSCOPE) {
				ICloudPlatform p = manager.getCloudPlatform(selectedScope.getCloudScope().getEntity().getPlatformId());
				if (p.getCloudSpec().getCloudServiceMonitorEnabled())
					conditions = manager.getWrapper().getPlatformServiceConditions(selectedScope.getCloudScope().getEntity().getCloudScopeId(), null, m_monitorBasic.getOwnerRoleId());
			} else if (selectedScope.getType() == TypeEnum.LOCATION) {
				ICloudPlatform p = manager.getCloudPlatform(selectedScope.getPlatformId());
				if (p.getCloudSpec().getCloudServiceMonitorEnabled())
					conditions = manager.getWrapper().getPlatformServiceConditions(selectedScope.getParentCloudScopeId(), selectedScope.getLocation().getId(), m_monitorBasic.getOwnerRoleId());
			}
		} catch (
				CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e1) {
			throw new CloudModelException(e1.getMessage(), e1);
		}
		
		serviceMap.clear();
		targetCombo.removeAll();
		currentFacilityId = facilityId;
		for (PlatformServiceConditionResponse condition: conditions) {
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
	protected MonitorInfoResponse createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		validateResult = m_truthValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}


		// 通知関連情報とアプリケーションの設定
		// 通知グループIDの設定
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

		if (this.inputData != null) {
			if(targetCombo.getSelectionIndex() == -1){
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						msgSelectTarget);
				return false;
			}
			PluginCheckInfoResponse pluginCheckInfo = new PluginCheckInfoResponse();
			List<MonitorPluginStringInfoResponse> stringInfos = pluginCheckInfo.getMonitorPluginStringInfoList();
			MonitorPluginStringInfoResponse stringInfo = new MonitorPluginStringInfoResponse();
			stringInfo.setKey("targets");
			stringInfo.setValue(serviceMap.get(targetCombo.getText()).getId());
			stringInfos.add(stringInfo);
			this.inputData.setPluginCheckInfo(pluginCheckInfo);

			String[] args = { this.inputData.getMonitorId(), getManagerName() };
			MonitorsettingRestClientWrapper wrapper = MonitorsettingRestClientWrapper.getWrapper(getManagerName());
			if(!this.updateFlg){
				// 作成の場合
				try {
					AddCloudserviceMonitorRequest info = new AddCloudserviceMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(AddCloudserviceMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getTruthValueInfo() != null
							&& this.inputData.getTruthValueInfo() != null) {
						for (int i = 0; i < info.getTruthValueInfo().size(); i++) {
							info.getTruthValueInfo().get(i).setPriority(MonitorTruthValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getPriority().getValue()));
							info.getTruthValueInfo().get(i).setTruthValue(MonitorTruthValueInfoRequest.TruthValueEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getTruthValue().getValue()));
						}
					}
					wrapper.addCloudserviceMonitor(info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
					result = true;
				} catch (MonitorDuplicate e) {
					// 監視項目IDが重複している場合、エラーダイアログを表示する
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
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
				try {
					ModifyCloudserviceMonitorRequest info = new ModifyCloudserviceMonitorRequest();
					RestClientBeanUtil.convertBean(this.inputData, info);
					info.setRunInterval(ModifyCloudserviceMonitorRequest.RunIntervalEnum.fromValue(this.inputData.getRunInterval().getValue()));
					if (info.getTruthValueInfo() != null
							&& this.inputData.getTruthValueInfo() != null) {
						for (int i = 0; i < info.getTruthValueInfo().size(); i++) {
							info.getTruthValueInfo().get(i).setPriority(MonitorTruthValueInfoRequest.PriorityEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getPriority().getValue()));
							info.getTruthValueInfo().get(i).setTruthValue(MonitorTruthValueInfoRequest.TruthValueEnum.fromValue(
									this.inputData.getTruthValueInfo().get(i).getTruthValue().getValue()));
						}
					}
					wrapper.modifyCloudserviceMonitor(this.inputData.getMonitorId(), info);
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
					result = true;
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole) {
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
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}

		return result;
	}

	@Override
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		return CloudRestClientWrapper.getWrapper(managerName);
	}
}
