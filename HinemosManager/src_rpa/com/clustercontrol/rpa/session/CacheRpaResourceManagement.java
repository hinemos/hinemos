/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.session;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.RpaManagementToolMasterNotFound;
import com.clustercontrol.rpa.factory.RpaManagementRestDefine;
import com.clustercontrol.rpa.factory.RpaManagementRestTokenCache;
import com.clustercontrol.rpa.factory.bean.RpaResourceInfo;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.model.RpaManagementToolMst;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.rpa.util.RpaUtil;

public class CacheRpaResourceManagement {
	private static Log m_log = LogFactory.getLog( CacheRpaResourceManagement.class );

	private final RpaManagementToolAccount account;
	private RpaManagementToolMst rpaManagementToolMst;
	private RpaManagementRestDefine rpaManagementRestDefine;

	private List<RpaResourceInfo> resourceInfos = new ArrayList<>();

	
	public CacheRpaResourceManagement(RpaManagementToolAccount account) {
		this.account = account;
	}
	

	// RPAリソース情報を更新する。
	public void update() throws IOException, RpaManagementToolMasterNotFound, HinemosUnknown {
		m_log.trace(String.format("update() rpaScopeId=%s, rpaManagementToolId=%s", account.getRpaScopeId(), account.getRpaManagementToolId()));
		this.rpaManagementToolMst = QueryUtil.getRpaManagementToolMstPK(account.getRpaManagementToolId());
		m_log.trace(String.format("update() rpaManagementToolId=%s", rpaManagementToolMst.getRpaManagementToolId()));
		this.rpaManagementRestDefine = RpaUtil.getRestDefine(rpaManagementToolMst); 

		String proxyUrl;
		Integer proxyPort;
		String proxyUser;
		String proxyPassword;
		
		if (account.getProxyFlg()) {
			proxyUrl = account.getProxyUrl();
			proxyPort = account.getProxyPort();
			proxyUser = account.getProxyUser();
			proxyPassword = account.getProxyPassword();
		} else {
			proxyUrl = null;
			proxyPort = null;
			proxyUser = null;
			proxyPassword = null;
		}
		
		m_log.trace("update() start connect.");
		try (CloseableHttpClient client = RpaUtil.createHttpClient(proxyUrl, proxyPort, proxyUser, proxyPassword)) {
			// RPA管理ツールからリソース情報を取得
			String token = RpaManagementRestTokenCache.getInstance().getToken(account, rpaManagementRestDefine, client);
			resourceInfos = rpaManagementRestDefine.getRpaResourceInfo(account.getUrl(), token, client);
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | NullPointerException e) {
			// 想定外例外
			throw new HinemosUnknown(e);
		}
	}
	
	public RpaManagementToolMst getRpaManagementToolMst() {
		return rpaManagementToolMst;
	}


	public void setRpaManagementToolMst(RpaManagementToolMst rpaManagementToolMst) {
		this.rpaManagementToolMst = rpaManagementToolMst;
	}


	public RpaManagementRestDefine getRpaManagementRestDefine() {
		return rpaManagementRestDefine;
	}


	public void setRpaManagementRestDefine(RpaManagementRestDefine rpaManagementRestDefine) {
		this.rpaManagementRestDefine = rpaManagementRestDefine;
	}


	public List<RpaResourceInfo> getResourceInfos() {
		return resourceInfos;
	}


	public void setResourceInfos(List<RpaResourceInfo> resourceInfos) {
		this.resourceInfos = resourceInfos;
	}


	public RpaManagementToolAccount getRpaManagementToolAccount() {
		return account;
	}
	
	/**
	 *  RPA管理ツールがリソース取得が有効なバージョンである場合はtrue, 無効なバージョンの場合はfalseを返す。
	 */
	public boolean enabledRpaResourceDetection() {
		return getRpaManagementRestDefine().enabledRpaResourceDetection();
	}
}
