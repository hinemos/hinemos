/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.commons.util.CryptUtil;
import com.clustercontrol.monitor.run.model.MonitorCheckInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;



/**
 * The persistent class for the cc_monitor_http_scenario_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_http_scenario_info", schema="setting")
@Cacheable(true)
public class HttpScenarioCheckInfo extends MonitorCheckInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String authType;
	private String authUser;
	private String authPassword;
	private Boolean proxyFlg;
	private String proxyUrl;
	private Integer proxyPort;
	private String proxyUser;
	private String proxyPassword;
	private Boolean monitoringPerPageFlg;
	private String userAgent;
	private Integer connectTimeout;
	private Integer requestTimeout;

	private List<Page> pages = new ArrayList<>();

	private MonitorInfo monitorInfo;

	public HttpScenarioCheckInfo() {
	}

	@Column(name="auth_type")
	public String getAuthType() {
		return this.authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}


	@Column(name="auth_user")
	public String getAuthUser() {
		return this.authUser;
	}

	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}

	@Transient
	public String getAuthPassword() {
		return CryptUtil.decrypt(getAuthPasswordCrypt());
	}

	public void setAuthPassword(String authPassword) {
		setAuthPasswordCrypt(CryptUtil.encrypt(authPassword));
	}

	@Column(name="auth_password")
	public String getAuthPasswordCrypt() {
		return this.authPassword;
	}

	public void setAuthPasswordCrypt(String authPassword) {
		this.authPassword = authPassword;
	}

	@Column(name="proxy_flg")
	public Boolean getProxyFlg() {
		return this.proxyFlg;
	}

	public void setProxyFlg(Boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}

	@Column(name="proxy_url")
	public String getProxyUrl() {
		return this.proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}


	@Column(name="proxy_port")
	public Integer getProxyPort() {
		return this.proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}


	@Column(name="proxy_user")
	public String getProxyUser() {
		return this.proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	@Transient
	public String getProxyPassword() {
		return CryptUtil.decrypt(getProxyPasswordCrypt());
	}

	public void setProxyPassword(String proxyPassword) {
		setProxyPasswordCrypt(CryptUtil.encrypt(proxyPassword));
	}

	@Column(name="proxy_password")
	public String getProxyPasswordCrypt() {
		return this.proxyPassword;
	}

	public void setProxyPasswordCrypt(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}


	@Column(name="monitoring_per_page_flg")
	public Boolean getMonitoringPerPageFlg() {
		return this.monitoringPerPageFlg;
	}

	public void setMonitoringPerPageFlg(Boolean monitoringPerPageFlg) {
		this.monitoringPerPageFlg = monitoringPerPageFlg;
	}


	@Column(name="user_agent")
	public String getUserAgent() {
		return this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	@Column(name="connect_timeout")
	public Integer getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}


	@OneToMany(mappedBy="monitorHttpScenarioInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<Page> getPages() {
		return this.pages;
	}

	public void setPages(List<Page> pages) {
		if (pages != null && pages.size() > 0) {
			Collections.sort(pages, new Comparator<Page>() {
				@Override
				public int compare(Page o1, Page o2) {
					return o1.getId().getPageOrderNo().compareTo(o2.getId().getPageOrderNo());
				}
			});
		}
		this.pages = pages;
	}


	//bi-directional one-to-one association to MonitorInfo
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn
	public MonitorInfo getMonitorInfo() {
		return this.monitorInfo;
	}

	@Deprecated
	public void setMonitorInfo(MonitorInfo monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	/**
	 * MonitorInfoオブジェクト参照設定<BR>
	 * 
	 * MonitorInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToMonitorInfo(MonitorInfo monitorInfo) {
		this.setMonitorInfo(monitorInfo);
		if (monitorInfo != null) {
			monitorInfo.setHttpScenarioCheckInfo(this);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// MonitorInfo
		if (this.monitorInfo != null) {
			this.monitorInfo.setHttpCheckInfo(null);
		}
	}

	@Column(name="request_timeout")
	public Integer getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

}