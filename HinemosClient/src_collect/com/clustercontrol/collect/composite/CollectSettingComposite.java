/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.collect.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.bean.SummaryTypeMessage;
import com.clustercontrol.collect.util.CollectEndpointWrapper;
import com.clustercontrol.collect.view.CollectGraphView;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.collect.CollectKeyInfoPK;
import com.clustercontrol.ws.collect.HinemosUnknown_Exception;
import com.clustercontrol.ws.collect.InvalidRole_Exception;
import com.clustercontrol.ws.collect.InvalidUserPass_Exception;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 性能グラフを表示するコンポジットクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class CollectSettingComposite extends Composite {
	private static Log m_log = LogFactory.getLog(CollectSettingComposite.class);

	/**
	 * 
	 */
	private CollectGraphView m_collectGraphView;

	/** 監視項目種別のリスト */
	private org.eclipse.swt.widgets.List m_listCollectorItem = null;
	/** サマリータイプのコンボ */
	private Combo m_comboSummaryItem = null;
	/** グラフ種別のコンボ */
	private Combo m_comboGraphTypeItem = null;
	
	/** 折り返しチェック有り無し */
	private Button returnButton = null;
	/** 種別折り返しチェック有り無し */
	private Button returnKindButton = null;
	/** 近似曲線表示フラグ */
	private Button approximateButton = null;
	/** 閾値設定モードフラグ */
	private Button thresholdButton = null;
	/** 凡例表示フラグ */
	private Button legendButton = null;
	/** 監視項目IDの選択順番index保持用 */
	private List<Integer> selectIndexList = new ArrayList<>();
	/** [適用]ボタン */
	private Button apply = null;

	private static final int SUMMARY_CODE_INVALID = -1;
	
	/** 区切り文字(#) */
	protected static final String SEPARATOR_HASH = "#";
	/** 区切り文字(##@##) */
	protected static final String SEPARATOR_HASH_HASH_AT_HASH_HASH = "##@##";
	/** 区切り文字(#!#) */
	protected static final String SEPARATOR_HASH_EX_HASH = "#!#";
	/** 区切り文字(@) */
	protected static final String SEPARATOR_AT = "@";
	
	/** 画面サイズで折り返しフラグ */
	public static final String P_COLLECT_GRAPH_SIZE_RETURN_FLG = "collectGraphSizeReturnFlg";

	/** 種別で折り返しフラグ */
	public static final String P_COLLECT_GRAPH_KIND_RETURN_FLG = "collectGraphKindReturnFlg";

	/** グラフをまとめるフラグ */
	public static final String P_COLLECT_GRAPH_TOTAL_FLG = "collectGraphTotalFlg";

	/** グラフをまとめるフラグ */
	public static final String P_COLLECT_GRAPH_STACK_FLG = "collectGraphStackFlg";

	/** 近似線を出すかフラグ */
	public static final String P_COLLECT_GRAPH_APPROX_FLG = "collectGraphApproxFlg";

	/** 閾値モードフラグ */
	public static final String P_COLLECT_GRAPH_THRESHOLD_FLG = "collectGraphThresholdFlg";

	/** 凡例モードフラグ */
	public static final String P_COLLECT_GRAPH_LEGEND_FLG = "collectGraphLegendFlg";

	/** ツリー選択状態 */
	public static final String P_COLLECT_GRAPH_SELECT_NODE_INFO = "collectGraphSelectNodeInfo";

	/** グラフ拡大レベル */
	public static final String P_COLLECT_GRAPH_ZOOM_LEVEL = "collectGraphZoomLevel";

	/** 監視項目選択状態 */
	public static final String P_COLLECT_GRAPH_SELECT_ITEM_INFO = "collectGraphSelectItemInfo";

	/** サマリータイプ選択状態 */
	public static final String P_COLLECT_GRAPH_SELECT_SUMMARY_INFO = "collectGraphSelectSummaryInfo";

	/** グラフタイプ選択状態 */
	public static final String P_COLLECT_GRAPH_SELECT_TYPE_INFO = "collectGraphSelectTypeInfo";

	/** グラフ拡大レベル(デフォルト) */
	public static final String DEFAULT_COLLECT_GRAPH_ZOOM_LEVEL = "100%";

	/**
	 * コンストラクタ
	 *
	 * @param settings グラフのヘッダ情報
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 */
	public CollectSettingComposite(Composite parent, int style, CollectGraphView collectGraphView) {
		super(parent, style);
		// 初期化
		initialize();
		m_collectGraphView = collectGraphView;
		GridData gridData = null;
	
		Composite graphCompo = this;
		WidgetTestUtil.setTestId(this, "graphCompo", graphCompo);
		GridLayout layout = new GridLayout(1, true);
		graphCompo.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// itemCode
		Label itemCodeLabel = new Label(graphCompo, SWT.NONE | SWT.RIGHT);
		itemCodeLabel.setText(Messages.getString("collection.display.name") + " : ");
		// itemCodeのリスト
		m_listCollectorItem = new org.eclipse.swt.widgets.List(graphCompo, SWT.MULTI | SWT.V_SCROLL | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.NONE);
		WidgetTestUtil.setTestId(this, "collectorItem", this.m_listCollectorItem);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		gridData.heightHint = m_listCollectorItem.getItemHeight() * 100;
		gridData.grabExcessVerticalSpace = true;
		m_listCollectorItem.setLayoutData(gridData);
		
		// 収集値表示名を選択するたびのイベント
		m_listCollectorItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 監視項目種別IDの選択状態を保持する(選択順番はRCPはgetSelectionIndex、RAPはgetSelectionIndicesで取れる)
				if (ClusterControlPlugin.isRAP()) {
					selectIndexList.clear();
					for(int number : m_listCollectorItem.getSelectionIndices()) {
						selectIndexList.add(number);
					}
				} else {
					if (m_listCollectorItem.getSelectionIndices().length == 0) {
						// 選択無し
						selectIndexList.clear();
					} else if (selectIndexList.size() == m_listCollectorItem.getSelectionIndices().length 
							|| m_listCollectorItem.getSelectionIndices().length == 1) {
						// 選択を変えた(1つから1つ) or 多数選択から1つ選択に変えた
						// 1しかありえない
						selectIndexList.clear();
						selectIndexList.add(m_listCollectorItem.getSelectionIndex());
					} else if (selectIndexList.size() < m_listCollectorItem.getSelectionIndices().length) {
						// 選択を増やした
						// ctrl + a の可能性があるので未登録かどうかをチェック
						if (!selectIndexList.contains((Integer)m_listCollectorItem.getSelectionIndex())) {
							selectIndexList.add((Integer)m_listCollectorItem.getSelectionIndex());
						}
					} else {
						// 選択を減らした(1つ減らした)
						Integer unselectedIndex = 0;
						List<Integer> selectIndicesList = new ArrayList<>();
						for (int i = 0; i < m_listCollectorItem.getSelectionIndices().length; i++) {
							selectIndicesList.add(m_listCollectorItem.getSelectionIndices()[i]);
						}
						for (Integer number : selectIndexList) {
							if (!selectIndicesList.contains(number)) {
								unselectedIndex = number;
								break;
							}
						}
						selectIndexList.remove(unselectedIndex);
					}
				}
				m_log.debug("監視項目リストの選択順番(index):" + selectIndexList.toString());
			}
		});
		// グラフ種別
		Label graphTypeLabel = new Label(graphCompo, SWT.RIGHT | SWT.NONE);
		graphTypeLabel.setText(Messages.getString("collection.graph.graphkind") + " : ");
		
		m_comboGraphTypeItem = new Combo(graphCompo, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "summaryItem", m_comboGraphTypeItem);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		m_comboGraphTypeItem.setLayoutData(gridData);
		m_comboGraphTypeItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// グラフ種別によってチェックボックスの選択可否を設定する
				setCheckBoxEnableByGraphKind();
			}
		});
		
		// summaryType コンボボックス
		Label graphSummaryLabel = new Label(graphCompo, SWT.RIGHT | SWT.NONE);
		graphSummaryLabel.setText(Messages.getString("collection.summary.type") + " : ");
		
		m_comboSummaryItem = new Combo(graphCompo, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "summaryItem", m_comboSummaryItem);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		m_comboSummaryItem.setLayoutData(gridData);
		
		Composite checkCompsite = new Composite(graphCompo, SWT.NONE);
		checkCompsite.setLayout(new GridLayout(2, true));
		
		// 折り返しチェックボックス
		returnButton = new Button(checkCompsite, SWT.CHECK);
		returnButton.setText(Messages.getString("collection.graph.wordwrap"));// 右端で折り返し
		returnButton.setToolTipText(Messages.getString("collection.graph.wordwrap"));
		// preferenceの情報を取得
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();
		returnButton.setSelection(store.getBoolean(P_COLLECT_GRAPH_SIZE_RETURN_FLG));
		
		// 種別折り返しチェックボックス
		returnKindButton = new Button(checkCompsite, SWT.CHECK);
		returnKindButton.setText(Messages.getString("collection.graph.foldedinmonitorid")); // 種別で折り返し
		returnKindButton.setToolTipText(Messages.getString("collection.graph.foldedinmonitorid"));
		// preferenceの情報を取得
		returnKindButton.setSelection(store.getBoolean(P_COLLECT_GRAPH_KIND_RETURN_FLG));

		// 近似直線表示フラグのチェックボックス
		approximateButton = new Button(checkCompsite, SWT.CHECK);
		approximateButton.setText(Messages.getString("collection.graph.approximatestraightline")); // 近似直線表示
		approximateButton.setToolTipText(Messages.getString("collection.graph.approximatestraightline"));
		// preferenceの情報を取得
		approximateButton.setSelection(store.getBoolean(P_COLLECT_GRAPH_APPROX_FLG));
		
		// 閾値設定モードフラグのチェックボックス
		thresholdButton = new Button(checkCompsite, SWT.CHECK);
		thresholdButton.setText(Messages.getString("collection.graph.upperandlowerlimits")); // 上限下限表示
		thresholdButton.setToolTipText(Messages.getString("collection.graph.upperandlowerlimits"));
		// preferenceの情報を取得
		thresholdButton.setSelection(store.getBoolean(P_COLLECT_GRAPH_THRESHOLD_FLG));
		
		// 凡例表示モードフラグのチェックボックス
		legendButton = new Button(checkCompsite, SWT.CHECK);
		legendButton.setText(Messages.getString("collection.graph.legend")); // 凡例表示
		legendButton.setToolTipText(Messages.getString("collection.graph.legend"));
		// preferenceの情報を取得
		legendButton.setSelection(store.getBoolean(P_COLLECT_GRAPH_LEGEND_FLG));

		// 適用ボタン
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		apply = new Button(graphCompo, SWT.NONE);
		apply.setText(Messages.getString("apply"));
		apply.setToolTipText(Messages.getString("apply"));
		apply.setLayoutData(gridData);
		apply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_log.debug("apply");
				String selectItemName = getSelectItemNameBySelectIndex();
				List<CollectKeyInfoPK> collectKeyInfoList = getCollectorItems();
				
				int summaryType = getSummaryItem();
				Boolean checkInput = checkTreeCombo(collectKeyInfoList, summaryType);
				if (checkInput) {
					// 設定項目を保持する
					applySetting(selectItemName);
					
					// グラフを描画する
					drawGraphs(collectKeyInfoList, summaryType, selectItemName);
				}
			}
		});
		// SWTアクセスを許可するスレッドからの操作用
		checkAsyncExec(new Runnable(){
			@Override
			public void run() {
				// 選択状態の復元
				m_collectGraphView.setItemCodeCheckedTreeItems();
			}
		});
	}

	/**
	 * グラフ描画前に、選択状態を保存します。(グラフ描画できる正常なデータを保存)
	 * 保存されるのは以下
	 * ・画面サイズで折り返すか(Boolean)
	 * ・種別で折り返すか(Boolean)
	 * ・近似線を表示するか(Boolean)
	 * ・閾値モードにするか(Boolean)
	 * ・凡例を表示するか(Boolean)
	 * ・グラフズームレベル(String)
	 * ・ツリー選択状態(String)
	 * ・監視項目選択状態(String)
	 * ・サマリータイプ選択状態(String、EN)
	 * ・グラフタイプ(int)
	 * 
	 */
	private void applySetting(String selectItemName) {
		IPreferenceStore store = ClusterControlPlugin.getDefault().getPreferenceStore();

		store.setValue(P_COLLECT_GRAPH_SIZE_RETURN_FLG, returnButton.getSelection());
		store.setValue(P_COLLECT_GRAPH_KIND_RETURN_FLG, returnKindButton.getSelection());
		store.setValue(P_COLLECT_GRAPH_APPROX_FLG, approximateButton.getSelection());
		store.setValue(P_COLLECT_GRAPH_THRESHOLD_FLG, thresholdButton.getSelection());
		store.setValue(P_COLLECT_GRAPH_LEGEND_FLG, legendButton.getSelection());
		store.setValue(P_COLLECT_GRAPH_ZOOM_LEVEL, m_collectGraphView.getCollectGraphComposite().getZoomLevel());
		String checkedTreeInfo = storeCheckedTreeInfo();
		m_log.debug("checkedTreeInfo : " + checkedTreeInfo);
		store.setValue(P_COLLECT_GRAPH_SELECT_NODE_INFO, checkedTreeInfo);
		store.setValue(P_COLLECT_GRAPH_SELECT_ITEM_INFO, selectItemName);
		String summaryEn = SummaryTypeMessage.typeToStringEN(m_comboSummaryItem.getSelectionIndex());
		store.setValue(P_COLLECT_GRAPH_SELECT_SUMMARY_INFO, summaryEn);
		store.setValue(P_COLLECT_GRAPH_SELECT_TYPE_INFO, m_comboGraphTypeItem.getSelectionIndex());
	}

	/**
	 * グラフコンポジットのウィジェットの構成初期化
	 *
	 * @param parent
	 * @param style
	 */
	private void initialize() {

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		this.setLayout( layout );
	}


	/**
	 * ビューの更新時に呼ばれるアクション(最新時刻のデータを表示)
	 */
	@Override
	public void update() {
		super.update();
	}
	/**
	 * 画面で選択された監視項目種別リストを返します
	 * 
	 * @return 監視項目種別リスト
	 */
	private List<CollectKeyInfoPK> getCollectorItems(){
		if (selectIndexList == null || selectIndexList.size() == 0) {
			return null;
		}
		List<CollectKeyInfoPK> collectKeyInfoList = new ArrayList<>();
		for (Integer number : selectIndexList) {
			String key = m_listCollectorItem.getItem(number);
			String itemCodeName = (String)m_listCollectorItem.getData(key);
			CollectKeyInfoPK pk = new CollectKeyInfoPK();
			String itemName = itemCodeName.split(SEPARATOR_AT)[0];
			String displayName = itemCodeName.split(SEPARATOR_AT)[1];
			String monitorId = itemCodeName.split(SEPARATOR_AT)[2];
			pk.setItemName(itemName);
			pk.setDisplayName(displayName);
			pk.setMonitorId(monitorId);
			collectKeyInfoList.add(pk);
			m_log.info("getCollectorItem() 選択した監視項目情報 number:" + number + ", itemName:" + pk.getItemName() 
			+ ", displayName:" + pk.getDisplayName() + ", monitorId:" + pk.getMonitorId());
		}
		return collectKeyInfoList;
	}
	
	/**
	 * 収集項目を選択するコンボボックスを生成します。
	 *
	 * @param managers マネージャ名の配列
	 */
	public void setCollectorItemCombo(){
		
		List<String> allItemList = new ArrayList<>();
		// 現在のファシリティツリーの選択状態の文字列を取得
		List<String> selectList = this.m_collectGraphView.getFacilityTreeComposite().getCheckedTreeInfo();
		TreeMap<String, List<String>> managerFacilityMap = new TreeMap<>();
		for (String selectStr : selectList) {
			String[] nodeDetail = selectStr.split(SEPARATOR_HASH_EX_HASH);
			if (nodeDetail.length != 0 && nodeDetail[nodeDetail.length - 1].equals(String.valueOf(FacilityConstant.TYPE_NODE))) {
				String facilityId = nodeDetail[nodeDetail.length - 2];
				String managerName = nodeDetail[0];
				List<String> facilityList = managerFacilityMap.get(managerName);
				if (facilityList == null) {
					facilityList = new ArrayList<String>();
					managerFacilityMap.put(managerName, facilityList);
				}
				if (!facilityList.contains(facilityId)) {
					m_log.debug("収集値表示名を取得する managerName:" + managerName + ", facilityId:" + facilityId);
					facilityList.add(facilityId);
				}
			}
		}
		
		for (Map.Entry<String, List<String>> map : managerFacilityMap.entrySet()) {
			String managerName = map.getKey();
			List<String> facilityList = map.getValue();
			// 収集項目の一覧を生成
			List<CollectKeyInfoPK> collectKeyInfoList;
			try {
				CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
				collectKeyInfoList = wrapper.getItemCodeList(facilityList);
			} catch (InvalidRole_Exception e) {
				m_log.warn("setCollectorItemCombo() getItemCodeList, " + e.getMessage());
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return;
			} catch (InvalidUserPass_Exception
					| HinemosUnknown_Exception e) {
				// 上記以外の例外
				m_log.warn("setCollectorItemCombo() getItemCodeList, " + e.getMessage(), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				return;
			}
			// DBから取得した情報をリストに追加する
			for (CollectKeyInfoPK collectKeyInfo : collectKeyInfoList) {
				String itemName = collectKeyInfo.getItemName();
				String monitorId = collectKeyInfo.getMonitorId();
				String displayName = collectKeyInfo.getDisplayName();
				if (!allItemList.contains(itemName + SEPARATOR_AT + displayName + SEPARATOR_AT + monitorId)) {
					// itemCode@itemName にしてリストに追加
					allItemList.add(itemName + SEPARATOR_AT + displayName + SEPARATOR_AT + monitorId);
				}
			}
		}

		// すでにコンボにある場合は追加しない
		for (String itemCodeName : allItemList) {
			String itemName = HinemosMessage.replace(itemCodeName.split(SEPARATOR_AT)[0]);
			String displayName = itemCodeName.split(SEPARATOR_AT)[1];
			String monitorId = itemCodeName.split(SEPARATOR_AT)[2];
			// itemNameにdisplayNameが入っているかのチェック
			if (!displayName.equals("") && !itemName.endsWith("[" + displayName + "]")) {
				itemName += "[" + displayName + "]";
			}
			String itemNameStr = itemName + "(" + monitorId + ")";
			if (!(Arrays.asList(m_listCollectorItem.getItems())).contains(itemNameStr)) {
				m_listCollectorItem.add(itemNameStr);
				m_listCollectorItem.setData(itemNameStr, itemCodeName);
			}
		}

		// 選択状態の復元
		setDefaultItemInfo();
	}
	
	/**
	 * 同期チェック
	 * @param r
	 * @return
	 */
	private boolean checkAsyncExec(Runnable r){

		if(!m_collectGraphView.getFacilityTreeComposite().isDisposed()){
			m_log.trace("CollectSettingComposite.checkAsyncExec() is true");
			m_collectGraphView.getFacilityTreeComposite().getDisplay().asyncExec(r);
			return true;
		}
		else{
			m_log.trace("CollectSettingComposite.checkAsyncExec() is false");
			return false;
		}
	}

	/**
	 * 収集値表示名のリストをクリアします
	 */
	public void clearItem() {
		m_listCollectorItem.removeAll();
		m_comboSummaryItem.removeAll();
		m_comboGraphTypeItem.removeAll();
		setCheckBoxEnableByGraphKind();
		selectIndexList.clear();
	}

	/**
	 * 監視項目リストを選択状態にします。
	 * 
	 */
	public void setDefaultItemInfo() {
		m_log.debug("Preferenceから収集値表示名を復元します");
		String treeSelect = ClusterControlPlugin.getDefault().getPreferenceStore()
				.getString(CollectSettingComposite.P_COLLECT_GRAPH_SELECT_ITEM_INFO);
		if (treeSelect == null || treeSelect.equals("")) {
			return;
		}
		
		// リストを選択状態にする
		String array[] = treeSelect.split(SEPARATOR_HASH);
		m_listCollectorItem.setSelection(array);
		
		// 監視項目名リストの選択状態を取得しメンバに設定する
		this.selectIndexList = getSelectIndexListByItemName();
	}
	
	/**
	 * グラフ種別を選択するコンボボックスを生成します。
	 *
	 */
	public void setGraphTypeItemCombo() {
		m_log.debug("グラフ種別のComboを生成し、Preferenceから復元します");
		List<String> graphTypeList = Arrays.asList(m_comboGraphTypeItem.getItems());
		if (graphTypeList == null || graphTypeList.size() == 0) {
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.line"));// 折れ線グラフ
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.line"), 1);
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.line") 
					+ "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")"); // 折れ線(監視項目で集約)
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.line") 
					+ "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")", 2);
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.area"));// 積み上げ面
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.area"), 3);
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.scatter")); // 散布図
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.scatter"), 6);
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.scatter") 
					+ "(" + Messages.getString("collection.graph.summarized") + ")");// 散布図(集約)
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.scatter") 
					+ "(" + Messages.getString("collection.graph.summarized") + ")", 7);
			m_comboGraphTypeItem.add("--------------------");
			m_comboGraphTypeItem.setData("--------------------", 10);
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.pie"));// 円グラフ
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.pie"), 4);
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.pie") 
					+ "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")");// 円グラフ(監視項目で集約)
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.pie") 
					+ "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")", 5);
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.stackedbar"));// 棒線
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.stackedbar"), 8);
			m_comboGraphTypeItem.add(Messages.getString("collection.graph.stackedbar") // 棒線(監視項目で集約)
					+ "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")");
			m_comboGraphTypeItem.setData(Messages.getString("collection.graph.stackedbar") 
					+ "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")", 9);
			
			// preferenceから前回の情報を取得
			int type = ClusterControlPlugin.getDefault().getPreferenceStore()
					.getInt(CollectSettingComposite.P_COLLECT_GRAPH_SELECT_TYPE_INFO);
			m_comboGraphTypeItem.select(type);
			
			// グラフタイプ別にチェックボックスをつける
			setCheckBoxEnableByGraphKind();
		}
	}

	/**
	 * 画面で選択されたグラフ種別を返します
	 * 
	 * @return グラフ種別(-1の場合は未選択or境目)
	 */
	private int getGraphTypeItem(){
		String itemName = m_comboGraphTypeItem.getText();
		if (itemName == null || itemName.equals("")) {
			return SUMMARY_CODE_INVALID;
		}
		if ((Integer)m_comboGraphTypeItem.getData(itemName) == 10) {// 境目の[-----]
			return SUMMARY_CODE_INVALID;
		}
		return (Integer)m_comboGraphTypeItem.getData(itemName);
	}

	/**
	 * 収集項目を選択するコンボボックスを生成します。
	 *
	 */
	public void setSummaryItemCombo(){

		m_log.debug("サマリータイプのComboを生成してpreferenceから復元します");
		List<String> summaryList = Arrays.asList(m_comboSummaryItem.getItems());
		if (summaryList == null || summaryList.size() == 0) {
			List<String> summaryTypeList = SummaryTypeMessage.getSummaryTypeList();
			for (String summaryType : summaryTypeList) {
				m_comboSummaryItem.add(summaryType);
				m_comboSummaryItem.setData(summaryType, SummaryTypeMessage.stringToType(summaryType));
			}
			String summaryEn = ClusterControlPlugin.getDefault().getPreferenceStore()
					.getString(CollectSettingComposite.P_COLLECT_GRAPH_SELECT_SUMMARY_INFO);
			int type = SummaryTypeMessage.stringENToType(summaryEn);
			m_comboSummaryItem.select(type);
		}
	}

	/**
	 * 画面で選択されたサマリータイプを返します
	 * 
	 * @return サマリータイプ
	 */
	private int getSummaryItem(){
		String itemName = m_comboSummaryItem.getText();
		if (itemName == null || itemName.equals("")) {
			return SUMMARY_CODE_INVALID;
		}
		int itemCode = (Integer)m_comboSummaryItem.getData(itemName);
		m_log.debug("getSummaryItem() itemName:" + itemName + ", itemCode:" + itemCode);
		return itemCode;
	}

	/**
	 * サマリータイプのComboboxの選択状態を変更します。
	 * @param type
	 */
	public void setSummaryTypeComboBox(int type) {
		m_log.debug("setSummaryTypeComboBox() type:" + type);
		m_comboSummaryItem.select(m_comboSummaryItem.indexOf(SummaryTypeMessage.typeToString(type)));
	}

	/**
	 * 
	 * @param itemCodeList
	 * @param summaryType
	 * @return
	 */
	private boolean checkTreeCombo(List<CollectKeyInfoPK> itemCodeList, int summaryType) {

		if (itemCodeList == null || itemCodeList.isEmpty()) {
			m_log.debug("checkTreeCombo() 監視項目未選択");
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.collection.graph.selectdisplayname"));

			return false;
		}

		if (getGraphTypeItem() == SUMMARY_CODE_INVALID) {
			m_log.debug("checkTreeCombo() グラフタイプの選択異常");
			MessageDialog.openInformation(
					null,
					Messages.getString("message"), // グラフ種別を選択してください。
					Messages.getString("message.collection.graph.select.one.graph.kind.from.the.list"));

			return false;
		}

		if ((getGraphTypeItem() == 6 || getGraphTypeItem() == 7) && itemCodeList.size() < 2) {
			m_log.debug("checkTreeCombo() 散布図で監視項目IDが複数でない");
			MessageDialog.openInformation(
					null,
					Messages.getString("message"), // 散布図は監視項目IDを複数選択する必要があります
					Messages.getString("message.collection.graph.must.specify.more.than.two.monitorid.in.scatter"));

			return false;
		}
		
		if (itemCodeList.size() > 10) {
			m_log.debug("checkTreeCombo() 監視項目10個以上選択 size:" + itemCodeList.size());
			MessageDialog.openInformation(
					null,
					Messages.getString("message"), // 収集値表示名が10種類以上選択されています
					Messages.getString("message.collection.graph.morethan.selected.monitorid"));

			return false;
		}
		
		if ((getGraphTypeItem() == 3 || getGraphTypeItem() == 6 || getGraphTypeItem() == 7 || getGraphTypeItem() == 8 || getGraphTypeItem() == 9) 
				&& summaryType == SummaryTypeConstant.TYPE_RAW) {
			// 積み上げ面グラフ、散布図、積み上げ棒グラフはローデータの場合はグラフを表示しない
			m_log.debug("checkTreeCombo() ローデータで表示できないグラフ種別を選択");
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),// ローデータで表示できません
					Messages.getString("message.collection.graph.do.not.display.in.law"));

			return false;
		}

		m_log.debug("checkTreeCombo() OK.");
		return true;
	}

	/**
	 * グラフ種別によってチェックボックスの選択可否を設定します。
	 * 
	 */
	private void setCheckBoxEnableByGraphKind() {
		int type = getGraphTypeItem();
		returnButton.setEnabled(true);
		returnKindButton.setEnabled(true);
		switch (type) {
			case 1: // 折れ線
			case 2: // 折れ線(集約)
				approximateButton.setEnabled(true);
				thresholdButton.setEnabled(true);
				legendButton.setEnabled(true);
				break;
			case 3: // 積み上げ面
				approximateButton.setEnabled(false);
				thresholdButton.setEnabled(false);
				legendButton.setEnabled(true);
				break;
			case 4: // 円グラフ
			case 5: // 円グラフ(集約)
			case 8: // 積み上げ棒
			case 9: // 積み上げ棒(集約)
				approximateButton.setEnabled(false);
				thresholdButton.setEnabled(false);
				legendButton.setEnabled(false);
				break;
			case 6: // 散布図
			case 7: // 散布図(集約)
				approximateButton.setEnabled(true);
				thresholdButton.setEnabled(false);
				legendButton.setEnabled(true);
				break;
			default: // -------
				returnButton.setEnabled(false);
				returnKindButton.setEnabled(false);
				approximateButton.setEnabled(false);
				thresholdButton.setEnabled(false);
				legendButton.setEnabled(false);
		}
	}
	
	/**
	 * PerformanceGraphCompositeを実行し、グラフを描画します
	 * 
	 * @param collectKeyList 選択された監視項目コードリスト
	 * @param summaryType 選択されたサマリータイプ
	 */
	private void drawGraphs(List<CollectKeyInfoPK> collectKeyList, int summaryType, String selectItemName) {
		List<FacilityTreeItem> treeItemList = m_collectGraphView.getCheckedTreeItemList();
		int type = getGraphTypeItem();
		boolean totalflg = type == 1 ? false : true;
		boolean stackflg = type == 3 ? true : false;
		boolean pieflg = type == 4 || type == 5 ? true : false;
		totalflg = type == 4 ? false : totalflg;
		totalflg = type == 5 ? true : totalflg;
		boolean scatterflg = type == 6 || type == 7 ? true : false;
		totalflg = type == 6 ? false : totalflg;
		totalflg = type == 7 ? true : totalflg;
		boolean barflg = type == 8 || type == 9 ? true : false;
		totalflg = type == 8 ? false : totalflg;
		totalflg = type == 9 ? true : totalflg;
		
		try {
			// 適用ボタンを押下不可にする
			apply.setEnabled(false);
			m_collectGraphView.getCollectGraphComposite().drawGraphs(collectKeyList, selectItemName, summaryType, treeItemList,
					returnButton.getSelection(), returnKindButton.getSelection(), totalflg, stackflg,
					approximateButton.getSelection(), thresholdButton.getSelection(), pieflg, scatterflg,
					legendButton.getSelection(), barflg);
		} catch (InvalidRole_Exception e) {
			m_log.error("drawGraphs InvalidRole_Exception");
			MessageDialog.openInformation(null, Messages.getString("message"),
			Messages.getString("message.accesscontrol.16"));
			m_collectGraphView.getCollectGraphComposite().removeGraphSliderDisp();
		} catch (InvalidUserPass_Exception e) {
			m_log.error("drawGraphs InvalidUserPass_Exception");
			MessageDialog.openInformation(null, Messages.getString("message"),
			Messages.getString("message.accesscontrol.45"));
			m_collectGraphView.getCollectGraphComposite().removeGraphSliderDisp();
		} catch (Exception e) {
			m_log.error("drawGraphs グラフ描画時にエラーが発生 message=" + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("error"),
					Messages.getString("message.collection.graph.unexpected.error") + " : " + e.getMessage());
			m_collectGraphView.getCollectGraphComposite().removeGraphSliderDisp();
		} finally {
			// 適用ボタンを押下可能にする
			apply.setEnabled(true);
		}
	}

	/**
	 * 画面の選択順番はindex番号で管理し、監視項目名を取得して<br>
	 * 項目名を「#」で連結して返します。<br>
	 * List.getSelectionIndex：rcpの場合は選択したindexが取れるが、rapの場合は選択したindexが取れない。<br>
	 * List.getSelectionIndices：rcpの場合は選択順番どおりに取れない(Listに追加順で取れる)が、rapの場合は選択順番どおりに取れる。<br>
	 * List.getSelection：rcpの場合は選択した順番どおりに取れない(Listに追加順で取れる)が、rapの場合は選択順番どおりに取れる。<br>
	 * @return
	 */
	private String getSelectItemNameBySelectIndex() {
		StringBuilder selectStrBuilder = new StringBuilder();
		if (selectIndexList.size() != m_listCollectorItem.getSelectionCount()) {
			// ctrl + Aを押された可能性
			// 表示されているもの順に入れ替える
			selectIndexList.clear();
			for (int ctrlnumber : m_listCollectorItem.getSelectionIndices()) {
				selectIndexList.add((Integer)ctrlnumber);
			}
		}
		for (Integer index : selectIndexList) {
			try {
				selectStrBuilder.append(this.m_listCollectorItem.getItem(index) + SEPARATOR_HASH);
			} catch (Exception e) {
				// nop
				m_log.error("getSelectItemNameBySelectIndex() エラー発生" + e.getMessage());
			}
		}
		m_log.debug("監視項目選択順番連結 selectStr:" + selectStrBuilder.toString());
		return selectStrBuilder.toString();
	}
	
	/**
	 * preferenceで保持していた監視項目選択情報(監視項目名の「#」連結)から<br>
	 * index番号に変換してリストで返します。<br>
	 * @return
	 */
	private List<Integer> getSelectIndexListByItemName() {
		List<Integer> selectList = new ArrayList<>();
		String treeSelect = ClusterControlPlugin.getDefault().getPreferenceStore()
				.getString(CollectSettingComposite.P_COLLECT_GRAPH_SELECT_ITEM_INFO);
		if (treeSelect == null || treeSelect.equals("")) {
			return selectList;
		}
		for (String str : treeSelect.split(SEPARATOR_HASH)) {
			int number = 0;
			for (String item : m_listCollectorItem.getItems()) {
				if (item.equals(str)) {
					selectList.add(number);
				}
				number++;
			}
		}
		m_log.debug("preferenceからindex情報を生成 :" + selectList.toString());
		return selectList;
	}
	/**
	 * ファシリティツリーの選択状態を返します。<br>
	 * 以下のような形式：<br>
	 * FacilityTreeComposite.getCheckedTreeInfo()参照<br>
	 * マネージャ3#!#REGISTERED#!#node_db_1#!#1##@##マネージャ4#!#REGISTERED#!#node_ap_1#!#1<br>
	 * #!# -> ファシリティの項目の区切り([マネージャ名]#!#[スコープ]#!#...#!#[ノードID]#!#)<br>
	 * ##@## -> ファシリティの区切り
	 * 
	 * @return
	 */
	private String storeCheckedTreeInfo() {
		List<String> selectList = this.m_collectGraphView.getFacilityTreeComposite().getCheckedTreeInfo();
		StringBuilder sb = new StringBuilder();
		for (String selectParam : selectList) {
			sb.append(selectParam);
			sb.append(SEPARATOR_HASH_HASH_AT_HASH_HASH);
		}
		if (sb.length() == 0) {
			return "";
		}
		return sb.toString().substring(0, sb.toString().length() - SEPARATOR_HASH_HASH_AT_HASH_HASH.length());
	}
}
