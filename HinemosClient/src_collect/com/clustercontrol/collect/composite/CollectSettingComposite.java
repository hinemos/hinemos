/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Shell;
import org.openapitools.client.model.CollectKeyInfoResponseP1;
import org.openapitools.client.model.FacilityInfoResponse;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.collect.bean.CollectConstant;
import com.clustercontrol.collect.bean.GraphTypeConstant;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.bean.SummaryTypeMessage;
import com.clustercontrol.collect.dialog.CollectItemJobDialog;
import com.clustercontrol.collect.util.CollectRestClientWrapper;
import com.clustercontrol.collect.view.CollectGraphView;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * 性能グラフを表示するコンポジットクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class CollectSettingComposite extends Composite {
	private static Log m_log = LogFactory.getLog(CollectSettingComposite.class);

	/** シェル */
	private Shell m_shell = null;

	/**
	 * 
	 */
	private CollectGraphView m_collectGraphView;

	/** 収集値表示名（ジョブ）のマップ（収集値表示名、収集値表示名の情報） */
	private Map<String, String> m_collectorItemJobMap = new HashMap<>();

	/** 収集値表示名（ジョブ）選択項目のリスト(収集値表示名) */
	private List<String> m_collectorItemJobCheckList = new ArrayList<>();

	/** ジョブ収集値表示名一覧ダイアログ表示ボタン */
	private Button m_showJobHistoryButton = null;

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
	/** 予測変動幅表示フラグ */
	private Button predictedButton = null;
	/** 監視項目IDの選択順番index保持用 */
	private List<Integer> selectIndexList = new ArrayList<>();
	/** [適用]ボタン */
	private Button apply = null;

	private static final int SUMMARY_CODE_INVALID = -1;
	
	/** 区切り文字(\u2029) */
	protected static final String SEPARATOR_HASH = "\u2029";
	/** 区切り文字(##@##) */
	protected static final String SEPARATOR_HASH_HASH_AT_HASH_HASH = "##@##";
	/** 区切り文字(#!#) */
	protected static final String SEPARATOR_HASH_EX_HASH = "#!#";
	/** 区切り文字(＠) */
	protected static final String SEPARATOR_AT = "＠";
	
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
	
	/**  */
	public static final String P_COLLECT_GRAPH_SIGMA_FLG = "collectGraphSigmaFlg";

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

		m_shell = this.getShell();

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
		// ジョブ収集値表示名一覧ダイアログ表示ボタン
		m_showJobHistoryButton = new Button(graphCompo, SWT.NONE);
		m_showJobHistoryButton.setText(Messages.getString("collection.graph.jobhistory.show"));
		m_showJobHistoryButton.setToolTipText(Messages.getString("collection.graph.jobhistory.show"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		m_showJobHistoryButton.setLayoutData(gridData);
		m_showJobHistoryButton.addSelectionListener( new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				CollectItemJobDialog dialog 
						= new CollectItemJobDialog(m_shell, m_collectorItemJobMap,
								m_collectorItemJobCheckList);
				if (dialog.open() == IDialogConstants.OK_ID) {
					// 処理なし
				}
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
		
		// 
		predictedButton = new Button(checkCompsite, SWT.CHECK);
		predictedButton.setText(Messages.getString("collection.graph.predicted")); // 予測変動表示
		predictedButton.setToolTipText(Messages.getString("collection.graph.predicted"));
		predictedButton.setSelection(store.getBoolean(P_COLLECT_GRAPH_SIGMA_FLG));

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
				List<CollectKeyInfoResponseP1> collectKeyInfoList = getCollectorItems();
				
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
		store.setValue(P_COLLECT_GRAPH_SIGMA_FLG, predictedButton.getSelection());
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
	private List<CollectKeyInfoResponseP1> getCollectorItems(){
		if ((selectIndexList == null || selectIndexList.size() == 0)
				&& (m_collectorItemJobCheckList == null || m_collectorItemJobCheckList.size() == 0)) {
			return null;
		}
		List<CollectKeyInfoResponseP1> collectKeyInfoList = new ArrayList<>();
		for (Integer number : selectIndexList) {
			String key = m_listCollectorItem.getItem(number);
			String itemCodeName = (String)m_listCollectorItem.getData(key);
			CollectKeyInfoResponseP1 pk = new CollectKeyInfoResponseP1();
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
		int type = getGraphTypeItem();
		if (type != GraphTypeConstant.LINE 
				&& type != GraphTypeConstant.LINE_SUMMARIZED 
				&& type != GraphTypeConstant.AREA) {
			// 折れ線グラフ、積み上げ面以外はジョブ履歴は対象外
			return 	collectKeyInfoList;
		}
		for (String collectorItemJobCheck : m_collectorItemJobCheckList) {
			String itemCodeName = m_collectorItemJobMap.get(collectorItemJobCheck);
			CollectKeyInfoResponseP1 pk = new CollectKeyInfoResponseP1();
			String itemName = itemCodeName.split(SEPARATOR_AT)[0];
			String displayName = itemCodeName.split(SEPARATOR_AT)[1];
			String monitorId = itemCodeName.split(SEPARATOR_AT)[2];
			pk.setItemName(itemName);
			pk.setDisplayName(displayName);
			pk.setMonitorId(monitorId);
			collectKeyInfoList.add(pk);
			m_log.info("getCollectorItem() 選択した監視項目情報 itemName:" + pk.getItemName() 
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
		m_log.info("setCollectorItemCombo() start.");

		List<String> allItemList = new ArrayList<>();
		// 現在のファシリティツリーの選択状態の文字列を取得
		List<String> selectList = this.m_collectGraphView.getFacilityTreeComposite().getSelectFacilityList();
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
					if (m_log.isTraceEnabled()) {
						m_log.trace("setCollectorItemCombo() 収集値表示名を取得する managerName:" + managerName + ", facilityId:" + facilityId);
					}
					facilityList.add(facilityId);
				}
			}
		}
		for (Map.Entry<String, List<String>> managerFacilityInfo : managerFacilityMap.entrySet()) {
			managerFacilityInfo.getValue().add(RoleSettingTreeConstant.ROOT_ID);
		}
		for (Map.Entry<String, List<String>> map : managerFacilityMap.entrySet()) {
			String managerName = map.getKey();
			List<String> facilityList = map.getValue();
			// 収集項目の一覧を生成
			List<CollectKeyInfoResponseP1> collectKeyInfoList = new ArrayList<>();
			try {
				CollectRestClientWrapper wrapper = CollectRestClientWrapper.getWrapper(managerName);
				String facilityIds = String.join(",", facilityList);
				List<CollectKeyInfoResponseP1> res = wrapper.getItemCodeList(facilityIds);
				CollectKeyInfoResponseP1 collectKeyInfoPK = new CollectKeyInfoResponseP1();
				for(CollectKeyInfoResponseP1 tmp:res){
					collectKeyInfoPK = new CollectKeyInfoResponseP1();
					collectKeyInfoPK.setDisplayName(tmp.getDisplayName());
					collectKeyInfoPK.setFacilityId(tmp.getFacilityId());
					collectKeyInfoPK.setItemName(tmp.getItemName());
					collectKeyInfoPK.setMonitorId(tmp.getMonitorId());
					collectKeyInfoList.add(collectKeyInfoPK);
				}
			} catch (HinemosException e) {
				// 上記以外の例外
				m_log.warn("setCollectorItemCombo() getItemCodeList, " + e.getMessage(), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				return;
			}
			// DBから取得した情報をリストに追加する
			for (CollectKeyInfoResponseP1 collectKeyInfo : collectKeyInfoList) {
				String itemName = collectKeyInfo.getItemName();
				String monitorId = collectKeyInfo.getMonitorId();
				String displayName = collectKeyInfo.getDisplayName();
				if (!allItemList.contains(itemName + SEPARATOR_AT + displayName + SEPARATOR_AT + monitorId)) {
					// itemCode＠itemName にしてリストに追加
					allItemList.add(itemName + SEPARATOR_AT + displayName + SEPARATOR_AT + monitorId);
				}
			}
		}

		// すでにコンボにある場合は追加しない
		List<String> itemList = new ArrayList<>();
		Map<String,String> itemMap = new HashMap<>();
		for (String itemCodeName : allItemList) {
			String itemName = HinemosMessage.replace(itemCodeName.split(SEPARATOR_AT)[0]);
			String displayName = itemCodeName.split(SEPARATOR_AT)[1];
			String monitorId = itemCodeName.split(SEPARATOR_AT)[2];
			// itemNameにdisplayNameが入っているかのチェック
			if (!displayName.equals("") && !itemName.endsWith("[" + displayName + "]")) {
				itemName += "[" + displayName + "]";
			}
			String itemNameStr = "";
			if (!monitorId.equals(CollectConstant.COLLECT_TYPE_JOB)) {
				// ジョブ履歴以外
				itemNameStr = itemName + "(" + monitorId + ")";
				if (!(Arrays.asList(m_listCollectorItem.getItems())).contains(itemNameStr)) {
					itemList.add(itemNameStr);
					itemMap.put(itemNameStr, itemCodeName);
				}
			} else {
				// ジョブ履歴
				itemNameStr = itemName;
				if (!m_collectorItemJobMap.containsKey(itemNameStr)) {
					m_collectorItemJobMap.put(itemNameStr, itemCodeName);
				}
				Iterator<String> iter = m_collectorItemJobCheckList.iterator();
				while(iter.hasNext()) {
					String collectorItemJobCheck = iter.next();
					if (!m_collectorItemJobMap.containsKey(collectorItemJobCheck)) {
						iter.remove();
					}
				}
			}
		}
		m_listCollectorItem.setItems(itemList.toArray(new String[0]));
		for (Map.Entry<String, String> entry : itemMap.entrySet()) {
			m_listCollectorItem.setData(entry.getKey(),entry.getValue());
		}

		// webクライアントで描画中に次に進むとエラーになる場合があるのでいったんチェックする（マネージャアクセス等は発生しない)
		m_listCollectorItem.update();

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
		m_collectorItemJobMap.clear();
		m_collectorItemJobCheckList.clear();
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

		// ジョブ監視項目名リストの選択状態を取得しメンバに設定する
		this.m_collectorItemJobCheckList = getSelectIndexListByItemNameForJob();
	}
	
	/**
	 * グラフ種別を選択するコンボボックスを生成します。
	 *
	 */
	public void setGraphTypeItemCombo() {
		m_log.debug("グラフ種別のComboを生成し、Preferenceから復元します");
		List<String> graphTypeList = Arrays.asList(m_comboGraphTypeItem.getItems());
		if (graphTypeList == null || graphTypeList.size() == 0) {
			// 折れ線グラフ
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.LINE));
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.LINE), 
					GraphTypeConstant.LINE);
			// 折れ線(監視項目で集約)
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.LINE_SUMMARIZED));
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.LINE_SUMMARIZED), 
					GraphTypeConstant.LINE_SUMMARIZED);
			// 積み上げ面
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.AREA));
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.AREA), 
					GraphTypeConstant.AREA);
			// 散布図
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.SCATTER)); 
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.SCATTER), 
					GraphTypeConstant.SCATTER);
			// 散布図(集約)
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.SCATTER_SUMMARIZED));
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.SCATTER_SUMMARIZED), 
					GraphTypeConstant.SCATTER_SUMMARIZED);
			// 区切り
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(null));
			// 円グラフ
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.PIE));
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.PIE), 
					GraphTypeConstant.PIE);
			// 円グラフ(監視項目で集約)
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.PIE_SUMMARIZED));
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.PIE_SUMMARIZED), 
					GraphTypeConstant.PIE_SUMMARIZED);
			// 棒線
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.STACKEDBAR));
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.STACKEDBAR), 
					GraphTypeConstant.STACKEDBAR);
			// 棒線(監視項目で集約)
			m_comboGraphTypeItem.add(GraphTypeConstant.typeToString(GraphTypeConstant.STACKEDBAR_SUMMARIZED));
			m_comboGraphTypeItem.setData(GraphTypeConstant.typeToString(GraphTypeConstant.STACKEDBAR_SUMMARIZED), 
					GraphTypeConstant.STACKEDBAR_SUMMARIZED);
			
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
		if (m_comboGraphTypeItem.getData(itemName) == null) {// 境目の[-----]
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
	private boolean checkTreeCombo(List<CollectKeyInfoResponseP1> itemCodeList, int summaryType) {

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

		if ((getGraphTypeItem() == GraphTypeConstant.SCATTER || getGraphTypeItem() == GraphTypeConstant.SCATTER_SUMMARIZED) 
				&& itemCodeList.size() < 2) {
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
		
		if ((getGraphTypeItem() == GraphTypeConstant.AREA 
				|| getGraphTypeItem() == GraphTypeConstant.SCATTER 
				|| getGraphTypeItem() == GraphTypeConstant.SCATTER_SUMMARIZED 
				|| getGraphTypeItem() == GraphTypeConstant.STACKEDBAR 
				|| getGraphTypeItem() == GraphTypeConstant.STACKEDBAR_SUMMARIZED) 
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
			case GraphTypeConstant.LINE: // 折れ線
			case GraphTypeConstant.LINE_SUMMARIZED: // 折れ線(集約)
				approximateButton.setEnabled(true);
				thresholdButton.setEnabled(true);
				legendButton.setEnabled(true);
				predictedButton.setEnabled(true);
				break;
			case GraphTypeConstant.AREA: // 積み上げ面
				approximateButton.setEnabled(false);
				thresholdButton.setEnabled(false);
				legendButton.setEnabled(true);
				predictedButton.setEnabled(false);
				break;
			case GraphTypeConstant.PIE: // 円グラフ
			case GraphTypeConstant.PIE_SUMMARIZED: // 円グラフ(集約)
			case GraphTypeConstant.STACKEDBAR: // 積み上げ棒
			case GraphTypeConstant.STACKEDBAR_SUMMARIZED: // 積み上げ棒(集約)
				approximateButton.setEnabled(false);
				thresholdButton.setEnabled(false);
				legendButton.setEnabled(false);
				predictedButton.setEnabled(false);
				break;
			case GraphTypeConstant.SCATTER: // 散布図
			case GraphTypeConstant.SCATTER_SUMMARIZED: // 散布図(集約)
				approximateButton.setEnabled(true);
				thresholdButton.setEnabled(false);
				legendButton.setEnabled(true);
				predictedButton.setEnabled(false);
				break;
			default: // -------
				returnButton.setEnabled(false);
				returnKindButton.setEnabled(false);
				approximateButton.setEnabled(false);
				thresholdButton.setEnabled(false);
				legendButton.setEnabled(false);
				predictedButton.setEnabled(false);
		}
	}
	
	/**
	 * PerformanceGraphCompositeを実行し、グラフを描画します
	 * 
	 * @param collectKeyList 選択された監視項目コードリスト
	 * @param summaryType 選択されたサマリータイプ
	 */
	private void drawGraphs(List<CollectKeyInfoResponseP1> collectKeyList, int summaryType, String selectItemName) {

		int type = getGraphTypeItem();

		List<FacilityTreeItemResponse> treeItemList = m_collectGraphView.getCheckedTreeItemList();
		Map<String, List<FacilityInfoResponse>> facilityInfoMap = new TreeMap<>();
		for (FacilityTreeItemResponse treeItem : treeItemList) {
			if (treeItem.getData().getFacilityType() != FacilityTypeEnum.NODE) {
				continue;
			}
			String managerName = ScopePropertyUtil.getManager(treeItem).getData().getFacilityId();
			if (!facilityInfoMap.containsKey(managerName)) {
				facilityInfoMap.put(managerName, new ArrayList<FacilityInfoResponse>());
				if (!m_collectorItemJobCheckList.isEmpty()
						&& (type == GraphTypeConstant.LINE 
						|| type == GraphTypeConstant.LINE_SUMMARIZED 
						|| type == GraphTypeConstant.AREA)) {
					// ジョブ履歴を表示する場合
					FacilityInfoResponse rootFacilityInfo = new FacilityInfoResponse();
					rootFacilityInfo.setFacilityId(RoleSettingTreeConstant.ROOT_ID);
					rootFacilityInfo.setFacilityName("");
					rootFacilityInfo.setFacilityType(FacilityTypeEnum.NODE);
					facilityInfoMap.get(managerName).add(rootFacilityInfo);
				}
			}
			facilityInfoMap.get(managerName).add(treeItem.getData());
		}

		boolean totalflg = false;
		boolean stackflg = false;
		boolean pieflg = false;
		boolean scatterflg = false;
		boolean barflg = false;
		if (type == GraphTypeConstant.AREA) {
			stackflg = true;
		}
		if (type == GraphTypeConstant.PIE || type == GraphTypeConstant.PIE_SUMMARIZED) {
			pieflg = true;
		}
		if (type == GraphTypeConstant.SCATTER || type == GraphTypeConstant.SCATTER_SUMMARIZED) {
			scatterflg = true;
		}
		if (type == GraphTypeConstant.STACKEDBAR || type == GraphTypeConstant.STACKEDBAR_SUMMARIZED) {
			barflg = true;
		}
		if (type == GraphTypeConstant.LINE_SUMMARIZED
				|| type == GraphTypeConstant.AREA
				|| type == GraphTypeConstant.PIE_SUMMARIZED
				|| type == GraphTypeConstant.SCATTER_SUMMARIZED
				|| type == GraphTypeConstant.STACKEDBAR_SUMMARIZED) {
			totalflg = true;
		}
		
		try {
			// 適用ボタンを押下不可にする
			apply.setEnabled(false);
			m_collectGraphView.getCollectGraphComposite().drawGraphs(collectKeyList, selectItemName, summaryType, facilityInfoMap,
					returnButton.getSelection(), returnKindButton.getSelection(), totalflg, stackflg,
					approximateButton.getSelection(), thresholdButton.getSelection(), pieflg, scatterflg,
					legendButton.getSelection(), predictedButton.getSelection(), barflg);
		} catch (InvalidRole e) {
			m_log.error("drawGraphs InvalidRole_Exception");
			MessageDialog.openInformation(null, Messages.getString("message"),
			Messages.getString("message.accesscontrol.16"));
			m_collectGraphView.getCollectGraphComposite().removeGraphSliderDisp();
		} catch (InvalidUserPass e) {
			m_log.error("drawGraphs InvalidUserPass_Exception");
			MessageDialog.openInformation(null, Messages.getString("message"),
			Messages.getString("message.accesscontrol.45"));
			m_collectGraphView.getCollectGraphComposite().removeGraphSliderDisp();
		} catch (HinemosDbTimeout e) {
			String message = HinemosMessage.replace(e.getMessage());
			m_log.error("drawGraphs グラフ描画時にエラーが発生 message=" + message, e);
			MessageDialog.openError(
					null,
					Messages.getString("error"),
					Messages.getString("message.collection.graph.unexpected.error") + " : " + message);
			m_collectGraphView.getCollectGraphComposite().removeGraphSliderDisp();
		} catch (IllegalStateException e) {
			// Webクライアントのみ、既に別スクリプトが実行中の場合に発生する。
			// メッセージが分かりにくいため変更する。スライダー、グラフは消去しない。
			m_log.warn("Another script is already being executed. message=" + HinemosMessage.replace(e.getMessage()));
			MessageDialog.openWarning(null, Messages.getString("word.warn"),
					Messages.getString("message.performance.4"));
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
	 * 項目名を区切り文字で連結して返します。<br>
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
		// ジョブ
		for (String collectorItemJob : m_collectorItemJobCheckList) {
			selectStrBuilder.append(collectorItemJob + SEPARATOR_HASH);
		}
		
		m_log.debug("監視項目選択順番連結 selectStr:" + selectStrBuilder.toString());
		return selectStrBuilder.toString();
	}
	
	/**
	 * preferenceで保持していた監視項目選択情報(区切り文字で連結されている監視項目名)から<br>
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
	 * preferenceで保持していた監視項目選択情報(監視項目名の「#」連結)をリストにして返します。<br>
	 * @return
	 */
	private List<String> getSelectIndexListByItemNameForJob() {
		List<String> selectList = new ArrayList<>();
		String treeSelect = ClusterControlPlugin.getDefault().getPreferenceStore()
				.getString(CollectSettingComposite.P_COLLECT_GRAPH_SELECT_ITEM_INFO);
		if (treeSelect == null || treeSelect.equals("")) {
			return selectList;
		}
		for (String str : treeSelect.split(SEPARATOR_HASH)) {
			if (m_collectorItemJobMap.containsKey(str)) {
				selectList.add(str);
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
		List<String> selectList = this.m_collectGraphView.getFacilityTreeComposite().getSelectFacilityList();
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
