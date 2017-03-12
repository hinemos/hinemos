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

package com.clustercontrol.collect.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.composite.CollectSettingComposite;
import com.clustercontrol.collect.preference.PerformancePreferencePage;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.ws.collect.CollectData;
import com.clustercontrol.ws.collect.CollectKeyInfoPK;
import com.clustercontrol.ws.collect.CollectorItemCodeMstData;
import com.clustercontrol.ws.collect.HashMapInfo;
import com.clustercontrol.ws.collect.HashMapInfo.Map3;
import com.clustercontrol.ws.collect.HashMapInfo.Map4;
import com.clustercontrol.ws.collect.HinemosUnknown_Exception;
import com.clustercontrol.ws.collect.InvalidRole_Exception;
import com.clustercontrol.ws.collect.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.EventDataInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorNumericValueInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * 性能グラフを表示する補助クラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class CollectGraphUtil {
	private static Log m_log = LogFactory.getLog(CollectGraphUtil.class);

	/**
	 * 期間の定数(1分のmsec)
	 */
	public static final long MILLISECOND_MINUTE = 60000L;
	/**
	 * 期間の定数(1時間のmsec)
	 */
	public static final long MILLISECOND_HOUR = MILLISECOND_MINUTE * 60L;
	/**
	 * 期間の定数(24時間のmsec)
	 */
	public static final long MILLISECOND_DAY = MILLISECOND_HOUR * 24L;
	/**
	 * 期間の定数(1週間のmsec)
	 */
	public static final long MILLISECOND_WEEK = MILLISECOND_DAY * 7L;
	/**
	 * 期間の定数(31日のmsec)
	 */
	public static final long MILLISECOND_MONTH = MILLISECOND_DAY * 31L;
	/**
	 * 期間の定数(1年のmsec)
	 */
	public static final long MILLISECOND_YEAR = MILLISECOND_DAY * 365L;
	/**
	 * 期間の定数(10年のmsec)
	 */
	public static final long MILLISECOND_10YEAR = MILLISECOND_YEAR * 10L;
	
	
	/**
	 * セパレータ
	 */
	private static final String SQUARE_SEPARATOR = "#";

	/**
	 * 現在表示中のグラフ表示の最古時間
	 * (DBに取りに行った最古時間、これよりも古い時刻を表示する場合は取得しにいく)
	 */
	private Long m_targetConditionStartDate = null;
	
	/**
	 * 現在表示中のグラフ表示の最新時間
	 * (DBにとりに行った最新時間、現在時刻を越えない)
	 */
	private Long m_targetConditionEndDate = null;
	
	/**
	 * サマリータイプ
	 */
	private int m_summaryType = -1;
	
	/**
	 * managerNameとfacilityIDとcollectIDのリスト(itemCode混合)
	 */
	private TreeMap<String, TreeMap<String, List<Integer>>> m_targetManagerFacilityCollectMap = new TreeMap<String, TreeMap<String, List<Integer>>>();
	
	/**
	 * マネージャ名とファシリティIDとファシリティ名のマップ
	 * (マネージャ名 + # + ファシリティID、ファシリティ名)の構造
	 */
	private TreeMap<String, String> m_managerFacilityIdNameMap = new TreeMap<>();
	
	/**
	 * マネージャ名とファシリティIDとダミーファシリティIDのマップ
	 * (マネージャ名 + # + ファシリティID、ダミーファシリティID)の構造
	 * ファシリティIDに「.」などが入るとグラフを正常に表示できないためダミーファシリティIDを作成する
	 */
	private TreeMap<String, String> m_managerFacilityIdDummyNameMap = new TreeMap<>();
	
	/**
	 * マネージャ名とダミーマネージャ名のマップ
	 * マネージャ名にはhtmlで使用されると誤作動を起こす文字(#など)が使用可能なので、ダミーマネージャ名を持ちまわる
	 */
	private TreeMap<String, String> m_managerDummyNameMap = new TreeMap<>();
	/**
	 * マネージャ名とitemName+displayName+monitorIdとcollectIdのリスト
	 * (※itemNameはコードではない)<br>
	 * リソース監視の[*ALL*]を選択した場合、displayNameとmonitorIdとfacilityIdで1つのグラフを生成し、<br>
	 * 内訳にチェックをした場合、monitorIdとitemNameとfacilityIdで1つのグラフを生成するため、keyはitemName+displayName+monitorIdになる。<br>
	 * 
	 */
	private TreeMap<String, Map<String, List<Integer>>> m_managerMonitorCollectIdMap = new TreeMap<String, Map<String, List<Integer>>>();
	
	/**
	 * 画面左で選択されたもの(itemNameとmonitorIdが入ってる)
	 */
	private List<CollectKeyInfoPK> m_collectKeyInfoList = new ArrayList<>();
	
	/**
	 * スライダーの全体開始時刻
	 */
	private Long m_sliderStart = null;

	/**
	 * スライダーの全体終了時刻
	 */
	private Long m_sliderEnd = null;
	
	/**
	 * グラフのズームサイズ
	 */
	private String m_graphZoomSize = null;
	
	/**
	 * 複数線グラフになるかどうか
	 */
	private boolean totalFlg = false;
	
	/**
	 * 積み上げ面グラフになるかどうか
	 */
	private boolean stackFlg = false;
	
	/**
	 * 円グラフになるかどうか
	 */
	private boolean pieFlg = false;
	
	/**
	 * 散布図になるかどうか
	 */
	private boolean scatterFlg = false;
	
	/**
	 * 棒線になるかどうか
	 */
	private boolean barFlg = false;
	
	/**
	 * 監視項目種別名の選択順番
	 */
	private String selectInfoStr = "";
	
	/**
	 * 画面の幅
	 */
	private int m_screenWidth = 0;
	
	private static CollectGraphUtil getInstance(){
		return SingletonUtil.getSessionInstance( CollectGraphUtil.class );
	}
	
	/**
	 * 現在表示中のグラフに線を追加します。
	 * 
	 * @param startDate DBから取得するデータの開始時刻
	 * @param endDate DBから取得するデータの終了時刻
	 * @param selectStartDate グラフの表示開始時刻
	 * @param selectEndDate グラフの表示終了時刻
	 * @return
	 * @throws InvalidRole_Exception 
	 * @throws HinemosUnknown_Exception 
	 * @throws InvalidUserPass_Exception 
	 */
	public static StringBuffer getAddGraphPlotJson(Long startDate, Long endDate, Long selectStartDate, Long selectEndDate) 
			throws InvalidUserPass_Exception, HinemosUnknown_Exception, InvalidRole_Exception {
		m_log.debug("getAddGraphPlotJson() start");
		StringBuffer sb = new StringBuffer();
		int count = 0;
		boolean allbreak = false;
		for (CollectKeyInfoPK collectKeyInfo : getInstance().m_collectKeyInfoList) {
			String itemName = HinemosMessage.replace(collectKeyInfo.getItemName());
			String monitorId = collectKeyInfo.getMonitorId();
			String displayName = collectKeyInfo.getDisplayName();
			for (Map.Entry<String, TreeMap<String, List<Integer>>> entry : getInstance().m_targetManagerFacilityCollectMap.entrySet()) {
				String managerName = entry.getKey();
				Map<Integer, List<CollectData>> graphMap = getGraphDetailDataMap(managerName, monitorId, displayName, itemName, startDate, endDate);
				String itemNameDisplayNameMonitorId = itemName + displayName + monitorId;
				Map<String, Integer> collectIdMap = getFacilityCollectMap(itemNameDisplayNameMonitorId, managerName);
				// plot文字列を取得
				List<String> plotList = parseGraphData(collectIdMap, graphMap);
				
				if (plotList == null || plotList.isEmpty()) {
					m_log.debug("getAddGraphPlotJson() plotDataList is Empty.");
					break;
				}
				// 閾値情報と単位情報とグラフレンジ情報を取得する
				String thresholdStr = getThresholdData(managerName, monitorId);
				String thresholdInfoMin = "0";
				String thresholdInfoMax = "0";
				String thresholdWarnMin = "0";
				String thresholdWarnMax = "0";
				String measure = "";
				String pluginId = "";
				String graphRange = Boolean.FALSE.toString(); // graphrangeが未指定だとjson構文エラーになるので初期値を設定
				String isHttpSce = Boolean.FALSE.toString();
				if (thresholdStr != null) {
					thresholdInfoMin = thresholdStr.split(",")[0];
					thresholdInfoMax = thresholdStr.split(",")[1];
					thresholdWarnMin = thresholdStr.split(",")[2];
					thresholdWarnMax = thresholdStr.split(",")[3];
					measure = thresholdStr.split(",")[4];
					pluginId = thresholdStr.split(",")[5];
					graphRange = thresholdStr.split(",")[6];
					isHttpSce = thresholdStr.split(",")[7];
				}
				
				//  #でsplitしてaddPoints
				for (String plotStr : plotList) {
					String split_plot[] = plotStr.split(SQUARE_SEPARATOR);
					String facilityId = split_plot[0];
					String collectId = split_plot[1];
					String plot = split_plot[2];
					String facilityName = getInstance().m_managerFacilityIdNameMap.get(managerName + SQUARE_SEPARATOR + facilityId);
					String facilityDummyId = getInstance().m_managerFacilityIdDummyNameMap.get(managerName + SQUARE_SEPARATOR + facilityId);
					String managerDummyName = getInstance().m_managerDummyNameMap.get(managerName);
					String itemName2 = itemName;
					if (!displayName.equals("") && !itemName2.endsWith("[" + displayName + "]")) {
						itemName2 += "[" + displayName + "]";
					}
					// groupIdをexecuteJavascriptForFirstDrawGraphs関数で作成したもの(id)と同じにすると、線が描画・追加描画される
					String groupId = itemName2 + "_" + monitorId;
					if (!getInstance().totalFlg) {
						groupId = facilityDummyId + "_" + managerDummyName + "_" + itemName2 + "_" + monitorId;
					}
					String param =
							"{"
							+ "\'data\':" + plot + ", "
							+ "\'realfacilityid\':\'" + facilityId + "\', "
							+ "\'facilityid\':\'" + facilityDummyId + "\', " // DUMMY
							+ "\'facilityname\':\'" + facilityName + "\', "
							+ "\'monitorid\':\'" + monitorId + "\', "
							+ "\'displayname\':\'" + displayName + "\', " 
							+ "\'collectid\':\'" + collectId + "\', "
							+ "\'realmanagername\':\'" + managerName + "\', "
							+ "\'managername\':\'" + managerDummyName + "\', " // DUMMY
							+ "\'itemname\':\'" + itemName2 + "\', "
							+ "\'groupid\':\'" + groupId + "\', " // 複数線の場合は、ここを同じにする
							+ "\'measure\':\'" + measure + "\', "
							+ "\'pluginid\':\'" + pluginId + "\', "
							+ "\'graphrange\':" + graphRange + ", "
							+ "\'ishttpsce\':" + isHttpSce + ", "
							+ "\'summarytype\':" + getInstance().m_summaryType + ", "
							+ "\'startdate\':\'" + selectStartDate + "\', "
							+ "\'enddate\':\'" + selectEndDate + "\', "
							+ "\'sliderstartdate\':\'" + getInstance().m_sliderStart + "\', "
							+ "\'sliderenddate\':\'" + getInstance().m_sliderEnd + "\', "
							+ "\'thresholdinfomin\':\'" + thresholdInfoMin + "\', "
							+ "\'thresholdinfomax\':\'" + thresholdInfoMax + "\', "
							+ "\'thresholdwarnmin\':\'" + thresholdWarnMin + "\', "
							+ "\'thresholdwarnmax\':\'" + thresholdWarnMax + "\' "
							+ "}";
					sb.append(param);
					sb.append(",");
					count++;
					if (count == ClusterControlPlugin.getDefault().getPreferenceStore().getInt(PerformancePreferencePage.P_GRAPH_MAX)) {
						allbreak = true;
						break;
					}
				}
				if (allbreak) {
					break;
				}
			}
			if (allbreak) {
				break;
			}
		}
		if (sb.length() == 0) {
			m_log.debug("getAddGraphPlotJson() plot data 0.");
			return null;
		}
		String appendfirst = "{\'all\':[";
		sb.insert(0, appendfirst);
		sb.deleteCharAt(sb.length()-1);
		sb.append("],");
		sb.append("\'eventflaginfo\':[" + getEventFlagInfo() + "], ");
		sb.append(orderItemInfoSelection());
		sb.append("}");
		return sb;
	}
	
	/**
	 * メンバで保持している情報から、引数で指定されたmonitorIdとmanagerNameに対応するfacilityId-collectIDのMapを返却します
	 * @param itemCode
	 * @return
	 */
	private static Map<String, Integer> getFacilityCollectMap(String itemDisplayNameMonitorId, String managerName) {
		List<Integer> collectIdList = getInstance().m_managerMonitorCollectIdMap.get(managerName).get(itemDisplayNameMonitorId);
		Map<String, List<Integer>> facilityCollectMap = getInstance().m_targetManagerFacilityCollectMap.get(managerName);
		TreeMap<String, Integer> retMap = new TreeMap<String, Integer>();
		if (facilityCollectMap != null && !facilityCollectMap.isEmpty()) {
			for (Map.Entry<String, List<Integer>> entry : facilityCollectMap.entrySet()) {
				String facilityId = entry.getKey();
				List<Integer> collectIdList1 = entry.getValue();
				boolean search = false;
				if (collectIdList != null && !collectIdList.isEmpty()) {
					for (Integer collectId : collectIdList) {
						if (collectIdList1.contains(collectId)) {
							retMap.put(facilityId, collectId);
							search = true;
							break;
						}
					}
				}
				// 指定されたitemDisplayNameMonitorIdとファシリティに関連するcollectIDが存在しない場合はnullをいれる
				// グラフをグレー表示にするため
				if (!search) {
					retMap.put(facilityId, null);
				}
			}
		}
		return retMap;
	}

	/**
	 * 初期処理を行います。
	 * 
	 * メンバで抑えている情報の初期化
	 * 
	 * @param totalflg 初期値
	 * @param stackflg 初期値
	 * @param collectKeyInfoList 初期値
	 * @param pieflg 初期値
	 * @param scatterflg 初期値
	 * @param barFlg 初期値
	 * @param selectInfoStr 初期値
	 */
	public static void init(boolean totalflg, boolean stackflg, List<CollectKeyInfoPK> collectKeyInfoList, 
			boolean pieflg, boolean scatterflg, boolean barFlg, String selectInfoStr) {
		m_log.debug("init()");
		getInstance().totalFlg = totalflg;
		getInstance().stackFlg = stackflg;
		getInstance().m_targetManagerFacilityCollectMap.clear();
		getInstance().m_managerFacilityIdNameMap.clear();
		getInstance().m_managerFacilityIdDummyNameMap.clear();
		getInstance().m_managerDummyNameMap.clear();
		getInstance().m_managerMonitorCollectIdMap.clear();
		getInstance().m_collectKeyInfoList.clear();
		getInstance().m_collectKeyInfoList = collectKeyInfoList;
		getInstance().pieFlg = pieflg;
		getInstance().scatterFlg = scatterflg;
		getInstance().barFlg = barFlg;
		getInstance().selectInfoStr = selectInfoStr;
	}

	/**
	 * マネージャ名とファシリティ名のマップを作成する
	 * @param item
	 * @param count
	 * @return ファシリティの数
	 */
	public static int sortManagerNameFacilityIdMap(FacilityTreeItem item, int count){
		int size = 0;
		ArrayList<String> targetConditionFacilityIdList = new ArrayList<String>();
		targetConditionFacilityIdList.clear();
		if (item.getData().getFacilityType() == FacilityConstant.TYPE_COMPOSITE) {
			// 最上位の[スコープ]はマネージャ名が無いので呼び出し元に戻る
			return 0;
		}
		String managerName = ScopePropertyUtil.getManager(item).getData().getFacilityId();
		String facilityId = item.getData().getFacilityId();
		String facilityName = item.getData().getFacilityName();
		if (item.getData().getFacilityType() == FacilityConstant.TYPE_NODE) {
			getInstance().m_managerFacilityIdNameMap.put(managerName + SQUARE_SEPARATOR + facilityId, facilityName);
			getInstance().m_managerFacilityIdDummyNameMap.put(managerName + SQUARE_SEPARATOR + facilityId, "dummy_" + count);
		}
		m_log.debug("sortManagerNameFacilityIdMap() managerName:" + managerName + ", facilityId:" + facilityId + ", facilityName:" + facilityName);
		switch (item.getData().getFacilityType()) {

		case FacilityConstant.TYPE_NODE:
			// 指定したノードだけ
			targetConditionFacilityIdList.add(facilityId);
			addTargetFacilityIdMap(managerName, facilityId);
			break;

		case FacilityConstant.TYPE_SCOPE:
		case FacilityConstant.TYPE_MANAGER:
			// 指定したスコープ配下に含まれる全てのノードを対象
			setTargetNodeFacilityId(item, targetConditionFacilityIdList);
			break;

		default: // 既定の対処はスルー。
			break;
		}
		size = getInstance().m_managerFacilityIdNameMap.size();
		return size;
	}

	/**
	 * FacilityTreeItemに所属する全てのノード(スコープではない)のファシリティIDをArrayListに設定します
	 *
	 * @param treeItem
	 * @param facilityIdList
	 */
	private static void setTargetNodeFacilityId(FacilityTreeItem treeItem, ArrayList<String> facilityIdList) {
		if (facilityIdList == null) {
			m_log.warn("facilityIdList is null");
			return;
		}
		m_log.trace("setTargetNodeFacilityId() treeItem = " + treeItem.getData().getFacilityId());
		List<FacilityTreeItem> children = treeItem.getChildren();

		for(FacilityTreeItem item : children){
			switch (item.getData().getFacilityType()) {
			case FacilityConstant.TYPE_NODE:
				facilityIdList.add(item.getData().getFacilityId());
				addTargetFacilityIdMap(ScopePropertyUtil.getManager(item).getData().getFacilityId(), item.getData().getFacilityId());
				break;

			case FacilityConstant.TYPE_SCOPE:
				setTargetNodeFacilityId(item, facilityIdList);
				break;

			default:
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param managerName
	 * @param facilityId
	 */
	private static void addTargetFacilityIdMap(String managerName, String facilityId) {
		if (getInstance().m_targetManagerFacilityCollectMap.containsKey(managerName)) {
			// マネージャが存在する
			Map<String, List<Integer>> facilityIdMap = getInstance().m_targetManagerFacilityCollectMap.get(managerName);
			if (!facilityIdMap.containsKey(facilityId)) {
				// ファシリティIDは存在しないため、新たにmapをputする
				m_log.trace("addTargetFacilityIdMap() targetFacilityIdMap.GetAdd containsKey:" + managerName + ", faciliyId:" + facilityId);
				// マネージャを選択すると、配下に同じFacilityIdが存在するため重複排除
				facilityIdMap.put(facilityId, new ArrayList<Integer>());
			}
		} else {
			// マネージャが存在しないので、新規にputする
			TreeMap<String, List<Integer>> facilityIdMap = new TreeMap<String, List<Integer>>();
			// この時点ではidListは不明のため、newを入れておく
			facilityIdMap.put(facilityId, new ArrayList<Integer>());
			getInstance().m_targetManagerFacilityCollectMap.put(managerName, facilityIdMap);
			m_log.trace("addTargetFacilityIdMap() targetFacilityIdMap.Put newKey:" + managerName + ", faciliyId:" + facilityId);
		}
		
	}

	/**
	 * 選択されたファシリティが所属するマネージャの数を返します。
	 * 
	 * @return マネージャ数
	 */
	public static int getSelectManagerCount() {
		int count = getInstance().m_targetManagerFacilityCollectMap.size();
		m_log.debug("マネージャ数:" + count);
		return count;
	}
	
	/**
	 * グラフのベースとなる部分の描画情報を作成します。
	 * 初回のみ呼ばれます。
	 * @param summaryType
	 * @return
	 */
	public static String drawGraphSheets(int summaryType) {
		long start = System.currentTimeMillis();
		m_log.debug("drawGraphSheets()");
		Long nowDate = System.currentTimeMillis();

		long formatTerm = MILLISECOND_MONTH;
		if (getInstance().m_targetConditionEndDate == null && getInstance().m_targetConditionStartDate == null) {
			// 取得時刻がnullの場合は、初回グラフ表示時(画面上に何もない状態)なので、開始時刻に現在時間-1h、終了時刻に現在時刻をいれる
			m_log.debug("drawGraphSheets() first draw.");
			getInstance().m_targetConditionEndDate = nowDate;
			
			// 指定されたサマリタイプ別に取得期間を変更する
			switch (summaryType) {
				case SummaryTypeConstant.TYPE_AVG_HOUR :
				case SummaryTypeConstant.TYPE_MIN_HOUR :
				case SummaryTypeConstant.TYPE_MAX_HOUR :
					// 「時」の場合は、取得期間を「1日」、スライダーは「1週間」
					getInstance().m_targetConditionStartDate = nowDate - MILLISECOND_DAY;
					formatTerm = MILLISECOND_WEEK;
					break;
				
				case SummaryTypeConstant.TYPE_AVG_DAY:
				case SummaryTypeConstant.TYPE_MIN_DAY :
				case SummaryTypeConstant.TYPE_MAX_DAY :
					// 「日」の場合は、取得期間を「1週間」、スライダーは「1ヶ月」
					getInstance().m_targetConditionStartDate = nowDate - MILLISECOND_WEEK;
					formatTerm = MILLISECOND_MONTH;
					break;

				case SummaryTypeConstant.TYPE_AVG_MONTH :
				case SummaryTypeConstant.TYPE_MIN_MONTH :
				case SummaryTypeConstant.TYPE_MAX_MONTH :
					// 「月」の場合は、取得期間を「1年」、スライダーは「10年」
					getInstance().m_targetConditionStartDate = nowDate - MILLISECOND_YEAR;
					formatTerm = MILLISECOND_10YEAR;
					break;

				default :// raw
					// 「ロー」の場合は、取得期間を「1時間」、スライダーは「1日」
					getInstance().m_targetConditionStartDate = nowDate - MILLISECOND_HOUR;
					formatTerm = MILLISECOND_DAY;
					break;
			}

		} else {
			m_log.debug("drawGraphSheets() already draw.");
			// 収集種別、サマリ、表示期間がすでに操作されている場合は、前回情報を元にする
			
			// 期間が1日以上でローが選択されている場合は、指定されている期間の中心時間から前後12時間にする(取得時間の短縮)
			if (isSelectTermOverDay() && summaryType == SummaryTypeConstant.TYPE_RAW) {
				long center = (getSelectTermDiff()/2) + getInstance().m_targetConditionStartDate;
				getInstance().m_targetConditionStartDate = center - (MILLISECOND_DAY/2);
				getInstance().m_targetConditionEndDate = center + (MILLISECOND_DAY/2);
				m_log.debug("drawGraphSheets() summaryType:raw, term over day. shortcut term. center:" + center);
			}
			// 前回の表示期間の取得、今回も同じ期間にする
			formatTerm = getInstance().m_sliderEnd - getInstance().m_sliderStart;
			
		}
		// 表示期間を真ん中に持ってくる
		long centerDate = getTargetConditionStartDate() 
				+ (getTargetConditionEndDate() - getTargetConditionStartDate()) /2;
		getInstance().m_sliderStart = centerDate - (formatTerm / 2);
		getInstance().m_sliderEnd = centerDate + (formatTerm / 2);
		
		// グラフのベース部の文字列作成
		ArrayList<String> plotJsonList = new ArrayList<>();
		int countgraph = 0;
		int dispCount = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(PerformancePreferencePage.P_GRAPH_MAX);
		boolean allbreak = false;
		boolean itembreak = false;
		for (CollectKeyInfoPK collectKeyInfoPK : getInstance().m_collectKeyInfoList) {
			// manager数分ループ
			for (Map.Entry<String, TreeMap<String, List<Integer>>> entry : getInstance().m_targetManagerFacilityCollectMap.entrySet()) {
				String managerName = entry.getKey();
				Map<String, List<Integer>> facilityCollectMap = entry.getValue();
				for (Map.Entry<String, List<Integer>> faci_entry : facilityCollectMap.entrySet()) {
					String facilityId = faci_entry.getKey();
					// 線情報をaddする
					String params = createBaseGraphDiv(
							managerName, collectKeyInfoPK.getItemName(), collectKeyInfoPK.getMonitorId(), collectKeyInfoPK.getDisplayName(), facilityId);
					plotJsonList.add(params);
					if (getInstance().totalFlg) {
						// まとめフラグがonの場合は、itemcodeに対して1つしかベースは作成しないため、breakする
						countgraph += facilityCollectMap.size();
						if (countgraph >= dispCount) {
							allbreak = true;
							break;
						}
						itembreak = true;
						break;
					}
					countgraph++;
					if (countgraph >= dispCount) {
						allbreak = true;
						break;
					}
				}
				if (allbreak) {
					break;
				}
				if (itembreak) {
					break;
				}
			}
			if (allbreak) {
				break;
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for (String plotStr : plotJsonList) {
			sb.append(plotStr);
			sb.append(",");
		}
		sb.insert(0, "{\'all\':[");
		if (plotJsonList.size() > 0) {
			sb.deleteCharAt(sb.length() - 1); // 「,」を消す
		}
		
		sb.append("], ");
		sb.append(orderItemInfoSelection());
		sb.append("}");
		m_log.info("createDrawGraphs() time:" + (System.currentTimeMillis() - start) + "ms");
		
		
		return sb.toString();
	}
	
	/**
	 * 監視項目種別IDの選択順番を文字列で返します
	 * 
	 * @return
	 */
	private static String orderItemInfoSelection() {
		String selectInfoStr = getInstance().selectInfoStr.substring(0, getInstance().selectInfoStr.length()-1);
		String selectInfoArr[] = selectInfoStr.split(SQUARE_SEPARATOR);
		StringBuffer sb = new StringBuffer();
		int count = 0;
		for (String select : selectInfoArr) {
			for (CollectKeyInfoPK collectInfo : getInstance().m_collectKeyInfoList) {
				String itemNameLocale = HinemosMessage.replace(collectInfo.getItemName());
				if (!collectInfo.getDisplayName().equals("") 
						&& !itemNameLocale.endsWith("[" + collectInfo.getDisplayName() + "]")) {
					itemNameLocale += "[" + collectInfo.getDisplayName() + "]";
				}
				String str = itemNameLocale + "(" + collectInfo.getMonitorId() + ")";
				if (str.equals(select)) {
					String param =
						"{"
						+ "\'count\':" + count + ", "
						+ "\'itemname\':\'" + itemNameLocale + "\', "
						+ "\'monitorid\':\'" + collectInfo.getMonitorId() + "\', "
						+ "\'displayname\':\'" + collectInfo.getDisplayName() + "\'"
						+ "},";
					sb.append(param);
					count++;
					break;
				}
			}
		}
		String ret = "\'orderitem\':[" + sb.substring(0, sb.toString().length() - 1) + "]";
		m_log.debug("orderItemInfoSelection() ret:" + ret);
		return ret;
	}
	
	/**
	 * collectIdの取得とメンバに保持
	 * @param summaryType
	 * @throws InvalidRole_Exception 
	 * @throws HinemosUnknown_Exception 
	 * @throws InvalidUserPass_Exception 
	 * @throws Exception 
	 */
	public static void collectCollectIdInfo(int summaryType) throws InvalidUserPass_Exception, HinemosUnknown_Exception, InvalidRole_Exception {
		long start2 = System.currentTimeMillis();

		getInstance().m_summaryType = summaryType;
		int count = 0;
		int dispCount = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(PerformancePreferencePage.P_GRAPH_MAX);
		// itemCode数分ループ
		for (CollectKeyInfoPK collectKeyInfoPK : getInstance().m_collectKeyInfoList) {
			// manager数分ループ
			for (Map.Entry<String, TreeMap<String, List<Integer>>> entry : getInstance().m_targetManagerFacilityCollectMap.entrySet()) {
				Map<String, List<Integer>> facilityIdMap = entry.getValue();
				String managerName = entry.getKey();
				List<String> facilityIdList = new ArrayList<String>();
				
				for (Map.Entry<String, List<Integer>> facientry : facilityIdMap.entrySet()) {
					facilityIdList.add(facientry.getKey());
					count++;
					if (count == dispCount) {
						break;
					}
				}
				
				// collectorIdの取得&メンバに保持
				getFacilityCollectIdMap(collectKeyInfoPK.getItemName(), collectKeyInfoPK.getDisplayName(), 
						collectKeyInfoPK.getMonitorId(), managerName, facilityIdList);
				if (count == dispCount) {
					break;
				}
			}
			if (count == dispCount) {
				break;
			}
		}
		m_log.info("collectCollectIdInfo() time:" + (System.currentTimeMillis() - start2) + "ms");
	}
	
	/**
	 * DBから対象のファシリティIDのCollectIDリストを取得します。
	 * 適用押下時にしか使用しません。
	 * @param itemName
	 * @param displayName
	 * @param monitorId
	 * @param managerName
	 * @param facilityIdList
	 * @throws InvalidRole_Exception 
	 * @throws HinemosUnknown_Exception 
	 * @throws InvalidUserPass_Exception 
	 * @throws Exception 
	 */
	private static void getFacilityCollectIdMap(String itemName, String displayName, String monitorId, String managerName, List<String> facilityIdList)
			throws InvalidUserPass_Exception, HinemosUnknown_Exception, InvalidRole_Exception {
		long start = System.currentTimeMillis();
		String itemNameMess = HinemosMessage.replace(itemName);
		CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
		HashMapInfo mapInfo = wrapper.getCollectId(itemName, displayName, monitorId, facilityIdList);
		Map4 map4 = mapInfo.getMap4();

		if (!getInstance().m_managerMonitorCollectIdMap.containsKey(managerName)) {
			getInstance().m_managerMonitorCollectIdMap.put(managerName, new HashMap<String, List<Integer>>());
		}
		if (!getInstance().m_managerMonitorCollectIdMap.get(managerName).containsKey(itemNameMess + displayName + monitorId)) {
			getInstance().m_managerMonitorCollectIdMap.get(managerName).put(itemNameMess + displayName + monitorId, new ArrayList<Integer>());
		}

		for (com.clustercontrol.ws.collect.HashMapInfo.Map4.Entry entry4 : map4.getEntry()) {
			String facilityId = entry4.getKey();
			Integer collectId = entry4.getValue();
			m_log.info("getFacilityCollectIdMap()  itemName:" + itemName + ", itemNameMess:"+ itemNameMess +", displayName:" + displayName 
					+ ", monitorId:" + monitorId + ", managerName:" + managerName + ", facilityId:" + facilityId + ", collectId:" + collectId);
			if (collectId == null) {
				continue;
			}
			// グラフ移動時に再度DBアクセスするため、メンバにも保持する
			List<Integer> collectList = getInstance().m_managerMonitorCollectIdMap.get(managerName).get(itemNameMess + displayName + monitorId);
			List<Integer> collectIdList1 = getInstance().m_targetManagerFacilityCollectMap.get(managerName).get(facilityId);
			collectIdList1.add(collectId);
			collectList.add(collectId);
		}
		m_log.debug("getFacilityCollectIdMap() DB time=" + (System.currentTimeMillis() - start) + "ms");
	}
	
	/**
	 * DBから対象のグラフ情報を取得します。<br />
	 * 画面いっぱいにグラフを表示させるために表示範囲以上の情報を取得します。<br>
	 * 選択されたサマリタイプによって前後時間に余裕を持たせます。<br>
	 * DBから取得する際にはfacilityIdではなくCollectIdを元に取得します。<br>
	 * 
	 * @param managerName
	 * @param monitorId
	 * @param displayName
	 * @param itemName
	 * @param fromTime 開始時刻
	 * @param toTime 終了時刻
	 * @return key:collectId, value:CollectDataのリスト
	 * @throws InvalidRole_Exception 
	 * @throws HinemosUnknown_Exception 
	 * @throws InvalidUserPass_Exception 
	 */
	private static Map<Integer, List<CollectData>> getGraphDetailDataMap(String managerName, String monitorId, String displayName, 
			String itemName, Long fromTime, Long toTime) throws InvalidUserPass_Exception, HinemosUnknown_Exception, InvalidRole_Exception {
		long start = System.currentTimeMillis();
		SimpleDateFormat DF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		DF.setTimeZone(TimezoneUtil.getTimeZone());
		m_log.info("getGraphDetailDataMap() managerName:" + managerName + ", monitorId:" + monitorId + ", displayName:" + displayName + ", itemName:" + itemName
				+ ", fromTime:" + DF.format(new Date(fromTime)) + ", toTime:" + DF.format(new Date(toTime)) + ", summaryType:" + getInstance().m_summaryType);
		Map<Integer, List<CollectData>> map = new HashMap<>();
		List<Integer> collectIdList = null;
		if (getInstance().m_managerMonitorCollectIdMap.get(managerName) == null) {
			m_log.warn("指定したmanagerに対応するリストが無い managerName" + managerName);
		} else {
			collectIdList = getInstance().m_managerMonitorCollectIdMap.get(managerName).get(itemName + displayName + monitorId);
		}
		if (collectIdList == null || collectIdList.isEmpty()) {
			m_log.info("collectIdListが空なのでDBに収集値をとりに行かない managerName:" + managerName);
			return map;
		}

		// 画面いっぱいにグラフを表示させるために表示範囲以上に情報を取得する
		// 画面で表示中のデータと今回取得するデータで重複するところがあるが、javascriptで重複排除する
		if (getInstance().m_summaryType == SummaryTypeConstant.TYPE_RAW) {
			// nop
		} else if (getInstance().m_summaryType == SummaryTypeConstant.TYPE_AVG_HOUR 
				|| getInstance().m_summaryType == SummaryTypeConstant.TYPE_MIN_HOUR 
				|| getInstance().m_summaryType == SummaryTypeConstant.TYPE_MAX_HOUR) {
			fromTime -= MILLISECOND_HOUR;
			toTime += MILLISECOND_HOUR;
		} else if (getInstance().m_summaryType == SummaryTypeConstant.TYPE_AVG_DAY
				|| getInstance().m_summaryType == SummaryTypeConstant.TYPE_MIN_DAY
				|| getInstance().m_summaryType == SummaryTypeConstant.TYPE_MAX_DAY) {
			fromTime -= MILLISECOND_DAY;
			toTime += MILLISECOND_DAY;
		} else if (getInstance().m_summaryType == SummaryTypeConstant.TYPE_AVG_MONTH
				|| getInstance().m_summaryType == SummaryTypeConstant.TYPE_MIN_MONTH
				|| getInstance().m_summaryType == SummaryTypeConstant.TYPE_MAX_MONTH) {
			fromTime -= MILLISECOND_MONTH;
			toTime += MILLISECOND_MONTH;
		}
		m_log.debug("getGraphDetailDataMap() REAL fromTime:" + DF.format(new Date(fromTime)) + ", toTime:" + DF.format(new Date(toTime)));

		CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
		HashMapInfo mapInfo = wrapper.getCollectData(collectIdList, getInstance().m_summaryType, fromTime, toTime);
		Map3 map3 = mapInfo.getMap3();
		int count = 0;
		for (com.clustercontrol.ws.collect.HashMapInfo.Map3.Entry entry3 : map3.getEntry()) {
			// keyがcollectId、valueは日時と測定値が入ったクラス(CollectData)のリスト
			map.put(entry3.getKey(), entry3.getValue().getList());
			count += entry3.getValue().getList().size();
		}
		m_log.debug("getGraphDetailDataMap : monitorId=" + monitorId + ", size=" + count + ", collectIdList.size:" + collectIdList.size());
		m_log.debug("getGraphDetailDataMap() DB time=" + (System.currentTimeMillis() - start) + "ms");
		return map;
	}

	/**
	 * 引数で指定されたcollectIdMap(facilityId, collectId>)とcollectDataMap(collectId, CollectData(timeList,valueList))から
	 * プロットデータを作成します。<br>
	 * 戻り値のStringは、以下のような形式のため、呼び出し元でsplitが必要<br>
	 * <br>
	 * facilityId#collectId#plotData<br>
	 * <br>
	 * facilityId    : ファシリティID<br>
	 * collectId     : 収集ID<br>
	 * plotData      : 座標情報([[x1,y1],[x2,y2],[x3,y3],･･･[xn,yn]])<br>
	 * <br>
	 * @param collectIdMap
	 * @param collectDataMap
	 * @param managerName
	 * @return
	 */
	private static List<String> parseGraphData(Map<String, Integer> collectIdMap, Map<Integer, List<CollectData>> collectDataMap) {
		List<String> plotDataList = new ArrayList<String>();
		Long start = System.currentTimeMillis();
		for (Map.Entry<String, Integer> entryFaci : collectIdMap.entrySet()) {
			String facilityId = entryFaci.getKey();
			Integer collectId = entryFaci.getValue();
			StringBuffer sb = new StringBuffer();
			if (collectId == null) {
				// collectIdがnullになるのはまだ収集が行われていない場合など、その場合は空データを作成
				m_log.debug("parseGraphData() collectId == null. facilityId:" + facilityId);
			} else {
				List<CollectData> dataList = collectDataMap.get(collectId);
				if (dataList != null && !dataList.isEmpty()) {
					m_log.info("parseGraphData() facilityId:" + facilityId + ", collectId:" + collectId + ", plotsize:" + dataList.size());
					for (CollectData data : dataList) {
						Long date = data.getTime();
						Float value = data.getValue();
						if (date == null || value == null) {
							continue;
						}
						sb.append("[" + date + "," + value + "],");
					}
				} else {
					// collectidはあるが、選択期間にデータがない場合
					m_log.info("parseGraphData() facilityId:" + facilityId + ", collectId:" + collectId + ", collectId is not null, but detailInfo is null");
				}
			}
			String plotData = "";
			if (collectId != null) {
				String detaildata = sb.toString();
				if (detaildata != null && detaildata.length() != 0) {
					detaildata = detaildata.substring(0, detaildata.length()-1);
				}
				plotData = facilityId + SQUARE_SEPARATOR + collectId + SQUARE_SEPARATOR 
						+ "[" + detaildata + "]";
			} else {
				// グラフ数制限のズレ対処のため、collectIdがない場合はデータなしでaddする
				plotData = facilityId + SQUARE_SEPARATOR + "none" + SQUARE_SEPARATOR + "[]";// collectId=0はありえないパターン
			}
			// プロットデータと閾値データを入れる
			plotDataList.add(plotData);
		}
		m_log.debug("parseGraphData() time=" + (System.currentTimeMillis() - start) + "ms");
		return plotDataList;
	}
	
	/**
	 * 画面にdivを追加し、グラフを追加します。
	 * 初期起動時にしか使えません。
	 * @param managerName
	 * @param itemName(未変換、コード値)
	 * @param monitorId
	 * @param displayName
	 * @param facilityId
	 * @return
	 */
	private static String createBaseGraphDiv(String managerName, String itemName, String monitorId, String displayName, String facilityId){
		long start = System.currentTimeMillis();
		String facilityName = getInstance().m_managerFacilityIdNameMap.get(managerName + SQUARE_SEPARATOR + facilityId);
		String facilityDummyId = getInstance().m_managerFacilityIdDummyNameMap.get(managerName + SQUARE_SEPARATOR + facilityId);
		String managerDummyName = getInstance().m_managerDummyNameMap.get(managerName);
		itemName = HinemosMessage.replace(itemName);
		if (!displayName.equals("") && !itemName.endsWith("[" + displayName + "]")) {
			itemName += "[" + displayName + "]";
		}
		m_log.debug("createBaseGraphDiv() managerName:" + managerName + ", itemName:" + itemName 
				+ ", facilityId:" + facilityId + ", facilityName:" + facilityName);
		// idをgetAddGraphPlotJson関数で作成したもの(groupId)と同じにすると、線が描画・追加描画される
		String id = itemName + "_" + monitorId;
		if (!getInstance().totalFlg) {
			id = facilityDummyId + "_" + managerDummyName + "_" + itemName + "_" + monitorId;
		}
		String param =
				"{"
				+ "\'managername\':\'" + managerDummyName + "\', "// DUMMY
				+ "\'facilityid\':\'" + facilityDummyId + "\', " // DUMMY
				+ "\'realmanagername\':\'" + managerName + "\', "
				+ "\'realfacilityid\':\'" + facilityId + "\', "
				+ "\'itemname\':\'" + itemName + "\', "
				+ "\'monitorid\':\'" + monitorId + "\', "
				+ "\'displayname\':\'" + displayName + "\', "
				+ "\'facilityname\':\'" + facilityName + "\', "
				+ "\'ylabel\':\'" + itemName + "(" + monitorId + ")\', "
				+ "\'id\':\'" + id + "\', "
				+ "\'summarytype\':" + getInstance().m_summaryType + ", "
				+ "\'sliderstartdate\':" + getInstance().m_sliderStart + ", " 
				+ "\'sliderenddate\':" + getInstance().m_sliderEnd + ", " 
				+ "\'startdate\':" + getInstance().m_targetConditionStartDate + ", " 
				+ "\'enddate\':" + getInstance().m_targetConditionEndDate 
				+ "}";

		m_log.debug("createBaseGraphDiv() time=" + (System.currentTimeMillis() - start) + "ms");
		return param;
	}
	
	/**
	 * メンバで保持しているm_targetConditionEndDateとm_targetConditionStartDateの時間差が1日以上ある場合は、trueを返します。
	 * それ以外はfalseを返します。
	 * 画面初期起動時は、falseを返します。
	 * 
	 * @return
	 */
	public static boolean isSelectTermOverDay() {
		long selectTerm = getSelectTermDiff();
		if (selectTerm > MILLISECOND_DAY) {
			return true;
		}
		return false;
	}
	
	/**
	 * メンバで保持しているm_targetConditionEndDateとm_targetConditionStartDateの時間差をLongで返します。
	 * 画面初期起動時は、0を返します。
	 * 
	 * @return
	 */
	public static Long getSelectTermDiff() {
		if (getInstance().m_targetConditionEndDate == null || getInstance().m_targetConditionStartDate == null) {
			return 0L;
		}
		return getInstance().m_targetConditionEndDate - getInstance().m_targetConditionStartDate;
	}
	
	/**
	 * 指定されたマネージャ名、monitoridを元に閾値情報と単位と100%表示可否を取得し、以下の形式で文字列で返します。<br>
	 * 文字列形式：thresholdInfoMin,thresholdInfoMax,thresholdWarnMin,thresholdWarnMax,単位,100%表示可否,HTTPシナリオかどうか<br>
	 * monitorIdに対応するMonitorInfoが存在しない場合は、nullを返します。<br>
	 * 
	 * @param managerName
	 * @param monitorId
	 * @return
	 */
	public static String getThresholdData(String managerName, String monitorId) {
		Double thresholdInfoMin = 0d;
		Double thresholdInfoMax = 0d;
		Double thresholdWarnMin = 0d;
		Double thresholdWarnMax = 0d;
		String measure = "";
		String pluginId = "";
		boolean isGraphRange = false;
		boolean isHttpSce = false;
		String thresholdStr = null;
		try {
			MonitorSettingEndpointWrapper monitorWrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			MonitorInfo monitorInfo = monitorWrapper.getMonitor(monitorId);
			measure = HinemosMessage.replace(monitorInfo.getMeasure());
			pluginId = monitorInfo.getMonitorTypeId();
			List<MonitorNumericValueInfo> thresholdValue = monitorInfo.getNumericValueInfo();
			for (MonitorNumericValueInfo thresholdInfo : thresholdValue) {
				if (thresholdInfo.getPriority().equals(PriorityConstant.TYPE_WARNING)) {
					thresholdWarnMax = thresholdInfo.getThresholdUpperLimit();
					thresholdWarnMin = thresholdInfo.getThresholdLowerLimit();
				} else if (thresholdInfo.getPriority().equals(PriorityConstant.TYPE_INFO)) {
					thresholdInfoMax = thresholdInfo.getThresholdUpperLimit();
					thresholdInfoMin = thresholdInfo.getThresholdLowerLimit();
				}
			}
			if (pluginId.equals(HinemosModuleConstant.MONITOR_PING)) {
				thresholdInfoMax = thresholdInfoMin;
				thresholdWarnMax = thresholdWarnMin;
				thresholdInfoMin = 0d;
				thresholdWarnMin = 0d;
			}
			// 100%表示可否を取得する
			// (CollectKeyInfoPKの中のitemNameはdisplaynameがついているのでequalsでヒットしない、monitorInfo.itemNameで比較)
			isGraphRange = isGraphRangePercent(managerName, monitorInfo.getItemName());
			
			// HTTP監視(シナリオ)かどうか
			// HTTP監視(シナリオ)の場合は、上限下限表示を無効・円グラフと積み上げ棒グラフは値が表示されないようにする
			if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_HTTP_SCENARIO)) {
				isHttpSce = true;
			}
			thresholdStr = thresholdInfoMin + "," + thresholdInfoMax + "," + thresholdWarnMin + "," + thresholdWarnMax + ","
			+ measure + "," + pluginId + "," + isGraphRange + "," + isHttpSce;
		} catch (com.clustercontrol.ws.monitor.HinemosUnknown_Exception 
				| com.clustercontrol.ws.monitor.InvalidRole_Exception
				| com.clustercontrol.ws.monitor.InvalidUserPass_Exception e) {
			m_log.error("getThresholdData : " + e.getMessage());
			// nop
		} catch (MonitorNotFound_Exception e) {
			m_log.info("getThresholdData : " + e.getMessage());
			// nop 複数マネージャで選択した収集値項目名がほかのマネージャに登録されている場合、ここを通る
		}
		m_log.info("getThresholdData() managerName=" + managerName + ", monitorId=" + monitorId + ", thresholdStr=" + thresholdStr);
		return thresholdStr;
	}
	
	/**
	 * イベントの旗立て情報を取得します。
	 * 
	 * @return
	 */
	private static String getEventFlagInfo() {
		
		StringBuffer sb = new StringBuffer();
		List<String> managerList = new ArrayList<String>(getInstance().m_targetManagerFacilityCollectMap.keySet());
		try {
		
		for (String managerName : managerList) {
			Map<String, List<Integer>> facilityMap = getInstance().m_targetManagerFacilityCollectMap.get(managerName);
			List<String> facilityIdList = new ArrayList<String>(facilityMap.keySet());
			CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
			Map<String, List<EventDataInfo>> mapInfo;
				mapInfo = wrapper.getEventDataMap(facilityIdList);
				for (Entry<String, List<EventDataInfo>> entry : mapInfo.entrySet()) {
					List<EventDataInfo> eventInfoList = entry.getValue();
					for (EventDataInfo eventInfo : eventInfoList) {
//						String application = eventInfo.getApplication();
//						Boolean collectGraphFlg = eventInfo.isCollectGraphFlg();
//						String comment = eventInfo.getComment();
//						Long commentDate = eventInfo.getCommentDate();
//						String commentUser = eventInfo.getCommentUser();
//						Long confirmDate = eventInfo.getConfirmDate();
//						String confirmUser = eventInfo.getConfirmUser();
//						Integer confirmed = eventInfo.getConfirmed();
//						Integer duplicationCount = eventInfo.getDuplicationCount();
						String facilityId = eventInfo.getFacilityId();
						Long generationDate = eventInfo.getGenerationDate();
//						String managerName;
						String message = HinemosMessage.replace(eventInfo.getMessage());
//						String messageOrg = eventInfo.getMessageOrg();
						String monitorDetailId = eventInfo.getMonitorDetailId();
						String monitorId = eventInfo.getMonitorId();
						Long outputDate = eventInfo.getOutputDate();
//						String ownerRoleId = eventInfo.getOwnerRoleId();
						String pluginId = eventInfo.getPluginId();
						Integer priority = eventInfo.getPriority();
//						String scopeText = eventInfo.getScopeText();
						
						String facilityName = getInstance().m_managerFacilityIdNameMap.get(managerName + SQUARE_SEPARATOR + facilityId);
						String dummyFacilityId = getInstance().m_managerFacilityIdDummyNameMap.get(managerName + SQUARE_SEPARATOR + facilityId);
						String dummyManagerName = getInstance().m_managerDummyNameMap.get(managerName);
						String ret = "{\'managername\':\'" + dummyManagerName + "\', "// DUMMY
								+ "\'realmanagername\':\'" + managerName + "\', "// real
								+ "\'generationdate\':\'" + generationDate + "\', " // フラグが立つ日時
								+ "\'outputdate\':\'" + outputDate + "\', "// イベント詳細画面を開くために必要な出力日時
								+ "\'date\':\'" + generationDate + "\', "
								+ "\'message\':\'" + message + "\', "
								+ "\'monitorid\':\'" + monitorId + "\', "
								+ "\'displayname\':\'" + monitorDetailId + "\', "
								+ "\'priority\':\'" + PriorityMessage.typeToString(priority) + "\', "
								+ "\'eventdetailid\':\'" + monitorId + facilityId + outputDate + "\', " // graph original value
								+ "\'facilityname\':\'" + facilityName + "\', "
								+ "\'pluginid\':\'" + pluginId + "\', "
								+ "\'facilityid\':\'" + dummyFacilityId + "\', "
								+ "\'realfacilityid\':\'" + facilityId + "\'}";
						sb.append(ret);
						sb.append(",");

					}
				}
			
			}
		} catch (HinemosUnknown_Exception e) {
			m_log.error(e.getMessage());
			// sbを初期化しておく
			sb.delete(0, sb.length());
		}
		String ret = sb.toString();
		if (ret != null && ret.length() != 0) {
			ret = ret.substring(0, ret.length()-1);
		}

		m_log.info("eventFlagInfo:" + ret);
		return ret;
	}
	
	/**
	 * 指定されたitemNameのy軸を100%固定で表示するかどうかを返します。<br>
	 * true=100%固定表示する、false=100%固定表示しない
	 * 
	 * @param managerName
	 * @param itemName
	 * @return
	 */
	private static boolean isGraphRangePercent(String managerName, String itemName) {
		CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
		try {
			// ADMINISTRATORS権限がないと、invalidRoleになる
			List<CollectorItemCodeMstData> collectorItemCodeMstDataList = wrapper.getCollectItemCodeMasterList();
			
			for (CollectorItemCodeMstData collectItem : collectorItemCodeMstDataList) {
				if (collectItem.getItemName().equals(itemName)) {
					m_log.debug("isGraphRangePercent 100%表示対象 itemName:" + itemName);
					return collectItem.isGraphRange();
				}
			}
		} catch (HinemosUnknown_Exception
				| InvalidRole_Exception
				| InvalidUserPass_Exception e) {
			m_log.error("isGraphRangePercent : " + e.getMessage());
		}
		m_log.debug("isGraphRangePercent 100%表示対象外 itemName:" + itemName);
		return false;
	}
	
	/**
	 * マネージャ名からダミーマネージャ名を作成します。
	 */
	public static void addManagerDummyName() {
		int count = 0;
		for (String managerName : getInstance().m_targetManagerFacilityCollectMap.keySet()) {
			getInstance().m_managerDummyNameMap.put(managerName, "manager_" + count);
			count++;
		}
	}
	
	/**
	 * 引数で指定された閾値情報を元に、閾値情報クラスのリストを返します。
	 * 
	 * @param infoMinValue
	 * @param infoMaxValue
	 * @param warnMinValue
	 * @param warnMaxValue
	 * @return
	 */
	public static List<MonitorNumericValueInfo> createMonitorNumericValueInfoList(String infoMinValue, String infoMaxValue, 
			String warnMinValue, String warnMaxValue) {
		List<MonitorNumericValueInfo> monitorNumericValueInfoList = new ArrayList<>();
		MonitorNumericValueInfo monitorNumericValue_info = new MonitorNumericValueInfo();
		monitorNumericValue_info.setPriority(PriorityConstant.TYPE_INFO);
		monitorNumericValue_info.setThresholdLowerLimit(Double.valueOf(infoMinValue));
		monitorNumericValue_info.setThresholdUpperLimit(Double.valueOf(infoMaxValue));
		
		MonitorNumericValueInfo monitorNumericValue_warn = new MonitorNumericValueInfo();
		monitorNumericValue_warn.setPriority(PriorityConstant.TYPE_WARNING);
		monitorNumericValue_warn.setThresholdLowerLimit(Double.valueOf(warnMinValue));
		monitorNumericValue_warn.setThresholdUpperLimit(Double.valueOf(warnMaxValue));

		monitorNumericValueInfoList.add(monitorNumericValue_warn);
		monitorNumericValueInfoList.add(monitorNumericValue_info);
		
		return monitorNumericValueInfoList;
	}
	
	/**
	 * 現在のスライダーの範囲をLongで返します。
	 * @return
	 */
	public static Long getSliderTerm() {
		return getInstance().m_sliderEnd - getInstance().m_sliderStart;
	}
	
	/**
	 * 
	 * @return 現在表示中のグラフ表示の最古時間<br>(DBに取りに行った最古時間、これよりも古い時刻を表示する場合は取得しにいく)
	 */
	public static Long getTargetConditionStartDate() {
		return getInstance().m_targetConditionStartDate;
	}
	/**
	 * 現在表示中のグラフ表示の最古時間<br>(DBに取りに行った最古時間、これよりも古い時刻を表示する場合は取得しにいく)
	 * @param targetConditionStartDate
	 */
	public static void setTargetConditionStartDate(Long targetConditionStartDate) {
		getInstance().m_targetConditionStartDate = targetConditionStartDate;
	}
	/**
	 * 
	 * @return 現在表示中のグラフ表示の最新時間<br>(DBにとりに行った最新時間、現在時刻を越えない)
	 */
	public static Long getTargetConditionEndDate() {
		return getInstance().m_targetConditionEndDate;
	}
	/**
	 * 
	 * @param targetConditionEndDate 現在表示中のグラフ表示の最新時間<br>(DBにとりに行った最新時間、現在時刻を越えない)
	 */
	public static void setTargetConditionEndDate(Long targetConditionEndDate) {
		getInstance().m_targetConditionEndDate = targetConditionEndDate;
	}
	public static void setSummaryType(int summaryType) {
		getInstance().m_summaryType = summaryType;
	}
	public static int getSummaryType() {
		return getInstance().m_summaryType;
	}
	public static Long getSliderStart() {
		return getInstance().m_sliderStart;
	}
	public static void setSliderStart(Long sliderStart) {
		getInstance().m_sliderStart = sliderStart;
	}
	public static Long getSliderEnd() {
		return getInstance().m_sliderEnd;
	}
	public static void setSliderEnd(Long sliderEnd) {
		getInstance().m_sliderEnd = sliderEnd;
	}
	public static String getGraphZoomSize() {
		String zoom = getInstance().m_graphZoomSize;
		ClusterControlPlugin.getDefault().getPreferenceStore().setDefault(
				CollectSettingComposite.P_COLLECT_GRAPH_ZOOM_LEVEL, CollectSettingComposite.DEFAULT_COLLECT_GRAPH_ZOOM_LEVEL);
		if (zoom == null) {
			zoom = ClusterControlPlugin.getDefault().getPreferenceStore().getString(CollectSettingComposite.P_COLLECT_GRAPH_ZOOM_LEVEL);
			setGraphZoomSize(zoom);
		}
		return zoom;
	}
	public static void setGraphZoomSize(String zoomSize) {
		getInstance().m_graphZoomSize = zoomSize;
	}

	/**
	 * managerNameとfacilityIDとcollectIDのリスト(itemCode混合)を返します。
	 * @return
	 */
	public static TreeMap<String, TreeMap<String, List<Integer>>> getM_targetManagerFacilityCollectMap() {
		return getInstance().m_targetManagerFacilityCollectMap;
	}

	/**
	 * マネージャ名とitemName+displayName+monitorIdとcollectIdのリストを返します。
	 * (※itemNameはコードではない)
	 * @return
	 */
	public static TreeMap<String, Map<String, List<Integer>>> getM_managerMonitorCollectIdMap() {
		return getInstance().m_managerMonitorCollectIdMap;
	}
	/**
	 * マネージャ名とファシリティIDとファシリティ名のマップ (マネージャ名 + # + ファシリティID、ファシリティ名)の構造を返します。
	 * @return
	 */
	public static TreeMap<String, String> getM_managerFacilityIdNameMap() {
		return getInstance().m_managerFacilityIdNameMap;
	}
	
	/**
	 * 画面左で選択されたもの(itemNameとmonitorIdが入ってる)を返します。
	 * @return
	 */
	public static List<CollectKeyInfoPK> getM_collectKeyInfoList() {
		return getInstance().m_collectKeyInfoList;
	}

	public static int getM_screenWidth() {
		return getInstance().m_screenWidth;
	}
	
	public static void setM_screenWidth(int screenWidth) {
		getInstance().m_screenWidth = screenWidth;
	}
	
	public static boolean getTotalFlg() {
		return getInstance().totalFlg;
	}
	
	public static boolean getStackFlg() {
		return getInstance().stackFlg;
	}
	
	public static boolean getPieFlg() {
		return getInstance().pieFlg;
	}
	
	public static boolean getScatterFlg() {
		return getInstance().scatterFlg;
	}
	
	public static boolean getBarFlg() {
		return getInstance().barFlg;
	}
}
