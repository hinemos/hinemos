/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.openapitools.client.model.MapAssociationInfoResponse;
import org.openapitools.client.model.MapAssociationInfoResponse.TypeEnum;
import org.openapitools.client.model.ScopeInfoResponseP1;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NodeMapElementNoPrivilege;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.nodemap.composite.NodeMapCanvasComposite;
import com.clustercontrol.nodemap.composite.NodeMapListComposite;
import com.clustercontrol.nodemap.editpart.MapViewController;
import com.clustercontrol.nodemap.preference.NodeMapPreferencePage;
import com.clustercontrol.nodemap.util.SecondaryIdMap;
import com.clustercontrol.nodemap.view.action.GetL2ConnectionAction;
import com.clustercontrol.nodemap.view.action.GetL3ConnectionAction;
import com.clustercontrol.nodemap.view.action.RegisterMapAction;
import com.clustercontrol.nodemap.view.action.SetEditModeAction;
import com.clustercontrol.nodemap.view.action.SetFixedModeAction;
import com.clustercontrol.nodemap.view.action.SetFloatingModeAction;
import com.clustercontrol.nodemap.view.action.SetListModeAction;
import com.clustercontrol.nodemap.view.action.UploadImageAction;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.view.AutoUpdateView;

/**
 * ノードマップビューを描画するためのクラス。
 * コントロール部分はMapViewController
 * @since 1.0.0
 */
public class NodeMapView extends AutoUpdateView {

	// ログ
	private static Log m_log = LogFactory.getLog( NodeMapView.class );

	public static final String ID = NodeMapView.class.getName();

	private CCombo m_ccombo;
	private Composite stackComposite;
	public NodeMapCanvasComposite m_canvasComposite;
	
	public NodeMapListComposite m_listComposite;

	private MapViewController m_controller;

	public String secondaryId;
	
	private boolean adjust = false;

	/*
	 * アイコンの背景色の判断基準はデフォルトではステータスのみ。
	 * つまり、statusFlg=true, eventFlg=falseとなる。
	 */
	private boolean statusFlg;
	private boolean eventFlg;

	/**
	 * NodeMapViewのモード定義
	 * LIST_MODE            : リストモード(ノードをリストで表示するモード)
	 * FIXED_MODE           : 編集不可モード
	 * FLOATING_MODE        : アイコン移動可能モード
	 * EDIT_CONNECTION_MODE : コネクション編集可能モード
	 */
	public static enum Mode {
		LIST_MODE,
		FIXED_MODE,
		FLOATING_MODE,
		EDIT_CONNECTION_MODE,
	};

	// ビューのモードを保持
	public Mode _mode = Mode.FIXED_MODE;

	private boolean editing = false;

	public NodeMapView(){
		m_log.debug("nodemapview constructor");
	}

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(1, false));

		// パス文字列表示コンポジットを作成
		Composite pathComposite = new Composite(parent, SWT.NONE);

		// パス文字列表示コンポジットがビューの大きさにあわせて可変になるように設定
		GridData pathGridData = new GridData();
		pathGridData.horizontalAlignment = GridData.FILL;
		pathGridData.verticalAlignment = GridData.FILL;
		pathComposite.setLayoutData(pathGridData);

		// コンポジット内のレイアウトを設定
		pathComposite.setLayout(new FormLayout());

		// ラベルを設定
		Label label = new Label(pathComposite, SWT.NONE);
		label.setText(Messages.getString("scope") + " : ");
		FormData labelData = new FormData();  // ラベルの位置設定
		labelData.top  = new FormAttachment(0, 0); // コンポジット上側に詰める
		labelData.left = new FormAttachment(0, 0);  // コンポジットの左側に詰める
		label.setLayoutData(labelData);

		// コンボボックスを生成
		m_ccombo = new CCombo(pathComposite, SWT.BORDER);
		FormData comboData = new FormData();  // コンボボックスの位置設定
		comboData.top  = new FormAttachment(0, 0);    // コンポジットの上側に詰める
		comboData.left = new FormAttachment(label, 0); // ラベルの右側に詰める
		comboData.right = new FormAttachment(100, 0); // コンポジットの右側に詰める
		m_ccombo.setLayoutData(comboData);
		m_ccombo.setEditable(false); // 編集不可とする
		m_ccombo.setBackground(ColorConstantsWrapper.white());
		// コンボボックス選択時のアクションを設定
		m_ccombo.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				CCombo combo = (CCombo)e.getSource();
				String item = combo.getItem(combo.getSelectionIndex());
				String facilityId = (String)combo.getData(item);
				// クライアント側のキャッシュを有効にして選択したファシリティIDのマップを描画
				// 選択されたファシリティIDのスコープに遷移
				// スコープの存在確認
				ScopeInfoResponseP1 scopeInfo = null;
				try {
					RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(m_canvasComposite.getManagerName());
					scopeInfo = wrapper.getScope(facilityId);
				} catch (Exception ex) {
					// スコープ情報取得エラー
					String errMessage = "";
					if (ex instanceof InvalidRole) {
						errMessage = Messages.getString("message.accesscontrol.16");
					} else {
						errMessage = ex.getMessage();
					}
					m_log.error("createPartControl() : "
							+ "Failed to get the list of Scope information, "
							+ errMessage
							, ex);
				}
				if (scopeInfo == null) {
					// スコープが存在しない場合
					// 履歴より対象スコープを削除
					combo.remove(combo.getSelectionIndex());
					// 履歴選択前のスコープのノードマップを表示
					String beforeFacilityId = SecondaryIdMap.getFacilityId(secondaryId);
					int beforeIndex = 0;
					for(int i=0; i<combo.getItemCount(); i++) {
						if (beforeFacilityId.equals(combo.getData(combo.getItem(i)))) {
							beforeIndex = i;
							break;
						}
					}
					combo.select(beforeIndex);
				} else {
					// 編集中の情報を解除してよいか確認
					if (isEditing()) {
						if (!MessageDialog.openQuestion(
								null,
								Messages.getString("confirm"),
								com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
							return;
						}
					}
					
					// スコープが存在する場合は対象スコープのノードマップを表示
					updateView(facilityId);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				m_log.debug(e.toString());
			}
		});

		stackComposite = new Composite(parent, SWT.NONE);
		stackComposite.setLayout(new StackLayout());

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		stackComposite.setLayoutData(gridData);

		initSubComposite();

		// 初期表示はマップ。
		setMode(Mode.FIXED_MODE);
		
		// このビューのSecondaryIdを取得する
		IViewSite viewSite = getViewSite();
		if(viewSite != null){
			// ファシリティIDとSecondaryIdの対応マップから、
			// 今新規に生成しようとしているViewのファシリティIDを特定する
			secondaryId = viewSite.getSecondaryId();
			String facilityId = SecondaryIdMap.getFacilityId(secondaryId);
			String managerName = SecondaryIdMap.getManagerName(secondaryId);
			m_log.debug("createPartControl updateView " +
					"secondaryId= " + secondaryId + ", managerName=" + managerName + ", facilityId=" + facilityId);
			
			m_canvasComposite.setManagerName(managerName);
			// 指定のファシリティIDのスコープのマップを表示
			updateView(facilityId);
		} else {
			m_log.warn("createPartControl(), createPartControl logic error, viewSite is null");
		}
		
		getSite().getPage().addPartListener(new IPartListener() {
			
			@Override
			public void partOpened(IWorkbenchPart part) {
			}
			
			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}
			
			@Override
			public void partClosed(IWorkbenchPart part) {
			}
			
			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
			}
			
			@Override
			public void partActivated(IWorkbenchPart part) {
				setEnabledActionAll();
			}
		});
		
		applySetting();
		update(false);
	}

	private void initSubComposite() {
		m_listComposite = new NodeMapListComposite(stackComposite, SWT.BORDER, this);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		m_listComposite.setLayoutData(gridData);

		m_canvasComposite = new NodeMapCanvasComposite(stackComposite, SWT.BORDER|SWT.DOUBLE_BUFFERED, this);
	}

	private void showCanvasComposite() {
		m_listComposite.setVisible(false);
		m_canvasComposite.setVisible(true);
		((StackLayout)stackComposite.getLayout()).topControl = m_canvasComposite;
	}

	private void showNodeListComposite() {
		m_canvasComposite.setVisible(false);
		m_listComposite.setVisible(true);
		((StackLayout)stackComposite.getLayout()).topControl = m_listComposite;
	}

	public void applySetting() {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		statusFlg = store.getBoolean(NodeMapPreferencePage.P_ICON_BG_STATUS_FLG);
		eventFlg = store.getBoolean(NodeMapPreferencePage.P_ICON_BG_EVENT_FLG);
		int cycle = store.getInt(NodeMapPreferencePage.P_HISTORY_UPDATE_CYCLE);
		boolean flag = store.getBoolean(NodeMapPreferencePage.P_HISTORY_UPDATE_FLG);
		m_log.info("applySetting : cycle=" + cycle + ", flag=" + flag +
				", status=" + statusFlg + ", event=" + eventFlg);
		this.setInterval(cycle);
		if (flag) {
			this.startAutoReload();
		} else {
			this.stopAutoReload();
		}
		m_canvasComposite.applySetting();
	}

	@Override
	public void setFocus() {
		super.setFocus();
		if (m_canvasComposite != null) {
			m_canvasComposite.setCanvasFocus();
		}
		m_log.debug("id:"+getViewSite().getId()+",secondary:"+getViewSite().getSecondaryId());
		m_log.debug("setFocus end");
	}
	

	public void setEnabledActionAll() {
		//ビューアクションの使用可/不可を設定
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(GetL2ConnectionAction.ID, null);
			service.refreshElements(GetL3ConnectionAction.ID, null);
			service.refreshElements(RegisterMapAction.ID, null);
			service.refreshElements(SetEditModeAction.ID, null);
			service.refreshElements(SetFixedModeAction.ID, null);
			service.refreshElements(SetFloatingModeAction.ID, null);
			service.refreshElements(SetListModeAction.ID, null);
			service.refreshElements(UploadImageAction.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}
	
	// マップ名を設定する
	public void setMapName(String name, String mapId) {
		String[] items = m_ccombo.getItems();

		// 表示するマップ名が既に登録されている場合はその名前を選択した状態にする
		for(int i=0; i<items.length; i++){
			String facilityId = (String)m_ccombo.getData(items[i]);
			if(mapId.equals(facilityId)){
				if (name.equals(items[i])) {
					// マップ名、ファシリティIDが一致する場合はその項目を選択した状態にする。
					m_ccombo.select(i);
					return;
				} else {
					// マップ名に変更がある場合は削除し、追加（変更するとSecondaryIdMapで不整合を起こすため）
					m_ccombo.remove(i);
					break;
				}
			}
		}

		// 登録されていない場合はコンボボックスの一番上に追加し選択
		m_ccombo.add(name, 0);
		m_ccombo.setData(name, mapId);
		m_ccombo.select(0);
	}

	/**
	 * 描画対象スコープのファシリティIDを設定し、ビューの内のマップを更新
	 */
	public void updateView(String facilityId){
		m_log.debug("@@updateView() " + facilityId);

		String oldFacilityId = null;
		if (m_controller != null) {
			oldFacilityId = m_controller.getCurrentScope();
		}
		
		if (m_canvasComposite == null) {
			m_log.info("m_canvasComposit is null");
			return;
		}

		// 描画対象スコープのFacilityIDからビューのSecondaryIdを特定できるよう、
		// FacilityIDをキーにSecondaryIdを登録する
		SecondaryIdMap.putSecondaryId(secondaryId, m_canvasComposite.getManagerName(), facilityId, NodeMapView.class);

		m_log.debug("put " + secondaryId + ", " + facilityId);

		// 描画対象スコープのFacilityIdを定義してコントローラを作成
		m_controller = new MapViewController(this, secondaryId, facilityId);

		// マネージャからマップ情報を取得して描画
		m_canvasComposite.setController(m_controller);
		boolean result = reload();
		if (!result) {
			// アップデート失敗の場合は、ひとつ前のスコープ表示に戻す
			if (oldFacilityId != null) {
				m_log.info("updateView(), rollback " + facilityId + "->" + oldFacilityId);
				SecondaryIdMap.putSecondaryId(secondaryId, m_canvasComposite.getManagerName(), oldFacilityId, NodeMapView.class);
				m_controller = new MapViewController(this, secondaryId, oldFacilityId);
				m_canvasComposite.setController(m_controller);
				reload();
			}
		}
		if (m_listComposite != null) {
			m_listComposite.setController(m_controller);
		} else {
			m_log.info("m_listComposite is null");
		}

		setViewTabName(false);
	}
	
	public void setViewTabName(boolean editing) {
		// ビューのタブ表示を設定
		String edit = "";
		if (editing) {
			edit += com.clustercontrol.nodemap.messages.Messages.getString("editing");
		}

		String mapName = HinemosMessage.replace(m_controller.getMapName());
		if (mapName == null || mapName.equals(FacilityConstant.STRING_COMPOSITE)) {
			mapName = com.clustercontrol.nodemap.messages.Messages.getString("view.nodemap");
		}
		setPartName(edit + mapName + "("+ m_controller.getManagerName() +")");
	}

	/**
	 * 現在選択されているマップの親のマップでビューを更新します
	 */
	public void upward() {
		// トップの場合はさらに上位には遷移しない
		if(!ReservedFacilityIdConstant.ROOT_SCOPE.equals(m_controller.getCurrentScope())){
			
			// 編集中の情報を解除してよいか確認
			if (isEditing()) {
				if (!MessageDialog.openQuestion(
						null,
						Messages.getString("confirm"),
						com.clustercontrol.nodemap.messages.Messages.getString("edit.refresh.confirm"))) {
					return;
				}
			}
			
			updateView(m_controller.getParent());
		}
	}

	public void registerNodeMap() throws Exception {
		// 削除予定の関連を削除してからマネージャに登録する
		List<MapAssociationInfoResponse> delAssoList = new ArrayList<MapAssociationInfoResponse>();
		List<MapAssociationInfoResponse> addAssoList = new ArrayList<MapAssociationInfoResponse>();
		for (MapAssociationInfoResponse asso : m_controller.getAssociation()) {
			if (asso.getType() == TypeEnum.REMOVE) {
				delAssoList.add(asso);
			} else if (asso.getType() == TypeEnum.NEW) {
				delAssoList.add(asso);
				addAssoList.add(asso);
			}
		}
		for (MapAssociationInfoResponse asso : delAssoList) {
			m_controller.removeAssociation(asso);
		}
		for (MapAssociationInfoResponse asso : addAssoList) {
			m_controller.addAssociation(asso.getSource(), asso.getTarget());
		}
		m_controller.registerNodeMap();
	}

	/*
	 * ジョブマップ同様ズーム後の再描画をおこなうメソッド
	 */
	public void updateNotManagerAccess() {
		m_canvasComposite.updateNotManagerAccess();
	}
	
	/**
	 * 自動更新用コールバックメソッド
	 * 定期的に呼び出される
	 * 閲覧モード(Mode.FIXED_MODE),リスト表示モードの時だけreloadを実行する。
	 */
	@Override
	public void update(boolean refleshFlg) {
		m_log.debug("nodemap update");
		// setMode(m_canvasComposite.getMode());
		if (_mode == Mode.FIXED_MODE || _mode == Mode.LIST_MODE) {
			reload();
		}
	}
	
	public NodeMapCanvasComposite getCanvasComposite() {
		return m_canvasComposite;
	}

	/**
	 * マネージャから情報を取得し画面を再描画する
	 */
	public boolean reload() {
		long start = System.currentTimeMillis();
		try {
			m_controller.updateMap(statusFlg, eventFlg);
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			String errMsg = Messages.getString("message.accesscontrol.16");
			m_canvasComposite.clearCanvas();
			m_canvasComposite.setErrorMessage(errMsg);
			return false;
		} catch (Exception e) {
			m_canvasComposite.clearCanvas();
			String errMsg = "";
			if (e instanceof RestConnectFailed) {
				m_log.debug("update() updateMap, " + e.getMessage());
				errMsg = Messages.getString("message.hinemos.failure.transfer") + ", " + e.getMessage();
			} else if (e instanceof NodeMapElementNoPrivilege) {
				m_log.debug("update() updateMap, " + e.getMessage());
				errMsg = Messages.getString("nodemap.element.noprivilege") + ", " + e.getMessage();
			} else {
				m_log.warn("update() updateMap, " + e.getMessage(), e);
				errMsg = Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage();
			}
			m_canvasComposite.setErrorMessage(errMsg);
			return false;
		}
		// マネージャからマップ情報を取得して描画
		if (_mode == Mode.LIST_MODE) {
			m_listComposite.update();
		} else {
			m_canvasComposite.update();
		}
		
		long end = System.currentTimeMillis();
		m_log.debug("reload() :" + (end - start) + "ms");
		return true;
	}

	public boolean isEditing() {
		return editing;
	}
	
	public void setEditing(boolean editing) {
		this.editing = editing;
		setViewTabName(this.editing);
	}
	
	/**
	 * disposeの際にm_secondaryIdMapを更新する
	 */
	@Override
	public void dispose(){
		m_log.info("call dispose(), secondaryId="+secondaryId+",facilityId="+SecondaryIdMap.getFacilityId(secondaryId));

		SecondaryIdMap.removeSecondaryId(secondaryId);

		super.dispose();
	}

	/**
	 * モード設定
	 * @param mode
	 */
	public void setMode(Mode mode){
		this._mode = mode;
		
		setEnabledActionAll();

		if (mode == Mode.LIST_MODE) {
			showNodeListComposite();
		} else {
			showCanvasComposite();
		}
	}

	/**
	 * モード取得
	 * @return
	 */
	public Mode getMode() {
		return _mode;
	}
	
	public boolean isEditableMode() {
		if (_mode == Mode.FLOATING_MODE || _mode == Mode.EDIT_CONNECTION_MODE) {
			return true;
		}
		
		return false;
	}
	
	public boolean isAdjust() {
		return adjust;
	}
	
	public void setAdjust(boolean adjust) {
		this.adjust = adjust;
	}
	
	public void zoomOut() {
		m_canvasComposite.zoomOut();
	}

	public void zoomIn() {
		m_canvasComposite.zoomIn();
	}

	@Override
	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * Controller取得
	 * 
	 * @return Controller
	 */
	public MapViewController getController() {
		return this.m_controller;
	}

	public void setEnabled(boolean enable) {
		m_canvasComposite.setEnabled(enable);
	}
}