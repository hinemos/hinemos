/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.collect.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.collectmaster.CollectMasterInfo;
import com.clustercontrol.ws.collectmaster.CollectorPlatformMstData;
import com.clustercontrol.ws.collectmaster.HinemosUnknown_Exception;
import com.clustercontrol.ws.collectmaster.InvalidRole_Exception;
import com.clustercontrol.ws.collectmaster.InvalidUserPass_Exception;
import com.clustercontrol.ws.collectmaster.PerformanceCollectMasterEndpoint;
import com.clustercontrol.ws.collectmaster.PerformanceCollectMasterEndpointService;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class PerformanceCollectMasterEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( PerformanceCollectMasterEndpointWrapper.class );

	/**
	 * 収集項目マスタデータを一括で登録します。
	 * 
	 * @param collectMasterInfo 収集項目マスタ情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public static boolean addCollectMaster(CollectMasterInfo collectMasterInfo)
	throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<PerformanceCollectMasterEndpoint> endpointSetting : EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getEndpoint(PerformanceCollectMasterEndpointService.class, PerformanceCollectMasterEndpoint.class)) {
			try {
				PerformanceCollectMasterEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.addCollectMaster(collectMasterInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addCollectMaster(), " + e.getMessage(), e);
//				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * 収集項目のマスタ情報を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * 
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public static boolean deleteCollectMasterAll()
	throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<PerformanceCollectMasterEndpoint> endpointSetting : EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getEndpoint(PerformanceCollectMasterEndpointService.class, PerformanceCollectMasterEndpoint.class)) {
			try {
				PerformanceCollectMasterEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.deleteCollectMasterAll();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addCollectMaster(), " + e.getMessage(), e);
//				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * 収集項目マスタデータを取得します。
	 * @return
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public static CollectMasterInfo getCollectMasterInfo()
	throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<PerformanceCollectMasterEndpoint> endpointSetting : EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getEndpoint(PerformanceCollectMasterEndpointService.class, PerformanceCollectMasterEndpoint.class)) {
			try {
				PerformanceCollectMasterEndpoint endpoint = endpointSetting.getEndpoint();
				CollectMasterInfo collectMasterInfo = endpoint.getCollectMasterInfo();
				return collectMasterInfo;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addCollectMaster(), " + e.getMessage(), e);
//				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * プラットフォームマスタデータを登録します。
	 * 
	 * @param collectPlatformMstData プラットフォームマスタ情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public static boolean addCollectPlatformMaster(CollectorPlatformMstData collectPlatformMstData)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<PerformanceCollectMasterEndpoint> endpointSetting : EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getEndpoint(PerformanceCollectMasterEndpointService.class, PerformanceCollectMasterEndpoint.class)) {
			try {
				PerformanceCollectMasterEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.addCollectPlatformMaster(collectPlatformMstData);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addCollectMaster(), " + e.getMessage(), e);
//				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}
	
	/**
	 * プラットフォームマスタデータを取得します。
	 * @return
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 */
	public static List<CollectorPlatformMstData> getCollectPlatformMaster()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<PerformanceCollectMasterEndpoint> endpointSetting : EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getEndpoint(PerformanceCollectMasterEndpointService.class, PerformanceCollectMasterEndpoint.class)) {
			try {
				PerformanceCollectMasterEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getCollectPlatformMaster();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addCollectMaster(), " + e.getMessage(), e);
//				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

}
