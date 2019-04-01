/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.nodemap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.BgFileNotFound;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.nodemap.NodeMapException;
import com.clustercontrol.nodemap.bean.Association;
import com.clustercontrol.nodemap.bean.NodeMapModel;
import com.clustercontrol.nodemap.session.NodeMapControllerBean;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * ノードマップ用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://nodemap.ws.clustercontrol.com")
public class NodeMapEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( NodeMapEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

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
	 * マップのデータをDBに登録します。<BR>
	 * 
	 * RepositoryWrite権限が必要
	 * 
	 * @param facilityId 描画対象スコープのファシリティID
	 * @return マップ描画用データ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.nodemap.model.NodeMapModel
	 */
	public void registerNodeMapModel(NodeMapModel map) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("registerNodeMapModel");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if (map != null) {
			msg.append(", MapID=");
			msg.append(map.getMapId());
		}

		try {
			new NodeMapControllerBean().registerNodeMapModel(map);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Set Map Failed, Method=registerNodeMapModel, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Set Map, Method=registerNodeMapModel, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * マップのデータを取得します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @param facilityId 描画対象スコープのファシリティID
	 * @return マップ描画用データ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws NodeMapException
	 * 
	 * @see com.clustercontrol.nodemap.model.NodeMapModel
	 */
	public NodeMapModel getNodeMapModel(String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown, NodeMapException {
		m_log.debug("getNodeMapModel");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=getNodeMapModel, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NodeMapControllerBean().getNodeMapModel(facilityId);
	}

	/**
	 * 構成情報検索の検索結果を取得します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @param parentFacilityId 親スコープのファシリティID
	 * @param nodeFilterInfo 構成情報による検索条件
	 * @return 検索結果のノード情報一覧
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws HinemosDbTimeout
	 * @throws HinemosUnknown
	 */
	public List<NodeInfo> getNodeList(String parentFacilityId, NodeInfo nodeFilterInfo)
			throws InvalidUserPass, InvalidRole, InvalidSetting, HinemosDbTimeout, HinemosUnknown {
		m_log.debug("getNodeList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(parentFacilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=getNodeList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getNodeList(parentFacilityId, nodeFilterInfo);
	}

	/**
	 * 構成情報ファイルの一時ファイルIDを返します。
	 * 
	 * @return 一時ファイルID
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String getNodeConfigFileId() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return new RepositoryControllerBean().getNodeConfigFileId();
	}

	/**
	 * 構成情報ファイルのヘッダーを返します。<BR><BR>
	 * 
	 * RepositoryRead権限が必要
	 * 	 *
	 * @param conditionStr 検索条件
	 * @param filename ファイル名
	 * @param language ロケール
	 * @return 帳票出力用構成情報一覧
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @since 6.2.0
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadNodeConfigFileHeader(String conditionStr, String filename, String language)
			throws HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("downloadNodeConfigFile");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		msg.append(", ConditionStr=");
		msg.append(conditionStr);
		msg.append(", FileName=");
		msg.append(filename);
		msg.append(", Locale=");
		msg.append(language);
		msg.append(", isFirst=");

		DataHandler ret = null;
		try {
			ret = new RepositoryControllerBean().downloadNodeConfigFileHeader(conditionStr, filename, new Locale(language));
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Download Failed, Method=downloadNodeConfigFileHeader, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Download, Method=downloadNodeConfigFileHeader, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 引数で指定された条件に一致する構成情報ファイルを返します。<BR><BR>
	 * 
	 * RepositoryRead権限が必要
	 * 	 *
	 * @param facilityIdlist ファシリティID一覧
	 * @param targetDatetime 対象日時
	 * @param filename ファイル名
	 * @param language ロケール
	 * @param managerName マネージャ名
	 * @param itemList 構成情報ダウンロード対象一覧
	 * @return 帳票出力用構成情報一覧
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @since 6.2.0
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadNodeConfigFile(List<String> facilityIdList, Long targetDatetime, 
			String filename, String language, String managerName, List<String> itemList)
			throws HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("downloadNodeConfigFile");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityIds=");
		if (facilityIdList != null && facilityIdList.size() > 0) {
			msg.append(Arrays.toString(facilityIdList.toArray(new String[facilityIdList.size()])));
		} else {
			msg.append("null");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		msg.append(", TargetDatetime=");
		if (targetDatetime != null
				&& targetDatetime != 0L) {
			msg.append(sdf.format(new Date(targetDatetime)));
		} else {
			msg.append("null");
		}
		msg.append(", FileName=");
		msg.append(filename);
		msg.append(", Locale=");
		msg.append(language);
		msg.append(", NodeConfigSettingItem=");
		for (String item : itemList) {
			msg.append(item);
			msg.append(",");
		}
		DataHandler ret = null;

		try {
			ret = new RepositoryControllerBean().downloadNodeConfigFile(
					facilityIdList, targetDatetime, filename, new Locale(language), managerName, itemList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Download Failed, Method=downloadNodeConfigFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Download, Method=downloadNodeConfigFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 構成情報CSVファイルダウンロードで一度に取得する情報のノード数を取得します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public int getDownloadNodeConfigCount() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		int maxsize = 0;
		maxsize = HinemosPropertyCommon.nodemap_download_node_config_count.getIntegerValue();
		
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getDownloadNodeConfigCount, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return maxsize;
	}

	/**
	 * 一時ファイルから指定されたファイル名の構成情報ファイルを削除します。
	 * 
	 * @param filename ファイル名
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deleteNodeConfigFile(String filename) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("deleteNodeConfigFile");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", deleteNodeConfigFile=");
		msg.append(filename);

		try {
			new RepositoryControllerBean().deleteNodeConfigInfoFile(filename);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Delete Failed, Method=deleteNodeConfigFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Delete, Method=deleteNodeConfigFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}

	/**
	 * 背景データを取得します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @param filename
	 * @return filedata
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws BgFileNotFound
	 */
	public byte[] getBgImage(String filename) throws InvalidUserPass, InvalidRole, HinemosUnknown, BgFileNotFound {
		m_log.debug("getBgImage");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(filename);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=getBgImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NodeMapControllerBean().getBgImage(filename);
	}

	/**
	 * 背景データをDBに登録します。<BR>
	 * 
	 * RepositoryWrite権限が必要
	 * 
	 * @param filename
	 * @param filedata
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 * @throws NodeMapException
	 */
	public void setBgImage(String filename, byte[] filedata) throws InvalidUserPass, InvalidRole, HinemosUnknown, NodeMapException {
		m_log.debug("setBgImage");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(filename);

		try {
			// TODO byte配列のサイズが大きくなる可能性あり。MTOMで実装したほうがよいかも。
			new NodeMapControllerBean().setBgImage(filename, filedata);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Set Image Failed, Method=setBgImage, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Set Image, Method=setBgImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 背景画像のファイル名一覧を取得します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @return Collection<String>
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public Collection<String> getBgImagePK() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getBgImagePK");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=getBgImagePK, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new NodeMapControllerBean().getBgImagePK();
	}

	/**
	 * 背景画像のファイル名の存在有無を取得します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @return boolean
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public boolean isBgImage(String filename) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("isBgImage");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(filename);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=isBgImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NodeMapControllerBean().isBgImage(filename);
	}

	/**
	 * アイコン画像を取得します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @param filename
	 * @return filedata
	 * @throws IconFileNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public byte[] getIconImage(String filename) throws HinemosUnknown, IconFileNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getIconImage");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(filename);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=getIconImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NodeMapControllerBean().getIconImage(filename);
	}

	/**
	 * アイコン画像をDBに登録します。<BR>
	 * 
	 * RepositoryWrite権限が必要
	 * 
	 * @param filename
	 * @param filedata
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws NodeMapException
	 * 
	 */
	public void setIconImage(String filename, byte[] filedata) throws InvalidUserPass, InvalidRole, HinemosUnknown, NodeMapException {
		m_log.debug("setIconImage");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(filename);

		try {
			new NodeMapControllerBean().setIconImage(filename, filedata);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Set Image Failed, Method=setIconImage, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Set Image, Method=setIconImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * アイコンのファイル名一覧を取得します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @return Collection<String>
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public Collection<String> getIconImagePK() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getIconImagePK");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=getIconImagePK, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new NodeMapControllerBean().getIconImagePK();
	}

	/**
	 * アイコンのファイル名の存在有無を取得します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @return boolean
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public boolean isIconImage(String filename) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("isIconImage");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(filename);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=isIconImage, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NodeMapControllerBean().isIconImage(filename);
	}

	/**
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public List<Association> getL2ConnectionMap(String scopeId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getL2ConnectionMap");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ScopeId=");
		msg.append(scopeId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=getL2ConnectionMap, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NodeMapControllerBean().getL2ConnectionMap(scopeId);
	}

	/**
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public List<Association> getL3ConnectionMap(String scopeId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getL3ConnectionMap");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ScopeId=");
		msg.append(scopeId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Get, Method=getL3ConnectionMap, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new NodeMapControllerBean().getL3ConnectionMap(scopeId);
	}

	/**
	 * 指定されたfacilityIdにpingを実施し、結果を文字列で返します。
	 * 
	 * @param facilityId
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws NodeMapException
	 */
	public List<String> ping(String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown, NodeMapException {
		m_log.debug("ping:" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_RESULT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		List<String> facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, null);
		
		return new NodeMapControllerBean().pingToFacilityList(facilityList);
	}


	/**
	 * 検索条件に一致するノードを割り当てたスコープを新規に追加します。<BR>
	 * 
	 * RepositoryWrite権限が必要
	 * 
	 * @param property スコープ情報
	 * @param facilityIdList 割当てノード情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws NodeMapException
	 * 
	 */
	public void addFilterScope(ScopeInfo property, List<String> facilityIdList)
			throws InvalidUserPass, FacilityDuplicate, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.debug("addFilterScope");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ScopeId=");
		if (property != null && property.getFacilityId() != null) {
			msg.append(property.getFacilityId());
		} else {
			msg.append("null");
		}
		msg.append(", FacilityIds=");
		if (facilityIdList != null && facilityIdList.size() > 0) {
			msg.append(Arrays.toString(facilityIdList.toArray(new String[facilityIdList.size()])));
		} else {
			msg.append("null");
		}

		try {
			new RepositoryControllerBean().addFilterScope(property, facilityIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " add Scope Failed, Method=addFilterScope, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " add Scope, Method=addFilterScope, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
}