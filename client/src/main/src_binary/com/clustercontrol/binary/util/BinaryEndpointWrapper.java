/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.util;

import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.hub.BinaryDownloadDTO;
import com.clustercontrol.ws.hub.BinaryEndpoint;
import com.clustercontrol.ws.hub.BinaryEndpointService;
import com.clustercontrol.ws.hub.BinaryQueryInfo;
import com.clustercontrol.ws.hub.BinaryRecordNotFound_Exception;
import com.clustercontrol.ws.hub.CollectStringDataPK;
import com.clustercontrol.ws.hub.HinemosDbTimeout_Exception;
import com.clustercontrol.ws.hub.HinemosUnknown_Exception;
import com.clustercontrol.ws.hub.IOException_Exception;
import com.clustercontrol.ws.hub.InvalidRole_Exception;
import com.clustercontrol.ws.hub.InvalidSetting_Exception;
import com.clustercontrol.ws.hub.InvalidUserPass_Exception;
import com.clustercontrol.ws.hub.StringQueryResult;
import com.clustercontrol.ws.monitor.BinaryCheckInfo;

/**
 * Hinemosマネージャとの通信をするクラス.<br>
 * HAのような複数マネージャ対応のため、このクラスを実装する.
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる<br>
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する<br>
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class BinaryEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog(BinaryEndpointWrapper.class);

	/** Manager通信管理オブジェクト */
	private EndpointUnit endpointUnit;

	/**
	 * コンストラクタ.
	 * 
	 * @param endpointUnit
	 *            Manager通信管理オブジェクト.
	 */
	public BinaryEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	/**
	 * マネージャ名を指定して通信オブジェクトを取得.
	 * 
	 * @param managerName
	 *            マネージャー名.
	 */
	public static BinaryEndpointWrapper getWrapper(String managerName) {
		return new BinaryEndpointWrapper(EndpointManager.getActive(managerName));
	}

	/**
	 * Manager通信Serviceに対応するEndpoint設定を取得.
	 * 
	 * @param endpointUnit
	 *            Manager通信管理オブジェクト.
	 */
	private static List<EndpointSetting<BinaryEndpoint>> getBinaryEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(BinaryEndpointService.class, BinaryEndpoint.class);
	}

	/**
	 * 収集バイナリデータ検索.
	 * 
	 * @param query
	 * @return
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws HinemosDbTimeout_Exception
	 * @throws InvalidSetting_Exception
	 */
	public StringQueryResult queryCollectBinaryData(BinaryQueryInfo query) throws InvalidUserPass_Exception,
			InvalidRole_Exception, HinemosUnknown_Exception, HinemosDbTimeout_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<BinaryEndpoint> endpointSetting : getBinaryEndpoint(endpointUnit)) {
			try {
				BinaryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.queryCollectBinaryData(query);
			} catch (WebServiceException e) {
				wse = e;
				Logger.getLogger(this.getClass()).warn("queryCollectBinaryData(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * ManagerからDB取得ファイル送信.
	 * 
	 * @param recordTime
	 *            表示レコード時刻
	 * @param recordKey
	 *            レコードキー
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 * @throws InvalidRole_Exception
	 * @throws IOException_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws BinaryRecordNotFound_Exception
	 * @throws HinemosDbTimeout_Exception
	 */
	public DataHandler downloadBinaryRecord(BinaryQueryInfo queryInfo, CollectStringDataPK primaryKey, String filename,
			String clientName) throws HinemosUnknown_Exception, IOException_Exception, InvalidRole_Exception,
			InvalidSetting_Exception, InvalidUserPass_Exception, BinaryRecordNotFound_Exception, HinemosDbTimeout_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<BinaryEndpoint> endpointSetting : getBinaryEndpoint(endpointUnit)) {
			try {
				BinaryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.downloadBinaryRecord(queryInfo, primaryKey, filename, clientName);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadBinaryRecord(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * Client受信済一時保存ファイルの削除.
	 * 
	 * @param recordTime
	 *            表示レコード時刻
	 * @param recordKey
	 *            レコードキー
	 * @throws com.clustercontrol.ws.binary.HinemosUnknown_Exception
	 * @throws com.clustercontrol.ws.binary.InvalidRole_Exception
	 * @throws com.clustercontrol.ws.binary.InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 */
	public void deleteDownloadedBinaryRecord(String fileName, String clientName) throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<BinaryEndpoint> endpointSetting : getBinaryEndpoint(endpointUnit)) {
			try {
				BinaryEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteDownloadedBinaryRecord(fileName, clientName);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadInfraFile(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * ManagerからDB取得ファイル送信.
	 * 
	 * @param recordTime
	 *            表示レコード時刻
	 * @param recordKey
	 *            レコードキー
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 * @throws InvalidRole_Exception
	 * @throws IOException_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws BinaryRecordNotFound_Exception
	 * @throws HinemosDbTimeout_Exception
	 */
	public DataHandler downloadBinaryRecords(String filename, List<BinaryDownloadDTO> binaryDownloadDTOList,
			String clientName) throws HinemosUnknown_Exception, IOException_Exception, InvalidRole_Exception,
			InvalidSetting_Exception, InvalidUserPass_Exception, BinaryRecordNotFound_Exception, HinemosDbTimeout_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<BinaryEndpoint> endpointSetting : getBinaryEndpoint(endpointUnit)) {
			try {
				BinaryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.downloadBinaryRecords(filename, binaryDownloadDTOList, clientName);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("downloadBinaryRecords(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * Managerからデータ構造プリセット取得.
	 * 
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public List<BinaryCheckInfo> getPresetList() throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidSetting_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<BinaryEndpoint> endpointSetting : getBinaryEndpoint(endpointUnit)) {
			try {
				BinaryEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getPresetList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getPresetList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

}
