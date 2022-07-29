/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.monitor.factory;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.log4j.Logger;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.factory.RpaManagementRestDefine;
import com.clustercontrol.rpa.factory.RpaManagementRestTokenCache;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.model.RpaManagementToolMst;
import com.clustercontrol.rpa.session.RpaControllerBean;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.factory.monitors.PlatformServiceRunMonitor;

import jakarta.persistence.EntityExistsException;

public class RunMonitorRpaManagementToolService extends RunMonitorTruthValueType {
	private static Logger m_log = Logger.getLogger(RunMonitorTruthValueType.class);
	private String m_messageOrg;
	private String m_message;
	
	// コンストラクタ
	public RunMonitorRpaManagementToolService() {
		super();
	}

	@Override
	protected RunMonitor createMonitorInstance() throws HinemosUnknown {
		return new RunMonitorRpaManagementToolService();
	}

	/**
	 * RPA管理ツールサービス監視を実行します。
	 * 
	 * RPA管理ツールサービス監視は、RPA管理ツールアカウントを表すスコープ単位で実行するため、独自のrunMonitorInfoを実装します。
	 * @see PlatformServiceRunMonitor#runMonitorInfo
	 * 
	 */
	@Override
	protected List<OutputBasicInfo> runMonitorInfo() throws FacilityNotFound, MonitorNotFound, EntityExistsException, InvalidRole, HinemosUnknown {
		m_log.debug("runMonitorInfo()");

		List<OutputBasicInfo> ret = Collections.emptyList();
		m_now = HinemosTime.getDateInstance();

		// 監視基本情報を設定
		if (!setMonitorInfo(m_monitorTypeId, m_monitorId)) {
			// 処理終了
			return ret;
		}

		// 判定情報を設定
		setJudgementInfo();

		// チェック条件情報を設定
		setCheckInfo();
		m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

		m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId + ", facilityId=" + m_facilityId);
		
		// 監視結果取得。
		OutputBasicInfo output = collectTargets(
				m_facilityId, 
				m_monitor.getRpaManagementToolServiceCheckInfo().getConnectTimeout(),
				m_monitor.getRpaManagementToolServiceCheckInfo().getRequestTimeout());
		
		m_log.debug("output=" + output);

		return Arrays.asList(output);
	}

	/**
	 * 監視の実態。
	 * 対象スコープ単位で実行される。
	 */
	public OutputBasicInfo collectTargets(String facilityId, int connectTimeout, int requestTimeout) throws FacilityNotFound, HinemosUnknown {
		// メッセージを定義(RPAスコープ以外が指定されていた場合、不明で通知する)
		m_message = MessageConstant.MESSAGE_PLEASE_SET_RPA_MANAGEMENT_TOOL_ACCOUNT_SCOPE.getMessage();
		m_messageOrg = MessageConstant.MESSAGE_PLEASE_SET_RPA_MANAGEMENT_TOOL_ACCOUNT_SCOPE.getMessage();


		if (m_isNode) {
			// 監視対象はRPA管理ツールアカウントを表すスコープのみ。
			return createOutputBasicInfo(true, facilityId, -1, m_now);
		}
		
		// 対象スコープが表しているRPA管理ツールアカウントを取得
		RpaManagementToolAccount rpaManagementToolAccount = null;
		RpaManagementToolMst master = null;
		for (RpaManagementToolAccount account : new RpaControllerBean().getRpaAccountList()) {
			try {
				master = QueryUtil.getRpaManagementToolMstPK(account.getRpaManagementToolId());
				if (facilityId.equals(RpaUtil.generateRpaManagementScopeId(account, master))) {
					rpaManagementToolAccount = account;
					break;
				}
			} catch (RpaManagementToolMasterNotFound e) {
				// 想定外例外
				throw new HinemosUnknown(e);
			}
		}
		
		if (rpaManagementToolAccount == null) {
			m_log.debug(String.format("scope is not RPA Scope. facilityId = %s", facilityId));
			return createOutputBasicInfo(true, facilityId, -1, m_now);
		}
		
		// サービス状態を確認
		OutputBasicInfo ret = checkAccount(rpaManagementToolAccount, facilityId, master, connectTimeout, requestTimeout);
		
		return ret;
	}
	
	private OutputBasicInfo checkAccount(RpaManagementToolAccount rpaManagementToolAccount, String facilityId, RpaManagementToolMst master, int connectTimeout, int requestTimeout) throws HinemosUnknown {
		// サービス状態を確認
		
		try (CloseableHttpClient client = RpaUtil.createHttpClient(rpaManagementToolAccount, connectTimeout, requestTimeout)) {
			// REST API定義クラスを取得
			RpaManagementRestDefine define = RpaUtil.getRestDefine(rpaManagementToolAccount.getRpaManagementToolId());
			m_messageOrg = "";
			m_message = "";

			// アカウント認証の正常性チェック(認証tokenの取得)
			String token = checkAuthentication(rpaManagementToolAccount, client, master.getRpaManagementToolType().getRpaManagementToolTypeName(), define);
			if (token.isEmpty()) {
				// tokenが取得できなかった場合、falseで通知
				m_message = MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_HEALTH_CHECK_FAILED.getMessage(master.getRpaManagementToolName());
				return createOutputBasicInfo(true, facilityId, TruthConstant.TYPE_FALSE, m_now);
			}
			m_messageOrg += "\n";
			// APIの正常性チェック
			String healthCheckInfo = checkApi(rpaManagementToolAccount, client, master.getRpaManagementToolType().getRpaManagementToolTypeName(), define, token);
			if (healthCheckInfo.isEmpty()) {
				// APIから取得できなかった場合、falseで通知
				m_message = MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_HEALTH_CHECK_FAILED.getMessage(master.getRpaManagementToolName());
				return createOutputBasicInfo(true, facilityId, TruthConstant.TYPE_FALSE, m_now);				
			}

			// 成功メッセージ定義
			m_message = MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_HEALTH_CHECK_SUCCESS.getMessage(master.getRpaManagementToolName());
			m_messageOrg += "\n";
			m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_HEALTH_CHECK_SUCCESS.getMessage(master.getRpaManagementToolName());
			return createOutputBasicInfo(true, facilityId, TruthConstant.TYPE_TRUE, m_now);
		} catch (Exception e) {
			// 想定外例外
			throw new HinemosUnknown(e);
		}
	}
	
	/**
	 * アカウント認証の正常性チェック
	 * 正常に取得した場合、認証tokenを返す。
	 * 取得に失敗した場合、空文字を返す。
	 */
	private String checkAuthentication(RpaManagementToolAccount rpaManagementToolAccount, CloseableHttpClient client, String managementToolTypeName, RpaManagementRestDefine define) {
		String token;
		m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_GET_TOKEN_TRY.getMessage(
				managementToolTypeName, 
				define.getGenerateTokenRequestUrl(rpaManagementToolAccount.getUrl())
				);

		try {
			// 認証tokenを取得(ログイン認証の正常性チェック)
			token = RpaManagementRestTokenCache.getInstance().getToken(rpaManagementToolAccount, define, client, true);
			m_messageOrg += "\n";
			m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_GET_TOKEN_SUCCESS.getMessage(managementToolTypeName);
		} catch (IOException e) {
			m_log.warn(e.getMessage(), e);
			if (e instanceof ConnectTimeoutException || e instanceof UnknownHostException || e instanceof SocketException) {
				// 管理ツールサービスへのアクセス失敗
				m_messageOrg += "\n";
				m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_ACCESS_FAILED.getMessage(managementToolTypeName);
				
			} else {
				// token取得失敗
				m_messageOrg += "\n";
				m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_GET_TOKEN_FAILED.getMessage(managementToolTypeName);
			}
			token = "";
		}

		return token;
	}

	/**
	 * APIの正常性チェック
	 * 正常に取得した場合、取得した情報を返す。
	 * 取得に失敗した場合、空文字を返す。
	 */
	private String checkApi(RpaManagementToolAccount rpaManagementToolAccount, CloseableHttpClient client, String managementToolTypeName, RpaManagementRestDefine define, String token) {
		String result;
		String healthCheckInfo = define.getHealthCheckInfo().getMessage();
		m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_ACCESS_API_TRY.getMessage(
				managementToolTypeName, 
				define.getHealthCheckRequestUrl(rpaManagementToolAccount.getUrl()),
				healthCheckInfo
				);

		try {
			// APIから情報を取得
			result = define.healthCheck(rpaManagementToolAccount.getUrl(), token, client);
			m_messageOrg += "\n";
			m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_ACCESS_API_SUCCESS.getMessage(managementToolTypeName, healthCheckInfo);
		} catch (IOException e) {
			m_log.warn(e.getMessage(), e);
			result = "";
			if (e instanceof ConnectTimeoutException || e instanceof UnknownHostException) {
				// 管理ツールサービスへのアクセス失敗
				m_messageOrg += "\n";
				m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_ACCESS_FAILED.getMessage(managementToolTypeName);
			} else {
				// APIからの情報取得失敗
				m_messageOrg += "\n";
				m_messageOrg += MessageConstant.MESSAGE_RPA_MANAGEMENT_TOOL_SERVICE_MON_ACCESS_API_FAILED.getMessage(managementToolTypeName);
			}
		}

		return result;
	}


	@Override
	public boolean collect(String facilityId) {
		throw new UnsupportedOperationException();
	}
	@Override
	public int getPriority(int key) {
		if (key != TruthConstant.TYPE_TRUE && key != TruthConstant.TYPE_FALSE) {
			// 重要度:不明で通知を行う。(監視結果が得られなかった場合)
			return PriorityConstant.TYPE_UNKNOWN;
		} else {
			// 監視設定で指定された重要度で通知。
			return super.getPriority(key);
		}
	}

	@Override
	public String getMessageOrg(int key) {
		return m_messageOrg;
	}
	
	@Override
	public String getMessage(int key) {
		return m_message;
	}

	@Override
	protected void setCheckInfo() throws MonitorNotFound {
	}

	@Override
	protected OutputBasicInfo createOutputBasicInfo(
			boolean isNode,
			String facilityId,
			int result,
			Date generationDate) throws HinemosUnknown {

		if (m_isMonitorJob) {
			// 監視ジョブからの実行の場合、ジョブ履歴向けの実行結果を格納する。
			m_monitorRunResultInfo = new MonitorRunResultInfo();
			m_monitorRunResultInfo.setPriority(getPriority(result));
			m_monitorRunResultInfo.setNodeDate(HinemosTime.currentTimeMillis());
			m_monitorRunResultInfo.setMessageOrg(getMessageOrg(result));
		}

		return super.createOutputBasicInfo(isNode, facilityId, result, generationDate);
	}
}
