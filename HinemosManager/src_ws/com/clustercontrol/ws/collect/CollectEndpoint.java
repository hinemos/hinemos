/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */
package com.clustercontrol.ws.collect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.collect.session.CollectControllerBean;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.session.PerformanceCollectMasterControllerBean;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.util.ArrayListInfo;
import com.clustercontrol.ws.util.HashMapInfo;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 性能用のWebAPIエンドポイント
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://collect.ws.clustercontrol.com")
public class CollectEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( CollectEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	
	private static boolean debug = false;//debug用のフラグ。trueでサンプルデータ、falseで実データを取得
	
	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}
	
	
	/**
	 * 
	 * ファシリティIDをキーとし、収集IDが格納されているHashMapを取得します
	 *
	 * CollectRead権限が必要
	 *
	 * @param itemCode 収集項目コード
	 * @param displayName 表示名(リソース監視)
	 * @param facilityIdList ファシリティIDのリスト
	 * @return ファシリティIDをキーとし、収集IDが格納されているHashMap
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public HashMapInfo getCollectId(String itemName, String displayName, String monitorId, List<String> facilityIdList) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getCollectId");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getCollectId, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		HashMap<String, Integer> map = new HashMap<>();
		//デバッグ用
		if(debug){
			for (String facilityId : facilityIdList) {
				int id = 0;
				id += itemName.hashCode();
				id *= 37;
				if (displayName != null) {
					id += displayName.hashCode();
				}
				id *= 37;
				id += facilityId.hashCode();
				map.put(facilityId, id); 
			}
		}else{
			//実際はこっち
			for(String facilityId : facilityIdList){
				m_log.debug("itemName:" + itemName + ", displayName:"+displayName + ", monitorId:" + monitorId + ", facilityId:" + facilityId);
				Integer id = null;
				try {
					id = new CollectControllerBean().getCollectId(itemName, displayName, monitorId, facilityId);
				} catch (Exception e) {
					m_log.debug(e.getClass().getName() + ", itemName:" + itemName + ", displayName:"+displayName + 
							", monitorId:" + monitorId + ", facilityId:" + facilityId);
				}
				map.put(facilityId, id);
			}
			
		}
		HashMapInfo ret = new HashMapInfo();
		ret.setMap4(map);
		
		return ret;
	}
	
	
	/**
	 * 
	 * 収集IDをキーとし、収集IDと時間とデータが格納されているHashMapを取得します
	 *
	 * CollectRead権限が必要
	 *
	 * @param idList 収集IDのリスト
	 * @param summaryType サマリタイプ
	 * @param fromTime 取得するデータの時間(起点)
	 * @param toTime 取得するデータの時間(終点)
	 * @return 収集IDをキーとし、収集IDと時間とデータが格納されているHashMap
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public HashMapInfo getCollectData (List<Integer> idList, Integer summaryType, Long fromTime, Long toTime) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getCollectData");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getCollectData, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		HashMap<Integer, ArrayListInfo> map = new HashMap<>();

		m_log.debug("getCollectData start"); // debug
		long start = HinemosTime.currentTimeMillis();
		//デバッグ用
		if(debug){
			int count = 0;
			int span = 60 * 1000;
				switch (summaryType) {
				case SummaryTypeConstant.TYPE_RAW :
					span *= 5; break; // 5分
				case SummaryTypeConstant.TYPE_AVG_HOUR: 
					span *= 12; break; // 1時間
				case SummaryTypeConstant.TYPE_MIN_HOUR: 
					span *= 12; break; // 1時間
				case SummaryTypeConstant.TYPE_MAX_HOUR: 
					span *= 12; break; // 1時間
				case SummaryTypeConstant.TYPE_AVG_DAY : 
					span *= 12 * 24; break; // 1日
				case SummaryTypeConstant.TYPE_MIN_DAY : 
					span *= 12 * 24; break; // 1日
				case SummaryTypeConstant.TYPE_MAX_DAY : 
					span *= 12 * 24; break; // 1日
				case SummaryTypeConstant.TYPE_AVG_MONTH : 
					span *= 12 * 24 * 30; break; // 1ヶ月
				case SummaryTypeConstant.TYPE_MIN_MONTH : 
					span *= 12 * 24 * 30; break; // 1ヶ月
				case SummaryTypeConstant.TYPE_MAX_MONTH : 
					span *= 12 * 24 * 30; break; // 1ヶ月
				default :
					break;
				}
			for (Integer id : idList) {
				ArrayListInfo list = new ArrayListInfo();
				// spanごとにランダムデータが入るように。
				long time = fromTime / span * span; // 分以下を切り捨て
				while (true) {
					time += span;
					if (toTime < time) {
						break;
					}
					double tmp = (Math.random()*10);
					CollectData data = new CollectData();
					data.setTime(time);
					data.setValue((float)tmp);
					list.getList().add(data);
					count ++;
					m_log.debug("id:" + id + ", time:" + time + ", value:" + tmp);
				}
				map.put(id, list);
			}
			try {
				Thread.sleep(count / 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			//実際はこっち
			CollectControllerBean controller = new CollectControllerBean();
			
			//欲しいサマリデータ、または収集データ(raw)のタイプでスイッチ
			switch(summaryType){
				case SummaryTypeConstant.TYPE_AVG_HOUR: {	
					ArrayListInfo list;
					List<SummaryHour> summaryList = controller.getSummaryHourList(idList, fromTime, toTime);
					for (SummaryHour summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
						}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_HOUR: {	
					ArrayListInfo list;
					List<SummaryHour> summaryList = controller.getSummaryHourList(idList, fromTime, toTime);
					for (SummaryHour summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMin());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_HOUR: {	
					ArrayListInfo list;
					List<SummaryHour> summaryHourList = controller.getSummaryHourList(idList, fromTime, toTime);
					for (SummaryHour summary : summaryHourList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMax());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_AVG_DAY: {
					ArrayListInfo list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(idList, fromTime, toTime);
					for (SummaryDay summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_DAY: {
					ArrayListInfo list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(idList, fromTime, toTime);
					for (SummaryDay summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMin());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_DAY: {
					ArrayListInfo list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(idList, fromTime, toTime);
					for (SummaryDay summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMax());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_AVG_MONTH: {
					ArrayListInfo list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(idList, fromTime, toTime);
					for (SummaryMonth summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_MONTH: {
					ArrayListInfo list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(idList, fromTime, toTime);
					for (SummaryMonth summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMin());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_MONTH: {
					ArrayListInfo list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(idList, fromTime, toTime);
					for (SummaryMonth summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMax());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				default: { // defaultはRAWとする
					ArrayListInfo list;
					List<CollectData> dataList = controller.getCollectDataList(idList, fromTime, toTime);
					for(CollectData data : dataList){
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
			}
		}
		
		if (m_log.isInfoEnabled()) { // debug
			int size = 0;
			for (Map.Entry<Integer, ArrayListInfo> entry : map.entrySet()) {
				size += entry.getValue().size();
			}
			long difftime = HinemosTime.currentTimeMillis() - start;
			if (difftime > 5 * 1000) {
				m_log.info("getCollectData end   size=" + size + ", " + difftime + "ms"); // debug
			}
		}
		HashMapInfo ret = new HashMapInfo();
		ret.setMap3(map);
		return ret;
	}
	
	
	/**
	 * 
	 * 収集項目コードのリストを取得します
	 *
	 * CollectRead権限が必要
	 *
	 * @return 収集項目コードのリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public List<CollectKeyInfoPK> getItemCodeList (List<String> facilityIdList) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getItemCodeList");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getItemCodeList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		List<CollectKeyInfoPK>ret = new CollectControllerBean().getItemCode(facilityIdList);
		return ret;
	}
	
	/**
	 * 性能実績のDLデータを作成する。
	 * 指定したファシリティIDがスコープの場合は、配下の全てのノードに対して1ファイルずつCSVファイルを作成する。
	 * 本メソッドが終了した時点で、Hinemosマネージャ上にファイルが作成され、そのファイルパスのリストを返却する
	 *
	 * CollectRead権限が必要
	 *
	 *
	 * @param facilityidList
	 * @param summaryType
	 * @param item_codeList
	 * @param header
	 * @param archive
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	
	public List<String> createPerfFile(HashMapInfo map1,
			List<CollectKeyInfoPK> collectKeyInfoList,
			List<String> facilityList,
			Integer summaryType,
			String localeStr,
			boolean header,
			String defaultDateStr) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("createPerfFile()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		List<String> ret = null;
		
		TreeMap<String, String>facilityIdNameMap = map1.getMap6();
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityIdNameMap=");
		msg.append(facilityIdNameMap);
		msg.append(", collectKeyInfoList=");
		msg.append(collectKeyInfoList.toString());
		msg.append(", facilityList=");
		msg.append(facilityList.toString());
		msg.append(", SummaryType=");
		msg.append(summaryType);
		msg.append(", LocaleStr=");
		msg.append(localeStr);
		msg.append(", Header=");
		msg.append(header);
		msg.append(", defaultDateStr=");
		msg.append(defaultDateStr);

		try {
			ret = new CollectControllerBean().createPerfFile(facilityIdNameMap, facilityList, collectKeyInfoList, 
					summaryType, localeStr, header, defaultDateStr);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=createPerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=createPerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		return ret;
	}

	/**
	 * 性能データのファイルをDLする
	 *
	 * CollectRead権限が必要
	 *
	 * @param filepath
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadPerfFile(String fileName) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		m_log.debug("downloadPerfFile() fileName = " + fileName);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(fileName);

		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr(
				"performance.export.dir", HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.PERFORMANCE_EXPORT_DIR));
		File file = new File(exportDirectory + fileName);
		if(!file.exists()) {
			m_log.info("file is not found : " + exportDirectory + fileName);
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=downloadPerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			return null;
		}
		m_log.info("file is found : " + exportDirectory + fileName);
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=downloadPerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		FileDataSource source = new FileDataSource(file);
		DataHandler dataHandler = new DataHandler(source);
		return dataHandler;
	}

	/**
	 * 性能データのファイルを削除する
	 *
	 * CollectREAD権限が必要
	 *
	 * @param filepathList
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deletePerfFile(ArrayList<String> fileNameList) throws HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("deletePerfFile() fileNameList.size = " + fileNameList.size());
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(Arrays.toString(fileNameList.toArray()));

		try {
			new CollectControllerBean().deletePerfFile(fileNameList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=deletePerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=deletePerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	public HashMapInfo getEventDataMap (ArrayList<String> facilityIdList) throws HinemosUnknown {
		HashMapInfo ret = new HashMapInfo();
		HashMap<String, ArrayListInfo> map = new HashMap<>();
		HashMap<String, ArrayList<EventDataInfo>> map1 = new CollectControllerBean().getEventDataMap(facilityIdList);
		for (Entry<String, ArrayList<EventDataInfo>> e : map1.entrySet()) {
			ArrayListInfo list = new ArrayListInfo();
			list.setList3(e.getValue());
			map.put(e.getKey(), list);
		}
		ret.setMap7(map);
		return ret;
	}
	
	/**
	 * 収集項目コードを取得します。
	 * 
	 * @return 収集項目コードリスト
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public ArrayList<CollectorItemCodeMstData> getCollectItemCodeMasterList() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCollectItemCodeMasterList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get Master, Method=getCollectItemCodeMasterList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		return new PerformanceCollectMasterControllerBean().getCollectItemCodeMasterList();
	}

}
