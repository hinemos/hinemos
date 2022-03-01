/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.RequestConfig.Builder;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.RpaManagementRestConnectFailed;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.maintenance.bean.MaintenanceTypeMstConstant;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceTypeMst;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.rpa.factory.RpaManagementRestDefine;
import com.clustercontrol.rpa.factory.RpaManagementRestTokenCache;
import com.clustercontrol.rpa.factory.bean.RpaResourceInfo;
import com.clustercontrol.rpa.model.RpaAbstructPattern;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.model.RpaManagementToolMst;
import com.clustercontrol.util.Messages;
import com.clustercontrol.winservice.model.WinServiceCheckInfo;

public class RpaUtil {
	private static Log m_log = LogFactory.getLog(RpaUtil.class);
	/**
	 * 指定されたログパターンでマッチングを行い、マッチするか判定する
	 * @param message
	 * @param logPattern
	 * @return マッチしたらtrue, しなければfalse
	 */
	public static boolean patternMatch(String message, RpaAbstructPattern logPattern){
		m_log.debug(String.format("patternMatch() start. message=\"%s\", pattern=\"%s\"", message, logPattern.getPattern()));
		Pattern pattern;
		if (!logPattern.getCaseSensitivityFlg()) {
			// 大文字・小文字を区別しない場合
			pattern = Pattern.compile(logPattern.getPattern(), Pattern.CASE_INSENSITIVE);
		} else {
			// 大文字・小文字を区別する場合
			pattern = Pattern.compile(logPattern.getPattern());
		}
		Matcher matcher = pattern.matcher(message);
		m_log.debug(String.format("patternMatch() result=\"%s\" message=\"%s\", pattern=\"%s\"", matcher.matches(), message, logPattern.getPattern()));
		return matcher.matches();
	}
	
	/**
	 * 手動操作時間「分:秒」(文字列)をLongに変換する。
	 * @param manualTimeEx
	 * @return 変換出来なければnull
	 */
	public static Long parseManualTimeEx(String manualTimeEx) {
		Matcher matcher = Pattern.compile("^(\\d*):(\\d{2})$").matcher(manualTimeEx);

		if (matcher.find()) {
			String minuteEx = matcher.group(1);
			String secondEx = matcher.group(2);
			return (Long.parseLong(minuteEx) * 60 + Long.parseLong(secondEx)) * 1000;
		} else {
			return null;

		}
	}

	/**
	 * 手動操作時間 (Long)を文字列「分:秒」に変換する。
	 * @param manualTimeEx
	 * @return
	 */
	public static String convertManualTimeEx(Long manualTime) {
		if (manualTime == null) {
			return null;
		}
		long minute = manualTime / 60000;
		long second = (manualTime % 60000) / 1000;
		return String.format("%d:%02d", minute, second);
	}
	
	/**
	 * RPA管理ツールアカウント設定からHTTPクライアントを作成する
	 */
	public static CloseableHttpClient createHttpClient(RpaManagementToolAccount account) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, NullPointerException {
		if (account.getProxyFlg()) {
			return createHttpClient(account.getProxyUrl(), account.getProxyPort(), account.getProxyUser(), account.getProxyPassword());
		} else {
			return createHttpClient(null, null, null, null);
		}
	}

	/**
	 * RPA管理ツールアカウント設定からコネクションタイムアウトとリクエストタイムアウトを指定してHTTPクライアントを作成する
	 */
	public static CloseableHttpClient createHttpClient(RpaManagementToolAccount account, int connectTimeout, int requestTimeout) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, NullPointerException {
		if (account.getProxyFlg()) {
			return createHttpClient(
					connectTimeout,
					requestTimeout,
					account.getProxyUrl(), account.getProxyPort(), account.getProxyUser(), account.getProxyPassword(),
					HinemosPropertyCommon.rpa_management_rest_client_config_ssl_trustall.getBooleanValue()
					);
		} else {
			return createHttpClient(
					connectTimeout,
					requestTimeout,
					null, null, null, null,
					HinemosPropertyCommon.rpa_management_rest_client_config_ssl_trustall.getBooleanValue()
					);
		}
	}

	/**
	 * REST API用のHTTPクライアントを作成する。
	 * @param proxyUrl
	 * @param proxyPort
	 * @param proxyUser
	 * @param proxyPassword
	 * @return CloseableHttpClient
	 * @throws NullPointerException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	public static CloseableHttpClient createHttpClient(
			String proxyUrl,
			Integer proxyPort,
			String proxyUser,
			String proxyPassword) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, NullPointerException {
		// タイムアウト値をHinemosプロパティから取得する
		return createHttpClient(
				HinemosPropertyCommon.rpa_management_rest_client_config_connection_timeout.getIntegerValue(),
				HinemosPropertyCommon.rpa_management_rest_client_config_read_timeout.getIntegerValue(),
				proxyUrl, proxyPort, proxyUser, proxyPassword,
				HinemosPropertyCommon.rpa_management_rest_client_config_ssl_trustall.getBooleanValue());
	}
	
	public static CloseableHttpClient createHttpClient(
			Integer connectTimeout,
			Integer requestTimeout,
			String proxyUrl,
			Integer proxyPort,
			String proxyUser,
			String proxyPassword,
			boolean sslTrustAll) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		
		CredentialsStore credentialsStore = null;
		HttpHost proxyHost = null;
		if (proxyUrl != null && proxyPort != null) {
			// プロキシ設定作成
			String proxyHostName = null;
			String proxyScheme = null;

			try {
				// プロキシのホスト名抽出
				URI proxyUri = new URI(proxyUrl);
				proxyHostName = proxyUri.getHost();
				proxyScheme = proxyUri.getScheme();				
			} catch (URISyntaxException e) {
				// URLのフォーマットはチェック済の想定
				m_log.warn("createHttpClient():" + e.getClass().getSimpleName() + ", " + e.getMessage());
			}

			if (proxyHostName != null && proxyScheme != null) {
				proxyHost = new HttpHost(proxyScheme, proxyHostName, proxyPort);
				if (proxyUser != null && proxyPassword != null) {
					credentialsStore = new BasicCredentialsProvider();
					credentialsStore.setCredentials(new AuthScope(proxyHost), new UsernamePasswordCredentials(proxyUser, proxyPassword.toCharArray()));
				}
			}
		}
		
		List<Header> headers = new ArrayList<>();
		HttpClientBuilder builder = HttpClients.custom()
				.setDefaultCredentialsProvider(credentialsStore)
				.setDefaultHeaders(headers)
				.setProxy(proxyHost);
		
		Builder requestBuilder = RequestConfig.custom().setCookieSpec(StandardCookieSpec.RELAXED);
		PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();

		if (connectTimeout != null)
			requestBuilder.setConnectTimeout(Timeout.ofMilliseconds(connectTimeout));
		
		if (requestTimeout != null){
			requestBuilder.setResponseTimeout((Timeout.ofMilliseconds(requestTimeout)));
			SocketConfig socketconfig = SocketConfig.custom()
					.setSoTimeout(Timeout.ofMilliseconds(requestTimeout)).build();
			connectionManagerBuilder.setDefaultSocketConfig(socketconfig);
		}
		
		if (sslTrustAll) {
			// SSL の認証カット
			TrustStrategy trustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			};
			connectionManagerBuilder.setSSLSocketFactory(
					new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, trustStrategy).build(),
					new NoopHostnameVerifier()));
		}

		builder.setConnectionManager(connectionManagerBuilder.build());
		
		builder.setDefaultRequestConfig(requestBuilder.build());
		headers.add(new BasicHeader(HttpHeaders.CONNECTION, HttpHeaders.KEEP_ALIVE));
		return builder.build();
	}
	
	/**
	 * RPA管理ツールアカウントの接続、認証チェックを行う
	 * @throws RpaManagementToolMasterNotFound 
	 * @throws HinemosUnknown 
	 * @throws RpaManagementRestConnectFailed 
	 */
	public static void checkAuthentification(
			String rpaScopeId,
			String rpaManagementToolId,
			String url,
			String accountId,
			String password,
			String tenantId,
			String proxyUrl,
			Integer proxyPort,
			String proxyUser,
			String proxyPassword
			) throws RpaManagementToolMasterNotFound, HinemosUnknown, RpaManagementRestConnectFailed {
		// RPA管理ツールIDからREST API定義クラスを取得
		RpaManagementRestDefine define = getRestDefine(rpaManagementToolId);
		
		// クライアントを作成しtoken取得を行う。
		try (CloseableHttpClient client = createHttpClient(proxyUrl, proxyPort, proxyUser, proxyPassword)) {
			String token = RpaManagementRestTokenCache.getInstance().getToken(rpaScopeId, url, accountId, password, tenantId, define, client, true);
			m_log.debug(String.format("checkAuthentification() : accessToken=%s", token));
		} catch (IOException e) {
			m_log.warn(e.getMessage(), e);
			throw new RpaManagementRestConnectFailed(e);
		} catch (Exception e) {
			m_log.warn(e.getMessage(), e);
			throw new HinemosUnknown(e);
		}
		
	}
	
	/**
	 * REST API定義クラス(@see RpaManagementRestDefine)を取得する。
	 * @param rpaManagementToolId RPA管理ツールID
	 * @return 各管理ツール向けのRpaManagementRestDefine継承クラス
	 * @throws RpaManagementToolMasterNotFound 
	 * @throws HinemosUnknown 
	 */
	public static RpaManagementRestDefine getRestDefine(String rpaManagementToolId) throws RpaManagementToolMasterNotFound, HinemosUnknown {		
		// RPA管理ツールマスタを取得
		RpaManagementToolMst master = QueryUtil.getRpaManagementToolMstPK(rpaManagementToolId);
		return getRestDefine(master);
	}
	
	/**
	 * REST API定義クラス(@see RpaManagementRestDefine)を取得する。
	 * @param master RPA管理ツールマスタ
	 * @return 各管理ツール向けのRpaManagementRestDefine継承クラス
	 * @throws HinemosUnknown 
	 */
	public static RpaManagementRestDefine getRestDefine(RpaManagementToolMst master) throws HinemosUnknown {
		// RPA管理ツールタイプから定義クラス名を取得
		String defineClassName = master.getRpaManagementToolType().getApiDefineClassName();
		try {
			// APIバージョンを引数にコンストラクタを呼び出し
			return (RpaManagementRestDefine)Class.forName(defineClassName)
					.getConstructor(int.class)
					.newInstance(master.getApiVersion());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			// マスタが取得できればクラスをロードできないことはないはずなので、ここには入らない想定
			m_log.warn(e.getMessage(), e);
			throw new HinemosUnknown(e);
		}
	}

	/**
	 * RPA管理機能用の組み込みスコープを作成
	 */
	public static List<ScopeInfo> createBuiltInRpaScopes() {
		List<ScopeInfo> rpaScopes = new ArrayList<>();

		// RPA
		ScopeInfo rpaScope = new ScopeInfo(RpaConstants.RPA);
		rpaScope.setFacilityName("RPA");
		rpaScope.setDescription("RPA");
		rpaScopes.add(rpaScope);
		
		// 管理製品なし(WinActor)
		ScopeInfo noMgrWinActor = new ScopeInfo(RpaConstants.RPA_NO_MGR_WINACTOR);
		noMgrWinActor.setFacilityName(String.format("$[%s]", "RPA_NO_MGR_WINACTOR"));
		noMgrWinActor.setDescription(Messages.getString("RPA_NO_MGR_WINACTOR"));
		rpaScopes.add(noMgrWinActor);
		
		// 管理製品なし(UiPath)
		ScopeInfo noMgrUiPath = new ScopeInfo(RpaConstants.RPA_NO_MGR_UIPATH);
		noMgrUiPath.setFacilityName(String.format("$[%s]", "RPA_NO_MGR_UIPATH"));
		noMgrUiPath.setDescription(Messages.getString("RPA_NO_MGR_UIPATH"));
		rpaScopes.add(noMgrUiPath);
		
		// 共通設定
		for (ScopeInfo scopeInfo : rpaScopes) {
			scopeInfo.setOwnerRoleId(RoleIdConstant.ADMINISTRATORS);
			scopeInfo.setDisplaySortOrder(100);
			scopeInfo.setValid(true);
			scopeInfo.setCreateUserId(UserIdConstant.HINEMOS);
			scopeInfo.setModifyUserId(UserIdConstant.HINEMOS);
		}
		
		return rpaScopes;
	}
	
	
	/**
	 * RPA管理機能用の組み込みスコープのリレーションを作成
	 */
	public static List<FacilityRelationEntity> createBuiltInRpaRelation() {
		List<FacilityRelationEntity> ret = new ArrayList<>();
		ret.add(new FacilityRelationEntity(RpaConstants.RPA, RpaConstants.RPA_NO_MGR_WINACTOR));
		ret.add(new FacilityRelationEntity(RpaConstants.RPA, RpaConstants.RPA_NO_MGR_UIPATH));
		
		return ret;
	}
	
	/**
	 * RPA管理機能用組み込みスコープのオブジェクト権限を作成
	 * 
	 * PPA,RPA_NO_MGR_WINACTOR,RPA_NO_MGR_UIPATHを対象に
	 * ALL_USERS[READ,MODIFY] を設定する
	 */
	public static List<ObjectPrivilegeInfo> createBuiltInRpaScopeObjectPrivilege() {
		List<ObjectPrivilegeInfo> ret = new ArrayList<ObjectPrivilegeInfo>();

		List<String> targetScopeList =  new ArrayList<String>();
		targetScopeList.add(RpaConstants.RPA);
		targetScopeList.add(RpaConstants.RPA_NO_MGR_WINACTOR);
		targetScopeList.add(RpaConstants.RPA_NO_MGR_UIPATH);
		
		List<String> settingPrivilegeList =  new ArrayList<String>();
		settingPrivilegeList.add(ObjectPrivilegeMode.READ.name());
		settingPrivilegeList.add(ObjectPrivilegeMode.MODIFY.name());
		
		for(String target :targetScopeList){
			for(String settingPrivilege :settingPrivilegeList){
				ObjectPrivilegeInfo privilege = new ObjectPrivilegeInfo();
				privilege.setRoleId(RoleIdConstant.ALL_USERS);
				privilege.setObjectId(target);
				privilege.setObjectPrivilege(settingPrivilege);
				privilege.setObjectType(HinemosModuleConstant.PLATFORM_REPOSITORY);
				privilege.setCreateUserId(UserIdConstant.HINEMOS);
				privilege.setModifyUserId(UserIdConstant.HINEMOS);
				ret.add(privilege);
			}
		}

		return ret;
	}
	
	/**
	 * 組み込み設定として登録するRPA管理ツールのミドルウェア監視設定(Windowsサービス監視)を作成
	 */
	public static List<MonitorInfo> createBuiltInMiddlewareMonitor() {
		List<MonitorInfo> ret = new ArrayList<>();
		
		for (Entry<String, List<String>> entry : getRpaManagementToolMiddleware().entrySet()) {
			// RPA管理ツール名
			String tool = entry.getKey();
			for (String middleware : entry.getValue()) {
				MonitorInfo info = new MonitorInfo();
				String monitorId = createBuiltInMiddlewareMonitorId(tool, middleware);
				info.setMonitorId(monitorId);
				
				info.setMonitorType(MonitorTypeConstant.TYPE_TRUTH);
				info.setMonitorTypeId(HinemosModuleConstant.MONITOR_WINSERVICE);
				info.setOwnerRoleId(RoleIdConstant.ADMINISTRATORS);
				info.setFacilityId(FacilityTreeAttributeConstant.REGISTERED_SCOPE);
				info.setApplication(monitorId);
				info.setMonitorFlg(false);
				info.setCollectorFlg(false);
				info.setRunInterval(300);
				info.setDelayTime(0);
				info.setFailurePriority(PriorityConstant.TYPE_UNKNOWN);
				info.setNotifyGroupId(NotifyGroupIdGenerator.generate(info));
				info.setPredictionFlg(false);
				info.setChangeFlg(false);
				info.setDescription(String.format("monitor %s service as middleware of %s", middleware, tool));
				
				WinServiceCheckInfo checkInfo = new WinServiceCheckInfo();
				checkInfo.setMonitorId(monitorId);
				checkInfo.setServiceName(middleware);
				info.setWinServiceCheckInfo(checkInfo);
				
				List<MonitorTruthValueInfo> truthValueInfo = new ArrayList<>();
				truthValueInfo.add(new MonitorTruthValueInfo(monitorId, PriorityConstant.TYPE_INFO, TruthConstant.TYPE_TRUE));
				truthValueInfo.add(new MonitorTruthValueInfo(monitorId, PriorityConstant.TYPE_CRITICAL, TruthConstant.TYPE_FALSE));
				info.setTruthValueInfo(truthValueInfo);
				
				info.setRegUser(UserIdConstant.HINEMOS);
				info.setUpdateUser(UserIdConstant.HINEMOS);
				
				ret.add(info);
			}
		}
		
		return ret;
	}
	
	/**
	 * 組み込み設定として登録するRPA管理ツールのミドルウェア監視項目のIDを作成
	 */
	public static String createBuiltInMiddlewareMonitorId(String rpaManagementTool, String middleware) {
		return String.join("_", 
				rpaManagementTool.replace(' ', '_').replace('.', '_').toUpperCase(),
				middleware);		
	}
	/**
	 * 組み込み設定として登録するRPA管理ツールのミドルウェア監視設定のオブジェクト権限を作成
	 */
	public static List<ObjectPrivilegeInfo> createBuiltInMiddlewareMonitorObjectPrivilege(String monitorId) {
		List<ObjectPrivilegeInfo> info = new ArrayList<>();
		
		ObjectPrivilegeInfo privilege = new ObjectPrivilegeInfo();
		privilege.setRoleId(RoleIdConstant.ALL_USERS);
		privilege.setObjectId(monitorId);
		privilege.setObjectPrivilege(ObjectPrivilegeMode.READ.name());
		privilege.setObjectType(HinemosModuleConstant.MONITOR);
		privilege.setCreateUserId(UserIdConstant.HINEMOS);
		privilege.setModifyUserId(UserIdConstant.HINEMOS);
		info.add(privilege);
		
		return info;
	}
	
	/**
	 * RPA管理ツール毎のミドルウェアを返す。
	 */
	private static Map<String, List<String>> getRpaManagementToolMiddleware() {
		Map<String, List<String>> ret = new HashMap<>();
		ret.put("WinDirector", new ArrayList<>());
		ret.get("WinDirector").add("Tomcat9");
		ret.get("WinDirector").add("wam-service");
		
		ret.put("WinDirector 2.2", new ArrayList<>());
		ret.get("WinDirector 2.2").add("postgresql-x64-9.6");

		ret.put("WinDirector 2.3", new ArrayList<>());
		ret.get("WinDirector 2.3").add("postgresql-x64-9.6");

		ret.put("WinDirector 2.4", new ArrayList<>());
		ret.get("WinDirector 2.4").add("postgresql-x64-11");

		ret.put("UiPath Orchestrator", new ArrayList<>());
		// IIS
		ret.get("UiPath Orchestrator").add("W3SVC");
		ret.get("UiPath Orchestrator").add("WAS");

		return ret;
	}

	/**
	 * 組み込み設定として登録するRPAシナリオ実績の履歴削除種別マスタを作成する
	 */
	public static MaintenanceTypeMst createBuiltInRpaMaintenanceTypeMst() {
		MaintenanceTypeMst operationResult = new MaintenanceTypeMst();
		operationResult.setType_id(MaintenanceTypeMstConstant.DELETE_RPA_SCENARIO_OPERATION_RESULT);
		operationResult.setOrder_no(1000);
		operationResult.setName_id("maintenance.delete_rpa_scenario_operation_result");
		
		return operationResult;
	}
	
	/**
	 * 組み込み設定として登録するRPAシナリオ実績の履歴削除設定を作成する
	 */
	public static List<MaintenanceInfo> createBuiltInRpaMaintenance() {
		List<MaintenanceInfo> ret = new ArrayList<>();
		MaintenanceInfo operationResult = new MaintenanceInfo();
		operationResult.setMaintenanceId(RpaConstants.rpaScenarioOperationResultMaintenanceId);
		operationResult.setDescription("RPA Scenario Operation Result deletion");
		operationResult.setTypeId(MaintenanceTypeMstConstant.DELETE_RPA_SCENARIO_OPERATION_RESULT);
		// 二ヶ月分を保持
		operationResult.setDataRetentionPeriod(62);
		
		// 毎日5:50に実行
		Schedule schedule = new Schedule(ScheduleConstant.TYPE_DAY, null, null, null, 5, 50);
		operationResult.setSchedule(schedule);
		
		operationResult.setNotifyGroupId(NotifyGroupIdGenerator.generate(operationResult));
		operationResult.setApplication(RpaConstants.rpaScenarioOperationResultMaintenanceId);
		operationResult.setValidFlg(true);

		operationResult.setOwnerRoleId(RoleIdConstant.ADMINISTRATORS);
		operationResult.setRegUser(UserIdConstant.HINEMOS);
		operationResult.setUpdateUser(UserIdConstant.HINEMOS);
		
		ret.add(operationResult);
		return ret;		
	}
	
	/**
	 * RPA管理ツールとして登録するスコープのファシリティIDを生成する。
	 * @param account
	 * @param master
	 * @return ファシリティID:_[RPA管理ツールタイプ]_[RPAスコープID]
	 */
	public static String generateRpaManagementScopeId(RpaManagementToolAccount account, RpaManagementToolMst master) {
		return String.format("_%s_%s", master.getRpaManagementToolType().getRpaManagementToolType(), account.getRpaScopeId());
	}

	/**
	 * RPAリソースとして自動登録するノードのファシリティIDを生成する。
	 * @param resourceInfo
	 * @param account
	 * @param master
	 * @return ファシリティID:_[RPA管理ツールタイプ]_[RPAスコープID]_[ホスト名 or IPアドレス]_[UUID]
	 */
	public static String generateRpaResourceNodeId(RpaResourceInfo resourceInfo, RpaManagementToolAccount account, RpaManagementToolMst master) {
		String hostname;
		// ホスト名またはIPアドスを用いる。
		if (!resourceInfo.getHostName().isEmpty()) {
			hostname = resourceInfo.getHostName();
		} else {
			hostname = resourceInfo.getIpAddress().replaceAll("\\.", "_").replaceAll(":", "_");
		}
		
		return String.format("_%s_%s_%s_%s", 
				master.getRpaManagementToolType().getRpaManagementToolType(), 
				account.getRpaScopeId(), 
				hostname, 
				UUID.nameUUIDFromBytes(resourceInfo.getRpaExecEnvId().getBytes()));
	}
	
	/**
	 * 引数のファシリティIDがRPA管理ツールサービスを表すスコープのIDかチェックする。
	 */
	public static boolean checkRpaScope(String facilityId) {
		try {
			for (RpaManagementToolAccount account : QueryUtil.getRpaAccountList_NONE()) {
					RpaManagementToolMst master = QueryUtil.getRpaManagementToolMstPK(account.getRpaManagementToolId());
					if (facilityId.equals(RpaUtil.generateRpaManagementScopeId(account, master))) {
						return true;
					}
			}
			
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * RPAリソースとして自動登録するノードのデフォルト値(Hinemosプロパティで定義)をセットする。<BR>
	 * @param nodeInfo
	 */
	public static void setDefaultNodeInfoValue(final NodeInfo nodeInfo) {
		// 即時反映用ポート
		nodeInfo.setAgentAwakePort(HinemosPropertyCommon.rpa_node_property_agent_awakeport.getIntegerValue());
		
		// ノード変数
		String key = HinemosPropertyCommon.rpa_node_property_node_variablename.getStringValue();
		String value = HinemosPropertyCommon.rpa_node_property_node_variablevalue.getStringValue();
		String[] keyArr = key.split(",");
		String[] valueArr = value.split(",");
		if (keyArr.length == valueArr.length) {
			List<NodeVariableInfo> vars = new ArrayList<NodeVariableInfo>();
			for (int i = 0; i < keyArr.length; i++) {
				if (!keyArr[i].isEmpty()) {
					NodeVariableInfo var = new NodeVariableInfo(nodeInfo.getFacilityId(), keyArr[i]);
					var.setNodeVariableValue(valueArr[i]);
					vars.add(var);
				}
			}
			if (!vars.isEmpty()) {
				nodeInfo.setNodeVariableInfo(vars);
			}
		}
		
		// ジョブ
		nodeInfo.setJobPriority(HinemosPropertyCommon.rpa_node_property_job_priority.getIntegerValue());
		nodeInfo.setJobMultiplicity(HinemosPropertyCommon.rpa_node_property_job_multiplicity.getIntegerValue());
		
		// SNMP
		nodeInfo.setSnmpUser(HinemosPropertyCommon.rpa_node_property_snmp_user.getStringValue());
		nodeInfo.setSnmpPort(HinemosPropertyCommon.rpa_node_property_snmp_port.getIntegerValue());
		nodeInfo.setSnmpCommunity(HinemosPropertyCommon.rpa_node_property_snmp_community.getStringValue());
		nodeInfo.setSnmpVersion(SnmpVersionConstant.stringToType(HinemosPropertyCommon.rpa_node_property_snmp_version.getStringValue()));
		nodeInfo.setSnmpSecurityLevel(HinemosPropertyCommon.rpa_node_property_snmp_securitylevel.getStringValue());
		nodeInfo.setSnmpAuthPassword(HinemosPropertyCommon.rpa_node_property_snmp_auth_password.getStringValue());
		nodeInfo.setSnmpPrivPassword(HinemosPropertyCommon.rpa_node_property_snmp_priv_password.getStringValue());
		nodeInfo.setSnmpAuthProtocol(HinemosPropertyCommon.rpa_node_property_snmp_auth_protocol.getStringValue());
		nodeInfo.setSnmpPrivProtocol(HinemosPropertyCommon.rpa_node_property_snmp_priv_protocol.getStringValue());
		nodeInfo.setSnmpTimeout(HinemosPropertyCommon.rpa_node_property_snmp_timeout.getIntegerValue());
		nodeInfo.setSnmpRetryCount(HinemosPropertyCommon.rpa_node_property_snmp_retries.getIntegerValue());
		
		// WBEM
		nodeInfo.setWbemUser(HinemosPropertyCommon.rpa_node_property_wbem_user.getStringValue());
		nodeInfo.setWbemUserPassword(HinemosPropertyCommon.rpa_node_property_wbem_userpassword.getStringValue());
		nodeInfo.setWbemPort(HinemosPropertyCommon.rpa_node_property_wbem_port.getIntegerValue());
		nodeInfo.setWbemProtocol(HinemosPropertyCommon.rpa_node_property_wbem_protocol.getStringValue());		
		nodeInfo.setWbemTimeout(HinemosPropertyCommon.rpa_node_property_wbem_timeout.getIntegerValue());
		nodeInfo.setWbemRetryCount(HinemosPropertyCommon.rpa_node_property_wbem_retries.getIntegerValue());

		// IPMI
		nodeInfo.setIpmiIpAddress(HinemosPropertyCommon.rpa_node_property_ipmi_ipaddress.getStringValue());
		nodeInfo.setIpmiPort(HinemosPropertyCommon.rpa_node_property_ipmi_port.getIntegerValue());
		nodeInfo.setIpmiUser(HinemosPropertyCommon.rpa_node_property_ipmi_user.getStringValue());
		nodeInfo.setIpmiUserPassword(HinemosPropertyCommon.rpa_node_property_ipmi_userpassword.getStringValue());
		nodeInfo.setIpmiTimeout(HinemosPropertyCommon.rpa_node_property_ipmi_timeout.getIntegerValue());
		nodeInfo.setIpmiRetries(HinemosPropertyCommon.rpa_node_property_ipmi_retries.getIntegerValue());
		nodeInfo.setIpmiProtocol(HinemosPropertyCommon.rpa_node_property_ipmi_protocol.getStringValue());
		nodeInfo.setIpmiLevel(HinemosPropertyCommon.rpa_node_property_ipmi_level.getStringValue());
		
		// WinRM
		nodeInfo.setWinrmUser(HinemosPropertyCommon.rpa_node_property_winrm_user.getStringValue());
		nodeInfo.setWinrmUserPassword(HinemosPropertyCommon.rpa_node_property_winrm_userpassword.getStringValue());
		nodeInfo.setWinrmVersion(HinemosPropertyCommon.rpa_node_property_winrm_version.getStringValue());		
		nodeInfo.setWinrmPort(HinemosPropertyCommon.rpa_node_property_winrm_port.getIntegerValue());
		nodeInfo.setWinrmProtocol(HinemosPropertyCommon.rpa_node_property_winrm_protocol.getStringValue());
		nodeInfo.setWinrmTimeout(HinemosPropertyCommon.rpa_node_property_winrm_timeout.getIntegerValue());
		nodeInfo.setWinrmRetries(HinemosPropertyCommon.rpa_node_property_winrm_retries.getIntegerValue());
		
		// SSH
		nodeInfo.setSshUser(HinemosPropertyCommon.rpa_node_property_ssh_user.getStringValue());
		nodeInfo.setSshUserPassword(HinemosPropertyCommon.rpa_node_property_ssh_userpassword.getStringValue());
		nodeInfo.setSshPrivateKeyFilepath(HinemosPropertyCommon.rpa_node_property_ssh_privkey_path.getStringValue());
		nodeInfo.setSshPrivateKeyPassphrase(HinemosPropertyCommon.rpa_node_property_ssh_privkey_passphrase.getStringValue());
		nodeInfo.setSshPort(HinemosPropertyCommon.rpa_node_property_ssh_port.getIntegerValue());
		nodeInfo.setSshTimeout(HinemosPropertyCommon.rpa_node_property_ssh_timeout.getIntegerValue());

		// 保守
		nodeInfo.setAdministrator(HinemosPropertyCommon.rpa_node_property_administrator.getStringValue());
		nodeInfo.setContact(HinemosPropertyCommon.rpa_node_property_contact.getStringValue());
	}
}
