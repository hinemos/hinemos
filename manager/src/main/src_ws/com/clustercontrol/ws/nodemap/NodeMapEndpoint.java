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
package com.clustercontrol.ws.nodemap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.BgFileNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.nodemap.NodeMapException;
import com.clustercontrol.nodemap.bean.Association;
import com.clustercontrol.nodemap.bean.NodeMapModel;
import com.clustercontrol.nodemap.session.NodeMapControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
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
	 * リポジトリ情報からマップのデータを生成します。<BR>
	 * 
	 * RepositoryRead権限が必要
	 * 
	 * @param parentFacilityId 描画対象スコープの親のファシリティID
	 * @param facilityId 描画対象スコープのファシリティID
	 * @return マップ描画用データ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 * @see com.clustercontrol.nodemap.model.NodeMapModel
	 */
	public NodeMapModel createNodeMapModel(String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("createNodeMapModel");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		NodeMapModel ret = null;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);

		try {
			ret = new NodeMapControllerBean().createNodeMapModel(facilityId);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Add Map Failed, Method=createNodeMapModel, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_NODEMAP + " Add Map, Method=createNodeMapModel, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
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

	public String getVersion() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return "1.0";
	}
}