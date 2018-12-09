/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.infra;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileBeingUsed;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.fault.InfraManagementDuplicate;
import com.clustercontrol.fault.InfraManagementInvalid;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InfraModuleNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.SessionNotFound;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.ModuleResult;
import com.clustercontrol.infra.model.InfraCheckResult;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.session.InfraControllerBean;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * 環境構築機能用のWebAPIエンドポイント
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://infra.ws.clustercontrol.com")
public class InfraEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log logger = LogFactory.getLog( InfraEndpoint.class );
	private static Log opelogger = LogFactory.getLog("HinemosOperation");

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
	 * 環境構築情報を作成します。
	 *
	 * InfraAdd 権限が必要
	 *
	 * @param info 作成対象の通知情報
	 * @return 作成に成功した場合、<code> true </code>
	 * @throws NotifyDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.notify.factory.AddNotify#add(NotifyInfo)
	 */
	public boolean addInfraManagement(InfraManagementInfo info) 
			throws InfraManagementDuplicate, InfraManagementNotFound, HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		logger.debug("addInfraManagement");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", ManagementID=");
			msg.append(info.getManagementId());
		}

		try {
			ret = new InfraControllerBean().addInfraManagement(info);
		} catch (Exception e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_INFRA + " Add Failed, Method=addInfraManagement, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_INFRA + " Add, Method=addInfraManagement, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * 環境構築情報を変更します。
	 *
	 */
	public boolean modifyInfraManagement(InfraManagementInfo info) throws NotifyDuplicate, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole,InvalidSetting, InfraManagementNotFound, InfraManagementDuplicate {
		logger.debug("modifyInfraManagement");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", ManagementID=");
			msg.append(info.getManagementId());
		}

		try {
			ret = new InfraControllerBean().modifyInfraManagement(info);
		} catch (Exception e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_INFRA + " Change Failed, Method=modifyInfraManagement, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_INFRA + " Change, Method=modifyInfraManagement, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 */
	public boolean deleteInfraManagement(String[] managementIds) throws HinemosUnknown, InvalidUserPass, InvalidSetting, InvalidRole, InfraManagementNotFound {
		logger.debug("deleteInfraManagement");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ManagementID=");
		msg.append(Arrays.toString(managementIds));

		try {
			ret = new InfraControllerBean().deleteInfraManagement(managementIds);
		} catch (Exception e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_INFRA + " Delete Failed, Method=deleteInfraManagement, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_INFRA + " Delete, Method=deleteInfraManagement, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 */
	public InfraManagementInfo getInfraManagement(String managementId) throws HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound {
		logger.debug("getInfraManagement");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ManagementID=");
		msg.append(managementId);
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=getInfraManagement, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new InfraControllerBean().getInfraManagement(managementId);
	}

	/**
	 */
	public List<InfraManagementInfo> getInfraManagementList() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		logger.debug("getInfraManagementList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=getInfraManagementList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new InfraControllerBean().getInfraManagementList();
	}

	/**
	 */
	public List<InfraManagementInfo> getInfraManagementListByOwnerRole(String ownerRoleId) throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		logger.debug("getInfraManagementListByOwnerRole");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=getInfraManagementListByOwnerRole, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new InfraControllerBean().getInfraManagementListByOwnerRole(ownerRoleId);
	}

	/**
	 */
	public List<String> getReferManagementIdList(String ownerRoleId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		logger.debug("getReferManagementIdList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", OwnerRoleID=");
		msg.append(ownerRoleId);
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=getReferManagementIdList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) + msg.toString());

		return new InfraControllerBean().getReferManagementIdList(ownerRoleId);
	}

	public String createSession(String managementId, List<String> moduleIdList, Integer nodeInputType, List<AccessInfo> accessList)
			throws InfraManagementNotFound, InfraModuleNotFound, FacilityNotFound, InfraManagementInvalid, 
			InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		logger.debug("createSession");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ManagementID=");
		msg.append(managementId);
		msg.append(", ModuleID=");
		boolean flag = false;
		if (moduleIdList != null) {
			for (String moduleId : moduleIdList) {
				msg.append(moduleId);
				if (flag) {
					msg.append(",");
				}
				flag = true;
			}
		}

		String ret = null;
		try {
			ret = new InfraControllerBean().createSession(managementId, moduleIdList, nodeInputType, accessList);
		} catch (InfraManagementNotFound | InfraModuleNotFound | InfraManagementInvalid | InvalidRole | FacilityNotFound | InvalidSetting e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Execute Failed, Method=createSession, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Execute, Method=createSession, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}
	
	public boolean deleteSession(String sessionId) 
			throws InfraManagementNotFound, InvalidUserPass, InvalidRole, HinemosUnknown { 
		logger.debug("deleteSession");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);

		boolean ret = false;
		try {
			ret = new InfraControllerBean().deleteSession(sessionId);
		} catch (Exception e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Execute Failed, Method=deleteSession, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Execute, Method=deleteSession, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}
	
	
	/**
	 * 
	 * アクセス情報を作成する
	 *
	 * InfraExec権限が必要
	 *
	 * @param managementId 環境構築ID
	 * @param moduleIdList モジュールIDリスト
	 * @return アクセス情報のリスト
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public List<AccessInfo> createAccessInfoListForDialog(String managementId, List<String> moduleIdList)
			throws InfraManagementNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		logger.debug("createInfraManagementAccessInfoList");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ManagementId=");
		msg.append(managementId);
		msg.append(", ModuleIdList=");
		if (moduleIdList != null) {
			msg.append(Arrays.toString(moduleIdList.toArray()));
		} else {
			msg.append("null");
		}
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=createAccessInfoList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new InfraControllerBean().createAccessInfoListForDialog(managementId, moduleIdList);
	}

	/**
	 */
	public ModuleResult runInfraModule(String sessionId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound, InfraModuleNotFound, SessionNotFound {
		logger.debug("runInfraModule=" + sessionId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionId=");
		msg.append(sessionId);

		ModuleResult ret = null;
		try {
			ret = new InfraControllerBean().runInfraModule(sessionId);
		} catch (Exception e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Execute Failed, Method=runInfraModule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Execute, Method=runInfraModule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}

	/**
	 */
	public ModuleResult checkInfraModule(String sessionId, boolean verbose)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound, InfraModuleNotFound, SessionNotFound {
		logger.debug("checkInfraModule() managementId=" + sessionId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);

		ModuleResult ret = null;
		try {
			ret = new InfraControllerBean().checkInfraModule(sessionId, verbose);
		} catch (Exception e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Check Failed, Method=checkInfraModule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_NOTIFY + " Check, Method=checkInfraModule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		return ret;
	}

	/**
	 */
	public List<InfraCheckResult> getCheckResultList(String managementId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		logger.debug("getCheckResults");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ManagementID=");
		msg.append(managementId);
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=getCheckResultList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new InfraControllerBean().getCheckResultList(managementId);
	}
	
	public void addInfraFile(InfraFileInfo fileInfo,
			@XmlMimeType("application/octet-stream") DataHandler fileContent)
			throws InvalidRole, HinemosUnknown, InfraFileTooLarge, InvalidUserPass, InfraManagementDuplicate {
		logger.debug("addInfraFile");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(fileInfo != null){
			msg.append(", FileID=");
			msg.append(fileInfo.getFileId());
			msg.append(", FileName=");
			msg.append(fileInfo.getFileName());
		}

		try {
			new InfraControllerBean().addInfraFile(fileInfo, fileContent);
		} catch (InvalidRole | HinemosUnknown | InfraFileTooLarge | InfraManagementDuplicate e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_INFRA + " Add Failed, Method=addInfraFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		} finally {
			if (fileContent instanceof Closeable) {
				try {
					((Closeable)fileContent).close(); // removes the MIME part file
				} catch (IOException e) {
					logger.warn("addInfraFile : " + e.getMessage());
				}
			}
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_INFRA + " Add, Method=addInfraFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	public void modifyInfraFile(InfraFileInfo fileInfo,
			@XmlMimeType("application/octet-stream") DataHandler fileContent)
			throws InvalidRole, HinemosUnknown, InfraFileTooLarge, InvalidUserPass {
		logger.debug("modifyInfraFile");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(fileInfo != null){
			msg.append(", FileID=");
			msg.append(fileInfo.getFileId());
			msg.append(", FileName=");
			msg.append(fileInfo.getFileName());
		}

		try {
			new InfraControllerBean().modifyInfraFile(fileInfo, fileContent);
		} catch (InvalidRole | HinemosUnknown | InfraFileTooLarge e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_INFRA + " Modify Failed, Method=modifyInfraFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_INFRA + " Modify, Method=modifyInfraFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	@XmlMimeType("application/octet-stream")
	public DataHandler downloadInfraFile(String fileId, String fileName) throws InvalidUserPass, InvalidRole, HinemosUnknown, InfraFileNotFound, InvalidSetting, IOException  {
		logger.debug("downloadInfraFile");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(fileId != null){
			msg.append(", FileID=");
			msg.append(fileId);
			msg.append(", FileName=");
			msg.append(fileName);
		}

		DataHandler dh = null;
		try {
			 dh = new InfraControllerBean().downloadInfraFile(fileId, fileName);
		} catch (InvalidSetting | InfraFileNotFound e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_INFRA + " Download Failed, Method=downloadInfraFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_INFRA + " Download, Method=downloadInfraFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return dh;
	}
	
	/*
	 * (非JavaDoc)
	 * WinRMを利用したファイル配布モジュールで利用する
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadTransferFile(String fileName) throws Exception {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.HINEMOS_AGENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(fileName != null){
			msg.append(", FileName=");
			msg.append(fileName);
		}
		
		DataHandler dh = null;
		dh = new InfraControllerBean().downloadTransferFile(fileName);

		opelogger.info(HinemosModuleConstant.LOG_PREFIX_INFRA + " Download, Method=downloadTransferFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return dh;
	}

	public void deleteDownloadedInfraFile(String fileName) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		logger.debug("deleteDownloadedInfraFile");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(fileName != null){
			msg.append(", FileName=");
			msg.append(fileName);
		}

		try {
			new InfraControllerBean().deleteDownloadedInfraFile(fileName);
		} catch (Exception e) {
			opelogger.warn(HinemosModuleConstant.LOG_PREFIX_INFRA + " Delete Failed, Method=deleteDownloadedInfraFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		opelogger.info(HinemosModuleConstant.LOG_PREFIX_INFRA + " Delete, Method=deleteDownloadedInfraFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
	}
	
	public void deleteInfraFileList(List<String> fileIdList) throws InvalidUserPass, InvalidRole, HinemosUnknown, InfraFileNotFound, InfraFileBeingUsed {
		logger.debug("deleteInfraFileList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=getInfraFileList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		new InfraControllerBean().deleteInfraFileList(fileIdList);
	}

	public List<InfraFileInfo> getInfraFileList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		logger.debug("getInfraFileList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=getInfraFileList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new InfraControllerBean().getInfraFileList();
	}

	public List<InfraFileInfo> getInfraFileListByOwnerRoleId(String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		logger.debug("getInfraFileList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.INFRA, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		opelogger.debug(HinemosModuleConstant.LOG_PREFIX_INFRA + " Get, Method=getInfraFileListByOwnerRoleId, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new InfraControllerBean().getInfraFileListByOwnerRoleId(ownerRoleId);
	}

	public int getInfraMaxFileSize() {
		int infraMaxFileSize = 0;
		infraMaxFileSize = HinemosPropertyCommon.infra_max_file_size.getIntegerValue();
		return infraMaxFileSize;
	}
}
