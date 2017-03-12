package com.clustercontrol.collect.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.collect.CollectEndpoint;
import com.clustercontrol.ws.collect.CollectEndpointService;
import com.clustercontrol.ws.collect.CollectKeyInfoPK;
import com.clustercontrol.ws.collect.CollectorItemCodeMstData;
import com.clustercontrol.ws.collect.HashMapInfo;
import com.clustercontrol.ws.collect.HashMapInfo.Map6;
import com.clustercontrol.ws.collect.HashMapInfo.Map7;
import com.clustercontrol.ws.collect.HinemosUnknown_Exception;
import com.clustercontrol.ws.collect.InvalidRole_Exception;
import com.clustercontrol.ws.collect.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.EventDataInfo;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class CollectEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( CollectEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public CollectEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static CollectEndpointWrapper getWrapper(String managerName) {
		return new CollectEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<CollectEndpoint>> getCollectEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(CollectEndpointService.class, CollectEndpoint.class);
	}


	
	public HashMapInfo getCollectId(String itemName, String displayName, String monitorId, List<String> facilityIdList) throws InvalidUserPass_Exception, HinemosUnknown_Exception, InvalidRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CollectEndpoint> endpointSetting : getCollectEndpoint(endpointUnit)) {
			try {
				CollectEndpoint endpoint = (CollectEndpoint) endpointSetting.getEndpoint();
				return endpoint.getCollectId(itemName, displayName, monitorId, facilityIdList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCollectId(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public HashMapInfo getCollectData(List<Integer> idList, Integer summaryType, Long fromTime, Long toTime) throws InvalidUserPass_Exception, HinemosUnknown_Exception, InvalidRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CollectEndpoint> endpointSetting : getCollectEndpoint(endpointUnit)) {
			try {
				CollectEndpoint endpoint = (CollectEndpoint) endpointSetting.getEndpoint();
				long start = System.currentTimeMillis();
				HashMapInfo info = endpoint.getCollectData(idList, summaryType, fromTime, toTime);
				m_log.debug("time:" + (System.currentTimeMillis() - start));
				return info;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCollectData(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<CollectKeyInfoPK> getItemCodeList(List<String> facilityIdList) throws InvalidUserPass_Exception, HinemosUnknown_Exception, InvalidRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CollectEndpoint> endpointSetting : getCollectEndpoint(endpointUnit)) {
			try {
				CollectEndpoint endpoint = (CollectEndpoint) endpointSetting.getEndpoint();
				return endpoint.getItemCodeList(facilityIdList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getItemCodeList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public List<String> createPerfFile(Map<String, String> facilityIdNameMap, 
			List<CollectKeyInfoPK> collectKeyInfoList, 
			List<String> facilityList, 
			Integer summaryType, boolean header, String defaultDateStr) 
					throws InvalidUserPass_Exception,HinemosUnknown_Exception, InvalidRole_Exception {
		WebServiceException wse = null;
		// ファイル出力時の言語を引数で渡すためにlocaleを取得する
		String localeStr = Locale.getDefault().getLanguage();
		for (EndpointSetting<CollectEndpoint> endpointSetting : getCollectEndpoint(endpointUnit)) {
			try {
				HashMapInfo map1= new HashMapInfo();
				Map6 map6 = new Map6();
				for (Map.Entry<String, String> facilityEntry : facilityIdNameMap.entrySet()) {
					Map6.Entry entry = new Map6.Entry();
					entry.setKey(facilityEntry.getKey());
					entry.setValue(facilityEntry.getValue());
					m_log.debug("key:" + entry.getKey() + ", value:" + entry.getValue());
					map6.getEntry().add(entry);
				}
				map1.setMap6(map6);
				
				CollectEndpoint endpoint = (CollectEndpoint) endpointSetting.getEndpoint();
				return endpoint.createPerfFile(map1, collectKeyInfoList, facilityList, 
						summaryType, localeStr, header, defaultDateStr);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("createPerformanceFile, " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public DataHandler downloadPerfFile(String fileName)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CollectEndpoint> endpointSetting : getCollectEndpoint(endpointUnit)) {
			try {
				CollectEndpoint endpoint = (CollectEndpoint) endpointSetting.getEndpoint();
				return endpoint.downloadPerfFile(fileName);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadPerformanceFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deletePerfFile(ArrayList<String> fileNameList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CollectEndpoint> endpointSetting : getCollectEndpoint(endpointUnit)) {
			try {
				CollectEndpoint endpoint = (CollectEndpoint) endpointSetting.getEndpoint();
				endpoint.deletePerfFile(fileNameList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deletePerformanceFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public Map<String, List<EventDataInfo>> getEventDataMap (List<String> facilityIdList) throws HinemosUnknown_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CollectEndpoint> endpointSetting : getCollectEndpoint(endpointUnit)) {
			try {
				CollectEndpoint endpoint = (CollectEndpoint) endpointSetting.getEndpoint();
				HashMapInfo mapInfo = endpoint.getEventDataMap(facilityIdList);

				HashMap<String, List<EventDataInfo>> ret = new HashMap<>();
				
				m_log.debug("EventDataMap start");
				for (Map7.Entry e : mapInfo.getMap7().getEntry()) {
					ret.put(e.getKey(), e.getValue().getList3());

					m_log.debug("EventDataMap key=" + e.getKey() + ", size=" + e.getValue().getList3().size());
				}
				
				return ret;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getEventDataMap(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * 収集項目コードリストを取得します。
	 * @return
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public List<CollectorItemCodeMstData> getCollectItemCodeMasterList()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<CollectEndpoint> endpointSetting : getCollectEndpoint(endpointUnit)) {
			try {
				CollectEndpoint endpoint = (CollectEndpoint) endpointSetting.getEndpoint();
				List<CollectorItemCodeMstData> collectMasterInfo = endpoint.getCollectItemCodeMasterList();
				return collectMasterInfo;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getCollectItemCodeMasterList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

}
