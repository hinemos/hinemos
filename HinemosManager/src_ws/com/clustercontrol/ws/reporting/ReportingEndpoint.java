/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.reporting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.bean.TemplateSetDetailInfo;
import com.clustercontrol.reporting.bean.TemplateSetInfo;
import com.clustercontrol.reporting.fault.ReportingDuplicate;
import com.clustercontrol.reporting.fault.ReportingNotFound;
import com.clustercontrol.platform.util.reporting.ExecReportingProcess;
import com.clustercontrol.reporting.session.ReportingControllerBean;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * レポーティング用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://reporting.ws.clustercontrol.com")
public class ReportingEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static final String LOG_PREFIX_REPORTING = "[Reporting]";
	private static final String REPORTING = "Reporting";

	private static Log m_log = LogFactory.getLog( ReportingEndpoint.class );
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
	 * レポーティング情報を追加します。
	 * 
	 * ReportingAdd権限が必要
	 * 
	 * @throws ReportingDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * 
	 */
	public boolean addReporting(ReportingInfo info) throws HinemosUnknown, ReportingDuplicate, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("addReporting");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", ReportScheduleID=");
			msg.append(info.getReportScheduleId());
		}

		try {
			ret = new ReportingControllerBean().addReporting(info);
		} catch (Exception e) {
			m_opelog.warn(LOG_PREFIX_REPORTING + " Add Failed, Method=addReporting, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(LOG_PREFIX_REPORTING + " Add, Method=addReporting, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}

	/**
	 * レポーティング情報を変更します。
	 * 
	 * ReportingModify権限が必要
	 * 
	 * @throws ReportingNotFound
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public boolean modifyReporting(ReportingInfo info) throws HinemosUnknown, NotifyNotFound, ReportingNotFound, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("modifyReporting");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", ReportScheduleID=");
			msg.append(info.getReportScheduleId());
		}

		try {
			ret = new ReportingControllerBean().modifyReporting(info);
		} catch (Exception e) {
			m_opelog.warn(LOG_PREFIX_REPORTING + " Change Failed, Method=modifyReporting, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(LOG_PREFIX_REPORTING + " Change, Method=modifyReporting, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}

	/**
	 * レポーティング情報を削除します。
	 * 
	 * ReportingModify権限が必要
	 * 
	 * @throws ReportingNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public boolean deleteReporting(String reportId) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("deleteReporting");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ReportID=");
		msg.append(reportId);

		try {
			ret = new ReportingControllerBean().deleteReporting(reportId);
		} catch (Exception e) {
			m_opelog.warn(LOG_PREFIX_REPORTING + " Delete Failed, Method=deleteReporting, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(LOG_PREFIX_REPORTING + " Delete, Method=deleteReporting, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}

	/**
	 * レポーティング情報を取得します。
	 *
	 * ReportingRead権限が必要
	 *
	 * @return
	 * @throws HinemosUnknown
	 * @throws ReportingNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public ReportingInfo getReportingInfo(String reportId) throws ReportingNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getReportingInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ReportID=");
		msg.append(reportId);
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=getReportingInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new ReportingControllerBean().getReportingInfo(reportId);
	}

	/**
	 * レポーティング情報の一覧を取得します。<BR>
	 * 
	 * ReportingRead権限が必要
	 * 
	 * @return レポーティング情報の一覧を保持するArrayList
	 * @throws ReportingNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public ArrayList<ReportingInfo> getReportingList() throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getReportingList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=getReportingList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new ReportingControllerBean().getReportingList();
	}

	/**
	 * レポーティングの有効、無効を変更するメソッドです。
	 * 
	 * ReportingWrite権限が必要
	 * 
	 * @throws HinemosUnknown
	 * @throws ReportingNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public void setReportingStatus(String reportId, boolean validFlag) throws NotifyNotFound, ReportingNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("setReportingStatus");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ReportID=");
		msg.append(reportId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			new ReportingControllerBean().setReportingStatus(reportId, validFlag);
		} catch (Exception e) {
			m_opelog.warn(LOG_PREFIX_REPORTING + " Change Valid Failed, Method=setReportingStatus, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(LOG_PREFIX_REPORTING + " Change Valid, Method=setReportingStatus, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 指定したレポートIDに対するレポート情報を作成する。
	 * 本メソッドが終了した時点で、Hinemosマネージャ上のレポート作成スレッドを開始し、作成されるファイル名のリストを返却する
	 * 
	 * ReportingRead権限が必要
	 * 
	 * @param reportId
	 * @return レポートファイル名のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public List<String> createReportingFile(String reportId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		return createReportingFileWithParam(reportId, null);
	}

	/**
	 * 指定したレポートIDに対するレポート情報を作成する。
	 * 追加パラメータとして即時実行時に指定するパラメータを格納するReportingInfoを渡す。
	 * 本メソッドが終了した時点で、Hinemosマネージャ上のレポート作成スレッドを開始し、作成されるファイル名のリストを返却する
	 * 
	 * ReportingRead権限が必要
	 * 
	 * @param reportId
	 * @param tmpReportingInfo
	 * @return レポートファイル名のリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public List<String> createReportingFileWithParam(String reportId, ReportingInfo tmpReportingInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("createReportingFile()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		List<String> ret = null;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ReportID=");
		msg.append(reportId);

		try {
			ret = new ReportingControllerBean().runReporting(reportId, tmpReportingInfo);
		} catch (Exception e) {
			m_opelog.warn(LOG_PREFIX_REPORTING + " Download Failed, Method=createReportingFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(LOG_PREFIX_REPORTING + " Download, Method=createReportingFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * レポートファイルをDLする
	 * 
	 * ReportingRead権限が必要
	 * 
	 * @param filepath
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadReportingFile(String fileName) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(fileName);

		File file = new File(ExecReportingProcess.getBasePath() + File.separator + fileName);
		if(!file.exists()) {
			m_log.info("file not found : " + file.getAbsolutePath());
			m_opelog.info(LOG_PREFIX_REPORTING + " Download Failed, Method=downloadReportingFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			return null;
		}
		m_log.info("file found : " + file.getAbsolutePath());
		m_opelog.info(LOG_PREFIX_REPORTING + " Download, Method=downloadReportingFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		FileDataSource source = new FileDataSource(file);
		DataHandler dataHandler = new DataHandler(source);
		return dataHandler;
	}
	
	/**
	 * テンプレートセット情報を追加します。
	 * 
	 * ReportingAdd権限が必要
	 * 
	 * @throws ReportingDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * 
	 */
	public boolean addTemplateSet(TemplateSetInfo info) throws HinemosUnknown, ReportingDuplicate, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("addTemplateSet");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", TemplateSetID=");
			msg.append(info.getTemplateSetId());
		}

		try {
			ret = new ReportingControllerBean().addTemplateSet(info);
		} catch (Exception e) {
			m_opelog.warn(LOG_PREFIX_REPORTING + " Add Failed, Method=addTemplateSet, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(LOG_PREFIX_REPORTING + " Add, Method=addTemplateSet, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}

	/**
	 * テンプレートセット情報を変更します。
	 * 
	 * ReportingModify権限が必要
	 * 
	 * @throws ReportingNotFound
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public boolean modifyTemplateSet(TemplateSetInfo info) throws HinemosUnknown, NotifyNotFound, ReportingNotFound, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("modifyTemplateSet");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", TemplateSetID=");
			msg.append(info.getTemplateSetId());
		}

		try {
			ret = new ReportingControllerBean().modifyTemplateSet(info);
		} catch (Exception e) {
			m_opelog.warn(LOG_PREFIX_REPORTING + " Change Failed, Method=modifyTemplateSet, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(LOG_PREFIX_REPORTING + " Change, Method=modifyTemplateSet, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}

	/**
	 * テンプレートセット情報を削除します。
	 * 
	 * ReportingModify権限が必要
	 * 
	 * @throws ReportingNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public boolean deleteTemplateSet(String templateSetId) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("deleteTemplateSet");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean ret = false;
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", TemplateSetID=");
		msg.append(templateSetId);

		try {
			ret = new ReportingControllerBean().deleteTemplateSet(templateSetId);
		} catch (Exception e) {
			m_opelog.warn(LOG_PREFIX_REPORTING + " Delete Failed, Method=deleteTemplateSet, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(LOG_PREFIX_REPORTING + " Delete, Method=deleteTemplateSet, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return ret;
	}
	
	
	/**
	 * オーナーロールIDを条件としてテンプレートセット情報の一覧を取得します。<BR>
	 * 
	 * ReportingRead権限が必要
	 * 
	 * @param ownerRoleId
	 * @return テンプレートセット情報の一覧を保持するArrayList
	 * @throws ReportingNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * 
	 */
	public ArrayList<TemplateSetInfo> getTemplateSetList(String ownerRoleId) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getTemplateSetList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=getTemplateSetList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new ReportingControllerBean().getTemplateSetListByOwnerRole(ownerRoleId);
	}
	
	/**
	 * テンプレートセットIDを基にテンプレートセット情報を取得します。
	 * 
	 * ReportingRead権限が必要
	 * 
	 * @param templateSetId
	 * @return
	 * @throws HinemosUnknown
	 * @throws ReportingNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public TemplateSetInfo getTemplateSetInfo(String templateSetId) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getTemplateSetInfo");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=getTemplateSetInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new ReportingControllerBean().getTemplateSetInfo(templateSetId);
	}
	
	/**
	 * テンプレートセットIDを基にテンプレートセット詳細情報のリストを取得します。
	 * 
	 * ReportingRead権限が必要
	 * 
	 * @param templateSetId
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 */
	public ArrayList<TemplateSetDetailInfo> getTemplateSetDetailInfoList(String templateSetId) throws InvalidRole, HinemosUnknown, InvalidUserPass {
		m_log.debug("getTemplateSetDetailInfoList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=getTemplateSetDetailInfoList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new ReportingControllerBean().getTemplateSetDetailInfoList(templateSetId);
	}
	
	/**
	 * テンプレートIDのリストを取得します。
	 * 
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getTemplateIdList(String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getTemplateIdList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=getTemplateIdList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new ReportingControllerBean().getTemplateIdList(ownerRoleId);
	}
	
	/**
	 * 対応可能な出力形式を取得します。 
	 * 
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getReportOutputTypeStrList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getReportOutputTypeStrList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=getReportOutputTypeStrList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new ReportingControllerBean().getReportOutputTypeStrList();
	}
	
	/**
	 * 出力形式の数値を文字列に変更して返します。
	 * 
	 * @param type
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String outputTypeToString(int type) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("outputTypeToString");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=outputTypeToString, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new ReportingControllerBean().outputTypeToString(type);
	}
	
	/**
	 * 出力形式の文字列を数値に変更して返します。
	 * 
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public int outputStringToType(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("outputStringToType");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(REPORTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(LOG_PREFIX_REPORTING + " Get, Method=outputStringToType, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new ReportingControllerBean().outputStringToType(str);
	}

	public String getVersion() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		String version = "1.0";
		// TODO 次版でこのtry catchは削除すること
		try {
			version = KeyCheck.getResultEnterprise();
		} catch (NoSuchMethodError e) {
			
		}
		return version;
	}
}