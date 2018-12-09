/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.hub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.activation.DataHandler;
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
import com.clustercontrol.binary.bean.BinaryDownloadDTO;
import com.clustercontrol.binary.bean.BinaryQueryInfo;
import com.clustercontrol.binary.factory.BinaryHubController;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.binary.session.BinaryControllerBean;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.BinaryRecordNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.hub.bean.StringQueryResult;
import com.clustercontrol.hub.model.CollectStringDataPK;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * バイナリファイル監視・収集機能用のWebAPIエンドポイント
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://hub.ws.clustercontrol.com")
public class BinaryEndpoint {

	@Resource
	WebServiceContext wsctx;

	/** ログ出力用オブジェクト. */
	private static Log logger = LogFactory.getLog(BinaryEndpoint.class);
	private static Log opelogger = LogFactory.getLog("HinemosOperation");
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * バイナリ収集情報を検索する.
	 * 
	 * @param query
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws HinemosDbTimeout
	 * @throws InvalidSetting
	 */
	public StringQueryResult queryCollectBinaryData(BinaryQueryInfo query)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, HinemosDbTimeout, InvalidSetting {
		logger.debug("queryCollectBinaryData");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		logger.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogTransferDestTypeMst, Method=queryCollectBinaryData, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new BinaryHubController().queryCollectBinaryData(query);
	}

	/**
	 * DB取得したバイナリファイルをクライアント送信用に返却.
	 * 
	 * @param recordTime
	 *            表示レコード時刻
	 * 
	 * @param recordKey
	 *            レコードキー
	 * @throws HinemosException
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadBinaryRecord(BinaryQueryInfo queryInfo, CollectStringDataPK primaryKey, String filename,
			String clientName) throws BinaryRecordNotFound, HinemosDbTimeout, IOException, HinemosUnknown, InvalidRole, InvalidSetting, InvalidUserPass {
		logger.debug("downloadBinaryRecord");

		// 権限チェック(収集蓄積・作成の権限を持っているか).
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ出力用文字列.
		StringBuffer msg = new StringBuffer();
		if (primaryKey != null) {
			msg.append(", FacilitiId=");
			msg.append(queryInfo.getFacilityId());
			msg.append(", MonitorId=");
			msg.append(queryInfo.getMonitorId());
			msg.append(", Key=");
			msg.append(primaryKey);
		}

		// ダウンロード処理.
		DataHandler dh = null;
		try {
			dh = new BinaryControllerBean().downloadBinaryRecord(queryInfo, primaryKey, filename, clientName);
		} catch (InvalidSetting e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_HUB + " Download Failed, Method=downloadBinaryRecord, User="
					+ HttpAuthenticator.getUserAccountString(wsctx) + msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_HUB + " Download, Method=downloadBinaryRecord, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) + msg.toString());

		return dh;
	}

	/**
	 * バイナリファイル送信用の一時ファイルを削除.
	 * 
	 * @param fileName
	 *            ダウンロードファイル名
	 * @throws InvalidSetting
	 */
	public void deleteDownloadedBinaryRecord(String fileName, String clientName)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		logger.debug("deleteDownloadedBinaryRecord");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();

		if (fileName == null) {
			InvalidSetting e = new InvalidSetting(methodName + DELIMITER + "fileName is not defined.");
			logger.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		msg.append(", FileName=");
		msg.append(fileName);

		try {
			new BinaryControllerBean().deleteDownloadedBinaryRecord(fileName, clientName);
		} catch (Exception e) {
			opelogger.warn(
					HinemosModuleConstant.LOG_PREFIX_HUB + " Delete Failed, Method=deleteDownloadedBinaryRecord, User="
							+ HttpAuthenticator.getUserAccountString(wsctx) + msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_HUB + " Delete, Method=deleteDownloadedBinaryRecord, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) + msg.toString());

	}

	/**
	 * DB取得したバイナリファイルをzipファイルにまとめてクライアント送信用に返却.
	 * 
	 * @param recordTime
	 *            表示レコード時刻
	 * 
	 * @param recordKey
	 *            レコードキー
	 * @return
	 * @throws HinemosException
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadBinaryRecords(String fileName, List<BinaryDownloadDTO> binaryDownloadDTOList,
			String clientName) throws BinaryRecordNotFound, IOException, InvalidUserPass, InvalidRole, InvalidSetting, HinemosDbTimeout, HinemosUnknown {
		logger.debug("downloadBinaryRecords");

		// 権限チェック(収集蓄積・作成の権限を持っているか).
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// レコード毎にDataHandlerを作成.
		StringBuffer msgAll = new StringBuffer();
		msgAll.append(", Size=");
		msgAll.append(binaryDownloadDTOList.size());
		StringBuffer msg = new StringBuffer();
		msg.append(", BinaryDownloadDTO=[");
		StringBuffer msgDtos = new StringBuffer();
		String prefix = ", BinaryDownloadDTO=[";

		ArrayList<String> intoZipList = new ArrayList<String>();
		BinaryControllerBean controller = new BinaryControllerBean();
		// ファイル統合用にダウンロード条件リストの順序をレコードキーで整列.
		Collections.sort(binaryDownloadDTOList, new Comparator<BinaryDownloadDTO>() {
			@Override
			public int compare(BinaryDownloadDTO info1, BinaryDownloadDTO info2) {
				// ファイル種別で比較した結果を返却.
				return info1.getRecordKey().compareTo(info2.getRecordKey());
			}
		});
		for (BinaryDownloadDTO binaryDownloadDTO : binaryDownloadDTOList) {
			try {
				// 認証済み操作ログ出力用文字列.
				if (binaryDownloadDTO.getRecordKey() != null) {
					msg.append("FacilitiId=");
					msg.append(binaryDownloadDTO.getQueryInfo().getFacilityId());
					msg.append(", MonitorId=");
					msg.append(binaryDownloadDTO.getQueryInfo().getMonitorId());
					msg.append(", Key=");
					msg.append(binaryDownloadDTO.getPrimaryKey());
					msg.append("]");
					msgDtos.append(prefix + msg);
					msg = new StringBuffer();
					prefix = ",[";
				}
				// マネージャーに一時ファイルを出力.
				intoZipList = controller.createTmpRecords(binaryDownloadDTO.getQueryInfo(),
						binaryDownloadDTO.getPrimaryKey(), intoZipList, clientName);
			} catch (InvalidSetting e) {
				// デバッグモードの場合は検索条件を全件出力.
				opelogger.warn(HinemosModuleConstant.LOG_PREFIX_HUB
						+ " Download Failed, Method=downloadBinaryRecord, User="
						+ HttpAuthenticator.getUserAccountString(wsctx) + ", BinaryDownloadDTO=[" + msg.toString());
				opelogger.debug(HinemosModuleConstant.LOG_PREFIX_HUB + " Download, Method=downloadBinaryRecord, User="
						+ HttpAuthenticator.getUserAccountString(wsctx) + msgAll.append(msgDtos).toString());
				throw e;
			}
		}
		if (opelogger.isDebugEnabled()) {
			opelogger.debug(HinemosModuleConstant.LOG_PREFIX_HUB + " Download, Method=downloadBinaryRecord, User="
					+ HttpAuthenticator.getUserAccountString(wsctx) + msgAll.append(msgDtos).toString());
		} else {
			opelogger.info(HinemosModuleConstant.LOG_PREFIX_HUB + " Download, Method=downloadBinaryRecord, User="
					+ HttpAuthenticator.getUserAccountString(wsctx) + msgAll.toString());
		}

		return controller.createZipHandler(intoZipList, fileName, clientName);
	}

	/**
	 * プリセットのリストを取得します。
	 * 
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws IOException
	 * @throws InvalidSetting
	 */
	public List<BinaryCheckInfo> getPresetList() throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		logger.debug("getPresetList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HUB, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		logger.debug(HinemosModuleConstant.LOG_PREFIX_HUB
				+ " Get LogTransferDestTypeMst, Method=queryCollectBinaryData, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new BinaryControllerBean().getPresetList();
	}
}
